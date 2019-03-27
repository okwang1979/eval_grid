package nc.ui.iufo.input.ufoe.comp;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nc.bs.framework.common.NCLocator;
import nc.itf.iufo.task.ITaskQueryService;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.iufo.formula.UfoeFmlManagePane;
import nc.ui.iufo.formula.manage.FmlManagePane;
import nc.ui.iufo.input.TableInputOperUtil;
import nc.ui.iufo.input.control.base.AbsRepDataControler;
import nc.ui.iufo.input.edit.base.AbsBaseRepDataEditor;
import nc.ui.iufo.input.edit.base.AbsRepDataEditor;
import nc.ui.iufo.input.edit.event.RepDataTabActiveEvent;
import nc.ui.iufo.input.ufoe.control.IUFORepDataControler;
import nc.ui.iufo.input.ufoe.edit.IUFOCombRepDataEditor;
import nc.ui.iufo.input.ufoe.edit.ListFindAction;
import nc.ui.iufo.input.view.base.AbsKeyCondPanel;
import nc.ui.iufo.input.view.base.FormulaManageViewer;
import nc.ui.iufo.input.view.base.KeyCondPaneUtil;
import nc.ui.iufo.pub.UfoPublic;
import nc.ui.iuforeport.rep.ReportBO_Client;
import nc.ui.pub.beans.UITabbedPane;
import nc.ui.pub.beans.constenum.DefaultConstEnum;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.iufo.approve.ApproveStateEnum;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.task.ICommitConfigConstant;
import nc.vo.iufo.task.TaskApproveVO;
import nc.vo.iufo.task.TaskInfoVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iuforeport.rep.ReportShowVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.pf.IPfRetCheckInfo;

import com.ufida.dataset.IContext;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.table.exarea.ExtendAreaCell;
import com.ufida.iufo.table.model.FreeDSStateUtil;
import com.ufida.report.anareport.exec.FreePolicy;
import com.ufida.report.anareport.exec.FreePolicyFactory;
import com.ufida.zior.comp.KMenuItem;
import com.ufida.zior.plugin.IPlugin;
import com.ufida.zior.plugin.IPluginAction;
import com.ufida.zior.plugin.PluginActionAdapter;
import com.ufida.zior.plugin.event.PluginActionEvent;
import com.ufida.zior.util.UIUtilities;
import com.ufida.zior.view.Mainboard;
import com.ufsoft.iufo.check.vo.CheckDetailVO;
import com.ufsoft.iufo.check.vo.CheckResultVO;
import com.ufsoft.iufo.fmtplugin.dynarea.DynamicAreaModel;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.DynAreaShowExt;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.IUfoInputDataPlugin;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.IUfoRepDisplayPercentAction;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.IUfoRepHeaderLockPlugin;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.IUfoRepStylePlugin;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.IUfoSystemPlugin;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.check.IUfoCheckPlugin;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.commit.view.TaskCommitControler;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.print.RDPrintControler;
import com.ufsoft.iufo.inputplugin.biz.file.ChangeKeywordsData;
import com.ufsoft.iufo.inputplugin.biz.file.MenuStateData;
import com.ufsoft.iufo.inputplugin.ufobiz.ufoe.reptemplate.UfoeRepFinalFileViewer;
import com.ufsoft.iuforeport.repdatainput.ufoe.IUfoTableInputHandlerHelper;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.report.constant.DefaultSetting;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.CellsPane;
import com.ufsoft.table.ReportTable;

@SuppressWarnings("serial")
public abstract class AbsCombRepDataEditor extends AbsBaseRepDataEditor {
	protected UITabbedPane m_tabPane = null;

	// TabPane是否初始化过
	protected boolean m_bInitedTabPane = false;

	protected IRepDataEditorInComb m_activeEditor = null;

	//表间审核结果
	protected CheckResultVO[] m_taskCheckResults = null;

	// 用户选择追踪的最近表间审核结果
	protected CheckDetailVO m_taskCheckDetail =null;

	// 审核单元格需要标注的颜色
	protected Color m_taskCheckColor =null;

	// 审核结果追踪定位到的单元格列表，只对应一个报表数据窗口
	protected List<CellPosition> m_taskCheckCells =null;

	//当前对应的舍位条件PK
	protected String m_strBalCondPK=BalanceCondVO.NON_SW_DATA_COND_PK;

	protected TaskVO task = null;

	protected String taskpk = null;

//	private List<RepCommitStateVO> vInputResult = null;

	protected boolean ismodified = false;

	protected String reppk = null;
	
	protected ITaskQueryService taskQueryService=null;
	/**
	 * 生成主TabPane面板的方法
	 */
	abstract protected void geneTabPaneEditors();

	/**
	 * 舍位时，需要保存该任务下所有报表数据所提示
	 * @return
	 */
	abstract protected String getAlertWhenNeedSave();

	/**
	 * 设置主视图标题
	 */
	abstract protected void doSetTitle();

	/**
	 * 初始化关键字录入界面的值
	 */
	abstract protected void initKeyValues(String[] strKeyVals);

	abstract protected KeyGroupVO getKeyGroup();

	/**
	 * 生成关键字面板
	 */
	abstract protected AbsKeyCondPanel createKeyCondPanel(String strOrgPK,ChangeKeywordsData[] changeKeywordsDatas,String strRepPK,String strRMSPK);
//	abstract protected void repaintKeyCondPanel(String strOrgPK,ChangeKeywordsData[] changeKeywordsDatas,String strRepPK,String strRMSPK);
	public AbsCombRepDataEditor(){
		super();
	}

	/*************** Begin:模拟AbsAnaReportDesigner的一组方法 ********************/
	@Override
	public JComponent createTableContainer(){
		m_tabPane = initTabbedPane();
		return m_tabPane;
	}

	@Override
	public void doInit(){
		addTitleAction(new RefreshViewAction(), 0);
		addTitleAction(new ListFindAction(this, m_tabPane), 0);
		getContext().setAttribute(IUfoContextKey.OPERATION_STATE,IUfoContextKey.OPERATION_INPUT);
		reInitContent();
		getEventManager().dispatch(new RepDataTabActiveEvent(null,getActiveRepDataEditor()));
//		try {
//			task=TaskSrvUtils.getTaskVOById(getTaskPK());
//		} catch (UFOSrvException e) {
//			e.printStackTrace();
//		}
	}

	public UITabbedPane getTabPane(){
		return this.m_tabPane;
	}

