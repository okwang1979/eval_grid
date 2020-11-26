package com.ufsoft.iufo.fmtplugin.dynarea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bd.accperiod.InvalidAccperiodExcetion;
import nc.bs.framework.common.NCLocator;
import nc.itf.iufo.storecell.IStoreCellPackQrySrv;
import nc.pub.iufo.accperiod.AccPeriodSchemeUtil;
import nc.pub.iufo.basedoc.GeneralAccessorUtil;
import nc.pub.iufo.basedoc.IDName;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pub.smart.cache.SmartDefCache;
import nc.pub.smart.cache.SmartModelCache;
import nc.pub.smart.context.SmartContext;
import nc.pub.smart.data.DataSet;
import nc.pub.smart.exception.SmartException;
import nc.pub.smart.metadata.DataTypeConstant;
import nc.pub.smart.metadata.Field;
import nc.pub.smart.metadata.MetaData;
import nc.pub.smart.model.SmartModel;
import nc.pub.smart.model.descriptor.Descriptor;
import nc.pub.smart.model.descriptor.FilterDescriptor;
import nc.pub.smart.model.descriptor.FilterItem;
import nc.pub.smart.model.descriptor.SortDescriptor;
import nc.pub.smart.model.descriptor.SortItem;
import nc.pub.smart.model.preferences.Parameter;
import nc.pub.smart.provider.SemanticDataProvider;
import nc.util.iufo.pub.IDMaker;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.keydef.KeySmartMapping;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.iufo.storecell.StoreCellVO;
import nc.vo.pub.lang.UFDouble;
import nc.vo.smart.SmartDefVO;

import com.borland.dx.dataset.Variant;
import com.ufida.dataset.IContext;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.table.exarea.ExtendAreaCell;
import com.ufida.iufo.table.exarea.ExtendAreaConstants;
import com.ufida.iufo.table.exarea.ExtendAreaModel;
import com.ufida.iufo.table.model.ReportFilterDescriptor;
import com.ufida.iufo.table.model.SortDescMng;
import com.ufida.report.anareport.model.AnaDataSetTool;
import com.ufida.report.anareport.model.AnaRepField;
import com.ufida.report.anareport.model.FreeAreaHideFields;
import com.ufida.report.anareport.util.AreaDescUtil;
import com.ufida.report.crosstable.FixField;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formula.FormulaVO;
import com.ufsoft.iufo.fmtplugin.key.KeywordModel;
import com.ufsoft.iufo.fmtplugin.kmanaset.DynFmtProvider;
import com.ufsoft.iufo.fmtplugin.kmanaset.KMAnaSetModel;
import com.ufsoft.iufo.fmtplugin.kmanaset.KMSortDescMng;
import com.ufsoft.iufo.repauth.RepDataAuthValidator;
import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CombinedAreaModel;
import com.ufsoft.table.CombinedCell;
import com.ufsoft.table.format.IFormat;
import com.ufsoft.table.format.NumberFormat;
import com.ufsoft.table.format.UFDoubleFormat;

/**
 * 动态区数据提供者，根据动态区定义的关键字和存储单元构建元数据。
 * 
 * @author wanyonga
 * 
 */
public class DynAreaProvider extends SemanticDataProvider
{
	private static final long serialVersionUID = 2581800231680956521L;
	
	public static String TIMEFIELD_CAPTION = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1155")/*@res "日期"*/;
	/**
	 * 动态区PK
	 */
	private String exCellPK;
	/**
	 * 动态区管理模型
	 */
	private DynamicAreaModel model;
	/**
	 * 报表原始数据
	 */
	private transient RepDataVO repData;
	/**
	 * 动态区模型
	 */
	private transient ExtendAreaCell dynCell;
	/**
	 * 二维数据
	 */
	private Object[][] datas;
	/**
	 * 动态区关键字列表
	 */
	private transient List<KeyVO> keys = new ArrayList<KeyVO>();
	
	/**
	 * 元数据字段及对应的存储单元和关键字的映射
	 */
	private Hashtable<String, Object> fldRefVo = new Hashtable<String, Object>();
	/**** key(关键字、指标、存储单元、公式)标识------value(对应字段名) *****/
	private Hashtable<String, String> metaToFldNameMap = new Hashtable<String, String>();
	/**
	 *动态区当前显示数据对应的数据集
	 */
	private DataSet displayDataSet;
	/**
	 * aloneid和原始数据集行号的对应关系
	 */
	private Hashtable<String, Integer> aloneIDToRow = new Hashtable<String, Integer>();
	/**
	 * 删除的数据集行的aloneIdList
	 */
	private List<String> deleteAloneList = new ArrayList<String>();
	/**
	 * 是否更新提供者的数据
	 */
	private boolean isDirty = false;
	/**
	 * 关键字在元数据中的索引
	 */
	private int[] keyIndex = null;
	/**
	 * 同一单元位置上录入态字段和格式定义字段名字的映射关系
	 */
	private transient Hashtable<String, String> fieldToRefFieldMap = null;
	/**
	 * 同一单元位置上录入态字段和格式定义字段索引的映射关系
	 */
	private Hashtable<Integer, Integer> fieldToRefFieldIndexMap = null;
	/**
	 * 采集表格式定义时动态区引用的语义模型的定义ID
	 */
	private String smartRefId;
	/**
	 * 采集表动态区引用语义模型格式定义时定义的筛选条件
	 */
	private FilterDescriptor[] formatDefFilters;
	/**
	 * 采集表格式设计时动态区(关键字、指标、存储单元)上的排序管理设置
	 */
	private transient KMSortDescMng kmSortDescMng;
	/**
	 * 采集表格式设计时动态区排序设置，只影响当始加载数据的显示
	 */
	private transient SortDescriptor sortDescriptor;
	/**
	 * 采集表格式设计时排序项和采集表录入时字段的映射关系
	 */
	private transient Hashtable<SortItem, Field> sortItemToFieldMap = null;
	/**
	 * 采集表动态区引用语义模型定义的参数和值
	 */
	private Parameter[] params;
	
	// 主表关键字生成的参数和值,筛选条件用
	private ArrayList<Parameter> mainKeyParams;
	
	private MeasurePubDataVO mainPubData = null;
	
	//在公式追踪时，为了保证关键字顺序和实际的顺序一致
	private List<KeyVO> keyVOs = new ArrayList<KeyVO>();
	
	private RepDataAuthValidator validator = null;

