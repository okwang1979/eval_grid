package nc.vo.ct.saledaily.entity;

import java.io.Serializable;
import java.util.List;

public class JsonSignItem implements Serializable{
	
	
	private String oppositeCode;
	private String opptName;
	private Integer opptIsAuth=0;
	private List<CtSaleFileJsonVO> opptAuth;
	
	public String getOppositeCode() {
		return oppositeCode;
	}
	public void setOppositeCode(String oppositeCode) {
		this.oppositeCode = oppositeCode;
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
	public List<CtSaleFileJsonVO> getOpptAuth() {
		return opptAuth;
	}
	public void setOpptAuth(List<CtSaleFileJsonVO> opptAuth) {
		this.opptAuth = opptAuth;
	}
	
	
	
	
	
//	"signItemList":[{
//		"oppositeCode":"相对方唯一标识",
//		"opptName":"对方签订人",
//		"opptIsAuth":"对方是否授权",
//		"opptAuth":[{"filename":"对方授权委托书附件","filepath":"路径","createtime":"","num":1}]
//		}],


}
