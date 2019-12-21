package nc.ui.hbbb.dxrelation.action;

import java.awt.event.ActionEvent;

import nc.ui.bd.pub.BDOrgPanel;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.uif2.actions.AddAction;
import nc.ui.uif2.actions.OrgRefFocusUtil;
import nc.ui.uif2.model.HierachicalDataAppModel;

import org.apache.commons.lang.StringUtils;

/**
 * ����ģ���½�action
 * 
 * @author liujunc
 * @created at 2010-4-16,����10:16:38
 */
public class DXAddAction extends AddAction {

    private static final long serialVersionUID = -7729440520146366663L;

    private HierachicalDataAppModel sortModel;

    private BDOrgPanel orgPanel;

    @Override
    public void doAction(ActionEvent e) throws Exception {

        // ��������֯����ֹͣ�༭
    	if(getOrgPanel() != null) {
    		this.getOrgPanel().getRefPane().stopEditing();
    	}
        if (StringUtils.isBlank(this.getModel().getContext().getPk_org())) {
            MessageDialog.showErrorDlg(this.getModel().getContext().getEntranceUI(), null, nc.vo.ml.NCLangRes4VoTransl
                    .getNCLangRes().getStrByID("pub_0", "01830001-0002")/* @res "��ѡ�������֯��" */);
            OrgRefFocusUtil.requestFocus(this.getModel().getContext());
            return;
        }

        // CommonVouchSortVO obj = ((ComVouchManageModel)getModel()).getVouchSort();
        // if(obj == null){
        // if(VouchFuncParamUtil.getVouchType(getModel().getContext()).equals(IVouchFuncParam.COMVOUCHTYPE_DX))
        // throw new BusinessException("��ѡ���õ�����¼���࣬���û�г��õ�����¼�������½���");
        // else
        // throw new BusinessException("��ѡ���õ���ƾ֤���࣬���û�г��õ���ƾ֤�������½���");
        // }
//        SuperVO parentVo = ((DXManageModel) this.getModel()).getParentVo();
//        if (parentVo == null || !(parentVo instanceof DXRelaSortVO)) {
//            throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0203")/*
//                                                                                                                        * @
//                                                                                                                        * res
//                                                                                                                        * "��ѡ��ģ����࣬���û��ģ��������½�"
//                                                                                                                        */);
//        }

        super.doAction(e);
    }

    @Override
    protected boolean isActionEnable() {
        return sortModel != null && this.sortModel.getSelectedData() != null && !this.sortModel.getSelectedNode().isRoot();
    }

    public BDOrgPanel getOrgPanel() {
        return this.orgPanel;
    }

    public void setOrgPanel(BDOrgPanel orgPanel) {
        this.orgPanel = orgPanel;
    }

    public void setSortModel(HierachicalDataAppModel sortModel) {
        this.sortModel = sortModel;
    }

    public HierachicalDataAppModel getSortModel() {
        return this.sortModel;
    }
}
