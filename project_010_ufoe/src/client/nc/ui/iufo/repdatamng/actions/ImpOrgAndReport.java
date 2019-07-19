package nc.ui.iufo.repdatamng.actions;

import java.io.File;

import nc.vo.iuforeport.rep.ReportVO;

public class ImpOrgAndReport {
	
	
	 
	
	private File file;
	
	private String pk_org;
	
	/**
	 * 文件主体名称
	 */
	private String fileOrgName;
	
	/**
	 * 对照后主体名称
	 */
	private String sysOrgName;
	
	private boolean defOrgName;
	
	
	

 

 

	public String getFileOrgName() {
		return fileOrgName;
	}

	public void setFileOrgName(String fileOrgName) {
		this.fileOrgName = fileOrgName;
	}

	public boolean isDefOrgName() {
		return defOrgName;
	}

	public void setDefOrgName(boolean defOrgName) {
		this.defOrgName = defOrgName;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getPk_org() {
		return pk_org;
	}

	public void setPk_org(String pk_org) {
		this.pk_org = pk_org;
	}

	public String getSysOrgName() {
		return sysOrgName;
	}

	public void setSysOrgName(String sysOrgName) {
		this.sysOrgName = sysOrgName;
	}
	
 
}
