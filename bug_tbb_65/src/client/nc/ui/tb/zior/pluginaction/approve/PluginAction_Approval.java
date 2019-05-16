package nc.ui.tb.zior.pluginaction.approve;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import nc.bs.ml.NCLangResOnserver;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.mdm.permission.INtbPerm;
import nc.itf.mdm.permission.INtbPermConst;
import nc.itf.tb.limit.permission.IDataPermGetter;
import nc.ms.mdm.limit.DataPermGetterUtil;
import nc.ms.tb.event.TaskChangeEvent;
import nc.ms.tb.ext.plan.TbCompliePlanConst;
import nc.ms.tb.ext.zior.xml.ZiorFrameCtl;
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

public class PluginAction_Approval extends AbstractTbRepPluginAction {
	/*
	 * �����ڵ��µ���������
	 */
	public PluginAction_Approval(String name, String code) {
		super(name, code);
	}

	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		TbPluginActionDescriptor tbpad = new TbPluginActionDescriptor();
		tbpad.setName(TbActionName.getName_TbbApprove()); 
		tbpad.setGroupPaths(TbActionName.getName_NApprove());
		tbpad.setExtensionPoints(new XPOINT[] { XPOINT.MENU, XPOINT.TOOLBAR });
		tbpad.setIcon(ITbPlanActionCode.APPROVAL_ICON);
		return tbpad;
	}

	@SuppressWarnings("restriction")
	@Override
	public void actionPerformed(ActionEvent actionevent)
			throws BusinessException {
		/* ���������л�ȡ���� */
		MdTask[] tasks = null;
		tasks = getContext().getTasks();
		/*����һ����־λ��������¼�Ƿ����ˢ��*/
		boolean isRefreshFlag = false;
		/* �����Ƿ�ѡ������ */
		if (tasks == null || tasks.length <= 0) {
			MessageDialog.showWarningDlg(
					getMainboard(),
					NCLangRes.getInstance().getStrByID("tbb_plan",
							"01812pln_000411")/* �Ƿ�ѡ������ */,
					NCLangRes.getInstance().getStrByID("tbb_plan",
							"01812pln_000479")/* ������ѡ���� */);
			return;
		}

		UserVO user = WorkbenchEnvironment.getInstance().getLoginUser();
		tasks=ZiorFrameCtl.getCanApproveTasks(tasks);
//		checkTaskPrv(tasks, user.getPrimaryKey(), WorkbenchEnvironment.getInstance().getGroupVO().getPrimaryKey(),
//				getContext().getNodeType());
        if(tasks==null||tasks.length<=0){
        	return;
        }
		String warning = "";
		/* �����ļ������񲻿������� */
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
						new String[] { task.getObjname() })/* ����{0}�Ѿ�����,���������� */;
				break;
			} else if (ApprovePlanTools.isAppPassTask(task)) {
				warning = NCLangRes.getInstance().getStrByID("tbb_plan",
						"01812pln_000690", null,
						new String[] { task.getObjname() })/* ����{0}�Ѿ�����ͨ��,���������� */;
				break;
			}else if (!TaskActionCtl.checkPrevStatus(userVo, task,
					ITaskAction.APPROVE_PASS)) {
				warning = NCLangResOnserver.getInstance().getStrByID(
						"tbb_task",
						"01812tsk_000069",
						null,
						new String[] {
								(String) task.getObjname(),
								TaskConst.getTaskStatusShowName(task
										.getPlanstatus()),
								TaskConst.getTaskActionShowName(ITaskAction.APPROVE_PASS) })/*
																		 * �ƻ� {
																		 * 0 }
																		 * ״̬ {
																		 * 1 }
																		 * �붯�� {
																		 * 2 }
																		 * ���� ��
																		 * �޷�����
																		 */;
				break;
			}
		}
		if (!warning.isEmpty()) {
			MessageDialog.showHintDlg(getMainboard(), "", warning);
			return;
		}

	
			if (MessageDialog.showYesNoDlg(
					this.getMainboard(),
					NCLangRes.getInstance().getStrByID("tbb_plan",
							"01812pln_000465")/* ѯ�� */, NCLangRes.getInstance()
							.getStrByID("tbb_plan", "01812pln_000408")/*
																	 * �����������޷��˻�,
																	 * ��ȷ���Ƿ��������?
																	 */) != MessageDialog.ID_YES) {
				return;
			}
		

		/* ����������������� */
		isRefreshFlag = TbAppTaskUiUtil.approvePlan(tasks, this.getMainboard()/*
															 * null ui.
															 * getAbstractFuncletUI
															 * ()
															 */,TbActionName.getName_PiFu());
		MdTask[] taks=null;
		 taks=getHavedDoMdTasks(tasks);
		if(taks==null||taks.length<=0){
			return;
		}
		getContext().setTasks(taks);
		if(taks.length==1&&(ITaskStatus.APPROVING.equals(taks[0].getPlanstatus())||ITaskStatus.LOCAL_APPROVING.equals(taks[0].getPlanstatus()))){
			//�����е����񲻲���ˢ��
			return;
		}
		
		if (isRefreshFlag) {
			if(getCurrentView() != null){
				if (getCurrentView() instanceof TBSheetViewer) {
					refeshTreeRender(taks);
				}
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Viewer refresh = getCurrentView();
					if(refresh != null){
						if (refresh instanceof TbPlanListPanel) {
							((TbPlanListPanel) refresh).refreshZiorTask();
						} else if (refresh instanceof TBSheetViewer) {
							((TBSheetViewer)refresh).getViewManager().refresh(getCurrentViewer());
							
						}
					}
					TaskChangeEvent taskChangeEvent = new TaskChangeEvent(getCurrentView(),
							1);
					getMainboard().getEventManager().dispatch(taskChangeEvent);
					PluginActionEvent pluginActionEvent = new PluginActionEvent(
		    				getCurrentView(), 1);
		    		getCurrentView().getMainboard().getEventManager()
							.dispatch(pluginActionEvent);
				}
			});
			
		}
	}

	private void checkTaskPrv(MdTask[] diftask, String user, String pk_group,
			String key_nodeType) throws BusinessException {
		int type = INtbPermConst.TYPE_BUDGETTASK;// Ԥ��������Դʵ������
		String[] opercodes = new String[] { INtbPermConst.RES_OPERCODE_APPROVE };
		List<MdTask> list = new ArrayList<MdTask>();
		for (MdTask task : diftask) {
			list.add(task);
		}
		Set<MdTask> tasks_filtered = filterObjectsByPrv(list, type, opercodes,
				user, pk_group);
		if (tasks_filtered == null || tasks_filtered.size() != list.size()) {
			throw new BusinessException(NCLangRes.getInstance().getStrByID(
					"tbb_plan", "01812pln_000698")/* ����Ȩ��У�鲻ͨ�� */);
		}
	}

	private <T extends INtbPerm> Set<T> filterObjectsByPrv(List<T> objlist,
			int type, String[] openCode, String user, String pk_group)
			throws BusinessException {
		if (objlist == null || objlist.size() == 0) {
			return null;
		}
		IDataPermGetter data = DataPermGetterUtil.getDataPermGetter();// ����Ȩ�޹��˵Ľӿ�
		try {
			Set<T> result = data.getDatasWithPermission(objlist, type,
					openCode, user, pk_group); // ����Ȩ�޹��˽ӿڵ��õķ���
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
				// �ж��Ƿ�ѡ��������ѡ��һ��
				String flag = "";
				// �ж��Ƿ�ѡ��������ѡ��һ��
				if (tasks != null && tasks.length > 0&&(getContext().getComplieStatus()==TbCompliePlanConst.COM_MODE_TASKVIEW)) {
					flag = tasks[0].getPlanstatus();
					ArrayList<String> status = new ArrayList<String>();
					status.add(ITaskStatus.PROMOTED);
					status.add(ITaskStatus.APPROVING);
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
		// TODO Auto-generated method stub
		super.showMessage(hintMessage);
	}
}
