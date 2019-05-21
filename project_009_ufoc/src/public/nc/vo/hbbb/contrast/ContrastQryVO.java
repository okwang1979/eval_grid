package nc.vo.hbbb.contrast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import nc.itf.hbbb.contrast.IntrMeasProjectCache;
import nc.vo.hbbb.dxrelation.DXContrastVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.pub.ValidationException;
import nc.vo.pub.ValueObject;
import nc.vo.pub.lang.UFDouble;


/**
 * ��������VO
 * @date 2011-01-19
 * @author liyra
 *
 */
public class ContrastQryVO extends ValueObject {
	
	private static final long serialVersionUID = 1234123412341L;
	
	/**
	 * ����ǰԤ�����õ�����ֻ������1-5
	 */
	private HBSchemeVO schemevo;        //1 �ϲ�����vo
	private String contrastorg;         //2 �ϲ���֯
	private String pk_hbrepstru;//3 ����ϲ���ϵ�汾����
	private DXContrastVO[] dxmodels;    //4 ģ�����
	private Map<String, String> keymap; //5 �ؼ������
	
	/**
	 * �ɱ���תȨ�淨�����õ�������6
	 */
	private int reporttype;// 6 ���ڳɱ���תȨ�淨,ȡ���汾,ֵΪ���𱨱�(5),���ߺϲ�����(6);
	/**
	 * �Զ����ɵ�����¼�����õ�������1-5��7
	 */
	private String pk_user;  //7 ��ǰ��½�û�
	
	/**
	 * ���˹��������ɵ��м����ݣ�8-12
	 */
	private HashSet<String> selfOrgs = null;// 8 ������λ����
	private HashSet<String> OppOrgs = null;// 9 �Է���λ����
	private HashSet<String> orgs = null;// 10 ���е�λ���ϣ�Ŀǰ��srep�������õ����������ֻ�ڶ��˵�ʱ����Ҫ���˲�����
	
	private HashSet<String> hashLowerOrgs =null;// 11 ���¼���λ����
	private HashSet<String> leafOrgs =null;// 12 ĩ����λ����
	private Map<String, String> org_supplier_map =null;// ��̬���ؼ���Ϊ�ڲ����̵�ʱ��Ԥ��ֵ��13��֯��Ӧ�Ŀ���map��
	private String pkLock = null;//���˶��̴߳��������pkhbschme+aloneid
	
	/**
	 * �������֮�������
	 */
	private String[] contrastorgs = null;// 13 �������ã����˶�
	private Map<String, String> oppEntityOrgs = new HashMap<String, String>();//14 �������ã��Է���֯����ʵ��֯���˶�
	private Map<String,MeasurePubDataVO> pubDataVos = null;//15�������ã�Ԥ���صĹؼ�����Ϣ
	public IntrMeasProjectCache meaprojectcache;//16����ʱ���ã�ӳ���ϵ�Ļ������ʵ��
	private Map<String,String> offset = null;//17����ʹ�����ã�Ԥ����ƫ����Ϊ0ʱ�Ĺؼ��ֶ�Ӧ��Ϣ
	
	
	//INTRBO������������ʱ,��̨����ȡ��,��ִ�������Ļ����д���,keyΪָ��PK,�ڲ�MAP  keyΪ���Է���֯PK(pk_seleforg+pk_opporg),��Ҫ����INTR
	Map<String, Map<String,UFDouble>> resultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	//������������ʱ,��̨����ȡ��,��ִ�������Ļ����д���,keyΪָ��PK,�ڲ�MAP  keyΪ��λPK(pk_seleforg+pk_opporg),��Ҫ����SREP,�洢�ϲ�����������
	Map<String, Map<String,UFDouble>> srep_hbadjSep_resultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	//������������ʱ,��̨����ȡ��,��ִ�������Ļ����д���,keyΪָ��PK,�ڲ�MAP  keyΪ��λPK(pk_seleforg+pk_opporg),��Ҫ����SREP,�洢�ϲ�������
	Map<String, Map<String,UFDouble>> srep_hbsep_resultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	//������������ʱ,��̨����ȡ��,��ִ�������Ļ����д���,keyΪָ��PK,�ڲ�MAP  keyΪ��λPK(pk_seleforg+pk_opporg),��Ҫ����SREP,�洢���𱨱������
	Map<String, Map<String,UFDouble>> srep_sepadj_resultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	//������������ʱ,��̨����ȡ��,��ִ�������Ļ����д���,keyΪָ��PK,�ڲ�MAP  keyΪ��λPK(pk_seleforg+pk_opporg),��Ҫ����SREP,�洢���𱨱�
	Map<String, Map<String,UFDouble>> srep_sep_resultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	//UCHECKBO������������ʱ,��̨����ȡ��,��ִ�������Ļ����д���,keyΪproject,�ڲ�MAP  keyΪ���Է���֯PK(pk_seleforg+pk_opporg),��Ҫ����INTR
	Map<String, Map<String,UFDouble>> ucheckResultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	/**���Է�+��һ����̬���ؼ���ֵ��aloneid*/
	Map<String, String> orgsWithDyn_aloneid_map = new HashMap<String, String>();
		
