package nc.ui.iufo.dataexchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import nc.bs.framework.common.NCLocator;
import nc.impl.iufo.utils.NCLangUtil;
import nc.itf.iufo.keydef.ICorpQuerySrv;
import nc.itf.iufo.storecell.IStoreCellPackQrySrv;
import nc.pub.iufo.accperiod.AccPeriodSchemeUtil;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.pub.iufo.cache.KeyGroupCache;
import nc.pub.iufo.cache.MeasureCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.CommonException;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pubitf.bd.accessor.GeneralAccessorFactory;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.data.RepDataBO_Client;
import nc.ui.iufo.input.InputKeywordsUtil;
import nc.ui.iufo.input.InputUtil;
import nc.util.iufo.pub.UfoStringUtils;
import nc.vo.bd.accessor.IBDData;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.param.TwoTuple;
import nc.vo.iufo.pub.date.UFODate;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.trade.excelimport.conversion.UFDateParseUtil;

import org.apache.commons.lang.StringUtils;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.table.exarea.ExtendAreaCell;
import com.ufida.iufo.table.exarea.ExtendAreaConstants;
import com.ufsoft.iufo.check.vo.CheckResultVO;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.dynarea.DynAreaUtil;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.fmtplugin.key.KeywordModel;
import com.ufsoft.iufo.fmtplugin.measure.MeasureModel;
import com.ufsoft.iufo.fmtplugin.service.ReportFormatSrv;
import com.ufsoft.iufo.report.propertyoperate.UFOFormulaEditControl;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.script.util.FmlCellEditUtil;
import com.ufsoft.script.util.FormulaCellEditUtil;
import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;

/**
 * @update 2004-08-25 whtao 修改错误：1、无法导入动态区域下有固定区域指标数据，无法导入动态区域有单位名称的报表数据。
 *         2、对有参照类型的指标或关键字，其数据中有小写的英文，无法导入
 *         错误原因是：1、对动态区下的固定区域位置计算有问题;2、对参照类型的名称比较存在bug
 * @end * @update 2004-08-07 whtao 解决动态区下方还有主表指标且行数达于两行时第二行的数据无法导入的错误
 *      原因是在计算实际主表指标位置时方法错误，导致没有定位到正确的Excel位置
 * @end * @update 2004-07-26 whtao 解决动态区为公有关键字时，写入了lineNo的错误
 * @end
 *
 * @update 2004-07-26 whtao 解决整表只有一个指标数据时导入报错的错误
 * @end <p>
 *      Title: 多表页Excel数据导入执行
 *      </p>
 *      <p>
 *      Description:
 *      </p>
 *      <p>
 *      Copyright: Copyright (c) 2004-04-13
 *      </p>
 *      <p>
 *      Company:
 *      </p>
 * @author whtao
 * @version 1.0
 */

public class MultiSheetImportUtil implements IUfoContextKey
{
	private static final String NOT_FIND = "NOT_FIND";
	protected String m_strOrgPK = null;
	protected String m_strRmsPK = null;
	protected String m_strGroupPK=null;
	protected String sheetName = null;
	protected String repcode = null;
	protected String repName = null;
	protected CellsModel excelCellsModel = null;
	protected ReportFormatSrv repFormatSrv = null;
	DataSourceVO m_voDataSource = null;
	protected String m_strDataUnitPK = null;
	protected boolean m_bFormCanImport = true;
//	protected boolean m_bSysFormCanImport = true;

	protected String m_strUserPK= null;
	protected int[] dynEnds;// 动态区结束行/列位置，该位置是实际位置

	protected int[] exMode;//TODO

	protected int errCount = 0;// 当前报表中的错误数量，如果超过SINGLE_SHEET_SER_NUM，则该报表中的数据不被保存
	private final Log logFile = new Log();
	@SuppressWarnings("unchecked")
	private Vector m_vCheckResult = new Vector();
	protected MeasurePubDataVO mainPubDataVO = null;
	@SuppressWarnings("unchecked")
	protected ArrayList<MeasurePubDataVO>dynPubDatavoList = null;
	public static final int SINGLE_SHEET_SER_NUM = 10;// 预置错误数
	private static final String MEAS_TYPE_NUM = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0368")/*@res "数值"*/; // "数值"
	protected boolean bAutoCalc = false;
	protected String strLoginDate = null;
	protected int dynAddRowActIndex = 0;

	protected ReportVO report = null;

	//edit by wuyongc at 2014年4月3日16:08:26
//	private IntSet pubDataHashKetSet = null;
	private Set<String> aloneIDSet=null;

	//按照编码查询自定义项，参照类的数据。
	private boolean bQryAsCode = true;
	/**
	 * 构造器,此时清空日志中存储的内容
	 *
	 * @param repcode
	 *            String
	 * @param sheetVal
	 *            HashMap
	 * @param sheetname
	 *            String
	 * @param repname
	 *            String
	 */
	public MultiSheetImportUtil(String reppk, CellsModel cellsModel,
			String sheetname, int[] dynendrow, MeasurePubDataVO mainpubVo,
			String strUserPK, DataSourceVO dataSource, String strOrgPK,
			String strRmsPK,String strCurGroupPK, boolean bAutoCalc, String strLoginDate)
	{
		init(reppk, cellsModel, sheetname, dynendrow, mainpubVo, strUserPK,
				dataSource, strOrgPK, strRmsPK, strCurGroupPK,bAutoCalc, strLoginDate);
	}

	@SuppressWarnings("unchecked")
	public void init(String repcode, CellsModel cellsModel, String sheetname,
			int[] dynendrow, MeasurePubDataVO mainpubVo, String strUserPK,
			DataSourceVO dataSource, String strOrgPK, String strRmsPK,String strCurGroupPK,
			boolean bAutoCalc, String strLoginDate)
	{
		this.repcode = repcode;
		this.excelCellsModel = cellsModel;
		this.sheetName = sheetname;
//		this.dynEndRow = -1;
		if (dynendrow != null)
			this.dynEnds = dynendrow;
		this.mainPubDataVO = mainpubVo;
		this.dynPubDatavoList = new ArrayList();
		this.m_strUserPK=strUserPK;
		this.m_voDataSource = dataSource;
		m_strOrgPK = strOrgPK;
		m_strRmsPK = strRmsPK;
		m_strGroupPK=strCurGroupPK;
		errCount = 0;
		this.bAutoCalc = bAutoCalc;
		this.strLoginDate = strLoginDate;
		m_strDataUnitPK = getDataUnitPK(mainPubDataVO);
		m_bFormCanImport = getFormCanImport(repcode);
//		m_bSysFormCanImport = getSysFormCanImport();
	}

	private String getDataUnitPK(MeasurePubDataVO pubData)
	{
		String strUnitPK = null;// IUFOCacheManager.getSingleton().getUnitCache().getRootUnitInfo().getPK();
		try
		{
			KeyGroupVO keyGroup = pubData.getKeyGroup();
			if (keyGroup.getKeyByKeyPk(KeyVO.CORP_PK) != null)
			{
				return pubData.getKeywordByIndex(1);
			}
		} catch (Exception e)
		{
		}
		return strUnitPK;
	}

	protected boolean getFormCanImport(String repcode)
	{
		boolean bCanImport = true;
		report = UFOCacheManager.getSingleton().getReportCache()
				.getByCode(repcode);
		if (report != null)
			bCanImport = UFOFormulaEditControl.isFormulaEdit(report
					.getPk_report());

		return bCanImport;
	}

//	private boolean getSysFormCanImport()
//	{
//		boolean bCanImport = true;
//		try {
//			bCanImport = UfobSysParamQueryUtil.getFormCellCanImport();
//		} catch (Exception e) {
//		}
//		return bCanImport;
//	}

