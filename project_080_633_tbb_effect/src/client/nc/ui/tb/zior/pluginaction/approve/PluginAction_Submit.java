
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
 * �����ڵ��µ��ϱ�����
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
					"tbb_plan", "01812pln_000479")/* ������ѡ���� */);
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
							"01812pln_000480")/* ��ʾ */, be.getMessage());
			return;
		}
//       //***********��ά�ύʱִ�й����excel���㣬��άר��
//		executeFmlAndRule();
//	   //************
		errorCount = 0;
		// �ϱ�֮ǰ������ѡ�����Ƿ���Ҫ���
		if (TbParamUtil.isCheckBeforeSubmitTask()) {
			errorCount = check();
		}
		if (errorCount > 0) {
			MessageDialog.showErrorDlg(
					getMainboard(),
					NCLangRes.getInstance().getStrByID("tbb_plan",
							"01812pln_000095")/* ��ʾ�� */,
					NCLangRes.getInstance().getStrByID("tbb_plan",
							"01812pln_000096")/* \n��˲�ͨ�����������ϱ��� */);
			return;
		}
		UserLoginVO uservo = ApprovePlanTools.getCompleteUser(task);
		// ������ѡ����״̬�Ƿ�����ύҪ���ж���������ҪԤ����ͨ����û�е���Ҫ�����ͱ����е�����ſ����ύ
		String action=ApprovePlanTools.getAction(task, "TaskActionFunctionPromote",getContext().getIsHasLocalApproval());
		tasks = ApprovePlanTools.getAskTasks(tasks,action,getContext().getIsHasLocalApproval());
		if (tasks == null || tasks.length == 0){
			if(getContext().getIsHasLocalApproval()){
				throw new BusinessException(NCLangRes.getInstance().getStrByID(
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0002")/*@res "��ʾ"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0278")/*@res "δ�ύ�ɹ�����ѡ����״̬�����ύ��"*/)/* ������ѡ���� ��״̬��ֻ�е�������Ʋſ���ͨ�� *//*
																				 * ������ѡ����
																				 * ��״̬
																				 * ��
																				 * ֻ�е�������Ʋſ���ͨ��
																				 */);
			}
			else{
				throw new BusinessException(NCLangRes.getInstance().getStrByID(
						"tbb_plan", "01812pln_000097")/* ������ѡ���� ��״̬��ֻ�е�������Ʋſ���ͨ�� *//*
																				 * ������ѡ����
																				 * ��״̬
																				 * ��
																				 * ֻ�е�������Ʋſ���ͨ��
																				 */);
			}
		}


		// �����ϱ�����Ի��� ���ճ��ڵ�ŵ����ϱ������
		String appnote = null;
		if (!ITbPlanActionCode.DAILY_NODETYPE
				.equals(getContext().getNodeType())) {
			//��ApprovePlanDlg�๹�캯���м��������������������ϱ������0�����˻������1��
			ApprovePlanDlg planDlg = new ApprovePlanDlg(this.getMainboard(),0);

			if (UIDialog.ID_OK != planDlg.showModal())
				return;
			appnote = planDlg.getAppNote();
//			if("Ĭ�����Ϊ'��'".equals(appnote)){
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
				//�ı�������Ⱦɫ
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
		int type = INtbPermConst.TYPE_BUDGETTASK;// Ԥ��������Դʵ������
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
			throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000706")/*û���ϱ�Ȩ��*/);
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
				// �ж��Ƿ�ѡ��������ѡ��һ��
				if (tasks != null && tasks.length > 0) {
					ArrayList<String> status = new ArrayList<String>();
						status.add(ITaskStatus.LOCAL_APPROVE_PASS);
						status.add(ITaskStatus.COMPILING);
						status.add(ITaskStatus.ADJUSTING);
					for (MdTask task : tasks) {
						if (!status.contains(task.getPlanstatus())
								|| (tbPlanContext.getComplieStatus() == TbCompliePlanConst.COM_MODE_TASKEDIT)) {
							
							//���:��־ǿ,���״�ϲ�����
							if(tbPlanContext.getComplieStatus() == TbCompliePlanConst.COM_MODE_TASKVIEW){
								if(task.getObjname().contains("�ϲ�")&&ITaskStatus.STARTED.equals(task.getPlanstatus())){
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
		// �ѵ�ǰ��modelset������datamodel��
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
		TaskDataModelAction action = new TaskDataModelAction(taskDataModel);// ���캯����ҪIWorkBook,���ǵ�ǰ���Ʊ���TaskDataModel
		action.checkWorkBook(sheets);// ����,Excel��˹�ʽִ��.
		// ������˵���Ϣ����
		List<CheckRuleResultVO> checkRuleResultVOList = new ArrayList<CheckRuleResultVO>();
		// �õ���ǰ�����Ĭ�Ϲ���
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
		// �����Ϣ�ŵ�taskDataModel��ÿ��TaskSheetDataModel�е�sheetCheckFml������.
		for (TBSheetViewer sheetView : siewers) {
			excuteCheck( sheetView);
			List<ExSheetCheckFml> checksFmls = sheetView.getTsDataModel()
					.getSheetCheckFml();
			if (checksFmls != null && checksFmls.size() > 0) {
				sheetCheckFmlMap.put(sheetView, checksFmls);
					excuteCheck( sheetView);
			}
		}
		// ����༭̬����ˣ�Ҫ����˵�excel��datamodelˢ�µ�ǰ̨
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

		// ��˺����ʾ��Ϣ,�������͵����ϱ�������Ҫ��ʾ

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

		// ����˽�������
		if ((errorCount > 0||warningCount>0)&&this.getCurrentViewer()!=null) {
			this.getCurrentViewer().getViewManager()
					.openView(info, getMainboard(), true);
			// ������˵���Ϣ������������
			((PlanCheckResultViewer) getMainboard().getView(
					ITbPlanActionCode.Id_CHECKVIEW)).setCheckData(
					checkRuleResultVOList, siewers, sheetCheckFmlMap);

		}
		return errorCount;
	}
	/**
	  * * ��ƺõķ�����Ϣչ����������
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
			if(sheetView.getTsDataModel().getName().equals(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0271")/*@res "03�̶��ʲ�Ͷ��Ԥ���"*/)){
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
			}else if(sheetView.getTsDataModel().getName().equals(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0272")/*@res "09����Ԥ���"*/)){
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

			}else if(sheetView.getTsDataModel().getName().equals(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0273")/*@res "11�ʲ���ծԤ���"*/)){
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

			}else if(sheetView.getTsDataModel().getName().equals(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0274")/*@res "12�������֧��Ԥ���"*/)){
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
