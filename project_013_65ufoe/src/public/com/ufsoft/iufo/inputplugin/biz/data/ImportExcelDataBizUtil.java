/*
 * �������� 2006-9-8
 *
 */
package com.ufsoft.iufo.inputplugin.biz.data;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.CommonException;
import nc.ui.iufo.dataexchange.MultiSheetImportUtil;
import nc.util.iufo.xls.BigXxlsHandler;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.pfxx.util.FileUtils;
import nc.vo.pub.BusinessException;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ufida.dataset.IContext;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iufo.inputplugin.biz.UfoExcelImpUtil;
import com.ufsoft.iufo.inputplugin.biz.file.ChooseRepData;
import com.ufsoft.iuforeport.tableinput.applet.TableInputException;
import com.ufsoft.table.CellsModel;

public class ImportExcelDataBizUtil {
    public static final String NONE_MATCH = "none_match";

    public ImportExcelDataBizUtil() {
        super();
    }
    /**
     * ���������ƺ�����˳���Զ�����ƥ�䣬�����ǰ�����Ѿ��򿪣����ҵ����Excel��Ҳֻ��һ�ű�����ֱ������ƥ��Զ�����
     * @param chooseRepDatas
     * @param workBook
     * @param strCurRepPK
     * @return
     * @throws TableInputException
     */
    public static Hashtable<String, Object> doGetAutoMatchMap(ChooseRepData[] chooseRepDatas, Workbook workBook, String strCurRepPK) throws TableInputException {
        Hashtable<String, Object> matchMap = new Hashtable<String, Object>();
    	if(chooseRepDatas == null || chooseRepDatas.length == 0){
    		return matchMap;
    	}

    	//��ÿһ�����������ƥ�䣬Ȼ����м�¼��
        //ƥ��ɹ��Ļ����գ����������ƣ�������룩Ϊһ�飻
        //ƥ�䲻�ɹ�������Ҫ���Ըù���������Ĭ��ֵ����ʾû��ƥ��
//        Map<String, String> repNMCMap = new HashMap<String, String>();
//        Map<String, String> repCMNMap = new HashMap<String, String>();
        DualDimMap dualMap = new DualDimMap();

        if(workBook != null){
            int sheetNum = workBook.getNumberOfSheets();
            String sheetName = null;
            String repCode = null;
            String repName = null;
            if(sheetNum == 1 && strCurRepPK!=null){
            	//���ڲ���ôҪ��ƥ�䵱ǰ�򿪱���
//                if(strCurRepPK == null){
//                    throw new TableInputException("miufo1002752");//��ǰϵͳ��û�д�Excel����Ҫ�����Ŀ�걨��  //"��ǰϵͳ��û�д�Excel����Ҫ�����Ŀ�걨��"+"!"
//                }
                sheetName = workBook.getSheetName(0);
                ChooseRepData chooseRepData = getCurRepData(chooseRepDatas,strCurRepPK);
                if(chooseRepData != null ){
                    repName = chooseRepData.getReportName();
                    repCode = chooseRepData.getReportCode();
                    //��ƥ���ֵ��¼����������֤һ��IUFO����ֻ�ܶ�Ӧһ��sheet
                    matchMap.put(sheetName, new String[]{repName, repCode});
                }
            }
            else{
                //���챨�����Ʊ��������ձ�
                int nRepCount = chooseRepDatas.length;
                for(int j = 0; j < nRepCount; j++){
//                    repNMCMap.put(chooseRepDatas[j].getReportName(), chooseRepDatas[j].getReportCode());
//                    repCMNMap.put(chooseRepDatas[j].getReportCode(), chooseRepDatas[j].getReportName());

                    dualMap.put(chooseRepDatas[j].getReportName(), chooseRepDatas[j].getReportCode());
                }

                String[] sheetNames = new String[sheetNum];
                for (int i = 0; i < sheetNames.length; i++) {
                	sheetNames[i] = workBook.getSheetName(i);
				}
                //��sheet��������,���Ȱ����ƴӳ���������,���ģ��ƥ���׼ȷ��.
                Arrays.sort(sheetNames, new Comparator<String>(){
					@Override
					public int compare(String o1, String o2) {
						int len = o2.length()-o1.length();
						if(len == 0)
							len = o1.compareTo(o2);
						return len;
					}
                });
                for(int i = 0; i < sheetNum; i++){
                    sheetName = sheetNames[i];
                    String [] repStr = dualMap.removeDimKeyValue(sheetName);
                    if(repStr == null){
                    	matchMap.put(sheetName, NONE_MATCH);
                    }else{
                    	matchMap.put(sheetName, repStr);
                    }
                }
            }
        }
        return matchMap;
    }
    