	public DynAreaProvider(String exCellPK, DynamicAreaModel model,
			RepDataVO repData, IContext context)
	{
		this.exCellPK = exCellPK;
		this.model = model;
		this.repData = repData;
		if (repData != null)
		{
			this.buildMeta();
			this.buildDatas(context);
			this.initUserDefSortDesc();
		}
	}

	@Override
	public DataSet provideData(SmartContext context)
	{
		MetaData metaData = getMetaData();
		return new DataSet(metaData, displayDataSet.getData2Array());// 用displayDataSet中的展示数据
	}

	@Override
	public MetaData provideMetaData(SmartContext context)
	{
		return getMetaData();
	}

	/**
	 * 将动态区格式设计时定义的固定成员添加到格式定义总的筛选描述器中
	 * 
	 * @create by wanyonga at 2010-7-19,上午10:09:06
	 * 
	 * @param formatField
	 */
	private void addFixFilter(AnaRepField formatField,
			FilterDescriptor formatDefFilter)
	{
		boolean isCharType = formatField.getFieldDataType() == Variant.STRING;
		if (formatField != null && formatField.getFixValues() != null
				&& formatField.getFixValues().size() > 0)
		{
			ArrayList<FixField> anaFlds = formatField.getFixValues();
			ArrayList<String> values = new ArrayList<String>();
			for (int j = 0; j < anaFlds.size(); j++)
			{
				values.add(anaFlds.get(j).getID().toString());
			}
			String value = AnaDataSetTool.convert2String(values, isCharType);
			FilterItem item = new FilterItem();
			item.setOperation(FilterItem.FILTER_OPERATOR_IN);
			item.setFieldInfo(formatField.getField());
			item.setValue(value);
			formatDefFilter.addFilter(item);
		}
	}

	/**
	 * 将动态区格式设计时定义值筛选、扩展区级的筛选、报表级筛选添加到格式定义总的筛选描述器中
	 * 
	 * @create by wanyonga at 2010-7-19,上午10:15:04
	 * 
	 */
	private FilterDescriptor[] addFilter(ExtendAreaCell exCell,
			FilterDescriptor formatDefFilter)
	{
		if (exCell.getAreaInfoSet().getSmartModelDefID() != null)
		{
			return AreaDescUtil.addFilterToDesc(exCell, ExtendAreaModel
					.getInstance(model.getCellsModel()), formatDefFilter,
					SmartModelCache.getInstance().getModel(smartRefId));
		} else if (formatDefFilter != null
				&& formatDefFilter.getFilterItemCount() > 0)
		{
			return new FilterDescriptor[] { formatDefFilter };
		} else
		{
			return null;
		}

	}

	/**
	 * 清除动态区格式态的分析设置
	 * 
	 * @create by wanyonga at 2010-7-19,上午11:15:28
	 * 
	 * @param exCell
	 */
	private void clearFormatAnaSet(ExtendAreaCell exCell)
	{
		// 引用语义模型
		if (exCell.getAreaInfoSet().getSmartModelDefID() != null)
		{
			exCell.getAreaInfoSet().setSmartModelDefID(null);
		}
		// 值筛选
		if (exCell.getAreaInfoSet().getValueFilter() != null)
		{
			exCell.getAreaInfoSet().setValueFilter(null);
		}
		// 扩展区级筛选
		FilterDescriptor areaFilter = exCell.getAreaInfoSet().getAreaFilter();
		if (areaFilter != null)
		{
			exCell.getAreaInfoSet().setAreaFilter(null);
		}
		// 报表级筛选
		if (smartRefId != null)
		{
			FilterDescriptor reportFilter = ReportFilterDescriptor.getInstance(
					ExtendAreaModel.getInstance(model.getCellsModel()))
					.getReportFilter(smartRefId);
			if (reportFilter != null)
			{
				ReportFilterDescriptor.getInstance(
						ExtendAreaModel.getInstance(model.getCellsModel()))
						.setReportFilter(smartRefId, null);
			}
		}
		// 参数
//		exCell.getAreaInfoSet().clearChangedParams();
	}

	/**
	 * 将动态区格式态关键字、指标、存储单元上排序设置添加到描述器
	 * 
	 * @create by wanyonga at 2010-8-30,上午09:53:13
	 * 
	 * @param meta
	 * @param fld
	 */
	private void addSortTODesc(Object meta, Field fld, int keyMode)
	{
		if (meta == null)
		{
			return;
		}
		KMAnaSetModel anaSetModel = KMAnaSetModel.getInstance(model
				.getCellsModel());
		String fldName = null;
		if (kmSortDescMng != null)
		{
			SortItem item = null;
			if (meta instanceof KeyVO)
			{
				if(keyMode == 0) {
					fldName = anaSetModel.getAnaSetFieldName(exCellPK, ((KeyVO) meta).getPk_keyword());
				} else if(keyMode == 1) {
					fldName = anaSetModel.getAnaSetFieldName(exCellPK, ((KeyVO) meta).getPk_keyword() + DynFmtProvider.NAME_SUFFIEX);
				} else {
					fldName = anaSetModel.getAnaSetFieldName(exCellPK, ((KeyVO) meta).getPk_keyword() + DynFmtProvider.CODE_SUFFIEX);
				}
//				fldName = anaSetModel.getAnaSetFieldName(exCellPK,
//						((KeyVO) meta).getPk_keyword());
				if (fldName != null)
				{
					item = kmSortDescMng.getSortDesc().getSortItem(fldName);
				}
			} else if (meta instanceof IStoreCell)
			{
				fldName = anaSetModel.getAnaSetFieldName(exCellPK,
						((IStoreCell) meta).getCode());
				if (fldName != null)
				{
					item = kmSortDescMng.getSortDesc().getSortItem(fldName);
				}
			}
			if (item != null)
			{
				if (kmSortDescMng.isUserDef())
				{
					// 按用户定义的顺序指定排序字段顺序
					if (sortItemToFieldMap == null)
					{
						sortItemToFieldMap = new Hashtable<SortItem, Field>();
					}
					sortItemToFieldMap.put(item, fld);
				} else
				{
					// 按界面顺序
					SortItem newItem = new SortItem(fld, item.isDescending());
					sortDescriptor.addSort(newItem);
				}
			}
		}
	}

	/**
	 * 获得采集表格式设计时对关键字、指标、存储单元的排序描述器
	 * 
	 * @create by wanyonga at 2010-8-30,上午10:45:20
	 * 
	 * @return
	 */
	public SortDescriptor getFormatSortDescriptor()
	{
		return sortDescriptor;
	}

