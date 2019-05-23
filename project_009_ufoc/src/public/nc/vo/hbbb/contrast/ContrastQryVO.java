package nc.vo.hbbb.contrast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import nc.itf.hbbb.contrast.IntrMeasProjectCache;
import nc.vo.hbbb.dxrelation.DXContrastVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.pub.ValidationException;
import nc.vo.pub.ValueObject;
import nc.vo.pub.lang.UFDouble;


/**
 * 对账条件VO
 * @date 2011-01-19
 * @author liyra
 *
 */
public class ContrastQryVO extends ValueObject {
	
	private static final long serialVersionUID = 1234123412341L;
	
	/**
	 * 对账前预先设置的条件只包括：1-5
	 */
	private HBSchemeVO schemevo;        //1 合并方案vo
	private String contrastorg;         //2 合并组织
	private String pk_hbrepstru;//3 报表合并体系版本主键
	private DXContrastVO[] dxmodels;    //4 模板组合
	private Map<String, String> keymap; //5 关键字组合
	
	/**
	 * 成本法转权益法需设置的条件：6
	 */
	private int reporttype;// 6 用于成本法转权益法,取数版本,值为个别报表(5),或者合并报表(6);
	/**
	 * 自动生成抵销分录需设置的条件：1-5、7
	 */
	private String pk_user;  //7 当前登陆用户
	
	/**
	 * 对账过程中生成的中间数据：8-12
	 */
	private HashSet<String> selfOrgs = null;// 8 本方单位集合
	private HashSet<String> OppOrgs = null;// 9 对方单位集合
	private HashSet<String> orgs = null;// 10 所有单位集合（目前：srep函数会用到这个参数，只在对账的时候需要填充此参数）
	
	private HashSet<String> hashLowerOrgs =null;// 11 有下级单位集合
	private HashSet<String> leafOrgs =null;// 12 末级单位集合
	private Map<String, String> org_supplier_map =null;// 动态区关键字为内部客商的时候预置值：13组织对应的客商map；
	private String pkLock = null;//对账多线程处理的锁定pkhbschme+aloneid
	
	/**
	 * 进入对账之后的设置
	 */
	private String[] contrastorgs = null;// 13 对账设置：对账对
	//Added by sunzeg 2017.6.16 过滤后全部的对账对，用于多线程
	private String[] allContrastOrgs = null;
	private Map<String, String> oppEntityOrgs = new HashMap<String, String>();//14 对账设置：对方组织的虚实组织对账对
	private Map<String,MeasurePubDataVO> pubDataVos = null;//15对账设置：预加载的关键字信息
	public IntrMeasProjectCache meaprojectcache;//16对账时设置：映射关系的缓存对象实例
	private Map<String,String> offset = null;//17对账使用设置：预加载偏移量为0时的关键字对应信息
	
	
	//INTRBO用于批量对账时,后台批量取数,在执行上下文环境中传递,key为指标PK,内层MAP  key为本对方组织PK(pk_seleforg+pk_opporg),主要用于INTR
	Map<String, Map<String,UFDouble>> resultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	//用于批量对账时,后台批量取数,在执行上下文环境中传递,key为指标PK,内层MAP  key为单位PK(pk_seleforg+pk_opporg),主要用于SREP,存储合并调整表数据
	Map<String, Map<String,UFDouble>> srep_hbadjSep_resultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	//用于批量对账时,后台批量取数,在执行上下文环境中传递,key为指标PK,内层MAP  key为单位PK(pk_seleforg+pk_opporg),主要用于SREP,存储合并表数据
	Map<String, Map<String,UFDouble>> srep_hbsep_resultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	//用于批量对账时,后台批量取数,在执行上下文环境中传递,key为指标PK,内层MAP  key为单位PK(pk_seleforg+pk_opporg),主要用于SREP,存储个别报表调整表
	Map<String, Map<String,UFDouble>> srep_sepadj_resultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	//用于批量对账时,后台批量取数,在执行上下文环境中传递,key为指标PK,内层MAP  key为单位PK(pk_seleforg+pk_opporg),主要用于SREP,存储个别报表
	Map<String, Map<String,UFDouble>> srep_sep_resultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	//UCHECKBO用于批量对账时,后台批量取数,在执行上下文环境中传递,key为project,内层MAP  key为本对方组织PK(pk_seleforg+pk_opporg),主要用于INTR
	Map<String, Map<String,UFDouble>> ucheckResultMap  = new HashMap<String, Map<String,UFDouble>>();
	
	/**本对方+另一个动态区关键字值，aloneid*/
	Map<String, String> orgsWithDyn_aloneid_map = new HashMap<String, String>();
		
	/**本对方，aloneid*/
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
	public String[] getAllContrastOrgs() {
		return allContrastOrgs;
	}

