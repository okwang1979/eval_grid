package nc.impl.tb.rule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
import nc.bs.mdm.dim.PKListTempTable;
import nc.bs.mdm.persistence.NtbDAO;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.mw.sqltrans.TempTable;
import nc.bs.uif2.BusinessExceptionAdapter;
import nc.impl.epmp.plan.txtdata.PlanTxtValueManagerImpl;
import nc.impl.pubapp.pattern.database.IDQueryBuilder;
import nc.itf.epmp.plan.txtdata.IPlanTxtValueManager;
import nc.itf.tb.rule.IBusiRuleQuery;
import nc.itf.tb.rule.StringArrayProcessor;
import nc.jdbc.framework.JdbcSession;
import nc.jdbc.framework.PersistenceManager;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.exception.DbException;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.ms.tb.pub.NtbSuperDMO;
import nc.ms.tb.rule.RuleManager;
import nc.ms.tb.rule.RuleServiceGetter;
import nc.ms.tb.rule.fmlset.FormulaCTL;
import nc.ms.tb.rule.ruletype.IPKRuleConst;
import nc.ms.tb.rule.ruletype.IRuleBigClassEnum;
import nc.ms.tb.rule.ruletype.IRuleClassConstEnum;
import nc.ms.tb.rule.ruletype.IRuleClassMapConst;
import nc.ms.tb.task.TbTaskCtl;
import nc.ms.tb.tracing.RuleTracingManager;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.cube.DimSectionTuple;
import nc.vo.mdm.cuberule.CubeRule;
import nc.vo.mdm.dim.DimDef;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.IDimLevelPKConst;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.mdm.pub.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.SuperVO;
import nc.vo.tb.formula.DimFormulaMVO;
import nc.vo.tb.formula.DimFormulaVO;
import nc.vo.tb.formula.UfoVersionVO;
import nc.vo.tb.obj.RelaRuleVO;
import nc.vo.tb.pubutil.IBusiTermConst;
import nc.vo.tb.rule.BusiRuleVO;
import nc.vo.tb.rule.CalculateMemberVO;
import nc.vo.tb.rule.CtrlSpecialUsage;
import nc.vo.tb.rule.DimRuleMemberParamVO;
import nc.vo.tb.rule.DimRuleMemberVO;
import nc.vo.tb.rule.EpmRuleRefVO;
import nc.vo.tb.rule.IRuleClassConst;
import nc.vo.tb.rule.IdCtrlInfoVO;
import nc.vo.tb.rule.IdFlexElementVO;
import nc.vo.tb.rule.IdFlexZoneVO;
import nc.vo.tb.rule.RuleClassVO;
import nc.vo.tb.rule.tracing.FormulaTracing;
import nc.vo.tb.rule.tracing.TracingFormulaInfo;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.util.ObjectWithByteTranslateTool;

public class BusiRuleQueryImpl implements IBusiRuleQuery {
	@Override
	public ArrayList<BusiRuleVO> queryByRuleClassAndSysAndPkorg(String bigClass, String ruleClass, String sys,String pk_busiattr)throws BusinessException{
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_bigclass = '" + bigClass + "' and pk_ruleclass = '" + ruleClass + "' and pk_busiattr = '"+pk_busiattr+"'  order by objname";
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, str);
		ArrayList<BusiRuleVO> voList = new ArrayList<BusiRuleVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	@Override
	public ArrayList<BusiRuleVO> queryByRuleClass(String bigClass, String ruleClass) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_bigclass = '" + bigClass + "' and pk_ruleclass = '" + ruleClass + "'   order by objname";
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, str);
		ArrayList<BusiRuleVO> voList = new ArrayList<BusiRuleVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}
	
	@Override
	public List<BusiRuleVO> queryByRuleClassAndSys(String bigClass, String ruleClass, String sys) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_bigclass = '" + bigClass + "' and pk_ruleclass = '" + ruleClass + "'    and syscode='"+sys+"'  order by objname";
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, str);
		ArrayList<BusiRuleVO> voList = new ArrayList<BusiRuleVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	@Override
	public ArrayList<BusiRuleVO> queryByRuleAndMdSheet(String ruleclass, String pk_mdsheet) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_mdsheet = '" + pk_mdsheet + "' and pk_ruleclass = '" + ruleclass + "'";
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, str);
		ArrayList<BusiRuleVO> voList = new ArrayList<BusiRuleVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	@Override
	public ArrayList<BusiRuleVO> queryByRuleAndMdWorkbook(String pk_workbook) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String ruleclass = "('" + IRuleClassConst.SCHEMA_SINGLE + "','" + IRuleClassConst.SCHEMA_GROUP + "','" + IRuleClassConst.SCHEMA_SPEC + "','"
				+ IRuleClassConst.SCHEMA_FLEX + "')";
		String str = " pk_mdsheet = '" + pk_workbook + "' and pk_ruleclass in " + ruleclass;
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, str);
		ArrayList<BusiRuleVO> voList = new ArrayList<BusiRuleVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	@Override
	public BusiRuleVO queryByPk(String pk_obj) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		BusiRuleVO vo = (BusiRuleVO) dmo.queryByPrimaryKey(BusiRuleVO.class, pk_obj);
		return vo;
	}

	@Override
	public BusiRuleVO[] queryBusiRuleByName(String objname) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, "objname = '" + objname + "'");

		return vos;
	}

	@Override
	public ArrayList<BusiRuleVO> queryByRuleType(String bigclass, String ruleType) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_bigclass = '" + bigclass + "' and ruletype = '" + ruleType + "'";
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, str);
		ArrayList<BusiRuleVO> voList = new ArrayList<BusiRuleVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	@Override
	public ArrayList<DimFormulaVO> queryByPKRule(String Pk_rule) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_parent = '" + Pk_rule + "' order by PRIORITY asc";
		DimFormulaVO[] vos = (DimFormulaVO[]) dmo.queryByWhereClause(DimFormulaVO.class, str);
		ArrayList<DimFormulaVO> voList = new ArrayList<DimFormulaVO>();
		voList.addAll(Arrays.asList(vos));
		addFormulaM(voList);
		return voList;
	}

	@Override
	public ArrayList<DimFormulaVO> queryAllVOByPkRule() throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " 1=1 ";
		ArrayList<DimFormulaVO> voList = new ArrayList<DimFormulaVO>();
		// DimFormulaVO[] vos = (DimFormulaVO[])dmo.queryByWhereClause(DimFormulaVO.class, str);
		// 修改查询方法按照priority字段进行排序 modify by chenleid
		try {

			DimFormulaVO[] vos = (DimFormulaVO[]) dmo.queryByClauses(DimFormulaVO.class, str, " priority "/* , null */);
			voList.addAll(Arrays.asList(vos));
		} catch (Exception ex) {
			throw new BusinessException(ex);
		}
		addFormulaM(voList);
		return voList;
	}

	@Override
	public DimFormulaVO queryDimFormulaByPK(String pk_dimformula) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		DimFormulaVO vo = (DimFormulaVO) dmo.queryByPrimaryKey(DimFormulaVO.class, pk_dimformula);
		addFormulaM(vo);
		return vo;
	}

	@Override
	public ArrayList<DimFormulaMVO> queryMVOByPkFormula(String pk_formula) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_ruleformula = '" + pk_formula + "' " + "ORDER BY VARNO DESC ";
		DimFormulaMVO[] vos = (DimFormulaMVO[]) dmo.queryByWhereClause(DimFormulaMVO.class, str);

		ArrayList<DimFormulaMVO> voList = new ArrayList<DimFormulaMVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	@Override
	public ArrayList<DimFormulaMVO> queryAllMVOByPkFormula() throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " 1=1 order by pk_ruleformula,varno asc ";
		DimFormulaMVO[] vos = (DimFormulaMVO[]) dmo.queryByWhereClause(DimFormulaMVO.class, str);
		ArrayList<DimFormulaMVO> voList = new ArrayList<DimFormulaMVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	@Override
	public String queryRuleStrByPkRule(String pk_rule) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_parent = '" + pk_rule + "' order by PRIORITY asc";
		DimFormulaVO[] vos = (DimFormulaVO[]) dmo.queryByWhereClause(DimFormulaVO.class, str);
		StringBuffer sbStr = new StringBuffer();

		for (int n = 0; n < (vos == null ? 0 : vos.length); n++) {
			DimFormulaVO vo = vos[n];
			// sbStr.append(vo.getFullcontent()).append(";");
			sbStr.append(FormulaCTL.getFullExpress(vo.getExeFullcontent(), vo.getPrimaryKey())).append(";");
		}
		return sbStr.toString();
	}

	@Override
	public ArrayList<DimRuleMemberVO> queryRuleMemberByPkDimDef(String pk_dimdef) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_dimdef = '" + pk_dimdef + "' ";
		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, str);
		ArrayList<DimRuleMemberVO> voList = new ArrayList<DimRuleMemberVO>();
		voList.addAll(Arrays.asList(vos));
		fullRuleMemberParam(voList);
		return voList;
	}

	@Override
	public ArrayList<DimRuleMemberVO> queryRuleMemberByPkDimLevel(String pk_dimlevel) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_dimlevel = '" + pk_dimlevel + "'";
		//ncm_tbb_begin_liuyqt业务规则参照不到规则成员
