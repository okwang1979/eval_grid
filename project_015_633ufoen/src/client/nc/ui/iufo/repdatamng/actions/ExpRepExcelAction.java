/**
 *
 */
package nc.ui.iufo.repdatamng.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.logging.Logger;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.funcnode.ui.AbstractFunclet;
import nc.impl.iufo.utils.MultiLangTextUtil;
import nc.itf.iufo.individual.IUFOIndividualSettingUtil;
import nc.login.vo.NCSession;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.iufo.NodeEnv;
import nc.ui.iufo.constants.IUfoeActionCode;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.dataexchange.FilePackage;
import nc.ui.iufo.dataexchange.IExcelExport;
import nc.ui.iufo.dataexchange.RepDataExport;
import nc.ui.iufo.dataexchange.RepDataWithCellsModelExport;
import nc.ui.iufo.dataexchange.TableDataToExcel;
import nc.ui.iufo.input.CSomeParam;
import nc.ui.iufo.input.funclet.AbsSwitchToftPanelAdaptor;
import nc.ui.iufo.input.table.TableInputParam;
import nc.ui.iufo.query.common.model.IUfoBillManageModel;
import nc.ui.iufo.repdataauth.actions.RepDataAuthViewBaseAction;
import nc.ui.iufo.repdatamng.view.ExpRepExcelDlg;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.UIFileChooser;
import nc.ui.uif2.DefaultExceptionHanler;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.model.AbstractUIAppModel;
import nc.ui.uif2.model.BillManageModel;
import nc.util.iufo.pub.UFOString;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.constant.CommonCharConstant;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.KeyDetailDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepExpParam;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.query.IUfoQueryInitParam;
import nc.vo.iufo.query.IUfoQueryLoginContext;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iufo.task.TaskInfoVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.lang.UFDate;
import nc.vo.uif2.LoginContext;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.collections.CollectionUtils;

import com.ufida.iufo.constant.impexp.ImpExpConstant;
import com.ufida.iufo.constant.output.IOutputMsgConstant;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.report.sysplugin.print.FreeReportPrintStatusMng;
import com.ufida.zior.console.ActionHandler;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.func.excel.text.ImpExpFileNameUtil;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.repdatainput.ufoe.IUfoTableInputActionHandler;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.iuforeport.tableinput.applet.RepDataParam;
import com.ufsoft.report.util.UfoPublic;
import com.ufsoft.table.CellsModel;

/**
 * 报表数据查询 - 导出Excel
 * @author wuyongc
 * @created at 2011-8-5,下午12:09:54
 *
 */
public class ExpRepExcelAction extends RepDataAuthViewBaseAction implements IUfoContextKey{

	private static final long serialVersionUID = -8985488630319954988L;

	private List<File> lf = new ArrayList<File>();

	private NodeEnv nodeEnv=null;

	final private String EXP_REP_EXCEL = "导出";

	public ExpRepExcelAction(){
		setBtnName("导出企业报表");
		setCode(IUfoeActionCode.REP_EXPORT_EXCEL);
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
		exceptionHandler = new DefaultExceptionHanler();
	}

	
	@SuppressWarnings("deprecation")
	private void setDataSource(UfoContextVO context) {
		DataSourceVO dataSource = new DataSourceVO();
		NCSession session = WorkbenchEnvironment.getInstance().getSession();
		dataSource.setDs_addr(session.getDsName());
		dataSource.setAccount_name(session.getBusiCenterName());
		dataSource.setDs_type(nc.vo.iufo.datasource.DataSourceVO.TYPENC2);
		context.setAttribute(IUfoContextKey.DATA_SOURCE, dataSource);
	}
	private LoginEnvVO getLoginEnvVO(){
		LoginEnvVO loginEnv = new LoginEnvVO();

		loginEnv.setCurLoginDate(WorkbenchEnvironment.getServerTime().toStdString());
		loginEnv.setDataExplore(true);
		loginEnv.setDataSource(IUFOIndividualSettingUtil.getDefaultDataSourceVo());
		loginEnv.setLangCode(WorkbenchEnvironment.getLangCode());
		loginEnv.setLoginUnit(nodeEnv.getCurrOrg());
		loginEnv.setRmsPK(nodeEnv.getCurrMngStuc());
		return loginEnv;
	}

