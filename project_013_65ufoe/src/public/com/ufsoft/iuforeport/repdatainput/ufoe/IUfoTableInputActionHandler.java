package com.ufsoft.iuforeport.repdatainput.ufoe;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.core.service.TimeService;
import nc.bs.uif2.validation.ValidationFailure;
import nc.impl.iufo.utils.MultiLangTextUtil;
import nc.itf.bd.defdoc.IDefdoclistQryService;
import nc.itf.iufo.approveset.IApproveQueryService;
import nc.itf.iufo.balance.IBalanceCondService;
import nc.itf.iufo.commit.ICommitManageService;
import nc.itf.iufo.commit.ICommitQueryService;
import nc.itf.iufo.keydef.ICorpQuerySrv;
import nc.itf.iufo.repdataquery.IRepDataInfoQuerySrv;
import nc.itf.iufo.report.IReportService;
import nc.itf.org.IOrgMetaDataIDConst;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.pub.iufo.basedoc.UserUtil;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.KeyGroupCache;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.CommonException;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pubitf.bd.accessor.GeneralAccessorFactory;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.ui.iufo.balance.BalanceBO_Client;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.dataexchange.IUFOMultiSheetImportUtil;
import nc.ui.iufo.dataexchange.MultiSheetImportUtil;
import nc.ui.iufo.dataexchange.RepDataExport;
import nc.ui.iufo.dataexchange.RepDataWithCellsModelExport;
import nc.ui.iufo.dataexchange.TableDataToExcel;
import nc.ui.iufo.input.CSomeParam;
import nc.ui.iufo.input.InputActionUtil;
import nc.ui.iufo.input.table.TableInputParam;
import nc.ui.iufo.input.ufoe.IUfoInputActionUtil;
import nc.util.iufo.input.BalanceReportExportUtil;
import nc.util.iufo.pub.AuditUtil;
import nc.util.iufo.pub.FileNameUtil;
import nc.util.iufo.pub.OIDMaker;
import nc.util.iufo.pub.UfoException;
import nc.util.iufo.repdataright.RepDataAuthUtil;
import nc.util.iufo.sysinit.UfobIndividualSettingUtil;
import nc.util.iufo.sysinit.UfobSysParamQueryUtil;
import nc.utils.iufo.CommitUtil;
import nc.utils.iufo.TaskCheckRunUtil;
import nc.utils.iufo.TaskSrvUtils;
import nc.utils.iufo.TotalSrvUtils;
import nc.vo.bd.accessor.IBDData;
import nc.vo.bd.defdoc.DefdoclistVO;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.commit.RepDataCommitVO;
import nc.vo.iufo.commit.TaskCommitVO;
import nc.vo.iufo.constant.CommonCharConstant;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.KeyDetailDataUtil;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasurePubDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepCellsModel;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.data.RepExpParam;
import nc.vo.iufo.data.TempRepExpParam;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.imprep.ImpAloneIdStateVO;
import nc.vo.iufo.imprep.ImpRepStateVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.param.FiveTuple;
import nc.vo.iufo.param.RepImpParam;
import nc.vo.iufo.param.ThreeTuple;
import nc.vo.iufo.param.TwoTuple;
import nc.vo.iufo.param.excel.ExcelImp4FmtParam;
import nc.vo.iufo.repdataauth.RepDataAuthType;
import nc.vo.iufo.repdataquery.RepCommitStateVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iufo.task.ApproveReportSet;
import nc.vo.iufo.task.ICommitConfigConstant;
import nc.vo.iufo.task.RepDataTaskParam;
import nc.vo.iufo.task.TaskApproveVO;
import nc.vo.iufo.task.TaskRepDataParam;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iufo.total.TotalSchemeVO;
import nc.vo.iuforeport.rep.RepFormatModel;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.iuforeport.rep.TemplateSuite;
import nc.vo.org.ReportOrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.pf.IPfRetCheckInfo;
import nc.vo.util.BDUniqueRuleValidate;
import nc.vo.util.bizlock.BizlockDataUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.poi.POIXMLException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.ufida.dataset.Context;
import com.ufida.iufo.constant.impexp.ImpExpConstant;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.pub.tools.BeanUtilities;
import com.ufida.zior.console.ActionHandler;
import com.ufsoft.iufo.check.vo.CheckConVO;
import com.ufsoft.iufo.check.vo.CheckResultVO;
import com.ufsoft.iufo.excel.util.RepImpExpPubUtil;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.dynarea.DynAreaUtil;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.fmtplugin.formula.FormulaModel;
import com.ufsoft.iufo.fmtplugin.formula.UfoFmlExecutor;
import com.ufsoft.iufo.fmtplugin.key.KeywordModel;
import com.ufsoft.iufo.fmtplugin.service.ReportFormatSrv;
import com.ufsoft.iufo.func.excel.text.ImpExpFileNameUtil;
import com.ufsoft.iufo.impexp.util.RepFmtImpExpUtils;
import com.ufsoft.iufo.inputplugin.MeasTraceInfo;
import com.ufsoft.iufo.inputplugin.biz.FontFactory;
import com.ufsoft.iufo.inputplugin.biz.UfoExcelImpUtil;
import com.ufsoft.iufo.inputplugin.biz.data.ImportExcelDataBizUtil;
import com.ufsoft.iufo.inputplugin.biz.file.ChooseRepData;
import com.ufsoft.iufo.inputplugin.biz.file.MenuStateData;
import com.ufsoft.iufo.inputplugin.inputcore.IDMaker;
import com.ufsoft.iufo.resource.ResourceUtil;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;
import com.ufsoft.iuforeport.repdatainput.TableInputActionHandler;
import com.ufsoft.iuforeport.repdatainput.TableInputHandlerHelper;
import com.ufsoft.iuforeport.tableinput.TraceDataResult;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.iuforeport.tableinput.applet.ITraceDataParam;
import com.ufsoft.iuforeport.tableinput.applet.RepDataParam;
import com.ufsoft.iuforeport.tableinput.applet.TableInputException;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.table.CellsModel;

public class IUfoTableInputActionHandler extends TableInputActionHandler{

	/**
	 * 任务审核
	 *
	 * @create by liuchuna at 2010-6-30,下午02:43:06
	 *
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public CheckResultVO[] checkTask(Object param) throws Exception{
		Object[] params=(Object[])param;
		return TaskCheckRunUtil.doCheckInTask((IRepDataParam)params[0],(LoginEnvVO)params[1], true);
	}

	/**
	 * 表内审核
	 *
	 * @create by liuchuna at 2010-6-30,下午02:43:30
	 *
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public CheckResultVO checkReport(Object param) throws Exception{
		Object[] params=(Object[])param;
		return TaskCheckRunUtil.doCheckInRep((IRepDataParam)params[0],(LoginEnvVO)params[1],(CellsModel)params[2],(Boolean)params[3], null);
	}

	/**
	 * 审核下级
	 *
	 * @create by liuchuna at 2010-6-30,下午03:00:06
	 *
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public CheckResultVO[] checkSubReport(Object param) throws Exception{
		Object[] params = (Object[]) param;
		return TaskCheckRunUtil.doCheckSubReport((MeasurePubDataVO)params[0],(CheckConVO)params[1],(IRepDataParam)params[2],(LoginEnvVO)params[3],(String)params[4]);
	}

	/**
	 * 任务审核下级
	 *
	 * @create by liuchuna at 2010-6-30,下午04:42:24
	 *
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public CheckResultVO[] checkSubTask(Object param) throws Exception{
		Object[] params = (Object[]) param;
		return TaskCheckRunUtil.doCheckSubTask((MeasurePubDataVO)params[0],(CheckConVO)params[1],(IRepDataParam)params[2],(LoginEnvVO)params[3],(String)params[4]);
	}

	/**
	 * 审核结果查询
	 *
	 * @create by liuchuna at 2010-7-2,上午11:38:28
	 *
	 * @param param
	 * @return
	 * @throws TableInputException
	 */
	public CheckResultVO[] queryCheckResult(Object param) throws TableInputException{
//		Object[] params = (Object[]) param;
//		return TaskCheckRunUtil.queryCheckResult((CheckConVO)params[0]);
		return null;
	}

	/**
	 * 查看汇总来源
	 * @create by xulm at 2010-7-15,上午10:57:08
	 *
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public TraceDataResult traceTotalData(Object param) throws Exception{
		try{
			Object[] params=(Object[])param;
			return innerTraceTotalData((IRepDataParam)params[0],(LoginEnvVO)params[1],(CellsModel)params[2],(ITraceDataParam)params[3],(TotalSchemeVO)params[4],(String)params[5]);
		}catch(Exception e){
			AppDebug.debug(e);
			throw e;
		}
	}


	@Override
	@SuppressWarnings({ "unchecked", "deprecation" })
	public RepDataOperResultVO openRepData(Object param) throws Exception{
		try{
			Object[] params=(Object[])param;
			IRepDataParam repDataParam=(IRepDataParam)params[0];

			RepDataOperResultVO result = null;
			boolean bFreeTotal = (Boolean) params[4];
			//如果组织为组织属性构建 :没有单位关键字时，不能通过单位关键字判断。
//			if (StringUtil.isEmptyWithTrim(OrgUtil.getOrgName(repDataParam.getPubData().getUnitPK())))
			if(bFreeTotal)
			{
				LoginEnvVO loginEnv=(LoginEnvVO)params[1];
				ArrayList<String> lstTotalReportOrgPK=(params!=null&&params.length>3)?(ArrayList<String>)params[3]:null;
				result = getFreeTotalResult(repDataParam, loginEnv, lstTotalReportOrgPK);
				return result;
			}else
			{
				result = super.openRepData(params);
			}

			if(result != null) {
				// 各张表的报送状态
				String[] repPks = TaskSrvUtils.getReportIdByTaskId(repDataParam.getTaskPK());
				//TaskVO taskVO = TaskSrvUtils.getTaskVOById(repDataParam.getTaskPK());
				List<RepCommitStateVO> vInputResult= (List<RepCommitStateVO>)ActionHandler.execWithZip(IUfoTableInputActionHandler.class.getName(), "getRepCommitStateVO", new Object[]{
					repDataParam.getAloneID(), repPks ,repDataParam.getTaskPK() });
				Map<String, Integer> commitMap = new HashMap<String, Integer>();
				// @edit by zhoushuang at 2014-1-12,下午4:44:17 增加报表审批状态判断
				Map<String, Integer> repApproveStatuMap = null;
				//if (taskVO.getApprovemode()!=null && taskVO.getApprovemode() == TaskVO.REPORTAPPROVEMODE) {
					IApproveQueryService approveQry = NCLocator.getInstance().lookup(IApproveQueryService.class);
					repApproveStatuMap = approveQry.getReportApproveStatus(repPks,repDataParam.getAloneID());
				//}
				
				if(vInputResult != null && !vInputResult.isEmpty()) {
					for(RepCommitStateVO repCommit : vInputResult) {
						commitMap.put(repCommit.getPk_report(), repCommit.getRepcommitstate());
					}
				}
				result.setRepCommitVos(commitMap);
				result.setRepApproveStatus(repApproveStatuMap);
			}
			return result;
		}catch(Exception e){
			AppDebug.debug(e);
			throw e;
		}
	}

	/**
	 * @create by wuyongc at 2012-3-19,下午3:27:42
	 *
	 * @param repDataParam
	 * @param loginEnv
	 * @param lstTotalReportOrgPK
	 * @return
	 * @throws UFOSrvException
	 * @throws TableInputException
	 */
	private RepDataOperResultVO getFreeTotalResult(IRepDataParam repDataParam,
			LoginEnvVO loginEnv, ArrayList<String> lstTotalReportOrgPK)
			throws UFOSrvException, TableInputException {
		TotalSchemeVO totalScheme=new TotalSchemeVO();
		totalScheme.setOrg_type(TotalSchemeVO.TYPE_FREE);
		totalScheme.setOrg_content(lstTotalReportOrgPK);
		totalScheme.setPk_org(repDataParam.getRepOrgPK());
		RepDataOperResultVO result= TotalSrvUtils.createFreeTotalResults(repDataParam.getPubData(), totalScheme,repDataParam.getReportPK(), null);
		UfoContextVO context=TableInputHandlerHelper.getContextVO(repDataParam,loginEnv);
		context.setAttribute(MEASURE_PUB_DATA_VO, repDataParam.getPubData());
		MenuStateData  menuState=TableInputHandlerHelper.getMenuStateData(context, repDataParam, loginEnv, UfoContextVO.RIGHT_DATA_WRITE);
		menuState.setCanCommit(false);
		menuState.setCanRequestCancelCommit(false);
		menuState.setCanAreaCal(false);
		menuState.setCanExcelImp(false);
		menuState.setCanSW(false);
		result.setMenuState(menuState);
		return result;
	}
	
	public CellsModel createFreeTotalResult(Object obj)
			throws UFOSrvException, TableInputException {
		Object[] objs = (Object[])obj;
		IRepDataParam repDataParam = (IRepDataParam) objs[0];
		LoginEnvVO loginEnv = (LoginEnvVO) objs[1];
		ArrayList<String> lstTotalReportOrgPK  = (ArrayList<String>) objs[2];
		RepDataOperResultVO rs = getFreeTotalResult(repDataParam, loginEnv, lstTotalReportOrgPK);
		return rs.getCellsModel();
	}

	//自由汇总 舍位时调用
	public RepDataOperResultVO freeTotalBalance(FiveTuple<IRepDataParam, LoginEnvVO, String, CellsModel,ArrayList<String>> tuple) throws Exception{
		LoginEnvVO loginEnv = tuple.second;
		IRepDataParam repDataParam = tuple.first;
		String strBalCondPK = tuple.third;
		CellsModel curCellsModel = tuple.four;

		MeasurePubDataVO pubData=repDataParam.getPubData();
        UfoContextVO context=TableInputHandlerHelper.getContextVO(repDataParam,loginEnv);

        if (strBalCondPK==null || strBalCondPK.equals(BalanceCondVO.NON_SW_DATA_COND_PK)){
			ArrayList<String> lstTotalReportOrgPK= tuple.five;
			RepDataOperResultVO result = getFreeTotalResult(repDataParam,
					loginEnv, lstTotalReportOrgPK);
			return result;
        }else{
        	BalanceCondVO balanceCond=BalanceBO_Client.loadBalanceCondByPK(strBalCondPK);
        	//TODO 因为自由汇总的数据没有保存在数据库中,按照现有的舍位机制无法实现对当前的cellsModel进行舍位,所以暂时不实现,前台控制 自由汇总不让舍位.
        	BalanceReportExportUtil.processBalanceRepCellsModel(new ReportFormatSrv(context,curCellsModel),false,balanceCond);
        	pubData.setVer(0);
            pubData.setAloneID(MeasureDataUtil.getAloneID(pubData));
        }

		RepDataOperResultVO result=new RepDataOperResultVO();
		result.setCellsModel(curCellsModel);

		boolean bIsCanInput = UfoEFormulaEditControl.isFormulaEdit(repDataParam.getReportPK(),repDataParam.getTaskPK());
		result.setFmlCanInput(bIsCanInput);

		return result;
	}
	@Override
	protected Object[] innerGetMeasTraceContext(MeasTraceInfo measTraceInfo,boolean bNeedCreateMainBoard) throws Exception{
		Context context=new Context();
		MeasurePubDataVO pubData=TableInputHandlerHelper.getTraceMeasPubData(measTraceInfo);
		context.setAttribute(MEASURE_PUB_DATA_VO,pubData);

		TaskVO[] tasks=IUfoTableInputHandlerHelper.getTraceMeasTaskVOs(pubData, measTraceInfo);
		if (tasks==null || tasks.length<=0)
			throw new UfoException("uiuforep00101");

		if (bNeedCreateMainBoard){
			context.setAttribute(CUR_REPORG_PK, measTraceInfo.getStrOrgPK());
		}
		return new Object[]{context,measTraceInfo,tasks};
	}

