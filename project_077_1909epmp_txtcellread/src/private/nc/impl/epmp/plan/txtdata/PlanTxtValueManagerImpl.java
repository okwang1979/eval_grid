package nc.impl.epmp.plan.txtdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.logging.Logger;
import nc.bs.pub.SuperDMO;
import nc.itf.epmp.plan.txtdata.IPlanTxtValueManager;
import nc.itf.tb.task.ITaskBusinessService;
import nc.md.epmp.plan.txtdata.PlanTextDataVO;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.SuperVO;
import nc.vo.tb.form.MdSheet;
import nc.vo.tb.form.excel.ExObjVarCell;
import nc.vo.tb.form.excel.ExTaskSheetData;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;

public class PlanTxtValueManagerImpl implements IPlanTxtValueManager{

	@Override
	public void getDataFromTask(String name_taskdef) {
		try {
			Logger.init("iufo");
			
			SuperDMO dmo = new SuperDMO();
			
			MdTaskDef taskDef = getTasmDefByName(name_taskdef);
			if(taskDef!=null) {
				dmo.deleteByWhereClause(PlanTextDataVO.class, "pk_taskdef='"+taskDef.getPk_obj()+"'");
				Logger.error("删除任务浮动行成功："+taskDef.getObjname());
			}else {
				String message = "未找到对应任务："+name_taskdef+";查询条件：SELECT * FROM tb_md_taskdef WHERE objname='"+name_taskdef+"'";
				Logger.error(message);
				throw new BusinessRuntimeException(message);
			}
			String queryData = taskDef.getObjname()+taskDef.getPk_obj();
			Logger.error("query taskdef use  "+name_taskdef+" get data is "+ queryData);
			SuperVO[] allTasks = dmo.queryByWhereClause(MdTask.class, "pk_taskdef='"+taskDef.getPk_obj()+"'");
		
			
			if(allTasks!=null&&allTasks.length >0) {
				Logger.error("根据任务定义共找到任务： "+allTasks.length);
				ITaskBusinessService service = nc.ms.tb.task.TbTaskServiceGetter.getTaskBusinessService();
				//List< HashMap<MdSheet, List<ExTaskSheetData>>> data
				for(SuperVO vo:allTasks) {
					MdTask task = (MdTask) vo;
					 HashMap<MdSheet, List<ExTaskSheetData>> sheetDataMap =  service.getTaskSheetFmtData( task.getPk_obj(),null,taskDef.getPk_workbook());	
					 if(sheetDataMap==null||sheetDataMap.isEmpty()) {
						 continue;
					 }
					 saveSheet(sheetDataMap,task.getPk_obj(),taskDef.getPk_obj());
					 
				}
				
			}else {
				Logger.error("Not find task data,use taskdef :"+name_taskdef);
			}
			
			
		}catch(Exception ex) {
			Logger.error(ex.getMessage(),ex);
			throw new BusinessRuntimeException(ex.getMessage(),ex);
			
		}finally {
			Logger.init();
		}
		
		
	}

