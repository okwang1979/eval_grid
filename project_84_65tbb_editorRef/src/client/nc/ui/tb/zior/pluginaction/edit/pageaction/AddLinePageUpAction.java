package nc.ui.tb.zior.pluginaction.edit.pageaction;

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.tb.zior.TBSheetViewer;
import nc.ui.tb.zior.TbVarAreaUtil;
import nc.vo.tb.form.excel.ExVarAreaDef;
import nc.vo.tb.form.iufo.CellExtInfo;
import nc.vo.tb.form.iufo.TbIufoConst;

import com.ufida.zior.util.ResourceManager;
import com.ufida.zior.view.Viewer;
import com.ufsoft.table.Cell;

public class AddLinePageUpAction  extends  TbPageAction{


	public AddLinePageUpAction(String funcode, String sheetviewid) {
		super(funcode, sheetviewid);
	}

	@Override
	protected void doAction(Viewer view) {
		// TODO Auto-generated method stub
		TBSheetViewer tbSheetViewer = (TBSheetViewer)view;
		if(!tbSheetViewer.isEditing())return;
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell c = cells.get(cells.size() - 1);
		if(c==null) return;
		CellExtInfo cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
		if(cInfo==null||cInfo.getVarId()==null)
			return;
		CellContentUtil util= new CellContentUtil(tbSheetViewer);
		util.addLine(CellContentUtil.ADDLINEUP);
	}

	@Override
	public String getName() {
		return TbActionName.getName_AddUpLine();
	}

	@Override
	public Icon getIcon() {
		return ResourceManager.createIcon(ITbPlanActionCode.ADDUPLINE_ICON);
	}

	@Override
	public KeyStroke getAccelerator() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_N,
				KeyEvent.CTRL_MASK);
	}

	@Override
	public boolean isEnabled() {
		if(getTbSheetViewer()==null)return false;
		if(!getTbSheetViewer().isEditing()) return false;
		List<Cell> cs = getTbSheetViewer().getSelectedCell();
		if(cs == null){
			return false;
		}
		Cell c = cs.get(cs.size()-1);
		if(c == null) return false;
		if(getTbSheetViewer().getTbPlanContext() != null && getTbSheetViewer().getTbPlanContext().isIndexApprove()){
			return false;
		}
		if(TbVarAreaUtil.IsAutoExpandVarArea(getTbSheetViewer())) return false;
		if(TbVarAreaUtil.getAutoExpandStaus(getTbSheetViewer())==ExVarAreaDef.isAutoExpandByDataCell_ALL)
			return false;
		CellExtInfo cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
		if(cInfo == null || cInfo.getVarId() == null){
			return false;
		}
		if(TbVarAreaUtil.IsFirstVarRow(c,cInfo, getTbSheetViewer().getCellsModel())){
			return false;
		}
		if(cInfo.getExVarAreaDef()!=null&&(cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_ROW
				||cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_COL))
			return true;
		else 
			return false;

	}
	
}
