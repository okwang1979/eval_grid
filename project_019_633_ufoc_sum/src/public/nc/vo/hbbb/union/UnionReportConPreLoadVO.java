package nc.vo.hbbb.union;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.itf.iufo.commit.ICommitQueryService;
import nc.pub.hbbb.exception.UFOCUnThrowableException;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.HBVersionUtil;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.vo.hbbb.adjustscheme.AdjustSchemeVO;
import nc.vo.hbbb.hbscheme.HBSchemeConstants;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.commit.RepDataCommitVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;

import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.table.CellsModel;

/**
 * �ϲ�����ĺϲ�����Ԥ��������
 * @author jiaah
 *
 */
public class UnionReportConPreLoadVO {
	//��ǰ�ϲ���֯
	private String pk_unionOrg;
	
	//�ϲ���ϵ
	private String pk_hbstru;
	
	//��ǰĳһ���ε����кϲ���֯
	private String[] unionorgs;
	
	private UnionReportQryVO unionqryVO;
	
	private String[] repids;
	
	//������֯����ʵ��λ��Ӧ
	private Map<String, UFBoolean> oppEntityOrgs = null; 
	
	//��ǰ�ĺϲ���֯�Ƿ�Ҷ�ӽڵ�
	private Map<String, UFBoolean> isLeafOrgMaps = null; 

	//ִ�кϲ�ʱ��֯ѡ�õı���İ汾--org->MeasurePubDataVO(����aloneid)
	private Map<String, Map<String, MeasurePubDataVO>> pubDataVOs = null; 
	
    //Ԥ�������еĸ�ʽ̬cellsmodel
	private Map<String,CellsModel> preFormatModel = null;
	
	public UnionReportConPreLoadVO(String[] unionorgs,String[] pk_reports, UnionReportQryVO qryVo){
		this.pk_unionOrg = qryVo.getUnionorg();
		this.pk_hbstru = qryVo.getPk_hbrepstru();
		this.unionorgs = unionorgs;
		this.unionqryVO = qryVo;
		this.repids = pk_reports;
	}
	
	public Map<String, UFBoolean> getOrgEntityMaps() throws BusinessException{
		if(oppEntityOrgs == null)
			oppEntityOrgs = HBRepStruUtil.getBooleanEntityOrgs(unionorgs, pk_hbstru);
		return oppEntityOrgs;
	}
	
	public Map<String, UFBoolean> getOrgIsLeafMemberMaps() throws BusinessException{
		if(isLeafOrgMaps == null)
			 isLeafOrgMaps = HBBaseDocItfService.getRemoteHBRepStru().isLeafMembers(unionorgs, pk_hbstru);
		return isLeafOrgMaps;
	}
	
