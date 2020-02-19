package nc.bs.hbbb.dxrelation.dxfuncall;

import static nc.itf.hbbb.dxrelation.IDxFunctionConst.CESUM;
import static nc.itf.hbbb.dxrelation.IDxFunctionConst.DPSUM;
import static nc.itf.hbbb.dxrelation.IDxFunctionConst.ESELECT;
import static nc.itf.hbbb.dxrelation.IDxFunctionConst.INTR;
import static nc.itf.hbbb.dxrelation.IDxFunctionConst.IPROPORTION;
import static nc.itf.hbbb.dxrelation.IDxFunctionConst.OPCE;
import static nc.itf.hbbb.dxrelation.IDxFunctionConst.PTPSUM;
import static nc.itf.hbbb.dxrelation.IDxFunctionConst.SREP;
import static nc.itf.hbbb.dxrelation.IDxFunctionConst.TPSUM;
import static nc.itf.hbbb.dxrelation.IDxFunctionConst.UCHECK;
import nc.itf.hbbb.dxrelation.IDxFunctionConst;
import nc.itf.hbbb.dxrelation.dxfuncall.IDXFormulaCallFunc;
import nc.vo.pub.BusinessException;

import com.ufsoft.script.base.ICalcEnv;


/**
 * 抵销函数函数调用
 * @date 20110316
 * @author liyra   
 *
 */
public class DXFormulaCallFuncImpl implements IDXFormulaCallFunc{
	
	public DXFormulaCallFuncImpl(){
		super();
	}
	
	public Object callFuncWithEnv(String strFuncName, java.lang.Object[] objParams, ICalcEnv env) throws BusinessException {
		if(null==strFuncName || strFuncName.trim().length()==0){
			return null;
		}
		if(strFuncName.equalsIgnoreCase(SREP)){
			return new SREPCallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase(INTR)){
			return new INTRCallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase(IDxFunctionConst.INTRBYKEY)){
			return new INTRBYKEYCallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase(DPSUM)){
			return new DPSUMCallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase(CESUM)){
			return new CESUMCallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase(IPROPORTION)){   
			return new IPROPORTIONCallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase(OPCE)){
			return new OPCECallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase(PTPSUM)){
			return new PTPSUMCallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase(ESELECT)){
			return new ESELECTCallFunc().callFunc(strFuncName, objParams, env);
		}
		/*else if(strFuncName.equalsIgnoreCase(INVSUM)){
			return new INVSUMCallFunc().callFunc(strFuncName, objParams, env);
		}*/else if(strFuncName.equalsIgnoreCase(TPSUM)){
			return new TPSUMCallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase(UCHECK)){
			return new UCHECKCallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase("CHECKBYKEY")){
			return new UCHECKCallFunc().callFunc(strFuncName, objParams, env);
		}
		else if(strFuncName.equalsIgnoreCase(IDxFunctionConst.UCHECKBYKEY)){
			return new UCHECKBYKEYCallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase(IDxFunctionConst.ZMONTH)){
			return new ZMONTHCallFunc().callFunc(strFuncName, objParams, env);
		}else if(strFuncName.equalsIgnoreCase("OESUM")){
			return new OESUMCallFunc().callFunc(strFuncName, objParams, env);
		}

		
		
		return null;
	}
	
	public Object callFunc(String strFuncName, String strParam) throws BusinessException {
		if(null==strFuncName || strFuncName.trim().length()==0){
			return null;
		}
		if(strFuncName.equalsIgnoreCase(SREP)){
			return new SREPCallFunc().callFunc(strFuncName, strParam);
		}else if(strFuncName.equalsIgnoreCase(INTR)){
			
		}else if(strFuncName.equalsIgnoreCase(DPSUM)){
			
		}else if(strFuncName.equalsIgnoreCase(CESUM)){
			
		}else if(strFuncName.equalsIgnoreCase(IPROPORTION)){   
			
		}else if(strFuncName.equalsIgnoreCase(OPCE)){
			
		}else if(strFuncName.equalsIgnoreCase(PTPSUM)){
			
		}/*else if(strFuncName.equalsIgnoreCase(SINTR)){
			
		}*/
       /* else if(strFuncName.equalsIgnoreCase(INVSUM)){
			
		}*/else if(strFuncName.equalsIgnoreCase(TPSUM)){
			
		}else if(strFuncName.equalsIgnoreCase(UCHECK)){
			
		}
		return null;
	}
	

}
