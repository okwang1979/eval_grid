package nc.ui.hbbb.hbreport.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.framework.common.NCLocator;
import nc.bs.uif2.BusinessExceptionAdapter;
import nc.itf.hbbb.hbreport.IHBReportQueryService;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.itf.hbbb.hbrepstru.IHBRepstruQrySrv;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.hbbb.quickquery.model.HBBBTangramInitEntrance;
import nc.ui.iufo.pub.UfoPublic;
import nc.ui.iufo.query.common.IUfoQueryExecutor;
import nc.ui.iufo.query.common.IUfoQueryPaging;
import nc.ui.iufo.query.common.model.IUfoBillManageModel;
import nc.ui.uif2.IShowMsgConstant;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.components.pagination.BillManagePaginationDelegator;
import nc.ui.uif2.components.pagination.IPaginationModelListener;
import nc.ui.uif2.components.pagination.IPaginationQueryService;
import nc.ui.uif2.components.pagination.PaginationModel;
import nc.ui.uif2.model.ModelDataDescriptor;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.iufo.pub.IDMaker;
import nc.vo.corg.ReportCombineStruMemberWithCodeNameVO;
import nc.vo.hbbb.hbscheme.HBSchemeReportVO;
import nc.vo.hbbb.util.KeyWordPubUtil;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;

/**
 * �ϲ������ѯִ����
 * 
 * @version V6.1
 * @author litfb
 */
@SuppressWarnings("restriction")
public class HBReportQueryExecutor implements IUfoQueryExecutor, IPaginationModelListener, IUfoQueryPaging {

