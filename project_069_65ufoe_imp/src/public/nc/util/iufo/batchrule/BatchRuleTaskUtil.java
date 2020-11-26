package nc.util.iufo.batchrule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.framework.common.NCLocator;
import nc.itf.iufo.batchrule.IBatchRuleQueryService;
import nc.itf.uap.IUAPQueryBS;
import nc.pub.iufo.accperiod.AccPeriodSchemeUtil;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.bi.clusterscheduler.ITask;
import nc.vo.iufo.batchrule.BatchRuleDesc;
import nc.vo.iufo.batchrule.BatchRuleVO;
import nc.vo.iufo.batchrule.RepCalcVO;
import nc.vo.iufo.batchrule.UnitDSInfo;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.pub.date.UFODate;
import nc.vo.iufo.schedule.TimingType;
import nc.vo.iufo.task.TaskReportVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.table.exarea.ExtendAreaCell;
import com.ufida.iufo.table.exarea.ExtendAreaModel;
import com.ufsoft.iufo.fmtplugin.dynarea.DynAreaUtil;
import com.ufsoft.iufo.fmtplugin.dynarea.ReportModel;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.fmtplugin.service.ReportCalcSrv;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.IExtModel;

public class BatchRuleTaskUtil {
	public static ITask[] loadAutoTasks(String strRulePK, String strOperUserPK,int iLevel,String strCurDate,int offsetType, int offsetVal){
		try{
			IBatchRuleQueryService service=(IBatchRuleQueryService)NCLocator.getInstance().lookup(IBatchRuleQueryService.class.getName());
			BatchRuleVO[] batchRules=service.loadBatchRuleByPKs(new String[]{strRulePK});
			if (batchRules==null || batchRules.length<=0 || batchRules[0]==null)
				return null;
			
			BatchRuleVO batchRule=batchRules[0];
			String strDataTime=getDataDate(batchRule.getPk_keygroup(),batchRule.getPk_accscheme(),strCurDate,offsetType,offsetVal);
			RepCalcVO[] repCalcs=dispatchBatch(batchRule, strDataTime, strCurDate,strOperUserPK,true);
			if (repCalcs==null)
				return null;
			
			ITask[] tasks=new ITask[repCalcs.length];
			for (int i=0;i<tasks.length;i++){
				tasks[i]=new RepCalcTask(repCalcs[i],iLevel,batchRule.getName(), batchRule.getRuleDesc().getRmsPk());
				((RepCalcTask)tasks[i]).setPk_group(batchRule.getPk_group());
				// TODO 后台执行，语种信息无法得到，临时性处理
				((RepCalcTask)tasks[i]).setLanguage("simpchn");
				((RepCalcTask) tasks[i]).setPk_task(batchRule.getRuleDesc().getStrCurTask());
			}
			
			return tasks;
		}catch(Exception e){
			AppDebug.debug(e);
			return null;
		}
	}
	
	public static ITask[] loadManualTasks(BatchRuleVO batchRule, String strOperUserPK,int iLevel,String strCurDate, String language){
		try{
			String strDataTime=getDataDate(batchRule.getPk_keygroup(),batchRule.getPk_accscheme(),strCurDate,TimingType.OFF_BY_DAY,0);
			RepCalcVO[] repCalcs=dispatchBatch(batchRule, strDataTime, strCurDate,strOperUserPK,false);
			if (repCalcs==null)
				return null;
			BatchRuleDesc ruleDesc=batchRule.getRuleDesc();
			Map<String, Integer>  reportGroupMap = new HashMap<>();
			if(ruleDesc!=null){
				 TaskReportVO[] taskReports = TaskSrvUtils.getTaskReportByTaskId(ruleDesc.getTaskPKs()[0]);
				
					for(TaskReportVO report:taskReports){
						reportGroupMap.put(report.getPk_report(), report.getGroup_number());}
		
			 
		
			ITask[] tasks=new ITask[repCalcs.length];
			for (int i=0;i<tasks.length;i++){
				 int useLevel = iLevel*10;
				if(reportGroupMap.get(repCalcs[i].getReport())!=null){
					useLevel= useLevel+reportGroupMap.get(repCalcs[i].getReport());
				}
				tasks[i]=new RepCalcTask(repCalcs[i],useLevel,batchRule.getName(), batchRule.getRuleDesc().getRmsPk());
				((RepCalcTask)tasks[i]).setPk_group(batchRule.getPk_group());
				((RepCalcTask)tasks[i]).setLanguage(language);
				((RepCalcTask) tasks[i]).setPk_task(batchRule.getRuleDesc().getStrCurTask());
			}
			
			return tasks;
			}
			return null;
		}catch(Exception e){
			AppDebug.debug(e);
			return null;
		}
	}
	
	public static String getDataDate(String strKeyCombPK,String strAccSchemePK,String strCurDate,int offsetType, int offsetVal){
		KeyGroupVO keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyCombPK);
		UFODate date=new UFODate(strCurDate);
		
		String[] strDateTypes={UFODate.DAY_PERIOD,UFODate.WEEK_PERIOD,UFODate.TENDAYS_PERIOD,UFODate.MONTH_PERIOD,
				UFODate.SEASON_PERIOD,UFODate.HALFYEAR_PERIOD,UFODate.YEAR_PERIOD};
		String strDateType=UFODate.DAY_PERIOD;
		if (offsetType>=1 && offsetType<=7)
			strDateType=strDateTypes[offsetType-1];
		
		String strDate=date.getNextDate(strDateType, offsetVal);
		
