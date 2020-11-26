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
		//ncm_begin_xulink_NC2016072000088_2016-07-27_通版 
		/** 
		* 此补丁修正了产品BUG:报送管理等节点参照自定义档案
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
		//ncm_end_xulink_NC2016072000088_2016-07-27_通版 

		holder.getNodeEnv().setCurrMngStuc(rmsPK);
		holder.getNodeEnv().setCurrOrg(mainOrgPK);
		holder.getNodeEnv().setCurrGroupPK(WorkbenchEnvironment.getInstance().getGroupVO().getPk_group());

		holder.getQueryCond().setPk_mainOrg(mainOrgPK);
		holder.getQueryCond().setPk_rms(rmsPK);
		getTaskRefPane().getTask_refPane().setEnabled(rmsPK != null && mainOrgPK != null);

		boolean bNeedReInit = true;// 大部分情况应该是需要重新初始化的
		TaskVO taskVo = null;
		if(rmsPK == null){
			getTaskRefPane().getTask_refPane().setPK(null);
			queryCondVo.setTaskVo(null);

			holder.getNodeEnv().setCurrTaskPK(null);
//			getEntrance().setTask(null);
//			getEntrance().initParamByTask(null);
		}else if (!UfoPublic.strIsEqual(oldRmsPK, rmsPK)) {// 组织体系改变
			String strTaskPK=holder.getNodeEnv().getCurrTaskPK();
			try{
				if (strTaskPK!=null) {
					//如果未分配,则将默认打开任务置为空 // TODO 判断的时候应该结合组织体系一起考虑,考虑组织体系的业务属性与任务的业务属性是否一致
					/*
					 * TODO  此处有待优化.可以直接维护上下文中的IUfoQueryInitParam里的信息,然后在 IUfoQueryOrgTree 中的refreshByQueryCond 直接
					 * 取上下文中的信息,而不用再从数据库中取数据
					 */
					//edit by congdy 2015.8.4 切换组织体系由于任务参照被清空，直接清空组织树等其他内容
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
//						// @edit by wuyongc at 2012-5-24,下午3:21:51
//						//组织体系切换了，那么任务在该组织体系分配的组织可能不一致。
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
		}else if (!UfoPublic.strIsEqual(oldOrgPK, mainOrgPK)){		//组织发生了变化,判断原任务是否分配给了新组织
			String strTaskPK=holder.getNodeEnv().getCurrTaskPK();
			try{
				if (strTaskPK!=null) {
					//如果未分配,则将默认打开任务置为空
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
			// 重新初始化 环境中的IUfoQueryCondVO
			holder.reInitQueryParam();

			getEntrance().setTask(taskVo);
		}
		
	}

	/**
	 * 清空界面上显示的查询结果
	 * @create by wuyongc at 2011-9-15,下午8:35:58
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
			// add by liuweiu 2015-04-13 清空数据，上面的代码并只能表面清空数据
			if(resultModel instanceof IUfoBillManageModel){
				((IUfoBillManageModel) resultModel).filterData(null, true);
			}
		}
		//wangqi 20120423 控制刷新按钮不可用
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

	//第一进入按任务初始化
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

		// 清空数据同时
//		emptyQueryResult();
		// 清空数据后再设置列表。
		if (configHelper!=null){
			// @edit by wuyongc at 2011-5-26,上午10:05:54  调用ConfigHandler,任务切换后重新获取定制和筛选的列表
			configHelper.initBillListViewByConfigList();
		}
		// 清空数据
		emptyQueryResult();
	}

	// 初始化查询区域
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
		//TODO 某些监听器已经执行过,此处会再次执行...
		queryHolder.getQueryCondChangeHandler().fireQueryCondChange(null, queryHolder.getQueryCond(), this);
	}

	private void reInitQueryArea(){
		IUfoQueryArea queryArea=queryAction.createQueryArea();
		queryArea.removeListener();
		queryArea.getQuickQueryArea().clearQuickQueryArea();

//		IUfoQueryCondVO oldQueryCond = queryAction.getQuickQueryHolder().getQueryCond();
		// 配置文件中会通过spring调用了一次createQueryArea，下面的方法会再调用一次。重复调用，待优化
		queryAction.resetQueryEnvironment();
		//TODO 构建快速查询区域的方法里会调用查询，然后会调用fireQueryCondChange 和164行形成了重复调用
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
		//TODO 某些监听器已经执行过,此处会再次执行...

		IUfoQueryCondVO newQueryCondVO = queryHolder.getQueryCond();

		// 此处直接把旧的条件设置Null ,此处本身就是重新构造界面的方法,避免因为前面的一些事件处理,已经导致区分不出改变前后的关键字组合而导致关键字信息没有加载的问题.
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
//		TitledBorder titledBorder = BorderFactory.createTitledBorder(new EmptyBorder(0, 0, 0, 0),nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0425")/*@res "报表组织"*/);
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
