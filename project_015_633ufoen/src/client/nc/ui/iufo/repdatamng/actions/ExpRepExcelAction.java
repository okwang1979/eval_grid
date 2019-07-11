 package nc.ui.iufo.repdatamng.actions;
 
 import com.ufida.iufo.constant.output.IOutputMsgConstant;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.report.sysplugin.print.FreeReportPrintStatusMng;
import com.ufida.zior.console.ActionHandler;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.func.excel.text.ImpExpFileNameUtil;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.repdatainput.ufoe.IUfoTableInputActionHandler;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.iuforeport.tableinput.applet.RepDataParam;
import com.ufsoft.report.util.UfoPublic;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.KeyStroke;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.impl.iufo.utils.MultiLangTextUtil;
import nc.impl.iufo.utils.NCLangUtil;
import nc.itf.iufo.individual.IUFOIndividualSettingUtil;
import nc.itf.iufo.task.ITaskQueryService;
import nc.login.vo.NCSession;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.ui.iufo.NodeEnv;
import nc.ui.iufo.dataexchange.FilePackage;
import nc.ui.iufo.input.funclet.AbsSwitchToftPanelAdaptor;
import nc.ui.iufo.query.common.model.IUfoBillManageModel;
import nc.ui.iufo.repdataauth.actions.RepDataAuthViewBaseAction;
import nc.ui.iufo.repdatamng.view.ExpRepExcelDlg;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.uif2.DefaultExceptionHanler;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.model.AbstractUIAppModel;
import nc.ui.uif2.model.BillManageModel;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.constant.CommonCharConstant;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.KeyDetailDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepExpParam;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.query.IUfoQueryInitParam;
import nc.vo.iufo.query.IUfoQueryLoginContext;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iufo.task.TaskInfoVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.org.GroupVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.uif2.LoginContext;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
 
 
 
 
 
 
 
 
 
 
 
 
 
 public class ExpRepExcelAction
   extends RepDataAuthViewBaseAction
   implements IUfoContextKey
 {
   private static final long serialVersionUID = -8985488630319954988L;
   private NodeEnv nodeEnv = null;
   
   private final String EXP_REP_EXCEL = ImpExpRepDataAuthBaseAction.REP_DATA_QUERY + "-" + NCLangUtil.getStrByID("1820001_0", "01820001-1451");
   
   public ExpRepExcelAction() {
     setBtnName(NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0845"));
     setCode("RepExportExcel");
     putValue("AcceleratorKey", KeyStroke.getKeyStroke(69, 2));
     this.exceptionHandler = new DefaultExceptionHanler();
   }
   
 
   private void setDataSource(UfoContextVO context)
   {
     DataSourceVO dataSource = new DataSourceVO();
     NCSession session = WorkbenchEnvironment.getInstance().getSession();
     dataSource.setDs_addr(session.getDsName());
     dataSource.setAccount_name(session.getBusiCenterName());
     dataSource.setDs_type(Integer.valueOf(3));
     context.setAttribute("key_DATA_SOURCE_IUFO", dataSource);
   }
   
   private LoginEnvVO getLoginEnvVO() { LoginEnvVO loginEnv = new LoginEnvVO();
     
     loginEnv.setCurLoginDate(WorkbenchEnvironment.getServerTime().toStdString());
     loginEnv.setDataExplore(true);
     loginEnv.setDataSource(IUFOIndividualSettingUtil.getDefaultDataSourceVo());
     loginEnv.setLangCode(WorkbenchEnvironment.getLangCode());
     loginEnv.setLoginUnit(this.nodeEnv.getCurrOrg());
     loginEnv.setRmsPK(this.nodeEnv.getCurrMngStuc());
     return loginEnv;
   }
   
 
 
   public void doAction(ActionEvent e)
     throws Exception
   {
     if (!FreeReportPrintStatusMng.CheckIfHaveLicense()) {
       ShowStatusBarMsgUtil.showErrorMsg(NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0844"), IOutputMsgConstant.CANNOT_EXP_NO_LICENSE, getModel().getContext());
       
       return;
     }
     super.doAction(e);
     Object[] objs = ((BillManageModel)getModel()).getSelectedOperaDatas();
     RepDataQueryResultVO[] repQryResults = new RepDataQueryResultVO[objs.length];
     System.arraycopy(objs, 0, repQryResults, 0, objs.length);
     
     RepDataQueryResultVO repQryResult = (RepDataQueryResultVO)getModel().getSelectedData();
     
     String taskPK = getTaskPK();
     
 
 
     Object[] retObjs = TaskSrvUtils.getTaskQueryService().getTaskInfoAndBalConds(taskPK, true);
     
     TaskInfoVO taskInfo = (TaskInfoVO)retObjs[0];
     BalanceCondVO[] balConds = (BalanceCondVO[])retObjs[1];
     IUfoQueryInitParam queryparam = ((IUfoQueryLoginContext)getModel().getContext()).getInitParam();
     
     KeyVO[] keys = queryparam.getKeyGroup().getKeys();
     TaskVO task = taskInfo.getTaskVO();
     ExpRepExcelDlg dlg = new ExpRepExcelDlg(getModel().getContext().getEntranceUI(), this.EXP_REP_EXCEL, task, queryparam.getMainOrgPK());
     
 
     dlg.setTitle(this.EXP_REP_EXCEL);
     RepDataQueryResultVO repDataQryResult = (RepDataQueryResultVO)getModel().getSelectedData();
     
 
     dlg.setBalConds(balConds);
     MeasurePubDataVO pubData = repDataQryResult.getPubData();
     String[] keyVals = pubData.getKeywords();
     dlg.setKeyGroupCheckBoxPanel(keys);
     
     StringBuilder keywordGroupValue = new StringBuilder();
     String orgName = MultiLangTextUtil.getCurLangText(((IUfoBillManageModel)getModel()).getOrgPkMap().get(repQryResult.getPk_org()));
     
 
     keywordGroupValue.append(orgName);
     
     String strAccSchemePK = task.getPk_accscheme();
     IKeyDetailData keyDetailData = null;
     for (int i = 1; i < keys.length; i++)
     {
       keyDetailData = KeyDetailDataUtil.getKeyDetailData(keys[i], keyVals[i], strAccSchemePK);
       
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
     if (reps.length == 1) {
       title1 = reps[0].getCode() + "_" + reps[0].getChangeName();
       title2 = title1;
     } else {
       title1 = reps[0].getCode() + "_" + reps[0].getChangeName();
       title2 = keywordGroupValue.toString();
     }
     dlg.setDefaultFileName(new String[] { title1, title2 });
     
     dlg.showModal();
     
     if (dlg.getResult() == 1) {
       RepExpParam expParam = dlg.getRepExpParam();
       
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
       File oneFile = new File(expParam.getFilePath());
       File ff = oneFile;
       if ((expParam.isSaveAll2OneFile()) && 
         (ff.exists())) {
         int iRet = UfoPublic.showConfirmDialog(getModel().getContext().getEntranceUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0842") + expParam.getFilePath() + NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0846"), NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0133"), 0);
         
 
 
 
 
 
 
 
 
 
 
 
 
 
         if (iRet != 0) {
           return;
         }
       }
       
       String extendName = ImpExpFileNameUtil.isExcel2007(expParam.getFilePath()) ? "xlsx" : "xls";
       FileOutputStream stream = null;
       try {
         boolean bSuc = false;
         Map<String, byte[]> map = (Map)ActionHandler.execWithZip(IUfoTableInputActionHandler.class.getName(), "exportRepData2Excel", new Object[] { getLoginEnvVO(), expParam, keys, strAccSchemePK, getRmsPK(), getMainOrgPK() });
         
         List<File> fileList = new ArrayList();
         String parentPath = null;
         if (expParam.isSaveAll2OneFile()) {
           stream = new FileOutputStream(expParam.getFilePath());
           Collection<byte[]> c = map.values();
           if ((c != null) && (c.size() == 1)) {
             stream.write((byte[])c.iterator().next());
             stream.flush();
             stream.close();
             bSuc = true;
             fileList.add(oneFile);
             parentPath = oneFile.getParent();
           }
         } else {
           Set<Map.Entry<String, byte[]>> set = map.entrySet();
           for (Map.Entry<String, byte[]> entry : set) {
             if (oneFile.isDirectory()) {
               parentPath = oneFile.getPath();
             }
             else {
               String path = oneFile.getPath();
               int lastFileSeparator = path.lastIndexOf(File.separator);
               String fileName = path.substring(lastFileSeparator + 1);
               int lastPointIndex = fileName.lastIndexOf(".");
               if ((lastPointIndex > 0) && (lastPointIndex < fileName.length() - 1)) {
                 parentPath = oneFile.getParent();
               } else {
                 parentPath = oneFile.getPath();
               }
             }
             File parentFilePath = new File(parentPath);
             if (!parentFilePath.exists()) {
               boolean mkdir = parentFilePath.mkdirs();
               if (!mkdir) {
                 throw new RuntimeException("Make dir fail!");
               }
             }
             
             String file = parentPath + File.separator + (String)entry.getKey();
             File f = new File(file);
             
 
             if (f.exists()) {
               int iRet = UfoPublic.showConfirmDialog(getModel().getContext().getEntranceUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0842") + expParam.getFilePath() + NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0846"), NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0133"), 0);
               
 
 
 
 
 
 
 
 
 
 
 
 
 
               if (iRet != 0) {}
             }
             else
             {
               stream = new FileOutputStream(f);
               stream.write((byte[])entry.getValue());
               stream.flush();
               stream.close();
               bSuc = true;
               fileList.add(f);
             }
           } }
         if (expParam.isbZip()) {
           FilePackage pack = new FilePackage();
           int fileSize = fileList.size();
           if (fileSize > 0) {
             String firstName = ((File)fileList.get(0)).getName();
             String zipFileName = firstName.replace("." + extendName, "(" + fileSize + ").zip");
             String zipFile = parentPath + File.separator + zipFileName;
             pack.zipFile((File[])fileList.toArray(new File[0]), zipFile);
             for (File file : fileList) {
               file.delete();
             }
           }
         }
 		//begin pzm 利用压缩文件加密execl 20190617
 		FilePackage pack = new FilePackage();
 		int fileSize = fileList.size();
 		if(fileSize>0){
 			String firstName = fileList.get(0).getName();
 			String zipFileName = firstName.replace((CommonCharConstant.POINT + extendName),"(" + fileSize +").zip");
 			String zipFile = parentPath + File.separator + zipFileName;
 			File zipF= new File(zipFile);
 			StringBuffer msg = new StringBuffer();
 			msg.append("导出文件：");
 			for (File file : fileList) {
 				msg.append(file.getName()).append("/n");
 				encryZipFile(zipF, file, "YouyonZQmima20190529");
 				file.delete();
 			}
 			msg.append("成功");
 			MessageDialog.showErrorDlg(getModel().getContext().getEntranceUI(), // 弹框提示
 					"提示", msg.toString());
 		}
 		//end
         if (bSuc) {
           ShowStatusBarMsgUtil.showStatusBarMsg(NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820002-0030"), getModel().getContext());
         }
       }
       catch (Throwable tte)
       {
         AppDebug.debug(e);
         
         ShowStatusBarMsgUtil.showErrorMsg(NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0608"), tte.getMessage(), getModel().getContext());
 
 
       }
       
 
 
 
     }
     else if (dlg.getResult() == 2) {
       ShowStatusBarMsgUtil.showStatusBarMsg(NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820002-0031"), getModel().getContext());
       
       return;
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
 
 
 
 
 
 
 
 
 
   private Map<String, List<RepDataQueryResultVO>> getGroupRepDataResult(RepDataQueryResultVO[] repQryResults)
   {
     Map<String, List<RepDataQueryResultVO>> orgResultMap = new LinkedHashMap();
     for (RepDataQueryResultVO rs : repQryResults) {
       if (orgResultMap.containsKey(rs.getPk_org())) {
         ((List)orgResultMap.get(rs.getPk_org())).add(rs);
       } else {
         List<RepDataQueryResultVO> repList = new ArrayList();
         repList.add(rs);
         orgResultMap.put(rs.getPk_org(), repList);
       }
     }
     return orgResultMap;
   }
   
 
 
 
 
 
   private UfoContextVO getContextVO(RepDataQueryResultVO repRequeryDataVO)
   {
     UfoContextVO context = new UfoContextVO();
     setDataSource(context);
     String pk_org = getModel().getContext().getPk_org();
     String pk_group = getModel().getContext().getPk_group();
     context.setAttribute("key_CUR_GROUP_PK", pk_group);
     context.setAttribute("CurrentReportOrg", pk_org);
     context.setAttribute("key_REPORT_PK", repRequeryDataVO.getPk_report());
     context.setAttribute("key_KEYGROUP_PK", repRequeryDataVO.getPubData().getKeyGroup().getKeyGroupPK());
     
     ReportVO rep = IUFOCacheManager.getSingleton().getReportCache().getByPK(repRequeryDataVO.getPk_report());
     
     context.setAttribute("key_REPORT_NAME", rep.getChangeName());
     context.setAttribute("key_MEASURE_PUB_DATA_VO", repRequeryDataVO.getPubData());
     return context;
   }
   
 
 
 
 
 
   private IRepDataParam getRepDataParam(RepDataQueryResultVO repRequeryDataVO)
   {
     IRepDataParam param = new RepDataParam();
     param.setAloneID(repRequeryDataVO.getAlone_id());
     param.setReportPK(repRequeryDataVO.getPk_report());
     param.setOperType("repdata_input");
     param.setTaskPK(repRequeryDataVO.getPk_task());
     param.setRepMngStructPK(this.nodeEnv.getCurrMngStuc());
     param.setRepOrgPK(this.nodeEnv.getCurrOrg());
     param.setCurGroupPK(WorkbenchEnvironment.getInstance().getGroupVO().getPk_group());
     
     param.setPubData(repRequeryDataVO.getPubData());
     return param;
   }
   
   protected String getRepPK()
   {
     return ((RepDataQueryResultVO)getModel().getSelectedData()).getPk_report();
   }
   
   protected String getOrgPK()
   {
     return ((RepDataQueryResultVO)getModel().getSelectedData()).getPk_org();
   }
   
   protected String getRmsPK()
   {
     return this.nodeEnv.getCurrMngStuc();
   }
   
   protected String getMainOrgPK()
   {
     return this.nodeEnv.getCurrOrg();
   }
   
   protected String getTaskPK()
   {
     return ((RepDataQueryResultVO)getModel().getSelectedData()).getPk_task();
   }
   
   public void setModel(AbstractUIAppModel model)
   {
     super.setModel(model);
     ((DefaultExceptionHanler)this.exceptionHandler).setContext(model.getContext());
     ((DefaultExceptionHanler)this.exceptionHandler).setErrormsg(NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0844"));
   }
   
 
   public void setLoginContext(LoginContext loginContext)
   {
     if ((loginContext.getEntranceUI() instanceof AbsSwitchToftPanelAdaptor)) {
       this.nodeEnv = ((AbsSwitchToftPanelAdaptor)loginContext.getEntranceUI()).getNodeEnv();
     }
   }
   
   public boolean isActionEnable() {
     return ((BillManageModel)getModel()).getSelectedOperaDatas() != null;
   }
 }

