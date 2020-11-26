package nc.util.info.sysimp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uif.pub.IUifService;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.pub.para.SysInitVO;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.ufsoft.table.CellsModel;

/**
 * ���뵼��ͨ�ù�����
 * 
 * @author ��־ǿ
 * 
 */

public class SysImpUtil {
	
	
	/**
	 * ������Ķ���ϵ�л��󣬴������stringָ�����ļ����������л���Ķ���ת����ʮ�������ַ�������
	 * @param object �����л��Ķ���
	 * @param string �洢�ļ���
	 * @return string ���л���Ķ����ʮ�������ַ���
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String writeObject(Object object)
			throws FileNotFoundException, IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//���ڽ�����ת����byte[]�����ObjectOutputStream
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		//������д��ByteArrayOutputStream
		oos.writeObject(object);
		byte[] bytes = baos.toByteArray();
		//���ڽ�����������ļ���ObjectOutputStream
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream oos2 = new ObjectOutputStream(byteArrayOutputStream);
		
//		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
//		objectStr.getBytes("ISO-8859-1"));
		//������д��stringָ�����ļ���
		oos2.writeObject(object);
		oos.close();
		oos2.close();
		baos.close();
		return bytesToHexString(bytes);

	}

	/**
	 * �����л�������ʮ�������ַ���ʾ�Ķ������л��ɶ���
	 * @param hexString ���л������ʮ�����Ʊ�ʾ��ʽ���ַ���
	 * @return �����л����ɵĶ���
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object readObject(String hexString) throws IOException,
			ClassNotFoundException {
		byte[] bytes = hexStringToBytes(hexString);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}

	/**
	 * �������byte[]����ת����ʮ�����������ַ���
	 * @param src Ҫת����byte����
	 * @return ����ʮ�����Ƶ��ַ���
	 */
	private static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			//��һ��byte�Ķ�������ת����ʮ�������ַ�
			String hv = Integer.toHexString(v);
			//�����������ת����ʮ����������λΪ0�������'0'�ַ�
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * ����������ʮ�����Ʊ�ʾ���ַ���ת����byte����
	 * @param hexString
	 * @return �����Ʊ�ʾ��byte[]����
	 */
	private static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase(Locale.getDefault());
		int length = hexString.length() / 2;
		//��ʮ�������ַ���ת�����ַ�����
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			//һ��ȥ�����ַ�
			int pos = i * 2;
			//�����ַ�һ����Ӧbyte�ĸ���λһ����Ӧ����λ
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * �����������ַ����������ת���ɶ�������
	 * @param c Ҫת�����ַ�
	 * @return ��byte���������ͷ����ַ���������ֵĶ����Ʊ�ʾ��ʽ
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

//	/**
//	 * �����л�������ʮ�������ַ���ʾ�Ķ������л��ɶ���
//	 * @param hexString ���л������ʮ�����Ʊ�ʾ��ʽ���ַ���
//	 * @return �����л����ɵĶ���
//	 * @throws IOException
//	 * @throws ClassNotFoundException
//	 */
//	public static Object readObject(String hexString) throws IOException,
//			ClassNotFoundException {
//		byte[] bytes = hexStringToBytes(hexString);
//		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//		ObjectInputStream ois = new ObjectInputStream(bais);
//		return ois.readObject();
//	}

//	/**
//	 * �������л�Ϊ�ַ���
//	 * 
//	 * @param object
//	 * @return
//	 */
//	public static String serializeObject(Object object) throws Exception {
//		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//		ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream);
//		out.writeObject(object);
//		// ������ISO-8859-1
////		String objectStr = byteArrayOutputStream.toString("ISO-8859-1");
//		String objectStr = byteArrayOutputStream.toString("UTF-8");
//		out.close();
//		byteArrayOutputStream.close();
//		return objectStr;
//	}
//
//	/**
//	 * �ַ������л�Ϊ����
//	 * 
//	 * @param objectStr
//	 * @return
//	 * @throws Exception
//	 */
//	public static Object stringSerializeObject(String objectStr)
//			throws Exception {
//		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
//				objectStr.getBytes("ISO-8859-1"));
//		ObjectInputStream objectInputStream = new ObjectInputStream(
//				byteArrayInputStream);
//		Object object = objectInputStream.readObject();
//		objectInputStream.close();
//		byteArrayInputStream.close();
//		return object;
//	}

