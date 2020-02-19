package nc.impl.corg;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nc.bs.bd.baseservice.BDMultiLangUtil;
import nc.bs.bd.baseservice.md.BatchBaseService;
import nc.bs.corg.budgetorgstru.validator.BudgetOrgStruMemberNullValidator;
import nc.bs.corg.budgetorgstru.validator.BudgetOrgStruMemberRootValidator;
import nc.bs.corg.budgetorgstru.validator.BudgetOrgStruMemberUniqueEntityValidator;
import nc.bs.corg.budgetorgstru.validator.BudgetOrgStruMemberUniqueValidator;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.org.orgmodel.org.validator.OrgEnableValidator;
import nc.bs.uap.oid.OidGenerator;
import nc.bs.uif2.BusinessExceptionAdapter;
import nc.bs.uif2.validation.Validator;
import nc.impl.org.BusinessOrgValueSetterAdapter;
import nc.itf.corg.IBudgetStatStruMemberQryService;
import nc.itf.org.IOrgConst;
import nc.itf.org.IOrgMetaDataIDConst;
import nc.itf.org.IOrgSyncCostCenterBudgetService;
import nc.itf.org.IOrgVersionConst;
import nc.jdbc.framework.util.DBConsts;
import nc.md.MDBaseQueryFacade;
import nc.md.model.IBean;
import nc.newinstall.util.StringUtil;
import nc.pubitf.eaa.InnerCodeUtil;
import nc.pubitf.para.SysInitQuery;
import nc.vo.bd.pub.BatchDistributedUpdateValidator;
import nc.vo.bd.pub.IPubEnumConst;
import nc.vo.corg.BudgetOrgStruMemberVO;
import nc.vo.corg.BudgetOrgStruVO;
import nc.vo.corg.BudgetStatStruMemberVO;
import nc.vo.org.DeptVO;
import nc.vo.org.OrgVO;
import nc.vo.org.PlanBudgetVO;
import nc.vo.org.util.OrgPubUtil;
import nc.vo.org.util.OrgTypeManager;
import nc.vo.pub.BusinessException;
import nc.vo.pub.IBBDPubConst;
import nc.vo.pub.SuperVO;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.trade.sqlutil.IInSqlBatchCallBack;
import nc.vo.trade.sqlutil.InSqlBatchCaller;
import nc.vo.util.BDPKLockUtil;
import nc.vo.vorg.BudgetOrgStruMemberVersionVO;
import nc.vo.vorg.BudgetStatStruMemberVersionVO;
import nc.vo.vorg.DeptVersionVO;
import nc.vo.vorg.OrgVersionVO;
import nc.vo.vorg.PlanBudgetVersionVO;

/**
 * 批量引入预算组织体系成员
 * 该类主要完成以下功能:
 * * 根据输入的预算组织体系成员,生成预算组织体系成员
 * * 如果成员未勾选预算组织,则勾选预算组织并启用
 * * 同步预算组织体系成员到预算统计体系中
 * * 同时对新增的所有VO添加版本信息
 * @version 6.0
 * @since 6.0
 * @author tanglv
 * @time 2012-10-26 上午09:25:48
 */
public class BudgetOrgStruBatchImportService {

  /**以下两个MAP主要用以描述预算组织体系成员与预算统计体系成员,以及组织与预算组织体系成员之间的关系*/
  //pk_bosm-pk_bssm,预算组织体系成员PK-预算统计体系成员PK映射,由于在组织预算统计体系成员结构的时候,需要根据预算统计体系成员对应的预算组织体系成员的上下级关系进行构造
  private Map<String, String> bosmToBssm;

  //pk_org-pk_bosm,组织对预算组织体系成员PK映射,批量引入中组织与预算组织体系一一对应但PK不一样,在勾选预算组织体系虚组织标记时需要用到这个映射关系
  private Map<String, String> orgToBosm;

  /**以下7个MAP主要用以记录PK与VO的关系,以便根据上述两个关系查找各VO信息*/
  //pk_bosm-vo_bosm,预算组织体系成员PK-VO映射,用于查找构造引入成员的上下级关系
  private Map<String, BudgetOrgStruMemberVO> bosmMapping;

  //pk_bssm-vo_bssm,预算统计体系成员PK-VO映射,处了记录需要处理的成员VO,还记录它们的PK,用以进行插入后的查找
  private Map<String, BudgetStatStruMemberVO> bssmMapping;

