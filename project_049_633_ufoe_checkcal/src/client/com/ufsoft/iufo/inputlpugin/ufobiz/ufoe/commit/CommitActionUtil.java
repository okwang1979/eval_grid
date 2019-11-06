package com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.commit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.itf.iufo.approveset.IApproveQueryService;
import nc.itf.iufo.check.ICheckResultSrv;
import nc.itf.iufo.commit.ICommitQueryService;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.iufo.accbookcheck.util.AccBookCheckUtil;
import nc.ui.iufo.commit.view.AbsCommitActionSelRepProvider;
import nc.ui.iufo.commit.view.CommitActionSelRepDlg;
import nc.ui.iufo.input.ufoe.comp.AbsCombRepDataEditor;
import nc.ui.iufo.task.view.TaskAnnotationsDlg;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIDialog;
import nc.util.iufo.sysinit.UfoeSysParamQueryUtil;
import nc.vo.iufo.commit.CommitActionParamVO;
import nc.vo.iufo.commit.CommitActionSelRepVO;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.commit.TaskCommitVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.task.AllCommitStateEnum;
import nc.vo.iufo.task.ApproveReportSet;
import nc.vo.iufo.task.ICommitConfigConstant;
import nc.vo.iufo.task.TaskAnnotationVO;
import nc.vo.iufo.task.TaskApproveVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.pf.IPfRetCheckInfo;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.zior.view.Mainboard;
import com.ufida.zior.view.Viewer;
import com.ufsoft.iufo.check.vo.TaskCheckStateVO;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.commit.view.TaskCommitControler;
import com.ufsoft.report.util.UfoPublic;
import com.ufsoft.script.util.ICheckResultStatus;

/**
 * �������ݽ���˵��뱨�͹���˵����õı��ͷ���
 * @author weixl
 *
 */
