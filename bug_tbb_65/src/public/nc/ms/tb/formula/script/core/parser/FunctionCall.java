package nc.ms.tb.formula.script.core.parser;

import java.text.DecimalFormat;
import java.util.*;

import nc.ms.tb.formula.core.cutcube.ICutCube;
import nc.ms.tb.formula.script.Calculator;
import nc.ms.tb.formula.script.Function;
// Referenced classes of package com.fr.report.script.core.parser:
//            AbstractNode, InterpreterError, Node, UtilEvalError, 
//            ConditionalExpression
import nc.ms.tb.formula.script.NameSpace;
import nc.ms.tb.formula.script.core.DefaultNameSpace;
import nc.ms.tb.formula.script.core.ExcelAreaStack;
import nc.ms.tb.formula.script.core.ObjectArray;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.tb.formula.IFormulaDataCell;

public class FunctionCall extends AbstractNode {

	public FunctionCall() {
	}

	public void setName(String s) {
		if (s == null) {
			return;
		} else {
			name = s.trim().toUpperCase();
			return;
		}
	}

	public void addArgument(Object conditionalexpression) {
		if (arguments == null)
			arguments = new ArrayList();
		arguments.add(conditionalexpression);
	}

	public Object eval(Calculator calculator) throws UtilEvalError {
		NameSpace namespace = calculator.getNameSpace();
		if (namespace == null)
			namespace = DefaultNameSpace.getInstance();
		Function function = namespace.getMethod(name, calculator);
		if (function == null){
			String info = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule", "01420rule-000034")/*函数*/;
			info=info+"["+name+"]";
			info = info+":"+"预算系统不支持此函数";
			NtbLogger.error(info);
			throw new BusinessRuntimeException(info);
		}
			
		
		
		
		function.setCalculator(calculator);
		Stack stack = new Stack();
		if (arguments != null) {
			int i = arguments.size();
			for (int j = i - 1; j >= 0; j--) {
				ExcelAreaStack paramStack = new ExcelAreaStack();
				Object obj = arguments.get(j);
				if (!(obj instanceof Node))
					continue;
				//取单元格值类型.
				if(function.isCellValue()){
					obj = ((Node) obj).eval(calculator);
				}else{
					if(function.cellPlace()==-1){
						calculator.setCellValueIsCell(true);
						obj = ((Node) obj).eval(calculator);
						calculator.setCellValueIsCell(false);
					}else if(j==function.cellPlace()){
						calculator.setCellValueIsCell(true);
						obj = ((Node) obj).eval(calculator);
						calculator.setCellValueIsCell(false);
					}else{
						obj = ((Node) obj).eval(calculator);
					}
					
				}
				
				if (obj instanceof ObjectArray){
					ObjectArray  oa = (ObjectArray)obj;
					List<Object> vales = ((ObjectArray) obj).getValueList();
					
					for(int u=vales.size()-1;u>=0;u--){
						
						stack.push(vales.get(u));
					}
					paramStack.addAll(((ObjectArray) obj).getValueList());
					paramStack.setColCount(oa.getColCount());
					paramStack.setRowCount(oa.getRowCount());
				}
					
				else{
					stack.push(obj);
					paramStack.push(obj);
				}					
				if(function.isRecountParam()){
					function.getParamStack().add(paramStack);
				}
			}

		}
		Object returnObj = function.run(stack);
		function.getParamStack().clear();
		function.setCalculator(null);
		return returnObj;
	}

	public void collect(Map map) {
		Object obj = (List) map.get(name);
		if (obj == null) {
			obj = new ArrayList();
			map.put(name, obj);
		}
		if (arguments != null) {
			ArrayList arraylist = new ArrayList();
			((List) (obj)).add(arraylist);
			int i = 0;
			for (int j = arguments.size(); i < j; i++) {
				arraylist.add(arguments.get(i).toString()
						.replaceAll("[\"']", ""));
			}

		}
	}

