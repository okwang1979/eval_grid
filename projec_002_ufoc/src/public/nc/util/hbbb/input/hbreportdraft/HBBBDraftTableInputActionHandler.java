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
 * ����ϲ�����׸���ʾ
 * @date 20110602
 * @author liyra
 * @modify litfb@20120515 �޸�dataMap���ݽṹΪConcurrentHashMap<String, Object>,���ڴ���ָ�����Ͳ�����ֵ
 */
public class HBBBDraftTableInputActionHandler extends HBBBTableInputActionHandler {

    /** ��Ź�˾��Ӧ��ָ��,����*/
    private Map<String, ConcurrentHashMap<String, Object>> srcOrgMeasDataMap = null;
    /** ��źϼ���*/
    private Map<String, Object> totalmap = null;
    /** �ϲ���˾�ĵ����跽����<����ָ��,ֵ>*/
    private Map<String, Object> debitmap = null;
    /** �ϲ���˾�ĵ�����������<����ָ��,ֵ>*/
    private Map<String, Object> creditmap = null;
    /**����������ֵ*/
    private Map<String, Object[]> typeCreDebMap = new LinkedHashMap<String, Object[]>();
    /**�ϲ�����ָ������*/
    private ConcurrentHashMap<String, Object> unionMeasdatamap = null;
    /**�򿪹����׸崫�ݹ����Ĳ������ϲ��汾pubdata*/
    private MeasurePubDataVO paramPubData = null;
    // TODO: ���ã���μ�ס��ѡ��λ
    private static String[] showorgs = null;
    
    /**����ָ��vo*/
    protected MeasureVO[] loadMeasureByReportPK = null;

    protected HBSchemeVO schemevo = null;

    protected List<Cell[]> lstAll = null;


    //��̬��ԭ������<pk_selforg+pk_opporg,map<code,value>>
    protected Map<String, Map<String, UFDouble>> srcDynRepMap = new HashMap<String, Map<String,UFDouble>>();
    //��̬��ĺϼ���
    protected Map<String, Map<String, UFDouble>> dynTotalMap = new HashMap<String, Map<String,UFDouble>>();
    //��̬��ĵ�����
    protected Map<String, Map<String, UFDouble>> dynDXContrastMap = new HashMap<String, Map<String,UFDouble>>();
    //��̬��ĺϲ���
    protected Map<String, Map<String, UFDouble>> dynUnionMap = new HashMap<String, Map<String,UFDouble>>();