	/**
	 *将格式设计时的排序描述器转成采集表录入时对应字段和顺序的排序描述器
	 * 
	 * @create by wanyonga at 2010-8-30,上午10:20:59
	 * 
	 */
	private void initUserDefSortDesc()
	{
		if (kmSortDescMng != null && kmSortDescMng.isUserDef())
		{
			SortDescriptor sortDesc = kmSortDescMng.getSortDesc();
			int num = sortDesc.getSortNum();
			SortItem item = null;
			SortItem newItem = null;
			Field field = null;
			for (int index = num - 1; index >= 0; index--)
			{
				item = sortDesc.getSortItem(index);
				if(sortItemToFieldMap != null) {
					field = sortItemToFieldMap.get(item);
					if (field != null)
					{
						newItem = new SortItem(field, item.isDescending());
						sortDescriptor.insertSort(newItem, 0);
					}
				}
			}
		}
	}

	/**
	 * 根据动态区定义关键字、指标、存储单元和公式构建元数据
	 */
	private void buildMeta()
	{
		dynCell = model.getExtendAreaCellByPK(exCellPK);
		
		KMAnaSetModel anaSetModel = KMAnaSetModel.getInstance(model.getCellsModel());
		kmSortDescMng = anaSetModel.getDynAreaSortDescByPk(exCellPK);
		if(kmSortDescMng == null) {
			SortDescMng sortDescMng = (SortDescMng)dynCell.getAreaInfoSet().getAreaLevelInfo("iufo_input_default_ana_order");
			if(sortDescMng != null) {
				SortDescriptor sortDesc = sortDescMng.getSortDesc();
				kmSortDescMng = new KMSortDescMng();
				kmSortDescMng.setSortDesc(sortDesc);
				kmSortDescMng.setUserDef(true);
			}
		}
		if (kmSortDescMng != null)
		{
			sortDescriptor = new SortDescriptor();
		}
		ArrayList<Field> fldList = new ArrayList<Field>();
		
		params = dynCell.getAreaInfoSet().getAreaParams(
				ExtendAreaModel.getInstance(model.getCellsModel()));
		if (dynCell.getAreaInfoSet().getSmartModelDefID() != null)
		{
			smartRefId = dynCell.getAreaInfoSet().getSmartModelDefID();
		}
		if (smartRefId != null)
		{
			fieldToRefFieldMap = new Hashtable<String, String>();
		}
		IStoreCellPackQrySrv storeCellPackQrySrv = NCLocator.getInstance().lookup(IStoreCellPackQrySrv.class);
		Hashtable<String, IStoreCell> storeCells = null;
		try {
			storeCells = storeCellPackQrySrv.getStoreCellsByRepID(repData.getReportPK());
		} catch (UFOSrvException e) {
			AppDebug.debug(e);
		}
		CellPosition[] cells = dynCell.getArea().split();
		CellPosition temp = null;
		StoreCellVO storeCell = null;
		MeasureVO measure = null;
		KeyVO key = null;
		Field field = null;
		AnaRepField anaField = null;
		AnaRepField formatField = null;
		FormulaVO formulaVO = null;
		int keyIndex = 1;
		int measIndex = 1;
		int storeIndex = 1;
		int formIndex = 1;
		FilterDescriptor formatDefFilter = new FilterDescriptor();
		CombinedAreaModel combModel=CombinedAreaModel.getInstance(model.getCellsModel());
		
		List<Field> refflds = new ArrayList<Field>();
		for (int i = 0; i < cells.length; i++)
		{
			temp = cells[i];
			
			IFormat format = model.getCellsModel().getCellFormat(temp);
			int digit = -2;
			if(format != null) {
				if(format.getDataFormat() instanceof NumberFormat) {
					digit = ((NumberFormat)format.getDataFormat()).getDecimalDigits();
				} else if(format.getDataFormat() instanceof UFDoubleFormat) {
					digit = ((UFDoubleFormat)format.getDataFormat()).getDecimalDigits();
				}
			}
			
			CombinedCell combCell=combModel.belongToCombinedCell(temp);
			if (combCell!=null && !combCell.getArea().getStart().equals(temp))
				continue;
			
			key = model.getKeywordModel().getDynAreaKeyVOByPos(exCellPK, temp);
			if (key != null)
			{
				if (isRefKey(key))
				{
					// 参照类型关键字
					Field[] flds = convertRefKeyVOToField(key, keyIndex);
					for(Field f : flds) {
//						if(!f.getFldname().equals(key)) {
							refflds.add(f);	
//						}
					}
					if(flds != null) {// TODO 可能没有映射字段 add by yuyangi
						for (Field f : flds)
						{
							fldList.add(f);
							fldRefVo.put(f.getFldname(), key);
						}
						field = flds[0];
						
						Field nameField = flds[1];
						addSortTODesc(key, nameField, 1);
						Field codeField = flds[2];
						addSortTODesc(key, codeField, 2);
					}
				} else
				{
					field = convertKeyVOToField(key, keyIndex);
					fldList.add(field);
					fldRefVo.put(field.getFldname(), key);
				}
				keys.add(key);
				keyVOs.add(key);
				addSortTODesc(key, field, 0);
				metaToFldNameMap.put(key.getPk_keyword(), field.getFldname());
				keyIndex++;
			} else
			{
				measure = model.getMeasureModel().getDynAreaMeasureVOByPos(exCellPK, temp);
				if (measure != null)
				{
					String fldName=anaSetModel.getAnaSetFieldName(exCellPK,measure.getCode());
					field = convertMeasureToField(measure, measIndex, digit,fldName);
					fldList.add(field);
					fldRefVo.put(field.getFldname(), measure);
					addSortTODesc(measure, field, 0);
					measIndex++;
					metaToFldNameMap.put(measure.getCode(), field.getFldname());
				} else
				{
					storeCell = (StoreCellVO) storeCells.get(temp.toString());
					if (storeCell != null)
					{
						String fldName=anaSetModel.getAnaSetFieldName(exCellPK,storeCell.getCode());
						field = convertStoreCellToField(storeCell, storeIndex, digit,fldName);
						fldList.add(field);
						fldRefVo.put(field.getFldname(), storeCell);
						addSortTODesc(storeCell, field, 0);
						storeIndex++;
						metaToFldNameMap.put(storeCell.getCode(), field
								.getFldname());
					} else
					{
						// 在非指标或关键字位置上定义的公式也作为构建的语义模型字段
						formulaVO = model.getFormulaModel()
								.getDynCellFmlByArea(exCellPK, temp);
						if (formulaVO != null)
						{
							field = convertFormulaVOToField(formulaVO, temp
									.toString(), formIndex);
							fldList.add(field);
							fldRefVo.put(field.getFldname(), formulaVO);
							formIndex++;
							metaToFldNameMap.put(temp.toString(), field
									.getFldname());
						} else
						{

							field = null;
						}
					}
				}
			}
			if (model.getCellsModel().getCell(temp) != null)
			{
				formatField = (AnaRepField) model.getCellsModel().getCell(temp)
						.getExtFmt(ExtendAreaConstants.FIELD_INFO);
			} else
			{
				formatField = null;
			}
			if (field != null)
			{
				anaField = new AnaRepField(field,
						AnaRepField.TYPE_DETAIL_FIELD, getSmartDefId());
				dynCell.getCellInfoSet().addExtInfo(temp,
						ExtendAreaConstants.FIELD_INFO, anaField);
				if (formatField != null)
				{
					this.addFixFilter(formatField, formatDefFilter);
					if(fieldToRefFieldMap != null) {
						this.fieldToRefFieldMap.put(field.getFldname(), formatField
								.getField().getFldname());
					}
				}
				model.getCellsModel().getCell(temp).addExtFmt(
						ExtendAreaConstants.FIELD_INFO, anaField);
			} else
			{
				if (formatField != null)
				{
					// 存在不是存储单元位置上的字段
					model.getCellsModel().getCell(temp).removeExtFmt(
							ExtendAreaConstants.FIELD_INFO);
				}
			}
		}
		field = convertAloneIDToField();
		fldList.add(field);
		FreeAreaHideFields hideFields = (FreeAreaHideFields)dynCell.getAreaInfoSet().getAreaLevelInfo(FreeAreaHideFields.KEY_HIDE_FIELDS);
		if(hideFields != null) {
			Field[] flds = hideFields.getHideFlds();
			if(flds != null) {
				for(Field f : flds) {
					fldList.add(f);
					if(fieldToRefFieldMap != null) {
						this.fieldToRefFieldMap.put(f.getFldname(), f.getFldname());
					}
				}
			}
			flds = hideFields.getHideAggrFlds();
			if(flds != null) {
				for(Field f : flds) {
					fldList.add(f);
					if(fieldToRefFieldMap != null) {
						this.fieldToRefFieldMap.put(f.getFldname(), f.getFldname());
					}
				}
			}
		} else {
			if(refflds != null) {
				hideFields = new FreeAreaHideFields();
				hideFields.setHideFlds(refflds.toArray(new Field[0]));	
				dynCell.getAreaInfoSet().addAreaLevelInfo(FreeAreaHideFields.KEY_HIDE_FIELDS, hideFields);
			}
		}
		Field[] flds = fldList.toArray(new Field[0]);
		MetaData metaData = new MetaData(flds);
		setMetaData(metaData);
		this.formatDefFilters = addFilter(dynCell, formatDefFilter);
		clearFormatAnaSet(dynCell);
	}

