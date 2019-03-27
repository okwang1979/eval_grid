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

	// TabPane�Ƿ��ʼ����
	protected boolean m_bInitedTabPane = false;

	protected IRepDataEditorInComb m_activeEditor = null;

	//�����˽��
	protected CheckResultVO[] m_taskCheckResults = null;

	// �û�ѡ��׷�ٵ���������˽��
	protected CheckDetailVO m_taskCheckDetail =null;

	// ��˵�Ԫ����Ҫ��ע����ɫ
	protected Color m_taskCheckColor =null;

	// ��˽��׷�ٶ�λ���ĵ�Ԫ���б�ֻ��Ӧһ���������ݴ���
	protected List<CellPosition> m_taskCheckCells =null;

	//��ǰ��Ӧ����λ����PK
	protected String m_strBalCondPK=BalanceCondVO.NON_SW_DATA_COND_PK;

	protected TaskVO task = null;

	protected String taskpk = null;

//	private List<RepCommitStateVO> vInputResult = null;

	protected boolean ismodified = false;

	protected String reppk = null;
	
	protected ITaskQueryService taskQueryService=null;
	/**
	 * ������TabPane���ķ���
	 */
	abstract protected void geneTabPaneEditors();

	/**
	 * ��λʱ����Ҫ��������������б�����������ʾ
	 * @return
	 */
	abstract protected String getAlertWhenNeedSave();

	/**
	 * ��������ͼ����
	 */
	abstract protected void doSetTitle();

	/**
	 * ��ʼ���ؼ���¼������ֵ
	 */
	abstract protected void initKeyValues(String[] strKeyVals);

	abstract protected KeyGroupVO getKeyGroup();

	/**
	 * ���ɹؼ������
	 */
	abstract protected AbsKeyCondPanel createKeyCondPanel(String strOrgPK,ChangeKeywordsData[] changeKeywordsDatas,String strRepPK,String strRMSPK);
//	abstract protected void repaintKeyCondPanel(String strOrgPK,ChangeKeywordsData[] changeKeywordsDatas,String strRepPK,String strRMSPK);
	public AbsCombRepDataEditor(){
		super();
	}

	/*************** Begin:ģ��AbsAnaReportDesigner��һ�鷽�� ********************/
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

		// ���ձ���ȫ���˵���������Ծ���ڷ��������
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
		
		//wangqi 20130503 �رմ򿪵���ͼʱҪ�����̬��չʾ��map
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
		//ֻ�м��ǰҳǩʱˢ����Ч
		if(getActiveRepDataEditor() != null && getActiveRepDataEditor().getAloneID() != null){
			refreshContent(m_strBalCondPK);
			if(getActiveRepDataEditor() instanceof AbsNotRepDataEditor){
				return;
			}
			
			//wangqi 20130731 ˢ�º������趨��ʾ����
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

			// add by yuyangi ����ɷ��¼�
			PluginActionEvent event=new PluginActionEvent(this,0);
			getEventManager().dispatch(event);
		}
	}

	/*************** End:ģ��AbsAnaReportDesigner��һ�鷽�� ********************/

	/*************** Begin:ʵ��AbsBaseRepDataEditor���һ�鷽�� **********/
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
	 * ������PK����ĳһEditor
	 *
	 * @param strRepPK
	 */
	@Override
	public boolean activeOneRepDataEditor(String strRepPK){
		try{
			// ���RepPKΪ�գ��򼤻��һ������
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
							// �򿪱������ݣ�����TabPane����TabPane��ѡ��ҳǩ
							innerReInitOneEditor(editor, true);
							// ��ѡ����ҳǩ����ҳǩ���¼����������̲߳�������initTabbedPane����
							subTabPane.setSelectedIndex(j);
							m_tabPane.setSelectedIndex(i);
							return true;
						}
						//wangqi 20120816 ��ֹû�б���鿴Ȩ��ʱ��editor.getRepPK()Ϊ�գ�������ѡ����֯��ѯʱ��
						//����innerReInitOneEditor��m_keyCondPane�趨keygroupPK����MeasureDataUtil-getAloneID�л����
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
					//wangqi 20120816 ��ֹû�б���鿴Ȩ��ʱ��editor.getRepPK()Ϊ�գ�������ѡ����֯��ѯʱ��
					//����innerReInitOneEditor��m_keyCondPane�趨keygroupPK����MeasureDataUtil-getAloneID�л����
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
			//�ϱ������沢�ϱ����ɹ�ʱ���ָ�Ϊδ����״̬��
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

			//��ֹ����refreshContent()�������ٴγ����Ƿ񱣴��ȷ����Ϣ
			activeEditor.getCellsModel().setDirty(false);
		}
