package nc.vo.ct.saledaily.entity;

import java.util.ArrayList;
import java.util.List;

import nc.vo.pub.lang.UFDouble;

public class JsonComeInfo {
	
	private String incomeId;
	
	private String incomeAmount;
	
	private List<CtSaleFileJsonVO> assistEvidence =new ArrayList<CtSaleFileJsonVO>();
	
	private String currentPeriodAmount;

	public String getIncomeId() {
		return incomeId;
	}

	public void setIncomeId(String incomeId) {
		this.incomeId = incomeId;
	}

 
	
	

	public String getIncomeAmount() {
		return incomeAmount;
	}

	public void setIncomeAmount(String incomeAmount) {
		this.incomeAmount = incomeAmount;
	}

	public List<CtSaleFileJsonVO> getAssistEvidence() {
		return assistEvidence;
	}

	public void setAssistEvidence(List<CtSaleFileJsonVO> assistEvidence) {
		this.assistEvidence = assistEvidence;
	}

	public String getCurrentPeriodAmount() {
		return currentPeriodAmount;
	}

	public void setCurrentPeriodAmount(String currentPeriodAmount) {
		this.currentPeriodAmount = currentPeriodAmount;
	}
	
	
	
	
	
	

}
