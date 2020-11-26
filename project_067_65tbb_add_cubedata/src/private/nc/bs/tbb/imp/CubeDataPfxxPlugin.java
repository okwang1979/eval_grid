package nc.bs.tbb.imp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microsoft.exchange.webservices.data.UserConfiguration;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.mw.sqltrans.TempTable;
import nc.bs.pfxx.ISwapContext;
import nc.bs.pfxx.plugin.AbstractPfxxPlugin;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.mdm.cube.IDataSetService;
import nc.itf.mdm.dim.IDimManager;
import nc.itf.tb.task.ITaskDataService;
import nc.itf.tb.task.ITaskObjectService;
import nc.itf.uif.pub.IUifService;
import nc.jdbc.framework.JdbcSession;
import nc.jdbc.framework.PersistenceManager;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.exception.DbException;
import nc.ms.mdm.cube.CubeServiceGetter;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.tb.control.CtlSchemeCTL;
import nc.ms.tb.formula.core.CubeHelper;
import nc.ms.tb.formula.excel.core.WorkBookExecute;
import nc.ms.tb.task.TaskActionCtl;
import nc.ms.tb.task.TbTaskServiceGetter;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ms.tb.task.data.TaskDataModelAction;
import nc.ms.tb.task.data.TaskSheetDataModel;
import nc.uif.pub.exception.UifException;
import nc.vo.bd.psn.PsndocVO;
import nc.vo.mdm.cube.CubeDataSet;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.cube.DataCellValue;
import nc.vo.mdm.cube.DimVector;
import nc.vo.mdm.cube.ICubeDataSet;
import nc.vo.mdm.cube.ISliceRule;
import nc.vo.mdm.dim.DimDef;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimLevelPKConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.pfxx.auxiliary.AggxsysregisterVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.para.SysInitVO;
import nc.vo.sm.UserVO;
import nc.vo.tb.data.ImpCubeDataVO;
import nc.vo.tb.data.ImpCubeInfoVO;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.rule.excel.CellElement;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;

public class CubeDataPfxxPlugin extends AbstractPfxxPlugin{
	
	private String taskName = "TASK_NAME";
	
	
	private String task_persion = "T_PERSION";
	
	private String task_other = "T_OTHER";
	
	private String cube_persion ="C_PERSION";
	
	private String cube_other = "C_OTHER";
	
	private String cube_code_persion = null;
	
	private String cube_code_other = null;
	
	private String task_name_persion = null;
	
	private String task_name_other = null;
	
	

	@Override
	protected Object processBill(Object arg0, ISwapContext arg1,
			AggxsysregisterVO arg2) throws BusinessException {
		
		
		try{
			Logger.init("iufo");
			Logger.error("################################开始接收外系统数据");
			
			if(arg0 instanceof ImpCubeInfoVO){
				ImpCubeInfoVO cubeData  =(ImpCubeInfoVO)arg0;
				ImpCubeDataVO[] datas = cubeData.getDatas();
				if(datas==null||datas.length==0){
					throw new BusinessException("未传入Datas。");
				}
				
			//查询初始化参数	
			cube_code_persion = getPanamValue(cube_persion);
			cube_code_other = getPanamValue(cube_other);
			task_name_persion = getPanamValue(task_persion);
			task_name_other = getPanamValue(task_other);
				
			
			CellGroup saveCells =	getChangedDataCells(cubeData,datas);
			
			
			
			 if(saveCells.getOtherCells().size()>0){
				 Logger.error("start other ctrlScheme:cells is "+saveCells.getOtherCells());
			 
					
					createCtrlScheme(saveCells.getOtherCells());
				
			 }
			 
			 if(saveCells.getPersionCells().size()>0){
				 
				 Logger.error("start Persion ctrlScheme:cells is "+saveCells.getPersionCells());
				 createPersionCtrlScheme(saveCells.getPersionCells());
			 }
			 
				
			}else{
				throw new BusinessException("传入参数错误：需要ImpCubeDataVO，传入。"+arg0.getClass().getName());
			}
			
		}catch(Exception ex){
			throw new BusinessException("数据交换平台错误:"+ex.getMessage(),ex);
		}finally{
			Logger.init();
		}
		
	
		 
		return "success";
	}
	
	
	
	private String getPanamValue(String initCode) throws UifException{
		IUifService service = NCLocator.getInstance().lookup(
				IUifService.class);
		SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(
				SysInitVO.class, "initcode = '"+initCode+"'");
		if (svos == null || svos.length == 0) {
			String message = "查询参数变量错误未查询到内容"+initCode;
			Logger.error(message);
			throw new BusinessRuntimeException(message);
		}
		SysInitVO vo =  svos[0];
		return vo.getValue();
	}
	

