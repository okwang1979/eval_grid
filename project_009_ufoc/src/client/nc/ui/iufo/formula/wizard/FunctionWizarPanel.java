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
 * �����򵼽���
 * 
 * @author liuchuna
 * @created at 2010-4-16,����04:43:14
 *
 */
public class FunctionWizarPanel extends UfoFuncWizardPanel {

	private static final long serialVersionUID = 6890739393697251055L;

	// ��ʽִ����
	private BaseFmlExecutor m_fmlExecutor = null;
	
	// ����������л�����û�й�ʽִ����ʱ���ô˻�����������ʽ��������˷�����Դ�ж�����˹�ʽ
	private ConditionOfRun conditionRun = null;
	
	// �Ƿ��ù�ʽִ������������ʽ
	private boolean isParseWithFmlExector = true;
	
	// ���㻷��
	private ICalcEnv calcEnv = null;
	
	// ���嵥Ԫ��ʽ��λ��
	private IArea m_area;
	
	// �������ģ��
	private CellsModel cellsModel;
	
	/**
	 * ���췽��
	 * 
	 * @create by liuchuna at 2010-4-22,����06:47:54
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
	 * ��д���෽����ʵ�ֹ�ʽУ��
	 */
	protected void checkUserDefFormula() throws ParseException, CmdSyntaxException {
		checkUserDefFormula(m_area);
	}
	
	/**
	 * У�麯����ʽ
	 * 
	 * @param area
	 * @throws ParseException
	 * @throws CmdSyntaxException 
	 */
	protected void checkUserDefFormula(IArea area) throws ParseException, CmdSyntaxException {
		if(isParseWithFmlExector){
			// ��ʽִ��������У�鹫ʽ
			m_fmlExecutor.checkUserDefFormula(area, strFuncContent);
		}else{
			// ����������л�������У�鹫ʽ
			parseFormula();
		}
	}

	private String parseFormula() throws ParseException, CmdSyntaxException  {
		if (strFuncContent == null
				|| (strFuncContent = strFuncContent.trim()).length() == 0)
			return null;

		// ��������������﷨���
		CmdInterpreter cmdIt = new CmdInterpreter(strFuncContent, conditionRun);
		cmdIt.getCtrlCmd().checkSyntax(conditionRun, true);
		return cmdIt.getCtrlCmd().toString(calcEnv);
	}
	
	public void setArea(IArea area) {
		m_area = area;
	}

	/**
	 * ȥ���������
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
	 * ���ù�ʽִ�������Խ���У�鹫ʽ
	 * 
	 * @create by liuchuna at 2010-4-22,����06:42:38
	 *
	 * @param fmlExecutor
	 */
	public void setFmlExecutor(BaseFmlExecutor fmlExecutor) {
		m_fmlExecutor = fmlExecutor;
	}

	/**
	 * ������ܹ�����ʽִ�������򹹽��������������㻷��������У�鹫ʽ
	 * 
	 * @create by liuchuna at 2010-4-22,����06:43:03
	 *
	 * @param conditionRun
	 */
	public void setConditionOfRun(ConditionOfRun conditionRun){
		this.conditionRun = conditionRun;
		this.isParseWithFmlExector = false;
		this.calcEnv = conditionRun.getCalEnv();
	}
	
	/**
	 * �˴����뷽��˵��
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