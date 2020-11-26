package nccloud.pubitf.platform.attachment;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import nc.lightapp.framework.web.action.attachment.AttachmentVO;
import nc.vo.pub.filesystem.NCFileNode;
import nc.vo.pub.filesystem.NCFileVO;
import uap.pub.fs.domain.basic.FileHeader;

public abstract interface IAttachmentService
{
  public abstract void deleteAttachDBInfo(String[] paramArrayOfString);

  public abstract InputStream download(String paramString1, String paramString2, String paramString3);

  public abstract long getFileLimit();

  public abstract HashMap<String, String> getFtpTransferConfig();

  public abstract HashMap<String, String> getFtpWlanConfig();

  public abstract HashMap<String, String> getRestTransferConfig();

  public abstract HashMap<String, String> getWlanConfig();

  public abstract NCFileVO[] queryNCFilesByFullPaths(String[] paramArrayOfString);

  public abstract NCFileVO[] queryNCFilesByNodePath(String paramString1, String paramString2);

  public abstract String readConf(String paramString);

  public abstract void remove(String paramString1, String paramString2, String paramString3);

  public abstract NCFileNode saveAttachDBInfo(String paramString, NCFileVO paramNCFileVO);

  public abstract FileHeader upload(String paramString1, InputStream paramInputStream, boolean paramBoolean, int paramInt, String paramString2, String billId,String fullPath,String appCode);

  public abstract String getRestfulURI(String paramString);
  
  public   abstract Map<String, String>   queryVbillCode(String billid);
  
  public Map<String,String> queryPurdailyMap(String billid);
  
  public NCFileVO[] queryNCFileByBill(String billId);
}