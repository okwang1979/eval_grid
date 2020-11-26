package nc.ui.tb.zior.pluginaction.edit.pageaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.mdm.dim.IDimManager;
import nc.ms.mdm.convertor.IStringConvertor;
import nc.ms.mdm.convertor.StringConvertorFactory;
import nc.ms.mdm.cube.CubeServiceGetter;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.tb.pub.IDimPkConst;
import nc.ms.tb.pub.TbParamUtil;
import nc.ms.tb.task.data.TCell;
import nc.ms.tb.task.data.TaskSheetDataModel;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.pubitf.para.SysInitQuery;
import nc.ui.bd.ref.RefInitializeCondition;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.tb.model.TBDataCellRefModel;
import nc.ui.tb.zior.TBSheetViewer;
import nc.ui.tb.zior.TbPlanContext;
import nc.ui.tb.zior.TbVarAreaUtil;
import nc.ui.tb.zior.pluginaction.edit.model.VarCellValueModel;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.cube.DimSectionTuple;
import nc.vo.mdm.cube.DimVector;
import nc.vo.mdm.dim.DimDef;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimDefPKConst;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.dim.MeasureUtil;
import nc.vo.mdm.integration.imp.MeasureTypeInfo;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.tb.form.excel.ExVarAreaDef;
import nc.vo.tb.form.excel.ExVarDef;
import nc.vo.tb.form.iufo.CellExtInfo;
import nc.vo.tb.form.iufo.TbIufoConst;
import nc.vo.tb.obj.LevelValueOfDimLevelVO;
import nc.vo.tb.task.MdTask;

import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.Cell;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;

public class CellContentUtil {
	// 浮动区区域map：key 浮动区ID，value 浮动区区域.
	public HashMap<String, AreaPosition> varAreaMap = new HashMap<String, AreaPosition>();
	// 区域浮动中合并区域对应的固定维 key varAreatype@区域浮动固定维所在列/行号 ，value cell.Getvalue().
	public Map<String, List> varColMap = new HashMap<String, List>();
	// 增行/列时复制单元格使用 key varID+行+@+列号 value CellInfo
	public Map<String, CellInfo> extInfo = new HashMap<String, CellInfo>();
	// 块浮动
	public Map<String, List<CellBlock>> mergeList = new HashMap<String, List<CellBlock>>();
	// 浮动维 key 行号+@+varID+@+列号
	public Map<String, LevelValueOfDimLevelVO> vos = new LinkedHashMap<String, LevelValueOfDimLevelVO>();
	public String SIGN = "@";
	public int addCunt = -1;
	public int maxIndex = -1;
	public TBSheetViewer tbSheetViewer;
	private int addType;
	public static int COLTYPE = 0;
	public static int ROWTYPE = 1;

	public static int ADDLINEUP = 0;
	public static int ADDLINEDOWN = 1;
	private int cellType;
	private int cellNum = -1;
	private String curVarID;
	private final static String paraName_startDeleteVarData = "TBB023";

	public CellContentUtil(TBSheetViewer tbSheetViewer) {
		super();
		this.tbSheetViewer = tbSheetViewer;
		try {
			List<Cell> cells = tbSheetViewer.getSelectedCell();
			Cell c = cells.get(cells.size() - 1);
			CellExtInfo cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
			cellType = cInfo.getExVarAreaDef().varAreaType;
			curVarID = cInfo.getVarId();
			initVarAreaInfo(cellType == ExVarAreaDef.varAreatType_ROW ? ROWTYPE
					: COLTYPE);
			ExVarAreaDef vardef = cInfo.getExVarAreaDef();
			if(vardef.isBlock == ExVarAreaDef.boolean_YES){
				addCunt = vardef.blockSize;
			}
		} catch (BusinessException e) {
			NtbLogger.error(e);
		}
	}

	private void containMergeArea(int firstNum, int lastNum, int rowNum,
			int type) {
		List<String> removeKey = new ArrayList<String>();
		if (mergeList.isEmpty()) {
			List<CellBlock> cbs = new ArrayList<CellBlock>();
			cbs.add(new CellBlock(rowNum, firstNum, lastNum));
			mergeList.put(firstNum + "@" + lastNum, cbs);
		} else {
			Set<String> keySet = mergeList.keySet();
			List<String> rmList = new ArrayList<String>();
			List<String> bakList = new ArrayList<String>(keySet);
			for (String areas : bakList) {
				String[] area = areas.split("@");
				int begin = Integer.parseInt(area[0]);
				int end = Integer.parseInt(area[1]);

				if ((firstNum < begin && lastNum > begin && lastNum < end)
						|| (firstNum > begin && firstNum < end && lastNum > end)) {
					// 删掉之前的整合
					removeKey.add(areas);
					int newBegin = firstNum < begin ? firstNum : begin;
					int newEnd = lastNum < end ? end : lastNum;
					String key = newBegin + "@" + newEnd;
					rmList.add(areas);

					List<CellBlock> cbs = mergeList.get(areas);
					cbs.add(new CellBlock(rowNum, firstNum, lastNum));
					mergeList.put(key, cbs);
					break;
				} else if ((firstNum > begin || firstNum == begin)
						&& (lastNum < end || lastNum == end)) {
					List<CellBlock> cbs = mergeList.get(areas);
					cbs.add(new CellBlock(rowNum, firstNum, lastNum));
					mergeList.put(areas, cbs);
					break;
				} else {
					int newBegin = firstNum < begin ? begin : firstNum;
					int newEnd = lastNum < end ? end : lastNum;
					String key = newBegin + "@" + newEnd;
					if (bakList.contains(key)) {
						List<CellBlock> cbs = mergeList.get(key);
						cbs.add(new CellBlock(rowNum, firstNum, lastNum));
						mergeList.put(key, cbs);
						break;
					} else {
						List<CellBlock> cbs = new ArrayList<CellBlock>();
						cbs.add(new CellBlock(rowNum, firstNum, lastNum));
						mergeList.put(key, cbs);
						break;
					}
				}
			}
			for (String rmKey : rmList) {
				mergeList.remove(rmKey);
			}
		}
	}

	private void appendMergeInfo(Cell cell) {
		AreaPosition ap = getCellsModel().getCombinedCellArea(
				CellPosition.getInstance(cell.getRow(), cell.getCol()));
		CellPosition[] cp = ap.split();
		if (cp.length >= 2) {
			CellPosition firstCp = cp[0];
			CellPosition lastCp = cp[cp.length - 1];
			CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
			int firstNum = -1, lastNum = -1, rowNum = -1;
			if (cInfo.getExVarAreaDef() != null
					&& cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_ROW) {
				firstNum = firstCp.getRow();
				lastNum = lastCp.getRow();
				rowNum = cell.getCol();
			} else if (cInfo.getExVarAreaDef() != null
					&& cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_COL) {
				firstNum = firstCp.getColumn();
				lastNum = lastCp.getColumn();
				rowNum = cell.getRow();
			}
			if (firstNum == -1 || lastNum == -1)
				return;
			containMergeArea(firstNum, lastNum, rowNum,
					cInfo.getExVarAreaDef().varAreaType);
		}
	}

