package nc.impl.tb.plan;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.mw.sqltrans.TempTable;
import nc.itf.mdm.dim.IDimManager;
import nc.itf.mdm.dim.IDimMemberLoader;
import nc.itf.mdm.permission.INtbPerm;
import nc.itf.mdm.permission.INtbPermConst;
import nc.itf.tb.limit.permission.IDataPermGetter;
import nc.itf.tb.plan.IZiorFrameModelService;
import nc.itf.tb.task.ITaskBusinessService;
import nc.itf.uap.pf.IWorkflowMachine;
import nc.itf.uap.rbac.IRoleManageQuery;
import nc.jdbc.framework.JdbcSession;
import nc.jdbc.framework.PersistenceManager;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.exception.DbException;
import nc.ms.mdm.convertor.IStringConvertor;
import nc.ms.mdm.convertor.StringConvertorFactory;
import nc.ms.mdm.dim.DimBusinessUtil;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.mdm.dim.DimUtil;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ms.mdm.limit.DataPermGetterUtil;
import nc.ms.tb.asynchronous.XmlUtils;
import nc.ms.tb.control.CtlSchemeCTL;
import nc.ms.tb.ext.plan.IZjTaskConst;
import nc.ms.tb.ext.zior.xml.PlanFrameModel;
import nc.ms.tb.ext.zior.xml.PluginSet;
import nc.ms.tb.ext.zior.xml.ViewSet;
import nc.ms.tb.ext.zior.xml.ZiorFrameCtl;
import nc.ms.tb.ext.zior.xml.ZiorOpenNodeModel;
import nc.ms.tb.form.FormServiceGetter;
import nc.ms.tb.form.SheetGroupCtl;
import nc.ms.tb.formula.script.TbbFormulaExecuteLogs;
import nc.ms.tb.node.NodeManagerServiceGetter;
import nc.ms.tb.pub.GlobalParameter;
import nc.ms.tb.pub.IDimPkConst;
import nc.ms.tb.pub.TbUserProfileCtl;
import nc.ms.tb.pubutil.CostTime;
import nc.ms.tb.rule.RuleManager;
import nc.ms.tb.task.TaskExtInfoLoader;
import nc.ms.tb.task.TbTaskCtl;
import nc.ms.tb.task.TbTaskExtCtl;
import nc.ms.tb.task.TbTaskServiceGetter;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ms.tb.task.data.TaskDataModelAction;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.PlanFrameModelManager;
import nc.ms.tb.zior.vo.ZiorActionTypeFactory;
import nc.ms.tb.zior.vo.ZiorTreeNodeObject;
import nc.pubitf.para.SysInitQuery;
import nc.vo.mdm.cube.CubeSnapShot;
import nc.vo.mdm.cube.DimSectionSetTuple;
import nc.vo.mdm.cube.DimSectionTuple;
import nc.vo.mdm.dim.DimHierarchy;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.DimMemberCache;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.IDimLevelPKConst;
import nc.vo.mdm.dim.IDimMemberPkConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbEnv;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.pf.workflow.IPFActionName;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.sm.funcreg.FuncRegisterVO;
import nc.vo.tb.dimdoc.constant.DimDocCodeConstant;
import nc.vo.tb.dimdoc.constant.DimDocConstant;
import nc.vo.tb.dimdocpub.DataAttrtypeEnum;
import nc.vo.tb.form.IFormConst;
import nc.vo.tb.form.MdSheet;
import nc.vo.tb.form.MdSheetGroup;
import nc.vo.tb.form.MdSheetGroupM;
import nc.vo.tb.form.MdWorkbook;
import nc.vo.tb.form.excel.ExOlapInfoSet;
import nc.vo.tb.ntbenum.NTBActionEnum;
import nc.vo.tb.plan.AggregatedMdTaskVO;
import nc.vo.tb.pubutil.IBusiTermConst;
import nc.vo.tb.rule.BusiRuleVO;
import nc.vo.tb.task.ITaskConst;
import nc.vo.tb.task.ITaskStatus;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;
import nc.vo.tb.task.TaskFilter;
import nc.vo.tb.task.TaskLeach;
import nc.vo.tb.task.node.TbNodeVO;
import nc.vo.tb.util.IConst;
import nc.vo.tb.util.SortTool;
import nc.vo.tb.util.SuperVOComparator;
import nc.vo.tb.wf.SheetVO;
import nc.vo.uap.rbac.role.RoleVO;
import nc.vo.wfengine.core.data.DataField;

import com.thoughtworks.xstream.XStream;
/**
 * 报表框架的实现类
 * @author liyingm
 *
 */
public class ZiorFrameModelImpl implements IZiorFrameModelService {
	public static final String TYPE_SINGLE_TASK_VIEW = "single_task_view";
	public static final String TYPE_MUTI_TASK_VIEW = "multi_task_view";
	public static final String BUDGET = "BUDGET";
	public static final String ACTUAL = "ACTUAL";
	public static final String INCOSTOM = "INCOSTOM";
	private final static String paraName_openTask = "TBB028";

	private BaseDAO dao = new BaseDAO();

	private static HashMap<String, String> funXmls = new HashMap<String, String>();
	static {
		funXmls.put("EDIT", "resources\\tbb\\zior\\Plan_18120601.xml");
		funXmls.put("DESKTOP", "resources\\tbb\\zior\\Plan_18120606.xml");
		funXmls.put("APPROVE", "resources\\tbb\\zior\\Plan_18120602.xml");
		funXmls.put("VIEW", "resources\\tbb\\zior\\Plan_18120604.xml");
		funXmls.put("ANALYSIS", "resources\\tbb\\zior\\Plan_18120610.xml");
		funXmls.put("ACTUAL", "resources\\tbb\\zior\\Plan_18121004.xml");
		funXmls.put("PREAPPROVE", "resources\\tbb\\zior\\Plan_18120603.xml");
		funXmls.put("DIRECTADJUST", "resources\\tbb\\zior\\Plan_18120808.xml");
		funXmls.put(ITbPlanActionCode.ADJUSTBILL_NODETYPE, "resources\\tbb\\zior\\Plan_18120800.xml");
		funXmls.put(ITbPlanActionCode.ADJUSTDISPENSE_NODETYPE, "resources\\tbb\\zior\\Plan_18120802.xml");
		funXmls.put(ITbPlanActionCode.VERSION_NODETYPE, "resources\\tbb\\zior\\Plan_18120608.xml");//版本查询类
		funXmls.put(ITbPlanActionCode.ADJUSTPREAPPROVE_NODETYPE, "resources\\tbb\\zior\\Plan_18120809.xml");//调整预审批类
		funXmls.put(ITbPlanActionCode.ADJUSTAPPROVE_NODETYPE, "resources\\tbb\\zior\\Plan_18120810.xml");//调整审批类
		funXmls.put(ITbPlanActionCode.CONSCHEME_NODETYPE, "resources\\tbb\\zior\\Plan_18121000.xml");//控制方案类
	}
public  PlanFrameModel getPlanFrameModelByFuncode(String parma) throws BusinessException{
		
		XStream xs = new XStream();
		PlanFrameModel planFrameModel=null;
		//加在XML文档  
		FileInputStream fis=null;
		try {
			fis = new FileInputStream(getFileByFileConfig(parma));
		} catch (Exception ex) {
			NtbLogger.error(ex);
			throw new BusinessException(ex.getMessage(),ex);
		}
		xs.alias("PlanFrameModel", PlanFrameModel.class);
		xs.alias("PluginSet", PluginSet.class);
		xs.alias("ViewSet", ViewSet.class);
		xs.alias("ViewSet", ViewSet.class);
		
		xs.useAttributeFor(PlanFrameModel.class,"title");
		xs.useAttributeFor(PlanFrameModel.class, "type");
		xs.useAttributeFor(PlanFrameModel.class,"nodeType");
		xs.useAttributeFor(PlanFrameModel.class,"operCode");
		xs.useAttributeFor(ViewSet.class,"id");
		xs.useAttributeFor(PluginSet.class,"ungroup");
		xs.useAttributeFor(PluginSet.class,"plugin");
		Object obj = xs.fromXML(fis);
		
		if(obj!=null)
		 {
		
			planFrameModel = (PlanFrameModel)obj;
			
		 }
		return planFrameModel;
	}
	
	public static File getFileByFileConfig(String filePath)throws BusinessException, URISyntaxException{
		String spath=filePath;
		if(filePath.indexOf("resources")>=0){
			 spath=filePath.trim().substring(10);//去掉 "/resources"
			 spath=spath.replaceAll("\\\\", "/");
		}
		URL uri = PlanFrameModelManager.class.getClassLoader().getResource(spath);
		return new File(uri.toURI());
		
	}

	@Override
	public ZiorOpenNodeModel getZiorOpenNodeModel(FuncRegisterVO frVO,String sysCode,String mdTaskDefName, String fileConfig,String nodepk )
			throws BusinessException {
		ZiorOpenNodeModel  ziorOpenNodeModel=new ZiorOpenNodeModel();
		ziorOpenNodeModel.setSysCode(sysCode);
		ziorOpenNodeModel.setMdTaskDefName(mdTaskDefName);
		String funCode=frVO.getFuncode();
		ziorOpenNodeModel.setFunCode(funCode);
		TbNodeVO tbNodeVO=null;
	    tbNodeVO=getTbNodeVOByPk(nodepk);//发布节点时配置的数据
		ziorOpenNodeModel.setTbNodeVO(tbNodeVO);
		PlanFrameModel planFrameModel=null;
		if(fileConfig!=null&&!fileConfig.isEmpty())
		{
			//默认打开
			planFrameModel=getPlanFrameModelByFuncode(fileConfig);//配置文档的model
		}else{
			//发布节点打开
			//String fileConfigPublic=ZiorNodeType.valueOf(ZiorNodeType.class, tbNodeVO.getFuntype()).getName();
			if(tbNodeVO==null){
				throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0541")/*@res "节点配置的不正确！"*/);
			}
			planFrameModel=getPlanFrameModelByFuncode(funXmls.get(tbNodeVO.getFuntype()));//配置文档的model
		}
		ziorOpenNodeModel.setPlanFrameModel(planFrameModel);
		if(planFrameModel!=null&&TYPE_MUTI_TASK_VIEW
				.equals(planFrameModel.getType())){
			//多任务节点的初始化加载
			initMulTaskUiData(ziorOpenNodeModel);
		}else{
			//单任务节点初始化的加载
			initSingleTaskUiData(ziorOpenNodeModel);
			
		}
		//加载节点可编辑业务方案
		loadNodeCanEditMvtype(ziorOpenNodeModel);
		return ziorOpenNodeModel;
	}
	/**
	 * 加载每个节点的可编辑业务方案
	 * @param ziorOpenNodeModel
	 */
	private void loadNodeCanEditMvtype(ZiorOpenNodeModel ziorOpenNodeModel) {
		List<LevelValue> values=new ArrayList<LevelValue>();
		 if(ziorOpenNodeModel.getTbNodeVO()!=null){
				if(ziorOpenNodeModel.getTbNodeVO().getMvtypeclass()!=null&&!ziorOpenNodeModel.getTbNodeVO().getMvtypeclass().equals(INCOSTOM)){
					
					
					IDimManager idm = DimServiceGetter.getDimManager();
					DimLevel mvtypeLevel = idm.getDimLevelByBusiCode(IDimLevelCodeConst.MVTYPE);
					List<LevelValue> lvs=mvtypeLevel.getDimDef().getAllLevelValues(mvtypeLevel);
					values.addAll(lvs);
//						if(BUDGET.equals(ziorOpenNodeModel.getTbNodeVO().getMvtypeclass())){
//							
//							
//						}else if(ACTUAL.equals(ziorOpenNodeModel.getTbNodeVO().getMvtypeclass())){
//							
//						}
						
				}else if(ziorOpenNodeModel.getTbNodeVO().getMvtypes()!=null&&ziorOpenNodeModel.getTbNodeVO().getMvtypes().size()>0){
					List<String> pks=ziorOpenNodeModel.getTbNodeVO().getMvtypes();
					IDimManager idm = DimServiceGetter.getDimManager();
					DimLevel mvtypeLevel = idm.getDimLevelByBusiCode(IDimLevelCodeConst.MVTYPE);
					for(String pk:pks){
						LevelValue val=	mvtypeLevel.getLevelValueByKey(pk);
						if(val!=null){
							values.add(val);
						}
						
					}
				}else{
					//加载节点默认的业务方案
					
				}
				
			}else{
				//加载节点默认的业务方案
			}
		 ziorOpenNodeModel.setCanEditMvtypes(values);
	}

	/**
	 * 单任务界面数据的加载
	 * @throws BusinessException 
	 */
	
	private void initSingleTaskUiData(ZiorOpenNodeModel  ziorOpenNodeModel) throws BusinessException{
		  //发布节点有配置的走发布节点配置的没有就默认加载
		  MdTaskDef[] taskDefs=getMdTaskDefsByWhere(ziorOpenNodeModel);
		  ziorOpenNodeModel.setUiMdTaskDefs(taskDefs);
		  getSingleTaskMemoryData(ziorOpenNodeModel.getFunCode(),ziorOpenNodeModel.getMdTaskDefName(),ziorOpenNodeModel);
		  MdTask[]    tasks=getMdTasksByWhere(ziorOpenNodeModel);
		  ziorOpenNodeModel.setUiMdTasks(tasks);
		  Map<String, DimMember>  validOrgPkList=getDimMembersByTasks(tasks,ziorOpenNodeModel);
		  ziorOpenNodeModel.setValidOrgPkList(validOrgPkList);
//		  Map<DimLevel, List<Object>> mapAll = new HashMap<DimLevel, List<Object>>();
//		  if(ziorOpenNodeModel.getMap()!=null&&ziorOpenNodeModel.getMap().size()>0){
//			  for(DimLevel dl:)
//		  }
//		  mapAll.putAll(ziorOpenNodeModel.getMap());
		 
		  MdWorkbook[] books=getMdWorkbooks(taskDefs);
		  ziorOpenNodeModel.setBooks(books);
	      //加载发布节点的表单范围
		  if(ziorOpenNodeModel.getTbNodeVO()!=null&&ziorOpenNodeModel.getTbNodeVO().getSheetRangeArray()!=null
					&&ziorOpenNodeModel.getTbNodeVO().getSheetRangeArray().length>0){
			  ziorOpenNodeModel.setPublicnodeLookSheets(ziorOpenNodeModel.getTbNodeVO().getSheetRangeArray());
			  
		  }
	}
	/**
	 * 单任务界面数据的加载
	 * @throws BusinessException 
	 */
	
