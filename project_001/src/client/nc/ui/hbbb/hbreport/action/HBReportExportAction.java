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
 * �ϲ�������
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
				"pub_0", "01830004-0002")/* @res "�ϲ�����" */);
	}

	@Override
	public void doAction(ActionEvent e) throws Exception {

		// licenceУ��
		if (!FreeReportPrintStatusMng.CheckIfHaveLicense()) {
			ShowStatusBarMsgUtil.showErrorMsg(
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
							"1820001_0", "01820001-0844")/* @res "����ʧ�ܣ�" */,
					IOutputMsgConstant.CANNOT_EXP_NO_LICENSE, getModel()
							.getContext());
			return;
		}
		// ��ѯ����
		IUfoQueryCondVO queryCond = getUserQryPanel().getQueryArea()
				.getQuickQueryArea().genQueryCond(false);
		// �ϲ�����
		String pk_hbscheme = queryCond.getPk_task();
		HBSchemeVO hbSchemeVO = HBSchemeSrvUtils
				.getHBSchemeByHBSchemeId(pk_hbscheme);
		// ��ѡ��¼�뱨��
		RepDataQueryResultVO[] repQryResults = getSelectedInputValue(queryCond,
				hbSchemeVO);
		if (repQryResults.length == 0) {
			return;
		}

		String dlgTitleName = "�ϲ�����";

		ExpRepExcelDlg dlg = new ExpRepExcelDlg(getModel().getContext()
				.getEntranceUI(), "", "",repQryResults);

		dlg.setTitle(dlgTitleName);
		RepDataQueryResultVO repDataQryResult = (RepDataQueryResultVO) getModel()
				.getSelectedData();

//		dlg.setTaskReports(repQryResults);

		dlg.showModal();

		if (dlg.getResult() == dlg.ID_OK) {
			// ������֯�����ݽ��з���
			Map<String, List<RepDataQueryResultVO>> orgResultMap = getGroupRepDataResult(repQryResults);
		

			File file = dlg.getFileChooser().getSelectedFile();
			// ��֤ѡ���ļ�·�������������쳣·������� D:\My Document\........\.......
			if (!UFOString.testJFileChooserPath(file.getPath())) {
				throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl
						.getNCLangRes()
						.getStrByID("1820001_0", "01820001-0438")/*
																 * @res
																 * "����ʧ��.��ѡ����ȷ��·������:\n["
																 */
						+ file.getPath() + "]");
			}
			// ���������ļ���
			final String dirPath = file.getPath();
			// �������ļ��� ����������Ϊѡ���·�������������ѡ���ļ��У��ڵ�����ʱ������ļ�����
			// �����һ�ļ�����ʱ������ļ���ʱ���޸��ļ���ʱ���ļ�������ȷ��excel��չ����ʽ�����Զ����.xls��չ��
			 

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
			 
				// ��ѡѡ�е����ļ�����ѡѡ�е���Ŀ¼�������Ŀ¼����Ҫ����ļ���
				
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
					// ͨ��HBReportQueryUtil.getMeasurePubData�õ���measpubdata��Ϣ��������
					// ���������ҵ����������ʱ��Ҫʹ��.@edit by dongjch 2015-06-06
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
							+ "�����" + ".xls";
				}
				
				File f = new File(filePath);
				if (f.exists()) {
					int iRet = UfoPublic.showConfirmDialog(
							getModel().getContext().getEntranceUI(),
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
									.getStrByID("1820001_0", "01820001-0842")
									/* @res "����Ϊ " */+ f.getName()
									+ nc.vo.ml.NCLangRes4VoTransl
											.getNCLangRes().getStrByID(
													"1820001_0",
													"01820001-0846")/*
																	 * @res
																	 * " ��excel�ļ��Ѿ����ڣ��Ƿ񸲸�?"
																	 */,
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
									.getStrByID("1820001_0", "01820001-0133")
							/* @res "��ʾ" */, JOptionPane.YES_NO_OPTION);
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
				"01830004-0002")/* @res "�ϲ�����" */;
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
