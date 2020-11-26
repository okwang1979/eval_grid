package nc.impl.iufo.cacu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.itf.iufo.data.IMeasureDataSrv;
import nc.itf.iufo.servive.IControlAccountService;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.param.CheckResultVO;
import nc.vo.pub.param.TempParamVO;

public class ControlAccountServiceImpl extends BaseService implements IControlAccountService {
	@Override
	public List<CheckResultVO> checkKB(TempParamVO params) throws BusinessException{
		String pk_org  = params.getPk_org();
		//根据任务code获取任务id
		String taskid = getTaskBycode("KUAIBAO");
		//会计期间
		String priod = params.getPeriod();
		//币种
		String pk_currency = params.getPk_currency();
		//关键字组合值
		String inputkeys[] = new String[]{pk_org,pk_currency,priod};
		MeasurePubDataVO measurePubDataVO = getMeasurePubDataVO(taskid, inputkeys);
		//指标坐标
		String positionD53 ="D53";
		String positionD54 = "D54";
		String positionF53 = "F53";
		String positionF54 = "F54";
		//根据报表编码获取报表指标坐标和指标的一一对应map
		Map<String, MeasureVO> reportMeasureVOPosition = getReportMeasureVOPosition("QYCWKB1");
		List<IStoreCell> cells = new ArrayList<IStoreCell>();
		cells.add(reportMeasureVOPosition.get(positionD53));
		cells.add(reportMeasureVOPosition.get(positionD54));
		cells.add(reportMeasureVOPosition.get(positionF53));
		cells.add(reportMeasureVOPosition.get(positionF54));

		//获取当前期间指标数据
		MeasureDataVO[] vos = NCLocator.getInstance().lookup(IMeasureDataSrv.class).getRepData(new String[] { measurePubDataVO.getAloneID() },cells.toArray(new MeasureVO[cells.size()]));
		
		List<CheckResultVO> results = new ArrayList<CheckResultVO>();
		
		CheckResultVO result1 = new CheckResultVO();
	
		//当前期间的单元格
		String D53 = "";
		String D54 = "";
		Map<IStoreCell,String> measureMap = new HashMap<IStoreCell,String>();
		
		for(MeasureDataVO vo : vos){
			measureMap.put(vo.getMeasureVO(),vo.getDataValue());
			if(reportMeasureVOPosition.get(positionD53).equals(vo.getMeasureVO())){
				D53 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionD54).equals(vo.getMeasureVO())){
				D54 = vo.getDataValue();
			}
		}
		if(measureMap.isEmpty()||(measureMap.get(reportMeasureVOPosition.get(positionD53)).equals("")||"0.0".equals(measureMap.get(reportMeasureVOPosition.get(positionD53))))
			&&(measureMap.get(reportMeasureVOPosition.get(positionF53))== null ||"".equals(measureMap.get(reportMeasureVOPosition.get(positionF53))))
			||
			(measureMap.get(reportMeasureVOPosition.get(positionD54)).equals("")||"0.0".equals(measureMap.get(reportMeasureVOPosition.get(positionD54))))
			&&(measureMap.get(reportMeasureVOPosition.get(positionF54))== null ||"".equals(measureMap.get(reportMeasureVOPosition.get(positionF54))))
				){
			result1.setCheckflag(false);
			result1.setMsg("职工人数未录入或者未说明人数为空原因，请填写D53，D54或F53,F54单元格再结账");
		}else{
			result1.setCheckflag(true);
			result1.setMsg("职工人数已录入，审核通过");
		}
		
		results.add(result1);
		
