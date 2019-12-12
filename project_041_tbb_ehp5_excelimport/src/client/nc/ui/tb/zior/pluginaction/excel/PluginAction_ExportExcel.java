package nc.ui.tb.zior.pluginaction.excel;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;
import nc.ms.mdm.convertor.IStringConvertor;
import nc.ms.mdm.convertor.StringConvertorFactory;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ms.tb.form.SheetGroupCtl;
import nc.ms.tb.formula.excel.core.IWorkSheet;
import nc.ms.tb.pub.plugin.TbPluginLoader;
import nc.ms.tb.pub.plugin.itf.ITbPluginSheetsFilter;
import nc.ms.tb.rule.RuleManager;
import nc.ms.tb.task.RuleExecuteHelper;
import nc.ms.tb.task.TaskExtInfoLoader;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ms.tb.task.data.TaskDataModelAction;
import nc.ms.tb.task.data.TaskSheetDataModel;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIDialog;
import nc.ui.tb.query.sum.TbQuerySumLeftDirView;
import nc.ui.tb.zior.TbPlanFrameUtil;
import nc.ui.tb.zior.TbZiorUiCtl;
import nc.ui.tb.zior.ZiorCellsModelUtil;
import nc.ui.tb.zior.pluginaction.AbstractTbPluginAction;
import nc.ui.tb.zior.pluginaction.TbPluginActionDescriptor;
import nc.ui.tb.zior.pluginaction.exportbatchexcel.ExcelFileFilter;
import nc.ui.tb.zior.pluginaction.exportbatchexcel.ExportExcelUtil;
import nc.vo.mdm.cube.DimSectionTuple;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.tb.form.MdSheet;
import nc.vo.tb.form.query.TbSumEnt;
import nc.vo.tb.ntbenum.NTBActionEnum;
import nc.vo.tb.rule.BusiRuleVO;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.report.sysplugin.excel.HSSFFontFactory;
import com.ufsoft.report.sysplugin.xml.ExtNameFileFilter;
import com.ufsoft.table.CellsModel;

/**
 * ����excel
 * 
 * Vindicator:liqqc
 * 
 */

public class PluginAction_ExportExcel extends AbstractTbPluginAction {
	private static final long serialVersionUID = 1L;
	private PlanToSheetDlg planTosheetDlg = null;
	private static final String sep = "_";