    protected final static String QUERY_RESULT_LABEL = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
            "1820001_0", "01820001-0220")/* @res "��ѯ���" */;
    
    protected final static String REP_TYPE = "HB";

    private IUfoBillManageModel model;

    private IUfoQueryCondVO queryCond;

    private HBBBTangramInitEntrance entrance;

    private ModelDataDescriptor descriptor = new ModelDataDescriptor(QUERY_RESULT_LABEL);

    private PaginationModel paginationModel;

    private BillManagePaginationDelegator paginationDelegator;

    private final Map<String, Object> pkVOMap = new HashMap<String, Object>();

    private HBReportQueryConfig queryConfig;

    @Override
    public void paging(Object[] results, String descriptName) {
        qryResult2Map(results);
        // ��descriptor ��Ϊ -1 ʱ ������ģ���вŻ����¼���
        descriptor.setCount(-1);
        descriptor.setName(descriptName);
        String[] ids = new String[results.length];
        for (int i = 0; i < results.length; i++) {
            ids[i] = ((SuperVO) results[i]).getPrimaryKey();
        }

        try {
            getPaginationModel().setObjectPks(ids, descriptor);
        } catch (BusinessException e) {
            throw new BusinessExceptionAdapter(e);
        }

        ShowStatusBarMsgUtil.showStatusBarMsg(IShowMsgConstant.getQuerySuccessInfo(results.length), getModel()
                .getContext());
    }

    /**
     * ����������棬����map�У��Ա���ҳʹ��
     * 
     * @create by wuyongc at 2011-9-15,����11:26:12
     * @param results
     */
    private void qryResult2Map(Object[] results) {
        pkVOMap.clear();
        for (Object repQryVO : results) {
            pkVOMap.put(((SuperVO) repQryVO).getPrimaryKey(), repQryVO);
        }
    }

    @Override
    public void onStructChanged() {

    }

    @Override
    public void onDataReady() {
        getPaginationDelegator().onDataReady();
    }

    @Override
    public void doQuery(IUfoQueryCondVO queryCond) throws Exception {
        this.queryCond = queryCond;
        // ��ѯ
        reQuery();
    }

    @Override
    public void reQuery() throws Exception {
        // ����ѯ�����Ƿ�����
        if (!checkQueryCondComplete(queryCond)) {
            return;
        }

        // ����������ѯ�ϲ�����
        RepDataQueryResultVO[] results = getRepDataQueryResults();

        final Map<String, OrgVO> orgMap = getOrgMap(results);
        getModel().setOrgPkMap(orgMap);
        for(RepDataQueryResultVO result:results){
        	if(orgMap.get(result.getPk_org())!=null){
        		result.setInnercode(orgMap.get(result.getPk_org()).getInnercode());
        	}
        }
        
        final Map<String,ReportCombineStruMemberWithCodeNameVO>  comReports =  getReportOrgMap(results);

        // ���������� �������������������ܻ��õ������絼��ʱ�����ܸ�������ȥȡ�˶���ֱ�ӶԽ����ϵ�ĳһ������и��¡�
        if (results != null && results.length>0) {
            for (RepDataQueryResultVO rq : results) {
                rq.setPk_result(IDMaker.makeID(20));
            }

            final Map<String, Integer> reportOrderMap = getReportOrderMap();

            // �Խ�������� ���Ȱ���֯����,��֯����,�ϲ���������˳������
            Arrays.sort(results, new Comparator<RepDataQueryResultVO>() {
                @Override
                public int compare(RepDataQueryResultVO o1, RepDataQueryResultVO o2) {
                    String innercode1 = orgMap.get(o1.getPk_org()).getInnercode();
                    String innercode2 = orgMap.get(o2.getPk_org()).getInnercode();
                     
                    if(innercode1 != null && innercode2 != null){
                    	if(innercode1.length()==innercode2.length()){
                    	
                    		if(innercode1.equals(innercode2)){
                    			return  reportOrderMap.get(o1.getPk_report()).compareTo(reportOrderMap.get(o2.getPk_report()));
                    		}
                    	}else if(innercode1.length()<innercode2.length()){
                    		innercode2 = innercode2.substring(0,innercode1.length());
                    		if(innercode2.equals(innercode1)){
                    			return -1;
                    		}
                    	}else{
                    		innercode1 = innercode1.substring(0,innercode2.length());
                    		if(innercode2.equals(innercode1)){
                    			return 1;
                    		}
                    	}
                    	ReportCombineStruMemberWithCodeNameVO nameVO1 = comReports.get(o1.getPk_org());
                    	ReportCombineStruMemberWithCodeNameVO nameVO2 = comReports.get(o2.getPk_org());
                    	if(nameVO1!=null&&nameVO2!=null){
                    		if(nameVO2.getIdx()==null||nameVO1.getIdx()==null){
                    			return nameVO2.getCode().compareTo(nameVO1.getCode());
                    		}
                    		return nameVO2.getIdx().compareTo(nameVO1.getIdx());
                    	}else{
                    		return compare(innercode1, innercode2);
                    	}
                    	
//                    	int i = compare(innercode1, innercode2);
//                        i = i == 0 ? compare(orgMap.get(o1.getPk_org()).getCode(), orgMap.get(o2.getPk_org()).getCode())
//                                : i;
//                        i = i == 0 ? reportOrderMap.get(o1.getPk_report()).compareTo(reportOrderMap.get(o2.getPk_report()))
//                                : i;
//                        return i;
                    }else
                    	return -1;
                   
                }
                public int compare(String str1, String str2) {
                    return str1.compareTo(str2);
                }
            });
        }

        // ȡ������ɸѡ�������˺������
        results = (RepDataQueryResultVO[]) model.filterData(results, true);
        paging(results, QUERY_RESULT_LABEL);
    }

	public boolean checkQueryCondComplete(IUfoQueryCondVO queryCond) throws Exception {
        KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(queryCond.getKeyGroupPK());
        if (keyGroup == null) {
            return false;
        }
        KeyVO[] keys = keyGroup.getKeys();
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0357");
        for (KeyVO keyVO : keys) {
            String strKeyVal = null;
            if (keyVO.isTTimeKeyVO()) {
                strKeyVal = queryCond.getDate();
            } else {
                strKeyVal = queryCond.getKeyVal(keyVO.getPk_keyword());
            }
            if (UfoPublic.stringIsNull(strKeyVal)) {
				String strLabel=keyVO.isTimeKeyVO()?nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UC000-0002327")/*@res "ʱ��"*/:KeyWordPubUtil.getMulName(keyVO);
				String msg = "[ "+strLabel+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0357")/*@res " ]¼��ֵ����Ϊ��"*/;
				ShowStatusBarMsgUtil.showErrorMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830009-0008")/*@res "��ѯʧ��!"*/, msg,getEntrance().getContext());
				return false;
			}
        }
        return true;
    }
    
    /**
     * �����ѯ���
     * 
     * @return
     * @throws BusinessException
     */
    protected RepDataQueryResultVO[] getRepDataQueryResults() throws BusinessException {
        RepDataQueryResultVO[] results = null;
        IHBReportQueryService queryService = NCLocator.getInstance().lookup(IHBReportQueryService.class);
        List<RepDataQueryResultVO> resultVOs = queryService.queryHbRepDataAndReportDataByCondAndType(queryCond,
                queryConfig.getShowColumns(), queryConfig.getRepType());
        results = resultVOs.toArray(new RepDataQueryResultVO[resultVOs.size()]);
        return results;
    }
    
    /**
     * ȡ�ý�����е���֯pk���Ӧvo��map�� �Թ�ʵ�ֱȽ�����������ʵ�ְ���֯��������
     * 
     * @param results
     * @return
     * @throws BusinessException
     */
    protected Map<String, OrgVO> getOrgMap(RepDataQueryResultVO[] results) throws BusinessException {
        Map<String, OrgVO> orgMap = new HashMap<String, OrgVO>();
        if (results != null && results.length > 0) {
            Set<String> orgSet = new HashSet<String>();
            for (RepDataQueryResultVO result : results) {
                orgSet.add(result.getPk_org());
            }

            String pk_rcs_v = getQueryCond().getPk_rms();
            String[] pk_orgs = getQueryCond().getSelectedOrgPKs();

            orgMap = HBRepStruUtil.queryOrgVOWithRcsvMemberInnerCode(pk_rcs_v, pk_orgs);
        }
        return orgMap;
    }
    
    private Map<String,ReportCombineStruMemberWithCodeNameVO>  getReportOrgMap(RepDataQueryResultVO[] results){
    	   IHBRepstruQrySrv service = (IHBRepstruQrySrv) nc.bs.framework.common.NCLocator.getInstance().lookup(
                   IHBRepstruQrySrv.class.getName());
           ReportCombineStruMemberWithCodeNameVO[] datas = null;
          String parentId = "";
          for(RepDataQueryResultVO vo:results){
        	  if(parentId.equals("")){
        		  parentId =  vo.getInnercode();
        	  }else if(parentId.length()>vo.getInnercode().length()){
        		  parentId =  vo.getInnercode();
        	  }
        	  
          }
          Map<String,ReportCombineStruMemberWithCodeNameVO> rtn = new HashMap<String, ReportCombineStruMemberWithCodeNameVO>();
           try {
        	   String pk_rcs_v = getQueryCond().getPk_rms();
               datas = service.queryReportCombineStruMemberWithCodeByInnercode(pk_rcs_v,parentId);
             
               for(RepDataQueryResultVO result:results){
            	   boolean isFind = false;
            	   for(ReportCombineStruMemberWithCodeNameVO nameVo:datas){
            		   if(nameVo.getPk_org().equals(result.getPk_org())){
            			   rtn.put(result.getPk_org(), nameVo);
            			   isFind =true;
            			   break;
            			   
            		   }
            
            	   }
        		   if(isFind==false){
        			   ReportCombineStruMemberWithCodeNameVO nameVO = service.queryReportCombineStruMemberByOrgPk(pk_rcs_v,result.getPk_org());
        			   if(nameVO!=null){
        				   rtn.put(result.getPk_org(), nameVO);
        			   }
        		   }
            	   
               }
             
           }catch(Exception ex){
        	   throw new BusinessExceptionAdapter(new BusinessException(ex.getMessage(),ex));
           }
           return rtn;
    }

    /**
     * ��úϲ���������˳��
     * 
     * @return
     * @throws BusinessException
     */
    protected Map<String, Integer> getReportOrderMap() throws BusinessException {
        Map<String, Integer> repOrderMap = new HashMap<String, Integer>();
        HBSchemeReportVO[] hbSchemeReportVOs = HBSchemeSrvUtils.getHBReportVOByHBSchemeId(queryCond.getPk_task());
        for (HBSchemeReportVO hbSchemeReportVO : hbSchemeReportVOs) {
            repOrderMap.put(hbSchemeReportVO.getPk_report(), hbSchemeReportVO.getPosition());
        }
        return repOrderMap;
    }

    private Object[] queryQryResultVOByPKs(String[] PKs) {
        List<Object> list = new ArrayList<Object>();
        for (String pk : PKs) {
            list.add(pkVOMap.get(pk));
        }
        return list.toArray();
    }

    public IUfoBillManageModel getModel() {
        return model;
    }

    public void setModel(IUfoBillManageModel model) {
        this.model = model;
        // �໥��������. ��ģ���б��ڴ���������ķ�ҳ����
        model.setQueryPaging(this);
    }

    public IUfoQueryCondVO getQueryCond() {
        return queryCond;
    }

    public void setQueryCond(IUfoQueryCondVO queryCond) {
        this.queryCond = queryCond;
    }

    public HBBBTangramInitEntrance getEntrance() {
        return entrance;
    }

    public void setEntrance(HBBBTangramInitEntrance entrance) {
        this.entrance = entrance;
    }

    public ModelDataDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(ModelDataDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public PaginationModel getPaginationModel() {
        return paginationModel;
    }

    public void setPaginationModel(PaginationModel paginationModel) {
        this.paginationModel = paginationModel;
        this.paginationModel.addPaginationModelListener(this);
        this.paginationModel.setPaginationQueryService(new IPaginationQueryService() {
            @Override
            public Object[] queryObjectByPks(String[] pks) throws BusinessException {
                return queryQryResultVOByPKs(pks);
            }
        });
    }

    public BillManagePaginationDelegator getPaginationDelegator() {
        return paginationDelegator;
    }

    public void setPaginationDelegator(BillManagePaginationDelegator paginationDelegator) {
        this.paginationDelegator = paginationDelegator;
    }

    public HBReportQueryConfig getQueryConfig() {
        return queryConfig;
    }

    public void setQueryConfig(HBReportQueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

    public Map<String, Object> getPkVOMap() {
        return pkVOMap;
    }

}