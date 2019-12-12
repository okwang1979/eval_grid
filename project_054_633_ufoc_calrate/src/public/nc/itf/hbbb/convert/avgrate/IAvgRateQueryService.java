package nc.itf.hbbb.convert.avgrate;

import java.util.Map;

import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.avgrateinfo.AvgRateVO;

public interface IAvgRateQueryService {
	public AvgRateVO computeAvgRate(AvgRateVO avgvo) throws BusinessException;
	
	public Map<String,UFDouble> getFinalRateByAccMonth(String pk_currinfo, AccperiodmonthVO[] months) throws BusinessException ;
	
	public UFDouble getFinalRateByAccMonth(String pk_currinfo, AccperiodmonthVO month) throws BusinessException ;

	public AvgRateVO computeAvgRate_gl(AvgRateVO computeRateVO);
}