    public static Hashtable<String, Object> doGetAutoMatchMap(ChooseRepData[] chooseRepDatas, String[] sheetNames, String strCurRepPK) throws TableInputException {
        Hashtable<String, Object> matchMap = new Hashtable<String, Object>();
    	if(chooseRepDatas == null || chooseRepDatas.length == 0){
    		return matchMap;
    	}
    	
        DualDimMap dualMap = new DualDimMap();
        if(sheetNames != null){
            int sheetNum = sheetNames.length;
            String sheetName = null;
            String repCode = null;
            String repName = null;
            if(sheetNum == 1 && strCurRepPK!=null){
                sheetName = sheetNames[0];
                ChooseRepData chooseRepData = getCurRepData(chooseRepDatas,strCurRepPK);
                if(chooseRepData != null ){
                    repName = chooseRepData.getReportName();
                    repCode = chooseRepData.getReportCode();
                    //��ƥ���ֵ��¼����������֤һ��IUFO����ֻ�ܶ�Ӧһ��sheet
                    matchMap.put(sheetName, new String[]{repName, repCode});
                }
            }
            else{
                //���챨�����Ʊ��������ձ�
                int nRepCount = chooseRepDatas.length;
                for(int j = 0; j < nRepCount; j++){
                    dualMap.put(chooseRepDatas[j].getReportName(), chooseRepDatas[j].getReportCode());
                }
                //��sheet��������,���Ȱ����ƴӳ���������,���ģ��ƥ���׼ȷ��.
                Arrays.sort(sheetNames, new Comparator<String>(){
					@Override
					public int compare(String o1, String o2) {
						int len = o2.length()-o1.length();
						if(len == 0)
							len = o1.compareTo(o2);
						return len;
					}
                });
                for(int i = 0; i < sheetNum; i++){
                    sheetName = sheetNames[i];
                    String [] repStr = dualMap.removeDimKeyValue(sheetName);
                    if(repStr == null){
                    	matchMap.put(sheetName, NONE_MATCH);
                    }else{
                    	matchMap.put(sheetName, repStr);
                    }
                }
            }
        }
        return matchMap;
    }
    /**
     * �ӵ�ǰ�ɵ���ı�����Ϣ�������ҵ�ָ��������Ϣ
     * @param chooseRepDatas
     * @param strCurRepPK
     * @return
     */
    public static ChooseRepData getCurRepData(ChooseRepData[] chooseRepDatas, String strCurRepPK) {
    	if(chooseRepDatas == null || strCurRepPK == null){
    		return null;
    	}
        int nTotalRepCount = chooseRepDatas.length;
        for(int i =0;i < nTotalRepCount;i++){
            if(chooseRepDatas[i]!=null && chooseRepDatas[i].getReportPK().equals(strCurRepPK)){
                return chooseRepDatas[i];
            }
        }
        return null;
    }
    /**
     * �ӵ�ǰ�ɵ���ı�����Ϣ�������ҵ�ָ��������Ϣ
     * @param chooseRepDatas
     * @param strCurRepCode
     * @return
     */
    public static ChooseRepData getCurRepDataByCode(ChooseRepData[] chooseRepDatas, String strCurRepCode) {
    	if(chooseRepDatas == null || strCurRepCode == null){
    		return null;
    	}
    	int nTotalRepCount = chooseRepDatas.length;
        for(int i =0;i < nTotalRepCount;i++){
            if(chooseRepDatas[i]!=null && chooseRepDatas[i].getReportCode().equals(strCurRepCode)){
                return chooseRepDatas[i];
            }
        }
        return null;
    }

