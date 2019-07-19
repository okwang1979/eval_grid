package nc.vo.hbbb.total;

import nc.vo.pub.SuperVO;

/**
 * 
 * 汇总方案表
 *
 */
public class HbTotalSchemeVO extends SuperVO {
	
		/**
		 * 主键
		 */
		private java.lang.String pk_hbscheme ;
		/**
		 * 设置主体pk
		 */
		private String app_org;
		/**
		 * 合并报表组织体系
		 */
		private java.lang.String pk_rms;
		/**
		 * 版本
		 */
		private java.lang.String pk_rmsversion;
		/**
		 * 没用，主体
		 */
		private java.lang.String pk_org;
		/**
		 * 没用，集团
		 */
		private java.lang.String pk_group;
 
		private java.lang.String creator;
		private nc.vo.pub.lang.UFDateTime creationtime;
		private java.lang.String modifier;
		private nc.vo.pub.lang.UFDateTime modifiedtime;
		
		/**
		 * 汇总类别
		 */
		private Integer totalType ;
		
		/**
		 * 直接下级
		 */
		public final  static Integer   TOTAL_TYPE_DIRECT = 1;
		
		/**
		 * 所有下级
		 */
		public final  static Integer   TOTAL_TYPE_ALL = 2;
		
		/**
		 * 不汇总
		 */
		public final  static Integer   TOTAL_TYPE_NOT = 3;
		
		
		/**
		 * 自定义
		 */
		public final  static Integer   TOTAL_TYPE_DIY = 4;
		

		private java.lang.Integer dr = 0;
		private nc.vo.pub.lang.UFDateTime ts;
		
		/**
		 * 自定义
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
		 * 属性pk_rms的Getter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getPk_rms () {
			return pk_rms;
		}   
		/**
		 * 属性pk_rms的Setter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @param newPk_rms java.lang.String
		 */
		public void setPk_rms (java.lang.String newPk_rms ) {
		 	this.pk_rms = newPk_rms;
		} 	  
		/**
		 * 属性app_org的Getter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getApp_org () {
			return app_org;
		}   
		/**
		 * 属性app_org的Setter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @param newApp_org java.lang.String
		 */
		public void setApp_org (java.lang.String newApp_org ) {
		 	this.app_org = newApp_org;
		} 	  
		/**
		 * 属性pk_org的Getter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getPk_org () {
			return pk_org;
		}   
		/**
		 * 属性pk_org的Setter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @param newPk_org java.lang.String
		 */
		public void setPk_org (java.lang.String newPk_org ) {
		 	this.pk_org = newPk_org;
		} 	  
		/**
		 * 属性pk_group的Getter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getPk_group () {
			return pk_group;
		}   
		/**
		 * 属性pk_group的Setter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @param newPk_group java.lang.String
		 */
		public void setPk_group (java.lang.String newPk_group ) {
		 	this.pk_group = newPk_group;
		} 	  
		/**
		 * 属性creator的Getter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getCreator () {
			return creator;
		}   
		/**
		 * 属性creator的Setter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @param newCreator java.lang.String
		 */
		public void setCreator (java.lang.String newCreator ) {
		 	this.creator = newCreator;
		} 	  
		/**
		 * 属性creationtime的Getter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @return nc.vo.pub.lang.UFDateTime
		 */
		public nc.vo.pub.lang.UFDateTime getCreationtime () {
			return creationtime;
		}   
		/**
		 * 属性creationtime的Setter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @param newCreationtime nc.vo.pub.lang.UFDateTime
		 */
		public void setCreationtime (nc.vo.pub.lang.UFDateTime newCreationtime ) {
		 	this.creationtime = newCreationtime;
		} 	  
		/**
		 * 属性modifier的Getter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getModifier () {
			return modifier;
		}   
		/**
		 * 属性modifier的Setter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @param newModifier java.lang.String
		 */
		public void setModifier (java.lang.String newModifier ) {
		 	this.modifier = newModifier;
		} 	  
		/**
		 * 属性modifiedtime的Getter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @return nc.vo.pub.lang.UFDateTime
		 */
		public nc.vo.pub.lang.UFDateTime getModifiedtime () {
			return modifiedtime;
		}   
		/**
		 * 属性modifiedtime的Setter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @param newModifiedtime nc.vo.pub.lang.UFDateTime
		 */
		public void setModifiedtime (nc.vo.pub.lang.UFDateTime newModifiedtime ) {
		 	this.modifiedtime = newModifiedtime;
		} 	  
		/**
		 * 属性dr的Getter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @return java.lang.Integer
		 */
		public java.lang.Integer getDr () {
			return dr;
		}   
		/**
		 * 属性dr的Setter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @param newDr java.lang.Integer
		 */
		public void setDr (java.lang.Integer newDr ) {
		 	this.dr = newDr;
		} 	  
		/**
		 * 属性ts的Getter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @return nc.vo.pub.lang.UFDateTime
		 */
		public nc.vo.pub.lang.UFDateTime getTs () {
			return ts;
		}   
		/**
		 * 属性ts的Setter方法.
		 * 创建日期:2010-03-15 11:08:11
		 * @param newTs nc.vo.pub.lang.UFDateTime
		 */
		public void setTs (nc.vo.pub.lang.UFDateTime newTs ) {
		 	this.ts = newTs;
		} 	  
	 
		/**
		  * <p>取得父VO主键字段.
		  * <p>
		  * 创建日期:2010-03-15 11:08:11
		  * @return java.lang.String
		  */
		public java.lang.String getParentPKFieldName() {
		    return null;
		}   
	    
		/**
		  * <p>取得表主键.
		  * <p>
		  * 创建日期:2010-03-15 11:08:11
		  * @return java.lang.String
		  */
		public java.lang.String getPKFieldName() {
		  return "pk_hbscheme";
		}
	    
		/**
		 * <p>返回表名称.
		 * <p>
		 * 创建日期:2010-03-15 11:08:11
		 * @return java.lang.String
		 */
		public java.lang.String getTableName() {
			return "iufo_hb_scheme";
		}    
		
		/**
		 * <p>返回表名称.
		 * <p>
		 * 创建日期:2010-03-15 11:08:11
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
