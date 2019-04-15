package nc.ms.tb.rule.fmlset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections.CollectionUtils;

import nc.itf.mdm.cube.IDataSetService;
import nc.itf.mdm.dim.IDimManager;
import nc.itf.tb.rule.fmlset.IDataCellTimeGetter;
import nc.itf.tb.rule.fmlset.IFilterAccumulate;
import nc.ms.mdm.cube.CubeServiceGetter;
import nc.ms.mdm.dim.DimMemberReader;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.tb.formula.context.IFormulaContext;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.cube.DimSectionSetTuple;
import nc.vo.mdm.cube.DimVector;
import nc.vo.mdm.cube.ICubeDataSet;
import nc.vo.mdm.dim.AllTimePeriods;
import nc.vo.mdm.dim.DimDef;
import nc.vo.mdm.dim.DimHierarchy;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimDefPKConst;
import nc.vo.mdm.dim.IDimLevelPKConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFDouble;

public abstract class AbstractFilterAccumulate implements IFilterAccumulate {
	
//	//起始时间
//	protected String startTime;
//	
//	//结束时间
//	protected String endTime;
//	
//	//合计预算数
//	protected UFDouble total;
//	
//	private ICubeDataSet dbSet;
//	
//	/**
//	 * 当累计至无穷小时，无法用一个DimSectionSetTuple描述
//	 */
//	private List<ICubeDataSet> extraDbSet;
	
