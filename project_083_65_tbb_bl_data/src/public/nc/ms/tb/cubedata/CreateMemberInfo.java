package nc.ms.tb.cubedata;

import java.io.Serializable;

/**
 * @author 王志强
 * 生成Member信息类.
 * 
 * 
 *
 */
public class CreateMemberInfo implements Serializable{
	
	/**
	 * dimlevel的code
	 */
	private String dimLevelCode;
	/**
	 * 对应levelvalue值
	 */
	private String levelValue;
	
	
	public CreateMemberInfo(String dimLevelCode,String levelValue){
		this.dimLevelCode = dimLevelCode;
		this.levelValue = levelValue;
	}
	
	
	public String getDimLevelCode() {
		return dimLevelCode;
	}
	public void setDimLevelCode(String dimLevelCode) {
		this.dimLevelCode = dimLevelCode;
	}
	public String getLevelValue() {
		return levelValue;
	}
	public void setLevelValue(String levelValue) {
		this.levelValue = levelValue;
	}
	
	
	

}
