package nccloud.web.ct.purdaily.action;

import nc.bs.ml.NCLangResOnserver;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.ct.enumeration.CtFlowEnum;
import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.ct.purdaily.entity.CtPaymentVO;
import nc.vo.ct.purdaily.entity.CtPuChangeVO;
import nc.vo.ct.uitl.ValueUtil;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.pubapp.pattern.model.meta.entity.vo.PseudoColumnAttribute;
import nc.vo.pubapp.pattern.pub.MathTool;
import nc.vo.scmpub.util.ArrayUtil;
import nc.vo.scmpub.util.StringUtil;
import nccloud.dto.scmpub.script.entity.SCMScriptResultDTO;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.ui.pattern.extbillcard.ExtBillCard;
import nccloud.pubitf.riart.pflow.CloudPFlowContext;
import nccloud.pubitf.scmpub.commit.service.IBatchRunScriptService;
import nccloud.web.ct.pub.action.ExtBaseSaveAction;
import nccloud.web.ct.purdaily.utils.PrecisionUtil;

/**
 * @description ����
 * @author xiahui
 * @date ����ʱ�䣺2019-1-15 ����9:55:23
 * @version ncc1.0
 **/
public class SaveAction extends ExtBaseSaveAction<AggCtPuVO> {

	@Override
	public AggCtPuVO excute(AggCtPuVO bill) {
		try {
			//by ��־ǿ,�жϺ�ͬ�����Ƿ�����ĩ��
			if(bill.getParentVO().getVdef2()==null) {
				boolean isLeaf =  ServiceLocator.find(ISendSaleServer.class).typeIsLeaf(bill.getParentVO().getVdef1());
				if(!isLeaf) {
				     ExceptionUtils.wrapBusinessException("��ѡ���ͬ���!");
				}
			}
			 
			this.beforeProcess(bill);

			CloudPFlowContext context = new CloudPFlowContext();
			context.setActionName("SAVEBASE");
			context.setBillType("Z2");
			context.setBillVos(new AggCtPuVO[] { bill });
			// ִ���ύ�����ű�
			SCMScriptResultDTO result = ServiceLocator.find(IBatchRunScriptService.class).runBacth(context,
					AggCtPuVO.class);
			AbstractBill[] retvos = result.getSucessVOs();
			return (AggCtPuVO) retvos[0];
		} catch (Exception e) {
			ExceptionUtils.wrapException(e);
		}
		return null;
	}

	private void beforeProcess(AggCtPuVO bill) {
		int status = bill.getParentVO().getStatus();
		if (status == VOStatus.NEW) {
			// ��������
			this.setWhenAdd(bill);
			// ǰ̨У���ͬ����Э��ҳǩ����
			this.validateCtPayment(bill);
		} else if (status == VOStatus.UPDATED) {
			// �����������ͨ��̬���޸�ʱ����״̬Ϊ����̬
			this.resetBillStatusForUnApprove(bill);
			// ǰ̨У���ͬ����Э��ҳǩ����
			this.validateCtPayment(bill);
		}
	}

