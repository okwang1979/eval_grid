package nc.util.hbbb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nc.bs.framework.common.NCLocator;
import nc.itf.hbbb.report.IHBMeasurPubDataQrySrv;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.vouch.StockInvestRelaVersionVO;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.verctrl.IDataVersionConsts;
import nc.vo.pub.BusinessException;
import nc.vo.ufoc.conver.ConvertRuleVO;

import com.ufida.iufo.pub.tools.AppDebug;

public class HBAloneIDUtil {
	
	Map<String,String> keygroup_aloneid_map = new ConcurrentHashMap<String,String>();
	
	/**
	 * 折算根据对方币种重新设置ALONEID
	 * @param pk_report
	 * @param datavos
	 * @param pubdata
	 * @param pk_currtype
	 * @return
	 */
	public MeasureDataVO[] resetAloneId(String pk_report,MeasureDataVO[] datavos,MeasurePubDataVO pubdata,ConvertRuleVO rulevo){
		MeasureDataVO[]  result=datavos;
		if(null==result || result.length==0){
			return result;
		}
		boolean intrade=HBBBReportUtil.isIntrateRep(pk_report);
		
		if(intrade){
			//内部交易表de aloneid按照关键组合分组,因为在内部交易表中与对方单位发生的数据是有对方单位,
			//A单位对B,C单位的,关键字一样,但是生成的ALONE_ID不一样
			//而比如合计行,的关键字失少了对方单位的,所以此处按照指标的关键字comb分组,add by wangxwb
			for(MeasureDataVO vo:result){
				//一个单位对应多个折算规则 第一个规则时keygroup_aloneid_map<pk+原币的aloneid,目的币的aloneid>,
				//当第二个规则过来时map中的key已经存在，但此时目的币种变了，再get原来的map会得到上一个规则的目的aloneid
				//出现两个规则的aloneid相同的情况: key增加折算规则pk
				if(vo.getMeasureVO().getKeyCombPK().equals(pubdata.getKeyGroup().getKeyGroupPK())){ //关键子组合没变化
					if(keygroup_aloneid_map.containsKey(rulevo.getPk_convertrule()+vo.getMeasureVO().getKeyCombPK()+vo.getAloneID())){
						vo.setAloneID(keygroup_aloneid_map.get(rulevo.getPk_convertrule()+vo.getMeasureVO().getKeyCombPK()+vo.getAloneID()));
					}else{
						MeasurePubDataVO newpubdata = null;
						try {
							newpubdata = MeasurePubDataBO_Client.findByAloneID(pubdata.getKeyGroup().getKeyGroupPK(),pubdata.getAloneID());
							newpubdata.setKeywordByPK(KeyVO.COIN_PK,rulevo.getDescurrtype());
							newpubdata.setAloneID(null);
							String aloneid=MeasurePubDataBO_Client.getAloneID(newpubdata);
							keygroup_aloneid_map.put(rulevo.getPk_convertrule()+vo.getMeasureVO().getKeyCombPK()+vo.getAloneID(),aloneid);
							vo.setAloneID(aloneid);
						} catch (Exception e) {
							AppDebug.debug(e);
						}
					}
				}else{
					//如果是内部交易表,则此时关键组合应与不同,因为少一个对方单位
					if(keygroup_aloneid_map.containsKey(rulevo.getPk_convertrule()+vo.getMeasureVO().getKeyCombPK()+vo.getAloneID())){
						vo.setAloneID(keygroup_aloneid_map.get(rulevo.getPk_convertrule()+vo.getMeasureVO().getKeyCombPK()+vo.getAloneID()));
					}else{
						MeasurePubDataVO newpubdata = null;
						try {
							newpubdata = MeasurePubDataBO_Client.findByAloneID(vo.getMeasureVO().getKeyCombPK(),vo.getAloneID());
							newpubdata.setKeywordByPK(KeyVO.COIN_PK,rulevo.getDescurrtype());
							newpubdata.setAloneID(null);
							String aloneid=MeasurePubDataBO_Client.getAloneID(newpubdata);
							keygroup_aloneid_map.put(rulevo.getPk_convertrule()+vo.getMeasureVO().getKeyCombPK()+vo.getAloneID(),aloneid);
							vo.setAloneID(aloneid);
						} catch (Exception e) {
							AppDebug.debug(e);
						}	
					}
				}
		    }
		}else{
		    for(MeasureDataVO vo:result){
		    	vo.setAloneID(pubdata.getAloneID());
		    }
			return result;
		}
		return result;
	}
	
	
	/**
	 * 成本法转权益法调整用
	 * @param new_qryvo
	 * @param bFromDB
	 * @param pk_org
	 * @return
	 */
	 public static String getCost2RightAdjustVoucherAlone_id(ContrastQryVO new_qryvo, boolean bFromDB,String pk_org){
	    	MeasurePubDataVO pubdata = new MeasurePubDataVO();
	    	 KeyGroupVO keyGroupVo = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(new_qryvo.getSchemevo().getPk_keygroup());
			pubdata.setKType(keyGroupVo.getKeyGroupPK());
			pubdata.setKeyGroup(keyGroupVo);
//			pubdata.setKeywords(keywordValues);
			if(null!=new_qryvo.getKeymap() && new_qryvo.getKeymap().size()>0){
				String[] keys=new String[new_qryvo.getKeymap().size()];
				new_qryvo.getKeymap().keySet().toArray(keys);
				if(null!=keys && keys.length>0){
					for(String key:keys){
							pubdata.setKeywordByPK(key, new_qryvo.getKeymap().get(key));
					}
				}
			}
			
			
			pubdata.setKeywordByPK(KeyVO.CORP_PK, /*new_qryvo.getContrastorg()*/pk_org);
			
			pubdata.setVer(/*new_qryvo.getSchemevo().getVersion());*/IDataVersionConsts.VER_VOUCHER);/*); 
	*/		String strAloneID = "";
			try {
				if(bFromDB)
					strAloneID = MeasurePubDataBO_Client.getAloneID(pubdata);
				else
					strAloneID = MeasureDataUtil.getAloneID(pubdata);
			} catch (Exception e) {
				AppDebug.debug(e);
			}
			return strAloneID;
	    }
	
