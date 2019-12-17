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
 * �ڲ����׶��˹�HBBBȡ����
 * 
 * @modified by jiaah ֱ�Ӷ���ģ��ͼ�Ӷ���ģ����isDirect���ƣ�
 * ����ͨ��gl_contrastdata���iscontrasted����ֱ�ӻ��Ǽ��ȡ��
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
	 * ��Ŀ����0��1��
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

		//�������Ϊ�գ�������ԭ�����߼�������ȡ�������-
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

		// ��Ҫ���ص�map������keyΪ����+�Է�
		Map<String, UFDouble> rtMap = new HashMap<String, UFDouble>();

		String period = qryvo.getPk_accend_period();
		String year = qryvo.getPk_accend_period().substring(0, 4);

		ResultListTabVO[] resultVOs = getTotalResult(qryvo, period, year,false);

		ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(
				qryvo, qryvo.getContrastrulevo().getPk_group());
		String assSqlStr = "";
		// ƴ�Ӹ�������
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
			
			// �����ֱ��ȡ��ģ��
			// �ܶ���˱��桱�Ķ�Ӧ���Ϊ���㡱ʱ�����ɵ�����¼
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
					// �ж��������������Ƿ�Ϊ0
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
					// �ж���֯�������������Ƿ�Ϊ0
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
					// �ж�ȫ�ֱ������������Ƿ�Ϊ0
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
					// �жϼ��ű������������Ƿ�Ϊ0
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
					// �ж�ԭ�����������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL
							.equals(balance_yearinit_cur == null ? UFDouble.ZERO_DBL
									: balance_yearinit_cur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}
			
			}
			// ׼����ʱ��Ŀ��
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
			// MODIFY BYWANGXWB ��-�� ��-�� ���ݿ�Ŀ�������
			// content.append("select sum(").append(this.getValueField(qryvo.getContenttype())).append(") from gl_contrastdata cdata ");
			content.append("select sum(").append(
					this.getValueField(qryvo.getContenttype(), acctab)).append(
					") from gl_contrastdata cdata ");
			content.append("   inner join " + acctab + " acc on acc."
					+ AccAsoaVO.PK_ACCASOA + "=cdata.pk_accasoa ");

			// ƴ�Ӹ�������
			if (assSql.isNeedAss()) {
				content.append(assSqlStr);
			}
			content.append("where CDATA.PK_ACCOUNTINGBOOK=? ");
			content.append("   and CDATA.PK_OTHERORGBOOK=? ");