    /**
     * ���غϲ�������ӹ�˾��Ӧ�汾��MeasurePubDataVO
     * @param pk_org
     * @param isSingleTab//�Ƿ�ȡ�����౨����ǰ�ϲ���֯��Ҷ�ӽڵ�ȡ�����౨����������֯ȡ�ϲ��౨��
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
        //���ó���֯��Ϊ�������ؼ��ֵ�ֵ
        pubdata = MeasurePubDataUtil.getMeasurePubData(pubdata, getParamPubData(), new String[] { KeyVO.CORP_PK });

        pubdata.setVer(0);
        //ȡ���汾�������౨��
        if (isSingleTab) {
            if (getHbschemevo().getPk_adjustscheme() !=  null) {
            	AdjustSchemeVO adjustSchemeVo = (AdjustSchemeVO) HBBaseDocItfService.getRemoteUAPQueryBS().retrieveByPK(
                        AdjustSchemeVO.class, getHbschemevo().getPk_adjustscheme());
                pubdata.setVer(adjustSchemeVo.getVersion());
                pubdata.setAloneID(MeasurePubDataBO_Client.getAloneID(pubdata));
                boolean existadjrep = HBPubItfService.getRemoteAdjReport().existAdjReportByAloneid(pubdata.getAloneID(), pk_report);
                //���ڵ���������ȡ������������ȡ���𱨱�
                if (!existadjrep) {
                    pubdata.setVer(0);
                }
            }
        } else {
            pubdata.setVer(HBVersionUtil.getHBAdjustByHBSchemeVO(getHbschemevo()));
            pubdata.setAloneID(MeasurePubDataBO_Client.getAloneID(pubdata));
            boolean existadjrep = HBPubItfService.getRemoteAdjReport().existAdjReportByAloneid(pubdata.getAloneID(), pk_report);
            if (!existadjrep) {
                pubdata.setVer(getHbschemevo().getVersion());//ȡ�ϲ�����
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
          //���ó���֯��Ϊ�������ؼ��ֵ�ֵ
          rtnPubData = MeasurePubDataUtil.getMeasurePubData(rtnPubData,currentPubData, new String[] { KeyVO.CORP_PK });
          rtnPubData.setVer(ver);
          try {
			rtnPubData.setAloneID(MeasurePubDataBO_Client.getAloneID(rtnPubData));
		} catch (Exception e) {
			throw new BusinessRuntimeException("ͨ�����ùؼ��ֲ�ѯAloneId����");
		}
          return rtnPubData;
    
    	
    }

    /**
     * ���úϲ���֯��ÿ����֯������
     * @param corps
     * @param unionorg
     * @param pk_report
     * @param Pk_hbrepstru
     * @throws BusinessException
     */
    private void setSrcReportVOS(String[] corps, String unionorg, String pk_report, String Pk_hbrepstru)
            throws BusinessException {
    	//�õ���ʵ��Ӧ��ϵ
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
            	//JIAAH �鵥λ�������ǵ�ǰ�ĺϲ���֯������£����𱨱���������Ϊnull
            	if(isXuUnit && pk_org.equals(unionorg)){
            		 getSrcOrgMeasDataMap().put(pk_org, new ConcurrentHashMap<String, Object>());
            		 continue;
            	}
                RepDataVO[] vos = null;
                MeasurePubDataVO pubdata = null;
                //��ǰ��֯�Ƿ�����¼�
                if (HBPubItfService.getRemoteUnionReport().hasSubOrgs(innercode, Pk_hbrepstru)) {
                    if (pk_org.equals(unionorg)) {//�Ƿ��ǵ�ǰ�ϲ���֯
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
     * ���ö�̬���ÿ����֯�Ե�����
     * @param corps
     * @param unionorg
     * @param pk_report
     * @param Pk_hbrepstru
     * @throws BusinessException
     */
    protected void setSrcDynReportVOS(String[] corps, String unionorg, String pk_report, String Pk_hbrepstru)
            throws BusinessException {
    	//�õ���ʵ��Ӧ��ϵ
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
                //��ǰ��֯�Ƿ�����¼�
                if (HBPubItfService.getRemoteUnionReport().hasSubOrgs(innercode, Pk_hbrepstru)) {
                    if (pk_org.equals(unionorg)) {//�Ƿ��ǵ�ǰ�ϲ���֯
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
                		
                        //�Է���λ����ؼ��֣���ͨ��
                  		//JIAAH �鵥λ�������ǵ�ǰ�ĺϲ���֯������£����𱨱���������Ϊnull
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
     * ���ö�̬����汾����
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
//					�Է���λ����ؼ��֣���ͨ��
//					dynTotalMap.put(vo.getKeywordByPK(KeyVO.CORP_PK)+ vo.getKeywordByPK(KeyVO.DIC_CORP_PK), map);
            		KeyGroupVO subKeyGroup = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(datas[0].getMeasureVO().getKeyCombPK());
            		String pk_dynValues = HBKeyGroupUtil.getPk_dynKeyValue(subKeyGroup, schemevo.getPk_keygroup());
            		//�Է���λ����ؼ��֣���ͨ��
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
     * ���ط��������
     * @param pubdata
     * @param pk_report
     * @throws BusinessException
     */
    private List<String> getVouchTypes(MeasurePubDataVO pubdata, String pk_report,IWorkDraft workdraft) throws BusinessException {
    	MeasurePubDataVO vouchpubData = MeasurePubDataUtil.getMeasurePubdata(pubdata,IDataVersionConsts.VER_VOUCHER);
    	VouchHeadVO[] headVO = HBPubItfService.getRemoteVouchQry().getVouchHead(vouchpubData.getAloneID(), getHbschemevo(),
    			new int[]{IVouchType.TYPE_AUTO_ENTRY,IVouchType.TYPE_MANU_ENTRY},null);

    	List<String> typeseqs = new ArrayList<String>();
    	//�׸�ѡ���Ҫ��ʾ�����
    	WorkDraftTempVO tempvo = workdraft.getWorkDraftTempVO();
    	if(tempvo != null){
    		DXTypeSeqVO[] seqvo = tempvo.getDxtypeseq();
    		if(seqvo != null){
        		for(DXTypeSeqVO typevo : seqvo){
        			typeseqs.add(typevo.getPk_dxtype());
        		}
        	}
    	}

    	//ƾ֤���
    	Set<String> dxtypes = new HashSet<String>();
    	for(VouchHeadVO vo: headVO){
        	dxtypes.add(vo.getPk_dxtype());
        }

    	//Ҫ��ʾ�����
    	List<String> showType = new ArrayList<String>();
    	for(String s :typeseqs){
    		if(dxtypes.contains(s))
    			showType.add(s);
    	}
        return showType;
    }
    /**
     * �õ����������������������datamap
     * @param pubdata
     * @param pk_report
     * @throws BusinessException
     */
    private void setUnionVouchDataMap(List<String> setDxType,MeasurePubDataVO pubdata, String pk_report) throws BusinessException {
        //���÷���������������
        for(String pk_dxtype : setDxType){
        	int midVer = DXTypeValue.getTypeVer(pk_dxtype);//�汾�ŵ���׺
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

    	//���õ����������
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
        	return doDynRepDraft(strRepPK, param, workdraft);//�ڲ����ײɼ���׸�չ��
    	return doFixRepDraft(strRepPK, param, workdraft);
    }

    /**
     * ��̬��׸�չ��
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

        //���غϲ���Χ����֯
        String pk_unionorg = param.getPubData().getKeywordByPK(KeyVO.CORP_PK);
        schemevo = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(param.getTaskPK());
        String hbRepStruVersionPK = getHBRepStruPK(param.getPubData(), schemevo);
        String innercode = HBPubItfService.getRemoteUnionReport().getInnerCode(hbRepStruVersionPK, pk_unionorg);
        if (null == innercode || innercode.trim().length() == 0) {
            throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
            		.getStrByID("pub_0", "01830001-0502"));//"��ǰ�ϲ���˾û���ҵ�inner code"
        }
        String[] corps = HBPubItfService.getRemoteUnionReport().getUnionOrgs(hbRepStruVersionPK, innercode);//�õ�����ϲ�����֯

        //���ù����׸�ѡ��λ:��ʾ����֯������
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

        //ѡ��λ�ͺϲ���Χ�󽻼����ص׸�����ʾ����֯������:�����Ƹ�corps
        ArrayList<String> showCorps = new ArrayList<String>();
        for (int i = 0; i < corps.length; i++) {
            String string = corps[i].substring(0, 20);
            if (hashset.contains(string)) {
                showCorps.add(corps[i]);
            }
        }
        corps = showCorps.toArray(new String[showCorps.size()]);

        setParamPubData(param.getPubData());
        //����keymap
        KeyVO[] keys = param.getPubData().getKeyGroup().getKeys();
        String[] keywords = param.getPubData().getKeywords();
        Map<String, String> keyMap = new HashMap<String, String>();
        for (int i = 0; i < keys.length; i++) {
            keyMap.put(keys[i].getPk_keyword(), keywords[i]);
        }
        keyMap.put(KeyVO.CORP_PK, pk_unionorg);

        //�ϲ�pubdataVO
        MeasurePubDataVO unionPubDataVO = MeasurePubDataUtil.getMeasurePubdata(schemevo.getVersion(), true, pk_unionorg,
                schemevo.getPk_keygroup(), keyMap, schemevo.getPk_accperiodscheme());
        //�׸�ı���cell
        IProjectCellHead createProduct = ProjectCellHeadFactory.getInstance().CreateProduct(ProjectHead.UNIONREPORT,corps, workdraft,null,true);

        //1.1 ��ӱ�ͷ����-�ϲ���������׸�
        Cell[] titlecells = createProduct.getTitleCells(param.getReportPK(), workdraft);
        lstAll.add(titlecells);
        lstAll.add(new Cell[] { null });
        lstAll.add(new Cell[] { null });

        //1.2 ��ӹؼ�����Ϣ
        // ������ռ����
        int iTitleRow = 3;
        // ��ȡ�׸�ؼ�����Ϣ
        Cell[] keywordCell = getKeywordCell(iTitleRow, param);
        lstAll.add(keywordCell);

        // ȡ�õ׸�����
        MeasureCache measureCache = UFOCacheManager.getSingleton().getMeasureCache();
        // ��ȡ����ָ��
        if (workdraft.getMesurevos() != null) {
            loadMeasureByReportPK = workdraft.getMesurevos();
        } else {
            // ��ȡ����ָ��
            loadMeasureByReportPK = measureCache.loadMeasureByReportPK(param.getReportPK());
        }


        //1.3 �����ͷ������ʾ
        Cell[] headCells = createProduct.getProjectHeadCells();
        lstAll.add(headCells);

        // ׼���ϲ��ӹ�˾ԭ��̬������
        setSrcDynReportVOS(corps,param.getRepOrgPK(), param.getReportPK(), hbRepStruVersionPK);
        // ׼���ϼ�������
        MeasurePubDataVO totalpubData = MeasurePubDataUtil.getMeasurePubdata(
                HBVersionUtil.getHBTotalyHBSchemeVO(schemevo), true, pk_unionorg, schemevo.getPk_keygroup(), keyMap,
                schemevo.getPk_accperiodscheme());
        setDynReportVerDataVOS(param.getRepOrgPK(),param.getReportPK(),hbRepStruVersionPK,totalpubData,dynTotalMap);
        //׼���ϲ�
        setDynReportVerDataVOS(param.getRepOrgPK(),param.getReportPK(),hbRepStruVersionPK,unionPubDataVO,dynUnionMap);
        //׼����������
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
      	//ֻ��ʾ��̬��ָ��
        loadMeasureByReportPK = dynMeasures.toArray(new MeasureVO[dynMeasures.size()]);
        if(loadMeasureByReportPK != null && loadMeasureByReportPK.length > 0){
        	String pk_wordraft = workdraft.getPk_workdrafttemp();
        	WorkDraftTempVO draftvo = workdraft.getWorkDraftTempVO();
        	if(pk_wordraft != null && draftvo != null && draftvo.getPk_report().equals(workdraft.getPk_report())){
        		MeasureInfoVO[] measinfovos = workdraft.getMeasinfovos();// �׸������õ�ָ��
				ConcurrentHashMap<String, MeasureVO> measmap = new ConcurrentHashMap<String, MeasureVO>();// ��ǰ���������е�ָ��
				for (MeasureVO vo : loadMeasureByReportPK) {
					measmap.put(vo.getCode(), vo);
				}
				// ֻ�ǰ��յ�λ����չʾ�����м��ص׸�����
				if (measinfovos == null)
					throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0309")/*@res "�����׸�δ������ʾָ��"*/);
				Set<MeasureVO> drafMeasures = new HashSet<MeasureVO>();
				for (int j = 0; j < measinfovos.length; j++) {
					MeasureVO vo = measmap.get(measinfovos[j].getPk_measure());
					if(vo != null)
						drafMeasures.add(vo);
				}
				//�����׸���Ҫ��ʾ��ָ��
				loadMeasureByReportPK = drafMeasures.toArray(new MeasureVO[drafMeasures.size()]);
        	}

        	KeyGroupVO subKeyGroup = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(loadMeasureByReportPK[0].getKeyCombPK());
    		String pk_dynValues = HBKeyGroupUtil.getPk_dynKeyValue(subKeyGroup, schemevo.getPk_keygroup());
    		boolean bDicCorp = true;
    		if(!pk_dynValues.equals(KeyVO.DIC_CORP_PK)){
    			bDicCorp = false;
    		}
        	//��ָ����ص׸�����
        	for (int i = 0; i < loadMeasureByReportPK.length; i++) {
        		Cell cells = new Cell();
				cells.setValue(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0310")/*@res "ָ�꣺"*/ + loadMeasureByReportPK[i].getName());
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

        // ���ؼ��ֵĵڶ��������������Ԫ��ϲ�
        int j = 1;
        for (int i = 1; i < getKeyword(param.getPubData()).length; i++) {
            AreaPosition keyarea = AreaPosition.getInstance(3, j, 2, 1);
            result.getCombinedAreaModel().combineCell(keyarea);
            j = j + 2;
        }

        // ��̬�����еĳ��ȳߴ�
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
     * һ�����飬�������οؼ���
     * 
     * @param param
     * @return
     * @author: ��־ǿ
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
				 throw new BusinessRuntimeException("��ѯ�ϲ���Χ����pk is��"+repDataParam.getTaskPK(), e1);
			}
		    
	        String hbRepStruVersionPK = getHBRepStruPK(repDataParam.getPubData(), scheme);
	        String innercode = null;
			try {
				innercode = HBPubItfService.getRemoteUnionReport().getInnerCode(hbRepStruVersionPK, pk_unionorg);
			} catch (BusinessException e1) {
				 Logger.error(e1);
				 throw new BusinessRuntimeException("�����ѯ����pk_org:"+pk_unionorg,e1);
			}
	        
	    
	        
	        //����keymap
	        KeyVO[] keys = repDataParam.getPubData().getKeyGroup().getKeys();
	        String[] keywords = repDataParam.getPubData().getKeywords();
	        Map<String, String> keyMap = new HashMap<String, String>();
	        for (int i = 0; i < keys.length; i++) {
	            keyMap.put(keys[i].getPk_keyword(), keywords[i]);
	        }
	        keyMap.put(KeyVO.CORP_PK, pk_unionorg);

	        //�ϲ�pubdataVO
	        try {
				MeasurePubDataVO unionPubDataVO = MeasurePubDataUtil.getMeasurePubdata(scheme.getVersion(), true, pk_unionorg,
				        scheme.getPk_keygroup(), keyMap, scheme.getPk_accperiodscheme());
			} catch (Exception e1) {
				 Logger.error(e1);
				 throw new BusinessRuntimeException("��ѯMeasurePubDataVO����",e1);
			}
	        
	        

	        // ȡ�õ׸�����
	        MeasureCache measureCache = UFOCacheManager.getSingleton().getMeasureCache();
	        // ��ȡ����ָ��
	        
	         MeasureVO[]   loadMeasureByReportPK = workDraf.getMesurevos();
	         
	         // ׼���ϲ��ӹ�˾����
	         String org = repDataParam.getRepOrgPK();
	         if(org == null)
	         	org = pk_unionorg;
	         
	         
	         ILinkEndQuery query = NCLocator.getInstance().lookup(ILinkEndQuery.class);
		     Map<String,String> orgInnerCodeMap = query.getUnionOrgsOrderByCode(hbRepStruVersionPK, innercode, null);
				
		        if(orgInnerCodeMap.keySet().size()>300){
		        	throw new BusinessRuntimeException("��ѡ��֯�¼�����300�����趨���鼶��!");
		        }
		     IBasicOrgUnitQryService orgService = NCLocator.getInstance().lookup(IBasicOrgUnitQryService.class);
		     OrgVO[] useOrgs = null;
		    
		     try {
		    	 useOrgs = orgService.getOrgs(orgInnerCodeMap.keySet().toArray(new String[0]));
			} catch (BusinessException e1) {
				 Logger.error(e1);
				 throw new BusinessRuntimeException("��ѯ��Ӧ�������",e1);
			}
		     
		     Map<String, OrgVO> orgCachMap = new HashMap<>();
		     for(OrgVO useOrg:useOrgs){
		    	 orgCachMap.put(useOrg.getPk_org(), useOrg);
		     }
		       //�Ƿ����鵥λ
	         Map<String, UFBoolean> notEntityorgMap = null;
	         try {
	        	 notEntityorgMap = HBRepStruUtil.getBooleanEntityOrgs(orgInnerCodeMap.keySet().toArray(new String[0]), hbRepStruVersionPK);
			} catch (BusinessException e) {
				 Logger.error(e);
				 throw new BusinessRuntimeException("��ѯ���������",e);
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
	        
	         //�ж���ѡ��֯���1.�ϲ�����֯����ѯ�ϲ�������,�¼����ϼ����������裬��������2���ϲ�ʵ��֯����ѯ�ϲ������ݣ���ѯ���������ݡ�3.ĩ����֯:��ѯ���������
	         
	         	//����������֯
	         
	      
	         
	         
	         //��������ϲ���
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
//				throw new BusinessRuntimeException("��ѯ��������ϲ����ݴ���!",e);
//			}
//			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(orgCachMap.get(org).getName()+"(�ϲ���)");
//	         if(vos!=null&&vos.length>0){
//		         RepDataVO repData = vos[0];
//		         LinkEndTreeUserObj rootObj = getUserObj(repData, loadMeasureByReportPK);
//		         rootObj.setOrgDisName(orgCachMap.get(org).getName());
//		         rootObj.setReportTypeName("�ϲ���");
//	         }
//	         //�ϲ���
	         DefaultMutableTreeNode root = getLindNode(LinkEndTreeUserObj.TYPENAME_HBS,scheme.getVersion(),orgCachMap.get(org).getName(),org, innercode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
	         //�ϼ���
	         DefaultMutableTreeNode root_hjs = getLindNode(LinkEndTreeUserObj.TYPENAME_HJS,HBVersionUtil.getHBTotalyHBSchemeVO(scheme),orgCachMap.get(org).getName(),org, innercode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
	         root.add(root_hjs);
	         //�������
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
	        		
	        	 //�ϼ���	
	   	         DefaultMutableTreeNode xOrgNode = getLindNode(LinkEndTreeUserObj.TYPENAME_HBS,scheme.getVersion(),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
		         //�ϼ���
		         DefaultMutableTreeNode xOrgNode_hjs = getLindNode(LinkEndTreeUserObj.TYPENAME_HJS,HBVersionUtil.getHBTotalyHBSchemeVO(scheme),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
		         xOrgNode.add(xOrgNode_hjs);
		         //�������
		         DefaultMutableTreeNode xOrgNodet_dxj = getLindNode(LinkEndTreeUserObj.TYPENAME_DXJ,HBVersionUtil.getHBBB_debit_src(scheme.getCode()),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
		         xOrgNode.add(xOrgNodet_dxj);
		         DefaultMutableTreeNode xOrgNode_dxd = getLindNode(LinkEndTreeUserObj.TYPENAME_DXD,HBVersionUtil.getHBBB_credit_src(scheme.getCode()),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
		         xOrgNode.add(xOrgNode_dxd);

	        	 getCacheNode(nodeMap,org_innerCode).add(xOrgNode)	;
	        		
	        	   nodeMap.put(org_innerCode, xOrgNode);	
	        		
	        		continue;
	        		
	        	}
	        	
	        	 //��ǰ�����Ƿ�����¼�
	        	boolean hasSubOrg = false;
	        	 try {
	        		 hasSubOrg = HBPubItfService.getRemoteUnionReport().hasSubOrgs(org_innerCode, hbRepStruVersionPK);
				} catch (BusinessException e) {
					 Logger.error(e);
					 throw new BusinessRuntimeException("��ѯ���������",e);
				}
	        	 
	        	 
	        	 if(hasSubOrg){
	        		 
	        		 //�ϼ���
		   	         DefaultMutableTreeNode entityOrgNode = getLindNode(LinkEndTreeUserObj.TYPENAME_HBS,scheme.getVersion(),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
		   	         //�ϼ���
			         DefaultMutableTreeNode xOrgNode_hjs = getLindNode(LinkEndTreeUserObj.TYPENAME_HJS,HBVersionUtil.getHBTotalyHBSchemeVO(scheme),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
			         entityOrgNode.add(xOrgNode_hjs);
			         //�������
			         DefaultMutableTreeNode xOrgNodet_dxj = getLindNode(LinkEndTreeUserObj.TYPENAME_DXJ,HBVersionUtil.getHBBB_debit_src(scheme.getCode()),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
			         entityOrgNode.add(xOrgNodet_dxj);
			         DefaultMutableTreeNode xOrgNode_dxd = getLindNode(LinkEndTreeUserObj.TYPENAME_DXD,HBVersionUtil.getHBBB_credit_src(scheme.getCode()),orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
			         entityOrgNode.add(xOrgNode_dxd);
			         
			         DefaultMutableTreeNode self_node = getLindNode(LinkEndTreeUserObj.TYPENAME_GBB,0,orgCachMap.get(pk_org).getName(),pk_org, org_innerCode,repDataParam.getPubData(),repDataParam.getReportPK(), scheme,loadMeasureByReportPK,hbRepStruVersionPK);
			         entityOrgNode.add(self_node);

		        	 getCacheNode(nodeMap,org_innerCode).add(entityOrgNode)	;
		        	 
		        	   nodeMap.put(org_innerCode, entityOrgNode);	
	        	 }else{
	        		 
	        		 //�������
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
    		BusinessRuntimeException ex = new BusinessRuntimeException("�ϼ�����δ���ɣ�");
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
			throw new BusinessRuntimeException("��ѯ��������ϲ����ݴ���!",e);
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
     * �̶���׸�չ��
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

        //���غϲ���Χ����֯
        String pk_unionorg = param.getPubData().getKeywordByPK(KeyVO.CORP_PK);
        schemevo = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(param.getTaskPK());
        String hbRepStruVersionPK = getHBRepStruPK(param.getPubData(), schemevo);
        String innercode = HBPubItfService.getRemoteUnionReport().getInnerCode(hbRepStruVersionPK, pk_unionorg);
        if (null == innercode || innercode.trim().length() == 0) {
            throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
            		.getStrByID("pub_0", "01830001-0502"));//"��ǰ�ϲ���˾û���ҵ�inner code"
        }
        String[] corps = HBPubItfService.getRemoteUnionReport().getUnionOrgsOrderByCode(hbRepStruVersionPK, innercode);//�õ�����ϲ�����֯

        //���ù����׸�ѡ��λ:��ʾ����֯������
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

        //ѡ��λ�ͺϲ���Χ�󽻼����ص׸�����ʾ����֯������:�����Ƹ�corps
        ArrayList<String> showCorps = new ArrayList<String>();
        for (int i = 0; i < corps.length; i++) {
            String string = corps[i].substring(0, 20);
            if (hashset.contains(string)) {
                showCorps.add(corps[i]);
            }
        }
        corps = showCorps.toArray(new String[showCorps.size()]);

        setParamPubData(param.getPubData());
        //����keymap
        KeyVO[] keys = param.getPubData().getKeyGroup().getKeys();
        String[] keywords = param.getPubData().getKeywords();
        Map<String, String> keyMap = new HashMap<String, String>();
        for (int i = 0; i < keys.length; i++) {
            keyMap.put(keys[i].getPk_keyword(), keywords[i]);
        }
        keyMap.put(KeyVO.CORP_PK, pk_unionorg);

        //�ϲ�pubdataVO
        MeasurePubDataVO unionPubDataVO = MeasurePubDataUtil.getMeasurePubdata(schemevo.getVersion(), true, pk_unionorg,
                schemevo.getPk_keygroup(), keyMap, schemevo.getPk_accperiodscheme());
        //���ص���ƾ֤���
        List<String> dxtypepk = getVouchTypes(unionPubDataVO, strRepPK,workdraft);
        //�׸�ı���cell
        IProjectCellHead createProduct = ProjectCellHeadFactory.getInstance().CreateProduct(ProjectHead.UNIONREPORT,corps, workdraft,dxtypepk,false);

        //1.1 ��ӱ�ͷ����-�ϲ���������׸�
        Cell[] titlecells = createProduct.getTitleCells(param.getReportPK(), workdraft);
        lstAll.add(titlecells);
        lstAll.add(new Cell[] { null });
        lstAll.add(new Cell[] { null });

        //1.2 ��ӹؼ�����Ϣ
        // ������ռ����
        int iTitleRow = 3;
        // ��ȡ�׸�ؼ�����Ϣ
        Cell[] keywordCell = getKeywordCell(iTitleRow, param);
        lstAll.add(keywordCell);

        //1.3 �����ͷ������ʾ
        Cell[] headCells = createProduct.getProjectHeadCells();
        lstAll.add(headCells);

        //1.4 ������������ʾ������£���ӵڶ�����ͷ����
        if (workdraft != null&& !StringUtil.isEmptyWithTrim(workdraft.getPk_workdrafttemp())
        		&& workdraft.getWorkDraftTempVO().getUnitshowtype() == IWorkDraftConst.UNITSHOWTYPE_COL) {
        	int type = workdraft.getWorkDraftTempVO().getDxshowtype();
        	if(type == IWorkDraftConst.DXSHOWTYPE_DX_NORMAL){
        		int width = 1 + corps.length + 1 + dxtypepk.size()*2 + 1;
        		Cell[] cells = new Cell[width];
        		for(int i = width - dxtypepk.size()*2 - 1 ; i < width ; i++){
        			cells[i] = null;
        			if(i >= width - dxtypepk.size()*2 - 1 && i != width -1){
        				cells[i] = IProjectCellHead.getHeadCellBy(5, i, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0311")/*@res "������"*/);
        				i = i + 1;
        				cells[i] = IProjectCellHead.getHeadCellBy(5, i, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0312")/*@res "������"*/);
        			}
        		}
                lstAll.add(cells);
        	}
        }

        // ȡ�õ׸�����
        MeasureCache measureCache = UFOCacheManager.getSingleton().getMeasureCache();
        // ��ȡ����ָ��
        if (workdraft.getMesurevos() != null) {
            loadMeasureByReportPK = workdraft.getMesurevos();
        } else {
            // ��ȡ����ָ��
            loadMeasureByReportPK = measureCache.loadMeasureByReportPK(param.getReportPK());
        }
        // ׼��ָ����Ŀӳ��
//        measureProjectMap = HBBaseDocItfService.getRemoteHBProjectMapQry().loadMappingsByReportId(param.getReportPK());

        // ׼���ϲ��ӹ�˾����
        String org = param.getRepOrgPK();
        if(org == null)
        	org = pk_unionorg;
        setSrcReportVOS(corps,org, param.getReportPK(), hbRepStruVersionPK);
        // ׼���ϼ�������
        MeasurePubDataVO totalpubData = MeasurePubDataUtil.getMeasurePubdata(
                HBVersionUtil.getHBTotalyHBSchemeVO(schemevo), true, pk_unionorg, schemevo.getPk_keygroup(), keyMap,
                schemevo.getPk_accperiodscheme());
        RepDataVO totaldatavo = HBPubItfService.getRemoteRepDataQry().loadRepData(param.getReportPK(), pk_unionorg, totalpubData,
                param.getRepMngStructPK())[0];
        totalmap = IProjectCellHead.getMeasdataMap(totaldatavo);
        // ׼���ϲ���������
        RepDataVO hbRepDatavo = HBPubItfService.getRemoteRepDataQry().loadRepData(param.getReportPK(),
        		pk_unionorg,unionPubDataVO, param.getRepMngStructPK())[0];
        unionMeasdatamap = IProjectCellHead.getMeasdataMap(hbRepDatavo);

        // ׼�����������ƾ֤����
        setUnionVouchDataMap(dxtypepk,unionPubDataVO, strRepPK);

        int row = 5;
        // ��ָ�����һ������
        loadMeasureByReportPK = MeasureUtil.getSortByPositionMeasureVOs(loadMeasureByReportPK, strRepPK);
        if (null != workdraft.getPk_workdrafttemp() && workdraft.getPk_workdrafttemp().trim().length() > 0) {
            // ʹ���˵׸�ģ��
            WorkDraftTempVO draftvo = workdraft.getWorkDraftTempVO();
            if (draftvo.getPk_report().equals(workdraft.getPk_report())) {
                MeasureInfoVO[] measinfovos = workdraft.getMeasinfovos();//�׸������õ�ָ��
                // @edit by zhoushuang at 2015-7-11,����3:22:52 û��ָ��ĵ׸�--�ϲ�����,�׸�򿪱���
                if(measinfovos == null){
            		throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0309")/*@res "�����׸�δ������ʾָ��"*/);
                }
                ConcurrentHashMap<String, MeasureVO> measmap = new ConcurrentHashMap<String, MeasureVO>();//��ǰ���������е�ָ��
                for (MeasureVO vo : loadMeasureByReportPK) {
                    measmap.put(vo.getCode(), vo);
                }
                //��λ����չʾ�����м��ص׸�����
                if (draftvo.getUnitshowtype() == IWorkDraftConst.UNITSHOWTYPE_COL) {
                	for (int j = 0; j < measinfovos.length; j++) {
                        MeasureVO vo = measmap.get(measinfovos[j].getPk_measure());
                        switch(draftvo.getDxshowtype()){
	                        case IWorkDraftConst.DXSHOWTYPE_NORMAL://�������
	                        	doDefault(row, loadMeasureByReportPK, corps, vo, measinfovos[j], workdraft);
	        					break;
	        				case IWorkDraftConst.DXSHOWTYPE_DX_NORMAL://����������
	        					doTypeDXCreAndDeb(row, loadMeasureByReportPK, corps, vo, measinfovos[j], workdraft);
	        					break;
	        				case IWorkDraftConst.DXSHOWTYPE_DX_CLEAN://�����������
	        					doTypeDXContrast(row, loadMeasureByReportPK, corps, vo, measinfovos[j], workdraft);
	        					break;
	        				default:
	        					doDefault(row, loadMeasureByReportPK, corps, vo, measinfovos[j], workdraft);
	        					break;
                        }
                        row++;
                    }
                } else {
                    // ��λ����չʾ
                    // �ȵ�λ����,������λ��Ӧ��ָ������
                	for (int i = 0; i < corps.length; i++) {
            		    ArrayList<Cell> cellList = new ArrayList<Cell>();// ��λ������
            		    int colnum = 0;
            		    String name = HBPubItfService.getRemoteOrgUnit().getOrg(corps[i].substring(0, 20)).getName();
            		    cellList.add(IProjectCellHead.getHeadCellBy(row + i, colnum, name));// ��λ����;
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

        //�ϲ���Ԫ�񣺷�������ڵ�
        if (workdraft != null&& !StringUtil.isEmptyWithTrim(workdraft.getPk_workdrafttemp())
        		&& workdraft.getWorkDraftTempVO().getUnitshowtype() == IWorkDraftConst.UNITSHOWTYPE_COL) {
        	int type = workdraft.getWorkDraftTempVO().getDxshowtype();
        	if(type == IWorkDraftConst.DXSHOWTYPE_DX_NORMAL){
        		//��Ŀ����˾���ϼ���
                for(int i = 0 ; i < trow - dxtypepk.size()*2 - 1; i++){
                	AreaPosition areax = AreaPosition.getInstance(4, i, 1, 2);
                    result.getCombinedAreaModel().combineCell(areax);
                }
                //������������
                for(int j = trow - dxtypepk.size()*2 - 1;j < trow -1;j=j+2){
                	AreaPosition areax = AreaPosition.getInstance(4, j, 2, 1);
                    result.getCombinedAreaModel().combineCell(areax);

                    CellPosition w = CellPosition.getInstance(4, j);
                    Cell cell = result.getCell(w);
                    cell.setFormat(headCell.getFormat());
                    cell.setValue(cell.getValue().toString().substring(0, 3));
                }
                //�ϲ���
                AreaPosition areax = AreaPosition.getInstance(4, trow-1, 1, 2);
                result.getCombinedAreaModel().combineCell(areax);
        	}
        }

        // ���ؼ��ֵĵڶ��������������Ԫ��ϲ�
        int j = 1;
        for (int i = 1; i < getKeyword(param.getPubData()).length; i++) {
            AreaPosition keyarea = AreaPosition.getInstance(3, j, 2, 1);
            result.getCombinedAreaModel().combineCell(keyarea);
            j = j + 2;
        }

        // ��̬�����еĳ��ȳߴ�
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

        // ������֯�����е���ʾ�߶�
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

		ArrayList<Cell> cellList = new ArrayList<Cell>();// �ϼ�������
		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + 1)));// �ϼ���
		// �ϼ���
		setColum(row, measinfovos, measmap, cellList, totalmap);
		lstAll.add(cellList.toArray(new Cell[0]));
		row = row + 1;

		int lastRow = 4;
		//���õ������������
		switch(draftvo.getDxshowtype()){
	        case IWorkDraftConst.DXSHOWTYPE_NORMAL://�������
	        	lastRow = doRowDefault(showOrgSize, createProduct, row, measinfovos,measmap);
				break;
			case IWorkDraftConst.DXSHOWTYPE_DX_NORMAL://����������
				lastRow = doRowTypeDXCreAndDeb(showOrgSize, createProduct, row, measinfovos,measmap);
				break;
			case IWorkDraftConst.DXSHOWTYPE_DX_CLEAN://�����������
				lastRow = doRowTypeDXContrast(showOrgSize, createProduct, row, measinfovos,measmap);
				break;
			default:
				lastRow = doRowDefault(showOrgSize, createProduct, row, measinfovos,measmap);
				break;
		}

		// �ϲ���
		cellList = new ArrayList<Cell>();// �ϲ���������
		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + lastRow)));// �ϲ���
		setColum(row, measinfovos, measmap, cellList, unionMeasdatamap);
		return cellList;
	}

	private int doRowDefault(String[] showOrgSize,
			IProjectCellHead createProduct, int row,
			MeasureInfoVO[] measinfovos,
			ConcurrentHashMap<String, MeasureVO> measmap) {
		List<Cell> cellList = new ArrayList<Cell>();// �����跽������
		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + 2)));// �����跽
		setColum(row, measinfovos, measmap, cellList, getDebitmap());
		lstAll.add(cellList.toArray(new Cell[0]));
		row = row + 1;

		// ��������
		cellList = new ArrayList<Cell>();// ��������������
		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + 3)));// ��������
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
	    //������������
        Set<String> sets = getTypeCreDebmap().keySet();
        if(sets.size() > 0){
        	int i = 2;
        	int j = 3;
            for(String s : sets){
            	List<Cell> cellList = new ArrayList<Cell>();// ��������跽������
        		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + i)));// �����跽
        		setColum(row, measinfovos, measmap, cellList, (Map<String, Object>)getTypeCreDebmap().get(s)[0]);
        		lstAll.add(cellList.toArray(new Cell[0]));
        		row = row + 1;

        		// ��������
        		cellList = new ArrayList<Cell>();// �����������������
        		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + j)));// ��������
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
		  //�����������
        Set<String> sets = getTypeCreDebmap().keySet();
        if(sets.size() > 0){
        	int i = 2;
            for(String s: sets){
            	List<Cell> cellList = new ArrayList<Cell>();// ���������������
        		cellList.add(IProjectCellHead.getHeadCellBy(row, 0, createProduct.getHeads().get(showOrgSize.length + i)));// �����跽
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
     * �Զ��幤���׸���ط����������򾻶����ݣ���λ������ʾ�����м��ع����׸�����
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
        // ��Ŀ����:
        Cell[] cells = null;
        cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        if (null == measinfovo) {
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        } else {
        	vo.setName(measinfovo.getName());
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        }
        col++;
        //��λ��Ӧ��ֵ
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
        // �ϼ���
        cells = StrTools.add(cells, IProjectCellHead.getDataCellBy(row, col, vo, totalmap));

        //������������
        Set<String> sets = getTypeCreDebmap().keySet();
        if(sets.size() > 0){
            for(String s: sets){
            	Object[] objs = getTypeCreDebmap().get(s);
                cells = StrTools.add(cells, IProjectCellHead.getDebitAndCreditDataCellBy(row, col, vo, (Map<String, Object>)objs[0],ExCell.DEBIT));
                cells = StrTools.add(cells, IProjectCellHead.getDebitAndCreditDataCellBy(row, col, vo, (Map<String, Object>)objs[1],ExCell.CREDIT));
            }
        }

        // �ϲ���
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
     * �Զ��幤���׸���ط�������������ݣ���λ������ʾ�����м��ع����׸�����
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
        // ��Ŀ����:
        Cell[] cells = null;
        cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        if (null == measinfovo) {
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        } else {
        	vo.setName(measinfovo.getName());
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        }
        col++;
        //��λ��Ӧ��ֵ
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
        // �ϼ���
        cells = StrTools.add(cells, IProjectCellHead.getDataCellBy(row, col, vo, totalmap));

        //������������//
        Set<String> sets = getTypeCreDebmap().keySet();
        if(sets.size() > 0){
            for(String s: sets){
            	Object[] objs = getTypeCreDebmap().get(s);
                cells = StrTools.add(cells, IProjectCellHead.getDataCellBy(row, col, vo, (Map<String, Object>)objs[2]));
            }
        }

        // �ϲ���
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
     * Ĭ�ϵ׸���ص����������λ������ʾ�����м��ع����׸�����
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
        // ��Ŀ����:
        Cell[] cells = null;

        cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        if (null == measinfovo) {
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        } else {
        	vo.setName(measinfovo.getName());
            cells = new Cell[] { IProjectCellHead.getValueCellBy(row, col, vo) };
        }
        col++;
        //��λ��Ӧ��ֵ
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
        // �ϼ���
        cells = StrTools.add(cells, IProjectCellHead.getDataCellBy(row, col, vo, totalmap));
        // �����跽
        cells = StrTools.add(cells, IProjectCellHead.getDebitAndCreditDataCellBy(row, col, vo, getDebitmap(),ExCell.DEBIT));
        // ��������
        cells = StrTools.add(cells, IProjectCellHead.getDebitAndCreditDataCellBy(row, col, vo, getCreditmap(),ExCell.CREDIT));
        // �ϲ���
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
    		//������п���
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
 	           //��̬���ؼ������ƣ�
 	           Cell[] cells = null;
 	           if(bDicCopr)
 	        	   cells = new Cell[] { IProjectCellHead.getDynNameCellBy(row, col, oppOrgPks[j]) };
 	           else
 	        	   cells = new Cell[] { IProjectCellHead.getDynNameCellByCust(row, col, custNameMap.get(oppOrgPks[j]),oppOrgPks[j]) };//��������
 	           col++;
 	           //��λ��Ӧ��ֵ
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
 	           //�ϼ���
 	           cells = StrTools.add(cells, IProjectCellHead.getDynDataCellBy(row, col, measureVO, dynTotalMap.get(pk_unionorg + oppOrgPks[j])));
 	           //������
 	           cells = StrTools.add(cells, IProjectCellHead.getDynDataCellBy(row, col, measureVO, dynDXContrastMap.get(pk_unionorg + oppOrgPks[j])));
 	           //�ϲ���
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
	 * ��̬��Ķ�̬�����йؼ���pk
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