	public Map<String, Map<String, MeasurePubDataVO>> getPubDataVosMaps() throws Exception{
		if(pubDataVOs == null){
			Map<String, Map<String, MeasurePubDataVO>> allmap = new HashMap<String, Map<String,MeasurePubDataVO>>();
			String pk_keygroup = unionqryVO.getSchemeVo().getPk_keygroup();
			KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
			
			String pk_adjscheme = unionqryVO.getSchemeVo().getPk_adjustscheme() ;
			AdjustSchemeVO adjustSchemeVo = null;
			if (null != pk_adjscheme && pk_adjscheme.trim().length() > 0) {
				adjustSchemeVo = (AdjustSchemeVO) HBBaseDocItfService.getRemoteUAPQueryBS().retrieveByPK(AdjustSchemeVO.class,pk_adjscheme);
			}
			
			int hbversion = unionqryVO.getSchemeVo().getVersion();
			int sepadjversion = -1;
			if(null != adjustSchemeVo)
				sepadjversion = adjustSchemeVo.getVersion();
			int hbadjversion = HBVersionUtil.getHBAdjustByHBSchemeVO(unionqryVO.getSchemeVo(), adjustSchemeVo);
			
			Map<String, MeasurePubDataVO[]> withAlonMap = new HashMap<String, MeasurePubDataVO[]>();
			for(String pk_org : unionorgs){
				List<MeasurePubDataVO> lstPubdatas = new ArrayList<MeasurePubDataVO>();
				MeasurePubDataVO pubdata = new MeasurePubDataVO();
				pubdata.setKType(pk_keygroup);
				pubdata.setKeyGroup(keygroupVo);
				String[] keys = new String[this.unionqryVO.getKeymap().size()];
				this.unionqryVO.getKeymap().keySet().toArray(keys);
				if (null != keys && keys.length > 0) {
					for (String key : keys) {
						pubdata.setKeywordByPK(key, unionqryVO.getKeymap().get(key));
					}
				}
				pubdata.setKeywordByPK(KeyVO.CORP_PK, pk_org);
			
				MeasurePubDataVO seppubdata = (MeasurePubDataVO) pubdata.clone();
				seppubdata.setVer(0);
				MeasurePubDataVO hbpubdata = (MeasurePubDataVO) pubdata.clone();
				hbpubdata.setVer(hbversion);
				MeasurePubDataVO sepAdjpubdata = (MeasurePubDataVO) pubdata.clone();
				MeasurePubDataVO hbadjpubdata = (MeasurePubDataVO) pubdata.clone();
				if(null != adjustSchemeVo){
					sepAdjpubdata.setVer(sepadjversion);
					hbadjpubdata.setVer(hbadjversion);
					lstPubdatas.add(sepAdjpubdata);
					lstPubdatas.add(hbadjpubdata);
				}
				lstPubdatas.add(seppubdata);
				lstPubdatas.add(hbpubdata);
				
				MeasurePubDataVO[] withAlondIdPubDatas = MeasurePubDataBO_Client.findByKeywordArray(lstPubdatas.toArray(new MeasurePubDataVO[lstPubdatas.size()]));
				
				withAlonMap.put(pk_org, withAlondIdPubDatas);//����֯��Ӧ�����ֱ��meapubdata������С�ڵ���4
			}
			
			ICommitQueryService commitQrySrv = NCLocator.getInstance().lookup(ICommitQueryService.class);
			Map<String, UFBoolean> isLeafOrgMaps = getOrgIsLeafMemberMaps();
			for(String pk_report: repids){
				Map<String, MeasurePubDataVO> meaPubData = new HashMap<String, MeasurePubDataVO>();

                StringBuilder errorMsg = new StringBuilder();
                StringBuilder noUnionOrg = new StringBuilder();
                StringBuilder noCommitOrg = new StringBuilder();
                for(Iterator<String> iters = withAlonMap.keySet().iterator();iters.hasNext();){
					String pk_org  = iters.next();
					String orgname = OrgUtil.getOrgName(pk_org);
					MeasurePubDataVO seppubdata = null;
					MeasurePubDataVO hbpubdata = null;
					MeasurePubDataVO sepadjPubdata = null;
					MeasurePubDataVO hbadjPubdata = null;
					
					MeasurePubDataVO[] withAloneid = withAlonMap.get(pk_org);
					
					for(MeasurePubDataVO pubdataVO : withAloneid){
						if(pubdataVO != null){
							if(pubdataVO.getVer() == sepadjversion)
								sepadjPubdata = pubdataVO;
							else if(pubdataVO.getVer() == hbadjversion)
								hbadjPubdata = pubdataVO;
							else if(pubdataVO.getVer() == 0)
								seppubdata = pubdataVO;
							else if(pubdataVO.getVer() == hbversion)
								hbpubdata = pubdataVO;
						}
					}
					
					MeasurePubDataVO destPubData = seppubdata;
					
					boolean singleRep = false;//�Ƿ�ȡ���𱨱�����
					//Ҷ�ӽڵ��ǰ�ϲ��ڵ�
					if(isLeafOrgMaps.get(pk_org).booleanValue() == true || pk_unionOrg.equals(pk_org)){
						singleRep = true;
						//�жϸ���������Ƿ����
						if(sepadjPubdata != null && commitQrySrv.isRepInput(sepadjPubdata.getAloneID(), pk_report))
							destPubData = sepadjPubdata;
					}
					//��Ҷ�ӽڵ㲢�Ҳ��ǵ�ǰ�ϲ��ڵ�
					else{
						//ȡ�ϲ������ϲ��������Ƿ����
						if(hbpubdata == null || !commitQrySrv.isRepInput(hbpubdata.getAloneID(), pk_report)){
							noUnionOrg.append(orgname).append(";");
						}
							
						destPubData = hbpubdata;
						if(hbadjPubdata != null && commitQrySrv.isRepInput(hbadjPubdata.getAloneID(), pk_report))
							destPubData = hbadjPubdata;
					}
					
				
					if (destPubData != null) {
                        // ��������δ�ϱ��Ƿ�����ϲ�-������
                        String ispermithb = unionqryVO.getSchemeVo().getIspermithb();
                    	//������鵥λ�����𱨱��Ƿ��ϱ�����ҪУ��--modified by jiaah
                        if (ispermithb != null && HBSchemeConstants.ISPERMITHB_NOT.equals(ispermithb) && 
                        		!(singleRep && oppEntityOrgs != null && oppEntityOrgs.get(pk_org) != null && oppEntityOrgs.get(pk_org).booleanValue())) {
                            boolean isCommit = false;
                            // ��������״̬��ѯ
                            ICommitQueryService commitQueryService = NCLocator.getInstance().lookup(ICommitQueryService.class);
                            RepDataCommitVO[] repDataCommitVOs = commitQueryService.getReportCommitState(new String[] { destPubData.getAloneID() }, pk_report);
                            if (repDataCommitVOs != null && repDataCommitVOs.length > 0) {
                                Integer commitState = repDataCommitVOs[0].getCommit_state();
                                // ��������״̬Ϊ���ϱ�����ȷ��
                                if (commitState != null
                                        && (CommitStateEnum.COMMITED == commitState || CommitStateEnum.AFFIRMED == commitState)) {
                                    isCommit = true;
                                }
                            }
                            if (!isCommit) {
                                if (destPubData.getVer() == 0) {
                                	noCommitOrg.append(orgname).append(":").append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830003-0106"))/*@res "���𱨱�"*/.append(";");
                                } else if (destPubData.getVer() == hbversion) {
                                	noCommitOrg.append(orgname).append(":").append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830003-0107"))/*@res "�ϲ�����"*/.append(";");
                                } else if (destPubData.getVer() == sepadjversion) {
                                	noCommitOrg.append(orgname).append(":").append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830003-0108"))/*@res "���𱨱������"*/.append(";");
                                } else if (destPubData.getVer() == hbadjversion) {
                                	noCommitOrg.append(orgname).append(":").append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830003-0109"))/*@res "�ϲ����������"*/.append(";");
                                }
                            }
                        }
                        meaPubData.put(pk_org, destPubData);
					}
				}
                errorMsg = errorMsg.append(noCommitOrg).append(noUnionOrg);
				if(errorMsg.length() != 0){
					StringBuffer buff = new StringBuffer();
					if(noUnionOrg.length() != 0)
						buff.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830008-0137")).append(noUnionOrg);//"δ���ɺϲ��������֯��"
					if(noCommitOrg.length() != 0)
						buff.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830003-0110")).append(noCommitOrg.toString());//"δ�ϱ��ı���"
					
//					if(buff.toString().length() > 60)
//						throw new UFOCUnThrowableException(buff.toString().substring(0, 60) + "...");
//					else
//						throw new UFOCUnThrowableException(buff.toString());
					//�¼���λδ�ϲ� У���쳣������--�й�����--add by mizhl--20190225
					if(!buff.toString().startsWith("δ���ɺϲ��������֯")){
						if(buff.toString().length() > 60){
							throw new UFOCUnThrowableException(buff.toString().substring(0, 60) + "...");
						}
						else{
							throw new UFOCUnThrowableException(buff.toString());
						}
					}

				}
				allmap.put(pk_report, meaPubData);
			}
			pubDataVOs = allmap;
		}
		return pubDataVOs;
	}
	
	
	public Map<String,CellsModel>  getPreCellModle() {
		if(preFormatModel == null){
			preFormatModel = new HashMap<String, CellsModel>();
			UfoContextVO contextVO = new UfoContextVO();
			contextVO.setAttribute(IUfoContextKey.KEYGROUP_PK, unionqryVO.getSchemeVo().getPk_keygroup());
			for (int i = 0; i < repids.length; i++){
				String pk_report = repids[i];
			    contextVO.setAttribute(ReportContextKey.REPORT_PK,pk_report );
				CellsModel formatmodel = CellsModelOperator.getFormatModelByPK(contextVO);
				preFormatModel.put(pk_report, formatmodel);
			}
		}
		return preFormatModel;
	}
	
	
}
