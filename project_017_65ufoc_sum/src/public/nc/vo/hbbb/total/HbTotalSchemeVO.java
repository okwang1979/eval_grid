package nc.vo.hbbb.total;

import nc.vo.pub.SuperVO;

/**
 * 
 * ���ܷ�����
 *
 */
public class HbTotalSchemeVO extends SuperVO {
	
		/**
		 * ����
		 */
		private java.lang.String pk_hbscheme ;
		/**
		 * ��������pk
		 */
		private String app_org;
		/**
		 * �ϲ�������֯��ϵ
		 */
		private java.lang.String pk_rms;
		/**
		 * �汾
		 */
		private java.lang.String pk_rmsversion;
		/**
		 * û�ã�����
		 */
		private java.lang.String pk_org;
		/**
		 * û�ã�����
		 */
		private java.lang.String pk_group;
 
		private java.lang.String creator;
		private nc.vo.pub.lang.UFDateTime creationtime;
		private java.lang.String modifier;
		private nc.vo.pub.lang.UFDateTime modifiedtime;
		
		/**
		 * �������
		 */
		private Integer totalType ;
		
		/**
		 * ֱ���¼�
		 */
		public final  static Integer   TOTAL_TYPE_DIRECT = 1;
		
		/**
		 * �����¼�
		 */
		public final  static Integer   TOTAL_TYPE_ALL = 2;
		
		/**
		 * ������
		 */
		public final  static Integer   TOTAL_TYPE_NOT = 3;
		
		
		/**
		 * �Զ���
		 */
		public final  static Integer   TOTAL_TYPE_DIY = 4;
		

		private java.lang.Integer dr = 0;
		private nc.vo.pub.lang.UFDateTime ts;
		
		/**
		 * �Զ���
		 */
		private String def1;
		
	 
		
		
		public Integer getTotalType() {
			return totalType;
		}
		public void setTotalType(Integer totalType) {
			this.totalType = totalType;
		}
		public String getDef1() {
			return def1;
		}
		public void setDef1(String def1) {
			this.def1 = def1;
		}
		public java.lang.String getPk_hbscheme() {
			return pk_hbscheme;
		}
		public void setPk_hbscheme(java.lang.String pk_hbscheme) {
			this.pk_hbscheme = pk_hbscheme;
		}
		
