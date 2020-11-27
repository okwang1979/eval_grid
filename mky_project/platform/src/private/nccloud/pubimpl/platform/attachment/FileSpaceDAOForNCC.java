package nccloud.pubimpl.platform.attachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.pub.filesystem.FileSystemUtil;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.filesystem.NCFileVO;
import nccloud.base.collection.tabular.IRow;
import nccloud.base.collection.tabular.IRowSet;
import nccloud.base.collection.tabular.IRowSetMetaData;
import nccloud.base.exception.ExceptionUtils;
import nccloud.pubimpl.platform.db.NCDataQuery;
import nccloud.pubitf.platform.db.SqlParameterCollection;

public class FileSpaceDAOForNCC
{
  public NCFileVO[] queryFileVOsByPath(String path)
  {
    path = FileSystemUtil.validatePathString(path);

    StringBuilder sql = new StringBuilder();
    sql.append("select pk, filepath, creator, isfolder, lasttime, ");
    sql.append("filelength, isdoc, pk_doc, modifier, filedesc, ");
    sql.append("scantimes, modifytime, filetype ");
    sql.append("from sm_pub_filesystem where filepath=? or filepath like ?");

    SqlParameterCollection paraCollection = new SqlParameterCollection();
    paraCollection.addVarChar(path);
    paraCollection.addVarChar(new StringBuilder().append(path).append("/%").toString());

    NCDataQuery dao = new NCDataQuery();
    IRowSet rowset = dao.query(sql.toString(), paraCollection);

    return handleResult(rowset);
  }
  public Map<String,String> queryVbillCode(String billId)
  {
	String vbillcode = "";
	String subscribedate = "";
	HashMap<String,String> map = new HashMap<String, String>();
    StringBuilder sql = new StringBuilder();
    sql.append("select vbillcode£¬subscribedate from ct_sale where pk_ct_sale = ?");
    SqlParameterCollection paraCollection = new SqlParameterCollection();
    paraCollection.addVarChar(billId);
//    paraCollection.addVarChar(new StringBuilder().append(billId).append("/%").toString());
    NCDataQuery dao = new NCDataQuery();
    IRowSet rowset = null;
     rowset = dao.query(sql.toString(),paraCollection);
    if(!rowset.hasNext()) {
    	StringBuilder sql1 = new StringBuilder();
    	sql1.append("select vbillcode£¬subscribedate from ct_pu where pk_ct_pu = ?");
    	rowset = dao.query(sql1.toString(),paraCollection);
    }
    while (rowset.hasNext()) {
    	IRow row = rowset.next();
    	vbillcode = row.getString(0);
    	map.put("vbillcode", vbillcode);
    	subscribedate = row.getString(1);
    	if(null != subscribedate && !"".equals(subscribedate)) {
    		map.put("subscribedate", subscribedate);
    	}
    }
    return map;
  }
  public NCFileVO[] queryNCFilesByFullPaths(String[] fullPaths) {
    if ((fullPaths == null) || (fullPaths.length == 0)) {
      throw new RuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("appbase", "FileSystemUtil-000000"));
    }

    StringBuilder sql = new StringBuilder();
    sql.append("select pk, filepath, creator, isfolder, lasttime, ");
    sql.append("filelength, isdoc, pk_doc, modifier, filedesc, ");
    sql.append("scantimes, modifytime, filetype ");
    sql.append("from sm_pub_filesystem where filepath in (?)");

    StringBuilder inSql = new StringBuilder();
    for (int i = 0; i < fullPaths.length; i++) {
      String fullPath = fullPaths[i];
      inSql.append(fullPath).append(",");
    }
    SqlParameterCollection paraCollection = new SqlParameterCollection();
    paraCollection.addVarChar(inSql.substring(0, inSql.length() - 1));

    NCDataQuery dao = new NCDataQuery();
    IRowSet rowset = dao.query(sql.toString(), paraCollection);

    return handleResult(rowset);
  }

  private NCFileVO[] handleResult(IRowSet rowset) {
    List list = new ArrayList();
    while (rowset.hasNext()) {
      IRow row = rowset.next();
      String pk = row.getString(0);
      String path = row.getString(1);
      String creator = row.getString(2);
      String strFolder = row.getString(3);
      String timeStr = row.getString(4);
      String fileLen = row.getString(5);

      String strisdoc = row.getString(6);

      boolean isFolder = strFolder == null ? false : strFolder
        .equalsIgnoreCase("y");

      String isdoc = strisdoc;
      String pk_doc = row.getString(7);

      String modifier = row.getString(8);
      String filedesc = row.getString(9);

      int scantimes = row
        .getInteger(10) == null ? 
        0 : row.getInteger(10).intValue();
      String modifytimeStr = row.getString(11);

      String filetype = row.getString(12);

      NCFileVO fileVO = new NCFileVO(path);
      fileVO.setPk(pk);
      fileVO.setFolder(isFolder);
      fileVO.setCreator(creator);
      long time = parseStringToLong(timeStr);
      fileVO.setTime(time);
      long fileLength = parseStringToLong(fileLen);
      fileVO.setFileLen(fileLength);

      fileVO.setIsdoc(isdoc);
      fileVO.setPk_doc(pk_doc);

      fileVO.setModifier(modifier);
      fileVO.setFiledesc(filedesc);
      fileVO.setScantimes(scantimes);
      fileVO.setModifytime(parseStringToLong(modifytimeStr));

      fileVO.setFiletype(filetype);

      list.add(fileVO);
    }
    return (NCFileVO[])list.toArray(new NCFileVO[0]);
  }

  private long parseStringToLong(String str) {
    long retr = 0L;
    try {
      retr = str == null ? 0L : Long.parseLong(str.trim());
    }
    catch (Exception e) {
      ExceptionUtils.wrapBusinessException(e.getMessage());
    }
    return retr;
  }
public Map<String, String> queryPurdailyMap(String billId) {
	
	String vbillcode = "";
	String subscribedate = "";
	HashMap<String,String> map = new HashMap<String, String>();
    StringBuilder sql = new StringBuilder();
    sql.append("select vbillcode£¬subscribedate from ct_pu where pk_ct_pu = ?");
    SqlParameterCollection paraCollection = new SqlParameterCollection();
    paraCollection.addVarChar(billId);
//    paraCollection.addVarChar(new StringBuilder().append(billId).append("/%").toString());
    NCDataQuery dao = new NCDataQuery();
    IRowSet rowset = dao.query(sql.toString(),paraCollection);
    while (rowset.hasNext()) {
    	IRow row = rowset.next();
    	vbillcode = row.getString(0);
    	map.put("vbillcode", vbillcode);
    	subscribedate = row.getString(1);
    	if(null != subscribedate && !"".equals(subscribedate)) {
    		map.put("subscribedate", subscribedate);
    	}
    }
    return map;
		
}
}