    //TODO
    public static String getCurKeyValByKeyVal(String[] keyVals, String keyVal){
    	if(keyVals == null || keyVal == null){
    		return null;
    	}
    	int nTotalRepCount = keyVals.length;
        for(int i =0;i < nTotalRepCount;i++){
            if(keyVals[i]!=null && keyVals[i].equals(keyVal)){
                return keyVals[i];
            }
        }
        return null;
    }
    /**
     * �õ�����Excel׼��������Ϣ�����пɴ�����
     *   ����ͨ��������Ϣ����sheetת��ΪCellsModel
     * @param array
     * @param workBook
     * @return List,����ÿ��Elemet��Object[]{sheetname,repcode,dynendrow,cellsModel}
     */
    public static List<Object[]> getImportInfos(List array, Workbook workBook,IContext context) {
    	return getImportInfos(array, workBook, context, null);
    }
    
    public static List<Object[]> getImportInfos(List array, Workbook workBook,IContext context, File file) {
        if(workBook == null || array == null || array.size() <=0 ){
            return null;
        }

        int nSheetSize = array.size();
        List<Object[]> listImportInfos = new ArrayList<Object[]>();
        String[] selVals = null;
        byte[] bytes = null;
        if(file != null){
        	if(file.exists()){
        		ByteArrayOutputStream baos;
					try {
						baos = FileUtils.getByteStreamFromFile(file);
						bytes = baos.toByteArray();
	            		} catch (Exception e) {
						AppDebug.debug(e);
					}
        	}else{
        		return null;
        	}
        }
        
        for(int i = 0 ; i <  nSheetSize; i++){
            Object[] objImportInfos = new Object[4];//{sheetname,repcode,dynendrow,cellsModel}
            selVals = (String[])array.get(i);//{sheetname,repcode,dynendrow}
            System.arraycopy(selVals,0,objImportInfos,0,3);

            String strSheetName = selVals[0];
                //�õ�sheet
            int sheetIndex = workBook.getSheetIndex(strSheetName);
            if (sheetIndex<0){
                continue;
            }
            else{
            	if(file != null){
//            		if(i == 0){
            			objImportInfos[3] = bytes;
//            		}
                     listImportInfos.add(objImportInfos);
            	}else{
            		 //TODO
                    CellsModel cellsModel=UfoExcelImpUtil.getCellsModelByExcel(workBook.getSheetAt(sheetIndex),workBook,context, true);
                    if (cellsModel==null){
                        continue;
                    }else{
                        objImportInfos[3] = cellsModel;
                        listImportInfos.add(objImportInfos);
                    }
            	}
            }
           
        }
        return listImportInfos;
    }
    
    //ByteArrayOutputStream baos = FileUtils.getByteStreamFromFile(new File(repImpParam.getFilePath()));
	
    /**
     * ��ȡExcel�е�sheet��Ϣ
     * @param filepath
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static HSSFWorkbook getImportWorkBook(String filepath) throws IOException, FileNotFoundException {
        if(filepath == null){
            return null;
        }
        FileInputStream fis=null;
        HSSFWorkbook workBook=null;
		try {
			fis=new FileInputStream(filepath);
			POIFSFileSystem fs =new POIFSFileSystem(fis);
			workBook = new HSSFWorkbook(fs);
		} catch (Exception e) {
			AppDebug.debug(e);
		}finally{
			if(fis!=null){
				fis.close();
			}
		}
        return workBook;
    }

    /**
     * ��ö�̬��ƥ�����Hearder��Ϣ����
     * @return
     */
    public static String[] getTableColumns() {
        String[] columns = new String[]{
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0590")/*@res "IUFO����"*/,  //"IUFO����"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0941")/*@res "Excel������"*/,  //"Excel������"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0942")/*@res "��̬��������"*/};  //"��̬��������λ��"
        return columns;
    }

