package nc.util.hbbb.dxrelation.formula;

import java.util.HashMap;
import java.util.Map;

import nc.vo.hbbb.dxrelation.DXRelationBodyVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.script.UfoFormulaProxy;
import com.ufsoft.script.base.UfoObject;
import com.ufsoft.script.base.UfoVal;
import com.ufsoft.script.exception.CmdException;
import com.ufsoft.script.exception.CreateProxyException;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.script.exception.UfoValueException;
import com.ufsoft.script.expression.UfoExpr;
import com.ufsoft.script.spreadsheet.UfoCalcEnv;

public class DxFormulaFuncCalc {

	public DxFormulaFuncCalc(UfoCalcEnv new_calcEnv, UfoFormulaProxy new_parser, String new_accPeriod) {
		this.setCalcEnv(new_calcEnv);
		this.setAccPeriod(new_accPeriod);
		this.setParser(new_parser);
	}

	private UfoCalcEnv calcEnv; // 计算环境
	private UfoFormulaProxy parser; // 公式解析器
	private String accPeriod; // 会计期间

	public double calcFuncValue(DXRelationBodyVO subVO, boolean bUseCache) throws BusinessException {
		try {
			UfoExpr expr = subVO.getExpr();//对账传递的参数中已经解析过
			if(expr == null){
				String strFormula = subVO.getFormula();
				if (strFormula == null) {
					return 0;
				}
				// 按照规定的次序设置环境：0：任务；1：会计期间；2：父公司(本方单位)；3：子公司(对方单位)；4：当前对账单位；5：是否使用数据缓存；6：投资日期
				// String[] env = {strAccPeriod,strSelfUnitCode,strCounterUnitCode,strCurHBUnitCode, bUseCache+""/*, investData == null ? null : investData.getChangeDate()*/};
//				boolean bPrintTime = false;
//				long nParseBeginTime = System.currentTimeMillis();
				expr = getParser().parseExpr(strFormula);
//				long nParseEndTime = System.currentTimeMillis();
			}
			
//			if (bPrintTime) {
//				nc.bs.logging.Logger.debug("公式解析花费时间:" + (nParseEndTime - nParseBeginTime) + "ms");// @devTools System.out.println("公式解析花费时间:"+(nParseEndTime - nParseBeginTime) + "ms");
//			}
			// ((UfoCalcEnv)parser.getEnv()).getMeasureEnv().setInputDate(strAccPeriod);
			UfoVal[] vals = expr.calcExpr(getParser().getEnv());
//			if (bPrintTime) {
//				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0144")/* @res "计算公式花费时间:" */+ (System.currentTimeMillis() - nParseEndTime) + "ms");// @devTools
																																															// System.out.println("计算公式花费时间:"+
																																															// (System.currentTimeMillis()
																																															// -
																																															// nParseEndTime)
																																															// + "ms");
//			}

			if (vals != null && vals.length > 0) {
				try {
					return vals[0].doubleValue();
				} catch (UfoValueException uve) {
					AppDebug.debug(uve);// @devTools uvAppDebug.debug(e);
					return 0;
				}
			} else {
				return 0;
			}
		} catch (ParseException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage(), e);
		} catch (CmdException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String,UFDouble>  batchcalcFuncValue(String strFormula, boolean bUseCache) throws BusinessException {
		Map<String,UFDouble> resultMap = new HashMap<String,UFDouble> ();
		try {
			if (strFormula == null) {
				return resultMap;
			}
			if (parser == null) {
				parser = getParser();
			}

//			FuncListInst funcList = parser.getEnv().loadFuncListInst();
//			DXFormulaDriver driver = (DXFormulaDriver) funcList.getExtDriver(DXFormulaDriver.class.getName());
			boolean bPrintTime = false;
			long nParseBeginTime = System.currentTimeMillis();
			UfoExpr expr;

			expr = parser.parseExpr(strFormula);

			long nParseEndTime = System.currentTimeMillis();
			if (bPrintTime) {
				nc.bs.logging.Logger.debug("公式解析花费时间:" + (nParseEndTime - nParseBeginTime) + "ms");// @devTools System.out.println("公式解析花费时间:"+(nParseEndTime - nParseBeginTime) + "ms");
			}
			UfoVal[] vals = expr.calcExpr(parser.getEnv());
			if (bPrintTime) {
				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0144")/* @res "计算公式花费时间:" */+ (System.currentTimeMillis() - nParseEndTime) + "ms");// @devTools
																																															// + "ms");
			}

			if (vals != null && vals.length > 0) {
				UfoObject ufoVal = (UfoObject) vals[0];
				return (Map<String, UFDouble>) ufoVal.getValue();
			} else {
				return resultMap;
			}
		} catch (ParseException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		} catch (CmdException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}
	}

//	private UfoCalcEnv getCalcEnv() {
//		return calcEnv;
//	}

	private void setCalcEnv(UfoCalcEnv calcEnv) {
		this.calcEnv = calcEnv;
	}

	private UfoFormulaProxy getParser() throws BusinessException {
		if (parser == null) {
			try {
				parser = new UfoFormulaProxy(calcEnv);
			} catch (CreateProxyException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
				throw new BusinessException(e);
			}
		}
		return parser;
	}

	private void setParser(UfoFormulaProxy parser) {
		this.parser = parser;
	}

//	private String getAccPeriod() {
//		return accPeriod;
//	}

	private void setAccPeriod(String accPeriod) {
		this.accPeriod = accPeriod;
	}

}
