package nc.vo.ct.saledaily.entity;

import java.io.Serializable;

public class CtSaleFileJsonVO implements Serializable{
	
	private String filename;
	
	private String filepath;
	
	private String createtime;
	
	private Integer num = 1;
	
	public CtSaleFileJsonVO(String filename,String filepath,String createtime) {
		this.filename = filename;
		this.filepath = filepath;
		this.createtime = createtime;
		
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}
	
	
	
	

}
