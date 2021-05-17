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
     *读取配置文件信息
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
            //服务器端 基路径
            basePath=properties.getProperty("ftpinfo.remote.base.path");
            //服务器端 文件路径
            filePath=properties.getProperty("ftpinfo.remote.file.path");
            //本地 下载到本地的目录
            localPath=properties.getProperty("ftpinfo.local.file.path");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
	 * 获取路径下文件中的字符wenjian
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
              * 上传重载
     * @param filename  上传到服务器端口重命名
     * @param buffer    byte[] 文件流
     * @return
     */
    public static boolean uploadFile(String filename, InputStream buffer, String optionType) throws Exception{
        getPropertity();
       return uploadFile(filePath,  filename,  buffer, optionType);
    }

    /**
             * 获取文件列表
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String[] displayFile(String filePath) throws Exception{
        getPropertity();
       return displayFile(host,  port,  username,  password,  basePath, filePath);
    }

    /**
             * 删除文件
     * @param filePath
     * @return
     */
    public static boolean deleteFile(String filePath, String fileName) throws Exception{
        getPropertity();
       return deleteFile(host,  port,  username,  password,  basePath, filePath, fileName);
    }

    /**
             * 判断文件是否存在
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
     * 打开目录
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
			// 连接FTP服务器
			ftp.connect(host, portNum);
			// 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
			ftp.login(username, password);
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			}
			// 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
			if (FTPReply.isPositiveCompletion(ftp.sendCommand("OPTS UTF8", "ON"))) {
			LOCAL_CHARSET = "UTF-8";
			}
			ftp.setControlEncoding(LOCAL_CHARSET);
			//切换到上传目录
			///* basePath+ "home\\document\\kgjn\\" + */
			//设置传输方式为流方式
            ftp.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
			boolean changeWorkingDirectory = ftp.changeWorkingDirectory(file);
			// 获取当前工作目录
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
             *下载重载
     * @param filePath  要下载的文件所在服务器的相对路径
     * @param fileName  要下载的文件名
     * @return
     */
    public static byte[] downloadFile(String filePath,String fileName) throws Exception{
        getPropertity();
        return downloadFile( host,  port,  username,  password,  basePath,
                filePath, fileName);
    }


    /**
     * Description: 向FTP服务器上传文件
     * @param host FTP服务器hostname
     * @param port FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param basePath FTP服务器基础目录
     * @param filePath FTP服务器文件存放路径。例如分日期存放：/2015/01/01。文件的路径为basePath+filePath
     * @param fileName 上传到FTP服务器上的文件名
     * @return 成功返回true，否则返回false
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
            //为了加大上传文件速度，将InputStream转成BufferInputStream  , InputStream input
            InputStream inputStream  = buffer;
            //加大缓存区
            ftp.setBufferSize(1024*1024);
            //设置上传文件的类型为二进制类型
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            boolean changeWorkingDirectory = ftp.changeWorkingDirectory(filePath);
            
                //如果目录不存在创建目录
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
            //上传文件
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
     * 在服务器上创建一个文件夹 
     * 
     * @param dir 
     *            文件夹名称，不能含有特殊字符，如 \ 、/ 、: 、* 、?、 "、 <、>... 
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
			//尝试切入目录
//			if(ftp.changeWorkingDirectory(d))
//				return true;
//			dir = StringExtend.trimStart(dir, "/");
//			dir = StringExtend.trimEnd(dir, "/");
            dir = "/home/document/kgjn/"  + dir;
			String[] arr =  dir.split("/");
			StringBuffer sbfDir=new StringBuffer();
			String printWorkingDirectory = ftp.printWorkingDirectory();
			System.out.println(printWorkingDirectory);
			//循环生成子目录
			for(String s : arr){
				sbfDir.append("/");
				sbfDir.append(s);
				//目录编码，解决中文路径问题
				dir = new String(sbfDir.toString().getBytes("GBK"),"iso-8859-1");
				//尝试切入目录
				if(ftp.changeWorkingDirectory(dir)) {
					continue;
				}
				if(!ftp.makeDirectory(dir)){
					System.out.println("[失败]ftp创建目录："+sbfDir.toString());
					return false;
				}
				System.out.println("[成功]创建ftp目录："+sbfDir.toString());
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
     * Description: 从FTP服务器下载文件
     * @param host FTP服务器hostname
     * @param port FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param basePath FTP服务器上的相对路径
     * @param fileName 要下载的文件名
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
                LOGGER.info("服务器端文件不存在...");
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
             * 获取服务器文件名列表
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
             * 删除文件
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
             * 连接ftp服务器并切换到目的目录
             * 调用此方法需手动关闭ftp连接
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
            // 连接FTP服务器
            ftp.connect(host, portNum);
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return result;
            }
            else {
            	result = true;
            }
            // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
            if (FTPReply.isPositiveCompletion(ftp.sendCommand("OPTS UTF8", "ON"))) {
                LOCAL_CHARSET = "UTF-8";
            }
            ftp.setControlEncoding(LOCAL_CHARSET);
            //切换到上传目录
//            basePath = "E:/";
//            filePath = "home/document/kgjn/202011";
            //设置传输方式为流方式
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