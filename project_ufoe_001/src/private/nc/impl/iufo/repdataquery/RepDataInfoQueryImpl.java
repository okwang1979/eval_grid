package nc.impl.iufo.repdataquery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.impl.iufo.utils.StringConnectUtil;
import nc.itf.iufo.approveset.IApproveQueryService;
import nc.itf.iufo.check.ICheckResultSrv;
import nc.itf.iufo.repdataquery.IRepDataInfoQuerySrv;
import nc.itf.iufo.ufoe.vorp.IUfoeVorpQuerySrv;
import nc.jdbc.framework.processor.BaseProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.iufo.balance.BalanceBO_Client;
import nc.ui.iufo.check.view.IUfoCheckUtil;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.input.InputUtil;
import nc.ui.iufo.pub.UfoPublic;
import nc.util.iufo.commit.RepDataResultUtil;
import nc.util.iufo.input.BalanceReportExportUtil;
import nc.util.iufo.pub.UFOString;
import nc.util.iufo.repdataright.RepDataAuthUtil;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.check.CheckFormulaVO;
import nc.vo.iufo.check.CheckSchemaVO;
import nc.vo.iufo.checkexecute.CheckExeQueryCondVO;
import nc.vo.iufo.checkexecute.CheckExeResultVO;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.commit.CommitVO;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasurePubDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.param.TwoTuple;
import nc.vo.iufo.pub.IDatabaseNames;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataauth.IRepDataAuthType;
import nc.vo.iufo.repdataauth.RepDataAuthType;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iufo.task.TaskApproveVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iufo.task.TaskVO.DataRightControlType;
import nc.vo.iuforeport.rep.RepShowPrintVO;
import nc.vo.iuforeport.rep.ReportShowVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.trade.pub.IBillStatus;

import org.apache.commons.lang.StringUtils;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iufo.check.vo.CheckConVO;
import com.ufsoft.iufo.check.vo.TaskCheckStateVO;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.fmtplugin.formula.IUfoCheckVO;
import com.ufsoft.iufo.fmtplugin.service.ReportFormatSrv;
import com.ufsoft.iufo.reptemplate.UfoeRepManageFileSrv;
import com.ufsoft.script.util.ICheckResultStatus;
import com.ufsoft.table.CellsModel;

/**
 * 报表数据信息查询服务实现类
 * 
 * @author yp
 * 
 */
