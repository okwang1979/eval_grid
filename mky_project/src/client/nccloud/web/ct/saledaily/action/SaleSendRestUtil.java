package nccloud.web.ct.saledaily.action;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import nccloud.framework.core.json.IJson;
import nccloud.framework.web.json.JsonFactory;



public class SaleSendRestUtil {
	
	
	 
	 
	 
	
	 
	 
	   public static TokenInfo restLogin(String appUser,String secretKey,String url) throws Exception {
		   
		   
		   SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		   factory.setConnectTimeout(3000);
		   factory.setReadTimeout(10000);
		   RestTemplate   template  = new RestTemplate(factory);
		   HttpHeaders headers = new HttpHeaders();
		   headers.add("appuser", appUser);
		   headers.add("secretkey", secretKey);
		 
		   
		   
		   
		   
		   
		   
		    
//		    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		    MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
//		    map.add("email", "844072586@qq.com");
		 
		    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		    ResponseEntity<String> response = template.postForEntity( url, request , String.class );
		   String json =   response.getBody();
//		   
//	    
	        return getToken(json);
	    }
	   
	   public static String registerContractInfo(String appuser,String token,String bodyJson,String url) {
		   
		   
		   
		   SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		   factory.setConnectTimeout(3000);
		   factory.setReadTimeout(10000);
		   RestTemplate   template  = new RestTemplate(factory);
 
		   
		   
		   
		   
		   HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_JSON);
		   headers.add("appuser", appuser);
		   headers.add("token", token);
		 
		   
		   
		   
		   
		   
		   
		    
		
		    MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
		    map.add("contractInfo", bodyJson);;
		 
		    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		    ResponseEntity<String> response = template.postForEntity( url, request , String.class );
		   
		  return response.getBody();
		  
 
		   
	   }
	   
	   
	   public static TokenInfo restLogin() {
		   
	
		   
		   return null;
	   }
	   
	   
	   
	   public static TokenInfo getToken(String jsonStr) {
//		   JSONObject json = new JSONObject(JsonInfo);
			IJson json = JsonFactory.create();
			TokenInfo info =  (TokenInfo)json.fromJson(jsonStr, TokenInfo.class);
			return info;
//			return info.
	   }
	   
	   
	   
//	   public static String restLogin(String jsonStr,String appUser) throws Exception {
//		   
//	        HttpClient httpClient = new DefaultHttpClient();
//	        ObjectMapper mapper = new ObjectMapper();
//	        HttpPost request = new HttpPost(REST_LOGIN );
//	        request.setHeader("Content-Type", "application/json");
//	        request.setHeader("Accept", "application/json");
//	        
//	        request.setHeader("appuser", appUser);
//	        
//	        StringEntity requestJson = new StringEntity(jsonStr, "utf-8");
//	        requestJson.setContentType("application/json");
//	        request.setEntity(requestJson);
//	        HttpResponse response = httpClient.execute(request);
//	        String json = EntityUtils.toString(response.getEntity());
//	        return json
//	    }
	   
	   
	   
//	   public static String rest

//	    public static void getAllResource() throws Exception {
//	        HttpClient httpClient = new DefaultHttpClient();
//	        HttpGet request = new HttpGet(REST_API + "/getAllResource");
//	        request.setHeader("Content-Type", "application/json");
//	        request.setHeader("Accept", "application/json");
//	        HttpResponse response = httpClient.execute(request);
//	        String json = EntityUtils.toString(response.getEntity());
//	        System.out.print("getAllResource result is : " + json + "\n");
//	    }



}






 


 
   

 

   

 