package nc.itf.iufo.servive;

import java.util.List;

import nc.vo.pub.BusinessException;
import nc.vo.pub.param.CheckResultVO;
import nc.vo.pub.param.TempParamVO;

public interface IControlAccountService {
	/**
	 * �����ҵ����챨һ��������ָ���Ƿ�¼��
	 * @return
	 */
	List<CheckResultVO>  checkKB(TempParamVO params) throws BusinessException;
	
	/**
	 * �������ѹ�ر�
	 */
	
	List<CheckResultVO>  checkPressureControl (TempParamVO params) throws BusinessException;
	
	
	/**
	 * 
	 * �����03��
	 * @return
	 * @throws BusinessException
	 */
	List<CheckResultVO>  checkN03B (TempParamVO params) throws BusinessException;
	
	

}
