/**
 *
 */
package com.ufsoft.iufo.fmtplugin.formula;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import nc.bs.logging.Logger;
import nc.impl.iufo.utils.ReportSrvUtil;
import nc.itf.org.IOrgConst;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.vo.bd.pub.NODE_TYPE;
import nc.vo.iufo.data.ComKeyDetailData;
import nc.vo.iufo.data.MeasureTraceVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.org.ReportOrgVO;
import nc.vo.pub.BusinessException;

import com.ufida.dataset.IContext;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.pub.tools.StringTools;
import com.ufida.iufo.table.exarea.ExtendAreaCell;
import com.ufida.iufo.table.exarea.ExtendAreaConstants;
import com.ufida.zior.exception.MessageException;
import com.ufsoft.iufo.fmtplugin.datastate.ReportDataModel;
import com.ufsoft.iufo.fmtplugin.dynarea.DynAreaUtil;
import com.ufsoft.iufo.fmtplugin.dynarea.DynamicAreaModel;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.key.KeywordModel;
import com.ufsoft.iufo.i18n.MultiLangUtil;
import com.ufsoft.iufo.util.OrgUtil;
import com.ufsoft.iufo.util.UfoeLicenseManager;
import com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup;
import com.ufsoft.iuforeport.reporttool.temp.KeyDataVO;
import com.ufsoft.iuforeport.tableinput.applet.IFormulaParsedDataItem;
import com.ufsoft.report.constant.DefaultSetting;
import com.ufsoft.report.constant.PropertyType;
import com.ufsoft.script.AreaFormulaUtil;
import com.ufsoft.script.CmdProxy;
import com.ufsoft.script.DynFormulaUtil;
import com.ufsoft.script.MeasFormulaUtil;
import com.ufsoft.script.MeasureFormulaChecker;
import com.ufsoft.script.base.CommonExprCalcEnv;
import com.ufsoft.script.base.ICalcEnv;
import com.ufsoft.script.base.IOperand;
import com.ufsoft.script.base.IParsed;
import com.ufsoft.script.base.IUfoTokenConsts;
import com.ufsoft.script.base.RowColFormulaParser;
import com.ufsoft.script.base.UfoArray;
import com.ufsoft.script.base.UfoEElement;
import com.ufsoft.script.base.UfoNullVal;
import com.ufsoft.script.base.UfoVal;
import com.ufsoft.script.datachannel.DataSetDynAreaDataParam;
import com.ufsoft.script.datachannel.ITableData;
import com.ufsoft.script.datachannel.IUFODynAreaDataParam;
import com.ufsoft.script.datachannel.IUFOTableData;
import com.ufsoft.script.exception.CmdException;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.script.exception.UfoValueException;
import com.ufsoft.script.expression.UfoCmdLet;
import com.ufsoft.script.expression.UfoCombExpr;
import com.ufsoft.script.expression.UfoExpr;
import com.ufsoft.script.expression.UfoFullArea;
import com.ufsoft.script.extfunc.DataSetFunc;
import com.ufsoft.script.extfunc.DataSetFuncCalcUtil;
import com.ufsoft.script.extfunc.MeasFunc;
import com.ufsoft.script.extfunc.MeasFuncUtil;
import com.ufsoft.script.extfunc.SrvCallU8;
import com.ufsoft.script.extfunc.StatisticFuncDriver;
import com.ufsoft.script.function.ExtFunc;
import com.ufsoft.script.function.FuncListInst;
import com.ufsoft.script.function.UfoFunc;
import com.ufsoft.script.spreadsheet.ReportDynCalcEnv;
import com.ufsoft.script.spreadsheet.UfoCalcEnv;
import com.ufsoft.script.util.FormulaPrintUtil;
import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.ExtDataModel;
import com.ufsoft.table.IArea;
import com.ufsoft.table.IExtModel;
import com.ufsoft.table.format.IFormat;
import com.ufsoft.table.format.TableConstant;

