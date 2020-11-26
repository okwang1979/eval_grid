package nc.vo.ct.saledaily.entity;

import java.util.List;

import nc.vo.pub.lang.UFDouble;

public class JsonComeInfo {
	
	private String incomeId;
	
	private UFDouble incomeAmount;
	
	private List<CtSaleFileJsonVO> assistEvidence;
	
	private UFDouble currentPeriodAmount;

	public String getIncomeId() {
		return incomeId;
	}

	public void setIncomeId(String incomeId) {
		this.incomeId = incomeId;
	}

 
	
	

	public UFDouble getIncomeAmount() {
		return incomeAmount;
	}

	public void setIncomeAmount(UFDouble incomeAmount) {
		this.incomeAmount = incomeAmount;
	}

	public List<CtSaleFileJsonVO> getAssistEvidence() {
		return assistEvidence;
	}

	public void setAssistEvidence(List<CtSaleFileJsonVO> assistEvidence) {
		this.assistEvidence = assistEvidence;
	}

	public UFDouble getCurrentPeriodAmount() {
		return currentPeriodAmount;
	}

	public void setCurrentPeriodAmount(UFDouble currentPeriodAmount) {
		this.currentPeriodAmount = currentPeriodAmount;
	}
	
	
	
	
	
	

}
