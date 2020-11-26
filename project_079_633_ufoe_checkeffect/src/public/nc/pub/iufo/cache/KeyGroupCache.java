package nc.pub.iufo.cache;

import java.util.Vector;

import nc.pub.iufo.data.thread.AbstractQueryData;
import nc.pub.iufo.data.thread.IufoThreadLocalUtil;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.pub.date.UFODate;
import nc.vo.iuforeport.rep.ReportVO;

/**
 * 关键字组合缓存
 * @author yp
 *
 */
public class KeyGroupCache extends Cache {

	static KeyGroupCache getInstance(){
		return  (KeyGroupCache) CacheManager.getCache(KeyGroupVO.class);
	}
	
	 /**
     * 得到全部的关键字组合
     */
    public KeyGroupVO[] getAllGroupVOs() {
        Object[] objs = getAll();
        if(objs == null || objs.length < 1)
        	return new KeyGroupVO[0];

        KeyGroupVO[] vos = new KeyGroupVO[objs.length];
        for(int i=0;i<objs.length;i++){
        	vos[i] = (KeyGroupVO) objs[i];
        }
        reloadKeyVOs(vos);
        return vos;

    }

    /**
     * 根据关键字组合主键获得关键字组合信息。
     * 
     * @param pk
     *            java.lang.String
     */
    public KeyGroupVO getByPK(String strKeyGroupPK) {
    	if(strKeyGroupPK == null)
    		return null;
    	
    	
    	final String finalKey = strKeyGroupPK;
    	String key = "nc.pub.iufo.cache.KeyGroupCache  KeyGroupCache | "+strKeyGroupPK;
    	KeyGroupVO vo = (KeyGroupVO)IufoThreadLocalUtil.getValue(key, new AbstractQueryData() {
					
					@Override
					public Object qqueryData() {
						KeyGroupVO rtn  = (KeyGroupVO) KeyGroupCache.this.get(finalKey);
						reloadKeyVOs(new KeyGroupVO[] { rtn });
						return rtn;
					}
				});
    	
//        KeyGroupVO vo = (KeyGroupVO) get(strKeyGroupPK);
//        reloadKeyVOs(new KeyGroupVO[] { vo });
        return vo;
    }

    /**
     * 根据关键字组合主键获得一组关键字组合信息。 创建日期
     * 
     * @param pk
     *            java.lang.String
     */
    public KeyGroupVO[] getByPKs(String[] pks) {
        if (pks == null || pks.length == 0)
            return null;
        
        KeyGroupVO[] keyGroupVOs = new KeyGroupVO[pks.length];
        for (int i = 0; i < pks.length; i++) {
            keyGroupVOs[i] = (KeyGroupVO) get(pks[i]);
        }
        reloadKeyVOs(keyGroupVOs);
        return keyGroupVOs;
    }