	/**���Է���aloneid*/
	Map<String, String> orgs_aloneid_map = new HashMap<String, String>();
	
	public HBSchemeVO getSchemevo() {
		return schemevo;
	}

	public void setSchemevo(HBSchemeVO schemevo) {
		this.schemevo = schemevo;
	}

	public DXContrastVO[] getDxmodels() {
		return dxmodels;
	}

	public void setDxmodels(DXContrastVO[] dxmodels) {
		this.dxmodels = dxmodels;
	}

	public String getContrastorg() {
		return contrastorg;
	}

	public void setContrastorg(String contrastorg) {
		this.contrastorg = contrastorg;
	}

	public Map<String, String> getKeymap() {
		return keymap;
	}

	public void setKeymap(Map<String, String> keymap) {
		this.keymap = keymap;
	}

	@Override
	public String getEntityName() {
		return null;
	}

	@Override
	public void validate() throws ValidationException {
		
	}

	public String getPk_user() {
		return pk_user;
	}

	public void setPk_user(String pk_user) {
		this.pk_user = pk_user;
	}

//	public String getStddate() {
//		return stddate;
//	}
//
//	public void setStddate(String stddate) {
//		this.stddate = stddate;
//	}

//	public Map<String, Map<String, UFDouble>> getCesumResultMap() {
//		return cesumResultMap;
//	}
//
//	public void setCesumResultMap(Map<String, Map<String, UFDouble>> cesumResultMap) {
//		this.cesumResultMap = cesumResultMap;
//	}

	public Map<String, Map<String, UFDouble>> getUcheckResultMap() {
		return ucheckResultMap;
	}

	public void setUcheckResultMap(
			Map<String, Map<String, UFDouble>> ucheckResultMap) {
		this.ucheckResultMap = ucheckResultMap;
	}

	public HashSet<String> getSelfOrgs() {
		return selfOrgs;
	}

	public void setSelfOrgs(HashSet<String> selfOrgs) {
		this.selfOrgs = selfOrgs;
	}

	public HashSet<String> getOppOrgs() {
		return OppOrgs;
	}

	public void setOppOrgs(HashSet<String> oppOrgs) {
		OppOrgs = oppOrgs;
	}

	public Map<String, Map<String, UFDouble>> getResultMap() {
		return resultMap;
	}

	public void setResultMap(Map<String, Map<String, UFDouble>> resultMap) {
		this.resultMap = resultMap;
	}

	public HashSet<String> getOrgs() {
		return orgs;
	}

	public void setOrgs(HashSet<String> orgs) {
		this.orgs = orgs;
	}

	public Map<String, Map<String, UFDouble>> getSrep_hbadjSep_resultMap() {
		return srep_hbadjSep_resultMap;
	}

	public void setSrep_hbadjSep_resultMap(Map<String, Map<String, UFDouble>> srep_hbadjSep_resultMap) {
		this.srep_hbadjSep_resultMap = srep_hbadjSep_resultMap;
	}

