package nc.vo.hbbb.total;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nc.jdbc.framework.processor.ResultSetProcessor;

/**
 * 
 *
 */
public class HbTotalOrgTreeResultSet implements ResultSetProcessor ,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object handleResultSet(ResultSet rs) throws SQLException {
		List<HbTotalOrgTreeVO> rtn = new ArrayList<>();
		while (rs.next()) {
			HbTotalOrgTreeVO vo = new HbTotalOrgTreeVO();
			vo.setCode(rs.getString("code"));
			vo.setInnercode(rs.getString("innercode"));
			vo.setName(rs.getString("name"));
			vo.setPk_org(rs.getString("pk_org"));
			vo.setPk_rcs(rs.getString("pk_rcs"));
			vo.setPk_svid(rs.getString("pk_svid"));
			vo.setPk_fatherorg(rs.getString("pk_fatherorg"));
			rtn.add(vo);
		}
		return rtn;
	}

}
