package nc.ui.hbbb.adjustrep.input.edit;

import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import nc.bs.logging.Logger;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.pub.iufo.cache.KeywordCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.hbbb.adjustrep.input.abs.UfocAbsNavUnitTreeViewer;
import nc.ui.hbbb.adjustrep.input.control.AdjRepDataControler;
import nc.ui.hbbb.hbreport.model.ReportCombineStruMemberVerisonWithCodeNameHierModel;
import nc.ui.hbbb.hbreport.view.ReportMemTreeCellRenderer;
import nc.ui.hbbb.hbscheme.model.HBSchemeRefModel;
import nc.ui.hbbb.hbscheme.view.HBSchemeRefPanel;
import nc.ui.hbbb.view.ReportCombinStruMemberWithCodeNameTreeCreateStrategy;
import nc.ui.iufo.input.control.base.AbsRepDataControler;
import nc.ui.iufo.input.ufoe.comp.AbsCombRepDataControler;
import nc.ui.iufo.input.ufoe.comp.AbsCombRepDataEditor;
import nc.ui.iufo.input.ufoe.comp.AbsNavUnitTreeViewer;
import nc.ui.iufo.input.ufoe.comp.AbsRepDataEditorInComb;
import nc.ui.iufo.pub.UfoPublic;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.ValueChangedEvent;
import nc.ui.pub.beans.ValueChangedListener;
import nc.util.hbbb.datacenter.DataCenterType;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.vo.bd.meta.BDObjectAdpaterFactory;
import nc.vo.corg.ReportCombineStruMemberWithCodeNameVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.keydef.KeyVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.zior.docking.plaf.resources.ColorResourceHandler;
import com.ufida.zior.docking.view.actions.DefaultCloseAction;
import com.ufida.zior.docking.view.actions.DefaultMaximizeAction;
import com.ufida.zior.docking.view.actions.DefaultPinAction;
import com.ufida.zior.plugin.system.RefreshViewPlugin;
import com.ufida.zior.view.Mainboard;
import com.ufida.zior.view.Viewer;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.inputplugin.biz.file.ChangeKeywordsData;
import com.ufsoft.iufo.view.AbsReportMiniAction;
import com.ufsoft.table.re.IInputEditor;


/**
 * @modified by jiaah at 2012-1-4 ͬ��60
 * @author jiaah
 *
 */
@SuppressWarnings({ "serial", "restriction"})
public class AdjNavUnitTreeViewer extends UfocAbsNavUnitTreeViewer implements ValueChangedListener {
	transient public static String NAV_UNIT_TREE_ID = "iufo.input.dir_unit.view";

	private HBSchemeRefPanel hbSchemeRefPane=null;
	
	private String curr_hbrep_stru_version;//��ǰ���صı���ϲ��ṹ�汾��pk
	
	//modified by jiaah ����Ϊ�ϲ�ִ�еĹ��ܽڵ�ţ���ǰ�Ǹ��𱨱������
	public static final String NODECODE_ADJ = "18300GARP1";//18300SREP
	
	private String selectedOrgPK = null;
	private String currentTTime = null;
	
	//����ʱ�仺����Ȩ�޵���֯
		Map<String, Object[]> timeToPermOrgsMap = new HashMap<String, Object[]>();

	protected UIPanel getNorthPanel(){
		if (hbSchemeRefPane == null){
			hbSchemeRefPane = new HBSchemeRefPanel(true,true);
			HBSchemeRefModel model=new HBSchemeRefModel();
			hbSchemeRefPane.setHbSchemeRefModel(model);
			hbSchemeRefPane.innerInitUI();
			hbSchemeRefPane.getHbScheme_refPane().addValueChangedListener(this);
			AbsCombRepDataControler controler=(AbsCombRepDataControler)AbsRepDataControler.getInstance(getMainboard());
			setSelectedTask(controler.getSelectedTaskPK());
			hbSchemeRefPane.setBackground(ColorResourceHandler.parseHexColor("#EBEBEB"));
		}
		return hbSchemeRefPane;
	}
	