	private void createCtrlScheme(Collection<DataCell> saveCells) {
		
		ITaskObjectService taskService =   TbTaskServiceGetter.getTaskObjectService();
	
		
//		MdTask[] mdTasks = .getMdTasksByWhere(" pk_obj='" + pk_task+"'");
		
		
		try{
			Logger.error("开始修改控制方案，cells 数量："+saveCells.size());
			Logger.error("查询用户设定任务名称。");
			String taskDefName = task_name_other;
			Logger.error("--查询完成"+taskDefName);
			Logger.error("查询任务定义开始...");
			MdTaskDef[] defs  = taskService.getMdTaskDefByWhere(" objname = '"+taskDefName+"'");

			if(defs==null||defs.length==0){
				throw new BusinessRuntimeException("没有查询到对应的MdTaskDef："+taskDefName);
			}
			String pk_taskDef = defs[0].getPk_obj();
			//分组,按照分组条件查找任务
			Map<GroupDim, Collection<DataCell>> groupMap = new HashMap<GroupDim, Collection<DataCell>>();
			for(DataCell dc:saveCells){
				GroupDim dm = new GroupDim(dc.getDimVector());
				if(groupMap.get(dm)==null){
					Collection<DataCell> cells = new ArrayList<DataCell>();
					cells.add(dc);
					groupMap.put(dm, cells);
				}else{
					groupMap.get(dm).add(dc);
				}
			}
			Map<GroupDim,MdTask> groupTaskMap = new HashMap<>();
			for(GroupDim gd:groupMap.keySet()){
				groupTaskMap.put(gd, getTask(gd,pk_taskDef));
			}
			//根据任务加载数据
			ITaskDataService taskLoadService = NCLocator.getInstance().lookup(ITaskDataService.class);
			
			String pk_user = getSysUser();
			
			
			String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		     UserLoginVO userVo = new UserLoginVO();
		       userVo.setPk_group(pk_group);
			  userVo.setPk_user(pk_user);
			  userVo.setLogTime(new UFDateTime(System.currentTimeMillis()));
//			  DimServiceGetter.getDimManager().getDimLevelByPK(IDimLevelPKConst.VERSION).getDimDef().getDefaultHierarchy().getMemberReader().getMembers()
			LevelValue defValue = DimServiceGetter.getDimManager().getDimLevelByPK(IDimLevelPKConst.VERSION).getLevelValueByCode("v3");
		
			for(GroupDim dm:groupTaskMap.keySet()){
				MdTask task = groupTaskMap.get(dm);
//				if(task==null){
//					Logger.error("传入数据未找到对应任务,稍后请重新传输.");
//					continue;
//				}
				MdTask[] tasks = {task};
				
				//
				
				 
		  
				
			  
			   HashMap<String, Object> paraMap = new HashMap();
			   paraMap.put("adjVerPk", defValue.getKey().toString());
			  
				TaskActionCtl.processAction(userVo,tasks,"STARTADJ",paraMap);
//				TbTaskServiceGetter.getTaskBusinessService().saveTaskUnLockAllDataFlag(tasks, false);
				TaskDataModel model = taskLoadService.getTaskDataModel( task.getPk_obj(), null);
				model.reInitDataCellMap();
				boolean isChange = false;
				Collection<DataCell> taskChangeCells = groupMap.get(dm);
				Map<DimVector, CellElement> cellMap = new HashMap<>();
				for(TaskSheetDataModel sheet:model.getTaskSheetDataModels()){
					for(CellElement cell:sheet.getAllCellElement()){
						if(cell.getDimVector()!=null){
							cell.getValue();
							cellMap.put(cell.getDimVector(), cell);
						}
					}
				}
				
				
				for(DataCell cell:taskChangeCells){
					 CellElement bookTCell = cellMap.get(cell.getDimVector());
					 if(bookTCell!=null){
						 DataCell  bookCell =   bookTCell.getDatacell();
						 if(bookCell==null){
							 bookCell = new DataCell(cell.getCubeDef(), cell.getDimVector());
						 }
 
//						 if(valueChange(cell,bookCell)){
							 bookTCell.setValue(cell.getCellValue().getValue());
							 isChange = true;
//						 } 
					 }else{
						 Logger.error("DATAERR ->外系统传入单元格不能匹配到任务:"+task.getObjname()+";cell info:"+cell.toString());
					 }
					
					
					 
				}
				
				if(isChange){
					 CtlSchemeCTL.stopCtrlScheme(tasks);
//					MdTask[] tasks = {task};
					WorkBookExecute execute = new WorkBookExecute();
					execute.executeWorkBook(new TaskDataModelAction(model));
					
				      for (TaskSheetDataModel sheet : model.getTaskSheetDataModels()) {
					       sheet.setValueFromTCellToDataCell();
				      }
					
					model.saveData();
					
					//提交
					   HashMap<String, Object> remarkMap = new HashMap();
					   paraMap.put("appNoteRemark", "");
					
					 TaskActionCtl.processAction(userVo, tasks, "PROMOTE", remarkMap);
					 
					 //审批通过
					   HashMap<String, Object> appMarkMap = new HashMap();
					   paraMap.put("appNoteRemark", "");
					
					 TaskActionCtl.processAction(userVo, task, "APPRPASS", appMarkMap);
					 
					 CtlSchemeCTL.onUseAllCrelSchemeInClient(tasks);
//					CtlSchemeCTL.startControlSchemeWhenRevEffected(tasks);
//					
//					TbTaskServiceGetter.getTaskBusinessService().saveTaskUnLockAllDataFlag(tasks, true);
				}else{
					//提交
					   HashMap<String, Object> remarkMap = new HashMap();
					   paraMap.put("appNoteRemark", "");
					
					 TaskActionCtl.processAction(userVo, tasks, "PROMOTE", remarkMap);
					 
					 //审批通过
					   HashMap<String, Object> appMarkMap = new HashMap();
					   paraMap.put("appNoteRemark", "");
					   TaskActionCtl.processAction(userVo, task, "APPRPASS", appMarkMap);
				}
				
//				ITaskActionFunction func = TaskActionFactory.getTaskActionFunction(ITaskAction.APPROVE_PASS);
			
//				
				//启动直接调整
				//调整数据
				//提交数据
				//保存数据
				
			}
			
			
//			CtlSchemeCTL.startControlSchemeWhenRevEffected(tasks);
			
			
			
//			//查询
//			BaseDAO dao = new BaseDAO();
//			//
//			List<IdCtrlformulaVO> queryFormuls = new ArrayList<>();
//			
//			IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
//		String tableName = createTempTable("tb_temp_table001001");
//		Map<String,DataCell> dvStrMap = new HashMap<String, DataCell>();
//		List<String> dvStrs = new ArrayList<>();
//		CubeDef cd = null;
//		for(DataCell dc:saveCells){
//			String dvStr = cvt.convertToString(dc.getDimVector());
//			dvStrs.add(dvStr);
//			dvStrMap.put(dvStr, dc);
//			cd = dc.getCubeDef();
//			Logger.error("query dv str :"+dvStr);
//		}
//		
//		
//		insertTmp(dvStrs,tableName);
//		//查询出单元格启动了控制公式。
//		Collection<IdCtrlformulaVO> formulas = dao.retrieveByClause(IdCtrlformulaVO.class, " pk_dimvector in(select pkdoc from "+tableName+" )");
//		Logger.error("find ctrl formula :"+formulas.size());
//		if(formulas==null||formulas.isEmpty()){
//			return ;
//		}
//		
//		tableName = createTempTable("tb_temp_table00100101");
//		List<String> formulaPks = new ArrayList<>();
//		for(IdCtrlformulaVO vo:formulas){
//			formulaPks.add(vo.getPrimaryKey());
//		}
//		insertTmp(formulaPks,tableName);
//		//查询具体方案
//		Collection<IdCtrlschemeVO> schemeVos = dao.retrieveByClause(IdCtrlschemeVO.class, " pk_ctrlformula  in(select pkdoc from "+tableName+" )");
//		if(schemeVos==null||schemeVos.size()==0){
//			Logger.error("not Find scheme!");
//			return ;
//		}
//		
//		Map<String,List<IdCtrlschemeVO>> formulaAndSchemeMap = new  HashMap<String, List<IdCtrlschemeVO>>();
//		for(IdCtrlschemeVO scheme:schemeVos){
//			List<IdCtrlschemeVO> collSchemes = formulaAndSchemeMap.get(scheme.getPk_ctrlformula());
//			if(collSchemes==null){
//				collSchemes = new ArrayList<>();
//				collSchemes.add(scheme);
//				formulaAndSchemeMap.put(scheme.getPk_ctrlformula(), collSchemes);
//			}else{
//				collSchemes.add(scheme);
//			}
//			
//		}
//		Map<String, List<String>> map = new HashMap<String, List<String>>();
//		//停用控制公式
//		for(IdCtrlformulaVO formula:formulas){
//			
//			
//			List<String> schemePkList = new ArrayList<String>();
//			List<IdCtrlschemeVO> schemeList = formulaAndSchemeMap.get(formula.getPrimaryKey());
//			if(schemeList==null){
//				continue;
//			}
//			Logger.error("stop formula:"+formula.getPrimaryKey());
//			for(IdCtrlschemeVO scheme:schemeList){
//				schemePkList.add(scheme.getPrimaryKey());
//				Logger.error("        stop ctrl scheme:"+scheme.getPrimaryKey());
//			}
//			if(schemePkList.size()>0){
//				map.put(formula.getPrimaryKey(), schemePkList);
//			}
//			
//		
//			
//		}
//		Logger.error("停用控制方案：");
//		ICtlScheme ctlSchemeServer = NCLocator.getInstance().lookup(ICtlScheme.class);
//		ctlSchemeServer.deleteCtrlScheme(map);
//		//启用控制公式
//		ArrayList<CtrlSchemeVO> vos = new ArrayList<>();
//		Map<String,DimFormulaVO> queryFormula= new HashMap<>();
//
//		for(IdCtrlformulaVO formuaVo: formulas){
//			List<IdCtrlschemeVO> schemeList = formulaAndSchemeMap.get( formuaVo.getPrimaryKey());
//			if(schemeList==null){
//				continue;
//			}
//			Calculator  calculator = new Calculator();
//			IFormulaContext context = new DefaultFormulaContext(cd);
//			calculator.setContext(context);
//			DataCell dc = dvStrMap.get(formuaVo.getPk_dimvector());
//			List<FormulaDataCell> cells =new  ArrayList<FormulaDataCell>();
//			FormulaDataCell ownerCell = new FormulaDataCell(dc);
//			
//			DimFormulaVO formula =  queryFormula.get(formuaVo.getPk_parent());
//			if(formula==null){
//				formula= 	(DimFormulaVO)dao.retrieveByPK(DimFormulaVO.class,  formuaVo.getPk_parent());
//				queryFormula.put(formuaVo.getPk_parent(), formula);
//			}
//		
//			
//			calculator.getContext().setOwnerCell(ownerCell);
//			calculator.getContext().setCurrentWhereCells(cells);
//			String express = FormulaCTL.getFullExpress(formula.getExeFullcontent(), formula.getPrimaryKey());
//			if(!express.endsWith(";")){
//				express = express+";";
//			}
//			StringReader stringreader = new StringReader(express);
//		       TbbLexer frlexer = new TbbLexer(stringreader);
//		       TbbParser frparser = new TbbParser(frlexer);
//		      
//		      
//		       TbbFormulaExpression ruleExpression = frparser.tbbrule();
//		       String expression =  ruleExpression.getBodyExpressionList().get(0).toValue(calculator);
//		       expression = expression.replaceAll("UFIND\\('", "UFIND\\(\"");
//		       expression = expression.replaceAll("PREFIND\\('", "PREFIND\\(\"");
//		       expression = expression.replaceAll("'\\)", "\"\\)");
//			Logger.error("restart formula dc is:"+String.valueOf(dc));
//			Logger.error("                formula is:"+formula.getObjname()+",pk is"+formula.getPrimaryKey());
//			Logger.error("                expression is:"+expression);
//			CtrlSchemeVO vo = new CtrlSchemeVO( expression, formuaVo.getPk_parent(),dc ,formuaVo.getPk_plan()); 
//			vos.add(vo);
//		}
//		 
//		String[] info = CtlSchemeCTL.startCtrlScheme(vos);
//		
//		StringBuffer sb = new StringBuffer();
//		for(String pk:info){
//			sb.append(pk);
//		}
//		Logger.error("启动控制方案完成,create formula pks:"+sb.toString());
		}catch(Exception ex){
			Logger.error("重启控制方案错误："+ex.getMessage());
			throw new BusinessRuntimeException(ex.getMessage(),ex);
		}

		 
		
	}
	
	
	
	
	
