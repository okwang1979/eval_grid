package nc.impl.tb.ext.plan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.framework.common.NCLocator;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.uap.lock.PKLock;
import nc.impl.mdm.cube.DataCellRelationDAO;
import nc.impl.tb.form.dao.SheetTableDMO;
import nc.impl.tb.task.CubeDataQueryThreadCache;
import nc.itf.mdm.cube.ICubeDefQueryService;
import nc.itf.mdm.cube.IDataSetService;
import nc.itf.mdm.dim.IDimManager;
import nc.itf.tb.ext.plan.IPlanExtZjService;
import nc.itf.tb.task.ITaskExcelService;
import nc.ms.mdm.cube.CubeServiceGetter;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ms.tb.ext.plan.PlanExtServiceGetter;
import nc.ms.tb.ext.plan.SumPlanNotFoundException;
import nc.ms.tb.form.FormServiceGetter;
import nc.ms.tb.plan.IPlanSum;
import nc.ms.tb.plan.SumTaskStateCheckTool;
import nc.ms.tb.plan.TBSumPlanCommonToolsWithExcel;
import nc.ms.tb.pub.NtbSuperDMO;
import nc.ms.tb.pub.TbParamUtil;
import nc.ms.tb.pub.plugin.TbPluginLoader;
import nc.ms.tb.pub.plugin.itf.ITbPluginSumDataCellFilter;
import nc.ms.tb.pubutil.CostTime;
import nc.ms.tb.pubutil.DateCtl;
import nc.ms.tb.rule.RuleManager;
import nc.ms.tb.rule.RuleServiceGetter;
import nc.ms.tb.task.RuleExecuteHelper;
import nc.ms.tb.task.TaskActionCtl;
import nc.ms.tb.task.TbTaskCtl;
import nc.ms.tb.task.TbTaskServiceGetter;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ms.tb.task.data.TaskDataModelAction;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.cube.CubeDimUsage;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.cube.DataCellValue;
import nc.vo.mdm.cube.DimSectionSetTuple;
import nc.vo.mdm.cube.DimVector;
import nc.vo.mdm.cube.DimVectorPropUtil;
import nc.vo.mdm.cube.ICubeDataSet;
import nc.vo.mdm.dim.DimDef;
import nc.vo.mdm.dim.DimDefType;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimLevelPKConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDouble;
import nc.vo.tb.form.FormVOTools;
import nc.vo.tb.form.IFormConst;
import nc.vo.tb.form.MdArea;
import nc.vo.tb.form.MdWorkbook;
import nc.vo.tb.form.excel.ExDataCell;
import nc.vo.tb.form.excel.ExVarAreaDef;
import nc.vo.tb.form.iufo.CellFmlInfo;
import nc.vo.tb.ntbenum.NTBActionEnum;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.plan.TbSumParamVO;
import nc.vo.tb.rule.AllotFormulaForTModelVO;
import nc.vo.tb.rule.BusiRuleVO;
import nc.vo.tb.rule.excel.FormulaCellInfo;
import nc.vo.tb.task.ITaskAction;
import nc.vo.tb.task.ITaskConst;
import nc.vo.tb.task.ITaskStatus;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;
import nc.vo.tb.task.TaskLog;
import nc.vo.tb.task.TaskLogExt;

public class PlanExtZjImpl implements IPlanExtZjService, IPlanSum {

	@Override
	public HashMap<String, ICubeDataSet> queryByOrg(HashMap<String, DimSectionSetTuple> querySliceRules) throws BusinessException {
		HashMap<String, ICubeDataSet> rtn = new HashMap<String, ICubeDataSet>();
		if (querySliceRules != null) {
			IDataSetService idss = CubeServiceGetter.getDataSetService();
			ICubeDefQueryService icdqs = CubeServiceGetter.getCubeDefQueryService();
			CubeDef cd;
			for (String cubeCode : querySliceRules.keySet()) {
				cd = icdqs.queryCubeDefByBusiCode(cubeCode);
				if (cd != null) {
					ICubeDataSet idcs = idss.queryDataSet(cd, querySliceRules.get(cubeCode));
					if (idcs != null)
						rtn.put(cubeCode, idcs);
				}
			}
		}
		return rtn;
	}