//		str = str + " and (membertype='RULEMEMBER' or membertype ='~') and sysCode = 'BCS'";
		str = str + " and (membertype='RULEMEMBER' or membertype ='~')";
		//ncm_tbb_end
		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, str);

		ArrayList<DimRuleMemberVO> voList = new ArrayList<DimRuleMemberVO>();
		voList.addAll(Arrays.asList(vos));

		if (IDimLevelPKConst.CURR.equals(pk_dimlevel)) {
			List<DimRuleMemberVO> removeList = new ArrayList<DimRuleMemberVO>();
			for (DimRuleMemberVO vo : voList) {
				if (vo.getPk_obj().equals("1001ZZ100000000049JU") || vo.getPk_obj().equals("1001Z810000000004TM1")
						|| vo.getPk_obj().equals("1001ZZ10000000004NFU")) {
					removeList.add(vo);
				}

			}
			voList.removeAll(removeList);

		}
		fullRuleMemberParam(voList);
		return voList;
	}

	@Override
	public ArrayList<DimRuleMemberVO> queryRuleMemberByPkDimLevel(String pk_dimlevel,String sysCode) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_dimlevel = '" + pk_dimlevel + "'";
		str = str + " and (membertype='RULEMEMBER' or membertype ='~')";
		//去掉业务系统过滤 1903去掉了
		//因为合并 和 报表的规则成员需要全部同时使用所以要同时查询 nigy
