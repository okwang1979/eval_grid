package com.ufsoft.script.spreadsheet;

/**
 *@update
 *2004-01-08����m_hashModuleExecutor���ڻ���������ִ�ж���,getModuleExecutorByName�������ڵõ�ִ�ж���
 *@end
 *@update
 *2004-01-02���Ӽ��㻷��������Ϣ
 *@end
 * @update
 * �޸����õ�ǰ����ʹͬʱ����˽��ָ�굥λ��Ϣ
 * @end
 * @update
 * �޸���getFields����
 * @end
 * iufo������㻷���ࡣ�������㹫ʽ����Ҫ�Ļ���������
 * �������ڣ�(2002-5-13 10:19:23)
 * @author��Administrator
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import nc.pub.iufo.cache.KeyGroupCache;
import nc.pub.iufo.cache.KeywordCache;
import nc.pub.iufo.cache.MeasureCache;
import nc.pub.iufo.cache.RepFormatModelCache;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.data.thread.AbstractQueryData;
import nc.pub.iufo.data.thread.IufoThreadLocalUtil;
import nc.pub.smart.data.DataSet;
import nc.vo.bd.period.AccperiodschemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.MeasureTraceVO;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.iuforeport.rep.ReportVO;

import com.ufsoft.iufo.fmtplugin.datastate.ReportDataModel;
import com.ufsoft.iufo.repauth.RepDataAuthValidator;
import com.ufsoft.iufo.util.parser.IContextObject;
import com.ufsoft.script.base.ICalcEnv;
import com.ufsoft.script.base.UfoFieldObject;
import com.ufsoft.script.base.UfoNullVal;
import com.ufsoft.script.base.UfoVal;
import com.ufsoft.script.datachannel.ITableData;
import com.ufsoft.script.datachannel.IUFOTableData;
import com.ufsoft.script.expression.AreaExprCalcEnv;
import com.ufsoft.table.CellPosition;

public class UfoCalcEnv extends AreaExprCalcEnv {

	private static final long serialVersionUID = -2379950949500687454L;

	public final static String EX_TASK_ID = "ex_task_id";
	
	public final static String EX_IN_CHECK_SCHEME="ex_in_check_scheme";
	
	public final static String KEY_DATA_SET_CACHE = "key_DATE_SET_CACHE";
	
	public final static String KEY_DYN_KEYDATAGROUP_INDEX = "key_DYN_KEYDATAGROUP_INDEX";
	
	public final static String KEY_IS_KEYAREA_FML = "key_IS_KEYAREA_FML";
	
	public final static String KEY_IS_REP_MANAGE_FILE = "key_IS_REP_MANAGE_FILE";
	
	/**
	 * �����Ƿ��ڷ������ˣ�true��
	 */
	private boolean m_bOnServer = false;

	/**
	 * ����pk
	 */
	private String m_strRepPK;

