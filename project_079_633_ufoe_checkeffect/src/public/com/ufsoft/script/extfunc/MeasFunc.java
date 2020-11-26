package com.ufsoft.script.extfunc;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.iufo.calculate.IVersionFetcher;
import nc.itf.iufo.storecell.IStoreCellPackQrySrv;
import nc.pub.iufo.cache.RepFormatModelCache;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.iufo.calculate.MeasFuncBO_Client;
import nc.vo.iufo.calculate.DatePropVO;
import nc.vo.iufo.calculate.KeyVO;
import nc.vo.iufo.calculate.KeyWordVO;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.MeasureTraceVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.pub.date.UFODate;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.iufo.storecell.StoreCellVO;
import nc.vo.pub.lang.UFDouble;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.table.exarea.ExtendAreaCell;
import com.ufsoft.iufo.fmtplugin.datastate.AbsRepDataChannel;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.dynarea.DynAreaUtil;
import com.ufsoft.iufo.fmtplugin.dynarea.DynamicAreaModel;
import com.ufsoft.iufo.util.parser.IFuncType;
import com.ufsoft.iufo.util.parser.UfoParseException;
import com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup;
import com.ufsoft.iuforeport.reporttool.temp.KeyDataVO;
import com.ufsoft.script.base.CommonExprCalcEnv;
import com.ufsoft.script.base.ICalcEnv;
import com.ufsoft.script.base.ITraceFunc;
import com.ufsoft.script.base.UfoDecimal;
import com.ufsoft.script.base.UfoDouble;
import com.ufsoft.script.base.UfoEElement;
import com.ufsoft.script.base.UfoInteger;
import com.ufsoft.script.base.UfoNullVal;
import com.ufsoft.script.base.UfoString;
import com.ufsoft.script.base.UfoVal;
import com.ufsoft.script.datachannel.IUFODynAreaDataParam;
import com.ufsoft.script.datachannel.IUFOTableData;
import com.ufsoft.script.exception.CmdException;
import com.ufsoft.script.exception.OprException;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.script.exception.ScriptException;
import com.ufsoft.script.exception.TranslateException;
import com.ufsoft.script.exception.UfoCmdException;
import com.ufsoft.script.exception.UfoValueException;
import com.ufsoft.script.expression.NumOperand;
import com.ufsoft.script.expression.StrOperand;
import com.ufsoft.script.expression.UfoExpr;
import com.ufsoft.script.expression.UfoFullArea;
import com.ufsoft.script.function.ExtFunc;
import com.ufsoft.script.function.UfoFunc;
import com.ufsoft.script.function.UfoFuncInfo;
import com.ufsoft.script.spreadsheet.ReportDynCalcEnv;
import com.ufsoft.script.spreadsheet.UfoCalcEnv;
import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.IArea;

/**
 * 指标函数
 * 
 * @author liuchuna
 * @created at 2010-6-7,下午01:20:52
 *
 */
@SuppressWarnings("unchecked")
public class MeasFunc extends ExtFunc implements ITraceFunc{
	
	private static final long serialVersionUID = 2295167828250943491L;

	//数据库指标数值 add by ljhua 2--5-5-19 解决动态区域指标函数优化
	private Hashtable m_hashDbData=null;
	
	private Hashtable m_hashDynMeasDataInRep =null;//如果此公式计算本表页动态指标，则此变量记录动态区域指标数值
	
	private Hashtable m_hashDataByKeyData;
	
	private MeasurePubDataVO m_oValueKey;//与m_oValue对应的主键信息
	
	/**
	 * 指标取数函数的条件 [0]表示非时间条件,且以补充当前环境非时间条件，且以zkey形式表示;
	 * 				   [1]表示时间条件，原函数条件中的时间条件，未经过补充
	 */
	private UfoExpr[] m_exprCons=null;
	private UfoExpr[] m_exprAllConds=null;
	private UfoExpr m_userRightExpr=null;
	/**
	 * 记录mselecta, hbmselecta函数中指标参数
	 * add by ljhua 2006-11-2
	 */
	protected IStoreCell[] m_measureParam=null;

	public final static java.lang.String MEASEXENVPREFIX = "EXENV_Meas_";
	public final static String TAG_CURKEYVALUE="CURKEY";

	private final static String CACHE_KEY="cachekey";
	private final static int MSTATICFUNC_STEP=10;
	private final static int MSELECTFUNC_STEP=100;
	
	private MeasFuncEx m_exFunc=null;


	/**
	 * GAMeasFunc constructor comment.
	 * @param nFid short
	 * @param params java.util.ArrayList
	 * @exception nc.util.ga.parser.UfoCmdException The exception description.
	 */
	public MeasFunc(short nFuncID, String strFuncName, java.util.ArrayList params, String strDriver, byte bRtnType) throws CmdException {
		super(nFuncID, strFuncName, params, strDriver, bRtnType);
        m_hashDbData = null;
        m_hashDataByKeyData = null;
        m_hashDynMeasDataInRep = null;
        m_exprCons = null;
        m_userRightExpr = null;
        m_measureParam = null;
	}


	/**
	 * 如果是固定表、没有进行预处理的指标计数函数用此方法计算。
	 * 创建日期：(2003-8-8 10:06:43)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures nc.vo.iufo.measure.MeasureVO
	 * @param objKeyCond com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	private UfoVal[] calcCountValue(
			IStoreCell objMeasure,
			UfoExpr objKeyCond,
			UfoCalcEnv objEnv)
	throws CmdException {
		if (objEnv == null || objEnv.getMeasureEnv() == null) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		try {
			Object objValue = null;

			com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup objKeyDatas = null;
			if(objEnv instanceof ReportDynCalcEnv){
				objKeyDatas = ((ReportDynCalcEnv) objEnv).getKeyDatas();
			}
			if(objEnv.isClient()==false){
				objValue = MeasFuncBO_Client.getAggrValue(
						MeasFuncDriver.getFuncIdByName(getFuncName()),
						objMeasure,
						objKeyCond,
						objEnv.getMeasureEnv(),
						objEnv.getExEnv(),
						objEnv.getRepPK(),
						objEnv.getKeys(),
						objKeyDatas,
						objEnv.getDataChannel());
			}else{
				objValue = nc.ui.iufo.calculate.MeasFuncBO_Client.getAggrValue(
						MeasFuncDriver.getFuncIdByName(getFuncName()),
						objMeasure,
						objKeyCond,
						objEnv.getMeasureEnv(),
						objEnv.getExEnv(),
						objEnv.getRepPK(),
						objEnv.getKeys(),
						objKeyDatas,
						objEnv.getDataChannel());
			}
			return new UfoVal[]{UfoVal.createVal(objValue)};

		} catch(ScriptException e){
			AppDebug.debug(e);//@devTools  e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		}
		catch (Exception e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
	}
	/**
	 * 与动态区域关联的指标计数函数取值
	 * 
	 * @create by liuchuna at 2010-6-7,下午01:21:40
	 *
	 * @param objEnv
	 * @return
	 * @throws CmdException
	 */
	private UfoVal[] calcDynCountValue(UfoCalcEnv objEnv) throws CmdException {
		Object objExZKeyValue = objEnv.getExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
		if (objEnv == null) {
//		if (objEnv == null || !(objEnv instanceof ReportDynCalcEnv)) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		ReportDynCalcEnv env = (ReportDynCalcEnv) objEnv;
		KeyDataGroup objKeyValueInEnv = env.getKeyDatas();
		String strEnvDynAreaPK = env.getDynArea();
		env.removeExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
		try {
			if (getValue() != null) {
				return getValue();
			}
			IStoreCell[] mvos = getMeasures(objEnv);

			if (mvos == null || objEnv == null
					|| objEnv.getUfoDataChannel() == null) {
				return new UfoVal[] { UfoNullVal.getSingleton() };
			}
			// 从数据库中取所有符合条件的值
			Hashtable hashDataInDB = null;
			if (mvos[0].getDbcolumn() != null) {
				hashDataInDB = readAggrDatasFromDB(objEnv);
			}
			if (hashDataInDB == null) {
				hashDataInDB = new Hashtable();
			}

			// 添加并替换当前计算报表数据
			String strCurMeasurePK=mvos[0].getCode();
			
			Hashtable hashDataInRep = null;
			String strDynAreaPK = null;
			if(mvos[0] instanceof MeasureVO) {
				// 获得指标所在动态区域pk
				strDynAreaPK = objEnv.getDynPKByMeasurePK(strCurMeasurePK);
				
				// 当前计算报表数据，缓存中数据
				hashDataInRep =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,mvos[0]);
			} else {
				// 获得指标所在动态区域pk
				strDynAreaPK = objEnv.getDynPKByStoreCellPos(CellPosition.getInstance(strCurMeasurePK));
				
				// 当前计算报表数据，缓存中数据
				hashDataInRep =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,mvos[0]);
			}
			
			env.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, objKeyValueInEnv);
			// 获取函数参数，即条件表达式
			UfoExpr objCond = (UfoExpr) getParams().get(1);

			Iterator it = hashDataInRep.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				KeyDataGroup objKeyDatas = (KeyDataGroup) entry.getKey();
				String strKey = "";
				// 设置动态区计算环境信息
				env.setDynAreaInfo(strDynAreaPK, objKeyDatas);
				// 根据关键字取得在动态区中的组号
				int m = DynAreaUtil.getOwnerUnitAreaNumByKeyData(objKeyDatas, strDynAreaPK, ((AbsRepDataChannel)env.getDataChannel()).getDataModel());
				if(m == -1){
					continue;
				}
				// 设置数据通道的动态区计算环境，动态区pk及组号
				((AbsRepDataChannel)env.getDataChannel()).setDynAreaCalcParam(new IUFODynAreaDataParam(m, null, strDynAreaPK));
				if (objCond == null || objCond.calcExpr(env)[0].doubleValue() == 1) {
					// 如果条件表达式存在，且条件为真，将当前报表中的值替代hashDataInDB中响应的值
					MeasurePubDataVO objMeasKeyData = objEnv.getMeasureEnv();
					if (objMeasKeyData == null) {
						throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
					}
					KeyGroupVO objKG = objMeasKeyData.getKeyGroup();
					if (objKG == null) {
						throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
					}
					nc.vo.iufo.keydef.KeyVO[] objKeyVOs = objKG.getKeys();
					if (objKeyVOs != null) {
						for (int i = 0; i < objKeyVOs.length; i++) {
							String strValue = objMeasKeyData.getKeywordByPK(objKeyVOs[i].getPk_keyword());
							if (strValue != null && strValue.length() > 0) {
								strKey += strValue;
								strKey += "\r\n";
							}
						}
					}
					hashDataInDB.put(strKey, ((MeasureDataVO) entry.getValue()).getUFDoubleValue());
				}

			}
			//计算
			UfoVal[] objVals = new UfoVal[] { UfoInteger.getInstance(hashDataInDB.size())};
			setValue(objVals);
			return objVals;

		}catch(ScriptException e){
			AppDebug.debug(e);
			throw new UfoCmdException(e);
		}
		catch (Exception e) {
			AppDebug.debug(e);
			throw new UfoCmdException(e.getMessage());
		}finally{
			if(objExZKeyValue != null){
				objEnv.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, objExZKeyValue);
			}else{
				objEnv.removeExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
			}
			env.setDynAreaInfo(strEnvDynAreaPK, objKeyValueInEnv);
			// liuchun 20110610 修改，清除动态区计算参数
			((AbsRepDataChannel)env.getDataChannel()).removeDynAreaCalcParam();
		}
	}
	/**
	 * 先从当前的报表数据中查找，没有再从数据库取值
	 * 创建日期：(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	protected UfoVal[] calcDynSelectValue(UfoCalcEnv objEnv) throws CmdException {
		try {
			// java.util.ArrayList alPara = getParams();
			IStoreCell[] objMeasures = getMeasures(objEnv);//函数要计算的所有区域指标信息.
			if (objMeasures == null
					|| objEnv == null
					||objEnv.getUfoDataChannel() == null) {
				//如果没有指标参数信息或者没有报表信息，抛错
				throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
			}
			MeasFuncDriver objFuncDriver =
				(MeasFuncDriver) objEnv.loadFuncListInst().getExtDriver(
						MeasFuncDriver.class.getName());

			//得到关键字组合的PK
			String strKeyGroupPK = getKeyGroupPK(objFuncDriver.getMeasCache(),objEnv);
			if (isOnlyOneParamMselect()) {
				//1.没有条件，那么从当前计算环境对应的动态区域取值
				return calcDynSelectValueByEnv(
						objMeasures,
						strKeyGroupPK,
						null,
						null,
						null,
						null,
						objEnv);
			} else {
				UfoExpr objOffset = getOffsetParamVal();//取偏移量
				UfoExpr objKeyCond = getKeyCondParamVal();//取条件表达式
				DatePropVO objDateProp = getDatePropParamVal();//取日期
				Integer nVer = getVerParamVal(objEnv);//获得版本信息参数的值
				//[b].计算条件的值，包括偏移量和关键字条件，
				Double nOffset = null;
				if (objOffset != null) {
					nOffset = new Double(objOffset.getValue(objEnv)[0].doubleValue());
				}
				UfoExpr objKeyCondValue = null;
				boolean bKeyCondSame = true;


				if (objKeyCond != null
						&& objEnv instanceof ReportDynCalcEnv
						&& ((ReportDynCalcEnv) objEnv).getKeyDatas() == null) {
					//当计算环境为主表计算时处理,表达式不为空且为当前日期
					return calcDynSelectValue(
							strKeyGroupPK, 			//关键字组合的PK
							objMeasures,			//函数要计算的所有区域指标信息.
							objDateProp,			//日期
							nOffset,				//偏移量 
							objKeyCond,				//条件表达式
							nVer,					//版本号
							objEnv);				//报表计算环境。
				}

				if (objKeyCond != null) {
					UfoEElement[] objEles = objKeyCond.getElements();
					UfoEElement[] objNewEles =
						objEles == null ? null : new UfoEElement[objEles.length];
					bKeyCondSame = checkKeyCondWithEnv(objKeyCond, objNewEles, objEnv);
					objKeyCondValue =
						new UfoExpr(objNewEles, objKeyCond.getType(), objKeyCond.getStatus());
				}

				//判断与当前计算环境是否一致，
				if ((nOffset == null || nOffset.intValue() == 0)
						&& (nVer == null || (objEnv.getMeasureEnv() != null && nVer.intValue() == objEnv.getMeasureEnv().getVer()))
						&& (objKeyCond == null || bKeyCondSame)) {
					//{a}如果一致
					return calcDynSelectValueByEnv(
							objMeasures,
							strKeyGroupPK,
							objDateProp,
							nOffset,
							objKeyCondValue,
							nVer,
							objEnv);
				}
				//取数条件不是当前表页数据时
				return calcDynSelectValue(
						strKeyGroupPK,
						objMeasures,
						objDateProp,
						nOffset,
						objKeyCondValue,
						nVer,
						objEnv);
			}
		} catch (UfoValueException e) {
			throw new UfoCmdException(e);
		}
	}
	/**
	 * 计算动态区域关联的指标取数函数值。
	 * 创建日期：(2003-8-8 10:06:43)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures nc.vo.iufo.measure.MeasureVO[]
	 * @param objDateProp nc.vo.iufo.keyword.DatePropVO
	 * @param nOffset java.lang.Double
	 * @param objKeyCond com.ufsoft.iufo.util.expression.UfoExpr
	 * @param nVer java.lang.Integer
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	private UfoVal[] calcDynSelectValue(
			String strKeyGroupPK,
			IStoreCell[] objMeasure,
			DatePropVO objDateProp,
			Double nOffset,
			UfoExpr objKeyCond,
			Integer nVer,
			UfoCalcEnv objEnv)
	throws CmdException {

		AppDebug.error("=================into calcDynSelectValue:");
		
		if (objEnv == null || objEnv.getMeasureEnv() == null) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}

		if (nVer != null && nVer.intValue() != objEnv.getMeasureEnv().getVer()) {
			//1.从批量计算的缓存数据中获得，如果未获得值则从数据库中获得数据
			return getSelectValueFromCache(
					strKeyGroupPK,
					objMeasure,
					objDateProp,
					nOffset,
					objKeyCond,
					nVer,
					objEnv);
		}

		if (!(objEnv instanceof ReportDynCalcEnv)) {
			throw new UfoCmdException("miufo1000401");  //"计算动态区域必须使用ReportDynCalcEnv作为计算环境！"
		}
		ReportDynCalcEnv env = (ReportDynCalcEnv) objEnv;
		com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup objKeyValueInEnv =
			env.getKeyDatas();
		Object oldExZKeyValue = env.getExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
		String strDynAreaInEnv = env.getDynArea();
		try {
//			if (objMeasure == null
//			|| env.getReportEnv() == null
//			|| env.getReportEnv().m_dataChannel == null
//			|| env.getReportEnv().m_dataChannel.getTable() == null
//			/*|| env.getDynArea() == null*/) {
//			return getNullVal(objEnv);
//			}
			if (objMeasure == null
					|| env.getDataChannel() == null) {
				return getNullVal(objEnv);
			}
			//note by ljhua 2005-1-20 感觉不需要检查报表
			/*
        com.ufsoft.iuforeport.reporttool.data.UfoTable objTable =
            env.getReportEnv().m_dataChannel.getTable();

        nc.vo.iuforeport.rep.ReportVO objRep =
            env.isOnServer()
                ? (nc.vo.iuforeport.rep.ReportVO) nc
                    .bs
                    .iufo
                    .cache
                    .BSCacheManager
                    .getSingleton()
                    .getReportCache()
                    .get(objMeasure[0].getReportPK())
                : (nc.vo.iuforeport.rep.ReportVO) nc
                    .ui
                    .iufo
                    .cache
                    .UICacheManager
                    .getSingleton()
                    .getReportCache()
                    .get(objMeasure[0].getReportPK());
        if (objRep == null) {
            throw new TranslateException("miufo1000402", new String[]{objMeasure[0].getName()});  //"指标" + objMeasure[0].getName() + "对应的报表找不到！"
        }*/
			UfoVal[] objVals = new UfoVal[objMeasure.length];
			boolean[] bHasValueInRep = new boolean[objMeasure.length];
			int nHasValue = 0;

			//add by ljhua 2005-6-10 解决mselect在动态区域内无法正确取当前报表指标值问题。
//			UfoExpr objKeyCondJudge=getSelectFullCond(objDateProp,nOffset,objEnv,strKeyGroupPK);
			UfoExpr objKeyCondJudge=getValueTimeCondExpr(objDateProp,nOffset,objEnv,strKeyGroupPK);

			//2.以下为指标取当前表页指标数据时取值处理.
			boolean isDynConTimeKey = isDynContainTimeKey(env);
			AppDebug.error("================= patch is exist:" + isDynConTimeKey);
			for (int i = 0; i < objMeasure.length; i++) {
				if (!isReferDynArea(objMeasure[i], objEnv)) {
					continue;
				}
				// @edit by wangyga at 2009-7-9,下午02:07:20
				if (!objEnv.isMeasureTrace() && (nOffset != null && nOffset.doubleValue() !=0  && !isDynConTimeKey))
					continue;
				
//				Hashtable hashValues =
//				objTable.getDynAreaMeasureValues(objMeasure[i].getCode());
				String strDynAreaPK = null;
				Hashtable hashValues = null;
				if(objMeasure[i] instanceof MeasureVO) {
					//获得指标在动态区域内的值
					strDynAreaPK=objEnv.getDynPKByMeasurePK(objMeasure[i].getCode());
					
					//需要调用取得关键字维度值的方法
					hashValues =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,objMeasure[i]);
				} else {
					strDynAreaPK=objEnv.getDynPKByStoreCellPos(CellPosition.getInstance(objMeasure[i].getCode()));
					
					hashValues =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,objMeasure[i]);
				}

				if (hashValues != null) {
					//add by ljhua 2005-3-7 解决mselect在动态区域内无法取当前报表指标值问题。
//					UfoExpr objKeyCondJudge =
//					ReplenishKeyCondUtil.replenishKeyCond(
//					objTable.getUTableCache().getAllKeyVO(
//					objTable.getUTableCache().getDynPKByMeasurePK(objMeasure[i].getCode())),
//					objDateProp,
//					nOffset,
//					objKeyCond,
//					objEnv);
					//note by ljhua 2005-6-10 解决mselect在动态区域内无法正确取当前报表指标值问题。
//					UfoExpr paramExprCond=getKeyCondParamVal();
//					UfoExpr objKeyCondJudge=replenishSelectCond(objDateProp,nOffset,paramExprCond,objEnv,strKeyGroupPK);

					env.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, objKeyValueInEnv);

					Enumeration enKey = hashValues.keys();
					while (enKey.hasMoreElements()) {
						com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup objKeyValues =
							(com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup) enKey.nextElement();
						if (objKeyValues != null) {
							env.setDynAreaInfo(strDynAreaPK, objKeyValues);
							
							measureTrace(env);

							if (objKeyCondJudge.calcExpr(env)[0].doubleValue() == 1) {
								//chxw 设置本表追踪参数
								if(objEnv.isMeasureTrace() && objEnv.getExEnv(ICalcEnv.MEASURE_TRACE_FLAG) != null){
									MeasureTraceVO[] arrtracevo = (MeasureTraceVO[])objEnv.getExEnv(ICalcEnv.MEASURE_TRACE_FLAG);
									if(arrtracevo != null){
										for(MeasureTraceVO tracevo:arrtracevo){
											IKeyDetailData[] keyvalues = (IKeyDetailData[])tracevo.getKeyvalues().clone();
											com.ufsoft.iuforeport.reporttool.temp.KeyDataVO[] keyDatas = objKeyValues.getKeyDatas();
											for(com.ufsoft.iuforeport.reporttool.temp.KeyDataVO keyData : keyDatas){
												int index = env.getMeasureEnv().getKeyByPK(keyData.getKey().getPk_keyword());
												keyvalues[index] = keyData.getKeyData();
											}
											tracevo.setKeyvalues(keyvalues);
											tracevo.setDynAreaPK(strDynAreaPK);
											tracevo.setObjKeyValues(objKeyValues);
										}				
									}			
								}
								MeasureDataVO	mdVO = (MeasureDataVO) hashValues.get(objKeyValues);
								if( mdVO != null ){
									if (objMeasure[i].getType() == MeasureVO.TYPE_NUMBER) {
										objVals[i] = UfoDouble.getInstance( mdVO.getUFDoubleValue().doubleValue());
									} else if(objMeasure[i].getType() == MeasureVO.TYPE_BIGDECIMAL){
										objVals[i] = UfoDecimal.getInstance(mdVO.getUFDoubleValue().toBigDecimal());
									} else {
										objVals[i] = UfoString.getInstance( mdVO.getDataValue() );
									}
								}else{
									objVals[i] = null;
								}
								bHasValueInRep[i] = true;
								nHasValue++;
								break;
							}
						}
					}
				}
			}
			if (objMeasure.length > nHasValue) {
				IStoreCell[] objMeasures = new MeasureVO[objMeasure.length - nHasValue];
				int nPos = 0;
				for (int i = 0; i < objMeasure.length; i++) {
					if (!bHasValueInRep[i]) {
						objMeasures[nPos++] = objMeasure[i];
					}
				}
				//add by ljhua 2005-3-7 解决mselect函数取不出数据问题..此问题是由于上面的while循环中改变了env环境值
				env.setDynAreaInfo(strDynAreaInEnv, objKeyValueInEnv);

				UfoVal[] objSomeVals =
					calcSelectValue(objMeasures, objDateProp, nOffset, objKeyCond, nVer, env);
				nPos = 0;
				//如果指标取数不为当前表页，则取数据库计算值.
				for (int i = 0; i < objMeasure.length; i++) {
					if (!bHasValueInRep[i]) {
						objVals[i] = objSomeVals[nPos++];
					}
				}
			}
			return objVals;

		}catch(ScriptException e){
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		}
		catch (Exception e) {
			//objEnv.removeExEnv(objEnv.EX_ZKEYVALUES);
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		} finally {
			if(oldExZKeyValue!=null)
				env.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, oldExZKeyValue);
			else
				env.removeExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
			env.setDynAreaInfo(strDynAreaInEnv, objKeyValueInEnv);
		}
	}
	
	private boolean isDynContainTimeKey(UfoCalcEnv env)
    {
        nc.vo.iufo.keydef.KeyVO mainTimeKey = env.getMeasureEnv().getKeyGroup().getTTimeKey();
        nc.vo.iufo.keydef.KeyVO allKeys[] = env.getKeys();
        if(allKeys != null)
        {
            for(int i = 0; i < allKeys.length; i++)
                if(allKeys[i].isTTimeKeyVO())
                    return mainTimeKey == null || !mainTimeKey.getPk_keyword().equals(allKeys[i].getPk_keyword());

        }
        return false;
    }
	
	/**
	 * 从当前的报表数据中查找
	 * 创建日期：(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	private UfoVal[] calcDynSelectValueByEnv(
			IStoreCell[] objMeasure,
			String strKeyGroupPK,
			DatePropVO objDateProp,
			Double nOffset,
			UfoExpr objKeyCondValue,
			Integer nVer,
			UfoCalcEnv objEnv)
	throws CmdException {
//		if (objMeasure == null
//				|| objEnv == null
//				|| objEnv.getUfoDataChannel() == null
//				|| !(objEnv instanceof ReportDynCalcEnv)
//				|| ((ReportDynCalcEnv) objEnv).getDynArea() == null) {
//			return getNullVal(objEnv);
//		}
		if (objMeasure == null
				|| objEnv == null
				|| objEnv.getUfoDataChannel() == null
				|| !(objEnv instanceof ReportDynCalcEnv)) {
			return getNullVal(objEnv);
		}
//		try {
		ReportDynCalcEnv env = (ReportDynCalcEnv) objEnv;

		UfoVal[] objVals = new UfoVal[objMeasure.length];
		boolean bNotAllDynMeas = false;
		boolean[] bNotDynMeas = new boolean[objMeasure.length];
		for (int i = 0; i < objMeasure.length; i++) {
			if (isReferDynArea(objMeasure[i], objEnv)) {
//				Hashtable hashValues =
//				objTable.getDynAreaMeasureValues(objMeasure[i].getCode());

				//获得指标在动态区域内的值
				//moidfy by ljhua 2006-8-9 解决mselect('动态区指标pk'）函数的计算效率
				if(m_hashDynMeasDataInRep==null){
					if(objMeasure[i] instanceof MeasureVO) {
						String strDynAreaPK=objEnv.getDynPKByMeasurePK(objMeasure[i].getCode());
						m_hashDynMeasDataInRep=strDynAreaPK==null?null:objEnv.getUfoDataChannel().
								getDatasByMeta(strDynAreaPK,objMeasure[i]);
					} else {
						String strDynAreaPK=objEnv.getDynPKByStoreCellPos(CellPosition.getInstance(objMeasure[i].getCode()));
						m_hashDynMeasDataInRep=strDynAreaPK==null?null:objEnv.getUfoDataChannel().
								getDatasByMeta(strDynAreaPK,objMeasure[i]);
					}
				}

				MeasureDataVO mdVO = (MeasureDataVO) m_hashDynMeasDataInRep.get(env.getKeyDatas());
				if( mdVO != null ){
					if (objMeasure[i].getType() == MeasureVO.TYPE_NUMBER) {
						objVals[i] = UfoDouble.getInstance( mdVO.getUFDoubleValue().doubleValue());
					} else if(objMeasure[i].getType() == MeasureVO.TYPE_BIGDECIMAL){
						objVals[i] = UfoDecimal.getInstance(mdVO.getUFDoubleValue().toBigDecimal());
					} else {
						objVals[i] = UfoString.getInstance( mdVO.getDataValue() );
					}
				}else{
					objVals[i] = null;
				}

				objMeasure[i] = null;
			} /*else if(MeasOperand.isRelaWithArea(objMeasure[i], objEnv, 2)){//注释掉是因为如果有指标与动态区域关联，那么不可能有指标与固定区域关联
            objVals[i] = getSelectValueFromArea(objMeasure[i], objEnv);
            }*/
			else {
				bNotAllDynMeas = true;
				bNotDynMeas[i] = true;
			}
		}
		if (bNotAllDynMeas) {
			//对于非当前报表动态区域指标，先从批量计算的缓存中获得数据，如果未获得则从数据库中装载。
			UfoVal[] objValInDB =
				getSelectValueFromCache(
						strKeyGroupPK,
						objMeasure,
						objDateProp,
						nOffset,
						objKeyCondValue,
						nVer,
						objEnv);
			for (int i = 0; i < bNotDynMeas.length; i++) {
				if (bNotDynMeas[i]) {
					objVals[i] = objValInDB[i];
				}
			}
		}
		return objVals;
