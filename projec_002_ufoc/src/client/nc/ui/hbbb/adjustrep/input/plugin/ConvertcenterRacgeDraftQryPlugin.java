package nc.ui.hbbb.adjustrep.input.plugin;

import nc.ui.hbbb.adjustrep.input.plugin.action.ConvertEndAction;
import nc.ui.hbbb.adjustrep.input.plugin.action.ConvertRacgeDraftAction;

import com.ufida.zior.plugin.AbstractPlugin;
import com.ufida.zior.plugin.IPluginAction;
/**
 * 
 * <p>
 * �������������������׸��ѯ
 * </p>
 *
 * �޸ļ�¼��<br>
 * <li>�޸��ˣ��޸����ڣ��޸����ݣ�</li>
 * <br><br>
 *
 * @see 
 * @author wangxwb
 * @version V6.0
 * @since V6.0 ����ʱ�䣺2011-6-10 ����01:26:39
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