//	/**
//	 * �����ڽ�����ʱ���¼��ǰ����������
//	 */
//	private String m_strTaskPK;
	//tianchuan ++ ��ǰ����PK
	private String taskPk=null;
	
	/**
	 * ����ڼ䷽��PK
	 */
	private String m_strAccPK;

	/**
	 * �����ڽ�����ʱ���¼��ǰ����ڼ䷽��
	 */
	private AccperiodschemeVO m_objSchemeVO = null;

	/**
	 * ���ݼ��������������Ӧ���ݼ���������������ԭ��
	 * 1��������ͬ�������������ݼ�����ʹ��ͬһ�����ݼ���
	 * 2��δ������������ݼ��������ʹ��ʱʹ��ͬһ��ʵ����
	 */
	private Hashtable<String, DataSet> m_hashParamCord2DataSet = new Hashtable<String, DataSet>();

	/**
	 * ����򱨱��Ӧ�Ĺؼ�����Ϣ����
	 */
	private KeyVO[] m_objKeys;

	/**
	 * ����Դ��Ϣ ��������ֻ���� UfoCalcEnvContext����getCurDataSource��ʹ��.
	 */
	private DataSourceVO m_oDataSource;

	/**
	 * ��½�û�����
	 */
	private String m_strLoginUserId;

	/**
	 * ��½��λpk
	 */
	private String m_strLoginUnitId;
	/**
	 * ��½ʱ��
	 *
	 */
	private String m_strLoginDate;

	/**
	 * ָ�����ݻ���
	 */
	private MeasurePubDataVO m_measurePubDataVO;

	private String m_strDbLang = null;

	// liuchun+ at 20110519��ǰ��֯���Բ�����Щ��֯�еı���
	private String[] orgs = null;
	// �Ƿ�У�鵱ǰ��֯�Ա���Ŀɼ���Χ����Ҫ���ڵ��빫ʽ�ĳ����б����¼���λ�����ϼ���λ�����ָ��
	private boolean isCheckOrgVisable = false;
	
	// ҵ������ȡ��Ȩ��У��
	private RepDataAuthValidator authValidator = null;
	
	// ���ݿ����ͣ��洢�ڼ��㻷���У�����ÿ�ζ�ȡ
	private String dbType = null;
	
	// private ContextVO m_contextVO; //��ǰ��
	// /**
	// * ������ִ�ж��󻺴棬key=modulename,value=UfoBCModuleInfo.getExecutor�����õ��Ķ���
	// */
	// private java.util.Hashtable m_hashModuleExecutor;
	
	private Map<String, Hashtable<String, IStoreCell>> storeCellMap = new Hashtable<String, Hashtable<String, IStoreCell>>();

	/**
	 * UfoCalcEnv ������ע�⡣
	 */
	public UfoCalcEnv(boolean bOnserver) {
		super();
		init();
		initCache(bOnserver);
	}



	/**
	 * UfoCalcEnv ������ע�⡣
	 */
	public UfoCalcEnv(UfoCalcEnv env) {
		super(env);

		if (env != null) {
			setMeasureEnv(env.getMeasureEnv());
			initCache(!env.isClient());

//			setTaskPK1(env.getTaskPK());
			setKeys(env.getKeys());

			setRepPK(env.getRepPK());
			setDataSource(env.getDataSource());
			if (env.getKeys() != null) {
				setKeys(env.getKeys());
			}
			setLoginDate(env.getLoginDate());
			setLoginUnitId(env.getLoginUnitId());
			setLoginUserId(env.getLoginUserId());

			//@edit by ll at ����10:12:20,ȷ�����ݼ�������ͬһ��ʵ��
			m_hashParamCord2DataSet = env.m_hashParamCord2DataSet;
			
			//tianchuan ++
			setTaskPk(env.getTaskPk());
			
			// m_measurePubDataVO = env.getMeasureEnv();
			// m_strTaskPK = env.getTaskPK();
			// m_objKeys = env.getKeys();
			// m_bOnServer = env.isClient();
			// m_bRepModel = env.isRepModel();
			// m_oDataSource = env.getDataSource();
			// //zyjun2004-06-18�޸ģ�����ÿ������MeasFuncDriver���Ӷ�ÿ�γ�ʼ��MeasFuncDriver�еĵ�λ�ṹ����
			// setFuncListInst(env.loadFuncListInst());
			//
			// setRepPK(env.getRepPK());

		} else {
			init();
		}
	}

	/**
	 *
	 * @param dataChannel
	 *            ����ͨ��
	 * @param strRepPK
	 *            ����pk
	 * @param strTaskPK
	 *            ����pk
	 * @param objKeys
	 *            �ؼ�����Ϣ����
	 * @param measurePubDataVO
	 *            ָ��������Ϣ
	 * @param extEnv
	 *            ��չ����
	 * @param bRepModel
	 *            �����Ƿ�ģ��
	 * @param bOnServer
	 *            �����Ƿ��ڷ�������
	 * @param dataSrcVO
	 *            ��������Դ
	 */

	private UfoCalcEnv(ITableData dataChannel, String strRepPK,
			String strTaskPK, KeyVO[] objKeys,
			MeasurePubDataVO measurePubDataVO, Hashtable extEnv, boolean bOnServer, DataSourceVO dataSrcVO) {
		super(extEnv);
		setDataChannel(dataChannel);
		setMeasureEnv(measurePubDataVO);
		initCache(bOnServer);
//		setTaskPK(strTaskPK);
		setRepPK(strRepPK);
		setDataSource(dataSrcVO);

		if (objKeys != null) {
			setKeys(objKeys);
		}else{
			String strKeyGroupPK=null;
			ReportVO report=UFOCacheManager.getSingleton().getReportCache().getByPK(strRepPK);
			if (report!=null)
				strKeyGroupPK=report.getPk_key_comb();
			else if (measurePubDataVO!=null)
				strKeyGroupPK=measurePubDataVO.getKType();

			if (strKeyGroupPK!=null){
				KeyGroupVO keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK);
				if (keyGroup!=null){
					setKeys(keyGroup.getKeys());
				}
			}
		}
		// should note .note by ljhua 2005-11-24.��Ҫ���.
		// if(getKeys() != null){
		// loadFuncListInst().registerExtFuncs(new MeasFuncDriver( bOnServer));
		// }
	}

	public UfoCalcEnv(ITableData dataChannel, String strRepPk,
			MeasurePubDataVO measurePubDataVO, Hashtable extEnv, boolean bOnServer, DataSourceVO dataSrcVO) {
		this(dataChannel, strRepPk, null, null, measurePubDataVO, extEnv, bOnServer, dataSrcVO);
	}

	public UfoCalcEnv(String strTaskPK,/* KeyVO[] objKeys, */
			MeasurePubDataVO measurePubDataVO, boolean bOnServer,
			DataSourceVO dataSrcVO) {
		this(null, null, strTaskPK, null, measurePubDataVO, null,
				bOnServer, dataSrcVO);
	}

	/**
	 * ����env���ݣ�����������ƵĶ�������ݽ��ᱻ�ı䡣 �������ڣ�(2003-6-16 11:16:56)
	 *
	 * @return com.ufsoft.iufo.util.parser.ICalcEnv
	 */
	public ICalcEnv cloneEnv() {
		return new UfoCalcEnv(this);
	}

	// /**
	// * ���ظü��㻷�������͡�
	// * �������ڣ�(2003-8-5 9:35:09)
	// * @return int
	// */
	// public static int getEnvType() {
	// return ENVTYPE_UFOCALCENV;
	// }
	/**
	 * �˴����뷽�������� �������ڣ�(2002-9-20 13:31:59)
	 *
	 * @return com.ufsoft.iufo.util.parser.UfoFieldObject[]
	 * @deprecated
	 * @i18n miufo00660=�ؼ��ּ���
	 */
	private static UfoFieldObject[] getFields() {
		UfoFieldObject[] fields = new UfoFieldObject[1];
		UfoFieldObject field = new UfoFieldObject("m_measureEnv", "keywords",
				nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1044")/*@res "�ؼ��ּ���"*/, true, "getMeasureEnv", "setMeasureEnv",
				UfoCalcEnv.class, MeasurePubDataVO.class);
		return fields;
	}

	public int getVer() {
		if(getMeasureEnv() == null){
			return 0;
		}
		return getMeasureEnv().getVer();
	}

	/**
	 * �õ�ָ�����Ƶ�module��ִ�ж��� �������ڣ�(2004-1-8 15:01:06)
	 *
	 * @author�����
	 * @return java.lang.Object
	 * @param strModuleName
	 *            java.lang.String
	 * @deprecated
	 */
	public Object getModuleExecutorByName(String strModuleName) {
		Object objExe = null;
		// if(m_hashModuleExecutor == null){
		// m_hashModuleExecutor = new java.util.Hashtable();
		// }else{
		// objExe = m_hashModuleExecutor.get(strModuleName);
		// }
		// if(objExe == null){
		// UfoBCModuleInfo objMInfo =
		// UfoBCModuleCenter.getModuleInfo(strModuleName);
		// if(objMInfo != null){
		// objExe = objMInfo.getExecutor();
		// m_hashModuleExecutor.put(strModuleName, objExe);
		// }
		// }
		return objExe;
	}

	/**
	 * �˴����뷽�������� �������ڣ�(2003-8-1 14:31:13)
	 *
	 * @return java.lang.String
	 */
	public java.lang.String getRepPK() {
		return m_strRepPK;
	}

	/**
	 * �˴����뷽�������� �������ڣ�(2002-5-15 13:28:14)
	 */
	protected void init() {
		super.init();
		m_oDataSource = null;
		m_measurePubDataVO = null;
	}

	public IUFOTableData getUfoDataChannel() {

		if (getDataChannel() instanceof IUFOTableData) {
			return (IUFOTableData) getDataChannel();
		}
		return null;
	}

	// /**
	// * ����˽��ָ��ĵ�λ������Ϣ��
	// * �������ڣ�(2002-11-19 16:10:49)
	// */
	// public void setPrivateMeasEnv() {
	// if(m_contextVO != null){
	// if(m_contextVO.isPrivate()){
	// setExEnv(EX_UNITFORPRIVATEREP, m_contextVO.getCurUnitId());
	// }
	// }
	// }

	/**
	 * ���ñ���pk,�����ñ�������ؼ��ּ��ϡ� �������ڣ�(2003-8-1 14:31:13)
	 *
	 * @param newRepPK
	 *            java.lang.String
	 */
	public void setRepPK(java.lang.String newRepPK) {
		m_strRepPK = newRepPK;
		if (m_strRepPK == null || getReportCache() == null)
			return;
//		ReportVO objRep = null;
//		getReportCache().get(m_strRepPK)
//		String key = "com.ufsoft.script.spreadsheet mathed :setRepPK reportPk | "+m_strRepPK;
//		objRep = (ReportVO)IufoThreadLocalUtil.getValue(key, new AbstractQueryData() {
//					
//					@Override
//					public Object qqueryData() {
//						return (ReportVO) getReportCache().get(m_strRepPK);
//					}
//				});
		ReportVO objRep = (ReportVO) getReportCache().get(m_strRepPK);
		if (objRep == null) {
			return;
		}
		// ��ñ������
		String strRepCode = objRep.getCode();

		// add by zyjun. 04��8��02 �����ڸ�ʽ����ʱ���������Ĺؼ��������޸ģ��ڸ�ʽ����ǰ�޷��ڹ�ʽ��ʹ�������ӵĹؼ��ֵĴ���.
		if (getUfoDataChannel() != null
				&& strRepCode.equals(getUfoDataChannel().getID()) && getUfoDataChannel() instanceof ReportDataModel && 
				((ReportDataModel)getUfoDataChannel()).getDataModel() != null) {
			// ��ñ�����������йؼ�����Ϣ
			KeyVO[] mainKeyVOs = convertKeyClass(getUfoDataChannel()
					.getExtDatasBySetPk(ITableData.MAINPEPORT,
							IUFOTableData.KEYWORD));
			setKeys(mainKeyVOs);
		} else {
			// ͨ�������ñ����Ӧ�ؼ��ּ��ϣ������á�
			if (getKeyGroupCache() != null) {
				nc.vo.iufo.keydef.KeyGroupVO objKeyGroup = getKeyGroupCache()
						.getByPK(objRep.getPk_key_comb());
				if (objKeyGroup != null) {
					setKeys(objKeyGroup.getKeys());
				}
			}
		}
	}

	// public void setContextVO(ContextVO contextVO){
	// if(contextVO!=null){
	// m_contextVO=(ContextVO)contextVO.clone();
	// setRepPK(m_contextVO.getContextId());
	// }
	// else
	// {
	// m_contextVO=contextVO;
	// }
	//
	// }
	// public ContextVO getContextVO(){
	// return m_contextVO;
	// }
	/**
	 * �˴����뷽�������� �������ڣ�(2003-9-1 14:12:52)
	 *
	 * @return nc.vo.iufo.keydef.KeyVO[]
	 */
	public nc.vo.iufo.keydef.KeyVO[] getKeys() {
		if(m_objKeys == null)
			m_objKeys = new KeyVO[0];
		return m_objKeys;
	}

	/**
	 * �˴����뷽�������� �������ڣ�(2002-5-15 13:31:11)
	 *
	 * @return nc.vo.iufo.input.MeasurePubDataVO
	 */
	public MeasurePubDataVO getMeasureEnv() {
		return m_measurePubDataVO;
	}