	@Override
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo) throws BusinessException {
		return sumPlan(pk_taskTo, fromOrgUnicode, userVo, null, null);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>> sumPlanVarArea(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo) throws BusinessException {
		return sumPlan(pk_taskTo, fromOrgUnicode, userVo, null, ExVarAreaDef.varAreaSumType_default, null, false);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, HashMap userInfo) throws BusinessException {
		return sumPlan(pk_taskTo, fromOrgUnicode, userVo, null, userInfo);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, HashMap userInfo) throws BusinessException {
		return sumPlan(pk_taskTo, fromOrgUnicode, userVo, pk_sheets, ExVarAreaDef.varAreaSumType_default, userInfo);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType) throws BusinessException {
		return sumPlan(pk_taskTo, fromOrgUnicode, userVo, pk_sheets, varAreaSumType, null);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlanVarArea(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException {
		return sumPlan(pk_taskTo, fromOrgUnicode, userVo, pk_sheets, varAreaSumType, userInfo, false);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException {
		return sumPlan(pk_taskTo, fromOrgUnicode, userVo, pk_sheets, varAreaSumType, userInfo, true);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlanVarAreaByParamDimLevel(String pk_taskTo, DimLevel sumParamDimLevel, String[] fromLevelValueUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException {
		return sumPlanByParamDimLevel(pk_taskTo, sumParamDimLevel, fromLevelValueUnicode, userVo, pk_sheets, varAreaSumType, userInfo, false);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlanByParamDimLevel(String pk_taskTo, DimLevel sumParamDimLevel, String[] fromLevelValueUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException {
		return sumPlanByParamDimLevel(pk_taskTo, sumParamDimLevel, fromLevelValueUnicode, userVo, pk_sheets, varAreaSumType, userInfo, true);
	}
	private HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo, boolean isSumDataCell) throws BusinessException {
		return sumPlanByParamDimLevel(pk_taskTo, DimServiceGetter.getDimManager().getDimLevelByPK(IDimLevelPKConst.ENT), fromOrgUnicode, userVo, pk_sheets, varAreaSumType, userInfo, isSumDataCell);
	}
	private HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlanByParamDimLevel(String pk_taskTo, DimLevel sumParamDimLevel, String[] fromLevelValueUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo, boolean isSumDataCell) throws BusinessException {
		MdTask taskTo = TbTaskCtl.getMdTaskByPk(pk_taskTo, false);
		if (taskTo == null)
			throw new SumPlanNotFoundException(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000415")/*汇总目标任务不存在*/, SumPlanNotFoundException.planNotFindType_uplvl);
		if (fromLevelValueUnicode == null || fromLevelValueUnicode.length == 0)
			throw new SumPlanNotFoundException(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000416")/*要汇总的任务不存在*/, SumPlanNotFoundException.planNotFindType_lowlvl);
		//20141204 限制在Excel端改变任务状态NC端没有及时刷新状态的时候，也不可以汇总。 pz
		List <String> state = new ArrayList<String>();
		state.add(taskTo.getPlanstatus());
		state.add(taskTo.getManagestatus());
		String result  = SumTaskStateCheckTool.checkUnValidMessage(state);
		if(result!=null){
			throw new BusinessException(result);
		}
		DimSectionSetTuple paradimSet = new DimSectionSetTuple();
//		IDimManager dm = DimServiceGetter.getDimManager();
		DimLevel dlEnt = sumParamDimLevel;//dm.getDimLevelByPK(IDimLevelPKConst.ENT);
//		List<String> pks=new ArrayList<String>();
		List<LevelValue> lvEnts = new ArrayList<LevelValue>();
		for (int i=0; i<fromLevelValueUnicode.length; i++) {
			LevelValue lvEnt = dlEnt.getLevelValueByUniqCode(fromLevelValueUnicode[i]);
			if (lvEnt != null)
				{
				paradimSet.getLevelValues(dlEnt).add(lvEnt);
				lvEnts.add(lvEnt);
//				pks.add(lvEnt.getKey().toString());
				}
		}
		//lym 修改用参数维去取任务，paradimSet取出来的任务大于实际汇总数
//		TaskFilter filter = new TaskFilter();
//        filter=ZiorFrameCtl.getTaskFilter(filter,taskTo);
//        MdTask[] tasks=TbTaskServiceGetter.getTaskObjectService().getDbMdTasksByFilter(taskTo.getPk_taskdef(), filter, "1=1", true);
//        if(tasks!=null&&tasks.length>0){
//        	List<MdTask> talist=new ArrayList<MdTask>();
//        	for(int i=0;i<tasks.length;i++){
//        		if(pks.contains(tasks[i].getPk_dataent())){
//        			talist.add(tasks[i]);
//        		}
//        	}
//        	return sumPlan(taskTo, talist.toArray(new MdTask[0]), null, userVo, pk_sheets, ExVarAreaDef.varAreaSumType_default, userInfo, isSumDataCell, sumParamDimLevel);
//        }
        
        
//      MdTask[] tasks=TbTaskServiceGetter.getTaskObjectService().getDbMdTasksByFilter(taskTo.getPk_taskdef(), filter, "1=1", true);

//		PlanSumCtl.fillDimSectionValue(taskTo, paradimSet);
//		MdTask[] tasksFrom = TbTaskServiceGetter.getTaskObjectService().getMdTaskByParadimSet(taskTo, paradimSet);
		PlanSumCtl.fillDimSectionValue(taskTo, paradimSet);
		MdTask[] tasksFrom = TbTaskServiceGetter.getTaskObjectService().getMdTaskByNewLevelValues(taskTo, dlEnt, lvEnts.toArray(new LevelValue[0]))/*getMdTaskByParadimSet(taskTo, paradimSet)*/;
		return sumPlan(taskTo, tasksFrom, null, userVo, pk_sheets, ExVarAreaDef.varAreaSumType_default, userInfo, isSumDataCell, sumParamDimLevel);
		
	}

	@Override
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo) throws BusinessException {
		return sumPlanDif(pk_upLvlPlan, lowerLvlOrgUnicode, difOrgUnicode, userVo, null, null);
	}
	@Override
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo, HashMap userInfo) throws BusinessException {
		return sumPlanDif(pk_upLvlPlan, lowerLvlOrgUnicode, difOrgUnicode, userVo, null, userInfo);
	}
	@Override
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo, String[] pk_sheets) throws BusinessException {
		return sumPlanDif(pk_upLvlPlan, lowerLvlOrgUnicode, difOrgUnicode, userVo, pk_sheets, null);
	}
	@Override
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo, String[] pk_sheets, HashMap userInfo) throws BusinessException {
		MdTask taskTo = TbTaskCtl.getMdTaskByPk(pk_upLvlPlan, false);
		if (taskTo == null)
			throw new SumPlanNotFoundException(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000417")/*要汇总的上级任务不存在*/, SumPlanNotFoundException.planNotFindType_uplvl);
		if (lowerLvlOrgUnicode == null || lowerLvlOrgUnicode.length == 0)
			throw new SumPlanNotFoundException(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000418")/*要汇总的下级任务不存在*/, SumPlanNotFoundException.planNotFindType_lowlvl);
		DimSectionSetTuple paradimSet = new DimSectionSetTuple();
		IDimManager dm = DimServiceGetter.getDimManager();
		DimLevel dlEnt = dm.getDimLevelByPK(IDimLevelPKConst.ENT);
		List<LevelValue> lvEnts = new ArrayList<LevelValue>();
		for (int i=0; i<lowerLvlOrgUnicode.length; i++) {
			LevelValue lvEnt = dlEnt.getLevelValueByUniqCode(lowerLvlOrgUnicode[i]);
			if (lvEnt != null) {
				paradimSet.getLevelValues(dlEnt).add(lvEnt);
				lvEnts.add(lvEnt);
			}
		}
		PlanSumCtl.fillDimSectionValue(taskTo, paradimSet);
		MdTask[] tasksFrom = TbTaskServiceGetter.getTaskObjectService().getMdTaskByNewLevelValues(taskTo, dlEnt, lvEnts.toArray(new LevelValue[0]))/*getMdTaskByParadimSet(taskTo, paradimSet)*/;
		if (tasksFrom == null || tasksFrom.length == 0)
			throw new SumPlanNotFoundException(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000418")/*要汇总的下级任务不存在*/, SumPlanNotFoundException.planNotFindType_lowlvl);
		paradimSet.getLevelValues(dlEnt).clear();
		LevelValue lvEnt = dlEnt.getLevelValueByUniqCode(difOrgUnicode);
		if (lvEnt != null)
			paradimSet.getLevelValues(dlEnt).add(lvEnt);
		else
			throw new SumPlanNotFoundException(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000419")/*要汇总的差额任务不存在*/, SumPlanNotFoundException.planNotFindType_sub);
//		fillDimSectionValue(taskToDif, paradimSet);
		MdTask[] taskC = TbTaskServiceGetter.getTaskObjectService().getMdTaskByParadimSet(taskTo, paradimSet);
		if (taskC == null || taskC.length == 0)
			throw new SumPlanNotFoundException(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000419")/*要汇总的差额任务不存在*/, SumPlanNotFoundException.planNotFindType_sub);
		MdTask[] taskSumFrom = new MdTask[tasksFrom.length+1];
		boolean[] isAdd = new boolean[taskSumFrom.length];
		taskSumFrom[0] = taskTo;
		isAdd[0] = true;
		for (int i=0; i<tasksFrom.length; i++) {
			taskSumFrom[i+1] = tasksFrom[i];
			isAdd[i+1] = false;
		}
		sumPlan(taskC[0], taskSumFrom, isAdd, userVo, pk_sheets, userInfo);
		return taskC[0].getPrimaryKey();
	}

	@Override
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo) throws BusinessException {
		return sumPlan(taskTo, taskFrom, isAdd, userVo, null);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets) throws BusinessException {
		return sumPlan(taskTo, taskFrom, isAdd, userVo, pk_sheets, ExVarAreaDef.varAreaSumType_default, null);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, HashMap userInfo) throws BusinessException {
		return sumPlan(taskTo, taskFrom, isAdd, userVo, pk_sheets, ExVarAreaDef.varAreaSumType_default, userInfo);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType) throws BusinessException {
		return sumPlan(taskTo, taskFrom, isAdd, userVo, pk_sheets, varAreaSumType, null);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException {
		return sumPlan(taskTo, taskFrom, isAdd, userVo, pk_sheets, varAreaSumType, userInfo, true);
	}
	@Override
	public HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlanVarArea(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException {
		return sumPlan(taskTo, taskFrom, isAdd, userVo, pk_sheets, varAreaSumType, userInfo, false);
	}
	private HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo, boolean isSumDataCell) throws BusinessException {
		return sumPlan(taskTo, taskFrom, isAdd, userVo, pk_sheets, varAreaSumType, userInfo, isSumDataCell, DimServiceGetter.getDimManager().getDimLevelByPK(IDimLevelPKConst.ENT));
	}
	/**
	 * 增加参数：汇总维度层sumDimlevel
	 * 原方法该参数默认为主体
	 * @param taskTo
	 * @param taskFrom
	 * @param isAdd
	 * @param userVo
	 * @param pk_sheets
	 * @param varAreaSumType
	 * @param userInfo
	 * @param isSumDataCell
	 * @param sumDimlevel
	 * @return
	 * @throws BusinessException
	 * @author: lrx@yonyou.com
	 */
	private HashMap<String, HashMap<String,ExDataCell>>/*void*/ sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo, boolean isSumDataCell, DimLevel sumDimlevel) throws BusinessException {
//		pk_sheets = null;
		if (taskTo == null)
			throw new SumPlanNotFoundException(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000415")/*汇总目标任务不存在*/, SumPlanNotFoundException.planNotFindType_uplvl);
		if (taskFrom == null || taskFrom.length == 0)
			throw new SumPlanNotFoundException(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000416")/*要汇总的任务不存在*/, SumPlanNotFoundException.planNotFindType_lowlvl);
		if (ITaskStatus.STARTED.equals(taskTo.getPlanstatus()))
			TaskActionCtl.processAction(userVo, taskTo, ITaskAction.COMPILE);
		long s = System.currentTimeMillis();
		// 是否汇总浮动区
		boolean isSumVarArea = true;
		if (isAdd == null || isAdd.length != taskFrom.length) {
			// 加减属性不合法，统一处理为默认值：+
			isAdd = new boolean[taskFrom.length];
			for (int i=0; i<isAdd.length; i++)
				isAdd[i] = true;
		}
		else {
			for (int i=0; i<isAdd.length; i++) {
				if (!isAdd[i]) {
					isSumVarArea = false;
					break;
				}
			}
		}
		boolean hasNoCacheSheet = false;
		if (isSumVarArea) {
			hasNoCacheSheet = PlanSumCtl.sumPlanVarAreas(taskTo, taskFrom, pk_sheets, varAreaSumType);
		}

		if (!isSumDataCell) {
			saveSumLog(taskTo, taskFrom, !isSumVarArea, userVo, pk_sheets, varAreaSumType, userInfo);
			return null;
		}

		try {
		HashMap<CubeDef, HashMap<DimVector, DataCell>> saveMap = new HashMap<CubeDef, HashMap<DimVector, DataCell>>();
		HashMap<CubeDef, DimMember> orgMap = new HashMap<CubeDef, DimMember>();

//		IDimManager dm = DimServiceGetter.getDimManager();
		DimDef ddEnt = sumDimlevel.getDimDef();//dm.getDimDefByPK(IDimDefPKConst.ENT);
		DimLevel dlEnt = sumDimlevel;//dm.getDimLevelByPK(IDimLevelPKConst.ENT);
//		LevelValue lvEntTaskTo = getTaskParamLevelValue(taskTo, dlEnt);//dlEnt.getLevelValueByKey(taskTo.getPk_dataent());
//		DimLevel dlMea = dm.getDimLevelByPK(IDimLevelPKConst.MEASURE);
//		DimLevel dlMvtype = dm.getDimLevelByPK(IDimLevelPKConst.MVTYPE);
//		LevelValue lvMvtype = dlMvtype.getLevelValueByKey(taskTo.getPk_mvtype());
//		String mvTypeCode = lvMvtype.getCode();

		// 查询并清空目标任务数据(只处理与任务主体相符的数据) ==>> 2013-9-4 增加：只处理业务方案与参数维一致的数据
		// ==>> 2013-9-24：9月4日的修改去掉(中建的汇总控制逻辑改为用业务方案的单价比率属性控制是否汇总)
		// lrx 2013-10-11 V631多维数据支持文本，汇总逻辑：汇总下级单元格唯一时取该单元格值，不唯一时不汇总
//		CubeDataCellSet cdcs = TbTaskServiceGetter.getTaskBusinessService().getDataCellsByTask(taskTo.getPrimaryKey());
//		Map<String, ICubeDataSet> dcMap = cdcs==null ? null : cdcs.getDataCellMap();
		HashMap<CubeDef, List<DataCell>> dcMap = (pk_sheets==null && TbParamUtil.isTaskDataCellRelationValid()) ?
				PlanSumCtl.getDataCellByTask(taskTo, taskFrom) : PlanSumCtl.getDataCellByTask(taskTo, taskFrom, pk_sheets);
		HashMap<CubeDef, HashMap<DimMember, List<DataCell>>> childTaskDcMap = new HashMap<CubeDef, HashMap<DimMember,List<DataCell>>>();
		if (dcMap != null) {
//			for (ICubeDataSet icds : dcMap.values()) {
			for (CubeDef cd : dcMap.keySet()) {
//				CubeDef cd = icds.getCubeDef();
				HashMap<DimMember, List<DataCell>> taskDcMap = new HashMap<DimMember, List<DataCell>>();
				childTaskDcMap.put(cd, taskDcMap);
//				DimMember dmEnt = cd.getDimHierarchy(ddEnt).getDimMemberByLevelValues(lvEntTaskTo);
				DimMember dmEnt = PlanSumCtl.getTaskParamLevelValue(taskTo, cd.getDimHierarchy(ddEnt), dlEnt);
				orgMap.put(cd, dmEnt);
				HashMap<DimVector, DataCell> map = new HashMap<DimVector, DataCell>();
				saveMap.put(cd, map);
//				List<DataCell> dcs = icds.getDataResult();
				List<DataCell> dcs = dcMap.get(cd);
				if (dcs != null && !dcs.isEmpty()) {
					for (DataCell dc : dcs) {
						// 指标=单价比率的DataCell不参与汇总
						if(DimVectorPropUtil.isRATIO(dc.getDimVector()))
							continue;
//						// 业务方案与参数维不同的不参与汇总
//						LevelValue lv = dc.getDimVector().getLevelValue(dlMvtype);
//						if (lv == null || !mvTypeCode.equals(lv.getCode()))
//							continue;
						if (dc.getDimVector().containsDimMember(dmEnt)) {
							dc.forceSetCellValue(new DataCellValue(new UFDouble(0), null));
							map.put(dc.getDimVector(), dc);
						}
						else {
							DimMember otherEntLv = dc.getDimVector().getDimMember(ddEnt);
							List<DataCell> cl = taskDcMap.get(otherEntLv);
							if (cl == null) {
								cl = new ArrayList<DataCell>();
								taskDcMap.put(otherEntLv, cl);
							}
							cl.add(dc);
						}
					}
				}
			}
		}

		// 依次汇总下级
		for (int i=0; i<taskFrom.length; i++) {
//			cdcs = TbTaskServiceGetter.getTaskBusinessService().getDataCellsByTask(taskFrom[i].getPrimaryKey());
//			dcMap = cdcs==null ? null : cdcs.getDataCellMap();
			if (dcMap != null) {
//				for (ICubeDataSet icds : dcMap.values()) {
				for (CubeDef cd : dcMap.keySet()) {
//					CubeDef cd = icds.getCubeDef();
//					LevelValue lvEntTask = getTaskParamLevelValue(taskFrom[i], dlEnt);//dlEnt.getLevelValueByKey(taskFrom[i].getPk_dataent());
//					DimMember dmEnt = cd.getDimHierarchy(ddEnt).getDimMemberByLevelValues(lvEntTask);
					DimMember dmEnt = PlanSumCtl.getTaskParamLevelValue(taskFrom[i], cd.getDimHierarchy(ddEnt), dlEnt);
					DimMember dmEntParent = orgMap.get(cd);
					if (dmEntParent == null) {
//						dmEntParent = cd.getDimHierarchy(ddEnt).getDimMemberByLevelValues(lvEntTaskTo);
						dmEntParent = PlanSumCtl.getTaskParamLevelValue(taskTo, cd.getDimHierarchy(ddEnt), dlEnt);
						orgMap.put(cd, dmEnt);
					}
					HashMap<DimVector, DataCell> map = saveMap.get(cd);
					if (map == null) {
						map = new HashMap<DimVector, DataCell>();
						saveMap.put(cd, map);
					}
					// list中有可能有DataCell重复
//					List<DataCell> dcs = icds.getDataResult();
					HashMap<DimMember, List<DataCell>> taskDcMap = childTaskDcMap.get(cd);
					List<DataCell> dcs = taskDcMap==null ? null : taskDcMap.get(dmEnt);
					HashMap<DimVector, DataCell> tmpDvMap = new HashMap<DimVector, DataCell>();
					if (dcs != null && !dcs.isEmpty()) {
						for (DataCell dc : dcs) {
							if (tmpDvMap.containsKey(dc.getDimVector()))
								continue;
							tmpDvMap.put(dc.getDimVector(), dc);
//							if (dc.getDimVector().containsDimMember(dmEnt)) {
								DimVector dv = dc.getDimVector().addOrReplaceDimMember(dmEntParent);
								DataCell dcParent = map.get(dv);
								// 2013-4-22 此处原来的处理方式在差额汇总上有bug
								if (dcParent == null) {
									dcParent = new DataCell(cd, dv);
									dcParent.setFlag_New(true);
									map.put(dv, dcParent);
								}
								DataCellValue parentValue = dcParent==null ? null : dcParent.getCellValue();
								Number numValue = parentValue==null ? new UFDouble(0.0) : parentValue.getValue();
								if (numValue == null)
									numValue = new UFDouble(0.0);
								if (dc.getCellValue() != null && dc.getCellValue().getValue() != null) {
									Number num2 = dc.getCellValue().getValue();
									UFDouble d1 = numValue instanceof UFDouble ? (UFDouble)numValue : new UFDouble(numValue.doubleValue());
									UFDouble d2 = num2 instanceof UFDouble ? (UFDouble)num2 : new UFDouble(num2.doubleValue());
									numValue = isAdd[i] ? d1.add(d2) : d1.sub(d2);
								}
								String parentTxtValue = dcParent.getCellValue()==null ? null : dcParent.getCellValue().getTxtValue();
								if (dc.getCellValue() != null && dc.getCellValue().getTxtValue() != null) {
									if (parentTxtValue == null)
										parentTxtValue = dc.getCellValue().getTxtValue();
									else
										parentTxtValue = "";
								}
								dcParent.forceSetCellValue(new DataCellValue(numValue, parentTxtValue));
//								if (dcParent == null) {
//									dcParent = new DataCell(cd, dv);
//									dcParent.setFlag_New(true);
//									dcParent.forceSetCellValue(new DataCellValue(dc.getCellValue().getValue()));
//									map.put(dv, dcParent);
//								}
//								else {
//									if (dcParent.getCellValue() == null || dcParent.getCellValue().getValue() == null)
//										dcParent.forceSetCellValue(new DataCellValue(dc.getCellValue().getValue()));
//									else if (dc.getCellValue() != null && dc.getCellValue().getValue() != null) {
//										Number num1 = dcParent.getCellValue().getValue();
//										Number num2 = dc.getCellValue().getValue();
//										UFDouble d1 = num1 instanceof UFDouble ? (UFDouble)num1 : new UFDouble(num1.doubleValue());
//										UFDouble d2 = num2 instanceof UFDouble ? (UFDouble)num2 : new UFDouble(num2.doubleValue());
//										dcParent.forceSetCellValue(new DataCellValue(isAdd[i] ? d1.add(d2) : d1.sub(d2)));
//									}
//								}
//							}
						}
					}
				}
			}
		}

		// 保存汇总后DataCell
		HashMap<String, HashMap<String,ExDataCell>> rtn = new HashMap<String, HashMap<String,ExDataCell>>();
		IDataSetService idss = CubeServiceGetter.getDataSetService();
		for (CubeDef cd : saveMap.keySet()) {
			HashMap<DimVector, DataCell> cells = saveMap.get(cd);
			if (cells != null && !cells.isEmpty()) {
				List<DataCell> save = new ArrayList<DataCell>();
				save.addAll(cells.values());
				// 根据某些规则过滤可以汇总的DataCell
				try {
					List<ITbPluginSumDataCellFilter> pluginList = TbPluginLoader.getInstance().getPluginSumDataCellFilter();
					if (pluginList != null && !pluginList.isEmpty()) {
						for (ITbPluginSumDataCellFilter filter : pluginList) {
							save = filter.filterDataCell(save);
						}
					}
				} catch (Throwable t) {
					NtbLogger.printException(t);
				}
				idss.saveDataSetCells(cd, save);
				if (hasNoCacheSheet) {
					DataCellRelationDAO dcDao = new DataCellRelationDAO();
					dcDao.dataCellSum(taskTo.getPrimaryKey(), cd, save);
				}
				// 整理返回的DataCell
				HashMap<String,ExDataCell> changedDc = new HashMap<String,ExDataCell>();
				DimLevel[] dls = FormVOTools.getSortedCubeParaDimLevels(cd);
				StringBuffer dvSb;
				DimVector dv;
				LevelValue lv;
				for (DataCell dc : save) {
					dvSb = new StringBuffer();
					dv = dc.getDimVector();
					for (int i = 0; i < dls.length; i++) {
						lv = dv.getLevelValue(dls[i]);
						if (lv == null)
							dvSb.append(IFormConst.celldv_null);
						else
							dvSb.append(lv.getUniqCode());
						dvSb.append(IFormConst.celldv_seperator);
					}
					changedDc.put(dvSb.toString(), FormVOTools.getExDataCell(dc, dc.getCellValue() == null ? null : dc.getCellValue().getValue(), false));
				}
				rtn.put(cd.getObjcode(), changedDc);
			}
		}
		saveSumLog(taskTo, taskFrom, !isSumVarArea, userVo, pk_sheets, varAreaSumType, userInfo);
		//NtbLogger.error(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000420")/*汇总任务耗时：*/+(System.currentTimeMillis()-s)+"ms");
		NtbLogger.error(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000421")+ pk_sheets/*表单：{0}*/);
		return rtn;
		} finally {
			SheetTableDMO.getInstance().clearThreadCache();
			CubeDataQueryThreadCache.clear();
		}
	}
//	private LevelValue getTaskParamLevelValue(MdTask task, DimLevel paramDimLevel) {
//		String pk_dimlevel = paramDimLevel.getPrimaryKey();
//		if (IDimLevelPKConst.ENT.equals(pk_dimlevel))
//			return paramDimLevel.getLevelValueByKey(task.getPk_dataent());
//		else if (IDimLevelPKConst.YEAR.equals(pk_dimlevel))
//			return paramDimLevel.getLevelValueByKey(task.getPk_year());
//		else if (IDimLevelPKConst.MONTH.equals(pk_dimlevel))
//			return paramDimLevel.getLevelValueByKey(task.getPk_month());
//		else if (IDimLevelPKConst.CURR.equals(pk_dimlevel))
//			return paramDimLevel.getLevelValueByKey(task.getPk_currency());
//		else if (IDimLevelPKConst.AIMCURR.equals(pk_dimlevel))
//			return paramDimLevel.getLevelValueByKey(task.getPk_aimcurr());
//		else if (IDimLevelPKConst.MVTYPE.equals(pk_dimlevel))
//			return paramDimLevel.getLevelValueByKey(task.getPk_mvtype());
//		else if (IDimLevelPKConst.VERSION.equals(pk_dimlevel))
//			return paramDimLevel.getLevelValueByKey(task.getPk_version());
//		else {
//			String otherDim = task.getPk_paradims();
//			if (otherDim != null && otherDim.length() > 0) {
//				IStringConvertor sc = StringConvertorFactory.getConvertor(DimSectionTuple.class);
//				DimSectionTuple paradim = (DimSectionTuple) sc.fromString(otherDim);
//				return paradim.getLevelValue(paramDimLevel);
//			}
//		}
//		return null;
//	}

//	private final static int maxReadRows = 100000; // UAP最大查询记录条数
//	private HashMap<CubeDef, List<DataCell>> getDataCellByTask(MdTask task, MdTask[] otherTask, String[] pk_sheets) {
//		try {
//			HashMap<String, MdAreasSliceRule> srMap = TbTaskServiceGetter.getTaskBusinessService().getTaskSliceRule(task, pk_sheets);
//			if (srMap ==  null || srMap.isEmpty())
//				return null;
//			IDataSetService idss = CubeServiceGetter.getDataSetService();
//			ICubeDefQueryService icdqs = CubeServiceGetter.getCubeDefQueryService();
//			HashMap<CubeDef, List<DataCell>> rtn = new HashMap<CubeDef, List<DataCell>>();
//			DimSectionTuple paraDim = TbTaskCtl.getTaskParadim(task);
//			TbTaskCtl.loadDimMemberValues(otherTask, true);
//			Map<DimLevel, LevelValue> pMap = paraDim.getLevelValues();
//			for (String cubeCode : srMap.keySet()) {
//				MdAreasSliceRule areaSr = srMap.get(cubeCode);
//				DimSectionSetTuple dsst = new DimSectionSetTuple();
//				for (DimLevel dl : pMap.keySet())
//					dsst.getLevelValues(dl).add(pMap.get(dl));
//				Map<DimLevel, Collection<LevelValue>> map = dsst.getLevelValues();
//				List<DimSectionSetTuple> l = areaSr.getDimSectionSetTuples();
//				for (DimSectionSetTuple d : l) {
//					for (DimLevel dl : map.keySet()) {
//						if (IDimLevelPKConst.ENT.equals(dl.getPrimaryKey())) {
//							for (int i=0; i<otherTask.length; i++) {
//								LevelValue lv = (LevelValue)otherTask[i].getExtrAttribute(otherTask[i].getPk_dataent());
//								if (lv != null) {
//									if (!map.get(dl).contains(lv))
//										map.get(dl).add(lv);
//									if (!d.getLevelValues(dl).contains(lv))
//										d.getLevelValues(dl).add(lv);
//								}
//							}
//							continue;
//						}
//						Collection<LevelValue> c = d.getLevelValues(dl);
//						if (c != null) {
//							for (LevelValue lv : c) {
//								if (!map.get(dl).contains(lv))
//									map.get(dl).add(lv);
//							}
//						}
//					}
//				}
//				CubeDef cd = icdqs.queryCubeDefByBusiCode(cubeCode);
//				ICubeDataSet idcs = idss.queryDataSet(cd, dsst);
//				List<DataCell> dcList = idcs==null ? null : idcs.getDataResult();
//				if (dcList != null) {
//					List<DataCell> list = new ArrayList<DataCell>();
//					if (dcList.size() == maxReadRows) {
//						// 查询记录数量等于UAP最大条数，表示极可能实际数据超出了没有全部查询到，需要重新按任务查询
//						DimLevel dlEnt = DimServiceGetter.getDimManager().getDimLevelByPK(IDimLevelPKConst.ENT);
//						LevelValue[] tmpEnts = dsst.getLevelValues(dlEnt).toArray(new LevelValue[0]);
//						for (int i=0; i<tmpEnts.length; i++) {
//							dsst.getLevelValues(dlEnt).clear();
//							dsst.getLevelValues(dlEnt).add(tmpEnts[i]);
//							idcs = idss.queryDataSet(cd, dsst);
//							dcList = idcs==null ? null : idcs.getDataResult();
//							if (dcList != null) {
//								for (DataCell dc : dcList) {
//									if (areaSr.acceptDimVector(dc.getDimVector()))
//										list.add(dc);
//								}
//							}
//						}
//					}
//					else {
//						for (DataCell dc : dcList) {
//							if (areaSr.acceptDimVector(dc.getDimVector()))
//								list.add(dc);
//						}
//					}
//					if (!list.isEmpty())
//						rtn.put(cd, list);
//				}
//			}
//			return rtn;
//		} catch (BusinessException e) {
//			NtbLogger.printException(e);
//		}
//		return null;
//	}
//	private HashMap<CubeDef, List<DataCell>> getDataCellByTask(MdTask task, MdTask[] otherTask) {
//		HashMap<CubeDef, List<DataCell>> rtn = new HashMap<CubeDef, List<DataCell>>();
//		NtbSuperDMO dmo = new NtbSuperDMO();
//		try {
//			SuperVO[] vos = dmo.queryByWhereClause(MdArea.class, "pk_workbook='"+task.getPk_workbook()+"'");
//			if (vos != null) {
//				long s = System.currentTimeMillis();
//				List<String> pk_tasks = new ArrayList<String>();
//				pk_tasks.add(task.getPrimaryKey());
//				for (int j=0; j<otherTask.length; j++) {
//					pk_tasks.add(otherTask[j].getPrimaryKey());
//				}
//				TbTaskServiceGetter.getTaskDataService().initTaskDataCellRelation(pk_tasks);
//				NtbLogger.error(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000422")/*初始化任务DataCell关联表耗时：*/+(System.currentTimeMillis()-s)+"ms");
//
//				IDataSetService idss = CubeServiceGetter.getDataSetService();
//				ICubeDefQueryService icdqs = CubeServiceGetter.getCubeDefQueryService();
//				HashMap<String, CubeDef> cdMap = new HashMap<String, CubeDef>();
//				for (int i=0; i<vos.length; i++) {
//					String cubeCode = ((MdArea)vos[i]).getCubecode();
//					if (cdMap.containsKey(cubeCode))
//						continue;
//					CubeDef cd = icdqs.queryCubeDefByBusiCode(cubeCode);
//					if (cd != null) {
//						// 以下两种查询方法在10几个任务验证时耗时接近，怕遇到选择任务太多的情况，还是使用分次查询
//						// 这是所有任务查询一次的方法---------------------
////						List<DataCell> list = idss.queryDataSetByTask(cd, pk_tasks.toArray(new String[0]));
////						if (list != null && !list.isEmpty())
////							rtn.put(cd, list);
//						// ----------------------------------------------
//						// 这是每个任务查询一次的方法---------------------
//						List<DataCell> list = idss.queryDataSetByTask(cd, task.getPrimaryKey());
//						if (list == null)
//							list = new ArrayList<DataCell>();
//						rtn.put(cd, list);
//						for (int j=0; j<otherTask.length; j++) {
//							List<DataCell> list1 = idss.queryDataSetByTask(cd, otherTask[j].getPrimaryKey());
//							if (list1 != null && !list1.isEmpty())
//								list.addAll(list1);
//						}
//						// ----------------------------------------------
//					}
//					cdMap.put(cubeCode, cd);
//				}
//			}
//		} catch (DAOException e) {
//			NtbLogger.printException(e);
//		} catch (BusinessException e) {
//			NtbLogger.printException(e);
//		}
//		return rtn;
//	}

//	private void fillDimSectionValue(MdTask task, DimSectionSetTuple paradimSet) {
//		TbTaskCtl.loadDimMemberValues(new MdTask[]{task}, true);
//		DimSectionTuple dst = TbTaskCtl.getTaskParadim(task);
//		Map<DimLevel, LevelValue> map = dst.getLevelValues();
//		Map<DimLevel, Collection<LevelValue>> tmpMap = paradimSet.getLevelValues();
//		for (DimLevel dl : map.keySet()) {
//			if (tmpMap.containsKey(dl))
//				continue;
//			paradimSet.getLevelValues(dl).add(map.get(dl));
//		}
//	}

//	/**
//	 * 汇总任务浮动区
//	 * @param taskTo
//	 * @param taskFrom
//	 * @param varAreaSumType - 2013-2-28 增加参数：浮动区汇总方式
//	 * 			ExVarAreaDef.varAreaSumType_default = -1; // 汇总方式：默认
//	 * 			ExVarAreaDef.varAreaSumType_LIST = 0; // 罗列汇总
//	 * 			ExVarAreaDef.varAreaSumType_SUM = 1;  // 分类汇总
//	 * 			ExVarAreaDef.varAreaSumType_NONE = 2; // 不汇总
//	 * @throws BusinessException
//	 */
//	private void sumPlanVarAreas(MdTask taskTo, MdTask[] taskFrom, String[] pk_sheets, int varAreaSumType) throws BusinessException {
//		if (taskTo == null || taskFrom == null || taskFrom.length == 0)
//			return;
//		HashMap<String, Object> sheetsMap = new HashMap<String, Object>();
//		if (pk_sheets != null) {
//			for (int i=0; i<pk_sheets.length; i++)
//				sheetsMap.put(pk_sheets[i], null);
//		}
//		// ---- 加载浮动区汇总方式map(pk_sheet, varId, sumType) ----
//		HashMap<String, HashMap<String, ExVarAreaDef>> varAreaSumTypeMap = new HashMap<String, HashMap<String,ExVarAreaDef>>();
//		HashMap<String, MdArea[]> areaMap = FormServiceGetter.getFormObjectService().getMdAreasByWorkbook(taskTo.getPk_workbook(), null);
//		if (areaMap != null) {
//			for (String pk_sheet : areaMap.keySet()) {
//				if (!sheetsMap.isEmpty() && !sheetsMap.containsKey(pk_sheet))
//					continue;
//				MdArea[] areas = areaMap.get(pk_sheet);
//				if (areas != null) {
//					for (int i=0; i<areas.length; i++) {
//						HashMap<String, ExVarAreaDef> vdMap = areas[i].getVarAreaDefMap();
//						if (vdMap != null) {
//							for (String vKey : vdMap.keySet()) {
//								HashMap<String, ExVarAreaDef> sheetVarSumMap = varAreaSumTypeMap.get(pk_sheet);
//								if (sheetVarSumMap == null) {
//									sheetVarSumMap = new HashMap<String, ExVarAreaDef>();
//									varAreaSumTypeMap.put(pk_sheet, sheetVarSumMap);
//								}
//								ExVarAreaDef vd = vdMap.get(vKey);
//								if (vd == null) {
//									vd = new ExVarAreaDef();
//									vd.varID = vKey;
//								}
//								if (varAreaSumType != ExVarAreaDef.varAreaSumType_default)
//									vd.varAreaSumType = varAreaSumType;
//								sheetVarSumMap.put(vKey, vd);
//							}
//						}
//					}
//				}
//			}
//		}
//		// --------------------------------------------------------
//		String pk_taskTo = taskTo.getPrimaryKey();
//		SuperVO[] vos = NtbSuperServiceGetter.getINtbSuper().queryByCondition(MdSheet.class, "pk_workbook='"+taskTo.getPk_workbook()+"'");
//		if (vos != null && vos.length > 0) {
//			SheetTableDMO stDmo = SheetTableDMO.getInstance();
//			HashMap<String, String[]> paradimMap = new HashMap<String, String[]>();
//			HashMap<String, List<HashMap<String,List<ExObjVarCell>>>> varMap = new HashMap<String, List<HashMap<String,List<ExObjVarCell>>>>();
//			for (int i=0; i<vos.length; i++) {
//				if (!sheetsMap.isEmpty() && !sheetsMap.containsKey(vos[i].getPrimaryKey()))
//					continue;
//				MdSheet sheet = (MdSheet)vos[i];
//				String tableKey = sheet.getTablekey();
//				if (tableKey != null) {
//					paradimMap.clear();
//					varMap.clear();
//					for (int j=0; j<taskFrom.length; j++) {
//						List<ExTaskSheetData> fmtData = stDmo.getTaskFmtData(tableKey, taskFrom[j].getPrimaryKey());
//						if (fmtData != null && !fmtData.isEmpty()) {
//							for (ExTaskSheetData data : fmtData) {
//								if (!stDmo.isVarMapEmpty(data)) {
//									String key = getKey(data.sheetparadim);
//									paradimMap.put(key, data.sheetparadim);
//									List<HashMap<String,List<ExObjVarCell>>> list = varMap.get(key);
//									if (list == null) {
//										list = new ArrayList<HashMap<String,List<ExObjVarCell>>>();
//										varMap.put(key, list);
//									}
//									list.add(data.varMap);
//								}
//							}
//						}
//					}
//					if (!varMap.isEmpty()) {
//						for (String key : varMap.keySet()) {
//							HashMap<String,List<ExObjVarCell>> saveValue = groupVarMaps(varMap.get(key), varAreaSumTypeMap.get(key));
//							saveValue = clearNullLine(saveValue, varAreaSumTypeMap.get(key));
//							if (saveValue != null && !saveValue.isEmpty()) {
//								stDmo.saveTaskFmtData(tableKey, pk_taskTo, paradimMap.get(key), null, saveValue);
//							}
//						}
//					}
//				}
//			}
//		}
//	}
//	/** 清除浮动区中的空行(列) */
//	private static HashMap<String,List<ExObjVarCell>> clearNullLine(HashMap<String,List<ExObjVarCell>> varCells,
//			HashMap<String, ExVarAreaDef> varAreaSumTypeMap/*boolean isRow*/) {
//		if (varAreaSumTypeMap == null)
//			varAreaSumTypeMap = new HashMap<String, ExVarAreaDef>();
//		HashMap<String,List<ExObjVarCell>> rtn = new HashMap<String,List<ExObjVarCell>>();
//		if (varCells != null) {
//			for (String varId : varCells.keySet()) {
//				ExVarAreaDef vd = varAreaSumTypeMap.get(varId);
//				boolean isRow = vd == null ? true : vd.varAreaType == ExVarAreaDef.varAreatType_ROW;
//				List<ExObjVarCell> l = varCells.get(varId);
//				if (l == null || l.isEmpty())
//					continue;
//				HashMap<Integer, List<ExObjVarCell>> map = new HashMap<Integer, List<ExObjVarCell>>();
//				for (ExObjVarCell vc : l) {
//					int idx = isRow ? vc.row : vc.col;
//					List<ExObjVarCell> tmpList = map.get(idx);
//					if (tmpList == null) {
//						tmpList = new ArrayList<ExObjVarCell>();
//						map.put(idx, tmpList);
//					}
//					tmpList.add(vc);
//				}
//				Integer[] idxArr = map.keySet().toArray(new Integer[0]);
//				Arrays.sort(idxArr);
//				ArrayList<Integer> removeList = new ArrayList<Integer>();
//				for (int i=0; i<idxArr.length; i++) {
//					List<ExObjVarCell> tmpList = map.get(idxArr[i]);
//					boolean isNull = true;
//					for (ExObjVarCell vc : tmpList) {
//						// 值为空，一定是null
//						if (vc.value == null || vc.value.length() == 0 || vc.value.trim().startsWith("?") ||
//								vc.value.trim().equals("VarStartFlag") || vc.value.trim().equals("N/H") ||
//								vc.value.trim().equals(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000423")/*—*/))
//							continue;
//						if (vc.fml != null && vc.fml.length() > 0) {
//							// 如果值不空但是有公式：判断值是否为0
//							try {
//								Double d = Double.valueOf(vc.value);
//								if (d > -0.00001 && d < 0.00001)
//									continue;
//							} catch (Throwable t) {
//							}
//						}
//						isNull = false;
//						break;
//					}
//					if (isNull) {
//						removeList.add(idxArr[i]);
//					}
//				}
//				if (!removeList.isEmpty()) {
//					Integer[] nullIdxArr = removeList.toArray(new Integer[0]);
//					Arrays.sort(nullIdxArr);
//					for (int i=nullIdxArr.length-1; i>=0; i--) {
//						for (Integer idx : map.keySet()) {
//							if (idx > nullIdxArr[i]) {
//								List<ExObjVarCell> tmpList = map.get(idx);
//								for (ExObjVarCell vc : tmpList) {
//									if (isRow)
//										vc.row--;
//									else
//										vc.col--;
//								}
//							}
//						}
//						map.remove(nullIdxArr[i]);
//					}
//				}
//				List<ExObjVarCell> list = new ArrayList<ExObjVarCell>();
//				for (int i=0; i<idxArr.length; i++) {
//					List<ExObjVarCell> tmpList = map.get(idxArr[i]);
//					if (tmpList != null) {
//						for (ExObjVarCell vc : tmpList) {
//							list.add(vc);
//						}
//					}
//				}
//				rtn.put(varId, list);
//			}
//		}
//		return rtn;
//	}
//	private static String getKey(String[] strs) {
//		StringBuffer sb = new StringBuffer();
//		if (strs != null && strs.length > 0) {
//			for (int i=0; i<strs.length; i++) {
//				if (strs[i] == null || strs[i].length() == 0)
//					sb.append(IFormConst.celldv_null);
//				else
//					sb.append(strs[i]);
//				sb.append(IFormConst.celldv_seperator);
//			}
//		}
//		return sb.toString();
//	}
//	/**
//	 * 合并浮动区(注：如果有浮动区有维度，则不合并直接返回空--只汇总无维度浮动区)
//	 * @param map
//	 * 2013-2-28 增加汇总方式
//	 * @return
//	 */
//	private static HashMap<String,List<ExObjVarCell>> groupVarMaps(List<HashMap<String,List<ExObjVarCell>>> map, HashMap<String, ExVarAreaDef> varAreaSumTypeMap) {
//		if (varAreaSumTypeMap == null)
//			varAreaSumTypeMap = new HashMap<String, ExVarAreaDef>();
//		HashMap<String,List<ExObjVarCell>> rtn = new HashMap<String,List<ExObjVarCell>>();
//		if (map != null && !map.isEmpty()) {
//			if (map.size() == 1)
//				rtn = map.get(0);
//			else {
//				ExVarCellComparator comp = new ExVarCellComparator();
//				// 注：仅支持行浮动
//				HashMap<String, List<List<ExObjVarCell>>> tmpMap = new HashMap<String, List<List<ExObjVarCell>>>();
//				for (HashMap<String,List<ExObjVarCell>> p : map) {
//					for (String key : p.keySet()) {
//						List<List<ExObjVarCell>> l = tmpMap.get(key);
//						if (l == null) {
//							l = new ArrayList<List<ExObjVarCell>>();
//							tmpMap.put(key, l);
//						}
//						l.add(p.get(key));
//					}
//				}
//				for (String varId : tmpMap.keySet()) {
//					ExVarAreaDef vd = varAreaSumTypeMap.get(varId);
//					if (vd != null && vd.varAreaSumType == ExVarAreaDef.varAreaSumType_NONE)
//						continue;
//					HashMap<Integer, Boolean> varKeyMap = new HashMap<Integer, Boolean>();
//					if (vd != null && vd.varDefList != null) {
//						for (ExVarDef vardef : vd.varDefList) {
//							if (vardef.isVarKey)
//								varKeyMap.put(vardef.index, true);
//						}
//					}
//					List<ExObjVarCell> list = new ArrayList<ExObjVarCell>();
//					rtn.put(varId, list);
//					HashMap<String, Object> dimKey = new HashMap<String, Object>();
//					List<ExObjVarCell> tmpList = new ArrayList<ExObjVarCell>();
//					int rowIdx = 0;
//					List<List<ExObjVarCell>> ll = tmpMap.get(varId);
//					for (List<ExObjVarCell> l : ll) {
//						ExObjVarCell[] arr = l.toArray(new ExObjVarCell[0]);
//						Arrays.sort(arr, comp);
//						int tmpRow = -1;
//						tmpList.clear();
//						StringBuffer sbKey = new StringBuffer();
//						for (int i=0; i<arr.length; i++) {
//							if (tmpRow < arr[i].row) {
//								// 开始新的一行
//								String key = sbKey.toString();
//								if (!dimKey.containsKey(key)) {
//									if (key != null && key.length() > 0)
//										dimKey.put(key, null);
//									boolean hasAddRow = false;
//									for (ExObjVarCell c : tmpList) {
//										c.row = rowIdx;
//										list.add(c);
//										hasAddRow = true;
//									}
//									if (hasAddRow)
//										rowIdx++;
//								}
//								tmpList.clear();
//								sbKey.setLength(0);
//								tmpRow = arr[i].row;
//							}
//							tmpList.add(arr[i]);
////							if (arr[i].pkDimLevel != null && arr[i].pkDimLevel.length() > 0)
////								sbKey.append(arr[i].col).append(IFormConst.celldv_null).append(arr[i].pkDimLevel).append(IFormConst.celldv_null).append(arr[i].value).append(IFormConst.celldv_seperator);
//							if (vd != null) {
//								if (vd.varAreaSumType == ExVarAreaDef.varAreaSumType_SUM) {
//									// 分类汇总
//									if (vd.varDefList == null || vd.varDefList.isEmpty()) {
//										// 未定义浮动区关键字：使用原有逻辑，按维度分类
//										if (arr[i].pkDimLevel != null && arr[i].pkDimLevel.length() > 0 && arr[i].value != null)
//											sbKey.append(arr[i].col).append(IFormConst.celldv_null).append(arr[i].pkDimLevel).append(IFormConst.celldv_null).append(arr[i].value).append(IFormConst.celldv_seperator);
//									}
//									else {
//										if (varKeyMap.containsKey(arr[i].col))
//											sbKey.append(arr[i].col).append(IFormConst.celldv_null).append(arr[i].cellID).append(IFormConst.celldv_null).append(arr[i].value).append(IFormConst.celldv_seperator);
//									}
//								}
//							}
//							else {
//								// 未设置汇总方式：使用原有逻辑，按维度分类
//								if (arr[i].pkDimLevel != null && arr[i].pkDimLevel.length() > 0 && arr[i].value != null)
//									sbKey.append(arr[i].col).append(IFormConst.celldv_null).append(arr[i].pkDimLevel).append(IFormConst.celldv_null).append(arr[i].value).append(IFormConst.celldv_seperator);
//							}
//						}
//						String key = sbKey.toString();
//						if (!dimKey.containsKey(key) && !tmpList.isEmpty()) {
//							if (key != null && key.length() > 0)
//								dimKey.put(key, null);
//							boolean hasAddRow = false;
//							for (ExObjVarCell c : tmpList) {
//								c.row = rowIdx;
//								list.add(c);
//								hasAddRow = true;
//							}
//							if (hasAddRow)
//								rowIdx++;
//						}
//					}
//				}
//			}
//		}
//		return rtn;
//	}

	@Override
	public MdTaskDef getHasOrgTaskDef(String pk_org) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		try {
			SuperVO[] vos = dmo.queryByWhereClause(MdTask.class,
					"versionstatus='"+ITaskConst.taskVersionStatus_compile+
					"' and managestatus='"+ITaskConst.taskManageStatus_unlock+
					"' and pk_planent='"+pk_org+"'");
			if (vos != null && vos.length > 0) {
				HashMap<String, MdWorkbook> map = new HashMap<String, MdWorkbook>();
				SuperVO[] books = dmo.queryByWhereClause(MdWorkbook.class, "booktype='"+IFormConst.strWbType_form+"' and isstarted='"+IFormConst.workbook_started+"'");
				if (books != null) {
					for (int i=0; i<books.length; i++) {
						map.put(books[i].getPrimaryKey(), (MdWorkbook)books[i]);
					}
				}
				for (int i=0; i<vos.length; i++) {
					MdTask task = (MdTask)vos[i];
					MdWorkbook book = map.get(task.getPk_workbook());
					if (book != null) {
//						String[] pks = book.getParadims().split(MdWorkbook.paradims_seperator);
//						// 是否只有默认的5个参数维：年、主体、币种、业务方案、版本
//						if (pks.length == 5)
							return (MdTaskDef)dmo.queryByPrimaryKey(MdTaskDef.class, task.getPk_taskdef());
					}
				}
//				String pk = ((MdTask)vos[0]).getPk_taskdef();
//				return (MdTaskDef)dmo.queryByPrimaryKey(MdTaskDef.class, pk);
			}
		} catch (Exception e) {
			NtbLogger.printException(e);
		}
		return null;
	}

	@Override
	public boolean isAutoSumTask(String pk_task) throws BusinessException {
		MdTask task = TbTaskCtl.getMdTaskByPk(pk_task, false);
		if (task == null)
			throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000424")/*没有找到对应任务！*/);
		HashMap<String, MdArea[]> areaMap = FormServiceGetter.getFormObjectService().getMdAreasByWorkbook(task.getPk_workbook(), null);
		if (areaMap != null && !areaMap.isEmpty()) {
			HashMap<String, Object> cdMap = new HashMap<String, Object>();
			ICubeDefQueryService icdqs = CubeServiceGetter.getCubeDefQueryService();
			for (MdArea[] areas : areaMap.values()) {
				if (areas == null)
					continue;
				for (MdArea area : areas) {
					String cubeCode = area.getCubecode();
					if (cdMap.containsKey(cubeCode))
						continue;
					try {
						CubeDef cd = icdqs.queryCubeDefByBusiCode(cubeCode);
						if (cd != null) {
							List<CubeDimUsage> cdus = cd.getCubeDimUsages();
							if (cdus != null) {
								for (CubeDimUsage cdu : cdus) {
									if (cdu.getSumFlag() != null && cdu.getSumFlag().booleanValue() && cdu.getDimDef() != null && cdu.getDimDef().getDimType() == DimDefType.ENTITY)
										return true;
								}
							}
						}
					} catch (BusinessException be) {
						NtbLogger.printException(be);
					}
					cdMap.put(cubeCode, null);
				}
			}
		}
		return false;
	}

	private void saveSumLog(MdTask task, MdTask[] fromTasks, boolean isSumDif, UserLoginVO userVo,
			String[] pk_sheets, int varAreaSumType, HashMap userInfo) {
		try {
			TaskLog log = new TaskLog();
			log.setPk_task(task.getPrimaryKey());
			log.setPrevstatus(task.getPlanstatus());
			log.setTbaction(isSumDif ? action_SumDif : action_Sum);
			log.setCreator(userVo.getPk_user());
			log.setCreationtime(DateCtl.getCurrentDateTime());
			log.setNextstatus(task.getPlanstatus());
			String pk = NtbSuperServiceGetter.getINtbSuper().insert(log);
			log.setPrimaryKey(pk);
			TaskLogExt rtn = new TaskLogExt();
			rtn.setPk_task(task.getPrimaryKey());
			rtn.setPk_tasklog(log.getPrimaryKey());
			rtn.setTbaction(isSumDif ? action_SumDif : action_Sum);
			HashMap<String, Object> extInfo = new HashMap<String, Object>();
			if (fromTasks.length > 0) {
				String[][] taskFromArr = new String[fromTasks.length][2];
				for (int i=0; i<fromTasks.length; i++) {
					taskFromArr[i][0] = fromTasks[i].getPrimaryKey();
					taskFromArr[i][1] = fromTasks[i].getObjname();
				}
				extInfo.put(extKey_taskFrom, taskFromArr);
			}
			if (pk_sheets != null && pk_sheets.length > 0)
				extInfo.put(extKey_pkSheets, pk_sheets);
			extInfo.put(extKey_varSumType, varAreaSumType);
			if (userInfo != null && !userInfo.isEmpty())
				extInfo.put(extKey_userInfo, userInfo);
			rtn.setExtinfo(extInfo);
			NtbSuperServiceGetter.getINtbSuper().insert(rtn);
		} catch (Throwable t) {
			NtbLogger.printException(t);
		}
	}


	public HashMap<String, Object> doLevelSum(TbSumParamVO sumparam, boolean isSumEffectiveData, 
			Map<String,String> map, Hashtable <String, Hashtable<String, List<String>>> table, 
			MdTask mdtask, DimLevel dimlevel) throws BusinessException {
		HashMap<String, Object> rtn = new HashMap<String, Object>();
		TaskDataModel taskDataModel = null;
		HashSet<String> updStatusTaskPks = new HashSet<String>();
		String  pkTaskTo="";
//		String where =TBSumPlanCommonToolsWithExcel.getQuerySqlByTask(mdtask,getSumedDimlevel());
//		MdTask []  tasks=TbTaskCtl.getMdTasksByWhere(where.toString(), true);
//		Map<String,String> map=filterSubmitAndApprovePassTasks(tasks,getSumedDimlevel());
		Set<String> keySet = map.keySet();
//		TbTreeNode node = (TbTreeNode) getTreePanel().getTreeModel().getRoot();
//		Hashtable<String, Hashtable<String, List<String>>> hashtable = new Hashtable<String, Hashtable<String, List<String>>>();
//		Hashtable <String, Hashtable<String, List<String>>>  table = getLevelNode(node, 0,hashtable);
		String []	fromUnicode=null;
		IPlanExtZjService iPlanExtZjService = PlanExtServiceGetter.getPlanExtZjService();
		for (int i = table.size() - 1; i >= 0; i--) {
			Hashtable<String, List<String>> levelTable = table.get(String
					.valueOf(i));
			for (Iterator<String> itr = levelTable.keySet().iterator(); itr
					.hasNext();) {
				String target = itr.next();
//				NtbLogger.print("target------->"+target);
				if (isSumEffectiveData) {//是否只汇总上报数据
					fromUnicode=TBSumPlanCommonToolsWithExcel.filterUnSubmit(levelTable.get(target), dimlevel, mdtask).toArray(new String [0]);
				}else{
					fromUnicode =  levelTable.get(target).toArray(new String [0]);
				}
//				HashMap<String, String> userInfo = new HashMap<String, String>();
//				userInfo.put("sumStyle",NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000278")/*逐级汇总*/ );
				if(keySet.contains(target)){
					pkTaskTo=map.get(target);
					NtbLogger.print("in processing of sum to----"+target);
//					TbSumParamVO sumparam = TbSumParamVO.getInstance();
					sumparam.pk_taskTo = pkTaskTo;
//					sumparam.sumParamDimLevel = getSumedDimlevel();
					sumparam.fromOrgUnicode = fromUnicode;
//					sumparam.userInfo = userInfo;
//					sumparam.userVo = getUserVo();
//					sumparam.pk_sumSheets = getSelectedSheetPks();
//					sumparam.varAreaSumType = getsumVartype();
//					sumparam.nodeType = getNodetype();
//					sumparam.isSumDataCell = !isAutoSumCube;
//					sumparam.calculateSheetPks = calculateSheetPks;
					taskDataModel=iPlanExtZjService.sumPlanByparadimAndCalculate(sumparam);
					updStatusTaskPks.add(pkTaskTo);
				}
			}
		}
		rtn.put("updStatusTaskPks", updStatusTaskPks);
		rtn.put("taskDataModel", taskDataModel);
		return rtn;
	}

	@Override
	public TaskDataModel sumPlanAndCalculate(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo,String key_nodeType) throws BusinessException {
	CostTime  ct=new CostTime();
	boolean suc = PKLock.getInstance().addDynamicLock(pk_taskTo);
	if(!suc)throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0530")/*@res "该任务正在被他人汇总，请稍后重试。"*/);
	sumPlan(pk_taskTo, fromOrgUnicode, userVo, pk_sheets, varAreaSumType, userInfo, true);
		ct.printStepCost("sumtotal cost------->:");
		MdTask task = TbTaskCtl.getMdTaskByPk(pk_taskTo, false);
		boolean isLoadConsistRule=true;
		TaskDataModel taskDataModel=null;
		CostTime  ct1=new CostTime();
		 taskDataModel = TaskDataCtl.getTaskDataModel(task.getPrimaryKey(),
				 null, isLoadConsistRule, null, true);
		 ct1.printStepCost("getTaskDataModel cost----------->:");
	List<BusiRuleVO> defaultRules = null;
	//编制执行 计算规则
	if(ITbPlanActionCode.COM_NODETYPE.equals(key_nodeType)
			||ITbPlanActionCode.TABLEOFTOP_NODETYPE.equals(key_nodeType)
			||ITbPlanActionCode.DIRECTADJUST_NODETYPE.equals(key_nodeType)){
		CostTime  ct2=new CostTime();
	defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(task
			.getPk_taskdef(), NTBActionEnum.CALACTION
			.toCodeString());
	if (defaultRules != null && !defaultRules.isEmpty()) {
	List<AllotFormulaForTModelVO> allotVos = RuleExecuteHelper.getAllotRule(taskDataModel,defaultRules);
	if (allotVos != null) {
		taskDataModel.getFormulaCellInfos().clear();
		taskDataModel.getFormulaCellInfos().addAll(FormulaCellInfo.convertCellInfo(allotVos));
		taskDataModel.initFmlMap(CellFmlInfo.fmlType_calaction);
	}
	}
	ct2.printStepCost(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000699")/*规则执行 cost----------->:*/);

	//日常执行取数规则,加载CellFmlInfo.fmlType_calaction类型公式
	}else if(ITbPlanActionCode.DAILY_NODETYPE.equals(key_nodeType)){
	defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(task.getPk_taskdef(),
			NTBActionEnum.GETDATAACTION.toCodeString());
	if (defaultRules != null && !defaultRules.isEmpty()) {
		List<AllotFormulaForTModelVO> allotVos = RuleServiceGetter.getRuleExecuteService().getAllotFormulaForWorkBook(
		TbTaskCtl.getTaskParadim(task), defaultRules,taskDataModel);

		if (allotVos != null) {
			taskDataModel.getFormulaCellInfos().clear();
			taskDataModel.getFormulaCellInfos().addAll(FormulaCellInfo.convertCellInfo(allotVos));
			taskDataModel.initFmlMap(CellFmlInfo.fmlType_calaction);
		}

	}
	}
	CostTime  ct3=new CostTime();
	RuleExecuteHelper.executeWorkBookRule(taskDataModel, defaultRules);
	TaskDataModelAction action = new TaskDataModelAction(taskDataModel);
	action.executeWorkBook();
	ct3.printStepCost(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000700")/*excel公式执行 cost----------->:*/);
	taskDataModel.save();
	TaskDataCtl.initTaskDataModelRuleInfo(taskDataModel, key_nodeType);
	return taskDataModel;

	}

@Override
public TaskDataModel sumPlanDifAndCalculate(String pk_upLvlPlan,
		String[] lowerLvlOrgUnicode, String difOrgUnicode,
		UserLoginVO userVo, String[] pk_sheets, HashMap userInfo,
		String key_nodeType) throws BusinessException {
	ITaskExcelService service = NCLocator.getInstance().lookup(
			ITaskExcelService.class);
	MdTask  diftask =service.getMdTaskByTaskAndOrg(pk_upLvlPlan, difOrgUnicode);
	boolean suc = PKLock.getInstance().addDynamicLock(diftask.getPrimaryKey());
	if(!suc)throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0530")/*@res "该任务正在被他人汇总，请稍后重试。"*/);
	CostTime  ct=new CostTime();
	sumPlanDif(pk_upLvlPlan,  lowerLvlOrgUnicode, difOrgUnicode,  userVo,  pk_sheets,  userInfo);
	ct.printStepCost(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000701")/*汇总过程耗时---------->:*/);
	boolean isLoadConsistRule=true;
	TaskDataModel taskDataModel=null;
	CostTime  ct1=new CostTime();
	 taskDataModel = TaskDataCtl.getTaskDataModel(diftask.getPrimaryKey(),
			null, isLoadConsistRule, null, true);
	ct1.printStepCost(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000702")/*查taskdatamodel  cost------->:*/);
	 List<BusiRuleVO> defaultRules = null;
	 CostTime  ct2=new CostTime();
	//编制执行 计算规则
	if(ITbPlanActionCode.COM_NODETYPE.equals(key_nodeType)
			||ITbPlanActionCode.TABLEOFTOP_NODETYPE.equals(key_nodeType)
			||ITbPlanActionCode.DIRECTADJUST_NODETYPE.equals(key_nodeType)){
	defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(diftask
			.getPk_taskdef(), NTBActionEnum.CALACTION.toCodeString());
	//日常执行取数规则
	}else if(ITbPlanActionCode.DAILY_NODETYPE.equals(key_nodeType)){
	defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(diftask
			.getPk_taskdef(), NTBActionEnum.GETDATAACTION.toCodeString());
	}
	RuleExecuteHelper.executeWorkBookRule(taskDataModel, defaultRules);
	ct2.printStepCost(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000703")/*规则执行 cost------------>:*/);
	CostTime  ct3=new CostTime();
	TaskDataModelAction action = new TaskDataModelAction(taskDataModel);
	action.executeWorkBook();
	ct3.printStepCost(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000704")/*Excel公式 cost-------->*/);
	taskDataModel.save();
	return taskDataModel;

}

	public TaskDataModel sumPlanByparadimAndOnlyRule(TbSumParamVO sumparams) throws BusinessException{
	
		List<BusiRuleVO> defaultRules = null;
		String nodeType = sumparams.nodeType; 
		String pk_taskTo = sumparams.pk_taskTo;
		CostTime  ct=new CostTime();
		boolean suc = PKLock.getInstance().addDynamicLock(sumparams.pk_taskTo);
		if(!suc)throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0530")/*@res "该任务正在被他人汇总，请稍后重试。"*/);
		if(sumparams.sumParamDimLevel==null){sumparams.sumParamDimLevel=DimServiceGetter.getDimManager().getDimLevelByPK(IDimLevelPKConst.ENT);}
		
		sumPlanByParamDimLevel(sumparams.pk_taskTo,sumparams.sumParamDimLevel, 
				sumparams.fromOrgUnicode, sumparams.userVo,
				sumparams.pk_sumSheets, sumparams.varAreaSumType,
				sumparams.userInfo, sumparams.isSumDataCell);
		
			ct.printStepCost("sumtotal cost------->:");
			MdTask task = TbTaskCtl.getMdTaskByPk(pk_taskTo, false);
			boolean isLoadConsistRule=true;
			TaskDataModel taskDataModel=null;
			CostTime  ct1=new CostTime();
			 taskDataModel = TaskDataCtl.getTaskDataModel(task.getPrimaryKey(),
					 sumparams.calculateSheetPks, isLoadConsistRule, null, true);
			 ct1.printStepCost("getTaskDataModel cost----------->:");
		
		//编制执行 计算规则
		if(ITbPlanActionCode.COM_NODETYPE.equals(nodeType)
				||ITbPlanActionCode.TABLEOFTOP_NODETYPE.equals(nodeType)
				||ITbPlanActionCode.DIRECTADJUST_NODETYPE.equals(nodeType)){
			CostTime  ct2=new CostTime();
		defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(task
				.getPk_taskdef(), NTBActionEnum.CALACTION
				.toCodeString());
		if (defaultRules != null && !defaultRules.isEmpty()) {
		List<AllotFormulaForTModelVO> allotVos = RuleExecuteHelper.getAllotRule(taskDataModel,defaultRules);
		if (allotVos != null) {
			taskDataModel.getFormulaCellInfos().clear();
			taskDataModel.getFormulaCellInfos().addAll(FormulaCellInfo.convertCellInfo(allotVos));
			taskDataModel.initFmlMap(CellFmlInfo.fmlType_calaction);
		}
		}
		ct2.printStepCost(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000699")/*规则执行 cost----------->:*/);
	
		//日常执行取数规则,加载CellFmlInfo.fmlType_calaction类型公式
		}else if(ITbPlanActionCode.DAILY_NODETYPE.equals(sumparams.nodeType)){
		defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(task.getPk_taskdef(),
				NTBActionEnum.GETDATAACTION.toCodeString());
		if (defaultRules != null && !defaultRules.isEmpty()) {
			List<AllotFormulaForTModelVO> allotVos = RuleServiceGetter.getRuleExecuteService().getAllotFormulaForWorkBook(
			TbTaskCtl.getTaskParadim(task), defaultRules,taskDataModel);
	
			if (allotVos != null) {
				taskDataModel.getFormulaCellInfos().clear();
				taskDataModel.getFormulaCellInfos().addAll(FormulaCellInfo.convertCellInfo(allotVos));
				taskDataModel.initFmlMap(CellFmlInfo.fmlType_calaction);
			}
	
		}
		}
//		CostTime  ct3=new CostTime();
//		RuleExecuteHelper.executeWorkBookRule(taskDataModel, defaultRules);
//		TaskDataModelAction action = new TaskDataModelAction(taskDataModel);
//		action.executeWorkBook();
//		ct3.printStepCost(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000700")/*excel公式执行 cost----------->:*/);
		taskDataModel.save();
		TaskDataCtl.initTaskDataModelRuleInfo(taskDataModel, nodeType);
		return taskDataModel;
	
	}

@Override
public TaskDataModel sumPlanByparadimAndCalculate(TbSumParamVO sumparams) throws BusinessException {
	List<BusiRuleVO> defaultRules = null;
	String nodeType = sumparams.nodeType; 
	String pk_taskTo = sumparams.pk_taskTo;
	CostTime  ct=new CostTime();
	boolean suc = PKLock.getInstance().addDynamicLock(sumparams.pk_taskTo);
	if(!suc)throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0530")/*@res "该任务正在被他人汇总，请稍后重试。"*/);
	if(sumparams.sumParamDimLevel==null){sumparams.sumParamDimLevel=DimServiceGetter.getDimManager().getDimLevelByPK(IDimLevelPKConst.ENT);}
	
	sumPlanByParamDimLevel(sumparams.pk_taskTo,sumparams.sumParamDimLevel, 
			sumparams.fromOrgUnicode, sumparams.userVo,
			sumparams.pk_sumSheets, sumparams.varAreaSumType,
			sumparams.userInfo, sumparams.isSumDataCell);
	
		ct.printStepCost("sumtotal cost------->:");
		MdTask task = TbTaskCtl.getMdTaskByPk(pk_taskTo, false);
		boolean isLoadConsistRule=true;
		TaskDataModel taskDataModel=null;
		CostTime  ct1=new CostTime();
		 taskDataModel = TaskDataCtl.getTaskDataModel(task.getPrimaryKey(),
				 sumparams.calculateSheetPks, isLoadConsistRule, null, true);
		 ct1.printStepCost("getTaskDataModel cost----------->:");
	
	//编制执行 计算规则
	if(ITbPlanActionCode.COM_NODETYPE.equals(nodeType)
			||ITbPlanActionCode.TABLEOFTOP_NODETYPE.equals(nodeType)
			||ITbPlanActionCode.DIRECTADJUST_NODETYPE.equals(nodeType)){
		CostTime  ct2=new CostTime();
	defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(task
			.getPk_taskdef(), NTBActionEnum.CALACTION
			.toCodeString());
	if (defaultRules != null && !defaultRules.isEmpty()) {
	List<AllotFormulaForTModelVO> allotVos = RuleExecuteHelper.getAllotRule(taskDataModel,defaultRules);
	if (allotVos != null) {
		taskDataModel.getFormulaCellInfos().clear();
		taskDataModel.getFormulaCellInfos().addAll(FormulaCellInfo.convertCellInfo(allotVos));
		taskDataModel.initFmlMap(CellFmlInfo.fmlType_calaction);
	}
	}
	ct2.printStepCost(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000699")/*规则执行 cost----------->:*/);

	//日常执行取数规则,加载CellFmlInfo.fmlType_calaction类型公式
	}else if(ITbPlanActionCode.DAILY_NODETYPE.equals(sumparams.nodeType)){
	defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(task.getPk_taskdef(),
			NTBActionEnum.GETDATAACTION.toCodeString());
	if (defaultRules != null && !defaultRules.isEmpty()) {
		List<AllotFormulaForTModelVO> allotVos = RuleServiceGetter.getRuleExecuteService().getAllotFormulaForWorkBook(
		TbTaskCtl.getTaskParadim(task), defaultRules,taskDataModel);

		if (allotVos != null) {
			taskDataModel.getFormulaCellInfos().clear();
			taskDataModel.getFormulaCellInfos().addAll(FormulaCellInfo.convertCellInfo(allotVos));
			taskDataModel.initFmlMap(CellFmlInfo.fmlType_calaction);
		}

	}
	}
	CostTime  ct3=new CostTime();
	RuleExecuteHelper.executeWorkBookRule(taskDataModel, defaultRules);
	TaskDataModelAction action = new TaskDataModelAction(taskDataModel);
	action.executeWorkBook();
	ct3.printStepCost(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000700")/*excel公式执行 cost----------->:*/);
	taskDataModel.save();
	TaskDataCtl.initTaskDataModelRuleInfo(taskDataModel, nodeType);
	return taskDataModel;
}

@Override
public TaskDataModel sumPlanAndCalOnlyRule(String pk_taskTo,
		String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets,
		int varAreaSumType, HashMap userInfo, String key_nodeType)
		throws BusinessException {
		CostTime  ct=new CostTime();
		boolean suc = PKLock.getInstance().addDynamicLock(pk_taskTo);
		if(!suc)throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0530")/*@res "该任务正在被他人汇总，请稍后重试。"*/);
		sumPlan(pk_taskTo, fromOrgUnicode, userVo, pk_sheets, varAreaSumType, userInfo, true);
			ct.printStepCost("sumtotal cost------->:");
			MdTask task = TbTaskCtl.getMdTaskByPk(pk_taskTo, false);
			boolean isLoadConsistRule=true;
			TaskDataModel taskDataModel=null;
			CostTime  ct1=new CostTime();
			 taskDataModel = TaskDataCtl.getTaskDataModel(task.getPrimaryKey(),
					 null, isLoadConsistRule, null, true);
			 ct1.printStepCost("getTaskDataModel cost----------->:");
		List<BusiRuleVO> defaultRules = null;
		//编制执行 计算规则
		if(ITbPlanActionCode.COM_NODETYPE.equals(key_nodeType)
				||ITbPlanActionCode.TABLEOFTOP_NODETYPE.equals(key_nodeType)
				||ITbPlanActionCode.DIRECTADJUST_NODETYPE.equals(key_nodeType)){
			CostTime  ct2=new CostTime();
		defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(task
				.getPk_taskdef(), NTBActionEnum.CALACTION
				.toCodeString());
		if (defaultRules != null && !defaultRules.isEmpty()) {
		List<AllotFormulaForTModelVO> allotVos = RuleExecuteHelper.getAllotRule(taskDataModel,defaultRules);
		if (allotVos != null) {
			taskDataModel.getFormulaCellInfos().clear();
			taskDataModel.getFormulaCellInfos().addAll(FormulaCellInfo.convertCellInfo(allotVos));
			taskDataModel.initFmlMap(CellFmlInfo.fmlType_calaction);
		}
		}
		ct2.printStepCost(NCLangResOnserver.getInstance().getStrByID("tbb_plan", "01812pln_000699")/*规则执行 cost----------->:*/);
	
		//日常执行取数规则,加载CellFmlInfo.fmlType_calaction类型公式
		}else if(ITbPlanActionCode.DAILY_NODETYPE.equals(key_nodeType)){
		defaultRules = RuleManager.getNeedExecuteRuleByTaskDefAndAction(task.getPk_taskdef(),
				NTBActionEnum.GETDATAACTION.toCodeString());
		if (defaultRules != null && !defaultRules.isEmpty()) {
			List<AllotFormulaForTModelVO> allotVos = RuleServiceGetter.getRuleExecuteService().getAllotFormulaForWorkBook(
			TbTaskCtl.getTaskParadim(task), defaultRules,taskDataModel);
	
			if (allotVos != null) {
				taskDataModel.getFormulaCellInfos().clear();
				taskDataModel.getFormulaCellInfos().addAll(FormulaCellInfo.convertCellInfo(allotVos));
				taskDataModel.initFmlMap(CellFmlInfo.fmlType_calaction);
			}
	
		}
		}
		
		taskDataModel.save();
		TaskDataCtl.initTaskDataModelRuleInfo(taskDataModel, key_nodeType);
		return taskDataModel;

	}

}