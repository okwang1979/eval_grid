package nc.bs.hbbb.union;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.framework.common.NCLocator;
import nc.bs.hbbb.org.util.HbOrgUtilBO;
import nc.bs.hbbb.project.ProjectSynchronize;
import nc.bs.logging.Logger;
import nc.bs.uif2.LockFailedException;
import nc.impl.hbbb.union.ImpUnionReport;
import nc.itf.hbbb.vouch.IVouchQrySrv;
import nc.itf.hbbb.vouch.constants.IVouchType;
import nc.itf.iufo.report.IUfoeRepDataSrv;
import nc.pub.hbbb.exception.UFOCUnThrowableException;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.data.RepDataBO_Client;
import nc.uif.pub.exception.UifException;
import nc.util.hbbb.HBBBReportUtil;
import nc.util.hbbb.MeasurePubDataUtil;
import nc.util.hbbb.UFOCSqlUtil;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.input.HBBBTableInputActionHandler;
import nc.util.hbbb.pub.HBPubItfService;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.util.ufoc.unionproject.ProjectSrvUtils;
import nc.vo.hbbb.hbscheme.HBSchemeReportVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.meetaccount.MeetResultBodyVO;
import nc.vo.hbbb.meetaccount.MeetResultHeadVO;
import nc.vo.hbbb.union.UnionReportConPreLoadVO;
import nc.vo.hbbb.union.UnionReportQryVO;
import nc.vo.hbbb.vouch.VouchHeadVO;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.verctrl.IDataVersionConsts;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.unionproject.ProjectVO;
import nc.vo.util.BDPKLockUtil;

import com.ufida.iufo.pub.tools.AppDebug;

public class UnionReportBO {

	private HBSchemeVO hbschemevo;
	private UnionReportQryVO qryvo;
	public static final String LOCK_HB_KEY="UFOC_HB";
	
	public UnionReportBO(UnionReportQryVO new_qryvo) throws BusinessException {
		super();
		qryvo = new_qryvo;
		HBSchemeVO schemeVO = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(qryvo.getPk_hbscheme());
		qryvo.setSchemeVo(schemeVO);
		setHbschemevo(schemeVO);
	}

