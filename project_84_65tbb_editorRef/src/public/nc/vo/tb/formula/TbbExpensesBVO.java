package nc.vo.tb.formula;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;

public class TbbExpensesBVO extends SuperVO  {

	private static final long serialVersionUID = 5983655338480342125L;



	public UFDateTime m_ts;
	
	private String pk_expenses;
	
	private String pk_expenses_b;
	
	private String szxm;
 
	 

	@Override
	public String getPKFieldName() {
		// TODO 自动生成方法存根
		return "pk_expenses_b";
	}

	@Override
	public String getParentPKFieldName() {
		// TODO 自动生成方法存根
		return "pk_expenses";
	}

	@Override
	public String getTableName() {
		// TODO 自动生成方法存根
		return "fy_expenses_b";
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

	public String getPk_expenses_b() {
		return pk_expenses_b;
	}

	public void setPk_expenses_b(String pk_expenses_b) {
		this.pk_expenses_b = pk_expenses_b;
	}

	public String getSzxm() {
		return szxm;
	}

	public void setSzxm(String szxm) {
		this.szxm = szxm;
	}

	
	
	
	
	
	

 

	
	
}