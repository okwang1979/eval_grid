package com.ufsoft.iufo.inputplugin.ufodynarea;

import java.util.List;
import java.util.Vector;

import javax.swing.KeyStroke;

import com.ufida.dataset.IContext;
import com.ufida.iufo.table.exarea.ExtendAreaCell;
import com.ufida.zior.exception.MessageException;
import com.ufida.zior.plugin.AbstractPluginAction;
import com.ufida.zior.plugin.IPluginActionDescriptor;
import com.ufida.zior.plugin.PluginActionDescriptor;
import com.ufida.zior.plugin.PluginKeys.XPOINT;
import com.ufida.zior.view.Viewer;
import com.ufsoft.iufo.fmtplugin.datapreview.ReportDataPreviewDesigner;
import com.ufsoft.iufo.fmtplugin.dynarea.DynAreaUtil;
import com.ufsoft.iufo.fmtplugin.dynarea.DynamicAreaModel;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.inputplugin.key.KeyFmt;
import com.ufsoft.report.dialog.UfoDialog;
import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.Cell;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.TableSetting;
import com.ufsoft.table.format.DefaultDataFormat;
import com.ufsoft.table.header.Header;

import nc.pub.iufo.basedoc.IDName;

import nc.ui.iufo.input.edit.base.AbsBaseRepDataEditor;
import nc.ui.iufo.input.edit.base.IRepDataEditor;

public abstract class AbsUfoDynAreaActionExt extends AbstractPluginAction
{

	public AbsUfoDynAreaActionExt()
	{
	}