	@Override
	public RepDataOperResultVO importExcelData(Object param) throws Exception{
		Object[] params=(Object[])param;
		TaskVO task = null;
		IRepDataParam repDataParam = (IRepDataParam)params[0];
		if(params.length == 5){
			task = (TaskVO)params[4];
		}else{
			task = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(repDataParam.getTaskPK());
		}
		return innerImportExcelData(repDataParam,(LoginEnvVO)params[1],(List<Object[]>)params[2],((Boolean)params[3]).booleanValue(),task);
	}

	/**
	 *
	 * 导入IUFO套表
	 * @create by wuyongc at 2012-2-9,下午4:25:56
	 *
	 * @param param
	 * @return CellsModel  当前的报表的CellsModel
	 * @throws Exception
	 */
	public TwoTuple<String,CellsModel> importTempIUFOData(Object p) throws Exception{
		try{
			Object[] params=(Object[])p;

			TaskRepDataParam param = (TaskRepDataParam)params[0];

			LoginEnvVO loginEnv = (LoginEnvVO)params[1];

			String[] strKeyVals = (String[])params[2];

	        //获得报表数据的AloneID
			ReportVO report= UFOCacheManager.getSingleton().getReportCache().getByPK(param.getReportPK());
			KeyGroupVO keyGroup= UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(report.getPk_key_comb());
	        String strImportAloneID = TableInputHandlerHelper.doGetNewAloneID(param,loginEnv,keyGroup.getKeys(),strKeyVals);
	        UfoContextVO context= null;

	        String strCurAloneID=param.getAloneID();
	        CellsModel cellsModel = null;
	        Set<ImpRepStateVO> repStateSet = new HashSet<ImpRepStateVO>();

	        TaskVO task = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(param.getTaskPK());
		    //如果要导入的关键字条件与当前关键字条件相同，给出出错提示
	        if(strImportAloneID != null && strImportAloneID.equals(strCurAloneID)){
	        	throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0959")/*@res "不能导入当前关键字组合的数据"*/);  //"关键字组合"
	        } else{
	        	MeasurePubDataBO_Client.getAloneID(param.getPubData());
	        	MeasurePubDataVO curPubData = param.getPubData();


	        	String curRepPK = param.getReportPK();

	    		 IApproveQueryService approveSrv = (IApproveQueryService)nc.bs.framework.common.NCLocator.getInstance().lookup(IApproveQueryService.class.getName());
	    		 //wangqi 20130109  取上报审批状态
	    		TaskApproveVO taskApprove = null;
	    		//boolean reportFlag = false;
	    		if (task.getApprovemode()==TaskVO.TASKAPPROVEMODE) {
	        		 taskApprove = approveSrv.getTaskApprove(param.getTaskPK(),param.getAloneID(),TaskApproveVO.FLOWTYPE_COMMIT);
				}
//	    		else {
//					 reportFlag = approveSrv.isAllApproveInOneStatus(param.getTaskPK(),param.getAloneID(),TaskApproveVO.FLOWTYPE_COMMIT, param.getPubData().getUnitPK(),IPfRetCheckInfo.NOSTATE);
//				}
	    		
	    		TaskApproveVO[] taskApproveVOs = approveSrv.getAllApproveVOInTask(param.getTaskPK(),param.getAloneID(),TaskApproveVO.FLOWTYPE_COMMIT);
				HashMap<String,Integer> reportToStatesMap=new HashMap<String,Integer>();
				if (taskApproveVOs != null && taskApproveVOs.length > 0) {				
					for (int i = 0; i < taskApproveVOs.length; i++) {
						Vector<String> repPkVector = ((ApproveReportSet)(taskApproveVOs[i].getPk_report())).getReportPKs();
						for (String reppk:repPkVector) {
							reportToStatesMap.put(reppk, taskApproveVOs[i].getVbillstatus());
						}
					}
				}
	    		
	    		 //任务审批状态为自由态才能导入
	    		 if(taskApprove == null ? true:taskApprove.getVbillstatus() == IPfRetCheckInfo.NOSTATE){

	    		        // 一次取出上报状态
	    		        RepDataCommitVO[] repCommits = CommitUtil.getReportCommitState(param.getAloneID(),param.getTaskPK(), param.getTaskRepPKs());
	    		        Map<String,Integer> repCommitMap = new HashMap<String,Integer>();
	    		        Map<String,String> repDistMap = new HashMap<String,String>();
	    		        //上报状态放入Map中
	    		        for(RepDataCommitVO commit : repCommits){
	    		        	repCommitMap.put(commit.getPk_report(), commit.getCommit_state());
	    		        	repDistMap.put(commit.getPk_report(), commit.getDataorigin());
	    		        }

	    		        for(int i = 0 ; i < param.getTaskRepPKs().length; i++){
	    		            String repPK = param.getTaskRepPKs()[i];
	    		            Integer commitStatus = repCommitMap.get(repPK);
	    		            String dataOrigin = repDistMap.get(repPK);
	    		            Integer approveStatus = IPfRetCheckInfo.NOSTATE;
	    		            if (reportToStatesMap.get(repPK)!=null) {
	    		            	approveStatus = reportToStatesMap.get(repPK);
							}
	    		            
	    		            //没有上报才能导入
	    		            if(approveStatus.equals(IPfRetCheckInfo.NOSTATE)&&(commitStatus == null || (commitStatus !=CommitStateEnum.COMMITED && commitStatus != CommitStateEnum.AFFIRMED))){
	    				        RepDataAuthType auth = RepDataAuthUtil.getAuthType(AuditUtil.getCurrentUser(), repPK, param.getPubData().getUnitPK(), param.getRepMngStructPK(),param.getRepOrgPK(),param.getTaskPK());
	    			            //有报表数据权限才能导入
	    				        if(auth == RepDataAuthType.EDIT){
	    				        	//分布式数据不能导入
	    				        	if (dataOrigin != null){
		    			            	ImpRepStateVO repStateVO = getNewImpRepStateVO(param, task);
		    			            	repStateVO.putDistState(repPK, dataOrigin);
		    			   			 	repStateSet.add(repStateVO);
	    				        	}else{
		    			        		report=UFOCacheManager.getSingleton().getReportCache().getByPK(repPK);
		    			        		if(keyGroup == null)
		    			        			keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(report.getPk_key_comb());

		    			        		MeasurePubDataVO newPubData=MeasurePubDataBO_Client.findByAloneID(report.getPk_key_comb(),strImportAloneID);
		    				        	curPubData.setAccSchemePK(param.getPubData().getAccSchemePK());
		    				        	newPubData.setAccSchemePK(param.getPubData().getAccSchemePK());

		    				        	//注意 ,下面的方法 将repPK设置到param中了,所以在for循环提前得到了curRepPK
		    				        	context = getContextVO(param,loginEnv,false,repPK);
		    				        	context.setAttribute(MEASURE_PUB_DATA_VO, newPubData);
		    				        	CellsModel importCellsModel = loadCellsModel(context);
		    				        	context.setAttribute(MEASURE_PUB_DATA_VO, curPubData);
		    				        	//修改cellsModel的关键字为原来的关键字值
		    				        	CellsModel fmtModel = DynAreaUtil.getDataModelWithExModel(importCellsModel);
		    				        	MeasurePubDataUtil.initMeasurePubDataKeyData(new MeasurePubDataVO[]{curPubData});
		    				        	CellsModelOperator.setMainKeyData(fmtModel, importCellsModel, curPubData);
		    				        	if(curRepPK.equals(param.getReportPK())){
		    				        		cellsModel = importCellsModel;
		    				        	}

		    				        	CellsModelOperator.saveDataToDB(importCellsModel, context);

		    							String userId = (String) context.getAttribute(CUR_USER_ID);
		    							// 添加录入审计信息
		    						    ICommitManageService commitSrv=NCLocator.getInstance().lookup(ICommitManageService.class);
		    					        commitSrv.addRepInputSate(param.getTaskPK(), strCurAloneID, repPK,
		    					        		userId, true, null);
	    				        	}
	    			            }else{
	    			            	ImpRepStateVO repStateVO = getNewImpRepStateVO(param, task);
	    			            	repStateVO.putRepAuth(repPK, auth);
	    			   			 	repStateSet.add(repStateVO);
	    			            }
	    		            }else{
	    						if (commitStatus==CommitStateEnum.COMMITED || commitStatus== CommitStateEnum.AFFIRMED) {
	    							ImpRepStateVO repStateVO = getNewImpRepStateVO(param, task);
	    							repStateVO.putRepCommitState(repPK, commitStatus);
	    							repStateSet.add(repStateVO);
	    						} else {
	    							ImpRepStateVO repStateVO = getNewImpRepStateVO(param, task);
	    							repStateVO.putApproveStatus(repPK, approveStatus);
	    							repStateSet.add(repStateVO);
	    						}
	    		            }
	    		        }
				} else {
					ImpRepStateVO repStateVO = getNewImpRepStateVO(param, task);
//					if (task.getApprovemode() == TaskVO.REPORTAPPROVEMODE) {
//						repStateVO.setApproveState(IPfRetCheckInfo.COMMIT);
//					} else {
						repStateVO.setApproveState(taskApprove.getVbillstatus());
//					}
					repStateSet.add(repStateVO);
				}

	    			StringBuilder msg = new StringBuilder();
	    	        for(ImpRepStateVO repState : repStateSet){
	    	        	if(msg.length() != 0)
	    	        		msg.append(ImpRepStateVO.ENTER_CHAR);
	    	        	msg.append(repState.getMsg());
	    	        }
	        	return new TwoTuple<String,CellsModel>(msg.toString(),cellsModel);
	        }
		}catch(Exception e){
			AppDebug.debug(e);
			throw e;
		}
	}

	/**
	 * 如果导入成功,则返回导入的CellsModel,否则返回不能导入的原因.
	 */
	@Override
	public TwoTuple<String,CellsModel> importIUFOData(Object obj) throws Exception{
		Object[] params=(Object[])obj;
		IRepDataParam param = (IRepDataParam)params[0];
				LoginEnvVO	loginEnv = (LoginEnvVO)params[1];
				String []strKeyVals = (String[])params[2];
				CellsModel importCellsModel = null;

			ImpRepStateVO repState = null;
	        //获得报表数据的AloneID
			ReportVO report=UFOCacheManager.getSingleton().getReportCache().getByPK(param.getReportPK());
			KeyGroupVO keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(report.getPk_key_comb());
	        String strImportAloneID = TableInputHandlerHelper.doGetNewAloneID(param,loginEnv,keyGroup.getKeys(),strKeyVals);
	        UfoContextVO context=TableInputHandlerHelper.getContextVO(param,loginEnv);

	        TaskVO task = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(param.getTaskPK());
	        String strCurAloneID=param.getAloneID();
		    //如果要导入的关键字条件与当前关键字条件相同，给出出错提示
	        if(strImportAloneID != null && strImportAloneID.equals(strCurAloneID)){
	        	throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0959")/*@res "不能导入当前关键字组合的数据"*/);  //"关键字组合"
	        } else{
	        	 IApproveQueryService approveSrv = (IApproveQueryService)nc.bs.framework.common.NCLocator.getInstance().lookup(IApproveQueryService.class.getName());
	        	 //wangqi 20130109  取上报审批状态
	        	 TaskApproveVO taskApprove =null;
	        	 //boolean reportFlag = false;
	        	 if (task.getApprovemode() == null||task.getApprovemode()==TaskVO.TASKAPPROVEMODE) {
	        		 taskApprove = approveSrv.getTaskApprove(param.getTaskPK(),param.getAloneID(),TaskApproveVO.FLOWTYPE_COMMIT);
				}
//	        	 else {
//					 reportFlag = approveSrv.isAllApproveInOneStatus(param.getTaskPK(),param.getAloneID(),TaskApproveVO.FLOWTYPE_COMMIT, param.getPubData().getUnitPK(),IPfRetCheckInfo.NOSTATE);
//				}
	        	TaskApproveVO[] taskApproveVOs = approveSrv.getAllApproveVOInTask(param.getTaskPK(),param.getAloneID(),TaskApproveVO.FLOWTYPE_COMMIT);
	 			HashMap<String,Integer> reportToStatesMap=new HashMap<String,Integer>();
	 			if (taskApproveVOs != null && taskApproveVOs.length > 0) {				
	 				for (int i = 0; i < taskApproveVOs.length; i++) {
	 					Vector<String> repPkVector = ((ApproveReportSet)(taskApproveVOs[i].getPk_report())).getReportPKs();
	 					for (String reppk:repPkVector) {
	 						reportToStatesMap.put(reppk, taskApproveVOs[i].getVbillstatus());
	 					}
	 				}
	 			}
	        	 //任务审批状态 或 任务下所有报表均为自由态才能导入
	    		 if(taskApprove == null ? true:taskApprove.getVbillstatus() == IPfRetCheckInfo.NOSTATE){
	    			 RepDataCommitVO repDataCommit =  CommitUtil.getReportCommitState(param.getAloneID(),param.getTaskPK(), param.getReportPK());
	    			 Integer approveStatus = IPfRetCheckInfo.NOSTATE;
	    			 if (reportToStatesMap.get(param.getReportPK())!=null) {
	    				 approveStatus = reportToStatesMap.get(param.getReportPK());
					 }
	    			  if((approveStatus.equals(IPfRetCheckInfo.NOSTATE))&&(repDataCommit == null || (repDataCommit.getCommit_state() !=CommitStateEnum.COMMITED && repDataCommit.getCommit_state() != CommitStateEnum.AFFIRMED))){
	    				  RepDataAuthType auth = RepDataAuthUtil.getAuthType(AuditUtil.getCurrentUser(), param.getReportPK(), param.getPubData().getUnitPK(), param.getRepMngStructPK(),param.getRepOrgPK(),param.getTaskPK());
  			            //有报表数据权限才能导入
  				        if(auth == RepDataAuthType.EDIT){
  				        	//分布式报表数据不能导入
  				        	if(repDataCommit != null && repDataCommit.getDataorigin() != null){
  		    				  repState = getNewImpRepStateVO(param, task);
  		    				  repState.putDistState(param.getReportPK(), repDataCommit.getDataorigin());
  				        	}else{
		  				      	MeasurePubDataBO_Client.getAloneID(param.getPubData());
		  			        	MeasurePubDataVO curPubData = param.getPubData();

		  			        	MeasurePubDataVO newPubData=MeasurePubDataBO_Client.findByAloneID(report.getPk_key_comb(),strImportAloneID);
		  			        	curPubData.setAccSchemePK(param.getPubData().getAccSchemePK());
		  			        	newPubData.setAccSchemePK(param.getPubData().getAccSchemePK());
		  			        	context.setAttribute(MEASURE_PUB_DATA_VO, newPubData);
		  			        	importCellsModel = loadCellsModel(context);
		  			        	context.setAttribute(MEASURE_PUB_DATA_VO, curPubData);
		  			        	//修改cellsModel的关键字为原来的关键字值
		  			        	CellsModel fmtModel = DynAreaUtil.getDataModelWithExModel(importCellsModel);
		  			        	MeasurePubDataUtil.initMeasurePubDataKeyData(new MeasurePubDataVO[]{curPubData});
		  			        	CellsModelOperator.setMainKeyData(fmtModel, importCellsModel, curPubData);
  				        	}
  				        }else{
		    				  repState = getNewImpRepStateVO(param, task);
		    				  repState.putRepAuth(param.getReportPK(), auth);
		    			  }
	    			  }else{
	    				  if (repDataCommit.getCommit_state() ==CommitStateEnum.COMMITED || repDataCommit.getCommit_state() == CommitStateEnum.AFFIRMED) {
	    					  repState = getNewImpRepStateVO(param, task);
		    				  repState.putRepCommitState(param.getReportPK(), repDataCommit.getCommit_state());
						  }else {
							  repState = getNewImpRepStateVO(param, task);
		    				  repState.putApproveStatus(param.getReportPK(), approveStatus);
						  }
	    				  
	    			  }
			} else {
				repState = getNewImpRepStateVO(param, task);
//				if (task.getApprovemode() != null && task.getApprovemode() == TaskVO.REPORTAPPROVEMODE) {
//					repState.setApproveState(IPfRetCheckInfo.COMMIT);
//				} else {
					repState.setApproveState(taskApprove.getVbillstatus());
//				}
			}
	        	return  new TwoTuple<String, CellsModel>(repState == null ? null : repState.getMsg(), importCellsModel);
	        }
	}

