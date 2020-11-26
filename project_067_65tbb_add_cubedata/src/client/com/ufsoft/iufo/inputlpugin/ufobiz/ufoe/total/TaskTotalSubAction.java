/**
 * 
 */
package com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.total;

import nc.ui.iufo.constants.IUfoeActionCode;

/**
 * 任务汇总下级Action
 * @author xulm
 * @created at 2010-6-28,上午10:12:07
 *
 */
public class TaskTotalSubAction extends BaseTotalAction{
    
	public TaskTotalSubAction(){
    	super.setMenuItem(TotalMenu.MENU_TASK_TOTALSUB);
    	super.setCode(IUfoeActionCode.INNER_TASKSUB);
    	super.setMemonic('M');
    }
	
//	@Override
//	public IPluginActionDescriptor getPluginActionDescriptor() {
//		PluginActionDescriptor des = new PluginActionDescriptor(getMenuItem().toString());
//		des.setExtensionPoints(XPOINT.MENU);
//		des.setToolTipText(getMenuItem().toString());
//		des.setGroupPaths(doGetTotalMenuPaths(TotalMenu.MENU_TASK_TOTAL.toString()));
//		return des;
//	}
}