    /**
     * 根据传入的关键字组合的pk关键字pk，找到该关键字在公有或私有关键字顺序列中的位置 1。如果关键字组合中不存在该关键字，返回-1；
     * 2。如果存在，则按照1、2、3、4的顺序返回值 创建日期：(2003-8-5 15:28:42)
     * 
     * @return nc.vo.iufo.keydef.KeyVO
     * @param keyGroupPk
     *            java.lang.String
     * @param keyPk
     *            java.lang.String
     */
    public int getIndexOfKey(String keyGroupPk, String keyPk) {

        if (keyGroupPk == null || keyPk == null) {
            return -1;
        }
        
        KeyGroupVO groupVo = getByPK(keyGroupPk);
        KeyVO[] keys = groupVo.getKeys();
        int priIndex = -1;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].getPk_keyword().equals(keyPk)) {
                return i + 1;
            }
        }
        //未找到，返回-1
        return -1;
    }

    /**
     * 此处插入方法描述。 创建日期：(2003-9-22 11:03:02)
     * 
     * @return nc.vo.iufo.keydef.KeyGroupVO[]
     * @param keyPk
     *            java.lang.String
     */
    public KeyGroupVO[] getKeyGroupVOsByKeyPk(String keyPk) {
        if (keyPk == null || keyPk.length() == 0)
            return null;
        
        Object[] objs = getAll();
        Vector vosVec = new Vector();
        KeyGroupVO vo = null;
        if (objs == null)
            return new KeyGroupVO[0];

        for (int i = 0; i < objs.length; i++) {
            vo = (KeyGroupVO) objs[i];
            if (vo.getKeyByKeyPk(keyPk) != null) {
                vosVec.addElement(vo);
            }
        }

        KeyGroupVO[] vos = new KeyGroupVO[vosVec.size()];
        vosVec.copyInto(vos);
        reloadKeyVOs(vos);
        return vos;
    }


    /**
     * 此处插入方法描述。 创建日期：
     * 
     * @param groupvo
     *            nc.vo.iufo.keydef.KeyGroupVO
     */
    public KeyGroupVO getPkByKeyGroup(KeyGroupVO vo) {
        if (vo == null)
            return null;
        KeyGroupVO keyGroupVo = innerGetPKByKeyGroup(vo);
        //yp 防止客户端之间缓存不同步引起的空指针问题，第一遍如果加载不到，显式的刷新缓存
       if(keyGroupVo == null){
    	   // @edit by wuyongc at 2013-7-31,上午10:35:52 应该仅仅刷新关键字组合的缓存。。
//    	   CacheManager.refresh();
    	   this.clear();
    	   keyGroupVo = innerGetPKByKeyGroup(vo);
       }
        return keyGroupVo;
    }
    
    private KeyGroupVO innerGetPKByKeyGroup(KeyGroupVO vo){
    	 KeyGroupVO[] vos = getAllGroupVOs();
         KeyVO[] fKeys = null;
         KeyVO[] aKeys = null;
         //	boolean find = false;
         for (int i = 0; i < vos.length; i++) {
             aKeys = vos[i].getKeys();
             fKeys = vo.getKeys();
             if (aKeys.length != fKeys.length)
                 continue;
             int num = 0;
             for (int j = 0; j < fKeys.length; j++) {
                 if (aKeys[j].getPk_keyword().equals(fKeys[j].getPk_keyword())) {
                     num++;
                 }
             }
             if (num == fKeys.length)
                 return vos[i];
         }
         return null;
    }

	/**
	 * 根据关键字组合PK获得自然时间属性（自然时间的期间类型）。 
	 * @param strKeyGroupPK
	 * @return
	 */
    public String getTimePropByPK(String strKeyGroupPK) {
    	if (strKeyGroupPK == null || strKeyGroupPK.length() == 0){
            return null;
    	}
        
        
    	if(getTTimeKeyType(strKeyGroupPK) == KeyGroupVO.TYPE_TTIME_TIME){
    		return getTTimePropByPK(strKeyGroupPK);
    	}
    	return null;    	
    }
    
    /**
     * 根据关键字组合PK获得时间属性（期间类型）。
     * @param strKeyGroupPK
     * @return KeyVO.TYPE_TIME，自然时间类型；KeyVO.TYPE_ACC，会计期间类型；-1，无期间类型
     */
    public int getTTimeKeyType(String strKeyGroupPK){
    	if (strKeyGroupPK == null || strKeyGroupPK.length() == 0)
            return -1;
        
        KeyGroupVO vo = (KeyGroupVO) get(strKeyGroupPK);
        KeyVO[] keyvos = vo.getKeys();
        
        return KeyGroupVO.getTTimeKeyType(keyvos);
    }
    /**
     * 根据关键字组合PK获得时间属性（期间类型）。 
     * @param strKeyGroupPK
     */
    public String getTTimePropByPK(String strKeyGroupPK) {
        if (strKeyGroupPK == null || strKeyGroupPK.length() == 0)
            return null;
        
        KeyGroupVO vo = (KeyGroupVO) get(strKeyGroupPK);
        KeyVO[] keyvos = vo.getKeys();
        if (keyvos == null)
            return null;
        for (int i = 0; i < keyvos.length; i++) {
            if (keyvos[i].getType() == KeyVO.TYPE_TIME) {
                if (KeyVO.CODE_TYPE_DAY.equals(keyvos[i].getCode()))
                    return UFODate.DAY_PERIOD;
                else if (KeyVO.CODE_TYPE_HALFYEAR.equals(keyvos[i].getCode()))
                    return UFODate.HALFYEAR_PERIOD;
                else if (KeyVO.CODE_TYPE_MONTH.equals(keyvos[i].getCode()))
                    return UFODate.MONTH_PERIOD;
                else if (KeyVO.CODE_TYPE_QUARTER.equals(keyvos[i].getCode()))
                    return UFODate.SEASON_PERIOD;
                else if (KeyVO.CODE_TYPE_TENDAYS.equals(keyvos[i].getCode()))
                    return UFODate.TENDAYS_PERIOD;
                else if (KeyVO.CODE_TYPE_WEEK.equals(keyvos[i].getCode()))
                    return UFODate.WEEK_PERIOD;
                else if (KeyVO.CODE_TYPE_YEAR.equals(keyvos[i].getCode()))
                    return UFODate.YEAR_PERIOD;
            }else if (keyvos[i].getType() == KeyVO.TYPE_ACC) {
            	if (KeyVO.CODE_TYPE_ACCYEAR.equals(keyvos[i].getCode()))
                    return UFODate.ACCYEAR_PERIOD;
                else if (KeyVO.CODE_TYPE_ACCSEASON.equals(keyvos[i].getCode()))
                    return UFODate.ACCSEASON_PERIOD;
                else if (KeyVO.CODE_TYPE_ACCMONTH.equals(keyvos[i].getCode()))
                    return UFODate.ACCMONTH_PERIOD;
                else if (KeyVO.CODE_TYPE_ACCHALFYEAR.equals(keyvos[i].getCode()))
                	return UFODate.ACCHALFYEAR_PERIOD;
            }
        }
        return null;
    }

    /**
     * 缓存中是否已包含和vo的关键字组合相同的关键字组合 创建日期：(2002-7-6 15:01:56)
     * 
     * @return boolean
     * @param vo
     *            nc.vo.iufo.keydef.KeyGroupVO
     */
    public String isContains(KeyGroupVO vo) {
        
        if (vo != null) {
            KeyGroupVO[] vos = getAllGroupVOs();

            if (vos != null && vos.length > 0) {
                for (int i = 0; i < vos.length; i++) {
                    if (vo.equals(vos[i])) {
                        return vos[i].getKeyGroupPK();
                    }
                }
            }

        }
        return null;
    }

