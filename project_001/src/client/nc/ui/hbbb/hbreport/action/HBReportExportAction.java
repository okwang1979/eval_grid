package nc.ui.hbbb.hbreport.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import nc.impl.iufo.utils.MultiLangTextUtil;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.dataexchange.IExcelExport;
import nc.ui.iufo.dataexchange.RepDataExport;
import nc.ui.iufo.dataexchange.RepDataWithCellsModelExport;
import nc.ui.iufo.dataexchange.TableDataToExcel;
import nc.ui.iufo.input.CSomeParam;
import nc.ui.iufo.query.common.model.IUfoBillManageModel;
import nc.ui.pub.beans.UIFileChooser;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.iufo.pub.UFOString;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.KeyDetailDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iuforeport.rep.ReportVO;

import com.ufida.iufo.constant.output.IOutputMsgConstant;
import com.ufida.report.sysplugin.print.FreeReportPrintStatusMng;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.report.sysplugin.xml.ExtNameFileFilter;
import com.ufsoft.report.util.UfoPublic;
import com.ufsoft.table.CellsModel;

/**
 * 合并报表导出
 * 
 * @version V6.1
 * @author litfb
 */
public class HBReportExportAction extends AbsReportExportAction {

	private static final long serialVersionUID = 3764657684400573028L;

	public HBReportExportAction() {
		super();
		setCode("exportHBRep");
		setBtnName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
				"pub_0", "01830004-0002")/* @res "合并报表" */);
	}

	@Override
	public void doAction(ActionEvent e) throws Exception {

		// licence校验
		if (!FreeReportPrintStatusMng.CheckIfHaveLicense()) {
			ShowStatusBarMsgUtil.showErrorMsg(
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
							"1820001_0", "01820001-0844")/* @res "导出失败！" */,
					IOutputMsgConstant.CANNOT_EXP_NO_LICENSE, getModel()
							.getContext());
			return;
		}
		// 查询条件
		IUfoQueryCondVO queryCond = getUserQryPanel().getQueryArea()
				.getQuickQueryArea().genQueryCond(false);
		// 合并方案
		String pk_hbscheme = queryCond.getPk_task();
		HBSchemeVO hbSchemeVO = HBSchemeSrvUtils
				.getHBSchemeByHBSchemeId(pk_hbscheme);
		// 已选已录入报表
		RepDataQueryResultVO[] repQryResults = getSelectedInputValue(queryCond,
				hbSchemeVO);
		if (repQryResults.length == 0) {
			return;
		}

		String dlgTitleName = "合并导出";

		ExpRepExcelDlg dlg = new ExpRepExcelDlg(getModel().getContext()
				.getEntranceUI(), "", "",repQryResults);

		dlg.setTitle(dlgTitleName);
		RepDataQueryResultVO repDataQryResult = (RepDataQueryResultVO) getModel()
				.getSelectedData();

