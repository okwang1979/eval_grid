/*
 * 创建日期 2006-9-8
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
     * 按照先名称后编码的顺序自动进行匹配，如果当前报表已经打开，而且导入的Excel中也只有一张报表，则直接生成匹配对儿返回
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

    	//对每一个工作表进行匹配，然后进行记录，
        //匹配成功的话按照（工作表名称：报表编码）为一组；
        //匹配不成功，则需要将对该工作表设置默认值，表示没有匹配
//        Map<String, String> repNMCMap = new HashMap<String, String>();
//        Map<String, String> repCMNMap = new HashMap<String, String>();
        DualDimMap dualMap = new DualDimMap();

        if(workBook != null){
            int sheetNum = workBook.getNumberOfSheets();
            String sheetName = null;
            String repCode = null;
            String repName = null;
            if(sheetNum == 1 && strCurRepPK!=null){
            	//现在不这么要求匹配当前打开报表
//                if(strCurRepPK == null){
//                    throw new TableInputException("miufo1002752");//当前系统中没有打开Excel数据要导入的目标报表  //"当前系统中没有打开Excel数据要导入的目标报表"+"!"
//                }
                sheetName = workBook.getSheetName(0);
                ChooseRepData chooseRepData = getCurRepData(chooseRepDatas,strCurRepPK);
                if(chooseRepData != null ){
                    repName = chooseRepData.getReportName();
                    repCode = chooseRepData.getReportCode();
                    //将匹配的值记录下来，并保证一张IUFO报表只能对应一张sheet
                    matchMap.put(sheetName, new String[]{repName, repCode});
                }
            }
            else{
                //构造报表名称报表编码对照表
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
                //对sheet名称排序,首先按名称从长到短排序,提高模糊匹配的准确性.
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
                    //将匹配的值记录下来，并保证一张IUFO报表只能对应一张sheet
                    matchMap.put(sheetName, new String[]{repName, repCode});
                }
            }
            else{
                //构造报表名称报表编码对照表
                int nRepCount = chooseRepDatas.length;
                for(int j = 0; j < nRepCount; j++){
                    dualMap.put(chooseRepDatas[j].getReportName(), chooseRepDatas[j].getReportCode());
                }
                //对sheet名称排序,首先按名称从长到短排序,提高模糊匹配的准确性.
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
     * 从当前可导入的报表信息集合中找到指定报表信息
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
     * 从当前可导入的报表信息集合中找到指定报表信息
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
     * 得到导入Excel准备数据信息，具有可传输性
     *   包括通过配置信息，将sheet转换为CellsModel
     * @param array
     * @param workBook
     * @return List,其中每个Elemet是Object[]{sheetname,repcode,dynendrow,cellsModel}
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
                //得到sheet
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
     * 读取Excel中的sheet信息
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
     * 获得动态区匹配表格的Hearder信息数组
     * @return
     */
    public static String[] getTableColumns() {
        String[] columns = new String[]{
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0590")/*@res "IUFO报表"*/,  //"IUFO报表"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0941")/*@res "Excel工作表"*/,  //"Excel工作表"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0942")/*@res "动态区结束行"*/};  //"动态区结束行位置"
        return columns;
    }

    public static String[] getSingleImpTableColumns() {
        String[] columns = new String[]{
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1220")/*@res "匹配关键字"*/,  //"匹配关键字"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0941")/*@res "Excel工作表"*/,  //"Excel工作表"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0942")/*@res "动态区结束行"*/};  //"动态区结束行位置"
        return columns;
    }

    public static String[] getIufoSingleImpTableColumns() {
        String[] columns = new String[]{
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1222")/*@res "匹配关键字值"*/,  //"匹配关键字值"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1223")/*@res "来源关键字组合值"*/,  //"来源关键字组合值"
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-1224")/*@res "目标关键字组合值"*/};  //"目标关键字组合值"
        return columns;
    }
    
    public static void processImportData(MultiSheetImportUtil importUtil, List listImportInfos, boolean isNeedSave) throws CommonException, BusinessException {
    	processImportData(importUtil, listImportInfos, isNeedSave,null);
    }

    /**
     * 将导入Excel准备数据信息(CellsModel),导入到数据库
     * @param importUtil
     * @param listImportInfos List,其中每个Elemet是Object[]{sheetname,repcode,dynendrow,cellsModel}
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
                					 * AscII A对应十进制为65 , 横向扩展的时候输入动态结束列可以输入 a,b,c来表示纵向的标识.
                					 * 在报表工具框架中,定位位置采取的是行列坐标的形式来表示,横向纵向均为数字表示,所以此处
                					 * 如果动态区结束列输入c 或者 d 其对应应该是  3 或者 4列.故如此实现这样的数据转换
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
            //设置报表相关信息
            importUtil.reInit(strRepCode,cellsModel, strSheetName, nDynEndRows);
            //导入Excel准备数据信息(CellsModel)
            importUtil.processImportData(isNeedSave);
        }
    }
    /**
     * 得到MultiSheetImportUtil实例
     *      注意：是不含具体报表信息，使用前需要调用 MultiSheetImportUtil.reInit方法
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
     * 检查预匹配结果
     * @param matchMap
     * @throws TableInputException
     */
    public static void checkMatchMap(Hashtable matchMap) throws TableInputException {
        if(matchMap == null || matchMap.size() <= 0)
            throw new TableInputException("miufo1002743");  //"导入的Excel文件中没有工作表"
    }

    /**
     * 格式是否是支持直接导入的报表
     * modify by guogang 2007-11-30
     * 支持录入的含动态区域的报表
     * @param repId
     * @return
     * @throws Exception
     */
    public static boolean isCanImportDirectedRep(String repId) throws Exception {
        //报表是否含有动态区域
        boolean bHaveDynArea = isHaveDynArea(repId);
        return !bHaveDynArea;
    }

    /**
     * 报表是否含有动态区域
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
            //如果报表的关键字组合数量大于1，则肯定包含动态区
            if(repKgPks.length > 1){
                isHaveDynArea = true;
            }
        }
        return isHaveDynArea;
    }

    static class DualDimMap extends DualHashBidiMap{

    	private static final long serialVersionUID = -535566992397986798L;

        /**
         * 不论 key 包含于当前 map中的 key或者value,则此键值对被移除 并返回该键值对
         * String[0]为key String[1] 为 value
         * 如果没有被溢出则返回null
         * @create by wuyongc at 2012-2-7,上午9:53:39
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
                					 * AscII A对应十进制为65 , 横向扩展的时候输入动态结束列可以输入 a,b,c来表示纵向的标识.
                					 * 在报表工具框架中,定位位置采取的是行列坐标的形式来表示,横向纵向均为数字表示,所以此处
                					 * 如果动态区结束列输入c 或者 d 其对应应该是  3 或者 4列.故如此实现这样的数据转换
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
            //设置报表相关信息
            importUtil.reInit(strRepCode,cellsModel, strSheetName, nDynEndRows);
            //导入Excel准备数据信息(CellsModel)
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
                					 * AscII A对应十进制为65 , 横向扩展的时候输入动态结束列可以输入 a,b,c来表示纵向的标识.
                					 * 在报表工具框架中,定位位置采取的是行列坐标的形式来表示,横向纵向均为数字表示,所以此处
                					 * 如果动态区结束列输入c 或者 d 其对应应该是  3 或者 4列.故如此实现这样的数据转换
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
            //设置报表相关信息
            importUtil.reInit(strRepCode,cellsModel, strSheetName, nDynEndRows);
            //导入Excel准备数据信息(CellsModel)
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
