package nc.ui.tb.zior;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uif.pub.IUifService;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.tb.formula.core.CubeHelper;
import nc.ui.tb.model.TBDataCellRefModel;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.para.SysInitVO;
import nc.vo.tb.form.excel.ExVarDef;
import nc.vo.tb.formula.TbbExpensesBVO;
import nc.vo.tb.formula.TbbExpensesVO;
import nc.vo.tb.obj.LevelValueOfDimLevelVO;
import nc.vo.tb.task.MdTaskDef;

public class TbMemberFilterUtil {
	
	private static Set<String> useTasks = new HashSet<>();
	
	private static String cnf = null;
	private static String cnfType = null;
	
	private static String measure = null;
	
	static{
		IUifService service = NCLocator.getInstance().lookup(IUifService.class);
	    try {
	    	//查询任务
			SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'TBB_TASK'");
			
			if(svos!=null&&svos.length>0){
				SysInitVO sVo  = svos[0];
				String taskNameDef = sVo.getValue();
				String[] taskNames = taskNameDef.split(",");
				StringBuffer sb = new StringBuffer();
				for(String taskName:taskNames){
					sb.append("'").append(taskName).append("'").append(",");
					
				}
				sb.delete(sb.length()-1, sb.length());
				MdTaskDef[] defs =  (MdTaskDef[])service.queryByCondition(MdTaskDef.class,"objname in ("+sb.toString()+")");
				if(defs!=null&&defs.length>0){
					for(MdTaskDef def:defs){
						useTasks.add(def.getPk_obj());
					}
					
					
				}else{
					NtbLogger.error("未查询到,对应的任务定义,"+sb.toString());
				}
			}
			
			
			
			svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'TBB_CNF'");
			
			if(svos!=null&&svos.length>0){
				SysInitVO sVo  = svos[0];
				cnf = sVo.getValue();
			 
			}
			
			svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'CNF_TYPE'");
			
			if(svos!=null&&svos.length>0){
				SysInitVO sVo  = svos[0];
				cnfType = sVo.getValue();
			 
			}
			
		svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'TB_MEASURE'");
			
			if(svos!=null&&svos.length>0){
				SysInitVO sVo  = svos[0];
				measure = sVo.getValue();
			 
			}
			
	  
		} catch (Exception e) {
			 NtbLogger.error(new BusinessRuntimeException("得到规则成员"+e.getMessage()));
		}
		
		
	}
	public static List<DimMember> getDimMembers(TBDataCellRefModel tBDataCellRefModel,LevelValueOfDimLevelVO levelValueOfDimLevelVO,
			String pk_user, String pk_group,String cubeCode, ExVarDef exVarDef,String parentKey,Map<DimLevel, LevelValue> currentMap){
		
		String pk_taskDef = levelValueOfDimLevelVO.task.getPk_taskdef();
		String pk_taskOrg = levelValueOfDimLevelVO.task.getPk_planent();
		return getDimMembers( tBDataCellRefModel, levelValueOfDimLevelVO,
				 pk_user,  pk_group, cubeCode,  exVarDef, parentKey, currentMap, pk_taskDef, pk_taskOrg);
	}
	public static List<DimMember> getDimMembers(TBDataCellRefModel tBDataCellRefModel,LevelValueOfDimLevelVO levelValueOfDimLevelVO,
			String pk_user, String pk_group,String cubeCode, ExVarDef exVarDef,String parentKey,Map<DimLevel, LevelValue> currentMap,String pk_taskDef,String pk_taskOrg){
		
		try{
			
		
		
		List<DimMember> dimMembers = DimServiceGetter.getVarMemberService().getVarMemberByTask(new LevelValueOfDimLevelVO[]{levelValueOfDimLevelVO}, cubeCode,pk_user,pk_group,exVarDef.mesType == null ? null:new String[]{exVarDef.mesType.name()},parentKey,currentMap);
		String dimLevelCode = levelValueOfDimLevelVO.dimlevelCode;
		int count = dimMembers!=null?dimMembers.size():0;
		Logger.error("1.find "+dimLevelCode+" members:"+count);
		
		Logger.error("Set cnf is:"+cnf+";Set cnfType is:"+cnfType+";Set measure is:"+measure);
		
		Logger.error("set useTasks size is:" +useTasks.size());
		
		Set<String> userCodes = new HashSet<String>();
		userCodes.add(cnf);
		userCodes.add(cnfType);
		userCodes.add(measure);
		if(useTasks.size()>0&&useTasks.contains(pk_taskDef)&&userCodes.contains(dimLevelCode)){
			
			CubeDef cd = CubeHelper.getCubeDefByCode(cubeCode);
		
			Logger.error("cube code is "+cubeCode);
			
			if(cd!=null){
				dimMembers =  cd.getDimHierarchy(DimServiceGetter.getDimManager().getDimLevelByBusiCode(dimLevelCode).getDimDef()).getMemberReader().getMembers();
				  count = dimMembers!=null?dimMembers.size():0;
				Logger.error("2.find "+dimLevelCode+" members:"+count);
			} 
			
			
			
			
			 
			DimLevel entityDimLevel =  DimServiceGetter.getDimManager().getDimLevelByBusiCode(IDimLevelCodeConst.ENTITY);
			
			LevelValue entityValue = entityDimLevel.getLevelValueByKey(pk_taskOrg);
			boolean isNeedFilter = false;
			if(entityValue!=null){
				String sql = "";
				if(entityValue.isDeptOrg()){
					sql = sql +"pk_dept = '"+pk_taskOrg+"'";
				}else{
					sql = sql +"pk_org = '"+pk_taskOrg+"'";
				}
				
				
				Logger.error("query org sql: select * from fy_expenses where "+sql);
				IUifService service = NCLocator.getInstance().lookup(IUifService.class);
				TbbExpensesVO[]  expens = (TbbExpensesVO[]) service.queryByCondition(TbbExpensesVO.class, sql);
				Set<String> keySet= new HashSet<>(); 
				 
				if(expens!=null&&expens.length>0){
					
					if(cnf!=null&&cnf.equals(dimLevelCode)){
						
					 
						
						isNeedFilter = true;
						DimLevel typeDimLevel =  DimServiceGetter.getDimManager().getDimLevelByBusiCode(cnfType);
						for(TbbExpensesVO expen:expens){
							if(currentMap!=null&&typeDimLevel!=null&&currentMap.get(typeDimLevel)!=null){
								LevelValue typeValue = currentMap.get(typeDimLevel);
								if(typeValue.getKey().equals(expen.getDef1())){
									keySet.add(expen.getPk_expenses_doc());
								}
							}else{
								Logger.error("not use dimLevel by code:"+cnfType+" filter cnf");
								keySet.add(expen.getPk_expenses_doc());
							}
							
						}
						 
						
					}else if(cnfType!=null&&cnfType.equals(dimLevelCode)){
						isNeedFilter = true;
					 
						for(TbbExpensesVO expen:expens){
							keySet.add(expen.getDef1());
						}
						
					}else if(measure!=null&&measure.equals(dimLevelCode)){
						
						
						DimLevel typeDimLevel =  DimServiceGetter.getDimManager().getDimLevelByBusiCode(cnfType);
						
						DimLevel ndfDimLevel =  DimServiceGetter.getDimManager().getDimLevelByBusiCode(cnf);
						
						if(typeDimLevel!=null&&ndfDimLevel!=null){
							StringBuffer sb = new StringBuffer();
							
							sb.append("pk_expenses in (select pk_expenses from fy_expenses where ").append(sql);
							
							
							if(  currentMap.get(typeDimLevel) !=null){
								sb.append(" and def1 = '"+currentMap.get(typeDimLevel).getKey()+"'");
							}

							if( currentMap.get(ndfDimLevel) !=null){
								sb.append(" and pk_expenses_doc = '"+currentMap.get(ndfDimLevel).getKey()+"'");
							}
							sb.append(")");
							
							Logger.error("query measure sql is: select * from fy_expenses_b where "+sb.toString());
							 
						
							TbbExpensesBVO[]  exBodys = (TbbExpensesBVO[]) service.queryByCondition(TbbExpensesBVO.class, sb.toString());
//							isNeedFilter = true;
							if(exBodys!=null&&exBodys.length>0){
								isNeedFilter = true;
								for(TbbExpensesBVO body  :exBodys){
									keySet.add(body.getSzxm());
								}
							}
							
						}else{
							Logger.error("not filter measure:"+cnfType +" or "+cnf +"not find dimlevel");
						}
						
		
					}
					if(isNeedFilter){
						List<DimMember> filterMembers = new  ArrayList<DimMember>();
						for(DimMember member:dimMembers){
							if(keySet.contains(member.getKey())){
								filterMembers.add(member); 
							}
							
						}
						
						dimMembers = filterMembers;
						count = dimMembers!=null?dimMembers.size():0;
						Logger.error("3.find "+dimLevelCode+" members:"+count);
					}
				
				}

				


			} 
			 

		}
			return dimMembers;
		}catch(Exception ex){
			NtbLogger.error(ex);
			return new ArrayList<DimMember>();
		}
	}

}