	private void createPersionCtrlScheme(Collection<DataCell> saveCells) {
		
		ITaskObjectService taskService =   TbTaskServiceGetter.getTaskObjectService();
	
		
		
		
		try{
			Logger.error("开始修改控制方案，cells 数量："+saveCells.size());
			Logger.error("查询用户设定任务名称。");
			String taskDefName = task_name_persion;
			Logger.error("--查询完成"+taskDefName);
			Logger.error("查询任务定义开始...");
			MdTaskDef[] defs  = taskService.getMdTaskDefByWhere(" objname = '"+taskDefName+"'");

			if(defs==null||defs.length==0){
				throw new BusinessRuntimeException("没有查询到对应的MdTaskDef："+taskDefName);
			}
			String pk_taskDef = defs[0].getPk_obj();
			//分组,按照分组条件查找任务
			Map<GroupDim, Collection<DataCell>> groupMap = new HashMap<GroupDim, Collection<DataCell>>();
			for(DataCell dc:saveCells){
				GroupDim dm = new GroupDim(dc.getDimVector());
				if(groupMap.get(dm)==null){
					Collection<DataCell> cells = new ArrayList<DataCell>();
					cells.add(dc);
					groupMap.put(dm, cells);
				}else{
					groupMap.get(dm).add(dc);
				}
			}
			Map<GroupDim,MdTask> groupTaskMap = new HashMap<>();
			for(GroupDim gd:groupMap.keySet()){
				groupTaskMap.put(gd, getTask(gd,pk_taskDef));
			}
			
			
			
			if(saveCells.size()>0){
				IDataSetService idss = CubeServiceGetter.getDataSetService();
				ICubeDataSet dataSet = 	new CubeDataSet(CubeHelper.getCubeDefByCode(cube_code_persion),new ISliceRule() {
					
					@Override
					public boolean acceptDimVector(DimVector arg0) {
						return true;
					}
				});
				List<DataCell> dataSetCells = new ArrayList<>();
				dataSetCells.addAll(saveCells);
				((CubeDataSet)dataSet).setDataResult(dataSetCells);
				Logger.error("begin save cells:"+dataSetCells.size());
				idss.saveDataSetCells(dataSet);
			}
			
			
			//根据任务加载数据
			ITaskDataService taskLoadService = NCLocator.getInstance().lookup(ITaskDataService.class);
			
			String pk_user = getSysUser();
			
			
			String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		     UserLoginVO userVo = new UserLoginVO();
		       userVo.setPk_group(pk_group);
			  userVo.setPk_user(pk_user);
			  userVo.setLogTime(new UFDateTime(System.currentTimeMillis()));
			LevelValue defValue = DimServiceGetter.getDimManager().getDimLevelByPK(IDimLevelPKConst.VERSION).getLevelValueByCode("v3");
		
			for(GroupDim dm:groupTaskMap.keySet()){
				MdTask task = groupTaskMap.get(dm);
//				if(task==null){
//					Logger.error("传入数据未找到对应任务,稍后请重新传输.");
//					continue;
//				}
				MdTask[] tasks = {task};
				
				
				 
		  
				
			  
			   HashMap<String, Object> paraMap = new HashMap();
			   paraMap.put("adjVerPk", defValue.getKey().toString());
			  
				TaskActionCtl.processAction(userVo,tasks,"STARTADJ",paraMap);
				TaskDataModel model = taskLoadService.getTaskDataModel( task.getPk_obj(), null);
				model.reInitDataCellMap();
				
				for(TaskSheetDataModel sheet:model.getTaskSheetDataModels()){
					for(CellElement cell:sheet.getAllCellElement()){
						if(cell.getDimVector()!=null){
							cell.getValue();
//							cellMap.put(cell.getDimVector(), cell);
						}
					}
				}
				boolean isChange = false;

				
				
					 CtlSchemeCTL.stopCtrlScheme(tasks);
					WorkBookExecute execute = new WorkBookExecute();
					execute.executeWorkBook(new TaskDataModelAction(model));
					
				      for (TaskSheetDataModel sheet : model.getTaskSheetDataModels()) {
					       sheet.setValueFromTCellToDataCell();
				      }
					
					model.saveData();
					
					//提交
					   HashMap<String, Object> remarkMap = new HashMap();
					   paraMap.put("appNoteRemark", "");
					
					 TaskActionCtl.processAction(userVo, tasks, "PROMOTE", remarkMap);
					 
					 //审批通过
					   HashMap<String, Object> appMarkMap = new HashMap();
					   paraMap.put("appNoteRemark", "");
					
					 TaskActionCtl.processAction(userVo, task, "APPRPASS", appMarkMap);
					 
					 CtlSchemeCTL.onUseAllCrelSchemeInClient(tasks);
				
			}
			
			
		}catch(Exception ex){
			Logger.error("重启控制方案错误："+ex.getMessage());
			throw new BusinessRuntimeException(ex.getMessage(),ex);
		}

		 
		
	}
	