//		} catch (UfoValueException e) {
//		throw new UfoCmdException("miufo1000403", e);  //"数据类型错误" 
//		}
	}
	private MeasurePubDataVO createMeaurePubData(KeyGroupVO keyGroupVO, String strKeyValue, ReportDynCalcEnv env){
		MeasurePubDataVO mpubData=new MeasurePubDataVO();
		mpubData.setKeyGroup(keyGroupVO);
		mpubData.setAccSchemePK(env.getAccPeriodSchemePK());
		mpubData.setKType(keyGroupVO.getKeyGroupPK());

		nc.vo.iufo.keydef.KeyVO[] keyVos=keyGroupVO.getKeys();
		StringTokenizer tokenizer=new StringTokenizer(strKeyValue,"\r\n");
		int iLen=keyVos.length;
		String strTemp=null;
		for (int i=0;i<iLen;i++){
			strTemp=tokenizer.nextToken();
			mpubData.setKeywordByIndex(i+1, strTemp, true);
//			if(keyVos[i].getType()==nc.vo.iufo.keydef.KeyVO.TYPE_TIME){
//				mpubData.setInputDate(strTemp);
//				mpubData.setTimeCode(UFODate.getTimeCode(strTemp));
//			}else if (keyVos[i].getPk_keyword()==nc.vo.iufo.keydef.KeyVO.CORP_PK){
//				mpubData.setUnitPK(strTemp);
//			}
//			if(i==0)
//				mpubData.setKeyword1(strTemp);
//			else if(i==1)
//				mpubData.setKeyword2(strTemp);
//			else if (i==2)
//				mpubData.setKeyword3(strTemp);
//			else if (i==3)
//				mpubData.setKeyword4(strTemp);
//			else if (i==4)
//				mpubData.setKeyword5(strTemp);
//			else if (i==5)
//				mpubData.setKeyword6(strTemp);
//			else if (i==6)
//				mpubData.setKeyword7(strTemp);
//			else if (i==7)
//				mpubData.setKeyword8(strTemp);
//			else if (i==8)
//				mpubData.setKeyword9(strTemp);
//			else if (i==9)
//				mpubData.setKeyword10(strTemp);
		}
		return mpubData;

	}
	/**
	 * 创建新的关键字数据集合
	 * @param oldKeyDatas
	 * @param strKeyValue
	 * @return
	 */
	@SuppressWarnings("unused")
	private KeyDataGroup createNewKeyGroupByselect(KeyDataGroup oldKeyDatas,String strKeyValue){

		//获得动态区关键字及其对应值的hashmap
		HashMap hashKeyValue=new HashMap();

		StringTokenizer tokenizer=new StringTokenizer(strKeyValue,";");
		String strTemp=null;
		String strKey=null;
		String strValue=null;
		while(tokenizer.hasMoreTokens()){
			strTemp=tokenizer.nextToken();
			int iPos=strTemp.indexOf("=");
			if(iPos>=0){
				strKey=strTemp.substring(0,iPos);
				strValue=strTemp.substring(iPos+1,strTemp.length());
				hashKeyValue.put(strKey,strValue);			
			}

		}

		//构建新的KeyDataGroup
		KeyDataGroup newKeyGroup=new KeyDataGroup();
		KeyDataVO[] oldDatas=oldKeyDatas.getKeyDatas();
		if(oldDatas!=null && oldDatas.length>0){
			int iLen=oldDatas.length;
			KeyDataVO[] newKeyDatas=new KeyDataVO[iLen];
			String strKeyPK=null;
			for(int i=0;i<iLen;i++){
				strKeyPK=oldDatas[i].getKey().getPk_keyword();
				newKeyDatas[i]=new KeyDataVO();
				newKeyDatas[i].setKey(oldDatas[i].getKey());

				if(	hashKeyValue.containsKey(strKeyPK)){
					String strNewKeyVal=(String) hashKeyValue.get(strKeyPK);
					newKeyDatas[i].setValue(strNewKeyVal);
				}else{
					newKeyDatas[i].setValue(oldDatas[i].getValue());
				}
			}
			newKeyGroup.setKeyDatas(newKeyDatas);

		}

		return newKeyGroup;
	}
	
	@SuppressWarnings("unused")
	private KeyDataGroup createNewKeyGroupBySelect(nc.vo.iufo.keydef.KeyVO[] keyVos,String strKeyValue,nc.vo.iufo.keydef.KeyVO[] mainKeyVOs){
		StringTokenizer tokenizer=new StringTokenizer(strKeyValue,"\r\n");

		ArrayList listMainKeyPKs=new ArrayList();
		if(mainKeyVOs!=null){
			for(int i=0;i<mainKeyVOs.length;i++)
				listMainKeyPKs.add(mainKeyVOs[i].getPk_keyword());
		}
		int iLen=keyVos.length;

		ArrayList listKeyData=new ArrayList();
		String stKeyValue=null;
		KeyDataVO newKeyData=null;
		for (int i=0;i<iLen;i++){
			stKeyValue=tokenizer.nextToken();
//			if(keyVos[i].isPrivate()){
//				newKeyData=new KeyDataVO();
//				newKeyData.setKey(keyVos[i]);
//				newKeyData.setValue(stKeyValue);
//				listKeyData.add(newKeyData);
//			}
		}
		KeyDataGroup newKeyGroup=new KeyDataGroup();
		if(listKeyData.size()>0){
			KeyDataVO[] newKeyDatas=new KeyDataVO[listKeyData.size()];
			listKeyData.toArray(newKeyDatas);
			newKeyGroup.setKeyDatas(newKeyDatas);
		}

		return newKeyGroup;
	}

	private KeyDataGroup createNewKeyGroup(nc.vo.iufo.keydef.KeyVO[] keyVos,String strKeyValue){
		StringTokenizer tokenizer=new StringTokenizer(strKeyValue,"\r\n");

		
		
		int iLen=keyVos.length;
		ArrayList<KeyDataVO> listKeyData=new ArrayList<KeyDataVO>();
		String stKeyValue=null;
		KeyDataVO newKeyData=null;
		for (int i=0;i<iLen;i++){
			stKeyValue=tokenizer.nextToken();
			newKeyData=new KeyDataVO();
			newKeyData.setKey(keyVos[i]);
			newKeyData.setValue(stKeyValue);
			listKeyData.add(newKeyData);
		}
		KeyDataGroup newKeyGroup=new KeyDataGroup();
		if(listKeyData.size()>0){
			KeyDataVO[] newKeyDatas=new KeyDataVO[listKeyData.size()];
			listKeyData.toArray(newKeyDatas);
			newKeyGroup.setKeyDatas(newKeyDatas);
		}

		return newKeyGroup;
	}

	/**
	 * 获得指标的关键字组合VO
	 * @param mvo
	 * @param env
	 * @return
	 * @throws UfoCmdException
	 */
	private KeyGroupVO getMeaureKeyGroupVO(IStoreCell mvo,ReportDynCalcEnv env)
	throws UfoCmdException{
		MeasFuncDriver objFuncDriver =
			(MeasFuncDriver) env.loadFuncListInst().getExtDriver(
					MeasFuncDriver.class.getName());
		String strKeyGroupPK = getKeyGroupPK(mvo,objFuncDriver.getMeasCache());
		nc.vo.iufo.keydef.KeyVO[] keyVos=null;
		KeyGroupVO keyGroupVO=null;
		if(strKeyGroupPK!=null){
			keyGroupVO=objFuncDriver.getKeyGroupCache().getByPK(strKeyGroupPK);
			if(keyGroupVO!=null){
				keyVos=keyGroupVO.getKeys();
			}
		}
		if(strKeyGroupPK==null || keyGroupVO==null || keyVos==null)
			return null;

		return keyGroupVO;
	}
	/**
	 * 从m_hashDbData中获得动态区指定行keyGroupData的统计函数数据
	 * @param keyGroupData
	 * @return
	 */
	private Hashtable getDynCurStatFromCache( UfoExpr objCond ,IStoreCell mvo,ReportDynCalcEnv env,String strDynAreaPK,KeyDataGroup curkeyGroupData)
	throws CmdException{
		if(m_hashDbData==null || m_hashDbData.size()==0 ||  curkeyGroupData==null)
			return null;

		//获得指标对应的关键字集合
		KeyGroupVO keyGroupVO=getMeaureKeyGroupVO(mvo,env);
		nc.vo.iufo.keydef.KeyVO[] measureKeyVOs=keyGroupVO.getKeys();


		Object oldExZKeyValue = env.getExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
		String strOldDynPk=env.getDynArea();
		KeyDataGroup oldKeyDatas=env.getKeyDatas();
		//tianchuan 2012.9.17 取克隆，否则keyValues会被修改
		MeasurePubDataVO oldMPubVO=(MeasurePubDataVO)env.getMeasureEnv().clone();

		//获得主表关键字值
		KeyDataVO[] mainKeyDatas=getMainKeyData(env);

		try{
			KeyDataGroup newKeyGroupCurKey=combineKeyDatas(mainKeyDatas,curkeyGroupData);
			env.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, newKeyGroupCurKey);
			//tianchuan 记录原表的MeasurePubDataVO，zyear、zmonth等函数使用
			env.setExEnv("oldMPubVO", oldMPubVO);
			
//			env.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, curkeyGroupData);
			KeyDataGroup newKeyGroup=null;
			Iterator iterat=m_hashDbData.keySet().iterator();
			Object objKey=null;
			Hashtable hashValue=null;
			String strKeyValueTemp=null;
			MeasurePubDataVO newMPubVO=null;

			Hashtable hashReturn=null;

			while(iterat.hasNext()){
				objKey= iterat.next();
				if(CACHE_KEY.equals(objKey))
					continue;

				hashValue=(Hashtable) m_hashDbData.get(objKey);
				if(hashValue==null || hashValue.size()==0)
					continue;

				strKeyValueTemp=(String) hashValue.keySet().iterator().next();
				newMPubVO=createMeaurePubData(keyGroupVO,strKeyValueTemp,env);
				newKeyGroup=createNewKeyGroup(measureKeyVOs,strKeyValueTemp);
				env.setDynAreaInfo(strDynAreaPK,newKeyGroup);
				env.setMeasureEnv(newMPubVO);

				if ( objCond.calcExpr(env)[0].doubleValue() == 1) {
					if(hashReturn==null)
						hashReturn=new Hashtable();
					hashReturn.putAll(hashValue);
				}

			}
			return hashReturn;

		}catch(ScriptException e){
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		}
		catch (Exception e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
		finally{
			if(oldExZKeyValue!=null)
				env.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, oldExZKeyValue);
			else
				env.removeExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
			//tianchuan 移除记录的原表MeasurePubDataVO
			if(env.getExEnv("oldMPubVO")!=null){
				env.removeExEnv("oldMPubVO");
			}
			env.setMeasureEnv(oldMPubVO);
			env.setDynAreaInfo(strOldDynPk,oldKeyDatas);
		}
	}
	/**
	 * 从m_hashDbData中获得动态区指定行keyGroupData的统计函数数据
	 * @param keyGroupData
	 * @return
	 */
	private Hashtable getDynCurStatFromCache1(UfoExpr objCond, ReportDynCalcEnv env, KeyDataGroup curkeyGroupData)
	throws CmdException{
		if(m_hashDbData==null || m_hashDbData.size()==0 ||  curkeyGroupData==null)
			return null;
		
		Hashtable hashReturn = new Hashtable();
		Object oldExZKeyValue = env.getExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
		String strOldDynPk=env.getDynArea();
		KeyDataGroup oldKeyDatas=env.getKeyDatas();
		MeasurePubDataVO oldMPubVO=env.getMeasureEnv();
		
		try{
			IStoreCell[] mvos = this.getMeasures(env);
			//获得主表关键字值
			KeyDataVO[] mainKeyDatas=getMainKeyData(env);
			for(int i = 0 ; i < mvos.length; i++){
				IStoreCell mvo = mvos[i];
				String strDynAreaPK = env.getDynPKByMeasurePK(mvos[i].getCode());
				
				//获得指标对应的关键字集合
				KeyGroupVO keyGroupVO = getMeaureKeyGroupVO(mvo,env);
				nc.vo.iufo.keydef.KeyVO[] measureKeyVOs=keyGroupVO.getKeys();
				KeyDataGroup newKeyGroupCurKey=combineKeyDatas(mainKeyDatas,curkeyGroupData);
				env.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, newKeyGroupCurKey);

//				env.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, curkeyGroupData);
				KeyDataGroup newKeyGroup=null;
				Iterator iterat=m_hashDbData.keySet().iterator();
				Object objKey=null;
				Hashtable hashValue=null;
				Hashtable hashTemp=null;
				Hashtable hashMeasValue=null;
				String strKeyValueTemp=null;
				MeasurePubDataVO newMPubVO=null;
				Iterator iteratTemp = null;

				while(iterat.hasNext()){
					objKey= iterat.next();
//					if(CACHE_KEY.equals(objKey))
//						continue;
//
//					hashValue=(Hashtable) m_hashDbData.get(objKey);
//					if(hashValue==null || hashValue.size()==0)
//						continue;
//
//					strKeyValueTemp=(String) hashValue.keySet().iterator().next();
					/*
					 * MSUMA取出的数据格式形如：
					 * {指标={取数条件;={主表关键字组合值=10.0}}}
					 * MSUM取出的数据格式形如：
					 * {取数条件;={主表关键字组合值=4.0}}
					 * 这里的处理不能按照MSUM的处理进行
					 */
					//没有!mvo.equals(objKey)的判断会出现后面的指标覆盖前面指标值的情况
					if(CACHE_KEY.equals(objKey) || !mvo.equals(objKey))
						continue;
					//1.根据指标取出m_hashDbData最外层的value（Hashtable）
					hashTemp=(Hashtable) m_hashDbData.get(objKey);
					if(hashTemp==null || hashTemp.size()==0)
						continue;
					iteratTemp = hashTemp.keySet().iterator();
					while(iteratTemp.hasNext()){
						//2、取出最外层的value（Hashtable）的key值--取数条件
						strKeyValueTemp = (String)iteratTemp.next();
						//3.根据strKeyValueTemp取出里层的value（Hashtable）
						hashValue=(Hashtable) hashTemp.get(strKeyValueTemp);
						if(hashValue==null || hashValue.size()==0)
							continue;
						//4.取出最里层的value（Hashtable）的key值--关键字组合值
						strKeyValueTemp = (String) hashValue.keySet().iterator().next();
						
						newMPubVO=createMeaurePubData(keyGroupVO,strKeyValueTemp,env);
						newKeyGroup=createNewKeyGroup(measureKeyVOs,strKeyValueTemp);
						env.setDynAreaInfo(strDynAreaPK,newKeyGroup);
						env.setMeasureEnv(newMPubVO);

						if (objCond.calcExpr(env)[0].doubleValue() == 1) {
							if(hashMeasValue == null)
								hashMeasValue = new Hashtable();
							hashMeasValue.putAll(hashValue);
						}
					}
				}
				hashReturn.put(mvo, hashMeasValue);
			}

		}catch(ScriptException e){
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		}
		catch (Exception e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
		finally{
			if(oldExZKeyValue!=null)
				env.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, oldExZKeyValue);
			else
				env.removeExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
			env.setMeasureEnv(oldMPubVO);
			env.setDynAreaInfo(strOldDynPk,oldKeyDatas);
		}
		
		return hashReturn;
	}
	/**
	 * 获得关键字值集合dynKeyDataGroups中指定关键字strExprDynKeys的值字串集合
	 * @param dynKeyDataGroup
	 * @param strExprDynKeys
	 * @return
	 */
	private Vector getKeyValueStrings(KeyDataGroup[] dynKeyDataGroups,String[] strExprDynKeys){
		if(dynKeyDataGroups==null || dynKeyDataGroups.length==0 || strExprDynKeys==null || strExprDynKeys.length==0 )
			return null;
		int iLen=dynKeyDataGroups.length;
		Vector vecReturn=new Vector(iLen);
		for(int i=0;i<iLen;i++){
			vecReturn.add(getKeyValueString(dynKeyDataGroups[i],strExprDynKeys));
		}
		return vecReturn;
	}
	/**
	 * 获得关键字值集合dynKeyDataGroup中指定关键字strExprDynKeys的值字串
	 * @param dynKeyDataGroup 动态区域当前计算行的关键字值集合
	 * @param strExprDynKeys 关键字pk数组
	 * @return
	 */
	private static String getKeyValueString(KeyDataGroup dynKeyDataGroup,String[] strExprDynKeys){
		if(strExprDynKeys==null || dynKeyDataGroup==null )
			return null;
		KeyDataVO[] keyDatas=dynKeyDataGroup.getKeyDatas();
		if(keyDatas==null || keyDatas.length==0)
			return null;

		StringBuffer strBuf=new StringBuffer();
		int iLen=strExprDynKeys.length;
		int iSize=keyDatas.length;

		for (int i=0;i<iLen;i++){
			if(strExprDynKeys[i]==null)
				continue;

			for(int j=0;j<iSize;j++){
				if(keyDatas[j]==null)
					continue;
				if(keyDatas[j].getKey()==null)
					continue;
				if(strExprDynKeys[i].equals(keyDatas[j].getKey().getPk_keyword())){
					strBuf.append(strExprDynKeys[i]);
					strBuf.append("=");
					strBuf.append(keyDatas[j].getValue());
					strBuf.append(";");
					break;
				}
			}
		}
		return strBuf.toString();
	}
	private ArrayList getDynKeysFromKeyDatas(KeyDataGroup dynKeyDataGroup){
		if( dynKeyDataGroup==null)
			return null;

		//1.获得动态区域关键字pk集合
		ArrayList listDynKeyPKs=new ArrayList();
		KeyDataVO[] keyDatas=dynKeyDataGroup.getKeyDatas();
		if(keyDatas!=null){
			int iLen=keyDatas.length;
			nc.vo.iufo.keydef.KeyVO keyVO=null;
			for (int i=0;i<iLen;i++){
				if(keyDatas[i]==null)
					continue;
				keyVO=keyDatas[i].getKey();
				if(keyVO==null)
					continue;
				listDynKeyPKs.add(keyVO.getPk_keyword());
			}
		}
		return listDynKeyPKs;
	}
	/**
	 * 查找条件expr中是否含有动态区域关键字，且按照条件编写顺序返回引用的关键字pk
	 * @param expr
	 * @param dynKeyDataGroup
	 * @return
	 */
