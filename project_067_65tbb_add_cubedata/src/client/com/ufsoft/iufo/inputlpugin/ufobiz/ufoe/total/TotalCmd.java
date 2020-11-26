/**
 *
 */
package com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.total;



import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.zior.view.Mainboard;
import com.ufida.zior.view.Viewer;
import com.ufsoft.iufo.fmtplugin.dynarea.DynAreaUtil;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.total.BaseTotalAction.TotalMenu;
import com.ufsoft.iufo.inputplugin.ufobiz.AbsUfoBizCmd;
import com.ufsoft.iufo.inputplugin.ufodynarea.AbsUfoDynAreaActionExt;
import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;
import com.ufsoft.report.util.UfoPublic;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.ICellAuth;
import com.ufsoft.table.TableSetting;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.ui.iufo.input.control.base.AbsRepDataControler;
import nc.ui.iufo.input.edit.base.IRepDataEditor;
import nc.ui.iufo.input.ufoe.comp.AbsCombRepDataControler;
import nc.ui.iufo.input.ufoe.comp.AbsRepDataEditorInComb;
import nc.ui.iufo.input.ufoe.comp.IRepDataEditorInComb;
import nc.ui.iufo.input.ufoe.edit.IUFOCombRepDataEditor;
import nc.ui.iufo.input.ufoe.edit.IUFORepDataEditor;
import nc.ui.iufo.input.ufoe.view.IUFOTotalResultViewer;

import nc.util.iufo.repdata.v5.IUFOKeyWordFillEnv;
import nc.util.iufo.repdata.v5.IUFORepDataFromV5Util;
import nc.utils.iufo.TotalSrvUtils;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iufo.total.TotalResultVO;
import nc.vo.iufo.total.TotalSchemeVO;
import nc.vo.jcom.lang.StringUtil;

/**
 * 报表数据汇总命令
 * @author xulm
 * @created at 2010-5-14,下午04:22:10
 *
 */
public class TotalCmd  extends AbsUfoBizCmd {
	// 汇总结果页板的ID
	transient public static String TOTAL_RESULT_ID = "iufo.input.totalresult.view";

	private TotalMenu m_totalMenu=TotalMenu.MENU_TOTAL;
	private TotalMenu m_menuItem = null;
	public TotalCmd(IRepDataEditor editor,TotalMenu totalMenu, TotalMenu m_menuItem){
		super(editor);
		m_totalMenu=totalMenu;
		this.m_menuItem = m_menuItem;
	}

