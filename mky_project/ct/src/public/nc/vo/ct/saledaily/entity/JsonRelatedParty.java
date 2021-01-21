package nc.vo.ct.saledaily.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import nc.vo.pub.lang.UFDouble;

public class JsonRelatedParty implements Serializable{
	
	
	@NotNull
	private Integer isRelatedParty;
	private Integer rpType;
	@NotNull
	private Integer isRelatedDeal;
	
	@NotNull
	@Size(max = 50)
	private String dealType;
	
	private Integer isIntertemporal;
	
	private String relatedDealItem;
	
	@Size(max = 50)
	private String intertemporalYear;
	private UFDouble estimateAmount;
	
	private Integer isImportantRelatedDeal;
	
	private Integer isNeedPerfApprove;
	
	private List<JsonIntertemporal>  intertemporalList = new ArrayList<JsonIntertemporal>();

	public Integer getIsRelatedParty() {
		return isRelatedParty;
	}

	public void setIsRelatedParty(Integer isRelatedParty) {
		this.isRelatedParty = isRelatedParty;
	}

	public Integer getRpType() {
		return rpType;
	}

	public void setRpType(Integer rpType) {
		this.rpType = rpType;
	}

	public Integer getIsRelatedDeal() {
		return isRelatedDeal;
	}

	public void setIsRelatedDeal(Integer isRelatedDeal) {
		this.isRelatedDeal = isRelatedDeal;
	}

	public String getDealType() {
		return dealType;
	}

	public void setDealType(String dealType) {
		this.dealType = dealType;
	}

	public Integer getIsIntertemporal() {
		return isIntertemporal;
	}

	public void setIsIntertemporal(Integer isIntertemporal) {
		this.isIntertemporal = isIntertemporal;
	}

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
		this.estimateAmount = estimateAmount;
	}

	public Integer getIsImportantRelatedDeal() {
		return isImportantRelatedDeal;
	}

	public void setIsImportantRelatedDeal(Integer isImportantRelatedDeal) {
		this.isImportantRelatedDeal = isImportantRelatedDeal;
	}

	public Integer getIsNeedPerfApprove() {
		return isNeedPerfApprove;
	}

	public void setIsNeedPerfApprove(Integer isNeedPerfApprove) {
		this.isNeedPerfApprove = isNeedPerfApprove;
	}
	
	public void addJsonIntertemporal(JsonIntertemporal intertemporal) {
		this.intertemporalList.add(intertemporal);
	}

	public String getRelatedDealItem() {
		return relatedDealItem;
	}

	public void setRelatedDealItem(String relatedDealItem) {
		this.relatedDealItem = relatedDealItem;
	}
	
	
	
	
	
	
//	"relatedPartyList":[{
//		"isRelatedParty":"�Ƿ������",
//		"rpType":"����������",
//		"isRelatedDeal":"�Ƿ��������",
//		"dealType":"������������",
//		"isIntertemporal":"�Ƿ���ں�ͬ",
//		"intertemporalYear":"���",
//		"estimateAmount":"Ԥ�����",
//		"isImportantRelatedDeal":"�Ƿ��ش��������",
//		"isNeedPerfApprove":"�Ƿ����й�����������"
//		}]
//		}


}
