package nc.bs.hbbb.contrast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.exception.ComponentException;
import nc.bs.hbbb.contrast.rightandInterest.RightAndInterestManager;
import nc.bs.hbbb.contrast.rightandInterest.RightAndInterestType;
import nc.bs.hbbb.dxmodelfunction.HBProjectBOUtil;
import nc.bs.logging.Logger;
import nc.bs.uif2.LockFailedException;
import nc.itf.corg.IStockInvestRelaQryService;
import nc.itf.hbbb.contrast.ContrastMeasPubDataCache;
import nc.itf.hbbb.contrast.IntrMeasProjectCache;
import nc.itf.hbbb.dxrelation.IDxFunctionConst;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.itf.uif.pub.IUifService;
import nc.jdbc.framework.SQLParameter;
import nc.pub.hbbb.exception.UFOCUnThrowableException;
import nc.pub.iufo.cache.KeywordCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.uif.pub.exception.UifException;
import nc.util.hbbb.EndDataUtil;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.NumberFormatUtil;
import nc.util.hbbb.UFOCSqlUtil;
import nc.util.hbbb.contrast.ContrastMeetFilterUtil;
import nc.util.hbbb.dxfunction.bself.DxFuncProxy;
import nc.util.hbbb.dxrelation.formula.DXFormulaDriver;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.pub.HBPubItfService;
import nc.util.iufo.pub.IDMaker;
import nc.vo.corg.StockInvestRelaVO;
import nc.vo.glcom.tools.GLContrastProxy;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.dxfunction.project.MeasureReportVO;
import nc.vo.hbbb.dxrelation.DXContrastVO;
import nc.vo.hbbb.dxrelation.DXRelaDiffRuleVO;
import nc.vo.hbbb.dxrelation.DXRelationBodyVO;
import nc.vo.hbbb.dxrelation.IDXRelaConst;
import nc.vo.hbbb.dxscheme.AggDXSchemeVO;
import nc.vo.hbbb.dxscheme.DXSchemeVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.meetaccount.AggMeetRltHeadVO;
import nc.vo.hbbb.meetdata.AggMeetdataVO;
import nc.vo.hbbb.meetdata.MeetdataVO;
import nc.vo.hbbb.meetdata.MeetdatasubVO;
import nc.vo.hbbb.util.MD5;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.dxscheme.DxSchemeSubVO;
import nc.vo.ufoc.dxscheme.DxschDetailVO;
import nc.vo.util.BDPKLockUtil;
import nc.vo.vorg.ReportCombineStruMemberVersionVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.script.UfoFormulaProxy;
import com.ufsoft.script.base.UfoEElement;
import com.ufsoft.script.exception.CreateProxyException;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.script.expression.UfoExpr;
import com.ufsoft.script.extfunc.MeasFuncDriver;
import com.ufsoft.script.function.ExtFunc;
import com.ufsoft.script.spreadsheet.UfoCalcEnv;

public class ContrastBO {

	private ContrastQryVO qryvo;
	
	private UFDate enddate;
	
	//��ʽ������
	private UfoFormulaProxy parser;
	//���㻷��
	private UfoCalcEnv env;
	
	public static String LOCK_HB_KEY = "UFOC_CONTRAST";

	public ContrastBO(ContrastQryVO new_qryvo) throws BusinessException {
		super();
		try {
			HBSchemeVO schemevo = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(new_qryvo.getSchemevo().getPk_hbscheme());
			new_qryvo.setSchemevo(schemevo);
			DXContrastVO[] dxmodels = new_qryvo.getDxmodels();
			for (DXContrastVO dxmodel : dxmodels) {
				DXRelationBodyVO[] bodyvos = ((nc.itf.hbbb.dxrelation.IDXRelationQrySrv) NCLocator.getInstance().lookup(nc.itf.hbbb.dxrelation.IDXRelationQrySrv.class.getName()))
						.queryDXFormulas(dxmodel.getHeadvo().getPk_dxrela_head());
				dxmodel.setBodyvos(bodyvos);
			}
			setQryvo(new_qryvo);
			enddate = getEndDate();
		} catch (ComponentException e) {
			this.error(e);
		} catch (UFOSrvException e) {
			this.error(e);
		}
	}
	
	private UfoFormulaProxy getParser() throws BusinessException {
		if (parser == null) {
			try {
				parser = new UfoFormulaProxy(getCalEnv());
			} catch (CreateProxyException e) {
				nc.bs.logging.Logger.error(e.getMessage(), e);
				throw new BusinessException(e);
			}
		}
		return parser;
	}
	
