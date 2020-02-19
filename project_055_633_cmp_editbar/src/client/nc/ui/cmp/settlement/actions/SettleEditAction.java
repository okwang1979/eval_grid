package nc.ui.cmp.settlement.actions;


import java.awt.event.ActionEvent;

import nc.cmp.utils.CmpUtils;
import nc.ui.cmp.settlement.view.SettlementCard;
import nc.ui.cmp.settlement.view.SettlementList;
import nc.ui.uif2.UIState;
import nc.ui.uif2.actions.ActionInitializer;
import nc.ui.uif2.actions.EditAction;
import nc.ui.uif2.model.AbstractAppModel;
import nc.vo.cmp.BusiStatus;
import nc.vo.cmp.SettleStatus;
import nc.vo.cmp.bill.BillAggVO;
import nc.vo.cmp.settlement.SettlementAggVO;
import nc.vo.cmp.settlement.SettlementHeadVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;

public class SettleEditAction extends EditAction {
	private static final long serialVersionUID = -5210224731564447169L;
	private AbstractAppModel model;
	private SettlementCard edit;
	private SettlementList listView;

	public SettleEditAction() {
		ActionInitializer.initializeAction(this, "Edit");
	}

	public void doAction(ActionEvent e) throws Exception {
		if (getValue() == null) {
			throw new BusinessException(NCLangRes4VoTransl
					.getNCLangRes().getStrByID("3607set_0", "03607set-0044"));
		}
		
		

		if (!getEdit().isShowing()) {
			getEdit().showMeUp();
		}
		getModel().setUiState(UIState.EDIT);
	}

	protected boolean isActionEnable() {
		if ((UIState.ADD == getModel().getUiState())
				|| (UIState.EDIT == getModel().getUiState())) {
			return false;
		}
		SettlementAggVO[] selectedAggVOs = getSelectedAggVOs();

		if ((CmpUtils.isListNull(selectedAggVOs))
				|| (selectedAggVOs.length > 1)) {
			return false;
		}

		SettlementHeadVO headvo = (SettlementHeadVO) selectedAggVOs[0]
				.getParentVO();
		Integer busistatus = Integer
				.valueOf(headvo.getBusistatus() == null ? BusiStatus.Save
						.getBillStatusKind() : headvo.getBusistatus()
						.intValue());

		Integer settleStatus = headvo.getSettlestatus();
		settleStatus = Integer
				.valueOf(settleStatus == null ? SettleStatus.NONESETTLE
						.getStatus() : settleStatus.intValue());

		if ((settleStatus.intValue() == SettleStatus.NONESETTLE
				.getStatus()) && (headvo.getPk_signer() == null)) {
			return true;
		}
		return false;
	}

	public SettlementAggVO[] getSelectedAggVOs() {
		Object[] value = null;
		if (isListSelected()) {
			value = getListView().getModel().getSelectedOperaDatas();
		} else {
			value = new Object[1];
			value[0] = getModel().getSelectedData();
		}

		if ((null == value) || (value.length == 0)) {
			return null;
		}
		SettlementAggVO[] aggs = new SettlementAggVO[value.length];
		System.arraycopy(value, 0, aggs, 0, aggs.length);
		if (!CmpUtils.isListNull(aggs)) {
			return aggs;
		}
		return null;
	}

	public void setEdit(SettlementCard edit) {
		this.edit = edit;
	}

	public SettlementCard getEdit() {
		return this.edit;
	}

	public SettlementList getListView() {
		return this.listView;
	}

	public void setListView(SettlementList listView) {
		this.listView = listView;
	}

	public AbstractAppModel getModel() {
		return this.model;
	}

	public void setModel(AbstractAppModel model) {
		this.model = model;
		model.addAppEventListener(this);
	}

	public boolean isListSelected() {
		if ((this.listView != null) && (this.listView.isShowing())) {
			return true;
		}
		return false;
	}

	public Object getValue() {
		if (isListSelected()) {
			SettlementAggVO[] aggvos = null;
			Object[] object = this.listView.getModel().getSelectedOperaDatas();
			if ((object instanceof BillAggVO[])) {
				aggvos = (SettlementAggVO[]) object;
			} else if ((null != object) && (object.length > 0)
					&& ((object[0] instanceof SettlementAggVO))) {
				aggvos = new SettlementAggVO[object.length];

				System.arraycopy(object, 0, aggvos, 0, object.length);
			}
			return aggvos;
		}
		return this.edit.getValue();
	}
}
