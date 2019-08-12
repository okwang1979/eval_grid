package nc.bs.hbbb.contrast.batch;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.bs.hbbb.contrast.ContrastDMO;
import nc.bs.hbbb.dxmodelfunction.HBProjectBOUtil;
import nc.bs.uap.lock.PKLock;
import nc.itf.hbbb.contrast.ContrastMeasPubDataCache;
import nc.itf.hbbb.contrast.IContrast;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.itf.iufo.data.IMeasurePubDataQuerySrv;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.pub.iufo.exception.UFOSrvException;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.pub.HBPubItfService;
import nc.util.iufo.pub.IDMaker;
import nc.vo.bi.clusterscheduler.IJob;
import nc.vo.bi.clusterscheduler.ITask;
import nc.vo.glcom.tools.GLContrastProxy;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.dxfunction.project.MeasureReportVO;
import nc.vo.hbbb.dxrelation.DXContrastVO;
import nc.vo.hbbb.dxrelation.DXRelationBodyVO;
import nc.vo.hbbb.dxrelation.IDXRelaConst;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.pub.BusinessException;

import com.ufida.iufo.pub.tools.AppDebug;

/**
 * 
 * 合并报表对账执行异步执行作业
 * @author zhoushuang
 * @created at 2015-7-3,下午3:20:19
 *
 */
public class ContrastExecJob implements IJob,Serializable {

	private static final long serialVersionUID = 2286998552337332171L;

	private ContrastQryVO qryvo = null;
	private String businessID = null;
	//每个作业对账对的数量
	private int CONTRASTORGNUM = 2000;

	public ContrastExecJob(String businessID){
		this.businessID = businessID;
	}

    public ContrastExecJob(ContrastQryVO qryvo){
		this.qryvo = qryvo;
		this.businessID = IDMaker.makeID(50);
	}

	@Override
	public String getBuinessID() {
		return businessID;
	}

	@Override
	public String getJobName() {
		return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830010-0015")/*@res "合并报表-自动对账"*/;
	}

	@Override
	public String getModuleClzName() {
		return ContrastExecModule.class.getName();
	}

	@Override
	public ITask[] split() {
		
		List<ITask> lstTask = new ArrayList<ITask>();

		String pk_hbrepstru = qryvo.getPk_hbrepstru();
		String pk_contrastorg = qryvo.getContrastorg();
		if (pk_hbrepstru != null && pk_contrastorg != null) {
			try {
				// 所有对账单位对组合
				String[] allcontrastorgs = HBPubItfService.getRemoteContrastSrv().getContrastOrgs(pk_hbrepstru, pk_contrastorg);
				//Modified by sunzeg 2017.6.12 减少对账对，只取有数据的对账对_begin
				String[] simlifiedContrastOrgs = getSimplifiedOrgs(allcontrastorgs);
				qryvo.setContrastorgs(simlifiedContrastOrgs);
				qryvo.setAllContrastOrgs(simlifiedContrastOrgs);
				//int count = allcontrastorgs.length / CONTRASTORGNUM;
				int count = simlifiedContrastOrgs.length / CONTRASTORGNUM;
				List<String[]> subContrastorgs = new ArrayList<String[]>();
				if (count > 0) {
					for (int i = 1; i <= count; i++) {
						//zhaojian8 分组数据少一条
//						subContrastorgs.add(Arrays.copyOfRange(allcontrastorgs,(i - 1) * CONTRASTORGNUM, i * CONTRASTORGNUM - 1));
						subContrastorgs.add(Arrays.copyOfRange(simlifiedContrastOrgs,(i - 1) * CONTRASTORGNUM, i * CONTRASTORGNUM));
					}
//					subContrastorgs.add(Arrays.copyOfRange(allcontrastorgs, count * CONTRASTORGNUM, allcontrastorgs.length - 1));
					subContrastorgs.add(Arrays.copyOfRange(simlifiedContrastOrgs, count * CONTRASTORGNUM, simlifiedContrastOrgs.length));
				} else {
					subContrastorgs.add(simlifiedContrastOrgs);
				}
				//Modified by sunzeg 2017.6.12 减少对账对，只取有数据的对账对_end
				for (String[] pk_orgs : subContrastorgs) {
					ContrastQryVO subQryVO = (ContrastQryVO) qryvo.clone();
					subQryVO.setContrastorgs(pk_orgs);
					ContrastExecTask task = new ContrastExecTask(subQryVO);
					lstTask.add(task);
				}
			} catch (BusinessException e) {
				AppDebug.debug(e.getMessage(), e);
			}
		}
		return lstTask.toArray(new ITask[0]);
	}

	@Override
	public boolean uniqueRunning() {
		return false;
	}
	