	private String getSysUser() throws Exception{
		try{
			IUifService service = NCLocator.getInstance().lookup(
					IUifService.class);
			SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(
					SysInitVO.class, "initcode = 'IMP_USER'");
			if (svos == null || svos.length == 0) {
				String message = "查询参数变量错误未查询到导入用户。";
				Logger.error(message);
				throw new BusinessRuntimeException(message);
			}
			SysInitVO vo =  svos[0];
			String userCode= vo.getValue();
			UserVO[] users =  (UserVO[] )service.queryByCondition(UserVO.class, "user_code = '"+userCode+"'");
			if(users == null || users.length==0){
				String message = "查询用户错误根据用户编码未找到对应用户:"+userCode;
				Logger.error(message);
				throw new BusinessRuntimeException(message);
			}
			return users[0].getCuserid();
		}catch(Exception ex){
			return null;
		}
		
		
		
	}



	private boolean valueChange(DataCell cell, DataCell bookCell) {
		
		if(cell.getCellValue()!=null&&bookCell.getCellValue()!=null){
			UFDouble outCellValue = null;
			if(cell.getCellValue().getValue()!=null){
				outCellValue =new UFDouble(cell.getCellValue().getValue().doubleValue());
			}else{
				outCellValue = new UFDouble();
			}
			
			UFDouble bookCellValue = null;
			if(bookCell.getCellValue().getValue()!=null){
				bookCellValue =new UFDouble(bookCell.getCellValue().getValue().doubleValue());
			}else{
				bookCellValue = new UFDouble();
			}
			return !outCellValue.equals(bookCellValue);
		}
		if(cell.getCellValue()!=null){
			UFDouble outCellValue = null;
			if(cell.getCellValue().getValue()!=null){
				outCellValue =new UFDouble(cell.getCellValue().getValue().doubleValue());
			}else{
				outCellValue = new UFDouble();
			}
			return !outCellValue.equals(new UFDouble());
		} 
		return false;
	}



