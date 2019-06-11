package nc.impl.hbbb.backplugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nc.jdbc.framework.processor.ResultSetProcessor;

public class EsbOrgVO {
	
	private String code;
	private String name;
	private String pcode;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPcode() {
		return pcode;
	}
	public void setPcode(String pcode) {
		this.pcode = pcode;
	}
	
	
	

}

class EsbOrgVOProcessor implements ResultSetProcessor {
	private static final long serialVersionUID = 5805152858101793359L;
	@Override
	public List<EsbOrgVO> handleResultSet(ResultSet rs) throws SQLException {
		List<EsbOrgVO> rsList = new ArrayList<EsbOrgVO>();
		while (rs.next()) {
			EsbOrgVO vo = new EsbOrgVO();
		 
				 
				vo.setCode(rs.getString("code"));
				vo.setName(rs.getString("name"));
				vo.setPcode(rs.getString("pcode"));
		 
			rsList.add(vo);
		}

		return rsList;
	}
	 

}