	/**
	 * ���ص�ǰ��������ʾ�ĺϲ�������pk
	 * @return
	 */
	public String excepString() {
		if(hbSchemeRefPane == null){
			return null;
		}
		return hbSchemeRefPane.getHbScheme_refPane().getRefPK();
	}

	
	@Override
	public void startup() {
		super.startup();

		removeTitleAction(DefaultPinAction.class.getName());
		//wangqi 20130312 �޸��������ͼչ���۵��Ĺ��� S
		removeTitleAction(String.valueOf((new DefaultMaximizeAction()).getValue(Action.NAME)));
		removeTitleAction(String.valueOf((new DefaultCloseAction()).getValue(Action.NAME)));
		AbsReportMiniAction action = new AbsReportMiniAction(getMainboard());
		action.setMiniid("nc.ui.hbbb.adjustrep.input.edit.AdjuNavUnitTreeMinViewer");
		addTitleAction(action) ;
		//wangqi 20130312 �޸��������ͼչ���۵��Ĺ��� E
	}
	
	@Override
	public void refresh() {
		try {
			initTreeModelData(currentTTime);
		} catch (Exception e) {
			Logger.error(e.getMessage(),e);
		}
	}
	
	@Override
	public void onViewActive(Viewer oldView, Viewer newView,boolean editorRefresh) {
		//�л�ҳǩ��ʱ����Ҫ���¸�����ģ�ͣ���ʼ���ؼ�������ʱ����ع���ģ��--modified by jiaah at 20130718
		super.onViewActive(oldView, newView,editorRefresh);
		//��֯��������Χѡ��ĵ�λ -- modified by jiaah
		String tTimeValue = null;
		AbsCombRepDataEditor combEditor = null;
		if(newView instanceof AbsCombRepDataEditor)
			 combEditor=(AbsCombRepDataEditor)newView;
		else if(oldView instanceof AbsCombRepDataEditor)
			 combEditor=(AbsCombRepDataEditor)oldView;
		if(combEditor != null && combEditor.getPubData() != null) {
			tTimeValue= combEditor.getPubData().getKeywordByPK(combEditor.getPubData().getKeyGroup().getTTimeKey().getPk_keyword());
			selectedOrgPK = combEditor.getPubData().getKeywordByPK(KeyVO.CORP_PK);
			setOrgInputValue(selectedOrgPK);
			selectUnitTreeNode(selectedOrgPK);
		}
		//Ϊ�˽���״δ��������ĵ�ʱ�򲻽���onViewActive -- modified by jiaah
		if (oldView instanceof AbsNavUnitTreeViewer || newView == oldView || newView instanceof AbsCombRepDataEditor==false || isLinkable()==false)
			return;
		try {
			if(tTimeValue != null) {
				initTreeModelData(tTimeValue);
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}
	
	private void setOrgInputValue(String pk_org) {
		KeywordCache keyCache = UFOCacheManager.getSingleton().getKeywordCache();
		if(m_keyCondPane != null){
			IInputEditor[] inputEditors = m_keyCondPane.getM_jFieldKeywords();
			ChangeKeywordsData[] keywordDatas = m_keyCondPane.getKeywordsDatas();
			for (int i=0;i<keywordDatas.length;i++){
	    		String strKeyPK = keywordDatas[i].getKeywordPK();
	    		KeyVO key = keyCache.getByPK(strKeyPK);
	    		if (KeyVO.isUnitKeyVO(key)){
	    			inputEditors[i].setValue(pk_org);
	    			break;
	    		}
	    	}
		}
	}
	
	@Override
	protected void setSelectedTask(String strTaskPK) {
		String cuurenthbschme = hbSchemeRefPane.getHbScheme_refPane().getRefPK();
		if(cuurenthbschme!=null && cuurenthbschme.equals(strTaskPK)) 
			return;
		hbSchemeRefPane.getHbScheme_refPane().setPK(strTaskPK);
		Mainboard mainBoard = getMainboard();
		mainBoard.getContext().setAttribute(IUfoContextKey.TASK_PK, strTaskPK);
//		AbsCombRepDataControler controler=(AbsCombRepDataControler)AbsRepDataControler.getInstance(getMainboard());
//		controler.setSelectedTaskPK(strTaskPK);
	}
	
	@Override
	public void initTreeModel(){
		treePane.setModel(new ReportCombineStruMemberVerisonWithCodeNameHierModel());
		treePane.getModel().setTreeCreateStrategy(new ReportCombinStruMemberWithCodeNameTreeCreateStrategy());
		treePane.getModel().setBusinessObjectAdapterFactory(new BDObjectAdpaterFactory());
		treePane.setTreeCellRenderer(new ReportMemTreeCellRenderer());
	}
	
	private String oldHBSchemePK = null;
	private String oldTime = null ;

	/**
	 * �����ڼ�ؼ��ֵ�ֵ�������ݣ��ڼ�ؼ��ֵ�ֵΪ���򲻼��أ�
	 * @create by fengzhy at 2012-3-2,����9:10:49
	 *
	 * @throws Exception
	 */
	public void initTreeModelData(String tTime) throws Exception {
		if(treePane == null) {
			return;
		}
		if(tTime == null ||tTime.trim().length() == 0) 
			return;
		
		currentTTime = tTime;
		String pk_hbscheme = hbSchemeRefPane.getHbScheme_refPane().getRefPK();
		if(pk_hbscheme == null || pk_hbscheme.trim().length() == 0)
			return;
		
		//����û�䣬ʱ��û�䣬�Ҳ�Ϊ�գ�����Ҫˢ������
		if(oldHBSchemePK != null && oldHBSchemePK.equals(pk_hbscheme) && oldTime != null && oldTime.equals(tTime)){
			if(selectedOrgPK != null) {
		    	selectUnitTreeNode(selectedOrgPK);
		    }
			else {//Ĭ��ѡ�и��ڵ�
		    	Object[] objs = treePane.getModel().getAllDatas();
		    	if(objs != null && objs.length>0) {
		    		treePane.getModel().setSelectedData(objs[0]);
		    		String pk_org = ((ReportCombineStruMemberWithCodeNameVO)objs[0]).getPk_org();
		    		setOrgInputValue(pk_org);
		    		selectedOrgPK = pk_org; 
		    	}
		    }
			return;
		}
		
		String cuerid = WorkbenchEnvironment.getInstance().getLoginUser().getCuserid();
		HBSchemeVO schemeVO = (HBSchemeVO) hbSchemeRefPane.getHbScheme_refPane().getVO();
		
//		//һ��Զ�̵��û�ȡ������Ҫ�İ汾�����ڵ����ƣ��Լ���֯vos modified by jiaah at 20130725
//		Object[] setObjs = HBBaseDocItfService.getRemoteCorpRms().queryPermissionLeftTreeDataByTime(schemeVO, tTime, cuerid, NODECODE_ADJ);
//		
		//Ч���Ż�by����־ǿ 633
		//Ȩ�����ӻ���
		Object[] setObjs = null;
		if (timeToPermOrgsMap.containsKey(pk_hbscheme + tTime + cuerid)) {
			setObjs = timeToPermOrgsMap.get(pk_hbscheme + tTime + cuerid);
		} else {
			// һ��Զ�̵��û�ȡ������Ҫ�İ汾�����ڵ����ƣ��Լ���֯vos modified by jiaah at 20130725
			setObjs = HBBaseDocItfService.getRemoteCorpRms().queryPermissionLeftTreeDataByTime(schemeVO, tTime, cuerid,NODECODE_ADJ);
			timeToPermOrgsMap.put(pk_hbscheme + tTime + cuerid, setObjs);
		}
		
		//end
		
		
		
		String hbRepStruVerPK = setObjs[1] == null ? null:setObjs[1].toString();
		
		AbsRepDataControler controler = AbsRepDataControler.getInstance(getMainboard());
		if(controler instanceof AdjRepDataControler)
			((AdjRepDataControler)controler).setSelectedHBRepStruPK(hbRepStruVerPK);
		
		if(UfoPublic.strIsEqual(hbRepStruVerPK, curr_hbrep_stru_version))
		{	
			//�µı���ϲ��汾���Ѿ����صİ汾��ͬ���������¼��� ��
		}
		else {
			ReportCombineStruMemberWithCodeNameVO[] membervos = setObjs[2] == null ? null:(ReportCombineStruMemberWithCodeNameVO[]) setObjs[2];
			((ReportCombinStruMemberWithCodeNameTreeCreateStrategy)treePane.getModel().getTreeCreateStrategy()).setStruname(
					setObjs[0] == null ? null:setObjs[0].toString());
			treePane.getModel().initModel(membervos);
		    curr_hbrep_stru_version = hbRepStruVerPK;
		    oldTime = tTime;
		    oldHBSchemePK = pk_hbscheme;
		}
	    if(selectedOrgPK != null) {
	    	selectUnitTreeNode(selectedOrgPK);
	    }
	    else {//Ĭ��ѡ�и��ڵ�
	    	Object[] objs = treePane.getModel().getAllDatas();
	    	if(objs != null && objs.length>0) {
	    		treePane.getModel().setSelectedData(objs[0]);
	    		String pk_org = ((ReportCombineStruMemberWithCodeNameVO)objs[0]).getPk_org();
	    		setOrgInputValue(pk_org);
	    		selectedOrgPK = pk_org; 
	    	}
	    }
	}
	
	@Override
	public String[] createPluginList() {
		return new String[]{RefreshViewPlugin.class.getName()};
	}

	@Override
	public void onDblClickTreeNode(JTree tree, MouseEvent event) {
		Mainboard mainBoard=getMainboard();

		AbsCombRepDataControler controler=(AbsCombRepDataControler)AbsRepDataControler.getInstance(mainBoard);
		DataCenterType attribute = (DataCenterType) mainBoard.getContext().getAttribute(DataCenterType.HBBB_DATACENTER);
		String strUnitPK=controler.getSelectedUnitPK();
		getMainboard().getContext().setAttribute(IUfoContextKey.CUR_REPORG_PK, strUnitPK);
		if(DataCenterType.HB.equals(attribute)|| DataCenterType.HB_ADJUST.equals(attribute)){
			//�ϲ�����Ҷ�ڵ㲻���ںϲ�����
			//���������ȡ����ѡ�е�����
//			ReportManaStruMemberExVO selectedData = (ReportManaStruMemberExVO) treePane.getModel().getSelectedData();

			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			ReportCombineStruMemberWithCodeNameVO selectedData = (ReportCombineStruMemberWithCodeNameVO) treeNode.getUserObject();

			//�ж��Ƿ��Ǻϲ�������������,����Ǻϲ�������������,����Ҫ�ж���ĩ�� �ڵ�
			//ȡ�õ�ǰѡ��ı�����֯��ϵ��Ա,�ж��Ƿ���ĩ��,
			String innercode = selectedData.getInnercode();
			boolean isLeaf =true;//�Ƿ���Ҷ�ڵ�
			Object[] allDatas = treePane.getModel().getAllDatas();
			for (int i = 0; i < allDatas.length; i++) {
				ReportCombineStruMemberWithCodeNameVO memberExvo = (ReportCombineStruMemberWithCodeNameVO) allDatas[i];
				if(memberExvo.getInnercode().startsWith(innercode)&& !memberExvo.getInnercode().equals(innercode)){
					isLeaf =false;
					break;
				}
			}
			if(isLeaf){
				return;
			}
		}
		if (strUnitPK==null)
			return;

		String strSchemePK=hbSchemeRefPane.getHbScheme_refPane().getRefPK();
		if (strSchemePK == null){
			MessageDialog.showHintDlg(getMainboard(),null,
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0054")/* @res "��ѡ��ϲ�����" */);
			return;
		}

		try{
			//�򿪱����������
			controler.setSelectedTaskPK(strSchemePK);
			controler.doOpenRepEditWin(mainBoard,false);

			//�ڴ򿪱��������������У����ܸı��˵�λ����ѡ�еĽڵ㣬�˴�ȷ����λ����ѡ�еĽڵ�Ϊԭ�ڵ�
			controler.setSelectedUnitPK(strUnitPK);
//			setSelectedTask(strSchemePK);
		}catch(Exception te){
			AppDebug.debug(te);
			JOptionPane.showMessageDialog(mainBoard, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0010")/*@res "�򿪱���ʧ��"*/);
		}
	}
	//��ʱΪ�˱���ͨ��modify By liyongru for Debug to run at 20100402
	@Override
	/*protected*/public void onTreeSelectionChange(TreePath path) {
		AbsCombRepDataControler controler=(AbsCombRepDataControler)AbsRepDataControler.getInstance(getMainboard());
		DefaultMutableTreeNode treeNode=(DefaultMutableTreeNode)path.getLastPathComponent();
		ReportCombineStruMemberWithCodeNameVO rmsMemVO=(ReportCombineStruMemberWithCodeNameVO)treeNode.getUserObject();
		//���ѡ�еĵ�λ�����˱仯��ˢ�±�������Χ
		if (!rmsMemVO.getPk_org().equals(controler.getSelectedUnitPK())){
			controler.setSelectedUnitPK(rmsMemVO.getPk_org());
		}
	}

	@Override
	public Object[] loadReportOrgAndRmsName(String strRmsPK, String strOrgPK)
			throws Exception {
		return null;
	}

	@Override
	public void valueChanged(ValueChangedEvent event) {
		try {
			initTreeModelData(null);
			AbsCombRepDataControler controler=(AbsCombRepDataControler)AbsRepDataControler.getInstance(getMainboard());
			String pk_hbscheme = hbSchemeRefPane.getHbScheme_refPane().getRefPK();
			if(pk_hbscheme == null){
				MessageDialog.showHintDlg(getMainboard(),null,
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0054")/* @res "��ѡ��ϲ�����" */);
				return;
			}
			getMainboard().getContext().setAttribute(IUfoContextKey.TASK_PK, pk_hbscheme);//�ϲ�����������Ϊ�գ�Ҳ��Ҫ���»�������
			getMainboard().getContext().setAttribute(IUfoContextKey.HBSCHEME_VO, hbSchemeRefPane.getHbScheme_refPane().getVO());
			controler.setSelectedTaskPK(pk_hbscheme);
			controler.doOpenRepEditWin(getMainboard(), false);
			treePane.getTree().invalidate();
			treePane.getTree().repaint();
		} catch (Exception e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void reInitKeyCondPane(String strRepPK){
		super.reInitKeyCondPane(strRepPK);
		AbsRepDataControler controler = AbsRepDataControler.getInstance(getMainboard());
		if (controler != null) {
			AbsCombRepDataEditor combEditor = (AbsCombRepDataEditor) getMainboard().getView(controler.getEditorID());
			if(combEditor != null && combEditor.getActiveRepDataEditor() instanceof AbsRepDataEditorInComb){
				m_keyCondPane = combEditor.getKeyCondPane();
				if (m_keyCondPane != null && m_keyCondPane instanceof AdjKeyCondPane){
					((AdjKeyCondPane)m_keyCondPane).gettTimeEditorListener().setUnitTreeViewer(this);
					((AdjKeyCondPane)m_keyCondPane).gettTimeEditorListener().valueChanged(new ValueChangedEvent(new Object()));//��ʼ���ؼ������� ʱ��ҲҪ�Ե�λ�ؼ��ֲ���������Ӧ�Ĳ���
				}
			}
		}
	}
	
	@Override
	protected void selectUnitTreeNode(String strUnitPK){
		Object[] objs = treePane.getModel().getAllDatas();
	    if(objs != null && objs.length>0) {
	    	boolean found = false;
	    	for(Object obj : objs) {
	    		ReportCombineStruMemberWithCodeNameVO vo = (ReportCombineStruMemberWithCodeNameVO)obj;
	    		if(vo.getPk_org().equals(strUnitPK)) {
	    			treePane.getModel().setSelectedData(vo);
	    			found = true;
	    			break;
	    		}
	    	}
	    	//add by fengzhy 2013-03-05 Ϊ�˽���л���ͬ�ĺϲ���ϵ��ʱ�����й�궪ʧ
	    	if(!found) {
	    		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treePane.getTree().getModel().getRoot();
	    		@SuppressWarnings("rawtypes")
				Enumeration e = rootNode.children();
	    		Object elem = null;
	    		if(e.hasMoreElements())
	    			elem = e.nextElement();
	    		ReportCombineStruMemberWithCodeNameVO vo = (ReportCombineStruMemberWithCodeNameVO)((DefaultMutableTreeNode)elem).getUserObject();
	    		treePane.getModel().setSelectedData(vo);
	    		setOrgInputValue(vo.getPk_org());
	    		selectedOrgPK = vo.getPk_org(); 
	    	}
	    }
	}

	@Override
	public String getExceptionMessage() {
		if(hbSchemeRefPane == null || hbSchemeRefPane.getHbScheme_refPane().getRefPK() == null){
			return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0054")/* @res "��ѡ��ϲ�����" */;
		}
		return null;
	}
}
