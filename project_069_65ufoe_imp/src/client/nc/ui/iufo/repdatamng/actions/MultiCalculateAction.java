/**
 *
 */
package nc.ui.iufo.repdatamng.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import com.ufsoft.iufo.inputplugin.ufobiz.ufoe.multicalc.MultiRepCalcDlg;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.funcnode.ui.FuncletInitData;
import nc.funcnode.ui.FuncletWindowLauncher;
import nc.pub.bi.clusterscheduler.SchedulerUtilities;
import nc.ui.pub.beans.UIDialog;
import nc.ui.sm.power.FuncRegisterCacheAccessor;

import nc.ui.uif2.ToftPanelAdaptor;
import nc.ui.uif2.model.BillManageModel;

import nc.ui.iufo.NodeEnv;
import nc.ui.iufo.batchrule.actions.BatchExecAction;
import nc.ui.iufo.constants.IUfoeActionCode;
import nc.ui.iufo.input.funclet.AbsSwitchToftPanelAdaptor;
import nc.ui.iufo.repdataauth.actions.RepDataAuthEditBaseAction;

import nc.util.iufo.multicalc.MultiRepCalcJob;
import nc.util.iufo.multicalc.MultiRepCalcModule;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.pub.BusinessException;
import nc.vo.sm.funcreg.FuncRegisterVO;
import nc.vo.uif2.LoginContext;

/**
 * 报表数据查询中多表计算执行
 *
 * @author wuyongc
 * @created at 2011-6-9,下午07:02:34
 *
 */
public class MultiCalculateAction extends RepDataAuthEditBaseAction {

	private static final long serialVersionUID = -7990602489510478213L;

	@SuppressWarnings("unused")
	private LoginContext loginContext=null;

	private NodeEnv nodeEnv=null;

	public MultiCalculateAction(){
		super();
		setBtnName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0115")/*@res "多表计算"*/);
		setCode(IUfoeActionCode.CALCMULTIREP);
//		putValue(Action.SHORT_DESCRIPTION, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0115")/*@res "多表计算"*/);
	}

	@Override
	public void doAction(ActionEvent e) throws Exception {
		super.doAction(e);
		// liuchun+ @2011-06-28, 多表计算实现
		Object[] objs = ((BillManageModel)getModel()).getSelectedOperaDatas();
		if(objs == null || objs.length ==0) {
			return;
		}

		if (getModel().getContext().getEntranceUI() instanceof AbsSwitchToftPanelAdaptor) {
			NodeEnv nodeEnv=((AbsSwitchToftPanelAdaptor)getModel().getContext().getEntranceUI()).getNodeEnv();
			RepDataQueryResultVO result = (RepDataQueryResultVO)objs[0];

			// 选择报表对话框
			MultiRepCalcDlg dlg = new MultiRepCalcDlg(getModel().getContext().getEntranceUI());
			// 取得当前任务、报表组织、rmspk
//			String taskPK = nodeEnv.getCurrTaskPK();
//			String curOrgPK = nodeEnv.getCurrOrg();
			String curRmsPK = nodeEnv.getCurrMngStuc();
			// 取得选中的报表数据、及报表数据的关键字值
			MeasurePubDataVO pubData = result.getPubData();
			String[] keyValues = pubData.getKeywords();
//			TaskVO taskVo = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(taskPK);
			// 设置dlg属性，并进行初始化
			dlg.setTaskVo(task);
			dlg.setStrOrgPK(getOrgPK());
			dlg.setRmsPK(curRmsPK);
			dlg.setKeyValues(keyValues);
			dlg.setMainOrgPk(getMainOrgPK());

			dlg.initUI();
			dlg.showModal();

			if(dlg.getResult() == UIDialog.ID_OK) {
				// 取得选择的任务、录入的关键字值、选择需要计算的报表
				String selTask = dlg.getSelTask();
				String[] inputKeys = dlg.getInputKeyValues();
				String[] selReps  = dlg.getSelReps();
				String userId = WorkbenchEnvironment.getInstance().getLoginUser().getPrimaryKey();
				// 生成一个作业并加入的队列中
				MultiRepCalcJob job=new MultiRepCalcJob(selTask, inputKeys, selReps, userId, curRmsPK);
				String loginDate = WorkbenchEnvironment.getInstance().getBusiDate().toLocalString();
				String language = WorkbenchEnvironment.getCurrLanguage().getCode();
				String pkGroup = getModel().getContext().getPk_group();
				job.setLoginDate(loginDate);
				job.setLanguage(language);
				job.setPkGroup(pkGroup);
				String jobId = SchedulerUtilities.addJob(job);

				int option = JOptionPane.showConfirmDialog(getModel().getContext().getEntranceUI(),
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0830")/*@res "多表计算作业已经加入执行队列，是否打开监控？"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0323")/*@res "确认"*/, JOptionPane.YES_NO_OPTION);
				if(option == JOptionPane.YES_OPTION) {
					doOpenMonitor(jobId);
				}
			}
		}
	}

	private void doOpenMonitor(String strGUId) throws BusinessException {
		// 打开监控界面
		ToftPanelAdaptor adaptor = (ToftPanelAdaptor) getModel().getContext().getEntranceUI();
		FuncletInitData data = new FuncletInitData();
		data.setInitData(new String[]{strGUId, MultiRepCalcModule.class.getName()});
		FuncletWindowLauncher.openFuncNodeInTabbedPane(adaptor, getFuncRegVO(), data, null, false);
	}

	private FuncRegisterVO getFuncRegVO() throws BusinessException {
		return FuncRegisterCacheAccessor.getInstance().getFuncRegisterVOByFunCode(BatchExecAction.REL_FUNC_CODE);
	}

	public void setLoginContext(LoginContext loginContext) {
		if (loginContext.getEntranceUI() instanceof AbsSwitchToftPanelAdaptor)
			nodeEnv=((AbsSwitchToftPanelAdaptor)loginContext.getEntranceUI()).getNodeEnv();
		this.loginContext = loginContext;
	}

	@Override
	protected boolean isActionEnable(){
		return getModel().getSelectedData() != null;
	}

	@Override
	protected String getRepPK() {
		return ((RepDataQueryResultVO)getModel().getSelectedData()).getPk_report();
	}

	@Override
	protected String getOrgPK() {
		return ((RepDataQueryResultVO)getModel().getSelectedData()).getPk_org();
	}

	@Override
	protected String getRmsPK() {
		return nodeEnv.getCurrMngStuc();
	}

	@Override
	protected String getMainOrgPK() {
		return nodeEnv.getCurrOrg();
	}

	@Override
	protected String getTaskPK() {
		return ((RepDataQueryResultVO)getModel().getSelectedData()).getPk_task();
	}

}