	private void loadExchangeRateNodeData(ZiorOpenNodeModel  ziorOpenNodeModel) throws BusinessException{
		  //发布节点有配置的走发布节点配置的没有就默认加载
		  MdWorkbook[] books = FormServiceGetter.getFormObjectService().getWorkbooksByWhere(getQueryWorkbookWhere(), false); 
		  MdTask[]    tasks=getMdTasksByWhere(ziorOpenNodeModel);
		  ziorOpenNodeModel.setUiMdTasks(tasks);
		  ziorOpenNodeModel.setBooks(books);
	
	}

	
	/**
	 * 设置多任务打开节点的初始化值
	 * @throws BusinessException 
	 */
	private void initMulTaskUiData(ZiorOpenNodeModel  ziorOpenNodeModel) throws BusinessException{
		loadMulTaskUiMemoryData(ziorOpenNodeModel);
		//把加载用户在当前节点有权限的组织的方法前移, 这样后续业务在校验权限的时候可以不用重复查询  qy 20150813
		String[] funcletOrgPks=getFuncletOrgPks(ziorOpenNodeModel);
		ziorOpenNodeModel.setFuncletOrgPks(funcletOrgPks);
		MdTask[] tasks=getMdTasksMulTaskUI(ziorOpenNodeModel);
		ziorOpenNodeModel.setUiMdTasks(tasks);
		MdTaskDef[] mdTaskDefs=getMulTaskUiMdTaskDef(ziorOpenNodeModel);
		ziorOpenNodeModel.setUiMdTaskDefs(mdTaskDefs);
		MdWorkbook[] books=getMdWorkbooks(mdTaskDefs);
		ziorOpenNodeModel.setBooks(books);
		  //加载发布节点的表单范围
		  if(ziorOpenNodeModel.getTbNodeVO()!=null&&ziorOpenNodeModel.getTbNodeVO().getSheetRangeArray()!=null
					&&ziorOpenNodeModel.getTbNodeVO().getSheetRangeArray().length>0){
			  ziorOpenNodeModel.setPublicnodeLookSheets(ziorOpenNodeModel.getTbNodeVO().getSheetRangeArray());
			  
		  }
   }
private MdWorkbook[] getMdWorkbooks(MdTaskDef[] mdTaskDefs) throws BusinessException{
	MdWorkbook[] books=null;
	StringBuffer sb = new StringBuffer();
	if (mdTaskDefs != null) {
		for (int i = 0; i < mdTaskDefs.length; i++) {
			if (mdTaskDefs[i].getPk_workbook() != null) {
				if (sb.length() > 0)
					sb.append(",");
				else
					sb.append("pk_obj in (");
				sb.append("'").append(mdTaskDefs[i].getPk_workbook())
						.append("'");
			}
		}
		if (sb.length() > 0) {
			sb.append(")");
			/**单任务节点打开效率优化 --- 项目问题移植 --- qy begin ---*/
//			books = FormServiceGetter.getFormObjectService()
//					.getWorkbooksByWhere(sb.toString(), false);
			Collection<MdWorkbook> cols = dao.retrieveByClause(MdWorkbook.class, sb.toString(), new String[] { "pk_obj", "paradims" });
			books = cols == null ? null : cols.toArray(new MdWorkbook[0]);
			/**单任务节点打开效率优化 --- 项目问题移植 --- qy begin ---*/
		}
	}
	return books;
}
/**
 * load多任务节点记忆信息
 * @throws BusinessException 
 */
   private void loadMulTaskUiMemoryData(ZiorOpenNodeModel  ziorOpenNodeModel) throws BusinessException{
		 List<Object> values= getMutiTaskInitData(ziorOpenNodeModel);
		 if(values!=null&&values.size()>0){
			 for(Object obj:values){
				 if(obj instanceof Vector){
					 if(obj!=null&&((Vector)obj).size()>0){
						 Vector v=(Vector)obj;
						 ziorOpenNodeModel.setMulTaskUiMemoryVector(v);
						 ziorOpenNodeModel.setTaskMulFilterSql(v.elementAt(1)==null?null:(String) v.elementAt(1)); 
							if(v.elementAt(0)!=null)
							 {
								MdTaskDef mdTaskDef = TbTaskServiceGetter.getTaskObjectService()
								.getMdTaskDefByPk(((MdTaskDef)v.elementAt(0)).getPrimaryKey());
									if(mdTaskDef!=null&&(mdTaskDef.getIsactive()==null||IConst.TRUE.equals(mdTaskDef.getIsactive())))
								     {
										ziorOpenNodeModel.setSelectTaskDef(mdTaskDef);
								     }
							 }
					 }
						
						}
							
				 }
			 }
		
		 
   }
	private MdTask[] getMdTasksMulTaskUI(ZiorOpenNodeModel ziorOpenNodeModel) throws BusinessException {
		    MdTask[] tasks =null;
			StringBuffer sql = new StringBuffer();
			String pk_user = InvocationInfoProxy.getInstance().getUserId();
			String pk_group = InvocationInfoProxy.getInstance().getGroupId();
			PlanFrameModel planFrameModel=ziorOpenNodeModel.getPlanFrameModel();
			 if(ziorOpenNodeModel.getTbNodeVO()!=null){
					if(ziorOpenNodeModel.getTbNodeVO().getParamvtypeclass()!=null&&!ziorOpenNodeModel.getTbNodeVO().getParamvtypeclass().equals(INCOSTOM)){
							if(BUDGET.equals(ziorOpenNodeModel.getTbNodeVO().getParamvtypeclass())){
								sql.append("versionstatus <> '"+ITaskConst.taskVersionStatus_analysis+"'"+"and versionstatus <> '"+ITaskConst.taskVersionStatus_excute+"'");
								sql.append(" and ").append(planFrameModel.getAfterPublicNodeSqlWhere()==null ? null : planFrameModel.getAfterPublicNodeSqlWhere());
							}else if(ACTUAL.equals(ziorOpenNodeModel.getTbNodeVO().getParamvtypeclass())){
								sql.append("versionstatus = '"+ITaskConst.taskVersionStatus_excute+"'");
								sql.append(" and ").append(planFrameModel.getAfterPublicNodeSqlWhere()==null ? null : planFrameModel.getAfterPublicNodeSqlWhere());
							}
							
					}else if(ziorOpenNodeModel.getTbNodeVO().getParadims()!=null&&ziorOpenNodeModel.getTbNodeVO().getParadims().getLevelValues().size()>0){
						Map<DimLevel, Collection<LevelValue>> cols=ziorOpenNodeModel.getTbNodeVO().getParadims().getLevelValues();
						IDimManager idm = DimServiceGetter.getDimManager();
						DimLevel mvtypeLevel = idm.getDimLevelByBusiCode(IDimLevelCodeConst.MVTYPE);
						if(!cols.keySet().contains(mvtypeLevel)||(cols.keySet().contains(mvtypeLevel)&&cols.get(mvtypeLevel).size()<=0)){
							sql.append(getMulTaskQueryWhere(ziorOpenNodeModel.getTbNodeVO().getTaskdefs(), ziorOpenNodeModel.getTbNodeVO().getParadims(),null));
							sql.append(" and ").append(planFrameModel.getAfterPublicNodeSqlWhere()==null ? null : planFrameModel.getAfterPublicNodeSqlWhere());
						}else{
							sql.append(planFrameModel.getAfterPublicNodeSqlWhere()==null ? null : planFrameModel.getAfterPublicNodeSqlWhere());
						}
					}else{
						sql.append(planFrameModel.getSqlWhere()==null ? null : planFrameModel.getSqlWhere());
					}
					
				}else{
					sql.append(planFrameModel.getSqlWhere()==null ? null : planFrameModel.getSqlWhere());
				}
			
			 String taskFilterWhere = ziorOpenNodeModel.getTaskMulFilterSql();
			 if(taskFilterWhere!=null&&!taskFilterWhere.isEmpty()&&!taskFilterWhere.equals("")&&!taskFilterWhere.equals(" "))
			 {
				 sql.append(" and ").append(taskFilterWhere);
			 }
			String sysCode=ziorOpenNodeModel.getSysCode()==null?IBusiTermConst.SYS_TB:ziorOpenNodeModel.getSysCode();
			sql.append(" and (").append("BUSISYSTEM='"+sysCode+"'").append(")");
			ziorOpenNodeModel.setSqlWhereGlobe(sql.toString());
			//由于之前已经加载过有权限的组织，所以直接作为参数传入，这样可避免重复sql，增加效率
//			tasks= (TbTaskCtl.getLimitMdTasksByWhere(sql.toString(),false,false,ziorOpenNodeModel.getPlanFrameModel().getOperCode(),true,ziorOpenNodeModel.getFunCode()));
			Map<String, Object> param = new HashMap<String, Object>();
			param.put(TbTaskCtl.PARAM_ORG, ziorOpenNodeModel == null ? null : ziorOpenNodeModel.getFuncletOrgPks());
			tasks= TbTaskCtl.getLimitMdTasksByWhere(sql.toString(),false,false,ziorOpenNodeModel.getPlanFrameModel().getOperCode(),true,ziorOpenNodeModel.getFunCode(),param);
			
			if(tasks!=null&&tasks.length>0){
				ArrayList<String> operCodes=new ArrayList<String>();
				operCodes.add(INtbPermConst.RES_OPERCODE_QUERY);
				List<MdTask> list = new ArrayList<MdTask>();
				list=Arrays.asList(tasks);
				Set<MdTask> listTasksAfterPerm=null;
				if(ziorOpenNodeModel.getPlanFrameModel().getNodeType().equals(ITbPlanActionCode.ANALYSE_NODETYPE)){
					listTasksAfterPerm = DataPermGetterUtil.getDataPermGetter().getDatasWithPermission(list, INtbPermConst.TYPE_ANALYSISTASK, operCodes.toArray(new String[0]),pk_user,
							pk_group);
				}else{
					listTasksAfterPerm = DataPermGetterUtil.getDataPermGetter().getDatasWithPermission(list, INtbPermConst.TYPE_BUDGETTASK, operCodes.toArray(new String[0]),pk_user,
							pk_group);
				}
				if(listTasksAfterPerm!=null&&listTasksAfterPerm.size()>0){
					tasks=listTasksAfterPerm.toArray(new MdTask[0]);
				}else{
					tasks=null;
				}
			}
		
	return tasks;
}
	private String getMulTaskQueryWhere(List<String> pk_taskDefs, DimSectionSetTuple paradimSet,String where) throws BusinessException{
		if (paradimSet == null)
			return null;
		StringBuffer sql = new StringBuffer();
		if (pk_taskDefs == null||pk_taskDefs.size()<=0)
			{
			sql.append("1=1");
			}
		else
		{
			sql.append("pk_taskdef in (");
				 for(int i=0;i<pk_taskDefs.size();i++){
					 if(i!=0){
						 sql.append(",");
					 }
					 sql.append("'"+pk_taskDefs.get(i)+"'");
				 }
				 sql.append(")");
		}
		Map<DimLevel, Collection<LevelValue>> map = paradimSet.getLevelValues();
		int idx = 0;
		for (DimLevel dl : map.keySet()) {
			String key = ZiorFrameCtl.dimLevelPk2TaskFields.get(dl.getPrimaryKey());
			if (key == null)
				continue;
			Collection<LevelValue> lvs = paradimSet.getLevelValues(dl);
			if (lvs != null && !lvs.isEmpty()) {
				if (lvs.size() > 800) {
					String tmp= createTempTable("dim", idx);
					insertTmp(tmp, lvs);
					sql.append(" and ").append(key).append(" in (select pk from ").append(tmp).append(")");
				}
				else {
					sql.append(" and ").append(key).append(" in (");
					boolean isFirst = true;
					for (LevelValue lv : lvs) {
						if (isFirst)
							isFirst = false;
						else
							sql.append(",");
						sql.append("'").append(lv.getKey()).append("'");
					}
					sql.append(")");
				}
			}
			idx++;
		}
		if (where != null && where.length() > 0)
			sql.append(" and ").append(where);
	   return sql.toString();
	}
private String createTempTable(String prefix,int tmpIndex)throws DAOException{
	String vtn = null;
	JdbcSession session = null;
	try {
		session = new JdbcSession();
		vtn = new TempTable().createTempTable(session.getConnection(),
				"tb_dm_"+prefix+tmpIndex, "pk varchar(20) not null", "pk");
	} catch (DbException e) {
		Logger.error(e.getMessage(), e);
		throw new DAOException(e.getMessage());
	} catch (SQLException e) {
		Logger.error(e.getMessage(), e);
		throw new DAOException(e.getMessage());
	} finally {
		if (session != null) {
			session.closeAll();
		}
	}
	return vtn;
}
private void insertTmp(String tmp, Collection<LevelValue> lvs)throws DAOException {
	PersistenceManager manager = null;
	try {
		manager = PersistenceManager.getInstance();
		JdbcSession session = manager.getJdbcSession();
		session.setAddTimeStamp(false);
		String sql = "insert into "+tmp+"(pk) values(?)";
		for (LevelValue lv : lvs) {
			if (lv == null)
				continue;
			SQLParameter sp=new SQLParameter();
			sp.addParam(lv.getKey());
			session.addBatch(sql, sp);
		}
		session.executeBatch();
	} catch (DbException dbe) {
		throw new DAOException(dbe);
	} finally {
		if (manager != null)
			manager.release();
	}
}
  private MdTaskDef[] getMulTaskUiMdTaskDef(ZiorOpenNodeModel  ziorOpenNodeModel){
	    String pk_user = InvocationInfoProxy.getInstance().getUserId();
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		MdTaskDef[] taskDefs = null;
		Set<MdTaskDef> listTaskDef=null;
		MdTaskDef[] taskDefsAfterPermission = null;
		try {
			StringBuffer buf=new StringBuffer();
			if(ziorOpenNodeModel.getTbNodeVO()!=null&&ziorOpenNodeModel.getTbNodeVO().getTaskdefs()!=null
					&&ziorOpenNodeModel.getTbNodeVO().getTaskdefs().size()>0){
				 List<String> pks=ziorOpenNodeModel.getTbNodeVO().getTaskdefs();
				 
				 buf.append("pk_obj in (");
				 for(int i=0;i<pks.size();i++){
					 if(i!=0){
						 buf.append(",");
					 }
					 buf.append("'"+pks.get(i)+"'");
				 }
				 buf.append(")");
//				 String sysCode=ziorOpenNodeModel.getSysCode()==null?IBusiTermConst.SYS_TB:ziorOpenNodeModel.getSysCode();
//				 buf.append(" and (").append("BUSISYSTEM='"+sysCode+"'").append(")");
				 taskDefs = TbTaskCtl.getMdTaskDefByWhere(buf.toString(),false);
			}else{
				String sysCode=ziorOpenNodeModel.getSysCode()==null?IBusiTermConst.SYS_TB:ziorOpenNodeModel.getSysCode();
				buf.append("BUSISYSTEM='"+sysCode+"'");
				taskDefs = TbTaskCtl.getMdTaskDefByWhere(buf.toString(), false);
			}
			if (taskDefs != null && taskDefs.length > 0) {
				ArrayList<String> operCodes=new ArrayList<String>();
				operCodes.add(INtbPermConst.RES_OPERCODE_DEFAULT);
				List<MdTaskDef> list = new ArrayList<MdTaskDef>();
				for (int i=0; i<taskDefs.length; i++) {
						list.add(taskDefs[i]);
				}
				listTaskDef = DataPermGetterUtil.getDataPermGetter().getDatasWithPermission(list, INtbPermConst.TYPE_TASKDEF, operCodes.toArray(new String[0]), pk_user,
						pk_group);
				if(listTaskDef!=null&&listTaskDef.size()>0){
					taskDefs=listTaskDef.toArray(new MdTaskDef[0]);
					SortTool.sort(taskDefs, new SuperVOComparator("objname"));
				}else{
					taskDefs=null;
				}
				
				
			}
		} catch (BusinessException e) {
			NtbLogger.printException(e);
		}
		return taskDefs;
  }
	private Map<String, DimMember> getDimMembersByTasks(MdTask[] m_allTasks,ZiorOpenNodeModel  ziorOpenNodeModel) {
		List<DimMember> dms = null;
		MdTaskDef m_taskDef=ziorOpenNodeModel.getSelectTaskDef();
		Map<String, DimMember> validOrgPkList = null;
		String pk_currOrg = null;
		LinkedHashMap<DimMember, MdTask> validOrgDimMemberMap = new LinkedHashMap<DimMember, MdTask>();
		if (m_allTasks != null && m_allTasks.length > 0) {
				// 初始化可见组织map
				validOrgPkList = new HashMap<String, DimMember>();
				String pk_orgStruct = m_taskDef==null ? DimUtil.pk_allOrgDimHierarchy : m_taskDef.getPk_orgstruct();
				if(NtbEnv.isOutLineUI){
					DimHierarchy dh = DimServiceGetter.getDimManager().getDimHierarchyByPK(pk_orgStruct);
					DimMemberCache cache=DimHierarchy.getMemberCache();
					cache.getCacheMap().remove(dh.getPrimaryKey());
					IDimMemberLoader idml = NCLocator.getInstance().lookup(IDimMemberLoader.class);
					try{
						dms = idml.loadAll(dh);
						if (dms != null && !dms.isEmpty()/*&&pkOrgString!=null&&pkOrgString.size()>0*/) {
							for (DimMember orgDm : dms) {
								//当前主体所属的组织是当前用户在当前节点下的当前系统下的组织才显示
								//if(pkOrgString.contains(orgDm.getOrgPk()))
								validOrgPkList.put(orgDm.getLevelValue().getKey().toString(), orgDm);
							}
						}
					}catch(Exception ex){
						
//						new BusinessException ("离线端从数据库加载预算主体成员失败");
						NtbLogger.print(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0475")/*@res "离线端从数据库加载预算主体成员失败"*/);
					}
				}else{
					if(pk_orgStruct!=null&&!pk_orgStruct.equals("")){
					DimHierarchy dh = DimServiceGetter.getDimManager().getDimHierarchyByPK(pk_orgStruct);
					dms = dh.getMemberReader().getMembers();
					//GroupAppTools.filterOrgDimByPkOrg(dh.getMemberReader().getMembers(), 
					//		InvocationInfoProxy.getInstance().getGroupId(), pk_currOrg);
					if (dms != null && !dms.isEmpty()/*&&pkOrgString!=null&&pkOrgString.size()>0*/) {
						for (DimMember orgDm : dms) {
							//当前主体所属的组织是当前用户在当前节点下的当前系统下的组织才显示
							//if(pkOrgString.contains(orgDm.getOrgPk()))
							validOrgPkList.put(orgDm.getLevelValue().getKey().toString(), orgDm);
						}
					}
					}
				}
			// 根据可见组织过滤可见任务
			DimMember dm;
			Map<DimMember, MdTask> tmpMap=new HashMap<DimMember, MdTask> ();
			for (int i=0; i<m_allTasks.length; i++) {
				dm = validOrgPkList.get(m_allTasks[i].getPk_dataent());
				if (dm != null)
					tmpMap.put(dm, m_allTasks[i]);
			}
			if(dms != null && !dms.isEmpty()){
				//按照主体排序 tzj+
				for(DimMember orgDm:dms){
					if(tmpMap.containsKey(orgDm)){
						validOrgDimMemberMap.put(orgDm, tmpMap.get(orgDm));
					}
				}
				ziorOpenNodeModel.setValidOrgDimMemberMap(validOrgDimMemberMap);
			}

			
		}
		return validOrgPkList;
	}
/**
 * 得到打开节点的任务----单任务节点
 * @param ziorOpenNodeModel
 * @return
 */
	private MdTask[] getMdTasksByWhere(ZiorOpenNodeModel  ziorOpenNodeModel) {
		// 从数据库刷新任务列表
		PlanFrameModel planFrameModel=ziorOpenNodeModel.getPlanFrameModel();
		MdTask[] m_allTasks = null;
		if (ziorOpenNodeModel.getSelectTaskDef() != null && ziorOpenNodeModel.getTaskFilter()!= null) {
			Set<MdTask> listTasksAfterPerm=null;
			ArrayList<String> operCodes=new ArrayList<String>();
			operCodes.add(INtbPermConst.RES_OPERCODE_QUERY);
			try {
				if(planFrameModel!=null){
					StringBuffer sb = new StringBuffer();
					sb.append("pk_taskdef='").append(ziorOpenNodeModel.getSelectTaskDef().getPrimaryKey()).append("'");
					String where =null;
					if(ziorOpenNodeModel.getTbNodeVO()!=null){
						if(ziorOpenNodeModel.getTbNodeVO().getParamvtypeclass()!=null&&!ziorOpenNodeModel.getTbNodeVO().getParamvtypeclass().equals(INCOSTOM)){
							where=planFrameModel.getAfterPublicNodeSqlWhere()==null ? null : planFrameModel.getAfterPublicNodeSqlWhere();
						}else if(ziorOpenNodeModel.getTbNodeVO().getParadims()!=null&&ziorOpenNodeModel.getTbNodeVO().getParadims().getLevelValues().size()>0){
							Map<DimLevel, Collection<LevelValue>> cols=ziorOpenNodeModel.getTbNodeVO().getParadims().getLevelValues();
							IDimManager idm = DimServiceGetter.getDimManager();
							DimLevel mvtypeLevel = idm.getDimLevelByBusiCode(IDimLevelCodeConst.MVTYPE);
							if(cols.keySet().contains(mvtypeLevel)&&cols.get(mvtypeLevel).size()>0){
								where=planFrameModel.getAfterPublicNodeSqlWhere()==null ? null : planFrameModel.getAfterPublicNodeSqlWhere();	
							}else{
								where=planFrameModel.getSqlWhere()==null ? null : planFrameModel.getSqlWhere();
							}
						}else{
							where=planFrameModel.getSqlWhere()==null ? null : planFrameModel.getSqlWhere();
						}
						
					}else{
						where=planFrameModel.getSqlWhere()==null ? null : planFrameModel.getSqlWhere();
					}
					
					String sql=getQuerytSql(ziorOpenNodeModel.getTaskFilter()).toString();
//					if(sql==null||sql.isEmpty()){
//						return null;//没有符合条件的任务参数维，那其他条件也不参与查询
//					}
					sb.append(sql);
					if (where != null && where.length() > 0)
						sb.append(" and (").append(where).append(")");
					String sysCode=ziorOpenNodeModel.getSysCode()==null?IBusiTermConst.SYS_TB:ziorOpenNodeModel.getSysCode();
					//汇率维护节点暂时不过滤业务系统
					if(!ITbPlanActionCode.RATEMAINTENANCE_NODETYPE.equals(ziorOpenNodeModel.getPlanFrameModel().getNodeType())){
						sb.append(" and (").append("BUSISYSTEM='"+sysCode+"'").append(")");
					}
					ziorOpenNodeModel.setSqlWhereGlobe(sb.toString());
					
					MdTask[] rtn=null;
						if(NtbEnv.isOutLineUI){
							rtn = TbTaskServiceGetter.getTaskObjectService().getDbMdTasksByFilter(ziorOpenNodeModel.getSelectTaskDef().getPrimaryKey(), ziorOpenNodeModel.getTaskFilter(),  planFrameModel.getSqlWhere(),true);
							for(MdTask task:rtn){
								task.setSheetlist(null);  //离线端不需要控制表单范围
							}
							m_allTasks = rtn;
						}else{
							List<MdTask> list = new ArrayList<MdTask>();
							if(ziorOpenNodeModel.getPlanFrameModel().getNodeType().equals(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE)){
								//汇率模型节点任务的加载
								rtn=TbTaskCtl.getLimitMdTasksByWhere(sb.toString(),false,false,"tbQuery",false,ziorOpenNodeModel.getFunCode());
								m_allTasks = rtn;
							}else{
								rtn=TbTaskCtl.getLimitMdTasksByWhere(sb.toString(),false,false,"tbQuery",true,ziorOpenNodeModel.getFunCode());
								   if(rtn!=null)
								    {
								    	list=Arrays.asList(rtn);
									    listTasksAfterPerm = DataPermGetterUtil.getDataPermGetter().getDatasWithPermission(list, INtbPermConst.TYPE_BUDGETTASK, operCodes.toArray(new String[0]), InvocationInfoProxy.getInstance().getUserId(),
									    		InvocationInfoProxy.getInstance().getGroupId());
								    }
								   m_allTasks = listTasksAfterPerm==null?null:listTasksAfterPerm.toArray(new MdTask[0]);
							}
						}
				}
			} catch (BusinessException e) {
				NtbLogger.printException(e);
			}
		}
		return  m_allTasks;
	}
/**
 * 得到单任务界面的任务模板，发布节点配置的走发布节点的，没发布的走默认节点打开的逻辑
 * @param ziorOpenNodeModel
 * @return
 * @throws BusinessException
 */
	private MdTaskDef[] getMdTaskDefsByWhere(ZiorOpenNodeModel  ziorOpenNodeModel) throws BusinessException {
		String pk_user = InvocationInfoProxy.getInstance().getUserId();
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		MdTaskDef[] books=null;
		if(ziorOpenNodeModel.getTbNodeVO()!=null&&ziorOpenNodeModel.getTbNodeVO().getTaskdefs()!=null
				&&ziorOpenNodeModel.getTbNodeVO().getTaskdefs().size()>0){
			//发布节点时选择的情况
			 List<String> pks=ziorOpenNodeModel.getTbNodeVO().getTaskdefs();
			 StringBuffer buf=new StringBuffer();
			 buf.append("pk_obj in (");
			 for(int i=0;i<pks.size();i++){
				 if(i!=0){
					 buf.append(",");
				 }
				 buf.append("'"+pks.get(i)+"'");
			 }
			 buf.append(")");
			 books = TbTaskCtl.getMdTaskDefByWhere(buf.toString(),false);
		}else{
			String wheresql=null;
			if(ziorOpenNodeModel.getPlanFrameModel().getNodeType().equals(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE)){
				//汇率模型节点任务模板的加载
				wheresql=getExchangeRatWhere();
			}else{
				String sysCode=ziorOpenNodeModel.getSysCode()==null?IBusiTermConst.SYS_TB:ziorOpenNodeModel.getSysCode();
				wheresql=getQueryWhere(sysCode);
			}
			
			books = TbTaskCtl.getMdTaskDefByWhere(wheresql,false);
		}
		
		if(books!=null&&books.length>0&&!(ziorOpenNodeModel.getPlanFrameModel().getNodeType().equals(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE))){
			ArrayList<String> operCodes=new ArrayList<String>();
			operCodes.add(INtbPermConst.RES_OPERCODE_DEFAULT);
			Set<MdTaskDef> set= DataPermGetterUtil.getDataPermGetter().getDatasWithPermission(Arrays.asList(books), INtbPermConst.TYPE_TASKDEF, operCodes.toArray(new String[0]),pk_user, pk_group); 
			 if(set!=null&&set.size()>0){
				 books=set.toArray(new MdTaskDef[0]);
				 SortTool.sort(books, new SuperVOComparator("objname"));
				return books;
			 }
			
		}
	    return books;
	}
    private String getExchangeRatWhere(){
    		 return " busitype='"+IFormConst.workbook_busitype_exchangerate+"' order by ts"; 

   }
	private String getQueryWhere(String sysCode) {
			if(sysCode!=null){
				return "tasktype in('"+ITaskConst.strTaskType_dir+"'"+",'"+ITaskConst.strTaskType_plan+"'"+")"+" and busisystem='"+sysCode+"' order by ts";
//				return " tasktype='"+ITaskConst.strTaskType_dir+"' or tasktype='"+ITaskConst.strTaskType_plan+
//				"' and busisystem='"+sysCode+"' order by ts";
			}
			return " tasktype='"+ITaskConst.strTaskType_dir+"' or tasktype='"+ITaskConst.strTaskType_plan+
				"' or tasktype='"+ITaskConst.strTaskType_ana+"' order by ts";
	}
/**
 * 获得一个任务模板所有可操作的参数维，发布节点+权限
 * @param taskDef
 * @param tasks
 * @param ziorOpenNodeModel
 * @return
 * @throws BusinessException
 */
	private Map<DimLevel, List<LevelValue>> getTaskParadimsMapDefault(MdTaskDef taskDef,MdTask[] tasks,ZiorOpenNodeModel  ziorOpenNodeModel) throws BusinessException {
		String pk_user = InvocationInfoProxy.getInstance().getUserId();
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		DimSectionSetTuple tuple=new DimSectionSetTuple();
		Map<DimLevel, List<LevelValue>> map = new HashMap<DimLevel, List<LevelValue>>();
		map.clear();
		List<DimLevel> dimLevels = new ArrayList<DimLevel>();//存发布节点配置的参数维
		List<DimLevel> taskdefDimLevels = new ArrayList<DimLevel>();//存任务模板配置的参数维，发布时配置的任务参数 任务模板上不一定有那些维度
		ArrayList<String> unused = new ArrayList<String>();
		//unused.add(IDimLevelPKConst.ENT);
		//默认的参数维
		DimLevel[] dls = taskDef.getParaDims();
		if(dls == null){
			TbTaskCtl.loadDetail(new MdTaskDef[]{taskDef});
		}
		dls = taskDef.getParaDims();
		for(DimLevel ll:dls){
			taskdefDimLevels.add(ll);
		}
		boolean  isDefault=true;
		List<LevelValue> levelValues = new ArrayList<LevelValue>();//缓记一下所有参数维相应的取值：默认+发布
		Collection<LevelValue> quertLvs=new ArrayList<LevelValue>();//缓存发布节点的参数维，供查询已启动的任务用
		//发布节点选的业务方案为实际数或者预算数，则业务方案为实际数或者预算数的全部
		if(ziorOpenNodeModel.getTbNodeVO()!=null){
			if(ziorOpenNodeModel.getTbNodeVO().getParamvtypeclass()!=null&&(ziorOpenNodeModel.getTbNodeVO().getParamvtypeclass().equals(ACTUAL)
					||ziorOpenNodeModel.getTbNodeVO().getParamvtypeclass().equals(BUDGET))){
				IDimManager idm = DimServiceGetter.getDimManager();
				DimLevel mvtypeLevel = idm.getDimLevelByBusiCode(IDimLevelCodeConst.MVTYPE);
				List<LevelValue> lvs=mvtypeLevel.getDimDef().getAllLevelValues(mvtypeLevel);
				if(taskdefDimLevels.contains(mvtypeLevel)&&lvs!=null&&lvs.size()>0){
					if(BUDGET.equals(ziorOpenNodeModel.getTbNodeVO().getParamvtypeclass())){
							for(LevelValue lv:lvs){
								if(DataAttrtypeEnum.BUDGET.value().toString().equals(lv.getPropValue("dataattrtype"))){
									levelValues.add(lv);
									quertLvs.add(lv);
								}
							}
							tuple.setLevelValues(mvtypeLevel, quertLvs);
					}else if(ACTUAL.equals(ziorOpenNodeModel.getTbNodeVO().getParamvtypeclass())){
						for(LevelValue lv:lvs){
							if(DataAttrtypeEnum.ACTUAL.value().toString().equals(lv.getPropValue("dataattrtype"))){
								levelValues.add(lv);
								quertLvs.add(lv);
							}
						}
						tuple.setLevelValues(mvtypeLevel, quertLvs);
					}
					dimLevels.add(mvtypeLevel);
					isDefault=false;
				}
				
				
				
			}else if(ziorOpenNodeModel.getTbNodeVO().getParadims()!=null&&ziorOpenNodeModel.getTbNodeVO().getParadims().getLevelValues().size()>0){
				//发布节点的参数维
				Map<DimLevel, Collection<LevelValue>> paradimsMap = ziorOpenNodeModel.getTbNodeVO().getParadims().getLevelValues();
				for(DimLevel dim:paradimsMap.keySet()){
					if(unused.contains(dim.getPrimaryKey())){
						continue;
					}
					if(!taskdefDimLevels.contains(dim)){
						continue;
					}
					dimLevels.add(dim);
					Collection<LevelValue> lvs=paradimsMap.get(dim);
					levelValues.addAll(lvs);
					tuple.setLevelValues(dim, lvs);
				}
				isDefault=false;
			}
		}
		
		for (DimLevel dimLevel : dls) {
			if(unused.contains(dimLevel.getPrimaryKey())){
				continue;
			}
			if(dimLevels.contains(dimLevel)){
				continue;
			}
			dimLevels.add(dimLevel);
			List<LevelValue> lvs = dimLevel.getDimDef()
					.getAllLevelValues(dimLevel);
			levelValues.addAll(lvs);
		}

		IDataPermGetter permGetter = DataPermGetterUtil
				.getDataPermGetter();

		levelValues = permGetter.getDatasWithPermission(
				levelValues,pk_user,pk_group);
		for (LevelValue lv : levelValues) {
			if (map.containsKey(lv.getDimLevel())) {
				map.get(lv.getDimLevel()).add(lv);
			} else {
				ArrayList<LevelValue> list = new ArrayList<LevelValue>();
				list.add(lv);
				map.put(lv.getDimLevel(), list);
			}
		}
		//默认每个节点加载的任务参数维
		if(isDefault)
		{
			filterTaskParadimByNodeType(map,ziorOpenNodeModel,isDefault);
		}
		//过滤掉没启动任务的参数维
		List<String> pk_taskDefs=new ArrayList<String>();
		pk_taskDefs.add(ziorOpenNodeModel.getSelectTaskDef().getPk_obj());
		String sql=getMulTaskQueryWhere(pk_taskDefs, tuple, null);
		if(sql!=null&&!sql.isEmpty()&&(tasks== null||tasks.length ==0)){
			MdTask[] taks=TbTaskCtl.getMdTasksByWhere(sql.toString(), false);
			filterTaskParadimByStartTasks(map,ziorOpenNodeModel,taskDef,taks,true);
		}
		else{
			filterTaskParadimByStartTasks(map,ziorOpenNodeModel,taskDef,tasks,true);
		}
		return map;
	}

	private TaskFilter getTaskFilter(MdTaskDef taskDef,TaskFilter filter,Map<DimLevel, List<LevelValue>> map,ZiorOpenNodeModel  ziorOpenNodeModel) throws BusinessException {
 		 Map<String , String> defaultParamDimPksMap=ZiorActionTypeFactory.getDefaultParamDimPksMap();
		 DimLevel[] dls=null;
		 List<LevelValue> paraDimList = new ArrayList<LevelValue>();
		 TaskFilter returnFilter=new TaskFilter();
			if(ziorOpenNodeModel.getParadimOrderMap()!=null&&ziorOpenNodeModel.getParadimOrderMap().get(ITbPlanActionCode.UICOMBOX)!=null
					&&ziorOpenNodeModel.getParadimOrderMap().get(ITbPlanActionCode.UICOMBOX).size()>0){
				dls=ziorOpenNodeModel.getParadimOrderMap().get(ITbPlanActionCode.UICOMBOX).values().toArray(new DimLevel[0]);
			}else  if(ziorOpenNodeModel.getParadimOrderMap()!=null&&ziorOpenNodeModel.getParadimOrderMap().get(ITbPlanActionCode.UITREE)!=null
					&&ziorOpenNodeModel.getParadimOrderMap().get(ITbPlanActionCode.UITREE).size()==map.keySet().size()){
				dls=null;
				return  returnFilter;
				
			}else{
				dls=map.keySet().toArray(new DimLevel[0]);
			}
		 if(filter==null){
			 return getInitFilter(dls,map,ziorOpenNodeModel);
		}else{
			HashMap<String, Object> paraMap=filter.getParaMap();
			if(paraMap.isEmpty())
			 return getInitFilter(dls,map,ziorOpenNodeModel);
		}
		
		
		for(DimLevel dl:dls){
			List<LevelValue> lvs=map.get(dl);
			if(lvs!=null&&lvs.size()>0){
				if(defaultParamDimPksMap.containsKey(dl.getPrimaryKey()))
				{
//					if(filter.getAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey()))!=null&&filter.getAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey())).equals("~")){
//						if(lvs.contains(null)){
//							returnFilter.setAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey()),"~");	
//						}
//						continue;
//					}
					LevelValue value=null;
					if(defaultParamDimPksMap.get(dl.getPrimaryKey()).equals(TaskFilter.key_pk_year)||
							defaultParamDimPksMap.get(dl.getPrimaryKey()).equals(TaskFilter.key_pk_month))
					{
						if(filter.getParaMap().containsKey(defaultParamDimPksMap.get(dl.getPrimaryKey())))
						{
							if(!filter.getAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey())).equals("~"))
							value=dl.getLevelValueByKey(Integer.parseInt((String)filter.getAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey()))));
							if(value!=null&&lvs.contains(value)){
								returnFilter.setAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey()), value.getKey()
										.toString());	
						}else{
							if(lvs.get(0)!=null)
							returnFilter.setAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey()),lvs.get(0).getKey()
									.toString());	
						}
					}else{
						replenishFilter(dl,map,ziorOpenNodeModel,returnFilter,paraDimList);
					}
					}else if(defaultParamDimPksMap.get(dl.getPrimaryKey()).equals(TaskFilter.key_pk_dataent)){

						if(filter.getParaMap().containsKey(defaultParamDimPksMap.get(dl.getPrimaryKey())))
						{
							value=dl.getLevelValueByKey( filter.getAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey())));
							if(lvs.contains(value)){
								returnFilter.setAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey()), value.getKey()
										.toString());	
						}else{
							if(lvs.get(0)!=null)
							returnFilter.setAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey()),lvs.get(0).getKey()
									.toString());	
						}
					}else{
						replenishFilter(dl,map,ziorOpenNodeModel,returnFilter,paraDimList);
					}
					
					}
					else{
						if(defaultParamDimPksMap.get(dl.getPrimaryKey()).equals(TaskFilter.key_pk_aimcurr)&&!ziorOpenNodeModel.getSelectTaskDef().isCurrConvert()
								){
							if(returnFilter.getAttribute(TaskFilter.key_pk_currency)!=null)
							{ returnFilter.setAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey()), returnFilter.getAttribute(TaskFilter.key_pk_currency));
							 continue;
							 }
							
						}
						if(filter.getParaMap().containsKey(defaultParamDimPksMap.get(dl.getPrimaryKey())))
						{
							value=dl.getLevelValueByKey( filter.getAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey())));
							if(lvs.contains(value)){
								returnFilter.setAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey()), value.getKey()
										.toString());	
						}else{
							if(lvs.get(0)!=null)
							returnFilter.setAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey()),lvs.get(0).getKey()
									.toString());	
						}
					}else{
						replenishFilter(dl,map,ziorOpenNodeModel,returnFilter,paraDimList);
					}
					}
				}else{

					if(filter!=null&&filter.getParaMap().containsKey(TaskFilter.key_pk_paradims)){
						IStringConvertor sc = StringConvertorFactory.getConvertor(DimSectionTuple.class);
						DimSectionTuple paraDim=(DimSectionTuple)sc.fromString((String)filter.getAttribute(TaskFilter.key_pk_paradims));
						LevelValue levelValue=paraDim.getLevelValue(dl);
						if(levelValue!=null&&lvs.contains(levelValue)){
							paraDimList.add(levelValue);
				
				}else{
					if(lvs.get(0)!=null)
					paraDimList.add(lvs.get(0));	
				}
				}else{
					replenishFilter(dl,map,ziorOpenNodeModel,returnFilter,paraDimList);
				}
				}
			
			}
