package nccloud.web.ct.saledaily.action;

/**
 * @author ��־ǿ,url������װ��������Զ��,���ƺ���
 *
 */
public class SaleUrlConst {
	
	
	private  String restLogin = "http://pv.sohu.com/cityjson";
	
	private String registerContractInfo;
	
	
//	private String 
	
	public static SaleUrlConst getUrlConst() {
		SaleUrlConst rtn = new SaleUrlConst();
		rtn.setRestLogin("http://pv.sohu.com/cityjson");
		rtn.setRegisterContractInfo("http://172.18.102.210:8888/rest/registerContractInfo");
		return rtn;
		
		
	}
	
	
	
	
	
	
	public String getRegisterContractInfo() {
		return registerContractInfo;
	}






	public void setRegisterContractInfo(String registerContractInfo) {
		this.registerContractInfo = registerContractInfo;
	}






	private SaleUrlConst() {
		
	}

	public String getRestLogin() {
		return restLogin;
	}

	public void setRestLogin(String restLogin) {
		this.restLogin = restLogin;
		
	}

}
