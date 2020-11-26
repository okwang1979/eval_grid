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
		// �ж��Ƿ�ѡ������
		if (tasks == null || tasks.length == 0)
			return;
		boolean flag = getContext().isBrowse();
		CtrlSchemeStates.getInstance().clear();
		if(!flag){   //��ʼ̬
//			ui.showHintMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000019")/*����ͣ�÷���..*/);
			CtlSchemeCTL.stopCtrlScheme(tasks);
//			ui.showHintMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000020")/*ͣ�÷������*/);
		}else {  //���̬
			//��ǰ�û���¼��Ϣ
			UserLoginVO userloginvo = new UserLoginVO();
			userloginvo.setPk_user(WorkbenchEnvironment.getInstance().getLoginUser().getPrimaryKey());
			//���Ʒ���ͣ��
//			List<Cell> datacells = ui.getTaskWorkbookViewPanel().getSelectedCells();
			List<Cell> datacells =((TBSheetViewer) this.getCurrentView()).getSelectedCell();
			if(datacells.size()<1){
				throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000494")/*��ѡ��һ������һ�����ϵ�Ԫ��ͣ�ÿ���*/);
			}
			if(datacells.size()==1){
				Cell cell = datacells.get(0);
				CellFmlInfo fmlInfo = (CellFmlInfo)cell.getExtFmt(TbIufoConst.formulaKey);
				String express = fmlInfo==null ? null : fmlInfo.getFmlexpress();
				if(express == null){
					throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0236")/*@res "�õ�Ԫ���²����ڿ��Ʒ���,��ѡ���п��Ʒ����ĵ�Ԫ��ͣ��"*/);
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
				throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000000")/*�õ�Ԫ���²����ڹ�ʽ,��ѡ���й�ʽ�ĵ�Ԫ��ͣ��*/);
			}
			if(list.size()>0) {
				showMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000019")/*����ͣ�÷���..*/);
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
					throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000078")/*û����Ҫͣ�õĿ��Ʒ���*/);
				showMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000020")/*ͣ�÷������*/);
			}
//			if(list.size()>0)
//			   ui.showHintMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000019")/*����ͣ�÷���..*/);
//			   CtlSchemeCTL.stopCtrlScheme(list.toArray(new DataCell[0]));
//			   ui.showHintMessage(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000020")/*ͣ�÷������*/);
		}
		try {
			//�б�ˢ��
			if (flag) {
//				TBSheetViewer view = getCurrentViewer().getViewManager().getCurrentTbSheetViewer();
				getCurrentViewer().getViewManager().refresh(getCurrentViewer());
			}
			//��Ƭˢ��
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
			return NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000020")/*ͣ�÷������*/;
		}

}

