package nc.bs.hbbb.dxrelation.dxfuncall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.hbbb.dxmodelfunction.HBProjectBOUtil;
import nc.itf.hbbb.constants.HBFmlConst;
import nc.itf.hbbb.contrast.ContrastMeasPubDataCache;
import nc.itf.hbbb.contrast.IntrMeasProjectCache;
import nc.itf.hbbb.dxrelation.IDxFunctionConst;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
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
		
		
		//直接在这里ByKey的运算，吧计算结果放到env环境变量。
		
		
		
		if(null==objParams || objParams.length==0  ){
			return null; 
		}
		String projectcode=HBProjectParamGetUtil.getProjectByParam(objParams[0]);
		
		int isself=0;
		if(objParams.length>1 && null!=objParams[1] && objParams[1] instanceof Integer){
			isself=(Integer)objParams[1];
		}
		
		
		
		String otherDynKeyToValPK = null;
		if(null!=objParams[2]){
			String keyword = String.valueOf(objParams[2]);
		 
			KeyVO keyvo = UFOCacheManager.getSingleton().getKeywordCache().getByName(keyword);
			otherDynKeyToValPK = keyvo.getPk_keyword();
		}
		
		Map<String,UFDouble>  values = getByKeyValue(projectcode,isself,otherDynKeyToValPK,env);
		
		Map<String,UFDouble>  rtn =  new HashMap<>();
		String opt = String.valueOf(objParams[5]);
		if(opt==null||opt.trim().length()==0){
			env.setExEnv(IDxFunctionConst.INTRBYKEY_RESULT_KEY, values);
			return 0D;
			
		}
		if(null!=objParams[3]&&String.valueOf(objParams[3]).trim().length()>2){
			
			
			int otherSelf=0;
			String otherProject=HBProjectParamGetUtil.getProjectByParam(objParams[3]);
			if(null!=objParams[4] && objParams[4] instanceof Integer){
				otherSelf=(Integer)objParams[4];
			}
			Map<String,UFDouble>   otherValues = getByKeyValue(otherProject,otherSelf,otherDynKeyToValPK,env);
			Set<String> keySet = new HashSet<>();
			keySet.addAll(values.keySet());
			keySet.addAll(otherValues.keySet());
			for(String key:keySet){
				UFDouble result =  this.getValue(values.get(key), otherValues.get(key), opt);
				if(result!=null&&result.doubleValue()!=0){
					rtn.put(key, result);
				}
			}
			
			
		}
		
		else if(objParams.length>7&&null!=objParams[6]&&String.valueOf(objParams[6]).trim().length()>2){
			
			
			UFDouble cons = new UFDouble(String.valueOf(objParams[6]));
		 
			Set<String> keySet = new HashSet<>();
			keySet.addAll(values.keySet());
		 
			for(String key:keySet){
				UFDouble result =  this.getValue(values.get(key), cons, opt);
				if(result!=null&&result.doubleValue()!=0){
					rtn.put(key, result);
				}
			}
			
		}else if(null!=objParams[6]){
			UFDouble cons = new UFDouble(String.valueOf(objParams[6]));
			 
			Set<String> keySet = new HashSet<>();
			keySet.addAll(values.keySet());
		 
			for(String key:keySet){
				UFDouble result =  this.getValue(values.get(key), cons, opt);
				if(result!=null&&result.doubleValue()!=0){
					rtn.put(key, result);
				}
			}
		}else{
		 
			 
					rtn.putAll(values);
			 
		}
		
		env.setExEnv(IDxFunctionConst.INTRBYKEY_RESULT_KEY, rtn);
		 
		 
		
		
		
		
		return 0D;
		
		
		
		
		
		
		