/**
 * UFO��ʽִ����
 *
 * @author liuchuna
 * @created at 2010-5-5,����10:10:57
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class UfoFmlExecutor extends BaseFmlExecutor
{
	//tianchuan ���ָ�����ͬ���͵Ĺ�ʽ
	private static final int PUBLIC_CELL_FML=0;	//���й�ʽ
	private static final int PRIVATE_CELL_FML=1;	//˽�й�ʽ
	private static final int TOTAL_FML=2;	//���ܹ�ʽ
	private static final int ZS_FML=3;		//���㹫ʽ
	private static final int CONS_FML=4;		//�ϲ���ʽ

	// ����Ԫ��ʽ������
	private Vector<Vector<IArea>> mainCellFmlList;

	// �ϲ���ʽ������
	private Vector<Vector<IArea>> allHBFmlList;

	// ��̬����Ԫ��ʽ������. key=��̬��pk,value=ĳ��̬����Ӧ��Ԫ��ʽ��
	private Map<String, Vector<Vector<IArea>>> dynCellFmlMap;

	// ������ܹ�ʽ������
	private Vector<Vector<IArea>> mainTotleFmlList;

	// ��̬�����ܹ�ʽ������. key=��̬��pk,value=ĳ��̬����Ӧ���ܹ�ʽ��
	private Map<String, Vector<Vector<IArea>>> dynTotleFmlMap;

	private Object diCalcExecutor = null;
	//edit by congdy hr ���ݷ�������ִ����
	private Object hrCalcExecutor = null;

	/**
	 * FormulaHandler ������ע��
	 *
	 * @param contextVO
	 *            ����ʹ��UfoContextVO����:
	 *            getContextId,getPubDataVO,isModel,getDataSource
	 *            ,isOnServer,getCurUserId getLoginTime,getCurrentLan
	 * @param cellModel
	 */
	protected UfoFmlExecutor(IContext contextVO, CellsModel cellModel)
	{
		this(contextVO, cellModel, false);
	}

	/**
	 * FormulaHandler ������ע��
	 *
	 * @param contextVO
	 *            ����ʹ��UfoContextVO����:
	 *            getContextId,getPubDataVO,isModel,getDataSource
	 *            ,isOnServer,getCurUserId getLoginTime,getCurrentLan
	 * @param cellModel
	 * @param bParseForm
	 *            �Ƿ������ʽ(�������еĵ�Ԫ��ʽ�����ܹ�ʽ)
	 */
	protected UfoFmlExecutor(IContext contextVO, CellsModel cellModel,
			boolean bParseForm)
	{
		super(contextVO, cellModel);

		if (bParseForm == true)
			initFormulaLet();

		// liuchun+ at 20110519 ���ݿɼ��Է�Χ���õ�ǰ��֯���Բ�����Щ��֯�еı���
		// ��Ե��빫ʽ�������ֶ���д��������֯�ı����е�ָ������
//		initVisableOrgs();
	}

	public static UfoFmlExecutor getInstance(IContext contextVO,
			CellsModel cellModel)
	{
		return getInstance(contextVO, cellModel, false);
	}

	/**
	 * v56,ͳһ��ʽ���������,ʹ�ñ��������档���ж�̬���͸��Ի���ʽʱ��ÿ�ι����¹�ʽ�� ����56���м�汾���˻���ͣ�á�
	 * UfoFmlExecutor�����ж������ʹ��CacheManager���ӵķ�ʽ����֤��CellsModelһ��ж�����档
	 *
	 * @author liuyy, 2009-2-20
	 */
	public static UfoFmlExecutor getInstance(final IContext contextVO,
			final CellsModel dataModel, boolean bParseForm)
	{
		return new UfoFmlExecutor(contextVO, dataModel, bParseForm);
	}

	public Object clone()
	{
		UfoFmlExecutor clone = new UfoFmlExecutor(null, null);
		clone.dynCellFmlMap = this.dynCellFmlMap;
		clone.mainCellFmlList = this.mainCellFmlList;
		clone.dynTotleFmlMap = this.dynTotleFmlMap;
		clone.mainTotleFmlList = this.mainTotleFmlList;

		return clone;
	}

	/**
	 * ��ʼ��ȫ���Ĺ�ʽ��(�������еĵ�Ԫ��ʽ�����ܹ�ʽ)
	 *
	 * @create by liuchuna at 2010-5-5,����10:55:39
	 *
	 */
	private void initFormulaLet()
	{
		try
		{
			// ��Ԫ��ʽ��ʽ��
			initCellFormulaLet();

			// ���ܹ�ʽ��ʽ��
			initTotleFormulaLet();
		} catch (Exception e)
		{
			AppDebug.debug("initFormulaLet", e);
		}
	}

	/**
	 * ������Ԫ��ʽ��(���������ӱ�ʽ��)
	 *
	 * @create by liuchuna at 2010-5-5,����10:53:51
	 *
	 */
	private void initCellFormulaLet()
	{
		// ������ʽ��
		if (getMainCellFormList() == null)
		{
			setupMainCellFmlList();
		}

		// ���ӱ�ʽ��
		ExtendAreaCell[] dynAreas = getDynAreaModel().getDynAreaCells();
		int iLen = dynAreas != null ? dynAreas.length : 0;
		for (int j = 0; j < iLen; j++)
		{
			String strDynPK = dynAreas[j].getBaseInfoSet().getExAreaPK();
			if (getDynCellFormList(strDynPK) == null)
			{
				// ������ʽ��
				setupDynCellFmlList(strDynPK);
			}
		}
		getExecutorEnv().setDynAreaInfo(null, null);

	}

	/**
	 * �������ܹ�ʽ��(���������ӱ�ʽ��)
	 *
	 * @create by liuchuna at 2010-5-5,����10:53:17
	 *
	 */
	private void initTotleFormulaLet()
	{
		// ������ʽ��
		if (getMainTotleFormList() == null)
		{
			setupMainTotleFmlList();
		}

		// ���ӱ�ʽ��
		ExtendAreaCell[] dynAreas = getDynAreaModel().getDynAreaCells();
		int iLen = dynAreas != null ? dynAreas.length : 0;
		for (int j = 0; j < iLen; j++)
		{

			String strDynPK = dynAreas[j].getBaseInfoSet().getExAreaPK();
			if (getDynTotleFmList(strDynPK) == null)
			{
				// ������ʽ��
				setupDynTotleFmlList(strDynPK);
			}
		}
		getExecutorEnv().setDynAreaInfo(null, null);

	}

	/**
	 * ���¹�����Ԫ��ʽ��
	 */
	public void reInitCellFmlLet()
	{
		reInitFormulaLet(true);

	}

	/**
	 * ���¹���������ʽ��
	 *
	 * @param bCell
	 *            true/false ��Ԫ��ʽ/���ܹ�ʽ
	 */
	private void reInitFormulaLet(boolean bCell)
	{
		if (bCell)
		{
			mainCellFmlList = null;
			dynCellFmlMap = null;
		} else
		{
			mainTotleFmlList = null;
			dynTotleFmlMap = null;
		}
		initFormulaLet();
	}

	/**
	 * �������Ԫ��ʽ��
	 *
	 * @create by liuchuna at 2010-5-5,����10:38:51
	 *
	 * @return
	 */
	private Vector<Vector<IArea>> getMainCellFormList()
	{
		return mainCellFmlList;
	}

	/**
	 * ���������ܹ�ʽ��
	 *
	 * @create by liuchuna at 2010-5-5,����10:39:13
	 *
	 * @return
	 */
	private Vector<Vector<IArea>> getMainTotleFormList()
	{
		return mainTotleFmlList;
	}

	/**
	 * ���ĳ��̬���ĵ�Ԫ��ʽ��
	 *
	 * @param strDynPK
	 * @return
	 */
	private Vector<Vector<IArea>> getDynCellFormList(String strDynPK)
	{
		return dynCellFmlMap == null ? null
				: (Vector<Vector<IArea>>) dynCellFmlMap.get(strDynPK);
	}

	/**
	 * ���ĳ��̬���Ļ��ܹ�ʽ��
	 *
	 * @create by liuchuna at 2010-5-6,����04:52:11
	 *
	 * @param strDynPK
	 * @return
	 */
	private Vector<Vector<IArea>> getDynTotleFmList(String strDynPK)
	{
		return dynTotleFmlMap == null ? null
				: (Vector<Vector<IArea>>) dynTotleFmlMap.get(strDynPK);
	}

	/**
	 * ɾ����̬����ʽ����ָ����̬���Ĺ�ʽ��
	 *
	 * @create by liuchuna at 2010-5-6,����04:52:41
	 *
	 * @param strDynPK
	 * @param bCell
	 */
	private void removeDynFormList(String strDynPK, int type) {
		if (type == FormulaModel.TYPE_CELL_FML) {
			if (dynCellFmlMap != null)
				dynCellFmlMap.remove(strDynPK);
		} else if (type == FormulaModel.TYPE_TOTAL_FML) {
			if (dynTotleFmlMap != null) {
				dynTotleFmlMap.remove(strDynPK);
			}
		}
	}

	/**
	 * ���ָ����ʽ�����Ӧ����Ч��̬����
	 *
	 * @param area
	 * @return
	 * @throws DynAreaException
	 */
	public ExtendAreaCell getValidDynamicArea(IArea area)
			throws DynAreaException
	{
		// ������򸲸ǵĶ�̬��
		ExtendAreaCell[] dynCells = getDynAreaModel()
				.getDynAreaCellByArea(area);

		if (dynCells != null && dynCells.length > 1)
		{
			// ��̬���򽻲�,���ܶ��幫ʽ
//			throw new DynAreaException();
			return null;
		}
		ExtendAreaCell dynVO = null;
		if (dynCells != null && dynCells.length == 1)
		{
			AreaPosition dynArea = DynAreaUtil.getFormatExCellByDataCell(
					dynCells[0], this.getDataModel()).getArea();
			if (dynArea.contain(area) == false)
			{
				// ������ͬʱ���й̶�����Ͷ�̬����
//				throw new DynAreaException();
				return null;
			}
			// ѡ�еĵ�Ԫ�ж�̬����,ֻ�ܶ����ڶ�̬������
			dynVO = dynCells[0];
		}
		return dynVO;
	}
	
	/**
	 * ����ʵ�����������ڶ�̬��
	 * 
	 * @param area
	 * @return
	 * @throws DynAreaException
	 */
	public ExtendAreaCell getDynCellByRealArea(IArea area) throws DynAreaException {
		// ������򸲸ǵĶ�̬��
		ExtendAreaCell[] dynCells = getDynAreaModel().getDynAreaCellByArea(area);
		ExtendAreaCell dynVO = null;
		if (dynCells != null && dynCells.length >= 1) {
			dynVO = dynCells[0];
		}
		
		return dynVO;
	}

	/**
	 * �����û���ʽ�Ĺ�ʽ
	 *
	 * @param showMessage
	 * @param area
	 * @param strUserDefFormula
	 * @param bPublic
	 *            �˲���ֻ���� bCell=trueʱ�����塣����Ϊ��ӵ��Ƿ�Ϊ���й�ʽ�� bPublic=true
	 *            �ҵ�ǰΪ������λʱ�����ܼ��빫�й�ʽ�� bPublic=false ���Ի���ʽ
	 * @return
	 */
	public boolean addUserDefFormula(StringBuffer showMessage, IArea area,
			String strUserDefFormula, int type, boolean bPublic)
			throws ParseException
	{
		return addFormula(showMessage, area, strUserDefFormula, null, true,
				type, bPublic);
	}

	/**
	 * �����û���ʽ�Ĺ�ʽ
	 *
	 * @param showMessage
	 * @param area
	 * @param strUserDefFormula
	 * @param bPublic
	 *            �˲���ֻ���� bCell=trueʱ�����塣����Ϊ��ӵ��Ƿ�Ϊ���й�ʽ�� bPublic=true
	 *            �ҵ�ǰΪ������λʱ�����ܼ��빫�й�ʽ�� bPublic=false ���Ի���ʽ
	 * @param bClearOldFormula
	 *            true ��ʾ���������ԭ�й�ʽ��false��ʾ�������ԭ�й�ʽ
	 * @return
	 */
	public boolean addUserDefFormula(StringBuffer showMessage, IArea area,
			String strUserDefFormula, int type, boolean bPublic,
			boolean bClearOldFormula) throws ParseException
	{
		return addFormula(showMessage, area, strUserDefFormula, null, true,
				type, bPublic, bClearOldFormula);
	}

	public boolean addUserDefFormula(StringBuffer showMessage, IArea area,
			String strUserDefFormula, int type, boolean bPublic,
			boolean bClearOldFormula, boolean isCheckLoop) throws ParseException
	{
		return addFormula(showMessage, area, strUserDefFormula, null, true,
				type, bPublic, bClearOldFormula, isCheckLoop);
	}

	/**
	 * �������ݿ���ʽ�Ĺ�ʽ
	 *
	 * @param showMessage
	 * @param area
	 * @param strdbDefFormula
	 * @param dbLet
	 * @param bPublic
	 *            �˲���ֻ���� bCell=trueʱ�����塣����Ϊ��ӵ��Ƿ�Ϊ���й�ʽ�� bPublic=true
	 *            �ҵ�ǰΪ������λʱ�����ܼ��빫�й�ʽ�� bPublic=false ���Ի���ʽ
	 * @return
	 */
	public boolean addDbDefFormula(StringBuffer showMessage, IArea area,
			String strdbDefFormula, IParsed dbLet, int type,
			boolean bPublic) throws ParseException
	{
		return addFormula(showMessage, area, strdbDefFormula, dbLet, false,
				type, bPublic);
	}

	/**
	 * �������ݿ���ʽ�Ĺ�ʽ
	 *
	 * @param showMessage
	 * @param area
	 * @param strdbDefFormula
	 * @param dbLet
	 * @param bPublic
	 *            �˲���ֻ���� bCell=trueʱ�����塣����Ϊ��ӵ��Ƿ�Ϊ���й�ʽ�� bPublic=true
	 *            �ҵ�ǰΪ������λʱ�����ܼ��빫�й�ʽ�� bPublic=false ���Ի���ʽ
	 * @param bClearOldFormula
	 *            true ��ʾ���������ԭ�й�ʽ��false��ʾ�������ԭ�й�ʽ
	 * @return
	 */
	public boolean addDbDefFormula(StringBuffer showMessage, IArea area,
			String strdbDefFormula, IParsed dbLet, int type,
			boolean bPublic, boolean bClearOldFormula) throws ParseException
	{
		return addFormula(showMessage, area, strdbDefFormula, dbLet, false,
				type, bPublic, bClearOldFormula);
	}

	/**
	 * ���ӹ�ʽ
	 *
	 * @param showMessage
	 *            ���빫ʽʱ����ʾ��Ϣ
	 * @param area
	 *            ��ʽ����
	 * @param strFormula
	 *            ��ʽ���ݡ����bUserDef=true ���ʾ�û���ʾ��ʽ�����ݡ�����Ϊ���ݿ���ʽ�Ĺ�ʽ����
	 * @param dbLet
	 *            ���ݿ���ʽ�Ĺ�ʽ��������
	 * @param bUserDef
	 * @param bPublic
	 *            �˲���ֻ���� bCell=trueʱ�����塣����Ϊ��ӵ��Ƿ�Ϊ���й�ʽ�� bPublic=true
	 *            �ҵ�ǰΪ������λʱ�����ܼ��빫�й�ʽ�� bPublic=false ���Ի���ʽ
	 * @param bOnlyAddFormList
	 *            true ��ʾֻ���ӵ���ʽ���У������޸�����ģ�͡�false��ʾ�����ӵ���ʽ���У����޸�����ģ��
	 * @param bClearOldFormula
	 *            true ��ʾ���������ԭ�й�ʽ��false��ʾ�������ԭ�й�ʽ
	 * @return
	 */
	private boolean addMainFormula(StringBuffer showMessage, IArea area,
			String strFormula, IParsed dbLet, boolean bUserDef, int type,
			boolean bPublic, boolean bOnlyAddFormList, boolean bClearOldFormula)
			throws ParseException
	{

		if ((strFormula == null || strFormula.trim().length() == 0)
				&& dbLet == null)
			return false;

		// ����Ϊ����ʽ����
		String strDbFormula = null;
		try
		{
			// if (strFormula == null) {
			// strDbFormula = dbLet.toString();
			// }

			// if (strFormula == null || strFormula.length()==0) {
			// return true;
			// }

			// add by ljhua 2005-3-18 �޷�����ⲿ���������Է�������ʱ�رռ�鿪��
			turnOffExtFuncCheck();

			// �̶����������̬��Ϣ
			getExecutorEnv().setDynAreaInfo(null, null);
			if (bUserDef == true)
			{
				if (dbLet == null)
				{
					UfoCmdLet objLet = getFormulaProxy().parseUserDefFormula(
							area, strFormula);

					if (MultiLangUtil.LANGCODE_ZH
							.equals(MultiLangUtil.LocalLan))
						dbLet = objLet;
					else
					{
						strDbFormula = objLet.toString(getExecutorEnv());
						dbLet = getFormulaProxy().parseFormula(area,
								strDbFormula);
					}
				}
			} else
			{
				strDbFormula = strFormula;
				if (dbLet == null && strDbFormula != null)
					dbLet = getFormulaProxy().parseFormula(area, strDbFormula);
			}

			if (strDbFormula == null)
				strDbFormula = dbLet.toString(getExecutorEnv());

			// �������ԭ�й�ʽ
			if (bClearOldFormula == true)
			{
				if(bPublic){
					clearDynFormula(showMessage, null, area, type, FormulaModel.CALC_FML_PUBLIC);
				} else {
					clearDynFormula(showMessage, null, area, type, FormulaModel.CALC_FML_PERSONAL);
				}

			}

			// �ӵ���ʽ����
			// ��ӹ�ʽʱ��������Ҫ����ʽ���뵽��ʽ����
			/*
			 * if (getMainFormList(bCell) == null) {
			 * setupMainFormulaList(bCell); } Vector alist =
			 * AreaFormulaUtil.getAreaList(dbLet); boolean bAddSuccess =
			 * getMainFormList(bCell).addFormula(area, alist);
			 */

			boolean bAddSuccess = true;

			if (bAddSuccess == true && bOnlyAddFormList == false)
			{
				//�޸��ڴ�й¶
//				FormulaVO fe = new FormulaVO(getReportPK(), strDbFormula, dbLet);
				FormulaVO fe = new FormulaVO(getReportPK(), strDbFormula);
				fe.setRelative(AreaFormulaUtil.isRelativeFormula(dbLet));
				fe.setUserDefContent(dbLet.toUserDefString(getExecutorEnv()));
				
				getFormulaModel().setMainFmlVO(area, fe, type, bPublic);
				// ������Ի���ʽ���
				// getFormulaModel().removePersonalFormulaByArea(area);

				// ��������ģ�����ù�ϵ
				addRefSmtDef(fe.getId(),dbLet);
			}
			return bAddSuccess;

		} catch (ParseException e)
		{
			AppDebug.debug(strDbFormula + " is wrong", e);
			throw e;
		} finally
		{
			turnOnExtFuncCheck();
		}

	}

	/**
	 * ���ӹ�ʽ
	 *
	 * @param showMessage
	 * @param area
	 *            ��ʽ����
	 * @param strFormula
	 *            ��ʽ���ݡ����bUserDef=true ���ʾ�û���ʾ��ʽ�����ݡ�����Ϊ���ݿ���ʽ�Ĺ�ʽ����
	 * @param dbLet
	 *            ���ݿ���ʽ�Ĺ�ʽ��������
	 * @param bUserDef
	 * @param bPublic
	 *            �˲���ֻ���� bCell=trueʱ�����塣����Ϊ��ӵ��Ƿ�Ϊ���й�ʽ�� bPublic=true
	 *            �ҵ�ǰΪ������λʱ�����ܼ��빫�й�ʽ�� bPublic=false ���Ի���ʽ
	 * @return
	 */
	private boolean addFormula(StringBuffer showMessage, IArea area,
			String strFormula, IParsed dbLet, boolean bUserDef, int type,
			boolean bPublic) throws ParseException
	{
		return addFormula(showMessage, area, strFormula, dbLet, bUserDef,
				type, bPublic, true);
	}

	private boolean addFormula(StringBuffer showMessage, IArea area,
			String strFormula, IParsed dbLet, boolean bUserDef, int type,
			boolean bPublic, boolean bClearOldFormula) throws ParseException
	{
		return addFormula(showMessage, area, strFormula, dbLet, bUserDef, type,
				bPublic, bClearOldFormula, true);
	}

	/**
	 * ���ӹ�ʽ
	 *
	 * @param showMessage
	 * @param area
	 *            ��ʽ����
	 * @param strFormula
	 *            ��ʽ���ݡ����bUserDef=true ���ʾ�û���ʾ��ʽ�����ݡ�����Ϊ���ݿ���ʽ�Ĺ�ʽ����
	 * @param dbLet
	 *            ���ݿ���ʽ�Ĺ�ʽ��������
	 * @param bUserDef
	 * @param bPublic
	 *            �˲���ֻ���� bCell=trueʱ�����塣����Ϊ��ӵ��Ƿ�Ϊ���й�ʽ�� bPublic=true
	 *            �ҵ�ǰΪ������λʱ�����ܼ��빫�й�ʽ�� bPublic=false ���Ի���ʽ
	 * @param bClearOldFormula
	 *            true ��ʾ���������ԭ�й�ʽ��false��ʾ�������ԭ�й�ʽ
	 * @return
	 */
	private boolean addFormula(StringBuffer showMessage, IArea area,
			String strFormula, IParsed dbLet, boolean bUserDef, int type,
			boolean bPublic, boolean bClearOldFormula, boolean isCheckLoop) throws ParseException
	{
		
		if(area!=null){	//tianchuan 2013.5.10  ��ֹ��������������磬��������CellsModel�����޷��򿪵�����
			if(area.getStart().getRow()>=DefaultSetting.MAX_ROW_NUM ||
					area.getStart().getColumn()>=DefaultSetting.MAX_COL_NUM || 
					area.getEnd().getRow()>=DefaultSetting.MAX_ROW_NUM || 
					area.getEnd().getColumn()>=DefaultSetting.MAX_COL_NUM){
				throw new ParseException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413013-0030")/*@res "��ʽ����Ƿ�"*/);
			}
		}
		
		if(area instanceof CellPosition) {
			// �ȱ���5ϵ���߼�����Ԫ��ʽ��λ�����Ͷ���AreaPosition����
			area = AreaPosition.getInstance(area.getStart(), area.getEnd());
		}

		if ((strFormula == null || strFormula.trim().length() == 0)
				&& dbLet == null)
		{
			if(bPublic){
				clearFormula(area, type, FormulaModel.CALC_FML_PUBLIC);
			} else {
				clearFormula(area, type, FormulaModel.CALC_FML_PERSONAL);
			}
			return true;
		}

		// �ж��Ƿ����ڶ�̬����
		ExtendAreaCell dynVO = null;
		try {
			dynVO = getValidDynamicArea(area);
		} catch (DynAreaException e) {
			return false;
		}
		// ���ö�̬��������Ϣ,����ʱ��Ҫ
		if(dynVO != null) {
			String strDynPK = dynVO.getBaseInfoSet().getExAreaPK();
			if (getExecutorEnv().getDynArea() == null || !(getExecutorEnv().getDynArea().equals(strDynPK)))	{
				getExecutorEnv().setDynAreaInfo(strDynPK, null);
			}
		}

		try {
			turnOffExtFuncCheck();
			if(!checkAllTypeFmlsInEveryCell(showMessage,area,bPublic)){
				return false;
			}


			if (bUserDef == true) {
				if (dbLet == null) {
					IParsed objUserLet = getFormulaProxy().parseUserDefExpr(strFormula);
					if (MultiLangUtil.LANGCODE_ZH.equals(MultiLangUtil.LocalLan)) {
						dbLet = objUserLet;
					} else {
						dbLet = getFormulaProxy().parseExpr(strFormula);
					}
				}
			} else {
				if (dbLet == null)
					dbLet = getFormulaProxy().parseExpr(strFormula);
			}
		} finally {
			turnOnExtFuncCheck();
		}

		// ����У��ѭ����ʽ���洢��ʽ̬��λ��
		Vector<String> areaVec = new Vector<String>();
		//begin-ncm-rendp-NCdp205348537-2015-4-20-ר�� 
		/** 
		* �˲��������˲�ƷBUG:�жϵ�Ԫ��ѭ������Ч���Ż������жϹ��ĵ�Ԫ����뻺�棬�����ظ��ж�
		*/ 
		// ���ڴ洢���й��жϴ���ĵ�Ԫ��
		Vector<String> dealedVec = new Vector<String>();
		// У���Ƿ����ѭ������
		//if(isCheckLoop && isLoopRef(areaVec, area, dbLet, strFormula, type, bUserDef)){
		if(isCheckLoop && isLoopRef(areaVec, area, dbLet, strFormula, type, bUserDef, dealedVec)){
			showMessage.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0907")/*@res "����Ĺ�ʽ����ѭ�����ã�"*/);
			return false;
		}
		//end-ncm-rendp-NCdp205348537-2015-4-20-ר�� 

		boolean bReturn = false;
		FormulaVO fvo = null;
		if (dynVO != null)
		{
			String strDynPK = dynVO.getBaseInfoSet().getExAreaPK();
			// ��̬����ʽֻ֮�ܶ��嵽ָ�굥Ԫ��
			bReturn = addDynFormula(showMessage, strDynPK, area, strFormula,
					bUserDef, dbLet, type, bPublic, false,true);

		} else
		{
			bReturn = addMainFormula(showMessage, area, strFormula, dbLet,
					bUserDef, type, bPublic, false, bClearOldFormula);
			if (bReturn)
			{
				fvo = getFormulaModel().getDirectFml(area,
						FormulaModel.MAINTABLE_DYNPK, type);

				// ���ݹ�ʽ��ֵ����,���ĵ�Ԫ�����͡������ָ�굥Ԫ���򲻸�
//				if (fvo != null && fvo.getLet() != null)
//				{
//					int iFormulaType = getFormulaValueType(fvo.getLet());
//					setCellType(area, iFormulaType);
//				}
				if (dbLet != null)
				{
					int iFormulaType = getFormulaValueType(dbLet);
					setCellType(area, iFormulaType);
				}
			}
		}
		return bReturn;

	}

	/*
	 * ���ÿһ����Ԫ���������͵Ĺ�ʽ����������뵱ǰ����ì�ܵĹ�ʽ���򣬷���false
	 */
	private boolean checkAllTypeFmlsInEveryCell(StringBuffer showMessage,IArea area,boolean bPublic){
		if(area.getStart()==area.getEnd()){
			return true;
		}
		//2013.4.2 ++ ����ͨ�䲻���������ļ��
		if(RowColFormulaParser.ROW_MATCH.equals(getCalcEnv().getExEnv(CommonExprCalcEnv.ROW_COL_MATCH_TYPE))
				|| RowColFormulaParser.COL_MATCH.equals(getCalcEnv().getExEnv(CommonExprCalcEnv.ROW_COL_MATCH_TYPE))){
			return true;
		}
		
//		if(area instanceof CellPosition){
//			return true;
//		}
		CellPosition[] allCells=area.split();
		if(allCells!=null && allCells.length>0){
			for(int i=0;i<allCells.length;i++){
				if(bPublic){	//������ǹ��й�ʽ��˽�й�ʽ�����������ǡ���Ϊ˽�й�ʽ���Ը��ǹ��й�ʽ
					//��鹫�й�ʽ
					if(!checkOneTypeFmlInOneCell(showMessage,area,allCells[i],PUBLIC_CELL_FML)){
						return false;
					}
				}

				//���˽�й�ʽ
				if(!checkOneTypeFmlInOneCell(showMessage,area,allCells[i],PRIVATE_CELL_FML)){
					return false;
				}
				//�����ܹ�ʽ
				if(!checkOneTypeFmlInOneCell(showMessage,area,allCells[i],TOTAL_FML)){
					return false;
				}
				//������㹫ʽ
				if(!checkOneTypeFmlInOneCell(showMessage,area,allCells[i],ZS_FML)){
					return false;
				}
				//���ϲ���ʽ
				if(!checkOneTypeFmlInOneCell(showMessage,area,allCells[i],CONS_FML)){
					return false;
				}
			}
		}
		return true;
	}

	/*
	 * ���ĳһ������ĳһ��Ԫ�ϵĹ�ʽ
	 */
	private boolean checkOneTypeFmlInOneCell(StringBuffer showMessage,IArea area,CellPosition cellPos,int fmlType){
		Object[] tempObjs=getRelatedFmlVO(getFormulaModel(),cellPos, fmlType);
		if(tempObjs!=null && tempObjs.length>=2){
			if(tempObjs[1]!=null){	//��ʽ����
				//�����ʽ�������Ѿ����ڵĹ�ʽ�����뽫Ҫ���������ͬ
				if(tempObjs[0] instanceof IArea && !((IArea)tempObjs[0]).toString().equalsIgnoreCase(area.toString())){
					showMessage.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0123")/*@res "��ʽ����ʧ�ܣ�ԭ��"*/);
					showMessage.append(tempObjs[0].toString());
					showMessage.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0124")/*@res "�Ѿ�������������ʽ"*/);
					return false;
				}
			}
		}
		return true;
	}

	//���������FmlDefUtil�����ظ��ˣ�����ʱ���������Ժ�Ҫ��
	private Object[] getRelatedFmlVO(FormulaModel formulaModel,CellPosition cellPos, int fmlType){
		Object[] objs ={null,null};
		switch(fmlType){
			case PUBLIC_CELL_FML:{
				IArea fmlArea = formulaModel.getRelatedFmlArea(cellPos, FormulaModel.TYPE_CELL_FML);
				// ��ȡ���й�ʽ
				FormulaVO publicCellFormula = fmlArea == null ? null: formulaModel.getPublicDirectFml(fmlArea);
				if(publicCellFormula != null) {
					objs[0]=fmlArea;
					objs[1]=publicCellFormula;
				}
				break;
			}
			case PRIVATE_CELL_FML:{
				IArea fmlArea = formulaModel.getRelatedFmlArea(cellPos, FormulaModel.TYPE_CELL_FML);
				// ��ȡ˽�й�ʽ
				FormulaVO personCellFormula = fmlArea == null ? null: formulaModel.getPersonalDirectFml(fmlArea);
				if(personCellFormula != null) {
					objs[0]=fmlArea;
					objs[1]=personCellFormula;
				}
				break;
			}
			case TOTAL_FML:{
				objs = formulaModel.getRelatedFmlVO(cellPos, FormulaModel.TYPE_TOTAL_FML);
				break;
			}
			case ZS_FML:{
				objs = formulaModel.getRelatedFmlVO(cellPos, FormulaModel.TYPE_ZS_FML);
				break;
			}
			case CONS_FML:{
				objs = formulaModel.getRelatedFmlVO(cellPos, FormulaModel.TYPE_CONS_FML);
				break;
			}
			default :{//Ĭ�ϾͰ����й�ʽȡ
				objs = formulaModel.getRelatedFmlVO(cellPos, FormulaModel.TYPE_CELL_FML);
				break;
			}
		}
		return objs;
	}

	private void addRefSmtDef(String fmlId,IParsed parsedLet) {
		if(fmlId == null || parsedLet==null) {
			return;
		}
		
		String refSmartId = getRefSmartId(parsedLet);
		if(refSmartId == null || refSmartId.length() == 0) {
			return;
		}
		Map<String, List<String>> smartIdToFmlId = getSmartIdToFmlIdMap();
		List<String> fmlIdList = null;
		if(smartIdToFmlId.containsKey(refSmartId)) {
			fmlIdList = smartIdToFmlId.get(refSmartId);
		} else {
			fmlIdList = new Vector<String>();
			smartIdToFmlId.put(refSmartId, fmlIdList);
		}
//		String fmlId = fvo.getId();
		if(!fmlIdList.contains(fmlId)) {
			fmlIdList.add(fmlId);
		}
	}

	private void clearRefSmtDef(String fmlId,IParsed parsedLet) {
		if(fmlId == null || parsedLet==null) {
			return;
		}
		
		String refSmartId = getRefSmartId(parsedLet);
		if(refSmartId == null || refSmartId.length() == 0) {
			return;
		}
		
		Map<String, List<String>> smartIdToFmlId = getSmartIdToFmlIdMap();
		List<String> fmlIdList = null;
		if(smartIdToFmlId.containsKey(refSmartId)) {
			fmlIdList = smartIdToFmlId.get(refSmartId);
		} else {
			fmlIdList = new Vector<String>();
			smartIdToFmlId.put(refSmartId, fmlIdList);
		}
//		String fmlId = fvo.getId();
		if(fmlIdList.contains(fmlId)) {
			fmlIdList.remove(fmlId);
		}
		if(fmlIdList.isEmpty()) {
			smartIdToFmlId.remove(refSmartId);
		}
	}

	private Map<String, List<String>> getSmartIdToFmlIdMap() {
		IExtModel refSmtIdModel = getCellModel().getExtProp(IUfoContextKey.KEY_REF_SMART_ID);
		if(refSmtIdModel == null) {
			Map<String,List<String>> smartIdToFmlId = new Hashtable<String,List<String>>();
			refSmtIdModel = new ExtDataModel(smartIdToFmlId);
			getCellModel().putExtProp(IUfoContextKey.KEY_REF_SMART_ID, refSmtIdModel);
		}

		Map<String,List<String>> smartIdToFmlId = (Map<String,List<String>>)((ExtDataModel)refSmtIdModel).getValue();
		return smartIdToFmlId;
	}

	private String getRefSmartId(IParsed parsedLet) {
//		IParsed objLet = fvo.getLet();
		if(parsedLet != null) {
			List alExpr = new ArrayList();
			parsedLet.getAllExprs(alExpr);
			for (Object obj : alExpr) {
				if (obj instanceof UfoExpr) {
					UfoEElement[] m_elements = ((UfoExpr) obj).getElements();
					for (int i = 0; i < m_elements.length; i++) {
						Object elem = m_elements[i].getObj();
						if (elem instanceof DataSetFunc) {
							return ((DataSetFunc) elem).getDataSetVal().getPk_def();
						}
					}
				}
			}
		}
		return "";
	}

	//begin-ncm-rendp-NCdp205348537-2015-4-20-ר�� 
	/** 
	* �˲��������˲�ƷBUG:�жϵ�Ԫ��ѭ������Ч���Ż������жϹ��ĵ�Ԫ����뻺�棬�����ظ��ж�
	*/ 
	//private boolean isLoopRef(Vector<String> areaVec, IArea defArea, IParsed dbLet, String strFormula, int type, boolean bUserDef){
	private boolean isLoopRef(Vector<String> areaVec, IArea defArea, IParsed dbLet, String strFormula, int type, boolean bUserDef, Vector<String> dealedVec){
		try {

			// ����λ�ü��뵽vector�У������ж�ѭ��
			addAreaIntoVector(areaVec, defArea);

			try {
				turnOffExtFuncCheck();

				if (bUserDef == true) {
					if (dbLet == null) {
						IParsed objUserLet = getFormulaProxy().parseUserDefExpr(strFormula);
						if (MultiLangUtil.LANGCODE_ZH.equals(MultiLangUtil.LocalLan)) {
							dbLet = objUserLet;
						} else {
							strFormula = objUserLet.toString(getExecutorEnv());
							dbLet = getFormulaProxy().parseExpr(strFormula);
						}
					}
				} else {
					if (dbLet == null)
						dbLet = getFormulaProxy().parseExpr(strFormula);
				}
			} finally {
				turnOnExtFuncCheck();
			}

			Vector<IArea> alist = MeasFormulaUtil.getReferringMeasArea(dbLet,
					MeasFormulaUtil.CHECK_EXP_MSELECT_COND, getExecutorEnv());

			// û�����õ�Ԫ���򲻴���ѭ��
			if (alist == null || alist.isEmpty()) {
				removeAreaFromVector(areaVec, defArea);
				return false;
			}
			// ѭ�����á�������У��
			for (IArea area : alist)
			{
				if (area == null) {
					continue;
				}
				CellPosition[] cells = null;
				if (area instanceof CellPosition)
				{
					cells = new CellPosition[] { (CellPosition) area };
				} else
				{
					cells = area.split();
				}
				if (cells != null && cells.length > 0)
				{
					for (CellPosition cell : cells)
					{
						if(dealedVec.contains(cell.toString())){//�õ�Ԫ���ѽ��й��ж�
							removeAreaFromVector(areaVec, defArea);
							continue;
						}
						
						if(areaVec.contains(cell.toString())){//����ѭ������
							removeAreaFromVector(areaVec, defArea);
							return true;
						}

	                    IArea realArea = DynAreaUtil.getRealArea(cell, getDataModel());
						FormulaVO formula = getFormulaByArea(null, (CellPosition) realArea, type, false);
						if (formula != null)
						{
							//if(isLoopRef(areaVec, realArea, formula.getLet(), formula.getFormulaContent(), type, false)){
							if(isLoopRef(areaVec, realArea, formula.getLet(), formula.getFormulaContent(), type, false, dealedVec)){
								removeAreaFromVector(areaVec, defArea);
								return true;
							}
						}

					}
				}
			}

			// �˳�֮�󣬽�λ����Ϣ����
			removeAreaFromVector(areaVec, defArea);
		} catch (Exception e){
			AppDebug.debug(e);
		} finally {
			removeAreaFromVector(areaVec, defArea);
			// ���Ѵ���ĵ�Ԫ����뵽vector�У������ظ��ж�
			addAreaIntoVector(dealedVec, defArea);
		}
		return false;
	}

	/**
	 * ��ù�ʽ����ֵ����
	 *
	 * @param parsedLet
	 *            ��ʽ�������
	 * @return
	 */
	private int getFormulaValueType(IParsed parsedLet)
	{
		int iType = TableConstant.CELLTYPE_NUMBER;
		if (parsedLet != null)
		{
			if (parsedLet instanceof UfoExpr)
			{
				byte bType = ((UfoExpr) parsedLet).getType();
				if ((bType & 0xf0) == IParsed.STRING_VAL)
					iType = TableConstant.CELLTYPE_STRING;
			} else if (parsedLet instanceof UfoCmdLet)
			{
				Vector vecLet = ((UfoCmdLet) parsedLet).getLetList();
				if (vecLet != null && vecLet.size() > 1)
				{
					Object objExpr = vecLet.get(1);
					if (objExpr != null && objExpr instanceof UfoExpr)
					{
						byte bType = ((UfoExpr) objExpr).getType();
						if ((bType & 0xf0) == IParsed.STRING_VAL)
							iType = TableConstant.CELLTYPE_STRING;
					}
				}
			}
		}
		return iType;
	}

	/**
	 * ���ݹ�ʽ��ֵ����,���ĵ�Ԫ�����͡������ָ�굥Ԫ���򲻸�
	 *
	 * @param area
	 * @param iFormulaType
	 */
	private void setCellType(IArea area, int iFormulaType)
	{
		if (area == null || area.getStart() == null)
			return;
		List<CellPosition> listCell = getCellModel().getSeperateCellPos(
				area);
		if (listCell == null)
			return;
		CellPosition cell = null;
		for (int i = 0, size = listCell.size(); i < size; i++)
		{
			cell = listCell.get(i);
			if (cell == null)
				continue;
			IFormat format =  getCellModel().getCellFormat(cell);
			if ((format == null
					|| format.getCellType() == TableConstant.CELLTYPE_SAMPLE || format
					.getCellType() == TableConstant.UNDEFINED)
					&& getDynAreaModel().getMeasureModel().getMeasureVOByPos(
							cell) == null)
			{
				getCellModel().setCellProperty(cell, PropertyType.DataType,
						iFormulaType);
			}
		}
	}

	/**
	 * ���Ӷ�̬��ָ�깫ʽ
	 *
	 * @param showMessage
	 * @param strDynPK
	 * @param cell
	 * @param strFormula
	 *            ��ʽ���ݡ����bUserDef=true ���ʾ�û���ʾ��ʽ�����ݡ�����Ϊ���ݿ���ʽ�Ĺ�ʽ����
	 * @param bUserDef
	 * @param dbLet
	 *            ���ݿ���ʽ�Ĺ�ʽ��������
	 * @param bPublic
	 *            �˲���ֻ���� bCell=trueʱ�����塣����Ϊ��ӵ��Ƿ�Ϊ���й�ʽ�� bPublic=true
	 *            �ҵ�ǰΪ������λʱ�����ܼ��빫�й�ʽ�� bPublic=false ���Ի���ʽ
	 * @param bOnlyAddFormList
	 *            true ��ʾֻ���ӵ���ʽ���У������޸�����ģ�͡�false��ʾ�����ӵ���ʽ���У����޸�����ģ��
	 * @return
	 */
	private boolean addDynFormula(StringBuffer showMessage, String strDynPK,
			IArea area, String strFormula, boolean bUserDef, IParsed dbLet,
			int type, boolean bPublic, boolean bOnlyAddFormList,boolean bClearOldFormula)
			throws ParseException
	{

		if (strDynPK == null || area == null)
			return false;

		if ((strFormula == null || strFormula.trim().length() == 0)
				&& dbLet == null)
			return false;

		// MeasureVO mVO =
		// getDynAreaModel().getMeasureModel().getMeasureVOByPos(cell);
		// if (mVO == null) {
		// //ָ���ĵ�Ԫ�ϱ�����ָ����Ϣ
		// // throw new ParseException("miufo1000977"); //"��̬������ֻ�ܶ���ָ�깫ʽ")
		// return false;
		// }

		String strDbFormula = null;
		try
		{
			// add by ljhua 2005-3-18 �޷�����ⲿ���������Է�������ʱ�رռ�鿪��
			turnOffExtFuncCheck();

			// if (strFormula == null) {
			// strDbFormula = dbLet.toString();
			// }
			// ��̬������Ҫ���û�������
			if (getExecutorEnv().getDynArea() == null
					|| !(getExecutorEnv().getDynArea().equals(strDynPK)))
			{
				getExecutorEnv().setDynAreaInfo(strDynPK, null);
			}

			if (bUserDef == true)
			{
				if (dbLet == null)
				{
					IParsed objUserLet = getFormulaProxy().parseUserDefExpr(
							strFormula);
					if (MultiLangUtil.LANGCODE_ZH
							.equals(MultiLangUtil.LocalLan))
					{
						dbLet = objUserLet;
					} else
					{
						strDbFormula = objUserLet.toString(getExecutorEnv());
						dbLet = getFormulaProxy().parseExpr(strDbFormula);
					}
				}
			} else
			{
				strDbFormula = strFormula;
				if (dbLet == null)
					dbLet = getFormulaProxy().parseExpr(strDbFormula);
			}

			if (strDbFormula == null)
				strDbFormula = dbLet.toString(getExecutorEnv());

			if (bOnlyAddFormList == false)
			{
				// ɾ��ԭ�й�ʽ
				// @edit by wangyga at 2008-12-31,����04:42:24
				// ��̬��ͬʱ���幫�й�ʽ�͸��Ի���ʽʱ���˴�������ᵼ��ֻ�����ϸ��Ի���ʽ��
				// clearDynFormula(showMessage, strDynPK, area, bCell);
			}

			// �����¹�ʽ��������Ҫ����ʽ���뵽��ʽ����
			/*
			 * if (getDynFormList(strDynPK, bCell) == null) { // ������ʽ��
			 * setupDynFormulaList(strDynPK, bCell); } Vector
			 * vecReferringDynArea = MeasFormulaUtil.getReferringMeasArea(
			 * dbLet, MeasFormulaUtil.CHECK_EXP_MSELECT_COND, getExecutorEnv());
			 * boolean bAdd = getDynFormList(strDynPK, bCell).addFormula(area,
			 * vecReferringDynArea);
			 */

			// �����̬��ԭ�й�ʽ
			if (bClearOldFormula == true)
			{
				if(bPublic){
					clearDynFormula(showMessage, strDynPK, area, type, FormulaModel.CALC_FML_PUBLIC);
				} else {
					clearDynFormula(showMessage, strDynPK, area, type, FormulaModel.CALC_FML_PERSONAL);
				}
			}

			boolean bAdd = true;

			if (bAdd == true && bOnlyAddFormList == false)
			{
				//�޸��ڴ����
//				FormulaVO fVO = new FormulaVO(getReportPK(), strDbFormula,
//						dbLet);
				FormulaVO fVO = new FormulaVO(getReportPK(), strDbFormula,dbLet);
				fVO.setUserDefContent(dbLet.toUserDefString(getCalcEnv()));
				getFormulaModel().setDynFmlVO(strDynPK, area, fVO, type,
						bPublic);

				// ��������ģ�����ù�ϵ
				addRefSmtDef(fVO.getId(),dbLet);
			}
			return bAdd;
		} catch (ParseException e)
		{
			AppDebug.debug(strDbFormula + " is wrong", e);
			throw e;
		} finally
		{
			turnOnExtFuncCheck();
		}
	}

	/**
	 * ������̬����Ԫ��ʽ������
	 *
	 * @create by liuchuna at 2010-5-6,����05:00:42
	 *
	 * @param strDynPK
	 */
	private void setupDynCellFmlList(String strDynPK)
	{
		Vector<Vector<IArea>> fmlList = getDynCellFormList(strDynPK);
		if (fmlList == null)
		{
			fmlList = new Vector<Vector<IArea>>();
			if (dynCellFmlMap == null)
			{
				dynCellFmlMap = new Hashtable<String, Vector<Vector<IArea>>>();
			}
			dynCellFmlMap.put(strDynPK, fmlList);
		} else
		{
			fmlList.clear();
		}
		Hashtable<IArea, FormulaVO> hashDynFmls = getFormulaModel().getDynFmls(
				strDynPK, FormulaModel.TYPE_CELL_FML);
		setupDynFormulaList(strDynPK, fmlList, hashDynFmls, FormulaModel.TYPE_CELL_FML);

		// �����в�νṹ�Ĺ�ʽ��
		setupGradeFmlList(hashDynFmls, fmlList);
	}

	/**
	 * ������̬�����ܹ�ʽ������
	 *
	 * @create by liuchuna at 2010-5-6,����05:01:13
	 *
	 * @param strDynPK
	 */
	private void setupDynTotleFmlList(String strDynPK)
	{
		Vector<Vector<IArea>> fmlList = getDynTotleFmList(strDynPK);
		if (fmlList == null)
		{
			fmlList = new Vector<Vector<IArea>>();
			if (dynTotleFmlMap == null)
			{
				dynTotleFmlMap = new Hashtable<String, Vector<Vector<IArea>>>();
			}
			dynTotleFmlMap.put(strDynPK, fmlList);
		} else
		{
			fmlList.clear();
		}
		Hashtable<IArea, FormulaVO> hashDynFmls = getFormulaModel().getDynFmls(
				strDynPK, FormulaModel.TYPE_TOTAL_FML);
		setupDynFormulaList(strDynPK, fmlList, hashDynFmls, FormulaModel.TYPE_TOTAL_FML);

		// �����в�νṹ�Ĺ�ʽ��
		setupGradeFmlList(hashDynFmls, fmlList);
	}

	/**
	 * ����ָ����̬���Ĺ�ʽ��
	 *
	 * @param strDynPK
	 *            ��̬��PK
	 * @param bCell
	 */
	private void setupDynFormulaList(String strDynPK,
			Vector<Vector<IArea>> fmlList,
			Hashtable<IArea, FormulaVO> hashDynFmls, int type)
	{

		if (hashDynFmls == null || hashDynFmls.size() == 0)
			return;

		getExecutorEnv().setDynAreaInfo(strDynPK, null);

		initformulaListLevel(hashDynFmls, true, type, false);

	}

	/**
	 * �������С�ɾ���С���Ԫ�ƶ��������еȲ���ʱ��ˢ�¶�̬����ʽ�������޸��������ʽ���ݡ�
	 *
	 * @param startPos
	 *            ��ʼ���л���.��0Ϊ��ʼλ�ã�����ʾ��һ�С���.
	 * @param num
	 *            ������
	 * @param operation
	 *            �������ͣ�0 ��ɾ���� 1 ��ɾ���� 2�в��룬 3 �в��룻4 �н����� 5 �н��� ,
	 *            6���Ҳ��뵥Ԫ��startPosΪ����λ�õ��кţ�numΪ����λ�õ��кţ� �� 7
	 *            ���²��뵥Ԫ��startPosΪ����λ�õ��кţ�numΪ����λ�õ��кţ��� 8
	 *            ����ɾ����Ԫ��startPosΪ����λ�õ��кţ�numΪ����λ�õ��кţ��� 9
	 *            ����ɾ����Ԫ��startPosΪ����λ�õ��кţ�numΪ����λ�õ��кţ� �� operation���Ͷ���μ����ྲ̬������
	 * @param strDynPK
	 *            ��̬��PK
	 * @param bCell
	 */
	private void updateDynFormulaList(int startPos, int num, int operation,
			String strDynPK, int type)
	{
		// UfoFormulaList formulaList = getDynFormList(strDynPK, bCell);
		// UfoFormulaList formulaList = null;
		// if (formulaList == null)
		// {
		// formulaList = new UfoFormulaList(getCellModel());
		// if (bCell == true)
		// {
		// if (dynCellFmlMap == null)
		// dynCellFmlMap = new Hashtable();
		// dynCellFmlMap.put(strDynPK, formulaList);
		// } else
		// {
		// if (m_oTotalDynFormulaList == null)
		// m_oTotalDynFormulaList = new Hashtable();
		// m_oTotalDynFormulaList.put(strDynPK, formulaList);
		// }
		// } else
		// formulaList.clear();

		// �����ⲿ��ʽ�﷨���
		turnOffExtFuncCheck();

		Hashtable hashDynFmls = getFormulaModel().getDynFmls(strDynPK, type);
		if (hashDynFmls == null || hashDynFmls.size() == 0)
			return;

		getExecutorEnv().setDynAreaInfo(strDynPK, null);

		// �����й�ʽ���������й�ʽ��������ʽ����
		int iMaxRow = getCellModel().getRowNum();
		int iMaxCol = getCellModel().getColNum();
		Iterator iter = hashDynFmls.keySet().iterator();
		IArea areaKey = null;
		FormulaVO fVO = null;
		IParsed parsedLet = null;
		while (iter.hasNext())
		{
			areaKey = (IArea) iter.next();
			fVO = (FormulaVO) hashDynFmls.get(areaKey);
			String content = fVO.getFormulaContent();
			if (content == null || content.length() == 0)
			{
				fVO.setErrorFml(false);
				continue;
			}

			boolean isCorrect = true;
			try
			{
				parsedLet = fVO.getLet();
				if (fVO.getLet() == null)
				{
					parsedLet = getFormulaProxy().parseExpr(content);
					fVO.setRelative(AreaFormulaUtil.isRelativeFormula(parsedLet));
//					fVO.setLet(parsedLet);
				} else
				{
					// //���ڰ����ⲿҵ�����Ĺ�ʽ,ֻ�е�ͬע��ɹ�������Դ��ͬʱ,����Ч
					// if(isValidFormula(fVO)==false)
					// isCorrect=false;
				}

				// ����Ƿ�����������ʾ, modify chxw ���Ӷ�̬����Թ�ʽ֧��
				if (fVO.isRelative())
				{
					isCorrect = AreaFormulaUtil.updateRelativeArea(parsedLet,
							startPos, num, operation, iMaxRow, iMaxCol);
					// �õ��仯�����ȷ��ʽ������
					if (isCorrect)
					{
						if (fVO.getLet() == null)
						{
							if(parsedLet instanceof UfoCmdLet){
								((UfoCmdLet) parsedLet).getLetList().setElementAt(
										new UfoEElement(UfoEElement.OPR,
												new UfoFullArea(areaKey)), 0);
							}
						}
						fVO.setFormulaContent(parsedLet
								.toString(getExecutorEnv()));
					}
				}
//				fVO.setLet(parsedLet);

				// ���ӵ���ʽ��
				if (isCorrect == true)
				{
					// Vector alist = MeasFormulaUtil.getReferringMeasArea(
					// parsedLet, MeasFormulaUtil.CHECK_EXP_MSELECT_COND,
					// getExecutorEnv());
					// if (formulaList.addFormula(areaKey, alist) == false)
					// {
					// isCorrect = false;
					// }
				}

			} catch (Exception e)
			{
				AppDebug.debug(fVO.getFormulaContent() + " is wrong", e);
				isCorrect = false;
			}
			if (!isCorrect)
			{
				fVO.setErrorFml(true);
				// fVO.setLet(null);
				// fVO.setDriverType(FormulaVO.NO_DEFINE);
			} else
				fVO.setErrorFml(false);

		}

		turnOnExtFuncCheck();
		// ˢ��ҳ��ģ��
		// getFormulaModel().syncCellsModel();

	}

	/**
	 * ��������Ԫ��ʽ������
	 *
	 * @create by liuchuna at 2010-5-6,����05:02:41
	 *
	 */
	private void setupMainCellFmlList() {
		// �����������Ԫ��ʽ
		Map<IArea, FormulaVO> mapMainFormula = getFormulaModel().getMainFmls(FormulaModel.TYPE_CELL_FML);

		// ��ʼ����ʽ������ʽ����Ϊ�ռ�
		if (mainCellFmlList == null) {
			mainCellFmlList = new Vector<Vector<IArea>>();
		} else {
			mainCellFmlList.clear();
		}

		// construct level formula list
		constructLevelFmlList(mainCellFmlList, mapMainFormula, false,FormulaModel.TYPE_CELL_FML);
	}

	/**
	 *
	 * @create by liuchuna at 2011-7-8,����02:55:20
	 *
	 * @param mapMainFormula
	 *            ������ʽ���Ĺ�ʽ����
	 * @param innerProcess
	 *            ������ʽ���Ĺ����У��ж�������ѭ���Ƿ���mapMainFormula�ڲ�����
	 *            true������mapMainFormula�ڲ�����������ѭ��
	 *            false������������ʽģ���д���������ѭ��
	 */
	private void constructLevelFmlList(Vector<Vector<IArea>> levelFmlList, Map<IArea, FormulaVO> mapMainFormula, boolean innerProcess,int fmlType) {
		// ������������Ԫ��ʽ�Ĳ�μ���
		initMainFormulaLevel(mapMainFormula, fmlType, innerProcess);

		// �����в�νṹ�Ĺ�ʽ��
		setupGradeFmlList(mapMainFormula, levelFmlList);
	}

	/**
	 * ����������ܹ�ʽ������
	 *
	 * @create by liuchuna at 2010-5-6,����05:02:58
	 *
	 */
	private void setupMainTotleFmlList() {

		// �������������ܹ�ʽ
		Map<IArea, FormulaVO> mapMainFormula = getFormulaModel().getMainFmls(FormulaModel.TYPE_TOTAL_FML);

		// ��ʼ����ʽ������ʽ����Ϊ�ռ�
		if (mainTotleFmlList == null) {
			mainTotleFmlList = new Vector<Vector<IArea>>();
		} else {
			mainTotleFmlList.clear();
		}

		// construct level formula list
		constructLevelFmlList(mainTotleFmlList, mapMainFormula, false,FormulaModel.TYPE_TOTAL_FML);

//		if (getMainTotleFormList() == null)
//		{
//			mainTotleFmlList = new Vector<Vector<IArea>>();
//		} else
//		{
//			getMainTotleFormList().clear();
//		}

//		initMainFormulaLevel(mapMainFormula, false);
//
//		// �����в�νṹ�Ĺ�ʽ��
//		setupGradeFmlList(mapMainFormula, mainTotleFmlList);
	}

	/**
	 * @create by liuchuna at 2010-5-6,����05:05:27
	 *
	 * @param mapMainFormula
	 */
	private void setupGradeFmlList(Map<IArea, FormulaVO> mapMainFormula,
			Vector<Vector<IArea>> mainCellFmlList)
	{
		Iterator<Map.Entry<IArea, FormulaVO>> iter = mapMainFormula.entrySet()
				.iterator();
		IArea areaKey = null;
		FormulaVO fVO = null;
		while (iter.hasNext())
		{
			Map.Entry<IArea, FormulaVO> entry = iter.next();
			areaKey = entry.getKey();
			fVO = entry.getValue();
			fVO.setChecked(false);

			int level = fVO.getFmlLevel();
			if (mainCellFmlList.size() < level + 1)
			{
				mainCellFmlList.setSize(level + 1);
			}
			Vector<IArea> areaVector = mainCellFmlList.get(level);
			if (areaVector == null)
			{
				areaVector = new Vector<IArea>();
				mainCellFmlList.setElementAt(areaVector, level);
			}
			areaVector.add(areaKey);
		}
	}

	/**
	 * ����Ԫ��ʽ�������
	 *
	 * @create by liuchuna at 2010-5-5,����11:04:45
	 *
	 */
	private void initMainFormulaLevel(Map<IArea, FormulaVO> mapMainFormula, int type, boolean innerProcess)
	{
		if (mapMainFormula == null || mapMainFormula.size() == 0)
			return;

		// ��ʼ�����й�ʽ�ڹ�ʽ���еĲ��
		initformulaListLevel(mapMainFormula, false, type, innerProcess);
	}

	/**
	 * �������й�ʽ����ʼ��ÿ����ʽ�Ĳ��
	 *
	 * @create by liuchuna at 2010-5-5,����11:19:48
	 *
	 * @param mapFormula
	 */
	private void initformulaListLevel(Map<IArea, FormulaVO> mapFormula,
			boolean isDynArea, int type, boolean innerProcess)
	{
		if (mapFormula == null || mapFormula.size() == 0)
			return;

		// �����ⲿ��ʽ�﷨���
		turnOffExtFuncCheck();

		// �����й�ʽ���������й�ʽ��������ʽ����
		Iterator<Map.Entry<IArea, FormulaVO>> iter = mapFormula.entrySet().iterator();
		// ����У��ѭ����ʽ���洢��ʽ̬��λ��
		Vector<String> areaVec = new Vector<String>();
		IArea areaKey = null;
		FormulaVO fVO = null;

		if(!isFormateState()) {
			while (iter.hasNext()) {
				Map.Entry<IArea, FormulaVO> entry = iter.next();
				areaKey = entry.getKey();
				fVO = entry.getValue();

				try {
					// �����ʽ����Ϊ�գ��������ʽ,�����ù�ʽ�������
					if (fVO.getLet() == null) {
						CellsModel formatModel=DynAreaUtil.getDataModelWithExModel(getDataModel());
//						IArea realArea = DynAreaUtil.getRealArea(areaKey, getDataModel());
						IParsed parsedLet = null;
						if (isInDynArea(areaKey, formatModel)) {
							parsedLet = getFormulaProxy().parseExpr(fVO.getFormulaContent());
						} else {
							parsedLet = getFormulaProxy().parseFormula(areaKey, fVO.getFormulaContent());
						}
						fVO.setLet(parsedLet);
					}
				} catch (Exception e) {
					AppDebug.debug(e);
					AppDebug.debug(fVO.getFormulaContent() + " is wrong", e);
					fVO.setErrorFml(true);
				}
			}
		}

		iter = mapFormula.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<IArea, FormulaVO> entry = iter.next();
			areaKey = entry.getKey();
			fVO = entry.getValue();

			if (!fVO.isChecked()){
				int level = getFormulaLevel(areaKey, fVO, mapFormula, isDynArea, areaVec, type, innerProcess);
				fVO.setFmlLevel(level);
			}
		}

		turnOnExtFuncCheck();
	}

	/**
	 * ȡ��ĳ����ʽ�ļ���
	 *
	 * @create by liuchuna at 2010-5-6,����05:07:46
	 *
	 * @param areaKey
	 * @param fVO
	 * @param mapFormula
	 * @return
	 */
	private int getFormulaLevel(IArea areaKey, FormulaVO fVO,
			Map<IArea, FormulaVO> mapFormula, boolean isDynArea, Vector<String> areaVec, int type, boolean innerProcess)
	{
		String content = fVO.getFormulaContent();
		fVO.setChecked(true);
		if (content == null || content.length() == 0) {
			fVO.setErrorFml(false);
			return 0;
		}
		boolean isCorrect = true;
		int level = 0;
		try {
			// ����λ�ü��뵽vector�У������ж�ѭ��
			addAreaIntoVector(areaVec, areaKey);

			IParsed parsedLet = fVO.getLet();
			if (parsedLet == null) {
				IArea realArea = DynAreaUtil.getRealArea(areaKey, getDataModel());
				if (isInDynArea(realArea, getDataModel())) {
					// liuchun 20100118 �޸ģ�����ǹ����̶�����ʽ���������ö�̬������ֹͣ������
					if(isDynArea) {
						parsedLet = getFormulaProxy().parseExpr(content);
					} else {
						removeAreaFromVector(areaVec, areaKey);
						return level;
					}
				} else {
					parsedLet = getFormulaProxy().parseFormula(areaKey, content);
				}
				// ������ȷ����Ϊ��ȷ��ʽ
				fVO.setErrorFml(false);
			}
			//�޸��ڴ����
			if(isFormateState() && parsedLet!=null){
				fVO.setUserDefContent(parsedLet.toUserDefString(getCalcEnv()));
			}
			if (isCorrect == true) {
				level = getFormulaLevel(parsedLet, fVO, mapFormula, isDynArea, areaVec, type, innerProcess);
				fVO.setFmlLevel(level);
			}

			// �˳�֮�󣬽�λ����Ϣ����
			removeAreaFromVector(areaVec, areaKey);
		} catch (Exception e) {
			// liuweiu 2015-05-27 �򿪱����Ǵ���ʽУ��
			fVO.setErrorFml(true);
			AppDebug.debug(fVO.getFormulaContent() + " is wrong", e);
		} finally {
			removeAreaFromVector(areaVec, areaKey);
		}

		return level;
	}

	private void addAreaIntoVector(Vector<String> areaVec,IArea area){
		if(area != null){
			CellPosition[] cells = area.split();
			for(CellPosition cell : cells){
				areaVec.add(cell.toString());
			}
		}
	}

	private void removeAreaFromVector(Vector<String> areaVec,IArea area){
		if(area != null){
			CellPosition[] cells = area.split();
			for(CellPosition cell : cells){
				if(areaVec.contains(cell.toString())){
					int index = areaVec.lastIndexOf(cell.toString());
					if(index >= 0){
						areaVec.remove(index);
					}
				}
			}
		}
	}

	/**
	 * �ж�ָ�������Ƿ��Ƕ�̬��
	 *
	 * @create by liuchuna at 2010-5-7,����09:58:30
	 *
	 * @param area
	 * @param dataModel
	 * @return
	 */
	private boolean isInDynArea(IArea area, CellsModel dataModel) {
		// liuchun 20110429 ����ȡ���е���չ�����е���չ����������չ��
		ExtendAreaCell[] dynArea = DynamicAreaModel.getInstance(dataModel).getDynAreaCells();
		if(dynArea != null) {
			for (ExtendAreaCell extendCell : dynArea) {
				AreaPosition eachArea = extendCell.getArea();
				if (eachArea.contain(area)) {
					return true;
				}
			}
		}
		return false;
	}

	private int getFormulaLevel(IParsed parsedLet, FormulaVO fVO,
			Map<IArea, FormulaVO> mapFormula, boolean isDynArea, Vector<String> areaVec, int type, boolean innerProcess)
	{
		Vector<IArea> alist = MeasFormulaUtil.getReferringMeasArea(parsedLet,
				MeasFormulaUtil.CHECK_EXP_MSELECT_COND, getExecutorEnv());
		if (alist == null || alist.isEmpty()) {
			return 0;
		}
		int level = 0;
		// ѭ�����á�������У��
		for (IArea area : alist) {
			if (area == null) {
				continue;
			}
			CellPosition[] cells = null;
			if (area instanceof CellPosition) {
				cells = new CellPosition[] { (CellPosition) area };
			} else {
				cells = area.split();
			}
			if (cells != null && cells.length > 0) {
				for (CellPosition cell : cells) {
					// ȡ�ø�λ�ö�Ӧ�Ĺ�ʽ
					IArea realArea = DynAreaUtil.getRealArea(cell, getDataModel());

					if(!isDynArea) {
						// ��������Ĺ�ʽ��
						if(isInDynArea(realArea, getDataModel())){
							// ������̬���������򷵻�
							continue;
						}
					}

					FormulaVO formula = getFormulaByArea(mapFormula, (CellPosition) realArea, type, innerProcess);
					if(areaVec.contains(cell.toString())){//����ѭ������
						fVO.setErrorFml(true);
					}
					if (formula == null) {
						continue;
					}
					if (formula.isChecked()) {
						int tempLevel = formula.getFmlLevel();
						if (tempLevel > level)
						{
							level = tempLevel;
						}
					} else {
						int tempLevel = getFormulaLevel(cell, formula,
								mapFormula, isDynArea, areaVec, type, innerProcess);
						if (tempLevel > level) {
							level = tempLevel;
						}
					}
				}
			}
		}

		return level + 1;
	}

	/**
	 * ����λ�÷��ظõ�Ԫ�Ĺ�ʽ
	 *
	 * ���ڶ����������ϵĹ�ʽ���ø������е�һ����Ԫ���ø÷���Ҳ���Է��ظ�����Ĺ�ʽ
	 *
	 * @create by liuchuna at 2010-6-24,����11:25:49
	 *
	 * @param selPos
	 * @return
	 */
	private FormulaVO getFormulaByArea(Map<IArea, FormulaVO> mapFormula, CellPosition selPos, int type, boolean innerProcess)
	{
		// ָ���ĵ�Ԫ�񷵻�CellPosition���ͣ����ѡ�������ϵ�Ԫ���򷵻���ȷ��ѡ������ ��
		IArea selArea = getDataModel().getArea(selPos);
		IArea fmtFmlArea = DynAreaUtil.getFormatArea(selArea, getDataModel());
		DynamicAreaModel dynAreaModel = DynamicAreaModel
				.getInstance(DynAreaUtil.getDataModelWithExModel(getDataModel()));
		FormulaModel formulaModel = dynAreaModel.getFormulaModel();
		IArea realFmlArea = formulaModel.getRelatedFmlArea(fmtFmlArea, type);
		if (realFmlArea == null) {
			return null;
		}
		FormulaVO formulaVO = null;
		if(innerProcess && mapFormula != null) {
			formulaVO = mapFormula.get(realFmlArea);
		} else {
			formulaVO = formulaModel.getDirectFml(realFmlArea, type);
		}
		return formulaVO;
	}

	/**
	 * ���ָ�������ڹ�ʽ
	 *
	 * @param showMessage
	 * @param area
	 *            �������ʽ������λ��
	 * @param strDynPK
	 *            strDynPK=null��ʾ�������ʽ�����������ʾ��̬��pk
	 */
	private void clearDynFormula(StringBuffer showMessage, String strDynPK,
			IArea area, int type, int calcFmlType) {
		if (area == null)
			return;

		// ���ɾ����ʽ���򼯺�
		IArea[] areaFormulas = null;
		if (strDynPK == null)
			areaFormulas = getFormulaModel()
					.getRelatedMainFmlAreas(area, type);
		else
			areaFormulas = getFormulaModel().getRelatedDynFmlAreas(strDynPK,
					area, type);

		if (areaFormulas != null && areaFormulas.length > 0) {

			for(IArea fmlArea : areaFormulas) {
				FormulaVO fvo = getFormulaModel().getDirectFml(fmlArea, strDynPK == null? FormulaModel.MAINTABLE_DYNPK:strDynPK, type);
				IParsed parsedLet=null;
				try{
					turnOffExtFuncCheck();
					parsedLet=parseDBFml(fvo, fmlArea);
				}finally{
					turnOnExtFuncCheck();
				}
				// ���������ģ�͵�����
				clearRefSmtDef(fvo.getId(),parsedLet);
			}


			// ���¹�ʽģ��
			Vector vecDelPerson = null;
			if (strDynPK == null) {
				vecDelPerson = getFormulaModel().removeMainRelatedFmlByType(
						area, type, calcFmlType);
			} else
				vecDelPerson = getFormulaModel().removeDynRelatedFmlByType(
						strDynPK, area, type, calcFmlType);

			if (type == FormulaModel.TYPE_CELL_FML) {
				// ��ɾ�����Ի���ʽ��Ӧ�Ĺ��й�ʽ���뵽��ʽ����
				if (vecDelPerson != null && vecDelPerson.size() > 0) {
					IArea areaPublic = null;
					FormulaVO fmlPublic = null;
					try {
						for (int i = 0, size = vecDelPerson.size(); i < size; i++) {
							areaPublic = (IArea) vecDelPerson.get(i);
							if (areaPublic == null)
								continue;
							fmlPublic = getFormulaModel().getPublicDirectFml(
									areaPublic);
							if (fmlPublic == null)
								continue;

							if (strDynPK == null)
								addMainFormula(showMessage, areaPublic,
										fmlPublic.getFormulaContent(),
										fmlPublic.getLet(), false, type, true,
										true, false);
							else {
								addDynFormula(showMessage, strDynPK, areaPublic
										.getStart(), fmlPublic
										.getFormulaContent(), false, fmlPublic
										.getLet(), type, true, true,true);
							}

						}
					} catch (ParseException e) {
						AppDebug.debug(e);
					}
				}
			}
		}
	}

	/**
	 * ����ָ�������ڹ�ʽ
	 *
	 * @param strDynPK
	 *            strDynPK=null��ʾ�������ʽ�����������ʾ��̬��pk
	 * @param oldArea
	 *            �����¹�ʽ������λ��
	 * @param newArea
	 *            ��ʽ�µ�����λ��
	 * @param fmlVO
	 *            ��ʽ
	 * @param bCell
	 * @param bPublic
	 */
	private void updateDynFormula(String strDynPK, IArea oldArea,
			IArea newArea, FormulaVO fmlVO, int type, boolean bPublic)
	{
		if (oldArea == null || newArea == null)
			return;

		// ��ø��¹�ʽ���򼯺�
		IArea[] areaFormulas = null;
		if (strDynPK == null)
			areaFormulas = getFormulaModel().getRelatedMainFmlAreas(oldArea,
					type);
		else
			areaFormulas = getFormulaModel().getRelatedDynFmlAreas(strDynPK,
					oldArea, type);

		if (areaFormulas != null && areaFormulas.length > 0)
		{
			// UfoFormulaList formulaList = null;
			// if (strDynPK == null)
			// formulaList = getMainFormList(bCell);
			// else
			// formulaList = getDynFormList(strDynPK, bCell);

			// �ڹ�ʽ����ɾ���ɵĹ�ʽ
			// if (formulaList != null)
			// {
			// for (int i = 0, size = areaFormulas.length; i < size; i++)
			// {
			// if (areaFormulas[i] == null)
			// continue;
			// formulaList.delFormula(areaFormulas[i]);
			// }
			// }

			// �ڹ�ʽ���м����µĹ�ʽ
			// IParsed dbLet = fmlVO.getLet();
			// Vector alist = AreaFormulaUtil.getAreaList(dbLet);
			// formulaList.addFormula(newArea, alist);

			// ���¹�ʽģ��
			try
			{
				StringBuffer showErrMessage = new StringBuffer();
				addFormula(showErrMessage, newArea, fmlVO.getFormulaContent(),
						fmlVO.getLet(), false, type, bPublic);
			} catch (ParseException e)
			{
				AppDebug.debug(e);
			}
		}

	}

	/**
	 * ɾ����Ԫ��ʽ�Ĺ���
	 *
	 * @param area
	 *            ��ɾ����ʽ������λ��
	 * @param bCell
	 *            �Ƿ�Ԫ��ʽ��true��Ԫ��ʽ��false���ܹ�ʽ
	 * @param fmlType
	 */
	public void clearFormula(IArea area, int type, int calcFmlType) {
		StringBuffer showErrMessage = new StringBuffer();

		// 1.�������ָ�������ڹ�ʽ
		clearDynFormula(showErrMessage, null, area, type, calcFmlType);

		// ������򸲸ǵĶ�̬��
		ExtendAreaCell[] dynCells = getDynAreaModel().getDynAreaCellByArea(area);

		// 2.�����̬����ָ�������ڹ�ʽ
		if (dynCells != null && dynCells.length > 0) {
			String strDynPK = null;

			for (int i = 0, iLen = dynCells.length; i < iLen; i++) {
				if (dynCells[i] == null)
					continue;
				strDynPK = dynCells[i].getBaseInfoSet().getExAreaPK();
				if (strDynPK == null)
					continue;
				clearDynFormula(showErrMessage, strDynPK, area, type,
						calcFmlType);
			}
		}
	}

	/**
	 * ���¹�ʽ�����������λ��
	 *
	 * @param oldArea
	 * @param newArea
	 * @param fmlVO
	 * @param bCell
	 * @param bPublic
	 */
	public void updateFormulaAreaPos(IArea oldArea, IArea newArea,
			FormulaVO fmlVO, int type, boolean bPublic)
	{
		try
		{
			// 1.��������ָ������Ĺ�ʽ
			updateDynFormula(null, oldArea, newArea, fmlVO, type, bPublic);

			// ������򸲸ǵĶ�̬��
			ExtendAreaCell[] dynCells = getDynAreaModel().getDynAreaCellByArea(
					oldArea);

			// 2.���¶�̬����ָ������Ĺ�ʽ
			if (dynCells != null && dynCells.length > 0)
			{
				String strDynPK = null;
				for (int i = 0, iLen = dynCells.length; i < iLen; i++)
				{
					if (dynCells[i] == null)
						continue;
					strDynPK = dynCells[i].getBaseInfoSet().getExAreaPK();
					if (strDynPK == null)
						continue;
					updateDynFormula(strDynPK, oldArea, newArea, fmlVO, type,
							bPublic);
				}
			}
		} finally
		{
		}
	}

	public void clearTotalFormula(){
		clearAllFormula(FormulaModel.TYPE_TOTAL_FML);
	}

	/**
	 * ɾ���������й�ʽ
	 *
	 * @param bCell
	 *            �Ƿ�Ԫ��ʽ��true��ʾ��Ԫ��ʽ��false��ʾ���ܹ�ʽ
	 */
	public void clearAllFormula(int type) {
		// ɾ���������й�ʽ
		if (type == FormulaModel.TYPE_CELL_FML) {
			mainCellFmlList = null;
			dynCellFmlMap = null;
		} else {
			mainTotleFmlList = null;
			dynTotleFmlMap = null;
		}
		getFormulaModel().removeAllFml(type);
	}

	public void clearPublicFormula(){
		getFormulaModel().clearPublicFormula();
	}

	public void clearPersonFormula(){
		getFormulaModel().clearPersonFormula();
	}

	/**
	 * �������С�ɾ���С���Ԫ�ƶ��������еȲ���ʱ����ʽ�������½��������޸��������ʽ���ݡ�
	 *
	 * @param startPos
	 *            ��ʼ���л���.��0Ϊ��ʼλ�ã�����ʾ��һ�С���.
	 * @param num
	 *            ������
	 * @param operation
	 *            �������ͣ�0 ��ɾ���� 1 ��ɾ���� 2�в��룬 3 �в��룻4 �н����� 5 �н��� ,
	 *            6���Ҳ��뵥Ԫ��startPosΪ����λ�õ��кţ�numΪ����λ�õ��кţ� �� 7
	 *            ���²��뵥Ԫ��startPosΪ����λ�õ��кţ�numΪ����λ�õ��кţ��� 8
	 *            ����ɾ����Ԫ��startPosΪ����λ�õ��кţ�numΪ����λ�õ��кţ��� 9
	 *            ����ɾ����Ԫ��startPosΪ����λ�õ��кţ�numΪ����λ�õ��кţ� �� operation���Ͷ���μ����ྲ̬������
	 * @param strDynPKs
	 *            ���б䶯�󣬶�̬���׵�Ԫλ�ñ仯�Ķ�̬��pk����
	 */
	public void updateFormulas(int startPos, int num, int operation,
			String[] strDynPKs, int type)
	{
		// startPos�Ǵ�0��ʼ�ģ��޸�ΪstartPos < 0
		if (startPos < 0 || num <= 0
				|| operation < AreaFormulaUtil.OPT_DEL_ROW
				|| operation > AreaFormulaUtil.OPT_UP_REMOVE)
		{
			return;
		}

		// 1.��������
		try
		{
			// �Ӻ�̨�õ���ʽ�б��Ա㽨���޸��������̨����ͬ��
			// ע�⣬��ʱ�õ��Ĺ�ʽ��λ�ñ仯����ɣ���ReportTableBO.posChange��ɣ�����û�����������������Ĺ�ʽ���ݵ��޸�
			Map mapMainFormula = getFormulaModel().getMainFmls(type);
			// if (getMainFormList(bCell) == null)
			// {
			// m_oMainFormulaCalcList = new UfoFormulaList(getCellModel());
			//
			// } else
			// {
			// getMainFormList(bCell).clear();
			// }

			// �����ⲿ��ʽ�﷨���
			turnOffExtFuncCheck();

			Iterator iter = mapMainFormula == null ? null : mapMainFormula
					.keySet().iterator();
			IArea areaKey = null;
			FormulaVO fVO = null;

			int iMaxRow = getCellModel().getRowNum();
			int iMaxCol = getCellModel().getColNum();
			while (iter != null && iter.hasNext())
			{
				areaKey = (IArea) iter.next();
				fVO = (FormulaVO) mapMainFormula.get(areaKey);
				String content = fVO.getFormulaContent();
				if (content == null || content.length() == 0)
					continue;

				if (fVO.isErrorFml() == false)
				{
					boolean bCorrect = true;
					try
					{
						IParsed parsedFunc = fVO.getLet();
						if (parsedFunc == null)
						{
							parsedFunc = getFormulaProxy().parseFormula(
									areaKey, content);
							fVO.setRelative(AreaFormulaUtil.isRelativeFormula(parsedFunc));
						}

						// ����Ƿ�����������ʾ
						if (fVO.isRelative())
						{
							bCorrect = AreaFormulaUtil.updateRelativeArea(
									parsedFunc, startPos, num, operation,
									iMaxRow, iMaxCol);
							// �õ��仯�����ȷ��ʽ������
							if (bCorrect)
							{
								if (fVO.getLet() == null)
								{
									((UfoCmdLet) parsedFunc).getLetList()
											.setElementAt(
													new UfoEElement(
															UfoEElement.OPR,
															new UfoFullArea(
																	areaKey)),
													0);
								}
								fVO.setFormulaContent(parsedFunc
										.toString(getExecutorEnv()));
							}
						} else if(content.startsWith(StatisticFuncDriver.MROWCOUNT)) {
							// �����к�MROWCOUNT��������ʾ��Ҫ�޸�Ϊ��̬���������򣬿��ǵ�MROWCOUNT����ʹ�ó��������⴦��
							fVO.setUserDefContent(null);
						}
						//�޸��ڴ����
//						fVO.setLet(parsedFunc);

						// ���ӵ���ʽ��
						if (bCorrect)
						{
							// Vector alist = AreaFormulaUtil.getAreaList(fVO
							// .getLet());

							// if (getMainFormList(bCell).addFormula(areaKey,
							// alist) == false)
							// {
							// bCorrect = false;
							// }
						}
					} catch (Exception e)
					{
						AppDebug.debug(fVO.getFormulaContent() + "is wrong", e);

						bCorrect = false;
					}

					if (bCorrect == false)
					{
						fVO.setErrorFml(true);
						// fVO.setLet(null);
						// fVO.setDriverType(FormulaVO.NO_DEFINE);
					}
				}
			}
		} finally
		{
			turnOnExtFuncCheck();
			// getFormatModel().clearFormulaCellsHash();
		}

		// 2.����̬��
		if (strDynPKs != null && strDynPKs.length > 0)
		{
			for (int i = 0, iLen = strDynPKs.length; i < iLen; i++)
			{
				removeDynFormList(strDynPKs[i], type);
				updateDynFormulaList(startPos, num, operation, strDynPKs[i],
						type);
			}
		}
	}

	/**
	 *
	 * ִ�б��ڵ�Ԫ��ʽ�ļ���
	 * ����˳�� 1����������ʽ�� 2���������ж�̬����ʽ�� 3�����������ڰ�����ֱ�����û������ö�̬��ָ�꺯���Ĺ�ʽ
	 *
	 * @create by liuchuna at 2010-12-2,����08:48:17
	 *
	 * @param bOnlyAreaCalc
	 *            true��ʾ�������(���������ѯ���ⲿҵ����)��false��ʾ�������ݵļ���
	 * @return
	 */
	public CellsModel calcAllFormula(boolean bOnlyAreaCalc) {
		try {
			// ��¼���㻷���в������Ƿ�����ⲿ����
			String strOldValue = (String) getExecutorEnv().getExEnv(CommonExprCalcEnv.EX_CALCEXFUNC);

			// �����Ƿ�����ⲿ��������ֵ
			if (bOnlyAreaCalc) {
				getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_CALCEXFUNC, CommonExprCalcEnv.EX_VALUE_OFF);
			} else {
				getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_CALCEXFUNC, CommonExprCalcEnv.EX_VALUE_ON);
				// ����Ƿ�������㣬���б����ѯ������
				calAllReportQuerys();
			}

			// ������ʽ��
			if (getMainCellFormList() == null) {
				setupMainCellFmlList();
			}

			// ���ݹ�ʽ����������ʽ�����б��б��д洢��ʽ������Ԫ
			Vector<Vector<IArea>> areaGradeVec = getMainCellFormList();
			Vector<IArea> flist = new Vector<IArea>();
			for (Vector<IArea> vec : areaGradeVec) {
				if (vec != null) {
					flist.addAll(vec);
				}
			}

			// ���ö�̬������ָ�������ʽ��Ӧ�����򼯺�(����ֱ�����ü�������õ�����ʽ)
			Vector<IArea> vecRefDynArea = new Vector<IArea>();
			// δ���ö�̬��ָ�������ʽ�����򼯺�(����ֱ�����ü�������õ�����ʽ)
			Vector<IArea> vecNonRefDynArea = new Vector<IArea>();
			// ֱ�����û������ö�̬��������ʽ
			getRefDynAreaFml(flist, vecRefDynArea, vecNonRefDynArea, FormulaModel.TYPE_CELL_FML);
			// edit by congdy  2013.9.16 HR���ݷ�������
			if(UfoeLicenseManager.isUFHRModuleValid() && !bOnlyAreaCalc) {// �����������㣬��ִ��
				calcMainHRScheme();
			}

			// �ȼ���̶�����ı�����Ŀӳ��ȡֵ
			if(UfoeLicenseManager.isUFDSModuleValid() && !bOnlyAreaCalc) {// �����������㣬��ִ�б�����Ŀȡ��
				calcMainRepItem();
			}

			// ��������ʽ(�����ö�̬��������ʽ�ȼ���)
			calcMainFormulas(vecNonRefDynArea, FormulaModel.TYPE_CELL_FML);

			// �������ж�̬����ʽ
			calAllDynAreas(true, FormulaModel.TYPE_CELL_FML, bOnlyAreaCalc);

			// ���¼��������ڰ���ֱ�����û������ö�̬����ָ�꺯���Ĺ�ʽ
			recalcMainFormula(vecRefDynArea, FormulaModel.TYPE_CELL_FML);

			// �ָ����㻷���в������Ƿ�����ⲿ����
			getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_CALCEXFUNC, strOldValue);

			//�������ģ���еĴ洢��Ԫ����
			IUFOTableData dataChannel = (IUFOTableData) getExecutorEnv().getDataChannel();
			if(dataChannel instanceof ReportDataModel) {
				((ReportDataModel) dataChannel).clearStoreCellCache();
			}

			return getCellModel();
		} finally {
			getExecutorEnv().removeExEnv(UfoCalcEnv.KEY_DATA_SET_CACHE);
			getExecutorEnv().removeExEnv(UfoCalcEnv.KEY_DYN_KEYDATAGROUP_INDEX);
			getExecutorEnv().clearStoreCellMap();
		}
	}
	
	/**
	 * ����̶������HR���ݷ�������
	 */
	private void calcMainHRScheme() {
		try {
			getContextVO().setAttribute(IUfoContextKey.HR_CALC_AREA_PK, ITableData.MAINPEPORT);
			invokeHRCalc();
		} catch (Exception e) {
			// ֻ����쳣��Ϣ��Ϊ��Ӱ������ļ���
			AppDebug.debug(e);
		} finally {
			getContextVO().removeAttribute(IUfoContextKey.HR_CALC_AREA_PK);
		}
	}

	private void invokeHRCalc() throws Exception {
		Method execRepItemCalcMethod = Class.forName("nc.util.iufo.hr.calc.HRUfoeCalcExecutor").getMethod("executeCalc", IContext.class);
		execRepItemCalcMethod.invoke(getHRCalcExecutor(), getContextVO());
	}
	
	private Object getHRCalcExecutor() throws Exception {
		if(hrCalcExecutor == null) {
			Constructor<?> constructor = (Constructor<?>) Class
					.forName("nc.util.iufo.hr.calc.HRUfoeCalcExecutor").getConstructor(CellsModel.class);
			hrCalcExecutor = constructor.newInstance(getCellModel());
		}
		// reset cellsmodel
		ClassLoader clsLoader = this.getClass().getClassLoader();
		Class c = clsLoader.loadClass("nc.util.iufo.hr.calc.HRUfoeCalcExecutor");
		Method setM = c.getMethod("setCellsModel", new Class[] { CellsModel.class });
		setM.invoke(hrCalcExecutor, getCellModel());
		return hrCalcExecutor;
	}
	
	private void setHRCalcExecutorFmlExt() throws Exception {
		Method setEnvMethod = Class.forName("nc.util.iufo.hr.calc.HRUfoeCalcExecutor").getMethod("setFmlExecutor", this.getClass());
		setEnvMethod.invoke(getHRCalcExecutor(), this);
	}

	private void setHRCalcExecutorCtx() throws Exception {
		Method setEnvMethod = Class.forName("nc.util.iufo.hr.calc.HRUfoeCalcExecutor").getMethod("setContext", IContext.class);
		setEnvMethod.invoke(getHRCalcExecutor(), getContextVO());
	}

	/**
	 * ����̶�����ı�����Ŀ����
	 */
	private void calcMainRepItem()  {
		try{
			getContextVO().setAttribute(IUfoContextKey.DI_CALC_AREA_PK, ITableData.MAINPEPORT);
			invokeRepItemCalc();
		} catch(Exception e){
			// ֻ����쳣��Ϣ��Ϊ��Ӱ������ļ���
			AppDebug.debug(e);
		} finally {
			getContextVO().removeAttribute(IUfoContextKey.DI_CALC_AREA_PK);
		}
	}

	private Object getDICalcExecutor() throws Exception {
		if(diCalcExecutor == null) {
			Constructor<?> constructor = (Constructor<?>) Class
					.forName("nc.util.ufoe.intdata.calc.UfoeDICalcExecutor").getConstructor(CellsModel.class);
			diCalcExecutor = constructor.newInstance(getCellModel());
		}
		// reset cellsmodel
		ClassLoader clsLoader = this.getClass().getClassLoader();
		Class c = clsLoader.loadClass("nc.util.ufoe.intdata.calc.UfoeDICalcExecutor");
		Method setM = c.getMethod("setCellsModel", new Class[] { CellsModel.class });
		setM.invoke(diCalcExecutor, getCellModel());
		
		return diCalcExecutor;
	}
	
	private void invokeRepItemCalc() throws Exception {
		Method execRepItemCalcMethod = Class.forName("nc.util.ufoe.intdata.calc.UfoeDICalcExecutor").getMethod("execRepItemCalc", IContext.class);
		execRepItemCalcMethod.invoke(getDICalcExecutor(), getContextVO());
	}

	private void setDICalcExecutorFmlExt() throws Exception {
		Method setEnvMethod = Class.forName("nc.util.ufoe.intdata.calc.UfoeDICalcExecutor").getMethod("setFmlExecutor", this.getClass());
		setEnvMethod.invoke(getDICalcExecutor(), this);
	}

	private void setDICalcExecutorCtx() throws Exception {
		Method setEnvMethod = Class.forName("nc.util.ufoe.intdata.calc.UfoeDICalcExecutor").getMethod("setContext", IContext.class);
		setEnvMethod.invoke(getDICalcExecutor(), getContextVO());
	}

	private void getRefDynAreaFml(Vector vecFormulaList,Vector vecValidArea,Vector<IArea> vecNonRefFormulaArea, int type) {
		if (vecFormulaList == null || vecFormulaList.size() == 0)
			return;

		ExtendAreaCell[] dynAreas = getDynAreaModel().getDynAreaCells();
		if (dynAreas == null || dynAreas.length == 0) {
			vecNonRefFormulaArea.addAll(vecFormulaList);
			return;
		}

		//tianchuan 20140928 �޸ĸۻ�����
		Set<IArea> nonRefSet=new HashSet<IArea>();
		Set<IArea> validSet=new HashSet<IArea>();
		
		Map mapMainFormula = getFormulaModel().getMainFmls(type); // ��������ʽ
		vecNonRefFormulaArea.addAll(vecFormulaList);

		FormulaVO oFormula = null;
		IParsed formulaLet = null;
		int nFormulas = vecFormulaList.size();
		for (int i = 0; i < nFormulas; i++)
		{
			oFormula = getMainFmlByArea((IArea) (vecFormulaList.get(i)),
					mapMainFormula);
			if (oFormula == null)
			{
				continue;
			}
			formulaLet = oFormula.getLet();
			if (formulaLet == null)
				continue;
			
			//tianchuan 2013.5.17 �޸� ��ȷ�ĵõ����ö�̬������ĺ���
			Vector<IArea> alist = MeasFormulaUtil.getReferringMeasArea(formulaLet,
					MeasFormulaUtil.CHECK_EXP_MSELECT_COND, getExecutorEnv());
			if(alist!=null && alist.size()>0){
				CellsModel formatModel=DynAreaUtil.getDataModelWithExModel(getDataModel());
				DynamicAreaModel dynAreaMocel = DynamicAreaModel.getInstance(formatModel);
				IArea tempArea=null;
				Iterator<IArea> it=alist.iterator();
				while(it.hasNext()){
					tempArea=it.next();
					if(tempArea == null) {
						continue;
					}
					if (dynAreaMocel.isInDynArea(tempArea.getStart()) || validSet.contains(tempArea)){	//����Ƕ�׵����
						if(!validSet.contains(vecFormulaList.get(i))) {
							validSet.add((IArea)vecFormulaList.get(i));
//							validSet.addElement(vecFormulaList.get(i));
							vecNonRefFormulaArea.remove(vecFormulaList.get(i));
						}
					}
				}
			}
			//tianchuan 2013.5.17 ����
			
//			ArrayList listMeasFunc = new ArrayList();
//			CmdProxy.getElementsByClass(formulaLet, listMeasFunc,
//					MeasFunc.class);
//			if (listMeasFunc.size() > 0)
//			{
//				for (int j = 0; j < listMeasFunc.size(); j++)
//				{
//					MeasFunc measFunc = (MeasFunc) listMeasFunc.get(j);
//					if (measFunc.isMeasReferDynArea(getExecutorEnv()))
//					{
//						if(!vecValidArea.contains(vecFormulaList.get(i))) {
//							vecValidArea.addElement(vecFormulaList.get(i));
//							vecNonRefFormulaArea.remove(vecFormulaList.get(i));
//						}
//					}
//				}
//			} else
//			{// �������ʽ������ʽ�����ö�̬��������빫ʽ��
//				if (isMainFormulaRefDynArea(formulaLet))
//				{
//					if(!vecValidArea.contains(vecFormulaList.get(i))) {
//						vecValidArea.addElement(vecFormulaList.get(i));
//						vecNonRefFormulaArea.remove(vecFormulaList.get(i));
//					}
//				}
//			}
		}

		/**
		 * ����ֱ�����ù�ʽ�Ƿ�������ֱ�����ù�ʽ
		 */
		for (; toCheckFormulaAreaList(vecNonRefFormulaArea, validSet, type);)
		{
		}
		//tianchuan 20140928 �޸ĸۻ�����
		nonRefSet.addAll(vecNonRefFormulaArea);
		IArea area=null;
		for(Object obj : vecFormulaList){
			area=(IArea)obj;
			if(!nonRefSet.contains(area)){
				vecValidArea.add(area);
			}
		}
	}

	private void calcMainFormulas(Vector flist, int type) {
		try{
			// ����������й�ʽ
			Map mapMainFormula = getFormulaModel().getMainFmls(type);

			calcFormulas(mapMainFormula, flist, type);
		} finally {
			getExecutorEnv().removeExEnv(UfoCalcEnv.KEY_DYN_KEYDATAGROUP_INDEX);
		}
	}

	/**
	 * ��������ʽ
	 *
	 * @create by liuchuna at 2010-12-2,����08:55:16
	 *
	 * @param flist
	 *            ��ʽ������Ԫ�б����ݴ��б���˳����м���
	 * @param bCell
	 *            �Ƿ�Ԫ��ʽ��true=��Ԫ��ʽ���㣬false=���ܹ�ʽ����
	 */
	private void calcFormulas(Map mapMainFormula, Vector flist, int type) {

		if (flist != null && flist.size() > 0) {

			// ��������ʽ�������̬����Ϣ
			getExecutorEnv().setDynAreaInfo(null, null);

			// ���Ƚ��з��飬�����ⲿҵ�����޹صĹ�ʽ���Ƚ��м��㰴
//			ArrayList[] separated = separateMainFormulas(flist);

			int nCalced = 0;
			IFormat format = null;
//			for (int i = 0; i < separated.length; i++) {
//				if (separated[i] != null && separated[i].size() > 0) {
//					int n = separated[i].size();
					int n = flist.size();
					UfoCmdLet[] objLets = new UfoCmdLet[n];
					IArea[] areaTemps = new IArea[n];
					for (int j = 0; j < n; j++) {
//						IArea areaKey = (IArea) (separated[i].get(j));
						IArea areaKey = (IArea) (flist.get(j));
						FormulaVO oFormula = getMainFmlByArea(areaKey,
								mapMainFormula);
						if (oFormula == null) {
							continue;
						}
						areaTemps[j] = areaKey;
						objLets[j] = (UfoCmdLet) oFormula.getLet();
					}
					try {
//						CmdProxy.preCalcExtFunc(objLets, i == 0 ? null
//								: separated[0], getExecutorEnv(), 3);
						CmdProxy.preCalcExtFunc(objLets, null, getExecutorEnv(), 3);
					} catch (Exception e) {
						AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0477")/*@res "Ԥ�����������"*/, e);
					}
					// ����
					FormulaVO oFormulaTemp = null;
					String calcFmlStr = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0908");/*@res "���㹫ʽ[area="*/
					String fmlContStr = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0909");/*@res "],��ʽ����["*/
					for (int j = 0; j < objLets.length; j++) { // �����й�ʽ���м���
						UfoCmdLet formParsed = objLets[j]; // �õ���ʽ����
						if (formParsed != null) {
							try {
								// @edit by wangyga at 2009-1-13,����04:26:54
								// ������㲻�������ݼ�����
								if (!isCalcDataSetFunc(formParsed)) {
									continue;
								}

								format = getDataModel().getRealFormat(areaTemps[j].getStart());
								if (format != null && format.getCellType() == TableConstant.CELLTYPE_BIGNUMBER) {
									// ����ֵ���͵�,��Ҫ���⴦��
									getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_IS_BIGNUMBER, true);
								} else {
									getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_IS_BIGNUMBER, false);
								}

								getFormulaProxy().runFormula(formParsed, getExecutorEnv());

								if(Logger.isDebugEnabled()) {
									StringBuffer strBufInfo = new StringBuffer();
									strBufInfo.append(calcFmlStr);
									strBufInfo.append(areaTemps[j] == null ? "" : areaTemps[j].toString());
									strBufInfo.append(fmlContStr);
									if (areaTemps[j] != null) {
										oFormulaTemp = (FormulaVO) mapMainFormula
												.get(areaTemps[j]);
										if (oFormulaTemp != null)
											strBufInfo.append(oFormulaTemp
													.getFormulaContent());
									}
									strBufInfo.append("]");
									AppDebug.info(strBufInfo.toString());
								}
							} catch (CmdException e) {
								String strFml = formParsed.toUserDefString(getExecutorEnv());

								StringBuffer strBufInfoErr = new StringBuffer();
								strBufInfoErr.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0908")/*@res "���㹫ʽ[area="*/);
								strBufInfoErr.append(areaTemps[j] == null ? ""
										: areaTemps[j].toString());
								strBufInfoErr.append("]");
								strBufInfoErr.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0910")/*@res "���㹫ʽ ["*/);
								strBufInfoErr.append(strFml);
								strBufInfoErr.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0911")/*@res "] ����"*/);
								AppDebug.error(strBufInfoErr.toString());// error����������������Ϣ
								nc.bs.logging.Logger.debug(e);// debug���������ջ��Ϣ
							} catch (Throwable e) {
								AppDebug.debug(e.getMessage(), e);// Throwable error���������ջ��Ϣ
							}
						}
					}
					CmdProxy.clearPreCalcValues(objLets, getExecutorEnv());
					nCalced += n;
				}
			}