	/**
	 * 构建关键字对应的元数据索引
	 */
	private void buildIndexs()
	{
		keyIndex = new int[keys.size()];
//		KeywordModel keywordModel = model.getKeywordModel();
		
		for (int i = 0; i < keyIndex.length; i++)
		{
			if (isRefKey(keys.get(i)))
			{
				
				keyIndex[i] = getMetaData().getIndex(
						getFldNameByMetaData(keys.get(i).getPk_keyword()));
			} else
			{
				keyIndex[i] = getMetaData().getIndex(
						getFldNameByMetaData(keys.get(i).getPk_keyword()));
			}
		}
	}

	/**
	 * 构建同一单元位置上录入态字段和格式定义字段名字的映射关系
	 * 
	 * @create by wanyonga at 2010-6-12,下午03:23:26
	 * 
	 */
	private void buildFieldToRefFieldIndex()
	{
		if (fieldToRefFieldMap != null && fieldToRefFieldMap.size() > 0)
		{
			fieldToRefFieldIndexMap = new Hashtable<Integer, Integer>();
			SmartModel sm = SmartModelCache.getInstance().getModel(smartRefId);
			if(sm == null) {
				return ;
			}
			Set<String> nameSet = fieldToRefFieldMap.keySet();
			for (String name : nameSet)
			{
				fieldToRefFieldIndexMap.put(getMetaData().getIndex(name), sm
						.getMetaData().getIndex(fieldToRefFieldMap.get(name)));
			}
		}
	}

