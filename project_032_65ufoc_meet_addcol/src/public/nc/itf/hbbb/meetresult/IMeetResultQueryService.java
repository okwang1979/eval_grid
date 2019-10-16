package nc.itf.hbbb.meetresult;

import java.util.Map;

import nc.vo.hbbb.meetaccount.AggMeetRltHeadVO;
import nc.vo.hbbb.meetaccount.MeetResultBodyVO;
import nc.vo.pub.BusinessException;

public interface IMeetResultQueryService {
	/**
	 * 仅用于内部交易表合并，返回bodyVO;
	 * 只需要三个字段
	 * 解决内存溢出
	 * MeetResultBodyVO.PK_OPPORG,MeetResultBodyVO.ADJUST_AMOUNT,MeetResultBodyVO.MEASURECODE
	 * @param sqlWhere
	 * @return
	 * @throws BusinessException
	 */
	public MeetResultBodyVO[] queryMeetBodyVoByCondition(String sqlWhere)throws BusinessException;
	/**
	 * 根据条件查询对账结果
	 * @param sqlWhere
	 * @return
	 */
	public AggMeetRltHeadVO[] queryAggMeetResultByCondition(String sqlWhere) throws BusinessException;
	
	/**
	 * 根据pk查询对账结果
	 * @param pks
	 * @return
	 * @throws BusinessException
	 */
	public AggMeetRltHeadVO[] queryAggMeetResultByPKs(String[] pks) throws BusinessException;
	
	/**
	 * 查询满足条件的PK数组
	 * @param sqlWhere
	 * @return
	 * @throws BusinessException
	 */
	public String[] queryMeetResultPKsByCondition(String sqlWhere) throws BusinessException;
	
	/**
	 *刷新对账结果，将项目和本方显示为name
	 * @param objects
	 * @return
	 * @throws BusinessException
	 * add by hubina
	 */
	public Object[] refreshMeetResult(Object[] objects) throws BusinessException;
	

	/**
	 * 根据合并报表项目pk获取合并报表项目pk和name
	 * @param pks
	 * @return
	 * @throws BusinessException
	 * add by hubina
	 */
	public Map<String, String> queryProjectCodeAndNameByPks(String[] pks)throws BusinessException;
	
	/**
	 * 根据组织pk获取组织pk和name
	 * @param pks
	 * @return
	 * @throws BusinessException
	 * add by hubina
	 */
	public Map<String, String> queryOrgCodeAndName(String[] pks)throws BusinessException;
	
	/**
	 * 根据用户pk获取用户code和name
	 * @param pks
	 * @return
	 * @throws BusinessException
	 * add by hubina
	 */
	public Map<String, String> queryUserCodeAndName(String[] pks)throws BusinessException;
}
