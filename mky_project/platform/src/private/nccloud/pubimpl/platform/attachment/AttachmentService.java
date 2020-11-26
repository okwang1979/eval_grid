package nccloud.pubimpl.platform.attachment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.pub.filesystem.IFileSystemService;
import nc.lightapp.framework.web.action.attachment.AttachmentVO;
import nc.pubitf.para.SysInitQuery;
import nc.vo.arap.receivable.ReceivableBillVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.filesystem.FileTypeConst;
import nc.vo.pub.filesystem.NCFileNode;
import nc.vo.pub.filesystem.NCFileVO;
import nc.vo.pubapp.AppContext;
import nccloud.base.exception.ExceptionUtils;
import nccloud.pubitf.platform.attachment.IAttachmentService;
import nccloud.pubitf.platform.attachment.tool.HttpUtils;
import nccloud.pubitf.platform.attachment.tool.StringUtils;
import nccloud.pubitf.platform.attachment.tool.UfsHostURI;
import nccloud.web.platform.attachment.vo.Vsftpd;
import uap.pub.fs.domain.basic.FileHeader;
import uap.pub.fs.prop.service.IReadPropService;

public class AttachmentService implements IAttachmentService {
	public void deleteAttachDBInfo(String[] nodePaths) {
		IFileSystemService service = (IFileSystemService) NCLocator.getInstance().lookup(IFileSystemService.class);

		try {
			service.deleteNCFileNodes(nodePaths);
		} catch (BusinessException var4) {
			ExceptionUtils.wrapBusinessException(var4.getMessage());
		}

	}

	public InputStream download(String isdoc, String pk_doc, String bucket) {
		if (FileTypeConst.getAttachMentStoreType(isdoc).equals("v63storetype")) {
			ExceptionUtils.unSupported();
		} else {
			if (FileTypeConst.getAttachMentStoreType(isdoc).equals("v65filesystem")) {
				InputStream input = this.downloadFile(pk_doc, bucket);
				return input;
			}

			ExceptionUtils.unSupported();
		}

		return null;
	}

	public long getFileLimit() {
		long size = 0L;

		try {
			Integer limit = SysInitQuery.getParaInt("GLOBLE00000000000000", "AttachLmt");
			size = (long) limit * 1024L * 1024L;
		} catch (BusinessException var4) {
			ExceptionUtils.wrapBusinessException(var4.getMessage());
		}

		return size;
	}

	public HashMap<String, String> getFtpTransferConfig() {
		IReadPropService service = (IReadPropService) NCLocator.getInstance().lookup(IReadPropService.class);
		HashMap<String, String> config = service.getFtpTransferConfig();
		return config;
	}

	public HashMap<String, String> getFtpWlanConfig() {
		IReadPropService service = (IReadPropService) NCLocator.getInstance().lookup(IReadPropService.class);
		HashMap<String, String> config = service.getFtpWlanConfig();
		return config;
	}

	public HashMap<String, String> getRestTransferConfig() {
		IReadPropService service = (IReadPropService) NCLocator.getInstance().lookup(IReadPropService.class);
		HashMap<String, String> config = service.getRestTransferConfig();
		return config;
	}

	public HashMap<String, String> getWlanConfig() {
		IReadPropService service = (IReadPropService) NCLocator.getInstance().lookup(IReadPropService.class);
		HashMap<String, String> config = service.getWlanConfig();
		return config;
	}

	public NCFileVO[] queryNCFilesByFullPaths(String[] fullPaths) {
		FileSpaceDAOForNCC dao = new FileSpaceDAOForNCC();
		NCFileVO[] vos = dao.queryNCFilesByFullPaths(fullPaths);
		return vos;
	}

	public NCFileVO[] queryNCFilesByNodePath(String nodePath, String creator) {
		FileSpaceDAOForNCC dao = new FileSpaceDAOForNCC();
		NCFileVO[] vos = dao.queryFileVOsByPath(nodePath);
		return vos;
	}

	public String readConf(String key) {
		IReadPropService service = (IReadPropService) NCLocator.getInstance().lookup(IReadPropService.class);
		String config = service.readConfProp(key);
		return config;
	}

	public void remove(String pk_doc, String fullPath, String bucket) {
		this.removeFileFromServer(pk_doc, bucket);
	}

