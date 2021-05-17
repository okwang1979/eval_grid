package nccloud.pubimpl.platform.attachment;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nc.bs.framework.common.RuntimeEnv;

public class FtpUtil {
    private static Logger LOGGER = LoggerFactory.getLogger(FtpUtil.class);
    private static String LOCAL_CHARSET = "UTF-8";
    private static String SERVER_CHARSET = "ISO-8859-1";
    private static String host;
    private static String port;
    private static String username;
    private static String password;
    private static String basePath;
    private static String filePath;
    private static String localPath;

    /**
     *��ȡ�����ļ���Ϣ
     * @return
     */
    public static  void getPropertity(){
        Properties properties = new Properties();
        ClassLoader load = FtpUtil.class.getClassLoader();
        String nchome = RuntimeEnv.getInstance().getCanonicalNCHome();
        InputStream in = null;
        String path = nchome + File.separator + "resources"+ File.separator + "kgjn" + File.separator  + "ftpinfo.properties";
			
//        String str = getString(path);
//        InputStream   is   =   new   ByteArrayInputStream(str.getBytes());
        //InputStream is = load.getResourceAsStream("conf/vsftpd.properties");
        try {
        	FileInputStream is = new FileInputStream(path);
            properties.load(is);
            host=properties.getProperty("ftpinfo.ip");
            port=properties.getProperty("ftpinfo.port");
            username=properties.getProperty("ftpinfo.user");
            password=properties.getProperty("ftpinfo.pwd");
            //�������� ��·��
            basePath=properties.getProperty("ftpinfo.remote.base.path");
            //�������� �ļ�·��
            filePath=properties.getProperty("ftpinfo.remote.file.path");
            //���� ���ص����ص�Ŀ¼
            localPath=properties.getProperty("ftpinfo.local.file.path");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
	 * ��ȡ·�����ļ��е��ַ�wenjian
	 * @param fileName
	 */
	public static String getString(String fileName) {
        StringBuffer sb = new StringBuffer();
        FileReader fr = null;
        try {
            fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
              * �ϴ�����
     * @param filename  �ϴ����������˿�������
     * @param buffer    byte[] �ļ���
     * @return
     */
    public static boolean uploadFile(String filename, InputStream buffer, String optionType) throws Exception{
        getPropertity();
       return uploadFile(filePath,  filename,  buffer, optionType);
    }

    /**
             * ��ȡ�ļ��б�
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String[] displayFile(String filePath) throws Exception{
        getPropertity();
       return displayFile(host,  port,  username,  password,  basePath, filePath);
    }

    /**
             * ɾ���ļ�
     * @param filePath
     * @return
     */
    public static boolean deleteFile(String filePath, String fileName) throws Exception{
        getPropertity();
       return deleteFile(host,  port,  username,  password,  basePath, filePath, fileName);
    }

    /**
             * �ж��ļ��Ƿ����
     * @param filePath
     * @return
     * @throws Exception
     */
    public static boolean fileExist(String filePath,String filename) throws Exception{
        if(StringUtils.isEmpty(filename)){
            return false;
        }
        getPropertity();
        String[] names =  displayFile(filePath);
        for (String name : names) {
            if(filename.equals(name)){
                return true;
            }
        }
        return false;
    }
    /**
     * ��Ŀ¼
     * @param fileName 
	 * @param filePath
	 * @return
	 * @throws Exception
	*/
	public static boolean openDirectory(String file, String fileName) throws Exception{
		try {
			getPropertity();
			FTPClient ftp = new FTPClient();
			int portNum = Integer.parseInt(port);
			int reply;
			// ����FTP������
			ftp.connect(host, portNum);
			// �������Ĭ�϶˿ڣ�����ʹ��ftp.connect(host)�ķ�ʽֱ������FTP������
			ftp.login(username, password);
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			}
			// ������������UTF-8��֧�֣����������֧�־���UTF-8���룬�����ʹ�ñ��ر��루GBK��.
			if (FTPReply.isPositiveCompletion(ftp.sendCommand("OPTS UTF8", "ON"))) {
			LOCAL_CHARSET = "UTF-8";
			}
			ftp.setControlEncoding(LOCAL_CHARSET);
			//�л����ϴ�Ŀ¼
			///* basePath+ "home\\document\\kgjn\\" + */
			//���ô��䷽ʽΪ����ʽ
            ftp.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
			boolean changeWorkingDirectory = ftp.changeWorkingDirectory(file);
			// ��ȡ��ǰ����Ŀ¼
			String pwd = ftp.printWorkingDirectory();
			System.out.println(pwd);
			FTPFile[] listFiles = ftp.listFiles();
			
			for (FTPFile FTPFile : listFiles) {
				String ii = "";;
	            if(fileName.equals(FTPFile.getName())){
	                return true;
	            }
	        }
			String ii = "";
		} catch (IOException e) {
			LOGGER.info(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return false;
	}
    /**
             *��������
     * @param filePath  Ҫ���ص��ļ����ڷ����������·��
     * @param fileName  Ҫ���ص��ļ���
     * @return
     */
    public static byte[] downloadFile(String filePath,String fileName) throws Exception{
        getPropertity();
        return downloadFile( host,  port,  username,  password,  basePath,
                filePath, fileName);
    }


    /**
     * Description: ��FTP�������ϴ��ļ�
     * @param host FTP������hostname
     * @param port FTP�������˿�
     * @param username FTP��¼�˺�
     * @param password FTP��¼����
     * @param basePath FTP����������Ŀ¼
     * @param filePath FTP�������ļ����·������������ڴ�ţ�/2015/01/01���ļ���·��ΪbasePath+filePath
     * @param fileName �ϴ���FTP�������ϵ��ļ���
     * @return �ɹ�����true�����򷵻�false
     */
    public static boolean uploadFile(String filePath, String fileName, InputStream buffer, String optionType)  throws Exception{
        FTPClient ftp = new FTPClient();
        getPropertity();
        try {
            fileName = new String(fileName);
            boolean result = connectFtp(ftp, host, port, username, password, basePath, "kgjn");
            if(!result){
                return result;
            }
            //Ϊ�˼Ӵ��ϴ��ļ��ٶȣ���InputStreamת��BufferInputStream  , InputStream input
            InputStream inputStream  = buffer;
            //�Ӵ󻺴���
            ftp.setBufferSize(1024*1024);
            //�����ϴ��ļ�������Ϊ����������
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            boolean changeWorkingDirectory = ftp.changeWorkingDirectory(filePath);
            
                //���Ŀ¼�����ڴ���Ŀ¼
//	            String[] dirs = filePath.split("\\");
//	            String tempPath = basePath;
//	            for (String dir : dirs) {
//	              if (null == dir || "".equals(dir)) {
//	                  continue;
//	              }
//	              tempPath += "\\" + dir;
//	              if (!ftp.changeWorkingDirectory(tempPath)) {
//	                  if (!ftp.makeDirectory(tempPath)) {
//	                      return result;
//	                  } else {
//	                      ftp.changeWorkingDirectory(tempPath);
//	                  }
//	              }
//	            }
//          }
            if(FtpConstants.REPLACE.equals(optionType)){
                ftp.deleteFile(fileName);
            }
            String printWorkingDirectory = ftp.printWorkingDirectory();
            System.out.println(printWorkingDirectory);
            //�ϴ��ļ�
            ftp.enterLocalPassiveMode();
            String  code = "GBK";
            if (!ftp.storeFile(new String(fileName.getBytes(code),"iso-8859-1"),inputStream)){
                return false;
            }
            inputStream.close();
            ftp.logout();
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return true;
    }
    /** 
     * �ڷ������ϴ���һ���ļ��� 
     * 
     * @param dir 
     *            �ļ������ƣ����ܺ��������ַ����� \ ��/ ��: ��* ��?�� "�� <��>... 
     */  
    public static boolean makeDirectory(String yearMonth,String dir,String filePath) {  
        boolean flag = true;  
        getPropertity();
        try {  
        	FTPClient ftp = new FTPClient();
        	boolean result = connectFtp(ftp, host, port, username, password, basePath, filePath);
            if(!result){
                return result;
            }
//            if(!"".equals(yearMonth)) {
//            	boolean changeWorkingDirectory = ftp.changeWorkingDirectory(yearMonth + "/" + "/");
//            	System.out.println(changeWorkingDirectory);
//            }
			//��������Ŀ¼
//			if(ftp.changeWorkingDirectory(d))
//				return true;
//			dir = StringExtend.trimStart(dir, "/");
//			dir = StringExtend.trimEnd(dir, "/");
            dir = "/home/document/kgjn/"  + dir;
			String[] arr =  dir.split("/");
			StringBuffer sbfDir=new StringBuffer();
			String printWorkingDirectory = ftp.printWorkingDirectory();
			System.out.println(printWorkingDirectory);
			//ѭ��������Ŀ¼
			for(String s : arr){
				sbfDir.append("/");
				sbfDir.append(s);
				//Ŀ¼���룬�������·������
				dir = new String(sbfDir.toString().getBytes("GBK"),"iso-8859-1");
				//��������Ŀ¼
				if(ftp.changeWorkingDirectory(dir)) {
					continue;
				}
				if(!ftp.makeDirectory(dir)){
					System.out.println("[ʧ��]ftp����Ŀ¼��"+sbfDir.toString());
					return false;
				}
				System.out.println("[�ɹ�]����ftpĿ¼��"+sbfDir.toString());
			}
			
//            flag = ftp.makeDirectory(dir);  
            if (flag) {  
                System.out.println("make Directory " +dir +" succeed");  
                boolean changeWorkingDirectory = ftp.changeWorkingDirectory(dir);
                System.out.println(ftp.printWorkingDirectory());
            } else {  
  
                System.out.println("make Directory " +dir+ " false");  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return flag;  
    }  

    /**
     * Description: ��FTP�����������ļ�
     * @param host FTP������hostname
     * @param port FTP�������˿�
     * @param username FTP��¼�˺�
     * @param password FTP��¼����
     * @param basePath FTP�������ϵ����·��
     * @param fileName Ҫ���ص��ļ���
     * @return
     */
    public static byte[] downloadFile(String host, String port, String username, String password, String basePath,
                                       String filePath, String fileName)  throws Exception{
        FTPSClient ftp = new FTPSClient();
        try {
            fileName = new String(fileName.getBytes(LOCAL_CHARSET));
            boolean result = connectFtp(ftp, host, port, username, password, basePath, filePath);
            if(!result){
                return null;
            }
            FTPFile[] fs = ftp.listFiles();
            boolean flag = true;
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    InputStream input = ftp.retrieveFileStream(ff.getName());
                    BufferedInputStream in = new BufferedInputStream(input);
                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while((len = in.read(buffer)) != -1){
                        outStream.write(buffer, 0, len);
                    }
                    outStream.close();
                    in.close();
                    byte[] arryArry = outStream.toByteArray();
                    return arryArry;
                }
            }
            if(flag) {
                LOGGER.info("���������ļ�������...");
                return null;
            }
            ftp.logout();
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return null;
    }

    /**
             * ��ȡ�������ļ����б�
     * @param host
     * @param port
     * @param username
     * @param password
     * @param basePath
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String[] displayFile(String host, String port, String username, String password, String basePath,
                                        String filePath) throws Exception{
        FTPClient ftp = new FTPClient();
        try {
    		boolean result = connectFtp(ftp, host, port, username, password, basePath, filePath);
    		if(!result){
    		return null;
        }
        	
            String[] names = ftp.listNames();
            ftp.logout();
           return names;
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
    }

    /**
             * ɾ���ļ�
     * @param host
     * @param port
     * @param username
     * @param password
     * @param basePath
     * @param filePath
     * @return
     */
    public static boolean deleteFile(String host, String port, String username, String password, String basePath,
                                     String filePath,String fileName) throws Exception{
        FTPSClient ftp = new FTPSClient();
        boolean b = false;
        try {
            boolean result = connectFtp(ftp, host, port, username, password, basePath, filePath);
            if(!result){
                return b;
            }
            b = ftp.deleteFile(fileName);
            ftp.logout();
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return b;
    }

    /**
             * ����ftp���������л���Ŀ��Ŀ¼
             * ���ô˷������ֶ��ر�ftp����
     * @param ftp
     * @param host
     * @param port
     * @param username
     * @param password
     * @param basePath
     * @param filePath
     * @return
     */
    private static boolean connectFtp( FTPClient ftp,String host, String port, String username,
                                       String password, String basePath, String filePath) throws Exception{
        boolean result = false;
        try {
            int portNum = Integer.parseInt(port);
            int reply;
            // ����FTP������
            ftp.connect(host, portNum);
            // �������Ĭ�϶˿ڣ�����ʹ��ftp.connect(host)�ķ�ʽֱ������FTP������
            ftp.login(username, password);
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return result;
            }
            else {
            	result = true;
            }
            // ������������UTF-8��֧�֣����������֧�־���UTF-8���룬�����ʹ�ñ��ر��루GBK��.
            if (FTPReply.isPositiveCompletion(ftp.sendCommand("OPTS UTF8", "ON"))) {
                LOCAL_CHARSET = "UTF-8";
            }
            ftp.setControlEncoding(LOCAL_CHARSET);
            //�л����ϴ�Ŀ¼
//            basePath = "E:/";
//            filePath = "home/document/kgjn/202011";
            //���ô��䷽ʽΪ����ʽ
            ftp.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
//            boolean changeWorkingDirectory = ftp.changeWorkingDirectory("../");
//            boolean changeWorkingDirectory1 = ftp.changeWorkingDirectory("202011");
            if(!"kgjn".equals(filePath)) {
            	boolean changeWorkingDirectory = ftp.changeWorkingDirectory(filePath);
                result = changeWorkingDirectory;
            }
            
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return result;
    }
}