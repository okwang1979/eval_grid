package nc.pub.iufo.cache;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import nc.pub.iufo.cache.Cache;
import nc.pub.iufo.cache.CacheManager;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.data.thread.AbstractQueryData;
import nc.pub.iufo.data.thread.IufoThreadLocalUtil;
import nc.pub.iufo.exception.CommonException;
import nc.ui.iufo.measure.MeasureBO_Client;
import nc.util.iufo.pub.IDMaker;
import nc.util.iufo.pub.OIDMaker;
import nc.vo.iufo.measure.MeasurePackVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.pub.IDatabaseNames;
import nc.vo.iuforeport.rep.ReportVO;

import com.ufida.iufo.pub.tools.AppDebug;
/**
 * �ɼ���ָ�껺��
 * @author yp
 *
 */
public class MeasureCache  extends Cache{

	public static MeasureCache getInstance(){
		return  (MeasureCache) CacheManager.getCache(MeasurePackVO.class);
	}

//	 public Object[] update(Object... values) {
//
//		 if (values == null || values.length < 1) {
//				return null;
//		}
//		 MeasurePackVO[] packs = new MeasurePackVO[values.length];
//		 System.arraycopy(values, 0, packs, 0, values.length);
//		 try {
//			updatePackagesWithParam(packs, false);
//		} catch (Exception e) {
//			AppDebug.debug(e);
//		}
//		putCacheElement(values);
//		return values;
//	 }

	 protected Object[] innerUpdate(Object... values) {
		 if (values == null || values.length < 1) {
				return null;
		}
		 MeasurePackVO[] packs = new MeasurePackVO[values.length];
		 System.arraycopy(values, 0, packs, 0, values.length);
		 try {
			updatePackagesWithParam(packs, false);
		} catch (Exception e) {
			AppDebug.debug(e);
		}
		return values;
	 }

	 /**
     * ���ݱ����ѯָ�ꡣ �������ڣ�(2001-11-13 16:10:01)
     *
     * updated by liuyy. 2005-1-6.
     *
     * @return nc.vo.iufo.measure.MeasureVO
     * @param aCode
     *            java.lang.String
     */
    public MeasureVO getMeasure(String strMeasurePK) {
        if (strMeasurePK == null || strMeasurePK.trim().length()<=MeasurePackVO.MEASUREPACK_PK_LENGTH)
            return null;

        final String strMeasPackPK = strMeasurePK.substring(0,
                MeasurePackVO.MEASUREPACK_PK_LENGTH);
        MeasurePackVO mpvo;
        MeasureVO mvo = null;
        try {
        	//*****�����־ǿ-�Ż�ָ���ȡ����---start
        	final String  pk_measure = strMeasurePK;
        	
        	String key = "nc.pub.iufo.cache.MeasureCache :strMeasurePK | "+strMeasurePK;
        	mvo = (MeasureVO)IufoThreadLocalUtil.getValue(key, new AbstractQueryData() {
    					
    					@Override
    					public Object qqueryData() {
    						MeasurePackVO rtnVO = (MeasurePackVO) MeasureCache.this.get(strMeasPackPK);
    				            if (rtnVO == null) {
    				                return null;
    				            }

    				            Hashtable<String, MeasureVO> measures = rtnVO.getMeasures();
    				            if (measures == null) {
    				                return null;
    				            }
    				            return    measures.get(pk_measure);
    					}
    				});
        	//*****�����־ǿ-�Ż�ָ���ȡ����----end
//            mpvo = (MeasurePackVO) this.get(strMeasPackPK);
//            if (mpvo == null) {
//                return null;
//            }
//
//            Hashtable<String, MeasureVO> measures = mpvo.getMeasures();
//            if (measures == null) {
//                return null;
//            }
//            mvo = measures.get(strMeasurePK);

        } catch (Exception e) {
        	AppDebug.debug(e);
            return null;
        }

        return mvo;

    }