//		}
//	}

	/**
	 * �����ⲿҵ�������й�ʽ�����б�ķָ�
	 *
	 * @param flist
	 *            Vector ��Ϊ��
	 * @return �����ȼ���Ĺ�ʽ����
	 */
	private ArrayList[] separateMainFormulas(Vector<IArea> flist)
	{
		// ��ǰҵ�����������ڣ������ⲿҵ����������Ϊon
		Object extFuncDriver = getExecutorEnv().loadFuncListInst()
				.getExtFuncDriver();
		ArrayList[] separated = new ArrayList[2];
		if (extFuncDriver != null
				&& (getExecutorEnv().getExEnv(CommonExprCalcEnv.EX_CALCEXFUNC) == null || getExecutorEnv()
						.getExEnv(CommonExprCalcEnv.EX_CALCEXFUNC).equals(
								CommonExprCalcEnv.EX_VALUE_ON)))
		{

			/**
			 * ����������й�ʽ ʹ��FormulaModel��õĹ�ʽ��Ϣ����Ӧ�Ĺ�ʽ�����Ǹ�ʽ̬����
			 */
			Map mapMainFormula = getFormulaModel().getMainFmls(FormulaModel.TYPE_CELL_FML);

			int nSize = flist.size();
			boolean bFind = false;
			ArrayList<IArea> firstList = new ArrayList<IArea>(nSize / 5);
			ArrayList<IArea> secondList = new ArrayList<IArea>(nSize / 5);
			String strDriverName = extFuncDriver.getClass().getName();
			for (int i = 0; i < nSize; i++)
			{
				IArea a = (IArea) flist.get(i);
				FormulaVO fVO = getMainFmlByArea(a, mapMainFormula);
				if (fVO == null || fVO.isErrorFml() == true)
					continue;

				if (bFind == false)
				{
					// �ҵ���һ��ʹ���ⲿҵ������
					if (CmdProxy.hasExtFunc(fVO.getLet(), strDriverName))
					{
						bFind = true;
						// modify by ljhua 2005-7-5 �������һ��ҵ�����������޹�ʱ�������һ�б���
						Vector vecList = AreaFormulaUtil.getAreaList(fVO
								.getLet());
						if (vecList.size() == 0)
						{
							firstList.add(a);
						} else
						{
							secondList.add(a);
						}
					} else
					{
						firstList.add(a);
					}
				} else
				{
					// �ҵ�û�������κ�����Ĺ�ʽ
					Vector vecList = AreaFormulaUtil.getAreaList(fVO.getLet());
					if (vecList.size() == 0)
					{
						firstList.add(a);
					} else
					{
						secondList.add(a);
					}
				}
			}
			separated[0] = firstList;
			separated[1] = secondList;
		} else
		{
			separated[0] = new ArrayList(flist);
			separated[1] = null;
		}
		return separated;
	}

	/**
	 * ���¼��������ڰ�����̬����ָ�꺯���Ĺ�ʽ����
	 *
	 * @param vecFormulaList
	 *            ����ʽ��
	 * @param pageid
	 */
