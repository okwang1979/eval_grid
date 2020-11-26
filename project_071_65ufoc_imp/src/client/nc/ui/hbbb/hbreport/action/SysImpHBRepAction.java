package nc.ui.hbbb.hbreport.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import nc.bs.framework.common.NCLocator;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.funcnode.ui.AbstractFunclet;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.itf.iufo.dataremove.IDataRemoveSrv;
import nc.itf.iufo.individual.IUFOIndividualSettingUtil;
import nc.login.vo.NCSession;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.hbbb.quickquery.model.HBBBQueryAreaShell;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.dataexchange.IExcelExport;
import nc.ui.iufo.dataexchange.RepDataExport;
import nc.ui.iufo.dataexchange.RepDataWithCellsModelExport;
import nc.ui.iufo.dataexchange.TableDataToExcel;
import nc.ui.iufo.input.CSomeParam;
import nc.ui.iufo.input.table.TableInputParam;
import nc.ui.iufo.repdatamng.actions.SysImpInfoDlg;
import nc.ui.iufo.repdatamng.actions.SysImpRepAction;
import nc.ui.pub.beans.UIFileChooser;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.model.BillManageModel;
import nc.util.hbbb.hbreport.HBReportQueryUtil;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.iufo.pub.UFOString;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.scmpub.json.UFDataTypeDeserializer;
import nc.vo.vorg.ReportCombineStruVersionVO;

import com.ufida.iufo.constant.output.IOutputMsgConstant;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.report.sysplugin.print.FreeReportPrintStatusMng;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.fmtplugin.service.base.IReportFormatSrvBase;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.iuforeport.tableinput.applet.RepDataParam;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.report.sysplugin.xml.ExtNameFileFilter;
import com.ufsoft.report.util.UfoPublic;
import com.ufsoft.table.CellsModel;

/**
 * 合并报表导出
 * 
 * @version V6.1
 * @author litfb
 */
public class SysImpHBRepAction extends AbsReportExportAction {

    protected static final String XLS = "xls";

    protected BillManageModel model;

    protected HBBBQueryAreaShell userQryPanel;

    public SysImpHBRepAction() {
        super();
        setCode("hbSysImpAction");
        setBtnName("报表数据推送");
    }

    @Override
    protected Integer getRepVersion(HBSchemeVO hbSchemeVO) {
        return hbSchemeVO.getVersion();
    }

    @Override
    protected String getRepTypeName() {
        return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830004-0002")/* @res "合并报表" */;
    }

    @Override
    protected CellsModel getCellModel(String pk_report, UfoContextVO context, IRepDataParam param) throws Exception {
        CellsModel formatModel = CellsModelOperator.getFormatModelByPKWithDataProcess(context);
        CellsModel cellsModel = CellsModelOperator.fillCellsModelWithDBData(formatModel, context);
        return cellsModel;
    }




 

