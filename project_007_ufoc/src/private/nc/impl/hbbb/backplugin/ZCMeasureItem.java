package nc.impl.hbbb.backplugin;

import com.ufsoft.table.CellPosition;

import nc.vo.pub.BusinessRuntimeException;

public class ZCMeasureItem extends AbstractMeasureItem {

	/**
	 * 期末金额
	 */
	private ZCMeasureInfo qmMeasure;

	/**
	 * 年初
	 */
	private ZCMeasureInfo ncMeasure;
	/**
	 * 上年同期
	 */
//	private ZCMeasureInfo snMeasure;

	private static final String c1 = "期末金额";

	private static final String c2 = "年初金额";

//	private static final String c3 = "上年同期金额";

	@Override
	public void addInfo(AbstractMeasureInfo info) {
		if (!canAddInfo(info)) {
			throw new BusinessRuntimeException("指标添加错误！");
		}
		if(getInfos().isEmpty()){
			this.setMeasureName(getInfoMeasureName(info));
		}
		

		if (info.getMeasure().toString().contains(c1)) {
			qmMeasure = (ZCMeasureInfo) info;

		}
		if (info.getMeasure().toString().contains(c2)) {
			ncMeasure = (ZCMeasureInfo) info;

		}
//		if (info.getMeasure().toString().contains(c3)) {
//			snMeasure = (ZCMeasureInfo) info;
//
//		}
		this.getInfos().add(info);

	}

	@Override
	public String getSql() {

		
		StringBuffer sb = new StringBuffer();
		sb.append("insert into t_etl_item(ITEM_CODE,ITEM_NAME,TABLE1,FIELD1,TABLE2,FIELD2,TABLE_NAME) values(");
		String code = "CODE";
		if(qmMeasure!=null){
			code = qmMeasure.getMeasure().getDbcolumn();
		}else if(ncMeasure!=null){
			code = ncMeasure.getMeasure().getDbcolumn();
		}
//		else if(snMeasure!=null){
//			code = snMeasure.getMeasure().getDbcolumn();
//		}
		
		sb.append(getSqlStringValue(code)).append(",");
		sb.append(getSqlStringValue(this.getMeasureName())).append(",");
	 
		if(qmMeasure!=null){
			sb.append(getSqlStringValue(qmMeasure.getMeasure().getDbtable())).append(",");
			sb.append(getSqlStringValue(qmMeasure.getMeasure().getCode())).append(",");
		}else{
			sb.append("null").append(",");
			sb.append("null").append(",");
		}
		if(ncMeasure!=null){
			sb.append(getSqlStringValue(ncMeasure.getMeasure().getDbtable())).append(",");
			sb.append(getSqlStringValue(ncMeasure.getMeasure().getCode())).append(",");
		}else{
			sb.append("null").append(",");
			sb.append("null").append(",");
		}
//		if(snMeasure!=null){
//			sb.append(getSqlStringValue(snMeasure.getMeasure().getDbtable())).append(",");
//			sb.append(getSqlStringValue(snMeasure.getMeasure().getDbcolumn())).append(",");
//		}else{
//			sb.append("null").append(",");
//			sb.append("null").append(",");
//		}
		sb.append(getSqlStringValue(this.getTableName()));
		sb.append(");");
		return sb.toString();

	}

	
	@Override
	public boolean canAddInfo(AbstractMeasureInfo info) {
		if (!(info instanceof ZCMeasureInfo)) {
			return false;
		}
		if (this.getInfos().isEmpty()) {
			return info.getMeasurePoint().getColumn()==2||info.getMeasurePoint().getColumn()==3||info.getMeasurePoint().getColumn()==7||info.getMeasurePoint().getColumn()==8;
			 
		}

	 
		boolean rowEqualse = info.getMeasurePoint().getRow()==this.getInfos().get(0).getMeasurePoint().getRow();
		if(rowEqualse){
			if( info.getMeasurePoint().getColumn()==2){
				return this.getInfos().get(0).getMeasurePoint().getColumn()==3;
			}
			if( info.getMeasurePoint().getColumn()==3){
				return this.getInfos().get(0).getMeasurePoint().getColumn()==2;
			}
			if( info.getMeasurePoint().getColumn()==7){
				return this.getInfos().get(0).getMeasurePoint().getColumn()==8;
			}
			if( info.getMeasurePoint().getColumn()==8){
				return this.getInfos().get(0).getMeasurePoint().getColumn()==7;
			}
		}
		 return false;

	}

	private String getInfoMeasureName(AbstractMeasureInfo info) {
		String measure = info.getMeasure().toString().replaceAll(c1, "");
		measure = measure.replaceAll(c2, "");
//		measure = measure.replaceAll(c3, "");
		return measure;
	}

	@Override
	public String getGroupKey() {
		ZCMeasureInfo currentInfo = null;
		if(qmMeasure!=null){
			currentInfo = qmMeasure;
		}else if(ncMeasure!=null){
			currentInfo = ncMeasure;
		}else{
			throw new BusinessRuntimeException("指标错误");
		}
		CellPosition cellPoint = currentInfo.getMeasurePoint();
		if(cellPoint.getColumn()==3||cellPoint.getColumn()==8){
			return "row:"+cellPoint.getRow()+"column:"+(cellPoint.getColumn()-1)+":"+cellPoint.getColumn();
			
		}else if(cellPoint.getColumn()==2||cellPoint.getColumn()==7){
			return "row:"+cellPoint.getRow()+"column:"+cellPoint.getColumn()+":"+(cellPoint.getColumn()+1);
		}else{
			throw new BusinessRuntimeException("位置错误");
		}
		 
	}

}
