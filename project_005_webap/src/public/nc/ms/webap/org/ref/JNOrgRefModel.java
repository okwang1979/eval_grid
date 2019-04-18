package nc.ms.webap.org.ref;

import nc.ui.org.ref.BusinessUnitDefaultRefModel;

/** 
 * 健康集团的单位
 * <b>Application name:</b>客开项目<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 用友软件股份有限公司版权所有。<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-4-15<br>
 * @author：王志强
 * @version 客开
 */ 
public class JNOrgRefModel extends BusinessUnitDefaultRefModel{
	
	
	
	
	/**
	 * 
	 * 构造方法：创建一个新GLBUWithBookRefModel实例
	 */
	public JNOrgRefModel() {
		super();
		setMatchPkWithWherePart(true);
		this.setWherePart(" 1=1 and  innercode like 'DV8Q7T2NE0RF%'  ");
	}
	
	
	
 

}