//	private String[] getDynKeyFromExpr(UfoExpr expr,ArrayList listDynKeyPKs,String strMainTimeKey,ReportDynCalcEnv objEnv){
//		if(expr==null  )
//			return null;
//		return MeasCondExprUtil.getDynKeyFromExpr(expr,listDynKeyPKs,strMainTimeKey);
//	}
	
	private boolean isRefDyn(UfoExpr expr,ArrayList listDynKeyPKs,String strMainTimeKey,ReportDynCalcEnv objEnv){
		if(expr==null){
			return false;
		}
		String[] strs=MeasCondExprUtil.getDynKeyAndAreasFromExpr(expr,listDynKeyPKs,strMainTimeKey,objEnv);
		if(strs==null || strs.length<=0){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 先从getValue()中查找，没有再从数据库取值
	 * 创建日期：(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	protected UfoVal[] calcDynStatValue(ReportDynCalcEnv objEnv,UfoExpr objCond) throws CmdException {
		if (getValue() != null) {
			return getValue();
		}
		if (objEnv == null) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}

		//记录环境中的原有信息
		String strEnvDynAreaPK = objEnv.getDynArea();
		KeyDataGroup objKeyValueInEnv =objEnv.getKeyDatas();//动态区域的关键字值
		Object objExZKeyValue = objEnv.getExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);

		try {
			objEnv.removeExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
//			java.util.ArrayList alPara = getParams();
			IStoreCell[] objMeasures = getMeasures(objEnv);

//			if (objMeasures == null
//					|| objEnv == null
//					|| objEnv.getDataChannel()==null) {
//				return new UfoVal[] { UfoNullVal.getSingleton()};
//			}
			if (objMeasures == null
					|| objEnv == null) {
				return new UfoVal[] { UfoNullVal.getSingleton()};
			}
			
//			com.ufsoft.iuforeport.reporttool.data.UfoTable objTable =
//			objEnv.getReportEnv().m_dataChannel.getTable();
//			String strDynAreaPK = objTable.getDynPKByMeasurePK(getMeasures()[0].getCode());


//			String strDynAreaPK = objEnv.getDynPKByMeasurePK(objMeasures[0].getCode());

			String strMainTimeKey=null;
			KeyGroupVO mainKeyGroupVO=objEnv.getKeyGroupVOInMain();
			if(mainKeyGroupVO!=null ){
				nc.vo.iufo.keydef.KeyVO mainTimeKeyVO=mainKeyGroupVO.getTimeKey();
				if(mainTimeKeyVO!=null)
					strMainTimeKey=mainTimeKeyVO.getPk_keyword();
			}

			//获得动态区域关键字pk集合
			ArrayList listDynKeyPKs= objKeyValueInEnv==null?null:getDynKeysFromKeyDatas(objKeyValueInEnv);

			//1.1判断objCond是否含有zkey()形式的动态区域关键字函数
			String[] strZkeyRefDyns=MeasCondExprUtil.getDynKeyFromExpr(objCond,listDynKeyPKs,strMainTimeKey);
			boolean isRefDyn=isRefDyn(objCond,listDynKeyPKs,strMainTimeKey,objEnv);
			
			//获得当前计算函数的数据集合
			UfoVal[] objVals = null;
			if(getFuncName().equalsIgnoreCase(MeasFuncDriver.MSUMA)){
				objVals = calcMeasureMSumaAggrDatas(objEnv, objCond, objKeyValueInEnv, objMeasures, strMainTimeKey, listDynKeyPKs, strZkeyRefDyns);
			} else{
				objVals = calcMeasureAggrDatas(objEnv, objCond, objKeyValueInEnv, objMeasures, strMainTimeKey, listDynKeyPKs, strZkeyRefDyns);
			}
			
			//处理计算结果是否放入函数属性中，以便此公式在动态区下一行计算时直接利用此结果。
			if(!isRefDyn){
				setValue(objVals);//note by ljhua 2005-2-24 解决动态区域内msum等公式取数错误问题.
			}
				

			return objVals;
//			if (objMeasures[0].getDbcolumn() != null){
//				if(objKeyValueInEnv==null || strZkeyRefDyns==null ){
//					//主表内公式或者条件中未引用动态区域关键字时，则从数据库中取所有符合条件的值
//					hashDataInDB= readAggrDatasFromDB(objEnv);
//				}else{
//					//动态区域内公式
//					//获得zkey(动态区关键字)对应的k()函数关键字集合
//					String[] strMeasKeyRefDyns=getKeyRefDynFromExpr(objCond,objKeyValueInEnv,strMainTimeKey);
//
//					//获得动态区域各行关键字值
//					KeyDataGroup[] dynaKeyDatas=(KeyDataGroup[]) objEnv.getDynAllKeyDatas();
//					if ( m_hashDbData==null ) { 
//						//批量读取指定行数的数据
//						m_hashDbData= batchReadAggrFromDB(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
//					}else {
//						Vector vecCacheKey=(Vector) m_hashDbData.get(CACHE_KEY);
//						//获得当前计算动态区域行中，条件中动态区域关键字的值串
//						String strExprDynKeyValue=getKeyValueString(objKeyValueInEnv,strZkeyRefDyns);
//						if(vecCacheKey==null || !vecCacheKey.contains(strExprDynKeyValue)){
//							m_hashDbData=batchReadAggrFromDB(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
//						}
//					}
//					//按照当前行从m_hashDbData中筛选数据
//					hashDataInDB = getDynCurStatFromCache(objCond,objMeasures[0],objEnv,strDynAreaPK,objKeyValueInEnv);
////					hashDataInDB=readAggrDatasFromDB(objEnv);
//				}
//			}
//
//			if (hashDataInDB == null) {
//				hashDataInDB = new Hashtable();
//			}
//
//
//			ArrayList listKeyValues=new ArrayList(); //当前表页符合统计函数条件的关键字值结合
//
//
//			if(isMeasReferDynArea(objEnv)){
//				if(m_hashDynMeasDataInRep==null){
//					//获得指标在动态区域内的值
//					m_hashDynMeasDataInRep =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,objMeasures[0].getCode());   
////					m_hashDynMeasDataInRep=objTable.getDynAreaMeasureValues(objMeasures[0].getCode());
//				}
//			}
//
//			/**
//			 * 优化动态区指标msum函数计算.2005-9-14 modify by ljhua
//			 * 优化目标：减少计算行数，减少设置objEnv.setDynAreaInfo(strDynAreaPK, objKeyDatas)数量.
//			 * 优化条件：只有当条件中没有or操作符，且动态区关键字条件为=时才能使用此优化方案。
//			 * 说明：当条件中动态区条件已写全时，能够判断唯一确定一行。
//			 * 
//			 */
//			if(m_hashDynMeasDataInRep!=null && m_hashDynMeasDataInRep.size()>0){
//				if(objKeyValueInEnv==null){
//					//当前计算的是主表公式
//					com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup tempKeyDataGroup= 
//						(com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup) m_hashDynMeasDataInRep.keySet().iterator().next();
//					listDynKeyPKs= getDynKeysFromKeyDatas(tempKeyDataGroup);
//
//				}
//
//				//2.添加并替换报表数据
//				objEnv.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, objKeyValueInEnv);
//
//				//获得可比较的条件中动态区关键字值
//				Map mapCompareKeyValue=null;
//				boolean  bOnlyOneRow=false;//标识是否只有一行满足条件
////				KeyDataGroup findOnlyRowData=null;//唯一行的关键字数据
//				//获得动态区时间关键字
//				String strDynTimeKey=DatePropVO.getTimeKey(listDynKeyPKs);
//
//				if(objCond!=null && listDynKeyPKs!=null && listDynKeyPKs.size()>0){
//					//判断是否有or操作符
//					boolean bHaveOrOpera=MeasCondExprUtil.isHaveOrOpera(objCond);
//					if(bHaveOrOpera==false){
//						mapCompareKeyValue=MeasCondExprUtil.getRefDynKeyValues(objCond,listDynKeyPKs,strMainTimeKey,objEnv);
//					}
//
//					//如果条件中所有的动态区关键字值都写全，则最多有一行数据满足条件.
//					if(mapCompareKeyValue!=null && mapCompareKeyValue.size()>0){
//						if(mapCompareKeyValue.size()==listDynKeyPKs.size()){
//							if(strDynTimeKey==null || MeasCondExprUtil.isCompletedTimeCond(strDynTimeKey,(Map) mapCompareKeyValue.get(strDynTimeKey))){
//								bOnlyOneRow=true;	            		
////								findOnlyRowData=MeasCondExprUtil.findCondRow(objKeyValueInEnv,mapCompareKeyValue);
//							}
//						}
//					}
//				}
//
//				boolean bAllRow=false;//标识是否所有行都满足条件.只有在主表公式内容为本表动态区指标的msum时才使用此标识.
//				boolean bFindOnlyRow=false;//标识是否找到惟一行
//				String strKey =null;
//
//				Enumeration enKey = m_hashDynMeasDataInRep.keys();
//				while (enKey.hasMoreElements()) {
//					com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup objKeyDatas =
//						(com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup) enKey.nextElement();
//
//					if(bOnlyOneRow==true){
//						if(bFindOnlyRow==true || MeasCondExprUtil.compareDynCurRow(objKeyDatas,mapCompareKeyValue,strDynTimeKey,true)==false)
//							continue;
//						else
//							bFindOnlyRow=true;
//
//					}
//					else if(mapCompareKeyValue!=null && mapCompareKeyValue.size()>0){
//						if( MeasCondExprUtil.compareDynCurRow(objKeyDatas,mapCompareKeyValue,strDynTimeKey,false)==false)
//							continue;
//					}
//
//					objEnv.setDynAreaInfo(strDynAreaPK, objKeyDatas);
//
//					MeasurePubDataVO  objMeasKeyData = objEnv.getMeasureEnv();
//
//					//计算当前计算的关键字值字串strKey
//					strKey = getKeyString(objMeasKeyData,objKeyDatas);          
//
//					listKeyValues.add(strKey);
//
//					boolean bPut=false;
//					if(objCond == null){
//						bPut=true;
//					}else if((listDynKeyPKs==null || listDynKeyPKs.size()==0) && bAllRow==true){
//						//当计算的是主表公式且已计算过某行满足此条件，则此行也满足条件。
//						bPut=true;
//					}else if(objCond.calcExpr(objEnv)[0].doubleValue() == 1){
//						//标识当计算的是主表公式时，动态区所有行都满足条件
//						if(listDynKeyPKs==null || listDynKeyPKs.size()==0)
//							bAllRow=true;
//
//						bPut=true;
//					}
//					if(bPut){
//						hashDataInDB.put(
//								strKey,
//								new Double(
//										((nc.vo.iufo.data.MeasureDataVO) m_hashDynMeasDataInRep.get(objKeyDatas))
//										.getDoubleValue()));
//					}
//
//				}      
//
//			}
//
//			//3.处理在录入状态已删除动态区域某些行但未保存时,去除在hashDataInDB存在的多余数据 add by ljhua 2005-2-24
//			if( isMeasReferDynArea(objEnv))
//				removeStateValue(objKeyValueInEnv,objEnv,strMainTimeKey,listKeyValues,hashDataInDB);
//
//			//4.计算
//			double nVal = 0;
//			int nCount = 0;
//			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
//			if( nFID == MeasFuncDriver.FMMIN ){
//				nVal = Double.MAX_VALUE;
//			}else if( nFID == MeasFuncDriver.FMMAX){
//				nVal = Double.MIN_VALUE;
//			}
//
//			Enumeration enKey1 = hashDataInDB.keys();
//			while (enKey1.hasMoreElements()) {
//				nCount++;
//				double nCurVal = 0;
//				nCurVal = ((Double) hashDataInDB.get(enKey1.nextElement())).doubleValue();
//
//				switch (nFID) {
//				case MeasFuncDriver.FMSUM :
//				case MeasFuncDriver.FMSUMA :
//				case MeasFuncDriver.FMAVG :
//					nVal += nCurVal;
//					break;
//				case MeasFuncDriver.FMMIN :
//					if (nCurVal < nVal) {
//						nVal = nCurVal;
//					}
//					break;
//				case MeasFuncDriver.FMMAX :
//					if (nCurVal > nVal) {
//						nVal = nCurVal;
//					}
//					break;
//				}
//			}
//			if (nFID == MeasFuncDriver.FMAVG) {
//				if (nCount == 0) {
//					nVal = 0;
//				} else {
//					nVal /= nCount;
//				}
//			}else if(nFID == MeasFuncDriver.FMMIN){
//				if(nVal==Double.MAX_VALUE)
//					nVal=0;
//			}
//
//			UfoVal[] objVals = new UfoVal[] { UfoDouble.getInstance(nVal)};
//
//			//5.处理计算结果是否放入函数属性中，以便此公式在动态区下一行计算时直接利用此结果。
//			if(strZkeyRefDyns==null || strZkeyRefDyns.length==0)
//				setValue(objVals);//note by ljhua 2005-2-24 解决动态区域内msum等公式取数错误问题.
//
//
//				return objVals;

		} catch(ScriptException e){
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		} catch (Exception e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		} finally {
			if (objExZKeyValue != null) {
				objEnv.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, objExZKeyValue);
			} else {
				objEnv.removeExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
			}
			objEnv.setDynAreaInfo(strEnvDynAreaPK, objKeyValueInEnv);
		}
	}

	/**
	 * 计算指标统计函数数据集[除MSUMA函数]
	 * 
	 * @param objEnv
	 * @param objCond
	 * @param objKeyValueInEnv
	 * @param objMeasures
	 * @param strMainTimeKey
	 * @param listDynKeyPKs
	 * @param strZkeyRefDyns
	 * @return
	 * @throws CmdException
	 * @throws UfoValueException
	 */
	private UfoVal[] calcMeasureAggrDatas(ReportDynCalcEnv objEnv, UfoExpr objCond, KeyDataGroup objKeyValueInEnv, IStoreCell[] objMeasures, String strMainTimeKey, ArrayList listDynKeyPKs, String[] strZkeyRefDyns) throws CmdException, UfoValueException {
		UfoVal[] objVals;
		Hashtable hashDataInDB = null;
		if(objKeyValueInEnv==null || strZkeyRefDyns==null){
			//主表内公式或者条件中未引用动态区域关键字时，则从数据库中取所有符合条件的值
			hashDataInDB= readAggrDatasFromDB(objEnv);
		}else{
			//动态区域内公式
			//获得zkey(动态区关键字)对应的k()函数关键字集合
			String[] strMeasKeyRefDyns = getKeyRefDynFromExpr(objCond,objKeyValueInEnv,strMainTimeKey);

			//获得动态区域各行关键字值
			KeyDataGroup[] dynaKeyDatas=(KeyDataGroup[]) objEnv.getDynAllKeyDatas();
			if ( m_hashDbData==null ) { 
				//批量读取指定行数的数据
				m_hashDbData= batchReadAggrFromDB(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
			}else {
				Vector vecCacheKey=(Vector) m_hashDbData.get(CACHE_KEY);
				//获得当前计算动态区域行中，条件中动态区域关键字的值串
				String strExprDynKeyValue=getKeyValueString(objKeyValueInEnv,strZkeyRefDyns);
				if(vecCacheKey==null || !vecCacheKey.contains(strExprDynKeyValue)){
					m_hashDbData=batchReadAggrFromDB(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
				}
			}
			//按照当前行从m_hashDbData中筛选数据
			String strDynAreaPK = objEnv.getDynPKByMeasurePK(objMeasures[0].getCode());
			hashDataInDB = getDynCurStatFromCache(objCond,objMeasures[0],objEnv,strDynAreaPK,objKeyValueInEnv);
		}
			
		hashDataInDB = calcMeasureAggrDatas(objEnv, objCond, objMeasures[0], hashDataInDB, objKeyValueInEnv, 
				listDynKeyPKs, strZkeyRefDyns, strMainTimeKey);
		
		UfoVal objVal = null;
		// 判断计算过程是否用大数值类型处理
		boolean isBigNumber = isBigNumber(objEnv);
		if(objMeasures[0].getType() == IStoreCell.TYPE_BIGDECIMAL || isBigNumber) {
			// 如果需要大数值类型的处理,则用UFDouble类型进行计算
			objVal = calcDynStatValueByUFDouble(hashDataInDB);
		} else {
			objVal = calcDynStatValue(hashDataInDB);
		}
//		UfoVal objVal = calcDynStatValue(hashDataInDB);
		
		
		objVals = new UfoVal[] {objVal};
		return objVals;
	}


	/**
	 * 是否大数值类型处理
	 * 
	 * @create by liuchuna at 2011-10-25,上午09:59:28
	 *
	 * @param objEnv
	 * @return
	 */
	protected boolean isBigNumber(UfoCalcEnv objEnv) {
		boolean isBigNumber = false;
		if(objEnv != null) {
			Object isBigNum = objEnv.getExEnv(CommonExprCalcEnv.EX_IS_BIGNUMBER);
			if(isBigNum != null && isBigNum instanceof Boolean) {
				isBigNumber = (Boolean)isBigNum;
			}
		}
		return isBigNumber;
	}

	/**
	 * 计算指标函数MSUMA数据集，MSUMA函数应该返回多值
	 * 
	 * @param objEnv
	 * @param objCond
	 * @param objKeyValueInEnv
	 * @param objMeasures
	 * @param strMainTimeKey
	 * @param listDynKeyPKs
	 * @param strZkeyRefDyns
	 * @return
	 * @throws CmdException
	 * @throws UfoValueException
	 */
	private UfoVal[] calcMeasureMSumaAggrDatas(ReportDynCalcEnv objEnv, UfoExpr objCond, KeyDataGroup objKeyValueInEnv, IStoreCell[] objMeasures, String strMainTimeKey, ArrayList listDynKeyPKs, String[] strZkeyRefDyns) throws CmdException, UfoValueException {
		UfoVal[] objVals;
		Hashtable hashMeas2DataInDB;
		//计算MSUMA函数，需要对统计区域每个指标进行计算
		objVals = new UfoVal[objMeasures.length];
		if(objKeyValueInEnv == null || strZkeyRefDyns == null ){
			//主表内公式或者条件中未引用动态区域关键字时，则从数据库中取所有符合条件的值
			hashMeas2DataInDB = readAggrDatasFromDB1(objEnv);
		}else{
			//动态区域内公式
			//获得zkey(动态区关键字)对应的k()函数关键字集合
			String[] strMeasKeyRefDyns = getKeyRefDynFromExpr(objCond, objKeyValueInEnv, strMainTimeKey);

			//获得动态区域各行关键字值
			KeyDataGroup[] dynaKeyDatas = (KeyDataGroup[]) objEnv.getDynAllKeyDatas();
			if ( m_hashDbData==null ) { 
				//批量读取指定行数的数据
				m_hashDbData= batchReadAggrFromDB1(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
			}else {
				Vector vecCacheKey=(Vector) m_hashDbData.get(CACHE_KEY);
				//获得当前计算动态区域行中，条件中动态区域关键字的值串
				String strExprDynKeyValue = getKeyValueString(objKeyValueInEnv,strZkeyRefDyns);
				if(vecCacheKey==null || !vecCacheKey.contains(strExprDynKeyValue)){
					m_hashDbData = batchReadAggrFromDB1(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
				}
			}
			//按照当前行从m_hashDbData中筛选数据
			hashMeas2DataInDB = getDynCurStatFromCache1(objCond, objEnv, objKeyValueInEnv);
			//hashDataInDB=readAggrDatasFromDB(objEnv);
		}
		
		//对指标数据进行修正，包括报表动态区数据被修改或定义筛选的数据处理
		for(int i = 0; i < objMeasures.length; i++){
			IStoreCell measureVO = objMeasures[i];
			Hashtable hashDataInDB = (Hashtable)hashMeas2DataInDB.get(measureVO);
			hashDataInDB = calcMeasureAggrDatas(objEnv, objCond, objMeasures[i], hashDataInDB, objKeyValueInEnv, 
					listDynKeyPKs, strZkeyRefDyns, strMainTimeKey);
			
			boolean isBigNumber = isBigNumber(objEnv);
			if(measureVO.getType() == IStoreCell.TYPE_BIGDECIMAL || isBigNumber) {
				// 如果需要大数值类型的处理,则用UFDouble类型进行计算
				objVals[i] = calcDynStatValueByUFDouble(hashDataInDB);
			} else {
				objVals[i] = calcDynStatValue(hashDataInDB);
			}
		}
		return objVals;
	}

	/**
	 * 计算指标统计函数的值
	 * 
	 * @param objEnv
	 * @param objCond
	 * @param objMeasure
	 * @param objKeyValueInEnv
	 * @param listDynKeyPKs
	 * @param strZkeyRefDyns
	 * @param strMainTimeKey
	 * @return
	 * @throws CmdException
	 * @throws UfoValueException
	 */
	private Hashtable calcMeasureAggrDatas(ReportDynCalcEnv objEnv, UfoExpr objCond, IStoreCell objMeasure, Hashtable hashDataInDB, KeyDataGroup objKeyValueInEnv, 
			ArrayList listDynKeyPKs, String[] strZkeyRefDyns, String strMainTimeKey) throws CmdException, UfoValueException{
		if(hashDataInDB == null) {
			hashDataInDB = new Hashtable();
		}
		
		//chxw. 处理指标公式追踪
		Vector<MeasureTraceVO> tracevos = new Vector<MeasureTraceVO>();
		MeasureTraceVO[] arrtracevo = objEnv.getMeasureTraceVOs();
		if(objEnv.isMeasureTrace() && arrtracevo != null){
			for(int i = 0; i < arrtracevo.length; i++){
				tracevos.add(arrtracevo[i]);
			}				
		}
		
		//当前表页符合统计函数条件的关键字值结合
		ArrayList listKeyValues = new ArrayList(); 
		String strDynAreaPK = null;
		if(objMeasure instanceof MeasureVO) {
			strDynAreaPK = objEnv.getDynPKByMeasurePK(objMeasure.getCode());
		} else {
			strDynAreaPK = objEnv.getDynPKByStoreCellPos(CellPosition.getInstance(objMeasure.getCode()));
		}
		
		if(isReferDynArea(objMeasure, objEnv)){
//			if(m_hashDynMeasDataInRep == null){
			//获得指标在动态区域内的值
			if(objMeasure instanceof MeasureVO) {
				m_hashDynMeasDataInRep = strDynAreaPK==null?null:objEnv.getUfoDataChannel().
						getDatasByMeta(strDynAreaPK, objMeasure);
			} else {
//				getUfoDataChannel().getMainDataByMeta(pos, IUFOTableData.STORECELL);
				m_hashDynMeasDataInRep = strDynAreaPK==null?null:objEnv.getUfoDataChannel().
						getDatasByMeta(strDynAreaPK, objMeasure);
			}
			//m_hashDynMeasDataInRep=objTable.getDynAreaMeasureValues(objMeasures[0].getCode());
//			}
		}

		/**
		 * 优化动态区指标msum函数计算.2005-9-14 modify by ljhua
		 * 优化目标：减少计算行数，减少设置objEnv.setDynAreaInfo(strDynAreaPK, objKeyDatas)数量.
		 * 优化条件：只有当条件中没有or操作符，且动态区关键字条件为=时才能使用此优化方案。
		 * 说明：当条件中动态区条件已写全时，能够判断唯一确定一行。
		 * 
		 */
		if(m_hashDynMeasDataInRep != null && m_hashDynMeasDataInRep.size()>0){
			KeyDataGroup tempKeyDataGroup=null;
			Iterator keyItes = m_hashDynMeasDataInRep.keySet().iterator();
			if(objKeyValueInEnv == null){
				//当前计算的是主表公式
				if(keyItes.hasNext()){
					tempKeyDataGroup= 
						(KeyDataGroup) keyItes.next();
					listDynKeyPKs = getDynKeysFromKeyDatas(tempKeyDataGroup);
				}
			}

			//2.添加并替换报表数据
			objEnv.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, objKeyValueInEnv);

			//获得可比较的条件中动态区关键字值
			Map mapCompareKeyValue=null;
//			boolean  bOnlyOneRow=false;//标识是否只有一行满足条件
//			KeyDataGroup findOnlyRowData=null;//唯一行的关键字数据
			//获得动态区时间关键字
			String strDynTimeKey = DatePropVO.getTimeKey(listDynKeyPKs);

			if(objCond!=null && listDynKeyPKs!=null && listDynKeyPKs.size()>0){
				//判断是否有or操作符
				boolean bHaveOrOpera=MeasCondExprUtil.isHaveOrOpera(objCond);
				if(bHaveOrOpera==false){
					mapCompareKeyValue = MeasCondExprUtil.getRefDynKeyValues(objCond,listDynKeyPKs,strMainTimeKey,objEnv);
				}

				//如果条件中所有的动态区关键字值都写全，则最多有一行数据满足条件.
				if(mapCompareKeyValue!=null && mapCompareKeyValue.size()>0){
					if(mapCompareKeyValue.size()==listDynKeyPKs.size()){
						if(strDynTimeKey==null || MeasCondExprUtil.isCompletedTimeCond(strDynTimeKey,(Map) mapCompareKeyValue.get(strDynTimeKey))){
//							bOnlyOneRow=true;	            		
//							findOnlyRowData=MeasCondExprUtil.findCondRow(objKeyValueInEnv,mapCompareKeyValue);
						}
					}
				}
			}
			//tianchuan 注start  下面这个bOnlyMainKeyCond的取法应该是有问题的
			//不过我在最后bPut的判断做了些修改，使它应该能屏蔽这个问题
			//先不对bOnlyMainKeyCond做修改了
        	boolean bOnlyMainKeyCond=false;
        	if (objCond!=null){
        		bOnlyMainKeyCond=MeasCondExprUtil.isOnlyMainKeyCondEqual(objCond, objEnv);
        	}			
        	//注end
        	
			String strKey =null;
			boolean bAllRow = false;//标识是否所有行都满足条件.只有在主表公式内容为本表动态区指标的msum时才使用此标识.
//			boolean bFindOnlyRow = false;//标识是否找到惟一行
			
			Enumeration enKey = m_hashDynMeasDataInRep.keys();
			while (enKey.hasMoreElements()) {
				KeyDataGroup objKeyDatas = (KeyDataGroup) enKey.nextElement();

//				if(bOnlyOneRow==true){
//					if(bFindOnlyRow==true || MeasCondExprUtil.compareDynCurRow(objKeyDatas,mapCompareKeyValue,strDynTimeKey,true)==false)
//						continue;
//					else
//						bFindOnlyRow=true;
//
//				} else if(bOnlyMainKeyCond==false && mapCompareKeyValue!=null && mapCompareKeyValue.size()>0){
//					if( MeasCondExprUtil.compareDynCurRow(objKeyDatas,mapCompareKeyValue,strDynTimeKey,false)==false)
//						continue;
//				}

				objEnv.setDynAreaInfo(strDynAreaPK, objKeyDatas);

				//计算当前计算的关键字值字串strKey
				MeasurePubDataVO  objMeasKeyData = objEnv.getMeasureEnv();
				strKey = getKeyString(objMeasKeyData,objKeyDatas);          
				listKeyValues.add(strKey);
				
				int m = -1;
				
				Object dynKeyDataIndex = objEnv.getExEnv(UfoCalcEnv.KEY_DYN_KEYDATAGROUP_INDEX);
				if(dynKeyDataIndex == null) {
					dynKeyDataIndex = new Hashtable<String, Hashtable<KeyDataGroup,Integer>>();
					objEnv.setExEnv(UfoCalcEnv.KEY_DYN_KEYDATAGROUP_INDEX, dynKeyDataIndex);
				}
				Hashtable<KeyDataGroup,Integer> keyDataIndexMap = ((Hashtable<String, Hashtable<KeyDataGroup,Integer>>)dynKeyDataIndex).get(strDynAreaPK);
				if(keyDataIndexMap == null) {
					keyDataIndexMap = new Hashtable<KeyDataGroup,Integer>();
					((Hashtable<String, Hashtable<KeyDataGroup,Integer>>)dynKeyDataIndex).put(strDynAreaPK, keyDataIndexMap);
					
					m = DynAreaUtil.getOwnerUnitAreaNumByKeyData(objKeyDatas, strDynAreaPK, 
							((AbsRepDataChannel)objEnv.getDataChannel()).getDataModel());
					
					keyDataIndexMap.put(objKeyDatas, m);
				} else {
					Integer index = keyDataIndexMap.get(objKeyDatas);
					if(index == null) {
						m = DynAreaUtil.getOwnerUnitAreaNumByKeyData(objKeyDatas, strDynAreaPK, 
								((AbsRepDataChannel)objEnv.getDataChannel()).getDataModel());
						
						keyDataIndexMap.put(objKeyDatas, m);
					} else {
						m = index.intValue();
					}
				}
				
				// 根据动态区关键字获得组号，将动态区pk及组号作为计算参数
//				m = DynAreaUtil.getOwnerUnitAreaNumByKeyData(objKeyDatas, strDynAreaPK, 
//						((AbsRepDataChannel)objEnv.getDataChannel()).getDataModel());
				if(m == -1){
					continue;
				}
				((AbsRepDataChannel)objEnv.getDataChannel()).setDynAreaCalcParam(new IUFODynAreaDataParam(m, null, strDynAreaPK));
                
				boolean bPut=false;
				if(bOnlyMainKeyCond==true || objCond == null){
					bPut=true;
				}
//				else if((listDynKeyPKs==null || listDynKeyPKs.size()==0) && bAllRow==true){
//					//当计算的是主表公式且已计算过某行满足此条件，则此行也满足条件。
//					bPut=true;
//				}
				else if(mapCompareKeyValue!=null){	//这里判断动态区关键字是否正确
					KeyDataVO[] keyDatas=objKeyDatas.getKeyDatas();
					String compareStr=null;
					if(keyDatas!=null){
						bPut=true;
						Object val=null;
						for(int i=0;i<keyDatas.length;i++){
							val=mapCompareKeyValue.get(keyDatas[i].getKey().getPk_keyword());
							if(val!=null){
								if(keyDatas[i].getKey().getRef_pk()!=null){
									compareStr=keyDatas[i].getKeyData().getCode();
								}else{
									compareStr=keyDatas[i].getValue();
								}
								if(!val.toString().equals(compareStr)){
									bPut=false;
								}
							}
						}
					}
				}else{
					bPut=true;
				}
				if(bPut && objCond!=null){	//这里相当于判断主表关键字是否正确
					bPut=false;
					if(objCond.calcExpr(objEnv)[0].doubleValue() == 1){
						//标识当计算的是主表公式时，动态区所有行都满足条件
						if(listDynKeyPKs==null || listDynKeyPKs.size()==0){
							bAllRow=true;
						}
						bPut=true;
					}
				}
				
				if(bPut){
					MeasureDataVO mdvo = ((nc.vo.iufo.data.MeasureDataVO) m_hashDynMeasDataInRep.get(objKeyDatas));
					if(mdvo.getMeasureVO().getType() == MeasureVO.TYPE_NUMBER) {
						hashDataInDB.put(strKey,mdvo.getUFDoubleValue().doubleValue());
					} else if(mdvo.getMeasureVO().getType() == MeasureVO.TYPE_BIGDECIMAL) {
						hashDataInDB.put(strKey,mdvo.getUFDoubleValue());
					}
				}
			}      
		}
        
		
		//3.处理在录入状态已删除动态区域某些行但未保存时,去除在hashDataInDB存在的多余数据 add by ljhua 2005-2-24
		if(isMeasReferDynArea(objEnv))
			removeStateValue(objKeyValueInEnv,objEnv,strMainTimeKey,listKeyValues,hashDataInDB);
		
		//chxw. 处理指标公式追踪
		if(objEnv.isMeasureTrace()){
			objEnv.setMeasureTraceVOs(tracevos.toArray(new MeasureTraceVO[0]));
		}
		if(objEnv.getDataChannel()!=null){
			// liuchun 20110610 修改，清除动态区计算参数
			((AbsRepDataChannel)objEnv.getDataChannel()).removeDynAreaCalcParam();
		}
		
		
		return hashDataInDB;
	}

	/**
	 * 检查tracevos数据库中是否已保存该关键字下指标数据
	 * @param tracevos
	 * @param strKey
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean isExistMeasKeyData(Vector<MeasureTraceVO> tracevos, String strKey) {
		boolean existMeasKeyData = false;
		for(MeasureTraceVO tracevo:tracevos){
			IKeyDetailData[] arrKeyvalues = tracevo.getKeyvalues();
			StringBuffer strKeyvalues = new StringBuffer();
			for(IKeyDetailData data:arrKeyvalues){
				if (data != null && data.getValue()!=null && data.getValue().length() > 0) {
					strKeyvalues.append(data.getValue());
					strKeyvalues.append("\r\n");
				}
			}
			if(strKeyvalues.toString().equals(strKey)){
				existMeasKeyData = true;
				break;
			}
		}
		return existMeasKeyData;
	}

	/**
	 * 计算动态区统计函数返回值
	 * @param hashDataInDB
	 * @return
	 * @throws CmdException
	 */
	protected UfoVal calcDynStatValue(Hashtable hashDataInDB) throws CmdException {
		if(hashDataInDB == null){
			return UfoDouble.getInstance(0);
		}
		
		double nVal = 0;
		try {
			int nCount = 0;
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			if( nFID == MeasFuncDriver.FMMIN ){
				nVal = Double.MAX_VALUE;
			}else if( nFID == MeasFuncDriver.FMMAX){
				nVal = Double.MIN_VALUE;
			}

			Enumeration enKey = hashDataInDB.keys();
			while (enKey.hasMoreElements()) {
				nCount++;
				double nCurVal = 0;
				Object element = enKey.nextElement();
				if(!(hashDataInDB.get(element) instanceof Double)) {
					continue;
				}
				nCurVal = ((Double) hashDataInDB.get(element)).doubleValue();
				switch (nFID) {
				case MeasFuncDriver.FMSUM :
				case MeasFuncDriver.FMSUMA :
				case MeasFuncDriver.FMAVG :
					nVal += nCurVal;
					break;
				case MeasFuncDriver.FMMIN :
					if (nCurVal < nVal) {
						nVal = nCurVal;
					}
					break;
				case MeasFuncDriver.FMMAX :
					if (nCurVal > nVal) {
						nVal = nCurVal;
					}
					break;
				}
			}
			if (nFID == MeasFuncDriver.FMAVG) {
				if (nCount == 0) {
					nVal = 0;
				} else {
					nVal /= nCount;
				}
			}else if(nFID == MeasFuncDriver.FMMIN){
				if(nVal==Double.MAX_VALUE)
					nVal=0;
			}
		} catch (UfoParseException e) {
			AppDebug.debug(e);
			throw new UfoCmdException(e);
		}
		return UfoDouble.getInstance(nVal);
		
	}
	
	//tianchuan 访问类型改为protected
	protected UfoVal calcDynStatValueByUFDouble(Hashtable hashDataInDB) throws CmdException {
		if(hashDataInDB == null){
			return UfoDouble.getInstance(0);
		}
		
		UFDouble nVal = new UFDouble(0);
		UFDouble maxDoule = new UFDouble(Double.MAX_VALUE);
		UFDouble minDoule = new UFDouble(Double.MIN_VALUE);
		try {
			int nCount = 0;
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			if( nFID == MeasFuncDriver.FMMIN ){
				nVal = maxDoule;
			}else if( nFID == MeasFuncDriver.FMMAX){
				nVal = minDoule;
			}

			Enumeration enKey = hashDataInDB.keys();
			while (enKey.hasMoreElements()) {
				nCount++;
				UFDouble nCurVal = new UFDouble(0);
				Object element = enKey.nextElement();
				if(hashDataInDB.get(element) instanceof Double) {
					nCurVal = new UFDouble((Double) hashDataInDB.get(element));
				} else if (hashDataInDB.get(element) instanceof UFDouble) {
					nCurVal = ((UFDouble) hashDataInDB.get(element));
				} else {
					continue;
				}
				
				switch (nFID) {
				case MeasFuncDriver.FMSUM :
				case MeasFuncDriver.FMSUMA :
				case MeasFuncDriver.FMAVG :
					nVal = nVal.add(nCurVal);
					break;
				case MeasFuncDriver.FMMIN :
					if (nCurVal.compareTo(nVal) < 0) {
						nVal = nCurVal;
					}
					break;
				case MeasFuncDriver.FMMAX :
					if (nCurVal.compareTo(nVal) > 0) {
						nVal = nCurVal;
					}
					break;
				}
			}
			if (nFID == MeasFuncDriver.FMAVG) {
				if (nCount == 0) {
					nVal = new UFDouble(0);
				} else {
//					nVal /= nCount;
					nVal = nVal.div(nCount);
				}
			}else if(nFID == MeasFuncDriver.FMMIN){
				if(nVal.equals(maxDoule))
					nVal=new UFDouble(0);
			}
		} catch (UfoParseException e) {
			AppDebug.debug(e);
			throw new UfoCmdException(e);
		}
//		return UfoDouble.getInstance(nVal);
		return UfoDecimal.getInstance(nVal.toBigDecimal());
	}

	
	private String getKeyString(MeasurePubDataVO  objMeasKeyData,KeyDataGroup objKeyDatas)throws  UfoCmdException{
		String strKey = "";

		if (objMeasKeyData == null) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		nc.vo.iufo.keydef.KeyGroupVO objKG = objMeasKeyData.getKeyGroup();
		if (objKG == null) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		nc.vo.iufo.keydef.KeyVO[] objKeyVOs = objKG.getKeys();
		if (objKeyVOs != null) {
			for (int i = 0; i < objKeyVOs.length; i++) {
				String strValue = objMeasKeyData.getKeywordByPK(objKeyVOs[i].getPk_keyword());
				if (strValue != null && strValue.length() > 0) {
					strKey += strValue;
					strKey += "\r\n";

				}
			}
		}

		return strKey;
	}

	/**
	 * 删除hashDataInDB中在录入状态已删除但未保存的动态区域行记录
	 * @param objKeyValueInEnv
	 * @param objEnv
	 * @param strMainTimeKey
	 * @param listKeyValues
	 * @param hashDataInDB
	 */
	private void removeStateValue(KeyDataGroup objKeyValueInEnv ,ReportDynCalcEnv objEnv,String strMainTimeKey,ArrayList listKeyValues,Hashtable hashDataInDB )throws CmdException{
		//处理在录入状态已删除动态区域某些行但未保存时,去除在hashDataInDB存在的多余数据 add by ljhua 2005-2-24


		//1.获得指标对应的关键字集合
		IStoreCell[] objMeasures = getMeasures(objEnv);
		KeyGroupVO keyGroupVO=null;
		try {
			keyGroupVO = getMeaureKeyGroupVO(objMeasures[0],objEnv);
		} catch (UfoCmdException e) {
			AppDebug.debug(e);
		}
		nc.vo.iufo.keydef.KeyVO[] measureKeyVOs=null;
		if(keyGroupVO!=null)
			measureKeyVOs=keyGroupVO.getKeys();

		//动态区指标不可能没有关键字
		if(measureKeyVOs==null || measureKeyVOs.length==0)
			return;

		//2.获得主表关键字
		KeyGroupVO mainKeyGroupVO=objEnv.getKeyGroupVOInMain();
		nc.vo.iufo.keydef.KeyVO[] mainKeys=mainKeyGroupVO.getKeys();

		//3.获得动态区的时间关键字
		String strDynTimeKey=null;
		String strTimeKey=getTimeKey(measureKeyVOs);
		if(strMainTimeKey==null || !strMainTimeKey.equals(strTimeKey))
			strDynTimeKey=strTimeKey;

		//4.当前主表的关键字值
		IKeyDetailData[] keyValuesInMain=objEnv.getKeyValuesInMain();

		//5.1获得主表关键字在指标关键字组合中的位置
		int[] iMainKeyInAllPos=new int[]{};//数组顺序按照主表关键字组合顺序

		//5.2获得主表时间关键字的在主表关键字组合中的位置
		int iMainTimePosInMain=-1;

		if(mainKeys!=null && mainKeys.length>0){
			iMainKeyInAllPos=new int[mainKeys.length]; 
			for(int i=0;i<iMainKeyInAllPos.length;i++){
				iMainKeyInAllPos[i]=-1;
				if(mainKeys[i]==null)
					continue;

				if(strMainTimeKey!=null && strMainTimeKey.equals(mainKeys[i].getPk_keyword()))
					iMainTimePosInMain=i;

				for(int j=0,size=measureKeyVOs.length;j<size;j++){
					if(measureKeyVOs[j]==null)
						continue;


					if(mainKeys[i].getPk_keyword().equals(measureKeyVOs[j].getPk_keyword())){
						iMainKeyInAllPos[i]=j;
						break;
					}

					if(strMainTimeKey!=null && strMainTimeKey.equals(mainKeys[i].getPk_keyword())
							&& strDynTimeKey!=null
							&& strDynTimeKey.equals(measureKeyVOs[j].getPk_keyword())){
						iMainKeyInAllPos[i]=j;
						break;
					}
				}
			}	
		}

		//6.当前主表时间关键字值
		String strMainTimeValue=null;
		if(keyValuesInMain!=null && iMainTimePosInMain>=0)
			strMainTimeValue=keyValuesInMain[iMainTimePosInMain]==null?null:keyValuesInMain[iMainTimePosInMain].getValue();

		//7.比较主表关键字值是否相同,如相同则删除该条记录
		Enumeration dbKeyValues=hashDataInDB.keys();//其中关键字值字串按照measureKeyVOs顺序
		String strTemp=null;
		StringTokenizer tokenValues=null;
		ArrayList listValues=null;
		while(dbKeyValues.hasMoreElements()){
			strTemp=(String) dbKeyValues.nextElement();
			if( listKeyValues.contains(strTemp))
				continue;

			//当主表无关键字时，则直接删除该行记录
			if(keyValuesInMain==null || keyValuesInMain.length==0 ||
					iMainKeyInAllPos.length==0){
				hashDataInDB.remove(strTemp);
				continue;
			}
			tokenValues=new StringTokenizer(strTemp,"\r\n");
			listValues=new ArrayList();
			while(tokenValues.hasMoreTokens()){
				listValues.add(tokenValues.nextToken());
			}

			//标示是否为当前表页数据，即主表关键字值相同
			boolean bEqual=false;

			//比较主表关键字值是否相同
			String str=null;
			for(int i=0;i<iMainKeyInAllPos.length;i++){

				if(iMainKeyInAllPos[i]<0 || iMainKeyInAllPos[i]>= listValues.size()){
					bEqual=false;
					break;
				}

				str=(String) listValues.get(iMainKeyInAllPos[i]);
				if(i==iMainTimePosInMain){
					if(isSameMainTime(strMainTimeValue,str,strMainTimeKey )){
						bEqual=true;
					}
					else{
						bEqual=false;
						break;
					}
				} else {
					if(nc.ui.iufo.pub.UfoPublic.strIsEqual(keyValuesInMain[i]==null?null:keyValuesInMain[i].getValue(),str)){
						bEqual=true;
					}else{
						bEqual=false;
						break;
					}
				}
			}

			if(bEqual){
				hashDataInDB.remove(strTemp);
			}

		}
	}
	/**
	 * 获得指定关键字集合中的时间关键字pk
	 * @param measureKeyVOs
	 * @return
	 */
	private static String getTimeKey(nc.vo.iufo.keydef.KeyVO[] measureKeyVOs){
		if(measureKeyVOs==null || measureKeyVOs.length==0)
			return null;
		int iLen=measureKeyVOs.length;
		ArrayList listKeyPKs=new ArrayList();
		for(int i=0;i<iLen;i++){
			if(measureKeyVOs[i]==null)
				continue;
			listKeyPKs.add(measureKeyVOs[i].getPk_keyword());
		}
		String strTimeKey=DatePropVO.getTimeKey(listKeyPKs);
		return strTimeKey;

	}
	/**
	 * 判断动态区域时间值strDynTimeValue是否与strMainTimeValue在同一个主表时间内.
	 * @param strMainTimeValue
	 * @param strDynTimeValue
	 * @param strMainTimeKey
	 * @return
	 */
	private boolean isSameMainTime(String strMainTimeValue ,String strDynTimeValue,String strMainTimeKey){
		UFODate dateMain=new UFODate(strMainTimeValue);
		UFODate dateDyn=new UFODate(strDynTimeValue);

		int iMainYear=dateMain.getYear();
		int iMainHalfY=dateMain.getHalfYear();
		int iMainQuarter=dateMain.getSeason();
		int iMainMonth=dateMain.getMonth();


		int iDynYear=dateDyn.getYear();
		int iDynHalfY=dateDyn.getHalfYear();
		int iDynQuarter=dateDyn.getSeason();
		int iDynMonth=dateDyn.getMonth();

		boolean bSame=false;
		if(strMainTimeKey.equals(nc.vo.iufo.keydef.KeyVO.YEAR_PK)){
			if(iMainYear==iDynYear)
				bSame=true;
		}else if (strMainTimeKey.equals(nc.vo.iufo.keydef.KeyVO.HALF_YEAR_PK)){
			if(iMainYear==iDynYear && iMainHalfY==iDynHalfY)
				bSame=true;
		}else if(strMainTimeKey.equals(nc.vo.iufo.keydef.KeyVO.QUARTER_PK)){
			if(iMainYear==iDynYear && iMainQuarter==iDynQuarter)
				bSame=true;
		}
		else if(strMainTimeKey.equals(nc.vo.iufo.keydef.KeyVO.MONTH_PK)){
			if(iMainYear==iDynYear && iMainMonth==iDynMonth)
				bSame=true;
		}else if(strMainTimeKey.equals(nc.vo.iufo.keydef.KeyVO.TENDAYS_PK)){
			int iMainTenDay=dateMain.getTendays();
			int iDynTenDay=dateDyn.getTendays();
			if(iMainYear==iDynYear && iMainMonth==iDynMonth && iMainTenDay==iDynTenDay )
				bSame=true;


		}else if(strMainTimeKey.equals(nc.vo.iufo.keydef.KeyVO.WEEK_PK)){
			int iMainWeek=dateMain.weekIndexOfYear();
			int iDynWeek=dateDyn.weekIndexOfYear();
			if(iMainYear==iDynYear && iMainWeek==iDynWeek)
				bSame=true;
		}
		return bSame;
	}
	
	/**
	 * 获得指标区数函数引用的动态区域关键字集合
	 * @param objMeasures
	 * @param objKeyCond 完整条件。即条件中包括偏移和补充的隐含条件.
	 * @param objDateProp
	 * @param nOffset
	 * @param objKeyDatas
	 * @param strMainTimeKey
	 * @param objEnv
	 * @return
	 */
	protected String[] getRefDynKeyFromMselect( IStoreCell[] objMeasures,String strKeyCombPK,UfoExpr objKeyCond,KeyDataGroup objKeyDatas,String strMainTimeKey,UfoCalcEnv objEnv){
		if(objKeyCond==null)
			return null;

		//1获得动态区关键字集合
		ArrayList listDynKeyPKs=getDynKeysFromKeyDatas(objKeyDatas);

		if(listDynKeyPKs==null || listDynKeyPKs.size()==0)
			return null;


		//2.获得指标的关键字集合
//		String[] strMeasKeys = null;
//		nc.vo.iufo.keydef.KeyVO[] keyVos = null;
		nc.vo.iufo.keydef.KeyVO[] keyVos = new nc.vo.iufo.keydef.KeyVO[6];
		MeasFuncDriver objFuncDriver = (MeasFuncDriver) objEnv
		.loadFuncListInst()
		.getExtDriver(MeasFuncDriver.class.getName());

		KeyGroupVO keyGroupVO = objFuncDriver.getKeyGroupCache().getByPK(
				strKeyCombPK);
		if (keyGroupVO != null) {
			keyVos = keyGroupVO.getKeys();
		}

		//3.获得动态区域时间关键字
//		String strDynTimeKey=DatePropVO.getTimeKey(listDynKeyPKs);

		//4..获得指标的时间关键字
//		nc.vo.iufo.keydef.KeyVO measTimeKey=keyGroupVO==null?null:keyGroupVO.getTimeKey();
//		String strMeasTimeKey=measTimeKey==null?null:measTimeKey.getPk_keyword();

		//5.获得指标条件中包含的关键字
		List<nc.vo.iufo.keydef.KeyVO> allKeyVOs = new ArrayList<nc.vo.iufo.keydef.KeyVO>();
		allKeyVOs.addAll(Arrays.asList(keyVos));
		List allExpr = new ArrayList();
		objKeyCond.getAllExprs(allExpr);
		for(int index = 0; index < allExpr.size(); index ++){
			UfoExpr expr  = (UfoExpr)allExpr.get(index);
			if(isExtKeyFunc(expr)){
				KFunc keyExpr = (KFunc)expr.getElementObjByIndex(0);
				nc.vo.iufo.keydef.KeyVO keyVO = keyExpr.getParamKeyVO();
				allKeyVOs.add(keyVO);
			}
			
		}
		
//		//6.获得指标关键字集合同动态区域关键字的交集
//		ArrayList listIntersect=null;
//		if(allKeyVOs!=null && allKeyVOs.size()>0 ){
//			listIntersect=new ArrayList();
//			int iLen=allKeyVOs.size();
//			for (int i=0;i<iLen;i++){
//				if(allKeyVOs.get(i)==null)
//					continue;
//				if(listDynKeyPKs.contains(allKeyVOs.get(i).getKeywordPK())){
//					listIntersect.add(allKeyVOs.get(i).getKeywordPK());
//				}else if(strMeasTimeKey!=null && strDynTimeKey!=null 
//						&& strMeasTimeKey.equals(allKeyVOs.get(i).getKeywordPK())){
//					//时间关键字
//					if(strMainTimeKey==null  || DatePropVO.getDateTypeByPK(strMeasTimeKey)>DatePropVO.getDateTypeByPK(strMainTimeKey))
//						listIntersect.add(strDynTimeKey);
//				}
//			}
//		}
//		
//		if(listIntersect==null || listIntersect.size()==0)
//			return null;
		
		ArrayList listIntersect=listDynKeyPKs;

		//7.2获得条件中引用动态区域关键字的集合
		String[] strRefKey=MeasCondExprUtil.getDynKeyFromExpr(objKeyCond,listIntersect,strMainTimeKey);
		return strRefKey;

	}
	/**
	 * 是否Key函数(Key或ZKey函数)
	 * @param expr 解析后的函数表达式
	 * @return
	 */
	private static boolean isExtKeyFunc(UfoExpr expr){
		Object objFunc = expr.getElementObjByIndex(0);
		if(objFunc == null || !(objFunc instanceof ExtFunc)){
			return false;
		}
		
		ExtFunc extFunc = (ExtFunc)objFunc;
		String strDriverName = extFunc.getFuncDriverName();
		if(strDriverName == null 
				|| !(extFunc instanceof KFunc)){
			return false;
		}
		
		return true;
	}
	/**
	 * 从数据库处单个计算指标取数函数值
	 * @param objMeasures
	 * @param objDateProp
	 * @param nOffset
	 * @param objKeyCond
	 * @param nVer
	 * @param objEnv
	 * @param objCurKeyDatas
	 * @return
	 * @throws com.ufsoft.iufo.util.parser.CmdException
	 */
	private Object[] calcSelectValueByOne(IStoreCell[] objMeasures,
			DatePropVO objDateProp, Double nOffset, UfoExpr objKeyCond,
			Integer nVer, UfoCalcEnv objEnv, KeyDataGroup objCurKeyDatas)
	throws CmdException {
		try {
			//预处理
			String strKeyGroupPK = null;
			Object[] objValues = new Object[objMeasures.length];
			for (int i = 0; i < objMeasures.length; i++) {
				if (objMeasures[i] == null) {
					objValues[i] = null;
				}
				if (objMeasures[i] != null) {
					if(objMeasures[i] instanceof MeasureVO) {
						strKeyGroupPK = objEnv.getMeasureCache().getKeyCombPk(
								objMeasures[i].getCode());
					} else {
//						String repPk = objEnv.getRepPK();
//						strKeyGroupPK = UFOCacheManager.getSingleton().getReportCache().getByPK(repPk).getPk_key_comb();
						strKeyGroupPK = objMeasures[i].getKeyCombPK();
					}
					
					if (strKeyGroupPK == null) {
						objMeasures[i] = null;
						objValues[i] = null;
					}
				}
				if (objMeasures[i]!= null && objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
					objValues[i] = new Double(0);
				} else if(objMeasures[i]!= null && objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL) {
					objValues[i] = new UFDouble(0);
				} else {
					objValues[i] = "";
				}
			}
			if (strKeyGroupPK == null) {
				return objValues;
			}
			
			boolean isOnServer = !objEnv.isClient();
			Hashtable hashValue = null;
			if (isOnServer) {
				hashValue = MeasFuncBO_Client
				.getSelectValue(objMeasures, 
						nOffset, 
						objDateProp,
						objKeyCond, 
						nVer, 
						objEnv.getMeasureEnv(),
						objEnv.getExEnv(), 
						objEnv.getRepPK(), 
						objEnv.getKeys(), 
						objCurKeyDatas);

			} else {
				hashValue = nc.ui.iufo.calculate.MeasFuncBO_Client
				.getSelectValue(objMeasures, nOffset, objDateProp,
						objKeyCond, nVer, objEnv.getMeasureEnv(),
						objEnv.getExEnv(), objEnv.getRepPK(), objEnv
						.getKeys(), objCurKeyDatas);
			}
			
			//处理指标公式追踪结果返回值
			if(objEnv.isMeasureTrace()){
				MeasureTraceVO[] mtvos = (MeasureTraceVO[]) hashValue.get(ICalcEnv.MEASURE_TRACE_FLAG);
				hashValue.remove(ICalcEnv.MEASURE_TRACE_FLAG);
				objEnv.setMeasureTraceVOs(mtvos);
			}
			
			//处理返回
			if(hashValue != null){
				for (int i = 0; i < objMeasures.length; i++) {
					if (objMeasures[i] != null) {
						if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER 
								|| objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL) {
							objValues[i] = hashValue.get(objMeasures[i].getCode());
						} else {
							objValues[i] = (String) hashValue.get(objMeasures[i].getCode());
						}
					}
				}			
			}
			return objValues;
		} catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}

	}

	/**
	 * 从数据库中获得mselect数据。 创建日期：(2003-8-8 10:06:43)
	 * 
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures
	 *            nc.vo.iufo.measure.MeasureVO[]
	 * @param objDateProp
	 *            nc.vo.iufo.keyword.DatePropVO
	 * @param nOffset
	 *            java.lang.Double
	 * @param objKeyCond
	 *            com.ufsoft.iufo.util.expression.UfoExpr
	 *            可能传入的是参数的条件表达式或已替换过当前关键字值的表达式。
	 * @param nVer
	 *            java.lang.Integer
	 * @param objEnv
	 *            com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException
	 *                异常说明。
	 */
	protected UfoVal[] calcSelectValue(
			IStoreCell[] objMeasures,
			DatePropVO objDateProp,
			Double nOffset,
			UfoExpr objKeyCond,
			Integer nVer,
			UfoCalcEnv objEnv)
	throws CmdException {
		if (objEnv == null || objEnv.getMeasureEnv() == null) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}

		try {
			int iLen=objMeasures.length;
			Object[] objValues = new Object[iLen];

			//检查并获得指标关键字组合pk
			String strKeyGroupPK = null;
			String strTemp=null;
			for (int i = 0; i < iLen; i++) {
				if (objMeasures[i] != null ) {
					strTemp =objMeasures[i].getKeyCombPK();
					if(strTemp == null){
						objValues[i]=null;
					}else if(strKeyGroupPK==null){
						strKeyGroupPK=strTemp;
					}
					if(strKeyGroupPK!=null && !strKeyGroupPK.equals(strTemp))
						throw new CmdException("miufocalc000548");
				}
			}


			if (strKeyGroupPK != null) {        

				KeyDataGroup objCurKeyDatas = null;
				if(objEnv instanceof ReportDynCalcEnv){
					objCurKeyDatas = ((ReportDynCalcEnv) objEnv).getKeyDatas();
				}

				if(objCurKeyDatas==null){
					//主表公式计算
					objValues=calcSelectValueByOne(objMeasures,objDateProp,nOffset,objKeyCond, nVer,objEnv,objCurKeyDatas);

				}else{
					//动态区公式计算
//					objValues=calcSelectValueByOne(objMeasures,objDateProp,nOffset,objKeyCond, nVer,objEnv,objCurKeyDatas);

					//1.获得主表时间关键字
					String strMainTimeKey=null;
					if(objEnv instanceof ReportDynCalcEnv){
						//获得主表时间关键字
						KeyGroupVO mainKeyGroupVO=((ReportDynCalcEnv) objEnv).getKeyGroupVOInMain();
						if(mainKeyGroupVO!=null ){
							nc.vo.iufo.keydef.KeyVO mainTimeKeyVO = mainKeyGroupVO.getTTimeKey();
							if(mainTimeKeyVO != null)
								strMainTimeKey = mainTimeKeyVO.getPk_keyword();
						}
					}

					//3.获得函数的完整条件
					UfoExpr exprAllCond=getZkeyTimeCondExpr(objEnv,strKeyGroupPK);

					//4.获得条件引用的动态区关键字

					String[] strRefDynKeys=getRefDynKeyFromMselect(objMeasures,strKeyGroupPK,exprAllCond,objCurKeyDatas,strMainTimeKey,objEnv);
					if(strRefDynKeys==null){
						//5.动态区各行计算的值相同
						if(m_hashDbData==null){
							m_hashDbData=new Hashtable();
							//5.1从数据库获得数据
							Object[] objValueTemps=calcSelectValueByOne(objMeasures,objDateProp,nOffset,objKeyCond, nVer,objEnv,objCurKeyDatas);
							Object objTemp=null;
							for(int i=0;i<iLen;i++){
								if(objMeasures[i]==null)
									continue;
								objTemp=objValueTemps[i];

								if(objTemp==null){
									if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
										objTemp= new Double(0);
									} else if(objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL) {
										objTemp= new UFDouble(0);
									} else{
										objTemp= "";
									}
								}
								//m_hashDbData存储数据为：指标对应数值
								m_hashDbData.put(objMeasures[i].getCode(),objTemp);
							}
						}
						//5.2 获得数据
						for(int i=0;i<iLen;i++){
							if(objMeasures[i]==null){
								objValues[i]=null;
								continue;
							}
							objValues[i]=m_hashDbData.get(objMeasures[i].getCode());
						}
					//	m_hashDbData = null;
					} else{
						//6.动态区各行计算的值不同，则进行多行批量计算.

						//6.1 获得动态区域各行关键字值
						KeyDataGroup[] dynaKeyDatas=(KeyDataGroup[]) ((ReportDynCalcEnv)objEnv).getDynAllKeyDatas();

						//6.2获得当前计算动态区域行中，条件中动态区域关键字的值串
						String strCondDynKeyValue=getKeyValueString(objCurKeyDatas,strRefDynKeys);

						boolean bLoadData=false;
						if ( m_hashDbData==null ) { 
							bLoadData=true;
						}else {
							Vector vecCacheKey=(Vector) m_hashDbData.get(CACHE_KEY);
							if(vecCacheKey==null || !vecCacheKey.contains(strCondDynKeyValue)){
								bLoadData=true;
							}
						}
						if(bLoadData){
							//补充用户权限条件
//							UfoExpr exprFullUserCond = MeasFunc.applyUserRightToCond(exprAllCond, objEnv, strKeyGroupPK);
							UfoExpr[] exprSelectConds=getSelectCondsAll(objEnv, strKeyGroupPK);
							UfoExpr exprNotTimeCond=ReplenishKeyCondUtil.combineBoolExpr(exprSelectConds[0],getUserRightExpr( objEnv, strKeyGroupPK));
							UfoExpr exprTimeCond=exprSelectConds[1];

							//6.3批量读取指定行数的数据
							m_hashDbData= batchReadSelectFromDB(objMeasures,
									exprNotTimeCond,exprTimeCond,objDateProp,nOffset,
									nVer,(ReportDynCalcEnv)objEnv,objCurKeyDatas,
									dynaKeyDatas,strRefDynKeys,strMainTimeKey);
							KeyDataGroup[] batchKeyDatas = getBatchKeyDatas(dynaKeyDatas, objCurKeyDatas, MSELECTFUNC_STEP);
							KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK);
							
							Map<String, IKeyDetailData> dynKeyDetail = new Hashtable<String, IKeyDetailData>();
							for(KeyDataGroup keyDataGroup : batchKeyDatas) {
								KeyDataVO[] keyDataVos = keyDataGroup.getKeyDatas();
								if(keyDataVos != null && keyDataVos.length > 0) {
									for(KeyDataVO keyData : keyDataVos) {
										if(keyData.getKeyData() != null) {
											String refPk = keyData.getKey().getRef_pk();
											if(refPk != null) {
												dynKeyDetail.put(refPk + "," + keyData.getKeyData().getValue(), keyData.getKeyData());
											}
										}
									}
								}
							}
							
							m_hashDataByKeyData = getDataByKeyData(m_hashDbData, batchKeyDatas, keyGroup, (ReportDynCalcEnv)objEnv, objDateProp, nOffset, dynKeyDetail);
						}
						
						//6.4 按照当前行从m_hashDbData中筛选数据
						objValues = getDynCurSelectFromCache( objMeasures,strCondDynKeyValue,(ReportDynCalcEnv)objEnv,objCurKeyDatas,objDateProp,nOffset,strKeyGroupPK);
					//	m_hashDbData = null;
					}
					//动态区公式计算处理结束
				}
			}

			//处理返回
			UfoVal[] objVals = new UfoVal[objMeasures.length];
			for (int i = 0; i < objMeasures.length; i++) {
				objVals[i] = UfoVal.createVal(objValues[i]);
			}
			return objVals;

		}catch(ScriptException e){
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		} 
		catch (Exception e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
	}
	
	/**
	 * 一次批量找出动态区中行的计算结果，为了减少循环次数
	 * @param hashData,从数据中找出的数据
	 * @param keyDatas，动态区关键字值数组
	 * @param keyGroupVO，动态区关键字组合VO
	 * @param env
	 * @param objDateProp，函数中的日期类型
	 * @param nOffset，日期偏移量
	 * @return
	 * @throws UfoCmdException
	 */
    private Hashtable getDataByKeyData(Hashtable<String,Object> hashData, KeyDataGroup keyDataGroups[], KeyGroupVO keyGroupVO, ReportDynCalcEnv env, DatePropVO objDateProp, Double nOffset, Map<String, IKeyDetailData> dynKeyDetail) throws UfoCmdException{
	    if(keyDataGroups == null || keyDataGroups.length <= 0)
	        return null;
	    
	    //对动态区中关键字值做排序
	    Arrays.sort(keyDataGroups,new Comparator<KeyDataGroup>(){
			public int compare(KeyDataGroup o1, KeyDataGroup o2) {
				return o1.toString().compareTo(o2.toString());
			}
	    });
	    
	    Object oldExZKeyValue = env.getExEnv("ZKEYVALUES");
	    String strOldDynPk = env.getDynArea();
	    KeyDataGroup oldKeyDatas = env.getKeyDatas();
	    MeasurePubDataVO oldMPubVO = env.getMeasureEnv();
	    
	    try{
	    	nc.vo.iufo.keydef.KeyVO[] measureKeyVOs = keyGroupVO.getKeys();
	    	UfoExpr exprKeyCond = null;
		    KeyDataVO mainKeyDatas[] = getMainKeyData(env);
		    List<KeyDataGroup> vKeyDataGroup=new ArrayList<KeyDataGroup>(Arrays.asList(keyDataGroups));
		    
		    //按动态区关键字值返回的计算结果
		    Hashtable hashRetData = new Hashtable();
	        hashData = (Hashtable<String,Object>)hashData.clone();
	        
	        //得到从数据库取到的值，并按关键字值做排序
	        String[] strKeyVals=hashData.keySet().toArray(new String[0]);
	        Arrays.sort(strKeyVals);
	        
	        //每行的exprKeyCond
	        List<UfoExpr> vExprKeyCond=new ArrayList<UfoExpr>();
	        
	        //该行是否被删除，即在判断时不需要判断
	        List<Boolean> vDeleted=new ArrayList<Boolean>();
	        
	        //初始化前两个数组
	        for (int i=0;i<vKeyDataGroup.size();i++){
	        	vDeleted.add(Boolean.FALSE);
	        	vExprKeyCond.add(null);
	        }
	        
	        for (int i=0;i<strKeyVals.length;i++){
	        	if("cachekey".equalsIgnoreCase(strKeyVals[i]))
	        		continue;
	        	
	        	if (vKeyDataGroup.size()<=0)
	        		break;
	        	
                MeasurePubDataVO newMPubVO =null;
                KeyDataGroup newKeyGroup =null;
	        	
                //2012.8.31 tianchuan 清空vDeleted。初步认为vDeleted的存在是有问题的
                vDeleted.clear();
                for (int j=0;j<vKeyDataGroup.size();j++){
                	vDeleted.add(Boolean.FALSE);
                }
                
                //记录最后一次匹配的位置
                int iLastFitPos=-1;
	        	for (int j=0;j<vKeyDataGroup.size();j++){
	        		if (vDeleted.get(j).booleanValue())
	        			continue;
	        		
	        		try{
		        		if (newMPubVO==null){
		                    newMPubVO =createMeaurePubData(keyGroupVO, strKeyVals[i],env);
		                    newKeyGroup = new KeyDataGroup();
		                    env.setDynAreaInfo(null, newKeyGroup);
		                    env.setMeasureEnv(newMPubVO);
		        		}
		        		
			            KeyDataGroup newKeyGroupCurKey = combineKeyDatas(mainKeyDatas,vKeyDataGroup.get(j));
			            env.setExEnv("ZKEYVALUES", newKeyGroupCurKey);
	
		            	exprKeyCond=vExprKeyCond.get(j);
		            	if (exprKeyCond==null){
		            		MeasurePubDataVO tmpPubData=(MeasurePubDataVO)newMPubVO.clone();
		            		
		            		for (nc.vo.iufo.keydef.KeyVO key:tmpPubData.getKeyGroup().getKeys()){
		            	        String strOldVal=oldMPubVO.getKeywordByPK(key.getPk_keyword());
		            	        if (strOldVal!=null){
		            	        	tmpPubData.setKeywordByPK(key.getPk_keyword(), strOldVal);
		            	        }
		            	    }
		            		
		            		KeyDataVO[] keyDataVOs=vKeyDataGroup.get(j).getKeyDatas();
		            		for (int k=0;k<keyDataVOs.length;k++){
		            			tmpPubData.setKeywordByPK(keyDataVOs[k].getKey().getPk_keyword(), keyDataVOs[k].getValue());
		            		}
		            		env.setMeasureEnv(tmpPubData);
		            		exprKeyCond = getValueTimeCondExpr(objDateProp, nOffset, env, keyGroupVO.getKeyGroupPK());
		            		env.setMeasureEnv(newMPubVO);
		            		vExprKeyCond.set(j, exprKeyCond);
		            	}
	        		}catch(Exception e){
	        			AppDebug.debug(e);
	        			vDeleted.set(j, Boolean.TRUE);
	        			continue;
	        		}

	        		newKeyGroup =createNewKeyGroup(measureKeyVOs, strKeyVals[i]);
	        		
	        		KeyDataVO[] keyDatas = newKeyGroup.getKeyDatas();
	        		for(KeyDataVO keyData : keyDatas) {
	        			String refPk = keyData.getKey().getRef_pk();
	        			if(refPk != null) {
	        				IKeyDetailData detailData = dynKeyDetail.get(refPk + "," + keyData.getValue());
	        				if(detailData != null) {
	        					keyData.setKeyData(detailData);
	        				}
	        			}
	        		}
	        		
	        		env.setDynAreaInfo(null, newKeyGroup);
	        		
		            double dbRet=exprKeyCond.calcExpr(env)[0].doubleValue();
		            if (dbRet==1){
		            	//符合条件的，放进返回结果
		            	hashRetData.put(vKeyDataGroup.get(j),m_hashDbData.get(strKeyVals[i]));
		            	iLastFitPos=j;
		            }else if (iLastFitPos>=0){
		            	//不符合条件，将上一次符合条件与本次之间的行标记为不可匹配行
		            	for (int k=iLastFitPos;k >= 0;k--){
		            		vDeleted.set(k, Boolean.TRUE);
		            	}
		            	//2012.8.31 tianchuan 为什么要break？这样后面匹配的就取不到了
		            	//break;
		            }
	        	}
	        }
	        return hashRetData;
	    }
	    catch(ScriptException e){
	        AppDebug.debug(e);
	        throw new UfoCmdException(e);
	    }
	    catch(Exception e){
	        AppDebug.debug(e);
	        throw new UfoCmdException(e.getMessage());
	    }
	    finally{
		    if(oldExZKeyValue != null)
		        env.setExEnv("ZKEYVALUES", oldExZKeyValue);
		    else
		        env.removeExEnv("ZKEYVALUES");
		    env.setMeasureEnv(oldMPubVO);
		    env.setDynAreaInfo(strOldDynPk, oldKeyDatas);
	    }
	}
 

	/**
	 * 批量获得指标取数函数的函数数值
	 * @param objMeasures
	 * @param exprNotTimeCond 条件中补充了zkey形式非时间关键字条件
	 * @param exprTimeCond 原表达式中时间关键字条件
	 * @param nOffset 
	 * @param objKeyCond
	 * @param nVer
	 * @param objEnv
	 * @param dynKeyDatas
	 * @param strRefDynKeys
	 * @param strMainTimeKey
	 * @return
	 */
    protected Hashtable batchReadSelectFromDB( IStoreCell[] objMeasures,
			UfoExpr  exprNotTimeKeyCond,
			UfoExpr exprTimeKeyCond,
			DatePropVO objDateProp,
			Double nOffset,
			Integer nVer,
			ReportDynCalcEnv objEnv,
			KeyDataGroup curKeyData,
			KeyDataGroup[] dynKeyDatas,
			String[] strRefDynKeys,
			String strMainTimeKey)throws CmdException{

		if(dynKeyDatas==null || dynKeyDatas.length==0 || curKeyData==null)
			return null;

		try {

//			KeyDataGroup[]	batchKeyDatas=getBatchSelectKeyDatas(objKeyCond,objEnv,dynKeyDatas,curKeyData);
			KeyDataGroup[]	batchKeyDatas=getBatchKeyDatas(dynKeyDatas,curKeyData,MSELECTFUNC_STEP);
			if(batchKeyDatas==null || batchKeyDatas.length==0)
				return null;

			Hashtable hashReturn=null;


			if (objEnv.isClient()==false) {
//				nc.bs.iufo.calculate.MeasFuncBO objMeasFuncBO = new nc.bs.iufo.calculate.MeasFuncBO();
				hashReturn=MeasFuncBO_Client.batchDynSelectValue(
						objMeasures,
						exprNotTimeKeyCond,
						exprTimeKeyCond,
						objDateProp,
						nOffset,
						nVer,
						objEnv.getMeasureEnv(),
						objEnv.getExEnv(),
						objEnv.getRepPK(),
						objEnv.getKeys(),
						curKeyData,
						batchKeyDatas,
						strRefDynKeys,
						strMainTimeKey);


			} else {
				hashReturn=MeasFuncBO_Client.batchDynSelectValue(
						objMeasures,
						exprNotTimeKeyCond,
						exprTimeKeyCond,
						objDateProp,
						nOffset,
						nVer,
						objEnv.getMeasureEnv(),
						objEnv.getExEnv(),
						objEnv.getRepPK(),
						objEnv.getKeys(),
						curKeyData,
						batchKeyDatas,
						strRefDynKeys,
						strMainTimeKey);

			}
			
			if(hashReturn==null){
				hashReturn=new Hashtable();
			}

			//处理指标公式追踪结果返回值
			if(objEnv.isMeasureTrace()){
				MeasureTraceVO[] mtvos = (MeasureTraceVO[]) hashReturn.get(ICalcEnv.MEASURE_TRACE_FLAG);
				hashReturn.remove(ICalcEnv.MEASURE_TRACE_FLAG);
				objEnv.setMeasureTraceVOs(mtvos);
			}
			
			//加入批量获得各行的关键字值信息
			Vector vecTemp=getKeyValueStrings(batchKeyDatas,strRefDynKeys);
			if(vecTemp!=null)
				hashReturn.put(CACHE_KEY,vecTemp);

			return hashReturn;
		} 
		catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}

	}
	private KeyDataGroup combineKeyDatas(KeyDataVO[] mainKeyDatas,KeyDataGroup dynKeyDatas){
		ArrayList listReturn=new ArrayList();
		String strDynTimeKey=null;
		boolean bInitDetail=true;
		if(dynKeyDatas!=null && dynKeyDatas.getKeyDatas().length>0){
			ArrayList listDynKeyPKs=getDynKeysFromKeyDatas(dynKeyDatas);
			strDynTimeKey=DatePropVO.getTimeKey(listDynKeyPKs);
			KeyDataVO[] dyns=dynKeyDatas.getKeyDatas();
			if(dyns!=null && dyns.length>0){
				listReturn.addAll(Arrays.asList(dyns));
			}
			if (!dynKeyDatas.getKeyDatas()[0].isInitedKeyData())
				bInitDetail=false;
		}
		
		if(mainKeyDatas!=null && mainKeyDatas.length>0){
			int iLen=mainKeyDatas.length;
			for(int i=0;i<iLen;i++){
				if(strDynTimeKey==null){
					listReturn.add(mainKeyDatas[i]);
				}else if(mainKeyDatas[i].getKey().getType()!=nc.vo.iufo.keydef.KeyVO.TYPE_TIME){

					listReturn.add(mainKeyDatas[i]);
				}
			}	
			
			if (!mainKeyDatas[0].isInitedKeyData())
				bInitDetail=false;
		}
		KeyDataGroup newKeyGroup=new KeyDataGroup();
		KeyDataVO[] newKeyDatas=new KeyDataVO[listReturn.size()];
		listReturn.toArray(newKeyDatas);
		newKeyGroup.setKeyDatas(newKeyDatas);
		
		if (bInitDetail==false){
			for (int i=0;i<newKeyDatas.length;i++)
				newKeyDatas[i].reSetInitedKeyData();
		}

		return newKeyGroup;
	}
	/**
	 * 获得主表关键字值
	 * @param env
	 * @return
	 */
	private KeyDataVO[] getMainKeyData(ReportDynCalcEnv env){
		nc.vo.iufo.keydef.KeyVO[] mainKeyVOs=env.getKeyGroupVOInMain().getKeys();
		KeyDataVO[] mainKeyDatas=null;
		MeasurePubDataVO mpubdata=env.getMeasureEnv();
		if(mainKeyVOs!=null && mainKeyVOs.length>0 && mpubdata!=null){
			int iLen=mainKeyVOs.length;
			mainKeyDatas=new KeyDataVO[iLen];
			MeasurePubDataUtil.initMeasurePubDataKeyData(new MeasurePubDataVO[]{mpubdata});
			for(int i=0;i<iLen;i++){
				mainKeyDatas[i]=new KeyDataVO();
				mainKeyDatas[i].setKey(mainKeyVOs[i]);
				IKeyDetailData keyValue=mpubdata.getKeyDataByPK(mainKeyVOs[i].getPk_keyword());
//				if (nc.vo.iufo.keydef.KeyVO.CODE_TYPE_CORP.equals(mainKeyVOs[i]
//				                                                             .getCode())
//				                                                             || nc.vo.iufo.keydef.KeyVO.CODE_TYPE_DIC_CORP.equals(mainKeyVOs[i].getCode())) {
//					String strUnitPK=strKeyValue;
//					try{
//						ReportOrgVO unitinfo = ReportOrgUtil.getReportOrg(strUnitPK);
//						if(unitinfo!=null){
//							strUnitPK = unitinfo.getCode() ;
//						}
//					}catch(BusinessException ex){
//						AppDebug.debug(ex);
//					}
//
//				}
				mainKeyDatas[i].setValue(keyValue==null?null:keyValue.getValue());
				mainKeyDatas[i].setKeyData(keyValue);
			}
		}
		return mainKeyDatas;
	}

	/**
	 * 从缓存结果中获得当前动态行的指标取数函数值
	 * @param objMeasures
	 * @param strCondDynKeyValue
	 * @return
	 */
    private Object[] getDynCurSelectFromCache(IStoreCell[] objMeasures, String strCondDynKeyValue, ReportDynCalcEnv env, KeyDataGroup curkeyGroupData, DatePropVO objDateProp, Double nOffset, String strKeyGroupPK)
    throws CmdException
	{
	    int iLen = objMeasures.length;
	    Object objValues[] = new Object[iLen];
	    for(int i = 0; i < objMeasures.length; i++)
	    {
	        if(objMeasures[i] == null)
	            objValues[i] = null;
	        if(objMeasures[i].getType() == IStoreCell.TYPE_NUMBER)
	            objValues[i] = new Double(0.0D);
	        else if(objMeasures[i].getType() == IStoreCell.TYPE_BIGDECIMAL)
	            objValues[i] = new UFDouble(0.0D);
	        else
	            objValues[i] = "";
	    }
	
	    if(m_hashDataByKeyData == null || m_hashDataByKeyData.size() == 0 || curkeyGroupData == null)
	        return objValues;
	    Hashtable hashValue = (Hashtable)m_hashDataByKeyData.get(curkeyGroupData);
	    Object objTemp = null;
	    if (hashValue!=null){
		    iLen = objMeasures.length;
		    for(int i = 0; i < iLen; i++){
		        if(objMeasures[i] != null){
		            objTemp = hashValue.get(objMeasures[i].getCode());
		            if(objTemp != null)
		                objValues[i] = objTemp;
		        }
		    }
	    }
	    return objValues;
	}

	protected UfoVal[] calcStatValues(IStoreCell[] objMeasures,
			UfoExpr objKeyCond,
			UfoCalcEnv objEnv)
	throws CmdException {
		int iMeasureLen=objMeasures.length;
		UfoVal[] valReturn=new UfoVal[iMeasureLen];
		for(int i=0;i<iMeasureLen;i++){
			valReturn[i]= calcStatValue(objMeasures[i], objKeyCond, objEnv);
		}
		return valReturn;
	}

	/**
	 * 如果是固定表、没有进行预处理的指标统计函数用此方法计算。
	 * 创建日期：(2003-8-8 10:06:43)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures nc.vo.iufo.measure.MeasureVO
	 * @param objKeyCond com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	protected UfoVal calcStatValue(
			IStoreCell objMeasure,
			UfoExpr objKeyCond,
			UfoCalcEnv objEnv)
	throws CmdException {
		if (objEnv == null || objEnv.getMeasureEnv() == null) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		boolean bModified = false;
		try {
			//如果指标为当前主表指标，则保存指标对应区域的值到env中，这样计算时就包括当前输入而未保存的值.
			bModified = saveMeasValueByArea(getMeasures(objEnv), objEnv);
			Object objValue = null;
			com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup objKeyDatas = null;
			if(objEnv instanceof ReportDynCalcEnv){
				objKeyDatas = ((ReportDynCalcEnv) objEnv).getKeyDatas();
			}
			boolean isOnServer = !objEnv.isClient();
			short funcId=MeasFuncDriver.getFuncIdByName(getFuncName());
			if(MeasFuncDriver.FMSUMA==funcId)
				funcId=MeasFuncDriver.FMSUM;

			if (isOnServer) {
				objValue =
					MeasFuncBO_Client.getAggrValueByBindParam(
							funcId,
							objMeasure,
							objKeyCond,
							objEnv.getMeasureEnv(),
							objEnv.getExEnv(),
							objEnv.getRepPK(),
							objEnv.getKeys(),
							objKeyDatas,objEnv.getDataChannel());
			} else {
				objValue =
					nc.ui.iufo.calculate.MeasFuncBO_Client.getAggrValueByBindParam(
							funcId,
							objMeasure,
							objKeyCond,
							objEnv.getMeasureEnv(),
							objEnv.getExEnv(),
							objEnv.getRepPK(),
							objEnv.getKeys(),
							objKeyDatas,objEnv.getDataChannel());
			}
			return UfoVal.createVal(objValue);

		}
		catch(ScriptException e){
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		}
		catch (Exception e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}finally{
			if(bModified){
				clearMeasValueByArea(new IStoreCell[] { objMeasure }, objEnv);
			}
		}
	}
	/**
	 * 将关键字条件表达式中的ExprOperand操作数用其值替换生成新的元素放在objNewEles中，并检查每个值是否与env中的关键字值一致，如果一致返回真
	 * 创建日期：(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param    UfoExpr objKeyCond,
	 * @param    UfoEElement[] objNewEles,这个参数中会放置新的表达式的元素
	 * @param    UfoExprCalcEnv objEnv * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	public static boolean checkKeyCondWithEnv(
			UfoExpr objKeyCond,
			UfoEElement[] objNewEles,
			UfoCalcEnv objEnv)
	throws CmdException {
		try {
			boolean bKeyCondSame = true;

			if (objKeyCond != null) {
				UfoEElement[] objEles = objKeyCond.getElements();
				if(objNewEles == null || objEles.length != objNewEles.length){
					throw new UfoCmdException("miufo1000404");  //"表达式因子数目不一致！"
				}
				for (int i = 0; objEles != null && i < objEles.length; i++) {
					if (objEles[i].getType() == UfoEElement.OPR
							&& ((objEles[i].getObj() instanceof UfoExpr) || (objEles[i].getObj() instanceof NumOperand))) {//instance of ExprOperand, modify by ljhua 2005-12-5 因为去除ExprOperand
						//tianchuan ++ 考虑自动生成条件的情况NumOperand
//						UfoVal[] objVal =
//							((UfoExpr) objEles[i].getObj()).getValue( objEnv);
						UfoVal[] objVal = null;
						if(objEles[i].getObj() instanceof UfoExpr){
							objVal =((UfoExpr) objEles[i].getObj()).getValue(objEnv);
						}else if(objEles[i].getObj() instanceof NumOperand){
							objVal =((NumOperand) objEles[i].getObj()).getValue(objEnv);
						}
						
						if (objVal == null || objVal[0] == null || objVal[0] == UfoNullVal.getSingleton()) {
							String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufo1000405",null,
									new String[]{((KeyVO) ((UfoFunc) objEles[i - 1].getObj()).getParams().get(0)).getName()});
							throw new UfoCmdException(msg);  //                              "关键字"                                  + ((KeyVO) ((UfoFunc) objEles[i - 1].getObj()).getParams().get(0)).getName()                                  + "必须指定一个值！"
						} else {
							try {
								objNewEles[i] =
									new UfoEElement(
											UfoEElement.OPR,
											NumOperand.getInstanceByValue(objVal[0].doubleValue()));
								int nType = ((KeyVO) ((UfoFunc) objEles[i - 1].getObj()).getParams().get(0)).getType();
								int nDateKeyValue;
								if(DatePropVO.isTimeKey(nType)){
									nDateKeyValue = DatePropFunc.getDateValue(nType, (objEnv == null?null:objEnv.getMeasureEnv()));
								} else {
									nDateKeyValue = DatePropFunc.getAccPeriodDateValue(nType, (objEnv == null?null:objEnv.getMeasureEnv()));
								}
								if (nDateKeyValue != objVal[0].doubleValue()) {
									bKeyCondSame = false;
								}
							} catch (UfoValueException e) {
								if (e.getErrNo() == UfoValueException.ERR_NOTNUMDATA
										&& (objVal[0] instanceof UfoString)) {
									objNewEles[i] =
										new UfoEElement(UfoEElement.OPR, new StrOperand((String) objVal[0].getValue()));
									KeyWordVO objKeyWord =
										(KeyWordVO) ((KFunc) objEles[i - 1].getObj()).getParams().get(0);
									if ((/*!objKeyWord.getKey().isPrivate() && */objEnv != null && objEnv.getMeasureEnv() != null
											&& !((String) objVal[0].getValue()).equals(ReplenishKeyCondUtil.getKeyValueFromMpub(objKeyWord.getKey(), objEnv)
											))/*
											|| (objKeyWord.getKey().isPrivate()
													&& (!(objEnv instanceof ReportDynCalcEnv)
															|| ((ReportDynCalcEnv) objEnv).getPrvKeyValue(objKeyWord.getKey().getPk_keyword()) == null
															|| !((ReportDynCalcEnv) objEnv)
															.getPrvKeyValue(objKeyWord.getKey().getPk_keyword())
															.equals(objVal[0].getValue())))*/) {
										bKeyCondSame = false;
									}
								} else {
									throw new UfoCmdException(e);
								}
							}
						}
					} else {
						objNewEles[i] = objEles[i];
					}
				}
			}
			return bKeyCondSame;
		} catch (OprException ope) {
			throw new UfoCmdException(ope);
		}
	}
	/**
	 * 在计算之后，清除区域与指标关联的值.
	 * Creation date: (2003-3-21 15:37:52)
	 * @param mVOs MeasureVO[]
	 * @param env UfoCalcEnv
	 * @return boolean 如果
	 */
	private void clearMeasValueByArea(IStoreCell[] mVOs, UfoCalcEnv env) throws CmdException {
		for (int i = 0; i < mVOs.length; i++) {
			if (mVOs[i] != null) {
				env.removeExEnv(MEASEXENVPREFIX + mVOs[i].getCode());
			}
		}
	}
	/**
	 * 用新的nFuncID和参数列表和其他本函数属性内容形成新的函数对象。所有子类必须重写此方法。
	 * 创建日期：(2003-7-11 13:17:11)
	 * @return com.ufsoft.iufo.util.expression.UfoFunc
	 * @param nFuncID short
	 * @param alPara java.util.ArrayList
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	public UfoFunc createNewFunc(short nFuncID, java.util.ArrayList alPara) throws CmdException {
		return new MeasFunc(nFuncID, getFuncName(), alPara, getFuncDriverName(), getReturnType());
	}
	/**
	 * 对给定日期的年、月、日、季、旬中任一部分的值进行调整，返回结果。
	 * 创建日期：(2001-11-26 20:38:26)
	 * @return java.lang.String
	 * @param date java.lang.String
	 * @param field java.lang.String 时间类型。要求为UfoDate类定义的期间类型
	 * @param n int
	 * @param strDateProp 时间类型。要求为UfoDate类定义的期间类型
	 * @deprecated
	 */
	public static NumOperand getAdjustedDate(
			String date,
			String field,
			int n,
			String strDateProp) throws TranslateException{
		if(date == null || date.equals(nc.vo.iufo.pub.date.UFODate.NONE_DATE)){
			throw new TranslateException(TranslateException.ERR_ENV);
		}

		nc.vo.iufo.pub.date.UFODate ufoDate = new nc.vo.iufo.pub.date.UFODate(date);
		if (field != null) {

			nc.vo.iufo.pub.date.UFODate ufoDate1 =
				new nc.vo.iufo.pub.date.UFODate(ufoDate.getNextDate(field, n));
			ufoDate = ufoDate1.getEndDay(strDateProp);
		}
		return NumOperand.getInstance(
				new Double(
						DatePropVO.getPropString(KeyVO.YEAR_TYPE, ufoDate.getYear())
						+ DatePropVO.getPropString(KeyVO.MONTH_TYPE, ufoDate.getMonth())
						+ DatePropVO.getPropString(KeyVO.DAY_TYPE, ufoDate.getDay())));

	}
	/**
	 * 计算指标区属函数的值。
	 * 如果指标是当前报表动态区域的指标或者被动态区域引入的指标，或者指标的条件中含有区域那么自行计算，
	 * 如果指标的条件计算结果表示与当前计算环境相同，并且指标与区域关联，那么从区域取值
	 * 如果当前计算环境与批量计算环境一致，返回预处理值，否则自行计算
	 * 创建日期：(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	private UfoVal[] getCountValue(UfoCalcEnv objEnv) throws CmdException {
		ArrayList alPara = getParams();
		
		if(objEnv == null ){
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}

		// 判断指标是否是当前报表动态区域中的指标
		if (isMeasReferDynArea(objEnv)) {
			// 函数引用本表的动态区指标
			return calcDynCountValue(objEnv);
		}

		//b.否则判断条件中是否有区域（包括与区域关联的指标函数），
		IStoreCell[] mvos=getMeasures(objEnv);
		if(isInEnvRep(objEnv) && mvos[0].getDbcolumn() == null){
			// 当前报表中指标没有对应数据表中的列，返回默认值
			return new UfoVal[]{UfoInteger.getInstance(1)};
		}
		UfoExpr objKeyCond = (UfoExpr) alPara.get(1);
		UfoExpr objKeyCondValue = objKeyCond;

		if (objKeyCond != null && MeasFuncDriver.isRelaWithArea(objKeyCond, objEnv)) {
			// 条件表达式与区域相关
			// TODO 统计函数，不需要将区域条件转换为新的条件表达式
			// objKeyCondValue =objKeyCond.solveFixedValue(objEnv);
			return getCountValueFromCache(mvos[0], objKeyCondValue, objEnv);

		} else if(getValue() != null){
			// 取预先计算好的函数值
			return getValue();
		}else{
			// 重新计算函数值
			return calcCountValue(mvos[0], objKeyCond, objEnv);
		}
	}
	
	/**
	 * 如果是固定表、已经进行了预处理指标统计函数用此方法计算
	 * 
	 * @create by liuchuna at 2010-6-7,下午02:24:10
	 *
	 * @param objMeasure
	 * @param objKeyCond
	 * @param objEnv
	 * @return
	 * @throws CmdException
	 */
	private UfoVal[] getCountValueFromCache(IStoreCell objMeasure,
			UfoExpr objKeyCond, UfoCalcEnv objEnv) throws CmdException {
		try {
			if (objMeasure == null) {
				// 计算指标统计函数时，发现指标不存在！
				throw new UfoCmdException("miufo1000406");
			}
			
			// 读取关键字组合PK
			nc.vo.iuforeport.rep.ReportVO objRep = null;
			if(objMeasure instanceof MeasureVO) {
				objRep =(nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(((MeasureVO)objMeasure).getReportPK());
			} else {
//				objRep =(nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(objEnv.getRepPK());
				objRep =(nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(objMeasure.getReportPK());
			}
			
			if (objRep == null) {
				// 指标对应的报表找不到！
				throw new UfoCmdException("miufo1000407");  
			}
			String strKeyGroupPK = objRep.getPk_key_comb();
			//从指标函数驱动中找到指标对应的所有值，并根据函数类型作相应的计算。
			MeasFuncDriver objFuncDriver =
				(MeasFuncDriver) objEnv.loadFuncListInst().getExtDriver(
						MeasFuncDriver.class.getName());
			//首先找到条件对应的数据集和
			Hashtable hashValue = objFuncDriver == null ? null
					: (Hashtable) objFuncDriver.getMeasValue(MeasFuncDriver.MCOUNT);

			if (hashValue != null
					&& hashValue.containsKey(strKeyGroupPK
							+ "\r\n"
							+ (objKeyCond == null ? "" : objKeyCond.toString()
									+ "\r\n"))) {
				Object objValue = ((Hashtable) hashValue.get(strKeyGroupPK
						+ "\r\n"
						+ (objKeyCond == null ? "" : objKeyCond
								.toString(objEnv)
								+ "\r\n"))).get(objMeasure.getCode());
				if (objValue != null) {
					return new UfoVal[] { UfoVal.createVal(objValue) };
				}
			}
			return calcCountValue(objMeasure, objKeyCond, objEnv);

		}catch(ScriptException e){
			AppDebug.debug(e);
			throw new UfoCmdException(e);
		} 
		catch (Exception e) {
			AppDebug.debug(e);
			throw new UfoCmdException(e.getMessage());
		}
	}
	/**
	 * 得到指标对应的关键字组合PK，如果有指标没有关键字组合pk，返回NULL没有找到抛出异常。
	 * 创建日期：(2003-8-7 10:04:39)
	 * @return java.lang.String
	 * @param objMeasureCache nc.pub.iufo.cache.MeasureCache
	 * @exception com.ufsoft.iufo.util.parser.UfoCmdException 异常说明。
	 */
	public String getKeyGroupPK(nc.pub.iufo.cache.MeasureCache objMeasureCache, UfoCalcEnv objEnv)
	throws UfoCmdException {
		try {
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			if (nFID == MeasFuncDriver.FMSELECT
					|| nFID == MeasFuncDriver.FMAVG
					|| nFID == MeasFuncDriver.FMCOUNT
					|| nFID == MeasFuncDriver.FMMAX
					|| nFID == MeasFuncDriver.FMMIN
					|| nFID == MeasFuncDriver.FMSUM
//					|| nFID == MeasFuncDriver.FHBMSELECT
					) {
				MeasOperand objMeas = (MeasOperand) getParams().get(0);//对单个指标取数

				return objMeasureCache.getKeyCombPk(objMeas.getMeasureVO().getCode());

			} else if (nFID == MeasFuncDriver.FMSELECTS
//					|| nFID == MeasFuncDriver.FHBMSELECTS
					) {
				MultiMeasOperand objMeas = (MultiMeasOperand) getParams().get(0);//对多个指标取数

				MeasureVO[] objMeasures = objMeas.getMeasList();
				String strKeyGroupPK = null;
				for (int i = 0; i < objMeasures.length; i++) {

					strKeyGroupPK=objMeasureCache.getKeyCombPk(objMeasures[i].getCode());
					if (strKeyGroupPK != null) {
						return strKeyGroupPK;
					}

				}

			}else if(nFID == MeasFuncDriver.FMSELECTA
//					|| nFID == MeasFuncDriver.FHBMSELECTA
					|| nFID==MeasFuncDriver.FMSUMA){//增加“FHBMSELECTA”
				IStoreCell[] objMeasures = getMeasures(objEnv);
				String strKeyGroupPK = null;
				if( objMeasures != null ){
					for (int i = 0; i < objMeasures.length; i++) {//对区域指标取数
						if( objMeasures[i] != null ){
							//tianchuan 2012.9.20 增加对存储单元的处理
//							strKeyGroupPK = objMeasureCache.getKeyCombPk(objMeasures[i].getCode());
							if(objMeasures[i] instanceof MeasureVO){
								   strKeyGroupPK = objMeasureCache.getKeyCombPk(objMeasures[i].getCode());
							   }
							   else if(objMeasures[i] instanceof StoreCellVO){
								   strKeyGroupPK=((StoreCellVO)objMeasures[i]).getKeyCombPK();
							   } else{
								   strKeyGroupPK=null;
							   }
							if(strKeyGroupPK != null){
								return strKeyGroupPK;
							}
						}
					}

				}
			}
			return null;
		} catch (UfoParseException e) {
			throw new UfoCmdException(e);
		}
	}
	/**
	 * 得到指标对应的关键字组合PK，如果没有找到抛出异常。
	 * 创建日期：(2003-8-7 10:04:39)
	 * @return java.lang.String
	 * @param objReortCache nc.pub.iufo.cache.ReportCache
	 * @exception com.ufsoft.iufo.util.parser.UfoCmdException 异常说明。
	 */
	public static String getKeyGroupPK(
			IStoreCell objMeasure,
			nc.pub.iufo.cache.MeasureCache objMeasureCache)
	throws UfoCmdException {
		/*return objReortCache.getKeyCombByRepMea(
        objMeasure.getReportPK(),
        objMeasure.getCode());*/
		return objMeasure.getKeyCombPK();
//		return objMeasureCache.getKeyCombPk(objMeasure.getCode());
	}

//	/**
//	* 返回函数要计算的所有指标信息.
//	* Creation date: (2003-3-18 14:31:23)
//	* @return nc.vo.ga.measure.MeasureVO[]
//	*/
//	public MeasureVO[] getMeasures() {
//	try{
//	short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
//	if(nFID == MeasFuncDriver.FMAVG
//	|| nFID == MeasFuncDriver.FMCOUNT
//	|| nFID == MeasFuncDriver.FMMAX
//	|| nFID == MeasFuncDriver.FMMIN
//	|| nFID == MeasFuncDriver.FMSELECT
//	|| nFID == MeasFuncDriver.FMSUM
//	|| nFID == MeasFuncDriver.FHBMSELECT
//	){
//	MeasOperand objOperand = (MeasOperand) getParams().get(0);
//	return new MeasureVO[]{objOperand.getMeasureVO()};
//	}else{
//	if(nFID == MeasFuncDriver.FMSELECTS 
//	|| nFID == MeasFuncDriver.FHBMSELECTS){
//	MultiMeasOperand objOperand = (MultiMeasOperand) getParams().get(0);
//	return objOperand.getMeasList();
//	}

//	}
//	return null;
//	}catch(UfoParseException e){
//	e.printStackTrace(System.out);
//	return null;
//	}
//	}
	/**
	 * 返回函数要计算的所有区域指标信息.
	 * Creation date: (2003-3-18 14:31:23)
	 * @param env CalcEnv
	 * @return nc.vo.ga.measure.MeasureVO[]
	 */
	public IStoreCell[] getMeasures(UfoCalcEnv env) throws UfoCmdException{
		try{
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			if(nFID == MeasFuncDriver.FMAVG
					|| nFID == MeasFuncDriver.FMCOUNT
					|| nFID == MeasFuncDriver.FMMAX
					|| nFID == MeasFuncDriver.FMMIN
					|| nFID == MeasFuncDriver.FMSELECT
					|| nFID == MeasFuncDriver.FMSUM
//					|| nFID == MeasFuncDriver.FHBMSELECT
			){
				MeasOperand objOperand = (MeasOperand) getParams().get(0);
				return new MeasureVO[]{objOperand.getMeasureVO()};
			}else if(nFID == MeasFuncDriver.FMSELECTS ){
				MultiMeasOperand objOperand = (MultiMeasOperand) getParams().get(0);
				return objOperand.getMeasList();
			}else if(nFID == MeasFuncDriver.FMSELECTA 
//					|| nFID == MeasFuncDriver.FHBMSELECTA
					|| nFID ==MeasFuncDriver.FMSUMA){
				//新增对函数“FHBMSELECTA”的判断
				if(env == null ){
					return null;
				}
//				List<MeasureVO> meas = new ArrayList<MeasureVO>();
				IStoreCell[] storeCells = getMeasuresByArea((UfoFullArea) getParams().get(0), (UfoCalcEnv) env);
//				for(IStoreCell storeCell : storeCells){
//					if(storeCell instanceof MeasureVO) {
//						meas.add((MeasureVO)storeCell);
//					}
//				}
//				return meas.toArray(new MeasureVO[0]);
				return storeCells;
			}
			return null;

		}catch(UfoParseException e){
			throw new UfoCmdException(e);
		}

	}

	/**
	 * 返回函数要计算的所有区域指标信息.
	 * Creation date: (2003-3-18 14:31:23)
	 * @param env CalcEnv
	 * @return nc.vo.ga.measure.MeasureVO[]
	 */
	public IStoreCell[] getMeasures2(UfoCalcEnv env) throws UfoCmdException{
		try{
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			if(nFID == MeasFuncDriver.FMAVG
					|| nFID == MeasFuncDriver.FMCOUNT
					|| nFID == MeasFuncDriver.FMMAX
					|| nFID == MeasFuncDriver.FMMIN
					|| nFID == MeasFuncDriver.FMSELECT
					|| nFID == MeasFuncDriver.FMSUM
//					|| nFID == MeasFuncDriver.FHBMSELECT
			){
				MeasOperand objOperand = (MeasOperand) getParams().get(0);
				return new MeasureVO[]{objOperand.getMeasureVO()};
			}else if(nFID == MeasFuncDriver.FMSELECTS 
//					|| nFID == MeasFuncDriver.FHBMSELECTS
					){
				MultiMeasOperand objOperand = (MultiMeasOperand) getParams().get(0);
				return objOperand.getMeasList();
			}else if(nFID == MeasFuncDriver.FMSELECTA 
//					|| nFID == MeasFuncDriver.FHBMSELECTA
					|| nFID ==MeasFuncDriver.FMSUMA){
				//新增对函数“FHBMSELECTA”的判断
				if(env == null ){
					return null;
				}
				return getMeasuresByArea2((UfoFullArea) getParams().get(0), (UfoCalcEnv) env);
			}
			return null;

		}catch(UfoParseException e){
			throw new UfoCmdException(e);
		}

	}
	
	/**
	 * 判断指定区域中各单元是否有指标.注意返回结果按照先行后列方式排列
	 * @param objArea
	 * @param objEnv
	 * @param measValues 要求指标值按照先行后列排列
	 * @return
	 * @throws UfoParseException 
	 */
	private UfoVal[] combineEmptyValue(UfoFullArea objArea, UfoCalcEnv objEnv,UfoVal[] measValues)throws UfoCmdException, UfoParseException {

		//记录指标位置信息，对于组合单元上的指标只记录首单元
		List<CellPosition> measCells=new ArrayList<CellPosition>();

		if (objArea.isCurReportArea(objEnv)) {
			// 	判断是否是当前报表区域
			if (objEnv != null && objEnv.getUfoDataChannel() != null) {
				//返回指标及其对应位置（对于组合单元，返回开始单元）
				short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
				Object[][] objMeasurs = getCombAreaMeasure(objEnv,objArea);
//				if(nFID == MeasFuncDriver.FMSELECTA || nFID == MeasFuncDriver.FHBMSELECTA
//						|| nFID ==MeasFuncDriver.FMSUMA){
//					objMeasurs = objEnv.getUfoDataChannel().getExtAreaData(objArea.getArea(),
//							IUFOTableData.AllSTORECELL);
//				} else {
//					objMeasurs = objEnv.getUfoDataChannel().getExtAreaData(objArea.getArea(),
//							IUFOTableData.MEASURE);
//				}
				
				if (objMeasurs != null && objMeasurs.length>=2) {
					String[] strAreas = (String[]) objMeasurs[1];
					if(strAreas!=null && strAreas.length>0){
//						measCells=new ArrayList<CellPosition>(strAreas.length);
						for(int i=0;i<strAreas.length;i++)
							measCells.add(CellPosition.getInstance(strAreas[i]));
					}
				}
			}
		} else {
			Map hashMvos=getOtherRepMeasure(objArea,objEnv);
			if (hashMvos != null && hashMvos.size() > 0) {
				//按照先行后列排列返回指标
				measCells.addAll(hashMvos.keySet());
			}
			List<IArea> cells=seperateArea(objArea,objEnv);
			Map hashStoreCell = getOtherRepStoreCell(objArea,objEnv);
			for(IArea area : cells) {
				if(hashStoreCell.containsKey(area.getStart().toString())) {
					measCells.add(area.getStart());
				}
			}
		}

		int i=0;
		int iMeasureLen=measValues.length;
		List<IArea> cells=seperateArea(objArea,objEnv);
		int iLen=cells==null?0:cells.size();
		UfoVal[] retValues=new UfoVal[iLen];
		for(int index=0;index<iLen;index++){
			IArea areaTemp=cells.get(index);
			if(areaTemp==null)
				continue;
			if(measValues==null || measValues.length==0 || measCells==null || measCells.size()==0){
				retValues[index]=UfoNullVal.getSingleton();
			}else{
				if(measCells.contains(areaTemp.getStart())){
					if(i<iMeasureLen)
						retValues[index]=measValues[i];
					else
						retValues[index]=measValues[measValues.length-1];
					i++;
				}
				else
					retValues[index]=UfoNullVal.getSingleton();
			}
		}

		return retValues;
	}
	
	protected Object[][] getCombAreaMeasure(UfoCalcEnv objEnv,UfoFullArea objArea){
		short nFID;
		try {
			nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			Object[][] objMeasurs = null;
			if(nFID == MeasFuncDriver.FMSELECTA ||nFID ==MeasFuncDriver.FMSUMA){
				objMeasurs = objEnv.getUfoDataChannel().getExtAreaData(objArea.getArea(),
						IUFOTableData.AllSTORECELL);
			} else {
				objMeasurs = objEnv.getUfoDataChannel().getExtAreaData(objArea.getArea(),
						IUFOTableData.MEASURE);
			}
			return objMeasurs;
		} catch (UfoParseException e) {
			AppDebug.debug(e);
		}
		return null;
	}
	
	/**
	 * 按照先行后列排列返回指标
	 * @param objArea
	 * @param objEnv
	 * @return
	 * @throws UfoCmdException
	 * @throws UfoParseException 
	 */
	protected IStoreCell[] getMeasuresByArea(UfoFullArea objArea, UfoCalcEnv objEnv)
	throws UfoCmdException, UfoParseException {
		if (objArea == null)
			return null;

		if (m_measureParam == null || m_measureParam.length < 1) {
//			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			List<IStoreCell> storeCells = new ArrayList<IStoreCell>();
			// 判断是否是当前报表区域
			if (objArea.isCurReportArea(objEnv)) {
				if (objEnv != null && objEnv.getDataChannel() != null) {
					Object[][] objMeasurs = getCombAreaMeasure(objEnv, objArea);
//					if(nFID == MeasFuncDriver.FMSELECTA 
////							|| nFID == MeasFuncDriver.FHBMSELECTA
//							|| nFID ==MeasFuncDriver.FMSUMA){
//						objMeasurs = objEnv.getUfoDataChannel().getExtAreaData(objArea.getArea(),
//								IUFOTableData.AllSTORECELL);
//					} else {
//						objMeasurs = objEnv.getUfoDataChannel().getExtAreaData(objArea.getArea(),
//								IUFOTableData.MEASURE);
//					}
					
					if (objMeasurs != null && objMeasurs[0] != null) {
						storeCells.addAll(Arrays.asList((IStoreCell[]) objMeasurs[0]));
					}
				}
			} else {

				Map hashMvos=getOtherRepMeasure(objArea,objEnv);
				Map hashStoreCell = getOtherRepStoreCell(objArea,objEnv);
				if (hashMvos != null && hashMvos.size() > 0) {
					//按照先行后列排列返回指标
					List<IArea> cellList = new ArrayList<IArea>(hashMvos.keySet());
					Collections.sort(cellList);
					String[] strMeasurePKs = new String[hashMvos.size()];
					int i = 0;
					MeasureVO mvoTemp=null;
					for(IArea pos : cellList){
						mvoTemp=(MeasureVO) hashMvos.get(pos);
						
						if(mvoTemp != null) {
							strMeasurePKs[i] =mvoTemp.getCode();
							storeCells.add(objEnv.getMeasureCache().getMeasure(mvoTemp.getCode()));
						} else if(hashStoreCell != null) {
							IStoreCell storeCell = (IStoreCell)hashStoreCell.get(pos.toString());
							storeCells.add(storeCell);
						}
						
						
						i++;
					}
//					mvos = objEnv.getMeasureCache().getMeasures(strMeasurePKs);
				}
				
				if (hashStoreCell != null && hashStoreCell.size() > 0) {
					CellPosition[] cells = objArea.getArea().split();
					//按照先行后列排列返回指标
					IStoreCell storecell = null;
					for(CellPosition cell : cells) {
						storecell = (IStoreCell)hashStoreCell.get(cell.toString());
						if(storecell != null) {
							storeCells.add(storecell);
						}
					}
				}
			}
			m_measureParam = storeCells.toArray(new IStoreCell[0]);
		}

		return m_measureParam;
	}
	
	/**
	 * 按照先行后列排列返回指标，该方法同上面方法不同之处在于：
	 * 如果区域对应位置无指标，则返回空指标，该方法为数据追踪提供
	 * 
	 * @param objArea
	 * @param objEnv
	 * @return
	 * @throws UfoCmdException
	 */
	protected IStoreCell[] getMeasuresByArea2(UfoFullArea objArea, UfoCalcEnv objEnv)
	throws UfoCmdException {
		if (objArea == null)
			return null;

		CellPosition[] cellList = objArea.getArea().split();
		IStoreCell[] mvos = new IStoreCell[cellList.length];
		// 判断是否是当前报表区域
		if (objArea.isCurReportArea(objEnv)) {
			if (objEnv != null && objEnv.getDataChannel() != null) {
				Object[][] objMeasurs = objEnv.getUfoDataChannel()
				.getExtAreaData(objArea.getArea(),
						IUFOTableData.MEASURE);
				if (objMeasurs != null && objMeasurs[0] != null) {
					mvos = (IStoreCell[]) objMeasurs[0];
				}
			}
		} else {
			Map hashMvos = getOtherRepMeasure(objArea, objEnv);
			if (hashMvos != null && hashMvos.size() > 0) {
				//按照先行后列排列返回指标
				List<IArea> mcellList = new ArrayList<IArea>(hashMvos.keySet());
				Collections.sort(mcellList);
				String[] strMeasurePKs = new String[hashMvos.size()];
				int i = 0;
				IStoreCell mvoTemp=null;
				for(IArea pos : mcellList){
					mvoTemp=(IStoreCell) hashMvos.get(pos);
					strMeasurePKs[i] =mvoTemp.getCode();
					i++;
				}
				for(i = 0 ;i < mvos.length; i++){
					mvoTemp = (IStoreCell)hashMvos.get(cellList[i]);
					if(mvoTemp!= null){
						mvos[i] = objEnv.getMeasureCache().getMeasure(mvoTemp.getCode());
					} else{
						mvos[i] = null;
					}						
				}					
			} else{
				for(int i = 0 ;i < mvos.length; i++){
					mvos[i] = null;
				}
			}
		}

		return mvos;
	}
	
	/**
	 * 返回指定区域对应的单元集合，对于组合单元只返回一个单元，且为组合单元整体位置.
	 * 要求返回区域按照先行后列排列
	 * @param objArea
	 * @param objEnv
	 * @return
	 * @throws UfoCmdException
	 */
	private List<IArea> seperateArea(UfoFullArea objArea, UfoCalcEnv objEnv)throws UfoCmdException{
		List<IArea> cellList=null;
		if (objArea.isCurReportArea(objEnv)) {
			if (objEnv != null && objEnv.getDataChannel() != null) {
				//对于组合单元，返回组合单元位置名
				String[] strAreas  = objEnv.getUfoDataChannel()
				.getSplitAreaNames(objArea.getArea());
				if(strAreas!=null && strAreas.length>0){
					cellList=new ArrayList<IArea>(strAreas.length);
					for(int i=0;i<strAreas.length;i++)
						cellList.add(AreaPosition.getInstance(strAreas[i]));
				}

			}
		} else {
			CellsModel formatModel =getOtherCellsModel(objArea,objEnv);
			cellList=formatModel==null?null:formatModel.seperateArea(objArea.getArea());
		}
		return cellList;

	}

	private CellsModel getOtherCellsModel(UfoFullArea objArea, UfoCalcEnv objEnv)throws UfoCmdException{
		// 得到报表缓存和报表格式缓存
		RepFormatModelCache repFormatCache = objEnv.getRepFormatCache();
		ReportCache repCache = objEnv.getReportCache();

		// 得到报表PK
		String strRepCode = null;
		try {
			strRepCode = objArea.getReportCode(objEnv);
		} catch (CmdException e) {
			throw new UfoCmdException(e);
		}
		String strRepPK = repCache.getRepPKByCode(strRepCode);
		if (strRepPK == null) {
			// 编码为｛｝的报表不存在
			String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufocalc000542",null,
					new String[] { strRepCode });
			throw new UfoCmdException(msg);
		}

		// 得到报表格式模型

		CellsModel formatModel = repFormatCache
		.getUfoTableFormatModel(strRepPK);

		if (formatModel == null) {
			// 报表{}格式尚未定义
			String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufocalc000543",null,
					new String[] { strRepCode });
			throw new UfoCmdException(msg);
		}
		return formatModel;
	}
	/**
	 * 返回他表区域对应的指标
	 * @param objArea
	 * @param objEnv
	 * @return
	 * @throws UfoCmdException
	 */
	private Map getOtherRepMeasure(UfoFullArea objArea, UfoCalcEnv objEnv)throws UfoCmdException{

		CellsModel formatModel =getOtherCellsModel(objArea,objEnv);
		//tianchuan 根据区域的位置是在动态区还是固定区，来决定取哪里的指标
		if(formatModel==null){
			return null;
		}
		DynamicAreaModel dynModel=DynamicAreaModel.getInstance(formatModel);
		//以第一个单元格为基准，判断是在动态区还是固定区
		ExtendAreaCell exCell = dynModel.getDynAreaCellByFmtPos(objArea.getArea().getStart());
		Map hashMvos = null;
		if(exCell==null){	//选的区域位于固定区
			/**
			 * key=CellPosition,value=MeasureVO
			 */
			hashMvos = CellsModelOperator.getMeasureModel(formatModel)
					.getMainMeasureVOByArea(objArea.getArea());
		}else{	//不为空，说明区域位于动态区
			hashMvos = CellsModelOperator.getMeasureModel(formatModel).getDynAreaMeasureVOByArea(
					exCell.getExAreaPK(), objArea.getArea());
		}
		return hashMvos;
	}
	
	private Map getOtherRepStoreCell(UfoFullArea objArea, UfoCalcEnv objEnv)throws UfoCmdException{
		
		ReportCache repCache = objEnv.getReportCache();

		// 得到报表PK
		String strRepCode = null;
		try {
			strRepCode = objArea.getReportCode(objEnv);
		} catch (CmdException e) {
			throw new UfoCmdException(e);
		}
		String strRepPK = repCache.getRepPKByCode(strRepCode);
		if (strRepPK == null) {
			// 编码为｛｝的报表不存在
			String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufocalc000542",null,
					new String[] { strRepCode });
			throw new UfoCmdException(msg);
		}
		
		Map<String, Hashtable<String, IStoreCell>> storeCellMap = objEnv.getStoreCellMap();
		if(storeCellMap.containsKey(strRepPK)) {
			return storeCellMap.get(strRepPK);
		} else {
			IStoreCellPackQrySrv storeCellQrySrv = NCLocator.getInstance().lookup(IStoreCellPackQrySrv.class);
			Hashtable<String, IStoreCell> storecells = null;
			try {
				storecells = storeCellQrySrv.getStoreCellsByRepID(strRepPK);
			} catch (UFOSrvException e) {
				AppDebug.debug(e);
			}
			storeCellMap.put(strRepPK, storecells);
			return storecells;
		}
	}
	
	/**
	 * 此方法在审核公式table.check执行时使用。
	 * @param objArea
	 * @param objEnv
	 * @return
	 * @throws UfoCmdException
	 */
	protected String[] getCellsWithMeasure(UfoFullArea objArea, UfoCalcEnv objEnv) throws UfoCmdException
	{	
		if( objArea != null ){
			//判断是否是当前报表区域
			if( objArea.isCurReportArea(objEnv)){
				if( objEnv != null && objEnv.getUfoDataChannel() != null){
					Object[][] objs=objEnv.getUfoDataChannel().getExtAreaData(objArea.getArea(),IUFOTableData.MEASURE);
					if(objs!=null && objs.length>1 && objs[1]!=null){
						return (String[])objs[1];
					}
				}

			}else{
				//得到报表缓存和报表格式缓存
				nc.pub.iufo.cache.RepFormatModelCache repFormatCache = objEnv.getRepFormatCache();
				nc.pub.iufo.cache.ReportCache  repCache = objEnv.getReportCache();

				//得到报表PK
				String strRepCode = null;
				try{
					strRepCode = objArea.getReportCode(objEnv);
				}catch(CmdException e){
					throw new UfoCmdException(e);
				}
				String strRepPK = repCache.getRepPKByCode(strRepCode);
				if( strRepPK == null){
					//编码为｛｝的报表不存在
					String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufocalc000542",null,
							new String[]{strRepCode});
					throw new UfoCmdException(msg);
				}
				//得到报表格式模型
//				UfoTableFormatModel formatModel = null;
//				formatModel = repFormatCache.getUfoTableFormatModel(strRepPK);
//				if( formatModel != null){
//				return formatModel.getCellsWithMeasure(objArea.getArea().toString());
//				}
				CellsModel formatModel= repFormatCache.getUfoTableFormatModel(strRepPK);
				Map hashMvos=CellsModelOperator.getMeasureModel(formatModel).getMainMeasureVOByArea(objArea.getArea());

				if(hashMvos!=null && hashMvos.size()>0){//modified by weixl, 2008-01-17
			        List<IArea> keyList = new ArrayList<IArea>(hashMvos.keySet());
			        Collections.sort(keyList);
					String[] strMeasureAreas = new String[hashMvos.size()];
					Iterator iter=keyList.iterator();
					int i=0;
					while(iter.hasNext()){
						strMeasureAreas[i]=((IArea) iter.next()).toString();
						i++;
					}
					return strMeasureAreas;
				}
			}

		}

		return null;
	}

	/**
	 * 得到指标对应的关键字组合PK，如果没有找到抛出异常。
	 * 创建日期：(2003-8-7 10:04:39)
	 * @return java.lang.String
	 * @param objReortCache nc.pub.iufo.cache.ReportCache
	 * @exception com.ufsoft.iufo.util.parser.UfoCmdException 异常说明。
	 */
	public UfoVal[] getNullVal(UfoCalcEnv objEnv)
	throws UfoCmdException {
		try {
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			if (nFID == MeasFuncDriver.FMSELECTS 
					|| nFID == MeasFuncDriver.FMSELECTA
//					|| nFID == MeasFuncDriver.FHBMSELECTA
					) {//新增对函数“FHBMSELECTA”的判断
				IStoreCell[] objMeasures = getMeasures(objEnv);
				UfoVal[] objVals = new UfoVal[objMeasures.length];
				for (int i = 0; i < objMeasures.length; i++) {
					objVals[i] = UfoNullVal.getSingleton();
				}
				return objVals;
			}
			return new UfoVal[] { UfoNullVal.getSingleton()};
		} catch (UfoParseException e) {
			throw new UfoCmdException(e);
		}
	}


