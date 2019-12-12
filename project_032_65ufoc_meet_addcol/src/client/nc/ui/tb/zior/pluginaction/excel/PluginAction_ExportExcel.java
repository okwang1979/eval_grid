package nc.ui.tb.zior.pluginaction.excel;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.ms.mdm.convertor.IStringConvertor;
import nc.ms.mdm.convertor.StringConvertorFactory;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ms.tb.ext.plan.TbCompliePlanConst;
import nc.ms.tb.form.SheetGroupCtl;
import nc.ms.tb.formula.excel.core.IWorkSheet;
import nc.ms.tb.pub.plugin.TbPluginLoader;
import nc.ms.tb.pub.plugin.itf.ITbPluginSheetsFilter;
import nc.ms.tb.task.TaskExtInfoLoader;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ms.tb.task.data.TaskDataModelAction;
import nc.ms.tb.task.data.TaskSheetDataModel;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.tb.query.sum.TbQuerySumLeftDirView;
import nc.ui.tb.zior.TBSheetViewer;
import nc.ui.tb.zior.TbZiorUiCtl;
import nc.ui.tb.zior.ZiorCellsModelUtil;
import nc.ui.tb.zior.pluginaction.AbstractTbPluginAction;
import nc.ui.tb.zior.pluginaction.TbPluginActionDescriptor;
import nc.ui.tb.zior.pluginaction.exportbatchexcel.ExportExcelUtil;
import nc.vo.mdm.cube.DimSectionTuple;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.tb.form.MdSheet;
import nc.vo.tb.form.query.TbSumEnt;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;
import com.ufsoft.table.CellsModel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.ufsoft.report.sysplugin.excel.FontFactory;
/**
 * 导出excel
 * 
 * Vindicator:liqqc
 * 
 */

public class PluginAction_ExportExcel extends AbstractTbPluginAction {
	private PlanToSheetDlg planTosheetDlg = null;
	private static final String sep = "_";
	private String excelVersion = null;
	private IExcelExportUtil excelExportUtil = null;
	
