package nc.ui.tb.zior.pluginaction.planDaily.submit;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.ms.tb.control.CtlSchemeCTL;
import nc.ms.tb.control.CtrlSchemeStates;
import nc.ms.tb.formula.core.CubeHelper;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.UIDialog;
import nc.ui.tb.zior.TBSheetViewer;
import nc.ui.tb.zior.pluginaction.AbstractTbRepPluginAction;
import nc.ui.tb.zior.pluginaction.TbPluginActionDescriptor;
import nc.view.tb.control.ZSelUseCtlDlg;
import nc.view.tb.plan.TbPlanListPanel;
import nc.vo.mdm.cube.DataCell;
import nc.vo.pub.BusinessException;
import nc.vo.tb.form.iufo.CellExtInfo;
import nc.vo.tb.form.iufo.CellFmlInfo;
import nc.vo.tb.form.iufo.TbIufoConst;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.rule.IdCtrlformulaVO;
import nc.vo.tb.rule.IdCtrlschemeVO;
import nc.vo.tb.task.MdTask;

import com.ufida.zior.plugin.PluginKeys.XPOINT;
import com.ufsoft.table.Cell;

public class PluginAction_OnStopCtrlScheme extends AbstractTbRepPluginAction{

	private final static String id="tb.report.task.view";
	public PluginAction_OnStopCtrlScheme(String name, String code) {
		super(name, code);
		// TODO Auto-generated constructor stub
	}
	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		// TODO Auto-generated method stub
		TbPluginActionDescriptor tad = new TbPluginActionDescriptor();
		tad.setName(TbActionName.getName_StopCtrlScheme());
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
//			ui.showHintMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000019")/*正在停用方案..*/);
			CtlSchemeCTL.stopCtrlScheme(tasks);
//			ui.showHintMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000020")/*停用方案完成*/);
		}else {  //浏览态
			//当前用户登录信息
			UserLoginVO userloginvo = new UserLoginVO();
			userloginvo.setPk_user(WorkbenchEnvironment.getInstance().getLoginUser().getPrimaryKey());
			//控制方案停用
//			List<Cell> datacells = ui.getTaskWorkbookViewPanel().getSelectedCells();
			List<Cell> datacells =((TBSheetViewer) this.getCurrentView()).getSelectedCell();
			if(datacells.size()<1){
				throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000494")/*请选择一个或者一个以上单元格停用控制*/);
			}
			if(datacells.size()==1){
				Cell cell = datacells.get(0);
				CellFmlInfo fmlInfo = (CellFmlInfo)cell.getExtFmt(TbIufoConst.formulaKey);
				String express = fmlInfo==null ? null : fmlInfo.getFmlexpress();
				if(express == null){
					throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0236")/*@res "该单元格下不存在控制方案,请选择有控制方案的单元格停用"*/);
				}
			}
			ArrayList<DataCell> list = new ArrayList<DataCell> ();
			ArrayList<String> expressList=new ArrayList<String>();
			for(int n=0;n<datacells.size();n++){
				Cell cell = datacells.get(n);
				CellFmlInfo fmlInfo = (CellFmlInfo)cell.getExtFmt(TbIufoConst.formulaKey);
				String express = fmlInfo==null ? null : fmlInfo.getFmlexpress();
				if(express == null){
					expressList.add(express);
					continue;
				}
				DataCell datacell = cell==null ? null : (DataCell)cell.getExtFmt(TbIufoConst.dataccellKey);
				express = express.replaceAll("\'", "\"");
	//			String pk_formula = fmlInfo.getBusiRulePk();
				if(datacell == null){
					CellExtInfo cInfo = (CellExtInfo) cell.getExtFmt(TbIufoConst.tbKey);
					datacell = new DataCell(CubeHelper.getCubeDefByCode(cInfo.getCubeCode()), cInfo.getDimVector());
				}
				list.add(datacell);
			}
			if(expressList.size()==datacells.size()){
				throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000000")/*该单元格下不存在公式,请选择有公式的单元格停用*/);
			}
			if(list.size()>0) {
				showMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000019")/*正在停用方案..*/);
				HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> ctlMapCube = CtlSchemeCTL.filterStopCtrlScheme(list.toArray(new DataCell[0]));
				if(ctlMapCube != null && !ctlMapCube.isEmpty()) {
					boolean isNeedSel = false;
					IdCtrlformulaVO[] allCtlFml = ctlMapCube.keySet().toArray(new IdCtrlformulaVO[0]);
					String pkParent = allCtlFml[0].getPk_parent();
					for(IdCtrlformulaVO vo : allCtlFml) {
						if(!vo.getPk_parent().equals(pkParent))
							isNeedSel = true;
					}
					if(ctlMapCube.keySet().size() > 1 && isNeedSel) {
						ZSelUseCtlDlg selDlg = new ZSelUseCtlDlg(ctlMapCube);
						if(selDlg.showModal() == UIDialog.ID_OK) {
							ctlMapCube = selDlg.getSelectedVOs0();
						} else {
//							ui.showHintMessage("");
							return;
						}
					}
					if(ctlMapCube!=null&&!ctlMapCube.isEmpty()){
//						CtlSchemeCTL.deleteCtrlScheme(ctlMapCube);
						Map<String, List<String>> deleteContent = new HashMap<String, List<String>>();
						for(Map.Entry<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> entry : ctlMapCube.entrySet()) {
							IdCtrlformulaVO vo = entry.getKey();
							ArrayList<IdCtrlschemeVO> schemelist = entry.getValue();
							List<String> pkSchemeList = new ArrayList<String>();
							for(IdCtrlschemeVO schemeVO : schemelist)
								pkSchemeList.add(schemeVO.getPrimaryKey());
							deleteContent.put(vo.getPrimaryKey(), pkSchemeList);
						}
						CtlSchemeCTL.deleteCtrlScheme(deleteContent);
					}
				 } else
					throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000078")/*没有需要停用的控制方案*/);
				showMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000020")/*停用方案完成*/);
			}
//			if(list.size()>0)
//			   ui.showHintMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000019")/*正在停用方案..*/);
//			   CtlSchemeCTL.stopCtrlScheme(list.toArray(new DataCell[0]));
//			   ui.showHintMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000020")/*停用方案完成*/);
		}
		try {
			//列表刷新
			if (flag) {
//				TBSheetViewer view = getCurrentViewer().getViewManager().getCurrentTbSheetViewer();
				getCurrentViewer().getViewManager().refresh(getCurrentViewer());
			}
			//卡片刷新
			else{
//				ui.getTaskWorkbookViewPanel().refresh();
				//((TbPlanListPanel)getMainboard().getView(id)).onZiorRefresh();
				((TbPlanListPanel)getMainboard().getView(id)).onZiorRefresh(tasks);
			}
		} catch (Exception ex) {
			throw new BusinessException(ex);
		}
	}

	 protected String getLeftBottomInfo(){
			return NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000020")/*停用方案完成*/;
		}

}

