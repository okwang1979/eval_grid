/*      */ package nc.uap.ctrl.tpl.pamgr;
/*      */ 
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collection;
/*      */ import java.util.Iterator;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import javax.servlet.http.HttpServletRequest;
/*      */ import javax.servlet.http.HttpSession;
/*      */ import nc.bs.framework.common.NCLocator;
/*      */ import nc.uap.cpb.log.CpLogger;
/*      */ import nc.uap.cpb.org.exception.CpbBusinessException;
/*      */ import nc.uap.cpb.org.itf.ICpAppsNodeQry;
/*      */ import nc.uap.cpb.org.vos.CpAppsNodeVO;
/*      */ import nc.uap.cpb.templaterela.itf.ITemplateRelationQryService;
/*      */ import nc.uap.cpb.templaterela.vo.CpTemplateOrgVO;
/*      */ import nc.uap.cpb.templaterela.vo.CpTemplateRoleVO;
/*      */ import nc.uap.cpb.templaterela.vo.CpTemplateUserVO;
/*      */ import nc.uap.ctrl.pa.PaHelper;
/*      */ import nc.uap.ctrl.pa.cmd.UifInitTemplateContentCmd;
/*      */ import nc.uap.ctrl.pa.cmd.UifTemplateExportCmd;
/*      */ import nc.uap.ctrl.pa.itf.IPaPublicQryService;
/*      */ import nc.uap.ctrl.pa.itf.IPaPublicService;
/*      */ import nc.uap.lfw.core.AppInteractionUtil;
/*      */ import nc.uap.lfw.core.LfwRuntimeEnvironment;
/*      */ import nc.uap.lfw.core.WebContext;
/*      */ import nc.uap.lfw.core.bm.ButtonStateManager;
/*      */ import nc.uap.lfw.core.cmd.CmdInvoker;
/*      */ import nc.uap.lfw.core.cmd.UifDatasetAfterSelectCmd;
/*      */ import nc.uap.lfw.core.cmd.UifDatasetLoadCmd;
/*      */ import nc.uap.lfw.core.cmd.UifDsLoadRowEnabledCmd;
/*      */ import nc.uap.lfw.core.cmd.UifEditCmd;
/*      */ import nc.uap.lfw.core.cmd.UifPlugoutCmd;
/*      */ import nc.uap.lfw.core.cmd.base.AbstractWidgetController;
/*      */ import nc.uap.lfw.core.cmd.base.FromWhereSQL;
/*      */ import nc.uap.lfw.core.combodata.CombItem;
/*      */ import nc.uap.lfw.core.combodata.ComboData;
/*      */ import nc.uap.lfw.core.combodata.StaticComboData;
/*      */ import nc.uap.lfw.core.comp.MenuItem;
/*      */ import nc.uap.lfw.core.comp.MenubarComp;
/*      */ import nc.uap.lfw.core.ctrl.IController;
/*      */ import nc.uap.lfw.core.ctrlfrm.ModePhase;
/*      */ import nc.uap.lfw.core.ctx.AppEnvironment;
/*      */ import nc.uap.lfw.core.ctx.AppLifeCycleContext;
/*      */ import nc.uap.lfw.core.ctx.ApplicationContext;
/*      */ import nc.uap.lfw.core.ctx.ViewContext;
/*      */ import nc.uap.lfw.core.ctx.WindowContext;
/*      */ import nc.uap.lfw.core.data.Dataset;
/*      */ import nc.uap.lfw.core.data.Row;
/*      */ import nc.uap.lfw.core.event.DataLoadEvent;
/*      */ import nc.uap.lfw.core.event.DatasetEvent;
/*      */ import nc.uap.lfw.core.event.DialogEvent;
/*      */ import nc.uap.lfw.core.event.MouseEvent;
/*      */ import nc.uap.lfw.core.exception.ComboInputItem;
/*      */ import nc.uap.lfw.core.exception.InputItem;
/*      */ import nc.uap.lfw.core.exception.LfwBusinessException;
/*      */ import nc.uap.lfw.core.exception.LfwRuntimeException;
/*      */ import nc.uap.lfw.core.exception.StringInputItem;
/*      */ import nc.uap.lfw.core.lifecycle.LifeCyclePhase;
/*      */ import nc.uap.lfw.core.lifecycle.RequestLifeCycleContext;
/*      */ import nc.uap.lfw.core.model.IPersonalizationService;
/*      */ import nc.uap.lfw.core.model.plug.TranslatedRow;
/*      */ import nc.uap.lfw.core.model.util.RunTimeDataProvider;
/*      */ import nc.uap.lfw.core.page.LfwView;
/*      */ import nc.uap.lfw.core.page.LfwWindow;
/*      */ import nc.uap.lfw.core.page.ViewMenus;
/*      */ import nc.uap.lfw.core.page.ViewModels;
/*      */ import nc.uap.lfw.core.plzt.PageModelUtil;
/*      */ import nc.uap.lfw.core.uimodel.Application;
/*      */ import nc.uap.lfw.core.uimodel.ViewConfig;
/*      */ import nc.uap.lfw.core.uimodel.WindowConfig;
/*      */ import nc.uap.lfw.core.util.LFWAllComponetsFetcher;
/*      */ import nc.uap.lfw.stylemgr.vo.UwTemplateVO;
/*      */ import nc.uap.lfw.stylemgr.vo.UwViewVO;
/*      */ import nc.uap.lfw.util.LanguageUtil;
/*      */ import nc.vo.ml.AbstractNCLangRes;
/*      */ import nc.vo.ml.NCLangRes4VoTransl;
/*      */ import nc.vo.pub.lang.UFBoolean;
/*      */ import org.apache.commons.lang.StringUtils;
/*      */ import uap.lfw.core.locator.ServiceLocator;
/*      */ import uap.lfw.core.ml.LfwResBundle;
/*      */ import uap.lfw.imp.query.base.ImpAppUtils;
/*      */ import uap.web.bd.pub.AppUtil;
/*      */ 
/*      */ 
/*      */ 
/*      */ public class PaMgrMainViewController
/*      */   extends AbstractWidgetController
/*      */   implements IController
/*      */ {
/*      */   public static final String EDIT_OPERATE = "EDIT_OPERATE";
/*      */   public static final String ADD_OPERATE = "ADD_OPERATE";
/*      */   public static final String OPERATE_STATUS = "OPERATE_STATUS";
/*      */   
/*      */   public PaMgrMainViewController() {}
/*      */   
/*      */   public void addEvent(MouseEvent<?> mouseEvent)
/*      */   {
/*  100 */     Dataset ds = AppUtil.getCntView().getViewModels().getDataset(getMasterDsId());
/*      */     
/*  102 */     Row row = ds.getSelectedRow();
/*  103 */     if (row == null) {
/*  104 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000018"));
/*      */     }
/*      */     
/*      */ 
/*      */ 
/*  109 */     int pkIndex = ds.nameToIndex("pk_template");
/*  110 */     int winIdIndex = ds.nameToIndex("windowid");
/*      */     
/*  112 */     String pk_appsnode = (String)row.getValue(pkIndex);
/*  113 */     String windowId = row.getString(winIdIndex);
/*  114 */     if (pk_appsnode != null)
/*  115 */       pk_appsnode = pk_appsnode.substring(0, 20);
/*  116 */     CpAppsNodeVO node = getCpAppsNodeVOByPK(pk_appsnode);
/*  117 */     if (node.getPk_template() != null) {
/*  118 */       String dftTmpPK = getDefautTplPKByNodePKAndWinId(pk_appsnode, windowId);
/*      */       
/*  120 */       AppUtil.addAppAttr("dftTmpPK", dftTmpPK);
/*      */     }
/*  122 */     UFBoolean flag = node.getSpecialflag();
/*  123 */     if (flag == null) {
/*  124 */       flag = UFBoolean.FALSE;
/*      */     }
/*  126 */     if (!flag.booleanValue()) {
/*  127 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000019"));
/*      */     }
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*  135 */     UifEditCmd cmd = new UifEditCmd("editView", "502", "240", NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000001"), false);
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*  140 */     getCurrentWinCtx().addAppAttribute("OPERATE_STATUS", "ADD_OPERATE");
/*  141 */     cmd.execute();
/*      */   }
/*      */   
/*      */   private String getDefautTplPKByNodePKAndWinId(String pk_appsnode, String windowId)
/*      */   {
/*  146 */     IPaPublicQryService qry = (IPaPublicQryService)ServiceLocator.getService(IPaPublicQryService.class);
/*  147 */     LinkedHashMap<String, String> dim = new LinkedHashMap();
/*  148 */     dim.put("windowid", windowId);
/*  149 */     dim.put("pk_funcnode", pk_appsnode);
/*  150 */     dim.put("issystemplate", "Y");
/*  151 */     String dftTmpPK = null;
/*      */     try
/*      */     {
/*  154 */       UwTemplateVO[] vos = qry.qryTemplateByDimensions(dim);
/*  155 */       if ((vos != null) && (vos.length > 0))
/*  156 */         dftTmpPK = vos[0].getPk_template();
/*      */     } catch (LfwBusinessException e) {
/*  158 */       CpLogger.error(e.getMessage(), e);
/*      */     }
/*  160 */     return dftTmpPK;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   public void beforeShowEvent(DialogEvent dialogEvent) {}
/*      */   
/*      */ 
/*      */ 
/*      */   public void editEvent(MouseEvent<?> mouseEvent)
/*      */   {
/*  172 */     UifEditCmd cmd = new UifEditCmd("editView", "502", "240", NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000002"));
/*      */     
/*      */ 
/*      */ 
/*  176 */     getCurrentWinCtx().addAppAttribute("OPERATE_STATUS", "EDIT_OPERATE");
/*      */     
/*  178 */     Dataset ds = getCurrentWinCtx().getViewContext("main").getView().getViewModels().getDataset(getMasterDsId());
/*      */     
/*  180 */     Row row = ds.getSelectedRow();
/*  181 */     if (row == null) {
/*  182 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000003"));
/*      */     }
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*  190 */     getCurrentWinCtx().addAppAttribute("editRow", row);
/*  191 */     getCurrentWinCtx().addAppAttribute("editDs", ds);
/*      */     
/*  193 */     cmd.execute();
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void deleteEvent(MouseEvent<?> mouseEvent)
/*      */   {
/*  202 */     LfwView widget = getCurrentWinCtx().getWindow().getView("main");
/*  203 */     Dataset ds = widget.getViewModels().getDataset(getMasterDsId());
/*  204 */     Row row = ds.getSelectedRow();
/*  205 */     if (row == null) {
/*  206 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000004"));
/*      */     }
/*      */     
/*  209 */     String pk_template = (String)row.getValue(ds.nameToIndex("pk_template"));
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*  215 */     IPaPublicService paService = (IPaPublicService)ServiceLocator.getService(IPaPublicService.class);
/*  216 */     IPaPublicQryService paQryService = (IPaPublicQryService)ServiceLocator.getService(IPaPublicQryService.class);
/*  217 */     ITemplateRelationQryService tmpRltQry = (ITemplateRelationQryService)NCLocator.getInstance().lookup(ITemplateRelationQryService.class);
/*      */     try
/*      */     {
/*  220 */       String condition = "pk_template = '" + pk_template + "'";
/*  221 */       CpTemplateOrgVO[] templateOrgs = tmpRltQry.getTemplateOrgVOsByCondition(condition);
/*      */       
/*  223 */       CpTemplateRoleVO[] templateRoles = tmpRltQry.getTemplateRoleVOsByCondition(condition);
/*      */       
/*  225 */       CpTemplateUserVO[] templateUsers = tmpRltQry.getTemplateUserVOsByCondition(condition);
/*      */       
/*  227 */       if (((templateOrgs != null) && (templateOrgs.length > 0)) || ((templateRoles != null) && (templateRoles.length > 0)) || ((templateUsers != null) && (templateUsers.length > 0)))
/*      */       {
/*      */ 
/*  230 */         throw new LfwRuntimeException(LfwResBundle.getInstance().getStrByID("imp", "PaMgrMainViewController-000000"));
/*      */       }
/*  232 */       UwTemplateVO template = paQryService.getTemplateVOByPK(pk_template);
/*  233 */       AppInteractionUtil.showConfirmDialog(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifDelMultiCmd-000000"), NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifDelMultiCmd-000001"));
/*      */       
/*      */ 
/*      */ 
/*      */ 
/*  238 */       if (AppInteractionUtil.getConfirmDialogResult().equals(Boolean.FALSE))
/*      */       {
/*  240 */         return; }
/*  241 */       if (template != null)
/*  242 */         paService.deleteTemplate(template);
/*  243 */       List<UwViewVO> views = paQryService.getViewVOsByCondition(condition);
/*  244 */       if ((views != null) && (views.size() > 0)) {
/*  245 */         paService.deleteViewVO((UwViewVO[])views.toArray(new UwViewVO[0]));
/*      */       }
/*      */     }
/*      */     catch (LfwBusinessException e)
/*      */     {
/*  250 */       CpLogger.error(e.getMessage(), e);
/*  251 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000005") + e.getMessage());
/*      */     }
/*      */     
/*      */ 
/*      */ 
/*  256 */     ds.removeRow(row);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void paSetEvent(MouseEvent<?> mouseEvent)
/*      */   {
/*  265 */     LfwView mwidget = getCurrentWinCtx().getWindow().getView("main");
/*      */     
/*  267 */     Dataset ds = mwidget.getViewModels().getDataset(getMasterDsId());
/*  268 */     Row row = ds.getSelectedRow();
/*  269 */     if (row == null) {
/*  270 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000006"));
/*      */     }
/*      */     
/*      */ 
/*  274 */     String winId = row.getString(ds.nameToIndex("windowid"));
/*  275 */     String pk_template = row.getString(ds.nameToIndex("pk_template"));
/*      */     
/*  277 */     String url = null;
/*  278 */     if (winId == null) {
/*  279 */       return;
/*      */     }
/*  281 */     if (pk_template == null) {
/*  282 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000007"));
/*      */     }
/*      */     
/*      */ 
/*      */ 
/*  287 */     LinkedHashMap<String, String> dimensions = new LinkedHashMap();
/*  288 */     dimensions.put("windowid", winId);
/*  289 */     url = PaHelper.createPaURL(dimensions, ModePhase.pa) + "&pk_template=" + pk_template;
/*      */     
/*      */ 
/*  292 */     String viewId = getSelectedViewByPK(pk_template);
/*  293 */     url = url + "&viewId=" + viewId;
/*      */     
/*      */ 
/*  296 */     String script = "window.open('" + url + "')";
/*  297 */     AppLifeCycleContext.current().getWindowContext().addExecScript(script);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   private String getSelectedViewByPK(String pk_template)
/*      */   {
/*  305 */     IPaPublicQryService paQryService = (IPaPublicQryService)ServiceLocator.getService(IPaPublicQryService.class);
/*  306 */     List<String> wIds = new ArrayList();
/*  307 */     LfwWindow pm = PageModelUtil.getPageMeta(pk_template);
/*      */     try {
/*  309 */       UwViewVO[] views = paQryService.getViewByTemplateId(pk_template);
/*  310 */       if ((views != null) && (views.length > 0)) {
/*  311 */         for (int i = 0; i < views.length; i++) {
/*  312 */           String viewId = views[i].getViewid();
/*  313 */           ViewConfig viewConfig = pm.getViewConfig(viewId);
/*      */           				 
					if(viewConfig == null) {
							continue;
					}
/*  315 */           boolean canFreeDisign = viewConfig.isCanFreeDesign();
/*  316 */           boolean isFreeDesign = views[i].getIsfreedesign() == null ? true : views[i].getIsfreedesign().booleanValue();
/*  317 */           boolean canDesign = (canFreeDisign) && (isFreeDesign);
/*  318 */           if (canDesign)
/*  319 */             wIds.add(viewId);
/*      */         }
/*      */       }
/*      */     } catch (LfwBusinessException e) {
/*  323 */       CpLogger.error(e.getMessage(), e);
/*      */     }
/*      */     
/*  326 */     if ((wIds == null) || (wIds.size() == 0)) {
/*  327 */       throw new LfwRuntimeException(LfwResBundle.getInstance().getStrByID("imp", "PaMgrMainViewController-000001"));
/*      */     }
/*      */     
/*  330 */     ComboInputItem item = new ComboInputItem("selectItemId", NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000020"), true);
/*      */     
/*      */ 
/*  333 */     ComboData cd = new StaticComboData();
/*  334 */     if ((wIds != null) && (wIds.size() > 0)) {
/*  335 */       for (int i = 0; i < wIds.size(); i++) {
/*  336 */         String value = (String)wIds.get(i);
/*  337 */         String caption = null;
/*  338 */         if (pm != null) {
/*  339 */           LfwView view = pm.getView((String)wIds.get(i));
/*  340 */           if (view != null) {
/*  341 */             caption = LanguageUtil.translate(view.getI18nName(), view.getCaption(), view.getLangDir());
/*      */           }
/*  343 */           value = StringUtils.isBlank(caption) ? value : caption;
/*      */         }
/*  345 */         cd.addCombItem(new CombItem((String)wIds.get(i), value));
/*      */       }
/*  347 */       item.setComboData(cd);
/*  348 */       item.setValue(wIds.get(0));
/*      */     }
/*  350 */     if ((wIds != null) && (wIds.size() == 1) && (item.getComboData().getAllCombItems().length == 1)) {
/*  351 */       return (String)item.getValue();
/*      */     }
/*      */     
/*  354 */     AppInteractionUtil.showInputDialog("selectView", NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000021"), new InputItem[] { item });
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*  359 */     Map<String, String> rs = AppInteractionUtil.getInputDialogResult("selectView");
/*      */     
/*  361 */     String widgetId = (String)rs.get("selectItemId");
/*      */     
/*  363 */     return widgetId;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private WindowContext getCurrentWinCtx()
/*      */   {
/*  374 */     return AppLifeCycleContext.current().getApplicationContext().getCurrentWindowContext();
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private LfwWindow getPageMetaById(String winId)
/*      */   {
/*  386 */     RequestLifeCycleContext.get().setPhase(LifeCyclePhase.nullstatus);
/*  387 */     LfwWindow pm = RunTimeDataProvider.getInstance().getWindow(winId);
/*      */     
/*  389 */     RequestLifeCycleContext.get().setPhase(LifeCyclePhase.ajax);
/*  390 */     return pm;
/*      */   }
/*      */   
/*      */   public String getMasterDsId()
/*      */   {
/*  395 */     return "ds_template";
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void onDataLoad(DataLoadEvent dataLoadEvent)
/*      */   {
/*  404 */     Dataset ds = (Dataset)dataLoadEvent.getSource();
/*      */     
/*  406 */     UifDsLoadRowEnabledCmd dsCmd = new UifDsLoadRowEnabledCmd(getMasterDsId(), null);
/*      */     
/*  408 */     dsCmd.execute();
/*  409 */     StringBuffer buf = new StringBuffer();
/*  410 */     buf.append("priority IS NOT NULL AND (pk_device IS NOT NULL and pk_device !='~')");
/*  411 */     String curgroup = AppUtil.getPk_group();
/*  412 */     String pk_org = AppUtil.getPk_org();
/*  413 */     buf.append("and pk_funcnode in (select pk_appsnode from cp_appsnode where pk_group = '" + curgroup + "' and pk_org = '" + pk_org + "' ");
/*  414 */     String nodeid = (String)AppLifeCycleContext.current().getApplicationContext().getAppAttribute("$Template_PK_Node");
/*  415 */     if (!StringUtils.isEmpty(nodeid)) {
/*  416 */       buf.append(" and id = '" + nodeid + "')");
/*      */     } else {
/*  418 */       buf.append(")");
/*      */     }
/*  420 */     ds.setLastCondition(buf.toString());
/*      */     
/*  422 */     CmdInvoker.invoke(new UifDatasetLoadCmd(ds.getId()));
/*  423 */     ButtonStateManager.updateButtons();
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void onAfterRowSelect(DatasetEvent datasetEvent)
/*      */   {
/*  433 */     Dataset ds = (Dataset)datasetEvent.getSource();
/*  434 */     Row row = ds.getSelectedRow();
/*  435 */     String fatherId = (String)row.getValue(ds.nameToIndex("fatherid"));
/*  436 */     String pk_funcnode = (String)row.getValue(ds.nameToIndex("pk_funcnode"));
/*      */     
/*      */ 
/*  439 */     if ((fatherId == null) && (pk_funcnode != null)) {
/*  440 */       String winId = (String)row.getValue(ds.nameToIndex("windowid"));
/*  441 */       fatherId = pk_funcnode + winId;
/*      */     }
/*      */     
/*      */ 
/*      */ 
/*  446 */     String pk = (String)row.getValue(ds.nameToIndex("pk_template"));
/*  447 */     String winId = (String)row.getValue(ds.nameToIndex("windowid"));
/*  448 */     String appId = (String)row.getValue(ds.nameToIndex("appid"));
/*  449 */     AppUtil.getCntAppCtx().addAppAttribute("pk_template", pk);
/*  450 */     AppUtil.getCntAppCtx().addAppAttribute("winId", winId);
/*  451 */     AppUtil.getCntAppCtx().addAppAttribute("appId", appId);
/*  452 */     AppUtil.getCntAppCtx().addAppAttribute("template_type", Integer.valueOf(1));
/*      */     
/*  454 */     CmdInvoker.invoke(new UifDatasetAfterSelectCmd(ds.getId()));
/*      */     
/*  456 */     refreshTemplateRelation(pk);
/*      */     
/*  458 */     setMenuState(fatherId);
/*      */   }
/*      */   
/*      */   private void setMenuState(String fatherId) {
/*  462 */     LfwView widget = AppUtil.getCntView();
/*  463 */     MenubarComp[] menubas = widget.getViewMenus().getMenuBars();
/*  464 */     if ((menubas != null) && (menubas.length > 0)) {
/*  465 */       for (int i = 0; i < menubas.length; i++) {
/*  466 */         MenubarComp menu = menubas[i];
/*  467 */         List<MenuItem> itemList = menu.getMenuList();
/*  468 */         if ((itemList != null) && (itemList.size() > 0))
/*      */         {
/*  470 */           for (int j = 0; j < itemList.size(); j++) {
/*  471 */             MenuItem item = (MenuItem)itemList.get(j);
/*  472 */             String itemName = item.getId();
/*  473 */             if (fatherId != null) {
/*  474 */               if (itemName.equals("add")) {
/*  475 */                 item.setEnabled(false);
/*      */               } else {
/*  477 */                 item.setEnabled(true);
/*      */               }
/*  479 */               if (itemName.equals("seniorSet")) {
/*  480 */                 item.setEnabled(true);
/*  481 */                 List<MenuItem> childItems = item.getChildList();
/*  482 */                 for (int k = 0; k < childItems.size(); k++) {
/*  483 */                   MenuItem childItem = (MenuItem)childItems.get(k);
/*  484 */                   if (childItem.getId().equals("look$template")) {
/*  485 */                     childItem.setEnabled(false);
/*      */                   } else {
/*  487 */                     childItem.setEnabled(true);
/*      */                   }
/*      */                 }
/*      */               }
/*      */             } else {
/*  492 */               if (itemName.equals("add")) {
/*  493 */                 item.setEnabled(true);
/*      */               } else {
/*  495 */                 item.setEnabled(false);
/*      */               }
/*  497 */               if (itemName.equals("seniorSet")) {
/*  498 */                 item.setEnabled(true);
/*  499 */                 List<MenuItem> childItems = item.getChildList();
/*  500 */                 for (int k = 0; k < childItems.size(); k++) {
/*  501 */                   MenuItem childItem = (MenuItem)childItems.get(k);
/*  502 */                   if (childItem.getId().equals("look$template")) {
/*  503 */                     childItem.setEnabled(true);
/*      */                   } else {
/*  505 */                     childItem.setEnabled(false);
/*      */                   }
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   public void pluginappscategory_plugin(Map<Object, Object> keys)
/*      */   {
/*  518 */     LfwView main = AppUtil.getCntView();
/*  519 */     Dataset ds = main.getViewModels().getDataset(getMasterDsId());
/*  520 */     ds.clear();
/*  521 */     TranslatedRow r = (TranslatedRow)keys.get("appscategory_click");
/*  522 */     String pk_appsnode = (String)r.getValue("pk_appsnode");
/*      */     
/*  524 */     CpAppsNodeVO node = getCpAppsNodeVOByPK(pk_appsnode);
/*      */     
/*  526 */     loadGridData(ds, node);
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*  533 */     refreshTemplateRelation("");
/*      */     
/*  535 */     setMenuState(null);
/*      */   }
/*      */   
/*      */   private CpAppsNodeVO getCpAppsNodeVOByPK(String pk_appsnode)
/*      */   {
/*  540 */     ICpAppsNodeQry appQry = (ICpAppsNodeQry)NCLocator.getInstance().lookup(ICpAppsNodeQry.class);
/*      */     
/*  542 */     CpAppsNodeVO node = null;
/*      */     try {
/*  544 */       node = appQry.getNodeByPk(pk_appsnode);
/*      */     }
/*      */     catch (CpbBusinessException e) {
/*  547 */       CpLogger.error(e.getMessage(), e);
/*  548 */       throw new LfwRuntimeException(e.getMessage(), e);
/*      */     }
/*  550 */     return node;
/*      */   }
/*      */   
/*  553 */   private CpAppsNodeVO getCpAppsNodeVOBynodeid(String appsnodeid) { ICpAppsNodeQry appQry = (ICpAppsNodeQry)NCLocator.getInstance().lookup(ICpAppsNodeQry.class);
/*      */     
/*  555 */     CpAppsNodeVO node = null;
/*      */     try {
/*  557 */       node = appQry.getNodeById(appsnodeid);
/*      */     }
/*      */     catch (CpbBusinessException e) {
/*  560 */       CpLogger.error(e.getMessage(), e);
/*  561 */       throw new LfwRuntimeException(e.getMessage(), e);
/*      */     }
/*  563 */     return node;
/*      */   }
/*      */   
/*      */   private void loadGridData(Dataset ds, CpAppsNodeVO node) {
/*  567 */     ds.clear();
/*  568 */     if (node.getSpecialflag() == UFBoolean.FALSE) {
/*  569 */       return;
/*      */     }
/*  571 */     int pkIndex = ds.nameToIndex("pk_template");
/*  572 */     int nameIndex = ds.nameToIndex("templatename");
/*  573 */     int winIndex = ds.nameToIndex("windowid");
/*  574 */     int appIndex = ds.nameToIndex("appid");
/*  575 */     int fatherIndex = ds.nameToIndex("fatherid");
/*      */     
/*  577 */     Row row = ds.getEmptyRow();
/*  578 */     if (node != null) {
/*  579 */       String url = node.getUrl();
/*  580 */       if (url.isEmpty()) {
/*  581 */         CpLogger.error(node.getId() + NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrLeftViewController-000000"));
/*      */       }
/*  583 */       String appId = node.getAppid();
/*  584 */       String ctxPath = LfwRuntimeEnvironment.getRootPath();
/*  585 */       Application app = LFWAllComponetsFetcher.getAppById(ctxPath, appId);
/*  586 */       if (app != null) {
/*  587 */         List<WindowConfig> winList = app.getWindowList();
/*  588 */         if (winList != null)
/*  589 */           for (int j = 0; j < winList.size(); j++) {
/*  590 */             WindowConfig pm = (WindowConfig)winList.get(j);
/*  591 */             if (pm != null)
/*      */             {
/*  593 */               if (pm.isCanFreeDesign())
/*      */               {
/*  595 */                 String winId = pm.getId();
/*  596 */                 LfwWindow win = LFWAllComponetsFetcher.getWindowByIdNotClone(LfwRuntimeEnvironment.getRootPath(), winId);
/*      */                 
/*  598 */                 if ((win == null) && (node.getPk_template() != null)) {
/*  599 */                   IPersonalizationService ipz = (IPersonalizationService)ServiceLocator.getService(IPersonalizationService.class);
/*  600 */                   win = ipz.getUIMetaByNodePKAndWin(node.getPk_appsnode(), winId);
/*      */                 }
/*      */                 
/*  603 */                 if (win != null) {
/*  604 */                   String pmTitle = LanguageUtil.getWithDefaultByProductCode(win.getLangDir(), win.getCaption(), win.getI18nName());
/*      */                   
/*  606 */                   row = ds.getEmptyRow();
/*  607 */                   row.setValue(pkIndex, node.getPk_appsnode() + winId);
/*  608 */                   row.setValue(fatherIndex, null);
/*  609 */                   row.setValue(appIndex, appId);
/*  610 */                   row.setValue(winIndex, winId);
/*  611 */                   row.setValue(nameIndex, pmTitle);
/*  612 */                   ds.addRow(row);
/*      */                   
/*  614 */                   loadSecondGridData(ds, node, winId);
/*      */                 }
/*      */               } }
/*      */           }
/*      */       } else {
/*  619 */         String winId = getWinId(url);
/*  620 */         if (winId == null)
/*  621 */           return;
/*  622 */         LfwWindow pm = getPageMetaById(winId);
/*  623 */         if (pm != null) {
/*  624 */           String pmTitle = LanguageUtil.getWithDefaultByProductCode(pm.getLangDir(), pm.getCaption(), pm.getI18nName());
/*      */           
/*      */ 
/*  627 */           row = ds.getEmptyRow();
/*  628 */           row.setValue(pkIndex, node.getPk_appsnode() + winId);
/*  629 */           row.setValue(appIndex, appId);
/*  630 */           row.setValue(winIndex, winId);
/*  631 */           row.setValue(nameIndex, pmTitle);
/*  632 */           ds.addRow(row);
/*      */           
/*  634 */           loadSecondGridData(ds, node, winId);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   private void loadSecondGridData(Dataset ds, CpAppsNodeVO node, String winId)
/*      */   {
/*  642 */     Row row = ds.getEmptyRow();
/*  643 */     String pk = node.getPk_appsnode();
/*  644 */     String appId = node.getAppid();
/*      */     
/*  646 */     IPaPublicQryService paQryService = (IPaPublicQryService)ServiceLocator.getService(IPaPublicQryService.class);
/*  647 */     if ((pk == null) || (appId == null) || (winId == null)) {
/*  648 */       return;
/*      */     }
/*      */     
/*  651 */     String con0 = " and appid = '" + appId + "' ";
/*  652 */     String con1 = " and windowid = '" + winId + "' ";
/*  653 */     String conditon = "pk_funcnode = '" + pk + "' " + con0 + con1 + " and issystemplate != 'Y'";
/*      */     try
/*      */     {
/*  656 */       Collection<UwTemplateVO> tvos = paQryService.getTemplateVOByCondition(conditon);
/*      */       
/*  658 */       if ((tvos != null) && (tvos.size() > 0)) {
/*  659 */         Iterator<UwTemplateVO> it = tvos.iterator();
/*  660 */         while (it.hasNext()) {
/*  661 */           UwTemplateVO vo = (UwTemplateVO)it.next();
/*  662 */           if (vo != null) {
/*  663 */             row = ds.getEmptyRow();
/*  664 */             String name = vo.getTemplatename();
/*  665 */             String name2 = vo.getTemplatename2();
/*  666 */             String name3 = vo.getTemplatename3();
/*  667 */             String name4 = vo.getTemplatename4();
/*  668 */             String name5 = vo.getTemplatename5();
/*  669 */             String name6 = vo.getTemplatename6();
/*  670 */             if ((!StringUtils.isEmpty(name)) || (!StringUtils.isEmpty(name2)) || (!StringUtils.isEmpty(name3)) || (!StringUtils.isEmpty(name4)) || (!StringUtils.isEmpty(name5)) || (!StringUtils.isEmpty(name6)))
/*      */             {
/*      */ 
/*      */ 
/*  674 */               row.setValue(ds.nameToIndex("pk_template"), vo.getPk_template());
/*      */               
/*  676 */               row.setValue(ds.nameToIndex("appid"), vo.getAppid());
/*  677 */               row.setValue(ds.nameToIndex("windowid"), vo.getWindowid());
/*      */               
/*  679 */               row.setValue(ds.nameToIndex("templatename"), name);
/*  680 */               row.setValue(ds.nameToIndex("templatename2"), name2);
/*  681 */               row.setValue(ds.nameToIndex("templatename3"), name3);
/*  682 */               row.setValue(ds.nameToIndex("templatename4"), name4);
/*  683 */               row.setValue(ds.nameToIndex("templatename5"), name5);
/*  684 */               row.setValue(ds.nameToIndex("templatename6"), name6);
/*  685 */               row.setValue(ds.nameToIndex("isactive"), vo.getIsactive());
/*      */               
/*  687 */               row.setValue(ds.nameToIndex("pk_funcnode"), vo.getPk_funcnode());
/*      */               
/*  689 */               row.setValue(ds.nameToIndex("priority"), vo.getPriority());
/*      */               
/*  691 */               row.setValue(ds.nameToIndex("pk_device"), vo.getPk_device());
/*      */               
/*  693 */               row.setValue(ds.nameToIndex("fatherid"), node.getPk_appsnode() + winId);
/*      */               
/*  695 */               ds.addRow(row);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     } catch (LfwBusinessException e1) {
/*  701 */       CpLogger.error(e1.getMessage(), e1);
/*  702 */       throw new LfwRuntimeException(e1);
/*      */     }
/*      */   }
/*      */   
/*      */   private String getWinId(String url)
/*      */   {
/*  708 */     String[] splitUrl = url.split("/");
/*  709 */     String winId = null;
/*  710 */     if (splitUrl.length < 3)
/*  711 */       return winId;
/*  712 */     winId = splitUrl[2];
/*      */     
/*  714 */     if (winId.contains("?")) {
/*  715 */       String[] temp2 = winId.split("\\?");
/*  716 */       winId = temp2[0];
/*      */     }
/*      */     
/*  719 */     return winId;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void pluginedit_plugin(Map<Object, Object> keys)
/*      */   {
/*  728 */     LfwView widget = getCurrentWinCtx().getWindow().getView("main");
/*  729 */     Dataset ds = widget.getViewModels().getDataset(getMasterDsId());
/*      */     
/*      */ 
/*      */ 
/*  733 */     String operStatus = (String)getCurrentWinCtx().getAppAttribute("OPERATE_STATUS");
/*      */     
/*  735 */     if ("ADD_OPERATE".equals(operStatus)) {
/*  736 */       Row row = ds.getEmptyRow();
/*  737 */       setRowValue(row, ds, keys);
/*  738 */       String pk = (String)AppLifeCycleContext.current().getApplicationContext().getAppAttribute("pk_template");
/*      */       
/*  740 */       row.setValue(ds.nameToIndex("fatherid"), pk);
/*  741 */       ds.addRow(row);
/*  742 */       ds.setRowSelectIndex(Integer.valueOf(ds.getRowIndex(row)));
/*  743 */     } else if ("EDIT_OPERATE".equals(operStatus)) {
/*  744 */       Row row = ds.getSelectedRow();
/*  745 */       setRowValue(row, ds, keys);
/*  746 */       String fatherId = (String)row.getValue(ds.nameToIndex("fatherid"));
/*  747 */       setMenuState(fatherId);
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private Row setRowValue(Row row, Dataset ds, Map<Object, Object> map)
/*      */   {
/*  761 */     TranslatedRow r = (TranslatedRow)map.get("editRow");
/*  762 */     if (r == null)
/*  763 */       return null;
/*  764 */     String[] keys = r.getKeys();
/*  765 */     for (String key : keys) {
/*  766 */       row.setValue(ds.nameToIndex(key), r.getValue(key));
/*      */     }
/*  768 */     return row;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void onTemplateAssignEvent(MouseEvent<?> mouseEvent)
/*      */   {
/*  777 */     Dataset ds = getCurrentWinCtx().getViewContext("main").getView().getViewModels().getDataset(getMasterDsId());
/*      */     
/*  779 */     Row row = ds.getSelectedRow();
/*      */     
/*      */ 
/*      */ 
/*  783 */     if (row == null) {
/*  784 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000003"));
/*      */     }
/*      */     
/*      */ 
/*      */ 
/*  789 */     AppLifeCycleContext.current().getApplicationContext().navgateTo("cp_templateassign", NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000014"), "960", "560");
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void onAfterRowUnSelect(DatasetEvent datasetEvent) {}
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void pluginsimpleQuery_plugin(Map<Object, Object> keys)
/*      */   {
/*  811 */     FromWhereSQL whereSql = (FromWhereSQL)keys.get("whereSql");
/*      */     
/*  813 */     Dataset ds = AppUtil.getCntView().getViewModels().getDataset(getMasterDsId());
/*      */     
/*      */ 
/*  816 */     String nodeid = (String)AppLifeCycleContext.current().getApplicationContext().getAppAttribute("$Template_PK_Node");
/*  817 */     if (StringUtils.isEmpty(nodeid)) {
/*  818 */       String wheresql = whereSql.getWhere();
/*  819 */       StringBuffer buf = new StringBuffer();
/*  820 */       if (!StringUtils.isEmpty(wheresql))
/*  821 */         buf.append(wheresql).append("and");
/*  822 */       buf.append(" ( priority IS NOT NULL AND (pk_device IS NOT NULL and pk_device !='~') )");
/*  823 */       String curgroup = AppUtil.getPk_group();
/*  824 */       String pk_org = AppUtil.getPk_org();
/*  825 */       buf.append("and pk_funcnode in (select pk_appsnode from cp_appsnode where pk_group = '" + curgroup + "' and pk_org = '" + pk_org + "')");
/*  826 */       ds.setLastCondition(buf.toString());
/*  827 */       CmdInvoker.invoke(new UifDatasetLoadCmd(ds.getId()));
/*      */     }
/*      */     else
/*      */     {
/*  831 */       CpAppsNodeVO node = getCpAppsNodeVOBynodeid(nodeid);
/*  832 */       loadGridData(ds, node);
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void paInitEvent(MouseEvent<MenuItem> mouseEvent)
/*      */   {
/*  844 */     Dataset ds = getCurrentWinCtx().getViewContext("main").getView().getViewModels().getDataset(getMasterDsId());
/*      */     
/*  846 */     Row row = ds.getSelectedRow();
/*  847 */     if (row == null) {
/*  848 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000003"));
/*      */     }
/*      */     
/*      */ 
/*      */ 
/*  853 */     String pk = (String)row.getValue(ds.nameToIndex("pk_template"));
/*  854 */     String winId = row.getString(ds.nameToIndex("windowid"));
/*  855 */     IPaPublicQryService qry = (IPaPublicQryService)ServiceLocator.getService(IPaPublicQryService.class);
/*  856 */     String dftTplPk = qry.qrySysTemplate(pk);
/*  857 */     UifInitTemplateContentCmd cmd = null;
/*  858 */     if (dftTplPk == null) {
/*  859 */       cmd = new UifInitTemplateContentCmd(pk);
/*      */     } else
/*  861 */       cmd = new UifInitTemplateContentCmd(pk, dftTplPk, true);
/*  862 */     cmd.execute();
/*      */   }
/*      */   
/*      */   public void paShowOrginalEvent(MouseEvent<MenuItem> mouseEvent) {
/*  866 */     LfwView view = getCurrentWinCtx().getCurrentViewContext().getView();
/*  867 */     Dataset ds = view.getViewModels().getDataset(getMasterDsId());
/*  868 */     Row row = ds.getSelectedRow();
/*  869 */     if (row == null) {
/*  870 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000003"));
/*      */     }
/*  872 */     String pk = row.getString(ds.nameToIndex("pk_template"));
/*  873 */     String pk_funnode = row.getString(ds.nameToIndex("pk_funcnode"));
/*  874 */     String winId = row.getString(ds.nameToIndex("windowid"));
/*  875 */     String dftTmpPk = null;
/*  876 */     ICpAppsNodeQry appQry = (ICpAppsNodeQry)ServiceLocator.getService(ICpAppsNodeQry.class);
/*      */     try
/*      */     {
/*  879 */       if ((StringUtils.isBlank(pk_funnode)) && (!StringUtils.isBlank(pk))) {
/*  880 */         pk_funnode = pk.subSequence(0, 20).toString();
/*      */       }
/*  882 */       CpAppsNodeVO appNode = appQry.getNodeByPk(pk_funnode);
/*  883 */       dftTmpPk = getDefautTplPKByNodePKAndWinId(pk_funnode, winId);
/*      */     }
/*      */     catch (CpbBusinessException e) {
/*  886 */       CpLogger.error(e.getMessage(), e);
/*      */     }
/*      */     
/*      */ 
/*  890 */     String url = null;
/*  891 */     if (winId == null) {
/*  892 */       return;
/*      */     }
/*  894 */     if (pk == null) {
/*  895 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000007"));
/*      */     }
/*      */     
/*      */ 
/*      */ 
/*  900 */     LinkedHashMap<String, String> dimensions = new LinkedHashMap();
/*  901 */     dimensions.put("windowid", winId);
/*  902 */     url = PaHelper.createPaURL(dimensions, ModePhase.pa);
/*  903 */     if (dftTmpPk != null) {
/*  904 */       url = url + "&pk_template=" + dftTmpPk;
/*      */     }
/*      */     
/*  907 */     String script = "window.open('" + url + "&showOriginal=0" + "')";
/*  908 */     AppLifeCycleContext.current().getWindowContext().addExecScript(script);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void templateExport(MouseEvent<MenuItem> mouseEvent)
/*      */   {
/*  922 */     UifTemplateExportCmd cmd = new UifTemplateExportCmd(getSelectedPktemplate());
/*      */     
/*  924 */     cmd.execute();
/*      */   }
/*      */   
/*      */   private String getSelectedPktemplate() {
/*  928 */     Dataset ds = getCurrentWinCtx().getViewContext("main").getView().getViewModels().getDataset(getMasterDsId());
/*      */     
/*  930 */     Row row = ds.getSelectedRow();
/*  931 */     if (row == null) {
/*  932 */       throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("bd", "PaMgrMainViewController-000003"));
/*      */     }
/*      */     
/*      */ 
/*      */ 
/*  937 */     return (String)row.getValue(ds.nameToIndex("pk_template"));
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   public void paCopyEvent(MouseEvent<MenuItem> mouseEvent)
/*      */   {
/*  945 */     LfwView view = AppLifeCycleContext.current().getViewContext().getView();
/*  946 */     Dataset ds = view.getViewModels().getDataset(getMasterDsId());
/*  947 */     Row row = ds.getSelectedRow();
/*  948 */     if (row == null) {
/*  949 */       throw new LfwRuntimeException(LfwResBundle.getInstance().getStrByID("imp", "PaMgrMainViewController-000002"));
/*      */     }
/*  951 */     String tmpPk = (String)row.getValue(ds.nameToIndex("pk_template"));
/*  952 */     IPaPublicQryService paQryService = (IPaPublicQryService)ServiceLocator.getService(IPaPublicQryService.class);
/*  953 */     UwTemplateVO tmpltVO = null;
/*      */     try {
/*  955 */       tmpltVO = paQryService.getTemplateVOByPK(tmpPk);
/*      */       
/*  957 */       UwViewVO[] views = paQryService.getViewByTemplateId(tmpPk);
/*      */       
/*  959 */       if ((tmpltVO != null) && (views != null) && (views.length > 0)) {
/*  960 */         AppLifeCycleContext.current().getApplicationContext().addAppAttribute("copyTmpVO", tmpltVO);
/*  961 */         AppLifeCycleContext.current().getApplicationContext().addAppAttribute("copyTmpViews", views);
/*      */       }
/*      */     } catch (LfwBusinessException e) {
/*  964 */       CpLogger.error(e.getMessage(), e);
/*  965 */       throw new LfwRuntimeException(e.getMessage(), e);
/*      */     }
/*  967 */     AppInteractionUtil.showMessageDialog(LfwResBundle.getInstance().getStrByID("imp", "PaMgrMainViewController-000003"));
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */   public void paPasteEvent(MouseEvent<MenuItem> mouseEvent)
/*      */   {
/*  974 */     UwTemplateVO copyTmpVO = (UwTemplateVO)AppLifeCycleContext.current().getApplicationContext().getAppAttribute("copyTmpVO");
/*  975 */     if (copyTmpVO == null) {
/*  976 */       throw new LfwRuntimeException(LfwResBundle.getInstance().getStrByID("imp", "PaMgrMainViewController-000004"));
/*      */     }
/*  978 */     UwViewVO[] views = (UwViewVO[])AppLifeCycleContext.current().getApplicationContext().getAppAttribute("copyTmpViews");
/*  979 */     IPaPublicQryService paQryService = (IPaPublicQryService)ServiceLocator.getService(IPaPublicQryService.class);
/*  980 */     IPaPublicService paService = (IPaPublicService)ServiceLocator.getService(IPaPublicService.class);
/*      */     try
/*      */     {
/*  983 */       InputItem item = new StringInputItem("templateName", LfwResBundle.getInstance().getStrByID("imp", "PaMgrMainViewController-000005"), false);
/*  984 */       AppInteractionUtil.showInputDialog("inputId", LfwResBundle.getInstance().getStrByID("imp", "PaMgrMainViewController-000006"), new InputItem[] { item });
/*      */       
/*  986 */       Map<String, String> rs = AppInteractionUtil.getInputDialogResult("inputId");
/*  987 */       String templateName = (String)rs.get("templateName");
/*  988 */       if (StringUtils.isBlank(templateName)) {
/*  989 */         throw new LfwRuntimeException(LfwResBundle.getInstance().getStrByID("imp", "PaMgrMainViewController-000007"));
/*      */       }
/*  991 */       String constCondition = "windowid = '" + copyTmpVO.getWindowid() + "' and appid = '" + copyTmpVO.getAppid() + "'";
/*  992 */       String condition = constCondition + " and templatename = '" + templateName + "'";
/*  993 */       Collection<UwTemplateVO> vos = paQryService.getTemplateVOByCondition(condition);
/*  994 */       if ((vos != null) && (vos.size() > 0)) {
/*  995 */         throw new LfwRuntimeException(LfwResBundle.getInstance().getStrByID("imp", "PaMgrMainViewController-000008"));
/*      */       }
/*      */       
/*      */ 
/*      */ 
/*      */ 
/* 1001 */       UwTemplateVO pasteVO = copyTmpVO;
/* 1002 */       pasteVO.setPk_template(null);
/* 1003 */       pasteVO.setTemplatename(templateName);
/* 1004 */       if (null == pasteVO.getIssystemplate()) {
/* 1005 */         pasteVO.setIssystemplate("~");
/*      */       }
/* 1007 */       String newPk = paService.insertTemplateVO(pasteVO);
/*      */       
/* 1009 */       if ((views != null) && (views.length > 0)) {
/* 1010 */         for (int i = 0; i < views.length; i++) {
/* 1011 */           UwViewVO viewVo = views[i];
/* 1012 */           UwViewVO newView = (UwViewVO)viewVo.clone();
/* 1013 */           newView.setPrimaryKey(null);
/* 1014 */           if (newPk != null) {
/* 1015 */             newView.setPk_template(newPk);
/* 1016 */             paService.insertViewVO(newView);
/*      */           }
/*      */         }
/*      */       }
/*      */       
/*      */ 
/*      */ 
/* 1023 */       String nodeid = (String)AppLifeCycleContext.current().getApplicationContext().getAppAttribute("$Template_PK_Node");
/* 1024 */       LfwView view = AppLifeCycleContext.current().getViewContext().getView();
/* 1025 */       Dataset ds = view.getViewModels().getDataset(getMasterDsId());
/* 1026 */       if (StringUtils.isEmpty(nodeid)) {
/* 1027 */         ds.clear();
/*      */       }
/*      */       else {
/* 1030 */         CpAppsNodeVO node = getCpAppsNodeVOBynodeid(nodeid);
/*      */         
/* 1032 */         loadGridData(ds, node);
/*      */ 
/*      */       }
/*      */       
/*      */ 
/*      */     }
/*      */     catch (LfwBusinessException e)
/*      */     {
/*      */ 
/* 1041 */       CpLogger.error(e.getMessage(), e);
/* 1042 */       throw new LfwRuntimeException(e.getMessage(), e);
/*      */     }
/*      */     
/*      */ 
/* 1046 */     AppInteractionUtil.showMessageDialog(LfwResBundle.getInstance().getStrByID("imp", "PaMgrMainViewController-000009"));
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void templateImport(MouseEvent mouseEvent)
/*      */   {
/* 1055 */     String pk_template = getSelectedPktemplate();
/* 1056 */     String url = LfwRuntimeEnvironment.getRootPath() + "/core/file.jsp?pageId=file&iscover=false&closeDialog=false&closeDialog=false&isalert=false&method=showMessageDialog&billitem=" + pk_template + "&uploadurl=/portal/pt/portaltemplate/importTemplate;jsessionid=" + LfwRuntimeEnvironment.getWebContext().getRequest().getSession().getId() + "&fileExt=*.zip;*.bin&fileDesc=xml";
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/* 1062 */     AppLifeCycleContext.current().getApplicationContext().showModalDialog(url, NCLangRes4VoTransl.getNCLangRes().getStrByID("uapprinttemplate", "CpPrintInitCtrl-000006"), "502", "390", "templateImportId", "TYPE_DIALOG");
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private void refreshTemplateRelation(String pk_template)
/*      */   {
/* 1075 */     AppLifeCycleContext.current().getApplicationContext().addAppAttribute("pk_template", pk_template);
/*      */     
/*      */ 
/* 1078 */     AppLifeCycleContext.current().getApplicationContext().addAppAttribute("template_type", Integer.valueOf(1));
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/* 1084 */     UifPlugoutCmd cmd = new UifPlugoutCmd("main", "main_plugout");
/*      */     
/* 1086 */     cmd.execute();
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */   public void pluginnodePlugin(Map paramMap)
/*      */   {
/* 1093 */     String nodeid = (String)AppLifeCycleContext.current().getApplicationContext().getAppAttribute("$Template_PK_Node");
/*      */     
/* 1095 */     AppLifeCycleContext.current().getApplicationContext().addAppAttribute("templatenodecode", nodeid);
/*      */     
/* 1097 */     LfwView main = AppLifeCycleContext.current().getWindowContext().getViewContext("main").getView();
/* 1098 */     Dataset ds = main.getViewModels().getDataset(getMasterDsId());
/*      */     
/* 1100 */     if (StringUtils.isEmpty(nodeid)) {
/* 1101 */       ds.clear();
/*      */     } else {
/* 1103 */       String pk_org = ImpAppUtils.getApplicationContext().getAppEnvironment().getPk_org();
/* 1104 */       if (StringUtils.isEmpty(pk_org)) {
/* 1105 */         ds.clear();
/*      */       }
/*      */       else {
/* 1108 */         CpAppsNodeVO node = getCpAppsNodeVOBynodeid(nodeid);
/* 1109 */         loadGridData(ds, node);
/*      */       }
/*      */     }
/*      */     
/* 1113 */     refreshTemplateRelation("");
/*      */   }
/*      */ }

/* Location:           D:\home\nchome190719\nchome\modules\webimp\classes
 * Qualified Name:     nc.uap.ctrl.tpl.pamgr.PaMgrMainViewController
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.0.1
 */