	@Override
	public CellsPane getCellsPane(){
		IRepDataEditorInComb activeEditor = getActiveEditor();
		return activeEditor == null ? null : activeEditor.getCellsPane();
	}

	@Override
	public ReportTable getTable(){
		IRepDataEditorInComb activeEditor = getActiveEditor();
		return activeEditor == null ? null : activeEditor.getTable();
	}

	@Override
	protected boolean save(){
		if (isDirty() == false)
			return true;

		// 仿照保存全部菜单处理，将活跃窗口放在最后处理
		IRepDataEditorInComb activeEditor = getActiveEditor();
		IRepDataEditorInComb[] editors = getAllRepDataEditors();
		for (IRepDataEditorInComb editor : editors){
			if (editor.isDirty() && editor != activeEditor){
				activeOneRepDataEditor(editor.getRepPK());
				if (!editor.save())
					return false;
			}
		}
		activeOneRepDataEditor(activeEditor.getRepPK());
		return activeEditor.save();
	}

	@Override
	public boolean isDirty(){
		if (m_repDataParam == null || m_repDataParam.getAloneID() == null)
			return false;

		if (m_strBalCondPK != null
				&& !m_strBalCondPK.equals(BalanceCondVO.NON_SW_DATA_COND_PK))
			return false;

		IRepDataEditorInComb[] editors = getAllRepDataEditors();
		for (IRepDataEditorInComb editor : editors){
			if (editor.isDirty())
				return true;
		}
		return false;
	}

	@Override
	public void shutdown(){
		Mainboard mainBoard = getMainboard();
		
		//wangqi 20130503 关闭打开的视图时要清除动态区展示的map
		IUFOCombRepDataEditor designer = null;
		if (mainBoard.getCurrentView() instanceof IUFOCombRepDataEditor) {
			designer = (IUFOCombRepDataEditor)mainBoard.getCurrentView();
		}

		if (designer != null && mainBoard.getPluginManager() != null) {
			List<JComponent> list = mainBoard.getPluginManager().getPluginComponents(IUfoInputDataPlugin.class.getName());
			if(list != null) {
				for (JComponent jComponent : list) {
					if (jComponent instanceof KMenuItem) {
						Action action = ((KMenuItem)jComponent).getAction();
						IPluginAction iPluginAction = ((PluginActionAdapter)action).getInterPluginAction();
						if (DynAreaShowExt.class.getName().equals(iPluginAction.getClass().getName())) {
							Map<String, CellsModel> map = ((DynAreaShowExt)iPluginAction).getMapmodel();
							map.remove(getRepDataParam().getReportPK() + getId());
							break;
						}
					}
				}
			}
		}
		
		super.shutdown();

		AbsNavUnitTreeViewer treeViewer = (AbsNavUnitTreeViewer)mainBoard.getView(AbsNavUnitTreeViewer.NAV_UNIT_TREE_ID);
		if (treeViewer != null) {
			treeViewer.reInitKeyCondPane(null);
		}

	}

	@Override
	public void refresh(){
		//只有激活当前页签时刷新有效
		if(getActiveRepDataEditor() != null && getActiveRepDataEditor().getAloneID() != null){
			refreshContent(m_strBalCondPK);
			if(getActiveRepDataEditor() instanceof AbsNotRepDataEditor){
				return;
			}
			
			//wangqi 20130731 刷新后重新设定显示比例
			IPlugin plugin = getMainboard().getPluginManager().getPlugin(getRepStylePluginName());
			if (plugin != null) {
				IPluginAction[] actions = plugin.getPluginActions();
				if (actions != null && actions.length > 0) {
					for(IPluginAction action : actions) {
						if (action instanceof IUfoRepDisplayPercentAction) {
							JComboBox combobox = ((IUfoRepDisplayPercentAction)action).getcombobox();
							if (combobox != null) {
								DefaultConstEnum selectItem = (DefaultConstEnum) combobox
										.getSelectedItem();
								if (selectItem != null && !selectItem.equals(DefaultSetting.REP_DISPLAY_PERCENT[4])) {
									ActionEvent actionEvent=new ActionEvent(combobox,-1,null);
									action.execute(actionEvent);
									break;
								}
							}
						}
					}
				}
			}

			// add by yuyangi 最后派发事件
			PluginActionEvent event=new PluginActionEvent(this,0);
			getEventManager().dispatch(event);
		}
	}

	/*************** End:模拟AbsAnaReportDesigner的一组方法 ********************/

	/*************** Begin:实现AbsBaseRepDataEditor类的一组方法 **********/
	@Override
	public IRepDataEditorInComb getActiveRepDataEditor(){
		return getActiveEditor();
	}

	@Override
	public String getInputDataPluginName(){
		return IUfoInputDataPlugin.class.getName();
	}

	@Override
	public String getCheckPluginName(){
		return IUfoCheckPlugin.class.getName();
	}

	@Override
	protected String getSystemPluginName(){
		return IUfoSystemPlugin.class.getName();
	}

	@Override
	protected String getRepStylePluginName(){
		return IUfoRepStylePlugin.class.getName();
	}

	protected String getRepHeaderLockPluginName(){
		return IUfoRepHeaderLockPlugin.class.getName();
	}

	@Override
	protected void initRepDataParam(){
		super.initRepDataParam();

		try {
			if (m_repDataParam.getTaskPK() != null && !m_repDataParam.getTaskPK().equals(taskpk)){
				taskpk = m_repDataParam.getTaskPK();
				TaskInfoVO taskInfoVo = ((IUFORepDataControler)IUFORepDataControler.getInstance(getMainboard())).getSelectedTaskInfo();
				if(taskInfoVo != null && taskInfoVo.getTaskId().equals(getTaskPK()))
					task = taskInfoVo.getTaskVO();
				else
					task=TaskSrvUtils.getTaskVOById(getTaskPK());

			}
		} catch (UFOSrvException e) {
			AppDebug.debug(e);
		}
	}