//			else{
//				if(defaultParamDimPksMap.containsKey(dl.getPrimaryKey()))
//				{
//					
//					returnFilter.setAttribute(defaultParamDimPksMap.get(dl.getPrimaryKey()),null);	
//				}
//			}
		}
		if (!paraDimList.isEmpty()&&paraDimList.size()>0) {
			DimSectionTuple paraDim = new DimSectionTuple(paraDimList);
			IStringConvertor sc = StringConvertorFactory
					.getConvertor(DimSectionTuple.class);
			returnFilter.setAttribute(TaskFilter.key_pk_paradims,
					sc.convertToString(paraDim));
		}
		
		return  returnFilter;
	}
	private TbNodeVO getTbNodeVOByPk(String pk) throws BusinessException {
		TbNodeVO tbNodeVO=NodeManagerServiceGetter.getNodeManagerService().getTbNode(pk);
		return tbNodeVO;
	}
	/**
	 * 读上次打开节点的记忆值
	 * 
	 * 
	 */
	protected void getSingleTaskMemoryData(String funCode,String taskDefName,ZiorOpenNodeModel  ziorOpenNodeModel) {
		String pk_user = InvocationInfoProxy.getInstance().getUserId();
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		try {
			UFBoolean isOpenTask = SysInitQuery.getParaBoolean(IDimPkConst.PK_GLOBE,
					paraName_openTask);
			TaskLeach taskLeach = (TaskLeach) TbUserProfileCtl.getInstance()
					.getUserTaskFilter(funCode, pk_user+pk_group);
			if (taskLeach != null) {
				ArrayList<Object> values = (ArrayList<Object>) taskLeach
						.getAttributes(funCode);
				MdTaskDef def = null;
				TaskFilter filter = null;
				DimMember m_selectedOrg = null;
				ZiorTreeNodeObject selectedTreeNode = null;
				Map<String, LinkedHashMap<Integer, DimLevel>> paradomOrderMap = null;
				int inputDir = 2;
				int paradimPnlState= 1;
				Map<MdTaskDef, MdTask[]>  defAndTasksmap;
				MdTask[] tasks=null;
				if(GlobalParameter.getInstance().getPara("usertaskdefname", Boolean.class) != null){
					Boolean bl = GlobalParameter.getInstance().getPara("usertaskdefname", Boolean.class);
					if(bl){
						defAndTasksmap=getMdTaskDefWithDetailAndMdTasks(" objname='" +taskDefName +"'");
						if(defAndTasksmap!=null&&defAndTasksmap.size()>0){
						for(MdTaskDef df:defAndTasksmap.keySet()){
							def=df;
							tasks=defAndTasksmap.get(def);
							break;
						 }
					}
					}
				}
				if (values != null && values.size() > 0) {
					for (Object obj : values) {
						if (obj instanceof MdTaskDef) {
							if(def == null){
								
								MdTaskDef updateDef=TbTaskCtl.getMdTaskDefByPk(((MdTaskDef) obj).getPk_obj(), false);
								def =updateDef;
								defAndTasksmap=getMdTaskDefWithDetailAndMdTasks(" pk_obj='" +((MdTaskDef) obj).getPk_obj() +"'");
								if(defAndTasksmap!=null&&defAndTasksmap.size()>0){
									for(MdTaskDef df:defAndTasksmap.keySet()){
										def=df;
										tasks=defAndTasksmap.get(def);
										break;
									 }
								}
								
							}
							
						} else if (obj instanceof TaskFilter) {
							filter = (TaskFilter) obj;
						} else if (obj instanceof DimMember) {
							m_selectedOrg = (DimMember) obj;
						}else if (obj instanceof Integer) {
							inputDir = (Integer) obj;
							ziorOpenNodeModel.setInputDir(inputDir);
						}else if(obj instanceof ZiorTreeNodeObject){
							if (isOpenTask.booleanValue()) {
								selectedTreeNode=(ZiorTreeNodeObject)obj;
								ziorOpenNodeModel.setSelectedTreeNode(selectedTreeNode);
							}
						}else if(obj instanceof Map){
							paradomOrderMap=(Map<String, LinkedHashMap<Integer, DimLevel>>) ((Map) obj).get(ITbPlanActionCode.PARADIMVALUES);
							if(paradomOrderMap==null){
								paradomOrderMap= new HashMap<String, LinkedHashMap<Integer, DimLevel>>();
								LinkedHashMap<Integer, DimLevel> dimMap=new LinkedHashMap<Integer, DimLevel>();
								IDimManager dm=DimServiceGetter.getDimManager();
								DimLevel dimlevel=null;
								if(ziorOpenNodeModel.getPlanFrameModel().getNodeType().equals(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE)){
									//汇率模型节点树模型参数维的加载
									 dimlevel = dm.getDimLevelByPK(IDimLevelPKConst.YEAR);
								}else{
									 dimlevel = dm.getDimLevelByPK(IDimLevelPKConst.ENT);
								}
								
								dimMap.put(1, dimlevel);
								paradomOrderMap.put(ITbPlanActionCode.UITREE, dimMap);
							}else{
								if(paradomOrderMap.get(ITbPlanActionCode.UITREE)==null||paradomOrderMap.get(ITbPlanActionCode.UITREE).size()<=0){
									LinkedHashMap<Integer, DimLevel> dimMap=new LinkedHashMap<Integer, DimLevel>();
									IDimManager dm=DimServiceGetter.getDimManager();
									DimLevel dimlevel=null;
									if(ziorOpenNodeModel.getPlanFrameModel().getNodeType().equals(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE)){
										//汇率模型节点树模型参数维的加载
										 dimlevel = dm.getDimLevelByPK(IDimLevelPKConst.YEAR);
									}else{
										 dimlevel = dm.getDimLevelByPK(IDimLevelPKConst.ENT);
									}
									dimMap.put(1, dimlevel);
									paradomOrderMap.put(ITbPlanActionCode.UITREE, dimMap);
								}
							}
							
							ziorOpenNodeModel.setParadimOrderMap(paradomOrderMap);
							if(((Map) obj).get(ITbPlanActionCode.PARADIMPNLSTATE)==null){
								ziorOpenNodeModel.setParadimPnlState(paradimPnlState);
							}else{
								ziorOpenNodeModel.setParadimPnlState((Integer) ((Map) obj).get(ITbPlanActionCode.PARADIMPNLSTATE));
							}
							//记忆用户选择的表单的加载，
							if(((Map) obj).get(ITbPlanActionCode.SELECTSHEET)!=null){
								ziorOpenNodeModel.setSelectSheetMap((Map)((Map) obj).get(ITbPlanActionCode.SELECTSHEET));
							}
//							//任务行宽列高的记忆值记忆
//							if(((Map) obj).get(ITbPlanActionCode.code_SaveRowColSize)!=null){
//								ziorOpenNodeModel.setSaveSizeMap((Map)((Map) obj).get(ITbPlanActionCode.code_SaveRowColSize));
//							}
						}
					}
				}else{
					ZiorFrameCtl.setTreeNodeWithTask(ziorOpenNodeModel);
				}
				MdTaskDef[] mdTaskDefs=ziorOpenNodeModel.getUiMdTaskDefs();
					if(def!=null&&(def.getIsactive()==null||IConst.TRUE.equals(def.getIsactive()))&&mdTaskDefs!=null&&mdTaskDefs.length>0){
						//节点记忆值在用户关闭节点后，到发发节点或者其他权限地方重新设置不在查看范围内了要过滤掉
						for(MdTaskDef df:mdTaskDefs){
							if(df.getPk_obj().equals(def.getPk_obj())){
								ziorOpenNodeModel.setSelectTaskDef(def);
								ziorOpenNodeModel.setM_selectedOrg(m_selectedOrg);
								ziorOpenNodeModel.setSelectedTreeNode(selectedTreeNode);
								//ziorOpenNodeModel.setUiMdTasks(tasks);
								Map<DimLevel, List<LevelValue>> map=getTaskParadimsMapDefault(def,tasks, ziorOpenNodeModel);
								ziorOpenNodeModel.setMap(map);
								//加载后再重新构建一下filter因为当前记忆的东西因为权限的原因有可能变了
								if(map!=null&&map.size()>0){
									TaskFilter needFilter= getTaskFilter(def,filter,map,  ziorOpenNodeModel);
									ziorOpenNodeModel.setTaskFilter(needFilter);
								}
								
								break;
							}
						}
					
							
					}

		} 
		}catch (BusinessException e) {
			NtbLogger.print(e.getMessage());
		}

	}
	public Map <MdTaskDef, MdTask[]> getMdTaskDefWithDetailAndMdTasks(String taskDefWhere){
		Map<MdTaskDef, MdTask[]>  map;
		try {
			map=TbTaskServiceGetter.getTaskObjectService().getMdTaskDefSimpleAndMdTasks(taskDefWhere);
			return map;
		} catch (BusinessException e) {
			NtbLogger.print(e.getMessage());
		}
		return null;
	}

	/**
	 * 通过TaskFilter得查询条件
	 * @param filter  lym
	 * @return
	 */
	private StringBuffer getQuerytSql(TaskFilter filter) {
		StringBuffer sb = new StringBuffer();
		if (filter != null) {
			String filterSql = filter.getSqlWhereWithEnt(false);
			if (filterSql != null && filterSql.length() > 0)
				sb.append(" and ").append(filterSql);
		}
		return sb;
	}
	/**
	 * 多任务界面的数据初始化
	 * @throws BusinessException 
	 */
		private List<Object>  getMutiTaskInitData(ZiorOpenNodeModel  ziorOpenNodeModel) throws BusinessException {
			String pk_user = InvocationInfoProxy.getInstance().getUserId();
			String pk_group = InvocationInfoProxy.getInstance().getGroupId();
			 //加载所有该节点的记忆值
		    TaskLeach taskLeach = (TaskLeach) TbUserProfileCtl.getInstance()
				.getUserTaskFilter(ziorOpenNodeModel.getFunCode(), pk_user+pk_group);
		    //加载保存行宽列高的记忆值
		    TaskLeach taskLeachRowColSize = (TaskLeach) TbUserProfileCtl.getInstance()
					.getUserTaskFilter(ziorOpenNodeModel.getFunCode(), pk_user+pk_group+ITbPlanActionCode.code_SaveRowColSize);
		    if(taskLeachRowColSize!=null){
		    	Map<String, Map> sizeMap=(Map<String, Map>) taskLeachRowColSize.getAttribute(ITbPlanActionCode.code_SaveRowColSize);
		    	ziorOpenNodeModel.setSaveSizeMap(sizeMap);
		    }
		if (taskLeach != null) {
			ArrayList<Object> values = (ArrayList<Object>) taskLeach
					.getAttributes(ziorOpenNodeModel.getFunCode());
			return values;
		}
		
	     return null;
	     }