	public PluginAction_ExportExcel(String name, String code) {
		super(name, code);
	}

	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		TbPluginActionDescriptor desc = new TbPluginActionDescriptor();
		desc.setName(TbActionName.getName_ExportExcel());
		desc.setGroupPaths(TbActionName.getName_Excel());
		return desc;
	}

	/*
	 * 执行事件
	 */
	@SuppressWarnings("restriction")
	@Override
	public void actionPerformed(ActionEvent actionevent) throws BusinessException {
		MdTask[] tasks = getContext().getTasks();
//		 getContext().getCurrReportViewer().getViewManager().get
		
		if (checkExport(tasks) == false)
			return;
		


		MdSheet[] sheets = getMdSheets(tasks);
		
		
		//by 王志强 at:2019/11/13 如果与分组过滤折进行控制
		String[] pk_sheets = TbZiorUiCtl.getTaskValidLookPkSheets(getMdTask(),  getContext(), false);
		if(pk_sheets==null||pk_sheets.length==0){
			return;
		}
		List<String> canLookSheets = Arrays.asList(pk_sheets);
		List<MdSheet> querySheets = new ArrayList<MdSheet>();
		for(MdSheet sheet:sheets){
			if(canLookSheets.contains(sheet.getPk_obj())){
				querySheets.add(sheet);
			}
			
		}		
		if (querySheets.size()==0)
			return;
		//************end******************************
		
		String funcode = getMainboard().getContext().getFuncNodeInfo().getFuncode();
		if (funcode == null) {
			funcode = "";
		}
		planTosheetDlg = new PlanToSheetDlg(getMainboard(), querySheets.toArray(new MdSheet[0]), tasks.length > 1, tasks[0].getObjname(), funcode);
		if (planTosheetDlg.showModal() == planTosheetDlg.ID_OK) {
			sheets = planTosheetDlg.getSelectSheets();
			excelVersion = planTosheetDlg.getSelectedExcelVersion();
			createFileByInf(planTosheetDlg, sheets, tasks, true);
		}
	}

	/**
	 * 
	 * {方法功能中文描述} 获取sheets
	 * 
	 * @param tasks
	 */
	private MdSheet[] getMdSheets(MdTask[] tasks) throws BusinessException {
		MdSheet[] sheets = null;
		if (tasks != null && tasks.length == 1) {
			List<MdSheet> mdsheets = new ArrayList<MdSheet>();
			// 获取任务指定的表单
			String[] Sheets = SheetGroupCtl.getPkSheetsByTaskSheetList(tasks[0].getSheetlist(), false);
			// 列表存储表单
			if (Sheets != null) {
				if (getCurrentViewer() != null) {
					if (getCurrentViewer().getViewManager().getTaskDataModel() != null) {
						List<String> pk_sheet = Arrays.asList(Sheets);
						TaskSheetDataModel[] tsdm = getCurrentViewer().getViewManager().getTaskDataModel().getTaskSheetDataModels();
						for (TaskSheetDataModel model : tsdm) {
							MdSheet sheet = model.getMdSheet();
							if (sheet != null && pk_sheet.contains(sheet.getPrimaryKey())) {
								mdsheets.add(sheet);
							}
						}
					}
				} else {
					SuperVO[] vos = NtbSuperServiceGetter.getINtbSuper().queryByPKArray(MdSheet.class, Sheets);
					for (SuperVO vo : vos) {
						if (vo != null) {
							if (vo instanceof MdSheet) {
								MdSheet sheet = (MdSheet) vo;
								mdsheets.add(sheet);
							}
						}
					}
				}
				sheets = mdsheets.toArray(new MdSheet[0]);
			} else {
				if (getCurrentViewer() != null) {
					sheets = IWorkSheetToMdSheet(getCurrentViewer().getViewManager().getTaskDataModel().getAllSheet());
				} else {
					for (MdTask task : tasks) {
						if (task != null) {
							sheets = getValidSheetsByPlan(task);
							if (sheets != null)
								break;
						}
					}
				}
			}
		}

		else {
			if (getCurrentViewer() != null) {
				sheets = IWorkSheetToMdSheet(getCurrentViewer().getViewManager().getTaskDataModel().getAllSheet());
			} else {
				Set<String> setSheetNames = new HashSet<>();
				Set<MdSheet> setSheets = new HashSet<>();
				//分配了Sheet页，需要循环每个任务获取有权限的Sheet页
				for (MdTask task : tasks) {
					if (task != null) {
						MdSheet[] sheetsForOneTask = getValidSheetsByPlan(task);
//						if (sheets != null)
//							break;
						if (sheetsForOneTask != null) {
							for (MdSheet sheet : sheetsForOneTask) {
								String pkObj = sheet.getPk_obj();
								if (!setSheetNames.contains(pkObj)) {
									setSheetNames.add(pkObj);
									setSheets.add(sheet);
								}
							}
						}
					}
				}
				
				sheets = setSheets.toArray(new MdSheet[setSheets.size()]);
			}
		}
		return sheets;
	}

	/**
	 * 
	 * {方法功能中文描述}检查是否能导出
	 * 
	 * @param tasks
	 * @return
	 */
	private boolean checkExport(MdTask[] tasks) {
		if (tasks == null || tasks.length == 0) {
			MessageDialog.showHintDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000718")/* 选择主体 */, NCLangRes
					.getInstance().getStrByID("tbb_plan", "01812pln_000719")/* 请您在左侧任务栏选择要导出的任务,再进行导出Excel操作 */);
			return false;
		}
		if (tasks.length > 1) {
			for (int i = 0; i < tasks.length; i++) {
				for (int j = i; j < tasks.length; j++) {
					if (!tasks[i].getPk_taskdef().equals(tasks[j].getPk_taskdef())) {
						// 提示任务模板不同
						MessageDialog.showHintDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000474")/** 提示 **/
						, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000730"));// 请选择相同的任务模板)
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * 
	 * {方法功能中文描述} 生成导出文件
	 * 
	 * @param planTosheetDlg2
	 * @param sheets
	 * @param tasks
	 * @param charge
	 * @throws BusinessException
	 */
	private void createFileByInf(PlanToSheetDlg planTosheetDlg2, MdSheet[] sheets, MdTask[] tasks, boolean charge) throws BusinessException {
		Map<String, MdSheet> m_filterSheets = new HashMap<String, MdSheet>();
		String[] pk_sheets = new String[sheets.length];
		if (sheets == null || sheets.length == 0)
			return;
		for (int i = 0; i < sheets.length; i++) {
			pk_sheets[i] = sheets[i].getPk_obj();
			m_filterSheets.put(pk_sheets[i], sheets[i]);
		}
		// 默认exportName的类型为false,但是这会影响导出excel时的命名规范，所以当只有一个任务时它的值为false,作为导出excel命名选择的判断
		boolean exportType = tasks.length > 1 ? true : false;
		exportType = planTosheetDlg.getExportType(); // 默认true是合并 false不合并 —————— 判断是否合并excel
		if (tasks.length == 1) {
			exportType = false;
		}

		if (exportType && !isSameTaskDef(tasks)) {
			MessageDialog.showErrorDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/* 提示 */, NCLangRes
					.getInstance().getStrByID("tbb_plan", "01812pln_000730")/* 请选择相同模板的任务 */);
			return;
		}
		String chosepath = planTosheetDlg.getChosePath(); // 2014年test1_预算数_人民币_默认版本(无锡宏远工厂)
		String name = planTosheetDlg.getNameBySelf().getText();
		
		if (excelVersion != null && excelVersion.equals(IExcelExportUtil.Excel2003)) {
			excelExportUtil = new ExcelExportUtil();
		} else {
			excelExportUtil = new ExcelExportUtilXSSF();
		}
		Workbook workBook = excelExportUtil.createWorkbook();
		Hashtable<String, CellStyle> hashStyleByKey = new Hashtable<String, CellStyle>();
		boolean exportName = false; // 任务大于一个
		MdTaskDef taskDef = null;
		String taskName = null;

		//如果是单任务，则取出
		Map<String, CellsModel> models = null;
		//取出当前所有的模型
		if(getTbPlanListViewer() == null){
			List<TBSheetViewer> sheetViewList = getContext().getCurrReportViewer().getViewManager().getSheetViewList();
			models = new HashMap<String, CellsModel>();
			for (TBSheetViewer viewer : sheetViewList) {
				if(viewer.getCellsModel()!=null){
					models.put(viewer.getId(), viewer.getCellsModel());
				}else{
					viewer.initTable();
					models.put(viewer.getId(), viewer.getCellsModel());
				}
			}
		}
		
		
		for (int i = 0; i < tasks.length; i++) {
			MdTask task = tasks[i];
			// 用来存储过滤之后的任务表单
			MdSheet[] filterSheets = sheets;
			String[] pk_filterSheets = pk_sheets;
			// 得到任务所属的表单的Pk
			String[] pk_defaultSheets = SheetGroupCtl.getPkSheetsByTaskSheetList(task.getSheetlist(), false);
			// 用来存储过滤之后的任务表单的pk
			if (pk_defaultSheets != null) {
				pk_filterSheets = ExportExcelUtil.intersect(pk_defaultSheets, pk_sheets);
				if (pk_filterSheets != null && pk_filterSheets.length != 0) {
					filterSheets = new MdSheet[pk_filterSheets.length];
					for (int k = 0; k < pk_filterSheets.length; k++) {
						if (m_filterSheets.containsKey(pk_filterSheets[k])) {
							filterSheets[k] = m_filterSheets.get(pk_filterSheets[k]);
						}
					}
				} else {
					continue;
				}
			}
			TaskExtInfoLoader taskExtInfoLoader = new TaskExtInfoLoader();
			taskExtInfoLoader.setMdTask(task, pk_filterSheets);
			if (taskDef == null)
				taskDef = task.getTaskDefWithoutDetail();
			if (!exportType) {
				workBook = excelExportUtil.createWorkbook();
				hashStyleByKey = new Hashtable<String, CellStyle>();
			}
			List<TaskSheetDataModel> tsDataModels = getTsDataModels(task, filterSheets);
			if (tsDataModels == null || tsDataModels.isEmpty())
				continue;

			StringBuffer b = new StringBuffer();
			// 合并时候的名称变化
			if (exportType == true) {
				String entityName = task.getObjname();
				int first = entityName.lastIndexOf("_");
				int start = entityName.indexOf("(", first);
				if (start < 0) {
					start = entityName.indexOf("(");
					first = entityName.lastIndexOf("_", start);
				}
				int last = entityName.lastIndexOf(")");
				b.append(entityName.substring(first + 1, start));
				b.append(sep);
				b.append(entityName.substring(start + 1, last));
			}

			FontFactory fontFactory = excelExportUtil.createFontFactory(workBook);
			String[] sheetNames = getSheetNames(sheets, tasks, exportType, i);
			for (int j = 0; j < tsDataModels.size(); j++) {
				String shtName = getSingleSheetName(exportType, workBook, task, tsDataModels, sheetNames, j);
				shtName = ExportExcelUtil.replaceIllegalAndSubSheetName(shtName);
				Sheet sheet = workBook.createSheet(shtName);
				CellsModel cellsModelByTsModel = null;
				if(models!=null&&models.size()!=0){
					cellsModelByTsModel = models.get(tsDataModels.get(j).getName());
					if(cellsModelByTsModel==null){
						cellsModelByTsModel = getCellsModelByTsModel(tsDataModels.get(j), taskExtInfoLoader);
					}
				}else{
					cellsModelByTsModel = getCellsModelByTsModel(tsDataModels.get(j), taskExtInfoLoader);
				}
				
				if (planTosheetDlg.isRemove) {
					CellsModel model = excelExportUtil.creatCellModelRemoveHide(cellsModelByTsModel);
					excelExportUtil.covertModel2Sheet(model, sheet, workBook, null, fontFactory, hashStyleByKey);
				} else {
					excelExportUtil.covertModel2Sheet(cellsModelByTsModel, sheet, workBook, null, fontFactory, hashStyleByKey);
				}
			}
			if (exportType && i != tasks.length - 1)
				continue;
			if (name == null || "".equals(name.toString().trim())) {
				MessageDialog.showErrorDlg(getMainboard(),
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0", "01050plan002-0222")/* @res "提示 " */,
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0", "01050plan002-0223")/* @res "请输入正确的文件名称" */);
				return;
				// taskName = (exportType && exportName) ? createTaskName(task, taskDef) : task.getObjname();
			} else {
//				taskName = name;
				
				//********* begin 2015-08-07 这段逻辑导致，预算查阅节点导出excel，不能改文件的名字
//				if (!(ITbPlanActionCode.COM_NODETYPE.equals(getContext().getNodeType())
//						|| ITbPlanActionCode.DAILY_NODETYPE.equals(getContext().getNodeType()) || ITbPlanActionCode.TABLEOFTOP_NODETYPE
//							.equals(getContext().getNodeType()))) {
//					taskName = task.getObjname();
//				}
				//********* end 2015-08-07 这段逻辑导致，预算查阅节点导出excel，不能改文件的名字
				
				taskName = ExportExcelUtil.createTaskName(task, taskDef);
//				if (exportType) {
//					taskName = b.toString();
//				}	
				if (!exportType) {
					taskName = task.getObjname();
				}

			}

			if (charge == true) {
				try {
					excelExportUtil.saveWorkBook2Local(workBook, chosepath, taskName, exportType, i == tasks.length - 1);
				} catch (Exception e) {
					System.gc();
					throw new BusinessException(e);
				}
				System.gc();
			}
		}
	}

	/**
	 * 
	 * {方法功能中文描述} 获取页签名称
	 * 
	 * @param exportType
	 * @param workBook
	 * @param task
	 * @param tsDataModels
	 * @param sheetNames
	 * @param j
	 * @return
	 */
	private String getSingleSheetName(boolean exportType, Workbook workBook, MdTask task, List<TaskSheetDataModel> tsDataModels,
			String[] sheetNames, int j) {
		String shtName = "";
		if (exportType) {
			if ((getContext().getPkFuncCode().equals("18122201")) && getTbQuerySumLeftDirView().getTmodel() != null) {
				shtName = task.getPk_year() == null ? "" : task.getPk_year() + sep
						+ ExportExcelUtil.getSerialNum(tsDataModels.get(j).getShowName(), 0)
						+ ((TbSumEnt) getTbQuerySumLeftDirView().GetSelectNode()).getObjname().trim()
						+ ExportExcelUtil.getSerialNum(tsDataModels.get(j).getShowName(), 1);

			} else {
				if (shtName.isEmpty()) {
					if (task.getPk_year() != null || task.getPk_month() != null) {
						if (task.getPk_year() != null) {
							shtName = task.getPk_year() + sep;
						}

						if (task.getPk_month() != null) {
							shtName = shtName + task.getPk_month() + sep;
						}

						shtName = shtName + ExportExcelUtil.getTaskEntryName(task, task.getPk_dataent()).trim() + sep
								+ ExportExcelUtil.getSerialNum(tsDataModels.get(j).getShowName(), 1);
					} else {
						// 找不到时间
						shtName = sheetNames[j];
					}
				}
			}
		} else {
			shtName = tsDataModels.get(j).getShowName().trim();
		}

		// 对于可能的重复问题进行编码
		shtName = excelExportUtil.resetSheetName(workBook, shtName);

		if (shtName.isEmpty()) {
			shtName = tsDataModels.get(j).getShowName().trim();
		}
		return shtName;
	}

	private String[] getSheetNames(MdSheet[] sheets, MdTask[] tasks, boolean exportType, int i) {
		String[] sheetNames = new String[sheets.length];
		if (exportType == true) {
			String entityName = tasks[i].getObjname();
			StringBuffer b = new StringBuffer();
			int first = entityName.lastIndexOf("_");
			int start = entityName.indexOf("(", first);
			if (start < 0) {
				start = entityName.indexOf("(");
				first = entityName.lastIndexOf("_", start);
			}
			int last = entityName.lastIndexOf(")");
			b.append(entityName.substring(first + 1, start));
			b.append(sep);
			b.append(entityName.substring(start + 1, last));
			for (int j = 0; j < sheets.length; j++) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(b.toString());
				buffer.append(sep);
				buffer.append(sheets[j].getObjname());
				sheetNames[j] = buffer.toString();
			}
		}
		return sheetNames;
	}

	private List<TaskSheetDataModel> getTsDataModels(MdTask task, MdSheet[] sheets) throws BusinessException {
		TaskDataModel taskDataModel = null;
		if (getContext() != null && getCurrentViewer() != null) {
			if (getContext().getTasks() != null && getContext().getTasks().length > 0) {
				MdTask currnTask = getContext().getTasks()[0];
				if (task.getPk_obj().equals(currnTask.getPk_obj())) {
					getCurrentViewer().getViewManager().setAllViewsDataToTsModel();
					taskDataModel = getCurrentViewer().getViewManager().getTaskDataModel();
					executeFmlAndRule(taskDataModel);
				}
			}
		}
		if (taskDataModel == null) {
			if (!(getContext().getPkFuncCode().equals("18122201"))
					|| ((getContext().getPkFuncCode().equals("18122201")) && getTbQuerySumLeftDirView().getTmodel() == null)) {
				taskDataModel = TaskDataCtl.getTaskDataModel(task.getPrimaryKey(), null, false, null, true);
			} else {
				taskDataModel = getTbQuerySumLeftDirView().getTmodel();
			}
		}
		List<TaskSheetDataModel> tsdatamodels = new ArrayList<TaskSheetDataModel>();
		Map<String, String> hm_sheetPk = new HashMap<String, String>();
		if (sheets != null && sheets.length > 0) {
			for (MdSheet s : sheets) {
				hm_sheetPk.put(s.getPrimaryKey(), s.getPrimaryKey());
			}
		}
		TaskSheetDataModel[] tsdModels = taskDataModel == null ? new TaskSheetDataModel[0] : taskDataModel.getTaskSheetDataModels();
		for (int i = 0; i < tsdModels.length; i++) {
			MdSheet mdsheet = tsdModels[i].getMdSheet();
			if (mdsheet == null || (!hm_sheetPk.isEmpty() && !hm_sheetPk.containsKey(mdsheet.getPrimaryKey()))) {
				continue;
			}
			tsdatamodels.add(tsdModels[i]);
		}
		return tsdatamodels;
	}

	// 执行excel公式
	private void executeFmlAndRule(TaskDataModel taskDataModel) throws BusinessException {
		// 执行属性函数
		TaskDataModelAction action = new TaskDataModelAction(taskDataModel);
		action.executeTableHeadFormula(null);
		// List<BusiRuleVO> defaultRules = null;
		// defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(getMdTask().getPk_taskdef(), NTBActionEnum.CALACTION.toCodeString());

		// List<BusiRuleVO> defaultRules = null;
		// if(getContext().getNodeType().equals(ITbPlanActionCode.DAILY_NODETYPE)){
		// defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(
		// getMdTask().getPk_taskdef(),
		// NTBActionEnum.GETDATAACTION.toCodeString());
		// }else{
		// defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(
		// getMdTask().getPk_taskdef(),
		// NTBActionEnum.CALACTION.toCodeString());
		// }
		//
		//
		// if (defaultRules != null && defaultRules.size() > 0) {
		// RuleExecuteHelper.executeWorkBookRule(taskDataModel, defaultRules);
		// }
		// TaskDataModelAction action = new TaskDataModelAction(taskDataModel);
		// action.executeWorkBook();
	}

	public String createTaskName(MdTask task, MdTaskDef taskDef, Boolean exportType, Boolean exportName) {
		String str = (exportType && exportName) ? createTaskName(task, taskDef) : task.getObjname();
		return str;
	}

	public String createTaskName(MdTask task, MdTaskDef m_td) {
		StringBuffer sb = new StringBuffer();
		sb.append(m_td.getObjname());
		appendValue(sb, task, task.getPk_mvtype(), true);
		appendValue(sb, task, task.getPk_currency(), true);
		appendValue(sb, task, task.getPk_version(), true);
		if (task.getPk_paradims() != null && task.getPk_paradims().length() > 0) {
			IStringConvertor sc = StringConvertorFactory.getConvertor(DimSectionTuple.class);
			DimSectionTuple paradim = (DimSectionTuple) sc.fromString(task.getPk_paradims());
			List<LevelValue> lvs = paradim.getLevelValuesSortedList();
			if (lvs != null) {
				for (LevelValue lv : lvs) {
					// 去掉主体和时间
					if (!lv.getDocName().equals("BD_ACCPERIOD"))
						sb.append("_").append(lv.getName());
				}
			}
		}
		return sb.toString();
	}

	private String appendValue(StringBuffer sb, MdTask task, String key, boolean addSeperator) {
		Object obj = task.getExtrAttribute(key);
		if (obj != null) {
			if (obj instanceof LevelValue) {
				if (addSeperator)
					sb.append("_");
				sb.append(((LevelValue) obj).getName());
			} else if (obj instanceof DimMember) {
				if (addSeperator)
					sb.append("_");
				sb.append(((DimMember) obj).getObjName());
			}
		}
		return sb.toString();
	}

	public MdSheet[] getValidSheetsByPlan(MdTask task) {
		SuperVO[] sheets = null;
		List<MdSheet> sheetList = new ArrayList<MdSheet>();
		try {
			String[] pk_sheets = null;
			List<ITbPluginSheetsFilter> filter = TbPluginLoader.getInstance().getPluginSheetsFilter();
			if (filter != null && !filter.isEmpty()) {
				// 根据插件注册功能获取可用sheet，只要有一个插件获取到结果就生效
				HashMap<String, Object> paraMap = new HashMap<String, Object>();
				paraMap.put(ITbPluginSheetsFilter.paraKey_nodeFunclet, getMainboard());
				paraMap.put(ITbPluginSheetsFilter.paraKey_task, task);
				for (ITbPluginSheetsFilter f : filter) {
					pk_sheets = f.getValidSheets(paraMap);
					if (pk_sheets != null) {
						sheets = NtbSuperServiceGetter.getINtbSuper().queryByPKArray(MdSheet.class, pk_sheets);
						if (sheets != null) {
							for (SuperVO vo : sheets) {
								if (vo == null)
									continue;
								if (vo instanceof MdSheet) {
									MdSheet sheet = (MdSheet) vo;
									sheetList.add(sheet);
								}
							}
						}
						return sheetList.toArray(new MdSheet[0]);
					}
				}
			}
			pk_sheets = SheetGroupCtl.getPkSheetsByTaskSheetList(task.getSheetlist(), false);
			if (pk_sheets == null)
				sheets = NtbSuperServiceGetter.getINtbSuper().queryByCondition(MdSheet.class, "PK_WORKBOOK = '" + task.getPk_workbook() + "'");
			else
				sheets = NtbSuperServiceGetter.getINtbSuper().queryByPKArray(MdSheet.class, pk_sheets);
		} catch (BusinessException e) {
			NtbLogger.error(e);
		}
		for (SuperVO vo : sheets) {
			if (vo == null)
				continue;
			if (vo instanceof MdSheet) {
				MdSheet sheet = (MdSheet) vo;
				sheetList.add(sheet);
			}
		}
		return sheetList.toArray(new MdSheet[0]);
	}

	private static MdSheet[] IWorkSheetToMdSheet(Collection<IWorkSheet> allsheet) {
		List<MdSheet> mdSheetList = new ArrayList<MdSheet>();
		if (allsheet != null) {
			for (IWorkSheet iWorkSheet : allsheet) {
				if (iWorkSheet instanceof TaskSheetDataModel) {
					TaskSheetDataModel taskSheetDataModel = (TaskSheetDataModel) iWorkSheet;
					mdSheetList.add(taskSheetDataModel.getMdSheet());
				}
			}
		}
		return mdSheetList.toArray(new MdSheet[0]);
	}

	private TbQuerySumLeftDirView getTbQuerySumLeftDirView() {
		return (TbQuerySumLeftDirView) this.getMainboard().getView("tb.report.dir.view");
	}

	private boolean isSameTaskDef(MdTask[] tasks) {
		if (tasks != null && tasks.length > 0) {
			String taskDef = tasks[0].getPk_taskdef();
			for (int i = 0; i < tasks.length; i++) {
				if (!taskDef.equals(tasks[i].getPk_taskdef())) {
					return false;
				}
			}
		}
		return true;
	}

	//如果当前没有界面，则从库里面查
	private CellsModel getCellsModelByTsModel(TaskSheetDataModel taskSheetDataModel, TaskExtInfoLoader taskExtInfo) {
		CellsModel initCellsModel = null;
		if (getTbPlanListViewer() != null) { // 多任务
			initCellsModel = taskSheetDataModel.getMdSheet().getCsmodel();
			ZiorCellsModelUtil.loadCellsModel(taskSheetDataModel, initCellsModel, taskExtInfo, getContext());
		} else {
			initCellsModel = taskSheetDataModel.getMdSheet().getCsmodel();
//			String showName = taskSheetDataModel.getShowName();
//			for (TBSheetViewer viewer : sheetViewList) {
//				if(showName.equals(viewer.getId())){
//					initCellsModel = viewer.getCellsModel();
//					break;
//				}
//			}
//			if (initCellsModel != null) {
//				ZiorCellsModelUtil.loadCellsModel(taskSheetDataModel, initCellsModel, taskExtInfo, getContext());
//			}
		}

		return initCellsModel;
	}

	@Override
	public boolean isActionEnabled() {
		int editing = getContext().getComplieStatus();
		if (TbCompliePlanConst.COM_MODE_TASKEDIT == editing) {
			return false;
		} else {
			return true;
		}
	}
}