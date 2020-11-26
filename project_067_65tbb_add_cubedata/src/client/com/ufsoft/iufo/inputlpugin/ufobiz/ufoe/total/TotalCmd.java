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
 * �������ݻ�������
 * @author xulm
 * @created at 2010-5-14,����04:22:10
 *
 */
public class TotalCmd  extends AbsUfoBizCmd {
	// ���ܽ��ҳ���ID
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
		//��ǰ�û�
		final String oper_user = editor.getRepDataParam().getOperUserPK();
		//����֯
		final String mainOrgPK = editor.getRepDataParam().getRepOrgPK();

    	if (!stopCellEditing(editor))
    		return;

    	if (!AbsUfoDynAreaActionExt.verifyBeforeSave(editor))
    		return;

    	//TODO: ִ�л���֮ǰ�Ƿ���ʾ����?

		Runnable runnable=new Runnable() {
			@Override
			public void run() {
				IUFOCombRepDataEditor repDataEditor = (IUFOCombRepDataEditor)editor.getParentEditor();
				TaskVO task = repDataEditor.getTask();
				MeasurePubDataVO pubData = repDataEditor.getPubData();
				AbsCombRepDataControler controler = (AbsCombRepDataControler) AbsRepDataControler.getInstance(repDataEditor.getMainboard());

				String strHintMsg = doExecute(params,oper_user,mainOrgPK);

				//liuchun �޸ģ���ִ����֮���Ѿ�ˢ�¹�����
//				controler.doSetTabStatus(repDataEditor.getMainboard(), pubData.getAloneID(), task.getPk_task());

				SwingUtilities.invokeLater(new LocateRunnable(strHintMsg));
			}
		};

