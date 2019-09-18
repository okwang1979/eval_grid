/**
 * 
 */
package nc.util.hbbb.dxrelation.formula;

import nc.bs.framework.common.NCLocator;
import nc.itf.hbbb.dxrelation.IDxFunctionConst;
import nc.itf.hbbb.dxrelation.dxfuncall.IDXFormulaCallFunc;
import nc.vo.pub.BusinessException;

import com.ufsoft.iufo.util.parser.UfoParseException;
import com.ufsoft.script.base.AbstractParser;
import com.ufsoft.script.base.ICalcEnv;
import com.ufsoft.script.base.Token;
import com.ufsoft.script.base.UfoTokenMgr;
import com.ufsoft.script.exception.CmdException;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.script.extfunc.DefaultFuncDirver;
import com.ufsoft.script.function.ExtFunc;
import com.ufsoft.script.function.IUfo2BatchCalcFunc;
import com.ufsoft.script.function.UfoFuncInfo;
import com.ufsoft.script.function.UfoFuncList;
import com.ufsoft.script.spreadsheet.UfoCalcEnv;

/**
 * 抵销模板函数公式驱动
 * 
 * @author jiaah
 * @created at 2010-10-14,下午02:14:45
 * 
 */
public class DXFormulaDriver extends DefaultFuncDirver implements IUfo2BatchCalcFunc, IDxFunctionConst {

	private static final long serialVersionUID = 1L;

	private static IDXFormulaCallFunc formularSrv = null;
	
	public DXFormulaDriver(UfoCalcEnv env) {
		super(env);
	}

	/**
	 * 返回函数类型列表
	 */
	@Override
	public String[] getCategoryList() {
		String[] strCatList = new String[CATNAMES.length];
		for (int i = 0; i < strCatList.length; i++) {
			strCatList[i] = CATNAMES[i];
		}
		return strCatList;
	}

	/**
	 * 返回驱动支持的函数列表
	 */
	@Override
	public UfoFuncInfo[] getFuncList() {
		// 返回排序后的函数列表
		return FUNCLIST;
//		return sortArray(FUNCLIST);
	}

//	/**
//	 * 按函数名称排序
//	 * 
//	 * @param array
//	 * @return
//	 */
//	private UfoFuncInfo[] sortArray(UfoFuncInfo[] array) {
//		Arrays.sort(array, new Comparator<UfoFuncInfo>() {
//			@Override
//			public int compare(UfoFuncInfo o1, UfoFuncInfo o2) {
//				return UFOString.compareHZString(o1.getFuncName(), o2.getFuncName());
//			}
//		});
//		return array;
//	}

	/**
	 * isCheckEnable == true时 需要对如下函数进行语法检查
	 */
	@Override
	public boolean isCheckEnable(String strFuncName) {
		if (null != strFuncName
				&& (strFuncName.equalsIgnoreCase(CESUM) || strFuncName.equalsIgnoreCase(DPSUM) || strFuncName.equalsIgnoreCase(INTR) || strFuncName.equalsIgnoreCase(ESELECT)
						|| strFuncName.equalsIgnoreCase(IPROPORTION)// 直接投资比例函数没有参数
						|| strFuncName.equalsIgnoreCase(OPCE)

						|| strFuncName.equalsIgnoreCase(PTPSUM)
				// || strFuncName.equalsIgnoreCase(SINTR)
				|| strFuncName.equalsIgnoreCase(SREP)) 
				|| strFuncName.equalsIgnoreCase(TPSUM) 
				|| strFuncName.equalsIgnoreCase(UCHECK)
				|| strFuncName.equalsIgnoreCase(UCHECKBYKEY)
				|| strFuncName.equalsIgnoreCase(UCHECKBYORG)
				|| strFuncName.equalsIgnoreCase(INTRBYKEY)) {
			return true;
		} else {
		}
		return false;
	}

