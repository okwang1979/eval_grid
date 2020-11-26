package nc.ui.tb.zior.pluginaction.planning;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import nc.ms.tb.event.TaskChangeEvent;
import nc.ms.tb.ext.plan.TbCompliePlanConst;
import nc.ms.tb.task.TaskActionCtl;
import nc.ms.tb.task.TbTaskCtl;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.tb.plan.action.ApprovePlanTools;
import nc.ui.tb.zior.CompileSaveLogs;
import nc.ui.tb.zior.TbPlanContext;
import nc.ui.tb.zior.TbPlanFrameUtil;
import nc.ui.tb.zior.pluginaction.AbstractTbRepPluginAction;
import nc.ui.tb.zior.pluginaction.TbPluginActionDescriptor;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.tb.control.exception.AdjustControlException;
import nc.vo.tb.ntbenum.CtrlTypeEnum;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.task.ITaskAction;
import nc.vo.tb.task.ITaskStatus;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;
import nc.vo.tb.util.IConst;

import com.ufida.zior.plugin.PluginKeys.XPOINT;
import com.ufida.zior.plugin.event.PluginActionEvent;

public class PluginAction_CompileSave_NoExcel extends AbstractTbRepPluginAction {
	private ImageIcon saveIcon = null;
	private ActionEvent actionevent = null;
	public PluginAction_CompileSave_NoExcel(String name, String code) {
		super(name, code);
	}

	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		TbPluginActionDescriptor  desc =new TbPluginActionDescriptor();
		desc.setName("保存不计算");
		desc.setGroupPaths(TbActionName.getName_file());
//		desc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		desc.setExtensionPoints(new XPOINT[]{XPOINT.TOOLBAR,XPOINT.MENU});
		desc.setIcon(ITbPlanActionCode.SAVE_ICON);

		return desc;
	}

	private ImageIcon getIcon() {
		if (saveIcon == null) {
			saveIcon = new ImageIcon(getClass().getResource(ITbPlanActionCode.SAVE_ICON));
		}
		return saveIcon;
	}

	@Override
	public void actionPerformed(ActionEvent actionevent) {
		try {
//			if(getContext() != null){
//				MdTask task = getMdTask();
//				TbPlanContext tbPlanContext = getContext();
//				if(GlobalParameter.getInstance().getPara("checkDirtyWhenCompile", Boolean.class) != null){
//					Boolean bl = GlobalParameter.getInstance().getPara("checkDirtyWhenCompile", Boolean.class);
//					if(bl != null && bl.booleanValue()){
//						if(tbPlanContext.getCurrentTs() != null){
//							String lastVersion = TbTaskServiceGetter.getTaskDataService().isLastVersion(task.getPk_obj(), tbPlanContext.getCurrentTs());
//							if(lastVersion != null){
//								MessageDialog.showErrorDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, "数据发生变化，请更新数据");
//								return ;
//							}
//						}
//					}
//				}
//			}
			CompileSaveLogs compileSaveLogs=new CompileSaveLogs();
			UFDateTime currentTs = getContext().getCurrentTs();
			getContext().setAction_Code(ITbPlanActionCode.code_CompileSave);
			boolean taskCacheTimeOut = getMdTask() == null? false:TaskDataCtl.isTaskCacheTimeOut(getMdTask().getPk_obj(),currentTs);
			if(taskCacheTimeOut){
				TbPlanFrameUtil.getTbPlanFrame(getMainboard()).stopAndreleaseProgress();
				MessageDialog.showErrorDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, NCLangRes.getInstance().getStrByID("tbb_plan_0","01050plan001-0408")/*@res "数据发生变化，请刷新数据"*/);
				return ;
			}
			//任务被停用后不能被保存
			MdTask task = getMdTask();
			MdTaskDef mdTaskDefBy= TbTaskCtl.getMdTaskDefByPk(task.getPk_taskdef(), false);
			String isactive = mdTaskDefBy == null ? null:mdTaskDefBy.getIsactive();
			if(isactive != null && IConst.FALSE.equals(isactive)){
				throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan_0","01050plan001-0119")/*@res "该任务模板已被停用，无法保存数据"*/);
			}
			boolean isIndexApp = getContext().isIndexApprove();  //是否指标审批
			if(!isIndexApp){
				UserLoginVO userLoginVO = ApprovePlanTools.getCompleteUser(getMdTask());
				Boolean flag = TaskActionCtl.checkPrevStatus(userLoginVO,
						new MdTask[] { task }, ITaskAction.COMPILE);
				if (flag) {
					showMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000250")/*任务状态改变中.....*/);
					processAction(userLoginVO, new MdTask[]{task}, ITaskAction.COMPILE, null);
					showMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000251")/*成功改变任务状态*/);
				} else {
					MessageDialog.showErrorDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000252", null, new String[]{task.getObjname()})/*任务{0}状态不符*/);
//					this.getTbReportDirView().refreshTask(task);
//					this.getTbReportDirView().getViewManager().stopAllViewEditing();
//					this.getTbReportDirView().setEditEnable(true);
//					getContext().setComplieStatus(TbCompliePlanConst.COM_MODE_TASKVIEW);
					return;
				}
			}
			// 保存任务（在该方法内增加了更新前台缓存时间的方法）
			compileSaveLogs= getCurrentViewer().getViewManager().saveTasksNoExcel();
			if(compileSaveLogs != null){
				if(compileSaveLogs.getAdjustControlRuleMessage() != null)
					throw new AdjustControlException(compileSaveLogs.getAdjustControlRuleMessage(), CtrlTypeEnum.WarningControl.toCodeString());
				if(compileSaveLogs.getRuleMessage() != null){
					NtbLogger.print(compileSaveLogs.getRuleMessage());
					getMainboard().getStatusBar().setHintMessage(NCLangRes.getInstance().getStrByID("tbb_plan_0","01050plan002-0359")/*@res ""部分单元格公式未执行成功，具体请查看日志！"*/,false);
				}
			}
			if(getTbReportDirView() != null){
				this.getTbReportDirView().setEditEnable(true);
			}
			/////////////////////////
			if(isIndexApp)   //由于liuyshb添加下面逻辑，需要改变任务状态的，导致指标审批总是弹出错误提示，暂时设定指标审批时可以直接返回
				return;
			MdTask[] tasks=TbTaskCtl.getMdTasksByWhere(task.getPKFieldName() +"='"+task.getPk_obj()+"'", true);
			if(tasks.length != 1){
				throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000253")/*任务状态出错*/);
			}
			task = tasks[0];
//				getContext().setComplieStatus(TbCompliePlanConst.COM_MODE_TASKEDIT);
			getContext().setTaskStatus(tasks[0].getPlanstatus());
			getContext().setTasks(tasks);
			this.getTbReportDirView().getViewManager().stopAllViewEditing();
			TaskChangeEvent taskChangeEvent = new TaskChangeEvent(getCurrentView(), 1);
			PluginActionEvent pluginActionEvent = new PluginActionEvent(getCurrentView(),1);
			this.getMainboard().getEventManager().dispatch(pluginActionEvent);
			this.getMainboard().getEventManager().dispatch(taskChangeEvent);
			this.getTbReportDirView().refreshTask(task);
		} catch(final AdjustControlException e) { 
		
			if(e.getControlType().equals(CtrlTypeEnum.WarningControl.toCodeString())) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						MessageDialog.showWarningDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, e.getMessage());
					}
				});
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						MessageDialog.showErrorDlg(getMainboard(),  NCLangRes.getInstance().getStrByID("tbb_bean", "01420ben_000018")/*错误*/, e.getMessage());

					}
				});
			}
