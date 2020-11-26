package nccloud.pubimpl.platform.attachment;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nccloud.commons.collections.CollectionUtils;
import nccloud.web.platform.attachment.vo.ErrorInfo;
import nccloud.web.platform.attachment.vo.VsftpResult;
import nccloud.web.platform.attachment.vo.Vsftpd;

/**
 * ftp���񣬶����ṩͳһ�ӿڵ�ַ��ͨ�����ƴ��ݵĲ���ʵ�� �ϴ��������ϴ�����ȡ�ļ��б������4������
 * ��������ο���Vsftpd.java, FtpConstants��
 */
//@Controller
//@RequestMapping("/vsftpdService")
public class FtpController {

    private static Logger LOGGER = LoggerFactory.getLogger(FtpController.class);

//    @ResponseBody
//    @RequestMapping(path = "/vsftpd", method = RequestMethod.POST)
	public VsftpResult getAuthInfo(Vsftpd vsftpd){
        LOGGER.info("ftpController.getAuthInfo start");
        FtpUploadStrategy strategy = null;
        List<ErrorInfo> errors =  new ArrayList<>();
        VsftpResult result = new VsftpResult();
        //��һ��У������Ƿ�Ϸ�
        if (StringUtils.isEmpty(vsftpd.getOptionType())) {
            ErrorInfo errorInfo = new ErrorInfo("PARAMETER.FAIL","���ò���[type]����Ϊ�գ�");
            errors.add(errorInfo);
        }
        if (StringUtils.isEmpty(vsftpd.getProjectCode())) {
            ErrorInfo errorInfo = new ErrorInfo("PARAMETER.FAIL","����[projectCode]����Ϊ�գ�");
            errors.add(errorInfo);
        }
        //������������ʹ�ò�ͬ����
        if(FtpConstants.UP_LOAD.equals(vsftpd.getOptionType())){
            strategy = new FtpUploadStrategy();
        }
//        else if(FtpConstants.REPLACE.equals(vsftpd.getOptionType())){
//            strategy = new FtpUploadStrategy();
//        }
//        else if(FtpConstants.DOWAN_LOAD.equals(vsftpd.getOptionType())){
//            strategy = new FtpDownLoadStrategy();
//        }else if(FtpConstants.DISPLAY.equals(vsftpd.getOptionType())){
//            strategy = new FtpDisplayStrategy();
//        }else if(FtpConstants.DELETE.equals(vsftpd.getOptionType())){
//            strategy = new FtpDeleteStrategy();
//        }
        else {
            ErrorInfo errorInfo = new ErrorInfo("PARAMETER.FAIL","���ò���[type]����");
            errors.add(errorInfo);
        }
        if (CollectionUtils.isEmpty(errors)) {
            result = strategy.vsftpMethod(vsftpd);
        }else{
            result.setStatus(false);
            result.setErrors(errors);
        }
        return result;
    }
}