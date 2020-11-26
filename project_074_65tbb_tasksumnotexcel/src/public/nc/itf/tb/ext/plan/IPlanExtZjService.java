package nc.itf.tb.ext.plan;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import nc.ms.tb.task.data.TaskDataModel;
import nc.vo.mdm.cube.DimSectionSetTuple;
import nc.vo.mdm.cube.ICubeDataSet;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.pub.BusinessException;
import nc.vo.tb.form.excel.ExDataCell;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.plan.TbSumParamVO;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;

/**
 * 中建项目任务功能接口
 * @author lrx
 *
 */
public interface IPlanExtZjService {

	/**
	 * 指标查询
	 * @param querySliceRules
	 * @return
	 * @throws BusinessException
	 */
	public HashMap<String, ICubeDataSet> queryByOrg(HashMap<String, DimSectionSetTuple> querySliceRules) throws BusinessException;
	
	/**
	 * 汇总(请注意只有一个pk是任务的，其他都是主体unicode)
	 * 汇总仅限与任务主体一致的多维数据
	 * @param pk_toPlan			- 汇总到的目标任务pk
	 * @param fromOrgUnicode	- 汇总来源主体unicode
	 * @param HashMap userInfo	- 用户自定义信息(记录在汇总日志中)
	 * @param varAreaSumType - 2013-2-28 增加参数：浮动区汇总方式
	 * 			ExVarAreaDef.varAreaSumType_default = -1; // 汇总方式：默认
	 * 			ExVarAreaDef.varAreaSumType_LIST = 0; // 罗列汇总
	 * 			ExVarAreaDef.varAreaSumType_SUM = 1;  // 分类汇总
	 * 			ExVarAreaDef.varAreaSumType_NONE = 2; // 不汇总	 
	 * @throws BusinessException
	 */
//	public void sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, HashMap userInfo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, HashMap userInfo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException;
	/**
	 * 差额汇总(请注意只有一个pk是任务的，其他都是主体unicode)
	 * 汇总仅限与任务主体一致的多维数据
	 * @param pk_upLvlPlan			- 汇总来源的上级主体任务pk
	 * @param lowerLvlOrgUnicode	- 汇总来源的下级主体unicode
	 * @param difOrgUnicode			- 汇总到的差额主体unicode
	 * @param HashMap userInfo		- 用户自定义信息(记录在汇总日志中)
	 * @return -- 差额主体pk
	 * @throws BusinessException
	 */
//	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo) throws BusinessException;
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo) throws BusinessException;
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo, HashMap userInfo) throws BusinessException;
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo, String[] pk_sheets) throws BusinessException;
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo, String[] pk_sheets, HashMap userInfo) throws BusinessException;
	
	/**
	 * 汇总(含减法)
	 * @param taskTo	- 汇总目标任务
	 * @param taskFrom	- 汇总来源任务
	 * @param isAdd		- 汇总来源任务在汇总中的计算方式(true: 加; false: 减)
	 * @param HashMap userInfo		- 用户自定义信息(记录在汇总日志中)
	 * 			ExVarAreaDef.varAreaSumType_default = -1; // 汇总方式：默认
	 * 			ExVarAreaDef.varAreaSumType_LIST = 0; // 罗列汇总
	 * 			ExVarAreaDef.varAreaSumType_SUM = 1;  // 分类汇总
	 * 			ExVarAreaDef.varAreaSumType_NONE = 2; // 不汇总	 
	 * @param varAreaSumType - 2013-2-28 增加参数：浮动区汇总方式
	 * 汇总仅限与任务主体一致的多维数据
	 * @throws BusinessException
	 */
