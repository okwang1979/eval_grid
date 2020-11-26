package nccloud.web.platform.attachment.vo;

import java.io.InputStream;

public class Vsftpd {
	/**
	             * 请求类型[必填]
	     * upload: 上传 文件已存在上传失败
	     * replace：上传 文件已存在则覆盖
	     * download: 下载
	     * display: 查看文件列表
	     * delete: 删除文件
     */
    private String optionType;
    /**
             * 请求者在接口管理系统中维护的项目编码[必填]
     */
    private String projectCode;
    /**
              * 上传/下载 文件名[非必填]
     */
    private String fileName;
    /**
             * 上传/下载 文件的字节数组[非必填]
     */
    private InputStream byteArry;
    
    /***
     * 合同编码
     */
    private String billId;
    
    /***
     * 附件分类
     */
    private String fullPath;
    
	public Vsftpd() {
		super();
		// TODO 自动生成的构造函数存根
	}
	public String getOptionType() {
		return optionType;
	}
	public void setOptionType(String optionType) {
		this.optionType = optionType;
	}
	public String getProjectCode() {
		return projectCode;
	}
	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public InputStream getByteArry() {
		return byteArry;
	}
	public void setByteArry(InputStream byteArry) {
		this.byteArry = byteArry;
	}
	public String getBillId() {
		return billId;
	}
	public void setBillId(String billId) {
		this.billId = billId;
	}
	public String getFullPath() {
		return fullPath;
	}
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
    
}
