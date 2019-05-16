package nc.impl.hbbb.backplugin;

import java.util.ArrayList;
import java.util.List;

import nc.vo.iufo.storecell.IStoreCell;

/** 
 * ��ѯ��װָ����
 * <b>Application name:</b>�Ϳ���Ŀ<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 ��������ɷ����޹�˾��Ȩ���С�<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-4-30<br>
 * @author����־ǿ
 * @version �Ϳ�
 */ 
public abstract class AbstractMeasureItem {
	
	/**
	 * ָ������
	 */
	private String measureName;
	/**
	 * ָ�����
	 */
	private String measureCode;
	
	/**
	 * ָ���
	 */
	private String tableName;
	
	private List<AbstractMeasureInfo> infos = new ArrayList<AbstractMeasureInfo>();
	
	 
	
	public abstract boolean canAddInfo(AbstractMeasureInfo info);
	

	public abstract void addInfo(AbstractMeasureInfo info);
	
	public abstract String getGroupKey();
	 

	public String getMeasureName() {
		return measureName;
	}
	
	public  String getSqlStringValue(Object param){
		if(param==null){
			return null;
		}else{
			return "'"+String.valueOf(param)+"'";
		}
	}

	public void setMeasureName(String measureName) {
		this.measureName = measureName;
	}

	public String getMeasureCode() {
		return measureCode;
	}

	public void setMeasureCode(String measureCode) {
		this.measureCode = measureCode;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<AbstractMeasureInfo> getInfos() {
		return infos;
	}

	public void setInfos(List<AbstractMeasureInfo> infos) {
		this.infos = infos;
	}
	
	
	public abstract  String getSql();
	
	 

	@Override
	public int hashCode() {
		return measureName.hashCode()*37+tableName.hashCode();
	}
	
	

	@Override
	public boolean equals(Object obj) {
		 if(obj instanceof AbstractMeasureItem){
			 AbstractMeasureItem item = (AbstractMeasureItem)obj;
			 return item.getMeasureName().equals(this.getMeasureName())&&item.getTableName().equals(this.getTableName());
		 }
		return super.equals(obj);
	}
	
	

}
