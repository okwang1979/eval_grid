package nccloud.web.ct.saledaily.action;

import nc.itf.ct.sendsale.ISendSaleServer;
import nccloud.framework.service.ServiceLocator;

/**
 * @author ��־ǿ,url������װ��������Զ��,���ƺ���
 *
 */
public class SaleUrlConst {
	
	
	private  String restLogin ;
	
	private String registerContractInfo;
	
	private String receiptBillInfo;
	
	private String payBillInfo;
	
	
	private String registerIncomeInfo;
	
//	private String 
	
	public static SaleUrlConst getUrlConst() {
		
		
		
		SaleUrlConst rtn = new SaleUrlConst();
		
		ISendSaleServer service = (ISendSaleServer) ServiceLocator.find(ISendSaleServer.class);
		String url = service.getSendUrl();
		
		//��֤
		rtn.setRestLogin(url+"/rest/login");
		//����/�ɹ���ͬ
		rtn.setRegisterContractInfo(url+"/rest/registerContractInfo");
		//�տ
		rtn.setReceiptBillInfo(url+"/rest/registerPaymentPlanAndFeedbackInfo");
		//���
		rtn.setPayBillInfo(url+"/rest/registerPaymentPlanAndFeedbackInfo");
		
		rtn.setRegisterIncomeInfo(url+"/rest/registerIncomeInfo");
		
		
		return rtn;
	}
	
	
	
	
	
	
	
	
	
	
	public String getRegisterIncomeInfo() {
		return registerIncomeInfo;
	}










	public void setRegisterIncomeInfo(String registerIncomeInfo) {
		this.registerIncomeInfo = registerIncomeInfo;
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
