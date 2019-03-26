package nc.util.hbbb.input.hbreportdraft;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** 
 * 一键联查前台查询条件封装类
 * <b>Application name:</b>客开项目<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 用友软件股份有限公司版权所有。<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-3-26<br>
 * @author：王志强
 * @version 客开
 */ 
public class LinkEndQueryVo implements Serializable{

	/**
	 * {字段功能描述}
	 */
	private static final long serialVersionUID = 1180187157016535196L;
	
	/**
	 * 所有下级
	 */
	public static final String ORG_QUERY_TYPE_ALL = "all";
	
	/**
	 *  末级
	 */
	public static final String ORG_QUERY_TYPE_END = "end";
	
	/**
	 * 直接下级
	 */
	public static final String ORG_QUERY_TYPE_CHILD = "child";
	
	/**
	 * 合并
	 */
	public static final String REPORT_VER_HBS ="Ver_hbs";
	
	/**
	 * 合计数
	 */
	public static final String REPORT_VER_HJS ="Ver_hjs";
	
	/**
	 * 抵消借
	 */
	public static final String REPORT_VER_DXJ = "Ver_dxj";
	
	/**
	 * 抵消贷
	 */
	public static final String REPORT_VER_DXD = "Ver_dxd";
	
	/**
	 * 个别表
	 */
	public static final String REPORT_VER_GBB = "Ver_gbb";
	
	
	private String orgQueryType = ORG_QUERY_TYPE_ALL;
	
	
	private Set<String> queryRepVers = new HashSet<>();


	public LinkEndQueryVo(String orgQueryType,Collection<String> queryRepVers){
		this.orgQueryType= orgQueryType;
		
		this.queryRepVers.clear();
		this.queryRepVers.addAll(queryRepVers);
	}
	
	public String getOrgQueryType() {
		return orgQueryType;
	}


	public void setOrgQueryType(String orgQueryType) {
		this.orgQueryType = orgQueryType;
	}


	public Set<String> getQueryRepVers() {
		return queryRepVers;
	}


	public void setQueryRepVers(Set<String> queryRepVers) {
		this.queryRepVers = queryRepVers;
	}
	
	
	
	

}
