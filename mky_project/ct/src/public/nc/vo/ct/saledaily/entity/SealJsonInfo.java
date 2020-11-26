package nc.vo.ct.saledaily.entity;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SealJsonInfo implements Serializable{
	
	
	private String sealTime;
	
	@NotNull
	@Size(max = 50)
	private String sealType;
	
	
	@NotNull 
	private Integer signNum;


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


	 

 
	
	
	
	
//	"sealTime":"用印日期",
//	"sealType":"用印类型",
//	"signNum":"用印份数",

	
	

}
