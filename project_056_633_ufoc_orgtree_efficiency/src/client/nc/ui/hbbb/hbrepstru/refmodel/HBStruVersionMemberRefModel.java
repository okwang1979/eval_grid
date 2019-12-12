package nc.ui.hbbb.hbrepstru.refmodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.impl.iufo.utils.MultiLangTextUtil;
import nc.itf.uap.IUAPQueryBS;
import nc.ui.corg.ref.OrgStruMemberDefaultRefModel;
import nc.ui.hbbb.utils.HBRepStruUIUtil;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.vorg.ReportCombineStruVersionVO;

/**
 * ����ϲ���ϵ��ʷ�汾��Ա����
 * @author jiaah
 *
 */
public class HBStruVersionMemberRefModel extends OrgStruMemberDefaultRefModel {
	
	
	private Map<String, Object> cacheMap = new HashMap<String, Object>();
	
	/**
	 * �ϲ���ϵ����
	 */
	private String pk_rcs = null;

	/**
	 * ��ѡ�е���֯��pk
	 */
	private String[] selected_org_pks = null;
	
	/**
	 * �Ƿ�����Ĭ��ѡ��ֵ
	 */
	private boolean bSetDefault = false;
	/**
	 * ����Ȩ�޵���֯pk����
	 */
	private String[] orgPKS = null;
	
	public HBStruVersionMemberRefModel(String[] orgPKS) {
		super();
		this.orgPKS = orgPKS;
		reset();
	}

