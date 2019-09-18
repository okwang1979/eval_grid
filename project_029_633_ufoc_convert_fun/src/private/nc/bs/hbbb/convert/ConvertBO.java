package nc.bs.hbbb.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nc.bd.accperiod.AccperiodParamAccessor;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.impl.iufo.utils.MultiLangTextUtil;
import nc.itf.bd.currinfo.ICurrinfoQueryService;
import nc.itf.hbbb.convertbalance.IConvertBalanceQueryService;
import nc.itf.hbbb.func.FuncReTurnObj;
import nc.itf.hbbb.rateset.IRateSetQueryService;
import nc.itf.iufo.commit.ICommitManageService;
import nc.itf.iufo.commit.ICommitQueryService;
import nc.itf.iufo.report.IUfoeRepDataSrv;
import nc.itf.uap.IUAPQueryBS;
import nc.pub.hbbb.exception.UFOCUnThrowableException;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.KeywordCache;
import nc.pub.iufo.cache.MeasureCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pubitf.accperiod.AccountCalendar;
import nc.ui.iufo.data.MeasureDataBO_Client;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.data.RepDataBO_Client;
import nc.util.convert.rate.Rate;
import nc.util.convert.rate.RateFactory;
import nc.util.convert.rate.RateQryVO;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.HBBBRepUtil;
import nc.util.hbbb.HBBBReportUtil;
import nc.util.hbbb.HBVersionUtil;
import nc.util.hbbb.MeasurePubDataUtil;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.input.HBBBTableInputActionHandler;
import nc.util.hbbb.measure.MeasureUtil;
import nc.util.hbbb.pub.HBPubItfService;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.util.iufo.pub.AuditUtil;
import nc.util.iufo.report.autocalc.ZsAutoCalcUtil;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.bd.currinfo.CurrinfoVO;
import nc.vo.bd.exratescheme.ExrateSchemeVO;
import nc.vo.hbbb.adjustscheme.AdjustSchemeVO;
import nc.vo.hbbb.func.HBBBFuncQryVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.pub.date.TTimeCode;
import nc.vo.iufo.pub.date.UFODate;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.conver.ConvertRuleVO;
import nc.vo.ufoc.convert.ConvertBalanceVO;
import nc.vo.ufoc.convert.CvtruleorgVO;
import nc.vo.ufoc.ratesetinfo.RateSetVO;

import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.script.exception.CmdException;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;

public class ConvertBO {
	
	private ICommitManageService commitSrv = null;
	
	private ICommitQueryService commitQrySrv = null;
	
	private Map<String, String> keymap = null;
	
	private Map<String, Double> ratePkValueMap = new HashMap<String, Double>();
	
	private Map<String, MeasurePubDataVO> measurePubDataCache = new HashMap<String, MeasurePubDataVO>();
	
	private Map<String, RateSetVO[]> rateSetCache = new HashMap<String, RateSetVO[]>();
	
	private Map<String, String> projectMeasMap = new ConcurrentHashMap<String, String>();
	private Map<String, String> measureDayratemethodMap = new ConcurrentHashMap<String, String>(); 
	
	private Map<String, Integer> ruleCvtmodeMap = new HashMap<String, Integer>();
	
	private HBAloneIDUtil hbAloneIdUtil;
	
	private Map<String, Boolean> repIsIntrateMap = new HashMap<String, Boolean>();
	
	private Map<String, AdjustSchemeVO> adjSchemeCache = new HashMap<String, AdjustSchemeVO>();
	
	private Map<String, String> accLastPeriodCache = new HashMap<String, String>();
	
	private Map<String, String> natLastPeriodCache = new HashMap<String, String>();
	
	private Map<String, Boolean> needLastPrdDataCache = new HashMap<String, Boolean>();
	
	private Map<String, ExrateSchemeVO> exrateschemePkVOCache = new HashMap<String, ExrateSchemeVO>();
	
	private MeasurePubDataVO lastPrdDesPubdata = null;
	private MeasurePubDataVO lastPrdSrcPubdata  = null;
	
	private String timePk = null;
	private String lastprdValue = null;
	
	public ConvertBO() {
		super();
	}
	public ConvertBO(	Map<String, String> newkeymap) {
		
		super();
		this.keymap=newkeymap;
	}

	public void doConvert(Map<String, String> newkeymap,
			ConvertRuleVO[] convertrulevos) throws BusinessException {
		if (null == newkeymap || newkeymap.size() == 0) {
			return;
		}
		this.keymap = newkeymap;
		
		if (null == convertrulevos || convertrulevos.length == 0) {
			return;
		}
		for (ConvertRuleVO rulevo : convertrulevos) {
			CvtruleorgVO[] orgs = rulevo.getOrgs();
			if (null == orgs || orgs.length == 0) {
				continue;
			}
			for (CvtruleorgVO orgvo : orgs) {
//				如果组织的原币和折算的原币相同泽进行折算
//				if (newkeymap.get(KeyVO.COIN_PK)==null||newkeymap.get(KeyVO.COIN_PK).equals(rulevo.getPk_currtype())) {
					doConvertBySingleCorpWithRule(orgvo, rulevo);
					newkeymap.put(KeyVO.COIN_PK, rulevo.getPk_currtype());
//				}
			}
		}
	}

