package nc.vo.ct.saledaily.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import javax.validation.constraints.NotNull;

import nc.vo.pub.lang.UFDouble;

public class CtSaleJsonVO implements Serializable{
	//合同唯一标识
	@NotNull
	@Size(max = 50)
	private String contractUniqueId;
	// 合同类型 合同类
	@NotNull
	private String contractType;
	@NotNull
	@Size(max = 1000)
	private String contractSubject;
	@NotNull
	@Size(max = 200)
	private String contractName;
	@NotNull
	@Size(max = 100)
	private String contractSelfCode;
	@Size(max = 200)
	private String relatedProjectName;
	
	@Size(max = 200)
	private String relatedProjectCode;

	@Size(max = 200)
	private Integer buyMethod;
	//中标通知书
	private List<CtSaleFileJsonVO> bidFile;
	@NotNull
	@Size(max = 20)
	private String contractAmount;
	@NotNull
	private Integer valuationMode;
	
	//11~20
	@NotNull
	@Size(max = 50)
	private String currencyName;
	@Size(max = 50)
	private String exchangeRate;
	@NotNull
	@Size(max = 500)
	private String amountExplain;
	@NotNull
	private Integer paymentDirection;
 	//文档需要整数,但是支持多选这里改成String每个用|分隔表示多选,后续可能改成数组或在List
	@NotNull
	private String paymentType;
	@Size(max = 50)
	@NotNull
	private String paymentMethod;
	
	private Integer isAdvancePayment;	
	@NotNull
	@Size(max = 200)
	private String signingSubject;
	@NotNull
	@Size(max = 100)
	private String signingSubjectCode;
	@NotNull
	@Size(max = 50)
	private String creatorAccount;
	
	//21~30
	@NotNull
	@Size(max = 50)
	private String creatorName;
	@NotNull
	@Size(max = 50)
	private String creatorDeptCode;
	@NotNull
	@Size(max = 50)
	private String creatorDeptName;
	@NotNull
	@Size(max = 50)
	private String performAddress;
	@NotNull
	@Size(max = 50)
	private String signAddress;
	@NotNull
	private Integer contractPeriod;
	@Size(max = 50)
	private String performPeriod;
	@Size(max = 100)
	private String periodExplain;
	@Size(max = 500)
	private String contractContent;
	@NotNull
	private List<CtSaleFileJsonVO> contractText ;
 
	//31~40
	private  List<CtSaleFileJsonVO> contractGist;
	@NotNull
	private  List<CtSaleFileJsonVO>  contractApprovalForm;	

	private List<CtSaleFileJsonVO> contractAttachment;
//	@NotNull
//	@Size(max = 50)
//	private String oppositeUniqueId;
//	@NotNull
//	@Size(max = 100)
//	private String oppositeName;
//	@NotNull
//	@Size(max = 50)
//	private String oppositeRelName;
//	@Size(max = 100)
//	private String bankOfDeposit;
//	@Size(max = 100)
//	private String bankAccount;
//	@NotNull
//	@Size(max = 100)
//	private String bankAccountName;
	
	private List<RelOppositeInfo> relOppositeInfoList = new ArrayList<RelOppositeInfo>();
	

	
	
	//41~50


	private  JsonRelatedParty relatedParty = new JsonRelatedParty();
 


 

	 

 

	 
	
	private List<SealJsonInfo> sealInfoList = new ArrayList<SealJsonInfo>();
//	private String sealTime;
//	@NotNull
//	@Size(max = 50)
//	private String sealType;
//	
//	//51~60
//	private Integer signNum;

	private Integer ourIsAuth;
	private Integer authType;

	private List<JsonSignInfo>  signInfoList = new ArrayList<JsonSignInfo>();
	
	private List<JsonSignItem>  signItemList = new ArrayList<JsonSignItem>();
	
//	private String oppositeCode;
//	@Size(max = 100)
//	private String opptName;
//	private Integer opptIsAuth;
//	private List<CtSaleFileJsonVO> opptAuth;
	