		//如果是自然期间关键字
		if (keyGroup.getTimeKey()!=null){
			return new UFODate(strDate).getEndDay(keyGroup.getTimeProp()).toString();
		}else if (keyGroup.getAccKey()!=null){//否则会计期间关键字，同会计期间方案有关
			return AccPeriodSchemeUtil.getInstance().getAccPeriodByNatDate(strAccSchemePK,keyGroup.getAccKey().getPk_keyword(),strDate);
		}else
			return strDate;
	}
	
	private static RepCalcVO[] dispatchBatch(BatchRuleVO batch,String strDataTime,String strLoginDate,String strOperUserPK,boolean bAuto) throws Exception{
		IUAPQueryBS service=(IUAPQueryBS)NCLocator.getInstance().lookup(IUAPQueryBS.class);
		
		BatchRuleDesc ruleDesc=batch.getRuleDesc();
		List<RepCalcVO> vecCalcVOs = new ArrayList<RepCalcVO>();
		UnitDSInfo[] unitDSs = ruleDesc.getUnitDSInfos();
		String[] strRepPKs = ruleDesc.getRepPKs();
		String[][] strKeyVals=ruleDesc.getKeyVals();

		KeyGroupVO keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(batch.getPk_keygroup());
		KeyVO[] keys=keyGroup.getKeys();
		
		DataSourceVO dataSource=null;
		if (batch.getPk_datasource()!=null)
			dataSource=(DataSourceVO)service.retrieveByPK(DataSourceVO.class, batch.getPk_datasource());

		for (int n = 0; unitDSs != null && n < unitDSs.length; n++) {
			String strOrgPK = unitDSs[n].getOrgPK();
//			String strDSPass = unitDSs[n].getUserPass();
//			String strDSUnit = unitDSs[n].getDSOrgCode();
//			String strDSUser = unitDSs[n].getUserCode();
			
			for (int m = 0; m < strRepPKs.length; m++) {
				String strRepPK = strRepPKs[m];
				if (dataSource!=null){
					dataSource=(DataSourceVO)dataSource.clone();
					dataSource.setLogin_name(batch.getRuleDesc().getDsUser());
					dataSource.setLogin_passw(batch.getRuleDesc().getDsPwd());
//					dataSource.setLogin_orgcode(strDSUnit);
					dataSource.setLogin_date(strLoginDate);
				}
				
				if (strKeyVals != null) {
					Set<String> usedKeyVal=new HashSet<String>();
					
					for (int k = 0; k < strKeyVals.length; k++) {
						String strKeyValStr=getAllKeyValStr(strDataTime, strKeyVals[k],keys,bAuto);
						if (usedKeyVal.contains(strKeyValStr))
							continue;
						
						RepCalcVO info = new RepCalcVO(strOrgPK, strDataTime, strRepPK, strKeyVals[k], dataSource,batch.getPk_accscheme(),strOperUserPK,bAuto);
						info.setLoginDate(strLoginDate);
						vecCalcVOs.add(info);
						usedKeyVal.add(strKeyValStr);
					}
				} else {
					RepCalcVO info = new RepCalcVO(strOrgPK,strDataTime, strRepPK, null, dataSource,batch.getPk_accscheme(),strOperUserPK,bAuto);
					info.setLoginDate(strLoginDate);
					vecCalcVOs.add(info);
				}
			}
		}
		return (RepCalcVO[]) vecCalcVOs.toArray(new RepCalcVO[vecCalcVOs.size()]);
	}
	
	public static String getAllKeyValStr(String strDataTime,String[] strKeyVals,KeyVO[] keys,boolean bAuto){
		StringBuffer buf=new StringBuffer();
		int iPos=0;
		for (KeyVO key:keys){
			if (KeyVO.isUnitKeyVO(key))
				continue;
			
			if (key.isTTimeKeyVO() && (bAuto || strKeyVals[iPos].startsWith("0000")))
				buf.append(strDataTime);
			else
				buf.append(strKeyVals[iPos]);
			buf.append("\r\n");
			iPos++;
		}
		return buf.toString();
	}
	
	/**
	 * 执行计算前先将扩展区引用的数据源和当前扩展区数据进行合并
	 * 
	 * @create by liuchuna at 2011-6-30,下午01:19:55
	 *
	 * @param reportCalcSrv
	 */
	public static ReportCalcSrv doBeforeCalculate(ReportCalcSrv reportCalcSrv) {
		CellsModel dataModel = reportCalcSrv.getCellsModel();
		UfoContextVO context = reportCalcSrv.getContextVO();
		CellsModel formatModel = DynAreaUtil.getDataModelWithExModel(dataModel);
		ExtendAreaCell[] exCells = ExtendAreaModel.getInstance(formatModel).getExtendAreaCells();
		if (exCells != null && exCells.length > 0){
			context.setAttribute(ReportContextKey.REPORT_CALCULATE, true);
			CellsModel newModel = DynAreaUtil.getNewDataModel(dataModel,context, ReportModel.OPERATION_INPUTDATA);
			// 进过处理后丢失了映射模型
			IExtModel extModel = formatModel.getExtProp("nc.ui.iufo.intdata.model.mapping.DIMappingModel");
			if (extModel != null) {
				newModel.putExtProp("nc.ui.iufo.intdata.model.mapping.DIMappingModel", extModel);
			}
			// 进过处理后丢失了映射模型
			extModel = formatModel.getExtProp("nc.pub.iufo.hr.model.HRMappingModel");
			if (extModel != null) {
				newModel.putExtProp("nc.pub.iufo.hr.model.HRMappingModel", extModel);
			}
			context.removeAttribute(ReportContextKey.REPORT_CALCULATE);
			// 已计算标记，每次重新生成数据模型时要填充主表扩展区
			context.setAttribute(ReportContextKey.REPORT_HAS_CALCULATEDED,true);
			ReportCalcSrv newClacSrv = new ReportCalcSrv(context, newModel);
			return newClacSrv;
		}
		return reportCalcSrv; 
	}
	
}