	/**
	 * 简化对账对，只取有数据的对账对
	 * @author sunzeg
	 * @param contrastorgs
	 * @return
	 * @throws BusinessException
	 */
	private String[] getSimplifiedOrgs(String[] contrastorgs) throws BusinessException{
		List<String> list = null;
		String pk_contrastorg = qryvo.getContrastorg(); 
		if(pk_contrastorg == null)
			return null;
		DXContrastVO[] dxvos = qryvo.getDxmodels();
		for (DXContrastVO dxmodel : dxvos) {
			DXRelationBodyVO[] bodyvos = ((nc.itf.hbbb.dxrelation.IDXRelationQrySrv) NCLocator.getInstance().lookup(nc.itf.hbbb.dxrelation.IDXRelationQrySrv.class.getName()))
					.queryDXFormulas(dxmodel.getHeadvo().getPk_dxrela_head());
			dxmodel.setBodyvos(bodyvos);
		}
		Object[] relas = getDxrelaByType(dxvos);
		List<DXContrastVO> noInvestDxRelas = (List<DXContrastVO>) relas[1];
		//预加载组织相关数据
		HashSet<String> selfOrgSet = new HashSet<String>();//本方组织
		for (String str : contrastorgs) {
			String pk_self = str.trim().substring(0, 20);
			selfOrgSet.add(pk_self);
		}
		//预加载组织对应的内部客商的pkMap
		Map<String, String> org_supplier_map = HBRepStruUtil.getOrgSuppliesMap(selfOrgSet.toArray(new String[0]));
		qryvo.setOrg_supplier_map(org_supplier_map);
		if(noInvestDxRelas.size() > 0){
			for(DXContrastVO vo: noInvestDxRelas){
				list = pretreatedContrastOrg(vo.getBodyvos(), pk_contrastorg, contrastorgs);
			}
		}
		return list.toArray(new String[0]);	
	}
	
	/**
	 * 预处理对账公司对，减少对账对
	 * @author zhaojian8
	 * @param bodyvo
	 * @param pk_contrastorg
	 * @param contrastorgs
	 * @return
	 * @throws BusinessException
	 */
	private List<String> pretreatedContrastOrg(DXRelationBodyVO[] bodyvo,String pk_contrastorg,String[] contrastorgs) throws BusinessException{
		Set<String> contrastOrg = null;
		Set<String> contrastOrgs = new HashSet<String>();
		//zhaojian8 begin
		Map<String,String> supplierOrg = qryvo.getOrg_supplier_map();
		Map<String,String> orgSupplier = new HashMap<String,String>();
		for (Map.Entry<String, String> entry : supplierOrg.entrySet()) {  
			orgSupplier.put(entry.getValue(), entry.getKey());
		}  
		//zhaojian8 end
		for(DXRelationBodyVO vo : bodyvo){
			if(vo.getType().intValue() == IDXRelaConst.DIFF){
				continue;
			}
			String formula = vo.getFormula();
			String projectcode = (formula.split("/")[1]).split("'")[0];
			MeasureReportVO result = HBProjectBOUtil.getProjectMeasVOByCode(qryvo.getSchemevo().getPk_hbscheme(),pk_contrastorg, projectcode, true);
			MeasureVO measure = result.getMeasVO();
			String measTable = measure.getDbtable();
			String measColumn = measure.getDbcolumn();
			String keyCombPk = measure.getKeyCombPK();
			
			String sql = " SELECT ALONE_ID FROM " + measTable.toUpperCase() + " WHERE " + measColumn.toUpperCase() + " IS NOT NULL AND " + measColumn.toUpperCase() +" <> 0 ";
			Object executeQuery = new BaseDAO().executeQuery(sql, new ColumnProcessor());
			StringBuffer sql1 = new StringBuffer();
			sql1.append(" SELECT DISTINCT ");
			if(executeQuery != null){
				String aloneId = (String)executeQuery;
				MeasurePubDataVO measPubData =  NCLocator.getInstance().lookup(IMeasurePubDataQuerySrv.class).findByAloneID(keyCombPk, aloneId);
				KeyGroupVO keyGroup = measPubData.getKeyGroup();
				//并发处理，加动态锁
				String alone_id = HBAloneIDUtil.getAdjustVoucherAlone_id(qryvo, false);
				//方案+aloneid唯一确定
				String schemeAloneId = qryvo.getSchemevo().getPk_hbscheme() + alone_id;
				String pk_dynkeyword = ContrastMeasPubDataCache.getInstance().getPk_dynKeyValue(keyGroup,qryvo.getSchemevo(), schemeAloneId).get(schemeAloneId);
				int i = 1;
				int j = 1;
				int k = 1;
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
				contrastOrg = NCLocator.getInstance().lookup(IContrast.class).getcontrastOrg(sql1.toString(),isDICCORP,orgSupplier);
			}
			contrastOrgs.addAll(contrastOrg);
		}
		List<String> list = new ArrayList<String>();
		for(String str : contrastorgs){
			if(contrastOrgs.contains(str)){
				list.add(str);
			}
		}	
		return list;
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

}