package nc.bs.gl.contrast.iufo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.gl.contrast.iufo.ContentType;
import nc.itf.gl.contrast.result.IContrastResult;
import nc.itf.org.IOrgEnumConst;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BaseProcessor;
import nc.jdbc.framework.processor.ColumnListProcessor;
import nc.vo.bd.account.AccAsoaVO;
import nc.vo.bd.pub.IPubEnumConst;
import nc.vo.fi.pub.SqlUtils;
import nc.vo.fipub.timecontrol.TimeCtrlUtil;
import nc.vo.fipub.utils.StrTools;
import nc.vo.gateway60.itfs.Currency;
import nc.vo.gl.contrast.data.ContrastDataVO;
import nc.vo.gl.contrast.iufo.ContrastHBBBQryVO;
import nc.vo.gl.contrast.result.ResultDetailTabVO;
import nc.vo.gl.contrast.result.ResultListTabVO;
import nc.vo.gl.contrast.result.ResultOccurTabDetailVO;
import nc.vo.gl.contrast.result.SumContrastQryVO;
import nc.vo.gl.contrast.rule.ContrastRuleAssSqlProvider;
import nc.vo.gl.contrast.rule.ContrastRuleVO;
import nc.vo.org.AccountingBookVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;

import org.apache.commons.lang.StringUtils;

/**
 * 内部交易对账供HBBB取数用
 * 
 * @modified by jiaah 直接对账模板和间接对账模板由isDirect控制，
 * 其中通过gl_contrastdata表的iscontrasted区分直接还是间接取数
 * @author liyra
 * 
 */
public class ContrastIUFOPubBO {

	public ContrastIUFOPubBO() {
		super();
	}

	/*private String getValueField(ContentType type) {
		StringBuilder result = new StringBuilder();
		if (type.equals(ContentType.AMOUNT)) {
			result.append(ContrastDataVO.CREDITQUANTITY).append("-").append(ContrastDataVO.DEBITQUANTITY);
		} else if (type.equals(ContentType.ORGTYPE)) {
			result.append(ContrastDataVO.LOCALCREDITAMOUNT).append("-").append(ContrastDataVO.LOCALDEBITAMOUNT);
		} else if (type.equals(ContentType.GLOBALTYPE)) {
			result.append(ContrastDataVO.GLOBALCREDITAMOUNT).append("-").append(ContrastDataVO.GLOBALDEBITAMOUNT);
		} else if (type.equals(ContentType.GROUPTYPE)) {
			result.append(ContrastDataVO.GROUPCREDITAMOUNT).append("-").append(ContrastDataVO.GROUPDEBITAMOUNT);
		}
		return result.toString();
	}*/

	private String getValueField(ContentType type, String tmptablename) {
		StringBuilder result = new StringBuilder();
		if (type.equals(ContentType.AMOUNT)) {
			result.append(" case   when acc.balanorient = 1  then ");
			result.append(ContrastDataVO.CREDITQUANTITY).append("-").append(ContrastDataVO.DEBITQUANTITY);
			result.append(" else ");
			result.append(ContrastDataVO.DEBITQUANTITY).append("-").append(ContrastDataVO.CREDITQUANTITY);
			result.append(" end ");
		} else if (type.equals(ContentType.ORGTYPE)) {

			result.append(" case   when acc.balanorient = 1  then ");
			result.append(ContrastDataVO.LOCALCREDITAMOUNT).append("-").append(ContrastDataVO.LOCALDEBITAMOUNT);
			result.append(" else ");
			result.append(ContrastDataVO.LOCALDEBITAMOUNT).append("-").append(ContrastDataVO.LOCALCREDITAMOUNT);
			result.append(" end ");

		} else if (type.equals(ContentType.GLOBALTYPE)) {

			result.append(" case   when acc.balanorient = 1  then ");
			result.append(ContrastDataVO.GLOBALCREDITAMOUNT).append("-").append(ContrastDataVO.GLOBALDEBITAMOUNT);
			result.append(" else ");
			result.append(ContrastDataVO.GLOBALDEBITAMOUNT).append("-").append(ContrastDataVO.GLOBALCREDITAMOUNT);
			result.append(" end ");

		} else if (type.equals(ContentType.GROUPTYPE)) {
			result.append(" case   when acc.balanorient = 1  then ");
			result.append(ContrastDataVO.GROUPCREDITAMOUNT).append("-").append(ContrastDataVO.GROUPDEBITAMOUNT);
			result.append(" else ");
			result.append(ContrastDataVO.GROUPDEBITAMOUNT).append("-").append(ContrastDataVO.GROUPCREDITAMOUNT);
			result.append(" end ");

		}else if (type.equals(ContentType.ORIGAMOUNT)) {
			result.append(" case   when acc.balanorient = 1  then ");
			result.append(ContrastDataVO.CREDITAMOUNT).append("-").append(ContrastDataVO.DEBITAMOUNT);
			result.append(" else ");
			result.append(ContrastDataVO.DEBITAMOUNT).append("-").append(ContrastDataVO.CREDITAMOUNT);
			result.append(" end ");

		}
		return result.toString();
	}
	
	/**
	 * 科目余额方向：0借1贷
	 * @param type
	 * @param tmptablename
	 * @return
	 */
	private String getVerifyValueField(ContentType type, String tmptablename) {
		StringBuilder result = new StringBuilder();
		if (type.equals(ContentType.AMOUNT)) {
			result.append(" case   when acc.balanorient = 0  then ");
			result.append("balancedebitquantity-balancecreditquantity");
			result.append(" else ");
			result.append("balancecreditquantity-balancedebitquantity");
			result.append(" end ");
		} else if (type.equals(ContentType.ORGTYPE)) {
			result.append(" case   when acc.balanorient = 0  then ");
			result.append("balancelocaldebitamount-balancelocalcreditamount+isnull(logdebitlocalamount,0)-isnull(logcreditlocalamount,0)");
			result.append(" else ");
			result.append("balancelocalcreditamount-balancelocaldebitamount+isnull(logcreditlocalamount,0)-isnull(logdebitlocalamount,0)");
			result.append(" end ");
		} else if (type.equals(ContentType.GLOBALTYPE)) {
			result.append(" case   when acc.balanorient = 0  then ");
			result.append("balanceglobaldebitamount-balanceglobalcreditamount+");
			result.append(" else ");
			result.append("balanceglobalcreditamount-balanceglobaldebitamount");
			result.append(" end ");
		} else if (type.equals(ContentType.GROUPTYPE)) {
			result.append(" case   when acc.balanorient = 0  then ");
			result.append("balancegroupdebitamount-balancegroupcreditamount+isnull(logdebitgroupamount,0)-isnull(logcreditgroupamount,0)");
			result.append(" else ");
			result.append("balancegroupcreditamount-balancegroupdebitamount+isnull(logcreditgroupamount,0)-isnull(logdebitgroupamount,0)");
			result.append(" end ");
		}else if (type.equals(ContentType.ORIGAMOUNT)) {
			result.append(" case   when acc.balanorient = 0  then ");
			result.append("balancedebitamount-balancecreditamount+isnull(logdebitamount,0)-isnull(logcreditamount,0)");
			result.append(" else ");
			result.append("balancecreditamount-balancedebitamount+isnull(logcreditamount,0)-isnull(logdebitamount,0)");
			result.append(" end ");
		}
		return result.toString();
	}
	