//			content.append("   and CDATA.PERIODV='00' ");
			content.append("   and CDATA.yearinit='Y' ");
			content.append("   and CDATA.YEARV=? ");
			content.append("   and isnull(cdata.dr,0)=0  and discardflag ='N'");
			if(assSql.isNeedAss()){//˵����UCHECKBYKEYȡ����ȡ�ѶԷ���
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

		// ��Ҫ���ص�map������keyΪ����+�Է�
		Map<String, UFDouble> rtMap = new HashMap<String, UFDouble>();

		String period = qryvo.getPk_accend_period();
		String year = qryvo.getPk_accend_period().substring(0, 4);

		ResultListTabVO[] resultVOs = getTotalResult(qryvo, period, year,true);

		ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(
				qryvo, qryvo.getContrastrulevo().getPk_group());
		String assSqlStr = "";
		// ƴ�Ӹ�������
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
					// �ж���������������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_quantity == null ? UFDouble.ZERO_DBL : bal_occur_quantity)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.ORGTYPE)) {
					UFDouble bal_occur_orgcur = currentTabVO.getBal_occur_orgcur();
					// �ж���֯���Ҿ���������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_orgcur == null ? UFDouble.ZERO_DBL : bal_occur_orgcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GLOBALTYPE)) {
					UFDouble bal_occur_globalcur = currentTabVO.getBal_occur_globalcur();
					// �ж�ȫ�ֱ��Ҿ���������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_globalcur == null ? UFDouble.ZERO_DBL : bal_occur_globalcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GROUPTYPE)) {
					UFDouble bal_occur_groupcur = currentTabVO.getBal_occur_groupcur();
					// �жϼ��ű��Ҿ���������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_groupcur == null ? UFDouble.ZERO_DBL : bal_occur_groupcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}else if (qryvo.getContenttype().equals(ContentType.ORIGAMOUNT)) {
					UFDouble bal_occur_cur = currentTabVO.getBal_occur_cur();
					// �жϼ��ű��Ҿ���������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_cur == null ? UFDouble.ZERO_DBL : bal_occur_cur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}
			}
			double result = 0;
			//�������Ϊ�գ�������ԭ�����߼�������ȡ�������-
			if(StrTools.isEmptyStr(qryvo.getPk_timecontrol_b())){
				StringBuilder content = new StringBuilder();
				content.append("select sum(").append(this.getValueField(qryvo.getContenttype(), acctab)).append(") from gl_contrastdata cdata ");
				content.append("   inner join " + acctab + " acc on acc." + AccAsoaVO.PK_ACCASOA + "=cdata.pk_accasoa ");
				// ƴ�Ӹ�������
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
				if(assSql.isNeedAss()){//˵����UCHECKBYKEYȡ����ȡ�ѶԷ���
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
//				parameter.addParam(qryvo.getPk_end_period());//��ʼ����ʱ��
//				parameter.addParam(qryvo.getPk_start_period());//��ʼ����ʱ��
//				parameter.addParam(qryvo.getContrastrulevo().getStartdate());//������ʼʱ��
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
					//�������ڣ� ��Ҫ���������ȡ���������䡣
					UFDate date = new UFDate(qryvo.getPk_end_period()).asEnd();
					String fipubtmp_timectrl = TimeCtrlUtil.getTimeCtrlTmpTableForGL(timectrol.get(0)[2].toString(), date) + " timectrl ";
					List<Object[]> startEndDate = (List<Object[]>)new BaseDAO().executeQuery("select startdate,enddate from " + fipubtmp_timectrl + " where propertyid = '"+timecontrol[1]+"'", new ArrayListProcessor());
					UFDate startdate = new UFDate(startEndDate.get(0)[0].toString()).asBegin();
					UFDate enddate = new UFDate(startEndDate.get(0)[1].toString()).asEnd();
					
					StringBuilder content = new StringBuilder();

					//�����������_�����ƣ��˲���ȡ���ʵ����ڣ�
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
					// ƴ�Ӹ�������
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
					//���˵�ֻʣ�¼��ű��ҵĺ�����¼
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
					parameter.addParam(enddate.toString());//��ʼ����ʱ��
					parameter.addParam(startdate.toString());//��ʼ����ʱ��
//					parameter.addParam(year);
					
					//parameter.addParam(qryvo.getContrastrulevo().getStartdate());//������ʼʱ��
//					parameter.addParam(enddate.toString());//��ʼ����ʱ��
//					parameter.addParam(startdate.toString());//��ʼ����ʱ��
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
		//1.ɾ��������
		String deletesql = " delete from tmpbd_currate ";
		dao.executeUpdate(deletesql);
		
		//2.����������
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
		//������Ŀ�������ͬ�����
		dao.executeUpdate(" insert into tmpbd_currate values ('" + groupCurrpk + "',1) ");
		
	}

	public Map<String,UFDouble> getQCBalanceBatch(ContrastHBBBQryVO qryvo,String[] pks_self_oppbook) throws BusinessException {
		
		//�������Ϊ�գ�������ԭ�����߼�������ȡ�������-
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

		// ��Ҫ���ص�map������keyΪ����+�Է�
		Map<String, UFDouble> rtMap = new HashMap<String, UFDouble>();

		String period = qryvo.getPk_accend_period();
		String year = qryvo.getPk_accend_period().substring(0, 4);

		ResultListTabVO[] resultVOs = getTotalResult(qryvo, period, year,false);

		ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(
				qryvo, qryvo.getContrastrulevo().getPk_group());
		String assSqlStr = "";
		// ƴ�Ӹ�������
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
					// �ж������ڳ�������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(balance_init_quantity == null ? UFDouble.ZERO_DBL : balance_init_quantity)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.ORGTYPE)) {
					UFDouble balance_init_orgcur = currentTabVO.getBalance_init_orgcur();
					// �ж���֯�����ڳ�������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(balance_init_orgcur == null ? UFDouble.ZERO_DBL : balance_init_orgcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GLOBALTYPE)) {
					UFDouble balance_init_globalcur = currentTabVO.getBalance_init_globalcur();
					// �ж�ȫ�ֱ����ڳ�������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(balance_init_globalcur == null ? UFDouble.ZERO_DBL : balance_init_globalcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GROUPTYPE)) {
					UFDouble balance_init_groupcur = currentTabVO.getBalance_init_groupcur();
					// �жϼ��ű����ڳ�������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(balance_init_groupcur == null ? UFDouble.ZERO_DBL : balance_init_groupcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}else if (qryvo.getContenttype().equals(ContentType.ORIGAMOUNT)) {
					UFDouble balance_init_cur = currentTabVO.getBalance_init_cur();
					// �жϼ��ű����ڳ�������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(balance_init_cur == null ? UFDouble.ZERO_DBL : balance_init_cur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}
			
			
			}
			// ׼����ʱ��Ŀ��
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
			// MODIFY BYWANGXWB ��-�� ��-�� ���ݿ�Ŀ�������
			// content.append("select sum(").append(this.getValueField(qryvo.getContenttype())).append(") from gl_contrastdata cdata ");
			content.append("select sum(").append(this.getValueField(qryvo.getContenttype(), acctab)).append(") from gl_contrastdata cdata ");
			content.append("   inner join " + acctab + " acc on acc." + AccAsoaVO.PK_ACCASOA + "=cdata.pk_accasoa ");
			
			// ƴ�Ӹ�������
			if (assSql.isNeedAss()) {
				content.append(assSqlStr);
			}
			content.append("where CDATA.PK_ACCOUNTINGBOOK=? ");
			content.append("   and CDATA.PK_OTHERORGBOOK=? ");
//			if(qryvo.isDriect())
//				content.append("   and CDATA.ISCONTRASTED='Y' ");//ֱ�����ɷ�¼�Ļ����ӹ��Ե�������
			content.append("   and (cdata.ISINIT='Y'  OR " +
					"(cdata.ISINIT='N' AND CDATA.PREPAREDDATEV<?  and cdata.PREPAREDDATEV>=?))");
			

//			or (cdata.ISINIT='N' and CDATA.PREPAREDDATEV<=? and CDATA.PREPAREDDATEV>=?)
//			content.append("   and CDATA.PREPAREDDATEV<=? and CDATA.PREPAREDDATEV>=? ");
			content.append("   and CDATA.YEARV=? ");
			content.append("   and isnull(cdata.dr,0)=0  and discardflag ='N' ");
			if(assSql.isNeedAss()){//˵����UCHECKBYKEYȡ����ȡ�ѶԷ���
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
			parameter.addParam(qryvo.getContrastrulevo().getStartdate());//���˹�����������
			
			parameter.addParam(year);
			
			CountQryProcessor processor = new CountQryProcessor();
			double result = queryFlag(content, dao, parameter, processor);
			
			rtMap.put(pk_self_opp, new UFDouble(result));
		}
		return rtMap;
	}

	public Map<String,UFDouble> getQMBalanceBatch(ContrastHBBBQryVO qryvo,String[] pks_self_oppbook) throws BusinessException {
		
		//�������Ϊ�գ�������ԭ�����߼�������ȡ�������-
		if(!StrTools.isEmptyStr(qryvo.getPk_timecontrol_b())){
			return getOccurBatch(qryvo, pks_self_oppbook);
		}
		
		if(pks_self_oppbook == null || pks_self_oppbook.length==0 || StringUtils.isEmpty(qryvo.getContrastrulevo().getPk_book())) 
			return null;
		
		Map<String, String> refOrgBookMap = getPk_accountingbooksByFinanceOrgIDAndSetOfBookID(qryvo.getContrastrulevo(),pks_self_oppbook,qryvo.getContrastrulevo().getPk_book());
		
		if(refOrgBookMap.size() ==0)
			return null;
		//��Ҫ���ص�map������keyΪ����+�Է�
		Map<String,UFDouble> rtMap = new HashMap<String, UFDouble>();
		
		String period = qryvo.getPk_accend_period();
		String year = qryvo.getPk_accend_period().substring(0, 4);
		
		ResultListTabVO[] resultVOs = getTotalResult(qryvo, period, year,false);
		
		ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(qryvo, qryvo.getContrastrulevo().getPk_group());
		String assSqlStr = "";
		// ƴ�Ӹ�������
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
					// �ж�������ĩ������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(balance_end_quantity == null ? UFDouble.ZERO_DBL : balance_end_quantity)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.ORGTYPE)) {
					UFDouble balance_end_orgcur = currentTabVO.getBalance_end_orgcur();
					// �ж���֯������ĩ������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(balance_end_orgcur == null ? UFDouble.ZERO_DBL : balance_end_orgcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GLOBALTYPE)) {
					UFDouble balance_end_globalcur = currentTabVO.getBalance_end_globalcur();
					// �ж�ȫ�ֱ�����ĩ������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(balance_end_globalcur == null ? UFDouble.ZERO_DBL : balance_end_globalcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GROUPTYPE)) {
					UFDouble balance_end_groupcur = currentTabVO.getBalance_end_groupcur();
					// �жϼ��ű�����ĩ������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(balance_end_groupcur == null ? UFDouble.ZERO_DBL : balance_end_groupcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}else if (qryvo.getContenttype().equals(ContentType.ORIGAMOUNT)) {
					UFDouble balance_end_cur = currentTabVO.getBalance_end_cur();
					// �жϼ��ű�����ĩ������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(balance_end_cur == null ? UFDouble.ZERO_DBL : balance_end_cur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}
			}
			// ׼����ʱ��Ŀ��
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
			// MODIFY BYWANGXWB ��-�� ��-�� ���ݿ�Ŀ�������
			// content.append("select sum(").append(this.getValueField(qryvo.getContenttype())).append(") from gl_contrastdata cdata ");
			content.append("select sum(").append(this.getValueField(qryvo.getContenttype(), acctab)).append(") from gl_contrastdata cdata ");
			content.append("   inner join " + acctab + " acc on acc." + AccAsoaVO.PK_ACCASOA + "=cdata.pk_accasoa ");
			
			// ƴ�Ӹ�������
			if (assSql.isNeedAss()) {
				content.append(assSqlStr);
			}
			content.append("where CDATA.PK_ACCOUNTINGBOOK=? ");
			content.append("   and CDATA.PK_OTHERORGBOOK=? ");
			
