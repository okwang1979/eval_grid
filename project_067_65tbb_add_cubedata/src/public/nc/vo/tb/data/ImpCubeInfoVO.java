package nc.vo.tb.data;

import java.util.List;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;

/**
 * @author 
 *
 */
public class ImpCubeInfoVO extends SuperVO {


	private String cube_code;

	private String user_code;
	private String remark;
	private String def1;
	private String def2;
	private String def3;
	private String def4;
	private String def5;

	private String def7;
	private String def8;

	
	private ImpCubeDataVO[]  datas;

	

	
	
	
	
	
	public ImpCubeDataVO[] getDatas() {
		return datas;
	}






	public void setDatas(ImpCubeDataVO[] datas) {
		this.datas = datas;
	}












	public String getCube_code() {
		return cube_code;
	}




	public void setCube_code(String cube_code) {
		this.cube_code = cube_code;
	}




	public String getUser_code() {
		return user_code;
	}




	public void setUser_code(String user_code) {
		this.user_code = user_code;
	}




	public String getRemark() {
		return remark;
	}




	public void setRemark(String remark) {
		this.remark = remark;
	}




	public String getDef1() {
		return def1;
	}




	public void setDef1(String def1) {
		this.def1 = def1;
	}




	public String getDef2() {
		return def2;
	}




	public void setDef2(String def2) {
		this.def2 = def2;
	}




	public String getDef3() {
		return def3;
	}




	public void setDef3(String def3) {
		this.def3 = def3;
	}




	public String getDef4() {
		return def4;
	}




	public void setDef4(String def4) {
		this.def4 = def4;
	}




	public String getDef5() {
		return def5;
	}




	public void setDef5(String def5) {
		this.def5 = def5;
	}




	public String getDef7() {
		return def7;
	}




	public void setDef7(String def7) {
		this.def7 = def7;
	}




	public String getDef8() {
		return def8;
	}




	public void setDef8(String def8) {
		this.def8 = def8;
	}



	

}