    public static String[] getSingleImpTableColumns() {
        String[] columns = new String[]{
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1220")/*@res "ƥ��ؼ���"*/,  //"ƥ��ؼ���"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0941")/*@res "Excel������"*/,  //"Excel������"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0942")/*@res "��̬��������"*/};  //"��̬��������λ��"
        return columns;
    }

    public static String[] getIufoSingleImpTableColumns() {
        String[] columns = new String[]{
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1222")/*@res "ƥ��ؼ���ֵ"*/,  //"ƥ��ؼ���ֵ"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1223")/*@res "��Դ�ؼ������ֵ"*/,  //"��Դ�ؼ������ֵ"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1224")/*@res "Ŀ��ؼ������ֵ"*/};  //"Ŀ��ؼ������ֵ"
        return columns;
    }
    
    public static void processImportData(MultiSheetImportUtil importUtil, List listImportInfos, boolean isNeedSave) throws CommonException, BusinessException {
    	processImportData(importUtil, listImportInfos, isNeedSave,null);
    }

    /**
     * ������Excel׼��������Ϣ(CellsModel),���뵽���ݿ�
     * @param importUtil
     * @param listImportInfos List,����ÿ��Elemet��Object[]{sheetname,repcode,dynendrow,cellsModel}
     * @param isNeedSave
     * @throws BusinessException
     * @throws CommonException
     */
    public static void processImportData(MultiSheetImportUtil importUtil, List listImportInfos, boolean isNeedSave, IContext context) throws CommonException, BusinessException {
        if(importUtil == null || listImportInfos == null || listImportInfos.size() <=0){
            return;
        }
        int nValidSheetSize = listImportInfos.size();
        Workbook workbook = null;
        for(int i = 0 ; i < nValidSheetSize; i++){
            Object[] objImportInfos = (Object[])listImportInfos.get(i);//{sheetname,repcode,dynendrow,cellsModel}
            String strSheetName = (String)objImportInfos[0];
            String strRepCode = (String)objImportInfos[1];
            String nDynEndRowStr = (String)objImportInfos[2];
            if(objImportInfos[3] == null){
            	continue;
            }
            CellsModel cellsModel= null;
            if(objImportInfos[3] instanceof CellsModel){
            	 cellsModel= (CellsModel)objImportInfos[3];
            }else{
            	try {
            		if(workbook == null){
            			byte[] bytes = (byte[])objImportInfos[3];
                    	InputStream is = new ByteArrayInputStream(bytes);
            			workbook = WorkbookFactory.create(is);
            		}
					 cellsModel=UfoExcelImpUtil.getCellsModelByExcel(workbook.getSheet((String)objImportInfos[0]),workbook,context, true);
				} catch (IOException e) {
					AppDebug.debug(e);
				} catch (InvalidFormatException e) {
					throw new BusinessException("It's a invalid Excel format! ");
				}
            }
           
            if (cellsModel==null){
                continue;
            }
            nDynEndRowStr = StringUtils.trimToEmpty(nDynEndRowStr);
            String[] dynEndStrs = nDynEndRowStr.split(",");
            int []nDynEndRows = new int[dynEndStrs.length];

            if(dynEndStrs != null ){
            	for (int j = 0; j < nDynEndRows.length; j++) {
            		nDynEndRows[j] = -1;
            			try{
            				String temp = StringUtils.trimToEmpty(dynEndStrs[j]);
            				if(StringUtils.isNotEmpty(temp)){
                				if(StringUtils.isNumeric(temp)){
                					nDynEndRows[j] = Integer.parseInt(StringUtils.trimToEmpty(temp));
                					if(nDynEndRows[j]>65534)
                						nDynEndRows[j] = 65534;
                				}else if(temp.length() == 1){
                					char c = temp.toUpperCase().charAt(0);
                					/**
                					 * AscII A��Ӧʮ����Ϊ65 , ������չ��ʱ�����붯̬�����п������� a,b,c����ʾ����ı�ʶ.
                					 * �ڱ����߿����,��λλ�ò�ȡ���������������ʽ����ʾ,���������Ϊ���ֱ�ʾ,���Դ˴�
                					 * �����̬������������c ���� d ���ӦӦ����  3 ���� 4��.�����ʵ������������ת��
                					 */
                					if(Character.isLetter(c)){
                						nDynEndRows[j] = c - 64;
                					}
                				}else if(temp.length()==2){
                					char c1 = temp.toUpperCase().charAt(0);
                					char c2 = temp.toUpperCase().charAt(1);
                					if(Character.isLetter(c1) && Character.isLetter(c2)){
                						int h = c1 - 64;
                						int l = c2 - 64;
                						nDynEndRows[j] = 26 * h + l;
                						if(nDynEndRows[j]>512)
                							nDynEndRows[j] = 512;
                					}
                				}
            				}

            			}catch (Exception e) {
            				AppDebug.debug(e);
						}

				}
            }
            //���ñ��������Ϣ
            importUtil.reInit(strRepCode,cellsModel, strSheetName, nDynEndRows);
            //����Excel׼��������Ϣ(CellsModel)
            importUtil.processImportData(isNeedSave);
        }
    }
    /**
     * �õ�MultiSheetImportUtilʵ��
     *      ע�⣺�ǲ������屨����Ϣ��ʹ��ǰ��Ҫ���� MultiSheetImportUtil.reInit����
     * @param strTaskPK
     * @param curUserInfoVO
     * @param strImportExcelDataClassPath
     * @param dataSource
     * @param importUtil
     * @param pubdataVo
     * @return
     */
    public static MultiSheetImportUtil getImportUtilBase(String strRepPK,String strUserPK, DataSourceVO dataSource, MeasurePubDataVO pubdataVo,String strOrgPK,String strRmsPK,String strGroupPK,boolean bAutoCalc,String strLoginDate) {
    	return new MultiSheetImportUtil(strRepPK,null,null,null,pubdataVo,strUserPK,dataSource,strOrgPK,strRmsPK,strGroupPK,bAutoCalc,strLoginDate);
    }
    /**
     * ���Ԥƥ����
     * @param matchMap
     * @throws TableInputException
     */
    public static void checkMatchMap(Hashtable matchMap) throws TableInputException {
        if(matchMap == null || matchMap.size() <= 0)
            throw new TableInputException("miufo1002743");  //"�����Excel�ļ���û�й�����"
    }

