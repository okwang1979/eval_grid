package nc.itf.hbbb.ref;

import nc.ui.bd.ref.AbstractRefModel;
public class JNReportTypeRefModel extends AbstractRefModel {

	public JNReportTypeRefModel() {
		super();

		setRefNodeName("��������");
		setRefTitle("��������");
		setFieldCode(new String[] { "code", "name"});
		setFieldName(new String[] {
				 "����","����"
					});
		 
		setPkFieldCode("code");
		setRefCodeField("code");
		setRefNameField("name");
		setTableName("iufo_jn_reporttype");
//		setFatherField(ApportionRuleVO.PK_UNIT);
//		setChildField(ApportionRuleVO.PK_UNIT);

		 

		//setResourceID();
		setMatchPkWithWherePart(false);
		resetFieldName();
		}

}
