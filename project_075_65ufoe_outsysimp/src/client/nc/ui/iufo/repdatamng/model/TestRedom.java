package nc.ui.iufo.repdatamng.model;

import nc.impl.ufoe.backplugin.CellsModelImpWorkPlugin;
import junit.framework.TestCase;

public class TestRedom extends TestCase {
	
	public void testRedom(){
		CellsModelImpWorkPlugin test =new CellsModelImpWorkPlugin();
		
		for(int i=0;i<100;i++){
			System.out.println(test.getGuid());
		}
	}
	
	

}