/**
 * 根据节点类型过滤任务的参数维值
 * @param map
 * @return
 */
		private Map<DimLevel, List<LevelValue>> filterTaskParadimByNodeType(Map<DimLevel, List<LevelValue>> map,ZiorOpenNodeModel  ziorOpenNodeModel,boolean isDefault) {
			String nodeType=ziorOpenNodeModel.getPlanFrameModel().getNodeType();
			Map<String , String> defaultParamDimPks=ZiorActionTypeFactory.getDefaultParamDimPksMap();
			if (map != null&&map.size()>0) {
				for(DimLevel dl:map.keySet()){
					List<LevelValue> lvs = map.get(dl);
					List<LevelValue>	lvList = new ArrayList<LevelValue>();
							String filedCode = defaultParamDimPks.get(dl.getPrimaryKey());
							if(defaultParamDimPks.containsKey(dl.getPrimaryKey()))
							{
							if(filedCode.equals(TaskFilter.key_pk_year)){
								for (LevelValue lv : lvs) {
									// 计算成员不创建任务
									if (lv!=null&&lv.isCalculatorValue())
										continue;
		
									else{
										lvList.add(lv);
									}
									
								}
							}else  if(filedCode.equals(TaskFilter.key_pk_version)){
								for (LevelValue lv : lvs) {
									// 计算成员不创建任务
									if (lv!=null&&lv.isCalculatorValue())
										continue;
									
									if (lv!=null&&DimBusinessUtil.versionIsCompile(lv)) {
										lvList.add(lv);
									}
								}
							}else  if(filedCode.equals(TaskFilter.key_pk_currency)){
								for (LevelValue lv : lvs) {
									// 计算成员不创建任务
									if (lv!=null&&lv.isCalculatorValue())
										continue;
									String valueKey = lv==null?null:lv.getKey().toString();
									if (valueKey!=null&&(valueKey.equals(IDimMemberPkConst.PK_GLOBE_CURRENCY)
											||valueKey.equals(IDimMemberPkConst.PK_GROUP_CURRENCY)
											||valueKey.equals(IDimMemberPkConst.PK_ORG_CURRENCY)))
										continue;
									lvList.add(lv);
								}
							}else  if(filedCode.equals(TaskFilter.key_pk_month)){
								for (LevelValue lv : lvs) {
									// 计算成员不创建任务
									if (lv!=null&&lv.isCalculatorValue())
										continue;
									else{
										lvList.add(lv);
									}
								}
							}else  if(filedCode.equals(TaskFilter.key_pk_mvtype)){
								if(!nodeType.equals(" ")&&(nodeType.equals(ITbPlanActionCode.COM_NODETYPE)
										||nodeType.equals(ITbPlanActionCode.DIRECTADJUST_NODETYPE)
										||nodeType.equals(ITbPlanActionCode.TABLEOFTOP_NODETYPE)
										||nodeType.equals(ITbPlanActionCode.BALANCE_APPROVE_NODETYPE)
										||nodeType.equals(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE)
										||nodeType.equals(ITbPlanActionCode.ADJUSTBILL_NODETYPE)
										||nodeType.equals(ITbPlanActionCode.ADJUSTDISPENSE_NODETYPE))){
									//编制节点下业务方案的数据,直接调整节点，预算桌面节点
									for (LevelValue lv : lvs) {
										// 计算成员不创建任务
										if (lv!=null&&lv.isCalculatorValue())
											continue;
										if(lv!=null&&lv.getPropValue(DimDocConstant.DIMDOC_DATAATTR_ATTRTYPE).equals((DataAttrtypeEnum.ACTUAL).toStringValue())&&isDefault)
											continue;
										lvList.add(lv);
									}
									
								}
								else if(!nodeType.equals(" ")&&nodeType.equals(ITbPlanActionCode.DAILY_NODETYPE)){
									//日常执行节点下业务方案的数据
									for (LevelValue lv : lvs) {
										// 计算成员不创建任务
										if (lv!=null&&lv.isCalculatorValue())
											continue;
										if(lv!=null&&lv.getPropValue(DimDocConstant.DIMDOC_DATAATTR_ATTRTYPE).equals((DataAttrtypeEnum.BUDGET).toStringValue())&&isDefault)
											continue;
										lvList.add(lv);
									}
								}

							}
							else{
								
									for (LevelValue lv : lvs) {
										// 计算成员不创建任务
										if (lv!=null&&lv.isCalculatorValue())
											continue;
										lvList.add(lv);
									}
							}
							}
							else {
								for (LevelValue lv : lvs) {
									// 计算成员不创建任务
									if (lv!=null&&lv.isCalculatorValue())
										continue;
									lvList.add(lv);
								}
							}
							map.put(dl, lvList);
			}
				
			}
			return map;
		}
		/**
		 * 初始化任务参数的时候分编制节点的初始化还是日常执行的初始化
		 * 编制：业务方案中只显示预算类型的方案
		 * 日常执行：业务方案中只显示实际数类型的方案，日常执行节点，业务方案默认选择实际数
		 * @param dls
		 * @param map
		 */
		private TaskFilter getInitFilter(DimLevel[] dls,Map<DimLevel, List<LevelValue>> map,ZiorOpenNodeModel  ziorOpenNodeModel){
			TaskFilter filter=new TaskFilter();
			String nodeType=ziorOpenNodeModel.getPlanFrameModel().getNodeType();
			Map<String , String> defaultParamDimPks=ZiorActionTypeFactory.getDefaultParamDimPksMap();
			List<LevelValue> lvList = new ArrayList<LevelValue>();
			List<LevelValue> paraDimList = new ArrayList<LevelValue>();
			for(DimLevel dimLevel :dls){
				lvList=map.get(dimLevel);
				if(lvList!=null&& !lvList.isEmpty()){
					String dvcode=defaultParamDimPks.get(dimLevel.getPrimaryKey());
					if(dvcode!=null&&dvcode.equals(TaskFilter.key_pk_year)){
						//显示当前年
						UFDate today = new UFDate(System.currentTimeMillis());
						LevelValue year = dimLevel.getLevelValueByKey(Integer.valueOf((today.getYear())));
						if (year != null &&lvList.contains(year)){
							filter.setAttribute(TaskFilter.key_pk_year,year.getKey().toString());
						}else{
							year = lvList.get(0);
							if (year != null)
							 filter.setAttribute(TaskFilter.key_pk_year,year.getKey().toString());
						}
							
					}
					else if(dvcode!=null&&dvcode.equals(TaskFilter.key_pk_version)){
						for(int i=0;i<lvList.size();i++){
							if (lvList.get(i)!=null&&lvList.get(i).isCalculatorValue())
								continue;
							//默认版本
							if( i==0){
								filter.setAttribute(TaskFilter.key_pk_version, lvList.get(i).getKey().toString());
							}
							if( lvList.get(i).getKey().toString().equals(DimDocCodeConstant.VERSION_KEY_DEFAULT))
							 {
								filter.setAttribute(TaskFilter.key_pk_version, lvList.get(i).getKey().toString());
								break;
							 }
						}
					
					}
					else if(dvcode!=null&&dvcode.equals(TaskFilter.key_pk_mvtype)){
						if(!nodeType.equals("")&&(nodeType.equals(ITbPlanActionCode.COM_NODETYPE)||nodeType.equals(ITbPlanActionCode.DIRECTADJUST_NODETYPE)
								||nodeType.equals(ITbPlanActionCode.TABLEOFTOP_NODETYPE)||nodeType.equals(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE)
								||nodeType.equals(ITbPlanActionCode.ADJUSTBILL_NODETYPE)||nodeType.equals(ITbPlanActionCode.ADJUSTDISPENSE_NODETYPE))){
							for(int i=0;i<lvList.size();i++){
								if (lvList.get(i)!=null&&lvList.get(i).isCalculatorValue())
									continue;
								if(lvList.get(i)!=null&&lvList.get(i).getPropValue(DimDocConstant.DIMDOC_DATAATTR_ATTRTYPE).equals((DataAttrtypeEnum.ACTUAL).toStringValue()))
									continue;
								//预算数
								if( i==0){
									filter.setAttribute(TaskFilter.key_pk_mvtype, lvList.get(i).getKey().toString());
								}
								if(lvList.get(i).getKey().toString().equals(DimDocCodeConstant.DATAATTR_KEY_BUDGET))
								 {
									filter.setAttribute(TaskFilter.key_pk_mvtype, lvList.get(i).getKey().toString());
									break;
								 }
							}
						}
						else if(!nodeType.equals("")&&nodeType.equals(ITbPlanActionCode.DAILY_NODETYPE)){
							for(int i=0;i<lvList.size();i++){
								if (lvList.get(i)!=null&&lvList.get(i).isCalculatorValue())
									continue;
								if(lvList.get(i)!=null&&lvList.get(i).getPropValue(DimDocConstant.DIMDOC_DATAATTR_ATTRTYPE).equals((DataAttrtypeEnum.BUDGET).toStringValue()))
									continue;
								//实际数
								if( i==0){
									filter.setAttribute(TaskFilter.key_pk_mvtype, lvList.get(i).getKey().toString());
								}
								if( lvList.get(i).getKey().toString().equals(DimDocCodeConstant.DATAATTR_KEY_ACTUAL))
								 {
									filter.setAttribute(TaskFilter.key_pk_mvtype, lvList.get(i).getKey().toString());
									break;
								 }
							}
						}

					}
					else if(dvcode!=null&&dvcode.equals(TaskFilter.key_pk_month)){
						UFDate today = new UFDate(System.currentTimeMillis());
						LevelValue month = dimLevel.getLevelValueByKey(Integer.valueOf(today.getMonth()));
						if (month != null && lvList.contains(month)){
							filter.setAttribute(TaskFilter.key_pk_month, month.getKey().toString());
						}else{
							month = lvList.get(0);
							if (month != null)
								//filter.setAttribute(TaskFilter.key_pk_month, new LevelValue[]{month});
							filter.setAttribute(TaskFilter.key_pk_month, month.getKey().toString());
						}
							
					}
					else if(dvcode!=null&&dvcode.equals(TaskFilter.key_pk_currency)){
						for(int i=0;i<lvList.size();i++){
							if (lvList.get(i)!=null&&lvList.get(i).isCalculatorValue())
								continue;
						
								String valueKey = lvList.get(i).getKey().toString();
								if (valueKey.equals(IDimMemberPkConst.PK_GLOBE_CURRENCY)
										||valueKey.equals(IDimMemberPkConst.PK_GROUP_CURRENCY)
										||valueKey.equals(IDimMemberPkConst.PK_ORG_CURRENCY))
									continue;
								//人民币
								if(i==0){
									filter.setAttribute(TaskFilter.key_pk_currency, lvList.get(i).getKey().toString());
								}
								if( lvList.get(i).getKey().toString().equals("1002Z0100000000001K1"))
								 {
									filter.setAttribute(TaskFilter.key_pk_currency, lvList.get(i).getKey().toString());
									break;
								 }
						
						
							
						}
					}
					else if(IDimLevelPKConst.AIMCURR.equals(dimLevel.getPrimaryKey())){
						if(filter.getAttribute(TaskFilter.key_pk_currency)!=null)
						    filter.setAttribute(TaskFilter.key_pk_aimcurr, filter.getAttribute(TaskFilter.key_pk_currency));
						else{
							for(int i=0;i<lvList.size();i++){
								if (lvList.get(i)!=null&&lvList.get(i).isCalculatorValue())
									continue;
									String valueKey = lvList.get(i).getKey().toString();
									if (valueKey.equals(IDimMemberPkConst.PK_GLOBE_CURRENCY)
											||valueKey.equals(IDimMemberPkConst.PK_GROUP_CURRENCY)
											||valueKey.equals(IDimMemberPkConst.PK_ORG_CURRENCY))
										continue;
									//人民币
									if(i==0){
										filter.setAttribute(TaskFilter.key_pk_aimcurr,  lvList.get(i).getKey().toString());
									}
									if( lvList.get(i).getKey().toString().equals("1002Z0100000000001K1"))
									 {
										filter.setAttribute(TaskFilter.key_pk_aimcurr,  lvList.get(i).getKey().toString());
										break;
									 }
								
								
								
							}
						}
					}
					else {
				
						for(LevelValue lv:lvList){
							if (lv!=null&&lv.isCalculatorValue())
								continue;
							if(lv!=null){
								paraDimList.add(lv);
								break;
							}
							
						}
						
					}

				}

			}
			if (!paraDimList.isEmpty()) {
				DimSectionTuple paraDim = new DimSectionTuple(paraDimList);
				IStringConvertor sc = StringConvertorFactory.getConvertor(DimSectionTuple.class);
				filter.setAttribute(TaskFilter.key_pk_paradims, sc.convertToString(paraDim));
			}
			return filter;
		}
		/**
		 * 补充不同任务模板要加载的任务参数维
		 * 编制：业务方案中只显示预算类型的方案
		 * 日常执行：业务方案中只显示实际数类型的方案，日常执行节点，业务方案默认选择实际数
		 * @param dls
		 * @param map
		 * @param paraDimList在方法外面增补，
		 *  补充时如果这个维度含有null维度的任务，那么优先打开null维度，否则不知道是启动的null维度还是后任务模板比前模板增加的维度
		 */    
		private void replenishFilter(DimLevel dimLevel,Map<DimLevel, List<LevelValue>> map,ZiorOpenNodeModel  ziorOpenNodeModel,TaskFilter filter,List<LevelValue> paraDimList){
			String nodeType=ziorOpenNodeModel.getPlanFrameModel().getNodeType();
			Map<String , String> defaultParamDimPks=ZiorActionTypeFactory.getDefaultParamDimPksMap();
			List<LevelValue> lvList = new ArrayList<LevelValue>();
			lvList=map.get(dimLevel);
				if(lvList!=null&&!lvList.isEmpty()){
					if(lvList.contains(null)){
						return;
					}
					String dvcode=defaultParamDimPks.get(dimLevel.getPrimaryKey());
					if(dvcode!=null&&dvcode.equals(TaskFilter.key_pk_year)){
						//显示当前年
						UFDate today = new UFDate(System.currentTimeMillis());
						LevelValue year = dimLevel.getLevelValueByKey(Integer.valueOf((today.getYear())));
					   if (year != null&&lvList.contains(year))
					   {
						   filter.setAttribute(TaskFilter.key_pk_year,year.getKey().toString());
					   }
					   else
					     {
						  if(lvList.get(0)!=null)
						   filter.setAttribute(TaskFilter.key_pk_year, lvList.get(0).getKey().toString());
					     }
				}
					else if(dvcode!=null&&dvcode.equals(TaskFilter.key_pk_version)){
						LevelValue lv = dimLevel.getLevelValueByKey(DimDocCodeConstant.VERSION_KEY_DEFAULT);
						if(lv!=null&&lvList.contains(lv)){
							filter.setAttribute(TaskFilter.key_pk_version, lv.getKey().toString());
						}else{
							if(lvList.get(0)!=null)
							filter.setAttribute(TaskFilter.key_pk_version, lvList.get(0).getKey().toString());
						}
					}
					else if(dvcode!=null&&dvcode.equals(TaskFilter.key_pk_mvtype)){
						if(!nodeType.equals("")&&(nodeType.equals(ITbPlanActionCode.COM_NODETYPE)||nodeType.equals(ITbPlanActionCode.DIRECTADJUST_NODETYPE)||nodeType.equals(ITbPlanActionCode.TABLEOFTOP_NODETYPE)
								||nodeType.equals(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE)
								||nodeType.equals(ITbPlanActionCode.ADJUSTBILL_NODETYPE)
								||nodeType.equals(ITbPlanActionCode.ADJUSTDISPENSE_NODETYPE))){
							LevelValue lv = dimLevel.getLevelValueByKey(DimDocCodeConstant.DATAATTR_KEY_BUDGET);
							if(lv!=null&&lvList.contains(lv)){
								filter.setAttribute(TaskFilter.key_pk_mvtype, lv.getKey().toString());
							}else{
								if(lvList.get(0)!=null)
								filter.setAttribute(TaskFilter.key_pk_mvtype, lvList.get(0).getKey().toString());
							}
						}
						else if(!nodeType.equals("")&&nodeType.equals(ITbPlanActionCode.DAILY_NODETYPE)){
							LevelValue lv = dimLevel.getLevelValueByKey(DimDocCodeConstant.DATAATTR_KEY_ACTUAL);
							if(lv!=null&&lvList.contains(lv)){
								filter.setAttribute(TaskFilter.key_pk_mvtype, lv.getKey().toString());
							}else{
								if(lvList.get(0)!=null)
								filter.setAttribute(TaskFilter.key_pk_mvtype, lvList.get(0).getKey().toString());
							}
						}

					}
					else if(dvcode!=null&&dvcode.equals(TaskFilter.key_pk_month)){
						UFDate today = new UFDate(System.currentTimeMillis());
						LevelValue month = dimLevel.getLevelValueByKey(Integer.valueOf(today.getMonth()));
						if(month!=null&&lvList.contains(month)){
							filter.setAttribute(TaskFilter.key_pk_month, month.getKey().toString());
						}else{
							if(lvList.get(0)!=null)
							filter.setAttribute(TaskFilter.key_pk_month, lvList.get(0).getKey().toString());
					}}
					else if(dvcode!=null&&dvcode.equals(TaskFilter.key_pk_currency)){
						LevelValue lv = dimLevel.getLevelValueByKey("1002Z0100000000001K1");
						if(lv!=null&&lvList.contains(lv)){
							filter.setAttribute(TaskFilter.key_pk_currency, lv.getKey().toString());
						}else{
							if(lvList.get(0)!=null)
							filter.setAttribute(TaskFilter.key_pk_currency, lvList.get(0).getKey().toString());
						}
					}
					else if(IDimLevelPKConst.AIMCURR.equals(dimLevel.getPrimaryKey())){
						if(filter.getAttribute(TaskFilter.key_pk_currency)!=null)
						    filter.setAttribute(TaskFilter.key_pk_aimcurr, filter.getAttribute(TaskFilter.key_pk_currency));
						else{
							LevelValue lv = dimLevel.getLevelValueByKey("1002Z0100000000001K1");
							if(lv!=null&&lvList.contains(lv)){
								filter.setAttribute(TaskFilter.key_pk_aimcurr, lv.getKey().toString());
							}else{
								if(lvList.get(0)!=null)
								filter.setAttribute(TaskFilter.key_pk_aimcurr, lvList.get(0).getKey().toString());
							}
						}
					}else if(IDimLevelPKConst.ENT.equals(dimLevel.getPrimaryKey())){
						if(filter.getAttribute(TaskFilter.key_pk_dataent)!=null)
						    filter.setAttribute(TaskFilter.key_pk_dataent, filter.getAttribute(TaskFilter.key_pk_dataent));
						else{
							if(lvList.get(0)!=null)
							filter.setAttribute(TaskFilter.key_pk_dataent, lvList.get(0).getKey().toString());
							
						}
					}
					else {
				    
				        	for(LevelValue lv:lvList){
								if (lv!=null&&lv.isCalculatorValue())
									continue;
								if(lv!=null){
									paraDimList.add(lv);
									break;
								}
								
							}
				        
					
						
					}

				}
//				else{
//                    //空维度的处理
//					String dvcode=defaultParamDimPks.get(dimLevel.getPrimaryKey());
//					if(defaultParamDimPks.containsKey(dimLevel.getPrimaryKey()))
//					     filter.setAttribute(dvcode, null);
//					
//					else {
//							paraDimList.add(null);
//						}
//						
//					}

				
		}
