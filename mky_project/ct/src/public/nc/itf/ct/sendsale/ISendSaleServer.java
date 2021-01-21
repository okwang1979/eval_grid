package nc.itf.ct.sendsale;

import java.util.Collection;
import java.util.List;

import nc.vo.arap.receivable.AggReceivableBillVO;
import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.ct.purdaily.entity.CtPuVO;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.ct.saledaily.entity.JsonReceivableVO;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;

/**
 * @author ��־ǿ
 * ú��Ժ�ӿں�̨
 *
 */
public interface ISendSaleServer {
	
	//���ۺ�ͬ
	CtSaleJsonVO pushSaleToService(AggCtSaleVO saleVO) ;
	//�ɹ���ͬ
	CtSaleJsonVO pushPurdailyToService(AggCtPuVO purVO) ;
	//�տ�ƻ�
	PaymentPlanAndFeedbackInfo pushBillToService(AggCtSaleVO saleVO) ;
	//�տ����
	PaymentPlanAndFeedbackInfo pushBillToService(String pk_ct_sale);
	PaymentPlanAndFeedbackInfo pushBillToService(String pk_ct_sale,String isNormal,String  abnormalReason);
	//����ƻ�
	PaymentPlanAndFeedbackInfo pushPayBillToService(AggCtPuVO purVo);
	//�������
	PaymentPlanAndFeedbackInfo pushPayBillToService(String pk_pu_sale);
	public PaymentPlanAndFeedbackInfo pushPayBillToService(String pk_pu_sale,String isNormal,String  abnormalReason );
	//Ӧ��������
	JsonReceivableVO pusReceivable(AggReceivableBillVO  billVo);
	
	

 

	
	
	//�տ����
	JsonReceivableVO pushReceivables(Collection<String>  billVo);
	
	/**
	 * �����趨,�Ƿ�����Ϣ
	 * @return
	 */
	UFBoolean isUseSend(); 
	
	
	UFBoolean isUseSend(Object sendHeadOrOrgPk);
	
	/**
	 * ���ҷ���url��ַ.
	 * @return
	 * 
	 */
	String getSendUrl();
	
	
	String getNCFileInfo(Object saleVoOrCpVo);
	
	
	void setSendFlag(SuperVO vo);
	
	/**
	 * ���ݺ�ͬ�Ų�ѯ�ɹ���ͬ 
	 * @param puNo
	 * @return
	 */
	CtPuVO queryByContractNO(String puNo);
	
 

}
