package nc.impl.hbbb.convert.avgrate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bd.accperiod.InvalidAccperiodExcetion;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.hbbb.convert.avgrate.IAvgRateQueryService;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.SQLParameter;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pubitf.accperiod.AccountCalendar;
import nc.vo.bd.currinfo.AdjustrateVO;
import nc.vo.bd.currinfo.CurrinfoVO;
import nc.vo.bd.currinfo.CurrrateVO;
import nc.vo.bd.currinfo.ICurrinfoConst;
import nc.vo.bd.period.AccHalfYearVO;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.bd.period3.AccperiodquartVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.avgrateinfo.AvgRateVO;

import com.ufida.iufo.pub.tools.AppDebug;

public class AvgRateQueryServiceImpl implements IAvgRateQueryService {

	//枚举规则计算方法
	//月平均
	public static final int month = 0;
	//季度平均
	public static final int season = 1;
	//半年平均
	public static final int halfyear = 2;
	//年内平均
	public static final int year = 3;

	@Override
	public AvgRateVO computeAvgRate(AvgRateVO avgvo) throws BusinessException {

		Map<String, AdjustrateVO> monthMap = new HashMap<String, AdjustrateVO>();
		@SuppressWarnings("unchecked")
		Collection<AdjustrateVO> adjustrateVOs = NCLocator.getInstance().lookup(IUAPQueryBS.class).retrieveByClause(AdjustrateVO.class,"pk_currinfo = '" + avgvo.getPk_currinfo() + "' and pk_accperiod = '" + avgvo.getPk_accperiod() +"'");
		if(adjustrateVOs != null) {
			for (AdjustrateVO adjustrateVO : adjustrateVOs) {
				monthMap.put(adjustrateVO.getRatemonth(), adjustrateVO);
			}
		}

		int indexMonth = Integer.parseInt(avgvo.getRatemonth());
		AdjustrateVO currAdjustrateVO = monthMap.get(avgvo.getRatemonth());
		if(currAdjustrateVO != null) {
			//重置期间汇率
			avgvo.setPeriodrate(currAdjustrateVO.getAdjustrate());
		}

		nc.vo.bd.currinfo.AvgRateVO[] rateInfos = getRateInfo(avgvo.getPk_currinfo());

		if(rateInfos == null) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0473")/*@res "未查询到平均汇率"*/);
		}

		UFDouble monthAvgRate = new UFDouble(0);
		UFDouble seasonRate = new UFDouble(0);
		UFDouble halfYearRate = new UFDouble(0);
		UFDouble yearRate = new UFDouble(0);

		AccountCalendar calendar = null;;
		try {
			calendar = AccountCalendar.getInstanceByAccperiodMonth(avgvo.getPk_accperiodmonth());
			String strYear = calendar.getYearVO().getPeriodyear();
			String strMonth = calendar.getMonthVO().getAccperiodmth();
			calendar.set(strYear,strMonth);
		} catch (InvalidAccperiodExcetion e) {
			throw new BusinessException(e);
		}

		for (nc.vo.bd.currinfo.AvgRateVO avgRateVO : rateInfos) {
			// @edit by zhoushuang at 2014-11-6,下午2:26:00 适配uap65 平均年半年季度月汇率的计算规则统一到一个vo中，不再放到四个vo中
//			if(ICurrinfoConst.AVGRATERULE_MONTH.equals(avgRateVO.getAvgrate_month())) {
				monthAvgRate = getAvgByType(calendar, avgvo, indexMonth, monthMap, avgRateVO.getAvgrate_month(), (avgRateVO.getOffsetmonth() == null ? 0:avgRateVO.getOffsetmonth()));
//			}
//			if(ICurrinfoConst.AVGRATERULE_QUARTER.equals(avgRateVO.getAvgrate_quarter())) {
				seasonRate = getAvgByType(calendar, avgvo, indexMonth, monthMap, avgRateVO.getAvgrate_quarter(), (avgRateVO.getOffsetmonth() == null ? 0:avgRateVO.getOffsetmonth()));
//			}
//			if(ICurrinfoConst.AVGRATERULE_HALFYEAR.equals(avgRateVO.getAvgrate_halfyear())) {
				halfYearRate = getAvgByType(calendar, avgvo, indexMonth, monthMap, avgRateVO.getAvgrate_halfyear(), (avgRateVO.getOffsetmonth() == null ? 0:avgRateVO.getOffsetmonth()));
//			}
//			if(ICurrinfoConst.AVGRATERULE_YEAR.equals(avgRateVO.getAvgrate_year())) {
				yearRate = getAvgByType(calendar, avgvo, indexMonth, monthMap, avgRateVO.getAvgrate_year(), (avgRateVO.getOffsetmonth() == null ? 0:avgRateVO.getOffsetmonth()));
//			}
		}

