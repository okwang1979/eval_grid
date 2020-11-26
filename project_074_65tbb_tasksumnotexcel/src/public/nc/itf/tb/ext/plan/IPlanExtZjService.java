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
 * �н���Ŀ�����ܽӿ�
 * @author lrx
 *
 */
public interface IPlanExtZjService {

	/**
	 * ָ���ѯ
	 * @param querySliceRules
	 * @return
	 * @throws BusinessException
	 */
	public HashMap<String, ICubeDataSet> queryByOrg(HashMap<String, DimSectionSetTuple> querySliceRules) throws BusinessException;
	
	/**
	 * ����(��ע��ֻ��һ��pk������ģ�������������unicode)
	 * ���ܽ�������������һ�µĶ�ά����
	 * @param pk_toPlan			- ���ܵ���Ŀ������pk
	 * @param fromOrgUnicode	- ������Դ����unicode
	 * @param HashMap userInfo	- �û��Զ�����Ϣ(��¼�ڻ�����־��)
	 * @param varAreaSumType - 2013-2-28 ���Ӳ��������������ܷ�ʽ
	 * 			ExVarAreaDef.varAreaSumType_default = -1; // ���ܷ�ʽ��Ĭ��
	 * 			ExVarAreaDef.varAreaSumType_LIST = 0; // ���л���
	 * 			ExVarAreaDef.varAreaSumType_SUM = 1;  // �������
	 * 			ExVarAreaDef.varAreaSumType_NONE = 2; // ������	 
	 * @throws BusinessException
	 */
//	public void sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, HashMap userInfo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, HashMap userInfo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(String pk_taskTo, String[] fromOrgUnicode, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException;
	/**
	 * ������(��ע��ֻ��һ��pk������ģ�������������unicode)
	 * ���ܽ�������������һ�µĶ�ά����
	 * @param pk_upLvlPlan			- ������Դ���ϼ���������pk
	 * @param lowerLvlOrgUnicode	- ������Դ���¼�����unicode
	 * @param difOrgUnicode			- ���ܵ��Ĳ������unicode
	 * @param HashMap userInfo		- �û��Զ�����Ϣ(��¼�ڻ�����־��)
	 * @return -- �������pk
	 * @throws BusinessException
	 */
//	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo) throws BusinessException;
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo) throws BusinessException;
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo, HashMap userInfo) throws BusinessException;
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo, String[] pk_sheets) throws BusinessException;
	public String sumPlanDif(String pk_upLvlPlan, String[] lowerLvlOrgUnicode, String difOrgUnicode, UserLoginVO userVo, String[] pk_sheets, HashMap userInfo) throws BusinessException;
	
	/**
	 * ����(������)
	 * @param taskTo	- ����Ŀ������
	 * @param taskFrom	- ������Դ����
	 * @param isAdd		- ������Դ�����ڻ����еļ��㷽ʽ(true: ��; false: ��)
	 * @param HashMap userInfo		- �û��Զ�����Ϣ(��¼�ڻ�����־��)
	 * 			ExVarAreaDef.varAreaSumType_default = -1; // ���ܷ�ʽ��Ĭ��
	 * 			ExVarAreaDef.varAreaSumType_LIST = 0; // ���л���
	 * 			ExVarAreaDef.varAreaSumType_SUM = 1;  // �������
	 * 			ExVarAreaDef.varAreaSumType_NONE = 2; // ������	 
	 * @param varAreaSumType - 2013-2-28 ���Ӳ��������������ܷ�ʽ
	 * ���ܽ�������������һ�µĶ�ά����
	 * @throws BusinessException
	 */
//	public void sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, HashMap userInfo) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType) throws BusinessException;
	public HashMap<String, HashMap<String,ExDataCell>> sumPlan(MdTask taskTo, MdTask[] taskFrom, boolean[] isAdd, UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo) throws BusinessException;
	
	/**
	 * �������񸡶���
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
	 * ָ������ά�Ȼ�������/������
	 * ע��������֮ǰ�İ�������ܻ���һ�£�������һ������DimLevel����Ҫָ��(ֱ��ֱ��Ĭ��Ϊ����)����ά�ȱ������������ά
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
	 * ����֯��ѯ���ڿ������������ģ��(���һ��)
	 * @param pk_org
	 * @return
	 * @throws BusinessException
	 */
	public MdTaskDef getHasOrgTaskDef(String pk_org) throws BusinessException;

	/**
	 * ��������Ƿ����Զ�����
	 * @param pk_task
	 * @return
	 * @throws BusinessException
	 */
	public boolean isAutoSumTask(String pk_task) throws BusinessException;
/**
 * ���ܺ�ִ�м��㹫ʽ  add  by  pengzhena  2013.05.09
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
	 * ���ܺ�ִ�м��㹫ʽ  add  by ��־ǿ at:2020/07/14
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
	 * �����ܺ�ִ�й�ʽ  add  by  pengzhena 2013.5.20
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
//	 * {��ά�Ȼ��ܲ�ִ�й�ʽ����}
//	 * 
//	 * @param pk_taskTo
//	 * @param sumParamDimLevel
//	 * @param fromOrgUnicode
//	 * @param userVo
//	 * @param pk_sheets--���ܱ���Χ
//	 * @param varAreaSumType
//	 * @param userInfo
//	 * @param key_nodeType
//	 * @param isSumDataCell
//	 * @param  calculateSheetPks---20141117���ӻ�����֮��ɼ������Χ
//	 * @return
//	 * @throws BusinessException
//	 * @author: pengzhena@yonyou.com
//	 */
//	public TaskDataModel sumPlanByparadimAndCalculate(String pk_taskTo, DimLevel sumParamDimLevel,String[] fromOrgUnicode,
//			UserLoginVO userVo, String[] pk_sheets, int varAreaSumType, HashMap userInfo,String key_nodeType , boolean isSumDataCell,
//			String [] calculateSheetPks) throws BusinessException;
	/**
	 * {��ά�Ȼ��ܲ�ִ�й�ʽ����}
	 * @param sumparams
	 * @return
	 * @author pengzhena 20150130
	 * @throws BusinessException
	 */
	public TaskDataModel sumPlanByparadimAndCalculate(TbSumParamVO sumparams) throws BusinessException;
	
	
	/**
	 * ���ܺ�ִ�м��㹫ʽ  add  by ��־ǿ at:2020/07/14
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
	 * lrx 2015-8-28 �𼶻��ܷŵ���̨����
	 * @param sumparam	- ���ܲ���,û��pk_taskTo��fromOrgUnicode,��Ҫ�𼶻���ʱ����
	 * @param isSumEffectiveData	- �����ϱ�����
	 * @param map	- SumSingleSheetDlg.doLevelSum()�����й����map
	 * @param table	- SumSingleSheetDlg.doLevelSum()�����й����table
	 * @return	- updStatusTaskPks:SumSingleSheetDlg.doLevelSum()�����е�updStatusTaskPks; taskDataModel:���ܽ��TaskDataModel
	 * @throws BusinessException
	 * @author: lrx@yonyou.com
	 */
	public HashMap<String, Object> doLevelSum(TbSumParamVO sumparam, boolean isSumEffectiveData, 
			Map<String,String> map, Hashtable <String, Hashtable<String, List<String>>> table, 
			MdTask mdtask, DimLevel dimlevel) throws BusinessException;
}
