package nc.ui.iufo.repdatamng.actions;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ZipFileFilter extends FileFilter {

	@Override
	public boolean accept(File file) {
		return file.isDirectory() ? true : file.getName().toLowerCase().endsWith(".zip");
	}

	@Override
	public String getDescription() {
		return "ZIP (*.zip)";
	}

}