    /**
     * ���ݱ����ѯһ��ָ�ꡣ �������ڣ�(2001-11-13 16:10:01)
     *
     * updated by liuyy. 2005-1-6.
     *
     */
    public MeasureVO[] getMeasures(String[] arrPKs) {
        if (arrPKs == null) {
            return null;
        }

        String strMeasPackPK = null;
        Vector<String> vecMeasPackPKs = new Vector<String>();
        int nLen = arrPKs.length;
        for (int i = 0; i < nLen; i++) {
        	if (arrPKs[i]==null || arrPKs[i].trim().length()<=MeasurePackVO.MEASUREPACK_PK_LENGTH)
        		continue;
            strMeasPackPK = arrPKs[i].substring(0,
                    MeasurePackVO.MEASUREPACK_PK_LENGTH);
            if (!vecMeasPackPKs.contains(strMeasPackPK)) {
                vecMeasPackPKs.add(strMeasPackPK);
            }
        }

        nLen = vecMeasPackPKs.size();
        String[] arrMeasPackPKs = new String[nLen];
        vecMeasPackPKs.copyInto(arrMeasPackPKs);
        Object[] objs = this.get(arrMeasPackPKs);

        if (objs == null || objs.length < 1) {
            return new MeasureVO[arrPKs.length];
        }
        MeasurePackVO[] arrmpvos = null;
        MeasureVO[] arrmvos = null;
        MeasurePackVO mpvo = null;

        nLen = objs.length;
        arrmpvos = new MeasurePackVO[nLen];
        System.arraycopy(objs, 0, arrmpvos, 0, nLen);

        int nMeasureNum = arrPKs.length;
        arrmvos = new MeasureVO[nMeasureNum];
        String strMeasPK;
        if (arrmpvos != null) {
            nLen = arrmpvos.length;
        } else {
            nLen = -1;
        }
        for (int i = 0; i < nLen; i++) {
            mpvo = arrmpvos[i];
            if (mpvo == null) {
                continue;
            }
            Hashtable<String, MeasureVO> hashMeas = mpvo.getMeasures();
            if (hashMeas == null) {
                continue;
            }
            for (int j = 0; j < nMeasureNum; j++) {
                strMeasPK = arrPKs[j];
                if (hashMeas.containsKey(strMeasPK)) {
                    arrmvos[j] = hashMeas.get(strMeasPK);
                }
            }

        }
        return arrmvos;
    }

    /**
     * �ж�ָ�������Ƿ����
     *
     * @return boolean
     * @param sName
     *            java.lang.String
     * @param strUnitId
     *            String
     */
    public boolean isExistName(String strReportPK, String sName) {
        MeasureVO[] arrmvos = this.loadMeasureByReportPK(strReportPK);

        if (arrmvos != null && arrmvos.length > 0) {
            for (int i = 0; i < arrmvos.length; i++) {
                MeasureVO mvo = arrmvos[i];
                if (mvo == null)
                    continue;
                if (mvo.getName().equals(sName)) {
                    return true;
                }
            }

        }
        return false;
    }
/**
 * ���ر������������ָ��VO
 * liuyy. 2005-4-4
 *
 * @param arrReportPKs
 * @return
 */
    public MeasureVO[] loadMeasureByReportPKs(String[] arrReportPKs) {
        if(arrReportPKs == null){
            return null;
        }
        Vector<MeasureVO> vecMeasVOs = new Vector<MeasureVO> ();
        MeasureVO[] arrmvos = null;
        for(int i = 0; i < arrReportPKs.length; i++){
            arrmvos = this.loadMeasureByReportPK(arrReportPKs[i]);
            if(arrmvos != null){
                vecMeasVOs.addAll(Arrays.asList(arrmvos));
            }
        }

        arrmvos = new MeasureVO[vecMeasVOs.size()];
        vecMeasVOs.copyInto(arrmvos);
        return arrmvos;
    }

    /**
     * ���ݱ����������ر��������ָ����Ϣ��
     *
     * @return nc.vo.iufo.measure.MeasureVO[]
     * @param strReportPK
     *            java.lang.String
     */
    public MeasureVO[] loadMeasureByReportPK(String strReportPK) {
        if (strReportPK == null) {
            return null;
        }

        //ָ����VOs
        MeasurePackVO[] arrMPVOs = loadMeasPackVOsByRepPK(strReportPK);
        if (arrMPVOs == null) {
            return null;
        }
        MeasureVO[] arrMeasures = null;

        int nLen = arrMPVOs.length;
        Vector<MeasureVO>  vecMeasures = new Vector<MeasureVO> ();//ָ�꼯��
        for (int i = 0; i < nLen; i++) {
            MeasurePackVO mpvo = arrMPVOs[i];
            if (mpvo == null) {
                continue;
            }
            Hashtable<String,MeasureVO> measures = mpvo.getMeasures();
            if (measures == null || measures.size() < 1) {
                continue;
            }
            vecMeasures.addAll(measures.values());
        }

        nLen = vecMeasures.size();
        arrMeasures = new MeasureVO[nLen];
        vecMeasures.copyInto(arrMeasures);

        return arrMeasures;
    }



