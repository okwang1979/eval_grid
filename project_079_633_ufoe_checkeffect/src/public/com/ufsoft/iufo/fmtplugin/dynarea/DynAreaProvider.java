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
 * ��̬�������ṩ�ߣ����ݶ�̬������Ĺؼ��ֺʹ洢��Ԫ����Ԫ���ݡ�
 * 
 * @author wanyonga
 * 
 */
public class DynAreaProvider extends SemanticDataProvider
{
	private static final long serialVersionUID = 2581800231680956521L;
	
	public static String TIMEFIELD_CAPTION = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1155")/*@res "����"*/;
	/**
	 * ��̬��PK
	 */
	private String exCellPK;
	/**
	 * ��̬������ģ��
	 */
	private DynamicAreaModel model;
	/**
	 * ����ԭʼ����
	 */
	private transient RepDataVO repData;
	/**
	 * ��̬��ģ��
	 */
	private transient ExtendAreaCell dynCell;
	/**
	 * ��ά����
	 */
	private Object[][] datas;
	/**
	 * ��̬���ؼ����б�
	 */
	private transient List<KeyVO> keys = new ArrayList<KeyVO>();
	
	/**
	 * Ԫ�����ֶμ���Ӧ�Ĵ洢��Ԫ�͹ؼ��ֵ�ӳ��
	 */
	private Hashtable<String, Object> fldRefVo = new Hashtable<String, Object>();
	/**** key(�ؼ��֡�ָ�ꡢ�洢��Ԫ����ʽ)��ʶ------value(��Ӧ�ֶ���) *****/
	private Hashtable<String, String> metaToFldNameMap = new Hashtable<String, String>();
	/**
	 *��̬����ǰ��ʾ���ݶ�Ӧ�����ݼ�
	 */
	private DataSet displayDataSet;
	/**
	 * aloneid��ԭʼ���ݼ��кŵĶ�Ӧ��ϵ
	 */
	private Hashtable<String, Integer> aloneIDToRow = new Hashtable<String, Integer>();
	/**
	 * ɾ�������ݼ��е�aloneIdList
	 */
	private List<String> deleteAloneList = new ArrayList<String>();
	/**
	 * �Ƿ�����ṩ�ߵ�����
	 */
	private boolean isDirty = false;
	/**
	 * �ؼ�����Ԫ�����е�����
	 */
	private int[] keyIndex = null;
	/**
	 * ͬһ��Ԫλ����¼��̬�ֶκ͸�ʽ�����ֶ����ֵ�ӳ���ϵ
	 */
	private transient Hashtable<String, String> fieldToRefFieldMap = null;
	/**
	 * ͬһ��Ԫλ����¼��̬�ֶκ͸�ʽ�����ֶ�������ӳ���ϵ
	 */
	private Hashtable<Integer, Integer> fieldToRefFieldIndexMap = null;
	/**
	 * �ɼ����ʽ����ʱ��̬�����õ�����ģ�͵Ķ���ID
	 */
	private String smartRefId;
	/**
	 * �ɼ���̬����������ģ�͸�ʽ����ʱ�����ɸѡ����
	 */
	private FilterDescriptor[] formatDefFilters;
	/**
	 * �ɼ����ʽ���ʱ��̬��(�ؼ��֡�ָ�ꡢ�洢��Ԫ)�ϵ������������
	 */
	private transient KMSortDescMng kmSortDescMng;
	/**
	 * �ɼ����ʽ���ʱ��̬���������ã�ֻӰ�쵱ʼ�������ݵ���ʾ
	 */
	private transient SortDescriptor sortDescriptor;
	/**
	 * �ɼ����ʽ���ʱ������Ͳɼ���¼��ʱ�ֶε�ӳ���ϵ
	 */
	private transient Hashtable<SortItem, Field> sortItemToFieldMap = null;
	/**
	 * �ɼ���̬����������ģ�Ͷ���Ĳ�����ֵ
	 */
	private Parameter[] params;
	
	// ����ؼ������ɵĲ�����ֵ,ɸѡ������
	private ArrayList<Parameter> mainKeyParams;
	
	private MeasurePubDataVO mainPubData = null;
	
