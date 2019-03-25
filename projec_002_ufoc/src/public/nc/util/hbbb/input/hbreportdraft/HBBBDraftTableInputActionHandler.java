package nc.util.hbbb.input.hbreportdraft;

import info.monitorenter.cpdetector.util.collections.ITreeNode.DefaultTreeNode;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.tree.DefaultMutableTreeNode;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.bd.cust.baseinfo.ICustSupQueryService;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.itf.hbbb.linkend.ILinkEndQuery;
import nc.itf.hbbb.vouch.constants.IVouchType;
import nc.itf.hbbb.workdrafttemp.IWorkDraftConst;
import nc.itf.iufo.data.IRepDataQuerySrv;
import nc.itf.org.IBasicOrgUnitQryService;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.MeasureCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.bd.intdata.MultiLangTextUtil;
import nc.util.hbbb.HBKeyGroupUtil;
import nc.util.hbbb.HBVersionUtil;
import nc.util.hbbb.MeasurePubDataUtil;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.input.HBBBTableInputActionHandler;
import nc.util.hbbb.measure.MeasureUtil;
import nc.util.hbbb.pub.HBPubItfService;
import nc.util.hbbb.pub.util.StrTools;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.util.hbbb.workdraft.head.ExCell;
import nc.util.hbbb.workdraft.head.IProjectCellHead;
import nc.util.hbbb.workdraft.head.ProjectCellHeadFactory;
import nc.util.hbbb.workdraft.head.ProjectHead;
import nc.util.hbbb.workdraft.pub.IWorkDraft;
import nc.vo.bd.cust.CustSupplierVO;
import nc.vo.hbbb.adjustscheme.AdjustSchemeVO;
import nc.vo.hbbb.dxtype.DXTypeValue;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.vouch.VouchHeadVO;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.iufo.verctrl.IDataVersionConsts;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.workdrafttemp.DXTypeSeqVO;
import nc.vo.ufoc.workdrafttemp.MeasureInfoVO;
import nc.vo.ufoc.workdrafttemp.WorkDraftTempVO;
import nc.vo.vorg.ReportCombineStruVersionVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.report.IufoFormat;
import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.Cell;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.format.CellFont;
import com.ufsoft.table.format.ICellFont;
import com.ufsoft.table.format.IFormat;

/**
 * 处理合并报表底稿显示
 * @date 20110602
 * @author liyra
 * @modify litfb@20120515 修改dataMap数据结构为ConcurrentHashMap<String, Object>,由于存在指标类型不是数值
 */
public class HBBBDraftTableInputActionHandler extends HBBBTableInputActionHandler {

    /** 存放公司对应的指标,数据*/
    private Map<String, ConcurrentHashMap<String, Object>> srcOrgMeasDataMap = null;
    /** 存放合计数*/
    private Map<String, Object> totalmap = null;
    /** 合并公司的抵销借方数据<报表指标,值>*/
    private Map<String, Object> debitmap = null;
    /** 合并公司的抵销贷方数据<报表指标,值>*/
    private Map<String, Object> creditmap = null;
    /**分类抵销借贷值*/
    private Map<String, Object[]> typeCreDebMap = new LinkedHashMap<String, Object[]>();
    /**合并报表指标数据*/
    private ConcurrentHashMap<String, Object> unionMeasdatamap = null;
    /**打开工作底稿传递过来的参数：合并版本pubdata*/
    private MeasurePubDataVO paramPubData = null;
    // TODO: 禁用，如何记住已选单位
    private static String[] showorgs = null;
    
    /**报表指标vo*/
    protected MeasureVO[] loadMeasureByReportPK = null;

    protected HBSchemeVO schemevo = null;

    protected List<Cell[]> lstAll = null;


    //动态表原表数据<pk_selforg+pk_opporg,map<code,value>>
    protected Map<String, Map<String, UFDouble>> srcDynRepMap = new HashMap<String, Map<String,UFDouble>>();
    //动态表的合计数
    protected Map<String, Map<String, UFDouble>> dynTotalMap = new HashMap<String, Map<String,UFDouble>>();
    //动态表的抵销数
    protected Map<String, Map<String, UFDouble>> dynDXContrastMap = new HashMap<String, Map<String,UFDouble>>();
    //动态表的合并数
    protected Map<String, Map<String, UFDouble>> dynUnionMap = new HashMap<String, Map<String,UFDouble>>();



    /**
     * 返回合并报表各子公司相应版本的MeasurePubDataVO
     * @param pk_org
     * @param isSingleTab//是否取个别类报表：当前合并组织和叶子节点取个别类报表，其他类组织取合并类报表
     * @param isSave
     * @param pk_report
     * @return
     * @throws Exception
     */
    protected MeasurePubDataVO getMeasureData(String pk_org, boolean isSingleTab,String pk_report)throws Exception {
        MeasurePubDataVO pubdata = new MeasurePubDataVO();
        pubdata.setKType(getHbschemevo().getPk_keygroup());
        pubdata.setAccSchemePK(getHbschemevo().getPk_accperiodscheme());
        KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(getHbschemevo().getPk_keygroup());
        pubdata.setKeyGroup(keygroupVo);
        pubdata.setKeywordByPK(KeyVO.CORP_PK, pk_org);
        //设置除组织以为的其他关键字的值
        pubdata = MeasurePubDataUtil.getMeasurePubData(pubdata, getParamPubData(), new String[] { KeyVO.CORP_PK });

        pubdata.setVer(0);
        //取数版本：个别类报表
        if (isSingleTab) {
            if (getHbschemevo().getPk_adjustscheme() !=  null) {
            	AdjustSchemeVO adjustSchemeVo = (AdjustSchemeVO) HBBaseDocItfService.getRemoteUAPQueryBS().retrieveByPK(
                        AdjustSchemeVO.class, getHbschemevo().getPk_adjustscheme());
                pubdata.setVer(adjustSchemeVo.getVersion());
                pubdata.setAloneID(MeasurePubDataBO_Client.getAloneID(pubdata));
                boolean existadjrep = HBPubItfService.getRemoteAdjReport().existAdjReportByAloneid(pubdata.getAloneID(), pk_report);
                //存在调整报表则取调整报表，否则取个别报表
                if (!existadjrep) {
                    pubdata.setVer(0);
                }
            }
        } else {
            pubdata.setVer(HBVersionUtil.getHBAdjustByHBSchemeVO(getHbschemevo()));
            pubdata.setAloneID(MeasurePubDataBO_Client.getAloneID(pubdata));
            boolean existadjrep = HBPubItfService.getRemoteAdjReport().existAdjReportByAloneid(pubdata.getAloneID(), pk_report);
            if (!existadjrep) {
                pubdata.setVer(getHbschemevo().getVersion());//取合并报表
            }
        }
        pubdata.setAloneID(null);
        pubdata.setAloneID(MeasurePubDataBO_Client.getAloneID(pubdata));
        return pubdata;
    }
    
    private MeasurePubDataVO createMeasureData(String pk_org,HBSchemeVO scheme,MeasurePubDataVO currentPubData,String pk_report,int ver){
    	
    	  MeasurePubDataVO rtnPubData = new MeasurePubDataVO();
          rtnPubData.setKType(scheme.getPk_keygroup());
          rtnPubData.setAccSchemePK(scheme.getPk_accperiodscheme());
          KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(scheme.getPk_keygroup());
          rtnPubData.setKeyGroup(keygroupVo);
          rtnPubData.setKeywordByPK(KeyVO.CORP_PK, pk_org);
          //设置除组织以为的其他关键字的值
          rtnPubData = MeasurePubDataUtil.getMeasurePubData(rtnPubData,currentPubData, new String[] { KeyVO.CORP_PK });
          rtnPubData.setVer(ver);
          try {
			rtnPubData.setAloneID(MeasurePubDataBO_Client.getAloneID(rtnPubData));
		} catch (Exception e) {
			throw new BusinessRuntimeException("通过公用关键字查询AloneId错误！");
		}
          return rtnPubData;
    
    	
    }