    /**
     * 获得成本法转权益法aloneID
     * 
     * @param new_qryvo
     * @param bFromDB
     * @param pk_org
     * @param isHb
     * @return
     */
    public static String getCost2RightAdjustVoucherAlone_id(ContrastQryVO new_qryvo, boolean bFromDB, String pk_org,
            boolean isHb) {

        MeasurePubDataVO pubdata = new MeasurePubDataVO();

        // 关键字组合
        KeyGroupVO keyGroupVo = IUFOCacheManager.getSingleton().getKeyGroupCache()
                .getByPK(new_qryvo.getSchemevo().getPk_keygroup());
        pubdata.setKType(keyGroupVo.getKeyGroupPK());
        pubdata.setKeyGroup(keyGroupVo);

        // 关键字值
        if (new_qryvo.getKeymap() != null && new_qryvo.getKeymap().size() > 0) {
            String[] keys = new String[new_qryvo.getKeymap().size()];
            new_qryvo.getKeymap().keySet().toArray(keys);
            if (null != keys && keys.length > 0) {
                for (String key : keys) {
                    pubdata.setKeywordByPK(key, new_qryvo.getKeymap().get(key));
                }
            }
        }
        pubdata.setKeywordByPK(KeyVO.CORP_PK, pk_org);

        // 版本
        if (isHb) {
            pubdata.setVer(IDataVersionConsts.VER_VOUCHER + IDataVersionConsts.VERTYPE_HBBB_ADJUST);
        } else {
            pubdata.setVer(IDataVersionConsts.VER_VOUCHER);
        }

        // 获得AloneID
        String strAloneID = "";
        try {
            if (bFromDB) {
                strAloneID = MeasurePubDataBO_Client.getAloneID(pubdata);
            } else {
                strAloneID = MeasureDataUtil.getAloneID(pubdata);
            }
        } catch (Exception e) {
            AppDebug.debug(e);
        }
        return strAloneID;
    }
    
