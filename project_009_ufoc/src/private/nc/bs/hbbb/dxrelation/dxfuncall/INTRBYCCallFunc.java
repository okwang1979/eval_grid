


package nc.bs.hbbb.dxrelation.dxfuncall;

import java.util.ArrayList;
import java.util.List;

import nc.pub.iufo.cache.UFOCacheManager;
import nc.util.hbbb.pub.HBPubItfService;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;

import com.ufsoft.script.base.ICalcEnv;

public class INTRBYCCallFunc  implements IDxCallFunc{

	@Override
	public Object callFunc(String strFuncName, Object[] objParams, ICalcEnv env)throws BusinessException {
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
		
		List<String>  filterKeys = new ArrayList<>();
		
//		String[] otherDynKeyToValPK = new String[2];
		if(objParams.length>3 && null!=objParams[3]){
			String keyword = String.valueOf(objParams[3]);
			String[] otherDynKeyToVal = keyword.split("=");
			KeyVO keyvo = UFOCacheManager.getSingleton().getKeywordCache().getByName(otherDynKeyToVal[0]);
			String pk_key = keyvo.getPk_keyword();
			filterKeys.add(pk_key);
			String value  =	HBPubItfService.getRemoteDxModelFunction().queryPKChooseKeyBYCode(keyvo,otherDynKeyToVal[1]);	
			filterKeys.add(value);
		}
		
		
		if(objParams.length>4 && null!=objParams[4]){
			String keyword = String.valueOf(objParams[4]);
			String[] otherDynKeyToVal = keyword.split("=");
			KeyVO keyvo = UFOCacheManager.getSingleton().getKeywordCache().getByName(otherDynKeyToVal[0]);
			String pk_key = keyvo.getPk_keyword();
			filterKeys.add(pk_key);
			String value  =	HBPubItfService.getRemoteDxModelFunction().queryPKChooseKeyBYCode(keyvo,otherDynKeyToVal[1]);	
			filterKeys.add(value);
		}
		
		try {
			return HBPubItfService.getRemoteDxModelFunction().getINTRBYKEY(projectcode, isself, offset, filterKeys.toArray(new String[0]), env);
		} catch (BusinessException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Object callFunc(String strFuncName, String strParam)throws BusinessException {
		return null;
	}
 
}