	/**
	 * 按报表PK激活某一Editor
	 *
	 * @param strRepPK
	 */
	@Override
	public boolean activeOneRepDataEditor(String strRepPK){
		try{
			// 如果RepPK为空，则激活第一个报表
			boolean bFirstRep = strRepPK == null;
			for (int i = 0; i < m_tabPane.getTabCount(); i++){
				Component comp = m_tabPane.getComponentAt(i);
				if (comp instanceof JTabbedPane){
					JTabbedPane subTabPane = (JTabbedPane) comp;
					for (int j = 0; j < subTabPane.getTabCount(); j++){
						IRepDataEditorInComb editor = (IRepDataEditorInComb) subTabPane.getComponentAt(j);
						if (bFirstRep){
							innerReInitOneEditor(editor, true);
							m_tabPane.setSelectedIndex(0);
							subTabPane.setSelectedIndex(0);
							return true;
						} else if (editor.getRepPK().equals(strRepPK)){
							// 打开报表数据，并置TabPane及子TabPane的选中页签
							innerReInitOneEditor(editor, true);
							// 先选中子页签，父页签的事件中有重启线程操作，见initTabbedPane方法
							subTabPane.setSelectedIndex(j);
							m_tabPane.setSelectedIndex(i);
							return true;
						}
						//wangqi 20120816 防止没有报表查看权限时（editor.getRepPK()为空），单击选中组织查询时，
						//不走innerReInitOneEditor给m_keyCondPane设定keygroupPK，在MeasureDataUtil-getAloneID中会出错
						String keygroupPK = editor.getPubData().getKType();
						m_keyCondPane.setKeygroupPk(keygroupPK);
					}
				} else{
					IRepDataEditorInComb editor = (IRepDataEditorInComb) comp;
					if (bFirstRep){
						innerReInitOneEditor(editor, true);
						m_tabPane.setSelectedIndex(0);
						return true;
					} else if (editor.getRepPK()!= null && editor.getRepPK().equals(strRepPK)){
						innerReInitOneEditor(editor, true);
						m_tabPane.setSelectedIndex(i);
						return true;
					}
					//wangqi 20120816 防止没有报表查看权限时（editor.getRepPK()为空），单击选中组织查询时，
					//不走innerReInitOneEditor给m_keyCondPane设定keygroupPK，在MeasureDataUtil-getAloneID中会出错
					String keygroupPK = editor.getPubData().getKType();
					m_keyCondPane.setKeygroupPk(keygroupPK);
				}
			}
			return false;
		}finally{
			m_activeEditor=null;
		}
	}

