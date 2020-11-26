package nc.ui.tb.zior;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nc.bs.framework.common.NCLocator;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.mdm.cube.IDataSetService;
import nc.itf.tb.plan.IZiorFrameModelService;
import nc.ms.mdm.cube.CubeServiceGetter;
import nc.ms.tb.ext.plan.TbCompliePlanConst;
import nc.ms.tb.ext.zior.xml.ZiorFrameCtl;
import nc.ms.tb.form.SheetGroupCtl;
import nc.ms.tb.formula.script.TbbFormulaExecuteLogs;
import nc.ms.tb.plan.GZWCoverServiceGetter;
import nc.ms.tb.pub.TbUserProfileCtl;
import nc.ms.tb.task.TaskActionCtl;
import nc.ms.tb.task.TaskExcelObjectConvertor;
import nc.ms.tb.task.TaskExtInfoLoader;
import nc.ms.tb.task.TbTaskExtCtl;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ms.tb.task.data.TaskSheetDataModel;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.ZiorCoverVO;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIDialog;
import nc.ui.tb.plan.action.ApprovePlanTools;
import nc.ui.tb.zior.pluginaction.check.PlanCheckResultViewer;
import nc.ui.tb.zior.pluginaction.edit.DataCellFroozeUtil;
import nc.ui.tb.zior.pluginaction.edit.PluginAction_InputDownDirect;
import nc.ui.tb.zior.pluginaction.edit.PluginAction_InputLeftDirect;
import nc.ui.tb.zior.pluginaction.edit.PluginAction_InputRightDirect;
import nc.ui.tb.zior.pluginaction.edit.PluginAction_InputUpDirect;
import nc.ui.tb.zior.pluginaction.edit.pageaction.AddLinePageDwonAction;
import nc.ui.tb.zior.pluginaction.edit.pageaction.AddLinePageUpAction;
import nc.ui.tb.zior.pluginaction.edit.pageaction.AddMultiLinePageAction;
import nc.ui.tb.zior.pluginaction.edit.pageaction.DelLinePageAction;
import nc.ui.tb.zior.pluginaction.edit.pageaction.FullContentPageAction;
import nc.view.tb.form.iufo.TbDefaultSheetCellRender;
import nc.view.tb.form.iufo.TbReportFactory;
import nc.view.tb.plan.IndexAppUiCtl;
import nc.view.tb.plan.IndexApproveDlg;
import nc.view.tb.plan.adj.MutiRowCellRenderer;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.cube.DimVector;
import nc.vo.mdm.cube.ICubeDataSet;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.tb.form.MdSheet;
import nc.vo.tb.form.excel.ExSheetCheckFml;
import nc.vo.tb.form.iufo.CellExtInfo;
import nc.vo.tb.form.iufo.TbIufoConst;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.plan.WrappedDataCell;
import nc.vo.tb.pubutil.BusiTermConst;
import nc.vo.tb.pubutil.IBusiTermConst;
import nc.vo.tb.pubutil.UISharedData;
import nc.vo.tb.rule.CheckRuleResultVO;
import nc.vo.tb.task.ITaskAction;
import nc.vo.tb.task.ITaskConst;
import nc.vo.tb.task.IndexAppdetailVO;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;
import nc.vo.tb.task.TaskLayoutRecord;
import nc.vo.tb.util.PubUtil;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.zior.comp.KTabbedPane;
import com.ufida.zior.docking.core.Dockable;
import com.ufida.zior.docking.core.DockingConstants;
import com.ufida.zior.docking.core.DockingManager;
import com.ufida.zior.docking.core.defaults.DockingSplitPane;
import com.ufida.zior.docking.core.state.DockingState;
import com.ufida.zior.docking.view.DockingViewport;
import com.ufida.zior.docking.view.actions.OpenViewAction;
import com.ufida.zior.docking.view.actions.TitleActionAdapter;
import com.ufida.zior.event.ListenerLifecycle;
import com.ufida.zior.perfwatch.PerfWatch;
import com.ufida.zior.perspective.Perspective;
import com.ufida.zior.perspective.RestorationManager;
import com.ufida.zior.plugin.event.PluginActionEvent;
import com.ufida.zior.view.IDockingContainer;
import com.ufida.zior.view.Mainboard;
import com.ufida.zior.view.ViewAdapter;
import com.ufida.zior.view.Viewer;
import com.ufsoft.table.Cell;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.SelectModel;
import com.ufsoft.table.event.SelectEvent;
import com.ufsoft.table.header.Header;
import com.ufsoft.table.header.HeaderModel;

/**
 * ͨ��ѡ�нڵ㹹������views
 *
 * @author changpeng
 *
 */
public class ViewManager {
	// �汾�Աȵ�model�ļ��ر�ʶ
	private int mode = -1;
	private HashMap<String, List<Object>> mapOnly=new HashMap<String, List<Object>>();
	public class TabChangedListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			Object obj = e.getSource();
  
			KTabbedPane kpane = (KTabbedPane) obj;
			Integer index = (Integer) kpane
					.getClientProperty("__index_to_remove__");
			if (index != null) {
				if(getTbPlanContext() != null && getTbPlanContext().getCurrReportViewer() != null){
					CellsModel cellsModel = getTbPlanContext().getCurrReportViewer().getCellsModel();
					if(cellsModel != null){
						SelectModel selectModel = cellsModel.getSelectModel();
						SelectEvent evt = new SelectEvent(selectModel, "anchor_changed",
								selectModel.getAnchorCell(), selectModel.getAnchorCell());
						getTbPlanContext().getCurrReportViewer().getEventManager().dispatch(evt);
					}

				}

				return;
			}
			Component c = kpane.getSelectedComponent();
			if (c instanceof ViewAdapter) {
				Viewer view = ((ViewAdapter) c).getViewer();
				if (view instanceof TBSheetViewer) {
					TBSheetViewer v = (TBSheetViewer) view;
					if (getTbPlanContext().getCurrReportViewer() == null
							|| !v.equals(getTbPlanContext()
									.getCurrReportViewer())) {
						if (getTbPlanContext().getCurrReportViewer() != null
								&& getTbPlanContext().getCurrReportViewer()
										.getTable().getCellEditor() != null) {
							getTbPlanContext().getCurrReportViewer().getTable()
									.getCellEditor().stopCellEditing();
						}
						if (getTbPlanContext().getCurrReportViewer() != null
								&& getTbPlanContext().getCurrReportViewer()
										.getCellsPane() != null)
							TbDefaultSheetCellRender
									.stopPlay(getTbPlanContext()
											.getCurrReportViewer()
											.getCellsPane());// ȥ�����Ƶ����߿�

						getTbPlanContext().setCurrReportViewer(v);
						DockingManager dmng = ViewManager.this.mainboard
								.getDockingManager();
						Dockable dockable = dmng.getDockable(v.getId());
						if (dockable != null) {
							dmng.display(dockable);
							((TBSheetViewer) v).onActive();
						}
						// CellPosition cp = CellPosition.getInstance("A1");
						if (v.getCellsModel() != null) {
							// ����һ�лᵼ�¹�������ĳЩ����ʧЧ������������Ϊ1
							Header[] headers = v.getCellsModel().getColumnHeaderModel().getHeaders();
							for(int i=0 ; i<headers.length ;i++){
								Header h = headers[i];
								int size = h.getSize();
								if(size == 0){
									v.getCellsModel().getColumnHeaderModel().setSize(i, 1);
								}
							}
							// v.getCellsModel().getSelectModel().setAnchorCell(cp);
							// �л�sheet����Ҫ���ж������
							try {
								DataCellFroozeUtil.checkAndFreezeTabel(
										v.getTable(), m_task.getPrimaryKey(),
										v.getPkMdSheet(), v.getTsDataModel()
												.getMdSheet().getCsmodel()
												.getRowNum(), v
												.getTsDataModel().getMdSheet()
												.getCsmodel().getColNum());
							} catch (BusinessException e1) {
								NtbLogger.printException(e1);
							}
							if(ITbPlanActionCode.mode_cellcontrast!=mode){
							setRowWidthAndColHeight();}//�汾�ԱȽ��治��Ҫ}
							if(ITbPlanActionCode.mode_cellcontrast==mode){
								DataCellFroozeUtil.freezeHeader(v.getTable(), v.getPkMdSheet());

							}
						}
					}
				}

			}
			String busiCode=getTbPlanContext().getSysCode()==null?IBusiTermConst.SYS_TB:getTbPlanContext().getSysCode();
			String til=BusiTermConst.getSysNameByCode(busiCode)+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0485")/*@res "Ĭ�ϱ�"*/;//NCLangRes.getInstance().getStrByID("tbb_plan",
			//�л�����ʱҪ��ѡ��ı���Χ����һ��
			if(getTbPlanContext().getCurrReportViewer().getTitle()!=null&&!getTbPlanContext().getCurrReportViewer().getTitle().equals(til)){
				 String selectSheetKey=m_task==null?null:m_task.getPk_obj();
				 if( getTbPlanContext().getCurrTbReportDirView()!=null)
				 getTbPlanContext().getCurrTbReportDirView().getSelectSheetMap().put(selectSheetKey, getTbPlanContext().getCurrReportViewer()==null?null:getTbPlanContext().getCurrReportViewer().getId());
			}

		}

	}
	private void setRowWidthAndColHeight() {
 		String user_pk=WorkbenchEnvironment.getInstance().getLoginUser().getPrimaryKey();
		String id=getTbPlanContext().getCurrReportViewer().getId();

		//�л������ʱ���Ѿ�����20150407
//		if(getTbPlanContext().getSaveRowColSizeMap() == null) {
//			   getTbPlanContext().setSaveRowColSizeMap( ZiorFrameCtl.loadRowColSize(m_task));
//		}


		if(getTbPlanContext().getSaveRowColSizeMap()!=null&&getTbPlanContext().getSaveRowColSizeMap().size()>0&&
				getTbPlanContext().getSaveRowColSizeMap().get(user_pk+m_task.getPk_obj())!=null&&
				getTbPlanContext().getSaveRowColSizeMap().get(user_pk+m_task.getPk_obj()).size()>0&&
				getTbPlanContext().getSaveRowColSizeMap().get(user_pk+m_task.getPk_obj()).get(id)!=null){
			// ����������п��и�
			CellsModel cellsModel=getTbPlanContext().getCurrReportViewer().getCellsModel();
			HeaderModel rowModel=getTbPlanContext().getCurrReportViewer().getCellsModel().getRowHeaderModel();
			HeaderModel colModel=getTbPlanContext().getCurrReportViewer().getCellsModel().getColumnHeaderModel();
			Map<String,Map> sizeMap=(Map<String, Map>) getTbPlanContext().getSaveRowColSizeMap().get(user_pk+m_task.getPk_obj()).get(id);
			if(sizeMap!=null&&sizeMap.size()>0){
				//�������õĸ�ʽ���Խڵ�+�û�+����Ϊ��
				Map<Integer,Integer> rowMap=sizeMap.get(ITbPlanActionCode.ROWSIZE);
				Map<Integer,Integer> colMap=sizeMap.get(ITbPlanActionCode.COLSIZE);
				if(rowMap!=null){
					for(int row=0;row<rowMap.size();row++){
						int width3 = rowMap.get(row);
						if(width3!=0)
						{
							rowModel.setHeader(row, Header.getInstance(width3));//rowWidth != 0 ? rowWidth:cellsModel.getRowHeaderModel().getPreferredSize()));
						}
					}
					cellsModel.getRowHeaderModel().resetSizeCache();
				}
				if(colMap!=null){
					for(int col=0;col<colMap.size();col++){
					int clHeight = colMap.get(col);
					if(clHeight!=0)
					{
						colModel.setHeader(col, Header.getInstance(clHeight));// != 0 ? colHeight:cellsModel.getColumnHeaderModel().getPreferredSize()));
					}
				}
					cellsModel.getColumnHeaderModel().resetSizeCache();
			}
			}

		}
		try {
			DataCellFroozeUtil.checkAndFreezeTabel(
					getTbPlanContext().getCurrReportViewer().getTable(), m_task.getPrimaryKey(),
					getTbPlanContext().getCurrReportViewer().getPkMdSheet(),getTbPlanContext().getCurrReportViewer().getTsDataModel()
							.getMdSheet().getCsmodel()
							.getRowNum(), getTbPlanContext().getCurrReportViewer()
							.getTsDataModel().getMdSheet()
							.getCsmodel().getColNum());
		} catch (BusinessException e1) {
			NtbLogger.printException(e1);
		}
}
	// ��ŵ�ǰ����Ķ��Viewer
	private List<TBSheetViewer> sheetViewList = new ArrayList<TBSheetViewer>();
	private TBSheetViewer coverTbSheetViewer =null;
	// ��ǰ����
	private MdTask m_task;
	/**����ɲ�����**/
	private String[] taskValidSheets;
	/**����ɼ�������**/
	private String[] taskValidLookSheets;
	/**�汾�Ա�ѡ��ı�**/
	private String[] taskCMPValidLookSheets;
	/** �����ڵ���صı���Χ**/
	private boolean isApp=false;
	/** �ɼ�����ΧΪnullʱ�������taskdatamodel�ϵı���Χ��Ϊtrue������ȫ�߿ɼ���**/
	private boolean isNotALLIfNotLook=false;
	/**�Ƿ���ˢ�°�ť���õ�ˢ��**/
	private boolean isRefreshAction=false;

	private TaskExtInfoLoader taskExtInfoLoader = new TaskExtInfoLoader();
	private TaskDataModel taskDataModel = null;
	private int currentValueScale = -1;
	private int iMode = TbReportFactory.mode_taskview;
	private int prevMode = iMode;
	// private TaskLayoutRecord taskLayoutRecord = null;

	private int scale = 1;// �汾�Ա�ʱѡ���Ҫ��ʾ�ĵ�λ��ǧԪ��Ԫ�����ñ�־��
	// ������ʾ��Ⱦ��
	private MutiRowCellRenderer render = null;
	private String lastSelectSheetViewer = null;

	public void setLastSelectSheetViewer(String lastSelectSheetViewer) {
		this.lastSelectSheetViewer = lastSelectSheetViewer;
	}

	public MdTask getMdTask() {
		return m_task;
	}

	public TaskDataModel getTaskDataModel() {
		return taskDataModel;
	}

	public void setTaskDataModel(TaskDataModel taskDataModel) {
		this.taskDataModel = taskDataModel;
	}

	public List<TBSheetViewer> getSheetViewList() {
		return sheetViewList;
	}
	public TBSheetViewer getCoverTbSheetViewer() {
		return coverTbSheetViewer;
	}
	public int getCurrentValueScale() {
		return currentValueScale;
	}

	public void setCurrentValueScale(int currentValueScale) {
		this.currentValueScale = currentValueScale;
	}

	public TBSheetViewer getCurrentTbSheetViewer() {
		TBSheetViewer view = (TBSheetViewer) getMainboard().getCurrentView();
		return view;
	}

	public TaskExtInfoLoader getTaskExtInfoLoader() {
		return taskExtInfoLoader;
	}

	public Mainboard getMainboard() {
		return mainboard;
	}

	private Mainboard mainboard = null;

	public ViewManager(Mainboard mainboard) {
		this.mainboard = mainboard;
	}

	public void clearViewCache() {
		if (sheetViewList != null) {
			sheetViewList.clear();
		}
		taskDataModel = null;
	}