	public NCFileNode saveAttachDBInfo(String fullPath, NCFileVO filevo) {
		IFileSystemService service = (IFileSystemService) NCLocator.getInstance().lookup(IFileSystemService.class);
		NCFileNode fileNode = null;

		try {
			fileNode = service.createCloudFileNode(fullPath, AppContext.getInstance().getPkUser(), filevo);
		} catch (BusinessException var6) {
			ExceptionUtils.wrapBusinessException(var6.getMessage());
		}

		return fileNode;
	}

	public FileHeader upload(String fileName, InputStream inStream, boolean override, int uploadMode, String bucket,String billId,String fullPath,String appCode) {
		FileHeader header = this.uploadToServer(fileName, inStream, override, uploadMode, bucket,billId,fullPath,appCode);
		return header;
	}

	private InputStream downloadFile(String filePath, String bucket) {
		InputStream inStream = null;
		HttpURLConnection conn = null;

		try {
			String url = UfsHostURI.getRestfulURI(bucket) + "/" + filePath + "?versionno=0";
			conn = HttpUtils.getConnectObject(url);
			conn.setRequestMethod("GET");
			HttpUtils.connect(conn, url);
			inStream = conn.getInputStream();
		} catch (ProtocolException var6) {
			ExceptionUtils.wrapBusinessException(
					NCLangRes4VoTransl.getNCLangRes().getStrByID("1501002_0", "01501002-0305"), var6);
		} catch (IOException var7) {
			ExceptionUtils
					.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501002_0", "01501002-0306")
							+ NCLangRes4VoTransl.getNCLangRes().getStrByID("1501003_0", "01501003-0130") + bucket
							+ NCLangRes4VoTransl.getNCLangRes().getStrByID("1501003_0", "01501003-0131")
							+ UfsHostURI.getRestfulURI(bucket) + "/" + filePath + "?versionno=0", var7);
		}

