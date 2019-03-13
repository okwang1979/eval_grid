package nc.ui.iufo.query.common.area;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import nc.ui.iufo.query.common.IUfoQueryExecutor;
import nc.ui.iufo.query.common.comp.IUfoOrgSelectedStrategyPanel;
import nc.ui.iufo.query.common.event.IUfoQueryCondChangeListener;
import nc.ui.iufo.repdatamng.actions.RepDataQueryExecutor;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.queryarea.component.QsTitleLabel;
import nc.ui.queryarea.util.QueryAreaColor;
import nc.vo.iufo.query.IUfoQueryCondVO;

// @edit by wuyongc at 2013-2-21,上午9:45:06 跟随UAP的脚步，调整UE style

public class IUfoQuickQueryAreaButtonPanel extends UIPanel implements IUfoQueryCondChangeListener {
	private static final long serialVersionUID = 8503659545087645974L;

	private AbstractAction clearAction;
	private AbstractAction queryAction;

	private final IUfoQuickQueryArea quickQueryArea;

	private IUfoOrgSelectedStrategyPanel orgSelectedPanel;
	
	private UIButton queryButton;

//	private OrgRepFilterPanel orgRepPane=null;

	public IUfoQuickQueryAreaButtonPanel(IUfoQuickQueryArea quickQueryArea) {
		this.quickQueryArea = quickQueryArea;
		quickQueryArea.getQueryHolder().getQueryCondChangeHandler().addQueryCondChangeListener(this);

		initUI();
//		QueryAreaColor.setBkgrdDefaultColor(this);

	}

	private AbstractAction getClearAction(){
		if(clearAction == null){
			clearAction = new IUfoQuickQueryAreaClearAction(quickQueryArea);
		}
		return clearAction;
	}
	
	private void initUI() {
		UIPanel leftPanel = new UIPanel( new FlowLayout(FlowLayout.LEFT,15,0));
		leftPanel.setOpaque(false);
		final UILabel titleLabel = new QsTitleLabel((String)getClearAction().getValue(Action.NAME),UILabel.LEFT); 
		titleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		titleLabel.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				getClearAction().actionPerformed(null);
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				((QsTitleLabel)titleLabel).setEntered(true);
				titleLabel.setForeground(QueryAreaColor.QSTITLE_FONT_SELECTED);
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				((QsTitleLabel)titleLabel).setEntered(false);
				titleLabel.setForeground(QueryAreaColor.QSTITLE_FONT_UNSELECTED);
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				
			}
			
		});
		leftPanel.add(titleLabel);
		UIPanel rightPanel = new UIPanel();
		rightPanel.setOpaque(false);
		rightPanel.setLayout(new FlowLayout(FlowLayout.LEFT,43,0));
		rightPanel.add(getQueryButton());
		add(leftPanel,BorderLayout.WEST);
		add(rightPanel,BorderLayout.EAST);
	}

	private UIButton getQueryButton() {
		if (queryButton == null) {
			queryButton = new UIButton();
			queryButton.setAction(getQueryAction());
			queryButton.setOpaque(false);
			queryButton.setToolTipText(NCLangRes.getInstance().getStrByID("_template", "UPP_NewQryTemplate-0078")/*查询*/ + "(Ctrl+Enter)");
			queryButton.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(final KeyEvent e) {
					//为了解决使用快捷键时绕过button，没有获取到焦点的问题。
					if(queryButton != null){
						queryButton.requestFocusInWindow();
					}
					//btn获取焦点是异步事件，所以需要以下操作
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
								IUfoQueryExecutor executor = quickQueryArea.getQueryExecutor();
								if(executor instanceof RepDataQueryExecutor){
									((RepDataQueryExecutor)executor).getDescriptor().setName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0220")/*@res "查询结果"*/);
								}
								quickQueryArea.doQuery();
							}
						}
					});

				}
				
			});
		}
		return queryButton;
	}


	//取得组织节点选择方式面板
	public IUfoOrgSelectedStrategyPanel getOrgSelectedStrategyPanel(){
		if(orgSelectedPanel == null){
			orgSelectedPanel = new IUfoOrgSelectedStrategyPanel();
		}
		return orgSelectedPanel;
	}

	private AbstractAction getClearButton() {
		if (clearAction == null) {

			clearAction = new IUfoQuickQueryAreaClearAction(quickQueryArea);
//			clearButton = new QueryAreaHyperlinkButton(clearAction);
//			clearButton.setFocusable(false);// 不参与焦点遍历
		}
		return clearAction;
	}

	@SuppressWarnings("serial")
	private AbstractAction getQueryAction() {
		if (queryAction == null) {
			queryAction = new AbstractAction(){
				@Override
				public void actionPerformed(ActionEvent e) {
					IUfoQueryExecutor executor = quickQueryArea.getQueryExecutor();
					if(executor instanceof RepDataQueryExecutor){
						((RepDataQueryExecutor)executor).getDescriptor().setName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0220")/*@res "查询结果"*/);
					}
					quickQueryArea.doQuery();
				}
			};
			queryAction.putValue(Action.NAME, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0250")/*@res "查询"*/);
			queryAction.putValue(Action.SHORT_DESCRIPTION, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0250")/*@res "查询"*/);
//			queryButton.setAction(action);
//			queryButton.setToolTipText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0702")/*@res "查询(Ctrl+Enter)"*/);

			getActionMap().put("queryButton", queryAction);;
			getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK),"queryButton");

			queryAction.setEnabled(false);
		}
		return queryAction;
	}

	@Override
	public void onQueryCondChange(IUfoQueryCondVO oldQueryCond,
			IUfoQueryCondVO newQueryCond, Object eventSource) {
		boolean bEnabled=false;
		if (newQueryCond!=null && newQueryCond.getPk_task()!=null)
			bEnabled=true;
		getQueryButton().setEnabled(bEnabled);
		getClearButton().setEnabled(bEnabled);
	}

	@Override
	public void onQueryCondUpdate(IUfoQueryCondVO oldQueryCond,
			IUfoQueryCondVO newQueryCond, Object eventSource) {
	}

	@Override
	public void onQueryCondClear(Object eventSource) {
	}

	@Override
	public String[] onQueryCondSave(IUfoQueryCondVO queryCond) {
		return null;
	}

//	public OrgRepFilterPanel getOrgRepFilterPane(){
//		if (orgRepPane==null){
//			orgRepPane=new OrgRepFilterPanel();
//			orgRepPane.setQueryAction(quickQueryArea.getQueryHolder().getQueryAction());
//			orgRepPane.initUI();
//		}
//		return orgRepPane;
//	}

	private class IUfoQuickQueryAreaClearAction extends AbstractAction {

		/**
		 *
		 */
		private static final long serialVersionUID = -7589972758148542044L;

		private final IUfoQuickQueryArea quickQueryArea;

		public IUfoQuickQueryAreaClearAction(IUfoQuickQueryArea quickQueryArea) {
			this.quickQueryArea = quickQueryArea;
			initAction();
		}

		private void initAction() {
			putValue(Action.NAME, createActionName());
			putValue(Action.SHORT_DESCRIPTION, createActionName());
		}

		private String createActionName() {
			return NCLangRes.getInstance().getStrByID("_Template",
					"UPP_NewQryTemplate-0028")/* 清空值 */;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			quickQueryArea.clearData();
		}
	}
}