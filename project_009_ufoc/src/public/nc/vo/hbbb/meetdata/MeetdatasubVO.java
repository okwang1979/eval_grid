/***************************************************************\
 *     The skeleton of this class is generated by an automatic *
 * code generator for NC product. It is based on Velocity.     *
\***************************************************************/
package nc.vo.hbbb.meetdata;
	
import nc.vo.pub.SuperVO;

/**
 * <b> �ڴ˴���Ҫ��������Ĺ��� </b>
 * <p>
 *     �ڴ˴����Ӵ����������Ϣ
 * </p>
 * ��������:
 * @author 
 * @version NCPrj ??
 */
@SuppressWarnings("serial")
public class MeetdatasubVO extends SuperVO {
	private java.lang.String subvos;
	private java.lang.String pk_meetdatasub;
	private java.lang.String pk_meetdata;
	private java.lang.String pk_measure;
	private nc.vo.pub.lang.UFDouble amount;
	private java.lang.Integer direction;
	private nc.vo.pub.lang.UFBoolean bself;  
	private java.lang.Integer dr = 0;
	private nc.vo.pub.lang.UFDateTime ts;
	
	private String meetNode;

	//���˼�¼intr����Դ����Դ�ı�aloneid��ָ��code����̬���ؼ���pk
	private java.lang.String aloneid;
	private java.lang.String measurecode;
	private java.lang.String pk_opporg;
	//add by jiaah at 2014-4-29
	private java.lang.String pk_selforg;

	public static final String SUBVOS = "subvos";
	public static final String PK_MEETDATASUB = "pk_meetdatasub";
	public static final String PK_MEETDATA = "pk_meetdata";
	public static final String PK_MEASURE = "pk_measure";
	public static final String AMOUNT = "amount";
	public static final String DIRECTION = "direction";
	public static final String BSELF = "bself";
	
	
	
