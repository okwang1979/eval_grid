package test.nc.tbb.cubedata;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.TestCase;

public class TestCubeDataPfxxPlugin extends TestCase{
	
	
	public void testCallService(){
		
		
		OutputStream os =null;
		InputStream is = null;
		 HttpURLConnection conn=null;
		try{
			  //服务的地址
	        URL wsUrl = new URL("http://192.168.1.100:6789/hello");
	        
	         conn = (HttpURLConnection) wsUrl.openConnection();
	        
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
	        
	         os = conn.getOutputStream();
	        
	        //请求体
	        String soap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:q0=\"http://ws.itcast.cn/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + 
	                      "<soapenv:Body> <q0:sayHello><arg0>aaa</arg0>  </q0:sayHello> </soapenv:Body> </soapenv:Envelope>";
	        
	        os.write(soap.getBytes());
	        
	         is = conn.getInputStream();
	        
	        byte[] b = new byte[1024];
	        int len = 0;
	        String s = "";
	        while((len = is.read(b)) != -1){
	            String ss = new String(b,0,len,"UTF-8");
	            s += ss;
	        }
	        System.out.println(s);
		}catch(Exception ex){
			
		}finally{
	 
			if(os!=null){
				 try{
					 os.close();
				 }catch(Exception ex){
					 
				 }
					
				 
				 
			}
			  if(is!=null){
				  try{
					  is.close();
				  }catch(Exception ex){
					  
				  }
				  
			  }
		    
			  if(conn!=null)
		      conn.disconnect();
		}
	
        
      
	}

}
