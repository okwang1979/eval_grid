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
 * ����ģ�庯����ʽ����
 * 
 * @author jiaah
 * @created at 2010-10-14,����02:14:45
 * 
 */
public class DXFormulaDriver extends DefaultFuncDirver implements IUfo2BatchCalcFunc, IDxFunctionConst {

	private static final long serialVersionUID = 1L;

	private static IDXFormulaCallFunc formularSrv = null;
	
	public DXFormulaDriver(UfoCalcEnv env) {
		super(env);
	}

	/**
	 * ���غ��������б�
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
	 * ��������֧�ֵĺ����б�
	 */
	@Override
	public UfoFuncInfo[] getFuncList() {
		// ���������ĺ����б�
		return FUNCLIST;
//		return sortArray(FUNCLIST);
	}

//	/**
//	 * ��������������
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
	 * isCheckEnable == trueʱ ��Ҫ�����º��������﷨���
	 */
	@Override
	public boolean isCheckEnable(String strFuncName) {
		if (null != strFuncName
				&& (strFuncName.equalsIgnoreCase(CESUM) || strFuncName.equalsIgnoreCase(DPSUM) || strFuncName.equalsIgnoreCase(INTR) || strFuncName.equalsIgnoreCase(ESELECT)
						|| strFuncName.equalsIgnoreCase(IPROPORTION)// ֱ��Ͷ�ʱ�������û�в���
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

		// ������������ʽ���е�λ��
		int iParamPos = objToken.getPos() + nBeginPos;

		switch (ptype) {
//			case UfoFuncList.MEASNAME:
				
			case UfoFuncList.HBACCOUNT:
				// iufoҵ�����е��������������
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
	// throw new UfoParseException("miufo1000530",nPos); //"���㻷������"
	// }
	//
	// MeasureVO mVO = null;
	// if (bUserDef) {
	// String strRepCode = null;
	// String strMeasName = null;
	// //modify by ljhua 2005-3-17 ����������������'-'ʱ����ָ����������
	// int iTempPos=strName.indexOf("->");
	// if(iTempPos==-1){
	// strMeasName=strName;
	// }else{
	// strRepCode=strName.substring(0,iTempPos);
	// strMeasName=strName.substring(iTempPos+2);
	// }
	//
	// if (strRepCode == null && strMeasName == null) {
	// throw new UfoParseException("miufopublic421", nPos); //"ָ��ĸ�ʽ������ȷ�ĸ�ʽ�ǣ�'�������->ָ������'"
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
	 * ����﷨ strFuncName�Ǻ��������� strParam�Ǻ�����ȫ�������ַ��� bUserDef��ʾ��ǰ�����ĺ����Ƿ����û�����ġ������漰�������� nBeginPos����������ʽ�иú�����ʼλ�ã����������Ŀ��������Ҫ�׳�������Ϣ��ʱ�������ֵ������strParam�г����λ����ΪUfoParseException��λ�ò���
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
		// //�������⺯�����ڴ���ɣ�����ɣ�
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

			if (ptypes != null) { // ׼�������б�
				m = ptypes.length;
				plist = new java.util.ArrayList(m);
			} else {
				m = 0;
				plist = null;
			}

			// ������
			UfoTokenMgr objTokenMgr = new UfoTokenMgr(strParam);
			Token objToken = objTokenMgr.getToken();

			byte flag = 0; // ���ò���ʡ�Ա��
			for (i = 1; i <= m; i++) {
				short nTokenID = objToken.getKind();
				byte paratype = ptypes[i - 1]; // �õ���������
				if ((paratype & 0x80) != 0) { // ��������ȱʡ
					if (nTokenID == UfoTokenMgr.TKN_COMMA) { // ������ʡ��
						plist.add(null);
						objToken = objTokenMgr.getToken();
						flag = 1; // ���ٺ���Ӧ��һ���������ܱ�ʡ��
						continue;
					} else if (nTokenID == UfoTokenMgr.TKN_INPUTEND) { // ��������
						break;
					}
				}

				plist.add(filterHbAccount(objTokenMgr, objToken, bUserDef, env, (byte) (paratype & 0x7f), nBeginPos));
				objToken = objTokenMgr.getToken();
				if (flag == 1) {
					flag = 0; // ������
				}
				if (i < m) {
					if (objToken.getKind() == UfoTokenMgr.TKN_INPUTEND) { // ��������
						i++;
						break;
					}
					// ƥ��","
					AbstractParser.matchToken(objToken.getKind(), UfoTokenMgr.TKN_COMMA, objToken.getPos());
				}
				objToken = objTokenMgr.getToken();
			}

			if (flag != 0) {
				AbstractParser.genErr(UfoParseException.ERR_PARA, objToken.getPos());
			}
			while (i <= m) { // ��ʡ�ԵĲ���
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
	 * ����ȡ�������� �������ڣ�(2002-02-04 14:55:36)
	 * 
	 * @author��������
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
			// "ָ�꺯����iUFO���еģ����㻷��Ҫ��UfoCalcEnv��";
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
		// * ����ָ�꺯�����з��鲢������hashFunc��
		// * hashFunc��key��MCOUNT��MSELECT,MSUMDATA,MMAX,MMIN,MAVG,MSUM��value��һ��Hashtable
		// * ���value�е�key������������+"\r\n"��ɵ��ַ�����value��һ��ArrayList
		// * ���ArrayList�еĵ�һ��Ԫ����������������ɵ�Object[]��֮�������Ԫ�ض���MeasureVO
		// * ��key=MSUMDATAʱ��ArrayList�ĵڶ���Ԫ��Ϊhashtable(key=ָ��pk,value=vector(ָ��ͳ�ƺ��������ϣ�)
		// */
		// Hashtable hashGroups = new Hashtable();
		// /**
		// * hashCount,hashSelect key������������+"\r\n"��ɵ��ַ���;value��MeasFunc��ɵ�ArrayList
		// */
		// Hashtable hashCount = new Hashtable();
		// Hashtable hashSelect = new Hashtable();
		// Hashtable hashHBSelect = new Hashtable();
		// /**
		// * hashSum key��mmax,mmin,msum,mavg�ִ�;value=hashtable ������key=��������+"\r\n"��ɵ��ַ���, value��MeasFunc��ɵ�ArrayList
		// */
		// Hashtable hashSum = new Hashtable();
		// //����ָ�꺯��
		// for (int i = 0; i < objExtFuncs.length; i++) {
		// ExtFunc objFunc = objExtFuncs[i];
		// String strFuncName = objFunc.getFuncName();
		// if (objFunc.getFuncDriverName().equals(this.getClass().getName()) && objFunc instanceof MeasFunc) {
		// MeasFunc objMeasFunc = (MeasFunc) objFunc;
		// if (strFuncName.equalsIgnoreCase(MSELECT)
		// || strFuncName.equalsIgnoreCase(MSELECTS)
		// || strFuncName.equalsIgnoreCase(MSELECTA)) {
		// //��������н�����mselect�������������޹ص�ָ�갴��������ָ��ArrayList���Hashtable��������MSELECT�����Hashtable������hashFunc��
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
		// //����ָ��׷�ٲ���
		// MeasureTraceVO[] measureTraceVOs = objEnv.getMeasureTraceVOs();
		// if(measureTraceVOs != null){
		// hashGroups.put(ICalcEnv.MEASURE_TRACE_FLAG, measureTraceVOs);
		// }
		//
		// //���ָ�꺯��ֵ
		// /**
		// * hashMeasValue��key��MSUMDATA��MSELECT,MCOUNT,MMAX,MMIN,MAVG,MSUM,HBMSELECT;
		// * value(1)��һ��Hashtable ,����key������������+"\r\n"��ɵ��ַ���,
		// * value(2)��hashtable,
		// * ��hashMeasValue��key=MSUMDATA, value(2)��key����ΪString(measurePK) ,value=hashtable (key=MSUM,MMAX,MMIN,MAVG,CURKEY,value=�ų���ǰ�ؼ�ֵ��ĺϼơ������С������,��ǰ�ؼ���ֵ�ִ�)
		// * ��hashMeasValue��key=����, value(2)��key ��measurePK,value��Double,Stringָ��ֵ
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
		// //��������mcountֵ
		// Hashtable hashCountValue = (Hashtable) hashMeasValue.get(MCOUNT);
		// batchCalcMfunc(hashCount,hashCountValue,objEnv);
		//
		// //��������Mmaxֵ
		// Hashtable hashMax = (Hashtable) hashSum.get(MMAX);
		// batchCalcMfunc(hashMax,(Hashtable) hashMeasValue.get(MMAX),objEnv);
		//
		// //��������Mminֵ
		// Hashtable hashMin = (Hashtable) hashSum.get(MMIN);
		// batchCalcMfunc(hashMin,(Hashtable) hashMeasValue.get(MMIN),objEnv);
		//
		// //��������Mavgֵ
		// Hashtable hashMavg = (Hashtable) hashSum.get(MAVG);
		// batchCalcMfunc(hashMavg,(Hashtable) hashMeasValue.get(MAVG),objEnv);
		//
		// //��������MSUMֵ
		// Hashtable hashMsum = (Hashtable) hashSum.get(MSUM);
		// batchCalcMfunc(hashMsum,(Hashtable) hashMeasValue.get(MSUM),objEnv);
		//
		// //��������Mselectֵ
		// Hashtable hashSelectValue = (Hashtable) hashMeasValue.get(MSELECT);
		// batchCalcMfunc(hashSelect,hashSelectValue,objEnv);
		//
		// //��HBselect�������д���
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