//	private void recalcMainFormula(Vector vecFormulaList, int type)
//	{
//		if (vecFormulaList == null || vecFormulaList.size() == 0)
//			return;
//
//		ExtendAreaCell[] dynAreas = getDynAreaModel().getDynAreaCells();
//		if (dynAreas == null || dynAreas.length == 0)
//			return;
//
//		Map mapMainFormula = getFormulaModel().getMainFmls(type); // ��������ʽ
//		Vector vecValidArea = new Vector(); // ���ö�̬������ָ�������ʽ��Ӧ�����򼯺�(����ֱ�����ü�������õ�����ʽ)
//		Vector<IArea> vecNonRefFormulaArea = new Vector<IArea>(); // δ���ö�̬��ָ�������ʽ�����򼯺�(����ֱ�����ü�������õ�����ʽ)
//		vecNonRefFormulaArea.addAll(vecFormulaList);
//
//		FormulaVO oFormula = null;
//		IParsed formulaLet = null;
//		int nFormulas = vecFormulaList.size();
//		for (int i = 0; i < nFormulas; i++)
//		{
//			oFormula = getMainFmlByArea((IArea) (vecFormulaList.get(i)),
//					mapMainFormula);
//			if (oFormula == null)
//			{
//				continue;
//			}
//			formulaLet = oFormula.getLet();
//			if (formulaLet == null)
//				continue;
//			ArrayList listMeasFunc = new ArrayList();
//			CmdProxy.getElementsByClass(formulaLet, listMeasFunc,
//					MeasFunc.class);
//			if (listMeasFunc.size() > 0)
//			{
//				for (int j = 0; j < listMeasFunc.size(); j++)
//				{
//					MeasFunc measFunc = (MeasFunc) listMeasFunc.get(j);
//					if (measFunc.isMeasReferDynArea(getExecutorEnv()))
//					{
//						vecValidArea.addElement(vecFormulaList.get(i));
//						vecNonRefFormulaArea.remove(vecFormulaList.get(i));
//					}
//				}
//			} else
//			{// �������ʽ������ʽ�����ö�̬��������빫ʽ��
//				if (isMainFormulaRefDynArea(formulaLet))
//				{
//					vecValidArea.addElement(vecFormulaList.get(i));
//					vecNonRefFormulaArea.remove(vecFormulaList.get(i));
//				}
//			}
//		}
//
//		/**
//		 * ����ֱ�����ù�ʽ�Ƿ�������ֱ�����ù�ʽ
//		 */
//		for (; toCheckFormulaAreaList(vecNonRefFormulaArea, vecValidArea, type);)
//		{
//		}
//
//		// ���¼�������ʽ
//		calcMainFormulas(vecValidArea, type);
//
//	}

	private void recalcMainFormula(Vector vecValidArea, int type) {
		// ���¼�������ʽ
		calcMainFormulas(vecValidArea, type);

	}

	/**
	 * �����������ʽ�Ƿ����ö�̬��
	 *
	 * @param formulaLet
	 * @return
	 */
	private boolean isMainFormulaRefDynArea(IParsed formulaLet)
	{
		List allExpr = new ArrayList();
		if (formulaLet instanceof UfoCmdLet)
		{
			((UfoCmdLet) formulaLet).getAllExprsAndNoMeasCord(allExpr);
		} else
		{
			formulaLet.getAllExprs(allExpr);
		}
		List<UfoExpr> listAllExpr = getCalaElemFromExpr(allExpr,
				getExecutorEnv());
		DynamicAreaModel dynAreaMocel = DynamicAreaModel.getInstance(this
				.getCellModel());
		boolean isContinue = true;
		for (int index = 0; index < listAllExpr.size() && isContinue; index++)
		{
			UfoExpr expr = listAllExpr.get(index);
			ArrayList<IArea> funcAreaList = getUfoFuncArea(expr, this
					.getCellModel());
			if (funcAreaList == null || funcAreaList.size() == 0)
				return false;

			for (IArea funcArea : funcAreaList)
			{
				if (dynAreaMocel.isInDynArea(funcArea.getStart()))// DynAreaUtil.getRealArea(
				// funcArea, this.getCellModel()).getStart()))
				{
					return true;
				}
			}

		}

		return false;
	}

	/**
	 * ���ط�ָ�꺯����Ufo�����������
	 *
	 * @param elem
	 * @return
	 */
	public ArrayList<IArea> getUfoFuncArea(UfoExpr expr, CellsModel cellsModel)
	{
		Object objFunc = expr.getElementObjByIndex(0);
		ArrayList<IArea> exprAreaList = new ArrayList<IArea>();
		if (objFunc != null && objFunc instanceof UfoFullArea)
		{
			UfoEElement[] elems = expr.getElements();
			for (UfoEElement elem : elems)
			{
				if (elem.getObj() != null
						&& elem.getObj() instanceof UfoFullArea)
				{
					UfoFullArea fullArea = (UfoFullArea) elem.getObj();
					exprAreaList.add(fullArea.getArea());
				}
			}
		} else if (objFunc != null && objFunc instanceof UfoFunc)
		{
			UfoFunc ufoFunc = (UfoFunc) objFunc;
			ArrayList funcParas = ufoFunc.getParams();
			if (funcParas == null)
			{
				return null;
			}
			for (int i = 0; i < funcParas.size(); i++)
			{
				Object param = funcParas.get(i);
				if (param instanceof UfoFullArea)
				{
					UfoFullArea fullArea = (UfoFullArea) param;
					exprAreaList.add(fullArea.getArea());
				}
				if (param instanceof UfoExpr)
				{
					UfoEElement[] elems = ((UfoExpr) param).getElements();
					for (UfoEElement elem : elems)
					{
						if (elem.getObj() != null
								&& elem.getObj() instanceof UfoFullArea)
						{
							UfoFullArea fullArea = (UfoFullArea) elem.getObj();
							exprAreaList.add(fullArea.getArea());
						}
					}
				}
			}
		}

		return exprAreaList;
	}

	/**
	 * ���ع�ʽ���ʽ����Ҫ��ʾ��ϸ�ڱ��ʽ
	 *
	 * @param listExpr
	 * @param env
	 * @return
	 */
	public static List getCalaElemFromExpr(List listExpr, ICalcEnv env)
	{
		if (listExpr == null || listExpr.size() == 0)
		{
			return null;
		}

		List listAllElemKey = new ArrayList();
		List listAllElem = new ArrayList();

		for (int i = 0, size = listExpr.size(); i < size; i++)
		{
			UfoExpr expr = (UfoExpr) listExpr.get(i);
			UfoEElement[] elements = expr.getElements();
			if (elements == null || elements.length == 0)
				continue;

			getCalaElemFromExpr(expr, env, listAllElemKey, listAllElem);
		}
		return listAllElem;
	}

	public static void getDetailCalaElemFromCombExpr(UfoCombExpr combExpr,
			ICalcEnv env)
	{
		if (combExpr == null)
			return;

		for (int i = 0, size = combExpr.getSubExpr().size(); i < size; i++)
		{
			UfoCombExpr subCombExpr = combExpr.getSubExpr().get(i);
			getDetailCalaElemFromCombExpr(subCombExpr, env);
		}

		getOneDetailCalaElemFromExpr(combExpr, env);
	}

	/**
	 * ���ع�ʽ���ʽ����Ҫ��ʾ��ϸ�ڱ��ʽ
	 *
	 * @param expr
	 * @param env
	 * @param listAllElemKey
	 * @param listAllElem
	 */
	public static void getOneDetailCalaElemFromExpr(UfoCombExpr combExpr,
			ICalcEnv env)
	{
		if (combExpr.getExpr() == null)
			return;

		Vector elements = new Vector();
		Stack stk = new Stack();
		// Vector abs = new Vector();
		Vector left;
		UfoEElement[] expElems = combExpr.getExpr().getElements();

		try
		{
			if (expElems != null && expElems.length > 1)
			{
				for (int i = 0; i < expElems.length; i++)
				{
					UfoEElement e = expElems[i];

					switch (e.getType())
					{
					case UfoEElement.OPR: // ������
						elements = new Vector();
						elements.add(expElems[i]);

						if (expElems[i].getObj() instanceof UfoFullArea
								|| expElems[i].getObj() instanceof UfoFunc)
						{
							// IOperand opr = (IOperand) expElems[i].getObj();
							// String str = opr.toString(env);

							UfoExpr subExpr = new UfoExpr(
									new UfoEElement[] { expElems[i] },
									IParsed.UNDEF_VAL);

							boolean bFind = false;
							for (int j = 0; j < combExpr.getSubExpr().size(); j++)
							{
								if (combExpr.getSubExpr().get(j) != null
										&& combExpr.getSubExpr().get(j)
												.toString().equals(
														subExpr.toString()))
									;
								{
									bFind = true;
									break;
								}
							}
							if (bFind == false)
								combExpr.getSubExpr().add(
										new UfoCombExpr(subExpr));
						}
						break;
					case UfoEElement.OP: // �����
						short op = ((Short) e.getObj()).shortValue();
						if ((op == IUfoTokenConsts.TKN_U_MINUS || op == IUfoTokenConsts.TKN_NOT)
								&& stk.size() >= 1)
						{ // һԪ�����
							elements = (Vector) stk.pop();
							elements.add(e);

						} else if (stk.size() >= 2)
						{ // ��Ԫ�����
							left = (Vector) stk.pop();
							elements = (Vector) stk.pop();

							if (op == IUfoTokenConsts.TKN_ASSIGN
									|| op == IUfoTokenConsts.TKN_LT
									|| op == IUfoTokenConsts.TKN_GE
									|| op == IUfoTokenConsts.TKN_GT
									|| op == IUfoTokenConsts.TKN_LE
									|| op == IUfoTokenConsts.TKN_NE
									|| op == IUfoTokenConsts.TKN_LIKE
									|| op == IUfoTokenConsts.TKN_NOTLIKE)
							{
								for (int m = 0; m < 2; m++)
								{
									Vector vecTemp = null;
									if (m == 0)
										vecTemp = elements;
									else
										vecTemp = left;
									if (vecTemp != null && vecTemp.size() > 1)
									{
										UfoExpr exprTemp = new UfoExpr(vecTemp,
												IParsed.UNDEF_VAL);
										combExpr.getSubExpr().add(
												new UfoCombExpr(exprTemp));
									}
								}
								continue;

							} else
							{
								elements.addAll(left);
								elements.add(e);
							}
						}
						break;

					}
					stk.push(elements);
				}
			}
		} catch (Exception e)
		{

		}
	}

	/**
	 * ���ع�ʽ���ʽ����Ҫ��ʾ��ϸ�ڱ��ʽ
	 *
	 * @param expr
	 * @param env
	 * @param listAllElemKey
	 * @param listAllElem
	 */
	public static void getCalaElemFromExpr(UfoExpr expr, ICalcEnv env,
			List listAllElemKey, List listAllElem)
	{
		Vector elements = new Vector();
		Stack stk = new Stack();
		// Vector abs = new Vector();
		Vector left;
		UfoEElement[] expElems = expr.getElements();

		try
		{
			if (expElems != null && expElems.length > 0)
			{
				for (int i = 0; i < expElems.length; i++)
				{
					UfoEElement e = expElems[i];

					switch (e.getType())
					{
					case UfoEElement.OPR: // ������
						elements = new Vector();
						elements.add(expElems[i]);

						if (expElems[i].getObj() instanceof UfoFullArea
								|| expElems[i].getObj() instanceof UfoFunc)
						{
							IOperand opr = (IOperand) expElems[i].getObj();
							String str = opr.toString(env);
							if (listAllElemKey.contains(str) == false)
							{
								listAllElemKey.add(str);
								listAllElem.add(new UfoExpr(
										new UfoEElement[] { expElems[i] },
										IParsed.UNDEF_VAL));
							}
						}
						break;
					case UfoEElement.OP: // �����
						short op = ((Short) e.getObj()).shortValue();
						if ((op == IUfoTokenConsts.TKN_U_MINUS || op == IUfoTokenConsts.TKN_NOT)
								&& stk.size() >= 1)
						{ // һԪ�����
							elements = (Vector) stk.pop();
							elements.add(e);

						} else if (stk.size() >= 2)
						{ // ��Ԫ�����
							left = (Vector) stk.pop();
							elements = (Vector) stk.pop();

							if (op == IUfoTokenConsts.TKN_ASSIGN
									|| op == IUfoTokenConsts.TKN_LT
									|| op == IUfoTokenConsts.TKN_GE
									|| op == IUfoTokenConsts.TKN_GT
									|| op == IUfoTokenConsts.TKN_LE
									|| op == IUfoTokenConsts.TKN_NE
									|| op == IUfoTokenConsts.TKN_LIKE
									|| op == IUfoTokenConsts.TKN_NOTLIKE)
							{
								for (int m = 0; m < 2; m++)
								{
									Vector vecTemp = null;
									if (m == 0)
										vecTemp = elements;
									else
										vecTemp = left;
									if (vecTemp != null && vecTemp.size() > 1)
									{
										UfoExpr exprTemp = new UfoExpr(vecTemp,
												IParsed.UNDEF_VAL);
										String str = exprTemp.toString(env);
										if (listAllElemKey.contains(str) == false)
										{
											listAllElemKey.add(str);
											listAllElem.add(exprTemp);
										}
									}
								}
								continue;

							} else
							{
								elements.addAll(left);
								elements.add(e);
							}
						}
						break;

					}
					stk.push(elements);
				}
			}
		} catch (Exception e)
		{

		}
	}

	/**
	 * ����ֱ�����ù�ʽ�Ƿ�������ֱ�����ù�ʽ
	 *
	 * <pre>
	 *   	 �����������ͺ���(��UfoFunc)��δֱ�����ö�̬��ָ�꣬���������������б������ʽ;
	 *     ����ຯ��Ҳ��Ҫ����������ʽʱ���㣬��Ҫ�ݹ�ʵ�֡�
	 * </pre>
	 *
	 * @param vecNonRefFormulaArea
	 *            ��ֱ�����ù�ʽ�б�
	 * @param vecRefFormulaArea
	 *            ֱ�����ù�ʽ�б�
	 * @param bCell
	 * @return
	 */
	private boolean toCheckFormulaAreaList(Vector<IArea> vecNonRefFormulaArea,
			Set<IArea> validSet, int type)
	{
		boolean isContinueChexk = false;
		Iterator itrElement = vecNonRefFormulaArea.iterator();
		while (itrElement.hasNext())
		{
			IArea nonRefFormulaArea = (IArea) itrElement.next();
			if (checkFormulaRefAreaList(nonRefFormulaArea, validSet,
					type))
			{
//				vecRefFormulaArea.addElement(nonRefFormulaArea);
				validSet.add(nonRefFormulaArea);
				itrElement.remove();
				isContinueChexk = true;

			}
		}
		return isContinueChexk;
	}

	/**
	 * ���ָ��λ�õĹ�ʽ���Ƿ�����ָ���б��еĵ�Ԫ����
	 *
	 * @param formulaArea
	 *            ����鹫ʽ�ĵ�Ԫ����
	 * @param vecRefFormulaArea
	 *            ��Ԫ�����б�
	 * @param bCell
	 *            ��Ԫ��ʽ/���ܹ�ʽ
	 * @return
	 */
	private boolean checkFormulaRefAreaList(IArea formulaArea,
			Set<IArea> validSet, int type)
	{
		IParsed formulaParsed = null;
		Map mapMainFormula = getFormulaModel().getMainFmls(type); // ��������ʽ
		FormulaVO oFormula = getMainFmlByArea(formulaArea, mapMainFormula);
		formulaParsed = oFormula.getLet();
		if (formulaParsed instanceof UfoCmdLet)
		{
			Vector vecUfoFullArea = AreaFormulaUtil.getAreaList(formulaParsed,
					AreaFormulaUtil.NOREP_AREA_TYPE, true);
			if (vecUfoFullArea == null || vecUfoFullArea.size() == 0)
			{
				return false;
			}
			for (int i = 0; i < vecUfoFullArea.size(); i++)
			{
				if (isExprAreaRefList((UfoFullArea) (vecUfoFullArea.get(i)),
						validSet))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * �����ʽ�������Ƿ�����ָ���б��е�����
	 *
	 * @param exprParamRefArea
	 *            ���ʽ�������õĵ�Ԫ����
	 * @param vecFormulaArea
	 *            �����б�
	 * @return
	 */
	private boolean isExprAreaRefList(UfoFullArea exprParamRefArea,
			Set<IArea> validSet)
	{
		for (IArea refArea : validSet)
		{
			if (refArea.intersection(exprParamRefArea.getArea()))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * ����һ�������ѯ���Ӳ�ѯ�������ý�����ݼ���ת��Ϊ�������ݡ�
	 *
	 * �������ڣ�(2003-9-10 19:36:28)
	 *
	 * @author������Ƽ
	 * @param repQueryVO
	 *            com.ufsoft.iuforeport.reporttool.query.ReportQueryVO
	 * @param bClear
	 *            boolean - �Ƿ������̬������ľ�����
	 */
	// private void calReportQuery(ReportQueryVO repQueryVO, boolean bClear) {
	// if (repQueryVO == null) {
	// return;
	// }
	//
	// ReportQueryUtil.convertDataSetToMeasureDatas(repQueryVO,
	// getContextVO(), getCellModel(), bClear);
	// }
	/**
	 * �������б����ѯ�ļ��㡣
	 *
	 * �������ڣ�(2003-9-10 19:28:26)
	 *
	 * @author������Ƽ
	 */
	private void calAllReportQuerys()
	{
		FormulaPrintUtil.printMsg("enter ufotable.calAllReportQuerys");

		// // ѭ���Ա�ҳ������б����ѯ���м���
		// ReportBusinessQuery reportBussQuery = ReportBusinessQuery
		// .getInstance(getCellModel());
		//
		// if (reportBussQuery != null) {
		// Vector vecRepQuerys = reportBussQuery.getAllReportQuery();
		// int iLen = vecRepQuerys != null ? vecRepQuerys.size() : 0;
		// // ��ѯ������ļ��㶯̬����PK��Hash
		// Hashtable hashDynAreaPks = new Hashtable();
		// for (int i = 0; i < iLen; i++) {
		// ReportQueryVO rqVO = (ReportQueryVO) vecRepQuerys.get(i);
		// String strDynAreaPK = rqVO.getDynAreaPK();
		// // ��һ�α����������У������Ǳ����ѯ��һ�μ��㵽�Ķ�̬����
		// // ֻ���кŹؼ��ֵĽ������̬����ľ�����
		// boolean bClear = false;
		// if (strDynAreaPK != null) {
		// if (!hashDynAreaPks.containsKey(strDynAreaPK)) {
		// hashDynAreaPks.put(strDynAreaPK, strDynAreaPK);
		// bClear = true;
		// }
		// }
		// String strQueryName = "";
		// if (rqVO != null && rqVO.getQuerydef() != null) {
		// strQueryName = rqVO.getQuerydef().getDisplayName();
		// }
		// FormulaPrintUtil.printMsg("��ʼ������ѯ����,��ѯ����= " + strQueryName);
		// calReportQuery(rqVO, bClear);
		// FormulaPrintUtil.printMsg("����������ѯ����,��ѯ����= " + strQueryName);
		// }
		// }
	}

	/**
	 * �������ж�̬����Ĺ�ʽ
	 *
	 * @create by liuchuna at 2010-12-1,����02:42:45
	 *
	 * @param bCalMeasure
	 * @param bCell
	 *            �Ƿ���㵥Ԫ��ʽ
	 */
	private void calAllDynAreas(boolean bCalMeasure, int type, boolean bOnlyAreaCalc){

		// ȡ�ñ��������еĶ�̬��
		ExtendAreaCell[] dynAreas = getDynAreaModel().getDynAreaCells();
		if(dynAreas != null && dynAreas.length > 0){
			for (int i = 0, len = dynAreas.length; i < len; i++) {
				if (dynAreas[i] != null && bCalMeasure){
					try {
						// ������Ԥ����(��̬���ؼ���)
						preCalDynAreaFormula(dynAreas[i], type, getContextVO());
						
						// ���㶯̬����HR���ݷ�������
						if(UfoeLicenseManager.isUFHRModuleValid() && !bOnlyAreaCalc && type == FormulaModel.TYPE_CELL_FML) {
							// �����������㣬��ִ�У����ұ����ǵ�Ԫ���㹫ʽ
							calcDynHRScheme(dynAreas[i]);
						}

						// ���㶯̬���ı�����Ŀ
						if(UfoeLicenseManager.isUFDSModuleValid() && !bOnlyAreaCalc && type == FormulaModel.TYPE_CELL_FML) {
							// �����������㣬��ִ�б�����Ŀ���㣬���ұ����ǵ�Ԫ���㹫ʽ
							calcDynRepItem(dynAreas[i]);
						}

						// ���㶯̬����ʽ
						calDynAreaFormula(dynAreas[i], type);
					} catch (Throwable e) {
						AppDebug.debug(e);
					} finally {
						getExecutorEnv().removeExEnv(UfoCalcEnv.KEY_DATA_SET_CACHE);
						getExecutorEnv().removeExEnv(UfoCalcEnv.KEY_DYN_KEYDATAGROUP_INDEX);
					}
				}
			}
		}
		// �����̬����Ϣ
		getExecutorEnv().setDynAreaInfo(null, null);
	}

	private void calcDynHRScheme(ExtendAreaCell dynArea) {
		try{
			// ��̬��pk
			getContextVO().setAttribute(IUfoContextKey.HR_CALC_AREA_PK, dynArea.getExAreaPK());
			
			// ��̬���ؼ���
			KeyDataGroup[] objKeydatas = getAllDynKeyDataGroups(dynArea.getExAreaPK());
			getContextVO().setAttribute(IUfoContextKey.HR_CALC_DYN_KEYS, objKeydatas);
			
			// ����̬������йؼ���ֵ�洢�ڼ��㻷����
			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			// �������ݼ�������̬������
			Hashtable<Integer, Hashtable<String, Object>> mapField2Values = getDynAreaField2KeyValues(dynArea, FormulaModel.TYPE_CELL_FML);
			this.getExecutorEnv().setDynField2Values(mapField2Values);
			
			// ���ü��㻷����Context
			setHRCalcExecutorFmlExt();
			setHRCalcExecutorCtx();

			// ִ�м���
			invokeHRCalc();
		} catch(Exception e) {
			// ֻ��ӡ�쳣��Ϣ��Ϊ�˲�Ӱ�������ʽ����
			AppDebug.debug(e);
		} finally {
			getContextVO().removeAttribute(IUfoContextKey.HR_CALC_AREA_PK);
			getContextVO().removeAttribute(IUfoContextKey.HR_CALC_DYN_KEYS);
		}

	}

	private void calcDynRepItem(ExtendAreaCell dynArea) {
		try {
			// ��̬��pk
			getContextVO().setAttribute(IUfoContextKey.DI_CALC_AREA_PK, dynArea.getExAreaPK());
			// ��̬���ؼ���
			KeyDataGroup[] objKeydatas = getAllDynKeyDataGroups(dynArea.getExAreaPK());

//			if(objKeydatas == null || objKeydatas.length == 0) {
//				// ��̬���ؼ���Ϊ��ʱ��������
//				return;
//			}
			getContextVO().setAttribute(IUfoContextKey.DI_CALC_DYN_KEYS, objKeydatas);

			// ����̬������йؼ���ֵ�洢�ڼ��㻷����
			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			// �������ݼ�������̬������
			Hashtable<Integer, Hashtable<String, Object>> mapField2Values = getDynAreaField2KeyValues(dynArea, FormulaModel.TYPE_CELL_FML);
			this.getExecutorEnv().setDynField2Values(mapField2Values);

			// ���ü��㻷����Context
			setDICalcExecutorFmlExt();
			setDICalcExecutorCtx();

			// ִ�м���
			invokeRepItemCalc();
		} catch(Exception e) {
			// ֻ��ӡ�쳣��Ϣ��Ϊ�˲�Ӱ�������ʽ����
			AppDebug.debug(e);
		} finally {
			getContextVO().removeAttribute(IUfoContextKey.DI_CALC_AREA_PK);
			getContextVO().removeAttribute(IUfoContextKey.DI_CALC_DYN_KEYS);
		}
	}

	/**
	 * ���ָ�������Ӧ��ָ��pk����
	 *
	 * @param vecDynFmls
	 * @return
	 */
	private String[] getDynFmlMeasurePK(Vector vecDynFmls)
	{
		String[] strMeasPKs = new String[vecDynFmls.size()];
		Hashtable hashTemp = null;
		MeasureVO mvo = null;
		for (int i = 0, iLen = strMeasPKs.length; i < iLen; i++)
		{
			if (vecDynFmls.get(i) == null)
				continue;
			hashTemp = getDynAreaModel().getMeasureModel().getMeasureVOByArea(
					(IArea) vecDynFmls.get(i));
			if (hashTemp != null && hashTemp.size() > 0)
			{
				// Set keys = hashTemp.keySet();
				Collection measures = hashTemp.values();
				mvo = (MeasureVO) measures.iterator().next();
				strMeasPKs[i] = mvo.getCode();
			}
		}
		return strMeasPKs;
	}

	public KeyDataGroup[] getDynKeyDataGroups(String strDynPK)
	{
		Object[] objValues = getExecutorEnv().getUfoDataChannel()
				.getMetaKeyValues(strDynPK);
		if (objValues == null || objValues.length == 0)
			return null;

		KeyDataGroup[] keyValues = new KeyDataGroup[objValues.length];
		for (int i = 0, iLen = keyValues.length; i < iLen; i++)
		{
			keyValues[i] = (KeyDataGroup) (objValues[i]);
		}
		return keyValues;
	}

	/**
	 * �Զ�̬����ʽִ��Ԥ���㡣 �µ�ҵ��Ҫ��̬���ؼ�����֧�ֶ������ݼ���ʽ�����������ݼ���չ��̬����
	 * �ǹؼ����϶���Ĺ�ʽ�������ݼ���ʽͬԭ��ҵ������һ�£�ȡ��ǰ��̬���ؼ��ֵ�ֵ���м���
	 *
	 * @create by liuchuna at 2010-12-1,����02:20:50
	 *
	 * @param dynAreaCell
	 *            ������Ķ�̬��
	 * @param bCell
	 *            �Ƿ���ܹ�ʽ
	 * @param context
	 */
	@SuppressWarnings("null")
	private void preCalDynAreaFormula(ExtendAreaCell dynAreaCell, int type,IContext context)
	{
		// �õ���̬���ؼ��ֶ��壺�ؼ���λ�ã��ؼ���
		Object[][] objKeyPos = getKeyPosVOsByDynArea(dynAreaCell);
		if (objKeyPos == null || objKeyPos.length == 0) {
			return;
		}

		// ��ȡ��̬��pkֵ
		String strDynPK = dynAreaCell.getBaseInfoSet().getExAreaPK();

		// ������̬����ʽ��
		Vector<Vector<IArea>> areaGradeVec;
		if (type == FormulaModel.TYPE_CELL_FML) {// ��Ԫ��ʽ
			if (getDynCellFormList(strDynPK) == null) {
				setupDynCellFmlList(strDynPK);
			}
			areaGradeVec = getDynCellFormList(strDynPK);
		} else {// ���ܹ�ʽ
			if (getDynTotleFmList(strDynPK) == null) {
				setupDynTotleFmlList(strDynPK);
			}
			areaGradeVec = getDynTotleFmList(strDynPK);
		}
		// �ӹ�ʽ����ȡ�ö�̬����ȫ����ʽ
		Vector<IArea> vecDynFmlAreas = new Vector<IArea>();
		for (Vector<IArea> vec : areaGradeVec) {
			if (vec != null) {
				vecDynFmlAreas.addAll(vec);
			}
		}

		// ��ʽ��ת��Ϊ����
		IArea[] areas = new IArea[vecDynFmlAreas.size()];
		vecDynFmlAreas.toArray(areas);

		if (areas == null && areas.length == 0) {
			return;
		}

		// ��ȡ�����ؼ�������Ĺ�ʽ���������б�
		Hashtable ht = getKeywordModel().getDynKeyVOPos(dynAreaCell.getBaseInfoSet().getExAreaPK());
		ArrayList<IArea> keyAreaList = new ArrayList<IArea>();
		for (IArea area : areas) {
			Iterator ite = ht.keySet().iterator();
			while (ite.hasNext()) {
				if (area.contain((CellPosition) ite.next()) && !keyAreaList.contains(area)) {
					keyAreaList.add(area);
				}
			}
		}
		// ����ؼ���������û�ж��幫ʽ���򷵻�
		if (keyAreaList.size() == 0)
			return;

		Hashtable<IArea, UfoVal[]> keyAreaValue = new Hashtable<IArea, UfoVal[]>();
		IArea[] keyAreas = keyAreaList.toArray(new IArea[0]);


		// ��ȡ�ö�̬�����й�ʽ
		Hashtable hashDynFmsl = getFormulaModel().getDynFmls(strDynPK, type);
//		Hashtable<IArea, UfoVal[]> keyAreaValue = new Hashtable<IArea, UfoVal[]>();
		boolean isCodeFill = true;
		//�Ƿ���Ҫ�������һ��
		boolean isShouldCalcTotally=true;
		for (IArea area : keyAreas) {
			try {
				boolean isKeyArea = isFmlDefInKeyArea(area, ht);
				getExecutorEnv().setExEnv(UfoCalcEnv.KEY_IS_KEYAREA_FML, isKeyArea);
				// ��ȡ��ʽ����
				FormulaVO formula = getDynFmlByArea(area, hashDynFmsl);
				if (formula != null) {
					try {
						IParsed formulaLet = formula.getLet();
						if (formulaLet == null) {
							formulaLet = parseExpr(formula.getFormulaContent());
							formula.setLet(formulaLet);
						}
						// �������ʱ���������ݼ�����
						if (!(isCalcDataSetFunc(formulaLet))) {
							continue;
						}

						isCodeFill = isCodeFill(formulaLet);
						
						// ȡ�ù�ʽ�����������ȼ�����������Ȼ����㱾��ʽ TODO
						Vector vecReferringDynArea = MeasFormulaUtil.getReferringMeasArea(formulaLet,
										MeasFormulaUtil.CHECK_EXP_MSELECT_COND, getExecutorEnv());
						if (vecReferringDynArea != null
								&& vecReferringDynArea.size() > 0) {
							IUFOTableData dataChannel = (IUFOTableData) getExecutorEnv().getDataChannel();
							// ���㶯̬�������������Ӳ���
							// int stepRow = dynAreaCell.isRowDirection() ? dynAreaCell
							// .getOriArea().getHeigth() : 0;
							// int stepCol = dynAreaCell.isRowDirection() ? 0
							// : dynAreaCell.getOriArea().getWidth();

							@SuppressWarnings("unused")
							IArea refArea = (IArea) vecReferringDynArea.get(0);
							//tianchuan 20141112 ��̬�������������������
							CellsModel formatModel=DynAreaUtil.getDataModelWithExModel(getDataModel());
							DynamicAreaModel dynModel=DynamicAreaModel.getInstance(formatModel);
							if(refArea!=null && dynModel.isInDynArea(refArea.getStart())){
								isShouldCalcTotally=false;
							}
							IArea realRefArea = null;
							for (IArea formulaArea : keyAreaValue.keySet()) {
								if (formulaArea.contain(refArea)) {
									realRefArea = formulaArea;
									break;
								}
							}

							if (realRefArea != null) {
								//ͨ����������õ�ֵ���Ͳ���Ҫ���������
								isShouldCalcTotally=false;
								UfoVal[] refAreaValue = keyAreaValue.get(realRefArea);
								if (refAreaValue != null && refAreaValue.length > 0) {
									ArrayList<UfoVal> keyValueList = new ArrayList<UfoVal>();
									for (int j = 0; j < refAreaValue.length; j++) {
										DataSetDynAreaDataParam param = new DataSetDynAreaDataParam(
												j, new int[0], strDynPK);
										param.setDynKeyValues(keyAreaValue);
										dataChannel.setDynAreaCalcParam(param);

										UfoVal[] dimValues = formulaLet
												.getValue(getExecutorEnv());
										if (dimValues == null || dimValues.length == 0) {
											continue;
										}
										keyValueList.add(dimValues[0]);

									}

									if (keyValueList.size() > 0) {
										keyAreaValue.put(area, keyValueList
												.toArray(new UfoVal[0]));
										continue;
									}
								}
							}
						}
						//tianchuan 2013.8.28 �޸� ������ֻ�������������֮ǰ�Ĵ���������
						//tianchuan 2013.10.11 ����isShouldCalcTotally�ж�
						if(isShouldCalcTotally){
							// ��ʽ����
							UfoVal[] dimValues = formulaLet.getValue(getExecutorEnv());

							// ���湫ʽ������
							if (dimValues != null) {
								keyAreaValue.put(area, dimValues);
							}
						}

						this.getExecutorEnv().setDynAllkeyaValue(keyAreaValue);

					} catch (ParseException e) {
						AppDebug.debug(e);
					} catch (CmdException e) {
						AppDebug.debug(e);
					}
				}
			} finally {
				getExecutorEnv().setExEnv(UfoCalcEnv.KEY_IS_KEYAREA_FML, null);
			}
		}

		// ����ת��
		ArrayList<UfoArray> allValueList = transferData(keyAreas, keyAreaValue);
		UfoVal[] allKeyValues = allValueList.toArray(new UfoArray[0]);

		// ������ݣ��������µ�����ģ��
		CellsModel newDataModel = DataSetFuncCalcUtil.convertDataSetToMeasureDatas(keyAreas, null,
				allKeyValues, getCellModel(), dynAreaCell,context, isCodeFill);
				
		//tianchuan 20140623  �����Ƿ��������ı�ʶ
		String strOldValue = (String) getExecutorEnv().getExEnv(CommonExprCalcEnv.EX_CALCEXFUNC);
		// ���������ɵ�����ģ��
		this.setDataModel(newDataModel);
				
		// ���³�ʼ��
		this.init();
		//�ָ�ԭ������������ʶ
		getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_CALCEXFUNC, strOldValue);

	}
	
	private boolean isCodeFill(IParsed parsedLet) {
		if (parsedLet == null) {
			return true;
		}
		UfoExpr ufoExpr = null;
		if (parsedLet instanceof UfoExpr) {
			ufoExpr = (UfoExpr) parsedLet;
		} else if (parsedLet instanceof UfoCmdLet) {
			ufoExpr = (UfoExpr) ((UfoCmdLet) parsedLet).getLetList().get(1);// ȡ�������ұ߱��ʽ
		}
		
		UfoFunc func = null;
		if(ufoExpr != null && ufoExpr.getElementObjByIndex(0) instanceof UfoFunc) {
			func = (UfoFunc) ufoExpr.getElementObjByIndex(0);
		}
		if(func instanceof DataSetFunc) {
			return ((DataSetFunc) func).getDataSetFillType() == 1 ? false : true;
		}
		return true;
	}
	
	private boolean isFmlDefInKeyArea(IArea area, Hashtable ht) {
		if(ht != null && !ht.isEmpty()) {
//			Iterator it = ht.keySet().iterator();
//			while(it.hasNext()) {
//				CellPosition cell = (CellPosition)it.next();
//				if(cell != null) {
//					area.
//				}
//			}
			CellPosition[] cells = area.split();
			for(CellPosition cell : cells) {
				if(!ht.containsKey(cell)) {
					return false;
				}
			}
		}
		return true;
	}

	public void reInit(CellsModel dataModel){

		// ���������ɵ�����ģ��
		this.setDataModel(dataModel);

		// ���³�ʼ��
		this.init();
	}

	/**
	 * @create by liuchuna at 2010-12-1,����02:41:27
	 *
	 * @param keyAreas
	 * @param keyAreaValue
	 * @return
	 */
	public ArrayList<UfoArray> transferData(IArea[] keyAreas,
			Hashtable<IArea, UfoVal[]> keyAreaValue) {
		ArrayList<UfoArray> allValueList = new ArrayList<UfoArray>();

		UfoVal[] areaValue = (UfoVal[]) keyAreaValue.get(keyAreas[0]);

		if(areaValue == null) {
			return allValueList;
		}
		for (int i = 0; i < areaValue.length; i++)
		{
			Vector<Object> rowValueVector = new Vector<Object>();
			UfoVal rowValue = areaValue[i];
			if (rowValue.getValue() instanceof Object[])
			{
				Object[] values = (Object[]) rowValue.getValue();
				for (int m = 0; m < values.length - 1; m++)
				{
					rowValueVector.add(values[m]);
				}
			}
			int iAreaSize = keyAreas.length;
			for (int j = 1; j < iAreaSize; j++)
			{
				if (!keyAreaValue.containsKey(keyAreas[j]))
				{
					continue;
				}
				UfoVal[] otherAreaValue = (UfoVal[]) keyAreaValue
						.get(keyAreas[j]);
				if (i < otherAreaValue.length)
				{
					UfoVal otherRowValue = otherAreaValue[i];
					if (otherRowValue.getValue() instanceof Object[])
					{
						Object[] values = (Object[]) otherRowValue.getValue();
						for (int m = 0; m < values.length - 1; m++)
						{
							rowValueVector.add(values[m]);
						}
					}else{
						rowValueVector.add(otherRowValue.getValue());
					}
				} else
				{
					// for (int n = 0; n < otherAreaValue[0].length - 1; n++) {
					rowValueVector.add(UfoVal.NULLSTR);
					// }
				}
			}
			try
			{
				/*
				 * edit by tanyj
				 * rowValueVector.toArray(new Object[0])�п��ܳ���Ϊ0��
				 * ����UfoArray.getInstance(array)��ִ��ʱ�׳������鳤�Ȳ���Ϊ0�ġ�UfoValueException
				 * �Ӷ����¼������̲�������ִ��
				 */
				Object[] arrays = rowValueVector.toArray(new Object[0]);
				if(arrays!=null && arrays.length>0){
					allValueList.add(UfoArray.getInstance(rowValueVector
							.toArray(new Object[0])));
				}
			} catch (UfoValueException e)
			{
				AppDebug.debug(e);
				throw new RuntimeException(e);
			}
		}
		return allValueList;
	}

	/**
	 * ���ض�̬���ֶ���ؼ���ӳ���ϵ
	 *
	 * @param dynAreaCell
	 * @param bCell
	 * @return
	 */
	private Hashtable<String, KeyVO> getDynAreaField2Keys(
			ExtendAreaCell dynAreaCell, int type)
	{
		Object[][] objKeyPos = getKeyPosVOsByDynArea(dynAreaCell);
		if (objKeyPos == null || objKeyPos.length == 0)
		{
			return null;
		}

		Hashtable<String, KeyVO> mapField2Keys = new Hashtable<String, KeyVO>();
		for (int i = 0; i < objKeyPos.length; i++)
		{
			CellPosition fmlPos = (CellPosition) objKeyPos[i][0];
			KeyVO keyVO =  (KeyVO)objKeyPos[i][1];
			FormulaVO formula = getFormulaByKeyArea(fmlPos);
			if (formula == null)
			{
				continue;
			}

			try
			{
				IParsed formulaLet = formula.getLet();
				//���������Ĵ�ʱ��ֱ��׷�٣��п���formulaLetʱUfoCmdLet���͵ģ������쳣���������UfoExpr���͵ģ�����������formulaLet
				if (formulaLet == null || !(formulaLet instanceof UfoExpr))
				{
					formulaLet = parseExpr(formula.getFormulaContent());
					formula.setLet(formulaLet);
				}

				Object objFunc = ((UfoExpr) formulaLet).getElementObjByIndex(0);
				if (objFunc == null || !(objFunc instanceof DataSetFunc))
				{
					continue;
				}
//				DynamicAreaModel dynAreaModel = DynamicAreaModel
//						.getInstance(this.getCellModel());
//				KeyVO keyVO = dynAreaModel.getKeywordModel().getKeyVOByPos(
//						fmlPos);
				String field = DataSetFuncCalcUtil.getFieldByKeyAreaK(
						getCellModel(), fmlPos, (DataSetFunc) objFunc);
				mapField2Keys.put(field, keyVO);
			} catch (ParseException e)
			{
				AppDebug.debug(e);
			}
		}
		return mapField2Keys;
	}

	/**
	 * ���ض�̬���ֶ���ؼ���ֵ��ӳ���ϵ
	 *
	 * @param dynAreaPK
	 * @param mapField2Keys
	 * @return
	 */
	private Hashtable<Integer, Hashtable<String, Object>> getDynAreaField2Values(
			String dynAreaPK, Hashtable<String, KeyVO> mapField2Keys, boolean isCodeFill)
	{
		if (mapField2Keys == null || mapField2Keys.size() == 0)
		{
			return null;
		}
		int i = 0;
		Hashtable<Integer, Hashtable<String, Object>> mapField2Values = new Hashtable<Integer, Hashtable<String, Object>>();
		ReportDataModel dataChannel = (ReportDataModel) this.getCalcEnv()
				.getDataChannel();
		KeyDataGroup[] keyDataGroups = (KeyDataGroup[]) dataChannel
				.getMetaKeyValues(dynAreaPK);
		for (KeyDataGroup keyDataGroup : keyDataGroups)
		{
			Enumeration<String> fields = mapField2Keys.keys();
			while (fields.hasMoreElements())
			{
				String f = fields.nextElement();
				KeyVO k = mapField2Keys.get(f);
				KeyDataVO keyDataVO = keyDataGroup
						.getByKeyPK(k.getPk_keyword());
				if(keyDataVO == null){
					throw new MessageException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0572")/*@res "��̬���ؼ������ݲ���Ϊ�գ�"*/);
				}
				Hashtable<String, Object> htField2Value = mapField2Values
						.get(Integer.valueOf(i));
				if (htField2Value == null)
				{
					htField2Value = new Hashtable<String, Object>();
					mapField2Values.put(Integer.valueOf(i), htField2Value);
				}

				if (k.getPk_keyword().equals(KeyVO.CORP_PK)
						|| k.getPk_keyword().equals(KeyVO.DIC_CORP_PK))
				{
					String strValue = null;
					if(keyDataVO.getKeyData() != null) {
						//tianchuan �޸� 63�Ժ� �洢PK��getDataһ����PKƥ��
						if(isCodeFill) {
							strValue = keyDataVO.getKeyData().getCode();
						} else {
							strValue = keyDataVO.getKeyData().getValue();
						}
					} else {
						ReportOrgVO repOrg = OrgUtil.getRepOrgVoByCode(keyDataVO.getValue());
						if(repOrg != null) {
							//tianchuan �޸� 63�Ժ� �洢PK��getDataһ����PKƥ��
							if(isCodeFill) {
								repOrg.getCode();
							} else {
								strValue = repOrg.getPk_org();
							}
						}
					}
					if (strValue != null)
					{
						htField2Value.put(f, strValue);
					}
				} else
				{
					//tianchuan ++ 63�Ժ� �洢PK��getDataһ����PKƥ��
					String strValue = keyDataVO.getValue();
					if(keyDataVO.getKeyData() instanceof ComKeyDetailData){
						if(keyDataVO.getKeyData().getCode()!=null){
							if(isCodeFill) {
								strValue=keyDataVO.getKeyData().getCode();
							} else {
								strValue=keyDataVO.getKeyData().getValue();
							}
						}
					}
					htField2Value.put(f, strValue);
				}

			}
			i++;
		}
		return mapField2Values;
	}

	/**
	 * ���ݹؼ��ֶ���λ�õõ���������Ĺ�ʽ
	 *
	 * @param keyPos
	 */
	private FormulaVO getFormulaByKeyArea(CellPosition keyPos)
	{
		Object[] keyFormulas = getFormulaModel().getRelatedFmlVO(keyPos, FormulaModel.TYPE_CELL_FML);
		if (keyFormulas != null && keyFormulas.length > 0
				&& keyFormulas[0] != null)
		{
			return (FormulaVO) keyFormulas[1];
		}
		return null;
	}

	/**
	 * ���㶯̬����ʽ��
	 * <p>
	 * �ɵĶ�̬����ʽ���㷽��(calDynAreaMeasureFm)��ֻ���㶯̬��ָ�깫ʽ�������µ�ҵ��
	 * ���󣬶�̬������֧�ֶ����ָ�깫ʽ֮������������Ĺ�ʽ���ͼ����㣬��˱����޸�Ϊ�Զ� ̬����ʽ���������㡣
	 *
	 * @param dynArea
	 *            ������Ķ�̬��
	 * @param bCell
	 *            �Ƿ���ܹ�ʽ
	 */
	private void calDynAreaFormula(ExtendAreaCell dynArea, int type)
	{
		long lstart = System.currentTimeMillis();

		// ������̬����ʽ��
		String strDynPK = dynArea.getBaseInfoSet().getExAreaPK();
		Vector<Vector<IArea>> areaGradeVec;
		if (type == FormulaModel.TYPE_CELL_FML)
		{// ��Ԫ��ʽ
			if (getDynCellFormList(strDynPK) == null)
			{
				setupDynCellFmlList(strDynPK);
			}
			areaGradeVec = getDynCellFormList(strDynPK);
		} else
		{// ���ܹ�ʽ
			if (getDynTotleFmList(strDynPK) == null)
			{
				setupDynTotleFmlList(strDynPK);
			}
			areaGradeVec = getDynTotleFmList(strDynPK);
		}
		// ȡ�ö�̬����ȫ����ʽ
		Vector<IArea> vecDynFmlAreas = new Vector<IArea>();
		for (Vector<IArea> vec : areaGradeVec)
		{
			if (vec != null)
			{
				vecDynFmlAreas.addAll(vec);
			}
		}

		IArea[] areas = new IArea[vecDynFmlAreas.size()];
		vecDynFmlAreas.toArray(areas);

		// ���㶯̬�������������Ӳ���
		// int stepRow = dynArea.isRowDirection() ? dynArea.getOriArea()
		// .getHeigth() : 0;
		// int stepCol = dynArea.isRowDirection() ? 0 : dynArea.getOriArea()
		// .getWidth();

		// ��ʼ������˳��������
		if (vecDynFmlAreas != null && vecDynFmlAreas.size() > 0)
		{
			IUFOTableData dataChannel = (IUFOTableData) getExecutorEnv()
					.getDataChannel();
			KeyDataGroup[] objKeydatas = getAllDynKeyDataGroups(strDynPK);
			if (objKeydatas == null || objKeydatas.length == 0)
			{
				return;
			}

			// ����̬������йؼ���ֵ�洢�ڼ��㻷����
			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			// �������в�����ָ�꺯���Ĳ�����������������
//			Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>> groupFuncValues = new Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>>();
//			Hashtable<IArea, UfoExpr[]> hashAreaFmExprs = batchCalcDynFinafunc(
//					dynArea, areas, objKeydatas, getExecutorEnv(), type, groupFuncValues);
//			if (hashAreaFmExprs == null || hashAreaFmExprs.size() == 0)
//			{
//				return;
//			}

			// ����̬������йؼ���ֵ�洢�ڼ��㻷����
//			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			// �������ݼ�������̬������
			Hashtable<Integer, Hashtable<String, Object>> mapField2Values = getDynAreaField2KeyValues(
					dynArea, type);
			this.getExecutorEnv().setDynField2Values(mapField2Values);

//			List<UfoExpr> vExpr = new ArrayList<UfoExpr>();
//			for (IArea area : hashAreaFmExprs.keySet())
//			{
//				UfoExpr[] exprs = (UfoExpr[]) hashAreaFmExprs.get(area);
//				if (exprs != null)
//					vExpr.addAll(Arrays.asList(exprs));
//			}
//			ReportCache cache = UFOCacheManager.getSingleton().getReportCache();
//			ReportVO report = cache.getByPK(getCalcEnv().getRepPK());
//			MeasFuncUtil.convertMeasFunc(vExpr.toArray(new UfoExpr[0]), report);

			if(dataChannel instanceof ReportDataModel) {
				((ReportDataModel) dataChannel).clearKeyDataMap();
			}

			// ��̬���ؼ��ֹ�ʽ����Ԥ�������Ѿ�����
//			List<IArea> dynKeyFmlAreas = new ArrayList<IArea>();
//			Object[][] objKeyPos = getKeyPosVOsByDynArea(dynArea);
//			if (objKeyPos != null && objKeyPos.length > 0) {
//				for (int i = 0; i < objKeyPos.length; i++) {
//					CellPosition fmlPos = (CellPosition) objKeyPos[i][0];
//					FormulaVO formula = getFormulaByKeyArea(fmlPos);
//					if (formula == null) {
//						continue;
//					}
//					IArea fmlArea = getFormulaModel().getAreaFmlQuicklyByCell(
//							fmlPos, FormulaModel.TYPE_CELL_FML);
//					dynKeyFmlAreas.add(fmlArea);
//				}
//			}

			for(int m = 0; m < areaGradeVec.size(); m++) {
				Vector<IArea> oneGradeAreas = areaGradeVec.get(m);
				if(oneGradeAreas==null){
					continue;
				}

				// �������в�����ָ�꺯���Ĳ�����������������
				Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>> groupFuncValues = new Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>>();
				Hashtable<IArea, UfoExpr[]> hashAreaFmExprs = batchCalcDynFinafunc(
						dynArea, oneGradeAreas.toArray(new IArea[0]), objKeydatas, getExecutorEnv(), type, groupFuncValues);
				if (hashAreaFmExprs == null || hashAreaFmExprs.size() == 0) {
					continue;
				}

				List<UfoExpr> vExpr = new ArrayList<UfoExpr>();
				for (IArea area : hashAreaFmExprs.keySet())
				{
					UfoExpr[] exprs = (UfoExpr[]) hashAreaFmExprs.get(area);
					if (exprs != null)
						vExpr.addAll(Arrays.asList(exprs));
				}
				ReportCache cache = UFOCacheManager.getSingleton().getReportCache();
				ReportVO report = cache.getByPK(getCalcEnv().getRepPK());
				MeasFuncUtil.convertMeasFunc(vExpr.toArray(new UfoExpr[0]), report);


				// ������㵥Ԫ��ʽ
				UfoExpr[] exprs = null;
				for (int i = 0; i < oneGradeAreas.size(); i++)
				{
					AreaPosition fmlAreaPos = (AreaPosition) oneGradeAreas.get(i);
					exprs = (UfoExpr[]) hashAreaFmExprs.get(fmlAreaPos);
					if (exprs == null || exprs.length == 0)
						continue;

//					boolean bHasDataSetFunc = this.checkFuncExpr(exprs[0]);
					boolean bHasDataSetFunc = false;
					try {
						bHasDataSetFunc = exprs[0].isDataSetFuncExpr(getCalcEnv());
					} catch(CmdException e) {
					}
					Map<Object, Map<AreaPosition, Object>> mapAreaValues = new Hashtable<Object, Map<AreaPosition, Object>>();
					for (int j = 0; objKeydatas != null && j < objKeydatas.length; j++)
					{
						// ����÷���¼��ؼ���Ϊ�գ�����������
						if (objKeydatas[j] == null)
						{
							continue;
						}

						UfoExpr exprTemp = j >= exprs.length ? exprs[0] : exprs[j];
						int[] unitDataColIndex = getUnitDataColIndex(exprTemp,
								fmlAreaPos);
						this.getExecutorEnv().setDynAreaInfo(strDynPK,
								objKeydatas[j]);
						dataChannel.setDynAreaCalcParam(new IUFODynAreaDataParam(j,
								unitDataColIndex, strDynPK));

						UfoVal[] ufoVals = null;
						try
						{
							ArrayList<ExtFunc> funcs = getValidFinaciaFunc(exprTemp);
							for(ExtFunc extFunc : funcs){
								if(groupFuncValues.get(extFunc) != null){
									extFunc.setValue(groupFuncValues.get(extFunc).get(objKeydatas[j]));
								}
							}

							ufoVals = exprTemp.calcExpr(getExecutorEnv(), 0, -1);

							for(ExtFunc extFunc : funcs){
	                        	extFunc.setValue(null);
							}
						} catch (CmdException ce)
						{
							AppDebug.debug(ce);
						}

						if (ufoVals != null && ufoVals.length > 0)
						{
							Map<AreaPosition, Object> mapTemp = mapAreaValues
									.get(objKeydatas[j]);
							if (mapTemp == null)
							{
								mapTemp = new Hashtable<AreaPosition, Object>();
								mapAreaValues.put(objKeydatas[j], mapTemp);
							}

							if (bHasDataSetFunc)
							{
								Object[] objVals = convertUfoVals2ObjValues(ufoVals);
								mapTemp.put(fmlAreaPos, objVals);
							}else if(fmlAreaPos.getWidth()>1 || fmlAreaPos.getHeigth()>1){
								Object[] objVals = convertUfoVals2ObjValues(ufoVals);
								mapTemp.put(fmlAreaPos, objVals);
							}else
							{
								if (ufoVals[0].getValue() != null)
									mapTemp.put(fmlAreaPos, ufoVals[0].getValue());
							}

						}

					}
					dataChannel.setMetaDatas(strDynPK,
							new AreaPosition[] { fmlAreaPos }, mapAreaValues);
					CmdProxy.clearPreCalcValues(exprs, getExecutorEnv());
				}
			}

//			// ������㵥Ԫ��ʽ
//			UfoExpr[] exprs = null;
//			for (int i = 0; i < vecDynFmlAreas.size(); i++)
//			{
//				AreaPosition fmlAreaPos = (AreaPosition) vecDynFmlAreas.get(i);
//				exprs = (UfoExpr[]) hashAreaFmExprs.get(fmlAreaPos);
//				if (exprs == null || exprs.length == 0)
//					continue;
//
////				boolean bHasDataSetFunc = this.checkFuncExpr(exprs[0]);
//				boolean bHasDataSetFunc = false;
//				try {
//					bHasDataSetFunc = exprs[0].isDataSetFuncExpr(getCalcEnv());
//				} catch(CmdException e) {
//				}
//				Map<Object, Map<AreaPosition, Object>> mapAreaValues = new Hashtable<Object, Map<AreaPosition, Object>>();
//				for (int j = 0; objKeydatas != null && j < objKeydatas.length; j++)
//				{
//					// ����÷���¼��ؼ���Ϊ�գ�����������
//					if (objKeydatas[j] == null)
//					{
//						continue;
//					}
//
//					UfoExpr exprTemp = j >= exprs.length ? exprs[0] : exprs[j];
//					int[] unitDataColIndex = getUnitDataColIndex(exprTemp,
//							fmlAreaPos);
//					this.getExecutorEnv().setDynAreaInfo(strDynPK,
//							objKeydatas[j]);
//					dataChannel.setDynAreaCalcParam(new IUFODynAreaDataParam(j,
//							unitDataColIndex, strDynPK));
//
//					UfoVal[] ufoVals = null;
//					try
//					{
//						ArrayList<ExtFunc> funcs = getValidFinaciaFunc(exprTemp);
//						for(ExtFunc extFunc : funcs){
//							if(groupFuncValues.get(extFunc) != null){
//								extFunc.setValue(groupFuncValues.get(extFunc).get(objKeydatas[j]));
//							}
//						}
//
//						ufoVals = exprTemp.calcExpr(getExecutorEnv(), 0, -1);
//
//						for(ExtFunc extFunc : funcs){
//                        	extFunc.setValue(null);
//						}
//					} catch (CmdException ce)
//					{
//						AppDebug.debug(ce);
//					}
//
//					if (ufoVals != null && ufoVals.length > 0)
//					{
//						Map<AreaPosition, Object> mapTemp = mapAreaValues
//								.get(objKeydatas[j]);
//						if (mapTemp == null)
//						{
//							mapTemp = new Hashtable<AreaPosition, Object>();
//							mapAreaValues.put(objKeydatas[j], mapTemp);
//						}
//
//						if (bHasDataSetFunc)
//						{
//							Object[] objVals = convertUfoVals2ObjValues(ufoVals);
//							mapTemp.put(fmlAreaPos, objVals);
//						}else if(fmlAreaPos.getWidth()>1 || fmlAreaPos.getHeigth()>1){
//							Object[] objVals = convertUfoVals2ObjValues(ufoVals);
//							mapTemp.put(fmlAreaPos, objVals);
//						}else
//						{
//							if (ufoVals[0].getValue() != null)
//								mapTemp.put(fmlAreaPos, ufoVals[0].getValue());
//						}
//
//					}
//
//				}
//				dataChannel.setMetaDatas(strDynPK,
//						new AreaPosition[] { fmlAreaPos }, mapAreaValues);
//				CmdProxy.clearPreCalcValues(exprs, getExecutorEnv());
//			}

			if(dataChannel instanceof ReportDataModel) {
				((ReportDataModel) dataChannel).clearKeyDataMap();
			}

			// ����̬�����йؼ���ֵ�ӻ��������������ͨ�������ݼ�ȡ������
			dataChannel.removeDynAreaCalcParam();
			this.getExecutorEnv().setDynAllKeyDatas(null);
			this.getExecutorEnv().setDynField2Values(null);
		}
		FormulaPrintUtil.printUsedTime(lstart,
				"calculating dynamic area cost time is:");
	}

	private ArrayList getValidFinaciaFunc(UfoExpr objExpr) {
		if (objExpr == null)
			return null;

		ArrayList listExtFunc=new ArrayList();
		int nSize = objExpr.getElementLength();
		for (int j = 0; j < nSize; j++) {
			Object objEle = objExpr.getElementObjByIndex(j);
			if (objEle != null && objEle instanceof ExtFunc) {
				ExtFunc objExtFunc = (ExtFunc) objEle;
				boolean bFinancialDriver =FuncListInst.isFinancialDriver(objExtFunc.getFuncDriverName());
				if(bFinancialDriver && isHaveMeasFunc(objExtFunc)==false){
					listExtFunc.add(objExtFunc);
				}
			}
			else if(objEle instanceof UfoFunc){
				//���������У��������Ϊ���ʽ���ͣ������Ƿ����������
				UfoFunc objFuncTemp=(UfoFunc)objEle;
				ArrayList listParams =objFuncTemp.getParams();
				if(listParams!=null && listParams.size()>0){
					int iParams=listParams.size();
					for(int k=0;k<iParams;k++){
						Object paramTemp=listParams.get(k);
						if(paramTemp!=null && paramTemp instanceof UfoExpr){
							listExtFunc.addAll(getValidFinaciaFunc((UfoExpr)paramTemp));
						}
					}
				}
			}
		}
		return listExtFunc;
	}

	private boolean isHaveMeasFunc(UfoFunc extFunc){
		ArrayList  alParams = extFunc.getParams();
        if( alParams != null ){
            for( int i=0; i<alParams.size(); i++ ){
                Object para = alParams.get(i);
                if (para != null) {
                    if (para instanceof MeasFunc) {
                            return true;
                    }else if( para instanceof UfoExpr){
                         if(isHaveMeasFuncInExpr((UfoExpr)para))
                         	return true;
                    }
                }
            }
        }
        return false;
	}
	private boolean isHaveMeasFuncInExpr(UfoExpr objExpr){
		if(objExpr==null)
			return false;

		int nSize = objExpr.getElementLength();
		for (int j = 0; j < nSize; j++) {
			Object objEle = objExpr.getElementObjByIndex(j);
			if (objEle != null && objEle instanceof UfoFunc) {
				UfoFunc objFunc = (UfoFunc) objEle;
				if(objFunc instanceof MeasFunc)
					return true;

				if(isHaveMeasFunc(objFunc)){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * ��ö�̬���ؼ���ֵ�����ݼ������ֶζ�ӦHashtable
	 *
	 * @param dynArea
	 * @param bCell
	 * @return
	 */
	public Hashtable<Integer, Hashtable<String, Object>> getDynAreaField2KeyValues(
			ExtendAreaCell dynArea, int type)
	{
//		Hashtable<String, KeyVO> mapField2Keys = getDynAreaField2Keys(dynArea,
//				type);
//		Hashtable<Integer, Hashtable<String, Object>> mapField2Values = getDynAreaField2Values(
//				dynArea.getBaseInfoSet().getExAreaPK(), mapField2Keys);
//		return mapField2Values;
		return getDynAreaField2KeyValues(dynArea, type, true);
	}
	
	public Hashtable<Integer, Hashtable<String, Object>> getDynAreaField2KeyValues(
			ExtendAreaCell dynArea, int type, boolean isCodeFill)
	{
		Hashtable<String, KeyVO> mapField2Keys = getDynAreaField2Keys(dynArea,
				type);
		Hashtable<Integer, Hashtable<String, Object>> mapField2Values = getDynAreaField2Values(
				dynArea.getBaseInfoSet().getExAreaPK(), mapField2Keys, isCodeFill);
		return mapField2Values;
	}

	/**
	 * add by wangyga
	 *
	 * @param fmlVo
	 * @return
	 */
//	private boolean checkFuncExpr(UfoExpr objExpr)
//	{
//		if (objExpr == null)
//			return false;
//		// ������ʽ��ÿ��ϸ�ڱ��ʽ
//		List allExpr = new ArrayList();
//		objExpr.getAllExprs(allExpr);
//
//		List<UfoExpr> listAllExpr = getCalaElemFromExpr(allExpr, getCalcEnv());
//		for (UfoExpr expr : listAllExpr)
//		{
//			try
//			{
//				if (expr.isDataSetFuncExpr(null)){
//					return true;
//				}
//				//tianchuan 2012.8.25 �޸ģ�����Ƿ��ض�ֵ��ָ�꺯����Ҳͬ������true
//				if (MeasFuncDriver.isMutiValsMeasFuncExpr(expr)){
//					return true;
//				}
//			} catch (CmdException e)
//			{
//				AppDebug.debug(e);
//				return false;
//			}
//		}
//		return false;
//	}

	/**
	 * ����ʽ����ֵ����ת��Ϊֵ����
	 *
	 * @param ufoVals
	 * @return
	 */
	private Object[] convertUfoVals2ObjValues(UfoVal[] ufoVals)
	{
		Object[] objVals = new Object[ufoVals.length];
		for (int i = 0; i < ufoVals.length; i++)
		{
			UfoVal objVal = ufoVals[i];
			objVals[i] = objVal.getValue();
		}
		return objVals;
	}

	/**
	 * ���غ����������������� ��������ֻ�����ݼ���������Ч
	 *
	 * @param expr
	 * @param formulaArea
	 * @return
	 */
	private int[] getUnitDataColIndex(UfoExpr expr, IArea area)
	{
		if (expr == null || area == null)
		{
			return null;
		}

		return new int[0];
	}

	/**
	 * �������㶯̬�����ڵĲ����в�����ָ�꺯���Ĳ�����
	 *
	 * @param dynArea
	 * @param areas
	 *            ָ���Ӧ�����򼯺�
	 * @return Hashtable key=ָ��pk�� value=ָ���Ӧ��ʽ�ڶ�̬������еĽ������UfoExpr[]
	 * @i18n uiiufofmt00035=��̬��������������preCalcDynExtFunc������Ϣ:
	 * @i18n uiiufofmt00036=��̬�����������������
	 */
	private Hashtable batchCalcDynFinafunc(ExtendAreaCell dynArea,
			IArea[] areas, KeyDataGroup[] objKeydatas, ICalcEnv env,
			int type, Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>> groupFuncValues)
	{

		Hashtable hashAreaFormula = null;
		Hashtable hashFinaFunc = null;

		// ��ö�̬����Ĺؼ��ּ���
		String strDynPK = dynArea.getBaseInfoSet().getExAreaPK();
		Hashtable hashDynFmsl = getFormulaModel().getDynFmls(strDynPK, type);

		ArrayList listDynKeyPKs = getDynKeyPKs(strDynPK);
		MeasureFormulaChecker checker = new MeasureFormulaChecker(env,
				objKeydatas, listDynKeyPKs);
		try
		{
			UfoExpr exprTemp = null;
			int iFmlAreaLen = areas.length;
			for (int i = 0; i < iFmlAreaLen; i++)
			{
				FormulaVO oFormula = getDynFmlByArea(areas[i], hashDynFmsl);
				if (oFormula == null || oFormula.isErrorFml() == true)
				{
					continue;
				}
				if (oFormula.getLet() == null)
				{
					oFormula.setLet(getFormulaProxy().parseExpr(
							oFormula.getFormulaContent()));
				}
				exprTemp = (UfoExpr) oFormula.getLet();
				checker.batchCalcDynFormula(areas[i], exprTemp, dynArea);
			}
			hashAreaFormula = checker.getAreaFormula();
			// ���кϸ�Ĳ�������key=KeyDataGroupVO value=ArrayList(����Ԫ������ΪExtFunc)��
			hashFinaFunc = checker.getBatchFinaFunc();
		} catch (CmdException e)
		{
			AppDebug.debug("batchCalcDynFinafunc", e);
		} catch (ParseException e)
		{
			AppDebug.debug("batchCalcDynFinafunc", e);
		}
		try
		{
			// ��������
			String strErrMsg = DynFormulaUtil.preCalcDynExtFunc(hashFinaFunc,
					getExecutorEnv(), strDynPK, groupFuncValues);
			if (strErrMsg != null && strErrMsg.length() > 0)
			{
				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0912")/*@res "��̬��������������preCalcDynExtFunc������Ϣ:"*/ + strErrMsg);
			}
		} catch (Exception e)
		{
			AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0913")/*@res "��̬�����������������"*/, e);
		}

		// ɾ����̬����Ԥ����Ĺ�ʽ
		Object[][] objKeyPos = getKeyPosVOsByDynArea(dynArea);
		if (objKeyPos != null && objKeyPos.length > 0)
		{
			for (int i = 0; i < objKeyPos.length; i++)
			{
				CellPosition fmlPos = (CellPosition) objKeyPos[i][0];
				FormulaVO formula = getFormulaByKeyArea(fmlPos);
				if (formula == null)
				{
					continue;
				}
				IArea fmlArea = getFormulaModel().getAreaFmlQuicklyByCell(
						fmlPos, FormulaModel.TYPE_CELL_FML);
				hashAreaFormula.remove(fmlArea);
			}
		}
		return hashAreaFormula;
	}

	/**
	 * �������㶯̬�����ڵĲ����в�����ָ�꺯���Ĳ�����
	 *
	 * @param dynArea
	 * @param exprTemp
	 *            ��ʽ
	 * @return Hashtable key=ָ��pk�� value=ָ���Ӧ��ʽ�ڶ�̬������еĽ������UfoExpr[]
	 * @i18n uiiufofmt00035=��̬��������������preCalcDynExtFunc������Ϣ:
	 * @i18n uiiufofmt00036=��̬�����������������
	 */
//	private Hashtable batchCalcDynFinafunc(ExtendAreaCell dynArea,
//			UfoExpr expr, KeyDataGroup[] objKeydatas, ICalcEnv env,
//			boolean bCell, Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>> groupFuncValues)
//	{
//
//		Hashtable hashFinaFunc = null;
//
//		// ��ö�̬����Ĺؼ��ּ���
//		String strDynPK = dynArea.getBaseInfoSet().getExAreaPK();
//
//		ArrayList listDynKeyPKs = getDynKeyPKs(strDynPK);
//		MeasureFormulaChecker checker = new MeasureFormulaChecker(env,
//				objKeydatas, listDynKeyPKs);
//		try
//		{
//			checker.batchCalcDynFormula(expr);
//			// ���кϸ�Ĳ�������key=KeyDataGroupVO value=ArrayList(����Ԫ������ΪExtFunc)��
//			hashFinaFunc = checker.getBatchFinaFunc();
//		} catch (CmdException e)
//		{
//			AppDebug.debug("batchCalcDynFinafunc", e);
//		}
//		try
//		{
//			// ��������
//			String strErrMsg = DynFormulaUtil.preCalcDynExtFunc(hashFinaFunc,
//					getExecutorEnv(), strDynPK, groupFuncValues);
//			if (strErrMsg != null && strErrMsg.length() > 0)
//			{
//				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0912")/*@res "��̬��������������preCalcDynExtFunc������Ϣ:"*/ + strErrMsg);
//			}
//		} catch (Exception e)
//		{
//			AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0913")/*@res "��̬�����������������"*/, e);
//		}
//
//		return hashFinaFunc;
//	}

	/**
	 * �������㶯̬�����ڵĲ����в�����ָ�꺯���Ĳ�����
	 *
	 * @param dynArea
	 * @param strMeasPKs
	 * @param areas
	 *            ָ���Ӧ�����򼯺�
	 * @return Hashtable key=ָ��pk�� value=ָ���Ӧ��ʽ�ڶ�̬������еĽ������UfoExpr[]
	 * @i18n uiiufofmt00035=��̬��������������preCalcDynExtFunc������Ϣ:
	 * @i18n uiiufofmt00036=��̬�����������������
	 */
//	@SuppressWarnings("unused")
//	private Hashtable batchCalcDynFinafunc(ExtendAreaCell dynArea,
//			String[] strMeasPKs, IArea[] areas, KeyDataGroup[] objKeydatas,
//			ICalcEnv env, int type, Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>> groupFuncValues)
//	{
//
//		Hashtable hashMeasFormula = null;
//		Hashtable hashFinaFunc = null;
//
//		// ��ö�̬����Ĺؼ��ּ���
//		String strDynPK = dynArea.getBaseInfoSet().getExAreaPK();
//		Hashtable hashDynFmsl = getFormulaModel().getDynFmls(strDynPK, type);
//
//		ArrayList listDynKeyPKs = getDynKeyPKs(strDynPK);
//		MeasureFormulaChecker checker = new MeasureFormulaChecker(env,
//				objKeydatas, listDynKeyPKs);
//		try
//		{
//			UfoExpr exprTemp = null;
//			int iMeasLen = strMeasPKs.length;
//			for (int i = 0; i < iMeasLen; i++)
//			{
//				FormulaVO oFormula = getDynFmlByArea(areas[i], hashDynFmsl);
//				if (oFormula == null || oFormula.isErrorFml() == true)
//				{
//					continue;
//				}
//				if (oFormula.getLet() == null)
//				{
//					oFormula.setLet(getFormulaProxy().parseExpr(
//							oFormula.getFormulaContent()));
//				}
//				exprTemp = (UfoExpr) oFormula.getLet();
//
//				checker.batchCalcDynFormula(strMeasPKs[i], exprTemp);
//			}
//			hashMeasFormula = checker.getMeasFormula();
//			// ���кϸ�Ĳ�������key=KeyDataGroupVO value=ArrayList(����Ԫ������ΪExtFunc)��
//			hashFinaFunc = checker.getBatchFinaFunc();
//		} catch (CmdException e)
//		{
//			AppDebug.debug("batchCalcDynFinafunc", e);
//			// UfoPublic.sendMessage(e.getMessage());
//		} catch (ParseException e)
//		{
//			AppDebug.debug("batchCalcDynFinafunc", e);
//			// UfoPublic.sendMessage(e.getMessage());
//		}
//		try
//		{
//			// ��������
//			String strErrMsg = DynFormulaUtil.preCalcDynExtFunc(hashFinaFunc,
//					getExecutorEnv(), strDynPK, groupFuncValues);
//			if (strErrMsg != null && strErrMsg.length() > 0)
//			{
//				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0912")/*@res "��̬��������������preCalcDynExtFunc������Ϣ:"*/ + strErrMsg);
//			}
//		} catch (Exception e)
//		{
//			AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0913")/*@res "��̬�����������������"*/, e);
//		}
//
//		return hashMeasFormula;
//	}

	/**
	 * ��ö�̬���ؼ���pk����
	 *
	 * @param dynArea
	 * @return
	 */
	private ArrayList getDynKeyPKs(String strDynPK)
	{
		ArrayList listKeyPKs = null;
		KeyVO[] keys = getDynKeyVOs(strDynPK);
		if (keys != null && keys.length > 0)
		{
			listKeyPKs = new ArrayList();
			for (int i = 0, iLen = keys.length; i < iLen; i++)
			{
				listKeyPKs.add(keys[i].getPk_keyword());
			}
		}
		return listKeyPKs;
	}

	/**
	 * �����ҳ�ĵ�Ԫ���ܹ�ʽ ע����ܹ�ʽ���㲻ִ�в�ѯ�����ݴ���
	 *
	 * @i18n uiiufofmt00037=���ܹ�ʽ����
	 */
	public void calcAllTotalFormulas()
	{
		long time = System.currentTimeMillis();

		// �õ���Ԫ��ʽ�б�
		if (getMainTotleFormList() == null)
		{
			setupMainTotleFmlList();
		}

		Vector<Vector<IArea>> areaGradeVec = getMainTotleFormList();
		Vector<IArea> flist = new Vector<IArea>();
		for (Vector<IArea> vec : areaGradeVec)
		{
			if (vec != null)
			{
				flist.addAll(vec);
			}
		}

		calcMainFormulas(flist, FormulaModel.TYPE_TOTAL_FML);

		calAllDynAreas(true, FormulaModel.TYPE_TOTAL_FML, false);

		FormulaPrintUtil.printUsedTime(time, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0914")/*@res "���ܹ�ʽ����"*/);
	}

	/**
	 * ����U8��ɢ����Դ�ⲿ������ʽ
	 *
	 * @return Hashtable
	 */
	public ArrayList<String> loadExtU8Funcs()
	{
		ArrayList<String> aryFuncFormulas = new ArrayList<String>();

		// 1.�Թ̶�����ʽ���д���
		if (getMainCellFormList() == null)
		{
			setupMainCellFmlList();
		}
		Vector<Vector<IArea>> areaGradeVec = getMainCellFormList();

		Vector<IArea> flist = new Vector<IArea>();
		;
		for (Vector<IArea> vec : areaGradeVec)
		{
			if (vec != null)
			{
				flist.addAll(vec);
			}
		}

		if (flist != null && flist.size() > 0)
		{
			int nFormulas = flist.size();

			getExecutorEnv().setDynAreaInfo(null, null);
			ArrayList<UfoExpr> aryExpr = new ArrayList<UfoExpr>(nFormulas);

			Map mapMainFormula = getFormulaModel().getMainFmls(FormulaModel.TYPE_CELL_FML);
			for (int i = 0; i < nFormulas; i++)
			{
				IArea a = flist.elementAt(i);
				FormulaVO oFormula = getMainFmlByArea(a, mapMainFormula);
				;
				if (oFormula == null || oFormula.getFormulaContent() == null
						|| oFormula.getFormulaContent().length() == 0)
				{
					continue;
				}
				try
				{
					// �õ���ʽ�Ҳ���ʽ
					UfoExpr expr = null;
					if (oFormula.getLet() != null)
					{
						if (oFormula.getLet() instanceof UfoExpr)
							expr = (UfoExpr) oFormula.getLet();
						else if (oFormula.getLet() instanceof UfoCmdLet)
						{
							UfoCmdLet cmdLet = (UfoCmdLet) oFormula.getLet();
							if (cmdLet.getLetList() != null
									&& cmdLet.getLetList().size() > 1)
							{
								expr = (UfoExpr) cmdLet.getLetList().get(1);
							}
						}
					} else
						expr = getFormulaProxy().parseExpr(
								oFormula.getFormulaContent());
					if (expr != null)
						aryExpr.add(expr);
				} catch (ParseException pe)
				{
					AppDebug.debug("loadExtU8Funcs", pe);
				}
			}

			// �õ��̶����գ�����ʽ
			Hashtable hashExtFunc = new Hashtable();
			CmdProxy.registerExtFuncs((UfoExpr[]) aryExpr
					.toArray(new UfoExpr[0]), hashExtFunc, 0, getExecutorEnv());
			loadExtFuncStringArray(hashExtFunc, aryFuncFormulas);
		}

		// 2.���ض�̬���ģգ�����ʽ
		ExtendAreaCell[] dynAreas = getDynAreaModel().getDynAreaCells();
		int iLen = dynAreas != null ? dynAreas.length : 0;
		for (int i = 0; i < iLen; i++)
		{
			loadDynAreaExtFunc(dynAreas[i], aryFuncFormulas);

		}
		getExecutorEnv().setDynAreaInfo(null, null);

		return aryFuncFormulas;
	}

	/**
	 * �ҳ��գ�����ʽ�����������滻��ת��Ϊ��ʽ����
	 *
	 * @param hashExtFunc
	 *            Hashtable
	 * @param aryFuncFormulas
	 *            ArrayList
	 */
	private void loadExtFuncStringArray(Hashtable hashExtFunc,
			ArrayList aryFuncFormulas)
	{
		if (hashExtFunc == null)
			return;

		ArrayList aryExtFunc = (ArrayList) hashExtFunc.get(SrvCallU8.class
				.getName());

		if (aryExtFunc == null || aryExtFunc.size() <= 0)
			return;

		ExtFunc[] funcs = (ExtFunc[]) aryExtFunc.toArray(new ExtFunc[0]);

		for (int i = 0; i < funcs.length; i++)
		{
			try
			{
				// ��Ԫ��ʽ�������滻�������ɹ�ʽ����
				aryFuncFormulas.add(funcs[i].getFuncName()
						+ "("
						+ StringTools.stringTrimRight(funcs[i]
								.getParamString(getExecutorEnv())) + ")");
			} catch (ParseException e)
			{
				AppDebug.debug(e);// @devTools AppDebug.debug(e);
			}
		}
	}

	/**
	 * ����ĳһ��̬���еģգ�����ʽ
	 *
	 * @param dynArea
	 *            DynamicAreaVO
	 * @param aryFuncFormulas
	 *            ArrayList
	 */
	private void loadDynAreaExtFunc(ExtendAreaCell dynArea,
			ArrayList aryFuncFormulas)
	{

		String strDynPK = dynArea.getBaseInfoSet().getExAreaPK();

		if (getDynCellFormList(strDynPK) == null)
		{
			// ������ʽ��
			setupDynCellFmlList(strDynPK);
		}
		Vector<Vector<IArea>> areaGradeVec = getDynCellFormList(strDynPK);

		Vector<IArea> vecDynFmls = new Vector<IArea>();

		for (Vector<IArea> vec : areaGradeVec)
		{
			if (vec != null)
			{
				vecDynFmls.addAll(vec);
			}
		}

		IArea[] areas = new IArea[vecDynFmls.size()];
		vecDynFmls.toArray(areas);
		String[] strMeasPKs = getDynFmlMeasurePK(vecDynFmls);

		if (strMeasPKs == null || strMeasPKs.length <= 0)
			return;

		Hashtable hashDynFmsl = getFormulaModel().getDynFmls(strDynPK, FormulaModel.TYPE_CELL_FML);

		for (int i = 0; i < strMeasPKs.length; i++)
		{
			try
			{
				FormulaVO oFormula = getDynFmlByArea(areas[i], hashDynFmsl);
				if (oFormula == null || oFormula.getFormulaContent() == null
						|| oFormula.getFormulaContent().length() == 0)
				{
					continue;
				}
				if (oFormula.getLet() == null)
				{
					oFormula.setLet(getFormulaProxy().parseExpr(
							oFormula.getFormulaContent()));
				}
				KeyDataGroup[] objKeydatas = getDynKeyDataGroups(strDynPK);
				if (objKeydatas == null)
					continue;

				// ͬһ��ʽ��Ӧ��������
				for (int j = 0; j < objKeydatas.length; j++)
				{
					Hashtable hashExtFunc = new Hashtable();
					getExecutorEnv().setDynAreaInfo(strDynPK, objKeydatas[j]);
					CmdProxy.registerExtFuncs(
							new UfoExpr[] { (UfoExpr) oFormula.getLet() },
							hashExtFunc, 3, getExecutorEnv());
					loadExtFuncStringArray(hashExtFunc, aryFuncFormulas);
				}
			} catch (ParseException e)
			{
				AppDebug.debug("loadDynAreaExtFunc", e);
			}
		}
	}

	/**
	 *
	 * @param measures
	 * @i18n miufo1000701=��ʽ����
	 */
	public void calcAllHBFormulas(Map<IArea, FormulaVO> mapHBFormula, MeasureVO[] measures,int formulaType)
	{
		long time = System.currentTimeMillis();

		// ��ʼ����ʽ������ʽ����Ϊ�ռ�
//		if (allHBFmlList == null) {
//			allHBFmlList = new Vector<Vector<IArea>>();
//		} else {
//			allHBFmlList.clear();
//		}

		// construct level formula list
		//2013.1.5 tianchuan �ϲ�������㹫ʽ�����⣬inner������Ϊtrue
//		constructLevelFmlList(allHBFmlList, mapHBFormula, true,formulaType);

		Vector<IArea> vecArea = new Vector<IArea>();
		vecArea.addAll(mapHBFormula.keySet());
		// ���ϲ�ָ��λ���ų�
		if (measures != null && measures.length > 0) {
			Vector<IArea> vAreaTemp = new Vector<IArea>();
			if (measures != null) {

				IArea area = null;
				for (int i = 0, size = measures.length; i < size; i++) {
					if (measures[i] == null)
						continue;
					if (measures[i].getExttype() != MeasureVO.TYPE_EXT_HEBING)
						continue;
					area = getDynAreaModel().getMeasureModel()
							.getMeasurePosByPK(measures[i].getCode());
					if (area != null && vAreaTemp.contains(area) == false)
						vAreaTemp.add(area);

				}
			}

			for (int i = 0; i < vAreaTemp.size(); i++) {
				vecArea.remove(vAreaTemp.elementAt(i));
			}
		}
		
		//tianchuan �����ｫ���еĺϲ���ʽ���Ϊ����ʽ�Ͷ�̬����ʽ��
		CellsModel formatModel=DynAreaUtil.getDataModelWithExModel(getDataModel());
		DynamicAreaModel dynModel=DynamicAreaModel.getInstance(formatModel);
		
		
		Map<IArea, FormulaVO> mapHBFormulaMain=new HashMap<IArea, FormulaVO>();
		Map<String, Hashtable<IArea, FormulaVO>> dynHBAreaFmlMap=new HashMap<String, Hashtable<IArea, FormulaVO>>();	//��̬���ϲ���ʽ��λ��ӳ��
		if(vecArea!=null){
			Iterator<IArea> it=vecArea.iterator();
			IArea tempArea=null;
			ExtendAreaCell tempDynCell=null;
			while(it.hasNext()){
				tempArea=it.next();
				tempDynCell=dynModel.getDynAreaCellByFmtPos(tempArea.getStart());
				if(tempDynCell!=null){
					if(!dynHBAreaFmlMap.containsKey(tempDynCell.getBaseInfoSet().getExAreaPK())){
						dynHBAreaFmlMap.put(tempDynCell.getBaseInfoSet().getExAreaPK(), new Hashtable<IArea, FormulaVO>());
					}
					dynHBAreaFmlMap.get(tempDynCell.getBaseInfoSet().getExAreaPK()).put(tempArea, mapHBFormula.get(tempArea));
				}else{
					mapHBFormulaMain.put(tempArea, mapHBFormula.get(tempArea));
				}
			}
		}
		//��������ϲ���ʽ
		Vector<Vector<IArea>> mainHBFmlList=new Vector<Vector<IArea>>(); 
		//��������ϲ���ʽ��
		constructLevelFmlList(mainHBFmlList, mapHBFormulaMain, true,formulaType);
		Vector<IArea> mainHBAreaVec=new Vector<IArea>();	//����ϲ���ʽ��λ��
		for (Vector<IArea> vec : mainHBFmlList) {
			if (vec != null) {
				mainHBAreaVec.addAll(vec);
			}
		}
		calcFormulas(mapHBFormula, mainHBAreaVec, formulaType);	//?
		
		//���㶯̬���ϲ���ʽ
		Iterator<Entry<String, Hashtable<IArea, FormulaVO>>> mapIt=dynHBAreaFmlMap.entrySet().iterator();
		Entry<String, Hashtable<IArea, FormulaVO>> tempEntry=null;
		Hashtable<IArea, FormulaVO> mapHBFormulaDyn=null;
		Vector<IArea> areaVec=null;
		Vector<Vector<IArea>> dynHBFmlList=null;
		while(mapIt.hasNext()){
			try{
				tempEntry=mapIt.next();
				getExecutorEnv().setDynAreaInfo(tempEntry.getKey(), null);
				mapHBFormulaDyn=tempEntry.getValue();
				dynHBFmlList=new Vector<Vector<IArea>>();
				setupDynFormulaList(tempEntry.getKey(), dynHBFmlList, mapHBFormulaDyn, formulaType);	//?
				// �����в�νṹ�Ĺ�ʽ��
				setupGradeFmlList(mapHBFormulaDyn, dynHBFmlList);
				areaVec=new Vector<IArea>();
				for (Vector<IArea> vec : dynHBFmlList) {
					if (vec != null) {
						areaVec.addAll(vec);
					}
				}
				//���㶯̬���ϲ���ʽ
				calDynAreaHBFormula(areaVec,tempEntry.getKey(), formulaType);	//?
			}finally{
				getExecutorEnv().setDynAreaInfo(null, null);
			}
		}
		FormulaPrintUtil.printUsedTime(time, "hb "
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0915")/*@res "��ʽ����"*/); // "��ʽ����"
	}
	
	//���㶯̬���ϲ���ʽ
	private void calDynAreaHBFormula(Vector<IArea> vecDynFmlAreas,String strDynPK, int type)
	{
		long lstart = System.currentTimeMillis();

//		String strDynPK = dynArea.getBaseInfoSet().getExAreaPK();
		ExtendAreaCell dynArea=getDynAreaModel().getExtendAreaCellByPK(strDynPK);

		// ��ʼ������˳��������
		if (vecDynFmlAreas != null && vecDynFmlAreas.size() > 0)
		{
			IUFOTableData dataChannel = (IUFOTableData) getExecutorEnv()
					.getDataChannel();
			KeyDataGroup[] objKeydatas = getAllDynKeyDataGroups(strDynPK);
			if (objKeydatas == null || objKeydatas.length == 0)
			{
				return;
			}

			// ����̬������йؼ���ֵ�洢�ڼ��㻷����
			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			// �������ݼ�������̬������
			Hashtable<Integer, Hashtable<String, Object>> mapField2Values = getDynAreaField2KeyValues(
					dynArea, type);
			this.getExecutorEnv().setDynField2Values(mapField2Values);

			if(dataChannel instanceof ReportDataModel) {
				((ReportDataModel) dataChannel).clearKeyDataMap();
			}

			Vector<IArea> oneGradeAreas = vecDynFmlAreas;
			if(oneGradeAreas==null){
				return;
			}

			// �������в�����ָ�꺯���Ĳ�����������������
			Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>> groupFuncValues = new Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>>();
			Hashtable<IArea, UfoExpr[]> hashAreaFmExprs = batchCalcDynFinafunc(
					dynArea, oneGradeAreas.toArray(new IArea[0]), objKeydatas, getExecutorEnv(), type, groupFuncValues);
			if (hashAreaFmExprs == null || hashAreaFmExprs.size() == 0) {
				return;
			}

			List<UfoExpr> vExpr = new ArrayList<UfoExpr>();
			for (IArea area : hashAreaFmExprs.keySet())
			{
				UfoExpr[] exprs = (UfoExpr[]) hashAreaFmExprs.get(area);
				if (exprs != null)
					vExpr.addAll(Arrays.asList(exprs));
			}
			ReportCache cache = UFOCacheManager.getSingleton().getReportCache();
			ReportVO report = cache.getByPK(getCalcEnv().getRepPK());
			MeasFuncUtil.convertMeasFunc(vExpr.toArray(new UfoExpr[0]), report);


			// ������㵥Ԫ��ʽ
			UfoExpr[] exprs = null;
			for (int i = 0; i < oneGradeAreas.size(); i++)
			{
				AreaPosition fmlAreaPos = (AreaPosition) oneGradeAreas.get(i);
				exprs = (UfoExpr[]) hashAreaFmExprs.get(fmlAreaPos);
				if (exprs == null || exprs.length == 0)
					continue;

//					boolean bHasDataSetFunc = this.checkFuncExpr(exprs[0]);
				boolean bHasDataSetFunc = false;
				try {
					bHasDataSetFunc = exprs[0].isDataSetFuncExpr(getCalcEnv());
				} catch(CmdException e) {
				}
				Map<Object, Map<AreaPosition, Object>> mapAreaValues = new Hashtable<Object, Map<AreaPosition, Object>>();
				for (int j = 0; objKeydatas != null && j < objKeydatas.length; j++)
				{
					// ����÷���¼��ؼ���Ϊ�գ�����������
					if (objKeydatas[j] == null)
					{
						continue;
					}

					UfoExpr exprTemp = j >= exprs.length ? exprs[0] : exprs[j];
					int[] unitDataColIndex = getUnitDataColIndex(exprTemp,
							fmlAreaPos);
					this.getExecutorEnv().setDynAreaInfo(strDynPK,
							objKeydatas[j]);
					dataChannel.setDynAreaCalcParam(new IUFODynAreaDataParam(j,
							unitDataColIndex, strDynPK));

					UfoVal[] ufoVals = null;
					try
					{
						ArrayList<ExtFunc> funcs = getValidFinaciaFunc(exprTemp);
						for(ExtFunc extFunc : funcs){
							if(groupFuncValues.get(extFunc) != null){
								extFunc.setValue(groupFuncValues.get(extFunc).get(objKeydatas[j]));
							}
						}

						ufoVals = exprTemp.calcExpr(getExecutorEnv(), 0, -1);

						for(ExtFunc extFunc : funcs){
                        	extFunc.setValue(null);
						}
					} catch (CmdException ce)
					{
						AppDebug.debug(ce);
					}

					if (ufoVals != null && ufoVals.length > 0)
					{
						Map<AreaPosition, Object> mapTemp = mapAreaValues
								.get(objKeydatas[j]);
						if (mapTemp == null)
						{
							mapTemp = new Hashtable<AreaPosition, Object>();
							mapAreaValues.put(objKeydatas[j], mapTemp);
						}

						if (bHasDataSetFunc)
						{
							Object[] objVals = convertUfoVals2ObjValues(ufoVals);
							mapTemp.put(fmlAreaPos, objVals);
						}else if(fmlAreaPos.getWidth()>1 || fmlAreaPos.getHeigth()>1){
							Object[] objVals = convertUfoVals2ObjValues(ufoVals);
							mapTemp.put(fmlAreaPos, objVals);
						}else
						{
							if (ufoVals[0].getValue() != null)
								mapTemp.put(fmlAreaPos, ufoVals[0].getValue());
						}

					}

				}
				dataChannel.setMetaDatas(strDynPK,
						new AreaPosition[] { fmlAreaPos }, mapAreaValues);
				CmdProxy.clearPreCalcValues(exprs, getExecutorEnv());
			}

			if(dataChannel instanceof ReportDataModel) {
				((ReportDataModel) dataChannel).clearKeyDataMap();
			}

			// ����̬�����йؼ���ֵ�ӻ��������������ͨ�������ݼ�ȡ������
			dataChannel.removeDynAreaCalcParam();
			this.getExecutorEnv().setDynAllKeyDatas(null);
			this.getExecutorEnv().setDynField2Values(null);
		}
		FormulaPrintUtil.printUsedTime(lstart, "calculating dynamic area cost time is:");
	}
	
	
	
	
	/**
	 * ����û�������ʽ�Ĺ�ʽ������ɹ����ع�ʽ������������򷵻�null�����׳��쳣
	 *
	 * @param area
	 *            ��ʽ��������λ��
	 * @param strUserDefFormula
	 *            ��ʽ����
	 * @return com.ufsoft.script.base.IParsed ��ʽ�������
	 * @throws ParseException
	 */
	public IParsed parseUserDefFormula(IArea area, String strUserDefFormula)
			throws ParseException
	{

		if (strUserDefFormula == null || strUserDefFormula.trim().length() == 0)
			return null;

		String strEnvDynPK = null;
		IParsed objUserLet = null;
		try
		{
			strEnvDynPK = getExecutorEnv().getDynArea();
			turnOffExtFuncCheck();
			ExtendAreaCell dynVO = null;
			dynVO = getValidDynamicArea(area);
			if (dynVO != null)
			{
				if (getExecutorEnv().getDynArea() == null
						|| !(getExecutorEnv().getDynArea().equals(dynVO
								.getBaseInfoSet().getExAreaPK())))
					;
				{
					getExecutorEnv().setDynAreaInfo(
							dynVO.getBaseInfoSet().getExAreaPK(), null);
				}
			} else
			{
				getExecutorEnv().setDynAreaInfo(null, null);
			}
			objUserLet = getFormulaProxy().parseUserDefExpr(strUserDefFormula);
			return objUserLet;
		} catch (DynAreaException e)
		{
			AppDebug.debug("DynAreaException", e);
		} finally
		{
			getExecutorEnv().setDynAreaInfo(strEnvDynPK, null);
			turnOnExtFuncCheck();
		}
		return null;
	}

	/**
	 * ����û�������ʽ�Ĺ�ʽ������ɹ��������ݿ���ʽ��ʽ���ݡ����򷵻�null�����׳��쳣
	 *
	 * @param area
	 *            ��ʽ��������λ��
	 * @param strUserDefFormula
	 *            ��ʽ����
	 * @return
	 * @throws ParseException
	 */
	@Override
	public String checkUserDefFormula(IArea area, String strUserDefFormula)
			throws ParseException
	{

		if (strUserDefFormula == null || strUserDefFormula.trim().length() == 0)
			return strUserDefFormula;

		String strReturn = null;
		String strEnvDynPK = null;
		try
		{
			strEnvDynPK = getExecutorEnv().getDynArea();
			turnOffExtFuncCheck();
			ExtendAreaCell dynVO = null;
			dynVO = getValidDynamicArea(area);
			if (dynVO != null)
			{
				if (getExecutorEnv().getDynArea() == null
						|| !(getExecutorEnv().getDynArea().equals(dynVO
								.getBaseInfoSet().getExAreaPK())))
				{
					getExecutorEnv().setDynAreaInfo(
							dynVO.getBaseInfoSet().getExAreaPK(), null);
				}
			} else
			{
				getExecutorEnv().setDynAreaInfo(null, null);
			}

			// modify by ljhua 2005-3-17 ��ʽ����󷵻ظ������ʽģ�͵�Ӧ��Ϊ��ʽ�������ݿ���ʽ�ִ�
			IParsed objUserLet = getFormulaProxy().parseUserDefExpr(
					strUserDefFormula);
			strReturn = objUserLet.toString(getExecutorEnv());

			// UfoCmdLet letTemp = getFormulaProxy().parseUserDefFormula(area,
			// strUserDefFormula);
			// strReturn = letTemp.toString(getExecutorEnv());

		} catch (DynAreaException e)
		{
			AppDebug.debug("DynAreaException", e);
		} finally
		{
			getExecutorEnv().setDynAreaInfo(strEnvDynPK, null);
			turnOnExtFuncCheck();
		}
		return strReturn;
	}

	/**
	 * ������������ؼ��֡��˷�������������ؼ��ֺ����
	 */
	public void resetMainKeyVos()
	{
		KeywordModel kModel = getDynAreaModel().getKeywordModel();

		if (kModel != null)
		{
			KeyVO[] keyVOs = kModel.getMainKeyVOs();
			getExecutorEnv().setKeys(keyVOs);
			reInitFormulaLet(true);
			reInitFormulaLet(false);

			// ��������˹�ʽ
			Vector vecComplex = getFormulaModel().getComplexCheckFml();
			checkRepCheckFml(vecComplex, false);

			Vector vecSimple = getFormulaModel().getSimpleCheckFml();
			checkRepCheckFml(vecSimple, true);

		}
	}

	/**
	 * ԭֻɾ����̬����ʽ��ʱ,������̬����ʽ��ɾ�� �ڲ�Ӱ�쵥Ԫ��ʽ��ָ�������¶�̬��֧���޸ģ�ɾ����̬������Ĺ�ʽ
	 *
	 * @param strDynPK
	 */
	public void removeDynamicArea(String strDynPK)
	{
		// ɾ����̬��ʱ��ɾ��������ع�ʽ
		if (dynCellFmlMap != null)
			dynCellFmlMap.remove(strDynPK);

		if (dynTotleFmlMap != null)
			dynTotleFmlMap.remove(strDynPK);

	}

	/**
	 * �ڲ�Ӱ�쵥Ԫ��ʽ��ָ�������¶�̬��֧���޸ģ�ɾ����̬������Ĺ�ʽ
	 *
	 * @param strDynPK
	 */
	public void updateDynamicArea(String strDynPK)
	{
		// ɾ������Ķ�̬����Ԫ��ʽ
		removeDynRedundantFml(strDynPK, FormulaModel.TYPE_CELL_FML);

		// ɾ������Ķ�̬�����ܹ�ʽ
		removeDynRedundantFml(strDynPK, FormulaModel.TYPE_TOTAL_FML);

	}

	/**
	 * ɾ����̬��������ع�ʽ
	 *
	 * @param strDynPK
	 * @param isInstantFml
	 */
	private void removeDynRedundantFml(String strDynPK, int type)
	{
		ExtendAreaCell dynAreaCell = this.getDynAreaModel()
				.getExtendAreaCellByPK(strDynPK);
		AreaPosition dynAreaPos = dynAreaCell.getArea();
		Hashtable<IArea, FormulaVO> hashDynFmls = this.getFormulaModel()
				.getDynFmls(strDynPK, type);
		for (IArea area : hashDynFmls.keySet())
		{
			if (dynAreaPos.contain(area))
			{
				continue;
			}
			this.getFormulaModel().removeDynRelatedFmlByType(strDynPK, area,
					type,FormulaModel.CALC_FML_ALL);
		}
	}

	/**
	 * ������λ��ת��Ϊ��Ԫδ֪�б�
	 *
	 * @param area
	 *            ����δ֪
	 * @return
	 */
	private static ArrayList<CellPosition> getSeperateCellPos(IArea area)
	{
		CellPosition startCell = area.getStart();
		CellPosition endCell = area.getEnd();
		ArrayList<CellPosition> list = new ArrayList<CellPosition>();
		int startRow = startCell.getRow();
		int startCol = startCell.getColumn();
		int endRow = endCell.getRow();
		int endCol = endCell.getColumn();
		for (int row = startRow; row <= endRow; row++)
		{
			for (int col = startCol; col <= endCol; col++)
			{
				CellPosition cellPos = CellPosition.getInstance(row, col);

				list.add(cellPos);
			}
		}
		return list;
	}

	/**
	 * ע������ͨ����ʽ���м�¼����������ʽģ�ͣ���ù�ʽ���ݵ����������Ҫ�ߴ˽ӿ�.
	 *
	 * @param areaKey
	 *            ��Ԫ����
	 * @param mapMainFormula
	 *            �������й�ʽ
	 * @return
	 */
	private FormulaVO getMainFmlByArea(IArea areaKey, Map mapMainFormula)
	{
		FormulaVO oFormula = (FormulaVO) mapMainFormula.get(areaKey);
		// modify by ljhua 2007-3-2 �����ϵ�Ԫ�׵�Ԫ�޷���������
		/*
		 * ������ϵ�Ԫ,mapMainFormula��ֻ��¼�׵�Ԫkey,����ʽ���м�¼��ϵ�Ԫ�����������Դ˴����⴦��
		 * mapMainFormula�ж�����ϵ�Ԫ�������幫ʽ��൥Ԫ�����׵�Ԫʱ����洢keyΪ�õ�Ԫ�����׵�Ԫ��
		 */
		// AppDebug.debug("��ʽarea="+areaKey.toString()+",��һ�β�ѯ��ʽoFormula="+oFormula);
		if (oFormula == null && areaKey.isCell() == false)
		{
			// ������ϵ�Ԫ���
			ArrayList<CellPosition> cells = getSeperateCellPos(areaKey);
			for (CellPosition cellTemp : cells)
			{
				oFormula = (FormulaVO) mapMainFormula.get(cellTemp);
				if (oFormula != null)
				{
					// AppDebug.debug("��ʽarea="+cellTemp.toString()+",���-��ѯ��ʽoFormula="+oFormula);
					break;
				}
			}
		}
		return oFormula;
	}

	/**
	 * ע������ͨ����ʽ���м�¼�����򼰶�̬����ʽģ�ͣ���ù�ʽ���ݵ����������Ҫ�ߴ˽ӿ�.
	 *
	 * @param areaKey
	 *            ��Ԫ����
	 * @param hashDynFmsl
	 *            ��̬�����й�ʽ
	 * @return
	 */
	private FormulaVO getDynFmlByArea(IArea areaKey, Map hashDynFmsl)
	{
		FormulaVO oFormula = (FormulaVO) hashDynFmsl.get(areaKey);
		// modify by ljhua 2007-3-2 �����ϵ�Ԫ�׵�Ԫ�޷���������
		if (oFormula == null && areaKey.isCell() == false)
		{
			oFormula = (FormulaVO) hashDynFmsl.get(areaKey.getStart());
		}
		return oFormula;
	}

	/**
	 * ����ָ����ʽ����
	 *
	 * @param formulaParsedDataItem
	 * @return ������ֵ
	 */
	public UfoVal[] calcFormula(IFormulaParsedDataItem formulaParsedDataItem)
	{
		if (!formulaParsedDataItem.isInDynArea())
		{
			return calcMainExpr(formulaParsedDataItem);
		} else
		{
			return calDynAreaExpr(formulaParsedDataItem);
		}
	}

	/**
	 * ��������ʽ����
	 *
	 * @param formulaParsedDataItem
	 * @return
	 * @i18n miufo00596=Ԥ�����������
	 * @i18n miufo00873=�ڹ�ʽ׷���У���ʽ��
	 * @i18n miufo00874=���������
	 */
	private UfoVal[] calcMainExpr(IFormulaParsedDataItem formulaParsedDataItem)
	{
		String strOld = (String) getExecutorEnv().getExEnv(
				CommonExprCalcEnv.EX_CALCEXFUNC);
		getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_CALCEXFUNC,
				CommonExprCalcEnv.EX_VALUE_ON);
		// @edit by ll at 2008-12-27,����10:07:24 ����׷�ٲ�����Ϣ
		if (formulaParsedDataItem.isInTraceNow())
			getExecutorEnv().setMeasureTraceVOs(new MeasureTraceVO[0]);
		try
		{
			try
			{
				CmdProxy.preCalcExtFuncExpr(formulaParsedDataItem
						.getTracedExpr(), getExecutorEnv(), 3);
			} catch (Exception e)
			{
				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0477")/*@res "Ԥ�����������"*/,
						e);
			}

			getExecutorEnv().setDynAreaInfo(null, null);
			return formulaParsedDataItem.getTracedExpr().getValue(
					getExecutorEnv());
		} catch (CmdException e)
		{
			AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0916")/*@res "�ڹ�ʽ׷���У���ʽ��"*/
					+ formulaParsedDataItem.getTracedExpr().toUserDefString(
							getExecutorEnv())
					+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0917")/*@res "���������"*/);
		}

		getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_CALCEXFUNC, strOld);
		return new UfoVal[] { UfoNullVal.getSingleton() };

	}

	/**
	 * ���㶯̬����ʽ����
	 *
	 * @param formulaParsedDataItem
	 * @return
	 * @i18n miufo00873=�ڹ�ʽ׷���У���ʽ��
	 */
	private UfoVal[] calDynAreaExpr(IFormulaParsedDataItem formulaParsedDataItem)
	{
		try
		{
			// ����̬������йؼ���ֵ�洢�ڼ��㻷����
			String strDynPK = formulaParsedDataItem.getDynAreaPK();
			ExtendAreaCell dynArea = DynamicAreaModel.getInstance(
					getCellModel()).getExtendAreaCellByPK(strDynPK);

			// ������̬����ʽ��
			if (getDynCellFormList(strDynPK) == null)
			{
				setupDynCellFmlList(strDynPK);
			}

			// ȡ�ö�̬����ȫ����ʽ
			// Vector<Vector<IArea>> areaGradeVector =
			// getDynCellFormList(strDynPK);
			// Vector<IArea> vecDynFmlAreas = new Vector<IArea>();
			// for(Vector<IArea> vec : areaGradeVector){
			// vecDynFmlAreas.addAll(vec);
			// }
			// IArea[] areas = new IArea[vecDynFmlAreas.size()];
			// vecDynFmlAreas.toArray(areas);

			// ���㶯̬�������������Ӳ���
			// int stepRow = dynArea.isRowDirection() ? dynArea.getOriArea()
			// .getHeigth() : 0;
			// int stepCol = dynArea.isRowDirection() ? 0 : dynArea.getOriArea()
			// .getWidth();

			// ��ʼ������˳��������
			IUFOTableData dataChannel = (IUFOTableData) getExecutorEnv()
					.getDataChannel();
			KeyDataGroup[] objKeydatas = getAllDynKeyDataGroups(strDynPK);
			if (objKeydatas == null || objKeydatas.length == 0)
			{
				return null;
			}

			// ����̬������йؼ���ֵ�洢�ڼ��㻷����
			KeyDataGroup keyDataGroup = objKeydatas[formulaParsedDataItem
					.getUnitDataNum()];
			objKeydatas = new KeyDataGroup[] { keyDataGroup };


			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>> groupFuncValues =
				new Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>>();

			// �������в�����ָ�꺯���Ĳ�����������������
//			Hashtable hashFinaFuncs = batchCalcDynFinafunc(dynArea,
//					formulaParsedDataItem.getTracedExpr(), objKeydatas,
//					getExecutorEnv(), true, groupFuncValues);
//			Hashtable hashFinaFuncs = null;

//			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);
			
			UfoExpr traceExpr = formulaParsedDataItem.getTracedExpr();
			boolean isCodeFill = isCodeFill(traceExpr);

			// �������ݼ�������̬������
			Hashtable<Integer, Hashtable<String, Object>> mapField2Values = getDynAreaField2KeyValues(
					dynArea, FormulaModel.TYPE_CELL_FML, isCodeFill);
			this.getExecutorEnv().setDynField2Values(mapField2Values);

			// ������㵥Ԫ��ʽ
			this.getExecutorEnv().setDynAreaInfo(strDynPK, keyDataGroup);
			// @edit by ll at 2008-12-26,����10:07:24 ����׷�ٲ�����Ϣ
			if (formulaParsedDataItem.isInTraceNow())
				getExecutorEnv().setMeasureTraceVOs(new MeasureTraceVO[0]);
			dataChannel.setDynAreaCalcParam(new IUFODynAreaDataParam(
					formulaParsedDataItem.getUnitDataNum(), null, strDynPK));
			// @edit by wuyongc at 2013-6-17,����2:19:03 ���꣬����һ��������û�������κ�ֵ����IF������֧������
//			if (hashFinaFuncs == null || hashFinaFuncs.size() == 0)
//			{
				return formulaParsedDataItem.getTracedExpr().calcExpr(
						getExecutorEnv(), 0, -1);
//			} else
//			{
//				UfoFunc func = null;
//				if (hashFinaFuncs.get(keyDataGroup) != null)
//				{
//					func = (UfoFunc) ((ArrayList) hashFinaFuncs
//							.get(keyDataGroup)).get(0);
//				} else
//				{
//					func = (UfoFunc) ((ArrayList) hashFinaFuncs.values()
//							.iterator().next()).get(0);
//				}
//
//				if(groupFuncValues.get(func) != null){
//					return groupFuncValues.get(func).get(keyDataGroup);
//				} else {
//					return func.getValue(getExecutorEnv());
//				}
////				return func.getValue(getExecutorEnv());
//			}
		} catch (CmdException e)
		{
			AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0916")/*@res "�ڹ�ʽ׷���У���ʽ��"*/
					+ formulaParsedDataItem.getTracedExpr().toUserDefString(
							getExecutorEnv()) + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0917")/*@res "���������"*/);
		} finally
		{
			getExecutorEnv().setDynAreaInfo(null, null);
		}
		return new UfoVal[] { UfoNullVal.getSingleton() };

	}

	/**
	 * �жϸñ��ʽ�Ƿ����ִ���������
	 *
	 * @create by wangyga at 2009-1-13,����06:26:33
	 *
	 * @param parsedLet
	 * @return
	 */
	private boolean isCalcDataSetFunc(IParsed parsedLet)
	{
		if (parsedLet == null)
		{
			return true;
		}
		UfoExpr ufoExpr = null;
		if (parsedLet instanceof UfoExpr)
		{
			ufoExpr = (UfoExpr) parsedLet;
		} else if (parsedLet instanceof UfoCmdLet)
		{
			ufoExpr = (UfoExpr) ((UfoCmdLet) parsedLet).getLetList().get(1);// ȡ�������ұ߱��ʽ
		}
		ReportDynCalcEnv env = getExecutorEnv();
		if (env != null
				&& env.getExEnv(CommonExprCalcEnv.EX_CALCEXFUNC).equals(
						CommonExprCalcEnv.EX_VALUE_OFF))
		{
			try
			{
				if (ufoExpr.isDataSetFuncExpr(env))
				{
					return false;
				}
			} catch (CmdException e)
			{
				AppDebug.debug(e);
				return true;
			}
		}
		return true;
	}

	public KeyDataGroup[] getAllDynKeyDataGroups(String strDynPK)
	{
		Object[] objValues = getExecutorEnv().getUfoDataChannel()
				.getMetaKeyValues(strDynPK, true);
		if (objValues == null || objValues.length == 0)
			return null;

		List<KeyDataGroup> vKeyData = new ArrayList<KeyDataGroup>();
		for (int i = 0, iLen = objValues.length; i < iLen; i++)
		{
			KeyDataGroup keyData = (KeyDataGroup) objValues[i];
			if (keyData == null || keyData.getKeyDatas() == null
					|| keyData.getKeyDatas().length <= 0)
				continue;
			vKeyData.add((KeyDataGroup) (objValues[i]));
		}
		return vKeyData.toArray(new KeyDataGroup[0]);
	}

	/**
	 * �õ���̬���ؼ��ֶ���(�ؼ���λ�ã��ؼ���)
	 *
	 * @create by liuchuna at 2010-12-1,����02:19:06
	 *
	 * @param dynAreaVO
	 * @return
	 */
	public Object[][] getKeyPosVOsByDynArea(ExtendAreaCell dynAreaVO)
	{
		Hashtable ht = getKeywordModel().getDynKeyVOPos(dynAreaVO.getBaseInfoSet().getExAreaPK());
		AreaPosition oriArea = DynAreaUtil.getFormatGroupArea(dynAreaVO, getDataModel());
		Object[][] rtnObjss = new Object[ht.size()][2];
		int i = 0;
		for (int dRow = 0; dRow < oriArea.getHeigth(); dRow++)
		{
			for (int dCol = 0; dCol < oriArea.getWidth(); dCol++)
			{
				CellPosition oriPos = (CellPosition) oriArea.getStart()
						.getMoveArea(dRow, dCol);
				KeyVO keyVO = getKeywordModel().getKeyVOByPos(oriPos);
				if (keyVO != null)
				{
					rtnObjss[i][0] = oriPos;
					rtnObjss[i][1] = keyVO;
					i++;
				}
			}
		}
		return rtnObjss;
	}

	/**
	 * ��������λ�á���ʽ���ͽ���ʽ���뵽��ʽģ����
	 * ���ù�ʽ���ͣ��ϲ���ʽ�����㹫ʽ
	 *
	 * @create by liuchuna at 2010-9-8,����04:49:42
	 *
	 * @param area
	 * @param strFormula
	 * @param fmlType
	 * @return
	 * @throws Exception
	 */
	public boolean addFormulaByFmlType(IArea area, String strFormula, int fmlType) throws Exception {
		if (strFormula == null || strFormula.trim().length() == 0 || area == null) {
			return false;
		}
		//tianchuan 20141009 �ϲ������㹫ʽ֧��ѭ�����õ�У��
		IParsed dbLet=null;
		try {
			turnOffExtFuncCheck();
			if (dbLet == null) {
				//tianchuan 20141015 �ϲ������㹫ʽ��������Db�͵�
				IParsed objUserLet = getFormulaProxy().parseExpr(strFormula);
				if (MultiLangUtil.LANGCODE_ZH.equals(MultiLangUtil.LocalLan)) {
					dbLet = objUserLet;
				} else {
					dbLet = getFormulaProxy().parseExpr(strFormula);
				}
			}
		} finally {
			turnOnExtFuncCheck();
		}
		// ����У��ѭ����ʽ���洢��ʽ̬��λ��
		Vector<String> areaVec = new Vector<String>();
		//tianchuan 2013.1.22 ����̫�ң���������һ��ת������FormulaManageConst�еĳ���ת��ΪFormulaModel�еĳ�������������
		int fmlTypeInFmlModel=-1;
		switch (fmlType) {
			case FormulaManageConst.ZS_FML_NUM:
				fmlTypeInFmlModel=FormulaModel.TYPE_ZS_FML;
				break;
			default:
				fmlTypeInFmlModel=FormulaModel.TYPE_CONS_FML;
				break;
		}
		// У���Ƿ����ѭ�����ã�ע��isLoopRef�д��Ĺ�ʽ����һ����FormulaModel��ĳ���������������
		Vector<String> dealedVec = new Vector<String>();
		if(isLoopRef(areaVec, area, dbLet, strFormula, fmlTypeInFmlModel, true, dealedVec)){
			throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0907")/*@res "����Ĺ�ʽ����ѭ�����ã�"*/);
		}
		//tianchuan 20141009  end
		
		// ��������λ���жϸ������Ƿ��ڶ�̬����
		ExtendAreaCell dynVO = null;
		try {
			dynVO = getValidDynamicArea(area);
		} catch (DynAreaException e) {
			return false;
		}

		boolean bReturn = false;
		if (dynVO != null) {
			// ��Ӷ�̬����ʽ
			String strDynPK = dynVO.getBaseInfoSet().getExAreaPK();
			bReturn = addDynFormulaByType(strDynPK, area, strFormula, fmlType);
		} else {
			// �������Ĺ�ʽ
			bReturn = addMainFormulaByType(area, strFormula, fmlType);
		}
		return bReturn;

	}

	public boolean removeFormulaByFmlType(IArea area, int fmlType) throws Exception {
		if (area == null) {
			return false;
		}

		// ��������λ���жϸ������Ƿ��ڶ�̬����
		ExtendAreaCell dynVO = null;
		try {
			dynVO = getValidDynamicArea(area);
		} catch (DynAreaException e) {
			return false;
		}

		boolean bReturn = false;
		if (dynVO != null) {
			// ɾ����̬����ʽ
			String strDynPK = dynVO.getBaseInfoSet().getExAreaPK();
			bReturn = removeDynFormulaByType(strDynPK, area, fmlType);
		} else {
			// ɾ������Ĺ�ʽ
			bReturn = removeMainFormulaByType(area, fmlType);
		}
		return bReturn;

	}

	private boolean addMainFormulaByType(IArea area, String strFormula, int fmlType)
			throws Exception {

		try {
			turnOffExtFuncCheck();

			// �̶����������̬��Ϣ
			getExecutorEnv().setDynAreaInfo(null, null);

			FormulaVO fe = new FormulaVO(getReportPK(), strFormula);
			getFormulaModel().setFmlByFmlType(ExtendAreaConstants.MAINTABLE_DYNAREAPK, area, fe, fmlType);

			return true;

		} catch (Exception e) {
			AppDebug.debug(strFormula + " is wrong", e);
			throw e;
		} finally {
			turnOnExtFuncCheck();
		}

	}

	private boolean removeMainFormulaByType(IArea area, int fmlType) throws Exception {

		try {
			turnOffExtFuncCheck();

			getExecutorEnv().setDynAreaInfo(null, null);

			getFormulaModel().removeFmlByFmlType(ExtendAreaConstants.MAINTABLE_DYNAREAPK, area, fmlType);

			return true;

		} catch (Exception e) {
			AppDebug.debug(e);
			throw e;
		} finally {
			turnOnExtFuncCheck();
		}

	}

	public void addSimpleCheckFml(SimpleCheckFmlVO vo){
		getFormulaModel().addSimpCheckFml(vo);
	}

	public void addComplexCheckFml(RepCheckVO vo){
		getFormulaModel().addComplexCheckFml(vo);
	}

	public void addBalanceFml(BalanceFmlVO balance){
		getFormulaModel().addBalanceFml(balance);
	}

	private boolean addDynFormulaByType(String strDynPK, IArea area,
			String strFormula, int fmlType) throws Exception {

		if (strDynPK == null)
			return false;

		try {
			turnOffExtFuncCheck();

			// ��̬������Ҫ���û�������
			if (getExecutorEnv().getDynArea() == null
					|| !(getExecutorEnv().getDynArea().equals(strDynPK))) {
				getExecutorEnv().setDynAreaInfo(strDynPK, null);
			}

			FormulaVO fVO = new FormulaVO(getReportPK(), strFormula);

			getFormulaModel().setFmlByFmlType(strDynPK, area, fVO, fmlType);
			return true;
		} catch (Exception e) {
			AppDebug.debug(strFormula + " is wrong", e);
			throw e;
		} finally {
			turnOnExtFuncCheck();
		}
	}

	private boolean removeDynFormulaByType(String strDynPK, IArea area, int fmlType) throws Exception {

		if (strDynPK == null)
			return false;

		try {
			turnOffExtFuncCheck();

			if (getExecutorEnv().getDynArea() == null
					|| !(getExecutorEnv().getDynArea().equals(strDynPK))) {
				getExecutorEnv().setDynAreaInfo(strDynPK, null);
			}

			getFormulaModel().removeFmlByFmlType(strDynPK, area, fmlType);
			return true;
		} catch (Exception e) {
			AppDebug.debug(e);
			throw e;
		} finally {
			turnOnExtFuncCheck();
		}
	}

	public FormulaVO getPublicDirectFml(IArea pos){
		return getFormulaModel().getPublicDirectFml(pos);
	}

	public FormulaVO getPersonalDirectFml(IArea pos){
		return getFormulaModel().getPersonalDirectFml(pos);
	}

	public FormulaVO getZhesDirectFml(IArea pos){
		return getFormulaModel().getZhesDirectFml(pos);
	}

	public FormulaVO getTotalDirectFml(IArea pos){
		return getFormulaModel().getDirectFml(pos, FormulaModel.TYPE_TOTAL_FML);
	}

	public FormulaVO getConsDirectFml(IArea pos){
		return getFormulaModel().getConsDirectFml(pos);
	}

	public void clearFormulaByType(int fmlType) {
		try {
			getFormulaModel().removeFmlByType(fmlType);
		} finally {
		}
	}

	public void clearCheckFormula(){
		getFormulaModel().clearCheckFormula();
	}

	public void removeCheckFmlById(String fmlId){
		getFormulaModel().removeCheckFmlById(fmlId);
	}

	public void removeBalanceFmlById(String fmlId){
		getFormulaModel().removeBalanceFmlById(fmlId);
	}

	/**
	 * ��ʽ������̵��쳣����
	 */
	private class DynAreaException extends Exception
	{
		private static final long serialVersionUID = -669244229511143894L;

		public DynAreaException()
		{
			super();
		}
	}

	@SuppressWarnings("unused")
	private void initVisableOrgs() {
		// liuchun+ at 20110519 ���ݿɼ��Է�Χ���õ�ǰ��֯���Բ�����Щ��֯�еı���
		// ��Ե��빫ʽ�������ֶ���д��������֯�ı����е�ָ������
		String pk_group = (String)getContextVO().getAttribute(IUfoContextKey.CUR_GROUP_PK);
		String strCurOrgPK = (String)getContextVO().getAttribute(IUfoContextKey.CUR_REPORG_PK);

		Integer operType = (Integer)getContextVO().getAttribute(IUfoContextKey.OPERATION_STATE);
		if(operType != null) {
			if(IUfoContextKey.OPERATION_INPUT == operType.intValue()) {
				// ����¼��̬����Ҫ�ɼ��Է�Χ
				return;
			}
		}

		NODE_TYPE type = null;
		if (strCurOrgPK.equals(IOrgConst.GLOBEORG)) {
			type = NODE_TYPE.GLOBE_NODE;
		} else if (strCurOrgPK.equals(pk_group)) {
			type = NODE_TYPE.GROUP_NODE;
		} else {
			type = NODE_TYPE.ORG_NODE;
		}

    	try {
			String[] orgs = ReportSrvUtil.getVisibleOrgs(strCurOrgPK, pk_group, type, "e4e50462-4fe9-4e0c-8933-4cad2996fe9c");
			getCalcEnv().setOrgs(orgs);
			getCalcEnv().setCheckOrgVisable(true);
		} catch (BusinessException e) {
			AppDebug.debug(e);
		}
	}

	public FormulaModel getFormulaModel() {
		FormulaModel formulaModel = DynAreaUtil.getFormulaModel(getDataModel());
		if(formulaModel.getUfoFmlExecutor() == null) {
			formulaModel.setUfoFmlExecutor(this);
		}
		return formulaModel;
	}

	/**
	 * ������ݿ���ʽ�Ĺ�ʽ���ݣ����ɹ��򷵻ؼ�����������׳��쳣
	 *@author by tanyj at 2012-8-25 ����9:15:09
	 * @param fVO
	 * @param areaKey
	 * @return
	 * @throws Exception
	 */
	public IParsed parseDBFml(FormulaVO fVO,IArea areaKey){
		try{
			IParsed parsedLet = null;
			// �����ʽ����Ϊ�գ��������ʽ,�����ù�ʽ�������
			if (fVO.getLet() == null) {
				IArea realArea = DynAreaUtil.getRealArea(areaKey, getDataModel());
				if (isInDynArea(realArea, getDataModel())) {
					ExtendAreaCell[] dynAreas = getDynAreaModel().getDynAreaCells();
					int iLen = dynAreas != null ? dynAreas.length : 0;
					for (int j = 0; j < iLen; j++)
					{
						String strDynPK = dynAreas[j].getBaseInfoSet().getExAreaPK();
						getExecutorEnv().setDynAreaInfo(strDynPK, null);
					    parsedLet = getFormulaProxy().parseExpr(fVO.getFormulaContent());
					}
				} else {
					parsedLet = getFormulaProxy().parseFormula(areaKey, fVO.getFormulaContent());
				}
//				fVO.setLet(parsedLet);
			}
			return parsedLet;
		}catch(ParseException e){
			fVO.setErrorFml(true);
		}
		return null;
	}
}