//		if(sysCode.equals(IBusiTermConst.SYS_BGR) || sysCode.equals(IBusiTermConst.SYS_BM)){
//			str = str + " and sysCode in ('" +IBusiTermConst.SYS_BM+ "','"+IBusiTermConst.SYS_BGR+"')";
//		}
//		str = str + " and (membertype='RULEMEMBER' or membertype ='~') and sysCode = '" +sysCode+ "'";
		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, str);

		ArrayList<DimRuleMemberVO> voList = new ArrayList<DimRuleMemberVO>();
		voList.addAll(Arrays.asList(vos));

		if (IDimLevelPKConst.CURR.equals(pk_dimlevel)) {
			List<DimRuleMemberVO> removeList = new ArrayList<DimRuleMemberVO>();
			for (DimRuleMemberVO vo : voList) {
				if (vo.getPk_obj().equals("1001ZZ100000000049JU") || vo.getPk_obj().equals("1001Z810000000004TM1")
						|| vo.getPk_obj().equals("1001ZZ10000000004NFU")) {
					removeList.add(vo);
				}

			}
			voList.removeAll(removeList);

		}
		fullRuleMemberParam(voList);
		return voList;
	}
	@Override
	public DimRuleMemberVO queryRuleMemberByPk(String pk_rulemember) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		DimRuleMemberVO rtn = (DimRuleMemberVO) dmo.queryByPrimaryKey(DimRuleMemberVO.class, pk_rulemember);
		fullRuleMemberParam(rtn);
		return rtn;
	}

	@Override
	public IdCtrlInfoVO queryCtrlInfoVOByPk(String pk_formula) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		IdCtrlInfoVO[] vos = (IdCtrlInfoVO[]) dmo.queryByWhereClause(IdCtrlInfoVO.class, "pk_ctrlrule='" + pk_formula + "'");
		if (vos == null || vos.length == 0) {
			return null;
		} else {
			return vos[0];
		}
	}

	public List<IdCtrlInfoVO> queryCtrlInfoVOByPks(List<String> pk_formulas) throws BusinessException {

		NtbSuperDMO dmo = new NtbSuperDMO();
		String querySql = null;
		String mergeSql = mergeSqlStr(pk_formulas);
		if (mergeSql == null)
			querySql = "";
		else
			querySql = " pk_ctrlrule in " + mergeSql;
		IdCtrlInfoVO[] vos = (IdCtrlInfoVO[]) dmo.queryByWhereClause(IdCtrlInfoVO.class, querySql);
		ArrayList<IdCtrlInfoVO> voList = new ArrayList<IdCtrlInfoVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;

	}

	@Override
	public IdCtrlInfoVO queryCtrlInfoVOByPrimkey(String pk_ctrlinfo) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		IdCtrlInfoVO[] vos = (IdCtrlInfoVO[]) dmo.queryByWhereClause(IdCtrlInfoVO.class, "pk_obj='" + pk_ctrlinfo + "'");
		if (vos == null || vos.length == 0) {
			return null;
		} else {
			return vos[0];
		}
	}

	@Override
	public IdCtrlInfoVO[] queryDefaultCtrlInfoVOs() throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		IdCtrlInfoVO[] vos = (IdCtrlInfoVO[]) dmo.queryByWhereClause(IdCtrlInfoVO.class, "pk_obj like '%@@@@%'");
		return vos;
	}

	public ArrayList<IdFlexElementVO> queryFlexElementByPk(String pk_dimformula) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		ArrayList<IdFlexElementVO> list = new ArrayList<IdFlexElementVO>();
		String strWhere = " PK_FORMULA ='" + pk_dimformula + "'";
		IdFlexElementVO[] vos = (IdFlexElementVO[]) dmo.queryByWhereClause(IdFlexElementVO.class, strWhere);
		list.addAll(Arrays.asList(vos));
		return list;
	}

	public ArrayList<IdFlexZoneVO> queryFlexZoneByPk(String pk_dimformula) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		ArrayList<IdFlexZoneVO> list = new ArrayList<IdFlexZoneVO>();
		String strWhere = " PK_FORMULA ='" + pk_dimformula + "'";
		IdFlexZoneVO[] vos = (IdFlexZoneVO[]) dmo.queryByWhereClause(IdFlexZoneVO.class, strWhere);
		list.addAll(Arrays.asList(vos));
		return list;
	}

	public ArrayList<DimFormulaVO> queryAllFormulasByRuleClassByPriorty(String pkRuleClass, String pk_cube, String avaentities)
			throws BusinessException {
		return null;
	}

	@Override
	public ArrayList<RuleClassVO> queryAllByBigClass(String bigClass) throws BusinessException {
		// TODO Auto-generated method stub
		ArrayList<RuleClassVO> voList = new ArrayList<RuleClassVO>();
		NtbSuperDMO dmo = new NtbSuperDMO();
		// String strWhere = " BIGCLASS = "+bigClass;
		StringBuffer sbStr = new StringBuffer();
		sbStr.append(" BIGCLASS = '" + bigClass + "'");
		sbStr.append(" ORDER BY RULETYPE ASC");
		sbStr.append(" ,OBJNAME DESC ");
		RuleClassVO[] vos = (RuleClassVO[]) dmo.queryByWhereClause(RuleClassVO.class, sbStr.toString());
		for (int n = 0; n < (vos == null ? 0 : vos.length); n++) {
			voList.add(vos[n]);
		}
		return voList;
	}

	@Override
	public List<RuleClassVO> queryBizRuleClassByCtrlRuleType(String bigClass) throws BusinessException {

		NtbSuperDMO dmo = new NtbSuperDMO();
		DimFormulaVO vo = new DimFormulaVO();

		StringBuffer conditionBuffer = new StringBuffer();
		conditionBuffer.append("pk_obj in (select distinct pk_ruleclass from ");
		conditionBuffer.append(vo.getTableName());
		conditionBuffer.append(" where bigclass = '");
		conditionBuffer.append(bigClass);
		conditionBuffer.append("')");
		if (IPKRuleConst.CTRL_RULE.equals(bigClass))
			conditionBuffer.append(" or pk_obj in ('TBRULE000SCHEMA_SPEC' ,'TBRULE000SCHEMA_FLEX')");
		conditionBuffer.append(" order by ruletype asc");

		RuleClassVO[] vos = (RuleClassVO[]) dmo.queryByWhereClause(RuleClassVO.class, conditionBuffer.toString());
		return Arrays.asList(vos);
	}

	@Override
	public ArrayList<RuleClassVO> queryAllByBigClassAndPkCube(int bigClass, String pkCube) throws BusinessException {
		// TODO Auto-generated method stub
		ArrayList<RuleClassVO> voList = new ArrayList<RuleClassVO>();
		NtbSuperDMO dmo = new NtbSuperDMO();
		StringBuffer strWhere = new StringBuffer();
		strWhere.append(" BIGCLASS = " + bigClass);
		strWhere.append(" and ");
		strWhere.append(" PK_CUBE = '").append(pkCube).append("'");
		RuleClassVO[] vos = (RuleClassVO[]) dmo.queryByWhereClause(RuleClassVO.class, strWhere.toString());
		for (int n = 0; n < (vos == null ? 0 : vos.length); n++) {
			voList.add(vos[n]);
		}
		return voList;
	}

	@Override
	public RuleClassVO queryBizRuleClassByCodeAndClassTypeAndBigType(String code, String classType, String bigType) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		StringBuffer strWhere = new StringBuffer();
		strWhere.append(" BIGCLASS = " + bigType);
		strWhere.append(" and ");
		strWhere.append(" objcode = '").append(code).append("'");
		strWhere.append(" and ");
		strWhere.append(" ruletype = '").append(classType).append("'");
		RuleClassVO[] vos = (RuleClassVO[]) dmo.queryByWhereClause(RuleClassVO.class, strWhere.toString());

		return null;
	}

	/**
	 * 通过pk_obj得到ruleClass 查询表 ntb_cd_ruleclass
	 *
	 * @return RuleClass
	 * @author dengyh
	 */
	public RuleClassVO queryBizRuleClassByPkObj(String pk_obj) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		RuleClassVO ruleClass = new RuleClassVO();
		StringBuffer strWhere = new StringBuffer();
		strWhere.append(" PK_OBJ = " + pk_obj);
		ruleClass = (RuleClassVO) dmo.queryByPrimaryKey(RuleClassVO.class, strWhere.toString());
		return ruleClass;
	}

	@Override
	public ArrayList<RuleClassVO> queryAll() throws BusinessException {
		// TODO Auto-generated method stub
		ArrayList<RuleClassVO> voList = new ArrayList<RuleClassVO>();
		NtbSuperDMO dmo = new NtbSuperDMO();
		RuleClassVO[] vos = (RuleClassVO[]) dmo.queryAll(RuleClassVO.class);
		for (int n = 0; n < (vos == null ? 0 : vos.length); n++) {
			voList.add(vos[n]);
		}
		return voList;
	}

	@Override
	public ArrayList<DimRuleMemberVO> queryRuleMemberByDimLevels(List<String> levelPKs) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String querySql = null;
		String mergeSql = mergeSqlStr(levelPKs);
		if (mergeSql == null)
			querySql = "";
		else
			querySql = " pk_dimlevel in " + mergeSql;
		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, querySql);
		ArrayList<DimRuleMemberVO> voList = new ArrayList<DimRuleMemberVO>();
		voList.addAll(Arrays.asList(vos));
		fullRuleMemberParam(voList);
		return voList;
	}

	private String mergeSqlStr(List<String> values) {
		if (values == null || values.size() == 0)
			return null;
		StringBuffer buff = new StringBuffer();
		buff.append("(");
		int i = 0;
		for (String value : values) {
			buff.append("'").append(value).append("'");
			if (i != values.size() - 1)
				buff.append(",");
			i++;
		}
		buff.append(")");
		return buff.toString();
	}

	@Override
	public ArrayList<DimRuleMemberVO> queryRuleMemberByPks(String[] pks) throws BusinessException {
		if (pks == null || pks.length == 0) {
			return new ArrayList<DimRuleMemberVO>();
		}
		NtbSuperDMO dmo = new NtbSuperDMO();
		String querySql = null;
		String mergeSql = mergeSqlStr(new ArrayList<String>(Arrays.asList(pks)));
		querySql = " pk_obj in " + mergeSql;
		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, querySql);
		ArrayList<DimRuleMemberVO> voList = new ArrayList<DimRuleMemberVO>();
		voList.addAll(Arrays.asList(vos));

		fullRuleMemberParam(voList);
		return voList;
	}

	@Override
	public boolean CheckRuleByObjName(String objName, String pk_bigClass) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String querySql = null;
		querySql = " objname ='" + objName + "' and  pk_bigClass = '" + pk_bigClass + "' ";
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, querySql);
		if (vos != null && vos.length != 0)
			return true;

		return false;
	}

	@Override
	public ArrayList<DimFormulaMVO> queryMVOByPkBusiRule(String pk_rule) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_ruleformula in( select pk_obj from tb_ruleformula where pk_parent='" + pk_rule + "') " + "ORDER BY VARNO DESC ";
		DimFormulaMVO[] vos = (DimFormulaMVO[]) dmo.queryByWhereClause(DimFormulaMVO.class, str);

		ArrayList<DimFormulaMVO> voList = new ArrayList<DimFormulaMVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	@Override
	public boolean CheckRuleMember(String pk_dimlevel, String objCode, boolean checkCode, boolean checkName) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		if (!checkCode && !checkName)
			return false;
		StringBuffer querySql = new StringBuffer();
		;
		querySql.append(" pk_dimlevel='").append(pk_dimlevel).append("' ");
		boolean hasContent = false;
		if (checkCode) {
			querySql.append(" and objcode='").append(objCode).append("' ");
			hasContent = true;
		}
		// if(checkName){
		// if(hasContent) querySql.append(" or ");
		// else querySql.append(" and ");
		// querySql.append(" objname='").append(objName).append("' ");
		// }
		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, querySql.toString());
		if (vos != null && vos.length != 0)
			return true;

		return false;
	}
	
	@Override
	public DimRuleMemberVO[] checkRuleMember(String pk_dimlevel, String objCode, boolean checkCode, boolean checkName) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		if (!checkCode && !checkName)
			return null;
		StringBuffer querySql = new StringBuffer();
		;
		querySql.append(" pk_dimlevel='").append(pk_dimlevel).append("' ");
		boolean hasContent = false;
		if (checkCode) {
			querySql.append(" and objcode='").append(objCode).append("' ");
			hasContent = true;
		}

		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, querySql.toString());
		if (vos != null && vos.length != 0)
			return vos;
		return null;
	}

	public ArrayList<BusiRuleVO> queryAllBusiRule(String bigclass) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_bigclass = '" + bigclass + "'";
		// 加排序
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, str, " objname ");
		// BusiRuleVO[] vos = (BusiRuleVO[])dmo.queryByWhereClause(BusiRuleVO.class, str);
		ArrayList<BusiRuleVO> voList = new ArrayList<BusiRuleVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	@Override
	public ArrayList<BusiRuleVO> queryAllBusiRuleOfMC(String bigclass, String queryStr) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_bigclass = '" + bigclass + "'" + " and " + queryStr;
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, str, " objname ");
		ArrayList<BusiRuleVO> voList = new ArrayList<BusiRuleVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	public List queryRuleInfoOfMC(String bigclass, String queryStr) throws BusinessException {
		ArrayList<BusiRuleVO> vos = queryAllBusiRuleOfMC(bigclass, queryStr);
		ArrayList<Object> objList = new ArrayList<Object>();
		ArrayList<IRuleClassConstEnum> enumList = IRuleClassMapConst.getRuleClassByBigClass(IRuleBigClassEnum.BUSI_RULE);
		for (IRuleClassConstEnum busiRule : enumList) {
			String ruleclass = busiRule.toCodeString();
			ArrayList<BusiRuleVO> vosList = new ArrayList<BusiRuleVO>();// busiRuleQuery.queryByRuleClass(bigclass,ruleclass);
			for (int n = 0; n < vos.size(); n++) {
				if (vos.get(n).getPk_ruleclass().equals(ruleclass)) {
					// 校验管控设置

					vosList.add(vos.get(n));
				}
			}
			// List<BusiRuleVO> mgList = managerControlCheck(vosList);
			if (vosList != null && vosList.size() != 0) {
				List<String> pks = new ArrayList<String>();
				Map<String, BusiRuleVO> dfMap = new HashMap<String, BusiRuleVO>();
				for (BusiRuleVO brvo : vosList) {
					pks.add(brvo.getPrimaryKey());
					dfMap.put(brvo.getPrimaryKey(), brvo);
				}
				List<DimFormulaVO> dfvos = queryRuleFormulaByBusiRules(pks);
				// 按照名称排序 by:wangzhqa at 2013-5-23
				DimFormulaVO[] arrayVos = dfvos.toArray(new DimFormulaVO[0]);

				Arrays.sort(arrayVos);
				dfvos = Arrays.asList(arrayVos);
				for (DimFormulaVO vo : dfvos) {
					BusiRuleVO tempbr = dfMap.get(vo.getPk_parent());
					tempbr.getDimFormulas().add(vo);
					if (vo.getObjname() == null)
						vo.setObjname(NCLangResOnserver.getInstance().getStrByID("tbb_rule", "01420rul_000129", null,
								new String[] { String.valueOf(tempbr.getDimFormulas().size()) })/* 公式{0} */);
				}
				objList.addAll(dfvos);
			}
			objList.add(busiRule);
			objList.addAll(vosList);
			// objList.add(formulas);
		}
		return objList;
	}

	// private ArrayList<DimFormulaVO>

	@Override
	public ArrayList<DimRuleMemberVO> queryAllRuleMembers() throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " 1=1 ";
		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, str);
		ArrayList<DimRuleMemberVO> voList = new ArrayList<DimRuleMemberVO>();
		voList.addAll(Arrays.asList(vos));
		fullRuleMemberParam(vos);
		return voList;
	}

	private void fullRuleMemberParam(DimRuleMemberVO... dimRuleMemberVOs) throws BusinessException {

		BaseDAO basDAO = new BaseDAO();

		for (DimRuleMemberVO vo : dimRuleMemberVOs) {
			List<DimRuleMemberParamVO> params = queryDimRuleMemberParamsByMemberPk(vo.getPk_obj());
			vo.setParams(params);
		}

	}

	private void fullRuleMemberParam(Collection<DimRuleMemberVO> dimRuleMemberVOs) throws BusinessException {

		BaseDAO basDAO = new BaseDAO();

		for (DimRuleMemberVO vo : dimRuleMemberVOs) {
			List<DimRuleMemberParamVO> params = queryDimRuleMemberParamsByMemberPk(vo.getPk_obj());
			if (params != null && params.size() > 0) {
				vo.setParams(params);
			}

		}

	}
	
	/**
	 * 批量填充规则成员参数
	 * @author sunzeg
	 * @param dimRuleMemberVOs
	 * @throws BusinessException
	 */
	private void fullRuleMemberParamBatch(List<DimRuleMemberVO> dimRuleMemberVOs) throws BusinessException {

		BaseDAO basDAO = new BaseDAO();
		
		List<String> ruleMemberPks = new ArrayList<String>();
		Map<String, DimRuleMemberVO> ruleMemberMap = new HashMap<String, DimRuleMemberVO>();
		for (DimRuleMemberVO vo : dimRuleMemberVOs) {
			ruleMemberPks.add(vo.getPk_obj());
			ruleMemberMap.put(vo.getPk_obj(), vo);
		}
		//构建where条件，pk超过100构建临时表
		IDQueryBuilder builder = new IDQueryBuilder();
		String inClause = builder.buildSQL("pk_rulemember", ruleMemberPks.toArray(new String[0]));
		@SuppressWarnings("unchecked")
		List<DimRuleMemberParamVO> DimRuleMemberParamVOs = (List<DimRuleMemberParamVO>) basDAO.retrieveByClause(DimRuleMemberParamVO.class, inClause);
		for(DimRuleMemberParamVO paramVO : DimRuleMemberParamVOs){
			DimRuleMemberVO ruleMember = ruleMemberMap.get(paramVO.getPk_rulemember());
			//规则成员不包含参数时直接跳过
			if(ruleMember == null)
				continue;
			List<DimRuleMemberParamVO> params = ruleMember.getParams();
			if(params == null){
				params = new ArrayList<DimRuleMemberParamVO>();
			}
			params.add(paramVO);
		}
	}

	@Override
	public ArrayList<DimRuleMemberVO> queryAllRuleMembersOfMC(String queryStr) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " (membertype='RULEMEMBER' or membertype ='~') " + " and " + queryStr;

		str = str + "  order by objCode ";
		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, str);
		ArrayList<DimRuleMemberVO> voList = new ArrayList<DimRuleMemberVO>();
		voList.addAll(Arrays.asList(vos));
		fullRuleMemberParam(voList);
		return voList;
	}
	
	@Override
	public List<DimRuleMemberVO> queryAllRuleMembersOfMCBatch(String queryStr) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str =  " (membertype='RULEMEMBER' or membertype ='~') "  + " and " + queryStr;

		str = str + "  order by objCode ";
		@SuppressWarnings("deprecation")
		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, str);
		List<DimRuleMemberVO> voList = Arrays.asList(vos);
		fullRuleMemberParamBatch(voList);
		return voList;
	}

	@Override
	public boolean CheckRuleIsUsed(String pk_busiRule) throws BusinessException {
		IPlanTxtValueManager service = new PlanTxtValueManagerImpl();
		service.getDataFromTask(pk_busiRule);;
		return false;
		
//		try {
//			NtbSuperDMO dmo = new NtbSuperDMO();
//			SuperVO[] vos = dmo.queryByWhereClause(RelaRuleVO.class, " pk_busirule = '" + pk_busiRule + "'");
//			if (vos != null && vos.length != 0)
//				throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("tbb_rule", "01801rul_000476")/* 该规则已经被任务引用,不能删除 */);
//			CubeRule[] crs = (CubeRule[]) dmo.queryByWhereClause(CubeRule.class, "pk_busirule='" + pk_busiRule + "'");
//			if (crs != null && crs.length != 0)
//				throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("tbb_rule", "01801rul_000477")/* 该规则已经被模型引用,不能删除 */);
//			
//			
//			EpmRuleRefVO[] rules = (EpmRuleRefVO[]) dmo.queryByWhereClause(EpmRuleRefVO.class, "pk_ref_rule='" + pk_busiRule + "'");
//			if (rules != null && rules.length != 0)
//				throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("tbb_rule", "01801rul_000477")/* 该规则已经被模型引用,不能删除 */);
//			
//			
//			
//			
//			return false;
//		} catch (DAOException e1) {
//			throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("tbb_rule", "01801rul_000478")/* 数据库连接异常，请联系管理员 */);
//		} catch (BusinessException be) {
//			throw be;
//		}
	}

	@Override
	public List<BusiRuleVO> qyerBusiRuleyByPks(String[] pks) {
		if (pks == null || pks.length == 0) {
			return new ArrayList<BusiRuleVO>();
		}
		NtbSuperDMO dmo = new NtbSuperDMO();
		String querySql = null;
		String mergeSql = mergeSqlStr(new ArrayList<String>(Arrays.asList(pks)));
		querySql = " pk_obj in " + mergeSql;
		BusiRuleVO[] vos = null;
		try {
			vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, querySql);
		} catch (DAOException e) {
			NtbLogger.error(e);
			return new ArrayList<BusiRuleVO>();
		}
		if (vos != null && vos.length > 0) {
			return new ArrayList<BusiRuleVO>(Arrays.asList(vos));
		}

		return new ArrayList<BusiRuleVO>();
	}

	@Override
	public List<UfoVersionVO> queryUFOVersionInfoByReportCode(String reportCode) throws BusinessException {
		// TODO Auto-generated method stub
		BaseDAO basDAO = new BaseDAO();
		// String querySql = "select pk_versionno,ver_content ,o.code " +
		// "from IUFO_DATAVERSION  n,iufo_report t ,iufo_hbscheme o" +
		// " where n.pk_group=t.pk_group and n.pk_versionno = o.version and n. ver_type = '5'   and t.code= '"+reportCode+"' ";

		String querySql = "select pk_versionno,ver_content  " + "from IUFO_DATAVERSION  n,iufo_report t  "
				+ " where n.pk_group=t.pk_group  and n.ver_type = '5'   and t.code= '" + reportCode + "' ";

		List<String[]> values = (List<String[]>) basDAO.executeQuery(querySql, new StringArrayProcessor());
		if (values == null || values.size() == 0)
			return null;
		List<UfoVersionVO> voList = new ArrayList<UfoVersionVO>();
		for (String[] value : values) {
			UfoVersionVO vo = new UfoVersionVO();
			vo.setName(value[1]);
			vo.setValue(value[0]);
			voList.add(vo);
		}
		return voList;
	}

	@Override
	public boolean isRuleMemberUsed(String ruleMemberPK) throws BusinessException {
		// TODO Auto-generated method stub
		String queryBuffer = " select top 1 1 from tb_ruleformula_m where content like '%@" + ruleMemberPK + "%'";
		Object result;
		try {
			result = new BaseDAO().executeQuery(queryBuffer.toString(), new ColumnProcessor());
		} catch (DAOException e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessExceptionAdapter(e);
		}
		return result != null;
	}

	@Override
	public List<CalculateMemberVO> queryAllRuleMemberOfCalMember() throws BusinessException {
		// 查询出规则成员及其对应的所在的维度和级别，统一查询避免多次查询
		NtbDAO dmo = new NtbDAO();
		List<DimRuleMemberVO> vos = dmo.queryByClause(DimRuleMemberVO.class, " membertype= '" + DimRuleMemberVO.RULEMEMBER + "'");
		ArrayList<CalculateMemberVO> voList = new ArrayList<CalculateMemberVO>();
		List<String> ddpks = new ArrayList<String>();
		List<String> lvpks = new ArrayList<String>();
		for (DimRuleMemberVO vo : vos) {
			CalculateMemberVO memberVO = new CalculateMemberVO(vo);
			voList.add(memberVO);
			ddpks.add(vo.getPK_Dimdef());
			lvpks.add(vo.getPk_dimlevel());
		}
		// 集中查询维度
		String mergeDdSql = mergeSqlStr(ddpks);
		List<DimDef> dds = dmo.queryByClause(DimDef.class, " pk_obj in" + mergeDdSql);
		for (DimDef dd : dds) {
			CalculateMemberVO memberVO = new CalculateMemberVO(dd);
			voList.add(memberVO);
		}
		// 集中查询层
		String mergelvSql = mergeSqlStr(lvpks);
		List<DimLevel> lvs = dmo.queryByClause(DimLevel.class, " pk_obj in" + mergelvSql);
		for (DimLevel lv : lvs) {
			CalculateMemberVO memberVO = new CalculateMemberVO(lv);
			voList.add(memberVO);
		}
		return voList;
	}

	@Override
	public List<DimRuleMemberVO> queryRuleMemberByPkDimLevelAndType(String pkLevel, String type) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_dimlevel = '" + pkLevel + "' ";
		if (type.equals(DimRuleMemberVO.RULEMEMBER)) {
			str += " and (membertype is null or membertype='" + type + "' or membertype ='~' )";
		} else {
			str += "' and membertype='" + type + "'";
		}
		str = str + " order by objCode ";
		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, str);
		ArrayList<DimRuleMemberVO> voList = new ArrayList<DimRuleMemberVO>();
		voList.addAll(Arrays.asList(vos));
		fullRuleMemberParam(voList);
		return voList;
	}

	@Override
	public ArrayList<DimRuleMemberVO> queryRuleMemberByPkDimDefAndType(String pk_dimdef, String type) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_dimdef = '" + pk_dimdef + "'";
		if (type.equals(DimRuleMemberVO.RULEMEMBER)) {
			str += " and (membertype is null or membertype='" + type + "')";
		} else {
			str += "' and membertype='" + type + "'";
		}
		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, str);
		ArrayList<DimRuleMemberVO> voList = new ArrayList<DimRuleMemberVO>();
		voList.addAll(Arrays.asList(vos));
		fullRuleMemberParam(voList);
		return voList;
	}

	@Override
	public CtrlSpecialUsage[] queryCtrlSpecialUsageByRuleFormula(String[] pksRuleFml, boolean isInCtrl) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		String condition = "pkruleformula in (";
		StringBuffer buffer = new StringBuffer(condition);
		for (String pk : pksRuleFml)
			buffer.append("'").append(pk).append("',");
		buffer.replace(buffer.length() - 1, buffer.length(), "");
		buffer.append(")");
		if (!isInCtrl)
			buffer.append(" and pkctrlformula is null");
		Collection result = null;
		try {
			result = dao.retrieveByClause(CtrlSpecialUsage.class, buffer.toString());
		} catch (DAOException e) {
			NtbLogger.error(e.getMessage());
		}
		if (result != null)
			return (CtrlSpecialUsage[]) result.toArray(new CtrlSpecialUsage[0]);
		return null;
	}

	@Override
	public CtrlSpecialUsage[] queryCtrlSpecialUsageByCtrlFormula(String[] pksCtrlFml) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		String condition = "pkctrlformula in (";
		StringBuffer buffer = new StringBuffer(condition);
		for (String pk : pksCtrlFml)
			buffer.append("'").append(pk).append("',");
		buffer.replace(buffer.length() - 1, buffer.length(), "");
		buffer.append(")");
		Collection result = null;
		try {
			result = dao.retrieveByClause(CtrlSpecialUsage.class, buffer.toString());
		} catch (DAOException e) {
			NtbLogger.error(e.getMessage());
		}
		if (result != null)
			return (CtrlSpecialUsage[]) result.toArray(new CtrlSpecialUsage[0]);
		return null;
	}

	@Override
	public ArrayList<DimFormulaVO> queryRuleFormulaByBusiRules(List<String> busiRulePks) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String querySql = null;
		String mergeSql = mergeSqlStr(busiRulePks);
		if (mergeSql == null)
			querySql = "";
		else
			querySql = " pk_parent in " + mergeSql;
		DimFormulaVO[] vos = (DimFormulaVO[]) dmo.queryByWhereClause(DimFormulaVO.class, querySql);
