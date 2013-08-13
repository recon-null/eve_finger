// This file is part of Finger for EVE.
//
// Finger for EVE is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Finger for EVE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.

// You should have received a copy of the GNU Affero General Public License
// along with Finger for EVE.  If not, see <http://www.gnu.org/licenses/>.

package net.eve.finger.client;

import net.eve.finger.shared.AllianceInfo;
import net.eve.finger.shared.CharInfo;
import net.eve.finger.shared.CharType;

import com.extjs.gxt.desktop.client.Desktop;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

/**
 * Alliance viewer.
 * Requires 'desktop', 'fingersvc', 'sessionid'
 * to be set in the GXT registry.
 */
public class WinViewAlliance extends Window 
{
	private LayoutContainer layoutTop;
	private HtmlContainer imgLogo;
	private LayoutContainer layoutInfoPanel;
	private HtmlContainer htmlNotes;
	private LayoutContainer layoutBottom;
	private ContentPanel pnlFC;
	private ContentPanel pnlScout;
	private ContentPanel pnlCyno;
	private ContentPanel pnlSuper;
	private ContentPanel pnlTitan;
	private ButtonBar btnbarPanelSwitcher;
	private Button btnFC;
	private Button btnScout;
	private Button btnCyno;
	private Button btnSuper;
	private Button btnTitan;
	
	/**
	 * Holds the alliance info shown by this viewer.
	 */
	private AllianceInfo allianceInfo;
	
	/**
	 * Holds the alliance ID for the alliance
	 * being shown by this viewer.
	 */
	private long allianceID;
	
	/**
	 * The desktop to add windows to.
	 */
	private static Desktop desk;
	
	/**
	 * Access to the server.
	 */
	private static FingerServiceAsync fingerSvc;
	
	/**
	 * Holds the current session ID.
	 */
	private static String sessionID;
	
	/**
	 * Error dialog for character lists.
	 */
	private static Dialog charListError = new Dialog();
	private ToolBar toolBar;
	private Button btnEdit;
	
	static
	{
		// Get the desktop
		desk = (Desktop)Registry.get("desktop");
		
		// Get the RPC service
		fingerSvc = (FingerServiceAsync)Registry.get("fingersvc");
		
		// Get the session ID
		sessionID = (String)Registry.get("sessionid");
		
		// Build the character list error dialog
		charListError.setHeading("Retrevial Error!");
		charListError.addText("Unable to retrieve a list of characters. There may have been a " +
							  "technical problem.");
		charListError.setBodyStyle("fontWeight:bold;padding:13px;");
		charListError.setSize(300, 120);
		charListError.setHideOnButtonClick(true);
		charListError.setButtons(Dialog.OK);	
		charListError.setClosable(false);
		charListError.setModal(true);
	}
	
	/**
	 * Default constructor.
	 * @wbp.parser.constructor
	 */
	public WinViewAlliance() 
	{
		createContents();		
	}
	
	/**
	 * Creates a viewer for the given alliance.
	 * @param allianceInfo The alliance to view.
	 */
	public WinViewAlliance(AllianceInfo allianceInfo)
	{		
		// Build the UI and save fields
		this();
		this.allianceInfo = allianceInfo;
		this.allianceID = allianceInfo.getAllianceID();
		
		// Build the alliance logo URL
		StringBuilder pURL = new StringBuilder();
		pURL.append("<img src=\"");
		pURL.append("http://image.eveonline.com/Alliance/");
		pURL.append(allianceInfo.getAllianceID());
		pURL.append("_128.png\" />");		
		imgLogo.setHtml(pURL.toString());
		
		// Set the header
		this.setHeading(allianceInfo.getAllianceName());
		
		// Load the notes
		String notes = allianceInfo.getNotes();
		
		// Are there notes?
		if (notes != null)
		{
			// Yes
			String noteLines[] = notes.split("\n");	
			StringBuilder noteHTML = new StringBuilder();
			noteHTML.append("<span style='white-space:normal'>");			
			for (String line : noteLines)
			{
				noteHTML.append(SafeHtmlUtils.htmlEscape(line));
				noteHTML.append("<br/>");
			}						
			noteHTML.append("</span>");
			htmlNotes.setHtml(noteHTML.toString());
		}
		else
		{
			// No notes
			htmlNotes.hide();
		}
		
		// Create the grids
		createCharGridFromRPC(pnlFC, btnFC, CharType.FC);
		createCharGridFromRPC(pnlScout, btnScout, CharType.SCOUT);
		createCharGridFromRPC(pnlCyno, btnCyno, CharType.CYNO);
		createCharGridFromRPC(pnlSuper, btnSuper, CharType.SUPER);
		createCharGridFromRPC(pnlTitan, btnTitan, CharType.TITAN);
	}	

