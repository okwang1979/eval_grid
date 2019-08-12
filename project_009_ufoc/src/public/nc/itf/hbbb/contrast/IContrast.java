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
 * 合并报表对账接口
 * @date 2011-01-19
 * @author liyra
 */
public interface IContrast {

	public void doContrast(ContrastQryVO qryvo) throws BusinessException;
	
	/**
	 * 调度使用
	 * @create by zhoushuang at 2015-7-27,下午4:55:00
	 *
	 * @param qryvo
	 * @throws BusinessException
	 */
	public int doContrastBySubContrastorgs(ContrastQryVO qryvo) throws BusinessException;
	
	/**
	 * 对账前校验(暂时未使用)
	 * @create by jiaah at 2012-4-17,上午11:15:29
	 * @param qryvo
	 * @throws BusinessException
	 */
	public void checkCommitOrCheckStatus(String pk_hbschme,String pk_org,Map<String, String> keyMap) throws BusinessException;
	
	/**
	 * 判断是否启用调度调度
	 * @create by zhoushuang at 2015-7-4,上午10:50:52
	 *
	 * @param pk_hbrepstru
	 * @param pk_contrastorg
	 * @return
	 * @throws BusinessException 
	 */
	public boolean isStartSchedule(String pk_hbrepstru, String pk_contrastorg) throws BusinessException;
	
	/**
	 * 确定对账的公司对
	 * @create by jiaah at 2011-12-30,上午10:32:32
	 * @param pk_hbrepstru 报表合并体系版本主键
	 * @param innercode 当前对账组织innercode
	 * @return
	 * @throws BusinessException
	 */
	public String[] getContrastOrgs(String pk_hbrepstru, String pk_contrastorg) throws BusinessException;
	/**
	 * 按模板删除对账记录
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
	 * 对账多线程时等待完成解锁调用
	 * @param jobId
	 * @throws UFOSrvException
	 */
	public int waitForJobComplete(String jobId) throws UFOSrvException;
	
	/**
	 * 获取对账对
	 * @author sunzeg
	 * @param sql
	 * @param isDICCORP
	 * @param orgSupplier
	 * @return
	 * @throws BusinessException
	 */
	public Set<String> getcontrastOrg(String sql, boolean isDICCORP, Map<String, String> orgSupplier) throws BusinessException;
	
	/**
	 * 创建对账对临时表，尚未使用，还需改进
	 * @author sunzeg
	 * @param selfOppOrgs
	 * @return
	 * @throws SQLException
	 */
	public String createTempTablebyContrastOrgs(List<String[]> selfOppOrgs) throws BusinessException;
	
	/**
	 * 通过临时表获取对账组织的关键字值，尚未使用，还需改进
	 * @author sunzeg
	 * @param pk_keygroup
	 * @param selfOppOrgs
	 * @return
	 * @throws BusinessException
	 */
	public MeasurePubDataVO[] findPubDataByContrastOrgs(String pk_keygroup, List<String[]> selfOppOrgs) throws BusinessException;	
}