	private void validateCtPayment(AggCtPuVO bill) {
		if (!StringUtil.isEmptyTrimSpace(bill.getParentVO().getPk_payterm())) {
			// ��ͷ����Э����ֵ�����帶��Э��ҳǩ����Ϊ��
			if (ArrayUtil.isEmpty(bill.getCtPaymentVO())) {
				ExceptionUtils.wrapBusinessException(
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0343")); // ��ͷ���ڸ���Э�飬���帶��Э��ҳǩ����Ϊ��

			}
		}
		if (!ArrayUtil.isEmpty(bill.getCtPaymentVO())) {
			// ����Э�鸶������ϲ��ܴ���100
			UFDouble accrate = UFDouble.ZERO_DBL;
			// �ʱ���������ֻ����һ���ʱ���
			int dcount = 0;

			for (CtPaymentVO ptvo : bill.getCtPaymentVO()) {
				if (ptvo.getStatus() == VOStatus.DELETED) {
					continue;
				}

				if (MathTool.compareTo(UFDouble.ZERO_DBL, ptvo.getAccrate()) >= 0
						|| MathTool.compareTo(ptvo.getAccrate(), new UFDouble(100)) > 0) {
					ExceptionUtils.wrapBusinessException(
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0350")); // �������Ӧ����0С�ڵ���100��

				}

				if (ptvo.getPaymentday() == null) {
					if (ValueUtil.isEmpty(ptvo.getOutaccountdate()) && ValueUtil.isEmpty(ptvo.getCheckdata())) {
						ExceptionUtils.wrapBusinessException(
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0347")); // ��������Ϊ�գ������ա��̶������ղ���ͬʱΪ�գ�

					}

					if (ValueUtil.isEmpty(ptvo.getCheckdata()) && !ValueUtil.isEmpty(ptvo.getOutaccountdate())) {
						ExceptionUtils.wrapBusinessException(
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0415")); // �����˳����պ�������ù̶������գ�
					}

					if (ptvo.getCheckdata() != null && ptvo.getEffectmonth() == null
							|| ptvo.getEffectaddmonth() == null) {
						ExceptionUtils.wrapBusinessException(
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0348")); // �̶���������ֵ����Ч�¡������²���Ϊ�գ�
					}
				} else {
					if (!ValueUtil.isEmpty(ptvo.getOutaccountdate()) || !ValueUtil.isEmpty(ptvo.getCheckdata())
							|| !ValueUtil.isEmpty(ptvo.getEffectmonth())
							|| !ValueUtil.isEmpty(ptvo.getEffectaddmonth())) {
						ExceptionUtils.wrapBusinessException(
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0349")); // ����������ֵ�������ա��̶������ա���Ч�¡������²�����ֵ��"

					}
				}

				accrate = MathTool.add(accrate, ptvo.getAccrate());
				if (UFBoolean.TRUE.equals(ptvo.getIsdeposit())) {
					dcount++;
				}
			}
			if (!MathTool.equals(accrate, new UFDouble(100))) {
				ExceptionUtils.wrapBusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0",
						"04020003-0344", null, new String[] { bill.getParentVO().getVbillcode() })); // ����{0}�ı����ܺʹ���100�����飡

			}
			if (dcount > 1) {
				ExceptionUtils.wrapBusinessException(NCLangResOnserver.getInstance().getStrByID("4020003_0",
						"04020003-0396", null, new String[] { bill.getParentVO().getVbillcode() })); // ����{0}ֻ����һ���ʱ������飡
			}
		}
	}

	private void setWhenAdd(AggCtPuVO bill) {
		String pk_org = bill.getParentVO().getPk_org();
		String pk_org_v = bill.getParentVO().getPk_org_v();
		String pk_group = bill.getParentVO().getPk_group();
		CtPuChangeVO[] puchangeVo = new CtPuChangeVO[] { new CtPuChangeVO() };
		puchangeVo[0].setVchangecode(UFDouble.ONE_DBL);
		puchangeVo[0].setVmemo(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0005")); // ԭʼ�汾
		puchangeVo[0].setAttributeValue(PseudoColumnAttribute.PSEUDOCOLUMN, Integer.valueOf(0));
		puchangeVo[0].setPk_org(pk_org);
		puchangeVo[0].setPk_org_v(pk_org_v);
		puchangeVo[0].setPk_group(pk_group);
		puchangeVo[0].setStatus(VOStatus.UNCHANGED); // ����ǰ̨������״̬Ϊ0
		bill.setCtPuChangeVO(puchangeVo);
	}

	@Override
	protected void afterProcess(ExtBillCard retCard) {
		// ���ȴ���
		PrecisionUtil.setExtCardPrecision(retCard);
	}

	/**
	 * ����'������ͨ��'Ϊ'����̬'
	 * 
	 * @param bill
	 */
	private void resetBillStatusForUnApprove(AggCtPuVO bill) {
		int fstatusflag = bill.getParentVO().getFstatusflag();
		if (CtFlowEnum.UNAPPROVE.toIntValue() == fstatusflag) {
			bill.getParentVO().setFstatusflag(CtFlowEnum.Free.toIntValue());
			bill.getParentVO().setApprover(null);
			bill.getParentVO().setTaudittime(null);
		}
	}

	/**
	 * 
	 * �Ƿ�����ʽ�쳣��Ĭ��Ϊ��
	 * 
	 * @return
	 *
	 */
	protected Boolean isHandleResumeException() {
		return Boolean.TRUE;
	}

}
