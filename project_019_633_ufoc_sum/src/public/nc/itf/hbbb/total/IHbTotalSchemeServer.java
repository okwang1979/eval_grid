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
	 * TODO ����ָ����pk��ȡ���ܷ���
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 ����10:40:01
	 */
	public  HbTotalSchemeVO getTotalSchemeByPK(String totalSchemeId) throws UFOSrvException;
	
	/**
	 * 
	 * TODO(������֯��ϵ��ȡָ������֯���ܹ���).
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 ����10:40:38
	 */
	public  HbTotalSchemeVO getTotalScheme(String rmsId,String orgId,String strRmsVersionPK) throws UFOSrvException;
	
	/**
	 * 
	 * TODO(������֯ʱ��ؼ��ֻ�ȡ����).
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2019-7-22 ����10:51:26
	 */
	public  HbTotalSchemeVO getHBTotalSchemeInfo(String rmsId,String strRmsVersionPK,String innercode,String orgId, MeasurePubDataVO pubData, String date) throws UFOSrvException;

	
	
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
