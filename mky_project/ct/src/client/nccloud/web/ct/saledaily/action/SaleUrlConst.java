package nccloud.web.ct.saledaily.action;

/**
 * @author ��־ǿ,url������װ��������Զ��,���ƺ���
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
		//��֤
		rtn.setRestLogin("http://172.18.102.210:8888/rest/login");
		//����/�ɹ���ͬ
		rtn.setRegisterContractInfo("http://172.18.102.210:8888/rest/registerContractInfo");
		//�տ
		rtn.setReceiptBillInfo("http://172.18.102.210:8888/rest/registerPaymentPlanAndFeedbackInfo");
		//���
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
