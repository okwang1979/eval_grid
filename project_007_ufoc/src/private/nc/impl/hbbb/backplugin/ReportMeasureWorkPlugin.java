package nc.impl.hbbb.backplugin;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.ufida.dataset.Context;
import com.ufida.dataset.IContext;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.measure.MeasureModel;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;

import nc.bs.logging.Logger;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.pa.PreAlertReturnType;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.itf.iufo.data.IRepDataQuerySrv;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.pub.HBPubItfService;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;

/**
 * <b>Application name:</b>生成指标脚本<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 用友软件股份有限公司版权所有。<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-4-29<br>
 * 
 * @author：王志强
 * @version 客开
 */
public class ReportMeasureWorkPlugin extends ReportImportWorkPlugin {

	@Override
	public PreAlertObject executeTask(BgWorkingContext arg0)
			throws BusinessException {
		try {
			Logger.init("iufo");
			for(String reportCode:ReportImportConst.getNameMap().keySet()){
				pringLog(reportCode);
			}
			
		} catch (Exception ex) {

		} finally {
			Logger.init();
		}

		return null;
	}

	private void pringLog(String code_report) {
		// 报表类别
		 

		// 查找所有主体
		ReportCache repCache = IUFOCacheManager.getSingleton().getReportCache();
		ReportVO repVO = repCache.getByCode(code_report);

		IContext reportContext = new Context(); 
		reportContext.setAttribute(ReportContextKey.REPORT_PK, repVO.getPk_report());
		CellsModel formatModel =CellsModelOperator.getFormatModelByPKWithDataProcess(reportContext, true);
		MeasureModel measure = 		 (MeasureModel)formatModel.getExtProp(MeasureModel.class.getName());
	
		try {
	 
			Hashtable<CellPosition, MeasureVO> measureMap = measure.getMeasureVOPosByAll();
				Map<String,AbstractMeasureItem> groupMap = new HashMap<String, AbstractMeasureItem>();
				for(CellPosition point:measureMap.keySet()){
					AbstractMeasureItem mItem = 	MeasureItemFactory.getMeasureInfo(code_report, measureMap.get(point),point);
					if(mItem==null) continue;
					if(groupMap.get(mItem.getGroupKey())==null){
						groupMap.put(mItem.getGroupKey(), mItem);
					}else{
						AbstractMeasureInfo info =  mItem.getInfos().get(0);
//						groupMap.get(mItem.getGroupKey()).
						groupMap.get(mItem.getGroupKey()).addInfo(info);
						
					}
					
					
				}
				for(AbstractMeasureItem item:groupMap.values()){
					Logger.error(item.getSql());
				}

			 
		} catch (Exception e) {
			Logger.error(e);
			Logger.error("查询报表错误："+repVO.getName()+";message:"+e.getMessage());
			//throw new BusinessRuntimeException("查询联查主体合并数据错误!", e);
		}

	}

	 

}
