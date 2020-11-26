/**
 *
 */
package nc.ui.iufo.repdatamng.actions;

import java.io.ByteArrayInputStream;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.funcnode.ui.AbstractFunclet;
import nc.impl.iufo.utils.MultiLangTextUtil;
import nc.impl.iufo.utils.NCLangUtil;
import nc.itf.iufo.dataremove.IDataRemoveSrv;
import nc.itf.iufo.individual.IUFOIndividualSettingUtil;
import nc.itf.iufo.ufoe.vorp.IUfoeVorpQuerySrv;
import nc.login.vo.NCSession;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.iufo.balance.BalanceBO_Client;
import nc.ui.iufo.constants.IUfoeActionCode;
import nc.ui.iufo.dataexchange.FilePackage;
import nc.ui.iufo.dataexchange.IExcelExport;
import nc.ui.iufo.dataexchange.RepDataExport;
import nc.ui.iufo.dataexchange.RepDataWithCellsModelExport;
import nc.ui.iufo.dataexchange.TableDataToExcel;
import nc.ui.iufo.input.CSomeParam;
import nc.ui.iufo.input.table.TableInputParam;
import nc.ui.iufo.query.common.model.IUfoBillManageModel;
import nc.ui.iufo.repdatamng.view.ExpSingleRepExcelDlg;
import nc.ui.iufo.uf2.RmsToRmsVerionPkByPubDataUtil;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIDialog;
import nc.ui.uif2.DefaultExceptionHanler;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.model.BillManageModel;
import nc.util.info.sysimp.NCConnTool;
import nc.util.info.sysimp.SysImpUtil;
import nc.util.iufo.pub.UFOString;
import nc.util.iufo.repdataright.RepDataAuthUtil;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.constant.CommonCharConstant;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.KeyDetailDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.data.SingleRepExpParam;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.query.IUfoQueryInitParam;
import nc.vo.iufo.query.IUfoQueryLoginContext;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iufo.task.TaskInfoVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iufo.task.TaskVO.DataRightControlType;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.vorg.ReportManaStruVersionVO;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.time.FastDateFormat;

import com.ufida.iufo.constant.output.IOutputMsgConstant;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.report.sysplugin.print.FreeReportPrintStatusMng;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.iuforeport.tableinput.applet.RepDataParam;
import com.ufsoft.report.util.UfoPublic;
import com.ufsoft.table.CellsModel;


/**
 * 报表数据查询-导出单表
 * @author wuyongc
 * @created at 2011-12-27,下午4:44:32
 *
 */

public class SysImpRepAction extends ImpExpRepDataAuthBaseAction implements IUfoContextKey,SysImpExecutor{
	//增加变量为了导出xml后地址保存
	String fialurl = null;
	StringBuffer tool = new StringBuffer();

//	private static final String EXP_SINGLE_REP = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820002-0029")/*@res "导出单表"*/;
	private static final long serialVersionUID = 6114966328135908251L;

	private static final int EXCEL_SHEET_NAME_MAX_LENGTH = 31;
	
	private static boolean isEnd = false;

	public SysImpRepAction(){
		super();
		setBtnName("外系统数据推送");
		setCode(IUfoeActionCode.EXP_SINGLE_REP);

		((DefaultExceptionHanler)exceptionHandler).setErrormsg(NCLangUtil.getStrByID("1820001_0", "01820001-0437")/*@res "导出失败!"*/);
	}