	public String getMeetNode() {
		return meetNode;
	}
	public void setMeetNode(String meetNode) {
		this.meetNode = meetNode;
	}
	/**
	 * ����subvos��Getter����.
	 * ��������:
	 * @return java.lang.String
	 */
	public java.lang.String getSubvos () {
		return subvos;
	}   
	/**
	 * ����subvos��Setter����.
	 * ��������:
	 * @param newSubvos java.lang.String
	 */
	public void setSubvos (java.lang.String newSubvos ) {
	 	this.subvos = newSubvos;
	} 	  
	/**
	 * ����pk_meetdatasub��Getter����.
	 * ��������:
	 * @return java.lang.String
	 */
	public java.lang.String getPk_meetdatasub () {
		return pk_meetdatasub;
	}   
	/**
	 * ����pk_meetdatasub��Setter����.
	 * ��������:
	 * @param newPk_meetdatasub java.lang.String
	 */
	public void setPk_meetdatasub (java.lang.String newPk_meetdatasub ) {
	 	this.pk_meetdatasub = newPk_meetdatasub;
	} 	  
	/**
	 * ����pk_meetdata��Getter����.
	 * ��������:
	 * @return java.lang.String
	 */
	public java.lang.String getPk_meetdata () {
		return pk_meetdata;
	}   
	/**
	 * ����pk_meetdata��Setter����.
	 * ��������:
	 * @param newPk_meetdata java.lang.String
	 */
	public void setPk_meetdata (java.lang.String newPk_meetdata ) {
	 	this.pk_meetdata = newPk_meetdata;
	} 	  
	/**
	 * ����pk_measure��Getter����.
	 * ��������:
	 * @return java.lang.String
	 */
	public java.lang.String getPk_measure () {
		return pk_measure;
	}   
	/**
	 * ����pk_measure��Setter����.
	 * ��������:
	 * @param newPk_measure java.lang.String
	 */
	public void setPk_measure (java.lang.String newPk_measure ) {
	 	this.pk_measure = newPk_measure;
	} 	  
	/**
	 * ����amount��Getter����.
	 * ��������:
	 * @return nc.vo.pub.lang.UFDouble
	 */
	public nc.vo.pub.lang.UFDouble getAmount () {
		return amount;
	}   
	/**
	 * ����amount��Setter����.
	 * ��������:
	 * @param newAmount nc.vo.pub.lang.UFDouble
	 */
	public void setAmount (nc.vo.pub.lang.UFDouble newAmount ) {
	 	this.amount = newAmount;
	} 	  
	/**
	 * ����direction��Getter����.
	 * ��������:
	 * @return java.lang.Integer
	 */
	public java.lang.Integer getDirection () {
		return direction;
	}   
	/**
	 * ����direction��Setter����.
	 * ��������:
	 * @param newDirection java.lang.Integer
	 */
	public void setDirection (java.lang.Integer newDirection ) {
	 	this.direction = newDirection;
	} 	  
	/**
	 * ����bself��Getter����.
	 * ��������:
	 * @return nc.vo.pub.lang.UFBoolean
	 */
	public nc.vo.pub.lang.UFBoolean getBself () {
		return bself;
	}   
	/**
	 * ����bself��Setter����.
	 * ��������:
	 * @param newBself nc.vo.pub.lang.UFBoolean
	 */
	public void setBself (nc.vo.pub.lang.UFBoolean newBself ) {
	 	this.bself = newBself;
	} 	  
	public java.lang.String getAloneid() {
		return aloneid;
	}
	public void setAloneid(java.lang.String aloneid) {
		this.aloneid = aloneid;
	}
	public java.lang.String getMeasurecode() {
		return measurecode;
	}
	public void setMeasurecode(java.lang.String measurecode) {
		this.measurecode = measurecode;
	}
	public java.lang.String getPk_opporg() {
		return pk_opporg;
	}
	public void setPk_opporg(java.lang.String pk_opporg) {
		this.pk_opporg = pk_opporg;
	}
	public java.lang.String getPk_selforg() {
		return pk_selforg;
	}
	public void setPk_selforg(java.lang.String pk_selforg) {
		this.pk_selforg = pk_selforg;
	}
	/**
	 * ����dr��Getter����.
	 * ��������:
	 * @return java.lang.Integer
	 */
	public java.lang.Integer getDr () {
		return dr;
	}   
	/**
	 * ����dr��Setter����.
	 * ��������:
	 * @param newDr java.lang.Integer
	 */
	public void setDr (java.lang.Integer newDr ) {
	 	this.dr = newDr;
	} 	  
	/**
	 * ����ts��Getter����.
	 * ��������:
	 * @return nc.vo.pub.lang.UFDateTime
	 */
	public nc.vo.pub.lang.UFDateTime getTs () {
		return ts;
	}   
	/**
	 * ����ts��Setter����.
	 * ��������:
	 * @param newTs nc.vo.pub.lang.UFDateTime
	 */
	public void setTs (nc.vo.pub.lang.UFDateTime newTs ) {
	 	this.ts = newTs;
	} 	  
 
	/**
	  * <p>ȡ�ø�VO�����ֶ�.
	  * <p>
	  * ��������:
	  * @return java.lang.String
	  */
	public java.lang.String getParentPKFieldName() {
		return "subvos";
	}   
    
	/**
	  * <p>ȡ�ñ�����.
	  * <p>
	  * ��������:
	  * @return java.lang.String
	  */
	public java.lang.String getPKFieldName() {
	  return "pk_meetdatasub";
	}
    
	/**
	 * <p>���ر�����.
	 * <p>
	 * ��������:
	 * @return java.lang.String
	 */
	public java.lang.String getTableName() {
		return "ufoc_meetdatasub";
	}    
	
	/**
	 * <p>���ر�����.
	 * <p>
	 * ��������:
	 * @return java.lang.String
	 */
	public static java.lang.String getDefaultTableName() {
		return "ufoc_meetdatasub";
	}    
    
    /**
	  * ����Ĭ�Ϸ�ʽ����������.
	  *
	  * ��������:
	  */
     public MeetdatasubVO() {
		super();	
	}    
} 