public class CommitActionUtil {
	/**
	 * �ϱ�������
	 * @param mainBoard:
	 * @param task����Ӧ����
	 * @param pubData:��Ӧ�ؼ�������
	 * @throws Exception
	 */
	public static void doCommit(final Mainboard mainBoard,TaskVO task,MeasurePubDataVO pubData) throws Exception{
		final TaskCommitControler controler=TaskCommitControler.getInstance(mainBoard);
		AbsCommitActionSelRepProvider provider=controler.getCommitSelRepProvider();
		CommitActionUtil.innerDoCommitAction(mainBoard, task, pubData, provider, AllCommitStateEnum.TASK_COMMIT_VAL, new ICommitHandler(){
			@Override
			public void refreshEditorMenuState(AbsCombRepDataEditor combEditor) {
				try{
					controler.doRefreshView(mainBoard,combEditor,false,true,true,true);
				}catch(Exception e){
					AppDebug.debug(e);
				}
			}

			@Override
			public String getSucceedMessage() {
				//��������״̬����
				mainBoard.getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, CommitStateEnum.STATE_COMMITED.getIntValue());
				return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0021")/*@res "�����ϱ��ɹ�"*/;
			}
		});
	}

	/**
	 * �����˻�������
	 * @param mainBoard:
	 * @param task����Ӧ����
	 * @param pubData:��Ӧ�ؼ�������
	 * @throws Exception
	 */
	public static void doRequestCancel(final Mainboard mainBoard,TaskVO task,MeasurePubDataVO pubData) throws Exception{
		final TaskCommitControler controler=TaskCommitControler.getInstance(mainBoard);
		AbsCommitActionSelRepProvider provider=controler.getRequestCancelSelRepProvider();

		ICommitQueryService commitSrv=NCLocator.getInstance().lookup(ICommitQueryService.class);
		TaskCommitVO[] commits=commitSrv.getTaskCommitsByAloneIDs(task.getPk_task(), new String[]{pubData.getAloneID()});
		int flg = 0;
		if (commits[0].getCommit_state().intValue() == CommitStateEnum.STATE_COMMITED.getIntValue()){
//			flg = "requestbackcommit";
			flg = AllCommitStateEnum.REQUEST_BACK_COMMITED_VAL;
		}
		if (commits[0].getCommit_state().intValue() == CommitStateEnum.STATE_AFFIRMED.getIntValue()){
//			flg = "requestbackaffirm";
			flg = AllCommitStateEnum.REQUEST_BACK_AFFIRMED_VAL;
		}
		CommitActionUtil.innerDoCommitAction(mainBoard, task, pubData, provider, flg, new ICommitHandler(){
			@Override
			public void refreshEditorMenuState(AbsCombRepDataEditor combEditor) {
				try{
					controler.doRefreshView(mainBoard,combEditor,false,true,true,true);
				}catch(Exception e){
					AppDebug.debug(e);
				}
			}

			@Override
			public String getSucceedMessage() {
				return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0022")/*@res "���������˻سɹ�"*/;
			}
		});
	}

	private static void innerDoCommitAction(final Mainboard mainBoard,TaskVO task,MeasurePubDataVO pubData,AbsCommitActionSelRepProvider provider,int commitflg, ICommitHandler handler) throws Exception{
		AbsCombRepDataEditor combEditor=getCombRepDataEditor(mainBoard,task,pubData.getAloneID());
		if (combEditor.confirmSaveRepData()==false)
			return;

		if (commitflg == AllCommitStateEnum.TASK_COMMIT_VAL) {
			//������ٽ����Ƿ���ϱ����ж�
			
			//wangqi 20131231 ���ӱ������ѡ��
//			//���ݲ�������˲�ͨ��ʱ,�������ϱ�
//			if ((ICommitConfigConstant.GLOBLE_PARAM_VALUE.equals(task.getIscheckerrorcommit()) &&
//					!UfoeSysParamQueryUtil.getCanCommitOnNoPass(task)) ||
//					ICommitConfigConstant.DIRECT_CANCEL_NO.equals(task.getIscheckerrorcommit())) {
//
//				ICheckResultSrv bo = (ICheckResultSrv) NCLocator.getInstance().lookup(ICheckResultSrv.class.getName());
//				TaskCheckStateVO[] taskCheckStats =  bo.loadTaskCheckState(task.getPk_task(), new String[]{pubData.getAloneID()});
//
//				if(taskCheckStats != null && taskCheckStats.length > 0) {
//					if (taskCheckStats[0].getCheckState() == ICheckResultStatus.NOPASS ||
//							taskCheckStats[0].getCheckState() == ICheckResultStatus.NOCHECK) {
//						MessageDialog.showHintDlg(mainBoard,
//								null,
//								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1343")/*@res "�ϱ�ʧ�ܣ�ԭ���ǣ�������˲�ͨ������δ������˵ı���"*/);
//						if (combEditor.ismodified()) {
//							combEditor.getActiveRepDataEditor().getCellsModel().setDirty(true);
//						}
//						return;
//					}
//				}
//			}

			// �˱�һ����У��
			boolean accBookCheckPass = AccBookCheckUtil.validateAccBook(task, pubData, mainBoard);
			if(!accBookCheckPass) {
				if (combEditor.ismodified()) {
					combEditor.getActiveRepDataEditor().getCellsModel().setDirty(true);
				}
				return;
			}

			//���ݲ�����������ͨ��ʱ,�������ϱ�
			if ((ICommitConfigConstant.GLOBLE_PARAM_VALUE.equals(task.getIsapproveerrorcommit()) &&
					!UfoeSysParamQueryUtil.getCanCommitOnApproveNoPass(task)) ||
					ICommitConfigConstant.DIRECT_CANCEL_NO.equals(task.getIsapproveerrorcommit())) {
				IApproveQueryService iApproveQueryService=NCLocator.getInstance().lookup(IApproveQueryService.class);
				//����������������ͨ�������ϱ�
				if (task.getApprovemode() == null || task.getApprovemode() == TaskVO.TASKAPPROVEMODE) {
					TaskApproveVO approvevo = iApproveQueryService.getTaskApprove(task.getPk_task(), pubData.getAloneID(), TaskApproveVO.FLOWTYPE_COMMIT);
//					IRepDataEditor editor = getRepDataEditor();
//					if (!Integer.valueOf(editor.getMenuState().getApprovestatus()).equals(IPfRetCheckInfo.PASSING)) {
					if (approvevo == null || !approvevo.getVbillstatus().equals(IPfRetCheckInfo.PASSING)) {
						MessageDialog.showHintDlg(mainBoard,
							null,
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1406")/*@res "�ϱ�ʧ�ܣ�ԭ���ǣ�������δ������������������ͨ����"*/);
						if (combEditor.ismodified()) {
							combEditor.getActiveRepDataEditor().getCellsModel().setDirty(true);
						}
						return;
					}
				}else {
					// @edit by zhoushuang at 2014-1-3,����3:09:33 ������������ȫ������ ���б���ͨ�������ϱ�
					if (task.getCommstrategy().intValue()==ICommitConfigConstant.COMMIT_STRAGY_ALL) {
						if (!iApproveQueryService.isAllApproveInOneStatus(task.getPk_task(), pubData.getAloneID(), TaskApproveVO.FLOWTYPE_COMMIT,pubData.getUnitPK(),IPfRetCheckInfo.PASSING)) {
							MessageDialog.showHintDlg(mainBoard,
									null,
									nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820006-0037")/*@res "�ϱ�ʧ�ܣ�ԭ���ǣ����������б���δ������������������ͨ����"*/);						
							if (combEditor.ismodified()) {
								combEditor.getActiveRepDataEditor().getCellsModel().setDirty(true);
							}
						return;
						}
					}
				}
			}
		}


		//wangqi 20120420 �޸�����������TaskCommitMngActionִ��
		//����getCommitPanel,���ص���ģ��
		TaskCommitControler.getInstance(mainBoard).getCommitPanel();

		String[] aloneIds = new String[1];
		String[] unitPKs = new String[1];
		MeasurePubDataVO[] pubdatas = new MeasurePubDataVO[1];
		aloneIds[0] = pubData.getAloneID();
		unitPKs[0] = pubData.getUnitPK();
		pubdatas[0] = pubData;

		provider.setTask(task);
		provider.setAloneIds(aloneIds);
		provider.setPubDatas(pubdatas);
		provider.setUnitPKs(unitPKs);

//		provider.setTask(task);
//		provider.setAloneId(pubData.getAloneID());
//		provider.setPubData(pubData);
		Map<String,CommitActionParamVO[]> mapSelRepVo = provider.loadSelCommitRepVOs(TaskApproveVO.FLOWTYPE_COMMIT);
		CommitActionParamVO[] paramVos= mapSelRepVo.get(pubData.getAloneID());
		//��Ͷ���  --by:��־ǿ  �����±��±�
		if("�±�".equals(task.getName()) ){
			//�ϱ���
			if(provider.getClass().getSimpleName().equals("RepCommitSelRepProvider")){
				String[] needInputs = { "��21��","��12��","��03��","��001","��002","��003"};
				List<String> codes =  new  ArrayList<String>(Arrays.asList(needInputs));
				for(CommitActionParamVO vo:paramVos){
					
					if(codes.contains(vo.getSelRep().getRepcode())){
						vo.getSelRep().setNoneinputflag(new UFBoolean(false));
					}
				}
			}
		}
			
		
		
		CommitActionSelRepVO[] selReps  = provider.getSelRepVos(paramVos);
		for(CommitActionParamVO paramVo : paramVos){
			provider.adjustOneSelRepVO(paramVo.getSelRep(), paramVo.getInputResult(), paramVo.getTaskRepVo());
	    }
		//�õ�������ʾ
		String[] strAlertHints=provider.getAlertHintsFromSelRepVOs(selReps);
		boolean bNeedAlert=strAlertHints!=null && strAlertHints.length>0;
		if (bNeedAlert)
			provider.setHints(strAlertHints);
		else
			provider.setHints(null);

		//��ǰ�ϱ�����֯
//		IUFORepDataControler controler = (IUFORepDataControler) AbsRepDataControler
//		  .getInstance(mainBoard);
//        String strUnitPK = controler.getSelectedUnitPK();
        String rmspk =  (String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK);
        String userID =  (String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_USER_ID);

		//�����Ҫ��ʾ����ѡ���� ���򵯳��Ի���
		if (bNeedAlert || task.getCommstrategy().intValue()==ICommitConfigConstant.COMMIT_STRAGY_SELECT){
			//wangqi 20130102 ������˺ͱ��������ʾ������˽��
//			boolean bNeedCheckValidate=task.getCommitcheck().intValue()==ICommitConfigConstant.COMMIT_CHECK_ALL;
			boolean bNeedCheckValidate=task.getCommitcheck().intValue()==ICommitConfigConstant.COMMIT_CHECK_ALL ||
					task.getCommitcheck().intValue()==ICommitConfigConstant.COMMIT_CHECK_REP;
			
			CommitActionSelRepDlg selRepDlg=TaskCommitControler.getInstance(mainBoard).getCommitSelRepDlg(mainBoard,bNeedCheckValidate,task,pubdatas);
			selRepDlg.setProvider(provider);
			selRepDlg.setCommitSelReps(selReps);
			selRepDlg.setDisableOKButton(bNeedAlert);

//			Boolean flg = false;
//			for (CommitActionSelRepVO vo : selReps) {
//				if (vo.getSelected().booleanValue()) {
//					flg = true;
//					break;
//				}
//			}
//			if (bNeedAlert) {
//				selRepDlg.setDisableOKButton(bNeedAlert);
//			} else {
//				selRepDlg.setDisableOKButton(flg);
//			}

			selRepDlg.setRmspk(rmspk);
			selRepDlg.setUserid(userID);
			selRepDlg.setCommitflg(commitflg);
			selRepDlg.doInitUI();
			if (selRepDlg.showModal()==UIDialog.ID_OK) {
				//wangqi 20130605 ��ע������ȡ����
				if (selRepDlg.getAnnotationscancelflg().booleanValue()) {
					handler.refreshEditorMenuState(combEditor);
					UfoPublic.sendMessage(handler.getSucceedMessage(),mainBoard);
//					provider.sendCommitMsg(strUnitPK,null,rmspk,userID,task,pubData,commitflg);
				}
			}
			return;
		}

		//����provider�ķ���
		List<CommitActionSelRepVO[]> selRepslist = new ArrayList<CommitActionSelRepVO[]>();
		selRepslist.add(selReps);

		TaskAnnotationsDlg dlg = new TaskAnnotationsDlg(combEditor.getTopLevelAncestor(),new MeasurePubDataVO[]{combEditor.getPubData()},task);

		int annotationflg = commitflg;
		if(commitflg == AllCommitStateEnum.REQUEST_BACK_AFFIRMED_VAL ||
				commitflg == AllCommitStateEnum.REQUEST_BACK_COMMITED_VAL){
			annotationflg = AllCommitStateEnum.REQUEST_BACK_VAL;
		}
		dlg.setBusi_type(annotationflg);
		dlg.setAnnotationText();
		if(dlg.showModal() == UIDialog.ID_OK){
			TaskAnnotationVO[] taskAnnotations = dlg.getTaskAnnotationsVO();
//			if(commitflg == "commit".equals(commitflg)){
//				taskAnnotations.setBusi_type(AnnotationsStateEnum.TASKCOMMIT_VAL);
//			}else{
//				taskAnnotations.setBusi_type(AnnotationsStateEnum.REQUESTBACK_VAL);
//			}
			for (TaskAnnotationVO taskAnnotationVO : taskAnnotations) {

				taskAnnotationVO.setBusi_type(annotationflg);
			}

			provider.doOKAction(selRepslist,null,rmspk,
					userID,commitflg,taskAnnotations,null);
			handler.refreshEditorMenuState(getCombRepDataEditor(mainBoard,task,pubData.getAloneID()));
			UfoPublic.sendMessage(handler.getSucceedMessage(),mainBoard);
		}
	}

	public static AbsCombRepDataEditor getCombRepDataEditor(Mainboard mainBoard,TaskVO task,String strAloneId){
		Viewer[] views=mainBoard.getAllViews();
		for (Viewer view:views){
			if (view instanceof AbsCombRepDataEditor==false)
				continue;

			AbsCombRepDataEditor combEditor=(AbsCombRepDataEditor)view;
			if (nc.ui.iufo.pub.UfoPublic.strIsEqual(task.getPk_task(), combEditor.getTaskPK())
					&& nc.ui.iufo.pub.UfoPublic.strIsEqual(strAloneId, combEditor.getAloneID())){
				return combEditor;
			}
		}
		return null;
	}
}