	/* (non-Javadoc)
	 * @see nc.ui.uif2.NCAction#doAction(java.awt.event.ActionEvent)
	 */
	@Override
	public void doAction(ActionEvent e) throws Exception {
		if (!FreeReportPrintStatusMng.CheckIfHaveLicense()) {
			ShowStatusBarMsgUtil.showErrorMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0844")/*@res "导出失败！"*/,
					IOutputMsgConstant.CANNOT_EXP_NO_LICENSE, getModel().getContext());
			return;
		}
		final List<File> files = new ArrayList<File>();
		super.doAction(e);
		Object[] objs = ((BillManageModel)getModel()).getSelectedOperaDatas();
		List<RepDataQueryResultVO> lo = new ArrayList<RepDataQueryResultVO>();
		RepDataQueryResultVO[] repQryResults1 = new RepDataQueryResultVO[objs.length];
		System.arraycopy(objs, 0, repQryResults1, 0, objs.length);
		for(RepDataQueryResultVO obj:repQryResults1){
			if (obj.getInputstate().booleanValue()) {
				lo.add(obj);
			}
		}
		if(lo == null || lo.size()<=0){
			nc.ui.pub.beans.MessageDialog.showErrorDlg(getModel().getContext().getEntranceUI(), // 弹框提示
					"提示", "选择的数据没有已录入的数据，请重新选择数据");
			return;
		}
		RepDataQueryResultVO[] repQryResults = new RepDataQueryResultVO[lo.size()];
		System.arraycopy(lo.toArray(), 0, repQryResults, 0,lo.size());
		

		RepDataQueryResultVO repQryResult= (RepDataQueryResultVO) getModel().getSelectedData();

		String taskPK = getTaskPK();

		
		
		// 按照组织对数据进行分组
		Map<String, List<RepDataQueryResultVO>> orgResultMap = getGroupRepDataResult(repQryResults);
		JFileChooser chooser = new UIFileChooser();
		chooser.setFileFilter(new ZipFileFilter());
		chooser.setMultiSelectionEnabled(false);
		chooser.setSelectedFile(new File( "企业报表.zip"));

		int returnVal = chooser.showSaveDialog(getModel().getContext().getEntranceUI());
		File choosedFile = chooser.getSelectedFile();
		final File zipFile = choosedFile.getPath().toLowerCase().endsWith(".zip") ? choosedFile : new File(choosedFile.getPath() + ".zip");

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// 验证选择文件路径出现这样的异常路径的情况 D:\My Document\........\.......
			if (!UFOString.testJFileChooserPath(zipFile.getPath())) {
				throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0438")/* @res "导出失败.请选择正确的路径导出:\n[" */
						+ zipFile.getPath() + "]");
			}

			if (zipFile.exists()) {
				int result = MessageDialog.showOkCancelDlg(getModel().getContext().getEntranceUI(), "提示", String.format("文件：%s，已存在。是否覆盖？", zipFile.getPath()));
				if (result != UIDialog.ID_OK) {
					return;
				}
				zipFile.delete();
			}

			// 导出到的文件夹
			final String dirPath = zipFile.getParent();
			// 导出的文件名 单个导出及为选择的路径，多个导出，选择文件夹，在迭代的时候加上文件名称
			// 如果单一文件导出时，浏览文件夹时，修改文件名时，文件不是正确的excel扩展名格式，则自动添加.xls扩展名

			final String repTypeName = "企业报表";
			final Iterator<Map.Entry<String, List<RepDataQueryResultVO>>> it = orgResultMap.entrySet().iterator();

			// 起另一个线程导入
			final AbstractFunclet funclet = (AbstractFunclet) getModel().getContext().getEntranceUI();
			funclet.showStatusBarMessage(NCLangRes4VoTransl.getNCLangRes().getStrByID("10140udddb", "010140udddb0002"));
			funclet.showProgressBar(true);
			funclet.lockFuncWidget(true);

			final String userId = InvocationInfoProxy.getInstance().getUserId();
			final String userDataSource = InvocationInfoProxy.getInstance().getUserDataSource();
			final String groupId = InvocationInfoProxy.getInstance().getGroupId();

			ExecutorService executor = getModel().getContext().getExecutor();

