package nc.ui.hbbb.hbreport.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.hbbb.hbreport.model.HBReportQueryExecutor;
import nc.ui.uif2.actions.DeleteAction;
import nc.ui.uif2.model.BillManageModel;
import nc.util.hbbb.pub.HBPubItfService;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.pub.BusinessException;

/**
 * �ϲ�����ɾ��Action
 *
 * @version V6.1
 * @author litfb
 */
public class HBReportDeleteAction extends DeleteAction {

    private static final long serialVersionUID = -7840860626406615835L;

    /** ɾ��ʧ��-�������ϱ� */
    public static final String FALSE_TYPE_COMMIT = "FALSE_TYPE_COMMIT";
    /** ��ѯִ���� */
    private HBReportQueryExecutor queryExecutor;

    @Override
    public void doAction(ActionEvent e) throws Exception {
//        if (UIDialog.ID_YES == CommonConfirmDialogUtils
//                .showConfirmDeleteDialog(getModel().getContext().getEntranceUI())) {
            Object[] selectedOperaDatas = ((BillManageModel) model).getSelectedOperaDatas();
            if (selectedOperaDatas != null) {
                // ���ϱ�����
                List<String> commitPks = new ArrayList<String>();
                // �ֲ�ʽ���ݣ�Key��DataOrigin,Value repPkLst
                Map<String, List<String>> dataOriginMap = new HashMap<String, List<String>>();
                for (int i = 0; i < selectedOperaDatas.length; i++) {
                    RepDataQueryResultVO object = (RepDataQueryResultVO) selectedOperaDatas[i];
                    if("1".equals(object.getKeyword10())){
                    	continue;
                    }
                    // ɾ���ӿ�IUFO��ʱֵ�ṩ����ɾ��,��FORѭ��
                    String result = deleteVO(object);
                    if (result != null) {
                        if (FALSE_TYPE_COMMIT.equals(result)) {
                            commitPks.add(object.getPk_report());
                        } else {
                            List<String> repPkLst = dataOriginMap.get(result);
                            if (repPkLst == null) {
                                repPkLst = new ArrayList<String>();
                            }
                            repPkLst.add(object.getPk_report());
                            dataOriginMap.put(result, repPkLst);
                        }
                    }
                }
                getQueryExecutor().reQuery();
                String msg = "";

                if (commitPks.size() > 0) {
                    msg += genCommitMsg(commitPks);
                }
                if (dataOriginMap.size() > 0) {
                    msg += genDataOriginMsg(dataOriginMap);
                }
                if (msg.length() > 0) {
//                    ShowStatusBarMsgUtil.showErrorMsg(
//                            nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0004")/* @res
//                                                                                                            * "��ʾ" */,
//                            msg, getModel().getContext());
                    throw new BusinessException(msg);
                } else {
//                    showSuccessInfo();
                }
            }
        }
//    }

    private String genCommitMsg(List<String> commitPks) {
        ReportVO[] reports = UFOCacheManager.getSingleton().getReportCache()
                .getByPks(commitPks.toArray(new String[commitPks.size()]));
        StringBuffer reportNames = new StringBuffer();
        if (reports != null) {
            for (int i = 0; i < reports.length; i++) {
                if (i > 0) {
                    reportNames.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830006-0121")/*@res "��"*/);
                }
                reportNames.append(reports[i].getNameWithCode());
            }
        }
        return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0102")/* @res���� */
                + reportNames.toString()
                + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0016")/* @res "�Ѿ��ϱ�,������ɾ��!" */;
    }

    private String genDataOriginMsg(Map<String, List<String>> dataOriginMap) {
        StringBuffer originMsg = new StringBuffer();
        for (String dataOrigin : dataOriginMap.keySet()) {
            List<String> repPks = dataOriginMap.get(dataOrigin);
            ReportVO[] reports = UFOCacheManager.getSingleton().getReportCache()
                    .getByPks(repPks.toArray(new String[repPks.size()]));
            if (reports != null) {
                StringBuffer reportNames = new StringBuffer();
                for (int i = 0; i < reports.length; i++) {
                    if (i > 0) {
                        reportNames.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830006-0121")/*@res "��"*/);
                    }
                    reportNames.append(reports[i].getNameWithCode());
                }
                originMsg.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0102")
                /* @res���� */);
                originMsg.append(reportNames);
                originMsg.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0103")
                /* @res���������ڷֲ�ʽϵͳ[ */);
                originMsg.append(dataOrigin);
                originMsg.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0104")
                /* @res]�������޸Ļ�ɾ���� */);
            }
        }
        return originMsg.toString();
    }

    /**
     * ɾ��vo
     * @param resultVO
     * @throws Exception
     */
    protected String deleteVO(RepDataQueryResultVO resultVO) throws Exception {
    	return HBPubItfService.getRemoteUnionReport().delUnionReport(resultVO);
    }

    public HBReportQueryExecutor getQueryExecutor() {
        return queryExecutor;
    }

    public void setQueryExecutor(HBReportQueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

}