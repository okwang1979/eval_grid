/**
 *
 */
package nc.ui.iufo.repdatamng.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.common.RuntimeEnv;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.funcnode.ui.AbstractFunclet;
import nc.impl.iufo.utils.NCLangUtil;
import nc.itf.iufo.commit.ICommitManageService;
import nc.itf.iufo.individual.IUFOIndividualSettingUtil;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.pub.iufo.basedoc.UserUtil;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.NodeEnv;
import nc.ui.iufo.constants.IUfoeActionCode;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.input.funclet.AbsSwitchToftPanelAdaptor;
import nc.ui.iufo.input.table.TableInputParam;
import nc.ui.iufo.query.common.IUfoQueryExecutor;
import nc.ui.iufo.repdataauth.actions.RepDataAuthEditBaseAction;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.uif2.DefaultExceptionHanler;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.editor.BillListView;
import nc.ui.uif2.model.AbstractUIAppModel;
import nc.ui.uif2.model.BillManageModel;
import nc.util.iufo.sysinit.UfobIndividualSettingUtil;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.org.OrgQueryUtil;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.uif2.LoginContext;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import com.ufida.dataset.Context;
import com.ufida.dataset.IContext;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.pub.tools.DateUtil;
import com.ufida.zior.console.ActionHandler;
import com.ufsoft.iufo.fmtplugin.BDContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.func.excel.text.ImpExpFileNameUtil;
import com.ufsoft.iufo.inputplugin.biz.UfoExcelImpUtil;
import com.ufsoft.iufo.inputplugin.biz.data.ImportExcelDataBizUtil;
import com.ufsoft.iufo.inputplugin.biz.data.ImportExcelTableRow;
import com.ufsoft.iufo.inputplugin.biz.data.InputDynEndRowDialog;
import com.ufsoft.iufo.inputplugin.biz.file.ChooseRepData;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;
import com.ufsoft.iuforeport.repdatainput.TableInputActionHandler;
import com.ufsoft.iuforeport.repdatainput.TableInputHandlerHelper;
import com.ufsoft.iuforeport.repdatainput.ufoe.IUfoTableInputActionHandler;
import com.ufsoft.iuforeport.reporttool.dialog.ExcelFileChooserDlg;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.iuforeport.tableinput.applet.RepDataParam;
import com.ufsoft.iuforeport.tableinput.applet.TableInputException;
import com.ufsoft.report.dialog.UfoDialog;
import com.ufsoft.report.util.UfoPublic;

/**
 * @author wuyongc
 * @created at 2011-8-5,����12:09:54
 * 
 */
public class ImpRepExcelAction extends RepDataAuthEditBaseAction {

	private static final long serialVersionUID = -8985488630319954988L;

	private LoginContext loginContext = null;

	private NodeEnv nodeEnv = null;

	private BillListView billListView = null;
	
	private static Set<String> errQuqe = new HashSet<String>();
	
	
	private static Set<String> scessQuqe = new HashSet<String>();

	public BillListView getBillListView() {
		return billListView;
	}

	public void setBillListView(BillListView billListView) {
		this.billListView = billListView;
	}

	private IUfoQueryExecutor queryExecutor = null;

	public ImpRepExcelAction() {
		setBtnName("������ҵ����");

		putValue(Action.ACCELERATOR_KEY,
				
				KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));

