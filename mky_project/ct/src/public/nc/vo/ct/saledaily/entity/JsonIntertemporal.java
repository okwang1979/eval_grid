package nc.vo.ct.saledaily.entity;

import java.io.Serializable;

import nc.vo.pub.lang.UFDouble;

public class JsonIntertemporal implements Serializable{
	
	private String intertemporalYear;
	
	private UFDouble estimateAmount;

	public String getIntertemporalYear() {
		return intertemporalYear;
	}

	public void setIntertemporalYear(String intertemporalYear) {
		this.intertemporalYear = intertemporalYear;
	}

	public UFDouble getEstimateAmount() {
		return estimateAmount;
	}

	public void setEstimateAmount(UFDouble estimateAmount) {
		
		this.estimateAmount = new UFDouble(estimateAmount.doubleValue(),2);
	}
	
	
	

}
