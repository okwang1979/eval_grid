package nc.impl.tb.rule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.NamingException;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.mw.sqltrans.TempTable;
import nc.itf.mdm.cube.ICubeDefQueryService;
import nc.itf.org.IOrgMetaDataIDConst;
import nc.itf.tb.control.IBusiSysExecAllDataProvider;
import nc.itf.tb.control.IBusiSysExecDataProvider;
import nc.itf.tb.control.IBusiSysExecDataProviderEx;
import nc.itf.tb.control.IBusiSysReg;
import nc.itf.tb.control.OutEnum;
import nc.itf.tb.rule.ICtlScheme;
import nc.itf.tb.sysmaintain.BusiSysReg;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.jdbc.framework.JdbcSession;
import nc.jdbc.framework.PersistenceManager;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.exception.DbException;
import nc.ms.mdm.convertor.IStringConvertor;
import nc.ms.mdm.convertor.StringConvertorFactory;
import nc.ms.mdm.cube.CubeServiceGetter;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ms.tb.control.AlertPercentHandler;
import nc.ms.tb.control.BudgetControlCTL;
import nc.ms.tb.control.CtlBdinfoCTL;
import nc.ms.tb.control.CtlSchemeCTL;
import nc.ms.tb.control.CtrlRuleCTL;
import nc.ms.tb.control.CtrltacticsCache;
import nc.ms.tb.control.DataGetterContext;
import nc.ms.tb.control.SqlPartlyTools;
import nc.ms.tb.formula.script.CtrlBusinessException;
import nc.ms.tb.formula.util.CountTimeCost;
import nc.ms.tb.pub.NtbSuperDMO;
import nc.ms.tb.pubutil.CostTime;
import nc.ms.tb.rule.RuleCacheManager;
import nc.ms.tb.rule.SingleSchema;
import nc.ms.tb.rule.SubLevelOrgGetter;
import nc.ms.tb.rule.fmlset.FormulaCTL;
import nc.ms.tb.rule.ruletype.IPKRuleConst;
import nc.pubitf.accperiod.AccountCalendar;
import nc.pubitf.bd.accessor.GeneralAccessorFactory;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.pubitf.org.IAccountingBookPubService;
import nc.pubitf.org.ILiabilityBookPubService;
import nc.vo.bd.accessor.IBDData;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.cube.DataCellValue;
import nc.vo.mdm.cube.DimVector;
import nc.vo.mdm.dim.IDimMemberPkConst;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.tb.control.ConvertToCtrlSchemeVO;
import nc.vo.tb.control.CtrlSchemeVO;
import nc.vo.tb.control.IdSysregVO;
import nc.vo.tb.formula.DimFormulaVO;
import nc.vo.tb.obj.GroupedNtbParamVO;
import nc.vo.tb.obj.NtbParamVO;
import nc.vo.tb.prealarm.IdAlarmDimVectorVO;
import nc.vo.tb.prealarm.IdAlarmschemeVO;
import nc.vo.tb.rule.IRuleClassConst;
import nc.vo.tb.rule.IdCtrlformulaVO;
import nc.vo.tb.rule.IdCtrlschemeVO;
import nc.vo.tb.task.MdTask;

import org.apache.commons.lang.RandomStringUtils;



public class CtlSchemeImpl implements ICtlScheme{





	public HashMap<String, ArrayList<String>> reloadZeroCtrlScheme() throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		StringBuffer sbStr = new StringBuffer();
		sbStr.append("PK_RULECLASS='" + IPKRuleConst.SCHEMA_ZERO + "'");
		DimFormulaVO[] vos = (DimFormulaVO[]) dmo.queryByWhereClause(DimFormulaVO.class, sbStr.toString());
		String ctrlName = null;
		for (int n = 0; n < (vos == null ? 0 : vos.length); n++) {
			DimFormulaVO vo = vos[n];
			sbStr.setLength(0);
			sbStr.append("PK_PARENT='" + vo.getPrimaryKey() + "'");
			IdCtrlformulaVO[] formulaVos = (IdCtrlformulaVO[]) dmo.queryByWhereClause(IdCtrlformulaVO.class, sbStr.toString());
			if (formulaVos != null && formulaVos.length > 0) {
				ctrlName = formulaVos[0].getCtrlname();
			}else{
				continue;
			}
			sbStr.setLength(0);
			StringBuffer nameShow = new StringBuffer();
			ArrayList<String> nameAndEntity = new ArrayList<String>();
			for (int m = 0; m < formulaVos.length; m++) {
				IdCtrlformulaVO formulaVo = formulaVos[m];
				sbStr.setLength(0);
				sbStr.append("pk_ctrlformula='"+formulaVo.getPrimaryKey()+"'");
				IdCtrlschemeVO[] schemeVos = (IdCtrlschemeVO[]) dmo.queryByWhereClause(IdCtrlschemeVO.class, sbStr.toString());
				if (schemeVos != null && schemeVos.length > 0) {
					List<String> orgList = new ArrayList<String> ();
					for(int i = 0 ; i < schemeVos.length ; i++) {
						String pk_org = schemeVos[i].getPk_org();
						String[] names = schemeVos[i].getNameidx().split(":");
						String[] pkidx = schemeVos[i].getStridx().split(":");
						if(!orgList.contains(pk_org)) {
							for(int k=0;k<pkidx.length;k++){
								String pk = pkidx[k];
								if(pk_org.equals(pk)){
									nameShow.append(names[k]).append(",");
								}
							}
							orgList.add(pk_org);
						}
					}
				}
			}
			if(!nameShow.toString().isEmpty()&&nameShow.toString().length()>0){
				nameShow.replace(nameShow.length() - 1, nameShow.length(), "");
				nameAndEntity.add(ctrlName);
				nameAndEntity.add(nameShow.toString());
				IUserManageQuery query = NCLocator.getInstance().lookup(IUserManageQuery.class);
				String userName = "";
				if(vo.getCreatedby() != null)
					userName = query.getUser(vo.getCreatedby()).getUser_name();
				nameAndEntity.add(userName);
				map.put(vo.getPrimaryKey(), nameAndEntity);	
			}
			
		}