			executor.execute(new Runnable() {

				@Override
				public void run() {
					InvocationInfoProxy.getInstance().setUserId(userId);
					InvocationInfoProxy.getInstance().setUserDataSource(userDataSource);
					InvocationInfoProxy.getInstance().setGroupId(groupId);

					List<String> orgNameList = new ArrayList<String>();
					String tempDir = System.getProperty("java.io.tmpdir");
					if(tempDir==null||tempDir.trim().length()<2){
						tempDir = dirPath;
					}
					try {
						while (it.hasNext()) {
							Map.Entry<String, List<RepDataQueryResultVO>> entry = it.next();
							String orgName = OrgUtil.getOrgName(entry.getKey());
							List<RepDataQueryResultVO> repDataList = entry.getValue();
							ArrayList<IExcelExport> excelExp = new ArrayList<IExcelExport>();
							// 单选选中的是文件，多选选中的是目录，如果是目录，需要另加文件名
							
							
							String filePath = tempDir + File.separator + orgName + "_" + repTypeName + ".xls";

							for (RepDataQueryResultVO vo : repDataList) {
								UfoContextVO context = getContextVO(vo);
								IRepDataParam param = getRepDataParam(vo);
								MeasurePubDataVO dataVO = vo.getPubData();
								// 通过HBReportQueryUtil.getMeasurePubData得到的measpubdata信息不完整，
								// 后面调用企业报表导出数据时需要使用.@edit by dongjch 2015-06-06
								if (null == dataVO.getAloneID()) {
									String aloneid = MeasurePubDataBO_Client.getAloneID(dataVO);
									dataVO.setAloneID(aloneid);
								}
								context.setAttribute("key_MEASURE_PUB_DATA_VO", dataVO);
								RepDataWithCellsModelExport exportObj = new RepDataWithCellsModelExport(context, getCellModel(vo.getPk_report(), context, param));

								String strReportPK4ExportExcel = param.getReportPK();
								CSomeParam cSomeParam = new CSomeParam();
								cSomeParam.setAloneId(param.getAloneID());
								cSomeParam.setRepId(strReportPK4ExportExcel);
								cSomeParam.setUserID(param.getOperUserPK());
								MeasurePubDataVO pubData = param.getPubData();
								cSomeParam.setUnitId(pubData.getUnitPK());
								((RepDataExport) exportObj).setParam(cSomeParam);
								((RepDataExport) exportObj).setLoginDate(getLoginEnvVO().getCurLoginDate());

								ReportVO rep = (ReportVO) IUFOCacheManager.getSingleton().getReportCache().get(vo.getPk_report());
								exportObj.setSheetName(rep.getChangeName());

								excelExp.add(exportObj);

							}
							File f = new File(filePath);
//							if (f.exists()) {
//								int iRet = UfoPublic.showConfirmDialog(getModel().getContext().getEntranceUI(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0842")
//								/* @res "名称为 " */+ f.getName() + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0846")/* @res " 的excel文件已经存在，是否覆盖?" */, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0133")
//								/* @res "提示" */, JOptionPane.YES_NO_OPTION);
//								if (iRet != JOptionPane.YES_OPTION) {
//									continue;
//								}
//							}
							TableDataToExcel.translateToMultiSheet(excelExp.toArray(new IExcelExport[0]), filePath);
							files.add(f);
						}

					} catch (Exception ex) {
						Logger.error(ex.getMessage(), ex);
					}
					for (File excelFile : files) {
						int pos = excelFile.getName().lastIndexOf("_");
						orgNameList.add(excelFile.getName().substring(0, pos));
					}

					if (CollectionUtils.isNotEmpty(files)) {
						try {
							ZipHelper.zip(zipFile, files.toArray(new File[0]), zipFile.getParent(), "YouyonZQmima20190529", true);
						} catch (Exception e) {
							Logger.error(e.getMessage(), e);
						}
					}
					// END
					// end animation
					funclet.lockFuncWidget(false);
					funclet.showProgressBar(false);

					if (orgNameList.size() > 0) {
						ShowStatusBarMsgUtil.showStatusBarMsg("导出成功。", getModel().getContext());
						String hint = "以下报表体系成员导出成功：";

						for (String successOrg : orgNameList) {
							hint += "\n" + successOrg;
						}

						MessageDialog.showHintDlg(getModel().getContext().getEntranceUI(), "导出结果", hint);
					} else {
						ShowStatusBarMsgUtil.showStatusBarMsg("导出失败。", getModel().getContext());
						MessageDialog.showWarningDlg(getModel().getContext().getEntranceUI(), "导出结果", "没有导出成功的报表体系成员。");
					}
				}

			});

		}
		
		
		
		//替换
		
		if(1==1){
			return;
		}

		Object[] retObjs = TaskSrvUtils.getTaskQueryService()
				.getTaskInfoAndBalConds(taskPK, true);
		TaskInfoVO taskInfo = (TaskInfoVO) retObjs[0];
		BalanceCondVO[] balConds = (BalanceCondVO[]) retObjs[1];
		IUfoQueryInitParam queryparam = ((IUfoQueryLoginContext) getModel().getContext())
				.getInitParam();
		KeyVO[] keys = queryparam.getKeyGroup().getKeys();
		TaskVO task = taskInfo.getTaskVO();
		ExpRepExcelDlg dlg = new ExpRepExcelDlg(getModel().getContext()
				.getEntranceUI(), EXP_REP_EXCEL,task,queryparam.getMainOrgPK());
		
		dlg.setTitle(EXP_REP_EXCEL);
		RepDataQueryResultVO repDataQryResult = (RepDataQueryResultVO) getModel().getSelectedData();

		dlg.setBalConds(balConds);
		MeasurePubDataVO pubData = repDataQryResult.getPubData();
		String[] keyVals = pubData.getKeywords();
		dlg.setKeyGroupCheckBoxPanel(keys);
		
		StringBuilder keywordGroupValue = new StringBuilder();
		String orgName = MultiLangTextUtil
				.getCurLangText(((IUfoBillManageModel) getModel())
						.getOrgPkMap().get(repQryResult.getPk_org()));
		keywordGroupValue.append(orgName);

		final String strAccSchemePK = task.getPk_accscheme();
		IKeyDetailData keyDetailData = null;
		for (int i = 1; i < keys.length; i++) {// 前面已经处理了单位关键字,所以直接从
												// 第二个关键字开始
			keyDetailData = KeyDetailDataUtil.getKeyDetailData(keys[i],	keyVals[i], strAccSchemePK);
			keywordGroupValue.append(",").append(keyDetailData.getMultiLangText());
		}

		ReportCache repCache = IUFOCacheManager.getSingleton().getReportCache();
		ReportVO[] reps = new ReportVO[repQryResults.length];
		for (int i = 0; i < reps.length; i++) {
			reps[i] = repCache.getByPK(repQryResults[i].getPk_report());
		}



		dlg.setTaskReports(repQryResults);

		String title1 = null;
		String title2 = null;
		if(reps.length == 1){
			title1 = reps[0].getCode() + "_" + reps[0].getChangeName();
			title2 = title1;
		}else{
			title1 = reps[0].getCode() + "_" + reps[0].getChangeName();
			title2 = keywordGroupValue.toString();
		}
		dlg.setDefaultFileName(new String[]{title1,title2});

		dlg.showModal();

		if (dlg.getResult() == UIDialog.ID_OK) {
			final RepExpParam expParam = dlg.getRepExpParam();


			final File oneFile = new File(expParam.getFilePath());
			File ff = oneFile;
			if(expParam.isSaveAll2OneFile()){
					if (ff.exists()) {
						int iRet = UfoPublic.showConfirmDialog(getModel().getContext().getEntranceUI(),
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0842")/*@res "名称为  "*/
										+ expParam.getFilePath()
										+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0",
												"01820001-0846")/* @res " 的excel文件已经存在，是否覆盖?"*/,
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0133")/* @res "提示" */,
								JOptionPane.YES_NO_OPTION);
						if (iRet != JOptionPane.YES_OPTION){
							return;
					}
				}
			}
			
			final JComponent UI = getModel().getContext().getEntranceUI();
			if (UI instanceof AbstractFunclet) {
				AbstractFunclet funclet = (AbstractFunclet) UI;
				funclet.showStatusBarMessage(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140udddb",
						"010140udddb0002")/* @res "正在进行后台操作, 请稍等..." */);
				funclet.showProgressBar(true);
				funclet.lockFuncWidget(true);
			}
			ExecutorService executor = getModel().getContext().getExecutor();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						excuteExport(expParam,strAccSchemePK,oneFile);
					} catch (Exception e){
						if (UI instanceof AbstractFunclet) {
							AbstractFunclet funclet = (AbstractFunclet) UI;
							funclet.lockFuncWidget(false);
							funclet.showProgressBar(false);
						}
						AppDebug.debug(e);
						ShowStatusBarMsgUtil.showErrorMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0608")/*@res "导出失败"*/,e.getMessage(), getModel().getContext());
					}finally{
						if (UI instanceof AbstractFunclet) {
							AbstractFunclet funclet = (AbstractFunclet) UI;
							funclet.lockFuncWidget(false);
							funclet.showProgressBar(false);
						}
					}
				}
		});

		} else if (dlg.getResult() == UIDialog.ID_CANCEL) {
			ShowStatusBarMsgUtil.showStatusBarMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820002-0031")/*@res "导出取消."*/,
					getModel().getContext());
			return;
		}
	}
	
	
	
    private  CellsModel getCellModel(String pk_report, UfoContextVO context, IRepDataParam param) throws Exception {
        CellsModel formatModel = CellsModelOperator.getFormatModelByPKWithDataProcess(context);
        CellsModel cellsModel = CellsModelOperator.fillCellsModelWithDBData(formatModel, context);
        return cellsModel;
    }
	
	/**
	 * 按照组织对数据进行分组
	 * 
	 * @param repQryResults
	 * @return Map<组织PK,报表数据查询结果VO>
	 */
	private Map<String, List<RepDataQueryResultVO>> getGroupRepDataResult(RepDataQueryResultVO[] repQryResults) {
		// 如果选中多条记录，则按照组织对数据进行分组，一个组织的报表导出为一个excel文件
		Map<String, List<RepDataQueryResultVO>> orgResultMap = new LinkedHashMap<String, List<RepDataQueryResultVO>>();
		for (RepDataQueryResultVO vo : repQryResults) {
			if (orgResultMap.containsKey(vo.getPk_org())) {
				orgResultMap.get(vo.getPk_org()).add(vo);
			} else {
				List<RepDataQueryResultVO> repList = new ArrayList<RepDataQueryResultVO>();
				repList.add(vo);
				orgResultMap.put(vo.getPk_org(), repList);
			}
		}
		return orgResultMap;
	}
	

	private void excuteExport(RepExpParam expParam, String strAccSchemePK, File oneFile) throws IOException {
		if(oneFile.exists()){
			nc.ui.pub.beans.MessageDialog.showErrorDlg(getModel().getContext().getEntranceUI(), // 弹框提示
					"提示", "请输入名！");
			return;
		}
		int lastPoint =  oneFile.getPath().lastIndexOf(File.separatorChar);
		String fileNameZip = oneFile.getPath()/*.substring(lastPoint+1, oneFile.getPath().length())*/;
		oneFile = new File(oneFile.getPath().substring(0, lastPoint));
		
		IUfoQueryInitParam queryparam = ((IUfoQueryLoginContext) getModel().getContext()).getInitParam();
		KeyVO[] keys = queryparam.getKeyGroup().getKeys();
		String extendName = ImpExpFileNameUtil.isExcel2007(expParam.getFilePath()) ? ImpExpConstant.XLSX : ImpExpConstant.XLS;
		FileOutputStream stream = null;
		boolean bSuc = false;
		Map<String,byte[]> map = (Map<String, byte[]>) ActionHandler.execWithZip(IUfoTableInputActionHandler.class.getName(), "exportRepData2Excel",
				new Object[]{getLoginEnvVO(),expParam,keys,strAccSchemePK,getRmsPK(),getMainOrgPK()});
		List<File>fileList = new ArrayList<File>();
		String parentPath = null;
		try {
			if(expParam.isSaveAll2OneFile()){
				stream = new FileOutputStream(expParam.getFilePath());
				Collection<byte[]> c = map.values();
				if(c != null && c.size()==1){
					stream.write(c.iterator().next());
					stream.flush();
//					stream.close();
					bSuc = true;
					fileList.add(oneFile);
					parentPath = oneFile.getParent();
				}
			}else{
				Set<Entry<String, byte[]>> set = map.entrySet();
				for (Entry<String, byte[]> entry : set) {
					if( oneFile.isDirectory()){
						parentPath = oneFile.getPath();

					}else{
						String path = oneFile.getPath();
						int lastFileSeparator = path.lastIndexOf(File.separator);
						String fileName = path.substring(lastFileSeparator + 1);
						int lastPointIndex = fileName.lastIndexOf(".");
						if(lastPointIndex>0 && lastPointIndex<fileName.length()-1){
							parentPath = oneFile.getParent();
						}else{
							parentPath = oneFile.getPath();
						}
					}
					File parentFilePath = new File(parentPath);
					if(!parentFilePath.exists()){
						boolean mkdir = parentFilePath.mkdirs();
						if(!mkdir){
							throw new RuntimeException("Make dir fail!");
						}
					}
					
					String file = parentPath + java.io.File.separator + entry.getKey();
					File f = new File(file);
					//TODO 
					
					if (f.exists()) {
						int iRet = UfoPublic.showConfirmDialog(getModel().getContext().getEntranceUI(),
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0842")/* @res "名称为  "*/
										+ expParam.getFilePath()
										+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0",
												"01820001-0846")/* @res " 的excel文件已经存在，是否覆盖?"*/,
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0133")/* @res "提示"*/,
								JOptionPane.YES_NO_OPTION);
						if (iRet != JOptionPane.YES_OPTION){
							continue;
						}
					}
					stream = new FileOutputStream(f);
					stream.write(entry.getValue());
					stream.flush();
					stream.close();
					bSuc = true;
					fileList.add(f);
				}
			}
		} catch (FileNotFoundException e) {
//			if (stream != null) {
//				stream.close();
//			}
			throw e;
		} catch (IOException e) {
//			if (stream != null) {
//				stream.close();
//			}
			throw e;
		} finally {
			stream.close();
		}
		if(expParam.isbZip()){
			FilePackage pack = new FilePackage();
			int fileSize = fileList.size();
			if(fileSize>0){
				String firstName = fileList.get(0).getName();
				String zipFileName = firstName.replace((CommonCharConstant.POINT + extendName),"(" + fileSize +").zip");
				String zipFile = parentPath + File.separator + zipFileName;
				pack.zipFile(fileList.toArray(new File[0]), zipFile);
				for (File file : fileList) {
					file.delete();
				}
			}
		}
		//begin pzm 利用压缩文件加密execl 20190529
