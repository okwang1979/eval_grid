package nc.ui.gl.addvoucher;

import nc.funcnode.ui.AbstractFunclet;
import nc.funcnode.ui.action.AbstractNCAction;

public abstract class AbstractUfoManageUI extends AbstractFunclet{
	
	public final static int INIT = 0;
	public final static int EDIT = 1;
	
	private int state = INIT;
	
	private AbstractNCAction[] initActions;
	
	private AbstractNCAction[] editActions;
	
	
	
	public void refreshMenuActions() {
		setMenuActions(loadActions());
		
	}
	

	
	
	private AbstractNCAction[] loadActions() {
		if (state == INIT)
			return initActions;
		else if (state == EDIT)
			return editActions;
		else
			return initActions;
	}
	
	
	 void setEditEable(){
	 

		 
	 
	}
	


	public int getState() {
		return state;
	}


	public void setState(int state) {
		this.state = state;
		setEditEable();
	}


	public AbstractNCAction[] getInitActions() {
		return initActions;
	}


	public void setInitActions(AbstractNCAction[] initActions) {
		this.initActions = initActions;
	}


	public AbstractNCAction[] getEditActions() {
		return editActions;
	}


	public void setEditActions(AbstractNCAction[] editActions) {
		this.editActions = editActions;
	}
	
	
	
	
	

}