	private Object filterHbAccount(UfoTokenMgr objTokenMgr, Token objToken, boolean bUserDefined, UfoCalcEnv objEnv, byte ptype, int nBeginPos) throws ParseException {

		// 参数在整个公式串中的位置
		int iParamPos = objToken.getPos() + nBeginPos;

		switch (ptype) {
//			case UfoFuncList.MEASNAME:
				
			case UfoFuncList.HBACCOUNT:
				// iufo业务函数中的特殊参数分析。
				if (objToken.getKind() != UfoTokenMgr.TKN_STRING) {
					AbstractParser.genErr(UfoParseException.ERR_PARA_TYPE, iParamPos);
				}
				return parseIufoParam(objToken.getImage(), ptype, bUserDefined, iParamPos, objEnv);
			default:
				return super.parseParam(objTokenMgr, objToken, bUserDefined, objEnv, ptype, nBeginPos);
		}
	}

	
	// private final Object genMeasOperand(
	// String strName,
	// boolean bUserDef,
	// int nPos,
	// ICalcEnv objEnv)
	// throws UfoParseException {
	// if (!(objEnv instanceof UfoCalcEnv)) {
	// throw new UfoParseException("miufo1000530",nPos); //"计算环境错误"
	// }
	//
	// MeasureVO mVO = null;
	// if (bUserDef) {
	// String strRepCode = null;
	// String strMeasName = null;
	// //modify by ljhua 2005-3-17 解决当报表编码中有'-'时解析指标错误情况。
	// int iTempPos=strName.indexOf("->");
	// if(iTempPos==-1){
	// strMeasName=strName;
	// }else{
	// strRepCode=strName.substring(0,iTempPos);
	// strMeasName=strName.substring(iTempPos+2);
	// }
	//
	// if (strRepCode == null && strMeasName == null) {
	// throw new UfoParseException("miufopublic421", nPos); //"指标的格式错误！正确的格式是：'报表编码->指标名称'"
	// } else if (strMeasName == null) {
	// strMeasName = strRepCode;
	// strRepCode = null;
	// }
	// mVO = getMeasure(strRepCode, strMeasName, nPos, objEnv);
	//
	// } else {
	// mVO = getMeasure(strName, nPos, objEnv);
	//
	// }
	// MeasOperand meas = new MeasOperand(mVO);
	// return meas;
	//
	//
	// }

	@Override
	public Object parseIufoParam(String strParam, byte bParamType, boolean bUserDef, int nParamPos, ICalcEnv objEnv) throws UfoParseException {

		if (strParam == null || (strParam = strParam.trim()).length() == 0) {
			AbstractParser.genErr(UfoParseException.ERR_PARA, nParamPos);
		}

		Object objReturn = null;
		if (bParamType == UfoFuncList.HBACCOUNT) {
			objReturn = "" + strParam + "";
		} else if (bParamType == UfoFuncList.DATEPROP) {
			objReturn = "" + strParam + "";
		}
		/*
		 * else if (bParamType == UfoFuncList.KEYNAME) { objReturn= genKeyVO(strParam, bUserDef, nParamPos,objEnv); }
		 */else if (bParamType == UfoFuncList.DWPROP) {
			objReturn = genCorpProp(strParam, bUserDef, nParamPos, objEnv);
		} else if (bParamType == UfoFuncList.KEYWORDNAME) {
			objReturn = genKeywordVO(strParam, bUserDef, nParamPos, objEnv);
		}
		return objReturn;

	}