		return inStream;
	}

	private void removeFileFromServer(String filePath, String bucket) {
		HttpURLConnection conn = null;

		try {
			String url = UfsHostURI.getRestfulURI(bucket) + "/" + filePath + "?versionno=" + 0;
			conn = HttpUtils.getConnectObject(url);
			conn.setRequestMethod("DELETE");
			HttpUtils.connect(conn, url);
			if (conn.getResponseCode() != 200 && conn.getResponseCode() != 500) {
				ExceptionUtils.wrapBusinessException("delete file error: responsecode=" + conn.getResponseCode());
			}
		} catch (ProtocolException var9) {
			ExceptionUtils.wrapException(var9);
		} catch (IOException var10) {
			ExceptionUtils.wrapException(var10);
		} finally {
			HttpUtils.closeConnection(conn);
		}

	}

	private FileHeader uploadToServer(String fileName, InputStream inStream, boolean override, int uploadMode,
			String bucket, String billId,String fullPath,String appCode) {
		if("400600200".equals(appCode) || "400400604".equals(appCode)) {
		  FtpController ftp = new FtpController();
		  Vsftpd vsftp = new Vsftpd();
		  vsftp.setFileName(fileName);
		  vsftp.setOptionType("upload");
		  vsftp.setByteArry(inStream);
		  vsftp.setProjectCode("home\\document\\kgjn");
		  vsftp.setBillId(billId);
		  vsftp.setFullPath(fullPath);
		  ftp.getAuthInfo(vsftp);
		}
		if("20060RBM".equals(appCode)) {
			
			  FtpController ftp = new FtpController();
			  Vsftpd vsftp = new Vsftpd();
			  vsftp.setFileName(fileName);
			  vsftp.setOptionType("upload");
			  vsftp.setByteArry(inStream);
			  vsftp.setProjectCode("home\\document\\kgjn");
			  
			  BaseDAO dao = new BaseDAO();
			  try {
				ReceivableBillVO  vo = (ReceivableBillVO)dao.retrieveByPK(ReceivableBillVO.class, billId);
				String  ctSale  = vo.getDef1();
				  //合同id
				  vsftp.setBillId(ctSale);
				  //合同id/应收单id
				  vsftp.setFullPath(billId);
			} catch (DAOException e) {
				throw new BusinessRuntimeException("查询应收单失败,应收单pk:"+billId+"!");
			}

			  ftp.getAuthInfo(vsftp);
			 
			
		}
		this.validateFileName(fileName);
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		HttpURLConnection conn = null;

		FileHeader var15;
		try {
			String url = UfsHostURI.getRestfulURI(bucket);
			conn = HttpUtils.getConnectObject(url);
			conn.setRequestMethod("POST");
			HttpUtils.connect(conn, url);
			byte[] buf = new byte[63];
			int len = -1;
			oos = new ObjectOutputStream(conn.getOutputStream());
			HashMap<String, Object> infoMap = new HashMap();
			String userId = AppContext.getInstance().getPkUser();
			infoMap.put("curruser", userId);
			infoMap.put("fileName", fileName);
			infoMap.put("override", String.valueOf(override));
			infoMap.put("operated", uploadMode);
			oos.writeObject(infoMap);
			if(inStream instanceof FileInputStream) {
				
				
//				import java.io.FileInputStream;
//				import java.io.FileOutputStream;
//				import java.io.IOException;
//				import java.lang.reflect.Field;
//				import java.lang.reflect.InvocationTargetException;
//				import java.lang.reflect.Method;
				
				try {
		
			        if (inStream.read() == -1) {
				        Class<? extends FileInputStream> inputStreamClass = ((FileInputStream)inStream).getClass();
				        Field fd = inputStreamClass.getDeclaredField("fd");
				        fd.setAccessible(true);
				        Object o = fd.get(inStream);
				        
				        
				        Field path = inputStreamClass.getDeclaredField("path");
				        path.setAccessible(true);
				        Object pathValue = path.get(inStream);
				        
				        System.out.println(o.hashCode());

			            Method open0 = inputStreamClass.getDeclaredMethod("open0", String.class);
			            open0.setAccessible(true);
			            open0.invoke(inStream, pathValue);
			        }
				}catch(Exception ex) {
					throw new BusinessRuntimeException("读取文件错误！");
					
				}
				
		
				
				
				
			}else {
				inStream.reset();
			}
			
			if (inStream != null) {
				while ((len = inStream.read(buf)) != -1) {
					oos.write(buf, 0, len);
				}
			}

			oos.flush();
			ois = new ObjectInputStream(conn.getInputStream());
			Object obj = ois.readObject();
			Thread.sleep(10L);
			if (obj instanceof Exception) {
				throw (Exception) obj;
			}

			var15 = (FileHeader) obj;
		} catch (ProtocolException var22) {
			ExceptionUtils.wrapException(var22);
			return null;
		} catch (IOException var23) {
			ExceptionUtils
					.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501002_0", "01501002-0305"));
			return null;
		} catch (ClassNotFoundException var24) {
			ExceptionUtils.wrapException(var24);
			return null;
		} catch (Exception var25) {
			ExceptionUtils.wrapException(var25);
			return null;
		} finally {
			HttpUtils.closeInputStream(inStream);
			HttpUtils.closeOutputStream(oos);
			HttpUtils.closeInputStream(ois);
			HttpUtils.closeConnection(conn);
		}

		return var15;
	}

	private void validateFileName(String fileName) {
		if (StringUtils.isEmpty(fileName)) {
			ExceptionUtils
					.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501002_0", "01501002-0307"));
		}

		if (fileName.length() > 300) {
			ExceptionUtils
					.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501002_0", "01501002-0308"));
		}

		if (!fileName.matches(
				"[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$")) {
			ExceptionUtils
					.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501002_0", "01501002-0309"));
		}

	}

	public String getRestfulURI(String bucket) {
		String url = UfsHostURI.getRestfulURI(bucket);
		return url;
	}

	@Override
	public Map<String, String> queryVbillCode(String billid) {
		 
		FileSpaceDAOForNCC dao = new FileSpaceDAOForNCC();
        Map<String, String> map = dao.queryVbillCode(billid);
        return map;
	}

	@Override
	public NCFileVO[] queryNCFileByBill(String billId) {
		 
		return new nccloud.pubimpl.platform.attachment.FileSpaceDAOForNCC().queryFileVOsByPath(billId);
	}

	@Override
	public Map<String, String> queryPurdailyMap(String billid) {
		FileSpaceDAOForNCC dao = new FileSpaceDAOForNCC();
        Map<String, String> map = dao.queryPurdailyMap(billid);
        return map;
		 
	}
}