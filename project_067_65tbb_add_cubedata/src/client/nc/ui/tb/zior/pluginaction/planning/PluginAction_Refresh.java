package nc.ui.tb.zior.pluginaction.planning;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.KeyStroke;

import nc.itf.mdm.cube.IDataSetService;
import nc.itf.mdm.dim.IDimManager;
import nc.itf.mdm.dim.INtbSuper;
import nc.ms.mdm.cube.CubeServiceGetter;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ms.tb.event.TaskChangeEvent;
import nc.ms.tb.ext.plan.TbCompliePlanConst;
import nc.ms.tb.ext.zior.xml.ZiorFrameCtl;
import nc.ms.tb.formula.core.CubeHelper;
import nc.ms.tb.task.TbTaskCtl;
import nc.ms.tb.task.TbTaskServiceGetter;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.tb.table.DimLevelValueCellEditor;
import nc.ui.tb.zior.TbPlanContext;
import nc.ui.tb.zior.pluginaction.AbstractTbPluginAction;
import nc.ui.tb.zior.pluginaction.TbPluginActionDescriptor;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.cube.DataCellValue;
import nc.vo.mdm.cube.DimVector;
import nc.vo.mdm.cube.ICubeDataSet;
import nc.vo.mdm.dim.DimDef;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.tb.task.MdTask;

import com.ufida.zior.plugin.PluginKeys.XPOINT;
import com.ufida.zior.plugin.event.PluginActionEvent;

public class PluginAction_Refresh extends AbstractTbPluginAction{
	private boolean isFinished = false;
	public PluginAction_Refresh(String name, String code) {
		super(name, code);

	}
	private TbPluginActionDescriptor desc;//改按钮的描述

	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		if(desc==null){
			 desc=new TbPluginActionDescriptor();
			 desc.setName(TbActionName.getName_Refresh());
			 desc.setGroupPaths(new String[]{TbActionName.getName_file()});
			 desc.setExtensionPoints(new XPOINT[]{ XPOINT.MENU,XPOINT.TOOLBAR});
			 desc.setIcon(ITbPlanActionCode.FRESH_ICON);
			 desc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
//			 desc.setShowDialog(true);
			// desc.setGroupPaths(groupPaths);
		}