    /**
     * ��ʽ�Ƿ���֧��ֱ�ӵ���ı���
     * modify by guogang 2007-11-30
     * ֧��¼��ĺ���̬����ı���
     * @param repId
     * @return
     * @throws Exception
     */
    public static boolean isCanImportDirectedRep(String repId) throws Exception {
        //�����Ƿ��ж�̬����
        boolean bHaveDynArea = isHaveDynArea(repId);
        return !bHaveDynArea;
    }

    /**
     * �����Ƿ��ж�̬����
     * @param repId
     * @return
     */
    private static boolean isHaveDynArea(String repId) {
        if(repId == null){
            return false;
        }
        boolean isHaveDynArea = false;
        ReportCache reportCache = UFOCacheManager.getSingleton().getReportCache();
        String[] repKgPks = reportCache.getKeyCombs(repId);
        if(repKgPks != null){
            //�������Ĺؼ��������������1����϶�������̬��
            if(repKgPks.length > 1){
                isHaveDynArea = true;
            }
        }
        return isHaveDynArea;
    }

    static class DualDimMap extends DualHashBidiMap{

    	private static final long serialVersionUID = -535566992397986798L;

        /**
         * ���� key �����ڵ�ǰ map�е� key����value,��˼�ֵ�Ա��Ƴ� �����ظü�ֵ��
         * String[0]Ϊkey String[1] Ϊ value
         * ���û�б�����򷵻�null
         * @create by wuyongc at 2012-2-7,����9:53:39
         *
         * @param key
         * @return
         */
    	@SuppressWarnings("unchecked")
        public String[] removeDimKeyValue(String key){
        	String[] str = new String[2];
        	if(containsKey(key)){
        		str[0] = key;
        		str[1] = (String)get(key);
        	}else if(containsValue(key)){
        		str[0] = (String)maps[1].get(key);
        		str[1] = key;
        	}else{
        		for(int i=0; i<2; i++){
					Set<String> set = maps[i].keySet();
					List<String> list = new ArrayList<String>(set);
					Collections.sort(list, new Comparator<String>(){
						@Override
						public int compare(String o1, String o2) {
							int len = o2.length()-o1.length();
							if(len == 0)
								len = o1.compareTo(o2);
							return len;
						}
					});
            		for(String s : list){
            			if(key.indexOf(s) != -1){
            				if(i == 0){
            					str[0] = s;
            	        		str[1] = (String)get(s);
            				}else{
            					str[0] = (String)maps[1].get(s);
            	        		str[1] = s;
            				}
            				maps[i].remove(s);
            				return str;
            			}
            		}
        		}
        		return null;
        	}
			return str;
        }
    }

