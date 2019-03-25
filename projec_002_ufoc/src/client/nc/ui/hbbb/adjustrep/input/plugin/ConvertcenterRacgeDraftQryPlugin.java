package nc.ui.hbbb.adjustrep.input.plugin;

import nc.ui.hbbb.adjustrep.input.plugin.action.ConvertEndAction;
import nc.ui.hbbb.adjustrep.input.plugin.action.ConvertRacgeDraftAction;

import com.ufida.zior.plugin.AbstractPlugin;
import com.ufida.zior.plugin.IPluginAction;
/**
 * 
 * <p>
 * 折算数据中心区域工作底稿查询
 * </p>
 *
 * 修改记录：<br>
 * <li>修改人：修改日期：修改内容：</li>
 * <br><br>
 *
 * @see 
 * @author wangxwb
 * @version V6.0
 * @since V6.0 创建时间：2011-6-10 下午01:26:39
 */
@SuppressWarnings("restriction")
public class ConvertcenterRacgeDraftQryPlugin extends AbstractPlugin {

	@Override
	protected IPluginAction[] createActions() {
		return new IPluginAction[]{new ConvertRacgeDraftAction(),new ConvertEndAction()};
	}

	@Override
	public void startup() {
	}

	@Override
	public void shutdown() {
	}

}
