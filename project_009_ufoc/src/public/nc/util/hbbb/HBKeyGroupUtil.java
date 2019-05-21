package nc.util.hbbb;

import nc.pub.iufo.cache.IUFOCacheManager;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;

public class HBKeyGroupUtil {

    /**
     * �����ӱ�̬���Ĺؼ���pk��Ĭ��Ϊ�Է���λ����
     * @param subKeyGroupVO
     * @param schemeVO
     * @return
     */
	public static String getPk_dynKeyValue(KeyGroupVO subKeyGroupVO,String pk_mainkeygroup) {
		String pk_dynKeyValue = KeyVO.DIC_CORP_PK;
		KeyGroupVO mainKeyGroup = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_mainkeygroup);
		KeyVO[] mainkeys = mainKeyGroup.getKeys();
		KeyVO[] subkeys = subKeyGroupVO.getKeys();
		//��̬���ؼ���pk Ŀǰ֧��һ����̬���ؼ���
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
     * �����ӱ�̬���Ĺؼ���pk��Ĭ��Ϊ�Է���λ����
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
		//��̬���ؼ���pk Ŀǰ֧��һ����̬���ؼ���
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
     * �����ӱ�̬���Ĺؼ���pk��(��������ؼ���)
     * @param subKeyGroupVO
     * @param schemeVO
     * @return
     */
	public static String[] getPk_dynKeyValues(KeyGroupVO subKeyGroupVO,String pk_mainkeygroup) {
		KeyGroupVO mainKeyGroup = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_mainkeygroup);
		KeyVO[] mainkeys = mainKeyGroup.getKeys();
		KeyVO[] subkeys = subKeyGroupVO.getKeys();
		String[] dynKeys = new String[subkeys.length-mainkeys.length];
		//��̬���ؼ���pk Ŀǰ֧��һ����̬���ؼ���
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
