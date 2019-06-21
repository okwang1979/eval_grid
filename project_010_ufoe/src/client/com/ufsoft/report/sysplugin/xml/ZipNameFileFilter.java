package com.ufsoft.report.sysplugin.xml;

import java.io.File;

/**
 * 导入弹框文件类型
 * @author pzm
 *
 */

public class ZipNameFileFilter extends javax.swing.filechooser.FileFilter {
	private String _extendName;

	public ZipNameFileFilter(String extendName) {
		_extendName = extendName;
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String extension = null;
		String name = f.getName();
		int pos = name.lastIndexOf('.');
		if ((pos > 0) && (pos < name.length() - 1)) {
			extension = name.substring(pos + 1);
		}
		if ((extension != null) && (extension.equalsIgnoreCase(_extendName))) {
			return true;
		}
		return false;
	}

	public String getDescription() {
		if (getExtendName().equalsIgnoreCase("zip")) {
			return "ZIP"	+ " (*.zip)";
		}
		return "";
	}

	public String getExtendName() {
		return _extendName;
	}

	public File getModifiedFile(File file) {
		String pathName = file.getPath();
		int pos = pathName.lastIndexOf('.');
		if (pos > 0) {
			String extName = pathName.substring(pos + 1);
			if (extName.equalsIgnoreCase(_extendName)) {
				return file;
			}
		}
		pathName = pathName + ".";
		pathName = pathName + _extendName;
		return new File(pathName);
	}
}
