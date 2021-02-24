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
 * @description 保存
 * @author xiahui
 * @date 创建时间：2019-1-15 上午9:55:23
 * @version ncc1.0
 **/
public class SaveAction extends ExtBaseSaveAction<AggCtPuVO> {

	@Override
	public AggCtPuVO excute(AggCtPuVO bill) {
		try {
			//by 王志强,判断合同分类是否是最末级
			if(bill.getParentVO().getVdef2()==null) {
				boolean isLeaf =  ServiceLocator.find(ISendSaleServer.class).typeIsLeaf(bill.getParentVO().getVdef1());
				if(!isLeaf) {
				     ExceptionUtils.wrapBusinessException("请选择合同标的!");
				}
			}
			 
			this.beforeProcess(bill);

			CloudPFlowContext context = new CloudPFlowContext();
			context.setActionName("SAVEBASE");
			context.setBillType("Z2");
			context.setBillVos(new AggCtPuVO[] { bill });
			// 执行提交动作脚本
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
			// 新增处理
			this.setWhenAdd(bill);
			// 前台校验合同付款协议页签数据
			this.validateCtPayment(bill);
		} else if (status == VOStatus.UPDATED) {
			// 如果是审批不通过态，修改时设置状态为自由态
			this.resetBillStatusForUnApprove(bill);
			// 前台校验合同付款协议页签数据
			this.validateCtPayment(bill);
		}
	}

	private void validateCtPayment(AggCtPuVO bill) {
		if (!StringUtil.isEmptyTrimSpace(bill.getParentVO().getPk_payterm())) {
			// 表头付款协议有值，表体付款协议页签不能为空
			if (ArrayUtil.isEmpty(bill.getCtPaymentVO())) {
				ExceptionUtils.wrapBusinessException(
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0343")); // 表头存在付款协议，表体付款协议页签不能为空

			}
		}
		if (!ArrayUtil.isEmpty(bill.getCtPaymentVO())) {
			// 付款协议付款比例合不能大于100
			UFDouble accrate = UFDouble.ZERO_DBL;
			// 质保金数量（只能有一个质保金）
			int dcount = 0;

			for (CtPaymentVO ptvo : bill.getCtPaymentVO()) {
				if (ptvo.getStatus() == VOStatus.DELETED) {
					continue;
				}

				if (MathTool.compareTo(UFDouble.ZERO_DBL, ptvo.getAccrate()) >= 0
						|| MathTool.compareTo(ptvo.getAccrate(), new UFDouble(100)) > 0) {
					ExceptionUtils.wrapBusinessException(
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0350")); // 付款比例应大于0小于等于100！

				}

				if (ptvo.getPaymentday() == null) {
					if (ValueUtil.isEmpty(ptvo.getOutaccountdate()) && ValueUtil.isEmpty(ptvo.getCheckdata())) {
						ExceptionUtils.wrapBusinessException(
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0347")); // 账期天数为空，出账日、固定结账日不能同时为空！

					}

					if (ValueUtil.isEmpty(ptvo.getCheckdata()) && !ValueUtil.isEmpty(ptvo.getOutaccountdate())) {
						ExceptionUtils.wrapBusinessException(
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0415")); // 设置了出账日后必须设置固定结账日！
					}

					if (ptvo.getCheckdata() != null && ptvo.getEffectmonth() == null
							|| ptvo.getEffectaddmonth() == null) {
						ExceptionUtils.wrapBusinessException(
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0348")); // 固定结账日有值，生效月、附加月不能为空！
					}
				} else {
					if (!ValueUtil.isEmpty(ptvo.getOutaccountdate()) || !ValueUtil.isEmpty(ptvo.getCheckdata())
							|| !ValueUtil.isEmpty(ptvo.getEffectmonth())
							|| !ValueUtil.isEmpty(ptvo.getEffectaddmonth())) {
						ExceptionUtils.wrapBusinessException(
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0349")); // 账期天数有值，出账日、固定结账日、生效月、附加月不能有值！"

					}
				}

				accrate = MathTool.add(accrate, ptvo.getAccrate());
				if (UFBoolean.TRUE.equals(ptvo.getIsdeposit())) {
					dcount++;
				}
			}
			if (!MathTool.equals(accrate, new UFDouble(100))) {
				ExceptionUtils.wrapBusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0",
						"04020003-0344", null, new String[] { bill.getParentVO().getVbillcode() })); // 单据{0}的比率总和大于100，请检查！

			}
			if (dcount > 1) {
				ExceptionUtils.wrapBusinessException(NCLangResOnserver.getInstance().getStrByID("4020003_0",
						"04020003-0396", null, new String[] { bill.getParentVO().getVbillcode() })); // 单据{0}只能有一个质保金，请检查！
			}
		}
	}

	private void setWhenAdd(AggCtPuVO bill) {
		String pk_org = bill.getParentVO().getPk_org();
		String pk_org_v = bill.getParentVO().getPk_org_v();
		String pk_group = bill.getParentVO().getPk_group();
		CtPuChangeVO[] puchangeVo = new CtPuChangeVO[] { new CtPuChangeVO() };
		puchangeVo[0].setVchangecode(UFDouble.ONE_DBL);
		puchangeVo[0].setVmemo(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0", "04020003-0005")); // 原始版本
		puchangeVo[0].setAttributeValue(PseudoColumnAttribute.PSEUDOCOLUMN, Integer.valueOf(0));
		puchangeVo[0].setPk_org(pk_org);
		puchangeVo[0].setPk_org_v(pk_org_v);
		puchangeVo[0].setPk_group(pk_group);
		puchangeVo[0].setStatus(VOStatus.UNCHANGED); // 返回前台后，设置状态为0
		bill.setCtPuChangeVO(puchangeVo);
	}

	@Override
	protected void afterProcess(ExtBillCard retCard) {
		// 精度处理
		PrecisionUtil.setExtCardPrecision(retCard);
	}

	/**
	 * 重置'审批不通过'为'自由态'
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
	 * 是否处理交互式异常，默认为否
	 * 
	 * @return
	 *
	 */
	protected Boolean isHandleResumeException() {
		return Boolean.TRUE;
	}

}
