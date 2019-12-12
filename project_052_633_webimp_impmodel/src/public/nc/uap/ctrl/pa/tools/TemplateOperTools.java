package nc.uap.ctrl.pa.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nc.uap.cpb.log.CpLogger;
import nc.uap.ctrl.pa.itf.IPaPublicQryService;
import nc.uap.ctrl.pa.itf.IPaPublicService;
import nc.uap.lfw.core.ContextResourceUtil;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.exception.LfwBusinessException;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.uap.lfw.core.util.JaxbMarshalFactory;
import nc.uap.lfw.pa.PaBusinessException;
import nc.uap.lfw.stylemgr.vo.UwTemplateVO;
import nc.uap.lfw.stylemgr.vo.UwViewVO;

import org.apache.commons.io.IOUtils;

import uap.lfw.core.locator.ServiceLocator;

/**
 * 模板操作类
 * 
 * @author licza
 * 
 */
public class TemplateOperTools {
	private static final String EXPORTFILES = "exportfiles";

	/**
	 * 导出模板
	 * 
	 * @param pk_template
	 * @return
	 */
	public static String doExport(String pk_template) {
		
		
		
		
//
///*  44 */     FileOutputStream out = null;
///*     */     try {
///*  46 */       TemplatePackObj pack = pack(pk_template);
///*  47 */       String xml = JaxbMarshalFactory.newIns().decodeXML(pack);
///*  48 */       String rootPath = getRootPath();
///*  49 */       String path = rootPath + "/" + "exportfiles";
///*  50 */       File dir = new File(path);
///*  51 */       if (!dir.exists())
///*  52 */         dir.mkdirs();
///*  53 */       String fileName = UUID.randomUUID().toString() + ".zip";
///*  54 */       out = new FileOutputStream(path + "/" + fileName);
///*  55 */       IOUtils.write(xml, out);
///*  56 */       return "exportfiles/" + fileName;
///*     */     }
///*     */     catch (IOException e) {
///*  59 */       throw new LfwRuntimeException("write pack error!");
///*     */     }
///*     */     catch (LfwBusinessException e) {
///*  62 */       throw new LfwRuntimeException("pack template error!");
///*     */     } finally {
///*  64 */       IOUtils.closeQuietly(out);
///*     */     }
///*     */   
		
		
		 	
		FileOutputStream out = null;
		try {
			TemplatePackObj pack = pack(pk_template);
			String xml = JaxbMarshalFactory.newIns().decodeXML(pack);
			String rootPath = getRootPath();
			String path = rootPath + "/" + EXPORTFILES;
			File dir = new File(path);
			if (!dir.exists())
				dir.mkdirs();
//			UwTemplateVO vo = getQryTmpService().getTemplateVOByPK(pk_template);
			String fileName =UUID.randomUUID().toString() + ".zip";
			out = new FileOutputStream(path + "/" + fileName);
			IOUtils.write(xml, out);
			return EXPORTFILES + "/" + fileName;
		} catch (IOException e) {
			CpLogger.error(e);
			throw new LfwRuntimeException("write pack error!");
		} catch (LfwBusinessException e) {
			CpLogger.error(e);
			throw new LfwRuntimeException("pack template error!");
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	/**
	 * 执行导入操作
	 * @param in
	 * @param pk_template
	 * @throws IOException 
	 * @throws PaBusinessException 
	 */
	public static void doImPort(InputStream in, String pk_template) throws IOException, LfwBusinessException{
		String xml = IOUtils.toString(in);
		TemplatePackObj packObj = JaxbMarshalFactory.newIns().encodeXML(TemplatePackObj.class, xml);
		if(packObj==null){
			throw new LfwBusinessException(" file content illegal ");
		}
		UwTemplateVO tmplate = getQryTmpService().getTemplateVOByPK(pk_template);
		if(tmplate == null || !tmplate.getWindowid().equals(packObj.getId()))
			throw new LfwRuntimeException("File id is "+ packObj.getId() +",but this template window id is "+ tmplate.getWindowid() +"!");
		
		tmplate.doSetPageMetaStr(packObj.getPagemeta());
		tmplate.doSetUIMetaStr(packObj.getUimeta());
		
		Map<String, ViewPackObj> viewMap = mapable(packObj);
		UwViewVO[] views = getQryTmpService().getViewByTemplateId(pk_template);
		List<UwViewVO> updateViews = new ArrayList<UwViewVO>();
		if (views != null && views.length > 0) {
			for (UwViewVO view : views) {
				ViewPackObj viewPack = viewMap.get(view.getViewid());
				//央客：王志强，特殊处理"glcw_listwin" 模板
				if(viewPack == null){
					if(	"glcw_listwin".equals(view.getViewid())){
						continue;
					}else{
						throw new LfwRuntimeException("Lost view : " + view.getViewid() + "!");
					}
				
				
				}
				//**********end
				
				 
				//old if(viewPack == null)				
				//old		throw new LfwRuntimeException("Lost view : " + view.getViewid() + "!");
				
					
				view.doSetUIMetaStr(viewPack.getUimeta());
				view.doSetWidgetStr(viewPack.getWidget());
				updateViews.add(view);
			}
		}
		//need marge tx
		try {
			//央客：王志强，特殊处理"glcw_listwin" 模板导入
			views = updateViews.toArray(new UwViewVO[0]);
			//end
			getTmpService().updateViewVO(views);
			getTmpService().updateTemplateVO(tmplate);
		} catch (Exception e) {
			CpLogger.error(e.getMessage(), e);
			throw new LfwRuntimeException("Import template error : " + e.getMessage());
		}
	}
	
	/**
	 * 将View列表转换成Map
	 * @param packObj
	 * @return
	 */
	private static Map<String, ViewPackObj> mapable(TemplatePackObj packObj){
		Map<String, ViewPackObj> viewMap = new HashMap<String, ViewPackObj>();
		List<ViewPackObj> viewlist = packObj.getViewlist();
		if(viewlist != null && viewlist.size() > 0){
			for(ViewPackObj view : viewlist){
				viewMap.put(view.getId() , view);
			}
		}
		return viewMap;
	}
	
	/**
	 * 获取根路径
	 * 
	 * @return
	 */
	private static String getRootPath() {
		boolean inTest = LfwRuntimeEnvironment.getWebContext() == null;
		String rootPath = inTest ? "d:" : ContextResourceUtil
				.getCurrentAppPath();
		return rootPath;
	}

	/**
	 * Pack Template to pack vo
	 * 
	 * @param pk_template
	 * @return
	 * @throws PaBusinessException
	 */
	private static TemplatePackObj pack(String pk_template)	throws LfwBusinessException {
		TemplatePackObj pack = new TemplatePackObj();
		UwTemplateVO vo = getQryTmpService().getTemplateVOByPK(pk_template);
		pack.setId(vo.getWindowid());
		pack.setPagemeta(vo.doGetPageMetaStr());
		pack.setUimeta(vo.doGetUIMetaStr());
		UwViewVO[] views = getQryTmpService().getViewByTemplateId(pk_template);
		if (views != null && views.length > 0) {
			for (UwViewVO view : views) {
				ViewPackObj viewPack = new ViewPackObj();
				viewPack.setId(view.getViewid());
				viewPack.setUimeta(view.doGetUIMetaStr());
				viewPack.setWidget(view.doGetWidgetStr());
				pack.getViewlist().add(viewPack);
			}
		}
		return pack;
	}

	private static IPaPublicQryService qryService = null;
	private static IPaPublicService tmpService = null;

	/**
	 * get Template Service
	 * 
	 * @return
	 */
	public static IPaPublicQryService getQryTmpService() {
		if (qryService == null) {
			synchronized (TemplateOperTools.class) {
				if (qryService == null){
					qryService = ServiceLocator.getService(IPaPublicQryService.class);
				}
			}
		}
		return qryService;
	}
	
	public static IPaPublicService getTmpService() {
		if (tmpService == null) {
			synchronized (TemplateOperTools.class) {
				if (tmpService == null){
					tmpService = ServiceLocator.getService(IPaPublicService.class);
				}
			}
		}
		return tmpService;
	}

}