        return map;
    }

	public void deleteZeroCtrlScheme(ArrayList<String> pks, boolean delFormulaVO) throws BusinessException {
			NtbSuperDMO dmo = new NtbSuperDMO();
			HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
			StringBuffer sbStr = new StringBuffer();
			sbStr.append("PK_OBJ IN (");
			for (int n = 0; n < pks.size(); n++) {
				if (n != pks.size() - 1) {
					sbStr.append("'").append(pks.get(n)).append("',");
				} else {
					sbStr.append("'").append(pks.get(n)).append("'");
				}
			}
			sbStr.append(")");
			if (delFormulaVO) {
				dmo.deleteByWhereClause(DimFormulaVO.class, sbStr.toString());
			}
			sbStr.setLength(0);

			/** 删除控制方案主表 */
			sbStr.append("PK_PARENT IN (");
			for (int n = 0; n < pks.size(); n++) {
				if (n != pks.size() - 1) {
					sbStr.append("'").append(pks.get(n)).append("',");
				} else {
					sbStr.append("'").append(pks.get(n)).append("'");
				}
			}
			sbStr.append(")");
			/** 获取控制方案主表主建 */
			IdCtrlformulaVO[] vos = (IdCtrlformulaVO[]) dmo.queryByWhereClause(IdCtrlformulaVO.class, sbStr.toString());
			dmo.deleteByWhereClause(IdCtrlformulaVO.class, sbStr.toString());
			sbStr.setLength(0);
			/** 删除控制方案子表 */
			if (vos != null && vos.length > 0) {
				sbStr.append("PK_CTRLFORMULA IN(");
				for (int n = 0; n < vos.length; n++) {
					if (n != vos.length - 1) {
						sbStr.append("'").append(vos[n].getPrimaryKey())
								.append("',");
					} else {
						sbStr.append("'").append(vos[n].getPrimaryKey())
								.append("'");
					}
				}
				sbStr.append(")");
				dmo.deleteByWhereClause(IdCtrlschemeVO.class, sbStr.toString());
			}
     }

	public String[] addCtrlformulas(ArrayList<IdCtrlformulaVO> vos) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String[] pks = dmo.insertArray(vos.toArray(new IdCtrlformulaVO[0]));
		return pks;
    }

    public String[] addCtrlScheme(ArrayList<IdCtrlschemeVO> vos) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String[] pks = dmo.insertArray(vos.toArray(new IdCtrlschemeVO[0]));
		return pks;
    }

	public ArrayList<DimFormulaVO> queryDimFormulas(ArrayList<String> pks) throws BusinessException {
		ArrayList<DimFormulaVO> vosList = new ArrayList<DimFormulaVO>();
		NtbSuperDMO dmo = new NtbSuperDMO();
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		StringBuffer sbStr = new StringBuffer();
		sbStr.append("PK_OBJ IN (");
		for (int n = 0; n < pks.size(); n++) {
			if (n != pks.size() - 1) {
				sbStr.append("'").append(pks.get(n)).append("',");
			} else {
				sbStr.append("'").append(pks.get(n)).append("'");
			}
		}
		sbStr.append(")");
		DimFormulaVO[] vos = (DimFormulaVO[]) dmo.queryByWhereClause(DimFormulaVO.class, sbStr.toString());
		vosList.addAll(Arrays.asList(vos));
		return vosList;
    }

	public ArrayList<IdCtrlformulaVO> queryCtrlFormula(String sWhere) throws BusinessException {
		ArrayList<IdCtrlformulaVO> returnvo = new ArrayList<IdCtrlformulaVO>();
		try {
			NtbSuperDMO dmo = new NtbSuperDMO();
			IdCtrlformulaVO[] sdtasks = (IdCtrlformulaVO[]) dmo.queryByWhereClause(IdCtrlformulaVO.class, sWhere);
			for (int i = 0; i < (sdtasks == null ? 0 : sdtasks.length); i++) {
				IdCtrlformulaVO vo = sdtasks[i];
				returnvo.add(vo);
			}
		} catch (Exception e) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000208")/* 查询计划出错 */);
		}
		return returnvo;
		}


	public void updateCtrlSchemeTable(NtbParamVO[] param) throws BusinessException {
		PersistenceManager manager = null;
		try {
			if (param == null ||param.length == 0)
				return;
			manager = PersistenceManager.getInstance();
			ArrayList listValues = new ArrayList<String>();
			JdbcSession session = manager.getJdbcSession();
			for (int i = 0; i < param.length; i++) {
				listValues.add(param[i].getCtrlscheme());
			}
			String[] sqlParts = SqlPartlyTools.getBatchSQL("pk_obj",(String[]) listValues.toArray(new String[0]));
			
			ArrayList listsqls = new ArrayList();
			String tmpSql = "update tb_ctrlscheme set rundata = ? ,readydata = ? where pk_obj= ?"; 
			SQLParameter parameter = null;// new SQLParameter();
			for (int i = 0; i < listValues.size(); i++) {
				int currtype = param[i].getCurr_type();
				parameter = new SQLParameter();
				parameter.addParam((param[i].getRundata() == null ? 0 : (param[i]
				                               								.getRundata()[currtype] == null ? 0 : param[i]
				                               								         								.getRundata()[currtype].doubleValue())));
				parameter.addParam((param[i].getReadydata() == null ? 0 : ((param[i]
				                                  								.getReadydata()[currtype] == null ? 0
				                                  										: param[i].getReadydata()[currtype]
				                                  												.doubleValue()))));
				parameter.addParam(listValues.get(i));
				session.addBatch(tmpSql, parameter);	
				
			}

			session.executeBatch();
		} catch (DbException dbe) {
			NtbLogger.error(dbe);
			throw new DAOException(dbe);
		} finally {
			if (manager != null)
				manager.release();
		}
	}

	public IdCtrlschemeVO[] convertIdCtrlscheme(SingleSchema schema) throws Exception {
		CountTimeCost getUfidScheme = new CountTimeCost();
		getUfidScheme.beginCost();
		//IdCtrlschemeVO schemevos = new IdCtrlschemeVO();
		String[] src_ufind = schema.getUFind();
		String[] src_prefind = schema.getPREUFind();
		List<ConvertToCtrlSchemeVO> convertorList = new ArrayList<ConvertToCtrlSchemeVO>();
		List<IdCtrlschemeVO> schemeList = new ArrayList<IdCtrlschemeVO>();
//		if (src_ufind.length > 0) {
//			convertor = new ConvertToCtrlSchemeVO(src_ufind[0], "UFIND");
//			// schemevos.setMethodcode("UFIND");
//			schemevos.setMethodname("UFIND");
//		} else if (src_prefind.length > 0) {
//			convertor = new ConvertToCtrlSchemeVO(src_prefind[0], "PREFIND");
//			// schemevos.setMethodcode("PREFIND");
//			schemevos.setMethodname("PREFIND");
//		} else {
//			return null;
//		}

		if(src_ufind != null) {
			for(String ufind : src_ufind) {
				ConvertToCtrlSchemeVO vo = new ConvertToCtrlSchemeVO(ufind, "UFIND");
				//convertorList.addAll(Arrays.asList(vo.composeCtrlSchmByBillType()));
				convertorList.add(vo);
			}
		}
		if(src_prefind != null) {
			for(String prefind : src_prefind) {
				ConvertToCtrlSchemeVO vo = new ConvertToCtrlSchemeVO(prefind, "PREFIND");
				//convertorList.addAll(Arrays.asList(vo.composeCtrlSchmByBillType()));
				convertorList.add(vo);
			}
		}
		try {
			// songrui 2009.02.22执行数取数如果没有对应具体的公司目录，抛异常处理
			for(ConvertToCtrlSchemeVO convertor : convertorList) {
				IdCtrlschemeVO schemevos = new IdCtrlschemeVO();
				if (convertor.getPkOrg().equals("null")
						|| convertor.getPkOrg().equals("")) {
					throw new BusinessException(NCLangRes4VoTransl
							.getNCLangRes().getStrByID("tbb_ctrl",
									"01801ctl_000212")/* 预算主体没有参照公司目录！ */);
				} else {
					schemevos.setPk_org(convertor.getPkOrg());
				}
				if (convertor.getPkCurrency().equals("null")
						|| convertor.getPkCurrency().equals("")) {
					throw new BusinessException(NCLangRes4VoTransl
							.getNCLangRes().getStrByID("tbb_ctrl",
									"01801ctl_000178")/* 币种没有参照外币档案！ */);
				} else {
					schemevos.setPk_currency(convertor.getPkCurrency());
				}

				getUfidScheme
						.addCost("Ufida schema 1", getUfidScheme.getCost());

				schemevos.setStridx(CtlBdinfoCTL.getActualPk(convertor));
				getUfidScheme
						.addCost("Ufida schema 2", getUfidScheme.getCost());
				// schemevos.setStridx(convertor.getPkIdx());
				schemevos.setMethodname(convertor.getMethodFunc());
				schemevos.setCtrlsys(convertor.getCtrlSys());
				schemevos.setBilltype(convertor.getBillType());
				schemevos.setCtrldirection(convertor.getCtrlDirection());
				schemevos.setCtrlobj(convertor.getCtrlObject());
				schemevos.setCtrlobjValue(convertor.getCtrlObjectValue());
				schemevos.setIncludeuneffected(convertor.getUneffenctdata());
				schemevos.setStartdate(convertor.getStartDate());
				schemevos.setEnddate(convertor.getEndDate());
				schemevos.setAccctrollflag(convertor.getAccCtrlFlag());
				schemevos.setPk_org(convertor.getPkOrg());
				schemevos.setCurrtype(getCurrencyType(convertor.getPkCurrency()));
				schemevos.setPk_currency(convertor.getPkCurrency());
				schemevos.setPk_ncentity(convertor.getPkOrg());
				schemevos.setFromitems(convertor.getFromItem());
				schemevos.setCodeidx(filterContent(convertor.getCodeIdx()));
				schemevos.setCtllevels(convertor.getCtrlLevel());
				schemevos.setRundata(new UFDouble(0));
				schemevos.setReadydata(new UFDouble(0));
				schemevos.setNameidx(filterContent(convertor.getNameIdx()));
				schemevos.setDatetype(convertor.getDataCatalg());
				schemeList.add(schemevos);
			}

		} catch (BusinessException e) {
			throw e;
		} catch (Exception ex) {
			NtbLogger.error(ex);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("tbb_ctrl", "01801ctl_000213")/* 分配公式出错！ */);
		}

    return schemeList.toArray(new IdCtrlschemeVO[0]);
    }

	private String filterContent(String content){
		String _REPLACE="@CONTENT@";
		if(content!=null&&content.contains(_REPLACE))
			content = content.replaceAll(_REPLACE,",");
		return content ;
	}

	public int getCurrencyType(String src) {
		// 原币
		int type = 3;
		// 全局本币
		if (src.equals(IDimMemberPkConst.PK_GLOBE_CURRENCY)) {
			type = 0;
		}
		// 集团本币
		if (src.equals(IDimMemberPkConst.PK_GROUP_CURRENCY)) {
			type = 1;
		}
		//组织本币
		if (src.equals(IDimMemberPkConst.PK_ORG_CURRENCY)) {
			type = 2;
		}
		return type;
	}

	private HashMap<String, ArrayList<NtbParamVO>> sortVOsBySys(IdCtrlschemeVO[] ctlvos) throws Exception {

		NtbParamVO[] params = parseCtrls(ctlvos,null);
		return getParamMapBySys(params);

	}

	private HashMap<String, ArrayList<NtbParamVO>> sortVOsBySys(Map<Integer, IdCtrlschemeVO[]> schemeMap) throws Exception {
		List<NtbParamVO> paramList = new ArrayList<NtbParamVO>();
		Map<String, String[]> cachMap = new HashMap<String, String[]>();
		SubLevelOrgGetter orgLevGetter  = new  SubLevelOrgGetter(); 
		for(Map.Entry<Integer, IdCtrlschemeVO[]> entry : schemeMap.entrySet()) {
			NtbParamVO[] params = parseCtrls(entry.getValue(),orgLevGetter);
			for(NtbParamVO param : params) {
				param.setNtbparamvoId(entry.getKey().toString());
				paramList.add(param);
			}
		}
		return getParamMapBySys(paramList.toArray(new NtbParamVO[0]));

	}

	private HashMap<String, ArrayList<NtbParamVO>> getParamMapBySys(NtbParamVO[] params) {
		HashMap<String, ArrayList<NtbParamVO>> map = new HashMap<String, ArrayList<NtbParamVO>>();
		for (int i = 0; i < params.length; i++) {
			String sys = params[i].getSys_id();
			if (map.containsKey(sys)) {
				ArrayList<NtbParamVO> list = (ArrayList<NtbParamVO>) map
						.get(sys);
				list.add(params[i]);
			} else {
				ArrayList<NtbParamVO> list = new ArrayList<NtbParamVO>();
				list.add(params[i]);
				map.put(sys, list);
			}
		}

		return map;
	}

	private String parseBillTypes(String billtyes) {
		if (billtyes == null || billtyes.trim().length() == 0) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		if (billtyes.indexOf("[") >= 0) {
			while (billtyes.indexOf("]") >= 0) {
				if (buffer.toString().length() == 0) {
					buffer.append(billtyes.substring(billtyes.indexOf("[") + 1,
							billtyes.indexOf("]")));
				} else {
					buffer.append("#");
					buffer.append(billtyes.substring(billtyes.indexOf("[") + 1,
							billtyes.indexOf("]")));

				}
				billtyes = billtyes.substring(billtyes.indexOf("]") + 1);
			}
		} else {
			buffer.append(billtyes);
		}

		return buffer.toString();
	}

	private String getPKORGByFINANCEId(String pk_finance) throws BusinessException {
			String[] pk_orgs = new String[] { pk_finance };
			String pk_accountingBook = null;
			/** 通过财务组织的PK去寻找财务核算账簿 */
			IAccountingBookPubService bookPubService = (IAccountingBookPubService) NCLocator
					.getInstance()
					.lookup(IAccountingBookPubService.class.getName());
			Map<String, String> map = bookPubService
					.queryAccountingBookIDByFinanceOrgIDWithMainAccountBook(pk_orgs);
			if (map == null) {
				throw new BusinessException(NCLangRes4VoTransl.getNCLangRes()
						.getStrByID("tbb_ctrl", "01801ctl_000207")/* 此预算组织没有对应总帐系统下的核算帐簿 */);
			} else {
				pk_accountingBook = map.get(pk_finance);
				if (pk_accountingBook == null) {
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes()
							.getStrByID("tbb_ctrl", "01801ctl_000207")/* 此预算组织没有对应总帐系统下的核算帐簿 */);
				}
			}
			return pk_accountingBook;
    }

	private AccountCalendar getAccountCalendar(String accperiod) throws BusinessException {
		AccountCalendar accountCalendar = AccountCalendar.getInstanceByPeriodScheme(accperiod);
		return accountCalendar;
    }

	public void dealAccountDate(NtbParamVO vo) throws BusinessException {
		/**
		 * 卫姐的新需求: 预算的计划期间和存货核算的会计期间匹配规则：
		 * 将“预算的计划期间的起始结束日期”与预算组织所属财务组织或对应财务组织主账簿的会计期间方案下的
		 * 具体会计期间的起始结束日期匹配后传给存货核算系统。
		 */
		String sysid = vo.getSys_id();
		boolean iskjqj = vo.isKjqj();
		IBusiSysReg resaReg = CtlSchemeCTL.getBusiSysReg(sysid);
		boolean isUseAccountDate = resaReg.isUseAccountDate(vo.getBill_type());
		if (isUseAccountDate || iskjqj) {
			if(sysid.equals(OutEnum.HRPSYS)) return;
			/** 对会计期间的处理,暂时怎么处理,正常情况下,应该根据各业务系统的帐簿来确定期间 yuyonga */
			/** 通过预算组织找到财务组织,如果不是财务组织,直接截取日期 */

			IGeneralAccessor financeorg_accesssor = GeneralAccessorFactory
					.getAccessor(IOrgMetaDataIDConst.FINANCEORG);
			IBDData financeorg_bddata = financeorg_accesssor.getDocByPk(vo
					.getPk_Org());
			String pk_accountingBook = null;
			if (OutEnum.RESASYS.equals(vo.getSys_id())) { // 责任会计是的主组织是责任核算帐簿
				financeorg_accesssor = GeneralAccessorFactory
						.getAccessor(IOrgMetaDataIDConst.LIABILITYBOOK);
				financeorg_bddata = financeorg_accesssor.getDocByPk(vo
						.getPk_Org());
				if (financeorg_bddata != null) {
					pk_accountingBook = vo.getPk_Org();
				}
			}
			if (financeorg_bddata == null) {
				String start = vo.getBegDate();
				String end = vo.getEndDate();
				String _strat = start.substring(0, 7);
				vo.setBegDate(_strat);
				String _end = end.substring(0, 7);
				vo.setEndDate(_end);
			} else {
				/** 是财务组织,如果是财务组织的话,并且有核算帐薄的话,就取核算帐簿,如果没有,直接截取 */
				if (pk_accountingBook == null) {
					pk_accountingBook = getPKORGByFINANCEId(vo.getPk_Org());
				}
				if (pk_accountingBook == null) {
					String start = vo.getBegDate();
					String end = vo.getEndDate();
					String _strat = start.substring(0, 7);
					vo.setBegDate(_strat);
					String _end = end.substring(0, 7);
					vo.setEndDate(_end);
				} else {
					String accperiod = null;
					if (OutEnum.RESASYS.equals(vo.getSys_id())) {
						ILiabilityBookPubService bookPubService = (ILiabilityBookPubService) NCLocator
								.getInstance().lookup(
										ILiabilityBookPubService.class
												.getName());
						accperiod = bookPubService
								.queryAccperiodCalendarIDByLiabilityBookID(pk_accountingBook);
					} else {
						IAccountingBookPubService bookPubService = (IAccountingBookPubService) NCLocator
								.getInstance().lookup(
										IAccountingBookPubService.class
												.getName());
						accperiod = bookPubService
								.queryAccperiodSchemeByAccountingBookID(pk_accountingBook);
					}

					AccountCalendar accountCalendar = getAccountCalendar(accperiod);
					String start = vo.getBegDate();
					String end = vo.getEndDate();
					if (start.length() > 7) {
						accountCalendar.setDate(new UFDate(start));
						vo.setBegDate(accountCalendar.getMonthVO().getYearmth());
					}
					if (end.length() > 7) {
						accountCalendar.setDate(new UFDate(end));
						vo.setEndDate(accountCalendar.getMonthVO().getYearmth());
					}
				}
			}
			/** 针对供应链存货核算做时间上的处理,如果选中的是JC单据,则把开始期间和结束期间合并为一个,取结束的 */
			if (OutEnum.IASYS.equals(sysid) && "JC".equals(vo.getBill_type())) {
				vo.setBegDate(vo.getEndDate());
			} /*
			 * else if (OutEnum.GLSYS.equalsIgnoreCase(sysid) &&
			 * vo.getData_attr().indexOf("balance") >= 0) {
			 * vo.setBegDate(vo.getBegDate() + "-01");
			 * vo.setEndDate(vo.getEndDate() + "-01"); }
			 */

		}
	}

	public String[] filterStridx(IdCtrlschemeVO vo, String[] bdinfotypeidx) throws Exception {
			String sysid = vo.getCtrlsys();
			String[] att = new String[5];
			StringBuffer bf_PkDim = new StringBuffer();
			StringBuffer bf_BusiAttrs = new StringBuffer();
			StringBuffer bf_Includelower = new StringBuffer();
			StringBuffer bf_TypeDim = new StringBuffer();
			StringBuffer bf_Code_dims = new StringBuffer();

			String[] stridx = vo.getStridx().split(":");
			String[] fromitem = vo.getFromitems().split(":");
			String[] ctllevel = vo.getCtllevels().split(":");
			String[] nameidx = vo.getNameidx().split(":");
			String[] codeidx = vo.getCodeidx().split(":");
			String[] bdinfotype = BudgetControlCTL.getBdinfoType(fromitem, sysid).split(":");
			// 过滤掉字段中为null的字段，ARAP取数不支持有null的字段,NOSUCHBASEPKATSUBCORP该字段只对外币档案做过滤
			for (int i = 0; i < fromitem.length; i++) {
				if (!(stridx[i].equals(OutEnum.NOSUCHBASEPKATSUBCORP) && bdinfotype[i]
						.equals(OutEnum.CURRDOC))) {
					bf_PkDim.append(stridx[i] + ":");
					bf_BusiAttrs.append(fromitem[i] + ":");
					bf_Includelower.append(ctllevel[i] + ":");
					bf_TypeDim.append(bdinfotypeidx[i] + ":");
					bf_Code_dims.append(codeidx[i] + ":");
				}
			}
			att[0] = bf_PkDim.toString();
			att[1] = bf_BusiAttrs.toString();
			att[2] = bf_Includelower.toString();
			att[3] = bf_TypeDim.toString();
			att[4] = bf_Code_dims.toString();
			return att;
    }

	public String[] startCtrlSchemeVOs(IdCtrlschemeVO[] schvos) throws Exception {

		HashMap paraMap = sortVOsBySys(schvos);
		ArrayList<String> infolist = new ArrayList<String>();
		Iterator itor = paraMap.keySet().iterator();
		while (itor.hasNext()) {
			String src = (String) itor.next();
			ArrayList ls = (ArrayList) paraMap.get(src);
			NtbParamVO[] params = (NtbParamVO[]) ls.toArray(new NtbParamVO[0]);
			IBusiSysExecDataProvider exeprovider = getExcProvider(src);
//			params = CtlSchemeServiceGetter.getICtlScheme().getExcProviderFunc(src,params);
			// 取得业务系统的控制阶段，设置参数的是否包含未生效单据字段
			setIncludeEff(exeprovider, params);
			/**
			 * UFind()公式取数不应该根据公司取是否包含未生效数据，根据手工设置的，在parseCtrl()方法中已经设置
			 * */
			ArrayList<NtbParamVO> ufindVO = new ArrayList<NtbParamVO>();
			ArrayList<NtbParamVO> prefindVO = new ArrayList<NtbParamVO>();
			for (int n = 0; n < params.length; n++) {
				if (params[n].getMethodCode().equals("UFIND")) {
					ufindVO.add(params[n]);
				} else if (params[n].getMethodCode().equals("PREFIND")) {
					prefindVO.add(params[n]);
				}
			}
			nc.vo.pub.lang.UFDouble[][] ufindretdatas = null;
			nc.vo.pub.lang.UFDouble[][] prefindretdatas = null;
			if (exeprovider instanceof IBusiSysExecAllDataProvider) {
				((IBusiSysExecAllDataProvider) exeprovider).setAllNtbParamVO(params);
			}
			if (ufindVO.size() > 0) {
				NtbParamVO[] vos = ufindVO.toArray(new NtbParamVO[0]);
				ufindretdatas=groupGetExeData(vos,exeprovider,false);
				if(ufindretdatas==null){
					ufindretdatas = exeprovider.getExecDataBatch(vos);

				}

			}

			if (prefindVO.size() > 0) {
				NtbParamVO[] vos = prefindVO.toArray(new NtbParamVO[0]);
				prefindretdatas=groupGetExeData(vos,exeprovider,true);
				if(prefindretdatas==null){
					prefindretdatas = exeprovider.getReadyDataBatch(vos);
				}
			}

			if (ufindretdatas != null) {
				for (int j = 0; j < ufindVO.size(); j++) {
					ufindVO.get(j).setRundata(ufindretdatas[j]);
				}
			}
			if (prefindretdatas != null) {
				for (int j = 0; j < prefindVO.size(); j++) {
					prefindVO.get(j).setReadydata(prefindretdatas[j]);
				}
			}

			ArrayList<NtbParamVO> allNtbParamVO = new ArrayList<NtbParamVO>();
			allNtbParamVO.addAll(ufindVO);
			allNtbParamVO.addAll(prefindVO);

			params = allNtbParamVO.toArray(new NtbParamVO[0]);

			// 合并公司控制下级的NtbParamVO
			// NtbParamVO[] mergeParamVos = mergeParamVO(params);
			NtbParamVO[] mergeParamVos = params;
//			if (!isTimingAlarm()) {
//				// info为其中一个系统的控制信息
//				info = compare(mergeParamVos, m_map, n_map);
//				for (int m = 0; m < info.length; m++) {
//					infolist.add(info[m]);
//				}
//			}
			CtlSchemeCTL.updateCtrlSchemeTable(mergeParamVos);
		}
		return (String[]) infolist.toArray(new String[0]);
	}


	private NtbParamVO[] parseCtrls(IdCtrlschemeVO[] ctlvos,SubLevelOrgGetter orgGetter) throws Exception {

		try {
			String spliter = ":";
			IBusiSysReg resaReg = null;
			ArrayList<NtbParamVO> listParams = new ArrayList<NtbParamVO>();
			for (int i = 0; i < ctlvos.length; i++) {
				NtbParamVO paramvo = new NtbParamVO();
				String funName = null;
				if (ctlvos[i].getMethodname() != null) {
					funName = ctlvos[i].getMethodname();
				}

				String pk_org = ctlvos[i].getPk_org();
				String billtype = parseBillTypes(ctlvos[i].getBilltype());
				String sysId = ctlvos[i].getCtrlsys();
				paramvo.setMethodCode(funName);
				paramvo.setSys_id(sysId);

				/** 获取对应的动作,影响远程调用次数,做缓存处理 */
				if (billtype != null) {
					if (!(billtype.indexOf(",") > 0)) { // 单个单据
						HashMap<String, String> actionMap = CtrltacticsCache
								.getNewInstance()
								.getActionByBillTypeAndSysId(paramvo.getSys_id(),parseBillTypes(ctlvos[i].getBilltype()),paramvo.getMethodCode());
						paramvo.setActionMap(actionMap);
						HashMap<String, HashMap<String, String>> _map = new HashMap<String, HashMap<String, String>>();
						_map.put(billtype, actionMap);
						paramvo.setBillTypesActionMap(_map);
					} else {
						String[] billtypes = parseBillTypes(
								ctlvos[i].getBilltype()).split(",");
						HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();
						for (int n = 0; n < billtypes.length; n++) {
							String _billtype = billtypes[n];
							HashMap<String, String> actionMap = CtrltacticsCache
									.getNewInstance()
									.getActionByBillTypeAndSysId(
											paramvo.getSys_id(), _billtype,
											paramvo.getMethodCode());
							map.put(_billtype, actionMap);
						}
						paramvo.setBillTypesActionMap(map);
					}
				}
				/** 结束 */
				/**
				 * UFind()取数用 songrui add 2008.11.25
				 * 控制方案取数时会在后面覆盖一遍，通过setIncludeEff()方法，UFind()取数和控制方案取数都不会有影响
				 * */
				/**
				 * 这个地方的setIsUnInure()方法对UFind()取数有用，对于控制来说没有意义，可以用这个字段设置是否包含未生效
				 * 字段includeuneffected字段的两个意义：(1)取数,包含未生效数据(2)控制,特殊方案+累进+包含期初余额
				 **/
				paramvo.setIsUnInure(ctlvos[i].getIncludeuneffected()
						.booleanValue());
				paramvo.setIncludeInit(ctlvos[i].getIncludeuneffected()
						.booleanValue());

				// 责任会计--是否是会计期间 特殊处理 dengyh 2011-05-31
				if (OutEnum.RESASYS.equalsIgnoreCase(sysId)) {
					resaReg = CtlSchemeCTL.getBusiSysReg(OutEnum.RESASYS);
					paramvo.setIsKjqj(resaReg.isUseAccountDate(billtype));
				} /*
				 * else if (OutEnum.GLSYS.equalsIgnoreCase(sysId) &&
				 * ctlvos[i].getCtrlobj().indexOf("balance") >= 0) {
				 * paramvo.setIsKjqj(true); }
				 */else {
					paramvo.setIsKjqj(false);
				}
				if (ctlvos[i].getStartdate() != null) {
					paramvo.setBegDate(ctlvos[i].getStartdate());
					paramvo.setEndDate(ctlvos[i].getEnddate());
				}
				paramvo.setPk_Org(pk_org);
				paramvo.setBill_type(billtype);
				paramvo.setData_attr(ctlvos[i].getCtrlobj());
				paramvo.setData_attrExt(ctlvos[i].getCtrlobjValue());
				/** 对各业务系统会计期间的统一处理 */
				dealAccountDate(paramvo);

				paramvo.setPk_ctrl(ctlvos[i].getPrimaryKey());
				/** 控制方案主表(公式表)pk */
				paramvo.setGroupname(ctlvos[i].getPk_ctrlformula());
				/** 业务单元 */
				paramvo.setPk_org_book(pk_org); // 针对总帐,责任会计
				/**获取到任务的集团*/
//				String pk_plan = ctlvos[i].getPk_plan();
//				MdTask task = TbTaskCtl.getMdTaskByPk(pk_plan, false);
                paramvo.setPk_Group(InvocationInfoProxy.getInstance().getGroupId());
				paramvo.setPk_accentity(pk_org);
				String bdinfotype = CtlSchemeCTL.getBdinfoType(
						ctlvos[i].getFromitems(), ctlvos[i].getCtrlsys());
				/** 基本档案类型 */
				String[] bdinfotypeidx = bdinfotype.split(spliter);
				/** 基本档案控制下级 */
				String[] ctrllevel = ctlvos[i].getCtllevels().split(spliter);
				/** 判断主组织是否控制下级 */
				boolean isControlDownCorp = false;
				for (int j = 0; j < bdinfotypeidx.length; j++) {
					/** 现在暂时只有资金和销售能够体现上下级关系 */
					if (bdinfotypeidx[j].equals(OutEnum.ZJORG)
							|| bdinfotypeidx[j].equals(OutEnum.XSOGR) || bdinfotypeidx[j].equals(OutEnum.ZHANGBU)) {
						Boolean value = new Boolean(ctrllevel[j]);
						isControlDownCorp = value.booleanValue();
						break;
					}
				}
				/** 判断是否控制所有公司需要考虑部门档案做为主体的情况 */
				boolean isControlAllCorp = false;
				/**
				 * (1)部门档案作主体可以根据当前部门档案取到公司的PK
				 * (2)单项方案和组方案都会根据当前单元格实例化控制方案，所以肯定有公司PK，不能出现控制全部公司的情况
				 * (3)特殊方案设置如果没有公司目录时，应该实例化pk_corp = "0001",控制全部公司
				 * (4)控制所有公司的条件就是pk_corp=0001的特殊方案，单项方案和组方案不会出现这种情况
				 * */
				/** 判断pk_org是否为集团 */
				// boolean isGroupType =
				// OrgTypeManager.getInstance().isTypeOfByPk(pk_org,
				// IOrgConst.GROUPORGTYPE);
//				boolean isGroupType = NtbOrgTypeCache.getNewInstance()
//						.getOrgType(pk_org, IOrgConst.GROUPORGTYPE);
//				if (false/* isGroupType */) {
//					isControlAllCorp = true;
//				}
				/** 具体币种PK,本币也计算出来具体币种,报销和收付有特殊要求,如果是控制本币的话,是不需要传具体币种 */
				String pk_currency = CtlSchemeCTL.getPk_currency(ctlvos[i].getPk_currency(), ctlvos[i].getPk_org(),sysId);
				paramvo.setPk_currency(pk_currency);
				/** 全局本币(0),集团本币(1),组织本币(2),原币(3) */
				paramvo.setCurr_type(CtlSchemeCTL.getCurrencyType(ctlvos[i]
						.getPk_currency()));
				paramvo.setSys_id(ctlvos[i].getCtrlsys());
				// paramvo.setBill_type(parseBillTypes(ctlvos[i].getBilltype()));

				paramvo.setDateType(ctlvos[i].getDatetype());
				paramvo.setDirection(ctlvos[i].getCtrldirection());

				/* 控制取数---0 执行取数--1 */
				paramvo.setCtrlstatus(0);
				// 所有pk为null的字段都应该去掉，ex.外币档案，如果对应的本币或者是辅币，则pk=null,在参数NrbParamVO中不允许出现为null的字段，需要过滤
				String[] att = filterStridx(ctlvos[i], bdinfotypeidx);
				paramvo.setPkDim(att[0].split(spliter/* ":" */));
				paramvo.setBusiAttrs(att[1].split(spliter/* ":" */));
				String[] ctrllevels = att[2].split(spliter/* ":" */);
				boolean[] value = new boolean[ctrllevels.length];
				HashMap<String, String[]> leveldownMap = new HashMap<String, String[]>();
				for (int j = 0; j < ctrllevels.length; j++) {
					value[j] = UFBoolean.valueOf(ctrllevels[j]).booleanValue();
					/** 如果是下级需要取数 */
					if (value[j]) {
						if(!paramvo.getPkDim()[j].equals(paramvo.getPk_Org())) {
							String[] levelDowsPks = CtlBdinfoCTL.getBdChilddataVO(
									paramvo.getPkDim()[j],
									paramvo.getBusiAttrs()[j], paramvo.getPk_Org(),
									paramvo.getSys_id(), true);
							leveldownMap.put(paramvo.getBusiAttrs()[j],
									levelDowsPks);
						} else {
							SubLevelOrgGetter orgLevGetter = null;
							if(orgGetter!=null){
								orgLevGetter = orgGetter;
							}else{
								orgLevGetter  = new SubLevelOrgGetter();
							}
							
							String[] levelDownPks = orgLevGetter.getSubLevelOrgsByOrgAndBd(
									paramvo.getPk_Org(),
									paramvo.getBusiAttrs()[j], paramvo.getSys_id());
							leveldownMap.put(paramvo.getBusiAttrs()[j], levelDownPks);
						}
					}
				}
				paramvo.setLowerArrays(leveldownMap);
				paramvo.setIncludelower(value);
				paramvo.setTypeDim(att[3].split(spliter));
				paramvo.setCode_dims(att[4].split(spliter));
				paramvo.setVarno(ctlvos[i].getVarno());
				paramvo.setCtrlscheme(ctlvos[i].getPrimaryKey());
				/** 对公司目录控制下级的处理,要考虑部门档案做为主体的情况 */
				if (isControlDownCorp) {
					listParams.add(paramvo);
					// CtlSchemeCTL.addCorpParams(paramvo,listParams,ctlvos[i],true,
					// bdAccCache);
				}/** 控制集团的处理 */
				else if (isControlAllCorp) {
					listParams.add(paramvo);
					CtlSchemeCTL.addGroupDownAllOrgParams(paramvo, listParams,
							ctlvos[i]);
				} else {
					listParams.add(paramvo);
				}
				CtlSchemeCTL.validateNtbParamVO(paramvo);
			}
			return (NtbParamVO[]) listParams.toArray(new NtbParamVO[0]);
		} catch (Exception ex) {
			NtbLogger.error(ex);
			throw ex;
		}
	}

	private IBusiSysExecDataProvider getExcProvider(String sys) throws BusinessException {
		IBusiSysExecDataProvider provider = null;
			try {
				BusiSysReg sysreg = BusiSysReg.getSharedInstance();
				IdSysregVO[] sysregvos = sysreg.getAllSysVOs();
				for (int i = 0; i < sysregvos.length; i++) {
					if (sysregvos[i].getSysid().equals(sys)) {
						provider = ((IBusiSysReg) Class.forName(sysregvos[i].getRegclass()).newInstance()).getExecDataProvider();
					}
				}
			} catch (Exception ex) {
				// ex.printStackTrace();
				NtbLogger.printException(ex);
				throw new BusinessException(ex);
			}
			return provider;
    }

	public NtbParamVO[] getExcProviderFunc(String sys,NtbParamVO[] params) throws BusinessException {
		IBusiSysExecDataProvider provider = getExcProvider(sys);
		try{
	    	setIncludeEff(provider,params);
		}catch(Exception ex){
			NtbLogger.print(ex);
		}
		return params;
	}


	// 从业务系统查询控制点，判断参数是否包含未生效单据
	private NtbParamVO[] setIncludeEff(IBusiSysExecDataProvider exeprovider,
			NtbParamVO[] params) throws Exception {
		HashMap hashCorp2Point = new HashMap();
		for (int i = 0; i < params.length; i++) {
			int ctlpoint = 0;
			boolean isIncludeeff = false;

			if (hashCorp2Point.containsKey(params[i].getPk_Org())) {
				ctlpoint = ((Integer) hashCorp2Point.get(params[i].getPk_Org()))
						.intValue();
			} else {
				try {
					ctlpoint = exeprovider.getCtlPoint(params[i].getPk_Org());
				} catch (Exception ex) {
					NtbLogger.error(ex);
					ctlpoint = 0;
				}
				hashCorp2Point
						.put(params[i].getPk_Org(), Integer.valueOf(ctlpoint));
			}

			if (ctlpoint == 0) {// 保存阶段
				isIncludeeff = true;
			} else if (ctlpoint == 1) {// 审核阶段
				isIncludeeff = false;
			}
			params[i].setIsUnInure(isIncludeeff);
		}

		return params;
	}

	public HashMap setFormulaMap(IdCtrlformulaVO vo, IdCtrlschemeVO[] vos,HashMap map) {
		if (map.containsKey(vo.getPrimaryKey())) {
			ArrayList list = (ArrayList) map.get(vo.getPrimaryKey());
			for (int i = 0; i < vos.length; i++) {
				list.add(vos[i]);
			}
		} else {
			ArrayList list = new ArrayList();
			for (int i = 0; i < vos.length; i++) {
				list.add(vos[i]);
			}
			map.put(vo.getPrimaryKey(), list);
		}
		return map;
	}

	private String getArraysStr(String[] values){
		StringBuffer sbStr = new StringBuffer();
		for(int n=0;n<values.length;n++){
			if(n==values.length-1){
				sbStr.append(values[n]);
			}else{
				sbStr.append(values[n]).append(":");
			}
		}
		return sbStr.toString();
	}

	public String[] startCtrlScheme(ArrayList<CtrlSchemeVO> vos) throws BusinessException {
		ArrayList<IdCtrlformulaVO> formulavoList = new ArrayList<IdCtrlformulaVO> ();
		ArrayList<IdCtrlschemeVO>  schemevoList = new ArrayList<IdCtrlschemeVO> ();
		HashMap<String, ArrayList<IdCtrlschemeVO>> m_map = new HashMap<String, ArrayList<IdCtrlschemeVO>>();
		HashMap<String, IdCtrlformulaVO> n_map = new HashMap<String, IdCtrlformulaVO>();
		ArrayList<String> infolist = new ArrayList<String>();
		HashMap<IdCtrlformulaVO, IdCtrlschemeVO[]> mapVos = new HashMap<IdCtrlformulaVO, IdCtrlschemeVO[]>();
		CostTime time = new CostTime();
		for(int n=0;n<vos.size();n++){
			CtrlSchemeVO vo = vos.get(n);
			String pk_formula = vo.getPk_formula()	;
			String pk_task = vo.getPk_task();
			DataCell datacell = vo.getAllotCell();
			String express = vo.getCellExpress();
			/**如果选择多个会计科目,支持在这里进行分解,其他地方不用操作*/
			express = CtrlRuleCTL.deposeRuleExpress(express);

			/**end*/
			try{
		    	DimFormulaVO dimformulaVO = RuleCacheManager.getNewInstance().getDimFormulaVOByPk(pk_formula);
		    	SingleSchema schemea = null;
		    	if(dimformulaVO == null)
		    		throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl003-0001")/*@res "没有找到控制规则："*/ + pk_formula + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl003-0002")/*@res "请刷新界面后重试"*/);
		    	if(dimformulaVO.getPk_ruleclass().equals(IRuleClassConst.SCHEMA_SINGLE)||dimformulaVO.getPk_ruleclass().equals(IRuleClassConst.SCHEMA_SPEC)){
					schemea = CtlSchemeCTL.singleAndSepSchema(express, datacell, datacell.getCubeDef(),dimformulaVO.getPk_ruleclass());
				}else if(dimformulaVO.getPk_ruleclass().equals(IRuleClassConst.SCHEMA_GROUP)){
					schemea = CtlSchemeCTL.groupSchema(express, datacell, datacell.getCubeDef());
				}else{
					schemea = CtlSchemeCTL.singleAndSepSchema(express, datacell, datacell.getCubeDef(),dimformulaVO.getPk_ruleclass());
				}
		    	ArrayList<IdCtrlschemeVO> lm = CtlSchemeCTL.convertIdCtrlscheme(schemea,pk_formula); //将公式转换为子表VO
				IdCtrlschemeVO[] schemevos = lm.toArray(new IdCtrlschemeVO[0]);
				vo.setSchemevos(schemevos);
				IdCtrlformulaVO formulavo = CtlSchemeCTL.convertIdCtrlFormula(datacell,schemea,schemevos,pk_formula); //将公式转换为主表VO
				formulavo.setPk_plan(pk_task);
				vo.setFormulavo(formulavo);
				formulavoList.add(formulavo);
			}catch(Exception ex){
				throw new BusinessException(ex.getMessage(),ex);
			}
		}
		time.printStepCost(vos.size()+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0","01050rule001-0040")/*@res "个公式进行实例化,花费:"*/);
		try{
		    String[] pks = CtlSchemeCTL.addCtrlformulas(formulavoList);
		    /**vos肯定是跟formulavoList同步*/
		    for(int n=0;n<vos.size();n++){
		    	CtrlSchemeVO vo = vos.get(n);
		    	for(int m=0;m<(vo.getSchemevos()==null?0:vo.getSchemevos().length);m++){
		    		vo.getSchemevos()[m].setPk_ctrlformula(pks[n]);
		    		vo.getSchemevos()[m].setPk_plan(vo.getPk_task());
		    	}
		    	schemevoList.addAll(Arrays.asList(vo.getSchemevos()));
				mapVos.put(vo.getFormulavo(), vo.getSchemevos());
		    }
		    String[] _pks = CtlSchemeCTL.addCtrlScheme(schemevoList);
		    for(int n=0;n<schemevoList.size();n++){
		    	IdCtrlschemeVO vo = schemevoList.get(n);
		    	vo.setPrimaryKey(_pks[n]);
		    }
		    time.printStepCost(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0","01050rule001-0041")/*@res "插入控制方案数据,花费:"*/);
			if(formulavoList.size()>0){
					Iterator iter = mapVos.entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry entry = (Map.Entry) iter.next();
						IdCtrlformulaVO key = (IdCtrlformulaVO) entry.getKey();
						IdCtrlschemeVO[] schemeVOS = (IdCtrlschemeVO[]) entry.getValue();
						for (int n = 0; n < schemeVOS.length; n++) {
							schemeVOS[n].setPk_ctrlformula(key.getPrimaryKey());
						}
						setFormulaMap(key, schemeVOS, m_map);
						n_map.put(key.getPrimaryKey(), key);
					}
			}
		AlertPercentHandler.updSectionsWithCtrlScheme(vos.toArray(new CtrlSchemeVO[0]));
		HashMap paraMap = CtlSchemeCTL.sortVOsBySys(schemevoList.toArray(new IdCtrlschemeVO[0]));
		Iterator itor = paraMap.keySet().iterator();
		while (itor.hasNext()) {
			String src = (String) itor.next();
			ArrayList ls = (ArrayList) paraMap.get(src);
			NtbParamVO[] params = (NtbParamVO[]) ls.toArray(new NtbParamVO[0]);
			IBusiSysExecDataProvider exeprovider = getExcProvider(src);
			// 取得业务系统的控制阶段，设置参数的是否包含未生效单据字段
			CtlSchemeCTL.setIncludeEff(exeprovider, params);
			/**
			 * UFind()公式取数不应该根据公司取是否包含未生效数据，根据手工设置的，在parseCtrl()方法中已经设置
			 * */
			ArrayList<NtbParamVO> ufindVO = new ArrayList<NtbParamVO>();
			ArrayList<NtbParamVO> prefindVO = new ArrayList<NtbParamVO>();
			for (int n = 0; n < params.length; n++) {
				if (params[n].getMethodCode().equals("UFIND")) {
					ufindVO.add(params[n]);
				} else if (params[n].getMethodCode().equals("PREFIND")) {
					prefindVO.add(params[n]);
				}
			}
			nc.vo.pub.lang.UFDouble[][] ufindretdatas = null;
			nc.vo.pub.lang.UFDouble[][] prefindretdatas = null;
			if (exeprovider instanceof IBusiSysExecAllDataProvider) {
				((IBusiSysExecAllDataProvider) exeprovider)
						.setAllNtbParamVO(params);
			}

			if (ufindVO.size() > 0) {
				NtbParamVO[] vosx = ufindVO.toArray(new NtbParamVO[0]);
				ufindretdatas=groupGetExeData(vosx,exeprovider,false);
				if(ufindretdatas==null){
					ufindretdatas = exeprovider.getExecDataBatch(vosx);

				}

			}

			if (prefindVO.size() > 0) {
				NtbParamVO[] vosx = prefindVO.toArray(new NtbParamVO[0]);
				prefindretdatas=groupGetExeData(vosx,exeprovider,true);
				if(prefindretdatas==null){
					prefindretdatas = exeprovider.getReadyDataBatch(vosx);
				}
			}

			if (ufindretdatas != null) {
				for (int j = 0; j < ufindVO.size(); j++) {
					ufindVO.get(j).setRundata(ufindretdatas[j]);
				}
			}
			if (prefindretdatas != null) {
				for (int j = 0; j < prefindVO.size(); j++) {
					prefindVO.get(j).setReadydata(prefindretdatas[j]);
				}
			}

			ArrayList<NtbParamVO> allNtbParamVO = new ArrayList<NtbParamVO>();
			allNtbParamVO.addAll(ufindVO);
			allNtbParamVO.addAll(prefindVO);

			params = allNtbParamVO.toArray(new NtbParamVO[0]);

			NtbParamVO[] mergeParamVos = params;
			String[] info = null;
			// info为其中一个系统的控制信息
			info = CtlSchemeCTL.compare(mergeParamVos, m_map, n_map,vos);
			for (int m = 0; m < info.length; m++) {
				infolist.add(info[m]);
			}
			CtlSchemeCTL.updateCtrlSchemeTable(mergeParamVos);
			time.printStepCost(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0","01050rule001-0045")/*@res "更新数据花费"*/);
		   }
		}catch(Exception ex){
			throw new BusinessException(ex);
		}

		return (String[]) infolist.toArray(new String[0]);




	}

	public void startCtrlSchemeBySchemeVO(List<IdCtrlschemeVO> schemevoList) throws BusinessException {
	    try
		 {
			HashMap paraMap = CtlSchemeCTL.sortVOsBySys(schemevoList.toArray(new IdCtrlschemeVO[0]));
			Iterator itor = paraMap.keySet().iterator();
			while (itor.hasNext()) {
				String src = (String) itor.next();
				ArrayList ls = (ArrayList) paraMap.get(src);
				NtbParamVO[] params = (NtbParamVO[]) ls.toArray(new NtbParamVO[0]);
				IBusiSysExecDataProvider exeprovider = getExcProvider(src);
				// 取得业务系统的控制阶段，设置参数的是否包含未生效单据字段
				CtlSchemeCTL.setIncludeEff(exeprovider, params);
				/**
				 * UFind()公式取数不应该根据公司取是否包含未生效数据，根据手工设置的，在parseCtrl()方法中已经设置
				 * */
				ArrayList<NtbParamVO> ufindVO = new ArrayList<NtbParamVO>();
				ArrayList<NtbParamVO> prefindVO = new ArrayList<NtbParamVO>();
				for (int n = 0; n < params.length; n++) {
					if (params[n].getMethodCode().equals("UFIND")) {
						ufindVO.add(params[n]);
					} else if (params[n].getMethodCode().equals("PREFIND")) {
						prefindVO.add(params[n]);
					}
				}
				nc.vo.pub.lang.UFDouble[][] ufindretdatas = null;
				nc.vo.pub.lang.UFDouble[][] prefindretdatas = null;
				if (exeprovider instanceof IBusiSysExecAllDataProvider) {
					((IBusiSysExecAllDataProvider) exeprovider)
							.setAllNtbParamVO(params);
				}

				if (ufindVO.size() > 0) {
					NtbParamVO[] vos = ufindVO.toArray(new NtbParamVO[0]);
					ufindretdatas=groupGetExeData(vos,exeprovider,false);
					if(ufindretdatas==null){
						ufindretdatas = exeprovider.getExecDataBatch(vos);

					}

				}

				if (prefindVO.size() > 0) {
					NtbParamVO[] vos = prefindVO.toArray(new NtbParamVO[0]);
					prefindretdatas=groupGetExeData(vos,exeprovider,true);
					if(prefindretdatas==null){
						prefindretdatas = exeprovider.getReadyDataBatch(vos);
					}
				}

				if (ufindretdatas != null) {
					for (int j = 0; j < ufindVO.size(); j++) {
						ufindVO.get(j).setRundata(ufindretdatas[j]);
					}
				}
				if (prefindretdatas != null) {
					for (int j = 0; j < prefindVO.size(); j++) {
						prefindVO.get(j).setReadydata(prefindretdatas[j]);
					}
				}

				ArrayList<NtbParamVO> allNtbParamVO = new ArrayList<NtbParamVO>();
				allNtbParamVO.addAll(ufindVO);
				allNtbParamVO.addAll(prefindVO);

				params = allNtbParamVO.toArray(new NtbParamVO[0]);

				NtbParamVO[] mergeParamVos = params;

				CtlSchemeCTL.updateCtrlSchemeTable(mergeParamVos);

			   }
		 }catch(Exception ex){
			throw new BusinessException(ex);
		 }
	}

	public NtbParamVO[] getExeData(String[] formulaExpress) throws BusinessException {
		try {
			CountTimeCost ufindGetData = new CountTimeCost();
			ufindGetData.beginCost();
			ArrayList<NtbParamVO> paramvos = new ArrayList();
			ArrayList<IdCtrlschemeVO> schemvos = new ArrayList();
			ArrayList<String> pkList = new ArrayList();
			HashMap<String, ArrayList<IdCtrlschemeVO>> map_cell = new HashMap();
			ArrayList<Integer> error_list = new ArrayList<Integer>();

			Map<Integer, IdCtrlschemeVO[]> schemeMap = new HashMap<Integer, IdCtrlschemeVO[]>();
			for (int i = 0; i < formulaExpress.length; i++) {
				String express = formulaExpress[i];
				SingleSchema schema = new SingleSchema(express);
				// 按照公式表达式中UFind的顺序返回

				IdCtrlschemeVO[] vos = convertIdCtrlscheme(schema);

				ufindGetData.addCost("Ufind get data 0.5", ufindGetData.getCost());

				for(IdCtrlschemeVO vo : vos)
					schemvos.add(vo);

				schemeMap.put(Integer.valueOf(i), vos);
			}

			IdCtrlschemeVO[] vos = (IdCtrlschemeVO[]) schemvos
					.toArray(new IdCtrlschemeVO[0]);
			/** 包括公司控制下级的NtbParamVO */
			HashMap paraMap = sortVOsBySys(schemeMap);

			Iterator itor = paraMap.keySet().iterator();
			ufindGetData.addCost("Ufind get data 1", ufindGetData.getCost());
			while (itor.hasNext()) {
				String src = (String) itor.next();
				ArrayList ls = (ArrayList) paraMap.get(src);
				NtbParamVO[] params = (NtbParamVO[]) ls
						.toArray(new NtbParamVO[0]);
				ufindGetData.addCost("Ufind get data 2", ufindGetData.getCost());
				CountTimeCost expressCost = new CountTimeCost();
				expressCost.beginCost();

				IBusiSysExecDataProvider exeprovider = getExcProvider(src);

				expressCost.addCost("Ufind get service", expressCost.getCost());

				/**
				 * UFind()公式取数不应该根据公司取是否包含未生效数据，根据手工设置的，在parseCtrl()方法中已经设置
				 * */
				ArrayList<NtbParamVO> ufindVO = new ArrayList<NtbParamVO>();
				ArrayList<NtbParamVO> prefindVO = new ArrayList<NtbParamVO>();
				for (int n = 0; n < params.length; n++) {
					if (params[n].getMethodCode().equals("UFIND")) {
						ufindVO.add(params[n]);
					} else if (params[n].getMethodCode().equals("PREFIND")) {
						prefindVO.add(params[n]);
					}
				}
				nc.vo.pub.lang.UFDouble[][] ufindretdatas = null;
				nc.vo.pub.lang.UFDouble[][] prefindretdatas = null;
				ufindGetData.addCost("Ufind get data 3", ufindGetData.getCost());

				try {
					long start = System.currentTimeMillis();
					if (ufindVO.size() > 0) {
						NtbParamVO[] vosx = ufindVO.toArray(new NtbParamVO[0]);
						ufindretdatas=groupGetExeData(vosx,exeprovider,false);
						if(ufindretdatas==null){
							ufindretdatas = exeprovider.getExecDataBatch(vosx);

						}

					}

					if (prefindVO.size() > 0) {
						NtbParamVO[] vosx = prefindVO.toArray(new NtbParamVO[0]);
						prefindretdatas=groupGetExeData(vosx,exeprovider,true);
						if(prefindretdatas==null){
							prefindretdatas = exeprovider.getReadyDataBatch(vosx);
						}
					}
				} catch (BusinessException ex) {
					/** 如果业务系统有错误,提示处理 */
					NtbLogger.error(ex);
					String sysid = null;
					String formulaName = null;
					if (ufindVO.size() > 0) {
						sysid = ufindVO.get(0).getSys_id();
						// formulaName = ufindVO.get(0)
					} else if (prefindVO.size() > 0) {
						sysid = prefindVO.get(0).getSys_id();
					}
					throw new BusinessException(NCLangRes4VoTransl
							.getNCLangRes().getStrByID(
									"tbb_ctrl",
									"01801ctl_000214",
									null,
									new String[] {
											(String) CtlBdinfoCTL
													.getSelectSystem(sysid)
													.getSysname(),
											ex.getMessage() })/*
															 * [{0}]业务系统执行取数公式出错.
															 * \n详细错误信息:\n{1}
															 */);
				}

				for (int j = 0; j < ufindVO.size(); j++) {
					ufindVO.get(j).setRundata(ufindretdatas[j]);

				}
				for (int j = 0; j < prefindVO.size(); j++) {
					prefindVO.get(j).setReadydata(prefindretdatas[j]);
				}

				ArrayList<NtbParamVO> allNtbParamVO = new ArrayList<NtbParamVO>();
				allNtbParamVO.addAll(ufindVO);
				allNtbParamVO.addAll(prefindVO);

				params = allNtbParamVO.toArray(new NtbParamVO[0]);

				// 先按找公司控制下级和控制所有公司合并NtbParamVO
				// NtbParamVO[] mergeParamVos = mergeParamVO(params);
				for (int i = 0; i < params.length; i++) {
					paramvos.add(params[i]);
				}
			}
			// NtbParamVO按照不同系统取数后，需要按照传入的
			NtbParamVO[] sma = (NtbParamVO[]) paramvos
					.toArray(new NtbParamVO[0]);
			ArrayList sortsVO = sortNtbParamVO(sma);
			ufindGetData.addCost("Ufind get data 5", ufindGetData.getCost());
			//AccountQryCache.getInstance().clearCache();
			DataGetterContext.getInstance().clearContext();
			return (NtbParamVO[]) sortsVO.toArray(new NtbParamVO[0]);
		} catch (Exception ex) {
			NtbLogger.error(ex);
			throw new BusinessException(
					ex.getMessage() == null ? NCLangRes4VoTransl.getNCLangRes()
							.getStrByID("tbb_ctrl", "01801ctl_000215")/* 业务系统执行取数公式出错 */
					: ex.getMessage(),ex);
		}

	}


//	private UFDouble[][] mergeNtbParam(UFDouble[][] values,ArrayList<NtbParamVO> newListvo) {
//		/**values的长度应该跟newListvo一样,这里需要合并*/
//		ArrayList<UFDouble[]> retdatas = new ArrayList<UFDouble[]> ();
//		UFDouble[] newValues = new UFDouble[] {UFDouble.ZERO_DBL,UFDouble.ZERO_DBL,UFDouble.ZERO_DBL,UFDouble.ZERO_DBL};
//		for(int n=0;n<newListvo.size();n++){
//			NtbParamVO vo = newListvo.get(n);
//			/**存在下一个的情况*/
//			  NtbParamVO nextvo = null;
//			if(n+1<newListvo.size()){
//				nextvo = newListvo.get(n+1);
//			}
//			String ntbparamvoId = vo.getNtbparamvoId();
//			String nextntbparamvoid = nextvo==null?null:nextvo.getNtbparamvoId();
//			if(ntbparamvoId==null||"".equals(ntbparamvoId)){
//				retdatas.add(values[n]);
//				continue;
//			}else if(ntbparamvoId!=null&&!"".equals(ntbparamvoId)&&ntbparamvoId.equals(nextntbparamvoid)){
//				newValues[0].add(values[n][0]);
//				newValues[1].add(values[n][1]);
//				newValues[2].add(values[n][2]);
//				newValues[3].add(values[n][3]);
//			}else if(ntbparamvoId!=null&&!"".equals(ntbparamvoId)&&!ntbparamvoId.equals(nextntbparamvoid)){
//				newValues[0].add(values[n][0]);
//				newValues[1].add(values[n][1]);
//				newValues[2].add(values[n][2]);
//				newValues[3].add(values[n][3]);
//				retdatas.add(newValues);
//				newValues = new UFDouble[] {UFDouble.ZERO_DBL,UFDouble.ZERO_DBL,UFDouble.ZERO_DBL,UFDouble.ZERO_DBL};
//			}
//
//		}
//		return retdatas.toArray(new UFDouble[0][]);
//	}
//
//
//
//	private ArrayList<NtbParamVO> decomposeNtbParam(ArrayList<NtbParamVO> listvo){
//		ArrayList<NtbParamVO> newListvo = new ArrayList<NtbParamVO> ();
//		for(int n=0;n<listvo.size();n++){
//		  NtbParamVO vo = listvo.get(n);
//		  String[] pkdims = vo.getPkDim();
//		  boolean isMultNtbVO = false;
//		  String multPk = "";
//		  String multCode = "";
////		  String multName = "";
//		  int location = 0;
//		  for(int m=0;m<pkdims.length;m++){
//			  String pkdim = pkdims[m];
//			  if(pkdim.indexOf("#")>0){
//				  isMultNtbVO = true;
//				  multPk = pkdim;
//				  multCode = vo.getCode_dims()[m];
////				  multName = vo.getTypeDim()[m];
//				  location = m;
//			  }
//		  }
//		  /**isMultNtbVO为true,说明需要分解*/
//		  if(isMultNtbVO){
//			  String[] pks = multPk.split("#");
//			  String[] codes = multCode.split("#");
////			  String[] names = multName.split("@");
//			  for(int m=0;m<pks.length;m++){
//				  NtbParamVO newvo = (NtbParamVO)vo.clone();
//				  String[] srcDims = newvo.getPkDim();
//				  String[] codeDims = newvo.getCode_dims();
//				  String[] nameDims = newvo.getTypeDim();
//				  if(srcDims!=null){
//						String[] cloneDims = new String[srcDims.length];
//						String[] cloneCodeDims = new String[codeDims.length];
//						String[] cloneNameDims = new String[nameDims.length];
//						System.arraycopy(srcDims, 0, cloneDims, 0, srcDims.length);
//						System.arraycopy(codeDims, 0, cloneCodeDims, 0, codeDims.length);
//						System.arraycopy(nameDims, 0, cloneNameDims, 0, nameDims.length);
//						cloneDims[location]=pks[m];
//						cloneCodeDims[location]=codes[m];
////						cloneNameDims[location]=names[m];
//						newvo.setPkDim(cloneDims);
//						newvo.setCode_dims(cloneCodeDims);
//						newvo.setTypeDim(cloneNameDims);
//
//				  }
//				  newvo.setNtbparamvoId("NTBPARAMVO"+n);
//				  newListvo.add(newvo);
//			  }
//		  }else{
//			  newListvo.add(vo);
//		  }
//		}
//		return newListvo;
//	}

	public ArrayList sortNtbParamVO(NtbParamVO[] vos) {
		Map<String, NtbParamVO> realParamMap = new HashMap<String, NtbParamVO>();
		for(NtbParamVO vo : vos) {
			if(realParamMap.containsKey(vo.getNtbparamvoId())) {
				NtbParamVO vo0 = realParamMap.get(vo.getNtbparamvoId());
				vo0.add(vo, vo0.getMethodCode());
			} else {
				realParamMap.put(vo.getNtbparamvoId(), vo);
			}
		}
		NtbParamVO[] ntbParamArr = new NtbParamVO[realParamMap.keySet().size()];
		ArrayList<NtbParamVO> linkedParamList = new ArrayList<NtbParamVO>();
		for(Map.Entry<String, NtbParamVO> entry : realParamMap.entrySet()) {
			int index = Integer.parseInt(entry.getKey());
			ntbParamArr[index] = entry.getValue();
		}
		for(NtbParamVO vo : ntbParamArr) linkedParamList.add(vo);
		return linkedParamList;
	}

	public String getExeFormulaExpress(DataCell cell, String exeExpress, String funcName) throws BusinessException {
		String express = null;
		try {


		    express = CtrlRuleCTL.getExeFormulaExpress(cell, exeExpress.toString(),  funcName);
		}catch(Exception ex){
//			throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("tbb_rule", "01420rule-000017")/*错误*/);
			throw new BusinessException(ex);
		}
		return express;
	}

	/**
	 * add by dengyh 2010-12-09 得到公式对应VO
	 */
	public TreeMap<Integer, ArrayList<NtbParamVO>> getUfNtbParamVOs(
			String[] formulaExpress) throws BusinessException {
		try {
			TreeMap<Integer, ArrayList<NtbParamVO>> ufNtbVoMap = new TreeMap<Integer, ArrayList<NtbParamVO>>();

			for (int i = 0; i < formulaExpress.length; i++) {
				ArrayList<IdCtrlschemeVO> schemvos = new ArrayList();
				String express = formulaExpress[i];
				SingleSchema schema = new SingleSchema(express);
				// 按照公式表达式中UFind的顺序返回
				IdCtrlschemeVO[] ctrlVos = convertIdCtrlscheme(schema);
				for(IdCtrlschemeVO ctrlVo : ctrlVos)
					schemvos.add(ctrlVo);
				IdCtrlschemeVO[] vos = (IdCtrlschemeVO[]) schemvos
						.toArray(new IdCtrlschemeVO[0]);
				ArrayList<NtbParamVO> voList = sortVOsByUf(vos);
				ufNtbVoMap.put(Integer.valueOf(i), voList);
			}
			return ufNtbVoMap;
		} catch (BusinessException be) {
			throw be;
		} catch (Exception e) {
			throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("tbb_rule", "01420rule-000183")/*不存在业务系统取数公式*/);
		}
	}

	/**
	 * add by dengyh 2010-12-09 得到公式对应VO
	 */
	private ArrayList<NtbParamVO> sortVOsByUf(IdCtrlschemeVO[] ctlvos)
			throws Exception {
		try {
			// 构造NtbParamVO,包括公司控制下级的paramvo
			ArrayList<NtbParamVO> voList = new ArrayList<NtbParamVO>();
			NtbParamVO[] params = parseCtrls(ctlvos,null);
			for (int i = 0; i < params.length; i++) {
				voList.add(params[i]);
			}
			return voList;
		} catch (Exception ex) {
			NtbLogger.error(ex);
			throw ex;
		}

	}

	public NtbParamVO[] getExeDataByVO(String sys, NtbParamVO[] vos) throws BusinessException {
			UFDouble[][] ufData = null;
			UFDouble[][] ufReadyData = null;
			NtbParamVO[] parms = vos;
			if (parms != null && parms.length != 0) {

				ArrayList<NtbParamVO> ufindVO = new ArrayList<NtbParamVO>();
				ArrayList<NtbParamVO> prefindVO = new ArrayList<NtbParamVO>();
				IBusiSysExecDataProvider exeprovider = getExcProvider(sys);

				// setIncludeEff(exeprovider, parms);
				for (int n = 0; n < vos.length; n++) {
					if (vos[n].getMethodCode().equals("UFIND")) {
						ufindVO.add(vos[n]);
					} else if (vos[n].getMethodCode().equals("PREFIND")) {
						prefindVO.add(vos[n]);
					}
				}

				if (exeprovider != null) {
					if (ufindVO.size() > 0) {
						NtbParamVO[] vos2 = ufindVO.toArray(new NtbParamVO[0]);
						ufData=groupGetExeData(vos2,exeprovider,false);
						if(ufData==null){
							ufData = exeprovider.getExecDataBatch(vos2);
						}

					}

					if (prefindVO.size() > 0) {
						NtbParamVO[] vos2 = prefindVO.toArray(new NtbParamVO[0]);
						ufReadyData=groupGetExeData(vos2,exeprovider,true);
						if(ufReadyData==null){
							ufReadyData = exeprovider.getReadyDataBatch(vos2);

						}
					}
					for (int i = 0; i < ufindVO.size(); i++) {


						ufindVO.get(i).setRundata(ufData[i]);
					}
					for (int j = 0; j < prefindVO.size(); j++) {
						prefindVO.get(j).setReadydata(ufReadyData[j]);
					}
					ArrayList<NtbParamVO> allNtbParamVO = new ArrayList<NtbParamVO>();
					allNtbParamVO.addAll(ufindVO);
					allNtbParamVO.addAll(prefindVO);
					parms = allNtbParamVO.toArray(new NtbParamVO[0]);
				}
			}
      return parms;
   }

	public ArrayList<IdCtrlformulaVO> getPlanStartAndStopCtrlformulaVO(String pk_cube) throws BusinessException {
		return CtlSchemeCTL.getPlanStartAndStopCtrlformulaVO(pk_cube);
	}


	public void deleteTempTable(String name) throws Exception, BusinessException, /* SystemException, */NamingException {
         NtbSuperDMO.executeUpdate("drop table " + name);
    }

	public HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> queryCtrlScheme(
			String sWhere) throws BusinessException {
		try {
			HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> returnvo = new HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>>();

			HashMap<String, IdCtrlformulaVO> tempMap = new HashMap<String, IdCtrlformulaVO>();
			NtbSuperDMO dmo = new NtbSuperDMO();

			SuperVO[] vos_parent = dmo.queryByWhereClause(IdCtrlformulaVO.class, sWhere);
			ArrayList<String> pkList = new ArrayList<String>();

			// liming 2009.11.12 cube map
			Map<String, String> cubeMap = new HashMap<String, String>();
			ArrayList<ArrayList> cubeList = new ArrayList<ArrayList>();

			if (vos_parent != null && vos_parent.length > 0) {

				for (int i = 0; i < vos_parent.length; i++) {
					IdCtrlformulaVO parentVO = (IdCtrlformulaVO) vos_parent[i];
					ArrayList<String> tmpList = new ArrayList<String> ();
					
					tmpList.add(parentVO.getPrimaryKey());
					tmpList.add(parentVO.getPrimaryKey());
					cubeList.add(tmpList);
					
					pkList.add(parentVO.getPrimaryKey());
					tempMap.put(parentVO.getPrimaryKey(), parentVO);
					cubeMap.put(parentVO.getPk_cube(), null);
				}
				
				String tmpTableName = "NTB_TMP_FORMUAL_"+RandomStringUtils.randomNumeric(3);
				tmpTableName = CtlSchemeCTL.createNtbTempTable_new(null,tmpTableName, cubeList);
				
//				String[] sqls = SqlPartlyTools.getSqls("pk_ctrlformula",(String[]) cubeList.toArray(new String[0]));
//				StringBuffer sql = new StringBuffer();
//				for (int i = 0; i < sqls.length; i++) {
//					if (i == 0) {
//						sql.append(sqls[i]);
//					} else {
//						sql.append("or");
//						sql.append(sqls[i]);
//					}
//
//				}

				StringBuffer sWhere_cube = new StringBuffer();
								sWhere_cube.append("pk_ctrlformula in (");
								sWhere_cube.append("select DATACELLCODE from ").append(tmpTableName);
								sWhere_cube.append(")");
		
				
				SuperVO[] vos_children = dmo.queryByWhereClause(IdCtrlschemeVO.class, sWhere_cube.toString()); 

				for (int i = 0; i < vos_children.length; i++) {
					IdCtrlschemeVO childrenVO = (IdCtrlschemeVO) vos_children[i];
					if (tempMap.containsKey(childrenVO.getPk_ctrlformula())) {
						IdCtrlformulaVO tempvo = tempMap.get(childrenVO
								.getPk_ctrlformula());
						if (returnvo.containsKey(tempvo)) {
							ArrayList<IdCtrlschemeVO> schmvols = returnvo
									.get(tempvo);
							schmvols.add(childrenVO);
						} else {
							ArrayList<IdCtrlschemeVO> schmvols = new ArrayList<IdCtrlschemeVO>();
							schmvols.add(childrenVO);
							returnvo.put(tempvo, schmvols);
						}
					}
				}

			}
			return returnvo;

		} catch (Exception e) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("tbb_ctrl", "01801ctl_000052")/* 未知异常 */);
		}
	}

	/**
	 * 删除控制方案VO
	 * */
	public void deleteCtrlScheme(
			Map<String, List<String>> map)
			throws BusinessException {
		try {
			NtbSuperDMO dmo = new NtbSuperDMO();

			ArrayList<String> parentList = new ArrayList<String>();
			ArrayList<String> childrenList = new ArrayList<String>();
			Iterator<String> iteraKey = map.keySet().iterator();

			while (iteraKey.hasNext()) {
				String parentVOPk = iteraKey.next();
//				/** 如果是弹性规则的话,需要删除ntb_id_flexelement表中的数据 */
//				if (parentVO.getSchemetype()
//						.equals(IRuleClassConst.SCHEMA_FLEX)) {
//					SuperVO[] vos = dmo.queryByWhereClause(
//							IdFlexElementVO.class,
//							"pk_formula = '" + parentVO.getPrimaryKey() + "'");
//					for (int n = 0; n < (vos == null ? 0 : vos.length); n++) {
//						IdFlexElementVO vo = (IdFlexElementVO) vos[n];
//						dmo.delete(vo);
//					}
//				}
				parentList.add(parentVOPk);
				List<String> templist = map.get(parentVOPk);
				childrenList.addAll(templist);
			}

			dmo.deleteArrayByPKs(IdCtrlformulaVO.class, parentList.toArray(new String[parentList.size()]));
			dmo.deleteArrayByPKs(IdCtrlschemeVO.class, childrenList.toArray(new String[childrenList.size()]));

			AlertPercentHandler.updAlertPercentWhenStop(parentList);

		} catch (Exception e) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("tbb_ctrl", "01801ctl_000052")/* 未知异常 */);
		}
	}
	
	public String createTempTable(String tableName, List<String> insertData) throws DAOException {
		
		String tablename=createTempTable(tableName);
		insertTemp(insertData, tablename);
		return tablename;
	}
	
	public String insertTemp(List<String> insertData, String tableName) throws DAOException {
		PersistenceManager manager = null;
		try {
			manager = PersistenceManager.getInstance();
			JdbcSession session = manager.getJdbcSession();
			session.setAddTimeStamp(false);
			String sql = "insert into " + tableName + "(pk) values(?)";
			for (String uk : insertData) {
				SQLParameter sp = new SQLParameter();
				sp.addParam(uk);
				session.addBatch(sql, sp);
			}
			session.executeBatch();
		} catch (DbException dbe) {
			throw new DAOException(dbe);
		} finally {
			if (manager != null)
				manager.release();
		}
		return tableName;
	}
	
	public String createTempTable(String tableName) {
		String vtn = null;
		JdbcSession session = null;
		try {
			session = new JdbcSession();
			String para2 = tableName;
			String para3 = "pk varchar(1000) not null";
			String para4 = "pk";
			vtn = new TempTable().createTempTable(session.getConnection(), para2, para3, para4);
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
		} catch (SQLException e) {
			Logger.error(e.getMessage(), e);
		} finally {
			if (session != null) {
				session.closeAll();
			}
		}
		return vtn;
	}

	public HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> createNtbTempTable(CubeDef cube, String name,
			ArrayList<ArrayList> listTempTableValue) throws Exception,
			BusinessException, /* SystemException, */NamingException {
		NtbSuperDMO demo = new NtbSuperDMO();
		StringBuffer sWhere_plan = new StringBuffer();
		String sTempTableName = null;
		sTempTableName = demo.getTempStringTable(name, new String[] {
				"DATACELLID", "DATACELLCODE" }, new String[] {
				"char(2000) not null ", "varchar(2000) " }, null,
				listTempTableValue);

		StringBuffer sWhere_cube = new StringBuffer();
		sWhere_cube.append("pk_dimvector in (");
		sWhere_cube.append("select DATACELLCODE from ").append(sTempTableName);
		sWhere_cube.append(") and isstarted = 'Y' and pk_cube = '");
		sWhere_cube.append(cube.getPrimaryKey());
		sWhere_cube.append("' and pk_parent is not null");
		HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> ctlmap_cube= queryCtrlScheme(sWhere_cube.toString());
		return ctlmap_cube;
	}

	public void createBillType(NtbParamVO[] paramvs, String syscode) throws BusinessException {
			IBusiSysExecDataProvider exeprovider = null;
			try {
				exeprovider = getExcProvider(syscode);
			} catch (Exception ex) {
				throw new BusinessException(NCLangRes4VoTransl.getNCLangRes()
						.getStrByID("tbb_ctrl", "01801ctl_000217", null,
								new String[] { syscode })/* 获取{0}系统推演生成单据实现类出错,请检查 */);
			}
			exeprovider.createBillType(paramvs);
     }

	public void updateCtrlSchemeVOs(
			HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> notStartCtrlscheme)
			throws BusinessException {
		try {
			NtbSuperDMO dmo = new NtbSuperDMO();

			ArrayList<IdCtrlformulaVO> parentls = new ArrayList<IdCtrlformulaVO>();
			ArrayList<IdCtrlschemeVO> childrenls = new ArrayList<IdCtrlschemeVO>();

			Iterator<IdCtrlformulaVO> iteraKey = notStartCtrlscheme.keySet()
					.iterator();
			Iterator<ArrayList<IdCtrlschemeVO>> iteraValue = notStartCtrlscheme
					.values().iterator();

			while (iteraKey.hasNext()) {
				IdCtrlformulaVO parentVO = (IdCtrlformulaVO) iteraKey.next();
				parentls.add(parentVO);
			}
			while (iteraValue.hasNext()) {
				ArrayList<IdCtrlschemeVO> childrenVO = (ArrayList<IdCtrlschemeVO>) iteraValue
						.next();
				childrenls.addAll(childrenVO);
			}
			dmo.updateArray(parentls.toArray(new IdCtrlformulaVO[0]));
			dmo.updateArray(childrenls.toArray(new IdCtrlschemeVO[0]));

		} catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("tbb_ctrl", "01801ctl_000052")/* 未知异常 */);
		}
	}



	/**
	 * 启动计划上的控制方案
	 * */
	public void updateCtrl(IdCtrlformulaVO[] vos) throws BusinessException {
		try {
			NtbSuperDMO dmo = new NtbSuperDMO();
			ArrayList<IdCtrlformulaVO> parentls = new ArrayList<IdCtrlformulaVO>();
			dmo.updateArray(vos);
		} catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("tbb_ctrl", "01801ctl_000052")/* 未知异常 */);
		}

	}

	@Override
	public String createNtbTempTable_new(CubeDef cube, String name,
			ArrayList<ArrayList> listTempTableValue) throws BusinessException,
			NamingException, Exception {
		NtbSuperDMO demo = new NtbSuperDMO("TEMP");
		StringBuffer sWhere_plan = new StringBuffer();
		String sTempTableName = null;
		sTempTableName = demo.getTempStringTable_New(name, new String[] {
				"DATACELLID", "DATACELLCODE" }, new String[] {
				"varchar(2000) not null ", "varchar(4000) " }, null,
				listTempTableValue);
		return sTempTableName;
	}

	@Override
	public ArrayList<IdCtrlformulaVO> getPlanStartCtrlformulaVO(String pk_cube,
			String pk_task) throws BusinessException {


		try{
			/**
			 * pk_parent=null是计划上设置的控制方案
			 * 模型上的控制方案没有停用状态，即isstarted = 'N'，只有计划上的控制方案有停用状态
			 * */
			HashMap<String, IdCtrlformulaVO> map = new HashMap<String, IdCtrlformulaVO>();
			String sWhere = "pk_cube = '" + pk_cube + "' and PK_PLAN ='"+pk_task+"'";
			IdCtrlformulaVO[] vos = (IdCtrlformulaVO[])NtbSuperServiceGetter.getINtbSuper().queryByCondition(IdCtrlformulaVO.class, sWhere);
			ArrayList<IdCtrlformulaVO> list = new ArrayList<IdCtrlformulaVO> ();
			if(vos!=null){
				list.addAll(Arrays.asList(vos));
			}
			return list;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*未知异常*/);
		}

	}

	public void checkExistCtrlSchemeFindByDv(HashMap<String,HashMap<DimVector,DataCellValue>> cubeMap) throws Exception {
		HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> ctrlscheme = new HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> ();
		Iterator iterator = cubeMap.entrySet().iterator();
		while(iterator.hasNext()){
			    Map.Entry obj = (Map.Entry)iterator.next();
			    String pk_cube = (String)obj.getKey();
			    HashMap<DimVector,DataCellValue> valueMap = (HashMap<DimVector,DataCellValue>)obj.getValue();
	            ArrayList<DimVector> dvList = new ArrayList<DimVector> ();
	            Iterator iter = valueMap.entrySet().iterator();
	            while(iter.hasNext()){
	            	Map.Entry _obj = (Map.Entry)iter.next();
	            	DimVector vec = (DimVector)_obj.getKey();
	            	dvList.add(vec);
	            }

	    		String tmpTableName = "NTB_TMP_CUBE_"+RandomStringUtils.randomNumeric(3);
				ArrayList<ArrayList> _list = new ArrayList<ArrayList> ();
	    		IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
				for(int m=0;m<dvList.size();m++){
					ArrayList<String> tmpList = new ArrayList<String> ();
					tmpList.add(cvt.convertToString(dvList.get(m))==null?" ":cvt.convertToString(dvList.get(m))); //yuyonga
					tmpList.add(cvt.convertToString(dvList.get(m))==null?" ":cvt.convertToString(dvList.get(m)));
					_list.add(tmpList);
				}
				tmpTableName = CtlSchemeCTL.createNtbTempTable_new(null,tmpTableName, _list);
				StringBuffer sWhere_cube = new StringBuffer();
				sWhere_cube.append("isstarted = 'Y' and pk_dimvector in (");
				sWhere_cube.append("select DATACELLCODE from ").append(tmpTableName);
				sWhere_cube.append(")").append(" and pk_cube = '").append(pk_cube).append("'");
                ctrlscheme.putAll(CtlSchemeCTL.queryCtrlScheme(sWhere_cube.toString()));
		}
		HashMap<String,DataCell> map = new HashMap<String,DataCell> ();
		HashMap<String,ArrayList<String>> pkMap = new HashMap<String,ArrayList<String>> ();
		Iterator _iter= ctrlscheme.entrySet().iterator();
		while(_iter.hasNext()){
			Map.Entry entry = (Map.Entry)_iter.next();
			IdCtrlformulaVO vo = (IdCtrlformulaVO)entry.getKey();
            if(pkMap.get(vo.getPk_cube())==null){
            	ArrayList<String> list = new ArrayList<String> ();
            	list.add(vo.getPk_dimvector());
            	pkMap.put(vo.getPk_cube(),list);
            }else{
            	pkMap.get(vo.getPk_cube()).add(vo.getPk_dimvector());
            }
		}
		Iterator iterPkMap= pkMap.entrySet().iterator();
		while(iterPkMap.hasNext()){
			Map.Entry entry = (Map.Entry)iterPkMap.next();
			String str = (String)entry.getKey();
			ArrayList<String> sList = (ArrayList<String>)entry.getValue();
			map.putAll(CtlSchemeCTL.getDataCellPkCubeByDimVector(str,sList.toArray(new String[0])));
		}

		ArrayList<IdCtrlformulaVO> updatevo = new ArrayList<IdCtrlformulaVO> ();
		HashMap<String,DimFormulaVO> formulaMap = new HashMap<String,DimFormulaVO> ();
		Iterator iter= ctrlscheme.entrySet().iterator();
		CtlSchemeCTL.getParserMap().clear();
		while(iter.hasNext()){
			Map.Entry entry = (Map.Entry)iter.next();
			IdCtrlformulaVO vo = (IdCtrlformulaVO)entry.getKey();
			if(formulaMap.get(vo.getPk_parent())==null){
				DimFormulaVO dimformulavo = FormulaCTL.getDimFormulaByPrimaryKey(vo.getPk_parent());  //这里有一次远程调用
				formulaMap.put(vo.getPk_parent(), dimformulavo);
			}
			ArrayList<IdCtrlschemeVO> vos = (ArrayList<IdCtrlschemeVO>)entry.getValue();
			IdCtrlschemeVO[] schemeArr = CtlSchemeCTL.getLinkedSchemeVOs(vos);
			String express = CtlSchemeCTL.againCalculate(map.get(vo.getPk_cube()+vo.getPk_dimvector()),vos.toArray(new IdCtrlschemeVO[0]),formulaMap.get(vo.getPk_parent()),cubeMap.get(vo.getPk_cube()));
			//检查express是否不满足条件,如果是,直接
			express = express.replaceAll("%", "/100");
			Boolean[] needctl = CtlSchemeCTL.needCtl(express);
			if(!needctl[0]){
				throw new CtrlBusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0","01050rule001-0046")/*@res "调整单元格["*/+map.get(vo.getPk_cube()+vo.getPk_dimvector()).toString()+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0","01050rule001-0047")/*@res "]有误,调整后的预算数小于执行数["*/+express+"]");
			}
		}

	}



	public String[] addAlarmScheme(IdAlarmschemeVO[] vos) throws BusinessException{
    	BaseDAO dao = new BaseDAO() ;
    	String[] pks = dao.insertVOArray(vos) ;
    	return pks ;
    }

	public String[] addAlarmDimVector(IdAlarmDimVectorVO[] vos) throws BusinessException {
		BaseDAO dao = new BaseDAO() ;
    	String[] pks = dao.insertVOArray(vos) ;
    	return pks ;
	}

	public Collection<IdAlarmschemeVO> queryAlarmScheme(String sqlWhere) throws BusinessException{
		BaseDAO dao = new BaseDAO() ;
		Collection<IdAlarmschemeVO> vos = dao.retrieveByClause(IdAlarmschemeVO.class, sqlWhere) ;
		return vos ;
	}

	public Collection<IdAlarmDimVectorVO> queryAlarmDimvector(String sqlWhere) throws BusinessException {
		BaseDAO dao = new BaseDAO() ;
		Collection<IdAlarmDimVectorVO> vos = dao.retrieveByClause(IdAlarmDimVectorVO.class, sqlWhere) ;
		return vos ;
	}

	public void updateAlarmScheme(IdAlarmschemeVO[] vo) throws BusinessException{
		BaseDAO dao = new BaseDAO() ;
		dao.updateVOArray(vo) ;
	}

	public void deleteAlarmScheme(ArrayList<IdAlarmschemeVO> list) throws BusinessException{
		BaseDAO dao = new BaseDAO() ;
		dao.deleteVOArray(list.toArray(new IdAlarmschemeVO[list.size()])) ;
	}

	public void deleteAlarmDimVector(List<IdAlarmDimVectorVO> list) throws BusinessException{
		BaseDAO dao = new BaseDAO() ;
		dao.deleteVOArray(list.toArray(new IdAlarmDimVectorVO[list.size()])) ;
	}

	public HashMap<String,HashMap<DimVector,Boolean>> querAlarmScheme(MdTask task) throws BusinessException {
		HashMap<String,HashMap<DimVector,Boolean>> alarmmap = null ;
		IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
		try{
			if(task == null){
				return alarmmap ;
			}
			if(CtlSchemeCTL.checkTaskStatus(task)){
				alarmmap = new HashMap<String,HashMap<DimVector,Boolean>>() ;
				StringBuilder sql0 = new StringBuilder() ;
				sql0.append(" pk_plan = '").append(task.getPrimaryKey()).append("'") ;
				Collection<IdAlarmschemeVO> collection0 = queryAlarmScheme(sql0.toString()) ;
				if(collection0 == null || collection0.size() == 0) {
					return null ;
				}
				StringBuilder sql1 = new StringBuilder() ;
				ICubeDefQueryService cubeQuery = CubeServiceGetter.getCubeDefQueryService() ;
				String cubecode = null ;
				sql1.append(" hasscheme = 'Y' and pk_alarmscheme in ('") ;
				for(IdAlarmschemeVO vo : collection0){
					if(cubecode == null){
						cubecode = cubeQuery.queryCubeDefByPK(vo.getPk_cube()).getObjcode() ;
					}
					sql1.append(vo.getPrimaryKey()).append("','") ;
				}
				sql1.append("')") ;
				Collection<IdAlarmDimVectorVO> collection1 = queryAlarmDimvector(sql1.toString()) ;
				HashMap<DimVector,Boolean> map = new HashMap<DimVector,Boolean>() ;
				for(IdAlarmDimVectorVO vo : collection1){
					if("".equals(vo.getPk_dimvector()) || null == vo.getPk_dimvector()){
						continue ;
					}
					DimVector dv = (DimVector) cvt.fromString(vo.getPk_dimvector()) ;
					if(map.get(dv) == null){
						map.put(dv, true) ;
					}
				}
				alarmmap.put(cubecode, map) ;
			}
		}catch(BusinessException e){
			NtbLogger.printException(e) ;
		}
		return alarmmap ;

	}

	@Override
	public Map<String, List<String>> queryCtrlSchemeSimply(String sWhere) throws BusinessException {
		try {
			CostTime time2 = new CostTime();
			Map<String, List<String>> returnvo = new HashMap<String, List<String>>();

			HashMap<String, IdCtrlformulaVO> tempMap = new HashMap<String, IdCtrlformulaVO>();
			NtbSuperDMO dmo = new NtbSuperDMO();

			SuperVO[] vos_parent = dmo.queryByWhereClause(IdCtrlformulaVO.class, sWhere, new String[]{new IdCtrlformulaVO().getPKFieldName()});
			ArrayList<String> pkList = new ArrayList<String>();

			// liming 2009.11.12 cube map
//			Map<String, String> cubeMap = new HashMap<String, String>();
			ArrayList<ArrayList> cubeList = new ArrayList<ArrayList>();

			if (vos_parent != null && vos_parent.length > 0) {

				for (int i = 0; i < vos_parent.length; i++) {
					IdCtrlformulaVO parentVO = (IdCtrlformulaVO) vos_parent[i];
					ArrayList<String> tmpList = new ArrayList<String> ();
					
					tmpList.add(parentVO.getPrimaryKey());
					tmpList.add(parentVO.getPrimaryKey());
					cubeList.add(tmpList);
					
					pkList.add(parentVO.getPrimaryKey());
					tempMap.put(parentVO.getPrimaryKey(), parentVO);
//					cubeMap.put(parentVO.getPk_cube(), null);
				}
				
				String tmpTableName = "NTB_TMP_FORMUAL_PKS";//+RandomStringUtils.randomNumeric(3);
				tmpTableName = CtlSchemeCTL.createNtbTempTable_new(null,tmpTableName, cubeList);
				
//				String[] sqls = SqlPartlyTools.getSqls("pk_ctrlformula",(String[]) cubeList.toArray(new String[0]));
//				StringBuffer sql = new StringBuffer();
//				for (int i = 0; i < sqls.length; i++) {
//					if (i == 0) {
//						sql.append(sqls[i]);
//					} else {
//						sql.append("or");
//						sql.append(sqls[i]);
//					}
//
//				}

				StringBuffer sWhere_cube = new StringBuffer();
								sWhere_cube.append("pk_ctrlformula in (");
								sWhere_cube.append("select DATACELLCODE from ").append(tmpTableName);
								sWhere_cube.append(")");
		
				
				SuperVO[] vos_children = dmo.queryByWhereClause(IdCtrlschemeVO.class, sWhere_cube.toString(), new String[]{"pk_obj", "pk_ctrlformula"}); 

				for (int i = 0; i < vos_children.length; i++) {
					IdCtrlschemeVO childrenVO = (IdCtrlschemeVO) vos_children[i];
					if (tempMap.containsKey(childrenVO.getPk_ctrlformula())) {
						IdCtrlformulaVO tempvo = tempMap.get(childrenVO
								.getPk_ctrlformula());
						if (returnvo.containsKey(tempvo.getPrimaryKey())) {
							List<String> schmvols = returnvo
									.get(tempvo.getPrimaryKey());
							schmvols.add(childrenVO.getPrimaryKey());
						} else {
							List<String> schmvols = new ArrayList<String>();
							schmvols.add(childrenVO.getPrimaryKey());
							returnvo.put(tempvo.getPrimaryKey(), schmvols);
						}
					}
				}

			}
			return returnvo;

		} catch (Exception e) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("tbb_ctrl", "01801ctl_000052")/* 未知异常 */);
		}
	}
	