    @Override
    public void doAction(ActionEvent e) throws Exception {
//    	//licence校验
//    	if (!FreeReportPrintStatusMng.CheckIfHaveLicense()) {
//			ShowStatusBarMsgUtil.showErrorMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0844")/*@res "导出失败！"*/,
//					IOutputMsgConstant.CANNOT_EXP_NO_LICENSE, getModel().getContext());
//			return;
//		}
//    	
    	
    	
    	final	SysImpInfoDlg infoDlg = new SysImpInfoDlg(getModel().getContext());
    	final JComponent UI = getModel().getContext().getEntranceUI();
		if (UI instanceof AbstractFunclet) {
			AbstractFunclet funclet = (AbstractFunclet) UI;
			funclet.showStatusBarMessage(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140udddb",
					"010140udddb0002")/* @res "正在进行后台操作, 请稍等..." */);
			funclet.showProgressBar(true);
			funclet.lockFuncWidget(true);
		}
    	new Thread(){

			@Override
			public void run() {
				try {

					SysImpHBRepAction.this.runImp(infoDlg);
				} catch (Exception e) {
				 
				}finally{
					ShowStatusBarMsgUtil.showStatusBarMsg("推送完成!", getModel().getContext());	
					if (UI instanceof AbstractFunclet) {
						AbstractFunclet funclet = (AbstractFunclet) UI;
						funclet.lockFuncWidget(false);
						funclet.showProgressBar(false);
					}
				}
			}
			
		}.start();
		infoDlg.showModal();
    	

    }

    protected void runImp(SysImpInfoDlg infoDlg) throws Exception {

        // 查询条件
        IUfoQueryCondVO queryCond = getUserQryPanel().getQueryArea().getQuickQueryArea().genQueryCond(false);
        // 合并方案
        String pk_hbscheme = queryCond.getPk_task();
        HBSchemeVO hbSchemeVO = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(pk_hbscheme);
        // 已选已录入报表
        RepDataQueryResultVO[] repQryResults = getSelectedInputValue(queryCond, hbSchemeVO);
        if (repQryResults.length == 0) {
            return;
        }
        // 按照组织对数据进行分组
        Map<String, List<RepDataQueryResultVO>> orgResultMap = getGroupRepDataResult(repQryResults);
 

        String orgName = null;
        String repTypeName = getRepTypeName();
        List<RepDataQueryResultVO> repDataList = null;
        UfoContextVO context = null;
        IRepDataParam param = null;
        List<IExcelExport> excelExp = null;
        Iterator<Map.Entry<String, List<RepDataQueryResultVO>>> it = orgResultMap.entrySet().iterator();
        IDataRemoveSrv remSrv = NCLocator.getInstance().lookup(IDataRemoveSrv.class);
        while (it.hasNext()) {
            Map.Entry<String, List<RepDataQueryResultVO>> entry = it.next();
          boolean isLeafMember =   HBRepStruUtil.isLeafMember(entry.getKey(), getUserQryPanel().getQueryArea().getQuickQueryArea().genQueryCond(false).getPk_rms());
          if(isLeafMember) continue;
            orgName = OrgUtil.getOrgName(entry.getKey());
            repDataList = entry.getValue();
            excelExp = new ArrayList<IExcelExport>();
            // 单选选中的是文件，多选选中的是目录，如果是目录，需要另加文件名

            for (RepDataQueryResultVO vo : repDataList) {
                context = getContextVO(vo);
                param = getRepDataParam(vo);
                MeasurePubDataVO dataVO = vo.getPubData();
                //通过HBReportQueryUtil.getMeasurePubData得到的measpubdata信息不完整，
                //后面调用企业报表导出数据时需要使用.@edit by dongjch 2015-06-06
                if(null == dataVO.getAloneID()){
                	String aloneid = MeasurePubDataBO_Client.getAloneID(dataVO);
                	dataVO.setAloneID(aloneid);
                }
                context.setAttribute("key_MEASURE_PUB_DATA_VO",dataVO);
                RepDataWithCellsModelExport exportObj = new RepDataWithCellsModelExport(context, getCellModel(
                        vo.getPk_report(), context, param));

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
                
               String strus=  getUserQryPanel().getQueryArea().getQuickQueryArea().genQueryCond(false).getPk_rms();
                ReportCombineStruVersionVO versionVO = HBRepStruUtil.getHBStruVersionVO((new UFDate()).toStdString(),strus);
                
                
                // 关键字组合
                KeyGroupVO keyGroupVO = pubData.getKeyGroup();
                // 时间关键字
                KeyVO key = keyGroupVO.getTTimeKey();
                // 报表合并体系版本
                ReportCombineStruVersionVO memberVO = null;
                if (key != null) {
                    String keyValue = pubData.getKeywordByPK(key.getPk_keyword());
                    if (key.isAccPeriodKey()) {
                        memberVO = HBRepStruUtil.getHBStruVersionVO(pubData.getAccSchemePK(), key.getPk_keyword(),
                                keyValue, hbSchemeVO.getPk_repmanastru());
                    } else {
                        memberVO = HBRepStruUtil.getHBStruVersionVO(keyValue, hbSchemeVO.getPk_repmanastru());
                    }
                } else {
                    memberVO = HBRepStruUtil.getLastVersion(hbSchemeVO.getPk_repmanastru());
                }
               
                Map<String,String> rtn = remSrv.pushReport(rep.getCode(), vo.getPubData().getKeyGroup().getKeys(), exportObj.getReportFormatSrv(true).getCellsModel(),pubData,memberVO.getCode(),OrgUtil.getOrgCode(entry.getKey()),hbSchemeVO.getCode()+"");

                rtn.put("report_name", orgName+"_"+rep.getCode());
                infoDlg.upUi(rtn);
            
                 

                }

            }
//        }
    
    }

	@Override
    protected boolean isActionEnable() {
        try {
            // 已选已录入报表
            RepDataQueryResultVO[] repQryResults = getSelectedInputValue();
            return repQryResults.length > 0;
        } catch (Exception e) {
            AppDebug.debug(e);
            return false;
        }
    }


    /**
     * 获取选中的已录入报表
     * 
     * @return
     */
    protected RepDataQueryResultVO[] getSelectedInputValue() {
        // 选中报表
        Object[] objects = getModel().getSelectedOperaDatas();
        List<RepDataQueryResultVO> repQryResults = new ArrayList<RepDataQueryResultVO>();
        if (objects != null && objects.length > 0) {
            for (Object obj : objects) {
                RepDataQueryResultVO queryResultVO = (RepDataQueryResultVO) obj;
                if (queryResultVO.getInputstate() != null && queryResultVO.getInputstate().booleanValue()) {
                    repQryResults.add(queryResultVO);
                }
            }
        }
        return repQryResults.toArray(new RepDataQueryResultVO[repQryResults.size()]);
    }

    /**
     * 获取选中的已录入报表
     * 
     * @param queryCond
     * @param hbSchemeVO
     * @return
     * @throws Exception
     */
    protected RepDataQueryResultVO[] getSelectedInputValue(IUfoQueryCondVO queryCond, HBSchemeVO hbSchemeVO)
            throws Exception {
        RepDataQueryResultVO[] repQryResults = getSelectedInputValue();
        if (repQryResults.length > 0) {
            Map<String, MeasurePubDataVO> pubDataMap = new HashMap<String, MeasurePubDataVO>();
            for (RepDataQueryResultVO repQryResult : repQryResults) {
                // MeasurePubData
                MeasurePubDataVO pubDataVO = pubDataMap.get(repQryResult.getPk_org());
                if (pubDataVO == null) {
                    pubDataVO = HBReportQueryUtil.getMeasurePubData(queryCond, hbSchemeVO, getRepVersion(hbSchemeVO),
                            repQryResult.getPk_org(), true);
                    pubDataMap.put(repQryResult.getPk_org(), pubDataVO);
                }
                repQryResult.setPubData(pubDataVO);
            }
        }
        return repQryResults;
    }

    /**
     * 按照组织对数据进行分组
     * 
     * @param repQryResults
     * @return Map<组织PK,报表数据查询结果VO>
     */
    protected Map<String, List<RepDataQueryResultVO>> getGroupRepDataResult(RepDataQueryResultVO[] repQryResults) {
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

    @SuppressWarnings("deprecation")
    protected UfoContextVO getContextVO(RepDataQueryResultVO repRequeryDataVO) {
        UfoContextVO context = new UfoContextVO();
        setDataSource(context);
        String pk_org = getModel().getContext().getPk_org();
        String pk_group = getModel().getContext().getPk_group();
        context.setAttribute(IUfoContextKey.CUR_GROUP_PK, pk_group);
        context.setAttribute(IUfoContextKey.CUR_REPORG_PK, pk_org);
        context.setAttribute(ReportContextKey.REPORT_PK, repRequeryDataVO.getPk_report());
        context.setAttribute(IUfoContextKey.KEYGROUP_PK, repRequeryDataVO.getPubData().getKType());
        ReportVO rep = IUFOCacheManager.getSingleton().getReportCache().getByPK(repRequeryDataVO.getPk_report());
        context.setAttribute(ReportContextKey.REPORT_NAME, rep.getChangeName());
        if("1".equals(repRequeryDataVO.getKeyword10())){
        	repRequeryDataVO.getPubData().setAloneID(repRequeryDataVO.getAlone_id());
        }
        context.setAttribute(IUfoContextKey.MEASURE_PUB_DATA_VO, repRequeryDataVO.getPubData());
        return context;
    }

    protected IRepDataParam getRepDataParam(RepDataQueryResultVO repRequeryDataVO) {
        IRepDataParam param = new RepDataParam();
        param.setAloneID(repRequeryDataVO.getAlone_id());
        param.setReportPK(repRequeryDataVO.getPk_report());
        param.setOperType(TableInputParam.OPERTYPE_REPDATA_INPUT);
        param.setTaskPK(repRequeryDataVO.getPk_task());
        param.setCurGroupPK(WorkbenchEnvironment.getInstance().getGroupVO().getPk_group());
        param.setPubData(repRequeryDataVO.getPubData());
        return param;
    }

    @SuppressWarnings("deprecation")
    protected void setDataSource(UfoContextVO context) {
        DataSourceVO dataSource = new DataSourceVO();
        NCSession session = WorkbenchEnvironment.getInstance().getSession();
        dataSource.setDs_addr(session.getDsName());
        dataSource.setAccount_name(session.getBusiCenterName());
        dataSource.setDs_type(nc.vo.iufo.datasource.DataSourceVO.TYPENC2);
        context.setAttribute(IUfoContextKey.DATA_SOURCE, dataSource);
    }

    protected LoginEnvVO getLoginEnvVO() {
        LoginEnvVO loginEnv = new LoginEnvVO();
        loginEnv.setCurLoginDate(WorkbenchEnvironment.getServerTime().toStdString());
        loginEnv.setDataExplore(true);
        loginEnv.setDataSource(IUFOIndividualSettingUtil.getDefaultDataSourceVo());
        loginEnv.setLangCode(WorkbenchEnvironment.getLangCode());
        return loginEnv;
    }

    public BillManageModel getModel() {
        return model;
    }

    public void setModel(BillManageModel model) {
        this.model = model;
        this.model.addAppEventListener(this);
    }

    public HBBBQueryAreaShell getUserQryPanel() {
        return userQryPanel;
    }

    public void setUserQryPanel(HBBBQueryAreaShell userQryPanel) {
        this.userQryPanel = userQryPanel;
    }
    
    
    
    
    
}
