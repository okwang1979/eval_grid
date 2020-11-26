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
		  names.add("2016-2018��ɷݹ�˾�ڲ���Ʒ����������������");
		  names.add("2019��Ͷ��Ԥ����������2020��ƻ�Ԥ�㽨���");
		  names.add("�ιɾ�ӪͶ���Բ����");
		  names.add("�ιɾ�ӪͶ���Բ�����ױ�");
		  names.add("���á���ʬ��ҵ���Ϳ�չ������ҵר�������ȼ���");
		  names.add("���-���Ź�˾�ڲ���Ʒ����������������");
		  names.add("���-��Ʒ���������ٻ��ܱ�");
		  names.add("���-�������Ʒ����������������");
		  names.add("���-������ҵ�ڲ���ƻ�����Ա��������չ");
		  names.add("��Ʒ���������ٻ��ܱ�");
		  names.add("�������Ʒ����������������-2019��4����-�滮��չ��");
		  names.add("�¶������ƻ�");
		  names.add("�¶����ۼƻ�");
		  names.add("�й�����2020��ѹ�������ƻ����Ȱ���");
		  names.add("�й�����2020��ѹ�������ƻ����Ȱ��ű�");
		  names.add("�й�����Ƹ�ʾ���ж���̨�˼ƻ���������");
		  names.add("�й�����Ƹ�ʾ���ж���̨�˼ƻ�����������");
		  names.add("������ҵ�������ĸ��չ���");
		  names.add("������ҵ��Ӫ�÷����⼰����������");
		  names.add("������ҵ�������ĸ��չ�����");
		  names.add("������ҵ��Ӫ�÷����⼰������������");
		  names.add("������ҵ�ڲ���ƻ�����Ա��������չ���ͳ�Ʊ�");	   
		  names.add("�ʲ�����-�ĸ����");
		  names.add("�ʲ�����-Ͷ��");	   
		  names.add("�ʲ���������");
		  names.add("�������ط������������г��۸���Ϣ���");
		  names.add("�������ط������������г��۸���Ϣ����");
		  
		  
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
