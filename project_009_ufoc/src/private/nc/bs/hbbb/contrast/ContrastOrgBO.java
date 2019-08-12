package nc.bs.hbbb.contrast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.ufida.iufo.pub.tools.AppDebug;

import nc.bs.iufo.DataManageObjectIufo;
import nc.bs.mw.sqltrans.TempTable;

/**
 * 创建对账对临时表
 * @author sunzeg
 *
 */
public class ContrastOrgBO extends DataManageObjectIufo {
	
	public String createTempTablebyContrastOrgs(List<String[]> selfOppOrgs) throws SQLException{

		String tempTableName ="iufo_temp_contrast_org";
		//String tempTableName = "iufo_contrast_org";
		Connection con = getConnection();
		PreparedStatement stmt  = null;
		//Statement stmt  = null;
		String columns = "self_org char(20), opp_org char(20)";
		try {
			/*TempTable tempTable = new TempTable();
			tempTableName = tempTable.createTempTable(con, tempTableName, columns, null);		*/	
			String sql = "insert into " + tempTableName + " (self_org, opp_org) values(?,?,?)";
			stmt = con.prepareStatement(sql);
			//stmt = con.createStatement();
			for(int i = 0; i < selfOppOrgs.size(); i++){	
				String[] contrastOrg = selfOppOrgs.get(i);
				stmt.setString(1, contrastOrg[0]);
				stmt.setString(2, contrastOrg[1]);
				stmt.addBatch();
				//String sql ="insert into " + tableName + " (self_org , opp_org) values(\'"+ contrastOrg[0] +"\',\'" + contrastOrg[1] + "\')";
				//stmt.addBatch(sql);
				//stmt.execute(sql);
				if ((i + 1) % 500 == 0) {
					stmt.executeBatch();
					stmt.clearBatch();
				}				
			}
			stmt.executeBatch();
			stmt.clearBatch();
		} catch(SQLException e){
			AppDebug.error(e);
			throw new SQLException(e);
		} catch(Error e){
			AppDebug.error(e);
		} catch(Throwable e){
			AppDebug.error(e);
		} finally {
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
		return tempTableName;
	}
}
