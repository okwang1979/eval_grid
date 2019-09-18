package nc.report.hbbb.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ufida.zior.console.ActionHandler;

import nc.bs.framework.test.AbstractTestCase;
 
import nc.vo.pub.pa.CurrEnvVO;

public class TestWorkPlugin extends AbstractTestCase{
	
 
 	
	public void testPlugin(){
//		ActionHandler.exec(ReportImportWorkPlugin.class.getName(),
//				"executeTask", new CurrEnvVO(), true);
	}
	
	public void testMeasurePlugin(){
//		ActionHandler.exec(ReportMeasureWorkPlugin.class.getName(),
//				"executeTask", new CurrEnvVO(), true);
	}
	
	
	public void testShop(){
		double[] values = {90,70};
		double totalValue = 4999;
		
		List<String> rtn =  MathClass.getTotal(values, totalValue,20);
		for(String printInfo:rtn){
			System.out.println(printInfo);
		}
	 
		
	}
	

	
	 

}
