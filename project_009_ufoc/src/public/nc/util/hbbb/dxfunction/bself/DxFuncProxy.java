package nc.util.hbbb.dxfunction.bself;

import nc.itf.hbbb.dxrelation.IDxFunctionConst;
import nc.vo.hbbb.dxrelation.DXRelationBodyVO;
import nc.vo.pub.BusinessException;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.script.UfoFormulaProxy;
import com.ufsoft.script.base.UfoEElement;
import com.ufsoft.script.exception.CreateProxyException;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.script.expression.UfoExpr;
import com.ufsoft.script.function.UfoFunc;
import com.ufsoft.script.spreadsheet.UfoCalcEnv;

public class DxFuncProxy implements IDxFunctionConst {

	private static DXFUNC isDxFunc(String funName,UfoFunc func) {
		DXFUNC result = null;
		if (funName.toUpperCase().startsWith(SREP)) {
			 result =  new SREP();
		} else if (funName.toUpperCase().startsWith(INTR)) {
			 result =  new INTR();
		} else if(funName.toUpperCase().startsWith(INTRBYKEY)){
			 result =  new INTRBYKEY();
		} else if (funName.toUpperCase().startsWith(CESUM)) {
			 result = new CESUM();
		} else if (funName.toUpperCase().startsWith(DPSUM)) {
			 result =  new DPSUM();
		} else if (funName.toUpperCase().startsWith(PTPSUM)) {
			 result =  new PTPSUM();
		} else if (funName.toUpperCase().startsWith(TPSUM)) {
			 result =  new TPSUM();
		} else if (funName.toUpperCase().startsWith(OPCE)) {
			 result =  new OPCE();
		} else if (funName.toUpperCase().startsWith(UCHECK)) {
			 result =  new UCHECK();
		} else if (funName.toUpperCase().startsWith(ESELECT)) {
			 result =  new ESELECT();
		} else if (funName.toUpperCase().startsWith(IPROPORTION)) {
			 result =  new IPROPORTION();
		} else if(funName.toUpperCase().startsWith("IF")){
			 result =  new DXFUNC() {
				@Override
				public boolean getBself() {
					return false;
				}
			};
		}else if(funName.toUpperCase().startsWith("K")){
			 result =  new DXFUNC() {
				@Override
				public boolean getBself() {
					return false;
				}
			};
		}
		result.setFunc(func);
		return result;
	}

//	private static DXFUNC getFirstDxFucExp(String pk_self, String pk_other,
//			HBSchemeVO schemeVO,String pk_hbrepstru, Map<String, String> keymap, String formula)
//			throws BusinessException {
//		if (null == formula || formula.trim().length() == 0) {
//			return null;
//		}
//		UfoCalcEnv env = new UfoCalcEnv(null, null, false, null);
//		try {
//			KeywordCache keyCache = UFOCacheManager.getSingleton()
//					.getKeywordCache();
//			Vector<KeyVO> keyVector = keyCache.getAllKeys();
//			env.setKeys(keyVector.toArray(new KeyVO[0]));
//			env.loadFuncListInst().registerExtFuncs(new DXFormulaDriver(env));
//
//			UfoFormulaProxy parser = new UfoFormulaProxy(env);
//
//			// 设置其他环境信息
//			ContrastQryVO qryvo = new ContrastQryVO();
//			qryvo.setKeymap(keymap);
////			qryvo.setPk_hbrepstru(getHBRepStruPK(keymap, HBSchemeSrvUtils.getHBSchemeByHBSchemeId(pk_hbscheme)));
//			qryvo.setPk_hbrepstru(pk_hbrepstru);
//			env.setExEnv(IContrastConst.CONTRASTQRYVO, qryvo);
//			env.setExEnv(IContrastConst.PK_SELFCORP, pk_self);
//			env.setExEnv(IContrastConst.PK_OPPCORP, pk_other);
//			env.setExEnv(IContrastConst.PK_HBSCHEME, schemeVO.getPk_hbscheme());
//			env.setExEnv(IContrastConst.HBSCHEMEVO, schemeVO);
//			UfoExpr expr = parser.parseExpr(formula);
//			UfoEElement[] elements = expr.getElements();
//			if (null == elements || elements.length == 0) {
//				return null;
//			}
//			for (int i = 0; i < elements.length; i++) {
//				UfoEElement ele = elements[i];
//				ExtFunc tmpformula = (ExtFunc) elements[i].getObj();
//				DXFUNC dxfun = isDxFunc(tmpformula.getFuncName(),tmpformula);
//
//				if (null == dxfun) {
//				} else {
//					return dxfun;
//				}
//			}
//
//		} catch (CreateProxyException e) {
//			throw new BusinessException(e);
//		} catch (ParseException e) {
//			throw new BusinessException(e);
//		}
//
//		return null;
//	}
	
	
	private static DXFUNC getFirstDxFucExp(UfoCalcEnv env,DXRelationBodyVO subVO)
			throws BusinessException {
		UfoExpr expr = subVO.getExpr();
		try {
			if(expr == null){
				String formula = subVO.getFormula();
				if (null == formula || formula.trim().length() == 0) {
					return null;
				}
				UfoFormulaProxy parser = new UfoFormulaProxy(env);
				expr = parser.parseExpr(formula);
			}
			
			UfoEElement[] elements = expr.getElements();
			if (null == elements || elements.length == 0) {
				return null;
			}
			for (int i = 0; i < elements.length; i++) {
				if(elements[i].getObj() instanceof UfoFunc){
					UfoFunc tmpformula = (UfoFunc) elements[i].getObj();
					DXFUNC dxfun = isDxFunc(tmpformula.getFuncName(),tmpformula);
					if (null == dxfun) {
					} else {
						return dxfun;
					}
				}
			}
		} catch (CreateProxyException e) {
			throw new BusinessException(e);
		} catch (ParseException e) {
			throw new BusinessException(e);
		}
		return null;
	}
	
//	private static String getHBRepStruPK(Map<String, String> map, HBSchemeVO VO){
//		ReportCombineStruVersionVO memberVO = null;
//		String pk_keygroup = VO.getPk_keygroup();
//		KeyGroupVO groupVO = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
//		KeyVO[] keys = groupVO.getKeys();
//		String dateValue = null;
//		String pk_keyword = null;
//		boolean isAccScheme = true;
//		for(int i = 0 ; i < keys.length ; i++){
//			if(keys[i].isTTimeKeyVO()){
//				pk_keyword = keys[i].getPk_keyword();
//				dateValue = map.get(pk_keyword);
//				if(keys[i].isTimeKeyVO()){
//					isAccScheme = false;
//				}
//				break;
//			}
//		}
//        if(isAccScheme)
//        	memberVO = HBRepStruUtil.getHBStruVersionVO(VO.getPk_accperiodscheme(), pk_keyword, dateValue,VO.getPk_repmanastru());
//        else
//        	memberVO = HBRepStruUtil.getHBStruVersionVO(dateValue,VO.getPk_repmanastru());
//        return memberVO.getPk_vid();
//	}

	public static boolean bSelf(UfoCalcEnv env,DXRelationBodyVO subVO)throws BusinessException {
		DXFUNC dxfun = getFirstDxFucExp(env,subVO);
		if (null == dxfun) {
			AppDebug.debug("函数没有找到是否存在本对方参数值");/*-=notranslate=-*/
			//函数没有找到是否存在本对方参数值
			return false;
		} else {
			return dxfun.getBself();
		}
	}
}