	//oppositeCode

	
	
	
	
	
	public	CtSaleJsonVO() {
		relOppositeInfoList.add(new RelOppositeInfo());
		sealInfoList.add(new SealJsonInfo() );
		signInfoList.add(new JsonSignInfo());
		signItemList.add(new JsonSignItem());
		 
	}
	
	
	public Integer getIsNeedPerfApprove() {
		return relatedParty.getIsNeedPerfApprove();
	}
	public void setIsNeedPerfApprove(Integer isNeedPerfApprove) {
		relatedParty.setIsNeedPerfApprove(isNeedPerfApprove);
	}
	public String getContractUniqueId() {
		return contractUniqueId;
	}
	public void setContractUniqueId(String contractUniqueId) {
		this.contractUniqueId = contractUniqueId;
	}
	
	
	
	public String getContractType() {
		return contractType;
	}


	public void setContractType(String contractType) {
		this.contractType = contractType;
	}


	public String getContractSubject() {
		return contractSubject;
	}
	public void setContractSubject(String contractSubject) {
		this.contractSubject = contractSubject;
	}
	public String getContractName() {
		return contractName;
	}
	public void setContractName(String contractName) {
		this.contractName = contractName;
	}
	public String getContractSelfCode() {
		return contractSelfCode;
	}
	public void setContractSelfCode(String contractSelfCode) {
		this.contractSelfCode = contractSelfCode;
	}
	public String getRelatedProjectName() {
		return relatedProjectName;
	}
	public void setRelatedProjectName(String relatedProjectName) {
		this.relatedProjectName = relatedProjectName;
	}
	public String getRelatedProjectCode() {
		return relatedProjectCode;
	}
	public void setRelatedProjectCode(String relatedProjectCode) {
		this.relatedProjectCode = relatedProjectCode;
	}
	public Integer getBuyMethod() {
		return buyMethod;
	}
	public void setBuyMethod(Integer buyMethod) {
		this.buyMethod = buyMethod;
	}
	public List<CtSaleFileJsonVO> getBidFile() {
		return bidFile;
	}
	public void setBidFile(List<CtSaleFileJsonVO> bidFile) {
		this.bidFile = bidFile;
	}
	public String getContractAmount() {
		return contractAmount;
	}
	public void setContractAmount(String contractAmount) {
		this.contractAmount = contractAmount;
	}
	public Integer getValuationMode() {
		return valuationMode;
	}
	public void setValuationMode(Integer valuationMode) {
		this.valuationMode = valuationMode;
	}
	public String getCurrencyName() {
		return currencyName;
	}
	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}
	public String getExchangeRate() {
		return exchangeRate;
	}
	public void setExchangeRate(String exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
	public String getAmountExplain() {
		return amountExplain;
	}
	public void setAmountExplain(String amountExplain) {
		this.amountExplain = amountExplain;
	}
	public Integer getPaymentDirection() {
		return paymentDirection;
	}
	public void setPaymentDirection(Integer paymentDirection) {
		this.paymentDirection = paymentDirection;
	}

	
	
	public String getPaymentType() {
		return paymentType;
	}


	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}


	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public Integer getIsAdvancePayment() {
		return isAdvancePayment;
	}
	public void setIsAdvancePayment(Integer isAdvancePayment) {
		this.isAdvancePayment = isAdvancePayment;
	}
	public String getSigningSubject() {
		return signingSubject;
	}
	public void setSigningSubject(String signingSubject) {
		this.signingSubject = signingSubject;
	}
	public String getSigningSubjectCode() {
		return signingSubjectCode;
	}
	public void setSigningSubjectCode(String signingSubjectCode) {
		this.signingSubjectCode = signingSubjectCode;
	}
	public String getCreatorAccount() {
		return creatorAccount;
	}
	public void setCreatorAccount(String creatorAccount) {
		this.creatorAccount = creatorAccount;
	}
	public String getCreatorName() {
		return creatorName;
	}
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	public String getCreatorDeptCode() {
		return creatorDeptCode;
	}
	public void setCreatorDeptCode(String creatorDeptCode) {
		this.creatorDeptCode = creatorDeptCode;
	}
	public String getCreatorDeptName() {
		return creatorDeptName;
	}
	public void setCreatorDeptName(String creatorDeptName) {
		this.creatorDeptName = creatorDeptName;
	}
	public String getPerformAddress() {
		return performAddress;
	}
	public void setPerformAddress(String performAddress) {
		this.performAddress = performAddress;
	}
	public String getSignAddress() {
		return signAddress;
	}
	public void setSignAddress(String signAddress) {
		this.signAddress = signAddress;
	}
	public Integer getContractPeriod() {
		return contractPeriod;
	}
	public void setContractPeriod(Integer contractPeriod) {
		this.contractPeriod = contractPeriod;
	}
	public String getPerformPeriod() {
		return performPeriod;
	}
	public void setPerformPeriod(String performPeriod) {
		this.performPeriod = performPeriod;
	}
	public String getPeriodExplain() {
		return periodExplain;
	}
	public void setPeriodExplain(String periodExplain) {
		this.periodExplain = periodExplain;
	}
	public String getContractContent() {
		return contractContent;
	}
	public void setContractContent(String contractContent) {
		this.contractContent = contractContent;
	}
	public List<CtSaleFileJsonVO> getContractText() {
		return contractText;
	}
	public void setContractText(List<CtSaleFileJsonVO> contractText) {
		this.contractText = contractText;
	}
	public List<CtSaleFileJsonVO> getContractGist() {
		return contractGist;
	}
	public void setContractGist(List<CtSaleFileJsonVO> contractGist) {
		this.contractGist = contractGist;
	}
	public List<CtSaleFileJsonVO> getContractApprovalForm() {
		return contractApprovalForm;
	}
	public void setContractApprovalForm(List<CtSaleFileJsonVO> contractApprovalForm) {
		this.contractApprovalForm = contractApprovalForm;
	}
	public List<CtSaleFileJsonVO> getContractAttachment() {
		return contractAttachment;
	}
	public void setContractAttachment(List<CtSaleFileJsonVO> contractAttachment) {
		this.contractAttachment = contractAttachment;
	}
	public String getOppositeUniqueId() {
		return relOppositeInfoList.get(0).getOppositeUniqueId();
	}
	public void setOppositeUniqueId(String oppositeUniqueId) {
		relOppositeInfoList.get(0).setOppositeUniqueId(oppositeUniqueId);
//		this.oppositeUniqueId = oppositeUniqueId;
	}
	public String getOppositeName() {
		return relOppositeInfoList.get(0).getOppositeName();
	}
	public void setOppositeName(String oppositeName) {
		relOppositeInfoList.get(0).setOppositeName( oppositeName);
	}
	public String getOppositeRelName() {
		return relOppositeInfoList.get(0).getOppositeRelName();
	}
	public void setOppositeRelName(String oppositeRelName) {
		relOppositeInfoList.get(0).setOppositeRelName( oppositeRelName);
	}
	public String getBankOfDeposit() {
		return relOppositeInfoList.get(0).getBankOfDeposit();
	}
	public void setBankOfDeposit(String bankOfDeposit) {
		relOppositeInfoList.get(0).setBankOfDeposit( bankOfDeposit);
	}
	public String getBankAccount() {
		return relOppositeInfoList.get(0).getBankAccount();
	}
	public void setBankAccount(String bankAccount) {
		relOppositeInfoList.get(0).setBankAccount( bankAccount);
	}
	public String getBankAccountName() {
		return relOppositeInfoList.get(0).getBankAccountName();
	}
	public void setBankAccountName(String bankAccountName) {
		relOppositeInfoList.get(0).setBankAccountName(bankAccountName);
	}
	public Integer getIsRelatedParty() {
		return relatedParty.getIsRelatedParty();
	}
	public void setIsRelatedParty(Integer isRelatedParty) {
		relatedParty.setIsRelatedParty(isRelatedParty);
	}
	public Integer getRpType() {
		return relatedParty.getRpType();
	}
	public void setRpType(Integer rpType) {
		relatedParty.setRpType(rpType);
	}
	public Integer getIsRelatedDeal() {
		return relatedParty.getIsRelatedDeal();
	}
	public void setIsRelatedDeal(Integer isRelatedDeal) {
		relatedParty.setIsRelatedDeal(isRelatedDeal);
	}
	public String getDealType() {
		return relatedParty.getDealType();
	}
	public void setDealType(String dealType) {
		relatedParty.setDealType(dealType);
	}
	public Integer getIsIntertemporal() {
		return relatedParty.getIsIntertemporal();
	}
	public void setIsIntertemporal(Integer isIntertemporal) {
		relatedParty.setIsIntertemporal(isIntertemporal);
	}
	public String getIntertemporalYear() {
		return relatedParty.getIntertemporalYear();
	}
	public void addIntertemporal(JsonIntertemporal jsonObj) {
		relatedParty.addJsonIntertemporal(jsonObj);
		
	}
	public void setIntertemporalYear(String intertemporalYear) {
		relatedParty.setIntertemporalYear(intertemporalYear);
	}
	public UFDouble getEstimateAmount() {
		return relatedParty.getEstimateAmount();
	}
	public void setEstimateAmount(UFDouble estimateAmount) {
		relatedParty.setEstimateAmount(estimateAmount);
	}
	public Integer getIsImportantRelatedDeal() {
		return relatedParty.getIsImportantRelatedDeal();
	}
	public void setIsImportantRelatedDeal(Integer isImportantRelatedDeal) {
		relatedParty.setIsImportantRelatedDeal(isImportantRelatedDeal);
	}

	public String getSealTime() {
		return sealInfoList.get(0).getSealTime();
	}
	public void setSealTime(String sealTime) {
		sealInfoList.get(0).setSealTime(sealTime);
	}
	public String getSealType() {
		return sealInfoList.get(0).getSealType();
	}
	public void setSealType(String sealType) {
		sealInfoList.get(0).setSealType(sealType);
	}
	public Integer getSignNum() {
		return sealInfoList.get(0).getSignNum();
	}
	public void setSignNum(Integer signNum) {
		sealInfoList.get(0).setSignNum(signNum);
	}
	public String getSignTime() {
		return signInfoList.get(0).getSignTime();
	}
	public void setSignTime(String signTime) {
		signInfoList.get(0).setSignTime(signTime); 
	}
 
	
	

	
	
	public Integer getOurIsAuth() {
		return ourIsAuth;
	}
	public void setOurIsAuth(Integer ourIsAuth) {
		this.ourIsAuth = ourIsAuth;
	}
	public Integer getAuthType() {
		return authType;
	}
	public void setAuthType(Integer authType) {
		this.authType = authType;
	}
	public List<CtSaleFileJsonVO> getOwnAuth() {
		return signInfoList.get(0).getOwnAuth();
	}
	public void setOwnAuth(List<CtSaleFileJsonVO> ownAuth) {
		signInfoList.get(0).setOwnAuth(ownAuth);
	}
	public String getOurName() {
		return signInfoList.get(0).getOurName();
	}
	public void setOurName(String ourName) {
		signInfoList.get(0).setOurName(ourName);
	}
	public String getOpptName() {
		return signItemList.get(0).getOpptName();
	}
	public void setOpptName(String opptName) {
		signItemList.get(0).setOpptName(opptName); 
	}
	public Integer getOpptIsAuth() {
		return signItemList.get(0).getOpptIsAuth();
	}
	public void setOpptIsAuth(Integer opptIsAuth) {
		signItemList.get(0).setOpptIsAuth(opptIsAuth);
	}
 
	
	
	
	
	
	public List<CtSaleFileJsonVO> getOpptAuth() {
		return signItemList.get(0).getOpptAuth();
	}
	public void setOpptAuth(List<CtSaleFileJsonVO> opptAuth) {
		signItemList.get(0).setOpptAuth(opptAuth);
	}
	
	
	
	
	public String getOppositeCode() {
		return signItemList.get(0).getOppositeCode();
	}


	public void setOppositeCode(String oppositeCode) {
		signItemList.get(0).setOppositeCode(oppositeCode);
	}


	public List<CtSaleFileJsonVO> getContractScanFile() {
		return signInfoList.get(0).getContractScanFile();
	}
	public void setContractScanFile(List<CtSaleFileJsonVO> contractScanFile) {
		signInfoList.get(0).setContractScanFile(contractScanFile);
	}


	public void setRelatedDealItem(String vdef26) {
		relatedParty.setRelatedDealItem(vdef26);
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