	private Set<String> getHasDataBook() throws DAOException{
		Set<String> set = new HashSet<String>();
		List result = (List)new BaseDAO().executeQuery("select distinct pk_accountingbook||pk_otherorgbook from gl_contrastdata", new ColumnListProcessor());
		for(Object obj : result){
			set.add(obj.toString());
		}
		return set;
	}

	
	public Map<String, UFDouble> getNCBalanceBatch(ContrastHBBBQryVO qryvo,
			String[] pks_self_oppbook) throws BusinessException {

		//如果账龄为空，还是走原来的逻辑，否则取核销余额-
		if(!StrTools.isEmptyStr(qryvo.getPk_timecontrol_b())){
			return getOccurBatch(qryvo, pks_self_oppbook);
		}
		
		if (pks_self_oppbook == null || pks_self_oppbook.length == 0
				|| StringUtils.isEmpty(qryvo.getContrastrulevo().getPk_book()))
			return null;

		Map<String, String> refOrgBookMap = getPk_accountingbooksByFinanceOrgIDAndSetOfBookID(qryvo.getContrastrulevo(),
				pks_self_oppbook, qryvo.getContrastrulevo().getPk_book());
		
		if(refOrgBookMap.size() == 0)
			return null;

		// 需要返回的map，其中key为本方+对方
		Map<String, UFDouble> rtMap = new HashMap<String, UFDouble>();

		String period = qryvo.getPk_accend_period();
		String year = qryvo.getPk_accend_period().substring(0, 4);

		ResultListTabVO[] resultVOs = getTotalResult(qryvo, period, year,false);

		ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(
				qryvo, qryvo.getContrastrulevo().getPk_group());
		String assSqlStr = "";
		// 拼接辅助核算
		if (assSql.isNeedAss()) {
			assSqlStr = assSql.getInnerJoinTabSql("cdata");
		}
		
		String acctab = nc.vo.gateway60.itfs.AccountUtilGL.getEnableSubjTmpTableByDate(
				refOrgBookMap.values().toArray(new String[0]),
				qryvo.getStddate(), "",
				new String[] { qryvo.getAccountcode() }, null);
		
		Set<String> dataSet = getHasDataBook();

		for (String pk_self_opp : pks_self_oppbook) {
			String pk_self = pk_self_opp.substring(0, 20);
			String pk_opp = pk_self_opp.substring(20, 40);

			String pk_selfaccountbook = "";
			String pk_oppaccountbook = "";
			if (qryvo.getIsself()) {
				pk_selfaccountbook = refOrgBookMap.get(pk_self);
				pk_oppaccountbook = refOrgBookMap.get(pk_opp);
			} else {
				pk_oppaccountbook = refOrgBookMap.get(pk_self);
				pk_selfaccountbook = refOrgBookMap.get(pk_opp);
			}
			
			if (null == pk_selfaccountbook || pk_selfaccountbook.trim().length() == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			
			if (null == pk_oppaccountbook || pk_oppaccountbook.trim().length() == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			
			if(!dataSet.contains(pk_selfaccountbook+pk_oppaccountbook)){
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			
			// 如果是直接取数模板
			// 总额对账报告”的对应差额为“零”时才生成抵销分录
			if (resultVOs == null || resultVOs.length == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			} 

			ResultDetailTabVO[] balanceDetailVOs = resultVOs[0]
					.getBalanceDetailVOs();
			ResultDetailTabVO currentTabVO = null;
			for (int i = 0; i < balanceDetailVOs.length; i++) {
				ResultDetailTabVO resultDetailTabVO = balanceDetailVOs[i];
				String self_accountingbook = resultDetailTabVO
						.getSelf_accountingbook();
				String other_accountingbook = resultDetailTabVO
						.getOther_accountingbook();
				if ((self_accountingbook.equals(pk_selfaccountbook) && other_accountingbook
						.equals(pk_oppaccountbook))
						|| (self_accountingbook.equals(pk_oppaccountbook) && other_accountingbook
								.equals(pk_selfaccountbook))) {
					currentTabVO = resultDetailTabVO;
					break;
				}
			}
			if (currentTabVO == null) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			} 
			
			if (qryvo.isDriect()) {
				if (qryvo.getContenttype().equals(ContentType.AMOUNT)) {
					UFDouble balance_yearinit_quantity = currentTabVO
							.getBalance_yearinit_quantity();
					// 判断数量年初余额差额是否为0
					if (!UFDouble.ZERO_DBL
							.equals(balance_yearinit_quantity == null ? UFDouble.ZERO_DBL
									: balance_yearinit_quantity)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(
						ContentType.ORGTYPE)) {
					UFDouble balance_yearinit_orgcur = currentTabVO
							.getBalance_yearinit_orgcur();
					// 判断组织本币年初余额差额是否为0
					if (!UFDouble.ZERO_DBL
							.equals(balance_yearinit_orgcur == null ? UFDouble.ZERO_DBL
									: balance_yearinit_orgcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(
						ContentType.GLOBALTYPE)) {
					UFDouble balance_yearinit_globalcur = currentTabVO
							.getBalance_yearinit_globalcur();
					// 判断全局本币年初余额差额是否为0
					if (!UFDouble.ZERO_DBL
							.equals(balance_yearinit_globalcur == null ? UFDouble.ZERO_DBL
									: balance_yearinit_globalcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(
						ContentType.GROUPTYPE)) {
					UFDouble balance_yearinit_groupcur = currentTabVO
							.getBalance_yearinit_groupcur();
					// 判断集团本币年初余额差额是否为0
					if (!UFDouble.ZERO_DBL
							.equals(balance_yearinit_groupcur == null ? UFDouble.ZERO_DBL
									: balance_yearinit_groupcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(
						ContentType.ORIGAMOUNT)) {
					UFDouble balance_yearinit_cur = currentTabVO
							.getBalance_yearinit_cur();
					// 判断原币年初余额差额是否为0
					if (!UFDouble.ZERO_DBL
							.equals(balance_yearinit_cur == null ? UFDouble.ZERO_DBL
									: balance_yearinit_cur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}
			
			}
			// 准备临时科目表
			// boolean bSelf=qryvo.getIsself();
			// new String[]{(bSelf? pk_selfaccountbook : pk_oppaccountbook)},
			// (bSelf? pk_selfaccountbook :
			// pk_oppaccountbook),qryvo.getPk_end_period().substring(0,4),
			// qryvo.getPk_end_period(), "",
			// nc.vo.gl.contrast.rule.ContrastRuleSubjUtil.getSubjCodes(voCondition.getContrastRuleVO(),
			// bSelf)
			
			//
			// ContrastRuleAssSqlProvider assSql = new
			// ContrastRuleAssSqlProvider(qryvo.getContrastrulevo(), null);
			StringBuilder content = new StringBuilder();
			// MODIFY BYWANGXWB 借-贷 贷-借 根据科目方向决定
			// content.append("select sum(").append(this.getValueField(qryvo.getContenttype())).append(") from gl_contrastdata cdata ");
			content.append("select sum(").append(
					this.getValueField(qryvo.getContenttype(), acctab)).append(
					") from gl_contrastdata cdata ");
			content.append("   inner join " + acctab + " acc on acc."
					+ AccAsoaVO.PK_ACCASOA + "=cdata.pk_accasoa ");

			// 拼接辅助核算
			if (assSql.isNeedAss()) {
				content.append(assSqlStr);
			}
			content.append("where CDATA.PK_ACCOUNTINGBOOK=? ");
			content.append("   and CDATA.PK_OTHERORGBOOK=? ");
//			content.append("   and CDATA.PERIODV='00' ");
			content.append("   and CDATA.yearinit='Y' ");
			content.append("   and CDATA.YEARV=? ");
			content.append("   and isnull(cdata.dr,0)=0  and discardflag ='N'");
			if(assSql.isNeedAss()){//说明是UCHECKBYKEY取数（取已对符）
				if(qryvo.isUncontrast()){
					content.append(" and amountequal<>'Y' ");
				}else{
					content.append(" and amountequal='Y' ");
				}
			}
			content.append("   group by acc.pk_accasoa, acc.balanorient  ");
			BaseDAO dao = new BaseDAO();
			SQLParameter parameter = new SQLParameter();
			parameter.addParam(pk_selfaccountbook);
			parameter.addParam(pk_oppaccountbook);
			parameter.addParam(year);
			CountQryProcessor processor = new CountQryProcessor();
			double result = queryFlag(content, dao, parameter, processor);
			rtMap.put(pk_self_opp, new UFDouble(result));
		}

		return rtMap;
	}

	private double queryFlag(StringBuilder content, BaseDAO dao,
			SQLParameter parameter, CountQryProcessor processor) throws DAOException {
		double result = 0;
		Object obj = dao.executeQuery(content.toString(), parameter, processor);
//		if(obj != null){
			result = (Double) obj;
//		}
		return result;
	}

	private ResultListTabVO[] getTotalResult(ContrastHBBBQryVO qryvo, String period, String year, Boolean isOccur) throws BusinessException {
		SumContrastQryVO queryVo = new SumContrastQryVO();
		queryVo.setPk_contrastrule(new String[] {
			qryvo.getContrastrulevo().getPk_contrastrule()
		});
		queryVo.setAccperiod(getPeriod(period));
		queryVo.setReportstatus(new String[] {
				"" + 1, "" + 2, "" + 3, "" + 4
		});
		queryVo.setYear(year);
		queryVo.setQryDate(new UFDate(qryvo.getStddate()));
		queryVo.setDateStr(new UFDate(qryvo.getStddate()));
		queryVo.setIsOccur(isOccur);
		ResultListTabVO[] resultVOs = ((IContrastResult) NCLocator.getInstance().lookup(IContrastResult.class.getName())).querySumContrastResult(queryVo);
		return resultVOs;
	}

	private String getPeriod(String period) {
		return period.substring(5, 7);
	}

	@SuppressWarnings({ "unchecked" })
	public Map<String,UFDouble> getOccurBatch(ContrastHBBBQryVO qryvo,String[] pks_self_oppbook) throws BusinessException {
		
		if (pks_self_oppbook == null || pks_self_oppbook.length == 0
				|| StringUtils.isEmpty(qryvo.getContrastrulevo().getPk_book()))
			return null;

		Map<String, String> refOrgBookMap = getPk_accountingbooksByFinanceOrgIDAndSetOfBookID(qryvo.getContrastrulevo(),
				pks_self_oppbook, qryvo.getContrastrulevo().getPk_book());
		
		if(refOrgBookMap.size() == 0)
			return null;

		// 需要返回的map，其中key为本方+对方
		Map<String, UFDouble> rtMap = new HashMap<String, UFDouble>();

		String period = qryvo.getPk_accend_period();
		String year = qryvo.getPk_accend_period().substring(0, 4);

		ResultListTabVO[] resultVOs = getTotalResult(qryvo, period, year,true);

		ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(
				qryvo, qryvo.getContrastrulevo().getPk_group());
		String assSqlStr = "";
		// 拼接辅助核算
		if (assSql.isNeedAss()) {
			assSqlStr = assSql.getInnerJoinTabSql("cdata");
		}
 
		String acctab = nc.vo.gateway60.itfs.AccountUtilGL.getEnableSubjTmpTableByDate(
				refOrgBookMap.values().toArray(new String[0]),
				qryvo.getStddate(), "",
				new String[] { qryvo.getAccountcode() }, null);
		Set<String> dataSet = getHasDataBook();

		for (String pk_self_opp : pks_self_oppbook) {

			String pk_self = pk_self_opp.substring(0, 20);
			String pk_opp = pk_self_opp.substring(20, 40);

			String pk_selfaccountbook = "";
			String pk_oppaccountbook = "";
			if (qryvo.getIsself()) {
				pk_selfaccountbook = refOrgBookMap.get(pk_self);
				pk_oppaccountbook = refOrgBookMap.get(pk_opp);
			} else {
				pk_oppaccountbook = refOrgBookMap.get(pk_self);
				pk_selfaccountbook = refOrgBookMap.get(pk_opp);
			}
			
			if (null == pk_selfaccountbook || pk_selfaccountbook.trim().length() == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			
			if (null == pk_oppaccountbook || pk_oppaccountbook.trim().length() == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			
			if(!dataSet.contains(pk_selfaccountbook+pk_oppaccountbook)){
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			if (resultVOs == null || resultVOs.length == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			} 
			
			ResultOccurTabDetailVO[] occurDetailVOs = resultVOs[0].getOccurDetailVOs();
			ResultOccurTabDetailVO currentTabVO = null;
			for (int i = 0; i < occurDetailVOs.length; i++) {
				ResultOccurTabDetailVO resultDetailTabVO = occurDetailVOs[i];
				String self_accountingbook = resultDetailTabVO.getPk_self();
				String other_accountingbook = resultDetailTabVO.getPk_opp();
				if ((self_accountingbook.equals(pk_selfaccountbook) && other_accountingbook.equals(pk_oppaccountbook))||
						(self_accountingbook.equals(pk_oppaccountbook) && other_accountingbook.equals(pk_selfaccountbook))) {
					currentTabVO = resultDetailTabVO;
					break;
				}
			}
			if (currentTabVO == null) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			} 
			
			if (qryvo.isDriect()) {

				if (qryvo.getContenttype().equals(ContentType.AMOUNT)) {
					UFDouble bal_occur_quantity = currentTabVO.getBal_occur_quantity();
					// 判断数量净发生差额是否为0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_quantity == null ? UFDouble.ZERO_DBL : bal_occur_quantity)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.ORGTYPE)) {
					UFDouble bal_occur_orgcur = currentTabVO.getBal_occur_orgcur();
					// 判断组织本币净发生差额是否为0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_orgcur == null ? UFDouble.ZERO_DBL : bal_occur_orgcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GLOBALTYPE)) {
					UFDouble bal_occur_globalcur = currentTabVO.getBal_occur_globalcur();
					// 判断全局本币净发生差额是否为0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_globalcur == null ? UFDouble.ZERO_DBL : bal_occur_globalcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GROUPTYPE)) {
					UFDouble bal_occur_groupcur = currentTabVO.getBal_occur_groupcur();
					// 判断集团本币净发生差额是否为0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_groupcur == null ? UFDouble.ZERO_DBL : bal_occur_groupcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}else if (qryvo.getContenttype().equals(ContentType.ORIGAMOUNT)) {
					UFDouble bal_occur_cur = currentTabVO.getBal_occur_cur();
					// 判断集团本币净发生差额是否为0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_cur == null ? UFDouble.ZERO_DBL : bal_occur_cur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}
			}
			double result = 0;
			//如果账龄为空，还是走原来的逻辑，否则取核销余额-
			if(StrTools.isEmptyStr(qryvo.getPk_timecontrol_b())){
				StringBuilder content = new StringBuilder();
				content.append("select sum(").append(this.getValueField(qryvo.getContenttype(), acctab)).append(") from gl_contrastdata cdata ");
				content.append("   inner join " + acctab + " acc on acc." + AccAsoaVO.PK_ACCASOA + "=cdata.pk_accasoa ");
				// 拼接辅助核算
				if (assSql.isNeedAss()) {
					content.append(assSqlStr);
				}
				content.append("where CDATA.PK_ACCOUNTINGBOOK=? ");
				content.append("   and CDATA.PK_OTHERORGBOOK=? ");
				//content.append("   and CDATA.PREPAREDDATEV<=? and CDATA.PREPAREDDATEV>=? and CDATA.PREPAREDDATEV>=? ");
				if(qryvo.isUncontrast()){
					content.append("   and CDATA.YEARV='"+year+"' ");
				}
				content.append("   and isnull(cdata.dr,0)=0  and discardflag ='N' ");
				content.append(" and  CDATA.yearinit<>'Y' ");
				if(assSql.isNeedAss()){//说明是UCHECKBYKEY取数（取已对符）
					if(qryvo.isUncontrast()){
						content.append(" and amountequal<>'Y' ");
						content.append("   and CDATA.PREPAREDDATEV<='"+qryvo.getPk_end_period()+  "' ");
					}else{
						content.append(" and amountequal='Y' ");
						content.append("   and CDATA.PREPAREDDATEV<='"+qryvo.getPk_end_period()+  "' ");
						//content.append("   and CDATA.periodv>='"+qryvo.getPk_start_period().substring(5,7)+  "' ");
					}
				}
				content.append("   group by acc.pk_accasoa, acc.balanorient  ");
				BaseDAO dao = new BaseDAO();
				SQLParameter parameter = new SQLParameter();
				parameter.addParam(pk_selfaccountbook);
				parameter.addParam(pk_oppaccountbook);
//				parameter.addParam(qryvo.getPk_end_period());//起始结束时间
//				parameter.addParam(qryvo.getPk_start_period());//起始结束时间
//				parameter.addParam(qryvo.getContrastrulevo().getStartdate());//规则起始时间
//				parameter.addParam(year);
				CountQryProcessor processor = new CountQryProcessor();
				result = queryFlag(content, dao, parameter, processor);
			}else{
				String[] timecontrol = qryvo.getPk_timecontrol_b().substring(1, qryvo.getPk_timecontrol_b().length()-1).split("=");
				List<Object[]> timectrol = (List<Object[]>)new BaseDAO().executeQuery("select b.startunit,b.endunit,b.pk_timectrl from fipub_timecontrol a,fipub_timecontrol_b b where a.pk_timectrl = b.pk_timectrl and a.code='"
						+timecontrol[0]+"' and b.propertyid = '"+timecontrol[1]+"'", new ArrayListProcessor());
//				List<Object[]> timectrol = (List<Object[]>)new BaseDAO().executeQuery("select startunit,endunit from fipub_timecontrol_b where pk_timectrl_b='"
//											+qryvo.getPk_timecontrol_b()+"'", new ArrayListProcessor());
				if(timectrol!=null&&timectrol.size()>0){
					//结束日期， 需要这个日期来取的账龄区间。
					UFDate date = new UFDate(qryvo.getPk_end_period()).asEnd();
					String fipubtmp_timectrl = TimeCtrlUtil.getTimeCtrlTmpTableForGL(timectrol.get(0)[2].toString(), date) + " timectrl ";
					List<Object[]> startEndDate = (List<Object[]>)new BaseDAO().executeQuery("select startdate,enddate from " + fipubtmp_timectrl + " where propertyid = '"+timecontrol[1]+"'", new ArrayListProcessor());
					UFDate startdate = new UFDate(startEndDate.get(0)[0].toString()).asBegin();
					UFDate enddate = new UFDate(startEndDate.get(0)[1].toString()).asEnd();
					
					StringBuilder content = new StringBuilder();

					//处理汇率问题_待完善（账簿、取汇率的日期）
//					String pk_accperiodscheme = AccountBookUtil.getAccPeriodSchemePKByAccountingbookPk(pk_selfaccountbook);
					if (qryvo.getContenttype().equals(ContentType.ORIGAMOUNT)){
						matchRate(pk_selfaccountbook, qryvo.getPk_end_period(), null, null);
						content.append("select sum((").append(this.getVerifyValueField(qryvo.getContenttype(), acctab)).append(")*rate.rate ) from gl_verifydetail verify ");
						content.append(" inner join tmpbd_currate rate on rate.pk_currtype = verify.pk_currtype ");
					}else{
						content.append("select sum(").append(this.getVerifyValueField(qryvo.getContenttype(), acctab)).append(") from gl_verifydetail verify ");
						
					}
					
					content.append(" left join (select l.pk_verifydetail" +
							",sum(verifydebitamount) logdebitamount, sum(verifylocaldebitamount) logdebitlocalamount,sum(verifygroupdebitamount) logdebitgroupamount " +
							",sum(verifycreditamount) logcreditamount, sum(verifylocalcreditamount) logcreditlocalamount,sum(verifygroupcreditamount) logcreditgroupamount " +
							"from gl_verify_log l where opdate > '"+qryvo.getPk_end_period()+"'  and dr = 0 group by l.pk_verifydetail) a " +
							" on verify.pk_verifydetail=a.pk_verifydetail");
					
					content.append("   inner join " + acctab + " acc on acc." + AccAsoaVO.PK_ACCASOA + "=verify.pk_accasoa ");
					
					content.append(" where pk_detail in (select pk_detail from gl_contrastdata cdata ");
					content.append("   inner join " + acctab + " acc on acc." + AccAsoaVO.PK_ACCASOA + "=cdata.pk_accasoa ");
					// 拼接辅助核算
					if (assSql.isNeedAss()) {
						content.append(assSqlStr);
					}
					content.append("where CDATA.PK_ACCOUNTINGBOOK=? ");
					content.append("   and CDATA.PK_OTHERORGBOOK=? ");
					content.append("   and CDATA.PREPAREDDATEV<=? and CDATA.PREPAREDDATEV>=?  ");
					content.append("   and isnull(cdata.dr,0)=0  and discardflag ='N' ");
					if(qryvo.isUncontrast()){
						content.append("   and CDATA.YEARV='"+year+"' ");
					}
					if(qryvo.isUncontrast()){
						content.append(" and amountequal<>'Y' ");
						content.append("   and CDATA.PREPAREDDATEV<='"+qryvo.getPk_end_period()+  "' ");
					}else{
						content.append(" and amountequal='Y' ");
						content.append("   and CDATA.PREPAREDDATEV<='"+qryvo.getPk_end_period()+  "' ");
						//content.append("   and CDATA.periodv>='"+qryvo.getPk_start_period().substring(5,7)+  "' ");
					}
					content.append("   ) ");
					//过滤掉只剩下集团本币的核销记录
//					content.append(" and ( verify.Balancedebitamount<-0.0001 or ");
//					content.append(" verify.Balancelocaldebitamount<-0.0001 or ");
//					content.append(" verify.Balancedebitamount>0.0001 or ");
//					content.append(" verify.Balancelocaldebitamount>0.0001 or ");
//					content.append(" verify.Balancecreditamount<-0.0001 or ");
//					content.append(" verify.Balancelocalcreditamount<-0.0001 or ");
//					content.append(" verify.Balancecreditamount>0.0001 or ");
//					content.append(" verify.Balancelocalcreditamount>0.0001 ) ");
					BaseDAO dao = new BaseDAO();
					SQLParameter parameter = new SQLParameter();
					parameter.addParam(pk_selfaccountbook);
					parameter.addParam(pk_oppaccountbook);
					parameter.addParam(enddate.toString());//起始结束时间
					parameter.addParam(startdate.toString());//起始结束时间
//					parameter.addParam(year);
					
					//parameter.addParam(qryvo.getContrastrulevo().getStartdate());//规则起始时间
//					parameter.addParam(enddate.toString());//起始结束时间
//					parameter.addParam(startdate.toString());//起始结束时间
					CountQryProcessor processor = new CountQryProcessor();   
					result = queryFlag(content, dao, parameter, processor);
				}
			}
			rtMap.put(pk_self_opp, new UFDouble(result));
		}

		return rtMap;
	}
	
	private void matchRate(String pk_accountingbook, String ratedate, String year, String month) throws BusinessException{
		BaseDAO dao = new BaseDAO();
		//1.删除旧数据
		String deletesql = " delete from tmpbd_currate ";
		dao.executeUpdate(deletesql);
		
		//2.插入新数据
		StringBuffer insertsql = new StringBuffer(" insert into tmpbd_currate ");
		insertsql.append(" select distinct cf.pk_currtype, ra.rate from bd_currrate ra inner join bd_currinfo cf on ra.pk_currinfo = cf.pk_currinfo ");
		insertsql.append(" inner join org_group gp on gp.pk_exratescheme = cf.pk_exratescheme inner join org_accountingbook book on gp.pk_group = book.pk_group ");
		insertsql.append(" where cf.oppcurrtype = '1002Z0100000000001K1' ");
		insertsql.append(" and book.pk_accountingbook = '"+pk_accountingbook+"' and ra.ratedate = '"+ratedate.substring(0, 10)+"' ");
		
//		insertsql.append(" select cf.pk_currtype, ra.adjustrate, (row_number() over (partition by  cf.pk_currtype order by p.periodyear || pm.accperiodmth desc)) flag ");
//		insertsql.append(" from bd_adjustrate ra inner join bd_currinfo cf on ra.pk_currinfo = cf.pk_currinfo ");
//		insertsql.append(" inner join bd_accperiodmonth pm on ra.pk_accperiodmonth = pm.pk_accperiodmonth ");
//		insertsql.append(" inner join bd_accperiod p on pm.pk_accperiod = p.pk_accperiod ");
//		insertsql.append(" inner join org_accountingbook book on book.pk_exratescheme = cf.pk_exratescheme ");
//		insertsql.append(" where cf.oppcurrtype = '1002Z0100000000001K1' ");
//		insertsql.append(" and p.periodyear <= '"+year+"' and pm.accperiodmth <= '"+month+"' ");
//		insertsql.append(" and pm.pk_accperiodscheme = '"+pk_accperiodscheme+"' and book.pk_accountingbook = '"+pk_accountingbook+"' ");
		
		dao.executeUpdate(insertsql.toString());
		String groupCurrpk = Currency.getGroupCurrpk(InvocationInfoProxy.getInstance().getGroupId());
		//处理与目标币种相同的情况
		dao.executeUpdate(" insert into tmpbd_currate values ('" + groupCurrpk + "',1) ");
		
	}

	public Map<String,UFDouble> getQCBalanceBatch(ContrastHBBBQryVO qryvo,String[] pks_self_oppbook) throws BusinessException {
		
		//如果账龄为空，还是走原来的逻辑，否则取核销余额-
		if(!StrTools.isEmptyStr(qryvo.getPk_timecontrol_b())){
			return getOccurBatch(qryvo, pks_self_oppbook);
		}
		
		if (pks_self_oppbook == null || pks_self_oppbook.length == 0
				|| StringUtils.isEmpty(qryvo.getContrastrulevo().getPk_book()))
			return null;

		Map<String, String> refOrgBookMap = getPk_accountingbooksByFinanceOrgIDAndSetOfBookID(qryvo.getContrastrulevo(),
				pks_self_oppbook, qryvo.getContrastrulevo().getPk_book());
		
		if(refOrgBookMap.size() ==0)
			return null;

		// 需要返回的map，其中key为本方+对方
		Map<String, UFDouble> rtMap = new HashMap<String, UFDouble>();

		String period = qryvo.getPk_accend_period();
		String year = qryvo.getPk_accend_period().substring(0, 4);

		ResultListTabVO[] resultVOs = getTotalResult(qryvo, period, year,false);

		ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(
				qryvo, qryvo.getContrastrulevo().getPk_group());
		String assSqlStr = "";
		// 拼接辅助核算
		if (assSql.isNeedAss()) {
			assSqlStr = assSql.getInnerJoinTabSql("cdata");
		}
		
		String acctab = nc.vo.gateway60.itfs.AccountUtilGL.getEnableSubjTmpTableByDate(
				refOrgBookMap.values().toArray(new String[0]),
				qryvo.getStddate(), "",
				new String[] { qryvo.getAccountcode() }, null);
		Set<String> dataSet = getHasDataBook();
		for (String pk_self_opp : pks_self_oppbook) {

			String pk_self = pk_self_opp.substring(0, 20);
			String pk_opp = pk_self_opp.substring(20, 40);

			String pk_selfaccountbook = "";
			String pk_oppaccountbook = "";
			if (qryvo.getIsself()) {
				pk_selfaccountbook = refOrgBookMap.get(pk_self);
				pk_oppaccountbook = refOrgBookMap.get(pk_opp);
			} else {
				pk_oppaccountbook = refOrgBookMap.get(pk_self);
				pk_selfaccountbook = refOrgBookMap.get(pk_opp);
			}
			
			if (null == pk_selfaccountbook || pk_selfaccountbook.trim().length() == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			
			if (null == pk_oppaccountbook || pk_oppaccountbook.trim().length() == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			if(!dataSet.contains(pk_selfaccountbook+pk_oppaccountbook)){
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			if (resultVOs == null || resultVOs.length == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			ResultDetailTabVO[] balanceDetailVOs = resultVOs[0].getBalanceDetailVOs();
			ResultDetailTabVO currentTabVO = null;
			for (int i = 0; i < balanceDetailVOs.length; i++) {
				ResultDetailTabVO resultDetailTabVO = balanceDetailVOs[i];
				String self_accountingbook = resultDetailTabVO.getSelf_accountingbook();
				String other_accountingbook = resultDetailTabVO.getOther_accountingbook();
				if ((self_accountingbook.equals(pk_selfaccountbook) && other_accountingbook.equals(pk_oppaccountbook))||
						(self_accountingbook.equals(pk_oppaccountbook) && other_accountingbook.equals(pk_selfaccountbook))) {
					currentTabVO = resultDetailTabVO;
					break;
				}
			}
			if (currentTabVO == null) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			} 
			if (qryvo.isDriect()) {

				if (qryvo.getContenttype().equals(ContentType.AMOUNT)) {
					UFDouble balance_init_quantity = currentTabVO.getBalance_init_quantity();
					// 判断数量期初余额差额是否为0
					if (!UFDouble.ZERO_DBL.equals(balance_init_quantity == null ? UFDouble.ZERO_DBL : balance_init_quantity)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.ORGTYPE)) {
					UFDouble balance_init_orgcur = currentTabVO.getBalance_init_orgcur();
					// 判断组织本币期初余额差额是否为0
					if (!UFDouble.ZERO_DBL.equals(balance_init_orgcur == null ? UFDouble.ZERO_DBL : balance_init_orgcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GLOBALTYPE)) {
					UFDouble balance_init_globalcur = currentTabVO.getBalance_init_globalcur();
					// 判断全局本币期初余额差额是否为0
					if (!UFDouble.ZERO_DBL.equals(balance_init_globalcur == null ? UFDouble.ZERO_DBL : balance_init_globalcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GROUPTYPE)) {
					UFDouble balance_init_groupcur = currentTabVO.getBalance_init_groupcur();
					// 判断集团本币期初余额差额是否为0
					if (!UFDouble.ZERO_DBL.equals(balance_init_groupcur == null ? UFDouble.ZERO_DBL : balance_init_groupcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}else if (qryvo.getContenttype().equals(ContentType.ORIGAMOUNT)) {
					UFDouble balance_init_cur = currentTabVO.getBalance_init_cur();
					// 判断集团本币期初余额差额是否为0
					if (!UFDouble.ZERO_DBL.equals(balance_init_cur == null ? UFDouble.ZERO_DBL : balance_init_cur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}
			
			
			}
			// 准备临时科目表
			// boolean bSelf=qryvo.getIsself();
			// new String[]{(bSelf? pk_selfaccountbook : pk_oppaccountbook)}, (bSelf? pk_selfaccountbook : pk_oppaccountbook),qryvo.getPk_end_period().substring(0,4), qryvo.getPk_end_period(), "",
			// nc.vo.gl.contrast.rule.ContrastRuleSubjUtil.getSubjCodes(voCondition.getContrastRuleVO(), bSelf)
//			String acctab = nc.vo.gateway60.itfs.AccountUtilGL.getEnableSubjTmpTableNew(new String[] {
//					pk_selfaccountbook
//			}, pk_selfaccountbook, year, period, "", new String[] {
//					qryvo.getAccountcode()
//			}, null, false, false);
			//
//			ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(qryvo.getContrastrulevo(), null);
			StringBuilder content = new StringBuilder();
			// MODIFY BYWANGXWB 借-贷 贷-借 根据科目方向决定
			// content.append("select sum(").append(this.getValueField(qryvo.getContenttype())).append(") from gl_contrastdata cdata ");
			content.append("select sum(").append(this.getValueField(qryvo.getContenttype(), acctab)).append(") from gl_contrastdata cdata ");
			content.append("   inner join " + acctab + " acc on acc." + AccAsoaVO.PK_ACCASOA + "=cdata.pk_accasoa ");
			
			// 拼接辅助核算
			if (assSql.isNeedAss()) {
				content.append(assSqlStr);
			}
			content.append("where CDATA.PK_ACCOUNTINGBOOK=? ");
			content.append("   and CDATA.PK_OTHERORGBOOK=? ");
//			if(qryvo.isDriect())
//				content.append("   and CDATA.ISCONTRASTED='Y' ");//直接生成分录的话增加勾对的条件；
			content.append("   and (cdata.ISINIT='Y'  OR " +
					"(cdata.ISINIT='N' AND CDATA.PREPAREDDATEV<?  and cdata.PREPAREDDATEV>=?))");
			

//			or (cdata.ISINIT='N' and CDATA.PREPAREDDATEV<=? and CDATA.PREPAREDDATEV>=?)
//			content.append("   and CDATA.PREPAREDDATEV<=? and CDATA.PREPAREDDATEV>=? ");
			content.append("   and CDATA.YEARV=? ");
			content.append("   and isnull(cdata.dr,0)=0  and discardflag ='N' ");
			if(assSql.isNeedAss()){//说明是UCHECKBYKEY取数（取已对符）
				if(qryvo.isUncontrast()){
					content.append(" and amountequal<>'Y' ");
				}else{
					content.append(" and amountequal='Y' ");
				}
			}
			content.append("   group by acc.pk_accasoa, acc.balanorient  ");
			BaseDAO dao = new BaseDAO();
			SQLParameter parameter = new SQLParameter();
			parameter.addParam(pk_selfaccountbook);
			parameter.addParam(pk_oppaccountbook);
//			parameter.addParam(qryvo.getPk_end_period());//
			parameter.addParam(qryvo.getPk_start_period());//
			parameter.addParam(qryvo.getContrastrulevo().getStartdate());//对账规则启用日期
			
			parameter.addParam(year);
			
			CountQryProcessor processor = new CountQryProcessor();
			double result = queryFlag(content, dao, parameter, processor);
			
			rtMap.put(pk_self_opp, new UFDouble(result));
		}
		return rtMap;
	}

	public Map<String,UFDouble> getQMBalanceBatch(ContrastHBBBQryVO qryvo,String[] pks_self_oppbook) throws BusinessException {
		
		//如果账龄为空，还是走原来的逻辑，否则取核销余额-
		if(!StrTools.isEmptyStr(qryvo.getPk_timecontrol_b())){
			return getOccurBatch(qryvo, pks_self_oppbook);
		}
		
		if(pks_self_oppbook == null || pks_self_oppbook.length==0 || StringUtils.isEmpty(qryvo.getContrastrulevo().getPk_book())) 
			return null;
		
		Map<String, String> refOrgBookMap = getPk_accountingbooksByFinanceOrgIDAndSetOfBookID(qryvo.getContrastrulevo(),pks_self_oppbook,qryvo.getContrastrulevo().getPk_book());
		
		if(refOrgBookMap.size() ==0)
			return null;
		//需要返回的map，其中key为本方+对方
		Map<String,UFDouble> rtMap = new HashMap<String, UFDouble>();
		
		String period = qryvo.getPk_accend_period();
		String year = qryvo.getPk_accend_period().substring(0, 4);
		
		ResultListTabVO[] resultVOs = getTotalResult(qryvo, period, year,false);
		
		ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(qryvo, qryvo.getContrastrulevo().getPk_group());
		String assSqlStr = "";
		// 拼接辅助核算
		if (assSql.isNeedAss()) {
			assSqlStr = assSql.getInnerJoinTabSql("cdata");
		}
		
		String acctab = nc.vo.gateway60.itfs.AccountUtilGL.getEnableSubjTmpTableByDate(
				refOrgBookMap.values().toArray(new String[0]),
				qryvo.getStddate(), "",
				new String[] { qryvo.getAccountcode() }, null);
		Set<String> dataSet = getHasDataBook();
		for (String pk_self_opp : pks_self_oppbook) {
			
			String pk_self = pk_self_opp.substring(0,20);
			String pk_other = pk_self_opp.substring(20,40);
			
			String pk_selfaccountbook = "";
			String pk_oppaccountbook = "";
			if (qryvo.getIsself()) {
				pk_selfaccountbook = refOrgBookMap.get(pk_self);
				pk_oppaccountbook = refOrgBookMap.get(pk_other);
			} else {
				pk_oppaccountbook = refOrgBookMap.get(pk_self);
				pk_selfaccountbook = refOrgBookMap.get(pk_other);
			}
			
			if (null == pk_selfaccountbook || pk_selfaccountbook.trim().length() == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			
			if (null == pk_oppaccountbook || pk_oppaccountbook.trim().length() == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			
			if (resultVOs == null || resultVOs.length == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			if(!dataSet.contains(pk_selfaccountbook+pk_oppaccountbook)){
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			ResultDetailTabVO[] balanceDetailVOs = resultVOs[0].getBalanceDetailVOs();
			ResultDetailTabVO currentTabVO = null;
			for (int i = 0; i < balanceDetailVOs.length; i++) {
				ResultDetailTabVO resultDetailTabVO = balanceDetailVOs[i];
				String self_accountingbook = resultDetailTabVO.getSelf_accountingbook();
				String other_accountingbook = resultDetailTabVO.getOther_accountingbook();
				if ((self_accountingbook.equals(pk_selfaccountbook) && other_accountingbook.equals(pk_oppaccountbook))||
						(self_accountingbook.equals(pk_oppaccountbook) && other_accountingbook.equals(pk_selfaccountbook))) {
					currentTabVO = resultDetailTabVO;
					break;
				}
			}
			if (currentTabVO == null) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			
			if (qryvo.isDriect()) {
				if (qryvo.getContenttype().equals(ContentType.AMOUNT)) {
					UFDouble balance_end_quantity = currentTabVO.getBalance_end_quantity();
					// 判断数量期末余额差额是否为0
					if (!UFDouble.ZERO_DBL.equals(balance_end_quantity == null ? UFDouble.ZERO_DBL : balance_end_quantity)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.ORGTYPE)) {
					UFDouble balance_end_orgcur = currentTabVO.getBalance_end_orgcur();
					// 判断组织本币期末余额差额是否为0
					if (!UFDouble.ZERO_DBL.equals(balance_end_orgcur == null ? UFDouble.ZERO_DBL : balance_end_orgcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GLOBALTYPE)) {
					UFDouble balance_end_globalcur = currentTabVO.getBalance_end_globalcur();
					// 判断全局本币期末余额差额是否为0
					if (!UFDouble.ZERO_DBL.equals(balance_end_globalcur == null ? UFDouble.ZERO_DBL : balance_end_globalcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GROUPTYPE)) {
					UFDouble balance_end_groupcur = currentTabVO.getBalance_end_groupcur();
					// 判断集团本币期末余额差额是否为0
					if (!UFDouble.ZERO_DBL.equals(balance_end_groupcur == null ? UFDouble.ZERO_DBL : balance_end_groupcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}else if (qryvo.getContenttype().equals(ContentType.ORIGAMOUNT)) {
					UFDouble balance_end_cur = currentTabVO.getBalance_end_cur();
					// 判断集团本币期末余额差额是否为0
					if (!UFDouble.ZERO_DBL.equals(balance_end_cur == null ? UFDouble.ZERO_DBL : balance_end_cur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}
			}
			// 准备临时科目表
			// boolean bSelf=qryvo.getIsself();
			// new String[]{(bSelf? pk_selfaccountbook : pk_oppaccountbook)}, (bSelf? pk_selfaccountbook : pk_oppaccountbook),qryvo.getPk_end_period().substring(0,4), qryvo.getPk_end_period(), "",
			// nc.vo.gl.contrast.rule.ContrastRuleSubjUtil.getSubjCodes(voCondition.getContrastRuleVO(), bSelf)
//			String acctab = nc.vo.gateway60.itfs.AccountUtilGL.getEnableSubjTmpTableNew(new String[] {
//					pk_selfaccountbook
//			}, pk_selfaccountbook, year, period, "", new String[] {
//					qryvo.getAccountcode()
//			}, null, false, false);
			//
			StringBuilder content = new StringBuilder();
			// MODIFY BYWANGXWB 借-贷 贷-借 根据科目方向决定
			// content.append("select sum(").append(this.getValueField(qryvo.getContenttype())).append(") from gl_contrastdata cdata ");
			content.append("select sum(").append(this.getValueField(qryvo.getContenttype(), acctab)).append(") from gl_contrastdata cdata ");
			content.append("   inner join " + acctab + " acc on acc." + AccAsoaVO.PK_ACCASOA + "=cdata.pk_accasoa ");
			
			// 拼接辅助核算
			if (assSql.isNeedAss()) {
				content.append(assSqlStr);
			}
			content.append("where CDATA.PK_ACCOUNTINGBOOK=? ");
			content.append("   and CDATA.PK_OTHERORGBOOK=? ");
			
//			if(qryvo.isDriect())
//				content.append("   and CDATA.ISCONTRASTED='Y' ");//直接生成分录的话增加勾对的条件；
			
			content.append(" and (cdata.ISINIT='Y' or (cdata.ISINIT='N' and cdata.PREPAREDDATEV>=? and cdata.PREPAREDDATEV<=?))");
//			content.append("   and CDATA.PREPAREDDATEV<=? and CDATA.PREPAREDDATEV>=? ");
			content.append("   and CDATA.YEARV=? ");
			content.append("   and isnull(cdata.dr,0)=0  and discardflag ='N' ");
			if(assSql.isNeedAss()){//说明是UCHECKBYKEY取数（取已对符）
				if(qryvo.isUncontrast()){
					content.append(" and amountequal<>'Y' ");
				}else{
					content.append(" and amountequal='Y' ");
				}
			}
			content.append("   group by acc.pk_accasoa, acc.balanorient  ");
			BaseDAO dao = new BaseDAO();
			SQLParameter parameter = new SQLParameter();
			parameter.addParam(pk_selfaccountbook);
			parameter.addParam(pk_oppaccountbook);
			
			parameter.addParam(qryvo.getContrastrulevo().getStartdate());//起始结束时间
//			parameter.addParam(qryvo.getPk_start_period());//起始结束时间
			parameter.addParam(qryvo.getPk_end_period());//起始结束时间
			
			parameter.addParam(year);
			CountQryProcessor processor = new CountQryProcessor();
			double result = queryFlag(content, dao, parameter, processor);
			rtMap.put(pk_self_opp, new UFDouble(result));
		}

		return rtMap;
	}
	
	/**
	 * 根据财务组织和账簿类型批量查询财务核算账簿
	 * @param pks_self_oppbook
	 * @param pk_setofbook
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	private Map<String,String> getPk_accountingbooksByFinanceOrgIDAndSetOfBookID(ContrastRuleVO contrastrulevo,String[] pks_self_oppbook,String pk_setofbook) throws BusinessException {
		
		Set<String> refOrgSet = new HashSet<String>();		
		for (String pk_self_opp : pks_self_oppbook) {
			String pk_self = pk_self_opp.substring(0,20);
			String pk_other = pk_self_opp.substring(20,40);
			refOrgSet.add(pk_self);
			refOrgSet.add(pk_other);
		}
		
		String inStr = SqlUtils.getInStr(AccountingBookVO.PK_RELORG, refOrgSet.toArray(new String[0]), true);
		
		StringBuilder condition = new StringBuilder();
		condition.append( " "+inStr) ;
		if(contrastrulevo.getIsmainorgcontrast() == null || !contrastrulevo.getIsmainorgcontrast().booleanValue()) {
			condition.append(" and " + AccountingBookVO.PK_SETOFBOOK + " = '" + pk_setofbook + "' ") ;
		}else {
			condition.append(" and " + AccountingBookVO.ACCOUNTTYPE + " = " + IOrgEnumConst.BOOKTYPE_MAINBOOK + " ") ;
		}
		condition.append(" and " + AccountingBookVO.ACCOUNTENABLESTATE + " = " + IPubEnumConst.ENABLESTATE_ENABLE + "");
        Collection<AccountingBookVO> c = (Collection<AccountingBookVO>) getService().retrieveByClause(AccountingBookVO.class, condition.toString());
        
        Map<String,String> orgBookMap = new HashMap<String, String>();
        
        if(c == null || c.size()==0)
        	return orgBookMap;
        
        
        for (AccountingBookVO accountingBookVO : c) {
        	String pk_relorg = accountingBookVO.getPk_relorg();
        	orgBookMap.put(pk_relorg, accountingBookVO.getPk_accountingbook());
        }
        
        return orgBookMap;
	}
	
	private IUAPQueryBS getService() {
		return NCLocator.getInstance().lookup(IUAPQueryBS.class);
	}

	public Map<String,UFDouble> getTotalOccurBatch(ContrastHBBBQryVO qryvo,String[] pks_self_oppbook) throws BusinessException {
		
		if(pks_self_oppbook == null || pks_self_oppbook.length==0 || StringUtils.isEmpty(qryvo.getContrastrulevo().getPk_book())) 
			return null;
		
		Map<String, String> refOrgBookMap = getPk_accountingbooksByFinanceOrgIDAndSetOfBookID(qryvo.getContrastrulevo(),pks_self_oppbook,qryvo.getContrastrulevo().getPk_book());
		
		if(refOrgBookMap.size() == 0)
			return null;
		
		//需要返回的map，其中key为本方+对方
		Map<String,UFDouble> rtMap = new HashMap<String, UFDouble>();
		
		String period = qryvo.getPk_accend_period();
		String year = qryvo.getPk_accend_period().substring(0, 4);
		
		ResultListTabVO[] resultVOs = getTotalResult(qryvo, period, year,true);
		
		ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(qryvo, qryvo.getContrastrulevo().getPk_group());
		String assSqlStr = "";
		// 拼接辅助核算
		if (assSql.isNeedAss()) {
			assSqlStr = assSql.getInnerJoinTabSql("cdata");
		}
		
		String acctab = nc.vo.gateway60.itfs.AccountUtilGL.getEnableSubjTmpTableByDate(
				refOrgBookMap.values().toArray(new String[0]),
				qryvo.getStddate(), "",
				new String[] { qryvo.getAccountcode() }, null);
		Set<String> dataSet = getHasDataBook();
		for (String pk_self_opp : pks_self_oppbook) {
			
			String pk_self = pk_self_opp.substring(0,20);
			String pk_other = pk_self_opp.substring(20,40);
			
			String pk_selfaccountbook = "";
			String pk_oppaccountbook = "";
			if (qryvo.getIsself()) {
				pk_selfaccountbook = refOrgBookMap.get(pk_self);
				pk_oppaccountbook = refOrgBookMap.get(pk_other);
			} else {
				pk_oppaccountbook = refOrgBookMap.get(pk_self);
				pk_selfaccountbook = refOrgBookMap.get(pk_other);
			}
			
			if (null == pk_selfaccountbook || pk_selfaccountbook.trim().length() == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			
			if (null == pk_oppaccountbook || pk_oppaccountbook.trim().length() == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			if (resultVOs == null || resultVOs.length == 0) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			if(!dataSet.contains(pk_selfaccountbook+pk_oppaccountbook)){
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			}
			ResultOccurTabDetailVO[] occurDetailVOs = resultVOs[0].getOccurDetailVOs();
			ResultOccurTabDetailVO currentTabVO = null;
			for (int i = 0; i < occurDetailVOs.length; i++) {
				ResultOccurTabDetailVO resultDetailTabVO = occurDetailVOs[i];
				String self_accountingbook = resultDetailTabVO.getPk_self();
				String other_accountingbook = resultDetailTabVO.getPk_opp();
				if ((self_accountingbook.equals(pk_selfaccountbook) && other_accountingbook.equals(pk_oppaccountbook))||
						(self_accountingbook.equals(pk_oppaccountbook) && other_accountingbook.equals(pk_selfaccountbook))) {
					currentTabVO = resultDetailTabVO;
					break;
				}
			}
			if (currentTabVO == null) {
				rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
				continue;
			} 
			if (qryvo.isDriect()) {

				if (qryvo.getContenttype().equals(ContentType.AMOUNT)) {
					UFDouble bal_accum_quantity = currentTabVO.getBal_accum_quantity();
					// 判断数量累计发生差额是否为0
					if (!UFDouble.ZERO_DBL.equals(bal_accum_quantity == null ? UFDouble.ZERO_DBL : bal_accum_quantity)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.ORGTYPE)) {
					UFDouble bal_accum_orgcur = currentTabVO.getBal_accum_orgcur();
					// 判断组织本币累计发生差额是否为0
					if (!UFDouble.ZERO_DBL.equals(bal_accum_orgcur == null ? UFDouble.ZERO_DBL : bal_accum_orgcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GLOBALTYPE)) {
					UFDouble bal_accum_globalcur = currentTabVO.getBal_accum_globalcur();
					// 判断全局本币累计发生差额是否为0
					if (!UFDouble.ZERO_DBL.equals(bal_accum_globalcur == null ? UFDouble.ZERO_DBL : bal_accum_globalcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GROUPTYPE)) {
					UFDouble bal_accum_groupcur = currentTabVO.getBal_accum_groupcur();
					// 判断集团本币累计发生差额是否为0
					if (!UFDouble.ZERO_DBL.equals(bal_accum_groupcur == null ? UFDouble.ZERO_DBL : bal_accum_groupcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}else if (qryvo.getContenttype().equals(ContentType.ORIGAMOUNT)) {
					UFDouble bal_occur_cur = currentTabVO.getBal_occur_cur();
					// 判断集团本币累计发生差额是否为0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_cur == null ? UFDouble.ZERO_DBL : bal_occur_cur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}
			
			}
			// 准备临时科目表
			// boolean bSelf=qryvo.getIsself();
			// new String[]{(bSelf? pk_selfaccountbook : pk_oppaccountbook)}, (bSelf? pk_selfaccountbook : pk_oppaccountbook),qryvo.getPk_end_period().substring(0,4), qryvo.getPk_end_period(), "",
			// nc.vo.gl.contrast.rule.ContrastRuleSubjUtil.getSubjCodes(voCondition.getContrastRuleVO(), bSelf)
//			String acctab = nc.vo.gateway60.itfs.AccountUtilGL.getEnableSubjTmpTableNew(new String[] {
//					pk_selfaccountbook
//			}, pk_selfaccountbook, year, period, "", new String[] {
//					qryvo.getAccountcode()
//			}, null, false, false);
			//
//			ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(qryvo.getContrastrulevo(), qryvo.getContrastrulevo().getPk_group());
			StringBuilder content = new StringBuilder();
			// MODIFY BYWANGXWB 借-贷 贷-借 根据科目方向决定
			// content.append("select sum(").append(this.getValueField(qryvo.getContenttype())).append(") from gl_contrastdata cdata ");
			content.append("select sum(").append(this.getValueField(qryvo.getContenttype(), acctab)).append(") from gl_contrastdata cdata ");
			content.append("   inner join " + acctab + " acc on acc." + AccAsoaVO.PK_ACCASOA + "=cdata.pk_accasoa ");
			
			// 拼接辅助核算
			if (assSql.isNeedAss()) {
				content.append(assSqlStr);
			}
			content.append("where CDATA.PK_ACCOUNTINGBOOK=? ");
			content.append("   and CDATA.PK_OTHERORGBOOK=? ");
//			if(qryvo.isDriect())
//				content.append("   and CDATA.ISCONTRASTED='Y' ");//直接生成分录的话增加勾对的条件；

			content.append("   and CDATA.YEARV=? ");
			content.append("   and isnull(cdata.dr,0)=0  and discardflag ='N' ");
			if(assSql.isNeedAss()){//说明是UCHECKBYKEY取数（取已对符）
				if(qryvo.isUncontrast()){
					content.append(" and amountequal<>'Y' ");
					content.append("   and CDATA.PREPAREDDATEV<='"+qryvo.getPk_end_period()+  "' ");
				}else{
					content.append(" and amountequal='Y' ");
					content.append("   and CDATA.periodv<='"+qryvo.getPk_end_period().substring(5,7)+  "' ");
				}
			}
			content.append("   group by acc.pk_accasoa, acc.balanorient  ");
			BaseDAO dao = new BaseDAO();
			SQLParameter parameter = new SQLParameter();
			parameter.addParam(pk_selfaccountbook);
			parameter.addParam(pk_oppaccountbook);
			//parameter.addParam(qryvo.getPk_end_period());//起始结束时间
			//parameter.addParam(qryvo.getContrastrulevo().getStartdate());//起始结束时间
			
			parameter.addParam(year);
			CountQryProcessor processor = new CountQryProcessor();
			double result = queryFlag(content, dao, parameter, processor);
			
			rtMap.put(pk_self_opp, new UFDouble(result));
		}
		
		return rtMap;
	}

	private static class CountQryProcessor extends BaseProcessor {
		private static final long serialVersionUID = 8032132243796225L;

		@Override
		public Object processResultSet(ResultSet rs) throws SQLException {
			double iCount = 0;
			if (rs.next()) {
				iCount = rs.getDouble(1);
			}
			return iCount;
		}
	}

}
