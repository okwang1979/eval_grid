package nc.vo.pub.param;

import nc.vo.pub.SuperVO;

/**
 * 参数VO
 * @author xuink
 *
 */
public class TempParamVO extends SuperVO{
	
 	private static final long serialVersionUID = -8676125388431930373L;
	private String pk_org;
	private String pk_group;
	private String pk_currency;//币种
	private String period;//日期
	private String langCode;
	private String curRmsPK;//报表组织体系
	private String taskid;//任务主键
	private String[] inputKeys;//关键字值
	private String[] repPks;//报表
	private String userid;
	private String taskcode;//任务code
	
	public String getTaskcode() {
		return taskcode;
	}
	public void setTaskcode(String taskcode) {
		this.taskcode = taskcode;
	}
	public String getPk_org() {
		return pk_org;
	}
	public void setPk_org(String pk_org) {
		this.pk_org = pk_org;
	}
	public String getPk_group() {
		return pk_group;
	}
	public void setPk_group(String pk_group) {
		this.pk_group = pk_group;
	}
	public String getPk_currency() {
		return pk_currency;
	}
	public void setPk_currency(String pk_currency) {
		this.pk_currency = pk_currency;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public String getLangCode() {
		return langCode;
	}
	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}
	public String getCurRmsPK() {
		return curRmsPK;
	}
	public void setCurRmsPK(String curRmsPK) {
		this.curRmsPK = curRmsPK;
	}
	public String getTaskid() {
		return taskid;
	}
	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}
	public String[] getInputKeys() {
		return inputKeys;
	}
	public void setInputKeys(String[] inputKeys) {
		this.inputKeys = inputKeys;
	}
	public String[] getRepPks() {
		return repPks;
	}
	public void setRepPks(String[] repPks) {
		this.repPks = repPks;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
}