	public void setAllContrastOrgs(String[] allContrastOrgs) {
		this.allContrastOrgs = allContrastOrgs;
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
	public Object clone(){
		ContrastQryVO newCQryVO = (ContrastQryVO) super.clone();
		try{
			//		//INTRBO用于批量对账时,后台批量取数,在执行上下文环境中传递,key为指标PK,内层MAP  key为本对方组织PK(pk_seleforg+pk_opporg),主要用于INTR
			//		Map<String, Map<String,UFDouble>> resultMap  = new HashMap<String, Map<String,UFDouble>>();
			//
			newCQryVO.resultMap =  null;
			if (this.resultMap != null) {
				newCQryVO.resultMap = new HashMap<String, Map<String, UFDouble>>();
				if (this.resultMap.size() > 0) {
					for (Entry<String, Map<String, UFDouble>> entry : this.resultMap.entrySet()) {
						newCQryVO.resultMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
			//		//用于批量对账时,后台批量取数,在执行上下文环境中传递,key为指标PK,内层MAP  key为单位PK(pk_seleforg+pk_opporg),主要用于SREP,存储合并调整表数据
			//		Map<String, Map<String,UFDouble>> srep_hbadjSep_resultMap  = new HashMap<String, Map<String,UFDouble>>();
			//		
			newCQryVO.srep_hbadjSep_resultMap =  null;
			if (this.srep_hbadjSep_resultMap != null) {
				newCQryVO.srep_hbadjSep_resultMap = new HashMap<String, Map<String, UFDouble>>();
				if (this.srep_hbadjSep_resultMap.size() > 0) {
					for (Entry<String, Map<String, UFDouble>> entry : this.srep_hbadjSep_resultMap.entrySet()) {
						newCQryVO.srep_hbadjSep_resultMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
			//		//用于批量对账时,后台批量取数,在执行上下文环境中传递,key为指标PK,内层MAP  key为单位PK(pk_seleforg+pk_opporg),主要用于SREP,存储合并表数据
			//		Map<String, Map<String,UFDouble>> srep_hbsep_resultMap  = new HashMap<String, Map<String,UFDouble>>();
			//		
			newCQryVO.srep_hbsep_resultMap =  null;
			if (this.srep_hbsep_resultMap != null) {
				newCQryVO.srep_hbsep_resultMap = new HashMap<String, Map<String, UFDouble>>();
				if (this.srep_hbsep_resultMap.size() > 0) {
					for (Entry<String, Map<String, UFDouble>> entry : this.srep_hbsep_resultMap.entrySet()) {
						newCQryVO.srep_hbsep_resultMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
			//		//用于批量对账时,后台批量取数,在执行上下文环境中传递,key为指标PK,内层MAP  key为单位PK(pk_seleforg+pk_opporg),主要用于SREP,存储个别报表调整表
			//		Map<String, Map<String,UFDouble>> srep_sepadj_resultMap  = new HashMap<String, Map<String,UFDouble>>();
			//		
			newCQryVO.srep_sepadj_resultMap =  null;
			if (this.srep_sepadj_resultMap != null) {
				newCQryVO.srep_sepadj_resultMap = new HashMap<String, Map<String, UFDouble>>();
				if (this.srep_sepadj_resultMap.size() > 0) {
					for (Entry<String, Map<String, UFDouble>> entry : this.srep_sepadj_resultMap.entrySet()) {
						newCQryVO.srep_sepadj_resultMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
			//		//用于批量对账时,后台批量取数,在执行上下文环境中传递,key为指标PK,内层MAP  key为单位PK(pk_seleforg+pk_opporg),主要用于SREP,存储个别报表
			//		Map<String, Map<String,UFDouble>> srep_sep_resultMap  = new HashMap<String, Map<String,UFDouble>>();
			//		
			newCQryVO.srep_sep_resultMap =  null;
			if (this.srep_sep_resultMap != null) {
				newCQryVO.srep_sep_resultMap = new HashMap<String, Map<String, UFDouble>>();
				if (this.srep_sep_resultMap.size() > 0) {
					for (Entry<String, Map<String, UFDouble>> entry : this.srep_sep_resultMap.entrySet()) {
						newCQryVO.srep_sep_resultMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
			//		//UCHECKBO用于批量对账时,后台批量取数,在执行上下文环境中传递,key为project,内层MAP  key为本对方组织PK(pk_seleforg+pk_opporg),主要用于INTR
			//		Map<String, Map<String,UFDouble>> ucheckResultMap  = new HashMap<String, Map<String,UFDouble>>();
			//			
			newCQryVO.ucheckResultMap =  null;
			if (this.ucheckResultMap != null) {
				newCQryVO.ucheckResultMap = new HashMap<String, Map<String, UFDouble>>();
				if (this.ucheckResultMap.size() > 0) {
					for (Entry<String, Map<String, UFDouble>> entry : this.ucheckResultMap.entrySet()) {
						newCQryVO.ucheckResultMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
			//		/**本对方，aloneid*/
			//		Map<String, String> orgs_aloneid_map = new HashMap<String, String>();
			//
			newCQryVO.orgs_aloneid_map =  null;
			if (this.orgs_aloneid_map != null) {
				newCQryVO.orgs_aloneid_map = new HashMap<String,String>();
				if (this.orgs_aloneid_map.size() > 0) {
					for (Entry<String, String> entry : this.orgs_aloneid_map.entrySet()) {
						newCQryVO.orgs_aloneid_map.put(entry.getKey(), entry.getValue());
					}
				}
			}
			//		private Map<String, String> oppEntityOrgs = new HashMap<String, String>();//14 对账设置：对方组织的虚实组织对账对
			newCQryVO.oppEntityOrgs =  null;
			if (this.oppEntityOrgs != null) {
				newCQryVO.oppEntityOrgs = new HashMap<String, String>();
				if (this.oppEntityOrgs.size() > 0) {
					for (Entry<String, String> entry : this.oppEntityOrgs.entrySet()) {
						newCQryVO.oppEntityOrgs.put(entry.getKey(), entry.getValue());
					}
				}
			}
			//		private Map<String,MeasurePubDataVO> pubDataVos = null;//15对账设置：预加载的关键字信息
			newCQryVO.pubDataVos =  null;
			if (this.pubDataVos != null) {
				newCQryVO.pubDataVos = new HashMap<String,MeasurePubDataVO>();
				if (this.pubDataVos.size() > 0) {
					for (Entry<String, MeasurePubDataVO> entry : this.pubDataVos.entrySet()) {
						newCQryVO.pubDataVos.put(entry.getKey(), entry.getValue());
					}
				}
			}
			//		private HashSet<String> selfOrgs = null;// 8 本方单位集合
			newCQryVO.selfOrgs =  null;
			if (this.selfOrgs != null) {
				newCQryVO.selfOrgs = new HashSet<String>();
				if (!this.selfOrgs.isEmpty()) {
					Iterator<String> entry = this.selfOrgs.iterator();
					while(entry.hasNext()){
						newCQryVO.selfOrgs.add(entry.next());
					}
				}
			}

			//		private HashSet<String> OppOrgs = null;// 9 对方单位集合
			newCQryVO.OppOrgs =  null;
			if (this.OppOrgs != null) {
				newCQryVO.OppOrgs = new HashSet<String>();
				if (!this.OppOrgs.isEmpty()) {
					Iterator<String> entry = this.OppOrgs.iterator();
					while(entry.hasNext()){
						newCQryVO.OppOrgs.add(entry.next());
					}
				}
			}
			//		private HashSet<String> orgs = null;// 10 所有单位集合（目前：srep函数会用到这个参数，只在对账的时候需要填充此参数）
			//		
			newCQryVO.orgs =  null;
			if (this.orgs != null) {
				newCQryVO.orgs = new HashSet<String>();
				if (!this.orgs.isEmpty()) {
					Iterator<String> entry = this.orgs.iterator();
					while(entry.hasNext()){
						newCQryVO.orgs.add(entry.next());
					}
				}
			}
			//		private HashSet<String> hashLowerOrgs =null;// 11 有下级单位集合
			newCQryVO.hashLowerOrgs =  null;
			if (this.hashLowerOrgs != null) {
				newCQryVO.hashLowerOrgs = new HashSet<String>();
				if (!this.hashLowerOrgs.isEmpty()) {
					Iterator<String> entry = this.hashLowerOrgs.iterator();
					while(entry.hasNext()){
						newCQryVO.hashLowerOrgs.add(entry.next());
					}
				}
			}
			//		private HashSet<String> leafOrgs =null;// 12 末级单位集合
			newCQryVO.leafOrgs =  null;
			if (this.leafOrgs != null) {
				newCQryVO.leafOrgs = new HashSet<String>();
				if (!this.leafOrgs.isEmpty()) {
					Iterator<String> entry = this.leafOrgs.iterator();
					while(entry.hasNext()){
						newCQryVO.leafOrgs.add(entry.next());
					}
				}
			}
			//		private Map<String, String> org_supplier_map =null;// 动态区关键字为内部客商的时候预置值：13组织对应的客商map；
			newCQryVO.org_supplier_map =  null;
			if (this.org_supplier_map != null) {
				newCQryVO.org_supplier_map = new HashMap<String, String>();
				if (this.org_supplier_map.size() > 0) {
					for (Entry<String,String> entry : this.org_supplier_map.entrySet()) {
						newCQryVO.org_supplier_map.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}catch(Exception cse){
			nc.bs.logging.Logger.error("ContrastQryVO CloneNotSupportedException", cse);
		}
		return newCQryVO;
	}
}