    /**
     * 批量获得成本法转权益法aloneID
     * 
     * @param new_qryvo
     * @param bFromDB
     * @param investrelavos
     * @param isHb
     * @return
     */
    public static String[] getCost2RightAdjustVoucherAlone_id(ContrastQryVO new_qryvo, boolean bFromDB,
            StockInvestRelaVersionVO[] investrelavos, boolean isHb) {

        MeasurePubDataVO pubdata = new MeasurePubDataVO();

        // 关键字组合
        KeyGroupVO keyGroupVo = IUFOCacheManager.getSingleton().getKeyGroupCache()
                .getByPK(new_qryvo.getSchemevo().getPk_keygroup());
        pubdata.setKType(keyGroupVo.getKeyGroupPK());
        pubdata.setKeyGroup(keyGroupVo);

        // 关键字值
        if (new_qryvo.getKeymap() != null && new_qryvo.getKeymap().size() > 0) {
            String[] keys = new String[new_qryvo.getKeymap().size()];
            new_qryvo.getKeymap().keySet().toArray(keys);
            if (null != keys && keys.length > 0) {
                for (String key : keys) {
                    pubdata.setKeywordByPK(key, new_qryvo.getKeymap().get(key));
                }
            }
        }

        // 版本
        if (isHb) {
            pubdata.setVer(IDataVersionConsts.VER_VOUCHER + IDataVersionConsts.VERTYPE_HBBB_ADJUST);
        } else {
            pubdata.setVer(IDataVersionConsts.VER_VOUCHER);
        }

        // 获得AloneID
        String[] aloneIDs = new String[investrelavos.length];
        try {
            for (int i = 0; i < investrelavos.length; i++) {
                aloneIDs[i] = "";
                pubdata.setKeywordByPK(KeyVO.CORP_PK, investrelavos[i].getInvestor());
                if (bFromDB) {
                    aloneIDs[i] = MeasurePubDataBO_Client.getAloneID(pubdata);
                } else {
                    aloneIDs[i] = MeasureDataUtil.getAloneID(pubdata);
                }
            }
        } catch (Exception e) {
            AppDebug.debug(e);
        }
        return aloneIDs;
    }
    
	 public static String getAdjustVoucherAlone_id(ContrastQryVO new_qryvo, boolean bFromDB){
		 return NCLocator.getInstance().lookup(IHBMeasurPubDataQrySrv.class).getAdjustVoucherAlone_id(new_qryvo, bFromDB);
	 }
	
	 
	    /**
	     * 取动态区域指标用
	     * @param pk_org
	     * @param pk_report
	     * @param keyMap
	     * @param pk_other_org
	     * @return
	     * @throws BusinessException
	     */
		public static  String  getAloneID(String pk_org,String pk_keygroup ,Map<String, String> keyMap,String pk_other_org,String pk_dynvalue) throws BusinessException{
			String result="";
			MeasurePubDataVO pubdata  = new MeasurePubDataVO();
	
			pubdata.setKType(pk_keygroup);
			KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
			pubdata.setKeyGroup(keygroupVo);
			KeyVO[] keyvos=keygroupVo.getKeys();
			if(null!=keyvos && null!=keyMap /*&& keyvos.length==keyMap.size()*/){
				String[] keys=new String[keyMap.size()];
				keyMap.keySet().toArray(keys);
				if(null!=keys && keys.length>0){
					for(String key:keys){
							pubdata.setKeywordByPK(key, keyMap.get(key));
					}
				}
				pubdata.setKeywordByPK(KeyVO.CORP_PK,pk_org);
				pubdata.setKeywordByPK(pk_dynvalue,pk_other_org);
				pubdata.setVer(0);
			    try {
					result=MeasurePubDataBO_Client.getAloneID(pubdata);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					nc.bs.logging.Logger.error(e.getMessage(), e);
					throw new BusinessException(e);
				}
			}
			
			return result;
		}
		
	
		/**
	     * 取动态区域指标用
	     * @param pk_org
	     * @param pk_report
	     * @param keyMap
	     * @param pk_other_org
	     * @return
	     * @throws BusinessException
	     */
		public static  String  findAloneID(String pk_org,String pk_keygroup ,Map<String, String> keyMap,String pk_other_org,String pk_dynvalue) throws BusinessException{
			MeasurePubDataVO result=null;
			MeasurePubDataVO pubdata  = new MeasurePubDataVO();
	
			pubdata.setKType(pk_keygroup);
			KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
			pubdata.setKeyGroup(keygroupVo);
			KeyVO[] keyvos=keygroupVo.getKeys();
			if(null!=keyvos && null!=keyMap /*&& keyvos.length==keyMap.size()*/){
				String[] keys=new String[keyMap.size()];
				keyMap.keySet().toArray(keys);
				if(null!=keys && keys.length>0){
					for(String key:keys){
							pubdata.setKeywordByPK(key, keyMap.get(key));
					}
				}
				pubdata.setKeywordByPK(KeyVO.CORP_PK,pk_org);
				pubdata.setKeywordByPK(pk_dynvalue,pk_other_org);
				pubdata.setVer(0);
			    try {
					result=MeasurePubDataBO_Client.findByKeywords(pubdata);
					if(result==null){
						return null;
					}else {
						return  result.getAloneID();
					}
				} catch (Exception e) {
					nc.bs.logging.Logger.error(e.getMessage(), e);
					throw new BusinessException(e);
				}
			}
			
			return null;
		}
	
