
package nc.ui.tb.zior.pluginaction.approve;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.mdm.permission.INtbPerm;
import nc.itf.mdm.permission.INtbPermConst;
import nc.itf.tb.limit.permission.IDataPermGetter;
import nc.itf.tb.rule.parser.IRuleExecute;
import nc.itf.tb.task.ITaskRuleExecuteAdapter;
import nc.ms.mdm.limit.DataPermGetterUtil;
import nc.ms.tb.event.TaskChangeEvent;
import nc.ms.tb.ext.plan.TbCompliePlanConst;
import nc.ms.tb.formula.core.cutcube.ICutCube;
import nc.ms.tb.formula.excel.core.IWorkSheet;
import nc.ms.tb.plan.GZWCoverServiceGetter;
import nc.ms.tb.pub.TbParamUtil;
import nc.ms.tb.rule.RuleManager;
import nc.ms.tb.rule.RuleServiceGetter;
import nc.ms.tb.task.RuleExecuteHelper;
import nc.ms.tb.task.TaskActionCtl;
import nc.ms.tb.task.TaskExecuteHelper;
import nc.ms.tb.task.TbTaskCtl;
import nc.ms.tb.task.TbTaskServiceGetter;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ms.tb.task.data.TaskDataModelAction;
import nc.ms.tb.task.filter.WorkBookSheetCellFilter;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ms.tb.zior.vo.ZiorCoverVO;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIDialog;
import nc.ui.tb.plan.action.ApprovePlanTools;
import nc.ui.tb.zior.TBSheetViewer;
import nc.ui.tb.zior.TbPlanContext;
import nc.ui.tb.zior.pluginaction.AbstractTbRepPluginAction;
import nc.ui.tb.zior.pluginaction.TbPluginActionDescriptor;
import nc.ui.tb.zior.pluginaction.approve.dialog.ApprovePlanDlg;
import nc.ui.tb.zior.pluginaction.check.CheckExtraRuleHelper;
import nc.ui.tb.zior.pluginaction.check.PlanCheckResultViewer;
import nc.ui.tb.zior.pluginaction.sum.UserInfo;
import nc.view.tb.plan.TbPlanListPanel;
import nc.vo.mdm.cube.DimSectionTuple;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.tb.form.excel.ExSheetCheckFml;
import nc.vo.tb.ntbenum.NTBActionEnum;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.rule.BusiRuleVO;
import nc.vo.tb.rule.CheckRuleResultVO;
import nc.vo.tb.task.ITaskAction;
import nc.vo.tb.task.ITaskStatus;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.TaskConst;

import com.ufida.zior.docking.core.state.DockingState;
import com.ufida.zior.plugin.PluginKeys.XPOINT;
import com.ufida.zior.plugin.event.PluginActionEvent;
import com.ufida.zior.view.Viewer;
import com.ufsoft.table.CellPosition;

/*
 * 审批节点下的上报功能
 *
 */
public class PluginAction_Submit extends AbstractTbRepPluginAction {
	private UIDialog dlg;
	private MdTask task = null;
	private String id = "tb.report.check.view";
	private int errorCount = 0;
	private int warningCount=0;
	private ZiorCoverVO  ziorCoverVO=null;
	private HashMap<TBSheetViewer, List<ExSheetCheckFml>> sheetCheckFmlMap = new HashMap<TBSheetViewer, List<ExSheetCheckFml>>();

	public PluginAction_Submit(String name, String code) {
		super(name, code);
	}

