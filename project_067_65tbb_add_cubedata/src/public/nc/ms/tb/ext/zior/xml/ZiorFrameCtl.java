package nc.ms.tb.ext.zior.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.mdm.dim.IDimManager;
import nc.itf.tb.plan.IZiorFrameModelService;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.tb.formula.script.TbbFormulaExecuteLogs;
import nc.ms.tb.pub.TbParamUtil;
import nc.ms.tb.pub.TbUserProfileCtl;
import nc.ms.tb.pubutil.CostTime;
import nc.ms.tb.rule.RuleManager;
import nc.ms.tb.task.RuleExecuteHelper;
import nc.ms.tb.task.TaskExecuteHelper;
import nc.ms.tb.task.TbTaskCtl;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ms.tb.task.data.TaskDataModelAction;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.vo.mdm.cube.CubeSnapShot;
import nc.vo.mdm.cube.DimSectionTuple;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.IDimLevelPKConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.sm.funcreg.FuncRegisterVO;
import nc.vo.tb.form.MdSheet;
import nc.vo.tb.form.MdSheetGroup;
import nc.vo.tb.form.excel.ExOlapInfoSet;
import nc.vo.tb.ntbenum.NTBActionEnum;
import nc.vo.tb.pubutil.ProfileSchema;
import nc.vo.tb.rule.BusiRuleVO;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;
import nc.vo.tb.task.TaskFilter;
import nc.vo.tb.task.TaskLeach;

/**
 * �����ܵĺ�̨���񹤾���
 * @author liyingm
 *
 */
