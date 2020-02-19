package nc.ui.cmp.settlement.actions;



import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import nc.itf.uap.pf.metadata.IFlowBizItf;
import nc.md.data.access.NCObject;
import nc.ui.ambd.constant.AppEventConst;
import nc.ui.cmp.settlement.view.SettlementCard;
import nc.ui.pub.beans.UIDialog;
import nc.ui.uif2.AppEvent;
import nc.ui.uif2.ExceptionHandlerUtil;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.UIState;
import nc.ui.uif2.actions.ActionInitializer;
import nc.ui.uif2.editor.BillForm;
import nc.ui.uif2.model.AbstractAppModel;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.cmp.settlement.SettlementHeadVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.ui.cmp.settlement.view.SettlementList;


/**
 * 单据录入节点、审批节点的影像查看按钮
 */
@SuppressWarnings("serial")
public class ReviwePictureAction extends NCAction {
	
	private SettlementList listView;
	private BillForm editor;

	private AbstractAppModel model;
	
	private SettlementCard edit;

	public ReviwePictureAction() {
		super();
		setCode("ReceiptCheck");
		setBtnName("影像查看");
//		ActionInitializer.initializeAction(this, "ReceiptCheck");
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
//			select billcode from cmp_settlement where   pk_busibill ='0001A2100000006W1FH5' 
			if( selectVo.getParentVO() instanceof SettlementHeadVO){
				SettlementHeadVO selectHead = (SettlementHeadVO)selectVo.getParentVO();
				rootDirStr = selectHead.getPk_busibill();
						billNo = selectHead.getBillcode();
			}else{
				return ;
			}
		
		}
		if(rootDirStr!=null&&billNo!=null){
			showModal(rootDirStr, billNo);
		}
		
	}

	private String getBillNo(AggregatedValueObject selectVo) {
		if (selectVo == null) {
			return null;
		}
//		NCObject.n
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

	public void setModel(AbstractAppModel model) {
		this.model = model;
		model.addAppEventListener(this);
	}

	public AbstractAppModel getModel() {
		return model;
	}
	
//	public AbstractAppModel getModel() {
//		return this.model;
//	}

	public BillForm getEditor() {
		return editor;
	}

	
	
	//***************************************************
	public void setEditor(BillForm editor) {
		this.editor = editor;
	}
	public void setEdit(SettlementCard edit) {
		this.edit = edit;
	}

	public SettlementCard getEdit() {
		return this.edit;
	}
	
	
	
	public void setListView(SettlementList listView) {
		this.listView = listView;
	}


}
//
//import java.awt.event.ActionEvent;
//
//import nc.cmp.utils.CmpUtils;
//import nc.ui.cmp.settlement.view.SettlementCard;
//import nc.ui.cmp.settlement.view.SettlementList;
//import nc.ui.uif2.UIState;
//import nc.ui.uif2.actions.ActionInitializer;
//import nc.ui.uif2.actions.EditAction;
//import nc.ui.uif2.model.AbstractAppModel;
//import nc.vo.cmp.BusiStatus;
//import nc.vo.cmp.SettleStatus;
//import nc.vo.cmp.bill.BillAggVO;
//import nc.vo.cmp.settlement.SettlementAggVO;
//import nc.vo.cmp.settlement.SettlementHeadVO;
//import nc.vo.ml.NCLangRes4VoTransl;
//import nc.vo.pub.BusinessException;
//
//public class ReviwePictureAction extends EditAction {
//	private static final long serialVersionUID = -5210224731564447169L;
//	private AbstractAppModel model;
//	private SettlementCard edit;
//	private SettlementList listView;
//
//	public ReviwePictureAction() {
//		ActionInitializer.initializeAction(this, "Reviwe");
//	}
//
//	public void doAction(ActionEvent e) throws Exception {
//		if (getValue() == null) {
//			throw new BusinessException(NCLangRes4VoTransl
//					.getNCLangRes().getStrByID("3607set_0", "03607set-0044"));
//		}
////
////		if (!getEdit().isShowing()) {
////			getEdit().showMeUp();
////		}
////		getModel().setUiState(UIState.EDIT);
//	}
//
//	protected boolean isActionEnable() {
//		if ((UIState.ADD == getModel().getUiState())
//				|| (UIState.EDIT == getModel().getUiState())) {
//			return false;
//		}
//		SettlementAggVO[] selectedAggVOs = getSelectedAggVOs();
//
//		if ((CmpUtils.isListNull(selectedAggVOs))
//				|| (selectedAggVOs.length > 1)) {
//			return false;
//		}
//
//		SettlementHeadVO headvo = (SettlementHeadVO) selectedAggVOs[0]
//				.getParentVO();
//		Integer busistatus = Integer
//				.valueOf(headvo.getBusistatus() == null ? BusiStatus.Save
//						.getBillStatusKind() : headvo.getBusistatus()
//						.intValue());
//
//		Integer settleStatus = headvo.getSettlestatus();
//		settleStatus = Integer
//				.valueOf(settleStatus == null ? SettleStatus.NONESETTLE
//						.getStatus() : settleStatus.intValue());
//
//		if ((settleStatus.intValue() == SettleStatus.NONESETTLE
//				.getStatus()) && (headvo.getPk_signer() == null)) {
//			return true;
//		}
//		return false;
//	}
//
//	public SettlementAggVO[] getSelectedAggVOs() {
//		Object[] value = null;
//		if (isListSelected()) {
//			value = getListView().getModel().getSelectedOperaDatas();
//		} else {
//			value = new Object[1];
//			value[0] = getModel().getSelectedData();
//		}
//
//		if ((null == value) || (value.length == 0)) {
//			return null;
//		}
//		SettlementAggVO[] aggs = new SettlementAggVO[value.length];
//		System.arraycopy(value, 0, aggs, 0, aggs.length);
//		if (!CmpUtils.isListNull(aggs)) {
//			return aggs;
//		}
//		return null;
//	}
//
//	public void setEdit(SettlementCard edit) {
//		this.edit = edit;
//	}
//
//	public SettlementCard getEdit() {
//		return this.edit;
//	}
//
//	public SettlementList getListView() {
//		return this.listView;
//	}
//
//	public void setListView(SettlementList listView) {
//		this.listView = listView;
//	}
//
//	public AbstractAppModel getModel() {
//		return this.model;
//	}
//
//	public void setModel(AbstractAppModel model) {
//		this.model = model;
//		model.addAppEventListener(this);
//	}
//
//	public boolean isListSelected() {
//		if ((this.listView != null) && (this.listView.isShowing())) {
//			return true;
//		}
//		return false;
//	}
//
//	public Object getValue() {
//		if (isListSelected()) {
//			SettlementAggVO[] aggvos = null;
//			Object[] object = this.listView.getModel().getSelectedOperaDatas();
//			if ((object instanceof BillAggVO[])) {
//				aggvos = (SettlementAggVO[]) object;
//			} else if ((null != object) && (object.length > 0)
//					&& ((object[0] instanceof SettlementAggVO))) {
//				aggvos = new SettlementAggVO[object.length];
//
//				System.arraycopy(object, 0, aggvos, 0, object.length);
//			}
//			return aggvos;
//		}
//		return this.edit.getValue();
//	}
//}