	protected void initVarAreaInfo(int type) throws BusinessException {
		varColMap.clear();
		extInfo.clear();
		vos.clear();
		varAreaMap.clear();
		mergeList.clear();
		Map<String, Integer> varIdList = new HashMap<String, Integer>();
		CellsModel csModel = getCellsModel();
		int colNum = -1;
		int rowNum = -1;
		boolean isSameRow = false;
		String preVarId = "";
		// 取CellsModel的List<List<Cell>中的Cell时一定要清楚
		// 尤其是存在合并区时，List<List<Cell>中的Cell的Row和Col再某个瞬间是旧态，只有双击界面单元格后，或者其它repaint后
		// 才可以把Row和Col设置正确；所以，正确的做法往往是通过行列索引来查找Cell
		for (int i = 0; i < csModel.getRowNum(); i++) {
			colNum = -1;
			isSameRow = false;
			for (int j = 0; j < csModel.getColNum(); j++) {
				Cell c = csModel.getCell(i, j);
				if (c == null)
					continue;
				CellExtInfo cInfo = (CellExtInfo) c
						.getExtFmt(TbIufoConst.tbKey);
				if (cInfo != null) {
					String varId = cInfo.getExVarAreaDef() == null ? null
							: cInfo.getExVarAreaDef().varID;

					if (varId != null && !varIdList.keySet().contains(varId)) {
						if (cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_ROW)
							varIdList.put(varId, c.getCol());
						else if (cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_COL)
							varIdList.put(varId, c.getRow());
					}
					if (cInfo.getExVarAreaDef() != null) {
						if ((cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_ROW && type != ROWTYPE)
								|| (cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_COL && type != COLTYPE)) {
							continue;
						}
					}

					if(cInfo.getVarId() == null || cInfo.getVarId() != curVarID)
						continue; //modify 20150313

					// 读取浮动区范围
					if (cInfo.getVarId() != null) {
						AreaPosition ap = varAreaMap.get(cInfo.getVarId());
						if (ap == null) {
							ap = AreaPosition.getInstance(c.getRow(),
									c.getCol(), 1, 1);
						} else {
							ap = ap.getInstanceUnionWith(CellPosition
									.getInstance(c.getRow(), c.getCol()));
						}
						varAreaMap.put(cInfo.getVarId(), ap);
					}

					// 初始化合并信息
					appendMergeInfo(c);
					if (cInfo.getExVarAreaDef() == null)
						continue;
					ExVarDef exVarDef = null;
					int numKey = -1;

					if (!isSameRow) {
						if (preVarId == null
								|| !preVarId
										.equals(cInfo.getExVarAreaDef().varID)) {
							preVarId = cInfo.getExVarAreaDef().varID;
							rowNum = 0;
						} else
							rowNum++;
						isSameRow = true;
					}

					if (cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_ROW) {
						numKey = c.getCol();
						colNum++;
					} else {
						if (cInfo.getExVarAreaDef().varID.equals(preVarId)) {
							colNum++;
						} else {
							colNum = 0;
							preVarId = cInfo.getExVarAreaDef().varID;
						}
						preVarId = cInfo.getExVarAreaDef().varID;
						numKey = c.getRow();
					}

					for (ExVarDef def : cInfo.getExVarAreaDef().varDefList) {
						if (def.index == cInfo.getIndex()) {
							exVarDef = def;
							break;
						}
					}
					if (exVarDef == null)
						continue;
					// 读取只读区内容,为了在增删行时候正确添加/删除单元格
					if (exVarDef.cellType == ExVarDef.cellType_readonly) {
						Object value = c.getValue();
						if (mergeList != null && !mergeList.isEmpty()) {
							int begin = -1;
							List<CellBlock> cbList = null;
							for (String key : mergeList.keySet()) {
								String[] ms = key.split("@");
								begin = Integer.parseInt(ms[0]);
								int end = Integer.parseInt(ms[1]);
								if ((begin < c.getRow() || begin == c.getRow())
										&& (end > c.getRow() || end == c
												.getRow())) {
									cbList = mergeList.get(key);
									break;
								}
							}
							if (cbList != null) {
								for (CellBlock cb : cbList) {
									if ((cb.cellPosition < numKey || cb.cellPosition == numKey)
											&& begin != c.getRow()) {
										// numKey--;
										value = getCellsModel().getCellValue(
												c.getRow(), numKey);
									}
								}
							}
						}
						String key = cInfo.getExVarAreaDef().varAreaType + SIGN
								+ numKey;
						List<Object> list = varColMap.get(key);
						if (list == null)
							list = new ArrayList();

						if (/* !list.contains(value)&& */value != null)
							if (varId != null && varId.equals(curVarID)) {
								list.add(value);
							}

						varColMap.put(key, list);
						if (addCunt < list.size()) {
							// addCunt = list.size();
							maxIndex = numKey;
						}
					}
					// 添加弹出框内容选择范围
					if (exVarDef.cellType == ExVarDef.cellType_dim
							|| exVarDef.cellType == ExVarDef.cellType_strlist) {

						int num = varIdList.get(varId);
						int insertnum = num + exVarDef.index;
						MdTask task = tbSheetViewer.getViewManager()
								.getTbPlanContext().getTasks()[0];
						List<LevelValue> dlList = getDlList(exVarDef.dimLevelCode);
						vos.put(getCellKey(cInfo.getExVarAreaDef().varAreaType,
								varId, insertnum),
								new LevelValueOfDimLevelVO(
										insertnum,
										exVarDef.dimLevelCode,
										exVarDef.levelValueList == null ? null
												: exVarDef.levelValueList
														.toArray(new String[0]),
										dlList, task));
					}
					// 添加浮动单元格信息
					String key = null;
					int varType = cInfo.getExVarAreaDef().varAreaType;
					if (varType == ExVarAreaDef.varAreatType_ROW)
						key = cInfo.getExVarAreaDef().varID + rowNum + SIGN
								+ c.getCol();
					else
						key = cInfo.getExVarAreaDef().varID + c.getRow() + SIGN
								+ colNum;
					CellInfo ci = new CellInfo();
					ci.setCellExtInfo(cInfo);
					ci.setiFormat(c.getFormat());
					ci.setValue(c.getValue());
					extInfo.put(key, ci);
				}
			}
		}
		if (varColMap.isEmpty()) {
			colNum = 1;
			addCunt = 1;
		} else {
			for (String s : mergeList.keySet()) {
				String[] ms = s.split("@");
				int begin = Integer.parseInt(ms[0]);
				int end = Integer.parseInt(ms[1]);
				if (addCunt < (end - begin + 1)) {
					addCunt = end - begin + 1;
				}
			}
			if (addCunt == -1) {
				addCunt = 1;
			}
		}
	}

