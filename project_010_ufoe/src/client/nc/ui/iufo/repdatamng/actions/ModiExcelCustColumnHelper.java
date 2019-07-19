package nc.ui.iufo.repdatamng.actions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import nc.bs.framework.common.NCLocator;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.ui.iufo.repdatamng.actions.ZCUseConstant;
import nc.vo.pub.BusinessException;

public class ModiExcelCustColumnHelper {

	public static void modiExcelCustColumn(File file) throws Exception {
		// ��������������ȡExcel
		Map<String, List<String>> oneColumnData = new HashMap<String, List<String>>();
		Map<String, List<String>> twoColumnData = new HashMap<String, List<String>>();
		InputStream is = new FileInputStream(file.getAbsolutePath()); // jxl�ṩ��Workbook��
		Workbook wb = Workbook.getWorkbook(is); // ֻ��һ��sheet,ֱ�Ӵ��� //����һ��Sheet����
		Sheet[] sheets = wb.getSheets();
		for (Sheet sheet : sheets) {
			int rows = sheet.getRows(); // ���е�����
			List<String> oneColumnTexts = new ArrayList<String>();
			List<String> twoColumnTexts = new ArrayList<String>();// ����������
			for (int row = 1; row < rows; row++) {
				Cell[] cells = sheet.getRow(row);

				for (int column = 0; column < 2; column++) {

					if (column == 0) {
						oneColumnTexts.add(cells[column].getContents().trim());
					} else {
						twoColumnTexts.add(cells[column].getContents().trim());
					}

				} // �洢ÿһ������
			}
			oneColumnData.put(sheet.getName(), oneColumnTexts);
			twoColumnData.put(sheet.getName(), twoColumnTexts);
		}
		updateExcel(file, oneColumnData, twoColumnData);
	}

	private static void updateExcel(File file,
			Map<String, List<String>> oneColumnData, Map<String, List<String>> twoColumnData) {
		int index = -1;
		Map<String,Integer>  sheetCustColumnMap = new  HashMap<>();
		if (oneColumnData != null || twoColumnData != null) {
			Set<String> excelOrgName = new HashSet<String>();
			for (String key : oneColumnData.keySet()) {
				List<String> A1 = oneColumnData.get(key);
				Boolean temp = false;
				for (int i = 0; i < A1.size(); i++) {
					if (temp && A1.get(i) != null && !"�ϼ�".equals(A1.get(i))
							&& !"".equals(A1.get(i))) {
						excelOrgName.add(A1.get(i));

					}
//					if (A1.get(i).compareTo("�Է���λ") == 0
//							&& "�Է���λ".equals(A1.get(i))) {
					if(ArrayUtils.contains(ZCUseConstant.FILTER_COLUMN, A1.get(i))){
						temp = true;
						index = 0;
						sheetCustColumnMap.put(key, 0);
					}

				}
			}

			for (String key : twoColumnData.keySet()) {
				List<String> B1 = twoColumnData.get(key);
				Boolean temp = false;
				for (int i = 0; i < B1.size(); i++) {
					if (temp && B1.get(i) != null && !"�ϼ�".equals(B1.get(i))
							&& !"".equals(B1.get(i))) {
						excelOrgName.add(B1.get(i));
					}
//					if (B1.get(i).compareTo("�Է���λ") == 0
//							&& "�Է���λ".equals(B1.get(i))) {
					if(ArrayUtils.contains(ZCUseConstant.FILTER_COLUMN, B1.get(i))){
						temp = true;
						index = 1;
						sheetCustColumnMap.put(key, 1);
					}

				}
			}

//			StringBuffer where = new StringBuffer();
//			where.append("select name,pk_org from org_orgs where name in (");
//			for (String str : excelOrgName) {
//				where.append("'").append(str).append("',");
//			}
//			where.append("'1')");
//			IUAPQueryBS service = NCLocator.getInstance().lookup(
//					IUAPQueryBS.class);
//			List<Map<String, String>> resultListMap;
			try {
//				resultListMap = (List<Map<String, String>>) service
//						.executeQuery(where.toString(), new MapListProcessor());
//				Set<String> simeSet = new HashSet<>();
//				if (!resultListMap.isEmpty() && resultListMap.size() > 0) {
//					for (Map<String, String> map : resultListMap) {
//						for (String str : excelOrgName) {
//							if (str.equals(map.get("name"))) {
//								simeSet.add(str);
//
//							}
//						}
//					}
//				}
//				if (simeSet.size() > 0) {
//					excelOrgName.removeAll(simeSet);
//				}
//				if (excelOrgName != null && excelOrgName.size() > 0) {
//					List<String> ls = new ArrayList<String>();
//					for (String str : excelOrgName) {
//						ls.add(str);
//					}

//					List<Map<String, String>> ss = sourceOrgCode(ls);
					readExcel1(file, sourceOrgCode(), sheetCustColumnMap);
//				}
			} catch (Exception e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}

		}

	}
	
	
	
