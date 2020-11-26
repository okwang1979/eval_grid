package com.ufsoft.iufo.inputplugin.ufobiz;

import javax.swing.JOptionPane;

import nc.ui.iufo.input.control.base.AbsRepDataControler;
import nc.ui.iufo.input.edit.base.IRepDataEditor;
import nc.ui.iufo.input.view.base.CheckResultViewer;
import nc.ui.iufo.input.view.base.FormTraceResultViewer;

import com.ufida.zior.view.Viewer;
import com.ufsoft.iufo.inputplugin.ufobiz.data.UfoSaveRepDataCmd;

public abstract class AbsUfoBizCmd {
	protected IRepDataEditor editor=null;

    protected AbsUfoBizCmd(IRepDataEditor editor){
    	this.editor=editor;
    }

    public void execute(Object[] params) {
        if(isNeedCheckParams()){
            if(!isValidParams(params)){
                return;
            }
        }

        if(isNeedComfirmSave()){
            doComfirmSave(editor,true);
        }
        executeIUFOBizCmd(editor,params);
    }

    protected IRepDataEditor getRepDataEditor(){
    	return editor;
    }

    protected boolean isValidParams(Object[] params) {
        return true;
    }

    protected boolean isNeedCheckParams() {
        return false;
    }

    protected  boolean isNeedComfirmSave(){
        return false;
    }

    public static boolean stopCellEditing(IRepDataEditor editor){
    	if (editor.getTable()!= null && editor.getTable().getCellEditor()!=null)
    		return editor.getTable().getCellEditor().stopCellEditing();
    	return true;
    }

    public static boolean doComfirmSave(IRepDataEditor editor){
    	return doComfirmSave(editor, true);
    }

    /**
     * 执行保存，不显示状态栏，
     * 对于切换报表适用，用于解决线程执行顺序问题。
     * @param ufoReport
     * @param withProcess
     */
    public static boolean doComfirmSave(IRepDataEditor editor, boolean withProcess){
        if(editor == null){
            return false;
        }

        //提示保存已打开的报表
        if (editor.getTable().getCellEditor()!=null)
        	editor.getTable().getCellEditor().stopCellEditing();

        boolean isSucceedSave = true;
        boolean bDirty = editor.isDirty();
        if(bDirty){
            String strSaveAlert = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0548")/*@res "已打开的报表已修改，请确认是否保存？"*/;//"已打开的报表已修改，请确认是否保存？";
            int bConfirmReturn = JOptionPane.showConfirmDialog(
            		editor.getMainboard(),
                    strSaveAlert,
                    nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0549")/*@res "是否保存当前报表"*/,//"是否保存当前报表",
                    JOptionPane.OK_CANCEL_OPTION);
            if(bConfirmReturn == JOptionPane.OK_OPTION){
                if(withProcess){
                	UfoSaveRepDataCmd saveRepCmd = new UfoSaveRepDataCmd(editor);
                	saveRepCmd.execute(null);
                	isSucceedSave = saveRepCmd.isSucceedSave();
                } else{
                	isSucceedSave = new UfoSaveRepDataCmd(editor).save(editor);
                }
            }
        }

        return isSucceedSave;
    }
    /**
     * 执行IUFO的业务命令
     * @param ufoReport
     * @param params
     */
    protected abstract void executeIUFOBizCmd(IRepDataEditor editor, Object[] params);

    /**
     * 获得审核结果视图
     *
     * @create by liuchuna at 2010-7-1,下午06:46:01
     *
     * @param editor
     * @return
     */
    public CheckResultViewer getTraceResultView(IRepDataEditor editor){
		Viewer viewer = editor.getMainboard().getView(AbsRepDataControler.CHECK_RESULT_ID);
		if(viewer == null) {
			viewer = editor.getMainboard().openView(FormTraceResultViewer.class.getName(), AbsRepDataControler.CHECK_RESULT_ID);
		}
		return (CheckResultViewer) viewer;

	}
}
