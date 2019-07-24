package nc.itf.hbbb.total;

import java.util.List;

import nc.vo.hbbb.total.HbTotalSchemeVO;

public interface IHbTotalSchemeServer {
	
	String saveOrUpdateScheme(HbTotalSchemeVO vo);
	
	boolean deleleSchemeByPk(String pk_scheme);
	
	List<HbTotalSchemeVO> queryBy(String pk_rms,String pk_rmsversion);

}