	/**
	 * 重新构造,此时不会改变日志中存储的内容
	 *
	 * @param repcode
	 *            String
	 * @param sheetVal
	 *            HashMap
	 * @param sheetname
	 *            String
	 * @param repname
	 *            String
	 */
	@SuppressWarnings("unchecked")
	public void reInit(String repcode, CellsModel cellsModel, String sheetname,
			int[] dynendrow)
	{
		//edit by wuyongc at 2014年4月3日16:08:52
//		this.pubDataHashKetSet =  new IntSet();
		aloneIDSet=new HashSet<String>();
		this.repcode = repcode;
		this.excelCellsModel = cellsModel;
		this.sheetName = sheetname;
		this.dynEnds = dynendrow;
		this.exMode = new int[this.dynEnds == null ? 0 : this.dynEnds.length];
		this.dynPubDatavoList = new ArrayList();
		m_bFormCanImport = getFormCanImport(repcode);
//		m_bSysFormCanImport = getSysFormCanImport();
		errCount = 0;
	}

	/**
	 * 导入数据，目前仅支持一个动态区 boolean isNeedSave 是否需要保存,如果是单表导入,则不需要保存,而是设置到
	 * m_tabUtil中，如果是多表导入，则需要保存
	 *
	 * @throws CommonException
	 * @return int 错误数量
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public int processImportData(boolean isNeedSave) throws CommonException, BusinessException
	{
		// 如果工作表或报表为空，则无法导入数据，直接返回
		if (excelCellsModel == null || report == null)
			return 0;

		this.repName = report.getChangeName();
		// 判断是否该报表上报了，如果上报了的话，则不能进行导入
		if (isCommitedOrHaveNoRight(m_strUserPK,report.getPk_report()))
			return errCount;

		UfoContextVO context = new UfoContextVO();
		context.setAttribute(ReportContextKey.REPORT_PK, report.getPk_report());
		context.setAttribute(CUR_USER_ID, report.getCreator());

		// 由于打开的是格式，所以，对格式应该有写的权限，对数据只有查看的权限
		repFormatSrv = new ReportFormatSrv(context, false);

		CellsModelOperator.getFormulaModel(repFormatSrv.getCellsModel()).setUnitID(m_strDataUnitPK);
		ExtendAreaCell[] dynAreas = repFormatSrv.getDynAreas();

		List<String> warnMsgList = new ArrayList<String>();
		// 得到主表的指标数据
		ArrayList mainListData = importMainRepData(report,warnMsgList);

		List vecDataList = null;
		// 处理动态区的指标和关键字数据
		ArrayList dynaListData = new ArrayList();

		//记录动态区重复关键字位置，动态区时间区间不在主表时间关键字区间内。
		List<String> warnList = new ArrayList<String>();
		if (dynAreas != null && dynAreas.length > 0)
		{
			dynAddRowActIndex = 0;
			for (int i=0; i<Math.min(dynAreas.length,dynEnds.length);i++) {
				if(dynAreas[i] != null){
					try{
						TwoTuple<List<MeasureDataVO>,List<String>> twoTuple = importDynRepData(dynAreas[i],i,warnMsgList);
						vecDataList = twoTuple.first;
						warnList.addAll(twoTuple.second);
						if (vecDataList != null){
							// 校验数据合法性
							checkPubDataVO(dynPubDatavoList, vecDataList);
							dynaListData.addAll(vecDataList);
						}

					} catch (CommonException ce)
					{
						addErr(ce.getMessage());
					}
				}

			}

		}
		if(!warnList.isEmpty()){
			StringBuilder sb = new StringBuilder();
			for (String string : warnList) {
				if(sb.length() == 0){
					sb.append(string);
				}else{
					sb.append("\r\n").append(string);
				}
			}
			throw new BusinessException(sb.toString());
		}
		if (errCount > 0){
			addMsg(warnMsgList);
			return errCount;
		}

		// 构造MeasurePubDataVO数组，包含动态区
		MeasurePubDataVO[] mpdatas = new MeasurePubDataVO[dynPubDatavoList
				.size() + 1];
		mpdatas[0] = mainPubDataVO;
		if (dynPubDatavoList.size() > 0)
		{
			for (int i = 0; i < dynPubDatavoList.size(); i++)
				mpdatas[i + 1] = (MeasurePubDataVO) dynPubDatavoList.get(i);
		}

		int dynListLen = 0;
		if (dynaListData != null && dynaListData.size() > 0)
			dynListLen = dynaListData.size();

		int mainDataCount = 0;
		if (mainListData != null && mainListData.size() > 0)
			mainDataCount = mainListData.size();

		int len = mainDataCount + dynListLen - 1;
		MeasureDataVO[] mdatas = new MeasureDataVO[len + 1];
		for (; len >= 0; len--)
		{
			if (len >= mainDataCount)
				mdatas[len] = (MeasureDataVO) dynaListData.get(len
						- mainDataCount);
			else
				mdatas[len] = (MeasureDataVO) mainListData.get(len);
		}

		RepDataVO repData = new RepDataVO(report.getPk_report(), report
				.getPk_key_comb());
		repData.setDatas(mpdatas, mdatas);
		repData.setUserID((String) context.getAttribute(CUR_USER_ID));

		MeasurePubDataVO[] measurePubDatas = dynPubDatavoList.toArray(new MeasurePubDataVO[0]);
		try {
			MeasurePubDataBO_Client.createFilterMeasurePubDatas(measurePubDatas);
		} catch (Exception e1) {
			AppDebug.error(e1);
		}
		// 保存指标数据
		try
		{
			@SuppressWarnings("unused")
			ReportFormatSrv reportFormatSrv = InputUtil.getReportFormatSrv(
					report.getPk_report(), null, repData.getMainPubData()
							.getAloneID(), repData.getMainPubData()
							.getAccSchemePK(), repData.getMainPubData()
							.getUnitPK(), m_strUserPK, true, false, strLoginDate);
			boolean isOnServer = context.getAttribute(ON_SERVER) == null ? false
					: Boolean.parseBoolean(context.getAttribute(ON_SERVER)
							.toString());
			if (isOnServer)
				RepDataBO_Client.createRepData(repData,
						m_strRmsPK);
			else
				RepDataBO_Client.createRepData(repData, m_strRmsPK);

			if (bAutoCalc)
			{
				ReportFormatSrv calRepFormatSrv = InputUtil.getReportFormatSrv(
						report.getPk_report(), null, repData.getMainPubData()
								.getAloneID(), repData.getMainPubData()
								.getAccSchemePK(), repData.getMainPubData()
								.getUnitPK(), m_strUserPK, true, false,
						strLoginDate);
				// liuchun 20110527 修改，数据态导入数据时，上下文中设置状态
				calRepFormatSrv.getContextVO().setAttribute(OPERATION_STATE,OPERATION_INPUT);

				// @edit by wuyongc at 2012-8-7,下午3:24:31 外部函数计算，需要数据源信息。
				calRepFormatSrv.getContextVO().setAttribute(IUfoContextKey.DATA_SOURCE, m_voDataSource);
				// end modify
				InputUtil.calculate(calRepFormatSrv, m_strUserPK, false, strLoginDate);
				calRepFormatSrv.saveReportData();
			}

			// String autoCheck =
			// SysPropMng.getSysProp(ISysProp.AUTO_CHECK).getValue();
			// if (autoCheck != null && autoCheck.equals("true")){
			// CheckResultVO[][]
			// results=CheckBO_Client.runRepCheck(strtaskId,new
			// String[]{report.getReportPK()},new
			// String[]{mainPubDataVO.getAloneID()},m_voDataSource,false,userInfo.getID(),strLoginDate,null);
			// if (results!=null){
			// for (int i=0;i<results.length;i++){
			// if (results[i]!=null)
			// m_vCheckResult.addAll(Arrays.asList(results[i]));
			// }
			// }
			// }
		} catch (Exception e)
		{
			AppDebug.debug(e);
			addErrSave();
		}
		dynAddRowActIndex = 0;
		addMsg(warnMsgList);
		return errCount;
	}

	/**
	 * 导入主表指标的数据
	 *
	 * @param sheetValue
	 *            HashMap
	 * @param measVec
	 *            Vector
	 * @param logFile
	 *            Log
	 */
	@SuppressWarnings("unchecked")
	protected ArrayList importMainRepData(ReportVO report,List<String> warnMsgList)
	{
		MeasureCache measCache = UFOCacheManager.getSingleton().getMeasureCache();
		IStoreCellPackQrySrv storeCellQrySrv = NCLocator.getInstance().lookup(IStoreCellPackQrySrv.class);
		Hashtable<String, IStoreCell> storeCells = null;
		try {
			storeCells = storeCellQrySrv.getStoreCellsByRepID(report.getPk_report());
		} catch (UFOSrvException e) {
			AppDebug.debug(e);
		}

		String[] strMeasPKs = repFormatSrv.getAllMeausrePK();
		if (strMeasPKs == null || strMeasPKs.length <= 0)
			return null;

		CellPosition maxCell = repFormatSrv.getMaxCellPosition();
		int iRowNum = maxCell.getRow() + 1;
		int iColNum = maxCell.getColumn() + 1;

		ArrayList vMeasData = new ArrayList();
		for (int iRow = 0; iRow < iRowNum; iRow++)
		{
			for (int iCol = 0; iCol < iColNum; iCol++)
			{
				CellPosition cellPos = CellPosition.getInstance(iRow, iCol);
				if (repFormatSrv.isSingleCellOrCombCellLeftTop(cellPos) == false)
					continue;

				String strMeasID = repFormatSrv.getMeasurePKByCell(cellPos);
				IStoreCell measure = measCache.getMeasure(strMeasID);
				if (measure == null){
					measure=storeCells.get(cellPos.toString());
					if (measure==null)
						continue;
				}

				if (measure.getKeyCombPK().equals(report.getPk_key_comb()) == false)
					continue;

				boolean bCanImport = m_bFormCanImport;
				com.ufsoft.table.Cell cell = repFormatSrv.getCellsModel().getCell(cellPos);

//				int iFormulaEditType = FmlCellEditUtil.getFormulaCellEditType(cell, m_strOrgPK);
//				if (iFormulaEditType == FmlCellEditUtil.FORMULA_EDIT_NO)
//				{
//					bCanImport = false;
//				} else if (iFormulaEditType == FmlCellEditUtil.FORMULA_EDIT_YES) {
//					bCanImport = true;
//				}

//				if (repFormatSrv.isHashFormulaByCell(cellPos))
				CellPosition fmtPos = DynAreaUtil.getFormatPosition(cellPos, repFormatSrv.getCellsModel());
				if (repFormatSrv.isHashFormulaByCell(cellPos) || FmlCellEditUtil.isRepitemCell(fmtPos, repFormatSrv.getCellsModel()))
				{
					bCanImport = m_bFormCanImport;
					int iFormulaEditType = FmlCellEditUtil.getFormulaCellEditType(cell, m_strOrgPK);
//					int iFormulaEditType = repFormatSrv.getFormulaCellEdit(cell);
					if (iFormulaEditType == FormulaCellEditUtil.FORMULA_EDIT_NO)
					{
						bCanImport = false;
					} else if (iFormulaEditType == FormulaCellEditUtil.FORMULA_EDIT_YES) {
						bCanImport = true;
					}

					if (bCanImport == false)
						continue;
				}

				CellPosition numRowCol = repFormatSrv.getCellRowColNum(cellPos);
				AreaPosition area = AreaPosition.getInstance(cell.getRow(),
						cellPos.getColumn(), numRowCol.getColumn(), numRowCol
								.getRow());

				area = getMainMeasureArea(area);
				String objVal = importMeasureDataByArea(area, measure,warnMsgList);

				MeasureDataVO measData = new MeasureDataVO();
				measData.setAloneID(mainPubDataVO.getAloneID());

				measData.setMeasureVO(measure);
				measData.setDataValue(StringUtils.trim(objVal));

				vMeasData.add(measData);
			}
		}
		return vMeasData;
	}