		setCode(IUfoeActionCode.REP_IMPORT_EXCEL);
		exceptionHandler = new DefaultExceptionHanler();
		((DefaultExceptionHanler) exceptionHandler).setErrormsg(NCLangUtil
				.getStrByID("1820001_0", "01820001-0442")/* @res "����ʧ�ܣ�" */);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doAction(ActionEvent e) throws Exception {
		super.doAction(e);
		JComponent UI = getModel().getContext().getEntranceUI();

		
		List<RepDataQueryResultVO> repRequeryDataVOs = ((nc.ui.iufo.query.common.model.IUfoBillManageModel) getModel()).getData();
		
		Set<String> canImportOrgPks = new HashSet<>();
		ReportCache repCache = IUFOCacheManager.getSingleton().getReportCache();
	
		Set<ReportVO> reports =  new HashSet<>();
		
		Map<String, RepDataQueryResultVO> orgQueryResult = new HashMap<>();
		
		for(RepDataQueryResultVO qr:repRequeryDataVOs ){
			canImportOrgPks.add(qr.getPk_org());
			ReportVO repVO = repCache.getByPK(qr.getPk_report());
			reports.add(repVO);
			orgQueryResult.put(qr.getPk_org(), qr);
		}
		//�ļ�map
		Collection<ImpOrgAndReport>  impReports = getFiles();
		if(impReports==null||impReports.isEmpty()){
			if (UI instanceof AbstractFunclet) {
				AbstractFunclet funclet = (AbstractFunclet) UI;
				funclet.lockFuncWidget(false);
				funclet.showProgressBar(false);
			}
			
			return;
		}
		impReports = getCanImport(impReports,canImportOrgPks);
		
		final	List<SingleExcelImportTask> importTasks = new ArrayList<SingleExcelImportTask>();
		TaskVO task =  TaskSrvUtils.getTaskVOById(repRequeryDataVOs.get(0).getPk_task());
		
		for(ImpOrgAndReport vo:impReports){
			RepDataQueryResultVO repRequeryDataVO = orgQueryResult.get(vo.getPk_org());
			if(repRequeryDataVO==null) continue;
			File file1 = vo.getFile();
			ModiExcelCustColumnHelper.modiExcelCustColumn(file1);
			final File file = file1;
			final IRepDataParam param = new RepDataParam();
			param.setPubData(repRequeryDataVO.getPubData());
			param.setAloneID(repRequeryDataVO.getAlone_id());
			param.setReportPK(repRequeryDataVO.getPk_report());
			param.setOperType(TableInputParam.OPERTYPE_REPDATA_INPUT);
			param.setTaskPK(repRequeryDataVO.getPk_task());
			param.setRepMngStructPK(nodeEnv.getCurrMngStuc());
			param.setRepOrgPK(repRequeryDataVO.getPk_org());
			param.setCurGroupPK(WorkbenchEnvironment.getInstance().getGroupVO().getPk_group());
			param.setVer("0");
			param.setOperUserPK(WorkbenchEnvironment.getInstance().getLoginUser().getCuserid());
			SingleExcelImportTask importTask = getImportTask(param,vo,file,task,reports);
			setImport( repRequeryDataVOs,importTask);
			importTasks.add(importTask);
			
		}
		
		
		
		
		// ����һ���̵߳���
		final AbstractFunclet funclet = (AbstractFunclet) getModel().getContext().getEntranceUI();
		funclet.showStatusBarMessage(NCLangRes4VoTransl.getNCLangRes().getStrByID("10140udddb", "010140udddb0002"));
		funclet.showProgressBar(true);
		funclet.lockFuncWidget(true);

		final String userId = InvocationInfoProxy.getInstance().getUserId();
		final String userDataSource = InvocationInfoProxy.getInstance().getUserDataSource();
		final String groupId = InvocationInfoProxy.getInstance().getGroupId();
		final List<ImpOrgAndReport> successOrgs = new ArrayList<>();
		ExecutorService executor = getModel().getContext().getExecutor();
		executor.execute(new Runnable() {

			@Override
			public void run() {
				InvocationInfoProxy.getInstance().setUserId(userId);
				InvocationInfoProxy.getInstance().setUserDataSource(userDataSource);
				InvocationInfoProxy.getInstance().setGroupId(groupId);
				//
				for (SingleExcelImportTask importTask : importTasks) {
					try {
						importTask.run();
						successOrgs.add(importTask.getVo());
					} catch (Exception e) {
						AppDebug.error("����:"+importTask.getVo().getSysOrgName()+" ʧ�ܣ�");
						AppDebug.error(e);
					}
				}
				funclet.lockFuncWidget(false);
				funclet.showProgressBar(false);

				if (successOrgs.size() > 0) {
					ShowStatusBarMsgUtil.showStatusBarMsg(NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0848"), getModel().getContext());
					String hint = "���±�����ϵ��Ա����ɹ���";

					for (ImpOrgAndReport successOrg : successOrgs) {
						hint += "\n" + successOrg.getSysOrgName();
					}

					MessageDialog.showHintDlg(getModel().getContext().getEntranceUI(), "������", hint);
				} else {
					ShowStatusBarMsgUtil.showStatusBarMsg("����ʧ�ܡ�", getModel().getContext());
					MessageDialog.showWarningDlg(getModel().getContext().getEntranceUI(), "������", "û�е���ɹ��ı�����ϵ��Ա��");
				}
			}
		});
		
		
		
		
		 
//		List<String> names = new ArrayList<>();
//		errQuqe.clear();
//		scessQuqe.clear();
//		
// 
//		if (repRequeryDataVOs != null && repRequeryDataVOs.size() > 0) {
//			for (int i = 0; i < repRequeryDataVOs.size(); i++) {
//				RepDataQueryResultVO repRequeryDataVO = repRequeryDataVOs
//						.get(i);
//				 ImpOrgAndReport currentReport = null;
//				for(ImpOrgAndReport rp:impReports){
//					if(rp.getPk_org()!=null&&rp.getPk_org().equals(repRequeryDataVO.getPk_org())&&rp.getReport().getPk_report().equals(repRequeryDataVO.getPk_report())){
//						currentReport = rp;
//						break;
//					}
//				}
//				 
//				 if(currentReport==null) continue;
//				
//				 
//				final ImpOrgAndReport findReprot = currentReport;
//				final IRepDataParam param = new RepDataParam();
//				param.setAloneID(repRequeryDataVO.getAlone_id());
//				param.setReportPK(repRequeryDataVO.getPk_report());
//				param.setOperType(TableInputParam.OPERTYPE_REPDATA_INPUT);
//				param.setTaskPK(repRequeryDataVO.getPk_task());
//				param.setRepMngStructPK(nodeEnv.getCurrMngStuc());
//				param.setRepOrgPK(repRequeryDataVO.getPk_org());
//				param.setCurGroupPK(WorkbenchEnvironment.getInstance()
//						.getGroupVO().getPk_group());
//				
//				dealMeasurePubdata(repRequeryDataVO);
//				param.setPubData(repRequeryDataVO.getPubData());
//				 
//				File file = findReprot.getFile();
//				if(file==null) continue;
//				readExcel(file);
//				final Object[] objs = doGetImportInfos(param, file,
//						nodeEnv.getCurrOrg());
//				
//			 
//				if (objs == null) {
//					ImpRepExcelAction.errQuqe.add(findReprot.getReport().getCode()+"_"+findReprot.getSysOrgName());
//					continue;
//					 
//				} else if (objs.length == 3) {
//					int dialogResult = (Integer) objs[2];
//					if (dialogResult != UfoDialog.ID_OK) {
//						ShowStatusBarMsgUtil.showStatusBarMsg(NCLangUtil
//								.getStrByID("1820001_0", "01820001-0052"/*
//																		 * @res
//																		 * "����ȡ����"
//																		 */),
//								getModel().getContext());
//						return;
//					} else if (objs[0] == null) {
//						ShowStatusBarMsgUtil.showErrorMsg(NCLangUtil
//								.getStrByID("1820001_0", "01820001-0442"/*
//																		 * @res
//																		 * "����ʧ�ܣ�"
//																		 */),
//								NCLangUtil
//										.getStrByID("1820001_0",
//												"01820002-0106"/*
//																 * ѡ�����Excelҳǩû��ƥ�䱨��
//																 * ��
//																 */),
//								getModel().getContext());
//						return;
//					}
//				}
//
//			
//			
//				ExecutorService executor = getModel().getContext()
//						.getExecutor();
//				executor.execute(new Runnable() {
//					@Override
//					public void run() {
//						try {
//							final File file = (File) objs[3];
//							List<Object[]> vParams = (List<Object[]>) objs[0];
//							boolean bAutoCal = (Boolean) objs[1];
//							String extendName = ImpExpFileNameUtil
//									.getExtendName(file.getPath());
//							byte[] bytes = ImportExcelDataBizUtil
//									.getFileBytes(file);
//							RepDataOperResultVO resultVO = (RepDataOperResultVO) ActionHandler
//									.execWithZip(
//											IUfoTableInputActionHandler.class
//													.getName(), "impExcelData",
//											new Object[] { param,
//													getLoginEnvVO(), vParams,
//													bAutoCal, task, bytes,
//													extendName });
//
//							getQueryExecutor().reQuery();
//
//							if (resultVO.getHintMessage() != null) {
//								throw new BusinessException(resultVO
//										.getHintMessage());
//							} else {
//								scessQuqe.add(findReprot.getReport().getCode()+"_"+findReprot.getSysOrgName());
//								
//							
//								
//							}
//
//						} catch (Exception e) {
//							ImpRepExcelAction.errQuqe.add(findReprot.getReport().getCode()+"_"+findReprot.getSysOrgName());
//							AppDebug.error(e);
//						} finally {
//						}
//					}
//				});
//
//			}
//
//		}
//
// 
//		
//		
//		StringBuffer sb = new StringBuffer();
//		names.removeAll(errQuqe);
//		
//		while(names.size()>errQuqe.size()+scessQuqe.size()){
//			Thread.sleep(1000);
//		}
//		int i=0;
//		if(names.size()>0){
//			
//			for(String str:names){
//				if(errQuqe.contains(str)){
//					continue;
//				}
//				if(i==0){
//					sb.append("���±�����ɹ�:").append("\n");
//				}
//				i++;
//				sb.append(str).append("\n");
//			}
//		}
//		if(errQuqe.size()>0){
//			sb.append("���±�����ʧ��:").append("\n");
//		}
//		for(String errMessage:errQuqe){ 
//					 
//					sb.append(errMessage).append("\n");
//				 
//			 
//			
//		}
// 
//		List<ImpOrgAndReport> filterReplrts = new ArrayList<>();
//		for(ImpOrgAndReport rp:impReports){
//			if(rp.getPk_org()==null){
//				filterReplrts.add(rp);
//			}
//		}
//		
//		if(filterReplrts.size()>0){
//			if(errQuqe.size()==0){
//				sb.append("���±�����ʧ��:").append("\n");
//			}
//			
//			for(ImpOrgAndReport rp:filterReplrts){ 
//				String reportCode = rp.getReport()!=null?rp.getReport().getCode():"";
//				if(reportCode.trim().length()>0){
//					sb.append(reportCode).append("_");
//				} 
//				sb.append(rp.getRepName()).append("\n");
//		
//			}
//		}
//		if (UI instanceof AbstractFunclet) {
//			AbstractFunclet funclet = (AbstractFunclet) UI;
//			funclet.lockFuncWidget(false);
//			funclet.showProgressBar(false);
//		}
//		
//		ShowStatusBarMsgUtil.showStatusBarMsg("�������",	getModel().getContext());
//		
//		if(sb.length()==0){
//			MessageDialog.showHintDlg(null, "������Ϣ", "ѡ���ļ���û���ܹ�����ı���!"); 
//		}else{
//			MessageDialog.showHintDlg(null, "�������", sb.toString()); 
//		}
	
	}
	
	
	private void setImport(List<RepDataQueryResultVO> repRequeryDataVOs,
			SingleExcelImportTask importTask) throws Exception {
		 List<RepDataQueryResultVO> imports = new ArrayList<RepDataQueryResultVO>();
		 List<String> importRepCodes = new ArrayList<String>();
		 List<Object[]>  reportInfos =  ( List<Object[]>)importTask.getObjs()[0];
		 for(Object[] obj:reportInfos){
			 importRepCodes.add(String.valueOf( obj[1]));
		 }
		 for(RepDataQueryResultVO rep:repRequeryDataVOs){
			 if(rep.getPk_org().equals(importTask.getVo().getPk_org())){
				
				  
				 ReportCache repCache = IUFOCacheManager.getSingleton().getReportCache();
				 ReportVO reportVo =  repCache.getByPK(rep.getPk_report());
				 if(importRepCodes.contains(reportVo.getCode())){
					 dealMeasurePubdata(rep);
					 
					 
				     ICommitManageService commitSrv=NCLocator.getInstance().lookup(ICommitManageService.class);
			        commitSrv.addRepInputSate(rep.getPk_task(), rep.getAlone_id(), rep.getPk_report(),InvocationInfoProxy.getInstance().getUserId(), true,new UFDateTime());
				 
					 
					 rep.setInputstate(UFBoolean.TRUE);
//					 rep.setLastopeperson(InvocationInfoProxy.getInstance().getUserId());
						((BillManageModel) getModel()).directlyUpdate(rep);
				 }
				 
			 }
		 }
		
	}

	private SingleExcelImportTask getImportTask(IRepDataParam param ,ImpOrgAndReport vo, File file,TaskVO task, Set<ReportVO> reports) throws Exception {
		 
		Object[] objs = doGetImportInfos(param, file,reports);
		LoginEnvVO loginEnvVO = getLoginEnvVO();
	 	return new SingleExcelImportTask(param, objs, loginEnvVO, task, vo);
	}
	

	
	private Object[] doGetImportInfos(IRepDataParam param, File file,Set<ReportVO> reports) throws Exception {
		if (!file.exists()) {
			UfoPublic.showErrorDialog(loginContext.getEntranceUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0467"), NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0850"));
			return null;
		}

		Map<String, String> shetName2NumMap = UfoExcelImpUtil.getSheetNames(file.getPath());

		ChooseRepData[] chooseRepDatas = TableInputHandlerHelper.geneChooseRepDatas(reports.toArray(new ReportVO[0]));
//				..geneChooseRepDatas(reports.toArray(new ReportVO[0])));

		if ((chooseRepDatas == null) || (chooseRepDatas.length <= 0)) {
			JOptionPane.showMessageDialog(loginContext.getEntranceUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0852"));
			return null;
		}

		String strCurRepPK = param.getReportPK();

		Hashtable<String, Object> matchMap = null;
		String[] sheetNames = (String[]) shetName2NumMap.keySet().toArray(new String[0]);
		matchMap = ImportExcelDataBizUtil.doGetAutoMatchMap(chooseRepDatas, sheetNames, strCurRepPK);
		ImportExcelDataBizUtil.checkMatchMap(matchMap);

		List<Object[]> listImportInfos = importAllSheets(matchMap, file); //
		
		//����¼��
		
	 

		return new Object[] { listImportInfos, false, Integer.valueOf(1), file };
	}
	
	
	/**
	 * Ĭ�ϵ�������ҳǩ
	 * 
	 * @return
	 */
	private List<Object[]> importAllSheets(Hashtable<String, Object> matchMap, File file) throws Exception {
		List<Object[]> listImportInfos = new ArrayList<>();
		byte[] bytes = FileUtils.readFileToByteArray(file);
		Iterator<String> iterator = matchMap.keySet().iterator();
		while (iterator.hasNext()) {
			String sheetName = iterator.next(); // ҳǩ����
			Object[] selStrs = new Object[4];
			selStrs[0] = sheetName;
			selStrs[1] = ((String[]) matchMap.get(sheetName))[1];
			selStrs[2] = "-1";
			selStrs[3] = bytes;
			listImportInfos.add(selStrs);
		}

		return listImportInfos;
	}

	private  Collection<ImpOrgAndReport> getCanImport(Collection<ImpOrgAndReport> impReports, Set<String> canImportOrgPks) {
		
		List<ImpOrgAndReport> sameOrgs = new ArrayList<>();
		
		List<ImpOrgAndReport> defOrgs = new ArrayList<>();
		
		List<ImpOrgAndReport> notQueryOrgs = new ArrayList<>();
		
		
		OrgVO[] vos = OrgQueryUtil.queryOrgVOByPks(canImportOrgPks.toArray(new String[0]));

		for(ImpOrgAndReport iOrg: impReports){
			boolean find = false;
			for(OrgVO vo:vos){
				if(iOrg.getFileOrgName().equals(vo.getName())){
					find = true;
					iOrg.setPk_org(vo.getPk_org());
					iOrg.setDefOrgName(false);
					iOrg.setSysOrgName(vo.getName());
					break;
				}
			
			}
			if(find){
				sameOrgs.add(iOrg);
			}else{
				notQueryOrgs.add(iOrg);
			}
		}
		if(notQueryOrgs.size()>0){
			Collection<String> queryName = new ArrayList<>();
			for(ImpOrgAndReport iOrg: notQueryOrgs){
				queryName.add(iOrg.getFileOrgName());
			}
			List<Map<String, String>>  queryOrgs = this.sourceOrgCode(queryName);
			if(queryOrgs!=null&&queryOrgs.size()>0){
				for(ImpOrgAndReport iOrg: notQueryOrgs){
					boolean find = false;
					for(Map<String,String> map:queryOrgs){
//						 b.exsysval,b.bdname
						String  exsysval = map.get("exsysval").trim();
						String bdname = map.get("bdname").trim();
						if(iOrg.getFileOrgName().equals(exsysval)){
							for(OrgVO vo:vos){
								if(bdname.equals(vo.getName())){
									find = true;
									iOrg.setPk_org(vo.getPk_org());
									iOrg.setDefOrgName(true);
									iOrg.setSysOrgName(vo.getName());
									break;
								}
							}
							break;
						}
					}
					if(find){
						defOrgs.add(iOrg);
					}
				}
			}
	
		}
		if(defOrgs.size()>0){
			sameOrgs.addAll(defOrgs);
			return sameOrgs;
		}else{
			return sameOrgs;
		}
		
		
		 
	}

	private Collection<ImpOrgAndReport> getFiles() {
		
	 

		// #���ļ�ѡ���
		File file = null;
		Boolean isAutoCal = false;

		String title = ImpExpRepDataAuthBaseAction.getRepDataQuery() + "-"
				+ getBtnName();

		// TaskVO task =
		// IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(param.getTaskPK());

		ExcelFileChooserDlg dlg = new ExcelFileChooserDlg(
				loginContext.getEntranceUI());

		dlg.setTitle(title);

		dlg.getTaskLabel().setText(task.getCode() + " " + task.getChangeName());

		dlg.show();

		if (dlg.getResult() != UfoDialog.ID_OK)
			return null;
		// return new Object[]{null,isAutoCal,UfoDialog.ID_CANCEL};

		file = dlg.getSelectedFile();
		isAutoCal = dlg.isAutoCal();

		// #�û�ѡ����ļ�������
		if (!file.exists()) {
			UfoPublic.showErrorDialog(
					loginContext.getEntranceUI(),
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
							"1820001_0", "01820001-0467")/* @res "�ļ�������" */,
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
							"1820001_0", "01820001-0850")/* @res "������" */);// "�ļ�������","������"
			return null;
		}
		String outDir = System.getProperty("java.io.tmpdir");
/*		if (outDir.isEmpty()) {
			outDir = System.getProperty("java.io.tmpdir");
		} else {
			outDir = outDir + "/nclogs";
		}*/

