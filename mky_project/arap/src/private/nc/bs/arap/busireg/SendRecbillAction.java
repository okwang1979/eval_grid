package nc.bs.arap.busireg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.yonyou.sscip.gson.Gson;
import com.yonyou.sscip.gson.GsonBuilder;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.ct.purdaily.IPurdailyMaintain;
import nc.itf.ct.saledaily.ISaledailyMaintain;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.arap.gathering.AggGatheringBillVO;
import nc.vo.arap.gathering.GatheringBillItemVO;
import nc.vo.arap.pay.PayBillItemVO;
import nc.vo.arap.receivable.AggReceivableBillVO;
import nc.vo.arap.receivable.ReceivableBillVO;
import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.ct.purdaily.entity.CtPuVO;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.JsonReceivableVO;
import nc.vo.ct.saledaily.entity.PaymentFeedback;
import nc.vo.ct.saledaily.entity.PaymentPlan;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;
import nc.vo.ct.saledaily.entity.SaleConst;
import nc.vo.pub.BusinessRuntimeException;
import nccloud.framework.service.ServiceLocator;

public class SendRecbillAction {

	private Gson json = null;

	public void setAggReceivableBillVO(AggReceivableBillVO vo) {

	 
		 
			ISendSaleServer service = NCLocator.getInstance().lookup(ISendSaleServer.class);
			JsonReceivableVO jsonVo = service.pusReceivable(vo);
			senObj(jsonVo,"/rest/registerIncomeInfo", "registerIncomeInfo");
 
	 
	}

	public void setPushBill(String pk_sale, String err, String errinfo) {
		try {
			
			
			ISendSaleServer service = NCLocator.getInstance().lookup(ISendSaleServer.class);
		 
			
			
			ISaledailyMaintain queryService = NCLocator.getInstance().lookup(ISaledailyMaintain.class);

			 
				  String[] ids = {pk_sale};
			   AggCtSaleVO[] vos = queryService.queryCtApVoByIds(ids);
			  if(vos==null&&vos.length==0) {
				  return;
			  }
				PaymentPlanAndFeedbackInfo planInfo = service.pushBillToService(vos[0]);
				
				
				if(planInfo.getPaymentPlanList()!=null&&planInfo.getPaymentPlanList().size()>0) {
					List<PaymentPlan> plans = new ArrayList<>(planInfo.getPaymentPlanList());
					for(PaymentPlan plan:plans) {
						planInfo.getPaymentPlanList().clear();
						planInfo.getPaymentPlanList().add(plan);
						senObj(planInfo,"/rest/registerPaymentPlanAndFeedbackInfo","paymentPlanAndFeedbackInfo");
					}
					
				}
			
//				senObj(planInfo,"/rest/registerPaymentPlanAndFeedbackInfo","paymentPlanAndFeedbackInfo");
			
			
			// 收款单协议计划反馈信息报送
			PaymentPlanAndFeedbackInfo planBackInfo = service.pushBillToService(pk_sale,err,errinfo);
			
			
			
			
			
			if(planBackInfo.getPaymentFeedbackList()!=null&&planBackInfo.getPaymentFeedbackList().size()>0) {
				List<PaymentFeedback> backs = new ArrayList<>(planBackInfo.getPaymentFeedbackList());
				for(PaymentFeedback back:backs) {
					planBackInfo.getPaymentFeedbackList().clear();
					planBackInfo.getPaymentFeedbackList().add(back);
					senObj(planBackInfo,"/rest/registerPaymentPlanAndFeedbackInfo","paymentPlanAndFeedbackInfo");

					
				}
			}
			
		
//			senObj(planBackInfo,"/rest/registerPaymentPlanAndFeedbackInfo","paymentPlanAndFeedbackInfo");

		}catch(Exception ex) {
			Logger.error(ex);
			 throw new BusinessRuntimeException(ex.getMessage());
		}

	
	}

