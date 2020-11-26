package nc.lightapp.framework.web.action.attachment;

import java.io.Serializable;
import java.util.Map;
import nccloud.framework.core.io.WebFile;

public class AttachmentVO implements Serializable{
	private String billId;
	private WebFile[] files;
	private Map<String, String[]> parameters;

	public String getBillId() {
		return this.billId;
	}

	public WebFile[] getFiles() {
		return this.files;
	}

	public Map<String, String[]> getParameters() {
		return this.parameters;
	}

	public void setBillId(String billId) {
		this.billId = billId;
	}

	public void setFiles(WebFile[] files) {
		this.files = files;
	}

	public void setParameters(Map<String, String[]> parameters) {
		this.parameters = parameters;
	}

}