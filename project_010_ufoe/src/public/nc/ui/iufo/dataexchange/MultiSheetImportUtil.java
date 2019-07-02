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
 * @update 2004-08-25 whtao �޸Ĵ���1���޷����붯̬�������й̶�����ָ�����ݣ��޷����붯̬�����е�λ���Ƶı������ݡ�
 *         2�����в������͵�ָ���ؼ��֣�����������Сд��Ӣ�ģ��޷�����
 *         ����ԭ���ǣ�1���Զ�̬���µĹ̶�����λ�ü���������;2���Բ������͵����ƱȽϴ���bug
 * @end * @update 2004-08-07 whtao �����̬���·���������ָ����������������ʱ�ڶ��е������޷�����Ĵ���
 *      ԭ�����ڼ���ʵ������ָ��λ��ʱ�������󣬵���û�ж�λ����ȷ��Excelλ��
 * @end * @update 2004-07-26 whtao �����̬��Ϊ���йؼ���ʱ��д����lineNo�Ĵ���
 * @end
 *
 * @update 2004-07-26 whtao �������ֻ��һ��ָ������ʱ���뱨��Ĵ���
 * @end <p>
 *      Title: ���ҳExcel���ݵ���ִ��
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
	protected int[] dynEnds;// ��̬��������/��λ�ã���λ����ʵ��λ��

	protected int[] exMode;//TODO

	protected int errCount = 0;// ��ǰ�����еĴ����������������SINGLE_SHEET_SER_NUM����ñ����е����ݲ�������
	private final Log logFile = new Log();
	@SuppressWarnings("unchecked")
	private Vector m_vCheckResult = new Vector();
	protected MeasurePubDataVO mainPubDataVO = null;
	@SuppressWarnings("unchecked")
	protected ArrayList<MeasurePubDataVO>dynPubDatavoList = null;
	public static final int SINGLE_SHEET_SER_NUM = 10;// Ԥ�ô�����
	private static final String MEAS_TYPE_NUM = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0368")/*@res "��ֵ"*/; // "��ֵ"
	protected boolean bAutoCalc = false;
	protected String strLoginDate = null;
	protected int dynAddRowActIndex = 0;

	protected ReportVO report = null;

	//edit by wuyongc at 2014��4��3��16:08:26
