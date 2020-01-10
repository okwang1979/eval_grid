package nc.impl.hbbb.backplugin;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nc.bs.framework.common.RuntimeEnv;
import nc.bs.logging.Logger;
import nc.vo.pub.BusinessException;

public class PropertyTool {

 
	public static final String DBFILE = "dbfile_esb";

	private PropertyTool() {
	}

	public static String GetValueByKey(String name,String key) throws BusinessException {
		String filepath = RuntimeEnv.getInstance().getNCHome() + "/resources/" + name + ".properties";
		Properties pps = getProperties(filepath);
		String value = pps.getProperty(key);
		if (value == null) {
 
		}
		return value;
	}

	public static Set<Map.Entry<Object, Object>> getAllProperties(String name) throws BusinessException {
		String filepath = RuntimeEnv.getInstance().getNCHome() + "/resources/" + name + ".properties";
		Properties pps = getProperties(filepath);
		Set<Map.Entry<Object, Object>> entrySet = pps.entrySet();
		return entrySet;
	}

	private static Properties getProperties(String filepath) throws BusinessException {
		Properties properties = new Properties();
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(filepath));
			properties.load(in);
		} catch (Exception e) {
		 
			Logger.error(e.getMessage(), e);
	 
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new BusinessException(e);
				}
			}
		}
		return properties;
	}
}