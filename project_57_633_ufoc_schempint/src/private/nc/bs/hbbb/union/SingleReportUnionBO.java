package nc.bs.hbbb.union;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.hbbb.project.ProjectSynchronize;
import nc.bs.logging.Logger;
import nc.bs.pub.SystemException;
import nc.impl.hbbb.vouchdata.VouchDataToDraftReportUtilBO;
import nc.itf.iufo.commit.ICommitManageService;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.pub.hbbb.exception.UFOCUnThrowableException;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.MeasureCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasureDataBO_Client;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.HBBBRepUtil;
import nc.util.hbbb.HBBBReportUtil;
import nc.util.hbbb.HBKeyGroupUtil;
import nc.util.hbbb.HBVersionUtil;
import nc.util.hbbb.MeasureDataProxy;
import nc.util.hbbb.MeasurePubDataUtil;
import nc.util.hbbb.UFOCSqlUtil;
import nc.util.iufo.pub.AuditUtil;
import nc.util.ufoc.unionproject.ProjectSrvUtils;
import nc.vo.hbbb.dxtype.DXTypeValue;
import nc.vo.hbbb.func.HBBBFuncQryVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.union.UnionReportConPreLoadVO;
import nc.vo.hbbb.union.UnionReportQryVO;
import nc.vo.hbbb.vouch.IVouchDirections;
import nc.vo.hbbb.vouchdata.VouchDadaToDraftRepQryVO;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.iufo.verctrl.IDataVersionConsts;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.unionproject.ProjectVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.table.CellsModel;

/**
 * modified by Jiaah at 2013-11-18 北方水泥补丁同步
 * @author jiaah
 *
 */
public class SingleReportUnionBO {

	private HBSchemeVO hbschemevo;

	private UnionReportQryVO qryvo;
	
	private static ICommitManageService service;

//	private Map<String, ReportCombineStruMemberVersionVO> reportMember_map = new HashMap<String, ReportCombineStruMemberVersionVO>();

	private synchronized static ICommitManageService getService() {
		if(service == null){
			service = NCLocator.getInstance().lookup(ICommitManageService.class);
		}
		return service;
	}

	public SingleReportUnionBO(UnionReportQryVO new_qryvo) {
		super();
		this.qryvo = new_qryvo;
		this.hbschemevo = new_qryvo.getSchemeVo();
//		initReportMemberVOs();
	}

//	private void initReportMemberVOs() {
//		try {
//			ReportCombineStruMemberVersionVO[] vos = HBBaseDocItfService.getRemoteHBRepStru().queryReportCombineStruMemberVersionByVersionId(this.getQryvo().getPk_hbrepstru());
//			for (int i = 0; i < vos.length; i++) {
//				ReportCombineStruMemberVersionVO vo = vos[i];
//				reportMember_map.put(vo.getPk_org(), vo);
//			}
//		} catch (BusinessException e) {
//			nc.bs.logging.Logger.error(e.getMessage(), e);
//		}
//	}

	private Map<String,List<MeasureDataVO>> getDynMeasureDatas(MeasurePubDataVO[] subPubDatas,MeasureVO[] dynmeasureVOs) throws BusinessException {

		Map<String,List<MeasureDataVO>> mapDataVO = new HashMap<String, List<MeasureDataVO>>();
		try {
			if (null == subPubDatas || subPubDatas.length == 0) {
				return null;
			}
			Set<String> aloneidset = new HashSet<String>();
			for (MeasurePubDataVO vo : subPubDatas) {
				aloneidset.add(vo.getAloneID());
				//可变表的指标数据
//				RepDataVO[] repData = RepDataBO_Client.loadRepData(pk_report, null, vo, null);
			}
			MeasureDataVO[] datavos = MeasureDataProxy.getRepData(aloneidset.toArray(new String[0]), dynmeasureVOs);
			for(MeasureDataVO vo : datavos){
				List<MeasureDataVO> lst = mapDataVO.get(vo.getAloneID());
				if(lst == null){
					lst = new ArrayList<MeasureDataVO>();
					lst.add(vo);
					mapDataVO.put(vo.getAloneID(), lst);
				}else{
					lst.add(vo);
				}
			}
		} catch (Exception e) {
			throw new BusinessException(e);
		}
		return mapDataVO;
	}

	private MeasureDataVO[] getMeasureDatas(MeasureVO[] fixmeasureVOs, List<MeasurePubDataVO> currPubDatalist) throws BusinessException {
		MeasureDataVO[] datavos = null;
		try {
			if (null == currPubDatalist || currPubDatalist.size() == 0) {
				return null;
			}
			MeasurePubDataVO[] vos = currPubDatalist.toArray(new MeasurePubDataVO[0]);
			Set<String> aloneidset = new HashSet<String>();
			for (MeasurePubDataVO vo : vos) {
				aloneidset.add(vo.getAloneID());
			}
			datavos = MeasureDataProxy.getRepData(aloneidset.toArray(new String[0]), fixmeasureVOs);
		} catch (Exception e) {
			throw new BusinessException(e);
		}
		return datavos;
	}

	private boolean isVoidOrg(MeasurePubDataVO Src_pubdata, String pk_org, Map<String, UFBoolean> map) throws BusinessException {
		boolean result = false;
		if (Src_pubdata.getVer() == 0) {
			if (null != map.get(pk_org) && map.get(pk_org).booleanValue() == true) {
				return true;
			}
		}
		return result;
	}
	/**
	 * 报表逐张合并
	 * 虚单位支持,当前的方案是若虚单位参与合并，则一定要保证虚单位的个别报表数据为空
	 * @param pk_report
	 * @param firstLevOrgs
	 * @param pk_hbrepstru
	 * @param preDataVO
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws NamingException
	 * @throws SQLException
	 */
	public void doUnionByReport(String pk_report, String[] firstLevOrgs,String pk_hbrepstru,UnionReportConPreLoadVO preDataVO,HBUnionVouchDataCond vouchDataCond,Map<String, Map<String, UFDouble>> dxvaluemap,Map<String, Map<String, UFDouble>> self_dxvaluemap,String pk_dynreppk) throws BusinessException, SystemException, NamingException, SQLException {
		try {

			//1、返回各子报表的关键字信息
			List<MeasurePubDataVO> currPubDatalist = new ArrayList<MeasurePubDataVO>();
			//虚组织对应的aloneid
			Set<String> voidOrgAloneidSet = new HashSet<String>();
			//所有组织的虚实单位对应--先加载虚实组织对照再加载pubdata， modifiey by jiaah
			Map<String, UFBoolean> oppEntityOrgs = preDataVO.getOrgEntityMaps();

			//执行合并时组织选用的报表的版本--pk_report-org->MeasurePubDataVO(包含aloneid)
			Map<String, Map<String, MeasurePubDataVO>> pubDataWithAloneIDVOs = preDataVO.getPubDataVosMaps();
			Map<String, MeasurePubDataVO> orgPubdata = 	pubDataWithAloneIDVOs.get(pk_report);
			Set<String> pk_entitiys = new HashSet<String>();
			for (String pk_orgwithinnercode : firstLevOrgs) {
				String pk_org = pk_orgwithinnercode.substring(0, 20);
				//当前组织需要取的报表版本
				MeasurePubDataVO destPubdata = orgPubdata.get(pk_org);
				if (destPubdata != null) {
				    if (this.isVoidOrg(destPubdata, pk_org, oppEntityOrgs)) {
				    	pk_entitiys.add(pk_org);
	                    voidOrgAloneidSet.add(destPubdata.getAloneID());
	                }
	                currPubDatalist.add(destPubdata);
				}
			}
			//如果子表的pubdata为null，则返回
			if (currPubDatalist.size() == 0) {
				throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0400")/*@res "所有下级个别报表都不存在"*/);
			}

			CellsModel formatModel = preDataVO.getPreCellModle().get(pk_report);
			
			if(HBBBReportUtil.isIntrateRep(pk_report)){
				//是否是多动态区关键字，是则不参与合并
				boolean flag = false;
				//主表关键字
				String mainpk = vouchDataCond.getPubdata().getKeyGroup().getKeyGroupPK();
				String[] allKeyGroupPks = IUFOCacheManager.getSingleton().getReportCache().getKeyCombs(pk_report);
				String[] dynKeyGroupPks = new String[allKeyGroupPks.length-1];
				int j=0;
				for (int i = 0; i < allKeyGroupPks.length; i++) {
					if (!allKeyGroupPks[i].equals(mainpk)) {
						dynKeyGroupPks[j] = allKeyGroupPks[i];
						j++;
					}
				}
				KeyGroupVO[] dynKeyGroupVOs = UFOCacheManager.getSingleton().getKeyGroupCache().getByPKs(dynKeyGroupPks);
				for (int i = 0; i < dynKeyGroupVOs.length; i++) {
					String[] pks = HBKeyGroupUtil.getPk_dynKeyValues(dynKeyGroupVOs[i], mainpk);
					if (pks!=null && pks.length>1) {
						flag = true;
						break;
					}
				}
				
				if(!flag){
					doDynRepUnion(pk_report, vouchDataCond, currPubDatalist,voidOrgAloneidSet,dxvaluemap,self_dxvaluemap,pk_entitiys,pk_dynreppk);
				}
			}
			else
				doFixRepUnion(pk_report, vouchDataCond, currPubDatalist,voidOrgAloneidSet,formatModel);

			//更新iufo_rep_commit表
			String strCurUserPK = AuditUtil.getCurrentUser();
			ICommitManageService commitSrv = getService();
			if(vouchDataCond.getPubdata() != null)
				commitSrv.addRepInputSate(getHbschemevo().getPk_hbscheme(), vouchDataCond.getPubdata().getAloneID(), pk_report, strCurUserPK, true, null);
		} catch (Exception e) {
			if(e instanceof UFOCUnThrowableException)
				throw new UFOCUnThrowableException(e.getMessage());
			else
				throw new BusinessException(e.getMessage());
		}
	}

