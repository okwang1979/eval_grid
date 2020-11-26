package nc.ui.iufo.repdatamng.actions;

import java.text.DecimalFormat;

/**
 * @author 王志强
 *外系统导入，信息展示info
 */
public class SysImpSendResultInfo  {
	
	private String report_name;
	
	private String info;
	
	private String state;
	
	private String errCode;
	
	private String messageInfo;
	
	
//	private String time;
	
	
	private long beginTime = 0;
	
	private long endTime = 0;
	
	public SysImpSendResultInfo(String report_name){
		
		this.report_name = report_name;
	}
	
	
	
	


	public String getReport_name() {
		return report_name;
	}






	public void setReport_name(String report_name) {
		this.report_name = report_name;
	}






	public String getState() {
		return state;
	}


	public void setState(String state) {
		this.state = state;
	}


	public String getErrCode() {
		return errCode;
	}


	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}


	public String getMessageInfo() {
		return messageInfo;
	}


	public void setMessageInfo(String messageInfo) {
		this.messageInfo = messageInfo;
	}

	
	

	public String getInfo() {
		return info;
	}


	public void setInfo(String info) {
		this.info = info;
	}


	public long getBeginTime() {
		return beginTime;
	}


	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}


	public long getEndTime() {
		return endTime;
	}


	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}


	public String getTime() {
		if(endTime==0){
			return "";
		}
		DecimalFormat decimalFormat = new DecimalFormat("#.00");
		return decimalFormat.format((endTime-beginTime)/1000)+"秒";
	}


	 
	
	

}
