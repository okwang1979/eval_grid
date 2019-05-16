package nc.impl.hbbb.backplugin;

import com.ufsoft.table.CellPosition;

import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.storecell.IStoreCell;

/** 
 * ����ָ��
 * <b>Application name:</b>�Ϳ���Ŀ<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 ��������ɷ����޹�˾��Ȩ���С�<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-4-30<br>
 * @author����־ǿ
 * @version �Ϳ�
 */ 
public abstract class AbstractMeasureInfo {
	
	private CellPosition measurePoint;
	 
	
	
	
	private IStoreCell measure;
	
	public AbstractMeasureInfo(IStoreCell measure,CellPosition measurePoint){
		this.measure = measure;
		this.measurePoint = measurePoint;
		
	}
	
	
	

	public IStoreCell getMeasure() {
		return measure;
	}

	public void setMeasure(IStoreCell measure) {
		this.measure = measure;
	}




	public CellPosition getMeasurePoint() {
		return measurePoint;
	}




	public void setMeasurePoint(CellPosition measurePoint) {
		this.measurePoint = measurePoint;
	}
	

	
	
	
	
	
	
	
	

}
