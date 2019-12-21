package nc.bs.hbbb.union;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.hbbb.project.ProjectSynchronize;
import nc.bs.logging.Logger;
import nc.bs.pub.SystemException;
import nc.impl.hbbb.vouchdata.VouchDataToDraftReportUtilBO;
import nc.itf.iufo.commit.ICommitManageService;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.pub.hbbb.exception.UFOCUnThrowableException;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.MeasureCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasureDataBO_Client;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.HBBBRepUtil;
import nc.util.hbbb.HBBBReportUtil;
import nc.util.hbbb.HBKeyGroupUtil;
import nc.util.hbbb.HBVersionUtil;
import nc.util.hbbb.MeasureDataProxy;
import nc.util.hbbb.MeasurePubDataUtil;
import nc.util.hbbb.UFOCSqlUtil;
import nc.util.iufo.pub.AuditUtil;
import nc.util.ufoc.unionproject.ProjectSrvUtils;
import nc.vo.hbbb.dxtype.DXTypeValue;
import nc.vo.hbbb.func.HBBBFuncQryVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.union.UnionReportConPreLoadVO;
import nc.vo.hbbb.union.UnionReportQryVO;
import nc.vo.hbbb.vouch.IVouchDirections;
import nc.vo.hbbb.vouchdata.VouchDadaToDraftRepQryVO;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.iufo.verctrl.IDataVersionConsts;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.unionproject.ProjectVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.table.CellsModel;

/**
 * modified by Jiaah at 2013-11-18 ����ˮ�ಹ��ͬ��
 * @author jiaah
 *
 */
public class SingleReportUnionBO {

	private HBSchemeVO hbschemevo;

	private UnionReportQryVO qryvo;
	
	private static ICommitManageService service;

//	private Map<String, ReportCombineStruMemberVersionVO> reportMember_map = new HashMap<String, ReportCombineStruMemberVersionVO>();

	private synchronized static ICommitManageService getService() {
		if(service == null){
			service = NCLocator.getInstance().lookup(ICommitManageService.class);
		}
		return service;
	}

	public SingleReportUnionBO(UnionReportQryVO new_qryvo) {
		super();
		this.qryvo = new_qryvo;
		this.hbschemevo = new_qryvo.getSchemeVo();
//		initReportMemberVOs();
	}

//	private void initReportMemberVOs() {
//		try {
//			ReportCombineStruMemberVersionVO[] vos = HBBaseDocItfService.getRemoteHBRepStru().queryReportCombineStruMemberVersionByVersionId(this.getQryvo().getPk_hbrepstru());
//			for (int i = 0; i < vos.length; i++) {
//				ReportCombineStruMemberVersionVO vo = vos[i];
//				reportMember_map.put(vo.getPk_org(), vo);
//			}
//		} catch (BusinessException e) {
//			nc.bs.logging.Logger.error(e.getMessage(), e);
//		}
//	}

	private Map<String,List<MeasureDataVO>> getDynMeasureDatas(MeasurePubDataVO[] subPubDatas,MeasureVO[] dynmeasureVOs) throws BusinessException {

		Map<String,List<MeasureDataVO>> mapDataVO = new HashMap<String, List<MeasureDataVO>>();
		try {
			if (null == subPubDatas || subPubDatas.length == 0) {
				return null;
			}
			Set<String> aloneidset = new HashSet<String>();
			for (MeasurePubDataVO vo : subPubDatas) {
				aloneidset.add(vo.getAloneID());
				//�ɱ���ָ������
//				RepDataVO[] repData = RepDataBO_Client.loadRepData(pk_report, null, vo, null);
			}
			MeasureDataVO[] datavos = MeasureDataProxy.getRepData(aloneidset.toArray(new String[0]), dynmeasureVOs);
			for(MeasureDataVO vo : datavos){
				List<MeasureDataVO> lst = mapDataVO.get(vo.getAloneID());
				if(lst == null){
					lst = new ArrayList<MeasureDataVO>();
					lst.add(vo);
					mapDataVO.put(vo.getAloneID(), lst);
				}else{
					lst.add(vo);
				}
			}
		} catch (Exception e) {
			throw new BusinessException(e);
		}
		return mapDataVO;
	}

	private MeasureDataVO[] getMeasureDatas(MeasureVO[] fixmeasureVOs, List<MeasurePubDataVO> currPubDatalist) throws BusinessException {
		MeasureDataVO[] datavos = null;
		try {
			if (null == currPubDatalist || currPubDatalist.size() == 0) {
				return null;
			}
			MeasurePubDataVO[] vos = currPubDatalist.toArray(new MeasurePubDataVO[0]);
			Set<String> aloneidset = new HashSet<String>();
			for (MeasurePubDataVO vo : vos) {
				aloneidset.add(vo.getAloneID());
			}
			datavos = MeasureDataProxy.getRepData(aloneidset.toArray(new String[0]), fixmeasureVOs);
		} catch (Exception e) {
			throw new BusinessException(e);
		}
		return datavos;
	}

	private boolean isVoidOrg(MeasurePubDataVO Src_pubdata, String pk_org, Map<String, UFBoolean> map) throws BusinessException {
		boolean result = false;
		if (Src_pubdata.getVer() == 0) {
			if (null != map.get(pk_org) && map.get(pk_org).booleanValue() == true) {
				return true;
			}
		}
		return result;
	}
	/**
	 * �������źϲ�
	 * �鵥λ֧��,��ǰ�ķ��������鵥λ����ϲ�����һ��Ҫ��֤�鵥λ�ĸ��𱨱�����Ϊ��
	 * @param pk_report
	 * @param firstLevOrgs
	 * @param pk_hbrepstru
	 * @param preDataVO
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws NamingException
	 * @throws SQLException
	 */
	public void doUnionByReport(String pk_report, String[] firstLevOrgs,String pk_hbrepstru,UnionReportConPreLoadVO preDataVO,HBUnionVouchDataCond vouchDataCond,Map<String, Map<String, UFDouble>> dxvaluemap,Map<String, Map<String, UFDouble>> self_dxvaluemap,String pk_dynreppk) throws BusinessException, SystemException, NamingException, SQLException {
		try {

			//1�����ظ��ӱ���Ĺؼ�����Ϣ
			List<MeasurePubDataVO> currPubDatalist = new ArrayList<MeasurePubDataVO>();
			//����֯��Ӧ��aloneid
			Set<String> voidOrgAloneidSet = new HashSet<String>();
			//������֯����ʵ��λ��Ӧ--�ȼ�����ʵ��֯�����ټ���pubdata�� modifiey by jiaah
			Map<String, UFBoolean> oppEntityOrgs = preDataVO.getOrgEntityMaps();

			//ִ�кϲ�ʱ��֯ѡ�õı���İ汾--pk_report-org->MeasurePubDataVO(����aloneid)
			Map<String, Map<String, MeasurePubDataVO>> pubDataWithAloneIDVOs = preDataVO.getPubDataVosMaps();
			Map<String, MeasurePubDataVO> orgPubdata = 	pubDataWithAloneIDVOs.get(pk_report);
			Set<String> pk_entitiys = new HashSet<String>();
			for (String pk_orgwithinnercode : firstLevOrgs) {
				String pk_org = pk_orgwithinnercode.substring(0, 20);
				//��ǰ��֯��Ҫȡ�ı���汾
				MeasurePubDataVO destPubdata = orgPubdata.get(pk_org);
				if (destPubdata != null) {
				    if (this.isVoidOrg(destPubdata, pk_org, oppEntityOrgs)) {
				    	pk_entitiys.add(pk_org);
	                    voidOrgAloneidSet.add(destPubdata.getAloneID());
	                }
	                currPubDatalist.add(destPubdata);
				}
			}
			//����ӱ��pubdataΪnull���򷵻�
			if (currPubDatalist.size() == 0) {
				throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0400")/*@res "�����¼����𱨱�������"*/);
			}

			CellsModel formatModel = preDataVO.getPreCellModle().get(pk_report);
			
			if(HBBBReportUtil.isIntrateRep(pk_report)){
				//�Ƿ��Ƕද̬���ؼ��֣����򲻲���ϲ�
				boolean flag = false;
				//����ؼ���
				String mainpk = vouchDataCond.getPubdata().getKeyGroup().getKeyGroupPK();
				String[] allKeyGroupPks = IUFOCacheManager.getSingleton().getReportCache().getKeyCombs(pk_report);
				String[] dynKeyGroupPks = new String[allKeyGroupPks.length-1];
				int j=0;
				for (int i = 0; i < allKeyGroupPks.length; i++) {
					if (!allKeyGroupPks[i].equals(mainpk)) {
						dynKeyGroupPks[j] = allKeyGroupPks[i];
						j++;
					}
				}
				KeyGroupVO[] dynKeyGroupVOs = UFOCacheManager.getSingleton().getKeyGroupCache().getByPKs(dynKeyGroupPks);
				for (int i = 0; i < dynKeyGroupVOs.length; i++) {
					String[] pks = HBKeyGroupUtil.getPk_dynKeyValues(dynKeyGroupVOs[i], mainpk);
					if (pks!=null && pks.length>1) {
						flag = true;
						break;
					}
				}
				
				if(!flag){
					doDynRepUnion(pk_report, vouchDataCond, currPubDatalist,voidOrgAloneidSet,dxvaluemap,self_dxvaluemap,pk_entitiys,pk_dynreppk);
				}
			}
			else
				doFixRepUnion(pk_report, vouchDataCond, currPubDatalist,voidOrgAloneidSet,formatModel);

			//����iufo_rep_commit��
			String strCurUserPK = AuditUtil.getCurrentUser();
			ICommitManageService commitSrv = getService();
			if(vouchDataCond.getPubdata() != null)
				commitSrv.addRepInputSate(getHbschemevo().getPk_hbscheme(), vouchDataCond.getPubdata().getAloneID(), pk_report, strCurUserPK, true, null);
		} catch (Exception e) {
			if(e instanceof UFOCUnThrowableException)
				throw new UFOCUnThrowableException(e.getMessage());
			else
				throw new BusinessException(e.getMessage());
		}
	}