	/**
	 * 根据RepDataVO构建二维数据
	 */
	private void buildDatas(IContext context)
	{
		// TODO:MeasurePubDataVO维度值需要添加
		if (repData != null)
		{
			this.buildIndexs();
			this.buildFieldToRefFieldIndex();
			MeasureDataVO[][] mDataVos = repData.getMeasureDatasAsMatrix(model
					.getKeyCompPK(model.getExtendAreaCellByPK(exCellPK)));
			if (mDataVos == null || mDataVos.length == 0)
			{
				// update by wanyonga 2010-3-23 无关键字指标数据，返回全是null的一组
				if (getMetaData() != null && getMetaData().getFieldNum() > 0)
				{
					datas = new Object[1][getMetaData().getFieldNum()];
					datas[0][getMetaData().getFieldNum() - 1] = IDMaker
							.makeID(12);
					aloneIDToRow.put((String) datas[0][getMetaData()
							.getFieldNum() - 1], Integer.valueOf(0));
					setDisplayDataSet(new DataSet(getMetaData(), datas));
					
				}
				// end
				return;
			}
			datas = new Object[mDataVos.length][];
			Object[] rowDatas = null;
			MeasureDataVO[] rowMeasureDataVOs;
			int index = 0;
			MeasurePubDataVO tempPubData;
			for (int i = 0; i < mDataVos.length; i++)
			{
				rowMeasureDataVOs = mDataVos[i];
				rowDatas = new Object[getMetaData().getFieldNum()];
				rowDatas[rowDatas.length - 1] = rowMeasureDataVOs[0]
						.getAloneID();
				aloneIDToRow.put(rowMeasureDataVOs[0].getAloneID(),
						Integer.valueOf(i));
				for (int k = 0; k < rowMeasureDataVOs.length; k++)
				{
					if (rowMeasureDataVOs[k] != null)
					{
						index = getMetaData().getIndex(
								getFldNameByMetaData(rowMeasureDataVOs[k]
										.getMeasureVO().getCode()));
						if(index==-1) continue;
						if (rowMeasureDataVOs[k].getMeasureVO().getType() == IStoreCell.TYPE_NUMBER)
						{
							rowDatas[index] = new Double(rowMeasureDataVOs[k]
									.getDataValue());
						} else if(rowMeasureDataVOs[k].getMeasureVO().getType() == IStoreCell.TYPE_BIGDECIMAL){
							rowDatas[index] = new UFDouble(rowMeasureDataVOs[k]
							           									.getDataValue());
						} //tianchuan 2012.9.24 参照指标的处理
						else if(rowMeasureDataVOs[k].getMeasureVO() instanceof MeasureVO && ((MeasureVO)rowMeasureDataVOs[k].getMeasureVO()).getRefPK()!=null){
							MeasureVO meas=(MeasureVO)rowMeasureDataVOs[k].getMeasureVO();
							String refPk= meas.getRefPK();
							String strOrgPK = (String)context.getAttribute(IUfoContextKey.CUR_REPORG_PK);
							String groupPK = (String)context.getAttribute(IUfoContextKey.CUR_GROUP_PK);
							IDName idName = GeneralAccessorUtil.getDocDataByCode(refPk, ""+rowMeasureDataVOs[k].getDataValue(), strOrgPK, groupPK);
							if (idName!=null){
								rowDatas[index] = idName;
							}else{
								rowDatas[index] = rowMeasureDataVOs[k].getDataValue();
							}
						} else
						{
							rowDatas[index] = rowMeasureDataVOs[k]
									.getDataValue();
						}
					}
				}
				for (KeyVO key : keys)
				{
					tempPubData = repData.getPubData(rowMeasureDataVOs[0]
							.getAloneID());
					if (isRefKey(key))
					{
						// 参照类型关键字
						this.setRefKeyData(rowDatas, key, tempPubData);
					} else
					{
						index = getMetaData().getIndex(
								getFldNameByMetaData(key.getPk_keyword()));
						rowDatas[index] = tempPubData.getKeywordByName(key
								.getName());
					}
				}
				datas[i] = rowDatas;
			}
			setDisplayDataSet(new DataSet(getMetaData(), datas));
		}
	}
	
	/**
	 * 此方法在报表保存后，将语义提供者中的动态区aloneid重新填充
	 * 
	 * @create by liuchuna at 2011-6-10,下午02:45:10
	 *
	 * @param mDataVos
	 */
	public void fillAloneIdAfterSave(MeasurePubDataVO[] pubDatas, KeyVO[] keys) {
		if (pubDatas != null && pubDatas.length > 0) {
			// 封装关键字对应的pubDatas
			Map<String, MeasurePubDataVO> keyMap = new Hashtable<String, MeasurePubDataVO>();
			for(int i = 0, n = pubDatas.length; i < n; i++) {
				
				String combKeyValue = "";
				for (KeyVO key : keys)
				{
					String keyValue = pubDatas[i].getKeywordByPK(key.getPk_keyword());
					
					combKeyValue = combKeyValue + "," + keyValue;
				}
				
				keyMap.put(combKeyValue, pubDatas[i]);
			}
			
			if(datas != null && datas.length > 0) {
				
				//　根据每行关键字的值，填充aloneid
				for(int j = 0, n = datas.length; j < n; j++) {
					Object[] object =  datas[j];
					String combKeyValue = "";
					for (KeyVO key : keys) {
						
						int keyIndex;
						if (isRefKey(key)) {
							// 参照类型关键字
							KeySmartMapping mapping = model.getKeywordModel().getKeySmartMapping(key.getPk_keyword());
							if(mapping == null)
								keyIndex = getIndex(getFldNameByMetaData(key.getPk_keyword()));
							else
								keyIndex = getIndex(mapping.getId() + "_map");
						} else {
							keyIndex = getMetaData().getIndex(getFldNameByMetaData(key.getPk_keyword()));
						}
						Object keyValue = object[keyIndex];
						if(keyValue != null)
							combKeyValue = combKeyValue + "," + keyValue;
					}
					
					if(keyMap.containsKey(combKeyValue)) {
						String aloneId = keyMap.get(combKeyValue).getAloneID();
						object[getMetaData().getFieldNum() - 1] = aloneId;
						
						if(aloneId != null) {
							aloneIDToRow.put(aloneId, j);
						}
					}
				}
			}
		}
	}

	/**
	 * 将关键字定义转为字段
	 * 
	 * @param key
	 * @param index
	 * @return
	 */
	public static Field convertKeyVOToField(KeyVO key, int keyIndex)
	{
		Field field = new Field();
		field.setDataType(DataTypeConstant.STRING);
		field.setFldname(DynFmtProvider.KEY_PREFIX + keyIndex);
		field.setCaption(key.getName() + "(" + field.getFldname() + ")");
		field.setPrecision(64);// @edit by ll at 2010-4-16,下午02:33:14
		// weixl要求统一设置成64
		field.setNote(key.getNote());
		return field;
	}

	/**
	 * 将一个存储单元转换为元数据字段
	 * 
	 * @create by wanyonga at 2010-6-18,上午09:11:48
	 * @return
	 */
	private Field convertStoreCellToField(StoreCellVO storeCell, int storeIndex, int digit,String fldName)
	{
		Field field = new Field();
		if (storeCell.getType() == IStoreCell.TYPE_NUMBER)
		{
			if(digit == 0) {
				field.setDataType(DataTypeConstant.INT);
			} else {
//				field.setDataType(DataTypeConstant.BIGDECIMAL);
				field.setDataType(DataTypeConstant.DOUBLE);
			}
		} else if(storeCell.getType() == IStoreCell.TYPE_BIGDECIMAL){
			field.setDataType(DataTypeConstant.BIGDECIMAL);
		}  else
		{
			field.setDataType(DataTypeConstant.STRING);
		}
		field.setPrecision(storeCell.getLen());
		if(digit == -1) {
			digit = 8;
		}
		field.setScale(digit);
		if(fldName!=null){
			field.setFldname(fldName);
		}else{
			field.setFldname(DynFmtProvider.STORECELL_PREFIX + storeIndex);
		}
		
		field.setCaption(storeCell.toString() + "(" + field.getFldname() + ")");
		return field;
	}

