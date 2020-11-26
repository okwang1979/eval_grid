package nc.ui.tb.zior.pluginaction.approve;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nc.bs.ml.NCLangResOnserver;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.mdm.permission.INtbPerm;
import nc.itf.mdm.permission.INtbPermConst;
import nc.itf.tb.limit.permission.IDataPermGetter;
import nc.ms.mdm.limit.DataPermGetterUtil;
import nc.ms.tb.event.TaskChangeEvent;
import nc.ms.tb.ext.plan.TbCompliePlanConst;
import nc.ms.tb.task.TaskActionCtl;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.tb.plan.action.ApprovePlanTools;
import nc.ui.tb.zior.TBSheetViewer;
import nc.ui.tb.zior.TbPlanContext;
import nc.ui.tb.zior.pluginaction.AbstractTbRepPluginAction;
import nc.ui.tb.zior.pluginaction.TbPluginActionDescriptor;
import nc.view.tb.plan.TbPlanListPanel;
import nc.vo.pub.BusinessException;
import nc.vo.sm.UserVO;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.task.ITaskAction;
import nc.vo.tb.task.ITaskStatus;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.TaskConst;

import com.ufida.zior.plugin.PluginKeys.XPOINT;
import com.ufida.zior.plugin.event.PluginActionEvent;
import com.ufida.zior.view.Viewer;

public class Pluginaction_LocalApproval extends AbstractTbRepPluginAction {
	/*
	 * 审批节点下的审批功能
	 */
	public Pluginaction_LocalApproval(String name, String code) {
		super(name, code);
	}

	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		TbPluginActionDescriptor tbpad = new TbPluginActionDescriptor();
		tbpad.setName(TbActionName.getName_preApprove());
		tbpad.setGroupPaths(TbActionName.getName_NApprove());
		tbpad.setExtensionPoints(new XPOINT[] { XPOINT.MENU, XPOINT.TOOLBAR });
		tbpad.setIcon(ITbPlanActionCode.APPROVAL_ICON);
		return tbpad;
	}

	@Override
	public void actionPerformed(ActionEvent actionevent)
			throws BusinessException {
		/* 从上下文中获取任务 */
		MdTask[] tasks = null;
		tasks = getContext().getTasks();
		/*定义一个标志位，用来记录是否进行刷新*/
		boolean isRefreshFlag = false;
		/* 检验是否选中任务 */
		if (tasks == null || tasks.length <= 0) {
			MessageDialog.showWarningDlg(
					getMainboard(),
					NCLangRes.getInstance().getStrByID("tbb_plan",
							"01812pln_000411")/* 是否选中任务 */,
					NCLangRes.getInstance().getStrByID("tbb_plan",
							"01812pln_000479")/* 请检查所选任务 */);
			return;
		}

		UserVO user = WorkbenchEnvironment.getInstance().getLoginUser();

		checkTaskPrv(tasks, user.getPrimaryKey(), WorkbenchEnvironment.getInstance().getGroupVO().getPrimaryKey(),
				getContext().getNodeType());

		String warning = "";
		/* 检验哪几种任务不可以审批 */
		for (MdTask task : tasks) {
			UserLoginVO userVo = new UserLoginVO();
			userVo.setPk_group(WorkbenchEnvironment.getInstance().getGroupVO()
					.getPrimaryKey());
			userVo.setPk_org(task.getPk_planent());
			userVo.setPk_user(WorkbenchEnvironment.getInstance().getLoginUser()
					.getPrimaryKey());
			if (ApprovePlanTools.isLockTask(task)) {
				warning = NCLangRes.getInstance().getStrByID("tbb_plan",
						"01812pln_000463", null,
						new String[] { task.getObjname() })/* 任务{0}已经冻结,不可再审批 */;
				break;
			} else if (ApprovePlanTools.isLocalAppPassTask(task)) {
				warning = NCLangRes.getInstance().getStrByID(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0002")/*@res "提示"*/,nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0269")/*@res "审批通过,不可再审批"*/, null,new String[] { task.getObjname() })/* 任务{0}已经本级审批通过,不可再审批 */;
				break;
			}else if (!TaskActionCtl.checkPrevStatus(userVo, task,
					ITaskAction.LOCAL_APPROVE_PASS)) {
				warning = NCLangResOnserver.getInstance().getStrByID(
						"tbb_task",
						"01812tsk_000069",
						null,
						new String[] {(String) task.getObjname(),TaskConst.getTaskStatusShowName(task
										.getPlanstatus()),TaskConst.getTaskActionShowName(ITaskAction.LOCAL_APPROVE_PASS) })/*
																		 * 计划 {
																		 * 0 }
																		 * 状态 {
																		 * 1 }
																		 * 与动作 {
																		 * 2 }
																		 * 不符 ，
																		 * 无法操作
																		 */;
				break;
			}
		}
		if (!warning.isEmpty()) {
			MessageDialog.showHintDlg(getMainboard(), "", warning);
			return;
		}

