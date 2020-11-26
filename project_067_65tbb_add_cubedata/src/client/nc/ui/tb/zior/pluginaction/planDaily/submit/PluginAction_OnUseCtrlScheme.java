package nc.ui.tb.zior.pluginaction.planDaily.submit;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.ms.mdm.convertor.IStringConvertor;
import nc.ms.mdm.convertor.StringConvertorFactory;
import nc.ms.tb.control.CtlSchemeCTL;
import nc.ms.tb.control.CtrlSchemeStates;
import nc.ms.tb.formula.core.CubeHelper;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIDialog;
import nc.ui.tb.zior.TBSheetViewer;
import nc.ui.tb.zior.pluginaction.AbstractTbRepPluginAction;
import nc.ui.tb.zior.pluginaction.TbPluginActionDescriptor;
import nc.view.tb.control.ZSelUseCtlDlg;
import nc.view.tb.plan.TbPlanListPanel;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.cube.DimVector;
import nc.vo.pub.BusinessException;
import nc.vo.tb.control.CtrlSchemeVO;
import nc.vo.tb.form.iufo.CellExtInfo;
import nc.vo.tb.form.iufo.CellFmlInfo;
import nc.vo.tb.form.iufo.TbIufoConst;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.rule.AllotFormulaVo;
import nc.vo.tb.rule.IdCtrlformulaVO;
import nc.vo.tb.rule.IdCtrlschemeVO;
import nc.vo.tb.rule.excel.DimFormulaInfo;
import nc.vo.tb.task.ITaskStatus;
import nc.vo.tb.task.MdTask;

import com.ufida.zior.plugin.PluginKeys.XPOINT;
import com.ufsoft.table.Cell;
import com.ufsoft.table.format.IDataFormat;
import com.ufsoft.table.format.INumberFormat;

public class PluginAction_OnUseCtrlScheme extends AbstractTbRepPluginAction{

