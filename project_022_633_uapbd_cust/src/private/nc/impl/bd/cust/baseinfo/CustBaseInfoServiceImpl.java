package nc.impl.bd.cust.baseinfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.bd.baseservice.md.SingleBaseService;
import nc.bs.bd.baseservice.md.VOArrayUtil;
import nc.bs.bd.baseservice.validator.RefPkExistsValidator;
import nc.bs.bd.batchupdate.BatchUpdateService;
import nc.bs.bd.cache.CacheProxy;
import nc.bs.bd.cust.baseinfo.validator.CustClassDisableValidator;
import nc.bs.bd.cust.baseinfo.validator.CustLinkmanValidator;
import nc.bs.bd.cust.baseinfo.validator.CustSupFinanceorgValidator;
import nc.bs.bd.cust.baseinfo.validator.CustSupNotDelValidator;
import nc.bs.bd.cust.baseinfo.validator.CustVatValidator;
import nc.bs.bd.cust.baseinfo.validator.CustomerUpdateLoopValidator;
import nc.bs.bd.cust.baseinfo.validator.CusttaxtypesValidator;
import nc.bs.bd.cust.baseinfo.validator.FreeCustValidator;
import nc.bs.bd.cust.baseinfo.validator.IntCustomerValidator;
import nc.bs.bd.cust.upgrade.validator.CustUpgradeRefNullValidator;
import nc.bs.bd.cust.upgrade.validator.CustUpgradeValidator;
import nc.bs.bd.pub.ansy.ReallyTread;
import nc.bs.bd.service.ErrLogElement;
import nc.bs.bd.service.ValueObjWithErrLog;
import nc.bs.bd.upgrade.util.BDUpgradeUtil;
import nc.bs.businessevent.EventDispatcher;
import nc.bs.businessevent.IEventType;
import nc.bs.businessevent.bd.BDCommonEvent;
import nc.bs.businessevent.bd.BDCommonEventUtil;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.core.service.TimeService;
import nc.bs.logging.Logger;
import nc.bs.ls.TaskConfigBuilder;
import nc.bs.uif2.validation.IValidationService;
import nc.bs.uif2.validation.NullValueValidator;
import nc.bs.uif2.validation.ValidationException;
import nc.bs.uif2.validation.ValidationFrameworkUtil;
import nc.bs.uif2.validation.Validator;
import nc.impl.bd.cust.assign.CustAssignServiceImpl;
import nc.itf.bd.config.uniquerule.UniqueRuleConst;
import nc.itf.bd.cust.baseinfo.ICustBaseInfoQueryService;
import nc.itf.bd.cust.baseinfo.ICustBaseInfoService;
import nc.itf.bd.cust.baseinfo.ICustSupQueryService;
import nc.itf.bd.cust.baseinfo.ICustSupplierService;
import nc.itf.bd.pub.IBDMetaDataIDConst;
import nc.itf.ls.ILightScheduler;
import nc.itf.org.IOrgConst;
import nc.itf.org.IOrgUnitQryService;
import nc.md.persist.framework.IMDPersistenceService;
import nc.md.persist.framework.MDPersistenceService;
import nc.pub.billcode.itf.IBillcodeManage;
import nc.pub.billcode.vo.BillCodeContext;
import nc.vo.bd.ansylog.AnsyDelLogVO;
import nc.vo.bd.cust.CustSupBusiException;
import nc.vo.bd.cust.CustSupplierVO;
import nc.vo.bd.cust.CustomerVO;
import nc.vo.bd.cust.ICustConst;
import nc.vo.bd.cust.upgrade.CustUpgradeVO;
import nc.vo.bd.errorlog.ErrLogReturnValue;
import nc.vo.bd.errorlog.ErrorLogUtil;
import nc.vo.bd.pub.DistributedAddBaseValidator;
import nc.vo.bd.pub.IPubEnumConst;
import nc.vo.bd.pub.MultiDistributedUpdateValidator;
import nc.vo.bd.supplier.SupplierVO;
import nc.vo.ls.TaskConfig;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.trade.sqlutil.IInSqlBatchCallBack;
import nc.vo.trade.sqlutil.InSqlBatchCaller;
import nc.vo.trade.voutils.VOUtil;
import nc.vo.util.AuditInfoUtil;
import nc.vo.util.BDPKLockUtil;
import nc.vo.util.BDUniqueRuleValidate;
import nc.vo.util.BDVersionValidationUtil;
import nc.vo.util.bizlock.BizlockDataUtil;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 客户后台服务实现
 * 
 * @author jiangjuna
 * @modifier lixa1
 * @sinnc NC6.0
 */
