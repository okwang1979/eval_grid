package nc.ms.webap.org.ref;

import nc.ui.org.ref.BusinessUnitDefaultRefModel;

/** 
 * �������ŵĵ�λ
 * <b>Application name:</b>�Ϳ���Ŀ<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 ��������ɷ����޹�˾��Ȩ���С�<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-4-15<br>
 * @author����־ǿ
 * @version �Ϳ�
 */ 
public class JNOrgRefModel extends BusinessUnitDefaultRefModel{
	
	
	
	
	/**
	 * 
	 * ���췽��������һ����GLBUWithBookRefModelʵ��
	 */
	public JNOrgRefModel() {
		super();
		setMatchPkWithWherePart(true);
		this.setWherePart(" 1=1 and  innercode like 'DV8Q7T2NE0RF%'  ");
	}
	
	
	
 

}
