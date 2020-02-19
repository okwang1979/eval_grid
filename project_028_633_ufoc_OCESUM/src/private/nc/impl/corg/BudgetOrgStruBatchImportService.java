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
 * ��������Ԥ����֯��ϵ��Ա
 * ������Ҫ������¹���:
 * * ���������Ԥ����֯��ϵ��Ա,����Ԥ����֯��ϵ��Ա
 * * �����Աδ��ѡԤ����֯,��ѡԤ����֯������
 * * ͬ��Ԥ����֯��ϵ��Ա��Ԥ��ͳ����ϵ��
 * * ͬʱ������������VO��Ӱ汾��Ϣ
 * @version 6.0
 * @since 6.0
 * @author tanglv
 * @time 2012-10-26 ����09:25:48
 */
public class BudgetOrgStruBatchImportService {

  /**��������MAP��Ҫ��������Ԥ����֯��ϵ��Ա��Ԥ��ͳ����ϵ��Ա,�Լ���֯��Ԥ����֯��ϵ��Ա֮��Ĺ�ϵ*/
  //pk_bosm-pk_bssm,Ԥ����֯��ϵ��ԱPK-Ԥ��ͳ����ϵ��ԱPKӳ��,��������֯Ԥ��ͳ����ϵ��Ա�ṹ��ʱ��,��Ҫ����Ԥ��ͳ����ϵ��Ա��Ӧ��Ԥ����֯��ϵ��Ա�����¼���ϵ���й���
  private Map<String, String> bosmToBssm;

  //pk_org-pk_bosm,��֯��Ԥ����֯��ϵ��ԱPKӳ��,������������֯��Ԥ����֯��ϵһһ��Ӧ��PK��һ��,�ڹ�ѡԤ����֯��ϵ����֯���ʱ��Ҫ�õ����ӳ���ϵ
  private Map<String, String> orgToBosm;

  /**����7��MAP��Ҫ���Լ�¼PK��VO�Ĺ�ϵ,�Ա��������������ϵ���Ҹ�VO��Ϣ*/
  //pk_bosm-vo_bosm,Ԥ����֯��ϵ��ԱPK-VOӳ��,���ڲ��ҹ��������Ա�����¼���ϵ
  private Map<String, BudgetOrgStruMemberVO> bosmMapping;

  //pk_bssm-vo_bssm,Ԥ��ͳ����ϵ��ԱPK-VOӳ��,���˼�¼��Ҫ����ĳ�ԱVO,����¼���ǵ�PK,���Խ��в����Ĳ���
  private Map<String, BudgetStatStruMemberVO> bssmMapping;

  //pk_org-vo_org,��֯PK-VOӳ��,��Ҫ��������OrgVO�жϳ�Ա����,��¼PK��
  private Map<String, OrgVO> orgMapping;

  //pk_org-vo_dept,����PK-VOӳ��,��Ҫ��������DeptVO�жϳ�ԱԤ����֯�Ƿ�ѡ
  private Map<String, DeptVO> deptMapping;

  //pk_org-vo_costcenter,ʵ����ֻ�õ�values(),����Ϊ����������Ա���һ����������MAP
  private Map<String, OrgVO> costcenterMapping;

  //pk_org-vo_businessunit,ʵ����ֻ�õ�values(),����Ϊ����������Ա���һ����������MAP
  private Map<String, OrgVO> buMapping;

  //pk_org-vo_budget,Ԥ����֯PK-VOӳ��,�����жϳ�ԱԤ����֯�Ƿ�����,��¼PK����;
  private Map<String, PlanBudgetVO> budgetMapping;

  /**����Ϊҵ���߼�*/

