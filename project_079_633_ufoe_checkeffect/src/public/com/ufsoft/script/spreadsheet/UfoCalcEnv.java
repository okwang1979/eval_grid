package com.ufsoft.script.spreadsheet;

/**
 *@update
 *2004-01-08增加m_hashModuleExecutor用于缓存批命令执行对象,getModuleExecutorByName方法用于得到执行对象
 *@end
 *@update
 *2004-01-02增加计算环境类型信息
 *@end
 * @update
 * 修改设置当前报表，使同时设置私有指标单位信息
 * @end
 * @update
 * 修改了getFields方法
 * @end
 * iufo报表计算环境类。包含计算公式所需要的环境参数。
 * 创建日期：(2002-5-13 10:19:23)
 * @author：Administrator
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
	 * 计算是否在服务器端，true是
	 */
	private boolean m_bOnServer = false;

	/**
	 * 报表pk
	 */
	private String m_strRepPK;

//	/**
//	 * 用于在解析的时候纪录当前分析的任务
//	 */
//	private String m_strTaskPK;
	//tianchuan ++ 当前任务PK
	private String taskPk=null;
	
	/**
	 * 会计期间方案PK
	 */
	private String m_strAccPK;

	/**
	 * 用于在解析的时候纪录当前会计期间方案
	 */
	private AccperiodschemeVO m_objSchemeVO = null;

	/**
	 * 数据集参数条件及其对应数据集，参数条件设置原则：
	 * 1、对于相同参数条件的数据集函数使用同一个数据集；
	 * 2、未定义参数的数据集函数多次使用时使用同一个实例；
	 */
	private Hashtable<String, DataSet> m_hashParamCord2DataSet = new Hashtable<String, DataSet>();

	/**
	 * 任务或报表对应的关键字信息集合
	 */
	private KeyVO[] m_objKeys;

	/**
	 * 数据源信息 此属性现只在类 UfoCalcEnvContext：：getCurDataSource中使用.
	 */
	private DataSourceVO m_oDataSource;

	/**
	 * 登陆用户编码
	 */
	private String m_strLoginUserId;

	/**
	 * 登陆单位pk
	 */
	private String m_strLoginUnitId;
	/**
	 * 登陆时间
	 *
	 */
	private String m_strLoginDate;

	/**
	 * 指标数据环境
	 */
	private MeasurePubDataVO m_measurePubDataVO;

	private String m_strDbLang = null;

	// liuchun+ at 20110519当前组织可以参照哪些组织中的报表
	private String[] orgs = null;
	// 是否校验当前组织对报表的可见范围，主要用于导入公式的场景中避免下级单位引用上级单位报表的指标
	private boolean isCheckOrgVisable = false;
	
	// 业务数据取数权限校验
	private RepDataAuthValidator authValidator = null;
	
	// 数据库类型，存储在计算环境中，避免每次都取
	private String dbType = null;
	
	// private ContextVO m_contextVO; //当前表
	// /**
	// * 批命令执行对象缓存，key=modulename,value=UfoBCModuleInfo.getExecutor方法得到的对象
	// */
	// private java.util.Hashtable m_hashModuleExecutor;
	
	private Map<String, Hashtable<String, IStoreCell>> storeCellMap = new Hashtable<String, Hashtable<String, IStoreCell>>();

	/**
	 * UfoCalcEnv 构造子注解。
	 */
	public UfoCalcEnv(boolean bOnserver) {
		super();
		init();
		initCache(bOnserver);
	}



	/**
	 * UfoCalcEnv 构造子注解。
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

			//@edit by ll at 上午10:12:20,确保数据集缓存是同一个实例
			m_hashParamCord2DataSet = env.m_hashParamCord2DataSet;
			
			//tianchuan ++
			setTaskPk(env.getTaskPk());
			
			// m_measurePubDataVO = env.getMeasureEnv();
			// m_strTaskPK = env.getTaskPK();
			// m_objKeys = env.getKeys();
			// m_bOnServer = env.isClient();
			// m_bRepModel = env.isRepModel();
			// m_oDataSource = env.getDataSource();
			// //zyjun2004-06-18修改，避免每次生成MeasFuncDriver，从而每次初始化MeasFuncDriver中的单位结构缓存
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
	 *            数据通道
	 * @param strRepPK
	 *            报表pk
	 * @param strTaskPK
	 *            任务pk
	 * @param objKeys
	 *            关键字信息集合
	 * @param measurePubDataVO
	 *            指标数据信息
	 * @param extEnv
	 *            扩展环境
	 * @param bRepModel
	 *            报表是否模板
	 * @param bOnServer
	 *            计算是否在服务器端
	 * @param dataSrcVO
	 *            计算数据源
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
		// should note .note by ljhua 2005-11-24.需要检查.
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
	 * 复制env内容，这个经过复制的对象的内容将会被改变。 创建日期：(2003-6-16 11:16:56)
	 *
	 * @return com.ufsoft.iufo.util.parser.ICalcEnv
	 */
	public ICalcEnv cloneEnv() {
		return new UfoCalcEnv(this);
	}

	// /**
	// * 返回该计算环境的类型。
	// * 创建日期：(2003-8-5 9:35:09)
	// * @return int
	// */
	// public static int getEnvType() {
	// return ENVTYPE_UFOCALCENV;
	// }
	/**
	 * 此处插入方法描述。 创建日期：(2002-9-20 13:31:59)
	 *
	 * @return com.ufsoft.iufo.util.parser.UfoFieldObject[]
	 * @deprecated
	 * @i18n miufo00660=关键字集合
	 */
	private static UfoFieldObject[] getFields() {
		UfoFieldObject[] fields = new UfoFieldObject[1];
		UfoFieldObject field = new UfoFieldObject("m_measureEnv", "keywords",
				nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1044")/*@res "关键字集合"*/, true, "getMeasureEnv", "setMeasureEnv",
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
	 * 得到指定名称的module的执行对象。 创建日期：(2004-1-8 15:01:06)
	 *
	 * @author：杨婕
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
	 * 此处插入方法描述。 创建日期：(2003-8-1 14:31:13)
	 *
	 * @return java.lang.String
	 */
	public java.lang.String getRepPK() {
		return m_strRepPK;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2002-5-15 13:28:14)
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
	// * 设置私有指标的单位编码信息。
	// * 创建日期：(2002-11-19 16:10:49)
	// */
	// public void setPrivateMeasEnv() {
	// if(m_contextVO != null){
	// if(m_contextVO.isPrivate()){
	// setExEnv(EX_UNITFORPRIVATEREP, m_contextVO.getCurUnitId());
	// }
	// }
	// }

	/**
	 * 设置报表pk,并设置报表主表关键字集合。 创建日期：(2003-8-1 14:31:13)
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
		// 获得报表编码
		String strRepCode = objRep.getCode();

		// add by zyjun. 04－8－02 改正在格式定义时，如果报表的关键字作了修改，在格式保存前无法在公式中使用新增加的关键字的错误.
		if (getUfoDataChannel() != null
				&& strRepCode.equals(getUfoDataChannel().getID()) && getUfoDataChannel() instanceof ReportDataModel && 
				((ReportDataModel)getUfoDataChannel()).getDataModel() != null) {
			// 获得报表主表的所有关键字信息
			KeyVO[] mainKeyVOs = convertKeyClass(getUfoDataChannel()
					.getExtDatasBySetPk(ITableData.MAINPEPORT,
							IUFOTableData.KEYWORD));
			setKeys(mainKeyVOs);
		} else {
			// 通过缓存获得报表对应关键字集合，并设置。
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
	 * 此处插入方法描述。 创建日期：(2003-9-1 14:12:52)
	 *
	 * @return nc.vo.iufo.keydef.KeyVO[]
	 */
	public nc.vo.iufo.keydef.KeyVO[] getKeys() {
		if(m_objKeys == null)
			m_objKeys = new KeyVO[0];
		return m_objKeys;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2002-5-15 13:31:11)
	 *
	 * @return nc.vo.iufo.input.MeasurePubDataVO
	 */
	public MeasurePubDataVO getMeasureEnv() {
		return m_measurePubDataVO;
	}

//	/**
//	 * 此处插入方法描述。 创建日期：(2003-8-5 11:07:27)
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
	 * 此处插入方法描述。 创建日期：(2003-8-5 11:07:27) 设置任务pk，并设置任务对应的关键字集合
	 *
	 * @param newTaskPK
	 *            java.lang.String
	 *
	 */
//	public void setTaskPK(java.lang.String newTaskPK) {
//		m_strTaskPK = newTaskPK;
//		m_objSchemeVO = null;
//		// 通过缓存获得任务对应关键字集合信息
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

		// 当数据库语言版本为空时，装载数据库记录的语言版本
		// if (getDbLang() == null) {
			// if(isClient()==true){
			// SysPropVO[] propVOs=null;
			// try {
			// propVOs = new SysPropBO().getSysPropsByNames(new
			// String[]{ISysProp.DB_LANGCODE});
			// } catch (UFOSrvException e) {
			// // TODO 自动生成 catch 块
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
		 * 以下代码注释 note by ljhua 2005-11-24.
		 * 因为在MeasFuncDriver(即DefaultFuncDriver)中记录关键字名称和单位结构名称，
		 * 当isServer属性变化是应该不用重新载入此信息.
		 */
		// MeasFuncDriver objMeasDriver = (MeasFuncDriver)
		// loadFuncListInst().getExtDriver(MeasFuncDriver.class.getName()) ;
		// if(objMeasDriver != null ){
		// objMeasDriver.reloadCaches();
		// }
	}


	/**
	 * iUFO没有报表的情况下使用的计算环境。 创建日期：(2004-1-2 8:50:23)
	 *
	 * @author：杨婕
	 * @param newOnServer
	 *            boolean
	 */
	public void setOnServer(boolean newOnServer) {
		if (newOnServer == m_bOnServer)
			return;

		initCache(newOnServer);

	}

	/**
	 * 此处插入方法描述。 创建日期：(2003-8-14 9:58:38)
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
	 * 获得报表编码
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
	 * 返回指定区域（动态区域pk)的所有关键字集合(包含主表关键字集合). if strDynAreaPK==null, 只返回主表关键字信息
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
			// 进行关键字的过滤，时间形的关键字不允许出现多次
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
	 * 根据指标名称获得指标信息。且只从数据通道寻找。
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
		// 如果报表pk等于当前数据通道报表pk
		if (strReportCode.equals(getDataChannel().getID())) {
			// 获得当前报表的创建及引用指标集合
			findMvos = (MeasureVO[]) getUfoDataChannel().getAllExtDatas(
					IUFOTableData.MEASURE);
		}
		// 查找相同指标名称的指标
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
	 * 通过指标主键获得指标所属的动态区域主键。（如果是主表的指标，则返回空）
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
	 * 获得主表指标值
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
	 * 判断指标是否在计算环境中报表的主表上。
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
//	 * 获得单位结构信息
//	 *
//	 * @return
//	 */
//	public UnitPropVO[] getUnitPropVOs() {
//		// 获得单位结构信息
//		UnitPropVO[] unitProps = null;
//		try {
//			if (isClient()) {
//				unitProps = Exprop4UnitPropBizProxy.loadAllInputUnitProp();
//			} else {
//				// 使用单位结构通用化自定义属性的修改,liulp 2006-03-15
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

	// 将扩展数据转为数组
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
//	 * 得到当前会计期间方案
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
	 * 得到当前会计期间方案PK
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
	 * 由数据集参数条件得到数据集
	 * @param paramCord
	 * @return
	 */
	public DataSet getDataSetByParamCord(String strParamCord) {
		return (strParamCord == null)?null:m_hashParamCord2DataSet.get(strParamCord);
	}

	/**
	 * 设置数据集参数条件及其对应数据集实例
	 * @param paramCord2DataSet
	 */
	public void setParamCord2DataSet(String strParamCord, DataSet dataset) {
		if(strParamCord == null || dataset == null){
			return;
		}
		m_hashParamCord2DataSet.put(strParamCord, dataset);
	}

	/**
	 * 获得时间关键字对象 add by wangya
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
