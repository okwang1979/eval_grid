package nc.ui.hbbb.dxrelation.formula.refprocessor;

//import nc.ui.hbbb.combox.util.ComBoxManager.ComBoxVO;
import nc.util.hbbb.dxrelation.formula.DXFmlEditConst;
import nc.vo.hbbb.util.ComBoxVO;

public class VoucherTypeProcessor  extends  ComBoxProcessor{
	
	public VoucherTypeProcessor(){
		super();
	}

	@Override
	public ComBoxVO[] getComboxvos() {
		// TODO Auto-generated method stub
		if(null==comboxvos){
			comboxvos=new ComBoxVO[]{ new ComBoxVO(Integer.valueOf(0).toString(),"ȫ��ƾ֤"),new ComBoxVO(Integer.valueOf(1).toString(),"�Զ�ƾ֤"),new ComBoxVO(Integer.valueOf(2).toString(),"�ֹ�ƾ֤")};
		}
		
		return comboxvos;
	}

}