	private void senObj(Object jsonObj,String url,String info) {
		
		TokenInfo loginInfo;
		try {
			Logger.init("iufo");
			loginInfo = restLogin(SaleConst.getAPP_USER(), SaleConst.getSECRE_KEY(),
					SaleConst.getIP_PORINT() + "/rest/login");
			if(loginInfo==null) {
				throw new BusinessRuntimeException("获取Token失败.");
			}
			
		
		 

	 
      
		String jsonStr = getJson().toJson(jsonObj);
		
		
		
		String rtn = callUrl(SaleConst.getAPP_USER(), loginInfo.getToken(), jsonStr,
				SaleConst.getIP_PORINT() + url,
				info);
		
		TokenInfo rtnToken =  (TokenInfo)json.fromJson(rtn, TokenInfo.class);
	     if(!"200".equals(rtnToken.getCode())) {
	    	 throw new BusinessRuntimeException(rtnToken.getMessage());
	     }
 
		Logger.error(jsonStr);
		} catch (Exception e) {
			Logger.init("iufo");
			Logger.error("获取token失败!");
			Logger.error(e);
			 throw new BusinessRuntimeException(e.getMessage());
			
			
			 
		}finally {
			Logger.init();
		}
		
	}

	private Gson getJson() {

		if (json == null) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setPrettyPrinting();
			json = gsonBuilder.create();

		}
		return json;

	}

	public static String callUrl(String appuser, String token, String bodyJson, String url, String key) {

		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(3000);
		factory.setReadTimeout(10000);
		RestTemplate template = new RestTemplate(factory);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("appuser", appuser);
		headers.add("token", token);
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
		headers.add("timestamp", f.format(new Date()));

		Map<String, String> map = new HashMap<String, String>();

		map.put(key, bodyJson);
		
 

		HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);

