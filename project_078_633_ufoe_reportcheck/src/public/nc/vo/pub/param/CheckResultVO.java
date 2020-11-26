package nc.vo.pub.param;

import nc.vo.pub.SuperVO;

public class CheckResultVO extends SuperVO{
	private static final long serialVersionUID = 27774285266872476L;
	private boolean checkflag;
	private String msg;
	public boolean isCheckflag() {
		return checkflag;
	}
	public void setCheckflag(boolean checkflag) {
		this.checkflag = checkflag;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
