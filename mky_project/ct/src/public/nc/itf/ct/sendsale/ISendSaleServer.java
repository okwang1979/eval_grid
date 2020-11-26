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
	//收款单
	PaymentPlanAndFeedbackInfo pushBillToService(AggCtSaleVO saleVO) ;
	//付款单
	PaymentPlanAndFeedbackInfo pushPayBillToService(AggCtPuVO purVO) ;
	
	
	JsonReceivableVO pusReceivable(AggReceivableBillVO  billVo);

}
