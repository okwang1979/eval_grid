package nc.itf.hbbb.total;

import java.util.List;

import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;

import nc.pub.iufo.exception.UFOSrvException;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;

public interface IHbTotalSchemeServer {
	
	String saveOrUpdateScheme(HbTotalSchemeVO vo);
	
	boolean deleleSchemeByPk(String pk_scheme);
	
	List<HbTotalSchemeVO> queryBy(String pk_rms,String pk_rmsversion);
	
	
	/**
	 * 
	 * TODO 根据指定的pk获取汇总方案
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 上午10:40:01
	 */
	public  HbTotalSchemeVO getTotalSchemeByPK(String totalSchemeId) throws UFOSrvException;
	
	/**
	 * 
	 * TODO(根据组织体系获取指定的组织汇总规则).
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 上午10:40:38
	 */
	public  HbTotalSchemeVO getTotalScheme(String rmsId,String orgId,String strRmsVersionPK) throws UFOSrvException;
	
	/**
	 * 
	 * TODO(根据组织时间关键字获取汇总).
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 上午10:51:26
	 */
	public  HbTotalSchemeVO getHBTotalSchemeInfo(String rmsId,String strRmsVersionPK,String innercode,String orgId, MeasurePubDataVO pubData, String date) throws UFOSrvException;

	
	
	/**
	 * 
	 * TODO 单体报表汇总
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-19 下午2:55:53
	 */
	public RepDataOperResultVO createTotalResults(MeasurePubDataVO mainPubData,String busiTime,
			HbTotalSchemeVO totalScheme, String[] reportIds,
			boolean[] extendParams,HBSchemeVO hbschemeVO,String oper_user,String mainOrgPK) throws UFOSrvException ;
	
	/**
	 * 
	 * TODO 获取要汇总的组织
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-19 下午2:57:05
	 */
	public String getTotalOrgCondSQL(HbTotalSchemeVO totalScheme, String rcsVerPk, String pk_hbscheme)throws UFOSrvException;


}
