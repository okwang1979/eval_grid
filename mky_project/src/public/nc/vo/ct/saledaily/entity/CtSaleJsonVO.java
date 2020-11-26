package nc.vo.ct.saledaily.entity;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Size;

import com.sun.istack.internal.NotNull;

import nc.vo.pub.lang.UFDouble;

public class CtSaleJsonVO implements Serializable{
	//合同唯一标识
	@NotNull
	@Size(max = 50)
	private String contractUniqueId;
	// 合同类型 合同类
	@NotNull
	private Integer contractType;
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
	@NotNull
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
	@NotNull
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
	@NotNull
	@Size(max = 50)
	private List<CtSaleFileJsonVO> contractAttachment;
	@NotNull
	@Size(max = 50)
	private String oppositeUniqueId;
	@NotNull
	@Size(max = 100)
	private String oppositeName;
	@NotNull
	@Size(max = 50)
	private String oppositeRelName;
	@Size(max = 100)
	private String bankOfDeposit;
	@Size(max = 100)
	private String bankAccount;
	@NotNull
	@Size(max = 100)
	private String bankAccountName;
	@NotNull
	private Integer isRelatedParty;
	
	
	//41~50
	private Integer rpType;
	@NotNull
	private Integer isRelatedDeal;
	@NotNull
	@Size(max = 50)
	private String dealType;
 
	private Integer isIntertemporal;
	@Size(max = 50)
	private String intertemporalYear;
 
	private UFDouble estimateAmount;
	 
	private Integer isImportantRelatedDeal;
 
	private Integer isNeedPerfApprove;
	 
	private String sealTime;
	@NotNull
	@Size(max = 50)
	private String sealType;
	
	//51~60
	private Integer signNum;
	@NotNull 
	private String signTime;
	private Integer ownIsAuth;
	private Integer authType;
	private List<CtSaleFileJsonVO> ownAuth;
	@Size(max = 50)
	private String ourName;
	@Size(max = 100)
	private String opptName;
	private Integer opptIsAuth;
	private List<CtSaleFileJsonVO> sealTopptAuthype;
	@NotNull 
	private List<CtSaleFileJsonVO> contractScanFile;
	
	
	
	
	
	
	public Integer getIsNeedPerfApprove() {
		return isNeedPerfApprove;
	}
	public void setIsNeedPerfApprove(Integer isNeedPerfApprove) {
		this.isNeedPerfApprove = isNeedPerfApprove;
	}
	public String getContractUniqueId() {
		return contractUniqueId;
	}
	public void setContractUniqueId(String contractUniqueId) {
		this.contractUniqueId = contractUniqueId;
	}
	public Integer getContractType() {
		return contractType;
	}
	public void setContractType(Integer contractType) {
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
		return oppositeUniqueId;
	}
	public void setOppositeUniqueId(String oppositeUniqueId) {
		this.oppositeUniqueId = oppositeUniqueId;
	}
	public String getOppositeName() {
		return oppositeName;
	}
	public void setOppositeName(String oppositeName) {
		this.oppositeName = oppositeName;
	}
	public String getOppositeRelName() {
		return oppositeRelName;
	}
	public void setOppositeRelName(String oppositeRelName) {
		this.oppositeRelName = oppositeRelName;
	}
	public String getBankOfDeposit() {
		return bankOfDeposit;
	}
	public void setBankOfDeposit(String bankOfDeposit) {
		this.bankOfDeposit = bankOfDeposit;
	}
	public String getBankAccount() {
		return bankAccount;
	}
	public void setBankAccount(String bankAccount) {
		this.bankAccount = bankAccount;
	}
	public String getBankAccountName() {
		return bankAccountName;
	}
	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
	}
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

	public String getSealTime() {
		return sealTime;
	}
	public void setSealTime(String sealTime) {
		this.sealTime = sealTime;
	}
	public String getSealType() {
		return sealType;
	}
	public void setSealType(String sealType) {
		this.sealType = sealType;
	}
	public Integer getSignNum() {
		return signNum;
	}
	public void setSignNum(Integer signNum) {
		this.signNum = signNum;
	}
	public String getSignTime() {
		return signTime;
	}
	public void setSignTime(String signTime) {
		this.signTime = signTime;
	}
	public Integer getOwnIsAuth() {
		return ownIsAuth;
	}
	public void setOwnIsAuth(Integer ownIsAuth) {
		this.ownIsAuth = ownIsAuth;
	}

	
	
	public Integer getAuthType() {
		return authType;
	}
	public void setAuthType(Integer authType) {
		this.authType = authType;
	}
	public List<CtSaleFileJsonVO> getOwnAuth() {
		return ownAuth;
	}
	public void setOwnAuth(List<CtSaleFileJsonVO> ownAuth) {
		this.ownAuth = ownAuth;
	}
	public String getOurName() {
		return ourName;
	}
	public void setOurName(String ourName) {
		this.ourName = ourName;
	}
	public String getOpptName() {
		return opptName;
	}
	public void setOpptName(String opptName) {
		this.opptName = opptName;
	}
	public Integer getOpptIsAuth() {
		return opptIsAuth;
	}
	public void setOpptIsAuth(Integer opptIsAuth) {
		this.opptIsAuth = opptIsAuth;
	}
	public List<CtSaleFileJsonVO> getSealTopptAuthype() {
		return sealTopptAuthype;
	}
	public void setSealTopptAuthype(List<CtSaleFileJsonVO> sealTopptAuthype) {
		this.sealTopptAuthype = sealTopptAuthype;
	}
	public List<CtSaleFileJsonVO> getContractScanFile() {
		return contractScanFile;
	}
	public void setContractScanFile(List<CtSaleFileJsonVO> contractScanFile) {
		this.contractScanFile = contractScanFile;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
