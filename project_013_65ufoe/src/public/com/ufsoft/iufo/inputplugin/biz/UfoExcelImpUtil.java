/**
 *
 */
package com.ufsoft.iufo.inputplugin.biz;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nc.util.iufo.xls.BigHxlSheetNameHandler;
import nc.vo.iufo.constant.CommonCharConstant;
import nc.vo.pub.lang.UFDouble;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.poi.POIXMLException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;

import com.ufida.dataset.IContext;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iufo.excel.util.RepImpExpPubUtil;
import com.ufsoft.iufo.fmtplugin.BDContextKey;
import com.ufsoft.iufo.fmtplugin.formula.FormulaModel;
import com.ufsoft.iufo.fmtplugin.formula.UfoFmlExecutor;
import com.ufsoft.iufo.func.excel.text.ImpExpFileNameUtil;
import com.ufsoft.report.IufoFormat;
import com.ufsoft.report.constant.DefaultSetting;
import com.ufsoft.report.sysplugin.excel.ExcelExpUtil;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.PrintSetModel;
import com.ufsoft.table.TableStyle;
import com.ufsoft.table.TableUtilities;
import com.ufsoft.table.format.CellAlign;
import com.ufsoft.table.format.CellFont;
import com.ufsoft.table.format.CellLines;
import com.ufsoft.table.format.DefaultDataFormat;
import com.ufsoft.table.format.DefaultFormatValue;
import com.ufsoft.table.format.ICellAlign;
import com.ufsoft.table.format.ICellFont;
import com.ufsoft.table.format.IDataFormat;
import com.ufsoft.table.format.IFormat;
import com.ufsoft.table.format.NumberFormat;
import com.ufsoft.table.format.StringFormat;
import com.ufsoft.table.format.TableConstant;
import com.ufsoft.table.header.Header;
import com.ufsoft.table.header.HeaderModel;
import com.ufsoft.table.print.PrintSet;

/**
 * @author liuchuna
 * @created at 2011-5-6,上午09:25:46 edit by congdy 2014.8.28 加段注释为了出补丁盘
 */
public class UfoExcelImpUtil {

	public static final String VLOOKUP_FUNC_NAME = "VLOOKUP";

	/**
	 * 从Excel文件对象生成CellsModel
	 * 
	 * @param excelFile
	 *            如果文件不存在，则返回null
	 * @return
	 */
	public static CellsModel importCellsModel(String excelFileName) {
		if (excelFileName == null) {
			throw new RuntimeException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0",
					"01413010-0136")/* @res "文件名称不能为空！" */);
		}
		CellsModel cellsModel = null;
		Workbook workBook;
		try {
			workBook = getWorkbook(excelFileName);
			Sheet sheet = workBook.getSheetAt(0);
			cellsModel = getCellsModelByExcel(sheet, workBook);
		} catch (FileNotFoundException e) {
			AppDebug.debug(e);
		} catch (IOException e) {
			AppDebug.debug(e);
		}

		return cellsModel;
	}

	public static Workbook getWorkbook(File excelFile) throws FileNotFoundException, IOException {
		return getWorkbook(excelFile.getPath());
	}

	public static Workbook getWorkbook(String excelFileName) throws FileNotFoundException, IOException {
		boolean isExcel2007 = ImpExpFileNameUtil.isExcel2007(excelFileName);

		Workbook workBook = null;
		POIFSFileSystem fs = null;

		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(excelFileName);
			if (isExcel2007) {
				workBook = new XSSFWorkbook(fileInputStream);
			}else{
				fs = new POIFSFileSystem(fileInputStream);
				workBook = new HSSFWorkbook(fs);
			}
		} catch (POIXMLException fileE) {
			fs = new POIFSFileSystem(fileInputStream);
			workBook = new HSSFWorkbook(fs);
		} catch (OfficeXmlFileException e) {
			workBook = new XSSFWorkbook(fileInputStream);
		}finally{
			if(fileInputStream!=null){
				fileInputStream.close();
			}
		}
		