	/**
	 * 将指标转为字段
	 * 
	 * @create by wanyonga at 2010-9-1,上午08:49:20
	 * 
	 * @param measure
	 * @param measureIndex
	 * @return
	 */
	private Field convertMeasureToField(MeasureVO measure, int measureIndex, int digit,String fldName)
	{
		Field field = new Field();
		if (measure.getType() == IStoreCell.TYPE_NUMBER)
		{
//			field.setDataType(DataTypeConstant.BIGDECIMAL);
			field.setDataType(DataTypeConstant.DOUBLE);
		} else if(measure.getType() == IStoreCell.TYPE_BIGDECIMAL){
			field.setDataType(DataTypeConstant.BIGDECIMAL);
		} else
		{
			field.setDataType(DataTypeConstant.STRING);
		}
		field.setPrecision(measure.getLen());
		if(digit == -1) {
			digit = 8;
		}
		field.setScale(digit);
		if(fldName!=null){
			field.setFldname(fldName);
		}else{
			field.setFldname(DynFmtProvider.MEASURE_PREFIX + measureIndex);
		}
		
		field.setCaption(measure.toString() + "(" + field.getFldname() + ")");
		return field;
	}

	/**
	 * 将FormulaVO转为元数据字段
	 * 
	 * @create by wanyonga at 2010-7-15,上午10:26:33
	 * 
	 * @param formula
	 * @param fmtPos
	 * @return
	 */
	private Field convertFormulaVOToField(FormulaVO formula, String fmtPos,
			int forIndex)
	{
		Field field = new Field();
		field.setDataType(DataTypeConstant.STRING);
		field.setFldname(DynFmtProvider.FORMULA_PREFIX + forIndex);
		field.setCaption(fmtPos + "(" + field.getFldname() + ")");
		field.setPrecision(64);
		field.setNote(formula.getFormulaContent());
		return field;
	}

	/**
	 * 将alone转为元数据字段
	 * 
	 * @create by wanyonga at 2010-6-18,上午09:38:09
	 * 
	 * @return
	 */
	private Field convertAloneIDToField()
	{
		Field field = new Field();
		field.setCaption(ExtendAreaConstants.DYNAREA_FIELD_ALONEID);
		field.setDataType(DataTypeConstant.STRING);
		field.setFldname(ExtendAreaConstants.DYNAREA_FIELD_ALONEID);
		field.setPrecision(ExtendAreaConstants.DYNAREA_FIELD_ALONEID_LENGTH);
		field.setNote(ExtendAreaConstants.DYNAREA_FIELD_ALONEID);
		return field;
	}

	/**
	 * 将参照关键字转为字段
	 * 
	 * @param key
	 * @return
	 */
	private Field[] convertRefKeyVOToField(KeyVO key, int keyIndex)
	{
		KeywordModel keywordModel = model.getKeywordModel();
	
		Field[] flds = new Field[3];
		Field field = null;
		for (int i = 0; i < flds.length; i++)
		{
			field = new Field();
			field.setDataType(DataTypeConstant.STRING);
			// @edit by ll at 2010-4-16,下午02:33:14
			// weixl要求统一设置成64
			field.setPrecision(64);
			field.setNote(key.getNote());
			flds[i] = field;
		}
		
		KeySmartMapping mapping = keywordModel.getKeySmartMapping(key.getPk_keyword());
		
		if(mapping != null) {// 关键字可能没有映射关系 add by yuyangi
		flds[0].setFldname(mapping.getId() + "_map");
		
		if(fieldToRefFieldMap == null) {
			fieldToRefFieldMap = new Hashtable<String, String>();
		}
		fieldToRefFieldMap.put(flds[0].getFldname(), flds[0].getFldname());
		flds[1].setFldname(mapping.getName());
		fieldToRefFieldMap.put(flds[1].getFldname(), flds[1].getFldname());
		flds[2].setFldname(mapping.getCode());
		fieldToRefFieldMap.put(flds[2].getFldname(), flds[2].getFldname());
		
	}else{
		flds[0].setFldname(DynFmtProvider.KEY_PREFIX + keyIndex);
		flds[1].setFldname(DynFmtProvider.KEY_PREFIX + keyIndex + DynFmtProvider.NAME_SUFFIEX);
		flds[2].setFldname(DynFmtProvider.KEY_PREFIX + keyIndex + DynFmtProvider.CODE_SUFFIEX);
	}
		flds[0].setDataType(DataTypeConstant.REF);
		flds[0].setCaption(key.getName() + "(" + flds[0].getFldname() + ")");
		flds[1].setCaption(key.getName() + "(" + flds[1].getFldname() + ")");
		flds[2].setCaption(key.getName() + "(" + flds[2].getFldname() + ")");
		return flds;
	}

	// 判断是否参照关键字
	public static boolean isRefKey(KeyVO key)
	{
		if (key.getRef_pk() != null
				|| KeyVO.CORP_PK.equals(key.getPk_keyword())
				|| KeyVO.DIC_CORP_PK.equals(key.getPk_keyword()))
		{
			return true;
		}
		return false;
	}

	// 设置参照关键字的值
	private void setRefKeyData(Object[] rowDatas, KeyVO key,
			MeasurePubDataVO measurePubDataVO)
	{
		KeywordModel keywordModel = model.getKeywordModel();
		KeySmartMapping mapping = keywordModel.getKeySmartMapping(key.getPk_keyword());
		if(mapping != null){
		int i = 0;
		i = getIndex(mapping.getId() + "_map");
		IKeyDetailData keyData = measurePubDataVO.getKeyDataByPK(key.getPk_keyword()); 
		rowDatas[i] =  keyData.getValue();
		rowDatas[i + 1 ] = keyData.getName1();
		rowDatas[i + 2] = keyData.getCode();
		}else{
			int i = 0;
			i = getIndex(getFldNameByMetaData(key.getPk_keyword()));
			rowDatas[i] = measurePubDataVO.getKeyDataByPK(key.getPk_keyword())
					.getValue();
			
			rowDatas[i + 1] = measurePubDataVO.getKeyDataByPK(key.getPk_keyword())
					.getName1();
		
			rowDatas[i + 2] = measurePubDataVO.getKeyDataByPK(key.getPk_keyword())
					.getCode();
			
		}
	}