	public boolean confirmSaveRepData(){
		if (getTable()!=null && getTable().getCellEditor() != null &&  !getTable().getCellEditor().stopCellEditing())
			return false;
		IRepDataEditorInComb activeEditor = getActiveEditor();
		if (activeEditor.isDirty()){
			//上报不保存并上报不成功时，恢复为未保存状态用
			ismodified = true;
			int iResult = UIUtilities.showConfirmDialog(getMainboard(),
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-1433"),
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-1434"),
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (iResult == JOptionPane.CANCEL_OPTION){
				return false;
			}
			if (iResult ==JOptionPane.CLOSED_OPTION){
				return false;
			}
			if (iResult == JOptionPane.YES_OPTION){
				if (!activeEditor.save()) {
					return false;
				} else {
					ismodified = false;
				}

			}

			//防止本类refreshContent()方法中再次出现是否保存的确认信息
			activeEditor.getCellsModel().setDirty(false);
		}
//		if (isDirty()){
////			int iResult = UIUtilities.showConfirmDialog(getMainboard(),nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0410")/*@res "是否保存报表数据，如果不保存，此次报表数据修改可能丢失"*/,
////					NCLangRes.getInstance().getStrByID("20090618", "upp09061800031"),JOptionPane.YES_NO_OPTION);
//			int iResult = UIUtilities.showConfirmDialog(getMainboard(),nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0410")/*@res "是否保存报表数据，如果不保存，此次报表数据修改可能丢失"*/,
//					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413001_0","01413001-0031")/*@res "确认"*/,JOptionPane.YES_NO_OPTION);
//			if (iResult != JOptionPane.YES_OPTION){
//				return true;
//			}
//			return save();
//		}

		return true;
	}

	public void resetCommitMenuState(MenuStateData newMenuState, Map<String, Integer> repCommitVos){
		IRepDataEditorInComb[] editors=getAllRepDataEditors();
		if (editors==null || newMenuState==null)
			return;

//		for (IRepDataEditorInComb editor:editors){
//			if (editor.getMenuState()!=null){
//				editor.getMenuState().setCanCommit(newMenuState.isCanCommit());
//				editor.getMenuState().setCommited(newMenuState.isCommited());
//				editor.getMenuState().setCanRequestCancelCommit(newMenuState.isCanRequestCancelCommit());
//
//				if (newMenuState.isCommited()){
//					editor.getContext().setAttribute(IUfoContextKey.DATA_RIGHT,IUfoContextKey.RIGHT_DATA_READ);
//					editor.clearDirty();
//				}
//			}
//		}

		String[] strRepPKs=new String[editors.length];
		for (int i=0;i<editors.length;i++){
			if (editors[i].getMenuState()!=null){
				editors[i].getMenuState().setCommitstatus(newMenuState.getCommitstatus());
				editors[i].getMenuState().setCanCommit(newMenuState.isCanCommit());
				editors[i].getMenuState().setCommited(newMenuState.isCommited());
				editors[i].getMenuState().setCanRequestCancelCommit(newMenuState.isCanRequestCancelCommit());
				editors[i].getMenuState().setApprovestatus(newMenuState.getApprovestatus());
				if (!Integer.valueOf(newMenuState.getApprovestatus()).equals(IPfRetCheckInfo.NOSTATE)){
					editors[i].getMenuState().setM_bCanInput(false);
				} else {
					editors[i].getMenuState().setM_bCanInput(true);
				}
				// 分布式上传的报表数据不能修改
				if (newMenuState.beDisTrans())
					editors[i].getMenuState().setM_bCanInput(false);
			}
			strRepPKs[i]=editors[i].getRepPK();
		}

		getContext().setAttribute(IUfoContextKey.TASK_APPROVE_STATE, newMenuState.getApprovestatus());

//		String aloneid = m_repDataParam.getAloneID();
//		IRepDataInfoQuerySrv querySrv = (IRepDataInfoQuerySrv) NCLocator.getInstance().lookup(IRepDataInfoQuerySrv.class.getName());
		try {
//			vInputResult= (List<RepCommitStateVO>)ActionHandler.execWithZip(IUfoTableInputActionHandler.class.getName(), "getRepCommitStateVO", new Object[]{
//				aloneid,strRepPKs,m_repDataParam.getTaskPK()
//			});

//			if (vInputResult != null && vInputResult.size() > 0) {
				for (IRepDataEditorInComb editor:editors){

//					for (RepCommitStateVO data : vInputResult) {
					if(editor.getRepPK() != null && repCommitVos != null) {
						Integer repCommitState = repCommitVos.get(editor.getRepPK());
						if(repCommitState != null && repCommitState.intValue() >= CommitStateEnum.STATE_COMMITED.getIntValue()) {
							if (editor.getMenuState() != null) {
								editor.getMenuState().setM_bCanInput(false);
							}
						}
					}
					
//						if (editor.getRepPK() != null && editor.getRepPK().equals(data.getPk_report()) &&
//								data.getRepcommitstate().intValue() >= CommitStateEnum.STATE_COMMITED.getIntValue()) {
//							if (editor.getMenuState() != null) {
//								editor.getMenuState().setM_bCanInput(false);
//							}
//						}
//					}
				}
//				if (vInputResult.get(0).getTaskcommitstate() == null) {
//					getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, CommitStateEnum.STATE_NOCOMMIT.getIntValue());
//				} else {
//					getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, vInputResult.get(0).getTaskcommitstate());
//				}

//			} else {
//				getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, CommitStateEnum.STATE_NOCOMMIT.getIntValue());
//			}
			
			if(newMenuState.getCommitstatus() != null) {
				getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, newMenuState.getCommitstatus());
			} else {
				getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, CommitStateEnum.STATE_NOCOMMIT.getIntValue());
			}
			

			if (task != null && task.getCommstrategy().intValue()==ICommitConfigConstant.COMMIT_STRAGY_SELECT &&
					IUfoTableInputHandlerHelper.commitstate != null &&
					IUfoTableInputHandlerHelper.commitstate.intValue() == CommitStateEnum.STATE_AFFIRMED.getIntValue()) {
				for (IRepDataEditorInComb editor:editors){
					editor.getContext().setAttribute(IUfoContextKey.DATA_RIGHT,IUfoContextKey.RIGHT_DATA_READ);
					editor.clearDirty();
				}
			}

		} catch (Exception e) {
			AppDebug.error(e);
		}
		// modify by yuyangi 在刷新的最后派发事件。
//		PluginActionEvent event=new PluginActionEvent(this,0);
//		getEventManager().dispatch(event);
	}
	
	//wangqi 2014015 方法重载，因为合并报表覆写了原resetCommitMenuState方法
	public void resetCommitMenuState(MenuStateData newMenuState, Map<String, Integer> repCommitVos,Map<String, Integer> repApproveStatues){
		IRepDataEditorInComb[] editors=getAllRepDataEditors();
		if (editors==null || newMenuState==null)
			return;

//		for (IRepDataEditorInComb editor:editors){
//			if (editor.getMenuState()!=null){
//				editor.getMenuState().setCanCommit(newMenuState.isCanCommit());
//				editor.getMenuState().setCommited(newMenuState.isCommited());
//				editor.getMenuState().setCanRequestCancelCommit(newMenuState.isCanRequestCancelCommit());
//
//				if (newMenuState.isCommited()){
//					editor.getContext().setAttribute(IUfoContextKey.DATA_RIGHT,IUfoContextKey.RIGHT_DATA_READ);
//					editor.clearDirty();
//				}
//			}
//		}

		String[] strRepPKs=new String[editors.length];
		for (int i=0;i<editors.length;i++){
			if (editors[i].getMenuState()!=null){
				editors[i].getMenuState().setCommitstatus(newMenuState.getCommitstatus());
				editors[i].getMenuState().setCanCommit(newMenuState.isCanCommit());
				editors[i].getMenuState().setCommited(newMenuState.isCommited());
				editors[i].getMenuState().setCanRequestCancelCommit(newMenuState.isCanRequestCancelCommit());
				editors[i].getMenuState().setApprovestatus(newMenuState.getApprovestatus());
				if (!Integer.valueOf(newMenuState.getApprovestatus()).equals(IPfRetCheckInfo.NOSTATE)){
					editors[i].getMenuState().setM_bCanInput(false);
				} else {
					editors[i].getMenuState().setM_bCanInput(true);
				}
				// 分布式上传的报表数据不能修改
				if (newMenuState.beDisTrans())
					editors[i].getMenuState().setM_bCanInput(false);
			}
			strRepPKs[i]=editors[i].getRepPK();
		}

		getContext().setAttribute(IUfoContextKey.TASK_APPROVE_STATE, newMenuState.getApprovestatus());

//		String aloneid = m_repDataParam.getAloneID();
//		IRepDataInfoQuerySrv querySrv = (IRepDataInfoQuerySrv) NCLocator.getInstance().lookup(IRepDataInfoQuerySrv.class.getName());
		try {
//			vInputResult= (List<RepCommitStateVO>)ActionHandler.execWithZip(IUfoTableInputActionHandler.class.getName(), "getRepCommitStateVO", new Object[]{
//				aloneid,strRepPKs,m_repDataParam.getTaskPK()
//			});

//			if (vInputResult != null && vInputResult.size() > 0) {
				for (IRepDataEditorInComb editor:editors){

//					for (RepCommitStateVO data : vInputResult) {
					if(editor.getRepPK() != null && repCommitVos != null) {
						Integer repCommitState = repCommitVos.get(editor.getRepPK());
						if(repCommitState != null && repCommitState.intValue() >= CommitStateEnum.STATE_COMMITED.getIntValue()) {
							if (editor.getMenuState() != null) {
								editor.getMenuState().setM_bCanInput(false);
							}
						}
					}
					// @edit by zhoushuang at 2014-1-9,下午6:44:02 根据报表审批状态判断cells是否可修改
					if(editor.getRepPK() != null && repApproveStatues != null) {
						Integer repApproveState = repApproveStatues.get(editor.getRepPK()+TaskApproveVO.FLOWTYPE_COMMIT);
						if(repApproveState != null && repApproveState.intValue() != ApproveStateEnum.NOSTATE) {
							if (editor.getMenuState() != null) {
								editor.getMenuState().setM_bCanInput(false);
							}
						}
					}
					
//						if (editor.getRepPK() != null && editor.getRepPK().equals(data.getPk_report()) &&
//								data.getRepcommitstate().intValue() >= CommitStateEnum.STATE_COMMITED.getIntValue()) {
//							if (editor.getMenuState() != null) {
//								editor.getMenuState().setM_bCanInput(false);
//							}
//						}
//					}
				}
//				if (vInputResult.get(0).getTaskcommitstate() == null) {
//					getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, CommitStateEnum.STATE_NOCOMMIT.getIntValue());
//				} else {
//					getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, vInputResult.get(0).getTaskcommitstate());
//				}

//			} else {
//				getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, CommitStateEnum.STATE_NOCOMMIT.getIntValue());
//			}
			
			if(newMenuState.getCommitstatus() != null) {
				getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, newMenuState.getCommitstatus());
			} else {
				getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, CommitStateEnum.STATE_NOCOMMIT.getIntValue());
			}
			

			if (task != null && task.getCommstrategy().intValue()==ICommitConfigConstant.COMMIT_STRAGY_SELECT &&
					IUfoTableInputHandlerHelper.commitstate != null &&
					IUfoTableInputHandlerHelper.commitstate.intValue() == CommitStateEnum.STATE_AFFIRMED.getIntValue()) {
				for (IRepDataEditorInComb editor:editors){
					editor.getContext().setAttribute(IUfoContextKey.DATA_RIGHT,IUfoContextKey.RIGHT_DATA_READ);
					editor.clearDirty();
				}
			}

		} catch (Exception e) {
			AppDebug.error(e);
		}
		// modify by yuyangi 在刷新的最后派发事件。