		return desc;
	}

	public boolean isShowProgress(){
		return false;
	}

	@Override
	public void actionPerformed(ActionEvent actionevent) {
//		TbReportDirView tbReportDirView = (TbReportDirView)this.getMainboard().getView("tb.report.dir.view");
//		tbReportDirView.refresh();
//		isFinished = false;
//		Thread thread1 = new Thread() {
//			@Override
//			public void run() {
//				IProgressMonitor progressMonitor = UFOCProgresses.createDialogProgressMonitor(null,false);
//				progressMonitor.beginTask("刷新数据", 130);
//				progressMonitor.setLeftTime(IProgressMonitor.UNKNOWN_REMAIN_TIME);
//				progressMonitor.setProcessInfo(NCLangRes.getInstance().getStrByID("tb_cubedef", "01420cub_000073")/*任务开始*/);
//				sleepThread(100);
//				progressMonitor.worked(20);
//				int i = 1 ;
//				while(!isFinished){
//					if(i<=10)
//					{	i++;
//						progressMonitor.worked(10);
//					}
//					sleepThread(100);
//				}
//				if(isFinished){
//					progressMonitor.setProcessInfo("刷新完成");
//					progressMonitor.worked(130);
//					progressMonitor.done();
//				}
//			}
//		};
//		thread1.start();
//		Thread thread2 = new Thread() {
//			@Override
//			public void run() {
		//liyingm不一定每次都有tbsheetview
		
		if(1==1){
			addDataFromData();
		}
		
	
		
		
		if(getCurrentViewer()!=null){
			MdTask task=null;
			try {
				task = TbTaskCtl.getMdTaskByPk(this.getContext().getTasks()[0].getPk_obj(), true);
			} catch (BusinessException e) {
				NtbLogger.print(e.getMessage());
			}
			if(task==null){
				MessageDialog.showErrorDlg(
						this.getMainboard(),
						NCLangRes.getInstance().getStrByID("tbb_plan",
								"01812pln_000480")/* 提示 */, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0237")/*@res "任务已经被删除,点击左侧树面板，刷新任务树！"*/);
				return;
			}
			TaskDataCtl.clearLocalVersion(this.getContext().getTasks()[0].getPk_obj(),null);
			getCurrentViewer().getViewManager().refresh(getCurrentViewer());
			TaskChangeEvent taskChangeEvent = new TaskChangeEvent(getCurrentView(),
					1);
			this.getMainboard().getEventManager().dispatch(taskChangeEvent);
			PluginActionEvent pluginActionEvent = new PluginActionEvent(
					getTbReportDirView(), 1);
			getCurrentView().getMainboard().getEventManager()
					.dispatch(pluginActionEvent);
		}

//				isFinished = true;
//			}
//		};
//		thread2.start();
////		((KStatusBar)getMainboard().getStatusBar()).processDisplay("保存", runnable);
//	}
//	private void sleepThread(int period){
//		try {
//			Thread.sleep(period);
//		} catch (InterruptedException e) {
//			NtbLogger.error(e);
//		}
	}

	private void addDataFromData() {
		
		
		
		try {
			
			
			MdTask task=null;
			try {
				task = TbTaskCtl.getMdTaskByPk(this.getContext().getTasks()[0].getPk_obj(), true);
			} catch (BusinessException e) {
				NtbLogger.print(e.getMessage());
			}
			if(task==null){
				MessageDialog.showErrorDlg(
						this.getMainboard(),
						NCLangRes.getInstance().getStrByID("tbb_plan",
								"01812pln_000480")/* 提示 */, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0237")/*@res "任务已经被删除,点击左侧树面板，刷新任务树！"*/);
				return;
			}
			String cubeCode = "HTYK";
//			String cubeCode = "FYYS003";
			INtbSuper ntbSuper = NtbSuperServiceGetter.getINtbSuper();
			String sql = "select code_version,code_curr,code_aimcurr,code_mvtype,code_entity,code_year,code_measure,code_ysl01,value from TB_CUBE_HTYK_OTHERSYS";
			//sql="select  code_version,code_curr,code_aimcurr,code_mvtype,code_entity,code_year,code_measure,code_obm001,value from tb_cube_FYYS003    where uniqkey ='7.1.12.1.12.1.11c.d.~.aq.1.~.1.6.'";
			List queryData =  ntbSuper.query4List(sql);
			// 保存多维数据
			TbTaskServiceGetter.getTaskBusinessService().saveTaskData(
					task.getPrimaryKey(), getChangedDataCells(cubeCode,queryData), null);
			
			
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private Map<String, List<DataCell>> getChangedDataCells(String cubeCode,List queryData) throws BusinessException {
		
		List<DataCell> cells = new ArrayList<>();
		Map<String,List<DataCell>> rtn = new HashMap<String, List<DataCell>>();
		CubeDef cd = CubeHelper.getCubeDefByCode(cubeCode);
		List<DimVector>  dvs = new ArrayList<>();
		
		List<DimDef> addDefs  = new ArrayList<>();
		for(Object obj:queryData ){
			Object[] objs = (Object[])obj;
			List<DimMember> members = new ArrayList<>();
			
			members.add(getMemberByCode("VERSION",String.valueOf(objs[0]),cubeCode,addDefs));
			
			
			members.add(getMemberByCode("CURR",String.valueOf(objs[1]),cubeCode,addDefs));
			members.add(getMemberByCode("AIMCURR",String.valueOf(objs[2]),cubeCode,addDefs));
			members.add(getMemberByCode("MVTYPE",String.valueOf(objs[3]),cubeCode,addDefs));
			members.add(getMemberByCode("ENTITY",String.valueOf(objs[4]),cubeCode,addDefs));
			members.add(getMemberByCode("YEAR",String.valueOf(objs[5]),cubeCode,addDefs));
			members.add(getMemberByCode("MEASURE",String.valueOf(objs[6]),cubeCode,addDefs));
			members.add(getMemberByCode("YSL01",String.valueOf(objs[7]),cubeCode,addDefs));
			
			
		 for(DimDef df:cd.getDimDefs()){
			 if(!addDefs.contains(df)){
				 members.add(cd.getDimHierarchy(df).getAllMember());
			 }
		 }
 
			
		
//			idss.saveDataSetCells(paramCubeDef, paramList);
			DimVector dv = new DimVector(members);
			dvs.add(dv);
			DataCell dc = new DataCell(cd, dv);
			dc.setCellValue(new DataCellValue(100*Double.valueOf(String.valueOf(objs[8]))));
			
			cells.add(dc);
		}

		IDataSetService idss = CubeServiceGetter.getDataSetService();
		ICubeDataSet dataSet = 	idss.queryDataSet(cd, dvs);
		dataSet.getDataCell(dvs.get(0)).setCellValue(new DataCellValue(4000));
		idss.saveDataSetCells(dataSet);
		rtn.put(cubeCode, cells);
		return rtn;
	}
	
	private DimMember getMemberByCode(String levelCode,String levelValue,String cubeCode,List<DimDef> addDefs){
		
		CubeDef cd = CubeHelper.getCubeDefByCode(cubeCode);
		DimLevel dl = DimServiceGetter.getDimManager().getDimLevelByBusiCode(levelCode);
		LevelValue lv = dl.getLevelValueByCode(levelValue);
		if(lv==null){
			lv = dl.getLevelValueByUniqCode(levelValue);
		}
		addDefs.add(dl.getDimDef());
		if(lv==null) throw new BusinessRuntimeException("查找成员错误："+levelCode+"  user value is :"+levelValue);
		return cd.getDimHierarchy(dl).getMemberReader().getMemberByLevelValues(lv);
		
	}

	@Override
	public boolean isActionEnabled() {
		TbPlanContext tbPlanContext = getContext();
		if(tbPlanContext != null){
			if(TbCompliePlanConst.COM_MODE_TASKVIEW == tbPlanContext.getComplieStatus()&&tbPlanContext.getTaskNumber()==1){
				return true;
			}
		}
		return false;
	}


}