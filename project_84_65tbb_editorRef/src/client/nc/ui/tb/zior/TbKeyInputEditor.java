package nc.ui.tb.zior;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;

import nc.itf.mdm.dim.IDimManager;
import nc.itf.tb.cell.ICellInfoChangedListener;
import nc.ms.mdm.convertor.IStringConvertor;
import nc.ms.mdm.convertor.StringConvertorFactory;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.tb.editor.TbVarCellEitor;
import nc.ui.tb.editor.TbVarDocCellEitor;
import nc.ui.tb.zior.pluginaction.edit.model.VarCellValueModel;
import nc.view.tb.form.iufo.StringEditor;
import nc.view.tb.form.iufo.TbDefaultSheetCellRender;
import nc.vo.mdm.cube.DimSectionTuple;
import nc.vo.mdm.cube.DimVectorPropUtil;
import nc.vo.mdm.dim.DimDataType;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimDefPKConst;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.tb.form.excel.ExVarAreaDef;
import nc.vo.tb.form.excel.ExVarDef;
import nc.vo.tb.form.iufo.CellExtInfo;
import nc.vo.tb.form.iufo.TbIufoConst;
import nc.vo.tb.task.MdTask;

import com.ufida.iufo.table.exarea.ExtendAreaCell;
import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.Cell;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.CellsPane;
import com.ufsoft.table.IAreaAtt;
import com.ufsoft.table.format.IFormat;
import com.ufsoft.table.format.INumberFormat;
import com.ufsoft.table.re.DateEditor;
import com.ufsoft.table.re.DefaultSheetCellEditor;
import com.ufsoft.table.re.GenericEditor;
import com.ufsoft.table.re.ReadOnlyEditor;
import com.ufsoft.table.re.SheetCellEditor;

