package nc.vo.ct.saledaily.entity;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class JsonSignInfo implements Serializable{
	
	
	@NotNull 
	private String signTime;
	
	@Size(max = 50)
	private String ourName;
	
	private List<CtSaleFileJsonVO> ownAuth;
	
	
	@NotNull 
	private List<CtSaleFileJsonVO> contractScanFile;


	public String getSignTime() {
		return signTime;
	}


	public void setSignTime(String signTime) {
		this.signTime = signTime;
	}


	public String getOurName() {
		return ourName;
	}


	public void setOurName(String ourName) {
		this.ourName = ourName;
	}


	public List<CtSaleFileJsonVO> getOwnAuth() {
		return ownAuth;
	}


	public void setOwnAuth(List<CtSaleFileJsonVO> ownAuth) {
		this.ownAuth = ownAuth;
	}


	public List<CtSaleFileJsonVO> getContractScanFile() {
		return contractScanFile;
	}


	public void setContractScanFile(List<CtSaleFileJsonVO> contractScanFile) {
		this.contractScanFile = contractScanFile;
	}
	
	
	
	
	
	
	
	
	
//	"signInfoList":[{
//		"signTime":"签订日期",
//		"ourName":"我方签订人",
//		"ownAuth":[{"filename":"我方授权委托书附件","filepath":"路径","createtime":"","num":1}],
//		"contractScanFile"::[{"filename":"签署文本","filepath":"路径","createtime":"","num":1}]
//		}],


}