	/**
	 * 根据动态区的扩展，根据表样的位置找到经过动态区扩展后对应的区域。
	 * @edit by wuyongc at 2013-7-17,下午1:52:05
	 *
	 * @param posArea
	 * @return
	 */
	private AreaPosition getMainMeasureArea(AreaPosition posArea)
	{
		AreaPosition realAreaPos = null;

		if (dynEnds == null)
			return posArea;

		ExtendAreaCell[] dynAreas = repFormatSrv.getDynAreas();
		if (dynAreas == null || dynAreas.length <= 0)
			return posArea;

		int dynAreaSize = dynAreas.length;
		for (int i=0,max=Math.min(dynAreaSize, dynEnds.length);i<max;i++) {
			// @edit by wuyongc at 2013-7-17,下午1:51:08 ,小于零表示该动态区没有数据应该忽略当前循环，继续循环下一个动态区
			if(dynEnds[i] <0){
				continue;
			}
			ExtendAreaCell dynVO = dynAreas[i];
			AreaPosition area = DynAreaUtil.getFormatExCellByDataCell(dynVO,
					repFormatSrv.getCellsModel()).getArea();
			if(exMode[i] == ExtendAreaConstants.EX_MODE_Y){
				if (posArea.getStart().getRow() > area.getEnd().getRow())
				{
					if(posArea.getStart().getColumn()>=area.getStart().getColumn() && posArea.getStart().getColumn()<=area.getEnd().getColumn()){
						realAreaPos = getAreaPosExY(posArea, i, area);
					}else{
						int k = i+1;
						while(k<max){
							ExtendAreaCell dynVO2 = dynAreas[k];
							AreaPosition area2 = DynAreaUtil.getFormatExCellByDataCell(dynVO2,
									repFormatSrv.getCellsModel()).getArea();
							if(posArea.getStart().getColumn()>=area2.getStart().getColumn() && posArea.getStart().getColumn()<=area2.getEnd().getColumn()){
								realAreaPos = getAreaPosExY(posArea, i, area);
								break;
							}
							k++;
						}
					}

				}
			}else if(exMode[i] == ExtendAreaConstants.EX_MODE_X){
				if (posArea.getStart().getColumn() > area.getEnd().getColumn())
				{
					if(posArea.getStart().getRow()>=area.getStart().getRow() && posArea.getStart().getRow()<=area.getEnd().getRow()){
						realAreaPos = getAreaPosExX(posArea, i, area);
					}else{
						int k = i+1;
						while(k<max){
							ExtendAreaCell dynVO2 = dynAreas[k];
							AreaPosition area2 = DynAreaUtil.getFormatExCellByDataCell(dynVO2,
									repFormatSrv.getCellsModel()).getArea();
							if(posArea.getStart().getRow()>=area2.getStart().getRow() && posArea.getStart().getRow()<=area2.getEnd().getRow()){
								realAreaPos = getAreaPosExX(posArea, i, area);
								break;
							}
							k++;
						}
					}
				}
			}
		}
		return realAreaPos == null ? posArea : realAreaPos;
	}

	private AreaPosition getAreaPosExX(AreaPosition posArea, int i,
			AreaPosition area) {
		int mdOffset;
		int mOf;
		mdOffset = posArea.getStart().getColumn() - area.getEnd().getColumn();
		mOf = posArea.getEnd().getColumn() - posArea.getStart().getColumn();

		CellPosition newStart = CellPosition.getInstance(posArea.getStart().getRow(), dynEnds[i] - 1
				+ mdOffset);
		CellPosition newEnd = CellPosition.getInstance(posArea.getEnd().getRow() , newStart
				.getColumn()
				+ mOf);
		posArea = AreaPosition.getInstance(newStart, newEnd);
		return posArea;
	}