	@Override
	public IPluginActionDescriptor getPluginActionDescriptor()
	{
		PluginActionDescriptor pad = new PluginActionDescriptor(getMenuName());
		pad.setGroupPaths(new String[] { nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0571")/*@res "插入删除行"*/ });
		pad.setAccelerator(getActionKeyStroke());

		String strIcon = getIconName();
		if (strIcon == null)
			pad.setExtensionPoints(XPOINT.POPUPMENU);
		else
		{
			pad.setIcon(strIcon);
			pad.setExtensionPoints(XPOINT.POPUPMENU, XPOINT.TOOLBAR);

		}

		return pad;
	}

	@Override
	public boolean isEnabled()
	{
		Viewer curView = getCurrentView();
		if (curView==null)
			return false;

		CellsModel cellsModel=getEditorCellsModel();
		if (cellsModel == null){
			return false;
		}

		// liuchun 2011-06-08 修改，当前视图不是报表数据视图时，返回的context为null，返回false
		IContext context = getEditorContext();
		if(context == null) {
			return false;
		}
		// end
		Boolean bShowDynGroup = (Boolean) context.getAttribute(
				IUfoContextKey.SHOW_DYNGROUP);
		if (Boolean.TRUE.equals(bShowDynGroup))
			return false;

		CellPosition anchorPos = cellsModel.getSelectModel().getAnchorCell();
		TableSetting tableSetting = cellsModel.getTableSetting();
		if (tableSetting != null && (tableSetting.getCellsAuth() == null
				|| tableSetting.getCellsAuth().isWritable(anchorPos.getRow(),
						anchorPos.getColumn())))
		{
			return isInDynArea(cellsModel, anchorPos.getRow(), anchorPos
					.getColumn());
		} else
		{
			return false;
		}
	}

	protected String getIconName()
	{
		return null;
	}

	protected int getInputCount(boolean bAddRow)
	{
		SelInsRowNumDlg dlg = new SelInsRowNumDlg(this.getMainboard(), bAddRow);
		dlg.setVisible(true);
		if (dlg.getResult() == UfoDialog.ID_OK)
		{
			return dlg.getInputNum();
		}
		return 0;
	}

//	protected IRepDataEditor getRepDataEditor()
//	{
//		return ((AbsBaseRepDataEditor) getCurrentView())
//				.getActiveRepDataEditor();
//	}

	/**
	 * 得到所有动态区域。
	 *
	 * @return DynAreaCell[]
	 */
	private static ExtendAreaCell[] getDynAreaCells(CellsModel cellsModel)
	{
		// 暂时不做缓存，效率优化时再考虑。
		DynamicAreaModel dynModel = DynamicAreaModel.getInstance(cellsModel);
		return dynModel.getDynAreaCells();
	}

	protected ExtendAreaCell getAnchorDynAreaCell(CellsModel cellsModel)
	{
		CellPosition anchorPos = cellsModel.getSelectModel().getAnchorCell();
		return getDynAreaCell(cellsModel, anchorPos);
	}

	protected boolean isInDynArea(CellsModel cellsModel, int row, int col)
	{
		return !(getDynAreaCell(cellsModel, CellPosition.getInstance(row, col)) == null);
	}

	protected static ExtendAreaCell getDynAreaCell(CellsModel cellsModel,
			CellPosition pos)
	{
		ExtendAreaCell[] dynCells = getDynAreaCells(cellsModel);
		for (ExtendAreaCell dynCell : dynCells) {
			AreaPosition area = dynCell.getArea();
			if (area.contain(pos))
			{
				return dynCell;
			}
		}
		return null;
	}

	/**
	 * 关键字不完整验证 关键字组合重复验证 动态区时间关键字范围验证，只验证月和日的关系。
	 *
	 * @return boolean
	 */
	public static boolean verifyBeforeSave(final IRepDataEditor editor)
	{
		ExtendAreaCell[] dynCells = getDynAreaCells(editor.getCellsModel());
		CellsModel cellsModel = editor.getCellsModel();
		for (ExtendAreaCell dynCell : dynCells) {// 针对每一个动态区域
			AreaPosition[] unitAreas = DynAreaUtil.getUnitAreas(dynCell,
					cellsModel);
			Vector<String> vecKeyComb = new Vector<String>();
			for (AreaPosition unitArea : unitAreas) {// 针对每一组数据
				if (isAllNull(editor, unitArea))
				{
					continue;
				}
				String keyComb = "";
				List<CellPosition> list = cellsModel
						.getSeperateCellPos(unitArea);
				CellPosition pos = null;
				for (CellPosition cellPosition : list) {// 针对每一个单元格
					CellPosition cellPos = cellPosition;
					Cell cell = cellsModel.getCell(cellPos);
					if (cell != null)
					{
						KeyFmt keyFmt = DynAreaUtil.getKeyFmt(cellPos,
								cellsModel);
						if (keyFmt != null)
						{
							Object value = cell.getValue();
							if (value == null || "".equals(value.toString()) || ((value instanceof IDName) ? ((IDName)value).getID() == null : false))
							{
//								SwingUtilities.invokeLater(new Runnable(){
//									public void run() {
//										JOptionPane
//										.showMessageDialog(
//												editor.getMainboard(),
//												nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0572")/*@res "动态区关键字数据不能为空！"*/);
//									}
//								});
								//关键字数据为空时，设置选中
								cellsModel.getSelectModel().setAnchorCell(cellPos);
								cellsModel.getSelectModel().setSelectedHeader(new int[]{cell.getRow(),cell.getRow()}, Header.ROW);
								throw new MessageException("动态区关键字("+cellPos.toString()+")数据不能为空！");
//								throw new MessageException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0572")/*@res "动态区关键字数据不能为空！"*/);
//								return false;
							} else
							{
								if(value instanceof IDName) {
									keyComb += ((IDName)value).getPk();
								} else {
									keyComb += value.toString();
								}
								pos = cellPos;
							}
							// 时间范围在结束编辑时控制，自动更新为范围内的值。这里不再控制。
							// if(keyFmt.getType() == KeyFmt.TYPE_TIME){
							// if(!isInTimeExtent(value.toString())){
							// return false;
							// }
							// }
						}
					}
				}
				if (!keyComb.equals(""))
				{// 本行无关键字（例如分组加出来的行），则不加入比较
					if (vecKeyComb.contains(keyComb))
					{
//						SwingUtilities.invokeLater(new Runnable(){
//							public void run() {
//								JOptionPane
//								.showMessageDialog(
//										editor.getMainboard(),
//										nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0246")/*@res "动态区关键字组合不能重复！"*/);
//						}
//						});
						cellsModel.getSelectModel().setAnchorCell(pos);
						throw new MessageException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0246")/*@res "动态区关键字组合不能重复！"*/);
//						return false;
					} else
					{
						vecKeyComb.add(keyComb);
					}
				}
			}
		}
		return true;
	}

	protected KeyStroke getActionKeyStroke()
	{
		return null;
	}

	private static boolean isAllNull(IRepDataEditor editor, AreaPosition areaPos)
	{
		List<CellPosition> list = editor.getCellsModel()
				.getSeperateCellPos(areaPos);
		for (CellPosition cellPosition : list) {
			CellPosition cellPos = cellPosition;
			Cell cell = editor.getCellsModel().getCell(cellPos);
			if (cell != null && cell.getValue() != null
					&& cell.getValue() instanceof Double
					&& ((Double) cell.getValue()).doubleValue() == 0.0)
			{// double值0.0也认为是空值
				continue;
			} else if(cell != null && cell.getValue() != null && cell.getValue() instanceof IDName &&
			         ((IDName)cell.getValue()).getID() != null) {
				return false;
			} else if (cell != null && cell.getValue() != null && !(cell.getValue() instanceof IDName)
					&& !"".equals(cell.getValue().toString()))
			{
				// @edit by wuyongc at 2012-4-20,下午12:21:52 如果不是动态区关键字并且是报表表样格式，则忽略处理。
				Object keyFmt = cell.getExtFmt(KeyFmt.EXT_FMT_KEYINPUT);
				//tianchuan 2012.8.28 新增 如果格式为空，一定不是需要存储的单元，忽略
				if(cell.getFormat()==null){
					continue;
				}
				if((cell.getFormat().getDataFormat() instanceof DefaultDataFormat) && keyFmt == null)
					continue;
				return false;
			}
		}
		return true;
	}

	protected CellsModel getEditorCellsModel(){
		Viewer curView = getCurrentView();
		if (curView==null)
			return null;

		if (curView instanceof AbsBaseRepDataEditor){
			IRepDataEditor editor = ((AbsBaseRepDataEditor) curView).getActiveRepDataEditor();
			return editor==null?null:editor.getCellsModel();
		}else if (curView instanceof ReportDataPreviewDesigner){
			return ((ReportDataPreviewDesigner)curView).getCellsModel();
		}else
			return null;
	}

	protected IContext getEditorContext(){
		Viewer curView = getCurrentView();
		if (curView==null)
			return null;

		if (curView instanceof AbsBaseRepDataEditor){
			IRepDataEditor editor = ((AbsBaseRepDataEditor) curView).getActiveRepDataEditor();
			return editor==null?null:editor.getContext();
		}else if (curView instanceof ReportDataPreviewDesigner){
			return curView.getContext();
		}else
			return null;
	}

	protected void setEditorCellsModel(CellsModel cellsModel){
		Viewer curView = getCurrentView();
		if (curView==null)
			return;

		CellsModel oldCellsModel=getEditorCellsModel();
		if (oldCellsModel!=null)
			cellsModel.getTableSetting().setCellsAuth(oldCellsModel.getTableSetting().getCellsAuth());

		if (curView instanceof AbsBaseRepDataEditor){
			IRepDataEditor editor = ((AbsBaseRepDataEditor) curView).getActiveRepDataEditor();
			if (editor!=null)
				editor.setCellsModel(cellsModel);
		}else if (curView instanceof ReportDataPreviewDesigner){
			((ReportDataPreviewDesigner)curView).setCellsModel(cellsModel);
		}
	}

	abstract public Object[] getParams();

	abstract protected String getMenuName();
}