		editor.getTable().getCells().setEnabled(false);
		editor.getMainboard().getStatusBar().processDisplay(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0000")/*@res "ϵͳ���ڴ�������, ���Ժ�..."*/,runnable);
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
    		else if(m_totalMenu==TotalMenu.MENU_BALANCE_TOTAL){	//������
    			extendParams=(boolean[])params[3];
    			resultVO=TotalSrvUtils.createBalanceTotalResults((MeasurePubDataVO)params[0], busiTime,(TotalSchemeVO)params[1], (String[])params[2], (boolean[])params[3],(String)params[4],(String)params[5],oper_user,mainOrgPk);
    		}
			if (resultVO.isOperSuccess()){
    			strHintMsg=nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0082")/*@res "����ִ�����"*/;
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
				//add by tanyj ������Ϻ��ȼ�¼�±����������Դ
				TotalSrvUtils.saveRepDataSrc((MeasurePubDataVO)params[0], repIds);
				
				//������ϣ�ˢ����ͼ modified by jiaah at 2011-3-9
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
							// �򿪱���ʱ��ֻ���ý��㣬�������б༭
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
//				//���������ñ��������޸�
//				editor.getTable().setEnabled(false);
//			}

    	}catch(Exception e){
    		AppDebug.debug(e);
    		strHintMsg=m_totalMenu.toString()+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0083")/*@res "ִ��ʧ�ܣ�"*/+e.getMessage();

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
	 * ����ִ�����ˢ����ͼ
	 * @create by jiaah at 2011-3-29,����07:27:07
	 */
	private void refreshView(Object[] params){
		
		if(m_menuItem != null && m_menuItem == TotalMenu.MENU_TOTAL_CUSTOM) {
			// ����...�����л��������������Բ����Զ�ˢ�£���Ҫ�ֹ�ˢ������
			
			//edit by congdy 2015.5.12 ˢ�µ�ǰ��ͼ
			Viewer curView = getRepDataEditor().getMainboard().getCurrentView();
			if(curView instanceof IUFOCombRepDataEditor){
				((IUFOCombRepDataEditor)curView).getActiveRepDataEditor().clearDirty();
				((IUFOCombRepDataEditor)curView).refreshContent(null);//ˢ�µ�ǰ�����editor
			}
			return;
		}
		
		//������ͼ
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
			//��ǰ�򿪵�����IUFOCombRepDataEditor
			Viewer[] combViews = lstCombEditors.toArray(new Viewer[0]);
			for(Viewer viewer : combViews){
				String taskPK = ((IUFOCombRepDataEditor)viewer).getTaskPK();
				if(taskPK.equals(taskID))
					lstRefreshEditors.add((IUFOCombRepDataEditor)viewer);
			}
		}

		//��ǰ��ͼ
		Viewer curView = getRepDataEditor().getMainboard().getCurrentView();

		if (m_totalMenu == TotalMenu.MENU_TOTAL){
			if(curView instanceof IUFOCombRepDataEditor){
				((IUFOCombRepDataEditor)curView).getActiveRepDataEditor().clearDirty();
				((IUFOCombRepDataEditor)curView).refreshContent(null);//ˢ�µ�ǰ�����editor
			}
		}
		else if (m_totalMenu == TotalMenu.MENU_TASK_TOTAL){
			//���������Ļ���ִ�У���ִ�н����Ժ�ֻˢ�µ�ǰ����ҳǩ��������ҳǩ״̬��Ϊδ����
			IRepDataEditorInComb[] editors = null;
			if(curView instanceof IUFOCombRepDataEditor){
				editors = ((IUFOCombRepDataEditor) curView).getAllRepDataEditors();
				IUFORepDataEditor currCombRepdataEditor = (IUFORepDataEditor)((IUFOCombRepDataEditor)curView).getActiveRepDataEditor();
				currCombRepdataEditor.clearDirty();
				currCombRepdataEditor.refreshContent(null);
				//wangqi 20120423 �޸������� �л�editorʱ��ˢ��
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
//					}// ˢ�µ�ǰ���е�editor
//				}
			}
		}
		else if(m_totalMenu == TotalMenu.MENU_TOTALSUB){
			IUFORepDataEditor currCombRepdataEditor = (IUFORepDataEditor)((IUFOCombRepDataEditor)curView).getActiveRepDataEditor();
			currCombRepdataEditor.clearDirty();
			currCombRepdataEditor.refreshContent(null);

			//wangqi 20120423 �޸������� �л�editorʱ��ˢ��
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
////					curEditor.setLoadedData(false);//ˢ���Ѿ��򿪵ĵ�ǰ�����µ����м����editor
//					curEditor.refreshContent(null);
//				}
//			}
		}
		else if(m_totalMenu == TotalMenu.MENU_TASK_TOTALSUB || m_totalMenu==TotalMenu.MENU_TOTAL_CUSTOM){
			IUFORepDataEditor currCombRepdataEditor = (IUFORepDataEditor)((IUFOCombRepDataEditor)curView).getActiveRepDataEditor();
			currCombRepdataEditor.clearDirty();
			currCombRepdataEditor.refreshContent(null);

			//wangqi 20120423 �޸������� �л�editorʱ��ˢ��
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
//						((IUFORepDataEditor)edit).refreshContent(null);//ˢ���Ѿ��򿪵ĵ�ǰ�����µ����е�editor
////						((IUFORepDataEditor)edit).setLoadedData(false);//ˢ���Ѿ��򿪵ĵ�ǰ�����µ����е�editor
//					}
//				}
//			}
		}

//		else if (m_totalMenu==TotalMenu.MENU_TOTAL_CUSTOM){
//			//TODO : �Զ������ʱˢ��ʲô��ˢ�����ļ���������¼���ˢ��
//			if(curView instanceof IUFOCombRepDataEditor){
//				((IUFOCombRepDataEditor)curView).getActiveRepDataEditor().clearDirty();
//				((IUFOCombRepDataEditor)curView).refreshContent(null);
//			}
//		}
	}



	/**
	 * �򿪻��ܽ��
	 * @create by xulm at 2010-7-2,����03:04:21
	 *
	 * @param mainBoard
	 * @param results
	 * @param pubData
	 * @param extendParams
	 */
	private  void openTotalResultView(Mainboard mainBoard,TotalResultVO[] results,MeasurePubDataVO pubData,boolean[] extendParams) {
		//�����Ƿ��л��ܽ�������򿪻��ܽ���Ի��� modified by jiaah
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