		/**
		 * ����pk_rms��Getter����.
		 * ��������:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getPk_rms () {
			return pk_rms;
		}   
		/**
		 * ����pk_rms��Setter����.
		 * ��������:2010-03-15 11:08:11
		 * @param newPk_rms java.lang.String
		 */
		public void setPk_rms (java.lang.String newPk_rms ) {
		 	this.pk_rms = newPk_rms;
		} 	  
		/**
		 * ����app_org��Getter����.
		 * ��������:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getApp_org () {
			return app_org;
		}   
		/**
		 * ����app_org��Setter����.
		 * ��������:2010-03-15 11:08:11
		 * @param newApp_org java.lang.String
		 */
		public void setApp_org (java.lang.String newApp_org ) {
		 	this.app_org = newApp_org;
		} 	  
		/**
		 * ����pk_org��Getter����.
		 * ��������:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getPk_org () {
			return pk_org;
		}   
		/**
		 * ����pk_org��Setter����.
		 * ��������:2010-03-15 11:08:11
		 * @param newPk_org java.lang.String
		 */
		public void setPk_org (java.lang.String newPk_org ) {
		 	this.pk_org = newPk_org;
		} 	  
		/**
		 * ����pk_group��Getter����.
		 * ��������:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getPk_group () {
			return pk_group;
		}   
		/**
		 * ����pk_group��Setter����.
		 * ��������:2010-03-15 11:08:11
		 * @param newPk_group java.lang.String
		 */
		public void setPk_group (java.lang.String newPk_group ) {
		 	this.pk_group = newPk_group;
		} 	  
		/**
		 * ����creator��Getter����.
		 * ��������:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getCreator () {
			return creator;
		}   
		/**
		 * ����creator��Setter����.
		 * ��������:2010-03-15 11:08:11
		 * @param newCreator java.lang.String
		 */
		public void setCreator (java.lang.String newCreator ) {
		 	this.creator = newCreator;
		} 	  
		/**
		 * ����creationtime��Getter����.
		 * ��������:2010-03-15 11:08:11
		 * @return nc.vo.pub.lang.UFDateTime
		 */
		public nc.vo.pub.lang.UFDateTime getCreationtime () {
			return creationtime;
		}   
		/**
		 * ����creationtime��Setter����.
		 * ��������:2010-03-15 11:08:11
		 * @param newCreationtime nc.vo.pub.lang.UFDateTime
		 */
		public void setCreationtime (nc.vo.pub.lang.UFDateTime newCreationtime ) {
		 	this.creationtime = newCreationtime;
		} 	  
		/**
		 * ����modifier��Getter����.
		 * ��������:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getModifier () {
			return modifier;
		}   
		/**
		 * ����modifier��Setter����.
		 * ��������:2010-03-15 11:08:11
		 * @param newModifier java.lang.String
		 */
		public void setModifier (java.lang.String newModifier ) {
		 	this.modifier = newModifier;
		} 	  
		/**
		 * ����modifiedtime��Getter����.
		 * ��������:2010-03-15 11:08:11
		 * @return nc.vo.pub.lang.UFDateTime
		 */
		public nc.vo.pub.lang.UFDateTime getModifiedtime () {
			return modifiedtime;
		}   
		/**
		 * ����modifiedtime��Setter����.
		 * ��������:2010-03-15 11:08:11
		 * @param newModifiedtime nc.vo.pub.lang.UFDateTime
		 */
		public void setModifiedtime (nc.vo.pub.lang.UFDateTime newModifiedtime ) {
		 	this.modifiedtime = newModifiedtime;
		} 	  
		/**
		 * ����dr��Getter����.
		 * ��������:2010-03-15 11:08:11
		 * @return java.lang.Integer
		 */
		public java.lang.Integer getDr () {
			return dr;
		}   
		/**
		 * ����dr��Setter����.
		 * ��������:2010-03-15 11:08:11
		 * @param newDr java.lang.Integer
		 */
		public void setDr (java.lang.Integer newDr ) {
		 	this.dr = newDr;
		} 	  
		/**
		 * ����ts��Getter����.
		 * ��������:2010-03-15 11:08:11
		 * @return nc.vo.pub.lang.UFDateTime
		 */
		public nc.vo.pub.lang.UFDateTime getTs () {
			return ts;
		}   
		/**
		 * ����ts��Setter����.
		 * ��������:2010-03-15 11:08:11
		 * @param newTs nc.vo.pub.lang.UFDateTime
		 */
		public void setTs (nc.vo.pub.lang.UFDateTime newTs ) {
		 	this.ts = newTs;
		} 	  
	 
		/**
		  * <p>ȡ�ø�VO�����ֶ�.
		  * <p>
		  * ��������:2010-03-15 11:08:11
		  * @return java.lang.String
		  */
		public java.lang.String getParentPKFieldName() {
		    return null;
		}   
	    
		/**
		  * <p>ȡ�ñ�����.
		  * <p>
		  * ��������:2010-03-15 11:08:11
		  * @return java.lang.String
		  */
		public java.lang.String getPKFieldName() {
		  return "pk_hbscheme";
		}
	    
		/**
		 * <p>���ر�����.
		 * <p>
		 * ��������:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getTableName() {
			return "iufo_hb_scheme";
		}    
		
		/**
		 * <p>���ر�����.
		 * <p>
		 * ��������:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public static java.lang.String getDefaultTableName() {
			return "iufo_hb_scheme";
		}    
	    
	  
		public java.lang.String getPk_rmsversion() {
			return pk_rmsversion;
		}
		public void setPk_rmsversion(java.lang.String pk_rmsversion) {
			this.pk_rmsversion = pk_rmsversion;
		}

}
