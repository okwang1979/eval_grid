package nc.impl.hbbb.backplugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nc.jdbc.framework.processor.ResultSetProcessor;

/** 
 * 指标表数据
 * <b>Application name:</b>客开项目<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 用友软件股份有限公司版权所有。<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-5-6<br>
 * @author：王志强
 * @version 客开
 */ 
public class ESBMeasrueVO {
	
	public static String selectSql = "select  ITEM_CODE,ITEM_NAME,CELL,TABLE1,FIELD1,TABLE2,FIELD2,TABLE3,FIELD3,TABLE_NAME,ITEM_NAME2   from t_etl_item_65";
	
	private String item_code;
	private String item_name;
	private String cell;
	private String table1;
	private String field1;
	private String table2;
	private String field2;
	private String table3;
	private String field3;
	private String table_name;
	private String item_name2;
	
	public String getItem_code() {
		return item_code;
	}
	public void setItem_code(String item_code) {
		this.item_code = item_code;
	}
	public String getItem_name() {
		return item_name;
	}
	public void setItem_name(String item_name) {
		this.item_name = item_name;
	}
	public String getCell() {
		return cell;
	}
	public void setCell(String cell) {
		this.cell = cell;
	}
	public String getTable1() {
		return table1;
	}
	public void setTable1(String table1) {
		this.table1 = table1;
	}
	public String getField1() {
		return field1;
	}
	public void setField1(String field1) {
		this.field1 = field1;
	}
	public String getTable2() {
		return table2;
	}
	public void setTable2(String table2) {
		this.table2 = table2;
	}
	public String getField2() {
		return field2;
	}
	public void setField2(String field2) {
		this.field2 = field2;
	}
	public String getTable3() {
		return table3;
	}
	public void setTable3(String table3) {
		this.table3 = table3;
	}
	public String getField3() {
		return field3;
	}
	public void setField3(String field3) {
		this.field3 = field3;
	}
	public String getTable_name() {
		return table_name;
	}
	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}
	public String getItem_name2() {
		return item_name2;
	}
	public void setItem_name2(String item_name2) {
		this.item_name2 = item_name2;
	}
	
	
	
	
	
	


}
class ESBMeasureProcessor implements ResultSetProcessor {
	private static final long serialVersionUID = 5805152858101793359L;
	@Override
	public List<ESBMeasrueVO> handleResultSet(ResultSet rs) throws SQLException {
		List<ESBMeasrueVO> rsList = new ArrayList<ESBMeasrueVO>();
		while (rs.next()) {
			ESBMeasrueVO vo = new ESBMeasrueVO();
			vo.setItem_code(rs.getString("item_code"));
			vo.setItem_name(rs.getString("item_name"));
			vo.setCell(rs.getString("cell"));
			vo.setTable1(rs.getString("table1"));
			vo.setTable2(rs.getString("table2"));
			vo.setTable3(rs.getString("table3"));
			vo.setField1(rs.getString("field1"));
			vo.setField2(rs.getString("field2"));
			
			vo.setField3(rs.getString("field3"));
			vo.setTable_name(rs.getString("table_name"));
			vo.setItem_name2(rs.getString("item_name2"));
			rsList.add(vo);
		}

		return rsList;
	}
	 

}