	public PluginAction_Submit(String name, String code, String tooltip) {
		super(name, code, tooltip);
	}

	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		// TODO Auto-generated method stub
		TbPluginActionDescriptor tad = new TbPluginActionDescriptor();
		tad.setName(TaskConst.getTaskActionShowName(ITaskAction.PREMOTE));
		//tad.setName(TbActionName.getName_Approve);
		tad.setGroupPaths(TbActionName.getName_NApprove());
		tad.setExtensionPoints(new XPOINT[] { XPOINT.MENU, XPOINT.TOOLBAR });
		tad.setIcon(ITbPlanActionCode.SUBMIT_ICON);
		return tad;
	}

	@Override
	public void actionPerformed(ActionEvent actionevent)
			throws BusinessException {
		MdTask[] tasks = getContext().getTasks();/*
												 * taskList.toArray(new
												 * MdTask[0]);
												 */
		if (tasks == null || tasks.length == 0) {
			throw new BusinessException(NCLangRes.getInstance().getStrByID(
					"tbb_plan", "01812pln_000479")/* 请检查所选任务 */);
		}
		if (tasks != null && tasks.length > 0)
			task = tasks[0];
		else
			return;
		try {
			if(ITaskStatus.STARTED.equals(task.getPlanstatus())){
				
				
				UserLoginVO userLoginVO = ApprovePlanTools.getCompleteUser(getMdTask());
				
//				processAction(userLoginVO, new MdTask[]{task}, ITaskAction.COMPILE, null);
				TaskActionCtl.processAction(userLoginVO, tasks, ITaskAction.COMPILE, null);
				task.setPlanstatus(ITaskStatus.COMPILING);
				getContext().setTaskStatus(task.getPlanstatus());
//				return ;
			}
			checkTaskPrv(tasks[0], UserInfo.getUserLoginVO(), getContext()
					.getNodeType());
		} catch (BusinessException be) {
			MessageDialog.showErrorDlg(
					this.getMainboard(),
					NCLangRes.getInstance().getStrByID("tbb_plan",
							"01812pln_000480")/* 提示 */, be.getMessage());
			return;
		}
//       //***********创维提交时执行规则和excel计算，创维专项
//		executeFmlAndRule();
//	   //************
		errorCount = 0;
		// 上报之前检验所选任务是否需要审核
		if (TbParamUtil.isCheckBeforeSubmitTask()) {
			errorCount = check();
		}
		if (errorCount > 0) {
			MessageDialog.showErrorDlg(
					getMainboard(),
					NCLangRes.getInstance().getStrByID("tbb_plan",
							"01812pln_000095")/* 提示： */,
					NCLangRes.getInstance().getStrByID("tbb_plan",
							"01812pln_000096")/* \n审核不通过，不允许上报！ */);
			return;
		}
		UserLoginVO uservo = ApprovePlanTools.getCompleteUser(task);
		// 检验所选任务状态是否符合提交要求：有二级审批的要预审批通过，没有的需要调整和编制中的任务才可以提交
		String action=ApprovePlanTools.getAction(task, "TaskActionFunctionPromote",getContext().getIsHasLocalApproval());
		tasks = ApprovePlanTools.getAskTasks(tasks,action,getContext().getIsHasLocalApproval());
		if (tasks == null || tasks.length == 0){
			if(getContext().getIsHasLocalApproval()){
				throw new BusinessException(NCLangRes.getInstance().getStrByID(
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0002")/*@res "提示"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0278")/*@res "未提交成功，所选任务状态允许提交！"*/)/* 请检查所选任务 的状态，只有调整或编制才可以通过 *//*
																				 * 请检查所选任务
																				 * 的状态
																				 * ，
																				 * 只有调整或编制才可以通过
																				 */);
			}
			else{
				throw new BusinessException(NCLangRes.getInstance().getStrByID(
						"tbb_plan", "01812pln_000097")/* 请检查所选任务 的状态，只有调整或编制才可以通过 *//*
																				 * 请检查所选任务
																				 * 的状态
																				 * ，
																				 * 只有调整或编制才可以通过
																				 */);
			}
		}


		// 弹出上报意见对话框 非日常节点才弹出上报意见框
		String appnote = null;
		if (!ITbPlanActionCode.DAILY_NODETYPE
				.equals(getContext().getNodeType())) {
			//在ApprovePlanDlg类构造函数中加入额外参数，用来区分上报意见框（0）和退回意见框（1）
			ApprovePlanDlg planDlg = new ApprovePlanDlg(this.getMainboard(),0);

			if (UIDialog.ID_OK != planDlg.showModal())
				return;
			appnote = planDlg.getAppNote();
//			if("默认意见为'无'".equals(appnote)){
//				appnote = null;
//			}
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(ApprovePlanTools.appNoteRemark, appnote);
		processAction(uservo, tasks, ITaskAction.PREMOTE, params);
		Viewer refresh = getCurrentView();
		getContext().setTasks(tasks);
		if(refresh!=null){
			if(refresh instanceof TbPlanListPanel){
				((TbPlanListPanel)refresh).refreshZiorTask();
			}else if(refresh instanceof TBSheetViewer){
				//改变树的渲染色
				refeshTreeRender(tasks);
			}
		}
		TaskChangeEvent taskChangeEvent = new TaskChangeEvent(getCurrentView(),
				1);
		this.getMainboard().getEventManager().dispatch(taskChangeEvent);
		PluginActionEvent pluginActionEvent = new PluginActionEvent(
				getCurrentView(), 1);
		getCurrentView().getMainboard().getEventManager()
				.dispatch(pluginActionEvent);
	}

	private void checkTaskPrv(MdTask task, UserLoginVO userVo,
			String key_nodeType) throws BusinessException {
		if (task == null) {
			return;
		}
		int type = INtbPermConst.TYPE_BUDGETTASK;// 预算任务资源实体类型
		String[] opercodes = new String[] { INtbPermConst.RES_OPERCODE_PLAN };
		if (ITbPlanActionCode.DIRECTADJUST_NODETYPE.equals(key_nodeType)) {
			opercodes = new String[] { INtbPermConst.RES_OPERCODE_ADJUST };
		}
		String pk_group = WorkbenchEnvironment.getInstance().getGroupVO().getPrimaryKey();
		List<MdTask> list = new ArrayList<MdTask>();
		list.add(task);
		Set<MdTask> tasks_filtered = filterObjectsByPrv(list, type, opercodes,
				userVo.getPk_user(), pk_group);
		if (tasks_filtered == null || tasks_filtered.size() != list.size()) {
			throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000706")/*没有上报权限*/);
		}
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

	public void processAction(UserLoginVO userVo, MdTask[] tasks,
			String action, HashMap<String, Object> params)
			throws BusinessException {
		TaskActionCtl.processAction(userVo, tasks, action, params);
	}

	@Override
	public boolean isActionEnabled() {
		TbPlanContext tbPlanContext = getContext();
		if (tbPlanContext != null) {
			if (tbPlanContext.getTasks() != null) {
				MdTask[] tasks = tbPlanContext.getTasks();
				// 判断是否选中任务且选中一个
				if (tasks != null && tasks.length > 0) {
					ArrayList<String> status = new ArrayList<String>();
						status.add(ITaskStatus.LOCAL_APPROVE_PASS);
						status.add(ITaskStatus.COMPILING);
						status.add(ITaskStatus.ADJUSTING);
					for (MdTask task : tasks) {
						if (!status.contains(task.getPlanstatus())
								|| (tbPlanContext.getComplieStatus() == TbCompliePlanConst.COM_MODE_TASKEDIT)) {
							
							//央客:王志强,浏览状合并任务
							if(tbPlanContext.getComplieStatus() == TbCompliePlanConst.COM_MODE_TASKVIEW){
								if(task.getObjname().contains("合并")&&ITaskStatus.STARTED.equals(task.getPlanstatus())){
									return true;
								}
							}
							
							//end
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	public int check() {
		sheetCheckFmlMap.clear();
		// 把当前的modelset到数据datamodel中
		if(this.getCurrentViewer()==null)
		{
			return 0;
		}
		this.getCurrentViewer().getViewManager().setAllViewsDataToTsModel();
		List<TBSheetViewer> siewers = getCurrentViewer().getViewManager()
				.getSheetViewList();
		if (siewers == null || siewers.size() < 0)
			return 0;
		List<IWorkSheet> sheets = new ArrayList<IWorkSheet>();
		for (TBSheetViewer sheetView : siewers) {
			//sheetView.getTsDataModel().getSheetCheckFml().clear();
			sheets.add(sheetView.getTsDataModel());
		}
		TaskDataModel taskDataModel = getCurrentViewer().getTsDataModel()
				.getParentModel();
		TaskDataModelAction action = new TaskDataModelAction(taskDataModel);// 构造函数需要IWorkBook,就是当前编制表单的TaskDataModel
		action.checkWorkBook(sheets);// 调用,Excel审核公式执行.
		// 规则审核的信息处理
		List<CheckRuleResultVO> checkRuleResultVOList = new ArrayList<CheckRuleResultVO>();
		// 得到当前任务的默认规则
		List<BusiRuleVO> defaultRules = null;
		try {
			defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(
					task.getPk_taskdef(),
					NTBActionEnum.CHECKACTION.toCodeString());
			if (defaultRules != null && !defaultRules.isEmpty()) {
				checkRuleResultVOList = exeCheckRule(task.getPrimaryKey(),
						defaultRules);

				List<CheckRuleResultVO> filters = new ArrayList<CheckRuleResultVO>();
				WorkBookSheetCellFilter filter = WorkBookSheetCellFilter.getInstance(taskDataModel);
				for(CheckRuleResultVO vo:checkRuleResultVOList){
					if(filter.convertCell(vo.getCubeCode(), vo.getCheckDimVector())){
						filters.add(vo);
					}
				}
				checkRuleResultVOList = filters;

			}
		} catch (BusinessException e1) {
			NtbLogger.print(e1.getMessage());
		}
		 ziorCoverVO=getCoverMes(getContext().getTasks()[0]);
		// 审核信息放到taskDataModel的每个TaskSheetDataModel中的sheetCheckFml属性里.
		for (TBSheetViewer sheetView : siewers) {
			excuteCheck( sheetView);
			List<ExSheetCheckFml> checksFmls = sheetView.getTsDataModel()
					.getSheetCheckFml();
			if (checksFmls != null && checksFmls.size() > 0) {
				sheetCheckFmlMap.put(sheetView, checksFmls);
					excuteCheck( sheetView);
			}
		}
		// 如果编辑态的审核，要把审核的excel从datamodel刷新到前台
		int isEditing = getContext().getComplieStatus();
		if (TbCompliePlanConst.COM_MODE_TASKEDIT == isEditing) {
			this.getCurrentViewer().getViewManager()
					.setAllViewsTsModelToCellsModel();
		}
		DockingState info = getMainboard().getPerspectiveManager()
				.getDockingState(id);
		if (info == null) {
			throw new IllegalArgumentException(id + " is not exist.");
		}

		// 审核后的提示信息,警告类型的让上报，但是要显示

		if (sheetCheckFmlMap != null && sheetCheckFmlMap.size() > 0) {
			List<ExSheetCheckFml> sCheckFml = null;
			for (TBSheetViewer vie : siewers) {
				sCheckFml = sheetCheckFmlMap.get(vie);
				if (sCheckFml == null)
					continue;
				for (int i = 0; i < sCheckFml.size(); i++) {
//					boolean isPass = sCheckFml.get(i).isPassCheck();
					if(sCheckFml.get(i).getFmlType()
							.equals(ITbPlanActionCode.FMLTYPE_WARNING)&&sCheckFml.get(i).isPassCheck()){
						warningCount = warningCount + 1;
					}
				   if (sCheckFml.get(i).getFmlType()
								.equals(ITbPlanActionCode.TBCHECKERROR)&&!sCheckFml.get(i).isPassCheck()) {
					     errorCount = errorCount + 1;

					}


				}

			}
		}
		if (checkRuleResultVOList != null && !checkRuleResultVOList.isEmpty()) {
			for(CheckRuleResultVO vo:checkRuleResultVOList){
				if(vo.getMesType()==1){
					errorCount = errorCount + 1;
				}
				if(vo.getMesType()==0){
					warningCount = warningCount + 1;
				}
			}

		}

		// 打开审核结果的面板
		if ((errorCount > 0||warningCount>0)&&this.getCurrentViewer()!=null) {
			this.getCurrentViewer().getViewManager()
					.openView(info, getMainboard(), true);
			// 规则审核的信息传到结果面板中
			((PlanCheckResultViewer) getMainboard().getView(
					ITbPlanActionCode.Id_CHECKVIEW)).setCheckData(
					checkRuleResultVOList, siewers, sheetCheckFmlMap);

		}
		return errorCount;
	}
	/**
	  * * 设计好的封面信息展现在任务上
	 */
	   private ZiorCoverVO getCoverMes(MdTask task) {
		   TBSheetViewer coverViewer=  this.getCurrentViewer().getViewManager().getCoverTbSheetViewer();
		   if(coverViewer==null){
			   return null;
		   }
		    try {
		    	ziorCoverVO = GZWCoverServiceGetter.getCoverService().queryFrontCoverDataByWhere("pk_task='"+task.getPrimaryKey()+"'");
		    	return ziorCoverVO;
			} catch (BusinessException e) {
				NtbLogger.error(e.getMessage());
			}
				return null;
	  }
	   private void excuteCheck(TBSheetViewer sheetView) {
			if(sheetView.getTsDataModel().getName().equals(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0271")/*@res "03固定资产投资预算表"*/)){
				CellPosition pos=CellPosition.getInstance("K44");
				CheckExtraRuleHelper helper=new CheckExtraRuleHelper(sheetView,ziorCoverVO,getContext().getTasks()[0]);
				List<ExSheetCheckFml> fmls=	helper.Excute03CheckFunc(sheetView,pos);
				List<ExSheetCheckFml> fmlsDta=	helper.Excute03CheckFuncDate(sheetView,pos);
				List<ExSheetCheckFml> fmlss=new ArrayList<ExSheetCheckFml>();
				if(sheetView.getTsDataModel().getSheetCheckFml()!=null&&sheetView.getTsDataModel().getSheetCheckFml().size()>0){
					for(ExSheetCheckFml fl:sheetView.getTsDataModel().getSheetCheckFml()){
						if(fl.isCanClear()){
							fmlss.add(fl);
						}else{
							continue;
						}
					}
					if(fmlss.size()>0){
						sheetView.getTsDataModel().getSheetCheckFml().removeAll(fmlss);
					}
				}
				if(fmls!=null&&fmls.size()>0)
				{
					sheetView.getTsDataModel().getSheetCheckFml().addAll(fmls);
				}
				if(fmlsDta!=null&&fmlsDta.size()>0)
				{
					sheetView.getTsDataModel().getSheetCheckFml().addAll(fmlsDta);
				}
			}else if(sheetView.getTsDataModel().getName().equals(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0272")/*@res "09利润预算表"*/)){
				if(ziorCoverVO!=null){
					CellPosition pos=CellPosition.getInstance("E29");
					CheckExtraRuleHelper helper=new CheckExtraRuleHelper(sheetView,ziorCoverVO,getContext().getTasks()[0]);
					List<ExSheetCheckFml> fmls=	helper.Excute09CheckFunc(sheetView,pos);
					List<ExSheetCheckFml> fmlss=new ArrayList<ExSheetCheckFml>();
					if(sheetView.getTsDataModel().getSheetCheckFml()!=null&&sheetView.getTsDataModel().getSheetCheckFml().size()>0){
						for(ExSheetCheckFml fl:sheetView.getTsDataModel().getSheetCheckFml()){
							if(fl.isCanClear()){
								fmlss.add(fl);
							}else{
								continue;
							}
						}
						if(fmlss.size()>0){
							sheetView.getTsDataModel().getSheetCheckFml().removeAll(fmlss);
						}
					}
					if(fmls!=null&&fmls.size()>0)
					{
						sheetView.getTsDataModel().getSheetCheckFml().addAll(fmls);
					}
				}

			}else if(sheetView.getTsDataModel().getName().equals(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0273")/*@res "11资产负债预算表"*/)){
				if(ziorCoverVO!=null){
					CellPosition pos=CellPosition.getInstance("K44");
					CheckExtraRuleHelper helper=new CheckExtraRuleHelper(sheetView,ziorCoverVO,getContext().getTasks()[0]);
					List<ExSheetCheckFml> fmls=	helper.Excute09CheckFunc(sheetView,pos);
					List<ExSheetCheckFml> fmlss=new ArrayList<ExSheetCheckFml>();
					if(sheetView.getTsDataModel().getSheetCheckFml()!=null&&sheetView.getTsDataModel().getSheetCheckFml().size()>0){
						for(ExSheetCheckFml fl:sheetView.getTsDataModel().getSheetCheckFml()){
							if(fl.isCanClear()){
								fmlss.add(fl);
							}else{
								continue;
							}
						}
						if(fmlss.size()>0){
							sheetView.getTsDataModel().getSheetCheckFml().removeAll(fmlss);
						}
					}
					if(fmls!=null&&fmls.size()>0)
					{
						sheetView.getTsDataModel().getSheetCheckFml().addAll(fmls);
					}
				}

			}else if(sheetView.getTsDataModel().getName().equals(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0274")/*@res "12对外捐赠支出预算表"*/)){
				CellPosition pos=CellPosition.getInstance("H8");
				CheckExtraRuleHelper helper=new CheckExtraRuleHelper(sheetView,ziorCoverVO,getContext().getTasks()[0]);
				List<ExSheetCheckFml> fmls=	helper.Excute12CheckFunc(sheetView,pos);
				List<ExSheetCheckFml> fmlss=new ArrayList<ExSheetCheckFml>();
				if(sheetView.getTsDataModel().getSheetCheckFml()!=null&&sheetView.getTsDataModel().getSheetCheckFml().size()>0){
					for(ExSheetCheckFml fl:sheetView.getTsDataModel().getSheetCheckFml()){
						if(fl.isCanClear()){
							fmlss.add(fl);
						}else{
							continue;
						}
					}
					if(fmlss.size()>0){
						sheetView.getTsDataModel().getSheetCheckFml().removeAll(fmlss);
					}
				}
				if(fmls!=null&&fmls.size()>0)
				{
					sheetView.getTsDataModel().getSheetCheckFml().addAll(fmls);
				}
			}

	}
	private List<CheckRuleResultVO> exeCheckRule(String pk_task,
			List<BusiRuleVO> rules) throws BusinessException {
		TaskDataModel book = TaskDataCtl.getTaskDataModel(pk_task, null, false,
				null);
		IRuleExecute service = RuleServiceGetter.getRuleExecuteService();
		DimSectionTuple defaultSectionTuple = TbTaskCtl.getTaskParadim(book
				.getMdTask());
		List<CheckRuleResultVO> vos = service.executeWorkBookCheckRule(
				defaultSectionTuple, rules, book,
				ICutCube.TYPE_WORKBOOK_DEFAULT, false);
		return vos;
	}
	
	private void executeFmlAndRule() throws BusinessException {
		
		
		ITaskRuleExecuteAdapter adapter = TbTaskServiceGetter.getTaskRuleExecuteAdapter();
		adapter.executeRuleAndFormula(new MdTask[]{getMdTask()},getContext().getNodeType());
		
	}
}
