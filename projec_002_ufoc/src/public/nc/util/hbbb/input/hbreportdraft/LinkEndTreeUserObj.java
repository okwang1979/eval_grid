package nc.util.hbbb.input.hbreportdraft;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.pub.lang.UFDouble;

/** 
 * 一键追踪TreeNode上的userObj记录每行数据内容
 * <b>Application name:</b>客开项目<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 用友软件股份有限公司版权所有。<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-3-20<br>
 * @author：王志强
 * @version 客开
 */ 
public class LinkEndTreeUserObj implements Serializable{
	
	public static final String TYPENAME_HBS ="合并数";
	
	public static final String TYPENAME_HJS ="合计数";
	
	
	public static final String TYPENAME_DXJ ="抵消借";
	
	
	public static final String TYPENAME_DXD ="抵消贷";
	
	
	public static final String TYPENAME_GBB ="个别表";
	
	private Integer ver;
	
	/**
	 * 每一行的数据
	 */
	private Map<MeasureVO,String> data = new HashMap<>();
 	
	
	/**
	 * 主体列显示内容
	 */
	private String orgDisName;
	
	/**
	 * 报表类别名称
	 */
	private String reportTypeName;
	
	
	public void addMeasureData(MeasureDataVO md){
		data.put((MeasureVO) md.getMeasureVO(), md.getDataValue());
		
	}
	
	

 
//	public void addMeasure(MeasureVO m){
//		data.put(m, "");
//		
//	}

	public Integer getVer() {
		return ver;
	}




	public void setVer(Integer ver) {
		this.ver = ver;
	}




	public void addMeasures(MeasureVO... measures){
		 for(MeasureVO m:measures){
			 data.put(m,"");
		 }
		
	}

	public Map<MeasureVO, String> getData() {
		return data;
	}




//
//	public void setData(Map<MeasureVO, String> data) {
//		this.data = data;
//	}





	public String getReportTypeName() {
		return reportTypeName;
	}





	public void setReportTypeName(String reportTypeName) {
		this.reportTypeName = reportTypeName;
	}





	public String getOrgDisName() {
		return orgDisName;
	}


	public void setOrgDisName(String orgDisName) {
		this.orgDisName = orgDisName;
	}
	
	
	
	

}
