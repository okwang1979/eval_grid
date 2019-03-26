package nc.itf.hbbb.linkend;

import java.util.Map;

import nc.vo.pub.BusinessRuntimeException;

/** 
 * �������ʹ�õ�Զ�̲�ѯ�ӿ�
 * <b>Application name:</b>�Ϳ���Ŀ<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 ��������ɷ����޹�˾��Ȩ���С�<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-3-21<br>
 * @author����־ǿ
 * @version �Ϳ�
 */ 
public interface ILinkEndQuery {
 
	/**
	 * ��ѯָ������������¼�
	 * 
	 * @param pk_svid���汾
	 * @param parent_innercode���ϼ�innercode
	 * @param level����ѯ���Ρ���С��1������Ϊnull��null��ѯ�����¼�
	 * @return
	 * @throws BusinessRuntimeException
	 * @author: ��־ǿ
	 */
	public Map<String,String> getUnionOrgsOrderByCode(String pk_svid,String parent_innercode,Integer level) throws BusinessRuntimeException ;
	
	
	/**
	 * ��ѯָ��InnerCodes������map,���IdxΪNull�����롣
	 * 
	 * @param innerCodes
	 * @return
	 * @throws BusinessRuntimeException
	 * @author: ��־ǿ
	 */
	public Map<String,Integer> getOrgIndex(String pk_svid,String[] innerCodes)throws BusinessRuntimeException ;

}