	private  void insertTmp(List<String> pkdocs,String tableName) throws DAOException {
		PersistenceManager manager = null;
		try {
			manager = PersistenceManager.getInstance();
			JdbcSession session = manager.getJdbcSession();
			session.setAddTimeStamp(false);
			String sql = "insert into " + tableName + "(pkdoc) values(?)";
			for (String uk : pkdocs) {
				SQLParameter sp = new SQLParameter();
				sp.addParam(uk);
				session.addBatch(sql, sp);
			}
			session.executeBatch();
		} catch (DbException dbe) {
			throw new DAOException(dbe);
		} finally {
			if (manager != null)
				manager.release();
		}
	}

	private String createTempTable(String tableName) throws DAOException {
		
		JdbcSession session = null;
		try {
			session = new JdbcSession();
			String para2 =  tableName;
			String para3 = "pkdoc varchar2(500) not null";
			String para4 = "pkdoc";
			return new TempTable().createTempTable(session.getConnection(), para2, para3, para4);
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
			throw new DAOException(e.getMessage());
		} catch (SQLException e) {
			Logger.error(e.getMessage(), e);
			throw new DAOException(e.getMessage());
		} finally {
			if (session != null) {
				session.closeAll();
			}
		}
		 
	}




	private CellGroup getChangedDataCells(ImpCubeInfoVO cubeData,ImpCubeDataVO[] datas ) throws BusinessException {
		
		
		CellGroup rtn   = new CellGroup();
		
//		List<DataCell>  persionCell = new ArrayList<>();
		
//		String cubeCode = 
//				cubeData.getCube_code();
		
//		List<DataCell> cells = new ArrayList<>();
//		CubeDef cd = CubeHelper.getCubeDefByCode(cubeCode);
		List<DimVector>  dvs = new ArrayList<>();
		
		Logger.error("录入人："+cubeData.getUser_code());
		Logger.error("remark:"+cubeData.getRemark());
		
 
		int row=0;
		
		for(ImpCubeDataVO data:datas){
			String cubeCode = null;
			CubeDef cd = null;
			boolean isPersionData = false;
			if(data.getCode_employee()!=null&&data.getCode_employee().trim().length()>2){
				isPersionData = true;
				cubeCode = cube_code_persion;
				cd = CubeHelper.getCubeDefByCode(cubeCode);
			}else{
				cubeCode = cube_code_other;
				cd =CubeHelper.getCubeDefByCode(cubeCode);
			}
			row ++;
			List<DimDef> addDefs  = new ArrayList<>();
			Logger.error("开始第（"+row+"）条Item,外系统标识："+data.getOut_flag());
			List<DimMember> members = new ArrayList<>();
			//默认修改
			members.add(getMemberByCode("VERSION",String.valueOf(data.getCode_version()),cubeCode,addDefs,"v0"));
			
			
			members.add(getMemberByCode("CURR",String.valueOf(data.getCode_curr()),cubeCode,addDefs,"CNY"));
			members.add(getMemberByCode("AIMCURR",String.valueOf(data.getCode_aimcurr()),cubeCode,addDefs,"CNY"));
			members.add(getMemberByCode("MVTYPE",String.valueOf(data.getCode_mvtype()),cubeCode,addDefs,"Budget"));
			
			//预算列YSL01
			if(data.getCode_ysl01()!=null){
				DimMember yslMember = getMemberByCode("YSL02",String.valueOf(data.getCode_ysl01()),cubeCode,addDefs,null);
				if(yslMember!=null){
					members.add(yslMember);
				}
			
			}
			
			
			
			DimMember entryMember = getEntryMember(cubeCode,data.getCode_entity(),data.getCode_dept());
			if(entryMember!=null){
				members.add(entryMember);
				addDefs.add(entryMember.getDimDef());
			}else{
				Logger.error("第("+row+")条记录主体查询错误主体："+data.getCode_entity()+";部门："+data.getCode_dept());
				continue;
			}
//			members.add(getMemberByCode("ENTITY",String.valueOf(data.getCode_entity()),cubeCode,addDefs));
		 
			members.add(getMemberByCode("MEASURE",String.valueOf(data.getCode_measure()),cubeCode,addDefs,null));
			
			DimMember employeeMember = createEmployee(String.valueOf(data.getCode_employee()),cubeCode);
			if(employeeMember!=null){
				members.add(employeeMember);
				addDefs.add(employeeMember.getDimDef());
			}
			
			
			DimMember timeMember = getTimeMemberByCode(data.getCode_year(),data.getCode_month(),cubeCode,addDefs);
			if(timeMember!=null){
				members.add(timeMember);
				addDefs.add(timeMember.getDimDef());
			}else{
				Logger.error("查询时间错误，year is "+data.getCode_year()+";month is "+data.getCode_month());
				continue;
			}
			
			
 
			
			//添加其他自定义
			
			
			
		 for(DimDef df:cd.getDimDefs()){
			 if(!addDefs.contains(df)){
				 members.add(cd.getDimHierarchy(df).getAllMember());
			 }
		 }
 
			
		
 
			DimVector dv = new DimVector(members);
			dvs.add(dv);
			DataCell dc = new DataCell(cd, dv);
			if(dv.isTextCell()){
				dc.setCellValue(new DataCellValue(null,data.getTxtvalue()));
			}else{
				dc.setCellValue(new DataCellValue(data.getValue()));
			}
			
			Logger.error("Add cell :"+dc);
			if(isPersionData){
				rtn.addPersionCell(dc);
			}else{
				rtn.addOtherCell(dc);
			}
		}



		 
		return rtn;
	}
	
	
	
	
	
	
	private DimMember getEntryMember(String cubeCode, String code_entity,
			String code_dept) {	
		DimMember member = null;
			try{
				
				if(code_entity==null||"~".equals(code_entity.trim())){
					return null;
				}
				CubeDef cd = CubeHelper.getCubeDefByCode(cubeCode);
				DimLevel dl = DimServiceGetter.getDimManager().getDimLevelByBusiCode("ENTITY");
				if(dl==null){
					Logger.error("查询DimLevel错误：ENTITY");
					return null;
					
				}
				
//				IUifService service = NCLocator.getInstance().lookup(
//						IUifService.class);
//				PsndocVO[] psnVos = (PsndocVO[])service.queryByCondition(PsndocVO.class, "id='"+cardId.trim()+"'");
//				if(psnVos==null||psnVos.length==0){
//					Logger.error("查询人员信息错误user code ："+cardId);
//					return null;
//				}
//				String pk_employee = psnVos[0].getPk_psndoc();
				LevelValue lv = dl.getLevelValueByCode(code_entity);
				
				if(lv==null){
					Logger.error("查询主体错误 ："+code_entity);
					return null;
				}
			
 
				member = cd.getDimHierarchy(dl).getMemberReader().getMemberByLevelValues(lv);
				if(code_dept!=null){
					List<DimMember> members =  cd.getDimHierarchy(dl).getMemberReader().getDirectChildren(member);
					DimMember deptMember = null;
					for(DimMember m:members){
						if(code_dept.equals(m.getLevelValue().getCode())){
							deptMember = m;
							break;
						}
					}
					if(deptMember==null){
						Logger.error("查询部门主体错误 ："+code_dept);
						return null;
					}else{
						member = deptMember;
					}
				}
				
				
			}catch(Exception ex){
				Logger.error("查询维度主体信息错误：主体编码"+code_entity+";部门编码："+code_dept+ex.getMessage(),ex);
			}
		
			
			return member;
		}