//			if(qryvo.isDriect())
//				content.append("   and CDATA.ISCONTRASTED='Y' ");//ֱ�����ɷ�¼�Ļ����ӹ��Ե�������
			
			content.append(" and (cdata.ISINIT='Y' or (cdata.ISINIT='N' and cdata.PREPAREDDATEV>=? and cdata.PREPAREDDATEV<=?))");
//			content.append("   and CDATA.PREPAREDDATEV<=? and CDATA.PREPAREDDATEV>=? ");
			content.append("   and CDATA.YEARV=? ");
			content.append("   and isnull(cdata.dr,0)=0  and discardflag ='N' ");
			if(assSql.isNeedAss()){//˵����UCHECKBYKEYȡ����ȡ�ѶԷ���
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
			
			parameter.addParam(qryvo.getContrastrulevo().getStartdate());//��ʼ����ʱ��
//			parameter.addParam(qryvo.getPk_start_period());//��ʼ����ʱ��
			parameter.addParam(qryvo.getPk_end_period());//��ʼ����ʱ��
			
			parameter.addParam(year);
			CountQryProcessor processor = new CountQryProcessor();
			double result = queryFlag(content, dao, parameter, processor);
			rtMap.put(pk_self_opp, new UFDouble(result));
		}

		return rtMap;
	}
	
	/**
	 * ���ݲ�����֯���˲�����������ѯ��������˲�
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
		
		//��Ҫ���ص�map������keyΪ����+�Է�
		Map<String,UFDouble> rtMap = new HashMap<String, UFDouble>();
		
		String period = qryvo.getPk_accend_period();
		String year = qryvo.getPk_accend_period().substring(0, 4);
		
		ResultListTabVO[] resultVOs = getTotalResult(qryvo, period, year,true);
		
		ContrastRuleAssSqlProvider assSql = new ContrastRuleAssSqlProvider(qryvo, qryvo.getContrastrulevo().getPk_group());
		String assSqlStr = "";
		// ƴ�Ӹ�������
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
					// �ж������ۼƷ�������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(bal_accum_quantity == null ? UFDouble.ZERO_DBL : bal_accum_quantity)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.ORGTYPE)) {
					UFDouble bal_accum_orgcur = currentTabVO.getBal_accum_orgcur();
					// �ж���֯�����ۼƷ�������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(bal_accum_orgcur == null ? UFDouble.ZERO_DBL : bal_accum_orgcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GLOBALTYPE)) {
					UFDouble bal_accum_globalcur = currentTabVO.getBal_accum_globalcur();
					// �ж�ȫ�ֱ����ۼƷ�������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(bal_accum_globalcur == null ? UFDouble.ZERO_DBL : bal_accum_globalcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				} else if (qryvo.getContenttype().equals(ContentType.GROUPTYPE)) {
					UFDouble bal_accum_groupcur = currentTabVO.getBal_accum_groupcur();
					// �жϼ��ű����ۼƷ�������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(bal_accum_groupcur == null ? UFDouble.ZERO_DBL : bal_accum_groupcur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}else if (qryvo.getContenttype().equals(ContentType.ORIGAMOUNT)) {
					UFDouble bal_occur_cur = currentTabVO.getBal_occur_cur();
					// �жϼ��ű����ۼƷ�������Ƿ�Ϊ0
					if (!UFDouble.ZERO_DBL.equals(bal_occur_cur == null ? UFDouble.ZERO_DBL : bal_occur_cur)) {
						rtMap.put(pk_self_opp, UFDouble.ZERO_DBL);
						continue;
					}
				}
			
			}
			// ׼����ʱ��Ŀ��
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
			// MODIFY BYWANGXWB ��-�� ��-�� ���ݿ�Ŀ�������
			// content.append("select sum(").append(this.getValueField(qryvo.getContenttype())).append(") from gl_contrastdata cdata ");
			content.append("select sum(").append(this.getValueField(qryvo.getContenttype(), acctab)).append(") from gl_contrastdata cdata ");
			content.append("   inner join " + acctab + " acc on acc." + AccAsoaVO.PK_ACCASOA + "=cdata.pk_accasoa ");
			
			// ƴ�Ӹ�������
			if (assSql.isNeedAss()) {
				content.append(assSqlStr);
			}
			content.append("where CDATA.PK_ACCOUNTINGBOOK=? ");
			content.append("   and CDATA.PK_OTHERORGBOOK=? ");
//			if(qryvo.isDriect())
//				content.append("   and CDATA.ISCONTRASTED='Y' ");//ֱ�����ɷ�¼�Ļ����ӹ��Ե�������

			content.append("   and CDATA.YEARV=? ");
			content.append("   and isnull(cdata.dr,0)=0  and discardflag ='N' ");
			if(assSql.isNeedAss()){//˵����UCHECKBYKEYȡ����ȡ�ѶԷ���
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
			//parameter.addParam(qryvo.getPk_end_period());//��ʼ����ʱ��
			//parameter.addParam(qryvo.getContrastrulevo().getStartdate());//��ʼ����ʱ��
			
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