/**
 ** 过滤掉没启动任务的参数维
 ** @param map
 * @return
 * @throws BusinessException 
 * isFresh 控制过滤的来源任务是否需要从库里重新查询
*/
   private Map<DimLevel, List<LevelValue>> filterTaskParadimByStartTasks(Map<DimLevel, List<LevelValue>> map,ZiorOpenNodeModel  ziorOpenNodeModel,MdTaskDef taskDef,MdTask[] tasks,boolean isFresh) throws BusinessException {
	 //过滤掉没启动任务的参数维
	   if(map!=null&&map.size()>0){
			DimSectionSetTuple tuple=null;
			if(isFresh)
			{
				tuple=TbTaskCtl.getDimSectionSetTupleByTaskDef(taskDef, tasks, true);
			}else{
				tuple=TbTaskCtl.getDimSectionSetTupleByTaskDef(taskDef, null, false);
			}
		   for(DimLevel dl:map.keySet()){
					if(tuple!=null){
						Collection<LevelValue> tupleValues=tuple.getLevelValues(dl);
							List<LevelValue> mapValues=map.get(dl);
							if(tupleValues==null||tupleValues.size()<=0||mapValues==null||mapValues.size()<=0){
								map.put(dl, null);
							}else{
								//求已启动任务和有权限参数维的交集
								List<LevelValue> joinValues=new ArrayList<LevelValue>();
								for(LevelValue value:mapValues){
									if(tupleValues.contains(value)){
										joinValues.add(value);
									}else{
										continue;
									}
								}
								if(tupleValues.contains(null)){
									joinValues.add(null);
								}
								map.put(dl, joinValues);
							}
							
						
					}else{
							map.put(dl, null);
					}
		   }
	   }
		
		return map;
}
/**
 * 单任务节点切换任务模板的刷新
 */