private UFDouble[][] groupGetExeData(NtbParamVO[] vos, IBusiSysExecDataProvider exeprovider, boolean isPreFind)throws BusinessException  {
	   if(!(exeprovider instanceof IBusiSysExecDataProviderEx))return null;
		CostTime ct=new CostTime();
		String maxDocType = GroupedNtbParamVO.getMaxDocType(vos);
		Map<GroupedNtbParamVO,List<String>> callParaG=new HashMap<GroupedNtbParamVO,List<String>>();
		Map<String,String[]> childGroupDocs=new HashMap<String,String[]>();
		Map<NtbParamVO,List<String>> callP=new HashMap<NtbParamVO,List<String>>();
		Map<NtbParamVO,GroupedNtbParamVO> map=new HashMap<NtbParamVO,GroupedNtbParamVO>();
		for(NtbParamVO vo: vos){
			GroupedNtbParamVO gvo=new GroupedNtbParamVO(vo, maxDocType);
			map.put(vo, gvo);
			List<String> list = callParaG.get(gvo);
			if(list==null){
				list=new ArrayList<String>();
				callParaG.put(gvo, list);
			}
			if(gvo.getPkGroupDoc() != null){
				list.add(gvo.getPkGroupDoc());
				String[] lowarray = gvo.getLowerArray();
				if(lowarray!=null&&lowarray.length>0)
				childGroupDocs.put(gvo.getPkGroupDoc(),lowarray);
			}
		}
		//转一次
		for(GroupedNtbParamVO g:callParaG.keySet()){
			callP.put(g.getGroupedvo(), callParaG.get(g));
		}
		//DO Call
		Map<NtbParamVO,Map<String,UFDouble[]>> ret=new HashMap<NtbParamVO,Map<String,UFDouble[]>>();
		if(isPreFind)
			ret = ((IBusiSysExecDataProviderEx)exeprovider).getReadyDataGroupBatch(maxDocType, callP,childGroupDocs);
		else
			ret = ((IBusiSysExecDataProviderEx)exeprovider).getExecDataGroupBatch(maxDocType, callP,childGroupDocs);
		if(ret == null)
			return null;
		//再转一次
		Map<GroupedNtbParamVO,Map<String,UFDouble[]>> ret2=new HashMap<GroupedNtbParamVO,Map<String,UFDouble[]>>();
		for(NtbParamVO n:ret.keySet()){
			ret2.put(new GroupedNtbParamVO(n, maxDocType), ret.get(n));
		}
		//
		UFDouble[][] aa =new UFDouble[vos.length][];
		for(int i=0;i<vos.length;i++){
			NtbParamVO vo=vos[i];
			GroupedNtbParamVO gvo = map.get(vo);
			Map<String,UFDouble[]> mdata = ret2.get(gvo);
			if(gvo.getGroupDocType() != null) {
				UFDouble[] ufDoubles = mdata.get(gvo.getPkGroupDoc());
				aa[i]=ufDoubles;
			} else {
				if(mdata.size() > 0) {
					aa[i] = (UFDouble[]) mdata.values().toArray()[0];
				} else {
					aa[i] = new UFDouble[4];
					for(int j = 0 ; j < aa[i].length ; j++)
						aa[i][j] = new UFDouble(0);
				}
			}
		}
		 ct.printStepCost("groupGetExeData:"+vos.length+"NtbParamVO:"+exeprovider);
		return aa;
	}

}