//		if (isExcel2007) {
//			try {
//				workBook = new XSSFWorkbook(fileInputStream);
//			} catch (POIXMLException fileE) {
//				fileInputStream.close();
//				fs = new POIFSFileSystem(new FileInputStream(excelFileName));
//				workBook = new HSSFWorkbook(fs);
//			}
//		} else {
//			try {
//				fs = new POIFSFileSystem(fileInputStream);
//				workBook = new HSSFWorkbook(fs);
//			} catch (OfficeXmlFileException e) {
//				fileInputStream.close();
//				workBook = new XSSFWorkbook(new FileInputStream(excelFileName));
//			}
//		}
//		if (fileInputStream != null)
//			fileInputStream.close();
		return workBook;
	}

	public static CellsModel getCellsModelByExcel(Sheet sheet, Workbook workBook) {
		CellsModel cellsModel = CellsModel.getInstance(null, true);
		return getCellsModelByExcel(cellsModel, sheet, workBook);
	}

	public static CellsModel getCellsModelByExcel(CellsModel cellsModel, Sheet sheet, Workbook workBook) {
		return getCellsModelByExcel(cellsModel, sheet, workBook, -1);
	}

	public static CellsModel getCellsModelByExcel(Map<String, String> sheet2CodeMap, CellsModel cellsModel,
			Sheet sheet, Workbook workBook, int fmlType) {

		// @edit by wuyongc at 2013-6-5,上午9:29:10 禁止事件，提高效率,处理完成后，再开启事件
		cellsModel.setEnableEvent(false);
		// 转换单元格
		for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (row == null) {
				continue;
			}
			short firstColIndex = row.getFirstCellNum();
			short lastCellNum = row.getLastCellNum();
			for (short colIndex = firstColIndex; colIndex <= lastCellNum; colIndex++) {
				if (colIndex >= 0) {
					Cell cell = row.getCell(colIndex);
					if (cell == null)
						continue;
					convertCell(sheet2CodeMap, cell, rowIndex, colIndex, cellsModel, sheet, workBook, fmlType);
				}
			}
		}

		treateHeightWidth(cellsModel, sheet);

		// 转换PrintSet
		convertPrintSet(sheet.getPrintSetup(), sheet, workBook, cellsModel);
		// 转换组合单元。
		convertCombinedCell(sheet, cellsModel);
		// 转换其他设置
		cellsModel.setEnableEvent(true);
		return cellsModel;

	}

	public static CellsModel getCellsModelByExcel(CellsModel cellsModel, Sheet sheet, Workbook workBook, int fmlType) {
		return getCellsModelByExcel(new HashMap<String, String>(0), cellsModel, sheet, workBook, fmlType);
	}

	static void convertCell(Map<String, String> sheet2CodeMap, Cell cell, int rowIndex, short colIndex,
			CellsModel cellsModel, Sheet sheet, Workbook workBook, int fmlType) {
		// 转换单元值
		Object value = null;
		IDataFormat dataFormat = DefaultDataFormat.getInstance();
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_NUMERIC:
			value = Double.valueOf(cell.getNumericCellValue());
			dataFormat = NumberFormat.getInstance();
			// cellsModel.getFormatIfNullNew(CellPosition.getInstance(rowIndex,colIndex)).setCellType(TableConstant.CELLTYPE_NUMBER);
			break;
		case HSSFCell.CELL_TYPE_BOOLEAN:
			value = Boolean.valueOf(cell.getBooleanCellValue());
			dataFormat = StringFormat.getInstance();
			// cellsModel.getFormatIfNullNew(CellPosition.getInstance(rowIndex,colIndex)).setCellType(TableConstant.CELLTYPE_STRING);
			break;

		case HSSFCell.CELL_TYPE_ERROR:
			value = cell.getErrorCellValue() + "";
			break;
		case HSSFCell.CELL_TYPE_BLANK:
			value = null;
			break;

		case HSSFCell.CELL_TYPE_FORMULA:
			if (fmlType != -1) {
				RepImpExpPubUtil.setCellFormula(sheet2CodeMap, fmlType, cell, rowIndex, colIndex, cellsModel);
				// @edit by wuyongc at 2013-6-5,下午1:35:43 公式也不能直接返回，否则会丢失格式。。
				// return;
			}
			// if (cell.getCellFormula() == null
			// || ("" + cell.getNumericCellValue()).equals("NaN") == false) {
			// value = Double.valueOf(cell.getNumericCellValue());
			// dataFormat = NumberFormat.getInstance();
			//
			// //
			// cellsModel.getFormatIfNullNew(CellPosition.getInstance(rowIndex,colIndex)).setCellType(TableConstant.CELLTYPE_NUMBER);
			// } else {
			// value = cell.getRichStringCellValue() + "";
			// }
			break;
		default:
			value = cell.getRichStringCellValue() + "";
			break;
		}
		//editor tianjlc 判断是否为报表关键字信息，是则不导入进表样
		if (value != null && value instanceof String) {
			if (StringUtils.isNotEmpty((String) value)) {
				try {
					if (((String) value).indexOf(CommonCharConstant.POUND_SING) != -1) {
						value = null;
					}
				} catch (Exception e) {
					AppDebug.debug(e);
				}
			}
		}
		if (value instanceof String && value != null) {
			String strValue = "*" + value;
			strValue = strValue.trim();
			strValue = strValue.substring(1);
			value = strValue;
		}

		cellsModel.setCellValue(rowIndex, colIndex, value);
		// 转换格式
		CellStyle cellStyle = cell.getCellStyle();
		if (cellStyle != null) {
			IFormat format = IufoFormat.getInstance();
			cellsModel.setCellFormat(rowIndex, colIndex, format);
			// 字体
			short backColorIndex = cellStyle.getFillForegroundColor();
			Color backColor = getColor(backColorIndex);
			Font cellFont = workBook.getFontAt(cellStyle.getFontIndex());
			Color fontColor = getColor(cellFont.getColor());
			String strFontName = null;
			// @edit by wuyongc at 2013-3-15,下午3:35:45 Calibri 字体中文显示会有问题。
			if ("Calibri".equals(cellFont.getFontName())) {
				strFontName = DefaultFormatValue.FONTNAME;
			} else {
				strFontName = cellFont.getFontName();
			}
			// int fontSize = (int) (cellFont.getFontHeight() /
			// ExcelExpUtil.FONT_SISE_SCALE_TOEXCEL);
			int fontSize = (int) (cellFont.getFontHeightInPoints() / 72.0 * 25.4 * 4);
			int fontstyle = format.getFont().getFontstyle();
			// 未定义格式时，赋默认值 ，解决没有导入字体粗体显示的问题
			if (fontstyle == TableConstant.UNDEFINED) {
				fontstyle = TableConstant.FS_NORMAL;
			}
			if (cellFont.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD) {
				// 按位操作符
				fontstyle = fontstyle | TableConstant.FS_BOLD;
			}
			if (cellFont.getItalic()) {
				fontstyle = fontstyle | TableConstant.FS_SLOPE;
			}
			if (cellFont.getUnderline() == HSSFFont.U_SINGLE) {
				fontstyle = fontstyle | TableConstant.FS_UNDERLINE;
			}

			ICellFont font = CellFont.getInstance(strFontName, fontstyle, fontSize, backColor, fontColor);

			// 对齐方式。
			int halign = getAlignment(cellStyle.getAlignment(), true);
			int valign = getAlignment(cellStyle.getVerticalAlignment(), false);
			int fold = cellStyle.getWrapText() ? 1 : 0;

			ICellAlign align = CellAlign.getInstance(halign, valign, fold, TableConstant.UNDEFINED,
					TableConstant.UNDEFINED);

			// 边框线性和边框颜色
			int[] lineTypes = DefaultSetting.NO_LINES_TYPE;
			Color[] lineColors = DefaultSetting.NO_LINES_COLOR;
			lineTypes[IFormat.TOPLINE] = getBorderType(cellStyle.getBorderTop());
			lineTypes[IFormat.BOTTOMLINE] = getBorderType(cellStyle.getBorderBottom());
			lineTypes[IFormat.LEFTLINE] = getBorderType(cellStyle.getBorderLeft());
			lineTypes[IFormat.RIGHTLINE] = getBorderType(cellStyle.getBorderRight());
			// @edit by wuyongc at 2013-8-12,下午4:55:36 找不到颜色，默认给设置上黑色。TODO
			Color color = getColor(cellStyle.getTopBorderColor());
			if (color != null) {
				lineColors[IFormat.TOPLINE] = color;
			} else {
				lineColors[IFormat.TOPLINE] = Color.BLACK;
			}
			color = getColor(cellStyle.getBottomBorderColor());
			if (color != null) {
				lineColors[IFormat.BOTTOMLINE] = color;
			} else {
				lineColors[IFormat.TOPLINE] = Color.BLACK;
			}
			color = getColor(cellStyle.getLeftBorderColor());
			if (color != null) {
				lineColors[IFormat.LEFTLINE] = color;
			} else {
				lineColors[IFormat.TOPLINE] = Color.BLACK;
			}
			color = getColor(cellStyle.getRightBorderColor());
			if (color != null) {
				lineColors[IFormat.RIGHTLINE] = color;
			} else {
				lineColors[IFormat.TOPLINE] = Color.BLACK;
			}
			// 其他格式
			IFormat newFormat = IufoFormat.getInstance(dataFormat, font, align,
					CellLines.getInstance(lineTypes, lineColors));
			cellsModel.setCellFormat(rowIndex, colIndex, newFormat);
		}
	}

	static Color getColor(short colorIndex) {
		if (colorIndex >= 0 && colorIndex < ExcelExpUtil.s_colorIndexHash.size()) {
			HSSFColor excelColor = (HSSFColor) ExcelExpUtil.s_colorIndexHash.get(Integer.valueOf(colorIndex));
			if (excelColor == null) {
				return null;
			}
			short[] rgb = excelColor.getTriplet();
			return new Color(rgb[0], rgb[1], rgb[2]);
		} else {
			return null;
		}

	}

	public static CellsModel getCellsModelByExcel(Sheet sheet, Workbook workBook, IContext context, boolean isImpData) {
		CellsModel cellsModel = CellsModel.getInstance(null, true);
		// @edit by wuyongc at 2013-6-5,上午9:29:54 禁止事件，提高效率,处理完成后，再开启事件
		cellsModel.setEnableEvent(false);

		FormulaModel formulaModel = isImpData ? null : FormulaModel.getInstance(cellsModel);
		UfoFmlExecutor executor = isImpData ? null : formulaModel.getUfoFmlExecutor();
		if (!isImpData && executor == null && context != null) {
			executor = UfoFmlExecutor.getInstance(context, cellsModel);
			formulaModel.setUfoFmlExecutor(executor);

			String strCurUnitId = (String) context.getAttribute(BDContextKey.CUR_REPORG_PK);
			if (strCurUnitId != null && strCurUnitId.trim().length() > 0) {
				formulaModel.setUnitID(strCurUnitId);
			}
		}

		StringBuffer showMsg = new StringBuffer();
		// 转换单元格
		int firstRowNum = sheet.getFirstRowNum();
		int lastRowNum = sheet.getLastRowNum();
		for (int rowIndex = firstRowNum; rowIndex <= lastRowNum; rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (row == null) {
				continue;
			}
			short firstCellNum = row.getFirstCellNum();
			short lastCellNum = row.getLastCellNum();
			for (short colIndex = firstCellNum; colIndex <= lastCellNum; colIndex++) {
				// @edit by wuyongc at
				// 2013-2-26,下午7:34:09,导入excel2007，导入的时候这里colIndex可能为-1
				// ，导致后面出现异常
				if (colIndex < 0)
					continue;
				Cell cell = row.getCell(colIndex);
				if (cell == null)
					continue;
				convertCell(cell, rowIndex, colIndex, cellsModel, sheet, workBook, isImpData);

				if (executor != null && !isImpData)
					convertFormula(cell, rowIndex, colIndex, executor, showMsg);
			}
		}
		// @edit by wuyongc at 2013-7-2,下午4:25:54 导入数据不需要处理格式了。。。
		if (!isImpData) {
			treateHeightWidth(cellsModel, sheet);

			// 转换PrintSet
			convertPrintSet(sheet.getPrintSetup(), sheet, workBook, cellsModel);
		} else {
			cellsModel.clearCells(null);
		}
		// 转换组合单元。
		convertCombinedCell(sheet, cellsModel);

		cellsModel.setEnableEvent(true);
		return cellsModel;
	}

	static void convertFormula(Cell cell, int rowIndex, short colIndex, UfoFmlExecutor executor, StringBuffer showMsg) {
		int cellType = cell.getCellType();
		if (cellType == HSSFCell.CELL_TYPE_FORMULA) {
			String cellFormula = cell.getCellFormula();
			CellPosition cellPos = CellPosition.getInstance(rowIndex, colIndex);

			// 处理VLOOKUP函数
			if (cellFormula != null && cellFormula.startsWith(VLOOKUP_FUNC_NAME)) {
				cellFormula = convertVlookupFormula(null, cellFormula);
			}

			try {
				executor.addUserDefFormula(showMsg, cellPos, cellFormula, FormulaModel.TYPE_CELL_FML, true);
			} catch (ParseException e) {

				AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0107")/*
																												 * @
																												 * res
																												 * "导入的公式不合法，公式内容："
																												 */
						+ cellFormula);
			}
		}

	}

	public static String convertVlookupFormula(Map<String, String> sheet2CodeMap, String exFormula) {
		StringBuffer formula = new StringBuffer();
		try {
			// 函数头
			formula.append("VLOOKUP(");
			// 截取函数参数部分
			String fmlContent = exFormula.substring(exFormula.indexOf("(") + 1, exFormula.lastIndexOf(")"));
			// 分割参数
			String[] params = fmlContent.split(",");
			if (params.length > 4 || params.length < 3) {
				return exFormula;
			}
			// 参照指标
			params[0] = params[0].trim();
			if (params[0].indexOf("$") < 0) {
				params[0] = "?" + params[0];
			} else {
				params[0] = params[0].replaceAll("[$]", "");
			}
			formula.append(params[0]).append(",");
			// 参照区域
			params[1] = params[1].trim();
			// 是否他表
			int osIndex = params[1].indexOf("!");
			if (osIndex > 0) {
				String repCode = params[1].substring(0, osIndex);
				if (sheet2CodeMap != null && sheet2CodeMap.get(repCode) != null) {
					repCode = sheet2CodeMap.get(repCode);
				}
				formula.append("'").append(repCode).append("->");
				params[1] = params[1].substring(osIndex + 1);
			}
			// 区域
			if (params[1].indexOf("$") < 0) {
				String[] pos = params[1].split(":");
				formula.append("?").append(pos[0]).append(":?").append(pos[1]);
			} else {
				params[1] = params[1].replaceAll("[$]", "");
				formula.append(params[1]);
			}
			if (osIndex > 0) {
				formula.append("'");
			}
			formula.append(",");
			// 相对序号
			params[2] = params[2].trim();
			formula.append(Integer.parseInt(params[2])).append(",");
			// 模糊匹配
			String match = null;
			if (params.length == 3) {
				match = "FALSE";
			} else {
				match = params[3].trim();
			}
			if (match.length() <= 0) {
				match = "FALSE";
			}
			formula.append("'").append(match).append("'");
			// 函数尾
			formula.append(")");
		} catch (Exception e) {
			AppDebug.debug(e);
			return exFormula;
		}
		return formula.toString();
	}

	public static Object getCellValue(Cell cell, int rowIndex, int colIndex, CellsModel cellsModel, Sheet sheet,
			Workbook workBook) {
		// 转换单元值
		Object value = null;
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_NUMERIC:
			if (HSSFDateUtil.isCellDateFormatted(cell)) {
				Date d = cell.getDateCellValue();
				// @edit by wuyongc at 2013-10-16,下午4:33:45 不能丢失了时间
				value = DateFormatUtils.format(d, "yyyy-MM-dd HH:mm:ss");
			} else {
				value = Double.valueOf(cell.getNumericCellValue());
			}
			break;
		case HSSFCell.CELL_TYPE_BOOLEAN:
			value = Boolean.valueOf(cell.getBooleanCellValue());
			break;

		case HSSFCell.CELL_TYPE_ERROR:
			value = cell.getErrorCellValue() + "";
			break;
		case HSSFCell.CELL_TYPE_BLANK:
			value = null;
			break;

		case HSSFCell.CELL_TYPE_FORMULA:
			try {
				value = cell.getNumericCellValue();
			} catch (Exception e) {
				try {
					value = cell.getStringCellValue();
				} catch (Exception e1) {
					try {
						value = cell.getRichStringCellValue();
					} catch (Exception e2) {
					}
				}
			}
			break;
		default:
			value = cell.getRichStringCellValue() + "";
			break;
		}
		if (value instanceof String && value != null) {
			String strValue = "*" + value;
			strValue = strValue.trim();
			strValue = strValue.substring(1);
			value = strValue;
		}
		return value;
	}

	// @add by wuyongc at 2013-7-2,下午4:28:24
	// 重写了CellsModel里的方法，为了提高效率，避免一些不必要的操作
	private static void setCellValue(CellsModel cellsModel, final int row, int col, Object value) {
		CellPosition cellPos = CellPosition.getInstance(row, col);
		Object oldValue = cellsModel.getCellValue(cellPos);
		if (value == null && oldValue == null) {
			return;
		}
		if (oldValue != null && oldValue.equals(value) || (value != null && value.equals(oldValue))) {
			return;
		}
		com.ufsoft.table.Cell cell = null;
		if (cellsModel.getCell(row, col) == null)
			setCell(cellsModel, row, col, new com.ufsoft.table.Cell());
		cell = cellsModel.getCell(row, col);
		cell.setValue(value);
	}

	// @add by wuyongc at 2013-7-2,下午4:28:46 重写了CellsModel里的方法，为了提高效率，避免一些不必要的操作
	public static void setCell(CellsModel cellsModel, int row, int col, com.ufsoft.table.Cell value) {
		if (!cellsModel.isInfinite() && (row >= cellsModel.getRowNum() || col >= cellsModel.getColNum())) { // 不是无限表并且无法容纳这个单元.
			return;
		}

		CellPosition pos = CellPosition.getInstance(row, col);
		pos = cellsModel.getModifiedStartCell(pos);
		row = pos.getRow();
		col = pos.getColumn();

		List<com.ufsoft.table.Cell> rowData = getTableRow(cellsModel, row);
		if (col >= rowData.size()) {
			TableUtilities.expandArray(rowData, col + 1);
		}
		rowData.set(col, value);
	}

	private static List<com.ufsoft.table.Cell> getTableRow(CellsModel cellsModel, int rowNo) {
		// if (rowNo > DefaultSetting.MAX_ROW_NUM) {
		// throw new IllegalArgumentException();
		// }
		int rowCount = cellsModel.getCells().size();
		if (rowNo >= rowCount) { // 扩展行模型
			int num = rowNo + 1 - rowCount;
			for (int i = 0; i < num; i++) {
				cellsModel.getCells().add(null);
			}
		}
		List<com.ufsoft.table.Cell> row = cellsModel.getCells().get(rowNo);
		if (row == null) {
			row = new ArrayList<com.ufsoft.table.Cell>();
			cellsModel.getCells().set(rowNo, row);
		}
		return row;
	}

	private static void convertCell(Cell cell, int rowIndex, short colIndex, CellsModel cellsModel, Sheet sheet,
			Workbook workBook, boolean isImpData) {
		// 转换单元值
		Object value = null;
		IDataFormat dataFormat = null;
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_NUMERIC:
			if (HSSFDateUtil.isCellDateFormatted(cell)) {
				Date d = cell.getDateCellValue();
				// @edit by wuyongc at 2013-10-16,下午4:32:33 不能丢失了时间信息
				value = DateFormatUtils.format(d, "yyyy-MM-dd HH:mm:ss");
				// @edit by wuyongc at 2013-7-2,下午4:27:15 导入数据不需要处理格式。。
				if (!isImpData)
					dataFormat = com.ufsoft.table.format.DateFormat.getInstance(0);
			} else {
				value = new UFDouble(cell.getNumericCellValue());
				if (!isImpData)
					dataFormat = NumberFormat.getInstance();
			}

			break;
		case HSSFCell.CELL_TYPE_BOOLEAN:
			value = Boolean.valueOf(cell.getBooleanCellValue());
			if (!isImpData)
				dataFormat = StringFormat.getInstance();
			break;

		case HSSFCell.CELL_TYPE_ERROR:
			value = cell.getErrorCellValue() + "";
			break;
		case HSSFCell.CELL_TYPE_BLANK:
			value = null;
			break;

		case HSSFCell.CELL_TYPE_FORMULA:
			if (isImpData) {
				// TODO 无奈了，暂时如此处理了。。。
				try {
//					value = cell.getNumericCellValue();
					// 通版补丁适配:有公式的导入数据精度错误，这里改用UFDouble
					value = new UFDouble(cell.getNumericCellValue());
				} catch (Exception e) {
					try {
						value = cell.getStringCellValue();
					} catch (Exception e1) {
						try {
							value = cell.getRichStringCellValue();
						} catch (Exception e2) {
						}
					}
				}
			}
			break;
		default:
			value = cell.getRichStringCellValue() + "";
			break;
		}

		// if (value instanceof String && value != null) {
		// String strValue = "*" + value;
		// strValue = strValue.trim();
		// strValue = strValue.substring(1);
		// value = strValue;
		// }
		if (isImpData) {
			setCellValue(cellsModel, rowIndex, colIndex, value);
		} else
			cellsModel.setCellValue(rowIndex, colIndex, value);
		// @edit by wuyongc at 2013-7-1,下午3:10:53 导入数据，就不需要知道格式相关信息了，不做处理了。。
		if (isImpData) {
			return;
		} else {
			if (dataFormat == null) {
				DefaultDataFormat.getInstance();
			}
		}
		// 转换格式
		CellStyle cellStyle = cell.getCellStyle();
		if (cellStyle != null) {
			IFormat format = IufoFormat.getInstance();
			cellsModel.setCellFormat(rowIndex, colIndex, format);
			// 字体
			short backColorIndex = cellStyle.getFillForegroundColor();
			Color backColor = getColor(backColorIndex);
			Font cellFont = workBook.getFontAt(cellStyle.getFontIndex());
			Color fontColor = getColor(cellFont.getColor());
			String strFontName = null;
			// @edit by wuyongc at 2013-3-15,下午3:35:45 Calibri
			// 字体中文显示会有问题。（Excel2007默认为此字体）
			if ("Calibri".equals(cellFont.getFontName())) {
				strFontName = DefaultFormatValue.FONTNAME;
			} else {
				strFontName = cellFont.getFontName();
			}
			// int fontSize = (int) (cellFont.getFontHeight() /
			// UfoExcelExpUtil.FONT_SISE_SCALE_TOEXCEL);
			int fontSize = (int) (cellFont.getFontHeightInPoints() / 72.0 * 25.4 * 4);
			// int fontStype = TableConstant.UNDEFINED;
			int fontstyle = format.getFont().getFontstyle();
			if (fontstyle == TableConstant.UNDEFINED) {
				fontstyle = TableConstant.FS_NORMAL;
			}
			if (cellFont.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD) {
				// 按位操作符
				fontstyle = fontstyle | TableConstant.FS_BOLD;
			}
			if (cellFont.getItalic()) {
				fontstyle = fontstyle | TableConstant.FS_SLOPE;
			}
			if (cellFont.getUnderline() == HSSFFont.U_SINGLE) {
				fontstyle = fontstyle | TableConstant.FS_UNDERLINE;
			}

			ICellFont font = CellFont.getInstance(strFontName, fontstyle, fontSize, backColor, fontColor);

			// 对齐方式。
			int halign = getAlignment(cellStyle.getAlignment(), true);
			int valign = getAlignment(cellStyle.getVerticalAlignment(), false);
			int fold = cellStyle.getWrapText() ? 1 : 0;

			ICellAlign align = CellAlign.getInstance(halign, valign, fold, TableConstant.UNDEFINED,
					TableConstant.UNDEFINED);

			// 边框线性和边框颜色
			int[] lineTypes = DefaultSetting.NO_LINES_TYPE;
			Color[] lineColors = DefaultSetting.NO_LINES_COLOR;
			lineTypes[IFormat.TOPLINE] = getBorderType(cellStyle.getBorderTop());
			lineTypes[IFormat.BOTTOMLINE] = getBorderType(cellStyle.getBorderBottom());
			lineTypes[IFormat.LEFTLINE] = getBorderType(cellStyle.getBorderLeft());
			lineTypes[IFormat.RIGHTLINE] = getBorderType(cellStyle.getBorderRight());

			Color color = getColor(cellStyle.getTopBorderColor());
			if (color != null) {
				lineColors[IFormat.TOPLINE] = color;
			}
			color = getColor(cellStyle.getBottomBorderColor());
			if (color != null) {
				lineColors[IFormat.BOTTOMLINE] = color;
			}
			color = getColor(cellStyle.getLeftBorderColor());
			if (color != null) {
				lineColors[IFormat.LEFTLINE] = color;
			}
			color = getColor(cellStyle.getRightBorderColor());
			if (color != null) {
				lineColors[IFormat.RIGHTLINE] = color;
			}
			// 其他格式
			IFormat newFormat = IufoFormat.getInstance(dataFormat, font, align,
					CellLines.getInstance(lineTypes, lineColors));
			cellsModel.setCellFormat(rowIndex, colIndex, newFormat);

		}
	}

	/**
	 * 处理行高列宽
	 * 
	 * @param cellsModel
	 * @param sheet
	 */
	public static void treateHeightWidth(CellsModel cellsModel, Sheet sheet) {
		if (cellsModel == null || sheet == null) {
			return;
		}
		HeaderModel rowHerderModel = cellsModel.getRowHeaderModel();
		HeaderModel columnHerderModel = cellsModel.getColumnHeaderModel();
		int iColNum = 0;
		for (int rowIndex = sheet.getFirstRowNum(); rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (row == null)
				continue;
			short height = row.getHeight();
			// 隐藏行处理
			if (row.getZeroHeight()) {
				height = TableStyle.MINHEADER;
			}

			if (height < 4)
				height = 0;
			Header header = rowHerderModel.getHeader(rowIndex);
			header = Header.getInstance(header.getValue(), (int) ((((double) height) * 4) / (20 * 3 * 0.9346)),
					header.getFormat(), true);
			rowHerderModel.setHeader(rowIndex, header);
			int singleColNum = row.getLastCellNum() + 1;
			if (singleColNum > iColNum)
				iColNum = singleColNum;
		}

		for (short colIndex = 0; colIndex < iColNum; colIndex++) {
			int width = sheet.getColumnWidth(colIndex);
			if (width < 4)
				width = 0;
			Header header = columnHerderModel.getHeader(colIndex);
			if (header == null)
				continue;
			int iNewWidth = (int) (((double) width * 7) / (256 * 0.892));
			if (iNewWidth < TableStyle.MINHEADER)
				iNewWidth = TableStyle.MINHEADER;
			else if (iNewWidth > TableStyle.MAXHEADER)
				iNewWidth = TableStyle.MAXHEADER;
			header = Header.getInstance(header.getValue(), iNewWidth, header.getFormat(), true);
			columnHerderModel.setHeader(colIndex, header);
		}
	}

	public static void convertPrintSet(PrintSetup excelPs, Sheet sheet, Workbook workBook, CellsModel cellsModel) {
		PrintSet ps = PrintSetModel.getInstance(cellsModel).getPrintSet();
		ps.setViewScale(excelPs.getScale());
	}

	public static void convertCombinedCell(Sheet sheet, CellsModel cellsModel) {
		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress region = sheet.getMergedRegion(i);
			AreaPosition areaPos = AreaPosition.getInstance(region.getFirstRow(), region.getFirstColumn(),
					region.getLastColumn() - region.getFirstColumn() + 1, region.getLastRow() - region.getFirstRow()
							+ 1);

			cellsModel.combineCell(areaPos);
		}
	}

	/**
	 * 从Excel文件对象生成CellsModel
	 * 
	 * @param excelFile
	 *            如果文件不存在，则返回null
	 * @return
	 */
	public static CellsModel importCellsModel(File excelFile, IContext context) {
		CellsModel cellsModel = null;
		if (excelFile.exists()) {
			Workbook workBook;
			try {
				workBook = getWorkbook(excelFile);
				Sheet sheet = workBook.getSheetAt(0);
				cellsModel = getCellsModelByExcel(sheet, workBook, context, false);
			} catch (FileNotFoundException e) {
				AppDebug.debug(e);
			} catch (IOException e) {
				AppDebug.debug(e);
			}
		}
		return cellsModel;
	}

	static int getAlignment(short alignment, boolean bHor) {
		if (bHor) {
			switch (alignment) {
			case HSSFCellStyle.ALIGN_CENTER:
				return TableConstant.HOR_CENTER;
			case HSSFCellStyle.ALIGN_LEFT:
				return TableConstant.HOR_LEFT;
			case HSSFCellStyle.ALIGN_RIGHT:
				return TableConstant.HOR_RIGHT;
			default:
				return TableConstant.UNDEFINED;
			}
		} else {
			switch (alignment) {
			case HSSFCellStyle.VERTICAL_CENTER:
				return TableConstant.VER_CENTER;
			case HSSFCellStyle.VERTICAL_TOP:
				return TableConstant.VER_UP;
			case HSSFCellStyle.VERTICAL_BOTTOM:
				return TableConstant.VER_DOWN;
			default:
				return TableConstant.UNDEFINED;
			}
		}
	}

	static short getBorderType(int lineType) {
		switch (lineType) {
		case HSSFCellStyle.BORDER_DASHED:
			return TableConstant.L_DASH;
		case HSSFCellStyle.BORDER_DASH_DOT:
			return TableConstant.L_DASHDOT;
		case HSSFCellStyle.BORDER_DOTTED:
			return TableConstant.L_DOT;
		case HSSFCellStyle.BORDER_NONE:
			return TableConstant.L_NULL;
		case HSSFCellStyle.BORDER_THIN:
			return TableConstant.L_SOLID1;
		case HSSFCellStyle.BORDER_MEDIUM:
			return TableConstant.L_SOLID2;
		case HSSFCellStyle.BORDER_THICK:
			return TableConstant.L_SOLID3;

		default:
			return TableConstant.UNDEFINED;
		}
	}

	public static Map<String, String> getSheetNames(String excelFileName) throws FileNotFoundException, IOException {

		boolean isExcel2007 = ImpExpFileNameUtil.isExcel2007(excelFileName);
		if (isExcel2007) {
			try {
				return getSheetNames4Excel07(excelFileName);
			} catch (Exception e) {
				AppDebug.debug(e);
				return getSheetNames4Excel03(excelFileName);
			}
		} else {
			try {
				return getSheetNames4Excel03(excelFileName);
			} catch (OfficeXmlFileException e) {
				try {
					return getSheetNames4Excel07(excelFileName);
				} catch (Exception e1) {
					AppDebug.debug(e1);
				}
			}
		}
		return null;

	}

	/**
	 * @create by wuyongc at 2013-11-12,上午11:03:30
	 * 
	 * @param excelFileName
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static Map<String, String> getSheetNames4Excel03(String excelFileName) throws IOException,
			FileNotFoundException {
		Map<String, String> map = new HashMap<String, String>();
		// POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(
		// excelFileName));
		// Workbook workBook = new HSSFWorkbook(fs);
		// int num = workBook.getNumberOfSheets();
		// for (int i = 0; i < num; i++) {
		// map.put(workBook.getSheetName(i), i+"");
		// }

		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(excelFileName);
			BigHxlSheetNameHandler handler = new BigHxlSheetNameHandler(
					fileInputStream);
			String[] sheetNames = handler.getSheetNames();
			for (int i = 0; i < sheetNames.length; i++) {
				map.put(sheetNames[i], (i + 1) + "");
			}
			return map;
		} catch (Exception e) {
		} finally {
			fileInputStream.close();
		}
		return null;
	}

	/**
	 * @create by wuyongc at 2013-11-12,上午10:25:45
	 * 
	 * @param excelFileName
	 * @return
	 * @throws IOException
	 * @throws OpenXML4JException
	 * @throws InvalidFormatException
	 */
	private static Map<String, String> getSheetNames4Excel07(String excelFileName) throws IOException,
			OpenXML4JException, InvalidFormatException {
		List<String> sheetNameList = new ArrayList<String>();
		final OPCPackage open = OPCPackage.open(excelFileName);
		XSSFReader xssReader = new XSSFReader(open);
		SheetIterator it = (SheetIterator) xssReader.getSheetsData();
		Map<String, String> map = new LinkedHashMap<String, String>();
		while (it.hasNext()) {
			it.next();
			String sheetName = it.getSheetName();
			try {
				// @edit by wuyongc at 2013-12-17,下午4:28:05 可能rId 和sheetId 不一致。
				// 根据反射取出CTSheet ，然后获得id
				Field field = it.getClass().getDeclaredField("ctSheet");
				field.setAccessible(true);
				CTSheet ctSheet = (CTSheet) field.get(it);
				String id = ctSheet.getId();
				sheetNameList.add(sheetName);
				map.put(sheetName, "" + id.substring(3));
			} catch (Exception e) {
				AppDebug.error(e);
			}
		}

		// 可能用户当前打开了该文件，造成文件占用没法关闭。此处try catch一下。。
		try {
			open.close();
		} catch (IOException e) {
			AppDebug.debug(e);
		}

		return map;
	}
}