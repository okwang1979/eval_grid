package nc.impl.hbbb.hbreport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.itf.hbbb.hbreport.IHBReportQueryService;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.hbbb.hbreport.model.HBReportQueryConfig;
import nc.util.hbbb.HBVersionUtil;
import nc.util.hbbb.hbreport.HBReportQueryUtil;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.vo.hbbb.hbreport.UnionReportVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.vorg.ReportCombineStruVersionVO;

import com.ufida.iufo.pub.tools.AppDebug;

public class HBReportQueryServiceImpl implements IHBReportQueryService {

    @Override
    public UnionReportVO[] queryUnionReport(String cond) throws BusinessException {
        @SuppressWarnings("unchecked")
        Collection<UnionReportVO> c = new BaseDAO().retrieveByClause(UnionReportVO.class, " dr=0 ");
        if (c != null && c.size() > 0)
            return c.toArray(new UnionReportVO[c.size()]);
        return new UnionReportVO[0];
    }

    /**
     * ��ѯ�������ݵĽڵ㣺
     * ���𱨱�ͺϲ���������ڵ㣬���û�е�������������null�����������Ҫ���ظ����ϲ�����
     * @author jiaah modified at 2013-8-8
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<RepDataQueryResultVO> queryRepDataByCondAndType(IUfoQueryCondVO queryCond, String[] showColumns,
            String repType) throws UFOSrvException {
        try {
        	List<RepDataQueryResultVO> lst = new ArrayList<RepDataQueryResultVO>();
            if (queryCond.getPk_task() == null) {
                return lst;
            }
            // ��ʾ��
            Set<String> vShowColumn = new HashSet<String>(Arrays.asList(showColumns));
            // �ϲ�����
            HBSchemeVO hbSchemeVO = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(queryCond.getPk_task());
            // version
            Integer ver = getVersionByRepType(hbSchemeVO, repType);
            if(ver == null)
            	return lst;
            
            boolean bContainIntrRep = true;
            // ������ֻ�������ݵ�,����Ҫ���ڲ����ײɼ���
            if (HBReportQueryConfig.REP_TYPE_SEPADJ.equals(repType) || HBReportQueryConfig.REP_TYPE_HBADJ.equals(repType)) {
                queryCond.setInputState(1);
                bContainIntrRep = false;
            }
          //��֯�Ƿ�Ҷ�ӽڵ�
    		Map<String, UFBoolean> isLeaf = null;
    		Map<String, UFBoolean> isBalance = null;
            //editted by zhoushuang at 2015.1.27 �ϲ���ֻ���ĩ����  �Ȳ��λ
            if (HBReportQueryConfig.REP_TYPE_HB.equals(repType)) {
            	KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(hbSchemeVO.getPk_keygroup());
        		String pk_period_keyword = null;
        		if(keyGroup.getTTimeKey() != null) {
        			pk_period_keyword = keyGroup.getTTimeKey().getPk_keyword();
        		}
        		ReportCombineStruVersionVO versionVO = null;
        		if (KeyVO.isAccPeriodKey(pk_period_keyword)) {
        			versionVO = HBRepStruUtil.getHBStruVersionVO(hbSchemeVO.getPk_accperiodscheme(), pk_period_keyword, queryCond.getDate(), hbSchemeVO.getPk_repmanastru());
				}else{
        			versionVO = HBRepStruUtil.getHBStruVersionVO(queryCond.getDate(), hbSchemeVO.getPk_repmanastru());
        		}
            	String[] pk_orgs = queryCond.getSelectedOrgPKs();
            	//��֯�Ƿ�Ҷ�ӽڵ�
        		isLeaf = HBBaseDocItfService.getRemoteHBRepStru().isLeafMembers(pk_orgs, versionVO.getPk_vid());
        		//��֯�Ƿ��λ
        		isBalance = HBBaseDocItfService.getRemoteHBRepStru().isBalanceUnits(pk_orgs, versionVO.getPk_vid());
        		//�Ȳ��λ
        		List<String> pk_orgList = new ArrayList<>();
        		if (isBalance != null && isBalance.size()>0) {
        			ver = HBVersionUtil.getDiffByHBSchemeVO(hbSchemeVO); 
        			for (int i = 0; i < pk_orgs.length; i++) {
    					if (isBalance.get(pk_orgs[i])!=null && isBalance.get(pk_orgs[i]).booleanValue()) {
    						pk_orgList.add(pk_orgs[i]);
    					}
    				}
            		
            		if (pk_orgList != null && pk_orgList.size() > 0) {
            			queryCond.setSelectedOrgPKs(pk_orgList.toArray(new String[pk_orgList.size()]));
    				}else {
    					queryCond.setSelectedOrgPKs(new String[0]);
    				} 
            		
            		if (queryCond.getSelectedOrgPKs()==null && queryCond.getSelectedOrgPKs().length==0) {
                     	return lst;
         			}
            		
            		// ���ݿ��ٲ�ѯ��������sql
//                    String strSQL = HBReportQueryUtil.getRepDataQuerySQL(queryCond, vShowColumn, hbSchemeVO, ver, bContainIntrRep);
            		  String strSQL = HBReportQueryUtil.getRepDataQuerySQL(queryCond, vShowColumn, hbSchemeVO, ver, false);
                    if(strSQL == null || strSQL.length() == 0){
                    	return lst;
                    }
                    BaseDAO dao = new BaseDAO();
                    List<RepDataQueryResultVO> difflst = (List<RepDataQueryResultVO>) dao.executeQuery(strSQL, new BeanListProcessor(RepDataQueryResultVO.class));
            		lst.addAll(difflst);
				}
        		
        		pk_orgList.clear();
        		ver = getVersionByRepType(hbSchemeVO, repType);
        		for (int i = 0; i < pk_orgs.length; i++) {
					if (!isLeaf.get(pk_orgs[i]).booleanValue()) {
						pk_orgList.add(pk_orgs[i]);
					}
				}
        		if (pk_orgList != null && pk_orgList.size() > 0) {
        			queryCond.setSelectedOrgPKs(pk_orgList.toArray(new String[pk_orgList.size()]));
				}else {
					queryCond.setSelectedOrgPKs(new String[0]);
				} 
            }
            
            if (queryCond.getSelectedOrgPKs()==null && queryCond.getSelectedOrgPKs().length==0) {
            	return lst;
			}
            
            // ���ݿ��ٲ�ѯ��������sql
            String strSQL = HBReportQueryUtil.getRepDataQuerySQL(queryCond, vShowColumn, hbSchemeVO, ver, bContainIntrRep);
            
            if(strSQL == null || strSQL.length() == 0){
            	return lst;
            }
            BaseDAO dao = new BaseDAO();
            List<RepDataQueryResultVO> hblst = (List<RepDataQueryResultVO>) dao.executeQuery(strSQL, new BeanListProcessor(RepDataQueryResultVO.class));
            lst.addAll(hblst);
            return lst;
        } catch (Exception e) {
            AppDebug.debug(e);
            throw new UFOSrvException(e.getMessage(), e);
        }
    }

    /**
     * ���ݱ������ͻ�ð汾��
     * 
     * @param hbSchemeVO
     * @param repType
     * @return
     */
    private Integer getVersionByRepType(HBSchemeVO hbSchemeVO, String repType) {
    	Integer ver = null;
        if (HBReportQueryConfig.REP_TYPE_SEPADJ.equals(repType) && hbSchemeVO.getPk_adjustscheme() != null) {
        	ver = HBVersionUtil.getSepAdjustByHBSchemeVO(hbSchemeVO);
        } else if (HBReportQueryConfig.REP_TYPE_HBADJ.equals(repType) && hbSchemeVO.getPk_adjustscheme() != null) {
            ver = HBVersionUtil.getHBAdjustByHBSchemeVO(hbSchemeVO);
        } else if(HBReportQueryConfig.REP_TYPE_HB.equals(repType)){
            ver = hbSchemeVO.getVersion();
        }
        return ver;
    }

}
