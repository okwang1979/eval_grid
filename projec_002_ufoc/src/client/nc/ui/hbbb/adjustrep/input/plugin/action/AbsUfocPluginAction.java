package nc.ui.hbbb.adjustrep.input.plugin.action;

import nc.ui.iufo.input.edit.base.AbsBaseRepDataEditor;
import nc.ui.iufo.input.edit.base.IRepDataEditor;

import com.ufida.report.plugin.AbstractRepPluginAction;
import com.ufida.zior.view.Viewer;

@SuppressWarnings("restriction")
public abstract class AbsUfocPluginAction extends AbstractRepPluginAction{

	public boolean isEnabled() {
    	if(getCellsModel() == null){
    		return false;
    	}
    	try{
    		 return isRepOpened();
    	}catch(Exception ex){
    		return false;
    	}
       
    }
	
    protected boolean isRepOpened(){
    	Viewer curView = getCurrentView();
    	if (curView == null || curView instanceof AbsBaseRepDataEditor == false)
    		return false;
    	IRepDataEditor editor = getRepDataEditor();
    	// @edit by zhoushuang at 2015-4-14,ÏÂÎç2:37:07 ²î¶î±í£¬°´Å¥ÖÃ»Ò
    	if (editor.getPubData().getVer() - 250000 < 1000 && editor.getPubData().getVer() - 250000 > 0) {
    		return false;
    	}
    	return editor != null && editor.getAloneID() != null && editor.isSWReport() == false;
    }
    
    protected IRepDataEditor getRepDataEditor(){
    	AbsBaseRepDataEditor editor = (AbsBaseRepDataEditor)getCurrentView();
    	return editor.getActiveRepDataEditor();
	}
}