    /**
     * ͨ������pk���ش˱�������ָ��pk����
     *
     * @param strReportPK
     *            String
     * @return String[]
     */
    public String[] loadMeasurePKsByRepPK(String strReportPK) {
        if (strReportPK == null) {
            return null;
        }

        MeasurePackVO[] arrMPVOs = loadMeasPackVOsByRepPK(strReportPK);
        if (arrMPVOs == null) {
            return null;
        }
        String[] arrMeasurePKs = null;

        int nLen = arrMPVOs.length;
        Vector<String> vecMeasurePKs = new Vector<String>();//ָ��PK����
        for (int i = 0; i < nLen; i++) {
            MeasurePackVO measurePackVO = arrMPVOs[i];
            if (measurePackVO == null) {
                continue;
            }
            Hashtable<String,MeasureVO>  measures = measurePackVO.getMeasures();
            if (measures == null) {
                continue;
            }
            vecMeasurePKs.addAll(measures.keySet());
        }

        nLen = vecMeasurePKs.size();
        arrMeasurePKs = new String[nLen];
        vecMeasurePKs.copyInto(arrMeasurePKs);

        return arrMeasurePKs;

    }

    /**
     * ���ݱ���PK����ָ����VO
     *
     * 2005-1-6 by liuyy.
     *
     * @param strReportPK
     * @return
     */
    public MeasurePackVO[] loadMeasPackVOsByRepPK(String strReportPK) {

        if (strReportPK == null) {
            return null;
        }
        MeasurePackVO[] arrMPVOs = null;

        String[] arrMeasPackPKs = this.loadMeasPackPKByRepId(strReportPK);
        Object[] arrObjs = this.get(arrMeasPackPKs);

        if (arrObjs == null || arrObjs.length < 1) {
            return null;
        }

        int nLength = arrObjs.length;
        arrMPVOs = new MeasurePackVO[nLength];
        System.arraycopy(arrObjs, 0, arrMPVOs, 0, nLength);
        return arrMPVOs;
    }

    /**
     * ���ǵ�Ч������, ��ReportVO�����������Ϣ, MeasPackPKsֱ��ȡ������Ϣ
     *
     * ���ݱ���PK����ָ����PK liuyy. 2005-3-29
     *
     * @param repId
     * @return
     * @throws SQLException
     */
    public String[] loadMeasPackPKByRepId(String repId) {

        ReportCache repCache = UFOCacheManager.getSingleton().getReportCache();

        ReportVO[] arrRepVOs = repCache.getByPks(new String[]{repId});
        if(arrRepVOs == null || arrRepVOs.length < 1 || arrRepVOs[0] == null){
            return null;
        }
        return repCache.getMeasPackPKs(arrRepVOs[0]);
    }

    /**
     * ���ָ�����=strMeasureCode��ָ���Ӧ�Ĺؼ������pk
     *
     * @param strMeasurePK
     *            String
     * @return String
     */
    public String getKeyCombPk(String strMeasureCode) {
        MeasureVO obj = this.getMeasure(strMeasureCode);

        if (obj != null) {
            String keyCombPK = ((MeasureVO) obj).getKeyCombPK();
            return keyCombPK;
        }

        return null;
    }

    /**
     * ���ձ��뷵��ָ�����顣 �������ڣ�(2001-11-06 14:45:33)
     *
     * @return nc.vo.iufo.measure.MeasureVO[]
     * @param codes
     *            java.lang.String
     * @exception nc.vo.iufo.pub.CommonException
     *                �쳣˵����
     */
    public MeasureVO[] loadMeasuresByCodes(String[] codes)
            throws CommonException {
        return getMeasures(codes);
    }