  //pk_org-vo_org,组织PK-VO映射,需要用来根据OrgVO判断成员类型,记录PK等
  private Map<String, OrgVO> orgMapping;

  //pk_org-vo_dept,部门PK-VO映射,需要用来根据DeptVO判断成员预算组织是否勾选
  private Map<String, DeptVO> deptMapping;

  //pk_org-vo_costcenter,实际上只用到values(),但是为了与其它成员风格一致所以用了MAP
  private Map<String, OrgVO> costcenterMapping;

  //pk_org-vo_businessunit,实际上只用到values(),但是为了与其它成员风格一致所以用了MAP
  private Map<String, OrgVO> buMapping;

  //pk_org-vo_budget,预算组织PK-VO映射,用来判断成员预算组织是否启用,记录PK等用途
  private Map<String, PlanBudgetVO> budgetMapping;

  /**以下为业务逻辑*/

  public BudgetOrgStruMemberVO[] importBudgetOrgStruMembers(
      BudgetOrgStruMemberVO[] vos) throws BusinessException {
    if (vos == null || vos.length == 0)
      return vos;

    try {
      //处理输入信息,生成可供更新或新增的VO集合
      init(vos);

      //锁定预算组织体系及相应的预算统计体系
      Map<String, BudgetOrgStruMemberVO> m =
          OrgPubUtil.extractField(vos, BudgetOrgStruMemberVO.PK_BOS);
      BDPKLockUtil.lockString(m.keySet().toArray(new String[0]));

      reOrganizeBosm(vos);
      handleOrg(vos);
      handleDept(vos);
      handleBudgetAndOrgTypes(vos);
      handleBssm(vos);

      //批量更新部门数据
      deptService.updateVO(deptMapping.values().toArray(new DeptVO[0]));
      //批量更新部门版本数据
      deptvService.updateVO(this.getVersionVOList(deptMapping.values(),
          VOStatus.UPDATED).toArray(new DeptVersionVO[0]));
      //因为业务单元和部门都需要更新orgvo,所以这里使用一个临时表
      Map<String, OrgVO> tempOrgVO = new HashMap<String, OrgVO>();
      for (String key : deptMapping.keySet()) {
        tempOrgVO.put(key, orgMapping.get(key));
      }
      tempOrgVO.putAll(buMapping);
      //批量更新业务单元数据
      orgService.updateVO(tempOrgVO.values().toArray(new OrgVO[0]));
      //批量更新业务单元版本数据
      orgvService.updateVO(this.getVersionVOList(tempOrgVO.values(),
          VOStatus.UPDATED).toArray(new OrgVersionVO[0]));
      //批量新增预算
      budgetService.insertVO(budgetMapping.values()
          .toArray(new PlanBudgetVO[0]));
      Collection<PlanBudgetVO> budgets =
          this.getVOListByPk(PlanBudgetVO.class, budgetMapping.keySet());
      for (PlanBudgetVO b : budgets) {
        budgetMapping.put(b.getPk_planbudget(), b);
      }
      //批量新增预算版本
      budgetvService.insertVO(this.getVersionVOList(budgetMapping.values(),
          VOStatus.NEW).toArray(new PlanBudgetVersionVO[0]));
      //回写成本中心
      for (OrgVO org : costcenterMapping.values()) {
        getOrgSyncCostCenterBudgetService().updateCCBudgetFlag(org.getPk_org(),
            UFBoolean.TRUE);
      }
      enableBudget(vos);

      //批量新增成员
      BudgetOrgStruMemberVO[] insertedBosmVO =
          bosmService.insertVO(bosmMapping.values().toArray(
              new BudgetOrgStruMemberVO[0]));
      //生成内部编码
      List<List<SuperVO>> bfsBudgetOrgStruMembers =
          bfsClassify(insertedBosmVO, BudgetOrgStruMemberVO.PK_FATHERMEMBER);
      for (List<SuperVO> ls : bfsBudgetOrgStruMembers) {
        for (SuperVO vo : ls) {
          InnerCodeUtil.generateInnerCodeAfterInsert(vo);
        }
      }
      Collection<BudgetOrgStruMemberVO> bosm =
          this.getVOListByPk(BudgetOrgStruMemberVO.class, bosmMapping.keySet());
      //批量新增成员版本
      bosmvService.insertVO(this.getVersionVOList(bosm, VOStatus.NEW).toArray(
          new BudgetOrgStruMemberVersionVO[0]));

      //批量新增统计成员
      BudgetStatStruMemberVO[] insertedBssmVO =
          bssmService.insertVO(bssmMapping.values().toArray(
              new BudgetStatStruMemberVO[0]));
      //生成内部编码
      List<List<SuperVO>> bfsBudgetStatStruMembers =
          bfsClassify(insertedBssmVO, BudgetStatStruMemberVO.PK_FATHERMEMBER);
      for (List<SuperVO> ls : bfsBudgetStatStruMembers) {
        for (SuperVO vo : ls) {
          InnerCodeUtil.generateInnerCodeAfterInsert(vo);
        }
      }
      Collection<BudgetStatStruMemberVO> bssm =
          this
              .getVOListByPk(BudgetStatStruMemberVO.class, bssmMapping.keySet());
      //批量新增统计成员版本
      bssmvService.insertVO(this.getVersionVOList(bssm, VOStatus.NEW).toArray(
          new BudgetStatStruMemberVersionVO[0]));
      return bosm.toArray(new BudgetOrgStruMemberVO[0]);
    }
    catch (BusinessException e) {
      throw e;
    }
    catch (Exception e) {
      throw new BusinessException(e);
    }

  }