public class ZiorFrameCtl {
	public static HashMap<String, String> dimLevelPk2TaskFields = new HashMap<String, String>();
	static {
		dimLevelPk2TaskFields.put(IDimLevelPKConst.ENT, "pk_dataent");
		dimLevelPk2TaskFields.put(IDimLevelPKConst.CURR, "pk_currency");
		dimLevelPk2TaskFields.put(IDimLevelPKConst.MVTYPE, "pk_mvtype");
		dimLevelPk2TaskFields.put(IDimLevelPKConst.VERSION, "pk_version");
		dimLevelPk2TaskFields.put(IDimLevelPKConst.YEAR, "pk_year");
		dimLevelPk2TaskFields.put(IDimLevelPKConst.MONTH, "pk_month");
		dimLevelPk2TaskFields.put(IDimLevelPKConst.AIMCURR, "pk_aimcurr");
	}
	public static ZiorOpenNodeModel getZiorOpenNodeModel(FuncRegisterVO frVO,String sysCode,String mdTaskDefName, String fileConfig,String nodepk )throws BusinessException {
		ZiorOpenNodeModel  ziorOpenNodeModel=NCLocator.getInstance().lookup(IZiorFrameModelService.class).getZiorOpenNodeModel(frVO,sysCode,mdTaskDefName,fileConfig, nodepk );
		if(ziorOpenNodeModel.getUiMdTaskDefs()!=null&&ziorOpenNodeModel.getUiMdTaskDefs().length>0){
			TbTaskCtl.loadDetail(ziorOpenNodeModel.getUiMdTaskDefs(),ziorOpenNodeModel.getBooks());
			MdTaskDef selectDef=ziorOpenNodeModel.getSelectTaskDef();
			if(selectDef!=null){
				for(MdTaskDef def:ziorOpenNodeModel.getUiMdTaskDefs()){
					if(def.getPk_obj().equals(selectDef.getPk_obj())){
						ziorOpenNodeModel.setSelectTaskDef(def);
						break;
					}
				}
			}

		}
		if(ziorOpenNodeModel.getUiMdTasks()!=null&&ziorOpenNodeModel.getUiMdTasks().length>0){
			TbTaskCtl.loadDimMemberValues(ziorOpenNodeModel.getUiMdTasks(),true);
		}
		 //����ά��˳���pkֵƴ�������ַ���������Ķ�Ӧ��ϵMap
	 setTreeNodeWithTask(  ziorOpenNodeModel);
	 LinkedHashMap<String, MdTask> pkstoTaskMap =getPksToTaskMap(ziorOpenNodeModel.getUiMdTasks(),ziorOpenNodeModel);
	 ziorOpenNodeModel.setPkstoTaskMap(pkstoTaskMap);
	 if(TbParamUtil.hasSheetGroup()){
		 loadSheetGroupByMdWorkbook( ziorOpenNodeModel,true);
	 }
		return ziorOpenNodeModel;


	}
	/**
	 * ������ڵ��л�����ģ���ˢ��
	 */
	public static ZiorOpenNodeModel refreshZiorOpenNodeModelByMdTaskDef(ZiorOpenNodeModel  ziorOpenNodeModel,MdTaskDef def)throws BusinessException {
		loadTaskDefParadimMess(  ziorOpenNodeModel);
	    ziorOpenNodeModel=NCLocator.getInstance().lookup(IZiorFrameModelService.class).refreshZiorOpenNodeModelByMdTaskDef(ziorOpenNodeModel, def);
		if(ziorOpenNodeModel.getUiMdTaskDefs()!=null&&ziorOpenNodeModel.getUiMdTaskDefs().length>0){
			TbTaskCtl.loadDetail(ziorOpenNodeModel.getUiMdTaskDefs(),ziorOpenNodeModel.getBooks());
			MdTaskDef selectDef=ziorOpenNodeModel.getSelectTaskDef();
			if(selectDef!=null){
				for(MdTaskDef df:ziorOpenNodeModel.getUiMdTaskDefs()){
					if(df.getPk_obj().equals(selectDef.getPk_obj())){
						ziorOpenNodeModel.setSelectTaskDef(df);
						break;
					}
				}
			}
		}
		if(ziorOpenNodeModel.getUiMdTasks()!=null&&ziorOpenNodeModel.getUiMdTasks().length>0){
			TbTaskCtl.loadDimMemberValues(ziorOpenNodeModel.getUiMdTasks(),true);
		}
		 //����ά��˳���pkֵƴ�������ַ���������Ķ�Ӧ��ϵMap
		setTreeNodeWithTask(  ziorOpenNodeModel);
		  LinkedHashMap<String, MdTask> pkstoTaskMap =getPksToTaskMap(ziorOpenNodeModel.getUiMdTasks(),ziorOpenNodeModel);;
		  ziorOpenNodeModel.setPkstoTaskMap(pkstoTaskMap);
		  if(TbParamUtil.hasSheetGroup()){
			  loadSheetGroupByMdWorkbook( ziorOpenNodeModel,false);
		  }

		return ziorOpenNodeModel;


	}
	/**
	 * ������ڵ�ˢ�¶�����ˢ��+��ѯ��ť
	 * @throws BusinessException
	 */
	public static ZiorOpenNodeModel refreshModelByMultiTaskRefresh(ZiorOpenNodeModel  ziorOpenNodeModel)throws BusinessException {
	    ziorOpenNodeModel=NCLocator.getInstance().lookup(IZiorFrameModelService.class).refreshModelByMultiTaskRefresh(ziorOpenNodeModel);
		if(ziorOpenNodeModel.getUiMdTaskDefs()!=null&&ziorOpenNodeModel.getUiMdTaskDefs().length>0){
			TbTaskCtl.loadDetail(ziorOpenNodeModel.getUiMdTaskDefs(),ziorOpenNodeModel.getBooks());
			MdTaskDef selectDef=ziorOpenNodeModel.getSelectTaskDef();
			if(selectDef!=null){
				for(MdTaskDef def:ziorOpenNodeModel.getUiMdTaskDefs()){
					if(def.getPk_obj().equals(selectDef.getPk_obj())){
						ziorOpenNodeModel.setSelectTaskDef(def);
						break;
					}
				}
			}
		}
		if(ziorOpenNodeModel.getUiMdTasks()!=null&&ziorOpenNodeModel.getUiMdTasks().length>0){
			TbTaskCtl.loadDimMemberValues(ziorOpenNodeModel.getUiMdTasks(),true);
		}
		 if(TbParamUtil.hasSheetGroup()){
			 loadSheetGroupByMdWorkbook( ziorOpenNodeModel,true);
		 }
		return ziorOpenNodeModel;


	}
	/**
	 * ������ڵ��л��������ά
	 * @throws BusinessException
	 */
	public static ZiorOpenNodeModel refreshModelBySingleTaskParadim(ZiorOpenNodeModel  ziorOpenNodeModel)throws BusinessException {
		loadTaskDefParadimMess(  ziorOpenNodeModel);
	    ziorOpenNodeModel=NCLocator.getInstance().lookup(IZiorFrameModelService.class).refreshModelBySingleTaskParadim(ziorOpenNodeModel);
		if(ziorOpenNodeModel.getUiMdTaskDefs()!=null&&ziorOpenNodeModel.getUiMdTaskDefs().length>0){
			TbTaskCtl.loadDetail(ziorOpenNodeModel.getUiMdTaskDefs(),ziorOpenNodeModel.getBooks());
			MdTaskDef selectDef=ziorOpenNodeModel.getSelectTaskDef();
			if(selectDef!=null){
				for(MdTaskDef def:ziorOpenNodeModel.getUiMdTaskDefs()){
					if(def.getPk_obj().equals(selectDef.getPk_obj())){
						ziorOpenNodeModel.setSelectTaskDef(def);
						break;
					}
				}
			}
		}
		if(ziorOpenNodeModel.getUiMdTasks()!=null&&ziorOpenNodeModel.getUiMdTasks().length>0){
			TbTaskCtl.loadDimMemberValues(ziorOpenNodeModel.getUiMdTasks(),true);
		}
		 //����ά��˳���pkֵƴ�������ַ���������Ķ�Ӧ��ϵMap
		  setTreeNodeWithTask(  ziorOpenNodeModel);
		  LinkedHashMap<String, MdTask> pkstoTaskMap =getPksToTaskMap(ziorOpenNodeModel.getUiMdTasks(),ziorOpenNodeModel);;
		  ziorOpenNodeModel.setPkstoTaskMap(pkstoTaskMap);
		  if(TbParamUtil.hasSheetGroup()){
			  loadSheetGroupByMdWorkbook( ziorOpenNodeModel,false);
		  }

		return ziorOpenNodeModel;


	}
	/**
	 * ������ڵ�������ϵ�ˢ�°�ť������+����ģ��+ѡ�������taskdatamodel
	 * @throws BusinessException
	 */
	public static ZiorOpenNodeModel refreshModelByRefeshAction(ZiorOpenNodeModel  ziorOpenNodeModel)throws BusinessException {
		loadTaskDefParadimMess(  ziorOpenNodeModel);
	    ziorOpenNodeModel=NCLocator.getInstance().lookup(IZiorFrameModelService.class).refreshModelByRefeshAction(ziorOpenNodeModel);
		if(ziorOpenNodeModel.getUiMdTaskDefs()!=null&&ziorOpenNodeModel.getUiMdTaskDefs().length>0){
			TbTaskCtl.loadDetail(ziorOpenNodeModel.getUiMdTaskDefs(),ziorOpenNodeModel.getBooks());
			MdTaskDef selectDef=ziorOpenNodeModel.getSelectTaskDef();
			if(selectDef!=null){
				for(MdTaskDef def:ziorOpenNodeModel.getUiMdTaskDefs()){
					if(def.getPk_obj().equals(selectDef.getPk_obj())){
						ziorOpenNodeModel.setSelectTaskDef(def);
						break;
					}
				}
			}
		}
		if(ziorOpenNodeModel.getUiMdTasks()!=null&&ziorOpenNodeModel.getUiMdTasks().length>0){
			TbTaskCtl.loadDimMemberValues(ziorOpenNodeModel.getUiMdTasks(),true);
		}
		 //����ά��˳���pkֵƴ�������ַ���������Ķ�Ӧ��ϵMap
	 setTreeNodeWithTask(  ziorOpenNodeModel);
	 LinkedHashMap<String, MdTask> pkstoTaskMap =getPksToTaskMap(ziorOpenNodeModel.getUiMdTasks(),ziorOpenNodeModel);;
	 ziorOpenNodeModel.setPkstoTaskMap(pkstoTaskMap);
	 if(TbParamUtil.hasSheetGroup()){
		 loadSheetGroupByMdWorkbook( ziorOpenNodeModel,true);
	 }

		return ziorOpenNodeModel;


	}
	/**
	 * ������ڵ�������������ά�Ⱥ��ˢ��
	 * @throws BusinessException
	 */
	public static ZiorOpenNodeModel refreshBySetTaskParadims(ZiorOpenNodeModel  ziorOpenNodeModel)throws BusinessException {

	 setTreeNodeWithTask(  ziorOpenNodeModel);
	 LinkedHashMap<String, MdTask> pkstoTaskMap =getPksToTaskMap(ziorOpenNodeModel.getUiMdTasks(),ziorOpenNodeModel);;
	 ziorOpenNodeModel.setPkstoTaskMap(pkstoTaskMap);
		return ziorOpenNodeModel;


	}
	public static LinkedHashMap<String, MdTask> getPksToTaskMap(MdTask[] tasks, ZiorOpenNodeModel ziorOpenNodeModel) throws BusinessException {
		LinkedHashMap<String, MdTask> pkstoTaskMap = new LinkedHashMap<String, MdTask>();
	    Map<String, LinkedHashMap<Integer, DimLevel>> paradimOrderMap =ziorOpenNodeModel.getParadimOrderMap();
		 if(paradimOrderMap==null||paradimOrderMap.size()<=0){
			 return null;
		 }
		 LinkedHashMap<Integer, DimLevel> dimOrders= paradimOrderMap.get(ITbPlanActionCode.UITREE);
		 if(dimOrders==null||dimOrders.size()<=0){
			 return null;
		 }
		 Map<String, String> treeKey = new TreeMap<String, String>();// ������TreeMap
			if(dimOrders!=null&&dimOrders.size()>0){
				for(DimLevel dl:dimOrders.values()){
					if(dl==null)continue;
					treeKey.put(dl.getObjCode(), null);
				}
			}
		if (tasks != null && tasks.length > 0) {
			StringBuffer  buf=new StringBuffer();
			for (int i=0; i<tasks.length; i++) {

		        boolean isMatch=matchTaskFilter(ziorOpenNodeModel.getTaskFilter(),tasks[i],treeKey);
		        if(!isMatch){
		        	continue;
		        }
				buf.delete(0, buf.length());
				DimSectionTuple tuple=TbTaskCtl.getTaskParadim(tasks[i]);
				int count=0;
				for(DimLevel dim:dimOrders.values()){
					count++;
					if(dim.getBusiCode().equals(IDimLevelCodeConst.ENTITY)){
						buf.append(tasks[i].getPk_dataent());
					}else{

						LevelValue value=tuple.getLevelValue(dim);
						if(value!=null)
						{
							if(dim.getPk_obj().equals(IDimLevelPKConst.MONTH)||dim.getPk_obj().equals(IDimLevelPKConst.QUARTER)){
								buf.append(value.getTreeKey()==null?value.getKey().toString()+dim.getObjName():value.getTreeKey()+dim.getObjName());
							}else{
								buf.append(value.getTreeKey()==null?value.getKey().toString():value.getTreeKey());
							}

						}else{
							if(count!=dimOrders.values().size())
							 {
								buf.append(dim.getObjName());
							 }
						}
					}

				}
				pkstoTaskMap.put(buf.toString(), tasks[i]);

			}
		}
		return pkstoTaskMap;
	}
	public static void setTreeNodeWithTask(ZiorOpenNodeModel  ziorOpenNodeModel){
		  //���ڵ��pk����������
		if( ziorOpenNodeModel.getMap()==null|| ziorOpenNodeModel.getMap().size()<=0){
			return;
		}
		  Map<String, LinkedHashMap<Integer, DimLevel>> paradomOrderMap = ziorOpenNodeModel.getParadimOrderMap();
			if(paradomOrderMap==null){
				paradomOrderMap= new HashMap<String, LinkedHashMap<Integer, DimLevel>>();
				LinkedHashMap<Integer, DimLevel> dimMap=new LinkedHashMap<Integer, DimLevel>();
				IDimManager dm=DimServiceGetter.getDimManager();
				DimLevel dimlevel = dm.getDimLevelByPK(IDimLevelPKConst.ENT);
				dimMap.put(1, dimlevel);
				paradomOrderMap.put(ITbPlanActionCode.UITREE, dimMap);
			}else{
				if(paradomOrderMap.get(ITbPlanActionCode.UITREE)==null||paradomOrderMap.get(ITbPlanActionCode.UITREE).size()<=0){
					LinkedHashMap<Integer, DimLevel> dimMap=new LinkedHashMap<Integer, DimLevel>();
					IDimManager dm=DimServiceGetter.getDimManager();
					DimLevel dimlevel=null;
					if(ziorOpenNodeModel.getPlanFrameModel().getNodeType().equals(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE)){
						//����ģ�ͽڵ���ģ�Ͳ���ά�ļ���
						 dimlevel = dm.getDimLevelByPK(IDimLevelPKConst.YEAR);
					}else{
						 dimlevel = dm.getDimLevelByPK(IDimLevelPKConst.ENT);
					}
					dimMap.put(1, dimlevel);
					paradomOrderMap.put(ITbPlanActionCode.UITREE, dimMap);
				}
			}
			ziorOpenNodeModel.setParadimOrderMap(paradomOrderMap);
			if( ziorOpenNodeModel.getMap()==null|| ziorOpenNodeModel.getMap().size()<=0){
				return;
			}
			LinkedHashMap<Integer, DimLevel> comboxValues=ziorOpenNodeModel.getParadimOrderMap().get(ITbPlanActionCode.UICOMBOX);
			LinkedHashMap<Integer, DimLevel> treeValues=ziorOpenNodeModel.getParadimOrderMap().get(ITbPlanActionCode.UITREE);
			LinkedHashMap<DimLevel, List<LevelValue>> mapAll=new  LinkedHashMap<DimLevel, List<LevelValue>>();
			if(comboxValues!=null&&comboxValues.size()>0){
				for(DimLevel dl:comboxValues.values()){
					if(dl.getPrimaryKey().equals(IDimLevelPKConst.ENT)){
						Set<DimMember> dims=ziorOpenNodeModel.getValidOrgDimMemberMap().keySet();
						if(dims==null||dims.size()<=0)
						{
							mapAll.put(dl, null);
						}
						else{

							List<LevelValue> values=new ArrayList<LevelValue>();
							for(DimMember l:dims){
								values.add(l.getLevelValue());
							}
							mapAll.put(dl, values);
						}

					}else{
						mapAll.put(dl, ziorOpenNodeModel.getMap().get(dl));
					}
				}
			}
			ziorOpenNodeModel.setMapAll(mapAll);

	}
	/**
	 * ��ѯ�����datamodel��ָ��������Ϣ����ע��Ԥ���������汾�Աȵ�����
	 * @param task
	 * @param pk_sheets
	 * @param isLoadConsistRule
	 * @param snapShotMap
	 * @param isInitFunctionText
	 * @param nodeType
	 * @param taskType
	 * @param obj   �汾�Աȵ�����
	 * @param taskExtInfoLoader  �ų�datamodel�������������
	 * @return
	 * @throws BusinessException
	 * lrx 2014-5-19 �޸Ĺ���TaskDataModel�ķ�ʽ(ʹ��ǰ̨SheetModel����)��ȥ��ԭ�нӿ��е�taskExtInfoLoader(��Ϊû����)
	 */