	//�ڹ�ʽ׷��ʱ��Ϊ�˱�֤�ؼ���˳���ʵ�ʵ�˳��һ��
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
		return new DataSet(metaData, displayDataSet.getData2Array());// ��displayDataSet�е�չʾ����
	}

	@Override
	public MetaData provideMetaData(SmartContext context)
	{
		return getMetaData();
	}

	/**
	 * ����̬����ʽ���ʱ����Ĺ̶���Ա��ӵ���ʽ�����ܵ�ɸѡ��������
	 * 
	 * @create by wanyonga at 2010-7-19,����10:09:06
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
	 * ����̬����ʽ���ʱ����ֵɸѡ����չ������ɸѡ������ɸѡ��ӵ���ʽ�����ܵ�ɸѡ��������
	 * 
	 * @create by wanyonga at 2010-7-19,����10:15:04
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
	 * �����̬����ʽ̬�ķ�������
	 * 
	 * @create by wanyonga at 2010-7-19,����11:15:28
	 * 
	 * @param exCell
	 */
	private void clearFormatAnaSet(ExtendAreaCell exCell)
	{
		// ��������ģ��
		if (exCell.getAreaInfoSet().getSmartModelDefID() != null)
		{
			exCell.getAreaInfoSet().setSmartModelDefID(null);
		}
		// ֵɸѡ
		if (exCell.getAreaInfoSet().getValueFilter() != null)
		{
			exCell.getAreaInfoSet().setValueFilter(null);
		}
		// ��չ����ɸѡ
		FilterDescriptor areaFilter = exCell.getAreaInfoSet().getAreaFilter();
		if (areaFilter != null)
		{
			exCell.getAreaInfoSet().setAreaFilter(null);
		}
		// ����ɸѡ
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
		// ����
//		exCell.getAreaInfoSet().clearChangedParams();
	}

	/**
	 * ����̬����ʽ̬�ؼ��֡�ָ�ꡢ�洢��Ԫ������������ӵ�������
	 * 
	 * @create by wanyonga at 2010-8-30,����09:53:13
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
					// ���û������˳��ָ�������ֶ�˳��
					if (sortItemToFieldMap == null)
					{
						sortItemToFieldMap = new Hashtable<SortItem, Field>();
					}
					sortItemToFieldMap.put(item, fld);
				} else
				{
					// ������˳��
					SortItem newItem = new SortItem(fld, item.isDescending());
					sortDescriptor.addSort(newItem);
				}
			}
		}
	}

	/**
	 * ��òɼ����ʽ���ʱ�Թؼ��֡�ָ�ꡢ�洢��Ԫ������������
	 * 
	 * @create by wanyonga at 2010-8-30,����10:45:20
	 * 
	 * @return
	 */
	public SortDescriptor getFormatSortDescriptor()
	{
		return sortDescriptor;
	}

	/**
	 *����ʽ���ʱ������������ת�ɲɼ���¼��ʱ��Ӧ�ֶκ�˳�������������
	 * 
	 * @create by wanyonga at 2010-8-30,����10:20:59
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
	 * ���ݶ�̬������ؼ��֡�ָ�ꡢ�洢��Ԫ�͹�ʽ����Ԫ����
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
					// �������͹ؼ���
					Field[] flds = convertRefKeyVOToField(key, keyIndex);
					for(Field f : flds) {
//						if(!f.getFldname().equals(key)) {
							refflds.add(f);	
//						}
					}
					if(flds != null) {// TODO ����û��ӳ���ֶ� add by yuyangi
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
						// �ڷ�ָ���ؼ���λ���϶���Ĺ�ʽҲ��Ϊ����������ģ���ֶ�
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
					// ���ڲ��Ǵ洢��Ԫλ���ϵ��ֶ�
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
	 * �����ؼ��ֶ�Ӧ��Ԫ��������
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
	 * ����ͬһ��Ԫλ����¼��̬�ֶκ͸�ʽ�����ֶ����ֵ�ӳ���ϵ
	 * 
	 * @create by wanyonga at 2010-6-12,����03:23:26
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
	 * ����RepDataVO������ά����
	 */
	private void buildDatas(IContext context)
	{
		// TODO:MeasurePubDataVOά��ֵ��Ҫ���
		if (repData != null)
		{
			this.buildIndexs();
			this.buildFieldToRefFieldIndex();
			MeasureDataVO[][] mDataVos = repData.getMeasureDatasAsMatrix(model
					.getKeyCompPK(model.getExtendAreaCellByPK(exCellPK)));
			if (mDataVos == null || mDataVos.length == 0)
			{
				// update by wanyonga 2010-3-23 �޹ؼ���ָ�����ݣ�����ȫ��null��һ��
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
						} //tianchuan 2012.9.24 ����ָ��Ĵ���
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
						// �������͹ؼ���
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
	 * �˷����ڱ�����󣬽������ṩ���еĶ�̬��aloneid�������
	 * 
	 * @create by liuchuna at 2011-6-10,����02:45:10
	 *
	 * @param mDataVos
	 */
	public void fillAloneIdAfterSave(MeasurePubDataVO[] pubDatas, KeyVO[] keys) {
		if (pubDatas != null && pubDatas.length > 0) {
			// ��װ�ؼ��ֶ�Ӧ��pubDatas
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
				
				//������ÿ�йؼ��ֵ�ֵ�����aloneid
				for(int j = 0, n = datas.length; j < n; j++) {
					Object[] object =  datas[j];
					String combKeyValue = "";
					for (KeyVO key : keys) {
						
						int keyIndex;
						if (isRefKey(key)) {
							// �������͹ؼ���
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
	 * ���ؼ��ֶ���תΪ�ֶ�
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
		field.setPrecision(64);// @edit by ll at 2010-4-16,����02:33:14
		// weixlҪ��ͳһ���ó�64
		field.setNote(key.getNote());
		return field;
	}

	/**
	 * ��һ���洢��Ԫת��ΪԪ�����ֶ�
	 * 
	 * @create by wanyonga at 2010-6-18,����09:11:48
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
	 * ��ָ��תΪ�ֶ�
	 * 
	 * @create by wanyonga at 2010-9-1,����08:49:20
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
	 * ��FormulaVOתΪԪ�����ֶ�
	 * 
	 * @create by wanyonga at 2010-7-15,����10:26:33
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
	 * ��aloneתΪԪ�����ֶ�
	 * 
	 * @create by wanyonga at 2010-6-18,����09:38:09
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
	 * �����չؼ���תΪ�ֶ�
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
			// @edit by ll at 2010-4-16,����02:33:14
			// weixlҪ��ͳһ���ó�64
			field.setPrecision(64);
			field.setNote(key.getNote());
			flds[i] = field;
		}
		
		KeySmartMapping mapping = keywordModel.getKeySmartMapping(key.getPk_keyword());
		
		if(mapping != null) {// �ؼ��ֿ���û��ӳ���ϵ add by yuyangi
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

	// �ж��Ƿ���չؼ���
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

	// ���ò��չؼ��ֵ�ֵ
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
	 * ���Ԫ�����ֶ�����Ӧ�Ĺؼ��ֻ�洢��Ԫ
	 * 
	 * @create by wanyonga at 2010-6-18,����09:51:14
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
	 * ���ö�ά����
	 */
	public void setDatas(Object[][] datas)
	{
		this.datas = datas;
	}

	/**
	 * ��̬���ؼ������
	 * 
	 * @create by wanyonga at 2010-6-18,����09:54:56
	 * 
	 * @return
	 */
	public String getKeyCombPK()
	{
		return model.getKeyCompPK(exCellPK);
	}

	/**
	 * ��ǰ��̬����ʾ���ݶ�Ӧ�����ݼ�
	 * 
	 * @create by wanyonga at 2010-6-18,����09:55:17
	 * 
	 * @return
	 */
	public DataSet getDisplayDataSet()
	{
		return displayDataSet;
	}

	/**
	 * ���ö�̬����ǰ��ʾ���ݶ�Ӧ�����ݼ�
	 * 
	 * @create by wanyonga at 2010-6-18,����09:56:59
	 * 
	 * @param dataSet
	 */
	public void setDisplayDataSet(DataSet dataSet)
	{
		this.displayDataSet = dataSet;
	}

	/**
	 * ���������ṩ�ߵ����ݣ���Ҫ�õ�ǰ���ݺ��ṩ�ߵ����ݽ��кϲ�
	 * 
	 * @create by wanyonga at 2010-6-18,����09:57:28
	 * 
	 * @param isSave
	 *            �Ƿ񱣴�
	 */
	public void reLoadData(boolean isSave)
	{
		if (isDirty)
		{
			String aloneID = null;
			// liuchun 20110609 �޸ģ�����ά������п�¡��֮�����ѭ��������ѭ����������л�ı�ԭ�����ֵ
			Object[][] curDisDatas = getDisplayDataSet().getDatas().clone();
			// end
			List<Object[]> addList = new ArrayList<Object[]>();
			Integer row = 0;
			int iAloneIDIndex = getMetaData().getIndex(ExtendAreaConstants.DYNAREA_FIELD_ALONEID);
			for (Object[] rowDatas : curDisDatas)
			{
				// @edit by wuyongc at 2013-11-25,����10:52:21 ��ΪReportSmartQueryUtil ��providerDataSet���� line 200�����˿յ���������
				if(rowDatas == null)
					continue;
				aloneID = (String) rowDatas[iAloneIDIndex];
				if (aloneID == null)
				{
					// ����
					rowDatas[iAloneIDIndex] = IDMaker.makeID(12);
					addList.add(rowDatas);
				} else
				{
					// �޸Ļ��޸ĵĶ����µ��滻
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
	 * ��¼ɾ���������ݵ�aloneid
	 * 
	 * @create by wanyonga at 2010-6-18,����09:59:48
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
	 * ���ö�̬�������Ƿ�仯
	 * 
	 * @create by wanyonga at 2010-6-18,����10:00:11
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty)
	{
		this.isDirty = isDirty;
	}

	/**
	 * ��ùؼ��ֶ�Ӧ��������
	 * 
	 * @create by wanyonga at 2010-4-20,����08:10:53
	 * 
	 * @return
	 */
	public int[] getKeywordIndex()
	{
		return keyIndex;
	}

	/**
	 * ��ö�̬��PK
	 * 
	 * @create by wanyonga at 2010-6-18,����10:00:45
	 * 
	 * @return
	 */
	public String getExCellPK()
	{
		return exCellPK;
	}

	/**
	 * ��ö�̬����ʽ����ʱ���õ�����ģ�Ͷ�Ӧ�����ݼ�
	 * 
	 * @create by wanyonga at 2010-6-12,����02:22:37
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
				// ���ֶβ������ڹؼ���λ����ʱ����ҪУ��Ȩ��
				if(!isOnlyKeyRefField() && validator != null) {
//					Descriptor[] orgFilterDesc = validator.getOrgFilterDescs(false);
					SmartDefVO defVO = SmartDefCache.getInstance().getDef(smartRefId);
					Descriptor[] orgFilterDesc = ReportSmartQueryUtil.getFilterDescByMngModel(smtModel, defVO, validator);
					if(orgFilterDesc != null) {
						descs.addAll(Arrays.asList(orgFilterDesc));
					}
				}
				
				SmartContext smtContext=(SmartContext)getRefContext();
				//tianchuan ++ ֧�ֺ�����ֶΣ��������Ľ������⴦��
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
	 * �Ƿ���йؼ���λ������ק������ģ���ֶ�
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
	 * ��ö�̬�����õ�����ģ��ִ�е�������
	 * 
	 * @create by wanyonga at 2010-9-2,����08:25:13
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
			
			// ������ؼ��ֵ�ֵ��Ϊ����ֵ������������
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
	 * ��ø�ʽ�����ֶκ�¼��״̬�ɹؼ��ֺ�ָ�깹���ֶε�ӳ���ϵ
	 * 
	 * @create by wanyonga at 2010-6-12,����03:26:24
	 * 
	 * @return
	 */
	public Hashtable<Integer, Integer> getFieldToRefFieldIndexMap()
	{
		return fieldToRefFieldIndexMap;
	}

	/**
	 * ����ɹؼ��֡�ָ�ꡢ�洢��Ԫ����ʽ����������ģ�͵�ID
	 * 
	 * @create by wanyonga at 2010-8-31,����04:29:54
	 * 
	 * @return
	 */
	private String getSmartDefId()
	{
		return ExtendAreaConstants.DYNAREA_SMARTID_PREFIX + exCellPK;
	}

	/**
	 * ���Ԫ����(�ؼ��֡�ָ�ꡢ�洢��Ԫ����ʽ)��Ӧ�ֶε��ֶ���
	 * 
	 * @create by wanyonga at 2010-9-1,����09:16:01
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
	 * �����ʾ��
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