	public void reset() {
//		cacheMap.clear();
		setRefNodeName("����ϲ���ϵ�汾��ʷ��Ա");
		
		setFieldCode(new String[] { "code", "name"});
		setFieldName(new String[] { 
				NCLangRes4VoTransl.getNCLangRes().getStrByID("common", "UC000-0003279") /* @res "����" */,
				NCLangRes4VoTransl.getNCLangRes().getStrByID("common", "UC000-0001155") /* @res "����" */
					});
		setHiddenFieldCode(new String[] {"pk_rcsmember", "pk_fathermember", "pk_org","pk_svid"});
		setPkFieldCode("pk_rcsmember");
		setRefCodeField("code");
		setRefNameField("name");
		setFatherField("pk_fathermember");
		setChildField("pk_rcsmember");
		
		setOrderPart("code");
		
		setTableName("(" +
				"select pk_svid, org_reportorg.code, org_reportorg.name, org_reportorg.name2, org_reportorg.name3, " + 
				"org_reportorg.name4, org_reportorg.name5, org_reportorg.name6, org_reportorg.pk_reportorg AS pk_org, " +
				"pk_fathermember, pk_rcsmember, pk_rcs from org_rcsmember_v  " +
				"left join org_reportorg on org_rcsmember_v.pk_org = org_reportorg.pk_reportorg "+
				") rcsm_temp ");
		
		setMatchPkWithWherePart(true);
		setRefTitle(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0080")/* @res "����ϲ���ϵ��Ա" */);
		resetFieldName();
	}
	
	@Override
	public String getRootName() {
		if(getPk_stru() != null) {
			try {
				ReportCombineStruVersionVO vo = (ReportCombineStruVersionVO) NCLocator.getInstance()
						.lookup(IUAPQueryBS.class).retrieveByPK(ReportCombineStruVersionVO.class, getPk_stru());
				String multLangName = MultiLangTextUtil.getCurLangText(vo, ReportCombineStruVersionVO.VNAME);
				return HBRepStruUIUtil.getHBRepStruName(getPk_rcs()) + "(" + multLangName + ")";
			} catch (BusinessException e) {
				Logger.debug(e.getMessage());
			}
			return null;
		}
		return getRefTitle();

	}
	

	@Override
	protected String getEnvWherePart() {
		return "(pk_svid = '" + getPk_stru() + "')";
	}
	
	/**
	 * ��ƥ��PK��ʱ����Ҫ�ӵ�sql����
	 * modified by jiaah at 20130726
	 */
	@Override
	public String getWherePart() {
		return "(pk_svid = '" + getPk_stru() + "')";
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void setSelectedData(Vector vecSelectedData) {
		super.setSelectedData(vecSelectedData);
		int fieldIndex = this.getFieldIndex("pk_org");
		if(vecSelectedData != null) {
			Iterator iter = vecSelectedData.iterator();
			Set<String> set = new  HashSet<String> ();
			if(iter != null) {
				while(iter.hasNext()) {
					Object obj = iter.next();
					if(obj instanceof Vector) {
						String pk_org = (String)((Vector)obj).get(fieldIndex);
						set.add(pk_org);
					}
				}
			}
			innerSetSelectedOrgPKs(set.toArray(new String[set.size()]));
		}
	}
	
	/**
	 * �����ϴ�ѡ�е���֯ˢ�²��գ�ͨ����������pk_rcs֮��
	 * @create by fengzhy at 2012-3-5,����10:15:28
	 *
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void refreshBySelectedOrgs() {
		int fieldIndex = this.getFieldIndex("pk_org");
		//����PK_org�õ�pk_resmember
		Vector vecSelectedData = new Vector();
		
		Vector refData = null;
		if(null != this.cacheMap && this.cacheMap.size() > 0){
			refData = getData();
		}else{
			refData = this.getRefData();
		}
		
		
		if(refData!=null&&refData.size()>0 && selected_org_pks!=null && selected_org_pks.length>0){
			for (Iterator iterator = refData.iterator(); iterator.hasNext();) {
				Vector object = (Vector) iterator.next();
				String pk_org = (String) object.get(fieldIndex);
				for(String selectedOrg : selected_org_pks) {
					if(selectedOrg!= null && selectedOrg.equals(pk_org)) {
						setbSetDefault(true);
						vecSelectedData.add(object);
						break;
					}
				}
			}
		}
		else{
			setbSetDefault(false);
		}
		if(vecSelectedData.size()>0) {
			this.setSelectedData(vecSelectedData);
		}
		else {
			this.setSelectedData(null);
		}
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Vector<Vector> getData() {
		
		String key = getRefSql();
		key = key+"||"+getRefDataCacheKey();
		if(cacheMap.get(key)!=null){
			return ( Vector<Vector> )cacheMap.get(key);
		}else{
			
			Vector<Vector> data = super.getData();
			if(orgPKS != null && data != null && data.size() >0 ){
				Vector<Vector> updatedata = new Vector<Vector>();
				if (data != null){
					for(Vector v : data){
						for(int i = 0 ;i < orgPKS.length; i++){
							if(v.get(4) !=null && v.get(4).toString().equals(orgPKS[i])){
								updatedata.add(v);
								break;
							}
						}
					}
					cacheMap.put(key, updatedata);
					return updatedata;
				}
			}
			cacheMap.put(key, data);
			return data;
			
		}
		
		//������633ԭʼ����ע�͵�
//		Vector<Vector> data = super.getData();
//		if(orgPKS != null && data != null && data.size() >0 ){
//			Vector<Vector> updatedata = new Vector<Vector>();
//			if (data != null){
//				for(Vector v : data){
//					for(int i = 0 ;i < orgPKS.length; i++){
//						if(v.get(4) !=null && v.get(4).toString().equals(orgPKS[i])){
//							updatedata.add(v);
//							break;
//						}
//					}
//				}
//				return updatedata;
//			}
//		}
//		return data;
	}
	
	
	public void setSelectedOrgPKs(String[] strings) {
		innerSetSelectedOrgPKs(strings);
		refreshBySelectedOrgs();
	}
	
	private void innerSetSelectedOrgPKs(String[] strings) {
		selected_org_pks = strings;
	}

	public String getPk_rcs() {
		return pk_rcs;
	}

	public void setPk_rcs(String pk_rcs) {
		this.pk_rcs = pk_rcs;
	}

	@Override
	public void setPk_stru(String pk_stru) {
		super.setPk_stru(pk_stru);
		refreshBySelectedOrgs();
	}
	
	public boolean isbSetDefault() {
		return bSetDefault;
	}

	public void setbSetDefault(boolean bSetDefault) {
		this.bSetDefault = bSetDefault;
	}
	
	/**
	 * ����ϲ���ϵ�������˶�����ŵ���֯���������ݻ����ʱ�����ز����������ŵ���֯
	 * �˲��ղ��߻��� -- jiaah
	 */
	@Override
	public boolean isCacheEnabled() {
		return false;
	}
	
	/**
	 * ����ϲ���ϵ�������˶�����ŵ���֯���������ݻ����ʱ�����ز����������ŵ���֯
	 * ������Ŀ��ѯ��ʱ�򣬴Ӻ�̨��ѯ���ݣ��������ݻ����ѯ
	 */
	@Override
	public boolean isQueryFromServer() {
		return true;
	}
}