//		PluginActionEvent event=new PluginActionEvent(this,0);
//		getEventManager().dispatch(event);
	}

	@Override
	protected FmlManagePane getFmlManagePane(FormulaManageViewer viewer,
			CellsPane cellsPane, CellsModel cellsModel, IContext context,
			boolean isFmtDesign) {
		return new UfoeFmlManagePane(viewer, cellsPane, getCellsModel(), getContext(), isFmtDesign);
	}

	/**
	 * 悬浮窗形式打开公式管理器
	 * creator tanyj
	 */
	@Override
	protected FmlManagePane getFmlManagePane(FormulaManageViewer viewer,
			CellsPane cellsPane, CellsModel cellsModel, IContext context,
			boolean isFmtDesign,Mainboard mainboard) {
		return new UfoeFmlManagePane(null, cellsPane, getCellsModel(), context, isFmtDesign,mainboard);
	}

	/*************** End:实现AbsBaseRepDataEditor类的一组方法 **********/

	/**
	 * 对报表数据进行刷新，及按舍位条件切换数据的方法
	 * @param strNewBalCondPK
	 */
	@Override
	public boolean refreshContent(String strNewBalCondPK){
		if (strNewBalCondPK == null)
			strNewBalCondPK = BalanceCondVO.NON_SW_DATA_COND_PK;

		// 提示报表数据是否保存的处理在本方法中进行
		IRepDataEditorInComb activeEditor = getActiveEditor();
		if (activeEditor == null || m_repDataParam.getAloneID() == null)
			return false;

		if (getTable()!= null && getTable().getCellEditor() != null && !getTable().getCellEditor().stopCellEditing())
			return false;

		if (m_strBalCondPK.equals(strNewBalCondPK)){
			if (activeEditor.isDirty()){
				int iResult = UIUtilities.showConfirmDialog(getMainboard(),
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-1433"),
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-1434"),
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (iResult == JOptionPane.CANCEL_OPTION){
					return false;
				}
				if (iResult == JOptionPane.YES_OPTION){
					if (!activeEditor.save())
						return false;
				}
			}
		} else{// 对应舍位
			if (isDirty()){
				int iResult = UIUtilities.showConfirmDialog(getMainboard(),getAlertWhenNeedSave(),
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-1434"),
						JOptionPane.YES_NO_OPTION);
				if (iResult != JOptionPane.YES_OPTION){
					return false;
				}
				if (!save())
					return false;
			}
		}

		// 设置新的舍位条件PK，并对当前活跃Editor进行舍位
		m_strBalCondPK = strNewBalCondPK;
		activeEditor.refreshContent(strNewBalCondPK);
		return true;
	}

	@Override
	public AbsKeyCondPanel geneKeyCondPane(String strRepPK) {
		if (m_repDataParam == null || m_repDataParam.getTaskPK() == null)
			return null;

		Mainboard mainBoard = getMainboard();

		MeasurePubDataVO pubData = m_pubData;
		KeyGroupVO keyGroup =getKeyGroup();
//		KeyGroupVO keyGroup = m_pubData.getKeyGroup();

		KeyVO[] keyVOs = keyGroup.getKeys();
		// 2,得到关键字的初始值
		String[] strKeyInitValues = null;
		try{
			strKeyInitValues = KeyCondPaneUtil.getKeyValues(mainBoard, keyVOs,pubData, AbsRepDataControler.getInstance(mainBoard).getLoginEnv(mainBoard).getCurLoginDate());
			initKeyValues(strKeyInitValues);
		} catch (Exception e){
			AppDebug.debug(e);
		}

		String strRmsPK = (String) getContext().getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK);
		String strOrgPK = (String) getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);

		// #得到切换关键字的界面显示需要的数据对象
//		ChangeKeywordsData[] datas = (ChangeKeywordsData[]) ActionHandler.exec(IUfoRepDataActionHandler.class.getName(),"geneChangeKeywordsDatas", new Object[] { keyVOs,strKeyInitValues, false, strOrgPK });
		ChangeKeywordsData[] datas = TableInputOperUtil.geneChangeKeywordsDatas(keyVOs, strKeyInitValues, false, strOrgPK);
		return createKeyCondPanel((String) getMainboard().getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK), datas, strRepPK, strRmsPK);
	}