//		FilePackage pack = new FilePackage();
		int fileSize = fileList.size();
		if(fileSize>0){
			String firstName = fileNameZip/*fileList.get(0).getName()*/;
			String zipFileName = firstName.replace((CommonCharConstant.POINT + extendName),".zip");
//			String zipFile = parentPath + File.separator + firstName;
			File zipF= new File(zipFileName);
			StringBuffer msg = new StringBuffer();
			msg.append("以下报表体系成员导出成功：");
			for(File fa:fileList){
//				int pos = fa.getName().lastIndexOf("_")== -1?fa.getName().length():fa.getName().lastIndexOf("_");
//				int last = fa.getName().substring(0, pos).lastIndexOf("_")== -1?fa.getName().length():fa.getName().substring(0, pos).lastIndexOf("_");
//				msg.append("\n").append(fa.getName().substring(fa.getName().indexOf("_")+1, last));
				
				String logFileName = fa.getName();
				logFileName = logFileName.replace( ".xls", "");
				
				String[]  names = logFileName.split("_");
				if(names.length>=2){
					msg.append("\n").append(names[0]+"_"+names[1]);
				}else{
					msg.append("\n").append(logFileName);
				}
				
				File f = new File(fa.getPath());
				encryZipFile(zipF, f, "YouyonZQmima20190529");
				f.delete();
				
				

				
				
			}
			MessageDialog.showHintDlg(getModel().getContext().getEntranceUI(), "导出结果", msg.toString());
		}
		//end
		
		
		if(bSuc){
			ShowStatusBarMsgUtil.showStatusBarMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820002-0030")/*@res "导出成功."*/,
					getModel().getContext());
			
		}
		
	
		
	}
	/**
	 * pzm 20190529
	 * @param zipFile
	 * @param addFile
	 * @param password
	 * 压缩文件加密
	 */
    public void encryZipFile(File zipFile,File addFile,String password){
    	 
        try {
            //创建压缩文件
            ZipFile respFile = new ZipFile(zipFile);
 
            //设置压缩文件参数
            ZipParameters parameters = new ZipParameters();
            //设置压缩方法
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
 
            //设置压缩级别
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
 
            //设置压缩文件加密
            parameters.setEncryptFiles(true);
 
            //设置加密方法
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
 
            //设置aes加密强度
            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
 
            //设置密码
            parameters.setPassword(password);
 
            //添加文件到压缩文件
            respFile.addFile (addFile,parameters);
            
          //  respFile.setPassword(password);
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }


//	/**
//	 * @create by wuyongc at 2011-9-29,下午1:56:29
//	 *
//	 * @param repQryResults
//	 * @return Map<组织PK,报表数据查询结果VO>
//	 */
//	private Map<String, List<RepDataQueryResultVO>> getGroupRepDataResult(
//			RepDataQueryResultVO[] repQryResults) {
//		/*
//		 *  如果选中多条记录，则按照组织对数据进行分组，一个组织的报表导出为一个excel文件。
//		 */
//		Map<String,List<RepDataQueryResultVO>> orgResultMap = new LinkedHashMap<String,List<RepDataQueryResultVO>>();
//		for(RepDataQueryResultVO rs : repQryResults){
//			if(orgResultMap.containsKey(rs.getPk_org())){
//				orgResultMap.get(rs.getPk_org()).add(rs);
//			}else{
//				List<RepDataQueryResultVO> repList = new ArrayList<RepDataQueryResultVO>();
//				repList.add(rs);
//				orgResultMap.put(rs.getPk_org(), repList);
//			}
//		}
//		return orgResultMap;
//	}

	/**
	 * @create by wuyongc at 2011-9-29,下午1:43:40
	 *
	 * @param repRequeryDataVO
	 * @return
	 */
	private UfoContextVO getContextVO(RepDataQueryResultVO repRequeryDataVO) {
		UfoContextVO context = new UfoContextVO();
		setDataSource(context);
		String pk_org = getModel().getContext().getPk_org();
		String pk_group = getModel().getContext().getPk_group();
		context.setAttribute(IUfoContextKey.CUR_GROUP_PK, pk_group);
		context.setAttribute(IUfoContextKey.CUR_REPORG_PK, pk_org);
		context.setAttribute(REPORT_PK, repRequeryDataVO.getPk_report());
		context.setAttribute(KEYGROUP_PK, repRequeryDataVO.getPubData()
				.getKeyGroup().getKeyGroupPK());
		ReportVO rep = IUFOCacheManager.getSingleton().getReportCache()
				.getByPK(repRequeryDataVO.getPk_report());
		context.setAttribute(REPORT_NAME, rep.getChangeName());
		context.setAttribute(MEASURE_PUB_DATA_VO, repRequeryDataVO.getPubData());
		return context;
	}

	/**
	 * @create by wuyongc at 2011-9-29,下午1:40:50
	 *
	 * @param repRequeryDataVO
	 * @return
	 */
	private IRepDataParam getRepDataParam(RepDataQueryResultVO repRequeryDataVO) {
		IRepDataParam param = new RepDataParam();
		param.setAloneID(repRequeryDataVO.getAlone_id());
		param.setReportPK(repRequeryDataVO.getPk_report());
		param.setOperType(TableInputParam.OPERTYPE_REPDATA_INPUT);
		param.setTaskPK(repRequeryDataVO.getPk_task());
		param.setRepMngStructPK(nodeEnv.getCurrMngStuc());
		param.setRepOrgPK(nodeEnv.getCurrOrg());
		param.setCurGroupPK(WorkbenchEnvironment.getInstance().getGroupVO()
				.getPk_group());
		param.setPubData(repRequeryDataVO.getPubData());
		return param;
	}

	@Override
	protected String getRepPK() {
		return ((RepDataQueryResultVO)getModel().getSelectedData()).getPk_report();
	}

	@Override
	protected String getOrgPK() {
		return ((RepDataQueryResultVO)getModel().getSelectedData()).getPk_org();
	}

	@Override
	protected String getRmsPK() {
		return nodeEnv.getCurrMngStuc();
	}

	@Override
	protected String getMainOrgPK() {
		return nodeEnv.getCurrOrg();
	}

	@Override
	protected String getTaskPK() {
		return ((RepDataQueryResultVO)getModel().getSelectedData()).getPk_task();
	}

	@Override
	public void setModel(AbstractUIAppModel model) {
		super.setModel(model);
		((DefaultExceptionHanler)exceptionHandler).setContext(model.getContext());
		((DefaultExceptionHanler)exceptionHandler).setErrormsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0844")/*@res "导出失败！"*/);
	}
	/**
	 * @param loginContext the loginContext to set
	 */
	public void setLoginContext(LoginContext loginContext) {
		if (loginContext.getEntranceUI() instanceof AbsSwitchToftPanelAdaptor)
			nodeEnv=((AbsSwitchToftPanelAdaptor)loginContext.getEntranceUI()).getNodeEnv();
	}

	@Override
	public boolean isActionEnable(){
		
		return ((BillManageModel)getModel()).getSelectedOperaDatas() != null/*getModel().getSelectedData() != null*/;
	}


}