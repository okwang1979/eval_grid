package nc.ui.tb.zior.pluginaction.planning;

import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.ml.NCLangRes;
import nc.ui.tb.zior.pluginaction.PluginAction_Seperator;
import nc.ui.tb.zior.pluginaction.print.PluginAction_MultiTasksPrint;
import nc.ui.tb.zior.pluginaction.print.PluginAction_Preview;
import nc.ui.tb.zior.pluginaction.print.PluginAction_Print;
import nc.ui.tb.zior.pluginaction.print.PluginAction_PrintAll;
import nc.vo.mdm.pub.NtbEnv;

import com.ufida.zior.plugin.AbstractPlugin;
import com.ufida.zior.plugin.IPluginAction;

public class PluginAction_FilePlugin extends AbstractPlugin{

	@Override
	protected IPluginAction[] createActions() {
		try{
			//yuyonga
			if(NtbEnv.isOutLineUI){
				return  new IPluginAction[]{
						new PluginAction_Compiling(TbActionName.getName_Compile(),ITbPlanActionCode.code_Compile),
						new PluginAction_CompileSave(TbActionName.getName_CompileSave(),ITbPlanActionCode.code_CompileSave),
						new PluginAction_CompileNoRuleSave(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0407")/*@res "暂存"*/,"CompileNoRuleSave"),
						new PluginAction_CompileCancle(TbActionName.getName_CompileCancle(),ITbPlanActionCode.code_CompileCancle),
						new PluginAction_Refresh(TbActionName.getName_Refresh(),ITbPlanActionCode.code_Refresh),
						PluginAction_Seperator.getInstance(ITbPlanActionCode.code_Seperator_File,NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000255")/*文件*/),
						//new PluginAction_Preview(TbActionName.getName_Preview,ITbPlanActionCode.code_Preview),
						new PluginAction_Print(TbActionName.getName_Print(),ITbPlanActionCode.code_Print),
						// depends to UFO code jar
						new	PluginAction_PrintAll(TbActionName.getName_PrintAll(),ITbPlanActionCode.code_PrintAll),
						new  PluginAction_MultiTasksPrint(TbActionName.getName_MultiTaskPrint(),ITbPlanActionCode.code_MultiTaskPrint)
				};
			}else{
				return  new IPluginAction[]{
						new PluginAction_Compiling(TbActionName.getName_Compile(),ITbPlanActionCode.code_Compile),
						new PluginAction_CompileSave(TbActionName.getName_CompileSave(),ITbPlanActionCode.code_CompileSave),
						new PluginAction_CompileCancle(TbActionName.getName_CompileCancle(),ITbPlanActionCode.code_CompileCancle),
						new PluginAction_Refresh(TbActionName.getName_Refresh(),ITbPlanActionCode.code_Refresh),
						new PluginAction_CompileSave_NoExcel("保存不计算",ITbPlanActionCode.code_CompileSave),
						PluginAction_Seperator.getInstance(ITbPlanActionCode.code_Seperator_File,NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000255")/*文件*/),
						new PluginAction_Preview(TbActionName.getName_Preview(),ITbPlanActionCode.code_Preview),
						new PluginAction_Print(TbActionName.getName_Print(),ITbPlanActionCode.code_Print),
						// depends to UFO code jar
						new	PluginAction_PrintAll(TbActionName.getName_PrintAll(),ITbPlanActionCode.code_PrintAll),
						new  PluginAction_MultiTasksPrint(TbActionName.getName_MultiTaskPrint(),ITbPlanActionCode.code_MultiTaskPrint)
				};
			}
		}catch(Throwable e){
			if(NtbEnv.isOutLineUI){
				return new IPluginAction[]{
						new PluginAction_Compiling(TbActionName.getName_Compile(),ITbPlanActionCode.code_Compile),
						new PluginAction_CompileSave(TbActionName.getName_CompileSave(),ITbPlanActionCode.code_CompileSave),
						new PluginAction_CompileNoRuleSave(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0407")/*@res "暂存"*/,"CompileNoRuleSave"),
						new PluginAction_CompileCancle(TbActionName.getName_CompileCancle(),ITbPlanActionCode.code_CompileCancle),
						new PluginAction_Refresh(TbActionName.getName_Refresh(),ITbPlanActionCode.code_Refresh),
						PluginAction_Seperator.getInstance(ITbPlanActionCode.code_Seperator_File,NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000255")/*文件*/),
						new PluginAction_Preview(TbActionName.getName_Preview(),ITbPlanActionCode.code_Preview),
						new PluginAction_Print(TbActionName.getName_Print(),ITbPlanActionCode.code_Print)
				};
			}else{
				return new IPluginAction[]{
						new PluginAction_Compiling(TbActionName.getName_Compile(),ITbPlanActionCode.code_Compile),
						new PluginAction_CompileSave(TbActionName.getName_CompileSave(),ITbPlanActionCode.code_CompileSave),
						new PluginAction_CompileCancle(TbActionName.getName_CompileCancle(),ITbPlanActionCode.code_CompileCancle),
						new PluginAction_Refresh(TbActionName.getName_Refresh(),ITbPlanActionCode.code_Refresh),
						PluginAction_Seperator.getInstance(ITbPlanActionCode.code_Seperator_File,NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000255")/*文件*/),
						new PluginAction_Preview(TbActionName.getName_Preview(),ITbPlanActionCode.code_Preview),
						new PluginAction_Print(TbActionName.getName_Print(),ITbPlanActionCode.code_Print)
				};
			}
		}

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