	private void doConvertBySingleCorpWithRule(CvtruleorgVO orgvo,
			ConvertRuleVO rulevo) throws BusinessException {
		HBSchemeVO schemevo = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(rulevo
				.getPk_hbscheme());
		String[] pk_reports = ((nc.itf.hbbb.hbscheme.IHBSchemeQrySrv) NCLocator
				.getInstance().lookup(
						nc.itf.hbbb.hbscheme.IHBSchemeQrySrv.class.getName()))
				.getReportIdByHBSchemeId(schemevo.getPk_hbscheme());
		if (null == pk_reports || pk_reports.length == 0) {
			return;
		}
		for (String pk_report : pk_reports) {

//			if(this.isIntrateRep(pk_report)&& rulevo.getDatatype().intValue() !=0
//					&& this.isIntrateRep(pk_report)&& rulevo.getDatatype().intValue() !=1){
//				//如果是内部交易采集表, 且原表数据不是个别报表也不是个别报表调整表,则不折算
//				continue;
//			}
			doConvertByCorpWithRuleAndRep(orgvo, rulevo, pk_report, schemevo);
		}

	}

	// 补充上汇率转换操作
	private MeasureDataVO[] appendRateInfo(MeasureDataVO[] datavos, 
			MeasureDataVO[] lastPeriodSrcDataVOs, MeasureDataVO[] lastPeriodDesDataVOs,
			CvtruleorgVO orgvo, ConvertRuleVO rulevo, String pk_report,
			HBSchemeVO schemevo, boolean iscrossyear, boolean isDynRep) throws BusinessException {
		
		RateSetVO[] vos = this.getRateSetVOsByRepPK(pk_report);
		if (null == vos || vos.length == 0) {
			return datavos;
		}
		
		// 准备rateQryvo;
		RateQryVO qryvo = getRateQryVO(orgvo, rulevo, schemevo);
		
		int convertmode = getConvertMode(rulevo);
		
		Map<String, String> srcLastPrdMeasValueMap = new ConcurrentHashMap<String, String>(); 
		Map<String, String> desLastPrdMeasValueMap = new ConcurrentHashMap<String, String>(); 
		
		// @edit by zhoushuang at 2014-3-6,下午4:04:41 若为动态表，需得到 aloneid映射关系 	
		//<本期原币alongid,上期原币alongid>
		Map<String, String> thisSrcToLastSrcMap = null; 
		// <本期原币alongid,上期目的币alongid>
		Map<String, String> thisSrcToLastDesMap = null; 
		if (isDynRep) {
			thisSrcToLastSrcMap = HBAloneIDUtil.getThisSrcToLastSrcMap(datavos, timePk, lastprdValue); 
			thisSrcToLastDesMap = HBAloneIDUtil.getThisSrcToLastDesMap(datavos,  timePk, lastprdValue, rulevo.getDescurrtype()); 
		}
		
		if(lastPeriodSrcDataVOs != null) {
			for(int i=0; i<lastPeriodSrcDataVOs.length; i++) {
				// @edit by zhoushuang at 2014-3-5,下午3:37:30 区分动态表<code+alongid,value>和固定表<code,value>
				if (isDynRep) {
					srcLastPrdMeasValueMap.put(lastPeriodSrcDataVOs[i].getCode()+lastPeriodSrcDataVOs[i].getAloneID(), lastPeriodSrcDataVOs[i].getDataValue());//jiaah 考虑动态区code是一致的，不能只拿code作为key
				}else {
					srcLastPrdMeasValueMap.put(lastPeriodSrcDataVOs[i].getCode(), lastPeriodSrcDataVOs[i].getDataValue());
				}
			}
		}
		if(lastPeriodDesDataVOs != null) {
			for(int i=0; i<lastPeriodDesDataVOs.length; i++) {
				// @edit by zhoushuang at 2014-3-5,下午3:37:30 区分动态表<code+alongid,value>和固定表<code,value>
				if (isDynRep) {
					desLastPrdMeasValueMap.put(lastPeriodDesDataVOs[i].getCode()+lastPeriodDesDataVOs[i].getAloneID(), lastPeriodDesDataVOs[i].getDataValue());
				}else {
					desLastPrdMeasValueMap.put(lastPeriodDesDataVOs[i].getCode(), lastPeriodDesDataVOs[i].getDataValue());
				}
			}
		}

		RateFactory factory = new RateFactory();
		// 开始取汇率算值
		for (MeasureDataVO vo : datavos) {
			if (projectMeasMap.containsKey(vo.getMeasureVO().getCode())) {
				String pk_dayrateproject = projectMeasMap.get(vo.getMeasureVO().getCode());
				double ratevalue = 1;
				String ratePk = getRatePK(pk_dayrateproject, qryvo);
				if(ratePkValueMap.get(ratePk) == null) {
					Rate ratevo = factory.CreateProduct(pk_dayrateproject, qryvo);
					ratevalue = ratevo.getRateValue();
					ratePkValueMap.put(ratePk, ratevalue);
				}
				else {
					ratevalue = ratePkValueMap.get(ratePk).doubleValue();
				}

				UFDouble value = new UFDouble(vo.getDataValue());
				UFDouble desvalue = UFDouble.ZERO_DBL;
				if (!RateFactory.getPeriodset().contains(pk_dayrateproject)) {

					// 交易日汇率项目，使用单独的折算算法
					String dayRateMethod = measureDayratemethodMap.get(vo.getMeasureVO().getCode());
					if (dayRateMethod == null) {
						desvalue = convertmode == 0 ? value.multiply(ratevalue) : value.div(ratevalue);
					} else {
						if (RateSetVO.DAY_CNVT_MTHD_AMOUNT_VALUE.equals(dayRateMethod) && iscrossyear) {
							// 发生额方式并且上一个期间跨年，使用公式 ：本期折算后结果=本期原币种值*本期汇率
							desvalue = convertmode == 0 ? value.multiply(ratevalue) : value.div(ratevalue);
						} else {// “余额方式”或者 “发生额方式且上一个期间不跨年”，使用公式：本期折算后结果=（本期原币种值—上一期原币种值）*本期汇率+上一期折算后结果
							UFDouble srcLastPrdValue = null;
							UFDouble desLastPrdValue = null;
							if (isDynRep) {
								// @edit by zhoushuang at 2014-3-5,下午3:41:39  动态表<code+alongid,value>
								srcLastPrdValue = new UFDouble(srcLastPrdMeasValueMap.get(vo.getCode() + thisSrcToLastSrcMap.get(vo.getAloneID())));
								desLastPrdValue = new UFDouble(desLastPrdMeasValueMap.get(vo.getCode() + thisSrcToLastDesMap.get(vo.getAloneID())));
							} else {
								// @edit by zhoushuang at 2014-3-5,下午3:41:39  固定表<code,value>
								srcLastPrdValue = new UFDouble(srcLastPrdMeasValueMap.get(vo.getCode()));
								desLastPrdValue = new UFDouble(desLastPrdMeasValueMap.get(vo.getCode()));
							}
							if (convertmode == 0) {
								desvalue = (value.sub(srcLastPrdValue)).multiply(ratevalue).add(desLastPrdValue);
							} else {
								desvalue = (value.sub(srcLastPrdValue)).div(ratevalue).add(desLastPrdValue);
							}
						}
					}
				}
				else {
					desvalue = convertmode==0 ? value.multiply(ratevalue): value.div(ratevalue);
				}
				vo.setDataValue(String.valueOf(desvalue));
			} else {// 没有设置汇率信息
					// 不做任何处理
				
			}
		}

		return datavos;

	}

