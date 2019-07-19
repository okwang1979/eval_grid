package nc.bs.hbbb.dxrelation.dxfuncall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.hbbb.dxmodelfunction.HBProjectBOUtil;
import nc.itf.hbbb.constants.HBFmlConst;
import nc.itf.hbbb.contrast.ContrastMeasPubDataCache;
import nc.itf.hbbb.contrast.IntrMeasProjectCache;
import nc.itf.hbbb.dxrelation.IDxFunctionConst;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.OffsetHanlder;
import nc.util.hbbb.pub.HBPubItfService;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.contrast.IContrastConst;
import nc.vo.hbbb.dxfunction.project.MeasureReportVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.unionproject.ProjectVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.script.base.ICalcEnv;

public class INTRBYKEYCallFunc implements IDxCallFunc{

	@Override
	public Object callFunc(String strFuncName, Object[] objParams, ICalcEnv env)throws BusinessException {
		
		
		//ֱ��������ByKey�����㣬�ɼ������ŵ�env����������
		
		
		
		if(null==objParams || objParams.length==0  ){
			return null; 
		}
		String projectcode=HBProjectParamGetUtil.getProjectByParam(objParams[0]);
		
		int isself=0;
		if(objParams.length>1 && null!=objParams[1] && objParams[1] instanceof Integer){
			isself=(Integer)objParams[1];
		}
		
		int offset=0;
		if(objParams.length>2 && null!=objParams[2]){
			offset=new UFDouble(String.valueOf(objParams[2])).intValue() ;
		}
		Map<String,UFDouble> result = new HashMap<String, UFDouble>();
		env.setExEnv(IDxFunctionConst.INTRBYKEY_RESULT_KEY, result);
		
		String[] otherDynKeyToValPK = new String[2];
		if(objParams.length>3 && null!=objParams[3]){
			String keyword = String.valueOf(objParams[3]);
			String[] otherDynKeyToVal = keyword.split("=");
			KeyVO keyvo = UFOCacheManager.getSingleton().getKeywordCache().getByName(otherDynKeyToVal[0]);
			otherDynKeyToValPK[0] = keyvo.getPk_keyword();
//			otherDynKeyToValPK[1] =	HBPubItfService.getRemoteDxModelFunction().queryPKChooseKeyBYCode(keyvo,otherDynKeyToVal[1]);	
		}
		
		
//		String pk_hbScheme =(String) env.getExEnv(IContrastConst.PK_HBSCHEME);
//		ContrastQryVO qryvo = (ContrastQryVO) env.getExEnv(IContrastConst.CONTRASTQRYVO);
////		qryvo.getKeymap().put(otherDynKeyToVal[0], otherDynKeyToVal[1]);
//		HBSchemeVO schemeVO = (HBSchemeVO) ((com.ufsoft.script.spreadsheet.UfoCalcEnv) env).getExEnv(IContrastConst.HBSCHEMEVO);
		
		MeasureReportVO measure = getMeasure(env,projectcode);
		if(measure==null){
			return new UFDouble(0);
		}
		
		
		ProjectVO provo = HBProjectBOUtil.getProjectVOByCode(projectcode);
		
		
		String pk_org = "";
		String pk_other_org = "";
		if (isself == HBFmlConst.SELF_TO_OPP) {
			pk_org = (String) env.getExEnv(IContrastConst.PK_SELFCORP);
			pk_other_org = (String) env.getExEnv(IContrastConst.PK_OPPCORP);
		} else {
			pk_org = (String) env.getExEnv(IContrastConst.PK_OPPCORP);
			pk_other_org = (String) env.getExEnv(IContrastConst.PK_SELFCORP);
		}
		ContrastQryVO qryvo = (ContrastQryVO) env.getExEnv(IContrastConst.CONTRASTQRYVO);
		Map<String, String> oppEntityOrgs = qryvo.getOppEntityOrgs();
		if(oppEntityOrgs != null && oppEntityOrgs.size() > 0)
			pk_other_org = oppEntityOrgs.get(pk_other_org);
		

		KeyGroupVO subKeyGroupVO = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(measure.getMeasVO().getKeyCombPK());
		
		
 
		KeyVO[] subkeys = subKeyGroupVO.getKeys();
		
		//�ڼ� ���֣����壬�Է���λ
		StringBuffer buf = new StringBuffer();
		String pkLock = qryvo.getPkLock();
		HBSchemeVO schemeVO = (HBSchemeVO) ((com.ufsoft.script.spreadsheet.UfoCalcEnv) env).getExEnv(IContrastConst.HBSCHEMEVO);
		
		Map<String, String> handOffset =qryvo.getOffset();
		for(int i = 0 ; i < subkeys.length ; i++){
			String pk_keyword = subkeys[i].getPk_keyword();
			if( subkeys[i].isTTimeKeyVO()){
				
			}
			
			
			
			//����
			if(pk_keyword.equals(KeyVO.CORP_PK)){
//				StringBuffer buf1 = new StringBuffer();
				buf.append("keyword");
				buf.append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.CORP_PK)+1);
			 

				buf.append(" = '"+pk_org+"'");
				buf.append(" and ");
		
			}
			else if(pk_keyword.equals(KeyVO.DIC_CORP_PK)){//�Է���λ
				
//				StringBuffer buf1 = new StringBuffer();
				buf.append("keyword");
				buf.append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.DIC_CORP_PK)+1);
			 

				buf.append(" = '"+pk_other_org+"'");
				buf.append(" and ");
		
				
			}else if(subkeys[i].isTTimeKeyVO()){//�ڼ�
				
				handOffset = 	OffsetHanlder.handOffset(schemeVO, handOffset, offset);
				buf.append("keyword");
				buf.append(subKeyGroupVO.getIndexByKeywordPK(subkeys[i].getPk_keyword())+1);
				buf.append(" =  '"+handOffset.get(subkeys[i].getPk_keyword())+"' and ");
			}else{//���ֵ�
				if(qryvo.getKeymap().get(pk_keyword)==null||otherDynKeyToValPK[0].equals(pk_keyword)){
					continue;
				}
				buf.append("keyword");
				buf.append(subKeyGroupVO.getIndexByKeywordPK(pk_keyword)+1);
				
				buf.append(" = '" + qryvo.getKeymap().get(pk_keyword) + "' and ");
			}
		}
		buf.append(" ver = 0");
		
		MeasurePubDataVO[] findByKeywordArray = MeasurePubDataBO_Client.findBySqlCondition(subKeyGroupVO.getKeyGroupPK(), buf.toString());
		if(findByKeywordArray==null||findByKeywordArray.length==0){
			return 0D;
		}
		ArrayList<String>  aloneids = new ArrayList<String>();
		for(MeasurePubDataVO pubData:findByKeywordArray){
			aloneids.add(pubData.getAloneID());
		}
		MeasureDataVO[] datavos = HBPubItfService.getRemoteMeasureDataSrv().getRepData(aloneids.toArray(new String[0]), new MeasureVO[] {
			measure.getMeasVO()
		});
		
		//�������
		for(MeasurePubDataVO pubData:findByKeywordArray){
			UFDouble value = new UFDouble();
			for(MeasureDataVO data:datavos){
				if(pubData.getAloneID().equals(data.getAloneID())){
					value = value.add(data.getUFDoubleValue());
				}
			}
			String key ="";
			int  groupIdx = pubData.getKeyGroup().getIndexByKeywordPK(otherDynKeyToValPK[0]);
			if( groupIdx>=0&&pubData.getKeywords().length>groupIdx){
				  key = pubData.getKeywords()[groupIdx];
			} 
			result.put(key, value);
			
			 
			
		}
		if(1==1){
			return 0;
		}