  //初始化
  private void init(BudgetOrgStruMemberVO[] vos) throws BusinessException {
    bosmMapping = new HashMap<String, BudgetOrgStruMemberVO>();
    bssmMapping = new HashMap<String, BudgetStatStruMemberVO>();
    bosmToBssm = new HashMap<String, String>();
    orgToBosm = new HashMap<String, String>();
    orgMapping = new HashMap<String, OrgVO>();
    deptMapping = new HashMap<String, DeptVO>();
    costcenterMapping = new HashMap<String, OrgVO>();
    buMapping = new HashMap<String, OrgVO>();
    budgetMapping = new HashMap<String, PlanBudgetVO>();
  }

  //重新组织预算组织体系结构,因为输入的组织体系结构是被处理过PK的,不能直接使用
  private void reOrganizeBosm(BudgetOrgStruMemberVO[] vos)
      throws BusinessException {
    //引入时都是同一体系的,直接查询
    BudgetOrgStruVO stru =
        (BudgetOrgStruVO) getDAO().retrieveByPK(BudgetOrgStruVO.class,
            vos[0].getPk_bos());
    //生成OID重新组织树结构
    Map<String, BudgetOrgStruMemberVO> vosOldKeyMapping =
        new HashMap<String, BudgetOrgStruMemberVO>();
    for (BudgetOrgStruMemberVO bosm : vos) {
      vosOldKeyMapping.put(bosm.getPk_bosmember(), bosm);
      bosm.setPk_bosmember(OidGenerator.getInstance().nextOid());
    }
    for (BudgetOrgStruMemberVO bosm : vos) {
      BudgetOrgStruMemberVO father =
          vosOldKeyMapping.get(bosm.getPk_fathermember());
      if (father != null) {
        bosm.setPk_fathermember(father.getPk_bosmember());
      }
      //设置版本信息
      setVOVersionInfo(bosm);
      bosm.setPk_svid(stru.getPk_vid());
    }
  }

  //收集所有对应组织VO
  private void handleOrg(BudgetOrgStruMemberVO[] vos) throws BusinessException {
    //收集org-member对应关系,收集PK,收集KEY-成员对应关系
    for (BudgetOrgStruMemberVO bosm : vos) {
      bosmMapping.put(bosm.getPk_bosmember(), bosm);
      orgMapping.put(bosm.getPk_org(), null);
      orgToBosm.put(bosm.getPk_org(), bosm.getPk_bosmember());

    }
    //获取orgVO
    Collection<OrgVO> orgData =
        this.getVOListByPk(OrgVO.class, orgMapping.keySet());
    for (OrgVO org : orgData) {
      orgMapping.put(org.getPk_org(), org);
      bosmMapping.get(orgToBosm.get(org.getPk_org())).setPk_orgvid(
          org.getPk_vid());
    }
  }