	private void createContents()
	{
		setMinWidth(465);
		setMinHeight(400);
		setSize("465px", "400px");
		setMinimizable(true);
		setHeading("Alliance Name");
		VBoxLayout boxLayout = new VBoxLayout();
		boxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		setLayout(boxLayout);
		
		this.toolBar = new ToolBar();
		this.toolBar.setAutoHeight(true);
		
		this.btnEdit = new Button("Edit");
		this.btnEdit.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) 
			{
				executeEdit();
			}
		});
		this.toolBar.add(this.btnEdit);
		add(this.toolBar);
		this.toolBar.setHeight("19");
		
		this.layoutTop = new LayoutContainer();
		this.layoutTop.setAutoHeight(true);
		this.layoutTop.setLayout(new ColumnLayout());
		
		this.imgLogo = new HtmlContainer("<b>Alliance Logo</b>");
		this.layoutTop.add(this.imgLogo);
		this.imgLogo.setWidth(128);
		this.imgLogo.setHeight(128);
		this.imgLogo.setSize("128", "128");
		
		this.layoutInfoPanel = new LayoutContainer();
		this.layoutInfoPanel.setAutoWidth(true);
		this.layoutInfoPanel.setAutoHeight(true);
		this.layoutInfoPanel.setLayout(new RowLayout(Orientation.VERTICAL));
		
		this.htmlNotes = new HtmlContainer("<b>Notes will be displayed here.</b>\r\n");
		this.layoutInfoPanel.add(this.htmlNotes, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(4, 0, 0, 5)));
		this.layoutTop.add(this.layoutInfoPanel);
		add(this.layoutTop);
		this.layoutTop.setBorders(true);
		
		this.btnbarPanelSwitcher = new ButtonBar();
		this.btnbarPanelSwitcher.setAlignment(HorizontalAlignment.CENTER);
		this.btnbarPanelSwitcher.setBorders(true);
		
		final CardLayout bottomCardLayout =  new CardLayout();
		this.layoutBottom = new LayoutContainer();
		this.layoutBottom.setLayout(bottomCardLayout);
		
		this.btnFC = new Button("Fleet Cmdrs");
		this.btnFC.setEnabled(false);
		this.btnFC.setTabIndex(1);
		this.btnFC.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) 
			{
				bottomCardLayout.setActiveItem(pnlFC);				
			}
		});
		this.btnbarPanelSwitcher.add(this.btnFC);
		
		this.btnScout = new Button("Scouts");
		this.btnScout.setEnabled(false);
		this.btnScout.setTabIndex(2);
		this.btnScout.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) 			
			{
				bottomCardLayout.setActiveItem(pnlScout);				
			}
		});
		this.btnbarPanelSwitcher.add(this.btnScout);
		
		this.btnCyno = new Button("Cynos");
		this.btnCyno.setEnabled(false);
		this.btnCyno.setTabIndex(3);
		this.btnCyno.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce)
			{
				bottomCardLayout.setActiveItem(pnlCyno);
			}
		});
		this.btnbarPanelSwitcher.add(this.btnCyno);
		
		this.btnSuper = new Button("Supers");
		this.btnSuper.setEnabled(false);
		this.btnSuper.setTabIndex(4);
		this.btnSuper.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce)
			{
				bottomCardLayout.setActiveItem(pnlSuper);
			}
		});
		this.btnbarPanelSwitcher.add(this.btnSuper);
		
		this.btnTitan = new Button("Titans");
		this.btnTitan.setEnabled(false);
		this.btnTitan.setTabIndex(5);
		this.btnTitan.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce)
			{
				bottomCardLayout.setActiveItem(pnlTitan);
			}
		});
		this.btnbarPanelSwitcher.add(this.btnTitan);
		add(this.btnbarPanelSwitcher);
		
		this.pnlFC = new ContentPanel();
		this.pnlFC.setHeading("Fleet Commanders");
		this.pnlFC.setLayout(new FitLayout());
		this.layoutBottom.add(this.pnlFC);
		
		this.pnlScout = new ContentPanel();
		this.pnlScout.setHeading("Scouts");
		this.pnlScout.setLayout(new FitLayout());
		this.layoutBottom.add(this.pnlScout);
		
		this.pnlCyno = new ContentPanel();
		this.pnlCyno.setHeading("Cynos");
		this.pnlCyno.setLayout(new FitLayout());
		this.layoutBottom.add(this.pnlCyno);
		
		this.pnlSuper = new ContentPanel();
		this.pnlSuper.setHeading("Supers");
		this.pnlSuper.setLayout(new FitLayout());
		this.layoutBottom.add(this.pnlSuper);
		
		this.pnlTitan = new ContentPanel();
		this.pnlTitan.setHeading("Titans");
		this.pnlTitan.setLayout(new FitLayout());
		this.layoutBottom.add(this.pnlTitan);		
		VBoxLayoutData vbld_layoutBottom = new VBoxLayoutData();
		vbld_layoutBottom.setFlex(5.0);
		add(this.layoutBottom, vbld_layoutBottom);
		this.layoutBottom.setBorders(true);
	}
	
	@Override
	protected void initTools()
	{
		// Actually init the tools
		super.initTools();
		
		// Please implement delegates...
		final Window thisAlias = this;
		
		// The close button needs to remove the window from the desktop
		closeBtn.addListener(Events.Select, new Listener<ComponentEvent>() 
				{
			        public void handleEvent(ComponentEvent ce) 
			        {
			        	desk.removeWindow(thisAlias);
			        }
				});	
	}
	
	/**
	 * Opens an editor for the current alliance.
	 */
	private void executeEdit() 
	{
		// TODO Auto-generated method stub		
	}

	/**
	 * Gets the requested character list from the RPC service,
	 * converts it into a {@link ListStore}, and adds a grid
	 * containing the character list to the given {@link ContentPanel}.
	 * Also enables the given {@link Button}.
	 * @param parent The parent to add the grid to.
	 * @param btnToEnable The button to enable once the grid is ready.
	 * @param charType The character type to get.
	 */
	private void createCharGridFromRPC(final ContentPanel parent, final Button btnToEnable, CharType charType)
	{	
		fingerSvc.getAllianceChars(sessionID, allianceID, charType, new AsyncCall<List<CharInfo>>() 
				{				
					@Override
					public void onFailure(Throwable caught) 
					{
						// Let the user know something went wrong
						super.onFailure(caught);
					}
				
					@Override
					public void onSuccess(List<CharInfo> result) 
					{
						// Did we get results?
						if (result != null)
						{
							// Yes, create the grid
							buildGrid(parent, btnToEnable, charInfoToListStore(result));
						}
						else
						{
							// Nope, something went wrong
							charListError.show();							
						}						
					}
			
				}); // RPC call		
		
	} // getCharInfoListFromRPC
	
	/**
	 * Converts the given list of CharInfo to a {@link ListStore}.
	 * @param chars The character list to convert.
	 * @return The character list as a {@link ListStore}.
	 */
	private ListStore<BeanModel> charInfoToListStore(List<CharInfo> chars)
	{		
		ListStore<BeanModel> store = new ListStore<BeanModel>();		
		store.add(BeanModelLookup.get().getFactory(CharInfo.class).createModel(chars));	
		return store;
	}
	
	/**
	 * Creates a character grid and adds it to the given parent.
	 * @param parent The parent to add the grid to.
	 * @param btnToEnable The button to enable once the grid is ready.
	 * @param store The store to bind to.
	 */
	private void buildGrid(ContentPanel parent, Button btnToEnable, ListStore<BeanModel> store)
	{
		// Holds the columns
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>(10);		
				
		// Create columns
		columns.add(new ColumnConfig("charName", "Name", 30));
		columns.add(new ColumnConfig("allianceName", "Alliance", 140));
		columns.add(new ColumnConfig("corpName", "Corp", 140));
		columns.add(new ColumnConfig("timezone", "Timezone", 70));		
		
		// Create the grid
		Grid<BeanModel> grid = new Grid<BeanModel>(store, new ColumnModel(columns));
		
		// Set the auto expand column
		grid.setAutoExpandColumn("charName");
		
		// Create the selection model
		final GridSelectionModel<BeanModel> selModel = new GridSelectionModel<BeanModel>();
		selModel.bindGrid(grid);
		selModel.bind(store);
		selModel.setSelectionMode(SelectionMode.SINGLE);
		grid.setSelectionModel(selModel);
		
		// ************ BEGIN: Build the grid's context menu ************		
		
		// Create the context menu
		Menu contextMenu = new Menu();
		
		// Build the view char item
		final MenuItem viewChar = new MenuItem("View character");
		viewChar.addSelectionListener(new SelectionListener<MenuEvent>()
				{
					@Override
					public void componentSelected(MenuEvent ce)
					{
						// Get the currently selected character
						List<BeanModel> selection = selModel.getSelectedItems();
						
						// There should only be one selected character
						if (selection.size() == 1)
						{
							// Get the bean so we can create a char viewer from it
							CharInfo charInfo = (CharInfo)selection.get(0).getBean();
							String charName = charInfo.getCharName();
							
							// Is this character already open?
							for (Window w : desk.getWindows())
							{
								if (w.getHeading().toLowerCase().equalsIgnoreCase(charName))
								{
									// Already open, so no reason to go any further
									w.show();
									w.toFront();
									return;
								}
							}
							
							// Not already open, so we'll create a viewer
							WinViewChar viewer = new WinViewChar(charInfo);							
							desk.addWindow(viewer);							
							viewer.show();
						
						} // If				
						
					} // componentSelected
			
				}); // Listener
		
		viewChar.setEnabled(false);
		contextMenu.add(viewChar);

		// Build the add contact item
		final MenuItem addContact = new MenuItem("IGB: Add contact");
		addContact.addSelectionListener(new SelectionListener<MenuEvent>()
				{
					@Override
					public void componentSelected(MenuEvent ce)
					{
						// Get the currently selected character
						List<BeanModel> selection = selModel.getSelectedItems();
						
						// There should only be one selected character
						if (selection.size() == 1)
						{
							// Open the add contact window
							Utils.IGB_addContact((Integer)selection.get(0).get("charID"));																
						
						} // If				
						
					} // componentSelected
			
				}); // Listener	
			
		addContact.setEnabled(false);
		contextMenu.add(addContact);
		
		// ************ END: Build the grid's context menu ************
		
		// Set the grid's context menu
		grid.setContextMenu(contextMenu);
		
		// Custom click handling for the grid to change
		// selection behavior and manage the context menu
		grid.addListener(Events.OnMouseDown, new Listener<GridEvent<BeanModel>>() 
				{
					@Override
					public void handleEvent(GridEvent<BeanModel> e) 
					{
						// Get the row (if one exists)						
						BeanModel note = e.getModel();
						
						// Is there a row?
						if (note != null)
						{	
							// Enable the context menu							
							addContact.setEnabled(true);
							viewChar.setEnabled(true);
							
							// Select this row				
							selModel.select(note, false);
						}
						else
						{
							// Disable the context menu	
							addContact.setEnabled(false);
							viewChar.setEnabled(false);
						}
					}			
				});		
		
		// Add the grid and make sure its layout is called
		parent.add(grid);
		parent.layout();
		
		// Enable the button
		btnToEnable.setEnabled(true);
	}
}
