package nc.ui.hbbb.convert.avgrate.action;

import java.awt.event.ActionEvent;

import nc.bs.framework.common.NCLocator;
import nc.itf.hbbb.convert.avgrate.IAvgRateQueryService;
import nc.ui.bd.commoninfo.accperiod.view.AccperiodMthRefModel;
import nc.ui.hbbb.convert.avgrate.model.AvgRateAppModel;
import nc.ui.hbbb.convert.avgrate.view.ComputeConditonDlg;
import nc.ui.hbbb.utils.HBBBAccPeriodUtil;
import nc.ui.iufo.dao.DAOAction_Client;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.bill.BillModel;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.UIState;
import nc.ui.uif2.editor.BillForm;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.bd.period.AccperiodVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.ufoc.avgrateinfo.AvgRateVO;

//nc57 nc/util/iufo/cytranslation/ERateFuncCalcUtil.java
public class ComputeAction  extends NCAction{

	private BillManageModel model;
	private BillForm editor;
	private ManageModeActionInterceptor currinfoEditalbe;
	public ComputeAction(){
		super();
		this.setBtnName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0164")/*@res "计算"*/);
		setCode("computeaction");
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -3010573244353918566L;

	@Override
	public void doAction(ActionEvent e) throws Exception {
		//增加权限校验
		if (getCurrinfoEditalbe().beforeDoAction(this, e)) {
			String pk_accperiod = ((AvgRateAppModel)model).getPk_accperiod();
			String pk_accperiodscheme = ((AvgRateAppModel)model).getPk_accperiodscheme();
			
			ComputeConditonDlg dlg = new ComputeConditonDlg(model.getContext().getEntranceUI(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0165")/*@res "会计期间平均汇率录入"*/);
			dlg.initUI();
			dlg.getRateSchemeRefPane().setPK("");
			
			((AccperiodMthRefModel)dlg.getPeriodRefPane().getRefModel()).setClassWherePart("bd_accperiod.pk_accperiod='" + pk_accperiod + "' ");
			AccperiodVO accperiodVO = (AccperiodVO)DAOAction_Client.load(AccperiodVO.class, pk_accperiod);
			
			String accperiod = HBBBAccPeriodUtil.getDefaultAccPeriod(pk_accperiodscheme, KeyVO.ACC_MONTH_PK);
			if(accperiod.substring(0,4).equals(accperiodVO.getPeriodyear())) {
				dlg.getPeriodRefPane().setPK(accperiod);
			}
			else {
				dlg.getPeriodRefPane().setPK(accperiodVO.getPeriodyear() + "-" +new UFDate().toLocalString().substring(5,7));
			}
			
			dlg.getAccperiodschemeRefPane().setPK(pk_accperiodscheme);
			dlg.getAccperiodschemeRefPane().setEnabled(false);
			int showModal = dlg.showModal();
			if(showModal == UIDialog.ID_OK) {

				String month = dlg.getPeriodRefPane().getRefName();
				if(month == null) {
					return;
				}

				AvgRateVO computeRateVO  = null;
				int index = -1;
				for (int i = 0; i < ((AvgRateAppModel)model).getCurrentVOs().length; i++) {
					AvgRateVO rateVO = ((AvgRateAppModel)model).getCurrentVOs()[i];
					if(month.equals(rateVO.getRatemonth())) {
						computeRateVO = rateVO;
						index = i;
						break;
					}
				}
				if(computeRateVO == null) {
					return;
				}
				//AvgRateVO computeAResultvgRate = NCLocator.getInstance().lookup(IAvgRateQueryService.class).computeAvgRate(computeRateVO);
				
				AvgRateVO computeAResultvgRate = NCLocator.getInstance().lookup(IAvgRateQueryService.class).computeAvgRate_gl(computeRateVO);
				computeRateVO = computeAResultvgRate;

				model.setUiState(UIState.EDIT);

				editor.getBillCardPanel().getBillModel().setBodyRowVO(computeRateVO, index);

				//重置状态,确保save时获取到该行数据.不用管是新增还是更新,后台自动根据pk判断
				editor.getBillCardPanel().getBillModel().setRowState(index, BillModel.MODIFICATION);
			}
		}
		
		
	}

	@Override
	protected boolean isActionEnable() {
		return ((AvgRateAppModel)model).getPk_accperiodscheme() != null && ((AvgRateAppModel)model).getPk_org() != null;
	}

	public BillManageModel getModel() {
		return model;
	}

	public void setModel(BillManageModel model) {
		this.model = model;
		this.model.addAppEventListener(this);
	}

	public BillForm getEditor() {
		return editor;
	}

	public void setEditor(BillForm editor) {
		this.editor = editor;
	}

	public ManageModeActionInterceptor getCurrinfoEditalbe() {
		return currinfoEditalbe;
	}

	public void setCurrinfoEditalbe(ManageModeActionInterceptor currinfoEditalbe) {
		this.currinfoEditalbe = currinfoEditalbe;
	}

}