	/**
	 * 不敢说这样是否没有问题，按说从排序最小时间的单元格上取比较准确，但是可能有效率问题，后续看看能否改进效率
	 * @param dataCell
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public String[] getStartAndEndTime(DataCell dataCell) throws BusinessException {
//		String[] time = new String[2];
//		time[0] = getAccumulateValue(dataCell).startTime;
//		time[1] = getAccumulateValue(dataCell).endTime;
//		return time;
		
		IDataCellTimeGetter timeGetter = FilterAccumulateFactory.getCellTimeGetter(dataCell);
		
		String endTime = timeGetter.getCurrentEndTimeInDataCell(dataCell);
		
		IDimManager dm = DimServiceGetter.getDimManager();
		DimDef timeDimDef = dm.getDimDefByPK(IDimDefPKConst.TIME);
		DimHierarchy dimhier = dataCell.getCubeDef().getDimHierarchy(IDimDefPKConst.TIME);
		DimMember cellMember = dataCell.getDimVector().getDimMember(timeDimDef);
		DimLevel level = cellMember.getDimLevel();
		
		String startTime = null;
		DimLevel lev = getAccumulateLevels(dimhier);
		if(lev != null) {
			if(cellMember.getDimLevel().equals(lev))
				startTime = timeGetter.getCurrentStartTimeInDataCell(dataCell);
			else {
				while(cellMember != null && !cellMember.getDimLevel().equals(lev)) {
					cellMember = cellMember.getParentMemberUpLevel();
				}
				if(cellMember != null && cellMember.getDimLevel().equals(lev)){
					List<DimMember> members = dimhier.getMemberReader().getDirectChildrenDownLevel(cellMember);
					while(members.size() > 0 && !members.get(0).getDimLevel().equals(level)) {
						members = dimhier.getMemberReader().getDirectChildrenDownLevel(members.get(0));
					}
					
					if(members.size() > 0 && members.get(0).getDimLevel().equals(level))
						startTime = timeGetter.getStartTimeByDimMember(dataCell, members.get(0));
				}
			}
		}
		
		
		if(startTime == null) startTime = timeGetter.getCurrentStartTimeInDataCell(dataCell);
		String[] times = new String[2];
		times[0] = startTime;
		times[1] = endTime;
		return times;
	}
	
	
	@Override
	public double calculateAccumData(DataCell dataCell) throws BusinessException {
		
		List<List<DataCell>> cubeDataSet = getAllAccumData(dataCell);
		
		
		double totalValue = 0;
		//预算数之和，及计算起止时间
		if(cubeDataSet != null) {
			
			for(List<DataCell> cells : cubeDataSet) {
				
				for(DataCell cell : cells) {
					if(cell.getCellValue().getValue() != null)
						totalValue += cell.getCellValue().getValue().doubleValue();
				}
			}
		}
		if(dataCell.getCellValue().getValue() != null)
			totalValue += dataCell.getCellValue().getValue().doubleValue();
		return totalValue;
//		return getAccumulateValue(dataCell).total.doubleValue();
	}
	
	
	/**
	 * 
	 * 获得单元格在模型上需要累计到得其他单元格，63EHP3起获取累计起止时间也走查询单元格的流程，有可能有效率问题。
	 * 
	 * @param dataCell
	 * @return
	 * @throws BusinessException
	 * 
	 */
	protected List<List<DataCell>> getAllAccumData(DataCell dataCell) throws BusinessException {
//		IDimManager dimManager = DimServiceGetter.getDimManager();
//		DimDef timeDimDef = dimManager.getDimDefByPK(IDimDefPKConst.TIME);
//		DimMember timeMember = dataCell.getDimVector().getDimMember(timeDimDef);
		
		Map<DimLevel, LevelValue> cellLevelsMap = new HashMap<DimLevel, LevelValue>();
		
		LevelValue[] levelValues = dataCell.getDimVector().getAllLevelValues().toArray(new LevelValue[0]);
		for(LevelValue value : levelValues) {
			if(value.getDocName() != null) {
				cellLevelsMap.put(value.getDimLevel(), value);
			}
		}
			
		DimHierarchy dimhier = dataCell.getCubeDef().getDimHierarchy(IDimDefPKConst.TIME);
		
		//返回累计的范围
		DimLevel accumLevels = getAccumulateLevels(dimhier);
		List<DimLevel> cellLvs = dimhier.getDimLevels();
		
//		DimSectionSetTuple tuple = new DimSectionSetTuple();
		List<List<DataCell>> extraDbSet = new ArrayList<List<DataCell>>();
		Map<DimLevel, DimSectionSetTuple> sections = new HashMap<DimLevel, DimSectionSetTuple>();
		
		List<DimLevel> comparedLvs = new ArrayList<DimLevel>();
		
		//按项目累计情况比较复杂，不能简单置为起始时间1900年
		
		//第一点，无穷小的起始时间难以累计预算数
		//第二点，不能确定业务系统支持较小起始时间，譬如1900年没有会计期间，总账无法取数
		if(accumLevels == null) {
			
			for(DimLevel level : cellLvs) {
				comparedLvs.add(level);
			}
			sections = getPossibleSectionSet(accumLevels, comparedLvs, dataCell);
				
			
		} else if(accumLevels.getPk_obj().equals("ERROR" + IDimLevelPKConst.QUARTER)) {
		
			String[] months = getQuarterInCommon(dataCell);
			
			DimLevel monthLev = null;
			for(DimLevel level : cellLvs) {
				if(level.getPk_obj().equals(IDimLevelPKConst.MONTH))
					monthLev = level;
			}
			if(months != null && monthLev != null) {
				
				int index = cellLvs.indexOf(monthLev); 
				for(int i = ++index ; i < cellLvs.size() ; i++)
					comparedLvs.add(cellLvs.get(i));
				sections = getPossibleSectionSet(monthLev, comparedLvs, dataCell);
				
				DimSectionSetTuple tuple = getPossibleSectionSet(months, dataCell);
				comparedLvs.add(monthLev);
				if(tuple != null)
					sections.put(monthLev, tuple);
			}
		
		} else {
		
			int index = cellLvs.indexOf(accumLevels);
			if(index >= 0) {
				for(int i = ++index ; i < cellLvs.size() ; i++)
					comparedLvs.add(cellLvs.get(i));
//
//				IDimManager dm = DimServiceGetter.getDimManager();
//				DimDef timeDimDef = dm.getDimDefByPK(IDimDefPKConst.TIME);
//				DimMember timeMember = dataCell.getDimVector().getDimMember(timeDimDef);
//		
//				Map<DimLevel, List<LevelValue>> sliceCondition = new HashMap<DimLevel, List<LevelValue>>();
//				for(DimLevel level : comparedLvs) {
//				
//					for(DimLevel level0 : comparedLvs) {
//						LevelValue value = dataCell.getDimVector().getLevelValue(level);
//						List<LevelValue> values = dimhier.getMemberReader().getAllLevelValues(level);
//						int location = values.indexOf(value);
//						if(location < 0) throw new BusinessException("invalid levelvalue:" + value);
//						sliceCondition.put(level0, values.subList(0, --index));
//					}
//				}
//				List<LevelValue> allValues = timeMember.getDimHierarchy().getMemberReader().getAllLevelValues(accumLevels);
				
				sections = getPossibleSectionSet(accumLevels, comparedLvs, dataCell);

//				List<LevelValue> resultList = new ArrayList<LevelValue>();
//		
//		
//				LevelValue currentValue = cellTimeMap.get(accumLevels);
//		
//		
//				if(currentValue == null)
//					throw new BusinessException("单元格不能从" + accumLevels.getObjName() + "初累计");
//				for(LevelValue value : allValues){
//			
//				if(Double.parseDouble(String.valueOf(value.getKey()))<=Double.parseDouble(String.valueOf(currentValue.getKey())))
//					resultList.add(value);
			}
		}
		
		for(Map.Entry<DimLevel, DimSectionSetTuple> entry : sections.entrySet()) {

			DimLevel level = entry.getKey();
			DimSectionSetTuple section = entry.getValue();
			
			for(LevelValue value : levelValues) {
				if(!comparedLvs.contains(value.getDimLevel()))
					section.setLevelValues(value.getDimLevel(), Arrays.asList(value));
				else {
					
					int index1 = comparedLvs.indexOf(level);
					int index2 = comparedLvs.indexOf(value.getDimLevel());
					if(index1 > index2)
						section.setLevelValues(value.getDimLevel(), Arrays.asList(value));
				}
			}
			
		}
		
		IDataSetService idss = CubeServiceGetter.getDataSetService();

		for(Map.Entry<DimLevel, DimSectionSetTuple> entry : sections.entrySet()) {
			DimSectionSetTuple section = entry.getValue();
			ICubeDataSet set = idss.queryDataSet(dataCell.getCubeDef(), section);
			
			//注意：过滤出的单元格可能待用"~"成员，需要再次判断一下
			List<DataCell> extraCells = new ArrayList<DataCell>();
			List<DataCell> dataCellRes = set.getDataResult();
			for(DataCell cell : dataCellRes) {
				
				boolean isContainLvs = true;
				for(DimLevel level : comparedLvs) {
					LevelValue cellvalue = cell.getDimVector().getLevelValue(level);
					
					if(cellLevelsMap.containsKey(level) && cellvalue == null) {
						isContainLvs = false;
						break;
					}
				}
				
				LevelValue[] cellvalues = cell.getDimVector().getAllLevelValues().toArray(new LevelValue[0]);
				List<DimLevel> levels = new ArrayList<DimLevel>(); 
				for(LevelValue value : cellvalues) {
					if(value.getDocName() != null)
						levels.add(value.getDimLevel());
				}
				
				for(DimLevel level : levels) {
					if(!cellLevelsMap.containsKey(level))
						isContainLvs = false;
				}
				if(cellLevelsMap.keySet().size() != levels.size())
					isContainLvs = false;
				
				
				if(isContainLvs)
					extraCells.add(cell);
			}
			
			extraDbSet.add(extraCells);
		}
		
		return extraDbSet;
	}
	
	
	private Map<DimLevel, DimSectionSetTuple> getPossibleSectionSet(DimLevel accumLevel, List<DimLevel> levelList, DataCell dataCell) throws BusinessException {
		
		IDimManager dm = DimServiceGetter.getDimManager();
		DimDef timeDimDef = dm.getDimDefByPK(IDimDefPKConst.TIME);
		DimMember cellMember = dataCell.getDimVector().getDimMember(timeDimDef);
		DimMember member = cellMember;
		
		Map<DimLevel, DimSectionSetTuple> sections = new HashMap<DimLevel, DimSectionSetTuple>();
		Collection<LevelValue> valueArr = dataCell.getDimVector().getAllLevelValues();
		List<DimLevel> timeLevs = new ArrayList<DimLevel>();
		for(LevelValue value : valueArr) {
			if(value.getDimDef().getPk_obj().equals(IDimDefPKConst.TIME))
				timeLevs.add(value.getDimLevel());
		}
		
		List<DimMember> comparedMems = new ArrayList<DimMember>();
		while(member != null) {
			if(!member.getDimLevel().equals(accumLevel)) {
				
				if(timeLevs.contains(member.getDimLevel()))
					comparedMems.add(member);
			} else {
				if(timeLevs.contains(member.getDimLevel()))
					comparedMems.add(member);
				break;
			}
			member = member.getParentMemberUpLevel();
		}
		
		
		
		
		DimHierarchy dimhier = dataCell.getCubeDef().getDimHierarchy(IDimDefPKConst.TIME);
		for(int i = comparedMems.size() - 1 ; i >= 0 ; i--) {
			
			DimMember mem = comparedMems.get(i);
			DimSectionSetTuple section = new DimSectionSetTuple();
			
			List<LevelValue> beforValues = null;
			if(i == comparedMems.size() - 1) {
				
				if(accumLevel != null)
					continue;
				else {
	
					List<DimMember> members = dimhier.getMemberReader().getMembersInLevel(mem.getDimLevel());
					beforValues = getBeforeValues(dimhier, mem.getLevelValue(), members);
					
				}
//				List<LevelValue> values = dimhier.getMemberReader().getAllLevelValues(mem.getDimLevel());
//				LevelValue cellvalue = mem.getLevelValue();
//				
//				int index = values.indexOf(cellvalue);
//				if(index < 0) throw new BusinessException("invalid levelvalue:" + cellvalue);
//				List<LevelValue> beforevalues = values.subList(0, index);
				
			} else {
				DimMember upLevelMem= comparedMems.get(i + 1);
				List<DimMember> levelMembers = dimhier.getMemberReader().getDirectChildrenDownLevel(upLevelMem);
			
				beforValues = getBeforeValues(dimhier, mem.getLevelValue(), levelMembers);
			}
			
			if(beforValues.size() > 0) {
				section.setLevelValues(mem.getDimLevel(), beforValues);
				sections.put(mem.getDimLevel(), section);
			}
		}

		
		return sections;
	}
	
