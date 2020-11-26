package nc.itf.ct.sendsale;

import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;

public interface ISendSaleServer {
	
	CtSaleJsonVO pushSaleToService(AggCtSaleVO saleVO) ;

}
