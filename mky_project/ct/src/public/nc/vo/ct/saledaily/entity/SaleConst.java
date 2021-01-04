package nc.vo.ct.saledaily.entity;

import nc.bs.framework.common.NCLocator;
import nc.itf.ct.sendsale.ISendSaleServer;
import nccloud.framework.service.ServiceLocator;

/**
 * @author 王志强
 * 煤科院接口常量接口
 *
 */
public class SaleConst {
	
	
	
	private static String APP_USER = "KGJN";
	
	private  static String SECRE_KEY="OXpXfaLG5v0LZedTEi2F2WcnGQmPoi5n0m+srzE1kmE=";
	
	private static String IP_PORINT;

	public static String getAPP_USER() {
		return APP_USER;
	}

	public static String getSECRE_KEY() {
		return SECRE_KEY;
	}

	public static String getIP_PORINT() {
		if(IP_PORINT==null) {
			ISendSaleServer service = NCLocator.getInstance().lookup(ISendSaleServer.class);
			String url = service.getSendUrl();
			IP_PORINT = url;
		}
		return IP_PORINT;
	}
	
	

 
	
	
	

}