		String passwd = "YouyonZQmima20190529";
				
				try {
					ZipFile respFile = new ZipFile(file);
					respFile.setPassword(passwd);
					respFile.extractAll(outDir);
					List<FileHeader> headerList = respFile.getFileHeaders();

					List<File> extractedFileList = new ArrayList<File>();

					for (FileHeader fileHeader : headerList) {
						if (!fileHeader.isDirectory()) {
							extractedFileList.add(new File(outDir, fileHeader
									.getFileName()));
						}
					}
					Collection<ImpOrgAndReport> rtns= new ArrayList<>();
					for(File unZipFile:extractedFileList){
						String name = unZipFile.getName();
						ImpOrgAndReport ir = new ImpOrgAndReport();
						ir.setFileOrgName(name.split("_")[0]);
						ir.setFile(unZipFile);
						rtns.add(ir);
					}
						 
					
						
						
//						for(String code: codeAndReport.keySet()){
//							if(name.startsWith(code+"_")){
//								name = name.replaceFirst(code+"_", "");
//								ImpOrgAndReport ir = new ImpOrgAndReport();
//								ir.setReport(codeAndReport.get(code));
//								if(name.contains("_")){
//									name = name.substring(0,	name.indexOf("_"));
//							}
//								ir.setRepName(name);
//								ir.setFile(unZipFile);
//								rtns.add(ir);
//								break;
//							}
//							
//						}
						 
//					}
					return rtns;
				} catch (ZipException e) {
					throw new BusinessRuntimeException("��ѹ�ļ�����");
					 
				}
			 
//		return new HashMap<String, File>();

	}

	public List<RepDataQueryResultVO> getRepDataQueryResultVO(
			List<RepDataQueryResultVO> rqr,Set<String> set) {
		
		if(rqr == null || set == null || set.size()<1 || rqr.size()<1) return null;
		List<RepDataQueryResultVO> newrqr = new ArrayList<RepDataQueryResultVO>();
		List<String> ls = new ArrayList<String>();
		for(String ll:set){
			ls.add(ll);
		}
		List<Map<String, String>> sl = queryOrgCode(ls);
		if(sl != null && sl.size()>0){
			for(Map<String, String> map:sl){
				for(String str:ls){
					if(str.equals(map.get("name"))){
						ls.remove(str);
					}
				}
			}
			for (int i = 0; i < rqr.size(); i++) {
				for (Map<String, String> map:sl) {
				
					if (map.get("pk_org").equals(rqr.get(i).getPk_org())) {
						newrqr.add(rqr.get(i));
						break;
					}

				}
			}
		}
		if(ls != null && ls.size()>0) {
			List<String> res = null; 
			List<Map<String, String>> resultListMap= sourceOrgCode(ls);
			if (resultListMap != null && resultListMap.size() > 0) {
				res = new ArrayList<String>();
				for(Map<String, String> map:resultListMap){
					res.add(map.get("bdname").trim());
				}
			}
			if(res == null){
				StringBuffer meg = new StringBuffer();
				meg.append("��������");
				for(String str:ls){
					meg.append(str).append(",");
				}
				meg.setLength(meg.length()-1);
				meg.append("�ڻ������ݶ��ձ�û�ж�Ӧ!");
				MessageDialog.showErrorDlg(getModel().getContext().getEntranceUI(), // ������ʾ
						"��ʾ", meg.toString());
				//ShowStatusBarMsgUtil.showStatusBarMsg(meg.toString(), super.getModel().getContext());
			}else{
				Set<String> ss = new HashSet<String>();
				for (int i = 0; i < rqr.size(); i++) {
					ss.add(rqr.get(i).getPk_org());
				}
				List<String> temp = new ArrayList<String>();
				List<Map<String, String>> orgcode =  OrgCode(ss);
				for(Map<String, String> map:orgcode){
					for(String str:res){
						if(str.equals(map.get("name"))){
							temp.add(map.get("pk_org"));
						}
					}
				}
				if(temp.size()<1){
					ShowStatusBarMsgUtil.showStatusBarMsg("����������֯��ϵͳ��֯û�ж�Ӧ����鿴�������ݶ��ձ�ڵ�", super.getModel().getContext());
				}
				
				for (String s : temp) {
					for (int i = 0; i < rqr.size(); i++) {
						if (s.equals(rqr.get(i).getPk_org())) {
							newrqr.add(rqr.get(i));
							break;
						}

					}
				}

			}
		}
		
		return newrqr;

	}
	
	public List<Map<String, String>> OrgCode(Set<String> ss){
		IUAPQueryBS service = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		StringBuffer where = new StringBuffer();
		where.append("select name,pk_org from org_orgs where pk_org in (");
		for(String str:ss){
			where.append("'").append(str).append("',");
		}
		where.setLength(where.length()-1);
		where.append(")");
		List<Map<String, String>> resultListMap;
		try {
			resultListMap = (List<Map<String, String>>) service.executeQuery(where.toString(), new MapListProcessor());
			if (!resultListMap.isEmpty() && resultListMap.size() > 0) {
				return resultListMap;
			}
		} catch (BusinessException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		return null;
		
	}
 
	
	public List<Map<String, String>> queryOrgCode(Collection<String> ls){
		if(ls.isEmpty()) return null;
		IUAPQueryBS service = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		StringBuffer where = new StringBuffer();
		where.append("select pk_org,name from org_orgs where name in (");
		for(String str:ls){
			where.append("'").append(str).append("',");
		}
		where.setLength(where.length()-1);
		where.append(")");
		try {
			List<Map<String, String>> resultListMap = (List<Map<String, String>>) service.executeQuery(where.toString(), new MapListProcessor());
			if (!resultListMap.isEmpty() && resultListMap.size() > 0) {
				return resultListMap;
			}
		} catch (BusinessException e) {
			
			throw new BusinessRuntimeException("��ѯ������մ���!");
		}
		return null;
	}

	public List<Map<String, String>> sourceOrgCode(Collection<String> ls) {
		IUAPQueryBS service = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		StringBuffer where = new StringBuffer();
		where.append(" select b.exsysval,b.bdname ");
		where.append(" from xx_bdcontra a, xx_bdcontra_b b, xx_exsystem c ");
		where.append(" where a.pk_contra = b.pk_contra ");
		where.append(" and a.exsystem = c.pk_exsystem ");
		where.append(" and c.exsystemcode = 'QYBB' ");
		where.append(" and b.exsysval in (");
		for(String str:ls){
			where.append("'").append(str).append("',");
		}
		where.setLength(where.length()-1);
		where.append(")");
		List<Map<String, String>> resultListMap;
		try {
			resultListMap = (List<Map<String, String>>) service.executeQuery(where.toString(), new MapListProcessor());
			if (!resultListMap.isEmpty() && resultListMap.size() > 0) {
				List<String> res = new ArrayList<String>();
				for(Map<String, String> map:resultListMap){
					res.add(map.get("exsysval"));
				}
				return resultListMap;
			}
		} catch (BusinessException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		

		return null;

	}

	@Override
	protected String getRepPK() {
		return ((RepDataQueryResultVO) getModel().getSelectedData())
				.getPk_report();
	}

	@Override
	protected String getOrgPK() {
		return ((RepDataQueryResultVO) getModel().getSelectedData())
				.getPk_org();
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
		return ((RepDataQueryResultVO) getModel().getSelectedData())
				.getPk_task();
	}

	/**
	 * 
	 * @create by wuyongc at 2011-8-6,����03:30:30
	 * 
	 * @param repRequeryDataVO
	 * @throws Exception
	 */
	private void dealMeasurePubdata(RepDataQueryResultVO repRequeryDataVO)
			throws Exception {
		if (!repRequeryDataVO.getInputstate().booleanValue()) {
			// �����δ¼�룬��ô��Ӧ��pubVO���ܴ��ڿ��ܲ����ڡ����Դ˴���Ҫ�ж�
			MeasurePubDataVO dbMeasurePubData = MeasurePubDataBO_Client
					.findByKeywords(repRequeryDataVO.getPubData());
			if (dbMeasurePubData == null) {
				MeasurePubDataBO_Client.createMeasurePubData(repRequeryDataVO
						.getPubData());
			}
		}
	}

	private LoginEnvVO getLoginEnvVO() {
		LoginEnvVO loginEnv = new LoginEnvVO();

		loginEnv.setCurLoginDate(WorkbenchEnvironment.getServerTime()
				.toStdString());
		loginEnv.setDataExplore(true);
		loginEnv.setDataSource(IUFOIndividualSettingUtil
				.getDefaultDataSourceVo());
		loginEnv.setLangCode(WorkbenchEnvironment.getLangCode());
		loginEnv.setLoginUnit(nodeEnv.getCurrOrg());
		loginEnv.setRmsPK(nodeEnv.getCurrMngStuc());
		return loginEnv;
	}

	@Override
	public void setModel(AbstractUIAppModel model) {
		super.setModel(model);
		((DefaultExceptionHanler) exceptionHandler).setContext(model
				.getContext());
		((DefaultExceptionHanler) exceptionHandler)
				.setErrormsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
						.getStrByID("1820001_0", "01820001-0442")/* @res "����ʧ�ܣ�" */);
	}

	/**
	 * @param loginContext
	 *            the loginContext to set
	 */
	public void setLoginContext(LoginContext loginContext) {
		if (loginContext.getEntranceUI() instanceof AbsSwitchToftPanelAdaptor)
			nodeEnv = ((AbsSwitchToftPanelAdaptor) loginContext.getEntranceUI())
					.getNodeEnv();
		this.loginContext = loginContext;
	}

	/**
	 * �õ�����Excel��׼��������Ϣ(CellsModel)
	 * 
	 * @param container
	 * @param ufoReport
	 * @return
	 */
	public File getFile() {

		// #���ļ�ѡ���
		File file = null;
		Boolean isAutoCal = false;

		String title = ImpExpRepDataAuthBaseAction.getRepDataQuery() + "-"
				+ getBtnName();

		// TaskVO task =
		// IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(param.getTaskPK());

		ExcelFileChooserDlg dlg = new ExcelFileChooserDlg(
				loginContext.getEntranceUI());

		dlg.setTitle(title);

		dlg.getTaskLabel().setText(task.getCode() + " " + task.getChangeName());

		dlg.show();

		if (dlg.getResult() != UfoDialog.ID_OK)
			return null;
		// return new Object[]{null,isAutoCal,UfoDialog.ID_CANCEL};

		file = dlg.getSelectedFile();
		isAutoCal = dlg.isAutoCal();

		// #�û�ѡ����ļ�������
		if (!file.exists()) {
			UfoPublic.showErrorDialog(
					loginContext.getEntranceUI(),
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
							"1820001_0", "01820001-0467")/* @res "�ļ�������" */,
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
							"1820001_0", "01820001-0850")/* @res "������" */);// "�ļ�������","������"
			return null;
		}
		String outDir = System.getProperty("java.io.tmpdir");
