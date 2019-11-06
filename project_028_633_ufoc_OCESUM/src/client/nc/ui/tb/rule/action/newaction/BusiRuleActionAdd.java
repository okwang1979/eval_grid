package nc.ui.tb.rule.action.newaction;

import java.awt.event.ActionEvent;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.funcnode.ui.action.AbstractNCAction;
import nc.itf.mdm.dim.INtbSuper;
import nc.itf.tb.rule.INodeTypeConst;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ms.tb.rule.RuleServiceGetter;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.UIDialog;
import nc.ui.tb.dialog.CreateObjnameDlg;
import nc.ui.tb.rule.NodeConfig_BizRule;
import nc.ui.tb.rule.dialog.CreateBusiRuleDlg;
import nc.ui.tb.table.NtbActionIconCtl;
import nc.view.mdm.constpub.IDimControlStatus;
import nc.view.tb.rule.BusiRuleTreePanel;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.tb.rule.BusiRuleVO;

public class BusiRuleActionAdd  extends AbstractTbRuleAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2833485874298764060L;
	
	
	
	public BusiRuleActionAdd(BusiRuleTreePanel busiRuleTreePanel) {
		super(busiRuleTreePanel,NtbActionIconCtl.ACTION_NEW,NtbActionIconCtl.getName(NtbActionIconCtl.ACTION_NEW),NtbActionIconCtl.getName(NtbActionIconCtl.ACTION_NEW));
		NtbActionIconCtl.setIcon(this);
		setEnabled(false);
	}
	@Override
	public void doAction(ActionEvent e) throws BusinessException {
		INtbSuper ntbsuper = NtbSuperServiceGetter.getINtbSuper();
		String sql = "create or replace  view v_iufo_other_data_001 as  select * from iufo_other_data@NC63_ZM";
		ntbsuper.execUpdate(sql);
		
		String bigClass = busiRuleTreePanel.getFramui().getBusiRuleTreeModel().getBigClass();
		CreateBusiRuleDlg dlg = new CreateBusiRuleDlg(busiRuleTreePanel.getFramui(),bigClass);
		if(dlg.showModal()==UIDialog.ID_OK){
			String ruleClass = busiRuleTreePanel.getFramui().getBusiRuleTreeModel().getRuleClass();
			String busiName = dlg.getTfName().getMultiLangText().toString().trim();
			String busiDesc = dlg.getTfDesc().getText();
			if(busiDesc!=null) busiDesc = busiDesc.trim();
			
			BusiRuleVO vo = new BusiRuleVO();
			vo.setObjname(busiName);
			vo.setDescript(busiDesc);
			vo.setPk_bigClass(bigClass);
			vo.setPk_ruleclass(ruleClass);
			
			vo.setPk_org(busiRuleTreePanel.getFramui().getCurrOrgPk());
			vo.setPk_Group(WorkbenchEnvironment.getInstance().getGroupVO().getPk_group());
			
			String pk_user = WorkbenchEnvironment.getInstance().getLoginUser().getPrimaryKey();
			UFDateTime time = WorkbenchEnvironment.getInstance().getServerTime();
			vo.setCreatedby(pk_user);
			vo.setCreateddate(time);
	    	String sysCode = busiRuleTreePanel.getFramui().getGroupAppPanel().getCurrSysCode();
	    	vo.setSysCode(sysCode);
	    	vo.setIssystem(UFBoolean.valueOf(false));
	    	vo.setRuletype("0");
			
			String pk = RuleServiceGetter.getIBusiRuleManager().addBusiRuleVO(vo);
			vo.setPk_obj(pk);
			
			((NodeConfig_BizRule)busiRuleTreePanel.getFramui().getNodeConfig()).getBusiRuleTreePnl().initLeftTree();
			busiRuleTreePanel.getFramui().getBusiRuleTreeModel().setSelectedObj(vo);
			busiRuleTreePanel.getFramui().changedToRuleBrowse();
		}
//		CreateObjnameDlg dlg = new CreateObjnameDlg(busiRuleTreePanel.getFramui(), "新增规则", "规则名称", ui.getCurrSysCode(), ui.getCurrOrgName(), true);
	}
	public void refreshButtonStatus() {
		Object selectedObj = this.busiRuleTreePanel.getUIModel().getSelectedObj();
		if(selectedObj==null&& this.busiRuleTreePanel.getUIModel().getRuleClass()!=null){
			setEnabled(true);
			return ;
		}
		setEnabled(false);
	}
	
//	protected boolean isCurrentOrgBusiRule() {
//
//		String pkOrg = busiRuleTreePanel.getFramui().getCurrOrgPk();
//		Object obj = busiRuleTreePanel.getFramui().getBusiRuleTreeModel().getSelectedObj();
//		if (obj instanceof BusiRuleVO) {
//			String pk = ((BusiRuleVO) obj).getOrgPk();
//			if (pkOrg == null || !pkOrg.equals(pk))
//				return false;
//		}
//		if (obj instanceof Object[]) {
//			Object[] selects = (Object[]) obj;
//			if (selects.length > 0 && selects[0] instanceof BusiRuleVO) {
//				String pk=((BusiRuleVO)selects[0]).getOrgPk();
//				if(pkOrg==null||!pkOrg.equals(pk))
//					 return false;
//			}
//	
//		}
//		if(!canEditRule()){
//			return false;
//		}
//
//		return true;
//	}
	
}
