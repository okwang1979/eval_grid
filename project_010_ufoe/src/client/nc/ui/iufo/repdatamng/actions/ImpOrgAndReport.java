package nc.ui.iufo.repdatamng.actions;

import java.io.File;

import nc.vo.iuforeport.rep.ReportVO;

public class ImpOrgAndReport {
	
	private ReportVO  report;
	
	private String repName;
	
	private File file;
	
	private String pk_org;
	
	private String sysOrgName;

	public ReportVO getReport() {
		return report;
	}

	public void setReport(ReportVO report) {
		this.report = report;
	}

	public String getRepName() {
		return repName;
	}

	public void setRepName(String repName) {
		this.repName = repName;
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