//		Map<String,DimFormulaVO> dimFormulamap=new HashMap<String, DimFormulaVO>();
//		for(DimFormulaVO vo:vos){
//			dimFormulamap.put(vo.getPk_parent(), vo);
//		}
		ArrayList<DimFormulaVO> voList = new ArrayList<DimFormulaVO>();
//		for(String dd:busiRulePks ){
//			voList.add(dimFormulamap.get(dd));
//		}
		voList.addAll(Arrays.asList(vos));
		addFormulaM(voList);
		return voList;
	}

	@Override
	public DimFormulaVO[] queryDimFormulasByPks(String[] pk_dimformulas) throws BusinessException {

		BaseDAO dao = new BaseDAO();
		String condSql = null;
		if (pk_dimformulas == null || pk_dimformulas.length == 0)
			return null;
		if (pk_dimformulas.length > 800) {
			PKListTempTable tmp = new PKListTempTable("tb_ti_dimformula");
			tmp.insertTmp(Arrays.asList(pk_dimformulas));

			condSql = "pk_obj in (select pkdoc from " + tmp.getTableName() + ")";

		} else {
			String condition = "pk_obj in (";
			StringBuffer condBuffer = new StringBuffer(condition);
			for (String pk : pk_dimformulas) {
				condBuffer.append("'").append(pk).append("',");
			}
			condBuffer.replace(condBuffer.length() - 1, condBuffer.length(), "");
			condBuffer.append(")");
			condSql = condBuffer.toString();
		}
		Collection result = null;
		// try {
		result = dao.retrieveByClause(DimFormulaVO.class, condSql);
		addFormulaM(result);
		// } c
		if (result != null) {
			return (DimFormulaVO[]) result.toArray(new DimFormulaVO[0]);
		}
		return null;
	}

	@Override
	public List<DimRuleMemberVO> queryRuleMemberByCodes(String dimLevel_pk, String[] ruleMember_codes) throws BusinessException {

		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_dimlevel = '" + dimLevel_pk + "' ";

		String mergeSql = mergeSqlStr(new ArrayList<String>(Arrays.asList(ruleMember_codes)));
		str = str + " and objcode in " + mergeSql;

		DimRuleMemberVO[] vos = (DimRuleMemberVO[]) dmo.queryByWhereClause(DimRuleMemberVO.class, str);
		ArrayList<DimRuleMemberVO> voList = new ArrayList<DimRuleMemberVO>();
		voList.addAll(Arrays.asList(vos));
		fullRuleMemberParam(voList);
		return voList;

	}

	@Override
	public List<String> getSuperVoTitle(Class<SuperVO> voClass) {

		// PersistenceManager manager = null;
		// try {
		// manager = PersistenceManager.getInstance();
		// JdbcSession session = manager.getJdbcSession();
		// BaseDAO dao = new BaseDAO();
		// dao.updateObject(vo, meta)
		return null;
	}

	@Override
	public List<DimFormulaMVO> queryMVOByPkFormulas(List<String> pks) throws BusinessException {
		// TODO Auto-generated method stub

		List<DimFormulaMVO> rtnList = queryFormulaM(pks);

		if (rtnList.size() > 99000) {
			if (pks.size() < 2) {
				throw new BusinessRuntimeException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0","01050rule004-0049")/*@res "规则错误。"*/);
			}
			List<String> pka = new ArrayList<String>();
			List<String> pkb = new ArrayList<String>();
			for (int i = 0; i < pks.size(); i++) {
				if (i > pks.size() / 2) {
					pka.add(pks.get(i));
				} else {
					pkb.add(pks.get(i));
				}
			}
			rtnList = new ArrayList<DimFormulaMVO>();
			rtnList.addAll(queryMVOByPkFormulas(pka));
			rtnList.addAll(queryMVOByPkFormulas(pkb));
		}
		return rtnList;

	}

	private List<DimFormulaMVO> queryFormulaM(List<String> pks) throws BusinessException {
		// TODO Auto-generated method stub
		NtbSuperDMO dmo = new NtbSuperDMO();
		String querySql = "";
		if (pks.size() > 800) {
			String tmp = createCtrlformulaTmpTable("tb_rule_formula_temp", pks.toArray(new String[0]));
			if (!StringUtil.isEmpty(tmp)) {
				querySql += " pk_ruleformula in  (select pk from " + tmp + ")";

			} else {
				querySql = "";
			}
		} else {
			String mergeSql = mergeSqlStr(pks);
			if (mergeSql == null)
				querySql = "";
			else
				querySql = " pk_ruleformula in " + mergeSql;

		}

		DimFormulaMVO[] vos = (DimFormulaMVO[]) dmo.queryByWhereClause(DimFormulaMVO.class, querySql);
		ArrayList<DimFormulaMVO> voList = new ArrayList<DimFormulaMVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;

	}

	private String createCtrlformulaTmpTable(String name, String[] pksCtrlFormula) throws DAOException {
		String vTbName = "";
		PersistenceManager manager = null;
		try {
			manager = PersistenceManager.getInstance();
			JdbcSession session = manager.getJdbcSession();
			vTbName = new TempTable().createTempTable(session.getConnection(), name, "pk varchar(20) not null", "pk");
			session.setAddTimeStamp(false);
			String sql = "insert into " + vTbName + "(pk) values(?)";
			for (String pk : pksCtrlFormula) {
				SQLParameter params = new SQLParameter();
				params.addParam(pk);
				session.addBatch(sql, params);
			}
			session.executeBatch();
		} catch (DbException e) {
			NtbLogger.error(e.getMessage());
			throw new DAOException(e.getMessage());
		} catch (SQLException e) {
			NtbLogger.error(e.getMessage());
			throw new DAOException(e.getMessage());
		} finally {
			if (manager != null) {
				manager.release();
			}
		}
		return vTbName;
	}

	/*
 		 *
		 */
	@Override
	public List<BusiRuleVO> getNeedExecuteRuleByTaskDefAndAction(String pk_taskdef, String action) throws BusinessException {

		ArrayList<RelaRuleVO> vos = RuleServiceGetter.getIRelaRuleQuery().queryByPrimkeyAction(pk_taskdef, action);

		if(vos.isEmpty())
			return new ArrayList<BusiRuleVO>();
		

		ArrayList<String> rulePKs = new ArrayList<String>();
		for(int i = 0; i < vos.size(); i ++){
			RelaRuleVO vo = vos.get(i);
			if (vo.getIsExec() != null && vo.getIsExec().booleanValue()) {
				rulePKs.add(vo.getPk_busirule());
			}
		}
		if(rulePKs.isEmpty())
			return new ArrayList<BusiRuleVO>();
		
		return qyerBusiRuleyByPks(rulePKs.toArray(new String[0]));
		
		/*for (int n = 0; n < (vos == null ? 0 : vos.size()); n++) {
			RelaRuleVO vo = vos.get(n);

			if (vo.getIsExec() != null && vo.getIsExec().booleanValue()) {
				BusiRuleVO result = RuleServiceGetter.getIBusiRuleQuery().queryByPk(vo.getPk_busirule());
				if (result != null) {
					results.add(result);
				}
			}

		}
		return results;*/

		// BaseDAO dao=new BaseDAO();
		// String whereCond = " pk_otherobj = ? and actioncode=? and isExec =?";
		// SQLParameter param = new SQLParameter();
		//
		// if(NTBActionEnum.CHECKACTION .toCodeString().equals(action)){
		// whereCond = " pk_otherobj = ? and actioncode=? ";
		// param.addParam(pk_taskdef);
		// param.addParam(action);
		// }else{
		// param.addParam(pk_taskdef);
		// param.addParam(action);
		// param.addParam("Y");
		// }
		//
		// List<RelaRuleVO> dataVOList = (List<RelaRuleVO>) dao.retrieveByClause(RelaRuleVO.class, whereCond, param);
		//
		// if(dataVOList!=null&&dataVOList.size()>0){
		// List<String> rulePks = new ArrayList<String>();
		// for(RelaRuleVO rVo:dataVOList){
		// rulePks.add(rVo.getPk_busirule());
		// }
		// return this.qyerBusiRuleyByPks(rulePks.toArray(new String[0]));
		//
		// }
		// return new ArrayList<BusiRuleVO>();

	}

	
	
	
	public List<BusiRuleVO> getBusiRuleVOByTaskDefAndActionAndPower(String pk_taskdef, String pk_dataent,String action) throws BusinessException {

		ArrayList<RelaRuleVO> vos = RuleServiceGetter.getIRelaRuleQuery().queryByPrimkeyAction(pk_taskdef, action);

		List<BusiRuleVO> results = new ArrayList<BusiRuleVO>();
		for (int n = 0; n < (vos == null ? 0 : vos.size()); n++) {
			RelaRuleVO vo = vos.get(n);

			
			
			ObjectWithByteTranslateTool.convertBytesToObject((byte[])vo.getPk_refRange(), false);
			
			if (vo.getIsExec() != null && vo.getIsExec().booleanValue()) {
				BusiRuleVO result = RuleServiceGetter.getIBusiRuleQuery().queryByPk(vo.getPk_busirule());
				if (result != null) {
					results.add(result);
				}
			}

		}
		return results;
		}
	
	
	
	
	
	public List<DimRuleMemberParamVO> queryDimRuleMemberParamsByMemberPk(String memberPk) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		String whereCond = " pk_rulemember = ? ";
		SQLParameter param = new SQLParameter();
		param.addParam(memberPk);
		try{
			List<DimRuleMemberParamVO> dataVOList = (List<DimRuleMemberParamVO>) dao.retrieveByClause(DimRuleMemberParamVO.class, whereCond, param);
			return dataVOList;
		}catch(Exception ex){
			return new ArrayList<DimRuleMemberParamVO>();
		}

	}

	@Override
	public FormulaTracing queryFormulaTracing(DimSectionTuple defaultSectionTuple, DataCell selectCell, List<BusiRuleVO> rules) {
		RuleTracingManager tm = new RuleTracingManager();
		return tm.getRuleTracing(defaultSectionTuple, selectCell, rules);
	}

	@Override
	public FormulaTracing queryFormulaTracing(DimSectionTuple defaultSectionTuple, DataCell selectCell, BusiRuleVO ruleVO, DimFormulaVO tracingFormula) {
		RuleTracingManager tm = new RuleTracingManager();
		FormulaTracing tracing = tm.getRuleTracing(defaultSectionTuple, selectCell, ruleVO, tracingFormula);
		return tracing;
	}

	private void addFormulaM(DimFormulaVO vo) throws BusinessException {
		List<DimFormulaVO> vos = new ArrayList<DimFormulaVO>();
		vos.add(vo);
		addFormulaM(vos);
	}

	private void addFormulaM(Collection<DimFormulaVO> vos) throws BusinessException {
		if (vos == null || vos.isEmpty()) {
			return;
		}
		// List<String> pkDimFormulas = new ArrayList<>();
		// for(DimFormulaVO vo:vos){
		// pkDimFormulas.add(vo.getParentPKFieldName());
		// }
		List<String> fPks = new ArrayList<String>();
		for (DimFormulaVO formula : vos) {
			if(formula!=null)
			fPks.add(formula.getPrimaryKey());
		}
		List<DimFormulaMVO> mvos = queryMVOByPkFormulas(fPks);

		Map<String, List<DimFormulaMVO>> childMap = new HashMap<String, List<DimFormulaMVO>>();

		for(DimFormulaMVO m : mvos){
			List<DimFormulaMVO> list = null ;
			if(childMap.get(m.getPk_ruleformula())==null){
				list =  new ArrayList<DimFormulaMVO>();
				childMap.put(m.getPk_ruleformula(), list);
			}else{
				list = childMap.get(m.getPk_ruleformula());
			}
			list.add(m);
		}

		for (DimFormulaVO formula : vos) {
			// List<DimFormulaMVO> ms = new ArrayList<DimFormulaMVO>();
			ArrayList<DimFormulaMVO> list = new ArrayList<DimFormulaMVO>();
			if(childMap.get(formula.getPrimaryKey())!=null){
				list.addAll(childMap.get(formula.getPrimaryKey()));
			}
			formula.setMemberVOs(list);
		}

	}

	@Override
	public Map<String, List<DimFormulaVO>> getBusiRuleInfoByBusiRules(String[] pk_rules) throws BusinessException {

		Map<String, List<DimFormulaVO>> rtn = new HashMap<String, List<DimFormulaVO>>();
		if(pk_rules == null || pk_rules.length == 0) {
			return rtn;
		}
		BaseDAO dao = new BaseDAO();
		String condition = "pk_parent in (";
		StringBuffer condBuffer = new StringBuffer(condition);
		for (String pk : pk_rules) {
			condBuffer.append("'").append(pk).append("',");
		}
		condBuffer.replace(condBuffer.length() - 1, condBuffer.length(), "");
		condBuffer.append(")");
		Collection result = null;
		// try {
		Collection<DimFormulaVO> dataVOList = dao.retrieveByClause(DimFormulaVO.class, condBuffer.toString());

		List<String> fPks = new ArrayList<String>();
		for (DimFormulaVO formula : dataVOList) {
			fPks.add(formula.getPrimaryKey());
		}
		List<DimFormulaMVO> mvos = queryMVOByPkFormulas(fPks);

		for (String rule : pk_rules) {
			List<DimFormulaVO> formulas = new ArrayList<DimFormulaVO>();

			for (DimFormulaVO formula : dataVOList) {
				if (formula.getPk_parent().equals(rule)) {
					formulas.add(formula);
				}
			}
			for (DimFormulaVO formula : formulas) {
				// List<DimFormulaMVO> ms = new ArrayList<DimFormulaMVO>();
				formula.setMemberVOs(new ArrayList<DimFormulaMVO>());
				for (DimFormulaMVO m : mvos) {
					if (m.getPk_ruleformula().equals(formula.getPrimaryKey())) {
						formula.getMemberVOs().add(m);
					}
				}
			}

			rtn.put(rule, formulas);
		}

		return rtn;
	}

	@Override
	public FormulaTracing queryFormulaTracing(MdTask task, String nodtType, DataCell selectCell) {

		List<BusiRuleVO> allRules = null;

		try {

			allRules = RuleManager.getTracingBusiRuleVOByPkAction(task.getPk_taskdef(), nodtType);
		} catch (Exception e) {

			throw new BusinessRuntimeException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0", "01050plan002-0037")/*
																																		 * @res
																																		 * "规则追踪,查询规则发生错误"
																																		 */
					+ e.getMessage(), e);
		}

		TbTaskCtl.loadDimMemberValues(new MdTask[] { task }, true);

		return this.queryFormulaTracing(TbTaskCtl.getTaskParadim(task), selectCell, allRules);

	}

	@Override
	public List<BusiRuleVO> queryByRuleClassAndSysWithPkorgs(String bigClass,
			String ruleClass, String sys, List<String> pk_orgs)
			throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		if(pk_orgs == null || pk_orgs.size() == 0){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		for(String pk : pk_orgs){
			builder.append("'"+pk+"',");
		}
		builder.deleteCharAt(builder.lastIndexOf(","));
		String inStr = "pk_org in ("+builder.toString()+")";
		String str = " pk_bigclass = '" + bigClass + "' and pk_ruleclass = '" + ruleClass + "' and syscode='"+sys + "' and "+inStr+"  order by objname";
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, str);
		ArrayList<BusiRuleVO> voList = new ArrayList<BusiRuleVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	@Override
	public List<BusiRuleVO> queryByRuleClassAndSysAndPkBusiattr(
			String bigclass, String ruleclass, String sys, String pk_busiattr)
			throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String str = " pk_bigclass = '" + bigclass + "' and pk_ruleclass = '" + ruleclass +"' and pk_busiattr = '" +pk_busiattr+ "'    and syscode='"+sys+"'  order by objname";
		BusiRuleVO[] vos = (BusiRuleVO[]) dmo.queryByWhereClause(BusiRuleVO.class, str);
		ArrayList<BusiRuleVO> voList = new ArrayList<BusiRuleVO>();
		voList.addAll(Arrays.asList(vos));
		return voList;
	}

	@Override
	public List<BusiRuleVO> getNeedExecuteRuleByTaskDefAndAction(MdTask arg0, String arg1, boolean arg2)
			throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FormulaTracing queryFormulaTracing(DimSectionTuple arg0, DataCell arg1, BusiRuleVO arg2, DimFormulaVO arg3,
			MdTask arg4, TracingFormulaInfo arg5, Map<String, Object> arg6) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, List<DimFormulaMVO>> queryMVOByPkFormulas(List<String> arg0, boolean arg1)
			throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}


}