//	/**
//	 * �˴����뷽�������� �������ڣ�(2003-8-5 11:07:27)
//	 *
//	 * @return java.lang.String
//	 */
//	public java.lang.String getTaskPK() {
//		return m_strTaskPK;
//	}


	public void setKeys(KeyVO[] newKeys) {
		m_objKeys = newKeys;
	}

	public void setMeasureEnv(MeasurePubDataVO newEnv) {
		m_measurePubDataVO = newEnv;
	}


//	public void setTaskPK1(java.lang.String newTaskPK) {
//		m_strTaskPK = newTaskPK;
//	}

	/**
	 * �˴����뷽�������� �������ڣ�(2003-8-5 11:07:27) ��������pk�������������Ӧ�Ĺؼ��ּ���
	 *
	 * @param newTaskPK
	 *            java.lang.String
	 *
	 */
//	public void setTaskPK(java.lang.String newTaskPK) {
//		m_strTaskPK = newTaskPK;
//		m_objSchemeVO = null;
//		// ͨ�������������Ӧ�ؼ��ּ�����Ϣ
//		if (m_strTaskPK != null) {
//			if (getTaskCache() != null) {
//				nc.vo.iufo.task.TaskVO objTask = getTaskCache()
//						.getTaskVO(m_strTaskPK);
//				if (objTask != null && getKeyGroupCache() != null) {
//					nc.vo.iufo.keydef.KeyGroupVO objKG = getKeyGroupCache()
//							.getByPK(objTask.getPk_keygroup());
//					if (objKG != null) {
//						setKeys(objKG.getKeys());
//						return;
//					}
//				}
//			}
//		}
//
//		setKeys(null);
//	}

	private void initCache(boolean bOnserver) {
		m_bOnServer = bOnserver;

		// �����ݿ����԰汾Ϊ��ʱ��װ�����ݿ��¼�����԰汾
		// if (getDbLang() == null) {
			// if(isClient()==true){
			// SysPropVO[] propVOs=null;
			// try {
			// propVOs = new SysPropBO().getSysPropsByNames(new
			// String[]{ISysProp.DB_LANGCODE});
			// } catch (UFOSrvException e) {
			// // TODO �Զ����� catch ��
			// AppDebug.debug(e);
			// }
			// if(propVOs!=null && propVOs.length>0){
			// m_strDbLang=propVOs[0].getValue();
			// }
			// }else{
			// SysPropVO propVO = null ;// SysPropMng.getSysProp(ISysProp.DB_LANGCODE);
			// if (propVO != null) {
				// m_strDbLang = propVO.getValue();
			// }
			// }
		// }

		/**
		 * ���´���ע�� note by ljhua 2005-11-24.
		 * ��Ϊ��MeasFuncDriver(��DefaultFuncDriver)�м�¼�ؼ������ƺ͵�λ�ṹ���ƣ�
		 * ��isServer���Ա仯��Ӧ�ò��������������Ϣ.
		 */
		// MeasFuncDriver objMeasDriver = (MeasFuncDriver)
		// loadFuncListInst().getExtDriver(MeasFuncDriver.class.getName()) ;
		// if(objMeasDriver != null ){
		// objMeasDriver.reloadCaches();
		// }
	}


	/**
	 * iUFOû�б���������ʹ�õļ��㻷���� �������ڣ�(2004-1-2 8:50:23)
	 *
	 * @author�����
	 * @param newOnServer
	 *            boolean
	 */
	public void setOnServer(boolean newOnServer) {
		if (newOnServer == m_bOnServer)
			return;

		initCache(newOnServer);

	}

	/**
	 * �˴����뷽�������� �������ڣ�(2003-8-14 9:58:38)
	 *
	 * @return boolean
	 */
	public boolean isClient() {
		return !m_bOnServer;
	}

	public void setDataSource(DataSourceVO dataSrcVO) {
		m_oDataSource = dataSrcVO;
	}

	public DataSourceVO getDataSource() {
		return m_oDataSource;
	}

	/**
	 * ��ñ������
	 *
	 * @return
	 */
	public String getReportCode() {
		if (getRepPK() == null || getReportCache() == null)
			return null;

		ReportVO repVO = (ReportVO) (getReportCache().get(getRepPK()));
		if (repVO != null)
			return repVO.getCode();
		return null;
	}

	public static KeyVO[] convertKeyClass(Object[] contextObjs) {
		if (contextObjs == null)
			return null;
		int iLen = contextObjs.length;
		KeyVO[] keyVOs = new KeyVO[iLen];
		for (int i = 0; i < iLen; i++) {
			keyVOs[i] = (KeyVO) contextObjs[i];
		}
		return keyVOs;
	}

	public KeyVO[] getMainKeyVOs() {
		IContextObject[] tempMainKeyVOs = (getUfoDataChannel()
				.getExtDatasBySetPk(ITableData.MAINPEPORT,
						IUFOTableData.KEYWORD));
		KeyVO[] mainKeyVO = convertKeyClass(tempMainKeyVOs);
		return mainKeyVO;
	}

	/**
	 * ����ָ�����򣨶�̬����pk)�����йؼ��ּ���(��������ؼ��ּ���). if strDynAreaPK==null, ֻ��������ؼ�����Ϣ
	 *
	 * @param strDynAreaPK
	 * @return
	 */
	public KeyVO[] getAllKeyVO(String strDynAreaPK) {
		Object[] tempSubKeys = strDynAreaPK == null ? null
				: (KeyVO[]) (getUfoDataChannel().getExtDatasBySetPk(
						strDynAreaPK, IUFOTableData.KEYWORD));
		KeyVO[] subKeyVO = convertKeyClass(tempSubKeys);

		KeyVO[] mainKeyVO = getMainKeyVOs();

		List listAll = new ArrayList();
		if (mainKeyVO != null)
			listAll.addAll(Arrays.asList(mainKeyVO));
		if (subKeyVO != null) {
			if (listAll != null)
				listAll.addAll(Arrays.asList(subKeyVO));
		}
		if (listAll != null && listAll.size() > 0) {
			ArrayList listRets = new ArrayList();
			// ���йؼ��ֵĹ��ˣ�ʱ���εĹؼ��ֲ�������ֶ��
			boolean bExistTimeKey = false;
			int iLen = listAll.size();
			KeyVO tempKeyVO = null;
			for (int i = iLen - 1; i >= 0; i--) {
				tempKeyVO = (KeyVO) listAll.get(i);
				if (tempKeyVO.getType() == KeyVO.TYPE_TIME) {
					if (!bExistTimeKey) {
						bExistTimeKey = true;
						listRets.add(tempKeyVO);
					}
				} else {
					listRets.add(tempKeyVO);
				}
			}
			KeyVO[] returnKeyVOs = new KeyVO[listRets.size()];
			listRets.toArray(returnKeyVOs);
			return returnKeyVOs;
		}
		return null;
	}

	/**
	 * ����ָ�����ƻ��ָ����Ϣ����ֻ������ͨ��Ѱ�ҡ�
	 *
	 * @param strReportCode
	 * @param strMeasureName
	 * @return
	 */

	public MeasureVO getMeasByRepCode(String strReportCode,
			String strMeasureName) {
		if (strReportCode == null || strMeasureName == null)
			return null;

		MeasureVO[] findMvos = null;
		// �������pk���ڵ�ǰ����ͨ������pk
		if (strReportCode.equals(getDataChannel().getID())) {
			// ��õ�ǰ����Ĵ���������ָ�꼯��
			findMvos = (MeasureVO[]) getUfoDataChannel().getAllExtDatas(
					IUFOTableData.MEASURE);
		}
		// ������ָͬ�����Ƶ�ָ��
		MeasureVO mvo = null;
		if (findMvos != null && findMvos.length > 0) {
			int iLen = findMvos.length;
			for (int i = 0; i < iLen; i++) {
				if (findMvos[i].getName().equals(strMeasureName)) {
					mvo = findMvos[i];
					break;
				}
			}
		}
		return mvo;
	}

	/**
	 * ͨ��ָ���������ָ�������Ķ�̬����������������������ָ�꣬�򷵻ؿգ�
	 *
	 * @param strMeasurePK
	 * @return
	 */
	public String getDynPKByMeasurePK(String strMeasurePK) {
		String strDynPK = null;
		if (getUfoDataChannel() != null) {
			strDynPK = getUfoDataChannel().getDatasetID(strMeasurePK,
					IUFOTableData.MEASURE);
			if (strDynPK != null && strDynPK.equals(ITableData.MAINPEPORT))
				strDynPK = null;
		}
		return strDynPK;
	}


	public String getDynPKByStoreCellPos(CellPosition pos) {
		String[] strDynPKs = null;
		String strDynPK = null;
		if (getUfoDataChannel() != null) {
			strDynPKs = getUfoDataChannel().getDatasetID(pos);
			if(strDynPKs != null && strDynPKs.length > 0) {
				strDynPK = strDynPKs[0];
			}
			if (strDynPK != null && strDynPK.equals(ITableData.MAINPEPORT))
				strDynPK = null;
		}
		return strDynPK;
	}

	/**
	 * �������ָ��ֵ
	 *
	 * @param strMeasurePK
	 * @return
	 */

	public UfoVal getMainMeasureValue(String strMeasurePK) {
		UfoVal returnValue = null;
		if (getDataChannel() == null)
			return returnValue;

		returnValue = getUfoDataChannel().getMainDataByMeta(strMeasurePK,
				IUFOTableData.MEASURE);
		if (returnValue == null)
			returnValue = UfoNullVal.getSingleton();
		return returnValue;
	}

	public UfoVal getMainStorecellValue(String pos) {
		UfoVal returnValue = null;
		if (getDataChannel() == null)
			return returnValue;

		returnValue = getUfoDataChannel().getMainDataByMeta(pos, IUFOTableData.STORECELL);
		if (returnValue == null)
			returnValue = UfoNullVal.getSingleton();
		return returnValue;
	}

	/**
	 * �ж�ָ���Ƿ��ڼ��㻷���б���������ϡ�
	 *
	 * @param strMeasurePK
	 * @return
	 */
	public boolean isMainMeasure(String strMeasurePK) {
		boolean bMain = false;
		if (getUfoDataChannel() != null) {
			String strDataSetId = getUfoDataChannel().getDatasetID(
					strMeasurePK, IUFOTableData.MEASURE);
			if (strDataSetId != null
					&& strDataSetId.equals(ITableData.MAINPEPORT)) {
				bMain = true;
			}
		}
		return bMain;
	}

	public boolean isMainStorecell(CellPosition pos) {
		String[] strDynPKs = null;
		String strDynPK = null;
		boolean bMain = false;
		if (getUfoDataChannel() != null) {
			strDynPKs = getUfoDataChannel().getDatasetID(pos);
			if(strDynPKs != null && strDynPKs.length > 0) {
				strDynPK = strDynPKs[0];
			}
			if (strDynPK != null && strDynPK.equals(ITableData.MAINPEPORT)) {
				bMain = true;
			}
		}
		return bMain;
	}

	public MeasureCache getMeasureCache() {
		return UFOCacheManager.getSingleton().getMeasureCache();
	}

	public ReportCache getReportCache() {
		return UFOCacheManager.getSingleton().getReportCache();
	}

