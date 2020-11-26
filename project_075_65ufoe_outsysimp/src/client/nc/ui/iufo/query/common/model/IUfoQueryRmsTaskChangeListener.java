package nc.ui.iufo.query.common.model;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComponent;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.impl.iufo.utils.MultiLangTextUtil;
import nc.itf.iufo.individual.IIUFODefaultSettingConsts;
import nc.ui.iufo.NodeEnv;
import nc.ui.iufo.commit.model.TangramInitEntrance;
import nc.ui.iufo.input.funclet.IRmsChangeListener;
import nc.ui.iufo.pub.UfoPublic;
import nc.ui.iufo.query.common.AbsIUfoQueryAction;
import nc.ui.iufo.query.common.ITaskChangeListener;
import nc.ui.iufo.query.common.area.IUfoQueryArea;
import nc.ui.iufo.query.common.area.IUfoQueryAreaShell;
import nc.ui.iufo.query.common.area.IUfoQuickQueryArea;
import nc.ui.iufo.query.common.comp.IUfoQueryFilterPanel;
import nc.ui.iufo.query.common.event.IUfoQueryHolder;
import nc.ui.iufo.query.common.filteritem.IUfoFilterItem;
import nc.ui.iufo.query.common.filteritem.KeyFilterItem;
import nc.ui.iufo.repdatamng.actions.ConfigListHelper;
import nc.ui.iufo.task.model.BusiPropTaskRefModel;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.ufoe.ref.RecieveTaskRefPanel;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.model.BillManageModel;
import nc.ui.uif2.model.HierachicalDataAppModel;
import nc.ui.uif2.model.ModelDataDescriptor;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataquery.RepBuziQueryCondVO;
import nc.vo.iufo.task.TaskVO;

import com.ufida.iufo.pub.tools.AppDebug;

public class IUfoQueryRmsTaskChangeListener implements IRmsChangeListener,ITaskChangeListener,IIUFODefaultSettingConsts{
	private AbsIUfoQueryAction queryAction=null;
	private RepBuziQueryCondVO queryCondVo=null;
	private IUfoQueryAreaShell queryShell=null;
	private TangramInitEntrance entrance=null;
	private BillManageModel resultModel=null;
	private ConfigListHelper configHelper=null;
	private RecieveTaskRefPanel taskRefPane=null;
	private IUfoQueryFilterPanel filterCondPane=null;
	private HierachicalDataAppModel commitmodel = null;

