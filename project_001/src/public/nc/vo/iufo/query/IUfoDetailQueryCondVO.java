package nc.vo.iufo.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.pub.tools.DeepCopyUtilities;

import nc.vo.iufo.param.RefPanelParam;

public class IUfoDetailQueryCondVO implements Serializable, Cloneable {
	private static final long serialVersionUID = -5271955151224861865L;

	private String keyGroupPK=null;
	private String[] orgPKs=null;
	private String[] repPKs=null;
	private String[] taskPKs=null;

	private int orgType=IUfoQueryCondVO.ORGTYPE_SELF;
	private String[] filterRepPKs=null;
	private String[] filterTaskPKs=null;

	private int inputState=IUfoQueryCondVO.INT_EMPTY_NOSELECT;
	private String inputPerson=IUfoQueryCondVO.STR_EMPTY_NOSELECT;
	private String inputDate=IUfoQueryCondVO.STR_EMPTY_NOSELECT;

	private int repCommitState=IUfoQueryCondVO.INT_EMPTY_NOSELECT;
	private int taskCommitState=IUfoQueryCondVO.INT_EMPTY_NOSELECT;

	private int reqBackFlag=IUfoQueryCondVO.INT_EMPTY_NOSELECT;
	private String reqBackPerson=IUfoQueryCondVO.STR_EMPTY_NOSELECT;
	private String reqBackDate=IUfoQueryCondVO.STR_EMPTY_NOSELECT;

	private int hastenFlag=IUfoQueryCondVO.INT_EMPTY_NOSELECT;
	private String hastenPerson=IUfoQueryCondVO.STR_EMPTY_NOSELECT;
	private String hastenDate=IUfoQueryCondVO.STR_EMPTY_NOSELECT;

	private int repCheckState=IUfoQueryCondVO.INT_EMPTY_NOSELECT;
	private int taskCheckState=IUfoQueryCondVO.INT_EMPTY_NOSELECT;

	private String beginDate=IUfoQueryCondVO.STR_EMPTY_NOSELECT;
	private String endDate=IUfoQueryCondVO.STR_EMPTY_NOSELECT;
	private String date=IUfoQueryCondVO.STR_EMPTY_NOSELECT;

	private int mustInputFlag=IUfoQueryCondVO.INT_EMPTY_NOSELECT;
	private int mustCommitFlag=IUfoQueryCondVO.INT_EMPTY_NOSELECT;

	private String assignTaskOrg=IUfoQueryCondVO.STR_EMPTY_NOSELECT;
	private int repDataRight=IUfoQueryCondVO.INT_EMPTY_NOSELECT;

	private Map<String,String> hashKeyVal=new HashMap<String,String>();

	// @edit by wuyongc at 2011-12-27,上午9:00:55
	private Map<String,RefPanelParam> keyRefVal = new HashMap<String,RefPanelParam>();

	//选中的组织PK数
	private String[] selectedOrgPKs;

	@Override
	@SuppressWarnings("unchecked")
	public Object clone(){
		IUfoDetailQueryCondVO o = null;
		try {
			o =(IUfoDetailQueryCondVO) super.clone();
			o.hashKeyVal=DeepCopyUtilities.getDeepCopy(hashKeyVal);
			o.keyRefVal=DeepCopyUtilities.getDeepCopy(keyRefVal);
			if(o.keyRefVal == null)//数据库中旧的记录导致keyRefVal 会为Null
				o.keyRefVal =  new HashMap<String,RefPanelParam>();
		} catch (CloneNotSupportedException e) {
			AppDebug.debug("clone not supported!");
		}
		return o;
	}

	public String getKeyGroupPK() {
		return keyGroupPK;
	}

	public void setKeyGroupPK(String keyGroupPK) {
		this.keyGroupPK = keyGroupPK;
	}

	public String[] getOrgPKs() {
		return orgPKs;
	}

	public void setOrgPKs(String[] orgPKs) {
		this.orgPKs = orgPKs;
	}

	public String[] getRepPKs() {
		return repPKs;
	}

	public void setRepPKs(String[] repPKs) {
		this.repPKs = repPKs;
	}

	public String[] getTaskPKs() {
		return taskPKs;
	}

	public void setTaskPKs(String[] taskPKs) {
		this.taskPKs = taskPKs;
	}

	public int getRepCommitState() {
		return repCommitState;
	}

	public void setRepCommitState(int repCommitState) {
		this.repCommitState = repCommitState;
	}

	public int getTaskCommitState() {
		return taskCommitState;
	}

