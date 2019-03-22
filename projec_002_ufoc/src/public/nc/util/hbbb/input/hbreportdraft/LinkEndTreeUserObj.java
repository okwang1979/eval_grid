package nc.util.hbbb.input.hbreportdraft;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.pub.lang.UFDouble;

/** 
 * һ��׷��TreeNode�ϵ�userObj��¼ÿ����������
 * <b>Application name:</b>�Ϳ���Ŀ<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 ��������ɷ����޹�˾��Ȩ���С�<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-3-20<br>
 * @author����־ǿ
 * @version �Ϳ�
 */ 
public class LinkEndTreeUserObj implements Serializable{
	
	public static final String TYPENAME_HBS ="�ϲ���";
	
	public static final String TYPENAME_HJS ="�ϼ���";
	
	
	public static final String TYPENAME_DXJ ="������";
	
	
	public static final String TYPENAME_DXD ="������";
	
	
	public static final String TYPENAME_GBB ="�����";
	
	private Integer ver;
	
	/**
	 * ÿһ�е�����
	 */
	private Map<MeasureVO,String> data = new HashMap<>();
 	
	
	/**
	 * ��������ʾ����
	 */
	private String orgDisName;
	
	/**
	 * �����������
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