    public static void processImpData(MultiSheetImportUtil importUtil, List listImportInfos, boolean isNeedSave, IContext context,Workbook workbook) throws CommonException, BusinessException {
        if(importUtil == null || listImportInfos == null || listImportInfos.size() <=0){
            return;
        }
        int nValidSheetSize = listImportInfos.size();
        for(int i = 0 ; i < nValidSheetSize; i++){
            Object[] objImportInfos = (Object[])listImportInfos.get(i);//{sheetname,repcode,dynendrow,cellsModel}
            String strSheetName = (String)objImportInfos[0];
            String strRepCode = (String)objImportInfos[1];
            String nDynEndRowStr = (String)objImportInfos[2];
            CellsModel cellsModel=UfoExcelImpUtil.getCellsModelByExcel(workbook.getSheet((String)objImportInfos[0]),workbook,context, true);
            if (cellsModel==null){
                continue;
            }
            nDynEndRowStr = StringUtils.trimToEmpty(nDynEndRowStr);
            String[] dynEndStrs = nDynEndRowStr.split(",");
            int []nDynEndRows = new int[dynEndStrs.length];

            if(dynEndStrs != null ){
            	for (int j = 0; j < nDynEndRows.length; j++) {
            		nDynEndRows[j] = -1;
            			try{
            				String temp = StringUtils.trimToEmpty(dynEndStrs[j]);
            				if(StringUtils.isNotEmpty(temp)){
                				if(StringUtils.isNumeric(temp)){
                					nDynEndRows[j] = Integer.parseInt(StringUtils.trimToEmpty(temp));
                					if(nDynEndRows[j]>65534)
                						nDynEndRows[j] = 65534;
                				}else if(temp.length() == 1){
                					char c = temp.toUpperCase().charAt(0);
                					/**
                					 * AscII A��Ӧʮ����Ϊ65 , ������չ��ʱ�����붯̬�����п������� a,b,c����ʾ����ı�ʶ.
                					 * �ڱ����߿����,��λλ�ò�ȡ���������������ʽ����ʾ,���������Ϊ���ֱ�ʾ,���Դ˴�
                					 * �����̬������������c ���� d ���ӦӦ����  3 ���� 4��.�����ʵ������������ת��
                					 */
                					if(Character.isLetter(c)){
                						nDynEndRows[j] = c - 64;
                					}
                				}else if(temp.length()==2){
                					char c1 = temp.toUpperCase().charAt(0);
                					char c2 = temp.toUpperCase().charAt(1);
                					if(Character.isLetter(c1) && Character.isLetter(c2)){
                						int h = c1 - 64;
                						int l = c2 - 64;
                						nDynEndRows[j] = 26 * h + l;
                						if(nDynEndRows[j]>512)
                							nDynEndRows[j] = 512;
                					}
                				}
            				}

            			}catch (Exception e) {
            				AppDebug.debug(e);
						}
				}
            }
            //���ñ��������Ϣ
            importUtil.reInit(strRepCode,cellsModel, strSheetName, nDynEndRows);
            //����Excel׼��������Ϣ(CellsModel)
            importUtil.processImportData(isNeedSave);
        }
    }
    