	public void setTaskCommitState(int taskCommitState) {
		this.taskCommitState = taskCommitState;
	}

	public int getRepCheckState() {
		return repCheckState;
	}

	public void setRepCheckState(int repCheckState) {
		this.repCheckState = repCheckState;
	}

	public int getTaskCheckState() {
		return taskCheckState;
	}

	public void setTaskCheckState(int taskCheckState) {
		this.taskCheckState = taskCheckState;
	}

	public int getInputState() {
		return inputState;
	}

	public void setInputState(int inputState) {
		this.inputState = inputState;
	}

	public String getInputPerson() {
		return inputPerson;
	}

	public void setInputPerson(String inputPerson) {
		this.inputPerson = inputPerson;
	}

	public int getReqBackFlag() {
		return reqBackFlag;
	}

	public void setReqBackFlag(int reqBackFlag) {
		this.reqBackFlag = reqBackFlag;
	}

	public int getHastenFlag() {
		return hastenFlag;
	}

	public void setHastenFlag(int hastenFlag) {
		this.hastenFlag = hastenFlag;
	}

	public String getHastenPerson() {
		return hastenPerson;
	}

	public void setHastenPerson(String hasterPerson) {
		hastenPerson = hasterPerson;
	}

	public int getMustInputFlag() {
		return mustInputFlag;
	}

	public void setMustInputFlag(int mustInputFlag) {
		this.mustInputFlag = mustInputFlag;
	}

	public int getMustCommitFlag() {
		return mustCommitFlag;
	}

	public void setMustCommitFlag(int mustCommitFlag) {
		this.mustCommitFlag = mustCommitFlag;
	}

	public String getInputDate() {
		return inputDate;
	}

	public void setInputDate(String inputDate) {
		this.inputDate = inputDate;
	}

	public String getReqBackPerson() {
		return reqBackPerson;
	}

	public void setReqBackPerson(String reqBackPerson) {
		this.reqBackPerson = reqBackPerson;
	}

	public String getReqBackDate() {
		return reqBackDate;
	}

	public void setReqBackDate(String reqBackDate) {
		this.reqBackDate = reqBackDate;
	}

	public String getHastenDate() {
		return hastenDate;
	}

	public void setHastenDate(String hastenDate) {
		this.hastenDate = hastenDate;
	}

	public String getAssignTaskOrg() {
		return assignTaskOrg;
	}

	public void setAssignTaskOrg(String assignTaskOrg) {
		this.assignTaskOrg = assignTaskOrg;
	}

	public int getRepDataRight() {
		return repDataRight;
	}

	public void setRepDataRight(int repDataRight) {
		this.repDataRight = repDataRight;
	}

	public String getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(String beginDate) {
		this.beginDate = beginDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setKeyVal(String strKeyPK,String strKeyVal){
		if (strKeyVal==null)
			removeKeyVal(strKeyPK);
		else
			hashKeyVal.put(strKeyPK, strKeyVal);
	}

	void removeKeyVal(String strKeyPK){
		hashKeyVal.remove(strKeyPK);
	}

	public String getKeyVal(String strKeyPK){
		return hashKeyVal.get(strKeyPK);
	}

	public void setKeyRefVal(String strKeyPK,RefPanelParam param){
		if(strKeyPK == null)
			removeKeyRefVal(strKeyPK);
		else
			keyRefVal.put(strKeyPK, param);
	}

	void removeKeyRefVal(String strKeyPK){
		keyRefVal.remove(strKeyPK);
	}

	public RefPanelParam getKeyRefVal(String strKeyPK){
		return keyRefVal.get(strKeyPK);
	}
	public int getOrgType() {
		return orgType;
	}


	public void setOrgType(int orgType) {
		this.orgType = orgType;
	}

	public String[] getFilterRepPKs() {
		return filterRepPKs;
	}

	public void setFilterRepPKs(String[] filterRepPKs) {
		this.filterRepPKs = filterRepPKs;
	}

	public String[] getFilterTaskPKs() {
		return filterTaskPKs;
	}

	public void setFilterTaskPKs(String[] filterTaskPKs) {
		this.filterTaskPKs = filterTaskPKs;
	}

	/**
	 * @return the selectedOrgPKs
	 */
	public String[] getSelectedOrgPKs() {
		return selectedOrgPKs;
	}

	/**
	 * @param selectedOrgPKs the selectedOrgPKs to set
	 */
	public void setSelectedOrgPKs(String[] selectedOrgPKs) {
		this.selectedOrgPKs = selectedOrgPKs;
	}


}