	/**
	 * @param cardId：身份证号码
	 * @param cubeCode
	 * @return
	 */
	private DimMember createEmployee(String cardId, String cubeCode) {
		DimMember member = null;
		try{
			if(cardId==null||"~".equals(cardId.trim())){
				return null;
			}
			CubeDef cd = CubeHelper.getCubeDefByCode(cubeCode);
			DimLevel dl = DimServiceGetter.getDimManager().getDimLevelByBusiCode("EMPLOYEE");
			if(dl==null){
				Logger.error("查询DimLevel错误：EMPLOYEE");
			}
			
			IUifService service = NCLocator.getInstance().lookup(
					IUifService.class);
			PsndocVO[] psnVos = (PsndocVO[])service.queryByCondition(PsndocVO.class, "id='"+cardId.trim()+"'");
			if(psnVos==null||psnVos.length==0){
				Logger.error("查询人员信息错误user code ："+cardId);
				return null;
			}
			String pk_employee = psnVos[0].getPk_psndoc();
			LevelValue lv = dl.getLevelValueByKey(pk_employee);
			if(lv==null){
				Logger.error("查询人员信息错误user code ："+cardId);
			}
		
			if(lv==null){
				return null;
			}
			member = cd.getDimHierarchy(dl).getMemberReader().getMemberByLevelValues(lv);
	 
		}catch(Exception ex){
			Logger.error("查询维度人员信息错误：身份证号"+cardId+";"+ex.getMessage(),ex);
		}
	
		
		return member;
	}




//	private Map<String, List<DataCell>> getChangedDataCells(String cubeCode,List queryData) throws BusinessException {
//		
//		List<DataCell> cells = new ArrayList<>();
//		Map<String,List<DataCell>> rtn = new HashMap<String, List<DataCell>>();
//		CubeDef cd = CubeHelper.getCubeDefByCode(cubeCode);
//		List<DimVector>  dvs = new ArrayList<>();
//		
//		List<DimDef> addDefs  = new ArrayList<>();
//		for(Object obj:queryData ){
//			Object[] objs = (Object[])obj;
//			List<DimMember> members = new ArrayList<>();
//			
//			members.add(getMemberByCode("VERSION",String.valueOf(objs[0]),cubeCode,addDefs));
//			
//			
//			members.add(getMemberByCode("CURR",String.valueOf(objs[1]),cubeCode,addDefs));
//			members.add(getMemberByCode("AIMCURR",String.valueOf(objs[2]),cubeCode,addDefs));
//			members.add(getMemberByCode("MVTYPE",String.valueOf(objs[3]),cubeCode,addDefs));
//			members.add(getMemberByCode("ENTITY",String.valueOf(objs[4]),cubeCode,addDefs));
//			members.add(getMemberByCode("YEAR",String.valueOf(objs[5]),cubeCode,addDefs));
//			members.add(getMemberByCode("MEASURE",String.valueOf(objs[6]),cubeCode,addDefs));
//			members.add(getMemberByCode("YSL01",String.valueOf(objs[7]),cubeCode,addDefs));
//			
//			
//		 for(DimDef df:cd.getDimDefs()){
//			 if(!addDefs.contains(df)){
//				 members.add(cd.getDimHierarchy(df).getAllMember());
//			 }
//		 }
// 
//			
//		
////			idss.saveDataSetCells(paramCubeDef, paramList);
//			DimVector dv = new DimVector(members);
//			dvs.add(dv);
//			DataCell dc = new DataCell(cd, dv);
//			dc.setCellValue(new DataCellValue(100*Double.valueOf(String.valueOf(objs[8]))));
//			
//			cells.add(dc);
//		}
//
//		IDataSetService idss = CubeServiceGetter.getDataSetService();
//		ICubeDataSet dataSet = 	idss.queryDataSet(cd, dvs);
//		dataSet.getDataCell(dvs.get(0)).setCellValue(new DataCellValue(4000));
//		idss.saveDataSetCells(dataSet);
//		rtn.put(cubeCode, cells);
//		return rtn;
//	}
	
	
	private DimMember getTimeMemberByCode(String year,String month,String cubeCode,List<DimDef> addDefs){
		
		CubeDef cd = CubeHelper.getCubeDefByCode(cubeCode);
		DimLevel yearDl = DimServiceGetter.getDimManager().getDimLevelByBusiCode("YEAR");
		LevelValue yearLv = yearDl.getLevelValueByCode(year);
		if(yearLv==null){
			throw new BusinessRuntimeException("查找YEAR成员错误：  user value is :"+year);
		}
		LevelValue monthLv = null;
		try{
			if(month!=null&&Integer.valueOf(month)>0){
				DimLevel monthDl = DimServiceGetter.getDimManager().getDimLevelByBusiCode("MONTH");
				monthLv = monthDl.getLevelValueByCode(month);
			}
		}catch(Exception ex){
			
		
		}
		DimMember timeMember = null;
		if(month!=null){
			 timeMember = cd.getDimHierarchy(yearDl.getDimDef()).getDimMemberByLevelValues(yearLv,monthLv);
		}else{
			 timeMember = cd.getDimHierarchy(yearDl.getDimDef()).getDimMemberByLevelValues(yearLv);
		}
		if(timeMember==null){
			throw new BusinessRuntimeException("查找YEAR成员错误：  user value is :"+year);
		}
			
		
		return timeMember;
		
		
		 
		 
		
	}
	