  //获取deptVO,设置预算标记,获取相应的预算VO
  private void handleDept(BudgetOrgStruMemberVO[] vos) throws BusinessException {

    Collection<DeptVO> deptData =
        this.getVOListByPk(DeptVO.class, orgMapping.keySet());
    for (DeptVO dept : deptData) {
      if (!OrgTypeManager.getInstance()
          .isTypeOf(dept, IOrgConst.PLANBUDGETTYPE)) {
        deptMapping.put(dept.getPk_dept(), dept);
        budgetMapping.put(dept.getPk_dept(),
            (PlanBudgetVO) new BusinessOrgValueSetterAdapter().valueSet(dept,
                new PlanBudgetVO(), PlanBudgetVO.PK_PLANBUDGET, VOStatus.NEW));
        dept.setOrgtype17(UFBoolean.TRUE);
        orgMapping.get(dept.getPk_dept()).setOrgtype17(UFBoolean.TRUE);
      }
    }
  }

  //收集成本中心以及业务单元,获取相应的预算VO
  private void handleBudgetAndOrgTypes(BudgetOrgStruMemberVO[] vos)
      throws BusinessException {
    //虚组织一般为预算组织成员或预算统计成员,已经为预算组织,没有需要更新的数据,所以直接过滤掉
    for (String key : orgMapping.keySet()) {
      if (deptMapping.containsKey(key)) {
        continue;
      }
      OrgVO org = orgMapping.get(key);
      assert (org != null);
      if (!OrgTypeManager.getInstance().isTypeOf(org, IOrgConst.PLANBUDGETTYPE)) {
        if (OrgTypeManager.getInstance()
            .isTypeOf(org, IOrgConst.RESACOSTCENTER)) {
          costcenterMapping.put(key, org);
        }
        else if (UFBoolean.TRUE.equals(org.getIsbusinessunit())) {
          buMapping.put(key, org);
        }
        budgetMapping.put(key,
            (PlanBudgetVO) new BusinessOrgValueSetterAdapter().valueSet(org,
                new PlanBudgetVO(), PlanBudgetVO.PK_PLANBUDGET, VOStatus.NEW));
        org.setOrgtype17(UFBoolean.TRUE);
      }
      if (UFBoolean.FALSE.equals(org.getIsbusinessunit())) {
        //虚组织勾选虚组织标记
        //已在引入中进行标记
        //bosmMapping.get(orgToBosm.get(key)).setVirtualorg(UFBoolean.TRUE);
      }
    }
  }

  //生成预算统计体系成员
  private void handleBssm(BudgetOrgStruMemberVO[] vos) throws BusinessException {
    String[] bosmNames = new BudgetOrgStruMemberVO().getAttributeNames();
    for (BudgetOrgStruMemberVO bosm : bosmMapping.values()) {
      BudgetStatStruMemberVO bssm = new BudgetStatStruMemberVO();
      for (String n : bosmNames) {
        bssm.setAttributeValue(n, bosm.getAttributeValue(n));
      }
      bssm.setStatus(bosm.getStatus());
      bssm.setPk_bssmember(OidGenerator.getInstance().nextOid());
      bssmMapping.put(bssm.getPk_bssmember(), bssm);
      bosmToBssm.put(bosm.getPk_bosmember(), bssm.getPk_bssmember());
    }
    for (BudgetStatStruMemberVO bssm : bssmMapping.values()) {
      String pk_father =
          bosmMapping.get(bssm.getPk_bosmember()).getPk_fathermember();
      String father = bosmToBssm.get(pk_father);
      //因引入一次一般只有一个根,所以直接查,不影响效率
      if (StringUtil.isEmpty(pk_father)) {
        father = null;
      }
      else if (father == null) {
        BudgetStatStruMemberVO fatherVO =
            getBudgetStatStruMemberQryService()
                .queryBudgetStatStruMemberVOByBudgetOrgStruMemberVOPK(
                    pk_father, bssm.getPk_bos());
        father = fatherVO.getPk_bssmember();
      }

      bssm.setPk_fathermember(father);
    }
  }

