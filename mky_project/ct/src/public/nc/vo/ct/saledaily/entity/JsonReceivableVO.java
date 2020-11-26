package nc.vo.ct.saledaily.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nc.vo.pub.lang.UFDouble;

/**
 * @author ”¶ ’µ•json
 *
 */
public class JsonReceivableVO implements Serializable{
	
	
	private String contractUniqueId;
	

	
	
	private List<JsonComeInfo>   incomeInfoList = new ArrayList<JsonComeInfo>();
	
	private UFDouble incomeTotalAmount;

	public String getContractUniqueId() {
		return contractUniqueId;
	}

	public void setContractUniqueId(String contractUniqueId) {
		this.contractUniqueId = contractUniqueId;
	}

 

	public UFDouble getIncomeTotalAmount() {
		return incomeTotalAmount;
	}

	public void setIncomeTotalAmount(UFDouble incomeTotalAmount) {
		this.incomeTotalAmount = incomeTotalAmount;
	}

	public List<JsonComeInfo> getIncomeInfoList() {
		return incomeInfoList;
	}

	public void setIncomeInfoList(List<JsonComeInfo> incomeInfoList) {
		this.incomeInfoList = incomeInfoList;
	}
	
	
	
	
	
	
	
	
	

}
