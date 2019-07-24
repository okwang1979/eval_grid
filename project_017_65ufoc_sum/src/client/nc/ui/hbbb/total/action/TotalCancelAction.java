package nc.ui.hbbb.total.action;

import java.awt.event.ActionEvent;

import nc.funcnode.ui.action.AbstractNCAction;
import nc.ui.hbbb.total.TotalSchemeManageUI;

public class TotalCancelAction extends AbstractNCAction{
	 
		
		
		private TotalSchemeManageUI ui;
		
		
		public TotalCancelAction(TotalSchemeManageUI ui){
			super("Cancel","·µ»Ø","·µ»Ø");
			this.ui = ui;
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			ui.setState(TotalSchemeManageUI.INIT);
			ui.refreshMenuActions();
			
		}

}
