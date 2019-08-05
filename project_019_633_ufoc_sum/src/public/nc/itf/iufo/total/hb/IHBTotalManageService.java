package nc.itf.iufo.total.hb;

import nc.pub.iufo.exception.UFOSrvException;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;

import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;

/**
 * �ϲ����ܹ������ӿ�
 * 
 * @author xulink
 *
 */
public interface IHBTotalManageService {
	/**
	 * 
	 * TODO ���屨�����
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-19 ����2:55:53
	 */
	public RepDataOperResultVO createTotalResults(MeasurePubDataVO mainPubData,String busiTime,
			HbTotalSchemeVO totalScheme, String[] reportIds,
			boolean[] extendParams,HBSchemeVO hbschemeVO,String oper_user,String mainOrgPK) throws UFOSrvException ;
	
	/**
	 * 
	 * TODO ��ȡҪ���ܵ���֯
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-19 ����2:57:05
	 */
	public String getTotalOrgCondSQL(HbTotalSchemeVO totalScheme, String rcsVerPk, String pk_hbscheme)throws UFOSrvException;

}
