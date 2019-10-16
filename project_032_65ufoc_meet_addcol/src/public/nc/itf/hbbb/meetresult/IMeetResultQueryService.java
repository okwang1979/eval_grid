package nc.itf.hbbb.meetresult;

import java.util.Map;

import nc.vo.hbbb.meetaccount.AggMeetRltHeadVO;
import nc.vo.hbbb.meetaccount.MeetResultBodyVO;
import nc.vo.pub.BusinessException;

public interface IMeetResultQueryService {
	/**
	 * �������ڲ����ױ�ϲ�������bodyVO;
	 * ֻ��Ҫ�����ֶ�
	 * ����ڴ����
	 * MeetResultBodyVO.PK_OPPORG,MeetResultBodyVO.ADJUST_AMOUNT,MeetResultBodyVO.MEASURECODE
	 * @param sqlWhere
	 * @return
	 * @throws BusinessException
	 */
	public MeetResultBodyVO[] queryMeetBodyVoByCondition(String sqlWhere)throws BusinessException;
	/**
	 * ����������ѯ���˽��
	 * @param sqlWhere
	 * @return
	 */
	public AggMeetRltHeadVO[] queryAggMeetResultByCondition(String sqlWhere) throws BusinessException;
	
	/**
	 * ����pk��ѯ���˽��
	 * @param pks
	 * @return
	 * @throws BusinessException
	 */
	public AggMeetRltHeadVO[] queryAggMeetResultByPKs(String[] pks) throws BusinessException;
	
	/**
	 * ��ѯ����������PK����
	 * @param sqlWhere
	 * @return
	 * @throws BusinessException
	 */
	public String[] queryMeetResultPKsByCondition(String sqlWhere) throws BusinessException;
	
	/**
	 *ˢ�¶��˽��������Ŀ�ͱ�����ʾΪname
	 * @param objects
	 * @return
	 * @throws BusinessException
	 * add by hubina
	 */
	public Object[] refreshMeetResult(Object[] objects) throws BusinessException;
	

	/**
	 * ���ݺϲ�������Ŀpk��ȡ�ϲ�������Ŀpk��name
	 * @param pks
	 * @return
	 * @throws BusinessException
	 * add by hubina
	 */
	public Map<String, String> queryProjectCodeAndNameByPks(String[] pks)throws BusinessException;
	
	/**
	 * ������֯pk��ȡ��֯pk��name
	 * @param pks
	 * @return
	 * @throws BusinessException
	 * add by hubina
	 */
	public Map<String, String> queryOrgCodeAndName(String[] pks)throws BusinessException;
	
	/**
	 * �����û�pk��ȡ�û�code��name
	 * @param pks
	 * @return
	 * @throws BusinessException
	 * add by hubina
	 */
	public Map<String, String> queryUserCodeAndName(String[] pks)throws BusinessException;
}
