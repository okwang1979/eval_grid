package nc.itf.ct.sendsale;

import nc.vo.arap.receivable.AggReceivableBillVO;
import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.ct.saledaily.entity.JsonReceivableVO;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;

public interface ISendSaleServer {
	
	//销售合同
	CtSaleJsonVO pushSaleToService(AggCtSaleVO saleVO) ;
	//采购合同
	CtSaleJsonVO pushPurdailyToService(AggCtPuVO purVO) ;
	//收款单计划
	PaymentPlanAndFeedbackInfo pushBillToService(AggCtSaleVO saleVO) ;
	//收款单反馈
	PaymentPlanAndFeedbackInfo pushBillToService(String pk_ct_sale);
	//付款单计划
	PaymentPlanAndFeedbackInfo pushPayBillToService(AggCtPuVO purVo);
	//付款单反馈
	PaymentPlanAndFeedbackInfo pushPayBillToService(String pk_pu_sale);
	//应付单报送
	JsonReceivableVO pusReceivable(AggReceivableBillVO  billVo);
	
 

}