	@Override
	public void onRmsChange(NodeEnv nodeEnv) {
		String oldRmsPK=queryCondVo.getRmsPK();
		String oldOrgPK=queryCondVo.getLoginOrgPK();

		String rmsPK=nodeEnv.getCurrMngStuc();
		String mainOrgPK=nodeEnv.getCurrOrg();
		queryCondVo.setLoginOrgPK(mainOrgPK);
		queryCondVo.setRmsPK(rmsPK);

		IUfoQueryHolder holder=queryAction.getQuickQueryHolder();
		//ncm_begin_xulink_NC2016072000088_2016-07-27_ͨ�� 
		/** 
		* �˲��������˲�ƷBUG:���͹���Ƚڵ�����Զ��嵵��
		*/ 
		List<IUfoFilterItem> vFilterItem=holder.getQuickQueryItem();
		if (vFilterItem != null && vFilterItem.size() > 0) {
			for (IUfoFilterItem item : vFilterItem) {
				if (item instanceof KeyFilterItem) {
					JComponent comp=item.getEditComponent();
					if (comp instanceof UIRefPane) {
						((UIRefPane)comp).setPk_org(mainOrgPK);
					}
				}
			}
		}
		//ncm_end_xulink_NC2016072000088_2016-07-27_ͨ�� 

		holder.getNodeEnv().setCurrMngStuc(rmsPK);
		holder.getNodeEnv().setCurrOrg(mainOrgPK);
		holder.getNodeEnv().setCurrGroupPK(WorkbenchEnvironment.getInstance().getGroupVO().getPk_group());

		holder.getQueryCond().setPk_mainOrg(mainOrgPK);
		holder.getQueryCond().setPk_rms(rmsPK);
		getTaskRefPane().getTask_refPane().setEnabled(rmsPK != null && mainOrgPK != null);

		boolean bNeedReInit = true;// �󲿷����Ӧ������Ҫ���³�ʼ����
		TaskVO taskVo = null;
		if(rmsPK == null){
			getTaskRefPane().getTask_refPane().setPK(null);
			queryCondVo.setTaskVo(null);

			holder.getNodeEnv().setCurrTaskPK(null);
//			getEntrance().setTask(null);
//			getEntrance().initParamByTask(null);
		}else if (!UfoPublic.strIsEqual(oldRmsPK, rmsPK)) {// ��֯��ϵ�ı�
			String strTaskPK=holder.getNodeEnv().getCurrTaskPK();
			try{
				if (strTaskPK!=null) {
					//���δ����,��Ĭ�ϴ�������Ϊ�� // TODO �жϵ�ʱ��Ӧ�ý����֯��ϵһ����,������֯��ϵ��ҵ�������������ҵ�������Ƿ�һ��
					/*
					 * TODO  �˴��д��Ż�.����ֱ��ά���������е�IUfoQueryInitParam�����Ϣ,Ȼ���� IUfoQueryOrgTree �е�refreshByQueryCond ֱ��
					 * ȡ�������е���Ϣ,�������ٴ����ݿ���ȡ����
					 */
					//edit by congdy 2015.8.4 �л���֯��ϵ����������ձ���գ�ֱ�������֯������������
					getTaskRefPane().getTask_refPane().setPK(null);
					holder.getNodeEnv().setCurrTaskPK(null);
					queryCondVo.setTaskVo(null);
					taskVo = null;
//					if (!TaskSrvUtils.isSameBussPropTaskWithRms(rmsPK, strTaskPK)){
//						getTaskRefPane().getTask_refPane().setPK(null);
//						holder.getNodeEnv().setCurrTaskPK(null);
//						queryCondVo.setTaskVo(null);
//						taskVo = null;
//
//					} else {
//						taskVo = getEntrance().getQueryCondVo().getTaskVo();
//
//						// @edit by wuyongc at 2012-5-24,����3:21:51
//						//��֯��ϵ�л��ˣ���ô�����ڸ���֯��ϵ�������֯���ܲ�һ�¡�
//						ITaskAssignQueryService assignService=NCLocator.getInstance().lookup(ITaskAssignQueryService.class);
//						IUfoQueryInitParam initParam = holder.getLoginContext().getInitParam();
//						TaskInfoAndAssignOrgVO taskInfoOrg =initParam.getTaskInfoOrg();
//						taskInfoOrg.setAssignedOrgPKs(assignService.getTaskAssignOrgPKs(taskVo.getPk_task(), rmsPK));
//						holder.getQueryCond().setOrgPKs(taskInfoOrg.getAssignedOrgPKs());
//
//						getEntrance().setTask(taskVo);
//						bNeedReInit = false;
//					}
				}

			}catch(Exception e){
				AppDebug.debug(e);
			}
			((BusiPropTaskRefModel)getTaskRefPane().getTaskRefModel()).setRmsPK(rmsPK);
//			getTaskRefPane().getTask_refPane().getRefModel().reloadData();
		}else if (!UfoPublic.strIsEqual(oldOrgPK, mainOrgPK)){		//��֯�����˱仯,�ж�ԭ�����Ƿ�����������֯
			String strTaskPK=holder.getNodeEnv().getCurrTaskPK();
			try{
				if (strTaskPK!=null) {
					//���δ����,��Ĭ�ϴ�������Ϊ��
					if (!TaskSrvUtils.isReceiveTask(mainOrgPK, strTaskPK)){
						getTaskRefPane().getTask_refPane().setPK(null);
						holder.getNodeEnv().setCurrTaskPK(null);
						queryCondVo.setTaskVo(null);
					} else {
						taskVo = getEntrance().getQueryCondVo().getTaskVo();
					}
				} else {
					taskVo = TaskSrvUtils.getDefaultTaskVOByOrg(mainOrgPK,rmsPK);
					if(taskVo != null){
						getTaskRefPane().getTask_refPane().setSelectedData(taskVo.getPk_task(), taskVo.getCode(), MultiLangTextUtil.getCurLangText(taskVo));
						holder.getNodeEnv().setCurrTaskPK(taskVo.getPk_task());
					}
				}

			}catch(Exception e){
				AppDebug.debug(e);
			}

			((BusiPropTaskRefModel)getTaskRefPane().getTaskRefModel()).setRmsPK(rmsPK);
		}else{
			taskVo = getEntrance().getQueryCondVo().getTaskVo();
		}
		
		emptyQueryResult();

		if(bNeedReInit){
			getEntrance().initParamByTask(taskVo);
			// ���³�ʼ�� �����е�IUfoQueryCondVO
			holder.reInitQueryParam();

			getEntrance().setTask(taskVo);
		}
		
	}

