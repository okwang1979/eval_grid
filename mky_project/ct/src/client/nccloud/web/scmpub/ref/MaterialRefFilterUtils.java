package nccloud.web.scmpub.ref;

import nc.vo.pubapp.pattern.pub.PubAppTool;
import nc.vo.pubapp.pattern.pub.SqlBuilder;
import nccloud.framework.web.processor.IRefSqlBuilder;
import nccloud.framework.web.processor.refgrid.RefQueryInfo;
import nccloud.framework.web.ui.meta.RefMeta;
import nccloud.pubitf.platform.db.SqlParameterCollection;
import nccloud.web.scmpub.ref.sql.MaterialFilterSql;

public class MaterialRefFilterUtils implements IRefSqlBuilder {

	private static final String SCM_DISCOUNTFLAG = "SCM_DISCOUNTFLAG";
	private static final String SCM_FEEFLAG = "SCM_FEEFLAG";
	private static final String SCM_PKMARBASCLASS = "SCM_PKMARBASCLASS";
	private static final String SCM_ISHPROITEMS = "SCM_ISHPROITEMS";
	

	@Override
	public String getExtraSql(RefQueryInfo para, RefMeta meta) {
		SqlBuilder sb = new SqlBuilder();

		if (!PubAppTool.isNull(para.getQueryCondition().get(SCM_PKMARBASCLASS))) {
			sb.append(MaterialFilterSql.filterRefByMarbasclass(para.getQueryCondition().get(SCM_PKMARBASCLASS)));
		}
		if (!PubAppTool.isNull(para.getQueryCondition().get(SCM_DISCOUNTFLAG))) {
			sb.append(MaterialFilterSql.filterRefByDiscountflag(para.getQueryCondition().get(SCM_DISCOUNTFLAG)));
			sb.append(" and ");
		}
		if (!PubAppTool.isNull(para.getQueryCondition().get(SCM_FEEFLAG))) {
			sb.append(MaterialFilterSql.filterRefByFeeflag(para.getQueryCondition().get(SCM_FEEFLAG)));
		}
		if (!PubAppTool.isNull(para.getQueryCondition().get(SCM_ISHPROITEMS))) {
			sb.append(" and ");
			sb.append(MaterialFilterSql.filterRefByIshproitems(para.getQueryCondition().get(SCM_ISHPROITEMS)));
		}
		if(sb.toString().endsWith("and ")) {
			return sb.toString().substring(0,sb.toString().length()-4);
		}else {
			return sb.toString();
		}

	
	}

	@Override
	public SqlParameterCollection getExtraSqlParameter(RefQueryInfo para, RefMeta meta) {
		SqlParameterCollection params = new SqlParameterCollection();
		if (!PubAppTool.isNull(para.getQueryCondition().get(SCM_PKMARBASCLASS))) {
			params.addVarChar(para.getQueryCondition().get(SCM_PKMARBASCLASS));
		}
		if (!PubAppTool.isNull(para.getQueryCondition().get(SCM_DISCOUNTFLAG))) {
			params.addVarChar(para.getQueryCondition().get(SCM_DISCOUNTFLAG));
		}
		if (!PubAppTool.isNull(para.getQueryCondition().get(SCM_FEEFLAG))) {
			params.addVarChar(para.getQueryCondition().get(SCM_FEEFLAG));
		}
		if (!PubAppTool.isNull(para.getQueryCondition().get(SCM_ISHPROITEMS))) {
			params.addVarChar(para.getQueryCondition().get(SCM_ISHPROITEMS));
		}
		return params;
	}

	@Override
	public String getOrderSql(RefQueryInfo para, RefMeta meta) {
		// TODO Auto-generated method stub
		return null;
	}

}
