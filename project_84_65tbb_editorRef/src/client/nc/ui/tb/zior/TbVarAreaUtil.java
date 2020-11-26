package nc.ui.tb.zior;

import java.util.List;
import java.util.Map;

import nc.itf.mdm.dim.IDimManager;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.tb.pub.TbParamUtil;
import nc.ms.tb.task.data.TCell;
import nc.ms.tb.task.data.TaskSheetDataModel;
import nc.ms.tb.tracing.RuleTracingManager;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ui.tb.model.TBDataCellRefModel;
import nc.ui.tb.table.DimLevelValueCellEditor;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.tb.form.excel.ExVarAreaDef;
import nc.vo.tb.form.excel.ExVarDef;
import nc.vo.tb.form.iufo.CellExtInfo;
import nc.vo.tb.form.iufo.CellFmlInfo;
import nc.vo.tb.form.iufo.TbIufoConst;
import nc.vo.tb.ntbenum.NTBActionEnum;
import nc.vo.tb.obj.LevelValueOfDimLevelVO;

import com.ufsoft.table.Cell;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.CellsPane;

/**
 * 浮动区工具类
 * @author liuysh
 *
 */
public class TbVarAreaUtil {

	/**
	 * 判断是否要自动扩展浮动区
	 * @param tbSheetViewer
	 * @return true Or false
	 */
	public static boolean IsAutoExpandVarArea(TBSheetViewer tbSheetViewer){
		boolean bl = false;
		if(!tbSheetViewer.isEditing())return false;
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell c = cells.get(cells.size() - 1);
		if(c==null) return false;
		CellExtInfo cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
		if(cInfo==null||cInfo.getVarId()==null)
			return false;
		ExVarAreaDef def =cInfo.getExVarAreaDef();
		if(def != null && def.varDefList != null){
			for(ExVarDef e:def.varDefList){
				if(e.cellType == ExVarDef.cellType_auto_expand){
					bl = true;
					break;
				}
			}
		}
		return bl;
	}
	/**
	 * 判断是否按多维数据加载
	 * @param tbSheetViewer
	 * @return
	 */
	public static int getAutoExpandStaus(TBSheetViewer tbSheetViewer){

		int i = -2;
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell c = cells.get(cells.size() - 1);
		if(c!=null){
			CellExtInfo cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
			if(cInfo!=null&&cInfo.getVarId()!=null){
				ExVarAreaDef def =cInfo.getExVarAreaDef();
				if(def!=null)
					i = def.isAutoExpandByDataCell;
			}
		}
		return i;
	}
	/**
	 * 判断是否文本浮动
	 * @param tbSheetViewer
	 * @return
	 */
	public static boolean IsTextVarArea(TBSheetViewer tbSheetViewer){
		boolean bl = true;
		if(!tbSheetViewer.isEditing()) return false;
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell c = cells.get(cells.size() - 1);
		if(c==null) return false;
		CellExtInfo cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
		if(cInfo==null||cInfo.getVarId()==null)
			return false;
		ExVarAreaDef def =cInfo.getExVarAreaDef();
		if(def != null && def.varDefList != null){
			for(ExVarDef e:def.varDefList){
				if(e.cellType == ExVarDef.cellType_dim){
					bl = false;
					break;
				}
			}
		}
		return bl;
	}
	/**
	 * 判断是否是多维浮动
	 * @param tbSheetViewer
	 * @return
	 */
	public static boolean IsDimVarArea(TBSheetViewer tbSheetViewer){
		boolean bl = false;
		if(!tbSheetViewer.isEditing()) return false;
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell c = cells.get(cells.size() - 1);
		if(c==null) return false;
		CellExtInfo cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
		if(cInfo==null||cInfo.getVarId()==null)
			return false;
		if(cInfo.getExVarAreaDef()!=null){
			for(ExVarDef varDef:cInfo.getExVarAreaDef().varDefList){
				if(varDef.dimLevelCode!=null&&!varDef.dimLevelCode.equals(""))
				{
					bl = true;
					break;
				}
			}
		}
		return bl;
	}
	public static boolean IsDimVarArea(CellExtInfo cInfo){
		boolean bl = false;
		if(cInfo==null||cInfo.getVarId()==null)
			return false;
		if(cInfo.getExVarAreaDef()!=null){
			for(ExVarDef varDef:cInfo.getExVarAreaDef().varDefList){
				if(varDef.dimLevelCode!=null&&!varDef.dimLevelCode.equals("")&& varDef.cellType == varDef.cellType_dim)
				{
					bl = true;
					break;
				}
			}
		}
		return bl;
	}
	/**
	 * 判断是否是区域浮动
	 * @param cInfo
	 * @return
	 */
	public static boolean isAreaVar(TBSheetViewer tbSheetViewer){
		boolean bl = true;
		if(!tbSheetViewer.isEditing()) return false;
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell c = cells.get(cells.size() - 1);
		if(c==null) return false;
		CellExtInfo cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
		if(cInfo==null||cInfo.getVarId()==null)
			return false;
		if(cInfo.getExVarDef() != null && cInfo.getExVarDef().levelValueList.size()>1){
			return true;
		}
		return bl;
	}
	/**
	 * 判断是否为多维浮动默认行
	 * @return
	 */
	public static boolean IsDefaultVarDim(Cell c,CellExtInfo cInfo , CellsModel cellsModel){
		if(cInfo == null){
			cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
		}
		boolean varDimNull = true;
		boolean isNewLine = true;
		int colNum = cellsModel.getColNum();
//		if(cInfo.getExVarAreaDef()!=null){
//			for(ExVarDef varDef:cInfo.getExVarAreaDef().varDefList){
//				if(varDef.dimLevelCode==null||varDef.dimLevelCode.equals(""))
//				{
//					varDimNull = false;
//					break;
//				}
//			}
//		}
		if(!IsDimVarArea(cInfo)){
			varDimNull = false;
		}
		for(int col = 0;col<colNum;col++){
			Cell cell = cellsModel.getCell(c.getRow(), col);
			CellExtInfo info = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
			if(info == null || info.getVarId()==null)
				continue;
			if(!info.isNewLine()){
				isNewLine = false;
			}
			if(info.getExVarDef() != null && info.getVarDimDef() != null && info.getExVarDef().cellType!=ExVarDef.cellType_readonly){
				if(cell.getValue()!=null&&!cell.getValue().toString().isEmpty()){
					 varDimNull = false;
				}
			}
		}
		if(!isNewLine && varDimNull){
			return true;
		}
		return false;
	}
	/**
	 * 浮动行第一行禁止向上增行按钮
	 * @param cellsModel
	 * @return
	 */
	public static boolean IsFirstVarRow(Cell c, CellExtInfo cellExtInfo, CellsModel cellsModel){
		if(c.getRow() < 1){
			return false;
		}
		ExVarAreaDef areaDef = cellExtInfo.getExVarAreaDef();
		if(areaDef == null){
			return false;
		}
		int mergeCount = areaDef.blockSize;
		if(mergeCount == 0){
			for (ExVarDef exVarDef : areaDef.varDefList) {
				if (exVarDef.cellType == ExVarDef.cellType_readonly) {
					if(exVarDef.levelValueList!=null){
						if(mergeCount == 0){
							mergeCount = exVarDef.levelValueList.size();
						}
					}
				}
			}
		}
		if(mergeCount == 0){
			mergeCount = 1;
		}
		for(int i=0;i<mergeCount;i++){
			Cell cell =  cellsModel.getCell(c.getRow() - 1 - i, c.getCol());
			CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
			if(cInfo != null && cInfo.getVarId() == null){
				return true;
			}
		}
		return false;
	}
	/**
	 * 浮动参照（拥有分类的维度）
	 * @param row
	 * @param col
	 * @param table
	 * @param varAreaDef
	 * @param varDef
	 * @return
	 */
	public static String getDimParentKey(int row,int col,CellsPane table, ExVarAreaDef varAreaDef,ExVarDef varDef){
		String levelValueKey = null;
		if(varDef.dimLevelCode != null){
			if(IDimLevelCodeConst.CUSTOM.equals(varDef.dimLevelCode) ||
					IDimLevelCodeConst.MARBAS.equals(varDef.dimLevelCode) ||
					IDimLevelCodeConst.SUPPLIER.equals(varDef.dimLevelCode) ||
					IDimLevelCodeConst.PRO.equals(varDef.dimLevelCode) ||
					IDimLevelCodeConst.MPPMARVERSION.equals(varDef.dimLevelCode)){
				IDimManager idm = DimServiceGetter.getDimManager();
				String code = getClassDimlevelBycode(varDef.dimLevelCode);
				if(code == null){
					return null;
				}
				for(ExVarDef def: varAreaDef.varDefList){
					if(code.equals(def.dimLevelCode)){
						DimLevel dl = idm.getDimLevelByBusiCode(code);
						if(varAreaDef.varAreaType  == ExVarAreaDef.varAreatType_ROW){
							col = col + def.index - varDef.index;
						}else if(varAreaDef.varAreaType  == ExVarAreaDef.varAreatType_COL){
							row = row + def.index - varDef.index;
						}
						Cell c = table.getCell(row, col);
						//单元格上没有数据则不处理，主要由于增加行的时候会带着复制单元格上的DimSectionTuple
						if(c.getValue() == null){
							return null;
						}
						CellExtInfo cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
						if(cInfo/*.getDimSectionTuple()*/ != null){
							LevelValue lv = cInfo.getLevelValue(dl);//cInfo.getDimSectionTuple().getLevelValue(dl);
							if(lv != null){
								return lv.getKey().toString();
							}
						}
						break;
					}
				}
			}else if(IDimLevelCodeConst.CUSTOMCLASS.equals(varDef.dimLevelCode) ||
					IDimLevelCodeConst.MARBASCLASS.equals(varDef.dimLevelCode) ||
					IDimLevelCodeConst.SUPPLIERCLASS.equals(varDef.dimLevelCode)||
					IDimLevelCodeConst.PROEPS.equals(varDef.dimLevelCode) ||
					IDimLevelCodeConst.MPPMARBASCLASS.equals(varDef.dimLevelCode)){
				IDimManager idm = DimServiceGetter.getDimManager();
				String code = getDimlevelByClasscode(varDef.dimLevelCode);
				if(code == null){
					return null;
				}
				for(ExVarDef def: varAreaDef.varDefList){
					if(code.equals(def.dimLevelCode)){
						DimLevel dl = idm.getDimLevelByBusiCode(code);
						if(varAreaDef.varAreaType  == ExVarAreaDef.varAreatType_ROW){
							col = col + def.index - varDef.index;
						}else if(varAreaDef.varAreaType  == ExVarAreaDef.varAreatType_COL){
							row = row + def.index - varDef.index;
						}
						Cell c = table.getCell(row, col);
						if(c.getValue() == null){
							return null;
						}
						CellExtInfo cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
						if(cInfo/*.getDimSectionTuple()*/ != null){
							LevelValue lv = cInfo.getLevelValue(dl);//cInfo.getDimSectionTuple().getLevelValue(dl);
							if(lv != null){
							 	DimMember dimMember = dl.getDimDef().getDefaultHierarchy().getMemberReader().getMemberByLevelValues(lv);
							 	if(dimMember != null){
							 		if(dimMember.getDimLevel().getObjCode().equals(IDimLevelCodeConst.PRO)){
							 			if(dimMember.getParentMemberUpLevel() != null && dimMember.getParentMemberUpLevel().getParentMemberUpLevel() != null){
								 			levelValueKey = dimMember.getParentMemberUpLevel().getParentMemberUpLevel().getKey().toString();
								 		}
							 		}else{
							 			if(dimMember.getParentMemberUpLevel() != null){
								 			levelValueKey = dimMember.getParentMemberUpLevel().getKey().toString();
								 		}
							 		}
							 	}
							 }
						}
						break;
					}
				}
			}
		}else{
			return null;
		}
		return levelValueKey;
	}

