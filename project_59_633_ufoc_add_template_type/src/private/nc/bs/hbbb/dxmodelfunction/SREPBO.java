package nc.bs.hbbb.dxmodelfunction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
import nc.itf.hbbb.constants.HBFmlConst;
import nc.itf.hbbb.vouch.constants.IVouchType;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BaseProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.HBVersionUtil;
import nc.util.hbbb.MeasureDataProxy;
import nc.util.hbbb.OffsetHanlder;
import nc.util.hbbb.UFOCSqlUtil;
import nc.util.hbbb.pub.HBPubItfService;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.vo.hbbb.account.HBAccChartVO;
import nc.vo.hbbb.account.MeasureAccountMapVO;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.contrast.IContrastConst;
import nc.vo.hbbb.dxfunction.project.MeasureReportVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.verctrl.IDataVersionConsts;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.vorg.ReportCombineStruMemberVersionVO;

import com.ufsoft.script.base.ICalcEnv;

/**
 * SREP ����ȡ������
 *
 * @author liyra
 * @date 20110310
 *
 */
public class SREPBO {

	public SREPBO() {
		super();
	}

	private class QueryResultProcessor extends BaseProcessor {
		private static final long serialVersionUID = 803213224379610333L;

		@Override
		public Object processResultSet(ResultSet rs) throws SQLException {
			List<MeasureAccountMapVO> lstResult = new ArrayList<MeasureAccountMapVO>();
			while (rs.next()) {
				MeasureAccountMapVO resultVo = new MeasureAccountMapVO();
				resultVo.setPk_report(rs.getString(1));
				resultVo.setPk_measure(rs.getString(2));
				lstResult.add(resultVo);
			}
			return lstResult;
		}
	}

	@SuppressWarnings("unchecked")
	public MeasureAccountMapVO getmeasureMapPk(String accchartcode, String accpropcode) throws BusinessException {
		MeasureAccountMapVO result = null;
		SQLParameter param = new SQLParameter();
		param.addParam(accchartcode);
		BaseDAO dao = new BaseDAO();
		try {
			String[] fields = new String[] {
				HBAccChartVO.PK_ACCCHART
			};
			Collection<HBAccChartVO> collect = dao.retrieveByClause(HBAccChartVO.class, " code=?  ", fields, param);
			HBAccChartVO[] vos = (HBAccChartVO[]) collect.toArray(new HBAccChartVO[0]);
			String pk_accchart = vos[0].getPk_accchart();
			StringBuilder content = new StringBuilder();
			content.append("SELECT accmap.pk_report, accmap.pk_measure ");
			content.append("  FROM iufo_hbaccountprop prop INNER JOIN iufo_hbaccount acc ");
			content.append("       ON prop.pk_account = acc.pk_account AND acc.pk_accchart = ? ");
			content.append("       INNER JOIN iufo_measaccmap accmap ON accmap.pk_hbaccount = ");
			content.append("                                                               prop.pk_accprop ");
			content.append(" WHERE prop.innercode = ? ");

			SQLParameter sparam = new SQLParameter();
			sparam.addParam(pk_accchart);
			sparam.addParam(accpropcode);
			List<MeasureAccountMapVO> lstResult = (List<MeasureAccountMapVO>) dao.executeQuery(content.toString(), sparam, new QueryResultProcessor());
			MeasureAccountMapVO[] vos1 = new MeasureAccountMapVO[lstResult.size()];
			lstResult.toArray(vos1);
			if (null != vos1 && vos1.length > 0) {
				result = vos1[0];
			}
		} catch (DAOException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			throw new BusinessException(e);
		}

		return result;
	}