	/**
	 * 导入单表IUFO数据
	 * @create by wuyongc at 2012-2-14,下午1:35:45
	 *
	 * @param p
	 * @return
	 * @throws Exception
	 */
	public String[] importSingleIUFOData(Object p) throws Exception{
		try{
			Object[] objs=(Object[])p;

			RepDataTaskParam[] params = (RepDataTaskParam[])objs[0];

			LoginEnvVO loginEnv = (LoginEnvVO)objs[1];

			String[][] strKeyVals = (String[][])objs[2];

			IApproveQueryService approveSrv = (IApproveQueryService)nc.bs.framework.common.NCLocator.getInstance().lookup(IApproveQueryService.class.getName());

			String[] aloneIds = new String[params.length];
			for (int i = 0; i < params.length; i++) {
				aloneIds[i] = params[i].getAloneID();
			}
			// 前台保证传入的参数不为空.此处不做参数为空的校验.
			String taskPK = params[0].getTaskPK();
			String repPK = params[0].getReportPK();

			Map<String,Integer> approveMap = approveSrv.getTaskApproveStateMap(taskPK, aloneIds);
			
			Map<String, Integer> reportApproveMap = approveSrv.getReportApproveStateMap(repPK,aloneIds);
			//如果已经审批则不需要检验是否上报...此处待优化
			Map<String,RepDataCommitVO>commitMap = CommitUtil.getRepCommitStateMap(aloneIds, repPK);

			Set<ImpRepStateVO> repStateSet = new HashSet<ImpRepStateVO>();

			
			for (int i = 0; i < params.length; i++) {
				RepDataTaskParam param = params[i];


				 Integer approveState = approveMap.get(param.getAloneID()+ TaskApproveVO.FLOWTYPE_COMMIT);
				//任务审批状态为自由态才能导入
				 if(approveState == null || approveState == IPfRetCheckInfo.NOSTATE){
					 RepDataCommitVO repCommit = commitMap.get(param.getAloneID());
					Integer commitState = repCommit == null ? null : repCommit.getCommit_state();
					Integer repApproveState = reportApproveMap.get(param.getAloneID()+ TaskApproveVO.FLOWTYPE_COMMIT);
					String dataOrigin = null;
					if (commitMap.get(param.getAloneID()) != null) {
						dataOrigin = commitMap.get(param.getAloneID()).getDataorigin();
					}
					if((repApproveState==null||repApproveState==IPfRetCheckInfo.NOSTATE)
							&&(commitState == null || (commitState !=CommitStateEnum.COMMITED && commitState != CommitStateEnum.AFFIRMED))){
						 RepDataAuthType auth = RepDataAuthUtil.getAuthType(AuditUtil.getCurrentUser(), param.getReportPK(), param.getPubData().getUnitPK(), param.getRepMngStructPK(),param.getRepOrgPK(),param.getTaskPK());
				            //有报表数据权限才能导入
					        if(auth == RepDataAuthType.EDIT){
	  				        	//分布式报表数据不能导入
	  				        	if(dataOrigin != null){
					            	ImpAloneIdStateVO repStateVO = getNewImpAloneIdStateVO(param, param.getTask(),repPK);
					            	repStateVO.putDistState(param.getAloneID(), dataOrigin);
					   			 	repStateSet.add(repStateVO);
	  				        	}else{
									 //获得报表数据的AloneID
									ReportVO report= UFOCacheManager.getSingleton().getReportCache().getByPK(param.getReportPK());
									KeyGroupVO keyGroup= UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(report.getPk_key_comb());
							        String strImportAloneID = TableInputHandlerHelper.doGetNewAloneID(param,loginEnv,keyGroup.getKeys(),strKeyVals[i]);
							        UfoContextVO context= null;

							        String strCurAloneID=param.getAloneID();


								    //如果要导入的关键字条件与当前关键字条件相同，给出出错提示
							        if(strImportAloneID != null && strImportAloneID.equals(strCurAloneID)){
							        	throw new TableInputException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0959")/*@res "不能导入当前关键字组合的数据"*/);  //"关键字组合"
							        } else{
							        	MeasurePubDataBO_Client.getAloneID(param.getPubData());
							        	MeasurePubDataVO curPubData = param.getPubData();

						        		MeasurePubDataVO oldPubData=MeasurePubDataBO_Client.findByAloneID(report.getPk_key_comb(),strImportAloneID);
							        	curPubData.setAccSchemePK(param.getPubData().getAccSchemePK());
							        	oldPubData.setAccSchemePK(param.getPubData().getAccSchemePK());
							        	//TODO
							        	RepDataTaskParam oldParam = (RepDataTaskParam) param.clone();
										oldParam.setPubData(oldPubData);
										oldParam.setAloneID(strImportAloneID);
							        	
							        	UfoContextVO oldContext = TableInputHandlerHelper.getContextVO(oldParam,loginEnv,false);
							        	oldContext.setAttribute(MEASURE_PUB_DATA_VO, oldPubData);
							        	CellsModel importCellsModel = loadCellsModel(oldContext);
							      
							        	context = TableInputHandlerHelper.getContextVO(param,loginEnv,false);
							        	context.setAttribute(MEASURE_PUB_DATA_VO, curPubData);
							        	//修改cellsModel的关键字为原来的关键字值
							        	CellsModel fmtModel = DynAreaUtil.getDataModelWithExModel(importCellsModel);
							        	MeasurePubDataUtil.initMeasurePubDataKeyData(new MeasurePubDataVO[]{curPubData});
							        	CellsModelOperator.setMainKeyData(fmtModel, importCellsModel, curPubData);

							        	CellsModelOperator.saveDataToDB(importCellsModel, context);

										String userId = (String) context.getAttribute(CUR_USER_ID);
										// 添加录入审计信息
									    ICommitManageService commitSrv=NCLocator.getInstance().lookup(ICommitManageService.class);
								        commitSrv.addRepInputSate(param.getTaskPK(), strCurAloneID, param.getReportPK(),
								        		userId, true, null);
							        }
	  				        	}
				            }else{
				            	ImpAloneIdStateVO repStateVO = getNewImpAloneIdStateVO(param, param.getTask(),repPK);
				            	repStateVO.putRepAuth(param.getAloneID(), auth);
				   			 	repStateSet.add(repStateVO);
				            }
					}else{
						if (commitState==CommitStateEnum.COMMITED || commitState== CommitStateEnum.AFFIRMED) {
							ImpRepStateVO repStateVO = getNewImpAloneIdStateVO(param, param.getTask(),repPK);
			            	repStateVO.putRepCommitState(param.getAloneID(), commitState);
			   			 	repStateSet.add(repStateVO);
						} else {
							ImpRepStateVO repStateVO = getNewImpAloneIdStateVO(param, param.getTask(),repPK);
							repStateVO.putApproveStatus(repPK,repApproveState);
							repStateSet.add(repStateVO);	
						}
		            
		            	
		            }
				 }else{
		            	ImpRepStateVO repStateVO = getNewImpAloneIdStateVO(param, param.getTask(),repPK);
		            	repStateVO.setApproveState(approveState);
		   			 	repStateSet.add(repStateVO);
		            }
			}
			String[] msg = new String[repStateSet.size()];

			int i = 0 ;
			for (ImpRepStateVO state : repStateSet) {
				msg[i] = state.getMsg();
			}
			return msg;

		}catch(Exception e){
			AppDebug.debug(e);
			throw e;
		}
	}

	public static UfoContextVO getContextVO(TaskRepDataParam oRepDataParam,LoginEnvVO loginEnv ,boolean bCreateMeasPubData,String repPK) throws TableInputException{
		oRepDataParam.setReportPK(repPK);
		return TableInputHandlerHelper.getContextVO(oRepDataParam, loginEnv, bCreateMeasPubData);
	}

	/**
	 * 不同的期间处理导入excel
	 * @create by wuyongc at 2012-2-8,上午10:56:39
	 *
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public String[] importExcelDataByMultiKeygroupVal(Object param) throws Exception{
		List<Object[]> list = (List)param;
		List<String> errMsg = new ArrayList<String>();
		List<ImpRepStateVO> stateList = new ArrayList<ImpRepStateVO>();
		for(Object[] params : list){
			UfoContextVO context=TableInputHandlerHelper.getContextVO((IRepDataParam)params[0],(LoginEnvVO)params[1]);
			//TODO? 是否存在PubDataVO 没有存到数据库中的情况，如果存在，则需要加上下面注释掉的这行代码，待确认。
//			MeasurePubDataBO_Client.createFilterMeasurePubDatas(new MeasurePubDataVO[] { context.getPubDataVO() });

			TwoTuple<List<Object[]>,Set<ImpRepStateVO>> twoTuple =  validateImpEnabled((IRepDataParam)params[0],(List<Object[]>)params[2],(TaskVO)params[4]);
			List<Object[]> objs = twoTuple.first;

			Set<ImpRepStateVO> repStateList = twoTuple.second;
	        String strErrMsg = processImportData((IRepDataParam)params[0], (LoginEnvVO)params[1], objs,
	        		((Boolean)params[3]).booleanValue(), context);

	        stateList.addAll(repStateList);

		}
		for(ImpRepStateVO repState : stateList){
        	errMsg.add(repState.getMsg());
        }
		return errMsg.toArray(new String[0]);
	}


	/**
	 *  此方法没有重写父类的innerImportExcelData
	 * @create by wuyongc at 2012-3-12,下午4:23:50
	 *
	 * @param param
	 * @param loginEnv
	 * @param listImportInfos
	 * @param isAutoCalc
	 * @param task
	 * @return
	 * @throws Exception
	 */

	public RepDataOperResultVO innerImportExcelData(IRepDataParam param,LoginEnvVO loginEnv,List<Object[]> listImportInfos,boolean isAutoCalc,TaskVO task) throws Exception{
		UfoContextVO context=TableInputHandlerHelper.getContextVO(param,loginEnv);
		TwoTuple<List<Object[]>,Set<ImpRepStateVO>> twoTuple = validateImpEnabled(param, listImportInfos, task);

		//导入的时候可能没有PubDataVO
		MeasurePubDataBO_Client.createFilterMeasurePubDatas(new MeasurePubDataVO[] { context.getPubDataVO() });

		List<Object[]> importInfos = twoTuple.first;
		Set<ImpRepStateVO> repStateSet = twoTuple.second;

		StringBuilder msg = new StringBuilder();
        for(ImpRepStateVO repState : repStateSet){
        	if(msg.length() != 0)
        		msg.append(ImpRepStateVO.ENTER_CHAR);
        	msg.append(repState.getMsg());
        }

        String strErrMsg = processImportData(param, loginEnv, importInfos,
				isAutoCalc, context);


        RepDataOperResultVO result=new RepDataOperResultVO();
        if (strErrMsg.length()>0){
        	if(msg.length() != 0){
        		msg.append(ImpRepStateVO.ENTER_CHAR);
        	}
        	msg.append(strErrMsg);
        	result.setHintMessage(msg.toString());

        	ReportFormatSrv repFormatSrv=new ReportFormatSrv(context,true);
        	CellsModel cellsModel=repFormatSrv.getCellsModel();
        	result.setCellsModel(cellsModel);
        	result.setOperSuccess(false);
        }else{
        	ReportFormatSrv repFormatSrv=new ReportFormatSrv(context,true);
        	CellsModel cellsModel=repFormatSrv.getCellsModel();
        	result.setCellsModel(cellsModel);
        	if(msg.length() != 0){
        		result.setHintMessage(msg.toString());
        	}
        }

		return result;
	}

