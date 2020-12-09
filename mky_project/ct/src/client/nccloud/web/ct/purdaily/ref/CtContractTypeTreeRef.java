package nccloud.web.ct.purdaily.ref;

import nc.bs.logging.Logger;
import nc.vo.pub.BusinessException;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.processor.refgrid.RefQueryInfo;
import nccloud.framework.web.ui.meta.RefMeta;
import nccloud.framework.web.ui.meta.TreeRefMeta;
import nccloud.pubitf.web.refer.INCCRefQry;
import nccloud.web.refer.DefaultTreeRefAction;

public class CtContractTypeTreeRef extends DefaultTreeRefAction{
	public CtContractTypeTreeRef() {
		setShowDisabledData(Boolean.valueOf(false));
		setUnitPkKey("pk_org");
	}

	public String getExtraSql(RefQueryInfo refQueryInfo, RefMeta refMeta) {
		String key = "vdef33Val";
		String queryStr =  " and pk_defdoclist='"+getPk_defdoclist(null)+"'";
		if(refQueryInfo.getQueryCondition().get(key)!=null&&refQueryInfo.getQueryCondition().get(key).length()>10) {
			queryStr  = queryStr +" and pid='"+refQueryInfo.getQueryCondition().get(key)+"'";
		}else {
			queryStr = queryStr+ "  and length(innercode)<=12";
		}
		return queryStr;
	}

//	public SqlParameterCollection getExtraSqlParameter(RefQueryInfo refQueryInfo, RefMeta refMeta) {
//		SqlParameterCollection para = new SqlParameterCollection();
//
//		para.addChar(getPk_defdoclist(refQueryInfo));
//
//		return para;
//	}
//
	private String getPk_defdoclist(RefQueryInfo refQueryInfo) {
		return "1001A11000000000UMPS";
	}

	public TreeRefMeta getRefMeta(RefQueryInfo refQueryInfo) {
		try {
			setResourceCode(((INCCRefQry) ServiceLocator.find(INCCRefQry.class))
					.getDefdocRefResourceId(getPk_defdoclist(refQueryInfo)));
		} catch (BusinessException ex) {
			Logger.error(ex.getMessage(), ex);
		}
		setMdClassId(getPk_defdoclist(refQueryInfo));

		TreeRefMeta refMeta = new TreeRefMeta();

		refMeta.setCodeField("code");
		refMeta.setNameField("name");
		refMeta.setPkField("pk_defdoc");
		refMeta.setPidField("pid");
		refMeta.setChildField("pk_defdoc");
		refMeta.setInnercodeField("innercode");
		refMeta.setExtraFields(new String[]{"code", "name", "shortname", "mnecode", "enablestate"});
		refMeta.setMutilLangNameRef(true);
		refMeta.setTableName(getTableName(getPk_defdoclist(refQueryInfo)));

		return refMeta;
	}

	protected String getTableName(String pk_defdoclist) {
 
		return "bd_defdoc";
	}
}