public class TbKeyInputEditor extends DefaultSheetCellEditor {
	public final static int DIR_RIGHT = 0;
	public final static int DIR_LEFT = 1;
	public final static int DIR_DOWN = 2;
	public final static int DIR_UP = 3;
	private SheetCellEditor proxy;
	private int decimaldigit = -2;
	private IFormat format;
	private Component comp;
	private Object value ;
	/**
	 * @param table
	 * @param value
	 * @param isSelected
	 * @param row
	 * @param column
	 * @return
	 * @see com.ufsoft.table.re.SheetCellEditor#getTableCellEditorComponent(com.ufsoft.table.CellsPane,
	 *      java.lang.Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(final CellsPane table,
			Object value, boolean isSelected, int row, int column) {
		this.value = value;
		boolean canWrite = table.getDataModel().getTableSetting().getCellsAuth().isWritable(row, column);
		
		if(!canWrite){
			return super.getTableCellEditorComponent(table, this.value, isSelected, row, column);
		}
		
		format = table.getPaintModel().getRealFormat(
				CellPosition.getInstance(row, column));
		// 去掉复制的虚线框
		TbDefaultSheetCellRender.stopPlay(table);
		if (format != null && (format.getDataFormat() instanceof INumberFormat)) {
			INumberFormat dataFormat = (INumberFormat) format.getDataFormat();
			decimaldigit = dataFormat.getDecimalDigits();
		}
		final Cell cell = table.getCell(row, column);
		final CellExtInfo cInfo = (CellExtInfo) cell
				.getExtFmt(TbIufoConst.tbKey);
		//编辑标示
		cInfo.setDirty(true);
		int cellCount = -1;
		//单元格编辑器
		if (cInfo.getExVarAreaDef() != null) {
			//浮动
			if (cInfo.getExVarAreaDef().varDefList != null
					&& !cInfo.getExVarAreaDef().varDefList.isEmpty()) {
				int i = cInfo.getIndex();
				cellCount++;
				ExVarDef exVarDef = null;
				if (cInfo.getExVarAreaDef().varDefList.size() - 1 >= i) {
					for (ExVarDef def : cInfo.getExVarAreaDef().varDefList) {
						if (def.index == i) {
							exVarDef = def;
							break;
						}
					}
				}
				proxy = getVarSheetCellEditor(exVarDef, cInfo, table, row, column);
			}
		}else if (cInfo.getFdAreaId() != null) {
			//非多维
			proxy = getFdAreaSheetCellEditor();
		} else {
			//多维区
			proxy = getAreaSheetCellEditor(table, cell, cInfo);
		}
		
		if(proxy == null){
			proxy = getDefaultSheetCellEditor();
		}
		
		comp = proxy.getTableCellEditorComponent(table, this.value, isSelected, row,
				column);
		if (comp instanceof JTextField) {
			((JTextField)comp).addActionListener(new EditorDelegate() {
				private static final long serialVersionUID = 6234181259639446513L;

				public void setValue(Object newValue) {
					((JTextField) comp)
							.setText((newValue != null) ? newValue.toString()
									: "");
				}

				public void actionPerformed(ActionEvent e) {
					// 编辑事件结束的时候会移除编辑组件
					CellsPane cp = null;
					if (comp != null && comp.getParent() instanceof CellsPane) {
						cp = (CellsPane) comp.getParent();
					}
					if (cp == null) {
						return;
					}
					ITbRepDataEditor editor = (ITbRepDataEditor) SwingUtilities
							.getAncestorOfClass(ITbRepDataEditor.class, cp);
					if (editor == null) {
						return;
					}
//					stopCellEditing();
				int inputDir = DIR_DOWN;
					TbPlanContext context = editor.getTbPlanContext();
					if (context == null) {
						return;
					}

					inputDir = context.getInputDir();

					int dx = 0;
					int dy = 0;
					if (inputDir == DIR_DOWN)
						dy = 1;
					else if (inputDir == DIR_UP)
						dy = -1;
					else if (inputDir == DIR_RIGHT)
						dx = 1;
					else
						dx = -1;

					TbRepDataKeyActionHelp.doNavigate(cp, dx, dy, true);
				}

				public Object getCellEditorValue() {
					return ((JTextField) editorComponent).getText();
				}
			});
		} else if (comp instanceof UIRefPane) {
			final UIRefPane refPane = (UIRefPane) comp;
			if(this.value != null){
				refPane.setText(""+this.value);
			}
		}
		return comp;
	}
	
	//浮动编辑器
	private SheetCellEditor getVarSheetCellEditor(ExVarDef exVarDef, final CellExtInfo cInfo,final CellsPane table,
			 int row, int column){
		if(exVarDef == null){
			return null;
		}
		
		final Cell cell = table.getCell(row, column);
		
		if (exVarDef.cellType == ExVarDef.cellType_fd_str) {
			if(exVarDef.dimLevelCode != null && !exVarDef.dimLevelCode.trim().equals("")){
				ITbRepDataEditor editor = (ITbRepDataEditor) SwingUtilities
				.getAncestorOfClass(ITbRepDataEditor.class, table);
				if (editor == null) {
					return null;
				}
				String uapRefName = TbVarAreaUtil.getUapRefName("" + exVarDef.dimLevelCode);
				if(uapRefName != null){
					proxy = new TbVarDocCellEitor(uapRefName);
				}
				
			}else{
				proxy = new StringEditor();
			}
		//只读与序号使用ReadOnlyEditor，不让编辑
		} else if (exVarDef.cellType == ExVarDef.cellType_readonly  
				|| exVarDef.cellType == ExVarDef.cellType_index) {
			proxy = new ReadOnlyEditor(
					new com.ufsoft.table.beans.UFOLabel());
		} else if (exVarDef.cellType == ExVarDef.cellType_none
				|| exVarDef.cellType == ExVarDef.cellType_datacell
				|| exVarDef.cellType == ExVarDef.cellType_fd_number
				|| exVarDef.cellType == ExVarDef.cellType_auto_expand
				|| exVarDef.cellType == ExVarDef.cellType_fd_count
		) {
			// 未定义、多维数据、数字
			if(cInfo.getDimVector() != null){
				proxy = getAreaSheetCellEditor(table, cell, cInfo);
			 }else{
				 proxy = new TbDoubleEditor();
			 }
		//日期类型
		} else if (exVarDef.cellType == ExVarDef.cellType_fd_time) {
			proxy = new DateEditor();
//			if(this.value == null || this.value.equals("")){
//				this.value = new UFDate().toStdString(TimeZone.getDefault());
//			}
			setDate();
		}else if(exVarDef.cellType == ExVarDef.cellType_strlist){
			proxy = new TbComboboxEditor(exVarDef.levelValueList);
		}else if(exVarDef.cellType == ExVarDef.cellType_dim ){
			ITbRepDataEditor editor = (ITbRepDataEditor) SwingUtilities
			.getAncestorOfClass(ITbRepDataEditor.class, table);
			if (editor == null) {
				return null;
			}
			TbPlanContext context = editor.getTbPlanContext();
			MdTask task = context.getTasks()[0];
			//对于客户、供应商、物料这类档案需要过滤，如分类选了A,则参照要显示A分类下的内容
			String parentKey = TbVarAreaUtil.getDimParentKey(row,column,table,cInfo.getExVarAreaDef(),exVarDef);
			List<String> levelKey = TbVarAreaUtil.getDimSelectedKey(row, column, table, cInfo.getExVarAreaDef(), exVarDef);
			Map<DimLevel, LevelValue> dvMap = TbVarAreaUtil.getDVMap(cell, cInfo, exVarDef, table);
			

			proxy = new TbVarCellEitor(getDlList(table,
					exVarDef.dimLevelCode), exVarDef,
					cInfo.getCubeCode(),
					new ICellInfoChangedListener() {

						@Override
						public void cellDataChanged(Object obj) {
							if (obj instanceof DimMember) {
								cellVarDataChanged(obj,cInfo,cell,table);
							}
						}
					},task,cInfo.getLevelMap()/*cInfo.getDimSectionTuple() == null? null:cInfo.getDimSectionTuple().getLevelValues()*/);
			((TbVarCellEitor)proxy).setParentKey(parentKey);
			((TbVarCellEitor)proxy).setLevelValueList(levelKey);
			((TbVarCellEitor)proxy).setDvMap(dvMap);
		}
		return proxy;
	}
	
	private void setDate(){
		if(this.value == null || this.value.equals("")){
			this.value = new UFDate().toStdString(TimeZone.getDefault());
		}else{
			try{
				this.value = new UFDate(this.value.toString());
			}catch(Exception e){
				this.value = new UFDate().toStdString(TimeZone.getDefault());
			}
		}
	}
	
	//非多维编辑器
	private SheetCellEditor getFdAreaSheetCellEditor(){
		SheetCellEditor proxy = new StringEditor(); 
		return proxy;
	}
	
	//多维编辑器
	private SheetCellEditor getAreaSheetCellEditor(final CellsPane table,Cell cell,CellExtInfo cInfo){
		SheetCellEditor proxy = null;
		if (isHiddenRow(table, cell)){
			proxy = new ReadOnlyEditor(
					new com.ufsoft.table.beans.UFOLabel());
		}else{
			if(cInfo.getDimVector() != null){
				int vectorType = cInfo.getDimVector().getDimDataType();
				//多维数据时间编辑器
				if(DimDataType.DATE == vectorType){
					proxy = new DateEditor();
//					if(this.value == null || this.value.equals("")){
//						this.value = new UFDate().toStdString(TimeZone.getDefault());
//					}
					setDate();
				}else if(DimDataType.ENUM_TXT == vectorType){
					DimDataType dimDataType = DimVectorPropUtil.getDimDataType( cInfo.getDimVector());
					String[] enumValues = dimDataType.getEnumValues();
					if(enumValues != null){
						proxy = new TbComboboxEditor(Arrays.asList(enumValues));
						((TbComboboxEditor)proxy).setValue(value);
					}
				}else if(DimDataType.TXT == vectorType){
					proxy = new StringEditor();
				}else if(DimDataType.REF_DOC == vectorType){
					DimDataType dimDataType = DimVectorPropUtil.getDimDataType( cInfo.getDimVector());
					String refDocName = dimDataType.getRefDocName();
					if(refDocName != null){
						proxy = new TbVarDocCellEitor(refDocName);
					}
				}else{
					proxy = new TbDoubleEditor();
				}
			}
		}
		return proxy;
	}
	
	private SheetCellEditor getDefaultSheetCellEditor(){
		SheetCellEditor proxy = new StringEditor(); 
		return proxy;
	}
	
	//浮动参照单元改变动作
	public void cellVarDataChanged(Object obj,CellExtInfo cInfo,Cell cell,final CellsPane table) {
		// TODO Auto-generated method stub
		if (obj instanceof DimMember) {
			int varType = cInfo
					.getExVarAreaDef().varAreaType;
			int beginNum, editNum;
			if (varType == ExVarAreaDef.varAreatType_ROW) {
				beginNum = cell.getRow();
				editNum = cell.getCol();
			} else {

				editNum = cell.getRow();
				beginNum = cell.getCol();
			}
			// tabel.get cell
			AreaPosition ap = table
					.getDataModel()
					.getCombinedCellArea(
							CellPosition
									.getInstance(
											cell.getRow(),
											cell.getCol()));

			VarCellValueModel varCellModel = new VarCellValueModel(
					varType,
					table.getDataModel(),
					beginNum,
					editNum,
					new DimMember[] { (DimMember) obj },
					ap.split().length);
			try {
				varCellModel
						.fireCellValueChaned();
			} catch (BusinessException be) {
				NtbLogger.error(be);
			}
		}
	}
	
	private boolean isHiddenRow(CellsPane table, Cell cell) {
		CellExtInfo extInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
		Cell rightCell = table.getCell(cell.getRow(), cell.getCol() + 1);
		CellExtInfo extRightInfo = null, extDownInfo = null;
		if (rightCell != null) {
			extRightInfo = (CellExtInfo) rightCell.getExtFmt(TbIufoConst.tbKey);
		}

		Cell downCell = table.getCell(cell.getRow() + 1, cell.getCol());
		if (downCell != null) {
			extDownInfo = (CellExtInfo) downCell.getExtFmt(TbIufoConst.tbKey);
		}
		if (extInfo != null && extInfo.getVarId() == null) {
			if (extRightInfo != null && extRightInfo.getVarId() != null) {
				return true;
			} else if (extDownInfo != null && extDownInfo.getVarId() != null) {
				return true;
			}
		}
		return false;
	}


	public boolean isInDynArea(int row, int col, CellsModel cm) {
		return !(getDynAreaCell(CellPosition.getInstance(row, col), cm) == null);
	}

	private ExtendAreaCell getDynAreaCell(CellPosition pos, CellsModel cm) {
		ExtendAreaCell[] dynCells = getDynAreaCells(cm);
		for (int i = 0; i < dynCells.length; i++) {
			AreaPosition area = dynCells[i].getArea();
			if (area.contain(pos)) {
				return dynCells[i];
			}
		}
		return null;
	}

	/**
	 * 得到所有动态区域。
	 * 
	 * @return DynAreaCell[]
	 */
	public ExtendAreaCell[] getDynAreaCells(CellsModel cm) {
		// 暂时不做缓存，效率优化时再考虑。
		ArrayList<ExtendAreaCell> list = new ArrayList<ExtendAreaCell>();
		Iterator<IAreaAtt> iter = cm.getAreaDatas().iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			if (element instanceof ExtendAreaCell) {
				list.add((ExtendAreaCell) element);
			}
		}
		return (ExtendAreaCell[]) list.toArray(new ExtendAreaCell[0]);
	}

	/**
	 * @return
	 * @see javax.swing.CellEditor#stopCellEditing()
	 */
	public boolean stopCellEditing() {
		if (proxy == null) {
			return false;
		}
		return proxy.stopCellEditing();
	}

	/**
	 * @return
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		try {
			return proxy.getCellEditorValue();
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	/**
	 * @see com.ufsoft.table.re.SheetCellEditor#getEditorPRI()
	 */
	public int getEditorPRI() {
		return 1;
	}

	/**
	 * @param anEvent
	 * @return
	 * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
	 */
	public boolean isCellEditable(EventObject anEvent) {
		if (proxy == null) {
			if (anEvent instanceof MouseEvent) {
				return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
			}
			return true;
		} else {
			return proxy.isCellEditable(anEvent);
		}
	}

	/**
	 * @param anEvent
	 * @return
	 * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
	 */
	public boolean shouldSelectCell(EventObject anEvent) {
		return proxy.shouldSelectCell(anEvent);
	}

	/**
	 * 
	 * @see javax.swing.CellEditor#cancelCellEditing()
	 */
	public void cancelCellEditing() {
		proxy.cancelCellEditing();
	}

	/**
	 * @param l
	 * @see javax.swing.CellEditor#addCellEditorListener(javax.swing.event.CellEditorListener)
	 */
	public void addCellEditorListener(CellEditorListener l) {
		proxy.addCellEditorListener(l);
	}

	/**
	 * @param l
	 * @see javax.swing.CellEditor#removeCellEditorListener(javax.swing.event.CellEditorListener)
	 */
	public void removeCellEditorListener(CellEditorListener l) {
		proxy.removeCellEditorListener(l);
	}

	public boolean isEnabled(CellsModel cellsModel, CellPosition cellPos) {
		return true;
	}

	/**
	 * 缺省的编辑器
	 */
	public SheetCellEditor getDefaultEditor() {
		return new GenericEditor(new JTextField());
	}


	public SheetCellEditor getProxy() {
		return proxy;
	}

	// 取出额外的维度信息
	private List<LevelValue> getDlList(CellsPane cp, String dlCode) {

		if (dlCode.equalsIgnoreCase(IDimLevelCodeConst.MONTH)
				|| dlCode.equalsIgnoreCase(IDimLevelCodeConst.QUARTER)) {
			List<LevelValue> a = new ArrayList<LevelValue>();

			// CellsPane cp = null;
			// if (editorComponent.getParent() instanceof CellsPane) {
			// cp = (CellsPane) editorComponent.getParent();
			// }
			// if (cp == null) {
			// return null;
			// }
			ITbRepDataEditor editor = (ITbRepDataEditor) SwingUtilities
					.getAncestorOfClass(ITbRepDataEditor.class, cp);
			if (editor == null) {
				return null;
			}
			// proxy.stopCellEditing();
			// int inputDir = DIR_DOWN;
			TbPlanContext context = editor.getTbPlanContext();

			// MdTask task =
			// tbSheetViewer.getViewManager().getTbPlanContext().getTasks()[0];
			MdTask task = context.getTasks()[0];
			// 年
			IDimManager idm = DimServiceGetter.getDimManager();
			DimLevel yl = idm.getDimLevelByBusiCode("YEAR");
			LevelValue ylv = yl.getLevelValueByUniqCode(task.getPk_year());
			a.add(ylv);
			// 季
			if (task.getPk_paradims() != null) {
				IStringConvertor sc = StringConvertorFactory
						.getConvertor(DimSectionTuple.class);
				DimSectionTuple paraDim = (DimSectionTuple) sc.fromString(task
						.getPk_paradims());
				Map<DimLevel, LevelValue> b = paraDim.getLevelValues();
				for (DimLevel dl : b.keySet()) {
					if (dl.getDimDef().getBusiCode()
							.equals(IDimDefPKConst.TIME)) {
						a.add(b.get(dl));
					}
				}

			}
			return a;
		}
		return null;
	}
}
