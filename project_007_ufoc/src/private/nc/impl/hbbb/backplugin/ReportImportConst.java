package nc.impl.hbbb.backplugin;

import java.util.HashMap;
import java.util.Map;

public final class ReportImportConst {
	public static  String[] reportCodes = { "CECEP_CWYB01", "CECEP_CWYB05",
			"CECEP_CWYB02", "CECEP_CWYB03" };
	
//	public static String OTHER_DATASOURCE = "ESB";
	
	
	
 
	// �ʲ���ծ�� ������Ȩ��� ����� �ֽ�����
	private static Map<String, String> nameMap = new HashMap<String, String>();
	static {
//		nameMap = new HashMap<String, String>();
		nameMap.put("CECEP_CWYB01", "65�ʲ���ծ��");
		nameMap.put("CECEP_CWYB05", "65������Ȩ���");
		nameMap.put("CECEP_CWYB02", "65�����");
		nameMap.put("CECEP_CWYB03", "65�ֽ�������");
	}
	
	public static  Map<String, String>  getNameMap(){
		return nameMap;
	}
	
	
	// �ʲ���ծ�� ������Ȩ��� ����� �ֽ�����
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