	public static Map<String,Object>  getTaskDataMesMap(MdTask task, String[] pk_sheets, boolean isLoadConsistRule, HashMap<String, CubeSnapShot> snapShotMap, boolean isInitFunctionText, String nodeType, Map<String,Object> paras) throws BusinessException{
		CostTime ct = new CostTime(1);
		Map<String,Object> taskDataMesModel=null;
		if (TbParamUtil.isSheetLockEnable()) {
			taskDataMesModel=TaskDataCtl.getTaskDataMap(TaskDataCtl.getTaskDataModelByUICall(task, pk_sheets, isLoadConsistRule, snapShotMap, isInitFunctionText), nodeType,paras);
			ct.printStepCost(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0565")/*@res "��Load TaskDataModel client��"*/);
		}
		else {
			taskDataMesModel=NCLocator.getInstance().lookup(IZiorFrameModelService.class).getTaskDataMap(task,pk_sheets,isLoadConsistRule,snapShotMap,isInitFunctionText, nodeType,paras);
			ct.printStepCost(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0566")/*@res "��Load TaskDataModel server��"*/);
			((TaskDataModel) taskDataMesModel.get(ITbPlanActionCode.TASKDATAMODEL)).instance();
		}
		return  taskDataMesModel;
	}
	/**
	 * Ȩ�޹��������������������ļ�����������Ϣ
	 * @param tasks
	 * @return
	 * @throws BusinessException
	 */
	public static MdTask[] getCanApproveTasks(MdTask[] tasks) throws BusinessException{
		MdTask[] taks=NCLocator.getInstance().lookup(IZiorFrameModelService.class).getCanApproveTasks(tasks);

		return taks;
	}
	/**
	 * �õ�һ�������Ӧ��Ѱ��·��
	 */
	public static  String getPatch(MdTask task, ZiorOpenNodeModel ziorOpenNodeModel) {
	    Map<String, LinkedHashMap<Integer, DimLevel>> paradimOrderMap =ziorOpenNodeModel.getParadimOrderMap();
	    StringBuffer  buf=new StringBuffer();
		 if(paradimOrderMap==null||paradimOrderMap.size()<=0){
			 return  buf.toString();
		 }
		 LinkedHashMap<Integer, DimLevel> dimTreeOrders= paradimOrderMap.get(ITbPlanActionCode.UITREE);
		 if(dimTreeOrders==null||dimTreeOrders.size()<=0){
			 return  buf.toString();
		 }
		 LinkedHashMap<Integer, DimLevel> dimComboxOrders= paradimOrderMap.get(ITbPlanActionCode.UICOMBOX);


		if (task != null ) {
				buf.delete(0, buf.length());
				DimSectionTuple tuple=TbTaskCtl.getTaskParadim(task);
				for(DimLevel dim:dimTreeOrders.values()){
					if(dim.getBusiCode().equals(IDimLevelCodeConst.ENTITY)){
						buf.append(task.getPk_dataent());
					}else{
						LevelValue value=tuple.getLevelValue(dim);
						buf.append(value.getTreeKey()==null?value.getKey().toString():value.getTreeKey());
					}
				}
		}
		return buf.toString();
	}
	/**
	 * �õ�ÿ�����û��Բ�ͬ������ģ�����õĲ���ά��ʾ���
	 * @param ziorOpenNodeModel
	 */
	 public static void loadTaskDefParadimMess( ZiorOpenNodeModel ziorOpenNodeModel){
		 String pk_user = InvocationInfoProxy.getInstance().getUserId();
		 String pk_group= InvocationInfoProxy.getInstance().getGroupId();
		 if(ziorOpenNodeModel.getSelectTaskDef()==null){
			 return ;
		 }
		 String taskdefpk=ziorOpenNodeModel.getSelectTaskDef().getPk_obj();
		 TaskLeach taskLeach=null;
				try {
					taskLeach = (TaskLeach) TbUserProfileCtl.getInstance()
							.getUserTaskFilter(ziorOpenNodeModel.getFunCode()+taskdefpk, pk_user+pk_group+taskdefpk);
				} catch (BusinessException e) {
					NtbLogger.print(e.getMessage());
				}
				if (taskLeach != null) {
					ArrayList<Object> values = (ArrayList<Object>) taskLeach
							.getAttributes(ziorOpenNodeModel.getFunCode()+taskdefpk);
					if (values != null && values.size() > 0) {
						Map <String,Object> valueMap=new HashMap<String,Object>();
						for (Object obj : values) {
							if(obj instanceof   Map){
								valueMap=(HashMap<String,Object>)obj;
								if(valueMap!=null&&valueMap.size()>0){
									Map<String, LinkedHashMap<Integer, DimLevel>> paradomOrderMap=(Map<String, LinkedHashMap<Integer, DimLevel>>) valueMap.get(ITbPlanActionCode.PARADIMVALUES);
									if(paradomOrderMap!=null&&paradomOrderMap.size()>0){
										ziorOpenNodeModel.setParadimOrderMap(paradomOrderMap);
										return;
									}
							}
						}
	             }

	 }
				}
				Map<String, LinkedHashMap<Integer, DimLevel>> paradomOrderMap = null;
				paradomOrderMap= new HashMap<String, LinkedHashMap<Integer, DimLevel>>();
				LinkedHashMap<Integer, DimLevel> treedimMap=new LinkedHashMap<Integer, DimLevel>();
				LinkedHashMap<Integer, DimLevel> comboxdimMap=new LinkedHashMap<Integer, DimLevel>();
				IDimManager dm=DimServiceGetter.getDimManager();
				ArrayList<String> unused = new ArrayList<String>();

				DimLevel dimlevel=null;
				if(ziorOpenNodeModel.getPlanFrameModel().getNodeType().equals(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE)){
					//����ģ�ͽڵ���ģ�Ͳ���ά�ļ���
					 dimlevel = dm.getDimLevelByPK(IDimLevelPKConst.YEAR);
					 unused.add(IDimLevelPKConst.YEAR);
				}else{
					 dimlevel = dm.getDimLevelByPK(IDimLevelPKConst.ENT);
					 unused.add(IDimLevelPKConst.ENT);
				}
				treedimMap.put(1, dimlevel);
				paradomOrderMap.put(ITbPlanActionCode.UITREE, treedimMap);
				DimLevel[]  dls=ziorOpenNodeModel.getSelectTaskDef().getParaDims();

				//Ĭ�ϵĲ���ά
				if(dls == null){
					try {
						TbTaskCtl.loadDetail(new MdTaskDef[]{ziorOpenNodeModel.getSelectTaskDef()});
					} catch (BusinessException e) {
						NtbLogger.print(e.getMessage());
					}
				}
				dls = ziorOpenNodeModel.getSelectTaskDef().getParaDims();
				int count =paradomOrderMap.get(ITbPlanActionCode.UITREE).size()+1;
				for (DimLevel dimLevel : dls) {
					if(ziorOpenNodeModel.getPlanFrameModel().getNodeType().equals(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE)
							&&dimLevel.getPrimaryKey().equals(IDimLevelPKConst.AIMCURR)){

						 continue;
					}
					if(unused.contains(dimLevel.getPrimaryKey())){
						continue;
					}
					comboxdimMap.put(count, dimLevel);
					count++;

				}

				paradomOrderMap.put(ITbPlanActionCode.UICOMBOX, comboxdimMap);
				ziorOpenNodeModel.setParadimOrderMap(paradomOrderMap);
	 }
	 public static void loadSheetGroupByMdWorkbook(ZiorOpenNodeModel ziorOpenNodeModel,boolean isLoadAll) throws BusinessException {
		 Map<String,Map> allTaskefGroups=null;
		 if(isLoadAll){
			 allTaskefGroups= NCLocator.getInstance().lookup(IZiorFrameModelService.class).getSheetGroupMapByPkWorkBook(ziorOpenNodeModel.getUiMdTaskDefs());
			 ziorOpenNodeModel.setAttribute(ITbPlanActionCode.ALLMDSHEETGROUPSHEETS, allTaskefGroups);
		 }

			loadMdTaskDefGroups(ziorOpenNodeModel);



		}
	 public static void loadMdTaskDefGroups(ZiorOpenNodeModel ziorOpenNodeModel) {
		 List<MdSheetGroup> lists=new ArrayList<MdSheetGroup>();
			MdSheetGroup gr=new MdSheetGroup();
			gr.setObjname(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0239")/*@res "ȫ����"*/);
			lists.add(gr);
			Map<MdSheetGroup, MdSheet[]> map=null;
			if(((Map<String,Map>)ziorOpenNodeModel.getAttribute(ITbPlanActionCode.ALLMDSHEETGROUPSHEETS))!=null&&((Map<String,Map>)ziorOpenNodeModel.getAttribute(ITbPlanActionCode.ALLMDSHEETGROUPSHEETS)).size()>0)
		    {
				map= ((Map<String,Map>)ziorOpenNodeModel.getAttribute(ITbPlanActionCode.ALLMDSHEETGROUPSHEETS)).get(ziorOpenNodeModel.getSelectTaskDef().getPk_workbook());
		    }
		 if(map!=null&&map.size()>0){
			 MdSheetGroup[] groups=map.keySet().toArray(new MdSheetGroup[0]);
			 if(groups!=null&&groups.length>0){
			    	for(MdSheetGroup op:groups){
			    		lists.add(op);
			    	}
			    }
		 }
		 ziorOpenNodeModel.setAttribute(ITbPlanActionCode.MDSHEETGROUPSHEETS, map);
		 ziorOpenNodeModel.setAttribute(ITbPlanActionCode.MDSHEETGROUP, lists.toArray(new MdSheetGroup[0]));

	}
	 /**
		 * ͨ��������taskfilter  lym
		 * @param filter
		 * @param lvs
		 */
		public static TaskFilter  getTaskFilter(TaskFilter filter,MdTask task) {
				if (task.getPk_year() != null)
					filter.setAttribute(TaskFilter.key_pk_year, task.getPk_year());
				if (task.getPk_month() != null)
					filter.setAttribute(TaskFilter.key_pk_month, task.getPk_month());
				if (task.getPk_currency() != null)
					filter.setAttribute(TaskFilter.key_pk_currency, task.getPk_currency());
				if (task.getPk_mvtype() != null)
					filter.setAttribute(TaskFilter.key_pk_mvtype, task.getPk_mvtype());
				if (task.getPk_version() != null)
					filter.setAttribute(TaskFilter.key_pk_version, task.getPk_version());
				if (task.getPk_aimcurr() != null)
					filter.setAttribute(TaskFilter.key_pk_aimcurr, task.getPk_aimcurr());
				if (task.getPk_paradims() != null)
					filter.setAttribute(TaskFilter.key_pk_paradims, task.getPk_paradims());
				return filter;
		}
		 /**
		 * ƥ�����taskfilter�Ƿ��Ǵ�������������ƴ������
		 * @param filter
		 * @param lvs
		 */
		public static boolean  matchTaskFilter(TaskFilter filter,MdTask task, Map<String, String> treeKey) {
			 if(treeKey.containsKey(IDimLevelCodeConst.CURR)&&!treeKey.containsKey(IDimLevelCodeConst.AIMCURR)){
             	if(task.getPk_currency()!=null&&!task.getPk_currency().equals(task.getPk_aimcurr())||task.getPk_currency()==null&&task.getPk_aimcurr()!=null){
             		return false;
             	}
             }
				if (filter.getAttribute(TaskFilter.key_pk_year)!=null&&!filter.getAttribute(TaskFilter.key_pk_year).equals(task.getPk_year())||filter.getAttribute(TaskFilter.key_pk_year)==null&&!treeKey.containsKey(IDimLevelCodeConst.YEAR)&&task.getPk_year()!=null)
					return false;
				if (filter.getAttribute(TaskFilter.key_pk_month)!=null&&!filter.getAttribute(TaskFilter.key_pk_month).equals(task.getPk_month())||filter.getAttribute(TaskFilter.key_pk_month)==null&&!treeKey.containsKey(IDimLevelCodeConst.MONTH)&&task.getPk_month()!=null)
					return false;
				if (filter.getAttribute(TaskFilter.key_pk_currency)!=null&&!filter.getAttribute(TaskFilter.key_pk_currency).equals(task.getPk_currency())||filter.getAttribute(TaskFilter.key_pk_currency)==null&&!treeKey.containsKey(IDimLevelCodeConst.CURR)&&task.getPk_currency()!=null)
					return false;
				if (filter.getAttribute(TaskFilter.key_pk_mvtype)!=null&&!filter.getAttribute(TaskFilter.key_pk_mvtype).equals(task.getPk_mvtype())||filter.getAttribute(TaskFilter.key_pk_mvtype)==null&&!treeKey.containsKey(IDimLevelCodeConst.MVTYPE)&&task.getPk_mvtype()!=null)
					return false;
				if (filter.getAttribute(TaskFilter.key_pk_version)!=null&&!filter.getAttribute(TaskFilter.key_pk_version).equals(task.getPk_version())||filter.getAttribute(TaskFilter.key_pk_version)==null&&!treeKey.containsKey(IDimLevelCodeConst.VERSION)&&task.getPk_version()!=null)
					return false;
				if (filter.getAttribute(TaskFilter.key_pk_aimcurr)!=null&&!filter.getAttribute(TaskFilter.key_pk_aimcurr).equals(task.getPk_aimcurr())||filter.getAttribute(TaskFilter.key_pk_aimcurr)==null&&!treeKey.containsKey(IDimLevelCodeConst.CURR)&&task.getPk_aimcurr()!=null)
					return false;
				if (filter.getAttribute(TaskFilter.key_pk_paradims)!=null&&task.getPk_paradims()!=null){
					String[] strs=new String[]{filter.getAttribute(TaskFilter.key_pk_paradims)==null?null:filter.getAttribute(TaskFilter.key_pk_paradims).toString(),task.getPk_paradims()};
					boolean isMatch=compareParadims(strs,treeKey);
					return isMatch;
				}else  if(filter.getAttribute(TaskFilter.key_pk_paradims)==null&&task.getPk_paradims()!=null){
					String content = task.getPk_paradims().trim().substring(1, task.getPk_paradims().length() - 1);
					String[] strs=content.split(",");
					Map<String, String> paradimKey = new TreeMap<String, String>();// ������TreeMap
					for (String item : strs) {
						String[] kv = item.split("=");
						paradimKey.put(kv[0].trim(), kv[1].trim());
					}

					if(!treeKey.keySet().containsAll(paradimKey.keySet()))
				  	return false;
				}

				return true;
		}
		private static boolean compareParadims(String[] strs,Map<String, String> treeKey) {

			ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>(strs.length);
			for (String line : strs) {
				if(line==null){
					continue;
				}
				String content = line.trim().substring(1, line.length() - 1);
				String[] entrys = content.split(",");
				Map<String, String> m = new TreeMap<String, String>();// ������TreeMap
				list.add(m);
				for (String item : entrys) {
					String[] kv = item.split("=");
					if(!treeKey.keySet().contains(kv[0].trim()))
					 {
						m.put(kv[0].trim(), kv[1].trim());
					 }
				}
			}

			for (int i = 0; i < strs.length; i++) {
				for (int j = i + 1; j < strs.length; j++) {
					String m1 = list.get(i).toString().substring(1, list.get(i).toString().length() - 1);
					String m2 = list.get(j).toString().substring(1, list.get(j).toString().length() - 1);

					if (m1.length() == m2.length() && m1.equals(m2)) {
						return true;
					}
				}
			}
			return false;
		}
	/**
	 * �����������õ��п��и�
	 * @param task
	 */
    public static Map<String, Map> loadRowColSize(MdTask task) {
    	    if(task==null){
    	    	return  null;
    	    }
			Map<String,String>map=new HashMap<String,String>();
			map.put(ProfileSchema.USER, InvocationInfoProxy.getInstance().getUserId());
			map.put(ProfileSchema.TASK, task.getPrimaryKey());
			map.put(ProfileSchema.NAME, ITbPlanActionCode.code_SaveRowColSize);
			try {
				Map<String, Map> sizeMap=  TbUserProfileCtl.getInstance().getZiorRowColSize(map);

				return sizeMap;
			} catch (BusinessException e1) {
				NtbLogger.printException(e1);
			}
			return null;

	}

    /**
	 * ��ȡtaskde�ɼ�sheet��Χ liyingm+
	 *  User2SheetCvsTools.getPkSheets(pk_user,pk_group,pk_��������,)��
	 * �����û�����ְ��ı�pk�����ְ�����ñ��н���Ļ�pk�п����ظ���
	 * ��������TaskDataModel�ı�pk�ظ�Ҳû�й�ϵ����û�п����������򷵻�null
	 * �ɼ���Ϊ�գ��������ɼ������Χ
	 * isApp=trueΪ��������Χ�����������ڵ�����
	 * @return
     * @throws BusinessException
	 */
	public static String[] getTaskValidLookPkSheets(MdTask task, String[] publicnodeLookSheets,String sheetGroupName,boolean isApp,String pk_org,MdTaskDef def) throws BusinessException {
	  return	NCLocator.getInstance().lookup(IZiorFrameModelService.class).getTaskValidLookPkSheets( task, publicnodeLookSheets, sheetGroupName, isApp, pk_org, def);
	}

	/**
	 *��������������ݿ����:(1)ִ�й�ʽ  ��2��ģ�����ݱ���
	 * @param nodetype  �ڵ�����
	 * @param taskDataModel   ����ģ��
	 * @param task
	 * @return
	 * @throws BusinessException
	 */
	public static Map<String,Object> saveTaskDataMesMap(String nodetype,TaskDataModel taskDataModel,MdTask task) throws BusinessException{
		//lys mod ��instance��������Ϊ����ǰ̨��Ҫ���ز���ά 2015-12-17
		Map<String,Object> taskDataMesModel = NCLocator.getInstance().lookup(IZiorFrameModelService.class).saveTaskDataMesMap(nodetype, taskDataModel, task);
		((TaskDataModel) taskDataMesModel.get(ITbPlanActionCode.TASKDATAMODEL)).instance();
		return taskDataMesModel;
	}
	public static TbbFormulaExecuteLogs executeFmlAndRule(String nodetype,TaskDataModel taskDataModel,MdTask task) throws BusinessException {
		taskDataModel.instance();
		if(task==null||nodetype==null||taskDataModel==null){
			return new TbbFormulaExecuteLogs(); 
		}
		List<BusiRuleVO> defaultRules = null;
		// ����ִ�� �������
		if (ITbPlanActionCode.COM_NODETYPE.equals(nodetype)
				|| ITbPlanActionCode.ADJUSTAPPROVE_NODETYPE
						.equals(nodetype)
				|| ITbPlanActionCode.DIRECTADJUST_NODETYPE   //ֱ�ӵ���
						.equals(nodetype)
				|| ITbPlanActionCode.TABLEOFTOP_NODETYPE
						.equals(nodetype)||ITbPlanActionCode.APPROVE_NODETYPE.equals(nodetype)
						||ITbPlanActionCode.PREAPPROVE_NODETYPE.equals(nodetype)) {
			defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(
					task.getPk_taskdef(),
					NTBActionEnum.CALACTION.toCodeString());
			// �ճ�ִ��ȡ������
		} else if (ITbPlanActionCode.DAILY_NODETYPE.equals(nodetype)) {
			defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(
					task.getPk_taskdef(),
					NTBActionEnum.GETDATAACTION.toCodeString());
		}
		if (defaultRules != null && defaultRules.size() > 0) {
			RuleExecuteHelper.executeWorkBookRule(taskDataModel, defaultRules);
		}
		TaskDataModelAction action = new TaskDataModelAction(taskDataModel);
		//��Ӱ��ձ����䷶Χִ�й��� by:wangzhqa  2014-3-18
		
		
//		TaskExecuteHelper.getExecuteSheetList(taskDataModel.getMdTask(), taskDataModel);
		
		return action.executeWorkBook();
	}
	/**
	 * �����������ע��Ϣ
	 * @param primaryKey   �����pk
	 * @param olapInfoList  ��ע��Ϣ
	 * @throws BusinessException
	 */
	public static void saveExOlapInfoSet(String primaryKey,
			List<ExOlapInfoSet> olapInfoList) throws BusinessException {
		NCLocator.getInstance().lookup(IZiorFrameModelService.class).saveExOlapInfoSet(primaryKey, olapInfoList);
	}
}