    /**
     * ����ָ���ڲ������һ�ָ��
     * @param strReportPK
     * @param strName
     * @return
     */
    public MeasureVO loadMeasuresByInputCode(String strReportPK, String strInputCode) {
    	if (strInputCode==null)
    		return null;

        MeasureVO[] arrmvos = loadMeasureByReportPK(strReportPK);

        if (arrmvos != null && arrmvos.length > 0) {
            for (int i = 0; i < arrmvos.length; i++) {
                MeasureVO mvo = arrmvos[i];
                if (mvo == null) {
                    continue;
                }
                if (strInputCode.equals(mvo.getInputCode())) {
                    return mvo;
                }
            }
        }
        return null;
    }

    /**
     * �������ַ���ָ�ꡣ �������ڣ�(2001-11-20 15:47:20)
     *
     * @return nc.vo.iufo.measure.MeasureVO[]
     * @param strNames
     *            java.lang.String[]
     * @param strUnitId
     *            String
     */
    public MeasureVO loadMeasuresByName(String strReportPK, String strName) {
        MeasureVO[] arrmvos = loadMeasureByReportPK(strReportPK);

        if (arrmvos != null && arrmvos.length > 0) {
            for (int i = 0; i < arrmvos.length; i++) {
                MeasureVO mvo = arrmvos[i];
                if (mvo == null) {
                    continue;
                }
                if (mvo.getName().equals(strName)) {
                    return mvo;
                }
            }
        }

        return null;
    }

    /**
     * �������ַ���ָ�����顣 �������ڣ�(2001-11-20 15:47:20)
     *
     * @return nc.vo.iufo.measure.MeasureVO[]
     * @param strNames
     *            java.lang.String[]
     * @param strUnitId
     *            String
     */
    public MeasureVO[] loadMeasuresByNames(String strReportPK, String[] strNames) {

        if (strNames == null || strNames.length <= 0) {
            return null;
        }

        MeasureVO[] arrmvos = loadMeasureByReportPK(strReportPK);

        if (arrmvos == null || arrmvos.length <= 0) {
            return null;
        }
        //����������Ϊ�����Ĺ�ϣ��
        Hashtable<String,MeasureVO>  hashMeasures = new Hashtable<String,MeasureVO> ();

        for (int i = 0; i < arrmvos.length; i++) {
            MeasureVO measureVO = arrmvos[i];
            if (measureVO == null) {
                continue;
            }
            hashMeasures.put(measureVO.getName(), measureVO);
        }
        arrmvos = new MeasureVO[strNames.length];
        for (int i = 0; i < strNames.length; i++) {
            arrmvos[i] = (MeasureVO) hashMeasures.get(strNames[i]);
        }

        return arrmvos;
    }

