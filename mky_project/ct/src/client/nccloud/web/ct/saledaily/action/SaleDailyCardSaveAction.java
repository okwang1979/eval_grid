package nccloud.web.ct.saledaily.action;

import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleChangeVO;
import nc.vo.pubapp.pattern.model.meta.entity.vo.PseudoColumnAttribute;
import nc.vo.scmpub.res.billtype.CTBillType;
import nc.vo.so.m4331.entity.DeliveryVO;
import nccloud.dto.scmpub.script.entity.SCMScriptResultDTO;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.action.itf.ICommonAction;
import nccloud.framework.web.container.IRequest;
import nccloud.framework.web.ui.pattern.extbillcard.ExtBillCard;
import nccloud.pubitf.riart.pflow.CloudPFlowContext;
import nccloud.pubitf.scmpub.commit.service.IBatchRunScriptService;
import nccloud.web.ct.saledaily.utils.SaleDailyCompareUtil;
import nccloud.web.scmpub.pub.operator.SCMExtBillCardOperator;

/**
 * @description ���ۺ�ͬ����
 * @author wangshrc
 * @date 2019��1��17�� ����10:16:56
 * @version ncc1.0
 */
public class SaleDailyCardSaveAction implements ICommonAction {

	@Override
	public Object doAction(IRequest request) {
		SCMExtBillCardOperator operator = SaleDailyCompareUtil.getBillCardOperator();
		AggCtSaleVO vo = (AggCtSaleVO) operator.toBill(request);
		AggCtSaleVO origvo = (AggCtSaleVO) operator.toBill(request);
		//by ��־ǿ,�жϺ�ͬ�����Ƿ�����ĩ��
		if(vo.getParentVO().getVdef2()==null) {
			boolean isLeaf =  ServiceLocator.find(ISendSaleServer.class).typeIsLeaf(vo.getParentVO().getVdef1());
			if(!isLeaf) {
			     ExceptionUtils.wrapBusinessException("��ѡ���ͬ���!");
			}
		}
		vo.getParentVO().setCbilltypecode("Z3");
		this.setWhenAdd(vo);
		AggCtSaleVO[] bills = new AggCtSaleVO[] { vo };
		CloudPFlowContext context = new CloudPFlowContext();
		context.setTrantype(vo.getParentVO().getVtrantypecode());
		context.setBillType(CTBillType.SaleDaily.getCode());
		context.setBillVos(bills);
		context.setActionName("SAVEBASE");
		SCMScriptResultDTO dto = ServiceLocator.find(IBatchRunScriptService.class).runBacth(context, DeliveryVO.class);
		if (dto.getSucessVOs() == null)
			return null;
		AggCtSaleVO singleVo = (AggCtSaleVO) dto.getSucessVOs()[0];
		ExtBillCard card = SaleDailyCompareUtil.operator(operator, singleVo,origvo);
		return card;
	}

	/**
	 * ������ȫǰ�˱����ʷԭʼ�汾����
	 * 
	 * @param vo
	 */
	private void setWhenAdd(AggCtSaleVO vo) {
		if (vo.getPrimaryKey() == null || "".equals(vo.getPrimaryKey().toString())) {
			CtSaleChangeVO[] salechangeVo = vo.getCtSaleChangeVO();
			String pk_org = vo.getParentVO().getPk_org();
			String pk_org_v = vo.getParentVO().getPk_org_v();
			String pk_group = vo.getParentVO().getPk_group();
//			salechangeVo[0].setVchangecode(UFDouble.ONE_DBL);
//			salechangeVo[0].setVmemo(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("4020003_0",
//					"04020003-0005")/* @res "ԭʼ�汾" */);
			salechangeVo[0].setAttributeValue(PseudoColumnAttribute.PSEUDOCOLUMN, Integer.valueOf(0));
			salechangeVo[0].setPk_org(pk_org);
			salechangeVo[0].setPk_org_v(pk_org_v);
			salechangeVo[0].setPk_group(pk_group);
			((AggCtSaleVO) vo).setCtSaleChangeVO(salechangeVo);
		}
	}
}
