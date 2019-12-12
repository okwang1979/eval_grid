package nc.ui.tb.zior.pluginaction.planning;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.KeyStroke;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.mdm.permission.INtbPerm;
import nc.itf.mdm.permission.INtbPermConst;
import nc.itf.tb.limit.permission.IDataPermGetter;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ms.mdm.limit.DataPermGetterUtil;
import nc.ms.tb.event.TaskChangeEvent;
import nc.ms.tb.ext.plan.TbCompliePlanConst;
import nc.ms.tb.task.TaskActionCtl;
import nc.ms.tb.task.TbTaskCtl;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.tb.plan.action.ApprovePlanTools;
import nc.ui.tb.zior.TBSheetViewer;
import nc.ui.tb.zior.TbPlanContext;
import nc.ui.tb.zior.ViewManager;
import nc.ui.tb.zior.pluginaction.AbstractTbRepPluginAction;
import nc.ui.tb.zior.pluginaction.TbPluginActionDescriptor;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbEnv;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.task.ITaskAction;
import nc.vo.tb.task.ITaskStatus;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;
import nc.vo.tb.util.IConst;

import com.ufida.zior.plugin.PluginKeys.XPOINT;
import com.ufida.zior.plugin.event.PluginActionEvent;

public class PluginAction_Compiling extends AbstractTbRepPluginAction {