    /**
     * ����һ��ָ��ID�������������ı���ID���顣 �������ڣ�(2003-8-12 9:19:20)
     *
     * @return java.lang.String[]
     * @param measureIds
     *            java.lang.String[]
     */
    public String[] loadRepIdsByMeasureIds(String[] measureIds) {
        if (measureIds == null || measureIds.length == 0) {
            return null;
        }
        Vector<String> vec = new Vector<String>();
        for (int i = 0; i < measureIds.length; i++) {
            MeasureVO measure = this.getMeasure(measureIds[i]);
            if (!vec.contains(measure.getReportPK())) {
                vec.add(measure.getReportPK());
            }
        }
        String[] result = new String[vec.size()];
        vec.copyInto(result);
        return result;
    }
//
//    /**
//     * ���¸��±��������ָ������Ϣ�� �������½����޸ĺ�ɾ�� �������ڣ�(2003-8-12 11:43:40)
//     *
//     * updated by liuyy. 2005-1-6
//     *
//     * @param strReportPK
//     *            java.lang.String
//     * @param arrNewMeasPackVOs
//     *            nc.vo.iufo.measure.MeasureVO[]
//     */
//    public void refreshRepMeasureVOs(String strReportPK,
//            MeasurePackVO[] arrNewMeasPackVOs) throws Exception {
//        //���ر������������ָ����
//        MeasurePackVO[] arrOldMeasPackVOs = this
//                .loadMeasPackVOsByRepPK(strReportPK);
//
//        //Ϊ�˱��ڲ��ң������鹹���ΪHashtable
//        Hashtable hashOldMeasPacks = new Hashtable();
//        if (arrOldMeasPackVOs != null && arrOldMeasPackVOs.length > 0) {
//            for (int i = 0; i < arrOldMeasPackVOs.length; i++) {
//                hashOldMeasPacks.put(arrOldMeasPackVOs[i].getPackgeID(),
//                        arrOldMeasPackVOs[i]);
//            }
//        }
//        //�Ա����ָ������й��ˣ��ֳ��½����޸ĺ�ɾ��
//        Vector vecUpdate = new Vector();
//        Vector vecAdd = new Vector();
//
//        if (arrNewMeasPackVOs != null) {
//            for (int i = 0; i < arrNewMeasPackVOs.length; i++) {
//                MeasurePackVO oldMPVO = (MeasurePackVO) hashOldMeasPacks
//                        .get(arrNewMeasPackVOs[i].getPackgeID());
//                if (oldMPVO != null) {
//                    //				if (!oldMPVO.equalsEntirely(arrNewMeasPackVOs[i])){
//                    vecUpdate.add(arrNewMeasPackVOs[i]);
//                    //				}
//                    hashOldMeasPacks.remove(arrNewMeasPackVOs[i].getPackgeID());
//                } else {
//                    vecAdd.add(arrNewMeasPackVOs[i]);
//                }
//            }
//        }
//        //ɾ��ָ����
//        int nLen = hashOldMeasPacks.size();
//        if (nLen > 0) {
//            MeasurePackVO[] arrRemoveMeasPackVOs = new MeasurePackVO[nLen];
//            hashOldMeasPacks.values().toArray(arrRemoveMeasPackVOs);
//            this.removePackages(arrRemoveMeasPackVOs);
//        }
//
//        //����ָ����
//        MeasurePackVO[] updateMeasures = new MeasurePackVO[vecUpdate.size()];
//        vecUpdate.copyInto(updateMeasures);
//        if (updateMeasures.length > 0) {
//            this.updatePackages(updateMeasures);
//        }
//        //�½�ָ����
//        MeasurePackVO[] addMeasures = new MeasurePackVO[vecAdd.size()];
//        vecAdd.copyInto(addMeasures);
//        if (addMeasures.length > 0) {
//            this.addPackages(addMeasures);
//        }
//
//        //	//�޸ı���PK--ָ����PK����
//        //    Vector vecMeasPackPKs = null;//ָ����PK����
//        //    for(int i = 0; i < arrNewMeasPackVOs.length; i++){
//        //        vecMeasPackPKs.addElement(arrNewMeasPackVOs[i].getPackgeID());
//        //    }
//        //    Hashtable hashRepMPCache = getRepMeasPackCache();
//        //    hashRepMPCache.remove(strReportPK);
//        //    hashRepMPCache.put(strReportPK, vecMeasPackPKs);
//
//    }

    /**
     * ���ݱ�������ɾ���ñ������������ָ�ꡣ
     *
     * updated by liuyy. 2005-1-6
     *
     * @param strReportPK
     *            java.lang.String
     */
    public void removeByReportPKs(String[] arrReportPKs) throws Exception {
        MeasurePackVO[] arrMPVOs = null;
        MeasurePackVO[] arrSubMPVOs = null;
        Vector<MeasurePackVO> vecMPVOs = new Vector<MeasurePackVO>();
        for (int i = 0; i < arrReportPKs.length; i++) {
            arrSubMPVOs = this.loadMeasPackVOsByRepPK(arrReportPKs[i]);
            if (arrSubMPVOs == null || arrSubMPVOs.length < 1) {
                continue;
            }
            vecMPVOs.addAll(Arrays.asList(arrSubMPVOs));
        }
        arrMPVOs = new MeasurePackVO[vecMPVOs.size()];
        vecMPVOs.copyInto(arrMPVOs);
        for(MeasurePackVO pack:arrMPVOs)
        	remove(pack.getPackgeID());
    }

    protected String getCacheModuleName() {
        return IDatabaseNames.IUFO_MEASURE_TABLE;
    }



    /**
     * added by weixl on 2005-1-24, ɾ����ָ��������ݵķ���������ModuleDataRemoveCenter�ṩ�ķ���
     *
     * @param os
     */
    public void removeMeasRelaDatas(MeasureVO[] os) throws Exception {
        if (os == null || os.length <= 0)
            return;

        Vector<String> vPK = new Vector<String>();
        for (int i = 0; i < os.length; i++) {
            if (os[i] != null)
                vPK.add(os[i].getUUID());
        }

        if (vPK.size() <= 0)
            return;

//        String[] strPKs = (String[]) vPK.toArray(new String[0]);
//        ModuleDataRemoveCenter center = ModuleDataRemoveCenter
//                .getShareInstance();
        //TODO ��ʱɾ��yp
//        center.removeDataByPK(getCacheModuleName(), strPKs, isOnUILayer(),
//                false);

    }