	/**
	 * 浮动参照（指定范围）
	 * @param row
	 * @param col
	 * @param table
	 * @param varAreaDef
	 * @param varDef
	 * @return
	 */
	public static List<String> getDimSelectedKey(int row,int col,CellsPane table, ExVarAreaDef varAreaDef,ExVarDef varDef){
		List<String> levelValueKey = null;
		if(varDef.dimLevelCode != null){
			if(IDimLevelCodeConst.CUSTOM.equals(varDef.dimLevelCode) ||
					IDimLevelCodeConst.MARBAS.equals(varDef.dimLevelCode) ||
					IDimLevelCodeConst.SUPPLIER.equals(varDef.dimLevelCode) ||
					IDimLevelCodeConst.PRO.equals(varDef.dimLevelCode) ||
					IDimLevelCodeConst.MPPMARVERSION.equals(varDef.dimLevelCode)){
				String code = getClassDimlevelBycode(varDef.dimLevelCode);
				if(code == null){
					return levelValueKey;
				}
				for(ExVarDef def: varAreaDef.varDefList){
					if(code.equals(def.dimLevelCode)){
						levelValueKey = def.levelValueList;
						break;
					}
				}
			}
		}
		return levelValueKey;
	}
	/**
	 * 查找当前行的多维CELL的DV
	 * {方法功能中文描述}
	 *
	 * @param code
	 * @return
	 * @author: changpeng@yonyou.com
	 */
	public static Map<DimLevel,LevelValue> getDVMap(Cell cell,final CellExtInfo cInfo,ExVarDef exVarDef, final CellsPane table){
		int varType = cInfo.getExVarAreaDef().varAreaType;
		Map<DimLevel,LevelValue> map = null;
		if(varType == ExVarAreaDef.varAreatType_ROW){
			int row = cell.getRow();
			int col = cell.getCol();
			while(table.getCell(row, col) != null && table.getCell(row, col).getExtFmt(TbIufoConst.tbKey) != null){
				CellExtInfo cextInfo = (CellExtInfo) table.getCell(row, col).getExtFmt(TbIufoConst.tbKey);
				if(cextInfo != null && (cextInfo.getDimVector() != null||cextInfo.getDimSectionTuple()!=null)){
					if( cextInfo.getDimVector() == null){
						DimLevel entityDl = DimServiceGetter.getDimManager().getDimLevelByBusiCode(IDimLevelCodeConst.ENTITY);
						if(cextInfo.getDimSectionTuple().getLevelValues().keySet().contains(entityDl)){
							map = cextInfo.getLevelMap()/*.getDimSectionTuple().getLevelValues()*/;
							break;
						}
						
					}else{
						map = cextInfo.getLevelMap()/*.getDimSectionTuple().getLevelValues()*/;
						break;
					}
						
					 
					
				}
				col++;
			}
		}else{
			int row = cell.getRow();
			int col = cell.getCol();
			while(table.getCell(row, col) != null && table.getCell(row, col).getExtFmt(TbIufoConst.tbKey) != null){
				CellExtInfo cextInfo = (CellExtInfo) table.getCell(row, col).getExtFmt(TbIufoConst.tbKey);
				if(cextInfo != null && cextInfo.getDimVector() != null){
					map = cextInfo.getLevelMap();//cextInfo.getDimSectionTuple().getLevelValues();
					break;
				}
				row++;
			}
		}
		return map;
	}
	public static String getClassDimlevelBycode(String code){
		if(IDimLevelCodeConst.CUSTOM.equals(code)){
			return IDimLevelCodeConst.CUSTOMCLASS;
		}else if(IDimLevelCodeConst.MARBAS.equals(code)){
			return IDimLevelCodeConst.MARBASCLASS;
		}else if(IDimLevelCodeConst.SUPPLIER.equals(code)){
			return IDimLevelCodeConst.SUPPLIERCLASS;
		}else if(IDimLevelCodeConst.PRO.equals(code)){
			return IDimLevelCodeConst.PROEPS;
		}else if(IDimLevelCodeConst.MPPMARVERSION.equals(code)){
			return IDimLevelCodeConst.MPPMARBASCLASS;
		}
		return null;
	}

