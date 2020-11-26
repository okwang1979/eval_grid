package nc.vo.tb.form.excel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExVarAreaDef implements Serializable, Cloneable {
	private static final long serialVersionUID = -1648839781081186551L;

	public final static int varAreatType_ROW = 0;	 // 行浮动
	public final static int varAreatType_COL = 1;	 // 列浮动
	public final static int varAreaSumType_default = -1; // 汇总方式：默认
	public final static int varAreaSumType_LIST = 0; // 罗列汇总
	public final static int varAreaSumType_SUM = 1;  // 分类汇总
	public final static int varAreaSumType_NONE = 2; // 不汇总
	public final static int isAutoExpandByDataCell_YES = 1;	// 该浮动区未编辑前按照已有多维数据加载
	public final static int isAutoExpandByDataCell_NO = -1;	// 该浮动区不按照多维数据加载
	public final static int isAutoExpandByDataCell_ALL = 2;	// 该浮动区始终按照多维数据加载 -->> lrx 2014-7-21 V635新增
	
	/** 浮动区标识 */
	public String varID;
	
	/** 浮动区类型：行/列，默认为行浮动区 */
	public int varAreaType = varAreatType_ROW;
	
	/** 浮动区汇总类型：罗列/分类/不汇总，默认为罗列汇总==>>默认为默认==多维按分类，非多维按罗列 */
	public int varAreaSumType = varAreaSumType_default/*varAreaSumType_LIST*/;
	
//	/** 分类汇总时填写：汇总基准行/列序号(取值同ExVarDef.index) */
//	public List<Integer> varSumIndexes;
	
	/** lrx 2013-11-21 新增：浮动区在没有已保存信息时，是否按照已存在的多维数据加载(默认为否) */
	public int isAutoExpandByDataCell = isAutoExpandByDataCell_NO;

	/** 浮动区行/列维度定义内容集合, 参见ExVarDef定义 */
	public List<ExVarDef> varDefList;
	
	public ExVarAreaDef cloneNew() {
		ExVarAreaDef rtn = new ExVarAreaDef();
		rtn.varID = varID;
		rtn.varAreaType = varAreaType;
		rtn.varAreaSumType = varAreaSumType;
		rtn.isAutoExpandByDataCell = isAutoExpandByDataCell;
		if (varDefList != null) {
			rtn.varDefList = new ArrayList<ExVarDef>();
			for (ExVarDef vd : varDefList)
				rtn.varDefList.add(vd);
		}
		return rtn;
	}
}