	private int getConvertMode(ConvertRuleVO rulevo) throws BusinessException {
		if(ruleCvtmodeMap.get(rulevo.getPk_convertrule()) != null)
			return ruleCvtmodeMap.get(rulevo.getPk_convertrule());
		else {
			int convertmode = 0 ;
			String pk_exratescheme = rulevo.getPk_exratescheme();
			ICurrinfoQueryService srv = (ICurrinfoQueryService) NCLocator.getInstance().lookup(ICurrinfoQueryService.class.getName());
			CurrinfoVO[] currInfos = srv.queryCurrinfoVOsByScheme(pk_exratescheme);
			for(CurrinfoVO infoVO : currInfos){
				String pk_currtype = infoVO.getPk_currtype();
				String pk_opptype = infoVO.getOppcurrtype();
				if(pk_currtype.equals(rulevo.getPk_currtype())&& pk_opptype.equals(rulevo.getDescurrtype())){
					convertmode = infoVO.getConvmode();
					break;
				}
			}
			ruleCvtmodeMap.put(rulevo.getPk_convertrule(), Integer.valueOf(convertmode));
			return convertmode;
		}
	}
	private RateQryVO getRateQryVO(CvtruleorgVO orgvo, ConvertRuleVO rulevo, HBSchemeVO schemevo) throws BusinessException {
		String pk_keygroup = schemevo.getPk_keygroup();
		KeyGroupVO keyGroupVo = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
		KeyVO tmieKeyVO = null;
		KeyVO[] keys = keyGroupVo.getKeys();
		for (int i = 0; i < keys.length; i++) {
			KeyVO keyVO = keys[i];
			if(keyVO.isTTimeKeyVO()){
				tmieKeyVO = keyVO;
				break;
			}
		}
		RateQryVO qryvo = new RateQryVO();
		String period = this.getKeymap().get(tmieKeyVO.getPk_keyword());
		if (null == period || period.trim().length() == 0) {
			period = this.getKeymap().get(KeyVO.MONTH_PK);
		}
		if(tmieKeyVO.getPk_keyword().equals(KeyVO.ACC_MONTH_PK)){
			qryvo.setMonth(period.substring(5, 7));
		}else if(tmieKeyVO.getPk_keyword().equals(KeyVO.ACC_SEASON_PK)){
			AccountCalendar calendar = AccountCalendar.getInstance();
			String s_year = period.substring(0, 4);
			String s_quarter = period.substring(5, 7);
			int quarter = Integer.parseInt(s_quarter);
			calendar.set(s_year, quarter);
			qryvo.setMonth(calendar.getQuarterVO().getEndmonth());
		}else if(tmieKeyVO.getPk_keyword().equals(KeyVO.ACC_HALFYEAR_PK)){
			AccountCalendar calendar = AccountCalendar.getInstance();
			String s_year = period.substring(0, 4);
			String s_halfyear = period.substring(5, 7);
			int halfyear = Integer.parseInt(s_halfyear);
			calendar.setYearAndHalfyear(s_year, halfyear);
			qryvo.setMonth(calendar.getHalfYearVO().getEndmonth());
		}else if(tmieKeyVO.getPk_keyword().equals(KeyVO.ACC_YEAR_PK)){
			AccountCalendar calendar = AccountCalendar.getInstance();
			String s_year = period.substring(0, 4);
			calendar.set(s_year);
			String month = calendar.getLastMonthOfCurrentYear().getAccperiodmth();
			qryvo.setMonth(month);
		}
		else {
			AccountCalendar instanceByPeriodScheme = AccountCalendar.getInstanceByPeriodScheme(schemevo.getPk_accperiodscheme());
			instanceByPeriodScheme.setDate(new UFDate(period));
			String accperiodmth = instanceByPeriodScheme.getMonthVO().getAccperiodmth();
			qryvo.setMonth(accperiodmth);
		}
		
		qryvo.setYear(period.substring(0, 4));
		
		qryvo.setPk_accperiodscheme(schemevo.getPk_accperiodscheme() == null ? AccperiodParamAccessor.getInstance()
				.getDefaultSchemePk(): schemevo.getPk_accperiodscheme());
		qryvo.setPk_exratescheme(rulevo.getPk_exratescheme());
		qryvo.setPk_srccurrtype(rulevo.getPk_currtype());
		qryvo.setPk_descurrtype(rulevo.getDescurrtype());
		
		String pk_exratescheme = rulevo.getPk_exratescheme();
		ExrateSchemeVO exrateScheme = getExtrateSchemeByPkFromCache(pk_exratescheme);
		if(exrateScheme != null)
			qryvo.setPk_createorg(exrateScheme.getPk_org());
		else 
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0134") /* @res "未取到汇率方案！" */);
//		String rulepk_org = rulevo.getPk_org();
//		String groupId = InvocationInfoProxy.getInstance().getGroupId();
//		if(rulepk_org.equals(IOrgConst.GLOBEORG)){
//			//折算规则是全局折算规则,组织设置为全局
//			qryvo.setPk_createorg(IOrgConst.GLOBEORG);
//		}else if(rulepk_org.equals(groupId)){
//			//折算规则为集团创建,组织设置为集团
//			qryvo.setPk_createorg(groupId);
//		}else{
//			qryvo.setPk_createorg(orgvo.getPk_org());
//		}
		return qryvo;
	}
	