@Override
public ZiorOpenNodeModel refreshZiorOpenNodeModelByMdTaskDef(ZiorOpenNodeModel ziorOpenNodeModel, MdTaskDef def)
		throws BusinessException {
		ziorOpenNodeModel.setSelectTaskDef(def);
		Map<DimLevel, List<LevelValue>> map=getTaskParadimsMapDefault(def,null, ziorOpenNodeModel);
		ziorOpenNodeModel.setMap(map);
		//加载后再重新构建一下filter因为当前记忆的东西因为权限的原因有可能变了
			TaskFilter needFilter= getTaskFilter(def,ziorOpenNodeModel.getTaskFilter(),map,ziorOpenNodeModel);
			ziorOpenNodeModel.setTaskFilter(needFilter);
			MdTask[]    tasks=getMdTasksByWhere(ziorOpenNodeModel);
			if(tasks==null||tasks.length<=0){
				ziorOpenNodeModel.setUiMdTasks(null);
				ziorOpenNodeModel.setM_selectedOrg(null);
				ziorOpenNodeModel.setSelectedTreeNode(null);
			    ziorOpenNodeModel.setSelectMdTask(null);
				LinkedHashMap<DimMember, MdTask> validOrgDimMemberMap = new LinkedHashMap<DimMember, MdTask>();
				ziorOpenNodeModel.setValidOrgDimMemberMap(validOrgDimMemberMap);
				ziorOpenNodeModel.setValidOrgPkList(new HashMap<String, DimMember>());
			}else{
				ziorOpenNodeModel.setUiMdTasks(tasks);
				Map<String, DimMember>  validOrgPkList=getDimMembersByTasks(tasks,ziorOpenNodeModel);
				ziorOpenNodeModel.setValidOrgPkList(validOrgPkList);
			}
	return ziorOpenNodeModel;
}
/**
 * 多任务节点刷新动作的刷新
 * @throws BusinessException 
 */