	protected DimSectionSetTuple getPossibleSectionSet(String[] months, DataCell dataCell) {
		
		DimMember member = getMonthMember(dataCell);
		LevelValue value = member.getLevelValue();
		DimHierarchy dimhier = dataCell.getCubeDef().getDimHierarchy(IDimDefPKConst.TIME);
		List<LevelValue> monthValues = dimhier.getMemberReader().getAllLevelValues(value.getDimLevel());
		
		DimSectionSetTuple tuple = new DimSectionSetTuple();
		int index = monthValues.indexOf(value);
		List<LevelValue> result = new ArrayList<LevelValue>();
		if(index >= 0) {
			int[] monthsNum = new int[3];
			for(int i = 0 ; i < months.length ; i++) {
				monthsNum[i] = Integer.parseInt(months[i]);
			}
			
			for(LevelValue monthValue : monthValues) {
				int month = 0;
				try {
					month = Integer.parseInt(monthValue.getCode());
				} catch (NumberFormatException e) {
					continue;
				}
				
				int index2 = monthValues.indexOf(monthValue);
				if((month == monthsNum[0] || month == monthsNum[1] || month == monthsNum[2]) && index2 < index) {
					result.add(monthValue);
				}
			}
			tuple.setLevelValues(value.getDimLevel(), result);	
			return tuple;
		}
		return null;
	}
	