	private UfoCalcEnv getCalEnv(){
		if(env == null){
			//����Ч�ʸ��ġ���Ԥ�����ü��㻷������Ҫÿ�μ��� jiaah
			env = new UfoCalcEnv(null,null,false,null);
			KeywordCache keyCache = UFOCacheManager.getSingleton().getKeywordCache();
			java.util.Vector<KeyVO> keyVector = keyCache.getAllKeys();
			env.setKeys(keyVector.toArray(new KeyVO[0]));
			env.loadFuncListInst().registerExtFuncs(new DXFormulaDriver(env));
			env.loadFuncListInst().registerExtFuncs(new MeasFuncDriver(env));
		}
		return env;
	}

	private void batchPareFormula(DXContrastVO[] dxcontrasts) throws BusinessException{
		for(DXContrastVO vo : dxcontrasts){
			DXRelationBodyVO[] bodys = vo.getBodyvos();
			for(DXRelationBodyVO bodyvo :  bodys){
				String formula = bodyvo.getFormula();
				UfoExpr expr = null;
				try {
					expr = getParser().parseExpr(formula);
				} catch (ParseException e) {
					AppDebug.error(e.getMessage());
				}
				bodyvo.setExpr(expr);
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void doContrast() throws BusinessException {
		String pk_hbrepstru = qryvo.getPk_hbrepstru();
		String pk_contrastorg = qryvo.getContrastorg(); 
		if(pk_hbrepstru == null  || pk_contrastorg == null)
			return;
		
		//���ж��˵�λ�����
		String[] contrastorgs = this.getContrastOrgs(pk_hbrepstru, pk_contrastorg);
		if (null == contrastorgs || contrastorgs.length == 0) {
			return;
		}
		//���������Ӷ�̬��
		String alone_id = HBAloneIDUtil.getAdjustVoucherAlone_id(this.getQryvo(), false);
		String pkLock = qryvo.getSchemevo().getPk_hbscheme() + alone_id;//����+aloneidΨһȷ��
		qryvo.setPkLock(pkLock);
		try {
			BDPKLockUtil.lockString(LOCK_HB_KEY + pkLock);
		} catch (Exception e1) {
			if (e1 instanceof LockFailedException) {
				throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0141")/* @res "�����û�����ִ�иò���,���Ժ�����!" */);
			}
		}
		startContrast(contrastorgs,alone_id);
	}
	
	@SuppressWarnings("unchecked")
	private void startContrast(String[] contrastorgs, String alone_id) throws BusinessException {
		// ����ɾ����ʷ�������� 
		Map<String, String> mapContrastNote = new HashMap<String, String>();
		DXContrastVO[] dxvos = qryvo.getDxmodels();
		for (DXContrastVO vo : dxvos) {
			mapContrastNote.putAll(ContrastResultBO.clearContrastedData(vo, qryvo));
		}
		
		//Ԥ������֯�������
		HashSet<String> selfOrgSet = new HashSet<String>();//������֯
		HashSet<String> oppOrgSet = new HashSet<String>();//�Է���֯
		HashSet<String> orgset = new HashSet<String>();//������֯
		
		for (String str : contrastorgs) {
			String pk_self = str.trim().substring(0, 20);
			selfOrgSet.add(pk_self);
			String pk_other = str.trim().substring(20, 40);
			oppOrgSet.add(pk_other);
			orgset.add(pk_self);
			orgset.add(pk_other);
		}
		// Ԥ�������жԷ���֯����ʵ��λ��Ӧ���˴����ص�Ϊ�鵥λ����ʵ��λ�ԣ������鵥λ�Ĳ���Ҫ����
		Map<String, String> oppEntityOrgs = HBRepStruUtil.getoppEntityOrgs(oppOrgSet.toArray(new String[0]), qryvo.getPk_hbrepstru());
		// Ԥ���ضԷ���λ�Ƿ�������֯
		// @edit by zhoushuang at 2015-5-26,����7:15:46
		// �Ƿ����鵥λͨ��oppEntityOrgs���ɻ�ã�����Ҫ��ѯ�� oppEntityOrgs.keySet()��Ϊ�����鵥λ
		Set<String> manageOrg = oppEntityOrgs.keySet();
		// Map<String, UFBoolean> mapIsManageOrg =HBRepStruUtil.getBooleanEntityOrgs(oppOrgSet.toArray(new String[0]), pk_hbrepstru);
		
		// Ԥ���ص�ǰ�ڼ�Ĺؼ���map
		Map<String, String> offset = ContrastMeasPubDataCache.getInstance().getOffSets(qryvo.getSchemevo(), qryvo).get(qryvo.getPkLock());
		
		// Ԥ������֯��Ӧ���ڲ����̵�pkMap
		Map<String, String> org_supplier_map = HBRepStruUtil.getOrgSuppliesMap(selfOrgSet.toArray(new String[0]));
		qryvo.setOrg_supplier_map(org_supplier_map);
		qryvo.setSelfOrgs(selfOrgSet);
		qryvo.setOppOrgs(oppOrgSet);
		qryvo.setOrgs(orgset);
		qryvo.setContrastorgs(contrastorgs);
		qryvo.setOppEntityOrgs(oppEntityOrgs);
		qryvo.setOffset(offset);
		qryvo.setMeaprojectcache(IntrMeasProjectCache.getSingleton().getInstance());
		// Ԥ���ص�ǰ���������ĵ�������:KEY:pk_dxrela
		Map<String, DXRelaDiffRuleVO> diffRuleMap = getAllDiffRuleMap();

		// ģ�尴��Ȩ��ͷ�Ȩ������з���
		Object[] relas = getDxrelaByType(dxvos);
		List<DXContrastVO> investDxRelas = (List<DXContrastVO>) relas[0];
		List<DXContrastVO> noInvestDxRelas = (List<DXContrastVO>) relas[1];
		
		// ����������ʽ,��bodyvo��Ԥ�ý�����expr
		batchPareFormula(dxvos);

		// ���ģ����ж���
		try {
			List<AggMeetRltHeadVO> resultLists = new ArrayList<AggMeetRltHeadVO>();
			// 1������Ȩ�����ģ��
			if (investDxRelas.size() > 0) {
				// Ԥ���ؿ�����ģ�壺�Է���֯�Ƿ�����Ч����֯
				Map<String, UFBoolean> mapIsVoidOrg = HBRepStruUtil
						.batchCheckVoidOrgWithManageOrg(
								orgset.toArray(new String[orgset.size()]),
								oppOrgSet.toArray(new String[oppOrgSet.size()]),
								manageOrg,  qryvo.getPk_hbrepstru());
				// Ԥ���ؿ�����ģ�壺����ģ�������жϱ��Է���֯�Ƿ����Ͷ�ʹ�ϵ
				Map<String, UFBoolean> mapIsExistInvest = getMapIsExistInvest(selfOrgSet, oppOrgSet, qryvo.getSchemevo().getPk_investscheme());
				// Ȩ����ģ�����
				for (DXContrastVO vo : investDxRelas) {
					for (String str : contrastorgs) {
						// ���һ�ױ��д��ڲ�ͬ�Ķ�̬���ؼ��ֵ��¶Բ������ݵ�����
						ContrastMeasPubDataCache.getInstance().clearPk_dynKeyValue(qryvo.getPkLock());

						String pk_self = str.trim().substring(0, 20);
						String pk_other = str.trim().substring(20, 40);
						boolean isVoidOrg = mapIsVoidOrg.get(pk_other)
								.booleanValue();
						if (mapIsExistInvest.get(str) == null
								&& !manageOrg.contains(pk_other)) {// ��ʵ��֯�Ҳ�����Ͷ�ʹ�ϵֱ��continue
							continue;
						}
						// ���ӹ�ȨͶ�ʷ�������
						RightAndInterestType righttype = RightAndInterestManager
								.createRightAndInterestType(vo, pk_self,
										pk_other, enddate,  qryvo.getPk_hbrepstru(), qryvo
												.getSchemevo()
												.getPk_investscheme());
						// �ǿ�����ģ����Ҫ�ж��鵥λ
						// ��ǰԭ���������˵�λ��������鵥λ����Ҫ���鵥λָ����ʵ�嵥λҲ�ڸö��ʵ�λ����,��ָ����ʵ�嵥λ���ڶ��ʵ�λ�������鵥λ������ִ�ж���
						// ͬʱ��ʵ�嵥λ�Ͳ��ٲ�����ʣ�����Ӧ���鵥λ�������,����ʵ�嵥λ���ڵ��鵥λ���ڸö��ʵ�λ������ִ�ж���
						// ��ס��ֻ�и�ʵ����鵥λ����Ϊ��Ͷ�ʷ���ʱ�������ж�
						if (isVoidOrg && righttype.needContrast()) {
							genMeetDataVO(getCalEnv(), true, vo, pk_self,
									pk_other, mapContrastNote, alone_id,
									resultLists, diffRuleMap);
							if (resultLists.size() > 2000) {
								HBPubItfService.getRemoteMeetResult().insertVos(resultLists.toArray(new AggMeetRltHeadVO[resultLists.size()]));
								resultLists.clear();
							}
						}
					}
					// �����ǰģ���qryVO�е��м���
					removeQryResultMap(vo);
				}

			}
			// 2����ν��н���������ģ��Ķ���
			if (noInvestDxRelas.size() > 0) {
				for (DXContrastVO vo : noInvestDxRelas) {
					// ���һ�ױ��д��ڲ�ͬ�Ķ�̬���ؼ��ֵ��¶Բ������ݵ�����
					ContrastMeasPubDataCache.getInstance().clearPk_dynKeyValue(qryvo.getPkLock());

					for (String str : contrastorgs) {
						String pk_self = str.trim().substring(0, 20);
						String pk_other = str.trim().substring(20, 40);
						// �鵥λ��ʱ����Ҫִ�н������ģ��
						if (manageOrg.contains(pk_other)
								|| manageOrg.contains(pk_self)) {
							continue;
						}
						genMeetDataVO(getCalEnv(), true, vo, pk_self, pk_other,mapContrastNote, alone_id, resultLists,diffRuleMap);
						if (resultLists.size() > 2000) {
							HBPubItfService.getRemoteMeetResult().insertVos(resultLists.toArray(new AggMeetRltHeadVO[resultLists.size()]));
							resultLists.clear();
						}
					}
					// �����ǰģ���qryVO�е��м���
					removeQryResultMap(vo);
				}
			}
			// ���������¼
			if (resultLists.size() > 0) {
				HBPubItfService.getRemoteMeetResult().insertVos(resultLists.toArray(new AggMeetRltHeadVO[resultLists.size()]));
				resultLists.clear();
			}
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
			if (e instanceof UFOCUnThrowableException)
				throw new UFOCUnThrowableException(e.getMessage());
			else {
				throw new BusinessException(e.getMessage());
			}
		} finally {
			// �������ͷ�
			ContrastMeasPubDataCache.getInstance().clear(qryvo.getPkLock());
			IntrMeasProjectCache.getSingleton().clear();
		}
	}
	/**
	 * ����ʹ�ã�pk������������������ҵ���ظ�
	 * @create by zhoushuang at 2015-7-4,����11:25:55
	 *
	 * @return
	 * @throws BusinessException
	 */
	public int doContrastBySubContrastorgs() throws BusinessException { 
		//���˵�λ�����
		String[] contrastorgs = qryvo.getContrastorgs();
		if (null == contrastorgs || contrastorgs.length == 0) {
			return -1;
		}
		//���������Ӷ�̬��
		String alone_id = HBAloneIDUtil.getAdjustVoucherAlone_id(this.getQryvo(), false);
		String pkLock = qryvo.getSchemevo().getPk_hbscheme() + alone_id + IDMaker.makeID(5);//����+aloneidΨһȷ��
		qryvo.setPkLock(pkLock);
		try {
			BDPKLockUtil.lockString(LOCK_HB_KEY + pkLock);
		} catch (Exception e1) {
			if (e1 instanceof LockFailedException) {
				throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0141")/* @res "�����û�����ִ�иò���,���Ժ�����!" */);
			}
		}
		startContrast(contrastorgs,alone_id);
		return 0;
	}
	
	/**
	 * ���qryVO�е��м伶���˽��
	 * @create by jiaah at 2013-8-8,����7:50:52
	 * @param measureCodes
	 * @throws BusinessException 
	 */
	@SuppressWarnings("rawtypes")
	private void removeQryResultMap(DXContrastVO dxContrastVO) throws BusinessException{
		if(qryvo == null)
			return;
		String pk_hbscheme = qryvo.getSchemevo().getPk_hbscheme();
		//Ԥ���ص�ӳ���ϵ
		Map<String, MeasureReportVO> prjoectMeasMapCache = new HashMap<String, MeasureReportVO>();
		IntrMeasProjectCache cacheinstance = qryvo.getIntrMeaProjectinstance();
		if(cacheinstance != null){
			prjoectMeasMapCache = cacheinstance.getMeasRepVOs();
		}
		//������Ҫ�����ָ��code
		Set<String> measureCodeLst = new HashSet<String>();
		DXRelationBodyVO[] bodyVos = dxContrastVO.getBodyvos();
		for(DXRelationBodyVO vo : bodyVos){
			UfoExpr expr = vo.getExpr();
			if(expr == null)
				continue;
			UfoEElement[] elements = expr.getElements();
			if(elements == null || elements.length == 0)
				continue;
			
			for (int i = 0; i < elements.length; i++) {
				// ������Ϊshort�����,Ŀǰֱ�Ӻ���
                if (!(elements[i].getObj() instanceof ExtFunc)) {
                    continue;
                }
                ExtFunc tmpformula = (ExtFunc) elements[i].getObj();
                //key����������
                if(tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.KEYFUNC)){
                	continue;
                }
                List params = tmpformula.getParams();
                String projectcode = String.valueOf( params.get(0));//������Ŀ����
                if (StringUtil.isEmptyWithTrim(projectcode)) {
                    continue;
                }
                String[] splitprojectcode = projectcode.split("/");
                
                //���ض�Ӧ��ָ�����
                if (splitprojectcode != null && splitprojectcode.length > 0) {
                	String key = pk_hbscheme + splitprojectcode[1];
            		MeasureReportVO measrepvo = prjoectMeasMapCache.get(key);
            		if(measrepvo == null){
            			try {
            				//��Щ��������δ���env�Ͷ�������ˣ���Ȩ����ģ�壬���������Ͷ�ʹ�ϵ��ʱ�� envӦ����Ĭ��ֵδ������������
    						measrepvo = HBProjectBOUtil.getProjectMeasVO(env, splitprojectcode[1], true);
    						if(measrepvo==null&&splitprojectcode[1].endsWith("'")){
    							measrepvo = HBProjectBOUtil.getProjectMeasVO(env, splitprojectcode[1].substring(0,splitprojectcode[1].length()-1), true);
    						}
    					} catch (Exception e) {
    						
    					}
            		}
            		if(measrepvo != null){
            			MeasureVO measVO = measrepvo.getMeasVO();	
            			measureCodeLst.add(measVO.getCode());
            		}
				}
			}
		}
		//����м���
		Map<String, Map<String,UFDouble>> resultMap = qryvo.getResultMap();
		if(resultMap != null && resultMap.size() > 0 && measureCodeLst.size() > 0){
			for(String s : measureCodeLst){
				resultMap.remove(s);
			}
		}
	}

	/**
	 * Ԥ���ر��Է��Ƿ����Ͷ�ʹ�ϵ
	 * @create by jiaah at 2013-8-8,����7:39:36
	 * @param selfOrgSet
	 * @param oppOrgSet
	 * @return
	 * @throws BusinessException
	 */
	private Map<String, UFBoolean> getMapIsExistInvest(HashSet<String> selfOrgSet, HashSet<String> oppOrgSet, String pk_investscheme)
			throws BusinessException {
		Map<String, UFBoolean> mapIsExistInvest = new HashMap<String, UFBoolean>();
		String investorStr = UFOCSqlUtil.buildInSql(StockInvestRelaVO.INVESTOR, selfOrgSet, true);
		String investeeStr = UFOCSqlUtil.buildInSql(StockInvestRelaVO.INVESTEE, oppOrgSet, true);
		String sqlWhere = StockInvestRelaVO.PK_INVESTSCHEME + " = '" + pk_investscheme+"' and "+investorStr + " and "+ investeeStr + " and investdate<='" + enddate + "' ";
		StockInvestRelaVO[] stockInvestRelaVOs = NCLocator.getInstance().lookup(IStockInvestRelaQryService.class).queryStockInvestRelaVOsByCon(sqlWhere);
		if(stockInvestRelaVOs != null && stockInvestRelaVOs.length > 0){
			for(StockInvestRelaVO vo : stockInvestRelaVOs){
				String key = vo.getInvestor() + vo.getInvestee();
				if(mapIsExistInvest.get(key) == null){
					mapIsExistInvest.put(key, UFBoolean.valueOf(true));
				}
			}
		}
		return mapIsExistInvest;
	}
	
	/**
	 * ����ģ�尴��Ȩ���Ȩ����з���
	 * @create by jiaah at 2013-8-8,����7:36:44
	 * @param dxvos
	 * @return
	 * @throws BusinessException
	 */
	private Object[] getDxrelaByType(DXContrastVO[] dxvos) throws BusinessException{
		//Ȩ����ģ��ͷ�Ȩ����ģ��
		List<DXContrastVO> investDxRelas = new ArrayList<DXContrastVO>();
		List<DXContrastVO> noInvestDxRelas = new ArrayList<DXContrastVO>();
		for (DXContrastVO dxrelaVO : dxvos){
			if(dxrelaVO.getHeadvo().getType().equals(IDXRelaConst.RIGHTANDRIGHT_INVEST) || dxrelaVO.getHeadvo().getType().equals(IDXRelaConst.RIGHTANDRIGHT_UNINVEST)
					|| dxrelaVO.getHeadvo().getType().equals(IDXRelaConst.RIGHTANDRIGHT_ALLOWNERINVEST)){
				investDxRelas.add(dxrelaVO);
			}
			else{
				noInvestDxRelas.add(dxrelaVO);
				//����������ڲ����׶��˹���
				if(dxrelaVO.getHeadvo().getPk_contrastrule() != null){
					dxrelaVO.setContrastRuleVo(GLContrastProxy.getRemoteContrastRule().findByPrimaryKey(dxrelaVO.getHeadvo().getPk_contrastrule()));
				}
			}
		}
		return new Object[]{investDxRelas,noInvestDxRelas};
	}
	
	
	/**
	 * Ԥ�������еĲ�����
	 * @param diffRuleMap
	 * @throws UifException
	 * @throws BusinessException
	 * @throws DAOException
	 */
	@SuppressWarnings("unchecked")
	private Map<String, DXRelaDiffRuleVO>  getAllDiffRuleMap()throws UifException, BusinessException, DAOException {
		Map<String, DXRelaDiffRuleVO> diffRuleMap = new HashMap<String, DXRelaDiffRuleVO>();
		if(qryvo.getSchemevo().getPk_dxscheme() != null){
			AggregatedValueObject[] vos = null;
			String[] clazzNames = new String[] { AggDXSchemeVO.class.getName(),
					DXSchemeVO.class.getName(), DxSchemeSubVO.class.getName(),
					DxschDetailVO.class.getName() };
			IUifService iuiService = NCLocator.getInstance().lookup(IUifService.class);
			vos = iuiService.queryBillVOByCondition(clazzNames, "pk_dxscheme = '"+ qryvo.getSchemevo().getPk_dxscheme() +"'");
			
			BaseDAO dao = new BaseDAO();
			if(vos != null && vos.length > 0){
				AggDXSchemeVO aggVo = (AggDXSchemeVO)vos[0];
				DxSchemeSubVO[] dxSubVos = (DxSchemeSubVO[])aggVo.getTableVO(aggVo.getTableCodes()[0]);
				if(dxSubVos != null && dxSubVos.length > 0){
					List<String> diffRulePks = new ArrayList<String>();
					for(DxSchemeSubVO subvo : dxSubVos){
						if(subvo.getPk_difrule() == null)
							continue;
						diffRulePks.add(subvo.getPk_difrule());
					}
					
					//�������еĲ�����������Ӧvo
					Map<String, DXRelaDiffRuleVO> map = new HashMap<String, DXRelaDiffRuleVO>();
					if(diffRulePks.size() > 0){
						StringBuffer buff = new StringBuffer();
						String condition = UFOCSqlUtil.buildInSql(DXRelaDiffRuleVO.PK_DIFRULE, diffRulePks);
						buff.append(condition);
						buff.append(" and sealflag = 'N'");
						Collection<DXRelaDiffRuleVO> c = dao.retrieveByClause(DXRelaDiffRuleVO.class, condition);
						if(c != null && c.size() > 0){
							for(DXRelaDiffRuleVO VO : c){
								map.put(VO.getPk_difrule(), VO);
							}
						}
					}
					
					for(DxSchemeSubVO subvo : dxSubVos){
						DXRelaDiffRuleVO diffvo = map.get(subvo.getPk_difrule());
						if(subvo.getPk_difrule() != null && diffvo != null){
							diffRuleMap.put(subvo.getPk_dxrelation(), diffvo);
						}
					}
				}
			}
		}
		return diffRuleMap;
	}


	private UFDate getEndDate() {
		UFDate result = null;
		String enddate = EndDataUtil.getEndDate(qryvo.getKeymap(), qryvo.getSchemevo());
		result = new UFDate(enddate);
		return result;
	}

	private MeetdatasubVO genMeetdatasubvo(DXRelationBodyVO subvo, ArrayList<MeetdatasubVO> meetdatasublist) {
		MeetdatasubVO subvo1 = new MeetdatasubVO();
		subvo1.setPk_measure(subvo.getPk_measure());
		MeetdatasubVO[] subvos = meetdatasublist.toArray(new MeetdatasubVO[0]);
		double debit = 0;
		double credit = 0;
		double dif = 0;
		for (MeetdatasubVO vo : subvos) {
			if (vo.getDirection().intValue() == IDXRelaConst.CREDIT) {
				credit = credit + NumberFormatUtil.Number2(vo.getAmount().doubleValue());
			} else {
				debit = debit + NumberFormatUtil.Number2(vo.getAmount().doubleValue());
			}
		}
		dif = debit - credit;
		if (debit == credit) {
			// return null;
		}
		//�����״��������¶�λС������
		if (dif > 0) {
			subvo1.setAmount(new UFDouble(NumberFormatUtil.Number2(dif)));
			subvo1.setDirection(IDXRelaConst.CREDIT);
		} else {
			subvo1.setAmount(new UFDouble(NumberFormatUtil.Number2(-dif)));
			subvo1.setDirection(IDXRelaConst.DEBIT);
		}
		subvo1.setBself(UFBoolean.TRUE);

		return subvo1;
	}

	private MeetdatasubVO genMeetdatasubvo(UfoCalcEnv env,DXRelationBodyVO subvo, boolean bself, String pk_self, String pk_other, DXContrastVO contrastVO) throws BusinessException {
		MeetdatasubVO subvo1 = new MeetdatasubVO();
		subvo1.setPk_measure(subvo.getPk_measure());

		try {
			UFDouble data = new UFDouble(ContrastFuncBO.callFunc(env,this.getQryvo(), bself, pk_self, pk_other, subvo, contrastVO,subvo1));
			//�������뱣����λС��
			subvo1.setAmount(new UFDouble(NumberFormatUtil.Number2(data.doubleValue())));
			subvo1.setDirection(subvo.getType());
			boolean self = DxFuncProxy.bSelf(env,subvo);
			if (self) {
				subvo1.setBself(UFBoolean.TRUE);
			} else {
				subvo1.setBself(UFBoolean.FALSE);
			}
		} catch (Exception e) {
			this.error(e);
		}
		return subvo1;
	}
	


	public static String genMd5Key(DXContrastVO vo, String pk_self, String pk_other, ContrastQryVO new_qryvo) {
		StringBuilder content = new StringBuilder();
		String result = "";
		// content.append(vo.getAmounttype());
		// content.append(vo.isIsquantity() ? "Y" :"N");
		content.append(vo.getHeadvo().getPk_dxrela_head());
		content.append(new_qryvo.getSchemevo().getPk_hbscheme());
		content.append(pk_self);
		content.append(pk_other);

		// ��ʼ���ùؼ���
		String[] keys = new String[new_qryvo.getKeymap().keySet().size()];
		new_qryvo.getKeymap().keySet().toArray(keys);
		Arrays.sort(keys);
		for (int i = 0; i < keys.length; i++) {
			content.append(keys[i]).append(new_qryvo.getKeymap().get(keys[i]));
		}
		result = new MD5().getMD5ofStr(content.toString());
		return result;

	}

	/**
	 * @param bself
	 * @param vo
	 * @param pk_self
	 * @param pk_other
	 * @param map����һ��String[����+�Է�+aloneid+hbid+dxrelaid+pk_measure]���ڶ���StringΪ����˵��meetnote
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("static-access")
	private AggMeetdataVO genMeetDataVO(UfoCalcEnv env,boolean bself, DXContrastVO vo, String pk_self, String pk_other,Map<String, String> map,String alone_id,List<AggMeetRltHeadVO> resultList,Map<String, DXRelaDiffRuleVO> diffRuleMap) throws BusinessException {

		AggMeetdataVO aggVO = new AggMeetdataVO();
		MeetdataVO result = new MeetdataVO();
		result.setPk_dxrela(vo.getHeadvo().getPk_dxrela_head());
		result.setPk_scheme(this.getQryvo().getSchemevo().getPk_hbscheme());
		result.setIsself(bself ? UFBoolean.TRUE : UFBoolean.FALSE);

		result.setPk_self(bself ? pk_self : pk_other);
		result.setPk_opp(bself ? pk_other : pk_self);
		result.setPk_contrastorg(this.getQryvo().getContrastorg());
		//modified by jiaah ����sqlʹ��
//		result.setAloneid(HBAloneIDUtil.getAdjustVoucherAlone_id(this.getQryvo(), false));
		result.setAloneid(alone_id);
		// ��ʼ���ùؼ���
		result.setPk_keygroup(this.getQryvo().getSchemevo().getPk_keygroup());

		DXRelationBodyVO[] subvos = vo.getBodyvos();
		ArrayList<MeetdatasubVO> meetdatasublist = new ArrayList<MeetdatasubVO>();
		DXRelationBodyVO difsubvo = null;
		boolean bZero = true;
		for (DXRelationBodyVO subvo : subvos) {
			// ���ǻ�ԵĻ�,���߷�¼�ϵĽ������
			// ���ǲ����Ŀ,���Ȳ�������ֵ
			if (subvo.getType().intValue() == IDXRelaConst.DIFF) {
				difsubvo = subvo;
			} else {
				MeetdatasubVO subVO = genMeetdatasubvo(env,subvo, bself, pk_self, pk_other, vo);
				if(!subVO.getAmount().equals(new UFDouble().ZERO_DBL))
					bZero = false;
				meetdatasublist.add(subVO);
			}
		}
		
		//������ж���0:�������ɶ��˼�¼--modified by jiaah
		if(bZero == true){
			return null;
		}
		
		// ���в����Ŀ����
		if (null != difsubvo) {
			MeetdatasubVO subvo = this.genMeetdatasubvo(difsubvo, meetdatasublist);
			if (null != subvo) {
				meetdatasublist.add(subvo);
			}

		}
		aggVO.setParentVO(result);
		if (meetdatasublist.size() > 0) {

			MeetdatasubVO[] meetsubvos = new MeetdatasubVO[meetdatasublist.size()];
			meetdatasublist.toArray(meetsubvos);
			aggVO.setChildrenVO(meetsubvos);
		}

		// ��ʼ���ж���
		if (null != difsubvo) { // �����߲������
			GenContrastResultBO.genContrastResultWithDifProject(aggVO,map,resultList,diffRuleMap);
		} else { // ��ʼ�߲������
			new GenContrastResultBO().genContrastResult(aggVO, this.getQryvo().getSchemevo(), vo,map,resultList,diffRuleMap);
		}

		return aggVO;
	}
	
	
	/**
	 * ȷ�����˵Ĺ�˾��
	 * @create by jiaah at 2011-12-30,����10:32:32
	 * @param pk_hbrepstru ����ϲ���ϵ�汾����
	 * @param ��ǰ������֯
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public static String[] getContrastOrgs(String pk_hbrepstru, String pk_contrastorg) throws BusinessException {
		String[] result = null;
		String innercode = getInnerCode(pk_hbrepstru, pk_contrastorg);
		if (null == innercode || innercode.trim().length() == 0) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0452")/* @res "��ǰ���˹�˾û���ҵ�inner code" */);
		}
		StringBuffer content = new StringBuffer();
		content.append(" pk_svid = ? AND innercode like '").append(innercode.trim()).append("%' ");
		SQLParameter param = new SQLParameter();
		param.addParam(pk_hbrepstru);
		BaseDAO dao = new BaseDAO();
		Collection<ReportCombineStruMemberVersionVO> c = dao.retrieveByClause(ReportCombineStruMemberVersionVO.class, content.toString(),param);

