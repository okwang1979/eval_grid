package nccloud.web.platform.attachment.vo;

public class AttachPathVo {
	//文件名
     private String fileName;
     //公司编码
     private String compCode;
     //合同编码
     private String ctCode;
     //日期
     private String yearMonthStr;
     //附件类型
     private String attachType;
     
	public AttachPathVo() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getCompCode() {
		return compCode;
	}
	public void setCompCode(String compCode) {
		this.compCode = compCode;
	}
	public String getCtCode() {
		return ctCode;
	}
	public void setCtCode(String ctCode) {
		this.ctCode = ctCode;
	}
	public String getYearMonthStr() {
		return yearMonthStr;
	}
	public void setYearMonthStr(String yearMonthStr) {
		this.yearMonthStr = yearMonthStr;
	}
	public String getAttachType() {
		return attachType;
	}
	public void setAttachType(String attachType) {
		this.attachType = attachType;
	}
     
}