	@SuppressWarnings("null")
	@Override
	public void doAction(ActionEvent e) throws Exception {
		try{
 
			
			
			
			if(UIDialog.ID_NO==MessageDialog.showYesNoDlg(getModel().getContext().getEntranceUI(), "确认", "是否推送当前报表数据，覆盖数据不能恢复？",UIDialog.ID_NO)){
				return ;
			}
			
			
			Object[] objs = ((BillManageModel)getModel()).getSelectedOperaDatas();
			RepDataQueryResultVO[] repQryResults = new RepDataQueryResultVO[objs.length];
			System.arraycopy(objs, 0, repQryResults, 0, objs.length);
			
		final	SysImpInfoDlg infoDlg = new SysImpInfoDlg(getModel().getContext());
			String taskPK = getTaskPK();
			Object[] retObjs=TaskSrvUtils.getTaskQueryService().getTaskInfoAndBalConds(taskPK, true);
			new Thread(){

				@Override
				public void run() {
					try {
						SysImpRepAction.this.runImp(infoDlg);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}.start();
			infoDlg.showModal();
		
	
			ShowStatusBarMsgUtil.showStatusBarMsg(tool.toString(), super.getModel().getContext());	
		}catch(Exception e1){
			throw new BusinessException(e1.getMessage());
		}
		
	}

	private void excuteExport(SingleRepExpParam expParam, RepDataQueryResultVO repDataQryResult, ReportVO rep, BalanceCondVO[] balConds, String strAccSchemePK) throws Exception {

		
		IUfoQueryInitParam param = ((IUfoQueryLoginContext)getLoginContext()).getInitParam() ;
		KeyVO[] keys = param.getKeyGroup().getKeys();
		String orgID  = expParam.getMeasurePubDatas()[0].getKeywords()[0];
		String orgName = OrgUtil.getOrgName(orgID);

 
		UfoContextVO context = null;
		IRepDataParam repParam = null;
		List<IExcelExport> excelExp = new ArrayList<IExcelExport>();

		orgName = MultiLangTextUtil.getCurLangText(((IUfoBillManageModel)getModel()).getOrgPkMap().get(repDataQryResult.getPk_org()));

			BalanceCondVO balanceCond = null;

			if (expParam.getBalancePK() !=null || !expParam.getBalancePK().equals(BalanceCondVO.NON_SW_DATA_COND_PK)){
				for(BalanceCondVO bc : balConds){
					if(bc.getPk_balancecond().equals(expParam.getBalancePK())){
						balanceCond = bc;
						break;
					}
				}
			}
			String rmsPK = ((IUfoQueryLoginContext)getLoginContext()).getInitParam().getRepStruPK();
			String repPK = expParam.getRepPK();
			if(!repPK.equals(rep.getPk_report())){
				rep = IUFOCacheManager.getSingleton().getReportCache().getByPK(repPK);
			}
 
				List<String> fileNames = new ArrayList<String>();
				int i = 0;
				for(MeasurePubDataVO pubData : expParam.getMeasurePubDatas()){
					repDataQryResult.setKeyword(pubData.getKeywords());
					repDataQryResult.setPk_report(expParam.getRepPK());
					excelExp = new ArrayList<IExcelExport>();
					repDataQryResult.setPubData(pubData);
					context = getContextVO(repDataQryResult);
					repParam = getRepDataParam(repDataQryResult);
					CellsModel formatModel = CellsModelOperator
							.getFormatModelByPKWithDataProcess(context);

					CellsModel cellsModel = null;
					if(balanceCond != null){
						RepDataVO repData=BalanceBO_Client.doSwBalance(repDataQryResult.getPubData(), balanceCond,repPK, rmsPK);
						cellsModel=CellsModelOperator.doGetDataModelFromRepDataVO(formatModel, repData, context);
					}else{
						cellsModel = CellsModelOperator.fillCellsModelWithDBData(formatModel, context);
					}
					
//					String report_code, List<KeyVO> keys,
//					CellsModel model,MeasurePubDataVO pubVo,String repOrgStructCode,String orgCode
					String pkOrgStruct = nodeEnv.getCurrMngStuc();
					String orgCode = ((IUfoBillManageModel)getModel()).getOrgPkMap().get(repDataQryResult.getPk_org()).getCode();
					String taskPK = getTaskPK();
					Object[] retObjs=TaskSrvUtils.getTaskQueryService().getTaskInfoAndBalConds(taskPK, true);
					
					TaskInfoVO taskInfo = (TaskInfoVO) retObjs[0];
					TaskVO task = taskInfo.getTaskVO();
					
					ReportManaStruVersionVO rmsVO = (ReportManaStruVersionVO)NCLocator.getInstance().lookup(IUfoeVorpQuerySrv.class).getRmsVerVOByDate((new UFDateTime(new Date())).toStdString(), pkOrgStruct);
					fialurl = SysImpUtil.createXml(rep.getCode(), keys, cellsModel,pubData,rmsVO.getCode(),orgCode,task.getCode());


				 
				}

 
			
	}

	/**
	 * @create by wuyongc at 2012-2-3,下午2:26:39
	 *
	 * @param repParam
	 * @param pubData
	 * @param strReportPK4ExportExcel
	 * @return
	 */
	private CSomeParam getCSomeParam(IRepDataParam repParam,
			MeasurePubDataVO pubData, String strReportPK4ExportExcel) {
		CSomeParam cSomeParam = new CSomeParam();
		cSomeParam.setAloneId(repParam.getAloneID());
		cSomeParam.setRepId(strReportPK4ExportExcel);
		cSomeParam.setUserID(repParam.getOperUserPK());
		cSomeParam.setUnitId(pubData.getUnitPK());
		return cSomeParam;
	}

//	/**
//	 * @create by wuyongc at 2012-2-3,下午2:17:38
//	 *
//	 * @param file
//	 */
//	private boolean confirmOverwrite(File file) {
//		if(file.exists()){
//			int iRet = UfoPublic.showConfirmDialog(
//					getLoginContext().getEntranceUI(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0842")/*@res "名称为  "*/ + file.getName()
//							+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0846")/*@res " 的excel文件已经存在，是否覆盖?"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0133")/*@res "提示"*/,
//					JOptionPane.YES_NO_OPTION);
//			if (iRet != JOptionPane.YES_OPTION)
//				return false;
//		}
//		return true;
//	}
//
//	/**
//	 * @create by wuyongc at 2012-2-3,下午2:11:04
//	 *
//	 * @param filePath
//	 * @param fileList
//	 * @throws FileNotFoundException
//	 * @throws IOException
//	 */
//	private void zipFile(String filePath, List<File> fileList)
//			throws FileNotFoundException, IOException {
//		StringBuilder zipName = new StringBuilder();
//		// @edit by wuyongc at 2013-7-30,下午4:36:55 
//		if(fileList.size()==1){
//			int lastPoint = filePath.lastIndexOf(".");
//			if(lastPoint>0){
//				zipName.append(filePath.substring(0, lastPoint)).append(".zip");
//			}else{
//				zipName.append(".zip");
//			}
//					
//		}else{
//			String date = FastDateFormat.getInstance("yyyy-MM-dd'T'HH-mm-ss").format(Calendar.getInstance());
//			zipName.append(filePath.substring(0, filePath.lastIndexOf(File.separator)+1));
//			zipName.append(date).append("(").append(fileList.size()).append(")");
//			zipName.append(".zip");
//		}
//
//		FilePackage pack = new FilePackage();
//		pack.zipFile(fileList.toArray(new File[0]), zipName.toString());
//		for(File f : fileList){
//			f.delete();
//		}
//	}

	/**
	 * 第一次的关键字组合值根据选择的查询结果取得
	 * @create by wuyongc at 2012-2-3,下午1:42:04
	 *
	 * @param strAccSchemePK
	 * @param keys
	 * @param keyVals
	 * @param repDataQryResult
	 * @param orgName
	 * @return
	 */
	private StringBuilder getKeyGroupStr(String strAccSchemePK, KeyVO[] keys,
			String[] keyVals, RepDataQueryResultVO repDataQryResult,
			String orgName) {
		IKeyDetailData keyDetailData;
		StringBuilder keywordStr = new StringBuilder(orgName);
		for(int i=1; i<keys.length; i++){//前面已经处理了单位关键字,所以直接从 第二个关键字开始
			keyVals[i] = repDataQryResult.getKeywordByIndex(i+1);
			if(keys[i].isTTimeKeyVO()){
				keywordStr.append(",").append(keyVals[i]);
			}else{
				keyDetailData = KeyDetailDataUtil.getKeyDetailData(keys[i], keyVals[i], strAccSchemePK);
				keywordStr.append(",").append(keyDetailData.getMultiLangText());
			}
		}
		return keywordStr;
	}

	/**
	 * 可能调整过关键字组合值后取得关键字组合值.
	 *
	 * @create by wuyongc at 2012-2-3,下午1:55:04
	 *
	 * @param strAccSchemePK
	 * @param keys
	 * @param keyVals
	 * @param orgName
	 * @return
	 */
	private StringBuilder getKeyGroupStr(String strAccSchemePK, KeyVO[] keys,
			String[] keyVals,
			String orgName) {
		IKeyDetailData keyDetailData;
		StringBuilder keywordStr = new StringBuilder(orgName);
		for(int i=1; i<keys.length; i++){//前面已经处理了单位关键字,所以直接从 第二个关键字开始
			keyDetailData = KeyDetailDataUtil.getKeyDetailData(keys[i], keyVals[i], strAccSchemePK);
			keywordStr.append(",").append(keyDetailData.getMultiLangText());
		}
		return keywordStr;
	}


	/**
	 * 构造excel的Sheet名称
	 * @create by wuyongc at 2012-1-9,上午9:23:29
	 *
	 * @param rep
	 * @param repDataQryResult
	 * @param expParam
	 * @param keys
	 * @param strAccSchemePK
	 * @return
	 */
	private String getSheetName(ReportVO rep,RepDataQueryResultVO repDataQryResult,SingleRepExpParam expParam,KeyVO[] keys,String strAccSchemePK){
		StringBuilder sb = new StringBuilder();
		if(expParam.isbContainRepCode())
			append(sb,rep.getCode());
		if(expParam.isbContainRepName())
			append(sb,rep.getChangeName());
		IKeyDetailData keyDetailData = null;

		String[] keyShowVals = new String[keys.length];
		for (int i = 0; i < keyShowVals.length; i++) {
			keyDetailData = KeyDetailDataUtil.getKeyDetailData(keys[i], repDataQryResult.getKeywordByIndex(i+1), strAccSchemePK);
			keyShowVals[i] = keyDetailData.getMultiLangText();
		}


		if(expParam.getContainKeywordNO() != null){
			for (int i = 0; i < expParam.getContainKeywordNO().length; i++) {
				append(sb,keyShowVals[expParam.getContainKeywordNO()[i]]);
			}
		}
		if(sb.length() == 0){
			for(int i=0; i<keyShowVals.length; i++){
				if(i == 0)
					sb.append(keyShowVals[i]);
				else
					sb.append("_").append(keyShowVals[i]);
			}
		}

		return sb.length()>EXCEL_SHEET_NAME_MAX_LENGTH ? sb.substring(0, 31).toString(): sb.toString();
	}

	private void append(StringBuilder sb, String str){
		if(sb.length() == 0)
			sb.append(str);
		else
			sb.append("_").append(str);
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

	private void setDataSource(UfoContextVO context) {
		DataSourceVO dataSource = new DataSourceVO();
		NCSession session = WorkbenchEnvironment.getInstance().getSession();
//		UserVO loginUser = WorkbenchEnvironment.getInstance().getLoginUser();
		dataSource.setDs_addr(session.getDsName());
		dataSource.setAccount_name(session.getBusiCenterName());
//		dataSource.setLogin_name(loginUser.getUser_code()); // 用户
//		dataSource.setLogin_passw(loginUser.getUser_password()); // 密码
		dataSource.setDs_type(nc.vo.iufo.datasource.DataSourceVO.TYPENC2);
		context.setAttribute(IUfoContextKey.DATA_SOURCE, dataSource);
	}

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
	public void runImp(SysImpUpdataUI ui) throws Exception {
		
		final JComponent UI = getModel().getContext().getEntranceUI();
		if (UI instanceof AbstractFunclet) {
			AbstractFunclet funclet = (AbstractFunclet) UI;
			funclet.showStatusBarMessage(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140udddb",
					"010140udddb0002")/* @res "正在进行后台操作, 请稍等..." */);
			funclet.showProgressBar(true);
			funclet.lockFuncWidget(true);
		}
		try{
			String taskPK = getTaskPK();
			Object[] retObjs=TaskSrvUtils.getTaskQueryService().getTaskInfoAndBalConds(taskPK, true);
			Object[] objs = ((BillManageModel)getModel()).getSelectedOperaDatas();
			RepDataQueryResultVO[] repQryResults = new RepDataQueryResultVO[objs.length];
			
			System.arraycopy(objs, 0, repQryResults, 0, objs.length);
			TaskInfoVO taskInfo = (TaskInfoVO) retObjs[0];
			TaskVO task = taskInfo.getTaskVO();
			
			IDataRemoveSrv remSrv = NCLocator.getInstance().lookup(IDataRemoveSrv.class);
			
			for(RepDataQueryResultVO repDataRs:repQryResults){
				
			
				
				
				
				// @edit by wuyongc at 2013-4-22,下午2:07:23
				//为了减少查询任务这个连接数，首先就一次查询出来了很多东西。。。有点无奈了

			
				boolean bHasEditPermission = false;
				if(task.getData_contype() != DataRightControlType.TYPE_NOTCONTROL.ordinal()){
					bHasEditPermission = RepDataAuthUtil.bHasViewPermission(taskPK,WorkbenchEnvironment.getInstance().getLoginUser().getCuserid(),
							getRepPK(), getOrgPK(), getRmsPK(), getMainOrgPK(),task.getData_contype());
					if (!bHasEditPermission) {
						throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0765")/*@res "当前用户没有对当前报表的查看操作权限！"*/);
					}
				}

				final BalanceCondVO[] balConds =(BalanceCondVO[])retObjs[1];
				final String strAccSchemePK = task.getPk_accscheme();

				IUfoQueryInitParam param = ((IUfoQueryLoginContext)getLoginContext()).getInitParam() ;
//				RepDataQueryResultVO repDataRs = (RepDataQueryResultVO)getModel().getSelectedData();
//				String rmsVersionPk=RmsToRmsVerionPkByPubDataUtil.getRmsVersionPkByRmsPkAndPubdata(repDataRs.getPubData(), param.getRepStruPK());
				KeyVO[] keys = param.getKeyGroup().getKeys();
				String[] keyVals = new String[keys.length];
				StringBuilder keywordGroupValue = new StringBuilder();
				final RepDataQueryResultVO repDataQryResult = new RepDataQueryResultVO();

				BeanUtils.copyProperties(repDataQryResult, repDataRs);

				String orgName = MultiLangTextUtil.getCurLangText(((IUfoBillManageModel)getModel()).getOrgPkMap().get(repDataQryResult.getPk_org()));

				StringBuilder keywordStr = getKeyGroupStr(strAccSchemePK, keys,
						keyVals, repDataQryResult, orgName);

				keywordGroupValue.append(keywordStr);

				ReportCache repCache = IUFOCacheManager.getSingleton().getReportCache();
				 ReportVO rep = repCache.getByPK(repDataQryResult.getPk_report());

				keyVals[0] = repDataQryResult.getPk_org();
				ExpSingleRepExcelDlg dlg = new ExpSingleRepExcelDlg(getLoginContext(),nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820002-0029")/*@res "导出单表"*/, taskPK,rep.getPk_report(), keyVals,task.getPk_accscheme(),param,repDataRs.getPubData());


				dlg.setAloneId(repDataQryResult.getAlone_id());

				dlg.setBalConds(balConds);


				dlg.setKeygroupValue(keywordStr.toString());

				dlg.setKeyVals(keyVals);
				//设置任务 关键字
				dlg.gettaskKeywordLabel().setText(task.getCode() + " " + task.getChangeName() + " " +  keywordGroupValue);


				dlg.setTableDatas(rep,repDataQryResult.getPubData());

				dlg.setTitle(getRepDataQuery() + "-" + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820002-0029")/*@res "导出单表"*/);

				
				
				
				dlg.setDefaultFileName(new String[]{
						rep.getCode() + "_" + rep.getChangeName(),
						keywordGroupValue.toString()});



			
						final SingleRepExpParam expParam = dlg.getSingleRepExpParam();
//						expParam.setFilePath("d:/"+repDataRs.getPk_report()+".xls");
//						dlg.getKeygroupValue();
						StringBuilder keyGroupStr = getKeyGroupStr(strAccSchemePK, keys, expParam.getMeasurePubDatas()[0].getKeywords(), orgName);
						dlg.setKeygroupValue(keyGroupStr.toString());
					
						int runTime = 0;
						isEnd = false;
					
						
//						IUfoQueryInitParam param = ((IUfoQueryLoginContext)getLoginContext()).getInitParam() ;
//						KeyVO[] keys = param.getKeyGroup().getKeys();
						String orgID  = expParam.getMeasurePubDatas()[0].getKeywords()[0];
//						String orgName = OrgUtil.getOrgName(orgID);

				 
						UfoContextVO context = null;
						IRepDataParam repParam = null;
						List<IExcelExport> excelExp = new ArrayList<IExcelExport>();

						orgName = MultiLangTextUtil.getCurLangText(((IUfoBillManageModel)getModel()).getOrgPkMap().get(repDataQryResult.getPk_org()));

							BalanceCondVO balanceCond = null;

							if (expParam.getBalancePK() !=null || !expParam.getBalancePK().equals(BalanceCondVO.NON_SW_DATA_COND_PK)){
								for(BalanceCondVO bc : balConds){
									if(bc.getPk_balancecond().equals(expParam.getBalancePK())){
										balanceCond = bc;
										break;
									}
								}
							}
							String rmsPK = ((IUfoQueryLoginContext)getLoginContext()).getInitParam().getRepStruPK();
							String repPK = expParam.getRepPK();
							if(!repPK.equals(rep.getPk_report())){
								rep = IUFOCacheManager.getSingleton().getReportCache().getByPK(repPK);
							}
				 
								List<String> fileNames = new ArrayList<String>();
								int i = 0;
								for(MeasurePubDataVO pubData : expParam.getMeasurePubDatas()){
									repDataQryResult.setKeyword(pubData.getKeywords());
									repDataQryResult.setPk_report(expParam.getRepPK());
									excelExp = new ArrayList<IExcelExport>();
									repDataQryResult.setPubData(pubData);
									context = getContextVO(repDataQryResult);
									repParam = getRepDataParam(repDataQryResult);
									CellsModel formatModel = CellsModelOperator
											.getFormatModelByPKWithDataProcess(context);

									CellsModel cellsModel = null;
									if(balanceCond != null){
										RepDataVO repData=BalanceBO_Client.doSwBalance(repDataQryResult.getPubData(), balanceCond,repPK, rmsPK);
										cellsModel=CellsModelOperator.doGetDataModelFromRepDataVO(formatModel, repData, context);
									}else{
										cellsModel = CellsModelOperator.fillCellsModelWithDBData(formatModel, context);
									}
									
//									String report_code, List<KeyVO> keys,
//									CellsModel model,MeasurePubDataVO pubVo,String repOrgStructCode,String orgCode
									String pkOrgStruct = nodeEnv.getCurrMngStuc();
									String orgCode = ((IUfoBillManageModel)getModel()).getOrgPkMap().get(repDataQryResult.getPk_org()).getCode();
//						 
									
									ReportManaStruVersionVO rmsVO = (ReportManaStruVersionVO)NCLocator.getInstance().lookup(IUfoeVorpQuerySrv.class).getRmsVerVOByDate((new UFDateTime(new Date())).toStdString(), pkOrgStruct);
									Map<String,String> rtn = remSrv.pushReport(rep.getCode(), keys, cellsModel,pubData,rmsVO.getCode(),orgCode,task.getCode());
									rtn.put("report_name", orgName+"_"+rep.getCode());
									ui.upUi(rtn);

								 
								}

			
								
//								remSrv.rmvSysImp(expParam,repDataQryResult,rep,balConds,strAccSchemePK);
//									excuteExport(expParam,repDataQryResult,rep,balConds,strAccSchemePK);
									

						
//					while(true){
//							Thread.sleep(100*5);
//							runTime++;
//							if(runTime>60)
//							break;
//						}
					
//					}
//					else if(dlg.getResult() == UIDialog.ID_CANCEL){
//						ShowStatusBarMsgUtil.showStatusBarMsg(NCLangUtil.getStrByID("1820001_0", "01820001-0050"/*导出取消。*/), getLoginContext());
//						return;
//					}

			
				
			}
			ui.upUi("finshed!");
			
		}catch(Exception ex){
			Logger.error(ex);
		}
		finally{
			
			ShowStatusBarMsgUtil.showStatusBarMsg("推送完成", getLoginContext());
			if (UI instanceof AbstractFunclet) {
				AbstractFunclet funclet = (AbstractFunclet) UI;
				funclet.lockFuncWidget(false);
				funclet.showProgressBar(false);
			}
		}
		
	
	 
		
	}
}