	private List<LevelValue> getBeforeValues(DimHierarchy dimhier, LevelValue value, List<DimMember> levelMembers) {
		List<LevelValue> allLevels = dimhier.getMemberReader().getAllLevelValues(value.getDimLevel());
		
		List<LevelValue> beforValues = new ArrayList<LevelValue>();
		for(DimMember m : levelMembers) {
			int index1 = allLevels.indexOf(m.getLevelValue());
			int index2 = allLevels.indexOf(value);
			
			if(index1 < index2)
				beforValues.add(m.getLevelValue());
		}
		return beforValues;
	}
	
	protected DimMember getMonthMember(DataCell dataCell) {
		IDimManager dm = DimServiceGetter.getDimManager();
		DimDef timeDimDef = dm.getDimDefByPK(IDimDefPKConst.TIME);
		DimMember cellMember = dataCell.getDimVector().getDimMember(timeDimDef);
		
		while(cellMember != null && !cellMember.getDimLevel().getPk_obj().equals(IDimLevelPKConst.MONTH)) {
			cellMember = cellMember.getParentMemberUpLevel();
		}
		if(cellMember.getDimLevel().getPk_obj().equals(IDimLevelPKConst.MONTH))
			return cellMember;
		return null;
	}
	
	protected String[] getQuarterInCommon(DataCell dataCell) {

		DimMember cellMember = getMonthMember(dataCell);
		
		
		if(cellMember != null) {
			String month = cellMember.getObjCode();
			
			int m = 0;
			try {
				m = Integer.parseInt(month);
			} catch(NumberFormatException e) {
				return null;
			}
			
			switch(m) {
			case 1:
			case 2:
			case 3:
				return new String[] {"01", "02", "03"};
			case 4:
			case 5:
			case 6:
				return new String[] {"04", "05", "06"};
			case 7:
			case 8:
			case 9:
				return new String[] {"07", "08", "09"};
			case 10:
			case 11:
			case 12:
				return new String[] {"10", "11", "12"};
			default:
				return null;
			}
		}
		return null;
	}
	
