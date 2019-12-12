package nc.ui.hbbb.convertcenter.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingWorker;

import nc.bs.hbbb.convert.batch.ConvertExecJob;
import nc.bs.logging.Logger;
import nc.pub.bi.clusterscheduler.SchedulerUtilities;
import nc.ui.hbbb.convertcenter.view.ConvertDlg;
import nc.ui.hbbb.convertcenter.view.ConvertDlgAdapter;
import nc.ui.hbbb.qrypanel.schemekey.SchemeKeyEditor;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.progress.DefaultProgressMonitor;
import nc.ui.pub.beans.progress.IProgressMonitor;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.components.progress.TPAProgressUtil;
import nc.ui.uif2.model.BillManageModel;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.pub.HBPubItfService;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.schemekey.SchemeKeyQryVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.ufoc.conver.ConvertRuleVO;
import nc.vo.ufoc.convert.CvtruleorgVO;
import nc.vo.uif2.LoginContext;

public class ConvertRuleExcuteAction extends NCAction {
	private static final long serialVersionUID = 1L;

	private BillManageModel model;

	private ConvertDlgAdapter dlgAdapter;

	private LoginContext context;

	public ConvertRuleExcuteAction() {
		this.setCode(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0182")/* @res "����ִ��" */);
		this.setBtnName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0182")/* @res "����ִ��" */);
	}

	@Override
	public void doAction(ActionEvent e) throws Exception {
		final ConvertDlg dlg = getDlgAdapter().getDlg(getContext().getEntranceUI());
		Object[] selectedOperaDatas = getModel().getSelectedOperaDatas();
		if (null == selectedOperaDatas || selectedOperaDatas.length == 0) {
			return;
		}
		// �ж����������ͳһ�ϲ�����
		Set<String> hbschemeSet = new HashSet<String>();
		final ArrayList<ConvertRuleVO> list = new ArrayList<ConvertRuleVO>();
		for (int i = 0; i < selectedOperaDatas.length; i++) {
			ConvertRuleVO rulevo = (ConvertRuleVO) selectedOperaDatas[i];
			list.add(rulevo);
			hbschemeSet.add(rulevo.getPk_hbscheme());
		}
		if (hbschemeSet.size() > 1) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830003-0003")/*@res "�����������ͬһ�ϲ�����!"*/);
		}
		HBSchemeVO schemevo = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(hbschemeSet.toArray(new String[0])[0]);
//		HBSchemeVO schemevo = HBSchemeSrvUtils.getHBSchemeByHBconvertruleid(hbschemeSet.toArray(new String[0])[0]);
		((SchemeKeyEditor) dlg.getSchemekeyeditor()).setSchemePK(schemevo.getPk_hbscheme());
		dlg.setSchemevo(schemevo);

		if (dlg.showModal() == UIDialog.ID_OK) {
			// �糵�ȴ�����
			TPAProgressUtil tpaProgressUtil = new TPAProgressUtil();
			tpaProgressUtil.setContext(getModel().getContext());
			final DefaultProgressMonitor mon = tpaProgressUtil.getTPAProgressMonitor();
			mon.beginTask(ConvertRuleExcuteAction.this.getBtnName() + "...",
					IProgressMonitor.UNKNOWN_TOTAL_TASK);
			mon.setProcessInfo(ConvertRuleExcuteAction.this.getBtnName() + "...");
			SwingWorker<String[], Object> sw = new SwingWorker<String[], Object>() {
				@Override
				protected String[] doInBackground() throws Exception {
					try {
						SchemeKeyQryVO qryvo = (SchemeKeyQryVO) ((SchemeKeyEditor) dlg.getSchemekeyeditor()).getValue();
						//����ִ�нڵ�-����ȡ�����Ի����ĵı���
						qryvo.getKeymap().remove(KeyVO.COIN_PK);
						//������֯����20���������ִ��
						ConvertRuleVO rule = list.get(0);
						CvtruleorgVO[] orgs = rule.getOrgs();
						if(orgs.length < 20){
							HBPubItfService.getRemoteConvert().doConvert(qryvo.getKeymap(), list.toArray(new ConvertRuleVO[0]));
							ShowStatusBarMsgUtil.showStatusBarMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0183")/* @res "�������" */, getContext());
						}else{
							ConvertExecJob job = new ConvertExecJob(rule, qryvo.getKeymap());
							SchedulerUtilities.addJob(job);
							ConvertRuleExcuteAction.this.showSuccessInfo();
						}
					} catch (Exception e) {
						Logger.error(e.getMessage(), e);
						ShowStatusBarMsgUtil.showErrorMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0071")/* @res "����" */
								, ""+e.getMessage(), getModel().getContext());

					}
					return null;
				}

				@Override
				protected void done() {
					// �����������
					mon.done();
				}
			};
			sw.execute();
		}
        
	}

	protected void showSuccessInfo() {
		ShowStatusBarMsgUtil.showStatusBarMsg(NCLangRes4VoTransl.getNCLangRes()
				.getStrByID("pub_0", "01830008-0130") /* @res "���������Ѿ��ύ��ִ̨�У��뵽����ִ�ж��в鿴ִ�н��" */, this.model.getContext());
	}

	@Override
	protected boolean isActionEnable() {
		return this.model.getSelectedData() != null;
	}

	public BillManageModel getModel() {
		return this.model;
	}

	public void setModel(BillManageModel model) {
		this.model = model;
		this.model.addAppEventListener(this);
	}

	public ConvertDlgAdapter getDlgAdapter() {
		return this.dlgAdapter;
	}

	public void setDlgAdapter(ConvertDlgAdapter dlgAdapter) {
		this.dlgAdapter = dlgAdapter;
	}

	public LoginContext getContext() {
		return this.context;
	}

	public void setContext(LoginContext context) {
		this.context = context;
	}

}