	public PluginAction_Compiling(String name, String code) {
		super(name, code);
	}

	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		TbPluginActionDescriptor desc = new TbPluginActionDescriptor();
		desc.setName(TbActionName.getName_Compile());
		desc.setGroupPaths(TbActionName.getName_file());
		desc.setExtensionPoints(new XPOINT[] { XPOINT.MENU, XPOINT.TOOLBAR });
		desc.setIcon(ITbPlanActionCode.EDIT_ICON);
		desc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,KeyEvent.CTRL_MASK));
		// desc.setMemonic('C');
		return desc;
	}

	/**
	 * 编制按钮
	 * 1.校验状态的合理性
	 * 2.改变任务状态
	 * 3.修改编辑状态
	 * 4.修改TbPlanContext
	 * 5.刷新树状态
	 */
	@Override
	public void actionPerformed(ActionEvent actionevent)
			throws BusinessException {
		TbPlanContext tbPlanContext = getContext();
		if(tbPlanContext != null){
			
	//特殊主体控制可编辑型 by：王志强 at：2019/11/14 to:环球医疗
			
			
			List<Object> queryObj =  NtbSuperServiceGetter.getINtbSuper().query4List("select count(*) from user_tables where table_name =upper('tb_not_edit_task')");
			
			Object[] row1 =  (Object[])queryObj.get(0);
			if(row1[0].equals(1)){
				String tasDefName = tbPlanContext.getTaskDef().getObjname();
				
				
				TBSheetViewer viewer = (TBSheetViewer)getCurrentView();
				 
				ViewManager manager = viewer.getViewManager();
				TaskDataModel model =  manager.getTaskDataModel();
				if(model!=null){
					DimLevel unitDl = DimServiceGetter.getDimManager().getDimLevelByBusiCode(IDimLevelCodeConst.ENTITY);
					LevelValue entityValue =  model.getTaskParadim().getLevelValue(unitDl);
					DimLevel mvTypeDl =  DimServiceGetter.getDimManager().getDimLevelByBusiCode(IDimLevelCodeConst.MVTYPE);
					LevelValue mvTypeValue  =  model.getTaskParadim().getLevelValue(mvTypeDl);
					String entiCode = entityValue!=null?entityValue.getCode():null;
					String mvtypeCode = mvTypeValue!=null?mvTypeValue.getCode():"budget";
					if(entiCode!=null){
						List<Object> editList =  NtbSuperServiceGetter.getINtbSuper().query4List("select 1 from tb_not_edit_task where task_name='"+tasDefName+"' and unit_code='"+entiCode.toLowerCase()+"' and mvtype_code='"+mvtypeCode.toLowerCase()+"'");
						if(editList!=null&&editList.size()>0){
							throw new BusinessRuntimeException("当前主体为汇总主体不允许编制："+entityValue.getName());
						}
					}
				}
//				String code_unit = tbPlanContext.
				
				
			}
			
			
			
			
			//**********************end

			MdTask task = getMdTask();
//			if(GlobalParameter.getInstance().getPara("checkDirtyWhenCompile", Boolean.class) != null){
//				Boolean bl = GlobalParameter.getInstance().getPara("checkDirtyWhenCompile", Boolean.class);
//				if(bl != null && bl.booleanValue()){
//					if(tbPlanContext.getCurrentTs() != null){
//						String lastVersion = TbTaskServiceGetter.getTaskDataService().isLastVersion(task.getPk_obj(), tbPlanContext.getCurrentTs());
//						if(lastVersion != null){
//							MessageDialog.showErrorDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("plan_0","01050plan001-0408")/*@res "数据发生变化，请刷新数据"*/);
//							return ;
//						}
//					}
//				}
//			}
			UFDateTime currentTs = tbPlanContext.getCurrentTs();
			boolean taskCacheTimeOut = TaskDataCtl.isTaskCacheTimeOut(task.getPk_obj(),currentTs);
			if(taskCacheTimeOut){
				MessageDialog.showErrorDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, NCLangRes.getInstance().getStrByID("tbb_plan_0","01050plan001-0408")/*@res "数据发生变化，请刷新数据"*/);
				return ;
			}
			UserLoginVO userLoginVO = ApprovePlanTools.getCompleteUser(getMdTask());
			//yuyonga 判断离线端表示,如果是离线端,跳过下面的代码
			if(!NtbEnv.isOutLineUI){
				try{
					checkTaskPrv(task, userLoginVO, tbPlanContext.getNodeType());
				}catch(BusinessException be){
					MessageDialog.showErrorDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, be.getMessage());
					return;
				}
			}
			Boolean flag = TaskActionCtl.checkPrevStatus(userLoginVO,
					new MdTask[] { getMdTask() }, ITaskAction.COMPILE);
			if (flag) {
				showMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000250")/*任务状态改变中.....*/);
				processAction(userLoginVO, new MdTask[]{task}, ITaskAction.COMPILE, null);
				showMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000251")/*成功改变任务状态*/);
			} else {
				MessageDialog.showErrorDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000252", null, new String[]{task.getObjname()})/*任务{0}状态不符*/);
			}
			//先暂时通过当前Viewer来做，这个获得一般是指注册的viewer
			//MdTask[] tasks = TbTaskServiceGetter.getTaskObjectService().getMdTasksByWhere(task.getPKFieldName() +"='"+task.getPk_obj()+"'");
			//by   liyingm
			MdTask[] tasks=TbTaskCtl.getMdTasksByWhere(task.getPKFieldName() +"='"+task.getPk_obj()+"'", true);
			if(tasks.length != 1){
				throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000253")/*任务状态出错*/);
			}
			task = tasks[0];
			tbPlanContext.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKEDIT);
			tbPlanContext.setTaskStatus(tasks[0].getPlanstatus());
			tbPlanContext.setTasks(tasks);
			this.getTbReportDirView().getViewManager().startEditing();
			TaskChangeEvent taskChangeEvent = new TaskChangeEvent(getCurrentView(), 1);
			PluginActionEvent pluginActionEvent = new PluginActionEvent(getCurrentView(),1);
			this.getMainboard().getEventManager().dispatch(pluginActionEvent);
			this.getMainboard().getEventManager().dispatch(taskChangeEvent);
			this.getTbReportDirView().refreshTask(task);
			this.getTbReportDirView().setEditEnable(false);

		}else{
			throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000254")/*获取上下文出错*/);
		}
	}

	private void checkTaskPrv(MdTask task, UserLoginVO userVo, String key_nodeType)throws BusinessException {
		if(task == null){
			return;
		}
		//任务被停用后不能被编制
		MdTaskDef mdTaskDefBy= TbTaskCtl.getMdTaskDefByPk(task.getPk_taskdef(), false);
		String isactive = mdTaskDefBy == null ? null:mdTaskDefBy.getIsactive();
		if(isactive != null && IConst.FALSE.equals(isactive)){
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0409")/*@res "该任务模板已被停用，无法修改数据"*/);
		}
		int type = INtbPermConst.TYPE_BUDGETTASK;// 预算任务资源实体类型
		String info = NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000436")/*编制*/;
		String[] opercodes = new String[] {INtbPermConst.RES_OPERCODE_PLAN };
		if(ITbPlanActionCode.DIRECTADJUST_NODETYPE.equals(key_nodeType)){
			opercodes = new String[] {INtbPermConst.RES_OPERCODE_ADJUST };
			info = NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000438")/*调整*/;
		}
		if(ITbPlanActionCode.RATEMAINTENANCE_NODETYPE.equals(key_nodeType)){
			return;
		}
		String pk_group = WorkbenchEnvironment.getInstance().getGroupVO().getPrimaryKey();
		List<MdTask> list = new ArrayList<MdTask>();
		list.add(task);
		Set<MdTask> tasks_filtered = filterObjectsByPrv(list, type, opercodes, userVo.getPk_user(), pk_group);
		if (tasks_filtered == null || tasks_filtered.size() != list.size()) {
			throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000705", null, new String[]{info})/*没有{0}权限*/);
		}
	}

	private <T extends INtbPerm> Set<T> filterObjectsByPrv(List<T> objlist, int type, String[] openCode, String user, String pk_group) throws BusinessException {
		if (objlist == null || objlist.size() == 0) {
			return null;
		}
		IDataPermGetter data = DataPermGetterUtil.getDataPermGetter();// 数据权限过滤的接口
		try {
			Set<T> result = data.getDatasWithPermission(objlist, type, openCode, user, pk_group); // 数据权限过滤接口调用的方法
			return result;
		} catch (BusinessException e) {
			throw e;
		}
	}

	public void processAction(UserLoginVO userVo, MdTask[] tasks, String action, HashMap<String, Object> params) throws BusinessException {
		TaskActionCtl.processAction(userVo, tasks, action, params);
	}

	/**
	 * 当前任务为已启动、编制中、调整、审批不通过中四种状态,当前界面单位为默认且TableReport为非编辑状态，编制PluginAction可见
	 *
	 */
	@Override
	public boolean isActionEnabled() {
		TbPlanContext tbPlanContext = getContext();
		if(tbPlanContext != null){
			String planstatus = tbPlanContext.getTaskStatus();
			if(planstatus != null){
				int status = tbPlanContext.getComplieStatus();
				int valueCale = tbPlanContext.getAttributeScale();
//				if(TbParamUtil.hasLocalApprove ()){
//					if (valueCale == -1 && TbCompliePlanConst.COM_MODE_TASKVIEW == status
//							&& (ITaskStatus.STARTED.equals(planstatus)
//									|| ITaskStatus.ADJUSTING.equals(planstatus) ||
//									ITaskStatus.COMPILING.equals(planstatus) ||
//									ITaskStatus.LOCAL_APPROVE_NOTPASS.equals(planstatus))&&tbPlanContext.getTaskNumber()==1){
//						return true;
//					}
//				}else{
//					if (valueCale == -1 && TbCompliePlanConst.COM_MODE_TASKVIEW == status
//							&& (ITaskStatus.STARTED.equals(planstatus)
//									|| ITaskStatus.ADJUSTING.equals(planstatus) ||
//									ITaskStatus.COMPILING.equals(planstatus) ||
//									ITaskStatus.APPROVE_NOTPASS.equals(planstatus))&&tbPlanContext.getTaskNumber()==1){
//						return true;
//					}
//				}
				if (valueCale == -1 && TbCompliePlanConst.COM_MODE_TASKVIEW == status
						&& (ITaskStatus.STARTED.equals(planstatus)
								|| ITaskStatus.ADJUSTING.equals(planstatus) ||
								ITaskStatus.COMPILING.equals(planstatus) ||
								ITaskStatus.APPROVE_NOTPASS.equals(planstatus) ||
								ITaskStatus.LOCAL_APPROVE_NOTPASS.equals(planstatus))&&tbPlanContext.getTaskNumber()==1){
					return true;
				}
			}
		}
		return false;
	}

}