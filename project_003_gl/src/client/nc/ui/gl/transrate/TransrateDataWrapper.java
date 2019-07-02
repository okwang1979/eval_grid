package nc.ui.gl.transrate;

/**
 * 此处插入类型说明。
 * 创建日期：(2001-11-7 20:27:48)
 * @author：王琛
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import nc.bd.accperiod.AccperiodParamAccessor;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.gl.utils.GLVoTools;
import nc.itf.gl.accountingbook.IAccountingbookService;
import nc.itf.gl.pub.IFreevaluePub;
import nc.itf.glcom.para.GLParaAccessor;
import nc.itf.org.IOrgConst;
import nc.pubitf.uapbd.CurrencyRateUtil;
import nc.ui.gl.exception.AdjustRateNotExitException;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.glcom.numbertool.GlNumberFormat;
import nc.vo.bd.account.AccAssVO;
import nc.vo.bd.account.AccountVO;
import nc.vo.bd.currinfo.AdjustrateVO;
import nc.vo.bd.currinfo.CurrinfoVO;
import nc.vo.bd.currinfo.CurrrateObj;
import nc.vo.bd.currinfo.CurrrateVO;
import nc.vo.bd.currinfo.ICurrinfoConst;
import nc.vo.bd.currtype.CurrtypeVO;
import nc.vo.fipub.freevalue.Module;
import nc.vo.fipub.freevalue.account.proxy.AccAssGL;
import nc.vo.gateway60.accountbook.AccountBookUtil;
import nc.vo.gateway60.itfs.Currency;
import nc.vo.gl.glreport.publictool.PrepareAssParse;
import nc.vo.gl.transrate.TransrateConst;
import nc.vo.gl.transrate.TransrateDefVO;
import nc.vo.gl.transrate.TransrateHeaderVO;
import nc.vo.gl.transrate.TransrateItemVO;
import nc.vo.gl.transrate.TransrateKey;
import nc.vo.gl.transrate.TransrateTableVO;
import nc.vo.gl.transrate.TransrateVO;
import nc.vo.glcom.account.Balanorient;
import nc.vo.glcom.ass.AssVO;
import nc.vo.glcom.balance.GLQueryKey;
import nc.vo.glcom.balance.GlBalanceKey;
import nc.vo.glcom.balance.GlBalanceVO;
import nc.vo.glcom.balance.GlQueryVO;
import nc.vo.glcom.inteltool.CDataSource;
import nc.vo.glcom.inteltool.CGenTool;
import nc.vo.glcom.inteltool.COutputTool;
import nc.vo.glcom.inteltool.CSumTool;
import nc.vo.glcom.shellsort.CShellSort;
import nc.vo.glcom.sorttool.CVoSortTool;
import nc.vo.glcom.sorttool.ISortToolProvider;
import nc.vo.glcom.tools.GLPubProxy;
import nc.vo.glcom.wizard.VoWizard60;
import nc.vo.glpub.IVoAccess;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;


@SuppressWarnings({"rawtypes","deprecation","unchecked"})
public class TransrateDataWrapper {
	public GlNumberFormat format = new GlNumberFormat();

	private nc.vo.gl.transrate.TransrateDefVO m_TransrateDefVO;

	private Map<String,AccAssVO[]> m_GLSubjAssVO;// 转出辅助核算科目

	private Map<String,TransrateTableVO[]> m_TransrateTableVO = null;

	private TransrateVO_Wrapper m_TransrateVOWrapper = null;

	private String pk_accountingbook = null;


	Hashtable htCurAndRate = new Hashtable();

	private Map<String,CurrInfoTool> currinfotool = new HashMap<String,CurrInfoTool>();

	private String pk_corp = null;

	class AssSortTool implements nc.vo.glcom.sorttool.ISortToolProvider {
		public nc.vo.glcom.sorttool.ISortTool getSortTool(Object objCompared) {
			try {
				return new nc.ui.glcom.balance.CAssSortTool();

			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
				return null;
			}
		}
	};

	/**
	 * ExchangeBuildBO 构造子注解。
	 */
	public TransrateDataWrapper() {
		super();
	}

	/***************************************************************************
	 * 其功能为：因为在GlBalanceBO 中读取的数据都是采用ID表示辅助核算组合， 在这里需要对这批ID进行处理，使之成为满足查询条件的数据
	 * 参数： GlBalanceVO[] vos 存在重复纪录的vo数组 创建日期：(2001-10-8 19:55:07) 作者： 王建华
	 **************************************************************************/
	public GlBalanceVO[] combineAss(Vector vos, TransrateVO aTransrateVO) throws Exception {

		nc.vo.glcom.intelvo.CIntelVO tt = new nc.vo.glcom.intelvo.CIntelVO();

		// 在此不指定分组信息,则不分组合计
		
		int[] intSortIndex;
		int intSumLimit = 2; // 指定合计的最小列号组合
		if(isBuSupport(aTransrateVO)){
			intSortIndex = new int[] {GlBalanceKey.GLBALANCE_BUSIUNIT, GlBalanceKey.GLBALANCE_PK_ACCASOA,GlBalanceKey.GLBALANCE_PK_CURRTYPE,GlBalanceKey.GLBALANCE_ASSVOS};
			intSumLimit = 3;
		}else{
			intSortIndex = new int[] {GlBalanceKey.GLBALANCE_PK_ACCASOA,GlBalanceKey.GLBALANCE_PK_CURRTYPE,GlBalanceKey.GLBALANCE_ASSVOS};
		}

		CGenTool genTool = new CGenTool();

		genTool.setLimitSumGen(intSumLimit);
		genTool.setSortIndex(intSortIndex);
		genTool.setGetSortTool(new ISortToolProvider(){
		    public nc.vo.glcom.sorttool.ISortTool getSortTool(java.lang.Object objCompared) {
		    	return new nc.ui.glcom.balance.CAssSortTool();
		    }
		});
		CSumTool sumTool = new CSumTool();
		int sumIndex[] = { GlBalanceKey.GLBALANCE_DEBITQUANTITY,
				GlBalanceKey.GLBALANCE_DEBITAMOUNT,
				GlBalanceKey.GLBALANCE_FRACDEBITAMOUNT,
				GlBalanceKey.GLBALANCE_LOCALDEBITAMOUNT,
				GlBalanceKey.GLBALANCE_CREDITQUANTITY,
				GlBalanceKey.GLBALANCE_CREDITAMOUNT,
				GlBalanceKey.GLBALANCE_FRACCREDITAMOUNT,
				GlBalanceKey.GLBALANCE_LOCALCREDITAMOUNT }; // 要进行合计的列
		sumTool.setSumIndex(sumIndex);

		COutputTool outputTool = new COutputTool();
		outputTool.setRequireOutputDetail(false);
		outputTool.setSummaryCol(-1); // 设置备注信息内容及所对应的列

		CDataSource datasource = new CDataSource();
		datasource.setSumVector(CDataSource.sortVector(vos, genTool, false));

		try {
			tt.setSumTool(sumTool);
			tt.setGenTool(genTool);
			tt.setDatasource(datasource);
			tt.setOutputTool(outputTool);
		} catch (Throwable e) {
			Logger.error(e.getMessage(), e);
		}

		Vector recVector = tt.getResultVector();

		if (recVector == null || recVector.size() == 0)
			return null;

		GlBalanceVO[] VOs = new GlBalanceVO[recVector.size()];
		recVector.copyInto(VOs);
		return VOs;

	}

	/***************************************************************************
	 * 其功能为：因为在GlBalanceBO 中读取的数据都是采用ID表示辅助核算组合， 在这里需要对这批ID进行处理，使之成为满足查询条件的数据
	 * 参数： GlBalanceVO[] vos 存在重复纪录的vo数组 创建日期：(2001-10-8 19:55:07) 作者： 王建华
	 **************************************************************************/
	public GlBalanceVO[] combineAss1(Vector vos, TransrateVO aTransrateVO) throws Exception {

		nc.vo.glcom.intelvo.CIntelVO tt = new nc.vo.glcom.intelvo.CIntelVO();

		// 在此不指定分组信息,则不分组合计

		int[] intSortIndex;
		int intSumLimit = 1; // 指定合计的最小列号组合
		if(isBuSupport(aTransrateVO)){
			intSortIndex = new int[] { GlBalanceKey.GLBALANCE_BUSIUNIT, GlBalanceKey.GLBALANCE_PK_ACCOUNT,GlBalanceKey.GLBALANCE_PK_CURRTYPE };
			intSumLimit = 2;
		}else{
			intSortIndex = new int[] { GlBalanceKey.GLBALANCE_PK_ACCOUNT,GlBalanceKey.GLBALANCE_PK_CURRTYPE };
		}

		CGenTool genTool = new CGenTool();
		genTool.setLimitSumGen(intSumLimit);
		genTool.setSortIndex(intSortIndex);
		// genTool.setGetSortTool(new AssSortTool());

		CSumTool sumTool = new CSumTool();
		int sumIndex[] = { GlBalanceKey.GLBALANCE_DEBITQUANTITY,
				GlBalanceKey.GLBALANCE_DEBITAMOUNT,
				GlBalanceKey.GLBALANCE_FRACDEBITAMOUNT,
				GlBalanceKey.GLBALANCE_LOCALDEBITAMOUNT,
				GlBalanceKey.GLBALANCE_CREDITQUANTITY,
				GlBalanceKey.GLBALANCE_CREDITAMOUNT,
				GlBalanceKey.GLBALANCE_FRACCREDITAMOUNT,
				GlBalanceKey.GLBALANCE_LOCALCREDITAMOUNT }; // 要进行合计的列
		sumTool.setSumIndex(sumIndex);

		COutputTool outputTool = new COutputTool();
		outputTool.setRequireOutputDetail(false);
		outputTool.setSummaryCol(-1); // 设置备注信息内容及所对应的列

		CDataSource datasource = new CDataSource();
		datasource.setSumVector(CDataSource.sortVector(vos, genTool, false));

		try {
			tt.setSumTool(sumTool);
			tt.setGenTool(genTool);
			tt.setDatasource(datasource);
			tt.setOutputTool(outputTool);
		} catch (Throwable e) {
			Logger.error(e.getMessage(), e);
		}

		Vector recVector = tt.getResultVector();

		if (recVector == null || recVector.size() == 0)
			return null;

		GlBalanceVO[] VOs = new GlBalanceVO[recVector.size()];
		recVector.copyInto(VOs);
		return VOs;

	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-12 14:09:40)
	 *
	 * @return nc.vo.gl.transrate.TransrateTableVO
	 * @param param
	 *            nc.vo.glcom.balance.GlBalanceVO
	 * @param param2
	 *            nc.vo.bd.currinfo.AdjustrateVO
	 * @exception java.lang.Exception
	 *                异常说明。
	 */
	private TransrateTableVO[] combineVO(Object[] param, TransrateVO transrateVO) throws java.lang.Exception {

		if (param == null || param.length == 0)
			return null;
		
		TransrateHeaderVO headVO = (TransrateHeaderVO)transrateVO.getParentVO();
		
		TransrateTableVO[] aTransrateTableVO = null;
		nc.vo.glcom.wizard.VoWizard aWizard = new nc.vo.glcom.wizard.VoWizard();
		aWizard.setMatchingIndex(new int[] { TransrateKey.K_PK_OrgBook,
				TransrateKey.K_AccPK, TransrateKey.K_CurrTypePK,
				TransrateKey.K_Ass }, new int[] {
				GlBalanceKey.GLBALANCE_PK_ACCOUNTINGBOOK,
				GlBalanceKey.GLBALANCE_PK_ACCASOA,
				GlBalanceKey.GLBALANCE_PK_CURRTYPE,
				GlBalanceKey.GLBALANCE_ASSVOS });
		aWizard.setAppendIndex(new int[] { TransrateKey.K_PK_OrgBook,TransrateKey.K_PK_UNIT,
				TransrateKey.K_AccPK, TransrateKey.K_AccCode,
				TransrateKey.K_AccName, TransrateKey.K_CurrTypePK,
				TransrateKey.K_Ass, TransrateKey.K_AssID,
				TransrateKey.K_InAssID, TransrateKey.K_CreditBalance,
				TransrateKey.K_CreditLocalBalance, TransrateKey.K_DebitBalance,
				TransrateKey.K_DebitLocalBalance,
				//全局集团本币
				TransrateKey.K_DebitGroupBalance,
				TransrateKey.K_DebitGlobalBalance,
				TransrateKey.K_CreditGroupBalance,
				TransrateKey.K_CreditGlobalBalance}, new int[] {
				GlBalanceKey.GLBALANCE_PK_ACCOUNTINGBOOK,GlBalanceKey.GLBALANCE_BUSIUNIT,
				GlBalanceKey.GLBALANCE_PK_ACCASOA,
				GlBalanceKey.GLBALANCE_SUBJCODE,
				GlBalanceKey.GLBALANCE_SUBJNAME,
				GlBalanceKey.GLBALANCE_PK_CURRTYPE,
				GlBalanceKey.GLBALANCE_ASSVOS, GlBalanceKey.GLBALANCE_ASSID,
				GlBalanceKey.GLBALANCE_ASSID,
				GlBalanceKey.GLBALANCE_CREDITAMOUNT,
				GlBalanceKey.GLBALANCE_LOCALCREDITAMOUNT,
				GlBalanceKey.GLBALANCE_DEBITAMOUNT,
				GlBalanceKey.GLBALANCE_LOCALDEBITAMOUNT,
				GlBalanceKey.GLBALANCE_DEBITGROUPAMOUNT,
				GlBalanceKey.GLBALANCE_DEBITGLOBALAMOUNT,
				GlBalanceKey.GLBALANCE_CREDITGROUPAMOUNT,
				GlBalanceKey.GLBALANCE_CREDITGLOBALAMOUNT});
		
		// 辅助核算需要设置排序规则
		nc.vo.glcom.sorttool.CVoSortTool aCVoSortTool = new nc.vo.glcom.sorttool.CVoSortTool();
		aCVoSortTool.setGetSortTool(new AssSortTool());
		aWizard.setSortTool(aCVoSortTool);

		aWizard.setLeftClass(TransrateTableVO.class);

		IVoAccess[] voAccess = aWizard.concat(aTransrateTableVO,(IVoAccess[])param, true);
		
		for (Object object : voAccess) {
			((TransrateTableVO)object).setM_TransferNO(headVO.getTransferno());
		}
		
		TransrateTableVO[] aVOAccess = new TransrateTableVO[voAccess.length];
		// 拼接调整汇率VO
		for (int i = 0; i < voAccess.length; i++) {
			aVOAccess[i] = (TransrateTableVO) voAccess[i];
			//汇率使用下面取
			aVOAccess[i].setLocalAdjustRate(getAdjustrate(aVOAccess[i].getPKCurrType(),getCurrinfotool(headVO.getPk_glorgbook()).getPk_LocalCurr(), transrateVO));
			if(Currency.isStartGroupCurr(GlWorkBench.getLoginGroup()))
				aVOAccess[i].setM_GroupAdjustRate(getAdjustrateByOrg(GlWorkBench.getLoginGroup(),aVOAccess[i].getPKCurrType(),getCurrinfotool(headVO.getPk_glorgbook()).getPk_groupCurr(),transrateVO ));
			if(Currency.isStartGlobalCurr())
				aVOAccess[i].setM_GlobalAdjustRate(getAdjustrateByOrg(IOrgConst.GLOBEORG,aVOAccess[i].getPKCurrType(),getCurrinfotool(headVO.getPk_glorgbook()).getPk_globalCurr(), transrateVO));
		}
		VoWizard60<TransrateTableVO> aWizard60 = new VoWizard60<TransrateTableVO>();
		aWizard60.setLeftClass(TransrateTableVO.class);
		
		
		// 拼接币种VO
		aWizard60 = new VoWizard60<TransrateTableVO>();
		aWizard60.setLeftClass(TransrateTableVO.class);
		aWizard60.setMatchingField(GLVoTools
				.getStringArray(new int[] { TransrateKey.K_CurrTypePK }),
				new String[] { CurrtypeVO.PK_CURRTYPE });
		aWizard60.setAppendField(GLVoTools.getStringArray(new int[] {
				TransrateKey.K_CurrCode, TransrateKey.K_CurrName }),
				new String[] { CurrtypeVO.CODE, CurrtypeVO.NAME });

		aWizard60.concat((TransrateTableVO[])aVOAccess, getVOWrapper().getCurrtypeVO(), false);
		// 拼接转出方向
		aWizard60 = new VoWizard60();
		aWizard60.setLeftClass(TransrateTableVO.class);

		aWizard60.setMatchingField(GLVoTools.getStringArray(new int[] { TransrateKey.K_AccPK }),new String[] { AccountVO.PK_ACCASOA });
		aWizard60.setAppendField(GLVoTools.getStringArray(new int[] { TransrateKey.K_Orientation }),new String[] { AccountVO.BALANORIENT });

		aWizard60.concat((TransrateTableVO[])aVOAccess, getVOWrapper().getAccVO(headVO.getPk_glorgbook()),false);
		if (aVOAccess.length > 0) {
			aTransrateTableVO = new TransrateTableVO[aVOAccess.length];
			for (int i = 0; i < aVOAccess.length; i++) {
				aTransrateTableVO[i] = (TransrateTableVO) aVOAccess[i];
				if (getCurrinfotool(((TransrateHeaderVO)transrateVO.getParentVO()).getPk_glorgbook()).getCurrtypesys() == 1)
					aTransrateTableVO[i].setLocalAdjustRate(null);
			}
			
			
			
			return aTransrateTableVO;
		} else
			return null;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-17 13:40:34)
	 *
	 * @param voParam
	 *            TransrateTableVO
	 * @return nc.vo.gl.transrate.TransrateTableVO[]
	 */
	private TransrateTableVO[] computeTableVO(TransrateTableVO[] voParam, TransrateVO transrateVO)
			throws Exception {
		
		String pk_accountingbook = ((TransrateHeaderVO)transrateVO.getParentVO()).getPk_glorgbook();
		if (voParam == null)
			return null;
		String sLocCurrPK = getCurrinfotool(pk_accountingbook).getPk_LocalCurr();
//		int iLocalNumber = Currency.getCurrDigit(sLocCurrPK);
		int[] digitAndRoundtype = nc.itf.fi.pub.Currency.getCurrDigitAndRoundtype(sLocCurrPK);

		for (int i = 0; i < voParam.length; i++) {
			
			String srcName = Currency.getCurrNameByPk(voParam[i].getPKCurrType());
			String destName = Currency.getCurrNameByPk(sLocCurrPK);
			CurrinfoVO currinfoVO = Currency.getCurrRateInfo(pk_accountingbook,voParam[i].getPKCurrType(), sLocCurrPK);
			if(currinfoVO == null){
				throw new AdjustRateNotExitException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505", "UPP20021505-000764", null, new String[]{srcName, destName})/* @res "没有设置{0}到{1}的汇率折算信息" */);
			}
			
			int bConvMode = currinfoVO.getConvmode();	
			voParam[i].setPKLocCurrType(sLocCurrPK);			

			if (voParam[i].getOrientation().intValue() == Balanorient.CREDIT) // 贷
			{
				voParam[i].setCreditBalance(voParam[i].getCreditBalance().sub(voParam[i].getDebitBalance()));
				voParam[i].setCreditLocalBalance(voParam[i].getCreditLocalBalance().sub(voParam[i].getDebitLocalBalance()));
				voParam[i].setDebitBalance(null);
				voParam[i].setDebitLocalBalance(null);
				if (voParam[i].getLocalAdjustRate() == null)
					voParam[i].setAdjustCreditLocalBalance(voParam[i].getCreditLocalBalance());
				else {
					if (bConvMode == ICurrinfoConst.CONVMODE_MULT)
						voParam[i].setAdjustCreditLocalBalance(voParam[i].getCreditBalance().multiply(voParam[i].getLocalAdjustRate(),digitAndRoundtype[0], digitAndRoundtype[1]));
					else{
						if(UFDouble.ZERO_DBL.equals(voParam[i].getLocalAdjustRate())){
							voParam[i].setAdjustCreditLocalBalance(new UFDouble(0, digitAndRoundtype[0]));
						}else{
							voParam[i].setAdjustCreditLocalBalance(voParam[i].getCreditBalance().div(voParam[i].getLocalAdjustRate(),digitAndRoundtype[0], digitAndRoundtype[1]));
						}
					}
				}
				voParam[i].setCreditLocalDiff(voParam[i].getAdjustCreditLocalBalance().sub(voParam[i].getCreditLocalBalance()));

			} else {
				voParam[i].setDebitBalance(voParam[i].getDebitBalance().sub(voParam[i].getCreditBalance()));
				voParam[i].setDebitLocalBalance(voParam[i].getDebitLocalBalance().sub(voParam[i].getCreditLocalBalance()));
				voParam[i].setCreditBalance(null);
				voParam[i].setCreditLocalBalance(null);
				if (voParam[i].getLocalAdjustRate() == null)
					voParam[i].setAdjustDebitLocalBalance(voParam[i].getDebitLocalBalance());
				else {
					if (bConvMode == ICurrinfoConst.CONVMODE_MULT)
						voParam[i].setAdjustDebitLocalBalance(voParam[i].getDebitBalance().multiply(voParam[i].getLocalAdjustRate(),digitAndRoundtype[0], digitAndRoundtype[1]));
					else{
						if(UFDouble.ZERO_DBL.equals(voParam[i].getLocalAdjustRate())){
							voParam[i].setAdjustDebitLocalBalance(new UFDouble(0, digitAndRoundtype[0]));
						}else{
							voParam[i].setAdjustDebitLocalBalance(voParam[i].getDebitBalance().div(voParam[i].getLocalAdjustRate(),digitAndRoundtype[0], digitAndRoundtype[1]));
						}
					}
				}
				voParam[i].setDebitLocalDiff(voParam[i].getAdjustDebitLocalBalance().sub(voParam[i].getDebitLocalBalance()));
			}
			if(Currency.isStartGroupCurr(GlWorkBench.getLoginGroup()))
				computeGroup(voParam[i], transrateVO);
			if(Currency.isStartGlobalCurr()){
				computeGlobal(voParam[i], transrateVO);
			}
			
		}

		return voParam;
	}
	//计算集团本币
	private void computeGroup(TransrateTableVO transvo ,TransrateVO transrateVO) throws BusinessException{
		
		String pk_accountingbook = ((TransrateHeaderVO)transrateVO.getParentVO()).getPk_glorgbook();
		String sGroupCurrPK = getCurrinfotool(pk_accountingbook).getPk_groupCurr();
		String curyType = transvo.getPKCurrType();
		if(!Currency.isGroupRawConvertModel(GlWorkBench.getLoginGroup())){  
			curyType = transvo.getPKLocCurrType();
		}
		
		String srcName = Currency.getCurrNameByPk(curyType);
		String destName = Currency.getCurrNameByPk(sGroupCurrPK);
		CurrinfoVO currinfoVO = Currency.getCurrRateInfo(pk_accountingbook,curyType, sGroupCurrPK);
		if(currinfoVO == null){
			throw new BusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505", "UPP20021505-000764", null, new String[]{srcName, destName})/* @res "没有设置{0}到{1}的汇率折算信息" */);
		}
		
		int bGroupConvMode = currinfoVO.getConvmode();
		transvo.setM_PKGroupCurrType(sGroupCurrPK);
//		int iGroupNumber = Currency.getCurrDigit(sGroupCurrPK);
		int[] digitAndRoundtype = nc.itf.fi.pub.Currency.getCurrDigitAndRoundtype(sGroupCurrPK);

		if (transvo.getOrientation().intValue() == Balanorient.CREDIT) // 贷
		{
			transvo.setM_CreditGroupBalance(transvo.getM_CreditGroupBalance().sub(transvo.getM_DebitGroupBalance()));
			transvo.setM_DebitGroupBalance(null);
			
			if (transvo.getM_GroupAdjustRate() == null)
				transvo.setM_AdjustCreditGroupBalance(transvo.getM_CreditGroupBalance());
			else {
				if (bGroupConvMode == ICurrinfoConst.CONVMODE_MULT)
					//判断是否基于组织本币计算
					if(Currency.isGroupRawConvertModel(GlWorkBench.getLoginGroup()))
						transvo.setM_AdjustCreditGroupBalance(transvo.getCreditBalance().multiply(transvo.getM_GroupAdjustRate(),digitAndRoundtype[0],digitAndRoundtype[1]));
					else
						transvo.setM_AdjustCreditGroupBalance(transvo.getAdjustCreditLocalBalance().multiply(transvo.getM_GroupAdjustRate(),digitAndRoundtype[0],digitAndRoundtype[1]));
				else
					if(Currency.isGroupRawConvertModel(GlWorkBench.getLoginGroup()))
						transvo.setM_AdjustCreditGroupBalance(transvo.getCreditBalance().div(transvo.getM_GroupAdjustRate(),digitAndRoundtype[0],digitAndRoundtype[1]));
					else
						transvo.setM_AdjustCreditGroupBalance(transvo.getAdjustCreditLocalBalance().div(transvo.getM_GroupAdjustRate(),digitAndRoundtype[0],digitAndRoundtype[1]));

			}
			transvo.setM_CreditGroupDiff(transvo.getM_AdjustCreditGroupBalance().sub(transvo.getM_CreditGroupBalance()));

		} else {
			transvo.setM_DebitGroupBalance(transvo.getM_DebitGroupBalance().sub(transvo.getM_CreditGroupBalance()));
			transvo.setM_CreditGroupBalance(null);
			if (transvo.getM_GroupAdjustRate() == null)
				transvo.setM_AdjustDebitGroupBalance(transvo.getM_DebitGroupBalance());
			else {
				if (bGroupConvMode == ICurrinfoConst.CONVMODE_MULT)
					if(Currency.isGroupRawConvertModel(GlWorkBench.getLoginGroup()))
						transvo.setM_AdjustDebitGroupBalance(transvo.getDebitBalance().multiply(transvo.getM_GroupAdjustRate(),digitAndRoundtype[0],digitAndRoundtype[1]));
					else
						transvo.setM_AdjustDebitGroupBalance(transvo.getAdjustDebitLocalBalance().multiply(transvo.getM_GroupAdjustRate(),digitAndRoundtype[0],digitAndRoundtype[1]));
				else
					if(Currency.isGroupRawConvertModel(GlWorkBench.getLoginGroup()))
						transvo.setM_AdjustDebitGroupBalance(transvo.getDebitBalance().div(transvo.getM_GroupAdjustRate(),digitAndRoundtype[0],digitAndRoundtype[1]));
					else
						transvo.setM_AdjustDebitGroupBalance(transvo.getAdjustDebitLocalBalance().div(transvo.getM_GroupAdjustRate(),digitAndRoundtype[0],digitAndRoundtype[1]));

			}
			transvo.setM_DebitGroupDiff(transvo.getM_AdjustDebitGroupBalance().sub(transvo.getM_DebitGroupBalance()));
		}
	}
	//计算全局本币
	private void computeGlobal(TransrateTableVO transvo, TransrateVO transrateVO) throws BusinessException{
		
		String pk_accountingbook = ((TransrateHeaderVO)transrateVO.getParentVO()).getPk_glorgbook();
		String sGlobalCurrPK = getCurrinfotool(pk_accountingbook).getPk_globalCurr();
		String curyType = transvo.getPKCurrType();
		if(!Currency.isGlobalRawConvertModel(null)){  
			curyType = transvo.getPKLocCurrType();
		}
		
		String srcName = Currency.getCurrNameByPk(curyType);
		String destName = Currency.getCurrNameByPk(sGlobalCurrPK);
		CurrinfoVO currinfoVO = Currency.getCurrRateInfo(pk_accountingbook,curyType, sGlobalCurrPK);
		if(currinfoVO == null){
			throw new BusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505", "UPP20021505-000764", null, new String[]{srcName, destName})/* @res "没有设置{0}到{1}的汇率折算信息" */);
		}
		
		int bGlobalConvMode = currinfoVO.getConvmode();
		
		transvo.setM_PKGlobalCurrType(sGlobalCurrPK);
//		int iGlobalNumber = Currency.getCurrDigit(sGlobalCurrPK);
		int[] digitAndRoundtype = nc.itf.fi.pub.Currency.getCurrDigitAndRoundtype(sGlobalCurrPK);
		if (transvo.getOrientation().intValue() == Balanorient.CREDIT) // 贷
		{
			transvo.setM_CreditGlobalBalance(transvo.getM_CreditGlobalBalance().sub(transvo.getM_DebitGlobalBalance()));
			transvo.setM_DebitGlobalBalance(null);
			
			if (transvo.getM_GlobalAdjustRate() == null)
				transvo.setM_AdjustCreditGlobalBalance(transvo.getM_CreditGlobalBalance());
			else {
				if (bGlobalConvMode == ICurrinfoConst.CONVMODE_MULT)
					if(Currency.isGlobalRawConvertModel(IOrgConst.GLOBEORG))
						transvo.setM_AdjustCreditGlobalBalance(transvo.getCreditBalance().multiply(transvo.getM_GlobalAdjustRate(), digitAndRoundtype[0], digitAndRoundtype[1]));
					else
						transvo.setM_AdjustCreditGlobalBalance(transvo.getAdjustCreditLocalBalance().multiply(transvo.getM_GlobalAdjustRate(), digitAndRoundtype[0], digitAndRoundtype[1]));
				else
					if(Currency.isGlobalRawConvertModel(IOrgConst.GLOBEORG))
						transvo.setM_AdjustCreditGlobalBalance(transvo.getCreditBalance().div(transvo.getM_GlobalAdjustRate(), digitAndRoundtype[0], digitAndRoundtype[1]));
					else
						transvo.setM_AdjustCreditGlobalBalance(transvo.getAdjustCreditLocalBalance().div(transvo.getM_GlobalAdjustRate(),digitAndRoundtype[0], digitAndRoundtype[1]));

			}
			transvo.setM_CreditGlobalDiff(transvo.getM_AdjustCreditGlobalBalance().sub(transvo.getM_CreditGlobalBalance()));

		} else {
			transvo.setM_DebitGlobalBalance(transvo.getM_DebitGlobalBalance().sub(transvo.getM_CreditGlobalBalance()));
			transvo.setM_CreditGlobalBalance(null);
			if (transvo.getM_GlobalAdjustRate() == null)
				transvo.setM_AdjustDebitGlobalBalance(transvo.getM_DebitGlobalBalance());
			else {
				if (bGlobalConvMode == ICurrinfoConst.CONVMODE_MULT)
					if(Currency.isGlobalRawConvertModel(IOrgConst.GLOBEORG))
						transvo.setM_AdjustDebitGlobalBalance(transvo.getDebitBalance().multiply(transvo.getM_GlobalAdjustRate(),digitAndRoundtype[0], digitAndRoundtype[1]));
					else
						transvo.setM_AdjustDebitGlobalBalance(transvo.getAdjustDebitLocalBalance().multiply(transvo.getM_GlobalAdjustRate(),digitAndRoundtype[0], digitAndRoundtype[1]));
				else
					if(Currency.isGlobalRawConvertModel(IOrgConst.GLOBEORG))
						transvo.setM_AdjustDebitGlobalBalance(transvo.getDebitBalance().multiply(transvo.getM_GlobalAdjustRate(),digitAndRoundtype[0], digitAndRoundtype[1]));
					else
						transvo.setM_AdjustDebitGlobalBalance(transvo.getAdjustDebitLocalBalance().div(transvo.getM_GlobalAdjustRate(),digitAndRoundtype[0], digitAndRoundtype[1]));

					
			}
			transvo.setM_DebitGlobalDiff(transvo.getM_AdjustDebitGlobalBalance().sub(transvo.getM_DebitGlobalBalance()));
		}
	}
	
