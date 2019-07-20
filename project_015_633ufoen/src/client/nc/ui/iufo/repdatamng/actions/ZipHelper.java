package nc.ui.iufo.repdatamng.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nc.bs.logging.Logger;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.FileUtils;

import com.ufida.iufo.pub.tools.AppDebug;

/**
 * @author xujinf
 * 
 */
public class ZipHelper {

	private ZipHelper() {
	}

	/**
	 * zip���
	 * 
	 * @return
	 */
	public static File zip(File zipFile, File[] files, String destPath,
			String password, boolean delete) throws Exception {
		ZipFile respFile = new ZipFile(zipFile);
		for (File file : files) {
			// ����ѹ���ļ�
			// ����ѹ���ļ�����
			ZipParameters parameters = new ZipParameters();
			// ����ѹ������
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			// ����ѹ������
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			// ����ѹ���ļ�����
			parameters.setEncryptFiles(true);
			// ���ü��ܷ���
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
			// ����aes����ǿ��
			parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
			// ��������
			parameters.setPassword(password);
			// ����ļ���ѹ���ļ�
			respFile.addFile(file, parameters);
			// ɾ���ļ�
			if (delete) {
				file.delete();
			}
		}
		return zipFile;
	}

	/**
	 * ��ѹzip��
	 * 
	 * @param file
	 * @return ��ѹ�ļ���
	 */
	public static File[] unZip(File zipFile, String destPath, String password) {

		try {
			String outDir = System.getProperty("java.io.tmpdir");
			/*
			 * if (outDir.isEmpty()) { outDir =
			 * System.getProperty("java.io.tmpdir"); } else { outDir = outDir +
			 * "/nclogs"; }
			 */

			ZipFile respFile = new ZipFile(zipFile);
			respFile.setPassword(password);
			respFile.extractAll(outDir);
			List<FileHeader> headerList = respFile.getFileHeaders();

			List<File> extractedFileList = new ArrayList<File>();

			for (FileHeader fileHeader : headerList) {
				if (!fileHeader.isDirectory()) {
					extractedFileList.add(new File(outDir, fileHeader
							.getFileName()));
				}
			}
			return extractedFileList.toArray(new File[0]);

		} catch (Exception ex) {
			AppDebug.error(ex);
			return null;
		}

		// try {
		//
		//
		//
		//
		// ZipFile zipPkg = new ZipFile(zipFile);
		// if (!zipPkg.isValidZipFile()) {
		// throw new ZipException("ѹ���ļ����Ϸ�,���ܱ���.");
		// }
		// if (zipPkg.isEncrypted()) {
		// zipPkg.setPassword(password);
		// }
		//
		// File destDir = new File(destPath);
		// if (destDir.exists()) {
		// FileUtils.deleteDirectory(destDir);
		// }
		// destDir.mkdir();
		//
		// zipPkg.extractAll(destPath);
		//
		// return destDir.listFiles(new ExcelFileFilter());
		// } catch (Exception e) {
		// Logger.error(e.getMessage(), e);
		// return null;
		// }

	}

}
