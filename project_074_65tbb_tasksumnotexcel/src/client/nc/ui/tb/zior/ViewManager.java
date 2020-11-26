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
 * 通过选中节点构造出多个views
 *
 * @author changpeng
 *
 */
public class ViewManager {
	// 版本对比的model的加载标识
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
											.getCellsPane());// 去掉复制的虚线框

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
							// 隐藏一列会导致滚动条在某些表中失效，这里先设置为1
							Header[] headers = v.getCellsModel().getColumnHeaderModel().getHeaders();
							for(int i=0 ; i<headers.length ;i++){
								Header h = headers[i];
								int size = h.getSize();
								if(size == 0){
									v.getCellsModel().getColumnHeaderModel().setSize(i, 1);
								}
							}
							// v.getCellsModel().getSelectModel().setAnchorCell(cp);
							// 切换sheet后需要进行冻结操作
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
							setRowWidthAndColHeight();}//版本对比界面不需要}
							if(ITbPlanActionCode.mode_cellcontrast==mode){
								DataCellFroozeUtil.freezeHeader(v.getTable(), v.getPkMdSheet());

							}
						}
					}
				}

			}
			String busiCode=getTbPlanContext().getSysCode()==null?IBusiTermConst.SYS_TB:getTbPlanContext().getSysCode();
			String til=BusiTermConst.getSysNameByCode(busiCode)+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0485")/*@res "默认表"*/;//NCLangRes.getInstance().getStrByID("tbb_plan",
			//切换任务时要把选择的表单范围记忆一下
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

		//切换主体的时候已经加载20150407
//		if(getTbPlanContext().getSaveRowColSizeMap() == null) {
//			   getTbPlanContext().setSaveRowColSizeMap( ZiorFrameCtl.loadRowColSize(m_task));
//		}


		if(getTbPlanContext().getSaveRowColSizeMap()!=null&&getTbPlanContext().getSaveRowColSizeMap().size()>0&&
				getTbPlanContext().getSaveRowColSizeMap().get(user_pk+m_task.getPk_obj())!=null&&
				getTbPlanContext().getSaveRowColSizeMap().get(user_pk+m_task.getPk_obj()).size()>0&&
				getTbPlanContext().getSaveRowColSizeMap().get(user_pk+m_task.getPk_obj()).get(id)!=null){
			// 设置任务的行宽列高
			CellsModel cellsModel=getTbPlanContext().getCurrReportViewer().getCellsModel();
			HeaderModel rowModel=getTbPlanContext().getCurrReportViewer().getCellsModel().getRowHeaderModel();
			HeaderModel colModel=getTbPlanContext().getCurrReportViewer().getCellsModel().getColumnHeaderModel();
			Map<String,Map> sizeMap=(Map<String, Map>) getTbPlanContext().getSaveRowColSizeMap().get(user_pk+m_task.getPk_obj()).get(id);
			if(sizeMap!=null&&sizeMap.size()>0){
				//保存设置的格式，以节点+用户+任务为主
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
	// 存放当前任务的多个Viewer
	private List<TBSheetViewer> sheetViewList = new ArrayList<TBSheetViewer>();
	private TBSheetViewer coverTbSheetViewer =null;
	// 当前任务
	private MdTask m_task;
	/**任务可操作表单**/
	private String[] taskValidSheets;
	/**任务可见操作表单**/
	private String[] taskValidLookSheets;
	/**版本对比选择的表单**/
	private String[] taskCMPValidLookSheets;
	/** 审批节点加载的表单范围**/
	private boolean isApp=false;
	/** 可见表单范围为null时，则加载taskdatamodel上的表单范围，为true，则完全走可见表单**/
	private boolean isNotALLIfNotLook=false;
	/**是否是刷新按钮调用的刷新**/
	private boolean isRefreshAction=false;

	private TaskExtInfoLoader taskExtInfoLoader = new TaskExtInfoLoader();
	private TaskDataModel taskDataModel = null;
	private int currentValueScale = -1;
	private int iMode = TbReportFactory.mode_taskview;
	private int prevMode = iMode;
	// private TaskLayoutRecord taskLayoutRecord = null;

	private int scale = 1;// 版本对比时选择的要显示的单位（千元万元的设置标志）
	// 多行显示渲染器
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
 * @param pk_sheets  任务的可见表单
 */
	public void setTask(MdTask task, String[] pk_sheets,boolean isRefreshAction) {
		if(task==null){
			removeAllViewers();
			return;
		}
		UISharedData.getInstance().prepareDimMemberCacheForPkOrg(task.getPk_dataent());
		releaseDataCell();
		// 设置一下最后一次的页签id  放到removeAllViewers()前，否则打开默认表占布局时会被刷掉  lym  请不要再换地方
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
			//暂时放上可见表单，不可见的表单不用加载指标和批注信息之类，有问题再换成可操作表单  ly
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
		// 每次保存前先将标识置空
		if (tbPlanContext != null && getMdTask() != null) {
			TBSheetViewer currViewer = tbPlanContext.getCurrReportViewer();
			boolean isIndexApprove = tbPlanContext.isIndexApprove();
			// 为TURE则为指标审批的保存,否则是编制的保存
			if (isIndexApprove) {
				// 指标审批保存动作
				boolean sucessFlag = doIndexApproveSave(compileSaveLogs);
				return;
			}
			// 删除空行空列
			deleteNullRowAndCol();
			if (currViewer != null) {
				currViewer.getCellsPane().editingStopped(
						new ChangeEvent(currViewer));
				// 询问重复维度向量是否继续保存+null指标没填时是否继续保存
				List<String> repeatDimOrNullIndex=currViewer.getRepeatDimOrNullIndex();
				if (repeatDimOrNullIndex!=null&&repeatDimOrNullIndex.size()>0) {
					String mes="";
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVEREPEATDIM)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0262")/*@res "存在重复维度组合,\n"*/;
					}
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVENULLINDEX)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0263")/*@res "指标的成员不能为空,\n"*/;
					}
					   mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0264")/*@res "请确认是否继续保存?"*/;
					int result = MessageDialog.showYesNoDlg(
							getMainboard(),
							NCLangRes.getInstance().getStrByID("tbb_plan",
									"01812pln_000465")/* 询问 */,mes);//NCLangRes.getInstance().getStrByID("tbb_plan","01812pln_000728")/*" 存在 重复的指标"*/);
					if (MessageDialog.ID_NO == result || MessageDialog.ID_CANCEL == result) {
						TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
						return;
					}
					TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				}
				// 从CellsModel写到TaskSheetDataModel
				setAllViewsDataToTsModel();
			}

			// 执行Excel公式和规则