	public void doUnion() throws BusinessException {

		if (null == this.hbschemevo || null== this.getHbschemevo().getPk_hbscheme()) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0467")/* @res "查询合并方案VO出错!!" */);
		}
		if (null == qryvo.getUnionorg() || qryvo.getUnionorg().trim().length() == 0) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0468")/* @res "合并组织不能为空!!" */);
		}
		// 检查有没有包含公司关键字
		if (!isContainCorpKeyWord()) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0469")/* @res "合并方案没有包含公司关键字!!" */);
		}
		try {
			// 合并pubdata
            MeasurePubDataVO pubdata = MeasurePubDataUtil.getMeasurePubdata(getHbschemevo().getVersion(), true, 
               		qryvo.getUnionorg(), qryvo.getKeymap(), getHbschemevo());
			String uinoinAloneid = pubdata.getAloneID();
			// 加锁，动态锁
			BDPKLockUtil.lockString(LOCK_HB_KEY + uinoinAloneid);
			checkHbSchemeParams(pubdata);
			String innercode = new HbOrgUtilBO().getInnerCode(qryvo.getPk_hbrepstru(), qryvo.getUnionorg());
			if (null == innercode || innercode.trim().length() == 0) {
				throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0452")/* @res "当前对账公司没有找到inner code" */);
			}
			String[] firstLevOrgs = new HbOrgUtilBO().getUnionOrgs(qryvo.getPk_hbrepstru(), innercode);
			String[] pk_reports = HBBaseDocItfService.getRemoteHBSchemeQrySrv().getReportIdByHBSchemeId(getHbschemevo().getPk_hbscheme());
			if (null == pk_reports || pk_reports.length == 0) {
				return;
			}
			//有序的报表
			HBSchemeReportVO[] allRepvos = ProjectSynchronize.getSortReportVos(pk_reports, getHbschemevo().getPk_hbscheme());
            //固定表pk
			Set<String> pk_fixrepids = new LinkedHashSet<String>();
			//第一张动态表pk，只有在第一张动态表的时候需要删除动态区的合并数据
			String pk_dynreppk = null;
			boolean bFindFirstDynRep = false;
			String[] pk_allrepids = new String[allRepvos.length];
			for (int i = 0; i < allRepvos.length; i++){
				String repID = allRepvos[i].getPk_report();
				pk_allrepids[i] = repID;
				if(!HBBBReportUtil.isIntrateRep(repID))
					pk_fixrepids.add(repID);
				else{
					if(!bFindFirstDynRep){
						pk_dynreppk = repID;
						bFindFirstDynRep = true;
					}
						
				}
			}
			
			// 校验报表分布式信息
			IUfoeRepDataSrv repDataSrv = nc.bs.framework.common.NCLocator.getInstance().lookup(IUfoeRepDataSrv.class);
            String[] repPks = new String[allRepvos.length];
			for (int i = 0; i < allRepvos.length; i++) {
				String pk_report = allRepvos[i].getPk_report();
				repPks[i]=pk_report;
		        String dataOrigin = repDataSrv.checkRepCommitDataOrigin(pk_report, uinoinAloneid);
				if (dataOrigin != null) {
				    throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0",
		                       "01830003-0102")/* @res报表 */
     	                       + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0103")/* @res数据来自于分布式系统[ */
	                           + dataOrigin + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0104")/* @res]，不能修改或删除！ */);
				}
			}
            //预加载meapubdata的合并版本、借贷版本
			HBUnionVouchDataCond vouchDataCond = new HBUnionVouchDataCond(pubdata, getHbschemevo());
			//删除抵销借贷版本
			// @edit by zhoushuang at 2014-4-10,上午10:43:17 只删除方案中所选标的所有指标，原来删除所有标的相关指标
			delelteDebitAndCredit(vouchDataCond,repPks);
			
           	//预先加载合并组织对应的虚实单位、是否叶子节点、组织对应vo、使用的报表的meapubdatavo等信息---jiaah
			String[] pk_unionids = new String[firstLevOrgs.length];
			for(int i = 0; i < firstLevOrgs.length; i++){
				pk_unionids[i] = firstLevOrgs[i].substring(0,20);
			}
            UnionReportConPreLoadVO preDataVO = new UnionReportConPreLoadVO(pk_unionids,pk_allrepids, qryvo);
            ImpUnionReport imp = new ImpUnionReport();
            //add by jiaah 动态区map；只有存在动态表的时候才会加载这些对账数据
            Map<String, Map<String, UFDouble>> dxvalumap = new HashMap<String, Map<String,UFDouble>>();
            Map<String, Map<String, UFDouble>> self_dxvaluemap = new HashMap<String, Map<String,UFDouble>>();
            if(pk_fixrepids.size() != allRepvos.length){
               	dxvalumap = getMeetVouchVos(vouchDataCond.getHBSchemeVO(),vouchDataCond.getPubdata());
               	self_dxvaluemap = getContrastDataMeetVouchVos(vouchDataCond.getHBSchemeVO(),vouchDataCond.getPubdata(),pk_unionids);
             }
            for (int i = 0; i < allRepvos.length; i++){
            	//央客客开  不合并内部交易表 by：许林 at：190711
            	if(IUFOCacheManager.getSingleton().getReportCache().getByPK(allRepvos[i].getPk_report()).getIsintrade().booleanValue()) continue;
            	//END
            	
            	
               	imp.doUnion(getQryvo(), allRepvos[i].getPk_report(), firstLevOrgs,preDataVO,vouchDataCond,dxvalumap,self_dxvaluemap,pk_dynreppk);
             }
				
			// 执行项目同步，只做固定表支持合并数的项目同步
			// 映射
			Map<String, ProjectVO> mappings = ProjectSrvUtils.getMeasureMappings(pk_fixrepids.toArray(new String[pk_fixrepids.size()]));
			// 执行同步
            ProjectSynchronize syn = new ProjectSynchronize();
            syn.setAryRepIDs(pk_fixrepids.toArray(new String[pk_fixrepids.size()]));
            syn.setDstPubdata(pubdata);
            syn.setMeasProMap(mappings);
            syn.setHbSchemeVo(this.getHbschemevo());
            syn.doSaveHBSynchronize();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			if (e instanceof LockFailedException) {
				throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0045")/* @res "其他用户正在执行该单位的合并报表,请稍后再试!" */);
			} else if(e instanceof UFOCUnThrowableException)
				throw new UFOCUnThrowableException(e.getMessage());
			else{
				throw new BusinessException(e.getMessage());
			}
		}
	}
    /**
     * TODO: 考虑效率问题/
     * {方法功能中文描述}
     * 
     * @param schemeVO
     * @param mainPubData
     * @return
     */
	private Map<String, Map<String, UFDouble>>  getMeetVouchVos(HBSchemeVO schemeVO,MeasurePubDataVO mainPubData){		
		//对账数据map<对方单位编码,map<mesurcode,double>>来源个别报表对应的对方单位编码，指标code，值
		Map<String, Map<String, UFDouble>> dxvalueMap = new HashMap<String, Map<String,UFDouble>>();
		
		//查询出抵销数据（对符的对账数据）
		//是否要求，这些对符的对账记录生成的抵销分录
		try {
			
//			String mainOrg = mainPubData.getKeywordByPK(KeyVO.CORP_PK);
			MeasurePubDataVO meetPubDataVO = MeasurePubDataUtil.getMeasurePubdata(mainPubData, IDataVersionConsts.VER_VOUCHER);

			VouchHeadVO[] headvos = HBPubItfService.getRemoteVouchQry().getVouchHead
					(meetPubDataVO.getAloneID(), schemeVO, new int[]{IVouchType.TYPE_AUTO_ENTRY}, null);
			
			if(headvos == null || headvos.length == 0 )
				return dxvalueMap;
			
			Set<String> pk_dxrelas = new HashSet<String>();
			for(VouchHeadVO head : headvos){
				pk_dxrelas.add(head.getPk_dxrela());
			}
			
			StringBuilder sqlWhere = new StringBuilder();
			sqlWhere.append(MeetResultHeadVO.ALONE_ID);
			sqlWhere.append("='");
			sqlWhere.append(meetPubDataVO.getAloneID());
			sqlWhere.append("' and ");
			sqlWhere.append(MeetResultHeadVO.PK_HBSCHEME);
			sqlWhere.append("='");
			sqlWhere.append(schemeVO.getPk_hbscheme());
//			sqlWhere.append("' and ");
//			sqlWhere.append(MeetResultHeadVO.PK_MEETORG);
//			sqlWhere.append("='");
//			sqlWhere.append(mainOrg);
			sqlWhere.append("' and ismeetable = 'Y'");
			sqlWhere.append(" and ");
			sqlWhere.append(UFOCSqlUtil.buildInSql(MeetResultHeadVO.PK_DXRELATION, pk_dxrelas));
			
			
			//JIAAH 解决查询超过了10000条记录
			MeetResultBodyVO[] filterRes = HBPubItfService.getRemoteMeetResultQry().queryMeetBodyVoByCondition(sqlWhere.toString());

			if(filterRes == null || filterRes.length == 0)
				return dxvalueMap;
			
			Map<String, UFDouble> valueMap = null;
			for(MeetResultBodyVO bodyvo : filterRes){
				String pk_opporg = bodyvo.getPk_opporg();
				String measurecode = bodyvo.getMeasurecode();
				if(measurecode == null)
					continue;
				valueMap = dxvalueMap.get(pk_opporg);
				UFDouble adjust_amount = bodyvo.getAdjust_amount();
				if(valueMap != null){
					UFDouble d = valueMap.get(measurecode);
					if(d == null)
						valueMap.put(measurecode, adjust_amount);
					else{
						UFDouble newd = d.add(adjust_amount);
						valueMap.put(measurecode, newd);
					}
				}
				else{
					valueMap = new HashMap<String, UFDouble>();
					valueMap.put(measurecode, adjust_amount);
					dxvalueMap.put(pk_opporg, valueMap);
				}
			}
			filterRes = null;
		} catch (UifException e) {
			AppDebug.debug(e.getMessage());
		} catch (BusinessException e) {
			AppDebug.debug(e.getMessage());
		}
		
		return dxvalueMap;
	}
	
	
	private Map<String, Map<String, UFDouble>>  getContrastDataMeetVouchVos(HBSchemeVO schemeVO,MeasurePubDataVO mainPubData,String[] pk_unionids){		
		//对账数据map<对方单位编码,map<mesurcode,double>>来源个别报表对应的本方单位编码，指标code，值
		Map<String, Map<String, UFDouble>> self_dxvaluemap = new HashMap<String, Map<String,UFDouble>>();
		
		//查询出抵销数据（对符的对账数据）
		//是否要求，这些对符的对账记录生成的抵销分录
		try {
			
//			List<MeetResultBodyVO> lsts = new ArrayList<MeetResultBodyVO>();
			
			//组织和aloneid的对应关系
			Map<String, String> org_aloneid_Map = getOrg_alonidMap(schemeVO,mainPubData, pk_unionids);
			
			VouchHeadVO[] allHeads = HBPubItfService.getRemoteVouchQry().getVouchHeadByAloneids
					(org_aloneid_Map.values().toArray(new String[org_aloneid_Map.size()]), schemeVO, new int[]{IVouchType.TYPE_AUTO_ENTRY}, null);
			
			Map<String, List<VouchHeadVO>> aloneid_vos = new HashMap<String, List<VouchHeadVO>>();
			for(VouchHeadVO headvo : allHeads){
				String aloneid = headvo.getAlone_id();
				List<VouchHeadVO> lst = aloneid_vos.get(aloneid);
				if(lst == null){
					lst = new ArrayList<VouchHeadVO>();
					lst.add(headvo);
					aloneid_vos.put(aloneid, lst);
				}else{
					lst.add(headvo);
				}
			}
			
			for(String s : pk_unionids){
				String aloneid = org_aloneid_Map.get(s);
				List<VouchHeadVO> headvos = aloneid_vos.get(aloneid);
				if(headvos == null || headvos.size() == 0 )
					continue;
				
				Set<String> pk_dxrelas = new HashSet<String>();
				for(VouchHeadVO head : headvos){
					pk_dxrelas.add(head.getPk_dxrela());
				}
				
				StringBuilder sqlWhere = new StringBuilder();
				sqlWhere.append(MeetResultHeadVO.ALONE_ID);
				sqlWhere.append("='");
				sqlWhere.append(aloneid);
				sqlWhere.append("' and ");
				sqlWhere.append(MeetResultHeadVO.PK_HBSCHEME);
				sqlWhere.append("='");
				sqlWhere.append(schemeVO.getPk_hbscheme());
//				sqlWhere.append("' and ");
//				sqlWhere.append(MeetResultHeadVO.PK_MEETORG);
//				sqlWhere.append("='");
//				sqlWhere.append(s);
				sqlWhere.append("' and ismeetable = 'Y'");
				sqlWhere.append(" and ");
				sqlWhere.append(UFOCSqlUtil.buildInSql(MeetResultHeadVO.PK_DXRELATION, pk_dxrelas));
				
				//JIAAH 解决查询超过了10000条记录
				MeetResultBodyVO[] filterRes = HBPubItfService.getRemoteMeetResultQry().queryMeetBodyVoByCondition(sqlWhere.toString());
				
				if(filterRes == null || filterRes.length == 0){
					continue;
				}
				
				Map<String, UFDouble> self_valueMap = null;
				
				for(MeetResultBodyVO bodyvo : filterRes){
					String pk_selforg = bodyvo.getPk_selforg();
					String measurecode = bodyvo.getMeasurecode();
					if(measurecode == null || pk_selforg == null)
						continue;
					self_valueMap = self_dxvaluemap.get(pk_selforg);
					UFDouble adjust_amount = bodyvo.getAdjust_amount();
					if(self_valueMap != null){
						UFDouble d = self_valueMap.get(measurecode);
						if(d == null)
							self_valueMap.put(measurecode, adjust_amount);
						else{
							UFDouble newd = d.add(adjust_amount);
							self_valueMap.put(measurecode, newd);
						}
					}
					else{
						self_valueMap = new HashMap<String, UFDouble>();
						self_valueMap.put(measurecode, adjust_amount);
						self_dxvaluemap.put(pk_selforg, self_valueMap);
					}
				}
				filterRes = null;
//				if(filterRes != null && filterRes.length > 0){
//					//每个层级的对账记录依次处理
//					lsts.addAll(Arrays.asList(filterRes));
//				}
			}
			
//			if(lsts == null || lsts.size() == 0)
//				return self_dxvaluemap;
//			
//			Map<String, UFDouble> self_valueMap = null;
//			for(MeetResultBodyVO bodyvo : lsts){
//				String pk_selforg = bodyvo.getPk_selforg();
//				String measurecode = bodyvo.getMeasurecode();
//				if(measurecode == null || pk_selforg == null)
//					continue;
//				self_valueMap = self_dxvaluemap.get(pk_selforg);
//				UFDouble adjust_amount = bodyvo.getAdjust_amount();
//				if(self_valueMap != null){
//					UFDouble d = self_valueMap.get(measurecode);
//					if(d == null)
//						self_valueMap.put(measurecode, adjust_amount);
//					else{
//						UFDouble newd = d.add(adjust_amount);
//						self_valueMap.put(measurecode, newd);
//					}
//				}
//				else{
//					self_valueMap = new HashMap<String, UFDouble>();
//					self_valueMap.put(measurecode, adjust_amount);
//					self_dxvaluemap.put(pk_selforg, self_valueMap);
//				}
//			}
		} catch (UifException e) {
			AppDebug.debug(e.getMessage());
		} catch (BusinessException e) {
			AppDebug.debug(e.getMessage());
		}
		return self_dxvaluemap;
	}
	
	/**
	 * 返回对账中间级的所有组织对应aloneid
	 * @param schemeVO
	 * @param mainPubData
	 * @param orgs
	 * @return
	 * @throws BusinessException
	 * @throws UFOSrvException
	 */
	private Map<String, String> getOrg_alonidMap(HBSchemeVO schemeVO,
			MeasurePubDataVO mainPubData, String[] orgs)
			throws BusinessException, UFOSrvException {
		Map<String,String> org_aloneid_Map = new HashMap<String, String>();
		KeyGroupVO groupVO = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(schemeVO.getPk_keygroup());
		KeyVO[] keys = groupVO.getKeys();
		StringBuffer buf = new StringBuffer();
		for(int i = 0 ; i < keys.length ; i++){
			String keyword = keys[i].getPk_keyword();
			if(keyword.equals(KeyVO.CORP_PK)){//动态区关键字的时候
				StringBuffer buf2 = new StringBuffer();
				buf2.append("keyword");
				buf2.append(groupVO.getIndexByKeywordPK(KeyVO.CORP_PK)+1);
				buf.append(UFOCSqlUtil.buildInSql(buf2.toString(), orgs));
				buf.append(" and ");
			}else{
				buf.append("keyword");
				buf.append(groupVO.getIndexByKeywordPK(keyword)+1);
				buf.append(" = '" + mainPubData.getKeywordByPK(keyword) + "' and ");
			}
		}
		buf.append(" ver ='");
		buf.append(IDataVersionConsts.VER_VOUCHER);
		buf.append("'");;
		MeasurePubDataVO[] findByKeywordArray = MeasurePubDataBO_Client.findBySqlCondition(groupVO.getKeyGroupPK(), buf.toString());
		for(MeasurePubDataVO vo : findByKeywordArray){
			org_aloneid_Map.put(vo.getKeywordByPK(KeyVO.CORP_PK), vo.getAloneID());
		}
		return org_aloneid_Map;
	}
	
	/**
	 * 删除抵销借贷版本、分类抵销借贷版本
	 * @param vouchCond
	 * @throws BusinessException
	 * @throws Exception
	 */
	private void delelteDebitAndCredit(HBUnionVouchDataCond vouchCond,String[] repPks) throws BusinessException, Exception {
		// 先得将所有报表的借贷版本清掉,否则再执行同步的时候,会产生累加
		List<MeasurePubDataVO> allDandCPubData = new ArrayList<MeasurePubDataVO>();
		MeasurePubDataVO debitpubdata = vouchCond.getDebitpubdata();
		allDandCPubData.add(debitpubdata);
		MeasurePubDataVO creaditpubdata = vouchCond.getCreaditpubdata();
		allDandCPubData.add(creaditpubdata);
		
		// 删除分类抵销借贷
		Map<String,MeasurePubDataVO[]> typeVouchs = vouchCond.getMap();
		Collection<MeasurePubDataVO[]> c = typeVouchs.values();
		for(MeasurePubDataVO[] m : c){
			if(m != null){
				allDandCPubData.add(m[0]);
				allDandCPubData.add(m[1]);
			}
		}
		// jiaah 20130706 删除合并数据：是为了防止mselect执行合计数的时候，取到上一次的合并结果
		allDandCPubData.add(vouchCond.getPubdata());
		// @edit by zhoushuang at 2014-4-10,上午10:43:17 只删除方案中所选标的所有指标，原来删除所有标的相关指标
		//MeasureDataBO_Client.deleteAllRepData(allDandCPubData.toArray(new MeasurePubDataVO[allDandCPubData.size()]));
		resetRepData(allDandCPubData.toArray(new MeasurePubDataVO[0]),repPks);
	}

	/**
	 * 删除报表数据
	 *
	 * @param pubdatas
	 * @param aryRepIDs
	 * @throws Exception
	 */
	private void resetRepData(MeasurePubDataVO[] pubdatas, String[] aryRepIDs)
			throws Exception {
		String pk_org = qryvo.getUnionorg();
		for (int i = 0; i < pubdatas.length; i++) {
			if (pubdatas[i] == null)
				continue;
			for (int j = 0; j < aryRepIDs.length; j++) {
				RepDataBO_Client.removeOneRepData(aryRepIDs[j], pk_org,
						pubdatas[i], false);
			}
		}

	}
	
	private boolean isContainCorpKeyWord() {
		boolean result = false;
		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton()
				.getKeyGroupCache().getByPK(getHbschemevo().getPk_keygroup());
		KeyVO[] keys = keygroupVo.getKeys();
		if (null == keys || keys.length == 0) {
			return result;
		}
		for (KeyVO key : keys) {
			if (null != key.getPk_keyword()
					&& key.getPk_keyword().trim().length() > 0
					&& key.getPk_keyword().equals(KeyVO.CORP_PK)) {
				return true;
			}
		}
		return result;
	}

	private void checkHbSchemeParams(MeasurePubDataVO pubdata) throws UFOSrvException{
		try {
			// 根据合并方案所抵销凭证必须审核,检查抵销凭证是否都已经审核,否则不允许合并
//			String pk_keygroup = this.hbschemevo.getPk_keygroup();
//			KeyGroupVO keyGroupVo = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
//			MeasurePubDataVO pubdata = new MeasurePubDataVO();
//			pubdata.setKeyGroup(keyGroupVo);
//			pubdata.setKType(pk_keygroup);
//			pubdata.setVer(this.hbschemevo.getVersion());
//			Map<String, String> keyMap = qryvo.getKeymap();
//			String[] keys = new String[keyMap.size()];
//			keyMap.keySet().toArray(keys);
//			if (null != keys && keys.length > 0) {
//				for (String key : keys) {
//					pubdata.setKeywordByPK(key, keyMap.get(key));
//				}
//			}
//			pubdata.setAccSchemePK(this.hbschemevo.getPk_accperiodscheme());
//			pubdata.setKeywordByPK(KeyVO.CORP_PK, keyMap.get(KeyVO.CORP_PK));
//			String aloneid = MeasurePubDataBO_Client.getAloneID(pubdata);
//			pubdata.setAloneID(aloneid);

			// 1 检查报表是否上报
			if (HBBBTableInputActionHandler.isSchemeCommit(pubdata,this.hbschemevo.getPk_hbscheme())) {
				throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0028")/*@res "报表已经上报,不允许合并!"*/);
			}

			// 2 检查合并方案下的所有凭证是否审核
			UFBoolean isdxreportverify = this.hbschemevo.getIsdxreportverify();
			if (isdxreportverify != null && isdxreportverify.booleanValue()) {
				MeasurePubDataVO clone = (MeasurePubDataVO) pubdata.clone();
				clone.setVer(IDataVersionConsts.VER_VOUCHER);
				clone.setAloneID(null);
				String aloneID2 = MeasureDataUtil.getAloneID(clone);
				// 抵销凭证必须检查该合并方案下所有的单位的抵销是否都已审核
				boolean qrySingleAdjVoucherIsAllVerify = NCLocator.getInstance().lookup(IVouchQrySrv.class).qryHBDXVoucherIsAllVerify(qryvo.getPk_hbscheme(),
								new String[] { qryvo.getUnionorg() },
								new String[] { aloneID2 });
				if (!qrySingleAdjVoucherIsAllVerify) {
					throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0277")/* @res "该合并方案下该合并组织的抵消凭证必须都审核!"*/);
			}
			}
		} catch (Exception e) {
			AppDebug.debug(e);
			if(e instanceof UFOCUnThrowableException)
				throw new UFOCUnThrowableException(e.getMessage());
			else
				throw new UFOSrvException(e.getMessage());
		}
	}
	
	public HBSchemeVO getHbschemevo() {
		return hbschemevo;
	}

	private void setHbschemevo(HBSchemeVO new_hbschemevo) {
		this.hbschemevo = new_hbschemevo;
	}

	public UnionReportQryVO getQryvo() {
		return qryvo;
	}

	public void setQryvo(UnionReportQryVO qryvo) {
		this.qryvo = qryvo;
	}

}