    public static void processImpData(MultiSheetImportUtil importUtil, List listImportInfos, boolean isNeedSave, IContext context,String fileName) throws CommonException, BusinessException {
        if(importUtil == null || listImportInfos == null || listImportInfos.size() <=0){
            return;
        }
        int nValidSheetSize = listImportInfos.size();
        BigXxlsHandler xxlHandler = new BigXxlsHandler(fileName);
        
        for(int i = 0 ; i < nValidSheetSize; i++){
            Object[] objImportInfos = (Object[])listImportInfos.get(i);//{sheetname,repcode,dynendrow,cellsModel}
            String strSheetName = (String)objImportInfos[0];
            String strRepCode = (String)objImportInfos[1];
            String nDynEndRowStr = (String)objImportInfos[2];
            //
            int sheetId = Integer.parseInt((String)objImportInfos[3]);
            CellsModel cellsModel = xxlHandler.procellSheet2CellsModel(sheetId);
//            CellsModel cellsModel=UfoExcelImpUtil.getCellsModelByExcel(workbook.getSheet((String)objImportInfos[0]),workbook,context, true);
            if (cellsModel==null){
                continue;
            }
            nDynEndRowStr = StringUtils.trimToEmpty(nDynEndRowStr);
            String[] dynEndStrs = nDynEndRowStr.split(",");
            int []nDynEndRows = new int[dynEndStrs.length];

            if(dynEndStrs != null ){
            	for (int j = 0; j < nDynEndRows.length; j++) {
            		nDynEndRows[j] = -1;
            			try{
            				String temp = StringUtils.trimToEmpty(dynEndStrs[j]);
            				if(StringUtils.isNotEmpty(temp)){
                				if(StringUtils.isNumeric(temp)){
                					nDynEndRows[j] = Integer.parseInt(StringUtils.trimToEmpty(temp));
                					if(nDynEndRows[j]>65534)
                						nDynEndRows[j] = 65534;
                				}else if(temp.length() == 1){
                					char c = temp.toUpperCase().charAt(0);
                					/**
                					 * AscII A��Ӧʮ����Ϊ65 , ������չ��ʱ�����붯̬�����п������� a,b,c����ʾ����ı�ʶ.
                					 * �ڱ����߿����,��λλ�ò�ȡ���������������ʽ����ʾ,���������Ϊ���ֱ�ʾ,���Դ˴�
                					 * �����̬������������c ���� d ���ӦӦ����  3 ���� 4��.�����ʵ������������ת��
                					 */
                					if(Character.isLetter(c)){
                						nDynEndRows[j] = c - 64;
                					}
                				}else if(temp.length()==2){
                					char c1 = temp.toUpperCase().charAt(0);
                					char c2 = temp.toUpperCase().charAt(1);
                					if(Character.isLetter(c1) && Character.isLetter(c2)){
                						int h = c1 - 64;
                						int l = c2 - 64;
                						nDynEndRows[j] = 26 * h + l;
                						if(nDynEndRows[j]>512)
                							nDynEndRows[j] = 512;
                					}
                				}
            				}

            			}catch (Exception e) {
            				AppDebug.debug(e);
						}
				}
            }
            //���ñ��������Ϣ
            importUtil.reInit(strRepCode,cellsModel, strSheetName, nDynEndRows);
            //����Excel׼��������Ϣ(CellsModel)
            importUtil.processImportData(isNeedSave);
        }
        xxlHandler.close();
    }

    /**
     * 
     * @param file
     * @return
     */
	public static byte[] getFileBytes(File file) {
		byte[] bytes = null;
        if(file != null){
        	if(file.exists()){
        		ByteArrayOutputStream baos;
					try {
						baos = FileUtils.getByteStreamFromFile(file);
						bytes = baos.toByteArray();
	            		} catch (Exception e) {
						AppDebug.debug(e);
					}
        	}else{
        		return null;
        	}
        }
		return bytes;
	}
}
