
package nc.itf.iufo.total;
import nc.pub.iufo.exception.UFOSrvException;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;

/**
 * 汇总查询服务接口
 * @author xulm
 * @created at 2010-4-19,下午02:58:00
 *
 */
public interface IHBTotalQueryService {
	
	/**
	 * 
	 * TODO 根据指定的pk获取汇总方案
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 上午10:40:01
	 */
	public abstract HbTotalSchemeVO getTotalSchemeByPK(String totalSchemeId) throws UFOSrvException;
	
	/**
	 * 
	 * TODO(根据组织体系获取指定的组织汇总规则).
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 上午10:40:38
	 */
	public abstract HbTotalSchemeVO getTotalScheme(String rmsId,String orgId,String strRmsVersionPK) throws UFOSrvException;
	
	/**
	 * 
	 * TODO(根据组织时间关键字获取汇总).
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 上午10:51:26
	 */
	public abstract HbTotalSchemeVO getHBTotalSchemeInfo(String rmsId,String strRmsVersionPK,String innercode,String orgId, MeasurePubDataVO pubData, String date) throws UFOSrvException;
	
	/*
	

	*//**
	 * 根据指定的pk获取汇总方案
	 * @create by xulm at 2010-5-11,下午12:06:17
	 *
	 * @param totalSchemeId
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract HBTotalSchemeVO getTotalSchemeByPK(String totalSchemeId) throws UFOSrvException;
	
	

	*//**
	 * 获取指定报表组织体系中指定组织的汇总方案
	 * @create by xulm at 2010-6-17,下午07:20:59
	 *
	 * @param rmsId
	 * @param orgId
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract HBTotalSchemeVO getTotalScheme(String rmsId,String orgId,String strRmsVersionPK) throws UFOSrvException;
	
	*//**
	 * 获取指定报表组织体系中指定组织的汇总方案,如果没有，就取父级方案
	 * @create by xulm at 2010-5-5,下午02:07:06
	 *
	 * @param rmsId
	 * @param orgId
	 * @param innercode
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract HBTotalSchemeVO getAppTotalScheme(String rmsId,String orgId,String innercode,String strRmsVersionPK) throws UFOSrvException;
	
	*//**
	 * 获取指定报表组织体系版本中指定组织的汇总方案,如果没有，就取父级方案
	 * @create by congdy at 2015-4-23,下午02:07:06
	 *
	 * @param rmsId
	 * @param orgId
	 * @param innercode
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract HBTotalSchemeVO getAppTotalSchemeByDate(String rmsId, String orgId,String innercode,
			String date) throws UFOSrvException;
	
	*//**
	 * 一次查询汇总所需信息，减少连接数
	 * 
	 * @param rmsId
	 * @param orgId
	 * @param taskPk
	 * @param pubData
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract Object[] getTotalSchemeInfo(String rmsId,String orgId, String taskPk, MeasurePubDataVO pubData, String busiDate) throws UFOSrvException;
	
	*//**
	 * 获取指定报表组织体系下指定的组织（本级及其下级非最末级节点)符合指定条件的汇总方案
	 * @create by xulm at 2010-5-5,下午03:44:24
	 *
	 * @param rmsId
	 * @param orgId
	 * @param clause
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract HBTotalSchemeVO[] getTotalSchemes(String rmsId,String orgId,String pk_rmsversion,String clause) throws UFOSrvException; 
	
	*//**
	 * 获取指定报表组织体系下指定的组织（本级及其下级非最末级节点)符合指定条件的汇总方案
	 * @create by xulm at 2010-5-5,下午03:44:24
	 *
	 * @param rmsId
	 * @param orgId
	 * @param clause
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract TotalSchemeVO[] getTotalSchemesByBusiTime(String rmsId,String orgId,String busiTime,String clause) throws UFOSrvException; 

	 
	 *//**
	  * 获取指定报表组织体系指定组织（本级及其下级非最末级节点)的汇总方案
	  * @create by xulm at 2010-6-3,上午11:29:12
	  *
	  * @param rmsId
	  * @param orgId
	  * @return
	  * @throws UFOSrvException
	  *//*
	 public abstract TotalSchemeVO[] getTotalSchemes(String rmsId,String orgId,String pk_rmsversion) throws UFOSrvException;
	 
	 
	 *//**
	  * 获取指定报表组织体系，指定多组织的汇总方案
	  * @create by xulm at 2010-6-25,上午10:26:28
	  *
	  * @param rmsId
	  * @param orgIds
	  * @return
	  * @throws UFOSrvException
	  *//*
	 public abstract TotalSchemeVO[] getTotalSchemes(String rmsId,String[] orgIds,String pk_rmsversion) throws UFOSrvException;
	 
	 
	 *//**
	  * 获取汇总来源
	  * @create by xulm at 2010-7-15,上午09:29:00
	  *
	  * @param totalVals
	  * @param strAloneID
	  * @param strRepID
	  * @param destKeys
	  * @param hashKeyPos
	  * @param strUserID
	  * @param strTaskID
	  * @param strOrgPK
	  * @return
	  * @throws UFOSrvException
	  *//*
	 @SuppressWarnings("unchecked")
	public abstract TotalCellKeyValue[][] getTotalSoure(TotalCellKeyValue[] totalVals,String strAloneID,String strRepID,KeyVO[] destKeys,Hashtable hashKeyPos,MeasurePubDataVO pubData,String busiTime,String strTaskID,String strRmsPK, String strOrgPK,TotalSchemeVO totalScheme) throws UFOSrvException;
	 
	 *//**
	  * 预览汇总组织范围
	  * @create by jiaah at 2011-3-24,下午02:18:24
	  * @param totalScheme
	  * @return
	  * @throws Exception
	  *//*
	 public abstract TotalSchemeVO[] getReviewTotalScheme(TotalSchemeVO[] totalSchemes,String date) throws Exception;


	 *//**
	  * 根据日期加载TotalSchemeVO
	  * 
	  * @creator tianjlc at 2015-3-24 上午8:46:33
	  * @param rmsId
	  * @param orgId
	  * @param clause
	  * @param date
	  * @return
	  * @throws UFOSrvException
	  * @return TotalSchemeVO[]
	  *//*
	public TotalSchemeVO[] getTotalSchemesByVersion(String rmsId, String orgId, String clause, String date) throws UFOSrvException;
	
	public Object[] getTotalSchemesByVersionAndRmsVersion(String rmsId, String orgId, String clause, String date) throws UFOSrvException;
	
	public Object[] getTotalSchemesByBusiTimeAndRmsVersion(String rmsId,String orgId,String busiTime,String clause) throws UFOSrvException; 
	
	public boolean isTotalLeafOrg(String pk_rmsversion, String pk_task, String pk_org) throws UFOSrvException;
	
	public Object[] getCommitAndApproveState(MeasurePubDataVO pubData, String pk_task, String[] repPKs) throws UFOSrvException;
	
	public Object[] getTotalSchemeWithOrgAttribute(String totalSchemeId, String curr_pk_org, String rmsId, String busiTime) throws UFOSrvException;
	
	public ReportOrgInnerVO[] createVersionTreeByOrgAttribute(UserdefitemVO[] userdefitems,String strTaskId,String mainOrgPK,String rmsPK,String date,boolean includeBalanUnit) throws UFOSrvException;

*/}