  //启用预算组织
  private void enableBudget(BudgetOrgStruMemberVO[] vos)
      throws BusinessException {
    //查询"是否自动启用"参数值，根据该参数值来决定是否调用启用方法
    String usedorgcode =
        OrgTypeManager.getInstance().getOrgTypeByFullclassname(
            PlanBudgetVO.class.getName()).getUsedparacode();
    UFBoolean paramvalue =
        SysInitQuery.getParaBoolean(InvocationInfoProxy.getInstance()
            .getGroupId(), usedorgcode);
    if (UFBoolean.TRUE.equals(paramvalue)) {
      //部门和业务单元的启用要分开
      List<List<SuperVO>> bfsBudgetStru =
          bfsClassify(buMapping.values().toArray(new OrgVO[0]),
              OrgVO.PK_FATHERORG);
      List<List<SuperVO>> bfsDeptBudgetStru =
          bfsClassify(deptMapping.values().toArray(new DeptVO[0]),
              DeptVO.PK_FATHERORG);
      List<List<SuperVO>> allBudgetStru = new ArrayList<List<SuperVO>>();
      allBudgetStru.addAll(bfsBudgetStru);
      allBudgetStru.addAll(bfsDeptBudgetStru);
      //将业务单元按广度优先原则分类,然后将预算职能按照这个分类顺序进行启用,因为预算组织本身不包含上下级结构数据
      //按深度顺序进行启用,避免出现上级未启用情况
      for (List<SuperVO> ls : allBudgetStru) {
        List<PlanBudgetVO> tempBudgets = new ArrayList<PlanBudgetVO>();
        for (SuperVO vo : ls) {
          //如果新增预算职能,未启用则进行启用
          PlanBudgetVO b = budgetMapping.get(vo.getPrimaryKey());
          if (b != null && IPubEnumConst.ENABLESTATE_INIT == b.getEnablestate()) {
            tempBudgets.add(b);
          }
        }
        if (tempBudgets.size() > 0) {
          budgetService.enableVO(tempBudgets.toArray(new PlanBudgetVO[0]));
        }
      }
    }
  }

  /**以下为服务,接口,和工具*/

  private IOrgSyncCostCenterBudgetService orgSyncCostCenterBudgetService;

  private IOrgSyncCostCenterBudgetService getOrgSyncCostCenterBudgetService() {
    if (orgSyncCostCenterBudgetService == null) {
      orgSyncCostCenterBudgetService =
          NCLocator.getInstance().lookup(IOrgSyncCostCenterBudgetService.class);
    }
    return orgSyncCostCenterBudgetService;
  }

  private IBudgetStatStruMemberQryService budgetStatStruMemberQryService;

  private IBudgetStatStruMemberQryService getBudgetStatStruMemberQryService() {
    if (budgetStatStruMemberQryService == null) {
      budgetStatStruMemberQryService =
          NCLocator.getInstance().lookup(IBudgetStatStruMemberQryService.class);
    }
    return budgetStatStruMemberQryService;
  }

  private BaseDAO dao;

  private BaseDAO getDAO() {
    if (dao == null) {
      dao = new BaseDAO();
    }
    return dao;
  }

  private BatchBaseService<BudgetOrgStruMemberVO> bosmService =
      new BudgetOrgStruBatchBaseService<BudgetOrgStruMemberVO>(
          IOrgMetaDataIDConst.BUDGETORGSTRUMEMBER, null) {
        @Override
        protected Validator[] getInsertValidator() {
          List<Validator> vs =
              new ArrayList<Validator>(Arrays
                  .asList(super.getInsertValidator()));
          vs.add(new BudgetOrgStruMemberNullValidator());
          vs.add(new BudgetOrgStruMemberRootValidator());
          vs.add(new BudgetOrgStruMemberUniqueEntityValidator());
          vs.add(new BudgetOrgStruMemberUniqueValidator());
          return vs.toArray(new Validator[0]);
        }
      };

  private BatchBaseService<BudgetOrgStruMemberVersionVO> bosmvService =
      new BudgetOrgStruBatchBaseService<BudgetOrgStruMemberVersionVO>(
          IOrgMetaDataIDConst.BUDGETORGSTRUMEMBER_V, null);

  private BatchBaseService<BudgetStatStruMemberVO> bssmService =
      new BudgetOrgStruBatchBaseService<BudgetStatStruMemberVO>(
          IOrgMetaDataIDConst.BUDGETSTATSTRUMEMBER, null);

  private BatchBaseService<BudgetStatStruMemberVersionVO> bssmvService =
      new BudgetOrgStruBatchBaseService<BudgetStatStruMemberVersionVO>(
          IOrgMetaDataIDConst.BUDGETSTATSTRUMEMBER_V, null);