//		if (MessageDialog.showYesNoDlg(
//				this.getMainboard(),
//				NCLangRes.getInstance().getStrByID("tbb_plan",
//						"01812pln_000465")/* 询问 */, NCLangRes.getInstance()
//						.getStrByID("tbb_plan", "01812pln_000408")/*
//																 * 审批后任务无法退回,
//																 * 请确认是否继续审批?
//																 */) != MessageDialog.ID_YES) {
//			return;
//		}

		/* 对任务进行审批操作 */
		isRefreshFlag = TbAppTaskUiUtil.approvePlan(tasks, this.getMainboard()/*
															 * null ui.
															 * getAbstractFuncletUI
															 * ()
															 */,TbActionName.getName_preApprove());

		MdTask[] taks=getHavedDoMdTasks(tasks);
		if(taks==null||taks.length<=0){
			return;
		}
		getContext().setTasks(taks);
		Viewer refresh = getCurrentView();
		if (refresh != null && isRefreshFlag) {
			if (refresh instanceof TbPlanListPanel) {
				((TbPlanListPanel) refresh).refreshZiorTask();
			} else if (refresh instanceof TBSheetViewer) {
				refeshTreeRender(taks);
//				((TBSheetViewer)refresh).getViewManager().refresh(getCurrentViewer());
//				 refreshTree();

			}
			TaskChangeEvent taskChangeEvent = new TaskChangeEvent(getCurrentView(),
					1);
			this.getMainboard().getEventManager().dispatch(taskChangeEvent);
			PluginActionEvent pluginActionEvent = new PluginActionEvent(
    				getCurrentView(), 1);
    		getCurrentView().getMainboard().getEventManager()
					.dispatch(pluginActionEvent);
		}
	}

	private void checkTaskPrv(MdTask[] diftask, String user, String pk_group,
			String key_nodeType) throws BusinessException {
		int type = INtbPermConst.TYPE_BUDGETTASK;// 预算任务资源实体类型
		String[] opercodes = new String[] { INtbPermConst.RES_OPERCODE_PREAPPROVE };
		List<MdTask> list = new ArrayList<MdTask>();
		for (MdTask task : diftask) {
			list.add(task);
		}
		Set<MdTask> tasks_filtered = filterObjectsByPrv(list, type, opercodes,
				user, pk_group);
		if (tasks_filtered == null || tasks_filtered.size() != list.size()) {
			throw new BusinessException(NCLangRes.getInstance().getStrByID(
					"tbb_plan", "01812pln_000698")/* 任务权限校验不通过 */);
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

	@Override
	public boolean isActionEnabled() {
		TbPlanContext tbPlanContext = getContext();
		if (tbPlanContext != null) {
			if (tbPlanContext.getTasks() != null) {
				MdTask[] tasks = tbPlanContext.getTasks();
				// 判断是否选中任务且选中一个
				String flag = "";
				// 判断是否选中任务且选中一个
				if (tasks != null && tasks.length > 0&&(getContext().getComplieStatus()==TbCompliePlanConst.COM_MODE_TASKVIEW)) {
					flag = tasks[0].getPlanstatus();
					ArrayList<String> status = new ArrayList<String>();
					status.add(ITaskStatus.LOCAL_PROMOTED);
					status.add(ITaskStatus.LOCAL_APPROVING);
					for (MdTask task : tasks) {
						if (!status.contains(task.getPlanstatus())||!flag.equals(task.getPlanstatus())) {
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void showMessage(String hintMessage) {
		super.showMessage(hintMessage);
	}
}