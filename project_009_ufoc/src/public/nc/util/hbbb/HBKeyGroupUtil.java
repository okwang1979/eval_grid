package nc.util.hbbb;

import nc.pub.iufo.cache.IUFOCacheManager;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;

public class HBKeyGroupUtil {

    /**
     * 返回子表动态区的关键字pk，默认为对方单位编码
     * @param subKeyGroupVO
     * @param schemeVO
     * @return
     */
	public static String getPk_dynKeyValue(KeyGroupVO subKeyGroupVO,String pk_mainkeygroup) {
		String pk_dynKeyValue = KeyVO.DIC_CORP_PK;
		KeyGroupVO mainKeyGroup = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_mainkeygroup);
		KeyVO[] mainkeys = mainKeyGroup.getKeys();
		KeyVO[] subkeys = subKeyGroupVO.getKeys();
		//动态区关键字pk 目前支持一个动态区关键字
		for (int i = 0; i < subkeys.length; i++) {
			boolean has = false;
			for (int j = 0; j < mainkeys.length; j++) {
				if (mainkeys[j].equals(subkeys[i])) {
					has = true;
					break;
				}
			}
			if(!has)
				pk_dynKeyValue = subkeys[i].getPk_keyword();
		}
		return pk_dynKeyValue;
	}
	
    /**
     * 返回子表动态区的关键字pk，默认为对方单位编码
     * @param subKeyGroupVO
     * @param schemeVO
     * @return
     */
	public static String getPk_dynKeyValueByPK(String pk_dyKeyGroup,String pk_mainkeygroup) {
		String pk_dynKeyValue = KeyVO.DIC_CORP_PK;
		KeyGroupVO mainKeyGroup = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_mainkeygroup);
		KeyGroupVO subkeysVO = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_dyKeyGroup);
		KeyVO[] mainkeys = mainKeyGroup.getKeys();
		KeyVO[] subkeys = subkeysVO.getKeys();
		//动态区关键字pk 目前支持一个动态区关键字
		for (int i = 0; i < subkeys.length; i++) {
			boolean has = false;
			for (int j = 0; j < mainkeys.length; j++) {
				if (mainkeys[j].equals(subkeys[i])) {
					has = true;
					break;
				}
			}
			if(!has)
				pk_dynKeyValue = subkeys[i].getPk_keyword();
		}
		return pk_dynKeyValue;
	}
	
	/**
     * 返回子表动态区的关键字pk，(最多两个关键字)
     * @param subKeyGroupVO
     * @param schemeVO
     * @return
     */
	public static String[] getPk_dynKeyValues(KeyGroupVO subKeyGroupVO,String pk_mainkeygroup) {
		KeyGroupVO mainKeyGroup = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_mainkeygroup);
		KeyVO[] mainkeys = mainKeyGroup.getKeys();
		KeyVO[] subkeys = subKeyGroupVO.getKeys();
		String[] dynKeys = new String[subkeys.length-mainkeys.length];
		//动态区关键字pk 目前支持一个动态区关键字
		int k = 0;
		for (int i = 0; i < subkeys.length; i++) {
			boolean has = false;
			for (int j = 0; j < mainkeys.length; j++) {
				if (mainkeys[j].equals(subkeys[i])) {
					has = true;
					break;
				}
			}
			if(!has){
				dynKeys[k++] = subkeys[i].getPk_keyword();
			}
		}
		return dynKeys;
	}
}
