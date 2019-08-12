package nc.bs.hbbb.contrast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nc.bs.iufo.DataManageObjectIufo;

import com.ufida.iufo.pub.tools.AppDebug;

public class ContrastDMO  extends DataManageObjectIufo{
	
	public Set<String> getcontrastOrg(String sql,boolean isDICCORP,Map<String,String> orgSupplier) throws SQLException {

		if(sql == null || sql.length() == 0){
			return null;
		}

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Set<String> contrastOrg = new HashSet<String>();
		try{
			con = getConnection();
			stmt = con.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String key1 = rs.getString(1);
				String key2 = rs.getString(2);
				if(!isDICCORP){
					key2 = orgSupplier.get(key2);
					if(key2 != null){
						contrastOrg.add(key1+key2);
						//zhaojian8 20180206 对账对少对方单位（之前没考虑单边） start
						contrastOrg.add(key2+key1);
						//zhaojian8 20180206 对账对少对方单位（之前没考虑单边） end
					}
				}else{
					contrastOrg.add(key1+key2);
					//zhaojian8 20180206  对账对少对方单位（之前没考虑单边） start
					contrastOrg.add(key2+key1);
					//zhaojian8 20180206  对账对少对方单位（之前没考虑单边） end
				}
			}

		}catch(SQLException e){
			AppDebug.debug(e);
			throw e;
		}finally{

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
				}
			}
		}

		return contrastOrg;

	}

}