/**
 *
 * @param task
 * @param pk_sheets  ����Ŀɼ���
 */
	public void setTask(MdTask task, String[] pk_sheets,boolean isRefreshAction) {
		if(task==null){
			removeAllViewers();
			return;
		}
		UISharedData.getInstance().prepareDimMemberCacheForPkOrg(task.getPk_dataent());
		releaseDataCell();
		// ����һ�����һ�ε�ҳǩid  �ŵ�removeAllViewers()ǰ�������Ĭ�ϱ�ռ����ʱ�ᱻˢ��  lym  �벻Ҫ�ٻ��ط�
		if (getTbPlanContext() != null
				&& getTbPlanContext().getCurrReportViewer() != null) {
			if(lastSelectSheetViewer==null){
				this.lastSelectSheetViewer = getTbPlanContext()
						.getCurrReportViewer().getId();
			}

		}
		if (!isRefreshAction&&(task == null || m_task != null && task != null
				&& m_task.getPrimaryKey().equals(task.getPrimaryKey())
				&& PubUtil.isEqual(this.taskValidLookSheets, pk_sheets))) {
			//��ʱ���Ͽɼ��������ɼ��ı����ü���ָ�����ע��Ϣ֮�࣬�������ٻ��ɿɲ�����  ly
			if(task==null)removeAllViewers();
				return;
		}
		this.m_task = task;
		this.taskValidLookSheets = pk_sheets;
		taskDataModel=null;
		taskExtInfoLoader.setMdTask(m_task, pk_sheets);
		Thread run = new Thread() {
			@Override
			public void run() {
				synchronized(mainboard){
				refreshTables();
				}
			}
		};
		synchronized (mainboard) {


		if (TbPlanFrameUtil.getTbPlanFrame(getMainboard()) == null) {
			refreshTables();
		} else {
			TbPlanFrameUtil.getTbPlanFrame(getMainboard()).invokeWithPorgress(
					run);
		}
		removeAllViewers0();
		}

		
	}

	public void deleteNullRowAndCol() {
		for (TBSheetViewer tbSheetViewer : sheetViewList) {
			tbSheetViewer.deleteNUllRowOrCol();
		}
	}

	public void saveNoRuleTasks() throws BusinessException  {
		CompileSaveLogs compileSaveLogs=new CompileSaveLogs();
		TbPlanContext tbPlanContext = getTbPlanContext();
		// ÿ�α���ǰ�Ƚ���ʶ�ÿ�
		if (tbPlanContext != null && getMdTask() != null) {
			TBSheetViewer currViewer = tbPlanContext.getCurrReportViewer();
			boolean isIndexApprove = tbPlanContext.isIndexApprove();
			// ΪTURE��Ϊָ�������ı���,�����Ǳ��Ƶı���
			if (isIndexApprove) {
				// ָ���������涯��
				boolean sucessFlag = doIndexApproveSave(compileSaveLogs);
				return;
			}
			// ɾ�����п���
			deleteNullRowAndCol();
			if (currViewer != null) {
				currViewer.getCellsPane().editingStopped(
						new ChangeEvent(currViewer));
				// ѯ���ظ�ά�������Ƿ��������+nullָ��û��ʱ�Ƿ��������
				List<String> repeatDimOrNullIndex=currViewer.getRepeatDimOrNullIndex();
				if (repeatDimOrNullIndex!=null&&repeatDimOrNullIndex.size()>0) {
					String mes="";
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVEREPEATDIM)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0262")/*@res "�����ظ�ά�����,\n"*/;
					}
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVENULLINDEX)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0263")/*@res "ָ��ĳ�Ա����Ϊ��,\n"*/;
					}
					   mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0264")/*@res "��ȷ���Ƿ��������?"*/;
					int result = MessageDialog.showYesNoDlg(
							getMainboard(),
							NCLangRes.getInstance().getStrByID("tbb_plan",
									"01812pln_000465")/* ѯ�� */,mes);//NCLangRes.getInstance().getStrByID("tbb_plan","01812pln_000728")/*" ���� �ظ���ָ��"*/);
					if (MessageDialog.ID_NO == result || MessageDialog.ID_CANCEL == result) {
						TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
						return;
					}
					TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				}
				// ��CellsModelд��TaskSheetDataModel
				setAllViewsDataToTsModel();
			}

			// ִ��Excel��ʽ�͹���
