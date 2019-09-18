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
		pad.setGroupPaths(new String[] { nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0571")/*@res "����ɾ����"*/ });
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

		// liuchun 2011-06-08 �޸ģ���ǰ��ͼ���Ǳ���������ͼʱ�����ص�contextΪnull������false
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
	 * �õ����ж�̬����
	 *
	 * @return DynAreaCell[]
	 */
	private static ExtendAreaCell[] getDynAreaCells(CellsModel cellsModel)
	{
		// ��ʱ�������棬Ч���Ż�ʱ�ٿ��ǡ�
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
	 * �ؼ��ֲ�������֤ �ؼ�������ظ���֤ ��̬��ʱ��ؼ��ַ�Χ��֤��ֻ��֤�º��յĹ�ϵ��
	 *
	 * @return boolean
	 */
	public static boolean verifyBeforeSave(final IRepDataEditor editor)
	{
		ExtendAreaCell[] dynCells = getDynAreaCells(editor.getCellsModel());
		CellsModel cellsModel = editor.getCellsModel();
		for (ExtendAreaCell dynCell : dynCells) {// ���ÿһ����̬����
			AreaPosition[] unitAreas = DynAreaUtil.getUnitAreas(dynCell,
					cellsModel);
			Vector<String> vecKeyComb = new Vector<String>();
			for (AreaPosition unitArea : unitAreas) {// ���ÿһ������
				if (isAllNull(editor, unitArea))
				{
					continue;
				}
				String keyComb = "";
				List<CellPosition> list = cellsModel
						.getSeperateCellPos(unitArea);
				CellPosition pos = null;
				for (CellPosition cellPosition : list) {// ���ÿһ����Ԫ��
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
//												nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0572")/*@res "��̬���ؼ������ݲ���Ϊ�գ�"*/);
//									}
//								});
								//�ؼ�������Ϊ��ʱ������ѡ��
								cellsModel.getSelectModel().setAnchorCell(cellPos);
								cellsModel.getSelectModel().setSelectedHeader(new int[]{cell.getRow(),cell.getRow()}, Header.ROW);
								throw new MessageException("��̬���ؼ���("+cellPos.toString()+")���ݲ���Ϊ�գ�");
//								throw new MessageException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0572")/*@res "��̬���ؼ������ݲ���Ϊ�գ�"*/);
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
							// ʱ�䷶Χ�ڽ����༭ʱ���ƣ��Զ�����Ϊ��Χ�ڵ�ֵ�����ﲻ�ٿ��ơ�
							// if(keyFmt.getType() == KeyFmt.TYPE_TIME){
							// if(!isInTimeExtent(value.toString())){
							// return false;
							// }
							// }
						}
					}
				}
				if (!keyComb.equals(""))
				{// �����޹ؼ��֣��������ӳ������У����򲻼���Ƚ�
					if (vecKeyComb.contains(keyComb))
					{
//						SwingUtilities.invokeLater(new Runnable(){
//							public void run() {
//								JOptionPane
//								.showMessageDialog(
//										editor.getMainboard(),
//										nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0246")/*@res "��̬���ؼ�����ϲ����ظ���"*/);
//						}
//						});
						cellsModel.getSelectModel().setAnchorCell(pos);
						throw new MessageException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0246")/*@res "��̬���ؼ�����ϲ����ظ���"*/);
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
			{// doubleֵ0.0Ҳ��Ϊ�ǿ�ֵ
				continue;
			} else if(cell != null && cell.getValue() != null && cell.getValue() instanceof IDName &&
			         ((IDName)cell.getValue()).getID() != null) {
				return false;
			} else if (cell != null && cell.getValue() != null && !(cell.getValue() instanceof IDName)
					&& !"".equals(cell.getValue().toString()))
			{
				// @edit by wuyongc at 2012-4-20,����12:21:52 ������Ƕ�̬���ؼ��ֲ����Ǳ��������ʽ������Դ���
				Object keyFmt = cell.getExtFmt(KeyFmt.EXT_FMT_KEYINPUT);
				//tianchuan 2012.8.28 ���� �����ʽΪ�գ�һ��������Ҫ�洢�ĵ�Ԫ������
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
