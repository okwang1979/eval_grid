package nc.ms.tb.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.itf.mdm.dim.IDimManager;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.dim.DimDef;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimDefPKConst;
import nc.vo.mdm.dim.IDimLevelPKConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFDouble;

/**
 * ufo函数类
 * @author wangzhqa
 *
 */
public class UfoResult implements Serializable {
	
	/**
	 * 函数名称
	 */
	private String functionName;
	
	/**
	 * UFO的参数:应该传入最基本的参数.
	 */
	private String param;
	
	/**
	 * 最终运算时的参数.
	 */
	private String convertParam;
	
	 
	
	/**
	 * UFO函数对应的DataCell
	 */
	private DataCell dataCell;
	
	/**
	 * UFo计算结果
	 */
	private UFDouble numResult;
	
	private String strResult;
	
	/**
	 * 有计算结果把这个标志设置成true
	 */
	private boolean finish = false;
	
	
	public UfoResult(String ufoExpress,DataCell dataCell){
		if(ufoExpress==null||ufoExpress.length()<=2){
			return ;
		}
		ufoExpress = ufoExpress.substring(1, ufoExpress.length()-1);
		this.setParam(ufoExpress);
		String strFuncName ="";
		String strParams = "";

		int leftBracket = ufoExpress.indexOf("(");
		int lastRightBracket = ufoExpress.lastIndexOf(")");
		String noUFO = ufoExpress.substring(leftBracket + 1, lastRightBracket);
		try{
			noUFO = repalceDefault(noUFO,dataCell);
		}catch (BusinessRuntimeException ex) {
			NtbLogger.print(ex);
			 
			
		}
		
		strFuncName = ufoExpress.substring(0, leftBracket);
		strParams = noUFO;
		strParams = strParams.replace('#', '"');
		
		this.setFunctionName(strFuncName);
	
		
		
//		String convertParam = param.replace('#', '"');
		this.setConvertParam(strParams);
		this.setDataCell(dataCell);
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public DataCell getDataCell() {
		return dataCell;
	}

	public void setDataCell(DataCell dataCell) {
		this.dataCell = dataCell;
	}

 

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public String getConvertParam() {
		return convertParam;
	}

	public void setConvertParam(String convertParam) {
		this.convertParam = convertParam;
	}

	public UFDouble getNumResult() {
		return numResult;
	}

	public void setNumResult(UFDouble numResult) {
		this.numResult = numResult;
	}

	public String getStrResult() {
		return strResult;
	}

	public void setStrResult(String strResult) {
		this.strResult = strResult;
	}

 
	public Object getResult(){
		if(numResult!=null){
			return numResult;
		}
		if(strResult!=null){
			return strResult;
		}
		return "";
	}
	
	private String repalceDefault(String express, DataCell dataCell) {

 
		// 存放对应的宏以及内容.
		Map<String, String> replaceValue = new LinkedHashMap<String, String>();

		String allSortLevel = "ALL_SORT_DIMLEVEL";
		List<DimLevelSort> allDimLevels=getAllDimLevel();


		String toUpExpress = express;
		String returnExpress = express;
		for (DimLevelSort sortLevel : allDimLevels) {
			if (toUpExpress.contains("@" + sortLevel.getLevelBusiCode())) {

				LevelValue value = dataCell.getDimVector()
						.getLevelValue(sortLevel.getDimLevel());
				if (value == null) {
					throw new BusinessRuntimeException(
							"UFO's owner cell not DimVector:"
									+ sortLevel.getLevelBusiCode());
				}
				returnExpress = returnExpress.replaceAll(
						"@" + sortLevel.getLevelBusiCode(), value.getCode());
				// replaceValue.put("@"+sortLevel.getLevelBusiCode(),
				// value.getCode());

			}
		}
		
		
		
		//央客-王志强 建投-CUSTOMCLASS提花问题--begin
		String  repStr = "@CUSTOMCLASS";
		if(returnExpress.contains(repStr) ){
			
			
			IDimManager dm = DimServiceGetter.getDimManager();
		 
			DimLevel custDl = dm.getDimLevelByBusiCode("CUSTOM");
			 
			LevelValue value = dataCell.getDimVector().getLevelValue(custDl);
			
			String custClass = String.valueOf(value.getPropValue("customclass"));
			
			returnExpress = returnExpress.replaceAll(repStr, custClass);
		
		}
		
		//****end*****************************end
		
		
		if(returnExpress.contains("@ENDDAY") ){
			
			IDimManager dm = DimServiceGetter.getDimManager();
			DimDef timeDef = dm.getDimDefByPK(IDimDefPKConst.TIME);
			DimMember timeMember = dataCell.getDimVector().getDimMember(timeDef);
			if(timeMember!=null){
				DimLevel yearL = dm.getDimLevelByPK(IDimLevelPKConst.YEAR);
				DimLevel monthL = dm.getDimLevelByPK(IDimLevelPKConst.MONTH);
				LevelValue yearValue = timeMember.getLevelValue(yearL);
				LevelValue monthValue = timeMember.getLevelValue(monthL);
				if(yearValue==null){
					yearL = dm.getDimLevelByPK(IDimLevelPKConst.ACCPYEAR);
					yearValue = timeMember.getLevelValue(yearL);
				}
				if(monthValue==null){
					 monthL = dm.getDimLevelByPK(IDimLevelPKConst.ACCMONTH);
					  monthValue = timeMember.getLevelValue(monthL);
				}
				
				if(yearValue!=null&&monthValue!=null){
					returnExpress = returnExpress.replaceAll(
							"@BEGINDAY", "01");
					String endDay = "31";
					int month = (Integer)monthValue.getKey();
					if(month==4||month ==6||month ==9||month==11){
						endDay ="30";
						
					}
					if(month==2){
						int year = Integer.parseInt(String.valueOf(yearValue.getKey()));
						if(year%4==0){
							endDay ="29";
						}else{
							endDay ="28";
						}
						
					}
					returnExpress = returnExpress.replaceAll(
							"@ENDDAY", endDay);
				
				}
			 
			
			}
		
			
		}
		return returnExpress;

	}

	private List<DimLevelSort> getAllDimLevel() {
		IDimManager dm = DimServiceGetter.getDimManager();
		List<DimDef> dimDefs = dm.getAllDimDef();
		Set<DimLevelSort> dimSet = new HashSet<DimLevelSort>();
		for (DimDef dimDef : dimDefs) {
			for (DimLevel dimLevel : dimDef.getDimLevels()) {
				dimSet.add(new DimLevelSort(dimLevel));
			}
		}
		List<DimLevelSort> returnList = new ArrayList<DimLevelSort>();
		DimLevelSort[] sorts = dimSet.toArray(new DimLevelSort[0]);
		Arrays.sort(sorts);
		returnList.addAll(Arrays.asList(sorts));

		return returnList;

	}

}
class DimLevelSort implements Comparable<DimLevelSort> {

	private String levelBusiCode;

	private DimLevel dimLevel;

	public DimLevelSort(DimLevel dimLevel) {
		this.dimLevel = dimLevel;
		levelBusiCode = dimLevel.getBusiCode().toUpperCase();
	}

	public String getLevelBusiCode() {
		return levelBusiCode;
	}

	public void setLevelBusiCode(String levelBusiCode) {
		this.levelBusiCode = levelBusiCode;
	}

	public DimLevel getDimLevel() {
		return dimLevel;
	}

	public void setDimLevel(DimLevel dimLevel) {
		this.dimLevel = dimLevel;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DimLevelSort) {
			return this.levelBusiCode
					.equals(((DimLevelSort) obj).levelBusiCode);
		}
		return false;
	}
	

	@Override
	public int hashCode() {
		 
		return this.levelBusiCode.hashCode();
	}

	@Override
	public int compareTo(DimLevelSort o) {

		return -this.levelBusiCode.compareTo(o.levelBusiCode);
	}

}