  //目前启用预算没有需要校验的数据,所以不添加任何校验
  private BatchBaseService<PlanBudgetVO> budgetService =
      new BudgetOrgStruBatchBaseService<PlanBudgetVO>(
          IOrgMetaDataIDConst.PLANBUDGET, null);

  private BatchBaseService<PlanBudgetVersionVO> budgetvService =
      new BudgetOrgStruBatchBaseService<PlanBudgetVersionVO>(
          IOrgMetaDataIDConst.PLANBUDGET_V, null);

  private BatchBaseService<OrgVO> orgService =
      new BudgetOrgStruBatchBaseService<OrgVO>(IOrgMetaDataIDConst.ORG, null) {
        @Override
        protected Validator[] getUpdateValidator(OrgVO[] oldVO) {
          List<Validator> vs =
              new ArrayList<Validator>(Arrays.asList(super
                  .getUpdateValidator(oldVO)));
          vs.add(new OrgEnableValidator());
          return vs.toArray(new Validator[0]);
        }
      };

  private BatchBaseService<OrgVersionVO> orgvService =
      new BudgetOrgStruBatchBaseService<OrgVersionVO>(
          IOrgMetaDataIDConst.ORG_V, null);

  private BatchBaseService<DeptVO> deptService =
      new BudgetOrgStruBatchBaseService<DeptVO>(IOrgMetaDataIDConst.DEPT, null);

  private BatchBaseService<DeptVersionVO> deptvService =
      new BudgetOrgStruBatchBaseService<DeptVersionVO>(
          IOrgMetaDataIDConst.DEPT_V, null);

  //继承了批量操作方法,用以屏蔽一些不必要的校验类如唯一性规则等,还用以改善一些性能问题
  //这里没有改为静态内部类有以下原因
  //1.内部类与静态内部类相比,只不过多了一个宿主的引用,并且不能进行嵌套,在初始化还是其它方面上的效率没有区别,而且这个也不是性能的瓶颈,这个修改不会对性能起到显著的改善
  //2.内部类可以访问宿主的成员变量和成员方法,而静态内部类不能,对于需要使用部分宿主提供的工具方法(getVOListByPk)的情况来说,只能选择内部类
  class BudgetOrgStruBatchBaseService<T extends SuperVO> extends
      BatchBaseService<T> {

    private Class<T> k;

    public BudgetOrgStruBatchBaseService(
        String MDId, String[] subAttributeNames) {
      super(MDId, subAttributeNames);

    }

    public BudgetOrgStruBatchBaseService(
        String MDId) {
      super(MDId);
    }

    private Class<T> getMDClass() {
      if (k == null) {
        try {
          IBean bean = MDBaseQueryFacade.getInstance().getBeanByID(getMDId());
          k = (Class<T>) Class.forName(bean.getFullClassName());
        }
        catch (Exception e) {
          throw new BusinessExceptionAdapter(new BusinessException(e));
        }
      }
      return k;

    }

    @Override
    protected Validator[] getInsertValidator() {
      return super.getInsertValidator();
    }

    //没有必要校验管控模式,因为引入不会修改编码名称
    @Override
    protected Validator[] getUpdateValidator(T[] oldVO) {
      Validator[] validators = new Validator[] {
        new BatchDistributedUpdateValidator()
      };
      return validators;
    }

    @Override
    protected T[] retrieveVO(String[] pks) throws BusinessException {
      return BudgetOrgStruBatchImportService.this.getVOListByPk(getMDClass(),
          Arrays.asList(pks)).toArray((T[]) Array.newInstance(getMDClass(), 0));
    }

    @Override
    protected String[] dbInsertVO(T... vos) throws BusinessException {
      return super.dbInsertVO(vos);
    }

    @Override
    protected void dbUpdateVO(T... vos) throws BusinessException {
      super.dbUpdateVO(vos);
    }
  }

