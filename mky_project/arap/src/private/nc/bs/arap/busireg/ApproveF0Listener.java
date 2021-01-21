package nc.bs.arap.busireg;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import nc.bs.businessevent.IBusinessEvent;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.arap.tally.ITallySourceData;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.jdbc.framework.SQLParameter;
import nc.pubitf.arap.tally.ITallyService;
import nc.vo.arap.gathering.GatheringBillItemVO;
import nc.vo.arap.gathering.GatheringBillVO;
import nc.vo.arap.pay.PayBillItemVO;
import nc.vo.arap.pay.PayBillVO;
import nc.vo.arap.receivable.ReceivableBillVO;
import nc.vo.arap.tally.BusiTypeEnum;
import nc.vo.ct.saledaily.entity.CtSalePayTermVO;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.SuperVO;
import nccloud.commons.lang.ArrayUtils;
import nccloud.framework.service.ServiceLocator;

public class ApproveF0Listener extends AbstractTallyListener {
	public void doAction(IBusinessEvent event) throws BusinessException {
		try {
			Logger.init("iufo");
			AggregatedValueObject[] obills = getBills(event);
			if (ArrayUtils.isEmpty(obills)) {
				return;
			}
			String eventType = event.getEventType();
			List<ITallySourceData> tallySourceData = construct(obills);
			if ((tallySourceData == null) || (tallySourceData.size() == 0)) {
				return;
			}
			SendRecbillAction send = new SendRecbillAction();
//			ITallyService tallySrv = (ITallyService) NCLocator.getInstance().lookup(ITallyService.class);
			if ("1020".equals(eventType)||"1004".equals(eventType)) {
				ISendSaleServer service1 = NCLocator.getInstance().lookup(ISendSaleServer.class);
				if(tallySourceData.get(0).getHeadVO() instanceof ReceivableBillVO) {
					
					ReceivableBillVO vo =  (ReceivableBillVO)tallySourceData.get(0).getHeadVO();
					if("F0-Cxx-01".equals(vo.getPk_tradetype())) {
						 if(!service1.isUseSend(vo).booleanValue()) {
							 return;
						 }
						
					
						send.setAggGatheringBillVO(vo);
						
					}
					 
				}else if(tallySourceData.get(0).getHeadVO() instanceof GatheringBillVO) {
					GatheringBillVO  billvo  =  (GatheringBillVO)tallySourceData.get(0).getHeadVO();
					
					
					 if(!service1.isUseSend(billvo).booleanValue()) {
						 return;
					 }
					
					
					
			    	GatheringBillItemVO[] childrenVO = (GatheringBillItemVO[]) tallySourceData.get(0).getChildVOs();
//			    	//合同主键
			    	String pk_ct_sale = "";
			    	if(null != childrenVO && childrenVO.length > 0) {
			    		pk_ct_sale = childrenVO[0].getTop_billid();
			    	}
//			    	//根据合同主键查询
			     
//			    	List<GatheringBillItemVO> queryCtSalePayterms = 
 			    	for (GatheringBillItemVO gatheringBillItemVO : childrenVO) {
 			    		
					try {
					 
						 
						send.setPushBill(gatheringBillItemVO.getTop_billid());
						
 
						}catch(Exception ex){
							Logger.init("iufo");
							Logger.error(ex);
							throw ex;
						}finally {
							Logger.init();
						}
					}
			      
					
					
					
					
					
					
				}else if(tallySourceData.get(0).getHeadVO() instanceof PayBillVO) {
					
					PayBillVO payVo = (PayBillVO)tallySourceData.get(0).getHeadVO();
					
					 if(!service1.isUseSend(payVo).booleanValue()) {
						 return;
					 }
			    	PayBillItemVO[] childrenVO = (PayBillItemVO[]) tallySourceData.get(0).getChildVOs();
//			    	//合同主键
 
			    	for (PayBillItemVO item : childrenVO) {
			    		send.pushPayBillToService(item,payVo.getDef2(),payVo.getDef3());
			    	}
				}
			
	 
			
			}
			
		}catch(Exception ex) {
			Logger.init("iufo");
			Logger.error(ex.getMessage(),ex);
			throw new BusinessRuntimeException(ex.getMessage(),ex);
		}finally {
			Logger.init();
		}

	}
	
	
	
	   public static String callUrl(String appuser,String token,String bodyJson,String url,String key) {
		   
		   SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		   factory.setConnectTimeout(3000);
		   factory.setReadTimeout(10000);
		   RestTemplate   template  = new RestTemplate(factory);
 
		   
		   
		   
		   
		   HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_JSON);
		   headers.add("appuser", appuser);
		   headers.add("token", token);
		   if("paymentPlanAndFeedbackInfo".equals(key)) {
				SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
			    headers.add("timestamp", f.format(new Date()));
		   }
		   
		   
		   
		   	Map<String, String> map = new HashMap<String, String>();
		   
		   	  map.put(key, bodyJson);
		 
		  
			HttpEntity<Map<String, String>>   request = new HttpEntity<>(map, headers);
		   
		   
		    
		

		    ResponseEntity<String> response = template.postForEntity( url, request , String.class );
		   
		  return response.getBody();
	   }
	
	
	public List<CtSalePayTermVO> queryCtSalePayterms(String pk_ct_sale)
	  {
		 BaseDAO dao = new BaseDAO();
		 SQLParameter params = new SQLParameter();
		 params.addParam(pk_ct_sale);
		 try {
			Collection<CtSalePayTermVO> rtns =  dao.retrieveByClause(CtSalePayTermVO.class, "pk_ct_sale = ?",params);
			return (List<CtSalePayTermVO>) rtns;
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	  }

}