	protected abstract DimLevel getAccumulateLevels(DimHierarchy dimhier);
	
	
	double addCell(Set<DataCell> cacheCells) {
		UFDouble rtnValue = new UFDouble();
		for(DataCell dc:cacheCells){
			if(dc.getCellValue()!=null&&dc.getCellValue().getValue()!=null){
				rtnValue = rtnValue.add(dc.getCellValue().getValue().doubleValue());
			}
		}
		return rtnValue.doubleValue();
	}
	
	@Override
	public double calculateAccumData(DataCell dataCell, IFormulaContext context) {
		
		
		
		IDimManager dm = DimServiceGetter.getDimManager();
		DimDef df =   dm.getDimDefByPK(IDimDefPKConst.TIME);
		DimMember timeMember = dataCell.getDimVector().getDimMember(df);
		
		String key = getClass().getName()+"_"+timeMember.getUniqKey();
		List<DimMember> addMembers = new ArrayList<DimMember>();
		if(context.getValue(key)==null){
			
			DimMember parentMember = getParentMember(timeMember);
		
			if(parentMember==null){
				throw new BusinessRuntimeException("汇总结构中没有对应的层，请修改控制规则");
			}
			if(parentMember.equals(timeMember)){
				Set<DataCell> addCells  = new HashSet<DataCell>();
				addCells.add(dataCell);
				return addCell(addCells) ;
			}
			DimHierarchy th = dataCell.getDimVector().getDimMember(df).getDimHierarchy();
			DimMemberReader reader = th.getMemberReader();
			List<DimMember> allChildMembers =  reader.getAllChildren(parentMember);
			if(allChildMembers==null){
				allChildMembers = new ArrayList<DimMember>();
			}
			
			List<DimMember> levelMembers = reader.getMembersInLevel(timeMember.getLevelValue().getDimLevel());
			
		
			Collection<DimMember> members =  CollectionUtils.intersection(allChildMembers,levelMembers);
			
			List<DimLevel> allDls = new ArrayList<DimLevel>();
			
			DimMember currentMember = timeMember;
			while(currentMember!=null){
				allDls.add(currentMember.getDimLevel());
				currentMember = currentMember.getParentMember();
				
			}
		
			AllTimePeriods ps = th.getTimePeriods(allDls);
			
			for(DimMember member:members){
				if(ps.getStartDates().get(member)==null||ps.getStartDates().get(timeMember)==null){
					if(allChildMembers.indexOf(timeMember)>=allChildMembers.indexOf(member)){
						addMembers.add(member);
					}
				}else{
					if(ps.getStartDates().get(member).compareTo(ps.getStartDates().get(timeMember))<=0){
						addMembers.add(member);
						
					}
				}
			
				 
			}
			context.setValue(key, addMembers);
			
		}else{
			addMembers = (List<DimMember>)context.getValue(key);
		}
		

		
		List<DimVector> queryVector = new ArrayList<DimVector>();
		Set<DataCell> cacheCells = new HashSet<DataCell>();
		
		for(DimMember member:addMembers){
			DimVector addDv = dataCell.getDimVector().addOrReplaceDimMember(member);
			DataCell cacheCall = context.getExecuteRangeCell(dataCell.getCubeDef().getObjcode(), addDv);
			if(cacheCall!=null){
				cacheCells.add(cacheCall);
			}else{
				queryVector.add(addDv);
				context.putExecuteRangeCell(addDv,new DataCell(dataCell.getCubeDef(), addDv));
			}
		}
		
		
		if(queryVector.size()>0){
			IDataSetService idss =  context.getService(IDataSetService.class);
			try {
				
				ICubeDataSet set = idss.queryDataSet(dataCell.getCubeDef(),queryVector);
//				putExecuteRangeCell
				for(DataCell dc:set.getDataResult()){
					context.putExecuteRangeCell(dc.getDimVector(),dc);
				}
				
				cacheCells.addAll(set.getDataResult());
			
			} catch (BusinessException e) {
				NtbLogger.error(e);
			}
			
		}
		return addCell(cacheCells) ;
		 
	}
	
	DimMember getParentMember(DimMember timeMember){
		
		
		DimMember parentMember = timeMember;
		String dimLevelPk = timeMember.getLevelValue().getDimLevel().getPk_obj();
		
		while(IDimLevelPKConst.YEAR.equals(dimLevelPk)||IDimLevelPKConst.ACCPYEAR.equals(dimLevelPk)){
			parentMember = timeMember.getParentMember();
			if(parentMember==null){
				NtbLogger.error(new BusinessRuntimeException("时间未发现年！"+timeMember.getUniqKey()));
				return null;
			}
		}
		return parentMember;
		
	}

}
