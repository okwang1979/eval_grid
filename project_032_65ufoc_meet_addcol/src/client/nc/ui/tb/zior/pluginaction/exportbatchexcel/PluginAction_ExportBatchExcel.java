package nc.ui.tb.zior.pluginaction.exportbatchexcel;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import nc.ms.tb.form.SheetGroupCtl;
import nc.ms.tb.formula.excel.core.IWorkSheet;
import nc.ms.tb.task.TaskExtInfoLoader;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ms.tb.task.data.TaskDataModelAction;
import nc.ms.tb.task.data.TaskFormulaFix;
import nc.ms.tb.task.data.TaskSheetDataModel;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIDialog;
import nc.ui.tb.zior.TbPlanFrameUtil;
import nc.ui.tb.zior.TbZiorUiCtl;
import nc.ui.tb.zior.ZiorCellsModelUtil;
import nc.ui.tb.zior.pluginaction.AbstractTbPluginAction;
import nc.ui.tb.zior.pluginaction.TbPluginActionDescriptor;
import nc.ui.tb.zior.pluginaction.excel.ExcelExportUtil;
import nc.ui.tb.zior.pluginaction.excel.ExcelExportUtilXSSF;
import nc.ui.tb.zior.pluginaction.excel.IExcelExportUtil;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pmpub.common.utils.ArrayUtils;
import nc.vo.pub.BusinessException;
import nc.vo.tb.form.MdSheet;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;

import org.apache.commons.collections.MapUtils;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.report.sysplugin.xml.ExtNameFileFilter;
import com.ufsoft.table.CellsModel;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.ufsoft.report.sysplugin.excel.FontFactory;

/**
 * ��������Excel
 * 
 * Vindicator:liqqc
 * 
 */

@SuppressWarnings("restriction")
public class PluginAction_ExportBatchExcel extends AbstractTbPluginAction {
	private static final String sep = "_";
	private IExcelExportUtil excelExportUtil = null;
	private String excelVersion = null;
	
	public PluginAction_ExportBatchExcel(String name, String code) {
		super(name, code);
	}

	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		TbPluginActionDescriptor desc = new TbPluginActionDescriptor();
		desc.setName(TbActionName.getName_ExportBatchExcels());
		desc.setGroupPaths(TbActionName.getName_Excel());
		return desc;
	}

	@Override
	public void actionPerformed(ActionEvent actionevent) throws BusinessException {
		Map<DimMember, MdTask> allValidDimMembers = null;
		// ȡ�õ�ǰ���ڵ��µ�����ά�ȳ�Ա
		allValidDimMembers = getContext().getZiorOpenNodeModel().getValidOrgDimMemberMap();
		if (getCurrentViewer() != null) {
			if (getCurrentViewer().getViewManager().getTaskDataModel() != null) {
				// �õ���ʾ�ı�
				Collection<IWorkSheet> sheets = getCurrentViewer().getViewManager().getTaskDataModel().getAllSheet();
				
				
				
				//by ��־ǿ at:2019/11/13 ������������۽��п���
				String[] pk_sheets = TbZiorUiCtl.getTaskValidLookPkSheets(getMdTask(),  getContext(), false);
				if(pk_sheets==null||pk_sheets.length==0){
					return;
				}
				Set<IWorkSheet> removeSheet = new HashSet<>();
				
				
					for(IWorkSheet sheet:sheets){
						boolean isRemove = true;
						for(String pk_sheet:pk_sheets){
							if(((TaskSheetDataModel)sheet).getMdSheet().getPk_obj().equals(pk_sheet)){
								isRemove = false;
								break;
							}
						}
						if(isRemove){
							removeSheet.add(sheet);
						}
					}
					sheets.removeAll(removeSheet);
			
			
 
			 
				//************end******************************
				
				
				
				
				Object obj = getTbReportDirView().getLeftContentPanel().getSelectBusiObj();
				// excel������������(������������Ҳ��),����������� �� �����Ա
				String funcode = getMainboard().getContext().getFuncNodeInfo().getFuncode();
				if (funcode == null) {
					funcode = "";
				}
				ExportExcelSheetsDlg showDlg = new ExportExcelSheetsDlg(sheets, allValidDimMembers, obj, funcode);
				showDlg.setVisible(true);

				if (showDlg.getResult() == UIDialog.ID_OK) {
					final MdTask[] tasks = showDlg.getSelectedTasks();
					final String[] taskName = showDlg.getTaskNames();
					final MdSheet[] Sheets = showDlg.getSelectSheets();
					final boolean exportType = showDlg.getExportType();
					final String ChosePath = showDlg.getChosePath();
					excelVersion = showDlg.getSelectedExcelVersion();
					
					Thread thread = new Thread() {
						public void run() {
							try {
								exportBatchExcels(tasks, taskName, Sheets, exportType, ChosePath);
							} catch (final BusinessException e) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										MessageDialog.showErrorDlg(getMainboard(),
												NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/* ��ʾ */, e.getMessage());
									}
								});
							}
						}
					};
					if (TbPlanFrameUtil.getTbPlanFrame(getMainboard()) == null) {
						exportBatchExcels(tasks, taskName, Sheets, exportType, ChosePath);
					} else {
						TbPlanFrameUtil.getTbPlanFrame(getMainboard()).invokeWithPorgress(thread);
					}
				}
			}
		} else {
			MessageDialog.showHintDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000718")/* ѡ������ */, NCLangRes
					.getInstance().getStrByID("tbb_plan", "01812pln_000721")/* ���������������������һ�������ٽ�����������Excel�Ĳ��� */);
			return;
		}
	}

	/**
	 * ��������excel�ķ���
	 * 
	 * @param mdTasks
	 * @param taskNames
	 * @param sheets
	 * @param exportType
	 * @param chosePath
	 * @throws BusinessException
	 */
	public void exportBatchExcels(MdTask[] mdTasks, String[] taskNames, MdSheet[] sheets, Boolean exportType, String chosePath)
			throws BusinessException {
		try {
		Map<String, MdSheet> m_filterSheets = new HashMap<String, MdSheet>();
		if (mdTasks == null || mdTasks.length <= 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					MessageDialog.showWarningDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000722")/* ����ȱʧ */, NCLangRes
							.getInstance().getStrByID("tbb_plan", "01812pln_000723")/* ����ѡ��Ҫ��ѡ������ */);
				}
			});
			return;
		}

		if (sheets == null || sheets.length <= 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					MessageDialog.showWarningDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000724")/* Excel��ȱʧ */,
							NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000725")/* ����ѡ��Ҫ��ѡ��excel�� */);
				}
			});
			return;
		}
		// ����exportName���ã���������������ʱ��excel�������������� �� + ҵ�񷽰� + ���� + ���֡�exportNameĬ��Ϊtrue�������������ʱ������������ + ҵ�񷽰� + ģ���� +������exportName Ĭ��Ϊfalse
		boolean exportName = true;
		if (mdTasks.length == 1) {
			exportName = false;
		}
		String[] pk_sheets = new String[sheets.length];