	/**
	 * ��̬��ϲ�
	 * @param pk_report
	 * @param vouchDataCond
	 * @param currPubDatalist
	 * @param voidOrgAloneidSet
	 * @throws BusinessException
	 * @throws Exception
	 * @throws SystemException
	 * @throws NamingException
	 * @throws SQLException
	 */
	private void doDynRepUnion(String pk_report,HBUnionVouchDataCond vouchDataCond,
			List<MeasurePubDataVO> currPubDatalist,
			Set<String> voidOrgAloneidSet,Map<String, Map<String, UFDouble>> dxvaluemap,Map<String, Map<String, UFDouble>> self_dxvaluemap,Set<String> lstEntityOrgs,String pk_dynreppk)
			throws BusinessException, Exception, SystemException,
			NamingException, SQLException {

		//�ϲ��汾����pubdata
		MeasurePubDataVO mainPubData = vouchDataCond.getPubdata();
		//����ָ��
		MeasureVO[] measureVOs = IUFOCacheManager.getSingleton().getMeasureCache().
				loadMeasureByReportPKs(new String[] {pk_report});
		//�̶���ָ��
		Set<MeasureVO> fixMeasures = new HashSet<MeasureVO>();
		//��̬��ָ��
		Set<MeasureVO> dynMeasures = new HashSet<MeasureVO>();
		//��̬��ָ��map
		Map<String, MeasureVO> dynMapMeasureMap = new HashMap<String, MeasureVO>();
		for(MeasureVO vo : measureVOs){
			String mainKeyGroup = mainPubData.getKType();
			if(vo.getKeyCombPK().equals(mainKeyGroup)){
				// @edit by zhoushuang at 2015-5-13,����2:35:21  �̶����ų��ַ���ָ��
				if (null != vo && (vo.getType() == IStoreCell.TYPE_NUMBER
						|| vo.getType() == IStoreCell.TYPE_BIGDECIMAL)){
					fixMeasures.add(vo);
				}
			}
			else{
				//modified by jiaah at 2014-2-18 ��̬���ų��ַ���ָ��
				if (null != vo && (vo.getType() == IStoreCell.TYPE_NUMBER
						|| vo.getType() == IStoreCell.TYPE_BIGDECIMAL)){
					dynMeasures.add(vo);
					dynMapMeasureMap.put(vo.getCode(), vo);
				}
			}
		}

		//���еĹؼ������
		String[] allKeyGroupPks = IUFOCacheManager.getSingleton().getReportCache().getKeyCombs(pk_report);
		if(allKeyGroupPks != null && allKeyGroupPks.length > 2){
			AppDebug.error("��̬�����ڶ���ؼ���,�ݲ�֧�ִ��ֺϲ�");/*-=notranslate=-*/
			return;
		}
		//���ӱ���ͬ�ؼ��֣����ڸ��Թؼ�������е�λ�ÿ��ܲ���ͬ������������Ķ�Ӧ��ϵ
		Hashtable<Integer, Integer> hashPos = new Hashtable<Integer, Integer>();
		KeyGroupVO mainKeyGroupVO = mainPubData.getKeyGroup();
		KeyVO[] mainKeys = mainKeyGroupVO.getKeys();
		
		String pk_subgroup = allKeyGroupPks[1];
		KeyGroupVO subGropVO = (KeyGroupVO)IUFOCacheManager.getSingleton().getKeyGroupCache().get(pk_subgroup);
		KeyVO[] subKeys = subGropVO.getKeys();
		//��̬���ؼ���pk Ŀǰ֧��һ����̬���ؼ���
		String pk_dynkeyword = "";
		for (int i = 0; i < subKeys.length; i++) {
			boolean has = false;
			for (int j = 0; j < mainKeys.length; j++) {
				if (mainKeys[j].equals(subKeys[i])) {
					hashPos.put(Integer.valueOf(j), Integer.valueOf(i));
					has = true;
					break;
				}
			}
			if(!has)
				pk_dynkeyword = subKeys[i].getPk_keyword();
		}

		//TODO:100����֯���ѯ100�Σ�ȡ�����в���ϲ�����֯�Ķ�̬���ؼ���MeasurePubDataVO
		List<MeasurePubDataVO> allDynMeasurePubDataLst = new ArrayList<MeasurePubDataVO>();
		for(MeasurePubDataVO vo : currPubDatalist){
			StringBuilder whereSQL = new StringBuilder();
			for (int j = 0; j < mainKeys.length; j++) {
	            String value = vo.getKeywordByPK(mainKeyGroupVO.getKeys()[j].getPk_keyword());
	            if (value != null){
	            	whereSQL.append(" keyword");
	                whereSQL.append(hashPos.get(j)+1);
	                whereSQL.append("='");
	                whereSQL.append(value);
	                whereSQL.append("'");
	            }
	            whereSQL.append(" and  ");
	         }
			if(vo.getVer() == 0 || (vo.getVer()+"").startsWith(IDataVersionConsts.VERTYPE_SEPERATE_ADJUST+""))//���𱨱����𱨱������ȡ���𱨱�汾
				whereSQL.append(" ver=0 ");
			else if((vo.getVer()+"").startsWith(IDataVersionConsts.VERTYPE_HBBB+"")||
					(vo.getVer()+"").startsWith(IDataVersionConsts.VERTYPE_HBBB_ADJUST+"")){//�ϲ������ϲ����������ȡ�ϲ�����汾
				whereSQL.append(" ver= " );
				whereSQL.append(mainPubData.getVer());
			}

			MeasurePubDataVO[] allDynPubDatas = MeasurePubDataBO_Client.findBySqlCondition(pk_subgroup, whereSQL.toString());
			allDynMeasurePubDataLst.addAll(Arrays.asList(allDynPubDatas));
		}
		
		
		//1�����������鵥λ�Ķ�̬����𱨱�,��Ϊ�鵥λ�ĸ��𱨱������ǲ��ܲ���ϲ���
		if (lstEntityOrgs.size() > 0) {
			List<MeasurePubDataVO> lstEntityPubdatavo = new ArrayList<MeasurePubDataVO>();
			for(MeasurePubDataVO vo : allDynMeasurePubDataLst){
				String pk_org = vo.getUnitPK();
				if(lstEntityOrgs.contains(pk_org))
					lstEntityPubdatavo.add(vo);
			}
			if(lstEntityPubdatavo.size() > 0)
				allDynMeasurePubDataLst.removeAll(lstEntityPubdatavo);
		}

		//���ж�̬���ؼ��ֵ�pks
		Set<String> otherWords = new HashSet<String>();
		for(MeasurePubDataVO vo : allDynMeasurePubDataLst){
			otherWords.add(vo.getKeywordByIndex(vo.getKeyByPK(pk_dynkeyword)+1));
		}

		//ֻ֧��һ����̬���ؼ��֣�map<�Է���λ����pk,onePubData>
		Map<String, MeasurePubDataVO> mapReusltUnionPubData = new HashMap<String, MeasurePubDataVO>();
		Map<String, MeasurePubDataVO> mapReusltTotalPubData = new HashMap<String, MeasurePubDataVO>();
		Map<String, MeasurePubDataVO> mapReusltContrastPubData = new HashMap<String, MeasurePubDataVO>();
		//���춯̬���Ĳ�ͬ�汾�Ĺؼ���
		MeasurePubDataVO dynPubData = new MeasurePubDataVO();
		dynPubData.setKeyGroup(subGropVO);
		dynPubData.setAccSchemePK(mainPubData.getAccSchemePK());//
		dynPubData.setKType(subGropVO.getKeyGroupPK());
		dynPubData.setVer(mainPubData.getVer());
		for (int i = 0; i < mainKeys.length; i++) {
			int iMainPos = 0;
			if (hashPos.get(Integer.valueOf(i)) != null){
				iMainPos = hashPos.get(Integer.valueOf(i)).intValue();
				dynPubData.setKeywordByIndex(iMainPos+1, mainPubData.getKeywordByIndex(i+1));
			}
		}
		

		//�ϲ�������汾
		int dxVersion = HBVersionUtil.getHBContrastByHBSchemeVO(vouchDataCond.getHBSchemeVO());
		int totalver = HBVersionUtil.getHBTotalyHBSchemeVO(getHbschemevo());
		//һ�λ�ȡ��̬���ĺϲ��ϼƺ͵����汾
		Map<String, Map<Integer,MeasurePubDataVO>> allRelaDynPubdatavoMap = getcachedynPubdatavos(mainPubData, subGropVO, pk_dynkeyword,otherWords, dynPubData);
		
		for(String s:otherWords) {
			Map<Integer,MeasurePubDataVO> vermap = allRelaDynPubdatavoMap.get(s);
			MeasurePubDataVO unionPubData = null;
			MeasurePubDataVO totalPubData = null;
			MeasurePubDataVO contrastPubDatavo = null;
			if(vermap != null){
				unionPubData = vermap.get(getHbschemevo().getVersion());
				if(unionPubData == null){
					unionPubData = getPubDataByVer(pk_dynkeyword, dynPubData,getHbschemevo().getVersion(), s);
				}
				totalPubData = vermap.get(totalver);
				if(totalPubData == null){
					totalPubData = getPubDataByVer(pk_dynkeyword, dynPubData,totalver, s);
				}
				contrastPubDatavo = vermap.get(dxVersion);
				if(contrastPubDatavo == null){
					contrastPubDatavo = getPubDataByVer(pk_dynkeyword,dynPubData, dxVersion, s);
				}
			}else{
				unionPubData = getPubDataByVer(pk_dynkeyword, dynPubData,getHbschemevo().getVersion(), s);
				totalPubData = getPubDataByVer(pk_dynkeyword, dynPubData,totalver, s);
				contrastPubDatavo = getPubDataByVer(pk_dynkeyword,dynPubData, dxVersion, s);
			}

			mapReusltUnionPubData.put(s, unionPubData);//�ϲ��汾
			mapReusltTotalPubData.put(s, totalPubData);//�ϼư汾
			mapReusltContrastPubData.put(s, contrastPubDatavo);//�ϲ������汾
		}
		
		//TODO:��61����63��ʱ���ǲ����ڶ�̬��ĺϲ������ݵģ�����ɾ���п��ܻᱨ����
		if(pk_dynreppk != null && pk_dynreppk.equals(pk_report)){//������еĶ�̬���ϲ�����(ֻɾ��һ�βſ���)
			MeasureDataBO_Client.deleteAllRepData(mapReusltUnionPubData.values().toArray(new MeasurePubDataVO[0]));
			MeasureDataBO_Client.deleteAllRepData(mapReusltTotalPubData.values().toArray(new MeasurePubDataVO[0]));
			MeasureDataBO_Client.deleteAllRepData(mapReusltContrastPubData.values().toArray(new MeasurePubDataVO[0]));
		}
		
		
		
		
		//��ѯ�����ж�̬��������  map<aloneid,ָ������list>
		Map<String,List<MeasureDataVO>> dynMapRepdatavos = getDynMeasureDatas(allDynMeasurePubDataLst.toArray(
				new MeasurePubDataVO[allDynMeasurePubDataLst.size()]),
				dynMeasures.toArray(new MeasureVO[dynMeasures.size()]));


		//ֻ�д��ڶ�̬�����ݲ�ִ����͵�������
		if(dynMapRepdatavos != null){
			//���ݶԷ���λ���������ͣ��õ�һ��map��map<�Է���λ����,map<meascolumn + dbtable,double>>
			Map<String, Map<String, UFDouble>> resultMap = getMapofTotalValue(dynMeasures, subGropVO, pk_dynkeyword,dynMapRepdatavos);

			//��ѯ���������ݣ��Է��Ķ������ݣ�
			//��������map<�Է���λ����,map<mesurcode,double>>��Դ���𱨱��Ӧ�ĶԷ���λ���룬ָ��code��ֵ
			Map<String, Map<String, UFDouble>> dxvalueMap = dxvaluemap;


			//���ɺϼ����ݡ����������ݡ��ϲ�����
			MeasureVO[] dynMeasureVos = new MeasureVO[dynMeasures.size()];
			dynMeasures.toArray(dynMeasureVos);
			for(Map.Entry<String,Map<String, UFDouble>> enter : resultMap.entrySet()){
				String pk_oppcode = enter.getKey();//�Է���λ����
				Map<String, UFDouble> mapValues = (Map<String, UFDouble>) enter.getValue();
				//��ָ̬�����ݼ���,�������ɺϼƱ�����-��������
				MeasureDataVO[] dataTotalvos = new MeasureDataVO[dynMapMeasureMap.size()];
				//��ָ̬�����ݼ���,�������ɺϲ�����=�ϼƱ�����-��������
				MeasureDataVO[] dataUnionvos = new MeasureDataVO[dynMapMeasureMap.size()];
				//��ָ̬�����ݼ���,�������ɵ���������
//				MeasureDataVO[] dataContrastvos = new MeasureDataVO[dynMapMeasureMap.size()];
				String unionaloneid = mapReusltUnionPubData.get(pk_oppcode).getAloneID();
				String totalaloneid = mapReusltTotalPubData.get(pk_oppcode).getAloneID();
//				String contrastaloneid = mapReusltContrastPubData.get(pk_oppcode).getAloneID();
				for (int i = 0 ; i < dynMeasureVos.length ; i++) {
					MeasureDataVO uniondvo = new MeasureDataVO();
					MeasureVO meavo = dynMapMeasureMap.get(dynMeasureVos[i].getCode());
					uniondvo.setMeasureVO(meavo);
					MeasureDataVO totaldvo = (MeasureDataVO) uniondvo.clone();
//					MeasureDataVO contratdvo = (MeasureDataVO) uniondvo.clone();
					String dbcolumn = meavo.getDbcolumn();
					String dbtable = meavo.getDbtable();
					if(dxvalueMap.get(pk_oppcode) != null && dxvalueMap.get(pk_oppcode).get(meavo.getCode()) != null){
						UFDouble total = mapValues.get(dbcolumn + dbtable);
						UFDouble dx = dxvalueMap.get(pk_oppcode).get(meavo.getCode());
						UFDouble union = total.sub(dx);
						uniondvo.setDataValue(String.valueOf(union));
//						contratdvo.setDataValue(String.valueOf(UFDouble.ZERO_DBL.sub(dx)));
					}else{
						uniondvo.setDataValue(mapValues.get(dbcolumn + dbtable).toString());
//						contratdvo.setDataValue(UFDouble.ZERO_DBL.toString());
					}
					uniondvo.setAloneID(unionaloneid);
					dataUnionvos[i] = uniondvo;

					totaldvo.setDataValue(mapValues.get(dbcolumn + dbtable).toString());
					totaldvo.setAloneID(totalaloneid);
					dataTotalvos[i] = totaldvo;

//					contratdvo.setAloneID(contrastaloneid);
//					dataContrastvos[i] = contratdvo;

				}
				MeasureDataBO_Client.editRepData(unionaloneid, dataUnionvos);//��̬���ϲ��汾
				MeasureDataBO_Client.editRepData(totalaloneid, dataTotalvos);//��̬���ϼư汾
//				MeasureDataBO_Client.editRepData(contrastaloneid, dataContrastvos);//��̬�������汾����
			}
			
			
			//�㱨��ý��Ҫ�������
			for(Map.Entry<String,Map<String, UFDouble>> enter : self_dxvaluemap.entrySet()){
				String pk_selforg = enter.getKey();//������λ��Ϊ�Է���λ����
				if(pk_selforg == null)
					continue;
				//��ָ̬�����ݼ���,�������ɵ���������
				MeasureDataVO[] dataContrastvos = new MeasureDataVO[dynMapMeasureMap.size()];
				if (mapReusltContrastPubData.get(pk_selforg)==null) {
					continue;
				}
				String contrastaloneid = mapReusltContrastPubData.get(pk_selforg).getAloneID();
				//TODO:�����aloneid����������
				if(contrastaloneid == null)
					continue;
				
				for (int i = 0 ; i < dynMeasureVos.length ; i++) {
					MeasureDataVO contratdvo = new MeasureDataVO();
					MeasureVO meavo = dynMapMeasureMap.get(dynMeasureVos[i].getCode());
					contratdvo.setMeasureVO(meavo);
					
					if(self_dxvaluemap.get(pk_selforg) != null && self_dxvaluemap.get(pk_selforg).get(meavo.getCode()) != null){
						UFDouble dx = self_dxvaluemap.get(pk_selforg).get(meavo.getCode());
						contratdvo.setDataValue(String.valueOf(dx));
					}else{
						contratdvo.setDataValue(UFDouble.ZERO_DBL.toString());
					}

					contratdvo.setAloneID(contrastaloneid);
					dataContrastvos[i] = contratdvo;

				}
				MeasureDataBO_Client.editRepData(contrastaloneid, dataContrastvos);//��̬�������汾����
			}
		}

		
		//1�����������鵥λ�ĸ��𱨱�,��Ϊ�鵥λ�ĸ��𱨱������ǲ��ܲ���ϲ���
		//�̶����ָ������getMeasureDatas
		MeasureDataVO[] repdatavos = getMeasureDatas(fixMeasures.toArray(new MeasureVO[fixMeasures.size()]),currPubDatalist);
		if (voidOrgAloneidSet.size() > 0) {
			for (MeasureDataVO tvo : repdatavos) {
				if (voidOrgAloneidSet.contains(tvo.getAloneID())) {
					tvo.setDataValue(null);
				}
			}
		}

		//ָ��code-�ϼ�ֵ
		ConcurrentHashMap<String, UFDouble> map = new ConcurrentHashMap<String, UFDouble>();
		//ָ��code-ָ��vo
		ConcurrentHashMap<String, IStoreCell> measuremap = new ConcurrentHashMap<String, IStoreCell>();
		//3�����ӱ����ݼ��ۼ���ͣ�����ָ�����ۼӣ�������ָ��������ɾ�����еĺϲ��������
		if (null != repdatavos && repdatavos.length > 0) {
			for (MeasureDataVO tvo : repdatavos) {
				// modified by litfb @20120514ֻ����ֵ�͸߾�����ֵ����ָ�����ϲ�
				if (null != tvo&& null != tvo.getMeasureVO()&& (tvo.getMeasureVO().getType() == IStoreCell.TYPE_NUMBER
						|| tvo.getMeasureVO().getType() == IStoreCell.TYPE_BIGDECIMAL)) {
					if (map.containsKey(tvo.getMeasureVO().getCode())) {
						UFDouble value = map.get(tvo.getMeasureVO().getCode());
						value = value.add(new UFDouble(tvo.getDataValue()));
						map.put(tvo.getMeasureVO().getCode(), value);
					} else {
						UFDouble value = new UFDouble(tvo.getDataValue());
						map.put(tvo.getMeasureVO().getCode(), value);
					}
					if (!measuremap.containsKey(tvo.getMeasureVO().getCode())) {
						measuremap.put(tvo.getMeasureVO().getCode(),tvo.getMeasureVO());
					}
				}
			}
		}else{
			//������ָ�꣺ɾ���ϲ����ݡ������衢��������
			MeasureDataBO_Client.deleteRepData(mainPubData.getAloneID(), measureVOs);
			MeasureDataBO_Client.deleteRepData(vouchDataCond.getDebitpubdata().getAloneID(), measureVOs);
			MeasureDataBO_Client.deleteRepData(vouchDataCond.getCreaditpubdata().getAloneID(), measureVOs);
		}

		//TODO:δ���ǹ̶������ڵ������ݵ����
		//3���������������ɺϼư汾����������汾��������汾���ϲ��汾
		if (map.size() > 0) {
			//���ϼ�����ָ�����ݼ�
			MeasureDataVO[] datavos = new MeasureDataVO[map.size()];
			//ָ�����
			String[] keys = new String[map.size()];
			map.keySet().toArray(keys);
			//�����ϼ�����ָ�����ݼ���,�������ɵ���������
			MeasureDataVO[] contrastdatavos = new MeasureDataVO[map.size()];
			for (int i = 0; i < keys.length; i++) {
				MeasureDataVO dvo = new MeasureDataVO();
				dvo.setMeasureVO(measuremap.get(keys[i]));
				dvo.setDataValue(String.valueOf(map.get(keys[i])));
				datavos[i] = dvo;
				MeasureDataVO dvo1 = new MeasureDataVO();
				dvo1.setMeasureVO(measuremap.get(keys[i]));
				contrastdatavos[i] = dvo1;
			}

			//�����洢�ϲ�����ĺϼ����汾,��Ϊ�ںϼ�����Ҫִ��MSELECT������ȥ��һ������ϲ�������ݣ�
			MeasurePubDataVO totalpubData = MeasurePubDataUtil.getMeasurePubdata(HBVersionUtil.
					getHBTotalyHBSchemeVO(getHbschemevo()), true,getQryvo().getUnionorg(), getHbschemevo().getPk_keygroup(),getQryvo().getKeymap(),getHbschemevo().getPk_accperiodscheme());

			// �ϲ���λ�ĺϼ�������
			String totalaloneid = totalpubData.getAloneID();
			for (MeasureDataVO dvo : datavos) {
				dvo.setAloneID(totalaloneid);
			}

			//����ϼ����汾��ͬʱ�ϼ���ִ��MESELECT����
			MeasureDataBO_Client.editRepData(totalpubData.getAloneID(), datavos);

			//�ϼ����У�ִ��MESELECT����
			HBBBFuncQryVO qryTotalvo  = new  HBBBFuncQryVO();
			qryTotalvo.setPubdata(totalpubData);
			qryTotalvo.setHbSchemeVo(getHbschemevo());
			qryTotalvo.setAryRepIDs(new String[]{pk_report});
			qryTotalvo.setIsconvert(false);
			qryTotalvo.setbAddLeft(Boolean.FALSE);
			qryTotalvo.setStrUserID("");
			qryTotalvo.setMeasures(measureVOs);
			qryTotalvo.setNeedreplaceAdd(false);
			HBBBRepUtil.calcHBBBTotalFormulasWithMselectFunc(qryTotalvo);
		}

		//�ϲ�������ִ�й�ʽ����
		HBBBFuncQryVO qryvo = new HBBBFuncQryVO();
		qryvo.setAryRepIDs(new String[] {pk_report});
		qryvo.setbAddLeft(Boolean.FALSE);
		qryvo.setHbSchemeVo(getHbschemevo());
		qryvo.setIsconvert(false);
		qryvo.setMeasures(measureVOs);
		qryvo.setPubdata(vouchDataCond.getContrastpubdata());
		qryvo.setStrUserID("");
		qryvo.setNeedreplaceAdd(false);
		HBBBRepUtil.calcHBConvertFormulasWithOutMSelectFunc(qryvo);

		//�ϲ����У�ִ������ʽ
		HBBBFuncQryVO qryUnionVO = new HBBBFuncQryVO();
		qryUnionVO.setPubdata(mainPubData);
		qryUnionVO.setHbSchemeVo(getHbschemevo());
		qryUnionVO.setAryRepIDs(new String[]{pk_report});
		qryUnionVO.setIsconvert(false);
		qryUnionVO.setbAddLeft(Boolean.FALSE);
		qryUnionVO.setStrUserID("");
		qryUnionVO.setMeasures(measureVOs);
		qryUnionVO.setNeedreplaceAdd(false);
		HBBBRepUtil.calcFormulasWithOutMSelectFuncWithOutTotalMeas(qryUnionVO);
	}

//	@SuppressWarnings("unchecked")
//	private Map<String, Map<String, UFDouble>> getMapofTotalValue(Set<MeasureVO> dynMeasures, KeyGroupVO subGropVO,
//			String pk_dynkeyword, Map<String, List<MeasureDataVO>> dynMapRepdatavos) throws BusinessException,DAOException {
//		//���ݶԷ���λ���������ͣ��õ�һ��map��map<�Է���λ����,map<meascolumn,double>>
//		Map<String,Map<String, UFDouble>> resultMap = new HashMap<String, Map<String,UFDouble>>();
//		
//		Set<String> aloneids = dynMapRepdatavos.keySet();
//		StringBuilder selectSql = new StringBuilder();
//		selectSql.append("select t2.keyword");
//		selectSql.append(subGropVO.getIndexByKeywordPK(pk_dynkeyword) + 1);
//		selectSql.append(",");
//
//		final MeasureVO[] vos = dynMeasures.toArray(new MeasureVO[dynMeasures.size()]);
//		for (int i = 0; i < vos.length; i++) {
//			IStoreCell measure = vos[i];
//			selectSql.append("sum(" + measure.getDbcolumn() + ")");
//			if (i < vos.length - 1) {
//				selectSql.append(",");
//			}
//		}
//		
//		selectSql.append(" from ");
//		selectSql.append(vos[0].getDbtable());
//		selectSql.append(" t1 ,");
//		selectSql.append(subGropVO.getTableName());
//		selectSql.append(" t2");
//		selectSql.append(" where t1.alone_id = t2.alone_id and  ");
//		//JIAAH ����sql
//		selectSql.append(UFOCSqlUtil.buildInSql("t2.alone_id", aloneids));
//		selectSql.append("group by t2.keyword");
//		selectSql.append(subGropVO.getIndexByKeywordPK(pk_dynkeyword) + 1);
//		BaseDAO dao = new BaseDAO();
//		
//		resultMap = (Map<String, Map<String, UFDouble>>) dao.executeQuery(selectSql.toString(),
//				new ResultSetProcessor() {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public Object handleResultSet(ResultSet rs) throws SQLException {
//				Map<String,Map<String, UFDouble>> map = new HashMap<String,Map<String, UFDouble>>();
//				while (rs != null && rs.next()){
//					Map<String, UFDouble> clouValues = new HashMap<String, UFDouble>();;
//					for (int i = 0; i < vos.length; i++){
//						clouValues.put(vos[i].getDbcolumn(), new UFDouble(rs.getDouble(i+2)));
//					}
//					map.put(rs.getString(1), clouValues);
//				}
//				return map;
//			}
//		});
//		return resultMap;
//	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Map<String, UFDouble>> getMapofTotalValue(Set<MeasureVO> dynMeasures, KeyGroupVO subGropVO,
			String pk_dynkeyword, Map<String, List<MeasureDataVO>> dynMapRepdatavos) throws BusinessException,DAOException {
		//���ݶԷ���λ���������ͣ��õ�һ��map��map<�Է���λ����,map<meascolumn+ָ�����,double>>
		Map<String,Map<String, UFDouble>> resultMap = new HashMap<String, Map<String,UFDouble>>();
		//dynMeasures���п��ܴ����ڲ�ͬ��ָ����У������Ȱ���ָ�����measurevo
		Map<String,List<MeasureVO>> mapOfMeasure = new HashMap<String, List<MeasureVO>>();
		for(MeasureVO vo:dynMeasures){
			String table = vo.getDbtable();
			List<MeasureVO> lst = mapOfMeasure.get(table);
			if(lst == null){
				lst = new ArrayList<MeasureVO>();
				lst.add(vo);
				mapOfMeasure.put(table, lst);
			}else
				lst.add(vo);
		}
		
		Set<String> aloneids = dynMapRepdatavos.keySet();
		
		for(Map.Entry<String, List<MeasureVO>> enter : mapOfMeasure.entrySet()){
			final String dbtable = enter.getKey();
			List<MeasureVO> lstMeas = enter.getValue();
			
			
			StringBuilder selectSql = new StringBuilder();
			selectSql.append("select t2.keyword");
			selectSql.append(subGropVO.getIndexByKeywordPK(pk_dynkeyword) + 1);
			selectSql.append(",");

			final MeasureVO[] vos = lstMeas.toArray(new MeasureVO[lstMeas.size()]);
			for (int i = 0; i < vos.length; i++) {
				IStoreCell measure = vos[i];
				selectSql.append("sum(" + measure.getDbcolumn() + ")");
				if (i < vos.length - 1) {
					selectSql.append(",");
				}
			}
			
			selectSql.append(" from ");
			selectSql.append(dbtable);
			selectSql.append(" t1 ,");
			selectSql.append(subGropVO.getTableName());
			selectSql.append(" t2");
			selectSql.append(" where t1.alone_id = t2.alone_id and  ");
			//JIAAH ����sql
			selectSql.append(UFOCSqlUtil.buildInSql("t2.alone_id", aloneids));
			selectSql.append("group by t2.keyword");
			selectSql.append(subGropVO.getIndexByKeywordPK(pk_dynkeyword) + 1);
			BaseDAO dao = new BaseDAO();
			
			Map<String, Map<String, UFDouble>> result = new HashMap<String, Map<String,UFDouble>>();
			result = (Map<String, Map<String, UFDouble>>) dao.executeQuery(selectSql.toString(),
					new ResultSetProcessor() {
				private static final long serialVersionUID = 1L;

				@Override
				public Object handleResultSet(ResultSet rs) throws SQLException {
					Map<String,Map<String, UFDouble>> map = new HashMap<String,Map<String, UFDouble>>();
					while (rs != null && rs.next()){
						Map<String, UFDouble> clouValues = new HashMap<String, UFDouble>();;
						for (int i = 0; i < vos.length; i++){
							clouValues.put(vos[i].getDbcolumn() + dbtable, new UFDouble(rs.getDouble(i+2)));
						}
						map.put(rs.getString(1), clouValues);
					}
					return map;
				}
			});
			
			if(result.size() == 0)
				continue;
			for(Map.Entry<String, Map<String, UFDouble>> enters: result.entrySet()){
				String key = enters.getKey();
				Map<String, UFDouble> value = enters.getValue();
				
				Map<String, UFDouble> newvalue = resultMap.get(key) ;
				if(newvalue == null){
					resultMap.put(key, value);
				}else{
					for(Map.Entry<String, UFDouble> s: value.entrySet()){
						newvalue.put(s.getKey(), s.getValue());
					}
				}
			}
		}
		return resultMap;
	}

	private MeasurePubDataVO getPubDataByVer(String pk_dynkeyword,MeasurePubDataVO dynPubData, int ver, String dynvalue)
			throws Exception {
		MeasurePubDataVO pubdata = (MeasurePubDataVO)dynPubData.clone();
		pubdata.setKeywordByPK(pk_dynkeyword, dynvalue);
		pubdata.setVer(ver);
		pubdata.setAloneID(null);
		String Aloneid = MeasurePubDataBO_Client.getAloneID(pubdata);
		pubdata.setAloneID(Aloneid);
		return pubdata;
	}

	/**
	 * һ�β�ѯ�����ݿ��еĺϲ��ϼƺ͵����Ķ�̬���汾
	 * @param mainPubData
	 * @param subGropVO
	 * @param pk_dynkeyword
	 * @param otherWords
	 * @param dynPubData
	 * @return
	 */
	private Map<String, Map<Integer,MeasurePubDataVO>>  getcachedynPubdatavos(MeasurePubDataVO mainPubData,
			KeyGroupVO subGropVO, String pk_dynkeyword,Set<String> otherWords, MeasurePubDataVO dynPubData) {
		Map<String, Map<Integer,MeasurePubDataVO>> allRelaDynPubdatavoMap = new HashMap<String, Map<Integer,MeasurePubDataVO>>();
		//�ϲ�������汾
		int dxVersion = HBVersionUtil.getHBContrastByHBSchemeVO(getHbschemevo());
		int totalver = HBVersionUtil.getHBTotalyHBSchemeVO(getHbschemevo());
		try {
			//��ѯ��̬�����MeasurePubDataVO��û�в�ѯ���ֹؼ��ֵĲ���ȷ���ڲ����̵�Ҳ������
			//������Ӧ�����϶�̬�������йؼ���
			//�ҳ���̬���Ĺؼ���
			KeyVO[] subKeys = subGropVO.getKeys();
			StringBuffer buf = new StringBuffer();
			for(int i = 0 ; i < subKeys.length ; i++){
				String keyword = subKeys[i].getPk_keyword();
				if(keyword.equals(pk_dynkeyword)){//��̬���ؼ��ֵ�ʱ��
					StringBuffer buf2 = new StringBuffer();
					buf2.append("keyword");
					buf2.append(subGropVO.getIndexByKeywordPK(pk_dynkeyword)+1);
					buf.append(UFOCSqlUtil.buildInSql(buf2.toString(), otherWords));
					buf.append(" and ");
				}else{
					buf.append("keyword");
					buf.append(subGropVO.getIndexByKeywordPK(keyword)+1);
					buf.append(" = '" + dynPubData.getKeywordByPK(keyword) + "' and ");
				}
			}
			buf.append(" ver in(");
			buf.append(mainPubData.getVer());
			buf.append(",");
			buf.append(totalver);
			buf.append(",");
			buf.append(dxVersion);
			buf.append(" )");
			MeasurePubDataVO[] findByKeywordArray = MeasurePubDataBO_Client.findBySqlCondition(subGropVO.getKeyGroupPK(), buf.toString());
			for(MeasurePubDataVO vo : findByKeywordArray){
				String otherpk = vo.getKeywordByPK(pk_dynkeyword);
				Map<Integer,MeasurePubDataVO> vermap = allRelaDynPubdatavoMap.get(otherpk);
				if(vermap == null){
					vermap = new HashMap<Integer, MeasurePubDataVO>();
				}
				vermap.put(vo.getVer(), vo);
				allRelaDynPubdatavoMap.put(otherpk, vermap);
			}
		} catch (Exception e) {
			AppDebug.debug(e);
		}
		return allRelaDynPubdatavoMap;
	}

	/**
	 * �̶���ϲ�
	 * @param pk_report
	 * @param vouchDataCond
	 * @param currPubDatalist
	 * @param voidOrgAloneidSet
	 * @throws BusinessException
	 * @throws Exception
	 * @throws SystemException
	 * @throws NamingException
	 * @throws SQLException
	 */
	private void doFixRepUnion(String pk_report,HBUnionVouchDataCond vouchDataCond,
			List<MeasurePubDataVO> currPubDatalist,
			Set<String> voidOrgAloneidSet,CellsModel formatModel)
			throws BusinessException, Exception, SystemException,
			NamingException, SQLException {

		//�ϲ��汾pubdata
		MeasurePubDataVO pubdata = vouchDataCond.getPubdata();

		MeasureVO[] measureVOs = IUFOCacheManager.getSingleton().getMeasureCache().
				loadMeasureByReportPKs(new String[] {pk_report});
		MeasureDataVO[] repdatavos = getMeasureDatas(measureVOs,currPubDatalist);

		//2�����������鵥λ�ĸ��𱨱�,��Ϊ�鵥λ�ĸ��𱨱������ǲ��ܲ���ϲ���
		if (voidOrgAloneidSet.size() > 0) {
			for (MeasureDataVO tvo : repdatavos) {
				if (voidOrgAloneidSet.contains(tvo.getAloneID())) {
					tvo.setDataValue(null);
				}
			}
		}

		//ָ��code-�ϼ�ֵ
		ConcurrentHashMap<String, UFDouble> map = new ConcurrentHashMap<String, UFDouble>();
		//ָ��code-ָ��vo
		ConcurrentHashMap<String, IStoreCell> measuremap = new ConcurrentHashMap<String, IStoreCell>();
		//3�����ӱ����ݼ��ۼ���ͣ�����ָ�����ۼӣ�������ָ��������ɾ�����еĺϲ��������
		if (null != repdatavos && repdatavos.length > 0) {
			for (MeasureDataVO tvo : repdatavos) {
				// modified by litfb @20120514ֻ����ֵ�͸߾�����ֵ����ָ�����ϲ�
				if (null != tvo&& null != tvo.getMeasureVO()&& (tvo.getMeasureVO().getType() == IStoreCell.TYPE_NUMBER
						|| tvo.getMeasureVO().getType() == IStoreCell.TYPE_BIGDECIMAL)) {
					if (map.containsKey(tvo.getMeasureVO().getCode())) {
						UFDouble value = map.get(tvo.getMeasureVO().getCode());
						value = value.add(new UFDouble(tvo.getDataValue()));
						map.put(tvo.getMeasureVO().getCode(), value);
					} else {
						UFDouble value = new UFDouble(tvo.getDataValue());
						map.put(tvo.getMeasureVO().getCode(), value);
					}
					if (!measuremap.containsKey(tvo.getMeasureVO().getCode())) {
						measuremap.put(tvo.getMeasureVO().getCode(),tvo.getMeasureVO());
					}
				}
			}
		}else{
			//������ָ�꣺ɾ���ϲ����ݡ������衢��������
			MeasureDataBO_Client.deleteRepData(pubdata.getAloneID(), measureVOs);
			MeasureDataBO_Client.deleteRepData(vouchDataCond.getDebitpubdata().getAloneID(), measureVOs);
			MeasureDataBO_Client.deleteRepData(vouchDataCond.getCreaditpubdata().getAloneID(), measureVOs);
		}

		//4���������������ɺϼư汾����������汾��������汾���ϲ��汾
		if (map.size() > 0) {
			//���ϼ�����ָ�����ݼ�
			MeasureDataVO[] datavos = new MeasureDataVO[map.size()];
			//ָ�����
			String[] keys = new String[map.size()];
			map.keySet().toArray(keys);
			//�����ϼ�����ָ�����ݼ���,�������ɵ���������
			MeasureDataVO[] contrastdatavos = new MeasureDataVO[map.size()];
			for (int i = 0; i < keys.length; i++) {
				MeasureDataVO dvo = new MeasureDataVO();
				dvo.setMeasureVO(measuremap.get(keys[i]));
				dvo.setDataValue(String.valueOf(map.get(keys[i])));
				datavos[i] = dvo;
				MeasureDataVO dvo1 = new MeasureDataVO();
				dvo1.setMeasureVO(measuremap.get(keys[i]));
				contrastdatavos[i] = dvo1;
			}

			//�����洢�ϲ�����ĺϼ����汾,��Ϊ�ںϼ�����Ҫִ��MSELECT������ȥ��һ������ϲ�������ݣ�
			MeasurePubDataVO totalpubData = MeasurePubDataUtil.getMeasurePubdata(HBVersionUtil.
					getHBTotalyHBSchemeVO(getHbschemevo()), true,getQryvo().getUnionorg(), getHbschemevo().getPk_keygroup(),getQryvo().getKeymap(),getHbschemevo().getPk_accperiodscheme());

			// �ϲ���λ�ĺϼ�������
			String totalaloneid = totalpubData.getAloneID();
			for (MeasureDataVO dvo : datavos) {
				dvo.setAloneID(totalaloneid);
			}

			//4.1: ����ϼ����汾��ͬʱ�ϼ���ִ��MESELECT����
			MeasureDataBO_Client.editRepData(totalpubData.getAloneID(), datavos);
			MeasureCache measureCache = UFOCacheManager.getSingleton().getMeasureCache();
			MeasureVO[] measuresWithData = measureCache.getMeasures(keys);
			HBBBFuncQryVO qryvo  =new  HBBBFuncQryVO();
			qryvo.setPubdata(totalpubData);
			qryvo.setHbSchemeVo(getHbschemevo());
			qryvo.setAryRepIDs(new String[]{pk_report});
			qryvo.setIsconvert(false);
			qryvo.setbAddLeft(Boolean.FALSE);
			qryvo.setStrUserID("");
			qryvo.setMeasures(measuresWithData);
			qryvo.setNeedreplaceAdd(false);
			qryvo.setFormatCellsModel(formatModel);
			HBBBRepUtil.calcHBBBTotalFormulasWithMselectFunc(qryvo);

			// �ϲ���������
			String aloneid = pubdata.getAloneID();
			ArrayList<MeasurePubDataVO>  totalaraylist  = new ArrayList<MeasurePubDataVO>();
			totalaraylist.add(totalpubData);
			// �ϲ���MeasureData
			datavos = getMeasureDatas(measureVOs, totalaraylist);
			for (MeasureDataVO dvo : datavos) {
				dvo.setAloneID(aloneid);
			}

			//����ָ���Ӧ����Ŀ
			Map<String, ProjectVO> measureProjectVOs = ProjectSrvUtils.getMeasureMappings(new String[] {pk_report});

			//4.2: ���ɵ����跽���ݡ����ɵ�����������
			storevouchdata(vouchDataCond, pk_report, measureProjectVOs,IVouchDirections.DIRECTION_DEBIT,measureVOs,formatModel);
			storevouchdata(vouchDataCond,  pk_report, measureProjectVOs,IVouchDirections.DIRECTION_CREDIT,measureVOs,formatModel);

			//�����衢��������
			Map<String, UFDouble> debitmap = getVouchDatas(vouchDataCond.getDebitpubdata(),measureVOs);
			Map<String, UFDouble> creditmap = getVouchDatas(vouchDataCond.getCreaditpubdata(),measureVOs);

			//4.3:���ɺϲ����汾
			//��Ͷ���--�ϲ����ж����С��ֻ������λС����
	
//			if(vouchDataCond.getHBSchemeVO()!=null&&vouchDataCond.getHBSchemeVO().getCode().equals(313)){
				genUnionRepData(pk_report,pubdata,datavos, measureProjectVOs, debitmap,creditmap,true);

//			}else{
//				genUnionRepData(pk_report,pubdata,datavos, measureProjectVOs, debitmap,creditmap,false);
//
//			}
			 
			
			//4.4:���ɷ����������汾
			Map<String, MeasurePubDataVO[]> typeMeaPubData = vouchDataCond.getMap();
			typeMeaPubData.put(DXTypeValue.DXTYPE_PK, new MeasurePubDataVO[]{vouchDataCond.getDebitpubdata(),
					vouchDataCond.getCreaditpubdata(),vouchDataCond.getContrastpubdata()});
			for(Map.Entry<String, MeasurePubDataVO[]> enter:typeMeaPubData.entrySet()){
				MeasurePubDataVO[] pubs = enter.getValue();
				if(pubs != null){
					MeasurePubDataVO typedeb = pubs[0];
					MeasurePubDataVO typecre = pubs[1];
					MeasurePubDataVO typecontra = pubs[2];

					//�����衢��������
					Map<String, UFDouble> typedebitmap = getVouchDatas(typedeb,measureVOs);
					Map<String, UFDouble> typecreditmap = getVouchDatas(typecre,measureVOs);

					MeasureDataVO[] typeContrDatas = contrastdatavos.clone();
					//���ص����������contrastdatavos
					getContrastMeaDataValues(measureProjectVOs, typeContrDatas, typedebitmap,typecreditmap);
					//���ɷ�����������ݰ汾
					genContrastRepData(typecontra,typeContrDatas,measuresWithData, pk_report,formatModel);
				}
			}

			//4.6:ִ����Ŀͬ���������ǽ������裬���������ݶ�ִ������Ŀ����ͬ��
			ProjectSynchronize syn = new ProjectSynchronize();
			syn.setDebitPubdata(vouchDataCond.getDebitpubdata());
			syn.setCreditPubdata(vouchDataCond.getCreaditpubdata());
			syn.setAryRepIDs(new String[] {pk_report});
			syn.setDstPubdata(pubdata);
			syn.setMeasProMap(measureProjectVOs);
			syn.setHbSchemeVo(getHbschemevo());
			syn.doHBSynchronize();

			//4.7:�ϲ�����ִ���������
			qryvo = new HBBBFuncQryVO();
			qryvo.setAryRepIDs(new String[] {pk_report});
			qryvo.setbAddLeft(Boolean.FALSE);
			qryvo.setHbSchemeVo(getHbschemevo());
			qryvo.setIsconvert(false);
			qryvo.setMeasures(measuresWithData);
			qryvo.setPubdata(pubdata);
			qryvo.setStrUserID(InvocationInfoProxy.getInstance().getUserId());
			qryvo.setNeedreplaceAdd(false);
			qryvo.setFormatCellsModel(formatModel);
			HBBBRepUtil.calcFormulasWithOutMSelectFuncWithOutTotalMeas(qryvo);
		}
	}

	private void genUnionRepData(String pk_report,MeasurePubDataVO pubdata,MeasureDataVO[] datavos,
			Map<String, ProjectVO> measureProjectVOs,
			Map<String, UFDouble> debitmap, Map<String, UFDouble> creditmap,boolean isTwoPoint) {
		try {
			for (MeasureDataVO dvo : datavos) {
				// @edit by zhoushuang at 2015-5-13,����2:30:42  ����ֵ��ָ�겻����ϲ�
				if (null != dvo&& null != dvo.getMeasureVO() && (dvo.getMeasureVO().getType() == IStoreCell.TYPE_NUMBER
						|| dvo.getMeasureVO().getType() == IStoreCell.TYPE_BIGDECIMAL)) {
					if(StringUtil.isEmpty(dvo.getDataValue())){
						dvo.setDataValue(UFDouble.ZERO_DBL.toString());//��ֹ��ЩvalueΪ"";
					}
					String measurekey = dvo.getMeasureVO().getCode();
					//���ݺϲ���Ŀ�Ľ������Ӽ�
					ProjectVO projectVO = measureProjectVOs.get(measurekey);
					//��Ϊ�յ����,������������
					if (projectVO == null) {
					
						if(isTwoPoint){
							UFDouble tempValue = new UFDouble(dvo.getDataValue(),2);
							dvo.setDataValue(tempValue.toString());
							continue; 
						}else{
							dvo.setDataValue(String.valueOf(dvo.getDataValue()));
							continue;
						}
						
					}
					Integer direction = projectVO.getDirection();
					UFDouble debitvalue = debitmap.get(measurekey) == null ? UFDouble.ZERO_DBL : debitmap.get(measurekey);
					UFDouble creditvalue = creditmap.get(measurekey) == null ? UFDouble.ZERO_DBL : creditmap.get(measurekey);
					UFDouble value = UFDouble.ZERO_DBL;
					UFDouble dvoValue = new UFDouble(dvo.getDataValue());
					if (direction != null && direction.intValue() == IVouchDirections.DIRECTION_DEBIT) {
						value = debitvalue.sub(creditvalue).add(dvoValue);
					} else if (direction != null && direction.intValue() == IVouchDirections.DIRECTION_CREDIT) {
						value = creditvalue.sub(debitvalue).add(dvoValue);
					}
					UFDouble testValue = new UFDouble(Math.abs( value.doubleValue()));
					
					double twoPointNum  =  Math.floor( testValue.multiply(100).doubleValue())*10000;
					double pointNum  =  Math.floor( testValue.multiply(1000000).doubleValue());
					if(twoPointNum!=pointNum){
						Logger.error("Measure data value err:"+dvo);
					}
					if(isTwoPoint){
						value = new UFDouble(value.doubleValue(),2);
					}
					dvo.setDataValue(value.toString());
				}
			}
			//4.4: ����ϲ�����汾��datavos�Ѿ����ӽ�����Ĳ���
			RepDataVO repData = new RepDataVO(pk_report, getHbschemevo().getPk_keygroup());
			repData.setDatas(new MeasurePubDataVO[] {pubdata}, datavos);
			MeasureDataBO_Client.editRepData(repData.getMainPubData().getAloneID(), datavos);
		} catch (NumberFormatException e) {
			AppDebug.debug(e.getMessage());
		} catch (Exception e) {
			AppDebug.debug(e.getMessage());
		}
	}

	/**
	 * ���ɺϲ�����������
	 * @param pubdata�ϲ�pubdata
	 * @param contrastdatavos
	 * @param contrastrepData
	 * @param measuresWithDataָ��vos
	 * @param pk_report
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws NamingException
	 * @throws SQLException
	 */
	private void genContrastRepData(MeasurePubDataVO contrastPubData,MeasureDataVO[] contrastdatavos,MeasureVO[] measuresWithData,String pk_report,CellsModel formatModel) throws BusinessException, SystemException,
			NamingException, SQLException {
		if (null == contrastdatavos || contrastdatavos.length == 0) {
			return;
		}
		try {
			//���ɵ���������
			RepDataVO contrRepData = new RepDataVO(pk_report, getHbschemevo().getPk_keygroup());
			for (MeasureDataVO vo : contrastdatavos) {
				vo.setAloneID(contrastPubData.getAloneID());
			}
			contrRepData.setDatas(new MeasurePubDataVO[] { contrastPubData }, contrastdatavos);
			MeasureDataBO_Client.editRepData(contrRepData.getMainPubData().getAloneID(), contrastdatavos);

			//��ʽ����
			HBBBFuncQryVO qryvo = new HBBBFuncQryVO();
			qryvo.setAryRepIDs(new String[] {pk_report});
			qryvo.setbAddLeft(Boolean.FALSE);
			qryvo.setHbSchemeVo(getHbschemevo());
			qryvo.setIsconvert(false);
			qryvo.setMeasures(measuresWithData);
			qryvo.setPubdata(contrastPubData);
			qryvo.setStrUserID("");
			qryvo.setNeedreplaceAdd(false);
			qryvo.setFormatCellsModel(formatModel);
			HBBBRepUtil.calcHBConvertFormulasWithOutMSelectFunc(qryvo);
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}

	private void storevouchdata(HBUnionVouchDataCond vouchDataCond,String pk_report, Map<String, ProjectVO> measureProjectMap, int DIRECTION,MeasureVO[] measureVOs,CellsModel formatmodel) throws BusinessException {
		VouchDadaToDraftRepQryVO dqryvo = new VouchDadaToDraftRepQryVO();
		MeasurePubDataVO pubdata = null;
		if(DIRECTION == IVouchDirections.DIRECTION_CREDIT)
			pubdata = vouchDataCond.getCreaditpubdata();
		else
			pubdata = vouchDataCond.getDebitpubdata();
		dqryvo.setUserid(getQryvo().getPk_user());
		dqryvo.setPubdata(pubdata);
		dqryvo.setAryRepIDs(new String[] {pk_report});
		dqryvo.setMappings(measureProjectMap);

		dqryvo.setStrVouchAloneID(vouchDataCond.getVouchPubData().getAloneID());
		dqryvo.setHbSchemeVo(getHbschemevo());
		dqryvo.setDirection(DIRECTION);
		dqryvo.setiVer(pubdata.getVer());
		new VouchDataToDraftReportUtilBO(dqryvo).storeHBBBVouchData(vouchDataCond,formatmodel);
	}

	/**
	 * ����ָ��ָ���ֵ
	 * @param pubdata
	 * @param measureVOs
	 * @return
	 */
	private Map<String, UFDouble> getVouchDatas(MeasurePubDataVO pubdata,MeasureVO[] measureVOs){
		Map<String, UFDouble> result = new ConcurrentHashMap<String, UFDouble>();
		try {
			MeasureDataVO[] dataYs = null; // ԭ�����׸嵱ǰ�ڼ�����
			dataYs = MeasureDataProxy.getRepData(new String[] {pubdata.getAloneID()}, measureVOs);
			for (MeasureDataVO vo : dataYs) {
				result.put(vo.getCode(), new UFDouble(vo.getUFDoubleValue()==null ? 0.0:vo.getUFDoubleValue().doubleValue()));
			}
		} catch (NumberFormatException e) {
			AppDebug.debug(e.getMessage());
		} catch (BusinessException e) {
			AppDebug.debug(e.getMessage());
		}
		return result;
	}

	private void getContrastMeaDataValues(
			Map<String, ProjectVO> measureProjectVOs,
			MeasureDataVO[] contrastdatavos, Map<String, UFDouble> debitmap,
			Map<String, UFDouble> creditmap) {
		for (MeasureDataVO dvo : contrastdatavos) {
			String measurekey = dvo.getMeasureVO().getCode();
			// �������ɵ���������,���ǰ��պϲ���Ŀ�Ľ������Ӽ�
			// ���ݺϲ���Ŀ�Ľ������Ӽ�
			ProjectVO projectVO = measureProjectVOs.get(measurekey);
			// ��Ϊ�յ����,������������
			if (projectVO == null) {
				continue;
			}
			Integer direction = projectVO.getDirection();

			UFDouble debitvalue = debitmap.get(measurekey) == null ? UFDouble.ZERO_DBL : debitmap.get(measurekey);
			UFDouble creditvalue = creditmap.get(measurekey) == null ? UFDouble.ZERO_DBL : creditmap.get(measurekey);
			UFDouble value = UFDouble.ZERO_DBL;
			if (direction != null && direction.intValue() == IVouchDirections.DIRECTION_DEBIT) {
				value = debitvalue.sub(creditvalue);
			} else if (direction != null && direction.intValue() == IVouchDirections.DIRECTION_CREDIT) {
				value = creditvalue.sub(debitvalue);
			}
			dvo.setDataValue(value.toString());
		}
	}

	public HBSchemeVO getHbschemevo() {
		return hbschemevo;
	}

	public UnionReportQryVO getQryvo() {
		return qryvo;
	}

}