	public PluginAction_ExportExcel(String name, String code) {
		super(name, code);
	}

	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		TbPluginActionDescriptor desc = new TbPluginActionDescriptor();
		desc.setName(TbActionName.getName_ExportExcel());
		desc.setGroupPaths(TbActionName.getName_Excel());
		// desc.setExtensionPoints(new XPOINT[]{XPOINT.MENU});
		return desc;
	}

	/*
	 * ִ���¼�
	 */
	@SuppressWarnings("restriction")
	@Override
	public void actionPerformed(ActionEvent actionevent) throws BusinessException {
		Map<String, MdSheet> m_filterSheets = new HashMap<String, MdSheet>();
		MdTask[] tasks = getContext().getTasks();
 		String name = null;
		String path = null;
		boolean charge = true;
		if (tasks == null || tasks.length == 0) {
			MessageDialog.showHintDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000718")/* ѡ������ */, NCLangRes
					.getInstance().getStrByID("tbb_plan", "01812pln_000719")/* ���������������ѡ��Ҫ����������,�ٽ��е���Excel���� */);
			return;
		}
		
		if(tasks.length > 1){
			for (int i = 0; i < tasks.length; i++) {
				for (int j = i; j < tasks.length; j++) {
					if(!tasks[i].getPk_taskdef().equals(tasks[j].getPk_taskdef())){
						//��ʾ����ģ�岻ͬ
						MessageDialog.showHintDlg(this.getMainboard(),NCLangRes.getInstance().getStrByID("tbb_plan","01812pln_000474")/**��ʾ**/,
								NCLangRes.getInstance().getStrByID("tbb_plan","01812pln_000730"));//��ѡ����ͬ������ģ��)
						return;
					}
				}
			}
		}
		
		MdSheet[] sheets = null;
		if (tasks != null && tasks.length == 1) {
			List<MdSheet> mdsheets = new ArrayList<MdSheet>();
			// ��ȡ����ָ���ı�
			String[] Sheets = SheetGroupCtl.getPkSheetsByTaskSheetList(tasks[0].getSheetlist(), false);
			// �б�洢��
			if (Sheets != null) {
				if (getCurrentViewer() != null) {
					if (getCurrentViewer().getViewManager().getTaskDataModel() != null) {
						List<String> pk_sheet = new ArrayList<String>();
						for (int idx = 0; idx < Sheets.length; idx++) {
							pk_sheet.add(Sheets[idx]);
						}
						TaskSheetDataModel[] tsdm = getCurrentViewer().getViewManager().getTaskDataModel().getTaskSheetDataModels();
						for (int i = 0; i < tsdm.length; i++) {
							MdSheet sheet = tsdm[i].getMdSheet();
							if (sheet != null && !pk_sheet.contains(sheet.getPrimaryKey())) {
								continue;
							}
							mdsheets.add(sheet);
						}
					}
				} else {
					SuperVO[] vos = NtbSuperServiceGetter.getINtbSuper().queryByPKArray(MdSheet.class, Sheets);
					for (SuperVO vo : vos) {
						if (vo == null)
							continue;
						if (vo instanceof MdSheet) {
							MdSheet sheet = (MdSheet) vo;
							mdsheets.add(sheet);
						}
					}
				}
				sheets = mdsheets.toArray(new MdSheet[0]);
				
				
			} else {
				if (getCurrentViewer() != null) {
					sheets = IWorkSheetToMdSheet(getCurrentViewer().getViewManager().getTaskDataModel().getAllSheet());
				} else {
					for (MdTask task : tasks) {
						if (task == null)
							continue;
						sheets = getValidSheetsByPlan(task);
						if (sheets != null)
							break;
					}
				}
			}
		} else {
			if (getCurrentViewer() != null) {
				sheets = IWorkSheetToMdSheet(getCurrentViewer().getViewManager().getTaskDataModel().getAllSheet());
			} else {
				for (MdTask task : tasks) {
					if (task == null)
						continue;
					sheets = getValidSheetsByPlan(task);
					if (sheets != null)
						break;
				}
			}
		}
		if (sheets == null) {
			return;
		}
		
		
		
		
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
		

		planTosheetDlg = new PlanToSheetDlg(getMainboard(), querySheets.toArray(new MdSheet[0]), tasks.length > 1, tasks[0].getObjname());
		// �����￪ʼȡ����ѡ�����ѡ��Ҫ������sheet
		if (planTosheetDlg.showModal() == planTosheetDlg.ID_OK) {
			sheets = planTosheetDlg.getSelectSheets();
			name = planTosheetDlg.getNameBySelf().getText().trim(); // 2014��test1_Ԥ����_�����_Ĭ�ϰ汾(������Զ����)
			path = planTosheetDlg.getChosePath().trim();
//			String obstractpath = path + "//" + name;  //C:\Documents and Settings\Administrator\����//wg2013-����_Ԥ����_�����_Ĭ�ϰ汾_2011����ڼ�(�ܾ�����)
//			String filename = name + ".xls";
//			File f = new File(path);
			
//			boolean flag = false; // �ж������Ƿ��и��ļ�
//			if (f.exists() && f.isDirectory()) {
//				for (File dirfile : f.listFiles()) {
//					if (dirfile.getName().equals(filename)) {
//						flag = true;
//					}
//				}
//			}
			

			// �������Excel�ʹ�������Excel�ļ�����ͬ������Ƿ񸲸��жϣ���������򴴽�������������򷵻�
//			if (flag) {
//				String title = NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000731")/* �ļ����� */;
//				String question = NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000732")/* �Ƿ񸲸����е��ļ� */;
//				int value = MessageDialog.showYesNoDlg(getMainboard(), title, question);
//				if (value == UIDialog.ID_NO) {
//					//������
//					
//					
//				} else {
//					createExcelIfNotExist(obstractpath, planTosheetDlg);
//					createFileByInf(planTosheetDlg, sheets, tasks, m_filterSheets, charge);
//				}
//			} else {
//				createExcelIfNotExist(obstractpath, planTosheetDlg);
				createFileByInf(planTosheetDlg, sheets, tasks, m_filterSheets, charge);
//			}
		} else {
			return;
		}
	}

	private void createExcelIfNotExist(String obstractpath, PlanToSheetDlg planTosheetDlg2) {
		File file = new File(obstractpath);
		ExtNameFileFilter xf = new ExtNameFileFilter("xls");
		file = xf.getModifiedFile(file);
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(file);
			NtbLogger.info(stream.toString());
		} catch (Exception e2) {
			planTosheetDlg2.getPnlMain().getStatusBar().setStatusBarMessage(e2.getMessage());
			return;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e1) {
					NtbLogger.print(e1);
				}
			}
		}
	}

	private void createFileByInf(PlanToSheetDlg planTosheetDlg2, MdSheet[] sheets, MdTask[] tasks, Map<String, MdSheet> m_filterSheets, boolean charge)
			throws BusinessException {
		String name = planTosheetDlg.getNameBySelf().getText();
		String taskName = null;
		if (sheets == null || sheets.length == 0) {
			return;
		}
		String[] pk_sheets = new String[sheets.length];
		for (int i = 0; i < sheets.length; i++) {
			pk_sheets[i] = sheets[i].getPk_obj();
			// ��¼ѡ��ĵı�
			m_filterSheets.put(pk_sheets[i], sheets[i]);
		}
		boolean exportType = planTosheetDlg.getExportType(); // Ĭ��true�Ǻϲ�   false���ϲ�   ������������ �ж��Ƿ�ϲ�excel
		boolean exportName = false; // �������һ��
		// Ĭ��exportName������Ϊfalse,�������Ӱ�쵼��excelʱ�������淶�����Ե�ֻ��һ������ʱ����ֵΪfalse,��Ϊ����excel����ѡ����ж�
		if (tasks.length > 1) {
			exportName = true;
		}else{
			exportType = false; //ֻ��һ��
		}
		
		if (exportType && !isSameTaskDef(tasks)) {
			MessageDialog.showErrorDlg(getMainboard(),NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/* ��ʾ */,NCLangRes
					.getInstance().getStrByID("tbb_plan", "01812pln_000730")/* ��ѡ����ͬģ������� */);
			return;
		}
		String chosepath = planTosheetDlg.getChosePath(); // 2014��test1_Ԥ����_�����_Ĭ�ϰ汾(������Զ����)
		HSSFWorkbook workBook = new HSSFWorkbook();
		MdTaskDef taskDef = null;
		
		for (int i = 0; i < tasks.length; i++) {
			// �����洢����֮��������
			MdSheet[] filterSheets = sheets;
			String[] pk_filterSheets = pk_sheets;
			MdTask task = tasks[i];
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
			taskExtInfoLoader.setMdTask(task, pk_filterSheets/* pk_sheets */);
			if (taskDef == null) {
				taskDef = task.getTaskDefWithoutDetail();
			}
			if (!exportType) {
				workBook = new HSSFWorkbook();
			}
			List<TaskSheetDataModel> tsDataModels = getTsDataModels(task, filterSheets);
			if (tsDataModels == null || tsDataModels.isEmpty()) {
				continue;
			}
			HSSFFontFactory fontFactory = new HSSFFontFactory(workBook);
			
			String[] sheetNames = new String[sheets.length];
			if(exportType ==true){
				String entityName = tasks[i].getObjname();
				StringBuffer b = new StringBuffer();
				int first = entityName.lastIndexOf("_");
				int start = entityName.indexOf("(");
				int last = entityName.lastIndexOf(")");
				b.append(entityName.substring(first+1, start));
				b.append(sep);
				b.append(entityName.substring(start+1, last));
				for (int j = 0; j < sheets.length; j++) {
					StringBuffer buffer = new StringBuffer();
					buffer.append(b.toString());
					buffer.append(sep);
					buffer.append(sheets[j].getObjname());
					sheetNames[j] = buffer.toString();
				}
			}
			
			for (int j = 0; j < tsDataModels.size(); j++) {
				String shtName = "";
//				String shtName = sheetNames[j];
					if (exportType) {
						if ((getContext().getPkFuncCode().equals("18122201")) && getTbQuerySumLeftDirView().getTmodel() != null) {
							shtName = task.getPk_year() == null ? "" : task.getPk_year() + sep
									+ ExportExcelUtil.getSerialNum(tsDataModels.get(j).getShowName(), 0)
									+ ((TbSumEnt) getTbQuerySumLeftDirView().GetSelectNode()).getObjname().trim()
									+ ExportExcelUtil.getSerialNum(tsDataModels.get(j).getShowName(), 1);

						} else {
							if(shtName.isEmpty()){
//								shtName = task.getPk_year() == null ? "" : task.getPk_year() + sep
//										+ ExportExcelUtil.getSerialNum(tsDataModels.get(j).getShowName(), 0)+ sep
//										+ ExportExcelUtil.getTaskEntryName(task, task.getPk_dataent()).trim()+ sep
//										+ ExportExcelUtil.getSerialNum(tsDataModels.get(j).getShowName(), 1);
								
								if(task.getPk_year()!=null ||task.getPk_month()!=null ){
									if(task.getPk_year()!=null){
										shtName = task.getPk_year()+ sep;
//												+ ExportExcelUtil.getTaskEntryName(task, task.getPk_dataent()).trim()+ sep
//												+ ExportExcelUtil.getSerialNum(tsDataModels.get(j).getShowName(), 1);
									}
									
									if(task.getPk_month()!=null){
										shtName = shtName + task.getPk_month()+ sep;
//											+ ExportExcelUtil.getSerialNum(tsDataModels.get(j).getShowName(), 1);
									}
									
									shtName = shtName+ ExportExcelUtil.getTaskEntryName(task, task.getPk_dataent()).trim()+ sep
											+ ExportExcelUtil.getSerialNum(tsDataModels.get(j).getShowName(), 1);
								}else{
									//�Ҳ���ʱ��
									shtName = sheetNames[j];
								}
								
//								shtName = task.getPk_year() == null ? "" : task.getPk_year() + sep
//										+ ExportExcelUtil.getTaskEntryName(task, task.getPk_dataent()).trim()+ sep
//										+ ExportExcelUtil.getSerialNum(tsDataModels.get(j).getShowName(), 1);
							}
						}
//						shtName = sheetNames[j];
					} else {
//						shtName = task.getPk_year() == null ? "" : task.getPk_year() + sep + tsDataModels.get(j).getShowName().trim();
						shtName = tsDataModels.get(j).getShowName().trim();
					}

					if (shtName.length() > 32) {
						// ֱ�ӽ�ȡǰ30���ַ���������...
						shtName = shtName.substring(0, shtName.length()-1);
					}

					// ���ڿ��ܵ��ظ�������б���
					shtName = resetSheetName(workBook, shtName);
					
					if (shtName.isEmpty()) {
						shtName = tsDataModels.get(j).getShowName().trim();
					}
			
				
				
				
				HSSFSheet sheet = workBook.createSheet(shtName);
				ExcelExportUtil.covertModel2Sheet(getCellsModelByTsModel(tsDataModels.get(j), taskExtInfoLoader), sheet, workBook, null, fontFactory);
			}

			if (exportType && i != tasks.length - 1)
				continue;

//			if (name != null && !name.isEmpty()) {
//				if (name.trim().endsWith(".xls")) {
//					int position = name.trim().lastIndexOf(".xls");
//					name = name.trim().substring(0, position);
//				}
//				if (name.trim().endsWith(".xlsx")) {
//					int positionx = name.trim().lastIndexOf(".xlsx");
//					name = name.trim().substring(0, positionx);
//				}
//				taskName = name.trim();
//			} else 
				
				if(name != null){
				  taskName = (exportType && exportName) ? createTaskName(task, taskDef) : task.getObjname();
				}

			if (charge == true) {
				try {
					saveWorkBook2Local(workBook, chosepath, taskName, exportType, i == tasks.length - 1);
				} catch (Exception e) {
					System.gc();
					throw new BusinessException(e);
				}
				System.gc();
			}
		}
	}


	/**
	 * ����excel
	 */
	@SuppressWarnings("restriction")
	private void saveWorkBook2Local(HSSFWorkbook workBook, String parentpath, String fileName, boolean exportType, boolean isShowSuccessInfo)
			throws Exception {
		if (workBook == null) {
			return;
		}
		ExtNameFileFilter xf = new ExtNameFileFilter("xls");
		FileSystemView fsv = FileSystemView.getFileSystemView();  // C:\Documents and Settings\Administrator\My Documents
		File file = fsv.getDefaultDirectory();   // C:\Documents and Settings\Administrator\����\2014��test1_Ԥ����_�����_Ĭ�ϰ汾(������Զ����)
		String absolutelyfile = parentpath + "\\" + fileName;   // C:\Documents and Settings\Administrator\����\2014��test1_Ԥ����_�����_Ĭ�ϰ汾(������Զ����)
		file = new File(absolutelyfile);   // C:\Documents and Settings\Administrator\����\2014��test1_Ԥ����_�����_Ĭ�ϰ汾(������Զ����).xls
		file = xf.getModifiedFile(file);
		
		if (file.exists()) {
			int value = MessageDialog.showYesNoDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000731")/* �ļ����� */
			, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000732")/* �Ƿ񸲸����е��ļ� */);
			
			// ѡ�񲻸���
			if (value == UIDialog.ID_NO) {
				// ��������ѡ���
				JFileChooser fileChooser = new JFileChooser(file.getAbsolutePath());
				fileChooser.setDialogTitle(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0", "01050plan001-0394")/* @res "������" */);
				// ���ļ���һ���������Ѿ��������ļ�������ʹ�������¸��ļ�����
				fileChooser.setSelectedFile(file);
				// ��ӹ��ˣ�ֻ��ʾexcel�ļ�
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.addChoosableFileFilter(new ExcelFileFilter("xlsx", file));
				fileChooser.addChoosableFileFilter(new ExcelFileFilter("xls", file));
				int result = fileChooser.showSaveDialog(getMainboard());

				// ȷ������
				if (result == JFileChooser.APPROVE_OPTION) {
					TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					if (path.equals(file.getAbsolutePath())) {
						MessageDialog.showHintDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/* ��ʾ */,
								NCLangRes.getInstance().getStrByID("tbb_plan", "�����޸��ļ���")/* �����޸��ļ��� */);

						return;

					}
					file = new File(path);
					// �����ǣ������󴴽�
					createNewXlsFile(file, workBook, isShowSuccessInfo);
				} else {
					// ȡ��
					return;
				}
			}else{
				// ѡ�񸲸ǣ�ֱ�Ӵ���
				createNewXlsFile(file, workBook, isShowSuccessInfo);
			}
		}else{
			// ��������ֱ�Ӵ���
			createNewXlsFile(file, workBook, isShowSuccessInfo);
		}
		
//		FileOutputStream stream = null;
//		try {
//			stream = new FileOutputStream(file);
//			workBook.write(stream);
//			stream.flush();
//			if (isShowSuccessInfo) {
//				MessageDialog.showHintDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/* ��ʾ */, NCLangRes
//						.getInstance().getStrByID("tbb_plan", "01812pln_000469")/* �����ɹ� */);
//			}
//		} catch (FileNotFoundException ex) {
//			MessageDialog.showErrorDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000243")/* ����ʧ�� */, ex.getMessage());
//			throw ex;
//		} catch (Exception e) {
//			AppDebug.debug(e);
//			throw e;
//		} finally {
//			try {
//				if (stream != null)
//					stream.close();
//			} catch (IOException e) {
//				AppDebug.debug(e);
//			}
//		}
	}

	private void createNewXlsFile(File file, HSSFWorkbook workBook, final boolean isShowSuccessInfo) throws Exception {
		TbPlanFrameUtil.getTbPlanFrame(getMainboard()).toFront();
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(file);
			workBook.write(stream);
			stream.flush();

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (isShowSuccessInfo) {
						MessageDialog.showHintDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/* ��ʾ */,
								NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000469")/* �����ɹ� */);
					}
				}
			});

		} catch (FileNotFoundException ex) {
			MessageDialog.showErrorDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000243")/* ����ʧ�� */, ex.getMessage());
			throw ex;
		} catch (Exception e) {
			AppDebug.debug(e);
			throw e;
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				AppDebug.debug(e);
			}
		}

	}
	
	
	
	private List<TaskSheetDataModel> getTsDataModels(MdTask task, MdSheet[] sheets) throws BusinessException {
		TaskDataModel taskDataModel = null;
		if (getContext() != null && getCurrentViewer() != null
		/* && getContext().getComplieStatus() == TbCompliePlanConst.COM_MODE_TASKEDIT */) {
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
		List<BusiRuleVO> defaultRules = null;
		defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(getMdTask().getPk_taskdef(), NTBActionEnum.CALACTION.toCodeString());
		if (defaultRules != null && defaultRules.size() > 0) {
			RuleExecuteHelper.executeWorkBookRule(taskDataModel, defaultRules);
		}
		TaskDataModelAction action = new TaskDataModelAction(taskDataModel);
		action.executeWorkBook();
	}

	public String createTaskName(MdTask task, MdTaskDef taskDef, Boolean exportType, Boolean exportName) {
		String str = (exportType && exportName) ? createTaskName(task, taskDef) : task.getObjname();
		return str;
	}

	public String createTaskName(MdTask task, MdTaskDef m_td) {
		StringBuffer sb = new StringBuffer();
//		String time = (String) task.getExtrAttribute(IDimDefPKConst.TIME);
//		if (time != null)
//			sb.append(time);
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
					//ȥ�������ʱ��
					if(!lv.getDocName().equals("BD_ACCPERIOD"))
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
			List<ITbPluginSheetsFilter> filter = TbPluginLoader.getPluginSheetsFilter();
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

	private String resetSheetName(HSSFWorkbook workBook, String shtName) {
		if (workBook != null) {
			HSSFSheet sheet = workBook.getSheet(shtName);
			if (sheet != null) {
				for (int i = 1; i < 100; i++) {
					sheet = workBook.getSheet(shtName + "_" + i);
					if (sheet == null) {
						return shtName + "_" + i;
					}
				}
			}
		}
		return shtName;
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

	private CellsModel getCellsModelByTsModel(TaskSheetDataModel taskSheetDataModel, TaskExtInfoLoader taskExtInfo) {
		CellsModel initCellsModel = taskSheetDataModel.getMdSheet().getCsmodel();
		if (initCellsModel != null) {
			ZiorCellsModelUtil.loadCellsModel(taskSheetDataModel, initCellsModel, taskExtInfo, getContext());
		}
		return initCellsModel;
	}
}
