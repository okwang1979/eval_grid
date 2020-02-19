package nc.ui.erm.action;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import nc.itf.uap.pf.metadata.IFlowBizItf;
import nc.md.data.access.NCObject;
import nc.ui.ambd.constant.AppEventConst;
import nc.ui.pub.beans.UIDialog;
import nc.ui.uif2.AppEvent;
import nc.ui.uif2.ExceptionHandlerUtil;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.UIState;
import nc.ui.uif2.editor.BillForm;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;

/**
 * 单据录入节点、审批节点的影像查看按钮
 */
@SuppressWarnings("serial")
public class ReceiptCheckAction extends NCAction {
	private BillForm editor;

	private BillManageModel model;

	public ReceiptCheckAction() {
		super();
		setCode("ReceiptCheck");
		setBtnName("影像查看");
	}

	UIDialog dlg = null;

	private UIDialog getDlg() {
		if (dlg == null) {
			dlg = new UIDialog(getModel().getContext().getEntranceUI(), "影像查看");
			dlg.getContentPane().setLayout(new BorderLayout());
			dlg.setResizable(true);
			// dlg.setSize(600, 400);
			// 获取屏幕的大小，使控件能够自适应屏幕
			Toolkit kit = Toolkit.getDefaultToolkit();
			Dimension screenSize = kit.getScreenSize();
			int screenWidth = (int) screenSize.getWidth();
			int screenHeight = (int) screenSize.getHeight();
			dlg.setSize(screenWidth - 50, screenHeight - 70);
			int dlgWidth = dlg.getWidth();
			dlg.setLocation((screenWidth - dlgWidth) / 2, 30);
		}
		return dlg;
	}

	public void handleEvent(AppEvent event) {
		super.handleEvent(event);
		if (event.getType() == AppEventConst.MULTI_SELECTION_CHANGED) {
			// 在浏览态切换单据
			if (getModel().getUiState() == UIState.NOT_EDIT && getModel().getSelectedData() != null && getDlg() != null && getDlg().isShowing()) {
				try {
					showDocument();
				} catch (BusinessException e) {
					new ExceptionHandlerUtil().processErrorMsg4SpecialAction(this, getExceptionHandler(), e);
				}
			}
		} else if (event.getType() == AppEventConst.UISTATE_CHANGED && getDlg() != null && getDlg().isShowing()) {
			if (getModel().getUiState() == UIState.ADD)
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						getDlg().dispose();
					}

				});
			else
				getDlg().dispose();
		}
	}

	public void showDocument() throws BusinessException {
		String rootDirStr = null;
		String billNo = null;
		AggregatedValueObject selectVo = (AggregatedValueObject) getModel().getSelectedData();// getSelectedOneAggVO();
		if (selectVo == null || selectVo.getParentVO().getPrimaryKey() == null) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("2006030102", "UPP2006030102-000168")/*
																														 * @
																														 * res
																														 * "请选择要进行文档管理的单据"
																														 */);
		} else {
			// 获得主键
			rootDirStr = selectVo.getParentVO().getPrimaryKey();
			billNo = getBillNo(selectVo);
		}
		showModal(rootDirStr, billNo);
	}

	private String getBillNo(AggregatedValueObject selectVo) {
		if (selectVo == null) {
			return null;
		}

		NCObject ncObject = NCObject.newInstance(selectVo);
		IFlowBizItf itf = ncObject.getBizInterface(IFlowBizItf.class);

		return itf.getBillNo();
	}

	private void showModal(String rootDirStr, String billNo) throws BusinessException {
		UIDialog dlg = getDlg();
		// 清除原有的东西
		Component[] comps = dlg.getContentPane().getComponents();
		if (comps != null && comps.length > 0)
			for (int i = 0; i < comps.length; i++)
				dlg.getContentPane().remove(i);
		// 标志单据为何种状态
		boolean isEdit = false;
		// 标志附件为何种状态
		String scanState = ScanPanel.Browse;
		ScanPanel scanPanel = new ScanPanel(rootDirStr, isEdit, scanState, billNo);
		scanPanel.tryPanel();
		dlg.getContentPane().add(scanPanel, BorderLayout.CENTER);
		if (getModel().getContext().getInitData() != null) {
			// 从消息中心打开的单据管理，应该设置为模态
			dlg.showModal();
		} else {
			dlg.setModal(false);
			dlg.show();
			dlg.toFront();
		}
	}

	@Override
	protected boolean isActionEnable() {
		UIState state = getModel().getUiState();
		// 新增单据时按钮为灰
		if (UIState.ADD == state) {
			return false;
		}
		if (getModel().getSelectedData() != null)
			return true;
		return false;
	}

	public void doAction(ActionEvent e) throws BusinessException {
		showDocument();
	}

	public void setModel(BillManageModel model) {
		this.model = model;
		model.addAppEventListener(this);
	}

	public BillManageModel getModel() {
		return model;
	}

	public BillForm getEditor() {
		return editor;
	}

	public void setEditor(BillForm editor) {
		this.editor = editor;
	}
}