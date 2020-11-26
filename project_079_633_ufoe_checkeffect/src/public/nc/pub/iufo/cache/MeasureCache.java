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
 * 采集表指标缓存
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
     * 根据编码查询指标。 创建日期：(2001-11-13 16:10:01)
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
        	//*****央客王志强-优化指标获取缓存---start
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
        	//*****央客王志强-优化指标获取缓存----end
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
     * 根据编码查询一组指标。 创建日期：(2001-11-13 16:10:01)
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
     * 判断指标名称是否存在
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
 * 返回报表包含的所有指标VO
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
     * 根据报表主键加载报表包含的指标信息。
     *
     * @return nc.vo.iufo.measure.MeasureVO[]
     * @param strReportPK
     *            java.lang.String
     */
    public MeasureVO[] loadMeasureByReportPK(String strReportPK) {
        if (strReportPK == null) {
            return null;
        }

        //指标组VOs
        MeasurePackVO[] arrMPVOs = loadMeasPackVOsByRepPK(strReportPK);
        if (arrMPVOs == null) {
            return null;
        }
        MeasureVO[] arrMeasures = null;

        int nLen = arrMPVOs.length;
        Vector<MeasureVO>  vecMeasures = new Vector<MeasureVO> ();//指标集合
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
     * 通过报表pk加载此报表创建的指标pk集合
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
        Vector<String> vecMeasurePKs = new Vector<String>();//指标PK集合
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
     * 根据报表PK返回指标组VO
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
     * 考虑到效率问题, 在ReportVO中添加冗余信息, MeasPackPKs直接取冗余信息
     *
     * 根据报表PK加载指标组PK liuyy. 2005-3-29
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
     * 获得指标编码=strMeasureCode的指标对应的关键字组合pk
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
     * 按照编码返回指标数组。 创建日期：(2001-11-06 14:45:33)
     *
     * @return nc.vo.iufo.measure.MeasureVO[]
     * @param codes
     *            java.lang.String
     * @exception nc.vo.iufo.pub.CommonException
     *                异常说明。
     */
    public MeasureVO[] loadMeasuresByCodes(String[] codes)
            throws CommonException {
        return getMeasures(codes);
    }

    /**
     * 根据指标内部编码找回指标
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
     * 按照名字返回指标。 创建日期：(2001-11-20 15:47:20)
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
     * 按照名字返回指标数组。 创建日期：(2001-11-20 15:47:20)
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
        //构建以名称为索引的哈希表
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
     * 根据一组指标ID，返回其所属的报表ID数组。 创建日期：(2003-8-12 9:19:20)
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
//     * 重新更新报表包含的指标组信息。 包括：新建、修改和删除 创建日期：(2003-8-12 11:43:40)
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
//        //加载报表包含的所有指标组
//        MeasurePackVO[] arrOldMeasPackVOs = this
//                .loadMeasPackVOsByRepPK(strReportPK);
//
//        //为了便于查找，将数组构造成为Hashtable
//        Hashtable hashOldMeasPacks = new Hashtable();
//        if (arrOldMeasPackVOs != null && arrOldMeasPackVOs.length > 0) {
//            for (int i = 0; i < arrOldMeasPackVOs.length; i++) {
//                hashOldMeasPacks.put(arrOldMeasPackVOs[i].getPackgeID(),
//                        arrOldMeasPackVOs[i]);
//            }
//        }
//        //对报表的指标组进行过滤，分出新建、修改和删除
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
//        //删除指标组
//        int nLen = hashOldMeasPacks.size();
//        if (nLen > 0) {
//            MeasurePackVO[] arrRemoveMeasPackVOs = new MeasurePackVO[nLen];
//            hashOldMeasPacks.values().toArray(arrRemoveMeasPackVOs);
//            this.removePackages(arrRemoveMeasPackVOs);
//        }
//
//        //更新指标组
//        MeasurePackVO[] updateMeasures = new MeasurePackVO[vecUpdate.size()];
//        vecUpdate.copyInto(updateMeasures);
//        if (updateMeasures.length > 0) {
//            this.updatePackages(updateMeasures);
//        }
//        //新建指标组
//        MeasurePackVO[] addMeasures = new MeasurePackVO[vecAdd.size()];
//        vecAdd.copyInto(addMeasures);
//        if (addMeasures.length > 0) {
//            this.addPackages(addMeasures);
//        }
//
//        //	//修改报表PK--指标组PK缓存
//        //    Vector vecMeasPackPKs = null;//指标组PK集合
//        //    for(int i = 0; i < arrNewMeasPackVOs.length; i++){
//        //        vecMeasPackPKs.addElement(arrNewMeasPackVOs[i].getPackgeID());
//        //    }
//        //    Hashtable hashRepMPCache = getRepMeasPackCache();
//        //    hashRepMPCache.remove(strReportPK);
//        //    hashRepMPCache.put(strReportPK, vecMeasPackPKs);
//
//    }

    /**
     * 根据报表主键删除该报表包含的所有指标。
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
     * added by weixl on 2005-1-24, 删除和指标关联数据的方法，调用ModuleDataRemoveCenter提供的方法
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
        //TODO 暂时删掉yp
//        center.removeDataByPK(getCacheModuleName(), strPKs, isOnUILayer(),
//                false);

    }


    /**
     * added by weixl on 2005-1-24
     * 删除一组报表对应的指标组时，仅删除指标组，不删除指标关联数据的方法，本方法供MeasureBO调用
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
     * 仅删除一组指标组自身的方法，调用removePackagesWithParam方法，本方法供MeasureBO调用
     */
    public void removeCacheOjectsOnlySelf(MeasurePackVO[] os)
            throws java.lang.Exception {
        removePackagesWithParam(os, false);
    }

    /**
     * added by weixl on 2005-1-24
     * 仅删除一个指标组自身的方法，调用removePackageWithParam方法，本方法供MeasureBO调用
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
     * 更新一组指标组自身的方法，调用updatePackagesWithParam方法，本方法供MeasureBO调用
     */
    public MeasurePackVO[] updateCacheObjectsOnlySelf(MeasurePackVO[] os)
            throws java.lang.Exception {
        return updatePackagesWithParam(os, false);
    }

    /**
     * added by weixl on 2005-1-24
     * 更新一个指标组自身的方法，调用updatePackageWithParam方法，本方法供MeasureBO调用
     */
    public MeasurePackVO updateOnlySelf(MeasurePackVO object)
            throws java.lang.Exception {
        return updatePackageWithParam(object, false);
    }

    /**
     * added by weixl on 2005-1-24
     * 删除多个指标组对象对象,供removePackages和removeCacheObjectsOnlySelf方法调用，
     * bDelCascade为true时，需要删除指标关联数据,为false时，仅需要删除指标组 对原removePackages方法的简单封装
     * @i18n miufo00390=删除MeasurePackVO
     */
    private MeasurePackVO[] removePackagesWithParam(MeasurePackVO[] arrps,
            boolean bDelCascade) throws Exception {
        if (arrps == null || arrps.length <= 0)
            return arrps;

        if (bDelCascade) {
            //删除指标组中指标关联的数据
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

        AppDebug.debug(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1055")/*@res "删除MeasurePackVO"*/);

        return arrps;
    }

    /**
     * added by weixl on 2005-1-24
     * 更新指标组对象,供updatePackage和updateOnlySelf方法调用，对于指标组中需要删除的指标，bDelCascade为true时，
     * 需要删除指标关联数据,并回收指标所占用的数据库表，为false时，仅需要删除指标,不需要删除指标关联数据和回收指标数据库表，
     * 对原updatePackage方法的简单封装
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
     * 更新指标组对象,供updatePackages和updateCacheObjectsOnlySelf方法调用，对于指标组中需要删除的指标，bDelCascade为true时，
     * 需要删除指标关联数据,并回收指标所占用的数据库表，为false时，仅需要删除指标,不需要删除指标关联数据和回收指标数据库表，
     * 对原updatePackages方法的简单封装
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

            Vector<MeasureVO> vecRecMVOs = new Vector<MeasureVO>();//待回收的指标集合
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

                //删除指标以前，需要删除和指标关联的数据
                removeMeasRelaDatas(arrRemoveMVOs);
            }
        }

        MeasureBO_Client.updatePackages(packs);
        return packs;
    }
    /**
     * 得到新的指标组的PK
     * @return
     */
    public String getNewMeasPackPK(){
        String strMeasPackPK  =IDMaker.makeID(MeasurePackVO.MEASUREPACK_PK_LENGTH);
        return strMeasPackPK;
    }
    /**
     * 根据指标组pk得到新的指标pk
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