	public static  String  getAloneID(String pk_org,String pk_keygroup ,Map<String, String> keyMap) throws BusinessException{
		String result="";
		MeasurePubDataVO pubdata  = new MeasurePubDataVO();
	

		
		pubdata.setKType(pk_keygroup);
		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
		pubdata.setKeyGroup(keygroupVo);
		KeyVO[] keyvos=keygroupVo.getKeys();
		if(null!=keyvos && null!=keyMap && keyvos.length==keyMap.size()){
			String[] keys=new String[keyMap.size()];
			keyMap.keySet().toArray(keys);
			if(null!=keys && keys.length>0){
				for(String key:keys){
						pubdata.setKeywordByPK(key, keyMap.get(key));
				}
			}
			pubdata.setKeywordByPK(KeyVO.CORP_PK,pk_org);
			pubdata.setVer(0);
		    try {
				result=MeasurePubDataBO_Client.getAloneID(pubdata);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
				throw new BusinessException(e);
			}
		}
		
		return result;
	}
	public static  String  findAloneID(String pk_org,String pk_keygroup ,Map<String, String> keyMap) throws BusinessException{
		MeasurePubDataVO resultpubdata=null;
		MeasurePubDataVO pubdata  = new MeasurePubDataVO();
		pubdata.setKType(pk_keygroup);
		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
		pubdata.setKeyGroup(keygroupVo);
		KeyVO[] keyvos=keygroupVo.getKeys();
		if(null!=keyvos && null!=keyMap && keyvos.length==keyMap.size()){
			String[] keys=new String[keyMap.size()];
			keyMap.keySet().toArray(keys);
			if(null!=keys && keys.length>0){
				for(String key:keys){
						pubdata.setKeywordByPK(key, keyMap.get(key));
				}
			}
			pubdata.setKeywordByPK(KeyVO.CORP_PK,pk_org);
			pubdata.setVer(0);
		    try {
		    	resultpubdata=MeasurePubDataBO_Client.findByKeywords(pubdata);
			} catch (Exception e) {
				nc.bs.logging.Logger.error(e.getMessage(), e);
				throw new BusinessException(e);
			}
			if(resultpubdata==null){
				return null;
			}else{
				return resultpubdata.getAloneID();
			}
		}
		return null;
	}
	
	
	public static  String  findAloneID(String pk_org,String pk_keygroup ,Map<String, String> keyMap,int version) throws BusinessException{
		MeasurePubDataVO resultpubdata=null;
		MeasurePubDataVO pubdata  = new MeasurePubDataVO();
		pubdata.setKType(pk_keygroup);
		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
		pubdata.setKeyGroup(keygroupVo);
		KeyVO[] keyvos=keygroupVo.getKeys();
		if(null != keyvos && null != keyMap){
			String[] keys = new String[keyMap.size()];
			keyMap.keySet().toArray(keys);
			if(null!=keys && keys.length>0){
				for(String key:keys){
					pubdata.setKeywordByPK(key, keyMap.get(key));
				}
			}
			pubdata.setKeywordByPK(KeyVO.CORP_PK,pk_org);
			pubdata.setVer(version);
		    try {
		    	resultpubdata=MeasurePubDataBO_Client.findByKeywords(pubdata);
			} catch (Exception e) {
				nc.bs.logging.Logger.error(e.getMessage(), e);
				throw new BusinessException(e);
			}
			if(resultpubdata==null){
				return null;
			}else{
				return resultpubdata.getAloneID();
			}
		}
		return null;
	}
	
	
	public static  String  getAloneID(String pk_org,Map<String, String> keyMap,String pk_key_comb,java.lang.Integer version) throws BusinessException{
		String result="";
		MeasurePubDataVO pubdata  = new MeasurePubDataVO();
		pubdata.setKType(pk_key_comb);
		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_key_comb);
		pubdata.setKeyGroup(keygroupVo);
		KeyVO[] keyvos=keygroupVo.getKeys();
		if(null!=keyvos && null!=keyMap &&keyMap.size() == keyvos.length ){
			String[] keys=new String[keyMap.size()];
			keyMap.keySet().toArray(keys);
			if(null!=keys && keys.length>0){
				for(String key:keys){
						pubdata.setKeywordByPK(key, keyMap.get(key));
				}
			}
			pubdata.setKeywordByPK(KeyVO.CORP_PK,pk_org);
			pubdata.setVer(version);
		    try {
				result=MeasurePubDataBO_Client.getAloneID(pubdata);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
				throw new BusinessException(e);
			}
		}
		