//			ITaskDataInteractService service = TbTaskInteractServiceGetter.getTaskDataInteractService();
//			service.exportTaskData(task.getPk_obj());
		} catch(final Exception e) {
			getCurrentViewer().getViewManager().refresh(getCurrentViewer());
			NtbLogger.error(e);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					
					MessageDialog.showErrorDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, e.getMessage());
				}
			});
		}catch(final Throwable e){    //捕获保存时Error错误
			NtbLogger.error(e);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					
					MessageDialog.showErrorDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, e.getMessage());
				}
			});
		}
	}

	public boolean isShowProgress(){
		return true;
	}
	public void processAction(UserLoginVO userVo, MdTask[] tasks, String action, HashMap<String, Object> params) throws BusinessException {
		TaskActionCtl.processAction(userVo, tasks, action, params);
	}
	@Override
	public boolean isActionEnabled() {
		TbPlanContext tbPlanContext = getContext();
		if(tbPlanContext != null){
			String planstatus = tbPlanContext.getTaskStatus();
			if(planstatus != null){
				int status = tbPlanContext.getComplieStatus();
				List<String> statuss=new ArrayList<String>();
				//指标审批保存 指标审批状态为FALSE 浏览状态 已上报
				boolean isIndexApprove = tbPlanContext.isIndexApprove();
				if (TbCompliePlanConst.COM_MODE_TASKEDIT == status){
					if(isIndexApprove){
						statuss.add(ITaskStatus.PROMOTED);
						statuss.add(ITaskStatus.LOCAL_PROMOTED);
						statuss.add(ITaskStatus.APPROVING);
						statuss.add(ITaskStatus.LOCAL_APPROVING);
					}else{
						statuss.add(ITaskStatus.STARTED);
						statuss.add(ITaskStatus.ADJUSTING);
						statuss.add(ITaskStatus.COMPILING);
						statuss.add(ITaskStatus.LOCAL_APPROVE_NOTPASS);
						statuss.add(ITaskStatus.APPROVE_NOTPASS);
					}
					if(statuss.contains(planstatus)){
						return true;
					}
				}
			}
		}
		return false;
	}

}
