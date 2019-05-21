package nc.itf.hbbb.contrast;

import java.util.Hashtable;
import java.util.Map;

import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.HBKeyGroupUtil;
import nc.util.hbbb.UFOCSqlUtil;
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
	
	/**动态区关键字pk*/
	private Hashtable<String,String[]> dynKeysTable = new Hashtable<String, String[]>();
	
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
	
	private MeasurePubDataVO[] getMeasurePubDataVOCaches(KeyGroupVO subKeyGroupVO,HBSchemeVO schemeVO,ContrastQryVO qryVO,String schemeAloneid) {
		KeyVO[] subkeys = null;
		if(subKeyGroupVO != null)
			subkeys = subKeyGroupVO.getKeys();
		
		//动态区关键字pk 目前支持一个动态区关键字
		String pk_dynkeyword = getPk_dynKeyValue(subKeyGroupVO.getKeyGroupPK(),schemeVO,schemeAloneid).get(schemeAloneid);
		
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
					}else if(pk_keyword.equals(pk_dynkeyword)){
						StringBuffer buf2 = new StringBuffer();
						buf2.append("keyword");
						buf2.append(subKeyGroupVO.getIndexByKeywordPK(pk_dynkeyword)+1);
						if(pk_dynkeyword.equals(KeyVO.DIC_CORP_PK)){
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
}