	/**
	 * ����xml����
	 */
	public static String createXml(String report_code, KeyVO[] keys,
			CellsModel model,MeasurePubDataVO pubVo,String repOrgStructCode,String orgCode,String taskCode ) {
		try {
			// 1������document����
			Document document = DocumentHelper.createDocument();
			// 2���������ڵ�rss
			
//			account="develop" billtype="report_sys_imp" filename="" groupcode="" isexchange="Y" replace="Y" roottag="" sender="U8"
			Element rss = document.addElement("ufinterface");
			// 3����rss�ڵ����version����
			rss.addAttribute("account", "develop");
			rss.addAttribute("billtype", "report_sys_imp");
			rss.addAttribute("filename", "");
			rss.addAttribute("groupcode", "");
			rss.addAttribute("isexchange", "Y");
			rss.addAttribute("replace", "Y");
			rss.addAttribute("roottag", "");
			rss.addAttribute("sender", "default");
 
			
			// 4�������ӽڵ㼰�ӽڵ�����
			Element bill = rss.addElement("bill");
			bill.addAttribute("id","");
			
			Element billhead = bill.addElement("billhead");
			 
			Element reportCode = billhead.addElement("reportCode");
			reportCode.setText(report_code);
			
			Element taskCodeEle = billhead.addElement("taskCode");
			taskCodeEle.setText(taskCode);
 
			
			
			Element keyCode1 = billhead.addElement("keyCode1");
			 
			keyCode1.setText(keys[0].getCode());
			
			Element keyCode2 = billhead.addElement("keyCode2");
			 
			keyCode2.setText(keys[1].getCode());
			
			if(keys.length>2){
				Element keyCode3 = billhead.addElement("keyCode3");
				 
				keyCode3.setText(keys[2].getCode());
			}

			
			
			Element keyValue1 = billhead.addElement("keyValue1");
			 
			keyValue1.setText(orgCode);
			
			Element keyValue2 = billhead.addElement("keyValue2");
			 
			keyValue2.setText(pubVo.getKeyDatas()[1].getCode());
			if(keys.length>2){
			Element keyValue3 = billhead.addElement("keyValue3");
			 
			keyValue3.setText(pubVo.getKeyDatas()[2].getCode());
			}
		 
			
			if(pubVo.getVer()!=0){
				Element versionEle = billhead.addElement("keyValue5");
				 //�����ȷŹ̶��ĺ�����Ҫ����
				versionEle.setText(""+pubVo.getVer() );		
			}
			Element repStructCode = billhead.addElement("repStructCode");
			 //�����ȷŹ̶��ĺ�����Ҫ����
			repStructCode.setText(repOrgStructCode );			
			
			
			
			Element reportinfos = billhead.addElement("reportinfos");
			
			
			 
			reportinfos.setText("dd");
			
			
			Element reportData = billhead.addElement("reportData");
			String strValue = writeObject(model);
			reportData.setText(strValue);
		 
			
			
	
		 
			 
//			Object obj = readObject(strValue);
//			title.setText("sdfsdfsd");
			// 5����������xml�ĸ�ʽ
			
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			// ���ñ����ʽ
			format.setEncoding("UTF-8");

			// 6������xml�ļ�
			//�޸������ļ���������ʱ��������������ļ�begin pzm
			String fileurl = NCConnTool.getResPath();
//			IUifService service = NCLocator.getInstance().lookup(IUifService.class);
//			SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'fileurl'");
//			if(svos != null && svos.length>0){
//				fileurl = svos[0].getValue();
//			}
			
			StringBuffer filename = new StringBuffer();
			filename.append(fileurl+"\\");
			filename.append(report_code);
			filename.append(System.currentTimeMillis()).append(".xml");
			File file = new File(filename.toString());
			XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
			// �����Ƿ�ת�壬Ĭ��ʹ��ת���ַ�
			writer.setEscapeText(false);
			writer.write(document);
			writer.close();
//			System.out.println("����rss.xml�ɹ�");
			return filename.toString();
		} catch (Exception e) {
			Logger.init("iufoRepCalcResult");
			Logger.error(e);
			Logger.init();

		}
		return null;

	}
	
	
	
}