	private void saveSheet(HashMap<MdSheet, List<ExTaskSheetData>> sheetDataMap,String pk_task,String pk_taskDef) throws Exception{
	
		List<PlanTextDataVO> saveVoList = new ArrayList<>();
		for(MdSheet sheet:sheetDataMap.keySet()) {
			List<ExTaskSheetData> sheetDatas = sheetDataMap.get(sheet);
			if(sheetDatas!=null&&sheetDatas.size()>0) {
				Map<String,PlanTextDataVO> textMap = new HashMap<>();
				for(ExTaskSheetData sheetDtat:sheetDatas) {
				
					 HashMap<String,List<ExObjVarCell>>  sheetCellsMap = sheetDtat.varMap;
					 if(sheetCellsMap==null) continue;
					 for(String varId:sheetCellsMap.keySet()) {
						 List<ExObjVarCell> sheetCells = sheetCellsMap.get(varId);
						 if(sheetCells!=null||sheetCells.size()>0) {
							 Logger.error("查找任务"+pk_task+"，共查询到："+sheetCells.size()+"条浮动数据。");
							 for(ExObjVarCell cell:sheetCells) {
								 Logger.error("----"+cell);
								 PlanTextDataVO dataVo = null;
								 String key = varId+"_"+cell.row;
								 if(textMap.get(key)==null) {
									 dataVo = new PlanTextDataVO();
									 dataVo.setPk_taskdef(pk_taskDef);
									 dataVo.setPk_task(pk_task);
									 dataVo.setPk_sheet(sheet.getPk_obj());
									 dataVo.setRow_num(cell.row);
									 dataVo.setVarid(varId);
									 saveVoList.add(dataVo);
									 textMap.put(key,dataVo);
								 }else {
									 dataVo = textMap.get(key);
								 }
								 setDataColValue(cell.col,dataVo,cell.value);

								 //case cell.col
								 
								 
							 }
							 
						 }
					 }
				}
			}
		}
		if(saveVoList.size()>0) {
			BaseDAO dao = new BaseDAO();
			String[] pks = dao.insertVOList(saveVoList);
			
			StringBuffer sb = new StringBuffer();
			for(String pk:pks) {
				sb.append("'").append(pk).append("',");
			}
			Logger.error("共保存task("+pk_task+")数据："+saveVoList+"条，pk为："+sb.toString());
			
		}
		
		
	}

	private MdTaskDef getTasmDefByName(String name_taskdef) throws Exception {
		

		SuperDMO dmo = new SuperDMO();
		
		SuperVO[]  taskDefs =  dmo.queryByWhereClause(MdTaskDef.class, "objname='"+name_taskdef+"'");
		
		if(taskDefs!=null&&taskDefs.length>0) {
			return (MdTaskDef)taskDefs[0];
		}
		return null;
	}
	
	private  void setDataColValue(int col,PlanTextDataVO dataVo,String value) {
		
		switch(col) {
		  case 1:
			  dataVo.setCol01(value);
			  break;
		  case 2:
			  dataVo.setCol02(value);
			  break;
		  case 3:
			  dataVo.setCol03(value);
			  break;
		  case 4:
			  dataVo.setCol04(value);
			  break;
		  case 5:
			  dataVo.setCol05(value);
			  break;
		  case 6:
			  dataVo.setCol06(value);
			  break;
		  case 7:
			  dataVo.setCol07(value);
			  break;
		  case 8:
			  dataVo.setCol08(value);
			  break;
		  case 9:
			  dataVo.setCol09(value);
			  break;
		  case 10:
			  dataVo.setCol10(value);
			  break;
		  case 11:
			  dataVo.setCol11(value);
			  break;
		  case 12:
			  dataVo.setCol12(value);
			  break;
		  case 13:
			  dataVo.setCol13(value);
			  break;
		  case 14:
			  dataVo.setCol14(value);
			  break;
		  case 15:
			  dataVo.setCol15(value);
			  break;
		  case 16:
			  dataVo.setCol16(value);
			  break;
		  case 17:
			  dataVo.setCol17(value);
			  break;
		  case 18:
			  dataVo.setCol18(value);
			  break;
		  case 19:
			  dataVo.setCol19(value);
			  break;
		  case 20:
			  dataVo.setCol20(value);
			  break;
		  case 21:
			  dataVo.setCol21(value);
			  break;
		  case 22:
			  dataVo.setCol22(value);
			  break;
		  case 23:
			  dataVo.setCol23(value);
			  break;
		  case 24:
			  dataVo.setCol24(value);
			  break;
		  case 25:
			  dataVo.setCol25(value);
			  break;
		  case 26:
			  dataVo.setCol26(value);
			  break;
		  case 27:
			  dataVo.setCol27(value);
			  break;
		  case 28:
			  dataVo.setCol28(value);
			  break;
		  case 29:
			  dataVo.setCol29(value);
			  break;
		  case 30:
			  dataVo.setCol30(value);
			  break;
			  
		 }
		
	}
 

}
