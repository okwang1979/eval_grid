package nc.ui.hbbb.total.action;

import java.awt.event.ActionEvent;

import nc.funcnode.ui.action.AbstractNCAction;
import nc.ui.hbbb.total.TotalSchemeManageUI;
import nc.vo.hbbb.total.HbTotalOrgTreeVO;

public class TotalSetAction extends AbstractNCAction{
 
	private static final long serialVersionUID = -6870539295062398107L;
	
	
	private TotalSchemeManageUI ui;
	
	
	public TotalSetAction(TotalSchemeManageUI ui){
		super("edit","设置","合并规则设置");
		this.ui = ui;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		HbTotalOrgTreeVO seletcVO = ui.getSelect() ;
		if(seletcVO!=null ){
			if(seletcVO.getScheme()==null){
				ui.getDriectChildButton().setSelected(true);
			} 
		} else{
			return;
		}
		
		ui.setState(TotalSchemeManageUI.EDIT);
		ui.refreshMenuActions();
	 
		
	}
	


 

}
