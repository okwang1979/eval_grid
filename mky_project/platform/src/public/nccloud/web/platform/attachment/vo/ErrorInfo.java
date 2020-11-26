package nccloud.web.platform.attachment.vo;

import org.apache.commons.net.ftp.FTPClient;

public class ErrorInfo {

    private String errorCode;

    private String errorMsg;
    
    public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public ErrorInfo() {
    }

    public ErrorInfo(String errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorInfo(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }
}