	/**
	 * 检查语法 strFuncName是函数的名字 strParam是函数的全部参数字符串 bUserDef表示当前分析的函数是否是用户定义的。这里涉及改名问题 nBeginPos是在整个公式中该函数起始位置，这个参数的目的是在需要抛出错误信息的时候，用这个值加上在strParam中出错的位置作为UfoParseException的位置参数
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public java.util.ArrayList checkFunc(java.lang.String strFuncName, java.lang.String strParam, ICalcEnv objEnv, boolean bUserDef, int nBeginPos) throws ParseException {
		// if(null==strParam || strParam.trim().length()==0){
		// return new ArrayList();
		// }
		// strParam=strParam.replace("'","");
		// String[] list=strParam.split(",");
		// ArrayList plist =new ArrayList();
		// for(String str:list){
		// plist.add(str);
		// }
		//
		// super.checkFunc(strFuncName, strParam, objEnv, bUserDef, nBeginPos);
		//
		// //若有特殊函数，在此完成（待完成）
		// return plist;
		//

		UfoFuncInfo fInfo = getFuncInfo(strFuncName);

		if (fInfo != null) {
			if (!(objEnv instanceof UfoCalcEnv)) {
				throw new UfoParseException(UfoParseException.ERR_MEASENV, nBeginPos);
			}
			UfoCalcEnv env = (UfoCalcEnv) objEnv;

			byte[] ptypes = fInfo.getParamTypeList();
			java.util.ArrayList plist;
			int m, i;

			if (ptypes != null) { // 准备参数列表
				m = ptypes.length;
				plist = new java.util.ArrayList(m);
			} else {
				m = 0;
				plist = null;
			}

			// 检查参数
			UfoTokenMgr objTokenMgr = new UfoTokenMgr(strParam);
			Token objToken = objTokenMgr.getToken();

			byte flag = 0; // 设置参数省略标记
			for (i = 1; i <= m; i++) {
				short nTokenID = objToken.getKind();
				byte paratype = ptypes[i - 1]; // 得到参数类型
				if ((paratype & 0x80) != 0) { // 参数可以缺省
					if (nTokenID == UfoTokenMgr.TKN_COMMA) { // 参数被省略
						plist.add(null);
						objToken = objTokenMgr.getToken();
						flag = 1; // 至少后面应有一个参数不能被省略
						continue;
					} else if (nTokenID == UfoTokenMgr.TKN_INPUTEND) { // 函数结束
						break;
					}
				}

				plist.add(filterHbAccount(objTokenMgr, objToken, bUserDef, env, (byte) (paratype & 0x7f), nBeginPos));
				objToken = objTokenMgr.getToken();
				if (flag == 1) {
					flag = 0; // 清除标记
				}
				if (i < m) {
					if (objToken.getKind() == UfoTokenMgr.TKN_INPUTEND) { // 函数结束
						i++;
						break;
					}
					// 匹配","
					AbstractParser.matchToken(objToken.getKind(), UfoTokenMgr.TKN_COMMA, objToken.getPos());
				}
				objToken = objTokenMgr.getToken();
			}

			if (flag != 0) {
				AbstractParser.genErr(UfoParseException.ERR_PARA, objToken.getPos());
			}
			while (i <= m) { // 对省略的参数
				if ((ptypes[i - 1] & 0x80) != 0) {
					plist.add(null);
					i++;
				} else {
					AbstractParser.genErr(UfoParseException.ERR_PARA_TYPE, objToken.getPos());
				}
			}
			AbstractParser.matchToken(objToken.getKind(), UfoTokenMgr.TKN_INPUTEND, objToken.getPos());
			return plist;
		}

		throw new UfoParseException(UfoParseException.ERR_FUNC, nBeginPos);
	}

	@Override
	public Object callFunc(String strFuncName, java.lang.Object[] objParams, ICalcEnv env) throws CmdException {
		try {
			return getIdxformulaCallFunc().callFuncWithEnv(strFuncName, objParams, env);
		} catch (BusinessException e) {
			throw new CmdException(e.getMessage());
		}
	}
	
	private static IDXFormulaCallFunc getIdxformulaCallFunc(){
		if(formularSrv == null)
			formularSrv = NCLocator.getInstance().lookup(IDXFormulaCallFunc.class);
		return formularSrv;
	}

	/**
	 * 调用取数函数。 创建日期：(2002-02-04 14:55:36)
	 * 
	 * @author：王少松
	 */
	@Override
	public Object callFunc(String strFuncName, String strParam) {
		Object result = null;
		try {
			result = getIdxformulaCallFunc().callFunc(strFuncName, strParam);
		} catch (BusinessException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
		}
		return result;
	}