//	private double roundDouble(double d, int scale) {
//		double dblNew = Math.abs(d);
//
//		long lngTemp = (long) dblNew;
//		double dblTemp = dblNew - lngTemp;
//
//		double convertFactor;
//		convertFactor = 0.5;
//
//		dblNew = lngTemp
//				+ (double) (((long) (dblTemp * Math.pow(10, scale) + convertFactor)) / Math
//						.pow(10, scale));
//
//		if (d < 0)
//			d = dblNew * (-1);
//		else
//			d = dblNew;
//
//		return d;
//	}

//	private nc.vo.pub.lang.UFDouble roundDouble(
//			nc.vo.pub.lang.UFDouble dblTemp, int intDigit) {
//		return new nc.vo.pub.lang.UFDouble(roundDouble(dblTemp.doubleValue(),
//				intDigit));
//	}

	
	/**
	 * 此处插入方法说明。 创建日期：(2001-11-12 10:32:49)
	 *
	 * @return nc.vo.glcom.balance.GlBalanceVO
	 * @param param
	 *            nc.vo.glcom.balance.GlBalanceVO[]
	 * @exception java.lang.Exception
	 *                异常说明。
	 */
	private nc.vo.glpub.IVoAccess[] dealBalanceVO(GlBalanceVO[] param, TransrateVO aTransrateVO)throws java.lang.Exception {
		
		String pk_transrate = ((TransrateHeaderVO)aTransrateVO.getParentVO()).getPk_transRate();
		
		if (param == null || param.length == 0)
			return null;

		GlBalanceVO[] voRet = null;
		Vector v1 = new Vector();
		for (int i = 0; i < param.length; i++) {
			String[] sCurr = getAllOutCurr(aTransrateVO);
			boolean flag = false;
			if(sCurr != null){
				for (int j = 0; j < sCurr.length; j++) {
					if (param[i].getPk_currtype().equals(sCurr[j])) {
						flag = true;
						break;
					}
				}
			}
			if (flag) {
				param[i].setUserData(null);
				v1.addElement(param[i]);
			}
		}
		if (v1 != null && v1.size() > 0) {
			if (getGLSubjAssVO(pk_transrate) != null && getGLSubjAssVO(pk_transrate).length > 0) {
				Vector vret = separate(v1);

				GlBalanceVO[] voret1 = combineAss1((Vector) vret.elementAt(0), aTransrateVO);//辅助核算ID为空
				GlBalanceVO[] voret2 = combineAss((Vector) vret.elementAt(1), aTransrateVO);//辅助核算ID不为空
				Vector vall = new Vector();
				if (voret1 != null && voret1.length > 0)
					for (int i = 0; i < voret1.length; i++) {
						vall.addElement(voret1[i]);
					}
				if (voret2 != null && voret2.length > 0)
					for (int i = 0; i < voret2.length; i++) {
						vall.addElement(voret2[i]);
					}
				voRet = new GlBalanceVO[vall.size()];
				for (int i = 0; i < vall.size(); i++) {
					voRet[i] = (GlBalanceVO) vall.elementAt(i);
				}
			} else {
				voRet = new GlBalanceVO[v1.size()];
				v1.copyInto(voRet);
			}
		}

		return voRet;

	}

	/**
	 * 
	 * @param pk_curr 原币
	 * @param pk_destCurr 本币 
	 * @return
	 * @throws BusinessException
	 */
	private UFDouble getAdjustrate(String pk_curr,String pk_destCurr, TransrateVO transrateVO) throws BusinessException{
		
		String pk_accountingbook = ((TransrateHeaderVO)transrateVO.getParentVO()).getPk_glorgbook();
		String pk_org = AccountBookUtil.getPk_orgByAccountBookPk(pk_accountingbook);
		AccperiodParamAccessor.getInstance().getAccperiodschemePkByPk_org(pk_org);
		String pk_accperiodscheme = AccountBookUtil.getAccPeriodSchemePKByAccountingbookPk(pk_accountingbook);
		UFDouble adjustrate = Currency.getAdjustRate(pk_accountingbook,pk_curr,pk_destCurr,pk_accperiodscheme, getYear(), getPeriod());

		return adjustrate;
	}
	
	/**
	 * 
	 * @param pk_curr 原币
	 * @param pk_destCurr 本币 
	 * @return
	 * @throws BusinessException
	 */
	private UFDouble getAdjustrateByOrg(String pk_org,String pk_curr,String pk_destCurr, TransrateVO transrateVO) throws BusinessException{
		
		TransrateHeaderVO headVO = (TransrateHeaderVO)transrateVO.getParentVO();
		if(pk_org.equals(IOrgConst.GLOBEORG)){
			if(!Currency.isGlobalRawConvertModel(pk_org)){  //如果基于组织本币计算
				pk_curr = getCurrinfotool(headVO.getPk_glorgbook()).getPk_LocalCurr();
			}
		}else{
			if(!Currency.isGroupRawConvertModel(pk_org)){  
				pk_curr = getCurrinfotool(headVO.getPk_glorgbook()).getPk_LocalCurr();
			}
		}
		UFDouble adjustrate = Currency.getAdjustRateByOrg(pk_org,pk_curr,pk_destCurr,null, getYear(), getPeriod());

		return adjustrate;
	}
	
	private AdjustrateVO[] getAdjustRateVOIFMultiCurrtype(CurrtypeVO[] m_CurrtypeVOs, String PKCorp, String year,String period, TransrateVO transrateVO) throws Exception {
		
		TransrateHeaderVO headVO = (TransrateHeaderVO)transrateVO.getParentVO();
		AdjustrateVO[] adjustvos = null;
		Vector vAdjustrateVOs = new Vector();
		String pk_org = AccountBookUtil.getPk_orgByAccountBookPk(headVO.getPk_glorgbook());
		AccperiodParamAccessor.getInstance().getAccperiodschemePkByPk_org(pk_org);
		String pk_accperiodscheme = AccountBookUtil.getAccPeriodSchemePKByAccountingbookPk(headVO.getPk_glorgbook());

		boolean isMultiCurrtype = getCurrinfotool(headVO.getPk_glorgbook()).getCurrtypesys() == 1 ? true : false;

		// 单主币情况下取调整汇率

		if (!isMultiCurrtype) {
			for (int i = 0; i < m_CurrtypeVOs.length; i++) {
				if (!m_CurrtypeVOs[i].getPk_currtype().equals(getCurrinfotool(headVO.getPk_glorgbook()).getPk_LocalCurr())) {
					nc.vo.bd.currinfo.AdjustrateVO aAdjustrateVO = new nc.vo.bd.currinfo.AdjustrateVO();
					nc.bs.logging.Logger.debug("进入UAP getCurrArith().getAdjustRate调用");
					nc.bs.logging.Logger.debug("公司： " + getPk_org());
					nc.bs.logging.Logger.debug("源币种： "+ m_CurrtypeVOs[i].getPk_currtype());
					nc.bs.logging.Logger.debug("目的币种： "+ getCurrinfotool(headVO.getPk_glorgbook()).getPk_LocalCurr());
					nc.bs.logging.Logger.debug("期间pk_accperiod： " + "i don't know ");
					nc.bs.logging.Logger.debug("年： " + year);
					nc.bs.logging.Logger.debug("期间： " + period);
					UFDouble adjustrate = null;
					try {
						
						adjustrate = CurrencyRateUtil.getInstanceByAccountingBook(headVO.getPk_glorgbook()).getAdjustRate(m_CurrtypeVOs[i].getPk_currtype(),getCurrinfotool(headVO.getPk_glorgbook()).getPk_LocalCurr(),pk_accperiodscheme, year, period);
					} catch (Exception e) {
						Logger.error(e.getMessage(), e);
					}

					nc.bs.logging.Logger.debug("返回调整汇率为：： " + adjustrate);
					if (adjustrate != null) {
						aAdjustrateVO.setAdjustrate(adjustrate);

						vAdjustrateVOs.addElement(aAdjustrateVO);
					}
				}
			}
		}
		if (vAdjustrateVOs.size() > 0) {
			adjustvos = new AdjustrateVO[vAdjustrateVOs.size()];
			vAdjustrateVOs.copyInto(adjustvos);
		}
		return adjustvos;
	}

	/**
	 * 取所有已设置调整汇率的币种 创建日期：(2001-11-9 16:19:13)
	 *
	 * @return nc.vo.bd.currinfo.AdjustrateVO[]
	 */
	public String[] getAdjustPK_CurrTypes(AdjustrateVO[] adjustvos) {
		if (adjustvos == null)
			return null;
		String[] pk_AdjustCurrtypes = null;
		Vector vAdjustCurrtypes = new Vector();
		for (int i = 0; i < adjustvos.length; i++) {
			//vAdjustCurrtypes.addElement(adjustvos[i].getPk_currtype());
		}
		if (vAdjustCurrtypes != null && vAdjustCurrtypes.size() > 0) {
			pk_AdjustCurrtypes = new String[vAdjustCurrtypes.size()];
			vAdjustCurrtypes.copyInto(pk_AdjustCurrtypes);
		}
		return pk_AdjustCurrtypes;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-10 12:01:27)
	 *
	 * @return java.lang.String[]
	 * @param param
	 *            nc.vo.gl.transrate.TransrateDefVO
	 * @exception java.lang.Exception
	 *                异常说明。
	 */
	private String[] getAllOutCurr(TransrateVO aTransrateVO)
			throws java.lang.Exception {

		TransrateItemVO[] aItemVO = (TransrateItemVO[]) aTransrateVO.getChildrenVO();

		Hashtable htCurr = new Hashtable();

		for (int i = 0; i < aItemVO.length; i++) {
			if (!aItemVO[i].getIsTransfer().booleanValue())
				continue;
			if (!htCurr.containsKey(aItemVO[i].getPk_currtype())) {
				htCurr.put(aItemVO[i].getPk_currtype(), "");
			}
		}
		if (htCurr.size() > 0) {
			String[] currtype = new String[htCurr.size()];
			htCurr.keySet().toArray(currtype);
			return currtype;
		} else
			return null;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-10 12:47:47)
	 *
	 * @return java.lang.String[]
	 * @param pk_acc
	 *            java.lang.String
	 * @exception java.lang.Exception
	 *                异常说明。
	 */
	private Map<String,AccAssVO[]> getAss() throws java.lang.Exception {
		// 根据会计科目查找辅助核算项目
		
		Map<String,AccAssVO[]> pkTranrate2AssVOs = new HashMap<String,AccAssVO[]>();
		if(getTransrateDefVO().getTransrateVO() != null){
			for (TransrateVO transrateVO : getTransrateDefVO().getTransrateVO()) {
				
				TransrateItemVO[] items = (TransrateItemVO[]) transrateVO.getChildrenVO();
				Vector accsubj = new Vector();
				for (int i = 0; i < items.length; i++) {
					accsubj.addElement(items[i].getPk_accsubj());
				}
				String[] sAccsubj = new String[accsubj.size()];
				accsubj.copyInto(sAccsubj);
				Map<String, List<AccAssVO>> accassMap = AccAssGL.queryAllBySubjPKs(sAccsubj, GlWorkBench.getBusiDate().toStdString());
				List<AccAssVO> accassList = new ArrayList<AccAssVO>();
				for (int i  = 0; sAccsubj != null && i < sAccsubj.length; i ++) {
					List<AccAssVO> accasss = accassMap.get(sAccsubj[i]);
					if (accasss != null) {
						AccAssVO[] accas = accasss.toArray(new AccAssVO[0]);
						for (int j = 0; accas != null && j < accas.length; j ++ ) {
							accassList.add(accas[j]); //原来是i
						}
					}
				}
				
				pkTranrate2AssVOs.put(((TransrateHeaderVO)transrateVO.getParentVO()).getPk_transRate(), accassList.toArray(new AccAssVO[0]));
			}
		}
		
		return pkTranrate2AssVOs;
	}


	/**
	 * 此处插入方法说明。 创建日期：(2001-11-9 16:05:08)
	 *
	 * @return nc.vo.glcom.balance.GlBalanceVO[]
	 */
	private nc.vo.glcom.balance.GlBalanceVO[] getBalanceVO(TransrateDefVO defvo, TransrateVO vo)throws Exception {
		
		TransrateHeaderVO headVO = (TransrateHeaderVO)vo.getParentVO();
		TransrateItemVO[] aItemVO = (TransrateItemVO[]) vo.getChildrenVO();
		PrepareAssParse pap = new PrepareAssParse();
		pap.setPk_accountingbook(headVO.getPk_glorgbook());
		nc.vo.glcom.balance.GlQueryVO aQueryVO = new nc.vo.glcom.balance.GlQueryVO();
		aQueryVO.setpk_accountingbook(new String[] { headVO.getPk_glorgbook() });
		aQueryVO.setBaseAccountingbook(headVO.getPk_glorgbook());
		aQueryVO.setQueryByPeriod(true);
		aQueryVO.setYear(defvo.getYear());
		aQueryVO.setPeriod(defvo.getPeriod());
		aQueryVO.setEndPeriod(defvo.getPeriod());
		aQueryVO.setSubjVersion(defvo.getM_stddate());
		aQueryVO.setShowZeroAmountRec(false);
		aQueryVO.setIncludeUnTallyed(defvo.isDealNoReg().booleanValue());
		if (getGLSubjAssVO(headVO.getPk_transRate()) == null || getGLSubjAssVO(headVO.getPk_transRate()).length == 0) {
			if(isBuSupport(vo)){
				aQueryVO.setGroupFields(new int[] {GLQueryKey.K_GLQRY_PK_ACCOUNTINGBOOK,GLQueryKey.K_GLQRY_UNIT, GLQueryKey.K_GLQRY_ACCOUNT,GLQueryKey.K_GLQRY_CURRTYPE });
			}else{
				aQueryVO.setGroupFields(new int[] {GLQueryKey.K_GLQRY_PK_ACCOUNTINGBOOK, GLQueryKey.K_GLQRY_ACCOUNT,GLQueryKey.K_GLQRY_CURRTYPE });
			}
		} else {
			if(isBuSupport(vo)){
				aQueryVO.setGroupFields(new int[] {GLQueryKey.K_GLQRY_PK_ACCOUNTINGBOOK,GLQueryKey.K_GLQRY_UNIT, GLQueryKey.K_GLQRY_ACCOUNT,GLQueryKey.K_GLQRY_ASSID, GLQueryKey.K_GLQRY_CURRTYPE });
			}else{
				aQueryVO.setGroupFields(new int[] {GLQueryKey.K_GLQRY_PK_ACCOUNTINGBOOK, GLQueryKey.K_GLQRY_ACCOUNT,GLQueryKey.K_GLQRY_ASSID, GLQueryKey.K_GLQRY_CURRTYPE });
			}
		}
		Vector<GlBalanceVO> vRetBalance = new Vector<GlBalanceVO>();
		Hashtable<String, Vector<String>> accsubjsTable = new Hashtable<String, Vector<String>>();
		Vector<String> accsubjsVector = null;
		GlQueryVO qryvo = null;
		// 没有辅助项的科目统一处理
		try {
			for (int i = 0; i < aItemVO.length; i++) {
				nc.vo.glcom.ass.AssVO[] qryAssvos = null;
				if (aItemVO[i].getAss() != null&& aItemVO[i].getAss().trim().length() > 0) {
					qryAssvos = pap.prepareAssitantToAssvos(aItemVO[i].getAss(), AccountBookUtil.getPk_orgByAccountBookPk(headVO.getPk_glorgbook()));
					if (qryAssvos != null && qryAssvos.length > 0) {
						qryvo = (GlQueryVO) aQueryVO.clone();
						qryvo.setPk_account(new String[] { aItemVO[i].getPk_accasoa() });
						qryvo.setPk_currtype(aItemVO[i].getPk_currtype());
						qryvo.setAssVos(qryAssvos);
						getEndBalance(vRetBalance, qryvo);
					}
				} else if (accsubjsTable.containsKey(aItemVO[i].getPk_currtype())) {
					(accsubjsTable.get(aItemVO[i].getPk_currtype())).add(aItemVO[i].getPk_accasoa());
				} else {
					accsubjsVector = new Vector<String>();
					accsubjsVector.add(aItemVO[i].getPk_accasoa());
					accsubjsTable.put(aItemVO[i].getPk_currtype(),accsubjsVector);
				}
			}
			Enumeration e = accsubjsTable.keys();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String[] accsubjs = new String[((Vector) accsubjsTable.get(key)).size()];
				((Vector) accsubjsTable.get(key)).copyInto(accsubjs);
				qryvo = (GlQueryVO) aQueryVO.clone();
				qryvo.setPk_account(accsubjs); 
				qryvo.setPk_currtype(key);
				getEndBalance(vRetBalance, qryvo);
			}
		} catch (Exception e) {
			Logger.info(e, this.getClass(), e.getMessage());
			throw e;
		}
		GlBalanceVO[] retVos = null;
		if (vRetBalance.size() > 0) {
			retVos = new GlBalanceVO[vRetBalance.size()];
			vRetBalance.copyInto(retVos);
		}
		return retVos;
	}
	
	private boolean isBuSupport(TransrateVO aTransrateVO){
		try {
			return GLParaAccessor.isSecondBUStart(((TransrateHeaderVO)aTransrateVO.getParentVO()).getPk_glorgbook()).booleanValue();
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		}
		return false;
	}
	/**
	 * 根据queryVo得到查询结果
	 *
	 * @param vRetBalance
	 * @param queryVo
	 * @throws Exception
	 */
	private void getEndBalance(Vector<GlBalanceVO> vRetBalance,
			GlQueryVO queryVo) throws Exception {
		GlBalanceVO[] vos = GLPubProxy.getRemoteCommAccBookPub().getEndBalance(
				queryVo);
		if (vos != null && vos.length > 0) {
			for (int j = 0; j < vos.length; j++) {
				vRetBalance.addElement(vos[j]);
			}
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-17 12:54:41)
	 *
	 * @return nc.ui.bd.b21.CurrArith
	 */

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-17 13:52:40)
	 *
	 * @return nc.vo.gl.transrate.TransrateTableVO[]
	 * @exception java.lang.Exception
	 *                异常说明。
	 */
	public TransrateTableVO[] getFinalTransRateTable(TransrateVO vo) throws AdjustRateNotExitException, java.lang.Exception {

		TransrateItemVO[] aItemVO = (TransrateItemVO[]) vo.getChildrenVO();
		
		HashMap hmKeyToAss = new HashMap();  //转出的辅助核算
		HashMap hmKeyToInAss = new HashMap(); //转入的辅助核算
		if (aItemVO == null || aItemVO.length == 0)
			throw new Exception(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505", "UPP20021505-000364")/* @res "转出科目未定义！" */);
		
		for (int i = 0; i < aItemVO.length; i++) {
			String key = aItemVO[i].getPk_accasoa()+ aItemVO[i].getPk_currtype();
			hmKeyToAss.put(key, aItemVO[i].getAss());
			hmKeyToInAss.put(key, aItemVO[i].getInAss());
		}
		TransrateTableVO[] tempvos = getTransRateTable(false, null, vo);//取到转出的TransrateTableVO
		TransrateTableVO[] finaltempvos = null;
		if (tempvos == null || tempvos.length == 0)
			throw new Exception(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505", "UPP20021505-000365")/* @res "没有生成凭证分录" */);
		
		IAccountingbookService service = (IAccountingbookService)NCLocator.getInstance().lookup(IAccountingbookService.class.getName());
		List<TransrateTableVO> vTempvos = service.adjustTransrateAss(tempvos, hmKeyToAss, hmKeyToInAss, getPk_accountingbook());
		if (vTempvos.size() > 0) {
			finaltempvos = new TransrateTableVO[tempvos.length];
			finaltempvos = vTempvos.toArray(new TransrateTableVO[0]);
		}

		return finaltempvos;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-12 10:54:51)
	 *
	 * @return nc.vo.bd.account.AccAssVO[]
	 */
	private nc.vo.bd.account.AccAssVO[] getGLSubjAssVO(String pk_transrate) {
		if(m_GLSubjAssVO == null){
			return null;
		}else{
			return m_GLSubjAssVO.get(pk_transrate);
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-17 13:16:28)
	 *
	 * @return java.lang.String
	 */
	private String getPeriod() {
		if (getTransrateDefVO() == null)
			return null;
		else
			return getTransrateDefVO().getPeriod();

	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-12 10:41:41)
	 *
	 * @return nc.vo.gl.transrate.TransrateDefVO
	 */
	public nc.vo.gl.transrate.TransrateDefVO getTransrateDefVO() {
		return m_TransrateDefVO;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-17 13:52:40)
	 *
	 * @return nc.vo.gl.transrate.TransrateTableVO[]
	 * @exception java.lang.Exception
	 *                异常说明。
	 */
	public TransrateTableVO[] getTransRateTable(boolean isRefresh, HashMap<Integer,UFDouble> rates, TransrateVO vo)throws AdjustRateNotExitException, java.lang.Exception {
		
		return getTransRateTable(isRefresh,rates,vo,false);
	}
	
	
	
	/**
	 * 央客新增方法，调用此方法汇率使用对应会计期间最后一天的全局汇率,只添加一个参数boolean isUseDayRate at:2019-7-1
	 *
	 * @return nc.vo.gl.transrate.TransrateTableVO[]
	 * @exception java.lang.Exception
	 *                异常说明。
	 */
	public TransrateTableVO[] getTransRateTable(boolean isRefresh, HashMap<Integer,UFDouble> rates, TransrateVO vo,boolean isUseDayRate)throws AdjustRateNotExitException, java.lang.Exception {
		
		if (m_TransrateTableVO == null || isRefresh) {
			try {
				m_GLSubjAssVO = getAss();
				m_TransrateTableVO = new HashMap<String,TransrateTableVO[]>();
				
				if(getTransrateDefVO().getTransrateVO() != null){
					for (TransrateVO transrateVO : getTransrateDefVO().getTransrateVO()) {
						
						// 取得科目项目余额
						GlBalanceVO[] aGLBalanceVO = getBalanceVO(getTransrateDefVO(), transrateVO);
						
						if(aGLBalanceVO == null){
							m_TransrateTableVO.put(((TransrateHeaderVO)transrateVO.getParentVO()).getPk_transRate(), new TransrateTableVO[0]);
							continue;
						}
						
						//增加辅助
						IFreevaluePub freevaluebo = NCLocator.getInstance().lookup(IFreevaluePub.class);
						String[] assids = new String[aGLBalanceVO.length];
						for(int i=0;i<assids.length;i++){
							if(aGLBalanceVO[i].getAssid() != null){
								assids[i] = aGLBalanceVO[i].getAssid();
							}
						}
						ConcurrentHashMap<String,AssVO[]> assvos = freevaluebo.queryAssvosByAssids(assids, Module.GL);
						for(GlBalanceVO bal : aGLBalanceVO){
							if(bal.getAssid() != null){
								bal.setAssVos(assvos.get(bal.getAssid()));
							}
						}
						nc.vo.glpub.IVoAccess[] aVoAccess = dealBalanceVO(aGLBalanceVO, transrateVO);
						
						TransrateTableVO[] voRet = combineVO(aVoAccess, transrateVO);
						
						if (rates != null && rates.size()> 0) { 
							for (int i = 0; voRet != null && i < voRet.length; i++) {
								Set<Integer> keys = rates.keySet();
								for(Integer key:keys){
									if(key==TransrateConst.LOCALRATE)
										voRet[i].setLocalAdjustRate(rates.get(key));
									else if(key==TransrateConst.GROUPRATE)
										voRet[i].setM_GroupAdjustRate(rates.get(key));
									else
										voRet[i].setM_GlobalAdjustRate(rates.get(key));
								}
								
							}
						}
						//*************央客添加代码 begin by ：王志强  at：2019-7-1
						//汇率取对应日期的汇率，如果对应日期月没有录入汇率取0
						else if(isUseDayRate) {
							String year  =  this.getYear();
							String month  = this.getPeriod();
							
							UFDate date = new UFDate(year+"-"+month+"-01");
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(date.toDate());
							int days =   calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
							
							date =  new UFDate(year+"-"+month+"-"+days); 
							for(TransrateTableVO row:voRet){
//								row.getPKCurrType()
								
								CurrrateObj currVo = CurrencyRateUtil.getGlobeInstance().getCurrrateAndRate(row.getPKCurrType(), getCurrinfotool(row.getM_PK_OrgBook()).getPk_LocalCurr(),date , 0);
								
//								CurrrateObj currVo = CurrencyRateUtil.getGlobeInstance().getCurrrateAndRate(curType, getModel().getCurrinfotool(getPk_accountintbook()).getPk_LocalCurr(),date , 0);
								
								//查找对应月份数据如果找不到则不做处理
								CurrrateVO[] cVos  = currVo.getCurrinfoVO().getCurrrate();
								UFDouble rate = new UFDouble(0);
								if(cVos!=null&&cVos.length>0){
									boolean haveCurrentMonthRate =  false;
									for(CurrrateVO cVo:cVos){
										UFDate rateDate  =  new UFDate(cVo.getRatedate().toDate());
										if(date.getYear()==rateDate.getYear()&&date.getMonth()==rateDate.getMonth()){
											haveCurrentMonthRate = true;
											break;
										}
										
									}
									if(haveCurrentMonthRate){
										rate = currVo.getRate();
									}
								} 
								
								row.setLocalAdjustRate(rate);

								
							}
							
						}
//						aVOAccess[i].getPKCurrType(),getCurrinfotool(headVO.getPk_glorgbook()).getPk_groupCurr()
						//*****************end
						
						// 计算调整后的数据
						TransrateTableVO[] tableVOs = computeTableVO(voRet, transrateVO);
						m_TransrateTableVO.put(((TransrateHeaderVO)transrateVO.getParentVO()).getPk_transRate(), tableVOs);
					}
				}
			} catch (Exception e) {
				Logger.error(e.getMessage());
				throw e;

			}
		}
		
		if(vo == null){
			
			//返回全部结果
			List<TransrateTableVO> list = new ArrayList<TransrateTableVO>();
			for (TransrateTableVO[] vos : m_TransrateTableVO.values()) {
				if(vos != null){
					for (TransrateTableVO transrateTableVO : vos) {
						list.add(transrateTableVO);
					}
				}
			}
			TransrateTableVO[] res = list.toArray(new TransrateTableVO[0]);
			CShellSort objShellSort = new CShellSort();
			CVoSortTool objVoSortTool = new CVoSortTool();
			objVoSortTool.setSortIndex(new int[]{TransrateKey.K_PK_OrgBook, TransrateKey.K_TransferNO});
			objShellSort.sort(res, objVoSortTool, false);
			
			return res;
		}else{
			//只返回指定规则的结果
			return m_TransrateTableVO.get(((TransrateHeaderVO)vo.getParentVO()).getPk_transRate());
		}
	}
	
	
	

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-26 16:36:24)
	 *
	 * @return nc.ui.gl.transrate.TransrateVO_Wrapper
	 */
	public TransrateVO_Wrapper getVOWrapper() {
		if (m_TransrateVOWrapper == null)
			m_TransrateVOWrapper = new TransrateVO_Wrapper(getPk_accountingbook());
		return m_TransrateVOWrapper;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-17 13:16:28)
	 *
	 * @return java.lang.String
	 */
	private String getYear() {
		if (getTransrateDefVO() == null)
			return null;
		else
			return getTransrateDefVO().getYear();

	}

	/**
	 * 此处插入方法说明。 创建日期：(2003-5-12 16:27:35)
	 *
	 * @return nc.vo.glcom.balance.GlBalanceVO[]
	 * @param v
	 *            java.util.Vector
	 */
	public Vector separate(Vector v) {
		Vector vret = new Vector();
		Vector v1 = new Vector();
		Vector v2 = new Vector();
		if (v.size() > 0) {
			for (int i = 0; i < v.size(); i++) {

				GlBalanceVO vo = (GlBalanceVO) v.elementAt(i);
				if (vo.getAssID() == null)
					v1.addElement(vo);
				else
					v2.addElement(vo);

			}
		}
		vret.addElement(v1);
		vret.addElement(v2);
		return vret;
	}


	/**
	 * 此处插入方法说明。 创建日期：(2001-11-9 16:21:53)
	 *
	 * @return nc.vo.gl.transrate.TransrateResultVO[]
	 * @param param
	 *            nc.vo.gl.transrate.TransrateDefVO
	 */
	public void setTransrateDef(nc.vo.gl.transrate.TransrateDefVO param) {
		m_TransrateDefVO = param;

		return;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-17 13:52:40)
	 *
	 * @return nc.vo.gl.transrate.TransrateTableVO[]
	 * @exception java.lang.Exception
	 *                异常说明。
	 */
	public void setTransRateTable(Map<String,TransrateTableVO[]> param) {
		m_TransrateTableVO = param;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-27 9:19:10)
	 *
	 * @param param
	 *            nc.ui.gl.transrate.TransrateVO_Wrapper
	 */
	public final void setVOWrapper(TransrateVO_Wrapper param) {
		m_TransrateVOWrapper = param;

	}

	/**
	 * 此处插入方法说明。 创建日期：(2003-5-12 16:04:40)
	 *
	 * @param v
	 *            java.util.Vector
	 * @param b
	 *            boolean
	 */
	public void transNulll(GlBalanceVO[] vos, boolean b) {
		if (vos != null && vos.length > 0) {
			if (b) {
				for (int i = 0; i < vos.length; i++) {
					GlBalanceVO vo = (GlBalanceVO) vos[i];
					if (vo.getAssID() == null)
						vo.setAssID("");
				}
			} else {
				for (int i = 0; i < vos.length; i++) {
					GlBalanceVO vo = (GlBalanceVO) vos[i];
					if (vo.getAssID() != null && vo.getAssID() == "")
						vo.setAssID(null);
				}
			}
		}
	}
	
	/**
	 * 改成支持多个规则之后，此核算账簿的值只限于与会计期间方案
	 * @return
	 */
	public String getPk_accountingbook(){
		if(pk_accountingbook == null){
			pk_accountingbook = GlWorkBench.getDefaultMainOrg();
		}
		return pk_accountingbook;
	}
	
	public void setPk_accountingbook(String pk_accountingbook) {
		this.pk_accountingbook = pk_accountingbook;
	}
	/**
	 * @return 返回 currinfotool。
	 */
	public CurrInfoTool getCurrinfotool(String pk_accountingbook ) {
		if(!currinfotool.containsKey(pk_accountingbook)){
			currinfotool.put(pk_accountingbook, new CurrInfoTool(pk_accountingbook));
		}
		return currinfotool.get(pk_accountingbook);
	}
	
	/**
	 * @return 返回 pk_corp。
	 */
	public String getPk_org() {
		if (pk_corp == null) {
			try {
				pk_corp = AccountBookUtil.getPk_orgByAccountBookPk(getPk_accountingbook());
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);

			}
		}
		return pk_corp;
	}
	
	
}