	/**
	 * @create by wuyongc at 2012-3-8,上午9:03:51
	 *
	 * @param param
	 * @param listImportInfos
	 * @return
	 * @throws UFOSrvException
	 * @throws Exception
	 */
	private TwoTuple<List<Object[]>,Set<ImpRepStateVO>> validateImpEnabled(IRepDataParam param,
			List<Object[]> listImportInfos,TaskVO task) throws UFOSrvException, Exception {
		Set<ImpRepStateVO> repStateSet = new HashSet<ImpRepStateVO>();

		ReportCache repCache = IUFOCacheManager.getSingleton().getReportCache();
		List<Object[]> importInfos = new ArrayList<Object[]>();
		 int nValidSheetSize = listImportInfos.size();
		 List<String> repPkList = new ArrayList<String>();
		 IApproveQueryService approveSrv = (IApproveQueryService)nc.bs.framework.common.NCLocator.getInstance().lookup(IApproveQueryService.class.getName());
		 //wangqi 20130109  取上报审批状态
		 TaskApproveVO taskApprove =null;
    	// boolean reportFlag = false;
    	 if (task.getApprovemode() == null || task.getApprovemode()==TaskVO.TASKAPPROVEMODE) {
    		 taskApprove = approveSrv.getTaskApprove(param.getTaskPK(),param.getAloneID(),TaskApproveVO.FLOWTYPE_COMMIT);
		}
//    	 else {
//			 reportFlag = approveSrv.isAllApproveInOneStatus(param.getTaskPK(),param.getAloneID(),TaskApproveVO.FLOWTYPE_COMMIT, param.getPubData().getUnitPK(),IPfRetCheckInfo.NOSTATE);
//		}

    	TaskApproveVO[] taskApproveVOs = approveSrv.getAllApproveVOInTask(param.getTaskPK(),param.getAloneID(),TaskApproveVO.FLOWTYPE_COMMIT);
		HashMap<String,Integer> reportToStatesMap=new HashMap<String,Integer>();
		if (taskApproveVOs != null && taskApproveVOs.length > 0) {				
			for (int i = 0; i < taskApproveVOs.length; i++) {
				Vector<String> repPkVector = ((ApproveReportSet)(taskApproveVOs[i].getPk_report())).getReportPKs();
				for (String reppk:repPkVector) {
					reportToStatesMap.put(reppk, taskApproveVOs[i].getVbillstatus());
				}
			}
		}
    	 
		 //任务审批状态为自由态才能导入
		 if(taskApprove == null ? true:taskApprove.getVbillstatus() == IPfRetCheckInfo.NOSTATE){
		        for(int i = 0 ; i < nValidSheetSize; i++){
		            Object[] objImportInfos = listImportInfos.get(i);//{sheetname,repcode,dynendrow,cellsModel}
		            String strRepCode = (String)objImportInfos[1];
		            repPkList.add(repCache.getRepPKByCode(strRepCode));
		        }
		        // 一次取出上报状态
		        RepDataCommitVO[] repCommits = CommitUtil.getReportCommitState(param.getAloneID(),param.getTaskPK(), repPkList.toArray(new String[0]));
		        Map<String,Integer> repCommitMap = new HashMap<String,Integer>();
		        Map<String,String> repDistMap = new HashMap<String,String>();
		        //上报状态放入Map中
		        for(RepDataCommitVO commit : repCommits){
		        	repCommitMap.put(commit.getPk_report(), commit.getCommit_state());
		        	repDistMap.put(commit.getPk_report(), commit.getDataorigin());
		        }

		        for(int i = 0 ; i < nValidSheetSize; i++){
		            Object[] objImportInfos = listImportInfos.get(i);//{sheetname,repcode,dynendrow,cellsModel}
		            String strRepCode = (String)objImportInfos[1];
		            String repPK = repCache.getRepPKByCode(strRepCode);
		            Integer commitStatus = repCommitMap.get(repPK);
		            String dataorigin = repDistMap.get(repPK);
		            Integer approveStatus = IPfRetCheckInfo.NOSTATE;
		            if (reportToStatesMap.get(repPK)!=null) {
		            	approveStatus = reportToStatesMap.get(repPK);
					}
		            
		            //没有上报才能导入
		            if((approveStatus==null||approveStatus.equals(IPfRetCheckInfo.NOSTATE))&&(commitStatus == null || (commitStatus !=CommitStateEnum.COMMITED && commitStatus != CommitStateEnum.AFFIRMED))){
				        RepDataAuthType auth = RepDataAuthUtil.getAuthType(AuditUtil.getCurrentUser(), repCache.getRepPKByCode(strRepCode), param.getPubData().getUnitPK(), param.getRepMngStructPK(),param.getRepOrgPK(),param.getTaskPK());
			            //有报表数据权限才能导入
				        if(auth == RepDataAuthType.EDIT){
				        	//分布式数据不能导入
				        	if (dataorigin == null)
				        		importInfos.add(objImportInfos);
				        	else{
				            	ImpRepStateVO repStateVO = getNewImpRepStateVO(param, task);
				            	repStateVO.putDistState(repPK, dataorigin);
				   			 	repStateSet.add(repStateVO);
				        	}
			            }else{
			            	ImpRepStateVO repStateVO = getNewImpRepStateVO(param, task);
			            	repStateVO.putRepAuth(repPK, auth);
			   			 	repStateSet.add(repStateVO);
			            }
				} else {
					if (commitStatus ==CommitStateEnum.COMMITED || commitStatus == CommitStateEnum.AFFIRMED) {
						ImpRepStateVO repStateVO = getNewImpRepStateVO(param, task);
						repStateVO.putRepCommitState(repPK, commitStatus);
						repStateSet.add(repStateVO);
					} else {
						ImpRepStateVO repStateVO = getNewImpRepStateVO(param, task);
						repStateVO.putApproveStatus(repPK,approveStatus);
						repStateSet.add(repStateVO);
					}
				 }
		        }
		 }else{
			 ImpRepStateVO repStateVO = getNewImpRepStateVO(param, task);
//			if (task.getApprovemode() != null && task.getApprovemode() == TaskVO.REPORTAPPROVEMODE) {
//				repStateVO.setApproveState(IPfRetCheckInfo.COMMIT);
//			} else {
				repStateVO.setApproveState(taskApprove.getVbillstatus());
//			}
			 repStateSet.add(repStateVO);
		 }
		return new TwoTuple<List<Object[]>,Set<ImpRepStateVO>>(importInfos,repStateSet);
	}

	/**
	 * @create by wuyongc at 2012-3-12,下午4:44:13
	 *
	 * @param param
	 * @param task
	 * @return
	 */
	private ImpRepStateVO getNewImpRepStateVO(IRepDataParam param, TaskVO task) {
		return new ImpRepStateVO(task,param.getPubData().getKeywords());
	}



	private ImpAloneIdStateVO getNewImpAloneIdStateVO(IRepDataParam param, TaskVO task,String repPK){
		ImpAloneIdStateVO impStateVO = new ImpAloneIdStateVO(task,param.getPubData().getKeywords());
		impStateVO.setRepPK(repPK);
		return impStateVO;
	}
	/**
	 * @create by wuyongc at 2012-2-8,上午11:04:17
	 *
	 * @param param
	 * @param loginEnv
	 * @param listImportInfos
	 * @param isAutoCalc
	 * @param context
	 * @return
	 * @throws BusinessException
	 * @throws CommonException
	 */
	private String processImportData(IRepDataParam param, LoginEnvVO loginEnv,
			List<Object[]> listImportInfos, boolean isAutoCalc,
			UfoContextVO context) throws CommonException, BusinessException {
		String strTaskPK =param.getTaskPK();
        DataSourceVO dataSource = loginEnv.getDataSource();
        MeasurePubDataVO pubdataVo = context.getPubDataVO();

        //导入对话框增加是否自动计算选项，此处暂时设置为自动计算 chxw 2007-09-26
        MultiSheetImportUtil importUtil = getImportUtilBase(strTaskPK, param.getReportPK(),null, dataSource, pubdataVo,param.getRepOrgPK(),param.getRepMngStructPK(), param.getCurGroupPK(),isAutoCalc, loginEnv.getCurLoginDate());

        //将导入Excel准备数据信息(CellsModel),导入到数据库
        boolean isNeedSave = true;
        ImportExcelDataBizUtil.processImportData(importUtil, listImportInfos, isNeedSave,context);
        String strErrMsg=importUtil.getLog()==null?"":importUtil.getLog().getResult();
		return strErrMsg;
	}


	protected void processMenuState(MenuStateData menuData,IRepDataParam param,LoginEnvVO loginEnv) throws Exception{
		MeasurePubDataVO pubData=param.getPubData();
		if (menuData!=null){
			menuData.setHasTaskCheckFormula(IUfoInputActionUtil.isExistTaskCheckFormulas(param.getTaskPK()));

			TaskVO task=TaskSrvUtils.getTaskVOById(param.getTaskPK());
			MenuStateData taskCommitState=getTaskMenuState(pubData, task,loginEnv);;
			menuData.setCommitstatus(taskCommitState.getCommitstatus());
			menuData.setCommited(taskCommitState.isCommited());
			menuData.setCanCommit(taskCommitState.isCanCommit());
			menuData.setCanRequestCancelCommit(taskCommitState.isCanRequestCancelCommit());

			//追加审批状态 wangqi
			//wangqi 20130109  据条件取上报审批状态或确认审批状态
			Integer flowtype;
			if (menuData.getCommitstatus().intValue() < CommitStateEnum.STATE_COMMITED.getIntValue()) {
				flowtype = TaskApproveVO.FLOWTYPE_COMMIT;
			} else {
				flowtype = TaskApproveVO.FLOWTYPE_AFFIRM;
			}
			IApproveQueryService approveQuerySrv=NCLocator.getInstance().lookup(IApproveQueryService.class);

			Integer approvemode = task.getApprovemode();
			if (approvemode == null||approvemode==TaskVO.TASKAPPROVEMODE) {	
				TaskApproveVO taskapprovevo = approveQuerySrv.getTaskApprove(task.getPk_task(),pubData.getAloneID(),flowtype);
				if (taskapprovevo != null) {
				menuData.setApprovestatus(taskapprovevo.getVbillstatus().intValue());
				}
			}
//			else {
//				int approvestate = approveQuerySrv.getTaskReportStatus(task.getPk_task(),pubData.getAloneID(),flowtype,param.getRepOrgPK());
//				menuData.setApprovestatus(approvestate);
//			}
			
			// 分布式数据来源相关 qugx
			ICommitQueryService commitQuerySrv = NCLocator.getInstance().lookup(ICommitQueryService.class);
			RepDataCommitVO[] repDataCmtVos = commitQuerySrv.getReportCommitState(new String[]{pubData.getAloneID()}, param.getReportPK());
			   if (repDataCmtVos != null && repDataCmtVos.length > 0){
			    menuData.setDisTrans(repDataCmtVos[0].getDataorigin() == null? false : true);
			}
		}
	}

	@Override
	public RepDataOperResultVO innerOpenRepData(IRepDataParam param,LoginEnvVO loginEnv,String strBalCondPK) throws Exception{
		int iRepDataAuthType = 0;
		//判断是否需要进行权限控制 add by jiaah at 20110615
		boolean bControlAuth = RepDataAuthUtil.isNeedControlDataAuth(param.getTaskPK());
		if(!bControlAuth){
			iRepDataAuthType = IUfoContextKey.RIGHT_DATA_WRITE;
		}
		else{
			iRepDataAuthType = getRepDataRight(param);
			if(iRepDataAuthType == IUfoContextKey.RIGHT_DATA_NULL)
		           throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1145")/*@res "无查看当前报表的数据权限"*/);
		}

		MeasurePubDataVO pubData=param.getPubData();
        UfoContextVO context=TableInputHandlerHelper.getContextVO(param,loginEnv);
        CellsModel cellsModel=null;
        if (strBalCondPK==null || strBalCondPK.equals(BalanceCondVO.NON_SW_DATA_COND_PK)){
        	//存储没有保存的MeasurePubData
//        	MeasurePubDataBO_Client.getAloneID(param.getPubData());
        	//将MeasurePubData放入到context中，创建数据模型service时，会从上下文中取MeasurePubData，如果取不到，会只加载数据模型。
        	context.setAttribute(MEASURE_PUB_DATA_VO, param.getPubData());
        	ReportFormatSrv repFormatSrv=new ReportFormatSrv(context,true);
        	cellsModel=repFormatSrv.getCellsModel();
        	BalanceReportExportUtil.dealPrintSetForBalance(cellsModel,null);
        }else{
        	BalanceCondVO balanceCond=BalanceBO_Client.loadBalanceCondByPK(strBalCondPK);
        	RepDataVO repData=BalanceBO_Client.doSwBalance(pubData, balanceCond,param.getReportPK(), param.getRepMngStructPK());
        	cellsModel=CellsModelOperator.getFormatModelByPK(context);
        	cellsModel=CellsModelOperator.doGetDataModelFromRepDataVO(cellsModel, repData, context);
        	BalanceReportExportUtil.processBalanceRepCellsModel(new ReportFormatSrv(context,cellsModel),false,balanceCond);
            //weixl
        	pubData.setVer(0);
            pubData.setAloneID(MeasureDataUtil.getAloneID(pubData));
            //针对舍位情况，处理打印设置
            BalanceReportExportUtil.dealPrintSetForBalance(cellsModel,balanceCond);
        }

		RepDataOperResultVO result=new RepDataOperResultVO();
		result.setCellsModel(cellsModel);
		result.setMenuState(TableInputHandlerHelper.getMenuStateData(context, param, loginEnv, iRepDataAuthType));

		boolean bIsCanInput = UfoEFormulaEditControl.isFormulaEdit(param.getReportPK(),param.getTaskPK());
		result.setFmlCanInput(bIsCanInput);

		MenuStateData menuData=result.getMenuState();
		processMenuState(menuData,param,loginEnv);

		return result;
	}
	
	protected int getRepDataRight(IRepDataParam param) throws Exception{
		return RepDataAuthUtil.getAuthType(param.getOperUserPK(),param.getReportPK(), param.getPubData().getUnitPK(),param.getRepMngStructPK(),param.getRepOrgPK(), param.getTaskPK()).ordinal();
	}

	@Override
	public TraceDataResult innerTraceData(IRepDataParam param,LoginEnvVO loginEnv,CellsModel cellsModel,ITraceDataParam traceParam,int iPort) throws Exception{
		UfoContextVO context=TableInputHandlerHelper.getContextVO(param,loginEnv);
        ReportFormatSrv reportFormatSrv = new ReportFormatSrv(context,cellsModel);
   	 	return TableInputHandlerHelper.loadTraceDatas(param.getTaskPK(),reportFormatSrv, traceParam,loginEnv,iPort,false);
	}

	public TraceDataResult innerTraceTotalData(IRepDataParam param,LoginEnvVO loginEnv,CellsModel cellsModel,ITraceDataParam traceParam,TotalSchemeVO totalscheme,String busiTime) throws Exception{
		UfoContextVO context=TableInputHandlerHelper.getContextVO(param,loginEnv);
        ReportFormatSrv reportFormatSrv = new ReportFormatSrv(context,cellsModel);
   	 	return IUfoTableInputHandlerHelper.loadTraceDatas(param,reportFormatSrv, traceParam,loginEnv,0,true,totalscheme,busiTime);
	}