public class CustBaseInfoServiceImpl extends SingleBaseService<CustomerVO>
        implements ICustBaseInfoService {

    private BaseDAO baseDAO = null;

    private ICustBaseInfoQueryService baseQryService;

    private IBillcodeManage billcodeManage;// 单据编码管理服务

    private CustAssignServiceImpl custAssignService;

    private ICustBaseInfoQueryService custBaseInfoQueryService = null;

    private final CustStructureService custStructureService =
            new CustStructureService();// 客户结构的服务

    private IMDPersistenceService mdService;

    private ILightScheduler lightScheduler;

    public CustBaseInfoServiceImpl() {
        super(IBDMetaDataIDConst.CUSTOMER, new String[] {
            ICustConst.ATTR_CUST_CONTACTS, ICustConst.ATTR_CUST_CUSTVAT
        });
    }

    @Override
    public ErrLogReturnValue batchUpdateCustBaseByCondition(String attr,
            Map<String, Object> attr_valueMap, String[] permissionOrgs,
            String[] selectedOrgs, String condition) throws BusinessException {
        return this.getBatchUpdateService().batchUpdateByCondition(attr,
                attr_valueMap, permissionOrgs, selectedOrgs, condition);
    }

    @Override
    public ErrLogReturnValue batchUpdateCustBaseByPks(String attr,
            Map<String, Object> attr_valueMap, String[] permissionOrgs,
            String[] selectedOrgs, String[] selectedPKs, boolean isNeedReturnVOs)
            throws BusinessException {
        return this.getBatchUpdateService().batchUpdateByPks(attr,
                attr_valueMap, permissionOrgs, selectedOrgs, selectedPKs,
                isNeedReturnVOs);
    }

    /** ******************** 删除操作 ************************* */
    @Override
    public void deleteCustomerVO(CustomerVO vo) throws BusinessException {
        this.deleteVO(vo);

        // 自动编码退号
        BillCodeContext billCodeContext =
                this.getBillcodeManage().getBillCodeContext(
                        ICustConst.BILLCODE_CUSTOMER, vo.getPk_group(),
                        vo.getPk_org());
        // 存在编码规则时，退号
        if (billCodeContext != null) {
            this.getBillcodeManage().returnBillCodeOnDelete(
                    ICustConst.BILLCODE_CUSTOMER, vo.getPk_group(),
                    vo.getPk_org(), vo.getCode(), vo);
        }

    }

    /** ******************** 停用操作 ************************* */
    @Override
    public ValueObjWithErrLog disableCustBaseInfo(CustomerVO[] vos)
            throws BusinessException {
        ValueObjWithErrLog objWithErrLog = this.disableVO(vos);
        CustomerVO[] customerVOs =
                VOArrayUtil.convertToVOArray(CustomerVO.class,
                        objWithErrLog.getVos());
        this.disableCustSupplier(customerVOs);
        return objWithErrLog;
    }

    /** ******************** 启用操作 ************************* */
    @Override
    public ValueObjWithErrLog enableCustBaseInfo(CustomerVO[] vos)
            throws BusinessException {
        ValueObjWithErrLog objWithErrLog = this.enableVO(vos);
        dealWithAnsyLog(vos);
        CustomerVO[] customerVOs =
                VOArrayUtil.convertToVOArray(CustomerVO.class,
                        objWithErrLog.getVos());
        customerVOs = resetCustomerVos(customerVOs);
        CustBaseInfoServiceImpl.this.getBaseDAO().updateVOArray(
                customerVOs,
                new String[] {
                    CustomerVO.DELETESTATE, CustomerVO.DELPERSON,
                    CustomerVO.DELTIME
                });
        CustomerVO[] aferuupdatevos = getAfterUpdatevos(customerVOs);
        this.enableCustSupplier(aferuupdatevos);
        objWithErrLog.setVos(aferuupdatevos);
        return objWithErrLog;
    }

    private CustomerVO[] resetCustomerVos(CustomerVO[] vos) {
        if (vos != null && vos.length > 0) {
            for (int i = 0; i < vos.length; i++) {
                vos[i].setDeletestate(0);
                vos[i].setDelperson(null);
                vos[i].setDeltime(null);
            }

        }
        return vos;
    }

    private void dealWithAnsyLog(CustomerVO[] vos) throws BusinessException {
        List<String> list =
                VOUtil.extractFieldValues(vos, CustomerVO.PK_CUSTOMER, null);
        InSqlBatchCaller caller =
                new InSqlBatchCaller(list.toArray(new String[0]));
        try {
            List<AnsyDelLogVO> ansylist =
                    (List<AnsyDelLogVO>) caller
                            .execute(new IInSqlBatchCallBack() {
                                List<AnsyDelLogVO> list =
                                        new ArrayList<AnsyDelLogVO>();

                                @Override
                                public Object doWithInSql(String inSql)
                                        throws BusinessException, SQLException {
                                    Collection<AnsyDelLogVO> col =
                                            CustBaseInfoServiceImpl.this
                                                    .getBaseDAO()
                                                    .retrieveByClause(
                                                            AnsyDelLogVO.class,
                                                            AnsyDelLogVO.PK_BASDOC
                                                                    + " in "
                                                                    + inSql);
                                    if (col != null && col.size() > 0) {
                                        this.list.addAll(col);
                                    }
                                    return this.list;
                                }
                            });
            CustBaseInfoServiceImpl.this.getBaseDAO().deleteVOList(ansylist);
        }

        catch (SQLException e) {
            Logger.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage(), e);
        }

    }

    /** ******************** 冻结操作 ************************* */
    @Override
    public ValueObjWithErrLog freezeCustBaseInfo(CustomerVO[] vos)
            throws BusinessException {
        ValueObjWithErrLog returnValue = null;
        CustomerVO[] returnVOs = null;

        if (vos == null || vos.length == 0
                || StringUtils.isBlank(CustomerVO.FROZENFLAG)) {
            return returnValue;
        }

        // LiFIXME: 数据权限校验

        // 加技术锁（主键锁）
        BDPKLockUtil.lockSuperVO(vos);

        // 版本校验（时间戳校验）
        BDVersionValidationUtil.validateSuperVO(vos);
        // 分布式校验
        // 业务校验逻辑
        validateCustomerForDist(vos);
        // 获取更新前的OldVO
        // 业务校验
        returnValue = this.filterCanOperateVO(vos, CustomerVO.FROZENFLAG);
        SuperVO[] superVOs = returnValue.getVos();
        if (superVOs != null && superVOs.length > 0) {
            CustomerVO[] canOperateVOs = new CustomerVO[superVOs.length];
            for (int i = 0; i < superVOs.length; i++) {
                canOperateVOs[i] = (CustomerVO) superVOs[i];
            }

            // 事件前批量通知
            BDCommonEvent beforeEvent =
                    new BDCommonEvent(this.getMDId(),
                            IEventType.TYPE_FREEZE_BEFORE,
                            (Object[]) canOperateVOs);
            EventDispatcher.fireEvent(beforeEvent);

            // 数据保存到数据库
            for (CustomerVO customerVO : canOperateVOs) {
                customerVO.setAttributeValue(CustomerVO.FROZENFLAG,
                        UFBoolean.TRUE); // 设置正向状态
                AuditInfoUtil.updateData(customerVO); // 设置审计信息
            }

            this.getBaseDAO().updateVOArray(
                    canOperateVOs,
                    new String[] {
                        CustomerVO.FROZENFLAG, CustomerVO.MODIFIER,
                        CustomerVO.MODIFIEDTIME
                    }); // 变更状态

            // 缓存通知
            CacheProxy.fireDataUpdated(CustomerVO.getDefaultTableName(), null);

            // 检索已被封存的VO
            returnVOs = this.retrieveVOs(canOperateVOs);

            // 事件后通知
            BDCommonEvent afterEvent =
                    new BDCommonEvent(this.getMDId(),
                            IEventType.TYPE_FREEZE_AFTER, (Object[]) returnVOs);
            EventDispatcher.fireEvent(afterEvent);

            // （业务日志）

            this.getBusiLogUtil().writeBusiLog("freeze", null, returnVOs);
            // 拼装反馈信息
            returnValue.setVos(returnVOs);
        }

        return returnValue;
    }

    private void validateCustomerForDist(CustomerVO[] vos)
            throws BusinessException {
        // 业务校验逻辑
        IValidationService validateService =
                ValidationFrameworkUtil
                        .createValidationService(new MultiDistributedUpdateValidator());
        validateService.validate(vos);
    }

    @Override
    public CustomerVO insertCustomer_RequiresNew(CustomerVO vo,
            boolean forceSave) throws BusinessException {
        return this.saveCustomer(vo, forceSave);
    }

    /**
     * ******************** 新增操作 *************************
     * modify by tangxx
     * 客户，供应商基础档案导入没法批量导入即是客户又是供应商的档案，客户的需求是批量导入客商（导入模板没有相关字段可选择），或者在导入后批量生成客商，
     */
    @Override
    public CustomerVO insertCustomerVO(CustomerVO vo, boolean forceSave)
            throws BusinessException {
        // 组织节点时，自动分配到对应的业务单元（自动产生该业务单元所具有职能的组织页签）
        if (this.isOrgData(vo)) {
            vo = this.saveCustomer(vo, forceSave);
            this.getCustAssignService().assignCustomerToSelfOrg(vo);
        }
        else {
            vo = this.saveCustomer(vo, forceSave);

        }
        String pk_supplier = vo.getPk_supplier();
        if (!StringUtils.isBlank(pk_supplier)) {
            getCustSupService().relaSupToCust(pk_supplier, vo);
        }
        return vo;
    }

    @Override
    public CustomerVO pfxxInsertCustomerVO(CustomerVO vo, boolean forceSave)
            throws BusinessException {
        CustomerVO newVO = this.insertCustomerVO(vo, forceSave);
        vo.setPrimaryKey(newVO.getPrimaryKey());
        this.getCustAssignService().assignCustomerWithData(vo);
        return newVO;
    }

    @Override
    public CustomerVO pfxxUpdateCustomerVO(CustomerVO vo, boolean forceSave)
            throws BusinessException {
        CustomerVO updatedVO = this.updateCustomerVO(vo, forceSave);
        this.getCustAssignService().cancelAssignCustomer(vo);
        this.getCustAssignService().assignCustomerWithData(vo);
        return updatedVO;
    }

    /** ******************** 取消冻结操作 ************************* */
    @Override
    public ValueObjWithErrLog unFreezeCustBaseInfo(CustomerVO[] vos)
            throws BusinessException {
        ValueObjWithErrLog returnValue = null;
        CustomerVO[] returnVOs = null;

        if (vos == null || vos.length == 0
                || StringUtils.isBlank(CustomerVO.FROZENFLAG)) {
            return returnValue;
        }

        // LiFIXME: 数据权限校验

        // 加技术锁（主键锁）
        BDPKLockUtil.lockSuperVO(vos);

        // 版本校验（时间戳校验）
        BDVersionValidationUtil.validateSuperVO(vos);
        // 分布式校验
        validateCustomerForDist(vos);

        // 业务校验
        returnValue = this.filterCanUnOperate(vos, CustomerVO.FROZENFLAG);
        SuperVO[] superVOs = returnValue.getVos();
        if (superVOs != null && superVOs.length > 0) {
            CustomerVO[] canUnOperateVOs = new CustomerVO[superVOs.length];
            for (int i = 0; i < superVOs.length; i++) {
                canUnOperateVOs[i] = (CustomerVO) superVOs[i];
            }

            // 事件前批量通知
            BDCommonEvent beforeEvent =
                    new BDCommonEvent(this.getMDId(),
                            IEventType.TYPE_UNFREEZE_BEFORE,
                            (Object[]) canUnOperateVOs);
            EventDispatcher.fireEvent(beforeEvent);

            // 数据保存到数据库
            for (CustomerVO customerVO : canUnOperateVOs) {
                customerVO.setAttributeValue(CustomerVO.FROZENFLAG,
                        UFBoolean.FALSE); // 设置逆向状态
                AuditInfoUtil.updateData(customerVO); // 设置审计信息
            }

            this.getBaseDAO().updateVOArray(
                    canUnOperateVOs,
                    new String[] {
                        CustomerVO.FROZENFLAG, CustomerVO.MODIFIER,
                        CustomerVO.MODIFIEDTIME
                    }); // 变更状态

            // 缓存通知
            CacheProxy.fireDataUpdated(CustomerVO.getDefaultTableName(), null);

            // 检索已被封存的VO
            returnVOs = this.retrieveVOs(canUnOperateVOs);

            // 事件后通知
            BDCommonEvent afterEvent =
                    new BDCommonEvent(this.getMDId(),
                            IEventType.TYPE_UNFREEZE_AFTER,
                            (Object[]) returnVOs);
            EventDispatcher.fireEvent(afterEvent);

            // （业务日志）
            this.getBusiLogUtil().writeBusiLog("unfreeze", null, returnVOs);
            // 拼装反馈信息
            returnValue.setVos(returnVOs);
        }

        return returnValue;
    }

    /** ******************** 修改操作 ************************* */
    @Override
    public CustomerVO updateCustomerVO(CustomerVO vo, boolean forceSave)
            throws BusinessException {
        if (vo == null) {
            return vo;
        }

        // 对唯一性字段进行trim处理
        getUniqueFieldTrimUtil().trimUniqueFields(vo);

        // 更新时的加锁操作
        this.updatelockOperate(vo);

        // 校验版本
        BDVersionValidationUtil.validateSuperVO(vo);

        // 获取更新前的OldVOs
        CustomerVO oldVO = this.retrieveVO(vo.getPrimaryKey());

        // 业务校验逻辑
        this.updateValidateVO(oldVO, vo);
        // 撞数据校验
        this.relationSupCheck(vo, forceSave);

        // 设置审计信息
        this.setUpdateAuditInfo(vo);

        // 更新前事件处理
        this.fireBeforeUpdateEvent(oldVO, vo);

        // 同步供应商及客户数据
        this.synchronizeCustInfoForUpdate(vo, oldVO);

        // 库操作
        this.dbUpdateVO(vo);

        // 同步客户层次关系
        this.custStructureService.synchronizeCustStructureForUpdate(vo, oldVO);

        // 更新缓存
        this.notifyVersionChangeWhenDataUpdated(vo);

        // 重新检索出新数据
        vo = this.retrieveVO(vo.getPrimaryKey());

        // 更新后事件通知
        this.fireAfterUpdateEvent(oldVO, vo);

        // 业务日志
        this.writeUpdatedBusiLog(oldVO, vo);

        return vo;
    }

    @Override
    public CustomerVO upgradeCustomer(CustomerVO upgradeVO)
            throws BusinessException {
        if (upgradeVO == null) {
            return null;
        }

        // 对唯一性字段进行trim处理
        getUniqueFieldTrimUtil().trimUniqueFields(upgradeVO);
        // 主键乐观锁
        BDPKLockUtil.lockSuperVO(upgradeVO);
        // 业务锁
        BizlockDataUtil.lockDataByBizlock(upgradeVO);

        // 版本校验
        BDVersionValidationUtil.validateSuperVO(upgradeVO);

        // 获取更新前的OldVO
        CustomerVO oldVO = this.retrieveVO(upgradeVO.getPrimaryKey());

        // 业务校验逻辑
        IValidationService validateService =
                ValidationFrameworkUtil.createValidationService(this
                        .getUpgradeValidator(oldVO));
        validateService.validate(upgradeVO);

        // 设置审计信息
        this.setUpdateAuditInfo(upgradeVO);

        // 升级前事件通知
        EventDispatcher.fireEvent(new BDCommonEvent(
                IBDMetaDataIDConst.CUSTOMER, IEventType.TYPE_UPGRADE_BEFORE,
                new CustomerVO[] {
                    oldVO
                }, new CustomerVO[] {
                    upgradeVO
                }));

        // 同步供应商及客户数据
        this.synchronizeCustInfoForUpdate(upgradeVO, oldVO);

        // 库操作
        this.dbUpdateVO(upgradeVO);

        // 同步客户层次关系
        this.custStructureService.synchronizeCustStructureForUpdate(upgradeVO,
                oldVO);

        // 新增供应商升级记录
        this.insertUpgradeInfo(this.getCustUpgradeVO(upgradeVO, oldVO));

        // 更新缓存
        this.notifyVersionChangeWhenDataUpdated(upgradeVO);

        // 重新检索出新数据
        upgradeVO = this.retrieveVO(upgradeVO.getPrimaryKey());
        // 业务日志
        this.getBusiLogUtil().writeBusiLog("update", null, upgradeVO);

        // 升级后事件通知
        EventDispatcher.fireEvent(new BDCommonEvent(
                IBDMetaDataIDConst.CUSTOMER, IEventType.TYPE_UPGRADE_AFTER,
                new Object[]{oldVO}, new Object[]{upgradeVO}));
        return upgradeVO;
    }

    @Override
    public ErrLogReturnValue upgradeCustomerBatch(String[] pks, String pk_org,
            String pk_group) throws BusinessException {
        if (pks == null || pks.length == 0) {
            return null;
        }
        // 加锁
        BDPKLockUtil.lockString(pks);

        // 查询要升级的数据
        CustomerVO[] vos = this.retrieveVOs(pks);

        // 对唯一性字段进行trim处理
        getUniqueFieldTrimUtil().trimUniqueFields(vos);

        if (vos == null || vos.length == 0) {
            return null;
        }

        int totalNum = vos.length;
        Map<String, CustomerVO> pk_oldVO_map =
                new HashMap<String, CustomerVO>();
        for (CustomerVO vo : vos) {
            pk_oldVO_map.put(vo.getPrimaryKey(), (CustomerVO) vo.clone());
        }

        // 准备升级数据（过滤不需升级的数据，及重置升级数据的值）
        List<CustomerVO> needUpgradeVOList =
                this.prepareUpdateData(vos, pk_org, pk_group);

        // 升级前校验
        String pk_user = InvocationInfoProxy.getInstance().getUserId();
        ErrorLogUtil util =
                new ErrorLogUtil(IBDMetaDataIDConst.CUSTOMER, pk_user,
                        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
                                "10140cub", "010140cub0032")/* @res "升级" */,
                        true);
        List<CustomerVO> toUpgradeVOList =
                this.validateUpgradeBatch(needUpgradeVOList, util);

        if (toUpgradeVOList == null || toUpgradeVOList.isEmpty()) {
            return util.getErrLogReturnValue(null, totalNum);
        }

        CustomerVO[] newVOs = toUpgradeVOList.toArray(new CustomerVO[0]);
        CustomerVO[] oldVOs = new CustomerVO[newVOs.length];
        for (int i = 0; i < oldVOs.length; i++) {
            oldVOs[i] = pk_oldVO_map.get(newVOs[i].getPrimaryKey());
        }

        AuditInfoUtil.updateData(newVOs);

        EventDispatcher.fireEvent(new BDCommonEvent(
                IBDMetaDataIDConst.CUSTOMER, IEventType.TYPE_UPGRADE_BEFORE,
                oldVOs, newVOs));

        this.doUpdateBatch(newVOs, oldVOs);

        CustUpgradeVO[] upgradeVOs = new CustUpgradeVO[newVOs.length];
        for (int i = 0; i < newVOs.length; i++) {
            upgradeVOs[i] = this.getCustUpgradeVO(newVOs[i], oldVOs[i]);
        }

        this.insertUpgradeInfo(upgradeVOs);

        CacheProxy.fireDataUpdated(upgradeVOs[0].getTableName(), null);

        newVOs = this.retrieveVOs(newVOs);

        EventDispatcher.fireEvent(new BDCommonEvent(
                IBDMetaDataIDConst.CUSTOMER, IEventType.TYPE_UPGRADE_AFTER,
                oldVOs, newVOs));

        return util.getErrLogReturnValue(null, totalNum);
    }

    @Override
    protected void dbDeleteVO(CustomerVO vo) throws BusinessException {
        this.getCustAssignService().cancelAssignCustomer(vo);
        // 同步客户层次关系
        this.custStructureService.synchronizeCustStructureForDelete(vo);

        super.dbDeleteVO(vo);

        // 删除供应商在客商表中的记录
        this.getCustSupService().deleteCustSupVOByPK(vo.getPrimaryKey());
    }

    @Override
    protected Validator[] getDeleteValidator() {
        CustSupNotDelValidator custSupNotDelValidator =
                new CustSupNotDelValidator();
        List<Validator> list = new ArrayList<Validator>();
        list.add(custSupNotDelValidator);
        list.addAll(Arrays.asList(super.getDeleteValidator()));
        return list.toArray(new Validator[0]);
    }

    @Override
    protected Validator[] getInsertValidator() {
        // 非空校验
        NullValueValidator notNullValidator =
                NullValueValidator.createMDNullValueValidator(CustomerVO.class
                        .getName(), Arrays.asList(CustomerVO.PK_GROUP,
                        CustomerVO.PK_ORG, CustomerVO.CODE, CustomerVO.NAME,
                        CustomerVO.ISSUPPLIER, CustomerVO.PK_CUSTCLASS,
                        CustomerVO.ENABLESTATE));
        // 散户校验
        FreeCustValidator freeCustValidator = new FreeCustValidator();
        // 联系人校验
        CustLinkmanValidator linkmanValidator = new CustLinkmanValidator(null);
        // 客户分类封存的校验
        CustClassDisableValidator custClassDisableValidator =
                new CustClassDisableValidator();
        // 内部客户校验
        IntCustomerValidator intCustomerValidator =
                new IntCustomerValidator(null);
        // 对应财务组织唯一校验
        CustSupFinanceorgValidator custSupFinanceorgValidator =
                new CustSupFinanceorgValidator();
        // 客户国家税类唯一性校验
        CusttaxtypesValidator custtaxtypesvalidator =
                new CusttaxtypesValidator(null);

        // 客户VAT唯一性校验
        CustVatValidator custvatvalidator = new CustVatValidator(null);

        // 分类存在校验
        RefPkExistsValidator refPkExistsValidator =
                new RefPkExistsValidator(CustomerVO.PK_CUSTCLASS);
        List<Validator> list = new ArrayList<Validator>();
        list.addAll(Arrays.asList(super.getInsertValidator()));
        list.addAll(Arrays.asList(new Validator[] {
            notNullValidator, freeCustValidator, linkmanValidator,
            custClassDisableValidator, intCustomerValidator,
            custSupFinanceorgValidator, custtaxtypesvalidator,
            custvatvalidator, refPkExistsValidator
        }));
        return list.toArray(new Validator[0]);
    }

    protected IMDPersistenceService getMDService() {
        if (this.mdService == null) {
            this.mdService = MDPersistenceService.lookupPersistenceService();
        }
        return this.mdService;
    }

    @Override
    protected Validator[] getUpdateValidator(CustomerVO oldVO) {
        // 非空校验
        NullValueValidator notNullValidator =
                NullValueValidator.createMDNullValueValidator(CustomerVO.class
                        .getName(), Arrays.asList(CustomerVO.PK_GROUP,
                        CustomerVO.PK_ORG, CustomerVO.CODE, CustomerVO.NAME,
                        CustomerVO.ISSUPPLIER, CustomerVO.PK_CUSTCLASS,
                        CustomerVO.ENABLESTATE));
        // 散户校验
        FreeCustValidator freeCustValidator = new FreeCustValidator();
        // 联系人校验
        CustLinkmanValidator linkmanValidator = new CustLinkmanValidator(oldVO);
        // 唯一性校验
        // BDUniqueRuleValidate uniqueValidator = new BDUniqueRuleValidate();
        // 上级客户循环引用校验
        CustomerUpdateLoopValidator loopValidator =
                new CustomerUpdateLoopValidator(oldVO);
        // 内部客户校验
        IntCustomerValidator intCustomerValidator =
                new IntCustomerValidator(oldVO);
        // 对应财务组织唯一校验
        CustSupFinanceorgValidator custSupFinanceorgValidator =
                new CustSupFinanceorgValidator();

        // 客户国家税类国家唯一性
        CusttaxtypesValidator custtaxtypesvalidator =
                new CusttaxtypesValidator(oldVO);

        // 客户VAT唯一性校验
        CustVatValidator custvatvalidator = new CustVatValidator(oldVO);

        // 分类存在校验
        RefPkExistsValidator refPkExistsValidator =
                new RefPkExistsValidator(CustomerVO.PK_CUSTCLASS);
        List<Validator> list = new ArrayList<Validator>();
        list.addAll(Arrays.asList(new Validator[] {
            notNullValidator, freeCustValidator, linkmanValidator,
            loopValidator, intCustomerValidator, custSupFinanceorgValidator,
            custtaxtypesvalidator, custvatvalidator, refPkExistsValidator
        }));
        list.addAll(Arrays.asList(super.getUpdateValidator(oldVO)));
        return list.toArray(new Validator[0]);
    }

    protected Validator[] getUpgradeValidator(CustomerVO oldVO) {
        // 非空校验
        NullValueValidator notNullValidator =
                NullValueValidator.createMDNullValueValidator(CustomerVO.class
                        .getName(), Arrays.asList(CustomerVO.PK_GROUP,
                        CustomerVO.PK_ORG, CustomerVO.CODE, CustomerVO.NAME,
                        CustomerVO.ISSUPPLIER, CustomerVO.PK_CUSTCLASS,
                        CustomerVO.ENABLESTATE));
        // 升级校验
        CustUpgradeValidator upgradeValidator = new CustUpgradeValidator();
        // 散户校验
        FreeCustValidator freeCustValidator = new FreeCustValidator();
        // 联系人校验
        CustLinkmanValidator linkmanValidator = new CustLinkmanValidator(oldVO);
        // 唯一性校验
        BDUniqueRuleValidate uniqueValidator = new BDUniqueRuleValidate();
        // 上级客户循环引用校验
        CustomerUpdateLoopValidator loopValidator =
                new CustomerUpdateLoopValidator(oldVO);
        // 内部客户校验
        IntCustomerValidator intCustomerValidator =
                new IntCustomerValidator(oldVO);
        // 对应财务组织唯一校验
        CustSupFinanceorgValidator custSupFinanceorgValidator =
                new CustSupFinanceorgValidator();
        DistributedAddBaseValidator disValidator =
                new DistributedAddBaseValidator();
        return new Validator[] {
            notNullValidator, upgradeValidator, freeCustValidator,
            linkmanValidator, uniqueValidator, loopValidator,
            intCustomerValidator, custSupFinanceorgValidator, disValidator
        };
    }

    @Override
    protected CustomerVO retrieveVO(String pk) throws BusinessException {
        CustomerVO[] vos = this.retrieveVOs(new String[] {
            pk
        });
        return vos != null && vos.length > 0 ? vos[0] : null;
    }

    @SuppressWarnings("unchecked")
    protected CustomerVO[] retrieveVOs(CustomerVO[] customerVOs)
            throws BusinessException {
        if (customerVOs == null || customerVOs.length == 0) {
            return null;
        }
        List<String> pkList =
                VOUtil.extractFieldValues(customerVOs, CustomerVO.PK_CUSTOMER,
                        null);
        return this.retrieveVOs(pkList.toArray(new String[pkList.size()]));
    }

    protected CustomerVO[] retrieveVOs(String[] pks) throws BusinessException {
        return this.getCustBaseInfoQueryService().queryDataByPkSet(pks);
    }

    private void checkCustSupplierCodeUnique(CustomerVO vo)
            throws BusinessException {
        CustSupplierVO newCustSupVO = new CustSupplierVO(vo);
        // 加锁
        BizlockDataUtil.lockDataByBizlock(newCustSupVO);
        // 校验编码唯一性
        IValidationService validateService =
                ValidationFrameworkUtil
                        .createValidationService(new Validator[] {
                            new BDUniqueRuleValidate()
                        });
        validateService.validate(newCustSupVO);
    }

    private void disableCustSupplier(CustomerVO[] customerVOs)
            throws BusinessException {
        if (ArrayUtils.isEmpty(customerVOs)) {
            return;
        }
        String[] pks = VOArrayUtil.getPrimaryKeyArray(customerVOs);
        CustSupplierVO[] custSupplierVO =
                this.getCustSupQrySer().queryCustSupVO(pks);
        this.getCustSupService().synDisableState(custSupplierVO,
                CustSupplierVO.CUSTENABLESTATE);
    }

    private void doUpdateBatch(CustomerVO[] vos, CustomerVO[] oldVOs)
            throws DAOException, BusinessException {
        // 同步供应商及客户数据
        for (int i = 0; i < vos.length; i++) {
            this.synchronizeCustInfoForUpdate(vos[i], oldVOs[i]);
        }

        // 库操作
        this.getBaseDAO().updateVOArray(vos);

        // 同步客户层次关系
        for (int i = 0; i < vos.length; i++) {
            this.custStructureService.synchronizeCustStructureForUpdate(vos[i],
                    oldVOs[i]);
        }
    }

    private void enableCustSupplier(CustomerVO[] customerVOs)
            throws BusinessException {
        if (ArrayUtils.isEmpty(customerVOs)) {
            return;
        }
        String[] pks = VOArrayUtil.getPrimaryKeyArray(customerVOs);
        CustSupplierVO[] custSupplierVO =
                this.getCustSupQrySer().queryCustSupVO(pks);
        this.getCustSupService().synEnableState(custSupplierVO,
                CustSupplierVO.CUSTENABLESTATE);
    }

    private ValueObjWithErrLog filterCanOperateVO(CustomerVO[] vos,
            String attributeName) {
        ValueObjWithErrLog returnWithErrLog = new ValueObjWithErrLog();
        if (vos != null && vos.length > 0) {
            ArrayList<ErrLogElement> errLogList =
                    new ArrayList<ErrLogElement>();
            ArrayList<CustomerVO> canOperateVOList =
                    new ArrayList<CustomerVO>();

            for (CustomerVO vo : vos) {
                ErrLogElement errLogElement =
                        this.validateCanOperate(vo, attributeName);
                if (errLogElement == null) {
                    // 当前VO可以变更状态
                    canOperateVOList.add(vo);
                }
                else {
                    // 当前VO不可以变更状态
                    errLogList.add(errLogElement);
                }
            }

            returnWithErrLog.setErrLogList(errLogList);
            returnWithErrLog.setVos(canOperateVOList
                    .toArray(new CustomerVO[canOperateVOList.size()]));
        }
        return returnWithErrLog;
    }

    private ValueObjWithErrLog filterCanUnOperate(CustomerVO[] vos,
            String attributeName) {
        ValueObjWithErrLog returnWithErrLog = new ValueObjWithErrLog();

        if (vos != null && vos.length > 0) {
            ArrayList<ErrLogElement> errLogList =
                    new ArrayList<ErrLogElement>();
            ArrayList<CustomerVO> canUnOperateVOList =
                    new ArrayList<CustomerVO>();
            ErrLogElement errLogElement = null;

            for (CustomerVO vo : vos) {
                errLogElement = this.validateCanUnOperate(vo, attributeName);
                if (errLogElement == null) {
                    // 当前VO可以逆向操作
                    canUnOperateVOList.add(vo);
                }
                else {
                    // 当前VO不可以逆向操作
                    errLogList.add(errLogElement);
                }
            }

            returnWithErrLog.setErrLogList(errLogList);
            returnWithErrLog.setVos(canUnOperateVOList
                    .toArray(new CustomerVO[canUnOperateVOList.size()]));
        }

        return returnWithErrLog;
    }

    private BaseDAO getBaseDAO() {
        if (this.baseDAO == null) {
            this.baseDAO = new BaseDAO();
        }
        return this.baseDAO;
    }

    private ICustBaseInfoQueryService getBaseQryService() {
        if (this.baseQryService == null) {
            this.baseQryService =
                    NCLocator.getInstance().lookup(
                            ICustBaseInfoQueryService.class);
        }
        return this.baseQryService;
    }

    private BatchUpdateService<CustomerVO> getBatchUpdateService() {
        return new CustBaseInfoBatchUpdateServiceImpl();
    }

    private Validator[] getBatchUpgradeValidator() {

        CustUpgradeRefNullValidator reNullValidator =
                new CustUpgradeRefNullValidator();
        // 升级校验
        CustUpgradeValidator upgradeValidator = new CustUpgradeValidator();
        // 内部客户校验
        IntCustomerValidator intCustomerValidator =
                new IntCustomerValidator(null);
        // 唯一性校验
        BDUniqueRuleValidate uniqueValidator = new BDUniqueRuleValidate();
        DistributedAddBaseValidator distAddValidator =
                new DistributedAddBaseValidator();
        Validator[] vals =
                new Validator[] { /* notNullValidator, */
                    reNullValidator, upgradeValidator, intCustomerValidator,
                    uniqueValidator, distAddValidator
                };
        return vals;
    }

    private IBillcodeManage getBillcodeManage() {
        if (this.billcodeManage == null) {
            this.billcodeManage =
                    NCLocator.getInstance().lookup(IBillcodeManage.class);
        }
        return this.billcodeManage;
    }

    private CustAssignServiceImpl getCustAssignService() {
        if (this.custAssignService == null) {
            this.custAssignService = new CustAssignServiceImpl();
        }
        return this.custAssignService;
    }

    private ICustBaseInfoQueryService getCustBaseInfoQueryService() {
        if (this.custBaseInfoQueryService == null) {
            this.custBaseInfoQueryService = new CustBaseInfoQueryServiceImpl();
        }
        return this.custBaseInfoQueryService;
    }

    private ICustSupQueryService getCustSupQrySer() {
        return NCLocator.getInstance().lookup(ICustSupQueryService.class);
    }

    /** ******************** 生成客商 ************************* */
    private ICustSupplierService getCustSupService() {
        return NCLocator.getInstance().lookup(ICustSupplierService.class);
    }

    private CustUpgradeVO getCustUpgradeVO(CustomerVO upgradeVO,
            CustomerVO oldVO) {
        CustUpgradeVO insertVO = new CustUpgradeVO();
        insertVO.setSourceorg(oldVO.getPk_org());
        insertVO.setSourcecode(oldVO.getCode());
        insertVO.setSourcename(oldVO.getName());
        insertVO.setSourcecust(oldVO);
        insertVO.setDestcust(upgradeVO.getPrimaryKey());
        insertVO.setOperator(InvocationInfoProxy.getInstance().getUserId());
        insertVO.setOperationdate(TimeService.getInstance().getUFDateTime()
                .getDate());
        return insertVO;
    }

    private IOrgUnitQryService getOrgQrySer() {
        return NCLocator.getInstance().lookup(IOrgUnitQryService.class);
    }

    private CustUpgradeVO[] insertUpgradeInfo(CustUpgradeVO... vos)
            throws BusinessException {
        // 插入前事件通知
        BDCommonEventUtil eventUtil =
                new BDCommonEventUtil(IBDMetaDataIDConst.CUSTUPGRADE);
        eventUtil.dispatchInsertBeforeEvent((Object[]) vos);

        // 库操作
        String[] pks = this.getBaseDAO().insertVOArray(vos);

        // 重新检索出插入的VO
        vos = this.retrieveCustUpgradeVOByPks(pks);

        // 插入事件后通知
        eventUtil.dispatchInsertAfterEvent((Object[]) vos);

        CacheProxy.fireDataInserted(CustUpgradeVO.getDefaultTableName());

        // 业务日志
        this.getBusiLogUtil().writeBusiLog("update", null, vos);

        return vos;
    }

    private CustomerVO insertVO(CustomerVO vo, boolean forceSave)
            throws BusinessException {
        if (vo == null) {
            return vo;
        }

        // 对唯一性字段进行trim处理
        getUniqueFieldTrimUtil().trimUniqueFields(vo);

        // 新增时的加锁操作
        this.insertlockOperate(vo);
        // 逻辑校验
        this.insertValidateVO(vo);
        // 撞数据校验
        this.relationSupCheck(vo, forceSave);

        // 设置审计信息
        this.setInsertAuditInfo(vo);

        // 插入前事件通知
        this.fireBeforeInsertEvent(vo);

        // 客户银行子表由银行帐户的监听处理
        vo.setCustbanks(null);
        // 库操作
        String pk = this.dbInsertVO(vo);

        // 通知更新缓存
        this.notifyVersionChangeWhenDataInserted(vo);

        vo.setPrimaryKey(pk);

        // 同步客户层次关系
        this.custStructureService.synchronizeCustStructureForInsert(vo);

        // 新增时，同步客户的数据到客商表
        this.synchronizeCustInfoForInsert(vo);

        // 插入事件后通知
        this.fireAfterInsertEvent(vo);

        vo = this.retrieveVO(pk);

        // 业务日志
        this.writeInsertBusiLog(vo);

        return vo;
    }

    private CustomerVO insertWithAutoCode(CustomerVO vo, boolean forceSave,
            BillCodeContext billCodeContext) throws BusinessException {
        boolean sucessed = false;
        while (!sucessed) {
            try {
                if (billCodeContext != null && !billCodeContext.isPrecode()) {
                    String aBillCode =
                            this.getBillcodeManage().getBillCode_RequiresNew(
                                    ICustConst.BILLCODE_CUSTOMER,
                                    vo.getPk_group(), vo.getPk_org(), vo);
                    vo.setCode(aBillCode);
                }
                this.checkCustSupplierCodeUnique(vo);
                vo = this.insertVO(vo, forceSave);
                sucessed = true;
                break;
            }
            catch (BusinessException e) {
                if (billCodeContext != null
                        && UniqueRuleConst.CODEBREAKUNIQUE.equals(e
                                .getErrorCodeString())) {
                    this.getBillcodeManage().AbandonBillCode_RequiresNew(
                            ICustConst.BILLCODE_CUSTOMER, vo.getPk_group(),
                            vo.getPk_org(), vo.getCode());
                    if (!billCodeContext.isPrecode()) {
                        continue;
                    }
                }
                throw e;
            }
        }
        return vo;
    }

    private boolean isOrgData(CustomerVO vo) {
        return !vo.getPk_group().equals(vo.getPk_org())
                && !vo.getPk_org().equals(IOrgConst.GLOBEORG);
    }

    private List<CustomerVO> prepareUpdateData(CustomerVO[] voList,
            String pk_org, String pk_group) throws BusinessException {

        String targetOrg =
                BDUpgradeUtil.getTargetOrgID(pk_org, pk_group,
                        IBDMetaDataIDConst.CUSTOMER);

        List<CustomerVO> needUpgradeVOList = new ArrayList<CustomerVO>();
        for (CustomerVO vo : voList) {
            if (vo.getPk_org().equals(pk_org)) {
                vo.setPk_org(targetOrg);
                needUpgradeVOList.add(vo);
            }
        }
        if (needUpgradeVOList.isEmpty()) {
            return null;
        }
        BDUpgradeUtil.filterRefValue(
                needUpgradeVOList.toArray(new CustomerVO[0]), targetOrg,
                pk_group, IBDMetaDataIDConst.CUSTOMER);
        return needUpgradeVOList;
    }

    // 客户与供应商关联校验
    private void relationSupCheck(CustomerVO vo, boolean forceSave)
            throws BusinessException {
        boolean isSupplier =
                vo.getIssupplier() == null ? false : vo.getIssupplier()
                        .booleanValue();
        boolean isFreecust =
                vo.getIsfreecust() == null ? false : vo.getIsfreecust()
                        .booleanValue();
        if (forceSave || isSupplier || isFreecust) {
            return;
        }
        SupplierVO[] supplierVOs =
                this.getCustSupQrySer().getSameTaxpayeridSuppliers(vo);
        if (supplierVOs != null && supplierVOs.length == 1) {
            CustSupBusiException be = new CustSupBusiException();
            be.setSameTaxpayidVO(supplierVOs[0]);
            OrgVO orgVO = this.getOrgQrySer().getOrg(vo.getPk_org());
            be.setOrgVO(orgVO);
            throw be;
        }
    }

    @SuppressWarnings("unchecked")
    private CustUpgradeVO[] retrieveCustUpgradeVOByPks(String[] pks)
            throws BusinessException {
        CustUpgradeVO[] vos = null;
        InSqlBatchCaller caller = new InSqlBatchCaller(pks);
        try {
            List<CustUpgradeVO> list =
                    (List<CustUpgradeVO>) caller
                            .execute(new IInSqlBatchCallBack() {
                                List<CustUpgradeVO> list =
                                        new ArrayList<CustUpgradeVO>();

                                @Override
                                public Object doWithInSql(String inSql)
                                        throws BusinessException, SQLException {
                                    Collection<CustUpgradeVO> col =
                                            CustBaseInfoServiceImpl.this
                                                    .getBaseDAO()
                                                    .retrieveByClause(
                                                            CustUpgradeVO.class,
                                                            CustUpgradeVO.PK_CUSTUPGRADE
                                                                    + " in "
                                                                    + inSql);
                                    if (col != null && col.size() > 0) {
                                        this.list.addAll(col);
                                    }
                                    return this.list;
                                }
                            });
            vos = list.toArray(new CustUpgradeVO[0]);
        }
        catch (SQLException e) {
            Logger.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage(), e);
        }
        return vos;
    }

    private CustomerVO saveCustomer(CustomerVO vo, boolean forceSave)
            throws BusinessException {
        BillCodeContext billCodeContext =
                this.getBillcodeManage().getBillCodeContext(
                        ICustConst.BILLCODE_CUSTOMER, vo.getPk_group(),
                        vo.getPk_org());

        vo = this.insertWithAutoCode(vo, forceSave, billCodeContext);
        if (billCodeContext != null && billCodeContext.isPrecode()) {
            this.getBillcodeManage().commitPreBillCode(
                    ICustConst.BILLCODE_CUSTOMER, vo.getPk_group(),
                    vo.getPk_org(), vo.getCode());
        }
        return vo;
    }

    /**
     * 同步客户到客商的数据
     * 
     * @param vo
     * @throws BusinessException
     */
    private void synchronizeCustInfoForInsert(CustomerVO vo)
            throws BusinessException {
        CustSupplierVO newCustSupVO = new CustSupplierVO(vo);
        if (!vo.getIssupplier().booleanValue()) {
            this.getCustSupService().insertCustSupVO(newCustSupVO);
        }
    }

    /**
     * 如果是客商档案，那么对客户的纳税人登记号的修改，需要同步到供应商和客商表上
     * 
     * @param vo
     * @throws BusinessException
     */
    private void synchronizeCustInfoForUpdate(CustomerVO vo, CustomerVO oldVO)
            throws BusinessException {
        boolean isSupplier =
                vo.getIssupplier() == null ? false : vo.getIssupplier()
                        .booleanValue();
        
        
        // 将客户的信息同步到客商表中
        CustSupplierVO custSupVO = this.getCustSupQrySer().queryCustSupVOByPk(vo.getPrimaryKey());
        
        //央客可开:王志强  at：2019-8-14
        //*******begin
        if (isSupplier) {

          synchronizeBaseInfo(vo, oldVO, custSupVO);

        } else {
          CustSupplierVO newcustSupVO = updateCustsupVOByCustVO(vo, custSupVO);
          this.getCustSupService().updateCustSupVO(newcustSupVO);
        }
        
        //******end替换了后续的代码
        
        // 若为客商，并且修改了对应财务组织，则需要同步到供应商
//        if (isSupplier) {
//            String old_financeorg = oldVO.getPk_financeorg();
//            String new_financeorg = vo.getPk_financeorg();
//            if (!StringUtils.equals(old_financeorg, new_financeorg)) {
//                SupplierVO supplierVO =
//                        (SupplierVO) this.getBaseDAO().retrieveByPK(
//                                SupplierVO.class, vo.getPrimaryKey());
//                supplierVO.setSupprop(vo.getCustprop());
//                supplierVO.setPk_financeorg(new_financeorg);
//                
//                //央客王志强 更改客商编码 at:2019-8-14
//                	
//                
//                //end
//                this.getCustSupService().synSupplierInfo(supplierVO);
//            }
//        }
//
//        // 将客户的信息同步到客商表中
//        CustSupplierVO custSupVO =
//                this.getCustSupQrySer().queryCustSupVOByPk(vo.getPrimaryKey());
//        custSupVO.setCode(vo.getCode());
//        custSupVO.setName(vo.getName());
//        custSupVO.setName2(vo.getName2());
//        custSupVO.setName3(vo.getName3());
//        custSupVO.setName4(vo.getName4());
//        custSupVO.setName5(vo.getName5());
//        custSupVO.setName6(vo.getName6());
//        custSupVO.setPk_areacl(vo.getPk_areacl());
//        custSupVO.setTaxpayerid(vo.getTaxpayerid());
//        custSupVO.setCustsupprop(vo.getCustprop());
//        custSupVO.setPk_financeorg(vo.getPk_financeorg());
//        custSupVO.setPk_org(vo.getPk_org());
//        custSupVO.setPk_custclass(vo.getPk_custclass());
//        this.getCustSupService().updateCustSupVO(custSupVO);
    }
    
    //使用65
    private CustSupplierVO updateCustsupVOByCustVO(CustomerVO vo, CustSupplierVO custSupVO) {
        custSupVO.setCode(vo.getCode());
        custSupVO.setName(vo.getName());
        custSupVO.setName2(vo.getName2());
        custSupVO.setName3(vo.getName3());
        custSupVO.setName4(vo.getName4());
        custSupVO.setName5(vo.getName5());
        custSupVO.setName6(vo.getName6());
        custSupVO.setPk_areacl(vo.getPk_areacl());
        custSupVO.setTaxpayerid(vo.getTaxpayerid());
        custSupVO.setCustsupprop(vo.getCustprop());
        custSupVO.setPk_financeorg(vo.getPk_financeorg());
        custSupVO.setPk_org(vo.getPk_org());
        custSupVO.setPk_custclass(vo.getPk_custclass());
        return custSupVO;
      }
    
    
    
    /**
     * 使用65的代码 ，63没有
     * <p>
     * 说明：说明：客商基本信息属性同步 包括：名称、纳税人登记号、地区分类、助记码、简称
     * <li></li>
     * </p>
     * 
     * @param customerVO
     * @param oldCustomerVO
     * @param custSupVO
     * @throws BusinessException
     * @date 2014年11月28日 下午2:14:43
     * @since NC6.5
     */
    private void synchronizeBaseInfo(CustomerVO customerVO, CustomerVO oldCustomerVO,
        CustSupplierVO custSupVO) throws BusinessException {

      SupplierVO supplierVO =
          (SupplierVO) this.getBaseDAO().retrieveByPK(SupplierVO.class, customerVO.getPrimaryKey());
      // 若为客商，并且修改了对应财务组织，则需要同步到客户
      String old_financeorg = oldCustomerVO.getPk_financeorg();
      String new_financeorg = customerVO.getPk_financeorg();
      if (!StringUtils.equals(old_financeorg, new_financeorg)) {
        // 同步客户类型和对应财务组织
        supplierVO.setPk_financeorg(new_financeorg);
        supplierVO.setSupprop(customerVO.getCustprop());
        custSupVO.setPk_financeorg(new_financeorg);
        custSupVO.setCustsupprop(customerVO.getCustprop());
      }
      // 纳税人登记号
      String new_taxpayerid = customerVO.getTaxpayerid();
      String old_taxpayerid = oldCustomerVO.getTaxpayerid();
      if (!StringUtils.equals(new_taxpayerid, old_taxpayerid)) {
        supplierVO.setTaxpayerid(new_taxpayerid);
        custSupVO.setTaxpayerid(new_taxpayerid);
      }
      // 地区分类必同步
      String new_pkareaclass = customerVO.getPk_areacl();
      String old_pkareaclass = oldCustomerVO.getPk_areacl();
      if (!StringUtils.equals(new_pkareaclass, old_pkareaclass)) {
        supplierVO.setPk_areacl(new_pkareaclass);
        custSupVO.setPk_areacl(new_pkareaclass);
      }
      // 名称
      String new_name = customerVO.getName();
      String old_name = oldCustomerVO.getName();
      if (!StringUtils.equals(new_name, old_name)) {
        supplierVO.setName(new_name);
        custSupVO.setName(new_name);
      }

      // 编码
      String new_code = customerVO.getCode();
      String old_code = oldCustomerVO.getCode();
      if (!StringUtils.equals(new_code, old_code)) {
        custSupVO.setCode(new_code);
        supplierVO.setCode(new_code);
      }
      // 助记码
      String new_mnecode = customerVO.getMnecode();
      String old_mnecode = oldCustomerVO.getMnecode();
      if (!StringUtils.equals(new_mnecode, old_mnecode)) {
        supplierVO.setMnecode(new_mnecode);
       
      }
      // 简称
      String new_shortname = customerVO.getShortname();
      String old_shortname = oldCustomerVO.getShortname();
      if (!StringUtils.equals(new_shortname, old_shortname)) {
        supplierVO.setShortname(new_shortname);
         
      }

      // 客户基本分类 （added by litingk)
      String new_class = customerVO.getPk_custclass();
      String old_class = oldCustomerVO.getPk_custclass();
      if (!StringUtils.equals(new_class, old_class)) {
        custSupVO.setPk_custclass(new_class);
      }

      // 组织 （added by litingk)
      String new_org = customerVO.getPk_org();
      String old_org = oldCustomerVO.getPk_org();
      if (!StringUtils.equals(new_org, old_org)) {
        custSupVO.setPk_org(new_org);
        // 客户和供应商所属组织必须一致
        supplierVO.setPk_org(new_org);
      }

      // 更新供应商信息
      this.getCustSupService().synSupplierInfo(supplierVO);
      // 更新客商信息
      this.getCustSupService().updateCustSupVO(custSupVO);
    }

    /**
     * 正向操作校验逻辑
     * 
     * @param vo
     * @param attributeName
     * @return
     */
    private ErrLogElement validateCanOperate(CustomerVO vo, String attributeName) {
        ErrLogElement errLogElement = null;
        // LiFIXME: 根据不同的属性名称，做不同业务逻辑校验，并返回处理结果
        return errLogElement;
    }

    /**
     * 逆向操作校验逻辑
     * 
     * @param vo
     * @param attributeName
     * @return
     */
    private ErrLogElement validateCanUnOperate(CustomerVO vo,
            String attributeName) {
        ErrLogElement errLogElement = null;
        // LiFIXME: 根据不同的属性名称，做不同业务逻辑校验，并返回处理结果

        return errLogElement;
    }

    private List<CustomerVO> validateUpgradeBatch(
            List<CustomerVO> needUpgradeVOList, ErrorLogUtil util)
            throws BusinessException {
        List<CustomerVO> toUpgradeVOList = new ArrayList<CustomerVO>();
        IValidationService validateService =
                ValidationFrameworkUtil.createValidationService(this
                        .getBatchUpgradeValidator());
        for (CustomerVO vo : needUpgradeVOList) {
            try {
                validateService.validate(vo);
                toUpgradeVOList.add(vo);
            }
            catch (ValidationException e) {
                util.addLogMsgArrayBatch(NCLangRes4VoTransl.getNCLangRes()
                        .getStrByID("", "010140cub0029", null, new String[] {
                            vo.getCode()
                        })/* 编码[{0}]的客户升级失败: */
                        + e.getMessage());
            }
        }
        util.writeLogMsgBatch();
        return toUpgradeVOList;
    }

    @Override
    public CustomerVO insertCustomerVOForCreate(CustomerVO vo, boolean forceSave)
            throws BusinessException {
        if (this.isOrgData(vo)) {
            vo = this.saveCustomer(vo, forceSave);
            this.getCustAssignService().assignCustomerToSelfOrg(vo);
        }
        else {
            vo = this.saveCustomer(vo, forceSave);

        }
        return vo;
    }

    @Override
    public void ansyDeleteCustomerVOs(CustomerVO[] vos)
            throws BusinessException {
        ValueObjWithErrLog errorlog = super.disableVO(vos);
        SuperVO[] currentvos = errorlog.getVos();
        String delperson = AuditInfoUtil.getCurrentUser();
        UFDateTime currenttime = AuditInfoUtil.getCurrentTime();
        String mdid = IBDMetaDataIDConst.CUSTOMER;
        List<SuperVO> list = new ArrayList<SuperVO>();
        getCustomervos(currentvos, vos, list);
        SuperVO[] needdeletevos = list.toArray(new SuperVO[0]);
        resetCustomerVos(needdeletevos, delperson, currenttime);
        new BaseDAO().updateVOArray(needdeletevos, new String[] {
            CustomerVO.DELETESTATE, CustomerVO.DELPERSON, CustomerVO.DELTIME
        });
        TaskConfig taskconfig =
                new TaskConfigBuilder().name("delete").immediate().assign()
                        .bind();
        taskconfig.setDef1("10140customer");
        taskconfig.setDef3(new UFDateTime(new Date(TimeService.getInstance()
                .getTime())).toString());
        String taskid = taskconfig.getId();
        SuperVO[] afterVos = getAfterUpdatevos(needdeletevos);
        ReallyTread thread =
                new ReallyTread(new CustomerAnsyDelete(), afterVos, delperson,
                        taskid, mdid);
        getLightScheduler().addTask(thread, taskconfig);

    }

    private void getCustomervos(SuperVO[] currentvos, CustomerVO[] vos,
            List<SuperVO> list) {
        if (currentvos != null && currentvos.length > 0) {
            Map<String, SuperVO> map = new HashMap<String, SuperVO>();
            for (SuperVO vo : currentvos) {
                map.put(vo.getPrimaryKey(), vo);
            }
            for (int i = 0; i < vos.length; i++) {
                SuperVO vo = vos[i];
                if (map.containsKey(vo.getPrimaryKey())) {
                    list.add(map.get(vo.getPrimaryKey()));
                }
                else {
                    list.add(vo);
                }
            }
        }
        else {
            for (int i = 0; i < vos.length; i++) {
                list.add(vos[i]);
            }
        }
    }

    private CustomerVO[] getAfterUpdatevos(SuperVO[] supervos)
            throws BusinessException {
        List<String> pkList =
                VOUtil.extractFieldValues(supervos, CustomerVO.PK_CUSTOMER,
                        null);
        CustomerVO[] vos = null;
        InSqlBatchCaller caller =
                new InSqlBatchCaller(pkList.toArray(new String[0]));
        try {
            List<CustomerVO> list =
                    (List<CustomerVO>) caller
                            .execute(new IInSqlBatchCallBack() {
                                List<CustomerVO> list =
                                        new ArrayList<CustomerVO>();

                                @Override
                                public Object doWithInSql(String inSql)
                                        throws BusinessException, SQLException {
                                    Collection<CustomerVO> col =
                                            CustBaseInfoServiceImpl.this
                                                    .getBaseDAO()
                                                    .retrieveByClause(
                                                            CustomerVO.class,
                                                            CustomerVO.PK_CUSTOMER
                                                                    + " in "
                                                                    + inSql);
                                    if (col != null && col.size() > 0) {
                                        this.list.addAll(col);
                                    }
                                    return this.list;
                                }
                            });
            vos = list.toArray(new CustomerVO[0]);
        }
        catch (SQLException e) {
            Logger.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage(), e);
        }
        return vos;

    }

    private void resetCustomerVos(SuperVO[] vos, String delperson,
            UFDateTime time) {
        for (SuperVO vo : vos) {
            vo.setAttributeValue(CustomerVO.DELETESTATE,
                    IPubEnumConst.DELETEING);
            vo.setAttributeValue(CustomerVO.DELPERSON, delperson);
            vo.setAttributeValue(CustomerVO.DELTIME, time);
        }

    }

    @Override
    public void deleteCustomerVO_RequiresNew(CustomerVO vo)
            throws BusinessException {
        this.deleteVO(vo);

        // 自动编码退号
        BillCodeContext billCodeContext =
                this.getBillcodeManage().getBillCodeContext(
                        ICustConst.BILLCODE_CUSTOMER, vo.getPk_group(),
                        vo.getPk_org());
        // 存在编码规则时，退号
        if (billCodeContext != null) {
            this.getBillcodeManage().returnBillCodeOnDelete(
                    ICustConst.BILLCODE_CUSTOMER, vo.getPk_group(),
                    vo.getPk_org(), vo.getCode(), vo);
        }
    }

    private ILightScheduler getLightScheduler() {
        if (lightScheduler == null)
            lightScheduler =
                    NCLocator.getInstance().lookup(ILightScheduler.class);
        return lightScheduler;
    }
    //63没有这个方法
    public void deleteCustomer_ForCustMerge(CustomerVO vo)
    	    throws BusinessException
    	  {
    	    if (vo == null) {
    	      return;
    	    }

    	    deletelockOperate(vo);

    	    BDVersionValidationUtil.validateSuperVO(new SuperVO[] { vo });

    	    deleteValidateVO(vo);

    	    notifyVersionChangeWhenDataDeleted(vo);

    	    dbDeleteVO(vo);

    	    writeDeletedBusiLog(vo);

    	    BillCodeContext billCodeContext = getBillcodeManage().getBillCodeContext("customer", vo.getPk_group(), vo.getPk_org());

    	    if (billCodeContext != null)
    	      getBillcodeManage().returnBillCodeOnDelete("customer", vo.getPk_group(), vo.getPk_org(), vo.getCode(), vo);
    	  }

}
