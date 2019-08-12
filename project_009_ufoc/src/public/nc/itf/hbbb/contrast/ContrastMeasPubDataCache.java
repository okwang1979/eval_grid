package nc.itf.hbbb.contrast;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;

import nc.bs.framework.common.NCLocator;
import nc.bs.mw.sqltrans.TempTable;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.HBKeyGroupUtil;
import nc.util.hbbb.UFOCSqlUtil;
import nc.util.hbbb.pub.HBPubItfService;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.pub.BusinessException;

import com.ufida.iufo.pub.tools.AppDebug;



/**
 * 内部交易对账，使用到的所有的当期数的measpubdatacache缓存
 * 所有的变量外加一层关键字hbschemepk+aloneid（为了处理多线程的问题）
 * @author jiaah
 */
public class ContrastMeasPubDataCache{
	
	/**预加载当前期间的关键字信息*/
	private Hashtable<String,Hashtable<String,MeasurePubDataVO>> measurePubDataVOTable = new Hashtable<String,Hashtable<String,MeasurePubDataVO>>();

	/** 预加载偏移量为0时的关键字对照关系*/
	private Hashtable<String,Hashtable<String,String>> OffSetTable = new Hashtable<String,Hashtable<String,String>>();
	
	/**当期时间*/
	private Hashtable<String,String> keytimeTable = new Hashtable<String, String>();
	
	/**动态区关键字pk*/
	private Hashtable<String,String> dynKeyTable = new Hashtable<String, String>();
	

	
	/**动态区关键字组合*/
	private Hashtable<String,KeyGroupVO> dynGroupTable = new Hashtable<String, KeyGroupVO>();
	
	private static ContrastMeasPubDataCache instance = null;
	
	private volatile Object lockObj1 = new Object();

	public ContrastMeasPubDataCache(){
		
	}
	
	public synchronized static ContrastMeasPubDataCache getInstance(){
		if(instance == null){
			instance = new ContrastMeasPubDataCache();
		}
		return instance;
	}
	
	public Hashtable<String,String> getKeyTimeTalbe(){
		return keytimeTable;
	}

	public void clear(String schemeAloneId){
		measurePubDataVOTable.remove(schemeAloneId);
		OffSetTable.remove(schemeAloneId);
		keytimeTable.remove(schemeAloneId);
		dynKeyTable.remove(schemeAloneId);
		dynGroupTable.remove(schemeAloneId);
	}
	
	public void clearInvestContrastCache(String schemeAloneId){
		measurePubDataVOTable.remove(schemeAloneId);
		keytimeTable.remove(schemeAloneId);
	}
	
	
	public void clearPk_dynKeyValue(String schemeAloneId) {
		dynKeyTable.remove(schemeAloneId);
	}
	
