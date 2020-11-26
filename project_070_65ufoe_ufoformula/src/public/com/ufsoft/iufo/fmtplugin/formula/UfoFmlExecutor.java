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
 * UFO公式执行器
 *
 * @author liuchuna
 * @created at 2010-5-5,上午10:10:57
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class UfoFmlExecutor extends BaseFmlExecutor
{
	//tianchuan 区分各个不同类型的公式
	private static final int PUBLIC_CELL_FML=0;	//公有公式
	private static final int PRIVATE_CELL_FML=1;	//私有公式
	private static final int TOTAL_FML=2;	//汇总公式
	private static final int ZS_FML=3;		//折算公式
	private static final int CONS_FML=4;		//合并公式

	// 主表单元公式计算链
	private Vector<Vector<IArea>> mainCellFmlList;

	// 合并公式计算链
	private Vector<Vector<IArea>> allHBFmlList;

	// 动态区单元公式计算链. key=动态区pk,value=某动态区对应单元公式链
	private Map<String, Vector<Vector<IArea>>> dynCellFmlMap;

	// 主表汇总公式计算链
	private Vector<Vector<IArea>> mainTotleFmlList;

	// 动态区汇总公式计算链. key=动态区pk,value=某动态区对应汇总公式链
	private Map<String, Vector<Vector<IArea>>> dynTotleFmlMap;

	private Object diCalcExecutor = null;
	//edit by congdy hr 数据方案计算执行器
	private Object hrCalcExecutor = null;

	/**
	 * FormulaHandler 构造字注解
	 *
	 * @param contextVO
	 *            此类使用UfoContextVO方法:
	 *            getContextId,getPubDataVO,isModel,getDataSource
	 *            ,isOnServer,getCurUserId getLoginTime,getCurrentLan
	 * @param cellModel
	 */
	protected UfoFmlExecutor(IContext contextVO, CellsModel cellModel)
	{
		this(contextVO, cellModel, false);
	}

	/**
	 * FormulaHandler 构造字注解
	 *
	 * @param contextVO
	 *            此类使用UfoContextVO方法:
	 *            getContextId,getPubDataVO,isModel,getDataSource
	 *            ,isOnServer,getCurUserId getLoginTime,getCurrentLan
	 * @param cellModel
	 * @param bParseForm
	 *            是否解析公式(包括所有的单元公式、汇总公式)
	 */
	protected UfoFmlExecutor(IContext contextVO, CellsModel cellModel,
			boolean bParseForm)
	{
		super(contextVO, cellModel);

		if (bParseForm == true)
			initFormulaLet();

		// liuchun+ at 20110519 根据可见性范围设置当前组织可以参照哪些组织中的报表
		// 针对导入公式、或者手动的写入其它组织的报表中的指标的情况
//		initVisableOrgs();
	}

	public static UfoFmlExecutor getInstance(IContext contextVO,
			CellsModel cellModel)
	{
		return getInstance(contextVO, cellModel, false);
	}

	/**
	 * v56,统一公式链构建入口,使用编译结果缓存。但有动态区和个性化公式时，每次构建新公式链 由于56是中间版本，此缓存停用。
	 * UfoFmlExecutor缓存的卸除建议使用CacheManager钩子的方式，保证和CellsModel一起卸出缓存。
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
	 * 初始化全部的公式链(包括所有的单元公式、汇总公式)
	 *
	 * @create by liuchuna at 2010-5-5,上午10:55:39
	 *
	 */
	private void initFormulaLet()
	{
		try
		{
			// 单元公式公式链
			initCellFormulaLet();

			// 汇总公式公式链
			initTotleFormulaLet();
		} catch (Exception e)
		{
			AppDebug.debug("initFormulaLet", e);
		}
	}

	/**
	 * 建立单元公式链(包括主表、子表公式链)
	 *
	 * @create by liuchuna at 2010-5-5,上午10:53:51
	 *
	 */
	private void initCellFormulaLet()
	{
		// 建主表公式链
		if (getMainCellFormList() == null)
		{
			setupMainCellFmlList();
		}

		// 建子表公式链
		ExtendAreaCell[] dynAreas = getDynAreaModel().getDynAreaCells();
		int iLen = dynAreas != null ? dynAreas.length : 0;
		for (int j = 0; j < iLen; j++)
		{
			String strDynPK = dynAreas[j].getBaseInfoSet().getExAreaPK();
			if (getDynCellFormList(strDynPK) == null)
			{
				// 建立公式链
				setupDynCellFmlList(strDynPK);
			}
		}
		getExecutorEnv().setDynAreaInfo(null, null);

	}

	/**
	 * 建立汇总公式链(包括主表、子表公式链)
	 *
	 * @create by liuchuna at 2010-5-5,上午10:53:17
	 *
	 */
	private void initTotleFormulaLet()
	{
		// 建主表公式链
		if (getMainTotleFormList() == null)
		{
			setupMainTotleFmlList();
		}

		// 建子表公式链
		ExtendAreaCell[] dynAreas = getDynAreaModel().getDynAreaCells();
		int iLen = dynAreas != null ? dynAreas.length : 0;
		for (int j = 0; j < iLen; j++)
		{

			String strDynPK = dynAreas[j].getBaseInfoSet().getExAreaPK();
			if (getDynTotleFmList(strDynPK) == null)
			{
				// 建立公式链
				setupDynTotleFmlList(strDynPK);
			}
		}
		getExecutorEnv().setDynAreaInfo(null, null);

	}

	/**
	 * 重新构建单元公式链
	 */
	public void reInitCellFmlLet()
	{
		reInitFormulaLet(true);

	}

	/**
	 * 重新构建整个公式链
	 *
	 * @param bCell
	 *            true/false 单元公式/汇总公式
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
	 * 获得主表单元公式链
	 *
	 * @create by liuchuna at 2010-5-5,上午10:38:51
	 *
	 * @return
	 */
	private Vector<Vector<IArea>> getMainCellFormList()
	{
		return mainCellFmlList;
	}

	/**
	 * 获得主表汇总公式链
	 *
	 * @create by liuchuna at 2010-5-5,上午10:39:13
	 *
	 * @return
	 */
	private Vector<Vector<IArea>> getMainTotleFormList()
	{
		return mainTotleFmlList;
	}

	/**
	 * 获得某动态区的单元公式链
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
	 * 获得某动态区的汇总公式链
	 *
	 * @create by liuchuna at 2010-5-6,下午04:52:11
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
	 * 删除动态区公式链中指定动态区的公式链
	 *
	 * @create by liuchuna at 2010-5-6,下午04:52:41
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
	 * 获得指定公式区域对应的有效动态区域
	 *
	 * @param area
	 * @return
	 * @throws DynAreaException
	 */
	public ExtendAreaCell getValidDynamicArea(IArea area)
			throws DynAreaException
	{
		// 获得区域覆盖的动态区
		ExtendAreaCell[] dynCells = getDynAreaModel()
				.getDynAreaCellByArea(area);

		if (dynCells != null && dynCells.length > 1)
		{
			// 动态区域交叉,不能定义公式
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
				// 区域不能同时还有固定区域和动态区域
//				throw new DynAreaException();
				return null;
			}
			// 选中的单元有动态区域,只能定义在动态区域上
			dynVO = dynCells[0];
		}
		return dynVO;
	}
	
	/**
	 * 根据实际区域获得所在动态区
	 * 
	 * @param area
	 * @return
	 * @throws DynAreaException
	 */
	public ExtendAreaCell getDynCellByRealArea(IArea area) throws DynAreaException {
		// 获得区域覆盖的动态区
		ExtendAreaCell[] dynCells = getDynAreaModel().getDynAreaCellByArea(area);
		ExtendAreaCell dynVO = null;
		if (dynCells != null && dynCells.length >= 1) {
			dynVO = dynCells[0];
		}
		
		return dynVO;
	}

	/**
	 * 增加用户形式的公式
	 *
	 * @param showMessage
	 * @param area
	 * @param strUserDefFormula
	 * @param bPublic
	 *            此参数只有在 bCell=true时有意义。含义为添加的是否为公有公式。 bPublic=true
	 *            且当前为创建单位时，才能加入公有公式。 bPublic=false 个性化公式
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
	 * 增加用户形式的公式
	 *
	 * @param showMessage
	 * @param area
	 * @param strUserDefFormula
	 * @param bPublic
	 *            此参数只有在 bCell=true时有意义。含义为添加的是否为公有公式。 bPublic=true
	 *            且当前为创建单位时，才能加入公有公式。 bPublic=false 个性化公式
	 * @param bClearOldFormula
	 *            true 表示不清除主表原有公式，false表示清楚主表原有公式
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
	 * 增加数据库形式的公式
	 *
	 * @param showMessage
	 * @param area
	 * @param strdbDefFormula
	 * @param dbLet
	 * @param bPublic
	 *            此参数只有在 bCell=true时有意义。含义为添加的是否为公有公式。 bPublic=true
	 *            且当前为创建单位时，才能加入公有公式。 bPublic=false 个性化公式
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
	 * 增加数据库形式的公式
	 *
	 * @param showMessage
	 * @param area
	 * @param strdbDefFormula
	 * @param dbLet
	 * @param bPublic
	 *            此参数只有在 bCell=true时有意义。含义为添加的是否为公有公式。 bPublic=true
	 *            且当前为创建单位时，才能加入公有公式。 bPublic=false 个性化公式
	 * @param bClearOldFormula
	 *            true 表示不清除主表原有公式，false表示清楚主表原有公式
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
	 * 增加公式
	 *
	 * @param showMessage
	 *            加入公式时的提示信息
	 * @param area
	 *            公式区域
	 * @param strFormula
	 *            公式内容。如果bUserDef=true 则表示用户显示方式的内容。否则为数据库形式的公式内容
	 * @param dbLet
	 *            数据库形式的公式解析对象
	 * @param bUserDef
	 * @param bPublic
	 *            此参数只有在 bCell=true时有意义。含义为添加的是否为公有公式。 bPublic=true
	 *            且当前为创建单位时，才能加入公有公式。 bPublic=false 个性化公式
	 * @param bOnlyAddFormList
	 *            true 表示只增加到公式链中，并不修改数据模型。false表示即增加到公式链中，又修改数据模型
	 * @param bClearOldFormula
	 *            true 表示不清除主表原有公式，false表示清楚主表原有公式
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

		// 以下为主表公式处理
		String strDbFormula = null;
		try
		{
			// if (strFormula == null) {
			// strDbFormula = dbLet.toString();
			// }

			// if (strFormula == null || strFormula.length()==0) {
			// return true;
			// }

			// add by ljhua 2005-3-18 无法检查外部函数，所以分析函数时关闭检查开关
			turnOffExtFuncCheck();

			// 固定区域清除动态信息
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

			// 清除主表原有公式
			if (bClearOldFormula == true)
			{
				if(bPublic){
					clearDynFormula(showMessage, null, area, type, FormulaModel.CALC_FML_PUBLIC);
				} else {
					clearDynFormula(showMessage, null, area, type, FormulaModel.CALC_FML_PERSONAL);
				}

			}

			// 加到公式链中
			// 添加公式时，不再需要将公式加入到公式链中
			/*
			 * if (getMainFormList(bCell) == null) {
			 * setupMainFormulaList(bCell); } Vector alist =
			 * AreaFormulaUtil.getAreaList(dbLet); boolean bAddSuccess =
			 * getMainFormList(bCell).addFormula(area, alist);
			 */

			boolean bAddSuccess = true;

			if (bAddSuccess == true && bOnlyAddFormList == false)
			{
				//修改内存泄露
//				FormulaVO fe = new FormulaVO(getReportPK(), strDbFormula, dbLet);
				FormulaVO fe = new FormulaVO(getReportPK(), strDbFormula);
				fe.setRelative(AreaFormulaUtil.isRelativeFormula(dbLet));
				fe.setUserDefContent(dbLet.toUserDefString(getExecutorEnv()));
				
				getFormulaModel().setMainFmlVO(area, fe, type, bPublic);
				// 清除个性化公式标记
				// getFormulaModel().removePersonalFormulaByArea(area);

				// 调整语义模型引用关系
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
	 * 增加公式
	 *
	 * @param showMessage
	 * @param area
	 *            公式区域
	 * @param strFormula
	 *            公式内容。如果bUserDef=true 则表示用户显示方式的内容。否则为数据库形式的公式内容
	 * @param dbLet
	 *            数据库形式的公式解析对象
	 * @param bUserDef
	 * @param bPublic
	 *            此参数只有在 bCell=true时有意义。含义为添加的是否为公有公式。 bPublic=true
	 *            且当前为创建单位时，才能加入公有公式。 bPublic=false 个性化公式
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
	 * 增加公式
	 *
	 * @param showMessage
	 * @param area
	 *            公式区域
	 * @param strFormula
	 *            公式内容。如果bUserDef=true 则表示用户显示方式的内容。否则为数据库形式的公式内容
	 * @param dbLet
	 *            数据库形式的公式解析对象
	 * @param bUserDef
	 * @param bPublic
	 *            此参数只有在 bCell=true时有意义。含义为添加的是否为公有公式。 bPublic=true
	 *            且当前为创建单位时，才能加入公有公式。 bPublic=false 个性化公式
	 * @param bClearOldFormula
	 *            true 表示不清除主表原有公式，false表示清除主表原有公式
	 * @return
	 */
	private boolean addFormula(StringBuffer showMessage, IArea area,
			String strFormula, IParsed dbLet, boolean bUserDef, int type,
			boolean bPublic, boolean bClearOldFormula, boolean isCheckLoop) throws ParseException
	{
		
		if(area!=null){	//tianchuan 2013.5.10  防止误操作造成区域过界，进而导致CellsModel过大无法打开的问题
			if(area.getStart().getRow()>=DefaultSetting.MAX_ROW_NUM ||
					area.getStart().getColumn()>=DefaultSetting.MAX_COL_NUM || 
					area.getEnd().getRow()>=DefaultSetting.MAX_ROW_NUM || 
					area.getEnd().getColumn()>=DefaultSetting.MAX_COL_NUM){
				throw new ParseException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413013-0030")/*@res "公式区域非法"*/);
			}
		}
		
		if(area instanceof CellPosition) {
			// 先保留5系列逻辑，单元公式的位置类型都是AreaPosition类型
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

		// 判断是否定义在动态区中
		ExtendAreaCell dynVO = null;
		try {
			dynVO = getValidDynamicArea(area);
		} catch (DynAreaException e) {
			return false;
		}
		// 设置动态区环境信息,解析时需要
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

		// 用于校验循环公式，存储格式态的位置
		Vector<String> areaVec = new Vector<String>();
		//begin-ncm-rendp-NCdp205348537-2015-4-20-专项 
		/** 
		* 此补丁修正了产品BUG:判断单元格循环引用效率优化——判断过的单元格加入缓存，避免重复判断
		*/ 
		// 用于存储进行过判断处理的单元格
		Vector<String> dealedVec = new Vector<String>();
		// 校验是否存在循环引用
		//if(isCheckLoop && isLoopRef(areaVec, area, dbLet, strFormula, type, bUserDef)){
		if(isCheckLoop && isLoopRef(areaVec, area, dbLet, strFormula, type, bUserDef, dealedVec)){
			showMessage.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0907")/*@res "定义的公式存在循环引用！"*/);
			return false;
		}
		//end-ncm-rendp-NCdp205348537-2015-4-20-专项 

		boolean bReturn = false;
		FormulaVO fvo = null;
		if (dynVO != null)
		{
			String strDynPK = dynVO.getBaseInfoSet().getExAreaPK();
			// 动态区域公式之只能定义到指标单元上
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

				// 根据公式数值类型,更改单元格类型。如果是指标单元，则不改
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
	 * 检查每一个单元中所有类型的公式，如果存在与当前区域矛盾的公式区域，返回false
	 */
	private boolean checkAllTypeFmlsInEveryCell(StringBuffer showMessage,IArea area,boolean bPublic){
		if(area.getStart()==area.getEnd()){
			return true;
		}
		//2013.4.2 ++ 行列通配不进行这样的检查
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
				if(bPublic){	//如果不是公有公式（私有公式），则允许覆盖。因为私有公式可以覆盖公有公式
					//检查公有公式
					if(!checkOneTypeFmlInOneCell(showMessage,area,allCells[i],PUBLIC_CELL_FML)){
						return false;
					}
				}

				//检查私有公式
				if(!checkOneTypeFmlInOneCell(showMessage,area,allCells[i],PRIVATE_CELL_FML)){
					return false;
				}
				//检查汇总公式
				if(!checkOneTypeFmlInOneCell(showMessage,area,allCells[i],TOTAL_FML)){
					return false;
				}
				//检查折算公式
				if(!checkOneTypeFmlInOneCell(showMessage,area,allCells[i],ZS_FML)){
					return false;
				}
				//检查合并公式
				if(!checkOneTypeFmlInOneCell(showMessage,area,allCells[i],CONS_FML)){
					return false;
				}
			}
		}
		return true;
	}

	/*
	 * 检查某一类型在某一单元上的公式
	 */
	private boolean checkOneTypeFmlInOneCell(StringBuffer showMessage,IArea area,CellPosition cellPos,int fmlType){
		Object[] tempObjs=getRelatedFmlVO(getFormulaModel(),cellPos, fmlType);
		if(tempObjs!=null && tempObjs.length>=2){
			if(tempObjs[1]!=null){	//公式存在
				//如果公式存在且已经存在的公式区域与将要定义的区域不同
				if(tempObjs[0] instanceof IArea && !((IArea)tempObjs[0]).toString().equalsIgnoreCase(area.toString())){
					showMessage.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0123")/*@res "公式定义失败！原因："*/);
					showMessage.append(tempObjs[0].toString());
					showMessage.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413010-0124")/*@res "已经定义了其他公式"*/);
					return false;
				}
			}
		}
		return true;
	}

	//这个方法与FmlDefUtil的类重复了，先临时这样做，以后要改
	private Object[] getRelatedFmlVO(FormulaModel formulaModel,CellPosition cellPos, int fmlType){
		Object[] objs ={null,null};
		switch(fmlType){
			case PUBLIC_CELL_FML:{
				IArea fmlArea = formulaModel.getRelatedFmlArea(cellPos, FormulaModel.TYPE_CELL_FML);
				// 获取公有公式
				FormulaVO publicCellFormula = fmlArea == null ? null: formulaModel.getPublicDirectFml(fmlArea);
				if(publicCellFormula != null) {
					objs[0]=fmlArea;
					objs[1]=publicCellFormula;
				}
				break;
			}
			case PRIVATE_CELL_FML:{
				IArea fmlArea = formulaModel.getRelatedFmlArea(cellPos, FormulaModel.TYPE_CELL_FML);
				// 获取私有公式
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
			default :{//默认就按公有公式取
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

	//begin-ncm-rendp-NCdp205348537-2015-4-20-专项 
	/** 
	* 此补丁修正了产品BUG:判断单元格循环引用效率优化——判断过的单元格加入缓存，避免重复判断
	*/ 
	//private boolean isLoopRef(Vector<String> areaVec, IArea defArea, IParsed dbLet, String strFormula, int type, boolean bUserDef){
	private boolean isLoopRef(Vector<String> areaVec, IArea defArea, IParsed dbLet, String strFormula, int type, boolean bUserDef, Vector<String> dealedVec){
		try {

			// 将该位置加入到vector中，用于判断循环
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

			// 没有引用单元格，则不存在循环
			if (alist == null || alist.isEmpty()) {
				removeAreaFromVector(areaVec, defArea);
				return false;
			}
			// 循环引用、自引用校验
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
						if(dealedVec.contains(cell.toString())){//该单元格已进行过判断
							removeAreaFromVector(areaVec, defArea);
							continue;
						}
						
						if(areaVec.contains(cell.toString())){//存在循环引用
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

			// 退出之后，将位置信息清理
			removeAreaFromVector(areaVec, defArea);
		} catch (Exception e){
			AppDebug.debug(e);
		} finally {
			removeAreaFromVector(areaVec, defArea);
			// 将已处理的单元格加入到vector中，以免重复判断
			addAreaIntoVector(dealedVec, defArea);
		}
		return false;
	}

	/**
	 * 获得公式的数值类型
	 *
	 * @param parsedLet
	 *            公式解析结果
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
	 * 根据公式数值类型,更改单元格类型。如果是指标单元，则不改
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
	 * 增加动态区指标公式
	 *
	 * @param showMessage
	 * @param strDynPK
	 * @param cell
	 * @param strFormula
	 *            公式内容。如果bUserDef=true 则表示用户显示方式的内容。否则为数据库形式的公式内容
	 * @param bUserDef
	 * @param dbLet
	 *            数据库形式的公式解析对象
	 * @param bPublic
	 *            此参数只有在 bCell=true时有意义。含义为添加的是否为公有公式。 bPublic=true
	 *            且当前为创建单位时，才能加入公有公式。 bPublic=false 个性化公式
	 * @param bOnlyAddFormList
	 *            true 表示只增加到公式链中，并不修改数据模型。false表示即增加到公式链中，又修改数据模型
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
		// //指定的单元上必须有指标信息
		// // throw new ParseException("miufo1000977"); //"动态区域中只能定义指标公式")
		// return false;
		// }

		String strDbFormula = null;
		try
		{
			// add by ljhua 2005-3-18 无法检查外部函数，所以分析函数时关闭检查开关
			turnOffExtFuncCheck();

			// if (strFormula == null) {
			// strDbFormula = dbLet.toString();
			// }
			// 动态区域需要设置环境变量
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
				// 删除原有公式
				// @edit by wangyga at 2008-12-31,下午04:42:24
				// 动态区同时定义公有公式和个性化公式时，此处的清除会导致只定义上个性化公式。
				// clearDynFormula(showMessage, strDynPK, area, bCell);
			}

			// 加入新公式，不再需要将公式加入到公式链中
			/*
			 * if (getDynFormList(strDynPK, bCell) == null) { // 建立公式链
			 * setupDynFormulaList(strDynPK, bCell); } Vector
			 * vecReferringDynArea = MeasFormulaUtil.getReferringMeasArea(
			 * dbLet, MeasFormulaUtil.CHECK_EXP_MSELECT_COND, getExecutorEnv());
			 * boolean bAdd = getDynFormList(strDynPK, bCell).addFormula(area,
			 * vecReferringDynArea);
			 */

			// 清除动态区原有公式
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
				//修改内存溢出
//				FormulaVO fVO = new FormulaVO(getReportPK(), strDbFormula,
//						dbLet);
				FormulaVO fVO = new FormulaVO(getReportPK(), strDbFormula,dbLet);
				fVO.setUserDefContent(dbLet.toUserDefString(getCalcEnv()));
				getFormulaModel().setDynFmlVO(strDynPK, area, fVO, type,
						bPublic);

				// 调整语义模型引用关系
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
	 * 构建动态区单元公式计算链
	 *
	 * @create by liuchuna at 2010-5-6,下午05:00:42
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

		// 构建有层次结构的公式链
		setupGradeFmlList(hashDynFmls, fmlList);
	}

	/**
	 * 构建动态区汇总公式计算链
	 *
	 * @create by liuchuna at 2010-5-6,下午05:01:13
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

		// 构建有层次结构的公式链
		setupGradeFmlList(hashDynFmls, fmlList);
	}

	/**
	 * 构建指定动态区的公式链
	 *
	 * @param strDynPK
	 *            动态区PK
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
	 * 当插行列、删行列、单元移动、交换行等操作时，刷新动态区公式链，并修改相对区域公式内容。
	 *
	 * @param startPos
	 *            开始的行或列.以0为开始位置，即表示第一行、列.
	 * @param num
	 *            行列数
	 * @param operation
	 *            操作类型（0 行删除， 1 列删除； 2行插入， 3 列插入；4 行交换， 5 列交换 ,
	 *            6向右插入单元（startPos为插入位置的行号，num为插入位置的列号） ， 7
	 *            向下插入单元（startPos为插入位置的行号，num为插入位置的列号）， 8
	 *            向左删除单元（startPos为插入位置的行号，num为插入位置的列号）， 9
	 *            向上删除单元（startPos为插入位置的行号，num为插入位置的列号） ） operation类型定义参见本类静态变量。
	 * @param strDynPK
	 *            动态区PK
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

		// 屏蔽外部公式语法检查
		turnOffExtFuncCheck();

		Hashtable hashDynFmls = getFormulaModel().getDynFmls(strDynPK, type);
		if (hashDynFmls == null || hashDynFmls.size() == 0)
			return;

		getExecutorEnv().setDynAreaInfo(strDynPK, null);

		// 对所有公式，编例所有公式，建立公式链表
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
					// //对于包含外部业务函数的公式,只有当同注册成功的数据源相同时,才有效
					// if(isValidFormula(fVO)==false)
					// isCorrect=false;
				}

				// 检查是否有相对区域表示, modify chxw 增加动态区相对公式支持
				if (fVO.isRelative())
				{
					isCorrect = AreaFormulaUtil.updateRelativeArea(parsedLet,
							startPos, num, operation, iMaxRow, iMaxCol);
					// 得到变化后的正确公式的内容
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

				// 增加到公式连
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
		// 刷新页面模型
		// getFormulaModel().syncCellsModel();

	}

	/**
	 * 构建主表单元公式计算链
	 *
	 * @create by liuchuna at 2010-5-6,下午05:02:41
	 *
	 */
	private void setupMainCellFmlList() {
		// 获得所有主表单元公式
		Map<IArea, FormulaVO> mapMainFormula = getFormulaModel().getMainFmls(FormulaModel.TYPE_CELL_FML);

		// 初始化公式链，公式链置为空集
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
	 * @create by liuchuna at 2011-7-8,下午02:55:20
	 *
	 * @param mapMainFormula
	 *            构建公式链的公式集合
	 * @param innerProcess
	 *            构建公式链的过程中，判断依赖、循环是否在mapMainFormula内部处理
	 *            true，则在mapMainFormula内部处理依赖、循环
	 *            false，则在整个公式模型中处理依赖、循环
	 */
	private void constructLevelFmlList(Vector<Vector<IArea>> levelFmlList, Map<IArea, FormulaVO> mapMainFormula, boolean innerProcess,int fmlType) {
		// 设置所有主表单元公式的层次级别
		initMainFormulaLevel(mapMainFormula, fmlType, innerProcess);

		// 构建有层次结构的公式链
		setupGradeFmlList(mapMainFormula, levelFmlList);
	}

	/**
	 * 构建主表汇总公式计算链
	 *
	 * @create by liuchuna at 2010-5-6,下午05:02:58
	 *
	 */
	private void setupMainTotleFmlList() {

		// 获得所有主表汇总公式
		Map<IArea, FormulaVO> mapMainFormula = getFormulaModel().getMainFmls(FormulaModel.TYPE_TOTAL_FML);

		// 初始化公式链，公式链置为空集
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
//		// 构建有层次结构的公式链
//		setupGradeFmlList(mapMainFormula, mainTotleFmlList);
	}

	/**
	 * @create by liuchuna at 2010-5-6,下午05:05:27
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
	 * 主表单元公式层次设置
	 *
	 * @create by liuchuna at 2010-5-5,上午11:04:45
	 *
	 */
	private void initMainFormulaLevel(Map<IArea, FormulaVO> mapMainFormula, int type, boolean innerProcess)
	{
		if (mapMainFormula == null || mapMainFormula.size() == 0)
			return;

		// 初始化所有公式在公式链中的层次
		initformulaListLevel(mapMainFormula, false, type, innerProcess);
	}

	/**
	 * 遍历所有公式，初始化每个公式的层次
	 *
	 * @create by liuchuna at 2010-5-5,上午11:19:48
	 *
	 * @param mapFormula
	 */
	private void initformulaListLevel(Map<IArea, FormulaVO> mapFormula,
			boolean isDynArea, int type, boolean innerProcess)
	{
		if (mapFormula == null || mapFormula.size() == 0)
			return;

		// 屏蔽外部公式语法检查
		turnOffExtFuncCheck();

		// 对所有公式，遍历所有公式，建立公式链表
		Iterator<Map.Entry<IArea, FormulaVO>> iter = mapFormula.entrySet().iterator();
		// 用于校验循环公式，存储格式态的位置
		Vector<String> areaVec = new Vector<String>();
		IArea areaKey = null;
		FormulaVO fVO = null;

		if(!isFormateState()) {
			while (iter.hasNext()) {
				Map.Entry<IArea, FormulaVO> entry = iter.next();
				areaKey = entry.getKey();
				fVO = entry.getValue();

				try {
					// 如果公式解析为空，则解析公式,并设置公式解析结果
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
	 * 取得某个公式的级次
	 *
	 * @create by liuchuna at 2010-5-6,下午05:07:46
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
			// 将该位置加入到vector中，用于判断循环
			addAreaIntoVector(areaVec, areaKey);

			IParsed parsedLet = fVO.getLet();
			if (parsedLet == null) {
				IArea realArea = DynAreaUtil.getRealArea(areaKey, getDataModel());
				if (isInDynArea(realArea, getDataModel())) {
					// liuchun 20100118 修改，如果是构建固定区公式链，且引用动态区，则停止计算层次
					if(isDynArea) {
						parsedLet = getFormulaProxy().parseExpr(content);
					} else {
						removeAreaFromVector(areaVec, areaKey);
						return level;
					}
				} else {
					parsedLet = getFormulaProxy().parseFormula(areaKey, content);
				}
				// 解析正确设置为正确公式
				fVO.setErrorFml(false);
			}
			//修改内存溢出
			if(isFormateState() && parsedLet!=null){
				fVO.setUserDefContent(parsedLet.toUserDefString(getCalcEnv()));
			}
			if (isCorrect == true) {
				level = getFormulaLevel(parsedLet, fVO, mapFormula, isDynArea, areaVec, type, innerProcess);
				fVO.setFmlLevel(level);
			}

			// 退出之后，将位置信息清理
			removeAreaFromVector(areaVec, areaKey);
		} catch (Exception e) {
			// liuweiu 2015-05-27 打开报表是错误公式校验
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
	 * 判断指定区域是否是动态区
	 *
	 * @create by liuchuna at 2010-5-7,上午09:58:30
	 *
	 * @param area
	 * @param dataModel
	 * @return
	 */
	private boolean isInDynArea(IArea area, CellsModel dataModel) {
		// liuchun 20110429 不能取所有的扩展区，有的扩展区是主表扩展区
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
		// 循环引用、自引用校验
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
					// 取得该位置对应的公式
					IArea realArea = DynAreaUtil.getRealArea(cell, getDataModel());

					if(!isDynArea) {
						// 构建主表的公式链
						if(isInDynArea(realArea, getDataModel())){
							// 依赖动态区的区域，则返回
							continue;
						}
					}

					FormulaVO formula = getFormulaByArea(mapFormula, (CellPosition) realArea, type, innerProcess);
					if(areaVec.contains(cell.toString())){//存在循环引用
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
	 * 根据位置返回该单元的公式
	 *
	 * 对于定义在区域上的公式，用该区域中的一个单元调用该方法也可以返回该区域的公式
	 *
	 * @create by liuchuna at 2010-6-24,上午11:25:49
	 *
	 * @param selPos
	 * @return
	 */
	private FormulaVO getFormulaByArea(Map<IArea, FormulaVO> mapFormula, CellPosition selPos, int type, boolean innerProcess)
	{
		// 指定的单元格返回CellPosition类型，如果选择的是组合单元，则返回正确的选择区域 而
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
	 * 清除指定区域内公式
	 *
	 * @param showMessage
	 * @param area
	 *            待清除公式的区域位置
	 * @param strDynPK
	 *            strDynPK=null表示清除主表公式。其它情况表示动态区pk
	 */
	private void clearDynFormula(StringBuffer showMessage, String strDynPK,
			IArea area, int type, int calcFmlType) {
		if (area == null)
			return;

		// 获得删除公式区域集合
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
				// 清除对语义模型的引用
				clearRefSmtDef(fvo.getId(),parsedLet);
			}


			// 更新公式模型
			Vector vecDelPerson = null;
			if (strDynPK == null) {
				vecDelPerson = getFormulaModel().removeMainRelatedFmlByType(
						area, type, calcFmlType);
			} else
				vecDelPerson = getFormulaModel().removeDynRelatedFmlByType(
						strDynPK, area, type, calcFmlType);

			if (type == FormulaModel.TYPE_CELL_FML) {
				// 将删除个性化公式对应的公有公式加入到公式链中
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
	 * 更新指定区域内公式
	 *
	 * @param strDynPK
	 *            strDynPK=null表示清除主表公式。其它情况表示动态区pk
	 * @param oldArea
	 *            待更新公式的区域位置
	 * @param newArea
	 *            公式新的区域位置
	 * @param fmlVO
	 *            公式
	 * @param bCell
	 * @param bPublic
	 */
	private void updateDynFormula(String strDynPK, IArea oldArea,
			IArea newArea, FormulaVO fmlVO, int type, boolean bPublic)
	{
		if (oldArea == null || newArea == null)
			return;

		// 获得更新公式区域集合
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

			// 在公式链中删除旧的公式
			// if (formulaList != null)
			// {
			// for (int i = 0, size = areaFormulas.length; i < size; i++)
			// {
			// if (areaFormulas[i] == null)
			// continue;
			// formulaList.delFormula(areaFormulas[i]);
			// }
			// }

			// 在公式链中加入新的公式
			// IParsed dbLet = fmlVO.getLet();
			// Vector alist = AreaFormulaUtil.getAreaList(dbLet);
			// formulaList.addFormula(newArea, alist);

			// 更新公式模型
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
	 * 删除单元公式的过程
	 *
	 * @param area
	 *            待删除公式的区域位置
	 * @param bCell
	 *            是否单元公式，true单元公式，false汇总公式
	 * @param fmlType
	 */
	public void clearFormula(IArea area, int type, int calcFmlType) {
		StringBuffer showErrMessage = new StringBuffer();

		// 1.清除主表指定区域内公式
		clearDynFormula(showErrMessage, null, area, type, calcFmlType);

		// 获得区域覆盖的动态区
		ExtendAreaCell[] dynCells = getDynAreaModel().getDynAreaCellByArea(area);

		// 2.清除动态区内指定区域内公式
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
	 * 更新公式对象定义的区域位置
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
			// 1.更新主表指定区域的公式
			updateDynFormula(null, oldArea, newArea, fmlVO, type, bPublic);

			// 获得区域覆盖的动态区
			ExtendAreaCell[] dynCells = getDynAreaModel().getDynAreaCellByArea(
					oldArea);

			// 2.更新动态区内指定区域的公式
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
	 * 删除表内所有公式
	 *
	 * @param bCell
	 *            是否单元公式，true表示单元公式，false表示汇总公式
	 */
	public void clearAllFormula(int type) {
		// 删除主表所有公式
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
	 * 当插行列、删行列、单元移动、交换行等操作时，公式链的重新建立，并修改相对区域公式内容。
	 *
	 * @param startPos
	 *            开始的行或列.以0为开始位置，即表示第一行、列.
	 * @param num
	 *            行列数
	 * @param operation
	 *            操作类型（0 行删除， 1 列删除； 2行插入， 3 列插入；4 行交换， 5 列交换 ,
	 *            6向右插入单元（startPos为插入位置的行号，num为插入位置的列号） ， 7
	 *            向下插入单元（startPos为插入位置的行号，num为插入位置的列号）， 8
	 *            向左删除单元（startPos为插入位置的行号，num为插入位置的列号）， 9
	 *            向上删除单元（startPos为插入位置的行号，num为插入位置的列号） ） operation类型定义参见本类静态变量。
	 * @param strDynPKs
	 *            行列变动后，动态区首单元位置变化的动态区pk集合
	 */
	public void updateFormulas(int startPos, int num, int operation,
			String[] strDynPKs, int type)
	{
		// startPos是从0开始的，修改为startPos < 0
		if (startPos < 0 || num <= 0
				|| operation < AreaFormulaUtil.OPT_DEL_ROW
				|| operation > AreaFormulaUtil.OPT_UP_REMOVE)
		{
			return;
		}

		// 1.处理主表
		try
		{
			// 从后台得到公式列表，以便建立修改链表，与后台数据同步
			// 注意，这时得到的公式的位置变化已完成（由ReportTableBO.posChange完成），但没有作因相对区域带来的公式内容的修改
			Map mapMainFormula = getFormulaModel().getMainFmls(type);
			// if (getMainFormList(bCell) == null)
			// {
			// m_oMainFormulaCalcList = new UfoFormulaList(getCellModel());
			//
			// } else
			// {
			// getMainFormList(bCell).clear();
			// }

			// 屏蔽外部公式语法检查
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

						// 检查是否有相对区域表示
						if (fVO.isRelative())
						{
							bCorrect = AreaFormulaUtil.updateRelativeArea(
									parsedFunc, startPos, num, operation,
									iMaxRow, iMaxCol);
							// 得到变化后的正确公式的内容
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
							// 插入行后MROWCOUNT函数的显示需要修改为动态区最新区域，考虑到MROWCOUNT特殊使用场景，特殊处理
							fVO.setUserDefContent(null);
						}
						//修改内存溢出
//						fVO.setLet(parsedFunc);

						// 增加到公式连
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

		// 2.处理动态区
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
	 * 执行表内单元公式的计算
	 * 计算顺序： 1、计算主表公式； 2、计算所有动态区公式； 3、重算主表内包含的直接引用或简介引用动态区指标函数的公式
	 *
	 * @create by liuchuna at 2010-12-2,下午08:48:17
	 *
	 * @param bOnlyAreaCalc
	 *            true表示区域计算(即不计算查询和外部业务函数)，false表示所有内容的计算
	 * @return
	 */
	public CellsModel calcAllFormula(boolean bOnlyAreaCalc) {
		try {
			// 记录计算环境中参数：是否计算外部函数
			String strOldValue = (String) getExecutorEnv().getExEnv(CommonExprCalcEnv.EX_CALCEXFUNC);

			// 设置是否计算外部函数参数值
			if (bOnlyAreaCalc) {
				getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_CALCEXFUNC, CommonExprCalcEnv.EX_VALUE_OFF);
			} else {
				getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_CALCEXFUNC, CommonExprCalcEnv.EX_VALUE_ON);
				// 如果是非区域计算，进行报表查询的运算
				calAllReportQuerys();
			}

			// 构建公式链
			if (getMainCellFormList() == null) {
				setupMainCellFmlList();
			}

			// 根据公式链，构建公式计算列表，列表中存储公式所属单元
			Vector<Vector<IArea>> areaGradeVec = getMainCellFormList();
			Vector<IArea> flist = new Vector<IArea>();
			for (Vector<IArea> vec : areaGradeVec) {
				if (vec != null) {
					flist.addAll(vec);
				}
			}

			// 引用动态区域内指标的主表公式对应的区域集合(包括直接引用及间接引用的主表公式)
			Vector<IArea> vecRefDynArea = new Vector<IArea>();
			// 未引用动态区指标的主表公式的区域集合(包括直接引用及间接引用的主表公式)
			Vector<IArea> vecNonRefDynArea = new Vector<IArea>();
			// 直接引用或间接引用动态区的主表公式
			getRefDynAreaFml(flist, vecRefDynArea, vecNonRefDynArea, FormulaModel.TYPE_CELL_FML);
			// edit by congdy  2013.9.16 HR数据方案计算
			if(UfoeLicenseManager.isUFHRModuleValid() && !bOnlyAreaCalc) {// 如果是区域计算，不执行
				calcMainHRScheme();
			}

			// 先计算固定区域的报表项目映射取值
			if(UfoeLicenseManager.isUFDSModuleValid() && !bOnlyAreaCalc) {// 如果是区域计算，不执行报表项目取数
				calcMainRepItem();
			}

			// 计算主表公式(不引用动态区的主表公式先计算)
			calcMainFormulas(vecNonRefDynArea, FormulaModel.TYPE_CELL_FML);

			// 计算所有动态区公式
			calAllDynAreas(true, FormulaModel.TYPE_CELL_FML, bOnlyAreaCalc);

			// 重新计算主表内包含直接引用或间接引用动态区域指标函数的公式
			recalcMainFormula(vecRefDynArea, FormulaModel.TYPE_CELL_FML);

			// 恢复计算环境中参数：是否计算外部函数
			getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_CALCEXFUNC, strOldValue);

			//清除数据模型中的存储单元缓存
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
	 * 计算固定区域的HR数据方案数据
	 */
	private void calcMainHRScheme() {
		try {
			getContextVO().setAttribute(IUfoContextKey.HR_CALC_AREA_PK, ITableData.MAINPEPORT);
			invokeHRCalc();
		} catch (Exception e) {
			// 只输出异常信息，为不影响后续的计算
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
	 * 计算固定区域的报表项目数据
	 */
	private void calcMainRepItem()  {
		try{
			getContextVO().setAttribute(IUfoContextKey.DI_CALC_AREA_PK, ITableData.MAINPEPORT);
			invokeRepItemCalc();
		} catch(Exception e){
			// 只输出异常信息，为不影响后续的计算
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

		//tianchuan 20140928 修改港华问题
		Set<IArea> nonRefSet=new HashSet<IArea>();
		Set<IArea> validSet=new HashSet<IArea>();
		
		Map mapMainFormula = getFormulaModel().getMainFmls(type); // 所有主表公式
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
			
			//tianchuan 2013.5.17 修改 正确的得到引用动态区区域的函数
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
					if (dynAreaMocel.isInDynArea(tempArea.getStart()) || validSet.contains(tempArea)){	//考虑嵌套的情况
						if(!validSet.contains(vecFormulaList.get(i))) {
							validSet.add((IArea)vecFormulaList.get(i));
//							validSet.addElement(vecFormulaList.get(i));
							vecNonRefFormulaArea.remove(vecFormulaList.get(i));
						}
					}
				}
			}
			//tianchuan 2013.5.17 结束
			
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
//			{// 如果主表公式是区域公式并引用动态区，则加入公式链
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
		 * 检查非直接引用公式是否间接引用直接引用公式
		 */
		for (; toCheckFormulaAreaList(vecNonRefFormulaArea, validSet, type);)
		{
		}
		//tianchuan 20140928 修改港华问题
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
			// 获得主表所有公式
			Map mapMainFormula = getFormulaModel().getMainFmls(type);

			calcFormulas(mapMainFormula, flist, type);
		} finally {
			getExecutorEnv().removeExEnv(UfoCalcEnv.KEY_DYN_KEYDATAGROUP_INDEX);
		}
	}

	/**
	 * 计算主表公式
	 *
	 * @create by liuchuna at 2010-12-2,下午08:55:16
	 *
	 * @param flist
	 *            公式所属单元列表，依据此列表中顺序进行计算
	 * @param bCell
	 *            是否单元公式：true=单元公式计算，false=汇总公式计算
	 */
	private void calcFormulas(Map mapMainFormula, Vector flist, int type) {

		if (flist != null && flist.size() > 0) {

			// 计算主表公式，清除动态区信息
			getExecutorEnv().setDynAreaInfo(null, null);

			// 首先进行分组，将与外部业务函数无关的公式首先进行计算按
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
						AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0477")/*@res "预批量计算错误"*/, e);
					}
					// 计算
					FormulaVO oFormulaTemp = null;
					String calcFmlStr = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0908");/*@res "计算公式[area="*/
					String fmlContStr = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0909");/*@res "],公式内容["*/
					for (int j = 0; j < objLets.length; j++) { // 对所有公式进行计算
						UfoCmdLet formParsed = objLets[j]; // 得到公式内容
						if (formParsed != null) {
							try {
								// @edit by wangyga at 2009-1-13,下午04:26:54
								// 区域计算不计算数据集函数
								if (!isCalcDataSetFunc(formParsed)) {
									continue;
								}

								format = getDataModel().getRealFormat(areaTemps[j].getStart());
								if (format != null && format.getCellType() == TableConstant.CELLTYPE_BIGNUMBER) {
									// 大数值类型的,需要特殊处理
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
								strBufInfoErr.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0908")/*@res "计算公式[area="*/);
								strBufInfoErr.append(areaTemps[j] == null ? ""
										: areaTemps[j].toString());
								strBufInfoErr.append("]");
								strBufInfoErr.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0910")/*@res "计算公式 ["*/);
								strBufInfoErr.append(strFml);
								strBufInfoErr.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0911")/*@res "] 错误"*/);
								AppDebug.error(strBufInfoErr.toString());// error级别输出计算错误信息
								nc.bs.logging.Logger.debug(e);// debug级别输出堆栈信息
							} catch (Throwable e) {
								AppDebug.debug(e.getMessage(), e);// Throwable error级别输出堆栈信息
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
	 * 根据外部业务函数进行公式计算列表的分割
	 *
	 * @param flist
	 *            Vector 不为空
	 * @return 可以先计算的公式区域
	 */
	private ArrayList[] separateMainFormulas(Vector<IArea> flist)
	{
		// 当前业务函数驱动存在，并且外部业务函数计算标记为on
		Object extFuncDriver = getExecutorEnv().loadFuncListInst()
				.getExtFuncDriver();
		ArrayList[] separated = new ArrayList[2];
		if (extFuncDriver != null
				&& (getExecutorEnv().getExEnv(CommonExprCalcEnv.EX_CALCEXFUNC) == null || getExecutorEnv()
						.getExEnv(CommonExprCalcEnv.EX_CALCEXFUNC).equals(
								CommonExprCalcEnv.EX_VALUE_ON)))
		{

			/**
			 * 获得主表所有公式 使用FormulaModel获得的公式信息，对应的公式区域都是格式态区域
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
					// 找到第一个使用外部业务函数的
					if (CmdProxy.hasExtFunc(fVO.getLet(), strDriverName))
					{
						bFind = true;
						// modify by ljhua 2005-7-5 解决当第一个业务函数与区域无关时，放入第一列表中
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
					// 找到没有引用任何区域的公式
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
	 * 重新计算主表内包含动态区域指标函数的公式计算
	 *
	 * @param vecFormulaList
	 *            主表公式链
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
//		Map mapMainFormula = getFormulaModel().getMainFmls(type); // 所有主表公式
//		Vector vecValidArea = new Vector(); // 引用动态区域内指标的主表公式对应的区域集合(包括直接引用及间接引用的主表公式)
//		Vector<IArea> vecNonRefFormulaArea = new Vector<IArea>(); // 未引用动态区指标的主表公式的区域集合(包括直接引用及间接引用的主表公式)
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
//			{// 如果主表公式是区域公式并引用动态区，则加入公式链
//				if (isMainFormulaRefDynArea(formulaLet))
//				{
//					vecValidArea.addElement(vecFormulaList.get(i));
//					vecNonRefFormulaArea.remove(vecFormulaList.get(i));
//				}
//			}
//		}
//
//		/**
//		 * 检查非直接引用公式是否间接引用直接引用公式
//		 */
//		for (; toCheckFormulaAreaList(vecNonRefFormulaArea, vecValidArea, type);)
//		{
//		}
//
//		// 重新计算主表公式
//		calcMainFormulas(vecValidArea, type);
//
//	}

	private void recalcMainFormula(Vector vecValidArea, int type) {
		// 重新计算主表公式
		calcMainFormulas(vecValidArea, type);

	}

	/**
	 * 检查主表区域公式是否引用动态区
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
	 * 返回非指标函数的Ufo函数区域参数
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
	 * 返回公式表达式中需要显示的细节表达式
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
	 * 返回公式表达式中需要显示的细节表达式
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
					case UfoEElement.OPR: // 操作数
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
					case UfoEElement.OP: // 运算符
						short op = ((Short) e.getObj()).shortValue();
						if ((op == IUfoTokenConsts.TKN_U_MINUS || op == IUfoTokenConsts.TKN_NOT)
								&& stk.size() >= 1)
						{ // 一元运算符
							elements = (Vector) stk.pop();
							elements.add(e);

						} else if (stk.size() >= 2)
						{ // 二元运算符
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
	 * 返回公式表达式中需要显示的细节表达式
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
					case UfoEElement.OPR: // 操作数
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
					case UfoEElement.OP: // 运算符
						short op = ((Short) e.getObj()).shortValue();
						if ((op == IUfoTokenConsts.TKN_U_MINUS || op == IUfoTokenConsts.TKN_NOT)
								&& stk.size() >= 1)
						{ // 一元运算符
							elements = (Vector) stk.pop();
							elements.add(e);

						} else if (stk.size() >= 2)
						{ // 二元运算符
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
	 * 检查非直接引用公式是否间接引用直接引用公式
	 *
	 * <pre>
	 *   	 对于其他类型函数(如UfoFunc)，未直接引用动态区指标，但可能引用上面列表的主表公式;
	 *     因此类函数也需要在重算主表公式时计算，需要递归实现。
	 * </pre>
	 *
	 * @param vecNonRefFormulaArea
	 *            非直接引用公式列表
	 * @param vecRefFormulaArea
	 *            直接引用公式列表
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
	 * 检查指定位置的公式中是否引用指定列表中的单元区域
	 *
	 * @param formulaArea
	 *            待检查公式的单元区域
	 * @param vecRefFormulaArea
	 *            单元区域列表
	 * @param bCell
	 *            单元公式/汇总公式
	 * @return
	 */
	private boolean checkFormulaRefAreaList(IArea formulaArea,
			Set<IArea> validSet, int type)
	{
		IParsed formulaParsed = null;
		Map mapMainFormula = getFormulaModel().getMainFmls(type); // 所有主表公式
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
	 * 检查表达式参数中是否引用指定列表中的区域
	 *
	 * @param exprParamRefArea
	 *            表达式参数引用的单元区域
	 * @param vecFormulaArea
	 *            区域列表
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
	 * 计算一个报表查询，从查询引擎里获得结果数据集并转换为报表数据。
	 *
	 * 创建日期：(2003-9-10 19:36:28)
	 *
	 * @author：刘良萍
	 * @param repQueryVO
	 *            com.ufsoft.iuforeport.reporttool.query.ReportQueryVO
	 * @param bClear
	 *            boolean - 是否清楚动态区域里的旧数据
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
	 * 进行所有报表查询的计算。
	 *
	 * 创建日期：(2003-9-10 19:28:26)
	 *
	 * @author：刘良萍
	 */
	private void calAllReportQuerys()
	{
		FormulaPrintUtil.printMsg("enter ufotable.calAllReportQuerys");

		// // 循环对表页里的所有报表查询进行计算
		// ReportBusinessQuery reportBussQuery = ReportBusinessQuery
		// .getInstance(getCellModel());
		//
		// if (reportBussQuery != null) {
		// Vector vecRepQuerys = reportBussQuery.getAllReportQuery();
		// int iLen = vecRepQuerys != null ? vecRepQuerys.size() : 0;
		// // 查询计算过的计算动态区域PK的Hash
		// Hashtable hashDynAreaPks = new Hashtable();
		// for (int i = 0; i < iLen; i++) {
		// ReportQueryVO rqVO = (ReportQueryVO) vecRepQuerys.get(i);
		// String strDynAreaPK = rqVO.getDynAreaPK();
		// // 在一次报表计算过程中，对于是报表查询第一次计算到的动态区域，
		// // 只有行号关键字的将清除动态区域的旧数据
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
		// FormulaPrintUtil.printMsg("开始单个查询运算,查询名称= " + strQueryName);
		// calReportQuery(rqVO, bClear);
		// FormulaPrintUtil.printMsg("结束单个查询运算,查询名称= " + strQueryName);
		// }
		// }
	}

	/**
	 * 计算所有动态区域的公式
	 *
	 * @create by liuchuna at 2010-12-1,下午02:42:45
	 *
	 * @param bCalMeasure
	 * @param bCell
	 *            是否计算单元公式
	 */
	private void calAllDynAreas(boolean bCalMeasure, int type, boolean bOnlyAreaCalc){

		// 取得报表中所有的动态区
		ExtendAreaCell[] dynAreas = getDynAreaModel().getDynAreaCells();
		if(dynAreas != null && dynAreas.length > 0){
			for (int i = 0, len = dynAreas.length; i < len; i++) {
				if (dynAreas[i] != null && bCalMeasure){
					try {
						// 批处理预计算(动态区关键字)
						preCalDynAreaFormula(dynAreas[i], type, getContextVO());
						
						// 计算动态区的HR数据方案数据
						if(UfoeLicenseManager.isUFHRModuleValid() && !bOnlyAreaCalc && type == FormulaModel.TYPE_CELL_FML) {
							// 如果是区域计算，不执行，而且必须是单元计算公式
							calcDynHRScheme(dynAreas[i]);
						}

						// 计算动态区的报表项目
						if(UfoeLicenseManager.isUFDSModuleValid() && !bOnlyAreaCalc && type == FormulaModel.TYPE_CELL_FML) {
							// 如果是区域计算，不执行报表项目计算，而且必须是单元计算公式
							calcDynRepItem(dynAreas[i]);
						}

						// 计算动态区公式
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
		// 清除动态区信息
		getExecutorEnv().setDynAreaInfo(null, null);
	}

	private void calcDynHRScheme(ExtendAreaCell dynArea) {
		try{
			// 动态区pk
			getContextVO().setAttribute(IUfoContextKey.HR_CALC_AREA_PK, dynArea.getExAreaPK());
			
			// 动态区关键字
			KeyDataGroup[] objKeydatas = getAllDynKeyDataGroups(dynArea.getExAreaPK());
			getContextVO().setAttribute(IUfoContextKey.HR_CALC_DYN_KEYS, objKeydatas);
			
			// 将动态区域各行关键字值存储在计算环境中
			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			// 设置数据集函数动态区参数
			Hashtable<Integer, Hashtable<String, Object>> mapField2Values = getDynAreaField2KeyValues(dynArea, FormulaModel.TYPE_CELL_FML);
			this.getExecutorEnv().setDynField2Values(mapField2Values);
			
			// 设置计算环境和Context
			setHRCalcExecutorFmlExt();
			setHRCalcExecutorCtx();

			// 执行计算
			invokeHRCalc();
		} catch(Exception e) {
			// 只打印异常信息，为了不影响后续公式计算
			AppDebug.debug(e);
		} finally {
			getContextVO().removeAttribute(IUfoContextKey.HR_CALC_AREA_PK);
			getContextVO().removeAttribute(IUfoContextKey.HR_CALC_DYN_KEYS);
		}

	}

	private void calcDynRepItem(ExtendAreaCell dynArea) {
		try {
			// 动态区pk
			getContextVO().setAttribute(IUfoContextKey.DI_CALC_AREA_PK, dynArea.getExAreaPK());
			// 动态区关键字
			KeyDataGroup[] objKeydatas = getAllDynKeyDataGroups(dynArea.getExAreaPK());

//			if(objKeydatas == null || objKeydatas.length == 0) {
//				// 动态区关键字为空时不做计算
//				return;
//			}
			getContextVO().setAttribute(IUfoContextKey.DI_CALC_DYN_KEYS, objKeydatas);

			// 将动态区域各行关键字值存储在计算环境中
			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			// 设置数据集函数动态区参数
			Hashtable<Integer, Hashtable<String, Object>> mapField2Values = getDynAreaField2KeyValues(dynArea, FormulaModel.TYPE_CELL_FML);
			this.getExecutorEnv().setDynField2Values(mapField2Values);

			// 设置计算环境和Context
			setDICalcExecutorFmlExt();
			setDICalcExecutorCtx();

			// 执行计算
			invokeRepItemCalc();
		} catch(Exception e) {
			// 只打印异常信息，为了不影响后续公式计算
			AppDebug.debug(e);
		} finally {
			getContextVO().removeAttribute(IUfoContextKey.DI_CALC_AREA_PK);
			getContextVO().removeAttribute(IUfoContextKey.DI_CALC_DYN_KEYS);
		}
	}

	/**
	 * 获得指定区域对应的指标pk集合
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
	 * 对动态区公式执行预计算。 新的业务要求动态区关键字上支持定义数据集公式，并根据数据集扩展动态区，
	 * 非关键字上定义的公式包括数据集公式同原来业务处理保持一致，取当前动态区关键字的值进行计算
	 *
	 * @create by liuchuna at 2010-12-1,下午02:20:50
	 *
	 * @param dynAreaCell
	 *            待计算的动态区
	 * @param bCell
	 *            是否汇总公式
	 * @param context
	 */
	@SuppressWarnings("null")
	private void preCalDynAreaFormula(ExtendAreaCell dynAreaCell, int type,IContext context)
	{
		// 得到动态区关键字定义：关键字位置，关键字
		Object[][] objKeyPos = getKeyPosVOsByDynArea(dynAreaCell);
		if (objKeyPos == null || objKeyPos.length == 0) {
			return;
		}

		// 获取动态区pk值
		String strDynPK = dynAreaCell.getBaseInfoSet().getExAreaPK();

		// 构建动态区公式链
		Vector<Vector<IArea>> areaGradeVec;
		if (type == FormulaModel.TYPE_CELL_FML) {// 单元公式
			if (getDynCellFormList(strDynPK) == null) {
				setupDynCellFmlList(strDynPK);
			}
			areaGradeVec = getDynCellFormList(strDynPK);
		} else {// 汇总公式
			if (getDynTotleFmList(strDynPK) == null) {
				setupDynTotleFmlList(strDynPK);
			}
			areaGradeVec = getDynTotleFmList(strDynPK);
		}
		// 从公式链中取得动态区的全部公式
		Vector<IArea> vecDynFmlAreas = new Vector<IArea>();
		for (Vector<IArea> vec : areaGradeVec) {
			if (vec != null) {
				vecDynFmlAreas.addAll(vec);
			}
		}

		// 公式链转换为数组
		IArea[] areas = new IArea[vecDynFmlAreas.size()];
		vecDynFmlAreas.toArray(areas);

		if (areas == null && areas.length == 0) {
			return;
		}

		// 获取包含关键字区域的公式定义区域列表
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
		// 如果关键字区域上没有定义公式，则返回
		if (keyAreaList.size() == 0)
			return;

		Hashtable<IArea, UfoVal[]> keyAreaValue = new Hashtable<IArea, UfoVal[]>();
		IArea[] keyAreas = keyAreaList.toArray(new IArea[0]);


		// 获取该动态区所有公式
		Hashtable hashDynFmsl = getFormulaModel().getDynFmls(strDynPK, type);
//		Hashtable<IArea, UfoVal[]> keyAreaValue = new Hashtable<IArea, UfoVal[]>();
		boolean isCodeFill = true;
		//是否需要整体计算一次
		boolean isShouldCalcTotally=true;
		for (IArea area : keyAreas) {
			try {
				boolean isKeyArea = isFmlDefInKeyArea(area, ht);
				getExecutorEnv().setExEnv(UfoCalcEnv.KEY_IS_KEYAREA_FML, isKeyArea);
				// 获取公式内容
				FormulaVO formula = getDynFmlByArea(area, hashDynFmsl);
				if (formula != null) {
					try {
						IParsed formulaLet = formula.getLet();
						if (formulaLet == null) {
							formulaLet = parseExpr(formula.getFormulaContent());
							formula.setLet(formulaLet);
						}
						// 区域计算时不计算数据集函数
						if (!(isCalcDataSetFunc(formulaLet))) {
							continue;
						}

						isCodeFill = isCodeFill(formulaLet);
						
						// 取得公式依赖的区域，先计算依赖区域，然后计算本公式 TODO
						Vector vecReferringDynArea = MeasFormulaUtil.getReferringMeasArea(formulaLet,
										MeasFormulaUtil.CHECK_EXP_MSELECT_COND, getExecutorEnv());
						if (vecReferringDynArea != null
								&& vecReferringDynArea.size() > 0) {
							IUFOTableData dataChannel = (IUFOTableData) getExecutorEnv().getDataChannel();
							// 计算动态区分组数据增加步长
							// int stepRow = dynAreaCell.isRowDirection() ? dynAreaCell
							// .getOriArea().getHeigth() : 0;
							// int stepCol = dynAreaCell.isRowDirection() ? 0
							// : dynAreaCell.getOriArea().getWidth();

							@SuppressWarnings("unused")
							IArea refArea = (IArea) vecReferringDynArea.get(0);
							//tianchuan 20141112 动态区的依赖，不整体计算
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
								//通过关联区域得到值，就不需要整体计算了
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
						//tianchuan 2013.8.28 修改 在这里只计算非依赖区域，之前的处理有问题
						//tianchuan 2013.10.11 采用isShouldCalcTotally判断
						if(isShouldCalcTotally){
							// 公式计算
							UfoVal[] dimValues = formulaLet.getValue(getExecutorEnv());

							// 保存公式计算结果
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

		// 数据转化
		ArrayList<UfoArray> allValueList = transferData(keyAreas, keyAreaValue);
		UfoVal[] allKeyValues = allValueList.toArray(new UfoArray[0]);

		// 填充数据，并生成新的数据模型
		CellsModel newDataModel = DataSetFuncCalcUtil.convertDataSetToMeasureDatas(keyAreas, null,
				allKeyValues, getCellModel(), dynAreaCell,context, isCodeFill);
				
		//tianchuan 20140623  保存是否区域计算的标识
		String strOldValue = (String) getExecutorEnv().getExEnv(CommonExprCalcEnv.EX_CALCEXFUNC);
		// 设置新生成的数据模型
		this.setDataModel(newDataModel);
				
		// 重新初始化
		this.init();
		//恢复原来的区域计算标识
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
			ufoExpr = (UfoExpr) ((UfoCmdLet) parsedLet).getLetList().get(1);// 取出函数右边表达式
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

		// 设置新生成的数据模型
		this.setDataModel(dataModel);

		// 重新初始化
		this.init();
	}

	/**
	 * @create by liuchuna at 2010-12-1,下午02:41:27
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
				 * rowValueVector.toArray(new Object[0])有可能长度为0，
				 * 导致UfoArray.getInstance(array)在执行时抛出“数组长度不能为0的”UfoValueException
				 * 从而导致计算流程不能正常执行
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
	 * 返回动态区字段与关键字映射关系
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
				//在数据中心打开时，直接追踪，有可能formulaLet时UfoCmdLet类型的，导致异常，如果不是UfoExpr类型的，则重新设置formulaLet
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
	 * 返回动态区字段与关键字值得映射关系
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
					throw new MessageException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0572")/*@res "动态区关键字数据不能为空！"*/);
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
						//tianchuan 修改 63以后， 存储PK，getData一律拿PK匹配
						if(isCodeFill) {
							strValue = keyDataVO.getKeyData().getCode();
						} else {
							strValue = keyDataVO.getKeyData().getValue();
						}
					} else {
						ReportOrgVO repOrg = OrgUtil.getRepOrgVoByCode(keyDataVO.getValue());
						if(repOrg != null) {
							//tianchuan 修改 63以后， 存储PK，getData一律拿PK匹配
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
					//tianchuan ++ 63以后， 存储PK，getData一律拿PK匹配
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
	 * 根据关键字定义位置得到该区域定义的公式
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
	 * 计算动态区公式。
	 * <p>
	 * 旧的动态区公式计算方法(calDynAreaMeasureFm)，只计算动态区指标公式；根据新的业务
	 * 需求，动态区必须支持定义除指标公式之外的其他常见的公式类型及计算，因此必须修改为以动 态区公式区域分组计算。
	 *
	 * @param dynArea
	 *            待计算的动态区
	 * @param bCell
	 *            是否汇总公式
	 */
	private void calDynAreaFormula(ExtendAreaCell dynArea, int type)
	{
		long lstart = System.currentTimeMillis();

		// 建立动态区公式链
		String strDynPK = dynArea.getBaseInfoSet().getExAreaPK();
		Vector<Vector<IArea>> areaGradeVec;
		if (type == FormulaModel.TYPE_CELL_FML)
		{// 单元公式
			if (getDynCellFormList(strDynPK) == null)
			{
				setupDynCellFmlList(strDynPK);
			}
			areaGradeVec = getDynCellFormList(strDynPK);
		} else
		{// 汇总公式
			if (getDynTotleFmList(strDynPK) == null)
			{
				setupDynTotleFmlList(strDynPK);
			}
			areaGradeVec = getDynTotleFmList(strDynPK);
		}
		// 取得动态区的全部公式
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

		// 计算动态区分组数据增加步长
		// int stepRow = dynArea.isRowDirection() ? dynArea.getOriArea()
		// .getHeigth() : 0;
		// int stepCol = dynArea.isRowDirection() ? 0 : dynArea.getOriArea()
		// .getWidth();

		// 开始按区域顺序分组计算
		if (vecDynFmlAreas != null && vecDynFmlAreas.size() > 0)
		{
			IUFOTableData dataChannel = (IUFOTableData) getExecutorEnv()
					.getDataChannel();
			KeyDataGroup[] objKeydatas = getAllDynKeyDataGroups(strDynPK);
			if (objKeydatas == null || objKeydatas.length == 0)
			{
				return;
			}

			// 将动态区域各行关键字值存储在计算环境中
			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			// 将参数中不包含指标函数的财务函数进行批量计算
//			Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>> groupFuncValues = new Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>>();
//			Hashtable<IArea, UfoExpr[]> hashAreaFmExprs = batchCalcDynFinafunc(
//					dynArea, areas, objKeydatas, getExecutorEnv(), type, groupFuncValues);
//			if (hashAreaFmExprs == null || hashAreaFmExprs.size() == 0)
//			{
//				return;
//			}

			// 将动态区域各行关键字值存储在计算环境中
//			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			// 设置数据集函数动态区参数
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

			// 动态区关键字公式区域，预计算中已经处理
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

				// 将参数中不包含指标函数的财务函数进行批量计算
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


				// 分组计算单元公式
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
						// 如果该分组录入关键字为空，则跳过计算
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

//			// 分组计算单元公式
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
//					// 如果该分组录入关键字为空，则跳过计算
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

			// 将动态区各行关键字值从环境中清除及数据通道的数据集取数参数
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
				//基本函数中，如果参数为表达式类型，则检查是否包含财务函数
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
	 * 获得动态区关键字值与数据集函数字段对应Hashtable
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
//		// 分析公式的每个细节表达式
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
//				//tianchuan 2012.8.25 修改，如果是返回多值的指标函数，也同样返回true
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
	 * 将公式计算值对象转换为值对象
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
	 * 返回函数分组数据列索引 该列索引只在数据集函数中有效
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
	 * 批量计算动态区域内的参数中不包含指标函数的财务函数
	 *
	 * @param dynArea
	 * @param areas
	 *            指标对应的区域集合
	 * @return Hashtable key=指标pk， value=指标对应公式在动态区域各行的解析结果UfoExpr[]
	 * @i18n uiiufofmt00035=动态区域内批量计算preCalcDynExtFunc错误信息:
	 * @i18n uiiufofmt00036=动态区域内批量计算错误
	 */
	private Hashtable batchCalcDynFinafunc(ExtendAreaCell dynArea,
			IArea[] areas, KeyDataGroup[] objKeydatas, ICalcEnv env,
			int type, Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>> groupFuncValues)
	{

		Hashtable hashAreaFormula = null;
		Hashtable hashFinaFunc = null;

		// 获得动态区域的关键字集合
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
			// 所有合格的财务函数。key=KeyDataGroupVO value=ArrayList(其中元素类型为ExtFunc)。
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
			// 批量计算
			String strErrMsg = DynFormulaUtil.preCalcDynExtFunc(hashFinaFunc,
					getExecutorEnv(), strDynPK, groupFuncValues);
			if (strErrMsg != null && strErrMsg.length() > 0)
			{
				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0912")/*@res "动态区域内批量计算preCalcDynExtFunc错误信息:"*/ + strErrMsg);
			}
		} catch (Exception e)
		{
			AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0913")/*@res "动态区域内批量计算错误"*/, e);
		}

		// 删除动态区已预计算的公式
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
	 * 批量计算动态区域内的参数中不包含指标函数的财务函数
	 *
	 * @param dynArea
	 * @param exprTemp
	 *            公式
	 * @return Hashtable key=指标pk， value=指标对应公式在动态区域各行的解析结果UfoExpr[]
	 * @i18n uiiufofmt00035=动态区域内批量计算preCalcDynExtFunc错误信息:
	 * @i18n uiiufofmt00036=动态区域内批量计算错误
	 */
//	private Hashtable batchCalcDynFinafunc(ExtendAreaCell dynArea,
//			UfoExpr expr, KeyDataGroup[] objKeydatas, ICalcEnv env,
//			boolean bCell, Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>> groupFuncValues)
//	{
//
//		Hashtable hashFinaFunc = null;
//
//		// 获得动态区域的关键字集合
//		String strDynPK = dynArea.getBaseInfoSet().getExAreaPK();
//
//		ArrayList listDynKeyPKs = getDynKeyPKs(strDynPK);
//		MeasureFormulaChecker checker = new MeasureFormulaChecker(env,
//				objKeydatas, listDynKeyPKs);
//		try
//		{
//			checker.batchCalcDynFormula(expr);
//			// 所有合格的财务函数。key=KeyDataGroupVO value=ArrayList(其中元素类型为ExtFunc)。
//			hashFinaFunc = checker.getBatchFinaFunc();
//		} catch (CmdException e)
//		{
//			AppDebug.debug("batchCalcDynFinafunc", e);
//		}
//		try
//		{
//			// 批量计算
//			String strErrMsg = DynFormulaUtil.preCalcDynExtFunc(hashFinaFunc,
//					getExecutorEnv(), strDynPK, groupFuncValues);
//			if (strErrMsg != null && strErrMsg.length() > 0)
//			{
//				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0912")/*@res "动态区域内批量计算preCalcDynExtFunc错误信息:"*/ + strErrMsg);
//			}
//		} catch (Exception e)
//		{
//			AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0913")/*@res "动态区域内批量计算错误"*/, e);
//		}
//
//		return hashFinaFunc;
//	}

	/**
	 * 批量计算动态区域内的参数中不包含指标函数的财务函数
	 *
	 * @param dynArea
	 * @param strMeasPKs
	 * @param areas
	 *            指标对应的区域集合
	 * @return Hashtable key=指标pk， value=指标对应公式在动态区域各行的解析结果UfoExpr[]
	 * @i18n uiiufofmt00035=动态区域内批量计算preCalcDynExtFunc错误信息:
	 * @i18n uiiufofmt00036=动态区域内批量计算错误
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
//		// 获得动态区域的关键字集合
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
//			// 所有合格的财务函数。key=KeyDataGroupVO value=ArrayList(其中元素类型为ExtFunc)。
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
//			// 批量计算
//			String strErrMsg = DynFormulaUtil.preCalcDynExtFunc(hashFinaFunc,
//					getExecutorEnv(), strDynPK, groupFuncValues);
//			if (strErrMsg != null && strErrMsg.length() > 0)
//			{
//				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0912")/*@res "动态区域内批量计算preCalcDynExtFunc错误信息:"*/ + strErrMsg);
//			}
//		} catch (Exception e)
//		{
//			AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0913")/*@res "动态区域内批量计算错误"*/, e);
//		}
//
//		return hashMeasFormula;
//	}

	/**
	 * 获得动态区关键字pk集合
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
	 * 计算表页的单元汇总公式 注意汇总公式计算不执行查询和数据处理
	 *
	 * @i18n uiiufofmt00037=汇总公式计算
	 */
	public void calcAllTotalFormulas()
	{
		long time = System.currentTimeMillis();

		// 得到单元公式列表
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

		FormulaPrintUtil.printUsedTime(time, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0914")/*@res "汇总公式计算"*/);
	}

	/**
	 * 加载U8离散数据源外部函数公式
	 *
	 * @return Hashtable
	 */
	public ArrayList<String> loadExtU8Funcs()
	{
		ArrayList<String> aryFuncFormulas = new ArrayList<String>();

		// 1.对固定区公式进行处理
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
					// 得到公式右侧表达式
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

			// 得到固定区Ｕ８帐务公式
			Hashtable hashExtFunc = new Hashtable();
			CmdProxy.registerExtFuncs((UfoExpr[]) aryExpr
					.toArray(new UfoExpr[0]), hashExtFunc, 0, getExecutorEnv());
			loadExtFuncStringArray(hashExtFunc, aryFuncFormulas);
		}

		// 2.加载动态区的Ｕ８帐务公式
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
	 * 找出Ｕ８帐务公式，并做参数替换，转化为公式内容
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
				// 单元公式作参数替换，并生成公式内容
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
	 * 查找某一动态区中的Ｕ８帐务公式
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
			// 建立公式链
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

				// 同一公式对应多行数据
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
	 * @i18n miufo1000701=公式计算
	 */
	public void calcAllHBFormulas(Map<IArea, FormulaVO> mapHBFormula, MeasureVO[] measures,int formulaType)
	{
		long time = System.currentTimeMillis();

		// 初始化公式链，公式链置为空集
//		if (allHBFmlList == null) {
//			allHBFmlList = new Vector<Vector<IArea>>();
//		} else {
//			allHBFmlList.clear();
//		}

		// construct level formula list
		//2013.1.5 tianchuan 合并报表计算公式链问题，inner参数改为true
//		constructLevelFmlList(allHBFmlList, mapHBFormula, true,formulaType);

		Vector<IArea> vecArea = new Vector<IArea>();
		vecArea.addAll(mapHBFormula.keySet());
		// 将合并指标位置排除
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
		
		//tianchuan 在这里将所有的合并公式拆分为主表公式和动态区公式：
		CellsModel formatModel=DynAreaUtil.getDataModelWithExModel(getDataModel());
		DynamicAreaModel dynModel=DynamicAreaModel.getInstance(formatModel);
		
		
		Map<IArea, FormulaVO> mapHBFormulaMain=new HashMap<IArea, FormulaVO>();
		Map<String, Hashtable<IArea, FormulaVO>> dynHBAreaFmlMap=new HashMap<String, Hashtable<IArea, FormulaVO>>();	//动态区合并公式的位置映射
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
		//计算主表合并公式
		Vector<Vector<IArea>> mainHBFmlList=new Vector<Vector<IArea>>(); 
		//构建主表合并公式链
		constructLevelFmlList(mainHBFmlList, mapHBFormulaMain, true,formulaType);
		Vector<IArea> mainHBAreaVec=new Vector<IArea>();	//主表合并公式的位置
		for (Vector<IArea> vec : mainHBFmlList) {
			if (vec != null) {
				mainHBAreaVec.addAll(vec);
			}
		}
		calcFormulas(mapHBFormula, mainHBAreaVec, formulaType);	//?
		
		//计算动态区合并公式
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
				// 构建有层次结构的公式链
				setupGradeFmlList(mapHBFormulaDyn, dynHBFmlList);
				areaVec=new Vector<IArea>();
				for (Vector<IArea> vec : dynHBFmlList) {
					if (vec != null) {
						areaVec.addAll(vec);
					}
				}
				//计算动态区合并公式
				calDynAreaHBFormula(areaVec,tempEntry.getKey(), formulaType);	//?
			}finally{
				getExecutorEnv().setDynAreaInfo(null, null);
			}
		}
		FormulaPrintUtil.printUsedTime(time, "hb "
				+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0915")/*@res "公式计算"*/); // "公式计算"
	}
	
	//计算动态区合并公式
	private void calDynAreaHBFormula(Vector<IArea> vecDynFmlAreas,String strDynPK, int type)
	{
		long lstart = System.currentTimeMillis();

//		String strDynPK = dynArea.getBaseInfoSet().getExAreaPK();
		ExtendAreaCell dynArea=getDynAreaModel().getExtendAreaCellByPK(strDynPK);

		// 开始按区域顺序分组计算
		if (vecDynFmlAreas != null && vecDynFmlAreas.size() > 0)
		{
			IUFOTableData dataChannel = (IUFOTableData) getExecutorEnv()
					.getDataChannel();
			KeyDataGroup[] objKeydatas = getAllDynKeyDataGroups(strDynPK);
			if (objKeydatas == null || objKeydatas.length == 0)
			{
				return;
			}

			// 将动态区域各行关键字值存储在计算环境中
			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			// 设置数据集函数动态区参数
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

			// 将参数中不包含指标函数的财务函数进行批量计算
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


			// 分组计算单元公式
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
					// 如果该分组录入关键字为空，则跳过计算
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

			// 将动态区各行关键字值从环境中清除及数据通道的数据集取数参数
			dataChannel.removeDynAreaCalcParam();
			this.getExecutorEnv().setDynAllKeyDatas(null);
			this.getExecutorEnv().setDynField2Values(null);
		}
		FormulaPrintUtil.printUsedTime(lstart, "calculating dynamic area cost time is:");
	}
	
	
	
	
	/**
	 * 检查用户定义形式的公式。如检查成功返回公式解析结果。否则返回null，并抛出异常
	 *
	 * @param area
	 *            公式所在区域位置
	 * @param strUserDefFormula
	 *            公式内容
	 * @return com.ufsoft.script.base.IParsed 公式解析结果
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
	 * 检查用户定义形式的公式。如检查成功返回数据库形式公式内容。否则返回null，并抛出异常
	 *
	 * @param area
	 *            公式所在区域位置
	 * @param strUserDefFormula
	 *            公式内容
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

			// modify by ljhua 2005-3-17 公式定义后返回给报表格式模型的应该为公式存入数据库形式字串
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
	 * 重新设置主表关键字。此方法在设置主表关键字后调用
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

			// 检查表内审核公式
			Vector vecComplex = getFormulaModel().getComplexCheckFml();
			checkRepCheckFml(vecComplex, false);

			Vector vecSimple = getFormulaModel().getSimpleCheckFml();
			checkRepCheckFml(vecSimple, true);

		}
	}

	/**
	 * 原只删除动态区公式链时,不处理动态区公式的删除 在不影响单元公式和指标的情况下动态区支持修改，删除动态区冗余的公式
	 *
	 * @param strDynPK
	 */
	public void removeDynamicArea(String strDynPK)
	{
		// 删除动态区时，删除所有相关公式
		if (dynCellFmlMap != null)
			dynCellFmlMap.remove(strDynPK);

		if (dynTotleFmlMap != null)
			dynTotleFmlMap.remove(strDynPK);

	}

	/**
	 * 在不影响单元公式和指标的情况下动态区支持修改，删除动态区冗余的公式
	 *
	 * @param strDynPK
	 */
	public void updateDynamicArea(String strDynPK)
	{
		// 删除冗余的动态区单元公式
		removeDynRedundantFml(strDynPK, FormulaModel.TYPE_CELL_FML);

		// 删除冗余的动态区汇总公式
		removeDynRedundantFml(strDynPK, FormulaModel.TYPE_TOTAL_FML);

	}

	/**
	 * 删除动态区冗余相关公式
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
	 * 将区域位置转换为单元未知列表
	 *
	 * @param area
	 *            区域未知
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
	 * 注意所有通过公式链中记录的区域及主表公式模型，获得公式内容的情况，都需要走此接口.
	 *
	 * @param areaKey
	 *            单元区域
	 * @param mapMainFormula
	 *            主表所有公式
	 * @return
	 */
	private FormulaVO getMainFmlByArea(IArea areaKey, Map mapMainFormula)
	{
		FormulaVO oFormula = (FormulaVO) mapMainFormula.get(areaKey);
		// modify by ljhua 2007-3-2 解决组合单元首单元无法计算问题
		/*
		 * 对于组合单元,mapMainFormula中只纪录首单元key,而公式链中记录组合单元完整区域，所以此处特殊处理
		 * mapMainFormula中对于组合单元，当定义公式左侧单元不是首单元时，则存储key为该单元而非首单元。
		 */
		// AppDebug.debug("公式area="+areaKey.toString()+",第一次查询公式oFormula="+oFormula);
		if (oFormula == null && areaKey.isCell() == false)
		{
			// 处理组合单元情况
			ArrayList<CellPosition> cells = getSeperateCellPos(areaKey);
			for (CellPosition cellTemp : cells)
			{
				oFormula = (FormulaVO) mapMainFormula.get(cellTemp);
				if (oFormula != null)
				{
					// AppDebug.debug("公式area="+cellTemp.toString()+",最后-查询公式oFormula="+oFormula);
					break;
				}
			}
		}
		return oFormula;
	}

	/**
	 * 注意所有通过公式链中记录的区域及动态区公式模型，获得公式内容的情况，都需要走此接口.
	 *
	 * @param areaKey
	 *            单元区域
	 * @param hashDynFmsl
	 *            动态区所有公式
	 * @return
	 */
	private FormulaVO getDynFmlByArea(IArea areaKey, Map hashDynFmsl)
	{
		FormulaVO oFormula = (FormulaVO) hashDynFmsl.get(areaKey);
		// modify by ljhua 2007-3-2 解决组合单元首单元无法计算问题
		if (oFormula == null && areaKey.isCell() == false)
		{
			oFormula = (FormulaVO) hashDynFmsl.get(areaKey.getStart());
		}
		return oFormula;
	}

	/**
	 * 计算指定公式子项
	 *
	 * @param formulaParsedDataItem
	 * @return 计算结果值
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
	 * 计算主表公式子项
	 *
	 * @param formulaParsedDataItem
	 * @return
	 * @i18n miufo00596=预批量计算错误
	 * @i18n miufo00873=在公式追踪中，公式【
	 * @i18n miufo00874=】计算出错
	 */
	private UfoVal[] calcMainExpr(IFormulaParsedDataItem formulaParsedDataItem)
	{
		String strOld = (String) getExecutorEnv().getExEnv(
				CommonExprCalcEnv.EX_CALCEXFUNC);
		getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_CALCEXFUNC,
				CommonExprCalcEnv.EX_VALUE_ON);
		// @edit by ll at 2008-12-27,上午10:07:24 增加追踪参数信息
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
				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0477")/*@res "预批量计算错误"*/,
						e);
			}

			getExecutorEnv().setDynAreaInfo(null, null);
			return formulaParsedDataItem.getTracedExpr().getValue(
					getExecutorEnv());
		} catch (CmdException e)
		{
			AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0916")/*@res "在公式追踪中，公式【"*/
					+ formulaParsedDataItem.getTracedExpr().toUserDefString(
							getExecutorEnv())
					+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0917")/*@res "】计算出错"*/);
		}

		getExecutorEnv().setExEnv(CommonExprCalcEnv.EX_CALCEXFUNC, strOld);
		return new UfoVal[] { UfoNullVal.getSingleton() };

	}

	/**
	 * 计算动态区公式子项
	 *
	 * @param formulaParsedDataItem
	 * @return
	 * @i18n miufo00873=在公式追踪中，公式【
	 */
	private UfoVal[] calDynAreaExpr(IFormulaParsedDataItem formulaParsedDataItem)
	{
		try
		{
			// 将动态区域各行关键字值存储在计算环境中
			String strDynPK = formulaParsedDataItem.getDynAreaPK();
			ExtendAreaCell dynArea = DynamicAreaModel.getInstance(
					getCellModel()).getExtendAreaCellByPK(strDynPK);

			// 建立动态区公式链
			if (getDynCellFormList(strDynPK) == null)
			{
				setupDynCellFmlList(strDynPK);
			}

			// 取得动态区的全部公式
			// Vector<Vector<IArea>> areaGradeVector =
			// getDynCellFormList(strDynPK);
			// Vector<IArea> vecDynFmlAreas = new Vector<IArea>();
			// for(Vector<IArea> vec : areaGradeVector){
			// vecDynFmlAreas.addAll(vec);
			// }
			// IArea[] areas = new IArea[vecDynFmlAreas.size()];
			// vecDynFmlAreas.toArray(areas);

			// 计算动态区分组数据增加步长
			// int stepRow = dynArea.isRowDirection() ? dynArea.getOriArea()
			// .getHeigth() : 0;
			// int stepCol = dynArea.isRowDirection() ? 0 : dynArea.getOriArea()
			// .getWidth();

			// 开始按区域顺序分组计算
			IUFOTableData dataChannel = (IUFOTableData) getExecutorEnv()
					.getDataChannel();
			KeyDataGroup[] objKeydatas = getAllDynKeyDataGroups(strDynPK);
			if (objKeydatas == null || objKeydatas.length == 0)
			{
				return null;
			}

			// 将动态区域各行关键字值存储在计算环境中
			KeyDataGroup keyDataGroup = objKeydatas[formulaParsedDataItem
					.getUnitDataNum()];
			objKeydatas = new KeyDataGroup[] { keyDataGroup };


			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);

			Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>> groupFuncValues =
				new Hashtable<ExtFunc,Hashtable<KeyDataGroup,UfoVal[]>>();

			// 将参数中不包含指标函数的财务函数进行批量计算
//			Hashtable hashFinaFuncs = batchCalcDynFinafunc(dynArea,
//					formulaParsedDataItem.getTracedExpr(), objKeydatas,
//					getExecutorEnv(), true, groupFuncValues);
//			Hashtable hashFinaFuncs = null;

//			this.getExecutorEnv().setDynAllKeyDatas(objKeydatas);
			
			UfoExpr traceExpr = formulaParsedDataItem.getTracedExpr();
			boolean isCodeFill = isCodeFill(traceExpr);

			// 设置数据集函数动态区参数
			Hashtable<Integer, Hashtable<String, Object>> mapField2Values = getDynAreaField2KeyValues(
					dynArea, FormulaModel.TYPE_CELL_FML, isCodeFill);
			this.getExecutorEnv().setDynField2Values(mapField2Values);

			// 分组计算单元公式
			this.getExecutorEnv().setDynAreaInfo(strDynPK, keyDataGroup);
			// @edit by ll at 2008-12-26,上午10:07:24 增加追踪参数信息
			if (formulaParsedDataItem.isInTraceNow())
				getExecutorEnv().setMeasureTraceVOs(new MeasureTraceVO[0]);
			dataChannel.setDynAreaCalcParam(new IUFODynAreaDataParam(
					formulaParsedDataItem.getUnitDataNum(), null, strDynPK));
			// @edit by wuyongc at 2013-6-17,下午2:19:03 尼玛，申明一个变量，没有设置任何值，还IF两个分支。。。
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
			AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0916")/*@res "在公式追踪中，公式【"*/
					+ formulaParsedDataItem.getTracedExpr().toUserDefString(
							getExecutorEnv()) + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0917")/*@res "】计算出错"*/);
		} finally
		{
			getExecutorEnv().setDynAreaInfo(null, null);
		}
		return new UfoVal[] { UfoNullVal.getSingleton() };

	}

	/**
	 * 判断该表达式是否可以执行区域计算
	 *
	 * @create by wangyga at 2009-1-13,下午06:26:33
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
			ufoExpr = (UfoExpr) ((UfoCmdLet) parsedLet).getLetList().get(1);// 取出函数右边表达式
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
	 * 得到动态区关键字定义(关键字位置，关键字)
	 *
	 * @create by liuchuna at 2010-12-1,下午02:19:06
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
	 * 根据区域位置、公式类型将公式加入到公式模型中
	 * 适用公式类型：合并公式、折算公式
	 *
	 * @create by liuchuna at 2010-9-8,下午04:49:42
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
		//tianchuan 20141009 合并和折算公式支持循环引用的校验
		IParsed dbLet=null;
		try {
			turnOffExtFuncCheck();
			if (dbLet == null) {
				//tianchuan 20141015 合并和折算公式在这里是Db型的
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
		// 用于校验循环公式，存储格式态的位置
		Vector<String> areaVec = new Vector<String>();
		//tianchuan 2013.1.22 常量太乱，在这里做一个转换，由FormulaManageConst中的常量转换为FormulaModel中的常量。。。。。
		int fmlTypeInFmlModel=-1;
		switch (fmlType) {
			case FormulaManageConst.ZS_FML_NUM:
				fmlTypeInFmlModel=FormulaModel.TYPE_ZS_FML;
				break;
			default:
				fmlTypeInFmlModel=FormulaModel.TYPE_CONS_FML;
				break;
		}
		// 校验是否存在循环引用，注意isLoopRef中传的公式类型一定是FormulaModel里的常量，先这样处理
		Vector<String> dealedVec = new Vector<String>();
		if(isLoopRef(areaVec, area, dbLet, strFormula, fmlTypeInFmlModel, true, dealedVec)){
			throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0907")/*@res "定义的公式存在循环引用！"*/);
		}
		//tianchuan 20141009  end
		
		// 根据区域位置判断该区域是否在动态区内
		ExtendAreaCell dynVO = null;
		try {
			dynVO = getValidDynamicArea(area);
		} catch (DynAreaException e) {
			return false;
		}

		boolean bReturn = false;
		if (dynVO != null) {
			// 添加动态区公式
			String strDynPK = dynVO.getBaseInfoSet().getExAreaPK();
			bReturn = addDynFormulaByType(strDynPK, area, strFormula, fmlType);
		} else {
			// 添加主表的公式
			bReturn = addMainFormulaByType(area, strFormula, fmlType);
		}
		return bReturn;

	}

	public boolean removeFormulaByFmlType(IArea area, int fmlType) throws Exception {
		if (area == null) {
			return false;
		}

		// 根据区域位置判断该区域是否在动态区内
		ExtendAreaCell dynVO = null;
		try {
			dynVO = getValidDynamicArea(area);
		} catch (DynAreaException e) {
			return false;
		}

		boolean bReturn = false;
		if (dynVO != null) {
			// 删除动态区公式
			String strDynPK = dynVO.getBaseInfoSet().getExAreaPK();
			bReturn = removeDynFormulaByType(strDynPK, area, fmlType);
		} else {
			// 删除主表的公式
			bReturn = removeMainFormulaByType(area, fmlType);
		}
		return bReturn;

	}

	private boolean addMainFormulaByType(IArea area, String strFormula, int fmlType)
			throws Exception {

		try {
			turnOffExtFuncCheck();

			// 固定区域清除动态信息
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

			// 动态区域需要设置环境变量
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
	 * 公式处理过程的异常类型
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
		// liuchun+ at 20110519 根据可见性范围设置当前组织可以参照哪些组织中的报表
		// 针对导入公式、或者手动的写入其它组织的报表中的指标的情况
		String pk_group = (String)getContextVO().getAttribute(IUfoContextKey.CUR_GROUP_PK);
		String strCurOrgPK = (String)getContextVO().getAttribute(IUfoContextKey.CUR_REPORG_PK);

		Integer operType = (Integer)getContextVO().getAttribute(IUfoContextKey.OPERATION_STATE);
		if(operType != null) {
			if(IUfoContextKey.OPERATION_INPUT == operType.intValue()) {
				// 数据录入态不需要可见性范围
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
	 * 检查数据库形式的公式内容，检查成功则返回检查结果，否则抛出异常
	 *@author by tanyj at 2012-8-25 上午9:15:09
	 * @param fVO
	 * @param areaKey
	 * @return
	 * @throws Exception
	 */
	public IParsed parseDBFml(FormulaVO fVO,IArea areaKey){
		try{
			IParsed parsedLet = null;
			// 如果公式解析为空，则解析公式,并设置公式解析结果
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