	public static String getDimlevelByClasscode(String code){
		if(IDimLevelCodeConst.CUSTOMCLASS.equals(code)){
			return IDimLevelCodeConst.CUSTOM;
		}else if(IDimLevelCodeConst.MARBASCLASS.equals(code)){
			return IDimLevelCodeConst.MARBAS;
		}else if(IDimLevelCodeConst.SUPPLIERCLASS.equals(code)){
			return IDimLevelCodeConst.SUPPLIER;
		}else if(IDimLevelCodeConst.PROEPS.equals(code)){
			return IDimLevelCodeConst.PRO;
		}else if(IDimLevelCodeConst.MPPMARBASCLASS.equals(code)){
			return IDimLevelCodeConst.MPPMARVERSION;
		}
		return null;
	}

//	public static boolean isMatchMember(Cell cell){
//		CellExtInfo cInfo = c == null ? null : (CellExtInfo) c
//				.getExtFmt(TbIufoConst.tbKey);
//		if (cInfo != null && cInfo.getVarId() != null
//				&& cInfo.getVarId().length() > 0
//				&& cInfo.getCubeCode() != null
//				&& cInfo.getDimVector() != null) {
//			DimMember member = cInfo.getDimVector().getDimMember(dd);
//		}
//		return true;
//	}