//		dlg.setTaskReports(repQryResults);

		dlg.showModal();

		if (dlg.getResult() == dlg.ID_OK) {
			// 按照组织对数据进行分组
			Map<String, List<RepDataQueryResultVO>> orgResultMap = getGroupRepDataResult(repQryResults);
		

			File file = dlg.getFileChooser().getSelectedFile();
			// 验证选择文件路径出现这样的异常路径的情况 D:\My Document\........\.......
			if (!UFOString.testJFileChooserPath(file.getPath())) {
				throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl
						.getNCLangRes()
						.getStrByID("1820001_0", "01820001-0438")/*
																 * @res
																 * "导出失败.请选择正确的路径导出:\n["
																 */
						+ file.getPath() + "]");
			}
			// 导出到的文件夹
			final String dirPath = file.getPath();
			// 导出的文件名 单个导出及为选择的路径，多个导出，选择文件夹，在迭代的时候加上文件名称
			// 如果单一文件导出时，浏览文件夹时，修改文件名时，文件不是正确的excel扩展名格式，则自动添加.xls扩展名
			 

			String orgName = null;
			String repTypeName = getRepTypeName();
			
			List<RepDataQueryResultVO> repDataList = null;
			UfoContextVO context = null;
			IRepDataParam param = null;
			List<IExcelExport> excelExp = null;
			Iterator<Map.Entry<String, List<RepDataQueryResultVO>>> it = orgResultMap
					.entrySet().iterator();
			List<String> selectReport = Arrays.asList(dlg.getCheckBoxTable().getSelectedPKs());
			while (it.hasNext()&&selectReport!=null&&selectReport.size()>0) {
				 
				Map.Entry<String, List<RepDataQueryResultVO>> entry = it.next();
				orgName = OrgUtil.getOrgName(entry.getKey());
				repDataList = entry.getValue();
				excelExp = new ArrayList<IExcelExport>();
			 
				// 单选选中的是文件，多选选中的是目录，如果是目录，需要另加文件名
				
				boolean isHbreport = true;
				for (RepDataQueryResultVO vo : repDataList) {
					String currentPk = vo.getAlone_id() + "@" + vo.getPk_report();
					if(!selectReport.contains(currentPk)){
						continue;
					}
					if("1".equals(vo.getKeyword10())){
						isHbreport = false;
					}
					context = getContextVO(vo);
					param = getRepDataParam(vo);
					MeasurePubDataVO dataVO = vo.getPubData();
					// 通过HBReportQueryUtil.getMeasurePubData得到的measpubdata信息不完整，
					// 后面调用企业报表导出数据时需要使用.@edit by dongjch 2015-06-06
					if (null == dataVO.getAloneID()) {
						String aloneid = MeasurePubDataBO_Client
								.getAloneID(dataVO);
						dataVO.setAloneID(aloneid);
					}
					context.setAttribute("key_MEASURE_PUB_DATA_VO", dataVO);
					RepDataWithCellsModelExport exportObj = new RepDataWithCellsModelExport(
							context, getCellModel(vo.getPk_report(), context,
									param));

					String strReportPK4ExportExcel = param.getReportPK();
					CSomeParam cSomeParam = new CSomeParam();
					cSomeParam.setAloneId(param.getAloneID());
					cSomeParam.setRepId(strReportPK4ExportExcel);
					cSomeParam.setUserID(param.getOperUserPK());
					MeasurePubDataVO pubData = param.getPubData();
					cSomeParam.setUnitId(pubData.getUnitPK());
					((RepDataExport) exportObj).setParam(cSomeParam);
					((RepDataExport) exportObj).setLoginDate(getLoginEnvVO()
							.getCurLoginDate());

					ReportVO rep = (ReportVO) IUFOCacheManager.getSingleton()
							.getReportCache().get(vo.getPk_report());
					exportObj.setSheetName(rep.getChangeName());

					excelExp.add(exportObj);

				}
				if(excelExp.isEmpty()){
					continue;
				}
				String filePath ="";
				if(isHbreport){
					filePath = dirPath + File.separator + orgName + "_"
								+ repTypeName + ".xls";
				}else{
					filePath = dirPath + File.separator + orgName + "_"
							+ "个别表" + ".xls";
				}
				
				File f = new File(filePath);
				if (f.exists()) {
					int iRet = UfoPublic.showConfirmDialog(
							getModel().getContext().getEntranceUI(),
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
									.getStrByID("1820001_0", "01820001-0842")
									/* @res "名称为 " */+ f.getName()
									+ nc.vo.ml.NCLangRes4VoTransl
											.getNCLangRes().getStrByID(
													"1820001_0",
													"01820001-0846")/*
																	 * @res
																	 * " 的excel文件已经存在，是否覆盖?"
																	 */,
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
									.getStrByID("1820001_0", "01820001-0133")
							/* @res "提示" */, JOptionPane.YES_NO_OPTION);
					if (iRet != JOptionPane.YES_OPTION) {
						continue;
					}
				}
				TableDataToExcel.translateToMultiSheet(
						excelExp.toArray(new IExcelExport[0]), filePath);
			}

		}

	}

	@Override
	protected Integer getRepVersion(HBSchemeVO hbSchemeVO) {
		return hbSchemeVO.getVersion();
	}

	@Override
	protected String getRepTypeName() {
		return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0",
				"01830004-0002")/* @res "合并报表" */;
	}

	@Override
	protected CellsModel getCellModel(String pk_report, UfoContextVO context,
			IRepDataParam param) throws Exception {
		CellsModel formatModel = CellsModelOperator
				.getFormatModelByPKWithDataProcess(context);
		CellsModel cellsModel = CellsModelOperator.fillCellsModelWithDBData(
				formatModel, context);
		return cellsModel;
	}

}