    public static MultiSheetImportUtil getImportUtilBase(String strTaskPK,String strRepPK,String strUserPK, DataSourceVO dataSource, MeasurePubDataVO pubdataVo,String strOrgPK,String strRmsPK,String strGroupPK,boolean bAutoCalc,String strLoginDate) {
    	return new IUFOMultiSheetImportUtil(strTaskPK,strRepPK,null,null,null,pubdataVo,strUserPK,dataSource,strOrgPK,strRmsPK,strGroupPK,bAutoCalc,strLoginDate);
    }

	@Override
	public ChooseRepData[] innerLoadTableImportReps(IRepDataParam param) throws Exception{
		return TableInputHandlerHelper.geneChooseRepDatas(TaskSrvUtils.getReportByTaskId(param.getTaskPK()));
	}

	@Override
	protected RepDataOperResultVO innerDoSaveRepData(CellsModel cellsModel,UfoContextVO context,IRepDataParam param) throws Exception{
		if (IUfoInputActionUtil.isRepCommit(param.getReportPK(), param.getAloneID())){
			throw new UfoException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1146")/*@res "报表数据已经上报，无法保存"*/);
		}

		MeasurePubDataVO pubData=param.getPubData();
        if (pubData.getUnitPK()!=null && pubData.getUnitPK().trim().length()>0){
        	int iRepDataAuthType = 0;
    		//判断是否需要进行权限控制 add by jiaah at 20110615
    		boolean bControlAuth = RepDataAuthUtil.isNeedControlDataAuth(param.getTaskPK());
    		if(!bControlAuth){
    			iRepDataAuthType = IUfoContextKey.RIGHT_DATA_WRITE;
    		}
    		else{
    			iRepDataAuthType=RepDataAuthUtil.getAuthType(param.getOperUserPK(),param.getReportPK(), param.getPubData().getUnitPK(),param.getRepMngStructPK(),param.getRepOrgPK(), param.getTaskPK()).ordinal();
            	if (iRepDataAuthType<IUfoContextKey.RIGHT_DATA_WRITE){
            		throw new UfoException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1147")/*@res "用户没有修改该单位报表数据的权限"*/);
            	}
    		}
        }

        putExtModel(cellsModel, param);
        
		CellsModel dataModel=cellsModel;
        ReportFormatSrv reportFormatSrv = new ReportFormatSrv(context,dataModel);
        reportFormatSrv.saveReportData();

        RepDataOperResultVO resultVO=new RepDataOperResultVO();
        
       
        resultVO.setCellsModel(cellsModel);
        resultVO.setOperSuccess(true);
        // @edit by wuyongc at 2011-6-18,下午03:59:27
        // 取得个性化中心的 是否自动审核 设置
        UFBoolean ufboolean = UfobIndividualSettingUtil.getAutoCheck();
        //如果没有个性化中心是否自动审核没有设置 则 取系统参数里的设置，否则取个性化中心的自动审核设置
        boolean autoCheck = (ufboolean == null && UfobSysParamQueryUtil.getAutoCheck()) || (ufboolean != null &&  ufboolean.booleanValue());
		// 20110120 liuchun 修改， 根据系统参数设置进行自动审核(如果存在审核公式的话)
        if(autoCheck && InputActionUtil.isExistRepCheckFormulas(param.getReportPK())){

        	TaskVO task = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(param.getTaskPK());
        	int checkWarn = task.getIswarnchecked();
        	boolean warnPass = getWarnPass(checkWarn);

            //进行表内审核并保存审核结果 ，并且审核完后更新模型中的审核结果状态。
        	CheckResultVO result=TableInputHandlerHelper.doCheckInRep(context, cellsModel, true,warnPass);
        	MeasurePubDataVO pub = param.getPubData();
        	KeyGroupVO keyGroupVO = pub.getKeyGroup();
        	String pk_org = "";
        	for (int i = 0; i < keyGroupVO.getKeys().length; i++) {
        		KeyVO key = keyGroupVO.getKeys()[i];
        		if(KeyVO.isUnitKeyVO(key)){
        			pk_org = pub.getKeywords()[i];
        		}
			}
        	result.setOrgId(pk_org);
        	resultVO.setCheckResult(result);
        }

        ICommitManageService commitSrv=NCLocator.getInstance().lookup(ICommitManageService.class);
        commitSrv.addRepInputSate(param.getTaskPK(), param.getAloneID(), param.getReportPK(), param.getOperUserPK(), true, param.getLastCalcTime());
        // @edit by wuyongc at 2013-6-19,上午10:00:42 最后再移除。。
        removeFiexedAttr(cellsModel);
		return resultVO;
	}
	public MenuStateData getTaskMenuState(ThreeTuple<MeasurePubDataVO, TaskVO, LoginEnvVO> threeTuple) throws Exception{
		return getTaskMenuState(threeTuple.first, threeTuple.second, threeTuple.third);
	}

	private MenuStateData getTaskMenuState(MeasurePubDataVO pubData,TaskVO task,LoginEnvVO loginEnv) throws Exception{
		MenuStateData menuData=new MenuStateData();
		//wangqi 20130111 增加报送状态到menuData中
		int commitstate = IUfoTableInputHandlerHelper.getTaskCommitState(pubData, task);
		boolean bCommited;
//		boolean bCommited=IUfoTableInputHandlerHelper.isTaskCommited(pubData, task);
		int commstrategyvalue = task.getCommstrategy().intValue();
		if (commstrategyvalue == ICommitConfigConstant.COMMIT_STRAGY_ALL) {
			bCommited = commitstate >= CommitStateEnum.STATE_COMMITED.getIntValue();
		} else {
			bCommited = commitstate > CommitStateEnum.STATE_COMMITED.getIntValue();
		}

		//wangqi 20110718
//		boolean bCancelCommited=IUfoTableInputHandlerHelper.isTaskCancelCommited(pubData, task);
		boolean bCancelCommited = commitstate >= CommitStateEnum.STATE_COMMITED.getIntValue();

		menuData.setCommitstatus(commitstate);
		menuData.setCommited(bCancelCommited);
		menuData.setCanRequestCancelCommit(IUfoTableInputHandlerHelper.isCanRequestCancel(pubData, task, loginEnv, bCancelCommited));
		menuData.setCanCommit(IUfoTableInputHandlerHelper.isCanTaskCommit(pubData, task, loginEnv,bCommited));
		// 分布式数据来源相关 qugx
		ICommitQueryService commitQuerySrv = NCLocator.getInstance().lookup(ICommitQueryService.class);
		TaskCommitVO[] taskCmts = commitQuerySrv.getTaskCommitsByAloneIDs(task.getPk_task(), new String[]{pubData.getAloneID()});
		if (taskCmts != null && taskCmts.length > 0){
			menuData.setDisTrans(taskCmts[0].getDataorigin() == null? false : true);
		}
		return menuData;
	}



	private static boolean getWarnPass(int checkWarn) throws Exception {
		boolean warnPass = true;
		switch(checkWarn){
			case TaskVO.WARN_NOT_CONTROL : warnPass = UfobSysParamQueryUtil.getCheckPassOnAlarm(); break;
			case TaskVO.WARN_NOT_PASS : warnPass = false;break;
			case TaskVO.WARN_PASS : warnPass = true;break;
		}
		return warnPass;
	}

	public List<RepDataQueryResultVO> getRepDataQueryResultVO(Object param) throws Exception{
		Object[] params = (Object[]) param;
		String aloneID = (String) params[0];
		String[] repIDs = (String[]) params[1];
		String pk_task = (String) params[2];
		IRepDataInfoQuerySrv repDataQry = (IRepDataInfoQuerySrv) NCLocator.getInstance().lookup(IRepDataInfoQuerySrv.class.getName());
		List<RepDataQueryResultVO> vInputResult=repDataQry.queryRepDataInfo(new String[]{aloneID}, repIDs, -1, -1, -1);

		ICommitQueryService commitQry = NCLocator.getInstance().lookup(ICommitQueryService.class);
		TaskCommitVO[] taskCommitVOs = commitQry.getTaskCommitsByAloneIDs(pk_task, new String[]{aloneID});

		if (vInputResult != null && vInputResult.size() > 0 &&
				taskCommitVOs != null && taskCommitVOs.length > 0) {
			for(RepDataQueryResultVO repDataQryVo : vInputResult){
				repDataQryVo.setTaskcommitstate(taskCommitVOs[0].getCommit_state());
			}
		}

		if ((vInputResult == null || vInputResult.size() == 0) &&
				(taskCommitVOs != null && taskCommitVOs.length > 0)) {
			vInputResult = new ArrayList<RepDataQueryResultVO>();
			RepDataQueryResultVO vo = new RepDataQueryResultVO();
			vo.setRepcommitstate(CommitStateEnum.STATE_NOCOMMIT.getIntValue());
			vo.setTaskcommitstate(taskCommitVOs[0].getCommit_state());
			vInputResult.add(vo);
		}

		return vInputResult;
	}

