package test.tbb.util;

import junit.framework.TestCase;

public class TestUtilSelf extends TestCase{
	
	public void testTest(){
		
		String taskDefName  = "aaa,bbb,ccc";
		if(taskDefName.contains(",")) {
		String[] taskNames = taskDefName.split(",");
		for(String currentTaskName:taskNames) {
			taskDefName = currentTaskName;
			if(taskDefName==null||taskDefName.trim().isEmpty()||"null".contains(taskDefName)){
				 continue;
			}
			}
	}
	}

}
