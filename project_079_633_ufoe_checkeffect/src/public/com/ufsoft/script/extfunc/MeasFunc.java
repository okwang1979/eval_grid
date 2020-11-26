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
 * ָ�꺯��
 * 
 * @author liuchuna
 * @created at 2010-6-7,����01:20:52
 *
 */
@SuppressWarnings("unchecked")
public class MeasFunc extends ExtFunc implements ITraceFunc{
	
	private static final long serialVersionUID = 2295167828250943491L;

	//���ݿ�ָ����ֵ add by ljhua 2--5-5-19 �����̬����ָ�꺯���Ż�
	private Hashtable m_hashDbData=null;
	
	private Hashtable m_hashDynMeasDataInRep =null;//����˹�ʽ���㱾��ҳ��ָ̬�꣬��˱�����¼��̬����ָ����ֵ
	
	private Hashtable m_hashDataByKeyData;
	
	private MeasurePubDataVO m_oValueKey;//��m_oValue��Ӧ��������Ϣ
	
	/**
	 * ָ��ȡ������������ [0]��ʾ��ʱ������,���Բ��䵱ǰ������ʱ������������zkey��ʽ��ʾ;
	 * 				   [1]��ʾʱ��������ԭ���������е�ʱ��������δ��������
	 */
	private UfoExpr[] m_exprCons=null;
	private UfoExpr[] m_exprAllConds=null;
	private UfoExpr m_userRightExpr=null;
	/**
	 * ��¼mselecta, hbmselecta������ָ�����
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
	 * ����ǹ̶���û�н���Ԥ�����ָ����������ô˷������㡣
	 * �������ڣ�(2003-8-8 10:06:43)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures nc.vo.iufo.measure.MeasureVO
	 * @param objKeyCond com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
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
	 * �붯̬���������ָ���������ȡֵ
	 * 
	 * @create by liuchuna at 2010-6-7,����01:21:40
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
			// �����ݿ���ȡ���з���������ֵ
			Hashtable hashDataInDB = null;
			if (mvos[0].getDbcolumn() != null) {
				hashDataInDB = readAggrDatasFromDB(objEnv);
			}
			if (hashDataInDB == null) {
				hashDataInDB = new Hashtable();
			}

			// ��Ӳ��滻��ǰ���㱨������
			String strCurMeasurePK=mvos[0].getCode();
			
			Hashtable hashDataInRep = null;
			String strDynAreaPK = null;
			if(mvos[0] instanceof MeasureVO) {
				// ���ָ�����ڶ�̬����pk
				strDynAreaPK = objEnv.getDynPKByMeasurePK(strCurMeasurePK);
				
				// ��ǰ���㱨�����ݣ�����������
				hashDataInRep =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,mvos[0]);
			} else {
				// ���ָ�����ڶ�̬����pk
				strDynAreaPK = objEnv.getDynPKByStoreCellPos(CellPosition.getInstance(strCurMeasurePK));
				
				// ��ǰ���㱨�����ݣ�����������
				hashDataInRep =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,mvos[0]);
			}
			
			env.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, objKeyValueInEnv);
			// ��ȡ�������������������ʽ
			UfoExpr objCond = (UfoExpr) getParams().get(1);

			Iterator it = hashDataInRep.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				KeyDataGroup objKeyDatas = (KeyDataGroup) entry.getKey();
				String strKey = "";
				// ���ö�̬�����㻷����Ϣ
				env.setDynAreaInfo(strDynAreaPK, objKeyDatas);
				// ���ݹؼ���ȡ���ڶ�̬���е����
				int m = DynAreaUtil.getOwnerUnitAreaNumByKeyData(objKeyDatas, strDynAreaPK, ((AbsRepDataChannel)env.getDataChannel()).getDataModel());
				if(m == -1){
					continue;
				}
				// ��������ͨ���Ķ�̬�����㻷������̬��pk�����
				((AbsRepDataChannel)env.getDataChannel()).setDynAreaCalcParam(new IUFODynAreaDataParam(m, null, strDynAreaPK));
				if (objCond == null || objCond.calcExpr(env)[0].doubleValue() == 1) {
					// ����������ʽ���ڣ�������Ϊ�棬����ǰ�����е�ֵ���hashDataInDB����Ӧ��ֵ
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
			//����
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
			// liuchun 20110610 �޸ģ������̬���������
			((AbsRepDataChannel)env.getDataChannel()).removeDynAreaCalcParam();
		}
	}
	/**
	 * �ȴӵ�ǰ�ı��������в��ң�û���ٴ����ݿ�ȡֵ
	 * �������ڣ�(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
	 */
	protected UfoVal[] calcDynSelectValue(UfoCalcEnv objEnv) throws CmdException {
		try {
			// java.util.ArrayList alPara = getParams();
			IStoreCell[] objMeasures = getMeasures(objEnv);//����Ҫ�������������ָ����Ϣ.
			if (objMeasures == null
					|| objEnv == null
					||objEnv.getUfoDataChannel() == null) {
				//���û��ָ�������Ϣ����û�б�����Ϣ���״�
				throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
			}
			MeasFuncDriver objFuncDriver =
				(MeasFuncDriver) objEnv.loadFuncListInst().getExtDriver(
						MeasFuncDriver.class.getName());

			//�õ��ؼ�����ϵ�PK
			String strKeyGroupPK = getKeyGroupPK(objFuncDriver.getMeasCache(),objEnv);
			if (isOnlyOneParamMselect()) {
				//1.û����������ô�ӵ�ǰ���㻷����Ӧ�Ķ�̬����ȡֵ
				return calcDynSelectValueByEnv(
						objMeasures,
						strKeyGroupPK,
						null,
						null,
						null,
						null,
						objEnv);
			} else {
				UfoExpr objOffset = getOffsetParamVal();//ȡƫ����
				UfoExpr objKeyCond = getKeyCondParamVal();//ȡ�������ʽ
				DatePropVO objDateProp = getDatePropParamVal();//ȡ����
				Integer nVer = getVerParamVal(objEnv);//��ð汾��Ϣ������ֵ
				//[b].����������ֵ������ƫ�����͹ؼ���������
				Double nOffset = null;
				if (objOffset != null) {
					nOffset = new Double(objOffset.getValue(objEnv)[0].doubleValue());
				}
				UfoExpr objKeyCondValue = null;
				boolean bKeyCondSame = true;


				if (objKeyCond != null
						&& objEnv instanceof ReportDynCalcEnv
						&& ((ReportDynCalcEnv) objEnv).getKeyDatas() == null) {
					//�����㻷��Ϊ�������ʱ����,���ʽ��Ϊ����Ϊ��ǰ����
					return calcDynSelectValue(
							strKeyGroupPK, 			//�ؼ�����ϵ�PK
							objMeasures,			//����Ҫ�������������ָ����Ϣ.
							objDateProp,			//����
							nOffset,				//ƫ���� 
							objKeyCond,				//�������ʽ
							nVer,					//�汾��
							objEnv);				//������㻷����
				}

				if (objKeyCond != null) {
					UfoEElement[] objEles = objKeyCond.getElements();
					UfoEElement[] objNewEles =
						objEles == null ? null : new UfoEElement[objEles.length];
					bKeyCondSame = checkKeyCondWithEnv(objKeyCond, objNewEles, objEnv);
					objKeyCondValue =
						new UfoExpr(objNewEles, objKeyCond.getType(), objKeyCond.getStatus());
				}

				//�ж��뵱ǰ���㻷���Ƿ�һ�£�
				if ((nOffset == null || nOffset.intValue() == 0)
						&& (nVer == null || (objEnv.getMeasureEnv() != null && nVer.intValue() == objEnv.getMeasureEnv().getVer()))
						&& (objKeyCond == null || bKeyCondSame)) {
					//{a}���һ��
					return calcDynSelectValueByEnv(
							objMeasures,
							strKeyGroupPK,
							objDateProp,
							nOffset,
							objKeyCondValue,
							nVer,
							objEnv);
				}
				//ȡ���������ǵ�ǰ��ҳ����ʱ
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
	 * ���㶯̬���������ָ��ȡ������ֵ��
	 * �������ڣ�(2003-8-8 10:06:43)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures nc.vo.iufo.measure.MeasureVO[]
	 * @param objDateProp nc.vo.iufo.keyword.DatePropVO
	 * @param nOffset java.lang.Double
	 * @param objKeyCond com.ufsoft.iufo.util.expression.UfoExpr
	 * @param nVer java.lang.Integer
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
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
			//1.����������Ļ��������л�ã����δ���ֵ������ݿ��л������
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
			throw new UfoCmdException("miufo1000401");  //"���㶯̬�������ʹ��ReportDynCalcEnv��Ϊ���㻷����"
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
			//note by ljhua 2005-1-20 �о�����Ҫ��鱨��
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
            throw new TranslateException("miufo1000402", new String[]{objMeasure[0].getName()});  //"ָ��" + objMeasure[0].getName() + "��Ӧ�ı����Ҳ�����"
        }*/
			UfoVal[] objVals = new UfoVal[objMeasure.length];
			boolean[] bHasValueInRep = new boolean[objMeasure.length];
			int nHasValue = 0;

			//add by ljhua 2005-6-10 ���mselect�ڶ�̬�������޷���ȷȡ��ǰ����ָ��ֵ���⡣
//			UfoExpr objKeyCondJudge=getSelectFullCond(objDateProp,nOffset,objEnv,strKeyGroupPK);
			UfoExpr objKeyCondJudge=getValueTimeCondExpr(objDateProp,nOffset,objEnv,strKeyGroupPK);

			//2.����Ϊָ��ȡ��ǰ��ҳָ������ʱȡֵ����.
			boolean isDynConTimeKey = isDynContainTimeKey(env);
			AppDebug.error("================= patch is exist:" + isDynConTimeKey);
			for (int i = 0; i < objMeasure.length; i++) {
				if (!isReferDynArea(objMeasure[i], objEnv)) {
					continue;
				}
				// @edit by wangyga at 2009-7-9,����02:07:20
				if (!objEnv.isMeasureTrace() && (nOffset != null && nOffset.doubleValue() !=0  && !isDynConTimeKey))
					continue;
				
//				Hashtable hashValues =
//				objTable.getDynAreaMeasureValues(objMeasure[i].getCode());
				String strDynAreaPK = null;
				Hashtable hashValues = null;
				if(objMeasure[i] instanceof MeasureVO) {
					//���ָ���ڶ�̬�����ڵ�ֵ
					strDynAreaPK=objEnv.getDynPKByMeasurePK(objMeasure[i].getCode());
					
					//��Ҫ����ȡ�ùؼ���ά��ֵ�ķ���
					hashValues =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,objMeasure[i]);
				} else {
					strDynAreaPK=objEnv.getDynPKByStoreCellPos(CellPosition.getInstance(objMeasure[i].getCode()));
					
					hashValues =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,objMeasure[i]);
				}

				if (hashValues != null) {
					//add by ljhua 2005-3-7 ���mselect�ڶ�̬�������޷�ȡ��ǰ����ָ��ֵ���⡣
//					UfoExpr objKeyCondJudge =
//					ReplenishKeyCondUtil.replenishKeyCond(
//					objTable.getUTableCache().getAllKeyVO(
//					objTable.getUTableCache().getDynPKByMeasurePK(objMeasure[i].getCode())),
//					objDateProp,
//					nOffset,
//					objKeyCond,
//					objEnv);
					//note by ljhua 2005-6-10 ���mselect�ڶ�̬�������޷���ȷȡ��ǰ����ָ��ֵ���⡣
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
								//chxw ���ñ���׷�ٲ���
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
				//add by ljhua 2005-3-7 ���mselect����ȡ������������..�����������������whileѭ���иı���env����ֵ
				env.setDynAreaInfo(strDynAreaInEnv, objKeyValueInEnv);

				UfoVal[] objSomeVals =
					calcSelectValue(objMeasures, objDateProp, nOffset, objKeyCond, nVer, env);
				nPos = 0;
				//���ָ��ȡ����Ϊ��ǰ��ҳ����ȡ���ݿ����ֵ.
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
	 * �ӵ�ǰ�ı��������в���
	 * �������ڣ�(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
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

				//���ָ���ڶ�̬�����ڵ�ֵ
				//moidfy by ljhua 2006-8-9 ���mselect('��̬��ָ��pk'�������ļ���Ч��
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
			} /*else if(MeasOperand.isRelaWithArea(objMeasure[i], objEnv, 2)){//ע�͵�����Ϊ�����ָ���붯̬�����������ô��������ָ����̶��������
            objVals[i] = getSelectValueFromArea(objMeasure[i], objEnv);
            }*/
			else {
				bNotAllDynMeas = true;
				bNotDynMeas[i] = true;
			}
		}
		if (bNotAllDynMeas) {
			//���ڷǵ�ǰ����̬����ָ�꣬�ȴ���������Ļ����л�����ݣ����δ���������ݿ���װ�ء�
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
//		throw new UfoCmdException("miufo1000403", e);  //"�������ʹ���" 
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
	 * �����µĹؼ������ݼ���
	 * @param oldKeyDatas
	 * @param strKeyValue
	 * @return
	 */
	@SuppressWarnings("unused")
	private KeyDataGroup createNewKeyGroupByselect(KeyDataGroup oldKeyDatas,String strKeyValue){

		//��ö�̬���ؼ��ּ����Ӧֵ��hashmap
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

		//�����µ�KeyDataGroup
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
	 * ���ָ��Ĺؼ������VO
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
	 * ��m_hashDbData�л�ö�̬��ָ����keyGroupData��ͳ�ƺ�������
	 * @param keyGroupData
	 * @return
	 */
	private Hashtable getDynCurStatFromCache( UfoExpr objCond ,IStoreCell mvo,ReportDynCalcEnv env,String strDynAreaPK,KeyDataGroup curkeyGroupData)
	throws CmdException{
		if(m_hashDbData==null || m_hashDbData.size()==0 ||  curkeyGroupData==null)
			return null;

		//���ָ���Ӧ�Ĺؼ��ּ���
		KeyGroupVO keyGroupVO=getMeaureKeyGroupVO(mvo,env);
		nc.vo.iufo.keydef.KeyVO[] measureKeyVOs=keyGroupVO.getKeys();


		Object oldExZKeyValue = env.getExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);
		String strOldDynPk=env.getDynArea();
		KeyDataGroup oldKeyDatas=env.getKeyDatas();
		//tianchuan 2012.9.17 ȡ��¡������keyValues�ᱻ�޸�
		MeasurePubDataVO oldMPubVO=(MeasurePubDataVO)env.getMeasureEnv().clone();

		//�������ؼ���ֵ
		KeyDataVO[] mainKeyDatas=getMainKeyData(env);

		try{
			KeyDataGroup newKeyGroupCurKey=combineKeyDatas(mainKeyDatas,curkeyGroupData);
			env.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, newKeyGroupCurKey);
			//tianchuan ��¼ԭ���MeasurePubDataVO��zyear��zmonth�Ⱥ���ʹ��
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
			//tianchuan �Ƴ���¼��ԭ��MeasurePubDataVO
			if(env.getExEnv("oldMPubVO")!=null){
				env.removeExEnv("oldMPubVO");
			}
			env.setMeasureEnv(oldMPubVO);
			env.setDynAreaInfo(strOldDynPk,oldKeyDatas);
		}
	}
	/**
	 * ��m_hashDbData�л�ö�̬��ָ����keyGroupData��ͳ�ƺ�������
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
			//�������ؼ���ֵ
			KeyDataVO[] mainKeyDatas=getMainKeyData(env);
			for(int i = 0 ; i < mvos.length; i++){
				IStoreCell mvo = mvos[i];
				String strDynAreaPK = env.getDynPKByMeasurePK(mvos[i].getCode());
				
				//���ָ���Ӧ�Ĺؼ��ּ���
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
					 * MSUMAȡ�������ݸ�ʽ���磺
					 * {ָ��={ȡ������;={����ؼ������ֵ=10.0}}}
					 * MSUMȡ�������ݸ�ʽ���磺
					 * {ȡ������;={����ؼ������ֵ=4.0}}
					 * ����Ĵ����ܰ���MSUM�Ĵ������
					 */
					//û��!mvo.equals(objKey)���жϻ���ֺ����ָ�긲��ǰ��ָ��ֵ�����
					if(CACHE_KEY.equals(objKey) || !mvo.equals(objKey))
						continue;
					//1.����ָ��ȡ��m_hashDbData������value��Hashtable��
					hashTemp=(Hashtable) m_hashDbData.get(objKey);
					if(hashTemp==null || hashTemp.size()==0)
						continue;
					iteratTemp = hashTemp.keySet().iterator();
					while(iteratTemp.hasNext()){
						//2��ȡ��������value��Hashtable����keyֵ--ȡ������
						strKeyValueTemp = (String)iteratTemp.next();
						//3.����strKeyValueTempȡ������value��Hashtable��
						hashValue=(Hashtable) hashTemp.get(strKeyValueTemp);
						if(hashValue==null || hashValue.size()==0)
							continue;
						//4.ȡ��������value��Hashtable����keyֵ--�ؼ������ֵ
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
	 * ��ùؼ���ֵ����dynKeyDataGroups��ָ���ؼ���strExprDynKeys��ֵ�ִ�����
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
	 * ��ùؼ���ֵ����dynKeyDataGroup��ָ���ؼ���strExprDynKeys��ֵ�ִ�
	 * @param dynKeyDataGroup ��̬����ǰ�����еĹؼ���ֵ����
	 * @param strExprDynKeys �ؼ���pk����
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

		//1.��ö�̬����ؼ���pk����
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
	 * ��������expr���Ƿ��ж�̬����ؼ��֣��Ұ���������д˳�򷵻����õĹؼ���pk
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
	 * �ȴ�getValue()�в��ң�û���ٴ����ݿ�ȡֵ
	 * �������ڣ�(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
	 */
	protected UfoVal[] calcDynStatValue(ReportDynCalcEnv objEnv,UfoExpr objCond) throws CmdException {
		if (getValue() != null) {
			return getValue();
		}
		if (objEnv == null) {
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}

		//��¼�����е�ԭ����Ϣ
		String strEnvDynAreaPK = objEnv.getDynArea();
		KeyDataGroup objKeyValueInEnv =objEnv.getKeyDatas();//��̬����Ĺؼ���ֵ
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

			//��ö�̬����ؼ���pk����
			ArrayList listDynKeyPKs= objKeyValueInEnv==null?null:getDynKeysFromKeyDatas(objKeyValueInEnv);

			//1.1�ж�objCond�Ƿ���zkey()��ʽ�Ķ�̬����ؼ��ֺ���
			String[] strZkeyRefDyns=MeasCondExprUtil.getDynKeyFromExpr(objCond,listDynKeyPKs,strMainTimeKey);
			boolean isRefDyn=isRefDyn(objCond,listDynKeyPKs,strMainTimeKey,objEnv);
			
			//��õ�ǰ���㺯�������ݼ���
			UfoVal[] objVals = null;
			if(getFuncName().equalsIgnoreCase(MeasFuncDriver.MSUMA)){
				objVals = calcMeasureMSumaAggrDatas(objEnv, objCond, objKeyValueInEnv, objMeasures, strMainTimeKey, listDynKeyPKs, strZkeyRefDyns);
			} else{
				objVals = calcMeasureAggrDatas(objEnv, objCond, objKeyValueInEnv, objMeasures, strMainTimeKey, listDynKeyPKs, strZkeyRefDyns);
			}
			
			//����������Ƿ���뺯�������У��Ա�˹�ʽ�ڶ�̬����һ�м���ʱֱ�����ô˽����
			if(!isRefDyn){
				setValue(objVals);//note by ljhua 2005-2-24 �����̬������msum�ȹ�ʽȡ����������.
			}
				

			return objVals;
//			if (objMeasures[0].getDbcolumn() != null){
//				if(objKeyValueInEnv==null || strZkeyRefDyns==null ){
//					//�����ڹ�ʽ����������δ���ö�̬����ؼ���ʱ��������ݿ���ȡ���з���������ֵ
//					hashDataInDB= readAggrDatasFromDB(objEnv);
//				}else{
//					//��̬�����ڹ�ʽ
//					//���zkey(��̬���ؼ���)��Ӧ��k()�����ؼ��ּ���
//					String[] strMeasKeyRefDyns=getKeyRefDynFromExpr(objCond,objKeyValueInEnv,strMainTimeKey);
//
//					//��ö�̬������йؼ���ֵ
//					KeyDataGroup[] dynaKeyDatas=(KeyDataGroup[]) objEnv.getDynAllKeyDatas();
//					if ( m_hashDbData==null ) { 
//						//������ȡָ������������
//						m_hashDbData= batchReadAggrFromDB(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
//					}else {
//						Vector vecCacheKey=(Vector) m_hashDbData.get(CACHE_KEY);
//						//��õ�ǰ���㶯̬�������У������ж�̬����ؼ��ֵ�ֵ��
//						String strExprDynKeyValue=getKeyValueString(objKeyValueInEnv,strZkeyRefDyns);
//						if(vecCacheKey==null || !vecCacheKey.contains(strExprDynKeyValue)){
//							m_hashDbData=batchReadAggrFromDB(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
//						}
//					}
//					//���յ�ǰ�д�m_hashDbData��ɸѡ����
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
//			ArrayList listKeyValues=new ArrayList(); //��ǰ��ҳ����ͳ�ƺ��������Ĺؼ���ֵ���
//
//
//			if(isMeasReferDynArea(objEnv)){
//				if(m_hashDynMeasDataInRep==null){
//					//���ָ���ڶ�̬�����ڵ�ֵ
//					m_hashDynMeasDataInRep =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,objMeasures[0].getCode());   
////					m_hashDynMeasDataInRep=objTable.getDynAreaMeasureValues(objMeasures[0].getCode());
//				}
//			}
//
//			/**
//			 * �Ż���̬��ָ��msum��������.2005-9-14 modify by ljhua
//			 * �Ż�Ŀ�꣺���ټ�����������������objEnv.setDynAreaInfo(strDynAreaPK, objKeyDatas)����.
//			 * �Ż�������ֻ�е�������û��or���������Ҷ�̬���ؼ�������Ϊ=ʱ����ʹ�ô��Ż�������
//			 * ˵�����������ж�̬��������дȫʱ���ܹ��ж�Ψһȷ��һ�С�
//			 * 
//			 */
//			if(m_hashDynMeasDataInRep!=null && m_hashDynMeasDataInRep.size()>0){
//				if(objKeyValueInEnv==null){
//					//��ǰ�����������ʽ
//					com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup tempKeyDataGroup= 
//						(com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup) m_hashDynMeasDataInRep.keySet().iterator().next();
//					listDynKeyPKs= getDynKeysFromKeyDatas(tempKeyDataGroup);
//
//				}
//
//				//2.��Ӳ��滻��������
//				objEnv.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, objKeyValueInEnv);
//
//				//��ÿɱȽϵ������ж�̬���ؼ���ֵ
//				Map mapCompareKeyValue=null;
//				boolean  bOnlyOneRow=false;//��ʶ�Ƿ�ֻ��һ����������
////				KeyDataGroup findOnlyRowData=null;//Ψһ�еĹؼ�������
//				//��ö�̬��ʱ��ؼ���
//				String strDynTimeKey=DatePropVO.getTimeKey(listDynKeyPKs);
//
//				if(objCond!=null && listDynKeyPKs!=null && listDynKeyPKs.size()>0){
//					//�ж��Ƿ���or������
//					boolean bHaveOrOpera=MeasCondExprUtil.isHaveOrOpera(objCond);
//					if(bHaveOrOpera==false){
//						mapCompareKeyValue=MeasCondExprUtil.getRefDynKeyValues(objCond,listDynKeyPKs,strMainTimeKey,objEnv);
//					}
//
//					//������������еĶ�̬���ؼ���ֵ��дȫ���������һ��������������.
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
//				boolean bAllRow=false;//��ʶ�Ƿ������ж���������.ֻ��������ʽ����Ϊ����̬��ָ���msumʱ��ʹ�ô˱�ʶ.
//				boolean bFindOnlyRow=false;//��ʶ�Ƿ��ҵ�Ωһ��
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
//					//���㵱ǰ����Ĺؼ���ֵ�ִ�strKey
//					strKey = getKeyString(objMeasKeyData,objKeyDatas);          
//
//					listKeyValues.add(strKey);
//
//					boolean bPut=false;
//					if(objCond == null){
//						bPut=true;
//					}else if((listDynKeyPKs==null || listDynKeyPKs.size()==0) && bAllRow==true){
//						//�������������ʽ���Ѽ����ĳ������������������Ҳ����������
//						bPut=true;
//					}else if(objCond.calcExpr(objEnv)[0].doubleValue() == 1){
//						//��ʶ�������������ʽʱ����̬�������ж���������
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
//			//3.������¼��״̬��ɾ����̬����ĳЩ�е�δ����ʱ,ȥ����hashDataInDB���ڵĶ������� add by ljhua 2005-2-24
//			if( isMeasReferDynArea(objEnv))
//				removeStateValue(objKeyValueInEnv,objEnv,strMainTimeKey,listKeyValues,hashDataInDB);
//
//			//4.����
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
//			//5.����������Ƿ���뺯�������У��Ա�˹�ʽ�ڶ�̬����һ�м���ʱֱ�����ô˽����
//			if(strZkeyRefDyns==null || strZkeyRefDyns.length==0)
//				setValue(objVals);//note by ljhua 2005-2-24 �����̬������msum�ȹ�ʽȡ����������.
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
	 * ����ָ��ͳ�ƺ������ݼ�[��MSUMA����]
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
			//�����ڹ�ʽ����������δ���ö�̬����ؼ���ʱ��������ݿ���ȡ���з���������ֵ
			hashDataInDB= readAggrDatasFromDB(objEnv);
		}else{
			//��̬�����ڹ�ʽ
			//���zkey(��̬���ؼ���)��Ӧ��k()�����ؼ��ּ���
			String[] strMeasKeyRefDyns = getKeyRefDynFromExpr(objCond,objKeyValueInEnv,strMainTimeKey);

			//��ö�̬������йؼ���ֵ
			KeyDataGroup[] dynaKeyDatas=(KeyDataGroup[]) objEnv.getDynAllKeyDatas();
			if ( m_hashDbData==null ) { 
				//������ȡָ������������
				m_hashDbData= batchReadAggrFromDB(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
			}else {
				Vector vecCacheKey=(Vector) m_hashDbData.get(CACHE_KEY);
				//��õ�ǰ���㶯̬�������У������ж�̬����ؼ��ֵ�ֵ��
				String strExprDynKeyValue=getKeyValueString(objKeyValueInEnv,strZkeyRefDyns);
				if(vecCacheKey==null || !vecCacheKey.contains(strExprDynKeyValue)){
					m_hashDbData=batchReadAggrFromDB(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
				}
			}
			//���յ�ǰ�д�m_hashDbData��ɸѡ����
			String strDynAreaPK = objEnv.getDynPKByMeasurePK(objMeasures[0].getCode());
			hashDataInDB = getDynCurStatFromCache(objCond,objMeasures[0],objEnv,strDynAreaPK,objKeyValueInEnv);
		}
			
		hashDataInDB = calcMeasureAggrDatas(objEnv, objCond, objMeasures[0], hashDataInDB, objKeyValueInEnv, 
				listDynKeyPKs, strZkeyRefDyns, strMainTimeKey);
		
		UfoVal objVal = null;
		// �жϼ�������Ƿ��ô���ֵ���ʹ���
		boolean isBigNumber = isBigNumber(objEnv);
		if(objMeasures[0].getType() == IStoreCell.TYPE_BIGDECIMAL || isBigNumber) {
			// �����Ҫ����ֵ���͵Ĵ���,����UFDouble���ͽ��м���
			objVal = calcDynStatValueByUFDouble(hashDataInDB);
		} else {
			objVal = calcDynStatValue(hashDataInDB);
		}
//		UfoVal objVal = calcDynStatValue(hashDataInDB);
		
		
		objVals = new UfoVal[] {objVal};
		return objVals;
	}


	/**
	 * �Ƿ����ֵ���ʹ���
	 * 
	 * @create by liuchuna at 2011-10-25,����09:59:28
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
	 * ����ָ�꺯��MSUMA���ݼ���MSUMA����Ӧ�÷��ض�ֵ
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
		//����MSUMA��������Ҫ��ͳ������ÿ��ָ����м���
		objVals = new UfoVal[objMeasures.length];
		if(objKeyValueInEnv == null || strZkeyRefDyns == null ){
			//�����ڹ�ʽ����������δ���ö�̬����ؼ���ʱ��������ݿ���ȡ���з���������ֵ
			hashMeas2DataInDB = readAggrDatasFromDB1(objEnv);
		}else{
			//��̬�����ڹ�ʽ
			//���zkey(��̬���ؼ���)��Ӧ��k()�����ؼ��ּ���
			String[] strMeasKeyRefDyns = getKeyRefDynFromExpr(objCond, objKeyValueInEnv, strMainTimeKey);

			//��ö�̬������йؼ���ֵ
			KeyDataGroup[] dynaKeyDatas = (KeyDataGroup[]) objEnv.getDynAllKeyDatas();
			if ( m_hashDbData==null ) { 
				//������ȡָ������������
				m_hashDbData= batchReadAggrFromDB1(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
			}else {
				Vector vecCacheKey=(Vector) m_hashDbData.get(CACHE_KEY);
				//��õ�ǰ���㶯̬�������У������ж�̬����ؼ��ֵ�ֵ��
				String strExprDynKeyValue = getKeyValueString(objKeyValueInEnv,strZkeyRefDyns);
				if(vecCacheKey==null || !vecCacheKey.contains(strExprDynKeyValue)){
					m_hashDbData = batchReadAggrFromDB1(objEnv,dynaKeyDatas,strZkeyRefDyns,strMeasKeyRefDyns,strMainTimeKey);
				}
			}
			//���յ�ǰ�д�m_hashDbData��ɸѡ����
			hashMeas2DataInDB = getDynCurStatFromCache1(objCond, objEnv, objKeyValueInEnv);
			//hashDataInDB=readAggrDatasFromDB(objEnv);
		}
		
		//��ָ�����ݽ�����������������̬�����ݱ��޸Ļ���ɸѡ�����ݴ���
		for(int i = 0; i < objMeasures.length; i++){
			IStoreCell measureVO = objMeasures[i];
			Hashtable hashDataInDB = (Hashtable)hashMeas2DataInDB.get(measureVO);
			hashDataInDB = calcMeasureAggrDatas(objEnv, objCond, objMeasures[i], hashDataInDB, objKeyValueInEnv, 
					listDynKeyPKs, strZkeyRefDyns, strMainTimeKey);
			
			boolean isBigNumber = isBigNumber(objEnv);
			if(measureVO.getType() == IStoreCell.TYPE_BIGDECIMAL || isBigNumber) {
				// �����Ҫ����ֵ���͵Ĵ���,����UFDouble���ͽ��м���
				objVals[i] = calcDynStatValueByUFDouble(hashDataInDB);
			} else {
				objVals[i] = calcDynStatValue(hashDataInDB);
			}
		}
		return objVals;
	}

	/**
	 * ����ָ��ͳ�ƺ�����ֵ
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
		
		//chxw. ����ָ�깫ʽ׷��
		Vector<MeasureTraceVO> tracevos = new Vector<MeasureTraceVO>();
		MeasureTraceVO[] arrtracevo = objEnv.getMeasureTraceVOs();
		if(objEnv.isMeasureTrace() && arrtracevo != null){
			for(int i = 0; i < arrtracevo.length; i++){
				tracevos.add(arrtracevo[i]);
			}				
		}
		
		//��ǰ��ҳ����ͳ�ƺ��������Ĺؼ���ֵ���
		ArrayList listKeyValues = new ArrayList(); 
		String strDynAreaPK = null;
		if(objMeasure instanceof MeasureVO) {
			strDynAreaPK = objEnv.getDynPKByMeasurePK(objMeasure.getCode());
		} else {
			strDynAreaPK = objEnv.getDynPKByStoreCellPos(CellPosition.getInstance(objMeasure.getCode()));
		}
		
		if(isReferDynArea(objMeasure, objEnv)){
//			if(m_hashDynMeasDataInRep == null){
			//���ָ���ڶ�̬�����ڵ�ֵ
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
		 * �Ż���̬��ָ��msum��������.2005-9-14 modify by ljhua
		 * �Ż�Ŀ�꣺���ټ�����������������objEnv.setDynAreaInfo(strDynAreaPK, objKeyDatas)����.
		 * �Ż�������ֻ�е�������û��or���������Ҷ�̬���ؼ�������Ϊ=ʱ����ʹ�ô��Ż�������
		 * ˵�����������ж�̬��������дȫʱ���ܹ��ж�Ψһȷ��һ�С�
		 * 
		 */
		if(m_hashDynMeasDataInRep != null && m_hashDynMeasDataInRep.size()>0){
			KeyDataGroup tempKeyDataGroup=null;
			Iterator keyItes = m_hashDynMeasDataInRep.keySet().iterator();
			if(objKeyValueInEnv == null){
				//��ǰ�����������ʽ
				if(keyItes.hasNext()){
					tempKeyDataGroup= 
						(KeyDataGroup) keyItes.next();
					listDynKeyPKs = getDynKeysFromKeyDatas(tempKeyDataGroup);
				}
			}

			//2.��Ӳ��滻��������
			objEnv.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, objKeyValueInEnv);

			//��ÿɱȽϵ������ж�̬���ؼ���ֵ
			Map mapCompareKeyValue=null;
//			boolean  bOnlyOneRow=false;//��ʶ�Ƿ�ֻ��һ����������
//			KeyDataGroup findOnlyRowData=null;//Ψһ�еĹؼ�������
			//��ö�̬��ʱ��ؼ���
			String strDynTimeKey = DatePropVO.getTimeKey(listDynKeyPKs);

			if(objCond!=null && listDynKeyPKs!=null && listDynKeyPKs.size()>0){
				//�ж��Ƿ���or������
				boolean bHaveOrOpera=MeasCondExprUtil.isHaveOrOpera(objCond);
				if(bHaveOrOpera==false){
					mapCompareKeyValue = MeasCondExprUtil.getRefDynKeyValues(objCond,listDynKeyPKs,strMainTimeKey,objEnv);
				}

				//������������еĶ�̬���ؼ���ֵ��дȫ���������һ��������������.
				if(mapCompareKeyValue!=null && mapCompareKeyValue.size()>0){
					if(mapCompareKeyValue.size()==listDynKeyPKs.size()){
						if(strDynTimeKey==null || MeasCondExprUtil.isCompletedTimeCond(strDynTimeKey,(Map) mapCompareKeyValue.get(strDynTimeKey))){
//							bOnlyOneRow=true;	            		
//							findOnlyRowData=MeasCondExprUtil.findCondRow(objKeyValueInEnv,mapCompareKeyValue);
						}
					}
				}
			}
			//tianchuan עstart  �������bOnlyMainKeyCond��ȡ��Ӧ�����������
			//�����������bPut���ж�����Щ�޸ģ�ʹ��Ӧ���������������
			//�Ȳ���bOnlyMainKeyCond���޸���
        	boolean bOnlyMainKeyCond=false;
        	if (objCond!=null){
        		bOnlyMainKeyCond=MeasCondExprUtil.isOnlyMainKeyCondEqual(objCond, objEnv);
        	}			
        	//עend
        	
			String strKey =null;
			boolean bAllRow = false;//��ʶ�Ƿ������ж���������.ֻ��������ʽ����Ϊ����̬��ָ���msumʱ��ʹ�ô˱�ʶ.
//			boolean bFindOnlyRow = false;//��ʶ�Ƿ��ҵ�Ωһ��
			
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

				//���㵱ǰ����Ĺؼ���ֵ�ִ�strKey
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
				
				// ���ݶ�̬���ؼ��ֻ����ţ�����̬��pk�������Ϊ�������
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
//					//�������������ʽ���Ѽ����ĳ������������������Ҳ����������
//					bPut=true;
//				}
				else if(mapCompareKeyValue!=null){	//�����ж϶�̬���ؼ����Ƿ���ȷ
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
				if(bPut && objCond!=null){	//�����൱���ж�����ؼ����Ƿ���ȷ
					bPut=false;
					if(objCond.calcExpr(objEnv)[0].doubleValue() == 1){
						//��ʶ�������������ʽʱ����̬�������ж���������
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
        
		
		//3.������¼��״̬��ɾ����̬����ĳЩ�е�δ����ʱ,ȥ����hashDataInDB���ڵĶ������� add by ljhua 2005-2-24
		if(isMeasReferDynArea(objEnv))
			removeStateValue(objKeyValueInEnv,objEnv,strMainTimeKey,listKeyValues,hashDataInDB);
		
		//chxw. ����ָ�깫ʽ׷��
		if(objEnv.isMeasureTrace()){
			objEnv.setMeasureTraceVOs(tracevos.toArray(new MeasureTraceVO[0]));
		}
		if(objEnv.getDataChannel()!=null){
			// liuchun 20110610 �޸ģ������̬���������
			((AbsRepDataChannel)objEnv.getDataChannel()).removeDynAreaCalcParam();
		}
		
		
		return hashDataInDB;
	}

	/**
	 * ���tracevos���ݿ����Ƿ��ѱ���ùؼ�����ָ������
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
	 * ���㶯̬��ͳ�ƺ�������ֵ
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
	
	//tianchuan �������͸�Ϊprotected
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
	 * ɾ��hashDataInDB����¼��״̬��ɾ����δ����Ķ�̬�����м�¼
	 * @param objKeyValueInEnv
	 * @param objEnv
	 * @param strMainTimeKey
	 * @param listKeyValues
	 * @param hashDataInDB
	 */
	private void removeStateValue(KeyDataGroup objKeyValueInEnv ,ReportDynCalcEnv objEnv,String strMainTimeKey,ArrayList listKeyValues,Hashtable hashDataInDB )throws CmdException{
		//������¼��״̬��ɾ����̬����ĳЩ�е�δ����ʱ,ȥ����hashDataInDB���ڵĶ������� add by ljhua 2005-2-24


		//1.���ָ���Ӧ�Ĺؼ��ּ���
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

		//��̬��ָ�겻����û�йؼ���
		if(measureKeyVOs==null || measureKeyVOs.length==0)
			return;

		//2.�������ؼ���
		KeyGroupVO mainKeyGroupVO=objEnv.getKeyGroupVOInMain();
		nc.vo.iufo.keydef.KeyVO[] mainKeys=mainKeyGroupVO.getKeys();

		//3.��ö�̬����ʱ��ؼ���
		String strDynTimeKey=null;
		String strTimeKey=getTimeKey(measureKeyVOs);
		if(strMainTimeKey==null || !strMainTimeKey.equals(strTimeKey))
			strDynTimeKey=strTimeKey;

		//4.��ǰ����Ĺؼ���ֵ
		IKeyDetailData[] keyValuesInMain=objEnv.getKeyValuesInMain();

		//5.1�������ؼ�����ָ��ؼ�������е�λ��
		int[] iMainKeyInAllPos=new int[]{};//����˳��������ؼ������˳��

		//5.2�������ʱ��ؼ��ֵ�������ؼ�������е�λ��
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

		//6.��ǰ����ʱ��ؼ���ֵ
		String strMainTimeValue=null;
		if(keyValuesInMain!=null && iMainTimePosInMain>=0)
			strMainTimeValue=keyValuesInMain[iMainTimePosInMain]==null?null:keyValuesInMain[iMainTimePosInMain].getValue();

		//7.�Ƚ�����ؼ���ֵ�Ƿ���ͬ,����ͬ��ɾ��������¼
		Enumeration dbKeyValues=hashDataInDB.keys();//���йؼ���ֵ�ִ�����measureKeyVOs˳��
		String strTemp=null;
		StringTokenizer tokenValues=null;
		ArrayList listValues=null;
		while(dbKeyValues.hasMoreElements()){
			strTemp=(String) dbKeyValues.nextElement();
			if( listKeyValues.contains(strTemp))
				continue;

			//�������޹ؼ���ʱ����ֱ��ɾ�����м�¼
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

			//��ʾ�Ƿ�Ϊ��ǰ��ҳ���ݣ�������ؼ���ֵ��ͬ
			boolean bEqual=false;

			//�Ƚ�����ؼ���ֵ�Ƿ���ͬ
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
	 * ���ָ���ؼ��ּ����е�ʱ��ؼ���pk
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
	 * �ж϶�̬����ʱ��ֵstrDynTimeValue�Ƿ���strMainTimeValue��ͬһ������ʱ����.
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
	 * ���ָ�������������õĶ�̬����ؼ��ּ���
	 * @param objMeasures
	 * @param objKeyCond �����������������а���ƫ�ƺͲ������������.
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

		//1��ö�̬���ؼ��ּ���
		ArrayList listDynKeyPKs=getDynKeysFromKeyDatas(objKeyDatas);

		if(listDynKeyPKs==null || listDynKeyPKs.size()==0)
			return null;


		//2.���ָ��Ĺؼ��ּ���
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

		//3.��ö�̬����ʱ��ؼ���
//		String strDynTimeKey=DatePropVO.getTimeKey(listDynKeyPKs);

		//4..���ָ���ʱ��ؼ���
//		nc.vo.iufo.keydef.KeyVO measTimeKey=keyGroupVO==null?null:keyGroupVO.getTimeKey();
//		String strMeasTimeKey=measTimeKey==null?null:measTimeKey.getPk_keyword();

		//5.���ָ�������а����Ĺؼ���
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
		
//		//6.���ָ��ؼ��ּ���ͬ��̬����ؼ��ֵĽ���
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
//					//ʱ��ؼ���
//					if(strMainTimeKey==null  || DatePropVO.getDateTypeByPK(strMeasTimeKey)>DatePropVO.getDateTypeByPK(strMainTimeKey))
//						listIntersect.add(strDynTimeKey);
//				}
//			}
//		}
//		
//		if(listIntersect==null || listIntersect.size()==0)
//			return null;
		
		ArrayList listIntersect=listDynKeyPKs;

		//7.2������������ö�̬����ؼ��ֵļ���
		String[] strRefKey=MeasCondExprUtil.getDynKeyFromExpr(objKeyCond,listIntersect,strMainTimeKey);
		return strRefKey;

	}
	/**
	 * �Ƿ�Key����(Key��ZKey����)
	 * @param expr ������ĺ������ʽ
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
	 * �����ݿ⴦��������ָ��ȡ������ֵ
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
			//Ԥ����
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
			
			//����ָ�깫ʽ׷�ٽ������ֵ
			if(objEnv.isMeasureTrace()){
				MeasureTraceVO[] mtvos = (MeasureTraceVO[]) hashValue.get(ICalcEnv.MEASURE_TRACE_FLAG);
				hashValue.remove(ICalcEnv.MEASURE_TRACE_FLAG);
				objEnv.setMeasureTraceVOs(mtvos);
			}
			
			//������
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
	 * �����ݿ��л��mselect���ݡ� �������ڣ�(2003-8-8 10:06:43)
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
	 *            ���ܴ�����ǲ������������ʽ�����滻����ǰ�ؼ���ֵ�ı��ʽ��
	 * @param nVer
	 *            java.lang.Integer
	 * @param objEnv
	 *            com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException
	 *                �쳣˵����
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

			//��鲢���ָ��ؼ������pk
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
					//����ʽ����
					objValues=calcSelectValueByOne(objMeasures,objDateProp,nOffset,objKeyCond, nVer,objEnv,objCurKeyDatas);

				}else{
					//��̬����ʽ����
//					objValues=calcSelectValueByOne(objMeasures,objDateProp,nOffset,objKeyCond, nVer,objEnv,objCurKeyDatas);

					//1.�������ʱ��ؼ���
					String strMainTimeKey=null;
					if(objEnv instanceof ReportDynCalcEnv){
						//�������ʱ��ؼ���
						KeyGroupVO mainKeyGroupVO=((ReportDynCalcEnv) objEnv).getKeyGroupVOInMain();
						if(mainKeyGroupVO!=null ){
							nc.vo.iufo.keydef.KeyVO mainTimeKeyVO = mainKeyGroupVO.getTTimeKey();
							if(mainTimeKeyVO != null)
								strMainTimeKey = mainTimeKeyVO.getPk_keyword();
						}
					}

					//3.��ú�������������
					UfoExpr exprAllCond=getZkeyTimeCondExpr(objEnv,strKeyGroupPK);

					//4.����������õĶ�̬���ؼ���

					String[] strRefDynKeys=getRefDynKeyFromMselect(objMeasures,strKeyGroupPK,exprAllCond,objCurKeyDatas,strMainTimeKey,objEnv);
					if(strRefDynKeys==null){
						//5.��̬�����м����ֵ��ͬ
						if(m_hashDbData==null){
							m_hashDbData=new Hashtable();
							//5.1�����ݿ�������
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
								//m_hashDbData�洢����Ϊ��ָ���Ӧ��ֵ
								m_hashDbData.put(objMeasures[i].getCode(),objTemp);
							}
						}
						//5.2 �������
						for(int i=0;i<iLen;i++){
							if(objMeasures[i]==null){
								objValues[i]=null;
								continue;
							}
							objValues[i]=m_hashDbData.get(objMeasures[i].getCode());
						}
					//	m_hashDbData = null;
					} else{
						//6.��̬�����м����ֵ��ͬ������ж�����������.

						//6.1 ��ö�̬������йؼ���ֵ
						KeyDataGroup[] dynaKeyDatas=(KeyDataGroup[]) ((ReportDynCalcEnv)objEnv).getDynAllKeyDatas();

						//6.2��õ�ǰ���㶯̬�������У������ж�̬����ؼ��ֵ�ֵ��
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
							//�����û�Ȩ������
//							UfoExpr exprFullUserCond = MeasFunc.applyUserRightToCond(exprAllCond, objEnv, strKeyGroupPK);
							UfoExpr[] exprSelectConds=getSelectCondsAll(objEnv, strKeyGroupPK);
							UfoExpr exprNotTimeCond=ReplenishKeyCondUtil.combineBoolExpr(exprSelectConds[0],getUserRightExpr( objEnv, strKeyGroupPK));
							UfoExpr exprTimeCond=exprSelectConds[1];

							//6.3������ȡָ������������
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
						
						//6.4 ���յ�ǰ�д�m_hashDbData��ɸѡ����
						objValues = getDynCurSelectFromCache( objMeasures,strCondDynKeyValue,(ReportDynCalcEnv)objEnv,objCurKeyDatas,objDateProp,nOffset,strKeyGroupPK);
					//	m_hashDbData = null;
					}
					//��̬����ʽ���㴦�����
				}
			}

			//������
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
	 * һ�������ҳ���̬�����еļ�������Ϊ�˼���ѭ������
	 * @param hashData,���������ҳ�������
	 * @param keyDatas����̬���ؼ���ֵ����
	 * @param keyGroupVO����̬���ؼ������VO
	 * @param env
	 * @param objDateProp�������е���������
	 * @param nOffset������ƫ����
	 * @return
	 * @throws UfoCmdException
	 */
    private Hashtable getDataByKeyData(Hashtable<String,Object> hashData, KeyDataGroup keyDataGroups[], KeyGroupVO keyGroupVO, ReportDynCalcEnv env, DatePropVO objDateProp, Double nOffset, Map<String, IKeyDetailData> dynKeyDetail) throws UfoCmdException{
	    if(keyDataGroups == null || keyDataGroups.length <= 0)
	        return null;
	    
	    //�Զ�̬���йؼ���ֵ������
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
		    
		    //����̬���ؼ���ֵ���صļ�����
		    Hashtable hashRetData = new Hashtable();
	        hashData = (Hashtable<String,Object>)hashData.clone();
	        
	        //�õ������ݿ�ȡ����ֵ�������ؼ���ֵ������
	        String[] strKeyVals=hashData.keySet().toArray(new String[0]);
	        Arrays.sort(strKeyVals);
	        
	        //ÿ�е�exprKeyCond
	        List<UfoExpr> vExprKeyCond=new ArrayList<UfoExpr>();
	        
	        //�����Ƿ�ɾ���������ж�ʱ����Ҫ�ж�
	        List<Boolean> vDeleted=new ArrayList<Boolean>();
	        
	        //��ʼ��ǰ��������
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
	        	
                //2012.8.31 tianchuan ���vDeleted��������ΪvDeleted�Ĵ������������
                vDeleted.clear();
                for (int j=0;j<vKeyDataGroup.size();j++){
                	vDeleted.add(Boolean.FALSE);
                }
                
                //��¼���һ��ƥ���λ��
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
		            	//���������ģ��Ž����ؽ��
		            	hashRetData.put(vKeyDataGroup.get(j),m_hashDbData.get(strKeyVals[i]));
		            	iLastFitPos=j;
		            }else if (iLastFitPos>=0){
		            	//����������������һ�η��������뱾��֮����б��Ϊ����ƥ����
		            	for (int k=iLastFitPos;k >= 0;k--){
		            		vDeleted.set(k, Boolean.TRUE);
		            	}
		            	//2012.8.31 tianchuan ΪʲôҪbreak����������ƥ��ľ�ȡ������
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
	 * �������ָ��ȡ�������ĺ�����ֵ
	 * @param objMeasures
	 * @param exprNotTimeCond �����в�����zkey��ʽ��ʱ��ؼ�������
	 * @param exprTimeCond ԭ���ʽ��ʱ��ؼ�������
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

			//����ָ�깫ʽ׷�ٽ������ֵ
			if(objEnv.isMeasureTrace()){
				MeasureTraceVO[] mtvos = (MeasureTraceVO[]) hashReturn.get(ICalcEnv.MEASURE_TRACE_FLAG);
				hashReturn.remove(ICalcEnv.MEASURE_TRACE_FLAG);
				objEnv.setMeasureTraceVOs(mtvos);
			}
			
			//����������ø��еĹؼ���ֵ��Ϣ
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
	 * �������ؼ���ֵ
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
	 * �ӻ������л�õ�ǰ��̬�е�ָ��ȡ������ֵ
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
	 * ����ǹ̶���û�н���Ԥ�����ָ��ͳ�ƺ����ô˷������㡣
	 * �������ڣ�(2003-8-8 10:06:43)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures nc.vo.iufo.measure.MeasureVO
	 * @param objKeyCond com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
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
			//���ָ��Ϊ��ǰ����ָ�꣬�򱣴�ָ���Ӧ�����ֵ��env�У���������ʱ�Ͱ�����ǰ�����δ�����ֵ.
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
	 * ���ؼ����������ʽ�е�ExprOperand����������ֵ�滻�����µ�Ԫ�ط���objNewEles�У������ÿ��ֵ�Ƿ���env�еĹؼ���ֵһ�£����һ�·�����
	 * �������ڣ�(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param    UfoExpr objKeyCond,
	 * @param    UfoEElement[] objNewEles,��������л�����µı��ʽ��Ԫ��
	 * @param    UfoExprCalcEnv objEnv * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
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
					throw new UfoCmdException("miufo1000404");  //"���ʽ������Ŀ��һ�£�"
				}
				for (int i = 0; objEles != null && i < objEles.length; i++) {
					if (objEles[i].getType() == UfoEElement.OPR
							&& ((objEles[i].getObj() instanceof UfoExpr) || (objEles[i].getObj() instanceof NumOperand))) {//instance of ExprOperand, modify by ljhua 2005-12-5 ��Ϊȥ��ExprOperand
						//tianchuan ++ �����Զ��������������NumOperand
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
							throw new UfoCmdException(msg);  //                              "�ؼ���"                                  + ((KeyVO) ((UfoFunc) objEles[i - 1].getObj()).getParams().get(0)).getName()                                  + "����ָ��һ��ֵ��"
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
	 * �ڼ���֮�����������ָ�������ֵ.
	 * Creation date: (2003-3-21 15:37:52)
	 * @param mVOs MeasureVO[]
	 * @param env UfoCalcEnv
	 * @return boolean ���
	 */
	private void clearMeasValueByArea(IStoreCell[] mVOs, UfoCalcEnv env) throws CmdException {
		for (int i = 0; i < mVOs.length; i++) {
			if (mVOs[i] != null) {
				env.removeExEnv(MEASEXENVPREFIX + mVOs[i].getCode());
			}
		}
	}
	/**
	 * ���µ�nFuncID�Ͳ����б���������������������γ��µĺ��������������������д�˷�����
	 * �������ڣ�(2003-7-11 13:17:11)
	 * @return com.ufsoft.iufo.util.expression.UfoFunc
	 * @param nFuncID short
	 * @param alPara java.util.ArrayList
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
	 */
	public UfoFunc createNewFunc(short nFuncID, java.util.ArrayList alPara) throws CmdException {
		return new MeasFunc(nFuncID, getFuncName(), alPara, getFuncDriverName(), getReturnType());
	}
	/**
	 * �Ը������ڵ��ꡢ�¡��ա�����Ѯ����һ���ֵ�ֵ���е��������ؽ����
	 * �������ڣ�(2001-11-26 20:38:26)
	 * @return java.lang.String
	 * @param date java.lang.String
	 * @param field java.lang.String ʱ�����͡�Ҫ��ΪUfoDate�ඨ����ڼ�����
	 * @param n int
	 * @param strDateProp ʱ�����͡�Ҫ��ΪUfoDate�ඨ����ڼ�����
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
	 * ����ָ������������ֵ��
	 * ���ָ���ǵ�ǰ����̬�����ָ����߱���̬���������ָ�꣬����ָ��������к���������ô���м��㣬
	 * ���ָ���������������ʾ�뵱ǰ���㻷����ͬ������ָ���������������ô������ȡֵ
	 * �����ǰ���㻷�����������㻷��һ�£�����Ԥ����ֵ���������м���
	 * �������ڣ�(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
	 */
	private UfoVal[] getCountValue(UfoCalcEnv objEnv) throws CmdException {
		ArrayList alPara = getParams();
		
		if(objEnv == null ){
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}

		// �ж�ָ���Ƿ��ǵ�ǰ����̬�����е�ָ��
		if (isMeasReferDynArea(objEnv)) {
			// �������ñ���Ķ�̬��ָ��
			return calcDynCountValue(objEnv);
		}

		//b.�����ж��������Ƿ������򣨰��������������ָ�꺯������
		IStoreCell[] mvos=getMeasures(objEnv);
		if(isInEnvRep(objEnv) && mvos[0].getDbcolumn() == null){
			// ��ǰ������ָ��û�ж�Ӧ���ݱ��е��У�����Ĭ��ֵ
			return new UfoVal[]{UfoInteger.getInstance(1)};
		}
		UfoExpr objKeyCond = (UfoExpr) alPara.get(1);
		UfoExpr objKeyCondValue = objKeyCond;

		if (objKeyCond != null && MeasFuncDriver.isRelaWithArea(objKeyCond, objEnv)) {
			// �������ʽ���������
			// TODO ͳ�ƺ���������Ҫ����������ת��Ϊ�µ��������ʽ
			// objKeyCondValue =objKeyCond.solveFixedValue(objEnv);
			return getCountValueFromCache(mvos[0], objKeyCondValue, objEnv);

		} else if(getValue() != null){
			// ȡԤ�ȼ���õĺ���ֵ
			return getValue();
		}else{
			// ���¼��㺯��ֵ
			return calcCountValue(mvos[0], objKeyCond, objEnv);
		}
	}
	
	/**
	 * ����ǹ̶����Ѿ�������Ԥ����ָ��ͳ�ƺ����ô˷�������
	 * 
	 * @create by liuchuna at 2010-6-7,����02:24:10
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
				// ����ָ��ͳ�ƺ���ʱ������ָ�겻���ڣ�
				throw new UfoCmdException("miufo1000406");
			}
			
			// ��ȡ�ؼ������PK
			nc.vo.iuforeport.rep.ReportVO objRep = null;
			if(objMeasure instanceof MeasureVO) {
				objRep =(nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(((MeasureVO)objMeasure).getReportPK());
			} else {
//				objRep =(nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(objEnv.getRepPK());
				objRep =(nc.vo.iuforeport.rep.ReportVO) objEnv.getReportCache().get(objMeasure.getReportPK());
			}
			
			if (objRep == null) {
				// ָ���Ӧ�ı����Ҳ�����
				throw new UfoCmdException("miufo1000407");  
			}
			String strKeyGroupPK = objRep.getPk_key_comb();
			//��ָ�꺯���������ҵ�ָ���Ӧ������ֵ�������ݺ�����������Ӧ�ļ��㡣
			MeasFuncDriver objFuncDriver =
				(MeasFuncDriver) objEnv.loadFuncListInst().getExtDriver(
						MeasFuncDriver.class.getName());
			//�����ҵ�������Ӧ�����ݼ���
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
	 * �õ�ָ���Ӧ�Ĺؼ������PK�������ָ��û�йؼ������pk������NULLû���ҵ��׳��쳣��
	 * �������ڣ�(2003-8-7 10:04:39)
	 * @return java.lang.String
	 * @param objMeasureCache nc.pub.iufo.cache.MeasureCache
	 * @exception com.ufsoft.iufo.util.parser.UfoCmdException �쳣˵����
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
				MeasOperand objMeas = (MeasOperand) getParams().get(0);//�Ե���ָ��ȡ��

				return objMeasureCache.getKeyCombPk(objMeas.getMeasureVO().getCode());

			} else if (nFID == MeasFuncDriver.FMSELECTS
//					|| nFID == MeasFuncDriver.FHBMSELECTS
					) {
				MultiMeasOperand objMeas = (MultiMeasOperand) getParams().get(0);//�Զ��ָ��ȡ��

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
					|| nFID==MeasFuncDriver.FMSUMA){//���ӡ�FHBMSELECTA��
				IStoreCell[] objMeasures = getMeasures(objEnv);
				String strKeyGroupPK = null;
				if( objMeasures != null ){
					for (int i = 0; i < objMeasures.length; i++) {//������ָ��ȡ��
						if( objMeasures[i] != null ){
							//tianchuan 2012.9.20 ���ӶԴ洢��Ԫ�Ĵ���
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
	 * �õ�ָ���Ӧ�Ĺؼ������PK�����û���ҵ��׳��쳣��
	 * �������ڣ�(2003-8-7 10:04:39)
	 * @return java.lang.String
	 * @param objReortCache nc.pub.iufo.cache.ReportCache
	 * @exception com.ufsoft.iufo.util.parser.UfoCmdException �쳣˵����
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
//	* ���غ���Ҫ���������ָ����Ϣ.
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
	 * ���غ���Ҫ�������������ָ����Ϣ.
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
				//�����Ժ�����FHBMSELECTA�����ж�
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
	 * ���غ���Ҫ�������������ָ����Ϣ.
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
				//�����Ժ�����FHBMSELECTA�����ж�
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
	 * �ж�ָ�������и���Ԫ�Ƿ���ָ��.ע�ⷵ�ؽ���������к��з�ʽ����
	 * @param objArea
	 * @param objEnv
	 * @param measValues Ҫ��ָ��ֵ�������к�������
	 * @return
	 * @throws UfoParseException 
	 */
	private UfoVal[] combineEmptyValue(UfoFullArea objArea, UfoCalcEnv objEnv,UfoVal[] measValues)throws UfoCmdException, UfoParseException {

		//��¼ָ��λ����Ϣ��������ϵ�Ԫ�ϵ�ָ��ֻ��¼�׵�Ԫ
		List<CellPosition> measCells=new ArrayList<CellPosition>();

		if (objArea.isCurReportArea(objEnv)) {
			// 	�ж��Ƿ��ǵ�ǰ��������
			if (objEnv != null && objEnv.getUfoDataChannel() != null) {
				//����ָ�꼰���Ӧλ�ã�������ϵ�Ԫ�����ؿ�ʼ��Ԫ��
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
				//�������к������з���ָ��
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
	 * �������к������з���ָ��
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
			// �ж��Ƿ��ǵ�ǰ��������
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
					//�������к������з���ָ��
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
					//�������к������з���ָ��
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
	 * �������к������з���ָ�꣬�÷���ͬ���淽����֮ͬ�����ڣ�
	 * ��������Ӧλ����ָ�꣬�򷵻ؿ�ָ�꣬�÷���Ϊ����׷���ṩ
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
		// �ж��Ƿ��ǵ�ǰ��������
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
				//�������к������з���ָ��
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
	 * ����ָ�������Ӧ�ĵ�Ԫ���ϣ�������ϵ�Ԫֻ����һ����Ԫ����Ϊ��ϵ�Ԫ����λ��.
	 * Ҫ�󷵻����������к�������
	 * @param objArea
	 * @param objEnv
	 * @return
	 * @throws UfoCmdException
	 */
	private List<IArea> seperateArea(UfoFullArea objArea, UfoCalcEnv objEnv)throws UfoCmdException{
		List<IArea> cellList=null;
		if (objArea.isCurReportArea(objEnv)) {
			if (objEnv != null && objEnv.getDataChannel() != null) {
				//������ϵ�Ԫ��������ϵ�Ԫλ����
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
		// �õ�������ͱ����ʽ����
		RepFormatModelCache repFormatCache = objEnv.getRepFormatCache();
		ReportCache repCache = objEnv.getReportCache();

		// �õ�����PK
		String strRepCode = null;
		try {
			strRepCode = objArea.getReportCode(objEnv);
		} catch (CmdException e) {
			throw new UfoCmdException(e);
		}
		String strRepPK = repCache.getRepPKByCode(strRepCode);
		if (strRepPK == null) {
			// ����Ϊ�����ı�������
			String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufocalc000542",null,
					new String[] { strRepCode });
			throw new UfoCmdException(msg);
		}

		// �õ������ʽģ��

		CellsModel formatModel = repFormatCache
		.getUfoTableFormatModel(strRepPK);

		if (formatModel == null) {
			// ����{}��ʽ��δ����
			String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufocalc000543",null,
					new String[] { strRepCode });
			throw new UfoCmdException(msg);
		}
		return formatModel;
	}
	/**
	 * �������������Ӧ��ָ��
	 * @param objArea
	 * @param objEnv
	 * @return
	 * @throws UfoCmdException
	 */
	private Map getOtherRepMeasure(UfoFullArea objArea, UfoCalcEnv objEnv)throws UfoCmdException{

		CellsModel formatModel =getOtherCellsModel(objArea,objEnv);
		//tianchuan ���������λ�����ڶ�̬�����ǹ̶�����������ȡ�����ָ��
		if(formatModel==null){
			return null;
		}
		DynamicAreaModel dynModel=DynamicAreaModel.getInstance(formatModel);
		//�Ե�һ����Ԫ��Ϊ��׼���ж����ڶ�̬�����ǹ̶���
		ExtendAreaCell exCell = dynModel.getDynAreaCellByFmtPos(objArea.getArea().getStart());
		Map hashMvos = null;
		if(exCell==null){	//ѡ������λ�ڹ̶���
			/**
			 * key=CellPosition,value=MeasureVO
			 */
			hashMvos = CellsModelOperator.getMeasureModel(formatModel)
					.getMainMeasureVOByArea(objArea.getArea());
		}else{	//��Ϊ�գ�˵������λ�ڶ�̬��
			hashMvos = CellsModelOperator.getMeasureModel(formatModel).getDynAreaMeasureVOByArea(
					exCell.getExAreaPK(), objArea.getArea());
		}
		return hashMvos;
	}
	
	private Map getOtherRepStoreCell(UfoFullArea objArea, UfoCalcEnv objEnv)throws UfoCmdException{
		
		ReportCache repCache = objEnv.getReportCache();

		// �õ�����PK
		String strRepCode = null;
		try {
			strRepCode = objArea.getReportCode(objEnv);
		} catch (CmdException e) {
			throw new UfoCmdException(e);
		}
		String strRepPK = repCache.getRepPKByCode(strRepCode);
		if (strRepPK == null) {
			// ����Ϊ�����ı�������
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
	 * �˷�������˹�ʽtable.checkִ��ʱʹ�á�
	 * @param objArea
	 * @param objEnv
	 * @return
	 * @throws UfoCmdException
	 */
	protected String[] getCellsWithMeasure(UfoFullArea objArea, UfoCalcEnv objEnv) throws UfoCmdException
	{	
		if( objArea != null ){
			//�ж��Ƿ��ǵ�ǰ��������
			if( objArea.isCurReportArea(objEnv)){
				if( objEnv != null && objEnv.getUfoDataChannel() != null){
					Object[][] objs=objEnv.getUfoDataChannel().getExtAreaData(objArea.getArea(),IUFOTableData.MEASURE);
					if(objs!=null && objs.length>1 && objs[1]!=null){
						return (String[])objs[1];
					}
				}

			}else{
				//�õ�������ͱ����ʽ����
				nc.pub.iufo.cache.RepFormatModelCache repFormatCache = objEnv.getRepFormatCache();
				nc.pub.iufo.cache.ReportCache  repCache = objEnv.getReportCache();

				//�õ�����PK
				String strRepCode = null;
				try{
					strRepCode = objArea.getReportCode(objEnv);
				}catch(CmdException e){
					throw new UfoCmdException(e);
				}
				String strRepPK = repCache.getRepPKByCode(strRepCode);
				if( strRepPK == null){
					//����Ϊ�����ı�������
					String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","miufocalc000542",null,
							new String[]{strRepCode});
					throw new UfoCmdException(msg);
				}
				//�õ������ʽģ��
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
	 * �õ�ָ���Ӧ�Ĺؼ������PK�����û���ҵ��׳��쳣��
	 * �������ڣ�(2003-8-7 10:04:39)
	 * @return java.lang.String
	 * @param objReortCache nc.pub.iufo.cache.ReportCache
	 * @exception com.ufsoft.iufo.util.parser.UfoCmdException �쳣˵����
	 */
	public UfoVal[] getNullVal(UfoCalcEnv objEnv)
	throws UfoCmdException {
		try {
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
			if (nFID == MeasFuncDriver.FMSELECTS 
					|| nFID == MeasFuncDriver.FMSELECTA
//					|| nFID == MeasFuncDriver.FHBMSELECTA
					) {//�����Ժ�����FHBMSELECTA�����ж�
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
//	* �˴����뷽��������
//	* �������ڣ�(2003-8-7 10:34:29)
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
//	|| nFID == MeasFuncDriver.FHBMSELECTA) {//����"HBMSELECTA"
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
	 * ���غ�������ķ���ֵ���͡�
	 * �������ڣ�(2002-5-29 16:17:28)
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
	 * ����ָ������������ֵ��
	 * ���ָ���ǵ�ǰ����̬�����ָ����߱���̬���������ָ�꣬����ָ��������к���������ô���м��㣬
	 * ���ָ���������������ʾ�뵱ǰ���㻷����ͬ������ָ���������������ô������ȡֵ
	 * �����ǰ���㻷�����������㻷��һ�£�����Ԥ����ֵ���������м���
	 * �������ڣ�(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
	 */
	protected UfoVal[] getSelectValue(UfoCalcEnv objEnv) throws CmdException {
		try {

			if (objEnv == null) {
				throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
			}
			UfoCalcEnv env = (UfoCalcEnv) objEnv;
			//�ж�ָ���Ƿ��ǵ�ǰ����̬�����е�ָ�꣬
			if (isMeasReferDynArea(env)) {
				//a.����ǣ����ö�̬����ָ��ȡ���������㷽����
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
////				����hb����,�Ƚ������Ƿ���ͬ, add by ljhua 2006-8-16
//				String strParamTask=env.getMeasureEnv().getFormulaID();
//				String strEnvTask=(String) env.getExEnv(UfoCalcEnv.EX_TASK_ID);
//				if(strEnvTask==null ||  strEnvTask.equals(strParamTask))
//					bSameTask=true;
//				else
//					bSameTask=false;
//			}
			
			boolean bOnlyOneParam=isOnlyOneParamMselect();
			//tianchuan �԰汾��Ԥ����
			Integer preVer=null;
			if(bOnlyOneParam){
				preVer = getVerParamVal(objEnv);
				if(preVer!=null && preVer>0){	//����ܹ�ȡ���汾����ô��Ϊ�汾��Ϊ�գ�������Ψһ
					bOnlyOneParam=false;
				}
			}
			

			//�жϲ��������Ƿ�Ϊ1��
			if (bOnlyOneParam) {
				//�ж�ָ���Ƿ�������ǰ����
				if (isInEnvRep(objEnv)) {
					//���ָ��Ϊ��ǰ��������ָ��,��ֱ�Ӵ�����ȡֵ��
					strKeyGroupPK =
						(objEnv.getMeasureEnv() == null ? null : objEnv.getMeasureEnv().getKType());
					return getSelectValueFromArea(
							strKeyGroupPK,
							null,
							null,
							null,
							null,
							objEnv);
				} else if (strKeyGroupPK == null) { //�������û�йؼ������PK��ָ�꣬��������ָ�궼���������������ô����
					throw new UfoCmdException("miufo1000408");  //"ָ�겻��ȷ��"
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
				//b.�����ж��������Ƿ������򣨰��������������ָ�꺯������
				boolean bRelaArea = false;
				UfoExpr objOffset = getOffsetParamVal();
				//[a].������ų���
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
				//[b].�������������ֵ������ƫ�����͹ؼ���������
				Double nOffset = null;
				if (objOffset != null) {
					nOffset = new Double(objOffset.getValue(objEnv)[0].doubleValue());
				}
				UfoExpr objKeyCondValue = null;
				boolean bKeyCondSame = true;
				//���ȴ�Ԥ����Ľ����ȡ
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
				
				//����ǹ�ʽ׷��
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
				
				//�ж�ָ���Ƿ�ǰ����ָ��,�����뵱ǰ���㻷���Ƿ�һ��
				if (isInEnvRep(objEnv)
						&& (nOffset == null || nOffset.intValue() == 0)
						&& (nVer == null
								|| (nVer.intValue()
										== (objEnv.getMeasureEnv() == null ? 0 : objEnv.getMeasureEnv().getVer())))
										&& (objKeyCond == null || bKeyCondSame)
										&& bSameTask ) {
					//{a}���һ�£��ӱ�����ȡ����
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
					throw new UfoCmdException("miufo1000408");  //"ָ�겻��ȷ��"
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
					//{b}���򣬷���Ԥ����ֵ��
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
	 * ����������Բ�����ֵ
	 * @return
	 */
	private DatePropVO getDatePropParamVal() {
		ArrayList alPara = getParams();	
//		if(getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECT) 
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTS)
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTA)){//���ӡ�HBMSELECTA��
//			return (DatePropVO) alPara.get(2);
//		}else{
			return (DatePropVO) alPara.get(1);
//		}
	}
	/**
	 * ��ùؼ�������������ֵ
	 * @return
	 */
	protected UfoExpr getKeyCondParamVal() {
		ArrayList alPara = getParams();
//		if(getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECT) 
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTS)
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTA)){//���ӡ�HBMSELECTA��
//			return (UfoExpr) alPara.get(4);
//		}
//		else{
			return (UfoExpr) alPara.get(3);
//		}	
	}

	/**
	 * ��������뵱ǰ���㻷��һ�£�����ָ����������������ģ�ʹ�ô˷������㡣
	 * �������ڣ�(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
	 */
	private UfoVal[] getSelectValueFromArea(String strKeyGroupPK,
			DatePropVO objDateProp, Double nOffset, UfoExpr objKeyCondValue,
			Integer nVer, UfoCalcEnv objEnv) throws CmdException {

		//ָ��׷�ٿ���Ҳ����ȼ�����̡�liuyy.
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
				// �������ͨ��Ϊ�գ���ӻ�������ݿ��л������
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
								// ȡ�洢��Ԫ��ֵ
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
			// modify by ljhua 2006-8-16 �����ѭ������
			// return getValue(objEnv);
			return new UfoVal[] { UfoNullVal.getSingleton() };

		} catch (UfoParseException e) {
			throw new UfoCmdException(e);
		}
	}
	/**
	 * ��������뵱ǰ���㻷��һ�£�����ָ����������������ģ�ʹ�ô˷������㡣 �������ڣ�(2003-8-7 15:28:32)
	 * 
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv
	 *            com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException
	 *                �쳣˵����
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
//		throw new UfoCmdException("miufo1000409", new String[]{getMeasures()[0].getName()});  //"ָ��" + getMeasures()[0].getName() + "�������޹أ�"
//		}

	}
	/**
	 * �����ͬ������ָ��ֵ�Ѿ�ȡ��������(ͨ���������㷽ʽ�ѷ��뻺��)����ôֱ�Ӷ������������BO�����ݿ��л�ý����
	 * �������ڣ�(2003-8-8 10:06:43)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures nc.vo.iufo.measure.MeasureVO[]
	 * @param objDateProp nc.vo.iufo.keyword.DatePropVO
	 * @param nOffset java.lang.Double
	 * @param objKeyCond com.ufsoft.iufo.util.expression.UfoExpr
	 * @param nVer java.lang.Integer
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
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
			//�ж��Ƿ�Ϊhb����
//			boolean bHB=isHBfunc();

			//ָ��׷�ٿ���Ҳ����ȼ�����̡�liuyy.
			measureTrace(objEnv);
			
			//hashSelectValue��ͨ��������������(calcFuncValues)���
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
			//add by ljhua 2006-8-16 ����hbmselect��������������id
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
	 * ����ָ������������ֵ��
	 * ���ָ���ǵ�ǰ����̬�����ָ����߱���̬���������ָ�꣬����ָ��������к���������ô���м��㣬
	 * ���ָ���������������ʾ�뵱ǰ���㻷����ͬ������ָ���������������ô������ȡֵ
	 * �����ǰ���㻷�����������㻷��һ�£�����Ԥ����ֵ���������м���
	 * �������ڣ�(2003-8-7 15:28:32)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprParser
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
	 */
	protected UfoVal[] getStatValue(UfoCalcEnv objEnv) throws CmdException {
		java.util.ArrayList alPara = getParams();

		if(objEnv == null ){
			throw new UfoCmdException(UfoCmdException.ERR_UFOCALCENV);
		}
		if ((objEnv instanceof ReportDynCalcEnv)) {
			ReportDynCalcEnv env = (ReportDynCalcEnv) objEnv;
			if (env.getDataChannel()!=null && isMeasReferDynArea(env)) {
				//1.���ָ���Ƿ��ǵ�ǰ����̬�����е�ָ��
				return calcDynStatValue(env,(UfoExpr) getParams().get(1));
			}
		}
		//2.���ָ��������ǰ����,��ָ��δ�����ݿ��ֶ�
		IStoreCell[] mvos=getMeasures(objEnv);
		if (isInEnvRep(objEnv) && mvos[0].getDbcolumn() == null) {
			return new UfoVal[] { getSelectValueFromArea(mvos[0], objEnv)};
		}
		//3.�����ж��������Ƿ������򣨰��������������ָ�꺯������
		UfoExpr objKeyCond = (UfoExpr) alPara.get(1);
		UfoExpr objKeyCondValue = objKeyCond;

		UfoVal[] valReturn=null;
		if (MeasFuncDriver.isRelaWithArea(objKeyCond, objEnv)) {
            // ͳ�ƺ�������Ҫת���µ��������ʽ
//			objKeyCondValue = objKeyCond.solveFixedValue(objEnv);
			if (isInEnvRep(objEnv)) {
				//���ָ���ڵ�ǰ�����ڣ������м���
				valReturn= calcStatValues(mvos, objKeyCondValue, objEnv);
			} else {
				//����,����Ԥ����ֵ����Ԥ����ֵΪ�գ������м��㡣
				if(mvos!=null && mvos.length>0){
					valReturn=new UfoVal[mvos.length];
					for(int i=0;i<mvos.length;i++)
						valReturn[i]= getStatValueMeasNotRelaArea(mvos[i], objKeyCondValue, objEnv);
				};
			}

		} else {
			if (isInEnvRep(objEnv)) {
				//ָ��������ǰ����
				// �жϼ�������Ƿ��ô���ֵ���ʹ���
				boolean isBigNumber = isBigNumber(objEnv);
				if(mvos[0].getType() == IStoreCell.TYPE_BIGDECIMAL || isBigNumber) {
					valReturn= getStatValueFromCacheByUFDouble(mvos, objKeyCondValue, objEnv);
				} else {
					valReturn= getStatValueFromCache(mvos, objKeyCondValue, objEnv);
				}
			} else if (getValue() != null) {
				//ָ��ֵ�ѻ�ã���ֱ��ȡ
				valReturn= getValue();
			} else {
				//����ָ�꣬��ָ��ֵδ���ʱ
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
	 * ����ǹ̶����Ѿ�������Ԥ����ָ��ͳ�ƺ����ô˷������㡣
	 * �������ڣ�(2003-8-8 10:06:43)
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures nc.vo.iufo.measure.MeasureVO
	 * @param objKeyCond com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException �쳣˵����
	 */
	protected UfoVal[] getStatValueFromCache(
			IStoreCell[] objMeasures,
			UfoExpr objKeyCond,
			UfoCalcEnv objEnv)
	throws CmdException {
		try {

			if (objMeasures == null || objMeasures.length==0) {
				throw new UfoCmdException("miufo1000406"); //"����ָ��ͳ�ƺ���ʱ������ָ�겻���ڣ�"
			}
			
			//tianchuan
			//ָ��׷�ٿ���Ҳ����ȼ�����̡�liuyy.
			measureTrace(objEnv);
			
			int iMeasureLen=objMeasures.length;
			
			//1.�жϵ�ǰ���㻷���Ƿ��������
			boolean bCurEnvPropt = false;
			if (objKeyCond == null) {
				bCurEnvPropt = true;
			} else {
				bCurEnvPropt = objKeyCond.calcExpr(objEnv)[0].doubleValue() == 1;
			}

			int[]  nCount = new int[iMeasureLen];
			//1.1 ���ó�ʼֵ

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
			//1.2��õ�ǰ�ؼ���ֵ�ִ�strCurKeyValue
			String strCurKeyValue = null;
			//�����ǰ���㻷��������������ָ���������������ôȡ����������ֵ
//			if (objEnv instanceof UfoCalcEnv) {
//				UfoCalcEnv env = (UfoCalcEnv) objEnv;
//				if (bCurEnvPropt) {
					//2.1��õ�ǰָ���������ֵdValue
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

//						//���ָ���������������ô�ӹ�������ȡֵ
//						if(objEnv.isMainMeasure(objMeasure.getCode())){
//						nCount++;
//						dValue =objEnv.getMainMeasureValue(objMeasure.getCode()).doubleValue();
//						}
//						else{
//						return getValue();
//						}
//					}
					//2.2��õ�ǰ�ؼ���ֵ��ɵ��ִ�strCurKeyValue
//					strCurKeyValue=getCurrentStrKeyValue(objEnv);
//				}
//			}
//			1.3 �����ǰ���㻷��������������ָ���������������ôȡ����������ֵ
//			if (objEnv instanceof UfoCalcEnv) {
			UfoVal[] objVals = new UfoVal[iMeasureLen];
			if (objEnv != null) {
//				UfoCalcEnv env = (UfoCalcEnv) objEnv;
				if (bCurEnvPropt) {
					strCurKeyValue=getCurrentStrKeyValue(objEnv);
					//2.1��õ�ǰָ���������ֵdValue
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
				
				//3.��ָ�꺯���������ҵ�ָ���Ӧ������ֵ�������ݺ�����������Ӧ�ļ��㡣
				MeasFuncDriver objFuncDriver = (MeasFuncDriver) objEnv
				.loadFuncListInst().getExtDriver(
						MeasFuncDriver.class.getName());

				//�����ҵ�������Ӧ�����ݼ���
				/**  ,											   
				 * hashValue (key = strCond,value=(hashtable key=ָ��pk,CURKEY
				 * 											 value=hashtable(key=MSUM,MMAX,MMIN,MAVG,
				 * 															value=�ų���ǰ�ؼ�ֵ��ĺϼơ������С������,��ǰ�ؼ���ֵ�ִ���) )
				 */
//				Hashtable hashValue = objFuncDriver == null ? null
//						: objFuncDriver.getMeasValue(MeasFuncDriver.SUMDATA);

				Hashtable hashValue = getMsumDataHashValue(objFuncDriver);

				
				//4.��������������ݣ�������ݿ�װ�غ���ֵ
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
					throw new UfoCmdException("miufo1000407"); //"ָ���Ӧ�ı����Ҳ�����"
				}
				String strKeyGroupPK = objRep.getPk_key_comb();

				hashValue = (Hashtable) hashValue
				.get(strKeyGroupPK
						+ "\r\n"
						+ (objKeyCond == null ? "" : objKeyCond.toString()
								+ "\r\n"));
				/**
				 * hashValue  key=ָ��pk,CURKEY value=hashtable(key=MSUM,MMAX,MMIN,MAVG,value=�ų���ǰ�ؼ�ֵ��ĺϼơ������С������,��ǰ�ؼ���ֵ�ִ���)
				 */
				if (hashValue == null) {
					return calcStatValues(objMeasures, objKeyCond, objEnv);
				}
				//���Ԥ�����ֵ�Ƿ���ȷ������ǰ�Ĺؼ���ֵ�ִ���Ԥ����洢�Ĺؼ���ֵ�Ƿ���ͬ����ͬ��ȡԤ����ֵ���������м���.
				if(bCurEnvPropt){
					String strCurKeyTemp=(String) hashValue.get(TAG_CURKEYVALUE);
					if (! strCurKeyTemp.equals(strCurKeyValue)) {
						return calcStatValues(objMeasures, objKeyCond, objEnv);
					}
				}

				//5.���ݺ������ͣ����㺯��ֵ

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
					 * hashMeasValueTemp, key=MSUM,MMAX,MMIN,MAVG,value=�ų���ǰ�ؼ�ֵ��ĺϼơ������С������,��ǰ�ؼ���ֵ�ִ���
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
				throw new UfoCmdException("miufo1000406"); //"����ָ��ͳ�ƺ���ʱ������ָ�겻���ڣ�"
			}

			//ָ��׷�ٿ���Ҳ����ȼ�����̡�liuyy.
			measureTrace(objEnv);
			
			int iMeasureLen=objMeasures.length;
			
			//1.�жϵ�ǰ���㻷���Ƿ��������
			boolean bCurEnvPropt = false;
			if (objKeyCond == null) {
				bCurEnvPropt = true;
			} else {
				bCurEnvPropt = objKeyCond.calcExpr(objEnv)[0].doubleValue() == 1;
			}

			int[]  nCount = new int[iMeasureLen];
			//1.1 ���ó�ʼֵ

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
			//1.2��õ�ǰ�ؼ���ֵ�ִ�strCurKeyValue
			String strCurKeyValue = null;
			//�����ǰ���㻷��������������ָ���������������ôȡ����������ֵ
//			if (objEnv instanceof UfoCalcEnv) {
//				UfoCalcEnv env = (UfoCalcEnv) objEnv;
//				if (bCurEnvPropt) {
					//2.1��õ�ǰָ���������ֵdValue
//					if (env != null && objEnv.getUfoDataChannel() != null) {
//
//					}
					//2.2��õ�ǰ�ؼ���ֵ��ɵ��ִ�strCurKeyValue
//					strCurKeyValue=getCurrentStrKeyValue(objEnv);
//				}
//			}
//			1.3 �����ǰ���㻷��������������ָ���������������ôȡ����������ֵ
//			if (objEnv instanceof UfoCalcEnv) {
			UfoVal[] objVals = new UfoVal[iMeasureLen];
			if (objEnv != null) {
				//2.2��õ�ǰ�ؼ���ֵ��ɵ��ִ�strCurKeyValue
				strCurKeyValue=getCurrentStrKeyValue(objEnv);
//				UfoCalcEnv env = (UfoCalcEnv) objEnv;
				if (bCurEnvPropt) {
					//2.1��õ�ǰָ���������ֵdValue
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
				
				//3.��ָ�꺯���������ҵ�ָ���Ӧ������ֵ�������ݺ�����������Ӧ�ļ��㡣
				MeasFuncDriver objFuncDriver = (MeasFuncDriver) objEnv
				.loadFuncListInst().getExtDriver(
						MeasFuncDriver.class.getName());

				//�����ҵ�������Ӧ�����ݼ���
				/**  ,											   
				 * hashValue (key = strCond,value=(hashtable key=ָ��pk,CURKEY
				 * 											 value=hashtable(key=MSUM,MMAX,MMIN,MAVG,
				 * 															value=�ų���ǰ�ؼ�ֵ��ĺϼơ������С������,��ǰ�ؼ���ֵ�ִ���) )
				 */
//				Hashtable hashValue = objFuncDriver == null ? null
//						: objFuncDriver.getMeasValue(MeasFuncDriver.SUMDATA);
				
				Hashtable hashValue = getMsumDataHashValue(objFuncDriver);
				
				//4.��������������ݣ�������ݿ�װ�غ���ֵ
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
					throw new UfoCmdException("miufo1000407"); //"ָ���Ӧ�ı����Ҳ�����"
				}
				String strKeyGroupPK = objRep.getPk_key_comb();

				hashValue = (Hashtable) hashValue
				.get(strKeyGroupPK
						+ "\r\n"
						+ (objKeyCond == null ? "" : objKeyCond.toString()
								+ "\r\n"));
				/**
				 * hashValue  key=ָ��pk,CURKEY value=hashtable(key=MSUM,MMAX,MMIN,MAVG,value=�ų���ǰ�ؼ�ֵ��ĺϼơ������С������,��ǰ�ؼ���ֵ�ִ���)
				 */
				if (hashValue == null) {
					return calcStatValues(objMeasures, objKeyCond, objEnv);
				}
				//���Ԥ�����ֵ�Ƿ���ȷ������ǰ�Ĺؼ���ֵ�ִ���Ԥ����洢�Ĺؼ���ֵ�Ƿ���ͬ����ͬ��ȡԤ����ֵ���������м���.
				if(bCurEnvPropt){
					String strCurKeyTemp=(String) hashValue.get(TAG_CURKEYVALUE);
					if (! strCurKeyTemp.equals(strCurKeyValue)) {
						return calcStatValues(objMeasures, objKeyCond, objEnv);
					}
				}

				//5.���ݺ������ͣ����㺯��ֵ

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
					 * hashMeasValueTemp, key=MSUM,MMAX,MMIN,MAVG,value=�ų���ǰ�ؼ�ֵ��ĺϼơ������С������,��ǰ�ؼ���ֵ�ִ���
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
	 * ����ǹ̶����Ѿ�������Ԥ����ָ��ͳ�ƺ����ô˷������㡣 �������ڣ�(2003-8-8 10:06:43)
	 * 
	 * @return com.ufsoft.iufo.util.expression.UfoVal[]
	 * @param objMeasures
	 *            nc.vo.iufo.measure.MeasureVO
	 * @param objKeyCond
	 *            com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv
	 *            com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @exception com.ufsoft.iufo.util.parser.CmdException
	 *                �쳣˵����
	 */
	protected UfoVal getStatValueMeasNotRelaArea(
			IStoreCell objMeasure,
			UfoExpr objKeyCond,
			UfoCalcEnv objEnv)
	throws CmdException {
		try {
			if (objMeasure == null) {
				throw new UfoCmdException("miufo1000406");  //"����ָ��ͳ�ƺ���ʱ������ָ�겻���ڣ�"
			}
			//��ȡ�ؼ������PK
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
					throw new UfoCmdException("miufo1000407");  //"ָ���Ӧ�ı����Ҳ�����"
				}
				String strKeyGroupPK = objRep.getPk_key_comb();
				//��ָ�꺯���������ҵ�ָ���Ӧ������ֵ�������ݺ�����������Ӧ�ļ��㡣
				MeasFuncDriver objFuncDriver =
					(MeasFuncDriver) objEnv.loadFuncListInst().getExtDriver(
							MeasFuncDriver.class.getName());

				//�����ҵ�������Ӧ�����ݼ���
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
	 * ��ð汾��Ϣ������ֵ
	 * @param objEnv
	 * @return
	 */
	public Integer getVerParamVal(UfoCalcEnv objEnv){
		
		//ȡ�����Ĳ����б�
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
		// �Ӽ��㻷����ȡ�ö������������汾��ȡ��
		Object verFetcher = objEnv.getExEnv(CommonExprCalcEnv.VERSION_FETCHER);
		if(verFetcher instanceof IVersionFetcher){
			return ((IVersionFetcher) verFetcher).getMselectVersion(objEnv, nOffset == null ? 0 : nOffset.intValue());
		}
			   
//		if(getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECT) 
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTS)
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTA) ){//���ӡ�HBMSELECTA��
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
	 * ���ʱ��ƫ�Ʋ�����ֵ
	 * @return
	 */
	private UfoExpr getOffsetParamVal() {
		ArrayList alPara = getParams();
//		if(getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECT) 
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTS)
//				|| getFuncName().equalsIgnoreCase(MeasFuncDriver.HBMSELECTA)){//���ӡ�HBMSELECTA��
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
	 * ָ�꺯��ȡֵ��ָ�꺯���ļ������
	 */
	public UfoVal[] getValue(ICalcEnv objEnv) throws CmdException {
		try {
			if (objEnv == null){
				// ���㻷��û�����ã�����ü���ģ��ĸ��������ü��㻷��֮����м��㣡
				throw new CmdException("miufo1000411");
			}
			
			if (!(objEnv instanceof UfoCalcEnv)) {
				// ָ�꺯����IUFO���еĺ��������㻷��������UfoExprCalcEnv���������࣡
				throw new UfoCmdException("miufo1000413");
			}
			
			UfoCalcEnv env = (UfoCalcEnv) objEnv;
			
			short nFID = MeasFuncDriver.getFuncIdByName(getFuncName());
//			if(nFID == MeasFuncDriver.FMSELECTA || nFID == MeasFuncDriver.FHBMSELECTA
//					|| nFID ==MeasFuncDriver.FMSUMA) {
//				// �����MSELECTA/MSUMA/HBMSELECTA��������Ҫ�ж����õ������������ű������õ����㻷����
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
			
			// ����ֵ
			UfoVal[] valReturns=null;
			
	        if (m_exFunc != null) {
				try {
					m_exFunc.setCurMeasure((MeasureVO)this.getMeasures((UfoCalcEnv) objEnv)[0]);
					return m_exFunc.getValue(objEnv);
				} catch (Exception e) {
				}
			}

//			if (nFID == MeasFuncDriver.FCODENAME) {
//				// CODENAME����
//				UfoVal objBM = ((UfoExpr) getParams().get(0)).calcExpr(objEnv)[0];
//				if (objBM.getType() != UfoVal.TYPE_STRING) {
//					// ����Ӧ����һ���ַ���ֵ�ı��ʽ��
//					throw new UfoCmdException("miufo1000412");
//				}
//				valReturns=new UfoVal[] {UfoVal.createVal(MeasFuncBO_Client.calcCodeNameValue((String) objBM.getValue(),
//					(String) getParams().get(1)))};
//				return valReturns;
//			}
	        
			if (nFID==MeasFuncDriver.FMSELECT
					|| nFID==MeasFuncDriver.FMSELECTA
					|| nFID==MeasFuncDriver.FMSELECTS) {

				// ָ��׷�ٿ���Ҳ����ȼ������
				measureTrace(env);
				
				valReturns= getSelectValue(env);
				
				//add by ljhua 2007-1-23 ���mselecta������ֵ��������λ�ö�Ӧ
				if(nFID==MeasFuncDriver.FMSELECTA){
					valReturns= combineEmptyValue((UfoFullArea) getParams().get(0), (UfoCalcEnv) env, valReturns);
				}
			} else if (nFID==MeasFuncDriver.FMCOUNT) {
				// MCOUNT����ȡֵ
				valReturns= getCountValue(env);
			} else {
				// ͳ�ƺ���ȡֵ
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
	 * ���ڱ������ã����뵱ǰ���㻷��һ�µ������ֱ�ӴӼ��㻷���ж�ȡָ��׷����Ϣ
	 * 
	 * @create by liuchuna at 2010-6-7,����11:20:58
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
	 * ����ֵ������ȴʡΪ1
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

//			case MeasFuncDriver.FHBMSELECTA ://������HBMSELECTA��
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
	��  ������
	����ֵ������ֵ����
	��  �ܣ����غ���ֵ����.
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
//			case MeasFuncDriver.FHBMSELECTA ://������HBMSELECTA��

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
	 * �жϸú����Ƿ���ȡ��ǰ��ҳ��ֵ��
	 * �������ڣ�(2004-1-6 11:03:12)
	 * @author�����
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
	 * �жϺ����е�ָ���Ƿ��ǵ�ǰ����̬�����е�ָ�꣬
	 * �������ڣ�(2003-8-7 15:58:49)
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
	 * ���ָ��������̬����pk��
	 * �������ڣ�(2003-8-7 15:58:49)
	 * @return boolean
	 */
	public static boolean isMeasReferDynArea(MeasureVO objMeasure,UfoCalcEnv ObjExprEnv) {

		if (objMeasure!=null && ObjExprEnv!=null) {
			//���ָ��������̬����pk
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
			//���ָ��������̬����pk
			if(ObjExprEnv.getDynPKByStoreCellPos(CellPosition.getInstance(storeCellVo.getCode()))!=null)
				return true;
//			com.ufsoft.iuforeport.reporttool.data.UfoTable objTable =
//			objEnv.getDataChannel().getTable();
//			return objTable.getDynPKByMeasurePK(objMeasure.getCode()) != null;
		}
		return false;
	}

	/**
	 * �ж��Ƿ���ָ���뵱ǰ�������������
	 * �������ڣ�(2003-8-7 10:46:55)
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
					||nFID == MeasFuncDriver.FMSUMA){//������HBMELECTA��
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
	 * ����Ƿ���������ء� �������ڣ�(2003-6-20 10:21:44)
	 * 
	 * @return boolean
	 * @param objEnv
	 *            com.ufsoft.iufo.util.parser.ICalcEnv
	 * @param nFlag
	 *            int nFlag=2,�������в����Ƿ�UfoFullArea�����⣬�����ָ������Ƿ���������������Ƿ����档
	 *            nFlag=3,����˼��UfoFullArea����ָ�꺯���еڶ��������Ժ��ָ�����������Ҳ������.
	 *            ָ����������ر�ʾָ��������ǰ���㻷������
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
	 * ��ôӵ�ǰ�п�ʼָ�������Ĺؼ������ݼ���
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
	 * ����ָ��������������ȡ��������
	 * @param objEnv
	 * @param dynKeyDatas
	 * @param strExprDynKeyPKs ͳ�ƺ������������а����Ķ�̬����ؼ���pk����
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
			
// ����ָ�깫ʽ׷�ٽ������ֵ  liuyy.
			if(objEnv.isMeasureTrace()){
				MeasureTraceVO[] mtvos = (MeasureTraceVO[]) hashReturn.get(ICalcEnv.MEASURE_TRACE_FLAG);
				hashReturn.remove(ICalcEnv.MEASURE_TRACE_FLAG);
				objEnv.setMeasureTraceVOs(mtvos);
			}
			
			//����������ø��еĹؼ���ֵ��Ϣ
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
	 * ����ָ��������������ȡ��������
	 * @param objEnv
	 * @param dynKeyDatas
	 * @param strExprDynKeyPKs ͳ�ƺ������������а����Ķ�̬����ؼ���pk����
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
			
			// ����ָ�깫ʽ׷�ٽ������ֵ  liuyy.
			if(objEnv.isMeasureTrace()){
				MeasureTraceVO[] mtvos = (MeasureTraceVO[]) hashReturn.get(ICalcEnv.MEASURE_TRACE_FLAG);
				hashReturn.remove(ICalcEnv.MEASURE_TRACE_FLAG);
				objEnv.setMeasureTraceVOs(mtvos);
			}
			
			//����������ø��еĹؼ���ֵ��Ϣ
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
	 * �����ݿ���ȡͳ�ƺ�������
	 * 
	 * @create by liuchuna at 2010-6-7,����01:41:12
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
			//ָ�꺯����̨����ǰ����Ҫ�����������������⻷��������
			Hashtable hashExEnv = objEnv.getExEnv();
			if(objEnv.getLoginUnitId() != null){
				hashExEnv.put(CommonExprCalcEnv.EX_LOGINUNIT_ID, objEnv.getLoginUnitId());
			}
			Hashtable aggrDatas = null;
			if (objEnv.isClient() == false) {
				// ��̨����
//				MeasFuncBO objMeasFuncBO = new MeasFuncBO();
				aggrDatas = MeasFuncBO_Client.getAggrDatas(getMeasures(objEnv)[0],
						(UfoExpr) getParams().get(1), objEnv.getMeasureEnv(),
						hashExEnv, objEnv.getRepPK(), objEnv.getKeys(),
						objKeyDatas, objEnv.getDataChannel());

			} else {
				// ǰ̨����
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
	 * �����ݿ��ж�ȡ�ѱ����ָ������
	 * 
	 * @return java.util.Hashtable
	 * @param objEnv
	 *            com.ufsoft.iufo.util.parser.UfoExprCalcEnv
	 * @param mvo MeasureVO
	 * @exception com.ufsoft.iufo.util.parser.CmdException
	 *                �쳣˵����
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
			//ָ�꺯����̨����ǰ����Ҫ�����������������⻷��������
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
	 * �ڼ���֮ǰ������������ָ�������ֵ.
	 * Creation date: (2003-3-21 15:37:52)
	 * @param mVOs MeasureVO[]
	 * @param env UfoCalcEnv
	 * @return boolean ���
	 */
	private boolean saveMeasValueByArea(IStoreCell[] mVOs, UfoCalcEnv env)
	throws CmdException {

		boolean bModified = false;
		if (env == null
				|| env.getDataChannel()== null)
			return bModified;
		//�õ�ָ��
		for (int i = 0; i < mVOs.length; i++) {
			if (mVOs[i] != null) {
				//���mVO��ĳ����Ԫ��������Ҫ�õ��õ�Ԫ��ֵ�����õ�env
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
	 * ������������ת��ΪSQL���׷�ӵ��������У�
	 * �漰�����ݿ���ŵ����С�
	 * �������ڣ�(2002-5-13 10:11:33)
	 * @param strMainRepKeyGroupPK String �������������ؼ����������
	 * @param alElements ArrayList ���ʽ��������ʽ
	 * @param nPos int ��ǰ�����Ԫ��������λ��
	 * @param sbBuf java.lang.StringBuffer
	 * @param dbTable java.util.Hashtable
	 * @param env com.ufsoft.iufo.util.parser.UfoCalcEnv
	 * @return int ���ش���������һ��Ԫ�ص�λ��
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

			if(nFID == MeasFuncDriver.FMSELECT && bCurPageMeas){//�����MSelect��������ÿ����������ֵ
				MeasOperand objMeasure = (MeasOperand)getParams().get(0);
				objMeasure.toSQL(strMainRepKeyGroupPK, sbBuf, dbTable, objEnv);
				return;
			}

			if (!(objEnv instanceof UfoCalcEnv) || (nFID == MeasFuncDriver.FMSELECTA && !(objEnv instanceof UfoCalcEnv))) {
				throw new TranslateException(TranslateException.ERR_ENV);//���㻷������
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
	 *2003-11-20����޸��㷨���Ǹ÷������������㷨����������㷨�ǰ���˳��ȷ���ģ����ǰ��û���׳��쳣����ʾǰ���ֵ�Ѿ�ȷ����ֵ��
	 *1�������δ���壬��strReviseTimes�е�����棬���strReviseTimes����Ҳδ���壬�׳��쳣
	 *2������������ܣ���ô����ȷ���������е�ʱ����Ϣ�󷵻�
	 *3�������������������û�ж��壬��ô��strReviseTimes�е�����ȷ����ʱ�䣬���strReviseTimes��ʱ����2��29�գ���ô���궨λ2��28��
	 *4���������δ���壬��ô�ü���ȷ�����꣬�����Ҳû�ж�����ô������ȷ������ͼ�����������¶�û�ж�����strReviseTimes�еİ��ꡢ��������ȷ�����������û��ȷ���׳��쳣
	 *5�������û�ж��壬��ô������ȷ�����Ͱ��꣬�����Ҳδ�����ð�����ȷ�������£����ǲ���ȷ���׳��쳣
	 *6�������û�ж��壬��ô�ü���ȷ��
	 *7�����Ѯû�ж��壬��ô������ȷ��Ѯ�������û�ж�����strReviseTimes�е�Ѱ��ȷ��
	 *8�������û�ж��壬��ô��Ѯ��ȷ��
	 *9�������ٸ����Ѿ�ȷ�����ꡢ�¡���ȷ����
	 *10�������͵õ���������ʱ����Ϣ
	 *@end
	 * ����Ӧʱ�����Ե�ֵ�Ƿ�Ϸ���
	 * �������ڣ�(2003-8-11 18:53:12)
	 * @return boolean
	 * @param nType int
	 * @param nValue int
	 * @param strDateTimes String[]���������һ������Ϊ7�����飬�������δ�����ꡢ���ꡢ�����¡�Ѯ���ܡ���
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
    if (nYear == 0) { //�����Ϊnull����У��ֵ����
        strDateTimes[0] = strReviseTimes[0];
    }
    nYear =
        strDateTimes[0] == null ? 0 : Integer.valueOf(strDateTimes[0]).intValue();
    if (nYear == 0) {
        throw new TranslateException("miufo1000417");  //"����Ϣ������"
    }
    if (nWeek > 0) { //�������ֵ��������
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
                throw new TranslateException("miufo1000418");  //"ʱ����Ϣ������"
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
    if (nHY == 0) { //�������Ϊnull�������������ֵȡ��Ӧֵ��������У��ֵ����
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
            throw new TranslateException("miufo1000419");  //"������Ϣ������"
        }
    }
    if (nSeason == 0) { //�����Ϊnull���������ֵ���ö�Ӧֵ���룬������У��ֵ����
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
        throw new TranslateException("miufo1000420");  //"����Ϣ������"
    }
    if (nMonth == 0) { //�����Ϊnull���ü���У��ֵ��ͬ������ֵ����
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
        throw new TranslateException("miufo1000421");  //"����Ϣ������"
    }
    if (nTenday == 0) { //���ѮΪnull�� �������ֵ����Ӧֵ���룬������У��ֵ����
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
            throw new TranslateException("miufo1000422");  //"Ѯ��Ϣ������"
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
        throw new TranslateException("miufo1000423");  //"����Ϣ������"
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
	 * ���ڶ�ֵ�ĺ�����������Եڼ���ֵ�ĺ�����ʾ��
	 * UfoFunc�����࣬��������ṩ��ֵ�ĺ�����Ӧ����д��������
	 * 
	 * �˷�����˹�ʽtable.checkִ��ʱʹ�á�
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
			//mselecta��mselects
			if( nFid == MeasFuncDriver.FMSELECTA 
					|| nFid == MeasFuncDriver.FMSELECTS 
//					|| nFid == MeasFuncDriver.FHBMSELECTS
//					|| nFid == MeasFuncDriver.FHBMSELECTA
					){	//������HBMSELECTA��
				ArrayList alParams = getParams();
				ArrayList alNewParams = new ArrayList(alParams);		

				if( nFid ==MeasFuncDriver.FMSELECTA){	//������HBMSELECTA��
					if( env instanceof UfoCalcEnv ){
						UfoFullArea fa = (UfoFullArea)alParams.get(0);
//						��������õ���ָ��ĵ�Ԫ�б�
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
				//��������mselecta�в�������������
				if( a.hasReport() == false){
					//�ж�mselecta�������Ƿ�Ϊ��
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
	 * ������������ָ����뵽�б���
	 * nType = 0, ����ָ��
	 * nType = 3, ��Mselect, mselects��������������ָ��
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
//	* ����ָ��ȡ����������������
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
	 * ���ʱ����ʽΪ k('��')=2005 and k('��')=06 and k('��')=30��ʽʱ������,�ҷ�ʱ�������в����Ϊzkey��ʽ
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
	 * ����û�Ȩ�ޱ��ʽ
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
							throw new UfoCmdException(msg); //"�ؼ���"+objKeys[i]+"û�����ü��㻷��ֵ"
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
						throw new UfoCmdException(msg); //"�ؼ���"
						// +
						// objKeys[i]
						// +
						// "û�����ü��㻷��ֵ"
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
	 * ���zkey(��̬���ؼ���)��Ӧ��k()�����ؼ��ּ���
	 * @param expr
	 * @param dynKeyDataGroup
	 * @param strMainTimeKey
	 * @return
	 */
	private String[] getKeyRefDynFromExpr(UfoExpr expr,KeyDataGroup dynKeyDataGroup,String strMainTimeKey){
		if(expr==null || dynKeyDataGroup==null)
			return null;

		//1.��ö�̬����ؼ���pk����
		ArrayList listDynKeyPKs= getDynKeysFromKeyDatas(dynKeyDataGroup);

		return MeasCondExprUtil.getKeyByRefDynKey(expr,listDynKeyPKs,strMainTimeKey);
	}
	/**
	 * ֻ��ȡ����������mselect���Ǽ򵥲��������������Ǹ��ӵġ�
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
	 * �ж��Ƿ�Ϊ��ǰ����ָ��
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
					|| nFID==MeasFuncDriver.FMSUMA ){//������HBMELECTA��
				UfoFullArea  a = (UfoFullArea)getParams().get(0);
					//�ж��Ƿ�Ϊ��ǰ��������
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