//	@Override
//	public void repaintKeyCondPane(String strRepPK) {
//		if (m_repDataParam == null || m_repDataParam.getTaskPK() == null)
//			return;
//
//		Mainboard mainBoard = getMainboard();
//
//		MeasurePubDataVO pubData = m_pubData;
//		KeyGroupVO keyGroup =getKeyGroup();
//
//		KeyVO[] keyVOs = keyGroup.getKeys();
//		// 2,得到关键字的初始值
//		String[] strKeyInitValues = null;
//		try{
//			strKeyInitValues = KeyCondPaneUtil.getKeyValues(mainBoard, keyVOs,pubData, AbsRepDataControler.getInstance(mainBoard).getLoginEnv(mainBoard).getCurLoginDate());
//			initKeyValues(strKeyInitValues);
//		} catch (Exception e){
//			AppDebug.debug(e);
//		}
//
//		String strRmsPK = (String) getContext().getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK);
//		String strOrgPK = (String) getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);
//
//		// #得到切换关键字的界面显示需要的数据对象
////		ChangeKeywordsData[] datas = (ChangeKeywordsData[]) ActionHandler.exec(IUfoRepDataActionHandler.class.getName(),"geneChangeKeywordsDatas", new Object[] { keyVOs,strKeyInitValues, false, strOrgPK });
//		ChangeKeywordsData[] datas = TableInputOperUtil.geneChangeKeywordsDatas(keyVOs, strKeyInitValues, false, strOrgPK);
//		repaintKeyCondPanel((String) getMainboard().getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK), datas, strRepPK, strRmsPK);
//	}

	/**
	 * 重新初始化窗口
	 * @i18n miufohbbb00125=打开报表失败
	 * @i18n miufohbbb00126=根据选定查询条件，未查询到报表数据，请确认在此关键字条件下是否有录入数据或是否对数据有操作权限
	 */
	@Override
	public void reInitContent(){
		try{
			String strPrevAloneID = m_repDataParam.getAloneID();
			String strPrevTaskPK = m_repDataParam.getTaskPK();
			MeasurePubDataVO prevPubData = m_repDataParam.getPubData();

			//单位关键字
			String strUnitPK = null;
			if(prevPubData != null)
				strUnitPK = prevPubData.getUnitPK();

			// 初始化RepDataParam
			initRepDataParam();
//			doSetTitle();

			// 判断任务是否发生了改变
			if (UfoPublic.strIsEqual(m_repDataParam.getTaskPK(), strPrevTaskPK)){
				// 判断任务是否为空,初始化未选择任务时，此时任务PK为空，不应该加载TabPane
				if (m_tabPane.getTabCount() <= 0){
					geneTabPaneEditors();
					addSubTabPaneListener();
				}

				// 判断关键字值是否发生了变化,没有发生变化，则直接退出
				if (!StringUtil.isEmptyWithTrim(m_repDataParam.getReportPK())
						&& UfoPublic.strIsEqual(m_repDataParam.getAloneID(),strPrevAloneID)
						&& (m_repDataParam.getPubData() == null || m_repDataParam.getAloneID() != null || m_repDataParam.getPubData().equals(prevPubData))){
					return;
				} else{
					// 重新生成关键字面板
					reInitKeyCondPane(m_repDataParam.getReportPK());

					//判断单位关键字，发生变化则重新生成页签；否则初始化页签 add by jiaah at 20110615
					if(isFilterRepTabs() && strUnitPK != null && !m_repDataParam.getPubData().getUnitPK().equals(strUnitPK)){
						// 重新生成各个Editor
						geneTabPaneEditors();
						addSubTabPaneListener();
					}
					else{
						// 关键字条件发生变化，则重置各页签Editor
						reInitAllTabEditors();
					}
				}
			} else{
				// 重新生成关键字面板
				reInitKeyCondPane(m_repDataParam.getReportPK());
				// 重新生成各个Editor
				geneTabPaneEditors();
				addSubTabPaneListener();
			}
			doSetTitle();
		} finally{
			// 激活用户选中的报表PK,打开报表
			doSetContext(getContext(), m_repDataParam);
			activeOneRepDataEditor(m_repDataParam.getReportPK());
			//wangqi 在AbsNavUnitTreeViewer中重新加载editor中生成的关键字面板
			Mainboard mainBoard = getMainboard();
			AbsNavUnitTreeViewer treeViewer = (AbsNavUnitTreeViewer)mainBoard.getView(AbsNavUnitTreeViewer.NAV_UNIT_TREE_ID);
			if (treeViewer != null) {
				treeViewer.reInitKeyCondPane(m_repDataParam.getReportPK());
			}
			AbsRepDataControler.getInstance(getMainboard()).setLastActiveRepDataEditor(this);
		}
	}

	/**
	 * 是否过滤报表页签
	 * @create by jiaah at 2011-6-15,下午05:28:52
	 * @return
	 */
	protected boolean isFilterRepTabs(){
		return false;
	}

	public String getTaskPK(){
		return m_repDataParam.getTaskPK();
	}

	/**
	 * 得到同后台交互的报表数据参数对象，此方法被IUFORepDataEditor类使用,做成包级私有
	 *
	 * @return
	 */
	IRepDataParam getRepDataParam(){
		return AbsRepDataEditor.appendRepDataParamDSInfo(getMainboard(),m_repDataParam, m_pubData);
	}

	/**
	 * 初始化TabbedPane的方法
	 */
	private UITabbedPane initTabbedPane(){
		UITabbedPane tabPane = new ExKTabbedPane(new Color(183, 181, 175),new Color(0, 0, 0), new Color(30, 30, 200));
		tabPane.setBorder(LineBorder.createGrayLineBorder());
		tabPane.addChangeListener(new ChangeListener(){
			/**
			 * 主TabPane页签署切换的事件响应
			 * @param e
			 */
			@Override
			public void stateChanged(final ChangeEvent e){
				// 判断TabPane是否被初始化过，没有初始化过，事件不作响应
				if (AbsCombRepDataEditor.this.m_bInitedTabPane == false)
					return;

				final IRepDataEditorInComb oldEditor = getActiveRepDataEditor();

				//当从管理报告页签切换到其他页签时，判断是否需要上传
				if(oldEditor instanceof UfoeRepFinalFileViewer){
					((UfoeRepFinalFileViewer)oldEditor).tabChange();
				}

				JTabbedPane tabPane = (JTabbedPane) e.getSource();
				Component comp = tabPane.getSelectedComponent();
				if (comp == null)
					return;

				// 报表分组时，取子TabbedPane中已经选中的Editor
				if (comp instanceof IRepDataEditorInComb == false && comp instanceof JTabbedPane){
					// 取报表分组中的某一Editor
					JTabbedPane subPane = (JTabbedPane) comp;

					// 取得以前选中的页签，以前没有选取中，则取第一个页签
					comp = subPane.getSelectedComponent();
					if (comp == null){
						comp = subPane.getComponentAt(0);
					}

					// 加载报表数据
					subPane.setSelectedComponent(comp);
				}

				loadActiveEditor();
				final IRepDataEditorInComb newEditor = getActiveEditor();
				final Mainboard mainBoard = getMainboard();

				// 判断editor的CellsModel是否需要从数据库中加载，如果需要，则走线程
				if (newEditor.isNeedLoadCellsModelFromDB(true) == false){
					innerOnSubTabChange(oldEditor, newEditor);
				} else{
					(mainBoard.getStatusBar()).processDisplay(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0000")/*@res "系统正在处理请求, 请稍候..."*/,
						new Runnable(){
							@Override
							public void run(){
								innerOnSubTabChange(oldEditor, newEditor);
								}
							}
					);
				}
			}
		});

		return tabPane;
	}

	/**
	 * 添加各子TabPane的事件响应
	 */
	protected void addSubTabPaneListener(){
		for (int i=0;i<m_tabPane.getTabCount();i++){
			Component comp=m_tabPane.getComponentAt(i);
			if (comp instanceof JTabbedPane==false)
				continue;

			JTabbedPane subTabPane=(JTabbedPane)comp;
			// 子TabPane的事件响应，直接打开报表数据
			subTabPane.addChangeListener(new ChangeListener(){
				@Override
				public void stateChanged(final ChangeEvent e){
					if (m_bInitedTabPane == false)
						return;

					final Mainboard mainBoard = getMainboard();
					final IRepDataEditorInComb oldEditor = getActiveRepDataEditor();
					JTabbedPane tabPane = (JTabbedPane) e.getSource();
					if(!(tabPane.getSelectedComponent() instanceof IRepDataEditorInComb )){
						return ;
					}
					final IRepDataEditorInComb newEditor = (IRepDataEditorInComb) tabPane.getSelectedComponent();

					loadActiveEditor();
					// 判断editor的CellsModel是否需要从数据库中加载，如果需要，则走线程
					if (newEditor.isNeedLoadCellsModelFromDB(true) == false){
						innerOnSubTabChange(oldEditor, newEditor);
					} else{
						(mainBoard.getStatusBar()).processDisplay(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0000")/*@res "系统正在处理请求, 请稍候..."*/,
							new Runnable(){
								@Override
								public void run(){
									innerOnSubTabChange(oldEditor,newEditor);
								}
							}
						);
					}
				}
			});
		}
	}

	/**
	 * 响应子TabbedPane页签选择改变的方法
	 *
	 * @param oldEditor
	 * @param newEditor
	 */
	private void innerOnSubTabChange(IRepDataEditorInComb oldEditor,IRepDataEditorInComb newEditor){
		try{
			// 是否需要加载模型
			boolean needLoadData = newEditor.isNeedLoadCellsModelFromDB(true);
			innerReInitOneEditor(newEditor, true);
			if (!needLoadData) {
				// 如果不需要加载则返回
				return;
			}
			// 如果需要加载数据，则判断是否进行动态区展示处理
			//20130121 wangqi 切换页签看是否为动态区展示态
			CellsModel cellsModel = newEditor.getCellsModel();
			if (cellsModel != null) {
				DynamicAreaModel dynAreaModel = DynamicAreaModel.getInstance(cellsModel);
				ExtendAreaCell[] cells = dynAreaModel.getDynAreaCells();
				if (cells != null && cells.length > 0) {
					//动态区是否录入数据
//					boolean inputflg = false;
//					for (int i = 0; i < cells.length; i++) {
//						DynAreaProvider provider = (DynAreaProvider) cells[i].getAreaInfoSet().getSmartModel().getProviders()[0];
//						Object[][] datas = provider.getDisplayDataSet().getDatas();
//						// @edit by wuyongc at 2013-6-6,上午11:35:03
//						if (datas != null && datas.length!=0 && datas[0] != null && datas[0].length>0 && datas[0][0] != null) {
//							inputflg = true;
//							break;
//						}
//					}
					//已经录入了并且没有进行过展示
					if (newEditor.getMenuState() != null) {
						//报表是否有修改权限
						if (((Integer) newEditor.getContext().getAttribute(IUfoContextKey.DATA_RIGHT)) != IUfoContextKey.RIGHT_DATA_WRITE ||
								(newEditor.getMenuState() != null && !newEditor.getMenuState().isM_bCanInput())){
							try{
								ReportShowVO reportShowVO = ReportBO_Client.loadRepShowCellsModel(reppk);
								//是否进行过展示设计
								if (reportShowVO != null) {
									
									boolean showflg = false;
									CellsModel cellsModelShow = (CellsModel)reportShowVO.getRepinfo();
									DynamicAreaModel dynAreaModelShow = DynamicAreaModel.getInstance(cellsModelShow);
									ExtendAreaCell[] extendAreaCellsShowNew = dynAreaModelShow.getDynAreaCells();
									if (extendAreaCellsShowNew != null && extendAreaCellsShowNew.length > 0) {
										for (int i = 0; i < extendAreaCellsShowNew.length; i++) {
											//进行过交叉和展示设计的不进行自动同步，否则自动同步
											if (extendAreaCellsShowNew[i].getAreaInfoSet().getAreaLevelInfo("sort") != null ||
													extendAreaCellsShowNew[i].getAreaInfoSet().getCrossSetInfo() != null) {
												showflg = true;
												break;
											}
										}
									}

									if (showflg) {
										List<JComponent> list = getMainboard().getPluginManager().getPluginComponents(IUfoInputDataPlugin.class.getName());
										if(list != null) {
											for (JComponent jComponent : list) {
												if (jComponent instanceof KMenuItem) {
													Action action = ((KMenuItem)jComponent).getAction();
													if (DynAreaShowExt.class.getName().equals(((PluginActionAdapter)action).getInterPluginAction().getClass().getName())) {
														ActionEvent actionEvent=new ActionEvent(this,-1,nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820004-0030")/*@res "动态区展示"*/);
														action.actionPerformed(actionEvent);
														break;
													}
												}
											}
										}
									}
								}
							}catch(Exception ex){
								AppDebug.debug(ex);
							}
						}
					}
				}
			}
		} finally{
			loadActiveEditor();
			RepDataTabActiveEvent event = new RepDataTabActiveEvent(oldEditor,getActiveRepDataEditor());
			getMainboard().getEventManager().dispatch(event);
		}
	}

	/**
	 * 重新初始化各Editor的状态为未打开状态
	 */
	private void reInitAllTabEditors(){
		IRepDataEditorInComb[] editors = getAllRepDataEditors();
		for (IRepDataEditorInComb editor : editors){
			innerReInitOneEditor(editor, false);
		}
	}

	/**
	 * 得到当前CombEditor中所有的子Editor
	 *
	 * @return
	 */
	public IRepDataEditorInComb[] getAllRepDataEditors(){
		List<IRepDataEditorInComb> vEditor = new ArrayList<IRepDataEditorInComb>();
		for (int i = 0; i < m_tabPane.getTabCount(); i++){
			Component comp = m_tabPane.getComponentAt(i);
			if (comp instanceof JTabbedPane){
				JTabbedPane subTabPane = (JTabbedPane) comp;
				for (int j = 0; j < subTabPane.getTabCount(); j++){
					if(subTabPane.getComponentAt(j) instanceof IRepDataEditorInComb){
						IRepDataEditorInComb editor = (IRepDataEditorInComb) subTabPane.getComponentAt(j);
						vEditor.add(editor);
					}
					
				}
			}  else if(comp instanceof IRepDataEditorInComb){
				IRepDataEditorInComb editor = (IRepDataEditorInComb) comp;
				vEditor.add(editor);
			}
		}

		IRepDataEditorInComb[] editors=vEditor.toArray(new IRepDataEditorInComb[0]);
		IRepDataEditorInComb[] retEditors=(IRepDataEditorInComb[])Array.newInstance(editors.getClass().getComponentType(),editors.length);
		System.arraycopy(editors, 0, retEditors, 0, editors.length);
		return retEditors;
	}

	/**
	 * 得到当前活跃的子Editor
	 *
	 * @return
	 */
	protected IRepDataEditorInComb getActiveEditor(){
		if (m_activeEditor == null)
			loadActiveEditor();
		return m_activeEditor;
	}

	private void loadActiveEditor(){
		Component comp = m_tabPane==null?null:m_tabPane.getSelectedComponent();
		if (comp == null){
			m_activeEditor = null;
			return;
		}

		if (comp instanceof IRepDataEditorInComb){
			m_activeEditor = (IRepDataEditorInComb) comp;
			return;
		}

		if(comp instanceof JTabbedPane){
			JTabbedPane tabPane = (JTabbedPane) comp;
			comp = tabPane.getSelectedComponent();
			m_activeEditor = (IRepDataEditorInComb) comp;
		}
	}

	/**
	 * 激活某一Editor的方法，
	 * @param editor
	 * @param bOpenData，是否需要打开报表数据
	 */
	private void innerReInitOneEditor(IRepDataEditorInComb editor,boolean bOpenData){
		// 调用IUFORepDataEditor的方法加载数据
		editor.reInitContent(bOpenData);

		// 记录各任务的最后一次打开的报表PK，
		if (bOpenData){
			AbsCombRepDataControler controler = (AbsCombRepDataControler) AbsRepDataControler.getInstance(getMainboard());
			if(controler != null){
				controler.setTaskActiveRepPK(m_repDataParam.getTaskPK(), editor.getRepPK());
			}
			
		}

		//TODO  editor 可能是任务批注的editor
		String keygroupPK = editor.getPubData().getKType();
		// 设置关键字面板对应的报表PK
		m_keyCondPane.setRepPK(editor.getRepPK());
		m_keyCondPane.setKeygroupPk(keygroupPK);

		//如果没有CellsModel，则认为为非报表面板，而是类似于 任务审批这样的编辑器。
		m_keyCondPane.setRepFocus(editor.getCellsModel() != null);
		reppk = editor.getRepPK();
		
		//add by tanyj at 2013-08-31 context的key_report_pk获取的报表PK是任务中第一张报表的pk，增加一条属性以便获取到当前editor中的报表PK
		getContext().setAttribute(IUfoContextKey.KEY_CURREDITOR_REPPK, reppk);
	}

	@Override
	protected void initContext(){
		IContext context = getContext();
		if (context != null){
			super.initContext();
			TaskCommitControler.removeFromContext(context);
			RDPrintControler.removeFromContext(context);
		}
	}

	// @edit by wuyongc at 2012-4-1,下午3:12:09 修改数据模型的刷新策略. 企业报表是全部填充.
	 @Override
	public void refreshDataModel(boolean reload){
		  if (reload) {
		   FreeDSStateUtil.setReportExecAll(getContext(), true);// 外部发起的刷新，除非指定都作为整表查询处理
		  }
		  FreePolicy policy = FreePolicyFactory.getRefreshPolicy(reload);
		  policy.setPaginalRows(FreePolicyFactory.PAGINAL_DATAROW_COUNT, 256*256);
		  refreshDataState(policy, getTable());
	}

	/**
	 * 设置Context,公式追踪用到
	 *
	 * @param context
	 * @param repDataParam
	 */
	private void doSetContext(IContext context, IRepDataParam repDataParam){
		AbsRepDataEditor.doSetContext(context, repDataParam, null);
	}

	public String getBalCondPK() {
		return m_strBalCondPK;
	}

	public void setBalCondPK(String balCondPK) {
		m_strBalCondPK = balCondPK;
	}

	public CheckResultVO[] getTaskCheckResults() {
		return m_taskCheckResults;
	}

	public void setTaskCheckResults(CheckResultVO[] taskCheckResults) {
		this.m_taskCheckResults = taskCheckResults;
	}

	public CheckDetailVO getTaskCheckDetail() {
		return m_taskCheckDetail;
	}

	public void setTaskCheckDetail(CheckDetailVO taskCheckDetail) {
		this.m_taskCheckDetail = taskCheckDetail;
	}

	public Color getTaskCheckColor() {
		return m_taskCheckColor;
	}

	public void setTaskCheckColor(Color taskCheckColor) {
		this.m_taskCheckColor = taskCheckColor;
	}

	public List<CellPosition> getTaskCheckCells() {
		return m_taskCheckCells;
	}

	public void setTaskCheckCells(List<CellPosition> taskCheckCells) {
		this.m_taskCheckCells = taskCheckCells;
	}

	public TaskVO getTask() {
		return task;
	}

	public void setTask(TaskVO task) {
		this.task = task;
	}

	public boolean ismodified() {
		return ismodified;
	}

	public void setIsmodified(boolean ismodified) {
		this.ismodified = ismodified;
	}

	public String getReppk() {
		return reppk;
	}
	public ITaskQueryService getTaskQueryService(){
		taskQueryService=(ITaskQueryService)NCLocator.getInstance().lookup(ITaskQueryService.class.getName());
		return taskQueryService;
	}
	public void setReppk(String reppk) {
		this.reppk = reppk;
		//add by tanyj at 2013-08-31 context的key_report_pk获取的报表PK是任务中第一张报表的pk，增加一条属性以便获取到当前editor中的报表PK
		getContext().setAttribute(IUfoContextKey.KEY_CURREDITOR_REPPK, reppk);
	}
	
	public String getReportId() {
		return null;
	}
}