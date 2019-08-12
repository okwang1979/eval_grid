package nc.bs.hbbb.dxmodelfunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nc.bs.hbbb.contrast.ContrastBO;
import nc.bs.logging.Logger;
import nc.itf.hbbb.constants.HBFmlConst;
import nc.itf.hbbb.contrast.ContrastMeasPubDataCache;
import nc.itf.hbbb.contrast.IntrMeasProjectCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.MeasureDataProxy;
import nc.util.hbbb.OffsetHanlder;
import nc.util.hbbb.pub.HBPubItfService;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.contrast.IContrastConst;
import nc.vo.hbbb.dxfunction.project.MeasureReportVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.meetdata.MeetdatasubVO;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.unionproject.ProjectVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.script.base.ICalcEnv;

/**
 * INTR 内部交易取数函数
 * modified by Jiaah at 2013-11-18 北方水泥问题
 * @author liyra
 * @date 20110310
 * 
 */
public class INTRBO {
	public INTRBO() {
		super();
	}
	
	
	public double getINTR(String projectcode, int isself, int offset, ICalcEnv env) throws BusinessException {
		String pk_hbScheme =(String) env.getExEnv(IContrastConst.PK_HBSCHEME);
		ContrastQryVO qryvo = (ContrastQryVO) env.getExEnv(IContrastConst.CONTRASTQRYVO);
		HBSchemeVO schemeVO = (HBSchemeVO) ((com.ufsoft.script.spreadsheet.UfoCalcEnv) env).getExEnv(IContrastConst.HBSCHEMEVO);
		//预加载的映射关系
		Map<String, MeasureReportVO> prjoectMeasMapCache = new HashMap<String, MeasureReportVO>();
		Map<String, ProjectVO> prjoectVOCache = new HashMap<String, ProjectVO>();
		IntrMeasProjectCache cacheinstance = qryvo.getIntrMeaProjectinstance();
		if(cacheinstance != null){
			prjoectMeasMapCache= cacheinstance.getMeasRepVOs();
			prjoectVOCache = cacheinstance.getProjectVOs();
		}

		String key = pk_hbScheme + projectcode;
		MeasureReportVO measrepvo = prjoectMeasMapCache.get(key);
		if(measrepvo == null){
			measrepvo = HBProjectBOUtil.getProjectMeasVO(env, projectcode, true);
			if (null != measrepvo ) {
				if(null != cacheinstance)
					cacheinstance.insertMearepVO(key, measrepvo);
			}
			else{
				//解决没有映射关系时对账也能够进行下去
				AppDebug.debug("计算INTR函数时,编号为"+projectcode+"合并报表项目为空,请检查映射!");/*-=notranslate=-*/
//				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0455")/* @res "计算INTR函数时,编号为"*/+projectcode+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0529")/*@res"合并报表项目为空,请检查映射!"*/);
			}
		}
		
		if(measrepvo != null){
			MeasureVO measVO = measrepvo.getMeasVO();

			// 执行项目上挂的报表管理内部交易采集表权益调整 暂时只有INTR,DPSUM 俩个函数支持
			ProjectVO provo = prjoectVOCache.get(projectcode);
			if(provo == null){
				provo = HBProjectBOUtil.getProjectVOByCode(projectcode);
				if(null != cacheinstance)
					cacheinstance.insertProject(projectcode, provo);
			}
			
			Map<String, String> handOffset = qryvo.getOffset();
			//如果不是当期或者没有取到缓存，则重新查询
			if(offset != 0 || handOffset == null){
				handOffset =  OffsetHanlder.handOffset(schemeVO,qryvo.getKeymap(),offset);
			}
			
			String pk_org = "";
			String pk_other_org = "";
			if (isself == HBFmlConst.SELF_TO_OPP) {
				pk_org = (String) env.getExEnv(IContrastConst.PK_SELFCORP);
				pk_other_org = (String) env.getExEnv(IContrastConst.PK_OPPCORP);
			} else {
				pk_org = (String) env.getExEnv(IContrastConst.PK_OPPCORP);
				pk_other_org = (String) env.getExEnv(IContrastConst.PK_SELFCORP);
			}


			
			
			
			Map<String, String> oppEntityOrgs = qryvo.getOppEntityOrgs();
//			if (null != provo.getIsreportorg() && provo.getIsreportorg().booleanValue()) {// 是勾上了
//				pk_other_org = oppEntityOrgs.get(pk_other_org);
//			}
			if(oppEntityOrgs != null && oppEntityOrgs.size() > 0){
				if (oppEntityOrgs.get(pk_other_org) != null) {
					pk_other_org = oppEntityOrgs.get(pk_other_org);
				}
			}
			
//			KeyGroupVO subKeyGroupVO = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(measrepvo.getMeasVO().getKeyCombPK());
			//动态区关键字pk 目前支持一个动态区关键字
			String pk_dynkeyword = ContrastMeasPubDataCache.getInstance().getPk_dynKeyValue(measrepvo.getMeasVO().getKeyCombPK(),schemeVO,qryvo.getPkLock()).get(qryvo.getPkLock());
			//如果不是对方单位关键字则查询对应的内部客商信息
			String pk_selforg = pk_org;
			if(!pk_dynkeyword.equals(KeyVO.DIC_CORP_PK)){
				pk_other_org = qryvo.getOrg_supplier_map().get(pk_other_org);
				pk_selforg = qryvo.getOrg_supplier_map().get(pk_selforg);
				if(pk_other_org == null)
					return 0.0;
			}
			
			if(qryvo.getSelfOrgs()!=null &&qryvo.getOppOrgs()!=null){
				UFDouble ufDouble = UFDouble.ZERO_DBL;
				Map<String, UFDouble> map = qryvo.getResultMap().get(measVO.getCode());
				if(map != null ){
					ufDouble = map.get(pk_org+pk_other_org);
					if(ufDouble == null){
						return 0.0;
					}
				}else{
					resetQryVOResult(measrepvo, measVO, provo, qryvo, handOffset);
					map = qryvo.getResultMap().get(measVO.getCode());
					if(map == null)
						return 0.0;
					ufDouble = map.get(pk_org+pk_other_org);
					if(ufDouble == null){
						return 0.0;
					}
				}
				Map<String, String> orgs_aloneid_map = qryvo.getOrgs_aloneid_map();
				if(orgs_aloneid_map != null ){
					//环境变量中设置对账的数据来源aloneid和指标id
					if(env.getExEnv(IContrastConst.MEET) != null){
						MeetdatasubVO meetResultVO = (MeetdatasubVO) env.getExEnv(IContrastConst.MEET);
						meetResultVO.setAloneid(orgs_aloneid_map.get(pk_org + pk_other_org));
						meetResultVO.setMeasurecode(measVO.getCode());
						meetResultVO.setPk_opporg(pk_other_org);
						meetResultVO.setPk_selforg(pk_selforg);//如果报表是对方单位编码关键字，则存在的是客商的pk
					}
				}
				return ufDouble.doubleValue();
			}
			String alone_id = HBAloneIDUtil.getAloneID(pk_org, measrepvo.getMeasVO().getKeyCombPK(), handOffset, pk_other_org,pk_dynkeyword);
			if(alone_id==null) return 0.0;
			double result = this.getMeasureValue(measVO, alone_id);
			return result;
		}
		return 0.0;
	}
	
