/**
 * 
 */
package nc.itf.tb.plan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.ms.tb.ext.zior.xml.PlanFrameModel;
import nc.ms.tb.ext.zior.xml.ZiorOpenNodeModel;
import nc.ms.tb.task.data.TaskDataModel;
import nc.vo.mdm.cube.CubeSnapShot;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.sm.funcreg.FuncRegisterVO;
import nc.vo.tb.form.excel.ExOlapInfoSet;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;

/**
 * 报表框架模型加载服务
 * @author liyingm
 * 
 */
public interface IZiorFrameModelService {
	public PlanFrameModel getPlanFrameModelByFuncode(String funcode)
			throws BusinessException;
	public ZiorOpenNodeModel getZiorOpenNodeModel(FuncRegisterVO frVO,String sysCode ,String mdTaskDefName, String fileConfig,String nodepk )
	throws BusinessException;
	public ZiorOpenNodeModel refreshZiorOpenNodeModelByMdTaskDef(ZiorOpenNodeModel  ziorOpenNodeModel,MdTaskDef def)
	throws BusinessException;
	public ZiorOpenNodeModel refreshModelByMultiTaskRefresh(ZiorOpenNodeModel ziorOpenNodeModel)
	throws BusinessException;
	public ZiorOpenNodeModel refreshModelBySingleTaskParadim(ZiorOpenNodeModel ziorOpenNodeModel)
	throws BusinessException;
	public ZiorOpenNodeModel refreshModelByRefeshAction(ZiorOpenNodeModel ziorOpenNodeModel)
	throws BusinessException;
	/**
	 * 加载任务数据TaskDataModel+公式和规则和规则成员
	 * @param pk_task
	 * @param pk_sheets
	 * @param isLoadConsistRule
	 * @param snapShotMap
	 * @param isInitFunctionText
	 * @param nodeType
	 * @param taskType
	 * @return
	 * @throws BusinessException
	 */
	public Map<String,Object> getTaskDataMap(MdTask task, String[] pk_sheets, boolean isLoadConsistRule, HashMap<String, CubeSnapShot> snapShotMap, boolean isInitFunctionText,String nodeType,Map<String,Object> paras)
	throws BusinessException;
	public MdTask[] getCanApproveTasks(MdTask[] tasks)
	throws BusinessException;
	public  Map<String,Map> getSheetGroupMapByPkWorkBook(MdTaskDef[] taskDefs)
			throws BusinessException;
	public  String[] getTaskValidLookPkSheets(MdTask task, String[] publicnodeLookSheets,String sheetGroupName,boolean isApp,String pk_org,MdTaskDef def)
			throws BusinessException;
	/**
	 * 保存任务的批注信息
	 * @param primaryKey
	 * @param olapInfoList
	 * @throws BusinessException
	 */
	public  void saveExOlapInfoSet(String primaryKey,
			List<ExOlapInfoSet> olapInfoList)
			throws BusinessException;
	public  Map<String,Object> saveTaskDataMesMap(String nodetype,TaskDataModel taskDataModel,MdTask task)
			throws BusinessException;
}
