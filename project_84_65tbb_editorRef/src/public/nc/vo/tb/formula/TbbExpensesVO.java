package nc.vo.tb.formula;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;

public class TbbExpensesVO extends SuperVO  {

	private static final long serialVersionUID = 5983655338480342125L;



	public UFDateTime m_ts;
	
	private String pk_expenses;
	
	
	private String pk_dept;//部门
	
	private String pk_expenses_doc;//事项
	
	private String pk_group;
	
	private String pk_org;//主体
	
	private String def1; //事项类型
	
	
	
	 

	@Override
	public String getPKFieldName() {
		// TODO 自动生成方法存根
		return "pk_expenses";
	}

	@Override
	public String getParentPKFieldName() {
		// TODO 自动生成方法存根
		return null;
	}

	@Override
	public String getTableName() {
		// TODO 自动生成方法存根
		return "fy_expenses";
	}

 

	public UFDateTime getTs() {
		return m_ts;
	}

	public void setTs(UFDateTime ts) {
		this.m_ts = ts;
	}

	public UFDateTime getM_ts() {
		return m_ts;
	}

	public void setM_ts(UFDateTime m_ts) {
		this.m_ts = m_ts;
	}

	public String getPk_expenses() {
		return pk_expenses;
	}

	public void setPk_expenses(String pk_expenses) {
		this.pk_expenses = pk_expenses;
	}

	public String getPk_dept() {
		return pk_dept;
	}

	public void setPk_dept(String pk_dept) {
		this.pk_dept = pk_dept;
	}

	public String getPk_expenses_doc() {
		return pk_expenses_doc;
	}

	public void setPk_expenses_doc(String pk_expenses_doc) {
		this.pk_expenses_doc = pk_expenses_doc;
	}

	public String getPk_group() {
		return pk_group;
	}

	public void setPk_group(String pk_group) {
		this.pk_group = pk_group;
	}

	public String getPk_org() {
		return pk_org;
	}

	public void setPk_org(String pk_org) {
		this.pk_org = pk_org;
	}

	public String getDef1() {
		return def1;
	}

	public void setDef1(String def1) {
		this.def1 = def1;
	}

	
	
	
	
	

 

	
	
}