//		if (isDirty()){
////			int iResult = UIUtilities.showConfirmDialog(getMainboard(),nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0410")/*@res "�Ƿ񱣴汨�����ݣ���������棬�˴α��������޸Ŀ��ܶ�ʧ"*/,
////					NCLangRes.getInstance().getStrByID("20090618", "upp09061800031"),JOptionPane.YES_NO_OPTION);
//			int iResult = UIUtilities.showConfirmDialog(getMainboard(),nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0410")/*@res "�Ƿ񱣴汨�����ݣ���������棬�˴α��������޸Ŀ��ܶ�ʧ"*/,
//					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413001_0","01413001-0031")/*@res "ȷ��"*/,JOptionPane.YES_NO_OPTION);
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
				// �ֲ�ʽ�ϴ��ı������ݲ����޸�
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
		// modify by yuyangi ��ˢ�µ�����ɷ��¼���
//		PluginActionEvent event=new PluginActionEvent(this,0);
//		getEventManager().dispatch(event);
	}
	
	//wangqi 2014015 �������أ���Ϊ�ϲ�����д��ԭresetCommitMenuState����
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
				// �ֲ�ʽ�ϴ��ı������ݲ����޸�
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
					// @edit by zhoushuang at 2014-1-9,����6:44:02 ���ݱ�������״̬�ж�cells�Ƿ���޸�
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
		// modify by yuyangi ��ˢ�µ�����ɷ��¼���
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
	 * ��������ʽ�򿪹�ʽ������
	 * creator tanyj
	 */
	@Override
	protected FmlManagePane getFmlManagePane(FormulaManageViewer viewer,
			CellsPane cellsPane, CellsModel cellsModel, IContext context,
			boolean isFmtDesign,Mainboard mainboard) {
		return new UfoeFmlManagePane(null, cellsPane, getCellsModel(), context, isFmtDesign,mainboard);
	}

	/*************** End:ʵ��AbsBaseRepDataEditor���һ�鷽�� **********/

	/**
	 * �Ա������ݽ���ˢ�£�������λ�����л����ݵķ���
	 * @param strNewBalCondPK
	 */
	@Override
	public boolean refreshContent(String strNewBalCondPK){
		if (strNewBalCondPK == null)
			strNewBalCondPK = BalanceCondVO.NON_SW_DATA_COND_PK;

		// ��ʾ���������Ƿ񱣴�Ĵ����ڱ������н���
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
		} else{// ��Ӧ��λ
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

		// �����µ���λ����PK�����Ե�ǰ��ԾEditor������λ
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
		// 2,�õ��ؼ��ֵĳ�ʼֵ
		String[] strKeyInitValues = null;
		try{
			strKeyInitValues = KeyCondPaneUtil.getKeyValues(mainBoard, keyVOs,pubData, AbsRepDataControler.getInstance(mainBoard).getLoginEnv(mainBoard).getCurLoginDate());
			initKeyValues(strKeyInitValues);
		} catch (Exception e){
			AppDebug.debug(e);
		}

		String strRmsPK = (String) getContext().getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK);
		String strOrgPK = (String) getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);

		// #�õ��л��ؼ��ֵĽ�����ʾ��Ҫ�����ݶ���
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
//		// 2,�õ��ؼ��ֵĳ�ʼֵ
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
//		// #�õ��л��ؼ��ֵĽ�����ʾ��Ҫ�����ݶ���
////		ChangeKeywordsData[] datas = (ChangeKeywordsData[]) ActionHandler.exec(IUfoRepDataActionHandler.class.getName(),"geneChangeKeywordsDatas", new Object[] { keyVOs,strKeyInitValues, false, strOrgPK });
//		ChangeKeywordsData[] datas = TableInputOperUtil.geneChangeKeywordsDatas(keyVOs, strKeyInitValues, false, strOrgPK);
//		repaintKeyCondPanel((String) getMainboard().getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK), datas, strRepPK, strRmsPK);
//	}

	/**
	 * ���³�ʼ������
	 * @i18n miufohbbb00125=�򿪱���ʧ��
	 * @i18n miufohbbb00126=����ѡ����ѯ������δ��ѯ���������ݣ���ȷ���ڴ˹ؼ����������Ƿ���¼�����ݻ��Ƿ�������в���Ȩ��
	 */
	@Override
	public void reInitContent(){
		try{
			String strPrevAloneID = m_repDataParam.getAloneID();
			String strPrevTaskPK = m_repDataParam.getTaskPK();
			MeasurePubDataVO prevPubData = m_repDataParam.getPubData();

			//��λ�ؼ���
			String strUnitPK = null;
			if(prevPubData != null)
				strUnitPK = prevPubData.getUnitPK();

			// ��ʼ��RepDataParam
			initRepDataParam();
//			doSetTitle();

			// �ж������Ƿ����˸ı�
			if (UfoPublic.strIsEqual(m_repDataParam.getTaskPK(), strPrevTaskPK)){
				// �ж������Ƿ�Ϊ��,��ʼ��δѡ������ʱ����ʱ����PKΪ�գ���Ӧ�ü���TabPane
				if (m_tabPane.getTabCount() <= 0){
					geneTabPaneEditors();
					addSubTabPaneListener();
				}

				// �жϹؼ���ֵ�Ƿ����˱仯,û�з����仯����ֱ���˳�
				if (!StringUtil.isEmptyWithTrim(m_repDataParam.getReportPK())
						&& UfoPublic.strIsEqual(m_repDataParam.getAloneID(),strPrevAloneID)
						&& (m_repDataParam.getPubData() == null || m_repDataParam.getAloneID() != null || m_repDataParam.getPubData().equals(prevPubData))){
					return;
				} else{
					// �������ɹؼ������
					reInitKeyCondPane(m_repDataParam.getReportPK());

					//�жϵ�λ�ؼ��֣������仯����������ҳǩ�������ʼ��ҳǩ add by jiaah at 20110615
					if(isFilterRepTabs() && strUnitPK != null && !m_repDataParam.getPubData().getUnitPK().equals(strUnitPK)){
						// �������ɸ���Editor
						geneTabPaneEditors();
						addSubTabPaneListener();
					}
					else{
						// �ؼ������������仯�������ø�ҳǩEditor
						reInitAllTabEditors();
					}
				}
			} else{
				// �������ɹؼ������
				reInitKeyCondPane(m_repDataParam.getReportPK());
				// �������ɸ���Editor
				geneTabPaneEditors();
				addSubTabPaneListener();
			}
			doSetTitle();
		} finally{
			// �����û�ѡ�еı���PK,�򿪱���
			doSetContext(getContext(), m_repDataParam);
			activeOneRepDataEditor(m_repDataParam.getReportPK());
			//wangqi ��AbsNavUnitTreeViewer�����¼���editor�����ɵĹؼ������
			Mainboard mainBoard = getMainboard();
			AbsNavUnitTreeViewer treeViewer = (AbsNavUnitTreeViewer)mainBoard.getView(AbsNavUnitTreeViewer.NAV_UNIT_TREE_ID);
			if (treeViewer != null) {
				treeViewer.reInitKeyCondPane(m_repDataParam.getReportPK());
			}
			AbsRepDataControler.getInstance(getMainboard()).setLastActiveRepDataEditor(this);
		}
	}

	/**
	 * �Ƿ���˱���ҳǩ
	 * @create by jiaah at 2011-6-15,����05:28:52
	 * @return
	 */
	protected boolean isFilterRepTabs(){
		return false;
	}

	public String getTaskPK(){
		return m_repDataParam.getTaskPK();
	}

	/**
	 * �õ�ͬ��̨�����ı������ݲ������󣬴˷�����IUFORepDataEditor��ʹ��,���ɰ���˽��
	 *
	 * @return
	 */
	IRepDataParam getRepDataParam(){
		return AbsRepDataEditor.appendRepDataParamDSInfo(getMainboard(),m_repDataParam, m_pubData);
	}

	/**
	 * ��ʼ��TabbedPane�ķ���
	 */
	private UITabbedPane initTabbedPane(){
		UITabbedPane tabPane = new ExKTabbedPane(new Color(183, 181, 175),new Color(0, 0, 0), new Color(30, 30, 200));
		tabPane.setBorder(LineBorder.createGrayLineBorder());
		tabPane.addChangeListener(new ChangeListener(){
			/**
			 * ��TabPaneҳǩ���л����¼���Ӧ
			 * @param e
			 */
			@Override
			public void stateChanged(final ChangeEvent e){
				// �ж�TabPane�Ƿ񱻳�ʼ������û�г�ʼ�������¼�������Ӧ
				if (AbsCombRepDataEditor.this.m_bInitedTabPane == false)
					return;

				final IRepDataEditorInComb oldEditor = getActiveRepDataEditor();

				//���ӹ�����ҳǩ�л�������ҳǩʱ���ж��Ƿ���Ҫ�ϴ�
				if(oldEditor instanceof UfoeRepFinalFileViewer){
					((UfoeRepFinalFileViewer)oldEditor).tabChange();
				}

				JTabbedPane tabPane = (JTabbedPane) e.getSource();
				Component comp = tabPane.getSelectedComponent();
				if (comp == null)
					return;

				// �������ʱ��ȡ��TabbedPane���Ѿ�ѡ�е�Editor
				if (comp instanceof IRepDataEditorInComb == false && comp instanceof JTabbedPane){
					// ȡ��������е�ĳһEditor
					JTabbedPane subPane = (JTabbedPane) comp;

					// ȡ����ǰѡ�е�ҳǩ����ǰû��ѡȡ�У���ȡ��һ��ҳǩ
					comp = subPane.getSelectedComponent();
					if (comp == null){
						comp = subPane.getComponentAt(0);
					}

					// ���ر�������
					subPane.setSelectedComponent(comp);
				}

				loadActiveEditor();
				final IRepDataEditorInComb newEditor = getActiveEditor();
				final Mainboard mainBoard = getMainboard();

				// �ж�editor��CellsModel�Ƿ���Ҫ�����ݿ��м��أ������Ҫ�������߳�
				if (newEditor.isNeedLoadCellsModelFromDB(true) == false){
					innerOnSubTabChange(oldEditor, newEditor);
				} else{
					(mainBoard.getStatusBar()).processDisplay(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0000")/*@res "ϵͳ���ڴ�������, ���Ժ�..."*/,
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
	 * ��Ӹ���TabPane���¼���Ӧ
	 */
	protected void addSubTabPaneListener(){
		for (int i=0;i<m_tabPane.getTabCount();i++){
			Component comp=m_tabPane.getComponentAt(i);
			if (comp instanceof JTabbedPane==false)
				continue;

			JTabbedPane subTabPane=(JTabbedPane)comp;
			// ��TabPane���¼���Ӧ��ֱ�Ӵ򿪱�������
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
					// �ж�editor��CellsModel�Ƿ���Ҫ�����ݿ��м��أ������Ҫ�������߳�
					if (newEditor.isNeedLoadCellsModelFromDB(true) == false){
						innerOnSubTabChange(oldEditor, newEditor);
					} else{
						(mainBoard.getStatusBar()).processDisplay(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0000")/*@res "ϵͳ���ڴ�������, ���Ժ�..."*/,
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
	 * ��Ӧ��TabbedPaneҳǩѡ��ı�ķ���
	 *
	 * @param oldEditor
	 * @param newEditor
	 */
	private void innerOnSubTabChange(IRepDataEditorInComb oldEditor,IRepDataEditorInComb newEditor){
		try{
			// �Ƿ���Ҫ����ģ��
			boolean needLoadData = newEditor.isNeedLoadCellsModelFromDB(true);
			innerReInitOneEditor(newEditor, true);
			if (!needLoadData) {
				// �������Ҫ�����򷵻�
				return;
			}
			// �����Ҫ�������ݣ����ж��Ƿ���ж�̬��չʾ����
			//20130121 wangqi �л�ҳǩ���Ƿ�Ϊ��̬��չʾ̬
			CellsModel cellsModel = newEditor.getCellsModel();
			if (cellsModel != null) {
				DynamicAreaModel dynAreaModel = DynamicAreaModel.getInstance(cellsModel);
				ExtendAreaCell[] cells = dynAreaModel.getDynAreaCells();
				if (cells != null && cells.length > 0) {
					//��̬���Ƿ�¼������
//					boolean inputflg = false;
//					for (int i = 0; i < cells.length; i++) {
//						DynAreaProvider provider = (DynAreaProvider) cells[i].getAreaInfoSet().getSmartModel().getProviders()[0];
//						Object[][] datas = provider.getDisplayDataSet().getDatas();
//						// @edit by wuyongc at 2013-6-6,����11:35:03
//						if (datas != null && datas.length!=0 && datas[0] != null && datas[0].length>0 && datas[0][0] != null) {
//							inputflg = true;
//							break;
//						}
//					}
					//�Ѿ�¼���˲���û�н��й�չʾ
					if (newEditor.getMenuState() != null) {
						//�����Ƿ����޸�Ȩ��
						if (((Integer) newEditor.getContext().getAttribute(IUfoContextKey.DATA_RIGHT)) != IUfoContextKey.RIGHT_DATA_WRITE ||
								(newEditor.getMenuState() != null && !newEditor.getMenuState().isM_bCanInput())){
							try{
								ReportShowVO reportShowVO = ReportBO_Client.loadRepShowCellsModel(reppk);
								//�Ƿ���й�չʾ���
								if (reportShowVO != null) {
									
									boolean showflg = false;
									CellsModel cellsModelShow = (CellsModel)reportShowVO.getRepinfo();
									DynamicAreaModel dynAreaModelShow = DynamicAreaModel.getInstance(cellsModelShow);
									ExtendAreaCell[] extendAreaCellsShowNew = dynAreaModelShow.getDynAreaCells();
									if (extendAreaCellsShowNew != null && extendAreaCellsShowNew.length > 0) {
										for (int i = 0; i < extendAreaCellsShowNew.length; i++) {
											//���й������չʾ��ƵĲ������Զ�ͬ���������Զ�ͬ��
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
														ActionEvent actionEvent=new ActionEvent(this,-1,nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820004-0030")/*@res "��̬��չʾ"*/);
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
	 * ���³�ʼ����Editor��״̬Ϊδ��״̬
	 */
	private void reInitAllTabEditors(){
		IRepDataEditorInComb[] editors = getAllRepDataEditors();
		for (IRepDataEditorInComb editor : editors){
			innerReInitOneEditor(editor, false);
		}
	}

	/**
	 * �õ���ǰCombEditor�����е���Editor
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
	 * �õ���ǰ��Ծ����Editor
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
	 * ����ĳһEditor�ķ�����
	 * @param editor
	 * @param bOpenData���Ƿ���Ҫ�򿪱�������
	 */
	private void innerReInitOneEditor(IRepDataEditorInComb editor,boolean bOpenData){
		// ����IUFORepDataEditor�ķ�����������
		editor.reInitContent(bOpenData);

		// ��¼����������һ�δ򿪵ı���PK��
		if (bOpenData){
			AbsCombRepDataControler controler = (AbsCombRepDataControler) AbsRepDataControler.getInstance(getMainboard());
			if(controler != null){
				controler.setTaskActiveRepPK(m_repDataParam.getTaskPK(), editor.getRepPK());
			}
			
		}

		//TODO  editor ������������ע��editor
		String keygroupPK = editor.getPubData().getKType();
		// ���ùؼ�������Ӧ�ı���PK
		m_keyCondPane.setRepPK(editor.getRepPK());
		m_keyCondPane.setKeygroupPk(keygroupPK);

		//���û��CellsModel������ΪΪ�Ǳ�����壬���������� �������������ı༭����
		m_keyCondPane.setRepFocus(editor.getCellsModel() != null);
		reppk = editor.getRepPK();
		
		//add by tanyj at 2013-08-31 context��key_report_pk��ȡ�ı���PK�������е�һ�ű����pk������һ�������Ա��ȡ����ǰeditor�еı���PK
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

	// @edit by wuyongc at 2012-4-1,����3:12:09 �޸�����ģ�͵�ˢ�²���. ��ҵ������ȫ�����.
	 @Override
	public void refreshDataModel(boolean reload){
		  if (reload) {
		   FreeDSStateUtil.setReportExecAll(getContext(), true);// �ⲿ�����ˢ�£�����ָ������Ϊ�����ѯ����
		  }
		  FreePolicy policy = FreePolicyFactory.getRefreshPolicy(reload);
		  policy.setPaginalRows(FreePolicyFactory.PAGINAL_DATAROW_COUNT, 256*256);
		  refreshDataState(policy, getTable());
	}

	/**
	 * ����Context,��ʽ׷���õ�
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
		//add by tanyj at 2013-08-31 context��key_report_pk��ȡ�ı���PK�������е�һ�ű����pk������һ�������Ա��ȡ����ǰeditor�еı���PK
		getContext().setAttribute(IUfoContextKey.KEY_CURREDITOR_REPPK, reppk);
	}
	
	public String getReportId() {
		return null;
	}
}