	/**
	 * 批取INTR函数的值
	 * @param measrepvo
	 * @param measVO
	 * @param provo
	 * @param qryvo
	 * @param handOffset
	 * @param selforgs
	 * @param opporgs
	 * @throws BusinessException
	 */
	/**
	 * @param measrepvo
	 * @param measVO
	 * @param provo
	 * @param qryvo
	 * @param handOffset
	 * @throws BusinessException
	 */
	private void resetQryVOResult(MeasureReportVO measrepvo, MeasureVO measVO, ProjectVO provo, ContrastQryVO qryvo, Map<String, String> handOffset)
			throws BusinessException {
		
		//批量获取够获取ALONEID
		HashMap<String,String>  selforgOpporg_AloneID_map  = new HashMap<String,String>();
		HashMap<String,MeasureDataVO>  AloneID_Value_map  = new HashMap<String,MeasureDataVO>();
		//修改效率：每次都new的话，那么每次都会执行resetQryVOResult，没有起到批量查询的目的
		Map<String, Map<String,UFDouble>>  resultMap   = qryvo.getResultMap();
		if(resultMap == null)
			resultMap = new HashMap<String, Map<String,UFDouble>>();
		
		ArrayList<MeasurePubDataVO>  arrayPubDataVOs = new ArrayList<MeasurePubDataVO>();
		ArrayList<String>   orgCombs = new ArrayList<String>();

		String code = measVO.getCode();
		//所有对账单位
		//zhaojian8 多线程对账存在取数不完整的问题
		//String[] contrastorgs = HBPubItfService.getRemoteContrastSrv().getContrastOrgs(qryvo.getPk_hbrepstru(), qryvo.getContrastorg());
		String[] contrastorgs = qryvo.getContrastorgs();
		if(contrastorgs == null || contrastorgs.length == 0){
			resultMap.put(code, new HashMap<String, UFDouble>());
			qryvo.setResultMap(resultMap);
			return;
		}
		
		//根据所有对方组织一次查询出虚实组织
		Map<String, String> oppEntityOrgs = qryvo.getOppEntityOrgs();
		
		KeyGroupVO subKeyGroupVO = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(measrepvo.getMeasVO().getKeyCombPK());
		KeyVO[] subkeys = subKeyGroupVO.getKeys();
		
		String timekey = null;
		for(int i = 0 ; i < subkeys .length ; i++){
			if(subkeys [i].isTTimeKeyVO()){
				timekey = subkeys [i].getPk_keyword();
				break;
			}
		}
		//pubdata缓存中的aloneid和schemee的唯一标识
		String pkLock;
		Map<String, MeasurePubDataVO> meslist;
		MeasurePubDataVO pubdataVO;
		String offset;
		Collection<MeasurePubDataVO> pubDataVOs;
		try {
			pkLock = qryvo.getPkLock();
			meslist = ContrastMeasPubDataCache.getInstance().getAllRelaMeasPubVOs(subKeyGroupVO,qryvo.getSchemevo(),qryvo).get(pkLock);
			pubdataVO = new MeasurePubDataVO();
			offset = ContrastMeasPubDataCache.getInstance().getKeyTimeTalbe().get(pkLock);
			pubDataVOs = meslist.values();		
			if(pubDataVOs.size() > 0){
				pubdataVO = pubDataVOs.toArray(new MeasurePubDataVO[0])[0];
				offset = pubdataVO.getKeywordByPK(timekey);
			}
		} catch (BusinessException e1) {
			AppDebug.error(e1.getMessage());
			throw new BusinessException(e1);
		}
		
		String timekeyvalue = null;//当前的时间关键字值
		if(null != subkeys  && null != handOffset){
			for(int i = 0 ; i < subkeys .length ; i++){
				if(subkeys [i].isTTimeKeyVO()){
					 timekeyvalue = handOffset.get(subkeys[i].getPk_keyword());
				}
			}
		}
		//jiaah--农垦：这种情况解决的是：当动态表中一条数据也没有的情况下，直接return；不在进行计算
		if(timekeyvalue.equals(offset) && pubDataVOs.size() == 0){
			resultMap.put(code, new HashMap<String, UFDouble>());
			qryvo.setResultMap(resultMap);
			return;
		}

		//动态区关键字pk 目前支持一个动态区关键字
		String pk_dynkeyword = ContrastMeasPubDataCache.getInstance().getPk_dynKeyValue(measrepvo.getMeasVO().getKeyCombPK(),qryvo.getSchemevo(),pkLock).get(pkLock);
		for(String s : contrastorgs){
			String selforg = s.substring(0,20);
			String oppOrg = s.substring(20,40);
//			if (null != provo.getIsreportorg() && provo.getIsreportorg().booleanValue()) {//是勾上了
//				
//			}
			//检查对方单位是不是虚组织
			if(oppEntityOrgs != null && oppEntityOrgs.size() > 0){
				if (oppEntityOrgs.get(oppOrg) != null) {
					oppOrg = oppEntityOrgs.get(oppOrg);
				}
			}
			if(!pk_dynkeyword.equals(KeyVO.DIC_CORP_PK)){
				oppOrg = qryvo.getOrg_supplier_map().get(oppOrg);
				if(oppOrg == null)
					continue;
			}
			
			StringBuffer keybuffer = new StringBuffer();
			keybuffer.append(selforg);
			keybuffer.append(oppOrg);
			keybuffer.append(timekeyvalue);
			keybuffer.append(subKeyGroupVO.getKeyGroupPK());
			MeasurePubDataVO vo = meslist.get(keybuffer.toString());
			
			if(vo != null){
				arrayPubDataVOs.add(vo);
			}
			else if(timekeyvalue.equals(offset)){
				continue;
			}
			else{
				try {
					//TODO:重新克隆一个vo，否则会改变cache中的measurevo
					MeasurePubDataVO pubdataVOClone = (MeasurePubDataVO) pubdataVO.clone();
					pubdataVOClone.setKeywordByPK(KeyVO.CORP_PK, selforg);
					pubdataVOClone.setKeywordByPK(pk_dynkeyword, oppOrg);
					pubdataVOClone.setKeywordByPK(timekey, timekeyvalue);
					MeasurePubDataVO[] findByKeywordArray = MeasurePubDataBO_Client.findByKeywordArray(new MeasurePubDataVO[]{pubdataVOClone});
					if(findByKeywordArray != null && findByKeywordArray.length > 0){
						arrayPubDataVOs.add(findByKeywordArray[0]);
						ContrastMeasPubDataCache.getInstance().insert(pk_dynkeyword,findByKeywordArray[0],pkLock);
					}
				} catch (Exception e) {
					AppDebug.debug(e);
				}
			}
			orgCombs.add(selforg+oppOrg);
		}
		
		try {
			MeasurePubDataVO[] findByKeywordArray = arrayPubDataVOs.toArray(new MeasurePubDataVO[0]);
			ArrayList<String>  aloneids = new ArrayList<String>();
			for (int i = 0; i < findByKeywordArray.length; i++) {
				MeasurePubDataVO measurePubDataVO = findByKeywordArray[i];
				if(measurePubDataVO==null) {
					continue;
				}
				String seleforg = measurePubDataVO.getKeywordByPK(KeyVO.CORP_PK);
				String opporg = measurePubDataVO.getKeywordByPK(pk_dynkeyword);
				selforgOpporg_AloneID_map.put(seleforg+opporg, measurePubDataVO.getAloneID());
				aloneids.add(measurePubDataVO.getAloneID());
			}
			MeasureDataVO[] datavos = HBPubItfService.getRemoteMeasureDataSrv().getRepData(aloneids.toArray(new String[0]), new MeasureVO[] {
				measVO
			});
			for (int i = 0; i < datavos.length; i++) {
				MeasureDataVO measureDataVO = datavos[i];
				AloneID_Value_map.put(measureDataVO.getAloneID(), measureDataVO);
			}
//				Set<String> keySet = selforgOpporg_AloneID_map.keySet();
			
			for (String string : orgCombs) {
				String string2 = selforgOpporg_AloneID_map.get(string);
				if(StringUtil.isEmptyWithTrim(string2) || AloneID_Value_map.get(string2) == null){
					if(!resultMap.containsKey(code)){
						HashMap<String,UFDouble> hashMap = new HashMap<String,UFDouble>();
						resultMap.put(code, hashMap);
					}
					continue;
				}
				MeasureDataVO measureDataVO = AloneID_Value_map.get(string2);
				if(resultMap.containsKey(code)){
					resultMap.get(code).put(string, measureDataVO.getUFDoubleValue());
				}else{
					HashMap<String,UFDouble> tmpMap = new HashMap<String,UFDouble>();
					tmpMap.put(string, measureDataVO.getUFDoubleValue());
					resultMap.put(measureDataVO.getMeasureVO().getCode(), tmpMap);
				}
//					selforgOpporg_Value_map.put(string, measureDataVO);
			}
			qryvo.setOrgs_aloneid_map(selforgOpporg_AloneID_map);
			qryvo.setResultMap(resultMap);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}


	private double getMeasureValue(MeasureVO measVO, String alone_id) throws BusinessException {
		MeasureVO[] measvos = new MeasureVO[] {
			measVO
		};
		MeasureDataVO[] datavos = MeasureDataProxy.getRepData(alone_id, measvos);
		if (null == datavos || datavos.length == 0) {
			return 0;
		}
		if (null != datavos[0]) {
			return datavos[0].getUFDoubleValue()==null ? 0.0:datavos[0].getUFDoubleValue().doubleValue();
		} else {
			return 0;
		}
	}

}