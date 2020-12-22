package nccloud.web.uapbd.ref.userdef;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import nc.bs.logging.Logger;
import nc.md.MDBaseQueryFacade;
import nc.md.model.IBean;
import nc.vo.bd.defdoc.DefdocVO;
import nc.vo.pub.BusinessException;
import nccloud.commons.lang.StringUtils;
import nccloud.framework.core.json.IJson;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.json.JsonFactory;
import nccloud.framework.web.processor.refgrid.RefQueryInfo;
import nccloud.framework.web.ui.meta.RefMeta;
import nccloud.framework.web.ui.meta.TreeRefMeta;
import nccloud.pubitf.platform.db.SqlParameterCollection;
import nccloud.pubitf.web.refer.INCCRefQry;
import nccloud.web.refer.DefaultTreeRefAction;
import nccloud.web.refer.IRefConst;

/***************************************************************************
 * 自定义档案(树)<br>
 * @author Rocex Wang
 * @version 2018-5-15 10:44:10
 * @see nc.ui.bd.ref.model.DefdocPkTreeRefModel
 ***************************************************************************/
public class DefdocTreeRef extends DefaultTreeRefAction
{
    public DefdocTreeRef()
    {
        super();
        
        setShowDisabledData(false);
        setUnitPkKey(IRefConst.KeyPkOrg);
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see nccloud.web.refer.DefaultGridRefAction#getExtraSql(nccloud.framework.web.processor.refgrid.RefQueryInfo,
     * nccloud.framework.web.ui.meta.RefMeta)
     * @author Rocex Wang
     * @version 2018-5-15 10:45:05
     ****************************************************************************/
    @Override
    public String getExtraSql(RefQueryInfo refQueryInfo, RefMeta refMeta)
    {
    	
		
		String queryStr = " and pk_defdoclist=?";
		
 		if("SCM001".equals(this.getResourceCode())) {
 			
 			String key = "vdef33Val";
 			String appCode = "appcode";
 		

 			if(refQueryInfo.getQueryCondition().get(key)!=null&&refQueryInfo.getQueryCondition().get(key).length()>10) {
 				String value = refQueryInfo.getQueryCondition().get(key);
 	 			try {
 	 				String jsonStr = refQueryInfo.getQueryCondition().get("crossRuleParams");
 	 	 			IJson json = JsonFactory.create();
 	 	 			JsonObject jsonObj =  json.fromJson(jsonStr,JsonObject.class);
 	 	 			
 	 	 
 	 	 			value = ((JsonObject)json.fromJson(jsonObj.get("headdata").getAsString(),JsonObject.class)).get("head").getAsJsonObject().get("rows").getAsJsonArray().get(0).getAsJsonObject().get("values").getAsJsonObject().get("vdef1").getAsJsonObject().get("value").toString();
 	 	 			value = value.replaceAll("\"", "");
 	 			}catch(Exception ex) {
 	 				
 	 			}
 				
 				
 				queryStr  = queryStr +" and pid='"+value+"'";
 			}else if(refQueryInfo.getQueryCondition().get(appCode)!=null&&refQueryInfo.getQueryCondition().get(appCode).length()>1) {
// 			}else {
 				queryStr = queryStr+ "  and length(innercode)<=12";
 			}
 		}

		return queryStr;
//        return " and pk_defdoclist=?";
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see nccloud.web.refer.DefaultGridRefAction#getExtraSqlParameter(nccloud.framework.web.processor.refgrid.RefQueryInfo,
     * nccloud.framework.web.ui.meta.RefMeta)
     * @author Rocex Wang
     * @version 2018-5-15 10:45:08
     ****************************************************************************/
    @Override
    public SqlParameterCollection getExtraSqlParameter(RefQueryInfo refQueryInfo, RefMeta refMeta)
    {
        SqlParameterCollection para = new SqlParameterCollection();
        
        para.addChar(getPk_defdoclist(refQueryInfo));
        
        return para;
    }
    
    /***************************************************************************
     * @param refQueryInfo
     * @return
     * @author Rocex Wang
     * @version 2018-5-15 10:45:19
     ***************************************************************************/
    protected String getPk_defdoclist(RefQueryInfo refQueryInfo)
    {
        return getQueryValue(refQueryInfo, "pk_defdoclist");
    }
    
    /****************************************************************************
     * {@inheritDoc}<br>
     * @see nccloud.web.refer.DefaultTreeRefAction#getRefMeta(nccloud.framework.web.processor.refgrid.RefQueryInfo)
     * @author Rocex Wang
     * @version 2018-5-15 10:45:16
     ****************************************************************************/
    @Override
    public TreeRefMeta getRefMeta(RefQueryInfo refQueryInfo)
    {
        try
        {
            setResourceCode(ServiceLocator.find(INCCRefQry.class).getDefdocRefResourceId(getPk_defdoclist(refQueryInfo)));
        }
        catch (BusinessException ex)
        {
            Logger.error(ex.getMessage(), ex);
        }
        
        setMdClassId(getPk_defdoclist(refQueryInfo));
        
        TreeRefMeta refMeta = new TreeRefMeta();
        
        refMeta.setCodeField(DefdocVO.CODE);
        refMeta.setNameField(DefdocVO.NAME);
        refMeta.setPkField(DefdocVO.PK_DEFDOC);
        refMeta.setPidField(DefdocVO.PID);
        refMeta.setChildField(DefdocVO.PK_DEFDOC);
        refMeta.setInnercodeField(DefdocVO.INNERCODE);
        refMeta.setExtraFields(new String[]{DefdocVO.CODE, DefdocVO.NAME, DefdocVO.SHORTNAME, DefdocVO.MNECODE, DefdocVO.ENABLESTATE});
        refMeta.setMutilLangNameRef(true);
        refMeta.setTableName(getTableName(getPk_defdoclist(refQueryInfo)));
        
        return refMeta;
    }
    
    /***************************************************************************
     * @param pk_defdoclist
     * @return
     * @author Rocex Wang
     * @version 2018-5-15 10:45:32
     ***************************************************************************/
    protected String getTableName(String pk_defdoclist)
    {
        if (StringUtils.isNotBlank(pk_defdoclist))
        {
            try
            {
                IBean bean = MDBaseQueryFacade.getInstance().getBeanByID(pk_defdoclist);
                
                if (bean != null)
                {
                    return bean.getTable().getName();
                }
            }
            catch (Exception e)
            {
                Logger.error(e);
            }
        }
        
        return "bd_defdoc";
    }
}