	/**
	 * ��ս�������ʾ�Ĳ�ѯ���
	 * @create by wuyongc at 2011-9-15,����8:35:58
	 *
	 */
	private void emptyQueryResult() {
		if (resultModel!=null){
			ModelDataDescriptor modelDataDescriptor = resultModel.getCurrentDataDescriptor();
			if(modelDataDescriptor != null){
				modelDataDescriptor.setName("");
				modelDataDescriptor.setCount(0);
			}else{
				modelDataDescriptor = new ModelDataDescriptor();
			}
			resultModel.initModel(null,modelDataDescriptor);
			// add by liuweiu 2015-04-13 ������ݣ�����Ĵ��벢ֻ�ܱ����������
			if(resultModel instanceof IUfoBillManageModel){
				((IUfoBillManageModel) resultModel).filterData(null, true);
			}
		}
		//wangqi 20120423 ����ˢ�°�ť������
		if (commitmodel != null) {
			commitmodel.initModel(null);
		}
		ShowStatusBarMsgUtil.showStatusBarMsg(null, getEntrance().getContext());
	}
//
//	private String getDefaultTaskID(){
//		IndividualSetting setting = null;
//		try {
//			setting = IUFOIndividualSettingUtil.getIndividualSetting();
//
//		if(setting == null)
//			return null;
//		String pk_task = setting.getString(DEFAULT_TASK);
//		return pk_task;
//		} catch (Exception e1) {
//			AppDebug.debug(e1);
//		}
//		return null;
//	}

	//��һ���밴�����ʼ��
	public void initTask(TaskVO task){
		if (configHelper!=null){
			configHelper.initBillListViewByConfigList();
		}
		queryAction.setEnabled(task != null);

		//TODO
		queryCondVo.setTaskVo(task);
		IUfoQueryHolder holder=queryAction.getQuickQueryHolder();
		String taskPK = task==null?null:task.getPk_task();
		holder.getNodeEnv().setCurrTaskPK(taskPK);
		holder.getLoginContext().getInitParam().setCurTaskPK(taskPK);
//		((IUfoQueryLoginContext)getResultModel().getContext()).getInitParam().setCurTaskPK(taskPK);


		initQueryArea();
	}


	@Override
	public void onTaskChange(TaskVO task,TaskVO oldTask) {
		queryAction.setEnabled(task!=null);
		queryCondVo.setTaskVo(task);
		IUfoQueryHolder holder=queryAction.getQuickQueryHolder();
		String taskPK = task==null?null:task.getPk_task();
		holder.getNodeEnv().setCurrTaskPK(taskPK);
//		((IUfoQueryLoginContext)getResultModel().getContext()).getInitParam().setCurTaskPK(taskPK);

//		IUfoQueryCondVO qc = holder.getQueryCond();
		if(task != null && oldTask != null && task.getPk_task().equals(oldTask.getPk_task())){
			reInitOrgArea(queryCondVo.getRmsPK(),queryCondVo.getLoginOrgPK());
		}else
 			reInitQueryArea();

		// �������ͬʱ
//		emptyQueryResult();
		// ������ݺ��������б�
		if (configHelper!=null){
			// @edit by wuyongc at 2011-5-26,����10:05:54  ����ConfigHandler,�����л������»�ȡ���ƺ�ɸѡ���б�
			configHelper.initBillListViewByConfigList();
		}
		// �������
		emptyQueryResult();
	}

	// ��ʼ����ѯ����
	private void initQueryArea(){
		IUfoQueryArea queryArea=queryAction.createQueryArea();
		getQueryShell().setQueryArea(queryArea);
		queryArea.setQueryExecutor(getQueryShell().getQueryExecutor());

		getQueryShell().add(queryArea,BorderLayout.CENTER);
		getQueryShell().revalidate();
		getQueryShell().repaint();

		if (getFilterCondPane()!=null)
			getFilterCondPane().setQueryAction(queryAction);
		IUfoQueryHolder queryHolder=queryAction.getQuickQueryHolder();
		//TODO ĳЩ�������Ѿ�ִ�й�,�˴����ٴ�ִ��...
		queryHolder.getQueryCondChangeHandler().fireQueryCondChange(null, queryHolder.getQueryCond(), this);
	}

	private void reInitQueryArea(){
		IUfoQueryArea queryArea=queryAction.createQueryArea();
		queryArea.removeListener();
		queryArea.getQuickQueryArea().clearQuickQueryArea();

//		IUfoQueryCondVO oldQueryCond = queryAction.getQuickQueryHolder().getQueryCond();
		// �����ļ��л�ͨ��spring������һ��createQueryArea������ķ������ٵ���һ�Ρ��ظ����ã����Ż�
		queryAction.resetQueryEnvironment();
		//TODO �������ٲ�ѯ����ķ��������ò�ѯ��Ȼ������fireQueryCondChange ��164���γ����ظ�����
		queryArea=queryAction.createQueryArea();

		getQueryShell().removeAll();
		getQueryShell().setQueryArea(queryArea);
		getQueryShell().setMiniAction4QueryArea();
		queryArea.setQueryExecutor(getQueryShell().getQueryExecutor());

		getQueryShell().add(queryArea,BorderLayout.CENTER);
		getQueryShell().revalidate();
		getQueryShell().repaint();

		if (getFilterCondPane()!=null)
			getFilterCondPane().setQueryAction(queryAction);
		IUfoQueryHolder queryHolder=queryAction.getQuickQueryHolder();
		//TODO ĳЩ�������Ѿ�ִ�й�,�˴����ٴ�ִ��...

		IUfoQueryCondVO newQueryCondVO = queryHolder.getQueryCond();

		// �˴�ֱ�ӰѾɵ���������Null ,�˴�����������¹������ķ���,������Ϊǰ���һЩ�¼�����,�Ѿ��������ֲ����ı�ǰ��Ĺؼ�����϶����¹ؼ�����Ϣû�м��ص�����.
		queryHolder.getQueryCondChangeHandler().fireQueryCondChange(null, newQueryCondVO, this);
	}