    /**
     * added by weixl on 2005-1-24
     * ɾ��һ�鱨���Ӧ��ָ����ʱ����ɾ��ָ���飬��ɾ��ָ��������ݵķ�������������MeasureBO����
     */
    public void removeByReportPKsOnlySelf(String[] arrReportPKs)
            throws Exception {

        MeasurePackVO[] arrMPVOs = null;
        MeasurePackVO[] arrSubMPVOs = null;
        Vector<MeasurePackVO> vecMPVOs = new Vector<MeasurePackVO>();
        for (int i = 0; i < arrReportPKs.length; i++) {
            arrSubMPVOs = this.loadMeasPackVOsByRepPK(arrReportPKs[i]);
            vecMPVOs.addAll(Arrays.asList(arrSubMPVOs));
        }
        arrMPVOs = new MeasurePackVO[vecMPVOs.size()];
        vecMPVOs.copyInto(arrMPVOs);

        for(MeasurePackVO pack:arrMPVOs)
        	remove(pack.getPackgeID());
    }


    /**
     * ��ɾ��һ��ָ��������ķ���������removePackagesWithParam��������������MeasureBO����
     */
    public void removeCacheOjectsOnlySelf(MeasurePackVO[] os)
            throws java.lang.Exception {
        removePackagesWithParam(os, false);
    }

    /**
     * added by weixl on 2005-1-24
     * ��ɾ��һ��ָ��������ķ���������removePackageWithParam��������������MeasureBO����
     */
    public MeasurePackVO removeOnlySelf(MeasurePackVO object) throws Exception {
        return removePackageWithParam(object, false);
    }

    private MeasurePackVO removePackageWithParam(MeasurePackVO p, boolean bDelCascade)
			throws Exception {
		this.removePackagesWithParam(new MeasurePackVO[] { p }, bDelCascade);
		return p;
	}


    /**
     * added by weixl on 2005-1-24
     * ����һ��ָ��������ķ���������updatePackagesWithParam��������������MeasureBO����
     */
    public MeasurePackVO[] updateCacheObjectsOnlySelf(MeasurePackVO[] os)
            throws java.lang.Exception {
        return updatePackagesWithParam(os, false);
    }

    /**
     * added by weixl on 2005-1-24
     * ����һ��ָ��������ķ���������updatePackageWithParam��������������MeasureBO����
     */
    public MeasurePackVO updateOnlySelf(MeasurePackVO object)
            throws java.lang.Exception {
        return updatePackageWithParam(object, false);
    }

    /**
     * added by weixl on 2005-1-24
     * ɾ�����ָ����������,��removePackages��removeCacheObjectsOnlySelf�������ã�
     * bDelCascadeΪtrueʱ����Ҫɾ��ָ���������,Ϊfalseʱ������Ҫɾ��ָ���� ��ԭremovePackages�����ļ򵥷�װ
     * @i18n miufo00390=ɾ��MeasurePackVO
     */
    private MeasurePackVO[] removePackagesWithParam(MeasurePackVO[] arrps,
            boolean bDelCascade) throws Exception {
        if (arrps == null || arrps.length <= 0)
            return arrps;

        if (bDelCascade) {
            //ɾ��ָ������ָ�����������
            Hashtable<String,MeasureVO> hashDelMeas = new Hashtable<String,MeasureVO>();
            for (int i = 0; i < arrps.length; i++) {
                MeasurePackVO packVO = (MeasurePackVO) arrps[i];
                if (packVO == null || packVO.getMeasures() == null
                        || packVO.getMeasures().size() <= 0)
                    continue;
                hashDelMeas.putAll(packVO.getMeasures());
            }
            MeasureVO[] delMeas = (MeasureVO[]) hashDelMeas.values().toArray(
                    new MeasureVO[0]);

            removeMeasRelaDatas(delMeas);
        }

//        for(MeasurePackVO pack:arrps)
//        	remove(pack.getPackgeID());
        MeasureBO_Client.removePackages(arrps);
        super.removeCacheElement(arrps);

        AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1055")/*@res "ɾ��MeasurePackVO"*/);

        return arrps;
    }

