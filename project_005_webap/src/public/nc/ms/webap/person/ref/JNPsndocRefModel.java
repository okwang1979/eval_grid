package nc.ms.webap.person.ref;

import nc.ui.bd.ref.model.PsndocDefaultNCRefModel;

public class JNPsndocRefModel extends PsndocDefaultNCRefModel {

	public JNPsndocRefModel() {
		super();
		this.setBusifuncode("all");
		// reset();
	}

	public String getClassWherePart() {
		String rtn = " pk_org in(select pk_org from org_orgs where innercode like 'DV8Q7T2NE0RF%' ) or  pk_org in(  select pk_dept from org_dept where pk_org in(select pk_org from org_orgs where innercode like 'DV8Q7T2NE0RF%'))";
		return rtn;
	}

	protected String getEnvWherePart() {
		StringBuffer sb = new StringBuffer();
		sb.append("11=11");
//		sb.append(PsnjobVO.getDefaultTableName() + "." + "pk_org" + " = '"
//				+ NCESAPI.clientSqlEncode(getPk_org()) + "'");
		sb.append(getIsLeaveCondition());
		return sb.toString();
	}

	public String getClassRefSql() {
		String sql = getClassSql(getClassFieldCode(), getClassTableName(),
				getClassWherePart(), getClassOrderPart());
		return sql;
	}

}