//		if(1==1)
//			return 0d;
//		
//		int offset=0;
//		Map<String,UFDouble> result = new HashMap<String, UFDouble>();
//		env.setExEnv(IDxFunctionConst.INTRBYKEY_RESULT_KEY, result);
//		
//
//		
//		
//		
//		MeasureReportVO measure = getMeasure(env,projectcode);
//		if(measure==null){
//			return new UFDouble(0);
//		}
//		
//		
//		ProjectVO provo = HBProjectBOUtil.getProjectVOByCode(projectcode);
//		
//		
//		String pk_org = "";
//		String pk_other_org = "";
//		if (isself == HBFmlConst.SELF_TO_OPP) {
//			pk_org = (String) env.getExEnv(IContrastConst.PK_SELFCORP);
//			pk_other_org = (String) env.getExEnv(IContrastConst.PK_OPPCORP);
//		} else {
//			pk_org = (String) env.getExEnv(IContrastConst.PK_OPPCORP);
//			pk_other_org = (String) env.getExEnv(IContrastConst.PK_SELFCORP);
//		}
//		ContrastQryVO qryvo = (ContrastQryVO) env.getExEnv(IContrastConst.CONTRASTQRYVO);
//		Map<String, String> oppEntityOrgs = qryvo.getOppEntityOrgs();
//		if(oppEntityOrgs != null && oppEntityOrgs.keySet().contains(pk_other_org)){
//			pk_other_org = oppEntityOrgs.get(pk_other_org);
//			if(pk_other_org==null){
//				return 0D;
//			}
//		}
//			
//		
//
//		KeyGroupVO subKeyGroupVO = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(measure.getMeasVO().getKeyCombPK());
//		
//		
// 
//		KeyVO[] subkeys = subKeyGroupVO.getKeys();
//		
//		//期间 币种，主体，对方单位
//		StringBuffer buf = new StringBuffer();
//		String pkLock = qryvo.getPkLock();
//		HBSchemeVO schemeVO = (HBSchemeVO) ((com.ufsoft.script.spreadsheet.UfoCalcEnv) env).getExEnv(IContrastConst.HBSCHEMEVO);
//		
//		Map<String, String> handOffset =qryvo.getOffset();
//		for(int i = 0 ; i < subkeys.length ; i++){
//			String pk_keyword = subkeys[i].getPk_keyword();
//			if( subkeys[i].isTTimeKeyVO()){
//				
//			}
//			
//			
//			
//			//主体
//			if(pk_keyword.equals(KeyVO.CORP_PK)){
//				buf.append("keyword");
//				buf.append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.CORP_PK)+1);
//			 
//
//				buf.append(" = '"+pk_org+"'");
//				buf.append(" and ");
//		
//			}
//			else if(pk_keyword.equals(KeyVO.DIC_CORP_PK)){//对方单位
//				
//				buf.append("keyword");
//				buf.append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.DIC_CORP_PK)+1);
//			 
//
//				buf.append(" = '"+pk_other_org+"'");
//				buf.append(" and ");
//		
//				
//			}else if(subkeys[i].isTTimeKeyVO()){//期间
//				
//				handOffset = 	OffsetHanlder.handOffset(schemeVO, handOffset, offset);
//				buf.append("keyword");
//				buf.append(subKeyGroupVO.getIndexByKeywordPK(subkeys[i].getPk_keyword())+1);
//				buf.append(" =  '"+handOffset.get(subkeys[i].getPk_keyword())+"' and ");
//			}else{//币种等
//				if(qryvo.getKeymap().get(pk_keyword)==null||otherDynKeyToValPK[0].equals(pk_keyword)){
//					continue;
//				}
//				buf.append("keyword");
//				buf.append(subKeyGroupVO.getIndexByKeywordPK(pk_keyword)+1);
//				
//				buf.append(" = '" + qryvo.getKeymap().get(pk_keyword) + "' and ");
//			}
//		}
//		buf.append(" ver = 0");
//		
//		MeasurePubDataVO[] findByKeywordArray = MeasurePubDataBO_Client.findBySqlCondition(subKeyGroupVO.getKeyGroupPK(), buf.toString());
//		if(findByKeywordArray==null||findByKeywordArray.length==0){
//			return 0D;
//		}
//		ArrayList<String>  aloneids = new ArrayList<String>();
//		for(MeasurePubDataVO pubData:findByKeywordArray){
//			aloneids.add(pubData.getAloneID());
//		}
//		MeasureDataVO[] datavos = HBPubItfService.getRemoteMeasureDataSrv().getRepData(aloneids.toArray(new String[0]), new MeasureVO[] {
//			measure.getMeasVO()
//		});
//		
//		//分组求和
//		for(MeasurePubDataVO pubData:findByKeywordArray){
//			UFDouble value = new UFDouble();
//			for(MeasureDataVO data:datavos){
//				if(pubData.getAloneID().equals(data.getAloneID())){
//					value = value.add(data.getUFDoubleValue());
//				}
//			}
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
//		if(1==1){
//			return 0;
//		}
//		//动态区关键字pk 目前支持2个动态区关键字
//		String[] pk_dynkeywords = ContrastMeasPubDataCache.getInstance().getPk_dynKeyValues(subKeyGroupVO,schemeVO,qryvo.getPkLock()).get(qryvo.getPkLock());
//		List<String> pk_dynkeywordList = Arrays.asList(pk_dynkeywords);
//		
//		//@edited by zhoushuang  2015.12.29 单位或客商的pk
//		String pk_dynCrop = KeyVO.DIC_CORP_PK;
//		//@edited by zhoushuang  2015.12.29 另一个关键字的pk
////		String pk_otherDynKey = otherDynKeyToVal[0];
//		
//		
//		try {
//			return HBPubItfService.getRemoteDxModelFunction().getINTRBYKEY(projectcode, isself, offset, otherDynKeyToValPK, env);
//		} catch (BusinessException e) {
//			nc.bs.logging.Logger.error(e.getMessage(), e);
//			throw e;
//		}
	}
	
	private UFDouble  getValue(UFDouble v1,UFDouble v2,String op){
		
		if(v1==null){
			v1 = new UFDouble(0);
		}
		if(v2==null){
			v2 = new UFDouble();
		}
		
		if("+".equals(op)){
			return v1.add(v2);
		}else if("-".equals(op)){
			return v1.sub(v2);
		}else if("*".equals(op)){
			return v1.multiply(v2);
		}else if("/".equals(op)){
			if(v2.doubleValue()==0){
				return  new UFDouble();
			}else{
				return v1.div(v2);
			}
		}else{
			return new UFDouble(); 
		}
		
		
		
	}
	
	
	private Map<String,UFDouble> getByKeyValue(String projectCode,int scopeType,String pk_group,ICalcEnv env) throws BusinessException{
		
		//直接在这里ByKey的运算，吧计算结果放到env环境变量。
//		String projectcode=HBProjectParamGetUtil.getProjectByParam(projectCode);
		
		int isself=scopeType;
		
		Map<String,UFDouble> result = new HashMap<String, UFDouble>();
		
//		String[] otherDynKeyToValPK = new String[2];
		 
		 
//		KeyVO keyvo = UFOCacheManager.getSingleton().getKeywordCache().getByName(groupName);
//		String otherDynKeyToValPK = keyvo.getPk_keyword();

		
		MeasureReportVO measure = getMeasure(env,projectCode);
		if(measure==null){
			return result;
		}
		
		
//		ProjectVO provo = HBProjectBOUtil.getProjectVOByCode(projectCode);
		
		
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
		if(oppEntityOrgs != null && oppEntityOrgs.keySet().contains(pk_other_org)){
			pk_other_org = oppEntityOrgs.get(pk_other_org);
			if(pk_other_org==null){
				return result;
			}
		}
			
		

		KeyGroupVO subKeyGroupVO = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(measure.getMeasVO().getKeyCombPK());
		
		
 
		KeyVO[] subkeys = subKeyGroupVO.getKeys();
		
		//期间 币种，主体，对方单位
		StringBuffer buf = new StringBuffer();
		String pkLock = qryvo.getPkLock();
		HBSchemeVO schemeVO = (HBSchemeVO) ((com.ufsoft.script.spreadsheet.UfoCalcEnv) env).getExEnv(IContrastConst.HBSCHEMEVO);
		
		Map<String, String> handOffset =qryvo.getOffset();
		for(int i = 0 ; i < subkeys.length ; i++){
			String pk_keyword = subkeys[i].getPk_keyword();
			if( subkeys[i].isTTimeKeyVO()){
				
			}
			
			
			
			//主体
			if(pk_keyword.equals(KeyVO.CORP_PK)){
//				StringBuffer buf1 = new StringBuffer();
				buf.append("keyword");
				buf.append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.CORP_PK)+1);
			 

				buf.append(" = '"+pk_org+"'");
				buf.append(" and ");
		
			}
			else if(pk_keyword.equals(KeyVO.DIC_CORP_PK)){//对方单位
				
//				StringBuffer buf1 = new StringBuffer();
				buf.append("keyword");
				buf.append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.DIC_CORP_PK)+1);
			 

				buf.append(" = '"+pk_other_org+"'");
				buf.append(" and ");
		
				
			}else if(subkeys[i].isTTimeKeyVO()){//期间
				
				handOffset = 	OffsetHanlder.handOffset(schemeVO, handOffset, 0);
				buf.append("keyword");
				buf.append(subKeyGroupVO.getIndexByKeywordPK(subkeys[i].getPk_keyword())+1);
				buf.append(" =  '"+handOffset.get(subkeys[i].getPk_keyword())+"' and ");
			}else{//币种等
				if(qryvo.getKeymap().get(pk_keyword)==null||pk_group.equals(pk_keyword)){
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
			return result;
		}
		ArrayList<String>  aloneids = new ArrayList<String>();
		for(MeasurePubDataVO pubData:findByKeywordArray){
			aloneids.add(pubData.getAloneID());
		}
		MeasureDataVO[] datavos = HBPubItfService.getRemoteMeasureDataSrv().getRepData(aloneids.toArray(new String[0]), new MeasureVO[] {
			measure.getMeasVO()
		});
		
		//分组求和
		for(MeasurePubDataVO pubData:findByKeywordArray){
			UFDouble value = new UFDouble();
			for(MeasureDataVO data:datavos){
				if(pubData.getAloneID().equals(data.getAloneID())){
					value = value.add(data.getUFDoubleValue());
				}
			}
			String key ="";
			int  groupIdx = pubData.getKeyGroup().getIndexByKeywordPK(pk_group);
			if( groupIdx>=0&&pubData.getKeywords().length>groupIdx){
				  key = pubData.getKeywords()[groupIdx];
			} 
			result.put(key, value);
			
			 
			
		}
		
		return result;
		
	}

	
	private MeasureReportVO getMeasure(ICalcEnv env,String projectcode) throws BusinessException{
		String pk_hbScheme =(String) env.getExEnv(IContrastConst.PK_HBSCHEME);
		ContrastQryVO qryvo = (ContrastQryVO) env.getExEnv(IContrastConst.CONTRASTQRYVO);
//		qryvo.getKeymap().put(otherDynKeyToVal[0], otherDynKeyToVal[1]);
		HBSchemeVO schemeVO = (HBSchemeVO) ((com.ufsoft.script.spreadsheet.UfoCalcEnv) env).getExEnv(IContrastConst.HBSCHEMEVO);
 
		
		//预加载的映射关系
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
				//解决没有映射关系时对账也能够进行下去
				AppDebug.debug("计算INTR函数时,编号为"+projectcode+"合并报表项目为空,请检查映射!");/*-=notranslate=-*/
//				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0455")/* @res "计算INTR函数时,编号为"*/+projectcode+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0529")/*@res"合并报表项目为空,请检查映射!"*/);
			}
		}
		return measrepvo;
		
	}
	
	@Override
	public Object callFunc(String strFuncName, String strParam)throws BusinessException {
		return null;
	}
 
}