	@Override
	public String calcFuncValues(/* ExtFunc[] objExtFuncs, ICalcEnv objEnv */ExtFunc[] objExtFuncs, ICalcEnv env) throws CmdException {
		// TODO Auto-generated method stub
		if (env == null || !(env instanceof UfoCalcEnv)) {
			// "指标函数是iUFO特有的，计算环境要求UfoCalcEnv！";
			return "miufocalc100019";
		}
//		UfoCalcEnv objEnv = (UfoCalcEnv) env;
//		String strErr = null;

		return "1000";

		// try {
		// for(ExtFunc objFunc: objExtFuncs){
		// String strFuncName = objFunc.getFuncName();
		// if (objFunc.getFuncDriverName().equals(this.getClass().getName())){
		//
		// }
		// }
		//
		//
		// /**
		// * 下面指标函数进行分组并保存在hashFunc中
		// * hashFunc的key有MCOUNT，MSELECT,MSUMDATA,MMAX,MMIN,MAVG,MSUM，value是一个Hashtable
		// * 这个value中的key是由所有条件+"\r\n"组成的字符串，value是一个ArrayList
		// * 这个ArrayList中的第一个元素是由所有条件组成的Object[]，之后的所有元素都是MeasureVO
		// * 当key=MSUMDATA时，ArrayList的第二个元素为hashtable(key=指标pk,value=vector(指标统计函数名集合）)
		// */
		// Hashtable hashGroups = new Hashtable();
		// /**
		// * hashCount,hashSelect key是由所有条件+"\r\n"组成的字符串;value是MeasFunc组成的ArrayList
		// */
		// Hashtable hashCount = new Hashtable();
		// Hashtable hashSelect = new Hashtable();
		// Hashtable hashHBSelect = new Hashtable();
		// /**
		// * hashSum key是mmax,mmin,msum,mavg字串;value=hashtable ，其中key=所有条件+"\r\n"组成的字符串, value是MeasFunc组成的ArrayList
		// */
		// Hashtable hashSum = new Hashtable();
		// //分组指标函数
		// for (int i = 0; i < objExtFuncs.length; i++) {
		// ExtFunc objFunc = objExtFuncs[i];
		// String strFuncName = objFunc.getFuncName();
		// if (objFunc.getFuncDriverName().equals(this.getClass().getName()) && objFunc instanceof MeasFunc) {
		// MeasFunc objMeasFunc = (MeasFunc) objFunc;
		// if (strFuncName.equalsIgnoreCase(MSELECT)
		// || strFuncName.equalsIgnoreCase(MSELECTS)
		// || strFuncName.equalsIgnoreCase(MSELECTA)) {
		// //这个方法中将所有mselect函数中与区域无关的指标按照条件和指标ArrayList存成Hashtable，并且用MSELECT和这个Hashtable保存在hashFunc中
		// groupMselect(hashGroups, hashSelect, objMeasFunc, objEnv);
		// }else if (strFuncName.equalsIgnoreCase(HBMSELECT) || strFuncName.equalsIgnoreCase(HBMSELECTS) || strFuncName.equalsIgnoreCase(HBMSELECTA)){
		// groupHBMselect(hashGroups, hashHBSelect, objMeasFunc, objEnv);
		// }
		// else if (strFuncName.equalsIgnoreCase(CODENAME)) {
		// continue;
		// } else if (strFuncName.equalsIgnoreCase(MCOUNT)) {
		// groupMCount(hashGroups, hashCount, objMeasFunc, objEnv);
		// } else {
		// groupMStatFunc(hashGroups, hashSum, objMeasFunc, objEnv);
		// }
		// }
		// }
		// if (hashGroups == null || hashGroups.size() == 0)
		// return strErr;
		//
		// // m_objEnv = objEnv;
		//
		// //设置指标追踪参数
		// MeasureTraceVO[] measureTraceVOs = objEnv.getMeasureTraceVOs();
		// if(measureTraceVOs != null){
		// hashGroups.put(ICalcEnv.MEASURE_TRACE_FLAG, measureTraceVOs);
		// }
		//
		// //获得指标函数值
		// /**
		// * hashMeasValue中key有MSUMDATA，MSELECT,MCOUNT,MMAX,MMIN,MAVG,MSUM,HBMSELECT;
		// * value(1)是一个Hashtable ,其中key是由所有条件+"\r\n"组成的字符串,
		// * value(2)是hashtable,
		// * 当hashMeasValue中key=MSUMDATA, value(2)中key类型为String(measurePK) ,value=hashtable (key=MSUM,MMAX,MMIN,MAVG,CURKEY,value=排除当前关键值外的合计、最大、最小、计数,当前关键字值字串)
		// * 当hashMeasValue中key=其它, value(2)中key 是measurePK,value是Double,String指标值
		// */
		// Hashtable hashMeasValue =
		// nc.ui.iufo.calculate.MeasFuncBO_Client.batCalc(hashGroups, objEnv);
		// if (hashMeasValue == null) {
		// setMeasValue(null) ;
		// // m_objEnv = null;
		// return null;
		// }
		//
		// if(hashMeasValue.get(ICalcEnv.MEASURE_TRACE_FLAG) != null){
		// objEnv.setMeasureTraceVOs((MeasureTraceVO[]) hashMeasValue.get(ICalcEnv.MEASURE_TRACE_FLAG));
		//
		// }
		//
		// //批量设置mcount值
		// Hashtable hashCountValue = (Hashtable) hashMeasValue.get(MCOUNT);
		// batchCalcMfunc(hashCount,hashCountValue,objEnv);
		//
		// //批量设置Mmax值
		// Hashtable hashMax = (Hashtable) hashSum.get(MMAX);
		// batchCalcMfunc(hashMax,(Hashtable) hashMeasValue.get(MMAX),objEnv);
		//
		// //批量设置Mmin值
		// Hashtable hashMin = (Hashtable) hashSum.get(MMIN);
		// batchCalcMfunc(hashMin,(Hashtable) hashMeasValue.get(MMIN),objEnv);
		//
		// //批量设置Mavg值
		// Hashtable hashMavg = (Hashtable) hashSum.get(MAVG);
		// batchCalcMfunc(hashMavg,(Hashtable) hashMeasValue.get(MAVG),objEnv);
		//
		// //批量设置MSUM值
		// Hashtable hashMsum = (Hashtable) hashSum.get(MSUM);
		// batchCalcMfunc(hashMsum,(Hashtable) hashMeasValue.get(MSUM),objEnv);
		//
		// //批量设置Mselect值
		// Hashtable hashSelectValue = (Hashtable) hashMeasValue.get(MSELECT);
		// batchCalcMfunc(hashSelect,hashSelectValue,objEnv);
		//
		// //对HBselect函数进行处理
		// batchCalcMfunc(hashHBSelect,(Hashtable) hashMeasValue.get(HBMSELECT), objEnv);
		//
		// setMeasValue(hashMeasValue);
		// if(hashMeasValue != null && hashMeasValue.containsKey("ERRORMSG")){
		// strErr = strErr == null? (String) hashMeasValue.get("ERRORMSG") : strErr + (String ) hashMeasValue.get("ERRORMSG");
		// }
		// } catch (Exception e) {
		// AppDebug.debug(e);
		// if (strErr == null)
		// strErr = e.getMessage();
		// else
		// strErr += e.getMessage();
		// }
		// return strErr;
	}

	@Override
	public void clearPreCalcData() {
		// TODO Auto-generated method stub

	}
}