//		HBSchemeVO schemeVO = (HBSchemeVO) ((com.ufsoft.script.spreadsheet.UfoCalcEnv) env).getExEnv(IContrastConst.HBSCHEMEVO);
		//��̬���ؼ���pk Ŀǰ֧��2����̬���ؼ���
		String[] pk_dynkeywords = ContrastMeasPubDataCache.getInstance().getPk_dynKeyValues(subKeyGroupVO,schemeVO,qryvo.getPkLock()).get(qryvo.getPkLock());
		List<String> pk_dynkeywordList = Arrays.asList(pk_dynkeywords);
		
		//@edited by zhoushuang  2015.12.29 ��λ����̵�pk
		String pk_dynCrop = KeyVO.DIC_CORP_PK;
		//@edited by zhoushuang  2015.12.29 ��һ���ؼ��ֵ�pk
//		String pk_otherDynKey = otherDynKeyToVal[0];
		
		
		try {
			return HBPubItfService.getRemoteDxModelFunction().getINTRBYKEY(projectcode, isself, offset, otherDynKeyToValPK, env);
		} catch (BusinessException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			throw e;
		}
	}

	
	private MeasureReportVO getMeasure(ICalcEnv env,String projectcode) throws BusinessException{
		String pk_hbScheme =(String) env.getExEnv(IContrastConst.PK_HBSCHEME);
		ContrastQryVO qryvo = (ContrastQryVO) env.getExEnv(IContrastConst.CONTRASTQRYVO);
//		qryvo.getKeymap().put(otherDynKeyToVal[0], otherDynKeyToVal[1]);
		HBSchemeVO schemeVO = (HBSchemeVO) ((com.ufsoft.script.spreadsheet.UfoCalcEnv) env).getExEnv(IContrastConst.HBSCHEMEVO);
 
		
		//Ԥ���ص�ӳ���ϵ
		Map<String, MeasureReportVO> prjoectMeasMapCache = new HashMap<String, MeasureReportVO>();
//		Map<String, ProjectVO> prjoectVOCache = new HashMap<String, ProjectVO>();
		IntrMeasProjectCache cacheinstance = qryvo.getIntrMeaProjectinstance();
		if(cacheinstance != null){
			prjoectMeasMapCache= cacheinstance.getMeasRepVOs();
//			prjoectVOCache = cacheinstance.getProjectVOs();
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
		return measrepvo;
		
	}
	
	@Override
	public Object callFunc(String strFuncName, String strParam)throws BusinessException {
		return null;
	}
 
}