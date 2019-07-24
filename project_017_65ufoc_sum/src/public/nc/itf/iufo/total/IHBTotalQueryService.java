
package nc.itf.iufo.total;
import nc.pub.iufo.exception.UFOSrvException;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;

/**
 * ���ܲ�ѯ����ӿ�
 * @author xulm
 * @created at 2010-4-19,����02:58:00
 *
 */
public interface IHBTotalQueryService {
	
	/**
	 * 
	 * TODO ����ָ����pk��ȡ���ܷ���
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 ����10:40:01
	 */
	public abstract HbTotalSchemeVO getTotalSchemeByPK(String totalSchemeId) throws UFOSrvException;
	
	/**
	 * 
	 * TODO(������֯��ϵ��ȡָ������֯���ܹ���).
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 ����10:40:38
	 */
	public abstract HbTotalSchemeVO getTotalScheme(String rmsId,String orgId,String strRmsVersionPK) throws UFOSrvException;
	
	/**
	 * 
	 * TODO(������֯ʱ��ؼ��ֻ�ȡ����).
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 ����10:51:26
	 */
	public abstract HbTotalSchemeVO getHBTotalSchemeInfo(String rmsId,String strRmsVersionPK,String innercode,String orgId, MeasurePubDataVO pubData, String date) throws UFOSrvException;
	
	/*
	

	*//**
	 * ����ָ����pk��ȡ���ܷ���
	 * @create by xulm at 2010-5-11,����12:06:17
	 *
	 * @param totalSchemeId
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract HBTotalSchemeVO getTotalSchemeByPK(String totalSchemeId) throws UFOSrvException;
	
	

	*//**
	 * ��ȡָ��������֯��ϵ��ָ����֯�Ļ��ܷ���
	 * @create by xulm at 2010-6-17,����07:20:59
	 *
	 * @param rmsId
	 * @param orgId
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract HBTotalSchemeVO getTotalScheme(String rmsId,String orgId,String strRmsVersionPK) throws UFOSrvException;
	
	*//**
	 * ��ȡָ��������֯��ϵ��ָ����֯�Ļ��ܷ���,���û�У���ȡ��������
	 * @create by xulm at 2010-5-5,����02:07:06
	 *
	 * @param rmsId
	 * @param orgId
	 * @param innercode
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract HBTotalSchemeVO getAppTotalScheme(String rmsId,String orgId,String innercode,String strRmsVersionPK) throws UFOSrvException;
	
	*//**
	 * ��ȡָ��������֯��ϵ�汾��ָ����֯�Ļ��ܷ���,���û�У���ȡ��������
	 * @create by congdy at 2015-4-23,����02:07:06
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
	 * һ�β�ѯ����������Ϣ������������
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
	 * ��ȡָ��������֯��ϵ��ָ������֯�����������¼�����ĩ���ڵ�)����ָ�������Ļ��ܷ���
	 * @create by xulm at 2010-5-5,����03:44:24
	 *
	 * @param rmsId
	 * @param orgId
	 * @param clause
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract HBTotalSchemeVO[] getTotalSchemes(String rmsId,String orgId,String pk_rmsversion,String clause) throws UFOSrvException; 
	
	*//**
	 * ��ȡָ��������֯��ϵ��ָ������֯�����������¼�����ĩ���ڵ�)����ָ�������Ļ��ܷ���
	 * @create by xulm at 2010-5-5,����03:44:24
	 *
	 * @param rmsId
	 * @param orgId
	 * @param clause
	 * @return
	 * @throws UFOSrvException
	 *//*
	public abstract TotalSchemeVO[] getTotalSchemesByBusiTime(String rmsId,String orgId,String busiTime,String clause) throws UFOSrvException; 

	 
	 *//**
	  * ��ȡָ��������֯��ϵָ����֯�����������¼�����ĩ���ڵ�)�Ļ��ܷ���
	  * @create by xulm at 2010-6-3,����11:29:12
	  *
	  * @param rmsId
	  * @param orgId
	  * @return
	  * @throws UFOSrvException
	  *//*
	 public abstract TotalSchemeVO[] getTotalSchemes(String rmsId,String orgId,String pk_rmsversion) throws UFOSrvException;
	 
	 
	 *//**
	  * ��ȡָ��������֯��ϵ��ָ������֯�Ļ��ܷ���
	  * @create by xulm at 2010-6-25,����10:26:28
	  *
	  * @param rmsId
	  * @param orgIds
	  * @return
	  * @throws UFOSrvException
	  *//*
	 public abstract TotalSchemeVO[] getTotalSchemes(String rmsId,String[] orgIds,String pk_rmsversion) throws UFOSrvException;
	 
	 
	 *//**
	  * ��ȡ������Դ
	  * @create by xulm at 2010-7-15,����09:29:00
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
	  * Ԥ��������֯��Χ
	  * @create by jiaah at 2011-3-24,����02:18:24
	  * @param totalScheme
	  * @return
	  * @throws Exception
	  *//*
	 public abstract TotalSchemeVO[] getReviewTotalScheme(TotalSchemeVO[] totalSchemes,String date) throws Exception;


	 *//**
	  * �������ڼ���TotalSchemeVO
	  * 
	  * @creator tianjlc at 2015-3-24 ����8:46:33
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
