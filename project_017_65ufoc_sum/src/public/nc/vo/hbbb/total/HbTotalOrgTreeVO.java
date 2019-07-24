package nc.vo.hbbb.total;

import java.io.Serializable;

/**
 * 报表合并体系查询VO
 *
 */
public class HbTotalOrgTreeVO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String pk_org;
	
	
	private String pk_fatherorg;
	
	
	
//	select  t1.pk_org,t1.code,t1.name,t2.innercode,t2.pk_rcs,t2.pk_svid  from org_orgs t1
//	on org_rcsmember_v t2 on t1.pk_org=t2.pk_org and t2.pk_rcs =? and   t2.pk_svid =?
//	order by   t2.idx 
	
	private String code;
	private String name;
	private String innercode;
	private String pk_rcs;//报表合并体系主键
	private String pk_svid;//报表合并体系版本
	
	private HbTotalSchemeVO scheme;

	
	
	
	public String getPk_fatherorg() {
		return pk_fatherorg;
	}

	public void setPk_fatherorg(String pk_fatherorg) {
		this.pk_fatherorg = pk_fatherorg;
	}

	public String getPk_org() {
		return pk_org;
	}

	public void setPk_org(String pk_org) {
		this.pk_org = pk_org;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInnercode() {
		return innercode;
	}

	public void setInnercode(String innercode) {
		this.innercode = innercode;
	}

	public String getPk_rcs() {
		return pk_rcs;
	}

	public void setPk_rcs(String pk_rcs) {
		this.pk_rcs = pk_rcs;
	}

	public String getPk_svid() {
		return pk_svid;
	}

	public void setPk_svid(String pk_svid) {
		this.pk_svid = pk_svid;
	}

	public HbTotalSchemeVO getScheme() {
		return scheme;
	}

	public void setScheme(HbTotalSchemeVO scheme) {
		this.scheme = scheme;
	}

	@Override
	public String toString() {
		 
		return this.name;
	}
	
	
	
	

}
