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
	 * ������ݶԷ�������������ALONEID
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
			//�ڲ����ױ�de aloneid���չؼ���Ϸ���,��Ϊ���ڲ����ױ�����Է���λ�������������жԷ���λ,
			//A��λ��B,C��λ��,�ؼ���һ��,�������ɵ�ALONE_ID��һ��
			//������ϼ���,�Ĺؼ���ʧ���˶Է���λ��,���Դ˴�����ָ��Ĺؼ���comb����,add by wangxwb
			for(MeasureDataVO vo:result){
				//һ����λ��Ӧ���������� ��һ������ʱkeygroup_aloneid_map<pk+ԭ�ҵ�aloneid,Ŀ�ıҵ�aloneid>,
				//���ڶ����������ʱmap�е�key�Ѿ����ڣ�����ʱĿ�ı��ֱ��ˣ���getԭ����map��õ���һ�������Ŀ��aloneid
				//�������������aloneid��ͬ�����: key�����������pk
				if(vo.getMeasureVO().getKeyCombPK().equals(pubdata.getKeyGroup().getKeyGroupPK())){ //�ؼ������û�仯
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
					//������ڲ����ױ�,���ʱ�ؼ����Ӧ�벻ͬ,��Ϊ��һ���Է���λ
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
	 * �ɱ���תȨ�淨������
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
     * ��óɱ���תȨ�淨aloneID
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

        // �ؼ������
        KeyGroupVO keyGroupVo = IUFOCacheManager.getSingleton().getKeyGroupCache()
                .getByPK(new_qryvo.getSchemevo().getPk_keygroup());
        pubdata.setKType(keyGroupVo.getKeyGroupPK());
        pubdata.setKeyGroup(keyGroupVo);

        // �ؼ���ֵ
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

        // �汾
        if (isHb) {
            pubdata.setVer(IDataVersionConsts.VER_VOUCHER + IDataVersionConsts.VERTYPE_HBBB_ADJUST);
        } else {
            pubdata.setVer(IDataVersionConsts.VER_VOUCHER);
        }

        // ���AloneID
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
     * ������óɱ���תȨ�淨aloneID
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

        // �ؼ������
        KeyGroupVO keyGroupVo = IUFOCacheManager.getSingleton().getKeyGroupCache()
                .getByPK(new_qryvo.getSchemevo().getPk_keygroup());
        pubdata.setKType(keyGroupVo.getKeyGroupPK());
        pubdata.setKeyGroup(keyGroupVo);

        // �ؼ���ֵ
        if (new_qryvo.getKeymap() != null && new_qryvo.getKeymap().size() > 0) {
            String[] keys = new String[new_qryvo.getKeymap().size()];
            new_qryvo.getKeymap().keySet().toArray(keys);
            if (null != keys && keys.length > 0) {
                for (String key : keys) {
                    pubdata.setKeywordByPK(key, new_qryvo.getKeymap().get(key));
                }
            }
        }

        // �汾
        if (isHb) {
            pubdata.setVer(IDataVersionConsts.VER_VOUCHER + IDataVersionConsts.VERTYPE_HBBB_ADJUST);
        } else {
            pubdata.setVer(IDataVersionConsts.VER_VOUCHER);
        }

        // ���AloneID
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
	     * ȡ��̬����ָ����
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
	     * ȡ��̬����ָ����
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
	 * ����ԭ��MeasureDataVO�õ�����ԭ��������ԭ�ҵ�aloneid
	 * <����ԭ��alongid,����ԭ��alongid>
	 * 
	 * @create by zhoushuang at 2014-3-6,����2:19:46
	 *
	 * @param thisSrcDataVOs
	 * @param lastPrdSrcPubdata
	 * @return <����ԭ��alongid,����ԭ��alongid>
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
	 * ����ԭ��MeasureDataVO�õ�����ԭ��������Ŀ�ıҵ�aloneid
	 * <����ԭ��alongid,����Ŀ�ı�alongid>
	 * 
	 * @create by zhoushuang at 2014-3-6,����2:20:14
	 *
	 * @param thisSrcDataVOs
	 * @param lastPrdDesPubdata
	 * @param desCoinPk
	 * @return <����ԭ��alongid,����Ŀ�ı�alongid>
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
