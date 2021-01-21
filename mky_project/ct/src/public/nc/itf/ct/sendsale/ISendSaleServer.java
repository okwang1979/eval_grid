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
 * @author 王志强
 * 煤科院接口后台
 *
 */
public interface ISendSaleServer {
	
	//销售合同
	CtSaleJsonVO pushSaleToService(AggCtSaleVO saleVO) ;
	//采购合同
	CtSaleJsonVO pushPurdailyToService(AggCtPuVO purVO) ;
	//收款单计划
	PaymentPlanAndFeedbackInfo pushBillToService(AggCtSaleVO saleVO) ;
	//收款单反馈
	PaymentPlanAndFeedbackInfo pushBillToService(String pk_ct_sale);
	PaymentPlanAndFeedbackInfo pushBillToService(String pk_ct_sale,String isNormal,String  abnormalReason);
	//付款单计划
	PaymentPlanAndFeedbackInfo pushPayBillToService(AggCtPuVO purVo);
	//付款单反馈
	PaymentPlanAndFeedbackInfo pushPayBillToService(String pk_pu_sale);
	public PaymentPlanAndFeedbackInfo pushPayBillToService(String pk_pu_sale,String isNormal,String  abnormalReason );
	//应付单报送
	JsonReceivableVO pusReceivable(AggReceivableBillVO  billVo);
	
	

 

	
	
	//收款单推送
	JsonReceivableVO pushReceivables(Collection<String>  billVo);
	
	/**
	 * 参数设定,是否发送消息
	 * @return
	 */
	UFBoolean isUseSend(); 
	
	
	UFBoolean isUseSend(Object sendHeadOrOrgPk);
	
	/**
	 * 查找发送url地址.
	 * @return
	 * 
	 */
	String getSendUrl();
	
	
	String getNCFileInfo(Object saleVoOrCpVo);
	
	
	void setSendFlag(SuperVO vo);
	
	/**
	 * 根据合同号查询采购合同 
	 * @param puNo
	 * @return
	 */
	CtPuVO queryByContractNO(String puNo);
	
 

}