//		int count = 0;
		for (int i = 0; i < sheets.length; i++) {
			pk_sheets[i] = sheets[i].getPk_obj();
			// ��¼ѡ��ĵı�
			m_filterSheets.put(pk_sheets[i], sheets[i]);
		}
		
		if (excelVersion != null && excelVersion.equals(IExcelExportUtil.Excel2003)) {
			excelExportUtil = new ExcelExportUtil();
		} else {
			excelExportUtil = new ExcelExportUtilXSSF();
		}
		Workbook workBook = excelExportUtil.createWorkbook();
		Hashtable<String, CellStyle> hashStyleByKey = new Hashtable<String, CellStyle>();
		MdTaskDef taskDef = null;
		int index = 0;
		for (int j = 0; j < mdTasks.length; j++) {

			// �������ӵ�=========================
			MdSheet[] filterSheets = sheets;
			String[] pk_filterSheets = pk_sheets;
			// ================================
			String taskSheetName = taskNames[j];
			MdTask task = mdTasks[j];
			// ��������Ҫ��ӱ�����
			String[] pk_defaultSheets = SheetGroupCtl.getPkSheetsByTaskSheetList(task.getSheetlist(), false);
			if (pk_defaultSheets != null) {
				pk_filterSheets = ExportExcelUtil.intersect(pk_defaultSheets, pk_sheets);
				// if (pk_filterSheets != null && pk_filterSheets.length != 0) {
				// ʹ�ù���������߼��ж� ������ʹ���ѷ�װ�ù�����ϰ�ߣ��ô��ǹ淶
				if (ArrayUtils.isNotEmpty(pk_filterSheets)) {
					filterSheets = new MdSheet[pk_filterSheets.length];
					for (int k = 0; k < pk_filterSheets.length; k++) {
						if (m_filterSheets.containsKey(pk_filterSheets[k])) {
							filterSheets[k] = m_filterSheets.get(pk_filterSheets[k]);
						}
					}
				} else {
					//task��sheets��ѡ�񵼳���sheetû�н���
					NtbLogger.error(task.getObjname() + " has no sheets after filter with selected!");
					continue;
				}
			}

			TaskExtInfoLoader taskExtInfoLoader = new TaskExtInfoLoader();
			taskExtInfoLoader.setMdTask(task, pk_filterSheets);
			// ���������PK��ȡ����ģ������֣������Ǹ��������ױ�������
			if (taskDef == null)
				taskDef = task.getTaskDefWithoutDetail();
			/**
			 * �������ͷ�Ϊ���֣�1.��������ı�������һ��excel�У�2.ÿ������ֱ�����Ե�excel exportType�������𵼳�����
			 */
			if (!exportType) {
//				count = 0;
				workBook = excelExportUtil.createWorkbook();
				hashStyleByKey = new Hashtable<String, CellStyle>();
			}
			List<TaskSheetDataModel> tsDataModels = getTsDataModels(task, filterSheets);
			if (tsDataModels == null || tsDataModels.isEmpty()) {
				String messageString = "***************\r\n" + tsDataModels + " is empty!";
				NtbLogger.error(messageString);
				continue;
			}
			

			StringBuffer b = new StringBuffer();
			//�ϲ�ʱ������Ʊ仯
			if(exportType ==true){
				String entityName = mdTasks[j].getObjname();
				int first = entityName.lastIndexOf("_");
				int start = entityName.indexOf("(", first);
				int last = entityName.lastIndexOf(")");
//				b.append(entityName.substring(first+1, start));
//				b.append(sep);
				b.append(entityName.substring(start+1, last));
			}
			

			FontFactory fontFactory = excelExportUtil.createFontFactory(workBook);
			
			for (int i = 0; i < tsDataModels.size(); i++) {

				String shtName = "";
//				if(mdTasks.length==1 || (mdTasks.length>1 && exportType==false)){
					if (exportType) {
//						shtName = task.getPk_year() == null ? "" : task.getPk_year() + sep
//								+ ExportExcelUtil.getSerialNum(tsDataModels.get(i).getShowName(), 0) + taskSheetName/*
//																													 * ExportExcelUtil.getTaskEntryName(task
//																													 * , task.getPk_dataent())
//																													 */
//								+ sep + ExportExcelUtil.getSerialNum(tsDataModels.get(i).getShowName(), 1);
						shtName = task.getPk_year() == null ? "" : task.getPk_year() + sep +
								task.getPk_month() == null ? "" : task.getPk_year()
								+ taskSheetName
								+ sep + ExportExcelUtil.getSerialNum(tsDataModels.get(i).getShowName(), 1);
						
//						shtName = b.toString()+ sep + tsDataModels.get(i).getShowName();
					} else {
//						shtName = task.getPk_year() == null ?  tsDataModels.get(i).getShowName() : task.getPk_year() + sep + tsDataModels.get(i).getShowName();
						shtName = tsDataModels.get(i).getShowName();
					}
					
//					if (shtName.length() > 32) {
//						// ֱ�ӽ�ȡǰ30���ַ���������...
//	//					shtName = shtName.substring(0, 29) + "..";
//						shtName.substring(0, shtName.length()-1);
//					}
					shtName = excelExportUtil.resetSheetName(workBook, shtName);
					
					// modifer zhaojung
					if (shtName.isEmpty()) {
						shtName = b.toString()+ sep + tsDataModels.get(i).getShowName();
//						shtName = tsDataModels.get(j).getShowName().trim();
					}
//				}else{
//					//���
//					shtName = sheetNames[j];
//				}
					
				shtName = ExportExcelUtil.replaceIllegalSheetName(shtName);
				 
				//workBook.createSheet����������31���ַ���
//				if (shtName.length() > 32) {
//					// ֱ�ӽ�ȡǰ30���ַ���������...
////					shtName = shtName.substring(0, 29) + "..";
//					shtName = shtName.substring(0, shtName.length()-1);
////					shtName = shtName.substring(shtName.length() - 32, shtName.length() - 1);
////					if (shtName.contains("_")) {
////						int first = shtName.indexOf("_");
////						shtName = shtName.substring(first, shtName.length() - 1);
////					}
//				}
				
				Sheet sheet = null;
				try {
					shtName = ExportExcelUtil.subSheetName(shtName);
					sheet = workBook.createSheet(shtName);
				} catch (IllegalArgumentException e) {
					shtName = index + shtName;
					shtName = ExportExcelUtil.subSheetName(shtName);
					sheet = workBook.createSheet(shtName);
					index ++;
				}
				
				excelExportUtil.covertModel2Sheet(getCellsModelByTsModel(tsDataModels.get(i), taskExtInfoLoader), sheet, workBook, null, fontFactory, hashStyleByKey);
			}
			

			if (exportType && j != mdTasks.length - 1)
				continue;
			String taskName = (exportType && exportName) ? ExportExcelUtil.createTaskName(task, taskDef) : task.getObjname();
			try {
				// ����Ҷǩ����ʱ���������� �磺2014_01���Ѽ���_Sheet1 01������������
				ExportExcelUtil.setCount(0);
				excelExportUtil.saveWorkBook2Local(workBook, chosePath, taskName, exportType, j == mdTasks.length - 1);
			} catch (Exception e) {
				System.gc();
				throw new BusinessException(e);
			}
			// ֪ͨJVM������������
			System.gc();
		}
		} catch(Exception e) {
			MessageDialog.showErrorDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/* ��ʾ */,
					e.getMessage());
			NtbLogger.error(e);
		}
	}
	/**
	 * �õ�List<TaskSheetDataModel>
	 * 
	 * @param task
	 * @param sheets
	 * @return
	 * @throws BusinessException
	 */
	private List<TaskSheetDataModel> getTsDataModels(MdTask task, MdSheet[] sheets) throws BusinessException {
		TaskDataModel taskDataModel = null;
		// ֧���ڱ༭״̬�µ���excel
		if (getContext() != null && getCurrentViewer() != null) {
			if (getContext().getTasks() != null && getContext().getTasks().length > 0) {
				MdTask currnTask = getContext().getTasks()[0];
				if (task.getPk_obj().equals(currnTask.getPk_obj())) {
					getCurrentViewer().getViewManager().setAllViewsDataToTsModel();
					taskDataModel = getCurrentViewer().getViewManager().getTaskDataModel();

				}
			}
		}

		if (taskDataModel == null) {
			try {
			taskDataModel = TaskDataCtl.getTaskDataModel(task.getPrimaryKey(), null, false, null, true);
			String nodeType = getContext().getNodeType();
			TaskDataCtl.initTaskDataModelRuleInfo(taskDataModel, nodeType);
			TaskDataModelAction action = new TaskDataModelAction(taskDataModel);
			TaskFormulaFix.fixFormula(taskDataModel);
			action.executeTableHeadFormula(null);
			} catch (Exception e) {
				NtbLogger.error(e);
				NtbLogger.error("*****************");
				boolean isNull = taskDataModel == null;
				NtbLogger.error(isNull + "");
				NtbLogger.error("*****************");
			}
		}

		List<TaskSheetDataModel> tsdatamodels = new ArrayList<TaskSheetDataModel>();
		Map<String, String> hm_sheetPk = new HashMap<String, String>();
		if (sheets != null && sheets.length > 0) {
			for (MdSheet s : sheets) {
				hm_sheetPk.put(s.getPrimaryKey(), s.getPrimaryKey());
			}
		}
		
		// ������ģ���еõ�������ģ��
		TaskSheetDataModel[] tsdModels = taskDataModel == null ? new TaskSheetDataModel[0] : taskDataModel.getTaskSheetDataModels();
		// ���ݽ�����ѡ��ı��õ���ӦSheetModels
		for (int i = 0; i < tsdModels.length; i++) {
			MdSheet mdsheet = tsdModels[i].getMdSheet();
			if (mdsheet == null || (MapUtils.isNotEmpty(hm_sheetPk) && !hm_sheetPk.containsKey(mdsheet.getPrimaryKey()))) {
				continue;
			}
			tsdatamodels.add(tsdModels[i]);
		}
		return tsdatamodels;
	}

	/**
	 * �õ�CellsMode
	 * 
	 * @param taskSheetDataModel
	 * @param taskExtInfo
	 * @return
	 */
	private CellsModel getCellsModelByTsModel(TaskSheetDataModel taskSheetDataModel, TaskExtInfoLoader taskExtInfo) {
		CellsModel initCellsModel = taskSheetDataModel.getMdSheet().getCsmodel();
		// 1.��ֹ�ױ��ĳ����Ϊ�� 2.���ݵ�Ԫ��ĺϲ���Ϣ���ϲ���Ԫ��,���ظ�������,�Ѷ�ά�������õ�Cell���Ը��������������к�
		if (initCellsModel != null) {
			ZiorCellsModelUtil.loadCellsModel(taskSheetDataModel, initCellsModel, taskExtInfo, getContext());
		}
		return initCellsModel;
	}
}