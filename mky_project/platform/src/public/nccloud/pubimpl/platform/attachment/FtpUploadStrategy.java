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
 * �ļ��ϴ�
 * ͨ������vsftpd.getOptionType()��Ϊ��ͨ�ϴ� �� �����ϴ�����
 * ��һ�����ļ��Ѵ����򷵻ش�����Ϣ��ʾ�ļ��Ѵ���
 * �ڶ�����ֱ�Ӹ���
 */
public class FtpUploadStrategy {

    private static Logger LOGGER = LoggerFactory.getLogger(FtpUploadStrategy.class);

    public VsftpResult vsftpMethod(Vsftpd vsftpd){
        LOGGER.info("FtpUploadStrategy.vsftpMethod start");
        VsftpResult result = new VsftpResult();
        List<ErrorInfo> errors = new ArrayList<>();
        if (StringUtils.isEmpty(vsftpd.getFileName())) {
            ErrorInfo errorInfo = new ErrorInfo("PARAMETER.FAIL","����[fileName]����Ϊ�գ�");
            errors.add(errorInfo);
        }
        if (vsftpd.getByteArry()==null) {
            ErrorInfo errorInfo = new ErrorInfo("PARAMETER.FAIL","����[byteArry]����Ϊ�գ�");
            errors.add(errorInfo);
        }
        //����ǿ���ϴ���ʱ��   �ļ����Ѵ������ϴ�ʧ��
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
                //�ж������ļ����Ƿ����
                boolean b = FtpUtil.fileExist("kgjn", yearMonth);
                
                vsftpd.setBillId(map.get("vbillcode"));
                if (b) {
//                    ErrorInfo errorInfo = new ErrorInfo("PARAMETER.FAIL", "�ļ�[" + vsftpd.getFileName() + "]�Ѵ��ڣ�");
//                    errors.add(errorInfo);
                	//�������ļ���
                	boolean c = FtpUtil.openDirectory(yearMonth,vsftpd.getBillId());
                	//�ж��Ƿ���ں�ͬ����ļ���
                	//�к�ͬ�����ļ���ʱ
                	if(c) {
                		//�ж��Ƿ��ж�Ӧ�ĸ��������ļ���
                		String attachType = vsftpd.getFullPath();
                		//��������ת��
                		attachType = attachTypeConvert(attachType);
                		//�򿪺�ͬ�����ļ��в��ж��Ƿ�����ͬ��ͬ������ļ���
                		boolean d = FtpUtil.openDirectory(yearMonth + "/" + vsftpd.getBillId(),attachType);
                		if(d) {
                			//�򿪶�Ӧ�ĸ��������ļ���,Ȼ���ϴ��ļ����������ظ��򸲸ǣ�
                    		boolean e = FtpUtil.openDirectory(yearMonth+ "/" + vsftpd.getBillId() + "/" + attachType,vsftpd.getFileName());
                    		if(!e) {
                    			flag = FtpUtil.uploadFile(yearMonth+ "/" + vsftpd.getBillId() + "/" + attachType, vsftpd.getFileName(),vsftpd.getByteArry(),"replace");
                    		}
                    		else {
                    			String attachType1 = vsftpd.getFullPath();
                        		//��������ת��
                        		attachType1 = attachTypeConvert(attachType1);
                            	FtpUtil.makeDirectory(yearMonth,yearMonth + "/" + vsftpd.getBillId() + "/" + attachType1,"kgjn");
                            	flag = FtpUtil.uploadFile(yearMonth + "/" + vsftpd.getBillId() + "/" + attachType1, vsftpd.getFileName(),vsftpd.getByteArry(),"replace");
                    		}
                		}
                		else {
                			String attachType1 = vsftpd.getFullPath();
                    		//��������ת��
                    		attachType1 = attachTypeConvert(attachType1);
                        	FtpUtil.makeDirectory(yearMonth,yearMonth + "/" + vsftpd.getBillId() + "/" + attachType1,"kgjn");
                        	flag = FtpUtil.uploadFile(yearMonth + "/" + vsftpd.getBillId() + "/" + attachType1, vsftpd.getFileName(),vsftpd.getByteArry(),"replace");
                		}
                		
                	}
                	//û�к�ͬ�����ļ���ʱ
                	else {
                		String attachType = vsftpd.getFullPath();
                		//��������ת��
                		attachType = attachTypeConvert(attachType);
                    	FtpUtil.makeDirectory(yearMonth,yearMonth + "/" + vsftpd.getBillId() + "/" + attachType,"kgjn");
                    	flag = FtpUtil.uploadFile(yearMonth + "/" + vsftpd.getBillId() + "/" + attachType, vsftpd.getFileName(),vsftpd.getByteArry(),"replace");
                	}
                }
                else {
                	//�ж��Ƿ��ж�Ӧ�ĸ��������ļ���
            		String attachType = vsftpd.getFullPath();
            		//��������ת��
            		attachType = attachTypeConvert(attachType);
                	FtpUtil.makeDirectory("",yearMonth + "/" + vsftpd.getBillId() + "/" + attachType,"kgjn");
                	flag = FtpUtil.uploadFile(yearMonth+ "/" + vsftpd.getBillId() + "/" + attachType, vsftpd.getFileName(),vsftpd.getByteArry(),"replace");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorInfo errorInfo = new ErrorInfo("FTP.ERROR","����ʧ�ܣ�������쳣��");
            errors.add(errorInfo);
        }
        if(!flag){
            ErrorInfo errorInfo = new ErrorInfo("FTP.ERROR","�ϴ�ʧ�ܣ�ϵͳ�쳣��");
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
	 * @date : 2020��11��4�� ����
	 * @Description : ��ȡ����
	 * @return
	 */
	public static String findYearMonth()
	{
		/**
		 * ����һ��int����year
		 */
		int year;
		/**
		 * ����һ��int����month
		 */
		int month;
		/**
		 * ����һ���ַ�������date
		 */
		String date;
		/**
		 * ʵ����һ������calendar
		 */
		Calendar calendar = Calendar.getInstance();
		/**
		 * ��ȡ���
		 */
		year = calendar.get(Calendar.YEAR);
		/**
		 * ��ȡ�·�
		 */
		month = calendar.get(Calendar.MONTH) + 1;
		/**
		 * ƴ����ݺ��·�
		 */
		date = year + "" + ( month<10 ? "0" + month : month);
		/**
		 * ���ص�ǰ����
		 */
		return date;
	}
	public static String attachTypeConvert(String attachType) {
		if (attachType.contains("��ͬ����")) {
			attachType = "zw";
		}
		if (attachType.contains("��ͬ������")) {
			attachType = "spattach";
		}
		if (attachType.contains("�ҷ���Ȩί����")) {
			attachType = "wsattach";
		}
		if (attachType.contains("�Է���Ȩί����")) {
			attachType = "dsattach";
		}
		if (attachType.contains("��ͬǩ���ı�")) {
			attachType = "qsattach";
		}
		if (attachType.contains("�б�֪ͨ��")) {
			attachType = "zbattach";
		}
		if (attachType.contains("����")) {
			attachType = "otherattach";
		}  
		return attachType;
	}
}
