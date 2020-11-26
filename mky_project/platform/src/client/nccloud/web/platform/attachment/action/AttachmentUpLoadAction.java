package nccloud.web.platform.attachment.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nc.lightapp.framework.web.action.attachment.AttachmentVO;
import nc.lightapp.framework.web.action.attachment.IUploadAction;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.filesystem.NCFileNode;
import nc.vo.pub.filesystem.NCFileVO;
import nc.vo.pubapp.AppContext;
import nccloud.commons.lang.StringUtils;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.core.io.WebFile;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.container.IRequest;
import nccloud.framework.web.container.RequestSysJsonVO;
import nccloud.pubitf.platform.attachment.IAttachmentService;
import nccloud.pubitf.platform.attachment.IFileStorageConst;
import nccloud.pubitf.platform.attachment.WebFileParaVO;
import nccloud.web.platform.attachment.tool.AttachmentButtonLimitUtils;
import nccloud.web.platform.attachment.tool.AttachmentUploadSystemParamUtils;
import uap.pub.fs.domain.basic.FileHeader;

public class AttachmentUpLoadAction
  implements IUploadAction
{
  private IAttachmentService ncservice = (IAttachmentService)ServiceLocator.find(IAttachmentService.class);

  public Object doAction(AttachmentVO paras)
  {
    Map params = paras.getParameters();

    String[] paths = (String[])params.get("fullPath");

    if (!AttachmentButtonLimitUtils.getButtonLimit(params, "REMOVE")) {
      ExceptionUtils.wrapBusinessException("无按钮权限");
      return null;
    }

    if ((paths == null) || (paths.length == 0))
    {
      ExceptionUtils.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes()
        .getStrByID("1501002_0", "01501002-0286"));

      return null;
    }
    String fullPath = paths[0];

    String[] storepaths = (String[])params.get("storepath");
    String newBucket = getStorePath(storepaths);

    WebFile[] files = paras.getFiles();
    String billId = ((String[])params.get("billId"))[0];
    List filePaths = new ArrayList();
    String appCode = ((String[]) params.get("sys_appcode"))[0];

    for (WebFile file : files)
    {
      FileHeader header = this.ncservice.upload(file.getFileName(), file.getInputStream(), false, 0, newBucket,billId,fullPath,appCode);
      saveDBInfo(fullPath, header);
      filePaths.add(fullPath + "/" + header.getName());
    }

    NCFileVO[] ncfiles = this.ncservice.queryNCFilesByFullPaths((String[])filePaths.toArray(new String[0]));

    WebFileParaConvertor convertor = new WebFileParaConvertor();

    WebFileParaVO[] ret = convertor
      .convertAfterUpload(ncfiles, billId, newBucket);

    return ret;
  }

  private void checkPara(WebFile[] files) {
    long limit = this.ncservice.getFileLimit();
    for (WebFile webfile : files) {
      if (webfile.getFileSize() > limit)
      {
        ExceptionUtils.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes()
          .getStrByID("1501002_0", "01501002-0282"));
      }

      String suffixes = "rar|zip|png|bmp|gif|jpg|pdf|html|htm|xml|doc|docx|xls|xlsx|ppt|ppts|csv|txt";

      String initCode = "AttachType";
      String pkOrg = "GLOBLE00000000000000";
      String paraString = AttachmentUploadSystemParamUtils.readSysParam(initCode, pkOrg);
      if (!StringUtils.isEmpty(paraString))
        suffixes = suffixes + "|" + paraString;
      String filename = webfile.getFileName();

      String suffix = "";
      if ((filename != null) && (filename.contains(".")))
      {
        suffix = filename
          .substring(filename
          .lastIndexOf(".") + 
          1).toLowerCase();
      }
      if ("".equals(suffix)) {
        ExceptionUtils.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501003_0", "01501003-0113") + filename);
      }
      if (!suffixes.contains(suffix))
        ExceptionUtils.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501003_0", "01501003-0114") + filename);
    }
  }

  private String getStorePath(String[] storepaths)
  {
    String storepath = IFileStorageConst.Bucket;
    if ((storepaths != null) && (storepaths.length > 0) && (storepaths[0] != null) && 
      (!""
      .equals(storepaths[0])))
    {
      storepath = storepaths[0];
    }
    return storepath;
  }

  private NCFileNode saveDBInfo(String fullPath, FileHeader header) {
    NCFileVO attach = new NCFileVO();
    attach.setPath(header.getName());
    attach.setCreator(AppContext.getInstance().getPkUser());
    attach.setFileLen(header.getFileSize().longValue());
    attach.setPk_doc(header.getPath());
    attach.setIsdoc("z");
    NCFileNode node = this.ncservice.saveAttachDBInfo(fullPath, attach);
    return node;
  }
}