	/**
	 * 动态表合并
	 * @param pk_report
	 * @param vouchDataCond
	 * @param currPubDatalist
	 * @param voidOrgAloneidSet
	 * @throws BusinessException
	 * @throws Exception
	 * @throws SystemException
	 * @throws NamingException
	 * @throws SQLException
	 */
	private void doDynRepUnion(String pk_report,HBUnionVouchDataCond vouchDataCond,
			List<MeasurePubDataVO> currPubDatalist,
			Set<String> voidOrgAloneidSet,Map<String, Map<String, UFDouble>> dxvaluemap,Map<String, Map<String, UFDouble>> self_dxvaluemap,Set<String> lstEntityOrgs,String pk_dynreppk)
			throws BusinessException, Exception, SystemException,
			NamingException, SQLException {

		//合并版本主表pubdata
		MeasurePubDataVO mainPubData = vouchDataCond.getPubdata();
		//所有指标
		MeasureVO[] measureVOs = IUFOCacheManager.getSingleton().getMeasureCache().
				loadMeasureByReportPKs(new String[] {pk_report});
		//固定区指标
		Set<MeasureVO> fixMeasures = new HashSet<MeasureVO>();
		//动态区指标
		Set<MeasureVO> dynMeasures = new HashSet<MeasureVO>();
		//动态区指标map
		Map<String, MeasureVO> dynMapMeasureMap = new HashMap<String, MeasureVO>();
		for(MeasureVO vo : measureVOs){
			String mainKeyGroup = mainPubData.getKType();
			if(vo.getKeyCombPK().equals(mainKeyGroup)){
				// @edit by zhoushuang at 2015-5-13,下午2:35:21  固定区排除字符型指标
				if (null != vo && (vo.getType() == IStoreCell.TYPE_NUMBER
						|| vo.getType() == IStoreCell.TYPE_BIGDECIMAL)){
					fixMeasures.add(vo);
				}
			}
			else{
				//modified by jiaah at 2014-2-18 动态区排除字符型指标
				if (null != vo && (vo.getType() == IStoreCell.TYPE_NUMBER
						|| vo.getType() == IStoreCell.TYPE_BIGDECIMAL)){
					dynMeasures.add(vo);
					dynMapMeasureMap.put(vo.getCode(), vo);
				}
			}
		}

		//所有的关键字组合
		String[] allKeyGroupPks = IUFOCacheManager.getSingleton().getReportCache().getKeyCombs(pk_report);
		if(allKeyGroupPks != null && allKeyGroupPks.length > 2){
			AppDebug.error("动态区存在多个关键字,暂不支持此种合并");/*-=notranslate=-*/
			return;
		}
		//主子表相同关键字，其在各自关键字组合中的位置可能不相同，建立两个间的对应关系
		Hashtable<Integer, Integer> hashPos = new Hashtable<Integer, Integer>();
		KeyGroupVO mainKeyGroupVO = mainPubData.getKeyGroup();
		KeyVO[] mainKeys = mainKeyGroupVO.getKeys();
		
		String pk_subgroup = allKeyGroupPks[1];
		KeyGroupVO subGropVO = (KeyGroupVO)IUFOCacheManager.getSingleton().getKeyGroupCache().get(pk_subgroup);
		KeyVO[] subKeys = subGropVO.getKeys();
		//动态区关键字pk 目前支持一个动态区关键字
		String pk_dynkeyword = "";
		for (int i = 0; i < subKeys.length; i++) {
			boolean has = false;
			for (int j = 0; j < mainKeys.length; j++) {
				if (mainKeys[j].equals(subKeys[i])) {
					hashPos.put(Integer.valueOf(j), Integer.valueOf(i));
					has = true;
					break;
				}
			}
			if(!has)
				pk_dynkeyword = subKeys[i].getPk_keyword();
		}

		//TODO:100家组织会查询100次；取得所有参与合并的组织的动态区关键字MeasurePubDataVO
		List<MeasurePubDataVO> allDynMeasurePubDataLst = new ArrayList<MeasurePubDataVO>();
		for(MeasurePubDataVO vo : currPubDatalist){
			StringBuilder whereSQL = new StringBuilder();
			for (int j = 0; j < mainKeys.length; j++) {
	            String value = vo.getKeywordByPK(mainKeyGroupVO.getKeys()[j].getPk_keyword());
	            if (value != null){
	            	whereSQL.append(" keyword");
	                whereSQL.append(hashPos.get(j)+1);
	                whereSQL.append("='");
	                whereSQL.append(value);
	                whereSQL.append("'");
	            }
	            whereSQL.append(" and  ");
	         }
			if(vo.getVer() == 0 || (vo.getVer()+"").startsWith(IDataVersionConsts.VERTYPE_SEPERATE_ADJUST+""))//个别报表或个别报表调整表取个别报表版本
				whereSQL.append(" ver=0 ");
			else if((vo.getVer()+"").startsWith(IDataVersionConsts.VERTYPE_HBBB+"")||
					(vo.getVer()+"").startsWith(IDataVersionConsts.VERTYPE_HBBB_ADJUST+"")){//合并报表或合并报表调整表取合并报表版本
				whereSQL.append(" ver= " );
				whereSQL.append(mainPubData.getVer());
			}

			MeasurePubDataVO[] allDynPubDatas = MeasurePubDataBO_Client.findBySqlCondition(pk_subgroup, whereSQL.toString());
			allDynMeasurePubDataLst.addAll(Arrays.asList(allDynPubDatas));
		}
		
		
		//1、清理所有虚单位的动态表个别报表,因为虚单位的个别报表数据是不能参与合并的
		if (lstEntityOrgs.size() > 0) {
			List<MeasurePubDataVO> lstEntityPubdatavo = new ArrayList<MeasurePubDataVO>();
			for(MeasurePubDataVO vo : allDynMeasurePubDataLst){
				String pk_org = vo.getUnitPK();
				if(lstEntityOrgs.contains(pk_org))
					lstEntityPubdatavo.add(vo);
			}
			if(lstEntityPubdatavo.size() > 0)
				allDynMeasurePubDataLst.removeAll(lstEntityPubdatavo);
		}

		//所有动态区关键字的pks
		Set<String> otherWords = new HashSet<String>();
		for(MeasurePubDataVO vo : allDynMeasurePubDataLst){
			otherWords.add(vo.getKeywordByIndex(vo.getKeyByPK(pk_dynkeyword)+1));
		}

		//只支持一个动态区关键字：map<对方单位编码pk,onePubData>
		Map<String, MeasurePubDataVO> mapReusltUnionPubData = new HashMap<String, MeasurePubDataVO>();
		Map<String, MeasurePubDataVO> mapReusltTotalPubData = new HashMap<String, MeasurePubDataVO>();
		Map<String, MeasurePubDataVO> mapReusltContrastPubData = new HashMap<String, MeasurePubDataVO>();
		//构造动态区的不同版本的关键字
		MeasurePubDataVO dynPubData = new MeasurePubDataVO();
		dynPubData.setKeyGroup(subGropVO);
		dynPubData.setAccSchemePK(mainPubData.getAccSchemePK());//
		dynPubData.setKType(subGropVO.getKeyGroupPK());
		dynPubData.setVer(mainPubData.getVer());
		for (int i = 0; i < mainKeys.length; i++) {
			int iMainPos = 0;
			if (hashPos.get(Integer.valueOf(i)) != null){
				iMainPos = hashPos.get(Integer.valueOf(i)).intValue();
				dynPubData.setKeywordByIndex(iMainPos+1, mainPubData.getKeywordByIndex(i+1));
			}
		}
		

		//合并抵消表版本
		int dxVersion = HBVersionUtil.getHBContrastByHBSchemeVO(vouchDataCond.getHBSchemeVO());
		int totalver = HBVersionUtil.getHBTotalyHBSchemeVO(getHbschemevo());
		//一次获取动态区的合并合计和抵消版本
		Map<String, Map<Integer,MeasurePubDataVO>> allRelaDynPubdatavoMap = getcachedynPubdatavos(mainPubData, subGropVO, pk_dynkeyword,otherWords, dynPubData);
		
		for(String s:otherWords) {
			Map<Integer,MeasurePubDataVO> vermap = allRelaDynPubdatavoMap.get(s);
			MeasurePubDataVO unionPubData = null;
			MeasurePubDataVO totalPubData = null;
			MeasurePubDataVO contrastPubDatavo = null;
			if(vermap != null){
				unionPubData = vermap.get(getHbschemevo().getVersion());
				if(unionPubData == null){
					unionPubData = getPubDataByVer(pk_dynkeyword, dynPubData,getHbschemevo().getVersion(), s);
				}
				totalPubData = vermap.get(totalver);
				if(totalPubData == null){
					totalPubData = getPubDataByVer(pk_dynkeyword, dynPubData,totalver, s);
				}
				contrastPubDatavo = vermap.get(dxVersion);
				if(contrastPubDatavo == null){
					contrastPubDatavo = getPubDataByVer(pk_dynkeyword,dynPubData, dxVersion, s);
				}
			}else{
				unionPubData = getPubDataByVer(pk_dynkeyword, dynPubData,getHbschemevo().getVersion(), s);
				totalPubData = getPubDataByVer(pk_dynkeyword, dynPubData,totalver, s);
				contrastPubDatavo = getPubDataByVer(pk_dynkeyword,dynPubData, dxVersion, s);
			}

			mapReusltUnionPubData.put(s, unionPubData);//合并版本
			mapReusltTotalPubData.put(s, totalPubData);//合计版本
			mapReusltContrastPubData.put(s, contrastPubDatavo);//合并抵销版本
		}
		
		//TODO:当61升级63的时候是不存在动态表的合并的数据的，所以删除有可能会报错吗？
		if(pk_dynreppk != null && pk_dynreppk.equals(pk_report)){//清除所有的动态区合并数据(只删除一次才可以)
			MeasureDataBO_Client.deleteAllRepData(mapReusltUnionPubData.values().toArray(new MeasurePubDataVO[0]));
			MeasureDataBO_Client.deleteAllRepData(mapReusltTotalPubData.values().toArray(new MeasurePubDataVO[0]));
			MeasureDataBO_Client.deleteAllRepData(mapReusltContrastPubData.values().toArray(new MeasurePubDataVO[0]));
		}
		
		
		
		
		//查询出所有动态区的数据  map<aloneid,指标数据list>
		Map<String,List<MeasureDataVO>> dynMapRepdatavos = getDynMeasureDatas(allDynMeasurePubDataLst.toArray(
				new MeasurePubDataVO[allDynMeasurePubDataLst.size()]),
				dynMeasures.toArray(new MeasureVO[dynMeasures.size()]));


		//只有存在动态区数据才执行求和抵销操作
		if(dynMapRepdatavos != null){
			//根据对方单位编码分组求和，得到一个map，map<对方单位编码,map<meascolumn + dbtable,double>>
			Map<String, Map<String, UFDouble>> resultMap = getMapofTotalValue(dynMeasures, subGropVO, pk_dynkeyword,dynMapRepdatavos);

			//查询出抵销数据（对符的对账数据）
			//对账数据map<对方单位编码,map<mesurcode,double>>来源个别报表对应的对方单位编码，指标code，值
			Map<String, Map<String, UFDouble>> dxvalueMap = dxvaluemap;


			//生成合计数据、抵销表数据、合并数据
			MeasureVO[] dynMeasureVos = new MeasureVO[dynMeasures.size()];
			dynMeasures.toArray(dynMeasureVos);
			for(Map.Entry<String,Map<String, UFDouble>> enter : resultMap.entrySet()){
				String pk_oppcode = enter.getKey();//对方单位编码
				Map<String, UFDouble> mapValues = (Map<String, UFDouble>) enter.getValue();
				//动态指标数据集合,用于生成合计表数据-抵销数据
				MeasureDataVO[] dataTotalvos = new MeasureDataVO[dynMapMeasureMap.size()];
				//动态指标数据集合,用于生成合并数据=合计表数据-抵销数据
				MeasureDataVO[] dataUnionvos = new MeasureDataVO[dynMapMeasureMap.size()];
				//动态指标数据集合,用于生成抵销表数据
//				MeasureDataVO[] dataContrastvos = new MeasureDataVO[dynMapMeasureMap.size()];
				String unionaloneid = mapReusltUnionPubData.get(pk_oppcode).getAloneID();
				String totalaloneid = mapReusltTotalPubData.get(pk_oppcode).getAloneID();
//				String contrastaloneid = mapReusltContrastPubData.get(pk_oppcode).getAloneID();
				for (int i = 0 ; i < dynMeasureVos.length ; i++) {
					MeasureDataVO uniondvo = new MeasureDataVO();
					MeasureVO meavo = dynMapMeasureMap.get(dynMeasureVos[i].getCode());
					uniondvo.setMeasureVO(meavo);
					MeasureDataVO totaldvo = (MeasureDataVO) uniondvo.clone();
//					MeasureDataVO contratdvo = (MeasureDataVO) uniondvo.clone();
					String dbcolumn = meavo.getDbcolumn();
					String dbtable = meavo.getDbtable();
					if(dxvalueMap.get(pk_oppcode) != null && dxvalueMap.get(pk_oppcode).get(meavo.getCode()) != null){
						UFDouble total = mapValues.get(dbcolumn + dbtable);
						UFDouble dx = dxvalueMap.get(pk_oppcode).get(meavo.getCode());
						UFDouble union = total.sub(dx);
						uniondvo.setDataValue(String.valueOf(union));
//						contratdvo.setDataValue(String.valueOf(UFDouble.ZERO_DBL.sub(dx)));
					}else{
						uniondvo.setDataValue(mapValues.get(dbcolumn + dbtable).toString());
//						contratdvo.setDataValue(UFDouble.ZERO_DBL.toString());
					}
					uniondvo.setAloneID(unionaloneid);
					dataUnionvos[i] = uniondvo;

					totaldvo.setDataValue(mapValues.get(dbcolumn + dbtable).toString());
					totaldvo.setAloneID(totalaloneid);
					dataTotalvos[i] = totaldvo;

//					contratdvo.setAloneID(contrastaloneid);
//					dataContrastvos[i] = contratdvo;

				}
				MeasureDataBO_Client.editRepData(unionaloneid, dataUnionvos);//动态区合并版本
				MeasureDataBO_Client.editRepData(totalaloneid, dataTotalvos);//动态区合计版本
//				MeasureDataBO_Client.editRepData(contrastaloneid, dataContrastvos);//动态区抵销版本数据
			}
			
			
			//浙报传媒的要求抵消表
			for(Map.Entry<String,Map<String, UFDouble>> enter : self_dxvaluemap.entrySet()){
				String pk_selforg = enter.getKey();//本方单位作为对方单位编码
				if(pk_selforg == null)
					continue;
				//动态指标数据集合,用于生成抵销表数据
				MeasureDataVO[] dataContrastvos = new MeasureDataVO[dynMapMeasureMap.size()];
				if (mapReusltContrastPubData.get(pk_selforg)==null) {
					continue;
				}
				String contrastaloneid = mapReusltContrastPubData.get(pk_selforg).getAloneID();
				//TODO:这里的aloneid是完整的吗？
				if(contrastaloneid == null)
					continue;
				
				for (int i = 0 ; i < dynMeasureVos.length ; i++) {
					MeasureDataVO contratdvo = new MeasureDataVO();
					MeasureVO meavo = dynMapMeasureMap.get(dynMeasureVos[i].getCode());
					contratdvo.setMeasureVO(meavo);
					
					if(self_dxvaluemap.get(pk_selforg) != null && self_dxvaluemap.get(pk_selforg).get(meavo.getCode()) != null){
						UFDouble dx = self_dxvaluemap.get(pk_selforg).get(meavo.getCode());
						contratdvo.setDataValue(String.valueOf(dx));
					}else{
						contratdvo.setDataValue(UFDouble.ZERO_DBL.toString());
					}

					contratdvo.setAloneID(contrastaloneid);
					dataContrastvos[i] = contratdvo;

				}
				MeasureDataBO_Client.editRepData(contrastaloneid, dataContrastvos);//动态区抵销版本数据
			}
		}

		
		//1、清理所有虚单位的个别报表,因为虚单位的个别报表数据是不能参与合并的
		//固定表的指标数据getMeasureDatas
		MeasureDataVO[] repdatavos = getMeasureDatas(fixMeasures.toArray(new MeasureVO[fixMeasures.size()]),currPubDatalist);
		if (voidOrgAloneidSet.size() > 0) {
			for (MeasureDataVO tvo : repdatavos) {
				if (voidOrgAloneidSet.contains(tvo.getAloneID())) {
					tvo.setDataValue(null);
				}
			}
		}

		//指标code-合计值
		ConcurrentHashMap<String, UFDouble> map = new ConcurrentHashMap<String, UFDouble>();
		//指标code-指标vo
		ConcurrentHashMap<String, IStoreCell> measuremap = new ConcurrentHashMap<String, IStoreCell>();
		//3、各子表数据简单累加求和：存在指标则累加，不存在指标数据则删除已有的合并相关数据
		if (null != repdatavos && repdatavos.length > 0) {
			for (MeasureDataVO tvo : repdatavos) {
				// modified by litfb @20120514只有数值和高精度数值类型指标参与合并
				if (null != tvo&& null != tvo.getMeasureVO()&& (tvo.getMeasureVO().getType() == IStoreCell.TYPE_NUMBER
						|| tvo.getMeasureVO().getType() == IStoreCell.TYPE_BIGDECIMAL)) {
					if (map.containsKey(tvo.getMeasureVO().getCode())) {
						UFDouble value = map.get(tvo.getMeasureVO().getCode());
						value = value.add(new UFDouble(tvo.getDataValue()));
						map.put(tvo.getMeasureVO().getCode(), value);
					} else {
						UFDouble value = new UFDouble(tvo.getDataValue());
						map.put(tvo.getMeasureVO().getCode(), value);
					}
					if (!measuremap.containsKey(tvo.getMeasureVO().getCode())) {
						measuremap.put(tvo.getMeasureVO().getCode(),tvo.getMeasureVO());
					}
				}
			}
		}else{
			//不存在指标：删除合并数据、抵销借、抵销贷、
			MeasureDataBO_Client.deleteRepData(mainPubData.getAloneID(), measureVOs);
			MeasureDataBO_Client.deleteRepData(vouchDataCond.getDebitpubdata().getAloneID(), measureVOs);
			MeasureDataBO_Client.deleteRepData(vouchDataCond.getCreaditpubdata().getAloneID(), measureVOs);
		}

		//TODO:未考虑固定区存在抵销数据的情况
		//3、抵销操作：生成合计版本、抵销借贷版本、抵销表版本、合并版本
		if (map.size() > 0) {
			//带合计数的指标数据集
			MeasureDataVO[] datavos = new MeasureDataVO[map.size()];
			//指标编码
			String[] keys = new String[map.size()];
			map.keySet().toArray(keys);
			//不带合计数的指标数据集合,用于生成抵销表数据
			MeasureDataVO[] contrastdatavos = new MeasureDataVO[map.size()];
			for (int i = 0; i < keys.length; i++) {
				MeasureDataVO dvo = new MeasureDataVO();
				dvo.setMeasureVO(measuremap.get(keys[i]));
				dvo.setDataValue(String.valueOf(map.get(keys[i])));
				datavos[i] = dvo;
				MeasureDataVO dvo1 = new MeasureDataVO();
				dvo1.setMeasureVO(measuremap.get(keys[i]));
				contrastdatavos[i] = dvo1;
			}

			//单独存储合并报表的合计数版本,因为在合计数需要执行MSELECT函数（去上一年调整合并结果数据）
			MeasurePubDataVO totalpubData = MeasurePubDataUtil.getMeasurePubdata(HBVersionUtil.
					getHBTotalyHBSchemeVO(getHbschemevo()), true,getQryvo().getUnionorg(), getHbschemevo().getPk_keygroup(),getQryvo().getKeymap(),getHbschemevo().getPk_accperiodscheme());

			// 合并单位的合计数报表
			String totalaloneid = totalpubData.getAloneID();
			for (MeasureDataVO dvo : datavos) {
				dvo.setAloneID(totalaloneid);
			}

			//保存合计数版本，同时合计数执行MESELECT函数
			MeasureDataBO_Client.editRepData(totalpubData.getAloneID(), datavos);

			//合计数列，执行MESELECT函数
			HBBBFuncQryVO qryTotalvo  = new  HBBBFuncQryVO();
			qryTotalvo.setPubdata(totalpubData);
			qryTotalvo.setHbSchemeVo(getHbschemevo());
			qryTotalvo.setAryRepIDs(new String[]{pk_report});
			qryTotalvo.setIsconvert(false);
			qryTotalvo.setbAddLeft(Boolean.FALSE);
			qryTotalvo.setStrUserID("");
			qryTotalvo.setMeasures(measureVOs);
			qryTotalvo.setNeedreplaceAdd(false);
			HBBBRepUtil.calcHBBBTotalFormulasWithMselectFunc(qryTotalvo);
		}

		//合并抵销表执行公式计算
		HBBBFuncQryVO qryvo = new HBBBFuncQryVO();
		qryvo.setAryRepIDs(new String[] {pk_report});
		qryvo.setbAddLeft(Boolean.FALSE);
		qryvo.setHbSchemeVo(getHbschemevo());
		qryvo.setIsconvert(false);
		qryvo.setMeasures(measureVOs);
		qryvo.setPubdata(vouchDataCond.getContrastpubdata());
		qryvo.setStrUserID("");
		qryvo.setNeedreplaceAdd(false);
		HBBBRepUtil.calcHBConvertFormulasWithOutMSelectFunc(qryvo);

		//合并数列，执行区域公式
		HBBBFuncQryVO qryUnionVO = new HBBBFuncQryVO();
		qryUnionVO.setPubdata(mainPubData);
		qryUnionVO.setHbSchemeVo(getHbschemevo());
		qryUnionVO.setAryRepIDs(new String[]{pk_report});
		qryUnionVO.setIsconvert(false);
		qryUnionVO.setbAddLeft(Boolean.FALSE);
		qryUnionVO.setStrUserID("");
		qryUnionVO.setMeasures(measureVOs);
		qryUnionVO.setNeedreplaceAdd(false);
		HBBBRepUtil.calcFormulasWithOutMSelectFuncWithOutTotalMeas(qryUnionVO);
	}

//	@SuppressWarnings("unchecked")
//	private Map<String, Map<String, UFDouble>> getMapofTotalValue(Set<MeasureVO> dynMeasures, KeyGroupVO subGropVO,
//			String pk_dynkeyword, Map<String, List<MeasureDataVO>> dynMapRepdatavos) throws BusinessException,DAOException {
//		//根据对方单位编码分组求和，得到一个map，map<对方单位编码,map<meascolumn,double>>
//		Map<String,Map<String, UFDouble>> resultMap = new HashMap<String, Map<String,UFDouble>>();
//		
//		Set<String> aloneids = dynMapRepdatavos.keySet();
//		StringBuilder selectSql = new StringBuilder();
//		selectSql.append("select t2.keyword");
//		selectSql.append(subGropVO.getIndexByKeywordPK(pk_dynkeyword) + 1);
//		selectSql.append(",");
//
//		final MeasureVO[] vos = dynMeasures.toArray(new MeasureVO[dynMeasures.size()]);
//		for (int i = 0; i < vos.length; i++) {
//			IStoreCell measure = vos[i];
//			selectSql.append("sum(" + measure.getDbcolumn() + ")");
//			if (i < vos.length - 1) {
//				selectSql.append(",");
//			}
//		}
//		
//		selectSql.append(" from ");
//		selectSql.append(vos[0].getDbtable());
//		selectSql.append(" t1 ,");
//		selectSql.append(subGropVO.getTableName());
//		selectSql.append(" t2");
//		selectSql.append(" where t1.alone_id = t2.alone_id and  ");
//		//JIAAH 更改sql
//		selectSql.append(UFOCSqlUtil.buildInSql("t2.alone_id", aloneids));
//		selectSql.append("group by t2.keyword");
//		selectSql.append(subGropVO.getIndexByKeywordPK(pk_dynkeyword) + 1);
//		BaseDAO dao = new BaseDAO();
//		
//		resultMap = (Map<String, Map<String, UFDouble>>) dao.executeQuery(selectSql.toString(),
//				new ResultSetProcessor() {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public Object handleResultSet(ResultSet rs) throws SQLException {
//				Map<String,Map<String, UFDouble>> map = new HashMap<String,Map<String, UFDouble>>();
//				while (rs != null && rs.next()){
//					Map<String, UFDouble> clouValues = new HashMap<String, UFDouble>();;
//					for (int i = 0; i < vos.length; i++){
//						clouValues.put(vos[i].getDbcolumn(), new UFDouble(rs.getDouble(i+2)));
//					}
//					map.put(rs.getString(1), clouValues);
//				}
//				return map;
//			}
//		});
//		return resultMap;
//	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Map<String, UFDouble>> getMapofTotalValue(Set<MeasureVO> dynMeasures, KeyGroupVO subGropVO,
			String pk_dynkeyword, Map<String, List<MeasureDataVO>> dynMapRepdatavos) throws BusinessException,DAOException {
		//根据对方单位编码分组求和，得到一个map，map<对方单位编码,map<meascolumn+指标表名,double>>
		Map<String,Map<String, UFDouble>> resultMap = new HashMap<String, Map<String,UFDouble>>();
		//dynMeasures是有可能存在于不同的指标表中，所以先按照指标表划分measurevo
		Map<String,List<MeasureVO>> mapOfMeasure = new HashMap<String, List<MeasureVO>>();
		for(MeasureVO vo:dynMeasures){
			String table = vo.getDbtable();
			List<MeasureVO> lst = mapOfMeasure.get(table);
			if(lst == null){
				lst = new ArrayList<MeasureVO>();
				lst.add(vo);
				mapOfMeasure.put(table, lst);
			}else
				lst.add(vo);
		}
		
		Set<String> aloneids = dynMapRepdatavos.keySet();
		
		for(Map.Entry<String, List<MeasureVO>> enter : mapOfMeasure.entrySet()){
			final String dbtable = enter.getKey();
			List<MeasureVO> lstMeas = enter.getValue();
			
			
			StringBuilder selectSql = new StringBuilder();
			selectSql.append("select t2.keyword");
			selectSql.append(subGropVO.getIndexByKeywordPK(pk_dynkeyword) + 1);
			selectSql.append(",");

			final MeasureVO[] vos = lstMeas.toArray(new MeasureVO[lstMeas.size()]);
			for (int i = 0; i < vos.length; i++) {
				IStoreCell measure = vos[i];
				selectSql.append("sum(" + measure.getDbcolumn() + ")");
				if (i < vos.length - 1) {
					selectSql.append(",");
				}
			}
			
			selectSql.append(" from ");
			selectSql.append(dbtable);
			selectSql.append(" t1 ,");
			selectSql.append(subGropVO.getTableName());
			selectSql.append(" t2");
			selectSql.append(" where t1.alone_id = t2.alone_id and  ");
			//JIAAH 更改sql
			selectSql.append(UFOCSqlUtil.buildInSql("t2.alone_id", aloneids));
			selectSql.append("group by t2.keyword");
			selectSql.append(subGropVO.getIndexByKeywordPK(pk_dynkeyword) + 1);
			BaseDAO dao = new BaseDAO();
			
			Map<String, Map<String, UFDouble>> result = new HashMap<String, Map<String,UFDouble>>();
			result = (Map<String, Map<String, UFDouble>>) dao.executeQuery(selectSql.toString(),
					new ResultSetProcessor() {
				private static final long serialVersionUID = 1L;

				@Override
				public Object handleResultSet(ResultSet rs) throws SQLException {
					Map<String,Map<String, UFDouble>> map = new HashMap<String,Map<String, UFDouble>>();
					while (rs != null && rs.next()){
						Map<String, UFDouble> clouValues = new HashMap<String, UFDouble>();;
						for (int i = 0; i < vos.length; i++){
							clouValues.put(vos[i].getDbcolumn() + dbtable, new UFDouble(rs.getDouble(i+2)));
						}
						map.put(rs.getString(1), clouValues);
					}
					return map;
				}
			});
			
			if(result.size() == 0)
				continue;
			for(Map.Entry<String, Map<String, UFDouble>> enters: result.entrySet()){
				String key = enters.getKey();
				Map<String, UFDouble> value = enters.getValue();
				
				Map<String, UFDouble> newvalue = resultMap.get(key) ;
				if(newvalue == null){
					resultMap.put(key, value);
				}else{
					for(Map.Entry<String, UFDouble> s: value.entrySet()){
						newvalue.put(s.getKey(), s.getValue());
					}
				}
			}
		}
		return resultMap;
	}

	private MeasurePubDataVO getPubDataByVer(String pk_dynkeyword,MeasurePubDataVO dynPubData, int ver, String dynvalue)
			throws Exception {
		MeasurePubDataVO pubdata = (MeasurePubDataVO)dynPubData.clone();
		pubdata.setKeywordByPK(pk_dynkeyword, dynvalue);
		pubdata.setVer(ver);
		pubdata.setAloneID(null);
		String Aloneid = MeasurePubDataBO_Client.getAloneID(pubdata);
		pubdata.setAloneID(Aloneid);
		return pubdata;
	}

	/**
	 * 一次查询出数据库中的合并合计和抵消的动态区版本
	 * @param mainPubData
	 * @param subGropVO
	 * @param pk_dynkeyword
	 * @param otherWords
	 * @param dynPubData
	 * @return
	 */
	private Map<String, Map<Integer,MeasurePubDataVO>>  getcachedynPubdatavos(MeasurePubDataVO mainPubData,
			KeyGroupVO subGropVO, String pk_dynkeyword,Set<String> otherWords, MeasurePubDataVO dynPubData) {
		Map<String, Map<Integer,MeasurePubDataVO>> allRelaDynPubdatavoMap = new HashMap<String, Map<Integer,MeasurePubDataVO>>();
		//合并抵消表版本
		int dxVersion = HBVersionUtil.getHBContrastByHBSchemeVO(getHbschemevo());
		int totalver = HBVersionUtil.getHBTotalyHBSchemeVO(getHbschemevo());
		try {
			//查询动态区相关MeasurePubDataVO，没有查询币种关键字的不正确，内部客商的也有问题
			//条件中应该列上动态区的所有关键字
			//找出动态区的关键字
			KeyVO[] subKeys = subGropVO.getKeys();
			StringBuffer buf = new StringBuffer();
			for(int i = 0 ; i < subKeys.length ; i++){
				String keyword = subKeys[i].getPk_keyword();
				if(keyword.equals(pk_dynkeyword)){//动态区关键字的时候
					StringBuffer buf2 = new StringBuffer();
					buf2.append("keyword");
					buf2.append(subGropVO.getIndexByKeywordPK(pk_dynkeyword)+1);
					buf.append(UFOCSqlUtil.buildInSql(buf2.toString(), otherWords));
					buf.append(" and ");
				}else{
					buf.append("keyword");
					buf.append(subGropVO.getIndexByKeywordPK(keyword)+1);
					buf.append(" = '" + dynPubData.getKeywordByPK(keyword) + "' and ");
				}
			}
			buf.append(" ver in(");
			buf.append(mainPubData.getVer());
			buf.append(",");
			buf.append(totalver);
			buf.append(",");
			buf.append(dxVersion);
			buf.append(" )");
			MeasurePubDataVO[] findByKeywordArray = MeasurePubDataBO_Client.findBySqlCondition(subGropVO.getKeyGroupPK(), buf.toString());
			for(MeasurePubDataVO vo : findByKeywordArray){
				String otherpk = vo.getKeywordByPK(pk_dynkeyword);
				Map<Integer,MeasurePubDataVO> vermap = allRelaDynPubdatavoMap.get(otherpk);
				if(vermap == null){
					vermap = new HashMap<Integer, MeasurePubDataVO>();
				}
				vermap.put(vo.getVer(), vo);
				allRelaDynPubdatavoMap.put(otherpk, vermap);
			}
		} catch (Exception e) {
			AppDebug.debug(e);
		}
		return allRelaDynPubdatavoMap;
	}

	/**
	 * 固定表合并
	 * @param pk_report
	 * @param vouchDataCond
	 * @param currPubDatalist
	 * @param voidOrgAloneidSet
	 * @throws BusinessException
	 * @throws Exception
	 * @throws SystemException
	 * @throws NamingException
	 * @throws SQLException
	 */
	private void doFixRepUnion(String pk_report,HBUnionVouchDataCond vouchDataCond,
			List<MeasurePubDataVO> currPubDatalist,
			Set<String> voidOrgAloneidSet,CellsModel formatModel)
			throws BusinessException, Exception, SystemException,
			NamingException, SQLException {

		//合并版本pubdata
		MeasurePubDataVO pubdata = vouchDataCond.getPubdata();

		MeasureVO[] measureVOs = IUFOCacheManager.getSingleton().getMeasureCache().
				loadMeasureByReportPKs(new String[] {pk_report});
		MeasureDataVO[] repdatavos = getMeasureDatas(measureVOs,currPubDatalist);

		//2、清理所有虚单位的个别报表,因为虚单位的个别报表数据是不能参与合并的
		if (voidOrgAloneidSet.size() > 0) {
			for (MeasureDataVO tvo : repdatavos) {
				if (voidOrgAloneidSet.contains(tvo.getAloneID())) {
					tvo.setDataValue(null);
				}
			}
		}

		//指标code-合计值
		ConcurrentHashMap<String, UFDouble> map = new ConcurrentHashMap<String, UFDouble>();
		//指标code-指标vo
		ConcurrentHashMap<String, IStoreCell> measuremap = new ConcurrentHashMap<String, IStoreCell>();
		//3、各子表数据简单累加求和：存在指标则累加，不存在指标数据则删除已有的合并相关数据
		if (null != repdatavos && repdatavos.length > 0) {
			for (MeasureDataVO tvo : repdatavos) {
				// modified by litfb @20120514只有数值和高精度数值类型指标参与合并
				if (null != tvo&& null != tvo.getMeasureVO()&& (tvo.getMeasureVO().getType() == IStoreCell.TYPE_NUMBER
						|| tvo.getMeasureVO().getType() == IStoreCell.TYPE_BIGDECIMAL)) {
					if (map.containsKey(tvo.getMeasureVO().getCode())) {
						UFDouble value = map.get(tvo.getMeasureVO().getCode());
						value = value.add(new UFDouble(tvo.getDataValue()));
						map.put(tvo.getMeasureVO().getCode(), value);
					} else {
						UFDouble value = new UFDouble(tvo.getDataValue());
						map.put(tvo.getMeasureVO().getCode(), value);
					}
					if (!measuremap.containsKey(tvo.getMeasureVO().getCode())) {
						measuremap.put(tvo.getMeasureVO().getCode(),tvo.getMeasureVO());
					}
				}
			}
		}else{
			//不存在指标：删除合并数据、抵销借、抵销贷、
			MeasureDataBO_Client.deleteRepData(pubdata.getAloneID(), measureVOs);
			MeasureDataBO_Client.deleteRepData(vouchDataCond.getDebitpubdata().getAloneID(), measureVOs);
			MeasureDataBO_Client.deleteRepData(vouchDataCond.getCreaditpubdata().getAloneID(), measureVOs);
		}

		//4、抵销操作：生成合计版本、抵销借贷版本、抵销表版本、合并版本
		if (map.size() > 0) {
			//带合计数的指标数据集
			MeasureDataVO[] datavos = new MeasureDataVO[map.size()];
			//指标编码
			String[] keys = new String[map.size()];
			map.keySet().toArray(keys);
			//不带合计数的指标数据集合,用于生成抵销表数据
			MeasureDataVO[] contrastdatavos = new MeasureDataVO[map.size()];
			for (int i = 0; i < keys.length; i++) {
				MeasureDataVO dvo = new MeasureDataVO();
				dvo.setMeasureVO(measuremap.get(keys[i]));
				dvo.setDataValue(String.valueOf(map.get(keys[i])));
				datavos[i] = dvo;
				MeasureDataVO dvo1 = new MeasureDataVO();
				dvo1.setMeasureVO(measuremap.get(keys[i]));
				contrastdatavos[i] = dvo1;
			}

			//单独存储合并报表的合计数版本,因为在合计数需要执行MSELECT函数（去上一年调整合并结果数据）
			MeasurePubDataVO totalpubData = MeasurePubDataUtil.getMeasurePubdata(HBVersionUtil.
					getHBTotalyHBSchemeVO(getHbschemevo()), true,getQryvo().getUnionorg(), getHbschemevo().getPk_keygroup(),getQryvo().getKeymap(),getHbschemevo().getPk_accperiodscheme());

			// 合并单位的合计数报表
			String totalaloneid = totalpubData.getAloneID();
			for (MeasureDataVO dvo : datavos) {
				dvo.setAloneID(totalaloneid);
			}

			//4.1: 保存合计数版本，同时合计数执行MESELECT函数
			MeasureDataBO_Client.editRepData(totalpubData.getAloneID(), datavos);
			MeasureCache measureCache = UFOCacheManager.getSingleton().getMeasureCache();
			MeasureVO[] measuresWithData = measureCache.getMeasures(keys);
			HBBBFuncQryVO qryvo  =new  HBBBFuncQryVO();
			qryvo.setPubdata(totalpubData);
			qryvo.setHbSchemeVo(getHbschemevo());
			qryvo.setAryRepIDs(new String[]{pk_report});
			qryvo.setIsconvert(false);
			qryvo.setbAddLeft(Boolean.FALSE);
			qryvo.setStrUserID("");
			qryvo.setMeasures(measuresWithData);
			qryvo.setNeedreplaceAdd(false);
			qryvo.setFormatCellsModel(formatModel);
			HBBBRepUtil.calcHBBBTotalFormulasWithMselectFunc(qryvo);

			// 合并报表数据
			String aloneid = pubdata.getAloneID();
			ArrayList<MeasurePubDataVO>  totalaraylist  = new ArrayList<MeasurePubDataVO>();
			totalaraylist.add(totalpubData);
			// 合并数MeasureData
			datavos = getMeasureDatas(measureVOs, totalaraylist);
			for (MeasureDataVO dvo : datavos) {
				dvo.setAloneID(aloneid);
			}

			//报表指标对应的项目
			Map<String, ProjectVO> measureProjectVOs = ProjectSrvUtils.getMeasureMappings(new String[] {pk_report});

			//4.2: 生成抵销借方数据、生成抵销贷方数据
			storevouchdata(vouchDataCond, pk_report, measureProjectVOs,IVouchDirections.DIRECTION_DEBIT,measureVOs,formatModel);
			storevouchdata(vouchDataCond,  pk_report, measureProjectVOs,IVouchDirections.DIRECTION_CREDIT,measureVOs,formatModel);

			//抵销借、贷方数据
			Map<String, UFDouble> debitmap = getVouchDatas(vouchDataCond.getDebitpubdata(),measureVOs);
			Map<String, UFDouble> creditmap = getVouchDatas(vouchDataCond.getCreaditpubdata(),measureVOs);

			//4.3:生成合并数版本
			//央客二开--合并数有多余的小数只保留两位小数。
	
//			if(vouchDataCond.getHBSchemeVO()!=null&&vouchDataCond.getHBSchemeVO().getCode().equals(313)){
				genUnionRepData(pk_report,pubdata,datavos, measureProjectVOs, debitmap,creditmap,true);

//			}else{
//				genUnionRepData(pk_report,pubdata,datavos, measureProjectVOs, debitmap,creditmap,false);
//
//			}
			 
			
			//4.4:生成分类抵销净额版本
			Map<String, MeasurePubDataVO[]> typeMeaPubData = vouchDataCond.getMap();
			typeMeaPubData.put(DXTypeValue.DXTYPE_PK, new MeasurePubDataVO[]{vouchDataCond.getDebitpubdata(),
					vouchDataCond.getCreaditpubdata(),vouchDataCond.getContrastpubdata()});
			for(Map.Entry<String, MeasurePubDataVO[]> enter:typeMeaPubData.entrySet()){
				MeasurePubDataVO[] pubs = enter.getValue();
				if(pubs != null){
					MeasurePubDataVO typedeb = pubs[0];
					MeasurePubDataVO typecre = pubs[1];
					MeasurePubDataVO typecontra = pubs[2];

					//抵销借、贷方数据
					Map<String, UFDouble> typedebitmap = getVouchDatas(typedeb,measureVOs);
					Map<String, UFDouble> typecreditmap = getVouchDatas(typecre,measureVOs);

					MeasureDataVO[] typeContrDatas = contrastdatavos.clone();
					//返回抵销表的数据contrastdatavos
					getContrastMeaDataValues(measureProjectVOs, typeContrDatas, typedebitmap,typecreditmap);
					//生成分类抵销表数据版本
					genContrastRepData(typecontra,typeContrDatas,measuresWithData, pk_report,formatModel);
				}
			}

			//4.6:执行项目同步，具体是将抵销借，抵销贷数据都执行下项目数据同步
			ProjectSynchronize syn = new ProjectSynchronize();
			syn.setDebitPubdata(vouchDataCond.getDebitpubdata());
			syn.setCreditPubdata(vouchDataCond.getCreaditpubdata());
			syn.setAryRepIDs(new String[] {pk_report});
			syn.setDstPubdata(pubdata);
			syn.setMeasProMap(measureProjectVOs);
			syn.setHbSchemeVo(getHbschemevo());
			syn.doHBSynchronize();

			//4.7:合并数列执行区域计算
			qryvo = new HBBBFuncQryVO();
			qryvo.setAryRepIDs(new String[] {pk_report});
			qryvo.setbAddLeft(Boolean.FALSE);
			qryvo.setHbSchemeVo(getHbschemevo());
			qryvo.setIsconvert(false);
			qryvo.setMeasures(measuresWithData);
			qryvo.setPubdata(pubdata);
			qryvo.setStrUserID(InvocationInfoProxy.getInstance().getUserId());
			qryvo.setNeedreplaceAdd(false);
			qryvo.setFormatCellsModel(formatModel);
			HBBBRepUtil.calcFormulasWithOutMSelectFuncWithOutTotalMeas(qryvo);
		}
	}

	private void genUnionRepData(String pk_report,MeasurePubDataVO pubdata,MeasureDataVO[] datavos,
			Map<String, ProjectVO> measureProjectVOs,
			Map<String, UFDouble> debitmap, Map<String, UFDouble> creditmap,boolean isTwoPoint) {
		try {
			for (MeasureDataVO dvo : datavos) {
				// @edit by zhoushuang at 2015-5-13,下午2:30:42  非数值型指标不参与合并
				if (null != dvo&& null != dvo.getMeasureVO() && (dvo.getMeasureVO().getType() == IStoreCell.TYPE_NUMBER
						|| dvo.getMeasureVO().getType() == IStoreCell.TYPE_BIGDECIMAL)) {
					if(StringUtil.isEmpty(dvo.getDataValue())){
						dvo.setDataValue(UFDouble.ZERO_DBL.toString());//防止有些value为"";
					}
					String measurekey = dvo.getMeasureVO().getCode();
					//根据合并项目的借贷方向加减
					ProjectVO projectVO = measureProjectVOs.get(measurekey);
					//有为空的情况,在这里先跳过
					if (projectVO == null) {
					
						if(isTwoPoint){
							UFDouble tempValue = new UFDouble(dvo.getDataValue(),2);
							dvo.setDataValue(tempValue.toString());
							continue; 
						}else{
							dvo.setDataValue(String.valueOf(dvo.getDataValue()));
							continue;
						}
						
					}
					Integer direction = projectVO.getDirection();
					UFDouble debitvalue = debitmap.get(measurekey) == null ? UFDouble.ZERO_DBL : debitmap.get(measurekey);
					UFDouble creditvalue = creditmap.get(measurekey) == null ? UFDouble.ZERO_DBL : creditmap.get(measurekey);
					UFDouble value = UFDouble.ZERO_DBL;
					UFDouble dvoValue = new UFDouble(dvo.getDataValue());
					if (direction != null && direction.intValue() == IVouchDirections.DIRECTION_DEBIT) {
						value = debitvalue.sub(creditvalue).add(dvoValue);
					} else if (direction != null && direction.intValue() == IVouchDirections.DIRECTION_CREDIT) {
						value = creditvalue.sub(debitvalue).add(dvoValue);
					}
					UFDouble testValue = new UFDouble(Math.abs( value.doubleValue()));
					
					double twoPointNum  =  Math.floor( testValue.multiply(100).doubleValue())*10000;
					double pointNum  =  Math.floor( testValue.multiply(1000000).doubleValue());
					if(twoPointNum!=pointNum){
						Logger.error("Measure data value err:"+dvo);
					}
					if(isTwoPoint){
						value = new UFDouble(value.doubleValue(),2);
					}
					dvo.setDataValue(value.toString());
				}
			}
			//4.4: 保存合并报表版本：datavos已经过加借减贷的操作
			RepDataVO repData = new RepDataVO(pk_report, getHbschemevo().getPk_keygroup());
			repData.setDatas(new MeasurePubDataVO[] {pubdata}, datavos);
			MeasureDataBO_Client.editRepData(repData.getMainPubData().getAloneID(), datavos);
		} catch (NumberFormatException e) {
			AppDebug.debug(e.getMessage());
		} catch (Exception e) {
			AppDebug.debug(e.getMessage());
		}
	}

	/**
	 * 生成合并抵销表数据
	 * @param pubdata合并pubdata
	 * @param contrastdatavos
	 * @param contrastrepData
	 * @param measuresWithData指标vos
	 * @param pk_report
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws NamingException
	 * @throws SQLException
	 */
	private void genContrastRepData(MeasurePubDataVO contrastPubData,MeasureDataVO[] contrastdatavos,MeasureVO[] measuresWithData,String pk_report,CellsModel formatModel) throws BusinessException, SystemException,
			NamingException, SQLException {
		if (null == contrastdatavos || contrastdatavos.length == 0) {
			return;
		}
		try {
			//生成抵销表数据
			RepDataVO contrRepData = new RepDataVO(pk_report, getHbschemevo().getPk_keygroup());
			for (MeasureDataVO vo : contrastdatavos) {
				vo.setAloneID(contrastPubData.getAloneID());
			}
			contrRepData.setDatas(new MeasurePubDataVO[] { contrastPubData }, contrastdatavos);
			MeasureDataBO_Client.editRepData(contrRepData.getMainPubData().getAloneID(), contrastdatavos);

			//公式计算
			HBBBFuncQryVO qryvo = new HBBBFuncQryVO();
			qryvo.setAryRepIDs(new String[] {pk_report});
			qryvo.setbAddLeft(Boolean.FALSE);
			qryvo.setHbSchemeVo(getHbschemevo());
			qryvo.setIsconvert(false);
			qryvo.setMeasures(measuresWithData);
			qryvo.setPubdata(contrastPubData);
			qryvo.setStrUserID("");
			qryvo.setNeedreplaceAdd(false);
			qryvo.setFormatCellsModel(formatModel);
			HBBBRepUtil.calcHBConvertFormulasWithOutMSelectFunc(qryvo);
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}

	private void storevouchdata(HBUnionVouchDataCond vouchDataCond,String pk_report, Map<String, ProjectVO> measureProjectMap, int DIRECTION,MeasureVO[] measureVOs,CellsModel formatmodel) throws BusinessException {
		VouchDadaToDraftRepQryVO dqryvo = new VouchDadaToDraftRepQryVO();
		MeasurePubDataVO pubdata = null;
		if(DIRECTION == IVouchDirections.DIRECTION_CREDIT)
			pubdata = vouchDataCond.getCreaditpubdata();
		else
			pubdata = vouchDataCond.getDebitpubdata();
		dqryvo.setUserid(getQryvo().getPk_user());
		dqryvo.setPubdata(pubdata);
		dqryvo.setAryRepIDs(new String[] {pk_report});
		dqryvo.setMappings(measureProjectMap);

		dqryvo.setStrVouchAloneID(vouchDataCond.getVouchPubData().getAloneID());
		dqryvo.setHbSchemeVo(getHbschemevo());
		dqryvo.setDirection(DIRECTION);
		dqryvo.setiVer(pubdata.getVer());
		new VouchDataToDraftReportUtilBO(dqryvo).storeHBBBVouchData(vouchDataCond,formatmodel);
	}

	/**
	 * 返回指定指标的值
	 * @param pubdata
	 * @param measureVOs
	 * @return
	 */
	private Map<String, UFDouble> getVouchDatas(MeasurePubDataVO pubdata,MeasureVO[] measureVOs){
		Map<String, UFDouble> result = new ConcurrentHashMap<String, UFDouble>();
		try {
			MeasureDataVO[] dataYs = null; // 原表工作底稿当前期间数据
			dataYs = MeasureDataProxy.getRepData(new String[] {pubdata.getAloneID()}, measureVOs);
			for (MeasureDataVO vo : dataYs) {
				result.put(vo.getCode(), new UFDouble(vo.getUFDoubleValue()==null ? 0.0:vo.getUFDoubleValue().doubleValue()));
			}
		} catch (NumberFormatException e) {
			AppDebug.debug(e.getMessage());
		} catch (BusinessException e) {
			AppDebug.debug(e.getMessage());
		}
		return result;
	}

	private void getContrastMeaDataValues(
			Map<String, ProjectVO> measureProjectVOs,
			MeasureDataVO[] contrastdatavos, Map<String, UFDouble> debitmap,
			Map<String, UFDouble> creditmap) {
		for (MeasureDataVO dvo : contrastdatavos) {
			String measurekey = dvo.getMeasureVO().getCode();
			// 这里生成抵消表数据,还是按照合并项目的借贷方向加减
			// 根据合并项目的借贷方向加减
			ProjectVO projectVO = measureProjectVOs.get(measurekey);
			// 有为空的情况,在这里先跳过
			if (projectVO == null) {
				continue;
			}
			Integer direction = projectVO.getDirection();

			UFDouble debitvalue = debitmap.get(measurekey) == null ? UFDouble.ZERO_DBL : debitmap.get(measurekey);
			UFDouble creditvalue = creditmap.get(measurekey) == null ? UFDouble.ZERO_DBL : creditmap.get(measurekey);
			UFDouble value = UFDouble.ZERO_DBL;
			if (direction != null && direction.intValue() == IVouchDirections.DIRECTION_DEBIT) {
				value = debitvalue.sub(creditvalue);
			} else if (direction != null && direction.intValue() == IVouchDirections.DIRECTION_CREDIT) {
				value = creditvalue.sub(debitvalue);
			}
			dvo.setDataValue(value.toString());
		}
	}

	public HBSchemeVO getHbschemevo() {
		return hbschemevo;
	}

	public UnionReportQryVO getQryvo() {
		return qryvo;
	}

}