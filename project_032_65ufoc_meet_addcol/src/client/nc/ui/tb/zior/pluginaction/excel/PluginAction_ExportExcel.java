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
 * ����excel
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
	 * ִ���¼�
	 */
	@SuppressWarnings("restriction")
	@Override
	public void actionPerformed(ActionEvent actionevent) throws BusinessException {
		MdTask[] tasks = getContext().getTasks();
//		 getContext().getCurrReportViewer().getViewManager().get
		
		if (checkExport(tasks) == false)
			return;
		


		MdSheet[] sheets = getMdSheets(tasks);
		
		
		//by ��־ǿ at:2019/11/13 ������������۽��п���
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
	 * {����������������} ��ȡsheets
	 * 
	 * @param tasks
	 */
	private MdSheet[] getMdSheets(MdTask[] tasks) throws BusinessException {
		MdSheet[] sheets = null;
		if (tasks != null && tasks.length == 1) {
			List<MdSheet> mdsheets = new ArrayList<MdSheet>();
			// ��ȡ����ָ���ı�
			String[] Sheets = SheetGroupCtl.getPkSheetsByTaskSheetList(tasks[0].getSheetlist(), false);
			// �б�洢��
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
				//������Sheetҳ����Ҫѭ��ÿ�������ȡ��Ȩ�޵�Sheetҳ
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
	 * {����������������}����Ƿ��ܵ���
	 * 
	 * @param tasks
	 * @return
	 */
	private boolean checkExport(MdTask[] tasks) {
		if (tasks == null || tasks.length == 0) {
			MessageDialog.showHintDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000718")/* ѡ������ */, NCLangRes
					.getInstance().getStrByID("tbb_plan", "01812pln_000719")/* ���������������ѡ��Ҫ����������,�ٽ��е���Excel���� */);
			return false;
		}
		if (tasks.length > 1) {
			for (int i = 0; i < tasks.length; i++) {
				for (int j = i; j < tasks.length; j++) {
					if (!tasks[i].getPk_taskdef().equals(tasks[j].getPk_taskdef())) {
						// ��ʾ����ģ�岻ͬ
						MessageDialog.showHintDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000474")/** ��ʾ **/
						, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000730"));// ��ѡ����ͬ������ģ��)
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * 
	 * {����������������} ���ɵ����ļ�
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
		// Ĭ��exportName������Ϊfalse,�������Ӱ�쵼��excelʱ�������淶�����Ե�ֻ��һ������ʱ����ֵΪfalse,��Ϊ����excel����ѡ����ж�
		boolean exportType = tasks.length > 1 ? true : false;
		exportType = planTosheetDlg.getExportType(); // Ĭ��true�Ǻϲ� false���ϲ� ������������ �ж��Ƿ�ϲ�excel
		if (tasks.length == 1) {
			exportType = false;
		}

		if (exportType && !isSameTaskDef(tasks)) {
			MessageDialog.showErrorDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/* ��ʾ */, NCLangRes
					.getInstance().getStrByID("tbb_plan", "01812pln_000730")/* ��ѡ����ͬģ������� */);
			return;
		}
		String chosepath = planTosheetDlg.getChosePath(); // 2014��test1_Ԥ����_�����_Ĭ�ϰ汾(������Զ����)
		String name = planTosheetDlg.getNameBySelf().getText();
		
		if (excelVersion != null && excelVersion.equals(IExcelExportUtil.Excel2003)) {
			excelExportUtil = new ExcelExportUtil();
		} else {
			excelExportUtil = new ExcelExportUtilXSSF();
		}
		Workbook workBook = excelExportUtil.createWorkbook();
		Hashtable<String, CellStyle> hashStyleByKey = new Hashtable<String, CellStyle>();
		boolean exportName = false; // �������һ��
		MdTaskDef taskDef = null;
		String taskName = null;

		//����ǵ�������ȡ��
		Map<String, CellsModel> models = null;
		//ȡ����ǰ���е�ģ��
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
			// �����洢����֮��������
			MdSheet[] filterSheets = sheets;
			String[] pk_filterSheets = pk_sheets;
			// �õ����������ı���Pk
			String[] pk_defaultSheets = SheetGroupCtl.getPkSheetsByTaskSheetList(task.getSheetlist(), false);
			// �����洢����֮����������pk
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
			// �ϲ�ʱ������Ʊ仯
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
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0", "01050plan002-0222")/* @res "��ʾ " */,
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0", "01050plan002-0223")/* @res "��������ȷ���ļ�����" */);
				return;
				// taskName = (exportType && exportName) ? createTaskName(task, taskDef) : task.getObjname();
			} else {
//				taskName = name;
				
				//********* begin 2015-08-07 ����߼����£�Ԥ����Ľڵ㵼��excel�����ܸ��ļ�������
//				if (!(ITbPlanActionCode.COM_NODETYPE.equals(getContext().getNodeType())
//						|| ITbPlanActionCode.DAILY_NODETYPE.equals(getContext().getNodeType()) || ITbPlanActionCode.TABLEOFTOP_NODETYPE
//							.equals(getContext().getNodeType()))) {
//					taskName = task.getObjname();
//				}
				//********* end 2015-08-07 ����߼����£�Ԥ����Ľڵ㵼��excel�����ܸ��ļ�������
				
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
	 * {����������������} ��ȡҳǩ����
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
						// �Ҳ���ʱ��
						shtName = sheetNames[j];
					}
				}
			}
		} else {
			shtName = tsDataModels.get(j).getShowName().trim();
		}

		// ���ڿ��ܵ��ظ�������б���
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

	// ִ��excel��ʽ
	private void executeFmlAndRule(TaskDataModel taskDataModel) throws BusinessException {
		// ִ�����Ժ���
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
					// ȥ�������ʱ��
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
				// ���ݲ��ע�Ṧ�ܻ�ȡ����sheet��ֻҪ��һ�������ȡ���������Ч
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

	//�����ǰû�н��棬��ӿ������
	private CellsModel getCellsModelByTsModel(TaskSheetDataModel taskSheetDataModel, TaskExtInfoLoader taskExtInfo) {
		CellsModel initCellsModel = null;
		if (getTbPlanListViewer() != null) { // ������
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