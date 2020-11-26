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
	//�տ�ƻ�
	PaymentPlanAndFeedbackInfo pushBillToService(AggCtSaleVO saleVO) ;
	//�տ����
	PaymentPlanAndFeedbackInfo pushBillToService(String pk_ct_sale);
	//����ƻ�
	PaymentPlanAndFeedbackInfo pushPayBillToService(AggCtPuVO purVo);
	//�������
	PaymentPlanAndFeedbackInfo pushPayBillToService(String pk_pu_sale);
	//Ӧ��������
	JsonReceivableVO pusReceivable(AggReceivableBillVO  billVo);
	
 

}