	private static DimLevel getCustomLevel(){
		IDimManager idm = DimServiceGetter.getDimManager();
		return idm.getDimLevelByBusiCode(IDimLevelCodeConst.CUSTOM);
	}

	private static DimLevel getMarbasLevel(){
		IDimManager idm = DimServiceGetter.getDimManager();
		return idm.getDimLevelByBusiCode(IDimLevelCodeConst.MARBAS);
	}

	private static DimLevel getSupplierLevel(){
		IDimManager idm = DimServiceGetter.getDimManager();
		return idm.getDimLevelByBusiCode(IDimLevelCodeConst.SUPPLIER);
	}

	public static ExVarDef getVarDefByCellExtInfo(CellExtInfo cInfo){
		if(cInfo==null)
			return null;
		int cellCount = -1;
		//单元格编辑器
		if (cInfo.getExVarAreaDef() != null) {
			//浮动
			if (cInfo.getExVarAreaDef().varDefList != null
					&& !cInfo.getExVarAreaDef().varDefList.isEmpty()) {
				int i = cInfo.getIndex();
				cellCount++;
				if (cInfo.getExVarAreaDef().varDefList.size() - 1 >= i) {
					for (ExVarDef def : cInfo.getExVarAreaDef().varDefList) {
						if (def.index == i) {
							return def;
						}
					}
				}
			}
		}
		return null;
	}