@Override
public ZiorOpenNodeModel refreshModelByMultiTaskRefresh(ZiorOpenNodeModel ziorOpenNodeModel) throws BusinessException {
	MdTask[] tasks=getMdTasksMulTaskUI(ziorOpenNodeModel);
	ziorOpenNodeModel.setUiMdTasks(tasks);
	MdTaskDef[] mdTaskDefs=getMulTaskUiMdTaskDef(ziorOpenNodeModel);
	ziorOpenNodeModel.setUiMdTaskDefs(mdTaskDefs);
//	MdWorkbook[] books=getMdWorkbooks(ziorOpenNodeModel.getUiMdTaskDefs());
//	ziorOpenNodeModel.setBooks(books);
	String[] funcletOrgPks=getFuncletOrgPks(ziorOpenNodeModel);
	ziorOpenNodeModel.setFuncletOrgPks(funcletOrgPks);
	return ziorOpenNodeModel;
}
/**
 * 获得当前节点有权限的组织
 * @throws BusinessException 
 */
private String[] getFuncletOrgPks(ZiorOpenNodeModel ziorOpenNodeModel) throws BusinessException{
	String[] permOrgPks = null;
	List<String> listpks=null;
	IDataPermGetter permGetter = DataPermGetterUtil.getDataPermGetter();
	String pk_uesr = InvocationInfoProxy.getInstance().getUserId();
	String pk_group = InvocationInfoProxy.getInstance().getGroupId();
	Set<String> setOrgPks = ziorOpenNodeModel.getFunCode() == null ? null : permGetter.getOrgPksWithGroupPermission(pk_uesr, ziorOpenNodeModel.getFunCode(), pk_group);
	if(setOrgPks==null||setOrgPks.size()<=0){
		return null;
	}else{
//		OrgVO[] orgArray = orgs == null ? new OrgVO[0] : orgs
//				.toArray(new OrgVO[0]);
//		listpks=new ArrayList<String>();
//		for (OrgVO orgVo : orgArray) {
//			String pk_org = orgVo.getPk_org();
//			if (!listpks.contains(pk_org)) {
//				listpks.add(pk_org);
//			}
//		}
//		permOrgPks=listpks.toArray(new String[0]);
		permOrgPks = setOrgPks.toArray(new String[0]);
	}
	return permOrgPks;
}
/**
 * 单任务节点切换任务参数维
 * @throws BusinessException 
 */
@Override
public ZiorOpenNodeModel refreshModelBySingleTaskParadim(ZiorOpenNodeModel ziorOpenNodeModel)
		throws BusinessException {
			MdTask[]    tasks=getMdTasksByWhere(ziorOpenNodeModel);
			if(tasks==null||tasks.length<=0){
				ziorOpenNodeModel.setUiMdTasks(null);
				LinkedHashMap<DimMember, MdTask> validOrgDimMemberMap = new LinkedHashMap<DimMember, MdTask>();
				ziorOpenNodeModel.setValidOrgDimMemberMap(validOrgDimMemberMap);
				ziorOpenNodeModel.setValidOrgPkList(new HashMap<String, DimMember>());
			}else{
				ziorOpenNodeModel.setUiMdTasks(tasks);
				Map<String, DimMember>  validOrgPkList=getDimMembersByTasks(tasks,ziorOpenNodeModel);
				ziorOpenNodeModel.setValidOrgPkList(validOrgPkList);
			}

	return ziorOpenNodeModel;
}

@Override
public ZiorOpenNodeModel refreshModelByRefeshAction(ZiorOpenNodeModel ziorOpenNodeModel)
		throws BusinessException {
	 //发布节点有配置的走发布节点配置的没有就默认加载
	TbNodeVO tbNodeVO=null;
    tbNodeVO=getTbNodeVOByPk(ziorOpenNodeModel.getTbNodeVO()==null?null:ziorOpenNodeModel.getTbNodeVO().getPk_obj());//发布节点时配置的数据
	ziorOpenNodeModel.setTbNodeVO(tbNodeVO);
	  MdTaskDef[] taskDefs=getMdTaskDefsByWhere(ziorOpenNodeModel);
	  ziorOpenNodeModel.setUiMdTaskDefs(taskDefs);
	  MdWorkbook[] books=getMdWorkbooks(taskDefs);
	  ziorOpenNodeModel.setBooks(books);
	  if(ziorOpenNodeModel.getSelectTaskDef()!=null&&taskDefs!=null&&taskDefs.length>0){
		  MdTaskDef def=ziorOpenNodeModel.getSelectTaskDef();
		  MdTaskDef seldef=null;
		  for(MdTaskDef df:taskDefs){
			  if(df.getPk_obj().equals(def.getPk_obj())){
				  seldef=df;
				  break;
			  }
		  }
		  if(seldef!=null&&(seldef.getIsactive()==null||IConst.TRUE.equals(seldef.getIsactive()))){
			  ziorOpenNodeModel.setSelectTaskDef(seldef);
			  ziorOpenNodeModel.setUiMdTasks(null);//重新查找任务来构建当前节点的可用的参数维
			  Map<DimLevel, List<LevelValue>> map=getTaskParadimsMapDefault(seldef,null, ziorOpenNodeModel);
				ziorOpenNodeModel.setMap(map);
				//加载后再重新构建一下filter因为当前记忆的东西因为权限的原因有可能变了
					TaskFilter needFilter= getTaskFilter(seldef,ziorOpenNodeModel.getTaskFilter(),map,ziorOpenNodeModel);
					ziorOpenNodeModel.setTaskFilter(needFilter);
					MdTask[]    tasks=getMdTasksByWhere(ziorOpenNodeModel);
					if(tasks==null||tasks.length<=0){
						ziorOpenNodeModel.setUiMdTasks(null);
						ziorOpenNodeModel.setSelectMdTask(null);
						ziorOpenNodeModel.setM_selectedOrg(null);
						ziorOpenNodeModel.setSelectedTreeNode(null);
						LinkedHashMap<DimMember, MdTask> validOrgDimMemberMap = new LinkedHashMap<DimMember, MdTask>();
						ziorOpenNodeModel.setValidOrgDimMemberMap(validOrgDimMemberMap);
						ziorOpenNodeModel.setValidOrgPkList(new HashMap<String, DimMember>());
					}else{
						ziorOpenNodeModel.setUiMdTasks(tasks);
						Map<String, DimMember>  validOrgPkList=getDimMembersByTasks(tasks,ziorOpenNodeModel);
						ziorOpenNodeModel.setValidOrgPkList(validOrgPkList);
					}
					
		  }else{
			    ziorOpenNodeModel.setUiMdTasks(null);
			    ziorOpenNodeModel.setSelectTaskDef(null);
			    ziorOpenNodeModel.setM_selectedOrg(null);
			    ziorOpenNodeModel.setSelectedTreeNode(null);
			    ziorOpenNodeModel.setSelectMdTask(null);
			    if(ziorOpenNodeModel.getTaskFilter()!=null)
			    {
			    	ziorOpenNodeModel.getTaskFilter().clear();
			    	if(ziorOpenNodeModel.getTaskFilter().getParaMap()!=null)
			    	 ziorOpenNodeModel.getTaskFilter().getParaMap().clear();
			    }
			    if(ziorOpenNodeModel.getMap()!=null)
			    {
			    	ziorOpenNodeModel.getMap().clear();
			    }
				LinkedHashMap<DimMember, MdTask> validOrgDimMemberMap = new LinkedHashMap<DimMember, MdTask>();
				ziorOpenNodeModel.setValidOrgDimMemberMap(validOrgDimMemberMap);
				ziorOpenNodeModel.setValidOrgPkList(new HashMap<String, DimMember>());
		  }
	
	  }else{
		    ziorOpenNodeModel.setUiMdTasks(null);
		    ziorOpenNodeModel.setSelectTaskDef(null);
		    ziorOpenNodeModel.setM_selectedOrg(null);
		    ziorOpenNodeModel.setSelectedTreeNode(null);
		    ziorOpenNodeModel.setSelectMdTask(null);
		  //  ziorOpenNodeModel.getTaskFilter().clear();
		    //ziorOpenNodeModel.getTaskFilter().getParaMap().clear();
		    if( ziorOpenNodeModel.getMap()!=null)
		    {
		    	ziorOpenNodeModel.getMap().clear();
		    }
			LinkedHashMap<DimMember, MdTask> validOrgDimMemberMap = new LinkedHashMap<DimMember, MdTask>();
			ziorOpenNodeModel.setValidOrgDimMemberMap(validOrgDimMemberMap);
			ziorOpenNodeModel.setValidOrgPkList(new HashMap<String, DimMember>());
	  }
	  return ziorOpenNodeModel;
}
@Override
public Map<String,Object> getTaskDataMap(MdTask task, String[] pk_sheets, boolean isLoadConsistRule, HashMap<String, CubeSnapShot> snapShotMap, boolean isInitFunctionText, String nodeType,Map<String,Object> paras)
		throws BusinessException {
	CostTime ct = new CostTime();
	Map<String,Object> taskDataMap=new HashMap<String, Object>();
	MdTaskDef   def=null;
	TaskDataModel taskDataModel = null;
	taskDataModel = TaskDataCtl.getTaskDataModel(task.getPrimaryKey(),pk_sheets, isLoadConsistRule, snapShotMap/*null*/, isInitFunctionText/*true*/);
	ct.printStepCost("getTaskDataMap getTaskDataModel:");
	def=task.getTaskDefWithoutDetail();
	String taskType=def.getTasktype();
	if(taskType.equals(IZjTaskConst.strTaskType_zjph)){
		TbTaskCtl.setModelMvtypeTableHead(taskDataModel);
	}
	TaskDataCtl.initTaskDataModelRuleInfo(taskDataModel, nodeType);
	ct.printStepCost("getTaskDataMap initTaskDataModelRuleInfo:");
	TaskDataModelAction action = new TaskDataModelAction(taskDataModel);
	TbbFormulaExecuteLogs logs=action.executeTableHeadFormula(null);
	ct.printStepCost("getTaskDataMap executeTableHeadFormula:");
	taskDataMap.put(ITbPlanActionCode.TBBFORMULAEXECUTELOGS, logs);
	taskDataMap.put(ITbPlanActionCode.TASKDATAMODEL, taskDataModel);
	TaskExtInfoLoader taskExtInfoLoader = new TaskExtInfoLoader();
	taskExtInfoLoader.setM_task(task/*taskExtInfoLoaderi.getM_task()*/);
	if (ITbPlanActionCode.APPROVE_NODETYPE.equals(nodeType)
			|| ITbPlanActionCode.BROESE_NODETYPE.equals(nodeType)
			|| ITbPlanActionCode.TABLEOFTOP_NODETYPE.equals(nodeType)
			|| ITbPlanActionCode.VERSION_NODETYPE.equals(nodeType)
			|| ITbPlanActionCode.ADJUSTAPPROVE_NODETYPE.equals(nodeType)
			||ITbPlanActionCode.COM_NODETYPE.equals(nodeType)
			||ITbPlanActionCode.PREAPPROVE_NODETYPE.equals(nodeType)
			||ITbPlanActionCode.DIRECTADJUST_NODETYPE.equals(nodeType)) {
		taskExtInfoLoader.loadIndexAppdetails();
		ct.printStepCost("getTaskDataMap loadIndexAppdetails:");
	}
	if(ITbPlanActionCode.CONSCHEME_NODETYPE.equals(nodeType)){
		taskExtInfoLoader.loadAlarmShemes() ;
		ct.printStepCost("getTaskDataMap loadAlarmShemes:");
	}
	taskExtInfoLoader.loadTaskNoteMap();
	ct.printStepCost("getTaskDataMap loadTaskNoteMap:");
	if(paras!=null&&paras.get(ITaskConst.versionTask)!=null&&paras.get(ITaskConst.versionTask) instanceof MdTask[]){
		MdTask[] versionTasks = (MdTask[])paras.get(ITaskConst.versionTask);
		taskExtInfoLoader.initVersionDataCellMap(versionTasks);
		ct.printStepCost("getTaskDataMap initVersionDataCellMap:");
	}
	taskDataMap.put(ITbPlanActionCode.TASKECTINFOLOADER, taskExtInfoLoader);
	//加载任务的可见表单
	String[] publicNodePks=  paras == null || paras.get(ITbPlanActionCode.PUBLICNODEPKS)==null ? null : (String[])paras.get(ITbPlanActionCode.PUBLICNODEPKS);
	String sheetGroupName= paras == null || paras.get(ITbPlanActionCode.SHEETGROUPNAME)==null ? null :(String)paras.get(ITbPlanActionCode.SHEETGROUPNAME) ;
	String pk_org=paras == null || paras.get(ITbPlanActionCode.USERPK_ORG)==null ? null :(String)paras.get(ITbPlanActionCode.USERPK_ORG) ;
	taskDataMap.put(ITbPlanActionCode.TASKVALIDLOOKSHEETS, ZiorFrameCtl.getTaskValidLookPkSheets( task,publicNodePks,sheetGroupName,false,pk_org,def));
	ct.printStepCost("getTaskDataMap end:");
	return taskDataMap;
}