	public void insert(String pk_dynkeyvalue,MeasurePubDataVO vo,String schemeAloneId){
		if(vo != null){
			StringBuffer keybuffer = new StringBuffer();
			keybuffer.append(vo.getKeywordByPK(KeyVO.CORP_PK));
			keybuffer.append(vo.getKeywordByPK(pk_dynkeyvalue));
			keybuffer.append(vo.getInputDate());//非当期数
			keybuffer.append(vo.getKType());
			Hashtable<String, MeasurePubDataVO> hashtable = measurePubDataVOTable.get(schemeAloneId);
			if(hashtable == null){
				hashtable = new Hashtable<String, MeasurePubDataVO>();
			}
			if(hashtable.get(keybuffer.toString()) == null)
				hashtable.put(keybuffer.toString(), vo);
			measurePubDataVOTable.put(schemeAloneId, hashtable);
		}
	}
	
	
	/**
	 * USE：取预置的内部交易表的MeasurePubDataVO全部取当期的
	 * @create by jiaah at 2013-8-7,上午9:46:12
	 *
	 * @param subKeyGroupVO
	 * @param schemeVO
	 * @param qryVO
	 * @param schemeAloneId
	 * @return
	 * @throws BusinessException
	 */
	public Hashtable<String,Hashtable<String,MeasurePubDataVO>> getAllRelaMeasPubVOs(KeyGroupVO subKeyGroupVO,HBSchemeVO schemeVO,ContrastQryVO qryVO) throws BusinessException {
		String schemeAloneId = qryVO.getPkLock();
		Hashtable<String, MeasurePubDataVO> currPubDatas = measurePubDataVOTable.get(schemeAloneId);
		if (currPubDatas == null) {
			return constructCache(subKeyGroupVO,schemeVO,qryVO,schemeAloneId);
		}else{
			//单位 客商；单位 供应商，同一个合并方案中有不同动态区关键字的表存在
			if(dynGroupTable.get(schemeAloneId) != null && !subKeyGroupVO.equals(dynGroupTable.get(schemeAloneId))){
				MeasurePubDataVO[] vos = getMeasurePubDataVOCaches(subKeyGroupVO,schemeVO,qryVO,schemeAloneId);
				if(vos != null){
					//动态区关键字pk 目前支持一个动态区关键字
					String pk_dynkeyword = getPk_dynKeyValue(subKeyGroupVO.getKeyGroupPK(),schemeVO,schemeAloneId).get(schemeAloneId);
					for(MeasurePubDataVO vo : vos){
						if(vo != null){
							StringBuffer keybuffer = new StringBuffer();
							keybuffer.append(vo.getKeywordByPK(KeyVO.CORP_PK));
							keybuffer.append(vo.getKeywordByPK(pk_dynkeyword));
							keybuffer.append(keytimeTable.get(schemeAloneId));//当期数
							keybuffer.append(vo.getKType());
							if(currPubDatas.get(keybuffer.toString()) == null)
								currPubDatas.put(keybuffer.toString(), vo);
						}
					}
				}
			}
			dynGroupTable.put(schemeAloneId, subKeyGroupVO);
		}
		return measurePubDataVOTable;
	}
	
	private Hashtable<String,Hashtable<String,MeasurePubDataVO>> constructCache(KeyGroupVO subKeyGroupVO,HBSchemeVO schemeVO,ContrastQryVO qryVO,String schemeAloneId) {
		dynGroupTable.put(schemeAloneId, subKeyGroupVO);
		Hashtable<String, MeasurePubDataVO> currMeasureTable = measurePubDataVOTable.get(schemeAloneId);
		if (currMeasureTable == null) {
			synchronized (lockObj1) {
				currMeasureTable = new Hashtable<String, MeasurePubDataVO>();
				MeasurePubDataVO[] vos = getMeasurePubDataVOCaches(subKeyGroupVO,schemeVO,qryVO,schemeAloneId);
				if(vos != null){
					//动态区关键字pk 目前支持一个动态区关键字
					String pk_dynkeyword = getPk_dynKeyValue(subKeyGroupVO.getKeyGroupPK(),schemeVO,schemeAloneId).get(schemeAloneId);
					for(MeasurePubDataVO vo : vos){
						if(vo != null){
							StringBuffer keybuffer = new StringBuffer();
							keybuffer.append(vo.getKeywordByPK(KeyVO.CORP_PK));
							keybuffer.append(vo.getKeywordByPK(pk_dynkeyword));
							keybuffer.append(keytimeTable.get(schemeAloneId));//当期数
							keybuffer.append(vo.getKType());
							if(currMeasureTable.get(keybuffer.toString()) == null)
								currMeasureTable.put(keybuffer.toString(), vo);
						}
					}
				}
			}
			measurePubDataVOTable.put(schemeAloneId, currMeasureTable);
		} 
		return measurePubDataVOTable;
	}
	
	

	/**
	 * USE:取动态区关键字主键
	 * @create by jiaah at 2013-8-7,上午9:45:29
	 * @param subKeyGroupVO
	 * @param schemeVO
	 * @param schemeAloneId
	 * @return
	 */
	public Hashtable<String,String> getPk_dynKeyValue(String pk_dyn,HBSchemeVO schemeVO,String schemeAloneId) {
		if(dynKeyTable.get(schemeAloneId) == null){
			dynKeyTable.put(schemeAloneId, HBKeyGroupUtil.getPk_dynKeyValueByPK(pk_dyn, schemeVO.getPk_keygroup()));
		}
		return dynKeyTable;
	}
	