	private static List<Map<String, String>> sourceOrgCode() {
		IUAPQueryBS service = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		StringBuffer where = new StringBuffer();
		where.append(" select b.exsysval,b.bdname ");
		where.append(" from xx_bdcontra a, xx_bdcontra_b b, xx_exsystem c ");
		where.append(" where a.pk_contra = b.pk_contra ");
		where.append(" and a.exsystem = c.pk_exsystem ");
		where.append(" and c.exsystemcode = 'QYBB' ");
		 
		List<Map<String, String>> resultListMap;
		try {
			resultListMap = (List<Map<String, String>>) service.executeQuery(
					where.toString(), new MapListProcessor());
			if (!resultListMap.isEmpty() && resultListMap.size() > 0) {
				List<String> res = new ArrayList<String>();
				for (Map<String, String> map : resultListMap) {
					res.add(map.get("exsysval"));
				}
				return resultListMap;
			}
		} catch (BusinessException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}

		return null;

	}

	 

	private static void readExcel1(File file, List<Map<String, String>> ss,
			Map<String, Integer> sheetCustColumnMap) {
		if(ss==null) return;
		InputStream is =null;
		jxl.write.WritableWorkbook wbe = null;
		try {
			jxl.Workbook wb = null; // ����һ��workbook����
		
			String excelpath = file.getAbsolutePath();

			is = new FileInputStream(excelpath); // ����һ���ļ���������Excel�ļ�
			wb = Workbook.getWorkbook(is); // ���ļ���д�뵽workbook����

			// jxl.Workbook ������ֻ���ģ��������Ҫ�޸�Excel����Ҫ����һ���ɶ��ĸ���������ָ��ԭExcel�ļ�
			 wbe = Workbook.createWorkbook(new File(
					excelpath), wb);// ����workbook�ĸ���
			int sheet_size = wbe.getNumberOfSheets();

			for (int index = 0; index < sheet_size; index++) {
				// ÿ��ҳǩ����һ��Sheet����
				WritableSheet sheet = wbe.getSheet(index); // ��ȡsheet
				Integer col = sheetCustColumnMap.get(sheet.getName());
				if(col==null){
					continue;
				}
				// sheet.getColumns()���ظ�ҳ��������
				int column_total = sheet.getRows()/* getColumns() */;
				for (int j = 0; j < column_total; j++) {
					String cellinfo = sheet.getCell(col, j).getContents();
					WritableCell cell = sheet.getWritableCell(col, j); // ��ȡ��һ�е����е�Ԫ��
					jxl.format.CellFormat cf = cell.getCellFormat();// ��ȡ��һ����Ԫ��ĸ�ʽ
					for (Map<String, String> map : ss) {
						if (map.get("exsysval").equals(cellinfo)) {
							jxl.write.Label lbl = new jxl.write.Label(col, j,
									map.get("bdname"));// �޸����ֵ
							lbl.setCellFormat(cf); // ���޸ĺ�ĵ�Ԫ��ĸ�ʽ�趨�ɸ�ԭ��һ��
							sheet.addCell(lbl); // ���Ĺ��ĵ�Ԫ�񱣴浽sheet
							break;
						}
					}
				}
			}
			wbe.write(); // ���޸ı��浽workbook
			wbe.close(); // �ر�workbook���ͷ��ڴ�
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BiffException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		} finally {
			
			if(wbe!=null){
			 
					try {
						wbe.close();
					} catch (Exception e) {
						 
					}
			 
			}
			
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					 
				}
			}
		
		}
	}

}