	private static int formulaCount = 3000;
	private final static String id="tb.report.task.view";
	public PluginAction_OnUseCtrlScheme(String name, String code) {
		super(name, code);
		// TODO Auto-generated constructor stub
	}
	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		// TODO Auto-generated method stub
		TbPluginActionDescriptor tad = new TbPluginActionDescriptor();
		tad.setName(TbActionName.getName_StartCtrlScheme());
		tad.setExtensionPoints(new XPOINT[]{XPOINT.MENU});
		return tad;

	}

	@Override
	public void actionPerformed(ActionEvent actionevent)
			throws BusinessException {
		// TODO Auto-generated method stub
		MdTask[] tasks = this.getContext().getTasks();
		// 判断是否选中任务
		if (tasks == null || tasks.length == 0)
			return;
		boolean flag = getContext().isBrowse();
		CtrlSchemeStates.getInstance().clear();
		if(!flag){   //初始态
			List<MdTask> tasksAfterApprove = new ArrayList<MdTask>();
			for(MdTask task : tasks)
				if(task.getPlanstatus().equals(ITaskStatus.APPROVE_PASS)){
					tasksAfterApprove.add(task);

				}
			if(tasksAfterApprove.size() == 0) {
//				throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000094")/*任务未生效，不能启动控制方案*/);
				MessageDialog.showWarningDlg(null, NCLangRes.getInstance().getStrByID("tbb_bean", "01420ben_000050")/*警告*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000094")/*任务未生效，不能启动控制方案*/);
				return;
			}

//			this.showMessage("启动控制方案中...");
//			String msg = CtlSchemeCTL.onUseAllCrelSchemeInClient(tasks);
			HashMap<String,List<AllotFormulaVo>> map = CtlSchemeCTL.allotFormula(tasksAfterApprove.toArray(new MdTask[0]));
			Iterator iterator = map.entrySet().iterator();
			int count =0;
			while(iterator.hasNext()){
				Map.Entry obj = (Map.Entry)iterator.next();
				List<AllotFormulaVo> values = (List<AllotFormulaVo>)obj.getValue();
				count+=(values==null?0:values.size());
			}
			String msg = null;
			Object result = null;
//			if(count>formulaCount){
//				if(MessageDialog.showOkCancelDlg(this.getMainboard(), getName(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0401")/*@res "控制方案过多,是否通过异步的方式,启动控制方案?"*/)==MessageDialog.ID_OK){
//					msg = CtlSchemeCTL.asynStartControl(tasksAfterApprove.toArray(new MdTask[0]));
//				}else{
//					result = CtlSchemeCTL.onUseAllCrelSchemeInClient(tasksAfterApprove.toArray(new MdTask[0]), map);
//					if(result == null)
//						throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000003")/*没有需要启用的控制方案*/);
//				}
//			}else{
				result = CtlSchemeCTL.onUseAllCrelSchemeInClient(tasksAfterApprove.toArray(new MdTask[0]), map);
				if(result == null)
					throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000003")/*没有需要启用的控制方案*/);
//			}
			
			

//			if(msg != null)
////				ui.handleException(new BusinessException(msg));
//				MessageDialog.showWarningDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000007")/*警告*/, msg);
			this.showMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000021")/*启动控制方案完成*/);
		}else {  //浏览态
			//当前用户登录信息
			UserLoginVO userloginvo = new UserLoginVO();
			userloginvo.setPk_user(WorkbenchEnvironment.getInstance().getLoginUser().getPrimaryKey());
			//控制方案启动
//			List<Cell> datacells = ui.getTaskWorkbookViewPanel().getSelectedCells();
			List<Cell> datacells =((TBSheetViewer) this.getCurrentView()).getSelectedCell();
			MdTask mdTask = tasks[0];
			if(!mdTask.getPlanstatus().equals(ITaskStatus.APPROVE_PASS)) {
				MessageDialog.showWarningDlg(null, NCLangRes.getInstance().getStrByID("tbb_bean", "01420ben_000050")/*警告*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000094")/*任务未生效，不能启动控制方案*/);
				return;
			}
			if(datacells.size()<1){
				throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000496")/*请选择一个或者一个以上单元格启动控制*/);
			}
			ArrayList<CtrlSchemeVO> list = filterDataSchemeVO(datacells,mdTask);  //启动控制方案
			ArrayList<String> messageRes = new ArrayList<String>();
//			if(!mdTask.getPlanstatus().equals(ITaskStatus.APPROVE_PASS)) {
//				throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000094")/*任务未生效，不能启动控制方案*/);
//			}
			if(list == null || list.size() == 0)
				throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000003")/*没有需要启用的控制方案*/);
			boolean isNeedSel = false;
			if(list != null && list.size() > 0) {
				String pk = list.get(0).getPk_formula();
				for(CtrlSchemeVO vo : list) {
					if(!vo.getPk_formula().equals(pk))
						isNeedSel = true;
				}
			}

			if(list != null &&list.size() > 1 && isNeedSel) {
				ZSelUseCtlDlg selDlg = new ZSelUseCtlDlg(list);
				if(selDlg.showModal() == UIDialog.ID_OK) {
					list = selDlg.getSelectedVOs();
				} else {
					return;
				}
			}

//			this.showMessage("启动控制方案中...");
			String[] info = CtlSchemeCTL.startCtrlScheme(list);  //启动控制方案
			this.showMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000021")/*启动控制方案完成*/);
			for(int i=0;i<info.length;i++){
				messageRes.add(info[i]);
			}
			if(messageRes!=null&&messageRes.size()!=0){
				StringBuffer buffer =  new StringBuffer();
				for(int j=0;j<messageRes.size();j++){
					buffer.append(messageRes.get(j));
					buffer.append("\n");
				}
				buffer.replace(buffer.length() - 1, buffer.length(), "");
				MessageDialog.showWarningDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000007")/*警告*/, buffer.toString());
			}
		}
		try {
			//列表刷新
			if (flag) {
//                getMainboard()
//				TBSheetViewer view = getCurrentViewer().getViewManager().getCurrentTbSheetViewer();
				getCurrentViewer().getViewManager().refresh(getCurrentViewer());
//				getCurrentViewer().getViewManager().refresh(getCurrentViewer());
			}
			//卡片刷新
			else{
				//((TbPlanListPanel)getMainboard().getView(id)).onZiorRefresh();
				((TbPlanListPanel)getMainboard().getView(id)).onZiorRefresh(tasks);
			}
		} catch (Exception ex) {
//			getMainboard().handleException(ex);
		}
//		getMainboard().showHintMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000021")/*启动控制方案完成*/);

	}

	/**启动控制方案的过滤*/
	private ArrayList<CtrlSchemeVO> filterDataSchemeVO(List<Cell> datacells,MdTask mdTask) throws BusinessException {
		ArrayList<CtrlSchemeVO> listSchemeVO = new ArrayList<CtrlSchemeVO> ();
		ArrayList<CtrlSchemeVO> tmpCtrlSchemeVO = getAllFormula(datacells,mdTask.getPrimaryKey());
		ArrayList<MdTask> tasks = new ArrayList<MdTask> ();
		tasks.add(mdTask);
		HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> map = getStartCtrlScheme(tasks).get(mdTask.getPrimaryKey());
		//比较,有哪些公式已经被启用,哪些还没有启用,没有启用的返回
		for(int n=0;n<tmpCtrlSchemeVO.size();n++){
			CtrlSchemeVO vo = tmpCtrlSchemeVO.get(n);
		    if(!isStartCtrlScheme(vo,map)){
		    	listSchemeVO.add(vo);
		    }
		}
		return listSchemeVO;
	}

	private ArrayList<CtrlSchemeVO> getAllFormula(List<Cell> datacells,String pk_mdTask) throws BusinessException {
		ArrayList<CtrlSchemeVO> listVo = new ArrayList<CtrlSchemeVO> ();
		for(int n=0;n<datacells.size();n++){
			Cell cell = datacells.get(n);
			CellFmlInfo fmlInfo = (CellFmlInfo)cell.getExtFmt(TbIufoConst.formulaKey);
			if(fmlInfo == null)
				continue;
			String pk_formula = fmlInfo.getFormulaVoPk();
			if(pk_formula == null)
				continue;
			DataCell datacell = (DataCell)cell.getExtFmt(TbIufoConst.dataccellKey);
			if(datacell == null){
				CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
				CubeDef cubeDef = CubeHelper.getCubeDefByCode(cInfo.getCubeCode());
				datacell = new DataCell(cubeDef, cInfo.getDimVector());
			}
			for(DimFormulaInfo vo : fmlInfo.getFmlList()) {
				String formulaExp = vo.getExpress().replace("\'", "\"");
				String formulaBd = formatBudgetData(cell, formulaExp);
				CtrlSchemeVO schemeVO = new CtrlSchemeVO(formulaBd, vo.getFormulaVoPk(), datacell, pk_mdTask);
				listVo.add(schemeVO);
			}
		}
		return listVo;
	}

	//临时处理，预算数的精度与单元格保持一致
	public String formatBudgetData(Cell cell, String formula) {
		IDataFormat format = cell.getFormat().getDataFormat();
		if(format instanceof INumberFormat) {
			int digits = ((INumberFormat) format).getDecimalDigits();
			return CtlSchemeCTL.formatBudgetData(digits, formula);
		}
		return formula;
	}

	private boolean isStartCtrlScheme(CtrlSchemeVO vo,HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> map){
		boolean isStartCtrlScheme = false;
		Iterator iter = map.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry obj = (Map.Entry)iter.next();
			IdCtrlformulaVO formulavo = (IdCtrlformulaVO)obj.getKey();
			DataCell datacell = vo.getAllotCell();
			DimVector dimvector = datacell.getDimVector();
			IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
			String pk_dimvector = cvt.convertToString(dimvector);
			if(vo.getPk_formula().equals(formulavo.getPk_parent())&&pk_dimvector.equals(formulavo.getPk_dimvector())){
				isStartCtrlScheme = true;
				break;
			}
		}
		return isStartCtrlScheme;
	}

	/**得到某些计划下所有启动的控制方案*/
	private HashMap<String,HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>>> getStartCtrlScheme(ArrayList<MdTask> mdTaskList) throws BusinessException {
		HashMap<String,HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>>> map = new HashMap<String,HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>>> ();
		for(int n=0;n<mdTaskList.size();n++){
			StringBuffer sWhere = new StringBuffer();
			String pk_mdTask = mdTaskList.get(n).getPrimaryKey();
			sWhere.append("isstarted = 'Y' and pk_plan = '").append(pk_mdTask).append("'");
			HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> startCtrlscheme= CtlSchemeCTL.queryCtrlSchemeByCtrlformula(sWhere.toString());
			map.put(pk_mdTask, startCtrlscheme);
		}
		return map;
	}

	 protected String getLeftBottomInfo(){
			return NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000413")/*启动完成*/;
		}
}