	/**
	 * 根据参数取得报表的报送状态，轻量级封装
	 *
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<RepCommitStateVO> getRepCommitStateVO(Object param) throws Exception{
		List<RepDataQueryResultVO> repQueryResults = getRepDataQueryResultVO(param);
		List<RepCommitStateVO> repCommitList = new ArrayList<RepCommitStateVO>();;
		if(repQueryResults != null && !repQueryResults.isEmpty()) {
			for(RepDataQueryResultVO result : repQueryResults) {
				RepCommitStateVO commitState = new RepCommitStateVO();
				commitState.setPk_report(result.getPk_report());
				commitState.setRepcommitstate(result.getRepcommitstate());
				commitState.setTaskcommitstate(result.getTaskcommitstate());

				repCommitList.add(commitState);
			}
		}

		return repCommitList;
	}

	/**
	 * 
	 * 返回需要更新的报表pk数组Object[1]，用于更新前台报表格式缓存。
	 * Object[2]为更新的提示信息，或者其它提示信息。
	 * @create by wuyongc at 2013-6-5,下午1:00:00
	 *
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Object[] impExcel4Reps(Object param)throws Exception{
		Object[] params = (Object[]) param;
		byte[] bytes = (byte[]) params[0];
		String extendName = (String)params[2];
		UfoContextVO context = (UfoContextVO) params[3];
		int fmlType = (Integer)params[4];
		Workbook workBook = getWorkbook(bytes, extendName);
		Map<String,List<RepImpParam>> repListMap = (Map<String,List<RepImpParam>>)params[1];

		Set<Entry<String, List<RepImpParam>>> set = repListMap.entrySet();
		StringBuilder sb = new StringBuilder();
		List<String> updateRepPKs = new ArrayList<String>();
		
		Map<String,String> sheet2CodeMap = new HashMap<String,String>();
		for (Entry<String, List<RepImpParam>> entry : set) {
			List<RepImpParam> sheetNameList = entry.getValue();
			for (RepImpParam repImpParam : sheetNameList) {
				sheet2CodeMap.put(repImpParam.getSheetName(), repImpParam.getCode());
			}
		}
		for (Entry<String, List<RepImpParam>> entry : set) {
			List<RepImpParam> sheetNameList = entry.getValue();
//			if(true)
//				return;
			String[] obj = impExcel4Rep(sheet2CodeMap,entry.getKey(),workBook,sheetNameList,context,fmlType);
			String msg = obj[1];
			if(obj[0]!= null){
				updateRepPKs.add(obj[0]);
			}
			if(sb.length() == 0){
				sb.append(msg);
			}else{
				sb.append("\r\n").append(msg);
			}
		}
		return new Object[]{updateRepPKs.toArray(new String[0]),sb.toString()};
	}

	public String impExcel4XMLs(Object param) throws Exception{
		Object[] params = (Object[]) param;
		byte[][] bytes = (byte[][]) params[0];
		RepImpParam[] impPrams = (RepImpParam[]) params[1];
		UfoContextVO context = (UfoContextVO) params[2];

		String pk_user = InvocationInfoProxy.getInstance().getUserId();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			RepImpParam impPram = impPrams[i];
			RepCellsModel repCellsModel = getRepCellsModel(bytes[i]);
			String msg = impRepByXml(impPram,repCellsModel,context,pk_user);
			if(!msg.isEmpty()){
				if(sb.length() == 0)
					sb.append("\r\n");
				sb.append(msg);
			}
		}
		return sb.toString();
	}

	private String impRepByXml(RepImpParam impPram,RepCellsModel repCellsModel,UfoContextVO context,String pk_user) throws BusinessException{
		ReportVO repVO = IUFOCacheManager.getSingleton().getReportCache().getByCode(impPram.getCode());
		StringBuilder sb = new StringBuilder();
		if(repVO == null){
			//创建报表表样
			repVO = createRep(context, impPram, pk_user);
			DataSourceVO dataSource = (DataSourceVO)context.getAttribute(IUfoContextKey.DATA_SOURCE);
			RepFmtImpExpUtils.importFromXMLFile(repCellsModel, repVO, dataSource, true, context);
		}else{
			String[] taskPKs = TaskSrvUtils.getTaskIdsByReportId(repVO.getPk_report());
			if(taskPKs.length>0){
				if(sb.length() == 0){
					sb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0041")/*@res "报表编码["*/).append(impPram.getCode()).append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0042")/*@res "]已经加入到任务中，不能导入表样！"*/);
				}else{
					sb.append("\r\n");
					sb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0041")/*@res "报表编码["*/).append(impPram.getCode()).append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0042")/*@res "]已经加入到任务中，不能导入表样！"*/);
				}
				return sb.toString();
			}else{
				String pk_org = (String)context.getAttribute(IUfoContextKey.CUR_REPORG_PK);
				String pk_group = (String)context.getAttribute(IUfoContextKey.CUR_GROUP_PK);
				//其它判断TODO ?还有其它限制？
				if(!repVO.getPk_org().equals(pk_org) || !repVO.getPk_group().equals(pk_group)){
					if(sb.length() == 0){
						sb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0041")/*@res "报表编码["*/).append(impPram.getCode()).append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0043")/*@res "]不在当前组织或者集团中，不能导入表样！"*/);
					}else{
						sb.append("\r\n");
						sb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0041")/*@res "报表编码["*/).append(impPram.getCode()).append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0043")/*@res "]不在当前组织或者集团中，不能导入表样！"*/);
					}
					return sb.toString();
				}
			}
			updateRep(context, repVO, pk_user, impPram);
			DataSourceVO dataSource = (DataSourceVO)context.getAttribute(IUfoContextKey.DATA_SOURCE);
			RepFmtImpExpUtils.importFromXMLFile(repCellsModel, repVO, dataSource, true, context);
		}
		return sb.toString();
	}
	private RepCellsModel getRepCellsModel(byte[] bytes) throws BusinessException{
		InputStream is = new ByteArrayInputStream(bytes);
		XStream stream = new XStream(new StaxDriver());
		Object obj =  stream.fromXML(is);
		if(obj instanceof RepCellsModel){
			return (RepCellsModel)obj;
		}else if(obj instanceof TemplateSuite){
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0044")/*@res "导入XML，不能使用套表文件！"*/);
		}
		return null;
	}
	private String[] impExcel4Rep(Map<String,String> sheet2CodeMap,String repCode,Workbook workbook,List<RepImpParam>sheetNameList,UfoContextVO context,int fmlType) throws BusinessException{
		Sheet repSheet = null;
		Sheet measNameSheet = null;
		Sheet measFmtSheet = null;
		List<Sheet> fmlSheetList = new ArrayList<Sheet>();

		context.setAttribute(TS_REPORT_FORMAT, null);
		StringBuilder sb = new StringBuilder();
		Map<String,RepImpParam> paramMap = new HashMap<String,RepImpParam>();
		for (RepImpParam repImpParam : sheetNameList) {
			switch(repImpParam.getImpType()){
				case RepImpParam.REP_SHEET : repSheet = workbook.getSheet(repImpParam.getSheetName()); break;
				case RepImpParam.MEASURE_SHEET : {
					if(repImpParam.getImpDetailType() == ExcelImp4FmtParam.MEASURE_FMT)
						measFmtSheet = workbook.getSheet(repImpParam.getSheetName());
					else{
						measNameSheet = workbook.getSheet(repImpParam.getSheetName());
					}
				} break;
				case RepImpParam.FORMULA_SHEET : fmlSheetList.add(workbook.getSheet(repImpParam.getSheetName()));break;
			}
			paramMap.put(repImpParam.getSheetName(), repImpParam);
		}

		String pk_user = InvocationInfoProxy.getInstance().getUserId();
		ReportVO repVO = IUFOCacheManager.getSingleton().getReportCache().getByCode(repCode);
		if(repVO != null){
			String[] taskPKs = TaskSrvUtils.getTaskIdsByReportId(repVO.getPk_report());
			if(taskPKs.length>0){
				if(sb.length() == 0){
					sb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0041")/*@res "报表编码["*/).append(repCode).append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0045")/*@res "]已经加入到任务中，不能导入表样，指标或者公式！"*/);
				}else{
					sb.append("\r\n");
					sb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0041")/*@res "报表编码["*/).append(repCode).append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0045")/*@res "]已经加入到任务中，不能导入表样，指标或者公式！"*/);
				}
				return new String[]{null,sb.toString()};
			}else{
				String pk_org = (String)context.getAttribute(IUfoContextKey.CUR_REPORG_PK);
				String pk_group = (String)context.getAttribute(IUfoContextKey.CUR_GROUP_PK);
				//其它判断TODO ?还有其它限制？
				if(!repVO.getPk_org().equals(pk_org) || !repVO.getPk_group().equals(pk_group)){
					if(sb.length() == 0){
						sb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0041")/*@res "报表编码["*/).append(repCode).append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0046")/*@res "]不在当前组织或者集团中，不能导入表样，指标或者公式！"*/);
					}else{
						sb.append("\r\n");
						sb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0041")/*@res "报表编码["*/).append(repCode).append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0046")/*@res "]不在当前组织或者集团中，不能导入表样，指标或者公式！"*/);
					}
					return new String[]{null,sb.toString()};
				}
			}
			context.setAttribute(IUfoContextKey.REPORT_PK, repVO.getPk_report());
		}
		String updateRepPK = null;
		if(repSheet == null){//如果没有表样，则仅仅能更新。
			if(repVO != null){
		        // 将当前选择的repVO的修改时间放入上下文中,后续导入时会据此做版本验证,是否当前的repVO的格式已被他人修改
		        if(repVO.getModifiedtime()!=null)
		        	context.setAttribute(TS_REPORT_FORMAT, repVO.getModifiedtime().toStdString());
				updateRepFmt(sheet2CodeMap,workbook, context,repSheet, measNameSheet, measFmtSheet,
						fmlSheetList, paramMap, repVO,fmlType);
				updateRepPK = repVO.getPk_report();
			}else{
				if(sb.length() == 0){
					sb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0041")/*@res "报表编码["*/).append(repCode).append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0047")/*@res "]不存在，不能导入指标或者公式！"*/);
				}else{
					sb.append("\r\n");
					sb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0041")/*@res "报表编码["*/).append(repCode).append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0047")/*@res "]不存在，不能导入指标或者公式！"*/);
				}
			}
		}else{
			//如果有表样sheet导入，那么首先判断导入到的报表是否存在，如果存在则更新，否则创建报表表样，然后是设置指标，公式。
			repVO = IUFOCacheManager.getSingleton().getReportCache().getByCode(repCode);
			String sheetName = repSheet.getSheetName();
			if(repVO == null){
				RepImpParam param = paramMap.get(sheetName);
				//创建报表表样
				repVO = createRep(context, param, pk_user);
				context.setAttribute(IUfoContextKey.REPORT_PK, repVO.getPk_report());
			}else{
				RepImpParam param = paramMap.get(sheetName);
				//TODO 不能根据编码随意更新，有限制
				updateRep(context, repVO, pk_user, param);
				updateRepPK = repVO.getPk_report();
			}
//			CellsModel cellsModel = UfoExcelImpUtil.getCellsModelByExcel(repSheet, workbook, context, false);
			updateRepFmt(sheet2CodeMap,workbook, context,repSheet, measNameSheet, measFmtSheet,
					fmlSheetList, paramMap, repVO,fmlType);
		}
		return new String[]{updateRepPK,sb.toString()};
	}

	private void updateRep(UfoContextVO context, ReportVO repVO,
			String pk_user, RepImpParam param) throws DAOException {
		String repName = param.getName();
		IReportService repSrv = NCLocator.getInstance().lookup(IReportService.class);
		//同一目录内 报表表样名称不能重复。
		repName = repSrv.getNotExstRepName(repName, repVO.getPk_report(), repVO.getPk_dir());

		repVO.setName(repName);
		repVO.setIsintrade(UFBoolean.valueOf(param.isInnerTrade()));
		repVO.setFmledit_type(param.isFmlEdit()? ReportVO.FML_EDIT : ReportVO.FML_CAN_NOTEDIT);
		repVO.setModifiedtime(new UFDateTime(new Date(TimeService.getInstance().getTime())));
		repVO.setModifier(pk_user);
		String note = param.getNote();
		MultiLangTextUtil.setCurLangText(repVO, note,"note");
		// @edit by wuyongc at 2013-8-15,下午2:16:56 通过缓存管理器去更新报表表样vo，否则会造成缓存中的vo可能没有更新。。
		IUFOCacheManager.getSingleton().getReportCache().update(repVO);
//			try {
////				repSrv.updateRepBaseInfo(repVO);
//			} catch (UFOSrvException e) {
//				AppDebug.debug(e);
//			}
			  // 将当前选择的repVO的修改时间放入上下文中,后续导入时会据此做版本验证,是否当前的repVO的格式已被他人修改
		    if(repVO.getModifiedtime()!=null)
		    	context.setAttribute(TS_REPORT_FORMAT, repVO.getModifiedtime().toStdString());

	}

	private ReportVO createRep(UfoContextVO context,
			RepImpParam param, String pk_user)
			throws BusinessException, UFOSrvException {
		ReportVO repVO;
		repVO = new ReportVO();
		repVO.setKeymeasures(null);
		repVO.setPk_report(OIDMaker.getOID());

		repVO.setCreator(pk_user);
		repVO.setCreationtime(new UFDateTime(new Date(TimeService.getInstance().getTime())));
		String dirPK = (String) context.getAttribute(IUfoContextKey.DIR_PK);
		repVO.setPk_dir(dirPK);
		

		String repName = param.getName();
		IReportService repSrv = NCLocator.getInstance().lookup(IReportService.class);
		repName = repSrv.getNotExstRepName(repName, repVO.getPk_report(), dirPK);
		repVO.setName(repName);
		repVO.setIsintrade(UFBoolean.valueOf(param.isInnerTrade()));
		repVO.setFmledit_type(param.isFmlEdit()? ReportVO.FML_EDIT : ReportVO.FML_CAN_NOTEDIT);
		repVO.setModifiedtime(null);
		repVO.setModifier(null);
		repVO.setReptype(ReportVO.REPTYPE_UFOE);
		String pk_org = (String)context.getAttribute(IUfoContextKey.CUR_REPORG_PK);
		repVO.setPk_org(pk_org);
		String pk_group = (String)context.getAttribute(IUfoContextKey.CUR_GROUP_PK);
		repVO.setPk_group(pk_group);
		String code = param.getCode();
		repVO.setCode(code);
		String note = param.getNote();
		MultiLangTextUtil.setCurLangText(repVO, note,"note");
		BizlockDataUtil.lockDataByBizlock(repVO);// 加业务锁
		ValidationFailure failure = new BDUniqueRuleValidate().validate(repVO);// 唯一性校验
		if(failure !=null && StringUtils.isEmpty(StringUtils.trimToEmpty(failure.getMessage()))){
			throw new UFOSrvException(failure.getMessage());
		}
		String busiProp = (String) context.getAttribute(IUfoContextKey.CUR_BUSINESS_PROP);
		repVO.setBusi_prop(busiProp);

			String pk = repSrv.createReport(repVO);
			repVO = IUFOCacheManager.getSingleton().getReportCache().getByPK(pk);
		return repVO;
	}

	private void updateRepFmt(Map<String,String> sheet2CodeMap,Workbook workbook, UfoContextVO context,Sheet repSheet,
			Sheet measNameSheet, Sheet measFmtSheet, List<Sheet> fmlSheetList,
			Map<String, RepImpParam> paramMap, ReportVO repVO,int fmlType) {
		String repPK = repVO.getPk_report();
		context.setAttribute(REPORT_PK, repPK);
		context.setAttribute(KEYGROUP_PK, repVO.getPk_key_comb());
		context.setAttribute(REPORT_NAME, repVO.getChangeName());
		CellsModel	cellsModel = CellsModelOperator.getFormatModelByPK(context);

		KeywordModel keywordModel = KeywordModel.getInstance(cellsModel);
		FormulaModel formulaModel = FormulaModel.getInstance(cellsModel);
		UfoFmlExecutor fmlExecutor = UfoFmlExecutor.getInstance(context,
				cellsModel);

		String oldKeyGroupPK = keywordModel.getMainKeyCombPK();
		//editor tianjlc 2015-04-08 关键字存于表样中
		if(repSheet != null){
			addGroupPk2Rep(cellsModel, workbook, repSheet, oldKeyGroupPK);
		}else{
			if(oldKeyGroupPK == null || oldKeyGroupPK.equals(KeyGroupVO.NULL_KEYCOMB_PK)){
				addUnitKeyVO(keywordModel);
			}
		}
		fmlExecutor.getCalcEnv().setKeys(keywordModel.getMainKeyVO().toArray(new KeyVO[0]));

		formulaModel.setUfoFmlExecutor(fmlExecutor);
		
		
		//处理表样
		if(repSheet != null){
			cellsModel = UfoExcelImpUtil.getCellsModelByExcel(sheet2CodeMap,cellsModel,repSheet, workbook,fmlType);
		}
		//处理指标
		if(measFmtSheet != null){
			IDefdoclistQryService defdoclistQrySrv = NCLocator.getInstance().lookup(IDefdoclistQryService.class);
			try {


				DefdoclistVO[] defDocListVOs = defdoclistQrySrv.queryDefdoclistVOs();
				Map<String, String> code2PkMap = new HashMap<String, String>(defDocListVOs.length,1);
				for (DefdoclistVO defdoclistVO : defDocListVOs) {
					code2PkMap.put(defdoclistVO.getCode(),defdoclistVO.getPk_defdoclist());
				}
				if(measNameSheet != null){//更新指标名称，类型
					RepImpExpPubUtil.updateStoreCell(cellsModel, workbook, measFmtSheet, measNameSheet, code2PkMap, repPK, true);
				}else{//更新指标格式
					RepImpExpPubUtil.updateStoreCellFmt(cellsModel, workbook, measFmtSheet, code2PkMap, repPK, true);
				}

			} catch (BusinessException e) {
				AppDebug.debug(e);
			}
		}else if(measNameSheet != null){//更新存储单元，指标名称
			RepImpExpPubUtil.updateStoreCellName(cellsModel, workbook, measNameSheet, repPK, true);
		}

		CellsModelOperator.initModelProperties(context, cellsModel);
		for (Sheet fmlSheet : fmlSheetList) {
			//处理公式
			RepImpExpPubUtil.impFmlsByExcel(sheet2CodeMap,paramMap.get(fmlSheet.getSheetName()).getImpDetailType(), fmlSheet, workbook, cellsModel);
		}
		CellsModelOperator.saveReportFormat(context, cellsModel);
		RepFormatModel repFormatModel = (RepFormatModel) UFOCacheManager.getSingleton().getRepFormatCache().get(repVO.getPk_report());
		if(repFormatModel != null) {
			repFormatModel.setFormatModel(cellsModel);
			repFormatModel.setMainKeyCombPK(repVO.getPk_key_comb());
			repFormatModel.setReportPK(repVO.getPk_report());
		}
//		IUFOCacheManager.getSingleton().getRepFormatCache().update(repFormatModel);
	}
	/**
	 * 将报表中的关键字信息导入到报表中
	 * 
	 * @creator tianjlc at 2015-4-21 上午10:23:23
	 * @param cellsModel
	 * @param workBook
	 * @param sheet
	 * @param oldKeyGroupPK
	 * @return void
	 */
	public void addGroupPk2Rep(CellsModel cellsModel,Workbook workBook,Sheet sheet,String oldKeyGroupPK) {
		KeywordModel keywordModel=KeywordModel.getInstance(cellsModel);
		String[] keyPKs = RepImpExpPubUtil.getKeyPksByRepFmtSheet(cellsModel, workBook, sheet);
		if(keyPKs != null){
			KeyVO[] keyVOs = IUFOCacheManager.getSingleton().getKeywordCache().getByPKs(keyPKs);
			List<KeyVO> keyList = new ArrayList<KeyVO>();
			for (KeyVO keyVO : keyVOs) {
				if(keyVO != null){
					keyList.add(keyVO);
				}
			}
			if(keyList.isEmpty()){
				if(oldKeyGroupPK == null || oldKeyGroupPK.equals(KeyGroupVO.NULL_KEYCOMB_PK)){
					addUnitKeyVO(keywordModel);
				}
			}else{
				KeyGroupCache keyGroupCache = IUFOCacheManager.getSingleton().getKeyGroupCache();
				KeyGroupVO keygroup = keyGroupCache.getPkByKeyGroup(new KeyGroupVO(keyList.toArray(new KeyVO[0])));
				//当前没有关键字才设置，如果有关键字则不取Excel中的关键字组合pk
				if(oldKeyGroupPK == null || oldKeyGroupPK.equals(KeyGroupVO.NULL_KEYCOMB_PK)){
					if(keygroup == null) {
						keygroup = (KeyGroupVO)keyGroupCache.add(new KeyGroupVO(keyVOs))[0];
					}
					if(keygroup != null){
						for(KeyVO key : keygroup.getKeys()){
							keywordModel.getMainUndisplayKeys().add(key.getPk_keyword());
						}
						keywordModel.setMainKeyCombPK(keygroup.getKeyGroupPK());
					}else{
						addUnitKeyVO(keywordModel);
					}
				}
			}
			
		}else{
			if(oldKeyGroupPK == null || oldKeyGroupPK.equals(KeyGroupVO.NULL_KEYCOMB_PK)){
				addUnitKeyVO(keywordModel);
			}
		}
	}

	private void addUnitKeyVO(KeywordModel keywordModel) {
		KeyVO unitKey = UFOCacheManager.getSingleton().getKeywordCache().getByPK(KeyVO.CORP_PK);
		keywordModel.getMainUndisplayKeys().add(unitKey.getPk_keyword());
		KeyGroupVO keyGroupVO = new KeyGroupVO();
		KeyVO[] newKeyVOs = new KeyVO[]{unitKey};
		keyGroupVO.addKeyToGroup(newKeyVOs);

		KeyGroupVO keyGroupTemp = UFOCacheManager.getSingleton()
				.getKeyGroupCache().getPkByKeyGroup(keyGroupVO);
		if(keyGroupTemp!=null)
			keywordModel.setMainKeyCombPK(keyGroupTemp.getKeyGroupPK());
	}

	private Workbook getWorkbook(byte[] bytes, String extendName)
			throws IOException {
		InputStream is = new ByteArrayInputStream(bytes);
		boolean isExcel2007 = ImpExpFileNameUtil.isExcel2007ExtName(extendName);
		Workbook workBook = null;
		POIFSFileSystem fs = null;
		try{
			if(isExcel2007){
				try {
					workBook = new XSSFWorkbook(is);
				}catch(POIXMLException fileE){
					is.close();
						fs = new POIFSFileSystem(is);
						workBook = new HSSFWorkbook(fs);
				}
			}else{
				try {
					fs = new POIFSFileSystem(is);
					workBook = new HSSFWorkbook(fs);
				} catch (OfficeXmlFileException e) {
					is.close();
					workBook = new XSSFWorkbook(is);
				}
			}
		}finally{
			if(is != null)
				is.close();
		}
		return workBook;
	}
	
	public RepDataOperResultVO impExcelData(Object param) throws Exception{
		Object[] params=(Object[])param;
		TaskVO task = null;
		IRepDataParam repDataParam = (IRepDataParam)params[0];
		task = (TaskVO)params[4];
		byte[] bytes = (byte[])params[5];
		String extendName = (String)params[6];
		return innerImpExcelData(repDataParam,(LoginEnvVO)params[1],(List<Object[]>)params[2],((Boolean)params[3]).booleanValue(),task,extendName,bytes);
	}
	
	private RepDataOperResultVO innerImpExcelData(IRepDataParam param,LoginEnvVO loginEnv,List<Object[]> listImportInfos,boolean isAutoCalc,TaskVO task,String extendName,byte[] bytes) throws Exception{
		UfoContextVO context=TableInputHandlerHelper.getContextVO(param,loginEnv);
		TwoTuple<List<Object[]>,Set<ImpRepStateVO>> twoTuple = validateImpEnabled(param, listImportInfos, task);

		//导入的时候可能没有PubDataVO
		MeasurePubDataBO_Client.createFilterMeasurePubDatas(new MeasurePubDataVO[] { context.getPubDataVO() });

		List<Object[]> importInfos = twoTuple.first;
		Set<ImpRepStateVO> repStateSet = twoTuple.second;

		StringBuilder msg = new StringBuilder();
        for(ImpRepStateVO repState : repStateSet){
        	if(msg.length() != 0)
        		msg.append(ImpRepStateVO.ENTER_CHAR);
        	msg.append(repState.getMsg());
        }

        boolean isExcel2007 = ImpExpFileNameUtil.isExcel2007ExtName(extendName);
        String strErrMsg = null;
        if(isExcel2007){
        	String dir = ResourceUtil.getResourceDir() + java.io.File.separator + "ufoe" + java.io.File.separator + "Excel";
			String fileName = dir + File.separator + IDMaker.makeID(20) + ".xlsx";
        	File f = new File(dir);
        	f.mkdirs();
        	FileOutputStream os = null;
        	try{
        		os = new FileOutputStream(fileName);
            	os.write(bytes);
            	os.flush();
            	os.close();
        	}catch(Exception e){
        		AppDebug.debug(e);
        	}finally{
        		os.close();
        	}
        	
        	strErrMsg = processImp07Data(param, loginEnv, importInfos, isAutoCalc, context, fileName);
        	File file = new File(fileName);
			file.delete();
        	
        }else{
            Workbook workbook = getWorkbook(bytes, extendName);
            strErrMsg = processImpData(param, loginEnv, importInfos,
    				isAutoCalc, context,workbook);
        }



        RepDataOperResultVO result=new RepDataOperResultVO();
        if (strErrMsg.length()>0){
        	if(msg.length() != 0){
        		msg.append(ImpRepStateVO.ENTER_CHAR);
        	}
        	msg.append(strErrMsg);
        	result.setHintMessage(msg.toString());

        	ReportFormatSrv repFormatSrv=new ReportFormatSrv(context,true);
        	CellsModel cellsModel=repFormatSrv.getCellsModel();
        	result.setCellsModel(cellsModel);
        	result.setOperSuccess(false);
        }else{
        	ReportFormatSrv repFormatSrv=new ReportFormatSrv(context,true);
        	CellsModel cellsModel=repFormatSrv.getCellsModel();
        	result.setCellsModel(cellsModel);
        	if(msg.length() != 0){
        		result.setHintMessage(msg.toString());
        	}
        }

		return result;
	}
	
	private String processImpData(IRepDataParam param, LoginEnvVO loginEnv,
			List<Object[]> listImportInfos, boolean isAutoCalc,
			UfoContextVO context,Workbook workbook) throws CommonException, BusinessException {
		String strTaskPK =param.getTaskPK();
        DataSourceVO dataSource = loginEnv.getDataSource();
        MeasurePubDataVO pubdataVo = context.getPubDataVO();

        //导入对话框增加是否自动计算选项，此处暂时设置为自动计算 chxw 2007-09-26
        MultiSheetImportUtil importUtil = getImportUtilBase(strTaskPK, param.getReportPK(),null, dataSource, pubdataVo,param.getRepOrgPK(),param.getRepMngStructPK(), param.getCurGroupPK(),isAutoCalc, loginEnv.getCurLoginDate());

        //将导入Excel准备数据信息(CellsModel),导入到数据库
        boolean isNeedSave = true;
        ImportExcelDataBizUtil.processImpData(importUtil, listImportInfos, isNeedSave,context,workbook);
        String strErrMsg=importUtil.getLog()==null?"":importUtil.getLog().getResult();
		return strErrMsg;
	}


	private String processImp07Data(IRepDataParam param, LoginEnvVO loginEnv,
			List<Object[]> listImportInfos, boolean isAutoCalc,
			UfoContextVO context,String fileName) throws CommonException, BusinessException {
		String strTaskPK =param.getTaskPK();
        DataSourceVO dataSource = loginEnv.getDataSource();
        MeasurePubDataVO pubdataVo = context.getPubDataVO();

        //导入对话框增加是否自动计算选项，此处暂时设置为自动计算 chxw 2007-09-26
        MultiSheetImportUtil importUtil = getImportUtilBase(strTaskPK, param.getReportPK(),null, dataSource, pubdataVo,param.getRepOrgPK(),param.getRepMngStructPK(), param.getCurGroupPK(),isAutoCalc, loginEnv.getCurLoginDate());

        //将导入Excel准备数据信息(CellsModel),导入到数据库
        boolean isNeedSave = true;
        ImportExcelDataBizUtil.processImpData(importUtil, listImportInfos, isNeedSave,context,fileName);
        String strErrMsg=importUtil.getLog()==null?"":importUtil.getLog().getResult();
		return strErrMsg;
	}
	

	// @edit by wuyongc at 2014-3-25,上午10:16:06 导出套表，后台处理数据。
	public byte[] exportTemp2Excel(IRepDataParam param,LoginEnvVO loginEnv, TempRepExpParam expParam,KeyVO[] keys,String strAccSchemePK,Map<String,String> typeDirMap,boolean isFreeTotal, ArrayList<String> vTotalOrgPKs) throws Exception{
			String strAloneID4ExportExcel = param.getAloneID();
			UfoContextVO context=TableInputHandlerHelper.getContextVO(param,loginEnv);
			String filepath = expParam.getFilePath();
			String[] repPKs = expParam.getRepPKs();
			if(repPKs!= null && repPKs.length>0){
				List<String> sheetNameList = new ArrayList<String>();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				Workbook workBook = null;
				if(ImpExpFileNameUtil.isExcel2003(filepath)){
					workBook = new HSSFWorkbook();
				}else if(ImpExpFileNameUtil.isExcel2007(filepath)){
					workBook = new org.apache.poi.xssf.streaming.SXSSFWorkbook(500);
				}
				 FontFactory fontFactory=new FontFactory(workBook);
				 BalanceCondVO balanceCond = null;
				 
				 if (expParam.getBalancePK() != null
		    				|| !expParam.getBalancePK().equals(
		    						BalanceCondVO.NON_SW_DATA_COND_PK)) {
					 IBalanceCondService balanceCondService = NCLocator.getInstance().lookup(IBalanceCondService.class);
					 balanceCond =  balanceCondService.loadBalanceCondByPK(expParam.getBalancePK());
					 
				 }
				for (String repPK : repPKs) {
					CellsModel cellsModel = null;
//					if(((String)context.getAttribute(ReportContextKey.REPORT_PK)).equals(repPK)){
//						
//					}
					param.setReportPK(repPK);// 2015-0626 设置报表pk，如果有问题，把这一行放到后面if(isFreeTotal) 里面
					context.setAttribute(ReportContextKey.REPORT_PK, repPK);
					
					CellsModel formatModel = CellsModelOperator
							.getFormatModelByPKWithDataProcess(context);
					if (balanceCond != null) {
						RepDataVO repData = BalanceBO_Client.doSwBalance(param.getPubData(),
								balanceCond, repPK, param.getRepMngStructPK());
						cellsModel = CellsModelOperator.doGetDataModelFromRepDataVO(
								formatModel, repData, context);
						// @edit by wuyongc at 2014-1-16,上午9:26:51 
						BalanceReportExportUtil.processBalanceRepCellsModel(new ReportFormatSrv(context,cellsModel),false,balanceCond);
					} else {
						if(isFreeTotal) {
							cellsModel = getFreeTotalResult(param,
									loginEnv, vTotalOrgPKs).getCellsModel();
						}
						else{
							cellsModel = CellsModelOperator.fillCellsModelWithDBData(
									formatModel, context);
						}

					}
					RepDataWithCellsModelExport exportObj=new RepDataWithCellsModelExport(context,cellsModel);

					String strReportPK4ExportExcel = repPK;
					CSomeParam cSomeParam = new CSomeParam();
					cSomeParam.setAloneId(strAloneID4ExportExcel);
					cSomeParam.setRepId(strReportPK4ExportExcel);
					cSomeParam.setUserID(param.getOperUserPK());
					MeasurePubDataVO pubData= param.getPubData();
					cSomeParam.setUnitId(pubData.getUnitPK());
					((RepDataExport)exportObj).setParam(cSomeParam);
					((RepDataExport)exportObj).setLoginDate(loginEnv.getCurLoginDate());
				   
					ReportVO rep = (ReportVO) IUFOCacheManager.getSingleton()
							.getReportCache().get(repPK);

					String sheetName = getSheetName(rep, pubData.getKeywords(),
							expParam, keys, strAccSchemePK, sheetNameList);

					if (expParam.getSheetSequence() == TempRepExpParam.TASK_REP_ORDER) {
						if (StringUtils.isNotEmpty(typeDirMap.get(repPK))) {
							sheetName = typeDirMap.get(repPK) + "." + sheetName;
						}
					}
					
				    if(StringUtils.isEmpty(sheetName)){
				    	sheetName = "sheet1";
				    }
				    exportObj.setSheetName(sheetName);
					TableDataToExcel.translate(exportObj, workBook, fontFactory);
					
				}
				workBook.write(outputStream);
				outputStream.flush();
				byte[] bytes=outputStream.toByteArray();
				outputStream.close();
				return bytes;
			}
			
			return null;
	}
	
	private String getSheetName(ReportVO rep, String[] keyVals,
			TempRepExpParam expParam, KeyVO[] keys, String strAccSchemePK,List<String> sheetNameList) {
		StringBuilder sb = new StringBuilder();
		if (expParam.isbContainRepCode())
			append(sb, rep.getCode());
		if (expParam.isbContainRepName())
			append(sb, rep.getChangeName());
		IKeyDetailData keyDetailData = null;
		if (expParam.getContainKeywordNO() != null) {
			for (int i = 0; i < expParam.getContainKeywordNO().length; i++) {
				int index = expParam.getContainKeywordNO()[i];
				keyDetailData = KeyDetailDataUtil.getKeyDetailData(keys[index],
						keyVals[index], strAccSchemePK);
				append(sb, keyDetailData.getMultiLangText());
			}
		}
		if (sb.length() == 0)
			sb.append(rep.getCode()).append("_").append(rep.getChangeName());
		String sheetName = sb.length() > EXCEL_SHEET_NAME_MAX_LENGTH ? sb.substring(0, EXCEL_SHEET_NAME_MAX_LENGTH).toString() : sb.toString();

		//替换特殊字符
		sheetName = sheetName.replaceAll("/", "_");;
		sheetName = sheetName.replaceAll("\\\\", "_");;
					
		String finalSheetName = sheetName;
		int i = 0;
		while(sheetNameList.contains(finalSheetName)){
			i++;
			finalSheetName = sheetName + i;
		}
		//极端情况,去掉两位，再拼凑2位Id来区别
		if(finalSheetName.length()>EXCEL_SHEET_NAME_MAX_LENGTH){
			finalSheetName = sheetName = finalSheetName.substring(0, EXCEL_SHEET_NAME_MAX_LENGTH-2);
			while(sheetNameList.contains(finalSheetName)){
				finalSheetName = sheetName + IDMaker.makeID(2);
			}
		}
		sheetNameList.add(finalSheetName);
		return finalSheetName;

	}
	
	private void append(StringBuilder sb, String str) {
		if (sb.length() == 0)
			sb.append(str);
		else
			sb.append("_").append(str);
	}
	
	private static final int EXCEL_SHEET_NAME_MAX_LENGTH = 29;
	
	private IRepDataParam getRepDataParam(MeasurePubDataVO pubData, String aloneId, String repPK, String mianOrgPk,
			String orgStuc) {
		IRepDataParam param = new RepDataParam();
		param.setAloneID(aloneId);
		param.setReportPK(repPK);
		param.setOperType(TableInputParam.OPERTYPE_REPDATA_INPUT);
		// param.setTaskPK(taskPK);
		param.setRepMngStructPK(orgStuc);
		param.setRepOrgPK(mianOrgPk);
		
		param.setCurGroupPK(InvocationInfoProxy.getInstance().getGroupId());
		param.setPubData(pubData);
		return param;
	}
	
	private UfoContextVO getUfoContextVO(String repPK, MeasurePubDataVO pubData, String pk_org, String pk_group) {
		UfoContextVO context = new UfoContextVO();
		//TODO
//		setDataSource(context);
		

		context.setAttribute(IUfoContextKey.CUR_GROUP_PK, pk_group);
		context.setAttribute(IUfoContextKey.CUR_REPORG_PK, pk_org);
		context.setAttribute(ReportContextKey.REPORT_PK, repPK);
		context.setAttribute(IUfoContextKey.KEYGROUP_PK, pubData.getKeyGroup().getKeyGroupPK());
		ReportVO rep = IUFOCacheManager.getSingleton().getReportCache().getByPK(repPK);
		context.setAttribute(ReportContextKey.REPORT_NAME, rep.getChangeName());
		context.setAttribute(IUfoContextKey.MEASURE_PUB_DATA_VO, pubData);
		return context;
	}
	
	public Map<String,byte[]> exportRepData2Excel(LoginEnvVO loginEnv, RepExpParam expParam,KeyVO[] keys,String strAccSchemePK,String rmsPK,String mainOrgPK) throws Exception{
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		Map<String,byte[]> map = new HashMap<String,byte[]>();
		Map<String,Workbook> workBookMap = new HashMap<String,Workbook>();
		Map<String,FontFactory> fontFactoryMap = new HashMap<String,FontFactory>();
		String filePath = expParam.getFilePath();
		UfoContextVO context = null;
		IRepDataParam repParam = null;

		Map<String, List<String>> aloneRepMap = expParam.getAloneRepMap();
		Set<Map.Entry<String, List<String>>> set = aloneRepMap.entrySet();
		List<String> sheetNameList = new ArrayList<String>();

		Map<String, ReportOrgVO> repOrgMap = new HashMap<String, ReportOrgVO>();
		Workbook workBook = null;
		if (!expParam.isbSingleFile()) {
			Set<String> keySet = aloneRepMap.keySet();
			String[] alones = keySet.toArray(new String[keySet.size()]);
			String[] repOrgPKs = new String[keySet.size()];
			for (int i = 0; i < repOrgPKs.length; i++) {
				repOrgPKs[i] = expParam.getAlonePubDataMap().get(alones[i]).getUnitPK();
			}
			ICorpQuerySrv corpQry = NCLocator.getInstance().lookup(ICorpQuerySrv.class);
			ReportOrgVO[] repOrgVOs = corpQry.getReportOrgVosByPKS(repOrgPKs);
			for (ReportOrgVO reportOrgVO : repOrgVOs) {
				repOrgMap.put(reportOrgVO.getPk_reportorg(), reportOrgVO);
			}
		}
//		BalanceCondVO balanceCond = null;
		 FontFactory fontFactory = null;
		if(expParam.isSaveAll2OneFile()){
			workBook = getNewWorkbook(filePath, workBook);
			 fontFactory=new FontFactory(workBook);
		}
		for (Map.Entry<String, List<String>> entry : set) {
			String aloneId = entry.getKey();
			List<String> repList = entry.getValue();
			// 当不是存储在单一文件中，应该每遍历玩一个单位的表clear一次这个list，就不会出现多出一个数字的问题
			if (!expParam.isSaveAll2OneFile()) {
				sheetNameList.clear();
			}
			for (String repPK : repList) {
				MeasurePubDataVO pubData = expParam.getAlonePubDataMap().get(aloneId);
				context = getUfoContextVO(repPK, pubData, mainOrgPK, pk_group);
				repParam = getRepDataParam(pubData, aloneId, repPK, mainOrgPK, rmsPK);
				CellsModel formatModel = CellsModelOperator.getFormatModelByPKWithDataProcess(context);

//				CellsModel cellsModel = null;
//				if (balanceCond != null) {
//					RepDataVO repData = BalanceBO_Client.doSwBalance(expParam.getAlonePubDataMap().get(aloneId),
//							balanceCond, repPK, rmsPK);
//					cellsModel = CellsModelOperator.doGetDataModelFromRepDataVO(formatModel, repData, context);
//					BalanceReportExportUtil.processBalanceRepCellsModel(new ReportFormatSrv(context,cellsModel),false,balanceCond);
//				} else {
//				}
				CellsModel cellsModel = null;
				//editor tianjlc 2015-04-14 报表数据导出的时候设置为非原表数据时，先将数据按照选中的处理，然后导出
				if(!expParam.getBalancePK().equals(BalanceCondVO.NON_SW_DATA_COND_PK)){
					BalanceCondVO balanceCond=BalanceBO_Client.loadBalanceCondByPK(expParam.getBalancePK());
		        	RepDataVO repData=BalanceBO_Client.doSwBalance(pubData, balanceCond,repPK, (String)context.getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK));
		        	cellsModel=CellsModelOperator.getFormatModelByPK(context);
		        	cellsModel=CellsModelOperator.doGetDataModelFromRepDataVO(cellsModel, repData, context);
		        	BalanceReportExportUtil.processBalanceRepCellsModel(new ReportFormatSrv(context,cellsModel),false,balanceCond);
		        	pubData.setVer(0);
		            pubData.setAloneID(MeasureDataUtil.getAloneID(pubData));
		            //针对舍位情况，处理打印设置
		            BalanceReportExportUtil.dealPrintSetForBalance(cellsModel,balanceCond);
				}else{
					cellsModel=CellsModelOperator.fillCellsModelWithDBData(formatModel, context);
				}
				RepDataWithCellsModelExport exportObj = new RepDataWithCellsModelExport(context, cellsModel);
				String strReportPK4ExportExcel = repParam.getReportPK();
				CSomeParam cSomeParam = new CSomeParam();
				cSomeParam.setAloneId(repParam.getAloneID());
				cSomeParam.setRepId(strReportPK4ExportExcel);
				cSomeParam.setUserID(repParam.getOperUserPK());
				cSomeParam.setUnitId(pubData.getUnitPK());
				((RepDataExport) exportObj).setParam(cSomeParam);
				((RepDataExport) exportObj).setLoginDate(loginEnv.getCurLoginDate());
				ReportVO rep = (ReportVO) IUFOCacheManager.getSingleton().getReportCache().get(repPK);
				String sheetName = getSheetName(rep, pubData.getKeywords(), expParam, pubData.getKeyGroup().getKeys(),
						sheetNameList);
				exportObj.setSheetName(sheetName);
				if (expParam.isSaveAll2OneFile()) {
					TableDataToExcel.translate(exportObj, workBook, fontFactory);
				} else {
					String fileName = getFileName(rep, pubData.getKeywords(), expParam,
							pubData.getKeyGroup().getKeys(), repOrgMap);
					String extendName = ImpExpFileNameUtil.isExcel2007(filePath) ? ImpExpConstant.XLSX : ImpExpConstant.XLS;
					fileName += CommonCharConstant.POINT + extendName;
					workBook = workBookMap.get(fileName);
					fontFactory= fontFactoryMap.get(fileName);
					if(workBook == null){
						 workBook = getNewWorkbook(fileName, workBook);
						 workBookMap.put(fileName, workBook);
						 fontFactory=new FontFactory(workBook);
						 fontFactoryMap.put(fileName, fontFactory);
					}
					TableDataToExcel.translate(exportObj, workBook, fontFactory);
				}
			}
		}
		
		if(expParam.isSaveAll2OneFile()){
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			 workBook.write(outputStream);
				outputStream.flush();
				byte[] bytes=outputStream.toByteArray();
				outputStream.close();
				map.put(filePath, bytes);
		}else{
			Set<Entry<String, Workbook>> wrokbookSet  = workBookMap.entrySet();
			for (Entry<String, Workbook> entry : wrokbookSet) {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				entry.getValue().write(outputStream);
				outputStream.flush();
				byte[] bytes=outputStream.toByteArray();
				outputStream.close();
				map.put(entry.getKey(), bytes);
			}
		}
		return map;
	}

	public Workbook getNewWorkbook(String filePath, Workbook workBook) {
		if(ImpExpFileNameUtil.isExcel2003(filePath)){
			workBook = new HSSFWorkbook();
		}else if(ImpExpFileNameUtil.isExcel2007(filePath)){
			workBook = new org.apache.poi.xssf.streaming.SXSSFWorkbook(500);
		}
		return workBook;
	}
	
	private String getFileName(ReportVO rep, String[] keywords, RepExpParam expParam, KeyVO[] keys,
			Map<String, ReportOrgVO> repOrgMap) {

		StringBuilder sb = new StringBuilder();
		if (expParam.isFileCode())
			append(sb, rep.getCode());
		if (expParam.isFileName())
			append(sb, rep.getChangeName());
		IKeyDetailData keyDetailData = null;
		if (expParam.getFileKeyNo() != null) {
			for (int i = 0; i < expParam.getFileKeyNo().length; i++) {
				int index = expParam.getFileKeyNo()[i];
				keyDetailData = KeyDetailDataUtil.getKeyDetailData(keys[index], keywords[index],
						expParam.getPk_accscheme());
				append(sb, keyDetailData.getMultiLangText());
			}
		}
		if (expParam.getCode() != null) {
			ReportOrgVO repOrg = repOrgMap.get(keywords[0]);
			Object propertyValue = BeanUtilities.getProperty(repOrg, expParam.getCode());
			;
			if (propertyValue != null) {
				if (expParam.getClassid() == null) {
					// ReportOrgVO repOrg = OrgUtil.getRepOrgVoByORGPK(keywords[0]);
					if (expParam.getCode().equals(ReportOrgVO.PK_REPORTORG)) {
						sb.append(MultiLangTextUtil.getCurLangText(repOrg));
					} else if (expParam.getCode().equals(ReportOrgVO.CREATOR)
							|| expParam.getCode().equals(ReportOrgVO.MODIFIER)) {
						if (propertyValue != null) {
							sb.append(sb.append(UserUtil.getUserName((String) propertyValue)));
						}
					} else if (expParam.getCode().equals(ReportOrgVO.CREATIONTIME)
							|| expParam.getCode().equals(ReportOrgVO.MODIFIEDTIME)) {
						UFDateTime dateTime = (UFDateTime) propertyValue;
						if (dateTime != null) {
							sb.append(DateFormatUtils.format(dateTime.getMillis(), "yyyy-MM-dd'T'HH-mm-ss"));
						}
					} else if (expParam.getCode().equals(ReportOrgVO.PK_GROUP)) {
						if (propertyValue != null) {
							IBDData group = GeneralAccessorFactory.getAccessor(IOrgMetaDataIDConst.GROUP).getDocByPk(
									(String) propertyValue);
							if (group != null) {
								sb.append(group.getName().toString());
							}
						}
					} else if (expParam.getCode().equals(ReportOrgVO.PK_ORG)) {
						if (propertyValue != null) {
							String orgName = OrgUtil.getOrgName((String) propertyValue);
							if (orgName == null) {
								sb.append(orgName);
							}
						}
					} else {
						sb.append(propertyValue);
					}

					// 判断是否有 这个字段
				} else {
					IGeneralAccessor accessor = GeneralAccessorFactory.getAccessor(expParam.getClassid());
					if (accessor != null) {
						IBDData bdData = accessor.getDocByPk((String) propertyValue);
						if (bdData != null) {
							sb.append(bdData.getName());
						}
					}
					if (sb.length() == 0) {
						sb.append(repOrg.getCode());
					}
				}
			} else {
				sb.append(repOrg.getCode());
			}
		}

		if (sb.length() == 0)
			sb.append(rep.getCode()).append("_").append(rep.getChangeName());
		String fileName = sb.length() > 216 ? sb.substring(0, 216).toString() : sb.toString();
		
		fileName = FileNameUtil.deleteIllegalChar(fileName);		


		return fileName;
	}

	
	private String getSheetName(ReportVO rep, String[] keyVals, RepExpParam expParam, KeyVO[] keys,
			List<String> sheetNameList) {
		StringBuilder sb = new StringBuilder();
		if (expParam.isbContainRepCode())
			append(sb, rep.getCode());
		if (expParam.isbContainRepName())
			append(sb, rep.getChangeName());
		IKeyDetailData keyDetailData = null;
		if (expParam.getContainKeywordNO() != null) {
			for (int i = 0; i < expParam.getContainKeywordNO().length; i++) {
				int index = expParam.getContainKeywordNO()[i];
				keyDetailData = KeyDetailDataUtil.getKeyDetailData(keys[index], keyVals[index],
						expParam.getPk_accscheme());
				append(sb, keyDetailData.getMultiLangText());
			}
		}
		if (sb.length() == 0)
			sb.append(rep.getCode()).append("_").append(rep.getChangeName());
		String sheetName = sb.length() > EXCEL_SHEET_NAME_MAX_LENGTH ? sb.substring(0, EXCEL_SHEET_NAME_MAX_LENGTH)
				.toString() : sb.toString();
		sheetName = sheetName.replaceAll("/", "_");
		;
		sheetName = sheetName.replaceAll("\\\\", "_");
		;
		String finalSheetName = sheetName;
		int i = 0;
		while (sheetNameList.contains(finalSheetName)) {
			i++;
			finalSheetName = sheetName + i;
		}
		// 极端情况,去掉两位，再拼凑2位Id来区别
		if (finalSheetName.length() > EXCEL_SHEET_NAME_MAX_LENGTH) {
			finalSheetName = sheetName = finalSheetName.substring(0, EXCEL_SHEET_NAME_MAX_LENGTH - 2);
			while (sheetNameList.contains(finalSheetName)) {
				finalSheetName = sheetName + IDMaker.makeID(2);
			}
		}
		sheetNameList.add(finalSheetName);
		return finalSheetName;
	}
	
	
}
