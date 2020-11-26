package nc.itf.iufo.servive;

import java.util.List;

import nc.vo.pub.BusinessException;
import nc.vo.pub.param.CheckResultVO;
import nc.vo.pub.param.TempParamVO;

public interface IControlAccountService {
	/**
	 * 检查企业财务快报一表中人数指标是否录入
	 * @return
	 */
	List<CheckResultVO>  checkKB(TempParamVO params) throws BusinessException;
	
	/**
	 * 检查两金压控表
	 */
	
	List<CheckResultVO>  checkPressureControl (TempParamVO params) throws BusinessException;
	
	
	/**
	 * 
	 * 检查内03表
	 * @return
	 * @throws BusinessException
	 */
	List<CheckResultVO>  checkN03B (TempParamVO params) throws BusinessException;
	
	

}
