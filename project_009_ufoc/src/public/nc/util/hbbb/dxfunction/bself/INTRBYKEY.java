package nc.util.hbbb.dxfunction.bself;

import nc.itf.hbbb.constants.HBFmlConst;

public class INTRBYKEY extends DXFUNC {
	
	public static int bselfparam_num=3;
	public boolean getBself() {
		if(this.getBselfParamValue()==HBFmlConst.SELF){
			return true;
		}else{
			return false;
		}
	
	}
}