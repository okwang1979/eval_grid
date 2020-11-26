package nc.ui.tb.zior.pluginaction.sum;

import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.TbActionName;

import com.ufida.zior.plugin.AbstractPlugin;
import com.ufida.zior.plugin.IPluginAction;
//import nc.ui.tb.zior.budgetdecomposition.PluginAction_BudgetDecomposited;
/**
 * 日常执行的分解菜单
 * <b>Application name:</b>NC63<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2014 用友软件股份有限公司版权所有。<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2014-6-6<br>
 * @author：liyingm
 * @version V6.31
 */
public class PluginAction_DailySumPlugin extends AbstractPlugin {

	@Override
	protected IPluginAction[] createActions() {
		// TODO Auto-generated method stub
	
		return new IPluginAction[] { 
				new PluginAction_SumSingleSheet(TbActionName.getName_Sum(),ITbPlanActionCode.code_SumSingleSheet),
				new PluginAction_SumSingleSheet_NoFormula(TbActionName.getName_Sum(),ITbPlanActionCode.code_SumSingleSheet)
//				new PluginAction_BudgetDecomposited(TbActionName.getName_SumDecomposition(),ITbPlanActionCode.code_SumDecomposition)
		};
	}

	@Override
	public void startup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

}