//	public void sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, HashMap userInfo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException;
	
	/**
	 * 汇总任务浮动区
	 * @param pk_taskTo
	 * @param fromOrgUnicode
	 * @param userVo
	 * @return
	 * @throws BusinessException
	 */
	public HashMap<String, HashMap<String,ExDataCell>> sumPlanVarArea(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlanVarArea(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlanVarArea(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException;
	
	/**
	 * 指定参数维度汇总任务/浮动区
	 * 注：参数与之前的按主体汇总基本一致，增加了一个汇总DimLevel，需要指定(直接直接默认为主体)，该维度必须是任务参数维
	 * @param pk_taskTo
	 * @param sumParamDimLevel
	 * @param fromLevelValueUnicode
	 * @param userVo
	 * @param pk_sheets
	 * @param varAreaSumType
	 * @param userInfo
	 * @return
	 * @throws BusinessException
	 * @author: lrx@yonyou.com
	 */
	public HashMap<String, HashMap<String,ExDataCell>> sumPlanVarAreaByParamDimLevel(String pk_taskTo, DimLevel sumParamDimLevel, String[] fromLevelValueUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlanByParamDimLevel(String pk_taskTo, DimLevel sumParamDimLevel, String[] fromLevelValueUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException;
	
	/**
	 * 按组织查询存在可用任务的任务模板(随机一个)
	 * @param pk_org
	 * @return
	 * @throws BusinessException
	 */
	public MdTaskDef getHasOrgTaskDef(String pk_org) throws BusinessException;

	/**
	 * 检查任务是否是自动汇总
	 * @param pk_task
	 * @return
	 * @throws BusinessException
	 */
	public boolean isAutoSumTask(String pk_task) throws BusinessException;
/**
 * 汇总后执行计算公式  add  by  pengzhena  2013.05.09
 * @param pk_taskTo
 * @param fromOrgUnicode
 * @param userVo
 * @param pk_sheets
 * @param varAreaSumType
 * @param userInfo
 * @param key_nodeType
 * @return
 * @throws BusinessException
 */
	
	public TaskDataModel sumPlanAndCalculate(String pk_taskTo,String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo,String key_nodeType) throws BusinessException;
	
	
	
	
	/**
	 * 汇总后执行计算公式  add  by 王志强 at:2020/07/14
	 * @param pk_taskTo
	 * @param fromOrgUnicode
	 * @param userVo
	 * @param pk_sheets
	 * @param varAreaSumType
	 * @param userInfo
	 * @param key_nodeType
	 * @return
	 * @throws BusinessException
	 */
		
		public TaskDataModel sumPlanAndCalOnlyRule(String pk_taskTo,String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo,String key_nodeType) throws BusinessException;
	/**
	 * 差额汇总后执行公式  add  by  pengzhena 2013.5.20
	 * @param pk_upLvlPlan
	 * @param lowerLvlOrgUnicode
	 * @param difOrgUnicode
	 * @param userVo
	 * @param pk_sheets
	 * @param userInfo
	 * @param key_nodeType
	 * @return
	 * @throws BusinessException
	 */
	public TaskDataModel sumPlanDifAndCalculate(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo, String[] pk_sheets, HashMap userInfo,String key_nodeType ) throws BusinessException;
//	/**
//	 * 
//	 * {多维度汇总并执行公式计算}
//	 * 
//	 * @param pk_taskTo
//	 * @param sumParamDimLevel
//	 * @param fromOrgUnicode
//	 * @param userVo
//	 * @param pk_sheets--汇总表单范围
//	 * @param varAreaSumType
//	 * @param userInfo
//	 * @param key_nodeType
//	 * @param isSumDataCell
//	 * @param  calculateSheetPks---20141117增加汇总完之后可计算表单范围
//	 * @return
//	 * @throws BusinessException
//	 * @author: pengzhena@yonyou.com
//	 */
//	public TaskDataModel sumPlanByparadimAndCalculate(String pk_taskTo, DimLevel sumParamDimLevel,String[] fromOrgUnicode,
//			UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo,String key_nodeType , boolean isSumDataCell,
//			String [] calculateSheetPks) throws BusinessException;
	/**
	 * {多维度汇总并执行公式计算}
	 * @param sumparams
	 * @return
	 * @author pengzhena 20150130
	 * @throws BusinessException
	 */
	public TaskDataModel sumPlanByparadimAndCalculate(TbSumParamVO sumparams) throws BusinessException;
	
	
	/**
	 * 汇总后执行计算公式  add  by 王志强 at:2020/07/14
	 * @param pk_taskTo
	 * @param fromOrgUnicode
	 * @param userVo
	 * @param pk_sheets
	 * @param varAreaSumType
	 * @param userInfo
	 * @param key_nodeType
	 * @return
	 * @throws BusinessException
	 */
	public TaskDataModel sumPlanByparadimAndOnlyRule(TbSumParamVO sumparams) throws BusinessException;
	
	/**
	 * lrx 2015-8-28 逐级汇总放到后台进行
	 * @param sumparam	- 汇总参数,没有pk_taskTo和fromOrgUnicode,需要逐级汇总时填上
	 * @param isSumEffectiveData	- 汇总上报数据
	 * @param map	- SumSingleSheetDlg.doLevelSum()方法中构造的map
	 * @param table	- SumSingleSheetDlg.doLevelSum()方法中构造的table
	 * @return	- updStatusTaskPks:SumSingleSheetDlg.doLevelSum()方法中的updStatusTaskPks; taskDataModel:汇总结果TaskDataModel
	 * @throws BusinessException
	 * @author: lrx@yonyou.com
	 */
	public HashMap<String, Object> doLevelSum(TbSumParamVO sumparam, boolean isSumEffectiveData, 
			Map<String,String> map, Hashtable <String, Hashtable<String, List<String>>> table, 
			MdTask mdtask, DimLevel dimlevel) throws BusinessException;
}