	/**
	 * USE:取动态区关键字主键
	 * Added by sunzeg 2017.6.12
	 * @create by jiaah at 2013-8-7,上午9:45:29
	 * @param subKeyGroupVO
	 * @param schemeVO
	 * @param schemeAloneId
	 * @return
	 */
	public Hashtable<String,String> getPk_dynKeyValue(KeyGroupVO subKeyGroupVO,HBSchemeVO schemeVO,String schemeAloneId) {
		if(dynKeyTable.get(schemeAloneId) == null){
			dynKeyTable.put(schemeAloneId, HBKeyGroupUtil.getPk_dynKeyValue(subKeyGroupVO, schemeVO.getPk_keygroup()));
		}
		return dynKeyTable;
	}
	
	
	/**
	 * USE:取当期数的meavo
	 * @throws BusinessException 
	 */
	public Hashtable<String,Hashtable<String, String>> getOffSets(HBSchemeVO schemeVO,ContrastQryVO qryVO){
		//时间偏移量为0，直接取原来的值即可
		Map<String, String> off = qryVO.getKeymap();
		Hashtable<String, String> tmp = new Hashtable<String, String>();
		tmp.putAll(off);
		OffSetTable.put(qryVO.getPkLock(), tmp);
		return OffSetTable;
	}
	
