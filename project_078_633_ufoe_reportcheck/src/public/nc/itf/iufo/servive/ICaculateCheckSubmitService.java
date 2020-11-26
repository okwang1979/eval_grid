package nc.itf.iufo.servive;

import com.ufsoft.iufo.check.vo.CheckResultVO;

import nc.vo.pub.BusinessException;
import nc.vo.pub.param.TempParamVO;

/**
 * 结账后，计算，审核，上报
 * @author xulink
 *
 */
public interface ICaculateCheckSubmitService {
	
	//开始执行
	public TempParamVO getParams(String pk_accountingbook,String year,String month) throws BusinessException;
	//计算
	public String caculate(TempParamVO param) throws BusinessException;
	//审核
	public CheckResultVO[]  check(TempParamVO param,String jobId) throws BusinessException;
	//上报
	public void submit(TempParamVO param,String jobId) throws BusinessException;
}
