package nc.impl.hbbb.backplugin;

import java.util.HashMap;
import java.util.Map;

public final class ReportImportConst {
	public static  String[] reportCodes = { "CECEP_CWYB01", "CECEP_CWYB05",
			"CECEP_CWYB02", "CECEP_CWYB03" };
	
//	public static String OTHER_DATASOURCE = "ESB";
	
	
	
 
	// 资产负债表 所有者权益表 利润表 现金流量
	private static Map<String, String> nameMap = new HashMap<String, String>();
	static {
//		nameMap = new HashMap<String, String>();
		nameMap.put("CECEP_CWYB01", "65资产负债表");
		nameMap.put("CECEP_CWYB05", "65所有者权益表");
		nameMap.put("CECEP_CWYB02", "65利润表");
		nameMap.put("CECEP_CWYB03", "65现金流量表");
	}
	
	public static  Map<String, String>  getNameMap(){
		return nameMap;
	}
	
	
	// 资产负债表 所有者权益表 利润表 现金流量
	private static Map<String, String> tableMap = new HashMap<String, String>();
	static {
//		nameMap = new HashMap<String, String>();
		tableMap.put("CECEP_CWYB01", "t_iufo_zcfz_65");
		tableMap.put("CECEP_CWYB05", "t_iufo_syzqy_65");
		tableMap.put("CECEP_CWYB02", "t_iufo_lr_65");
		tableMap.put("CECEP_CWYB03", "t_iufo_chash_65");
	}
	
	public static  Map<String, String>  getTableNameMap(){
		return tableMap;
	}


}
