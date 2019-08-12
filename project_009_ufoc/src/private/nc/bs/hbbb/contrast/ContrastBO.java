package nc.bs.hbbb.contrast;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import nc.itf.hbbb.contrast.IContrast;
import nc.itf.hbbb.contrast.IntrMeasProjectCache;
import nc.itf.hbbb.dxrelation.IDxFunctionConst;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.itf.hbbb.vouch.constants.IVouchType;
import nc.itf.iufo.data.IMeasurePubDataQuerySrv;
import nc.itf.uif.pub.IUifService;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BaseProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.pub.hbbb.exception.UFOCUnThrowableException;
import nc.pub.iufo.cache.KeywordCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.uif.pub.exception.UifException;
import nc.util.hbbb.EndDataUtil;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.NumberFormatUtil;
import nc.util.hbbb.UFOCSqlUtil;
import nc.util.hbbb.UfocLangLibUtil;
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
import nc.vo.hbbb.meetaccount.MeetResultBodyVO;
import nc.vo.hbbb.meetaccount.MeetResultHeadVO;
import nc.vo.hbbb.meetdata.AggMeetdataVO;
import nc.vo.hbbb.meetdata.MeetdataVO;
import nc.vo.hbbb.meetdata.MeetdatasubVO;
import nc.vo.hbbb.util.MD5;
import nc.vo.hbbb.vouch.VouchHeadVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.ml.NCLangRes4VoTransl;
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
import com.ufsoft.script.exception.CmdException;
import com.ufsoft.script.exception.CreateProxyException;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.script.expression.UfoExpr;
import com.ufsoft.script.extfunc.MeasFuncDriver;
import com.ufsoft.script.function.ExtFunc;
import com.ufsoft.script.function.UfoFunc;
import com.ufsoft.script.spreadsheet.UfoCalcEnv;

public class ContrastBO {

	private ContrastQryVO qryvo;
	
	private UFDate enddate;
	
	//公式解析器
	private UfoFormulaProxy parser;
	//计算环境
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
			//对账效率更改——预先设置计算环境，不要每次加载 jiaah
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
		
