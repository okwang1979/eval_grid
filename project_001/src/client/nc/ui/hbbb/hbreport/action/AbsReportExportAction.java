package nc.ui.hbbb.hbreport.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import nc.desktop.ui.WorkbenchEnvironment;
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
import nc.ui.pub.beans.UIFileChooser;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.model.BillManageModel;
import nc.util.hbbb.hbreport.HBReportQueryUtil;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.iufo.pub.UFOString;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iuforeport.rep.ReportVO;

import com.ufida.iufo.constant.output.IOutputMsgConstant;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.report.sysplugin.print.FreeReportPrintStatusMng;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.iuforeport.tableinput.applet.RepDataParam;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.report.sysplugin.xml.ExtNameFileFilter;
import com.ufsoft.report.util.UfoPublic;
import com.ufsoft.table.CellsModel;

/**
 * ��Χ������������
 * 
 * @version V6.1
 * @author litfb
 */
@SuppressWarnings("restriction")
public abstract class AbsReportExportAction extends NCAction {

    private static final long serialVersionUID = 4434694908362856090L;

    protected static final String XLS = "xls";

    protected BillManageModel model;

    protected HBBBQueryAreaShell userQryPanel;

    public AbsReportExportAction() {
        super();
    }

    @Override
    public void doAction(ActionEvent e) throws Exception {
    	//licenceУ��
    	if (!FreeReportPrintStatusMng.CheckIfHaveLicense()) {
			ShowStatusBarMsgUtil.showErrorMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0844")/*@res "����ʧ�ܣ�"*/,
					IOutputMsgConstant.CANNOT_EXP_NO_LICENSE, getModel().getContext());
			return;
		}
        // ��ѯ����
        IUfoQueryCondVO queryCond = getUserQryPanel().getQueryArea().getQuickQueryArea().genQueryCond(false);
        // �ϲ�����
        String pk_hbscheme = queryCond.getPk_task();
        HBSchemeVO hbSchemeVO = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(pk_hbscheme);
        // ��ѡ��¼�뱨��
        RepDataQueryResultVO[] repQryResults = getSelectedInputValue(queryCond, hbSchemeVO);
        if (repQryResults.length == 0) {
            return;
        }
        // ������֯�����ݽ��з���
        Map<String, List<RepDataQueryResultVO>> orgResultMap = getGroupRepDataResult(repQryResults);
        JFileChooser chooser = new UIFileChooser();
        // ��ѡ
        boolean isSingleSelected = orgResultMap.size() == 1;
        if (isSingleSelected) {
            ExtNameFileFilter xf = new ExtNameFileFilter(XLS);
            chooser.setFileFilter(xf);
            chooser.setMultiSelectionEnabled(false);
            String orgName = OrgUtil.getOrgName(repQryResults[0].getPk_org());
            chooser.setSelectedFile(new File(orgName + "_" + getRepTypeName() + "." + XLS));
        } else {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }

        int returnVal = chooser.showSaveDialog(getModel().getContext().getEntranceUI());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            // ��֤ѡ���ļ�·�������������쳣·������� D:\My Document\........\.......
            if (!UFOString.testJFileChooserPath(file.getPath())) {
                throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0",
                        "01820001-0438")/* @res "����ʧ��.��ѡ����ȷ��·������:\n[" */
                        + file.getPath() + "]");
            }
            // ���������ļ���
            final String dirPath = file.getPath();
            // �������ļ��� ����������Ϊѡ���·�������������ѡ���ļ��У��ڵ�����ʱ������ļ�����
            // �����һ�ļ�����ʱ������ļ���ʱ���޸��ļ���ʱ���ļ�������ȷ��excel��չ����ʽ�����Զ����.xls��չ��
            String filePath = isSingleSelected ? (dirPath.toLowerCase().endsWith("." + XLS) ? dirPath : dirPath + "."
                    + XLS) : null;

            String orgName = null;
            String repTypeName = getRepTypeName();
            List<RepDataQueryResultVO> repDataList = null;
            UfoContextVO context = null;
            IRepDataParam param = null;
            List<IExcelExport> excelExp = null;
            Iterator<Map.Entry<String, List<RepDataQueryResultVO>>> it = orgResultMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, List<RepDataQueryResultVO>> entry = it.next();
                orgName = OrgUtil.getOrgName(entry.getKey());
                repDataList = entry.getValue();
                excelExp = new ArrayList<IExcelExport>();
                // ��ѡѡ�е����ļ�����ѡѡ�е���Ŀ¼�������Ŀ¼����Ҫ����ļ���
                if (!isSingleSelected) {
                    filePath = dirPath + File.separator + orgName + "_" + repTypeName + ".xls";
                }

                for (RepDataQueryResultVO vo : repDataList) {
                    context = getContextVO(vo);
                    param = getRepDataParam(vo);
                    MeasurePubDataVO dataVO = vo.getPubData();
                    //ͨ��HBReportQueryUtil.getMeasurePubData�õ���measpubdata��Ϣ��������
                    //���������ҵ����������ʱ��Ҫʹ��.@edit by dongjch 2015-06-06
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

                }
                File f = new File(filePath);
                if (f.exists()) {
                    int iRet = UfoPublic.showConfirmDialog(
                            getModel().getContext().getEntranceUI(),
                            nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0842")
                                    /* @res "����Ϊ " */+ f.getName()
                                    + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0",
                                            "01820001-0846")/* @res " ��excel�ļ��Ѿ����ڣ��Ƿ񸲸�?" */,
                            nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0133")
                            /* @res "��ʾ" */, JOptionPane.YES_NO_OPTION);
                    if (iRet != JOptionPane.YES_OPTION) {
                        continue;
                    }
                }
                TableDataToExcel.translateToMultiSheet(excelExp.toArray(new IExcelExport[0]), filePath);
            }
        }
    }

    @Override
    protected boolean isActionEnable() {
        try {
            // ��ѡ��¼�뱨��
            RepDataQueryResultVO[] repQryResults = getSelectedInputValue();
            return repQryResults.length > 0;
        } catch (Exception e) {
            AppDebug.debug(e);
            return false;
        }
    }

    /**
     * ���ر���汾
     * 
     * @param hbSchemeVO
     * @return
     */
    protected abstract Integer getRepVersion(HBSchemeVO hbSchemeVO);

    /**
     * �����汾����
     * 
     * @return
     */
    protected abstract String getRepTypeName();

    /**
     * ����ģ��
     * 
     * @param pk_report
     * @param context
     * @param param
     * @return
     * @throws Exception
     */
    protected abstract CellsModel getCellModel(String pk_report, UfoContextVO context, IRepDataParam param)
            throws Exception;

    /**
     * ��ȡѡ�е���¼�뱨��
     * 
     * @return
     */
    protected RepDataQueryResultVO[] getSelectedInputValue() {
        // ѡ�б���
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
     * ��ȡѡ�е���¼�뱨��
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
     * ������֯�����ݽ��з���
     * 
     * @param repQryResults
     * @return Map<��֯PK,�������ݲ�ѯ���VO>
     */
    protected Map<String, List<RepDataQueryResultVO>> getGroupRepDataResult(RepDataQueryResultVO[] repQryResults) {
        // ���ѡ�ж�����¼��������֯�����ݽ��з��飬һ����֯�ı�����Ϊһ��excel�ļ�
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
