package nccloud.pubimpl.platform.attachment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nc.vo.pub.filesystem.NCFileVO;
import nccloud.web.platform.attachment.vo.AttachPathVo;

public class GetFilePathService {
	public  static final String FILT_TYPE_HTXZW ="zw" ;	
	public  static final String FILT_TYPE_HTSPD ="spattach" ;
	public  static final String FILT_TYPE_WFSQWTS ="wsattach" ;
	public  static final String FILT_TYPE_DFSQWTS ="dsattach" ;
	public  static final String FILT_TYPE_HTQSWB ="qsattach" ;
	public  static final String FILT_TYPE_ZBTZS ="zbattach" ;
	public  static final String FILT_TYPE_QT ="otherattach" ;
	
	
	public List<AttachPathVo> getFilePath(String pk_ct) {
		List<AttachPathVo> resultList = new ArrayList<AttachPathVo>();
		try {
			NCFileVO[] ncfiles = null;
//	   	    pk_ct = "1001A11000000000VTTI";
	   	    FileSpaceDAOForNCC dao = new FileSpaceDAOForNCC();	 
	   	    ncfiles = dao.queryFileVOsByPath(pk_ct);
	   	    Map<String, String> map = dao.queryVbillCode(pk_ct);
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
	        Date parse;
			parse = sdf.parse(map.get("subscribedate"));
	        String yearMonth = sdf.format(parse);
	        String ctCode = map.get("vbillcode");
	   	    for (int i = 0; i < ncfiles.length; i++) {
				NCFileVO ncFileVO = ncfiles[i];
				String name = ncFileVO.getName();
				if(!pk_ct.equals(name)) {
					AttachPathVo attachPathVo  = new AttachPathVo();
					attachPathVo.setCompCode("kgjn");
					attachPathVo.setYearMonthStr(yearMonth);
					attachPathVo.setCtCode(ctCode);
					String parentpath = ncFileVO.getParentpath();
					String attachType = attachTypeConvert(parentpath);
					attachPathVo.setAttachType(attachType);
					attachPathVo.setFileName(name);
					resultList.add(attachPathVo);
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultList;
	}
	/***
	   * 附件类型转换
	 * @param attachType
	 * @return
	 */
	public static String attachTypeConvert(String attachType) {
		if (attachType.contains("合同正文")) {
			attachType = FILT_TYPE_HTXZW;
		}
		if (attachType.contains("合同审批单")) {
			attachType = FILT_TYPE_HTSPD;
		}
		if (attachType.contains("我方授权委托书")) {
			attachType = FILT_TYPE_WFSQWTS;
		}
		if (attachType.contains("对方授权委托书")) {
			attachType = FILT_TYPE_DFSQWTS;
		}
		if (attachType.contains("合同签署文本")) {
			attachType = FILT_TYPE_HTQSWB;
		}
		if (attachType.contains("中标通知书")) {
			attachType = FILT_TYPE_ZBTZS;
		}
		if (attachType.contains("其它")) {
			attachType = FILT_TYPE_QT;
		}  
		return attachType;
	}
}