//			executeFmlAndRule();

			// �༭���ݱ��涯��
			doCompileNoRuleSave();
			// ��TaskSheetDataModelд��CellsModel
						setAllViewsTsModelToCellsModel();
			//********* begin 2015-04-01 (NCdp205325639) quankj �����棬���ƹ��ܣ����ݴ水ť��Ӧ�ѱ���ȡ����ť�ûң��ݴ�=����+��ִ�й�ʽ
			if (currViewer != null) {
				currViewer.getTable().repaint();
				// ȥ�����Ƶ����߿�
				TbDefaultSheetCellRender.stopPlay(currViewer.getCellsPane());
				// �ַ�����¼�
				PluginActionEvent pluginActionEvent = new PluginActionEvent(
						currViewer, 1);
				this.getMainboard().getEventManager()
						.dispatch(pluginActionEvent);
//				if(getCurrentTbSheetViewer()!=null){
//					//�������ע��ϢҪ�رյ� --->�ҵ������������,�ʲ��ſ��˵ط����ſ������Ӳ���Ҫ��Ч������   by liying
//					getCurrentTbSheetViewer().hideAllPostils();
//				}
			}
			//********* end 2015-04-01 (NCdp205325639) quankj �����棬���ƹ��ܣ����ݴ水ť��Ӧ�ѱ���ȡ����ť�ûң��ݴ�=����+��ִ�й�ʽ

		}
	}

	private void doCompileNoRuleSave() throws BusinessException {
		stopAllViewEditing();
		// ��ע��Ϣ(�˴�ֻ���£������ǣ��Ա�֤��Щδ���ص�CellsModel�ϵ���ע���ᱻ����)
		HashMap<String, HashMap<DimVector, String>> map = getTaskExtInfoLoader()
				.getTaskNoteMap();
		Map<String, Map<String, List<Header>>> recordMap = new HashMap<String, Map<String, List<Header>>>();
		for (TBSheetViewer tbViewer : getSheetViewList()) {
			Map<String, List<Header>> rowAndColMap = new HashMap<String, List<Header>>();
			CellsModel cModel = tbViewer.getCellsModel();
			if (cModel != null) {
				rowAndColMap.put(TaskLayoutRecord.M_COL, Arrays.asList(cModel
						.getColumnHeaderModel().getHeaders()));
				rowAndColMap.put(TaskLayoutRecord.M_ROW,
						Arrays.asList(cModel.getRowHeaderModel().getHeaders()));
				recordMap.put(tbViewer.getMdSheet().getPk_obj(), rowAndColMap);

				// ��ȡ��ע��Ϣ
				List<List<Cell>> cells = cModel.getCells();
				for (List<Cell> row : cells) {
					if (row == null)
						continue;
					for (Cell cell : row) {
						CellExtInfo cInfo = cell == null ? null
								: (CellExtInfo) cell
										.getExtFmt(TbIufoConst.tbKey);
						if (cInfo != null && cInfo.getCubeCode() != null
								&& cInfo.getDimVector() != null) {
							HashMap<DimVector, String> m = map.get(cInfo
									.getCubeCode());
							String note = (String) cell
									.getExtFmt(TbIufoConst.cellNote);
							if (note == null || note.length() == 0) {
								if (m != null)
									m.remove(cInfo.getDimVector());
							} else {
								if (m == null) {
									m = new HashMap<DimVector, String>();
									map.put(cInfo.getCubeCode(), m);
								}
								m.put(cInfo.getDimVector(), note);
							}
						}
					}
				}
			}
		}
		boolean isIndexApprove = getTbPlanContext().isIndexApprove();
		// ΪTURE��Ϊָ�������ı���,�����Ǳ��Ƶı���
		if (!isIndexApprove) {
			saveTaskLayoutRecord(recordMap);
		}
		// ��������ģ��
		getTaskDataModel().save();
		// ������ע
		TbTaskExtCtl.saveExOlapInfoSet(getMdTask().getPrimaryKey(),TaskExcelObjectConvertor.getOlapInfoList(map, getMdTask()
						.getPrimaryKey()));
		TbPlanContext tbPlanContext = getTbPlanContext();
		if (tbPlanContext != null) {
			//********* begin 2015-04-01 (NCdp205325639) quankj �����棬���ƹ��ܣ����ݴ水ť��Ӧ�ѱ���ȡ����ť�ûң��ݴ�=����+��ִ�й�ʽ
			//tbPlanContext.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKEDIT);
			tbPlanContext.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKVIEW);
			//********* end 2015-04-01 (NCdp205325639) quankj �����棬���ƹ��ܣ����ݴ水ť��Ӧ�ѱ���ȡ����ť�ûң��ݴ�=����+��ִ�й�ʽ
		}
	}



	/**
	 * ��������
	 *
	 * @throws BusinessException
	 */
	public CompileSaveLogs saveTasks() throws BusinessException {
		TbPlanContext tbPlanContext = getTbPlanContext();
		CompileSaveLogs compileSaveLogs=new CompileSaveLogs();
		String message = null;
		// ÿ�α���ǰ�Ƚ���ʶ�ÿ�
		if (tbPlanContext != null && getMdTask() != null) {
			TBSheetViewer currViewer = tbPlanContext.getCurrReportViewer();
			boolean isIndexApprove = tbPlanContext.isIndexApprove();
			// ΪTURE��Ϊָ�������ı���,�����Ǳ��Ƶı���
			if (isIndexApprove) {
				// ָ���������涯��
				boolean sucessFlag = doIndexApproveSave(compileSaveLogs);
				return compileSaveLogs;
			}

			

			if (currViewer != null) {
				currViewer.getCellsPane().editingStopped(
						new ChangeEvent(currViewer));
				// ɾ�����п���
				deleteNullRowAndCol();
				// ѯ���ظ�ά�������Ƿ��������+����������ָ��û��ʱ�Ƿ��������
				List<String> repeatDimOrNullIndex=currViewer.getRepeatDimOrNullIndex();
				if (repeatDimOrNullIndex!=null&&repeatDimOrNullIndex.size()>0) {
					String mes="";
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVEREPEATDIM)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0262")/*@res "�����ظ�ά�����,\n"*/;
					}
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVENULLINDEX)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0265")/*@res "ָ��ĳ�Ա����Ϊ��,Ϊ�յ������޷�����!\n"*/;
					}
					   mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0264")/*@res "��ȷ���Ƿ��������?"*/;
					int result = MessageDialog.showYesNoDlg(
							getMainboard(),
							NCLangRes.getInstance().getStrByID("tbb_plan",
									"01812pln_000465")/* ѯ�� */,mes);//NCLangRes.getInstance().getStrByID("tbb_plan","01812pln_000728")/*" ���� �ظ���ָ��"*/);
					if (MessageDialog.ID_NO == result || MessageDialog.ID_CANCEL == result) {
						TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
						return compileSaveLogs;
					}
					TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				}
				// ��CellsModelд��TaskSheetDataModel
				setAllViewsDataToTsModel();
			}
        //��������������ݿ����:(1)ִ�й�ʽ  ��2��ģ�����ݱ���
			Map<String,Object> map=ZiorFrameCtl.saveTaskDataMesMap(tbPlanContext.getNodeType(), taskDataModel, m_task);
			UFDateTime save=(UFDateTime) map.get(ITbPlanActionCode.SAVETIME);
			taskDataModel=(TaskDataModel) map.get(ITbPlanActionCode.TASKDATAMODEL);
			message = (String) map.get(ITbPlanActionCode.ADJUSTCONTROLRULE);
			
			compileSaveLogs.setAdjustControlRuleMessage(message);
			TbbFormulaExecuteLogs logs=(TbbFormulaExecuteLogs) map.get(ITbPlanActionCode.TBBFORMULAEXECUTELOGS);
			if(logs.haveErr()){
				compileSaveLogs.setRuleMessage(logs.getErrInfo());
			}
			// ��ע�ı���
			saveExOlapInfoSet();
			TaskDataCtl.updateTaskDataModelUICache(getTaskDataModel(), save);
			getTbPlanContext().setCurrentTs(save);
			if (tbPlanContext != null) {
				tbPlanContext.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKVIEW);
			}
			updateViewerDataModel();
			setAllViewsTsModelToCellsModel();
			// �����ˢ�ºͷַ��¼�
			if (currViewer != null) {
				currViewer.getTable().repaint();
				// ȥ�����Ƶ����߿�
				TbDefaultSheetCellRender.stopPlay(currViewer.getCellsPane());
				// �ַ�����¼�
				PluginActionEvent pluginActionEvent = new PluginActionEvent(
						currViewer, 1);
				this.getMainboard().getEventManager()
						.dispatch(pluginActionEvent);
//				if(getCurrentTbSheetViewer()!=null){
//					//�������ע��ϢҪ�رյ� --->�ҵ������������,�ʲ��ſ��˵ط����ſ������Ӳ���Ҫ��Ч������   by liying
//					getCurrentTbSheetViewer().hideAllPostils();
//				}
			}
		}
		return compileSaveLogs;
	}
	
	
	
	


	private void updateViewerDataModel(){
		TaskSheetDataModel[] tsdModels = taskDataModel == null ? new TaskSheetDataModel[0]
				: taskDataModel.getTaskSheetDataModels();
		if(tsdModels!=null){
			for (TBSheetViewer tbSheetViewer : sheetViewList) {
				for(int i=0;i<tsdModels.length;i++){
					if(tsdModels[i].getName().equals(tbSheetViewer.getTitle())){
						tbSheetViewer.setTsDataModel(tsdModels[i]);
						break;
					}
				}

			}
		}

	}

	private void saveExOlapInfoSet() throws BusinessException {
		stopAllViewEditing();
		// ��ע��Ϣ(�˴�ֻ���£������ǣ��Ա�֤��Щδ���ص�CellsModel�ϵ���ע���ᱻ����)
		HashMap<String, HashMap<DimVector, String>> map = getTaskExtInfoLoader()
				.getTaskNoteMap();
	//	Map<String, Map<String, List<Header>>> recordMap = new HashMap<String, Map<String, List<Header>>>();
		for (TBSheetViewer tbViewer : getSheetViewList()) {
			Map<String, List<Header>> rowAndColMap = new HashMap<String, List<Header>>();
			CellsModel cModel = tbViewer.getCellsModel();
			if (cModel != null) {
//				rowAndColMap.put(TaskLayoutRecord.M_COL, Arrays.asList(cModel
//						.getColumnHeaderModel().getHeaders()));
//				rowAndColMap.put(TaskLayoutRecord.M_ROW,
//						Arrays.asList(cModel.getRowHeaderModel().getHeaders()));
//				recordMap.put(tbViewer.getMdSheet().getPk_obj(), rowAndColMap);

				// ��ȡ��ע��Ϣ
				List<List<Cell>> cells = cModel.getCells();
				for (List<Cell> row : cells) {
					if (row == null)
						continue;
					for (Cell cell : row) {
						CellExtInfo cInfo = cell == null ? null
								: (CellExtInfo) cell
										.getExtFmt(TbIufoConst.tbKey);
						if (cInfo != null && cInfo.getCubeCode() != null
								&& cInfo.getDimVector() != null) {
							HashMap<DimVector, String> m = map.get(cInfo
									.getCubeCode());
							String note = (String) cell
									.getExtFmt(TbIufoConst.cellNote);
							if (note == null || note.length() == 0) {
								if (m != null)
									m.remove(cInfo.getDimVector());
							} else {
								if (m == null) {
									m = new HashMap<DimVector, String>();
									map.put(cInfo.getCubeCode(), m);
								}
								m.put(cInfo.getDimVector(), note);
							}
						}
					}
				}
			}
		}
//		boolean isIndexApprove = getTbPlanContext().isIndexApprove();
		//lym 20150415 ֻ����û�е��õĵط��˴˴�ע����û�õĹ��ܣ�
//		// ΪTURE��Ϊָ�������ı���,�����Ǳ��Ƶı���
//		if (!isIndexApprove) {
//			saveTaskLayoutRecord(recordMap);
//		}
		// ������ע
		if(map!=null&&map.size()>0){
			ZiorFrameCtl.saveExOlapInfoSet(getMdTask().getPrimaryKey(),
					TaskExcelObjectConvertor.getOlapInfoList(map, getMdTask()
							.getPrimaryKey()));
		}

	}

	private void saveTaskLayoutRecord(
			Map<String, Map<String, List<Header>>> recordMap)
			throws BusinessException {
		TaskLayoutRecord taskLayoutRecord = new TaskLayoutRecord();
		taskLayoutRecord.setTaskRecord(recordMap);
		taskLayoutRecord.setMdTask(getMdTask().getPk_obj());
		TbUserProfileCtl.getInstance().saveTaskLayoutRecord(taskLayoutRecord,
				getMdTask().getPk_obj());
	}

	private boolean doIndexApproveSave(CompileSaveLogs compileSaveLogs) throws BusinessException {
		TbPlanContext tbPlanContext = getTbPlanContext();
		TBSheetViewer currViewer = tbPlanContext.getCurrReportViewer();
		MdTask task = getMdTask();
		// ����޸Ĺ��ĵ�Ԫ�� --- DataCell��status���
//		Map<MdTask, List<WrappedDataCell>> hmTaskToWdcs = getChangedWrappedDataCells();
		Map<String, Object> mapChanged = getChangedWrappedDataCells();
		Map<MdTask, List<WrappedDataCell>> hmTaskToWdcs = null;   //�޸Ĺ��ĵ�Ԫ��
		boolean isTxtChanged = false;  //�Ƿ��зǶ�ά�����޸Ĺ�
		if(mapChanged != null){
			hmTaskToWdcs = (Map<MdTask, List<WrappedDataCell>>)mapChanged.get(TbZiorUiCtl.KEY_DATACELL);
			isTxtChanged = "Y".equals(mapChanged.get(TbZiorUiCtl.KEY_CHANGED));
		}
		// ��õ�ǰҳǩ�޸Ĺ��ĵ�Ԫ��(ָ������ֻ��������ǰҳǩ--- ����ʵ��Ψһ)
		List<WrappedDataCell> listWdcs = hmTaskToWdcs == null ? null : hmTaskToWdcs.get(task);
		boolean hasChangedDc = listWdcs != null && !listWdcs.isEmpty();
		//ָ��������ϸ
		ArrayList<IndexAppdetailVO> alAppDetail = new ArrayList<IndexAppdetailVO>();
		//ָ���������
		String planAppNote = null;
		// �Ƿ��޸����
		boolean isApproveSuccess = true;
		if (!hasChangedDc) {
			if(!isTxtChanged)
				throw new BusinessException(NCLangRes.getInstance().getStrByID(
					"tbb_plan", "01812pln_000475")/* Ŀǰû���κ����ݱ��޸ģ������ڽ������޸���Ҫ���������� */);
		}else{
			// ��ģ�ͱ��浥Ԫ��
			Map<CubeDef, Map<DimVector, WrappedDataCell>> hmCubeToDvDc = new LinkedHashMap<CubeDef, Map<DimVector, WrappedDataCell>>();
			for (int i = 0; i < listWdcs.size(); i++) {
				WrappedDataCell wdc = listWdcs.get(i);
				// �޸ĺ�Ԫ��
				DataCell dc = wdc.getDataCell();
				if (dc == null)
					continue;
				// Ӧ��ģ��
				CubeDef cube = dc.getCubeDef();
				// ά������
				DimVector dv = dc.getDimVector();
				// ��Ԫ�񻺴�
				Map<DimVector, WrappedDataCell> hmDvToDc = hmCubeToDvDc.get(cube);
				if (hmDvToDc == null) {
					hmDvToDc = new LinkedHashMap<DimVector, WrappedDataCell>();
					hmCubeToDvDc.put(cube, hmDvToDc);
				}
				hmDvToDc.put(dv, wdc);
			}
			// ���ݲ�ѯ�ӿ�
			IDataSetService service = CubeServiceGetter.getDataSetService();
			// ��ģ�ͱ���
			Iterator<CubeDef> itCube = hmCubeToDvDc.keySet().iterator();
			while (itCube.hasNext()) {
				// Ӧ��ģ��
				CubeDef cube = itCube.next();
				// ԭ��Ԫ�񻺴�
				Map<DimVector, WrappedDataCell> hmNew = hmCubeToDvDc.get(cube);
				if (hmNew == null || hmNew.size() == 0)
					continue;
				// ��Ԫ��ά������
				List<DimVector> alDvs = new ArrayList<DimVector>();
				alDvs.addAll(hmNew.keySet());
				// ͨ����Ԫ���ά������ȥ���ݿ��в�ѯԭ������, ͨ���Ƚϻ�������޸Ĺ����ݵĵ�Ԫ��
				ICubeDataSet ids = service.queryDataSet(cube, alDvs);
				// ������Ԫ��,ȷ�������Ƿ��޸Ĺ�
				for (int j = 0; j < alDvs.size(); j++) {
					DimVector dv = alDvs.get(j);
					// �µ�Ԫ��
					WrappedDataCell wdc = hmNew.get(dv);
					// ���ݿ��е�ԭ��Ԫ��
					DataCell oldDc = ids.getDataCell(dv);
					//���������ϸ
					IndexAppdetailVO obj = IndexAppUiCtl.getIndexAppDetailVO(cube, task, wdc, oldDc);
					if(obj != null){
						alAppDetail.add(obj);
					}
				}
			}
			// ����������ϸ����
			if(getTbPlanContext().getTaskDef()==null){
				MdTaskDef def=task.getTaskDefWithoutDetail();
				getTbPlanContext().setTaskDef(def);
			}
			IndexApproveDlg dlg = new IndexApproveDlg(this.getMainboard(), task,
					alAppDetail,getTbPlanContext().getTaskDef());
			if (dlg.showModal() == UIDialog.ID_OK) {
				TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				// ������ϸ
				alAppDetail = dlg.getPlanAppDetail();
				// �������
				planAppNote = dlg.getApproveNote();
			} else {
				// showMessage(NCLangRes.getInstance().getStrByID("tbb_plan",
				// "01812pln_000476")/*��ȡ������*/);
				TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				isApproveSuccess = false;
			}
		}
		
		/******************************************/
		//����Ϊ���ñ������ݺͱ���ָ���������һ�£���ý������ı������ͬһ��������
		if(isApproveSuccess){
			// ɾ�����п���
			deleteNullRowAndCol();
			if (currViewer != null) {
				currViewer.getCellsPane().editingStopped(
						new ChangeEvent(currViewer));
				// ѯ���ظ�ά�������Ƿ��������+����������ָ��û��ʱ�Ƿ��������
				List<String> repeatDimOrNullIndex=currViewer.getRepeatDimOrNullIndex();
				if (repeatDimOrNullIndex!=null&&repeatDimOrNullIndex.size()>0) {
					String mes="";
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVEREPEATDIM)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0266")/*@res "�����ظ�ά�����\n,"*/;
					}
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVENULLINDEX)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0267")/*@res "ָ��ĳ�Ա����Ϊ��\n,"*/;
					}
					   mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0264")/*@res "��ȷ���Ƿ��������?"*/;
					int result = MessageDialog.showYesNoDlg(
							getMainboard(),
							NCLangRes.getInstance().getStrByID("tbb_plan",
									"01812pln_000465")/* ѯ�� */,mes);//NCLangRes.getInstance().getStrByID("tbb_plan","01812pln_000728")/*" ���� �ظ���ָ��"*/);
					if (MessageDialog.ID_NO == result || MessageDialog.ID_CANCEL == result) {
						TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
						return false;
					}
					TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				}
				// ��CellsModelд��TaskSheetDataModel
				setAllViewsDataToTsModel();
			}

			// �����������
			isApproveSuccess = saveApproveDetail(task, alAppDetail, planAppNote);
			//��������������ݿ����:(1)ִ�й�ʽ  ��2��ģ�����ݱ���
			Map<String,Object> map=ZiorFrameCtl.saveTaskDataMesMap(tbPlanContext.getNodeType(), taskDataModel, m_task);
			UFDateTime save=(UFDateTime) map.get(ITbPlanActionCode.SAVETIME);
			taskDataModel=(TaskDataModel) map.get(ITbPlanActionCode.TASKDATAMODEL);
            String message = (String) map.get(ITbPlanActionCode.ADJUSTCONTROLRULE);
			compileSaveLogs.setAdjustControlRuleMessage(message);
			TbbFormulaExecuteLogs logs=(TbbFormulaExecuteLogs) map.get(ITbPlanActionCode.TBBFORMULAEXECUTELOGS);
			if(logs.haveErr()){
				compileSaveLogs.setRuleMessage(logs.getErrInfo());
			}
			// ��ע�ı���
			saveExOlapInfoSet();
			TaskDataCtl.updateTaskDataModelUICache(getTaskDataModel(), save);
			getTbPlanContext().setCurrentTs(save);
			if (tbPlanContext != null) {
				tbPlanContext.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKVIEW);
			}
			updateViewerDataModel();
			setAllViewsTsModelToCellsModel();
			tbPlanContext.setIndexApprove(false);
			taskExtInfoLoader.loadIndexAppdetails();
			if (currViewer != null) {
				currViewer.getCellsPane().editingStopped(
						new ChangeEvent(currViewer));
				// �����ˢ�ºͷַ��¼�
				if (currViewer != null) {
					currViewer.getTable().repaint();
					// ȥ�����Ƶ����߿�
					TbDefaultSheetCellRender.stopPlay(currViewer.getCellsPane());
					// �ַ�����¼�
					PluginActionEvent pluginActionEvent = new PluginActionEvent(
							currViewer, 1);
					this.getMainboard().getEventManager()
							.dispatch(pluginActionEvent);
				}
			}
		}else{
			refresh(currViewer);
			return isApproveSuccess;
		}
		/*****************************************/
		// ָ������֮��ı�״̬
		getTbPlanContext().setIndexApprove(false);
		if (tbPlanContext != null) {
			tbPlanContext
					.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKVIEW);
		}
		return true;
	}

	/**
	 * ������������
	 */
	protected boolean saveApproveDetail(MdTask task,
			ArrayList<IndexAppdetailVO> listAppDetail, String planAdjNote)
			throws BusinessException {
		for (IndexAppdetailVO detailVo : listAppDetail) {
			detailVo.setTxtnote(planAdjNote);
		}
		UserLoginVO userloginvo = ApprovePlanTools.getCompleteUser(task);
		String appType = ITaskAction.APPROVE_EDIT;
		HashMap<String, Object> hsDetailVos = new HashMap<String, Object>();
		hsDetailVos.put(task.getPermPrimary(), listAppDetail);
		
		TaskActionCtl.processAction(userloginvo, task, appType, hsDetailVos);
		this.taskExtInfoLoader.loadIndexAppdetails();
		return true;
	}

	private String getDimDetailInf(DataCell datacell) {
		StringBuffer dimDetailInf = new StringBuffer();
		DimVector dv = datacell.getDimVector();
		Collection<LevelValue> colLevelValue = dv.getAllLevelValuesSorted();
		Iterator<LevelValue> it = colLevelValue.iterator();
		while (it.hasNext()) {
			LevelValue lv = it.next();
			String dimLevelCode = lv.getDimLevel().getBusiCode();
			String levelValueKey = lv.getKey().toString();
			dimDetailInf.append(dimLevelCode);
			dimDetailInf.append("=");
			dimDetailInf.append(levelValueKey);
			dimDetailInf.append(",");
		}
		String detailInf = dimDetailInf.toString();
		if (!detailInf.isEmpty())
			detailInf = detailInf.substring(0, detailInf.lastIndexOf(","));

		return detailInf;
	}

	/**
	 * sheets �ɼ�����Χ
	 * �汾�Ա�����ĳ�ʼ��
	 */
	public void setVersionCmpTask(MdTask srcTask, MdSheet[] sheets,
			MutiRowCellRenderer render, int mode, int scale) {
		if (sheets != null && sheets.length > 0) {
			taskCMPValidLookSheets = new String[sheets.length];
			for (int i = 0; i < sheets.length; i++) {
				taskCMPValidLookSheets[i] = sheets[i].getPrimaryKey();
			}
		}
		this.render = render;
		this.mode = mode;
		this.scale = scale;
		releaseDataCell();
		removeAllViewers();
		this.m_task = srcTask;
		taskExtInfoLoader.setMdTask(m_task, taskCMPValidLookSheets);
		this.m_task = srcTask;
		if (m_task != null && getTbPlanContext() != null) {
			try {
				String[] pk_sheets = getTaskValidPkSheets();
				getTaskDataModelFromDb(pk_sheets);
				initSheetViewers();
			} catch (Exception e) {
				NtbLogger.printException(e);
			}
		}

	}
	/**
	 * sheets �ɼ�����Χ
	 * ��ʽ׷������ĳ�ʼ��
	 */
	public void setVersionCmpTask(MdTask srcTask, MdSheet[] sheets,
			TaskDataModel taskDataModel) {
		if (sheets != null && sheets.length > 0) {
			taskCMPValidLookSheets = new String[sheets.length];
			for (int i = 0; i < sheets.length; i++) {
				taskCMPValidLookSheets[i] = sheets[i].getPrimaryKey();
			}
		}
		releaseDataCell();
		removeAllViewers();
		this.m_task = srcTask;
		taskExtInfoLoader.setMdTask(m_task, taskCMPValidLookSheets);
		this.m_task = srcTask;
		if (m_task != null && getTbPlanContext() != null) {
			try {
				this. taskDataModel=taskDataModel;
				initSheetViewers();
			} catch (Exception e) {
				NtbLogger.printException(e);
			}
		}

	}
	private void releaseDataCell() {
		taskExtInfoLoader.clear();
	}

	@SuppressWarnings("restriction")
	private void refreshTables() {
		if (m_task != null && getTbPlanContext() != null) {
			try {
				//���ؿɲ�����������
				this.taskValidSheets = getTaskValidPkSheets();
				getTaskDataModelFromDb(taskValidSheets);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						initSheetViewers();
					}
				});
                if(mapOnly!=null&&mapOnly.size()>0/*&&mapOnly.keySet().contains("SHENHETONGGUO")*/){
                	SwingUtilities.invokeLater(new Runnable() {
                		// ��ҵ�λ���ʱѡ�е����ļҵ�λ
        				DockingState info = getMainboard().getPerspectiveManager()
        						.getDockingState(ITbPlanActionCode.Id_CHECKVIEW);
    					@Override
    					public void run() {

    						openCheckView(info,mapOnly);
    					}
    				});
                }
			} catch (Exception e) {
				NtbLogger.printException(e);
				MessageDialog.showErrorDlg(getMainboard(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0165")/*@res "����"*/, e.getMessage());
			}
		}
	}

	/**
	 * ��ҵ�λ��˺�������ĵ���
	 *
	 * @author liyingm
	 */
	public void openCheckView(DockingState info,
			HashMap<String, List<Object>> mapOnly) {

		// ������˵���Ϣ����
		List<CheckRuleResultVO> checkRuleResultVOList = new ArrayList<CheckRuleResultVO>();
		// eccelde �����Ϣ
		HashMap<TBSheetViewer, List<ExSheetCheckFml>> sheetCheckFmlMap = new HashMap<TBSheetViewer, List<ExSheetCheckFml>>();

		// �����Ϣ�ŵ�taskDataModel��ÿ��TaskSheetDataModel�е�sheetCheckFml������.
		List<TBSheetViewer> viewerList = ((TBSheetViewer) getMainboard()
				.getCurrentView()).getViewManager().getSheetViewList();
		;
		for (TBSheetViewer sheetView : viewerList) {
			List<Object> checksFmls = mapOnly.get(sheetView.getId());
			List<ExSheetCheckFml> excelList = new ArrayList<ExSheetCheckFml>();
			if (checksFmls != null && checksFmls.size() > 0) {
				for (Object obj : checksFmls) {
					if (obj instanceof CheckRuleResultVO) {
						CheckRuleResultVO ruleVO = (CheckRuleResultVO) obj;
						if (!checkRuleResultVOList.contains(ruleVO)) {
							checkRuleResultVOList.add(ruleVO);
						}

					} else if (obj instanceof ExSheetCheckFml) {
						ExSheetCheckFml excelVO = (ExSheetCheckFml) obj;
						if (!excelList.contains(excelVO)) {
							excelList.add(excelVO);
						}
					}
				}
			}
			sheetCheckFmlMap.put(sheetView, excelList);
		}
		// ����˽�������
		if ((checkRuleResultVOList != null && checkRuleResultVOList.size() > 0)
				|| (sheetCheckFmlMap != null && sheetCheckFmlMap.size() > 0)) {
			openView(info, getMainboard());
			if (getMainboard().getView(ITbPlanActionCode.Id_CHECKVIEW) != null) {
				getMainboard().getView(ITbPlanActionCode.Id_CHECKVIEW)
						.setTitle(
								NCLangRes.getInstance().getStrByID("tbb_plan",
										"01812pln_000123")/* �����Ϣ */);
				// ������˵���Ϣ������������
				((PlanCheckResultViewer) getMainboard().getView(
						ITbPlanActionCode.Id_CHECKVIEW)).setCheckData(
						checkRuleResultVOList, viewerList, sheetCheckFmlMap);
			}

		}
	}
	private void initSheetViewers() {
		Viewer viewDefault = this.getMainboard()
				.getView("tb.report.sheet.view");
		if (viewDefault != null) {
			String busiCode=getTbPlanContext().getSysCode()==null?IBusiTermConst.SYS_TB:getTbPlanContext().getSysCode();
			viewDefault.setTitle(BusiTermConst.getMultiLangTextWithSysCode(busiCode, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0485")/*@res "Ĭ�ϱ�"*/));//NCLangRes.getInstance().getStrByID("tbb_plan",
					//"01812pln_000430")/* Ԥ��Ĭ�ϱ� */);
		}
		Map<String, String> hm_sheetPk = new HashMap<String, String>();
		if(taskCMPValidLookSheets!=null&&taskCMPValidLookSheets.length>0){
			this.taskValidLookSheets=taskCMPValidLookSheets;
		}


		if (this.taskValidLookSheets != null && this.taskValidLookSheets.length > 0) {
			for (String s : this.taskValidLookSheets) {
				hm_sheetPk.put(s, s);
			}
		}
		TaskSheetDataModel[] tsdModels = taskDataModel == null ? new TaskSheetDataModel[0]
				: taskDataModel.getTaskSheetDataModels();
		TBSheetViewer viewDisplay = null;
		Dockable dockableDisplay = null;
		DockingManager dmng = ViewManager.this.mainboard.getDockingManager();
		sheetViewList.clear();
		coverTbSheetViewer=null;
		if(hm_sheetPk.isEmpty()&&isNotALLIfNotLook){
			//MessageDialog.showHintDlg(mainboard, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0283")/*@res "��ʾ��"*/, "");
			return;
		}
		for (int i = 0; i < tsdModels.length; i++) {
			MdSheet mdsheet = tsdModels[i].getMdSheet();
			if (mdsheet == null
					|| (!hm_sheetPk.isEmpty() && !hm_sheetPk
							.containsKey(mdsheet.getPrimaryKey()))) {
				continue;
			}

			DockingState info = getMainboard().getPerspectiveManager()
					.getDockingState(tsdModels[i].getName());
			Viewer v = null;
			if (info == null) {
				info = new DockingState(tsdModels[i].getName());
				info.setTitle(tsdModels[i].getName());
				info.setClzName(TBSheetViewer.class.getName());
			}
			info.setRegion("CENTER");
			v = openView(info, getMainboard(), false);
			final String id = info.getDockableId();

			Dockable dockable = dmng.getDockable(id);

			TBSheetViewer tr = (TBSheetViewer) v;
			//�������Ϣ������������ҵ��
			if(tsdModels[i].getName().equals(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0486")/*@res "�ֻ�����"*/)){
				//nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0486")/*@res "�ֻ�����"*/)
				this.coverTbSheetViewer=tr;

//				continue;
			}
			sheetViewList.add(tr);
			if (mode==ITbPlanActionCode.mode_cellcontrast) {
				tr.setCellContractRender(render);
				tr.setVersionTask(m_task, taskExtInfoLoader,
						ITbPlanActionCode.mode_cellcontrast);
				TaskSheetDataModel[] cmTaskSheetDataModel = new TaskSheetDataModel[1];
				cmTaskSheetDataModel[0] = tsdModels[i];
				tr.setTsDataModel(cmTaskSheetDataModel[0]);
				tr.setScale(this.scale);
			} else {
				tr.setTask(m_task);// �����񴫸���ǰ����ͼ
				tr.setTsDataModel(tsdModels[i]);
				// ����ǰ�ı༭״̬���ݸ�TBSheetViewer
				tr.setiMode(tr.getTbPlanContext() == null ? ITbPlanActionCode.mode_taskview
						: tr.getTbPlanContext().getComplieStatus());
			}
			tr.setMdSheet(tsdModels[i].getMdSheet());
			tr.setViewManager(ViewManager.this);
			tr.setPk_sheets(ViewManager.this.taskValidLookSheets);
			tr.setTitle(tsdModels[i].getName());
			tr.setTaskExtInfoLoader(taskExtInfoLoader);
			if (tr.getCellsModel() != null) {
				// ���CellsModel��Ϊ�գ���Ҫ����ˢ��
				try {
					tr.initTable();
					DataCellFroozeUtil.checkAndFreezeTabel(
							tr.getTable(), m_task.getPrimaryKey(),
							tr.getPkMdSheet(), tr.getTsDataModel()
        							.getMdSheet().getCsmodel()
        							.getRowNum(), tr
        							.getTsDataModel().getMdSheet()
        							.getCsmodel().getColNum());
				} catch (Throwable te) {
					NtbLogger.error(te);
				}
			}
			if (lastSelectSheetViewer != null) {
				if (lastSelectSheetViewer.equals(v.getId())) {
					// �Ѿ���¼�����һ�ε�sheet���˴�ֱ�Ӷ�λ�����sheet��ͨ��sheetid��λ��
					viewDisplay = (TBSheetViewer) v;
					dockableDisplay = dockable;
				}

			} else if (viewDisplay == null) {
				// û�м�¼sheet��Ĭ�϶�λ����һ��
				viewDisplay = (TBSheetViewer) v;
				dockableDisplay = dockable;
			}

			if (viewDisplay == null) {
				// û�ж�λ��sheet��Ĭ�϶�λ�����
				viewDisplay = (TBSheetViewer) v;
				dockableDisplay = dockable;
			}
		}
		if(sheetViewList.size()<=0){
			MessageDialog.showHintDlg(mainboard, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0283")/*@res "��ʾ��"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0487")/*@res "����Χ�����쳣���ױ���sheet��ʾ�ѱ��޸ģ������������ڵ��������ñ���Χ��"*/);
			return;
		}
		// ��ʾlastSelectSheetViewerҳǩ���ߵ�һ��ҳǩ
		if (dockableDisplay != null) {
			if (dockableDisplay instanceof ViewAdapter) {
				if (((ViewAdapter) dockableDisplay).getParent() instanceof KTabbedPane) {
					KTabbedPane tabbedPane = (KTabbedPane) (((ViewAdapter) dockableDisplay)
							.getParent());
					if (tabbedPane.getChangeListeners() != null
							&& tabbedPane.getChangeListeners().length > 0) {
						for (ChangeListener cl : tabbedPane
								.getChangeListeners()) {
							if (cl instanceof TabChangedListener) {
								tabbedPane.removeChangeListener(cl);
							}
						}
					}
					tabbedPane.addChangeListener(new TabChangedListener());
					tabbedPane.setAddCloseBtn(false);
					tabbedPane.revalidate();
				}
			}
			// �����ȼ���viewer�������ʼ����ʱ�������Ҳ�����ǰviewer������������Ҵ�������
			dmng.display(dockableDisplay);
			this.getTbPlanContext().setCurrReportViewer(viewDisplay);
			// ֻ��ʼ��ĳһ��ҳǩ, �ܹ���Լһ����ʱ��
			if (viewDisplay.getCellsModel() == null) {
				try {
					viewDisplay.initTable();
					if(viewDisplay.getCellsModel() != null&&ITbPlanActionCode.mode_cellcontrast==mode){
						DataCellFroozeUtil.freezeHeader(viewDisplay.getTable(), viewDisplay.getPkMdSheet());
					}
				} catch (Throwable te) {
					NtbLogger.error(te);
				}
			}
			// xuzx -- remove the code block because of bugs
			// if(taskLayoutRecord != null){
			// if(taskLayoutRecord.getColVector(viewDisplay.getMdSheet().getPk_obj())
			// != null &&
			// taskLayoutRecord.getColVector(viewDisplay.getMdSheet().getPk_obj()).size()
			// > 2){
			// viewDisplay.getCellsModel().getColumnHeaderModel().setHeaders(taskLayoutRecord.getColVector(viewDisplay.getMdSheet().getPk_obj()).toArray(new
			// Header[]{}));
			// }
			//
			// if(taskLayoutRecord.getRowVector(viewDisplay.getMdSheet().getPk_obj())
			// != null &&
			// taskLayoutRecord.getRowVector(viewDisplay.getMdSheet().getPk_obj()).size()
			// > 2){
			// viewDisplay.getCellsModel().getRowHeaderModel().setHeaders(taskLayoutRecord.getRowVector(viewDisplay.getMdSheet().getPk_obj()).toArray(new
			// Header[]{}));
			// }
			// }
		}
		// ɾ��Ĭ�ϱ�
		if (tsdModels.length > 0) {
			Dockable dockabletemp = getMainboard().getDockingManager()
					.getDockable("tb.report.sheet.view");
			if (dockabletemp != null) {
				dockabletemp.getDockingContainer().getDockingManager()
						.close(dockabletemp, false);
			}
		}
		if (dockableDisplay instanceof ViewAdapter) {
			if (((ViewAdapter) dockableDisplay).getParent() instanceof KTabbedPane) {
				KTabbedPane tabbedPane = (KTabbedPane) (((ViewAdapter) dockableDisplay)
						.getParent());
				tabbedPane.setSelectedComponent((ViewAdapter) dockableDisplay);
			} else {
				DockingViewport dv = getMainboard().getRootDockingViewport();
				Component cm = dv.getDockedComponent();
				if (cm instanceof DockingSplitPane) {
					DockingSplitPane dsp = (DockingSplitPane) cm;
					dsp.setDividerLocation(250);
					dsp.setDividerSize(2);
					dsp.doLayout();
				}
			}
		}
		//��ʼ���������Ϣ
		if(coverTbSheetViewer!=null){
			setCoverMesToTask(coverTbSheetViewer.getTask()) ;
		}
		if(ITbPlanActionCode.mode_cellcontrast!=mode){
			setRowWidthAndColHeight();}//�汾�ԱȽ��治��Ҫ
		initInputDir();
	}
	private void initInputDir() {
		int inputDir=getTbPlanContext().getInputDir();
		 ActionEvent actionevent=new ActionEvent(getMainboard(),1002,null);
		 if(getMainboard().getPluginManager().getPlugin("nc.ui.tb.zior.pluginaction.edit.PluginAction_EditPlugin")!=null){
				if(inputDir == TbKeyInputEditor.DIR_DOWN){
			        getMainboard().getPluginManager().getPlugin("nc.ui.tb.zior.pluginaction.edit.PluginAction_EditPlugin").getAction(PluginAction_InputDownDirect.class).execute(actionevent);
				}else if(inputDir == TbKeyInputEditor.DIR_UP){
					getMainboard().getPluginManager().getPlugin("nc.ui.tb.zior.pluginaction.edit.PluginAction_EditPlugin").getAction(PluginAction_InputUpDirect.class).execute(actionevent);
				}else if (inputDir == TbKeyInputEditor.DIR_LEFT){
					getMainboard().getPluginManager().getPlugin("nc.ui.tb.zior.pluginaction.edit.PluginAction_EditPlugin").getAction(PluginAction_InputLeftDirect.class).execute(actionevent);
				}else if (inputDir == TbKeyInputEditor.DIR_RIGHT){
					getMainboard().getPluginManager().getPlugin("nc.ui.tb.zior.pluginaction.edit.PluginAction_EditPlugin").getAction(PluginAction_InputRightDirect.class).execute(actionevent);
				}else{
					getMainboard().getPluginManager().getPlugin("nc.ui.tb.zior.pluginaction.edit.PluginAction_EditPlugin").getAction(PluginAction_InputDownDirect.class).execute(actionevent);
				}
		 }

}
	public void removeAllViewers0() {
		if(sheetViewList==null||sheetViewList.size()<=0){
			return;
		}
		// ��û��TBSheetViewerʱ��Ĭ�ϱ�
		if (getMainboard().getView("tb.report.sheet.view") == null
				&& sheetViewList.size() > 0) {
			DockingState info = new DockingState("tb.report.sheet.view");
			info.setClzName(TBSheetViewer.class.getName());
			String busiCode=getTbPlanContext().getSysCode()==null?IBusiTermConst.SYS_TB:getTbPlanContext().getSysCode();
			info.setTitle(BusiTermConst.getMultiLangTextWithSysCode(busiCode, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0485")/*@res "Ĭ�ϱ�"*/));//NCLangRes.getInstance().getStrByID("tbb_plan",
			info.setRegion(DockingConstants.CENTER_REGION);
			info.setSplitRatio(0.8f);
			OpenViewAction.openView(info, getMainboard());

		}
		getMainboard().getEventManager();
		DockingManager dmng = this.mainboard.getDockingManager();
		for (TBSheetViewer viewer : sheetViewList) {
			removeSheetViewerTitleActions(viewer) ;
			if(viewer.getCellsModel()!=null){
				viewer.getCellsModel().getRowHeaderModel().clearListeners();
			}
			Dockable dockable = getMainboard().getDockingManager().getDockable(
					viewer.getId());
			if (dockable != null) {
				getMainboard().getEventManager().removeListener(
						new TitleActionAdapter(new FullContentPageAction(
								getTbPlanContext().getPkFuncCode(), "tb.report.sheet.view")));
				getMainboard().getEventManager().removeListener(
						new TitleActionAdapter(new DelLinePageAction(
								getTbPlanContext().getPkFuncCode(), "tb.report.sheet.view")));
				getMainboard().getEventManager().removeListener(
						new TitleActionAdapter(new AddLinePageDwonAction(
								getTbPlanContext().getPkFuncCode(), "tb.report.sheet.view")));
				getMainboard().getEventManager().removeListener(
						new TitleActionAdapter(new AddLinePageUpAction(
								getTbPlanContext().getPkFuncCode(), "tb.report.sheet.viewtb.report.sheet.view")));
				getMainboard().getEventManager().removeListener(
						new TitleActionAdapter(new AddMultiLinePageAction(
								getTbPlanContext().getPkFuncCode(), "tb.report.sheet.view")));
				dockable.getDockingContainer().getDockingManager()
						.close(dockable , false);
				// ����������dockable��DockingState��ǣ��������ǰһ�ε�sheet�������ص�sheet����
				// �ͻ���ɽ�����ң�������Ҫ���ѵ�������sheet��idȡ����MdSheet��id����ȷ������Ӧ���Ǽ���Task��pk��
				// xuzx 20130521
				getMainboard().getPerspectiveManager().getCurrentPerspective()
						.getLayout().remove(viewer.getId());
			}
		}
		
		sheetViewList.clear();
		getTbPlanContext().clearViewCache();
	}
	public void removeAllViewers() {
		removeAllViewers0();
		taskDataModel = null;
		m_task = null;
	}
	private void removeSheetViewerTitleActions(TBSheetViewer viewer) {
		//getMainboard().getEventManager().removeListener(listeners);
		viewer.removeTitleAction("���");
		viewer.removeTitleAction("ɾ��");
		viewer.removeTitleAction("�������");
		viewer.removeTitleAction("��ǰ����");
		viewer.removeTitleAction("��������");
		}
	public void stopAllViewEditing() {
		iMode = prevMode;
		for (TBSheetViewer tbSheetViewer : sheetViewList) {
			tbSheetViewer.stopEdit();
		}
	}

	public void startEditing() {
		prevMode = iMode;
		iMode = TbReportFactory.mode_taskedit;
		for (TBSheetViewer tbSheetViewer : sheetViewList) {
			tbSheetViewer.startEdit();
		}
	}

	public boolean isEditing() {
		return iMode == TbReportFactory.mode_taskedit ? true : false;
	}

	private void getTaskDataModelFromDb(String[] pks) throws BusinessException {
		boolean isLoadConsistRule = false;
		MdTaskDef def=getTbPlanContext().getTaskDef()==null?m_task.getTaskDefWithoutDetail():getTbPlanContext().getTaskDef();
		String taskType=/*m_task.getTaskDefWithoutDetail()*/def.getTasktype();
		if (getTbPlanContext() == null) {
			return;
		}
		Map<String,Object> paras=new HashMap<String,Object>();//����һЩ��Ҫ��ѯ��������ʱ������չ�ӿڲ����Ĳ�����
		String[] publicNodePks= (getTbPlanContext() == null || getTbPlanContext().getZiorOpenNodeModel() == null) ? null : getTbPlanContext().getZiorOpenNodeModel().getPublicnodeLookSheets();
		paras.put(ITbPlanActionCode.PUBLICNODEPKS, publicNodePks);
		paras.put(ITbPlanActionCode.SHEETGROUPNAME, getTbPlanContext().getSheetGroupName());
		String nodeType = getTbPlanContext().getNodeType();
		//�ԱȰ汾������
		Object obj = m_task.getExtrAttribute(ITaskConst.versionTask);
		paras.put(ITaskConst.versionTask, obj);
		paras.put(ITbPlanActionCode.USERPK_ORG, WorkbenchEnvironment.getInstance().getLoginUser().getPk_org());
		Map<String,Object> taskDataMesMap=ZiorFrameCtl.getTaskDataMesMap(m_task, pks, isLoadConsistRule, null, true, nodeType,paras);
		taskDataModel=(TaskDataModel) taskDataMesMap.get(ITbPlanActionCode.TASKDATAMODEL);
		this.taskValidLookSheets = (String[]) taskDataMesMap.get(ITbPlanActionCode.TASKVALIDLOOKSHEETS);
		taskExtInfoLoader.clear();
	  	((TaskExtInfoLoader) taskDataMesMap.get(ITbPlanActionCode.TASKECTINFOLOADER)).setM_task(taskExtInfoLoader.getM_task());
		((TaskExtInfoLoader) taskDataMesMap.get(ITbPlanActionCode.TASKECTINFOLOADER)).setPk_shes(taskExtInfoLoader.getPk_shes());
		taskExtInfoLoader=(TaskExtInfoLoader) taskDataMesMap.get(ITbPlanActionCode.TASKECTINFOLOADER);
		 UFDateTime serverTime = taskDataModel.getLoadTime() != null ? taskDataModel.getLoadTime():WorkbenchEnvironment.getServerTime();
		getTbPlanContext().setCurrentTs(serverTime);

	}

	/**
	 * ��ȡtaskde�ɼ�sheet��Χ liyingm+
	 *  User2SheetCvsTools.getPkSheets(pk_user,pk_group,pk_��������,)��
	 * �����û�����ְ��ı�pk�����ְ�����ñ��н���Ļ�pk�п����ظ���
	 * ��������TaskDataModel�ı�pk�ظ�Ҳû�й�ϵ����û�п����������򷵻�null
	 * �ɼ���Ϊ�գ��������ɼ������Χ
	 * isApp=trueΪ��������Χ�����������ڵ�����
	 * @return
	 */
	private String[] getTaskValidLookPkSheets(boolean isApp) {
		//������Ǩ����TbZiorUiCtl-qy-20141114
		taskValidLookSheets = TbZiorUiCtl.getTaskValidLookPkSheets(getMdTask(), getTbPlanContext(), isApp);
		return taskValidLookSheets;
	}

	/**
	 * ��ȡtask�������Χ���ɲ����ı���Χ��
	 * �������������ڵ�ʱչ��Ҫ�����ı�
	 * @return
	 */
	private String[] getTaskValidPkSheets() {
		taskValidSheets=SheetGroupCtl.getPkSheetsByTaskSheetList(getMdTask().getSheetlist(), false);
		return taskValidSheets;
	}
	public void refreshByTaskDataModel(TaskDataModel taskDataModel) {
		setTaskDataModel(taskDataModel);
		// ����һ�����һ�ε�ҳǩid
		if (getTbPlanContext() != null
				&& getTbPlanContext().getCurrReportViewer() != null) {
			this.lastSelectSheetViewer = getTbPlanContext()
					.getCurrReportViewer().getId();
		}
		initSheetViewers();
	}


	public void refreshByTaskDataModel(TaskDataModel taskDataModel,MdTask mdtask) {
		this.m_task = mdtask;
		setTaskDataModel(taskDataModel);
		// ����һ�����һ�ε�ҳǩid
		if (getTbPlanContext() != null
				&& getTbPlanContext().getCurrReportViewer() != null) {
			this.lastSelectSheetViewer = getTbPlanContext()
					.getCurrReportViewer().getId();
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				initSheetViewers();
			}
		});
	}
	/**
	 * ˢ��֮����ʾ��һ��ҳǩ
	 */
	public void refresh() {
		refresh(true);
	}

	public void refresh(boolean isloadFromDB) {
		if (isloadFromDB) {
			refreshTables();
		} else {
			setAllViewsTsModelToCellsModel();
		}
	}

	/**
	 * ˢ��֮����ʾlastSelectedViewerҳǩ
	 */
	public void refresh(TBSheetViewer lastSelectedViewer) {
		getTbPlanContext().setSaveRowColSizeMap( ZiorFrameCtl.loadRowColSize(getMdTask()));
		this.lastSelectSheetViewer = lastSelectedViewer.getId();
		//refreshTables();
		if(getTbPlanContext().getTasks()!=null&&getTbPlanContext().getTasks().length>0)
		{
			setTask(getTbPlanContext().getTasks()[0], null,true);
		}
	}

	public void refreshTree() {
		TbReportDirView tbReportDirView = (TbReportDirView) this.getMainboard()
				.getView("tb.report.dir.view");
		tbReportDirView.refresh();
	}

	public TbPlanContext getTbPlanContext() {
		return (TbPlanContext) getMainboard().getContext().getAttribute(
				"nc.ui.tb.zior.tbplancontext");
	}

	public void setAllViewsDataToTsModel() {
		for (TBSheetViewer tbSheetViewer : sheetViewList) {
			tbSheetViewer.updateDataCellValue();
			 tbSheetViewer.getViewFmtData();
		}
	}
	public void setVarValidChangedMap() {
		for (TBSheetViewer viewer : sheetViewList) {
			viewer.setVarValidChangedMap();
		}
	}
	public void setAllViewsTsModelToCellsModel() {
		for (TBSheetViewer tbSheetViewer : sheetViewList) {
			// tbSheetViewer.loadVarAreas(tbSheetViewer.getCellsModel(),
			// tbSheetViewer.getTsDataModel().getVarAreaDefMap(),
			// tbSheetViewer.getTsDataModel().getVarMap());
			tbSheetViewer.setDataToCellsModel(tbSheetViewer.getTsDataModel(),
					tbSheetViewer.getCellsModel(), taskExtInfoLoader);
//			tbSheetViewer.getViewFmtData();
			tbSheetViewer.repaint();
		}
	}

	public TBSheetViewer getTbSheetViewerByName(String name){
		for (TBSheetViewer tbSheetViewer : sheetViewList) {
			if(tbSheetViewer.getMdSheet().getObjname().trim().equals(name.trim())){
				return tbSheetViewer;
			}
		}
		return null;
	}

	public boolean hasSheetDataChanged() {
		boolean ischanged = false;
		for (TBSheetViewer tbSheetViewer : sheetViewList) {
			if (tbSheetViewer.getTsDataModel().isSheetDataChanged()) {
				ischanged = true;
				break;
			}
		}

		return ischanged;
	}

	/**
	 * ��ȡ����ҳǩ���޸Ĺ���DataCell(�����ӿڸ�ʽ)
	 *
	 * @return
	 */
	public Map<String, Object> getChangedWrappedDataCells(){
		for (TBSheetViewer tbSheetViewer : sheetViewList) {
			tbSheetViewer.updateDataCellValue();
		}
		//��ǰ�����Ƿ��޸Ĺ�, ��Ҫ��ԷǶ�ά����
		boolean isChanged = false;
		//������ĵ�Ԫ��
		Map<MdTask, List<WrappedDataCell>> rtn = new LinkedHashMap<MdTask, List<WrappedDataCell>>();
		for (TBSheetViewer tbViewer : sheetViewList) {
			MdTask task = tbViewer.getTask();
			if(task == null)
				continue;
//			Map<String, Object> map = tbViewer.getChangedWrappedDataCells(false);
			Map<String, Object> map = tbViewer.getChangedWrappedDataCells(true);
			if(map == null)
				continue;
			List<WrappedDataCell> listDcs = null;
			Object obj = map.get(TbZiorUiCtl.KEY_DATACELL);
			if(obj != null)
				listDcs = (List<WrappedDataCell>)obj;
			if(listDcs != null){
				List<WrappedDataCell> list = rtn.get(task);
				if(list == null){
					list = new ArrayList<WrappedDataCell>();
					rtn.put(task, list);
				}
				list.addAll(listDcs);
			}
			if(!isChanged){
				obj = map.get(TbZiorUiCtl.KEY_CHANGED);
				if(obj != null && "Y".equals(obj))
					isChanged = true;
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(TbZiorUiCtl.KEY_DATACELL, rtn);
		map.put(TbZiorUiCtl.KEY_CHANGED, isChanged ? "Y" : "N");
		return map;
	}

	public static Viewer openView(DockingState info, IDockingContainer container) {
		return openView(info, container, false);
	}

	public static Viewer openView(DockingState info,
			IDockingContainer container, boolean isDisplay) {
		if (info == null) {
			throw new IllegalArgumentException();
		}
		final String id = info.getDockableId();
		DockingManager dmng = container.getDockingManager();
		Dockable dockable = dmng.getDockable(id);
		if (dockable != null) {
			RestorationManager.getInstance().restore(dockable);
			if (dockable instanceof ViewAdapter) {
				return ((ViewAdapter) dockable).getViewer();
			}
			return null;
		}

		Perspective currentPerspective = container.getPerspectiveManager()
				.getCurrentPerspective();
		currentPerspective.getLayout().addDockingState(info);

		PerfWatch pw = new PerfWatch("CreateView: " + id);

		ListenerLifecycle listenerLifecycle = container.getEventManager()
				.getListenerLifecycle();
		try {

			dockable = dmng.createDockable(id);// ��ͼ����
			RestorationManager.getInstance().restore(dockable);// ��ӵ�����,������ͼ
			if (isDisplay) {
				dmng.display(dockable);
			}
			dockable.init();// ��ͼ��ʼ�����ɷ����¼������ز��
		} catch (Throwable e) {
			AppDebug.debug(e);
		} finally {
			listenerLifecycle.stopLifecycle();
			pw.stop();
		}

		if (dockable != null && dockable instanceof ViewAdapter) {
			Viewer view = ((ViewAdapter) dockable).getViewer();
			return view;
		}

		return null;
	}

	/**
	 * ������ļ���
	 *
	 * @author liyingm
	 */
	public static Viewer closeView(DockingState info,
			IDockingContainer container) {
		if (info == null) {
			throw new IllegalArgumentException();
		}
		final String id = info.getDockableId();
		DockingManager dmng = container.getDockingManager();
		Dockable dockable = dmng.getDockable(id);
		if (dockable != null) {
			dmng.display(dockable);
			if (dockable instanceof ViewAdapter) {
				return ((ViewAdapter) dockable).getViewer();
			}
			return null;
		}

		Perspective currentPerspective = container.getPerspectiveManager()
				.getCurrentPerspective();
		currentPerspective.getLayout().addDockingState(info);

		PerfWatch pw = new PerfWatch("CreateView: " + id);

		ListenerLifecycle listenerLifecycle = container.getEventManager()
				.getListenerLifecycle();
		try {

			dockable = dmng.createDockable(id);// ��ͼ����
			dmng.display(dockable);// ��ӵ�����,������ͼ
			dockable.init();// ��ͼ��ʼ�����ɷ����¼������ز��
			if (dockable instanceof ViewAdapter) {
				if (((ViewAdapter) dockable).getParent() instanceof KTabbedPane) {
					KTabbedPane tabbedPane = (KTabbedPane) (((ViewAdapter) dockable)
							.getParent());
					tabbedPane.doLayout();
					tabbedPane.ScrollVisibleToSelect();
				}
			}

		} catch (Throwable e) {
			AppDebug.debug(e);
		} finally {
			listenerLifecycle.stopLifecycle();
			pw.stop();
		}

		if (dockable != null && dockable instanceof ViewAdapter) {
			Viewer view = ((ViewAdapter) dockable).getViewer();
			return view;
		}

		return null;
	}

	public void changeAllViewsValueScale(int newvalueScale, int oldValueSacle) {
		if (newvalueScale != oldValueSacle) {
			for (TBSheetViewer tbSheetViewer : sheetViewList) {
				tbSheetViewer.changValueScale(newvalueScale, oldValueSacle);
				tbSheetViewer.repaint();
			}
		}

	}
	/**
	 *
	 * {����������������}
	 *
	 * @param pks
	 * @param isNotALLIfNotLook,����Ϊfalseʱ��������ɼ���Ϊnull������ʱtaskdatamodel�ϵ����б���true����û�пɼ�������ʱĬ�ϱ�
	 * @author:
	 */
	public void changeAllViewsBySheetGroupPks(String[] pks,boolean isNotALLIfNotLook) {

		//pks�û�ѡ��ı�������ı�
		//�õ��������ڵ����õĿɼ�����Χ
		this.isNotALLIfNotLook=isNotALLIfNotLook;
	    String[] lookSheets=null;
		String[] oldpks=getTaskValidLookPkSheets(this.isApp);


		if(pks!=null&&pks.length>0){
			if(oldpks!=null&&oldpks.length>0){
				List<String>  list1=new ArrayList<String>();
				List<String>  list2=new ArrayList<String>();
				for(String pk:pks){
					list1.add(pk);
				}
				for(String o:oldpks){
					if(list1.contains(o)){
						list2.add(o);
					}
				}
				if(list2.size()>0){
					lookSheets=list2.toArray(new String[0]);
				}
			}
		}

        //��������չʾ�ı���Χ
		this.taskValidLookSheets=lookSheets;
		removeAllViewers0();
		initSheetViewers();

	}
	public static void removeUsableTitleAction(Viewer view) {
		view.removeTitleAction("com.ufida.zior.docking.view.actions.DefaultCloseAction");
		view.removeTitleAction(NCLangRes.getInstance().getStrByID("tbb_plan",
				"01812pln_000576")/* �ر� */);/* notranslate */
		view.removeTitleAction("close");
		view.removeTitleAction("com.ufida.zior.docking.view.actions.DefaultPinAction");
		view.removeTitleAction(NCLangRes.getInstance().getStrByID("tbb_plan",
				"01812pln_000116")/* ��С�� */);
		view.removeTitleAction("pin");
	}

	public void setCheckMesssage(HashMap<String, List<Object>> mapCheck) {
		this.mapOnly=mapCheck;
	}
	/**
	 * ��ƺõķ�����Ϣչ����������
	 */
	private void setCoverMesToTask(MdTask task) {
		ZiorCoverVO ziorCoverVO=null;
		if (coverTbSheetViewer.getCellsModel() == null) {
			coverTbSheetViewer.initTable();
		}
		try {
			ziorCoverVO = GZWCoverServiceGetter.getCoverService().queryFrontCoverDataByWhere("pk_task='"+task.getPrimaryKey()+"'");
		} catch (BusinessException e) {
			NtbLogger.error(e.getMessage());
		}
		if(ziorCoverVO==null){
			return;
		}
		 //��λ����
		  CellPosition namePos=CellPosition.getInstance("C14");
		  coverTbSheetViewer.getCellsModel().getCell(namePos).setValue(ziorCoverVO.getOrgname());
       //��λ������
		  CellPosition managerPos=CellPosition.getInstance("C16");
		 coverTbSheetViewer.getCellsModel().getCell(managerPos).setValue(ziorCoverVO.getOrgManager());
       //�ܻ��ʦ
		 CellPosition accountantPos=CellPosition.getInstance("C18");
		 coverTbSheetViewer.getCellsModel().getCell(accountantPos).setValue(ziorCoverVO.getChefAccountant());
       //�������
		 CellPosition agencyManPos=CellPosition.getInstance("C20");
		 coverTbSheetViewer.getCellsModel().getCell(agencyManPos).setValue(ziorCoverVO.getAgencyManager());
       //�����
		 CellPosition operatorPos=CellPosition.getInstance("C22");
		 coverTbSheetViewer.getCellsModel().getCell(operatorPos).setValue(ziorCoverVO.getOperator());
       //�绰����
		 CellPosition districtNumPos=CellPosition.getInstance("C24");
		 CellPosition extNumPos=CellPosition.getInstance("G24");
		 CellPosition telPos=CellPosition.getInstance("D24");
		 coverTbSheetViewer.getCellsModel().getCell(districtNumPos).setValue(ziorCoverVO.getDistrictNum());
		 coverTbSheetViewer.getCellsModel().getCell(extNumPos).setValue(ziorCoverVO.getExtNum());
		 coverTbSheetViewer.getCellsModel().getCell(telPos).setValue(ziorCoverVO.getTel());
       //����ҵ����
		 CellPosition selfCode=CellPosition.getInstance("O2");
		 coverTbSheetViewer.getCellsModel().getCell(selfCode).setValue(ziorCoverVO.getSelfCode());
       //�ϼ���ҵ����
		 CellPosition parentCodePos=CellPosition.getInstance("O3");
		 coverTbSheetViewer.getCellsModel().getCell(parentCodePos).setValue(ziorCoverVO.getParentCode());
       //�����ܴ���
		 CellPosition groupCodePos=CellPosition.getInstance("O4");
		 coverTbSheetViewer.getCellsModel().getCell(groupCodePos).setValue(ziorCoverVO.getGroupCode());
       //������ϵ
		 CellPosition relationPos=CellPosition.getInstance("O7");
		 CellPosition combRelationPos=CellPosition.getInstance("O6");
		 coverTbSheetViewer.getCellsModel().getCell(relationPos).setValue(ziorCoverVO.getRelation());
		 coverTbSheetViewer.getCellsModel().getCell(combRelationPos).setValue(ziorCoverVO.getCombRelation());
//       //���ŷ���
//		 CellPosition divisionCodePos=CellPosition.getInstance("C18");
//		 CellPosition combDeptPos=CellPosition.getInstance("C18");
//		 coverTbSheetViewer.getCellsModel().getCell(divisionCodePos).setValue(ziorCoverVO.getDivisionCode());
//		 coverTbSheetViewer.getCellsModel().getCell(combDeptPos).setValue(ziorCoverVO.getCombDept());
       //���ڵ���
		 CellPosition locationPos=CellPosition.getInstance("O11");
		 CellPosition combLocationPos=CellPosition.getInstance("O10");
		 coverTbSheetViewer.getCellsModel().getCell(locationPos).setValue(ziorCoverVO.getLocation());
	     coverTbSheetViewer.getCellsModel().getCell(combLocationPos).setValue(ziorCoverVO.getCombLocation());
       //������ҵ��
	     CellPosition busiCodePos=CellPosition.getInstance("O14");
	     CellPosition combBusiCodePos=CellPosition.getInstance("O13");
		 coverTbSheetViewer.getCellsModel().getCell(busiCodePos).setValue(ziorCoverVO.getBusiCode());
		 coverTbSheetViewer.getCellsModel().getCell(combBusiCodePos).setValue(ziorCoverVO.getCombBusiCode());
       //��Ӫ��ģ
		 CellPosition busiScalePos=CellPosition.getInstance("O16");
		 //CellPosition combScalePos=CellPosition.getInstance("C18");
		 coverTbSheetViewer.getCellsModel().getCell(busiScalePos).setValue(ziorCoverVO.getBusiScale());
		 //coverTbSheetViewer.getCellsModel().getCell(combScalePos).setValue(ziorCoverVO.getCombScale());
       //�����ʽ
		 CellPosition orgFormPos=CellPosition.getInstance("O21");
//		 CellPosition combDivisionPos=CellPosition.getInstance("C18");
		 CellPosition detailPos=CellPosition.getInstance("N21");
		 coverTbSheetViewer.getCellsModel().getCell(orgFormPos).setValue(ziorCoverVO.getOrgForm());
//		 coverTbSheetViewer.getCellsModel().getCell(combDivisionPos).setValue(ziorCoverVO.getCombDivision());
		 coverTbSheetViewer.getCellsModel().getCell(detailPos).setValue(ziorCoverVO.getDetail());
       //��Ʊ����
		 CellPosition stockCodePos=CellPosition.getInstance("N19");
		 coverTbSheetViewer.getCellsModel().getCell(stockCodePos).setValue(ziorCoverVO.getStockCode());
       //������
		 CellPosition foundYearPos=CellPosition.getInstance("O25");
		 coverTbSheetViewer.getCellsModel().getCell(foundYearPos).setValue(ziorCoverVO.getFoundYear());
       //��������
		 CellPosition sheetTypePos=CellPosition.getInstance("O24");
		 //CellPosition combJsheetTYpePos=CellPosition.getInstance("C18");
		 coverTbSheetViewer.getCellsModel().getCell(sheetTypePos).setValue(ziorCoverVO.getSheetType());
		// coverTbSheetViewer.getCellsModel().getCell(combJsheetTYpePos).setValue(ziorCoverVO.getCombJsheetTYpe());
       //������
		 CellPosition spareCodePos=CellPosition.getInstance("O28");
		 coverTbSheetViewer.getCellsModel().getCell(spareCodePos).setValue(ziorCoverVO.getSpareCode());


	}
	/**
	 * pk_sheets �ɼ�����Χ
	 * ˢ�����񣬲������ѡ���������һ�ε�һ����
	 */
	public void setRefreshTask(MdTask task, String[] pk_sheets) {
		releaseDataCell();
		// ����һ�����һ�ε�ҳǩid  �ŵ�removeAllViewers()ǰ�������Ĭ�ϱ�ռ����ʱ�ᱻˢ��  lym  �벻Ҫ�ٻ��ط�
		if (getTbPlanContext() != null
				&& getTbPlanContext().getCurrReportViewer() != null) {
			this.lastSelectSheetViewer = getTbPlanContext()
					.getCurrReportViewer().getId();
		}

		if (task == null) {
			removeAllViewers();
			return;
		}
		if (pk_sheets == null) {
			pk_sheets  =this.getTaskValidLookPkSheets(isApp);
		}



		this.m_task = task;
		this.taskValidLookSheets = pk_sheets;
		// try {
		// taskLayoutRecord = (TaskLayoutRecord)
		// TbUserProRefCtl.getInstance().getTaskLayoutRecord(m_task.getPk_obj());
		// } catch (BusinessException e) {
		// NtbLogger.error(e);
		// }

		taskExtInfoLoader.setMdTask(m_task, pk_sheets);
		Thread run = new Thread() {
			@Override
			public void run() {
				synchronized(mainboard){
				refreshTables();
				}
			}
		};
		synchronized (mainboard) {


		if (TbPlanFrameUtil.getTbPlanFrame(getMainboard()) == null) {
			refreshTables();
		} else {
			TbPlanFrameUtil.getTbPlanFrame(getMainboard()).invokeWithPorgress(
					run);
		}
		removeAllViewers0();
		}
	}
	public boolean isApp() {
		return isApp;
	}

	public void setApp(boolean isApp) {
		this.isApp = isApp;
	}
	
	/**��ͣ���־ǿ  at:20200717
	 * ��������ִ��excel
	 *
	 * @throws BusinessException
	 */
	public CompileSaveLogs saveTasksNoExcel() throws BusinessException {
		TbPlanContext tbPlanContext = getTbPlanContext();
		CompileSaveLogs compileSaveLogs=new CompileSaveLogs();
		String message = null;
		// ÿ�α���ǰ�Ƚ���ʶ�ÿ�
		if (tbPlanContext != null && getMdTask() != null) {
			TBSheetViewer currViewer = tbPlanContext.getCurrReportViewer();
			boolean isIndexApprove = tbPlanContext.isIndexApprove();
			// ΪTURE��Ϊָ�������ı���,�����Ǳ��Ƶı���
			if (isIndexApprove) {
				// ָ���������涯��
				boolean sucessFlag = doIndexApproveSave(compileSaveLogs);
				return compileSaveLogs;
			}

			

			if (currViewer != null) {
				currViewer.getCellsPane().editingStopped(
						new ChangeEvent(currViewer));
				// ɾ�����п���
				deleteNullRowAndCol();
				// ѯ���ظ�ά�������Ƿ��������+����������ָ��û��ʱ�Ƿ��������
				List<String> repeatDimOrNullIndex=currViewer.getRepeatDimOrNullIndex();
				if (repeatDimOrNullIndex!=null&&repeatDimOrNullIndex.size()>0) {
					String mes="";
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVEREPEATDIM)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0262")/*@res "�����ظ�ά�����,\n"*/;
					}
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVENULLINDEX)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0265")/*@res "ָ��ĳ�Ա����Ϊ��,Ϊ�յ������޷�����!\n"*/;
					}
					   mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0264")/*@res "��ȷ���Ƿ��������?"*/;
					int result = MessageDialog.showYesNoDlg(
							getMainboard(),
							NCLangRes.getInstance().getStrByID("tbb_plan",
									"01812pln_000465")/* ѯ�� */,mes);//NCLangRes.getInstance().getStrByID("tbb_plan","01812pln_000728")/*" ���� �ظ���ָ��"*/);
					if (MessageDialog.ID_NO == result || MessageDialog.ID_CANCEL == result) {
						TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
						return compileSaveLogs;
					}
					TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				}
				// ��CellsModelд��TaskSheetDataModel
				setAllViewsDataToTsModel();
			}
        //��������������ݿ����:(1)ִ�й�ʽ  ��2��ģ�����ݱ���
//			Map<String,Object> map=ZiorFrameCtl.saveTaskDataMesMap(tbPlanContext.getNodeType(), taskDataModel, m_task);
			
			
			 Map<String, Object> map = ((IZiorFrameModelService)NCLocator.getInstance().lookup(IZiorFrameModelService.class)).saveTaskDataNotExcelMesMap(tbPlanContext.getNodeType(), taskDataModel, m_task);
			    ((TaskDataModel)map.get("taskDataModel")).instance();
		 
			
			UFDateTime save=(UFDateTime) map.get(ITbPlanActionCode.SAVETIME);
			taskDataModel=(TaskDataModel) map.get(ITbPlanActionCode.TASKDATAMODEL);
			message = (String) map.get(ITbPlanActionCode.ADJUSTCONTROLRULE);
			
			compileSaveLogs.setAdjustControlRuleMessage(message);
			TbbFormulaExecuteLogs logs=(TbbFormulaExecuteLogs) map.get(ITbPlanActionCode.TBBFORMULAEXECUTELOGS);
			if(logs.haveErr()){
				compileSaveLogs.setRuleMessage(logs.getErrInfo());
			}
			// ��ע�ı���
			saveExOlapInfoSet();
			TaskDataCtl.updateTaskDataModelUICache(getTaskDataModel(), save);
			getTbPlanContext().setCurrentTs(save);
			if (tbPlanContext != null) {
				tbPlanContext.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKVIEW);
			}
			updateViewerDataModel();
			setAllViewsTsModelToCellsModel();
			// �����ˢ�ºͷַ��¼�
			if (currViewer != null) {
				currViewer.getTable().repaint();
				// ȥ�����Ƶ����߿�
				TbDefaultSheetCellRender.stopPlay(currViewer.getCellsPane());
				// �ַ�����¼�
				PluginActionEvent pluginActionEvent = new PluginActionEvent(
						currViewer, 1);
				this.getMainboard().getEventManager()
						.dispatch(pluginActionEvent);
//				if(getCurrentTbSheetViewer()!=null){
//					//�������ע��ϢҪ�رյ� --->�ҵ������������,�ʲ��ſ��˵ط����ſ������Ӳ���Ҫ��Ч������   by liying
//					getCurrentTbSheetViewer().hideAllPostils();
//				}
			}
		}
		return compileSaveLogs;
	}

}
