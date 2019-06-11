package nc.ui.iufo.formula.wizard;

import nc.ui.iufo.formula.common.ComFmlEditPane;

import com.ufida.dataset.IContext;
import com.ufsoft.iufo.fmtplugin.formula.BaseFmlExecutor;
import com.ufsoft.iufo.util.parser.UfoSimpleObject;
import com.ufsoft.script.base.ICalcEnv;
import com.ufsoft.script.cmdcontrol.CmdInterpreter;
import com.ufsoft.script.cmdcontrol.CmdSyntaxException;
import com.ufsoft.script.cmdcontrol.ConditionOfRun;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.script.function.FuncListInst;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.IArea;

/**
 * 函数向导界面
 * 
 * @author liuchuna
 * @created at 2010-4-16,下午04:43:14
 *
 */
public class FunctionWizarPanel extends UfoFuncWizardPanel {

	private static final long serialVersionUID = 6890739393697251055L;

	// 公式执行器
	private BaseFmlExecutor m_fmlExecutor = null;
	
	// 批命令的运行环境，没有公式执行器时，用此环境来解析公式。用于审核方案资源中定义审核公式
	private ConditionOfRun conditionRun = null;
	
	// 是否用公式执行器来解析公式
	private boolean isParseWithFmlExector = true;
	
	// 计算环境
	private ICalcEnv calcEnv = null;
	
	// 定义单元公式的位置
	private IArea m_area;
	
	// 表格数据模型
	private CellsModel cellsModel;
	
	/**
	 * 构造方法
	 * 
	 * @create by liuchuna at 2010-4-22,下午06:47:54
	 *
	 * @param comFmlEditDlg
	 * @param cellsPane
	 * @param contextVo
	 * @param function
	 * @param ufoFuncList
	 * @param cellsModel
	 */
	public FunctionWizarPanel(ComFmlEditPane comFmlEditDlg, IContext context,
			UfoSimpleObject function, FuncListInst ufoFuncList,CellsModel cellsModel) {
		super(comFmlEditDlg, function, ufoFuncList, context);
		this.cellsModel = cellsModel;
	}
	
	/**
	 * 复写父类方法，实现公式校验
	 */
	protected void checkUserDefFormula() throws ParseException, CmdSyntaxException {
		checkUserDefFormula(m_area);
	}
	
	/**
	 * 校验函数格式
	 * 
	 * @param area
	 * @throws ParseException
	 * @throws CmdSyntaxException 
	 */
	protected void checkUserDefFormula(IArea area) throws ParseException, CmdSyntaxException {
		if(isParseWithFmlExector){
			// 公式执行器解析校验公式
			m_fmlExecutor.checkUserDefFormula(area, strFuncContent);
		}else{
			// 批命令的运行环境解析校验公式
			parseFormula();
		}
	}

	private String parseFormula() throws ParseException, CmdSyntaxException  {
		if (strFuncContent == null
				|| (strFuncContent = strFuncContent.trim()).length() == 0)
			return null;

		// 调用批命令进行语法检查
		CmdInterpreter cmdIt = new CmdInterpreter(strFuncContent, conditionRun);
		cmdIt.getCtrlCmd().checkSyntax(conditionRun, true);
		return cmdIt.getCtrlCmd().toString(calcEnv);
	}
	
	public void setArea(IArea area) {
		m_area = area;
	}

	/**
	 * 去多余的引号
	 * 
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unused")
	private String toDelQuotes(String str) {
		if (str == null || str.length() == 0 || str.equals("''")) {
			return "";
		}
		if (str.charAt(0) == '\'' && str.charAt(str.length() - 1) == '\'') {
			return str.substring(1, str.length() - 1);
		} else {
			return str;
		}
	}
	
	/**
	 * 设置公式执行器，以解析校验公式
	 * 
	 * @create by liuchuna at 2010-4-22,下午06:42:38
	 *
	 * @param fmlExecutor
	 */
	public void setFmlExecutor(BaseFmlExecutor fmlExecutor) {
		m_fmlExecutor = fmlExecutor;
	}

	/**
	 * 如果不能构建公式执行器，则构建运行条件及计算环境来解析校验公式
	 * 
	 * @create by liuchuna at 2010-4-22,下午06:43:03
	 *
	 * @param conditionRun
	 */
	public void setConditionOfRun(ConditionOfRun conditionRun){
		this.conditionRun = conditionRun;
		this.isParseWithFmlExector = false;
		this.calcEnv = conditionRun.getCalEnv();
	}
	
	/**
	 * 此处插入方法说明
	 * 
	 * @param evt
	 *            FocusEvent
	 */
//	@Override
//	public void focusGained(FocusEvent evt) {
//		for (int i = 0; i < paramNum; i++) {
//			if (evt.getSource().equals(funcParamfields[i])
//					|| evt.getSource().equals(paramRefBtns[i])) {
//				m_FocusPlace = i;
//				return;
//			}
//		}
//		m_FocusPlace = -1;
//	}
	
	public void setCellsModel(CellsModel cellsModel) {
		this.cellsModel = cellsModel;
	}

	public CellsModel getCellsModel() {
		return cellsModel;
	}
	
}