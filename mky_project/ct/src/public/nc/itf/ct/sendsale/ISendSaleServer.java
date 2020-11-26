package nc.itf.ct.sendsale;

import nc.vo.arap.receivable.AggReceivableBillVO;
import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.ct.saledaily.entity.JsonReceivableVO;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;

public interface ISendSaleServer {
	
	//���ۺ�ͬ
	CtSaleJsonVO pushSaleToService(AggCtSaleVO saleVO) ;
	//�ɹ���ͬ
	CtSaleJsonVO pushPurdailyToService(AggCtPuVO purVO) ;
	//�տ
	PaymentPlanAndFeedbackInfo pushBillToService(AggCtSaleVO saleVO) ;
	//���
	PaymentPlanAndFeedbackInfo pushPayBillToService(AggCtPuVO purVO) ;
	
	
	JsonReceivableVO pusReceivable(AggReceivableBillVO  billVo);

}