//	private IntSet pubDataHashKetSet = null;
	private Set<String> aloneIDSet=null;

	//���ձ����ѯ�Զ��������������ݡ�
	private boolean bQryAsCode = true;
	/**
	 * ������,��ʱ�����־�д洢������
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
	 * ���¹���,��ʱ����ı���־�д洢������
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
		//edit by wuyongc at 2014��4��3��16:08:52
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
	 * �������ݣ�Ŀǰ��֧��һ����̬�� boolean isNeedSave �Ƿ���Ҫ����,����ǵ�����,����Ҫ����,�������õ�
	 * m_tabUtil�У�����Ƕ���룬����Ҫ����
	 *
	 * @throws CommonException
	 * @return int ��������
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public int processImportData(boolean isNeedSave) throws CommonException, BusinessException
	{
		// ���������򱨱�Ϊ�գ����޷��������ݣ�ֱ�ӷ���
		if (excelCellsModel == null || report == null)
			return 0;

		this.repName = report.getChangeName();
		// �ж��Ƿ�ñ����ϱ��ˣ�����ϱ��˵Ļ������ܽ��е���
		if (isCommitedOrHaveNoRight(m_strUserPK,report.getPk_report()))
			return errCount;

		UfoContextVO context = new UfoContextVO();
		context.setAttribute(ReportContextKey.REPORT_PK, report.getPk_report());
		context.setAttribute(CUR_USER_ID, report.getCreator());

		// ���ڴ򿪵��Ǹ�ʽ�����ԣ��Ը�ʽӦ����д��Ȩ�ޣ�������ֻ�в鿴��Ȩ��
		repFormatSrv = new ReportFormatSrv(context, false);

		CellsModelOperator.getFormulaModel(repFormatSrv.getCellsModel()).setUnitID(m_strDataUnitPK);
		ExtendAreaCell[] dynAreas = repFormatSrv.getDynAreas();

		List<String> warnMsgList = new ArrayList<String>();
		// �õ������ָ������
		ArrayList mainListData = importMainRepData(report,warnMsgList);

		List vecDataList = null;
		// ����̬����ָ��͹ؼ�������
		ArrayList dynaListData = new ArrayList();

		//��¼��̬���ظ��ؼ���λ�ã���̬��ʱ�����䲻������ʱ��ؼ��������ڡ�
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
							// У�����ݺϷ���
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

		// ����MeasurePubDataVO���飬������̬��
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
		// ����ָ������
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
				// liuchun 20110527 �޸ģ�����̬��������ʱ��������������״̬
				calRepFormatSrv.getContextVO().setAttribute(OPERATION_STATE,OPERATION_INPUT);

				// @edit by wuyongc at 2012-8-7,����3:24:31 �ⲿ�������㣬��Ҫ����Դ��Ϣ��
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
	 * ��������ָ�������
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
	 * ���ݶ�̬������չ�����ݱ�����λ���ҵ�������̬����չ���Ӧ������
	 * @edit by wuyongc at 2013-7-17,����1:52:05
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
			// @edit by wuyongc at 2013-7-17,����1:51:08 ,С�����ʾ�ö�̬��û������Ӧ�ú��Ե�ǰѭ��������ѭ����һ����̬��
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
	 * ���붯̬��ָ�������
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
		//Ĭ��Ϊָ���˶�̬��������
		boolean bSpecifiedDynEndLine = true;
		int dynEndRow = dynEnds[dynIndex];
		// �������������Ԥ�ô���������ֱ�ӷ��أ�����������־��ʾ���û�
		if (errCount > SINGLE_SHEET_SER_NUM)
			return null;

		// �����̬��������λ��Ϊ-1���߽�����λ��ҪС�ڶ�̬����ʼ��λ�ã���ֱ�ӷ���null;
		AreaPosition dynArea = dynVo.getArea();

		int exMode = dynVo.getBaseInfoSet().getExMode();
		this.exMode = reSize(this.exMode,dynIndex);
		this.exMode[dynIndex] = exMode;
		if(dynEndRow == -1){//û��ָ����̬�������У�����Ϊȡ����У�Ȼ���ȡ��̬���ؼ��ֵ��Ҳ����ؼ���ֵΪֹʱ��Ϊ�����С�
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
				// @edit by wuyongc at 2014-4-30,����10:38:25 �޸ĵ�Ԫ��ɱ༭Ȩ�޿��ơ�
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

		// ���Ƕ�̬���ڵ�ָ����ܶ����ڶ����ϣ���Ҫ��¼һ������һ��pubdataVO���ݵ�ʱ��һ�����Ӷ�����,��ƫ����
		// Ĭ��Ϊָ��͹ؼ��ֶ���ͬһ����
		int dynOffset = getDynOffset(hashDynMeas, hashDynKeys,exMode);
		// int dynRowOffset=0;

		if(dynAddRowActIndex > 0){
			//��N����̬����ƫ������1 ��   (N>1)
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

			// ��־У�鶯̬���Ĺؼ��������Ƿ�Ϸ���������Ϸ������ܽ��ùؼ�����ȷ����һ�����ݵ���
			boolean checkKey = true;
			CellPosition[] cells = hashDynKeys.keySet()
					.toArray(new CellPosition[0]);
			for (CellPosition cell : cells) {
				CellPosition numRowCol = repFormatSrv.getCellRowColNum(cell);
				KeyVO key = hashDynKeys.get(cell);
				//TODO wuyongc ����ǵڶ�����չ�� ������������ߺ����λ��Ҫ����ǰ��Ķ�̬����չ���˴���һ�����Լ������ơ�
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
				//�� "�ϼ�"��������ı��ʶ���������ʾ��̬�������ˣ������ڲ�ָ����̬����ֹ�е�ʱ���ѡ��ϼơ�Ҳ���ɶ�̬����
				// �������Ľ��������̬�����ַ��ؼ��֣�������ʾ��
				if(twoTuple!= null && !twoTuple.second){
//					if(!bSpecifiedDynEndLine){
						dynEnds[dynIndex] = getDynAreaStart(actArea.getStart(), exMode);
						checkKey = false;
						//��ʵ�ǿ���ָ���˽����У������˴���Ϊ�ǵ���̬���Ѿ�������.
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
						//��̬�������ڷ�ΧӦ��������ؼ��ֵ����ڷ�Χ��.
//						value = getValidKeyTimeValue(cell, value,key.getTTimeProperty());
						if(key.isTTimeKeyVO() && !isValidTimeValue(key, value)){
							warnList.add(NCLangUtil.getStrByID("1413007_0", "01413007-1228"/*��̬��ʱ��ؼ���ֵ��������ʱ��ؼ���ֵ�ķ�Χ��*/ )+ "\r\n"+ actArea.toString() + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0253")/*@res ":��"*/ + value + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0254")/*@res "��"*/);
