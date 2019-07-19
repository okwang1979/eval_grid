package nc.ui.iufo.repdatamng.actions;
import java.io.File;
import java.util.List;

import nc.util.hbbb.input.HBBBTableInputActionHandler;
import nc.vo.iufo.task.TaskVO;
import nc.vo.pub.BusinessException;

import com.ufida.zior.console.ActionHandler;
import com.ufsoft.iufo.func.excel.text.ImpExpFileNameUtil;
import com.ufsoft.iufo.inputplugin.biz.data.ImportExcelDataBizUtil;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;
import com.ufsoft.iuforeport.repdatainput.TableInputActionHandler;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
public class SingleExcelImportTask {

	private IRepDataParam params;
	private Object[] objs;
	private LoginEnvVO loginEnvVO;
	private TaskVO task;
	private ImpOrgAndReport vo;

	public SingleExcelImportTask(IRepDataParam params, Object[] objs, LoginEnvVO loginEnvVO, TaskVO task, ImpOrgAndReport vo) {
		this.params = params;
		this.objs = objs;
		this.loginEnvVO = loginEnvVO;
		this.task = task;
		this.vo = vo;
	}

	@SuppressWarnings("unchecked")
	public void run() throws Exception {
		File file = (File) objs[3];
		List<Object[]> vParams = (List<Object[]>) objs[0];
		boolean bAutoCal = ((Boolean) objs[1]).booleanValue();
		String extendName = ImpExpFileNameUtil.getExtendName(file.getPath());
		byte[] bytes = ImportExcelDataBizUtil.getFileBytes(file);
		RepDataOperResultVO resultVO = (RepDataOperResultVO) ActionHandler.execWithZip(HBBBTableInputActionHandler.class.getName(), "importExcelData", //
				new Object[] { params, loginEnvVO,vParams   , Boolean.valueOf(bAutoCal), task, bytes, extendName });

		// queryExecutor.reQuery();
		//
		if (resultVO.getHintMessage() != null) {
			throw new BusinessException(resultVO.getHintMessage());
		}
	}

	public ImpOrgAndReport getVo() {
		return vo;
	}

	public IRepDataParam getParams() {
		return params;
	}

	public void setParams(IRepDataParam params) {
		this.params = params;
	}

	public Object[] getObjs() {
		return objs;
	}

	public void setObjs(Object[] objs) {
		this.objs = objs;
	}

	public LoginEnvVO getLoginEnvVO() {
		return loginEnvVO;
	}

	public void setLoginEnvVO(LoginEnvVO loginEnvVO) {
		this.loginEnvVO = loginEnvVO;
	}

	public TaskVO getTask() {
		return task;
	}

	public void setTask(TaskVO task) {
		this.task = task;
	}

	public void setVo(ImpOrgAndReport vo) {
		this.vo = vo;
	}
	
	

}
