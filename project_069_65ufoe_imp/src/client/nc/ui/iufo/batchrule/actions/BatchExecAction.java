package nc.ui.iufo.batchrule.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.funcnode.ui.FuncletInitData;
import nc.funcnode.ui.FuncletWindowLauncher;
import nc.itf.iufo.constants.IufoeFuncCodeConstants;
import nc.pub.bi.clusterscheduler.SchedulerUtilities;
import nc.ui.iufo.constants.IUfoeActionCode;
import nc.ui.sm.power.FuncRegisterCacheAccessor;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.ToftPanelAdaptor;
import nc.ui.uif2.UIState;
import nc.ui.uif2.model.AbstractAppModel;
import nc.ui.uif2.model.BillManageModel;
import nc.uif2.annoations.MethodType;
import nc.uif2.annoations.ModelMethod;
import nc.uif2.annoations.ModelType;
import nc.util.iufo.batchrule.BatchCalcJob;
import nc.util.iufo.batchrule.BatchCalcModule;
import nc.util.iufo.funcpermission.FuncPermissionCheckUtil;
import nc.vo.iufo.batchrule.BatchRuleVO;
import nc.vo.pub.BusinessException;
import nc.vo.sm.funcreg.FuncRegisterVO;

import com.ufida.iufo.pub.tools.AppDebug;

/**
 * 批量规则执行
 *
 * @author liuchuna
 * @created at 2011-6-28,下午03:14:58
 *
 */
public class BatchExecAction extends NCAction {
	private static final long serialVersionUID = -979198940806600082L;

	protected AbstractAppModel model = null;

	protected BatchMonitorAction monitorAction=null;

	private FuncRegisterVO funRegVo = null;

	// 关联功能节点
	public static final String REL_FUNC_CODE = "18200RCM";

	public BatchExecAction(){
		super();
		setCode(IUfoeActionCode.BATCHEXEC);
		setBtnName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0086")/*@res "执行"*/);
	}

	@Override
	public void doAction(ActionEvent e) throws Exception {
		Object[] batchRules=(Object[])((BillManageModel)getModel()).getSelectedOperaDatas();
		StringBuffer buf=new StringBuffer();
		for (int i=0;i<batchRules.length;i++){
			buf.append(((BatchRuleVO)batchRules[i]).getPk_batchrule());
			if(i<batchRules.length-1)
				buf.append(",");
		}
		String language = WorkbenchEnvironment.getCurrLanguage().getCode();
		BatchCalcJob job=new BatchCalcJob(buf.toString(),getModel().getContext().getPk_loginUser(), language);
		String strGUId=null;
		//shilt 修改执行按钮提示
		try{
			strGUId=SchedulerUtilities.addJob(job);
		}catch(Exception ex){
			AppDebug.debug(ex);
			ShowStatusBarMsgUtil.showErrorMsg(
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820012-0010")/*@res "执行失败！"*/,nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820012-0011")/*@res "该作业标记为只能运行一个实例，调度队列中已经存在该实例。"*/, getModel().getContext());
		}



		int option = JOptionPane.showConfirmDialog(getModel().getContext().getEntranceUI(),
				nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820005-0002")/*@res "批量计算作业已经加入执行队列，是否打开监控？"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0323")/*@res "确认"*/, JOptionPane.YES_NO_OPTION);
		if(option == JOptionPane.YES_OPTION) {
			doOpenMonitor(strGUId);
		}

	}

	private void doOpenMonitor(String strGUId) throws Exception {
		if(FuncPermissionCheckUtil.checkFuncPermission(getModel().getContext().getPk_loginUser(),
				getModel().getContext().getPk_group(), IufoeFuncCodeConstants.FUNC_BATCHMONITOR)){
			// 打开监控界面
			ToftPanelAdaptor adaptor = (ToftPanelAdaptor) getModel().getContext().getEntranceUI();
			FuncletInitData data = new FuncletInitData();
			data.setInitData(new String[]{strGUId, BatchCalcModule.class.getName()});
			FuncletWindowLauncher.openFuncNodeInTabbedPane(adaptor, getFuncRegVO(), data, null, false);
		}
	}

	private FuncRegisterVO getFuncRegVO() throws BusinessException {
		if (funRegVo == null) {
			funRegVo = FuncRegisterCacheAccessor.getInstance().getFuncRegisterVOByFunCode(REL_FUNC_CODE);
		}
		return funRegVo;
	}

	@Override
	protected boolean isActionEnable() {
		return model.getUiState()==UIState.NOT_EDIT&&model.getSelectedData()!=null;
	}

	@ModelMethod(modelType=ModelType.AbstractAppModel,methodType=MethodType.GETTER)
	public AbstractAppModel getModel() {
		return model;
	}

	@ModelMethod(modelType=ModelType.AbstractAppModel,methodType=MethodType.SETTER)
	public void setModel(AbstractAppModel model) {
		this.model = model;
		model.addAppEventListener(this);
	}

	public BatchMonitorAction getMonitorAction() {
		return monitorAction;
	}

	public void setMonitorAction(BatchMonitorAction monitorAction) {
		this.monitorAction = monitorAction;
	}

}