//    /**
//     * 判断当前全部关键字组合中是否包含传入的关键字 创建日期：(2003-11-3 11:08:57)
//     * 
//     * @return java.lang.String[]
//     * @param KeyPk
//     *            java.lang.String
//     */
//    public String[] isContainsKeys(String KeyPk) {
//        String[] groupPks = null;
//        try {
//            if (getProxy() instanceof BSKeyGroupCacheProxy) {
//                groupPks = ((nc.bs.iufo.cache.BSKeyGroupCacheProxy) getProxy())
//                        .isContainsKeys(KeyPk);
//            } else {
//                groupPks = ((nc.ui.iufo.cache.UIKeywordGroupCacheProxy) getProxy())
//                        .isContainsKeys(KeyPk);
//            }
//
//        } catch (Exception e) {
//AppDebug.debug(e);//@devTools             AppDebug.debug(e);
//        }
//        return groupPks;
//    }


    /**
     * 重新加载KeyVO
     * @param groupvos KeyGroupVO[]
     */
    private void reloadKeyVOs(KeyGroupVO[] groupvos) {
        KeywordCache keyCache = UFOCacheManager.getSingleton().getKeywordCache();
        for (int i = 0; i < groupvos.length; i++) {
            if (groupvos[i] == null)
                continue;
            KeyVO[] keys = groupvos[i].getKeys();
            if (keys == null || keys.length <= 0)
                continue;
            int length = keys.length;
            String[] kpks = new String[length];
            for (int j = 0; j < length; j++) {
                if (keys[j] != null)
                    kpks[j] = keys[j].getPk_keyword();
            }
            KeyVO[] newKeys = keyCache.getByPKs(kpks);
            String strKeyGroupPK=groupvos[i].getKeyGroupPK();
            groupvos[i]=new KeyGroupVO(strKeyGroupPK);
            groupvos[i].resetKeyVOs(newKeys);            
        }
    }

	}