	private AreaPosition getAreaPosExY(AreaPosition posArea, int i,
			AreaPosition area) {
		int mdOffset;
		int mOf;
		mdOffset = posArea.getStart().getRow() - area.getEnd().getRow();
		mOf = posArea.getEnd().getRow() - posArea.getStart().getRow();

		CellPosition newStart = CellPosition.getInstance(dynEnds[i] - 1
				+ mdOffset, posArea.getStart().getColumn());
		CellPosition newEnd = CellPosition.getInstance(newStart
				.getRow()
				+ mOf, posArea.getEnd().getColumn());
		posArea = AreaPosition.getInstance(newStart, newEnd);
		return posArea;
	}

	/**
	 * 导入动态区指标的数据
	 *
	 * @param sheetValue
	 *            HashMap
	 * @param measVec
	 *            Vector
	 * @param logFile
	 *            Log
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	protected TwoTuple<List<MeasureDataVO>,List<String>> importDynRepData(ExtendAreaCell dynVo, int dynIndex,List<String> warnMsgList) throws BusinessException
	{
		List<String> warnList = new ArrayList<String>();
		//默认为指定了动态区结束行
		boolean bSpecifiedDynEndLine = true;
		int dynEndRow = dynEnds[dynIndex];
		// 如果错误数超过预置错误数，则直接返回，并将错误日志提示给用户
		if (errCount > SINGLE_SHEET_SER_NUM)
			return null;

		// 如果动态区结束行位置为-1或者结束行位置要小于动态区起始行位置，则直接返回null;
		AreaPosition dynArea = dynVo.getArea();

		int exMode = dynVo.getBaseInfoSet().getExMode();
		this.exMode = reSize(this.exMode,dynIndex);
		this.exMode[dynIndex] = exMode;
		if(dynEndRow == -1){//没有指定动态区结束行，设置为取最大行，然后读取动态区关键字到找不到关键字值为止时的为结束行。
			dynEndRow = getMaxData(exMode);
			bSpecifiedDynEndLine = false;
		}
//		if (dynEndRow == -1 || dynEndRow < dynArea.getStart().getRow())
		if(dynEndRow <getDynAreaStart(dynArea.getStart(), exMode))
			return null;

		MeasureModel measModel = MeasureModel.getInstance(repFormatSrv.getCellsModel());
		@SuppressWarnings("rawtypes")
		Hashtable hashDynMeas = measModel.getDynAreaMeasureVOPos(dynVo.getBaseInfoSet().getExAreaPK());
		KeywordModel keyModel = KeywordModel.getInstance(repFormatSrv.getCellsModel());


		IStoreCellPackQrySrv storeCellQrySrv = NCLocator.getInstance().lookup(IStoreCellPackQrySrv.class);
		Hashtable<String, IStoreCell> storeCells = null;
		try {
			storeCells = storeCellQrySrv.getStoreCellPackByRepKeyGroupID(report.getPk_report(), keyModel.getDynAreaKeyCombPK(dynVo.getExAreaPK()));
		} catch (UFOSrvException e1) {
			AppDebug.debug(e1);
		}
		Hashtable<CellPosition, IStoreCell> hashDynStore = new Hashtable<CellPosition, IStoreCell>();
		for (Entry<String, IStoreCell> entry : storeCells.entrySet()) {
			hashDynStore.put(CellPosition.getInstance(entry.getKey()), entry.getValue());
		}
		if ((hashDynMeas == null || hashDynMeas.size() <= 0) && (hashDynStore == null || hashDynStore.size() <= 0))
			return null;

		if (hashDynMeas==null || hashDynMeas.size()<=0)
			hashDynMeas=hashDynStore;
		else if (hashDynStore==null || hashDynStore.size()<=0)
			;
		else{
			hashDynMeas.putAll(hashDynStore);
		}

//		if (m_bSysFormCanImport == false)
//		{
			CellPosition[] allCells = (CellPosition[]) hashDynMeas.keySet()
					.toArray(new CellPosition[0]);
			for (CellPosition cellPos : allCells) {
				// @edit by wuyongc at 2014-4-30,上午10:38:25 修改单元格可编辑权限控制。
				com.ufsoft.table.Cell cell = repFormatSrv.getCellsModel().getCell(cellPos);
				boolean bCanImport = m_bFormCanImport;
//				int iFormulaEditType = FmlCellEditUtil.getFormulaCellEditType(cell, m_strOrgPK);
//				if (iFormulaEditType == FmlCellEditUtil.FORMULA_EDIT_NO)
//				{
//					bCanImport = false;
//				} else if (iFormulaEditType == FmlCellEditUtil.FORMULA_EDIT_YES) {
//					bCanImport = true;
//				}
//
//				if (!bCanImport)
//					hashDynMeas.remove(cellPos);

//				if (repFormatSrv.isHashFormulaByCell(cellPos))
				CellPosition fmtPos = DynAreaUtil.getFormatPosition(cellPos, repFormatSrv.getCellsModel());
				if (repFormatSrv.isHashFormulaByCell(cellPos) || FmlCellEditUtil.isRepitemCell(fmtPos, repFormatSrv.getCellsModel()))
				{
//					boolean bCanImport = m_bFormCanImport;
//					int iFormulaEditType = repFormatSrv.getFormulaCellEdit(cellPos);
					int iFormulaEditType = FmlCellEditUtil.getFormulaCellEditType(cell, m_strOrgPK);
					if (iFormulaEditType == FormulaCellEditUtil.FORMULA_EDIT_NO)
					{
						bCanImport = false;
					} else if (iFormulaEditType == FormulaCellEditUtil.FORMULA_EDIT_YES) {
						bCanImport = true;
					}

					if (!bCanImport)
						hashDynMeas.remove(cellPos);
				}
			}
//		}

		Hashtable<CellPosition, KeyVO> hashDynKeys = keyModel.getDynKeyVOPos(dynVo.getBaseInfoSet()
				.getExAreaPK());
		if (hashDynKeys == null || hashDynKeys.size() <= 0)
			return null;

		// 考虑动态区内的指标可能定义在多行上，需要记录一下增加一个pubdataVO数据的时候一次增加多少行,即偏移量
		// 默认为指标和关键字都在同一行上
		int dynOffset = getDynOffset(hashDynMeas, hashDynKeys,exMode);
		// int dynRowOffset=0;

		if(dynAddRowActIndex > 0){
			//第N个动态区的偏移量多1 了   (N>1)
			dynAddRowActIndex--;
		}
		int oldDynIndex = dynAddRowActIndex;
		MeasureDataVO md = null;
		MeasurePubDataVO dynPubDataVo = null;
		KeyGroupCache keyGroupCache = UFOCacheManager.getSingleton()
				.getKeyGroupCache();
		KeyGroupVO newMainKeyGroup = (KeyGroupVO) mainPubDataVO.getKeyGroup()
				.clone();

		KeyVO[] dynKeyVos = hashDynKeys.values()
				.toArray(new KeyVO[0]);

		newMainKeyGroup.addKeyToGroup(dynKeyVos);


		KeyGroupVO tempMainKeyGroup = keyGroupCache.getPkByKeyGroup(newMainKeyGroup);
		if(tempMainKeyGroup == null){
			keyGroupCache.add(newMainKeyGroup);
			tempMainKeyGroup = keyGroupCache.getByPK(newMainKeyGroup.getKeyGroupPK());
		}
		newMainKeyGroup = tempMainKeyGroup;

		ArrayList<MeasureDataVO> vecDataList = new ArrayList<MeasureDataVO>();



//		int iMaxDataRow = getMaxDataRow();
//		int iDynStartRow = dynArea.getStart().getRow();


		int iMaxData = getMaxData(exMode);
		int iDynStartRow = getDynAreaStart(dynArea.getStart(), exMode);

		iDynStartRow +=dynAddRowActIndex;
		boolean bEnd = false;
		while (iDynStartRow <= dynEndRow - 1 && iDynStartRow <= iMaxData)
		{
//			HashMap indexValueMap = new HashMap();

			dynPubDataVo = new MeasurePubDataVO();
			dynPubDataVo.setKType(newMainKeyGroup.getKeyGroupPK());
			dynPubDataVo.setKeyGroup(newMainKeyGroup);
			dynPubDataVo.setVer(mainPubDataVO.getVer());

			KeyVO[] mainKeys = mainPubDataVO.getKeyGroup().getKeys();
			if (mainKeys != null && mainKeys.length > 0)
			{
				for (KeyVO mainKey : mainKeys)
					dynPubDataVo.setKeywordByName(mainKey.getName(),
							mainPubDataVO.getKeywordByName(mainKey
									.getName()));
			}

			// 标志校验动态区的关键字输入是否合法，如果不合法，则不能将该关键字所确定的一组数据导入
			boolean checkKey = true;
			CellPosition[] cells = hashDynKeys.keySet()
					.toArray(new CellPosition[0]);
			for (CellPosition cell : cells) {
				CellPosition numRowCol = repFormatSrv.getCellRowColNum(cell);
				KeyVO key = hashDynKeys.get(cell);
				//TODO wuyongc 如果是第二个扩展区 ，可能纵向或者横向的位置要随着前面的动态区扩展，此处下一步可以继续完善。
				AreaPosition area = AreaPosition.getInstance(cell.getRow(),
						cell.getColumn(), numRowCol.getColumn(), numRowCol
								.getRow());
				AreaPosition actArea = getDynActArea(dynAddRowActIndex, area,
						dynArea,exMode);
				String value = null;
				TwoTuple<String, Boolean> twoTuple = null;
				try{
					twoTuple = importKeyDataByArea(actArea, key,warnMsgList);
					value = twoTuple.first;
					if(NOT_FIND.equals(value)){
						checkKey = false;
						break;
					}
				}catch(IllegalArgumentException e){
					throw new IllegalArgumentException(actArea.toString() + " key.getRefPK:" + key.getRef_pk() + e.getMessage(),e);
				}
				//将 "合计"这样特殊的标记识别出来，表示动态区结束了，否则在不指定动态区截止行的时候会把“合计”也当成动态区了
				// 后续待改进，如果动态区是字符关键字，给予提示。
				if(twoTuple!= null && !twoTuple.second){
//					if(!bSpecifiedDynEndLine){
						dynEnds[dynIndex] = getDynAreaStart(actArea.getStart(), exMode);
						checkKey = false;
						//其实是可能指定了结束行，但到此处认为是到动态区已经结束了.
						bEnd = true;
						if(vecDataList.isEmpty()){
							dynEnds[dynIndex]++;
						}
						break;
//					}
				}
				if (value == null || value == null
						|| value.trim().length() <= 0)
				{
					if(!bSpecifiedDynEndLine)//
						dynEnds[dynIndex] = getDynAreaStart(actArea.getStart(), exMode);
					checkKey = false;
					bEnd = true;
					break;
				}


				if (dynPubDataVo != null){
					if (StringUtils.isNotEmpty(StringUtils.trimToEmpty(value))){
						//动态区的日期范围应该在主表关键字的日期范围内.
//						value = getValidKeyTimeValue(cell, value,key.getTTimeProperty());
						if(key.isTTimeKeyVO() && !isValidTimeValue(key, value)){
							warnList.add(NCLangUtil.getStrByID("1413007_0", "01413007-1228"/*动态区时间关键字值不在主表时间关键字值的范围！*/ )+ "\r\n"+ actArea.toString() + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0253")/*@res ":【"*/ + value + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0254")/*@res "】"*/);