	private DimMember getMemberByCode(String levelCode,String levelValue,String cubeCode,List<DimDef> addDefs,String defCode){
		DimMember member = null;
		try{
			CubeDef cd = CubeHelper.getCubeDefByCode(cubeCode);
			DimLevel dl = DimServiceGetter.getDimManager().getDimLevelByBusiCode(levelCode);

			LevelValue lv = dl.getLevelValueByCode(levelValue);
			if(lv==null){
				lv = dl.getLevelValueByUniqCode(levelValue);
			}
		
			if(lv==null){
				if(defCode==null){
					return null;	
				}else{
					lv = dl.getLevelValueByCode(defCode);
				}
				
			}
			if(lv==null){
				return null;
			}
			member = cd.getDimHierarchy(dl).getMemberReader().getMemberByLevelValues(lv);
			if(member!=null){
				addDefs.add(dl.getDimDef());
			}
		}catch(Exception ex){
			String errInfo = "查询维度："+levelCode+",发现成员("+levelValue+")错误："+ex.getMessage();
			Logger.error(errInfo,ex);
			throw new BusinessRuntimeException(errInfo,ex);
		}
	
		
		return member;
		
	}
	
	private MdTask getTask(GroupDim dim,String pk_taskdef) throws BusinessException{
		ITaskObjectService taskService =   TbTaskServiceGetter.getTaskObjectService();
		IDimManager dm = DimServiceGetter.getDimManager();
		DimLevel yearDl = dm.getDimLevelByPK(IDimLevelPKConst.YEAR);
		
		DimLevel entityDl = dm.getDimLevelByPK(IDimLevelPKConst.ENT);
		DimLevel currDl = dm.getDimLevelByPK(IDimLevelPKConst.CURR);
		DimLevel mvtypeDl = dm.getDimLevelByPK(IDimLevelPKConst.MVTYPE);
		DimLevel versionDl = dm.getDimLevelByPK(IDimLevelPKConst.VERSION);
		
		String curr = String.valueOf(currDl.getLevelValueByCode("CNY").getKey());
		String mvtype = String.valueOf(mvtypeDl.getLevelValueByCode("Budget").getKey());
		String version = String.valueOf(versionDl.getLevelValueByCode("v0").getKey());
		String year = String.valueOf(dim.getTime().getLevelValue(yearDl).getKey());
		String entity = String.valueOf(dim.getEntity().getLevelValue().getKey());
		String sql = "pk_taskdef ='"+pk_taskdef+"' and pk_year = '"+year+"' and pk_month='~' and pk_planent ='"+entity+"' and pk_currency='"+curr+"'  and pk_mvtype='"+mvtype+"'  and pk_version='"+version+"'  and planstatus ='320'  ";
		MdTask[] tasks =  taskService.getMdTasksByWhere(sql);
		
		if(tasks==null||tasks.length==0){
			throw new BusinessRuntimeException("查询任务错误，未查询到相关任务,time is:"+dim.getTime()+";entity is:"+dim.getEntity());
		}
		if(tasks.length>1){
			StringBuffer sb = new StringBuffer();
			for(MdTask task:tasks){
				sb.append(task.getPk_obj()+";");
			}
			Logger.error("查询任务大于两个，所有PK为："+sb.toString());
		}
		return tasks[0];
		
	}

}