	/*
	 * <#业务方案#>
	 */
	public static String getUapRefName(String inputName){
		if(inputName == null || inputName.length() < 4){
			return null;
		}
		String refName = inputName.substring(2, inputName.length() - 2);
		return refName;
	}

	public static void initTBDataCellRefModel(TBDataCellRefModel tBDataCellRefModel,LevelValueOfDimLevelVO levelValueOfDimLevelVO,
				String pk_user, String pk_group,String cubeCode, ExVarDef exVarDef,String parentKey,Map<DimLevel, LevelValue> dvMap)throws BusinessException{
		try {
			if(levelValueOfDimLevelVO != null && (levelValueOfDimLevelVO.dimlevelCode == null || levelValueOfDimLevelVO.dimlevelCode.trim().equals(""))){
				tBDataCellRefModel.setDicStrList(exVarDef.levelValueList);
			}else{
//				List<DimMember> dimMembers = DimServiceGetter.getVarMemberService().getVarMemberByTask(new LevelValueOfDimLevelVO[]{levelValueOfDimLevelVO}, cubeCode,pk_user,pk_group,exVarDef.mesType == null ? null:new String[]{exVarDef.mesType.name()},parentKey,dvMap);
				List<DimMember> dimMembers = TbMemberFilterUtil.getDimMembers(tBDataCellRefModel, levelValueOfDimLevelVO, pk_user, pk_group, cubeCode, exVarDef, parentKey, dvMap);
				tBDataCellRefModel.setDimMembers(dimMembers);
			}

			DimLevel dimlevel = DimServiceGetter.getDimManager().getDimLevelByBusiCode(levelValueOfDimLevelVO.dimlevelCode);
			tBDataCellRefModel.setRootName(dimlevel.getObjName());
			tBDataCellRefModel.setCanSelectLevelCode(levelValueOfDimLevelVO.dimlevelCode);
			tBDataCellRefModel.setTableName(levelValueOfDimLevelVO.dimlevelCode);
			tBDataCellRefModel.setRaradims(levelValueOfDimLevelVO, cubeCode,pk_user,pk_group,exVarDef,parentKey,dvMap);
		    if(!ITbPlanActionCode.NOTSUPPORT.equals(TbParamUtil.isStartDeptByOrg())&&IDimLevelCodeConst.DEPT.equals(exVarDef.dimLevelCode)){
//				tBDataCellRefModel.setFilterRefNodeName(new String[] { "预算主体参照" });
		    	tBDataCellRefModel.setFilterRefNodeName(new String[] { nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0211")/*@res "业务单元"*/ });

			}
		} catch (Exception e) {
			NtbLogger.print(e);
		}
	}
	public static boolean canDeleteData(String nodeType,CellsModel cellsModel,TaskSheetDataModel tsDataModel,int row,int col,List<LevelValue>  canEditMvtypes){

		Cell cell = cellsModel==null ? null : cellsModel.getCell(row, col);
		CellExtInfo cInfo = cell==null ? null : (CellExtInfo)cell.getExtFmt(TbIufoConst.tbKey);
		if (cInfo != null) {
			if(cInfo.isReadOnly()){
				return false;
			}else{
				DataCell dc = (DataCell)cell.getExtFmt(TbIufoConst.dataccellKey);
				if(dc != null && (!dc.canSetValue()/*!dc.isFlag_Summed()*/ ||
						dc.isFlag_HasConsistFormula() || dc.isFlag_Adjust() || dc.isFlag_Locked()))  //单据调整可以调整锁定的单元格, 所以放开锁定单元格不可调的限制
					return false;
			}
		}else{
			return false;
		}
		if(tsDataModel != null){
			TCell tc = tsDataModel.getCellAt(row, col);
			RuleTracingManager tm = new RuleTracingManager();
			String ruleType = nodeType.equals(ITbPlanActionCode.COM_NODETYPE) ? NTBActionEnum.CALACTION.toString():NTBActionEnum.GETDATAACTION.toString();
			boolean isActualCanEdit = TbParamUtil.isActualCanEdit();
			if(!TbZiorUiCtl.isSameMvType(cInfo,nodeType,tsDataModel.getMdTask(),canEditMvtypes, isActualCanEdit))
				return false;
			CellFmlInfo cellFmlInfo=tsDataModel.getParentModel().getFmlsByDimVector(tc.getCubeCode(), tc.getDimVector());
			if(cellFmlInfo!=null&&!tm.isAllotRuleForCell(tsDataModel.getMdTask(),tc.getDatacell(),ruleType))
				return false;
		}
		return true;

	}
}