//			executeFmlAndRule();

			// 编辑数据保存动作
			doCompileNoRuleSave();
			// 从TaskSheetDataModel写到CellsModel
						setAllViewsTsModelToCellsModel();
			//********* begin 2015-04-01 (NCdp205325639) quankj 单机版，编制功能，点暂存按钮后，应把保存取消按钮置灰，暂存=保存+不执行公式
			if (currViewer != null) {
				currViewer.getTable().repaint();
				// 去掉复制的虚线框
				TbDefaultSheetCellRender.stopPlay(currViewer.getCellsPane());
				// 分发插件事件
				PluginActionEvent pluginActionEvent = new PluginActionEvent(
						currViewer, 1);
				this.getMainboard().getEventManager()
						.dispatch(pluginActionEvent);
//				if(getCurrentTbSheetViewer()!=null){
//					//如果有批注信息要关闭掉 --->找到其他方法解决,故不放开此地方，放开会增加不必要的效率问题   by liying
//					getCurrentTbSheetViewer().hideAllPostils();
//				}
			}
			//********* end 2015-04-01 (NCdp205325639) quankj 单机版，编制功能，点暂存按钮后，应把保存取消按钮置灰，暂存=保存+不执行公式

		}
	}

	private void doCompileNoRuleSave() throws BusinessException {
		stopAllViewEditing();
		// 批注信息(此处只更新，不覆盖，以保证那些未加载的CellsModel上的批注不会被覆盖)
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

				// 提取批注信息
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
		// 为TURE则为指标审批的保存,否则是编制的保存
		if (!isIndexApprove) {
			saveTaskLayoutRecord(recordMap);
		}
		// 保存数据模型
		getTaskDataModel().save();
		// 保存批注
		TbTaskExtCtl.saveExOlapInfoSet(getMdTask().getPrimaryKey(),TaskExcelObjectConvertor.getOlapInfoList(map, getMdTask()
						.getPrimaryKey()));
		TbPlanContext tbPlanContext = getTbPlanContext();
		if (tbPlanContext != null) {
			//********* begin 2015-04-01 (NCdp205325639) quankj 单机版，编制功能，点暂存按钮后，应把保存取消按钮置灰，暂存=保存+不执行公式
			//tbPlanContext.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKEDIT);
			tbPlanContext.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKVIEW);
			//********* end 2015-04-01 (NCdp205325639) quankj 单机版，编制功能，点暂存按钮后，应把保存取消按钮置灰，暂存=保存+不执行公式
		}
	}



	/**
	 * 保存任务
	 *
	 * @throws BusinessException
	 */
	public CompileSaveLogs saveTasks() throws BusinessException {
		TbPlanContext tbPlanContext = getTbPlanContext();
		CompileSaveLogs compileSaveLogs=new CompileSaveLogs();
		String message = null;
		// 每次保存前先将标识置空
		if (tbPlanContext != null && getMdTask() != null) {
			TBSheetViewer currViewer = tbPlanContext.getCurrReportViewer();
			boolean isIndexApprove = tbPlanContext.isIndexApprove();
			// 为TURE则为指标审批的保存,否则是编制的保存
			if (isIndexApprove) {
				// 指标审批保存动作
				boolean sucessFlag = doIndexApproveSave(compileSaveLogs);
				return compileSaveLogs;
			}

			

			if (currViewer != null) {
				currViewer.getCellsPane().editingStopped(
						new ChangeEvent(currViewer));
				// 删除空行空列
				deleteNullRowAndCol();
				// 询问重复维度向量是否继续保存+浮动新增行指标没填时是否继续保存
				List<String> repeatDimOrNullIndex=currViewer.getRepeatDimOrNullIndex();
				if (repeatDimOrNullIndex!=null&&repeatDimOrNullIndex.size()>0) {
					String mes="";
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVEREPEATDIM)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0262")/*@res "存在重复维度组合,\n"*/;
					}
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVENULLINDEX)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0265")/*@res "指标的成员不能为空,为空的数据无法保存!\n"*/;
					}
					   mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0264")/*@res "请确认是否继续保存?"*/;
					int result = MessageDialog.showYesNoDlg(
							getMainboard(),
							NCLangRes.getInstance().getStrByID("tbb_plan",
									"01812pln_000465")/* 询问 */,mes);//NCLangRes.getInstance().getStrByID("tbb_plan","01812pln_000728")/*" 存在 重复的指标"*/);
					if (MessageDialog.ID_NO == result || MessageDialog.ID_CANCEL == result) {
						TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
						return compileSaveLogs;
					}
					TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				}
				// 从CellsModel写到TaskSheetDataModel
				setAllViewsDataToTsModel();
			}
        //保存数据设计数据库操作:(1)执行公式  （2）模型数据保存
			Map<String,Object> map=ZiorFrameCtl.saveTaskDataMesMap(tbPlanContext.getNodeType(), taskDataModel, m_task);
			UFDateTime save=(UFDateTime) map.get(ITbPlanActionCode.SAVETIME);
			taskDataModel=(TaskDataModel) map.get(ITbPlanActionCode.TASKDATAMODEL);
			message = (String) map.get(ITbPlanActionCode.ADJUSTCONTROLRULE);
			
			compileSaveLogs.setAdjustControlRuleMessage(message);
			TbbFormulaExecuteLogs logs=(TbbFormulaExecuteLogs) map.get(ITbPlanActionCode.TBBFORMULAEXECUTELOGS);
			if(logs.haveErr()){
				compileSaveLogs.setRuleMessage(logs.getErrInfo());
			}
			// 批注的保存
			saveExOlapInfoSet();
			TaskDataCtl.updateTaskDataModelUICache(getTaskDataModel(), save);
			getTbPlanContext().setCurrentTs(save);
			if (tbPlanContext != null) {
				tbPlanContext.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKVIEW);
			}
			updateViewerDataModel();
			setAllViewsTsModelToCellsModel();
			// 保存后刷新和分发事件
			if (currViewer != null) {
				currViewer.getTable().repaint();
				// 去掉复制的虚线框
				TbDefaultSheetCellRender.stopPlay(currViewer.getCellsPane());
				// 分发插件事件
				PluginActionEvent pluginActionEvent = new PluginActionEvent(
						currViewer, 1);
				this.getMainboard().getEventManager()
						.dispatch(pluginActionEvent);
//				if(getCurrentTbSheetViewer()!=null){
//					//如果有批注信息要关闭掉 --->找到其他方法解决,故不放开此地方，放开会增加不必要的效率问题   by liying
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
		// 批注信息(此处只更新，不覆盖，以保证那些未加载的CellsModel上的批注不会被覆盖)
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

				// 提取批注信息
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
		//lym 20150415 只保存没有调用的地方顾此处注销掉没用的功能，
//		// 为TURE则为指标审批的保存,否则是编制的保存
//		if (!isIndexApprove) {
//			saveTaskLayoutRecord(recordMap);
//		}
		// 保存批注
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
		// 获得修改过的单元格 --- DataCell的status标记
//		Map<MdTask, List<WrappedDataCell>> hmTaskToWdcs = getChangedWrappedDataCells();
		Map<String, Object> mapChanged = getChangedWrappedDataCells();
		Map<MdTask, List<WrappedDataCell>> hmTaskToWdcs = null;   //修改过的单元格
		boolean isTxtChanged = false;  //是否有非多维数据修改过
		if(mapChanged != null){
			hmTaskToWdcs = (Map<MdTask, List<WrappedDataCell>>)mapChanged.get(TbZiorUiCtl.KEY_DATACELL);
			isTxtChanged = "Y".equals(mapChanged.get(TbZiorUiCtl.KEY_CHANGED));
		}
		// 获得当前页签修改过的单元格(指标审批只能审批当前页签--- 任务实例唯一)
		List<WrappedDataCell> listWdcs = hmTaskToWdcs == null ? null : hmTaskToWdcs.get(task);
		boolean hasChangedDc = listWdcs != null && !listWdcs.isEmpty();
		//指标审批明细
		ArrayList<IndexAppdetailVO> alAppDetail = new ArrayList<IndexAppdetailVO>();
		//指标审批意见
		String planAppNote = null;
		// 是否修改完成
		boolean isApproveSuccess = true;
		if (!hasChangedDc) {
			if(!isTxtChanged)
				throw new BusinessException(NCLangRes.getInstance().getStrByID(
					"tbb_plan", "01812pln_000475")/* 目前没有任何数据被修改，请先在界面中修改需要审批的数据 */);
		}else{
			// 将模型保存单元格
			Map<CubeDef, Map<DimVector, WrappedDataCell>> hmCubeToDvDc = new LinkedHashMap<CubeDef, Map<DimVector, WrappedDataCell>>();
			for (int i = 0; i < listWdcs.size(); i++) {
				WrappedDataCell wdc = listWdcs.get(i);
				// 修改后单元格
				DataCell dc = wdc.getDataCell();
				if (dc == null)
					continue;
				// 应用模型
				CubeDef cube = dc.getCubeDef();
				// 维度向量
				DimVector dv = dc.getDimVector();
				// 单元格缓存
				Map<DimVector, WrappedDataCell> hmDvToDc = hmCubeToDvDc.get(cube);
				if (hmDvToDc == null) {
					hmDvToDc = new LinkedHashMap<DimVector, WrappedDataCell>();
					hmCubeToDvDc.put(cube, hmDvToDc);
				}
				hmDvToDc.put(dv, wdc);
			}
			// 数据查询接口
			IDataSetService service = CubeServiceGetter.getDataSetService();
			// 按模型遍历
			Iterator<CubeDef> itCube = hmCubeToDvDc.keySet().iterator();
			while (itCube.hasNext()) {
				// 应用模型
				CubeDef cube = itCube.next();
				// 原单元格缓存
				Map<DimVector, WrappedDataCell> hmNew = hmCubeToDvDc.get(cube);
				if (hmNew == null || hmNew.size() == 0)
					continue;
				// 单元格维度向量
				List<DimVector> alDvs = new ArrayList<DimVector>();
				alDvs.addAll(hmNew.keySet());
				// 通过单元格的维度向量去数据库中查询原有数据, 通过比较获得真正修改过数据的单元格
				ICubeDataSet ids = service.queryDataSet(cube, alDvs);
				// 遍历单元格,确认数据是否修改过
				for (int j = 0; j < alDvs.size(); j++) {
					DimVector dv = alDvs.get(j);
					// 新单元格
					WrappedDataCell wdc = hmNew.get(dv);
					// 数据库中的原单元格
					DataCell oldDc = ids.getDataCell(dv);
					//构造调整明细
					IndexAppdetailVO obj = IndexAppUiCtl.getIndexAppDetailVO(cube, task, wdc, oldDc);
					if(obj != null){
						alAppDetail.add(obj);
					}
				}
			}
			// 弹出审批明细界面
			if(getTbPlanContext().getTaskDef()==null){
				MdTaskDef def=task.getTaskDefWithoutDetail();
				getTbPlanContext().setTaskDef(def);
			}
			IndexApproveDlg dlg = new IndexApproveDlg(this.getMainboard(), task,
					alAppDetail,getTbPlanContext().getTaskDef());
			if (dlg.showModal() == UIDialog.ID_OK) {
				TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				// 审批明细
				alAppDetail = dlg.getPlanAppDetail();
				// 审批意见
				planAppNote = dlg.getApproveNote();
			} else {
				// showMessage(NCLangRes.getInstance().getStrByID("tbb_plan",
				// "01812pln_000476")/*已取消审批*/);
				TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				isApproveSuccess = false;
			}
		}
		
		/******************************************/
		//由于为了让保存数据和保存指标审批结果一致，最好将它俩的保存放在同一个事务中
		if(isApproveSuccess){
			// 删除空行空列
			deleteNullRowAndCol();
			if (currViewer != null) {
				currViewer.getCellsPane().editingStopped(
						new ChangeEvent(currViewer));
				// 询问重复维度向量是否继续保存+浮动新增行指标没填时是否继续保存
				List<String> repeatDimOrNullIndex=currViewer.getRepeatDimOrNullIndex();
				if (repeatDimOrNullIndex!=null&&repeatDimOrNullIndex.size()>0) {
					String mes="";
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVEREPEATDIM)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0266")/*@res "存在重复维度组合\n,"*/;
					}
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVENULLINDEX)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0267")/*@res "指标的成员不能为空\n,"*/;
					}
					   mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0264")/*@res "请确认是否继续保存?"*/;
					int result = MessageDialog.showYesNoDlg(
							getMainboard(),
							NCLangRes.getInstance().getStrByID("tbb_plan",
									"01812pln_000465")/* 询问 */,mes);//NCLangRes.getInstance().getStrByID("tbb_plan","01812pln_000728")/*" 存在 重复的指标"*/);
					if (MessageDialog.ID_NO == result || MessageDialog.ID_CANCEL == result) {
						TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
						return false;
					}
					TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				}
				// 从CellsModel写到TaskSheetDataModel
				setAllViewsDataToTsModel();
			}

			// 保存审批结果
			isApproveSuccess = saveApproveDetail(task, alAppDetail, planAppNote);
			//保存数据设计数据库操作:(1)执行公式  （2）模型数据保存
			Map<String,Object> map=ZiorFrameCtl.saveTaskDataMesMap(tbPlanContext.getNodeType(), taskDataModel, m_task);
			UFDateTime save=(UFDateTime) map.get(ITbPlanActionCode.SAVETIME);
			taskDataModel=(TaskDataModel) map.get(ITbPlanActionCode.TASKDATAMODEL);
            String message = (String) map.get(ITbPlanActionCode.ADJUSTCONTROLRULE);
			compileSaveLogs.setAdjustControlRuleMessage(message);
			TbbFormulaExecuteLogs logs=(TbbFormulaExecuteLogs) map.get(ITbPlanActionCode.TBBFORMULAEXECUTELOGS);
			if(logs.haveErr()){
				compileSaveLogs.setRuleMessage(logs.getErrInfo());
			}
			// 批注的保存
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
				// 保存后刷新和分发事件
				if (currViewer != null) {
					currViewer.getTable().repaint();
					// 去掉复制的虚线框
					TbDefaultSheetCellRender.stopPlay(currViewer.getCellsPane());
					// 分发插件事件
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
		// 指标审批之后改变状态
		getTbPlanContext().setIndexApprove(false);
		if (tbPlanContext != null) {
			tbPlanContext
					.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKVIEW);
		}
		return true;
	}

	/**
	 * 保存审批明細
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
	 * sheets 可见表单范围
	 * 版本对比任务的初始化
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
	 * sheets 可见表单范围
	 * 公式追踪任务的初始化
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
				//加载可操作表单的数据
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
                		// 多家单位审核时选中的是哪家单位
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
				MessageDialog.showErrorDlg(getMainboard(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0165")/*@res "错误"*/, e.getMessage());
			}
		}
	}

	/**
	 * 多家单位审核后审核面板的调用
	 *
	 * @author liyingm
	 */
	public void openCheckView(DockingState info,
			HashMap<String, List<Object>> mapOnly) {

		// 规则审核的信息处理
		List<CheckRuleResultVO> checkRuleResultVOList = new ArrayList<CheckRuleResultVO>();
		// eccelde 审核信息
		HashMap<TBSheetViewer, List<ExSheetCheckFml>> sheetCheckFmlMap = new HashMap<TBSheetViewer, List<ExSheetCheckFml>>();

		// 审核信息放到taskDataModel的每个TaskSheetDataModel中的sheetCheckFml属性里.
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
		// 打开审核结果的面板
		if ((checkRuleResultVOList != null && checkRuleResultVOList.size() > 0)
				|| (sheetCheckFmlMap != null && sheetCheckFmlMap.size() > 0)) {
			openView(info, getMainboard());
			if (getMainboard().getView(ITbPlanActionCode.Id_CHECKVIEW) != null) {
				getMainboard().getView(ITbPlanActionCode.Id_CHECKVIEW)
						.setTitle(
								NCLangRes.getInstance().getStrByID("tbb_plan",
										"01812pln_000123")/* 审核信息 */);
				// 规则审核的信息传到结果面板中
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
			viewDefault.setTitle(BusiTermConst.getMultiLangTextWithSysCode(busiCode, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0485")/*@res "默认表"*/));//NCLangRes.getInstance().getStrByID("tbb_plan",
					//"01812pln_000430")/* 预算默认表 */);
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
			//MessageDialog.showHintDlg(mainboard, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0283")/*@res "提示："*/, "");
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
			//封面的信息不参与其他的业务
			if(tsdModels[i].getName().equals(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0486")/*@res "分户封面"*/)){
				//nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0486")/*@res "分户封面"*/)
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
				tr.setTask(m_task);// 把任务传给当前的视图
				tr.setTsDataModel(tsdModels[i]);
				// 将当前的编辑状态传递给TBSheetViewer
				tr.setiMode(tr.getTbPlanContext() == null ? ITbPlanActionCode.mode_taskview
						: tr.getTbPlanContext().getComplieStatus());
			}
			tr.setMdSheet(tsdModels[i].getMdSheet());
			tr.setViewManager(ViewManager.this);
			tr.setPk_sheets(ViewManager.this.taskValidLookSheets);
			tr.setTitle(tsdModels[i].getName());
			tr.setTaskExtInfoLoader(taskExtInfoLoader);
			if (tr.getCellsModel() != null) {
				// 如果CellsModel不为空，则要重新刷新
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
					// 已经记录了最后一次的sheet，此处直接定位到这个sheet（通过sheetid定位）
					viewDisplay = (TBSheetViewer) v;
					dockableDisplay = dockable;
				}

			} else if (viewDisplay == null) {
				// 没有记录sheet，默认定位到第一个
				viewDisplay = (TBSheetViewer) v;
				dockableDisplay = dockable;
			}

			if (viewDisplay == null) {
				// 没有定位到sheet，默认定位到最后
				viewDisplay = (TBSheetViewer) v;
				dockableDisplay = dockable;
			}
		}
		if(sheetViewList.size()<=0){
			MessageDialog.showHintDlg(mainboard, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0283")/*@res "提示："*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0487")/*@res "表单范围加载异常，套表中sheet标示已被修改，请在任务管理节点重新设置表单范围！"*/);
			return;
		}
		// 显示lastSelectSheetViewer页签或者第一个页签
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
			// 必须先激活viewer，否则初始化的时候会出现找不到当前viewer的情况（或者找错的情况）
			dmng.display(dockableDisplay);
			this.getTbPlanContext().setCurrReportViewer(viewDisplay);
			// 只初始化某一个页签, 能够节约一定的时间
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
		// 删除默认表
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
		//初始化封面的信息
		if(coverTbSheetViewer!=null){
			setCoverMesToTask(coverTbSheetViewer.getTask()) ;
		}
		if(ITbPlanActionCode.mode_cellcontrast!=mode){
			setRowWidthAndColHeight();}//版本对比界面不需要
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
		// 当没有TBSheetViewer时打开默认表
		if (getMainboard().getView("tb.report.sheet.view") == null
				&& sheetViewList.size() > 0) {
			DockingState info = new DockingState("tb.report.sheet.view");
			info.setClzName(TBSheetViewer.class.getName());
			String busiCode=getTbPlanContext().getSysCode()==null?IBusiTermConst.SYS_TB:getTbPlanContext().getSysCode();
			info.setTitle(BusiTermConst.getMultiLangTextWithSysCode(busiCode, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0485")/*@res "默认表"*/));//NCLangRes.getInstance().getStrByID("tbb_plan",
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
				// 这里必须清除dockable的DockingState标记，否则如果前一次的sheet与后面加载的sheet重名
				// 就会造成界面混乱（这里需要提醒的是现在sheet的id取的是MdSheet的id，正确的做法应该是加上Task的pk）
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
		viewer.removeTitleAction("填充");
		viewer.removeTitleAction("删行");
		viewer.removeTitleAction("向后增行");
		viewer.removeTitleAction("向前增行");
		viewer.removeTitleAction("批量增行");
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
		Map<String,Object> paras=new HashMap<String,Object>();//放置一些需要查询其他数据时不在扩展接口参数的参数用
		String[] publicNodePks= (getTbPlanContext() == null || getTbPlanContext().getZiorOpenNodeModel() == null) ? null : getTbPlanContext().getZiorOpenNodeModel().getPublicnodeLookSheets();
		paras.put(ITbPlanActionCode.PUBLICNODEPKS, publicNodePks);
		paras.put(ITbPlanActionCode.SHEETGROUPNAME, getTbPlanContext().getSheetGroupName());
		String nodeType = getTbPlanContext().getNodeType();
		//对比版本的任务
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
	 * 获取taskde可见sheet范围 liyingm+
	 *  User2SheetCvsTools.getPkSheets(pk_user,pk_group,pk_责任主体,)，
	 * 返回用户分配职责的表单pk，多个职责设置表单有交叉的话pk有可能重复，
	 * 不过传给TaskDataModel的表单pk重复也没有关系。如没有可适配内容则返回null
	 * 可见表单为空，则打开任务可计算表单范围
	 * isApp=true为审批表单范围，在审批流节点配置
	 * @return
	 */
	private String[] getTaskValidLookPkSheets(boolean isApp) {
		//将代码迁移至TbZiorUiCtl-qy-20141114
		taskValidLookSheets = TbZiorUiCtl.getTaskValidLookPkSheets(getMdTask(), getTbPlanContext(), isApp);
		return taskValidLookSheets;
	}

	/**
	 * 获取task计算表单范围（可操作的表单范围）
	 * 审批表单在审批节点时展现要审批的表单
	 * @return
	 */
	private String[] getTaskValidPkSheets() {
		taskValidSheets=SheetGroupCtl.getPkSheetsByTaskSheetList(getMdTask().getSheetlist(), false);
		return taskValidSheets;
	}
	public void refreshByTaskDataModel(TaskDataModel taskDataModel) {
		setTaskDataModel(taskDataModel);
		// 设置一下最后一次的页签id
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
		// 设置一下最后一次的页签id
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
	 * 刷新之后显示第一个页签
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
	 * 刷新之后显示lastSelectedViewer页签
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
	 * 获取所有页签上修改过的DataCell(调整接口格式)
	 *
	 * @return
	 */
	public Map<String, Object> getChangedWrappedDataCells(){
		for (TBSheetViewer tbSheetViewer : sheetViewList) {
			tbSheetViewer.updateDataCellValue();
		}
		//当前界面是否修改过, 主要针对非多维数据
		boolean isChanged = false;
		//保存过的单元格
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

			dockable = dmng.createDockable(id);// 视图创建
			RestorationManager.getInstance().restore(dockable);// 添加到容器,激活视图
			if (isDisplay) {
				dmng.display(dockable);
			}
			dockable.init();// 视图初始化、派发打开事件、加载插件
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
	 * 审核面板的加载
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

			dockable = dmng.createDockable(id);// 视图创建
			dmng.display(dockable);// 添加到容器,激活视图
			dockable.init();// 视图初始化、派发打开事件、加载插件
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
	 * {方法功能中文描述}
	 *
	 * @param pks
	 * @param isNotALLIfNotLook,参数为false时代表，如果可见表单为null，则暂时taskdatamodel上的所有表单，true，则没有可见表单就暂时默认表
	 * @author:
	 */
	public void changeAllViewsBySheetGroupPks(String[] pks,boolean isNotALLIfNotLook) {

		//pks用户选择的表单分组里的表
		//得到任务管理节点设置的可见表单范围
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

        //重新整理展示的表单范围
		this.taskValidLookSheets=lookSheets;
		removeAllViewers0();
		initSheetViewers();

	}
	public static void removeUsableTitleAction(Viewer view) {
		view.removeTitleAction("com.ufida.zior.docking.view.actions.DefaultCloseAction");
		view.removeTitleAction(NCLangRes.getInstance().getStrByID("tbb_plan",
				"01812pln_000576")/* 关闭 */);/* notranslate */
		view.removeTitleAction("close");
		view.removeTitleAction("com.ufida.zior.docking.view.actions.DefaultPinAction");
		view.removeTitleAction(NCLangRes.getInstance().getStrByID("tbb_plan",
				"01812pln_000116")/* 最小化 */);
		view.removeTitleAction("pin");
	}

	public void setCheckMesssage(HashMap<String, List<Object>> mapCheck) {
		this.mapOnly=mapCheck;
	}
	/**
	 * 设计好的封面信息展现在任务上
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
		 //单位名称
		  CellPosition namePos=CellPosition.getInstance("C14");
		  coverTbSheetViewer.getCellsModel().getCell(namePos).setValue(ziorCoverVO.getOrgname());
       //单位负责人
		  CellPosition managerPos=CellPosition.getInstance("C16");
		 coverTbSheetViewer.getCellsModel().getCell(managerPos).setValue(ziorCoverVO.getOrgManager());
       //总会计师
		 CellPosition accountantPos=CellPosition.getInstance("C18");
		 coverTbSheetViewer.getCellsModel().getCell(accountantPos).setValue(ziorCoverVO.getChefAccountant());
       //负责机构
		 CellPosition agencyManPos=CellPosition.getInstance("C20");
		 coverTbSheetViewer.getCellsModel().getCell(agencyManPos).setValue(ziorCoverVO.getAgencyManager());
       //填表人
		 CellPosition operatorPos=CellPosition.getInstance("C22");
		 coverTbSheetViewer.getCellsModel().getCell(operatorPos).setValue(ziorCoverVO.getOperator());
       //电话号码
		 CellPosition districtNumPos=CellPosition.getInstance("C24");
		 CellPosition extNumPos=CellPosition.getInstance("G24");
		 CellPosition telPos=CellPosition.getInstance("D24");
		 coverTbSheetViewer.getCellsModel().getCell(districtNumPos).setValue(ziorCoverVO.getDistrictNum());
		 coverTbSheetViewer.getCellsModel().getCell(extNumPos).setValue(ziorCoverVO.getExtNum());
		 coverTbSheetViewer.getCellsModel().getCell(telPos).setValue(ziorCoverVO.getTel());
       //本企业代码
		 CellPosition selfCode=CellPosition.getInstance("O2");
		 coverTbSheetViewer.getCellsModel().getCell(selfCode).setValue(ziorCoverVO.getSelfCode());
       //上级企业代码
		 CellPosition parentCodePos=CellPosition.getInstance("O3");
		 coverTbSheetViewer.getCellsModel().getCell(parentCodePos).setValue(ziorCoverVO.getParentCode());
       //集团总代码
		 CellPosition groupCodePos=CellPosition.getInstance("O4");
		 coverTbSheetViewer.getCellsModel().getCell(groupCodePos).setValue(ziorCoverVO.getGroupCode());
       //隶属关系
		 CellPosition relationPos=CellPosition.getInstance("O7");
		 CellPosition combRelationPos=CellPosition.getInstance("O6");
		 coverTbSheetViewer.getCellsModel().getCell(relationPos).setValue(ziorCoverVO.getRelation());
		 coverTbSheetViewer.getCellsModel().getCell(combRelationPos).setValue(ziorCoverVO.getCombRelation());
//       //部门分类
//		 CellPosition divisionCodePos=CellPosition.getInstance("C18");
//		 CellPosition combDeptPos=CellPosition.getInstance("C18");
//		 coverTbSheetViewer.getCellsModel().getCell(divisionCodePos).setValue(ziorCoverVO.getDivisionCode());
//		 coverTbSheetViewer.getCellsModel().getCell(combDeptPos).setValue(ziorCoverVO.getCombDept());
       //所在地区
		 CellPosition locationPos=CellPosition.getInstance("O11");
		 CellPosition combLocationPos=CellPosition.getInstance("O10");
		 coverTbSheetViewer.getCellsModel().getCell(locationPos).setValue(ziorCoverVO.getLocation());
	     coverTbSheetViewer.getCellsModel().getCell(combLocationPos).setValue(ziorCoverVO.getCombLocation());
       //所属行业码
	     CellPosition busiCodePos=CellPosition.getInstance("O14");
	     CellPosition combBusiCodePos=CellPosition.getInstance("O13");
		 coverTbSheetViewer.getCellsModel().getCell(busiCodePos).setValue(ziorCoverVO.getBusiCode());
		 coverTbSheetViewer.getCellsModel().getCell(combBusiCodePos).setValue(ziorCoverVO.getCombBusiCode());
       //经营规模
		 CellPosition busiScalePos=CellPosition.getInstance("O16");
		 //CellPosition combScalePos=CellPosition.getInstance("C18");
		 coverTbSheetViewer.getCellsModel().getCell(busiScalePos).setValue(ziorCoverVO.getBusiScale());
		 //coverTbSheetViewer.getCellsModel().getCell(combScalePos).setValue(ziorCoverVO.getCombScale());
       //组合形式
		 CellPosition orgFormPos=CellPosition.getInstance("O21");
//		 CellPosition combDivisionPos=CellPosition.getInstance("C18");
		 CellPosition detailPos=CellPosition.getInstance("N21");
		 coverTbSheetViewer.getCellsModel().getCell(orgFormPos).setValue(ziorCoverVO.getOrgForm());
//		 coverTbSheetViewer.getCellsModel().getCell(combDivisionPos).setValue(ziorCoverVO.getCombDivision());
		 coverTbSheetViewer.getCellsModel().getCell(detailPos).setValue(ziorCoverVO.getDetail());
       //股票代码
		 CellPosition stockCodePos=CellPosition.getInstance("N19");
		 coverTbSheetViewer.getCellsModel().getCell(stockCodePos).setValue(ziorCoverVO.getStockCode());
       //创建年
		 CellPosition foundYearPos=CellPosition.getInstance("O25");
		 coverTbSheetViewer.getCellsModel().getCell(foundYearPos).setValue(ziorCoverVO.getFoundYear());
       //报表类型
		 CellPosition sheetTypePos=CellPosition.getInstance("O24");
		 //CellPosition combJsheetTYpePos=CellPosition.getInstance("C18");
		 coverTbSheetViewer.getCellsModel().getCell(sheetTypePos).setValue(ziorCoverVO.getSheetType());
		// coverTbSheetViewer.getCellsModel().getCell(combJsheetTYpePos).setValue(ziorCoverVO.getCombJsheetTYpe());
       //备用码
		 CellPosition spareCodePos=CellPosition.getInstance("O28");
		 coverTbSheetViewer.getCellsModel().getCell(spareCodePos).setValue(ziorCoverVO.getSpareCode());


	}
	/**
	 * pk_sheets 可见表单范围
	 * 刷新任务，不管这次选的任务和上一次的一样不
	 */
	public void setRefreshTask(MdTask task, String[] pk_sheets) {
		releaseDataCell();
		// 设置一下最后一次的页签id  放到removeAllViewers()前，否则打开默认表占布局时会被刷掉  lym  请不要再换地方
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
	
	/**央客：王志强  at:20200717
	 * 保存任务不执行excel
	 *
	 * @throws BusinessException
	 */
	public CompileSaveLogs saveTasksNoExcel() throws BusinessException {
		TbPlanContext tbPlanContext = getTbPlanContext();
		CompileSaveLogs compileSaveLogs=new CompileSaveLogs();
		String message = null;
		// 每次保存前先将标识置空
		if (tbPlanContext != null && getMdTask() != null) {
			TBSheetViewer currViewer = tbPlanContext.getCurrReportViewer();
			boolean isIndexApprove = tbPlanContext.isIndexApprove();
			// 为TURE则为指标审批的保存,否则是编制的保存
			if (isIndexApprove) {
				// 指标审批保存动作
				boolean sucessFlag = doIndexApproveSave(compileSaveLogs);
				return compileSaveLogs;
			}

			

			if (currViewer != null) {
				currViewer.getCellsPane().editingStopped(
						new ChangeEvent(currViewer));
				// 删除空行空列
				deleteNullRowAndCol();
				// 询问重复维度向量是否继续保存+浮动新增行指标没填时是否继续保存
				List<String> repeatDimOrNullIndex=currViewer.getRepeatDimOrNullIndex();
				if (repeatDimOrNullIndex!=null&&repeatDimOrNullIndex.size()>0) {
					String mes="";
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVEREPEATDIM)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0262")/*@res "存在重复维度组合,\n"*/;
					}
					if(repeatDimOrNullIndex.contains(ITbPlanActionCode.SAVENULLINDEX)){
						mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0265")/*@res "指标的成员不能为空,为空的数据无法保存!\n"*/;
					}
					   mes=mes+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0264")/*@res "请确认是否继续保存?"*/;
					int result = MessageDialog.showYesNoDlg(
							getMainboard(),
							NCLangRes.getInstance().getStrByID("tbb_plan",
									"01812pln_000465")/* 询问 */,mes);//NCLangRes.getInstance().getStrByID("tbb_plan","01812pln_000728")/*" 存在 重复的指标"*/);
					if (MessageDialog.ID_NO == result || MessageDialog.ID_CANCEL == result) {
						TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
						return compileSaveLogs;
					}
					TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
				}
				// 从CellsModel写到TaskSheetDataModel
				setAllViewsDataToTsModel();
			}
        //保存数据设计数据库操作:(1)执行公式  （2）模型数据保存
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
			// 批注的保存
			saveExOlapInfoSet();
			TaskDataCtl.updateTaskDataModelUICache(getTaskDataModel(), save);
			getTbPlanContext().setCurrentTs(save);
			if (tbPlanContext != null) {
				tbPlanContext.setComplieStatus(TbCompliePlanConst.COM_MODE_TASKVIEW);
			}
			updateViewerDataModel();
			setAllViewsTsModelToCellsModel();
			// 保存后刷新和分发事件
			if (currViewer != null) {
				currViewer.getTable().repaint();
				// 去掉复制的虚线框
				TbDefaultSheetCellRender.stopPlay(currViewer.getCellsPane());
				// 分发插件事件
				PluginActionEvent pluginActionEvent = new PluginActionEvent(
						currViewer, 1);
				this.getMainboard().getEventManager()
						.dispatch(pluginActionEvent);
//				if(getCurrentTbSheetViewer()!=null){
//					//如果有批注信息要关闭掉 --->找到其他方法解决,故不放开此地方，放开会增加不必要的效率问题   by liying
//					getCurrentTbSheetViewer().hideAllPostils();
//				}
			}
		}
		return compileSaveLogs;
	}

}