//							throw new BusinessException(NCLangUtil.getStrByID("1413007_0", "01413007-1228"/*动态区时间关键字值不在主表时间关键字值的范围！*/ + "\r\n")+ actArea.toString() + ":【" + value + "】");
						}
					}

					dynPubDataVo.setKeywordByName(key.getName(), value);
//					if(key.getType() == key.TYPE_CHAR){
//						if(value.getBytes().length > key.getLen()){
//							throw new BusinessException(actArea.toString() + ":【" + value + "】的长度超过了关键字" + key.toString() + "的录入限定长度" + key.getLen() );
//						}
//					}

				}
			}

			String strAloneID = mainPubDataVO.getAloneID();
			if (dynPubDataVo != null){
				strAloneID = MeasureDataUtil.getAloneID(dynPubDataVo);
				// @edit by wuyongc at 2014-1-2,上午9:59:16 在数据量极大的时候提高效率
				if(aloneIDSet.contains(strAloneID)){
//				if(dynPubDatavoList.contains(dynPubDataVo)){
//					String keyVals = KeyDetailDataUtil.getKeygroupValue(dynPubDataVo.getKeyGroup().getKeys(), dynPubDataVo.getKeywords(), accschemePk);
					warnList.add(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0255")/*@res "动态区关键字组合值不能重复！\r\n"*/ + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820999-0191")/*@res "行"*/ + iDynStartRow);
//					throw new BusinessException("动态区关键字组合值不能重复！\r\n" + actArea.toString() + ":【" + value + "】");
				}
			}

			if (checkKey)
			{
				try
				{
					if (dynPubDataVo != null)
					{

//						strAloneID = MeasurePubDataBO_Client
//								.getAloneID(dynPubDataVo);

//						strAloneID = MeasureDataUtil.getAloneID(dynPubDataVo);

						dynPubDataVo.setAloneID(strAloneID);
						dynPubDatavoList.add(dynPubDataVo);
						aloneIDSet.add(strAloneID);
//						pubDataHashKetSet.add(dynPubDataVo.hashCode());
					}
				} catch (Exception e)
				{
				}

				// 关键字校验成功才允许进行指标数据的导入
				cells = (CellPosition[]) hashDynMeas.keySet().toArray(
						new CellPosition[0]);
				// 横向扩展的时候  步长应该加上指标数据的个数
//				if(exMode == ExtendAreaConstants.EX_MODE_X)
//					cellsSize = cells.length;
				for (CellPosition cell : cells) {
					CellPosition numRowCol = repFormatSrv
							.getCellRowColNum(cell);
					IStoreCell measure = (IStoreCell) hashDynMeas.get(cell);

					AreaPosition area = AreaPosition.getInstance(cell.getRow(),
							cell.getColumn(), numRowCol.getColumn(), numRowCol
									.getRow());
					//TODO
					AreaPosition actArea = getDynActArea(dynAddRowActIndex,
							area, dynArea,exMode);

					// 控制导入时按照用户设定的值导入
//					if (actArea.getStart().getRow() > dynEndRow)
//						continue;
					if(getDynAreaStart(actArea.getStart(), exMode)>dynEndRow)
						continue;

					String objVal = importMeasureDataByArea(actArea, measure,warnMsgList);
					if (objVal == null)
						continue;

					md = new MeasureDataVO();
					md.setAloneID(strAloneID);

					md.setMeasureVO(measure);
					// if(isHavePriKey)
					// //动态区为私有关键字时，要设置行号
					// md.setRowNo(iDynRowNo);
					// else
					// 动态区为公有关键字时，行号设为0
					md.setDataValue(StringUtils.trim(objVal));
					vecDataList.add(md);
				}
			}else if(bEnd)
				break;
			dynAddRowActIndex = dynAddRowActIndex+ dynOffset;
			iDynStartRow = iDynStartRow + dynOffset;
		}
		//如果当前的动态区为空，则加一个，因为到下一个动态区 会减 1
		if(dynAddRowActIndex == oldDynIndex){
			dynAddRowActIndex++;
		}
		TwoTuple<List<MeasureDataVO>,List<String>> twoTuple = new TwoTuple<List<MeasureDataVO>, List<String>>(vecDataList, warnList);
		return twoTuple;
	}


	/**
	 * @create by wuyongc at 2013-1-22,下午1:57:18
	 *
	 * @param exMode2
	 * @param dynIndex
	 */
	private int[] reSize(int[] exMode, int dynIndex) {
		int[] newExMode = null;
		if(exMode.length<dynIndex+1){
			newExMode = new int[dynIndex+1];
			System.arraycopy(exMode, 0, newExMode, 0, exMode.length);
		}else{
			return exMode;
		}
		return newExMode;

	}

	/**
	 * @create by wuyongc at 2012-2-29,下午2:38:19
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	private boolean isValidTimeValue(KeyVO key, String value) {
		KeyVO mainTimeKey= mainPubDataVO.getKeyGroup().getTimeKey();
		String mainTableTimePeriod = InputKeywordsUtil.getTimePeriodByKey(mainTimeKey);
		String mainTableTimeValue =(mainTableTimePeriod!=null)?mainPubDataVO.getKeywordByPK(mainTimeKey.getPk_keyword()):null;

		return InputKeywordsUtil.isValidTimeValue(mainTimeKey, key.getTTimeProperty(), mainTableTimeValue, value, mainPubDataVO.getAccSchemePK());
	}

	// @edit by wuyongc at 2012-2-28,下午4:25:26 修改为取纵向扩展的动态区的指标的位置
	/**
	 *
	 *
	 * 转换指标或关键字sheet中对应数据的实际位置，包括增加一行数据后指标或关键字对应的新位置
	 *
	 * @param dynAddRowActIndex
	 *            int
	 * @param posArea
	 *            AreaPosition
	 * @param dynArea
	 *            AreaPosition
	 * @return AreaPosition
	 */
	private AreaPosition getDynActAreaExY(int dynAddRowActIndex,
			AreaPosition posArea, AreaPosition dynArea)
	{
		// 目前仅支持行扩展
		int iStartRow = dynAddRowActIndex + posArea.getStart().getRow();
		int iStartCol = posArea.getStart().getColumn();
		int iEndRow = dynAddRowActIndex + posArea.getEnd().getRow();
		int iEndCol = posArea.getEnd().getColumn();
		return AreaPosition.getInstance(CellPosition.getInstance(iStartRow,
				iStartCol), CellPosition.getInstance(iEndRow, iEndCol));
	}

	/**
	 * @create by wuyongc at 2012-2-28,下午4:25:56
	 *
	 */
	private AreaPosition getDynActArea(int dynAddRowActIndex,
			AreaPosition posArea, AreaPosition dynArea, int exMode)	{
		if(exMode == ExtendAreaConstants.EX_MODE_Y){
			return getDynActAreaExY(dynAddRowActIndex, posArea, dynArea);
		}else if(exMode == ExtendAreaConstants.EX_MODE_X){
			return getDynActAreaExX(dynAddRowActIndex, posArea, dynArea);
		}else{
			return getDynActAreaExY(dynAddRowActIndex, posArea, dynArea);
		}
	}

	/**
	 * 取得横向扩展动态区的指标位置
	 * @create by wuyongc at 2012-2-28,下午4:27:30
	 *
	 * @param dynAddRowActIndex
	 * @param posArea
	 * @param dynArea
	 */
	private AreaPosition getDynActAreaExX(int dynAddRowActIndex, AreaPosition posArea,
			AreaPosition dynArea) {

					int iStartRow =  posArea.getStart().getRow();
					int iStartCol = posArea.getStart().getColumn() + dynAddRowActIndex;
					int iEndRow =  posArea.getEnd().getRow();
					int iEndCol = posArea.getEnd().getColumn() + dynAddRowActIndex;
					return AreaPosition.getInstance(CellPosition.getInstance(iStartRow,
							iStartCol), CellPosition.getInstance(iEndRow, iEndCol));

	}

	/**
	 * 计算动态区增加一组数据应该要增加的实际行数，用来在动态区定位指标或关键字取相应位置的值
	 *
	 * @param measVec
	 *            Vector
	 * @param keywordVec
	 *            Vector
	 * @return int
	 */
	@SuppressWarnings("unchecked")
	private int getDynOffsetRows(Hashtable hashMeas, Hashtable hashKey)
	{
		int startRow = -1, endRow = -1;

		ArrayList vCell = new ArrayList();
		vCell.addAll(hashMeas.keySet());
		vCell.addAll(hashKey.keySet());

		for (int i = 0; i < vCell.size(); i++)
		{
			CellPosition cell = (CellPosition) vCell.get(i);
			CellPosition numRowCol = repFormatSrv.getCellRowColNum(cell);
			AreaPosition area = AreaPosition.getInstance(cell.getRow(), cell
					.getColumn(), numRowCol.getColumn(), numRowCol.getRow());

			if (startRow < 0 || area.getStart().getRow() < startRow)
				startRow = area.getStart().getRow();
			if (endRow < 0 || area.getEnd().getRow() > endRow)
				endRow = area.getEnd().getRow();
		}

		return endRow - startRow + 1;
	}

	/**
	 * 计算动态区增加一组数据应该增加的行数或者列数
	 * (如果动态区的扩展方向是纵向,为行数,扩展方向是横向,则为列数)
	 * @create by wuyongc at 2012-2-28,下午5:26:23
	 *
	 * @param exMode
	 * @return
	 */
	private int getDynOffset(Hashtable hashMeas, Hashtable hashKey,int exMode){
		if(exMode == ExtendAreaConstants.EX_MODE_X){
			return getDynOffsetColumns(hashMeas, hashKey);
		}else if(exMode == ExtendAreaConstants.EX_MODE_Y){
			return getDynOffsetRows(hashMeas, hashKey);
		}else{
			return getDynOffsetRows(hashMeas, hashKey);
		}

	}

	/**
	 * @create by wuyongc at 2012-2-28,下午5:29:37
	 *
	 * @param hashMeas
	 * @param hashKey
	 * @return
	 */
	private int getDynOffsetColumns(Hashtable hashMeas, Hashtable hashKey) {
		int startColumn = -1, endColumn = -1;

		ArrayList vCell = new ArrayList();
		vCell.addAll(hashMeas.keySet());
		vCell.addAll(hashKey.keySet());
		//TODO

		for (int i = 0; i < vCell.size(); i++)
		{
			CellPosition cell = (CellPosition) vCell.get(i);
			CellPosition numRowCol = repFormatSrv.getCellRowColNum(cell);
			AreaPosition area = AreaPosition.getInstance(cell.getRow(), cell
					.getColumn(), numRowCol.getColumn(), numRowCol.getRow());

			if (startColumn < 0 || area.getStart().getColumn() < startColumn)
				startColumn = area.getStart().getColumn();
			if (endColumn < 0 || area.getEnd().getColumn() > endColumn)
				endColumn = area.getEnd().getColumn();
		}

		return endColumn - startColumn+1;
	}

	/**
	 * @param area
	 * @param kvo
	 * @return
	 */
	private TwoTuple<String, Boolean> importKeyDataByArea(AreaPosition area, KeyVO kvo,List<String> warnMsgList)
	{

		Object objVal = excelCellsModel.getCellValue(area.getStart());
		String contentValue = null;
		//第二个参数布尔值表示 该值是否是关键字， 大部分情况下是，特殊情况如 “合计” 这样的认为不是关键字。
		TwoTuple<String, Boolean> twoTuple = null;
		// 如果当前单元没有值，则继续进行下一个单元的取值
		String val = objVal == null ? null : objVal.toString();
		if (val == null || val.trim().isEmpty())
			return new TwoTuple<String, Boolean>(null,false);;
		// @edit by wuyongc at 2013-9-11,下午1:41:10 因港华数据后边带有空格，根据dongjw，xiaomenga商议后决定对后边进行trim
		val = UfoStringUtils.trimRight(val);
		if (kvo.getType() == KeyVO.TYPE_TIME || kvo.getType() == KeyVO.TYPE_ACC)
		{
			// 对自然时间类型关键字的导入,要校验日期的合法性，
			contentValue = checkKeyDateStr(val, kvo, area);
		} else if (kvo.getType() == KeyVO.TYPE_REF)
		{
			contentValue = checkRefDataStr(val, kvo.getRef_pk(),
					area);
			if(contentValue == null){
				if(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0256")/*@res "合计"*/.equals(val)){
					twoTuple = new TwoTuple(val,false);
					return twoTuple;
				}
				String warnMsg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0257")/*@res "excel单元格："*/ + area.toString() + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0258")/*@res "的值【"*/ + val + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0259")/*@res "】不存在于关键字【"*/ + kvo.toString() + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0260")/*@res "】所参照的基础档案或者自定义档案中"*/;
				warnMsgList.add(warnMsg);
			}
		} else
		{
			// 对时间类型关键字的导入,要校验日期的合法性，
			contentValue = checkKeyCharStr(kvo, val, area);
		}
		twoTuple = new TwoTuple(contentValue == null ? NOT_FIND : contentValue,true);
		return twoTuple;

	}
	/**
	 *  @edit by wuyongc at 2014-1-13,下午3:20:18 导出的参照类型可能是名称也可能是编码，但是大部分情况应该是统一的，要么是编码或者要么是名称
	 *  此处引入一个变量来尝试着判断是否是编码，如果第一次能根据编码查询到，则认为Excel里的参照类型是以编码导出的，否则是以名称导出的
	 *
	 *  避免大数据量的导入的时候，找不到缓存，不断访问数据库的情况出现。
	 */

	private String checkRefDataStr(String strValue, String codeId,AreaPosition area){
		if(StringUtils.isEmpty(codeId)){
			return null;
		}
		IGeneralAccessor accessor = GeneralAccessorFactory.getAccessor(codeId);
		String strOrgPK = mainPubDataVO.getUnitPK();
		if (strOrgPK == null || strOrgPK.length() <= 0)
			strOrgPK = m_strOrgPK;

		if(bQryAsCode){
			IBDData data = accessor.getDocByCode(strOrgPK, strValue);
			if (data == null){
				data = accessor.getDocByNameWithMainLang(strOrgPK, strValue);
				if(data != null){
					bQryAsCode = false;
					return	 data.getPk();
					
				}
				return null;
			}else{
				return  data.getPk();
			}
		}else{
			IBDData data = accessor.getDocByNameWithMainLang(strOrgPK, strValue);
			if (data == null){
				data = accessor.getDocByCode(strOrgPK, strValue);
				if(data != null){
					bQryAsCode = true;
					return  data.getPk();
				}
				return null;
			}else{
				return   data.getPk();
			}
		}

//		IBDData data = accessor.getDocByCode(strOrgPK, strValue);
//		if (data == null)
//			data = accessor.getDocByNameWithMainLang(strOrgPK, strValue);
//
//		if (data == null){
////			addErrData(area.toString());
//			return null;
//		} else
//			return data.getPk();
////			return data.getCode();
	}

	private String checkRefMeasureDataStr(String strValue, String codeId,AreaPosition area){
		if(StringUtils.isEmpty(codeId)){
			return "";
		}
		IGeneralAccessor accessor = GeneralAccessorFactory.getAccessor(codeId);
		String strOrgPK = mainPubDataVO.getUnitPK();
		if (strOrgPK == null || strOrgPK.length() <= 0)
			strOrgPK = m_strOrgPK;
		IBDData data = null;
		if(bQryAsCode){
			data = accessor.getDocByCode(strOrgPK, strValue);
			if (data == null){
				data = accessor.getDocByNameWithMainLang(strOrgPK, strValue);
				if(data != null){
					bQryAsCode = false;
				}
			}
		}else{
			data =  accessor.getDocByNameWithMainLang(strOrgPK, strValue);
			if (data == null){
				data = accessor.getDocByCode(strOrgPK, strValue);
				if(data != null){
					bQryAsCode = true;
				}
			}
		}

		if (data == null){
//			addErrData(area.toString());
			return null;
		} else
//			return data.getPk();
			return data.getCode();
	}

	/**
	 * 校验时间关键字的录入
	 *
	 * @param strValue
	 *            String
	 * @param timeProp
	 *            String
	 * @param area
	 *            AreaPosition
	 * @return String
	 */
	protected String checkKeyDateStr(String strValue, KeyVO key,
			AreaPosition area)
	{
		if (strValue != null && strValue.length() > 0)
		{
			if (key.isAccPeriodKey())
			{
				AccPeriodSchemeUtil util = AccPeriodSchemeUtil.getInstance();
				String[] strNatDates = util.getNatDateByAccPeriod(mainPubDataVO
						.getAccSchemePK(), key.getPk_keyword(), strValue);
				if (strNatDates == null || strNatDates.length < 1
						|| strNatDates[0] == null)
				{
					addErrDataTime(area.toString(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1060")/*@res "会计期间不合法"*/);
					return null;
				}
			} else
			{
				if (UFODate.isAllowDate(strValue) == false)
				{
					addErrDataTime(area.toString(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1061")/*@res "日期不合法"*/); // "日期不合法"
					return null;
				}
				UFODate ufoDate = new nc.vo.iufo.pub.date.UFODate(strValue);
				String strInputDate = ufoDate.getEndDay(key.getTimeProperty())
						.toString();
				strValue = strInputDate;
			}
		}
		return strValue;
	}

	/**
	 * 校验单位关键字
	 *
	 * @param kvo
	 *            KeyVO
	 * @param strValue
	 *            String
	 * @param area
	 *            AreaPosition
	 * @return String
	 */
	private String checkKeyCharStr(KeyVO kvo, String strValue, AreaPosition area)
	{
		if (strValue == null || strValue.length() <= 0)
			return null;

		// 主要校验单位合法性
		if (kvo.getPk_keyword().equals(KeyVO.CORP_PK)
				|| kvo.getPk_keyword().equals(KeyVO.DIC_CORP_PK))
		{
			String strOrgPK = checkRefDataStr(strValue, OrgUtil.ORGCACHENAME,
					area);
			if (strOrgPK == null)
				return null;

			if (m_strRmsPK == null)
				return strOrgPK;

			String strParentOrgPK = mainPubDataVO.getUnitPK();
			if (strParentOrgPK == null || strParentOrgPK.length() <= 0)
				strParentOrgPK = m_strOrgPK;

			if (kvo.getPk_keyword().equals(KeyVO.DIC_CORP_PK))
				strParentOrgPK = null;

			// 判断该组织是否在当前组织结构中，并为数据单位的下级
			// 走IUFO做的方法，在大批量操作时，EJB调用过多，以后如果NC提供缓存，改用NC的方法
			ICorpQuerySrv service = NCLocator.getInstance()
					.lookup(ICorpQuerySrv.class);
			boolean bValid = false;
			try
			{
				bValid = service.isOrgMemberInReportManaStru(m_strRmsPK,
						strParentOrgPK, strOrgPK);
			} catch (Exception e)
			{
				AppDebug.debug(e);
				bValid = true;
			}
			if (bValid == false)
			{
				addErrDataTime(area.toString(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1062")/*@res "组织编码为 ["*/ + strValue
						+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1063")/*@res "] 的组织不在当前报表组织结构中或不是报表数据组织的下级组织"*/);
				return null;
			}

			return strOrgPK;
		} else
			return strValue;
	}

	/**
	 * 根据位置和指标的类型得到录入的值
	 *
	 * @param area
	 *            AreaPosition
	 * @param mvo
	 *            MeasureTableVO
	 * @return String
	 */
	private String importMeasureDataByArea(AreaPosition area, IStoreCell measure,List<String>warnMsgList)
	{
		Object objVal = excelCellsModel.getCellValue(area.getStart());

		// 如果当前单元没有值，则继续进行下一个单元的取值
		if (objVal == null)
			return null;
		// @edit by wuyongc at 2013-9-11,下午1:43:58 对后边空格trim
		String content = UfoStringUtils.trimRight(objVal.toString());
		if (measure.getType() == IStoreCell.TYPE_NUMBER && content != null
				&& content.trim().length() > 0)
		{
			// 对数值型指标的导入,如果Double.parseDouble出错，则说明类型不符
			try
			{
				Double.parseDouble(content);
			} catch (Exception e)
			{
				addErrDataType(area.toString(), MEAS_TYPE_NUM);
				return null;
			}
			content = objVal.toString();
		} else if(measure.getType() == IStoreCell.TYPE_BIGDECIMAL && content != null
				&& content.trim().length() > 0) {
			content = objVal.toString();
		}
		else if (measure instanceof MeasureVO){
			MeasureVO meas=(MeasureVO)measure;
			if (meas.getType() == MeasureVO.TYPE_CODE){
				if(meas.getRefPK() == null){
					return null;
				}
				try{
					content = checkRefMeasureDataStr(content, meas.getRefPK(), area);
				}catch(IllegalArgumentException e){
					warnMsgList.add(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0261")/*@res "指标参照的基础档案或者自定义档案不存在，指标的参照pk："*/ + meas.getRefPK());
					AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820999-0192")/*@res "指标参照的基础档案或者自定义档案不存在，指标的参照pk："*/ + meas.getRefPK());
					AppDebug.debug(e);
					return null;
				}
			}else if(meas.getType() == MeasureVO.TYPE_DATE){
				// @edit by wuyongc at 2014-1-10,下午2:08:27 对于日期类型的数据，解析一下，因为目前NC仅支持“-” 连接的日期格式。
				content = UFDateParseUtil.parseDate(content);
			}
		}

		// 对于字符型指标，由于字符限制比较少，则直接处理
		return content;
	}

	/**
	 * 写错误日志,相应的位置上没有相应的指定的类型的数据,或类型也不相符合
	 *
	 * @param area
	 *            String
	 * @param repType
	 *            String
	 */
	private void addErrDataType(String area, String repType)
	{
		errCount += 1;
		// "IUFO报表："+repName+"和匹配的Excel工作表："+sheetName+"中的相应位置 "+area+" 上数据类型不符!需要\""+repType+"\"类型"
		logFile.writer(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0590")/*@res "IUFO报表"*/
				+ // "IUFO报表"
				nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0109")/*@res "："*/ + repName
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1064")/*@res "和匹配的Excel工作表"*/
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0109")/*@res "："*/
				+ sheetName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1065")/*@res "中的相应位置"*/
				+ " " + area + " "
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1066")/*@res "上数据类型不符!需要"*/ + "\""
				+ repType + "\""
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0365")/*@res "类型"*/); // "类型"
	}

	/**
	 * 写错误日志,相应的位置上没有相应的指定的类型的数据,或类型也不相符合，主要指关键字的日期不合法
	 *
	 * @param area
	 *            String
	 * @param repType
	 *            String
	 */
	protected void addErrDataTime(String area, String errMsg)
	{
		errCount += 1;
		// "IUFO报表："+repName+"和匹配的Excel工作表："+sheetName+"中的相应位置 "+area+" 上关键字录入错误："+errMsg+"!"
		logFile.writer(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0590")/*@res "IUFO报表"*/
				+ // "IUFO报表"
				nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0109")/*@res "："*/ + repName
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1064")/*@res "和匹配的Excel工作表"*/
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0109")/*@res "："*/
				+ sheetName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1065")/*@res "中的相应位置"*/
				+ " " + area + " "
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1067")/*@res "上关键字录入错误"*/
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0109")/*@res "："*/ + errMsg
				+ "!");
	}

	protected void addMsg(List<String> msgList){
		if(!msgList.isEmpty()){
			StringBuilder sb = new StringBuilder();
			for (String msg : msgList) {
				if(sb.length() == 0){
					sb.append(msg);
				}else{
					sb.append("\r\n").append(msg);
				}
			}
			if(sb.length()!=0){
				getLog().writer(sb.toString());
			}
		}
	}
	protected void addErrSave()
	{
		errCount += 1;
		// "保存报表"+repName+"("+repCode+")"+"的指标数据时出错！"
		logFile.writer(
				NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0", "miufo1002770", null, new String[] { repName, repcode }));
		// + //"保存报表"
		// repName+"("+repCode+")"+"保存报表{0}({1})的指标数据时出错！");
	}

	protected void addErrCommited()
	{
		errCount += 1;
		// "报表"+repName+"("+repCode+")已经上报，不能再修改该报表的数据!"
		logFile.writer(
				NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0", "miufo1002771", null, new String[] { repName, repcode }));
	}

	protected void addErr(String errStr)
	{
		errCount += 1;
		logFile.writer(errStr);
	}

	public Log getLog()
	{
		return logFile;
	}

	/**
	 * 判断报表是否已经上报，如果已经上报，则不能进行导入数据
	 *
	 * @return boolean
	 */
	protected boolean isCommitedOrHaveNoRight(String strUserPK,String repPk)
	{
		return false;
	}

	/**
	 * 如果需要对导入的数据进行校验，则通过继承本类，并实现该方法来进行校验
	 *
	 * @param pubDataVOList
	 *            ArrayList
	 * @param measureDataList
	 *            ArrayList
	 * @throws CommonException
	 */
	@SuppressWarnings("unchecked")
	protected void checkPubDataVO(ArrayList pubDataVOList,
			List measureDataList) throws CommonException
	{
	}

	protected boolean isNeedFilterByDataRight()
	{
		return true;
	}

	protected int getMaxData(int exMode){
		if(exMode == ExtendAreaConstants.EX_MODE_Y){
			return getMaxDataRow();
		}else if(exMode == ExtendAreaConstants.EX_MODE_X){
			return getMaxDataColumn();
		}else if(exMode == ExtendAreaConstants.EX_MODE_XY){
			//目前还不支持此模式
			return 0;
		}
		return 0;
	}

	/**
	 *
	 * @create by wuyongc at 2012-2-28,下午3:53:01
	 *
	 * @param cellPosition
	 * @return
	 */
	private int getDynAreaStart(CellPosition cellPosition,int exMode){
		if(exMode == ExtendAreaConstants.EX_MODE_Y){
			return cellPosition.getRow();
		}else if(exMode == ExtendAreaConstants.EX_MODE_X){
			return cellPosition.getColumn();
		}else if(exMode == ExtendAreaConstants.EX_MODE_XY){
			//目前还不支持此模式
			return 0;
		}
		return 0;
	}

	protected int getMaxDataRow()
	{
		return excelCellsModel.getMaxRow();
	}

	protected int getMaxDataColumn(){
		return excelCellsModel.getMaxCol();
	}

	@SuppressWarnings("unchecked")
	public CheckResultVO[] getCheckResults()
	{
		return (CheckResultVO[]) m_vCheckResult.toArray(new CheckResultVO[0]);
	}

	public void setCheckResults(CheckResultVO[] results)
	{
		m_vCheckResult = new Vector<CheckResultVO>(Arrays.asList(results));
	}
}