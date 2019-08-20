package nc.ui.hbbb.dxrelation.formula.refprocessor;

import nc.vo.hbbb.util.ComBoxVO;

public class HBBBOperatorProcessor extends  ComBoxProcessor {

	public HBBBOperatorProcessor(){
		super();
	}
	
	@Override
	public ComBoxVO[] getComboxvos() {
		 
		if(null==comboxvos){
			ComBoxVO nullBox = new ComBoxVO("''", "");
			ComBoxVO addBox = new ComBoxVO("'+'", "+");
			ComBoxVO subBox = new ComBoxVO("'-'", "-");
			ComBoxVO multiBox = new ComBoxVO("'*'", "*");
			ComBoxVO divBox = new ComBoxVO("'/'", "/");
			comboxvos = new ComBoxVO[]{nullBox,addBox,subBox,multiBox,divBox};
		}
		return comboxvos;
	}

}