public class RepDataInfoQueryImpl implements IRepDataInfoQuerySrv, ICheckResultStatus {
	@SuppressWarnings("unchecked")
	@Override
	public List<CheckExeResultVO> loadCheckExeInfo(IUfoQueryCondVO queryCond, String[] showColumns)
			throws UFOSrvException {
		BaseDAO dao = new BaseDAO();
		KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(queryCond.getKeyGroupPK());
		Map<String, TaskVO> taskMap = new HashMap<String, TaskVO>();
		MeasurePubDataVO pubData = null;
		if (keyGroup.getKeyByKeyPk(KeyVO.CORP_PK) != null && queryCond.getInputState() != 1)
			pubData = isSingleCond(queryCond, taskMap);

		String strSQL = null;
		try {
			strSQL = CheckExeInfoQueryUtil.getCheckExeQuerySql(queryCond, showColumns, pubData);
		} catch (Exception e1) {
			AppDebug.debug(e1);
		}

		if (StringUtils.isEmpty(strSQL))
			return new ArrayList<CheckExeResultVO>();
		try {
			List<CheckExeResultVO> vResult = (List<CheckExeResultVO>) dao.executeQuery(strSQL, new BeanListProcessor(
					CheckExeResultVO.class));
			CheckConVO checkCon = ((CheckExeQueryCondVO) queryCond).getCheckCon();
			if (checkCon.isTaskCheck()) {
				// 任务审核不需要填充
			} else if (checkCon.isFmlCheck()) {
				// 首先填充没有审核的表内公式
				List<CheckExeResultVO> repFmlList = new ArrayList<CheckExeResultVO>();
				if (checkCon.getReports() != null) {
					for (String repPK : checkCon.getReports()) {
						if (!StringUtils.isEmpty(repPK)) {
							Vector<IUfoCheckVO> fmlVector = IUfoCheckUtil.getCheckFmlByRepPK(repPK);
							ReportVO rep = IUFOCacheManager.getSingleton().getReportCache().getByPK(repPK);
							if (fmlVector != null && fmlVector.size() > 0) {
								// repFmlList
								for (IUfoCheckVO check : fmlVector) {
									//
									Vector<IUfoCheckVO> v = checkCon.getRepFormulas().get(repPK);
									boolean isSelectedFml = false;
									for (IUfoCheckVO ic : v) {
										if (ic.getID().equals(check.getID())) {
											isSelectedFml = true;
											break;
										}
									}
									if (isSelectedFml) {
										CheckExeResultVO ce = new CheckExeResultVO();
										ce.setRepcheckstatus("1");
										ce.setCheckcontent(rep.getChangeName() + "-"
												+ StringUtils.trimToEmpty(check.getFmlName()));
										ce.setCheckstatus("1");
										ce.setPk_formula(check.getID());
										ce.setPk_report(repPK);
										ce.setPk_task(checkCon.getTaskId());
										ce.setRepcheckstatus("1");
										ce.setSchemecheckstatus("-1");
										ce.setTaskcheckstatus("1");
										ce.setChecktype("3");
										// TODO
										ce.setPk_org(queryCond.getPk_mainOrg());
										ce.setPubData(pubData);
										repFmlList.add(ce);
									}
								}
							}
						}
					}
				}
				// <关键字组合： List<方案公式审核结果VO>>
				// <关键字组合： List<方案审核结果VO>>
				Map<String, List<CheckExeResultVO>> repFmlMap = new LinkedHashMap<String, List<CheckExeResultVO>>();
				List<CheckExeResultVO> subList = null;
				for (CheckExeResultVO checkExe : vResult) {
					checkExe.setPubData(pubData);
					if (!repFmlMap.containsKey(checkExe.getKeyValue())) {
						subList = new ArrayList<CheckExeResultVO>();
						if (!StringUtils.isEmpty(checkExe.getPk_formula())) {
							repFmlMap.put(checkExe.getKeyValue(), subList);
							subList.add(checkExe);
						} else {
							repFmlMap.put(checkExe.getKeyValue(), subList);
						}
					} else {
						List<CheckExeResultVO> slist = repFmlMap.get(checkExe.getKeyValue());
						if (!StringUtils.isEmpty(checkExe.getPk_formula())) {
							slist.add(checkExe);
						}
					}
				}
				List<CheckExeResultVO> delList = new ArrayList<CheckExeResultVO>();
				List<CheckExeResultVO> needRepFmlFillList = new ArrayList<CheckExeResultVO>();
				Iterator<Map.Entry<String, List<CheckExeResultVO>>> it = repFmlMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, List<CheckExeResultVO>> entry = it.next();
					String key = entry.getKey();
					List<CheckExeResultVO> cList = entry.getValue();
					for (CheckExeResultVO ce : repFmlList) {
						ce.setPubData(pubData);
						boolean exists = false;
						for (CheckExeResultVO c : cList) {
							if (ce.getPk_formula().equals(c.getPk_formula())) {
								exists = true;
								break;
							}
						}
						if (!exists) {
							CheckExeResultVO c = (CheckExeResultVO) ce.clone();
							c.setKeyValue(key);
							needRepFmlFillList.add(c);
						}
					}
				}
				//
				for (CheckExeResultVO cer : vResult) {
					if (StringUtils.isEmpty(cer.getPk_formula())) {
						delList.add(cer);
					}
				}

				vResult.removeAll(delList);
				delList.clear();
				vResult.addAll(needRepFmlFillList);
				// 表内审核公式结束

				// 方案审核公式开始
				List<CheckExeResultVO> needSchemeFmlFillList = new ArrayList<CheckExeResultVO>();
				StringBuilder bufSQL = new StringBuilder();
				StringBuilder schemeFmlSql = new StringBuilder();
				schemeFmlSql
						.append("select '' aloneid,t1.pk_task,3 checktype, (t3.name ||'-' || t2.name) checkcontent,1 taskcheckstatus, 1 checkstatus, 1 schemecheckstatus,-1 repcheckstatus,t2.pk_check_schema pk_scheme,'' pk_report,t2.pk_check_formula pk_formula"
								+ " from iufo_taskscheme t1, iufo_check_formula t2,iufo_check_schema t3 "
								+ "where t1.pk_scheme = t2.pk_check_schema  and t1.scheme_type = 1  and t2.pk_check_schema = t3.pk_check_schema  "
								+ " and t1.pk_task='").append(checkCon.getTaskId()).append("' and ");
				Map<Object, Vector<Object>> schemeFmlMap = checkCon.getSchemaFmls();
				Iterator<Map.Entry<Object, Vector<Object>>> schemeIt = schemeFmlMap.entrySet().iterator();
				// 方案公式审核 sql串拼接
				StringBuilder checkSchemeParam = new StringBuilder("(");
				String[] schemefmlIds = null;
				Vector<Object> schemeFmlVOs = null;
				String schemefmlParam = "";
				CheckSchemaVO checkScheme;
				while (schemeIt.hasNext()) {
					Map.Entry<Object, Vector<Object>> entry = schemeIt.next();

					checkScheme = (CheckSchemaVO) entry.getKey();
					if (checkSchemeParam.length() > 1)
						checkSchemeParam.append(" or ");
					checkSchemeParam.append("(t2.pk_check_schema='").append(checkScheme.getPk_check_schema())
							.append("'");
					schemeFmlVOs = schemeFmlMap.get(checkScheme);
					if (schemeFmlVOs != null && schemeFmlVOs.size() > 0) {
						schemefmlIds = new String[schemeFmlVOs.size()];
						for (int i = 0; i < schemeFmlVOs.size(); i++)
							schemefmlIds[i] = ((CheckFormulaVO) schemeFmlVOs.get(i)).getPk_check_formula();
						schemefmlParam = UFOString.getSqlStrByArr(schemefmlIds);
						checkSchemeParam.append(" and t2.pk_check_formula in").append(schemefmlParam).append(")");
					}

				}
				checkSchemeParam.append(")");
				if (checkSchemeParam.length() > 2) {
					bufSQL.append(schemeFmlSql).append(checkSchemeParam);
				}

				strSQL = bufSQL.toString();

				if (!StringUtils.isEmpty(strSQL)) {
					List<CheckExeResultVO> vSchemeResult = (List<CheckExeResultVO>) dao.executeQuery(strSQL,
							new BeanListProcessor(CheckExeResultVO.class));
					// <关键字组合： List<方案审核结果VO>>
					Map<String, List<CheckExeResultVO>> checkFmlMap = new LinkedHashMap<String, List<CheckExeResultVO>>();
					List<CheckExeResultVO> schemeFmlSubList = null;

					List<CheckExeResultVO> temList = new ArrayList<CheckExeResultVO>();
					Set<String> set = new HashSet<String>();
					set.addAll(Arrays.asList(showColumns));
					strSQL = CheckExeInfoQueryUtil.getSchemeCheckKeycondTable(queryCond, set, pubData, keyGroup,
							checkCon.getTaskId(), checkCon.getReports()).toString();
					// 根据关键字生成VO。便于填充VO到列表中
					List<CheckExeResultVO> vSFMLResult = (List<CheckExeResultVO>) dao.executeQuery(strSQL,
							new BeanListProcessor(CheckExeResultVO.class));

					temList.addAll(vResult);
					temList.addAll(vSFMLResult);

					for (CheckExeResultVO checkExe : temList) {
						if (!checkFmlMap.containsKey(checkExe.getKeyValue())) {
							schemeFmlSubList = new ArrayList<CheckExeResultVO>();
							if (!StringUtils.isEmpty(checkExe.getPk_formula())) {
								checkFmlMap.put(checkExe.getKeyValue(), schemeFmlSubList);
								schemeFmlSubList.add(checkExe);
							}
							// else{
							// schemeFmlSubList.add(checkExe);
							// checkFmlMap.put(checkExe.getKeyValue(),
							// schemeFmlSubList);
							// }
						} else {
							List<CheckExeResultVO> slist = checkFmlMap.get(checkExe.getKeyValue());
							if (!StringUtils.isEmpty(checkExe.getPk_formula())) {
								slist.add(checkExe);
							}
						}
					}

					Iterator<Map.Entry<String, List<CheckExeResultVO>>> schemeFmlit = checkFmlMap.entrySet().iterator();
					while (schemeFmlit.hasNext()) {
						Map.Entry<String, List<CheckExeResultVO>> entry = schemeFmlit.next();
						String key = entry.getKey();
						List<CheckExeResultVO> cList = entry.getValue();
						for (CheckExeResultVO ce : vSchemeResult) {
							ce.setPubData(pubData);
							boolean exists = false;
							for (CheckExeResultVO c : cList) {
								if (!StringUtils.isEmpty(ce.getPk_formula())
										&& ce.getPk_formula().equals(c.getPk_formula())) {
									exists = true;
									break;
								}
							}
							if (!exists) {
								ce.setPubData(pubData);
								CheckExeResultVO c = (CheckExeResultVO) ce.clone();
								c.setKeyValue(key);

								needSchemeFmlFillList.add(c);
							}
						}
					}
					vResult.addAll(needSchemeFmlFillList);
				}

				// 方案审核公式VO填充结束。。

			} else {
				repSchemeCheckFill(dao, vResult, checkCon, pubData);
			}
			String[] alone_ids = fillCheckExePubData(vResult, queryCond, pubData);

			if (alone_ids != null && alone_ids.length > 0) {
				ICheckResultSrv bo = (ICheckResultSrv) NCLocator.getInstance().lookup(ICheckResultSrv.class.getName());

				TaskCheckStateVO[] taskCheckStats = bo.loadTaskCheckState(checkCon.getTaskId(), alone_ids);
				Map<String, TaskCheckStateVO> taskCheckStatsMap = new HashMap<String, TaskCheckStateVO>(
						taskCheckStats.length);
				for (TaskCheckStateVO tct : taskCheckStats) {
					if (!taskCheckStatsMap.containsKey(tct.getAlone_id())) {
						taskCheckStatsMap.put(tct.getAlone_id(), tct);
					}

				}

				for (CheckExeResultVO cer : vResult) {
					// @edit by wuyongc at 2013-8-15,上午9:31:07
					if (cer.getAlone_id() != null) {
						cer.setTaskcheckstatus(taskCheckStatsMap.get(cer.getAlone_id()).getCheckState() + "");
						if (checkCon.isTaskCheck()) {// 如果是任务审核,再此处更新审核人,审核时间
							cer.setCheckstatus(taskCheckStatsMap.get(cer.getAlone_id()).getCheckState() + "");
							cer.setCheckperson(taskCheckStatsMap.get(cer.getAlone_id()).getCheckperson());
							cer.setChecktime(taskCheckStatsMap.get(cer.getAlone_id()).getChecktime());
						}
					}
				}
			}
			// @edit by wuyongc at 2013-8-6,下午6:37:51 拼的数据集合里，可能有的结果中没有aloneId
			// 和pubDataVO，在这里迭代一次，取出可以找到的，然后进行填充。
			Map<String, TwoTuple<String, MeasurePubDataVO>> pubMap = new HashMap<String, TwoTuple<String, MeasurePubDataVO>>();
			List<CheckExeResultVO> needFillList = new ArrayList<CheckExeResultVO>();
			for (CheckExeResultVO checkExe : vResult) {
				if (checkExe.getAlone_id() == null) {
					needFillList.add(checkExe);
				} else {
					if (!pubMap.containsKey(checkExe.getKeyValue())) {
						pubMap.put(checkExe.getKeyValue(),
								new TwoTuple<String, MeasurePubDataVO>(checkExe.getAlone_id(), checkExe.getPubData()));
					}
				}
			}
			for (CheckExeResultVO checkExeResultVO : needFillList) {
				if (pubMap.containsKey(checkExeResultVO.getKeyValue())) {
					TwoTuple<String, MeasurePubDataVO> two = pubMap.get(checkExeResultVO.getKeyValue());
					checkExeResultVO.setAlone_id(two.first);
					checkExeResultVO.setPubData(two.second);
				} else {
					// @edit by wuyongc at 2013-8-16,下午3:04:50 填充不了，就移除了。。
					vResult.remove(checkExeResultVO);
				}
			}
			List<CheckExeResultVO> list = new ArrayList<CheckExeResultVO>();
			for (CheckExeResultVO checkExeResultVO : vResult) {
				if (checkExeResultVO.getPubData() == null) {
					list.add(checkExeResultVO);
				}
			}
			for (CheckExeResultVO checkExeResultVO : list) {
				vResult.remove(checkExeResultVO);
			}
			return vResult;
		} catch (DAOException e) {
			AppDebug.debug(strSQL);
			throw new UFOSrvException(e.getMessage(), e);
		}
	}

	/**
	 * @create by wuyongc at 2011-8-10,下午03:14:36
	 * 
	 * @param dao
	 * @param vResult
	 * @param checkCon
	 * @throws DAOException
	 */
	@SuppressWarnings("unchecked")
	private void repSchemeCheckFill(BaseDAO dao, List<CheckExeResultVO> vResult, CheckConVO checkCon,
			MeasurePubDataVO pubData) throws DAOException {
		String strSQL;
		// 生成 审核方案对应的结果，然后迭代vResult ,如果vResult中没有审核方案结果，则往vResult中填充生成的vo
		String[] schemes = checkCon.getSchemas();
		List<CheckExeResultVO> needSchemeFillList = new ArrayList<CheckExeResultVO>();
		if (schemes != null && schemes.length > 0) {
			StringBuilder schemeSb = new StringBuilder(
					"select 2 checktype,'' alone_id, 1 checkstatus, -1 repcheckstatus,1 schemecheckstatus, 1 taskcheckstatus, c.name checkcontent, b.pk_scheme,'' pk_report, '' pk_formula,b.pk_task from iufo_taskscheme b, iufo_check_schema c where b.pk_scheme=c.pk_check_schema and b.pk_task='");
			schemeSb.append(checkCon.getTaskId()).append("'");
			schemeSb.append(" and c.pk_check_schema in ").append(UFOString.getSqlStrByArr(schemes));
			strSQL = schemeSb.toString();

			List<CheckExeResultVO> vSchemeResult = (List<CheckExeResultVO>) dao.executeQuery(strSQL,
					new BeanListProcessor(CheckExeResultVO.class));
			// <关键字组合： List<方案审核结果VO>>
			Map<String, List<CheckExeResultVO>> schemeMap = new LinkedHashMap<String, List<CheckExeResultVO>>();
			List<CheckExeResultVO> temList = new ArrayList<CheckExeResultVO>();
			temList.addAll(vResult);
			temList.addAll(vSchemeResult);
			List<CheckExeResultVO> schemeSubList = null;
			for (CheckExeResultVO checkExe : temList) {
				checkExe.setPubData(pubData);
				if (!schemeMap.containsKey(checkExe.getKeyValue())) {
					schemeSubList = new ArrayList<CheckExeResultVO>();
					if (!StringUtils.isEmpty(checkExe.getPk_scheme())) {
						schemeMap.put(checkExe.getKeyValue(), schemeSubList);
						schemeSubList.add(checkExe);
					} else {
						schemeMap.put(checkExe.getKeyValue(), schemeSubList);
					}
				} else {
					List<CheckExeResultVO> slist = schemeMap.get(checkExe.getKeyValue());
					if (!StringUtils.isEmpty(checkExe.getPk_scheme())) {
						slist.add(checkExe);
					}
				}
			}

			Iterator<Map.Entry<String, List<CheckExeResultVO>>> schemeMapIt = schemeMap.entrySet().iterator();
			while (schemeMapIt.hasNext()) {
				Map.Entry<String, List<CheckExeResultVO>> entry = schemeMapIt.next();
				String key = entry.getKey();
				List<CheckExeResultVO> cList = entry.getValue();
				for (CheckExeResultVO ce : vSchemeResult) {
					ce.setPubData(pubData);
					boolean exists = false;
					for (CheckExeResultVO c : cList) {
						if (ce.getPk_scheme().equals(c.getPk_scheme())) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						CheckExeResultVO c = (CheckExeResultVO) ce.clone();
						c.setKeyValue(key);
						needSchemeFillList.add(c);
					}
				}
			}
			//

		}

		// 生成 报表审核对应的结果，然后迭代vResult ,如果vResult中没有报表审核结果，则往vResult中填充生成的vo
		String[] repPKs = checkCon.getReports();
		List<CheckExeResultVO> needRepFillList = new ArrayList<CheckExeResultVO>();
		if (repPKs != null && repPKs.length > 0) {
			StringBuilder repSb = new StringBuilder(
					"select 2 checktype,'' alone_id, 1 checkstatus, 1 repcheckstatus,-1 schemecheckstatus, 1 taskcheckstatus, name checkcontent, '' pk_scheme, pk_report, '' pk_formula,'");
			repSb.append(checkCon.getTaskId()).append("' pk_task from iufo_report b where ");
			repSb.append(" b.pk_report in ").append(UFOString.getSqlStrByArr(repPKs));
			strSQL = repSb.toString();

			List<CheckExeResultVO> vRepResult = (List<CheckExeResultVO>) dao.executeQuery(strSQL,
					new BeanListProcessor(CheckExeResultVO.class));
			// <关键字组合： List<方案审核结果VO>>
			Map<String, List<CheckExeResultVO>> repMap = new LinkedHashMap<String, List<CheckExeResultVO>>();
			List<CheckExeResultVO> subList = null;
			for (CheckExeResultVO checkExe : vResult) {
				if (!repMap.containsKey(checkExe.getKeyValue())) {
					subList = new ArrayList<CheckExeResultVO>();
					if (!StringUtils.isEmpty(checkExe.getPk_report())) {
						repMap.put(checkExe.getKeyValue(), subList);
						subList.add(checkExe);
					}
					// else{
					// repMap.put(checkExe.getKeyValue(), subList);
					// }
				} else {
					List<CheckExeResultVO> slist = repMap.get(checkExe.getKeyValue());
					if (!StringUtils.isEmpty(checkExe.getPk_report())) {
						slist.add(checkExe);
					}
				}
			}

			Iterator<Map.Entry<String, List<CheckExeResultVO>>> repMapIt = repMap.entrySet().iterator();
			while (repMapIt.hasNext()) {
				Map.Entry<String, List<CheckExeResultVO>> entry = repMapIt.next();
				String key = entry.getKey();
				List<CheckExeResultVO> cList = entry.getValue();
				for (CheckExeResultVO ce : vRepResult) {
					ce.setPubData(pubData);
					boolean exists = false;
					for (CheckExeResultVO c : cList) {
						if (!StringUtils.isEmpty(ce.getPk_report()) && ce.getPk_report().equals(c.getPk_report())) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						CheckExeResultVO c = (CheckExeResultVO) ce.clone();
						c.setKeyValue(key);
						needRepFillList.add(c);
					}
				}
			}
			//

		}
		List<CheckExeResultVO> delList = new ArrayList<CheckExeResultVO>();
		for (CheckExeResultVO cer : vResult) {
			if (StringUtils.isEmpty(cer.getPk_scheme()) && StringUtils.isEmpty(cer.getPk_report())) {
				delList.add(cer);
			}
		}

		vResult.removeAll(delList);
		delList.clear();
		// 报表审核结果VO填充结束
		vResult.addAll(needSchemeFillList);
		vResult.addAll(needRepFillList);
	}

	private static String[] fillCheckExePubData(List<CheckExeResultVO> vResult, IUfoQueryCondVO queryCond,
			MeasurePubDataVO defaultPubData) throws UFOSrvException {
		Set<String> set = new HashSet<String>();
		if (vResult == null || vResult.size() == 0) {
			return null;
		}
		TaskVO task = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(queryCond.getPk_task());
		KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(queryCond.getKeyGroupPK());
		KeyVO[] keys = keyGroup.getKeys();

		Map<String, MeasurePubDataVO> hashPubData = new HashMap<String, MeasurePubDataVO>();
		Map<Integer, String> keyValMap = new HashMap<Integer, String>();

		int keysLen = keys.length;
		for (CheckExeResultVO result : vResult) {
			for (int i = 0; i < keysLen; i++) {
				String strKeyVal = result.getKeywordByIndex(i + 1);
				if (StringUtils.isNotEmpty(strKeyVal) && !keyValMap.containsKey(i)) {
					keyValMap.put(i, strKeyVal);
				}
				if (keyValMap.size() == keysLen)
					break;
			}
			if (keyValMap.size() == keysLen)
				break;
		}
		for (CheckExeResultVO result : vResult) {
			if (result.getAlone_id() != null) {
				MeasurePubDataVO pubData = hashPubData.get(result.getAlone_id());
				if (pubData == null) {
					if (defaultPubData == null) {
						pubData = new MeasurePubDataVO();

					} else
						pubData = (MeasurePubDataVO) defaultPubData.lightClone();
					pubData.setKType(queryCond.getKeyGroupPK());
					pubData.setKeyGroup(keyGroup);
					for (int i = 0; i < keys.length; i++) {
						String strKeyVal = result.getKeywordByIndex(i + 1);
						if (StringUtils.isEmpty(strKeyVal) && keyValMap.containsKey(i)) {
							strKeyVal = keyValMap.get(i);
						}
						pubData.setKeywordByIndex(i + 1, strKeyVal);
					}
					// 设置会计期间
					pubData.setAccSchemePK(task.getPk_accscheme());

					// pubData.setAloneID(MeasureDataUtil.getAloneID(pubData));
					try {
						pubData.setAloneID(MeasurePubDataBO_Client.getAloneID(pubData));
					} catch (Exception e) {
						throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0",
								"01820001-1141")/* @res "取得 AloneID失败" */, e);
					}
					hashPubData.put(pubData.getAloneID(), pubData);
				}
				result.setPubData(pubData);
			} else {
				if (defaultPubData != null) {
					MeasurePubDataVO pubData = (MeasurePubDataVO) defaultPubData.lightClone();
					pubData.setKeywordByIndex(1, result.getKeyword1());
					pubData.setAloneID(MeasureDataUtil.getAloneID(pubData));
					if (hashPubData.get(pubData.getAloneID()) != null) {
						pubData = hashPubData.get(pubData.getAloneID());
					} else {
						hashPubData.put(pubData.getAloneID(), pubData);
					}
					result.setPubData(pubData);
					result.setAlone_id(pubData.getAloneID());
					// 设置会计期间
					pubData.setAccSchemePK(task.getPk_accscheme());
				}

			}
			// @edit by wuyongc at 2013-8-15,上午9:25:18
			// 可能没有选择全部的关键字，导致defaultPubData可能为null，则result的aloneid也可能为null
			if (result.getAlone_id() != null)
				set.add(result.getAlone_id());
		}
		return set.toArray(new String[0]);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public List<RepDataQueryResultVO> loadRepDataInfo(IUfoQueryCondVO queryCond, String[] showColumns, String busiDate)
			throws UFOSrvException {
		try {
			BaseDAO dao = new BaseDAO();
			// taskPK : TaskVO
			Map<String, TaskVO> taskVOMap = new HashMap<String, TaskVO>();
			// 查询任务VO封装到taskVOMap中
			TaskVO[] tasks = TaskSrvUtils.getTaskVOsByIds(queryCond.getTaskPKs());
			for (TaskVO task : tasks) {
				taskVOMap.put(task.getPk_task(), task);
			}
			KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(queryCond.getKeyGroupPK());
			MeasurePubDataVO pubData = null;
			if (keyGroup.getKeyByKeyPk(KeyVO.CORP_PK) != null && queryCond.getInputState() != 1)
				pubData = isSingleCond(queryCond, taskVOMap);

			String strSQL = RepDataInfoQueryUtil.getRepDataQuerySQL(queryCond, showColumns, pubData);

			List vResult = (List) dao.executeQuery(strSQL, new BeanListProcessor(RepDataQueryResultVO.class));
			List<RepDataQueryResultVO> vRetResult = new ArrayList<RepDataQueryResultVO>();
			if (vResult != null)
				vRetResult.addAll(vResult);

			fillMeasurePubData(vRetResult, queryCond, pubData, taskVOMap);

			/*
			 * 主要为了取得任务审核结果
			 * 
			 * 取得任务审核结果极其费劲, 后续待优化需要注意的是 报表数据查询可能是多个任务根据查询结果知道
			 */
			Map<String, Set<String>> taskAloneMap = new HashMap<String, Set<String>>();
			String taskPK = "";
//			Set<String> aloneSet = null;
			// Map<String, Integer> finalFileCountMap = new HashMap<String,
			// Integer>();
			initFinalFileCountMap(vRetResult);
			//editor tianjlc 减少数据查询语句修改添加
			List<String> taskPKs=new ArrayList<String>();
			List<String> aloneids=new ArrayList<String>();
			for (RepDataQueryResultVO rqVO : vRetResult){
				taskPKs.add(rqVO.getPk_task());
				aloneids.add(rqVO.getAlone_id());
			}
			Map<String,TaskApproveVO> taskApproveVOMap=new HashMap<String,TaskApproveVO>();
			IApproveQueryService approveQuerySrv = NCLocator.getInstance().lookup(IApproveQueryService.class);
			taskApproveVOMap=approveQuerySrv.getTaskApproveMap(taskPKs, aloneids);
			for (RepDataQueryResultVO rqVO : vRetResult) {
				if (rqVO.getRepcheckstate().intValue() == ICheckResultStatus.NOPASS
						|| rqVO.getRepcheckstate().intValue() == ICheckResultStatus.NOCHECK) {
					// 如果表内审核状态为不通过,或者未审核,那么任务审核状态一定和表内审核状态一致,不需要再处理任务审核状态
					rqVO.setTaskcheckstate(rqVO.getRepcheckstate());
					continue;
				}
				if (rqVO.getTaskcheckstate().intValue() == ICheckResultStatus.NOPASS
						|| rqVO.getTaskcheckstate().intValue() == ICheckResultStatus.PASS) {
					// 如果任务审核状态是未通过或者通过,表明取到了任务审核状态,那么也不需要再处理任务审核状态
					continue;
				}
				// 剩下 如刚好此表没有审核公式,会查询出 表内审核状态为 没有审核公式,此种情况,需要进行下列查询取到正确的任务审核状态.
				initTaskAloneMap(rqVO, taskAloneMap);

			}
			Map<String, Integer> taskCheckStatsMap = new HashMap<String, Integer>();
			Set<Map.Entry<String, Set<String>>> mapSet = taskAloneMap.entrySet();
			ICheckResultSrv bo = null;
			for (Map.Entry<String, Set<String>> entry : mapSet) {
				if (entry.getValue().size() > 0) {
					taskPK = entry.getKey();
					String[] alone_ids = entry.getValue().toArray(new String[0]);
					if (bo == null)
						bo = (ICheckResultSrv) NCLocator.getInstance().lookup(ICheckResultSrv.class.getName());

					TaskCheckStateVO[] taskCheckStats = bo.loadTaskCheckState(taskPK, alone_ids);

					for (TaskCheckStateVO tct : taskCheckStats) {
						if (!taskCheckStatsMap.containsKey(tct.getPk_task() + "@" + tct.getAlone_id())) {
							taskCheckStatsMap.put(tct.getPk_task() + "@" + tct.getAlone_id(), tct.getCheckState());
						}
					}

				}
			}
			for (RepDataQueryResultVO repData : vRetResult) {
				if (taskCheckStatsMap.get(repData.getPk_task() + "@" + repData.getAlone_id()) != null)
					repData.setTaskcheckstate(taskCheckStatsMap.get(repData.getPk_task() + "@" + repData.getAlone_id()));
			}
			//delete tianjlc 改为一次性取出
//			Map<String, Integer> taskApproveMap = new HashMap<String, Integer>();

			// Map<String,Integer> finalFileCountMap = new HashMap<String,
			// Integer>();
			// FinalReportFileVO[] vos = null;
			// Integer finalFileCount = null;

//			Integer taskApproveStat = null;
			for (RepDataQueryResultVO rqVO : vRetResult) {
//				taskApproveStat = taskApproveMap.get(rqVO.getPk_task() + rqVO.getAlone_id());
//				if (taskApproveStat != null) {
//					rqVO.setApprovestatus(taskApproveStat);
//				} else {
				// 增加上报或者确认审批状态
				Integer flowtype;
				if (rqVO.getCommit_state() == null
						|| rqVO.getCommit_state().intValue() < CommitStateEnum.STATE_COMMITED.getIntValue()) {
					// 上报审批流
					flowtype = TaskApproveVO.FLOWTYPE_COMMIT;
				} else {
					// 确认审批流
					flowtype = TaskApproveVO.FLOWTYPE_AFFIRM;
				}
				//editor tianjlc 减少数据查询语句，将数据一次性取出
//					TaskApproveVO taskapprovevo = approveQuerySrv.getTaskApprove(rqVO.getPk_task(), rqVO.getAlone_id(),
//							flowtype);
				if (taskApproveVOMap!=null&&!taskApproveVOMap.isEmpty()) {
					TaskApproveVO taskAproveVO=taskApproveVOMap.get(rqVO.getPk_task() + rqVO.getAlone_id()+flowtype);
					Integer vBillStatus=taskAproveVO==null?IBillStatus.FREE:taskAproveVO.getVbillstatus();
//						taskApproveMap.put(rqVO.getPk_task() + rqVO.getAlone_id(), vBillStatus);
					rqVO.setApprovestatus(vBillStatus);
				} else {
//						taskApproveMap.put(rqVO.getPk_task() + rqVO.getAlone_id(), IBillStatus.FREE);
					rqVO.setApprovestatus(IBillStatus.FREE);
				}
//				}

				// if(finalFileCount != null){
				// rqVO.setFinalfilecount(finalFileCount);
				// }else{
				// vos =
				// UfoeRepManageFileSrv.loadFinalReportFileVOByTaskId(rqVO.getPk_task(),
				// rqVO.getAlone_id());
				// if(vos == null){
				// rqVO.setFinalfilecount(0);
				// finalFileCountMap.put(rqVO.getPk_task() + rqVO.getAlone_id(),
				// 0);
				// }else{
				// rqVO.setFinalfilecount(vos.length);
				// finalFileCountMap.put(rqVO.getPk_task() + rqVO.getAlone_id(),
				// vos.length);
				// }
				//
				// }
			}
			// 填充数据来源
			// ICommitQueryService commitSrv =
			// NCLocator.getInstance().lookup(ICommitQueryService.class);
			// for (RepDataQueryResultVO rqVO : vRetResult) {
			// RepDataCommitVO[] repDataCmtVos =
			// commitSrv.getReportCommitState(new String[]{rqVO.getAlone_id()},
			// rqVO.getPk_report());
			// if (repDataCmtVos != null && repDataCmtVos.length == 1){
			// rqVO.setDataorigin(repDataCmtVos[0].getDataorigin());
			// }
			// }

			fillAllSubCommited(vRetResult, queryCond, keyGroup, taskVOMap, pubData, busiDate);
			// 填充权限字段内容到VO
			fillAuth(vRetResult, queryCond, taskVOMap);

			return vRetResult;
		} catch (Exception e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}
	}

	private void initTaskAloneMap(RepDataQueryResultVO rqVO, Map<String, Set<String>> taskAloneMap) {
		if (taskAloneMap.containsKey(rqVO.getPk_task())) {
			taskAloneMap.get(rqVO.getPk_task()).add(rqVO.getAlone_id());
		} else {
			Set<String> aloneSet = new HashSet<String>();
			aloneSet.add(rqVO.getAlone_id());
			taskAloneMap.put(rqVO.getPk_task(), aloneSet);
		}
	}

	/**
	 * 将报表管理报告数量set进RepDataQueryResultVO
	 * 
	 * @creator tianjlc at 2015-4-20 下午1:34:27
	 * @param vRetResult
	 * @param finalFileCountMap
	 * @throws UFOSrvException
	 * @return void
	 */
	private void initFinalFileCountMap(List<RepDataQueryResultVO> vRetResult) throws UFOSrvException {
		if (vRetResult == null || vRetResult.size() == 0)
			return;
		// editor tianjlc 管理报告数量默认值不能为null
		Integer finalFileCount = 0;
		// 改进查询效率:按任务分组，查询后填充
		Map<String, List<String>> groupRetResultId = new HashMap<String, List<String>>();
		for (RepDataQueryResultVO rqVO : vRetResult) {
			List<String> tempRetResult = groupRetResultId.get(rqVO.getPk_task());
			if (tempRetResult == null){
				tempRetResult = new ArrayList<String>();
				groupRetResultId.put(rqVO.getPk_task(), tempRetResult);
			}
			tempRetResult.add(rqVO.getAlone_id());
		}
		
		for (String pk_task : groupRetResultId.keySet()){
			Map<String, Integer> subFileNumMap = UfoeRepManageFileSrv.loadFinalReportFileVOByTaskAndAloneIds(
					pk_task, groupRetResultId.get(pk_task).toArray(new String[0]));
			
			for (RepDataQueryResultVO rqVO : vRetResult) {
				if (rqVO.getPk_task().equals(pk_task)){
					finalFileCount = subFileNumMap.get(rqVO.getAlone_id() + "_" + rqVO.getPk_task());
					rqVO.setFinalfilecount(finalFileCount);
				}
			}
		}
//		for (RepDataQueryResultVO rqVO : vRetResult) {
//			Map<String, Integer> subFileNumMap = UfoeRepManageFileSrv.loadFinalReportFileVOByTaskAndAloneIds(
//					rqVO.getPk_task(), new String[] { rqVO.getAlone_id() });
//			// 填充管理报告数据量到VO
//			finalFileCount = subFileNumMap.get(rqVO.getAlone_id() + "_" + rqVO.getPk_task());
//			rqVO.setFinalfilecount(finalFileCount);
//		}
	}

	// TODO
	// private static void fillAuth2(List<RepDataQueryResultVO>
	// vResult,IUfoQueryCondVO queryCond,Map<String,TaskVO> taskMap) throws
	// Exception{
	// String rmsPK = queryCond.getPk_rms();
	// String mainOrgPK = queryCond.getPk_mainOrg();
	// Map<String,Boolean> tasksNeedAuth = new HashMap<String, Boolean>();
	//
	// for(TaskVO vo : taskMap.values()){
	// if(vo.getData_contype() !=
	// DataRightControlType.TYPE_NOTCONTROL.ordinal())
	// tasksNeedAuth.put(vo.getPk_task(), true);
	// else
	// tasksNeedAuth.put(vo.getPk_task(), false);
	// }
	//
	// List<RepAuthParam> paramList = new ArrayList<RepAuthParam>();
	//
	// Map<RepAuthParam,RepDataQueryResultVO> map = new HashMap<RepAuthParam,
	// RepDataQueryResultVO>();
	// for(RepDataQueryResultVO rq : vResult){
	// rq.setRepdataright(2);
	// if(tasksNeedAuth.get(rq.getPk_task()) != null &&
	// tasksNeedAuth.get(rq.getPk_task())){
	// RepAuthParam repAuthParam = new RepAuthParam(rq.getPk_report(),
	// rq.getPk_org(), rmsPK, mainOrgPK, rq.getPk_task());
	// paramList.add(repAuthParam);
	// map.put(repAuthParam, rq);;
	// }
	// }
	//
	// RepAuthParam[] paramArray = paramList.toArray(new RepAuthParam[0]);
	// Map<RepAuthParam, RepDataAuthType> authMap =
	// RepDataAuthUtil.getAuthTypeMap(paramArray);
	// for (RepAuthParam repAuthParam : paramArray) {
	// map.get(repAuthParam).setRepdataright(getRepDataAuth(authMap.get(repAuthParam)));
	// }
	//
	// }
	private static void fillAuth(List<RepDataQueryResultVO> vResult, IUfoQueryCondVO queryCond,
			Map<String, TaskVO> taskMap) throws Exception {
		String userId = queryCond.getPk_user();
		String rmsPK = queryCond.getPk_rms();
		String mainOrgPK = queryCond.getPk_mainOrg();

		Map<String, Boolean> tasksNeedAuth = new HashMap<String, Boolean>();

		for (TaskVO vo : taskMap.values()) {
			if (vo.getData_contype() != DataRightControlType.TYPE_NOTCONTROL.ordinal())
				tasksNeedAuth.put(vo.getPk_task(), true);
			else
				tasksNeedAuth.put(vo.getPk_task(), false);
		}

		// Map<String,Boolean>tasksNeedAuth =
		// RepDataAuthUtil.isNeedControlDataAuth(queryCond.getTaskPKs());
		// TODO
		for (RepDataQueryResultVO rq : vResult) {
			rq.setRepdataright(2);
			if (tasksNeedAuth.get(rq.getPk_task()) != null && tasksNeedAuth.get(rq.getPk_task())) {
				RepDataAuthType repAuth = RepDataAuthUtil.getAuthType(userId, rq.getPk_report(), rq.getPk_org(), rmsPK,
						mainOrgPK, rq.getPk_task());
				rq.setRepdataright(getRepDataAuth(repAuth));
			}
		}
	}

	private static int getRepDataAuth(RepDataAuthType repAuth) {
		switch (repAuth) {
		case NONE:
			return IRepDataAuthType.NONE;
		case VIEW:
			return IRepDataAuthType.VIEW;
		case EDIT:
			return IRepDataAuthType.EDIT;
		}
		return IRepDataAuthType.EDIT;
	}

	private static void fillMeasurePubData(List<RepDataQueryResultVO> vResult, IUfoQueryCondVO queryCond,
			MeasurePubDataVO defaultPubData, Map<String, TaskVO> taskVOMap) throws Exception {
		KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(queryCond.getKeyGroupPK());
		KeyVO[] keys = keyGroup.getKeys();

		Map<String, MeasurePubDataVO> hashPubData = new HashMap<String, MeasurePubDataVO>();
		TaskVO task = null;
		for (RepDataQueryResultVO result : vResult) {
			task = taskVOMap.get(result.getPk_task());
			if (result.getAlone_id() != null) {
				MeasurePubDataVO pubData = hashPubData.get(result.getAlone_id());
				if (pubData == null) {
					if (defaultPubData == null)
						pubData = new MeasurePubDataVO();
					else
						pubData = (MeasurePubDataVO) defaultPubData.lightClone();
					pubData.setKType(queryCond.getKeyGroupPK());
					pubData.setKeyGroup(keyGroup);
					for (int i = 0; i < keys.length; i++) {
						String strKeyVal = result.getKeywordByIndex(i + 1);
						pubData.setKeywordByIndex(i + 1, strKeyVal);
					}
					// 设置会计期间
					// TaskVO task =
					// IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(result.getPk_task());

					pubData.setAccSchemePK(task.getPk_accscheme());
					pubData.setAloneID(MeasureDataUtil.getAloneID(pubData));
					// pubData.setAloneID(MeasurePubDataBO_Client.getAloneID(pubData));
					hashPubData.put(pubData.getAloneID(), pubData);
				}
				result.setPubData(pubData);
			} else {
				MeasurePubDataVO pubData = (MeasurePubDataVO) defaultPubData.lightClone();
				pubData.setKeywordByIndex(1, result.getKeyword1());
				pubData.setAloneID(MeasureDataUtil.getAloneID(pubData));
				// pubData.setAloneID(MeasurePubDataBO_Client.getAloneID(pubData));
				if (hashPubData.get(pubData.getAloneID()) != null) {
					pubData = hashPubData.get(pubData.getAloneID());
				} else {
					hashPubData.put(pubData.getAloneID(), pubData);
				}
				result.setPubData(pubData);
				result.setAlone_id(pubData.getAloneID());
				// 设置会计期间
				pubData.setAccSchemePK(task.getPk_accscheme());
			}
		}
	}

	private static void fillAllSubCommited(List<RepDataQueryResultVO> vResult, IUfoQueryCondVO queryCond,
			KeyGroupVO keyGroup, Map<String, TaskVO> taskVOMap ,MeasurePubDataVO pubData,String busiDate) throws Exception {
		Map<String, List<RepDataQueryResultVO>> hashResult = new HashMap<String, List<RepDataQueryResultVO>>();
		for (RepDataQueryResultVO result : vResult) {
			String strTaskPK = result.getPk_task();
			List<RepDataQueryResultVO> vOneResult = hashResult.get(strTaskPK);
			if (vOneResult == null) {
				vOneResult = new ArrayList<RepDataQueryResultVO>();
				hashResult.put(strTaskPK, vOneResult);
			}
			vOneResult.add(result);
		}

		String[] strTaskPKs = hashResult.keySet().toArray(new String[0]);
		String rmsVersionPK = queryCond.getPk_rms();
		if(busiDate!=null && pubData!=null){
			rmsVersionPK=NCLocator.getInstance().lookup(IUfoeVorpQuerySrv.class).getRmsVerPk(queryCond.getPk_rms(), pubData, busiDate);
		}
		for (String strTaskPK : strTaskPKs) {
			List<RepDataQueryResultVO> vOneResult = hashResult.get(strTaskPK);
			TaskVO task = taskVOMap.get(strTaskPK);
			RepDataResultUtil.addAllSubCommitedAttr(rmsVersionPK, vOneResult, task, keyGroup);
		}
	}

	/**
	 * 判断查询条件是否是单一条件
	 * 
	 * @param queryCond
	 * @return
	 */
	private static MeasurePubDataVO isSingleCond(IUfoQueryCondVO queryCond, Map<String, TaskVO> taskMap) {
		KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(queryCond.getKeyGroupPK());
		KeyVO[] keys = keyGroup.getKeys();

		MeasurePubDataVO pubData = new MeasurePubDataVO();
		pubData.setKType(queryCond.getKeyGroupPK());
		pubData.setKeyGroup(keyGroup);
		for (KeyVO key : keys) {
			if (key.getPk_keyword().equals(KeyVO.CORP_PK))
				continue;

			if (key.isTTimeKeyVO()) {
				if (UfoPublic.stringIsNull(queryCond.getDate()))
					return null;
				pubData.setKeywordByPK(key.getPk_keyword(), queryCond.getDate());
				continue;
			}

			String strKeyVal = queryCond.getKeyVal(key.getPk_keyword());
			if (UfoPublic.stringIsNull(strKeyVal))
				return null;

			pubData.setKeywordByPK(key.getPk_keyword(), strKeyVal);
		}
		// 前面保证了 这些任务是会计期间方案一致的任务。
		String taskPk = queryCond.getTaskPKs()[0];
		TaskVO task = taskMap.get(taskPk);
		if (task == null) {
			task = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(taskPk);
			taskMap.put(taskPk, task);
		}
		pubData.setAccSchemePK(task.getPk_accscheme());
		MeasurePubDataUtil.initMeasurePubDataKeyData(new MeasurePubDataVO[] { pubData });
		return pubData;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RepDataQueryResultVO> queryRepDataInfo(String[] aryAloneIds, String[] aryRepIds, int ver,
			int repCommitFlag, int checkState) throws UFOSrvException {

		BaseDAO dao = new BaseDAO();

		String strWhereRepCommit = repCommitFlag < 0 ? "" : " coalesce(t2.commit_state,"
				+ CommitStateEnum.STATE_NOCOMMIT.getIntValue() + ") = " + repCommitFlag;
		String strWhereCheckState = checkState < 0 ? "" : " coalesce(t1.checkstate," + NOCHECK + ") = " + checkState;

		String strT1 = "select * from " + IDatabaseNames.IUFO_REP_CHECK + " where aloneid in "
				+ RepDataInfoQueryUtil.arrayToString(aryAloneIds) + " and repid in "
				+ RepDataInfoQueryUtil.arrayToString(aryRepIds);
		strT1 = " (" + strT1 + ") t1 ";

		String strT2 = "select * from iufo_rep_commit where alone_id in "
				+ RepDataInfoQueryUtil.arrayToString(aryAloneIds) + " and pk_report in "
				+ RepDataInfoQueryUtil.arrayToString(aryRepIds);
		strT2 = " (" + strT2 + ") t2 ";

		String strSql = "select coalesce(t1.repid,t2.pk_report) ,coalesce(t1.aloneid,t2.alone_id) ,"
				+ " coalesce(t1.checkstate,0),coalesce(t2.commit_state," + CommitStateEnum.STATE_NOCOMMIT.getIntValue()
				+ ")" + " ,t2.pk_task,coalesce(t2.flag_input, 'N') inputstate ,t2.dataorigin from " + strT1
				+ " full outer " + " join " + strT2 + " on t1.aloneid = t2.alone_id and t1.repid = t2.pk_report";

		if (strWhereRepCommit.length() > 0) {
			strSql += " where " + strWhereRepCommit;
		}

		if (strWhereCheckState.length() > 0) {
			if (strWhereRepCommit.length() > 0) {
				strSql += " and ";
			}
			strSql += " " + strWhereCheckState;
		}

		List<RepDataQueryResultVO> lstResult = new ArrayList<RepDataQueryResultVO>();
		try {
			lstResult = (List<RepDataQueryResultVO>) dao.executeQuery(strSql, new QueryResultProcessor());
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}
		return lstResult;

	}

	private class QueryResultProcessor extends BaseProcessor {
		private static final long serialVersionUID = 803213224379610245L;

		@Override
		public Object processResultSet(ResultSet rs) throws SQLException {
			List<RepDataQueryResultVO> lstResult = new ArrayList<RepDataQueryResultVO>();
			while (rs.next()) {
				RepDataQueryResultVO resultVo = new RepDataQueryResultVO();
				resultVo.setPk_report(rs.getString(1));
				resultVo.setAlone_id(rs.getString(2));
				resultVo.setRepcheckstate(rs.getInt(3));
				resultVo.setRepcommitstate(rs.getInt(4));
				resultVo.setPk_task(rs.getString(5));
				// @edit by wuyongc at 2013-6-8,上午10:46:37
				resultVo.setInputstate(UFBoolean.valueOf(rs.getString(6).equalsIgnoreCase("Y")));
				// 增加数据来源
				resultVo.setDataorigin(rs.getString(7));
				lstResult.add(resultVo);
			}
			return lstResult;
		}
	}

	// wangqi 20110906
	// 当前关键字下如果查询组织没有任何录入及报送则执行strSQL1为null，结果hashCommited也返回true了
	// @SuppressWarnings({ "unchecked", "serial" })
	// @Override
	// public Map<String, UFBoolean> queryAllSubCommited(String
	// strRmsPK,String[] strAloneIDs,KeyGroupVO keyGroup,String taskpk,int
	// iCommitState) throws UFOSrvException {
	// BaseDAO dao = new BaseDAO();
	//
	// Map<String,UFBoolean> hashCommited=new HashMap<String,UFBoolean>();
	// String pubTable=keyGroup.getTableName();
	// KeyVO[] keys=keyGroup.getKeys();
	// String strSQL1="select t1.alone_id, t3.pk_org,t2.ver ";
	// String strSQL2="select t5.keyword1,t5.ver ";
	// for (int i=1;i<keys.length;i++){
	// strSQL1+=",keyword"+(i+1);
	// strSQL2+=",keyword"+(i+1);
	// }
	//
	// strSQL1+=" from iufo_task_commit t1,"+pubTable+" t2, org_rmsmember t3 where  t1.alone_id=t2.alone_id"
	// +" and t3.pk_rms='"+strRmsPK+"' "+
	// " and t2.keyword1=t3.pk_fatherorg and t1.alone_id in "+RepDataInfoQueryUtil.arrayToString(strAloneIDs);
	// strSQL2+=" from iufo_task_commit t4,"+pubTable+" t5 where pk_task='" +
	// taskpk +
	// "' and t4.commit_state >= "+iCommitState+" and t4.alone_id = t5.alone_id";
	//
	// String
	// strSQL="select tmp1.alone_id from ("+strSQL1+") tmp1 left outer join ("+strSQL2+") tmp2"
	// +
	// " on tmp1.pk_org=tmp2.keyword1 and tmp1.ver=tmp2.ver";
	// for (int i=1;i<keys.length;i++){
	// strSQL+=" and tmp1.keyword"+(i+1)+"=tmp2.keyword"+(i+1);
	// }
	//
	// strSQL+=" where isnull(tmp2.keyword1,'~')='~'";
	//
	// Set<String> vFilterAloneID=null;
	// try {
	// vFilterAloneID = (Set<String>)dao.executeQuery(strSQL, new
	// BaseProcessor(){
	// @Override
	// public Object processResultSet(ResultSet rs) throws SQLException {
	// Set<String> vAloneID = new HashSet<String>();
	// while(rs.next()){
	// vAloneID.add(rs.getString(1));
	// }
	// return vAloneID;
	// }
	// });
	// } catch (DAOException e) {
	// AppDebug.debug(e);
	// throw new UFOSrvException(e.getMessage(),e);
	// }
	//
	//
	// for (String strAloneID:strAloneIDs){
	// if (vFilterAloneID.contains(strAloneID))
	// hashCommited.put(strAloneID, UFBoolean.FALSE);
	// else
	// hashCommited.put(strAloneID, UFBoolean.TRUE);
	// }
	// return hashCommited;
	// }

	@SuppressWarnings({ "unchecked" })
	@Override
	public Map<String, UFBoolean> queryAllSubCommited(String strRmsPK, MeasurePubDataVO[] pubDatas,
			KeyGroupVO keyGroup, String taskpk, int iCommitState) throws UFOSrvException {
		BaseDAO dao = new BaseDAO();

		Map<String, UFBoolean> hashCommited = new HashMap<String, UFBoolean>();

		String pubTable = keyGroup.getTableName();
		KeyVO[] keys = keyGroup.getKeys();
		String[] keywords;
		String strSQL;
		String strSQLnew;
		String aloneID;
		Boolean flg;
		ArrayList<CommitVO> lstCommit;

		for (MeasurePubDataVO pubData : pubDatas) {
			flg = true;
			keywords = pubData.getKeywords();
			aloneID = pubData.getAloneID();
			strSQL = " select m.keyword1,t.commit_state from " + pubTable + " m, iufo_task_commit t "
					+ " where m.alone_id=t.alone_id and t.pk_task='" + taskpk + "' and m.ver=0";

			for (int j = 1; j < keys.length; j++) {
				strSQL += " and m.keyword" + (j + 1) + "='" + keywords[j] + "'";
			}

			strSQLnew = "select tmp1.pk_org,tmp2.commit_state from org_reportmanastrumember_v tmp1 left outer join (" + strSQL
					+ ") tmp2" + " on tmp1.pk_org=tmp2.keyword1" + " where tmp1.pk_fatherorg='" + keywords[0]
					+ "' and tmp1.pk_svid='" + strRmsPK + "' "
					+ " and tmp1.pk_org in (select pk_receiveorg from iufo_taskassign where pk_task='" + taskpk + "') ";

			try {
				lstCommit = (ArrayList<CommitVO>) dao.executeQuery(strSQLnew, new BeanListProcessor(CommitVO.class));
			} catch (DAOException e) {
				AppDebug.debug(e);
				throw new UFOSrvException(e.getMessage(), e);
			}

			if (lstCommit != null && lstCommit.size() > 0) {
				for (int k = 0; k < lstCommit.size(); k++) {
					CommitVO commit = lstCommit.get(k);
					if (commit.getCommit_state() == null || commit.getCommit_state().intValue() < iCommitState) {
						hashCommited.put(aloneID, UFBoolean.FALSE);
						flg = false;
						break;
					}
				}
				if (flg) {
					hashCommited.put(aloneID, UFBoolean.TRUE);
				}
			} else {
				hashCommited.put(aloneID, UFBoolean.TRUE);
			}

		}

		return hashCommited;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public Map<String, UFBoolean> queryAllVersionSubCommited(String strRmsVerPK, MeasurePubDataVO[] pubDatas,
			KeyGroupVO keyGroup, String taskpk, int iCommitState) throws UFOSrvException {
		BaseDAO dao = new BaseDAO();

		Map<String, UFBoolean> hashCommited = new HashMap<String, UFBoolean>();

		String pubTable = keyGroup.getTableName();
		KeyVO[] keys = keyGroup.getKeys();
		String[] keywords;
		String strSQL;
		String strSQLnew;
		String aloneID;
		Boolean flg;
		ArrayList<CommitVO> lstCommit;

		for (MeasurePubDataVO pubData : pubDatas) {
			flg = true;
			keywords = pubData.getKeywords();
			aloneID = pubData.getAloneID();
			strSQL = " select m.keyword1,t.commit_state from " + pubTable + " m, iufo_task_commit t "
					+ " where m.alone_id=t.alone_id and t.pk_task='" + taskpk + "' and m.ver=0";

			for (int j = 1; j < keys.length; j++) {
				strSQL += " and m.keyword" + (j + 1) + "='" + keywords[j] + "'";
			}

			strSQLnew = "select tmp1.pk_org,tmp2.commit_state from org_reportManaStruMember_v tmp1 left outer join ("
					+ strSQL + ") tmp2" + " on tmp1.pk_org=tmp2.keyword1" + " where tmp1.pk_fatherorg='" + keywords[0]
					+ "' and tmp1.pk_svid='" + strRmsVerPK + "' "
					+ " and tmp1.pk_org in (select pk_receiveorg from iufo_taskassign where pk_task='" + taskpk + "') ";

			try {
				lstCommit = (ArrayList<CommitVO>) dao.executeQuery(strSQLnew, new BeanListProcessor(CommitVO.class));
			} catch (DAOException e) {
				AppDebug.debug(e);
				throw new UFOSrvException(e.getMessage(), e);
			}

			if (lstCommit != null && lstCommit.size() > 0) {
				for (int k = 0; k < lstCommit.size(); k++) {
					CommitVO commit = lstCommit.get(k);
					if (commit.getCommit_state() == null || commit.getCommit_state().intValue() < iCommitState) {
						hashCommited.put(aloneID, UFBoolean.FALSE);
						flg = false;
						break;
					}
				}
				if (flg) {
					hashCommited.put(aloneID, UFBoolean.TRUE);
				}
			} else {
				hashCommited.put(aloneID, UFBoolean.TRUE);
			}

		}

		return hashCommited;
	}

	@SuppressWarnings({ "unchecked", "serial" })
	@Override
	public Map<String, UFBoolean> queryDirectParentCommited(String[] strAloneIDs, KeyGroupVO keyGroup, String rms,
			String taskpk) throws UFOSrvException {
		String pubTable = keyGroup.getTableName();
		KeyVO[] keys = keyGroup.getKeys();
		String strSQL1 = "select t1.alone_id, t3.pk_fatherorg,t2.ver ";
		String strSQL2 = "select t5.keyword1,t5.ver ";
		for (int i = 1; i < keys.length; i++) {
			strSQL1 += ",keyword" + (i + 1);
			strSQL2 += ",keyword" + (i + 1);
		}
		// tianchuan 20141021 in语句不能超过1000的问题
		String aloneIdSqlIn = StringConnectUtil.getInSqlGroupByArr(strAloneIDs, "t1.alone_id");

		strSQL1 += " from iufo_task_commit t1," + pubTable + " t2, org_rmsmember t3 where  t1.alone_id=t2.alone_id"
				+ " and t2.keyword1=t3.pk_org and t3.pk_rms='" + rms + "' and " + aloneIdSqlIn;
		strSQL2 += " from iufo_task_commit t4," + pubTable + " t5 where pk_task='" + taskpk + "' and  t4.commit_state>"
				+ CommitStateEnum.STATE_BACKED.getIntValue() + " and t4.alone_id = t5.alone_id";

		String strSQL = "select tmp1.alone_id from (" + strSQL1 + ") tmp1 inner join (" + strSQL2 + ") tmp2"
				+ " on tmp1.pk_fatherorg=tmp2.keyword1 and tmp1.ver=tmp2.ver";
		for (int i = 1; i < keys.length; i++) {
			strSQL += " and tmp1.keyword" + (i + 1) + "=tmp2.keyword" + (i + 1);
		}

		BaseDAO dao = new BaseDAO();
		Set<String> vFilterAloneID = null;
		try {
			vFilterAloneID = (Set<String>) dao.executeQuery(strSQL, new BaseProcessor() {
				@Override
				public Object processResultSet(ResultSet rs) throws SQLException {
					Set<String> vAloneID = new HashSet<String>();
					while (rs.next()) {
						vAloneID.add(rs.getString(1));
					}
					return vAloneID;
				}
			});
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}

		Map<String, UFBoolean> hashCommited = new HashMap<String, UFBoolean>();
		for (String strAloneID : strAloneIDs) {
			if (vFilterAloneID.contains(strAloneID))
				hashCommited.put(strAloneID, UFBoolean.TRUE);
			else
				hashCommited.put(strAloneID, UFBoolean.FALSE);
		}
		return hashCommited;
	}

	@SuppressWarnings({ "unchecked", "serial" })
	@Override
	public Map<String, UFBoolean> queryDirectParentVersionCommited(String[] strAloneIDs, KeyGroupVO keyGroup,
			String strRmsVerPK, String taskpk) throws UFOSrvException {
		String pubTable = keyGroup.getTableName();
		KeyVO[] keys = keyGroup.getKeys();
		String strSQL1 = "select t1.alone_id, t3.pk_fatherorg,t2.ver ";
		String strSQL2 = "select t5.keyword1,t5.ver ";
		for (int i = 1; i < keys.length; i++) {
			strSQL1 += ",keyword" + (i + 1);
			strSQL2 += ",keyword" + (i + 1);
		}
		// tianchuan 20141021 in语句不能超过1000的问题
		String aloneIdSqlIn = StringConnectUtil.getInSqlGroupByArr(strAloneIDs, "t1.alone_id");

		strSQL1 += " from iufo_task_commit t1," + pubTable
				+ " t2, org_reportManaStruMember_v t3 where  t1.alone_id=t2.alone_id"
				+ " and t2.keyword1=t3.pk_org and t3.pk_svid='" + strRmsVerPK + "' and " + aloneIdSqlIn;
		strSQL2 += " from iufo_task_commit t4," + pubTable + " t5 where pk_task='" + taskpk + "' and  t4.commit_state>"
				+ CommitStateEnum.STATE_BACKED.getIntValue() + " and t4.alone_id = t5.alone_id";

		String strSQL = "select tmp1.alone_id from (" + strSQL1 + ") tmp1 inner join (" + strSQL2 + ") tmp2"
				+ " on tmp1.pk_fatherorg=tmp2.keyword1 and tmp1.ver=tmp2.ver";
		for (int i = 1; i < keys.length; i++) {
			strSQL += " and tmp1.keyword" + (i + 1) + "=tmp2.keyword" + (i + 1);
		}

		BaseDAO dao = new BaseDAO();
		Set<String> vFilterAloneID = null;
		try {
			vFilterAloneID = (Set<String>) dao.executeQuery(strSQL, new BaseProcessor() {
				@Override
				public Object processResultSet(ResultSet rs) throws SQLException {
					Set<String> vAloneID = new HashSet<String>();
					while (rs.next()) {
						vAloneID.add(rs.getString(1));
					}
					return vAloneID;
				}
			});
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}

		Map<String, UFBoolean> hashCommited = new HashMap<String, UFBoolean>();
		for (String strAloneID : strAloneIDs) {
			if (vFilterAloneID.contains(strAloneID))
				hashCommited.put(strAloneID, UFBoolean.TRUE);
			else
				hashCommited.put(strAloneID, UFBoolean.FALSE);
		}
		return hashCommited;
	}

	@SuppressWarnings({ "unchecked", "serial" })
	@Override
	public Map<String, String[]> queryTaskAssignOrgPK(String strTaskPK, String pk_rms, String strOrgInnCode)
			throws UFOSrvException {

		String sql;
		if (strOrgInnCode.contains("(")) {
			sql = " select t1.pk_org,t2.pk_assignorg,t2.pk_groupcorp from  org_rmsmember t1 left outer join iufo_taskassign t2 "
					+ "on t1.pk_org=t2.pk_receiveorg and t2.pk_task='"
					+ strTaskPK
					+ "' where t1.pk_rms='"
					+ pk_rms
					+ "' and " + strOrgInnCode;
		} else {
			sql = " select t1.pk_org,t2.pk_assignorg,t2.pk_groupcorp from  org_rmsmember t1 left outer join iufo_taskassign t2 "
					+ "on t1.pk_org=t2.pk_receiveorg and t2.pk_task='"
					+ strTaskPK
					+ "' where t1.pk_rms='"
					+ pk_rms
					+ "' and t1.innercode like '" + strOrgInnCode + "%'";
		}

		BaseDAO dao = new BaseDAO();
		Map<String, String[]> hashAssignOrgPK = null;
		try {
			hashAssignOrgPK = (Map<String, String[]>) dao.executeQuery(sql, new BaseProcessor() {
				@Override
				public Object processResultSet(ResultSet rs) throws SQLException {
					Map<String, String[]> hashAssignOrgPK = new HashMap<String, String[]>();
					String[] pk;
					while (rs.next()) {
						pk = new String[2];
						pk[0] = rs.getString(2);
						pk[1] = rs.getString(3);
						hashAssignOrgPK.put(rs.getString(1), pk);
					}
					return hashAssignOrgPK;
				}
			});
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}
		return hashAssignOrgPK;
	}

	@SuppressWarnings({ "unchecked", "serial" })
	@Override
	public Map<String, String[]> queryTaskAssignVersionOrgPK(String strTaskPK, String strRmsVerPK, String strOrgInnCode)
			throws UFOSrvException {

		String sql;
		if (strOrgInnCode.contains("(")) {
			sql = " select t1.pk_org,t2.pk_assignorg,t2.pk_groupcorp from  org_reportmanaStruMember_v t1 left outer join iufo_taskassign t2 "
					+ "on t1.pk_org=t2.pk_receiveorg and t2.pk_task='"
					+ strTaskPK
					+ "' where t1.pk_svid='"
					+ strRmsVerPK + "' and " + strOrgInnCode;
		} else {
			sql = " select t1.pk_org,t2.pk_assignorg,t2.pk_groupcorp from  org_reportmanaStruMember_v t1 left outer join iufo_taskassign t2 "
					+ "on t1.pk_org=t2.pk_receiveorg and t2.pk_task='"
					+ strTaskPK
					+ "' where t1.pk_svid='"
					+ strRmsVerPK + "' and t1.innercode like '" + strOrgInnCode + "%'";
		}

		BaseDAO dao = new BaseDAO();
		Map<String, String[]> hashAssignOrgPK = null;
		try {
			hashAssignOrgPK = (Map<String, String[]>) dao.executeQuery(sql, new BaseProcessor() {
				@Override
				public Object processResultSet(ResultSet rs) throws SQLException {
					Map<String, String[]> hashAssignOrgPK = new HashMap<String, String[]>();
					String[] pk;
					while (rs.next()) {
						pk = new String[2];
						pk[0] = rs.getString(2);
						pk[1] = rs.getString(3);
						hashAssignOrgPK.put(rs.getString(1), pk);
					}
					return hashAssignOrgPK;
				}
			});
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}
		return hashAssignOrgPK;
	}

	@Override
	public List<CellsModel> getCellsModels(List<RepDataQueryResultVO> RepDataQueryResultVOs, DataSourceVO ds,
			String userId, UFDateTime ufDateTime) throws UFOSrvException {
		List<CellsModel> models = new ArrayList<CellsModel>();
		for (RepDataQueryResultVO vo : RepDataQueryResultVOs) {

			TaskVO task = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(vo.getPk_task());

			UfoContextVO context = InputUtil.getUfoContextVO(vo.getPk_report(), ds, vo.getAlone_id(),
					task.getPk_accscheme(), task.getPk_org(), userId, ufDateTime.toStdString());
			CellsModel cellmodel = CellsModelOperator.getFormatModelByPK(context, true);
			models.add(cellmodel);
		}
		return models;
	}

	/**
	 * 根据报表PK和AloneID取得Map<报表VO,PubDataVO>
	 * 
	 * @create by qugx at 2011-8-11,上午09:55:14
	 * 
	 * @param repPks
	 * @param aloneIDs
	 * @return
	 * @throws UFOSrvException
	 */
	@Override
	public Map<ReportVO, MeasurePubDataVO> getRepPubDatasByAloneIDs(String[] repPks, String[] aloneIDs)
			throws UFOSrvException {
		Map<ReportVO, MeasurePubDataVO> result = new HashMap<ReportVO, MeasurePubDataVO>();
		try {
			for (int i = 0; i < repPks.length; i++) {
				ReportVO reportVO = IUFOCacheManager.getSingleton().getReportCache().getByPK(repPks[i]);
				MeasurePubDataVO pubData = MeasurePubDataBO_Client
						.findByAloneID(reportVO.getPk_key_comb(), aloneIDs[i]);
				result.put(reportVO, pubData);
			}
		} catch (Exception e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}
		return result;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public List<RepDataQueryResultVO> loadPrintRepDataInfo(String[] aloneids, String taskpk, int inputstate,
			List<String> commitstate, String[] repPks) throws UFOSrvException {
		try {
			Map<String, RepDataQueryResultVO[]> aloneToVOsMap = new HashMap<String, RepDataQueryResultVO[]>();
			for (int i = 0; i < aloneids.length; i++) {
				aloneToVOsMap.put(aloneids[i], new RepDataQueryResultVO[repPks.length]);
			}
			Map<String, Integer> repPkToIndexMap = new HashMap<String, Integer>();
			for (int i = 0; i < repPks.length; i++) {
				repPkToIndexMap.put(repPks[i], i);
			}

			BaseDAO baseDao = new BaseDAO();
			StringBuffer aloneIdSb = new StringBuffer();
			aloneIdSb.append("(");
			for (int i = 0; i < aloneids.length; i++) {
				aloneIdSb.append("'");
				aloneIdSb.append(aloneids[i]);
				aloneIdSb.append("'");
				aloneIdSb.append(",");
			}
			aloneIdSb.deleteCharAt(aloneIdSb.length() - 1);
			aloneIdSb.append(")");

			StringBuffer repPkSb = new StringBuffer();
			repPkSb.append("(");
			for (int i = 0; i < repPks.length; i++) {
				repPkSb.append("'");
				repPkSb.append(repPks[i]);
				repPkSb.append("'");
				repPkSb.append(",");
			}
			repPkSb.deleteCharAt(repPkSb.length() - 1);
			repPkSb.append(")");

			String sql = " select t1.pk_report,t1.pk_task,t2.alone_id,t2.commit_state as repcommitstate,t2.input_person as inputperson,"
					+ "t2.input_time as inputtime,t2.lastoperator as lastopeperson,t2.lastopertime as lastopetime"
					+ " from  iufo_taskreport t1 left outer join iufo_rep_commit t2 on t1.pk_report=t2.pk_report and t2.alone_id in "
					+ aloneIdSb.toString()
					+ " where t1.pk_task='"
					+ taskpk
					+ "' and t1.pk_report in "
					+ repPkSb.toString();

			List<RepDataQueryResultVO> vResult = (List<RepDataQueryResultVO>) baseDao.executeQuery(sql,
					new BeanListProcessor(RepDataQueryResultVO.class));
			// List<RepDataQueryResultVO> vResultnew;
			List<RepDataQueryResultVO> retList = new ArrayList<RepDataQueryResultVO>();
			if (vResult != null && vResult.size() > 0) {
				for (RepDataQueryResultVO vo : vResult) {
					if (vo.getRepcommitstate() == null) {
						vo.setRepcommitstate(CommitStateEnum.STATE_NOCOMMIT.getIntValue());
					}
					if (vo.getInputperson() != null || vo.getInputtime() != null) {
						vo.setInputstate(UFBoolean.valueOf(true));
					} else {
						vo.setInputstate(UFBoolean.valueOf(false));
					}
				}

				boolean shouldKeep = false;
				for (RepDataQueryResultVO vo : vResult) {
					shouldKeep = false;
					if (inputstate == IUfoQueryCondVO.NO_MATTER_INPUT
							&& (commitstate == null || commitstate.size() == 0)) {
						shouldKeep = true;
					} else if (inputstate == IUfoQueryCondVO.NO_MATTER_INPUT) {
						if (commitstate.contains(vo.getRepcommitstate().toString())) {
							shouldKeep = true;
						}
					} else if (commitstate == null || commitstate.size() == 0) {
						if (inputstate == IUfoQueryCondVO.NOT_INPUT) {
							if (!vo.getInputstate().booleanValue()) {
								shouldKeep = true;
							}
						} else {
							if (vo.getInputstate().booleanValue()) {
								shouldKeep = true;
							}
						}
					} else {
						if (!commitstate.contains(vo.getRepcommitstate().toString())) {
							shouldKeep = false;
						} else if (inputstate == IUfoQueryCondVO.NOT_INPUT && vo.getInputstate() != null
								&& vo.getInputstate().booleanValue()) {
							shouldKeep = false;
						} else if (inputstate == IUfoQueryCondVO.HAS_INPUT
								&& (vo.getInputstate() == null || !vo.getInputstate().booleanValue())) {
							shouldKeep = false;
						} else {
							shouldKeep = true;
						}
					}
					if (shouldKeep && vo.getAlone_id() != null) {
						aloneToVOsMap.get(vo.getAlone_id())[repPkToIndexMap.get(vo.getPk_report())] = vo;
					}
				}

				RepDataQueryResultVO tempVO = null;
				RepDataQueryResultVO[] vos = null;
				for (int i = 0; i < aloneids.length; i++) {
					vos = aloneToVOsMap.get(aloneids[i]);
					for (int j = 0; j < vos.length; j++) {
						if (inputstate == IUfoQueryCondVO.NO_MATTER_INPUT
								&& vos[j] == null
								&& (commitstate.contains(String.valueOf(CommitStateEnum.STATE_NOCOMMIT.getIntValue()))
										|| commitstate == null || commitstate.size() == 0)) {// 剩下为空的，即为库中没有的，这些一定是未上报的，所以只有包含未上报，才加入
							tempVO = new RepDataQueryResultVO();
							tempVO.setPk_report(repPks[j]);
							tempVO.setPk_task(taskpk);
							tempVO.setRepcommitstate(CommitStateEnum.STATE_NOCOMMIT.getIntValue());
							tempVO.setInputstate(UFBoolean.valueOf(false));
							tempVO.setAlone_id(aloneids[i]);
							vos[j] = tempVO;
						}
						if (vos[j] != null) {
							retList.add(vos[j]);
						}
					}
				}

				// if (inputstate == IUfoQueryCondVO.INT_EMPTY_SELECT &&
				// (commitstate == null || commitstate.size() == 0)) {
				//
				// } else if (inputstate == IUfoQueryCondVO.INT_EMPTY_SELECT) {
				// vResultnew = new ArrayList<RepDataQueryResultVO>();
				// for (RepDataQueryResultVO vo : vResult) {
				// if (commitstate.contains(vo.getRepcommitstate().toString()))
				// {
				// vResultnew.add(vo);
				// }
				// }
				// return vResultnew;
				// } else if (commitstate == null || commitstate.size() == 0){
				// vResultnew = new ArrayList<RepDataQueryResultVO>();
				// for (RepDataQueryResultVO vo : vResult) {
				// if (inputstate == 0) {
				// if (!vo.getInputstate().booleanValue()) {
				// vResultnew.add(vo);
				// }
				// } else {
				// if (vo.getInputstate().booleanValue()) {
				// vResultnew.add(vo);
				// }
				// }
				// }
				// return vResultnew;
				// } else {
				// vResultnew = new ArrayList<RepDataQueryResultVO>();
				// for (RepDataQueryResultVO vo : vResult) {
				// if (!commitstate.contains(vo.getRepcommitstate().toString()))
				// {
				// continue;
				// }
				// if (inputstate == 0 && vo.getInputstate() != null &&
				// vo.getInputstate().booleanValue()) {
				// continue;
				// }
				// if (inputstate == 1 && (vo.getInputstate() == null ||
				// !vo.getInputstate().booleanValue())) {
				// continue;
				// }
				// vResultnew.add(vo);
				// }
				// return vResultnew;
				// }
			}
			return retList;
			// return vResult;
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}

	}

	@Override
	public List<List<RepShowPrintVO>> getRepCellsModelAndShowVOs(List<RepDataQueryResultVO> repDataQueryResultVOs,
			DataSourceVO ds, String userId, UFDateTime ufDateTime, String strBalCondPK, String rmsPk)
			throws UFOSrvException {

		List<List<RepShowPrintVO>> repShowPrintVOs = new ArrayList<List<RepShowPrintVO>>();
		if (repDataQueryResultVOs == null || repDataQueryResultVOs.size() <= 0) {
			return repShowPrintVOs;
		}
		try {
			RepShowPrintVO repShowPrintVO = null;
			TaskVO task = IUFOCacheManager.getSingleton().getTaskCache()
					.getTaskVO(repDataQueryResultVOs.get(0).getPk_task());
			List<String> reppks = new ArrayList<String>();
			TaskVO taskVO = TaskSrvUtils.getTaskVOById(repDataQueryResultVOs.get(0).getPk_task());
			String keyGroupPk = taskVO.getPk_keygroup();
			MeasurePubDataVO pubData = null;
			// 上一个单位PK，用来分组
			String lastUnitPk = null;
			List<RepShowPrintVO> lastList = null;
			// 取出原始表的cellsmodel
			for (RepDataQueryResultVO vo : repDataQueryResultVOs) {
				UfoContextVO context = InputUtil.getUfoContextVO(vo.getPk_report(), ds, vo.getAlone_id(),
						task.getPk_accscheme(), task.getPk_org(), userId, ufDateTime.toStdString());
				CellsModel cellsModel = null;
				pubData = vo.getPubData();
				if (pubData == null) {
					pubData = MeasurePubDataBO_Client.findByAloneID(keyGroupPk, vo.getAlone_id());
				}
				if (strBalCondPK == null || strBalCondPK.equals(BalanceCondVO.NON_SW_DATA_COND_PK) || pubData == null) {
					context.setAttribute(IUfoContextKey.MEASURE_PUB_DATA_VO, pubData);
					ReportFormatSrv repFormatSrv = new ReportFormatSrv(context, true);
					cellsModel = repFormatSrv.getCellsModel();
					BalanceReportExportUtil.dealPrintSetForBalance(cellsModel, null);
				} else {
					BalanceCondVO balanceCond = BalanceBO_Client.loadBalanceCondByPK(strBalCondPK);
					RepDataVO repData = BalanceBO_Client.doSwBalance(pubData, balanceCond, vo.getPk_report(), rmsPk);
					cellsModel = CellsModelOperator.getFormatModelByPK(context);
					cellsModel = CellsModelOperator.doGetDataModelFromRepDataVO(cellsModel, repData, context);
					BalanceReportExportUtil.processBalanceRepCellsModel(new ReportFormatSrv(context, cellsModel),
							false, balanceCond);
					BalanceReportExportUtil.dealPrintSetForBalance(cellsModel, balanceCond);
				}
				// cellsModel = CellsModelOperator.getFormatModelByPK(context,
				// true);
				if (!reppks.contains(vo.getPk_report())) {
					reppks.add(vo.getPk_report());
				}

				repShowPrintVO = new RepShowPrintVO(vo.getPk_report(), cellsModel, null, vo.getAlone_id());
				if (pubData != null && (lastUnitPk == null || !lastUnitPk.equals(pubData.getUnitPK()))) {
					lastList = new ArrayList<RepShowPrintVO>();
					repShowPrintVOs.add(lastList);
					lastUnitPk = pubData.getUnitPK();
				}
				lastList.add(repShowPrintVO);
				// repShowPrintVOs.add(repShowPrintVO);
			}

			// 取出展示表的cellsmodel
			String pringSql = "pk_report in " + UFOString.getSqlStrByArr(reppks.toArray(new String[reppks.size()]));
			BaseDAO baseDao = new BaseDAO();
			List<ReportShowVO> reportShowVOs = (List<ReportShowVO>) baseDao.retrieveByClause(ReportShowVO.class,
					pringSql);

			if (reportShowVOs != null && reportShowVOs.size() > 0) {
				for (List<RepShowPrintVO> tempList : repShowPrintVOs) {
					for (RepShowPrintVO printVO : tempList) {
						for (ReportShowVO reportShowVO : reportShowVOs) {
							if (printVO.getReppk().equals(reportShowVO.getPk_report())) {
								printVO.setRepShowVO(reportShowVO);
								break;
							}
						}
					}
				}
			}

		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		} catch (Exception e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}

		return repShowPrintVOs;
	}

	@Override
	public List<List<RepShowPrintVO>> getRepVersionCellsModelAndShowVOs(
			List<RepDataQueryResultVO> repDataQueryResultVOs, DataSourceVO ds, String userId, UFDateTime ufDateTime,
			String strBalCondPK, String strRmsVerPk) throws UFOSrvException {

		// List<List<RepShowPrintVO>> repShowPrintVOs = new
		// ArrayList<List<RepShowPrintVO>>();
		// if(repDataQueryResultVOs==null || repDataQueryResultVOs.size()<=0){
		// return repShowPrintVOs;
		// }
		// try {
		// RepShowPrintVO repShowPrintVO=null;
		// TaskVO task =
		// IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(repDataQueryResultVOs.get(0).getPk_task());
		// List<String> reppks = new ArrayList<String>();
		// TaskVO
		// taskVO=TaskSrvUtils.getTaskVOById(repDataQueryResultVOs.get(0).getPk_task());
		// String keyGroupPk=taskVO.getPk_keygroup();
		// MeasurePubDataVO pubData=null;
		// //上一个单位PK，用来分组
		// String lastUnitPk=null;
		// List<RepShowPrintVO> lastList=null;
		// //取出原始表的cellsmodel
		// for (RepDataQueryResultVO vo : repDataQueryResultVOs) {
		// UfoContextVO context = InputUtil.getUfoContextVO(vo.getPk_report(),
		// ds, vo.getAlone_id(), task.getPk_accscheme(), task.getPk_org(),
		// userId, ufDateTime.toStdString());
		// CellsModel cellsModel = null;
		// pubData=vo.getPubData();
		// if(pubData==null){
		// pubData=MeasurePubDataBO_Client.findByAloneID(keyGroupPk,
		// vo.getAlone_id());
		// }
		// if (strBalCondPK==null ||
		// strBalCondPK.equals(BalanceCondVO.NON_SW_DATA_COND_PK) ||
		// pubData==null){
		// context.setAttribute(IUfoContextKey.MEASURE_PUB_DATA_VO, pubData);
		// ReportFormatSrv repFormatSrv=new ReportFormatSrv(context,true);
		// cellsModel=repFormatSrv.getCellsModel();
		// BalanceReportExportUtil.dealPrintSetForBalance(cellsModel,null);
		// }else{
		// BalanceCondVO
		// balanceCond=BalanceBO_Client.loadBalanceCondByPK(strBalCondPK);
		// RepDataVO repData=BalanceBO_Client.doSwBalance(pubData,
		// balanceCond,vo.getPk_report(),rmsPk);
		// cellsModel=CellsModelOperator.getFormatModelByPK(context);
		// cellsModel=CellsModelOperator.doGetDataModelFromRepDataVO(cellsModel,
		// repData, context);
		// BalanceReportExportUtil.processBalanceRepCellsModel(new
		// ReportFormatSrv(context,cellsModel),false,balanceCond);
		// BalanceReportExportUtil.dealPrintSetForBalance(cellsModel,balanceCond);
		// }
		// // cellsModel = CellsModelOperator.getFormatModelByPK(context, true);
		// if(!reppks.contains(vo.getPk_report())){
		// reppks.add(vo.getPk_report());
		// }
		//
		//
		// repShowPrintVO = new RepShowPrintVO(vo.getPk_report(), cellsModel,
		// null,vo.getAlone_id());
		// if(pubData!=null && (lastUnitPk==null ||
		// !lastUnitPk.equals(pubData.getUnitPK()))){
		// lastList=new ArrayList<RepShowPrintVO>();
		// repShowPrintVOs.add(lastList);
		// lastUnitPk=pubData.getUnitPK();
		// }
		// lastList.add(repShowPrintVO);
		// // repShowPrintVOs.add(repShowPrintVO);
		// }
		//
		// //取出展示表的cellsmodel
		// String pringSql = "pk_report in "+
		// UFOString.getSqlStrByArr(reppks.toArray(new String[reppks.size()]));
		// BaseDAO baseDao = new BaseDAO();
		// List<ReportShowVO> reportShowVOs =
		// (List<ReportShowVO>)baseDao.retrieveByClause(ReportShowVO.class,
		// pringSql);
		//
		// if (reportShowVOs != null && reportShowVOs.size() > 0) {
		// for(List<RepShowPrintVO> tempList : repShowPrintVOs){
		// for (RepShowPrintVO printVO : tempList) {
		// for (ReportShowVO reportShowVO : reportShowVOs) {
		// if (printVO.getReppk().equals(reportShowVO.getPk_report())) {
		// printVO.setRepShowVO(reportShowVO);
		// break;
		// }
		// }
		// }
		// }
		// }
		//
		// } catch (DAOException e) {
		// AppDebug.debug(e);
		// throw new UFOSrvException(e.getMessage(),e);
		// } catch (Exception e) {
		// AppDebug.debug(e);
		// throw new UFOSrvException(e.getMessage(),e);
		// }

		return null;
	}
}