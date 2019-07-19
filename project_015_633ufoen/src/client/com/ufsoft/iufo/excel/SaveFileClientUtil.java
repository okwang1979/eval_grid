package com.ufsoft.iufo.excel;

import java.util.prefs.Preferences;

/*
 * 客户端保存文件相关的工具类
 */
public class SaveFileClientUtil {

	public static final String THIS_CLASS_NAME="com.ufsoft.iufo.excel.SaveFileClientUtil";
	
	public static final String EXP_DATA_PATH="exp_data_path";
	
	public static String getLastSelExpDataPath(){
		Preferences pref=Preferences.userRoot().node(THIS_CLASS_NAME);
		String lastPath=pref.get(EXP_DATA_PATH, "");
		return lastPath;
	}

	public static void putLastSelExpDataPath(String lastPath){
		Preferences pref=Preferences.userRoot().node(THIS_CLASS_NAME);
		pref.put(EXP_DATA_PATH, lastPath);
	}
	
}