		String priod2 = "";
		try {
			priod2 = getLastPriod(priod);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//获取上个期间的数据
		String pD53 = "";
		String pD54 = "";
		String inputkeys2[] = new String[]{pk_org,pk_currency,priod2};
		MeasurePubDataVO measurePubDataVO2 = getMeasurePubDataVO(taskid, inputkeys2);
		MeasureDataVO[] vos2 = NCLocator.getInstance().lookup(IMeasureDataSrv.class).getRepData(new String[] { measurePubDataVO2.getAloneID() },cells.toArray(new MeasureVO[cells.size()]));
		if(vos2 != null){
			for(MeasureDataVO vo : vos2){
				if(reportMeasureVOPosition.get(positionD53).equals(vo.getMeasureVO())){
					pD53 = vo.getDataValue();
				}else if(reportMeasureVOPosition.get(positionD54).equals(vo.getMeasureVO())){
					pD54 = vo.getDataValue();
				}
			}
		}
		
		CheckResultVO result2 = new CheckResultVO();
		if(pD53.equals("")){
			result2.setCheckflag(false);
			result2.setMsg("'职工人数(D53)'数据未录入，请录入再进行结账");
			results.add(result2);
		}else{
			UFDouble rate = null;
			if("0.0".equals(pD53)){
				rate = new UFDouble(0);
			}
			else{ 
				rate =(new UFDouble(D53).add(new UFDouble(pD53).multiply(new UFDouble(-1)))).div( new UFDouble(pD53));
			}
			if(rate.toDouble() > 0.1d){
				result2.setCheckflag(false);
				result2.setMsg("'职工人数(D53)'与上月数对比变动超过10%，解释原因请写在F53单元格中");
			}else{
				if(!"0.0".equals(measureMap.get(reportMeasureVOPosition.get(positionD53)))){
					result2.setCheckflag(true);
					result2.setMsg("'职工人数(D53)'审核通过");
				}
			}
			results.add(result2);
		}
		CheckResultVO result3 = new CheckResultVO();
		if(pD54.equals("")){
			result3.setCheckflag(false);
			result3.setMsg("'其中：在岗职工人数(D54)'数据未录入，请录入再进行结账");
			results.add(result3);
		}else{
			UFDouble rate = null;
			if("0.0".equals(pD54)){
				rate = new UFDouble(0);
			}else{
				rate =(new UFDouble(D54).add(new UFDouble(pD54).multiply(new UFDouble(-1)))).div( new UFDouble(pD54));
			}
			if(rate.toDouble() > 0.1d){
				result3.setCheckflag(false);
				result3.setMsg("'其中：在岗职工人数(D54)'与上月数对比变动超过10%，解释原因请写在F54单元格中");
			}else{
				if(!"0.0".equals(measureMap.get(reportMeasureVOPosition.get(positionD53)))){
					result3.setCheckflag(true);
					result3.setMsg("'其中：在岗职工人数(D54)'审核通过");
				}
			}
			results.add(result3);
		}
		
		return results;
	}

