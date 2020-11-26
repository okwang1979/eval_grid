package nc.ui.tb.zior.pluginaction.sum;


import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.tb.zior.budgetdecomposition.PluginAction_BudgetDecomposited;

import com.ufida.zior.plugin.AbstractPlugin;
import com.ufida.zior.plugin.IPluginAction;
/**
 * 汇总插件--包含汇总下级,差额汇总,自定义汇总
 * @author pengzhena
 *
 */
@SuppressWarnings("restriction")
public class PluginAction_sumPlugin extends AbstractPlugin {

	@Override
	protected IPluginAction[] createActions() {
	
		return new IPluginAction[] { 
				new PluginAction_SumSingleSheet(TbActionName.getName_Sum(),ITbPlanActionCode.code_SumSingleSheet),
				new PluginAction_SumDif(TbActionName.getName_SumDif(),ITbPlanActionCode.code_SumDif),
				new PluginAction_BudgetDecomposited(TbActionName.getName_SumDecomposition(),ITbPlanActionCode.code_SumDecomposition),
				new PluginAction_SumSingleSheet_NoFormula(TbActionName.getName_Sum(),ITbPlanActionCode.code_SumSingleSheet)
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