	//TODO
	private void reInitOrgArea(String strRmsPK,String strOrgPK){
		if (getFilterCondPane()!=null)
			getFilterCondPane().setQueryAction(queryAction);
		IUfoQueryArea queryArea=queryAction.createQueryArea();
		IUfoQuickQueryArea qqa = queryArea.getQuickQueryArea();
		IUfoQueryCondVO inputCond=qqa.getQueryHolder().getQueryCondEditHandler().getInputFilterCond(qqa.getQueryHolder().getQueryCond().getPk_querycond());

		//IUfoQueryCondVO cond=queryHolder.getQueryCondEditHandler().getInputFilterCond(queryHolder.getQueryCond().getPk_querycond());
		String strSelOrgPK=inputCond==null?null:inputCond.getKeyVal(KeyVO.CORP_PK);
		try {
			qqa.getOrgTreePane().refreshByQueryCond(qqa.getQueryHolder().getQueryCond(), strRmsPK,strOrgPK, qqa.getQueryHolder().getQueryCond().getOrgPKs(), strSelOrgPK);
		} catch (Exception e) {
			AppDebug.debug(e);
		}


		IUfoQueryHolder holder = queryAction.getQuickQueryHolder();
		if (inputCond == null) {
			inputCond=(IUfoQueryCondVO)holder.getQueryCond().clone();
			holder.getQueryCondEditHandler().addInputFilterCond(inputCond);
		}
		holder.getQueryCondChangeHandler().fireQueryCondSave(inputCond);
//		qqa.getScrollOrgTreePane().removeAll();
//
//		JPanel pal = new JPanel();
//		TitledBorder titledBorder = BorderFactory.createTitledBorder(new EmptyBorder(0, 0, 0, 0),nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0425")/*@res "������֯"*/);
//		pal.setBorder(titledBorder);
//		pal.setLayout(new BorderLayout());
//		pal.add(qqa.getOrgTreePane(),BorderLayout.CENTER);
//		pal.setBackground(Color.WHITE);
//
//		qqa.getScrollOrgTreePane().setViewportView(pal);
//		qqa.repaint();
	}
	public AbsIUfoQueryAction getQueryAction() {
		return queryAction;
	}

	public void setQueryAction(AbsIUfoQueryAction queryAction) {
		this.queryAction = queryAction;
	}

	public RepBuziQueryCondVO getQueryCondVo() {
		return queryCondVo;
	}

	public void setQueryCondVo(RepBuziQueryCondVO queryCondVo) {
		this.queryCondVo = queryCondVo;
	}

	public IUfoQueryAreaShell getQueryShell() {
		return queryShell;
	}

	public void setQueryShell(IUfoQueryAreaShell queryShell) {
		this.queryShell = queryShell;
	}

	public RecieveTaskRefPanel getTaskRefPane() {
		return taskRefPane;
	}

	public void setTaskRefPane(RecieveTaskRefPanel taskRefPane) {
		this.taskRefPane = taskRefPane;
	}

	public TangramInitEntrance getEntrance() {
		return entrance;
	}

	public void setEntrance(TangramInitEntrance entrance) {
		this.entrance = entrance;
		this.entrance.registerTaskChangeListener(this);
	}

	public BillManageModel getResultModel() {
		return resultModel;
	}

	public void setResultModel(BillManageModel resultModel) {
		this.resultModel = resultModel;
	}

	public ConfigListHelper getConfigHelper() {
		return configHelper;
	}

	public void setConfigHelper(ConfigListHelper configHelper) {
		this.configHelper = configHelper;
	}

	public IUfoQueryFilterPanel getFilterCondPane() {
		return filterCondPane;
	}

	public void setFilterCondPane(IUfoQueryFilterPanel filterCondPane) {
		this.filterCondPane = filterCondPane;
	}

	public HierachicalDataAppModel getCommitmodel() {
		return commitmodel;
	}

	public void setCommitmodel(HierachicalDataAppModel commitmodel) {
		this.commitmodel = commitmodel;
	}
}
