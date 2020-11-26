package nccloud.web.ct.saledaily.action;

/**
 * @author 王志强,url常量封装后续调用远程,完善函数
 *
 */
public class SaleUrlConst {
	
	
	private  String restLogin = "http://pv.sohu.com/cityjson";
	
	private String registerContractInfo;
	
	private String receiptBillInfo;
	
	private String payBillInfo;
	
	
//	private String 
	
	public static SaleUrlConst getUrlConst() {
		SaleUrlConst rtn = new SaleUrlConst();
		//验证
		rtn.setRestLogin("http://172.18.102.210:8888/rest/login");
		//销售/采购合同
		rtn.setRegisterContractInfo("http://172.18.102.210:8888/rest/registerContractInfo");
		//收款单
		rtn.setReceiptBillInfo("http://172.18.102.210:8888/rest/registerPaymentPlanAndFeedbackInfo");
		//付款单
		rtn.setPayBillInfo("http://172.18.102.210:8888/rest/registerPaymentPlanAndFeedbackInfo");
		
		return rtn;
	}
	
	
	
	
	
	
	private void setReceiptBillInfo(String receiptBillInfo) {
		
		this.receiptBillInfo = receiptBillInfo;
	
   }

	public String getReceiptBillInfo() {
		return receiptBillInfo;
	}
	
	
	
	private void setPayBillInfo(String payBillInfo) {
		
		this.payBillInfo = payBillInfo;
		
	}
	
	public String getPayBillInfo() {
		return payBillInfo;
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
