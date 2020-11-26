package nc.itf.iufo.servive;

import com.ufsoft.iufo.check.vo.CheckResultVO;

import nc.vo.pub.BusinessException;
import nc.vo.pub.param.TempParamVO;

/**
 * ���˺󣬼��㣬��ˣ��ϱ�
 * @author xulink
 *
 */
public interface ICaculateCheckSubmitService {
	
	//��ʼִ��
	public TempParamVO getParams(String pk_accountingbook,String year,String month) throws BusinessException;
	//����
	public String caculate(TempParamVO param) throws BusinessException;
	//���
	public CheckResultVO[]  check(TempParamVO param,String jobId) throws BusinessException;
	//�ϱ�
	public void submit(TempParamVO param,String jobId) throws BusinessException;
}
