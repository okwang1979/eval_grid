package test.tbb;

import java.util.HashMap;
import java.util.Map;

import nc.bs.framework.test.AbstractTestCase;
import nc.vo.pfxx.jms.JMSUtil;

public class TestNcMessage extends AbstractTestCase {

	public void testJMSUtil() throws Exception {
		Map<String, String> object = new HashMap<String, String>();
		object.put("account", "design");
		object.put("sender", "1101");
		object.put("receiver", "0001");
		JMSUtil.sendFile("tcp://localhost:61616",
				"E:\\temp\\excelimport.xml", "UAP_EAI_MSG_Q",
				"UAP_EAI_MSG_RET_Q","dd","dd","dd", object,"",1,true);

	}

}
