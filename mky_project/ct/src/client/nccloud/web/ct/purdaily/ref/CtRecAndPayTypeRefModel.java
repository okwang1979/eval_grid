package nccloud.web.ct.purdaily.ref;

import nc.vo.pubapp.pattern.pub.SqlBuilder;
import nccloud.framework.web.container.SessionContext;
import nccloud.framework.web.processor.refgrid.RefQueryInfo;
import nccloud.framework.web.ui.meta.RefMeta;
import nccloud.web.refer.DefaultGridRefAction;

/**
 * 合同收付款类型
 * @author zhangfx
 * @since 2020/11/03
 * */
public class CtRecAndPayTypeRefModel extends DefaultGridRefAction{

	@Override
	public RefMeta getRefMeta(RefQueryInfo refQueryInfo) {
		// TODO 自动生成的方法存根
		RefMeta meta = new RefMeta();
		meta.setCodeField("code");
		meta.setNameField("name");
		meta.setExtraFields(new String[] { "code", "name" });
		meta.setPkField("pk_defdoc");
		meta.setTableName(getTableName());
		
		return meta;
	}

	private String getTableName() { 
		
		return "bd_defdoc"; 
	}
	
	public String getExtraSql(RefQueryInfo paramRefQueryInfo, RefMeta paramRefMeta) {
		String pk_group = SessionContext.getInstance().getClientInfo().getPk_group();
		SqlBuilder sql = new SqlBuilder();
		sql.append("bd_defdoc.pk_group", pk_group);
		sql.append(" and bd_defdoc.pk_defdoclist = '1001A11000000000UUQJ' ");
		return sql.toString();
	}
}