	/**
	 * 获得元数据字段名对应的关键字或存储单元
	 * 
	 * @create by wanyonga at 2010-6-18,上午09:51:14
	 * 
	 * @param fldName
	 * @return
	 */
	public Object getVo(String fldName)
	{
		return this.fldRefVo.get(fldName);
	}

	public int getIndex(String fieldName)
	{
		return getMetaData().getIndex(fieldName);
	}

	/**
	 * 设置二维数据
	 */
	public void setDatas(Object[][] datas)
	{
		this.datas = datas;
	}

	/**
	 * 动态区关键字组合
	 * 
	 * @create by wanyonga at 2010-6-18,上午09:54:56
	 * 
	 * @return
	 */
	public String getKeyCombPK()
	{
		return model.getKeyCompPK(exCellPK);
	}

	/**
	 * 当前动态区显示数据对应的数据集
	 * 
	 * @create by wanyonga at 2010-6-18,上午09:55:17
	 * 
	 * @return
	 */
	public DataSet getDisplayDataSet()
	{
		return displayDataSet;
	}

	/**
	 * 设置动态区当前显示数据对应的数据集
	 * 
	 * @create by wanyonga at 2010-6-18,上午09:56:59
	 * 
	 * @param dataSet
	 */
	public void setDisplayDataSet(DataSet dataSet)
	{
		this.displayDataSet = dataSet;
	}

	/**
	 * 重新设置提供者的数据，需要用当前数据和提供者的数据进行合并
	 * 
	 * @create by wanyonga at 2010-6-18,上午09:57:28
	 * 
	 * @param isSave
	 *            是否保存
	 */
	public void reLoadData(boolean isSave)
	{
		if (isDirty)
		{
			String aloneID = null;
			// liuchun 20110609 修改，将二维数组进行克隆，之后对其循环处理，在循环处理过程中会改变原数组的值
			Object[][] curDisDatas = getDisplayDataSet().getDatas().clone();
			// end
			List<Object[]> addList = new ArrayList<Object[]>();
			Integer row = 0;
			int iAloneIDIndex = getMetaData().getIndex(ExtendAreaConstants.DYNAREA_FIELD_ALONEID);
			for (Object[] rowDatas : curDisDatas)
			{
				// @edit by wuyongc at 2013-11-25,上午10:52:21 因为ReportSmartQueryUtil 中providerDataSet方法 line 200插入了空的数据引起。
				if(rowDatas == null)
					continue;
				aloneID = (String) rowDatas[iAloneIDIndex];
				if (aloneID == null)
				{
					// 新增
					rowDatas[iAloneIDIndex] = IDMaker.makeID(12);
					addList.add(rowDatas);
				} else
				{
					// 修改或不修改的都按新的替换
					row = aloneIDToRow.get(aloneID);
					if(row != null)
						datas[row] = rowDatas;
				}
			}
			int len = datas.length + addList.size() - deleteAloneList.size();
			Object[][] newDatas = new Object[len][];
			int index = 0;
			for (Object[] oldRow : datas)
			{
				aloneID = (String) oldRow[oldRow.length - 1];
				if (!deleteAloneList.contains(aloneID))
				{
					newDatas[index] = oldRow;
					index++;
				}
			}
			for (Object[] newRow : addList)
			{
				newDatas[index] = newRow;
				index++;
			}
			datas = newDatas;
			deleteAloneList.clear();
			aloneIDToRow.clear();
			for (int i = 0; i < datas.length; i++)
			{
				if (datas[i][datas[i].length - 1] != null)
				{
					aloneIDToRow.put((String) datas[i][datas[i].length - 1], i);
				}
			}
			setDirty(false);
		}
		if (!isSave)
		{
			displayDataSet = null;
		}
	}

	/**
	 * 记录删除的组数据的aloneid
	 * 
	 * @create by wanyonga at 2010-6-18,上午09:59:48
	 * 
	 * @param aloneID
	 */
	public void addDeleteAloneID(String aloneID)
	{
		if (aloneID != null && !deleteAloneList.contains(aloneID))
		{
			deleteAloneList.add(aloneID);
		}
	}

	/**
	 * 设置动态区数据是否变化
	 * 
	 * @create by wanyonga at 2010-6-18,上午10:00:11
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty)
	{
		this.isDirty = isDirty;
	}

	/**
	 * 获得关键字对应的列索引
	 * 
	 * @create by wanyonga at 2010-4-20,下午08:10:53
	 * 
	 * @return
	 */
	public int[] getKeywordIndex()
	{
		return keyIndex;
	}

	/**
	 * 获得动态区PK
	 * 
	 * @create by wanyonga at 2010-6-18,上午10:00:45
	 * 
	 * @return
	 */
	public String getExCellPK()
	{
		return exCellPK;
	}

	/**
	 * 获得动态区格式定义时引用的语义模型对应的数据集
	 * 
	 * @create by wanyonga at 2010-6-12,下午02:22:37
	 * 
	 * @return
	 */
	public DataSet getRefDataSet()
	{
		DataSet refDs = null;
		if (smartRefId != null)
		{
			try
			{
				List<Descriptor> descs = new ArrayList<Descriptor>();
				if (formatDefFilters != null && formatDefFilters.length > 0)
				{
					for (FilterDescriptor fmtDesc : formatDefFilters)
					{
						if (fmtDesc != null && fmtDesc.getFilterItemCount() > 0)
						{
							descs.add(fmtDesc);
						}
					}
				}
				
				SmartModel smtModel=SmartModelCache.getInstance().getModel(smartRefId);
				if(smtModel == null){
					return refDs;	
				}
				// 当字段不仅仅在关键字位置上时，需要校验权限
				if(!isOnlyKeyRefField() && validator != null) {
//					Descriptor[] orgFilterDesc = validator.getOrgFilterDescs(false);
					SmartDefVO defVO = SmartDefCache.getInstance().getDef(smartRefId);
					Descriptor[] orgFilterDesc = ReportSmartQueryUtil.getFilterDescByMngModel(smtModel, defVO, validator);
					if(orgFilterDesc != null) {
						descs.addAll(Arrays.asList(orgFilterDesc));
					}
				}
				
				SmartContext smtContext=(SmartContext)getRefContext();
				//tianchuan ++ 支持宏变量字段，对上下文进行特殊处理
				if(mainPubData != null) {
					smtContext.setAttribute(SmartContext.KEY_ORG_PK, mainPubData.getUnitPK());
				}
				smtContext=AreaDescUtil.setMacroVars(smtModel,smtContext);
				
				refDs = smtModel.provideData(smtContext,descs.toArray(new Descriptor[0]));

			} catch (SmartException e)
			{
				throw new IllegalArgumentException("smart" + smartRefId
						+ "is not exist.");
			}
		}
		return refDs;
	}
	
