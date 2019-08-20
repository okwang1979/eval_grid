package nc.bs.hbbb.dxmodelfunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.logging.Logger;
import nc.itf.hbbb.constants.HBFmlConst;
import nc.itf.hbbb.contrast.ContrastMeasPubDataCache;
import nc.itf.hbbb.contrast.IntrMeasProjectCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.bd.intdata.UFDSSqlUtil;
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
 * INTR �ڲ�����ȡ������
 * modified by Jiaah at 2013-11-18 ����ˮ������
 * @author liyra
 * @date 20110310
 * 
 */
public class INTRBYKEYBO {
	public INTRBYKEYBO() {
		super();
	}
	
	
	public double getINTRBYKEY(String projectcode, int isself, int offset, String[] otherDynKeyToVal, ICalcEnv env) throws BusinessException {
		String pk_hbScheme =(String) env.getExEnv(IContrastConst.PK_HBSCHEME);
		ContrastQryVO qryvo = (ContrastQryVO) env.getExEnv(IContrastConst.CONTRASTQRYVO);
		qryvo.getKeymap().put(otherDynKeyToVal[0], otherDynKeyToVal[1]);
		if(otherDynKeyToVal.length==4){
			qryvo.getKeymap().put(otherDynKeyToVal[2], otherDynKeyToVal[3]);
		}
		HBSchemeVO schemeVO = (HBSchemeVO) ((com.ufsoft.script.spreadsheet.UfoCalcEnv) env).getExEnv(IContrastConst.HBSCHEMEVO);
		//Ԥ���ص�ӳ���ϵ
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
				//���û��ӳ���ϵʱ����Ҳ�ܹ�������ȥ
				AppDebug.debug("����INTR����ʱ,���Ϊ"+projectcode+"�ϲ�������ĿΪ��,����ӳ��!");/*-=notranslate=-*/
//				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0455")/* @res "����INTR����ʱ,���Ϊ"*/+projectcode+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0529")/*@res"�ϲ�������ĿΪ��,����ӳ��!"*/);
			}
		}
		
		if(measrepvo != null){
			MeasureVO measVO = measrepvo.getMeasVO();

			// ִ����Ŀ�Ϲҵı�������ڲ����ײɼ���Ȩ����� ��ʱֻ��INTR,DPSUM ��������֧��
			ProjectVO provo = prjoectVOCache.get(projectcode);
			if(provo == null){
				provo = HBProjectBOUtil.getProjectVOByCode(projectcode);
				if(null != cacheinstance)
					cacheinstance.insertProject(projectcode, provo);
			}
			
			Map<String, String> handOffset = qryvo.getOffset();
			//@edited by zhoushuang  2015.12.29 ��һ����̬ȡ�ؼ��ֵ�ֵ
//			handOffset.put(otherDynKeyToVal[0], otherDynKeyToVal[1]);
//			//������ǵ��ڻ���û��ȡ�����棬�����²�ѯ
//			if(offset != 0 || handOffset == null){
//				handOffset =  OffsetHanlder.handOffset(schemeVO,qryvo.getKeymap(),offset);
//			}
			
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
//			if (null != provo.getIsreportorg() && provo.getIsreportorg().booleanValue()) {// �ǹ�����
//				pk_other_org = oppEntityOrgs.get(pk_other_org);
//			}
			if(oppEntityOrgs != null && oppEntityOrgs.size() > 0)
				pk_other_org = oppEntityOrgs.get(pk_other_org);
			
			
			KeyGroupVO subKeyGroupVO = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(measrepvo.getMeasVO().getKeyCombPK());
			//��̬���ؼ���pk Ŀǰ֧��2����̬���ؼ���
			String[] pk_dynkeywords = ContrastMeasPubDataCache.getInstance().getPk_dynKeyValues(subKeyGroupVO,schemeVO,qryvo.getPkLock()).get(qryvo.getPkLock());
			List<String> pk_dynkeywordList = Arrays.asList(pk_dynkeywords);
			
			//@edited by zhoushuang  2015.12.29 ��λ����̵�pk
			String pk_dynCrop = KeyVO.DIC_CORP_PK;
			//@edited by zhoushuang  2015.12.29 ��һ���ؼ��ֵ�pk
			String pk_otherDynKey = otherDynKeyToVal[0];
			
			//������ǶԷ���λ�ؼ������ѯ��Ӧ���ڲ�������Ϣ
			String pk_selforg = pk_org;
			if(!pk_dynkeywordList.contains(KeyVO.DIC_CORP_PK)){
				for (int i = 0; i < pk_dynkeywords.length; i++) {
					if (!pk_dynkeywords[i].equals(pk_otherDynKey)) {
						pk_dynCrop = pk_dynkeywords[i];
						break;
					}
				}
				pk_other_org = qryvo.getOrg_supplier_map().get(pk_other_org);
				pk_selforg = qryvo.getOrg_supplier_map().get(pk_selforg);
				if(pk_other_org == null)
					return 0.0;
			}
			
			if(qryvo.getSelfOrgs()!=null &&qryvo.getOppOrgs()!=null){
				UFDouble ufDouble = UFDouble.ZERO_DBL;
				Map<String, UFDouble> map = qryvo.getResultMap().get(measVO.getCode()+otherDynKeyToVal[1]+getTwoValue(otherDynKeyToVal));
				
//				String cacheKey = pk_org + pk_other_org + otherDynKeyToVal[1];
				
//				if(otherDynKeyToVal.length==4){
//					cacheKey = cacheKey +  otherDynKeyToVal[3];
//				}
				if(map != null ){
					
					
 
					
					ufDouble = map.get(getMapStr(pk_org, pk_other_org , otherDynKeyToVal[1],getTwoValue(otherDynKeyToVal)));
					if(ufDouble == null){
						return 0.0;
					}
				}else{
					resetQryVOResult(measrepvo, measVO, provo, qryvo, handOffset, otherDynKeyToVal);
					map = qryvo.getResultMap().get(measVO.getCode()+otherDynKeyToVal[1]+getTwoValue(otherDynKeyToVal));
					if(map == null)
						return 0.0;
					ufDouble = map.get(getMapStr(pk_org, pk_other_org , otherDynKeyToVal[1],getTwoValue(otherDynKeyToVal)));
					if(ufDouble == null){
						return 0.0;
					}
				}
				Map<String, String> orgs_aloneid_map = qryvo.getOrgs_aloneid_map();
				if(orgs_aloneid_map != null ){
					//�������������ö��˵�������Դaloneid��ָ��id
					if(env.getExEnv(IContrastConst.MEET) != null){
						MeetdatasubVO meetResultVO = (MeetdatasubVO) env.getExEnv(IContrastConst.MEET);
						meetResultVO.setAloneid(orgs_aloneid_map.get(pk_org + pk_other_org));
						meetResultVO.setMeasurecode(measVO.getCode());
						meetResultVO.setPk_opporg(pk_other_org);
						meetResultVO.setPk_selforg(pk_selforg);//��������ǶԷ���λ����ؼ��֣�����ڵ��ǿ��̵�pk
					}
				}
				return ufDouble.doubleValue();
			}
			String alone_id = HBAloneIDUtil.getAloneID(pk_org, measrepvo.getMeasVO().getKeyCombPK(), handOffset, pk_other_org, pk_dynCrop);
			if(alone_id==null) return 0.0;
			double result = this.getMeasureValue(measVO, alone_id);
			return result;
		}
		return 0.0;
	}
	
	
	private String getTwoValue(String[] cons){
		if(cons.length==4){
			return cons[3];
		}
		return "";
	}
	
	/**
	 * ��ȡINTR������ֵ
	 * @param measrepvo
	 * @param measVO
	 * @param provo
	 * @param qryvo
	 * @param handOffset
	 * @param selforgs
	 * @param opporgs
	 * @throws BusinessException
	 */
	private void resetQryVOResult(MeasureReportVO measrepvo, MeasureVO measVO, ProjectVO provo, ContrastQryVO qryvo, Map<String, String> handOffset, String[] otherDynKeyToVal)
			throws BusinessException {
		
		//������ȡ����ȡALONEID
		HashMap<String,String>  selforgOpporgotherDyn_AloneID_map  = new HashMap<String,String>();
		HashMap<String,MeasureDataVO>  AloneID_Value_map  = new HashMap<String,MeasureDataVO>();
		
		Map<String, Map<String,UFDouble>>  resultMap   = new HashMap<String, Map<String,UFDouble>>();
		ArrayList<MeasurePubDataVO>  arrayPubDataVOs = new ArrayList<MeasurePubDataVO>();
		ArrayList<String>   orgCombsWithOtherDyn = new ArrayList<String>();
		String key = 	measVO.getCode()+otherDynKeyToVal[1]+getTwoValue(otherDynKeyToVal);
		String code = measVO.getCode();
		//���ж��˵�λ
		String[] contrastorgs = qryvo.getContrastorgs();
		if(contrastorgs == null || contrastorgs.length == 0){
			resultMap.put(key, new HashMap<String, UFDouble>());
			qryvo.setResultMap(resultMap);
			return;
		}
		
		//�������жԷ���֯һ�β�ѯ����ʵ��֯
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
		//pubdata�����е�aloneid��schemee��Ψһ��ʶ
//		String pkLock = qryvo.getPkLock();
		//һ����ȡ�������ݻ��浽qryvo��
		
	
		

		Map<String , UFDouble> map =  qryvo.getResultMap().get(key);
		if(map!=null){
			map.get(key);
		}
		
		Set<String> orgs = new HashSet<>();
		Set<String> oppOrgs  = new HashSet<>();;
		
		for(String s : contrastorgs){
			String selforg = s.substring(0,20);
			String oppOrg = s.substring(20,40);
//			if (null != provo.getIsreportorg() && provo.getIsreportorg().booleanValue()) {//�ǹ�����
//				
//			}
			//���Է���λ�ǲ�������֯
			if(oppEntityOrgs != null && oppEntityOrgs.size() > 0)
				oppOrg = oppEntityOrgs.get(oppOrg);
			
			if(!KeyVO.DIC_CORP_PK.equals(KeyVO.DIC_CORP_PK)){
				oppOrg = qryvo.getOrg_supplier_map().get(oppOrg);
				if(oppOrg == null)
					continue;
			}
			orgs.add(selforg);
			oppOrgs.add(oppOrg);
		}	
		
		String timekeyvalue = null;//��ǰ��ʱ��ؼ���ֵ
		if(null != subkeys  && null != handOffset){
			for(int i = 0 ; i < subkeys .length ; i++){
				if(subkeys [i].isTTimeKeyVO()){
					 timekeyvalue = handOffset.get(subkeys[i].getPk_keyword());
				}
			}
		}
		
		StringBuffer buf = new StringBuffer();
//		Map<String, String> handOffset =qryvo.getOffset();
		for(int i = 0 ; i < subkeys.length ; i++){
			String pk_keyword = subkeys[i].getPk_keyword();
			if( subkeys[i].isTTimeKeyVO()){
				
			}
			
			
			
			//����
			if(pk_keyword.equals(KeyVO.CORP_PK)){
				String field = "keyword"+new Integer(subKeyGroupVO.getIndexByKeywordPK(KeyVO.CORP_PK)+1);
//				buf.append("keyword");
//				buf.append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.CORP_PK)+1);
			 

				buf.append( UFDSSqlUtil.getInClause(orgs.toArray(new String[0]), field));
				buf.append(" and ");
		
			}
			else if(pk_keyword.equals(KeyVO.DIC_CORP_PK)){//�Է���λ
				
////				StringBuffer buf1 = new StringBuffer();
//				buf.append("keyword");
//				buf.append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.DIC_CORP_PK)+1);
//			 
//
//				buf.append(" = '"+pk_other_org+"'");
				
				
				String field = "keyword"+new Integer(subKeyGroupVO.getIndexByKeywordPK(KeyVO.DIC_CORP_PK)+1);
//				buf.append("keyword");
//				buf.append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.CORP_PK)+1);
			 

				buf.append( UFDSSqlUtil.getInClause(oppOrgs.toArray(new String[0]), field));
				
				buf.append(" and ");
		
				
			}else if(subkeys[i].isTTimeKeyVO()){//�ڼ�
				
				handOffset = 	OffsetHanlder.handOffset(qryvo.getSchemevo(), handOffset, 0);
				buf.append("keyword");
				buf.append(subKeyGroupVO.getIndexByKeywordPK(subkeys[i].getPk_keyword())+1);
				buf.append(" =  '"+handOffset.get(subkeys[i].getPk_keyword())+"' and ");
			}else{//���ֵ�
				if(qryvo.getKeymap().get(pk_keyword)==null||otherDynKeyToVal[0].equals(pk_keyword)){
					continue;
				}
				buf.append("keyword");
				buf.append(subKeyGroupVO.getIndexByKeywordPK(pk_keyword)+1);
				
				buf.append(" = '" + qryvo.getKeymap().get(pk_keyword) + "' and ");
			}
		}
		
		buf.append("keyword");
		buf.append(subKeyGroupVO.getIndexByKeywordPK(otherDynKeyToVal[0])+1);
		buf.append(" = '" + otherDynKeyToVal[1] + "' and ");
		
		buf.append(" ver = 0");
		
		MeasurePubDataVO[] findByKeywordArray = MeasurePubDataBO_Client.findBySqlCondition(subKeyGroupVO.getKeyGroupPK(), buf.toString());
		
		
		
		if(findByKeywordArray==null||findByKeywordArray.length==0){
			return ;
		}
		ArrayList<String>  aloneids = new ArrayList<String>();
		Map<String, MeasurePubDataVO> alonMap = new HashMap<String, MeasurePubDataVO>();
		for(MeasurePubDataVO pubData:findByKeywordArray){
			alonMap.put(pubData.getAloneID(),pubData);
			aloneids.add(pubData.getAloneID());
		}
		MeasureDataVO[] datavos = HBPubItfService.getRemoteMeasureDataSrv().getRepData(aloneids.toArray(new String[0]), new MeasureVO[] {
			measrepvo.getMeasVO()
		});
		
	
		Map<String,UFDouble> queryDataMap  = new HashMap<>();
//		//�������
//		for(MeasurePubDataVO pubData:findByKeywordArray){
//			UFDouble value = new UFDouble();
			for(MeasureDataVO data:datavos){
				UFDouble value =  data.getUFDoubleValue();
				
				if(value==null||value.doubleValue()==0D){
					continue;
				}
				
				MeasurePubDataVO measurePub = alonMap.get(data.getAloneID());
				String org= measurePub.getKeywordByIndex(subKeyGroupVO.getIndexByKeywordPK(KeyVO.CORP_PK)+1);
				String opp_org= measurePub.getKeywordByIndex(subKeyGroupVO.getIndexByKeywordPK(KeyVO.DIC_CORP_PK)+1);
				
				String inKey = this.getMapStr(org, opp_org, otherDynKeyToVal[1],getTwoValue(otherDynKeyToVal));
				queryDataMap.put(inKey, value); 
//				if(pubData.getAloneID().equals(data.getAloneID())){
//					value = value.add(data.getUFDoubleValue());
//				}
			}
			
			
			resultMap.put(key, queryDataMap);
			
			qryvo.setResultMap(resultMap);
			
//			String key ="";
//			int  groupIdx = pubData.getKeyGroup().getIndexByKeywordPK(otherDynKeyToValPK[0]);
//			if( groupIdx>=0&&pubData.getKeywords().length>groupIdx){
//				  key = pubData.getKeywords()[groupIdx];
//			} 
//			result.put(key, value);
//			
//			 
//			
//		}
		
		
////		Map<String,MeasurePubDataVO> meslist = ContrastMeasPubDataCache.getInstance().get2AllRelaMeasPubVOs(subKeyGroupVO,qryvo.getSchemevo(),qryvo,pk_otherDynKey).get(pkLock+handOffset.get(pk_otherDynKey));
////		MeasurePubDataVO pubdataVO = new MeasurePubDataVO();
//		String offset = ContrastMeasPubDataCache.getInstance().getKeyTimeTalbe().get(pkLock);//�����ʱ��ؼ���ֵ
//		
//		
////		
////		if(meslist==null){
////			resultMap.put(code+handOffset.get(pk_otherDynKey), new HashMap<String, UFDouble>());
////			qryvo.setResultMap(resultMap);
////			return;
////		}
////		if(meslist.values().size() > 0){
////			pubdataVO = meslist.values().toArray(new MeasurePubDataVO[0])[0];
////			offset = pubdataVO.getKeywordByPK(timekey);
////		}
//		
//
////		//jiaah--ũ�ѣ��������������ǣ�����̬����һ������Ҳû�е�����£�ֱ��return�����ڽ��м���
////		if(timekeyvalue.equals(offset) && meslist.values().size() == 0){
////			resultMap.put(code+handOffset.get(pk_otherDynKey), new HashMap<String, UFDouble>());
////			qryvo.setResultMap(resultMap);
////			return;
////		}
//
//		//��̬���ؼ���pk Ŀǰ֧��һ����̬���ؼ���
//		String[] pk_dynkeywords = ContrastMeasPubDataCache.getInstance().getPk_dynKeyValues(subKeyGroupVO,qryvo.getSchemevo(),pkLock).get(pkLock);
//		
//		List<String> pk_dynkeywordList = Arrays.asList(pk_dynkeywords);
//		
//		
//		//@edited by zhoushuang  2015.12.29 ��λ����̵�pk
//		String pk_dynCrop = KeyVO.DIC_CORP_PK;
//		if(!pk_dynkeywordList.contains(KeyVO.DIC_CORP_PK)){
//			for (int i = 0; i < pk_dynkeywords.length; i++) {
//				if (!pk_dynkeywords[i].equals(pk_otherDynKey)) {
//					pk_dynCrop = pk_dynkeywords[i];
//					break;
//				}
//			}
//		}
//		
//		
//		
//		
//		
//		
//		for(String s : contrastorgs){
//			String selforg = s.substring(0,20);
//			String oppOrg = s.substring(20,40);
////			if (null != provo.getIsreportorg() && provo.getIsreportorg().booleanValue()) {//�ǹ�����
////				
////			}
//			//���Է���λ�ǲ�������֯
//			if(oppEntityOrgs != null && oppEntityOrgs.size() > 0)
//				oppOrg = oppEntityOrgs.get(oppOrg);
//			
//			if(!pk_dynCrop.equals(KeyVO.DIC_CORP_PK)){
//				oppOrg = qryvo.getOrg_supplier_map().get(oppOrg);
//				if(oppOrg == null)
//					continue;
//			}
//			
//			StringBuffer keybuffer = new StringBuffer();
//			keybuffer.append(selforg);
//			keybuffer.append(oppOrg);
//			keybuffer.append(otherDynKeyval);
//			keybuffer.append(timekeyvalue);
//			keybuffer.append(subKeyGroupVO.getKeyGroupPK());
//			MeasurePubDataVO vo = meslist.get(keybuffer.toString());
//			
//			if(vo != null){
//				arrayPubDataVOs.add(vo);
//			}
//			else{
//				try {
//					//TODO:���¿�¡һ��vo�������ı�cache�е�measurevo
//					MeasurePubDataVO pubdataVOClone = (MeasurePubDataVO) pubdataVO.clone();
//					pubdataVOClone.setKeywordByPK(KeyVO.CORP_PK, selforg);
//					pubdataVOClone.setKeywordByPK(pk_dynCrop, oppOrg);
//					pubdataVOClone.setKeywordByPK(timekey, timekeyvalue);
//					pubdataVOClone.setKeywordByPK(pk_otherDynKey, otherDynKeyval);
//					MeasurePubDataVO[] findByKeywordArray = MeasurePubDataBO_Client.findByKeywordArray(new MeasurePubDataVO[]{pubdataVOClone});
//					if(findByKeywordArray != null && findByKeywordArray.length > 0){
//						arrayPubDataVOs.add(findByKeywordArray[0]);
//						ContrastMeasPubDataCache.getInstance().insert(pk_dynCrop+pk_otherDynKey,findByKeywordArray[0],pkLock);
//					}
//				} catch (Exception e) {
//					AppDebug.debug(e);
//				}
//			}
//			orgCombsWithOtherDyn.add(selforg+oppOrg+otherDynKeyval);
//		}
//		
//		try {
//			MeasurePubDataVO[] findByKeywordArray = arrayPubDataVOs.toArray(new MeasurePubDataVO[0]);
//			ArrayList<String>  aloneids = new ArrayList<String>();
//			for (int i = 0; i < findByKeywordArray.length; i++) {
//				MeasurePubDataVO measurePubDataVO = findByKeywordArray[i];
//				if(measurePubDataVO==null) {
//					continue;
//				}
//				String seleforg = measurePubDataVO.getKeywordByPK(KeyVO.CORP_PK);
//				String opporg = measurePubDataVO.getKeywordByPK(pk_dynCrop);
//				String otherDyn = measurePubDataVO.getKeywordByPK(pk_otherDynKey);
//				selforgOpporgotherDyn_AloneID_map.put(seleforg+opporg+otherDyn, measurePubDataVO.getAloneID());
//				aloneids.add(measurePubDataVO.getAloneID());
//			}
//			MeasureDataVO[] datavos = HBPubItfService.getRemoteMeasureDataSrv().getRepData(aloneids.toArray(new String[0]), new MeasureVO[] {
//				measVO
//			});
//			for (int i = 0; i < datavos.length; i++) {
//				MeasureDataVO measureDataVO = datavos[i];
//				AloneID_Value_map.put(measureDataVO.getAloneID(), measureDataVO);
//			}
////				Set<String> keySet = selforgOpporg_AloneID_map.keySet();
//			resultMap = qryvo.getResultMap();
//			for (String string : orgCombsWithOtherDyn) {
//				String string2 = selforgOpporgotherDyn_AloneID_map.get(string);
//				if(StringUtil.isEmptyWithTrim(string2)){
//					if(resultMap.containsKey(code+handOffset.get(pk_otherDynKey))){
//						resultMap.get(code+handOffset.get(pk_otherDynKey)).put(string, null);
//					}else{
//						HashMap<String,UFDouble> hashMap = new HashMap<String,UFDouble>();
//						hashMap.put(string, null);
//						resultMap.put(code+handOffset.get(pk_otherDynKey), hashMap);
//					}
//					continue;
//				}
//				MeasureDataVO measureDataVO = AloneID_Value_map.get(string2);
//				if(measureDataVO==null){
//					if(resultMap.containsKey(code+handOffset.get(pk_otherDynKey))){
//						resultMap.get(code+handOffset.get(pk_otherDynKey)).put(string, null);
//					}else{
//						HashMap<String,UFDouble> hashMap = new HashMap<String,UFDouble>();
//						hashMap.put(string, null);
//						resultMap.put(code+handOffset.get(pk_otherDynKey), hashMap);
//					}
//					continue;
//				}
//				if(resultMap.containsKey(measureDataVO.getMeasureVO().getCode()+handOffset.get(pk_otherDynKey))){
//					resultMap.get(measureDataVO.getMeasureVO().getCode()+handOffset.get(pk_otherDynKey)).put(string, measureDataVO.getUFDoubleValue());
//				}else{
//					HashMap<String,UFDouble> tmpMap = new HashMap<String,UFDouble>();
//					tmpMap.put(string, measureDataVO.getUFDoubleValue());
//					resultMap.put(measureDataVO.getMeasureVO().getCode()+handOffset.get(pk_otherDynKey), tmpMap);
//				}
////					selforgOpporg_Value_map.put(string, measureDataVO);
//			}
//			qryvo.setOrgsWithDyn_aloneid_map(selforgOpporgotherDyn_AloneID_map);
//			qryvo.setResultMap(resultMap);
//		} catch (Exception e) {
//			Logger.error(e.getMessage(), e);
//		}
	}
	
	
	private String getMapStr(String pk_org,String opp_org,String other_pk,String two_pk){
		if(two_pk==null){
			return "org:"+pk_org+"|opp:"+opp_org+"|other:"+other_pk;
		}else{
			return "org:"+pk_org+"|opp:"+opp_org+"|other:"+other_pk+"|other2:"+two_pk;
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