		return result;
	}


	public static  String findAloneID(String pk_org,Map<String, String> keyMap,String pk_key_comb,java.lang.Integer version) throws BusinessException{
		MeasurePubDataVO result =null ;
		MeasurePubDataVO pubdata  = new MeasurePubDataVO();
		pubdata.setKType(pk_key_comb);
		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_key_comb);
		pubdata.setKeyGroup(keygroupVo);
		KeyVO[] keyvos=keygroupVo.getKeys();
		if(null!=keyvos && null!=keyMap && keyvos.length==keyMap.size()){
			String[] keys=new String[keyMap.size()];
			keyMap.keySet().toArray(keys);
			if(null!=keys && keys.length>0){
				for(String key:keys){
						pubdata.setKeywordByPK(key, keyMap.get(key));
				}
			}
			pubdata.setKeywordByPK(KeyVO.CORP_PK,pk_org);
			pubdata.setVer(version);
		    try {
				result=MeasurePubDataBO_Client.findByKeywords(pubdata);
			} catch (Exception e) {
				nc.bs.logging.Logger.error(e.getMessage(), e);
				throw new BusinessException(e.getMessage());
			}
		}
		if(result!=null)
			return result.getAloneID();
		else
			return null;
	}
	
	public static  String findAloneIDByMath(String pk_org,Map<String, String> keyMap,String pk_key_comb,java.lang.Integer version) throws BusinessException{
		String result =null ;
		MeasurePubDataVO pubdata  = new MeasurePubDataVO();
		pubdata.setKType(pk_key_comb);
		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_key_comb);
		pubdata.setKeyGroup(keygroupVo);
		KeyVO[] keyvos=keygroupVo.getKeys();
		if(null!=keyvos && null!=keyMap && keyvos.length==keyMap.size()){
			String[] keys=new String[keyMap.size()];
			keyMap.keySet().toArray(keys);
			if(null!=keys && keys.length>0){
				for(String key:keys){
						pubdata.setKeywordByPK(key, keyMap.get(key));
				}
			}
			pubdata.setKeywordByPK(KeyVO.CORP_PK,pk_org);
			pubdata.setVer(version);
			result = MeasureDataUtil.getAloneID(pubdata);
		}
		return result;
	}
	
	
	public static  String  findAloneIDByMath(String pk_org,String pk_keygroup ,Map<String, String> keyMap) throws BusinessException{
		String result =null ;
		MeasurePubDataVO pubdata  = new MeasurePubDataVO();
		pubdata.setKType(pk_keygroup);
		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
		pubdata.setKeyGroup(keygroupVo);
		KeyVO[] keyvos=keygroupVo.getKeys();
		if(null!=keyvos && null!=keyMap && keyvos.length==keyMap.size()){
			String[] keys=new String[keyMap.size()];
			keyMap.keySet().toArray(keys);
			if(null!=keys && keys.length>0){
				for(String key:keys){
						pubdata.setKeywordByPK(key, keyMap.get(key));
				}
			}
			pubdata.setKeywordByPK(KeyVO.CORP_PK,pk_org);
			pubdata.setVer(0);
	    	result = MeasureDataUtil.getAloneID(pubdata);
		}
		return result;
	}
	
	public synchronized static int getPeriodIndex(KeyGroupVO keyGroupVO){
		KeyVO[] keys = keyGroupVO.getKeys();
		for (int i = 0; i < keys.length; i++) {
			if (keys[i].getType() == KeyVO.TYPE_ACC||keys[i].getType() == KeyVO.TYPE_TIME) {
				return i+1;
			}
		}
		return -1;
	}

	/**
	 * 根据原币MeasureDataVO得到本期原币与上期原币的aloneid
	 * <本期原币alongid,上期原币alongid>
	 * 
	 * @create by zhoushuang at 2014-3-6,下午2:19:46
	 *
	 * @param thisSrcDataVOs
	 * @param lastPrdSrcPubdata
	 * @return <本期原币alongid,上期原币alongid>
	 */
	public static Map<String, String> getThisSrcToLastSrcMap(MeasureDataVO[] thisSrcDataVOs,String timePk,String lastprdValue){
		Map<String, String>  thisSrcToLastSrcMap= new ConcurrentHashMap<String, String>(); 
		try {
			for (int i = 0; i < thisSrcDataVOs.length; i++) {
				String thisSrcAloneid = thisSrcDataVOs[i].getAloneID();
				if (!thisSrcToLastSrcMap.containsKey(thisSrcAloneid)) {
					MeasurePubDataVO thisSrcPubData = MeasurePubDataBO_Client.findByAloneID(thisSrcDataVOs[i].getMeasureVO().getKeyCombPK(), thisSrcAloneid);
					thisSrcPubData.setKeywordByPK(timePk, lastprdValue);
					thisSrcPubData.setAloneID(null);
					String lastSrcAloneid = MeasurePubDataBO_Client.getAloneID(thisSrcPubData);
					thisSrcToLastSrcMap.put(thisSrcAloneid, lastSrcAloneid);
				}
			}
		} catch (Exception e) {
			AppDebug.debug(e);
		}
		return thisSrcToLastSrcMap;
	}

	/**
	 * 根据原币MeasureDataVO得到本期原币与上期目的币的aloneid
	 * <本期原币alongid,上期目的币alongid>
	 * 
	 * @create by zhoushuang at 2014-3-6,下午2:20:14
	 *
	 * @param thisSrcDataVOs
	 * @param lastPrdDesPubdata
	 * @param desCoinPk
	 * @return <本期原币alongid,上期目的币alongid>
	 */
	public static Map<String, String> getThisSrcToLastDesMap(MeasureDataVO[] thisSrcDataVOs,String timePk,String lastprdValue, String desCoinPk){
		Map<String, String> thisSrcToLastDesMap  = new ConcurrentHashMap<String, String>(); 
		try {
			for (int i = 0; i < thisSrcDataVOs.length; i++) {
				String thisSrcAloneid = thisSrcDataVOs[i].getAloneID();
				if (!thisSrcToLastDesMap.containsKey(thisSrcAloneid)) {
					MeasurePubDataVO thisSrcPubData = MeasurePubDataBO_Client.findByAloneID(thisSrcDataVOs[i].getMeasureVO().getKeyCombPK(), thisSrcAloneid);
					thisSrcPubData.setKeywordByPK(KeyVO.COIN_PK, desCoinPk);
					thisSrcPubData.setKeywordByPK(timePk, lastprdValue);
					thisSrcPubData.setAloneID(null);
					String lastSrcAloneid = MeasurePubDataBO_Client.getAloneID(thisSrcPubData);
					thisSrcToLastDesMap.put(thisSrcAloneid, lastSrcAloneid);
				}
			}
		} catch (Exception e) {
			AppDebug.debug(e);
		}
		return thisSrcToLastDesMap;
	}
	
}
