package nc.ui.hbbb.dxrelation.action;

import java.awt.event.ActionEvent;

import nc.ui.bd.pub.BDOrgPanel;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.uif2.actions.AddAction;
import nc.ui.uif2.actions.OrgRefFocusUtil;
import nc.ui.uif2.model.HierachicalDataAppModel;

import org.apache.commons.lang.StringUtils;

/**
 * 抵销模板新建action
 * 
 * @author liujunc
 * @created at 2010-4-16,上午10:16:38
 */
public class DXAddAction extends AddAction {

    private static final long serialVersionUID = -7729440520146366663L;

    private HierachicalDataAppModel sortModel;

    private BDOrgPanel orgPanel;

    @Override
    public void doAction(ActionEvent e) throws Exception {

        // 首先让组织参照停止编辑
    	if(getOrgPanel() != null) {
    		this.getOrgPanel().getRefPane().stopEditing();
    	}
        if (StringUtils.isBlank(this.getModel().getContext().getPk_org())) {
            MessageDialog.showErrorDlg(this.getModel().getContext().getEntranceUI(), null, nc.vo.ml.NCLangRes4VoTransl
                    .getNCLangRes().getStrByID("pub_0", "01830001-0002")/* @res "请选择财务组织！" */);
            OrgRefFocusUtil.requestFocus(this.getModel().getContext());
            return;
        }

        // CommonVouchSortVO obj = ((ComVouchManageModel)getModel()).getVouchSort();
        // if(obj == null){
        // if(VouchFuncParamUtil.getVouchType(getModel().getContext()).equals(IVouchFuncParam.COMVOUCHTYPE_DX))
        // throw new BusinessException("请选择常用抵销分录分类，如果没有常用抵销分录分类请新建！");
        // else
        // throw new BusinessException("请选择常用调整凭证分类，如果没有常用调整凭证分类请新建！");
        // }
//        SuperVO parentVo = ((DXManageModel) this.getModel()).getParentVo();
//        if (parentVo == null || !(parentVo instanceof DXRelaSortVO)) {
//            throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0203")/*
//                                                                                                                        * @
//                                                                                                                        * res
//                                                                                                                        * "请选择模板分类，如果没有模板分类请新建"
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
