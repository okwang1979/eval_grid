package nccloud.pubimpl.platform.attachment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nc.bs.framework.common.NCLocator;
import nccloud.commons.collections.CollectionUtils;
import nccloud.pubitf.platform.attachment.IAttachmentService;
import nccloud.web.platform.attachment.vo.ErrorInfo;
import nccloud.web.platform.attachment.vo.VsftpResult;
import nccloud.web.platform.attachment.vo.Vsftpd;

/**
 * 文件上传
 * 通过参数vsftpd.getOptionType()分为普通上传 和 覆盖上传两种
 * 第一种若文件已存在则返回错误信息提示文件已存在
 * 第二种则直接覆盖
 */
public class FtpUploadStrategy {

    private static Logger LOGGER = LoggerFactory.getLogger(FtpUploadStrategy.class);

    public VsftpResult vsftpMethod(Vsftpd vsftpd){
        LOGGER.info("FtpUploadStrategy.vsftpMethod start");
        VsftpResult result = new VsftpResult();
        List<ErrorInfo> errors = new ArrayList<>();
        if (StringUtils.isEmpty(vsftpd.getFileName())) {
            ErrorInfo errorInfo = new ErrorInfo("PARAMETER.FAIL","参数[fileName]不能为空！");
            errors.add(errorInfo);
        }
        if (vsftpd.getByteArry()==null) {
            ErrorInfo errorInfo = new ErrorInfo("PARAMETER.FAIL","参数[byteArry]不能为空！");
            errors.add(errorInfo);
        }
        //当不强制上传的时候   文件若已存在则上传失败
        boolean flag = false;
        try {
            if(FtpConstants.UP_LOAD.equals(vsftpd.getOptionType())) {
//                FileSpaceDAOForNCC dao = new FileSpaceDAOForNCC();
//                Map<String, String> map = dao.queryVbillCode(vsftpd.getBillId());
            
            	Map<String, String> map =	NCLocator.getInstance().lookup(IAttachmentService.class).queryVbillCode(vsftpd.getBillId());
            	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                 Date parse = sdf.parse(map.get("subscribedate"));
                 String yearMonth = sdf.format(parse);
                  yearMonth = yearMonth.replace("-", "");
                //判断日期文件夹是否存在
                boolean b = FtpUtil.fileExist("kgjn", yearMonth);
                
                vsftpd.setBillId(map.get("vbillcode"));
                if (b) {
//                    ErrorInfo errorInfo = new ErrorInfo("PARAMETER.FAIL", "文件[" + vsftpd.getFileName() + "]已存在！");
//                    errors.add(errorInfo);
                	//打开日期文件夹
                	boolean c = FtpUtil.openDirectory(yearMonth,vsftpd.getBillId());
                	//判断是否存在合同编号文件夹
                	//有合同编码文件夹时
                	if(c) {
                		//判断是否有对应的附件分类文件夹
                		String attachType = vsftpd.getFullPath();
                		//附件分类转换
                		attachType = attachTypeConvert(attachType);
                		//打开合同编码文件夹并判断是否有相同合同编码的文件夹
                		boolean d = FtpUtil.openDirectory(yearMonth + "/" + vsftpd.getBillId(),attachType);
                		if(d) {
                			//打开对应的附件类型文件夹,然后上传文件（若名字重复则覆盖）
                    		boolean e = FtpUtil.openDirectory(yearMonth+ "/" + vsftpd.getBillId() + "/" + attachType,vsftpd.getFileName());
                    		if(!e) {
                    			flag = FtpUtil.uploadFile(yearMonth+ "/" + vsftpd.getBillId() + "/" + attachType, vsftpd.getFileName(),vsftpd.getByteArry(),"replace");
                    		}
                    		else {
                    			String attachType1 = vsftpd.getFullPath();
                        		//附件分类转换
                        		attachType1 = attachTypeConvert(attachType1);
                            	FtpUtil.makeDirectory(yearMonth,yearMonth + "/" + vsftpd.getBillId() + "/" + attachType1,"kgjn");
                            	flag = FtpUtil.uploadFile(yearMonth + "/" + vsftpd.getBillId() + "/" + attachType1, vsftpd.getFileName(),vsftpd.getByteArry(),"replace");
                    		}
                		}
                		else {
                			String attachType1 = vsftpd.getFullPath();
                    		//附件分类转换
                    		attachType1 = attachTypeConvert(attachType1);
                        	FtpUtil.makeDirectory(yearMonth,yearMonth + "/" + vsftpd.getBillId() + "/" + attachType1,"kgjn");
                        	flag = FtpUtil.uploadFile(yearMonth + "/" + vsftpd.getBillId() + "/" + attachType1, vsftpd.getFileName(),vsftpd.getByteArry(),"replace");
                		}
                		
                	}
                	//没有合同编码文件夹时
                	else {
                		String attachType = vsftpd.getFullPath();
                		//附件分类转换
                		attachType = attachTypeConvert(attachType);
                    	FtpUtil.makeDirectory(yearMonth,yearMonth + "/" + vsftpd.getBillId() + "/" + attachType,"kgjn");
                    	flag = FtpUtil.uploadFile(yearMonth + "/" + vsftpd.getBillId() + "/" + attachType, vsftpd.getFileName(),vsftpd.getByteArry(),"replace");
                	}
                }
                else {
                	//判断是否有对应的附件分类文件夹
            		String attachType = vsftpd.getFullPath();
            		//附件分类转换
            		attachType = attachTypeConvert(attachType);
                	FtpUtil.makeDirectory("",yearMonth + "/" + vsftpd.getBillId() + "/" + attachType,"kgjn");
                	flag = FtpUtil.uploadFile(yearMonth+ "/" + vsftpd.getBillId() + "/" + attachType, vsftpd.getFileName(),vsftpd.getByteArry(),"replace");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorInfo errorInfo = new ErrorInfo("FTP.ERROR","下载失败！服务端异常！");
            errors.add(errorInfo);
        }
        if(!flag){
            ErrorInfo errorInfo = new ErrorInfo("FTP.ERROR","上传失败！系统异常！");
            errors.add(errorInfo);
        }
        if(CollectionUtils.isEmpty(errors)){
            result.setStatus(true);
        }else{
            result.setStatus(false);
            result.setErrors(errors);
        }
        LOGGER.info("FtpUploadStrategy.vsftpMethod end");
        return result;
    }
	/**
	 * 
	 * @Title : findYearMonth
	 * @Type : YearAndMonth
	 * @date : 2020年11月4日 下午
	 * @Description : 获取年月
	 * @return
	 */
	public static String findYearMonth()
	{
		/**
		 * 声明一个int变量year
		 */
		int year;
		/**
		 * 声明一个int变量month
		 */
		int month;
		/**
		 * 声明一个字符串变量date
		 */
		String date;
		/**
		 * 实例化一个对象calendar
		 */
		Calendar calendar = Calendar.getInstance();
		/**
		 * 获取年份
		 */
		year = calendar.get(Calendar.YEAR);
		/**
		 * 获取月份
		 */
		month = calendar.get(Calendar.MONTH) + 1;
		/**
		 * 拼接年份和月份
		 */
		date = year + "" + ( month<10 ? "0" + month : month);
		/**
		 * 返回当前年月
		 */
		return date;
	}
	public static String attachTypeConvert(String attachType) {
		if (attachType.contains("合同正文")) {
			attachType = "zw";
		}
		if (attachType.contains("合同审批单")) {
			attachType = "spattach";
		}
		if (attachType.contains("我方授权委托书")) {
			attachType = "wsattach";
		}
		if (attachType.contains("对方授权委托书")) {
			attachType = "dsattach";
		}
		if (attachType.contains("合同签署文本")) {
			attachType = "qsattach";
		}
		if (attachType.contains("中标通知书")) {
			attachType = "zbattach";
		}
		if (attachType.contains("其它")) {
			attachType = "otherattach";
		}  
		return attachType;
	}
}