//	public UserCache getUserCache() {
//		return (UserCache) getCacheManager()
//		.getCache(IUFOCacheConstants.UserCacheObjName);
//	}
//
//	public UnitCache getUnitCache() {
//		return (UnitCache) getCacheManager()
//		.getCache(BDCacheConstants.UnitCacheObjName);
//	}

	public KeyGroupCache getKeyGroupCache() {
		return UFOCacheManager.getSingleton().getKeyGroupCache();
	}

	public RepFormatModelCache getRepFormatCache() {
		return UFOCacheManager.getSingleton().getRepFormatCache();
	}

//	public TaskCache getTaskCache() {
//		return UFOCacheManager.getSingleton().getTaskCache();
//	}

	public KeywordCache getKeyWordCache() {

		return UFOCacheManager.getSingleton().getKeywordCache();
	}

	public String getDbLang() {
		return m_strDbLang;
	}

//	/**
//	 * ��õ�λ�ṹ��Ϣ
//	 *
//	 * @return
//	 */
//	public UnitPropVO[] getUnitPropVOs() {
//		// ��õ�λ�ṹ��Ϣ
//		UnitPropVO[] unitProps = null;
//		try {
//			if (isClient()) {
//				unitProps = Exprop4UnitPropBizProxy.loadAllInputUnitProp();
//			} else {
//				// ʹ�õ�λ�ṹͨ�û��Զ������Ե��޸�,liulp 2006-03-15
//				// nc.bs.iufo.unit.UnitPropDMO uPropDMO = new
//				// nc.bs.iufo.unit.UnitPropDMO(
//				// nc.vo.iufo.pub.DataManageObjectIufo.IUFO_DATASOURCE);
//				// java.util.Vector vecuPropVO = uPropDMO.loadAllInputProp();
//				java.util.Vector<UnitPropVO> vecuPropVO = UnitPropDMOProxy
//						.loadAllInputProp();
//				if (vecuPropVO != null && vecuPropVO.size() > 0) {
//					unitProps = new UnitPropVO[vecuPropVO.size()];
//					vecuPropVO.toArray(unitProps);
//				}
//			}
//		} catch (Exception e) {
//		}
//		return unitProps;
//	}

	public String getLoginDate() {
		return m_strLoginDate;
	}

	public void setLoginDate(String loginDate) {
		m_strLoginDate = loginDate;
	}

	public String getLoginUnitId() {
		return m_strLoginUnitId;
	}

	public void setLoginUnitId(String loginUnitId) {
		m_strLoginUnitId = loginUnitId;
	}

	public String getLoginUserId() {
		return m_strLoginUserId;
	}

	public void setLoginUserId(String loginUserId) {
		m_strLoginUserId = loginUserId;
	}

	// ����չ����תΪ����
	public MeasureTraceVO[] getMeasureTraceVOs() {
		Object[] objs = (Object[]) this.getExEnv(MEASURE_TRACE_FLAG);
		if (objs == null) {
			return null;
		}
		ArrayList<MeasureTraceVO> list = new ArrayList<MeasureTraceVO>();
		for (Object obj : objs) {
			if(obj == null){
				list.add(null);
			} else if(obj instanceof MeasureTraceVO) {
				list.add((MeasureTraceVO) obj);
			} else {
				MeasureTraceVO[] vos = (MeasureTraceVO[]) obj;
				for (int i = 0; i < vos.length; i++) {
					if (vos[i] != null) {
						list.add(vos[i]);
					}
				}
			}
		}

		return list.toArray(new MeasureTraceVO[list.size()]);

	}

	public void setMeasureTraceVOs(MeasureTraceVO[] measureTraceVOs) {
		this.setExEnv(MEASURE_TRACE_FLAG, measureTraceVOs);
	}

	public boolean isMeasureTrace() {
		return getExEnv(MEASURE_TRACE_FLAG) != null;
	}

