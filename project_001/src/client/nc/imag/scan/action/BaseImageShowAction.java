/*     */ package nc.imag.scan.action;
/*     */ 
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Map;
/*     */ import nc.bs.framework.common.InvocationInfoProxy;
/*     */ import nc.bs.logging.Logger;
/*     */ import nc.imag.pub.uitl.ImagBasePubUtil;
/*     */ import nc.imag.pub.util.ImageServiceUtil;
/*     */ import nc.imag.scan.service.ScanFieldConvertService;
/*     */ import nc.imag.util.ws.ImageFactoryConfigUtil;
/*     */ import nc.itf.uap.pf.metadata.IFlowBizItf;
/*     */ import nc.md.data.access.NCObject;
/*     */ import nc.md.model.IBean;
/*     */ import nc.ui.pubapp.uif2app.AppUiState;
/*     */ import nc.ui.uif2.NCAction;
/*     */ import nc.ui.uif2.ShowStatusBarMsgUtil;
/*     */ import nc.ui.uif2.model.AbstractUIAppModel;
/*     */ import nc.vo.ml.AbstractNCLangRes;
/*     */ import nc.vo.ml.NCLangRes4VoTransl;
/*     */ import nc.vo.pub.BusinessException;
/*     */ import nc.vo.pub.SuperVO;
/*     */ import nc.vo.pubapp.pattern.exception.ExceptionUtils;
/*     */ import nc.vo.uif2.LoginContext;
/*     */ import uap.lfw.dbl.cpdoc.util.DocCommonUtil;
/*     */ import uap.lfw.dbl.vo.MetaDataBaseAggVO;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class BaseImageShowAction
/*     */   extends NCAction
/*     */ {
/*     */   private AbstractUIAppModel model;
/*     */   private Object dataObj;
/*     */   private Object selDataObj;
/*     */   private String pk_billtype;
/*     */   private String pk_org;
/*     */   private String ownmodule;
/*     */   
/*     */   public BaseImageShowAction()
/*     */   {
/*  45 */     setCode("BaseImageShow");
/*  46 */     setBtnName(NCLangRes4VoTransl.getNCLangRes().getStrByID("1054002_0", "01054002-0005"));
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*  51 */   ScanUIDialog dlg = null;
/*     */   
/*     */   public void doAction(ActionEvent e) {
/*     */     try {
/*  55 */       if (!ImagBasePubUtil.imageIsEnabled()) {
/*  56 */         ShowStatusBarMsgUtil.showErrorMsg(NCLangRes4VoTransl.getNCLangRes().getStrByID("1054002_0", "01054002-0001"), NCLangRes4VoTransl.getNCLangRes().getStrByID("1054002_0", "01054002-0006"), getModel().getContext());
/*     */         
/*     */ 
/*     */ 
/*     */ 
/*  61 */         return;
/*     */       }
/*     */     } catch (BusinessException e2) {
/*  64 */       ExceptionUtils.wrappException(e2);
/*  65 */       return;
/*     */     }
/*     */     try
/*     */     {
/*  69 */       this.pk_org = this.model.getContext().getPk_group();
/*  70 */       if (this.pk_org == null) {
/*  71 */         NCObject ncObj = NCObject.newInstance(this.selDataObj);
/*  72 */         IFlowBizItf itf = (IFlowBizItf)ncObj.getBizInterface(IFlowBizItf.class);
/*  73 */         this.pk_org = itf.getPkorg();
/*     */       }
/*  75 */       if (this.dataObj == null) {
/*  76 */         this.selDataObj = this.model.getSelectedData();
/*  77 */         if (this.model.getAppUiState().equals(AppUiState.ADD)) {
/*  78 */           throw new BusinessException("µ¥¾ÝÎ´ÔÝ´æ");
/*     */         }
/*     */       } else {
/*  81 */         this.selDataObj = this.dataObj;
/*  82 */         this.dataObj = null;
/*     */       }
/*  84 */       if (this.selDataObj == null) {
/*  85 */         throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("1054002_0", "01054002-0003"));
/*     */       }
/*     */       
/*     */ 
/*  89 */       String factoryCode = ImageFactoryConfigUtil.factoryCode;
/*  90 */       if (factoryCode == null) {
/*  91 */         factoryCode = ImageServiceUtil.getImageFactoryCode(this.pk_org);
/*     */       }
/*  93 */       if ("tchzt".equalsIgnoreCase(factoryCode)) {
/*  94 */         showModal();
/*     */       } else {
/*  96 */         if ((this.selDataObj instanceof MetaDataBaseAggVO)) {
/*  97 */           String docPK = (String)((MetaDataBaseAggVO)this.selDataObj).getParentVO().getAttributeValue("pk_doc");
/*  98 */           IBean bean = DocCommonUtil.getBeanByMD(docPK);
/*  99 */           ((MetaDataBaseAggVO)this.selDataObj).getParentVO().setAttributeValue("bean", bean);
/*     */         }
/* 101 */         NCObject ncObj = NCObject.newInstance(this.selDataObj);
/* 102 */         IFlowBizItf itf = (IFlowBizItf)ncObj.getBizInterface(IFlowBizItf.class);
/* 103 */         String billtype = itf.getTranstype();
/* 104 */         if ((billtype == null) || (billtype.equals(""))) {
/* 105 */           billtype = itf.getBilltype();
/*     */         }
/* 107 */         if ((billtype == null) || (billtype.equals(""))) {
/* 108 */           billtype = this.pk_billtype;
/*     */         }
/* 110 */         String billid = itf.getBillId();
/* 111 */         String userid = InvocationInfoProxy.getInstance().getUserId();
/* 112 */         ImageServiceUtil.imageShowCheck(this.selDataObj, billtype, billid, this.pk_org);
/* 113 */         String url = ImageServiceUtil.getImageShowURL(this.selDataObj, billtype, billid, userid, this.pk_org);
/* 114 */         Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
/*     */       }
/*     */     }
/*     */     catch (Exception e1) {
/* 118 */       Logger.error(e1.getMessage(), e1);
/* 119 */       ShowStatusBarMsgUtil.showErrorMsg(NCLangRes4VoTransl.getNCLangRes().getStrByID("1054002_0", "01054002-0001"), e1.getMessage(), this.model.getContext());
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */   private void showModal()
/*     */     throws Exception
/*     */   {
/* 127 */     ScanFieldConvertService service = new ScanFieldConvertService();
/* 128 */     Map<String, String> fieldMap = service.getFieldMap(this.selDataObj, this.pk_billtype, this.pk_org);
/*     */     
/* 130 */     ImageScanBrowser browser = new ImageScanBrowser(null, fieldMap);
/* 131 */     browser.showBrowser(ImageScanBrowser.SCANTYPE_SHOW, null);
/*     */   }
/*     */   
/*     */   public void setModel(AbstractUIAppModel model) {
/* 135 */     this.model = model;
/* 136 */     model.addAppEventListener(this);
/*     */   }
/*     */   
/*     */   public AbstractUIAppModel getModel() {
/* 140 */     return this.model;
/*     */   }
/*     */   
/*     */   public String getPk_billtype() {
/* 144 */     return this.pk_billtype;
/*     */   }
/*     */   
/*     */   public void setPk_billtype(String pk_billtype) {
/* 148 */     this.pk_billtype = pk_billtype;
/*     */   }
/*     */   
/*     */   public Object getDataObj() {
/* 152 */     return this.selDataObj;
/*     */   }
/*     */   
/*     */   public void setDataObj(Object dataObj) {
/* 156 */     this.dataObj = dataObj;
/*     */   }
/*     */ }

/* Location:           D:\home\nchome0218_jn\modules\imag\client\classes
 * Qualified Name:     nc.imag.scan.action.BaseImageShowAction
 * Java Class Version: 7 (51.0)
 * JD-Core Version:    0.7.0.1
 */