@Override
public MdTask[] getCanApproveTasks(MdTask[] tasks) throws BusinessException {
	int type = INtbPermConst.TYPE_BUDGETTASK;// 预算任务资源实体类型
	String[] opercodes = new String[] { INtbPermConst.RES_OPERCODE_APPROVE };
	List<MdTask> list = new ArrayList<MdTask>();
	for (MdTask task : tasks) {
		list.add(task);
	}
	String pk_user = InvocationInfoProxy.getInstance().getUserId();
	String pk_group = InvocationInfoProxy.getInstance().getGroupId();
	Set<MdTask> tasks_filtered = filterObjectsByPrv(list, type, opercodes,
			pk_user, pk_group);
	if (tasks_filtered == null || tasks_filtered.size() != list.size()) {
		throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan", "01812pln_000698")/*任务权限校验不通过*/);
	}
	List<MdTask> tasks_reload = loadTaskAppFlowInfo(tasks_filtered.toArray(new MdTask[0]));
	return tasks_reload.toArray(new MdTask[0]);
}
private <T extends INtbPerm> Set<T> filterObjectsByPrv(List<T> objlist,
		int type, String[] openCode, String user, String pk_group)
		throws BusinessException {
	if (objlist == null || objlist.size() == 0) {
		return null;
	}
	IDataPermGetter data = DataPermGetterUtil.getDataPermGetter();// 数据权限过滤的接口
	try {
		Set<T> result = data.getDatasWithPermission(objlist, type,
				openCode, user, pk_group); // 数据权限过滤接口调用的方法
		return result;
	} catch (BusinessException e) {
		throw e;
	}
}
private static List<MdTask> loadTaskAppFlowInfo(MdTask[] tasks)
throws BusinessException {
if (tasks == null || tasks.length == 0) {
return null;
}
List<String> pk_tasks = new ArrayList<String>();
for (MdTask task : tasks) {
pk_tasks.add(task.getPrimaryKey());
}

ITaskBusinessService service = NCLocator.getInstance().lookup(
	ITaskBusinessService.class);
List<MdTask> tasks_reload = service.loadTaskFlowAppInfo(pk_tasks);
return tasks_reload;
}
protected String getQueryWorkbookWhere() { 
	return " busitype='"+IFormConst.workbook_busitype_exchangerate+"' order by ts"; 

	} 
public MdSheetGroup[] getSheetGroupByMdWorkbook(String pk_book) {
	MdSheetGroup[] groups=null;
	HashMap<MdSheetGroup, MdSheet[]> map;
		try {
			map = pk_book==null ? null : FormServiceGetter.getFormGroupService().getSheetGroupMapByPkWorkBook(pk_book);
			if (map != null&&map.size()>0) {
				groups  = map.keySet().toArray(new MdSheetGroup[0]);
				}
		} catch (BusinessException e) {
			NtbLogger.printException(e);
		}
		
		return groups;
}
/**
 * 查询给出的任务模板对应的套表的表单分组
 * {方法功能中文描述}
 * 
 * @param taskDefs
 * @return
 * @throws BusinessException
 * @author: liyingm@yonyou.com
 */
       @Override
          public Map<String, Map> getSheetGroupMapByPkWorkBook(MdTaskDef[] taskDefs) throws BusinessException {
    	   BaseDAO base=new BaseDAO();
    	   Map<String,Map> allTaskefGroups=new HashMap<String, Map>();
    	   if(taskDefs==null||taskDefs.length<=0){
				 return  allTaskefGroups;
			 }
          	for(MdTaskDef def:taskDefs){
          		if(def!=null){
         			if(allTaskefGroups.containsKey(def.getPk_workbook())){
          				continue;
          			}
          			Map<MdSheetGroup, MdSheet[]> map = null;
    				try {
    					map = FormServiceGetter.getFormGroupService().getSheetGroupMapByPkWorkBook(def.getPk_workbook());
    					if (map != null&&map.size()>0) {
    						allTaskefGroups.put(def.getPk_workbook(), map);
    						}
    				} catch (BusinessException e) {
    					NtbLogger.printException(e);
    				}
    				
          		}
          	}
			return allTaskefGroups;
         }
       /**
   	 * 获取taskde可见sheet范围 liyingm+
   	 *  User2SheetCvsTools.getPkSheets(pk_user,pk_group,pk_责任主体,)，
   	 * 返回用户分配职责的表单pk，多个职责设置表单有交叉的话pk有可能重复，
   	 * 不过传给TaskDataModel的表单pk重复也没有关系。如没有可适配内容则返回null
   	 * 可见表单为空，则打开任务可计算表单范围
   	 * isApp=true为审批表单范围，在审批流节点配置
   	 * @return
   	 */
@Override
public String[] getTaskValidLookPkSheets(MdTask task,
		String[] publicnodeLookSheets, String sheetGroupName, boolean isApp,
		String pk_org, MdTaskDef def) throws BusinessException {
	String[] taskValidLookSheets = null;
	if (task == null)
		return taskValidLookSheets;
	String pk_user = InvocationInfoProxy.getInstance().getUserId();
	String pk_group= InvocationInfoProxy.getInstance().getGroupId();
	// 发布节点时设置的表单范围
	// 可见表单范围为所有可设置地方的交集
	String[] publicNodePks = publicnodeLookSheets;
	String[] formSheetPks = null;
	String[] publicAppPks = null;
	try {
		// 套表管理节点设置的表单分组
		formSheetPks = FormServiceGetter.getFormGroupService().getPkSheetsByUserperm(task.getPk_workbook(), pk_user, pk_org, pk_group);
	} catch (BusinessException e) {
		NtbLogger.print(e.getMessage());
	}
	if (sheetGroupName != null && !sheetGroupName.equals("")) {
		try {
			// 任务管理发布节点设置的范围
			SuperVO[] vos = NtbSuperServiceGetter.getINtbSuper().queryByCondition(MdSheetGroup.class, "pk_workbook='" + task.getPk_workbook() + "' and objname='" + sheetGroupName + "'");
			if (vos != null && vos.length > 0) {
				vos = NtbSuperServiceGetter.getINtbSuper().queryByCondition(MdSheetGroupM.class, "pk_sheetgroup='" + vos[0].getPrimaryKey() + "'");
				if (vos != null && vos.length > 0) {
					String[] pks = new String[vos.length];
					for (int i = 0; i < vos.length; i++)
						pks[i] = ((MdSheetGroupM) vos[i]).getPk_sheet();
					taskValidLookSheets = pks;
				}
			}
		} catch (BusinessException be) {
			return null;
		}
	} else {
		// 任务管理节点设置的范围
		taskValidLookSheets = SheetGroupCtl.getPkSheetsByTaskSheetList(task.getSheetlist(), true);

	}
	publicAppPks = getDefaultPkSheets(task,pk_org, def);
	if (publicNodePks != null && publicNodePks.length > 0) {
		List<String> pks = new ArrayList<String>();
		List<String> lookPks = new ArrayList<String>();
		pks.addAll(Arrays.asList(publicNodePks));
		if (taskValidLookSheets != null && taskValidLookSheets.length > 0) {
			for (String k : taskValidLookSheets) {
				if (pks.contains(k)) {
					lookPks.add(k);
				} else {
					continue;
				}
			}
			if (lookPks.size() > 0)
				taskValidLookSheets = lookPks.toArray(new String[0]);
			else
				taskValidLookSheets = null;
		} else {
			taskValidLookSheets = publicNodePks;
		}
	}
	if (formSheetPks != null && formSheetPks.length > 0) {

		List<String> pks = new ArrayList<String>();
		List<String> lookPks = new ArrayList<String>();
		pks.addAll(Arrays.asList(formSheetPks));
		if (taskValidLookSheets != null && taskValidLookSheets.length > 0) {
			for (String k : taskValidLookSheets) {
				if (pks.contains(k)) {
					lookPks.add(k);
				} else {
					continue;
				}
			}
			if (lookPks.size() > 0)
				taskValidLookSheets = lookPks.toArray(new String[0]);
			else
				taskValidLookSheets = null;
		} else {
			taskValidLookSheets = formSheetPks;
		}

	}
	if (publicAppPks != null && publicAppPks.length > 0) {

		List<String> pks = new ArrayList<String>();
		List<String> lookPks = new ArrayList<String>();
		pks.addAll(Arrays.asList(publicAppPks));
		if (taskValidLookSheets != null && taskValidLookSheets.length > 0) {
			for (String k : taskValidLookSheets) {
				if (pks.contains(k)) {
					lookPks.add(k);
				} else {
					continue;
				}
			}
			if (lookPks.size() > 0)
				taskValidLookSheets = lookPks.toArray(new String[0]);
			else
				taskValidLookSheets = null;
		} else {
			taskValidLookSheets = publicAppPks;
		}

	}
	return taskValidLookSheets;
}
/**
 * 得到审批中的任务表单范围 {方法功能中文描述}
 * 
 * @return
 * @author: 
 */
public static String[] getDefaultPkSheets(MdTask task,String pk_org,MdTaskDef def) {
	String[] pk_sheets = null;
	if (task != null && ITaskStatus.APPROVING.equals(task.getPlanstatus())) {
		try {
			AggregatedMdTaskVO billvo = new AggregatedMdTaskVO();
			billvo.setParentVO(task);
			if(def==null){
				def = TbTaskServiceGetter.getTaskObjectService().getMdTaskDefByPk(task.getPk_taskdef());
			}
			String billType = def.getPk_transtype();

			IWorkflowMachine wfMachine = NCLocator.getInstance().lookup(IWorkflowMachine.class);
			WorkflownoteVO wfNoteVo = wfMachine.checkWorkFlow(IPFActionName.APPROVE, billType, billvo, null);
			List list = wfNoteVo.getApplicationArgs();
			for (int i = 0; i < list.size(); i++) {
				DataField field = (DataField) list.get(i);
				ArrayList<SheetVO> sheetVos = (ArrayList<SheetVO>) XmlUtils.fromXML(field.getInitialValue());
				for (SheetVO sVo : sheetVos) {
					boolean isMatch = false;
					if (sVo.getUserId() != null) {
						if (sVo.getUserId().equals(InvocationInfoProxy.getInstance().getUserId()))
							isMatch = true;
					}
					if (sVo.getRoleId() != null && !isMatch) {
						IRoleManageQuery roleBS = (IRoleManageQuery) NCLocator.getInstance().lookup(IRoleManageQuery.class.getName());
						RoleVO[] roles = roleBS.queryRoleByUserID(InvocationInfoProxy.getInstance().getUserId(),
								 pk_org);
						if (roles != null) {
							for (int j = 0; j < roles.length; j++) {
								if (sVo.getRoleId().equals(roles[i].getPrimaryKey())) {
									isMatch = true;
									break;
								}
							}
						}
					}
					if (isMatch) {
						List<MdSheet> sheetList = sVo.getSheetList();
						if (sheetList != null) {
							pk_sheets = new String[sheetList.size()];
							for (int j = 0; j < pk_sheets.length; j++)
								pk_sheets[j] = sheetList.get(j).getPrimaryKey();
						}
						break;
					}
				}
			}
		} catch (Throwable t) {
			NtbLogger.printException(t);
			pk_sheets = null;
		}
	}
	if (pk_sheets != null && pk_sheets.length == 0)
		pk_sheets = null;
	return pk_sheets;
}

@Override
public void saveExOlapInfoSet(String primaryKey,
		List<ExOlapInfoSet> olapInfoList) throws BusinessException {
	TbTaskExtCtl.saveExOlapInfoSet(primaryKey,olapInfoList);
	
}

@Override
public Map<String,Object> saveTaskDataMesMap(String nodetype,
		TaskDataModel taskDataModel, MdTask task) throws BusinessException {
	TbbFormulaExecuteLogs logs = ZiorFrameCtl.executeFmlAndRule( nodetype, taskDataModel, task);
	TaskDataCtl.initTaskDataModelRuleInfo(taskDataModel, nodetype);
	Map<String,Object> map=new HashMap<String,Object>();
	
	//如果是直接调整节点需要校验控制规则
	if(nodetype.equals(ITbPlanActionCode.DIRECTADJUST_NODETYPE)) {
		String controlMsg = CtlSchemeCTL.checkExistCtrlSchemeFindByDvInDirectAdjust(taskDataModel.getChangedDataCells());
		map.put(ITbPlanActionCode.ADJUSTCONTROLRULE, controlMsg);
	}
	
	// 保存数据模型
	UFDateTime save = taskDataModel.save();
	map.put(ITbPlanActionCode.SAVETIME, save);
	map.put(ITbPlanActionCode.TASKDATAMODEL, taskDataModel);
	map.put(ITbPlanActionCode.TBBFORMULAEXECUTELOGS, logs);
	return map;
}

@Override
public Map<String, Object> saveTaskDataNotExcelMesMap(String nodetype,
		TaskDataModel taskDataModel, MdTask task) throws BusinessException {
	TbbFormulaExecuteLogs logs = new TbbFormulaExecuteLogs();
	TaskDataCtl.initTaskDataModelRuleInfo(taskDataModel, nodetype);
	Map<String,Object> map=new HashMap<String,Object>();
	
	//如果是直接调整节点需要校验控制规则
	if(nodetype.equals(ITbPlanActionCode.DIRECTADJUST_NODETYPE)) {
		String controlMsg = CtlSchemeCTL.checkExistCtrlSchemeFindByDvInDirectAdjust(taskDataModel.getChangedDataCells());
		map.put(ITbPlanActionCode.ADJUSTCONTROLRULE, controlMsg);
	}
	
	// 保存数据模型
	UFDateTime save = taskDataModel.save();
	map.put(ITbPlanActionCode.SAVETIME, save);
	map.put(ITbPlanActionCode.TASKDATAMODEL, taskDataModel);
	map.put(ITbPlanActionCode.TBBFORMULAEXECUTELOGS, logs);
	return map;
}

private static void executeFmlAndRule(String nodetype,TaskDataModel taskDataModel,MdTask task) throws BusinessException {
	taskDataModel.instance();
	if(task==null||nodetype==null||taskDataModel==null){
		return ; 
	}
	List<BusiRuleVO> defaultRules = null;
	// 编制执行 计算规则
	if (ITbPlanActionCode.COM_NODETYPE.equals(nodetype)
			|| ITbPlanActionCode.ADJUSTAPPROVE_NODETYPE
					.equals(nodetype)
			|| ITbPlanActionCode.DIRECTADJUST_NODETYPE   //直接调整
					.equals(nodetype)
			|| ITbPlanActionCode.TABLEOFTOP_NODETYPE
					.equals(nodetype)||ITbPlanActionCode.APPROVE_NODETYPE.equals(nodetype)
					||ITbPlanActionCode.PREAPPROVE_NODETYPE.equals(nodetype)) {
		defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(
				task.getPk_taskdef(),
				NTBActionEnum.CALACTION.toCodeString());
		// 日常执行取数规则
	} else if (ITbPlanActionCode.DAILY_NODETYPE.equals(nodetype)) {
		defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(
				task.getPk_taskdef(),
				NTBActionEnum.GETDATAACTION.toCodeString());
	}
	if (defaultRules != null && defaultRules.size() > 0) {
		  nc.ms.tb.task.RuleExecuteHelper.executeWorkBookRule(taskDataModel, defaultRules);
	}
//	TaskDataModelAction action = new TaskDataModelAction(taskDataModel);
	//添加按照表单分配范围执行规则 by:wangzhqa  2014-3-18
	
	
//	TaskExecuteHelper.getExecuteSheetList(taskDataModel.getMdTask(), taskDataModel);
	
	 
}
}