	private RateSetVO[] getRateSetVOsByRepPK(String pk_report) throws BusinessException {
		if(rateSetCache.get(pk_report) != null) {
			return rateSetCache.get(pk_report);
		}
		else {
			StringBuilder content = new StringBuilder();
			content.append(RateSetVO.PK_REPORT).append("='").append(pk_report)
					.append("' ");
			// 提取汇率设置信息
			RateSetVO[] vos = this.getMapQrySrv().queryRateSetVOByCon(
					content.toString());
			if(vos == null)
				vos = new RateSetVO[0];
			else if(vos.length>0){
				// 初始化汇率设置MAP
				for (RateSetVO vo : vos) {
					if(vo != null && vo.getPk_measure() != null)
						projectMeasMap.put(vo.getPk_measure(), vo.getPk_dayrateproject());
					if(vo != null && vo.getPk_measure() != null && vo.getDay_convert_method() != null)
						measureDayratemethodMap.put(vo.getPk_measure(), vo.getDay_convert_method());
				}
			}
			rateSetCache.put(pk_report, vos);
			return vos;
		}
	}
	
	private String getRatePK(String pk_dayrateproject, RateQryVO qryvo) {
		return pk_dayrateproject + "@" 
				+ qryvo.getPk_srccurrtype() + "@"
				+ qryvo.getPk_descurrtype() + "@" 
				+ qryvo.getMonth() + "@"
				+ qryvo.getYear() + "@"
				+ qryvo.getPk_accperiodscheme() + "@"
				+ qryvo.getPk_exratescheme() + "@"
				+ qryvo.getPk_createorg();
	}
	
	public IRateSetQueryService getMapQrySrv() {
		return NCLocator.getInstance().lookup(IRateSetQueryService.class);
	}


