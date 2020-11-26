package test.ui.epmp.plan.txtdata;

import java.util.ArrayList;
import java.util.List;

import nc.bs.framework.common.NCLocator;
import nc.bs.framework.test.AbstractTestCase;
import nc.itf.epmp.plan.txtdata.IPlanTxtValueManager;
import nc.itf.tb.rule.IBusiRuleQuery;

public class TestIPlanTxtValueManager extends AbstractTestCase{
	
	    protected String getHost() {
	      return System.getProperty("nc.host", "localhost");
	  }
	  
	  protected String getPort() {
		  return "6500";
	     }
	  
	  public void testGetTaskData() throws Exception{
		  
		  List<String>  names = new ArrayList<>();
		  names.add("2016-2018年股份公司内部审计发现问题整改情况表");
		  names.add("2019年投资预计完成情况及2020年计划预算建议表");
		  names.add("参股经营投资自查情况");
		  names.add("参股经营投资自查情况套表");
		  names.add("处置“僵尸企业”和开展特困企业专项治理季度监测表");
		  names.add("审计-集团公司内部审计发现问题整改情况表");
		  names.add("审计-审计发现问题跟踪汇总表");
		  names.add("审计-审计署审计发现问题整改情况表");
		  names.add("审计-中央企业内部审计机构人员及工作开展");
		  names.add("审计发现问题跟踪汇总表");
		  names.add("审计署审计发现问题整改情况表-2019年4季度-规划发展部");
		  names.add("月度生产计划");
		  names.add("月度销售计划");
		  names.add("中国铁物2020年压减工作计划进度安排");
		  names.add("中国铁物2020年压减工作计划进度安排表");
		  names.add("中国铁物“科改示范行动”台账计划及完成情况");
		  names.add("中国铁物“科改示范行动”台账计划及完成情况表");
		  names.add("中央企业厂办大集体改革进展情况");
		  names.add("中央企业经营用房出租及减免租金情况");
		  names.add("中央企业厂办大集体改革进展情况表");
		  names.add("中央企业经营用房出租及减免租金情况表");
		  names.add("中央企业内部审计机构人员及工作开展情况统计表");	   
		  names.add("资产报表-改革改制");
		  names.add("资产报表-投资");	   
		  names.add("资产报表任务");
		  names.add("自有土地房产所在区域市场价格信息监测");
		  names.add("自有土地房产所在区域市场价格信息监测表");
		  
		  
		  IBusiRuleQuery query = NCLocator.getInstance().lookup(IBusiRuleQuery.class);
		  
		  for(String name:names) {
			  query.CheckRuleIsUsed(name);
		  }
		 
//		  IPlanTxtValueManager manager = NCLocator.getInstance().lookup(IPlanTxtValueManager.class);
//		  
//		  for(String name:names) {
//			  manager.getDataFromTask(name);
//		  }
		  
	  
//		  
//		  
//		
//		  
//		  
//		
//		  
//		  
//		  
//		  
//		 
//		 
		  
	  }

}
