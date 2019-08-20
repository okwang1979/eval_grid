package nc.ui.hbbb.dxrelation.formula.refprocessor;

import nc.util.hbbb.dxrelation.formula.DXFmlEditConst;
import nc.vo.hbbb.util.ComBoxVO;

public class INTRReportDataSourceProcessor extends  ComBoxProcessor {

	public INTRReportDataSourceProcessor(){
		super();
	}
	
	@Override
	public ComBoxVO[] getComboxvos() {
		// TODO Auto-generated method stub
		if(null==comboxvos){
			comboxvos=new ComBoxVO[]{DXFmlEditConst.REPORT_SELF_TO_OPP,DXFmlEditConst.REPORT_OPP_TO_SELF};
		}
		return comboxvos;
	}

}