	@Override
	public List<CheckResultVO> checkPressureControl(TempParamVO params)
			throws BusinessException {
		String pk_org  = params.getPk_org();
		//根据任务code获取任务id
		String taskid = getTaskBycode("KUAIBAO");
		//会计期间
		String priod = params.getPeriod();
		//币种
		String pk_currency = params.getPk_currency();
		//关键字组合值
		String inputkeys[] = new String[]{pk_org,pk_currency,priod};
		MeasurePubDataVO measurePubDataVO = getMeasurePubDataVO(taskid, inputkeys);
		//指标坐标
		String positionD4 = "D4";
		String positionD5 ="D5";
		String positionD10 = "D10";
		String positionD11= "D11";
		String positionD12 ="D12";
		
		String positionC4 = "C4";
		String positionC5 ="C5";
		String positionC10 = "C10";
		String positionC11= "C11";
		String positionC12 ="C12";
		
		String positionJ4 = "J4";
		String positionJ5 ="J5";
		String positionJ10 = "J10";
		String positionJ11 = "J11";
		String positionJ12 ="J12";
		//根据报表编码获取报表指标坐标和指标的一一对应map
		Map<String, MeasureVO> reportMeasureVOPosition = getReportMeasureVOPosition("QYCWKB2");
		List<IStoreCell> cells = new ArrayList<IStoreCell>();
		cells.add(reportMeasureVOPosition.get(positionD4));
		cells.add(reportMeasureVOPosition.get(positionD5));
		cells.add(reportMeasureVOPosition.get(positionD10));
		cells.add(reportMeasureVOPosition.get(positionD11));
		cells.add(reportMeasureVOPosition.get(positionD12));
		
		cells.add(reportMeasureVOPosition.get(positionC4));
		cells.add(reportMeasureVOPosition.get(positionC5));
		cells.add(reportMeasureVOPosition.get(positionC10));
		cells.add(reportMeasureVOPosition.get(positionC11));
		cells.add(reportMeasureVOPosition.get(positionC12));
		
		cells.add(reportMeasureVOPosition.get(positionJ4));
		cells.add(reportMeasureVOPosition.get(positionJ5));
		cells.add(reportMeasureVOPosition.get(positionJ10));
		cells.add(reportMeasureVOPosition.get(positionJ11));
		cells.add(reportMeasureVOPosition.get(positionJ12));
		//获取当前期间指标数据
		MeasureDataVO[] vos = NCLocator.getInstance().lookup(IMeasureDataSrv.class).getRepData(new String[] { measurePubDataVO.getAloneID() },cells.toArray(new MeasureVO[cells.size()]));
		//当前期间的单元格
		String  C4 = null,C5 = null,C10= null,C11= null,C12= null,
				D4 = null,D5 = null,D10= null,D11= null,D12= null,
				J4= null,J5= null,J10= null,J11= null,J12= null;
		for(MeasureDataVO vo : vos){
			if(reportMeasureVOPosition.get(positionC4).equals(vo.getMeasureVO())){
				C4 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionC5).equals(vo.getMeasureVO())){
				C5 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionC10).equals(vo.getMeasureVO())){
				C10 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionC11).equals(vo.getMeasureVO())){
				C11 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionC12).equals(vo.getMeasureVO())){
				C12 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionD4).equals(vo.getMeasureVO())){
				D4 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionD5).equals(vo.getMeasureVO())){
				D5 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionD10).equals(vo.getMeasureVO())){
				D10 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionD11).equals(vo.getMeasureVO())){
				D11 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionD12).equals(vo.getMeasureVO())){
				D12 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionJ4).equals(vo.getMeasureVO())){
				J4 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionJ5).equals(vo.getMeasureVO())){
				J5 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionJ10).equals(vo.getMeasureVO())){
				J10 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionJ11).equals(vo.getMeasureVO())){
				J11 = vo.getDataValue();
			}else if(reportMeasureVOPosition.get(positionJ12).equals(vo.getMeasureVO())){
				J12 = vo.getDataValue();
			}
		}
		List<CheckResultVO> results = new ArrayList<CheckResultVO>();
		String priod2 = null;
		try {
			priod2 = getLastPriod(priod);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//获取上个期间的数据
		String  lastD4 = null,lastD5 = null,lastD10 = null,lastD11 = null,lastD12 = null,lastJ4= null,lastJ5= null,lastJ10= null,lastJ11= null,lastJ12= null;
		
		String inputkeys2[] = new String[]{pk_org,pk_currency,priod2};
		MeasurePubDataVO measurePubDataVO2 = getMeasurePubDataVO(taskid, inputkeys2);
		MeasureDataVO[] vos2 = NCLocator.getInstance().lookup(IMeasureDataSrv.class).getRepData(new String[] { measurePubDataVO2.getAloneID() },cells.toArray(new MeasureVO[cells.size()]));
		if(vos2 != null){
			for(MeasureDataVO vo : vos2){
				if(reportMeasureVOPosition.get(positionD4).equals(vo.getMeasureVO())){
					lastD4 = vo.getDataValue();
				}else if(reportMeasureVOPosition.get(positionD5).equals(vo.getMeasureVO())){
					lastD5 = vo.getDataValue();
				}else if(reportMeasureVOPosition.get(positionD10).equals(vo.getMeasureVO())){
					lastD10 = vo.getDataValue();
				}else if(reportMeasureVOPosition.get(positionD11).equals(vo.getMeasureVO())){
					lastD11 = vo.getDataValue();
				}else if(reportMeasureVOPosition.get(positionD12).equals(vo.getMeasureVO())){
					lastD12 = vo.getDataValue();
				}
			}
		}
		
		double rate = 0.0;
		
		CheckResultVO result2 = new CheckResultVO();
		if((null == D4 ||"".equals(D4)||"0.0".equals(D4))&&("".equals(J4)||null == J4)){
			result2.setCheckflag(false);
			result2.setMsg("应收账款压降金额(D4)未录入，如无此项可忽略");
			results.add(result2);
		}else{
			rate =(new UFDouble(D4).doubleValue()-new UFDouble(lastD4).doubleValue())/ new UFDouble(lastD4).doubleValue();
			if(new UFDouble(C4).doubleValue()<new UFDouble(D4).doubleValue()){
				result2.setCheckflag(false);
				result2.setMsg("'应收账款压降金额(D4)'应小于等于'应收账款基准日净额(C4)'");
			}else if(new UFDouble(D4).doubleValue() < new UFDouble(lastD4).doubleValue()){
				result2.setCheckflag(false);
				result2.setMsg("'应收账款压降金额(D4)'本月数应大于等于上月数");
			}else if(rate > 0.1d){
				result2.setCheckflag(false);
				result2.setMsg("应收账款压降金额(D4)与上月比变动超过10%，解释原因请写在J4单元格中");
			}else{
				result2.setCheckflag(true);
				result2.setMsg("应收账款压降金额(D4)，审核通过");
			}
			results.add(result2);
			
		}
		CheckResultVO result3 = new CheckResultVO();
		if((null == D5||"".equals(D5)||"0.0".equals(D5))&&("".equals(J5)||null == J5)){
			result3.setCheckflag(false);
			result3.setMsg("1年以上应收账款压降金额(D5)数据未录入，如无此项可忽略");
			results.add(result3);
		}else{
			rate =(new UFDouble(D5).doubleValue()-new UFDouble(lastD5).doubleValue())/ new UFDouble(lastD5).doubleValue();
			if(new UFDouble(C5).doubleValue()<new UFDouble(D5).doubleValue()){
				result3.setCheckflag(false);
				result3.setMsg("'1年以上应收账款压降金额(D5)'应小于等于'其中：1年以上应收账款基准日净额'(C5)");
				results.add(result3);
			}else if(new UFDouble(D5).doubleValue() < new UFDouble(lastD5).doubleValue()){
				result3.setCheckflag(false);
				result3.setMsg("'1年以上应收账款压降金额(D5)'本月数应大于等于上月数");
			}else if(rate > 0.1d){
				result3.setCheckflag(false);
				result3.setMsg("'1年以上应收账款压降金额(D5)'与上月比变动超过10%，解释原因请写在J5单元格中");
			}else{
				result3.setCheckflag(true);
				result3.setMsg("1年以上应收账款压降金额(D5)，审核通过");
			}
			results.add(result3);
		} 
		
		CheckResultVO result4 = new CheckResultVO();
		if((null == D10||"".equals(D10)||"0.0".equals(D10))&&("".equals(J10)||null == J10)){
			result4.setCheckflag(false);
			result4.setMsg("'存货压降金额(D10)'数据未录入，如无此项可忽略");
			results.add(result4);
		}else{
			rate =(new UFDouble(D10).doubleValue()-new UFDouble(lastD10).doubleValue())/ new UFDouble(lastD10).doubleValue();
			if(new UFDouble(C10).doubleValue()<new UFDouble(D10).doubleValue()){
				result4.setCheckflag(false);
				result4.setMsg("'存货压降金额(D10)'应小于等于'存货基准日净额(C10)'");
				
			}else if(new UFDouble(D10).doubleValue() < new UFDouble(lastD10).doubleValue()){
				result4.setCheckflag(false);
				result4.setMsg("'存货压降金额(D10)'本月数应大于等于上月数");
			}else if(rate > 0.1d){
				result4.setCheckflag(false);
				result4.setMsg("'存货压降金额(D10)'与上月比变动超过10%，解释原因请写在J10单元格中");
			}else{
				result4.setCheckflag(true);
				result4.setMsg("'存货压降金额(D10)'，审核通过");
			}
			results.add(result4);
		}
		
		CheckResultVO result5 = new CheckResultVO();
		if((null == D11||"".equals(D11)||"0.0".equals(D11))&&("".equals(J11)||null == J11)){
			result5.setCheckflag(false);
			result5.setMsg("'其中：1年以上的存货压降金额(D11)'数据未录入，如无此项可忽略");
			results.add(result5);
		}else{
			rate =(new UFDouble(D11).doubleValue()-new UFDouble(lastD11).doubleValue())/ new UFDouble(lastD11).doubleValue();
			if(new UFDouble(C11).doubleValue()<new UFDouble(D11).doubleValue()){
				result5.setCheckflag(false);
				result5.setMsg("'其中：1年以上的存货压降金额(D11)'应小于等于'其中：1年以上的存货基准日净额(C11)'");
			}else if(new UFDouble(D11).doubleValue() < new UFDouble(lastD11).doubleValue()){
				result5.setCheckflag(false);
				result5.setMsg("'其中：1年以上的存货压降金额(D11)'本月数应大于等于上月数");
			}else if(rate > 0.1d){
				result5.setCheckflag(false);
				result5.setMsg("'其中：1年以上的存货压降金额(D11)'与上月比变动超过10%，解释原因请写在J11单元格中");
			}else{
				result5.setCheckflag(true);
				result5.setMsg("'其中：1年以上的存货压降金额(D11)'，审核通过");
			}
			results.add(result5);
		}
		
		CheckResultVO result6 = new CheckResultVO();
		if((null == D12||"".equals(D12)||"0.0".equals(D12))&&("".equals(J12)||null == J11)){
			result6.setCheckflag(false);
			result6.setMsg("'其中：非正常存货压降金额(D12)'数据未录入，如无此项可忽略");
			results.add(result6);
		}else{
			rate =(new UFDouble(D12).doubleValue()-new UFDouble(lastD12).doubleValue())/ new UFDouble(lastD12).doubleValue();
			if(new UFDouble(C12).doubleValue()<new UFDouble(D12).doubleValue()){
				result6.setCheckflag(false);
				result6.setMsg("'其中：非正常存货压降金额(D12)'应小于等于C12");
			}else if(new UFDouble(D12).doubleValue() < new UFDouble(lastD12).doubleValue()){
				result6.setCheckflag(false);
				result6.setMsg("'其中：非正常存货压降金额(D12)'本月数应大于等于上月数");
			}else if(rate > 0.1d){
				result6.setCheckflag(false);
				result6.setMsg("'其中：非正常存货压降金额(D12)'与上月比变动超过10%，解释原因请写在J12单元格中");
			}else{
				result6.setCheckflag(true);
				result6.setMsg("'其中：非正常存货压降金额(D12)'，审核通过");
			}
			results.add(result6);
		}
		
		return results;
	}

	@Override
	public List<CheckResultVO> checkN03B(TempParamVO params)
			throws BusinessException {
		String pk_org  = params.getPk_org();
		//根据任务code获取任务id
		String taskid = getTaskBycode("KUAIBAO");
		//会计期间
		String priod = params.getPeriod();
		//币种
		String pk_currency = params.getPk_currency();
		//关键字组合值
		String inputkeys[] = new String[]{pk_org,pk_currency,priod};
		MeasurePubDataVO measurePubDataVO = getMeasurePubDataVO(taskid, inputkeys);
		//指标坐标
		String positionR6 ="R6";
		//根据报表编码获取报表指标坐标和指标的一一对应map
		Map<String, MeasureVO> reportMeasureVOPosition = getReportMeasureVOPosition("内03表");
		List<IStoreCell> cells = new ArrayList<IStoreCell>();
		cells.add(reportMeasureVOPosition.get(positionR6));
		
		//获取当前期间指标数据
		MeasureDataVO[] vos = NCLocator.getInstance().lookup(IMeasureDataSrv.class).getRepData(new String[] { measurePubDataVO.getAloneID() },cells.toArray(new MeasureVO[cells.size()]));
		List<CheckResultVO> results = new ArrayList<CheckResultVO>();
		CheckResultVO result = new CheckResultVO();
		if(vos.length < 1){
			result.setCheckflag(false);
			result.setMsg("此表未检查");
			results.add(result);
		}
		for(MeasureDataVO vo : vos){
			if("1".equals(vo.getValue())){
				result.setCheckflag(true);
				result.setMsg("此表审核通过");
				results.add(result);
			}else{
				result.setCheckflag(false);
				result.setMsg("此表未检查");
				results.add(result);
			}
		}
		return results;
	}


}
