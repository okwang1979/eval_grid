package nccloud.web.ct.saledaily.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.ct.saledaily.ISaledailyMaintain;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.arap.gathering.GatheringBillItemVO;
import nc.vo.arap.gathering.GatheringBillVO;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.ct.saledaily.entity.JsonReceivableVO;
import nc.vo.ct.saledaily.entity.PaymentFeedback;
import nc.vo.ct.saledaily.entity.PaymentPlan;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;
import nccloud.dto.so.pub.entity.SimpleQueryInfo;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.core.json.IJson;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.container.IRequest;
import nccloud.framework.web.json.JsonFactory;
import nccloud.framework.web.ui.pattern.extbillcard.ExtBillCard;
import nccloud.web.ct.saledaily.utils.SaleDailyCompareUtil;
import nccloud.web.scmpub.pub.operator.SCMExtBillCardOperator;

public class SaleDailyCardResendAction extends  SaleDailyCardCommonAction{
	
	
	@Override
	public Object doAction(IRequest request) {
		
		
		
		SimpleQueryInfo[] infos = buildParams(request);

		List<String> pks = new ArrayList<String>();
		for(SimpleQueryInfo info:infos) {
			pks.add(info.getPk());
		}
		ISendSaleServer service = (ISendSaleServer) ServiceLocator.find(ISendSaleServer.class);
		 AggCtSaleVO[] vos = queryVos(pks.toArray(new String[0]));
		 if(vos==null||vos.length==0) {
				return super.doAction(request);
		 }
		Set<String> needUpdateSale  = new HashSet<String>(); 
		if(service.isUseSend(vos[0].getParentVO()).booleanValue()) {
				
				try {
					Logger.init("iufo");
						String appUser="KGJN";
						String secretKey="OXpXfaLG5v0LZedTEi2F2WcnGQmPoi5n0m+srzE1kmE=";
						SaleUrlConst url = SaleUrlConst.getUrlConst();
						TokenInfo tInfo =   SaleSendRestUtil.restLogin( appUser, secretKey,url.getRestLogin());
						
						
					     if(!"200".equals(tInfo.getCode())) {
					      ExceptionUtils.wrapBusinessException(tInfo.getMessage());
					     }
			 	
					for(AggCtSaleVO vo :vos) {
						
						if(vo.getParentVO().getFstatusflag()!=null&&vo.getParentVO().getFstatusflag().intValue()==1) {
							 
						}else {
							ExceptionUtils.wrapBusinessException("请选择生效合同推送。");
						}
						
						//销售合同
						String checkMessage  = service.getNCFileInfo(vo.getParentVO());
						
						if(checkMessage!=null&&checkMessage.length()>0) {
							ExceptionUtils.wrapBusinessException(checkMessage);
						}
						
						CtSaleJsonVO jsonVO = service.pushSaleToService(vo);
						
						IJson json = JsonFactory.create();
						String jsonStr =  json.toJson(jsonVO);
						
						 Logger.init("iufo");
						 Logger.error(jsonStr);
						String rtn = SaleSendRestUtil.registerContractInfo(appUser, tInfo.getToken(), jsonStr, url.getRegisterContractInfo());
						
						TokenInfo info =  (TokenInfo)json.fromJson(rtn, TokenInfo.class);
					     if(!"200".equals(info.getCode())) {
					      ExceptionUtils.wrapBusinessException(info.getMessage());
					     }
					     needUpdateSale.add(vo.getParentVO().getPk_ct_sale());
//					     service.updateSale();
					     vo.getParentVO().setVdef25("已上报");
					     
					     
					     
					     //收入计划单
							ISendSaleServer service1 = ServiceLocator.find(ISendSaleServer.class);
							List<JsonReceivableVO> receivables = service1.pushReceivablesBySale(vo.getParentVO().getPk_ct_sale());
							for(JsonReceivableVO receivable:receivables) {
								
								 jsonStr =  json.toJson(receivable);
								rtn =  SaleSendRestUtil.registerGathering(appUser, tInfo.getToken(), jsonStr, url.getRegisterIncomeInfo());
								
								  info =  (TokenInfo)json.fromJson(rtn, TokenInfo.class);
							     if(!"200".equals(info.getCode())) {
							      ExceptionUtils.wrapBusinessException(info.getMessage());
							     }
							}
							
							
//							senObj(jsonVo1,"/rest/registerIncomeInfo", "registerIncomeInfo");
					     
					     
					     
							 
							
						 //收款单-收款计划
							 
								PaymentPlanAndFeedbackInfo planInfo = service.pushBillToService(vo);
								
								if(planInfo.getPaymentPlanList()!=null&&planInfo.getPaymentPlanList().size()>0) {
									List<PaymentPlan> plans = new ArrayList<PaymentPlan>(planInfo.getPaymentPlanList());
									for(PaymentPlan plan:plans) {
										planInfo.getPaymentPlanList().clear();
										planInfo.getPaymentPlanList().add(plan);
										 jsonStr =  json.toJson(planInfo);
											rtn =  SaleSendRestUtil.receiptBillInfo(appUser, tInfo.getToken(), jsonStr, url.getReceiptBillInfo());
											
											  info =  (TokenInfo)json.fromJson(rtn, TokenInfo.class);
										     if(!"200".equals(info.getCode())) {
										      ExceptionUtils.wrapBusinessException(info.getMessage());
										     }
									}
								}
								
//								 jsonStr =  json.toJson(planInfo);
//									rtn =  SaleSendRestUtil.receiptBillInfo(appUser, tInfo.getToken(), jsonStr, url.getReceiptBillInfo());
//									
//									  info =  (TokenInfo)json.fromJson(rtn, TokenInfo.class);
//								     if(!"200".equals(info.getCode())) {
//								      ExceptionUtils.wrapBusinessException(info.getMessage());
//								     }
								
								
							
//								senObj(planInfo,"/rest/registerPaymentPlanAndFeedbackInfo","paymentPlanAndFeedbackInfo");
							
							
							// 收款单协议计划反馈信息报送
							PaymentPlanAndFeedbackInfo planBackInfo = service.pushBillToService(vo.getParentVO().getPk_ct_sale());
							
							
							if(planBackInfo.getPaymentFeedbackList()!=null&&planBackInfo.getPaymentFeedbackList().size()>0) {
								List<PaymentFeedback> backs = new ArrayList<>(planBackInfo.getPaymentFeedbackList());
								for(PaymentFeedback back:backs) {
									planBackInfo.getPaymentFeedbackList().clear();
									planBackInfo.getPaymentFeedbackList().add(back);
									 jsonStr =  json.toJson(planBackInfo);
									rtn =  SaleSendRestUtil.receiptBillInfo(appUser, tInfo.getToken(), jsonStr, url.getReceiptBillInfo());
									
									  info =  (TokenInfo)json.fromJson(rtn, TokenInfo.class);
								     if(!"200".equals(info.getCode())) {
								      ExceptionUtils.wrapBusinessException(info.getMessage());
								     }
									
								}
								
							}
							
//							 jsonStr =  json.toJson(planBackInfo);
//								rtn =  SaleSendRestUtil.receiptBillInfo(appUser, tInfo.getToken(), jsonStr, url.getReceiptBillInfo());
//								
//								  info =  (TokenInfo)json.fromJson(rtn, TokenInfo.class);
//							     if(!"200".equals(info.getCode())) {
//							      ExceptionUtils.wrapBusinessException(info.getMessage());
//							     }
							
							
							
							
						
//							senObj(planBackInfo,"/rest/registerPaymentPlanAndFeedbackInfo","paymentPlanAndFeedbackInfo");
					     
					     
					     
//							GatheringBillVO[]  billvos  =  null;
//							
//						 for(GatheringBillVO bill:billvos) {
//							 
//						 }
//							
//					    	GatheringBillItemVO[] childrenVO = (GatheringBillItemVO[]) tallySourceData.get(0).getChildVOs();
////					    	//合同主键
//					    	String pk_ct_sale = "";
//					    	if(null != childrenVO && childrenVO.length > 0) {
//					    		pk_ct_sale = childrenVO[0].getTop_billid();
//					    	}
////					    	//根据合同主键查询
//					     
////					    	List<GatheringBillItemVO> queryCtSalePayterms = 
//		 			    	for (GatheringBillItemVO gatheringBillItemVO : childrenVO) {
//		 			    		
//							try {
//							 
//								 
//								send.setPushBill(gatheringBillItemVO.getTop_billid(),billvo.getDef2(),billvo.getDef3());
//								
//		 
//								}catch(Exception ex){
//									Logger.init("iufo");
//									Logger.error(ex);
//									throw ex;
//								}finally {
//									Logger.init();
//								}
//							}
//		 			    	service1.updateGathering(billvo.getPk_gatherbill());
					     
					     
					     
					     
//					   //收款单协议计划信息推送
//						PaymentPlanAndFeedbackInfo planInfo = service.pushBillToService(vos[0]);
//						if(planInfo==null)  ExceptionUtils.wrapException(new BusinessRuntimeException("收款单协议计划信息,转换失败!"));
//							
//						SaleParamCheckUtils.doValidator(planInfo);
//						IJson json1 = JsonFactory.create();
//						String jsonStrPlan =  json1.toJson(planInfo);
//						String resultStr = SaleSendRestUtil.receiptBillInfo(appUser, tInfo.getToken(), jsonStrPlan, url.getReceiptBillInfo());
//						TokenInfo info1 =  (TokenInfo)json1.fromJson(resultStr, TokenInfo.class);
//						if(!"200".equals(info1.getCode())) {
//							ExceptionUtils.wrapBusinessException("收款计划：" + info1.getMessage());
//						}
//						System.out.println("lalalala");
						
//						Logger.error(resultStr);
						
					}
						
		
			 	
					 
			 
					
					
				}catch(Exception ex){
					Logger.init();
					ExceptionUtils.wrapException(ex);
				}
		}
		
		SCMExtBillCardOperator operator = SaleDailyCompareUtil.getBillCardOperator();
		
		ExtBillCard billcard = SaleDailyCompareUtil.operator(operator, vos[0], vos[0]);
		for(String pk_sale:needUpdateSale) {
			service.updateSale(pk_sale);
		}
		return billcard;
		
//		return this.action(vos);
//		return null;
		
	}

	public String getPFActionName() {
		return "RESEND";
	}

	public String getActioncode() {
		return "RESEND";
	}

}