		List<ReportCombineStruMemberVersionVO> lstMemberVOs = new ArrayList<ReportCombineStruMemberVersionVO>();
		lstMemberVOs.addAll(c);
		
		result = ContrastMeetFilterUtil.getContrastOrgs(lstMemberVOs, innercode);
		return result;
	}

	@SuppressWarnings("unchecked")
	private static String  getInnerCode(String pk_hbrepstru,String pk_org) throws BusinessException{
		ReportCombineStruMemberVersionVO result = null;
		BaseDAO dao = new BaseDAO();
		StringBuilder content = new StringBuilder();
		content.append(" pk_svid=?  and pk_org=?");
		SQLParameter params = new SQLParameter();
		params.addParam(pk_hbrepstru);
		params.addParam(pk_org);
		
		Collection<ReportCombineStruMemberVersionVO> list = null;
		list = dao.retrieveByClause(ReportCombineStruMemberVersionVO.class, content.toString(), params);
		if (null == list || list.size() == 0) {
			throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0451")/*@res "��ѯ������֯��ʱ��δ��ȡ�ñ�����֯VO"*/);
		}
		result = list.toArray(new ReportCombineStruMemberVersionVO[0])[0];
	    return result.getInnercode();
	}

	public ContrastQryVO getQryvo() {
		return qryvo;
	}

	private void setQryvo(ContrastQryVO new_qryvo) {
		qryvo = new_qryvo;
	}

	private void error(Throwable e) throws BusinessException {
		nc.bs.logging.Logger.error(e.getMessage(), e);
		throw new BusinessException(e.getMessage(), e);
	}

	/**
	 * �ж��Ƿ����õ��ȵ���(������λ���¼���������200��������)
	 * @create by zhoushuang at 2015-7-4,����10:50:52
	 *
	 * @param pk_hbrepstru
	 * @param pk_contrastorg
	 * @return
	 * @throws BusinessException 
	 */
	@SuppressWarnings("unchecked")
	public static boolean isStartSchedule(String pk_hbrepstru, String pk_contrastorg) throws BusinessException {
		String innercode = getInnerCode(pk_hbrepstru, pk_contrastorg);
		if (null == innercode || innercode.trim().length() == 0) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0452")/* @res "��ǰ���˹�˾û���ҵ�inner code" */);
		}
		StringBuffer content = new StringBuffer();
		content.append(" pk_svid = ? AND innercode like '").append(innercode.trim()).append("%' ");
		SQLParameter param = new SQLParameter();
		param.addParam(pk_hbrepstru);
		BaseDAO dao = new BaseDAO();
		Collection<ReportCombineStruMemberVersionVO> c = dao.retrieveByClause(ReportCombineStruMemberVersionVO.class, content.toString(),param);
		if (c != null && c.size() > 200) {
			return true;
		}else {
			return false;
		}
	}
	
	

}