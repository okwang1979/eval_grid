package nc.util.hbbb.input.hbreportdraft;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** 
 * һ������ǰ̨��ѯ������װ��
 * <b>Application name:</b>�Ϳ���Ŀ<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 ��������ɷ����޹�˾��Ȩ���С�<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-3-26<br>
 * @author����־ǿ
 * @version �Ϳ�
 */ 
public class LinkEndQueryVo implements Serializable{

	/**
	 * {�ֶι�������}
	 */
	private static final long serialVersionUID = 1180187157016535196L;
	
	/**
	 * �����¼�
	 */
	public static final String ORG_QUERY_TYPE_ALL = "all";
	
	/**
	 *  ĩ��
	 */
	public static final String ORG_QUERY_TYPE_END = "end";
	
	/**
	 * ֱ���¼�
	 */
	public static final String ORG_QUERY_TYPE_CHILD = "child";
	
	/**
	 * �ϲ�
	 */
	public static final String REPORT_VER_HBS ="Ver_hbs";
	
	/**
	 * �ϼ���
	 */
	public static final String REPORT_VER_HJS ="Ver_hjs";
	
	/**
	 * ������
	 */
	public static final String REPORT_VER_DXJ = "Ver_dxj";
	
	/**
	 * ������
	 */
	public static final String REPORT_VER_DXD = "Ver_dxd";
	
	/**
	 * �����
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
