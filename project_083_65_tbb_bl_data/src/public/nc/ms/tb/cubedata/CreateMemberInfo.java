package nc.ms.tb.cubedata;

import java.io.Serializable;

/**
 * @author ��־ǿ
 * ����Member��Ϣ��.
 * 
 * 
 *
 */
public class CreateMemberInfo implements Serializable{
	
	/**
	 * dimlevel��code
	 */
	private String dimLevelCode;
	/**
	 * ��Ӧlevelvalueֵ
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