    /**
     * 设置合并组织的每个组织的数据
     * @param corps
     * @param unionorg
     * @param pk_report
     * @param Pk_hbrepstru
     * @throws BusinessException
     */
    private void setSrcReportVOS(String[] corps, String unionorg, String pk_report, String Pk_hbrepstru)
            throws BusinessException {
    	//得到虚实对应关系
    	String[] orgs = new String[corps.length];
    	for (int i = 0 ; i < corps.length ; i++){
    		orgs[i] = corps[i].substring(0, 20);
    	}
    	Map<String, UFBoolean> map = HBRepStruUtil.getBooleanEntityOrgs(orgs, Pk_hbrepstru);
    	
        for (String pk_org : corps) {
            try {
            	
                String innercode = pk_org.substring(20, pk_org.trim().length());
                pk_org = pk_org.substring(0, 20);
                boolean isXuUnit = map.get(pk_org) == null ? false : map.get(pk_org).booleanValue();
            	//JIAAH 虚单位，并且是当前的合并组织的情况下，个别报表数据设置为null
            	if(isXuUnit && pk_org.equals(unionorg)){
            		 getSrcOrgMeasDataMap().put(pk_org, new ConcurrentHashMap<String, Object>());
            		 continue;
            	}
                RepDataVO[] vos = null;
                MeasurePubDataVO pubdata = null;
                //当前组织是否存在下级
                if (HBPubItfService.getRemoteUnionReport().hasSubOrgs(innercode, Pk_hbrepstru)) {
                    if (pk_org.equals(unionorg)) {//是否是当前合并组织
                    	pubdata = getMeasureData(pk_org, true,pk_report);
                    } else {
                    	pubdata = getMeasureData(pk_org, false,pk_report);
                    }
                } else {
                    pubdata = getMeasureData(pk_org,true,pk_report);
                }
                vos = HBPubItfService.getRemoteRepDataQry().loadRepData(pk_report, pk_org, pubdata, Pk_hbrepstru);
                ConcurrentHashMap<String, Object> srcdatamap = IProjectCellHead.getMeasdataMap(vos[0]);
                getSrcOrgMeasDataMap().put(pk_org, srcdatamap);
            } catch (UFOSrvException e) {
                nc.bs.logging.Logger.error(e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
    }

    /**
     * 设置动态表的每个组织对的数据
     * @param corps
     * @param unionorg
     * @param pk_report
     * @param Pk_hbrepstru
     * @throws BusinessException
     */
    protected void setSrcDynReportVOS(String[] corps, String unionorg, String pk_report, String Pk_hbrepstru)
            throws BusinessException {
    	//得到虚实对应关系
    	String[] orgs = new String[corps.length];
    	for (int i = 0 ; i < corps.length ; i++){
    		orgs[i] = corps[i].substring(0, 20);
    	}
    	Map<String, UFBoolean> entityorg = HBRepStruUtil.getBooleanEntityOrgs(orgs, Pk_hbrepstru);
    	
        for (String pk_org : corps) {
            try {
                String innercode = pk_org.substring(20, pk_org.trim().length());
                pk_org = pk_org.substring(0, 20);
                boolean isXuUnit = entityorg.get(pk_org) == null ? false : entityorg.get(pk_org).booleanValue();
                
                RepDataVO[] vos = null;
                MeasurePubDataVO pubdata = null;
                //当前组织是否存在下级
                if (HBPubItfService.getRemoteUnionReport().hasSubOrgs(innercode, Pk_hbrepstru)) {
                    if (pk_org.equals(unionorg)) {//是否是当前合并组织
                    	pubdata = getMeasureData(pk_org, true,pk_report);
                    } else {
                    	pubdata = getMeasureData(pk_org, false,pk_report);
                    }
                } else {
                    pubdata = getMeasureData(pk_org,true,pk_report);
                }
                vos = HBPubItfService.getRemoteRepDataQry().loadRepData(pk_report, pk_org, pubdata, Pk_hbrepstru);
                MeasurePubDataVO[] allPubDataVos = vos[0].getPubDatas();
                Set<MeasurePubDataVO> dynPubDataVos = new HashSet<MeasurePubDataVO>();
                for(MeasurePubDataVO vo : allPubDataVos){
                	if(!vo.getKType().equals(pubdata.getKType())){
                		dynPubDataVos.add(vo);

                	}
                }

                
                for(MeasurePubDataVO vo : dynPubDataVos){
                	MeasureDataVO[] datas = vos[0].getMeasureDatas(vo);
                	if (null != datas && datas.length > 0){
                		Map<String, UFDouble> map = new HashMap<String, UFDouble>();
                		for (MeasureDataVO datavo : datas){
                			if (datavo.getMeasureVO().getType() == IStoreCell.TYPE_NUMBER || datavo.getMeasureVO().getType() == IStoreCell.TYPE_BIGDECIMAL)
                				map.put(datavo.getCode(), new UFDouble(datavo.getDataValue()));
                			else
                				map.put(datavo.getCode(), UFDouble.ZERO_DBL);
                		}

                		KeyGroupVO subKeyGroup = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(datas[0].getMeasureVO().getKeyCombPK());
                		String pk_dynValues = HBKeyGroupUtil.getPk_dynKeyValue(subKeyGroup, schemevo.getPk_keygroup());
                		
                        //对方单位编码关键字，不通用
                  		//JIAAH 虚单位，并且是当前的合并组织的情况下，个别报表数据设置为null
                      	if(isXuUnit && pk_org.equals(unionorg)){
                      		srcDynRepMap.put(pk_org + vo.getKeywordByPK(pk_dynValues), new HashMap<String, UFDouble>());
                      	}else
                      		srcDynRepMap.put(vo.getKeywordByPK(KeyVO.CORP_PK) + vo.getKeywordByPK(pk_dynValues), map);
                	}
                }
            } catch (UFOSrvException e) {
                nc.bs.logging.Logger.error(e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
    }


    /**
     * 设置动态表各版本数据
     * @param corps
     * @param unionorg
     * @param pk_report
     * @param Pk_hbrepstru
     * @throws BusinessException
     */
    protected void setDynReportVerDataVOS(String unionorg, String pk_report, String Pk_hbrepstru,MeasurePubDataVO pubData,Map<String, Map<String, UFDouble>> dynTotalMap)
            throws BusinessException {
    	try {

           RepDataVO datavo = HBPubItfService.getRemoteRepDataQry().loadRepData(pk_report, unionorg, pubData,
        		   Pk_hbrepstru)[0];

           MeasurePubDataVO[] allPubDataVos = datavo.getPubDatas();
           Set<MeasurePubDataVO> dynPubDataVos = new HashSet<MeasurePubDataVO>();
           for(MeasurePubDataVO vo : allPubDataVos){
           		if(!vo.getKType().equals(pubData.getKType())){
           			dynPubDataVos.add(vo);
           		}
           }
			for (MeasurePubDataVO vo : dynPubDataVos) {
				MeasureDataVO[] datas = datavo.getMeasureDatas(vo);
				if (null != datas && datas.length > 0) {
					Map<String, UFDouble> map = new HashMap<String, UFDouble>();
					for (MeasureDataVO data : datas) {
						if (data.getMeasureVO().getType() == IStoreCell.TYPE_NUMBER
								|| data.getMeasureVO().getType() == IStoreCell.TYPE_BIGDECIMAL)
							map.put(data.getCode(),new UFDouble(data.getDataValue()));
						else
							map.put(data.getCode(), UFDouble.ZERO_DBL);
					}
//					对方单位编码关键字，不通用
//					dynTotalMap.put(vo.getKeywordByPK(KeyVO.CORP_PK)+ vo.getKeywordByPK(KeyVO.DIC_CORP_PK), map);
            		KeyGroupVO subKeyGroup = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(datas[0].getMeasureVO().getKeyCombPK());
            		String pk_dynValues = HBKeyGroupUtil.getPk_dynKeyValue(subKeyGroup, schemevo.getPk_keygroup());
            		//对方单位编码关键字，不通用
            		dynTotalMap.put(vo.getKeywordByPK(KeyVO.CORP_PK) + vo.getKeywordByPK(pk_dynValues), map);

				}
			}
       } catch (UFOSrvException e) {
           nc.bs.logging.Logger.error(e.getMessage(), e);
           throw new BusinessException(e.getMessage());
       } catch (Exception e) {
           throw new BusinessException(e.getMessage());
       }

    }

    /**
     * 返回分类的主键
     * @param pubdata
     * @param pk_report
     * @throws BusinessException
     */
    private List<String> getVouchTypes(MeasurePubDataVO pubdata, String pk_report,IWorkDraft workdraft) throws BusinessException {
    	MeasurePubDataVO vouchpubData = MeasurePubDataUtil.getMeasurePubdata(pubdata,IDataVersionConsts.VER_VOUCHER);
    	VouchHeadVO[] headVO = HBPubItfService.getRemoteVouchQry().getVouchHead(vouchpubData.getAloneID(), getHbschemevo(),
    			new int[]{IVouchType.TYPE_AUTO_ENTRY,IVouchType.TYPE_MANU_ENTRY},null);

    	List<String> typeseqs = new ArrayList<String>();
    	//底稿选择的要显示的类别
    	WorkDraftTempVO tempvo = workdraft.getWorkDraftTempVO();
    	if(tempvo != null){
    		DXTypeSeqVO[] seqvo = tempvo.getDxtypeseq();
    		if(seqvo != null){
        		for(DXTypeSeqVO typevo : seqvo){
        			typeseqs.add(typevo.getPk_dxtype());
        		}
        	}
    	}

    	//凭证类别
    	Set<String> dxtypes = new HashSet<String>();
    	for(VouchHeadVO vo: headVO){
        	dxtypes.add(vo.getPk_dxtype());
        }

    	//要显示的类别
    	List<String> showType = new ArrayList<String>();
    	for(String s :typeseqs){
    		if(dxtypes.contains(s))
    			showType.add(s);
    	}
        return showType;
    }
    /**
     * 得到抵销借贷、分类抵销借贷的datamap
     * @param pubdata
     * @param pk_report
     * @throws BusinessException
     */
    private void setUnionVouchDataMap(List<String> setDxType,MeasurePubDataVO pubdata, String pk_report) throws BusinessException {
        //设置分类抵销借贷差数据
        for(String pk_dxtype : setDxType){
        	int midVer = DXTypeValue.getTypeVer(pk_dxtype);//版本号的中缀
			MeasurePubDataVO debit = MeasurePubDataUtil.getMeasurePubdata(pubdata,
								HBVersionUtil.getHBTyep_debit_src(getHbschemevo().getVersion(),midVer));
			MeasurePubDataVO credit = MeasurePubDataUtil.getMeasurePubdata(pubdata,
								HBVersionUtil.getHBTyep_credit_src(getHbschemevo().getVersion(),midVer));
			MeasurePubDataVO contrast = MeasurePubDataUtil.getMeasurePubdata(pubdata,
								HBVersionUtil.getHBTyep_Contrast_src(getHbschemevo().getVersion(),midVer));

        	Map<String, Object> typecremap = getVouchMeasDatas(pk_report, credit);
        	Map<String, Object> typedebmap = getVouchMeasDatas(pk_report, debit);
            Map<String, Object> typecontramap = getVouchMeasDatas(pk_report, contrast);
            getTypeCreDebmap().put(pk_dxtype, new Object[]{typedebmap,typecremap,typecontramap});
        }

    	//设置抵销借贷数据
    	MeasurePubDataVO vouchCreditpubData = MeasurePubDataUtil.getMeasurePubdata(pubdata,
                HBVersionUtil.getHBBB_credit_src(pubdata.getVer()));
        Map<String, Object> cremap = getVouchMeasDatas(pk_report, vouchCreditpubData);
        getCreditmap().putAll(cremap);

        MeasurePubDataVO vouchDebitpubData = MeasurePubDataUtil.getMeasurePubdata(pubdata,
                HBVersionUtil.getHBBB_debit_src(pubdata.getVer()));
        Map<String, Object> debmap = getVouchMeasDatas(pk_report, vouchDebitpubData);
        getDebitmap().putAll(debmap);
    }

	private Map<String, Object> getVouchMeasDatas(String pk_report, MeasurePubDataVO pubdata) throws BusinessException {
		Map<String, Object> vouchdatamap = new HashMap<String, Object>();
		MeasureDataVO[] creditdatas = HBPubItfService.getRemoteUnionReport().getMeasDataVOS(pk_report, pubdata);
		if (null != creditdatas && creditdatas.length > 0) {
		    for (MeasureDataVO vo : creditdatas) {
		    	vouchdatamap.put(vo.getCode(),
		                new UFDouble(vo.getUFDoubleValue() == null ? 0.0 : vo.getUFDoubleValue().doubleValue()));
		    }
		}
		return vouchdatamap;
	}

    @Override
    public CellsModel loadDraftCellsModel(String strRepPK, IRepDataParam param, IWorkDraft workdraft) throws Exception {
        ReportVO vo = IUFOCacheManager.getSingleton().getReportCache().getByPK(strRepPK);
        if(vo != null && vo.getIsintrade()!= null && vo.getIsintrade().booleanValue() )
        	return doDynRepDraft(strRepPK, param, workdraft);//内部交易采集表底稿展现
    	return doFixRepDraft(strRepPK, param, workdraft);
    }

    /**
     * 动态表底稿展现
     * @param strRepPK
     * @param param
     * @param workdraft
     * @return
     * @throws UFOSrvException
     * @throws BusinessException
     * @throws Exception
     */
    protected CellsModel doDynRepDraft(String strRepPK, IRepDataParam param,
			IWorkDraft workdraft) throws UFOSrvException, BusinessException,
			Exception {

		lstAll = new ArrayList<Cell[]>();

        //返回合并范围的组织
        String pk_unionorg = param.getPubData().getKeywordByPK(KeyVO.CORP_PK);
        schemevo = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(param.getTaskPK());
        String hbRepStruVersionPK = getHBRepStruPK(param.getPubData(), schemevo);
        String innercode = HBPubItfService.getRemoteUnionReport().getInnerCode(hbRepStruVersionPK, pk_unionorg);
        if (null == innercode || innercode.trim().length() == 0) {
            throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
            		.getStrByID("pub_0", "01830001-0502"));//"当前合并公司没有找到inner code"
        }
        String[] corps = HBPubItfService.getRemoteUnionReport().getUnionOrgs(hbRepStruVersionPK, innercode);//得到参与合并的组织

        //设置工作底稿选择单位:显示的组织的数据
        HashSet<String> hashset = new HashSet<String>();
        if (workdraft.getShoworgs() != null) {
            showorgs = workdraft.getShoworgs();
            hashset.addAll(Arrays.asList(showorgs));
        } else if (showorgs != null) {
            hashset.addAll(Arrays.asList(showorgs));
        } else {
            for (int i = 0; i < corps.length; i++) {
                hashset.add(corps[i].substring(0, 20));
            }
        }

        //选择单位和合并范围求交集返回底稿上显示的组织的数据:并复制给corps
        ArrayList<String> showCorps = new ArrayList<String>();
        for (int i = 0; i < corps.length; i++) {
            String string = corps[i].substring(0, 20);
            if (hashset.contains(string)) {
                showCorps.add(corps[i]);
            }
        }
        corps = showCorps.toArray(new String[showCorps.size()]);

        setParamPubData(param.getPubData());
        //返回keymap
        KeyVO[] keys = param.getPubData().getKeyGroup().getKeys();
        String[] keywords = param.getPubData().getKeywords();
        Map<String, String> keyMap = new HashMap<String, String>();
        for (int i = 0; i < keys.length; i++) {
            keyMap.put(keys[i].getPk_keyword(), keywords[i]);
        }
        keyMap.put(KeyVO.CORP_PK, pk_unionorg);

        //合并pubdataVO
        MeasurePubDataVO unionPubDataVO = MeasurePubDataUtil.getMeasurePubdata(schemevo.getVersion(), true, pk_unionorg,
                schemevo.getPk_keygroup(), keyMap, schemevo.getPk_accperiodscheme());
        //底稿的标题cell
        IProjectCellHead createProduct = ProjectCellHeadFactory.getInstance().CreateProduct(ProjectHead.UNIONREPORT,corps, workdraft,null,true);

        //1.1 添加表头名称-合并利润表工作底稿
        Cell[] titlecells = createProduct.getTitleCells(param.getReportPK(), workdraft);
        lstAll.add(titlecells);
        lstAll.add(new Cell[] { null });
        lstAll.add(new Cell[] { null });

        //1.2 添加关键字信息
        // 标题所占行数
        int iTitleRow = 3;
        // 获取底稿关键字信息
        Cell[] keywordCell = getKeywordCell(iTitleRow, param);
        lstAll.add(keywordCell);

        // 取得底稿数据
        MeasureCache measureCache = UFOCacheManager.getSingleton().getMeasureCache();
        // 提取报表指标
        if (workdraft.getMesurevos() != null) {
            loadMeasureByReportPK = workdraft.getMesurevos();
        } else {
            // 提取报表指标
            loadMeasureByReportPK = measureCache.loadMeasureByReportPK(param.getReportPK());
        }


        //1.3 添加列头名称显示
        Cell[] headCells = createProduct.getProjectHeadCells();
        lstAll.add(headCells);

        // 准备合并子公司原动态表数据
        setSrcDynReportVOS(corps,param.getRepOrgPK(), param.getReportPK(), hbRepStruVersionPK);
        // 准备合计数数据
        MeasurePubDataVO totalpubData = MeasurePubDataUtil.getMeasurePubdata(
                HBVersionUtil.getHBTotalyHBSchemeVO(schemevo), true, pk_unionorg, schemevo.getPk_keygroup(), keyMap,
                schemevo.getPk_accperiodscheme());
        setDynReportVerDataVOS(param.getRepOrgPK(),param.getReportPK(),hbRepStruVersionPK,totalpubData,dynTotalMap);
        //准备合并
        setDynReportVerDataVOS(param.getRepOrgPK(),param.getReportPK(),hbRepStruVersionPK,unionPubDataVO,dynUnionMap);
        //准备抵销数据
        MeasurePubDataVO dxpubData = MeasurePubDataUtil.getMeasurePubdata(
                HBVersionUtil.getHBContrastByHBSchemeVO(schemevo), true, pk_unionorg, schemevo.getPk_keygroup(), keyMap,
                schemevo.getPk_accperiodscheme());
        setDynReportVerDataVOS(param.getRepOrgPK(),param.getReportPK(),hbRepStruVersionPK,dxpubData,dynDXContrastMap);

        int row = 5;

        loadMeasureByReportPK = MeasureUtil.getSortByPositionMeasureVOs(loadMeasureByReportPK, strRepPK);
      	List<MeasureVO> dynMeasures = new ArrayList<MeasureVO>();
      	for(MeasureVO vo : loadMeasureByReportPK){
      		String mainKeyGroup = unionPubDataVO.getKType();
      		if(!vo.getKeyCombPK().equals(mainKeyGroup)){
      			dynMeasures.add(vo);
      		}
      	}
      	//只显示动态区指标
        loadMeasureByReportPK = dynMeasures.toArray(new MeasureVO[dynMeasures.size()]);
        if(loadMeasureByReportPK != null && loadMeasureByReportPK.length > 0){
        	String pk_wordraft = workdraft.getPk_workdrafttemp();
        	WorkDraftTempVO draftvo = workdraft.getWorkDraftTempVO();
        	if(pk_wordraft != null && draftvo != null && draftvo.getPk_report().equals(workdraft.getPk_report())){
        		MeasureInfoVO[] measinfovos = workdraft.getMeasinfovos();// 底稿上设置的指标
				ConcurrentHashMap<String, MeasureVO> measmap = new ConcurrentHashMap<String, MeasureVO>();// 当前报表上所有的指标
				for (MeasureVO vo : loadMeasureByReportPK) {
					measmap.put(vo.getCode(), vo);
				}
				// 只是按照单位按列展示：逐行加载底稿数据
				if (measinfovos == null)
					throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0309")/*@res "工作底稿未设置显示指标"*/);
				Set<MeasureVO> drafMeasures = new HashSet<MeasureVO>();
				for (int j = 0; j < measinfovos.length; j++) {
					MeasureVO vo = measmap.get(measinfovos[j].getPk_measure());
					if(vo != null)
						drafMeasures.add(vo);
				}
				//工作底稿需要显示的指标
				loadMeasureByReportPK = drafMeasures.toArray(new MeasureVO[drafMeasures.size()]);
        	}

        	KeyGroupVO subKeyGroup = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(loadMeasureByReportPK[0].getKeyCombPK());
    		String pk_dynValues = HBKeyGroupUtil.getPk_dynKeyValue(subKeyGroup, schemevo.getPk_keygroup());
    		boolean bDicCorp = true;
    		if(!pk_dynValues.equals(KeyVO.DIC_CORP_PK)){
    			bDicCorp = false;
    		}
        	//分指标加载底稿数据
        	for (int i = 0; i < loadMeasureByReportPK.length; i++) {
        		Cell cells = new Cell();
				cells.setValue(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0310")/*@res "指标："*/ + loadMeasureByReportPK[i].getName());
				cells.setRow(row);
				cells.setCol(0);
				cells.setFormat(IProjectCellHead.keywordformat);
				lstAll.add(new Cell[] { cells });
				row++;
				doDynRepDefaultWithOutDraft(row, loadMeasureByReportPK[i],corps, workdraft, pk_unionorg,bDicCorp);
			}
        }

        CellsModel result = CellsModel.getInstance(lstAll.toArray(new Object[0][0]), true);
        CellPosition start = CellPosition.getInstance(1, 0);

        int trow = 5 + hashset.size();
        if (workdraft != null
                && !StringUtil.isEmptyWithTrim(workdraft.getPk_workdrafttemp())
                && workdraft.getWorkDraftTempVO().getUnitshowtype() == IWorkDraftConst.UNITSHOWTYPE_ROW) {
            trow = workdraft.getMeasinfovos().length + 1;
        }

        AreaPosition area = AreaPosition.getInstance(1, 0, trow, 0);
        result.getCombinedAreaModel().combineCell(area);
        Cell head = null;
        for (Cell cvo : titlecells) {
            if (null != cvo) {
                head = cvo;
                break;
            }
        }

        // 将关键字的第二个后面的两个单元格合并
        int j = 1;
        for (int i = 1; i < getKeyword(param.getPubData()).length; i++) {
            AreaPosition keyarea = AreaPosition.getInstance(3, j, 2, 1);
            result.getCombinedAreaModel().combineCell(keyarea);
            j = j + 2;
        }

        // 动态设置列的长度尺寸
        int rowNum = result.getRowNum();
        int maxLenth = 0;
        AreaPosition area2 = AreaPosition.getInstance(1, 0, 1, rowNum);
        Cell[][] cells = result.getCells(area2);
        for (int i = 0; i < cells.length; i++) {
            Cell[] cells2 = cells[i];
            if (cells2[0] != null) {
                Object value = cells2[0].getValue();
                if (value != null && value instanceof String) {
                    String projectname = (String) value;
                    int length = projectname.length();
                    if (length > maxLenth)
                        maxLenth = length;
                } else if (value != null && value instanceof MeasureVO) {
                    String projectname = ((MeasureVO) value).toString();
                    int length = projectname.length();
                    if (length > maxLenth)
                        maxLenth = length;
                }
            }
        }
        UFDouble div = new UFDouble(maxLenth + 3).div(new UFDouble(10));
        UFDouble multiply = div.multiply(new UFDouble(100));
        result.getColumnHeaderModel().setSize(0, multiply.intValue());
        result.getCell(start).setFormat(head.getFormat());
        result.getCell(start).setValue(head.getValue());
        result.setDirty(false);

        return result;
    }
    
    
    /**
     * 一键联查，返回树形控件。
     * 
     * @param param
     * @return
     * @author: 王志强
     */
    public LinkEndTreeModel getReport(Object param){
    	
 
    	

	 
			Object[] params=(Object[])param;
			IRepDataParam repDataParam=(IRepDataParam)params[0];
			
			LoginEnvVO logVo =  (LoginEnvVO)params[1];
			
			
			IWorkDraft workDraf = (IWorkDraft)params[5];
			
			MeasureVO measureVo = (MeasureVO)params[6];
			
			int dataCenterType = (Integer)params[7];
			
			
		    String pk_unionorg = repDataParam.getPubData().getKeywordByPK(KeyVO.CORP_PK);
		    HBSchemeVO scheme;
			try {
				scheme = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(repDataParam.getTaskPK());
			} catch (UFOSrvException e1) {
				 Logger.error(e1);
				 throw new BusinessRuntimeException("查询合并范围错误，pk is："+repDataParam.getTaskPK(), e1);
			}
		    
	        String hbRepStruVersionPK = getHBRepStruPK(repDataParam.getPubData(), scheme);
	        String innercode = null;
			try {
				innercode = HBPubItfService.getRemoteUnionReport().getInnerCode(hbRepStruVersionPK, pk_unionorg);
			} catch (BusinessException e1) {
				 Logger.error(e1);
				 throw new BusinessRuntimeException("主体查询错误pk_org:"+pk_unionorg,e1);
			}
	        
	    
	        
	        //返回keymap
	        KeyVO[] keys = repDataParam.getPubData().getKeyGroup().getKeys();
	        String[] keywords = repDataParam.getPubData().getKeywords();
	        Map<String, String> keyMap = new HashMap<String, String>();
	        for (int i = 0; i < keys.length; i++) {
	            keyMap.put(keys[i].getPk_keyword(), keywords[i]);
	        }
	        keyMap.put(KeyVO.CORP_PK, pk_unionorg);

	        //合并pubdataVO
	        try {
				MeasurePubDataVO unionPubDataVO = MeasurePubDataUtil.getMeasurePubdata(scheme.getVersion(), true, pk_unionorg,
				        scheme.getPk_keygroup(), keyMap, scheme.getPk_accperiodscheme());
			} catch (Exception e1) {
				 Logger.error(e1);
				 throw new BusinessRuntimeException("查询MeasurePubDataVO错误！",e1);
			}
	        
	        

	        // 取得底稿数据
	        MeasureCache measureCache = UFOCacheManager.getSingleton().getMeasureCache();
	        // 提取报表指标
	        
	         MeasureVO[]   loadMeasureByReportPK = workDraf.getMesurevos();
	         
	         // 准备合并子公司数据
	         String org = repDataParam.getRepOrgPK();
	         if(org == null)
	         	org = pk_unionorg;
	         
	         
	         ILinkEndQuery query = NCLocator.getInstance().lookup(ILinkEndQuery.class);
		     Map<String,String> orgInnerCodeMap = query.getUnionOrgsOrderByCode(hbRepStruVersionPK, innercode, null);
				
		        if(orgInnerCodeMap.keySet().size()>300){
		        	throw new BusinessRuntimeException("所选组织下级超过300，请设定联查级次!");
		        }
		     IBasicOrgUnitQryService orgService = NCLocator.getInstance().lookup(IBasicOrgUnitQryService.class);
		     OrgVO[] useOrgs = null;
		    
		     try {
		    	 useOrgs = orgService.getOrgs(orgInnerCodeMap.keySet().toArray(new String[0]));
			} catch (BusinessException e1) {
				 Logger.error(e1);
				 throw new BusinessRuntimeException("查询对应主体错误！",e1);
			}
		     
		     Map<String, OrgVO> orgCachMap = new HashMap<>();
		     for(OrgVO useOrg:useOrgs){
		    	 orgCachMap.put(useOrg.getPk_org(), useOrg);
		     }
		       //是否是虚单位
	         Map<String, UFBoolean> notEntityorgMap = null;
	         try {
	        	 notEntityorgMap = HBRepStruUtil.getBooleanEntityOrgs(orgInnerCodeMap.keySet().toArray(new String[0]), hbRepStruVersionPK);
			} catch (BusinessException e) {
				 Logger.error(e);
				 throw new BusinessRuntimeException("查询虚主体错误！",e);
			}
		     orgInnerCodeMap.remove(org);
		     
		     Map<String, String> innerCodeOrgPkMap = new  HashMap<>();
		     for(String key:orgInnerCodeMap.keySet()){
		    	 String innerCode = orgInnerCodeMap.get(key);
		    	 innerCodeOrgPkMap.put(innerCode, key);
		     }
	         //order by org
	         List<String> innerCodeOrtList = new ArrayList<>();
	         innerCodeOrtList.addAll(orgInnerCodeMap.values());
	         Collections.sort(innerCodeOrtList);
	         Map<String,DefaultMutableTreeNode>  nodeMap = new HashMap<>();
	        
	         //判断所选组织类别1.合并虚组织，查询合并表数据,下级，合计数，抵消借，抵消贷。2，合并实组织：查询合并表数据，查询调整表数据。3.末级组织:查询个别表数据
	         
	         	//处理联查组织
	         
	      
	         
	         
	         //联查主体合并数
//	         
//	         IRepDataQuerySrv qrySrv =  HBPubItfService.getRemoteRepDataQry();
//	         MeasurePubDataVO currentPubData =  createMeasureData(org, scheme, repDataParam.getPubData(), repDataParam.getReportPK(), HBVersionUtil.getHBTotalyHBSchemeVO(scheme));
//
//	         
//	         RepDataVO[] vos =null;;
//			try {
//				vos = qrySrv.loadRepDataWithMeasures(repDataParam.getReportPK(), org, currentPubData, hbRepStruVersionPK,loadMeasureByReportPK);
//			} catch (Exception e) {
//				Logger.error(e);
//				throw new BusinessRuntimeException("查询联查主体合并数据错误!",e);
//			}
//			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(orgCachMap.get(org).getName()+"(合并数)");
//	         if(vos!=null&&vos.length>0){
//		         RepDataVO repData = vos[0];
//		         LinkEndTreeUserObj rootObj = getUserObj(repData, loadMeasureByReportPK);
//		         rootObj.setOrgDisName(orgCachMap.get(org).getName());
//		         rootObj.setReportTypeName("合并数");
//	         }
//	         //合并数
	         DefaultMutableTreeNode root = getLindNode(LinkEndTreeUserObj.TYPENAME_HBS,scheme.getVersion(),orgCachMap.get(org).getName(),org, innercode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
	         //合计数
	         DefaultMutableTreeNode root_hjs = getLindNode(LinkEndTreeUserObj.TYPENAME_HJS,HBVersionUtil.getHBTotalyHBSchemeVO(scheme),orgCachMap.get(org).getName(),org, innercode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
	         root.add(root_hjs);
	         //抵消借贷
	         DefaultMutableTreeNode root_dxj = getLindNode(LinkEndTreeUserObj.TYPENAME_DXJ,HBVersionUtil.getHBBB_debit_src(scheme.getVersion()),orgCachMap.get(org).getName(),org, innercode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
	         root.add(root_dxj);
	         DefaultMutableTreeNode root_dxd = getLindNode(LinkEndTreeUserObj.TYPENAME_DXD,HBVersionUtil.getHBBB_credit_src(scheme.getVersion()),orgCachMap.get(org).getName(),org, innercode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
	         root.add(root_dxd);
	         nodeMap.put(innercode, root);


	     	if(!notEntityorgMap.get(org).booleanValue()){
	     		 DefaultMutableTreeNode rootSelfNode = getLindNode(LinkEndTreeUserObj.TYPENAME_GBB,0,orgCachMap.get(org).getName(),org, innercode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
	     		 root.add(rootSelfNode);
	     	}
	         for(String org_innerCode:innerCodeOrtList){
	        	 String pk_org = innerCodeOrgPkMap.get(org_innerCode);
	        	if(notEntityorgMap.get(pk_org).booleanValue()){
	        		
	        	 //合计数	
	   	         DefaultMutableTreeNode xOrgNode = getLindNode(LinkEndTreeUserObj.TYPENAME_HBS,scheme.getVersion(),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
		         //合计数
		         DefaultMutableTreeNode xOrgNode_hjs = getLindNode(LinkEndTreeUserObj.TYPENAME_HJS,HBVersionUtil.getHBTotalyHBSchemeVO(scheme),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
		         xOrgNode.add(xOrgNode_hjs);
		         //抵消借贷
		         DefaultMutableTreeNode xOrgNodet_dxj = getLindNode(LinkEndTreeUserObj.TYPENAME_DXJ,HBVersionUtil.getHBBB_debit_src(scheme.getCode()),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
		         xOrgNode.add(xOrgNodet_dxj);
		         DefaultMutableTreeNode xOrgNode_dxd = getLindNode(LinkEndTreeUserObj.TYPENAME_DXD,HBVersionUtil.getHBBB_credit_src(scheme.getCode()),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
		         xOrgNode.add(xOrgNode_dxd);

	        	 getCacheNode(nodeMap,org_innerCode).add(xOrgNode)	;
	        		
	        	   nodeMap.put(org_innerCode, xOrgNode);	
	        		
	        		continue;
	        		
	        	}
	        	
	        	 //当前主体是否存在下级
	        	boolean hasSubOrg = false;
	        	 try {
	        		 hasSubOrg = HBPubItfService.getRemoteUnionReport().hasSubOrgs(org_innerCode, hbRepStruVersionPK);
				} catch (BusinessException e) {
					 Logger.error(e);
					 throw new BusinessRuntimeException("查询虚主体错误！",e);
				}
	        	 
	        	 
	        	 if(hasSubOrg){
	        		 
	        		 //合计数
		   	         DefaultMutableTreeNode entityOrgNode = getLindNode(LinkEndTreeUserObj.TYPENAME_HBS,scheme.getVersion(),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
		   	         //合计数
			         DefaultMutableTreeNode xOrgNode_hjs = getLindNode(LinkEndTreeUserObj.TYPENAME_HJS,HBVersionUtil.getHBTotalyHBSchemeVO(scheme),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
			         entityOrgNode.add(xOrgNode_hjs);
			         //抵消借贷
			         DefaultMutableTreeNode xOrgNodet_dxj = getLindNode(LinkEndTreeUserObj.TYPENAME_DXJ,HBVersionUtil.getHBBB_debit_src(scheme.getCode()),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
			         entityOrgNode.add(xOrgNodet_dxj);
			         DefaultMutableTreeNode xOrgNode_dxd = getLindNode(LinkEndTreeUserObj.TYPENAME_DXD,HBVersionUtil.getHBBB_credit_src(scheme.getCode()),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
			         entityOrgNode.add(xOrgNode_dxd);
			         
			         DefaultMutableTreeNode self_node = getLindNode(LinkEndTreeUserObj.TYPENAME_GBB,0,orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
			         entityOrgNode.add(self_node);

		        	 getCacheNode(nodeMap,org_innerCode).add(entityOrgNode)	;
		        	 
		        	   nodeMap.put(org_innerCode, entityOrgNode);	
	        	 }else{
	        		 
	        		 //个别表数
	        		 DefaultMutableTreeNode entityOrgNode = getLindNode(LinkEndTreeUserObj.TYPENAME_GBB,0,orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
	        		 getCacheNode(nodeMap,org_innerCode).add(entityOrgNode)	;
	        	 }
	        	 
//	        	 HBPubItfService.getRemoteUnionReport().hasSubOrgs(innercode, hbRepStruVersionPK)
	         }
	         LinkEndTreeModel treeModel = new LinkEndTreeModel(root);
	         treeModel.setMeasures(new ArrayList<>(Arrays.asList( loadMeasureByReportPK)));
	         return treeModel;
	         

			
			
			 //TODO not end
		 
    }
    
    
    private DefaultMutableTreeNode getCacheNode(
			Map<String, DefaultMutableTreeNode> nodeMap, String org_innerCode) {
		 
    	String key = org_innerCode.substring(0,org_innerCode.length()-4);
    	if(nodeMap==null||nodeMap.get(key)==null){
    		BusinessRuntimeException ex = new BusinessRuntimeException("上级主体未生成！");
    		 Logger.error(ex);
			 throw  ex;
    	}
    	
		return nodeMap.get(key);
	}

	private DefaultMutableTreeNode getLindNode(String verName,Integer ver,String orgName,String pk_org,String innerCode,MeasurePubDataVO pubData,String pk_report,HBSchemeVO scheme,MeasureVO[] measures,String s_ver){
    	
    	
        IRepDataQuerySrv qrySrv =  HBPubItfService.getRemoteRepDataQry();
        MeasurePubDataVO currentPubData =  createMeasureData(pk_org, scheme, pubData, pk_report,ver);

        
        RepDataVO[] vos =null;;
		try {
			vos = qrySrv.loadRepDataWithMeasures(pk_report, pk_org, currentPubData, s_ver,measures);
		} catch (Exception e) {
			Logger.error(e);
			throw new BusinessRuntimeException("查询联查主体合并数据错误!",e);
		}
		
		
		DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(orgName+"("+verName+")");
		LinkEndTreeUserObj userObj = null;
        if(vos!=null&&vos.length>0){
	         RepDataVO repData = vos[0];
	         userObj = getUserObj(repData, measures);
	        
        }else{
        	 userObj = getUserObj(null, measures);
        }
        userObj.setOrgDisName(orgName+"("+verName+")");
        userObj.setReportTypeName(verName);
        treeNode.setUserObject(userObj);
        return treeNode;
        
    	
    }
    
    private LinkEndTreeUserObj getUserObj(RepDataVO report,MeasureVO[] measures){
    	LinkEndTreeUserObj rtn = new LinkEndTreeUserObj();
    	rtn.addMeasures(measures);
    	
    	if(report!=null){
    		MeasureDataVO[] datas = report.getAllMeasureDatas();
    		for(MeasureDataVO data:datas){
    			if(Arrays.asList(measures).contains(data.getMeasureVO())){
    				rtn.addMeasureData(data);
    			}
//    			int i = Arrays.binarySearch(measures, data.getMeasureVO());
//    			if(i>=0){
//    				
//    			}
    		}
    	}
    	return rtn;
    	
    }
	            
	           
    
    
    /**
     * 固定表底稿展现
     * @param strRepPK
     * @param param
     * @param workdraft
     * @return
     * @throws UFOSrvException
     * @throws BusinessException
     * @throws Exception
     */
	private CellsModel doFixRepDraft(String strRepPK, IRepDataParam param,
			IWorkDraft workdraft) throws UFOSrvException, BusinessException,
			Exception {
		lstAll = new ArrayList<Cell[]>();

        //返回合并范围的组织
        String pk_unionorg = param.getPubData().getKeywordByPK(KeyVO.CORP_PK);
        schemevo = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(param.getTaskPK());
        String hbRepStruVersionPK = getHBRepStruPK(param.getPubData(), schemevo);
        String innercode = HBPubItfService.getRemoteUnionReport().getInnerCode(hbRepStruVersionPK, pk_unionorg);
        if (null == innercode || innercode.trim().length() == 0) {
            throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
            		.getStrByID("pub_0", "01830001-0502"));//"当前合并公司没有找到inner code"
        }
        String[] corps = HBPubItfService.getRemoteUnionReport().getUnionOrgsOrderByCode(hbRepStruVersionPK, innercode);//得到参与合并的组织

        //设置工作底稿选择单位:显示的组织的数据
        HashSet<String> hashset = new HashSet<String>();
        if (workdraft.getShoworgs() != null) {
            showorgs = workdraft.getShoworgs();
            hashset.addAll(Arrays.asList(showorgs));
        } else if (showorgs != null) {
            hashset.addAll(Arrays.asList(showorgs));
        } else {
            for (int i = 0; i < corps.length; i++) {
                hashset.add(corps[i].substring(0, 20));
            }
        }

        //选择单位和合并范围求交集返回底稿上显示的组织的数据:并复制给corps
        ArrayList<String> showCorps = new ArrayList<String>();
        for (int i = 0; i < corps.length; i++) {
            String string = corps[i].substring(0, 20);
            if (hashset.contains(string)) {
                showCorps.add(corps[i]);
            }
        }
        corps = showCorps.toArray(new String[showCorps.size()]);

        setParamPubData(param.getPubData());
        //返回keymap
        KeyVO[] keys = param.getPubData().getKeyGroup().getKeys();
        String[] keywords = param.getPubData().getKeywords();
        Map<String, String> keyMap = new HashMap<String, String>();
        for (int i = 0; i < keys.length; i++) {
            keyMap.put(keys[i].getPk_keyword(), keywords[i]);
        }
        keyMap.put(KeyVO.CORP_PK, pk_unionorg);

        //合并pubdataVO
        MeasurePubDataVO unionPubDataVO = MeasurePubDataUtil.getMeasurePubdata(schemevo.getVersion(), true, pk_unionorg,
                schemevo.getPk_keygroup(), keyMap, schemevo.getPk_accperiodscheme());
        //返回抵销凭证类别
        List<String> dxtypepk = getVouchTypes(unionPubDataVO, strRepPK,workdraft);
        //底稿的标题cell
        IProjectCellHead createProduct = ProjectCellHeadFactory.getInstance().CreateProduct(ProjectHead.UNIONREPORT,corps, workdraft,dxtypepk,false);

        //1.1 添加表头名称-合并利润表工作底稿
        Cell[] titlecells = createProduct.getTitleCells(param.getReportPK(), workdraft);
        lstAll.add(titlecells);
        lstAll.add(new Cell[] { null });
        lstAll.add(new Cell[] { null });

        //1.2 添加关键字信息
        // 标题所占行数
        int iTitleRow = 3;
        // 获取底稿关键字信息
        Cell[] keywordCell = getKeywordCell(iTitleRow, param);
        lstAll.add(keywordCell);

        //1.3 添加列头名称显示
        Cell[] headCells = createProduct.getProjectHeadCells();
        lstAll.add(headCells);

        //1.4 分类抵销借贷显示的情况下：添加第二层列头名称
        if (workdraft != null&& !StringUtil.isEmptyWithTrim(workdraft.getPk_workdrafttemp())
        		&& workdraft.getWorkDraftTempVO().getUnitshowtype() == IWorkDraftConst.UNITSHOWTYPE_COL) {
        	int type = workdraft.getWorkDraftTempVO().getDxshowtype();
        	if(type == IWorkDraftConst.DXSHOWTYPE_DX_NORMAL){
        		int width = 1 + corps.length + 1 + dxtypepk.size()*2 + 1;
        		Cell[] cells = new Cell[width];
        		for(int i = width - dxtypepk.size()*2 - 1 ; i < width ; i++){
        			cells[i] = null;
        			if(i >= width - dxtypepk.size()*2 - 1 && i != width -1){
        				cells[i] = IProjectCellHead.getHeadCellBy(5, i, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0311")/*@res "抵销借"*/);
        				i = i + 1;
        				cells[i] = IProjectCellHead.getHeadCellBy(5, i, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0312")/*@res "抵销贷"*/);
        			}
        		}
                lstAll.add(cells);
        	}
        }

        // 取得底稿数据
        MeasureCache measureCache = UFOCacheManager.getSingleton().getMeasureCache();
        // 提取报表指标
        if (workdraft.getMesurevos() != null) {
            loadMeasureByReportPK = workdraft.getMesurevos();
        } else {
            // 提取报表指标
            loadMeasureByReportPK = measureCache.loadMeasureByReportPK(param.getReportPK());
        }
        // 准备指标项目映射
//        measureProjectMap = HBBaseDocItfService.getRemoteHBProjectMapQry().loadMappingsByReportId(param.getReportPK());

        // 准备合并子公司数据
        String org = param.getRepOrgPK();
        if(org == null)
        	org = pk_unionorg;
        setSrcReportVOS(corps,org, param.getReportPK(), hbRepStruVersionPK);
        // 准备合计数数据
        MeasurePubDataVO totalpubData = MeasurePubDataUtil.getMeasurePubdata(
                HBVersionUtil.getHBTotalyHBSchemeVO(schemevo), true, pk_unionorg, schemevo.getPk_keygroup(), keyMap,
                schemevo.getPk_accperiodscheme());
        RepDataVO totaldatavo = HBPubItfService.getRemoteRepDataQry().loadRepData(param.getReportPK(), pk_unionorg, totalpubData,
                param.getRepMngStructPK())[0];
        totalmap = IProjectCellHead.getMeasdataMap(totaldatavo);
        // 准备合并报表数据
        RepDataVO hbRepDatavo = HBPubItfService.getRemoteRepDataQry().loadRepData(param.getReportPK(),
        		pk_unionorg,unionPubDataVO, param.getRepMngStructPK())[0];
        unionMeasdatamap = IProjectCellHead.getMeasdataMap(hbRepDatavo);

        // 准备抵销借贷差凭证数据
        setUnionVouchDataMap(dxtypepk,unionPubDataVO, strRepPK);

        int row = 5;
        // 对指标进行一下排序
        loadMeasureByReportPK = MeasureUtil.getSortByPositionMeasureVOs(loadMeasureByReportPK, strRepPK);
        if (null != workdraft.getPk_workdrafttemp() && workdraft.getPk_workdrafttemp().trim().length() > 0) {
            // 使用了底稿模板
            WorkDraftTempVO draftvo = workdraft.getWorkDraftTempVO();
            if (draftvo.getPk_report().equals(workdraft.getPk_report())) {
                MeasureInfoVO[] measinfovos = workdraft.getMeasinfovos();//底稿上设置的指标
                // @edit by zhoushuang at 2015-7-11,下午3:22:52 没有指标的底稿--合并中心,底稿打开报错
                if(measinfovos == null){
            		throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0309")/*@res "工作底稿未设置显示指标"*/);
                }
                ConcurrentHashMap<String, MeasureVO> measmap = new ConcurrentHashMap<String, MeasureVO>();//当前报表上所有的指标
                for (MeasureVO vo : loadMeasureByReportPK) {
                    measmap.put(vo.getCode(), vo);
                }
                //单位按列展示：逐行加载底稿数据
                if (draftvo.getUnitshowtype() == IWorkDraftConst.UNITSHOWTYPE_COL) {
                	for (int j = 0; j < measinfovos.length; j++) {
                        MeasureVO vo = measmap.get(measinfovos[j].getPk_measure());
                        switch(draftvo.getDxshowtype()){
	                        case IWorkDraftConst.DXSHOWTYPE_NORMAL://抵销借贷
	                        	doDefault(row, loadMeasureByReportPK, corps, vo, measinfovos[j], workdraft);
	        					break;
	        				case IWorkDraftConst.DXSHOWTYPE_DX_NORMAL://分类抵销借贷
	        					doTypeDXCreAndDeb(row, loadMeasureByReportPK, corps, vo, measinfovos[j], workdraft);
	        					break;
	        				case IWorkDraftConst.DXSHOWTYPE_DX_CLEAN://分类抵销净额
	        					doTypeDXContrast(row, loadMeasureByReportPK, corps, vo, measinfovos[j], workdraft);
	        					break;
	        				default:
	        					doDefault(row, loadMeasureByReportPK, corps, vo, measinfovos[j], workdraft);
	        					break;
                        }
                        row++;
                    }
                } else {
                    // 单位按行展示
                    // 先单位数据,各个单位对应的指标数据
                	for (int i = 0; i < corps.length; i++) {
            		    ArrayList<Cell> cellList = new ArrayList<Cell>();// 单位行数据
            		    int colnum = 0;
            		    String name = HBPubItfService.getRemoteOrgUnit().getOrg(corps[i].substring(0, 20)).getName();
            		    cellList.add(IProjectCellHead.getHeadCellBy(row + i, colnum, name));// 单位名称;
            		    String pk_org = corps[i].substring(0, 20);
            		    ConcurrentHashMap<String, Object> srcdatamap = getSrcOrgMeasDataMap().get(pk_org);
            		    for (int j = 0; j < measinfovos.length; j++) {
            		        MeasureVO vo = measmap.get(measinfovos[j].getPk_measure());
            		        cellList.add(IProjectCellHead.getDataCellBy(row, colnum + j + 1, vo, srcdatamap));
            		    }
            		    if (hashset.contains(pk_org)) {
            		        lstAll.add(cellList.toArray(new Cell[0]));
            		        row++;
            		    }
            		}
            		row = row + 1;
                    ArrayList<Cell> cellList = getRowData(corps,createProduct, row, measinfovos, measmap,draftvo);
                    lstAll.add(cellList.toArray(new Cell[0]));
                }
            } else {
            	doDefaultWithOutDraft(row, loadMeasureByReportPK, corps, workdraft);
            }
        } else {
        	doDefaultWithOutDraft(row, loadMeasureByReportPK, corps, workdraft);
        }

        CellsModel result = CellsModel.getInstance(lstAll.toArray(new Object[0][0]), true);
        CellPosition start = CellPosition.getInstance(1, 0);

        int trow = 5 + corps.length;
        if (workdraft != null
                && !StringUtil.isEmptyWithTrim(workdraft.getPk_workdrafttemp())
                && workdraft.getWorkDraftTempVO().getUnitshowtype() == IWorkDraftConst.UNITSHOWTYPE_ROW) {
            trow = workdraft.getMeasinfovos().length + 1;
        }

        if (workdraft != null&& !StringUtil.isEmptyWithTrim(workdraft.getPk_workdrafttemp())
        		&& workdraft.getWorkDraftTempVO().getUnitshowtype() == IWorkDraftConst.UNITSHOWTYPE_COL) {
        	int type = workdraft.getWorkDraftTempVO().getDxshowtype();
        	if(type == IWorkDraftConst.DXSHOWTYPE_DX_NORMAL)
        		trow = dxtypepk.size()*2 + 3 + corps.length;
        	else if(type == IWorkDraftConst.DXSHOWTYPE_DX_CLEAN)
        		trow = dxtypepk.size() + 3 + corps.length;
        }

        AreaPosition area = AreaPosition.getInstance(1, 0, trow, 0);
        result.getCombinedAreaModel().combineCell(area);
        Cell head = null;
        for (Cell cvo : titlecells) {
            if (null != cvo) {
                head = cvo;
                break;
            }
        }

        Cell headCell = null;
        for (Cell cvo : headCells) {
            if (null != cvo) {
            	headCell = cvo;
                break;
            }
        }

        //合并单元格：分类抵销节点
        if (workdraft != null&& !StringUtil.isEmptyWithTrim(workdraft.getPk_workdrafttemp())
        		&& workdraft.getWorkDraftTempVO().getUnitshowtype() == IWorkDraftConst.UNITSHOWTYPE_COL) {
        	int type = workdraft.getWorkDraftTempVO().getDxshowtype();
        	if(type == IWorkDraftConst.DXSHOWTYPE_DX_NORMAL){
        		//项目、公司、合计列
                for(int i = 0 ; i < trow - dxtypepk.size()*2 - 1; i++){
                	AreaPosition areax = AreaPosition.getInstance(4, i, 1, 2);
                    result.getCombinedAreaModel().combineCell(areax);
                }
                //分类抵销借贷列
                for(int j = trow - dxtypepk.size()*2 - 1;j < trow -1;j=j+2){
                	AreaPosition areax = AreaPosition.getInstance(4, j, 2, 1);
                    result.getCombinedAreaModel().combineCell(areax);

                    CellPosition w = CellPosition.getInstance(4, j);
                    Cell cell = result.getCell(w);
                    cell.setFormat(headCell.getFormat());
                    cell.setValue(cell.getValue().toString().substring(0, 3));
                }
                //合并列
                AreaPosition areax = AreaPosition.getInstance(4, trow-1, 1, 2);
                result.getCombinedAreaModel().combineCell(areax);
        	}
        }

        // 将关键字的第二个后面的两个单元格合并
        int j = 1;
        for (int i = 1; i < getKeyword(param.getPubData()).length; i++) {
            AreaPosition keyarea = AreaPosition.getInstance(3, j, 2, 1);
            result.getCombinedAreaModel().combineCell(keyarea);
            j = j + 2;
        }

        // 动态设置列的长度尺寸
        int rowNum = result.getRowNum();
        int maxLenth = 0;
        AreaPosition area2 = AreaPosition.getInstance(1, 0, 1, rowNum);
        Cell[][] cells = result.getCells(area2);
        for (int i = 0; i < cells.length; i++) {
            Cell[] cells2 = cells[i];
            if (cells2[0] != null) {
                Object value = cells2[0].getValue();
                if (value != null && value instanceof String) {
                    String projectname = (String) value;
                    int length = projectname.length();
                    if (length > maxLenth)
                        maxLenth = length;
                } else if (value != null && value instanceof MeasureVO) {
                    String projectname = ((MeasureVO) value).toString();
                    int length = projectname.length();
                    if (length > maxLenth)
                        maxLenth = length;
                }
            }
        }
        UFDouble div = new UFDouble(maxLenth + 3).div(new UFDouble(10));
        UFDouble multiply = div.multiply(new UFDouble(100));
        result.getColumnHeaderModel().setSize(0, multiply.intValue());
        result.getCell(start).setFormat(head.getFormat());
        result.getCell(start).setValue(head.getValue());
        result.setDirty(false);

        // 设置组织名次列的显示高度
        int colNum = result.getColNum();
        int maxLenth2 = 0;
        AreaPosition area3 = AreaPosition.getInstance(4, 1, colNum, 1);
        CellPosition[] cellPoss = area3.split();
        for (int i = 0; i < cellPoss.length; i++) {
        	Object value = result.getCellValue(cellPoss[i]);
            if (value != null && value instanceof String) {
                String projectname = (String) value;
                int length = projectname.length();
                if (length > maxLenth2)
                	maxLenth2 = length;
            } 
        }
        int rowHeight = maxLenth2 / 6;
        if(maxLenth2 % 6 != 0) {
        	rowHeight = rowHeight + 1;
        } 
        result.getRowHeaderModel().setSize(4, rowHeight * 23);
        
        return result;
	}

	private ArrayList<Cell> getRowData(String[] showOrgSize,
			IProjectCellHead createProduct, int row,
			MeasureInfoVO[] measinfovos,
			ConcurrentHashMap<String, MeasureVO> measmap,WorkDraftTempVO draftvo)
			throws BusinessException {

		ArrayList<Cell> cellList = new ArrayList<Cell>();// 合计行数据
		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + 1)));// 合计数
		// 合计数
		setColum(row, measinfovos, measmap, cellList, totalmap);
		lstAll.add(cellList.toArray(new Cell[0]));
		row = row + 1;

		int lastRow = 4;
		//设置抵销借贷数行数
		switch(draftvo.getDxshowtype()){
	        case IWorkDraftConst.DXSHOWTYPE_NORMAL://抵销借贷
	        	lastRow = doRowDefault(showOrgSize, createProduct, row, measinfovos,measmap);
				break;
			case IWorkDraftConst.DXSHOWTYPE_DX_NORMAL://分类抵销借贷
				lastRow = doRowTypeDXCreAndDeb(showOrgSize, createProduct, row, measinfovos,measmap);
				break;
			case IWorkDraftConst.DXSHOWTYPE_DX_CLEAN://分类抵销净额
				lastRow = doRowTypeDXContrast(showOrgSize, createProduct, row, measinfovos,measmap);
				break;
			default:
				lastRow = doRowDefault(showOrgSize, createProduct, row, measinfovos,measmap);
				break;
		}

		// 合并数
		cellList = new ArrayList<Cell>();// 合并数行数据
		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + lastRow)));// 合并数
		setColum(row, measinfovos, measmap, cellList, unionMeasdatamap);
		return cellList;
	}

	private int doRowDefault(String[] showOrgSize,
			IProjectCellHead createProduct, int row,
			MeasureInfoVO[] measinfovos,
			ConcurrentHashMap<String, MeasureVO> measmap) {
		List<Cell> cellList = new ArrayList<Cell>();// 抵销借方行数据
		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + 2)));// 抵销借方
		setColum(row, measinfovos, measmap, cellList, getDebitmap());
		lstAll.add(cellList.toArray(new Cell[0]));
		row = row + 1;

		// 抵销贷方
		cellList = new ArrayList<Cell>();// 抵销贷方行数据
		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + 3)));// 抵销贷方
		setColum(row, measinfovos, measmap, cellList, getCreditmap());
		lstAll.add(cellList.toArray(new Cell[0]));
		row = row + 1;
		return 4;
	}

	@SuppressWarnings("unchecked")
	private int doRowTypeDXCreAndDeb(String[] showOrgSize,
			IProjectCellHead createProduct, int row,
			MeasureInfoVO[] measinfovos,
			ConcurrentHashMap<String, MeasureVO> measmap){
	    //分类抵销借贷方
        Set<String> sets = getTypeCreDebmap().keySet();
        if(sets.size() > 0){
        	int i = 2;
        	int j = 3;
            for(String s : sets){
            	List<Cell> cellList = new ArrayList<Cell>();// 分类抵销借方行数据
        		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + i)));// 抵销借方
        		setColum(row, measinfovos, measmap, cellList, (Map<String, Object>)getTypeCreDebmap().get(s)[0]);
        		lstAll.add(cellList.toArray(new Cell[0]));
        		row = row + 1;

        		// 抵销贷方
        		cellList = new ArrayList<Cell>();// 分类抵销贷方行数据
        		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + j)));// 抵销贷方
        		setColum(row, measinfovos, measmap, cellList, (Map<String, Object>)getTypeCreDebmap().get(s)[1]);
        		lstAll.add(cellList.toArray(new Cell[0]));
        		row = row + 1;
        		i = i + 2;
        		j = j + 2;
            }
            return j - 1;
        }
        return 2;
	}

	@SuppressWarnings("unchecked")
	private int doRowTypeDXContrast(String[] showOrgSize,
			IProjectCellHead createProduct, int row,
			MeasureInfoVO[] measinfovos,
			ConcurrentHashMap<String, MeasureVO> measmap){
		  //分类抵销净额
        Set<String> sets = getTypeCreDebmap().keySet();
        if(sets.size() > 0){
        	int i = 2;
            for(String s: sets){
            	List<Cell> cellList = new ArrayList<Cell>();// 分类抵销净额数据
        		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + i)));// 抵销借方
        		setColum(row, measinfovos, measmap, cellList, (Map<String, Object>)getTypeCreDebmap().get(s)[2]);
        		lstAll.add(cellList.toArray(new Cell[0]));
        		row = row + 1;
        		i++;
        	}
            return i;
        }
        return 2;
	}


    protected String getHBRepStruPK(MeasurePubDataVO pubVO, HBSchemeVO VO) {
        KeyGroupVO groupVO = pubVO.getKeyGroup();
        KeyVO[] keys = groupVO.getKeys();
        String dateValue = null;
        String pk_keyword = null;
        boolean isAccScheme = true;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].isTTimeKeyVO()) {
                pk_keyword = keys[i].getPk_keyword();
                String detailData = pubVO.getKeywordByPK(pk_keyword);
                dateValue = detailData;
                if (keys[i].isTimeKeyVO()) {
                    isAccScheme = false;
                }
                break;
            }
        }
        ReportCombineStruVersionVO memberVO = null;
        if (isAccScheme)
            memberVO = HBRepStruUtil.getHBStruVersionVO(pubVO.getAccSchemePK(), pk_keyword, dateValue,
                    VO.getPk_repmanastru());
        else
            memberVO = HBRepStruUtil.getHBStruVersionVO(dateValue, VO.getPk_repmanastru());
        return memberVO.getPk_vid();
    }

    protected void setColum(int row, MeasureInfoVO[] measinfovos, ConcurrentHashMap<String, MeasureVO> measmap,
            List<Cell> cellList, Map<String, Object> datamap) {
        for (int j = 0; j < measinfovos.length; j++) {
            int colnum = 0;
            MeasureVO vo = measmap.get(measinfovos[j].getPk_measure());
            cellList.add(IProjectCellHead.getDataCellBy(row, colnum + j + 1, vo, datamap));
        }
    }

    /**
     * 自定义工作底稿加载分类抵销借贷或净额数据：单位按列显示：逐行加载工作底稿数据
     * @param row
     * @param loadMeasureByReportPK
     * @param corps
     * @param vo
     * @param measinfovo
     * @param workdraft
     */
    @SuppressWarnings("unchecked")
	private void doTypeDXCreAndDeb(int row, MeasureVO[] loadMeasureByReportPK, String[] corps, MeasureVO vo,
            MeasureInfoVO measinfovo, IWorkDraft workdraft) {
        int col = 0;
        // 项目名称:
        Cell[] cells = null;
        cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        if (null == measinfovo) {
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        } else {
        	vo.setName(measinfovo.getName());
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        }
        col++;
        //单位对应的值
        HashSet<String> hashset = new HashSet<String>();
        if (workdraft.getShoworgs() != null) {
            hashset.addAll(Arrays.asList(workdraft.getShoworgs()));
        } else {
            for (int i = 0; i < corps.length; i++) {
                hashset.add(corps[i].substring(0, 20));
            }
        }
        for (int i = 0; i < corps.length; i++) {
            String pk_org = corps[i].substring(0, 20);
            ConcurrentHashMap<String, Object> srcdatamap = getSrcOrgMeasDataMap().get(pk_org);
            if (hashset.contains(pk_org)) {
                cells = StrTools.add(cells, IProjectCellHead.getDataCellBy(row, col, vo, srcdatamap));
                col++;
            }
        }
        // 合计数
        cells = StrTools.add(cells, IProjectCellHead.getDataCellBy(row, col, vo, totalmap));

        //分类抵销借贷方
        Set<String> sets = getTypeCreDebmap().keySet();
        if(sets.size() > 0){
            for(String s: sets){
            	Object[] objs = getTypeCreDebmap().get(s);
                cells = StrTools.add(cells, IProjectCellHead.getDebitAndCreditDataCellBy(row, col, vo, (Map<String, Object>)objs[0],ExCell.DEBIT));
                cells = StrTools.add(cells, IProjectCellHead.getDebitAndCreditDataCellBy(row, col, vo, (Map<String, Object>)objs[1],ExCell.CREDIT));
            }
        }

        // 合并数
        Cell dataCellBy = IProjectCellHead.getDataCellBy(row, col, vo, unionMeasdatamap);
        MeasureVO relamesurevos = workdraft.getRelamesurevos();
        if (relamesurevos != null && vo.getCode().equals(relamesurevos.getCode())) {
            IufoFormat format = (IufoFormat) dataCellBy.getFormat();
            CellFont font = (CellFont) format.getFont();
            ICellFont instance = CellFont.getInstance(font.getFontname(), font.getFontstyle(), font.getFontsize(),
                    Color.GRAY, font.getForegroundColor());
            IFormat instance2 = IufoFormat.getInstance(format.getDataFormat(), instance, format.getAlign(),
                    format.getLines());
            dataCellBy.setFormat(instance2);
        }
        cells = StrTools.add(cells, dataCellBy);
        lstAll.add(cells);
    }

    /**
     * 自定义工作底稿加载分类抵销净额数据：单位按列显示：逐行加载工作底稿数据
     * @param row
     * @param loadMeasureByReportPK
     * @param corps
     * @param vo
     * @param measinfovo
     * @param workdraft
     */
    @SuppressWarnings("unchecked")
	private void doTypeDXContrast(int row, MeasureVO[] loadMeasureByReportPK, String[] corps, MeasureVO vo,
            MeasureInfoVO measinfovo, IWorkDraft workdraft) {
        int col = 0;
        // 项目名称:
        Cell[] cells = null;
        cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        if (null == measinfovo) {
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        } else {
        	vo.setName(measinfovo.getName());
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        }
        col++;
        //单位对应的值
        HashSet<String> hashset = new HashSet<String>();
        if (workdraft.getShoworgs() != null) {
            hashset.addAll(Arrays.asList(workdraft.getShoworgs()));
        } else {
            for (int i = 0; i < corps.length; i++) {
                hashset.add(corps[i].substring(0, 20));
            }
        }
        for (int i = 0; i < corps.length; i++) {
            String pk_org = corps[i].substring(0, 20);
            ConcurrentHashMap<String, Object> srcdatamap = getSrcOrgMeasDataMap().get(pk_org);
            if (hashset.contains(pk_org)) {
                cells = StrTools.add(cells, IProjectCellHead.getDataCellBy(row, col, vo, srcdatamap));
                col++;
            }
        }
        // 合计数
        cells = StrTools.add(cells, IProjectCellHead.getDataCellBy(row, col, vo, totalmap));

        //分类抵销借贷方//
        Set<String> sets = getTypeCreDebmap().keySet();
        if(sets.size() > 0){
            for(String s: sets){
            	Object[] objs = getTypeCreDebmap().get(s);
                cells = StrTools.add(cells, IProjectCellHead.getDataCellBy(row, col, vo, (Map<String, Object>)objs[2]));
            }
        }

        // 合并数
        Cell dataCellBy = IProjectCellHead.getDataCellBy(row, col, vo, unionMeasdatamap);
        MeasureVO relamesurevos = workdraft.getRelamesurevos();
        if (relamesurevos != null && vo.getCode().equals(relamesurevos.getCode())) {
            IufoFormat format = (IufoFormat) dataCellBy.getFormat();
            CellFont font = (CellFont) format.getFont();
            ICellFont instance = CellFont.getInstance(font.getFontname(), font.getFontstyle(), font.getFontsize(),
                    Color.GRAY, font.getForegroundColor());
            IFormat instance2 = IufoFormat.getInstance(format.getDataFormat(), instance, format.getAlign(),
                    format.getLines());
            dataCellBy.setFormat(instance2);
        }
        cells = StrTools.add(cells, dataCellBy);
        lstAll.add(cells);
    }

    /**
     * 默认底稿加载抵销借贷：单位按列显示：逐行加载工作底稿数据
     * @param row
     * @param loadMeasureByReportPK
     * @param corps
     * @param vo
     * @param measinfovo
     * @param workdraft
     */
	private void doDefault(int row, MeasureVO[] loadMeasureByReportPK, String[] corps, MeasureVO vo,
            MeasureInfoVO measinfovo, IWorkDraft workdraft) {
        int col = 0;
        // 项目名称:
        Cell[] cells = null;

        cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        if (null == measinfovo) {
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        } else {
        	vo.setName(measinfovo.getName());
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        }
        col++;
        //单位对应的值
        HashSet<String> hashset = new HashSet<String>();
        if (workdraft.getShoworgs() != null) {
            hashset.addAll(Arrays.asList(workdraft.getShoworgs()));
        } else {
            for (int i = 0; i < corps.length; i++) {
                hashset.add(corps[i].substring(0, 20));
            }
        }
        for (int i = 0; i < corps.length; i++) {
            String pk_org = corps[i].substring(0, 20);
            ConcurrentHashMap<String, Object> srcdatamap = getSrcOrgMeasDataMap().get(pk_org);
            if (hashset.contains(pk_org)) {
                cells = StrTools.add(cells, IProjectCellHead.getDataCellBy(row, col, vo, srcdatamap));
                col++;
            }
        }
        // 合计数
        cells = StrTools.add(cells, IProjectCellHead.getDataCellBy(row, col, vo, totalmap));
        // 抵销借方
        cells = StrTools.add(cells, IProjectCellHead.getDebitAndCreditDataCellBy(row, col, vo, getDebitmap(),ExCell.DEBIT));
        // 抵销贷方
        cells = StrTools.add(cells, IProjectCellHead.getDebitAndCreditDataCellBy(row, col, vo, getCreditmap(),ExCell.CREDIT));
        // 合并数
        Cell dataCellBy = IProjectCellHead.getDataCellBy(row, col, vo, unionMeasdatamap);
        MeasureVO relamesurevos = workdraft.getRelamesurevos();
        if (relamesurevos != null && vo.getCode().equals(relamesurevos.getCode())) {
            IufoFormat format = (IufoFormat) dataCellBy.getFormat();
            CellFont font = (CellFont) format.getFont();
            ICellFont instance = CellFont.getInstance(font.getFontname(), font.getFontstyle(), font.getFontsize(),
                    Color.GRAY, font.getForegroundColor());
            IFormat instance2 = IufoFormat.getInstance(format.getDataFormat(), instance, format.getAlign(),
                    format.getLines());
            dataCellBy.setFormat(instance2);
        }
        cells = StrTools.add(cells, dataCellBy);
        lstAll.add(cells);
    }


    private void doDefaultWithOutDraft(int row, MeasureVO[] loadMeasureByReportPK, String[] corps, IWorkDraft workdraft) {
        for (MeasureVO vo : loadMeasureByReportPK) {
           doDefault(row, loadMeasureByReportPK, corps, vo, null, workdraft);
           row++;
        }
    }

    private void doDynRepDefaultWithOutDraft(int row, MeasureVO measureVO, String[] corps, IWorkDraft workdraft,String pk_unionorg,boolean bDicCopr) {
    	String[] oppOrgPks = getOppOrgPks();
    	if(oppOrgPks != null && oppOrgPks.length > 0){
    		//查出所有客商
    		Map<String, String> custNameMap = new HashMap<String, String>();
    		if(!bDicCopr){
    			try {
					ICustSupQueryService custQry = NCLocator.getInstance().lookup(ICustSupQueryService.class);
					CustSupplierVO[] custs = custQry.queryCustSupVO(oppOrgPks);
					if(custs != null && custs.length > 0){
						for(CustSupplierVO vo : custs){
							custNameMap.put(vo.getPk_cust_sup(), MultiLangTextUtil.getCurLangText(vo));
						}
					}
				} catch (BusinessException e) {
					AppDebug.debug(e.getMessage());
				}
    		}

    		for(int j = 0 ; j < oppOrgPks.length ;j++){
    			int col = 0;
 	           //动态区关键字名称：
 	           Cell[] cells = null;
 	           if(bDicCopr)
 	        	   cells = new Cell[] { IProjectCellHead.getDynNameCellBy(row, col, oppOrgPks[j]) };
 	           else
 	        	   cells = new Cell[] { IProjectCellHead.getDynNameCellByCust(row, col, custNameMap.get(oppOrgPks[j]),oppOrgPks[j]) };//客商名称
 	           col++;
 	           //单位对应的值
 	           HashSet<String> hashset = new HashSet<String>();
 	           if (workdraft.getShoworgs() != null) {
 	               hashset.addAll(Arrays.asList(workdraft.getShoworgs()));
 	           } else {
 	               for (int i = 0; i < corps.length; i++) {
 	                   hashset.add(corps[i].substring(0, 20));
 	               }
 	           }
 	           for (int i = 0; i < corps.length; i++) {
 	               String pk_org = corps[i].substring(0, 20);
 	               if (hashset.contains(pk_org)) {
 	                   cells = StrTools.add(cells, IProjectCellHead.getDynDataCellBy(row, col, measureVO, srcDynRepMap.get(pk_org + oppOrgPks[j])));
 	                   col++;
 	               }
 	           }
 	           //合计数
 	           cells = StrTools.add(cells, IProjectCellHead.getDynDataCellBy(row, col, measureVO, dynTotalMap.get(pk_unionorg + oppOrgPks[j])));
 	           //抵销数
 	           cells = StrTools.add(cells, IProjectCellHead.getDynDataCellBy(row, col, measureVO, dynDXContrastMap.get(pk_unionorg + oppOrgPks[j])));
 	           //合并数
 	           Cell dataCellBy = IProjectCellHead.getDynDataCellBy(row, col, measureVO, dynUnionMap.get(pk_unionorg + oppOrgPks[j]));
 	           MeasureVO relamesurevos = workdraft.getRelamesurevos();
 	           if (relamesurevos != null && measureVO.getCode().equals(relamesurevos.getCode())) {
 	               IufoFormat format = (IufoFormat) dataCellBy.getFormat();
 	               CellFont font = (CellFont) format.getFont();
 	               ICellFont instance = CellFont.getInstance(font.getFontname(), font.getFontstyle(), font.getFontsize(),
 	                       Color.GRAY, font.getForegroundColor());
 	               IFormat instance2 = IufoFormat.getInstance(format.getDataFormat(), instance, format.getAlign(),
 	                       format.getLines());
 	               dataCellBy.setFormat(instance2);
 	           }
 	           cells = StrTools.add(cells, dataCellBy);
 	           lstAll.add(cells);
 	           row++;
    		}
    	}
    }

    public HBSchemeVO getHbschemevo() {
        return schemevo;
    }

    public void setSchemevo(HBSchemeVO schemevo) {
		this.schemevo = schemevo;
	}

	private MeasurePubDataVO getParamPubData() {
        return paramPubData;
    }

    public void setParamPubData(MeasurePubDataVO paramPubData) {
        this.paramPubData = paramPubData;
    }

    private Map<String, ConcurrentHashMap<String, Object>> getSrcOrgMeasDataMap() {
        if (null == srcOrgMeasDataMap) {
            srcOrgMeasDataMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, Object>>();
        }
        return srcOrgMeasDataMap;
    }

    private Map<String, Object> getDebitmap() {
        if (null == debitmap) {
            debitmap = new ConcurrentHashMap<String, Object>();
        }
        return debitmap;
    }

    private Map<String, Object> getCreditmap() {
        if (null == creditmap) {
            creditmap = new ConcurrentHashMap<String, Object>();
        }
        return creditmap;
    }

    private Map<String, Object[]> getTypeCreDebmap() {
        if (null == typeCreDebMap) {
        	typeCreDebMap = new LinkedHashMap<String, Object[]>();
        }
        return typeCreDebMap;
    }

	/**
	 * 动态表的动态区所有关键字pk
	 * @return
	 */
	protected String[] getOppOrgPks(){
		if(srcDynRepMap.size() == 0)
			return null;
		Set<String> allOrgs = srcDynRepMap.keySet();
		Set<String> oppOrgs = new HashSet<String>();
		for(String s : allOrgs){
			oppOrgs.add(s.substring(20, 40));
		}
		return oppOrgs.toArray(new String[oppOrgs.size()]);
	}

	public Map<String, Map<String, UFDouble>> getDynTotalMap() {
		return dynTotalMap;
	}

	public void setDynTotalMap(Map<String, Map<String, UFDouble>> dynTotalMap) {
		this.dynTotalMap = dynTotalMap;
	}

	public Map<String, Map<String, UFDouble>> getDynDXContrastMap() {
		return dynDXContrastMap;
	}

	public void setDynDXContrastMap(
			Map<String, Map<String, UFDouble>> dynDXContrastMap) {
		this.dynDXContrastMap = dynDXContrastMap;
	}

	public Map<String, Map<String, UFDouble>> getDynUnionMap() {
		return dynUnionMap;
	}

	public void setDynUnionMap(Map<String, Map<String, UFDouble>> dynUnionMap) {
		this.dynUnionMap = dynUnionMap;
	}

}
