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
 * 报表数据界面菜单与报送管理菜单共用的报送方法
 * @author weixl
 *
 */
public class CommitActionUtil {
	/**
	 * 上报主方法
	 * @param mainBoard:
	 * @param task：对应任务
	 * @param pubData:对应关键字数据
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
				//数据中心状态栏用
				mainBoard.getContext().setAttribute(IUfoContextKey.TASK_COMMIT_STATE, CommitStateEnum.STATE_COMMITED.getIntValue());
				return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0021")/*@res "任务上报成功"*/;
			}
		});
	}

	/**
	 * 请求退回主方法
	 * @param mainBoard:
	 * @param task：对应任务
	 * @param pubData:对应关键字数据
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
				return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0022")/*@res "任务请求退回成功"*/;
			}
		});
	}

	private static void innerDoCommitAction(final Mainboard mainBoard,TaskVO task,MeasurePubDataVO pubData,AbsCommitActionSelRepProvider provider,int commitflg, ICommitHandler handler) throws Exception{
		AbsCombRepDataEditor combEditor=getCombRepDataEditor(mainBoard,task,pubData.getAloneID());
		if (combEditor.confirmSaveRepData()==false)
			return;

		if (commitflg == AllCommitStateEnum.TASK_COMMIT_VAL) {
			//保存后再进行是否可上报的判断
			
			//wangqi 20131231 增加报表表样选项
//			//根据参数，审核不通过时,不可以上报
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
//								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1343")/*@res "上报失败！原因是：存在审核不通过或者未进行审核的报表。"*/);
//						if (combEditor.ismodified()) {
//							combEditor.getActiveRepDataEditor().getCellsModel().setDirty(true);
//						}
//						return;
//					}
//				}
//			}

			// 账表一致性校验
			boolean accBookCheckPass = AccBookCheckUtil.validateAccBook(task, pubData, mainBoard);
			if(!accBookCheckPass) {
				if (combEditor.ismodified()) {
					combEditor.getActiveRepDataEditor().getCellsModel().setDirty(true);
				}
				return;
			}

			//根据参数，审批不通过时,不可以上报
			if ((ICommitConfigConstant.GLOBLE_PARAM_VALUE.equals(task.getIsapproveerrorcommit()) &&
					!UfoeSysParamQueryUtil.getCanCommitOnApproveNoPass(task)) ||
					ICommitConfigConstant.DIRECT_CANCEL_NO.equals(task.getIsapproveerrorcommit())) {
				IApproveQueryService iApproveQueryService=NCLocator.getInstance().lookup(IApproveQueryService.class);
				//按任务审批，任务通过，可上报
				if (task.getApprovemode() == null || task.getApprovemode() == TaskVO.TASKAPPROVEMODE) {
					TaskApproveVO approvevo = iApproveQueryService.getTaskApprove(task.getPk_task(), pubData.getAloneID(), TaskApproveVO.FLOWTYPE_COMMIT);
//					IRepDataEditor editor = getRepDataEditor();
//					if (!Integer.valueOf(editor.getMenuState().getApprovestatus()).equals(IPfRetCheckInfo.PASSING)) {
					if (approvevo == null || !approvevo.getVbillstatus().equals(IPfRetCheckInfo.PASSING)) {
						MessageDialog.showHintDlg(mainBoard,
							null,
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1406")/*@res "上报失败！原因是：该任务未进行审批或者审批不通过。"*/);
						if (combEditor.ismodified()) {
							combEditor.getActiveRepDataEditor().getCellsModel().setDirty(true);
						}
						return;
					}
				}else {
					// @edit by zhoushuang at 2014-1-3,下午3:09:33 按报表审批，全部策略 所有报表通过，可上报
					if (task.getCommstrategy().intValue()==ICommitConfigConstant.COMMIT_STRAGY_ALL) {
						if (!iApproveQueryService.isAllApproveInOneStatus(task.getPk_task(), pubData.getAloneID(), TaskApproveVO.FLOWTYPE_COMMIT,pubData.getUnitPK(),IPfRetCheckInfo.PASSING)) {
							MessageDialog.showHintDlg(mainBoard,
									null,
									nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820006-0037")/*@res "上报失败！原因是：该任务下有报表未进行审批或者审批不通过。"*/);						
							if (combEditor.ismodified()) {
								combEditor.getActiveRepDataEditor().getCellsModel().setDirty(true);
							}
						return;
						}
					}
				}
			}
		}


		//wangqi 20120420 修改连接数放在TaskCommitMngAction执行
		//调用getCommitPanel,加载单据模板
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
		//央客二开  --by:王志强  国旅月报月报
		if("月报".equals(task.getName()) ){
			//上报表，
			if(provider.getClass().getSimpleName().equals("RepCommitSelRepProvider")){
				String[] needInputs = { "内21表","内12表","内03表","内001","内002","内003"};
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
		//得到出错提示
		String[] strAlertHints=provider.getAlertHintsFromSelRepVOs(selReps);
		boolean bNeedAlert=strAlertHints!=null && strAlertHints.length>0;
		if (bNeedAlert)
			provider.setHints(strAlertHints);
		else
			provider.setHints(null);

		//当前上报的组织
//		IUFORepDataControler controler = (IUFORepDataControler) AbsRepDataControler
//		  .getInstance(mainBoard);
//        String strUnitPK = controler.getSelectedUnitPK();
        String rmspk =  (String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK);
        String userID =  (String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_USER_ID);

		//如果需要提示或手选报表 ，则弹出对话框
		if (bNeedAlert || task.getCommstrategy().intValue()==ICommitConfigConstant.COMMIT_STRAGY_SELECT){
			//wangqi 20130102 所有审核和报表审核显示报表审核结果
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
				//wangqi 20130605 批注界面点击取消后
				if (selRepDlg.getAnnotationscancelflg().booleanValue()) {
					handler.refreshEditorMenuState(combEditor);
					UfoPublic.sendMessage(handler.getSucceedMessage(),mainBoard);
//					provider.sendCommitMsg(strUnitPK,null,rmspk,userID,task,pubData,commitflg);
				}
			}
			return;
		}

		//调用provider的方法
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
