package nc.ui.hbbb.adjustrep.input.plugin.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTabbedPane;

import nc.pub.iufo.cache.IUFOCacheManager;
import nc.ui.gl.contrast.rule.util.UITable;
import nc.ui.hbbb.adjustrep.input.edit.AdjustCombRepDataEditor;
import nc.ui.hbbb.adjustrep.input.edit.AdjustRepDataEditor;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UITabbedPane;
import nc.util.hbbb.datacenter.DataCenterType;
import nc.util.hbbb.input.HBBBTableInputActionHandler;
import nc.util.hbbb.input.hbreportdraft.HBBBDraftTableInputActionHandler;
import nc.util.hbbb.workdraft.pub.IWorkDraft;
import nc.util.hbbb.workdraft.pub.ReportType;
import nc.util.hbbb.workdraft.pub.WorkDraft;
import nc.vo.iufo.measure.MeasureVO;

import com.ufida.zior.console.ActionHandler;
import com.ufida.zior.plugin.IPluginActionDescriptor;
import com.ufida.zior.plugin.PluginActionDescriptor;
import com.ufida.zior.plugin.PluginKeys.XPOINT;
import com.ufsoft.iufo.inputplugin.measure.MeasureFmt;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.Cell;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.CellsPane;
import com.ufsoft.table.ReportTable;
import com.ufsoft.table.SelectModel;

public class ConvertEndAction extends AbsUfocPluginAction {

	@Override
	public void execute(ActionEvent paramActionEvent) {
		
		
		
		AdjustCombRepDataEditor curView = (AdjustCombRepDataEditor) getCurrentView();
		if (curView != null) {
			UITabbedPane uiTabbedPane = curView.getUITabbedPane();
			nc.ui.iufo.input.ufoe.comp.ExKTabbedPane workdraftSHowTab = (nc.ui.iufo.input.ufoe.comp.ExKTabbedPane) uiTabbedPane.getSelectedComponent();
			AdjustRepDataEditor selectedComponent = (AdjustRepDataEditor) workdraftSHowTab.getSelectedComponent();
			IWorkDraft workdraft = selectedComponent.getWorkdraft();
			if(workdraft.isdraft()) return;
			ReportTable table = selectedComponent.getTable();
			CellsPane cells = table.getCells();
			SelectModel selectionModel = cells.getSelectionModel();
			CellsModel cellsModel = selectionModel.getCellsModel();
			AreaPosition[] selectedAreas = selectionModel.getSelectedAreas();
			ArrayList<MeasureVO>  selectedMeasureCol = new ArrayList<MeasureVO>();
			for (int i = 0; i < selectedAreas.length; i++) {
				Set<String> measurePks = new HashSet<String>();
				AreaPosition areaPosition = selectedAreas[i];
				Cell[][] cells2 = cellsModel.getCells(areaPosition);
				for(int j = 0; j < cells2.length ; j++){
					Cell[] cellj = cells2[j];
					for(int m = 0 ; m < cellj.length; m++){
						Cell cell = cells2[j][m];
						//�õ���ǰѡ��Ԫ���ָ���ʽ��Ϣ,֧�ֶ�̬��
						MeasureFmt fmt = (MeasureFmt) cell.getExtFmt(MeasureFmt.EXT_FMT_MEASUREINPUT);
						if(fmt != null)
							measurePks.add(fmt.getCode());
//						Map<CellPosition, MeasureVO> mainMeasureVOByArea = CellsModelOperator.getMeasureModel(cellsModel).getMainMeasureVOByArea(areaPosition);
					}
				}
				MeasureVO[] vos = IUFOCacheManager.getSingleton().getMeasureCache().getMeasures(measurePks.toArray(new String[measurePks.size()]));
				selectedMeasureCol.addAll(Arrays.asList(vos));
				DataCenterType datatype= (DataCenterType) getContext().getAttribute(DataCenterType.HBBB_DATACENTER);
				JTabbedPane jTabbedPane = curView.getTabbePaneMap().get(workdraft.getPk_report());
				ReportType convertDraft = ReportType.CONVERT_DRAFT;
				if(datatype.equals(DataCenterType.CONVERT)){
					convertDraft = ReportType.CONVERT_DRAFT;
				}else if(datatype.equals(DataCenterType.HB)){
					convertDraft = ReportType.HB_DRAFT;
					//��̬�������׸�-modified by jiaah at 20130619
					if(vos != null && vos.length > 0 && vos[0] != null &&  (!vos[0].getKeyCombPK().equals(workdraft.getHbSchemevo().getPk_keygroup())))
						convertDraft = ReportType.HB_DYN1_DRAFT;
				}else if(datatype.equals(DataCenterType.HB_ADJUST)){
					convertDraft = ReportType.HB_ADJ_DRAFT;
				}else if(datatype.equals(DataCenterType.SEP_ADJUST)){
					convertDraft = ReportType.SEP_ADJ_DRAFT;
				}
				WorkDraft workdraftNew= new WorkDraft(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0056")/*@res "�������׸�"*/, workdraft.getPk_report(),convertDraft,"",selectedMeasureCol.toArray(new MeasureVO[0] ),workdraft.getPk_hbscheme());
				AdjustRepDataEditor subEditor = new AdjustRepDataEditor(workdraftNew, curView);
//				jTabbedPane.add(workdraftNew.getName(), subEditor);
//				Component com = new nc.ui.pub.beans.UIPanel()
				
				
				
				
				
//				String strRepPK, IRepDataParam param,
//				IWorkDraft workdraft
//				ConvertTablePanel convertPanel = new ConvertTablePanel(workdraft.getPk_report()��);
				
				String value = (String)ActionHandler.exec(HBBBDraftTableInputActionHandler.class.getName(),
						"getReport", selectedComponent.getRemotParam(), true);
				jTabbedPane.add("һ���׸�",new UIPanel());
//				subEditor.initRepDataEditor();

				//���ò鿴�׸�Ϊ�������׸�
				workdraftSHowTab.setSelectedIndex(workdraftSHowTab.getTabCount()-1);
			}
		}
	}

	@Override
	public IPluginActionDescriptor getPluginActionDescriptor() {
		PluginActionDescriptor desc = new PluginActionDescriptor("һ���׸�");
		String[] groupPath =new String[]{nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0022")/*@res "����"*/,"ConvertRacgeDraftAction"};//"����"��
		desc.setGroupPaths(groupPath);
		desc.setExtensionPoints(XPOINT.MENU,XPOINT.POPUPMENU);
//		desc.setIcon("/images/reportcore/refresh.gif");
		desc.setCode("ConvertEndAction");
		return desc;
	}

}