	public void doConvertByCorpWithRuleAndRep(CvtruleorgVO orgvo,ConvertRuleVO rulevo, String pk_report, HBSchemeVO schemevo)throws BusinessException {
		
		try {
			String pk_org = orgvo.getPk_org();
			
			//目的币种
			getKeymap().put(KeyVO.COIN_PK, rulevo.getDescurrtype());
			MeasurePubDataVO pubdata = MeasurePubDataUtil.getMeasurePubdata(
					this.getVersion(schemevo, rulevo.getDatatype(),
							pk_report), true, pk_org, this.getKeymap(), schemevo);
			
			//校验目的币种的报表数据是否允许覆盖（已经上报和来源于分布式的数据不能被修改）
			if(HBBBTableInputActionHandler.isRepCommit(pubdata.getAloneID(), pk_report)){
        		throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0073")
                /* @res "目的币种报表已经上报，不能修改！" */);
            }
			IUfoeRepDataSrv repDataSrv = nc.bs.framework.common.NCLocator.getInstance().lookup(IUfoeRepDataSrv.class);
            String dataOrigin = repDataSrv.checkRepCommitDataOrigin(pk_report, pubdata.getAloneID());
            if (dataOrigin != null) {
                throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0",
                        "01830008-0074", null, new String[]{dataOrigin})/* @res "目的币种报表数据来自于分布式系统[{0}]，不能修改或删除！"*/);
            }
			
			//提取折算原表的数据
			getKeymap().put(KeyVO.COIN_PK, rulevo.getPk_currtype());
			MeasurePubDataVO srcPubdata = MeasurePubDataUtil.getMeasurePubdata(
					this.getVersion(schemevo, rulevo.getDatatype(),
							pk_report), true, pk_org, this.getKeymap(), schemevo);
			MeasureDataVO[] datavos = null;
			if(getCommitQrySrv().isRepInput(srcPubdata.getAloneID(), pk_report)) {
				datavos =HBBBReportUtil.getAllMeasData(pk_report, srcPubdata);
			}
			else {
				//删除折算后的表数据,有可能以往有数据,然后原表数据删除了
				MeasureVO[] measureVOs = IUFOCacheManager.getSingleton().getMeasureCache().loadMeasureByReportPKs(new String[] { pk_report });
				getKeymap().put(KeyVO.COIN_PK, rulevo.getDescurrtype());
				MeasurePubDataVO desPubdata = MeasurePubDataUtil.getMeasurePubdata(this.getVersion(schemevo, rulevo.getDatatype(),pk_report), true, pk_org, this.getKeymap(), schemevo);
				MeasureDataBO_Client.deleteRepData(desPubdata.getAloneID(), measureVOs);
				getCommitSrv().addRepInputSate(schemevo.getPk_hbscheme(), desPubdata.getAloneID(), pk_report, null, false, null);
				return ;
			}
			
			//取得上一个期间的原表数据和折算后表数据
			MeasureDataVO[] lastPrdSrcDataVOs = null;
			MeasureDataVO[] lastPrdDesDataVOs = null;
			boolean hasTimeKey = false;//是否含有时间关键字
			boolean iscrossyear = false;//上一个期间是否跨年
			
			Map<String, String> lastPrdKeyMap = new HashMap<String, String>();
			Iterator<String> iter = getKeymap().keySet().iterator();
			while(iter.hasNext()) {
				String key = iter.next();
				String value = getKeymap().get(key);
				if(KeyVO.isTTimeKey(key)) {
					hasTimeKey = true;
					//时间关键字取上一个期间
					KeywordCache keyCache = UFOCacheManager.getSingleton().getKeywordCache();
					KeyVO keyVO = keyCache.getByPK(key);
					String newvalue = "";
					if(keyVO.isAccPeriodKey()) {
						newvalue = getAccLastPrdFromCache(keyVO.getAccPeriodProperty(), value, schemevo.getPk_accperiodscheme(), keyVO.getDateType());
//						TTimeCode inputTTimeCode = TTimeCode.getInstanceOfAcc(keyVO.getAccPeriodProperty(), value, schemevo.getPk_accperiodscheme());
//						newvalue = inputTTimeCode.getNextDate(keyVO.getDateType(), -1);
					}
					else {
						newvalue = getNatLastPrdFromCache(value, keyVO.getTimeProperty());
//						UFODate ufodate = new UFODate(value);
//						newvalue = ufodate.getNextDate(keyVO.getTimeProperty(), -1);
					}
					if(newvalue != null && newvalue.length()>3 
							&& !newvalue.trim().substring(0,4).equals(value.trim().substring(0, 4))) {
						iscrossyear = true;
					}
					value = newvalue;
					timePk= key;
					lastprdValue = value;
				}
				lastPrdKeyMap.put(key, value);
			}
			lastPrdKeyMap.put(KeyVO.COIN_PK, rulevo.getPk_currtype());
			boolean needLastPeriodData = needLastDataByCache(datavos, pk_report, iscrossyear);
			if(hasTimeKey && needLastPeriodData) {
				lastPrdSrcPubdata = MeasurePubDataUtil.getMeasurePubdata(
						this.getVersion(schemevo, rulevo.getDatatype(),
								pk_report), true, pk_org, lastPrdKeyMap, schemevo);
				
				lastPrdSrcDataVOs = HBBBReportUtil.getAllMeasData(pk_report, lastPrdSrcPubdata);
				
				if(null != lastPrdSrcDataVOs && lastPrdSrcDataVOs.length > 0){
					//上期原表录入过数据
					lastPrdKeyMap.put(KeyVO.COIN_PK, rulevo.getDescurrtype());
					lastPrdDesPubdata = MeasurePubDataUtil.getMeasurePubdata(this.getVersion(schemevo, rulevo.getDatatype(),pk_report), true, pk_org, lastPrdKeyMap, schemevo);
					lastPrdDesDataVOs = HBBBReportUtil.getAllMeasData(pk_report, lastPrdDesPubdata);
				}
			}
			boolean isDynRep= false;
			// @edit by zhoushuang at 2014-3-5,下午3:31:39
			RepDataVO[] repDatas = RepDataBO_Client.loadRepData(pk_report, null, srcPubdata, null);
			if(repDatas != null && repDatas.length>0) {
				for(RepDataVO repData : repDatas) {
					if (repData.getPubDatas().length>1) {
						isDynRep = true;
						break;
					}
				}
			}
			
			// 开始进行汇率换算
			datavos = this.appendRateInfo(datavos,lastPrdSrcDataVOs, lastPrdDesDataVOs, 
					orgvo, rulevo, pk_report, schemevo, iscrossyear, isDynRep);

			// 开始进行保存折算表
			// 首先替换aloneid
			datavos = getHBAloneIDUtil().resetAloneId(pk_report, datavos, pubdata, rulevo);
			
			// 进行折算表数据保存
			if (this.isIntrateRep(pk_report)||isDynRep) {
				this.saveRepData(pubdata, datavos, pk_report);
			} else {
				MeasureDataBO_Client.editRepData(pubdata.getAloneID(), datavos);
			}
			
			// 然后进行折算公式运算
			MeasureCache measureCache = UFOCacheManager.getSingleton()
					.getMeasureCache();
			MeasureVO[] loadMeasureByReportPK = measureCache
					.loadMeasureByReportPK(pk_report);
			HBBBFuncQryVO qryvo = new HBBBFuncQryVO();
			qryvo.setAryRepIDs(new String[] { pk_report });
			qryvo.setbAddLeft(Boolean.FALSE);
			qryvo.setHbSchemeVo(schemevo);
			qryvo.setIsconvert(true);
			qryvo.setMeasures(loadMeasureByReportPK);
			qryvo.setPubdata(pubdata);
			qryvo.setStrUserID("");
			qryvo.setNeedreplaceAdd(false);

			// 运行折算公式
			AdjustSchemeVO adjustScheme = getAdjustScheme(qryvo.getHbSchemeVo().getPk_adjustscheme());
			FuncReTurnObj[] returnobjs = HBBBRepUtil.calcZSFormulas(qryvo, adjustScheme);

			// 开始计算折算差额
			this.computeConvertBalance(pk_report, returnobjs);
			
			String pk_task = schemevo.getPk_hbscheme();
			if(rulevo.getDatatype().intValue()== 0) {//个别报表需要使用报表所属的任务
				TaskVO[] tasks = TaskSrvUtils.getTaskByReportId(pk_report);
				if(tasks != null && tasks.length>0)
					pk_task = tasks[0].getPk_task();
			}
			getCommitSrv().addRepInputSate(pk_task, pubdata.getAloneID(), pk_report,
	                    AuditUtil.getCurrentUser(), true, AuditUtil.getCurrentTime());
			
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			ReportVO report = UFOCacheManager.getSingleton().getReportCache().getByPK(pk_report);
			String pk_org = orgvo.getPk_org();
			OrgVO org = (OrgVO) NCLocator.getInstance().lookup(IUAPQueryBS.class).retrieveByPK(OrgVO.class, pk_org);
			String[] params = new String[] {
					"(" + rulevo.getCode() +")" + MultiLangTextUtil.getCurLangText(rulevo)
					, report.getNameWithCode()
					, "(" + org.getCode() +")"+MultiLangTextUtil.getCurLangText(org)
					, e.getMessage()
					};
			String msg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0072", null, params)/* @res "执行折算规则“{0}”出错。当前折算表样：{1}；折算单位：{2}。错误原因：{3}" */;
			if(e instanceof UFOCUnThrowableException)
				throw new UFOCUnThrowableException(msg, e);
			else
				throw new BusinessException(msg, e);
		}

	}

	private HBAloneIDUtil getHBAloneIDUtil() {
		if(hbAloneIdUtil == null) {
			hbAloneIdUtil = new HBAloneIDUtil();
		}
		return hbAloneIdUtil;
	}
	
	/**
	 * 开始执行折算差额 差额需求差额项目=左边项目值-右边项目 并且有折算差额项目都是在固定表内执行计算
	 *
	 * @param pk_report
	 * @param returnobj
	 * @throws BusinessException
	 */
	private void computeConvertBalance(String pk_report, FuncReTurnObj[] returnobjs)
			throws BusinessException {
		try {
			if (this.isIntrateRep(pk_report)) { // 暂时只支持在固定表上挂折算差额项目
				return;
			}

//			ConvertBalanceVO[] cvtvos = new ConvertBalanceQueryServiceImpl()
//					.queryConvertBalanceVOByreport(pk_report);
			
			ConvertBalanceVO[] cvtvos = NCLocator.getInstance().lookup(IConvertBalanceQueryService.class)
					.queryConvertBalanceVOByreport(pk_report);
			
			if (null != cvtvos && cvtvos.length > 0) {

				for (ConvertBalanceVO vo : cvtvos) {

					this.computeByCvtBalance(vo, returnobjs, pk_report);

				}
			}

		} catch (CmdException e) {
			throw new BusinessException(e.getMessage());
		}

	}

	private void computeByCvtBalance(ConvertBalanceVO vo,
			FuncReTurnObj[] returnobjs, String pk_report) throws BusinessException,
			CmdException {
		MeasureVO left = MeasureCache.getInstance().getMeasure(vo.getPk_left());
		MeasureVO right = MeasureCache.getInstance().getMeasure(
				vo.getPk_right());
		MeasureVO project = MeasureCache.getInstance().getMeasure(
				vo.getPk_project());
		MeasureDataVO measdata = null;
		FuncReTurnObj returnobj=null;
		for(FuncReTurnObj obj:returnobjs){
			if(obj.getPk_report().equals(pk_report)){
				returnobj=obj;
				break;
			}
		}

		MeasureDataVO[] vos = HBPubItfService.getRemoteMeasureDataSrv().getRepData(
				new String[] { returnobj.getAloneid() },
				new MeasureVO[] { left, right, project });
		if(vos==null || vos.length==0)
			return;
		UFDouble value = UFDouble.ZERO_DBL;
		for (MeasureDataVO tvo : vos) {
			if (tvo.getMeasureVO().getCode().equals(left.getCode())) {
				value = value.add(tvo.getUFDoubleValue()==null ? UFDouble.ZERO_DBL:tvo.getUFDoubleValue());
			} else if (tvo.getMeasureVO().getCode().equals(right.getCode())) {
				value = value.sub(tvo.getUFDoubleValue()==null ? UFDouble.ZERO_DBL:tvo.getUFDoubleValue());
			} else if (tvo.getMeasureVO().getCode().equals(project.getCode())) {
				//left-rignt-project//否则当project有值的时候，会导致折算后不平
				value = value.add(tvo.getUFDoubleValue()==null ? UFDouble.ZERO_DBL:tvo.getUFDoubleValue());
				measdata = tvo;
			}
		}
		if(measdata==null) return;//折算差额规则三个项目如果其中有两个相同,则不处理.因为此时无效
		measdata.setDataValue(value.toString());
		HBPubItfService.getRemoteMeasureDataSrv().editRepData(returnobj.getAloneid(),
				new MeasureDataVO[] { measdata });
		CellPosition area = MeasureUtil.getSortByPositionMeasureVOs(project,
				pk_report);

		try {
			CellsModel cellsModel = CellsModelOperator.getFormatModelByPK(returnobj.getContextVO(), true);
			ZsAutoCalcUtil zsauto = new ZsAutoCalcUtil(cellsModel, pk_report,true,false);
			zsauto.calcRefrenceFormula(area);
			CellsModelOperator.saveDataToDB(cellsModel, returnobj.getContextVO());

		} catch (ParseException e) {
			throw new  BusinessException(e.getMessage());
		} catch (Exception e) {
			throw new  BusinessException(e.getMessage());
		}

	}

	private RepDataVO saveRepData(MeasurePubDataVO mainPubdata,MeasureDataVO[] measuredatas,String pk_report) throws  BusinessException{
		
		List<MeasurePubDataVO> lstPubdata = new ArrayList<MeasurePubDataVO>();
		lstPubdata.add(mainPubdata);
		for(MeasureDataVO data : measuredatas){
			String alone_id = data.getAloneID();
			MeasurePubDataVO pubdata = measurePubDataCache.get(data.getMeasureVO().getKeyCombPK() + "@" + alone_id);
			try {
				if(pubdata == null) {
					pubdata = MeasurePubDataBO_Client.findByAloneID(data.getMeasureVO().getKeyCombPK(), alone_id);
					measurePubDataCache.put(data.getMeasureVO().getKeyCombPK() + "@" + alone_id, pubdata);
				}
			} catch (Exception e) {
				throw new BusinessException(e);
			}
			if(pubdata != null && !lstPubdata.contains(pubdata))
					lstPubdata.add(pubdata);
		}
		RepDataVO repdata = new RepDataVO(pk_report, mainPubdata.getKType());
		repdata.setDatas(lstPubdata.toArray(new MeasurePubDataVO[0]),measuredatas);
		RepDataVO result=null;
		try {
			result = RepDataBO_Client.createRepData(repdata, null);
		} catch (Exception e) {
			 throw new  BusinessException(e);
		}
		return result;
		
	}
	
	private boolean needLastDataByCache(MeasureDataVO[] datavos, String pk_report,
			boolean iscrossyear) throws BusinessException {
		String map_key = pk_report + "@" + iscrossyear;
		if(needLastPrdDataCache.get(map_key) != null) {
			return needLastPrdDataCache.get(map_key);
		}
		else {
			boolean b = this.needLastData(datavos, pk_report, iscrossyear);
			needLastPrdDataCache.put(map_key, b);
			return b;
		}
	}
	
	// 是否需要上期数据
	private boolean needLastData(MeasureDataVO[] datavos, String pk_report,
			boolean iscrossyear) throws BusinessException {
		RateSetVO[] vos = this.getRateSetVOsByRepPK(pk_report);
		if (null == vos || vos.length == 0) {
			return false;
		}

		for (MeasureDataVO vo : datavos) {
			if (projectMeasMap.containsKey(vo.getMeasureVO().getCode())) {
				String pk_dayrateproject = projectMeasMap.get(vo.getMeasureVO()
						.getCode());
				if(!RateFactory.getPeriodset().contains(pk_dayrateproject) ) {
					//交易日汇率项目，使用单独的折算算法
					String dayRateMethod = measureDayratemethodMap.get(vo.getMeasureVO()
						.getCode());
					if(RateSetVO.DAY_CNVT_MTHD_AMOUNT_VALUE.equals(dayRateMethod) && iscrossyear) {
						
					}
					else {
						return true;
					}
				}
			}
		}

		return false;
	}
	
	private boolean isIntrateRep(String pk_report) {
		if(repIsIntrateMap.get(pk_report) == null) {
			boolean b = HBBBReportUtil.isIntrateRep(pk_report);
			repIsIntrateMap.put(pk_report, new Boolean(b));
			return b;
		}
		else {
			return repIsIntrateMap.get(pk_report);
		}
	}
	
	/**
	 * 获得版本,内部交易表都取个别报表(因为内部交易表没有调整表)
	 * @create by fengzhy at 2012-6-4,下午4:15:09
	 *
	 * @param schemevo
	 * @param rulevo
	 * @param pk_report
	 * @return
	 * @throws BusinessException
	 */
	private int getVersion(HBSchemeVO schemevo, Integer dataType, String pk_report) throws BusinessException {
		if(isIntrateRep(pk_report) && dataType == 1) {//折算规则类型是个别报表调整表，则内部交易表取个别报表
			return HBVersionUtil.getVersion(schemevo, 0,
					false);
		}
		else if(isIntrateRep(pk_report) && dataType == 4) {
			return HBVersionUtil.getVersion(schemevo, 5,
					false);
		}
		else {
			return HBVersionUtil.getVersion(schemevo, dataType,
					false);
		}
	}
	
	private AdjustSchemeVO getAdjustScheme(String pk_adjustScheme) throws BusinessException {
		if(pk_adjustScheme == null)
			return null;
		AdjustSchemeVO vo = adjSchemeCache.get(pk_adjustScheme);
		if(vo == null) {
			vo = (AdjustSchemeVO) NCLocator.getInstance().lookup(IUAPQueryBS.class).retrieveByPK(AdjustSchemeVO.class, pk_adjustScheme);
			adjSchemeCache.put(pk_adjustScheme, vo);
		}
		return vo;
	}
	
	private String getAccLastPrdFromCache(String strTTimeProp,String strTTimeValue,String strAccSchemePK, int key_dateType) {
		
		String key = strTTimeProp + "@" + strTTimeValue + "@" + strAccSchemePK + "@" + key_dateType;
		String value = accLastPeriodCache.get(key);
		if(value == null) {
			TTimeCode inputTTimeCode = TTimeCode.getInstanceOfAcc(strTTimeProp,strTTimeValue,strAccSchemePK);
			value = inputTTimeCode.getNextDate(key_dateType, -1);
			accLastPeriodCache.put(key, value);
		}
		return value;
	}
	
	private String getNatLastPrdFromCache (String strDate, String key_timeProperty) {
		String key = strDate + "@" + key_timeProperty;
		String value = natLastPeriodCache.get(key);
		if(value == null) {
			UFODate ufodate = new UFODate(strDate);
			value = ufodate.getNextDate(key_timeProperty, -1);
			natLastPeriodCache.put(key, value);
		}
		return value;
	}
	
	private ExrateSchemeVO getExtrateSchemeByPkFromCache(String pk_exratescheme) throws BusinessException {
		ExrateSchemeVO exrateScheme = exrateschemePkVOCache.get(pk_exratescheme);
		if(exrateScheme == null) {
			exrateScheme = (ExrateSchemeVO)HBBaseDocItfService.getRemoteUAPQueryBS().retrieveByPK(ExrateSchemeVO.class, pk_exratescheme);
			exrateschemePkVOCache.put(pk_exratescheme, exrateScheme);
		}
		return exrateScheme;
	}
	
//	private IUfoeRepDataSrv getRepDataSrv() {
//		if(repDataSrv == null) {
//			repDataSrv = nc.bs.framework.common.NCLocator.getInstance().lookup(IUfoeRepDataSrv.class);
//		}
//		return repDataSrv;
//	}
	
	private ICommitManageService getCommitSrv() {
		if(commitSrv == null) {
			commitSrv = NCLocator.getInstance().lookup(ICommitManageService.class);
		}
		return commitSrv;
	}
	
	private ICommitQueryService getCommitQrySrv() {
		if(commitQrySrv == null) {
			commitQrySrv = NCLocator.getInstance().lookup(ICommitQueryService.class);
		}
		return commitQrySrv;
	}
	private Map<String, String> getKeymap() {
		return keymap;
	}

}