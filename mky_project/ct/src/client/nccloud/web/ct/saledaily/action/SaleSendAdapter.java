package nccloud.web.ct.saledaily.action;

import java.util.ArrayList;
import java.util.List;

import nc.bs.logging.Logger;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.ct.saledaily.entity.PaymentFeedback;
import nc.vo.ct.saledaily.entity.PaymentPlan;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;
import nc.vo.ct.saledaily.entity.SaleParamCheckUtils;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.core.json.IJson;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.json.JsonFactory;

public class SaleSendAdapter {
	
	public  void doAction(AbstractBill[] bills,boolean isReSend) {
		
		ISendSaleServer service = (ISendSaleServer) ServiceLocator.find(ISendSaleServer.class);
		
		
		for(AbstractBill bill:bills) {
			if(bill instanceof AggCtPuVO) {
				AggCtPuVO aggVo = (AggCtPuVO) bill;
//				if(aggVo.getParentVO().getFstatusflag()!=null&&aggVo.getParentVO().getFstatusflag().intValue()==1) {
//					 
//				}else {
//					ExceptionUtils.wrapBusinessException("请发送生效合同。");
//				}
				
			 
				if( !service.isUseSend(aggVo.getParentVO()).booleanValue()) {
					return;
				}
				 
				String checkMessage  = service.getNCFileInfo(aggVo.getParentVO());
				
				if(checkMessage!=null&&checkMessage.length()>0) {
					ExceptionUtils.wrapBusinessException(checkMessage);
				}
			}
			
		}
	
		
		String appUser="KGJN";
		String secretKey="OXpXfaLG5v0LZedTEi2F2WcnGQmPoi5n0m+srzE1kmE=";
		
		TokenInfo tInfo = null;
		SaleUrlConst url = SaleUrlConst.getUrlConst();

		try {
			
			
			
			tInfo = SaleSendRestUtil.restLogin( appUser, secretKey,url.getRestLogin());

		     if(!"200".equals(tInfo.getCode())) {
		      ExceptionUtils.wrapBusinessException(tInfo.getMessage());
		     }
		} catch (Exception e) {
			if(tInfo!=null) {
				ExceptionUtils.wrapBusinessException(tInfo.getMessage());
			}else {
				ExceptionUtils.wrapBusinessException("查询Token错误!");
			}
			
		}
		
		
	
	
		
		for(AbstractBill bill:bills) {
			if(bill instanceof AggCtPuVO) {
				AggCtPuVO aggVo = (AggCtPuVO) bill;
				
			
				CtSaleJsonVO jsonVO = service.pushPurdailyToService(aggVo);
				
//				SaleParamCheckUtils.doValidator(jsonVO);
				IJson json = JsonFactory.create();
				String jsonStr =  json.toJson(jsonVO);
				
				 Logger.init("iufo");
				 Logger.error(jsonStr);
				String rtn = SaleSendRestUtil.registerContractInfo(appUser, tInfo.getToken(), jsonStr, url.getRegisterContractInfo());
				
				TokenInfo info =  (TokenInfo)json.fromJson(rtn, TokenInfo.class);
			     if(!"200".equals(info.getCode())) {
			      ExceptionUtils.wrapBusinessException(info.getMessage());
			     }
//			     
			     
			  
//			     service.updatePu(aggVo.getParentVO().getPk_ct_pu());
			     aggVo.getParentVO().setVdef25("已上报");
			     
			     
			     if(isReSend) {
						 
					 
						  PaymentPlanAndFeedbackInfo planInfo = service.pushPayBillToService(aggVo);
						  
						   IJson json1 =  JsonFactory.create(); 
						 
						   if(planInfo.getPaymentPlanList()!=null&&planInfo.getPaymentPlanList().size()>0) {
							   java.util.List<PaymentPlan>  plans = new ArrayList<>(planInfo.getPaymentPlanList());
//							   plans.addAll(c);
							   for(PaymentPlan plan:plans) {
								   planInfo.getPaymentPlanList().clear();
								   planInfo.getPaymentPlanList().add(plan);
								   String jsonStrPlan = json1.toJson(planInfo);
								   String	 resultStr = SaleSendRestUtil.payBillInfo(appUser, tInfo.getToken(), jsonStrPlan, url.getPayBillInfo()); 
								TokenInfo info1 = (TokenInfo)json1.fromJson(resultStr, TokenInfo.class);
									   if(!"200".equals(info1.getCode())) {
										   ExceptionUtils.wrapBusinessException("付款计划：" + info1.getMessage()); 
									  }
							   }
							   
						   }
						
						   
						   
						   
							   planInfo = service.pushPayBillToService(aggVo.getParentVO().getPk_ct_pu());
							  
							   if(planInfo.getPaymentFeedbackList()!=null&&planInfo.getPaymentFeedbackList().size()>0) {
								   
								   List<PaymentFeedback> backs = new ArrayList<>(planInfo.getPaymentFeedbackList());
								   for(PaymentFeedback back:backs) {
									   planInfo.getPaymentFeedbackList().clear();
									   planInfo.getPaymentFeedbackList().add(back);
									   String jsonStrPlan = json1.toJson(planInfo);
								    	 String resultStr = SaleSendRestUtil.payBillInfo(appUser, tInfo.getToken(), jsonStrPlan, url.getPayBillInfo()); 
								    	 TokenInfo info1 = (TokenInfo)json1.fromJson(resultStr, TokenInfo.class);
									   if(!"200".equals(info1.getCode())) {
										   ExceptionUtils.wrapBusinessException("付款计划：" + info1.getMessage()); 
									  }
									   
								   }
								 
								   
							   }
							   
							   
							   
								//付款单协议计划信息推送（废弃）
								/*
								 * PaymentPlanAndFeedbackInfo planInfo = service.pushPayBillToService(aggVo);
								 * SaleParamCheckUtils.doValidator(planInfo); IJson json1 =
								 * JsonFactory.create(); String jsonStrPlan = json1.toJson(planInfo); String
								 * resultStr = SaleSendRestUtil.payBillInfo(appUser, tInfo.getToken(),
								 * jsonStrPlan, url.getPayBillInfo()); TokenInfo info1 =
								 * (TokenInfo)json1.fromJson(resultStr, TokenInfo.class);
								 * if(!"200".equals(info1.getCode())) {
								 * ExceptionUtils.wrapBusinessException("付款计划：" + info1.getMessage()); }
								 */
							  
					 
			     }

			
			
				
				
				
				
				
			}
			
		}
		
		
	}
	
	public  void doAction(AbstractBill[] bills) {
		doAction(bills,false);
	}
	

}