  public BudgetOrgStruMemberVO[] importBudgetOrgStruMembers(
      BudgetOrgStruMemberVO[] vos) throws BusinessException {
    if (vos == null || vos.length == 0)
      return vos;

    try {
      //����������Ϣ,���ɿɹ����»�������VO����
      init(vos);

      //����Ԥ����֯��ϵ����Ӧ��Ԥ��ͳ����ϵ
      Map<String, BudgetOrgStruMemberVO> m =
          OrgPubUtil.extractField(vos, BudgetOrgStruMemberVO.PK_BOS);
      BDPKLockUtil.lockString(m.keySet().toArray(new String[0]));

      reOrganizeBosm(vos);
      handleOrg(vos);
      handleDept(vos);
      handleBudgetAndOrgTypes(vos);
      handleBssm(vos);

      //�������²�������
      deptService.updateVO(deptMapping.values().toArray(new DeptVO[0]));
      //�������²��Ű汾����
      deptvService.updateVO(this.getVersionVOList(deptMapping.values(),
          VOStatus.UPDATED).toArray(new DeptVersionVO[0]));
      //��Ϊҵ��Ԫ�Ͳ��Ŷ���Ҫ����orgvo,��������ʹ��һ����ʱ��
      Map<String, OrgVO> tempOrgVO = new HashMap<String, OrgVO>();
      for (String key : deptMapping.keySet()) {
        tempOrgVO.put(key, orgMapping.get(key));
      }
      tempOrgVO.putAll(buMapping);
      //��������ҵ��Ԫ����
      orgService.updateVO(tempOrgVO.values().toArray(new OrgVO[0]));
      //��������ҵ��Ԫ�汾����
      orgvService.updateVO(this.getVersionVOList(tempOrgVO.values(),
          VOStatus.UPDATED).toArray(new OrgVersionVO[0]));
      //��������Ԥ��
      budgetService.insertVO(budgetMapping.values()
          .toArray(new PlanBudgetVO[0]));
      Collection<PlanBudgetVO> budgets =
          this.getVOListByPk(PlanBudgetVO.class, budgetMapping.keySet());
      for (PlanBudgetVO b : budgets) {
        budgetMapping.put(b.getPk_planbudget(), b);
      }
      //��������Ԥ��汾
      budgetvService.insertVO(this.getVersionVOList(budgetMapping.values(),
          VOStatus.NEW).toArray(new PlanBudgetVersionVO[0]));
      //��д�ɱ�����
      for (OrgVO org : costcenterMapping.values()) {
        getOrgSyncCostCenterBudgetService().updateCCBudgetFlag(org.getPk_org(),
            UFBoolean.TRUE);
      }
      enableBudget(vos);

      //����������Ա
      BudgetOrgStruMemberVO[] insertedBosmVO =
          bosmService.insertVO(bosmMapping.values().toArray(
              new BudgetOrgStruMemberVO[0]));
      //�����ڲ�����
      List<List<SuperVO>> bfsBudgetOrgStruMembers =
          bfsClassify(insertedBosmVO, BudgetOrgStruMemberVO.PK_FATHERMEMBER);
      for (List<SuperVO> ls : bfsBudgetOrgStruMembers) {
        for (SuperVO vo : ls) {
          InnerCodeUtil.generateInnerCodeAfterInsert(vo);
        }
      }
      Collection<BudgetOrgStruMemberVO> bosm =
          this.getVOListByPk(BudgetOrgStruMemberVO.class, bosmMapping.keySet());
      //����������Ա�汾
      bosmvService.insertVO(this.getVersionVOList(bosm, VOStatus.NEW).toArray(
          new BudgetOrgStruMemberVersionVO[0]));

      //��������ͳ�Ƴ�Ա
      BudgetStatStruMemberVO[] insertedBssmVO =
          bssmService.insertVO(bssmMapping.values().toArray(
              new BudgetStatStruMemberVO[0]));
      //�����ڲ�����
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
      //��������ͳ�Ƴ�Ա�汾
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

  //��ʼ��
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

  //������֯Ԥ����֯��ϵ�ṹ,��Ϊ�������֯��ϵ�ṹ�Ǳ������PK��,����ֱ��ʹ��
  private void reOrganizeBosm(BudgetOrgStruMemberVO[] vos)
      throws BusinessException {
    //����ʱ����ͬһ��ϵ��,ֱ�Ӳ�ѯ
    BudgetOrgStruVO stru =
        (BudgetOrgStruVO) getDAO().retrieveByPK(BudgetOrgStruVO.class,
            vos[0].getPk_bos());
    //����OID������֯���ṹ
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
      //���ð汾��Ϣ
      setVOVersionInfo(bosm);
      bosm.setPk_svid(stru.getPk_vid());
    }
  }

  //�ռ����ж�Ӧ��֯VO
  private void handleOrg(BudgetOrgStruMemberVO[] vos) throws BusinessException {
    //�ռ�org-member��Ӧ��ϵ,�ռ�PK,�ռ�KEY-��Ա��Ӧ��ϵ
    for (BudgetOrgStruMemberVO bosm : vos) {
      bosmMapping.put(bosm.getPk_bosmember(), bosm);
      orgMapping.put(bosm.getPk_org(), null);
      orgToBosm.put(bosm.getPk_org(), bosm.getPk_bosmember());

    }
    //��ȡorgVO
    Collection<OrgVO> orgData =
        this.getVOListByPk(OrgVO.class, orgMapping.keySet());
    for (OrgVO org : orgData) {
      orgMapping.put(org.getPk_org(), org);
      bosmMapping.get(orgToBosm.get(org.getPk_org())).setPk_orgvid(
          org.getPk_vid());
    }
  }

  //��ȡdeptVO,����Ԥ����,��ȡ��Ӧ��Ԥ��VO
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

  //�ռ��ɱ������Լ�ҵ��Ԫ,��ȡ��Ӧ��Ԥ��VO
  private void handleBudgetAndOrgTypes(BudgetOrgStruMemberVO[] vos)
      throws BusinessException {
    //����֯һ��ΪԤ����֯��Ա��Ԥ��ͳ�Ƴ�Ա,�Ѿ�ΪԤ����֯,û����Ҫ���µ�����,����ֱ�ӹ��˵�
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
        //����֯��ѡ����֯���
        //���������н��б��
        //bosmMapping.get(orgToBosm.get(key)).setVirtualorg(UFBoolean.TRUE);
      }
    }
  }

  //����Ԥ��ͳ����ϵ��Ա
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
      //������һ��һ��ֻ��һ����,����ֱ�Ӳ�,��Ӱ��Ч��
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

  //����Ԥ����֯
  private void enableBudget(BudgetOrgStruMemberVO[] vos)
      throws BusinessException {
    //��ѯ"�Ƿ��Զ�����"����ֵ�����ݸò���ֵ�������Ƿ�������÷���
    String usedorgcode =
        OrgTypeManager.getInstance().getOrgTypeByFullclassname(
            PlanBudgetVO.class.getName()).getUsedparacode();
    UFBoolean paramvalue =
        SysInitQuery.getParaBoolean(InvocationInfoProxy.getInstance()
            .getGroupId(), usedorgcode);
    if (UFBoolean.TRUE.equals(paramvalue)) {
      //���ź�ҵ��Ԫ������Ҫ�ֿ�
      List<List<SuperVO>> bfsBudgetStru =
          bfsClassify(buMapping.values().toArray(new OrgVO[0]),
              OrgVO.PK_FATHERORG);
      List<List<SuperVO>> bfsDeptBudgetStru =
          bfsClassify(deptMapping.values().toArray(new DeptVO[0]),
              DeptVO.PK_FATHERORG);
      List<List<SuperVO>> allBudgetStru = new ArrayList<List<SuperVO>>();
      allBudgetStru.addAll(bfsBudgetStru);
      allBudgetStru.addAll(bfsDeptBudgetStru);
      //��ҵ��Ԫ���������ԭ�����,Ȼ��Ԥ��ְ�ܰ����������˳���������,��ΪԤ����֯�����������¼��ṹ����
      //�����˳���������,��������ϼ�δ�������
      for (List<SuperVO> ls : allBudgetStru) {
        List<PlanBudgetVO> tempBudgets = new ArrayList<PlanBudgetVO>();
        for (SuperVO vo : ls) {
          //�������Ԥ��ְ��,δ�������������
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

  /**����Ϊ����,�ӿ�,�͹���*/

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

  //Ŀǰ����Ԥ��û����ҪУ�������,���Բ�����κ�У��
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

  //�̳���������������,��������һЩ����Ҫ��У������Ψһ�Թ����,�����Ը���һЩ��������
  //����û�и�Ϊ��̬�ڲ���������ԭ��
  //1.�ڲ����뾲̬�ڲ������,ֻ��������һ������������,���Ҳ��ܽ���Ƕ��,�ڳ�ʼ���������������ϵ�Ч��û������,�������Ҳ�������ܵ�ƿ��,����޸Ĳ���������������ĸ���
  //2.�ڲ�����Է��������ĳ�Ա�����ͳ�Ա����,����̬�ڲ��಻��,������Ҫʹ�ò��������ṩ�Ĺ��߷���(getVOListByPk)�������˵,ֻ��ѡ���ڲ���
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

    //û�б�ҪУ��ܿ�ģʽ,��Ϊ���벻���޸ı�������
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

  //����VO��ȡ�汾VO����
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

  //����PK��ȡVO����
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

  //���ݹ������ԭ����๤��
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

  //���ð汾��Ϣ
  private void setVOVersionInfo(SuperVO vo) throws BusinessException{
	BDMultiLangUtil.setMultiLangValues(vo, IOrgVersionConst.VNAME, "org", "0org0284"/*��ʼ�汾*/);
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