//	/**
//	 * �õ���ǰ����ڼ䷽��
//	 * @return
//	 */
//	public AccperiodschemeVO getAccPeriodSchemeVO(){
//		if(m_objSchemeVO == null){
//			String strAccPeriodSchemePK = getAccPeriodSchemePK();
//			m_objSchemeVO = AccPeriodSchemeUtil.getInstance().getPeriodSchemeByPK(strAccPeriodSchemePK);
//		}
//		return m_objSchemeVO;
//	}

	/**
	 * �õ���ǰ����ڼ䷽��PK
	 * @return
	 */
	public String getAccPeriodSchemePK(){
		if (getMeasureEnv() != null) {
			return getMeasureEnv().getAccSchemePK();
		}
		return null;
	}

	public String getAccPeriodPK() {
		return m_strAccPK;
	}

	public void setAccPeriodPK(String accPK) {
		m_strAccPK = accPK;
	}

	/**
	 * �����ݼ����������õ����ݼ�
	 * @param paramCord
	 * @return
	 */
	public DataSet getDataSetByParamCord(String strParamCord) {
		return (strParamCord == null)?null:m_hashParamCord2DataSet.get(strParamCord);
	}

	/**
	 * �������ݼ��������������Ӧ���ݼ�ʵ��
	 * @param paramCord2DataSet
	 */
	public void setParamCord2DataSet(String strParamCord, DataSet dataset) {
		if(strParamCord == null || dataset == null){
			return;
		}
		m_hashParamCord2DataSet.put(strParamCord, dataset);
	}

	/**
	 * ���ʱ��ؼ��ֶ��� add by wangya
	 * @return
	 */
	public KeyVO getTimeKey(){
		KeyVO[] keyVos = getKeys();
		for(KeyVO keyVo : keyVos){
			if(keyVo.isTTimeKeyVO())
				return keyVo;
		}
		return null;
	}

	/**
	 * @param orgs the orgs to set
	 */
	public void setOrgs(String[] orgs) {
		this.orgs = orgs;
	}

	/**
	 * @return the orgs
	 */
	public String[] getOrgs() {
		return orgs;
	}
	/**
	 * @param isCheckOrgVisable the isCheckOrgVisable to set
	 */
	public void setCheckOrgVisable(boolean isCheckOrgVisable) {
		this.isCheckOrgVisable = isCheckOrgVisable;
	}
	/**
	 * @return the isCheckOrgVisable
	 */
	public boolean isCheckOrgVisable() {
		return isCheckOrgVisable;
	}

	public String getTaskPk() {
		return taskPk;
	}

	public void setTaskPk(String taskPk) {
		this.taskPk = taskPk;
	}

	public void setAuthValidator(RepDataAuthValidator authValidator) {
		this.authValidator = authValidator;
	}

	public RepDataAuthValidator getAuthValidator() {
		return authValidator;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getDbType() {
		return dbType;
	}

	public Map<String, Hashtable<String, IStoreCell>> getStoreCellMap() {
		return storeCellMap;
	}
	
	public void clearStoreCellMap() {
		storeCellMap.clear();
	}
	
}
