package nc.itf.hbbb.convert.avgrate;

import java.util.Map;

import nc.pub.iufo.exception.UFOSrvException;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.avgrateinfo.AvgRateVO;

public interface IAvgRateQueryService {
	
	public AvgRateVO computeAvgRate(AvgRateVO avgvo) throws BusinessException;
	
	public Map<String,UFDouble> getFinalRateByAccMonth(String pk_currinfo, AccperiodmonthVO[] months) throws BusinessException ;
	
	public UFDouble getFinalRateByAccMonth(String pk_currinfo, AccperiodmonthVO month) throws BusinessException ;

	public UFDouble getPeriodRateByAccMonth(String pk_currinfo, AccperiodmonthVO month) throws BusinessException;

	/**
	 * 查询给定组织在某一汇率方案下某期间的平均及交易日汇率
	 * 
	 * @create by zhoushuang at 2014-4-24,上午11:03:11
	 * 
	 * @param pk_exratescheme
	 * @param pk_srccurrtype
	 * @param pk_descurrtype
	 * @param month
	 * @param pk_org
	 * @return
	 * @throws UFOSrvException
	 */
    public AvgRateVO getAvgRates(String pk_exratescheme,String pk_srccurrtype, String pk_descurrtype, AccperiodmonthVO month,String pk_org) throws UFOSrvException;

	public AvgRateVO computeAvgRate_gl(AvgRateVO computeRateVO);

}