  //根据VO获取版本VO工具
  private <T extends SuperVO, V extends SuperVO> List<V> getVersionVOList(
      Collection<T> vos, int vostatus) throws BusinessException {

    List<V> vls = new ArrayList<V>();
    if (vos == null || vos.size() == 0)
      return vls;

    try {
      Map<V, T> relation = new HashMap<V, T>();
      Class<V> k =
          OrgPubUtil
              .getVersionClass(vos.iterator().next().getClass().getName());
      String[] attrs = vos.iterator().next().getAttributeNames();
      if (vostatus == VOStatus.UPDATED || vostatus == VOStatus.DELETED) {
        Map<String, T> pks = new HashMap<String, T>();
        for (T t : vos) {
          pks.put(t.getPrimaryKey(), t);
        }
        vls.addAll(getVOListByPk(k, pks.keySet()));
        for (V v : vls) {
          relation.put(v, pks.get(v.getPrimaryKey()));
        }
      }
      else if (vostatus == VOStatus.NEW) {
        for (T t : vos) {
          V vx = k.newInstance();
          vls.add(vx);
          relation.put(vx, t);
        }
      }
      for (V v : vls) {
        v.setStatus(vostatus);
        T tx = relation.get(v);
        for (String attr : attrs) {
          if (IBBDPubConst.TS_FIELD.equals(attr)) {
            continue;
          }
          v.setAttributeValue(attr, tx.getAttributeValue(attr));
        }
      }
      return vls;
    }
    catch (BusinessException e) {
      throw e;
    }
    catch (Exception e) {
      throw new BusinessException(e);
    }
  }

  //根据PK获取VO工具
  private <T extends SuperVO> List<T> getVOListByPk(final Class<T> k,
      final Collection<String> pks) throws BusinessException {
    final List<T> result = new ArrayList<T>();
    InSqlBatchCaller caller = new InSqlBatchCaller(pks.toArray(new String[0]));
    try {
      caller.execute(new IInSqlBatchCallBack() {
        @Override
        public Object doWithInSql(String inSql) throws BusinessException,
            SQLException {
          try {
            String condition =
                k.newInstance().getPKFieldName() + " in " + inSql;
            Collection<T> c = new BaseDAO().retrieveByClause(k, condition);
            if (c != null && c.size() > 0) {
              result.addAll(c);
            }
          }
          catch (BusinessException e) {
            throw e;
          }
          catch (Exception e) {
            throw new BusinessExceptionAdapter(new BusinessException(e));
          }
          return null;
        }
      });
      return result;
    }
    catch (BusinessException e) {
      throw e;
    }
    catch (SQLException e) {
      throw new BusinessExceptionAdapter(new BusinessException(e));
    }
  }

  //根据广度优先原则归类工具
  private List<List<SuperVO>> bfsClassify(SuperVO[] vos, String parentField) {
    Map<String, SuperVO> map = new HashMap<String, SuperVO>();
    Map<Integer, List<SuperVO>> deep = new HashMap<Integer, List<SuperVO>>();
    for (int i = 0; i < vos.length; i++) {
      map.put(vos[i].getPrimaryKey(), vos[i]);
    }
    for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
      String key = it.next();
      String aim = key;
      String parent = null;
      int i = 0;
      while ((parent = (String) map.get(aim).getAttributeValue(parentField)) != null
          && !DBConsts.NULL_WAVE.equals(parent) && map.containsKey(parent)) {
        aim = map.get(parent).getPrimaryKey();
        i++;
      }
      List<SuperVO> list = deep.get(i);
      if (list == null) {
        list = new ArrayList<SuperVO>();
        deep.put(i, list);
      }
      list.add(map.get(key));
    }
    List<Integer> sk = new ArrayList<Integer>(deep.keySet());
    Collections.sort(sk);
    List<List<SuperVO>> result = new ArrayList<List<SuperVO>>();
    for (Integer i : sk) {
      result.add(deep.get(i));
    }
    return result;
  }

  //设置版本信息
  private void setVOVersionInfo(SuperVO vo) throws BusinessException{
	BDMultiLangUtil.setMultiLangValues(vo, IOrgVersionConst.VNAME, "org", "0org0284"/*初始版本*/);
    UFDate time = new UFDate(new Date());
    vo.setAttributeValue(IOrgVersionConst.VNO, time.getYear() + "01");
    vo.setAttributeValue(IOrgVersionConst.PK_VID, OidGenerator.getInstance()
        .nextOid());
    vo.setAttributeValue(IOrgVersionConst.VSTARTDATE, time);
    vo.setAttributeValue(IOrgVersionConst.VENDDATE, new UFDate(
        IOrgVersionConst.DEFAULTVENDDATE, false));
    vo.setAttributeValue(IOrgVersionConst.ISLASTVERSION, UFBoolean.TRUE);
  }
}