//							throw new BusinessException(NCLangUtil.getStrByID("1413007_0", "01413007-1228"/*��̬��ʱ��ؼ���ֵ��������ʱ��ؼ���ֵ�ķ�Χ��*/ + "\r\n")+ actArea.toString() + ":��" + value + "��");
						}
					}

					dynPubDataVo.setKeywordByName(key.getName(), value);
//					if(key.getType() == key.TYPE_CHAR){
//						if(value.getBytes().length > key.getLen()){
//							throw new BusinessException(actArea.toString() + ":��" + value + "���ĳ��ȳ����˹ؼ���" + key.toString() + "��¼���޶�����" + key.getLen() );
//						}
//					}

				}
			}

			String strAloneID = mainPubDataVO.getAloneID();
			if (dynPubDataVo != null){
				strAloneID = MeasureDataUtil.getAloneID(dynPubDataVo);
				// @edit by wuyongc at 2014-1-2,����9:59:16 �������������ʱ�����Ч��
				if(aloneIDSet.contains(strAloneID)){
//				if(dynPubDatavoList.contains(dynPubDataVo)){
//					String keyVals = KeyDetailDataUtil.getKeygroupValue(dynPubDataVo.getKeyGroup().getKeys(), dynPubDataVo.getKeywords(), accschemePk);
					warnList.add(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0255")/*@res "��̬���ؼ������ֵ�����ظ���\r\n"*/ + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820999-0191")/*@res "��"*/ + iDynStartRow);
//					throw new BusinessException("��̬���ؼ������ֵ�����ظ���\r\n" + actArea.toString() + ":��" + value + "��");
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

				// �ؼ���У��ɹ����������ָ�����ݵĵ���
				cells = (CellPosition[]) hashDynMeas.keySet().toArray(
						new CellPosition[0]);
				// ������չ��ʱ��  ����Ӧ�ü���ָ�����ݵĸ���
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

					// ���Ƶ���ʱ�����û��趨��ֵ����
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
					// //��̬��Ϊ˽�йؼ���ʱ��Ҫ�����к�
					// md.setRowNo(iDynRowNo);
					// else
					// ��̬��Ϊ���йؼ���ʱ���к���Ϊ0
					md.setDataValue(StringUtils.trim(objVal));
					vecDataList.add(md);
				}
			}else if(bEnd)
				break;
			dynAddRowActIndex = dynAddRowActIndex+ dynOffset;
			iDynStartRow = iDynStartRow + dynOffset;
		}
		//�����ǰ�Ķ�̬��Ϊ�գ����һ������Ϊ����һ����̬�� ��� 1
		if(dynAddRowActIndex == oldDynIndex){
			dynAddRowActIndex++;
		}
		TwoTuple<List<MeasureDataVO>,List<String>> twoTuple = new TwoTuple<List<MeasureDataVO>, List<String>>(vecDataList, warnList);
		return twoTuple;
	}


	/**
	 * @create by wuyongc at 2013-1-22,����1:57:18
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
	 * @create by wuyongc at 2012-2-29,����2:38:19
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

	// @edit by wuyongc at 2012-2-28,����4:25:26 �޸�Ϊȡ������չ�Ķ�̬����ָ���λ��
	/**
	 *
	 *
	 * ת��ָ���ؼ���sheet�ж�Ӧ���ݵ�ʵ��λ�ã���������һ�����ݺ�ָ���ؼ��ֶ�Ӧ����λ��
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
		// Ŀǰ��֧������չ
		int iStartRow = dynAddRowActIndex + posArea.getStart().getRow();
		int iStartCol = posArea.getStart().getColumn();
		int iEndRow = dynAddRowActIndex + posArea.getEnd().getRow();
		int iEndCol = posArea.getEnd().getColumn();
		return AreaPosition.getInstance(CellPosition.getInstance(iStartRow,
				iStartCol), CellPosition.getInstance(iEndRow, iEndCol));
	}

	/**
	 * @create by wuyongc at 2012-2-28,����4:25:56
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
	 * ȡ�ú�����չ��̬����ָ��λ��
	 * @create by wuyongc at 2012-2-28,����4:27:30
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
	 * ���㶯̬������һ������Ӧ��Ҫ���ӵ�ʵ�������������ڶ�̬����λָ���ؼ���ȡ��Ӧλ�õ�ֵ
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
	 * ���㶯̬������һ������Ӧ�����ӵ�������������
	 * (�����̬������չ����������,Ϊ����,��չ�����Ǻ���,��Ϊ����)
	 * @create by wuyongc at 2012-2-28,����5:26:23
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
	 * @create by wuyongc at 2012-2-28,����5:29:37
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
		//�ڶ�����������ֵ��ʾ ��ֵ�Ƿ��ǹؼ��֣� �󲿷�������ǣ���������� ���ϼơ� ��������Ϊ���ǹؼ��֡�
		TwoTuple<String, Boolean> twoTuple = null;
		// �����ǰ��Ԫû��ֵ�������������һ����Ԫ��ȡֵ
		String val = objVal == null ? null : objVal.toString();
		if (val == null || val.trim().isEmpty())
			return new TwoTuple<String, Boolean>(null,false);;
		// @edit by wuyongc at 2013-9-11,����1:41:10 ��ۻ����ݺ�ߴ��пո񣬸���dongjw��xiaomenga���������Ժ�߽���trim
		val = UfoStringUtils.trimRight(val);
		if (kvo.getType() == KeyVO.TYPE_TIME || kvo.getType() == KeyVO.TYPE_ACC)
		{
			// ����Ȼʱ�����͹ؼ��ֵĵ���,ҪУ�����ڵĺϷ��ԣ�
			contentValue = checkKeyDateStr(val, kvo, area);
		} else if (kvo.getType() == KeyVO.TYPE_REF)
		{
			contentValue = checkRefDataStr(val, kvo.getRef_pk(),
					area);
			if(contentValue == null){
				if(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0256")/*@res "�ϼ�"*/.equals(val)){
					twoTuple = new TwoTuple(val,false);
					return twoTuple;
				}
				String warnMsg = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0257")/*@res "excel��Ԫ��"*/ + area.toString() + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0258")/*@res "��ֵ��"*/ + val + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0259")/*@res "���������ڹؼ��֡�"*/ + kvo.toString() + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0260")/*@res "�������յĻ������������Զ��嵵����"*/;
				warnMsgList.add(warnMsg);
			}
		} else
		{
			// ��ʱ�����͹ؼ��ֵĵ���,ҪУ�����ڵĺϷ��ԣ�
			contentValue = checkKeyCharStr(kvo, val, area);
		}
		twoTuple = new TwoTuple(contentValue == null ? NOT_FIND : contentValue,true);
		return twoTuple;

	}
	/**
	 *  @edit by wuyongc at 2014-1-13,����3:20:18 �����Ĳ������Ϳ���������Ҳ�����Ǳ��룬���Ǵ󲿷����Ӧ����ͳһ�ģ�Ҫô�Ǳ������Ҫô������
	 *  �˴�����һ���������������ж��Ƿ��Ǳ��룬�����һ���ܸ��ݱ����ѯ��������ΪExcel��Ĳ����������Ա��뵼���ģ������������Ƶ�����
	 *
	 *  ������������ĵ����ʱ���Ҳ������棬���Ϸ������ݿ��������֡�
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
	 * У��ʱ��ؼ��ֵ�¼��
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
					addErrDataTime(area.toString(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1060")/*@res "����ڼ䲻�Ϸ�"*/);
					return null;
				}
			} else
			{
				if (UFODate.isAllowDate(strValue) == false)
				{
					addErrDataTime(area.toString(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1061")/*@res "���ڲ��Ϸ�"*/); // "���ڲ��Ϸ�"
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
	 * У�鵥λ�ؼ���
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

		// ��ҪУ�鵥λ�Ϸ���
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

			// �жϸ���֯�Ƿ��ڵ�ǰ��֯�ṹ�У���Ϊ���ݵ�λ���¼�
			// ��IUFO���ķ������ڴ���������ʱ��EJB���ù��࣬�Ժ����NC�ṩ���棬����NC�ķ���
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
				addErrDataTime(area.toString(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1062")/*@res "��֯����Ϊ ["*/ + strValue
						+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1063")/*@res "] ����֯���ڵ�ǰ������֯�ṹ�л��Ǳ���������֯���¼���֯"*/);
				return null;
			}

			return strOrgPK;
		} else
			return strValue;
	}

	/**
	 * ����λ�ú�ָ������͵õ�¼���ֵ
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

		// �����ǰ��Ԫû��ֵ�������������һ����Ԫ��ȡֵ
		if (objVal == null)
			return null;
		// @edit by wuyongc at 2013-9-11,����1:43:58 �Ժ�߿ո�trim
		String content = UfoStringUtils.trimRight(objVal.toString());
		if (measure.getType() == IStoreCell.TYPE_NUMBER && content != null
				&& content.trim().length() > 0)
		{
			// ����ֵ��ָ��ĵ���,���Double.parseDouble������˵�����Ͳ���
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
					warnMsgList.add(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0261")/*@res "ָ����յĻ������������Զ��嵵�������ڣ�ָ��Ĳ���pk��"*/ + meas.getRefPK());
					AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820999-0192")/*@res "ָ����յĻ������������Զ��嵵�������ڣ�ָ��Ĳ���pk��"*/ + meas.getRefPK());
					AppDebug.debug(e);
					return null;
				}
			}else if(meas.getType() == MeasureVO.TYPE_DATE){
				// @edit by wuyongc at 2014-1-10,����2:08:27 �����������͵����ݣ�����һ�£���ΪĿǰNC��֧�֡�-�� ���ӵ����ڸ�ʽ��
				content = UFDateParseUtil.parseDate(content);
			}
		}

		// �����ַ���ָ�꣬�����ַ����ƱȽ��٣���ֱ�Ӵ���
		return content;
	}

	/**
	 * д������־,��Ӧ��λ����û����Ӧ��ָ�������͵�����,������Ҳ�������
	 *
	 * @param area
	 *            String
	 * @param repType
	 *            String
	 */
	private void addErrDataType(String area, String repType)
	{
		errCount += 1;
		// "IUFO����"+repName+"��ƥ���Excel������"+sheetName+"�е���Ӧλ�� "+area+" ���������Ͳ���!��Ҫ\""+repType+"\"����"
		logFile.writer(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0590")/*@res "IUFO����"*/
				+ // "IUFO����"
				nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0109")/*@res "��"*/ + repName
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1064")/*@res "��ƥ���Excel������"*/
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0109")/*@res "��"*/
				+ sheetName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1065")/*@res "�е���Ӧλ��"*/
				+ " " + area + " "
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1066")/*@res "���������Ͳ���!��Ҫ"*/ + "\""
				+ repType + "\""
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0365")/*@res "����"*/); // "����"
	}

	/**
	 * д������־,��Ӧ��λ����û����Ӧ��ָ�������͵�����,������Ҳ������ϣ���Ҫָ�ؼ��ֵ����ڲ��Ϸ�
	 *
	 * @param area
	 *            String
	 * @param repType
	 *            String
	 */
	protected void addErrDataTime(String area, String errMsg)
	{
		errCount += 1;
		// "IUFO����"+repName+"��ƥ���Excel������"+sheetName+"�е���Ӧλ�� "+area+" �Ϲؼ���¼�����"+errMsg+"!"
		logFile.writer(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0590")/*@res "IUFO����"*/
				+ // "IUFO����"
				nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0109")/*@res "��"*/ + repName
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1064")/*@res "��ƥ���Excel������"*/
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0109")/*@res "��"*/
				+ sheetName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1065")/*@res "�е���Ӧλ��"*/
				+ " " + area + " "
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1067")/*@res "�Ϲؼ���¼�����"*/
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0109")/*@res "��"*/ + errMsg
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
		// "���汨��"+repName+"("+repCode+")"+"��ָ������ʱ����"
		logFile.writer(
				NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0", "miufo1002770", null, new String[] { repName, repcode }));
		// + //"���汨��"
		// repName+"("+repCode+")"+"���汨��{0}({1})��ָ������ʱ����");
	}

	protected void addErrCommited()
	{
		errCount += 1;
		// "����"+repName+"("+repCode+")�Ѿ��ϱ����������޸ĸñ��������!"
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
	 * �жϱ����Ƿ��Ѿ��ϱ�������Ѿ��ϱ������ܽ��е�������
	 *
	 * @return boolean
	 */
	protected boolean isCommitedOrHaveNoRight(String strUserPK,String repPk)
	{
		return false;
	}

	/**
	 * �����Ҫ�Ե�������ݽ���У�飬��ͨ���̳б��࣬��ʵ�ָ÷���������У��
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
			//Ŀǰ����֧�ִ�ģʽ
			return 0;
		}
		return 0;
	}

	/**
	 *
	 * @create by wuyongc at 2012-2-28,����3:53:01
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
			//Ŀǰ����֧�ִ�ģʽ
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