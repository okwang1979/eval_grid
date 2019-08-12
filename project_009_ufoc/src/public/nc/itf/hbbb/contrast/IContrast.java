package nc.itf.hbbb.contrast;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.pub.iufo.exception.UFOSrvException;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.pub.BusinessException;


/**
 * �ϲ�������˽ӿ�
 * @date 2011-01-19
 * @author liyra
 */
public interface IContrast {

	public void doContrast(ContrastQryVO qryvo) throws BusinessException;
	
	/**
	 * ����ʹ��
	 * @create by zhoushuang at 2015-7-27,����4:55:00
	 *
	 * @param qryvo
	 * @throws BusinessException
	 */
	public int doContrastBySubContrastorgs(ContrastQryVO qryvo) throws BusinessException;
	
	/**
	 * ����ǰУ��(��ʱδʹ��)
	 * @create by jiaah at 2012-4-17,����11:15:29
	 * @param qryvo
	 * @throws BusinessException
	 */
	public void checkCommitOrCheckStatus(String pk_hbschme,String pk_org,Map<String, String> keyMap) throws BusinessException;
	
	/**
	 * �ж��Ƿ����õ��ȵ���
	 * @create by zhoushuang at 2015-7-4,����10:50:52
	 *
	 * @param pk_hbrepstru
	 * @param pk_contrastorg
	 * @return
	 * @throws BusinessException 
	 */
	public boolean isStartSchedule(String pk_hbrepstru, String pk_contrastorg) throws BusinessException;
	
	/**
	 * ȷ�����˵Ĺ�˾��
	 * @create by jiaah at 2011-12-30,����10:32:32
	 * @param pk_hbrepstru ����ϲ���ϵ�汾����
	 * @param innercode ��ǰ������֯innercode
	 * @return
	 * @throws BusinessException
	 */
	public String[] getContrastOrgs(String pk_hbrepstru, String pk_contrastorg) throws BusinessException;
	/**
	 * ��ģ��ɾ�����˼�¼
	 * 
	 * @edit by zhaojian8 at 2016-11-24 13:44:17 
	 * @param vo
	 * @param selfOrgs
	 * @param oppOrgs
	 * @param qryvo
	 * @return
	 * @throws BusinessException
	 */
	public void clearContrastedData(ContrastQryVO qryvo) throws BusinessException;
	/**
	 * ���˶��߳�ʱ�ȴ���ɽ�������
	 * @param jobId
	 * @throws UFOSrvException
	 */
	public int waitForJobComplete(String jobId) throws UFOSrvException;
	
	/**
	 * ��ȡ���˶�
	 * @author sunzeg
	 * @param sql
	 * @param isDICCORP
	 * @param orgSupplier
	 * @return
	 * @throws BusinessException
	 */
	public Set<String> getcontrastOrg(String sql, boolean isDICCORP, Map<String, String> orgSupplier) throws BusinessException;
	
	/**
	 * �������˶���ʱ����δʹ�ã�����Ľ�
	 * @author sunzeg
	 * @param selfOppOrgs
	 * @return
	 * @throws SQLException
	 */
	public String createTempTablebyContrastOrgs(List<String[]> selfOppOrgs) throws BusinessException;
	
	/**
	 * ͨ����ʱ���ȡ������֯�Ĺؼ���ֵ����δʹ�ã�����Ľ�
	 * @author sunzeg
	 * @param pk_keygroup
	 * @param selfOppOrgs
	 * @return
	 * @throws BusinessException
	 */
	public MeasurePubDataVO[] findPubDataByContrastOrgs(String pk_keygroup, List<String[]> selfOppOrgs) throws BusinessException;	
}
