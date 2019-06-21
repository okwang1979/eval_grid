package nc.impl.hbbb.backplugin;

import com.ufsoft.table.CellPosition;

import nc.vo.pub.BusinessRuntimeException;

public class XJMeasureItem extends AbstractMeasureItem {

	/**
	 * 本期金额
	 */
	private XJMeasureInfo qmMeasure;

	/**
	 * 年初
	 */
	private XJMeasureInfo ncMeasure;
	/**
	 * 上年同期
	 */
	private XJMeasureInfo snMeasure;

	private static final String c1 = "本期金额";

	private static final String c2 = "本年累计金额";

	private static final String c3 = "上年累计金额";

	@Override
	public void addInfo(AbstractMeasureInfo info) {
		if (!canAddInfo(info)) {
			throw new BusinessRuntimeException("指标添加错误！");
		}
		if(getInfos().isEmpty()){
			this.setMeasureName(getInfoMeasureName(info));
		}
		

		if (info.getMeasure().toString().contains(c1)) {
			qmMeasure = (XJMeasureInfo) info;

		}
		if (info.getMeasure().toString().contains(c2)) {
			ncMeasure = (XJMeasureInfo) info;

		}
		if (info.getMeasure().toString().contains(c3)) {
			snMeasure = (XJMeasureInfo) info;

		}
		this.getInfos().add(info);

	}

	@Override
	public String getSql() {

		
		StringBuffer sb = new StringBuffer();
		sb.append("insert into t_etl_item_65(ITEM_CODE,ITEM_NAME,TABLE1,FIELD1,TABLE2,FIELD2,TABLE3,FIELD3,TABLE_NAME) values(");
//		sb.append(getSqlStringValue("CODE")).append(",");
		String code = "CODE";
		if(qmMeasure!=null){
			code = qmMeasure.getMeasure().getDbcolumn();
		}else if(ncMeasure!=null){
			code = ncMeasure.getMeasure().getDbcolumn();
		}else if(snMeasure!=null){
			code = ncMeasure.getMeasure().getDbcolumn();
		}
		
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
		if(snMeasure!=null){
			sb.append(getSqlStringValue(snMeasure.getMeasure().getDbtable())).append(",");
			sb.append(getSqlStringValue(snMeasure.getMeasure().getCode())).append(",");
		}else{
			sb.append("null").append(",");
			sb.append("null").append(",");
		}
		sb.append(getSqlStringValue(this.getTableName()));
		sb.append(");");
		return sb.toString();

	}

	
	@Override
	public boolean canAddInfo(AbstractMeasureInfo info) {
		if (!(info instanceof XJMeasureInfo)) {
			return false;
		}
		 
		if (this.getInfos().isEmpty()) {
			return true;
		}
		return info.getMeasurePoint().getRow()==this.getInfos().get(0).getMeasurePoint().getRow();
		 

	}

	private String getInfoMeasureName(AbstractMeasureInfo info) {
		String measure = info.getMeasure().toString().replaceAll(c1, "");
		measure = measure.replaceAll(c2, "");
		measure = measure.replaceAll(c3, "");
		return measure;
	}

	@Override
	public String getGroupKey() {

		XJMeasureInfo currentInfo = null;
		if(qmMeasure!=null){
			currentInfo = qmMeasure;
		}else if(ncMeasure!=null){
			currentInfo = ncMeasure;
		}
		else if(snMeasure!=null){
			currentInfo = snMeasure;
		}
		else{
			throw new BusinessRuntimeException("指标错误");
		}
		
		
		
		CellPosition cellPoint = currentInfo.getMeasurePoint();
		if(cellPoint.getColumn()==4){
			return "row:"+cellPoint.getRow()+"column:"+(cellPoint.getColumn()-2)+":"+cellPoint.getColumn();
			
		}else if(cellPoint.getColumn()==3){
			return "row:"+cellPoint.getRow()+"column:"+(cellPoint.getColumn()-1)+":"+(cellPoint.getColumn()+1);
		}else if(cellPoint.getColumn()==2){
			return "row:"+cellPoint.getRow()+"column:"+cellPoint.getColumn()+":"+(cellPoint.getColumn()+2);
		}
		else{
			throw new BusinessRuntimeException("位置错误");
		}
	}

}