	@SuppressWarnings("unchecked")
	private MeasurePubDataVO[] getMeasurePubDataVOCaches(KeyGroupVO subKeyGroupVO,HBSchemeVO schemeVO,ContrastQryVO qryVO,String schemeAloneid) {
		//zhaojian8 多线程对账存在取数不完整的问题
		try {
			String[] contrastorgs = null;
			Map<String, String> org_supplier_map = null;
			//Modified by sunzeg 2017.6.15 通过对账对查询而不是通过两个in集合_begin
			HashSet<String> selfOrgSet = new HashSet<String>();//本方组织
			/*HashSet<String> oppOrgSet = new HashSet<String>();//对方组织
			HashSet<String> orgset = new HashSet<String>();//所有组织		
*/			//Modified by sunzeg 2017.6.12 简化对账对
			//contrastorgs = HBPubItfService.getRemoteContrastSrv().getContrastOrgs(qryVO.getPk_hbrepstru(), qryVO.getContrastorg());
			contrastorgs = qryVO.getAllContrastOrgs();
			List<String[]> selfOppOrgs = new ArrayList<String[]>();	
			org_supplier_map = qryVO.getOrg_supplier_map();
			//动态区关键字pk 目前支持一个动态区关键字
			String pk_dynkeyword = getPk_dynKeyValue(subKeyGroupVO.getKeyGroupPK(),schemeVO,schemeAloneid).get(schemeAloneid);
			for (String str : contrastorgs) {
				String pk_self = str.trim().substring(0, 20);
				selfOrgSet.add(pk_self);
				String pk_other = str.trim().substring(20, 40);
				//oppOrgSet.add(pk_other);
				//orgset.add(pk_self);
				//.add(pk_other);
				//Added by sunzeg 2017.6.13 结构化对账对
				String[] contrastPair = new String[2];
				contrastPair[0] = pk_self;
				if(!pk_dynkeyword.equals(KeyVO.DIC_CORP_PK)){
					contrastPair[1] = org_supplier_map.get(pk_other);
				}else{
					contrastPair[1] = pk_other;
				}
				
				selfOppOrgs.add(contrastPair);
			}
			//Modified by sunzeg 2017.6.15 通过对账对查询而不是通过两个in集合_end
			

			KeyVO[] subkeys = null;
			if(subKeyGroupVO != null)
				subkeys = subKeyGroupVO.getKeys();
		
			
			
			Hashtable<String, String> currOffset = OffSetTable.get(schemeAloneid);
			if(null != subkeys && null != currOffset ){
				String[] keys = new String[currOffset.size()];
				currOffset.keySet().toArray(keys);
	
				String keytime = null;
				for(int i = 0 ; i < subkeys.length ; i++){
					String keyvalue = currOffset.get(subkeys[i].getPk_keyword());
					if(subkeys[i].isTTimeKeyVO()){
						keytime = subkeys[i].getPk_keyword();
						keytimeTable.put(schemeAloneid, keyvalue);
						break;
					}
				}		
			    //Modified by sunzeg 2017.6.15 通过对账对查询而不是通过两个in集合
				/*//查询预置的MeasurePubDataVO，没有查询币种关键字的不正确，内部客商的也有问题
				//条件中应该列上动态区的所有关键字
				//找出动态区的关键字
				StringBuffer buf = new StringBuffer();
				for(int i = 0 ; i < subkeys.length ; i++){
					String pk_keyword = subkeys[i].getPk_keyword();
					if(pk_keyword.equals(KeyVO.CORP_PK)){
						StringBuffer buf1 = new StringBuffer();
						buf1.append("keyword");
						buf1.append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.CORP_PK)+1);
						//zhaojian8
//						String selforg = UFOCSqlUtil.buildInSql(buf1.toString(), qryVO.getSelfOrgs());
						String selforg = UFOCSqlUtil.buildInSql(buf1.toString(), selfOrgSet);
						buf.append(selforg);
						buf.append(" and ");
					}else if(pk_keyword.equals(pk_dynkeyword)){
						StringBuffer buf2 = new StringBuffer();
						buf2.append("keyword");
						buf2.append(subKeyGroupVO.getIndexByKeywordPK(pk_dynkeyword)+1);
						if(pk_dynkeyword.equals(KeyVO.DIC_CORP_PK)){
							//zhaojian8
//							buf.append(UFOCSqlUtil.buildInSql(buf2.toString(), qryVO.getOppOrgs()));
							buf.append(UFOCSqlUtil.buildInSql(buf2.toString(), oppOrgSet));
						}else{
//							buf.append(UFOCSqlUtil.buildInSql(buf2.toString(), qryVO.getOrg_supplier_map().values()));
							buf.append(UFOCSqlUtil.buildInSql(buf2.toString(), org_supplier_map.values()));
						}
						buf.append(" and ");
					}else if(subkeys[i].isTTimeKeyVO()){
						buf.append("keyword");
						buf.append(subKeyGroupVO.getIndexByKeywordPK(keytime)+1);
						buf.append(" = '" + keytimeTable.get(schemeAloneid) + "' and ");
					}else{
						buf.append("keyword");
						buf.append(subKeyGroupVO.getIndexByKeywordPK(pk_keyword)+1);
						buf.append(" = '" + qryVO.getKeymap().get(pk_keyword) + "' and ");
					}
				}
				buf.append(" ver = 0");	*/			
				//Modified by sunzeg 2017.6.14 临时表存储对账对
				/*String tempTableName = NCLocator.getInstance().lookup(IContrast.class).createTempTablebyContrastOrgs(selfOppOrgs);	
				if(tempTableName == null){
					throw new BusinessException("创建对账对临时表失败");
				}*/
				//String whereSql = "(keyword1,keyword2) in (select keyword1, keyword2 from iufo_temp_contrast_org)";
				//String tempTable = NCLocator.getInstance().lookup(IContrast.class).createTempTablebyContrastOrgs(selfOppOrgs);
				//String whereSql = "(keyword1,keyword3) in (select self_org, opp_org from " + tempTable + ")";
				//一次查询in条件中对账对的数目
				int inSize = 500;
				//查询次数
				int queryTimes = selfOppOrgs.size()/inSize;
				//分割对账对后查询
				List<String[]> subSelfOppOrgs = null;
				List<MeasurePubDataVO> simplifiedPubDatas = new ArrayList<MeasurePubDataVO>();
				MeasurePubDataVO[] subPubDatas = null;	
				String whereSql = null;
				for(int i = 0; i < queryTimes; i++){
					subSelfOppOrgs = selfOppOrgs.subList(i * inSize, (i + 1) * inSize);
					whereSql  = buildSql(subkeys, subKeyGroupVO, pk_dynkeyword, keytime, subSelfOppOrgs, schemeAloneid, qryVO.getKeymap());
					subPubDatas = MeasurePubDataBO_Client.findBySqlCondition(subKeyGroupVO.getKeyGroupPK(), whereSql);
					simplifiedPubDatas.addAll(Arrays.asList(subPubDatas));
				}
				//处理余下的
				subSelfOppOrgs = selfOppOrgs.subList(queryTimes * inSize, selfOppOrgs.size());		
				if(subSelfOppOrgs.size() > 0){
					whereSql  = buildSql(subkeys, subKeyGroupVO, pk_dynkeyword, keytime, subSelfOppOrgs, schemeAloneid, qryVO.getKeymap());
					subPubDatas = MeasurePubDataBO_Client.findBySqlCondition(subKeyGroupVO.getKeyGroupPK(), whereSql);
					simplifiedPubDatas.addAll(Arrays.asList(subPubDatas));
				}				
				//MeasurePubDataVO[] findByKeywordArray = MeasurePubDataBO_Client.findBySqlCondition(subKeyGroupVO.getKeyGroupPK(), buf.toString());
				return simplifiedPubDatas.toArray(new MeasurePubDataVO[0]);			
			}
		} catch (BusinessException e) {
			AppDebug.debug(e);
		} catch (Exception e) {
			AppDebug.debug(e);
		}
		return null;
	}
	