	// 取出额外的维度信息
	private List<LevelValue> getDlList(String dlCode) {

		if (dlCode.equalsIgnoreCase(IDimLevelCodeConst.MONTH)
				|| dlCode.equalsIgnoreCase(IDimLevelCodeConst.QUARTER)) {
			List<LevelValue> a = new ArrayList<LevelValue>();
			MdTask task = tbSheetViewer.getViewManager().getTbPlanContext()
					.getTasks()[0];
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

	public String getCellKey(int rowType, String varId, int colNum) {
		return rowType + SIGN + varId + SIGN + colNum;
	}

	protected CellsModel getCellsModel() {
		return tbSheetViewer.getCellsModel();
	}

	/**
	 * 文本浮动增多行
	 *
	 * @param type
	 *            （0，向上，1，向下）
	 * @param count
	 *            （增行数）
	 */
	public void addMultiLine(int type, int count) {
		this.addType = type;
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell cell = cells.get(cells.size() - 1);
		CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
		ExVarAreaDef vardef = cInfo.getExVarAreaDef();
		int varType = cInfo.getExVarAreaDef().varAreaType;
		if (varType == ExVarAreaDef.varAreatType_ROW) {
			cellNum = cell.getCol();
			String varId = cInfo.getVarId();
			int inSertIndex = getAddFirstRowIndex(cell, type);
			if(vardef.isBlock == ExVarAreaDef.boolean_YES){
				int blocksize = vardef.blockSize;
				//TaskSheetDataModel上增块
				int baseBeginRow = type==ADDLINEDOWN?inSertIndex-blocksize+1:inSertIndex;
				addVarAreaBlock(type, baseBeginRow, blocksize,count);
				//前端cellsModel增块
				addMultiTbRow(varId, inSertIndex, count);
			}else{
				addVarAreaRow(type, inSertIndex, count);
				addMultiTbRow(varId, inSertIndex, count);
			}
		}
	}

	private void addMultiTbRow(String varId, int inSertIndex, int count) {
		int colCount = tbSheetViewer.getCellsModel().getColNum();
		AreaPosition varPArea = varAreaMap.get(varId);
		int startCol = varPArea.getStart().getColumn();

		int addCount = /* count == 0 ? addCunt : */addCunt * count;
		int laCt = addCount;
		AreaPosition varArea = AreaPosition.getInstance(inSertIndex, startCol,
				tbSheetViewer.getCellsModel().getColNum(), addCount);
		Map<String, Object> map = bakCellsModel(inSertIndex);
		int allCount;
		if (cellType == ExVarAreaDef.varAreatType_ROW)
			allCount = addCount + getCellsModel().getRowNum() + addCount;
		else
			allCount = addCount + getCellsModel().getColNum() + addCount;
		// 增行
		tbSheetViewer.getCellsModel().getRowHeaderModel()
				.addHeader(inSertIndex, addCount);
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell selectedCell = cells.get(cells.size() - 1);
		String showText = null;
		int indexnum = -1;
		int lastNum = -1;
		int varType = -1;
		DimMember[] dms = null;
		for (int row = inSertIndex; row < inSertIndex + laCt; row++) {
			for (int i = 0; i < colCount; i++) {

				Cell c = getCellsModel().getCellIfNullNew(row, i);
				CellInfo cellInfo = extInfo.get(varId
						+ ((row - inSertIndex) % addCunt) + SIGN + i);
				int rown = (row - inSertIndex) % addCunt;
				CellExtInfo cellExtInfo = cloneAndSetExtInfo(c, cellInfo,
						showText, varType, rown, i);
				if (cellExtInfo == null)
					continue;
				if (varType == -1)
					varType = cellExtInfo.getExVarAreaDef().varAreaType;
				List<ExVarDef> defList = cellExtInfo.getExVarAreaDef().varDefList;
				// 可去掉
				for (ExVarDef def : defList) {
					if (def.index == cellExtInfo.getIndex()) {
						ExVarDef targetDef = def;
						if (targetDef.cellType == ExVarDef.cellType_index) {
							// 如果是序列类型，需要加上序号
							if (indexnum == -1)
								indexnum = i;
							Object obj = getCellsModel().getCell(row - 1, i)
									.getValue();
							lastNum = getIndexNum(obj);
						}
						break;
					}
				}

			}
		}
		// 合并单元格
		if (addCount != 1) {
			if (!this.mergeList.isEmpty()) {
				List<CellBlock> cbList = null;
				List<String> hadCombs = new ArrayList<String>();
				int insertCount = addCount / addCunt;
				for (String key : mergeList.keySet()) {
					cbList = mergeList.get(key);
					if (this.addType == ADDLINEUP) {
						for (int i = 0; i < insertCount; i++) {
							for (CellBlock cb : cbList) {
								AreaPosition ap = AreaPosition.getInstance(
										cb.blockStart + addCunt * (i),
										cb.cellPosition, 1, (cb.blockEnd
												- cb.blockStart + 1));
								if (!hadCombs.contains(ap.toString())) {
									getCellsModel().combineCell(ap);
								}
								hadCombs.add(ap.toString());
							}
						}
					} else {
						for (int i = 0; i < insertCount; i++) {
							for (CellBlock cb : cbList) {
								AreaPosition ap = AreaPosition.getInstance(
										cb.blockStart + addCunt * (i + 1),
										cb.cellPosition, 1, (cb.blockEnd
												- cb.blockStart + 1));
								if (!hadCombs.contains(ap.toString())) {
									getCellsModel().combineCell(ap);
								}
								hadCombs.add(ap.toString());
							}
						}

					}
				}
			}
		}
		VarCellValueModel varCellModel = new VarCellValueModel(varType,
				getCellsModel(), inSertIndex, selectedCell.getCol(), dms,
				addCunt);
		try {
			// 设置单元格的多维信息
			varCellModel.fireCellValueChaned();
		} catch (BusinessException be) {
			NtbLogger.error(be);
		}
		// 增行后填充
		fillfullCells(inSertIndex + laCt, allCount, map);
		AreaPosition ap = varAreaMap.get(varId);
		int bgRow = ap.getStart().getRow();
		lastNum = 0;
		while (indexnum != -1) {
			Cell nextc = getCellsModel().getCellIfNullNew(bgRow++, indexnum);// varId
			if (nextc == null)
				break;
			CellExtInfo currentCellInfo = (CellExtInfo) nextc
					.getExtFmt(TbIufoConst.tbKey);
			if (currentCellInfo != null && currentCellInfo.getVarId() != null
					&& currentCellInfo.getVarId().equals(varId)) {
				AreaPosition nextAp = getCellsModel()
						.getCombinedCellArea(
								CellPosition.getInstance(nextc.getRow(),
										nextc.getCol()));
				if (nextAp.split().length > 1) {
					int areaType = currentCellInfo.getExVarAreaDef().varAreaType;
					if (areaType == ExVarAreaDef.varAreatType_ROW) {
						if ((bgRow - nextAp.getStart().getRow())
								% (nextAp.split().length) == 0) {
							nextc.setValue(++lastNum);
						}
					} else if (areaType == ExVarAreaDef.varAreatType_COL) {
						if ((bgRow - nextAp.getStart().getColumn())
								% (nextAp.split().length) == 0) {
							nextc.setValue(++lastNum);
						}
					}
				} else
					nextc.setValue(++lastNum);
			} else
				indexnum = -1;
		}
	}

	public void addLine(int type) {

		this.addType = type;
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell cell = cells.get(cells.size() - 1);
		CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
		int varType = cInfo.getExVarAreaDef().varAreaType;
		int celNum = varType == ExVarAreaDef.varAreatType_ROW ? cell.getCol()
				: cell.getRow();
		LevelValueOfDimLevelVO vo = vos.get(getCellKey(varType,
				cInfo.getVarId(), celNum));
		if (varType == ExVarAreaDef.varAreatType_ROW) {
			cellNum = cell.getCol();
			if (vo == null || vo.dimlevelCode.equals("")) {
				addRow(type);
			} else {
				UIRefPane refPane = new UIRefPane("维度选择");/*-=notranslate=-*/
				refPane.setMultiSelectedEnabled(true);
				TBDataCellRefModel tBDataCellRefModel = (TBDataCellRefModel) refPane
						.getRefModel();
				ExVarDef exVarDef = TbVarAreaUtil.getVarDefByCellExtInfo(cInfo);
				refPane.setMultiSelectedEnabled(true);
				String pk_user = WorkbenchEnvironment.getInstance()
						.getLoginUser().getPrimaryKey();
				String pk_group = WorkbenchEnvironment.getInstance()
						.getGroupVO().getPrimaryKey();
				Map<DimLevel, LevelValue> dvMap = TbVarAreaUtil.getDVMap(cell, cInfo, exVarDef, tbSheetViewer.getCellsPane());
				try {
					TbVarAreaUtil.initTBDataCellRefModel(tBDataCellRefModel,
							vo, pk_user, pk_group, cInfo.getCubeCode(),
							exVarDef, null,dvMap);

				} catch (BusinessException e) {
					NtbLogger.print(e);
				}
				if( !ITbPlanActionCode.NOTSUPPORT.equals(TbParamUtil.isStartDeptByOrg())&&IDimLevelCodeConst.DEPT.equals(exVarDef.dimLevelCode)){
			        refPane.setMultiCorpRef(true);
			        RefInitializeCondition condition = refPane.getRefUIConfig().getRefFilterInitconds()[0];
			        condition.setDefaultPk(vo.task.getPk_dataent());
			        try {
						List<String> pks=DimServiceGetter.getIBDModeService().getCommsionOrg(vo.task.getPk_dataent());
						if(pks!=null&&pks.size()>0){
							condition.setFilterPKs(pks.toArray(new String[0]));
						}
					   } catch (BusinessException e) {
						NtbLogger.print(e.getMessage());
					   }
			        refPane.getRefUIConfig().setRefFilterInitconds(new RefInitializeCondition[] { condition });
				}
				if (IDimLevelCodeConst.MEASURE.equals(exVarDef.dimLevelCode)) {
					if (exVarDef.mesType != null
							&& MeasureUtil.getMeasureName(exVarDef.mesType
									.name()) != null) {
						tBDataCellRefModel.setDisplayDocName(exVarDef.mesType
								.name());
					} else {
						tBDataCellRefModel
								.setDisplayDocName(MeasureTypeInfo.tb_budgetsub
										.name());
						refPane.setMultiCorpRef(true);// 如果过滤参照要显示，并且参照要设置为单选，则此设置为True,isMultiOrgSelected设置为false就可以
						refPane.setMultiOrgSelected(true);
					}
				}
				refPane.getRefModel();
				refPane.showModel();
				if (refPane.getReturnButtonCode() == 1 && refPane.stopEditing()) {
					refPane.setButtonFireEvent(true);
					Object objs = tBDataCellRefModel.getSelectedData();
					List<Object> busiObjs = new ArrayList<Object>();
					if (objs != null && objs instanceof Vector) {
						Vector vector = (Vector) objs;
						if (vector.size() > 0) {
							for (Object obj : vector) {
								Vector vct = (Vector) obj;
								if (vo.dimlevelCode == null
										|| vo.dimlevelCode.equals("")) {
									if (vct != null && vct.size() > 0) {
										Object o = vct.get(0);
										busiObjs.add(o);
									}
								} else {
									if (vct != null && vct.size() > 0) {
										DimMember dimMember = tBDataCellRefModel
												.getDimMember((String) vct
														.get(2));
										busiObjs.add(dimMember);
									}
								}
							}
						}
						addRow(busiObjs.toArray(), type);
					}
				}
			}
		}
	}
	public void addEmptyLine(int type) {
		this.addType = type;
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell cell = cells.get(cells.size() - 1);
		CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
		int varType = cInfo.getExVarAreaDef().varAreaType;
		int celNum = varType == ExVarAreaDef.varAreatType_ROW ? cell.getCol()
				: cell.getRow();
		LevelValueOfDimLevelVO vo = vos.get(getCellKey(varType,
				cInfo.getVarId(), celNum));
		if (varType == ExVarAreaDef.varAreatType_ROW) {
			addRow(type);
		}
	}

	/**
	 * 扩展多维浮动区
	 *
	 * @param list
	 * @param type
	 *            （0，向上，1，向下）
	 */
	public void extendDimVarArea(List<DimMember> list, int type) {
		this.addType = type;
		int count = list.size();
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell cell = cells.get(cells.size() - 1);
		CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
		int varType = cInfo.getExVarAreaDef().varAreaType;
		if (varType == ExVarAreaDef.varAreatType_ROW) {
			cellNum = cell.getCol();
			String varId = cInfo.getVarId();
			int inSertIndex = getAddFirstRowIndex(cell, type);
			addRow(list.toArray(), type);
			// addVarAreaRow(type,inSertIndex, count);
			// addMultiTbRow(varId, inSertIndex, count);
		}
	}

	//块浮动增行
	public void addRow(int type) {
		Cell cell = tbSheetViewer.getSelectedCell().get(
				tbSheetViewer.getSelectedCell().size() - 1);
		CellExtInfo selectedCellInfo = (CellExtInfo) cell
				.getExtFmt(TbIufoConst.tbKey);
		ExVarAreaDef vardef = selectedCellInfo.getExVarAreaDef();
		String varId = selectedCellInfo.getVarId();
		int inSertIndex = getAddFirstRowIndex(cell, type);
		if(vardef.isBlock == ExVarAreaDef.boolean_YES && vardef.blockSize>1){
			int blocksize = vardef.blockSize;
			//TaskSheetDataModel上增块
			int baseIndex = type==ADDLINEDOWN?inSertIndex-blocksize+1:inSertIndex;
			addVarAreaBlock(type, baseIndex, blocksize,1);
			//前端cellsModel增块
			addTbBlock(varId, inSertIndex, null);
		}else{
			addVarAreaRow(type, inSertIndex, 1);
			addTbRow(varId, inSertIndex, null);
		}
	}

	public void addRow(Object[] objs, int type) {
		if (objs == null || objs.length == 0)
			return;
		Cell cell = tbSheetViewer.getSelectedCell().get(
				tbSheetViewer.getSelectedCell().size() - 1);
		CellExtInfo selectedCellInfo = (CellExtInfo) cell
				.getExtFmt(TbIufoConst.tbKey);
		ExVarAreaDef vardef = selectedCellInfo.getExVarAreaDef();
		String varId = selectedCellInfo.getVarId();
		int inSertIndex = getAddFirstRowIndex(cell, type);
		if(vardef.isBlock == ExVarAreaDef.boolean_YES && vardef.blockSize>1){
			int blocksize = vardef.blockSize;
			//TaskSheetDataModel上增块
			int baseIndex = type==ADDLINEDOWN?inSertIndex-blocksize+1:inSertIndex;
			addVarAreaBlock(type, baseIndex, blocksize,objs.length);
			//前端cellsModel增块
			addTbBlock(varId, inSertIndex, objs);
		}else{
			addVarAreaRow(type, inSertIndex, objs.length);
			addTbRow(varId, inSertIndex, objs);
		}
	}
	/**
	 *
	 * UI增加浮动块
	 * @param type
	 * @author: pengzhena@yonyou.com
	 */
	public void addTbBlock(String varId, int inSertIndex, Object[] objs) {
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell selectedCell = cells.get(cells.size() - 1);
		CellExtInfo selectedCellInfo = (CellExtInfo) selectedCell
				.getExtFmt(TbIufoConst.tbKey);
		ExVarAreaDef vardef = selectedCellInfo.getExVarAreaDef();
		int colCount = tbSheetViewer.getCellsModel().getColNum();
		AreaPosition varPArea = varAreaMap.get(varId);
		int varblocksize = vardef.blockSize;
		int startCol = varPArea.getStart().getColumn();
		int addCount = objs == null ? varblocksize : varblocksize * objs.length;
		int laCt = addCount;
//		AreaPosition varArea = AreaPosition.getInstance(inSertIndex, startCol,
//				tbSheetViewer.getCellsModel().getColNum(), addCount);
		Map<String, Object> map = bakCellsModel(inSertIndex);
		int allCount;
		if (cellType == ExVarAreaDef.varAreatType_ROW)
			allCount = addCount + getCellsModel().getRowNum() + addCount;
		else
			allCount = addCount + getCellsModel().getColNum() + addCount;
		// 增行
		tbSheetViewer.getCellsModel().getRowHeaderModel()
				.addHeader(inSertIndex, addCount);

		// 增行后填充数据
		int arrayCount = 0;
		String showText = null;
		int indexnum = -1;
		int lastNum = -1;
		int varType = -1;
		DimMember[] dms = null;
		String[] enumObjs = null;
		// 增行类型-1 无 ，1 维度 ， 2 枚举
		int addType = -1;
		if (objs != null) {
			if (objs[0] instanceof DimMember) {
				addType = 1;
				dms = new DimMember[objs.length];
				for (int i = 0; i < objs.length; i++) {
					dms[i] = (DimMember) objs[i];
				}
			} else {
				addType = 2;
				enumObjs = new String[objs.length];
				for (int i = 0; i < objs.length; i++) {
					enumObjs[i] = objs[i].toString();
				}
			}
		}

		for (int row = inSertIndex; row < inSertIndex + laCt; row++) {
			DimMember dm = null;
			if (addType != -1) {
				if ((row - inSertIndex) % addCunt == 0) {
					if (addType == 1) {
						dm = (DimMember) objs[arrayCount];
						showText = dm.getObjName();
					} else {
						showText = enumObjs[arrayCount];
					}
					arrayCount++;
				}
			}
			for (int i = 0; i < colCount; i++) {

				Cell c = getCellsModel().getCellIfNullNew(row, i);
				CellInfo cellInfo = extInfo.get(varId
						+ ((row - inSertIndex) % addCunt) + SIGN + i);
				int rown = (row - inSertIndex) % addCunt;
				CellExtInfo cellExtInfo = cloneAndSetExtInfo(c, cellInfo,
						showText, varType, rown, i);
				if (cellExtInfo == null)
					continue;
				if (varType == -1)
					varType = cellExtInfo.getExVarAreaDef().varAreaType;
				List<ExVarDef> defList = cellExtInfo.getExVarAreaDef().varDefList;
				// 可去掉
				for (ExVarDef def : defList) {
					if (def.index == cellExtInfo.getIndex()) {
						ExVarDef targetDef = def;
						if (targetDef.cellType == ExVarDef.cellType_index) {
							// 如果是序列类型，需要加上序号
							if (indexnum == -1)
								indexnum = i;
							Object obj = getCellsModel().getCell(row - 1, i)
									.getValue();
							lastNum = getIndexNum(obj);
						}
						break;
					}
				}
			}
		}
		// 合并单元格
		if (addCount != 1) {
			if (!this.mergeList.isEmpty()) {
				List<CellBlock> cbList = null;
				List<String> hadCombs = new ArrayList<String>();
				int insertCount = addCount / addCunt;
				for (String key : mergeList.keySet()) {
					cbList = mergeList.get(key);
					if (this.addType == ADDLINEUP) {
						for (int i = 0; i < insertCount; i++) {
							for (CellBlock cb : cbList) {
								AreaPosition ap = AreaPosition.getInstance(
										cb.blockStart + addCunt * (i),
										cb.cellPosition, 1, (cb.blockEnd
												- cb.blockStart + 1));
								if (!hadCombs.contains(ap.toString())) {
									getCellsModel().combineCell(ap);
								}
								hadCombs.add(ap.toString());
							}
						}
					} else {
						for (int i = 0; i < insertCount; i++) {
							for (CellBlock cb : cbList) {
								AreaPosition ap = AreaPosition.getInstance(
										cb.blockStart + addCunt * (i + 1),
										cb.cellPosition, 1, (cb.blockEnd
												- cb.blockStart + 1));
								if (!hadCombs.contains(ap.toString())) {
									getCellsModel().combineCell(ap);
								}
								hadCombs.add(ap.toString());
							}
						}

					}
				}
			}
		}
		VarCellValueModel varCellModel = new VarCellValueModel(varType,
				getCellsModel(), inSertIndex, selectedCell.getCol(), dms,
				addCunt);
		try {
			// 设置单元格的多维信息
			varCellModel.fireCellValueChaned();
		} catch (BusinessException be) {
			NtbLogger.error(be);
		}
		// 增行后填充
		fillfullCells(inSertIndex + laCt, allCount, map);

		AreaPosition ap = varAreaMap.get(varId);
		int bgRow = ap.getStart().getRow();
		lastNum = 0;
		while (indexnum != -1) {
			Cell nextc = getCellsModel().getCellIfNullNew(bgRow++, indexnum);// varId
			if (nextc == null)
				break;
			CellExtInfo currentCellInfo = (CellExtInfo) nextc
					.getExtFmt(TbIufoConst.tbKey);
			if (currentCellInfo != null && currentCellInfo.getVarId() != null
					&& currentCellInfo.getVarId().equals(varId)) {
				AreaPosition nextAp = getCellsModel()
						.getCombinedCellArea(
								CellPosition.getInstance(nextc.getRow(),
										nextc.getCol()));
				if (nextAp.split().length > 1) {
					int areaType = currentCellInfo.getExVarAreaDef().varAreaType;
					if (areaType == ExVarAreaDef.varAreatType_ROW) {
						if ((bgRow - nextAp.getStart().getRow())
								% (nextAp.split().length) == 0) {
							nextc.setValue(++lastNum);
						}
					} else if (areaType == ExVarAreaDef.varAreatType_COL) {
						if ((bgRow - nextAp.getStart().getColumn())
								% (nextAp.split().length) == 0) {
							nextc.setValue(++lastNum);
						}
					}
				} else
					nextc.setValue(++lastNum);
			} else
				indexnum = -1;
		}
	}
	/**
	 * TaskSheetDataModel  后台model上增行
	 * @param type
	 * @author: pengzhena@yonyou.com
	 */
	private void addVarAreaBlock(int type, int baseBeginRow,int blocksize, int blockCount) {
		TaskSheetDataModel taskSheetDataModel = tbSheetViewer.getTsDataModel();
		if(type == ADDLINEUP)
			taskSheetDataModel.addRow(baseBeginRow,blocksize, blockCount,false,true);
		else
			taskSheetDataModel.addRow(baseBeginRow - 1,blocksize, blockCount,true,true);
		taskSheetDataModel.setSheetDataChanged(true);
	}
	// 备份数据
	private Map<String, Object> bakCellsModel(int inSertIndex) {
		Map<String, Object> valueMap = new HashMap<String, Object>();
		if (cellType == ExVarAreaDef.varAreatType_ROW) {
			for (int row = inSertIndex; row < getCellsModel().getRowNum(); row++) {
				for (int col = 0; col < getCellsModel().getColNum(); col++) {
					Cell cell = getCellsModel().getCell(row, col);
					if (cell != null && cell.getValue() != null)
						valueMap.put(row - inSertIndex + "@" + col,
								cell.getValue());
				}
			}
		} else {
			for (int col = inSertIndex; col < getCellsModel().getColNum(); col++) {
				for (int row = 0; row < getCellsModel().getRowNum(); row++) {
					Cell cell = getCellsModel().getCell(row, col);

					if (cell != null && cell.getValue() != null)
						valueMap.put(row + "@" + (col - inSertIndex),
								cell.getValue());
				}
			}
		}
		return valueMap;
	}

/**
 *
 * UI端的增行
 * @param varId
 * @param inSertIndex
 * @param objs
 */
	private void addTbRow(String varId, int inSertIndex, Object[] objs) {
		int colCount = tbSheetViewer.getCellsModel().getColNum();
		AreaPosition varPArea = varAreaMap.get(varId);
		int startCol = varPArea.getStart().getColumn();
		int addCount = objs == null ? addCunt : addCunt * objs.length;
		int laCt = addCount;
		AreaPosition varArea = AreaPosition.getInstance(inSertIndex, startCol,
				tbSheetViewer.getCellsModel().getColNum(), addCount);
		Map<String, Object> map = bakCellsModel(inSertIndex);
		int allCount;
		if (cellType == ExVarAreaDef.varAreatType_ROW)
			allCount = addCount + getCellsModel().getRowNum() + addCount;
		else
			allCount = addCount + getCellsModel().getColNum() + addCount;
		// 增行
		tbSheetViewer.getCellsModel().getRowHeaderModel()
				.addHeader(inSertIndex, addCount);
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell selectedCell = cells.get(cells.size() - 1);
		// 增行后填充数据
		int arrayCount = 0;
		String showText = null;
		int indexnum = -1;
		int lastNum = -1;
		int varType = -1;
		DimMember[] dms = null;
		String[] enumObjs = null;
		// 增行类型-1 无 ，1 维度 ， 2 枚举
		int addType = -1;
		if (objs != null) {
			if (objs[0] instanceof DimMember) {
				addType = 1;
				dms = new DimMember[objs.length];
				for (int i = 0; i < objs.length; i++) {
					dms[i] = (DimMember) objs[i];
				}
			} else {
				addType = 2;
				enumObjs = new String[objs.length];
				for (int i = 0; i < objs.length; i++) {
					enumObjs[i] = objs[i].toString();
				}
			}
		}

		for (int row = inSertIndex; row < inSertIndex + laCt; row++) {
			DimMember dm = null;
			if (addType != -1) {
				if ((row - inSertIndex) % addCunt == 0) {
					if (addType == 1) {
						dm = (DimMember) objs[arrayCount];
						showText = dm.getObjName();
					} else {
						showText = enumObjs[arrayCount];
					}
					arrayCount++;
				}
			}
			for (int i = 0; i < colCount; i++) {

				Cell c = getCellsModel().getCellIfNullNew(row, i);
				CellInfo cellInfo = extInfo.get(varId
						+ ((row - inSertIndex) % addCunt) + SIGN + i);
				int rown = (row - inSertIndex) % addCunt;
				CellExtInfo cellExtInfo = cloneAndSetExtInfo(c, cellInfo,
						showText, varType, rown, i);
				if (cellExtInfo == null)
					continue;
				if (varType == -1)
					varType = cellExtInfo.getExVarAreaDef().varAreaType;
				List<ExVarDef> defList = cellExtInfo.getExVarAreaDef().varDefList;
				// 可去掉
				for (ExVarDef def : defList) {
					if (def.index == cellExtInfo.getIndex()) {
						ExVarDef targetDef = def;
						if (targetDef.cellType == ExVarDef.cellType_index) {
							// 如果是序列类型，需要加上序号
							if (indexnum == -1)
								indexnum = i;
							Object obj = getCellsModel().getCell(row - 1, i)
									.getValue();
							lastNum = getIndexNum(obj);
						}
						break;
					}
				}
			}
		}
		// 合并单元格
		if (addCount != 1) {
			if (!this.mergeList.isEmpty()) {
				List<CellBlock> cbList = null;
				List<String> hadCombs = new ArrayList<String>();
				int insertCount = addCount / addCunt;
				for (String key : mergeList.keySet()) {
					cbList = mergeList.get(key);
					if (this.addType == ADDLINEUP) {
						for (int i = 0; i < insertCount; i++) {
							for (CellBlock cb : cbList) {
								AreaPosition ap = AreaPosition.getInstance(
										cb.blockStart + addCunt * (i),
										cb.cellPosition, 1, (cb.blockEnd
												- cb.blockStart + 1));
								if (!hadCombs.contains(ap.toString())) {
									getCellsModel().combineCell(ap);
								}
								hadCombs.add(ap.toString());
							}
						}
					} else {
						for (int i = 0; i < insertCount; i++) {
							for (CellBlock cb : cbList) {
								AreaPosition ap = AreaPosition.getInstance(
										cb.blockStart + addCunt * (i + 1),
										cb.cellPosition, 1, (cb.blockEnd
												- cb.blockStart + 1));
								if (!hadCombs.contains(ap.toString())) {
									getCellsModel().combineCell(ap);
								}
								hadCombs.add(ap.toString());
							}
						}

					}
				}
			}
		}
		VarCellValueModel varCellModel = new VarCellValueModel(varType,
				getCellsModel(), inSertIndex, selectedCell.getCol(), dms,
				addCunt);
		try {
			// 设置单元格的多维信息
			varCellModel.fireCellValueChaned();
		} catch (BusinessException be) {
			NtbLogger.error(be);
		}
		// 增行后填充
		fillfullCells(inSertIndex + laCt, allCount, map);

		AreaPosition ap = varAreaMap.get(varId);
		int bgRow = ap.getStart().getRow();
		lastNum = 0;
		while (indexnum != -1) {
			Cell nextc = getCellsModel().getCellIfNullNew(bgRow++, indexnum);// varId
			if (nextc == null)
				break;
			CellExtInfo currentCellInfo = (CellExtInfo) nextc
					.getExtFmt(TbIufoConst.tbKey);
			if (currentCellInfo != null && currentCellInfo.getVarId() != null
					&& currentCellInfo.getVarId().equals(varId)) {
				AreaPosition nextAp = getCellsModel()
						.getCombinedCellArea(
								CellPosition.getInstance(nextc.getRow(),
										nextc.getCol()));
				if (nextAp.split().length > 1) {
					int areaType = currentCellInfo.getExVarAreaDef().varAreaType;
					if (areaType == ExVarAreaDef.varAreatType_ROW) {
						if ((bgRow - nextAp.getStart().getRow())
								% (nextAp.split().length) == 0) {
							nextc.setValue(++lastNum);
						}
					} else if (areaType == ExVarAreaDef.varAreatType_COL) {
						if ((bgRow - nextAp.getStart().getColumn())
								% (nextAp.split().length) == 0) {
							nextc.setValue(++lastNum);
						}
					}
				} else
					nextc.setValue(++lastNum);
			} else
				indexnum = -1;
		}
	}

	private CellExtInfo cloneAndSetExtInfo(Cell c, CellInfo cellInfo,
			String showText, int varType, int rowNum, int colNum) {
		if (cellInfo == null)
			return null;
		CellExtInfo baseExtInfo = cellInfo.getCellExtInfo();
		CellExtInfo cellExtInfo = null;
		try {
			cellExtInfo = baseExtInfo.cloneNew();
		} catch (CloneNotSupportedException e) {
			NtbLogger.error(e);
		}
		if (cellExtInfo.getDimVector() != null) {
			CubeDef cubeDef = null;
			try {
				cubeDef = CubeServiceGetter.getCubeDefQueryService()
						.queryCubeDefByBusiCode(cellExtInfo.getCubeCode());
			} catch (BusinessException e) {
				NtbLogger.print(e);
			}
			IDimManager idm = DimServiceGetter.getDimManager();
			for (String s : vos.keySet()) {
				LevelValueOfDimLevelVO dlCode = vos.get(s);
				DimLevel dl = idm.getDimLevelByBusiCode(dlCode.dimlevelCode);
				if (dl == null)
					continue;
				DimMember dimMember = cubeDef.getDimHierarchy(dl.getDimDef())
						.getAllMember();
				if (dimMember != null) {
					cellExtInfo.setDimVector(cellExtInfo.getDimVector()
							.addOrReplaceDimMember(dimMember));
				}else{
					cellExtInfo.setDimVector(cellExtInfo.getDimVector().removeDimDef(dl.getDimDef()));
				}
			}
		}
		cellExtInfo.setIndex(baseExtInfo.getIndex());
		c.setExtFmt(TbIufoConst.tbKey, cellExtInfo);
		c.setFormat(cellInfo.getiFormat());
		Object value = null;
		if (varType == -1)
			varType = cellExtInfo.getExVarAreaDef().varAreaType;
		if ((varType == ExVarAreaDef.varAreatType_ROW && colNum == this.cellNum)
				|| (varType == ExVarAreaDef.varAreatType_COL && rowNum == this.cellNum))
			value = showText;
		if (varColMap.keySet().contains(
				ExVarAreaDef.varAreatType_ROW + SIGN + c.getCol())) {
			List list = varColMap.get(ExVarAreaDef.varAreatType_ROW + SIGN
					+ c.getCol());
			if (list != null && !list.isEmpty())
				value = list.get(rowNum % addCunt);
		}
		c.setValue(value);
		cellExtInfo.setNewLine(true);
//		cellExtInfo.setDirty(true);
		return cellExtInfo;
	}

	private void fillfullCells(int insertIndex, int allCellsCount,
			Map<String, Object> valueMap) {
		if (cellType == ExVarAreaDef.varAreatType_ROW) {
			for (int row = insertIndex; row < allCellsCount; row++) {
				for (int col = 0; col < getCellsModel().getColNum(); col++) {
					Object obj = valueMap.get(row - insertIndex + "@" + col);
					Cell cell = getCellsModel().getCellIfNullNew(row, col);
					if (cell == null)
						continue;
					if (obj != null) {
						cell.setValue(obj);
					} else
						cell.setValue(null);
				}
			}
		} else {
			for (int col = insertIndex; col < allCellsCount; col++) {
				for (int row = 0; row < getCellsModel().getRowNum(); row++) {
					Object obj = valueMap.get(row + "@" + (col - insertIndex));
					if (obj != null) {
						getCellsModel().getCell(row, col).setValue(obj);
					} else {
						Cell c = getCellsModel().getCell(row, col);
						if (c == null)
							continue;
						getCellsModel().getCell(row, col).setValue(null);
					}
				}
			}
		}
	}

	private int getIndexNum(Object obj) {
		if (obj == null)
			return 1;
		else if (obj instanceof Integer) {
			return ((Integer) obj).intValue() + 1;
		} else if (obj instanceof String) {
			try {
				return Integer.parseInt((String) obj) + 1;
			} catch (Exception ex) {
				return 1;
			}
		}
		return 1;
	}
/**
 * TaskSheetDataModel  后台model上增行
 * @param type
 * @param inSertIndex
 * @param addSize
 */
	private void addVarAreaRow(int type, int inSertIndex, int addSize) {
		TaskSheetDataModel taskSheetDataModel = tbSheetViewer.getTsDataModel();
		if(type == ADDLINEUP)
			taskSheetDataModel.addRow(inSertIndex,1, addCunt * addSize,false,true);
		else
			taskSheetDataModel.addRow(inSertIndex - 1,1, addCunt * addSize,true,true);
		taskSheetDataModel.setSheetDataChanged(true);
	}

	// 得到要删除的行数
	private int getAddFirstRowIndex(Cell cell, int type) {
		if (!this.mergeList.isEmpty()) {
			int row = cell.getRow();
			int returnValue = -1;
			for (String key : mergeList.keySet()) {
				String[] ms = key.split("@");
				int begin = Integer.parseInt(ms[0]);
				int end = Integer.parseInt(ms[1]);
				if ((begin < row || begin == row) && (end > row || end == row)) {
					// addCunt = end - begin + 1;
					returnValue = type == ADDLINEDOWN ? end + 1 : begin;
					break;
				}
			}
			return returnValue;
		} else {
			if (addCunt == 1) {
				return type == ADDLINEDOWN ? cell.getRow() + 1 : cell.getRow();
			} else {
				CellsModel cellsModel = tbSheetViewer.getCellsModel();
				Cell markCell = cellsModel.getCell(cell.getRow(), maxIndex);
				List colList = varColMap.get(ExVarAreaDef.varAreatType_ROW
						+ SIGN + maxIndex);
				int index = colList.indexOf(markCell.getValue());
				return type == ADDLINEDOWN ? cell.getRow() - index + addCunt
						: cell.getRow() - index + addCunt - 1;
			}
		}
	}
	// 删行

	public void deleteRow(Cell cell, int size) {
		int[] inSertIndex = getDeleteFirstRowIndex(cell, size);
		boolean isAllCell = isAllCell(cell, size, ROWTYPE);
		deleteVarAreaRow(inSertIndex[0], inSertIndex[1], isAllCell);
		deleteTbRow(cell, inSertIndex[0], inSertIndex[1], isAllCell);
	}

	private boolean isAllCell(Cell cell, int size, int type) {

		CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
		AreaPosition ap = varAreaMap.get(cInfo.getVarId());
		int cellSize = -1;
		if (COLTYPE == type) {
			cellSize = ap.getEnd().getColumn() - ap.getStart().getColumn() + 1;
		} else
			cellSize = ap.getEnd().getRow() - ap.getStart().getRow() + 1;
		return cellSize == size;
	}

	private void deleteVarAreaRow(int firstRow, int size, boolean isAllCell) {
		Cell cell = tbSheetViewer.getSelectedCell().get(
				tbSheetViewer.getSelectedCell().size() - 1);
		CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
		AreaPosition varArea = varAreaMap.get(cInfo.getVarId());
		boolean isLastMergeRow = false;
		if (addCunt == 0) {
			return;
		}
		if (addCunt > 1) {
			isLastMergeRow = (varArea.getHeigth() - size == 0 ? true : false);
		}
		TaskSheetDataModel taskSheetDataModel = tbSheetViewer.getTsDataModel();
		List<List<TCell>> tCells = taskSheetDataModel.getCells();
		List<TCell> deleteTcells = new ArrayList<TCell>();
		try {
			UFBoolean bb = SysInitQuery.getParaBoolean(IDimPkConst.PK_GLOBE,
					paraName_startDeleteVarData);
			if (bb.booleanValue()) {
				for (int m = 0; m < size / addCunt; m++) {
					if (addCunt > 1) {
						for (int i = 0; i < addCunt; i++) {
							deleteTcells.addAll(getDeleteAllVarDimsTs(tCells
									.get(firstRow + i + m * addCunt),firstRow + i + m * addCunt));
						}
					} else {
						deleteTcells.addAll(getDeleteAllVarDimsTs(tCells
								.get(firstRow + m),firstRow + m));
					}
				}

				taskSheetDataModel.getParentModel().addVarRemoveDataCells(
						deleteTcells, cInfo.getCubeCode());
			}
		} catch (BusinessException e) {
			NtbLogger.error(e);
		}
		if (isLastMergeRow) {
			size = (size / addCunt - 1) * addCunt;
			taskSheetDataModel.addRow(firstRow, -size);
		} else {
			taskSheetDataModel.addRow(firstRow, -size + (isAllCell ? 1 : 0));
		}
		if (isLastMergeRow || isAllCell) {
			IDimManager idm = DimServiceGetter.getDimManager();
			DimDef dimDef = null;
			DimLevel dimLevel = null;
			for (List<TCell> ts : tCells) {
				for (TCell t : ts) {
					if (t == null || t.getDimVector() == null
							|| t.getVarId() == null)
						continue;
					if(!t.getVarId().equals(curVarID))
						continue;
					for (String s : vos.keySet()) {
						String dimlevelcode = vos.get(s).dimlevelCode;
						dimLevel = idm.getDimLevelByBusiCode(dimlevelcode);
						if(dimLevel == null){
							t.setValue(null);
							continue;
						}
						dimDef = dimLevel.getDimDef();
						if (t.getDimVector().containsDimDef(dimDef)) {
							t.setDatacell(null);
							//将缓存中的数据也需要更新一下
							HashMap<DimVector, DataCell> hashMap = taskSheetDataModel.getParentModel().getDatacellMap().get(cInfo.getCubeCode());
							if(hashMap != null){hashMap.remove(t.getDimVector());}
							t.setDimVector(t.getDimVector()
									.removeDimDef(dimDef));
							t.setValue(null);
						}
					}
				}
			}
		}
		taskSheetDataModel.setSheetDataChanged(true);
	}

	private List<TCell> getDeleteAllVarDimsTs(List<TCell> ts,int row) {
		List<TCell> tcells = new ArrayList<TCell>();
		boolean isAllVarDimsEdit = true;
		boolean canDel = false;
		IDimManager idm = DimServiceGetter.getDimManager();
		DimDef dimDef = null;
		DimLevel dimLevel = null;
		TbPlanContext context = tbSheetViewer.getTbPlanContext();
		for (TCell t : ts) {
			if (t == null || t.getDimVector() == null || t.getVarId() == null
					|| t.getValue() == null)
				continue;
			// 判断可变维是否为空
			isAllVarDimsEdit = true;
			canDel = false;
			for (String s : vos.keySet()) {
				String dimlevelcode = vos.get(s).dimlevelCode;
				dimLevel = idm.getDimLevelByBusiCode(dimlevelcode);
				if(dimLevel == null) continue;
				dimDef = dimLevel.getDimDef();
				if (!t.getDimVector().containsDimDef(dimDef)
						|| t.getDimVector().getDimMember(dimDef).isAllMember()) {
					isAllVarDimsEdit = false;
					break;
				}
			}
			if(TbVarAreaUtil.canDeleteData(context.getNodeType(), tbSheetViewer.getCellsModel(),
					tbSheetViewer.getTsDataModel(), row, t.getCol(), context.getCanEditMvtypes()))
				canDel = true;
			if (isAllVarDimsEdit&&canDel) {
				if(!isRepeatedCell(t))
					tcells.add(t);
			}
		}
		return tcells;
	}
	private boolean isRepeatedCell(TCell tcell){
		AreaPosition varArea = varAreaMap.get(tcell.getVarId());
		TaskSheetDataModel taskSheetDataModel = tbSheetViewer.getTsDataModel();
		for(int row = varArea.getStart().getRow();row <= varArea.getEnd().getRow();row ++){
//			if(row == tcell.getRow())
//				continue;
			for(int col = varArea.getStart().getColumn();col <= varArea.getEnd().getColumn();col ++){
				if(row == tcell.getRow() && col == tcell.getCol())
					continue;
				TCell t = taskSheetDataModel.getCellAt(row, col);
				if (t == null || t.getDimVector() == null || t.getVarId() == null)
					continue;
				if(t.getDimVector().equals(tcell.getDimVector())){
					return true;
				}
			}
		}
		return false;
	}
	private void deleteTbRow(Cell firstCell, int firstRow, int size,
			boolean isAllCell) {

		Cell cell = tbSheetViewer.getSelectedCell().get(
				tbSheetViewer.getSelectedCell().size() - 1);
		CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
		AreaPosition varArea = varAreaMap.get(cInfo.getVarId());
		boolean isLastMergeRow = false;
		if (addCunt == 0) {
			return;
		}
		if (addCunt > 1) {
			isLastMergeRow = (varArea.getHeigth() - size == 0 ? true : false);
		}
		if (isLastMergeRow) {
			tbSheetViewer.getCellsModel().getRowHeaderModel()
					.removeHeader(firstRow, (size / addCunt - 1) * addCunt);
			int rowNum = firstRow;
			for (int i = 0; i < tbSheetViewer.getCellsModel().getColNum(); i++) {
				for (int j = firstRow; j < addCunt + firstRow; j++) {
					Cell c = tbSheetViewer.getCellsModel().getCell(j, i);
					if (c == null)
						continue;

					CellExtInfo Info = (CellExtInfo) c
							.getExtFmt(TbIufoConst.tbKey);
					if (Info != null && Info.getExVarDef() != null
							&& Info.getExVarDef().levelValueList != null) {
						continue;
					}
					if (Info != null && Info.getVarId() != null
							&& Info.getDimVector() != null) {
						for (String s : vos.keySet()) {
							IDimManager iDimManager = DimServiceGetter
									.getDimManager();
							DimLevel dl = iDimManager.getDimLevelByBusiCode(vos
									.get(s).dimlevelCode);
							if(dl == null)	continue;
							Info.setDimVector(Info.getDimVector().removeDimDef(
									dl.getDimDef()));
						}
						if (Info.getVarDimDef() != null) {
							Info.setDimSectionTuple(null);
						}
					}
					if(Info != null && Info.getVarId() != null && Info.getExVarDef().cellType == ExVarDef.cellType_readonly){
						continue;
					}
					if (c != null)
						c.setValue(null);
				}
			}
			tbSheetViewer.validate();
		} else if (isAllCell && addCunt <= 1) {
			tbSheetViewer.getCellsModel().getRowHeaderModel()
					.removeHeader(firstRow, size - 1);
			int rowNum = firstRow;
			for (int i = 0; i < tbSheetViewer.getCellsModel().getColNum(); i++) {
				Cell c = tbSheetViewer.getCellsModel().getCell(rowNum, i);
				if (c == null)
					continue;
				if (c != null)
					c.setValue(null);

				CellExtInfo Info = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
				if (Info != null && Info.getVarId() != null
						&& Info.getDimVector() != null) {
					for (String s : vos.keySet()) {
						IDimManager iDimManager = DimServiceGetter
								.getDimManager();
						DimLevel dl = iDimManager.getDimLevelByBusiCode(vos
								.get(s).dimlevelCode);
						if(dl == null)	continue;
						Info.setDimVector(Info.getDimVector().removeDimDef(
								dl.getDimDef()));
					}
					if (Info.getVarDimDef() != null) {
						Info.setDimSectionTuple(null);
					}
				}
				if (c != null)
					c.setValue(null);
			}
			tbSheetViewer.validate();
		} else
			tbSheetViewer.getCellsModel().getRowHeaderModel()
					.removeHeader(firstRow, size);
		appendNum(firstCell);
	}

	private void appendNum(Cell cell) {
		if (cell == null)
			return;
		CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
		AreaPosition ap = varAreaMap.get(cInfo.getVarId());
		int bgRow = -1;
		int lastNum = 0;
		int num = -1;
		int indexnum = -1;
		if (cInfo == null || cInfo.getExVarAreaDef() == null)
			return;
		if (cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_ROW) {
			num = tbSheetViewer.getCellsModel().getColNum();
			bgRow = ap.getStart().getRow();
			for (int i = 0; i < num; i++) {
				Cell targetCell = tbSheetViewer.getCellsModel().getCell(
						cell.getRow(), i);
				if (targetCell == null)
					continue;
				CellExtInfo targetCInfo = (CellExtInfo) targetCell
						.getExtFmt(TbIufoConst.tbKey);
				if (targetCInfo == null
						|| targetCInfo.getExVarAreaDef() == null
						|| targetCInfo.getExVarAreaDef().varDefList == null)
					continue;
				ExVarDef exVarDef = getExVarDefOfCell(
						targetCInfo.getExVarAreaDef().varDefList,
						targetCInfo.getIndex());
				if (exVarDef.cellType == ExVarDef.cellType_index) {
					indexnum = i;
					break;
				}
			}
		} else if (cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_COL) {
			num = tbSheetViewer.getCellsModel().getRowNum();
			bgRow = ap.getStart().getColumn();
			for (int i = 0; i < num; i++) {
				Cell targetCell = tbSheetViewer.getCellsModel().getCell(i,
						cell.getCol());
				if (targetCell == null)
					continue;
				CellExtInfo targetCInfo = (CellExtInfo) targetCell
						.getExtFmt(TbIufoConst.tbKey);
				if (targetCInfo == null
						|| targetCInfo.getExVarAreaDef() == null
						|| targetCInfo.getExVarAreaDef().varDefList == null)
					continue;
				ExVarDef exVarDef = getExVarDefOfCell(
						targetCInfo.getExVarAreaDef().varDefList,
						targetCInfo.getIndex());
				if (exVarDef.cellType == ExVarDef.cellType_index) {
					indexnum = i;
					break;
				}
			}
		}
		while (indexnum != -1) {
			Cell nextc = null;
			if (cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_ROW)
				nextc = getCellsModel().getCellIfNullNew(bgRow++, indexnum);// varId
			else if (cInfo.getExVarAreaDef().varAreaType == ExVarAreaDef.varAreatType_COL)
				nextc = getCellsModel().getCellIfNullNew(indexnum, bgRow++);// varId
			if (nextc == null)
				break;
			CellExtInfo currentCellInfo = (CellExtInfo) nextc
					.getExtFmt(TbIufoConst.tbKey);
			if (currentCellInfo != null && currentCellInfo.getVarId() != null
					&& currentCellInfo.getVarId().equals(cInfo.getVarId())) {
				{
					AreaPosition nextAp = getCellsModel().getCombinedCellArea(
							CellPosition.getInstance(nextc.getRow(),
									nextc.getCol()));
					if (nextAp.split().length > 1) {
						int areaType = currentCellInfo.getExVarAreaDef().varAreaType;
						if (areaType == ExVarAreaDef.varAreatType_ROW) {
							if ((bgRow - nextAp.getStart().getRow())
									% (nextAp.split().length) == 0) {
								nextc.setValue(++lastNum);
							}
						} else if (areaType == ExVarAreaDef.varAreatType_COL) {
							if (nextc.getRow() == nextAp.getStart().getColumn()) {
								nextc.setValue(++lastNum);
							}
						}
					} else	nextc.setValue(++lastNum);
				}
			} else 	indexnum = -1;
		}
	}

	private ExVarDef getExVarDefOfCell(List<ExVarDef> varDefList, int index) {
		ExVarDef exVarDef = null;
		for (ExVarDef def : varDefList) {
			if (def.index == index) {
				exVarDef = def;
				break;
			}
		}
		return exVarDef;
	}

	private int[] getDeleteFirstRowIndex(Cell cell, int size) {
		int[] returnInfo = new int[2];
		if (addCunt == 1) {
			returnInfo[0] = cell.getRow();
			returnInfo[1] = size;
		} else {
			CellsModel cellsModel = tbSheetViewer.getCellsModel();
//			Cell markCell = cellsModel.getCell(cell.getRow(), maxIndex);
//			List colList = varColMap.get(ExVarAreaDef.varAreatType_ROW + SIGN
//					+ maxIndex);
//			int index = colList.indexOf(markCell.getValue());
			int first = cell.getRow();
			int last = first + size - 1;
			int start = -1;
			int end = -1;
			int sizeStart = -1, sizeEnd = -1;
			int mergeAreaCount = mergeList.size();
			if (mergeAreaCount > 0) {
				for (String s : mergeList.keySet()) {
					if (sizeStart != -1 && sizeEnd != -1) {
						break;
					}
					start = Integer.parseInt(s.split(SIGN)[0]);
					end = Integer.parseInt(s.split(SIGN)[1]);
					if ((end - start + 1) != addCunt) {
						continue;
					}
					if (first <= end && first >= start) {
						sizeStart = start;
					}
					if (last <= end && last >= start) {
						sizeEnd = end;
					}
				}
			}
			returnInfo[0] = sizeStart;
			returnInfo[1] = sizeEnd - sizeStart + 1;
		}
		return returnInfo;
	}

	/**
	 * 取浮动填充，调用原填充方法，按取浮动的顺序填充（先按浮动增多行进行增行）
	 * @param map
	 * @param count
	 */
	public void fillContentByObtainVar(Map<String,Map<String,List<DimMember>>> addmap,int count,boolean dropRepeated){
		if(addmap == null || addmap.size() < 1) return;
		List<String> codeList = new ArrayList<String>();
		CellsModel csModel = this.tbSheetViewer.getCellsModel();
		StringBuffer sb = new StringBuffer();
		Map<String,Boolean> allDimsMap = new HashMap<String,Boolean>();
		Map<String,String> comMap = new HashMap<String, String>();
		AreaPosition ap = varAreaMap.get(this.curVarID);
			for(int r = ap.getStart().getRow();r<=ap.getEnd().getRow();r++){
				sb.setLength(0);
				for(int col = ap.getStart().getColumn();col<=ap.getEnd().getColumn();col++){
					Cell c = csModel.getCell(r, col);
					if (c == null || c.getValue() == null || c.getValue().toString().equals(""))
						continue;

					CellExtInfo cInfo = (CellExtInfo) c.getExtFmt(TbIufoConst.tbKey);
					if(cInfo.getVarDimDef() == null) continue;
					List<LevelValue> list = cInfo.getLevelValuesSortedList();
					if (list != null/*dst != null && dst.getLevelValues() != null*/) {
						for (LevelValue lv : list) {
							if (lv == null || lv.getDimLevel().isAllLevel())
								continue;
							sb.append(lv.getCode()).append("@");
							comMap.put(lv.getDimLevel().getObjCode(), lv.getCode());
						}
					}
				}
				codeList.add(sb.toString());
			}
//		}
		sb.setLength(0);
		Map<String,Map<String,List<DimMember>>> map = new LinkedHashMap<String, Map<String,List<DimMember>>>();
		for(Entry<String,Map<String,List<DimMember>>> en:addmap.entrySet()){
			for(Entry<String,List<DimMember>> entry:en.getValue().entrySet()){
				for(DimMember dimm:entry.getValue()){
					sb.append(dimm.getLevelValue().getCode()).append("@");
				}
				if(!allDimsMap.containsKey(entry.getKey()))
					allDimsMap.put(entry.getKey(),Boolean.FALSE);
			}
			if(!codeList.contains(sb.toString())){
				if(dropRepeated){
					codeList.add(sb.toString());
				}
				map.put(en.getKey(), en.getValue());
			}
			sb.setLength(0);
		}
		if(map == null || map.size() < 1) return;
		count = map.size();
		this.addMultiLine(1, count);
		Map<String,List<Cell>> cellMap = new LinkedHashMap<String, List<Cell>>();
		List<Cell> cells = tbSheetViewer.getSelectedCell();
		Cell c = tbSheetViewer.getSelectedCell().get(cells.size() - 1);
		CellsModel cmodel=tbSheetViewer.getCellsModel();
		//获取填充区域
		for(int col = 0;col<cmodel.getColNum();col++){
			Cell cell=cmodel.getCell(c.getRow()+1, col);
			CellExtInfo Info = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
			if(Info!=null&&Info.getVarId()!=null&&Info.getVarDimDef()!=null/*&&!Info.getVarDimDef().equals("")*/){
				if(!cellMap.containsKey(Info.getVarDimDef().getBusiCode()))
					cellMap.put(Info.getVarDimDef().getBusiCode(), new ArrayList<Cell>());
				cellMap.get(Info.getVarDimDef().getBusiCode()).add(cell);
				for(int j=1;j<count;j++){
					cellMap.get(Info.getVarDimDef().getBusiCode()).add(cmodel.getCell(cell.getRow()+j, cell.getCol()));
				}
			}
		}
		Map<String,List<DimMember>> dimmap=new LinkedHashMap<String, List<DimMember>>();
		for(String row:map.keySet()){
			for(String code:allDimsMap.keySet()){
				allDimsMap.put(code, false);
			}
			for(String dimCode:map.get(row).keySet()){
				if(!dimmap.containsKey(dimCode)){
					dimmap.put(dimCode, new ArrayList<DimMember>());
				}
				dimmap.get(dimCode).addAll(map.get(row).get(dimCode));
				allDimsMap.put(dimCode, true);
			}
			for(String code:allDimsMap.keySet()){
				if(!allDimsMap.get(code)){
					if(!dimmap.containsKey(code)){
						dimmap.put(code, new ArrayList<DimMember>());
					}
					dimmap.get(code).add(null);
				}
			}
		}
		for(String code:dimmap.keySet()){
			if(cellMap.containsKey(code)){
				fullFillContent(cellType,cellMap.get(code),dimmap.get(code).toArray(new Object[0]));
			}
		}
	}

	// 填充
	public void fullFillContent(int varType, List<Cell> cells, Object[] busiObjs) {
		Cell c = cells.get(cells.size() - 1);
		Cell firstCell = cells.get(0);
		String firstVarId = ((CellExtInfo) c.getExtFmt(TbIufoConst.tbKey))
				.getVarId();
		int cellNum = -1;
		int firstNum = -1;
		if (varType == ExVarAreaDef.varAreatType_ROW) {
			cellNum = c.getCol();
			firstNum = firstCell.getRow();
		} else {
			cellNum = c.getRow();
			firstNum = firstCell.getCol();
		}
		// DimMember[] dms = new DimMember[busiObjs.length];
		// for(int i = 0 ; i <busiObjs.length;i++){
		// dms[i] = (DimMember)busiObjs[i];
		// }

		DimMember[] dms = null;
		String[] enumObjs = null;
		// 增行类型-1 无 ，1 维度 ， 2 枚举
		int addType = -1;
		if (busiObjs != null) {
			if (busiObjs[0] instanceof DimMember) {
				addType = 1;
				dms = new DimMember[busiObjs.length];
				for (int i = 0; i < busiObjs.length; i++) {
					dms[i] = (DimMember) busiObjs[i];
				}
			} else {
				addType = 2;
				enumObjs = new String[busiObjs.length];
				for (int i = 0; i < busiObjs.length; i++) {
					enumObjs[i] = busiObjs[i].toString();
				}
			}
		}
		if (addCunt == 1) {
			if (cells.size() < busiObjs.length) {
				// 参照多选
				for (int i = firstNum; i < firstNum + busiObjs.length; i++) {
					int row, col;
					if (varType == ExVarAreaDef.varAreatType_ROW) {
						row = i;
						col = cellNum;
					} else {
						row = cellNum;
						col = i;
					}
					Cell targetCell = this.tbSheetViewer.getCellsModel()
							.getCell(row, col);
					if (targetCell == null)
						return;
					CellExtInfo targetExtInfo = (CellExtInfo) targetCell
							.getExtFmt(TbIufoConst.tbKey);
					if (targetExtInfo == null)
						return;
					String targetVarId = targetExtInfo.getVarId();
					if (!firstVarId.equalsIgnoreCase(targetVarId))
						return;
					if (targetCell != null) {
						Object obj = busiObjs[((i - firstNum) % busiObjs.length)];
						if (obj == null || obj.equals("")) {
							targetCell.setValue("");
						} else {
							if (obj instanceof String) {
								String objStr = (String) obj;
								if (objStr.indexOf("_") == 0) {
									targetCell.setValue(objStr);
								} else {
									String[] objs = objStr.split("_");
									targetCell.setValue(objs[1]);
								}
							} else if (obj instanceof DimMember) {
								DimMember dm = (DimMember) obj;
								VarCellValueModel varCellModel = new VarCellValueModel(
										varType,
										tbSheetViewer.getCellsModel(),
										varType == ExVarAreaDef.varAreatType_ROW ? targetCell
												.getRow() : targetCell.getCol(),
										cellNum, new DimMember[] { dm });
								try {
									varCellModel.fireCellValueChaned();
								} catch (BusinessException be) {
									NtbLogger.error(be);
								}
								targetCell.setValue(dm.getObjName());
							}
						}
					}

				}

			} else {
				// 单元格多选

				for (int i = firstNum; i < firstNum + cells.size(); i++) {
					int row, col;
					if (varType == ExVarAreaDef.varAreatType_ROW) {
						row = i;
						col = cellNum;
					} else {
						row = cellNum;
						col = i;
					}
					Cell targetCell = this.tbSheetViewer.getCellsModel()
							.getCell(row, col);
					if (targetCell != null) {
						Object obj = busiObjs[((i - firstNum) % busiObjs.length)];
						if (obj == null || obj.equals("")) {
							targetCell.setValue("");
						} else {
							if (obj instanceof String) {
								String objStr = (String) obj;
								if (objStr.indexOf("_") == -1) {
									targetCell.setValue(objStr);
								} else {
									String[] objs = objStr.split("_");
									targetCell.setValue(objs[1]);
								}
							} else if (obj instanceof DimMember) {
								DimMember dm = (DimMember) obj;
								VarCellValueModel varCellModel = new VarCellValueModel(
										varType,
										tbSheetViewer.getCellsModel(),
										varType == ExVarAreaDef.varAreatType_ROW ? targetCell
												.getRow() : targetCell.getCol(),
										cellNum, new DimMember[] { dm });
								try {
									varCellModel.fireCellValueChaned();
								} catch (BusinessException be) {
									NtbLogger.error(be);
								}
								targetCell.setValue(dm.getObjName());
							}
						}
					}
				}
			}
		} else {

			if (cells.size() / addCunt < busiObjs.length) {
				// 参照多选
				for (int i = firstNum; i < firstNum + busiObjs.length; i++) {
					int row, col;
					if (varType == ExVarAreaDef.varAreatType_ROW) {
						row = firstNum + (i - firstNum) * addCunt;
						col = cellNum;
					} else {
						row = cellNum;
						col = firstNum + (i - firstNum) * addCunt;
					}
					Cell targetCell = this.tbSheetViewer.getCellsModel()
							.getCell(row, col);
					if (targetCell == null)
						return;
					CellExtInfo targetExtInfo = (CellExtInfo) targetCell
							.getExtFmt(TbIufoConst.tbKey);
					if (targetExtInfo == null)
						return;
					String targetVarId = targetExtInfo.getVarId();
					if (!firstVarId.equalsIgnoreCase(targetVarId))
						return;
					if (targetCell != null) {
						Object obj = busiObjs[((i - firstNum) % busiObjs.length)];
						if (obj == null || obj.equals("")) {
							targetCell.setValue("");
						} else {
							if (obj instanceof String) {
								String objStr = (String) obj;
								if (objStr.indexOf("_") == 0) {
									targetCell.setValue(objStr);
								} else {
									String[] objs = objStr.split("_");
									targetCell.setValue(objs[1]);
								}
							} else if (obj instanceof DimMember) {
								DimMember dm = (DimMember) obj;
								VarCellValueModel varCellModel = new VarCellValueModel(
										varType,
										tbSheetViewer.getCellsModel(),
										varType == ExVarAreaDef.varAreatType_ROW ? targetCell
												.getRow() : targetCell.getCol(),
										cellNum, new DimMember[] { dm },
										addCunt);
								try {
									varCellModel.fireCellValueChaned();
								} catch (BusinessException be) {
									NtbLogger.error(be);
								}
								targetCell.setValue(dm.getObjName());
							}
						}
					}

				}

			} else {
				// 单元格多选

				for (int i = firstNum; i < firstNum + cells.size() / addCunt; i++) {
					int row, col;
					if (varType == ExVarAreaDef.varAreatType_ROW) {
						row = firstNum + (i - firstNum) * addCunt;
						col = cellNum;
					} else {
						row = cellNum;
						col = firstNum + (i - firstNum) * addCunt;
					}
					Cell targetCell = this.tbSheetViewer.getCellsModel()
							.getCell(row, col);
					if (targetCell != null) {
						Object obj = busiObjs[((i - firstNum) % busiObjs.length)];
						if (obj == null || obj.equals("")) {
							targetCell.setValue("");
						} else {
							if (obj instanceof String) {
								String objStr = (String) obj;
								if (objStr.indexOf("_") == -1) {
									targetCell.setValue(objStr);
								} else {
									String[] objs = objStr.split("_");
									targetCell.setValue(objs[1]);
								}
							} else if (obj instanceof DimMember) {
								DimMember dm = (DimMember) obj;
								VarCellValueModel varCellModel = new VarCellValueModel(
										varType,
										tbSheetViewer.getCellsModel(),
										varType == ExVarAreaDef.varAreatType_ROW ? targetCell
												.getRow() : targetCell.getCol(),
										cellNum, new DimMember[] { dm },
										addCunt);
								try {
									varCellModel.fireCellValueChaned();
								} catch (BusinessException be) {
									NtbLogger.error(be);
								}
								targetCell.setValue(dm.getObjName());
							}
						}
					}
				}
			}
		}
		tbSheetViewer.getTable().repaint();
	}

}