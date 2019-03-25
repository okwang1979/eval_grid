package nc.impl.hbbb.linkend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.logging.Logger;
import nc.itf.hbbb.linkend.ILinkEndQuery;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.pub.smart.util.SmartUtilities;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;

import com.ufida.dataset.db.DbType;

public class LinkEndQueryImpl implements ILinkEndQuery{
	
	
	public Map<String,String> getUnionOrgsOrderByCode(String pk_svid,String parent_innercode,Integer level) throws BusinessRuntimeException {
//		List<String> result = new ArrayList<String>();
		StringBuffer content = new StringBuffer();
//		DbType dbType = SmartUtilities.getDbType(InvocationInfoProxy.getInstance().getUserDataSource());
//		// 操作符
//		String operator = "||";
//		if (dbType == DbType.SQLSERVER) {
//			operator = "+";
//		}
		content.append("SELECT org_rcsmember_v.pk_org,org_rcsmember_v.innercode innercode ");
		content.append("  FROM org_rcsmember_v,org_orgs ");
		content.append(" WHERE org_orgs.pk_org= org_rcsmember_v.pk_org and pk_svid = ? AND org_rcsmember_v.innercode like '")
			.append(parent_innercode.trim()).append("%' ").append(" order by org_rcsmember_v.idx desc");
//				.append(parent_innercode.trim()).append("%' order by org_rcsmember_v.idx desc");
		if(level!=null){
			int maxLen = parent_innercode.length()+level.intValue()*4;
			content.append(" and length(org_rcsmember_v.innercode)<="+maxLen);
		}
	 

		SQLParameter param = new SQLParameter();
		param.addParam(pk_svid);
		BaseDAO dao = new BaseDAO();
		Map<String, String> childern;
		try {
			childern = (Map<String,String>)dao.executeQuery(content.toString(), param, new ResSetProcessor());
		} catch (Exception e) {
			  Logger.error(e.getMessage(), e);
              throw new BusinessRuntimeException(e.getMessage());
		}
		
		return childern;
		
		
		
		
//		int firstsubnode = 20 + parent_innercode.trim().length() + 4;
//		int rootnode = 20 + parent_innercode.trim().length();
//		String rootnodesString = null;
		
//		ArrayList<String> list = new ArrayList<String>();
//		if (null != pks && pks.size() > 0) {
//			for (String str : pks) {
//				if (null != str && str.trim().length() > 0) {
//					if (str.trim().length() == rootnode) {
//						//@edited by zhoushuang 先排除母公司
////							list.add(str.trim());
//						rootnodesString = str.trim();
//					} else if (str.trim().length() == firstsubnode) {
//						list.add(str.trim());
//					}
//				}
//			}
//			if (list.size() > 0) {
//				if (rootnodesString!=null) {
//					result.add(rootnodesString);
//				}
//				result.addAll(result.size(), list);
//			}
//		}
//		return result.toArray(new String[0]);
	}
	private class ResSetProcessor implements ResultSetProcessor {
		private static final long serialVersionUID = 8715819462600958845L;

		@Override
		public Map<String,String> handleResultSet(ResultSet rs) throws SQLException {
			Map<String,String> rtn = new HashMap<String, String>();
			while(rs.next()) {
				String pk_org = rs.getString("pk_org");
				String innercode  = rs.getString("innercode");
				rtn.put(pk_org,innercode);
			}
			return rtn;
		}
		
	}

}