	/**
	 * 构建基于对账对的查询条件
	 * @author sunzeg
	 * @param subkeys
	 * @param subKeyGroupVO
	 * @param pk_dynkeyword
	 * @param keytime
	 * @param selfOppOrgs
	 * @param schemeAloneid
	 * @param keyMap
	 * @return
	 */
	private String buildSql(KeyVO[] subkeys, KeyGroupVO subKeyGroupVO, String pk_dynkeyword, String keytime, 
			List<String[]> selfOppOrgs, String schemeAloneid, Map<String, String> keyMap){
		
		final String KEYWORD = "keyword";
		StringBuffer whereSQL = new StringBuffer();
		//拼接where后的查询字段
		StringBuffer keywordSQL = new StringBuffer("(");
		for(int i = 0 ; i < subkeys.length ; i++){
			String pk_keyword = subkeys[i].getPk_keyword();
			if(pk_keyword.equals(KeyVO.CORP_PK)){
				keywordSQL.append(KEYWORD).append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.CORP_PK)+1);
			}else if(pk_keyword.equals(pk_dynkeyword)){
				//还需要适配客商
				keywordSQL.append(",").append(KEYWORD).append(subKeyGroupVO.getIndexByKeywordPK(pk_dynkeyword)+1);
			}	
		}
		keywordSQL.append(")");
		
		//拼接in条件
		StringBuffer inSQL = new StringBuffer(" in ("); 
		for(int i = 0; i < selfOppOrgs.size(); i++){
			String[] contrastOrg = selfOppOrgs.get(i);
			if(i != 0){
				inSQL.append(",");
			}
			inSQL.append("(\'");
			inSQL.append(contrastOrg[0]);
			inSQL.append("\',\'");
			inSQL.append(contrastOrg[1]);
			inSQL.append("\')");
		}
		inSQL.append(")");
		
		StringBuffer otherSQL = new StringBuffer();
		for(int i = 0 ; i < subkeys.length ; i++){
			String pk_keyword = subkeys[i].getPk_keyword();
			if(pk_keyword.equals(KeyVO.CORP_PK) || pk_keyword.equals(pk_dynkeyword)){
				continue;
			}
			if(subkeys[i].isTTimeKeyVO()){
				otherSQL.append(" and ").append(KEYWORD).append(subKeyGroupVO.getIndexByKeywordPK(keytime)+1);
				otherSQL.append("=\'").append(keytimeTable.get(schemeAloneid)).append("\'");
			}else{
				otherSQL.append(" and ").append(KEYWORD).append(subKeyGroupVO.getIndexByKeywordPK(pk_keyword)+1);
				otherSQL.append("= \'").append(keyMap.get(pk_keyword)).append("\'");
			}			
		}
		otherSQL.append(" and ver=0");
		
		whereSQL.append(keywordSQL).append(inSQL).append(otherSQL);
		return whereSQL.toString();
	}
	/**
	 * 中免
	 * @param subKeyGroupVO
	 * @param schemeVO
	 * @param qryVO
	 * @param schemeAloneid
	 * @param pk_otherDynKey
	 * @return
	 */
	private MeasurePubDataVO[] get2MeasurePubDataVOCaches(KeyGroupVO subKeyGroupVO,HBSchemeVO schemeVO,ContrastQryVO qryVO,String schemeAloneid, String pk_otherDynKey) {
		KeyVO[] subkeys = null;
		if(subKeyGroupVO != null)
			subkeys = subKeyGroupVO.getKeys();
		
		//动态区关键字pk 目前支持一个动态区关键字
		String[] pk_dynkeywords = getPk_dynKeyValues(subKeyGroupVO,schemeVO,schemeAloneid).get(schemeAloneid);
		String pk_dyncorp = KeyVO.DIC_CORP_PK;
		for(String pk: pk_dynkeywords){
			if(!pk.equals(pk_otherDynKey)){
				pk_dyncorp = pk;
				break;
			}
		}
		
		Hashtable<String, String> currOffset = OffSetTable.get(schemeAloneid);
		if(null != subkeys && null != currOffset ){
			String[] keys = new String[currOffset.size()];
			currOffset.keySet().toArray(keys);

			String keytime = null;
			for(int i = 0 ; i < subkeys.length ; i++){
				String keyvalue = currOffset.get(subkeys[i].getPk_keyword());
				if(subkeys[i].isTTimeKeyVO()){
					keytime = subkeys[i].getPk_keyword();
					keytimeTable.put(schemeAloneid, keyvalue);
					break;
				}
			}
			
			try {
				//查询预置的MeasurePubDataVO，没有查询币种关键字的不正确，内部客商的也有问题
				//条件中应该列上动态区的所有关键字
				//找出动态区的关键字
				StringBuffer buf = new StringBuffer();
				for(int i = 0 ; i < subkeys.length ; i++){
					String pk_keyword = subkeys[i].getPk_keyword();
					if(pk_keyword.equals(KeyVO.CORP_PK)){
						StringBuffer buf1 = new StringBuffer();
						buf1.append("keyword");
						buf1.append(subKeyGroupVO.getIndexByKeywordPK(KeyVO.CORP_PK)+1);
						String selforg = UFOCSqlUtil.buildInSql(buf1.toString(), qryVO.getSelfOrgs());

						buf.append(selforg);
						buf.append(" and ");
					}else if(pk_keyword.equals(pk_dyncorp)){
						StringBuffer buf2 = new StringBuffer();
						buf2.append("keyword");
						buf2.append(subKeyGroupVO.getIndexByKeywordPK(pk_dyncorp)+1);
						if(pk_dyncorp.equals(KeyVO.DIC_CORP_PK)){
							buf.append(UFOCSqlUtil.buildInSql(buf2.toString(), qryVO.getOppOrgs()));
						}else{
							buf.append(UFOCSqlUtil.buildInSql(buf2.toString(), qryVO.getOrg_supplier_map().values()));
						}
						buf.append(" and ");
					}else if(subkeys[i].isTTimeKeyVO()){
						buf.append("keyword");
						buf.append(subKeyGroupVO.getIndexByKeywordPK(keytime)+1);
						buf.append(" = '" + keytimeTable.get(schemeAloneid) + "' and ");
					}else{
						buf.append("keyword");
						buf.append(subKeyGroupVO.getIndexByKeywordPK(pk_keyword)+1);
						buf.append(" = '" + qryVO.getKeymap().get(pk_keyword) + "' and ");
					}
				}
				buf.append(" ver = 0");
				MeasurePubDataVO[] findByKeywordArray = MeasurePubDataBO_Client.findBySqlCondition(subKeyGroupVO.getKeyGroupPK(), buf.toString());
				return findByKeywordArray;
			} catch (Exception e) {
				AppDebug.debug(e);
			}
		}
		return null;
	}
	/**
	 * USE：取预置的内部交易表的MeasurePubDataVO全部取当期的，多关键字
	 * 中免 
	 * @param subKeyGroupVO
	 * @param schemeVO
	 * @param qryVO
	 * @param schemeAloneId
	 * @return
	 * @throws BusinessException
	 */
	public Hashtable<String,Hashtable<String,MeasurePubDataVO>> get2AllRelaMeasPubVOs(KeyGroupVO subKeyGroupVO,HBSchemeVO schemeVO,ContrastQryVO qryVO, String pk_otherDynKey) throws BusinessException {
		String schemeAloneId = qryVO.getPkLock();
		Hashtable<String, MeasurePubDataVO> currPubDatas = measurePubDataVOTable.get(schemeAloneId+qryVO.getKeymap().get(pk_otherDynKey));
		if (currPubDatas == null) {
			return construct2Cache(subKeyGroupVO,schemeVO,qryVO,schemeAloneId,pk_otherDynKey);
		}else{
			//单位 客商；单位 供应商，同一个合并方案中有不同动态区关键字的表存在
			if(dynGroupTable.get(schemeAloneId) != null && !subKeyGroupVO.equals(dynGroupTable.get(schemeAloneId))){
				MeasurePubDataVO[] vos = get2MeasurePubDataVOCaches(subKeyGroupVO,schemeVO,qryVO,schemeAloneId,pk_otherDynKey);
				if(vos != null){
					//动态区关键字pk 目前支持一个动态区关键字
					String[] pk_dynkeyword = getPk_dynKeyValues(subKeyGroupVO,schemeVO,schemeAloneId).get(schemeAloneId);
					for(MeasurePubDataVO vo : vos){
						if(vo != null){
							StringBuffer keybuffer = new StringBuffer();
							keybuffer.append(vo.getKeywordByPK(KeyVO.CORP_PK));
							for(String pk : pk_dynkeyword){
								if(pk!=pk_otherDynKey){
									keybuffer.append(vo.getKeywordByPK(pk));
									break;
								}
							}
							keybuffer.append(vo.getKeywordByPK(pk_otherDynKey));
							keybuffer.append(keytimeTable.get(schemeAloneId));//当期数
							keybuffer.append(vo.getKType());
							if(currPubDatas.get(keybuffer.toString()) == null)
								currPubDatas.put(keybuffer.toString(), vo);
						}
					}
				}
			}
			dynGroupTable.put(schemeAloneId, subKeyGroupVO);
		}
		return measurePubDataVOTable;
	}
	
	
	/**
	 * 中免
	 * @param subKeyGroupVO
	 * @param schemeVO
	 * @param qryVO
	 * @param schemeAloneId
	 * @return
	 */
	private Hashtable<String,Hashtable<String,MeasurePubDataVO>> construct2Cache(KeyGroupVO subKeyGroupVO,HBSchemeVO schemeVO,ContrastQryVO qryVO,String schemeAloneId, String pk_otherDynKey) {
		dynGroupTable.put(schemeAloneId, subKeyGroupVO);
		Hashtable<String, MeasurePubDataVO> currMeasureTable = measurePubDataVOTable.get(schemeAloneId+qryVO.getKeymap().get(pk_otherDynKey));
		if (currMeasureTable == null) {
			synchronized (lockObj1) {
				currMeasureTable = new Hashtable<String, MeasurePubDataVO>();
				MeasurePubDataVO[] vos = get2MeasurePubDataVOCaches(subKeyGroupVO,schemeVO,qryVO,schemeAloneId,pk_otherDynKey);
				if(vos != null){
					//动态区关键字pk 目前支持2个动态区关键字
					String[] pk_dynkeyword = getPk_dynKeyValues(subKeyGroupVO,schemeVO,schemeAloneId).get(schemeAloneId);
					for(MeasurePubDataVO vo : vos){
						if(vo != null){
							StringBuffer keybuffer = new StringBuffer();
							keybuffer.append(vo.getKeywordByPK(KeyVO.CORP_PK));
							for(String pk : pk_dynkeyword){
								if(pk!=pk_otherDynKey){
									keybuffer.append(vo.getKeywordByPK(pk));
									break;
								}
							}
							keybuffer.append(vo.getKeywordByPK(pk_otherDynKey));
							keybuffer.append(keytimeTable.get(schemeAloneId));//当期数
							keybuffer.append(vo.getKType());
							if(currMeasureTable.get(keybuffer.toString()) == null)
								currMeasureTable.put(keybuffer.toString(), vo);
						}
					}
				}
			}
			measurePubDataVOTable.put(schemeAloneId+qryVO.getKeymap().get(pk_otherDynKey), currMeasureTable);
		} 
		return measurePubDataVOTable;
	}
	
	/**动态区关键字pk*/
	private Hashtable<String,String[]> dynKeysTable = new Hashtable<String, String[]>();
	/**
	 * USE:取动态区关键字主键
	 * 中免
	 * @return
	 */
	public Hashtable<String,String[]> getPk_dynKeyValues(KeyGroupVO subKeyGroupVO,HBSchemeVO schemeVO,String schemeAloneId) {
		if(dynKeysTable.get(schemeAloneId) == null){
			dynKeysTable.put(schemeAloneId, HBKeyGroupUtil.getPk_dynKeyValues(subKeyGroupVO, schemeVO.getPk_keygroup()));
		}
		return dynKeysTable;
	}
	
}