		avgvo.setMonthrate(monthAvgRate);
		avgvo.setQuarterrate(seasonRate);
		avgvo.setHalfyearrate(halfYearRate);
		avgvo.setYearrate(yearRate);

		return avgvo;
	}

	private UFDouble getAvgByType(AccountCalendar calendar, AvgRateVO avgvo, int indexMonth, Map<String, AdjustrateVO> monthMap, int type, int offsetMonth) throws BusinessException {
		switch(type) {
			case 0 : {
				//月平均
				return getAvgRate(indexMonth - offsetMonth, 1 + offsetMonth, monthMap);
			}
			case 1 : {
				//季度
				AccperiodquartVO quarterVO = calendar.getQuarterVO();
//				int startMonthOfSeason = ((int)(indexMonth - 1)/3)*3 + 1;
				int startMothOfSeason = Integer.parseInt(quarterVO.getBeginmonth());
				return getAvgRate(startMothOfSeason, indexMonth - startMothOfSeason + 1, monthMap);
			}
			case 2 : {
				//半年
//				int startMonthOfHalfYear = ((int)(indexMonth - 1)/6)*6 + 1;
				AccHalfYearVO halfYearByMonth = getHalfYearByMonth(avgvo);
				int startMonthOfHalfYear =  Integer.parseInt(halfYearByMonth.getBeginmonth());
				return getAvgRate(startMonthOfHalfYear, indexMonth - startMonthOfHalfYear + 1, monthMap);
			}
			case 3 : {
				//年内
				int startMonthOfYear = Integer.parseInt(calendar.getFirstMonthOfCurrentYear().getAccperiodmth());
				return getAvgRate(startMonthOfYear, indexMonth - startMonthOfYear + 1, monthMap);
			}
			default : return new UFDouble(0);
		}
	}

	@SuppressWarnings("unchecked")
	private AccHalfYearVO getHalfYearByMonth(AvgRateVO avgRateVO) throws BusinessException {
		List<AccHalfYearVO> halfYearVOs = null;
		String condition = AccHalfYearVO.PK_ACCPERIODSCHEME + "='" + avgRateVO.getPk_accperiodscheme() + "' and " + AccHalfYearVO.PK_ACCPERIOD + "='" + avgRateVO.getPk_accperiod() + "'";
		try {
			halfYearVOs = (List<AccHalfYearVO>) new BaseDAO().retrieveByClause(AccHalfYearVO.class, condition);
		} catch (DAOException e) {
			throw new BusinessException(e);
		}
		if(halfYearVOs == null) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0478")/*@res "查询不到会计半年的信息!"*/);
		}

		AccHalfYearVO yearVO = null;
		for (AccHalfYearVO accHalfYearVO : halfYearVOs) {
			if(accHalfYearVO.getBeginmonth().compareTo(avgRateVO.getRatemonth()) <= 0 && accHalfYearVO.getEndmonth().compareTo(avgRateVO.getRatemonth()) >= 0) {
				yearVO = accHalfYearVO;
				break;
			}
		}
		return yearVO;
	}

	private UFDouble getAvgRate(int startMonth, int countOfMonth, Map<String, AdjustrateVO> monthMap) {
		int countMonthOfData = 0;
		UFDouble total = new UFDouble(0);
		for (int i = startMonth; i < startMonth + countOfMonth; i++) {
			AdjustrateVO adjustrateVO = monthMap.get(getStrOfMonth(i));
			if(adjustrateVO != null && adjustrateVO.getAdjustrate() != null) {
				total = total.add(adjustrateVO.getAdjustrate());
				countMonthOfData++;
			}
		}
		return  countMonthOfData == 0? new UFDouble(0) : total.div(countMonthOfData);
	}

	private String getStrOfMonth(int month) {
		return month <= 9 ? ("0" + (month)) : (month + "");
	}

	@SuppressWarnings("unchecked")
	private nc.vo.bd.currinfo.AvgRateVO[] getRateInfo(String currentPK) throws BusinessException {
		String where = " pk_currinfo='"+currentPK+"'";
		Collection<AvgRateVO> col;
		try {
			col = new BaseDAO().retrieveByClause(nc.vo.bd.currinfo.AvgRateVO.class, where);
		} catch (DAOException e) {
			throw new BusinessException(e.getMessage());
		}
		return col==null ? null : col.toArray(new nc.vo.bd.currinfo.AvgRateVO[0]);
	}
	
	@Override
	public Map<String,UFDouble> getFinalRateByAccMonth(String pk_currinfo, AccperiodmonthVO[] months) throws BusinessException {
      Map<String,UFDouble> monthFinalrateMap = new HashMap<String,UFDouble>();
      for(AccperiodmonthVO month : months ) {
    	  String lastDayInMonth = month.getEnddate().toStdString();
    	  String beginDayInMonth = month.getBegindate().toStdString();
	      String condition = "pk_currinfo = '" + pk_currinfo + "' and ratedate <='" + lastDayInMonth + "' and ratedate>='" + beginDayInMonth + "'";
	      BaseDAO dao = new BaseDAO();
	      Collection<CurrrateVO> col = dao.retrieveByClause(CurrrateVO.class, condition, "ratedate", new SQLParameter());
	      CurrrateVO[] aryCurrrate = col.toArray(new CurrrateVO[col.size()]);
	      for(int i=0; i<aryCurrrate.length; i++) {
	    	  CurrrateVO currratevo = aryCurrrate[i];
	    	  if(currratevo == null)
		       		continue;
		      if(currratevo.getRate() == null)
		       		continue;
		      monthFinalrateMap.put(month.getPk_accperiodmonth(), currratevo.getRate().equals(UFDouble.ZERO_DBL)? new UFDouble(0) : currratevo.getRate());
	      }
	  }
      return monthFinalrateMap;
	}
	
	@Override
	public UFDouble getFinalRateByAccMonth(String pk_currinfo, AccperiodmonthVO month) throws BusinessException {
	    String lastDayInMonth = month.getEnddate().toStdString();
		String condition = "pk_currinfo = '" + pk_currinfo + "' and ratedate <='" + lastDayInMonth + "'";
		BaseDAO dao = new BaseDAO();
		Collection<CurrrateVO> col = dao.retrieveByClause(CurrrateVO.class, condition, "ratedate", new SQLParameter());
		CurrrateVO[] aryCurrrate = col.toArray(new CurrrateVO[col.size()]);
		for(int i=aryCurrrate.length-1; i>=0; i--) {
			CurrrateVO currratevo = aryCurrrate[i];
		    if(currratevo == null)
			       	continue;
		    else if(currratevo.getRate() == null)
			       	continue;
		    else  {
		    	return currratevo.getRate().equals(UFDouble.ZERO_DBL)? new UFDouble(1) : currratevo.getRate();
		    }
		}
		return new UFDouble(1);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public UFDouble getPeriodRateByAccMonth(String pk_currinfo, AccperiodmonthVO month) throws BusinessException {
		String monthPk = month.getPk_accperiodmonth();
		String condition = "pk_currinfo = '" + pk_currinfo + "' and PK_ACCPERIODMONTH = '" + monthPk + "'";
		BaseDAO dao = new BaseDAO();
		ArrayList<AdjustrateVO> col = (ArrayList<AdjustrateVO>)dao.retrieveByClause(AdjustrateVO.class, condition);
		if (col!=null&&col.size()>0) {
			AdjustrateVO adjustrateVO = col.get(0);
			return adjustrateVO.getAdjustrate();
		}else {
			return new UFDouble(1);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public AvgRateVO getAvgRates(String pk_exratescheme,String pk_srccurrtype,String pk_descurrtype, AccperiodmonthVO month,String pk_org) throws UFOSrvException {
		String monthPk = month.getPk_accperiodmonth();
		BaseDAO baseDao = new BaseDAO();
		SQLParameter param=new SQLParameter();
		String pk_currinfo = getCurrInfoVO(pk_exratescheme,pk_srccurrtype,pk_descurrtype).getPk_currinfo();
		param.addParam(pk_currinfo);
		param.addParam(monthPk);
		param.addParam(pk_org);
		try {
			List<AvgRateVO> avgRateVOs = (List<AvgRateVO>)baseDao.retrieveByClause(AvgRateVO.class, "PK_CURRINFO=? and PK_ACCPERIODMONTH=? and PK_ORG=?",param);
			if (avgRateVOs!=null&&avgRateVOs.size()>0) {
				return avgRateVOs.get(0);
			}else {
				return null;
			}
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(),e);
		}
	}

	@SuppressWarnings("unchecked")
	private CurrinfoVO getCurrInfoVO(String pk_exratescheme,String pk_srccurrtype,String pk_descurrtype) {
		try {
			Collection<CurrinfoVO> retrieveByClause = NCLocator
					.getInstance()
					.lookup(IUAPQueryBS.class)
					.retrieveByClause(
							CurrinfoVO.class,
							CurrinfoVO.PK_EXRATESCHEME + "='"
									+ pk_exratescheme
									+ "' and " + CurrinfoVO.PK_CURRTYPE + "='"
									+ pk_srccurrtype
									+ "'  and " + CurrinfoVO.OPPCURRTYPE + "='"
									+ pk_descurrtype + "'");
			if (retrieveByClause.size() > 0) {
				return retrieveByClause.toArray(new CurrinfoVO[0])[0];
			}
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public AvgRateVO computeAvgRate_gl(AvgRateVO computeRateVO) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