	public Map<String, Map<String, UFDouble>> getSrep_hbsep_resultMap() {
		return srep_hbsep_resultMap;
	}

	public Map<String, String> getOffset() {
		return offset;
	}

	public void setOffset(Map<String, String> offset) {
		this.offset = offset;
	}

	public Map<String,MeasurePubDataVO> getPubDataVos() {
		return pubDataVos;
	}

	public void setPubDataVos(Map<String,MeasurePubDataVO> pubDataVos) {
		this.pubDataVos = pubDataVos;
	}

	public Map<String, String> getOrgs_aloneid_map() {
		return orgs_aloneid_map;
	}

	public void setOrgs_aloneid_map(Map<String, String> orgs_aloneid_map) {
		this.orgs_aloneid_map = orgs_aloneid_map;
	}

	public Map<String, String> getOppEntityOrgs() {
		return oppEntityOrgs;
	}

	public void setOppEntityOrgs(Map<String, String> oppEntityOrgs) {
		this.oppEntityOrgs = oppEntityOrgs;
	}

	public String[] getContrastorgs() {
		return contrastorgs;
	}

	public void setContrastorgs(String[] contrastorgs) {
		this.contrastorgs = contrastorgs;
	}

	public void setSrep_hbsep_resultMap(Map<String, Map<String, UFDouble>> srep_hbsep_resultMap) {
		this.srep_hbsep_resultMap = srep_hbsep_resultMap;
	}

	public Map<String, Map<String, UFDouble>> getSrep_sepadj_resultMap() {
		return srep_sepadj_resultMap;
	}

	public void setSrep_sepadj_resultMap(Map<String, Map<String, UFDouble>> srep_sepadj_resultMap) {
		this.srep_sepadj_resultMap = srep_sepadj_resultMap;
	}

	public Map<String, Map<String, UFDouble>> getSrep_sep_resultMap() {
		return srep_sep_resultMap;
	}

	public void setSrep_sep_resultMap(Map<String, Map<String, UFDouble>> srep_sep_resultMap) {
		this.srep_sep_resultMap = srep_sep_resultMap;
	}

	public HashSet<String> getHashLowerOrgs() {
		return hashLowerOrgs;
	}

	public void setHashLowerOrgs(HashSet<String> hashLowerOrgs) {
		this.hashLowerOrgs = hashLowerOrgs;
	}

	public HashSet<String> getLeafOrgs() {
		return leafOrgs;
	}

	public void setLeafOrgs(HashSet<String> leafOrgs) {
		this.leafOrgs = leafOrgs;
	}

	public int getReporttype() {
		return reporttype;
	}

	public void setReporttype(int reporttype) {
		this.reporttype = reporttype;
	}

	public String getPk_hbrepstru() {
		return pk_hbrepstru;
	}

	public void setPk_hbrepstru(String pk_hbrepstru) {
		this.pk_hbrepstru = pk_hbrepstru;
	}
	
	public IntrMeasProjectCache getIntrMeaProjectinstance() {
		return meaprojectcache;
	}
	public void setMeaprojectcache(IntrMeasProjectCache meaprojectcache) {
		this.meaprojectcache = meaprojectcache;
	}

	public Map<String, String> getOrg_supplier_map() {
		return org_supplier_map;
	}

	public void setOrg_supplier_map(Map<String, String> org_supplier_map) {
		this.org_supplier_map = org_supplier_map;
	}

	public String getPkLock() {
		return pkLock;
	}

	public void setPkLock(String pkLock) {
		this.pkLock = pkLock;
	}
	
	public Map<String, String> getOrgsWithDyn_aloneid_map() {
		return orgsWithDyn_aloneid_map;
	}
	
	public void setOrgsWithDyn_aloneid_map(
			Map<String, String> orgsWithDyn_aloneid_map) {
		this.orgsWithDyn_aloneid_map = orgsWithDyn_aloneid_map;
	}
}