    /**
     * added by weixl on 2005-1-24
     * ����ָ�������,��updatePackage��updateOnlySelf�������ã�����ָ��������Ҫɾ����ָ�꣬bDelCascadeΪtrueʱ��
     * ��Ҫɾ��ָ���������,������ָ����ռ�õ����ݿ��Ϊfalseʱ������Ҫɾ��ָ��,����Ҫɾ��ָ��������ݺͻ���ָ�����ݿ��
     * ��ԭupdatePackage�����ļ򵥷�װ
     */
    private MeasurePackVO updatePackageWithParam(MeasurePackVO p, boolean bDelCascade)
            throws Exception {
        if (p == null)
            return null;

        this.updatePackagesWithParam(new MeasurePackVO[] { p }, bDelCascade);
        return p;
    }

    /**
     * added by weixl on 2005-1-24
     * ����ָ�������,��updatePackages��updateCacheObjectsOnlySelf�������ã�����ָ��������Ҫɾ����ָ�꣬bDelCascadeΪtrueʱ��
     * ��Ҫɾ��ָ���������,������ָ����ռ�õ����ݿ��Ϊfalseʱ������Ҫɾ��ָ��,����Ҫɾ��ָ��������ݺͻ���ָ�����ݿ��
     * ��ԭupdatePackages�����ļ򵥷�װ
     */
    private MeasurePackVO[] updatePackagesWithParam(MeasurePackVO[] packs,
            boolean bDelCascade) throws Exception {

        if (packs == null) {
            return null;
        }

        if (bDelCascade) {
            MeasurePackVO[] arrNewMPVOs = null;

            int nLen = packs.length;
            arrNewMPVOs = new MeasurePackVO[nLen];
            System.arraycopy(packs, 0, arrNewMPVOs, 0, nLen);

            Vector<MeasureVO> vecRecMVOs = new Vector<MeasureVO>();//�����յ�ָ�꼯��
            MeasurePackVO oldmpvo = null;
            MeasurePackVO newmpvo = null;

            for (int z = 0; z < nLen; z++) {
                newmpvo = arrNewMPVOs[z];
                oldmpvo = (MeasurePackVO) this.get(newmpvo.getPackgeID());
                if (oldmpvo == null) {
                    continue;
                }
                Hashtable<String, MeasureVO> hashOldMeas = oldmpvo.getMeasures();
                if (hashOldMeas == null) {
                    continue;
                }
                Hashtable<String, MeasureVO> hashNewMeas = newmpvo.getMeasures();
                int nNum = hashNewMeas.size();
                String[] arrMeasPKs = new String[nNum];
                hashNewMeas.keySet().toArray(arrMeasPKs);
                for (int i = 0; i < nNum; i++) {
                    String strPK = arrMeasPKs[i];
                    if (hashOldMeas.get(strPK) != null) {
                        hashOldMeas.remove(strPK);
                    }
                }
                nNum = hashOldMeas.size();
                if (nNum > 0) {
                    vecRecMVOs.addAll(hashOldMeas.values());
                }
            }

            nLen = vecRecMVOs.size();
            if (nLen > 0) {
                MeasureVO[] arrRemoveMVOs = new MeasureVO[nLen];
                vecRecMVOs.copyInto(arrRemoveMVOs);

                MeasureBO_Client.recycleTables(arrRemoveMVOs);

                //ɾ��ָ����ǰ����Ҫɾ����ָ�����������
                removeMeasRelaDatas(arrRemoveMVOs);
            }
        }

        MeasureBO_Client.updatePackages(packs);
        return packs;
    }
    /**
     * �õ��µ�ָ�����PK
     * @return
     */
    public String getNewMeasPackPK(){
        String strMeasPackPK  =IDMaker.makeID(MeasurePackVO.MEASUREPACK_PK_LENGTH);
        return strMeasPackPK;
    }
    /**
     * ����ָ����pk�õ��µ�ָ��pk
     * @param strMeasPackPK
     * @return
     */
    public String getNewMeasurePK(String strMeasPackPK){
        String strId = null;
        String strMeasPK = null;
        strId = IDMaker.makeID(MeasurePackVO.MEASURE_ID_LENGTH);
        strMeasPK = strMeasPackPK + strId;
        return strMeasPK;
    }


}