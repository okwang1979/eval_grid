package nccloud.web.arap.arappub.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import nc.bs.logging.Logger;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.arap.receivable.AggReceivableBillVO;
import nc.vo.ct.saledaily.entity.JsonReceivableVO;
import nc.vo.ct.saledaily.entity.SaleConst;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.core.json.IJson;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.json.JsonFactory;

public class SendRecbillAction {

	public void setAggReceivableBillVO(AggReceivableBillVO vo) {

		TokenInfo tInfo = null;
		try {
			Logger.init("iufo");
			tInfo = restLogin(SaleConst.getAPP_USER(), SaleConst.getSECRE_KEY(),
					SaleConst.getIP_PORINT() + "/rest/login");
			if (!"200".equals(tInfo.getCode())) {
				ExceptionUtils.wrapBusinessException(tInfo.getMessage());
			}

			ISendSaleServer service = (ISendSaleServer) ServiceLocator.find(ISendSaleServer.class);
			JsonReceivableVO jsonVo = service.pusReceivable(vo);
			IJson json = JsonFactory.create();
			String jsonStr = json.toJson(jsonVo);

			Logger.init("iufo");
			Logger.error(jsonStr);
			String rtn = callUrl(SaleConst.getAPP_USER(), tInfo.getToken(), jsonStr,
					SaleConst.getIP_PORINT() + "/rest/registerIncomeInfo",
					"registerIncomeInfo");
			
			TokenInfo info =  (TokenInfo)json.fromJson(rtn, TokenInfo.class);
		     if(!"200".equals(info.getCode())) {
		      ExceptionUtils.wrapBusinessException(info.getMessage());
		     }
		} catch (Exception e) {
			Logger.init("iufo");
			Logger.error(e);
			ExceptionUtils.wrapException(e);
		} finally {
			Logger.init();
		}

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

	private static TokenInfo restLogin(String appUser, String secretKey, String url) throws Exception {

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

	private static TokenInfo getToken(String jsonStr) {
		IJson json = JsonFactory.create();
		TokenInfo info = (TokenInfo) json.fromJson(jsonStr, TokenInfo.class);
		return info;
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