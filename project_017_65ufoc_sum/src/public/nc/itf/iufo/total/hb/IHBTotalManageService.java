package nc.itf.iufo.total.hb;

import nc.pub.iufo.exception.UFOSrvException;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;

import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;

/**
 * 合并汇总规则管理接口
 * 
 * @author xulink
 *
 */
public interface IHBTotalManageService {
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