	public double getSREP(String projectcode, int isself, int offset, ICalcEnv env) throws BusinessException {
		
		String projectCacheKey = "nc.bs.hbbb.dxmodelfunction.getSREP";
		MeasureReportVO measrepvo = null;
		if(env.getExEnv(projectCacheKey)!=null){
			Map<String, MeasureReportVO> caches = (Map)env.getExEnv(projectCacheKey);
			if(caches.get(projectcode)!=null){
				measrepvo = caches.get(projectcode);
			}else{
				measrepvo  = HBProjectBOUtil.getProjectMeasVO(env, projectcode, false);
				caches.put(projectcode, measrepvo);
			}
		}else{
			Map<String, MeasureReportVO> caches = new HashMap<String, MeasureReportVO>();
			measrepvo  = HBProjectBOUtil.getProjectMeasVO(env, projectcode, false);
			caches.put(projectcode, measrepvo);
			env.setExEnv(projectCacheKey, caches);
		}

		

		if (measrepvo == null)
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0114")/*@res "SREP����"*/ + projectcode + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0107")/*@res "δ��ȷӳ��!"*/);
		MeasureVO measVO = measrepvo.getMeasVO();

		String pk_org = "";
		if (isself == HBFmlConst.SELF) {
			pk_org = (String) env.getExEnv(IContrastConst.PK_SELFCORP);
		} else {
			pk_org = (String) env.getExEnv(IContrastConst.PK_OPPCORP);
		}
		ContrastQryVO qryvo = (ContrastQryVO) env.getExEnv(IContrastConst.CONTRASTQRYVO);
		HBSchemeVO schemeVO = (HBSchemeVO) ((com.ufsoft.script.spreadsheet.UfoCalcEnv) env).getExEnv(IContrastConst.HBSCHEMEVO);
		Map<String, String> handOffset = OffsetHanlder.handOffset(schemeVO,qryvo.getKeymap(), offset);
		// �ж��Ƿ��Ƿ�Ҷ�ӽڵ㣬���Ƿ�Ҷ�ӽڵ㣬��ȡ�ϲ��汾������,Ŀǰֻ��SREPBO��PTPSUMBO,TPSUMBO������֧�������㷨
		HashSet<String> orgs = qryvo.getOrgs();
		//jiaah ���˵�ʱ����ߴ˷�֧
		if(orgs!=null){
			//ȡ�øñ�����֯��ϵ,��ĩ����λ
			if(qryvo.getHashLowerOrgs()==null){
				resetHashLowerOrgs(qryvo, orgs);
			}
			HashSet<String> hashLowerOrgs = qryvo.getHashLowerOrgs();
			if(qryvo.getLeafOrgs()==null){
				HashSet<String> leaforgs = new HashSet<String>();
				for (String string : orgs) {
					if(!hashLowerOrgs.contains(string)){
						leaforgs.add(string);
					}
				}
				qryvo.setLeafOrgs(leaforgs);
			}
			HashSet<String> leaforgs = orgs;//jiaah Ŀ�������İ����ж��˵�λ�ĸ��𱨱����ݶ�ȡ������������Է�srep����ֵ��ʱ��ȡ������ֵ
			if(hashLowerOrgs.contains(pk_org) && !(pk_org.equals(qryvo.getContrastorg()))){
				//��ĩ����λ
				//��ȡ�ϲ��������������
				Map<String, Map<String, UFDouble>> srep_hbadjSep_resultMap = qryvo.getSrep_hbadjSep_resultMap();
				Map<String, UFDouble> map = srep_hbadjSep_resultMap.get(measVO.getCode());
				if(map!=null){
					return getHBSepData(measVO, pk_org, qryvo, handOffset, hashLowerOrgs, map);
				}else{
					//����ȡ�úϲ��������������
					reSetDatMap(measVO, qryvo, handOffset, hashLowerOrgs, srep_hbadjSep_resultMap,HBVersionUtil.getHBAdjustByHBSchemeVO(qryvo.getSchemevo()));
					map = srep_hbadjSep_resultMap.get(measVO.getCode());
					return getHBSepData(measVO, pk_org, qryvo, handOffset, hashLowerOrgs, map);
				}
			}else{
				//ĩ����λ
				//��ȡ���𱨱����������
				Map<String, Map<String, UFDouble>> srep_sepadj_resultMap = qryvo.getSrep_sepadj_resultMap();
				Map<String, UFDouble> map = srep_sepadj_resultMap.get(measVO.getCode());
				if(map!=null){
					return getSepData(measVO, pk_org, qryvo, handOffset, leaforgs, map);
				}else{
					//����ȡ�ø��𱨱������������
					reSetDatMap(measVO, qryvo, handOffset, leaforgs, srep_sepadj_resultMap,HBVersionUtil.getVersion(qryvo.getSchemevo(), IDataVersionConsts.VERTYPE_SEPERATE_ADJUST, false));
					map = srep_sepadj_resultMap.get(measVO.getCode());
					return getSepData(measVO, pk_org, qryvo, handOffset, leaforgs, map);
				}
			}
		}

		String alone_id = "";
		//�ɱ���תȨ�淨���ߴ˷�֧
		// jiaah �ɱ���תȨ�淨��Ͷ�ʷ��İ汾��ȡ��ȡ���𱨱��Ǻϲ��������
		int reporttype = qryvo.getReporttype();
		if((isself != HBFmlConst.SELF) && reporttype > 0){
			String pk_ficititiousorg = null;// ʵ�嵥λ����Ӧ���ⵥλ,�����жϼ��ι�ϵ,����ʵ��Ӧ�ó���ֻ�����鵥λ����ʵ�嵥λ�ϼ�,����ֱ���ϼ�
			String sql = "select pk_org from org_rcsmember_v  where PK_ENTITYORG = ?  and pk_svid=?  and ISMANAGEORG='Y' ";
			SQLParameter parameter = new SQLParameter();
			parameter.addParam(pk_org);
			parameter.addParam(qryvo.getPk_hbrepstru());
			Object executeQuery = new BaseDAO().executeQuery(sql, parameter,
					new ColumnProcessor());
			if (executeQuery != null) {
				pk_ficititiousorg = (String) executeQuery;
			}

			if (IVouchType.TYPE_AUTO_SEP_ADJVOUCH == reporttype) {
				// ����Ƿ���ڸ��𱨱������,������ڸ��𱨱������,��ȡ���𱨱������
				alone_id = TPSUMBO.getSepAloneID(measVO, qryvo, pk_org,qryvo.getKeymap());
			} else {
				if (!StringUtil.isEmptyWithTrim(pk_ficititiousorg)) {
					// �����Ӧ��λ����ֱ���ϼ��鵥λ,Ӧ��ȡ����λת��Ϊ���ⵥλ,ȡ�鵥λ�ĺϲ�������
					alone_id = TPSUMBO.getHbAloneId(measVO, qryvo,pk_ficititiousorg, qryvo.getKeymap());
				} else {
					alone_id = TPSUMBO.getHbAloneId(measVO, qryvo,
							pk_org, qryvo.getKeymap());
				}
			}
		}else{
			ReportCombineStruMemberVersionVO[] vos = HBBaseDocItfService.getRemoteHBRepStru().queryReportCombineStruMemberByVersionId(qryvo.getPk_hbrepstru(), pk_org);
//			 ����ƫ����,��ָ������Ӧ����ʱ��ؼ���Ϊ׼�������Ժ���ʱ��ؼ���Ϊ׼����ʱ��ָ���Ӧ��ָ��Ϊ׼
			String keyCombPK = measrepvo.getMeasVO().getKeyCombPK();
			if (vos.length > 1) {
				alone_id = HBAloneIDUtil.findAloneID(pk_org, handOffset, keyCombPK, qryvo.getSchemevo().getVersion());
			} else {
				alone_id = HBAloneIDUtil.findAloneID(pk_org, keyCombPK, handOffset);
			}
		}
		
		//ƫ�ƺ󲻴���,�򷵻�0
		if(alone_id==null) return 0.0;

		double result = this.getSREP(measVO, alone_id);
		return result;
	}

	private double getHBSepData(MeasureVO measVO, String pk_org, ContrastQryVO qryvo, Map<String, String> handOffset, HashSet<String> hashLowerOrgs, Map<String, UFDouble> map) {
		UFDouble ufDouble = map.get(pk_org);
		if(ufDouble==null ){
			//ȡ�ϲ���������
			Map<String, UFDouble> map2 = qryvo.getSrep_hbsep_resultMap().get(measVO.getCode());
			if(map2!=null){
				UFDouble ufDouble2 = map2.get(pk_org);
				if(ufDouble2==null){
					return 0.0;
				}else{
					return ufDouble2.getDouble();
				}
			}else{
				//����ȡ�úϲ���������
				reSetDatMap(measVO, qryvo, handOffset, hashLowerOrgs, qryvo.getSrep_hbsep_resultMap(),qryvo.getSchemevo().getVersion());
				map2 = qryvo.getSrep_hbsep_resultMap().get(measVO.getCode());
				if(map2!=null){
					UFDouble ufDouble2 = map2.get(pk_org);
					if(ufDouble2==null){
						return 0.0;
					}else{
						return ufDouble2.getDouble();
					}
				}else{
					return 0.0;
				}
			}
		}else{
			return ufDouble.getDouble();
		}
	}
	private double getSepData(MeasureVO measVO, String pk_org, ContrastQryVO qryvo, Map<String, String> handOffset, HashSet<String> orgs, Map<String, UFDouble> map) {
		UFDouble ufDouble = map.get(pk_org);
		if(ufDouble==null ){
			//ȡ���𱨱�����
			Map<String, UFDouble> map2 = qryvo.getSrep_sep_resultMap().get(measVO.getCode());
			if(map2!=null){
				UFDouble ufDouble2 = map2.get(pk_org);
				if(ufDouble2==null){
					return 0.0;
				}else{
					return ufDouble2.getDouble();
				}
			}else{
				//����ȡ�ø��𱨱�����
				reSetDatMap(measVO, qryvo, handOffset, orgs, qryvo.getSrep_sep_resultMap(),0);
				map2 = qryvo.getSrep_sep_resultMap().get(measVO.getCode());
				if(map2!=null){
					UFDouble ufDouble2 = map2.get(pk_org);
					if(ufDouble2==null){
						return 0.0;
					}else{
						return ufDouble2.getDouble();
					}
				}else{
					return 0.0;
				}
			}
		}else{
			return ufDouble.getDouble();
		}
	}

	private void reSetDatMap(MeasureVO measVO, ContrastQryVO qryvo, Map<String, String> handOffset, HashSet<String> orgs, Map<String, Map<String, UFDouble>> datamap,int version) {
		ArrayList<MeasurePubDataVO>  arrayPubDataVOs = new ArrayList<MeasurePubDataVO>();
		HashMap<String,String>  org_AloneID_map  = new HashMap<String,String>();
		HashMap<String,MeasureDataVO>  AloneID_Value_map  = new HashMap<String,MeasureDataVO>();
		for (String org : orgs) {
			MeasurePubDataVO pubdata  = new MeasurePubDataVO();
			pubdata.setKType(measVO.getKeyCombPK());
			KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(measVO.getKeyCombPK());
			pubdata.setKeyGroup(keygroupVo);
			KeyVO[] keyvos=keygroupVo.getKeys();
			if(null!=keyvos && null!=handOffset /*&& keyvos.length==keyMap.size()*/){
				String[] keys=new String[handOffset.size()];
				handOffset.keySet().toArray(keys);
				if(null!=keys && keys.length>0){
					for(String key:keys){
							pubdata.setKeywordByPK(key, handOffset.get(key));
					}
				}
			}
			pubdata.setKeywordByPK(KeyVO.CORP_PK,org);
			pubdata.setVer(version);
			arrayPubDataVOs.add(pubdata);
		}
		try {
			MeasurePubDataVO[] findByKeywordArray = MeasurePubDataBO_Client.findByKeywordArray(arrayPubDataVOs.toArray(new MeasurePubDataVO[0]));
			ArrayList<String>  aloneids = new ArrayList<String>();
			for (int i = 0; i < findByKeywordArray.length; i++) {
				MeasurePubDataVO measurePubDataVO = findByKeywordArray[i];
				if(measurePubDataVO==null) {
					continue;
				}
				String org = measurePubDataVO.getKeywordByPK(KeyVO.CORP_PK);
				org_AloneID_map.put(org, measurePubDataVO.getAloneID());
				aloneids.add(measurePubDataVO.getAloneID());
			}
			MeasureDataVO[] datavos = HBPubItfService.getRemoteMeasureDataSrv().getRepData(aloneids.toArray(new String[0]), new MeasureVO[] {
				measVO
			});
			for (int i = 0; i < datavos.length; i++) {
				MeasureDataVO measureDataVO = datavos[i];
				AloneID_Value_map.put(measureDataVO.getAloneID(), measureDataVO);
			}
			for (String org : orgs) {
				String aloneid = org_AloneID_map.get(org);
				if(StringUtil.isEmptyWithTrim(aloneid)){
					if(datamap.containsKey(measVO.getCode())){
						datamap.get(measVO.getCode()).put(org, null);
					}else{
						HashMap<String,UFDouble> hashMap = new HashMap<String,UFDouble>();
						hashMap.put(org, null);
						datamap.put(measVO.getCode(), hashMap);
					}
					continue;
				}
				MeasureDataVO measureDataVO = AloneID_Value_map.get(aloneid);
				if(measureDataVO==null){
					if(datamap.containsKey(measVO.getCode())){
						datamap.get(measVO.getCode()).put(org, null);
					}else{
						HashMap<String,UFDouble> hashMap = new HashMap<String,UFDouble>();
						hashMap.put(org, null);
						datamap.put(measVO.getCode(), hashMap);
					}
					continue;
				}
				if(datamap.containsKey(measureDataVO.getMeasureVO().getCode())){
					datamap.get(measureDataVO.getMeasureVO().getCode()).put(org, measureDataVO.getUFDoubleValue());
				}else{
					HashMap<String,UFDouble> tmpMap = new HashMap<String,UFDouble>();
					tmpMap.put(org, measureDataVO.getUFDoubleValue());
					datamap.put(measureDataVO.getMeasureVO().getCode(), tmpMap);
				}
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private void resetHashLowerOrgs(ContrastQryVO qryvo, HashSet<String> orgs) throws BusinessException, DAOException {
		String sql ="select distinct pk_org From  org_rcsmember_v  where pk_rcsmember in ( select distinct  PK_FATHERMEMBER From   org_rcsmember_v  where pk_svid='"+qryvo.getPk_hbrepstru()+
		"'  and "+UFOCSqlUtil.buildInSql(ReportCombineStruMemberVersionVO.PK_ORG, orgs)+" )";
		HashSet<String> hashLowerOrgs = (HashSet<String>) new BaseDAO().executeQuery(sql, new ResultSetProcessor(){
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Object handleResultSet(ResultSet rs) throws SQLException {
				HashSet<String>  result = new HashSet<String>();
				while (rs.next()) {
					String object = (String) rs.getObject(1);
					result.add(object);
				}
				return result;
			}
			
		});
		qryvo.setHashLowerOrgs(hashLowerOrgs);
	}

	private double getSREP(MeasureVO measVO, String alone_id) throws BusinessException {
		MeasureVO[] measvos = new MeasureVO[] {
			measVO
		};
		MeasureDataVO[] datavos = MeasureDataProxy.getRepData(alone_id, measvos);
		if (null == datavos || datavos.length == 0) {
			return 0;
		}
		if (null != datavos[0]) {
			return datavos[0].getUFDoubleValue()==null ? 0.0:datavos[0].getUFDoubleValue().doubleValue();
		} else {
			return 0;
		}

	}

}