//	/**
//	* 此处插入方法描述。
//	* 创建日期：(2003-8-7 10:34:29)
//	* @return java.lang.String
//	*/
//	public String getReportPK() {
//	try{
//	short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
//	if (nFID == MeasFuncDriver.FMSELECT
//	|| nFID == MeasFuncDriver.FMAVG
//	|| nFID == MeasFuncDriver.FMCOUNT
//	|| nFID == MeasFuncDriver.FMMAX
//	|| nFID == MeasFuncDriver.FMMIN
//	|| nFID == MeasFuncDriver.FMSUM) {
//	MeasOperand objMeas = (MeasOperand) getParams().get(0);
//	return objMeas.getMeasureVO().getReportPK();
//	} else if (nFID == MeasFuncDriver.FMSELECTS
//	|| nFID == MeasFuncDriver.FHBMSELECTA) {//增加"HBMSELECTA"
//	MultiMeasOperand objMeas = (MultiMeasOperand) getParams().get(0);
//	return objMeas.getMeasList()[0].getReportPK();
//	}
//	return null;
//	}catch(UfoParseException e){
//	e.printStackTrace(System.out);
//	return null;
//	}
//	}
	/**
	 * 返回函数定义的返回值类型。
	 * 创建日期：(2002-5-29 16:17:28)
	 * @return byte
	 */
	public byte getReturnType()
	{
		try{
			UfoFuncInfo  finfo = MeasFuncDriver.FUNCLIST[MeasFuncDriver.getFuncIdByName(getFuncName()) - 1];
			return finfo.getReturnType();
		}catch(UfoParseException e){
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			return IFuncType.VALUE;
		}
	}
	/**
	 * 计算指标区属函数的值。
	 * 如果指标是当前报表动态区域的指标或者被动态区域引入的指标，或者指标的条件中含有区域那么自行计算，
	 * 如果指标的条件计算结果表示与当前计算环境相同，并且指标与区域关联，那么从区域取值
	 * 如果当前计算环境与批量计算环境一致，返回预处理值，否则自行计算
	 * 创建日期：(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	protected UfoVal[] getSelectValue(UfoCalcEnv objEnv) throws CmdException {
		try {

			if (objEnv == null) {
				throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
			}
			UfoCalcEnv env = (UfoCalcEnv) objEnv;
			//判断指标是否是当前报表动态区域中的指标，
			if (isMeasReferDynArea(env)) {
				//a.如果是，调用动态区域指标取数函数计算方法；
				return calcDynSelectValue(env);
			}

			MeasFuncDriver objFuncDriver =
				(MeasFuncDriver) objEnv.loadFuncListInst().getExtDriver(
						MeasFuncDriver.class.getName());
			if(objFuncDriver == null){
			    objFuncDriver = (MeasFuncDriver) objEnv.loadFuncListInst().getExtDriver("nc.util.hbbb.func.AdjustMeasFuncDriver");
			}
			String strKeyGroupPK = getKeyGroupPK(objFuncDriver.getMeasCache(), objEnv);

			boolean bSameTask=true;
//			boolean bHB=isHBfunc();
//			if(bHB==true){
////				对于hb函数,比较任务是否相同, add by ljhua 2006-8-16
//				String strParamTask=env.getMeasureEnv().getFormulaID();
//				String strEnvTask=(String) env.getExEnv(UfoCalcEnv.EX_TASK_ID);
//				if(strEnvTask==null ||  strEnvTask.equals(strParamTask))
//					bSameTask=true;
//				else
//					bSameTask=false;
//			}
			
			boolean bOnlyOneParam=isOnlyOneParamMselect();
			//tianchuan 对版本的预计算
			Integer preVer=null;
			if(bOnlyOneParam){
				preVer = getVerParamVal(objEnv);
				if(preVer!=null && preVer>0){	//如果能够取出版本，那么视为版本不为空，参数不唯一
					bOnlyOneParam=false;
				}
			}
			

			//判断参数个数是否为1，
			if (bOnlyOneParam) {
				//判断指标是否所属当前报表，
				if (isInEnvRep(objEnv)) {
					//如果指标为当前报表主表指标,则直接从区域取值；
					strKeyGroupPK =
						(objEnv.getMeasureEnv() == null ? null : objEnv.getMeasureEnv().getKType());
					return getSelectValueFromArea(
							strKeyGroupPK,
							null,
							null,
							null,
							null,
							objEnv);
				} else if (strKeyGroupPK == null) { //如果存在没有关键字组合PK的指标，并且所有指标都不与区域关联，那么错误
					throw new UfoCmdException("miufo1000408");  //"指标不正确！"
				} else {
					if (env.isMeasureTrace() || getValue() == null
							|| (getValueKey() != null && !getValueKey().equals(objEnv.getMeasureEnv()))) {
//						Integer nVer =
//							(objEnv.getMeasureEnv() == null
//									? null
//											: Integer.valueOf(objEnv.getMeasureEnv().getVer()));
						Integer nVer = getVerParamVal(objEnv);
						return calcSelectValue(getMeasures(objEnv), null, null, null, nVer, objEnv);
					} else {
						return getValue();
					}
				}
			} else {
				//b.否则判断条件中是否有区域（包括与区域关联的指标函数），
				boolean bRelaArea = false;
				UfoExpr objOffset = getOffsetParamVal();
				//[a].如果有排除；
				if (objOffset != null && MeasFuncDriver.isRelaWithArea(objOffset, objEnv)) {
					bRelaArea = true;
					;
				}
				UfoExpr objKeyCond = getKeyCondParamVal();
				if (objKeyCond != null && MeasFuncDriver.isRelaWithArea(objKeyCond, objEnv)) {
					bRelaArea = true;
					;
				}
				DatePropVO objDateProp = getDatePropParamVal();
				//[b].否则计算条件的值，包括偏移量和关键字条件，
				Double nOffset = null;
				if (objOffset != null) {
					nOffset = new Double(objOffset.getValue(objEnv)[0].doubleValue());
				}
				UfoExpr objKeyCondValue = null;
				boolean bKeyCondSame = true;
				//优先从预计算的结果中取
				Integer nVer = null;
				if(preVer!=null && preVer>0){
					nVer=preVer;
				}else{
					nVer = getVerParamVal(objEnv);
				}

				if (objKeyCond != null) {
					UfoEElement[] objEles = objKeyCond.getElements();
					UfoEElement[] objNewEles =
						objEles == null ? null : new UfoEElement[objEles.length];
					bKeyCondSame = checkKeyCondWithEnv(objKeyCond, objNewEles, objEnv);
					objKeyCondValue =
						new UfoExpr(objNewEles, objKeyCond.getType(), objKeyCond.getStatus());
				}
				
				//如果是公式追踪
				IStoreCell[] mvos = null;
				if(env.isMeasureTrace()){
					mvos = getMeasures2(objEnv);
				} else {
					mvos = getMeasures(objEnv);
				}
				
				try {
					short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
					if(nFID == MeasFuncDriver.FMSELECTA 
//							|| nFID == MeasFuncDriver.FHBMSELECTA
							|| nFID ==MeasFuncDriver.FMSUMA){
						mvos = getMeasuresByArea((UfoFullArea) getParams().get(0), (UfoCalcEnv) env);
					}
				} catch (UfoParseException e) {
					throw new UfoCmdException(e);
				}
				
				//判断指标是否当前报表指标,并且与当前计算环境是否一致
				if (isInEnvRep(objEnv)
						&& (nOffset == null || nOffset.intValue() == 0)
						&& (nVer == null
								|| (nVer.intValue()
										== (objEnv.getMeasureEnv() == null ? 0 : objEnv.getMeasureEnv().getVer())))
										&& (objKeyCond == null || bKeyCondSame)
										&& bSameTask ) {
					//{a}如果一致，从报表中取数，
					strKeyGroupPK =
						(objEnv.getMeasureEnv() == null ? null : objEnv.getMeasureEnv().getKType());
					return getSelectValueFromArea(
							strKeyGroupPK,
							objDateProp,
							nOffset,
							objKeyCondValue,
							nVer,
							(UfoCalcEnv) objEnv);
				} else if (strKeyGroupPK == null) {
					throw new UfoCmdException("miufo1000408");  //"指标不正确！"
				} else if (bRelaArea) {

					return getSelectValueFromCache(
							strKeyGroupPK,
							mvos,
							objDateProp,
							nOffset,
							objKeyCondValue,
							nVer,
							objEnv);
				} else if (env.isMeasureTrace() || getValue() == null) {
					//{b}否则，返回预处理值。
					return calcSelectValue(
							mvos,
							objDateProp,
							nOffset,
							objKeyCondValue,
							nVer,
							objEnv);
				} else {
					return getValue();

				}
			}
		} catch (UfoValueException e) {
			throw new UfoCmdException(e);
		}
	}
	/**
	 * 获得日期属性参数的值
	 * @return
	 */
	private DatePropVO getDatePropParamVal() {
		ArrayList alPara = getParams();	
//		if(getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECT) 
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTS)
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTA)){//增加“HBMSELECTA”
//			return (DatePropVO) alPara.get(2);
//		}else{
			return (DatePropVO) alPara.get(1);
//		}
	}
	/**
	 * 获得关键字条件参数的值
	 * @return
	 */
	protected UfoExpr getKeyCondParamVal() {
		ArrayList alPara = getParams();
//		if(getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECT) 
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTS)
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTA)){//增加“HBMSELECTA”
//			return (UfoExpr) alPara.get(4);
//		}
//		else{
			return (UfoExpr) alPara.get(3);
//		}	
	}

	/**
	 * 如果条件与当前计算环境一致，并且指标中有与区域关联的，使用此方法计算。
	 * 创建日期：(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	private UfoVal[] getSelectValueFromArea(String strKeyGroupPK,
			DatePropVO objDateProp, Double nOffset, UfoExpr objKeyCondValue,
			Integer nVer, UfoCalcEnv objEnv) throws CmdException {

		//指标追踪可能也会调度计算过程。liuyy.
		measureTrace(objEnv);
		
		try {
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			IStoreCell[] objMeasures = getMeasures(objEnv);
			
			if(nFID == MeasFuncDriver.FMSELECTA 
//					|| nFID == MeasFuncDriver.FHBMSELECTA
					|| nFID ==MeasFuncDriver.FMSUMA){
				objMeasures = getMeasuresByArea((UfoFullArea) getParams().get(0), (UfoCalcEnv) objEnv);
			}

			if (objEnv.getDataChannel() == null) {
//			if (objEnv == null || objEnv.getDataChannel() == null) {
				// 如果数据通道为空，则从缓存或数据库中获得数据
				return getSelectValueFromCache(strKeyGroupPK, objMeasures,
						objDateProp, nOffset, objKeyCondValue, nVer, objEnv);
			}
			if (nFID == MeasFuncDriver.FMSELECT
					|| nFID == MeasFuncDriver.FMSELECTA
//					|| nFID == MeasFuncDriver.FHBMSELECT
//					|| nFID== MeasFuncDriver.FHBMSELECTA
					) {
				if (objMeasures != null) {
					int iMeasureNum = objMeasures.length;
					UfoVal[] objVals = new UfoVal[iMeasureNum];
					for (int i = 0; i < iMeasureNum; i++) {
						if(objMeasures[i] instanceof MeasureVO) {
							if (objEnv.isMainMeasure(objMeasures[i].getCode())) {
								objVals[i] = objEnv.getMainMeasureValue(objMeasures[i].getCode());
							} else {
								objVals[i] = getSelectValueFromCache(strKeyGroupPK,
										new IStoreCell[] { objMeasures[i] },
										objDateProp, nOffset, objKeyCondValue,
										nVer, objEnv)[0];
							}
						} else {
							if (objEnv.isMainStorecell(CellPosition.getInstance(objMeasures[i].getCode()))) {
								objVals[i] = objEnv.getMainStorecellValue(objMeasures[i].getCode());
							} else {
								// 取存储单元的值
								objVals[i] = getSelectValueFromCache(strKeyGroupPK,
										new IStoreCell[] { objMeasures[i] },
										objDateProp, nOffset, objKeyCondValue,
										nVer, objEnv)[0];
							}
						}
					}
					return objVals;
				} else {
					return new UfoVal[] { UfoNullVal.getSingleton() };
				}

			} else if (nFID == MeasFuncDriver.FMSELECTS
//					|| nFID== MeasFuncDriver.FHBMSELECTS
					) {
				if (objMeasures != null) {
					boolean bNotAllInRep = false;
					boolean[] bNotInRep = new boolean[objMeasures.length];
					UfoVal[] objVals = new UfoVal[objMeasures.length];
					IStoreCell[] objMeasClone = new IStoreCell[objMeasures.length];
					for (int i = 0; i < objMeasures.length; i++) {
						if (objEnv.isMainMeasure(objMeasures[i].getCode())) {
							objVals[i] = objEnv.getMainMeasureValue(objMeasures[i].getCode());
						} else {
							bNotAllInRep = true;
							bNotInRep[i] = true;
							objMeasClone[i] = objMeasures[i];
						}

					}
					if (bNotAllInRep) {
						UfoVal[] objValInDB = getSelectValueFromCache(
								strKeyGroupPK, objMeasClone, objDateProp,
								nOffset, objKeyCondValue, nVer, objEnv);
						for (int i = 0; i < bNotInRep.length; i++) {
							if (bNotInRep[i]) {
								objVals[i] = objValInDB[i];
							}
						}
					}
					return objVals;
				} else {
					return new UfoVal[] { UfoNullVal.getSingleton() };
				}
			}
			// modify by ljhua 2006-8-16 解决死循环问题
			// return getValue(objEnv);
			return new UfoVal[] { UfoNullVal.getSingleton() };

		} catch (UfoParseException e) {
			throw new UfoCmdException(e);
		}
	}
	/**
	 * 如果条件与当前计算环境一致，并且指标中有与区域关联的，使用此方法计算。 创建日期：(2003-8-7 15:28:32)
	 * 
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv
	 *            com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException
	 *                异常说明。
	 */
	protected UfoVal getSelectValueFromArea(IStoreCell objMeasure, UfoCalcEnv env)
	throws CmdException {


		if (env.getDataChannel() == null) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		if(objMeasure instanceof MeasureVO) {
			if(env.isMainMeasure(objMeasure.getCode())){
				return env.getMainMeasureValue(objMeasure.getCode());
			}
			else {
				String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufo1000409",null,
						new String[]{getMeasures(env)[0].getCode()});
				throw new UfoCmdException(msg);
			}
		} else {
			if(env.isMainStorecell(CellPosition.getInstance(objMeasure.getCode()))){
				return env.getMainStorecellValue(objMeasure.getCode());
			}
			else {
				String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufo1000409",null,
						new String[]{getMeasures(env)[0].getCode()});
				throw new UfoCmdException(msg);
			}
		}

