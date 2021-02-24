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
	

	
	
	/**
	 * 更新销售合同状态
	 * @param pk_sale
	 */
	void updateSale(String pk_sale);//CtSaleVO  ct_sale  def25
	
	/**
	 * 更新采购合同状态
	 * @param pk_pu
	 */
	void updatePu(String pk_pu);//CtPuVO  ct_pu  def25
	
	/**
	 * 更新收入确认单
	 * @param pk_receivable
	 */
	void updateReceivable(String pk_receivable);//ReceivableBillVO   ar_recbill  def8
	
	
	/**更新付款单标志
	 * @param pk_pay
	 */
	void updatePayBill(String pk_pay);//付款单 PayBillVO   ap_paybill   def8
	
	
	/**
	 * 更新收款单标志
	 * @param pk_gethering
	 */
	void updateGathering(String pk_gethering);//GatheringBillVO   ar_gatherbill def8
	boolean typeIsLeaf(String vdef1);
	
	/**根据销售合同返回对应的所有收入确认单的json对象.
	 * @param pk_ct_sale
	 * @return
	 */
	List<JsonReceivableVO> pushReceivablesBySale(String pk_ct_sale);
	
	
	
	
 

}
