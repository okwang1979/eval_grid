package nc.impl.hbbb.backplugin;

import com.ufsoft.table.CellPosition;

import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.pub.BusinessRuntimeException;

public class MeasureItemFactory {
	
	public static AbstractMeasureItem getMeasureInfo(String reportCode,IStoreCell info,CellPosition measurePoint){
		if(info.getDbtable()==null||info.getDbcolumn()==null){
			return null;
		}
		if(reportCode.equals(ReportImportConst.reportCodes[0])){
			if("资产负债表版本".equals(info.toString())){
				return null;
			}
			ZCMeasureItem item = new ZCMeasureItem();
			item.setTableName(ReportImportConst.getNameMap().get(reportCode));
			
			AbstractMeasureInfo mInfo = new ZCMeasureInfo(info,measurePoint);
			if(item.canAddInfo(mInfo)){
				item.addInfo(mInfo);
				return item;
			}
			return null;
			
		}else if(reportCode.equals(ReportImportConst.reportCodes[2])){
			if("利润表版本".equals(info.toString())){
				return null;
			}
			LRMeasureItem item = new LRMeasureItem();
			item.setTableName(ReportImportConst.getNameMap().get(reportCode));
			LRMeasureInfo mInfo = new LRMeasureInfo(info,measurePoint);
			item.addInfo(mInfo);
			return item;
		}else if(reportCode.equals(ReportImportConst.reportCodes[3])){
			if("现金流量表版本".equals(info.toString())){
				return null;
			}
			if("A63".equals(info.toString())){
				return null;
			}
			XJMeasureItem item = new XJMeasureItem();
			item.setTableName(ReportImportConst.getNameMap().get(reportCode));
			XJMeasureInfo mInfo = new XJMeasureInfo(info,measurePoint);
			item.addInfo(mInfo);
			return item;
		}else if(reportCode.equals(ReportImportConst.reportCodes[1])){
			 
			QYMeasureItem item = new QYMeasureItem();
			item.setTableName(ReportImportConst.getNameMap().get(reportCode));
			 
			
			AbstractMeasureInfo mInfo = new QYMeasureInfo(info,measurePoint);
			if(item.canAddInfo(mInfo)){
				item.addInfo(mInfo);
				return item;
			}
			return null;
		}
			return null;
			//throw new BusinessRuntimeException("错误报表类型！");
		 
		
		
		
	}

}