//		UfoCalcEnv objEnv = (UfoCalcEnv) env;
//		if (objEnv.getReportEnv() == null
//		|| objEnv.getReportEnv().m_dataChannel == null
//		|| objEnv.getReportEnv().m_curReport == null
//		|| objEnv.getReportEnv().m_curPage == null) {
//		throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
//		}
//		IUfoDataChannel dataChannel = objEnv.getReportEnv().m_dataChannel;
//		UfoArea a =
//		dataChannel.getMeasureCell(
//		objEnv.getReportEnv().m_curReport,
//		objMeasure.getCode());
//		if (a != null) {
//		return dataChannel.getAreaData(
//		objEnv.getReportEnv().m_curReport,
//		objEnv.getReportEnv().m_curPage,
//		a)[0];

//		} else {
//		throw new UfoCmdException("miufo1000409", new String[]{getMeasures()[0].getName()});  //"指标" + getMeasures()[0].getName() + "与区域无关！"
//		}

	}
	/**
	 * 如果相同条件的指标值已经取到缓存中(通过批量计算方式已放入缓存)，那么直接读数，否则调用BO从数据库中获得结果。
	 * 创建日期：(2003-8-8 10:06:43)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures nc.vo.iufo.measure.MeasureVO[]
	 * @param objDateProp nc.vo.iufo.keyword.DatePropVO
	 * @param nOffset java.lang.Double
	 * @param objKeyCond com.ufsoft.iufo.util.expression.UfoExpr
	 * @param nVer java.lang.Integer
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	private UfoVal[] getSelectValueFromCache(
			String strKeyGroupPK,
			IStoreCell[] objMeasures,
			DatePropVO objDateProp,
			Double nOffset,
			UfoExpr objKeyCond,
			Integer nVer,
			UfoCalcEnv objEnv)
	throws CmdException {

		try {
			MeasFuncDriver objFuncDriver =
				(MeasFuncDriver) objEnv.loadFuncListInst().getExtDriver(
						MeasFuncDriver.class.getName());
			//判断是否为hb函数
//			boolean bHB=isHBfunc();

			//指标追踪可能也会调度计算过程。liuyy.
			measureTrace(objEnv);
			
			//hashSelectValue是通过函数批量计算(calcFuncValues)获得
			Hashtable hashSelectValue =null;
			if(objFuncDriver!=null){
//				if(bHB)
					hashSelectValue=(Hashtable) objFuncDriver.getMeasValue(MeasFuncDriver.MSELECT);
//				else
//					hashSelectValue=(Hashtable) objFuncDriver.getMeasValue(MeasFuncDriver.HBMSELECT);

			}

			StringBuffer strCond = new StringBuffer();
			strCond.append(strKeyGroupPK);
			strCond.append("\r\n");
			//add by ljhua 2006-8-16 对于hbmselect函数，增加任务id
//			if(bHB){
//				strCond.append(objEnv.getMeasureEnv().getFormulaID());
//				strCond.append("\r\n");
//			}

			if(objDateProp != null && objDateProp.getName() != null){
				strCond.append(objDateProp.getName());
			}
			strCond.append("\r\n");
			if(nOffset != null){
				strCond.append(nOffset);
			}
			strCond.append("\r\n");
			if (objKeyCond != null) {
				strCond.append(objKeyCond.toString(objEnv));
			}
			strCond.append("\r\n");
			if(nVer != null){
				strCond.append(nVer);
			}
			strCond.append("\r\n");

			//	short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			if (hashSelectValue != null && hashSelectValue.size() > 0) {
				Hashtable hashValue = (Hashtable) hashSelectValue.get(strCond.toString());
				if (hashValue != null) {
					UfoVal[] objVals = new UfoVal[objMeasures.length];
					for (int j = 0; j < objMeasures.length; j++) {
						if(objMeasures[j] == null){
							objVals[j] = UfoNullVal.getSingleton();
							continue;
						}
						Object objValue = hashValue.get(objMeasures[j].getCode());
						if (objValue != null) {
							objVals[j] = UfoVal.createVal(objValue);
						} else {
							return calcSelectValue(
									objMeasures,
									objDateProp,
									nOffset,
									objKeyCond,
									nVer,
									objEnv);
						}
					}

					return objVals;
				}
			}
			return calcSelectValue(
					objMeasures,
					objDateProp,
					nOffset,
					objKeyCond,
					nVer,
					objEnv);

		}catch(ScriptException e){
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		} 
		catch (Exception e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
	}
	/**
	 * 计算指标区属函数的值。
	 * 如果指标是当前报表动态区域的指标或者被动态区域引入的指标，或者指标的条件中含有区域那么自行计算，
	 * 如果指标的条件计算结果表示与当前计算环境相同，并且指标与区域关联，那么从区域取值
	 * 如果当前计算环境与批量计算环境一致，返回预处理值，否则自行计算
	 * 创建日期：(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	protected UfoVal[] getStatValue(UfoCalcEnv objEnv) throws CmdException {
		java.util.ArrayList alPara = getParams();

		if(objEnv == null ){
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		if ((objEnv instanceof ReportDynCalcEnv)) {
			ReportDynCalcEnv env = (ReportDynCalcEnv) objEnv;
			if (env.getDataChannel()!=null && isMeasReferDynArea(env)) {
				//1.如果指标是否是当前报表动态区域中的指标
				return calcDynStatValue(env,(UfoExpr) getParams().get(1));
			}
		}
		//2.如果指标所属当前报表,且指标未有数据库字段
		IStoreCell[] mvos=getMeasures(objEnv);
		if (isInEnvRep(objEnv) && mvos[0].getDbcolumn() == null) {
			return new UfoVal[] { getSelectValueFromArea(mvos[0], objEnv)};
		}
		//3.否则判断条件中是否有区域（包括与区域关联的指标函数），
		UfoExpr objKeyCond = (UfoExpr) alPara.get(1);
		UfoExpr objKeyCondValue = objKeyCond;

		UfoVal[] valReturn=null;
		if (MeasFuncDriver.isRelaWithArea(objKeyCond, objEnv)) {
            // 统计函数不需要转换新的条件表达式
//			objKeyCondValue = objKeyCond.solveFixedValue(objEnv);
			if (isInEnvRep(objEnv)) {
				//如果指标在当前报表内，则自行计算
				valReturn= calcStatValues(mvos, objKeyCondValue, objEnv);
			} else {
				//否则,调用预处理值。如预处理值为空，则自行计算。
				if(mvos!=null && mvos.length>0){
					valReturn=new UfoVal[mvos.length];
					for(int i=0;i<mvos.length;i++)
						valReturn[i]= getStatValueMeasNotRelaArea(mvos[i], objKeyCondValue, objEnv);
				};
			}

		} else {
			if (isInEnvRep(objEnv)) {
				//指标所属当前报表
				// 判断计算过程是否用大数值类型处理
				boolean isBigNumber = isBigNumber(objEnv);
				if(mvos[0].getType() == IStoreCell.TYPE_BIGDECIMAL || isBigNumber) {
					valReturn= getStatValueFromCacheByUFDouble(mvos, objKeyCondValue, objEnv);
				} else {
					valReturn= getStatValueFromCache(mvos, objKeyCondValue, objEnv);
				}
			} else if (getValue() != null) {
				//指标值已获得，则直接取
				valReturn= getValue();
			} else {
				//它表指标，且指标值未获得时
				if(objEnv instanceof ReportDynCalcEnv){
					valReturn= calcDynStatValue((ReportDynCalcEnv)objEnv,objKeyCondValue);
				}else{
					valReturn=calcStatValues(mvos, objKeyCondValue, objEnv);
				}  
			}
		}

		return valReturn;

	}
	/**
	 * 如果是固定表、已经进行了预处理指标统计函数用此方法计算。
	 * 创建日期：(2003-8-8 10:06:43)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures nc.vo.iufo.measure.MeasureVO
	 * @param objKeyCond com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException 异常说明。
	 */
	protected UfoVal[] getStatValueFromCache(
			IStoreCell[] objMeasures,
			UfoExpr objKeyCond,
			UfoCalcEnv objEnv)
	throws CmdException {
		try {

			if (objMeasures == null || objMeasures.length==0) {
				throw new UfoCmdException("miufo1000406"); //"计算指标统计函数时，发现指标不存在！"
			}
			
			//tianchuan
			//指标追踪可能也会调度计算过程。liuyy.
			measureTrace(objEnv);
			
			int iMeasureLen=objMeasures.length;
			
			//1.判断当前计算环境是否符合条件
			boolean bCurEnvPropt = false;
			if (objKeyCond == null) {
				bCurEnvPropt = true;
			} else {
				bCurEnvPropt = objKeyCond.calcExpr(objEnv)[0].doubleValue() == 1;
			}

			int[]  nCount = new int[iMeasureLen];
			//1.1 设置初始值

			double[] dValue = new double[iMeasureLen];
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			for(int i=0;i<iMeasureLen;i++){
				if (nFID == MeasFuncDriver.FMMAX) {
					dValue[i] = Double.MIN_VALUE;
				} else if (nFID == MeasFuncDriver.FMMIN) {
					dValue[i] = Double.MAX_VALUE;
				}else
					dValue[i]=0;
			}
			//1.2获得当前关键字值字串strCurKeyValue
			String strCurKeyValue = null;
			//如果当前计算环境符合条件并且指标与区域关联，那么取出关联区域值
//			if (objEnv instanceof UfoCalcEnv) {
//				UfoCalcEnv env = (UfoCalcEnv) objEnv;
//				if (bCurEnvPropt) {
					//2.1获得当前指标区域的数值dValue
//					if (env != null && objEnv.getUfoDataChannel() != null) {
						/*
						IUfoDataChannel dataChannel = env.getReportEnv().m_dataChannel;
						UfoArea a = dataChannel.getMeasureCell(env
								.getReportEnv().m_curReport, objMeasure
								.getCode());
						if (a != null) {
							nCount++;
							dValue = dataChannel.getAreaData(
									env.getReportEnv().m_curReport, env
											.getReportEnv().m_curPage, a)[0]
									.doubleValue();
						} else {
							return getValue();
						}*/

//						//如果指标与区域关联，那么从关联区域取值
//						if(objEnv.isMainMeasure(objMeasure.getCode())){
//						nCount++;
//						dValue =objEnv.getMainMeasureValue(objMeasure.getCode()).doubleValue();
//						}
//						else{
//						return getValue();
//						}
//					}
					//2.2获得当前关键字值组成的字串strCurKeyValue
//					strCurKeyValue=getCurrentStrKeyValue(objEnv);
//				}
//			}
//			1.3 如果当前计算环境符合条件并且指标与区域关联，那么取出关联区域值
//			if (objEnv instanceof UfoCalcEnv) {
			UfoVal[] objVals = new UfoVal[iMeasureLen];
			if (objEnv != null) {
//				UfoCalcEnv env = (UfoCalcEnv) objEnv;
				if (bCurEnvPropt) {
					strCurKeyValue=getCurrentStrKeyValue(objEnv);
					//2.1获得当前指标区域的数值dValue
					if (objEnv.getUfoDataChannel() != null) {

						for(int i=0;i<iMeasureLen;i++){
							if(objMeasures[i].getType()!=MeasureVO.TYPE_NUMBER 
									&& objMeasures[i].getType()!=MeasureVO.TYPE_BIGDECIMAL)
								continue;

							if(objMeasures[i] instanceof MeasureVO) {
								if(objEnv.isMainMeasure(objMeasures[i].getCode())){
									nCount[i]++;
									dValue[i] =objEnv.getMainMeasureValue(objMeasures[i].getCode()).doubleValue();
								} else {
									return getValue();
								}
							} else {
								if(objEnv.isMainStorecell(CellPosition.getInstance(objMeasures[i].getCode()))){
									nCount[i]++;
									dValue[i] =objEnv.getMainStorecellValue(objMeasures[i].getCode()).doubleValue();
								} else {
									return getValue();
								}
							}
							
						}
					}

				}
				
				//3.从指标函数驱动中找到指标对应的所有值，并根据函数类型作相应的计算。
				MeasFuncDriver objFuncDriver = (MeasFuncDriver) objEnv
				.loadFuncListInst().getExtDriver(
						MeasFuncDriver.class.getName());

				//首先找到条件对应的数据集和
				/**  ,											   
				 * hashValue (key = strCond,value=(hashtable key=指标pk,CURKEY
				 * 											 value=hashtable(key=MSUM,MMAX,MMIN,MAVG,
				 * 															value=排除当前关键值外的合计、最大、最小、计数,当前关键字值字串）) )
				 */
//				Hashtable hashValue = objFuncDriver == null ? null
//						: objFuncDriver.getMeasValue(MeasFuncDriver.SUMDATA);

				Hashtable hashValue = getMsumDataHashValue(objFuncDriver);

				
				//4.如果驱动中无数据，则从数据库装载函数值
				if (hashValue == null) {
					return calcStatValues(objMeasures, objKeyCond, objEnv);
				}
				nc.vo.iuforeport.rep.ReportVO objRep = null;
				if(objMeasures[0] instanceof MeasureVO) {
					objRep = (nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(((MeasureVO)objMeasures[0]).getReportPK());
				} else {
//					objRep = (nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(objEnv.getRepPK());
					objRep = (nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(objMeasures[0].getReportPK());
				}

				if (objRep == null) {
					throw new UfoCmdException("miufo1000407"); //"指标对应的报表找不到！"
				}
				String strKeyGroupPK = objRep.getPk_key_comb();

				hashValue = (Hashtable) hashValue
				.get(strKeyGroupPK
						+ "\r\n"
						+ (objKeyCond == null ? "" : objKeyCond.toString()
								+ "\r\n"));
				/**
				 * hashValue  key=指标pk,CURKEY value=hashtable(key=MSUM,MMAX,MMIN,MAVG,value=排除当前关键值外的合计、最大、最小、计数,当前关键字值字串）)
				 */
				if (hashValue == null) {
					return calcStatValues(objMeasures, objKeyCond, objEnv);
				}
				//检查预计算的值是否正确。即当前的关键字值字串与预计算存储的关键字值是否相同，如同则取预计算值，否则自行计算.
				if(bCurEnvPropt){
					String strCurKeyTemp=(String) hashValue.get(TAG_CURKEYVALUE);
					if (! strCurKeyTemp.equals(strCurKeyValue)) {
						return calcStatValues(objMeasures, objKeyCond, objEnv);
					}
				}

				//5.根据函数类型，计算函数值

//				UfoVal[] objVals = new UfoVal[iMeasureLen];
				for(int i=0;i<iMeasureLen;i++){
					//NEW_ADD
					if(objMeasures[i].getType()!=MeasureVO.TYPE_NUMBER && objMeasures[i].getType()!=MeasureVO.TYPE_BIGDECIMAL){
						dValue[i]=0;
						objVals[i] = UfoDouble.getInstance(dValue[i]);
						continue;
					}

					Hashtable hashMeasValueTemp=(Hashtable) hashValue.get(objMeasures[i].getCode());

					/**
					 * hashMeasValueTemp, key=MSUM,MMAX,MMIN,MAVG,value=排除当前关键值外的合计、最大、最小、计数,当前关键字值字串）
					 */
					if(hashMeasValueTemp==null )
						objVals[i]= calcStatValue(objMeasures[i], objKeyCond, objEnv);
					else{
						if(nFID==MeasFuncDriver.FMSUM || nFID==MeasFuncDriver.FMAVG || nFID==MeasFuncDriver.FMSUMA){

							Double dSum=(Double) hashMeasValueTemp.get(MeasFuncDriver.MSUM);
							dValue[i]+=dSum==null?0:dSum.doubleValue();
						}
						if(nFID==MeasFuncDriver.FMAVG){
							Integer iCountTemp=(Integer) hashMeasValueTemp.get(MeasFuncDriver.MAVG);
							nCount[i]+=iCountTemp==null?0:iCountTemp.intValue();
							if (nCount[i] > 0) {
								dValue[i] = dValue[i] / nCount[i];
							} else {
								dValue[i] = 0;
							}
						}else if (nFID==MeasFuncDriver.FMMAX){
							Double dMax=(Double) hashMeasValueTemp.get(MeasFuncDriver.MMAX);
							if(dMax!=null){
								if(dMax.doubleValue()>dValue[i]){
									dValue[i] = dMax.doubleValue();
								}
							}
							if(dValue[i]==Double.MIN_VALUE)
								dValue[i]=0;
						}else if(nFID==MeasFuncDriver.FMMIN){
							Double dMin=(Double) hashMeasValueTemp.get(MeasFuncDriver.MMIN);
							if(dMin!=null){
								if(dMin.doubleValue()<dValue[i]){
									dValue[i] = dMin.doubleValue();
								}
							}
							if(dValue[i]==Double.MAX_VALUE)
								dValue[i]=0;
						}

						objVals[i] = UfoDouble.getInstance(dValue[i]);
					}
				}
			}

			return objVals;

		} catch (ScriptException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		} catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
	}
	
	protected UfoVal[] getStatValueFromCacheByUFDouble(
			IStoreCell[] objMeasures,
			UfoExpr objKeyCond,
			UfoCalcEnv objEnv)
	throws CmdException {
		try {

			if (objMeasures == null || objMeasures.length==0) {
				throw new UfoCmdException("miufo1000406"); //"计算指标统计函数时，发现指标不存在！"
			}

			//指标追踪可能也会调度计算过程。liuyy.
			measureTrace(objEnv);
			
			int iMeasureLen=objMeasures.length;
			
			//1.判断当前计算环境是否符合条件
			boolean bCurEnvPropt = false;
			if (objKeyCond == null) {
				bCurEnvPropt = true;
			} else {
				bCurEnvPropt = objKeyCond.calcExpr(objEnv)[0].doubleValue() == 1;
			}

			int[]  nCount = new int[iMeasureLen];
			//1.1 设置初始值

			UFDouble[] dValue = new UFDouble[iMeasureLen];
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			UFDouble minValue = new UFDouble(Double.MIN_VALUE);
			UFDouble maxValue = new UFDouble(Double.MAX_VALUE);
			for(int i=0;i<iMeasureLen;i++){
				if (nFID == MeasFuncDriver.FMMAX) {
					dValue[i] = minValue;
				} else if (nFID == MeasFuncDriver.FMMIN) {
					dValue[i] = maxValue;
				}else
					dValue[i]= new UFDouble(0);
			}
			//1.2获得当前关键字值字串strCurKeyValue
			String strCurKeyValue = null;
			//如果当前计算环境符合条件并且指标与区域关联，那么取出关联区域值
//			if (objEnv instanceof UfoCalcEnv) {
//				UfoCalcEnv env = (UfoCalcEnv) objEnv;
//				if (bCurEnvPropt) {
					//2.1获得当前指标区域的数值dValue
//					if (env != null && objEnv.getUfoDataChannel() != null) {
//
//					}
					//2.2获得当前关键字值组成的字串strCurKeyValue
//					strCurKeyValue=getCurrentStrKeyValue(objEnv);
//				}
//			}
//			1.3 如果当前计算环境符合条件并且指标与区域关联，那么取出关联区域值
//			if (objEnv instanceof UfoCalcEnv) {
			UfoVal[] objVals = new UfoVal[iMeasureLen];
			if (objEnv != null) {
				//2.2获得当前关键字值组成的字串strCurKeyValue
				strCurKeyValue=getCurrentStrKeyValue(objEnv);
//				UfoCalcEnv env = (UfoCalcEnv) objEnv;
				if (bCurEnvPropt) {
					//2.1获得当前指标区域的数值dValue
					if (objEnv.getUfoDataChannel() != null) {

						for(int i=0;i<iMeasureLen;i++){
							if(objMeasures[i].getType()!=MeasureVO.TYPE_NUMBER 
									&& objMeasures[i].getType()!=MeasureVO.TYPE_BIGDECIMAL)
								continue;

							if(objMeasures[i] instanceof MeasureVO) {
								if(objEnv.isMainMeasure(objMeasures[i].getCode())){
									nCount[i]++;
									UfoVal temp = objEnv.getMainMeasureValue(objMeasures[i].getCode());
									if(temp instanceof UfoDecimal) {
										dValue[i] = new UFDouble((BigDecimal)temp.getValue());
									} else {
										dValue[i] = new UFDouble(temp.doubleValue());
									}
								} else {
									return getValue();
								}
							} else {
								if(objEnv.isMainStorecell(CellPosition.getInstance(objMeasures[i].getCode()))){
									nCount[i]++;
									UfoVal temp = objEnv.getMainStorecellValue(objMeasures[i].getCode());
									if(temp instanceof UfoDecimal) {
										dValue[i] = new UFDouble((BigDecimal)temp.getValue());
									} else {
										dValue[i] = new UFDouble(temp.doubleValue());
									}
								} else {
									return getValue();
								}
							}
							
						}
					}

				}
				
				//3.从指标函数驱动中找到指标对应的所有值，并根据函数类型作相应的计算。
				MeasFuncDriver objFuncDriver = (MeasFuncDriver) objEnv
				.loadFuncListInst().getExtDriver(
						MeasFuncDriver.class.getName());

				//首先找到条件对应的数据集和
				/**  ,											   
				 * hashValue (key = strCond,value=(hashtable key=指标pk,CURKEY
				 * 											 value=hashtable(key=MSUM,MMAX,MMIN,MAVG,
				 * 															value=排除当前关键值外的合计、最大、最小、计数,当前关键字值字串）) )
				 */
//				Hashtable hashValue = objFuncDriver == null ? null
//						: objFuncDriver.getMeasValue(MeasFuncDriver.SUMDATA);
				
				Hashtable hashValue = getMsumDataHashValue(objFuncDriver);
				
				//4.如果驱动中无数据，则从数据库装载函数值
				if (hashValue == null) {
					return calcStatValues(objMeasures, objKeyCond, objEnv);
				}
				nc.vo.iuforeport.rep.ReportVO objRep = null;
				if(objMeasures[0] instanceof MeasureVO) {
					objRep = (nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(((MeasureVO)objMeasures[0]).getReportPK());
				} else {
//					objRep = (nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(objEnv.getRepPK());
					objRep = (nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(objMeasures[0].getReportPK());
				}

				if (objRep == null) {
					throw new UfoCmdException("miufo1000407"); //"指标对应的报表找不到！"
				}
				String strKeyGroupPK = objRep.getPk_key_comb();

				hashValue = (Hashtable) hashValue
				.get(strKeyGroupPK
						+ "\r\n"
						+ (objKeyCond == null ? "" : objKeyCond.toString()
								+ "\r\n"));
				/**
				 * hashValue  key=指标pk,CURKEY value=hashtable(key=MSUM,MMAX,MMIN,MAVG,value=排除当前关键值外的合计、最大、最小、计数,当前关键字值字串）)
				 */
				if (hashValue == null) {
					return calcStatValues(objMeasures, objKeyCond, objEnv);
				}
				//检查预计算的值是否正确。即当前的关键字值字串与预计算存储的关键字值是否相同，如同则取预计算值，否则自行计算.
				if(bCurEnvPropt){
					String strCurKeyTemp=(String) hashValue.get(TAG_CURKEYVALUE);
					if (! strCurKeyTemp.equals(strCurKeyValue)) {
						return calcStatValues(objMeasures, objKeyCond, objEnv);
					}
				}

				//5.根据函数类型，计算函数值

//				UfoVal[] objVals = new UfoVal[iMeasureLen];
				for(int i=0;i<iMeasureLen;i++){
					//NEW_ADD
					if(objMeasures[i].getType()!=MeasureVO.TYPE_NUMBER && objMeasures[i].getType()!=MeasureVO.TYPE_BIGDECIMAL){
						dValue[i]= new UFDouble(0);
						objVals[i] = UfoDouble.getInstance(dValue[i]);
						continue;
					}

					Hashtable hashMeasValueTemp=(Hashtable) hashValue.get(objMeasures[i].getCode());

					/**
					 * hashMeasValueTemp, key=MSUM,MMAX,MMIN,MAVG,value=排除当前关键值外的合计、最大、最小、计数,当前关键字值字串）
					 */
					if(hashMeasValueTemp==null )
						objVals[i]= calcStatValue(objMeasures[i], objKeyCond, objEnv);
					else{
						if(nFID==MeasFuncDriver.FMSUM || nFID==MeasFuncDriver.FMAVG || nFID==MeasFuncDriver.FMSUMA){

							Object dSum = hashMeasValueTemp.get(MeasFuncDriver.MSUM);
							
							if(dSum instanceof Double) {
								dSum = new UFDouble((Double)dSum);
							} else if(dSum instanceof UFDouble) {
								dSum = (UFDouble)dSum;
							} else {
								dSum = new UFDouble(0);
							}
							
							dValue[i] = ((UFDouble)dSum).add(dValue[i]);
						}
						if(nFID==MeasFuncDriver.FMAVG){
							Integer iCountTemp=(Integer) hashMeasValueTemp.get(MeasFuncDriver.MAVG);
							nCount[i]+=iCountTemp==null?0:iCountTemp.intValue();
							if (nCount[i] > 0) {
								dValue[i] = dValue[i].div(nCount[i]) ;
							} else {
								dValue[i] = new UFDouble(0);
							}
						}else if (nFID==MeasFuncDriver.FMMAX){
							Object dMax= hashMeasValueTemp.get(MeasFuncDriver.MMAX);
							if(dMax!=null){
								if(dMax instanceof Double) {
									dMax = new UFDouble((Double)dMax);
								} else if(dMax instanceof UFDouble) {
									dMax = (UFDouble)dMax;
								} else {
									continue;
								}
								if(((UFDouble)dMax).compareTo(dValue[i]) > 0){
									dValue[i] = (UFDouble)dMax;
								}
							}
							if(dValue[i].equals(minValue))
								dValue[i]=new UFDouble(0);
						}else if(nFID==MeasFuncDriver.FMMIN){
							Object dMin = hashMeasValueTemp.get(MeasFuncDriver.MMIN);
							if(dMin!=null){
								if(dMin instanceof Double) {
									dMin = new UFDouble((Double)dMin);
								} else if(dMin instanceof UFDouble) {
									dMin = (UFDouble)dMin;
								} else {
									continue;
								}
								if(((UFDouble)dMin).compareTo(dValue[i]) < 0){
									dValue[i] = (UFDouble)dMin;
								}
							}
							if(dValue[i].equals(maxValue))
								dValue[i]=new UFDouble(0);
						}
						objVals[i] = UfoDecimal.getInstance(dValue[i].toBigDecimal());
					}
				}
			}

			return objVals;

		} catch (ScriptException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		} catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
	}
	
	protected Hashtable getMsumDataHashValue(MeasFuncDriver objFuncDriver){
		return objFuncDriver == null ? null
				: objFuncDriver.getMeasValue(MeasFuncDriver.SUMDATA);
	}
	
	/**
	 * 如果是固定表、已经进行了预处理指标统计函数用此方法计算。 创建日期：(2003-8-8 10:06:43)
	 * 
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures
	 *            nc.vo.iufo.measure.MeasureVO
	 * @param objKeyCond
	 *            com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv
	 *            com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException
	 *                异常说明。
	 */
	protected UfoVal getStatValueMeasNotRelaArea(
			IStoreCell objMeasure,
			UfoExpr objKeyCond,
			UfoCalcEnv objEnv)
	throws CmdException {
		try {
			if (objMeasure == null) {
				throw new UfoCmdException("miufo1000406");  //"计算指标统计函数时，发现指标不存在！"
			}
			//读取关键字组合PK
//			nc.vo.iuforeport.rep.ReportVO objRep =
//				(nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(objMeasure.getReportPK());;

				nc.vo.iuforeport.rep.ReportVO objRep = null;
				if(objMeasure instanceof MeasureVO) {
					objRep = (nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(((MeasureVO)objMeasure).getReportPK());
				} else {
					objRep = (nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(objMeasure.getReportPK());
//					objRep = (nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(objEnv.getRepPK());
				}
				
				if (objRep == null) {
					throw new UfoCmdException("miufo1000407");  //"指标对应的报表找不到！"
				}
				String strKeyGroupPK = objRep.getPk_key_comb();
				//从指标函数驱动中找到指标对应的所有值，并根据函数类型作相应的计算。
				MeasFuncDriver objFuncDriver =
					(MeasFuncDriver) objEnv.loadFuncListInst().getExtDriver(
							MeasFuncDriver.class.getName());

				//首先找到条件对应的数据集和
				Hashtable hashValue = null;
				if(objFuncDriver!=null){
					switch (MeasFuncDriver.getFuncIdByName(getFuncName())) {
					case MeasFuncDriver.FMAVG :
					{
						hashValue =objFuncDriver.getMeasValue(MeasFuncDriver.MAVG);
						break;
					}
					case MeasFuncDriver.FMMAX :
					{
						hashValue = objFuncDriver.getMeasValue(MeasFuncDriver.MMAX);
						break;
					}
					case MeasFuncDriver.FMMIN :
					{
						hashValue = objFuncDriver.getMeasValue(MeasFuncDriver.MMIN);
						break;
					}
					case MeasFuncDriver.FMSUM :
					{
						hashValue =objFuncDriver.getMeasValue(MeasFuncDriver.MSUM);
						break;
					}
					}
				}

				if (hashValue != null
						&& hashValue.containsKey(
								strKeyGroupPK + "\r\n" + (objKeyCond == null ?"":objKeyCond.toString() + "\r\n"))) {
					Object objValue =
						(
								(Hashtable) hashValue.get(
										strKeyGroupPK + "\r\n" + (objKeyCond == null ?"":objKeyCond.toString() + "\r\n"))).get(
												objMeasure.getCode());
					if (objValue != null) {
						return UfoVal.createVal(objValue);
					}
				}
				return calcStatValue(objMeasure, objKeyCond, objEnv);

		}catch (ScriptException e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		} 
		catch (Exception e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
	}

	/**
	 * 获得版本信息参数的值
	 * @param objEnv
	 * @return
	 */
	public Integer getVerParamVal(UfoCalcEnv objEnv){
		
		//取函数的参数列表
		ArrayList alPara = getParams();
		UfoExpr objOffset = (UfoExpr) alPara.get(2);
		Double nOffset = null;
		if (objOffset != null) {
			try {
				nOffset = new Double(objOffset.getValue(objEnv)[0].doubleValue());
			} catch (UfoValueException e) {
				Logger.debug(e.getMessage());
			} catch (CmdException e) {
				Logger.debug(e.getMessage());
			}
		}
		// 从计算环境中取得额外计算参数：版本获取器
		Object verFetcher = objEnv.getExEnv(CommonExprCalcEnv.VERSION_FETCHER);
		if(verFetcher instanceof IVersionFetcher){
			return ((IVersionFetcher) verFetcher).getMselectVersion(objEnv, nOffset == null ? 0 : nOffset.intValue());
		}
			   
//		if(getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECT) 
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTS)
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTA) ){//增加“HBMSELECTA”
//			return new Integer(ExtFuncIUfoConst.HB_VERSION_NUM);
//		}

		Integer nVer = null;
		if (alPara.get(4) != null) {
			nVer = (Integer) MeasFuncDriver.getVerNoByName((String) alPara.get(4));
		} else {
			nVer =
				(objEnv.getMeasureEnv() == null
						? null
								: Integer.valueOf(objEnv.getMeasureEnv().getVer()));
		}

		return nVer;
	}

	/**
	 * 获得时间偏移参数的值
	 * @return
	 */
	private UfoExpr getOffsetParamVal() {
		ArrayList alPara = getParams();
//		if(getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECT) 
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTS)
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTA)){//增加“HBMSELECTA”
//			return (UfoExpr) alPara.get(3);
//		}else{
			return (UfoExpr) alPara.get(2);
//		}

	}
//	private boolean isHBfunc(){
//		if(getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECT)
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTS)
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTA))
//			return true;
//		return false;
//	}
	
	/**
	 * 指标函数取值，指标函数的计算入口
	 */
	public UfoVal[] getValue(ICalcEnv objEnv) throws CmdException {
		try {
			if (objEnv == null){
				// 计算环境没有设置，请调用计算模块的负责人设置计算环境之后进行计算！
				throw new CmdException("miufo1000411");
			}
			
			if (!(objEnv instanceof UfoCalcEnv)) {
				// 指标函数是IUFO特有的函数，计算环境必须是UfoExprCalcEnv或者其子类！
				throw new UfoCmdException("miufo1000413");
			}
			
			UfoCalcEnv env = (UfoCalcEnv) objEnv;
			
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
//			if(nFID == MeasFuncDriver.FMSELECTA || nFID == MeasFuncDriver.FHBMSELECTA
//					|| nFID ==MeasFuncDriver.FMSUMA) {
//				// 如果是MSELECTA/MSUMA/HBMSELECTA函数，需要判断引用的区域属于哪张表，并设置到计算环境中
//				Object param = this.getParams().get(0);
//				if(param instanceof UfoFullArea) {
//					String repCode = ((UfoFullArea) param).getReportCode(env);
//					if(repCode != null) {
//						String areaFuncRepPk = UFOCacheManager.getSingleton().getReportCache().getRepPKByCode(repCode);
//						env.setAreaFuncRepPk(areaFuncRepPk);
//					} else {
//						env.setAreaFuncRepPk(env.getRepPK());
//					}
//				}
//			}
			
			// 返回值
			UfoVal[] valReturns=null;
			
	        if (m_exFunc != null) {
				try {
					m_exFunc.setCurMeasure((MeasureVO)this.getMeasures((UfoCalcEnv) objEnv)[0]);
					return m_exFunc.getValue(objEnv);
				} catch (Exception e) {
				}
			}

//			if (nFID == MeasFuncDriver.FCODENAME) {
//				// CODENAME函数
//				UfoVal objBM = ((UfoExpr) getParams().get(0)).calcExpr(objEnv)[0];
//				if (objBM.getType() != UfoVal.TYPE_STRING) {
//					// 编码应该是一个字符串值的表达式！
//					throw new UfoCmdException("miufo1000412");
//				}
//				valReturns=new UfoVal[] {UfoVal.createVal(MeasFuncBO_Client.calcCodeNameValue((String) objBM.getValue(),
//					(String) getParams().get(1)))};
//				return valReturns;
//			}
	        
			if (nFID==MeasFuncDriver.FMSELECT
					|| nFID==MeasFuncDriver.FMSELECTA
					|| nFID==MeasFuncDriver.FMSELECTS) {

				// 指标追踪可能也会调度计算过程
				measureTrace(env);
				
				valReturns= getSelectValue(env);
				
				//add by ljhua 2007-1-23 解决mselecta将返回值按照区域位置对应
				if(nFID==MeasFuncDriver.FMSELECTA){
					valReturns= combineEmptyValue((UfoFullArea) getParams().get(0), (UfoCalcEnv) env, valReturns);
				}
			} else if (nFID==MeasFuncDriver.FMCOUNT) {
				// MCOUNT函数取值
				valReturns= getCountValue(env);
			} else {
				// 统计函数取值
				valReturns= getStatValue(env);
			}
			
			return valReturns;

		} catch (ScriptException e) {
			AppDebug.debug(e);//@devTools     e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		} catch (Exception e) {
			AppDebug.debug(e);//@devTools     e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		} finally {
//			((UfoCalcEnv)objEnv).setAreaFuncRepPk(null);
		}
	}

	/**
	 * 对于本表引用，且与当前计算环境一致的情况。直接从计算环境中读取指标追踪信息
	 * 
	 * @create by liuchuna at 2010-6-7,上午11:20:58
	 *
	 * @param env
	 * @throws UfoCmdException
	 */
	protected void measureTrace(UfoCalcEnv env) throws UfoCmdException {
		if(!env.isMeasureTrace()){
			return;
		}
		
		IStoreCell[] mvos = getMeasures2(env);
		if(mvos == null || mvos.length < 1){
			return;
		}
		int len = mvos.length;
		MeasureTraceVO[] mtvos = new MeasureTraceVO[len];
		MeasurePubDataUtil.initMeasurePubDataKeyData(new MeasurePubDataVO[]{env.getMeasureEnv()});
		for(int i = 0; i < len; i++){
			IStoreCell mvo  =  mvos[i];
			MeasureTraceVO mtvo = new MeasureTraceVO();
			if(mvo != null){
				mtvo.setAloneID(env.getMeasureEnv().getAloneID());
				mtvo.setMeasurePK(mvo.getCode());
				mtvo.setReportpk(mvo.getReportPK());
				mtvo.setKeyvalues(env.getMeasureEnv().getKeyDatas());
				mtvo.setCurRepDynTrace(true);
				mtvos[i] = mtvo;
			} else{
				mtvos[i] = null;
			}
			
		}
		
//		MeasureTraceVO[] mtvos2 = env.getMeasureTraceVOs();
//		if(mtvos2 != null && mtvos2.length > 0){
//			MeasureTraceVO[] mt = new MeasureTraceVO[mvos.length + mtvos2.length];
//			System.arraycopy(mvos, 0, mt, 0, mvos.length);
//			System.arraycopy(mtvos2, 0, mt, mvos.length, mtvos2.length);
//			mtvos = mt;
//		}
//		
		env.setMeasureTraceVOs(mtvos);
	}
	
	/**
	 * 返回值个数。却省为1
	 */
	public int getValueNum(ICalcEnv env) {
		try {
			switch (MeasFuncDriver.getFuncIdByName(getFuncName())) {
			case MeasFuncDriver.FMSELECTA :
				try{
					List<IArea> sepCells=seperateArea((UfoFullArea) getParams().get(0),(UfoCalcEnv)env);
					return sepCells==null?0:sepCells.size();
				}catch(UfoCmdException e){
					return 0;
				}

//			case MeasFuncDriver.FHBMSELECTA ://新增“HBMSELECTA”
			case MeasFuncDriver.FMSUMA:
				try{
					if (env instanceof UfoCalcEnv) {
						IStoreCell[] mVOs = getMeasures((UfoCalcEnv) env);
						if( mVOs != null){
							return mVOs.length;
						}
					} 
					return 0;
				}catch(UfoCmdException e){
					return 0;
				}
			case MeasFuncDriver.FMSELECTS :
//			case MeasFuncDriver.FHBMSELECTS:
				try {
					IStoreCell[] mVOs = getMeasures(null);
					if (mVOs != null) {
						return mVOs.length;
					}
					return 0;
				} catch (UfoCmdException e) {
					return 0;
				}
			}
			return 1;
		} catch (UfoParseException e) {
			return 1;
		}
	}
	/*	public byte getValueType ()
	参  数：无
	返回值：函数值类型
	功  能：返回函数值类型.
	 */
	public byte getValueType() {
		try {
			switch (MeasFuncDriver.getFuncIdByName(getFuncName())) {
			case MeasFuncDriver.FMAVG :
			case MeasFuncDriver.FMMAX :
			case MeasFuncDriver.FMMIN :
			case MeasFuncDriver.FMSUM :
			case MeasFuncDriver.FMCOUNT :
			{
				return UfoExpr.S_VAL | UfoExpr.NUM_VAL;

			}
//			case MeasFuncDriver.FCODENAME:
//				return  UfoExpr.S_VAL | UfoExpr.STRING_VAL;

			case MeasFuncDriver.FMSUMA:
			case MeasFuncDriver.FMSELECTA :
//			case MeasFuncDriver.FHBMSELECTA ://新增“HBMSELECTA”

			IArea objArea = ((UfoFullArea) getParams().get(0))
			.getArea();
			if (objArea.isCell()) {
				return UfoExpr.S_VAL;
			}
			return UfoExpr.M_VAL;



			case MeasFuncDriver.FMSELECT :
//			case MeasFuncDriver.FHBMSELECT:

				try {
					if (getMeasures(null)[0].getType() == MeasureVO.TYPE_NUMBER || 
							getMeasures(null)[0].getType() == MeasureVO.TYPE_BIGDECIMAL) {
						return UfoExpr.S_VAL | UfoExpr.NUM_VAL;

					} else {
						return UfoExpr.S_VAL | UfoExpr.STRING_VAL;
					}
				} catch (UfoCmdException e1) {
					AppDebug.debug(e1);
					return -1;
				}

			case MeasFuncDriver.FMSELECTS :
//			case MeasFuncDriver.FHBMSELECTS :
				try {
					IStoreCell[] objMeasures = getMeasures(null);
					int nNum = 0;
					int nStr = 0;
					for (int i = 0; i < objMeasures.length; i++) {
						if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER 
								|| objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL) {
							nNum++;

						} else {
							nStr++;
						}

					}
					if (nNum == objMeasures.length) {
						if (nNum > 1) {
							return UfoExpr.M_VAL | UfoExpr.NUM_VAL;
						} else {
							return UfoExpr.S_VAL | UfoExpr.NUM_VAL;
						}

					} else if (nStr == objMeasures.length) {
						if (nStr > 1) {
							return UfoExpr.M_VAL | UfoExpr.STRING_VAL;
						} else {
							return UfoExpr.S_VAL | UfoExpr.STRING_VAL;
						}
					} else {
						if (objMeasures.length == 1) {
							return UfoExpr.S_VAL;
						} else {
							return UfoExpr.M_VAL;
						}
					}
				}catch (UfoCmdException e1) {
					AppDebug.debug(e1);
					return -1;
				}
			}
			return -1;
		} catch (UfoParseException e) {
			return -1;
		}
	}
	/**
	 * 判断该函数是否是取当前表页的值。
	 * 创建日期：(2004-1-6 11:03:12)
	 * @author：杨婕
	 * @return boolean
	 */
	protected boolean isOnlyOneParamMselect() {
		if(getFuncName().equalsIgnoreCase(MeasFuncDriver.MSELECT)){
			java.util.ArrayList alPara = getParams();
			boolean bCur = true;
			for (int i = 1; i < alPara.size(); i++) {
				if (alPara.get(i) != null) {
					bCur = false;
					break;
				}
			}
			return bCur;
		}
		return false;
	}
	/**
	 * 判断函数中的指标是否是当前报表动态区域中的指标，
	 * 创建日期：(2003-8-7 15:58:49)
	 * @return boolean
	 */
	public boolean isMeasReferDynArea(
			UfoCalcEnv ObjExprEnv) {

		if(ObjExprEnv==null )
			return false;

		try{
			IStoreCell[] mvos=getMeasures(ObjExprEnv);
			if(mvos!=null){
				int iLen=mvos.length;
				for (int i=0;i<iLen;i++){
					if(MeasFunc.isReferDynArea(mvos[i],ObjExprEnv))
						return true;
				}
			}
			return false;
		} catch (UfoCmdException e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			return false;
		}
	}

	public static boolean isReferDynArea(IStoreCell storeCell,UfoCalcEnv ObjExprEnv){
		if(storeCell instanceof MeasureVO)
			return isMeasReferDynArea((MeasureVO)storeCell, ObjExprEnv);
		else{
			return isStoreCellReferDynArea((StoreCellVO)storeCell, ObjExprEnv);
		}
	}
	
	/**
	 * 获得指标所属动态区域pk。
	 * 创建日期：(2003-8-7 15:58:49)
	 * @return boolean
	 */
	public static boolean isMeasReferDynArea(MeasureVO objMeasure,UfoCalcEnv ObjExprEnv) {

		if (objMeasure!=null && ObjExprEnv!=null) {
			//获得指标所属动态区域pk
			if(ObjExprEnv.getDynPKByMeasurePK(objMeasure.getCode())!=null)
				return true;
//			com.ufsoft.iuforeport.reporttool.data.UfoTable objTable =
//			objEnv.getDataChannel().getTable();
//			return objTable.getDynPKByMeasurePK(objMeasure.getCode()) != null;
		}
		return false;
	}
	
	public static boolean isStoreCellReferDynArea(StoreCellVO storeCellVo,UfoCalcEnv ObjExprEnv) {

		if (storeCellVo!=null && ObjExprEnv!=null) {
			//获得指标所属动态区域pk
			if(ObjExprEnv.getDynPKByStoreCellPos(CellPosition.getInstance(storeCellVo.getCode()))!=null)
				return true;
//			com.ufsoft.iuforeport.reporttool.data.UfoTable objTable =
//			objEnv.getDataChannel().getTable();
//			return objTable.getDynPKByMeasurePK(objMeasure.getCode()) != null;
		}
		return false;
	}

	/**
	 * 判断是否有指标与当前报表区域关联。
	 * 创建日期：(2003-8-7 10:46:55)
	 * @param objEnv UfoCalcEnv
	 * @return boolean
	 */
	public boolean isInEnvRep(UfoCalcEnv objEnv) {
		try {
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			if (nFID == MeasFuncDriver.FMSELECT
					|| nFID == MeasFuncDriver.FMAVG
					|| nFID == MeasFuncDriver.FMCOUNT
					|| nFID == MeasFuncDriver.FMMAX
					|| nFID == MeasFuncDriver.FMMIN
					|| nFID == MeasFuncDriver.FMSUM) {
				MeasOperand objMeas = (MeasOperand) getParams().get(0);
				return objMeas.isInEnvRep(objEnv);

			} else if (nFID == MeasFuncDriver.FMSELECTS) {
				MultiMeasOperand objMeas = (MultiMeasOperand) getParams().get(0);
				return objMeas.isInEnvRep(objEnv);

			} else if( nFID == MeasFuncDriver.FMSELECTA
					||nFID == MeasFuncDriver.FMSUMA){//新增“HBMELECTA”
				UfoFullArea  a = (UfoFullArea)getParams().get(0);
					return a.isCurReportArea(objEnv);
			}
			return true;
		} catch (UfoParseException e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			return true;
		}
	}
	/**
	 * 检查是否与区域相关。 创建日期：(2003-6-20 10:21:44)
	 * 
	 * @return boolean
	 * @param objEnv
	 *            com.ufsoft.iufo.util.parser.ICalcEnv
	 * @param nFlag
	 *            int nFlag=2,则检查所有参数是否UfoFullArea类型外，还检查指标参数是否与区域关联，如是返回真。
	 *            nFlag=3,则除了检查UfoFullArea外检查指标函数中第二个参数以后的指标与区域关联也返回真.
	 *            指标与区域相关表示指标所属当前计算环境报表
	 */
	public boolean isRelaWithArea(ICalcEnv objEnv, int nFlag) {
		if (getParams() == null) {
			return false;
		}
		if (nFlag == PARAM_AREA_FLAG2
				&& objEnv instanceof UfoCalcEnv
				&& (isMeasReferDynArea((UfoCalcEnv) objEnv)
						|| isInEnvRep((UfoCalcEnv) objEnv))) {
			return true;

		}
		ArrayList alPara = getParams();
		int n = alPara.size();
		for (int i = 0; i < n; i++) {
			if (i == 0 && nFlag == PARAM_AREA_FLAG3) {
				nFlag = PARAM_AREA_FLAG2;
				continue;
			}
			Object para = alPara.get(i);
			if (para != null) {
				if (para instanceof UfoFullArea) {
					return true;
				}else if( para instanceof UfoExpr){
					if( ((UfoExpr)para).isRelaWithArea(objEnv, nFlag) ){
						return true;
					}
				}else{
					if (nFlag == 2) {
						if (para instanceof MeasOperand) {
							if (((MeasOperand) para).isInEnvRep(objEnv))
								return true;
						} else if (para instanceof MultiMeasOperand) {
							if (((MultiMeasOperand) para).isInEnvRep(objEnv))
								return true;
						} else if (para instanceof nc.vo.iufo.measure.MeasureVO) {
							if (new MeasOperand((nc.vo.iufo.measure.MeasureVO) para)
							.isInEnvRep(objEnv))
								return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * 获得从当前行开始指定步长的关键字数据集合
	 * @param dynKeyDatas
	 * @param curRowKeyDatas
	 * @return
	 */
	private static KeyDataGroup[] getBatchKeyDatas(KeyDataGroup[] dynKeyDatas,KeyDataGroup curRowKeyDatas,int iStepNum){

		if(curRowKeyDatas==null || dynKeyDatas==null || dynKeyDatas.length==0)
			return null;

		ArrayList listRet=new ArrayList();
		int iStart=-1;
		int iLen=dynKeyDatas.length;
		for (int i=0;i<iLen;i++){
			if(dynKeyDatas[i] != null && curRowKeyDatas.equals(dynKeyDatas[i])){
				iStart=i;
			}
			if(iStart>=0 && i>=iStart && i<iStart+iStepNum){
				listRet.add(dynKeyDatas[i]);
			}
		}
		if(listRet.size()==0)
			return null;

		KeyDataGroup[] keyDataGroups=new KeyDataGroup[listRet.size()];
		listRet.toArray(keyDataGroups);
		return keyDataGroups;
	}

	/**
	 * 按照指定步长，批量读取各行数据
	 * @param objEnv
	 * @param dynKeyDatas
	 * @param strExprDynKeyPKs 统计函数条件参数中包含的动态区域关键字pk集合
	 * @return
	 */
	private Hashtable batchReadAggrFromDB(ReportDynCalcEnv objEnv,
			KeyDataGroup[] dynKeyDatas,String[] strZkeyRefDynKeys,String[] strMeasKeyRefDyns,String strMainTimeKey)throws CmdException  {
		if (objEnv == null || objEnv.getMeasureEnv() == null) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		if(dynKeyDatas==null || dynKeyDatas.length==0)
			return null;

		try {

			KeyDataGroup	objKeyDatas =  objEnv.getKeyDatas();
			KeyDataGroup[]	batchKeyDatas=getBatchKeyDatas(dynKeyDatas,objKeyDatas,MSTATICFUNC_STEP);
			if(batchKeyDatas==null || batchKeyDatas.length==0)
				return null;

			UfoExpr exprCond=(UfoExpr) getParams().get(1);
			IStoreCell mvo = getMeasures(objEnv)[0];
			Hashtable hashReturn=null;

			if (objEnv.isClient()==false) {
//				nc.bs.iufo.calculate.MeasFuncBO objMeasFuncBO = new nc.bs.iufo.calculate.MeasFuncBO();
				hashReturn= MeasFuncBO_Client.batchGetAggrDatas(
						mvo,exprCond, strZkeyRefDynKeys,strMeasKeyRefDyns,strMainTimeKey,
						objEnv.getMeasureEnv(),
						objEnv.getExEnv(), 
						objEnv.getRepPK(), 
						objEnv.getKeys(),
						objKeyDatas,
						batchKeyDatas);
			} else {
				hashReturn= nc.ui.iufo.calculate.MeasFuncBO_Client.batchGetAggrDatas(
						mvo, exprCond, strZkeyRefDynKeys,strMeasKeyRefDyns,strMainTimeKey,
						objEnv.getMeasureEnv(), 
						objEnv.getExEnv(), 
						objEnv.getRepPK(), 
						objEnv.getKeys(), 
						objKeyDatas,
						batchKeyDatas);
			}
			
// 处理指标公式追踪结果返回值  liuyy.
			if(objEnv.isMeasureTrace()){
				MeasureTraceVO[] mtvos = (MeasureTraceVO[]) hashReturn.get(ICalcEnv.MEASURE_TRACE_FLAG);
				hashReturn.remove(ICalcEnv.MEASURE_TRACE_FLAG);
				objEnv.setMeasureTraceVOs(mtvos);
			}
			
			//加入批量获得各行的关键字值信息
			if(hashReturn!=null){
				Vector vecTemp=getKeyValueStrings(batchKeyDatas,strZkeyRefDynKeys);
				if(vecTemp!=null)
					hashReturn.put(CACHE_KEY,vecTemp);
			}
			return hashReturn;
		} 
//		catch (UfoException e) {
//		e.printStackTrace(System.out);
//		throw new UfoCmdException(e);
//		} 
		catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
	}
	/**
	 * 按照指定步长，批量读取各行数据
	 * @param objEnv
	 * @param dynKeyDatas
	 * @param strExprDynKeyPKs 统计函数条件参数中包含的动态区域关键字pk集合
	 * @return
	 */
	private Hashtable batchReadAggrFromDB1(ReportDynCalcEnv objEnv,
			KeyDataGroup[] dynKeyDatas,String[] strZkeyRefDynKeys,String[] strMeasKeyRefDyns,String strMainTimeKey)throws CmdException  {
		if (objEnv == null || objEnv.getMeasureEnv() == null) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		if(dynKeyDatas==null || dynKeyDatas.length==0)
			return null;

		try {

			KeyDataGroup	objKeyDatas =  objEnv.getKeyDatas();
			KeyDataGroup[]	batchKeyDatas=getBatchKeyDatas(dynKeyDatas,objKeyDatas,MSTATICFUNC_STEP);
			if(batchKeyDatas==null || batchKeyDatas.length==0)
				return null;

			UfoExpr exprCond=(UfoExpr) getParams().get(1);
			Hashtable hashReturn = null;

			if (objEnv.isClient()==false) {
//				nc.bs.iufo.calculate.MeasFuncBO objMeasFuncBO = new nc.bs.iufo.calculate.MeasFuncBO();
				hashReturn= MeasFuncBO_Client.batchGetAggrDatas(
						getMeasures(objEnv),exprCond, strZkeyRefDynKeys,strMeasKeyRefDyns,strMainTimeKey,
						objEnv.getMeasureEnv(),
						objEnv.getExEnv(), 
						objEnv.getRepPK(), 
						objEnv.getKeys(),
						objKeyDatas,
						batchKeyDatas);
			} else {
				hashReturn= nc.ui.iufo.calculate.MeasFuncBO_Client.batchGetAggrDatas(
						getMeasures(objEnv), exprCond, strZkeyRefDynKeys,strMeasKeyRefDyns,strMainTimeKey,
						objEnv.getMeasureEnv(), 
						objEnv.getExEnv(), 
						objEnv.getRepPK(), 
						objEnv.getKeys(), 
						objKeyDatas,
						batchKeyDatas);
			}
			
			// 处理指标公式追踪结果返回值  liuyy.
			if(objEnv.isMeasureTrace()){
				MeasureTraceVO[] mtvos = (MeasureTraceVO[]) hashReturn.get(ICalcEnv.MEASURE_TRACE_FLAG);
				hashReturn.remove(ICalcEnv.MEASURE_TRACE_FLAG);
				objEnv.setMeasureTraceVOs(mtvos);
			}
			
			//加入批量获得各行的关键字值信息
			if(hashReturn!=null){
				Vector vecTemp=getKeyValueStrings(batchKeyDatas,strZkeyRefDynKeys);
				if(vecTemp!=null)
					hashReturn.put(CACHE_KEY,vecTemp);
			}
			return hashReturn;
		} 
//		catch (UfoException e) {
//		e.printStackTrace(System.out);
//		throw new UfoCmdException(e);
//		} 
		catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
	}
	
	/**
	 * 从数据库中取统计函数数据
	 * 
	 * @create by liuchuna at 2010-6-7,下午01:41:12
	 *
	 * @param objEnv
	 * @return
	 * @throws CmdException
	 */
	private Hashtable readAggrDatasFromDB(UfoCalcEnv objEnv)
			throws CmdException {
		if(objEnv == null || objEnv.getMeasureEnv() == null){
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		try {
			KeyDataGroup objKeyDatas = null;
			if(objEnv instanceof ReportDynCalcEnv){
				objKeyDatas = ((ReportDynCalcEnv) objEnv).getKeyDatas();
			}
			//指标函数后台计算前，需要设置其他参数到额外环境参数中
			Hashtable hashExEnv = objEnv.getExEnv();
			if(objEnv.getLoginUnitId() != null){
				hashExEnv.put(CommonExprCalcEnv.EX_LOGINUNIT_ID, objEnv.getLoginUnitId());
			}
			Hashtable aggrDatas = null;
			if (objEnv.isClient() == false) {
				// 后台调用
//				MeasFuncBO objMeasFuncBO = new MeasFuncBO();
				aggrDatas = MeasFuncBO_Client.getAggrDatas(getMeasures(objEnv)[0],
						(UfoExpr) getParams().get(1), objEnv.getMeasureEnv(),
						hashExEnv, objEnv.getRepPK(), objEnv.getKeys(),
						objKeyDatas, objEnv.getDataChannel());

			} else {
				// 前台调用
				aggrDatas = MeasFuncBO_Client.getAggrDatas(
						getMeasures(objEnv)[0], (UfoExpr) getParams().get(1),
						objEnv.getMeasureEnv(), hashExEnv, objEnv.getRepPK(),
						objEnv.getKeys(), objKeyDatas, objEnv.getDataChannel());
			}
			
			if(aggrDatas != null && objEnv.isMeasureTrace()){
				MeasureTraceVO[] mtvos = (MeasureTraceVO[]) aggrDatas.get(ICalcEnv.MEASURE_TRACE_FLAG);
				aggrDatas.remove(ICalcEnv.MEASURE_TRACE_FLAG);
				objEnv.setMeasureTraceVOs(mtvos);
			}
			return aggrDatas;
		}catch (ScriptException e) {
			AppDebug.debug(e);//@devTools     e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		} 
		catch (Exception e) {
			AppDebug.debug(e);//@devTools     e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
	}
	
	/**
	 * 从数据库中读取已保存的指标数据
	 * 
	 * @return java.util.Hashtable
	 * @param objEnv
	 *            com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @param mvo MeasureVO
	 * @exception com.ufsoft.iufo.util.parser.CmdException
	 *                异常说明。
	 */
	private Hashtable readAggrDatasFromDB1(UfoCalcEnv objEnv) 
	throws CmdException {
		if(objEnv == null || objEnv.getMeasureEnv() == null){
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		try {
			com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup objKeyDatas = null;
			if(objEnv instanceof ReportDynCalcEnv){
				objKeyDatas = ((ReportDynCalcEnv) objEnv).getKeyDatas();
			}
			//指标函数后台计算前，需要设置其他参数到额外环境参数中
			Hashtable hashExEnv = objEnv.getExEnv();
			if(objEnv.getLoginUnitId() != null)
				hashExEnv.put(CommonExprCalcEnv.EX_LOGINUNIT_ID,
					objEnv.getLoginUnitId());
			Hashtable aggrDatas = null;
			if (objEnv.isClient()==false) {
//				nc.bs.iufo.calculate.MeasFuncBO objMeasFuncBO =
//					new nc.bs.iufo.calculate.MeasFuncBO();
				aggrDatas = MeasFuncBO_Client.getAggrDatas(
						getMeasures(objEnv),
						(UfoExpr) getParams().get(1),
						objEnv.getMeasureEnv(),
						hashExEnv,
						objEnv.getRepPK(),
						objEnv.getKeys(),
						objKeyDatas);
				
			} else {
				aggrDatas =  nc.ui.iufo.calculate.MeasFuncBO_Client.getAggrDatas(
						getMeasures(objEnv),
						(UfoExpr) getParams().get(1),
						objEnv.getMeasureEnv(),
						hashExEnv,
						objEnv.getRepPK(),
						objEnv.getKeys(),
						objKeyDatas);
			}
			
			if(objEnv.isMeasureTrace()){
				MeasureTraceVO[] mtvos = (MeasureTraceVO[]) aggrDatas.get(ICalcEnv.MEASURE_TRACE_FLAG);
				aggrDatas.remove(ICalcEnv.MEASURE_TRACE_FLAG);
				objEnv.setMeasureTraceVOs(mtvos);
			}
			
			
			return aggrDatas;
		}catch (ScriptException e) {
			AppDebug.debug(e);//@devTools     e.printStackTrace(System.out);
			throw new UfoCmdException(e);
		} 
		catch (Exception e) {
			AppDebug.debug(e);//@devTools     e.printStackTrace(System.out);
			throw new UfoCmdException(e.getMessage());
		}
	}
	/**
	 * 在计算之前，保存区域与指标关联的值.
	 * Creation date: (2003-3-21 15:37:52)
	 * @param mVOs MeasureVO[]
	 * @param env UfoCalcEnv
	 * @return boolean 如果
	 */
	private boolean saveMeasValueByArea(IStoreCell[] mVOs, UfoCalcEnv env)
	throws CmdException {

		boolean bModified = false;
		if (env == null
				|| env.getDataChannel()== null)
			return bModified;
		//得到指标
		for (int i = 0; i < mVOs.length; i++) {
			if (mVOs[i] != null) {
				//如果mVO与某个单元关联，需要得到该单元的值，设置到env
				if(mVOs[i] instanceof MeasureVO) {
					if(env.isMainMeasure(mVOs[i].getCode())){
						env.setExEnv(MEASEXENVPREFIX + mVOs[i].getCode(),env.getMainMeasureValue(mVOs[i].getCode()).getValue());
						bModified = true;
					}
				} else {
					if(env.isMainStorecell(CellPosition.getInstance(mVOs[i].getCode()))){
						env.setExEnv(MEASEXENVPREFIX + mVOs[i].getCode(),env.getMainStorecellValue(mVOs[i].getCode()).getValue());
						bModified = true;
					}
				}
				//note by ljhua 2004-11-16
//				ITableData dataChannel = objEnv.getDataChannel();
//				IArea a =
//				dataChannel.getMeasureCell(objEnv.getContextVO(), mVOs[i].getCode());
//				if (a != null) {
//				UfoVal[] vals =
//				dataChannel.getAreaData(a);

//				objEnv.setExEnv(MEASEXENVPREFIX + mVOs[i].getCode(), vals[0].getValue());
//				bModified = true;
//				}
			}
		}
		return bModified;
	}
	
	/**
	 * 将操作数对象转换为SQL语句追加到缓冲区中，
	 * 涉及的数据库表存放到表集中。
	 * 创建日期：(2002-5-13 10:11:33)
	 * @param strMainRepKeyGroupPK String 条件处理的主表关键字组合主键
	 * @param alElements ArrayList 表达式的中序表达式
	 * @param nPos int 当前处理的元素所处的位置
	 * @param sbBuf java.lang.StringBuffer
	 * @param dbTable java.util.Hashtable
	 * @param env com.ufsoft.iufo.util.parser.UfoCalcEnv
	 * @return int 返回处理过的最后一个元素的位置
	 */
	public void toSQL(java.lang.String strMainRepKeyGroupPK, java.lang.StringBuffer sbBuf, 
			Map<String,String> dbTable,
			Map<Integer,String> hashTableByKeyPos,
			ICalcEnv objEnv,
			String strDbtype) throws TranslateException {
		try {
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			boolean bCurPageMeas = true;
			ArrayList alPara = getParams();
			for(int i = 1; i < alPara.size(); i++){
				if(alPara.get(i) != null){
					bCurPageMeas = false;
				}
			}

			if(nFID == MeasFuncDriver.FMSELECT && bCurPageMeas){//如果是MSelect函数并且每个参数都有值
				MeasOperand objMeasure = (MeasOperand)getParams().get(0);
				objMeasure.toSQL(strMainRepKeyGroupPK, sbBuf, dbTable, objEnv);
				return;
			}

			if (!(objEnv instanceof UfoCalcEnv) || (nFID == MeasFuncDriver.FMSELECTA && !(objEnv instanceof UfoCalcEnv))) {
				throw new TranslateException(TranslateException.ERR_ENV);//计算环境错误
			}

			UfoCalcEnv env = (UfoCalcEnv) objEnv;
			UfoVal[] objVal = getValue(env);
			for (int i = 0; objVal != null && i < objVal.length; i++) {
				if (i != 0) {
					sbBuf.append(',');
				}
				sbBuf.append(' ');
				if (objVal[i] instanceof UfoString) {
					sbBuf.append('\'');
					sbBuf.append(objVal[i].getValue());
					sbBuf.append('\'');
				} else {
					sbBuf.append(objVal[i].getValue());
				}
				sbBuf.append(' ');
			}
		} catch (CmdException e) {
			throw new TranslateException(TranslateException.ERR_FUNC);
		} catch (UfoParseException pe) {
			throw new TranslateException(TranslateException.ERR_FUNC);
		}

	}
	/**
	 *@update
	 *2003-11-20杨婕修改算法，是该方法是先如下算法：（下面的算法是按照顺序确定的，如果前面没有抛出异常，表示前面的值已经确定有值）
	 *1。如果年未定义，用strReviseTimes中的年代替，如果strReviseTimes中年也未定义，抛出异常
	 *2。如果定义了周，那么用周确定其他所有的时间信息后返回
	 *3。如果除了年外其他都没有定义，那么用strReviseTimes中的月日确定该时间，如果strReviseTimes中时间是2月29日，那么今年定位2月28日
	 *4。如果半年未定义，那么用季来确定半年，如果季也没有定义那么用月来确定半年和季，如果季和月都没有定义用strReviseTimes中的半年、季、月来确定，如果还是没有确定抛出异常
	 *5。如果季没有定义，那么用月来确定季和半年，如果月也未定义用半年来确定季和月，还是不能确定抛出异常
	 *6。如果月没有定义，那么用季来确定
	 *7。如果旬没有定义，那么用日来确定旬，如果日没有定义用strReviseTimes中的寻来确定
	 *8。如果日没有定义，那么用旬来确定
	 *9。最州再根据已经确定的年、月、日确定周
	 *10。这样就得到了完整的时间信息
	 *@end
	 * 检查对应时间属性的值是否合法。
	 * 创建日期：(2003-8-11 18:53:12)
	 * @return boolean
	 * @param nType int
	 * @param nValue int
	 * @param strDateTimes String[]这个参数是一个长度为7的数组，其中依次存放着年、半年、季、月、旬、周、日
	 */
	/*
public final static void getRevisedTimeCode(
    String[] strDateTimes,
    String[] strReviseTimes)
    throws TranslateException {
    int nYear =
        strDateTimes[0] == null ? 0 : Integer.valueOf(strDateTimes[0]).intValue();
    int nHY =
        strDateTimes[1] == null ? 0 : Integer.valueOf(strDateTimes[1]).intValue();
    int nSeason =
        strDateTimes[2] == null ? 0 : Integer.valueOf(strDateTimes[2]).intValue();
    int nMonth =
        strDateTimes[3] == null ? 0 : Integer.valueOf(strDateTimes[3]).intValue();
    int nTenday =
        strDateTimes[4] == null ? 0 : Integer.valueOf(strDateTimes[4]).intValue();
    int nWeek =
        strDateTimes[5] == null ? 0 : Integer.valueOf(strDateTimes[5]).intValue();
    int nDay =
        strDateTimes[6] == null ? 0 : Integer.valueOf(strDateTimes[6]).intValue();
    if (nYear == 0) { //如果年为null，用校正值带入
        strDateTimes[0] = strReviseTimes[0];
    }
    nYear =
        strDateTimes[0] == null ? 0 : Integer.valueOf(strDateTimes[0]).intValue();
    if (nYear == 0) {
        throw new TranslateException("miufo1000417");  //"年信息不完整"
    }
    if (nWeek > 0) { //如果周有值，不调整
        nc.vo.iufo.pub.date.UFODate objDate =
            new nc.vo.iufo.pub.date.UFODate(strDateTimes[0] + "-01-01");
        objDate.setWeekIndex(nWeek);
        DatePropVO.getPropTimeCode(DatePropVO.YEAR_TYPE, objDate.getYear(), strDateTimes);
        DatePropVO.getPropTimeCode(DatePropVO.HALFYEAR_TYPE, objDate.getHalfYear(), strDateTimes);
        DatePropVO.getPropTimeCode(DatePropVO.QUATER_TYPE, objDate.getSeason(), strDateTimes);
        DatePropVO.getPropTimeCode(DatePropVO.MONTH_TYPE, objDate.getMonth(), strDateTimes);
        DatePropVO.getPropTimeCode(DatePropVO.TENDAYS_TYPE, objDate.getTendays(), strDateTimes);
        strDateTimes[5] = DatePropVO.getPropString(DatePropVO.WEEK_TYPE, objDate.weekIndexOfYear());
        DatePropVO.getPropTimeCode(DatePropVO.DAY_TYPE, objDate.getDay(), strDateTimes);
        return;
    } else
        if (nHY == 0 && nSeason == 0 && nMonth == 0 && nTenday == 0 && nDay == 0) {
            int nReviseMonth =
                strReviseTimes[3] == null ? 0 : Integer.valueOf(strReviseTimes[3]).intValue();
            int nReviseDay =
                strReviseTimes[6] == null ? 0 : Integer.valueOf(strReviseTimes[6]).intValue();
            if (nReviseDay == 0 || nReviseMonth == 0) {
                throw new TranslateException("miufo1000418");  //"时间信息不完整"
            }
            nc.vo.iufo.pub.date.UFODate objDate = null;
            if (nReviseMonth != 2) {
                objDate =
                    new nc.vo.iufo.pub.date.UFODate(
                        strDateTimes[0] + "-" + strReviseTimes[3] + "-" + strReviseTimes[6]);
            } else
                if (nReviseDay == 29) {
                    objDate =
                        new nc.vo.iufo.pub.date.UFODate(
                            strReviseTimes[0] + "-" + strReviseTimes[3] + "-28");
                } else {
                    objDate =
                        new nc.vo.iufo.pub.date.UFODate(
                            strDateTimes[0] + "-" + strReviseTimes[3] + "-" + strReviseTimes[6]);
                }
            DatePropVO.getPropTimeCode(DatePropVO.YEAR_TYPE, objDate.getYear(), strDateTimes);
            DatePropVO.getPropTimeCode(DatePropVO.HALFYEAR_TYPE, objDate.getHalfYear(), strDateTimes);
            DatePropVO.getPropTimeCode(DatePropVO.QUATER_TYPE, objDate.getSeason(), strDateTimes);
            DatePropVO.getPropTimeCode(DatePropVO.MONTH_TYPE, objDate.getMonth(), strDateTimes);
            DatePropVO.getPropTimeCode(DatePropVO.TENDAYS_TYPE, objDate.getTendays(), strDateTimes);
            strDateTimes[5] = DatePropVO.getPropString(DatePropVO.WEEK_TYPE, objDate.weekIndexOfYear());
            DatePropVO.getPropTimeCode(DatePropVO.DAY_TYPE, objDate.getDay(), strDateTimes);
            return;
        }
    if (nHY == 0) { //如果半年为null，如果季、月有值取对应值，否则用校正值带入
        if (nSeason > 0) {
            if (nSeason < 3) {
                strDateTimes[1] = "01";
            } else {
                strDateTimes[1] = "02";
            }
        } else {
            if (nMonth > 0) {
                if (nMonth < 7) {
                    strDateTimes[1] = "01";
                    if (nMonth < 4) {
                        strDateTimes[2] = "01";
                    } else {
                        strDateTimes[2] = "02";
                    }
                } else {
                    strDateTimes[1] = "02";
                    if (nMonth < 10) {
                        strDateTimes[2] = "03";
                    } else {
                        strDateTimes[2] = "04";
                    }
                }
            } else {
                strDateTimes[1] = strReviseTimes[1];
                strDateTimes[2] = strReviseTimes[2];
                strDateTimes[3] = strReviseTimes[3];
                nMonth =
                    strDateTimes[3] == null ? 0 : Integer.valueOf(strDateTimes[3]).intValue();
            }
            nSeason =
                strDateTimes[2] == null ? 0 : Integer.valueOf(strDateTimes[2]).intValue();
        }
        nHY = strDateTimes[1] == null ? 0 : Integer.valueOf(strDateTimes[1]).intValue();
        if (nHY == 0) {
            throw new TranslateException("miufo1000419");  //"半年信息不完整"
        }
    }
    if (nSeason == 0) { //如果季为null，如果月有值，用对应值带入，否则用校正值带入
        if (nMonth > 0) {
            if (nMonth < 7) {
                strDateTimes[1] = "01";
                if (nMonth < 4) {
                    strDateTimes[2] = "01";
                } else {
                    strDateTimes[2] = "02";
                }
            } else {
                strDateTimes[1] = "02";
                if (nMonth < 10) {
                    strDateTimes[2] = "03";
                } else {
                    strDateTimes[2] = "04";
                }
            }
        } else {
            if (nHY == 1) {
                strDateTimes[2] = "02";
                strDateTimes[3] = "06";
            } else {
                strDateTimes[3] = "12";
                strDateTimes[2] = "04";
            }
            nMonth =
                strDateTimes[3] == null ? 0 : Integer.valueOf(strDateTimes[3]).intValue();
        }
    }
    nSeason =
        strDateTimes[2] == null ? 0 : Integer.valueOf(strDateTimes[2]).intValue();
    if (nSeason == 0) {
        throw new TranslateException("miufo1000420");  //"季信息不完整"
    }
    if (nMonth == 0) { //如果月为null，用季与校正值共同决定的值带入
        switch (nSeason) {
            case 1 :
                {
                    strDateTimes[3] = "03";
                    break;
                }
            case 2 :
                {
                    strDateTimes[3] = "06";
                    break;
                }
            case 3 :
                {
                    strDateTimes[3] = "09";
                    break;
                }
            case 4 :
                {
                    strDateTimes[3] = "12";
                    break;
                }
        }
    }
    nMonth =
        strDateTimes[3] == null ? 0 : Integer.valueOf(strDateTimes[3]).intValue();
    if (nMonth == 0) {
        throw new TranslateException("miufo1000421");  //"月信息不完整"
    }
    if (nTenday == 0) { //如果旬为null， 如果日有值用相应值带入，否则用校正值带入
        if (nDay > 0) {
            nc.vo.iufo.pub.date.UFODate objDate =
                new nc.vo.iufo.pub.date.UFODate(
                    strDateTimes[0] + "-" + strDateTimes[3] + "-" + strDateTimes[6]);
            strDateTimes[5] = DatePropVO.getPropString(DatePropVO.WEEK_TYPE, objDate.weekIndexOfYear());
            strDateTimes[4] = DatePropVO.getPropString(DatePropVO.TENDAYS_TYPE, objDate.getTendays());
        } else {
            strDateTimes[4] = strReviseTimes[4];
            strDateTimes[6] = strReviseTimes[6];
            nDay =
                strDateTimes[6] == null ? 0 : Integer.valueOf(strDateTimes[6]).intValue();
        }
        nTenday =
            strDateTimes[4] == null ? 0 : Integer.valueOf(strDateTimes[4]).intValue();
        if (nTenday == 0) {
            throw new TranslateException("miufo1000422");  //"旬信息不完整"
        }
    }
    if (nDay == 0) {
        switch (nTenday) {
            case 1 :
                {
                    strDateTimes[6] = "1";
                    break;
                }
            case 2 :
                {
                    strDateTimes[6] = "11";
                    break;
                }
            case 3 :
                {
                    strDateTimes[6] = "21";
                    break;
                }
        }
        nc.vo.iufo.pub.date.UFODate objDate =
            new nc
                .util
                .iufo
                .pub
                .UFODate(strDateTimes[0] + "-" + strDateTimes[3] + "-" + strDateTimes[6])
                .getEndDay(nc.vo.iufo.pub.date.UFODate.TENDAYS_PERIOD);
        strDateTimes[5] = DatePropVO.getPropString(DatePropVO.WEEK_TYPE, objDate.weekIndexOfYear());
        strDateTimes[6] = DatePropVO.getPropString(DatePropVO.DAY_TYPE, objDate.getDay());
        nDay =
            strDateTimes[6] == null ? 0 : Integer.valueOf(strDateTimes[6]).intValue();
    }
    if (nDay == 0) {
        throw new TranslateException("miufo1000423");  //"日信息不完整"
    }
    if (nDay > nc.vo.iufo.pub.date.UFODate.getDaysMonth(nYear, nMonth)) {
        strDateTimes[6] =
            DatePropVO.getPropString(DatePropVO.DAY_TYPE, nc.vo.iufo.pub.date.UFODate.getDaysMonth(nYear, nMonth));
    }
    nc.vo.iufo.pub.date.UFODate objDate =
        new nc.vo.iufo.pub.date.UFODate(
            strDateTimes[0] + "-" + strDateTimes[3] + "-" + strDateTimes[6]);
    strDateTimes[5] = DatePropVO.getPropString(DatePropVO.WEEK_TYPE, objDate.weekIndexOfYear());

    return;
}*/
	/**
	 * 对于多值的函数，返回针对第几个值的函数表示。
	 * UfoFunc的子类，如果可能提供多值的函数，应该重写本方法。
	 * 
	 * 此方法审核公式table.check执行时使用。
	 * @param objEnv
	 * @param bUserDef
	 * @return
	 */
	public String[] toIndividualStrs(ICalcEnv env,boolean bUserDef)
	{
		String[]  strFuncContents= null;
		MeasFunc  mFunc = null;
		try{
			int nFid = MeasFuncDriver.getFuncIdByName(getFuncName());
			//mselecta和mselects
			if( nFid == MeasFuncDriver.FMSELECTA 
					|| nFid == MeasFuncDriver.FMSELECTS 
//					|| nFid == MeasFuncDriver.FHBMSELECTS
//					|| nFid == MeasFuncDriver.FHBMSELECTA
					){	//新增“HBMSELECTA”
				ArrayList alParams = getParams();
				ArrayList alNewParams = new ArrayList(alParams);		

				if( nFid ==MeasFuncDriver.FMSELECTA){	//新增“HBMSELECTA”
					if( env instanceof UfoCalcEnv ){
						UfoFullArea fa = (UfoFullArea)alParams.get(0);
//						根据区域得到有指标的单元列表
						String[] strCells = getCellsWithMeasure(fa, (UfoCalcEnv)env);
						if( strCells != null){
							strFuncContents= new String[strCells.length];
							UfoFullArea newfa = (UfoFullArea)fa.cloneOperand();
							mFunc = new MeasFunc(getFid(), getFuncName(), alNewParams, getFuncDriverName(), getReturnType());	        			
							for( int i=0; i<strFuncContents.length;i++ ){
								newfa.setArea(AreaPosition.getInstance(strCells[i]));
								alNewParams.set(0, newfa);
								strFuncContents[i] = bUserDef ? mFunc.toUserDefString(env):mFunc.toString(env);
							}
						}
						return strFuncContents;
					}
				}else{
					MultiMeasOperand mlist = (MultiMeasOperand)alParams.get(0);
					MeasureVO[] mVOs = mlist.getMeasList();
					strFuncContents = new String[mVOs.length];
					mFunc = new MeasFunc(getFid(), getFuncName(), alNewParams, getFuncDriverName(), getReturnType());

					for(int i=0; i<strFuncContents.length; i++){
						MultiMeasOperand newmlist = new MultiMeasOperand(new MeasureVO[]{mVOs[i]});
						alNewParams.set(0, newmlist);
						strFuncContents[i] = bUserDef ? mFunc.toUserDefString(env):mFunc.toString(env);
					}
					return strFuncContents;
				}	
			}
		}catch(Exception e){
		}
		strFuncContents = new String[1];
		strFuncContents[0] = bUserDef ? this.toUserDefString(env):this.toString(env);
		return strFuncContents;
	}
//	public void getAreaList(Vector vecAreas, int nFlag)
	public List getAreaParam( int nFlag)
	{
		List listReturn=new ArrayList();
		if( MeasFuncDriver.MSELECTA.equalsIgnoreCase(getFuncName())){
			ArrayList alPara = getParams();
			int       nParaSize = alPara.size();
			UfoFullArea a = (UfoFullArea)alPara.get(0);
			if( nFlag == NOREP_AREA_MSELECTA){
				//本表，并且mselecta中不能有其他条件
				if( a.hasReport() == false){
					//判断mselecta的条件是否为空
					boolean hasOtherParam = false;
					for (int k = 1; k < nParaSize; k++) {
						if( alPara.get(k) !=  null ){
							hasOtherParam  = true;
						}
					}
					if( hasOtherParam == false){
//						vecAreas.add(a.getArea());
						listReturn.add(a);
					}
				}
			}else{
//				a.getAreaList(vecAreas, nFlag);
				listReturn.add(a);
			}
		}	
		return listReturn;
	}
	/**
	 * 将符合条件的指标加入到列表中
	 * nType = 0, 所有指标
	 * nType = 3, 除Mselect, mselects中有条件的所有指标
	 * @param mlist
	 * @param nType
	 */
	public void getReferringMeasures(Vector mlist, int nType)
	{
		ArrayList plist = getParams();
		if( plist == null || plist.size() == 0){
			return;
		}
		int nCheckParam;
		if( nType == 3 && 
				(MeasFuncDriver.MSELECT.equalsIgnoreCase(getFuncName())||
						MeasFuncDriver.MSELECTS.equalsIgnoreCase(getFuncName()))){
			nCheckParam =1;
			for( int i=1; i<plist.size(); i++){
				if( plist.get(i) != null){
					nCheckParam = 0;
					break;
				}
			}
		}else{
			nCheckParam = plist.size();
		}
		for( int i=0; i <nCheckParam; i++ ){
			Object param = plist.get(i);
			if( param == null ){
				continue;
			}
			if( param instanceof nc.vo.iufo.measure.MeasureVO ){
				mlist.addElement( ((nc.vo.iufo.measure.MeasureVO)param).getCode());
			}else if( param instanceof MeasOperand ){
				mlist.addElement( ((MeasOperand)param).getMeasureVO().getCode());
			}else if(param instanceof MultiMeasOperand){
				nc.vo.iufo.measure.MeasureVO[] objMVOs = ((MultiMeasOperand) param).getMeasList();
				for(int j =0 ; objMVOs != null && j < objMVOs.length; j++){
					mlist.addElement(objMVOs[j].getCode());
				}
			}
		}
	}
	public void clearData() {
		super.clearData();
		m_hashDbData=null;
		m_exprCons=null;
		m_userRightExpr=null;
		m_hashDynMeasDataInRep=null;

	}

//	/**
//	* 返回指标取数函数的完整条件
//	* @param objDateProp
//	* @param nOffset
//	* @param objEnv
//	* @param strKeyGroupPK
//	* @return
//	* @throws TranslateException
//	*/
//	private UfoExpr getSelectFullCond( DatePropVO objDateProp,
//	Double nOffset,UfoExprCalcEnv objEnv,String strKeyGroupPK)throws TranslateException{
//	if(m_exprSelectFullCond==null){

//	UfoExpr exprAllCond=getKeyCondParamVal();
//	exprAllCond=ReplenishKeyCondUtil.replenishKeyCond(
//	objEnv,
//	strKeyGroupPK,
//	exprAllCond,objDateProp,nOffset,true,false);

//	m_exprSelectFullCond=exprAllCond;
//	}
//	return m_exprSelectFullCond;

//	}
	protected UfoExpr[] getSelectConds(UfoCalcEnv objEnv,String strKeyGroupPK)throws TranslateException{
		if(m_exprCons==null){
			UfoExpr exprAllCond=getKeyCondParamVal();
			m_exprCons=ReplenishKeyCondUtil.getReplenishedKeyCond(objEnv,strKeyGroupPK,exprAllCond,true,true);
		}
		return m_exprCons;
	}

	protected UfoExpr[] getSelectCondsAll(UfoCalcEnv objEnv,String strKeyGroupPK)throws TranslateException{
		if(m_exprAllConds==null){
			UfoExpr exprAllCond=getKeyCondParamVal();
			m_exprAllConds=ReplenishKeyCondUtil.getReplenishedKeyCond(objEnv,strKeyGroupPK,exprAllCond,true,true);
		}
		return m_exprAllConds;
	}

	/**
	 * 获得时间形式为 k('年')=2005 and k('月')=06 and k('日')=30形式时间条件,且非时间条件中补充的为zkey形式
	 * @param objDateProp
	 * @param nOffset
	 * @param objEnv
	 * @param strKeyGroupPK
	 * @return
	 * @throws TranslateException
	 */
	private UfoExpr getValueTimeCondExpr(DatePropVO objDateProp,
			Double nOffset,UfoCalcEnv objEnv,String strKeyGroupPK)throws TranslateException{

		UfoExpr[] exprs=getSelectConds(objEnv,strKeyGroupPK);

		return ReplenishKeyCondUtil.replenishTimeValueKeyCond(exprs[0],exprs[1],objEnv,strKeyGroupPK,objDateProp,nOffset);
	}
	protected UfoExpr getZkeyTimeCondExpr(UfoCalcEnv objEnv,String strKeyGroupPK)throws TranslateException{

		UfoExpr[] exprs=getSelectConds(objEnv,strKeyGroupPK);

		return ReplenishKeyCondUtil.replenishZkeyTimeCond(exprs[0],exprs[1],objEnv,strKeyGroupPK);
	}

	/**
	 * 获得用户权限表达式
	 * @param objEnv
	 * @param strKeyGroupPK
	 * @return
	 */
	protected UfoExpr getUserRightExpr(UfoCalcEnv objEnv,String strKeyGroupPK)throws ParseException{
		if(m_userRightExpr==null)
			m_userRightExpr=UserCalcRightUtil.getUserRightCond(null,objEnv,strKeyGroupPK);
		return m_userRightExpr;

	}

	public static String getCurrentStrKeyValue(UfoCalcEnv env)
	throws UfoCmdException {
		String strCurKeyValue = null;
		if (env.getMeasureEnv() != null) {
			nc.vo.iufo.data.MeasurePubDataVO objPubData = env.getMeasureEnv();
			strCurKeyValue = "";
			nc.vo.iufo.keydef.KeyVO[] objKeys = env.getKeys();
			nc.vo.iufo.keydef.KeyGroupVO kgVO = new nc.vo.iufo.keydef.KeyGroupVO(
					objKeys);
			objKeys = kgVO.getKeys();
			if (env instanceof ReportDynCalcEnv
					&& ((ReportDynCalcEnv) env).getDynArea() != null) {
				com.ufsoft.iuforeport.reporttool.temp.KeyDataVO[] objKeydatas = ((ReportDynCalcEnv) env)
				.getKeyDatas().getKeyDatas();
				// nc.vo.iufo.keydef.KeyVO[] objKeys =
				// env.getKeys();
				for (int i = 0; i < objKeys.length; i++) {
					String keyValue = objPubData.getKeywordByName(objKeys[i].getName());
					if (keyValue == null) {
						for (int j = 0; j < objKeydatas.length; j++) {
							if (objKeys[i].getPk_keyword().equals(
									objKeydatas[j].getKey().getPk_keyword())) {
								keyValue = objKeydatas[j].getValue();
							}
						}
						if (keyValue == null) {
							String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufo1000410",null,
									new String[] { objKeys[i].getName() });
							throw new UfoCmdException(msg); //"关键字"+objKeys[i]+"没有设置计算环境值"
						}

					}
					strCurKeyValue +=keyValue + "\r\n";
				}
			} else {
				//nc.vo.iufo.keydef.KeyVO[] objKeys =
				// env.getKeys();
				for (int i = 0; i < objKeys.length; i++) {
					String keyValue = objPubData.getKeywordByName(objKeys[i].getName());
					if (keyValue == null) {
						String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufo1000410",null,
								new String[] { objKeys[i].getName() });
						throw new UfoCmdException(msg); //"关键字"
						// +
						// objKeys[i]
						// +
						// "没有设置计算环境值"
					} else {
						strCurKeyValue += keyValue + "\r\n";
					}
				}
			}
		}

		return strCurKeyValue;
	}

	public MeasurePubDataVO getValueKey() {
		return m_oValueKey;
	}
	public void setValueKey(MeasurePubDataVO newValueKey) {
		m_oValueKey = newValueKey;
	}
	/**
	 * 获得zkey(动态区关键字)对应的k()函数关键字集合
	 * @param expr
	 * @param dynKeyDataGroup
	 * @param strMainTimeKey
	 * @return
	 */
	private String[] getKeyRefDynFromExpr(UfoExpr expr,KeyDataGroup dynKeyDataGroup,String strMainTimeKey){
		if(expr==null || dynKeyDataGroup==null)
			return null;

		//1.获得动态区域关键字pk集合
		ArrayList listDynKeyPKs= getDynKeysFromKeyDatas(dynKeyDataGroup);

		return MeasCondExprUtil.getKeyByRefDynKey(expr,listDynKeyPKs,strMainTimeKey);
	}
	/**
	 * 只有取本表条件的mselect才是简单操作数，其它都是复杂的。
	 * 
	 */
	public boolean isComplex(){
		boolean bComplex = true;
		if (MeasFuncDriver.MSELECT.equalsIgnoreCase(getFuncName())
				&& getParams().size() == 1) {
			bComplex = false;
		}
		return bComplex;

	}
	public boolean isStringType(ICalcEnv env) {

		boolean bSring = false;
		try{
			if (getFuncName().equalsIgnoreCase(MeasFuncDriver.MSELECT)
					&& getMeasures(null)[0].getType() != MeasureVO.TYPE_NUMBER 
					&& getMeasures(null)[0].getType() != MeasureVO.TYPE_BIGDECIMAL) {
				return true;
			} else {
				IStoreCell[] objMeasures = null;
				if (getFuncName().equalsIgnoreCase(MeasFuncDriver.MSELECTS)) {
					objMeasures = getMeasures(null);
				} else if (getFuncName().equalsIgnoreCase(MeasFuncDriver.MSELECTA)
						&& env instanceof UfoCalcEnv) {
					try {
						objMeasures = getMeasures((UfoCalcEnv) env);
					} catch (Exception e) {
						objMeasures = null;
					}

				}
				for (int j = 0; objMeasures != null && j < objMeasures.length; j++) {
					if (objMeasures[j].getType() != MeasureVO.TYPE_NUMBER 
							&& objMeasures[j].getType() != MeasureVO.TYPE_BIGDECIMAL) {
						return true;
					}
				}
			}
		}
		catch(UfoCmdException e){
			AppDebug.debug(e);//@devTools  e.printStackTrace(System.out);
		}
		return bSring;
	}

	/**
	 * 判断是否为当前主表指标
	 * @param objEnv
	 * @return
	 */
	public boolean isCurMainRepMeas(UfoCalcEnv objEnv){
		//NEW_ADD
		try {
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			if (nFID == MeasFuncDriver.FMSELECT
					|| nFID == MeasFuncDriver.FMAVG
					|| nFID == MeasFuncDriver.FMCOUNT
					|| nFID == MeasFuncDriver.FMMAX
					|| nFID == MeasFuncDriver.FMMIN
					|| nFID == MeasFuncDriver.FMSUM
//					|| nFID == MeasFuncDriver.FHBMSELECT
					) {
				MeasOperand objMeas = (MeasOperand) getParams().get(0);
				return objMeas.isCurMainMeas(objEnv);

			} else if (nFID == MeasFuncDriver.FMSELECTS) {
				MultiMeasOperand objMeas = (MultiMeasOperand) getParams().get(0);
				return objMeas.isCurMainMeas(objEnv);

			} else if( nFID == MeasFuncDriver.FMSELECTA
//					||nFID == MeasFuncDriver.FHBMSELECTA
					|| nFID==MeasFuncDriver.FMSUMA ){//新增“HBMELECTA”
				UfoFullArea  a = (UfoFullArea)getParams().get(0);
					//判断是否为当前报表区域
					return a.isCurReportArea(objEnv);
			}
			return true;
		} catch (UfoParseException e) {
			AppDebug.debug(e);//@devTools         e.printStackTrace(System.out);
			return true;
		}
	}

	public boolean isConvertDirect(int compDirect){
		try {
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());

			if (nFID == MeasFuncDriver.FMSELECT) {
				MeasureVO meas = (MeasureVO)getMeasures(null)[0];
				if (meas.getExttype() == MeasureVO.TYPE_EXT_HEBING
						&& nc.vo.iufo.measure.HBBBMeasParser.getDirection(meas
								.getProps()) != compDirect)
					return true;
			}

		} catch (UfoParseException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
		}
		catch(UfoCmdException e){
			AppDebug.debug(e);//@devTools  e.printStackTrace(System.out);
		}
		return false;
	}

	public boolean isCheckRelaArea(){
		return true;
	}

	public void setExFunc(MeasFuncEx func) {
		m_exFunc = func;
	}


	/* (non-Javadoc)
	 * @see com.ufsoft.script.base.ITraceFunc#getTraceSplitInfo()
	 */
	@Override
	public String[] getTraceSplitInfo(int type, ICalcEnv objEnv) {
		return super.getTraceInfos(type, objEnv);
	}
	
	public boolean isRelaWithArea2(ICalcEnv objEnv, int nFlag){
		if (getParams() == null) {
			return false;
		}
		if (nFlag == PARAM_AREA_FLAG2
				&& objEnv instanceof UfoCalcEnv
				&& (isMeasReferDynArea((UfoCalcEnv) objEnv)
						|| isInEnvRep((UfoCalcEnv) objEnv))) {
			return true;

		}
		ArrayList alPara = getParams();
		int n = alPara.size();
		for (int i = 0; i < n; i++) {
			if (i == 0 && nFlag == PARAM_AREA_FLAG3) {
				nFlag = PARAM_AREA_FLAG2;
				continue;
			}
			Object para = alPara.get(i);
			if (para != null) {
				if (para instanceof UfoFullArea) {
					return true;
				}else if( para instanceof UfoExpr){
					if( ((UfoExpr)para).isRelaWithArea(objEnv, nFlag) ){
						return true;
					}
				}else{
					if (nFlag == 2) {
						if (para instanceof MeasOperand) {
							return true;
						} else if (para instanceof MultiMeasOperand) {
							return true;
						} else if (para instanceof nc.vo.iufo.measure.MeasureVO) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