		//所有对账单位对组合
		String[] contrastorgs = this.getContrastOrgs(pk_hbrepstru, pk_contrastorg);
		if (null == contrastorgs || contrastorgs.length == 0) {
			return;
		}
		//并发处理，加动态锁
		String alone_id = HBAloneIDUtil.getAdjustVoucherAlone_id(this.getQryvo(), false);
		String pkLock = qryvo.getSchemevo().getPk_hbscheme() + alone_id;//方案+aloneid唯一确定
		qryvo.setPkLock(pkLock);
		try {
			BDPKLockUtil.lockString(LOCK_HB_KEY + pkLock);
		} catch (Exception e1) {
			if (e1 instanceof LockFailedException) {
				throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0141")/* @res "其他用户正在执行该操作,请稍后再试!" */);
			}
		}
		 clearContrastedData();
		startContrast(contrastorgs,alone_id);
	}
	
	@SuppressWarnings("unchecked")
	private void startContrast(String[] contrastorgs, String alone_id) throws BusinessException {
		// 批量删除历史对账数据 
		Map<String, String> mapContrastNote = new HashMap<String, String>();
		DXContrastVO[] dxvos = qryvo.getDxmodels();
		for (DXContrastVO vo : dxvos) {
//			mapContrastNote.putAll(ContrastResultBO.clearContrastedData(vo, qryvo));
			mapContrastNote.putAll(ContrastResultBO.setMeetNote(vo, qryvo));
		}
		
		//预加载组织相关数据
		HashSet<String> selfOrgSet = new HashSet<String>();//本方组织
		HashSet<String> oppOrgSet = new HashSet<String>();//对方组织
		HashSet<String> orgset = new HashSet<String>();//所有组织
		
		for (String str : contrastorgs) {
			String pk_self = str.trim().substring(0, 20);
			selfOrgSet.add(pk_self);
			String pk_other = str.trim().substring(20, 40);
			oppOrgSet.add(pk_other);
			orgset.add(pk_self);
			orgset.add(pk_other);
		}
		// 预加载所有对方组织的虚实单位对应，此处返回的为虚单位的虚实单位对，不是虚单位的不需要返回
		Map<String, String> oppEntityOrgs = HBRepStruUtil.getoppEntityOrgs(oppOrgSet.toArray(new String[0]), qryvo.getPk_hbrepstru());
		// 预加载对方单位是否是虚组织
		// @edit by zhoushuang at 2015-5-26,下午7:15:46
		// 是否是虚单位通过oppEntityOrgs即可获得，不需要查询了 oppEntityOrgs.keySet()即为所有虚单位
		Set<String> manageOrg = oppEntityOrgs.keySet();
		// Map<String, UFBoolean> mapIsManageOrg =HBRepStruUtil.getBooleanEntityOrgs(oppOrgSet.toArray(new String[0]), pk_hbrepstru);
		
		// 预加载当前期间的关键字map
		Map<String, String> offset = ContrastMeasPubDataCache.getInstance().getOffSets(qryvo.getSchemevo(), qryvo).get(qryvo.getPkLock());
		
		// 预加载组织对应的内部客商的pkMap
		Map<String, String> org_supplier_map = HBRepStruUtil.getOrgSuppliesMap(selfOrgSet.toArray(new String[0]));
		qryvo.setOrg_supplier_map(org_supplier_map);
		qryvo.setSelfOrgs(selfOrgSet);
		qryvo.setOppOrgs(oppOrgSet);
		qryvo.setOrgs(orgset);
		qryvo.setContrastorgs(contrastorgs);
		qryvo.setOppEntityOrgs(oppEntityOrgs);
		qryvo.setOffset(offset);
		qryvo.setMeaprojectcache(IntrMeasProjectCache.getSingleton().getInstance());
		// 预加载当前抵销方案的抵销规则:KEY:pk_dxrela
		Map<String, DXRelaDiffRuleVO> diffRuleMap = getAllDiffRuleMap();

		// 模板按照权益和非权益类进行分类
		Object[] relas = getDxrelaByType(dxvos);
		List<DXContrastVO> investDxRelas = (List<DXContrastVO>) relas[0];
		List<DXContrastVO> noInvestDxRelas = (List<DXContrastVO>) relas[1];
		
		// 批量解析公式,在bodyvo中预置解析的expr
		batchPareFormula(dxvos);

		// 逐个模板进行对账
		try {
			List<AggMeetRltHeadVO> resultLists = new ArrayList<AggMeetRltHeadVO>();
			String[] allContrastOrgs = contrastorgs;
			// 1、首先权益类的模板
			if (investDxRelas.size() > 0) {
				// 预加载控制类模板：对方组织是否是有效的组织
				Map<String, UFBoolean> mapIsVoidOrg = HBRepStruUtil
						.batchCheckVoidOrgWithManageOrg(
								orgset.toArray(new String[orgset.size()]),
								oppOrgSet.toArray(new String[oppOrgSet.size()]),
								manageOrg,  qryvo.getPk_hbrepstru());
				// 预加载控制类模板：根据模板类型判断本对方组织是否存在投资关系
				Map<String, UFBoolean> mapIsExistInvest = getMapIsExistInvest(selfOrgSet, oppOrgSet, qryvo.getSchemevo().getPk_investscheme());
		        //modify by zhaojian8 修改权益类模板含有大量INTR公式导致的效率问题
		        List<String> investOrg = new ArrayList<String>();
		        for(String str : contrastorgs){
		          String pk_other = str.trim().substring(20, 40);
		          if (mapIsExistInvest.get(str) != null
		              || manageOrg.contains(pk_other)) {// 是实组织且不存在投资关系直接continue
		            investOrg.add(str);
		          }
		        }
		        qryvo.setAllContrastOrgs(investOrg.toArray(new String[0]));
		        qryvo.setContrastorgs(investOrg.toArray(new String[0])); 
//		        qryvo.setAllContrastOrgs(allContrastOrgs);
//		        qryvo.setContrastorgs(allContrastOrgs);
				// 权益类模板对账
				for (DXContrastVO vo : investDxRelas) {
					for (String str : contrastorgs) {
						// 解决一套表中存在不同的动态区关键字导致对不出数据的问题
						ContrastMeasPubDataCache.getInstance().clearPk_dynKeyValue(qryvo.getPkLock());

						String pk_self = str.trim().substring(0, 20);
						String pk_other = str.trim().substring(20, 40);
						boolean isVoidOrg = mapIsVoidOrg.get(pk_other)
								.booleanValue();
						if (mapIsExistInvest.get(str) == null
								&& !manageOrg.contains(pk_other)) {// 是实组织且不存在投资关系直接continue
							continue;
						}
						// 增加股权投资方案参数
						RightAndInterestType righttype = RightAndInterestManager
								.createRightAndInterestType(vo, pk_self,
										pk_other, enddate,  qryvo.getPk_hbrepstru(), qryvo
												.getSchemevo()
												.getPk_investscheme());
						// 是控制类模板需要判断虚单位
						// 当前原则是若对账单位对里存在虚单位，则要求虚单位指定的实体单位也在该对帐单位对里,若指定的实体单位不在对帐单位对里则虚单位不参与执行对帐
						// 同时该实体单位就不再参与对帐，而对应的虚单位参与对账,若该实体单位存在的虚单位不在该对帐单位里，则参与执行对帐
						// 记住，只有该实体和虚单位都作为被投资方的时才这样判断
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
					// 清掉当前模板的qryVO中的中间结果
					removeQryResultMap(vo);
				}
				   qryvo.getResultMap().clear();
			        ContrastMeasPubDataCache.getInstance().clearInvestContrastCache(qryvo.getPkLock());

			}
			// 2、其次进行交易往来类模板的对账
			if (noInvestDxRelas.size() > 0) {
		    	  //zhaojian8 交易类简化对账对 begin
		    	  //单线程测试用，多线程需注释掉
		    	  List<String> simplifiedOrgList = null;
		    	  if(allContrastOrgs.length < 500){
		    		  simplifiedOrgList = new ArrayList<String>(allContrastOrgs.length);
		    		  simplifiedOrgList.addAll(Arrays.asList(allContrastOrgs));
		    	  }else{
		    		  List<DXRelationBodyVO> bodyVos = new ArrayList<DXRelationBodyVO>();
		        	  for(DXContrastVO vo : noInvestDxRelas){
		        		  DXRelationBodyVO[] vos = vo.getBodyvos();
		        		  for(DXRelationBodyVO bodyVo : vos){
		        			  bodyVos.add(bodyVo);
		        		  }
		        	  }
		        	  
		        	  String pk_contrastorg = qryvo.getContrastorg();
		        	  simplifiedOrgList = pretreatedContrastOrg(bodyVos.toArray(new DXRelationBodyVO[0]), pk_contrastorg, allContrastOrgs);
		    	  }
		    	  
		    	  qryvo.setAllContrastOrgs(simplifiedOrgList.toArray(new String[0]));

		    	  for (DXContrastVO vo : noInvestDxRelas) {
		    		  if(simplifiedOrgList == null || simplifiedOrgList.size() == 0){
		        		  continue;
		        	  }
		    		  String[] simplifiedContrastOrg = simplifiedOrgList.toArray(new String[0]);
		    		  qryvo.setContrastorgs(simplifiedContrastOrg);
		    		  //zhaojian8 end
					// 解决一套表中存在不同的动态区关键字导致对不出数据的问题
					ContrastMeasPubDataCache.getInstance().clearPk_dynKeyValue(qryvo.getPkLock());

					for (String str : simplifiedContrastOrg) {
						String pk_self = str.trim().substring(0, 20);
						String pk_other = str.trim().substring(20, 40);
						// 虚单位的时候不需要执行交易类的模板
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
					// 清掉当前模板的qryVO中的中间结果
					removeQryResultMap(vo);
				}
			}
			// 批量插入记录
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
			// 对账完释放
			ContrastMeasPubDataCache.getInstance().clear(qryvo.getPkLock());
			IntrMeasProjectCache.getSingleton().clear();
		}
	}
	/**
	 * 调度使用，pk增加随机数，否则多作业会重复
	 * @create by zhoushuang at 2015-7-4,上午11:25:55
	 *
	 * @return
	 * @throws BusinessException
	 */
	public int doContrastBySubContrastorgs() throws BusinessException { 
		//对账单位对组合
		String[] contrastorgs = qryvo.getContrastorgs();
		if (null == contrastorgs || contrastorgs.length == 0) {
			return -1;
		}
		//并发处理，加动态锁
		String alone_id = HBAloneIDUtil.getAdjustVoucherAlone_id(this.getQryvo(), false);
		String pkLock = qryvo.getSchemevo().getPk_hbscheme() + alone_id + IDMaker.makeID(5);//方案+aloneid唯一确定
		qryvo.setPkLock(pkLock);
		try {
			BDPKLockUtil.lockString(LOCK_HB_KEY + pkLock);
		} catch (Exception e1) {
			if (e1 instanceof LockFailedException) {
				throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0141")/* @res "其他用户正在执行该操作,请稍后再试!" */);
			}
		}
		startContrast(contrastorgs,alone_id);
		return 0;
	}
	  /**
	   * 预处理对账公司对，减少对账对
	   * @author zhaojian8
	   * @param bodyvo
	   * @param pk_contrastorg
	   * @param contrastorgs
	   * @throws BusinessException
	   * @return
	   * 
	   */
	  private List<String> pretreatedContrastOrg(DXRelationBodyVO[] bodyvo,String pk_contrastorg,String[] contrastorgs) throws BusinessException{
		  Set<String> contrastOrg = null;
		  Set<String> contrastOrgs = new HashSet<String>();
		  ContrastDMO contrastDMO = new ContrastDMO();
		  //zhaojian8 begin
		  Map<String,String> supplierOrg = qryvo.getOrg_supplier_map();
		  Map<String,String> orgSupplier = new HashMap<String,String>();
		  Map<String,Integer> existTable = new HashMap<String,Integer>();
		  for (Map.Entry<String, String> entry : supplierOrg.entrySet()) {  
			  orgSupplier.put(entry.getValue(), entry.getKey());
		  }  
		  //zhaojian8 end
		  //此处循环应该不会存在效率问题，两层for循环理论上总体循环次数不会超过10次
		  for(DXRelationBodyVO vo : bodyvo){
			  if(vo.getType().intValue() == IDXRelaConst.DIFF){
				  continue;
			  }
			  Set<String> projectCodes = getProjectCodeByFormula(vo.getExpr());
			  //Added by sunzeg 2017.11.7 处理多个公式四则运算 的情况_begin
			  //String projectcode = (formula.split("/")[1]).split("'")[0];

			  //      String[] partsOfFormula = formula.split("/");
			  //      for(String part : partsOfFormula){
			  //        String[] pieces = part.split("\',");
			  //        if(pieces.length > 1){
			  //modified by zhaojian8 修改匹配逻辑
			  Iterator<String> it = projectCodes.iterator();
			  while(it.hasNext()){

				  //合并报表项目必须是被/和'/包围的，如：INTR('项目1/0001',0)+INTR('项目2/0002',0);INTR('项目1/0001',0)/INTR('项目2/0002',0)
				  String projectcode = it.next();
				  //TODO 需要修改接口，在两层循环里面查数据库太low了
				  MeasureReportVO result = HBProjectBOUtil.getProjectMeasVOByCode(qryvo.getSchemevo().getPk_hbscheme(),pk_contrastorg, projectcode, true);
				  //zhaojian8 20180207 异常判定
				  if(result == null){
					  throw new BusinessException("当前合并方案中不存在引用合并报表项目"+ projectcode +" 的报表");
				  }
				  MeasureVO measure = result.getMeasVO();
				  String measTable = measure.getDbtable();
				  String measColumn = measure.getDbcolumn();
				  String keyCombPk = measure.getKeyCombPK();

				  String countSql = " SELECT COUNT(ALONE_ID) FROM " + measTable.toUpperCase() + " WHERE " + measColumn.toUpperCase() + " IS NOT NULL AND " + measColumn.toUpperCase() +" <> 0";
				  String sql = " SELECT ALONE_ID FROM " + measTable.toUpperCase() + " WHERE " + measColumn.toUpperCase() + " IS NOT NULL AND " + measColumn.toUpperCase() +" <> 0";
				  Object executeQuery = new BaseDAO().executeQuery(countSql, new ColumnProcessor());
				  StringBuffer sql1 = new StringBuffer();
				  sql1.append(" SELECT DISTINCT ");
				  Integer num = (Integer)executeQuery;
				  if(num != null && num > 0){
					  KeyGroupVO keyGroup =UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(keyCombPk);
					  //并发处理，加动态锁
					  String alone_id = HBAloneIDUtil.getAdjustVoucherAlone_id(qryvo, false);
					  //方案+aloneid唯一确定
					  String schemeAloneId = qryvo.getSchemevo().getPk_hbscheme() + alone_id;
					  String pk_dynkeyword = ContrastMeasPubDataCache.getInstance().getPk_dynKeyValue(keyGroup,qryvo.getSchemevo(), schemeAloneId).get(schemeAloneId);
					  int i = 1;
					  int j = 1;
					  int k = 1;
					  //是否为对方单位关键字
					  boolean isDICCORP = true;
					  KeyVO[] keyVO = keyGroup.getKeys();
					  for(KeyVO key : keyVO){
						  if(key.getPk_keyword().equals(KeyVO.CORP_PK) || key.getPk_keyword().equals(pk_dynkeyword)){
							  if(i > 1){
								  sql1.append(" , ");
							  }
							  if(!key.getPk_keyword().equals(KeyVO.CORP_PK) && !key.getPk_keyword().equals(KeyVO.DIC_CORP_PK)){
								  isDICCORP = false;
							  }
							  sql1.append(" KEYWORD").append(j);
							  i++;
						  }
						  if(key.getType() == 3 || key.getType() == 4){
							  k = j;

						  }
						  j++;
					  }
					  sql1.append(" FROM ").append(" IUFO_MEASPUB_").append(keyGroup.getKeyGroupPK().substring(keyGroup.getKeyGroupPK().length()-4, keyGroup.getKeyGroupPK().length()));
					  sql1.append(" WHERE ALONE_ID IN (").append(sql).append(" ) ") ;
					  sql1.append(" AND KEYWORD").append(k);
					  sql1.append(" = '").append(qryvo.getKeymap().get(keyVO[k-1].getPk_keyword())).append("'");
					  try {
						  contrastOrg = contrastDMO.getcontrastOrg(sql1.toString(),isDICCORP,orgSupplier);
					  } catch (SQLException e) {
						  throw new BusinessException(e.getMessage());
					  }
				  }
				  if(contrastOrg != null){
					  contrastOrgs.addAll(contrastOrg);
				  }
				  //        }
			  }     
			  //Added by sunzeg 2017.11.7 处理多个公式四则运算 的情况_end
		  }
		  List<String> list = new ArrayList<String>();
		  for(String str : contrastorgs){
			  if(contrastOrgs.contains(str) && !list.contains(str)){
				  list.add(str);
			  }
		  } 
		  return list;
	  }
	  
	  /**
	   * 根据公式获取含有INTR的合并报表项目
	   * @author zhaojian8
	   * @param expr 
	   * @return
	   */
	  private Set<String> getProjectCodeByFormula(UfoExpr expr){
	    Set<String> rtn = new HashSet<String>();
	    UfoEElement[] elements = expr.getElements();
	    for(UfoEElement element : elements){
	      Object obj = null;
	      if(element.getType() == 1){
	        obj = element.getObj();
	        if(obj instanceof UfoFunc){
	          UfoFunc func = (UfoFunc)obj;
	          List<UfoExpr> listParams = null;
	          if(func instanceof UfoFunc && func.getParams() != null && func.getParams().size() > 0){
	            if(func.getParams().get(0) instanceof UfoExpr){
	              listParams = func.getParams();
	              for(UfoExpr param : listParams){
	                if(param.toString().toUpperCase().indexOf("INTR") >= 0){
	                  if(param.getElementLength() == 1){
	                    String formula = param.toString();
	                    String[] partsOfFormula = formula.split("/");
	                    for(String part : partsOfFormula){
	                      String[] pieces = part.split("\',");
	                      if(pieces.length > 1){
	                        rtn.add(pieces[0]);
	                      }
	                    }
	                  }else{
	                    rtn.addAll(getProjectCodeByFormula(param));
	                  }
	                }
	              }
	            }else if(func instanceof ExtFunc){
	              if(func.toString().toUpperCase().indexOf("INTR") >= 0){
	                String param = (String)func.getParams().get(0);
	                String[] partsOfFormula = param.split("/");
	                rtn.add(partsOfFormula[1]);
	              }
	            }
	          }
	        }
	      }
	    }
	    return rtn;
	  }
	  
	/**
	 * 清除qryVO中的中间级对账结果
	 * @create by jiaah at 2013-8-8,下午7:50:52
	 * @param measureCodes
	 * @throws BusinessException 
	 */
	@SuppressWarnings("rawtypes")
	private void removeQryResultMap(DXContrastVO dxContrastVO) throws BusinessException{
		if(qryvo == null)
			return;
		String pk_hbscheme = qryvo.getSchemevo().getPk_hbscheme();
		//预加载的映射关系
		Map<String, MeasureReportVO> prjoectMeasMapCache = new HashMap<String, MeasureReportVO>();
		IntrMeasProjectCache cacheinstance = qryvo.getIntrMeaProjectinstance();
		if(cacheinstance != null){
			prjoectMeasMapCache = cacheinstance.getMeasRepVOs();
		}
		//返回需要清除的指标code
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
				// 有类型为short的情况,目前直接忽略
                if (!(elements[i].getObj() instanceof ExtFunc)) {
                    continue;
                }
                ExtFunc tmpformula = (ExtFunc) elements[i].getObj();
                //key函数不考虑
                if(tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.KEYFUNC)){
                	continue;
                }
                List params = tmpformula.getParams();
                String projectcode = String.valueOf( params.get(0));//返回项目编码
                if (StringUtil.isEmptyWithTrim(projectcode)) {
                    continue;
                }
                String[] splitprojectcode = projectcode.split("/");
                
                //返回对应的指标编码
                if (splitprojectcode != null && splitprojectcode.length > 0) {
                	String key = pk_hbscheme + splitprojectcode[1];
            		MeasureReportVO measrepvo = prjoectMeasMapCache.get(key);
            		if(measrepvo == null){
            			try {
            				//有些场景，并未填充env就对账完毕了，如权益类模板，如果不存在投资关系的时候 env应该是默认值未设置其他参数
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
		//清除中间结果
		Map<String, Map<String,UFDouble>> resultMap = qryvo.getResultMap();
		if(resultMap != null && resultMap.size() > 0 && measureCodeLst.size() > 0){
			for(String s : measureCodeLst){
				resultMap.remove(s);
			}
		}
	}

	/**
	 * 预加载本对方是否存在投资关系
	 * @create by jiaah at 2013-8-8,下午7:39:36
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
	 * 抵销模板按照权益非权益进行分类
	 * @create by jiaah at 2013-8-8,下午7:36:44
	 * @param dxvos
	 * @return
	 * @throws BusinessException
	 */
	private Object[] getDxrelaByType(DXContrastVO[] dxvos) throws BusinessException{
		//权益类模板和非权益类模板
		List<DXContrastVO> investDxRelas = new ArrayList<DXContrastVO>();
		List<DXContrastVO> noInvestDxRelas = new ArrayList<DXContrastVO>();
		for (DXContrastVO dxrelaVO : dxvos){
			if(dxrelaVO.getHeadvo().getType().equals(IDXRelaConst.RIGHTANDRIGHT_INVEST) || dxrelaVO.getHeadvo().getType().equals(IDXRelaConst.RIGHTANDRIGHT_UNINVEST)
					|| dxrelaVO.getHeadvo().getType().equals(IDXRelaConst.RIGHTANDRIGHT_ALLOWNERINVEST)){
				investDxRelas.add(dxrelaVO);
			}
			else{
				noInvestDxRelas.add(dxrelaVO);
				//存在则填充内部交易对账规则
				if(dxrelaVO.getHeadvo().getPk_contrastrule() != null){
					dxrelaVO.setContrastRuleVo(GLContrastProxy.getRemoteContrastRule().findByPrimaryKey(dxrelaVO.getHeadvo().getPk_contrastrule()));
				}
			}
		}
		return new Object[]{investDxRelas,noInvestDxRelas};
	}
	
	
	/**
	 * 预加载所有的差额规则：
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
					
					//返回所有的差额规则主键对应vo
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
		//北京首创：差额项导致多位小数问题
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
	
	
	

	private MeetdatasubVO[] genMeetdatasubvos(UfoCalcEnv env,DXRelationBodyVO subvo, boolean bself, String pk_self, String pk_other, DXContrastVO contrastVO) throws BusinessException {
		List<MeetdatasubVO> rtns = new ArrayList<MeetdatasubVO>();
		

		try {
			MeetdatasubVO subvo1 = new MeetdatasubVO();
			UFDouble data = new UFDouble(ContrastFuncBO.callFunc(env,this.getQryvo(), bself, pk_self, pk_other, subvo, contrastVO,subvo1));
			if(env.getExEnv(IDxFunctionConst.INTRBYKEY_RESULT_KEY)!=null){
				Map<String, UFDouble> values = (Map)env.getExEnv(IDxFunctionConst.INTRBYKEY_RESULT_KEY);
				env.getExEnv().remove(IDxFunctionConst.INTRBYKEY_RESULT_KEY);
				if(values.isEmpty()){
					subvo1.setPk_measure(subvo.getPk_measure());
					//四舍五入保留两位小数
					subvo1.setAmount(new UFDouble());
					subvo1.setDirection(subvo.getType());
					boolean self = DxFuncProxy.bSelf(env,subvo);
					if (self) {
						subvo1.setBself(UFBoolean.TRUE);
					} else {
						subvo1.setBself(UFBoolean.FALSE);
					}
					
					rtns.add(subvo1);
					return rtns.toArray(new MeetdatasubVO[0]);
				}
				for(String name:values.keySet()){
					 
					MeetdatasubVO svo = new MeetdatasubVO();
					svo.setMeetNode(name);
					
					svo.setPk_measure(subvo.getPk_measure());
					//四舍五入保留两位小数
					svo.setAmount(new UFDouble(NumberFormatUtil.Number2(values.get(name).doubleValue())));
					svo.setDirection(subvo.getType());
					boolean self = DxFuncProxy.bSelf(env,subvo);
					if (self) {
						svo.setBself(UFBoolean.TRUE);
					} else {
						svo.setBself(UFBoolean.FALSE);
					}
					rtns.add(svo);
					 
				
				}
				
				
				
			}else{
				
				subvo1.setPk_measure(subvo.getPk_measure());
				//四舍五入保留两位小数
				subvo1.setAmount(new UFDouble(NumberFormatUtil.Number2(data.doubleValue())));
				subvo1.setDirection(subvo.getType());
				boolean self = DxFuncProxy.bSelf(env,subvo);
				if (self) {
					subvo1.setBself(UFBoolean.TRUE);
				} else {
					subvo1.setBself(UFBoolean.FALSE);
				}
				rtns.add(subvo1);
				return rtns.toArray(new MeetdatasubVO[0]);
			}
			
			
		} catch (Exception e) {
			this.error(e);
		}
		return 	rtns.toArray(new MeetdatasubVO[0]);
	}
	
	
	
//	private MeetdatasubVO[] genMeetdatasubvos(UfoCalcEnv env,DXRelationBodyVO subvo, boolean bself, String pk_self, String pk_other, DXContrastVO contrastVO) throws BusinessException {
//		List<MeetdatasubVO> rtn = new ArrayList<MeetdatasubVO>();
//
//		try {
//			MeetdatasubVO subvo1 = new MeetdatasubVO();
//			subvo1.setPk_measure(subvo.getPk_measure());
//			UFDouble data = new UFDouble(ContrastFuncBO.callFunc(env,this.getQryvo(), bself, pk_self, pk_other, subvo, contrastVO,subvo1));
//			//四舍五入保留两位小数
//			subvo1.setAmount(new UFDouble(NumberFormatUtil.Number2(data.doubleValue())));
//			subvo1.setDirection(subvo.getType());
//			boolean self = DxFuncProxy.bSelf(env,subvo);
//			if (self) {
//				subvo1.setBself(UFBoolean.TRUE);
//			} else {
//				subvo1.setBself(UFBoolean.FALSE);
//			}
//			 
//		} catch (Exception e) {
//			this.error(e);
//		}
//		return rtn;
//	}

//	private MeetdatasubVO genMeetdatasubvo(UfoCalcEnv env,DXRelationBodyVO subvo, boolean bself, String pk_self, String pk_other, DXContrastVO contrastVO) throws BusinessException {
//		MeetdatasubVO subvo1 = new MeetdatasubVO();
//		subvo1.setPk_measure(subvo.getPk_measure());
//
//		try {
//			UFDouble data = new UFDouble(ContrastFuncBO.callFunc(env,this.getQryvo(), bself, pk_self, pk_other, subvo, contrastVO,subvo1));
//			//四舍五入保留两位小数
//			subvo1.setAmount(new UFDouble(NumberFormatUtil.Number2(data.doubleValue())));
//			subvo1.setDirection(subvo.getType());
//			boolean self = DxFuncProxy.bSelf(env,subvo);
//			if (self) {
//				subvo1.setBself(UFBoolean.TRUE);
//			} else {
//				subvo1.setBself(UFBoolean.FALSE);
//			}
//		} catch (Exception e) {
//			this.error(e);
//		}
//		return subvo1;
//	}
	


	public static String genMd5Key(DXContrastVO vo, String pk_self, String pk_other, ContrastQryVO new_qryvo) {
		StringBuilder content = new StringBuilder();
		String result = "";
		// content.append(vo.getAmounttype());
		// content.append(vo.isIsquantity() ? "Y" :"N");
		content.append(vo.getHeadvo().getPk_dxrela_head());
		content.append(new_qryvo.getSchemevo().getPk_hbscheme());
		content.append(pk_self);
		content.append(pk_other);

		// 开始设置关键字
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
	 * @param map：第一个String[本方+对方+aloneid+hbid+dxrelaid+pk_measure]；第二个String为对账说明meetnote
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
		//modified by jiaah 减少sql使用
//		result.setAloneid(HBAloneIDUtil.getAdjustVoucherAlone_id(this.getQryvo(), false));
		result.setAloneid(alone_id);
		// 开始设置关键字
		result.setPk_keygroup(this.getQryvo().getSchemevo().getPk_keygroup());

		DXRelationBodyVO[] subvos = vo.getBodyvos();
		ArrayList<MeetdatasubVO> meetdatasublist = new ArrayList<MeetdatasubVO>();
		DXRelationBodyVO difsubvo = null;
		boolean bZero = true;
		for (DXRelationBodyVO subvo : subvos) {
			// 若是混对的话,则走分录上的金额性质
			// 若是差额项目,则先不进行设值
			if (subvo.getType().intValue() == IDXRelaConst.DIFF) {
				difsubvo = subvo;
			} else {
				env.getExEnv().remove(IDxFunctionConst.INTRBYKEY_RESULT_KEY);
				MeetdatasubVO[] subVOs = genMeetdatasubvos(env,subvo, bself, pk_self, pk_other, vo);
				
				for(MeetdatasubVO subVO:subVOs){
				if(!subVO.getAmount().equals(new UFDouble().ZERO_DBL))
					bZero = false;
				meetdatasublist.add(subVO);
				}
			}
		}
		
		//如果所有都是0:不再生成对账记录--modified by jiaah
		if(bZero == true){
			return null;
		}
		
		// 进行差额项目处理
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

		// 开始进行对账
		if (null != difsubvo) { // 则不再走差异规则
			GenContrastResultBO.genContrastResultWithDifProject(aggVO,map,resultList,diffRuleMap);
		} else { // 开始走差异规则
			new GenContrastResultBO().genContrastResult(aggVO, this.getQryvo().getSchemevo(), vo,map,resultList,diffRuleMap);
		}

		return aggVO;
	}
	
	
	/**
	 * 确定对账的公司对
	 * @create by jiaah at 2011-12-30,上午10:32:32
	 * @param pk_hbrepstru 报表合并体系版本主键
	 * @param 当前对账组织
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public static String[] getContrastOrgs(String pk_hbrepstru, String pk_contrastorg) throws BusinessException {
		String[] result = null;
		String innercode = getInnerCode(pk_hbrepstru, pk_contrastorg);
		if (null == innercode || innercode.trim().length() == 0) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0452")/* @res "当前对账公司没有找到inner code" */);
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
			throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0451")/*@res "查询报表组织的时候，未能取得报表组织VO"*/);
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
	 * 判断是否启用调度调度(给定单位的下级总数超过200个即启用)
	 * @create by zhoushuang at 2015-7-4,上午10:50:52
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
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0452")/* @res "当前对账公司没有找到inner code" */);
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
	 /**
	   * 按模板删除对账记录
	   * 删除in语句，没有用，还浪费时间
	   * @edit by zhoushuang at 2015-6-1,下午2:57:45
	   * @edit by zhaojian8 at 2016-11-24 13:44:17 
	   * @param vo
	   * @param selfOrgs
	   * @param oppOrgs
	   * @param qryvo
	   * @return
	   * @throws BusinessException
	   */
	  @SuppressWarnings({ "rawtypes", "unchecked" })
	  public void clearContrastedData() throws BusinessException{
	    
	    BaseDAO dmo = null;
	    SQLParameter params = null;
	    for(DXContrastVO vo : qryvo.getDxmodels()){
	      StringBuilder content = new StringBuilder();
	      dmo = new BaseDAO();
	      content.append(" pk_hbscheme = ? ");
	      content.append(" AND pk_dxrelation = ? ");
	      content.append(" AND alone_id = ? ");
	      String headWhere = content.toString();
	      content.append(" AND isnull(dataorigin,'~')<>'~' ");
	      
	      String disDataWhere = content.toString();
	      params = new SQLParameter();
	      params.addParam(qryvo.getSchemevo().getPk_hbscheme());
	      params.addParam(vo.getHeadvo().getPk_dxrela_head());
	      String aloneid = HBAloneIDUtil.getAdjustVoucherAlone_id(qryvo, true);
	      params.addParam(aloneid);
	      
	      //校验对账记录是否是分布式传过来的数据
	      Collection disData = dmo.retrieveByClause(MeetResultHeadVO.class, disDataWhere, params);
	      if(disData != null && disData.size()>0){
	        throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0051"));
	      }
	      
	      content = new StringBuilder();
	      content.append("  alone_id = ? ");
	      content.append("  AND pk_hbscheme = ? ");
	      content.append("  AND pk_dxrela = ? ");
	      content.append("  AND dr = 0  ");
	      content.append("  AND (checker <>'~' ");
	      content.append("  or isnull(dataorigin,'~')<>'~' )");//已审核的分录或者数据来源有值的情况，都不能重新执行对账
	      
	      content.append("  AND vouch_type =  ").append(IVouchType.TYPE_AUTO_ENTRY);
	      
	      SQLParameter vouchParams = new SQLParameter();
	      vouchParams.addParam(HBAloneIDUtil.getAdjustVoucherAlone_id(qryvo, true));
	      vouchParams.addParam(qryvo.getSchemevo().getPk_hbscheme());
	      vouchParams.addParam(vo.getHeadvo().getPk_dxrela_head());

	      // 查询自动生成的抵销分录是否已经存在审核，是否是来自于分布式的数据
	      Collection retrieveByClause = dmo.retrieveByClause(VouchHeadVO.class, content.toString(), vouchParams);
	      if (retrieveByClause != null && retrieveByClause.size() > 0) {
	        VouchHeadVO headvo  = (VouchHeadVO)retrieveByClause.toArray(new VouchHeadVO[0])[0];
	        if(headvo.getDataorigin() != null){
	          throw new UFOCUnThrowableException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0051"));
	        }
	        else{
	          throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0021")/* @当前执行条件下,抵消模板为 */+ "'" + UfocLangLibUtil.toCurrentLang(vo.getHeadvo()) + "'"
	              + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0022")/* @已生成凭证且已审核! 请取消审核再执行! */);
	        }
	      }
	      
	      content = new StringBuilder();

	      content.append(" exists ( ");
	      content.append("        SELECT pk_totalinfo ");
	      content.append("             FROM iufo_meetdata_head ");
	      content.append("        where pk_totalinfo = iufo_meetdata_body.details");
	      content.append("              AND pk_hbscheme = ? ");
	      content.append("              AND pk_dxrelation = ? ");
	      content.append("              AND alone_id = ? ");
	      content.append("             ) ");
	      
	      String bodywhere = content.toString();
	      
	      dmo.deleteByClause(MeetResultBodyVO.class, bodywhere, params);
	      dmo.deleteByClause(MeetResultHeadVO.class, headWhere, params);
	    }
	    
	  }
	

}