package nc.impl.hbbb.backplugin;

import nc.vo.pub.BusinessRuntimeException;

public class QYMeasureItem extends AbstractMeasureItem {
	/**
	 * 期末金额
	 */
	private QYMeasureInfo qmMeasure;




	@Override
	public void addInfo(AbstractMeasureInfo info) {
		if (!canAddInfo(info)) {
			throw new BusinessRuntimeException("指标添加错误！");
		}
		if(getInfos().isEmpty()){
			this.setMeasureName(getInfoMeasureName(info));
			qmMeasure = (QYMeasureInfo)info;
			this.getInfos().add(info);
		}
		

	

	}

	@Override
	public String getSql() {

		
		StringBuffer sb = new StringBuffer();
		sb.append("insert into t_etl_item(ITEM_CODE,ITEM_NAME,TABLE1,FIELD1,TABLE_NAME) values(");
		String code = "CODE";
		if(qmMeasure!=null){
			code = qmMeasure.getMeasure().getDbcolumn();
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
		
		sb.append(getSqlStringValue(this.getTableName()));
		sb.append(");");
		return sb.toString();

	}

	
	@Override
	public boolean canAddInfo(AbstractMeasureInfo info) {
		if (!(info instanceof QYMeasureInfo)) {
			return false;
		}
		if (this.getInfos().isEmpty()) {
			return info.getMeasurePoint().getColumn()<=16;
		}

		  return false;

	}

	private String getInfoMeasureName(AbstractMeasureInfo info) {
		return info.getMeasure().toString();
	}

	@Override
	public String getGroupKey() {
		 if(qmMeasure!=null){
			 return "row："+qmMeasure.getMeasurePoint().getRow()+"column:"+qmMeasure.getMeasurePoint().getColumn();
		 }else{
			 throw new BusinessRuntimeException("所有者权益无对应指标");
		 }
	}

}