	/**
	 * 是否仅有关键字位置上拖拽了语义模型字段
	 * 
	 * @return
	 */
	private boolean isOnlyKeyRefField() {
		Hashtable<Integer, Integer> fieldToRefMap = getFieldToRefFieldIndexMap();
		if(fieldToRefMap != null && !fieldToRefMap.isEmpty()) {
			Set<Integer> refIndex = fieldToRefMap.keySet();
			int[] keyIndex = getKeywordIndex();
			Iterator<Integer> it = refIndex.iterator();
			while(it.hasNext()) {
				Integer fieldIndex = it.next();
				boolean beFind = false;
				for(int index : keyIndex) {
					if(fieldIndex != null && fieldIndex.intValue() == index) {
						beFind = true;
						break;
					}
				}
				if(beFind) {
					continue;
				} else {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 获得动态区引用的语义模型执行的上下文
	 * 
	 * @create by wanyonga at 2010-9-2,下午08:25:13
	 * 
	 * @return
	 * @throws SmartException 
	 */
	public IContext getRefContext() throws SmartException
	{
		SmartContext context = new SmartContext();
		try {
			if (params != null && params.length > 0)
			{
				for (Parameter param : params)
				{
					if (param != null)
					{
						Parameter[] params = (Parameter[])ExtendAreaModel.getInstance(model.getCellsModel()).getRepLevelInfo(ExtendAreaConstants.REPORT_PARAMS);
						Object value = param.getValue();
						if(params != null && params.length > 0) {
							Parameter tempParam = getParamByCode(params, param.getCode());
							if(tempParam != null) {
								value = tempParam.getValue();
							}
						}
						if(value == null || value.toString().length() == 0) {
							continue;
						}
						context.setParameterValue(param, value);
					}
				}
			}
			
			// 将主表关键字的值作为参数值放入上下文中
			if(mainKeyParams != null && mainKeyParams.size() > 0) {
				for(Parameter param : mainKeyParams) {
					if (param != null) {
						context.setParameterValue(param, param.getValue());
					}
				}
			}
			
			initKeyValue(context);
			
		} catch(Exception e) {
			throw new SmartException(e);
		}
		
		return context;
	}
	
	private void initKeyValue(SmartContext context) throws InvalidAccperiodExcetion {
		if(mainPubData != null) {
			KeyVO timeKey = mainPubData.getKeyGroup().getTTimeKey();
			if(timeKey != null && timeKey.isAccPeriodKey()) {
				String curOrgPk = mainPubData.getUnitPK();
				String[] accFromTo = AccPeriodSchemeUtil.getAccFromToMonth(timeKey, mainPubData);
				context.setAttribute(IUfoContextKey.CUR_REPORG_PK, curOrgPk);
				context.setAttribute(IUfoContextKey.KEY_GLSMT_ACC_FROM, accFromTo[0]);
				context.setAttribute(IUfoContextKey.KEY_GLSMT_ACC_TO, accFromTo[1]);
			}
		}
	}

	private Parameter getParamByCode(Parameter[] params, String code) {
		Parameter p = null;
		if(params == null || params.length == 0) {
			return p;
		}
		for(Parameter param : params) {
			if(param.getCode().equals(code)) {
				p = param;
				break;
			}
		}
		return p;
	}
	
	@SuppressWarnings("unused")
	private ExtendAreaCell getDynCells() {
		if(dynCell != null) {
			return dynCell;
		}
		return ExtendAreaModel.getInstance(model.getCellsModel()).getExAreaByPK(exCellPK);
	}
	
	/**
	 * 获得格式定义字段和录入状态由关键字和指标构建字段的映射关系
	 * 
	 * @create by wanyonga at 2010-6-12,下午03:26:24
	 * 
	 * @return
	 */
	public Hashtable<Integer, Integer> getFieldToRefFieldIndexMap()
	{
		return fieldToRefFieldIndexMap;
	}

	/**
	 * 获得由关键字、指标、存储单元、公式构建的语义模型的ID
	 * 
	 * @create by wanyonga at 2010-8-31,下午04:29:54
	 * 
	 * @return
	 */
	private String getSmartDefId()
	{
		return ExtendAreaConstants.DYNAREA_SMARTID_PREFIX + exCellPK;
	}

	/**
	 * 获得元数据(关键字、指标、存储单元、公式)对应字段的字段名
	 * 
	 * @create by wanyonga at 2010-9-1,上午09:16:01
	 * 
	 * @param metaData
	 * @return
	 */
	public String getFldNameByMetaData(String metaDataID)
	{
		return metaToFldNameMap.get(metaDataID);
	}

	@Override
	public String getCode()
	{
		String code = super.getCode();
		if (code == null)
		{
			ExtendAreaCell exCell = model.getExtendAreaCellByPK(this.exCellPK);
			if (exCell != null)
			{
				AreaPosition area = exCell.getArea();
				code = area.getStart().toString() + area.getEnd().toString();
				setCode(code);
			}
		}
		return super.getCode();
	}

	/**
	 * @param mainKeyParams the mainKeyParams to set
	 */
	public void setMainKeyParams(ArrayList<Parameter> mainKeyParams) {
		this.mainKeyParams = mainKeyParams;
	}

	/**
	 * @return the mainKeyParams
	 */
	public ArrayList<Parameter> getMainKeyParams() {
		return mainKeyParams;
	}
	/**
	 * 获得显示名
	 */
	public String getTitle() {
		return getCode();
	}

	public void setMainPubData(MeasurePubDataVO mainPubData) {
		this.mainPubData = mainPubData;
	}

	public MeasurePubDataVO getMainPubData() {
		return mainPubData;
	}

	public KeyVO[] getKeys() {
		if(keyVOs!=null){
			return keyVOs.toArray(new KeyVO[keyVOs.size()]);
		}
		return new KeyVO[0];
	}
	
	public DynamicAreaModel getModel(){
		return model;
	}

	public void setValidator(RepDataAuthValidator validator) {
		this.validator = validator;
	}

	public RepDataAuthValidator getValidator() {
		return validator;
	}
	
}