	public String toString() {
		StringBuffer stringbuffer = new StringBuffer();
		stringbuffer.append(name).append("(");
		for (int i = 0; arguments != null && i < arguments.size(); i++) {
			if (i != 0)
				stringbuffer.append(",");
			stringbuffer.append(arguments.get(i));
		}

		stringbuffer.append(")");
		return stringbuffer.toString();
	}
	
	
	
	@Override
	public String toValue(Calculator calculator) {
		try {
			if (name.startsWith("UFIND") || name.startsWith("PREFIND")
					|| name.startsWith("CELL_IUFO")
					|| name.startsWith("FLEXEXPRESS") || name.startsWith("UFO")) {
				return this.toString();
			}
			if ("FIND".equals(name)) {
				Object value = null;
				if (calculator.getContext().getWorkBook() != null) {
					int oldType = calculator.getContext().getCutCubeType();
					calculator.getContext().setCutCubeType(
							ICutCube.TYPE_WORKBOOK_DEFAULT);
					value = eval(calculator);
					calculator.getContext().setCutCubeType(oldType);

				} else {
					value = eval(calculator);
				}

				return formatValue(value);
			}
			Object value = eval(calculator);
			return formatValue(value);
		} catch (UtilEvalError e) {
			NtbLogger.error(e);
			return "";
		}
	}
	

	
	private String formatValue(Object value){
		double rtn = 0;
		if (value instanceof Collection) {

			for (Object obj : (Collection) value) {
				IFormulaDataCell cell = (IFormulaDataCell) obj;
				if (cell.getDataCell() != null
						&& cell.getDataCell().getCellValue() != null&&cell.getDataCell().getCellValue()
								.getValue()!=null) {
					rtn = rtn
							+ cell.getDataCell().getCellValue()
									.getValue().doubleValue();
				}

			}

		} else if (value instanceof Number) {
			rtn = ((Number) value).doubleValue();
		}
		
		else if(value instanceof IFormulaDataCell){
				IFormulaDataCell cell = (IFormulaDataCell)value;
				if(cell.getDataCell()!=null&&cell.getDataCell().getCellValue()!=null&& cell.getDataCell().getCellValue().getValue()!=null){
					rtn =  cell.getDataCell().getCellValue().getValue().doubleValue();
				}
		}
		
		
		else {
			rtn = 0;
		}

		DecimalFormat fommat = new DecimalFormat("#0.00000000");
		return fommat.format(rtn);
	}


	@Override
	public String toDesc(DescriptionContext descContext) {

		NameSpace namespace = DefaultNameSpace.getInstance();
		Function function = namespace.getMethod(name, new Calculator());
		if (function == null)
			throw new InterpreterError("no function found: " + name);
		Stack stack = new Stack();
		if (arguments != null) {
			int i = arguments.size();
			for (int j = i - 1; j >= 0; j--) {
				Object obj = arguments.get(j);
				if (!(obj instanceof Node))
					continue;
				obj = ((AbstractNode) obj).toDesc(descContext);
//				if (obj instanceof ObjectArray)
//					stack.addAll(((ObjectArray) obj).getValueList());
				if (obj instanceof String) {
					String stringParam = (String) obj;
					if (stringParam.startsWith("'")
							&& stringParam.endsWith("'")) {
						stack.push(stringParam.substring(1,
								stringParam.length() - 1));
					} else {
						stack.push(obj);
					}

				} else {
					stack.push(obj);
				}

			}

		}
		String express = function.toDesc(stack, descContext);
		function.getParamStack().clear();
		function.setCalculator(null);
		return express;

	}

	@Override
	public List<Node> getNodes(List<Node> nodeList) {
		nodeList.add(this);
		return nodeList;
	}

	public String getName() {
		return name;
	}

	public List getArguments() {
		return arguments;
	}

	private String name;
	private List arguments;
}
