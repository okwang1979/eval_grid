package nccloud.web.platform.attachment.vo;

import java.io.InputStream;

public class Vsftpd {
	/**
	             * ��������[����]
	     * upload: �ϴ� �ļ��Ѵ����ϴ�ʧ��
	     * replace���ϴ� �ļ��Ѵ����򸲸�
	     * download: ����
	     * display: �鿴�ļ��б�
	     * delete: ɾ���ļ�
     */
    private String optionType;
    /**
             * �������ڽӿڹ���ϵͳ��ά������Ŀ����[����]
     */
    private String projectCode;
    /**
              * �ϴ�/���� �ļ���[�Ǳ���]
     */
    private String fileName;
    /**
             * �ϴ�/���� �ļ����ֽ�����[�Ǳ���]
     */
    private InputStream byteArry;
    
    /***
     * ��ͬ����
     */
    private String billId;
    
    /***
     * ��������
     */
    private String fullPath;
    
	public Vsftpd() {
		super();
		// TODO �Զ����ɵĹ��캯�����
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