/*		if (outDir.isEmpty()) {
			outDir = System.getProperty("java.io.tmpdir");
		} else {
			outDir = outDir + "/nclogs";
		}*/

		file = unzip(file, outDir, "YouyonZQmima20190529");

		return file;

	}

	private Object[] doGetImportInfos(IRepDataParam param, File file1,
			String org) {

		// #���ļ�ѡ���
		File file = file1;
		Boolean isAutoCal = false;
		try {

			// file = getFileDlg();
			final Map<String, String> shetName2NumMap = UfoExcelImpUtil
					.getSheetNames(file.getPath());
			// �õ���ѡ�񱨱�����
			ChooseRepData[] chooseRepDatas = doGetChooseRepDatas(param);
			if (chooseRepDatas == null || chooseRepDatas.length <= 0) {
				JOptionPane.showMessageDialog(
						loginContext.getEntranceUI(),
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
								"1820001_0", "01820001-0852")/*
															 * @res
															 * "���������û�п�ѡ��ı���"
															 */);// "���������û�п�ѡ��ı���"
				return null;
			}

			String strCurRepPK = param.getReportPK();
			// #�õ��Զ�ƥ����Ϣ�����
			Hashtable<String, Object> matchMap = null;
			try {
				String[] sheetNames = shetName2NumMap.keySet().toArray(
						new String[0]);
				matchMap = ImportExcelDataBizUtil.doGetAutoMatchMap(
						chooseRepDatas, sheetNames, strCurRepPK);
				// ���ƥ����Ϣ
				ImportExcelDataBizUtil.checkMatchMap(matchMap);
			} catch (TableInputException e) {
				AppDebug.debug(e);// @devTools e.printStackTrace(System.out);
				JOptionPane.showMessageDialog(loginContext.getEntranceUI(),
						e.getMessage());
				return null;
			}

			List<Object[]> listImportInfos = null;
			List<String[]> array = null;
			if (matchMap.size() == 1) {
				// ƥ������һ�ű�ҳ,����ǿ�ֱ�ӵ���ı�����׼����array
				ChooseRepData chooseRepData = ImportExcelDataBizUtil
						.getCurRepData(chooseRepDatas, strCurRepPK);
				if (chooseRepData == null) {
					return null;
				}
				// �Ƿ��ǿ�ֱ��¼��ı���
				String[] selStrs = new String[4];
				String sheetName = matchMap.keySet().iterator().next();
				selStrs[0] = sheetName;

				selStrs[3] = shetName2NumMap.get(sheetName);
				selStrs[1] = ((String[]) matchMap.get(sheetName))[1];

				if (chooseRepData.isCanImportDirected()) {
					if (selStrs[0] != null
							&& selStrs[0].trim().equalsIgnoreCase("null") == false
							&& selStrs[1] != null
							&& selStrs[1].trim().equalsIgnoreCase("null") == false) {
						selStrs[2] = "-1";
						array = new ArrayList<String[]>();
						array.add(selStrs);
					}
				} 
				else {// ��ʾ�û����붯̬������չ����
//					InputDynEndRowDialog inputDynEndRowDialog = new InputDynEndRowDialog(
//							loginContext.getEntranceUI());
//					inputDynEndRowDialog.show();
//					if (inputDynEndRowDialog.getSelectOption() != JOptionPane.OK_OPTION) {
						selStrs[2] = "-1";
//					} else {
//						selStrs[2] = inputDynEndRowDialog.getText();
//					}
					array = new ArrayList<String[]>();
					array.add(selStrs);
				}
			} else {
				// ����ƥ����棬�õ�ƥ�����󣬼��㵼��Excel��׼��������Ϣ(CellsModel)
				// ImportExcelDataSettingDlg setDlg = new
				// ImportExcelDataSettingDlg(loginContext.getEntranceUI(),chooseRepDatas,matchMap);

				array = getExcelData(matchMap, chooseRepDatas, org);
				for (String[] strings : array) {
					strings[3] = shetName2NumMap.get(strings[0]);
				}

			}

//			RepDataQueryResultVO repRequeryDataVO = (RepDataQueryResultVO) getModel()
//					.getSelectedData();
//
//			IContext ctx = null;
//			try {
//				ctx = createContext(repRequeryDataVO);
//			} catch (IllegalArgumentException e) {
//				AppDebug.debug(e);
//			} catch (IllegalAccessException e) {
//				AppDebug.debug(e);
//			}
			// �õ�����Excel��׼��������Ϣ(CellsModel)
			// listImportInfos = ImportExcelDataBizUtil.getImportInfos(array,
			// workBook,ctx);
			return new Object[] { array, isAutoCal, UfoDialog.ID_OK, file };
		} catch (FileNotFoundException e1) {

			AppDebug.debug(e1);
		} catch (IOException e1) {
			AppDebug.debug(e1);
		}
		return null;
	}

	/**
	 * ʹ�ø��������ѹָ��ѹ���ļ���ָ��Ŀ¼
	 * 
	 * @param inFile
	 *            ָ��Zip�ļ�
	 * @param outDir
	 *            ��ѹĿ¼
	 * @param passwd
	 *            ��ѹ����
	 * @return
	 */
	public File unzip(File inFile, String outDir, String passwd) {

		try {
			ZipFile respFile = new ZipFile(inFile);
			respFile.setPassword(passwd);
			respFile.extractAll(outDir);
			List<FileHeader> headerList = respFile.getFileHeaders();

			List<File> extractedFileList = new ArrayList<File>();

			for (FileHeader fileHeader : headerList) {
				if (!fileHeader.isDirectory()) {
					extractedFileList.add(new File(outDir, fileHeader
							.getFileName()));
				}
			}
			return extractedFileList.get(0);
		} catch (ZipException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		return inFile;

	}

	private IContext createContext(RepDataQueryResultVO result)
			throws IllegalArgumentException, IllegalAccessException {
		IContext context = new Context();
		String pk_org = nodeEnv.getCurrOrg();
		context.setAttribute(IUfoContextKey.CUR_REPORG_PK, pk_org);
		context.setAttribute(BDContextKey.CUR_USER_ID,
				UserUtil.getCurrentUser());
		context.setAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK, getRmsPK());
		context.setAttribute(IUfoContextKey.PERSPECTIVE_ID,
				IUfoContextKey.PERS_DATA_INPUT);
		// TODO δ��������Ȩ���ж�
		context.setAttribute(IUfoContextKey.DATA_RIGHT,
				IUfoContextKey.RIGHT_DATA_WRITE);
		context.setAttribute(IUfoContextKey.LOGIN_DATE, DateUtil.getCurDay());
		context.setAttribute(IUfoContextKey.OPERATION_STATE,
				IUfoContextKey.OPERATION_INPUT);
		context.setAttribute(IUfoContextKey.REPORT_PK, result.getPk_report());
		context.setAttribute(IUfoContextKey.ALONE_ID, result.getAlone_id());
		context.setAttribute(IUfoContextKey.MEASURE_PUB_DATA_VO,
				result.getPubData());
		// @edit by wuyongc at 2011-6-7,����08:58:42 ����ѡ�еĲ�ѯ���������������ж�λ
		context.setAttribute(IUfoContextKey.TASK_PK, result.getPk_task());
		// context.setAttribute(IUfoContextKey.TASK_PK,
		// getQueryCondVo().getTaskVo().getPk_task());
		ReportVO repVo = IUFOCacheManager.getSingleton().getReportCache()
				.getByPK(result.getPk_report());
		context.setAttribute(IUfoContextKey.KEYGROUP_PK, repVo.getPk_key_comb());
		// liuchun add @20111124 �������м�������Դ��Ϣ
		context.setAttribute(IUfoContextKey.DATA_SOURCE,
				UfobIndividualSettingUtil.getDefaultDataSourceVo());

		return context;
	}

	public static ChooseRepData[] doGetChooseRepDatas(IRepDataParam param) {
		try {
			ChooseRepData[] chooseRepDatas = null;

			chooseRepDatas = (ChooseRepData[]) ActionHandler.execWithZip(
					IUfoTableInputActionHandler.class.getName(),
					"loadTableImportReps", param);

			// if (chooseRepDatas!=null && editor.getPubData()!=null &&
			// editor.getPubData().getVer()==HBBBSysParaUtil.VER_HBBB){
			// chooseRepDatas = filterRepDatas(chooseRepDatas);
			// }
			return chooseRepDatas;
		} catch (Exception e) {
			AppDebug.debug(e);
			return null;
		}
	}

	private static ChooseRepData[] filterRepDatas(ChooseRepData[] chooseRepDatas) {
		ArrayList<ChooseRepData> lstRepDatas = new ArrayList<ChooseRepData>();
		ReportCache repCache = UFOCacheManager.getSingleton().getReportCache();
		for (ChooseRepData chooseRepData : chooseRepDatas) {
			String reportPK = chooseRepData.getReportPK();
			if (!repCache.getByPK(reportPK).getIsintrade().booleanValue())
				lstRepDatas.add(chooseRepData);
		}

		ChooseRepData[] returnObj = null;
		if (lstRepDatas.size() > 0) {
			returnObj = new ChooseRepData[lstRepDatas.size()];
			returnObj = lstRepDatas.toArray(returnObj);
		}
		return returnObj;

	}

	@Override
	protected boolean isActionEnable() {
		return getModel().getSelectedData() != null;
		// if (getModel().getSelectedData()==null)
		// return false;
		// RepDataQueryResultVO repRequeryDataVO = (RepDataQueryResultVO)
		// getModel().getSelectedData();
		// return repRequeryDataVO.getDataorigin()==null;
	}

	/**
	 * @return the queryExecutor
	 */
	public IUfoQueryExecutor getQueryExecutor() {
		return queryExecutor;
	}

	/**
	 * @param queryExecutor
	 *            the queryExecutor to set
	 */
	public void setQueryExecutor(IUfoQueryExecutor queryExecutor) {
		this.queryExecutor = queryExecutor;
	}

	private List<String[]> getExcelData(Map<String, Object> matchMap,
			ChooseRepData[] chooseRepDatas, String org) {
		// ͨ��matchMap���õ�ImportExcelTableRow[]
		ImportExcelTableRow[] importExcelTableRows = null;
		if (matchMap != null && matchMap.size() > 0) {
			// �����б���
			String[] mapKeys = new String[matchMap.size()];
			Set<String> keySet = matchMap.keySet();
			keySet.toArray(mapKeys);
			importExcelTableRows = new ImportExcelTableRow[mapKeys.length];
			for (int i = 0; i < importExcelTableRows.length; i++) {
				// sheetname=repcode
				String repCode = matchMap.get(mapKeys[i]).getClass().isArray() ? ((String[]) matchMap
						.get(mapKeys[i]))[1] : null;
				ChooseRepData chooseRepData = ImportExcelDataBizUtil
						.getCurRepDataByCode(chooseRepDatas, repCode);
				importExcelTableRows[i] = new ImportExcelTableRow(
						chooseRepData, mapKeys[i]);
			}
		} else {
			importExcelTableRows = new ImportExcelTableRow[0];
		}

		// �Ե�������ݽ�������,���ձ����������
		Arrays.sort(importExcelTableRows,
				new Comparator<ImportExcelTableRow>() {
					@Override
					public int compare(ImportExcelTableRow o1,
							ImportExcelTableRow o2) {
						if (o1.getRepCode() != null && o2.getRepCode() != null)
							return o1.getRepCode().compareTo(o2.getRepCode());
						else
							return o1.getSheetName().compareTo(
									o2.getSheetName());
					}
				});

		List m_listResultArray = new ArrayList<String[]>();
		for (int j = 0; j < importExcelTableRows.length; j++) {
			String[] strInfos = new String[4];
			// if(org.equals(arg0))
			strInfos[0] = importExcelTableRows[j].getSheetName();
			strInfos[1] = importExcelTableRows[j].getRepCode();
			strInfos[2] = importExcelTableRows[j].getDynAreaEndRow();

			if (strInfos[1] != null)
				m_listResultArray.add(strInfos);
		}
		return m_listResultArray;
	}

	private Set<String> readExcel(File file) throws Exception {
		// ��������������ȡExcel
		Set<String> danwei = new HashSet<String>();
//		Map<String,List<List<String>>> allData= new HashMap<String,List<List<String>>>();
		Map<String,List<String>> sheetA1 = new HashMap<String,List<String>>();
		Map<String,List<String>> sheetB1 = new HashMap<String,List<String>>();
		InputStream is = new FileInputStream(file.getAbsolutePath()); // jxl�ṩ��Workbook��
		Workbook wb = Workbook.getWorkbook(is); // ֻ��һ��sheet,ֱ�Ӵ��� //����һ��Sheet����
		Sheet[] sheets = wb.getSheets();
		for(Sheet sheet:sheets){
			int rows = sheet.getRows(); // ���е�����
//			String company = null;
//			List<List<String>> sheetData = new ArrayList<List<String>>(); // Խ����һ��
			List<String> A1 = new ArrayList<String>();	
			List<String> B1 = new ArrayList<String>();// ����������
			for (int j = 1; j < rows; j++) {
				List<String> oneData = new ArrayList<String>(); // �õ�ÿһ�еĵ�Ԫ�������
				Cell[] cells = sheet.getRow(j);
				
				for (int k = 0; k < 2; k++) {
					oneData.add(cells[k].getContents().trim());
					if(cells[k].getContents().trim().compareTo("��λ")>0 && cells[k].getContents().trim().indexOf("��λ")==0){
						if(cells[k].getContents().trim().length()>3)
							danwei.add( cells[k].getContents().trim().substring(3, cells[k].getContents().trim().length()));
					}
					if(k==0){
						A1.add(cells[k].getContents().trim());
					}else{
						B1.add(cells[k].getContents().trim());
					}
					
				} // �洢ÿһ������
//				sheetData.add(oneData); // ��ӡ��ÿһ������ //
				//System.out.println(oneData);
			}
//			if(company != null)
//			allData.put(company==null?sheet.getName():company, sheetData);
			sheetA1.put(sheet.getName(), A1);
			sheetB1.put(sheet.getName(), B1);
		}
		updateExcel(file, sheetA1,sheetB1);
		return danwei;
	}
	
	public void updateExcel(File file,Map<String,List<String>> sheetA1,Map<String,List<String>> sheetB1){
		int index = -1;
		if(sheetA1 != null||sheetB1!=null){
			Set<String> excelOrgName = new HashSet<String>();
			for(String key:sheetA1.keySet()){
				List<String> A1 = sheetA1.get(key);
				Boolean temp = false;
				for(int i=0;i<A1.size();i++){
					if(temp && A1.get(i) != null && !"�ϼ�".equals(A1.get(i)) && !"".equals(A1.get(i))){
						excelOrgName.add(A1.get(i));
						
					}
//					if(A1.get(i).compareTo("�Է���λ")==0 && "�Է���λ".equals(A1.get(i))){
					if(ArrayUtils.contains(ZCUseConstant.FILTER_COLUMN, A1.get(i))){
						temp=true;
						index=0;
					}
					
				}
			}
			
			for(String key:sheetB1.keySet()){
				List<String> B1 = sheetB1.get(key);
				Boolean temp = false;
				for(int i=0;i<B1.size();i++){
					if(temp && B1.get(i) != null && !"�ϼ�".equals(B1.get(i)) && !"".equals(B1.get(i))){
						excelOrgName.add(B1.get(i));
					}
//					if(B1.get(i).compareTo("�Է���λ")==0 && "�Է���λ".equals(B1.get(i))){
					if(ArrayUtils.contains(ZCUseConstant.FILTER_COLUMN, B1.get(i))){
						temp=true;
						index=1;
					}
					
				}
			}
			
			StringBuffer where = new StringBuffer();
			where.append("select name,pk_org from org_orgs where name in (");
			for(String str:excelOrgName){
				where.append("'").append(str).append("',");
			}
			where.append("'1')");
			IUAPQueryBS service = NCLocator.getInstance().lookup(IUAPQueryBS.class);
			List<Map<String, String>> resultListMap;
			try {
				resultListMap = (List<Map<String, String>>) service.executeQuery(where.toString(), new MapListProcessor());
				Set<String> simeSet = new HashSet<>();
				if (!resultListMap.isEmpty() && resultListMap.size() > 0) {
					for(Map<String, String> map:resultListMap){
						for (String str : excelOrgName) {
							if(str.equals(map.get("name"))){
								simeSet.add(str);
							
							}
						}
					}
				}
				if(simeSet.size()>0){
					excelOrgName.removeAll(simeSet);
				}
				if(excelOrgName != null && excelOrgName.size()>0){
					List<String> ls = new ArrayList<String>();
					for(String str:excelOrgName){
						ls.add(str);
					}
					
					List<Map<String, String>> ss =  sourceOrgCode(ls);
					readExcel1(file, ss,index);
				}
			} catch (BusinessException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	 private void readExcel1(File file,List<Map<String, String>> ss, int cont){
		 if(ss==null) return ;
         try {
             jxl.Workbook wb =null;                                 //����һ��workbook����
             String excelpath = file.getAbsolutePath();
             
             InputStream is = new FileInputStream(excelpath);      //����һ���ļ���������Excel�ļ�
             wb = Workbook.getWorkbook(is);                        //���ļ���д�뵽workbook����
                             
             //jxl.Workbook ������ֻ���ģ��������Ҫ�޸�Excel����Ҫ����һ���ɶ��ĸ���������ָ��ԭExcel�ļ�
             jxl.write.WritableWorkbook wbe= Workbook.createWorkbook(new File(excelpath), wb);//����workbook�ĸ���
             int sheet_size=wbe.getNumberOfSheets();
             
             
             for (int index = 0; index < sheet_size; index++) {
                 // ÿ��ҳǩ����һ��Sheet����
                 WritableSheet sheet  = wbe.getSheet(index);        //��ȡsheet
                 // sheet.getColumns()���ظ�ҳ��������
                 int column_total = sheet.getRows()/*getColumns()*/;
                 for (int j = 0; j < column_total; j++) {
                     String cellinfo = sheet.getCell(cont, j).getContents();
                     WritableCell cell =sheet.getWritableCell(cont, j); //��ȡ��һ�е����е�Ԫ��
                     jxl.format.CellFormat cf = cell.getCellFormat();//��ȡ��һ����Ԫ��ĸ�ʽ
                     for(Map<String, String> map:ss){
                    	 if(map.get("exsysval").equals(cellinfo)){
                    		 jxl.write.Label lbl = new jxl.write.Label(cont, j,map.get("bdname"));//�޸����ֵ
                             lbl.setCellFormat(cf); //���޸ĺ�ĵ�Ԫ��ĸ�ʽ�趨�ɸ�ԭ��һ��
                             sheet.addCell(lbl);                             //���Ĺ��ĵ�Ԫ�񱣴浽sheet
                             break;
                    	 }
                     }
                 }
             }
             wbe.write();                                            //���޸ı��浽workbook
             wbe.close();                                            //�ر�workbook���ͷ��ڴ� 
             is.close();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (WriteException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (BiffException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}finally {
			
		}
     }
	

}