//		    MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
//		    map.add("contractInfo", bodyJson);;
//		 
//		    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		ResponseEntity<String> response = template.postForEntity(url, request, String.class);

		return response.getBody();
	}

	private TokenInfo restLogin(String appUser, String secretKey, String url) throws Exception {

		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(3000);
		factory.setReadTimeout(10000);
		RestTemplate template = new RestTemplate(factory);
		HttpHeaders headers = new HttpHeaders();
		headers.add("appuser", appUser);
		headers.add("secretkey", secretKey);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		ResponseEntity<String> response = template.postForEntity(url, request, String.class);
		String json = response.getBody();
//		   
//	    
		return getToken(json);
	}

	private TokenInfo getToken(String jsonStr) {

		TokenInfo info = (TokenInfo) getJson().fromJson(jsonStr, TokenInfo.class);
		return info;
	}

	public void setAggGatheringBillVO(AggGatheringBillVO bill) {

		Set<String> pks = new HashSet<>();
		if (bill.getBodyVOs() == null || bill.getBodyVOs().length == 0) {
			return;
		}
		for (GatheringBillItemVO item : bill.getBodyVOs()) {
			if (item.getSrc_billid() != null) {
				pks.add(item.getSrc_billid());
			}

		}

//		ISendSaleServer service1 = (ISendSaleServer) ServiceLocator.find(ISendSaleServer.class);
//		
//		JsonReceivableVO jsonVo1 = service1.pushReceivables(pks);
		TokenInfo tInfo = null;
		try {
			Logger.init("iufo");
			tInfo = restLogin(SaleConst.getAPP_USER(), SaleConst.getSECRE_KEY(),
					SaleConst.getIP_PORINT() + "/rest/login");
			if (!"200".equals(tInfo.getCode())) {
			}

			ISendSaleServer service = (ISendSaleServer) NCLocator.getInstance().lookup(ISendSaleServer.class);

			JsonReceivableVO jsonVo = service.pushReceivables(pks);

			String jsonStr = getJson().toJson(jsonVo);

			Logger.init("iufo");
			Logger.error(jsonStr);
			String rtn = callUrl(SaleConst.getAPP_USER(), tInfo.getToken(), jsonStr,
					SaleConst.getIP_PORINT() + "/rest/registerIncomeInfo", "registerIncomeInfo");

			TokenInfo info = (TokenInfo) json.fromJson(rtn, TokenInfo.class);
			if (!"200".equals(info.getCode())) {
			}
		} catch (Exception e) {
			Logger.init("iufo");
			Logger.error(e);
			throw new BusinessRuntimeException(e.getMessage(), e);
		} finally {
			Logger.init();
		}

	}

	public void setAggGatheringBillVO(ReceivableBillVO vo) {

		Set<String> pks = new HashSet<>();

		if (vo != null && vo.getPk_recbill() != null) {
			pks.add(vo.getPk_recbill());
		}

		ISendSaleServer service1 = NCLocator.getInstance().lookup(ISendSaleServer.class);
		JsonReceivableVO jsonVo1 = service1.pushReceivables(pks);
		
		senObj(jsonVo1,"/rest/registerIncomeInfo", "registerIncomeInfo");
 

	}

	public void pushPayBillToService(PayBillItemVO item,String isNormal,String abnormalReason) {
		try {
			
		    ISendSaleServer service1 = NCLocator.getInstance().lookup(ISendSaleServer.class);
			String pk_pu_sale =item.getContractno();
			CtPuVO po =  service1.queryByContractNO(pk_pu_sale);
			
			if(po==null) {
				return;
			}
			pk_pu_sale = po.getPk_ct_pu();
		    IPurdailyMaintain service2 = (IPurdailyMaintain) NCLocator.getInstance().lookup(IPurdailyMaintain.class);
		    String[] ids = {pk_pu_sale};
		    AggCtPuVO[] vos = service2.queryCtPuVoByIds(ids);
	
		    if(vos!=null&&vos.length>0) {
		    	PaymentPlanAndFeedbackInfo info =	service1.pushPayBillToService(vos[0]);
		    	

		    	if(info.getPaymentPlanList()!=null&&info.getPaymentPlanList().size()>0) {
		    		List<PaymentPlan> plans = new ArrayList<>(info.getPaymentPlanList());
		    		for(PaymentPlan plan:plans) {
		    			info.getPaymentPlanList().clear();
		    			info.getPaymentPlanList().add(plan);
		    			senObj(info,"/rest/registerPaymentPlanAndFeedbackInfo", "paymentPlanAndFeedbackInfo");
		    		}
		    		
		    	}
		    	
//				senObj(info,"/rest/registerPaymentPlanAndFeedbackInfo", "paymentPlanAndFeedbackInfo");
				
		    }else {
		    	return ;
		    }
			
			
			
		    PaymentPlanAndFeedbackInfo info  = service1.pushPayBillToService(pk_pu_sale,isNormal,abnormalReason);
			
		    
		    if(info.getPaymentFeedbackList()!=null&&info.getPaymentFeedbackList().size()>0) {
		    	List<PaymentFeedback> backs = new ArrayList<>(info.getPaymentFeedbackList());
		    	for(PaymentFeedback back:backs) {
		    		info.getPaymentFeedbackList().clear();
		    		info.getPaymentFeedbackList().add(back);
		    		
		    		senObj(info,"/rest/registerPaymentPlanAndFeedbackInfo", "paymentPlanAndFeedbackInfo");
		    	}
		    }
			
//			senObj(info,"/rest/registerPaymentPlanAndFeedbackInfo", "paymentPlanAndFeedbackInfo");
			
		}catch(Exception ex) { 	
			throw new BusinessRuntimeException(ex.getMessage(), ex);
			
		}
	
	}

}

class TokenInfo {
	private String code;
	private String token;
	private Integer successflag;

	private String message;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Integer getSuccessflag() {
		return successflag;
	}

	public void setSuccessflag(Integer successflag) {
		this.successflag = successflag;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}