	@Override
	protected void executeIUFOBizCmd(final IRepDataEditor editor, final Object[] params) {
		if(editor == null){
			return ;
		}
		//当前用户
		final String oper_user = editor.getRepDataParam().getOperUserPK();
		//主组织
		final String mainOrgPK = editor.getRepDataParam().getRepOrgPK();

    	if (!stopCellEditing(editor))
    		return;

    	if (!AbsUfoDynAreaActionExt.verifyBeforeSave(editor))
    		return;

    	//TODO: 执行汇总之前是否提示保存?

		Runnable runnable=new Runnable() {
			@Override
			public void run() {
				IUFOCombRepDataEditor repDataEditor = (IUFOCombRepDataEditor)editor.getParentEditor();
				TaskVO task = repDataEditor.getTask();
				MeasurePubDataVO pubData = repDataEditor.getPubData();
				AbsCombRepDataControler controler = (AbsCombRepDataControler) AbsRepDataControler.getInstance(repDataEditor.getMainboard());

				String strHintMsg = doExecute(params,oper_user,mainOrgPK);

				//liuchun 修改，在执行完之后已经刷新过界面
//				controler.doSetTabStatus(repDataEditor.getMainboard(), pubData.getAloneID(), task.getPk_task());

				SwingUtilities.invokeLater(new LocateRunnable(strHintMsg));
			}
		};

		editor.getTable().getCells().setEnabled(false);
		editor.getMainboard().getStatusBar().processDisplay(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0000")/*@res "系统正在处理请求, 请稍候..."*/,runnable);
		editor.getTable().getCells().setEnabled(true);
	}

	@SuppressWarnings("unchecked")
	private String doExecute(Object[] params,String oper_user,String mainOrgPk){
        String strHintMsg="";
        boolean[] extendParams=null;
		RepDataOperResultVO resultVO=null;
		TotalResultVO[] totalResults=null;
		String busiTime = WorkbenchEnvironment.getInstance().getBusiDate().toString();
		try{
    		if (m_totalMenu==TotalMenu.MENU_TOTAL ||m_totalMenu==TotalMenu.MENU_TASK_TOTAL)
    		{
    			extendParams=(boolean[])params[3];
    			resultVO=TotalSrvUtils.createTotalResults((MeasurePubDataVO)params[0], busiTime,(TotalSchemeVO)params[1], (String[])params[2], (boolean[])params[3],(String)params[4],oper_user,mainOrgPk);
    		}
    		else if (m_totalMenu==TotalMenu.MENU_TOTALSUB ||m_totalMenu==TotalMenu.MENU_TASK_TOTALSUB)
    		{
    			extendParams=(boolean[])params[4];
    			resultVO=TotalSrvUtils.createTotalSubResults((MeasurePubDataVO)params[0], busiTime,(String)params[1],  (String)params[2], (String[])params[3], (boolean[])params[4], (String)params[5],oper_user,mainOrgPk);
    		}
    		else if (m_totalMenu==TotalMenu.MENU_TOTAL_CUSTOM )
    		{
    			extendParams=(boolean[])params[4];
    			resultVO=TotalSrvUtils.createCustomTotalResults((MeasurePubDataVO)params[0], busiTime,(String)params[1],(String[])params[2], (String[])params[3], (boolean[])params[4], (String)params[5],false,oper_user,mainOrgPk);
    		}
    		else if(m_totalMenu==TotalMenu.MENU_BALANCE_TOTAL){	//差额汇总
    			extendParams=(boolean[])params[3];
    			resultVO=TotalSrvUtils.createBalanceTotalResults((MeasurePubDataVO)params[0], busiTime,(TotalSchemeVO)params[1], (String[])params[2], (boolean[])params[3],(String)params[4],(String)params[5],oper_user,mainOrgPk);
    		}
			if (resultVO.isOperSuccess()){
    			strHintMsg=nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0082")/*@res "汇总执行完成"*/;
    		}else{
    			strHintMsg=resultVO.getHintMessage();
    		}


			if (!StringUtil.isEmptyWithTrim(getRepDataEditor().getAloneID())){
				String[] repIds = null;
				if (m_totalMenu==TotalMenu.MENU_TOTAL ||m_totalMenu==TotalMenu.MENU_TASK_TOTAL 
						|| m_totalMenu==TotalMenu.MENU_BALANCE_TOTAL){
					repIds = (String[])params[2];
				}else if (m_totalMenu==TotalMenu.MENU_TOTALSUB ||m_totalMenu==TotalMenu.MENU_TASK_TOTALSUB){
					repIds = (String[])params[3];
				}else if (m_totalMenu==TotalMenu.MENU_TOTAL_CUSTOM ){
					repIds = (String[])params[3];
				}
				//add by tanyj 汇总完毕后，先记录下报表的数据来源
				TotalSrvUtils.saveRepDataSrc((MeasurePubDataVO)params[0], repIds);
				
				//汇总完毕，刷新视图 modified by jiaah at 2011-3-9
				refreshView(params);
				CellsModel cellsModel = getRepDataEditor().getCellsModel();
				TableSetting tableSetting = cellsModel.getTableSetting();
				ICellAuth auth = tableSetting.getCellsAuth();
				int iMaxRow = cellsModel.getRowNum();
				int iMaxCol = cellsModel.getColNum();
				for (int iRow = 0; iRow < iMaxRow; iRow++){
					for (int iCol = 0; iCol < iMaxCol; iCol++){
//						if (auth == null || auth.isWritable(iRow, iCol)){
							CellPosition cellPos = CellPosition.getInstance(iRow, iCol);
							// 打开报表时，只设置焦点，而不进行编辑
//							editCell(cellPos);
							cellsModel.getSelectModel().clearAnchor();
							cellsModel.getSelectModel().setAnchorCell(cellPos);
							return null;
//						}
					}
				}
			}

			//
//			Boolean isEditable = SysParamQueryUtil.getTotalCanModify();
//			if(isEditable){
//				//在哪里设置报表不可以修改
//				editor.getTable().setEnabled(false);
//			}

    	}catch(Exception e){
    		AppDebug.debug(e);
    		strHintMsg=m_totalMenu.toString()+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0083")/*@res "执行失败："*/+e.getMessage();

    	}finally{
    		if (resultVO!=null)
    		{
    		    totalResults=(TotalResultVO[])resultVO.getDetail().toArray(new TotalResultVO[0]);
    		}
    		openTotalResultView(getRepDataEditor().getMainboard(),totalResults,(MeasurePubDataVO)params[0],extendParams);
    		return strHintMsg;
    	}
	}

	/**
	 * 汇总执行完毕刷新视图
	 * @create by jiaah at 2011-3-29,下午07:27:07
	 */
	private void refreshView(Object[] params){
		
		if(m_menuItem != null && m_menuItem == TotalMenu.MENU_TOTAL_CUSTOM) {
			// 汇总...可以切换汇总条件，所以不做自动刷新，需要手工刷新数据
			
			//edit by congdy 2015.5.12 刷新当前视图
			Viewer curView = getRepDataEditor().getMainboard().getCurrentView();
			if(curView instanceof IUFOCombRepDataEditor){
				((IUFOCombRepDataEditor)curView).getActiveRepDataEditor().clearDirty();
				((IUFOCombRepDataEditor)curView).refreshContent(null);//刷新当前激活的editor
			}
			return;
		}
		
		//所有视图
		Viewer[] viewers = getRepDataEditor().getMainboard().getAllViews();
		List<Viewer> lstCombEditors = new ArrayList<Viewer>();
		for(Viewer view : viewers){
			if(view instanceof IUFOCombRepDataEditor)
				lstCombEditors.add(view);
		}
		String taskID = null;
		List<IUFOCombRepDataEditor> lstRefreshEditors = new ArrayList<IUFOCombRepDataEditor>();
		if(m_totalMenu == TotalMenu.MENU_TOTALSUB || m_totalMenu == TotalMenu.MENU_TASK_TOTALSUB){
			taskID = (String)params[5];
			//当前打开的所有IUFOCombRepDataEditor
			Viewer[] combViews = lstCombEditors.toArray(new Viewer[0]);
			for(Viewer viewer : combViews){
				String taskPK = ((IUFOCombRepDataEditor)viewer).getTaskPK();
				if(taskPK.equals(taskID))
					lstRefreshEditors.add((IUFOCombRepDataEditor)viewer);
			}
		}

		//当前视图
		Viewer curView = getRepDataEditor().getMainboard().getCurrentView();

		if (m_totalMenu == TotalMenu.MENU_TOTAL){
			if(curView instanceof IUFOCombRepDataEditor){
				((IUFOCombRepDataEditor)curView).getActiveRepDataEditor().clearDirty();
				((IUFOCombRepDataEditor)curView).refreshContent(null);//刷新当前激活的editor
			}
		}
		else if (m_totalMenu == TotalMenu.MENU_TASK_TOTAL){
			//如果是任务的汇总执行，在执行结束以后只刷新当前数据页签，其它的页签状态设为未加载
			IRepDataEditorInComb[] editors = null;
			if(curView instanceof IUFOCombRepDataEditor){
				editors = ((IUFOCombRepDataEditor) curView).getAllRepDataEditors();
				IUFORepDataEditor currCombRepdataEditor = (IUFORepDataEditor)((IUFOCombRepDataEditor)curView).getActiveRepDataEditor();
				currCombRepdataEditor.clearDirty();
				currCombRepdataEditor.refreshContent(null);
				//wangqi 20120423 修改连接数 切换editor时再刷新
				for(IRepDataEditorInComb editor : editors){
				if (editor != currCombRepdataEditor && editor instanceof AbsRepDataEditorInComb) {
					((AbsRepDataEditorInComb) editor).setLoadedData(false);
				}
			}
//				for(IRepDataEditorInComb editor : editors){
//					if (editor != currCombRepdataEditor) {
//						editor.clearDirty();
////						((IUFORepDataEditor) editor).setLoadedData(false);
//						((IUFORepDataEditor) editor).refreshContent(null);
//					}// 刷新当前所有的editor
//				}
			}
		}
		else if(m_totalMenu == TotalMenu.MENU_TOTALSUB){
			IUFORepDataEditor currCombRepdataEditor = (IUFORepDataEditor)((IUFOCombRepDataEditor)curView).getActiveRepDataEditor();
			currCombRepdataEditor.clearDirty();
			currCombRepdataEditor.refreshContent(null);

			//wangqi 20120423 修改连接数 切换editor时再刷新
//			for(IUFOCombRepDataEditor editor : lstRefreshEditors.toArray(new IUFOCombRepDataEditor[0])){
//				IUFORepDataEditor curEditor = (IUFORepDataEditor) editor.getActiveRepDataEditor();
//				if(curEditor != currCombRepdataEditor){
//					((AbsRepDataEditorInComb) curEditor).setLoadedData(false);;
//				}
//			}

//			for(IUFOCombRepDataEditor editor : lstRefreshEditors.toArray(new IUFOCombRepDataEditor[0])){
//				IUFORepDataEditor curEditor = (IUFORepDataEditor) editor.getActiveRepDataEditor();
//				if(curEditor != currCombRepdataEditor){
//					curEditor.clearDirty();
////					curEditor.setLoadedData(false);//刷新已经打开的当前任务下的所有激活的editor
//					curEditor.refreshContent(null);
//				}
//			}
		}
		else if(m_totalMenu == TotalMenu.MENU_TASK_TOTALSUB || m_totalMenu==TotalMenu.MENU_TOTAL_CUSTOM){
			IUFORepDataEditor currCombRepdataEditor = (IUFORepDataEditor)((IUFOCombRepDataEditor)curView).getActiveRepDataEditor();
			currCombRepdataEditor.clearDirty();
			currCombRepdataEditor.refreshContent(null);

			//wangqi 20120423 修改连接数 切换editor时再刷新
//			for(IUFOCombRepDataEditor editor : lstRefreshEditors.toArray(new IUFOCombRepDataEditor[0])){
//				IRepDataEditorInComb[] editors = editor.getAllRepDataEditors();
			    IRepDataEditorInComb[] editors = ((IUFOCombRepDataEditor) curView).getAllRepDataEditors();
				for(IRepDataEditorInComb edit:editors){
					if (edit != currCombRepdataEditor && edit instanceof AbsRepDataEditorInComb) {
						((AbsRepDataEditorInComb) edit).setLoadedData(false);;
					}
				}
//			}

//			for(IUFOCombRepDataEditor editor : lstRefreshEditors.toArray(new IUFOCombRepDataEditor[0])){
//				IRepDataEditorInComb[] editors = editor.getAllRepDataEditors();
//				for(IRepDataEditorInComb edit:editors){
//					if (edit != currCombRepdataEditor) {
//						edit.clearDirty();
//						((IUFORepDataEditor)edit).refreshContent(null);//刷新已经打开的当前任务下的所有的editor
////						((IUFORepDataEditor)edit).setLoadedData(false);//刷新已经打开的当前任务下的所有的editor
//					}
//				}
//			}
		}

//		else if (m_totalMenu==TotalMenu.MENU_TOTAL_CUSTOM){
//			//TODO : 自定义汇总时刷新什么，刷新最大的即任务汇总下级的刷新
//			if(curView instanceof IUFOCombRepDataEditor){
//				((IUFOCombRepDataEditor)curView).getActiveRepDataEditor().clearDirty();
//				((IUFOCombRepDataEditor)curView).refreshContent(null);
//			}
//		}
	}



	/**
	 * 打开汇总结果
	 * @create by xulm at 2010-7-2,下午03:04:21
	 *
	 * @param mainBoard
	 * @param results
	 * @param pubData
	 * @param extendParams
	 */
	private  void openTotalResultView(Mainboard mainBoard,TotalResultVO[] results,MeasurePubDataVO pubData,boolean[] extendParams) {
		//无论是否有汇总结果，都打开汇总结果对话框 modified by jiaah
		mainBoard.openView(IUFOTotalResultViewer.class.getName(),TOTAL_RESULT_ID);
		IUFOTotalResultViewer view = (IUFOTotalResultViewer) mainBoard.getView(TOTAL_RESULT_ID);
		if (view != null){
			view.initData(results,pubData,extendParams);
		}
	}

	private class LocateRunnable implements Runnable{
		String message=null;
		LocateRunnable(String message){
			this.message=message;
		}
		@Override
		public void run() {
			if (message!=null)
				UfoPublic.sendMessage(message,editor.getMainboard());
		}
	}
}