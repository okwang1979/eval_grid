package nc.ui.hbbb.dxrelation.formula.refprocessor;

import nc.vo.hbbb.dxchoosekey.ChooseKeyVO;

import com.ufida.dataset.IContext;
import com.ufsoft.script.function.IufoRefProcessor;
import com.ufsoft.script.function.UfoFuncInfo;
public class HBBBKeyWordRefProcessor extends IufoRefProcessor{

	@Override
	public void doRefAction(IContext context, UfoFuncInfo funcInfo) {
		HBBBKeyWordSelectDlg refDialog = new HBBBKeyWordSelectDlg(inputPane, null,funcInfo);
		refDialog.setLocationRelativeTo(inputPane);
		refDialog.setModal(true);
		refDialog.show();
		ChooseKeyVO vo = refDialog.getRefVO();
		Object refvo = refDialog.getResultvo();
		if (vo != null) {
			paramText.setText("\'"+vo.getName1()+"\'"); 
		}
	}
}
