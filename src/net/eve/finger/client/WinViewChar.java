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

import net.eve.finger.shared.CharInfo;
import net.eve.finger.shared.CharNote;
import net.eve.finger.shared.LoginResult;

import com.extjs.gxt.desktop.client.Desktop;
import com.extjs.gxt.ui.client.Style.AutoSizeMode;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record.RecordUpdate;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;


/**
 * Character viewer.
 * Requires 'desktop', 'fingersvc', 'sessionid', 'staticlogindata',
 * 'username' to be set in the GXT registry.
 */
public class WinViewChar extends Window 
{
	
	private LayoutContainer layoutTop;
	private HtmlContainer imgPortrait;
	private LayoutContainer layoutInfoBlock;
	private Text txtFC;
	private Text txtScout;
	private Text txtTitan;
	private Text txtMemberOf;
	private Text txtSuper;
	private Text txtCyno;
	private Text txtAssocWith;
	private ToolBar toolBar;
	private Button btnEdit;
	private Text txtTimezone;	
	private EditorGrid<BeanModel> gridNotes;
	private Button btnAddNote;
	private Text txtAccess;
	private Button btnAddContact;
	
	/**
	 * The row editor for the notes grid.
	 */
	private NoteRowEditor<BeanModel> notesRowEditor = new NoteRowEditor<BeanModel>();
	
	/**
	 * Notes for the current character.
	 */
	private ListStore<BeanModel> charNotesStore = new ListStore<BeanModel>();	
			
	/**
	 * The character info currently being displayed.
	 */
	private CharInfo charInfo;
	
	/**
	 * The current user's name.
	 */
	private static String username;
	
	/**
	 * Drives access selection combo boxes in the grid
	 */
	private static ListStore<BeanModel> accessStore;
	
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
	 * Access to permission data.
	 */
	private static LoginResult staticLoginData;
	
	/**
	 * The current user's ID.
	 */
	private static int userID;
	
	static
	{
		// Get the desktop
		desk = (Desktop)Registry.get("desktop");
		
		// Get the RPC service
		fingerSvc = (FingerServiceAsync)Registry.get("fingersvc");
		
		// Get the username
		username = (String)Registry.get("username");
		
		// Get the session ID
		sessionID = (String)Registry.get("sessionid");
		
		// Get the static login data
		staticLoginData = (LoginResult)Registry.get("staticlogindata");
		
		// Get the user's ID
		userID = staticLoginData.getUserID();
		
		// Convert the access group list into something the combobox can use
		accessStore = Utils.getAccessGroupStoreFiltered(staticLoginData.getAccessGroupList(),
														staticLoginData.getAccessGroupPower());
	}	

	/**
	 * Default constructor.
	 * @wbp.parser.constructor
	 */
	public WinViewChar() 
	{
		// Create the UI
		createContents();		
	}
	
	/**
	 * Inits the character viewer with data for the given character.
	 * @param charInfo The character info to display.
	 * @param fingerSvc The Finger service used to pull char notes.
	 */
	public WinViewChar(CharInfo charInfo)
	{
		// Init the UI and save fields
		this();	
		this.charInfo = charInfo;
		
		// Build the character portrait URL
		StringBuilder pURL = new StringBuilder();
		pURL.append("<img src=\"");
		pURL.append("http://image.eveonline.com/Character/");
		pURL.append(charInfo.getCharID());
		pURL.append("_128.jpg\" />");		
		imgPortrait.setHtml(pURL.toString());
		
		// Set the character name
		this.setHeading(charInfo.getCharName());
		
		// Set corp/alliance info tooltip
		StringBuilder caInfo = new StringBuilder();
		caInfo.append("Member of: &lt;");
		caInfo.append(charInfo.getAllianceName());
		caInfo.append("&gt;, [");
		caInfo.append(charInfo.getCorpName());
		caInfo.append("]");
		txtMemberOf.setToolTip(caInfo.toString());
		
		// Holds the member of string
		StringBuilder memberOf = new StringBuilder();
		
		// Are they in an alliance
		if (charInfo.getAllianceID() != 0)
		{		
			// Yes, show it
			memberOf.append("Member of: &lt;");
			memberOf.append(charInfo.getAllianceName());
			memberOf.append("&gt;");
		}
		else
		{
			// No alliance, so we'll list the corp
			memberOf.append("Member of: [");
			memberOf.append(charInfo.getCorpName());
			memberOf.append("]");
		}
		
		// Set the member of info to be displayed
		txtMemberOf.setText(memberOf.toString());
		
		// Do they have an association?
		if (charInfo.getAssocWithAlliance() != 0)
		{
			// Yes, set it
			StringBuilder asInfo = new StringBuilder();
			asInfo.append("Associated with: &lt;");
			asInfo.append(charInfo.getAssocWithAllianceName());
			asInfo.append("&gt;");
			txtAssocWith.setText(asInfo.toString());
		}
		else
		{
			// No association
			txtAssocWith.hide();
		}

		// Do they have a timezone?
		String timezone = charInfo.getTimezone();
		if (timezone != null)
		{
			txtTimezone.setText("Timezone: " + timezone);
		}
		else
		{
			// No timezone
			txtTimezone.hide();
		}
		
		// Show the access level
		txtAccess.setText("Access: " + (String)accessStore.findModel("id", charInfo.getNeededAccess()).get("name"));
		
		// *** BEGIN Set info bits ***
		
		if (!charInfo.getIsFC())
		{
			txtFC.hide();
		}
		
		if (!charInfo.getIsScout())
		{
			txtScout.hide();
		}
		
		if (!charInfo.getIsCyno())
		{
			txtCyno.hide();
		}
		
		if (!charInfo.getIsSuper())
		{
			txtSuper.hide();
		}
				
		if (!charInfo.getIsTitan())
		{
			txtTitan.hide();
		}		
		
		// *** END Set info bits ***
		
		// Isn't it obvious?
		loadNotes();
	}

	@Override
	protected void onRender(Element parent, int pos) 
	{
		// TODO Auto-generated method stub
		super.onRender(parent, pos);
		
		// Not anymore
		// Make important stuff blink
		// Utils.blinkText(new Text[] {txtFC, txtCyno, txtScout}, "redAlert-blink", 500, MAX_BLINKS);				
	}

	/**
	 * Builds the UI.
	 */
	private void createContents() 
	{
		setMinHeight(341);
		setMinWidth(450);
		setSize("450px", "341px");
		setHeading("CharName");
		setMinimizable(true);
		
		this.toolBar = new ToolBar();
		this.toolBar.setTabIndex(1);
		this.toolBar.setAutoHeight(true);
		
		this.btnEdit = new Button("Edit Character");
		this.btnEdit.addSelectionListener(new SelectionListener<ButtonEvent>() 
				{					
					public void componentSelected(ButtonEvent ce) 
					{
						executeCharEdit();
					}
				});
		VBoxLayout boxLayout = new VBoxLayout();
		boxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		setLayout(boxLayout);
		this.toolBar.add(this.btnEdit);
		
		this.btnAddNote = new Button("Add Note");
		this.btnAddNote.addSelectionListener(new SelectionListener<ButtonEvent>() 
				{
					public void componentSelected(ButtonEvent ce) 
					{
						executeAddNote();
					}
				});
		
		this.btnAddContact = new Button("IGB: Add Contact");
		this.btnAddContact.addSelectionListener(new SelectionListener<ButtonEvent>() 
				{
					public void componentSelected(ButtonEvent ce) 
					{
						Utils.IGB_addContact(((Long)charInfo.getCharID()).intValue());
					}
				});
		
		this.toolBar.add(this.btnAddContact);
		this.toolBar.add(this.btnAddNote);
		add(this.toolBar);
		
		this.layoutTop = new LayoutContainer();
		this.layoutTop.setBorders(true);
		this.layoutTop.setAutoHeight(true);
		this.layoutTop.setLayout(new ColumnLayout());
		
		this.imgPortrait = new HtmlContainer("<b>Character Portrait</b>");
		this.imgPortrait.setWidth(128);
		this.imgPortrait.setHeight(128);
		this.layoutTop.add(this.imgPortrait);
		this.imgPortrait.setSize("128", "128");
		
		this.layoutInfoBlock = new LayoutContainer();
		this.layoutInfoBlock.setAutoWidth(true);
		this.layoutInfoBlock.setAutoHeight(true);		
		this.layoutTop.add(this.layoutInfoBlock, new ColumnData(1.0));
		
		this.txtMemberOf = new Text("Member of: &lt;alliance&gt;, [corp]");
		this.txtMemberOf.setAutoWidth(true);
		this.txtMemberOf.setAutoHeight(true);
		this.txtMemberOf.addStyleName("boldInfo");
		
		this.txtFC = new Text("Fleet Commander");
		this.txtFC.setAutoWidth(true);
		this.txtFC.setAutoHeight(true);
		
		this.txtFC.addStyleName("redAlert");
		this.txtCyno = new Text("Cyno");
		this.txtCyno.setAutoWidth(true);
		this.txtCyno.setAutoHeight(true);
		this.txtCyno.addStyleName("redAlert");
		RowLayout rl_layoutInfoBlock = new RowLayout(Orientation.VERTICAL);
		this.layoutInfoBlock.setLayout(rl_layoutInfoBlock);
		
		this.txtScout = new Text("Scout");
		this.txtScout.setAutoWidth(true);
		this.txtScout.setAutoHeight(true);
		this.txtScout.addStyleName("redAlert");	
		
		this.txtTitan = new Text("Titan");
		this.txtTitan.setAutoWidth(true);
		this.txtTitan.setAutoHeight(true);
		this.txtTitan.addStyleName("boldInfo");
		
		this.layoutInfoBlock.add(this.txtMemberOf, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(4, 0, 2, 5)));
		
		this.txtAssocWith = new Text("Associated with: &lt;alliance&gt;, [corp]");
		this.txtAssocWith.setAutoHeight(true);
		this.txtAssocWith.setAutoWidth(true);
		this.txtAssocWith.addStyleName("boldInfo");
		this.layoutInfoBlock.add(this.txtAssocWith, new RowData(Style.DEFAULT, 20.0, new Margins(2, 0, 2, 5)));
		this.txtAssocWith.setSize("239", "23");
		
		this.txtTimezone = new Text("Timezone: [timezone]");
		this.txtTimezone.setAutoWidth(true);
		this.txtTimezone.setAutoHeight(true);
		txtTimezone.addStyleName("boldInfo");
		this.layoutInfoBlock.add(this.txtTimezone, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(2, 0, 2, 5)));
		this.layoutInfoBlock.add(this.txtFC, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(2, 0, 2, 5)));
		this.layoutInfoBlock.add(this.txtScout, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(2, 0, 2, 5)));
		this.layoutInfoBlock.add(this.txtCyno, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(2, 0, 2, 5)));
		this.layoutInfoBlock.add(this.txtTitan, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(2, 0, 2, 5)));
		
		this.txtSuper = new Text("Super");
		this.txtSuper.setAutoWidth(true);
		this.txtSuper.setAutoHeight(true);
		this.txtSuper.addStyleName("boldInfo");
		this.layoutInfoBlock.add(this.txtSuper, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(2, 0, 2, 5)));
		
		this.txtAccess = new Text("[Access Grp Name]");
		this.txtAccess.addStyleName("boldInfo");
		this.layoutInfoBlock.add(this.txtAccess, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(2, 0, 4, 5)));	
		add(this.layoutTop);
		this.layoutTop.setHeight("150");
		
		this.gridNotes = createNotesGrid();		
		VBoxLayoutData vbld_gridNotes = new VBoxLayoutData();
		vbld_gridNotes.setFlex(5.0);
		add(this.gridNotes, vbld_gridNotes);
		this.gridNotes.setBorders(true);		
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
	 * Loads notes from the server.
	 */
	private void loadNotes() 
	{		
		// Load notes
		fingerSvc.getCharNotes(sessionID, charInfo.getCharID(), new AsyncCall<List<CharNote>>()
				{
					@Override
					public void onSuccess(List<CharNote> result) 
					{
						// Are there notes to load?
						if (result != null)
						{
							if (result.size() > 0)
							{
								// Yes, load them
								charNotesStore.add(BeanModelLookup.get().getFactory(CharNote.class).createModel(result));									
							}
							
							// Bind the grid to the DB via RPC
							createNotesStoreEvents();
						}
					}
				});		
	}
	
	/**
	 * Connects the notes store to the RPC service, mirroring all changes
	 * to the server.
	 */
	private void createNotesStoreEvents() 
	{
		// Connect the notes store to the RPC service
		charNotesStore.addStoreListener(new StoreListener<BeanModel>()
				{				
					@Override
					public void storeRemove(StoreEvent<BeanModel> se) 
					{
						// If the super class does anything with this,
						// let it do so.
						super.storeRemove(se);
						
						// Get the note
						CharNote note = (CharNote)se.getModel().getBean();
						
						// Is this a note that needs to be removed
						// from the database?
						if (note.getId() != -1)
						{						
							// Yes, delete the note from the DB
							fingerSvc.deleteCharNote(sessionID, note, new AsyncCall<Boolean>() 
									{
										@Override
										public void onFailure(Throwable caught) 
										{
											// Display an error
											super.onFailure(caught);
											
											// This note didn't really get deleted
											charNotesStore.rejectChanges();
										}
										
										@Override
										public void onSuccess(Boolean result) 
										{
											// Was the deletion successful?
											if (result)
											{
												// Yes
												charNotesStore.commitChanges();											
											}
											else
											{
												// No, something failed gracefully on the server
												charNotesStore.rejectChanges();
											}
										}			
										
									}); // RPC call
						}
						else
						{
							// Not in the DB, so we'll just remove it locally
							charNotesStore.commitChanges();
						}
						
					} // Store remove method
					
					@Override
					public void storeUpdate(final StoreEvent<BeanModel> se) 
					{
						// If the super class does anything with this,
						// let it do so.
						super.storeUpdate(se);
						
						// We call commit to update the UI, not to handle RPC calls, so
						// we'll ignore those types of updates
						if (se.getOperation() != RecordUpdate.COMMIT)
						{	
							// Get the note
							CharNote note = (CharNote)se.getModel().getBean();
							
							// Update the note
							fingerSvc.updateCharNote(sessionID, note, new AsyncCall<Integer>()
									{									
										@Override
										public void onFailure(Throwable caught) 
										{
											// Show an error message
											super.onFailure(caught);
											
											// Throw away the changes since they
											// didn't go through
											charNotesStore.rejectChanges();
										}
										
										@Override
										public void onSuccess(Integer result) 
										{
											// Was the update successful?
											if (result > 0)
											{
												// Yes, set the note's ID
												se.getModel().set("id", result);
												charNotesStore.commitChanges();
											}
											else
											{
												// No, something failed gracefully on the server
												charNotesStore.rejectChanges();
											}
										}
								
									}); // RPC call
							
						} // Store update method
						
					} // Commit check
					
				});	// Store listener
	
	} // createStoreEvents

	
	/**
	 * Creates the note editor grid.
	 * @return The note editor grid.
	 */
	private EditorGrid<BeanModel> createNotesGrid()
	{
		// Holds the columns
		List<ColumnConfig> columns = new ArrayList<ColumnConfig>(3);
		
		// ************ BEGIN: Build the notes column ************
		
		// Create the column's editor and container
		TextArea noteEditor = new TextArea();
		
		ColumnConfig noteConfig = new ColumnConfig("notes", "Notes", 100);
		CellEditor noteCellEditor = new CellEditor(noteEditor);
		noteEditor.setHeight(50);
		noteCellEditor.setAutoSizeMode(AutoSizeMode.WIDTH);
		noteConfig.setEditor(noteCellEditor);
		
		// We'd like this column to have wordwrap
		noteConfig.setRenderer(new GridCellRenderer<BeanModel>()
				{
					@Override
					public Object render(BeanModel model, String property,
										 com.extjs.gxt.ui.client.widget.grid.ColumnData config,
										 int rowIndex, int colIndex,
										 ListStore<BeanModel> store, Grid<BeanModel> grid) 
					{
						StringBuilder sb = new StringBuilder();
						sb.append("<span style='white-space:normal'>");
						String noteLines[] = SafeHtmlUtils.htmlEscape((String)model.get("notes")).split("\n");
						for (String line : noteLines)
						{
							sb.append(line);
							sb.append("<br/>");
						}						
						sb.append("</span>");
						return sb.toString();
					}			
				});
	
		// Add the notes column
		columns.add(noteConfig);
		
		// ************ END: Build the notes column ************
		
		
		// Add the added by column
		columns.add(new ColumnConfig("addedByName", "Added By", 70));
		
		
		// ************ BEGIN: Build the access column ************
		
		// Create the column container
		ColumnConfig accessConfig = new ColumnConfig("neededAccess", "Access", 70);
		
		// Since access groups are integers, we need to render something more
		// meaningful, such as the access group's name.
		accessConfig.setRenderer(new GridCellRenderer<BeanModel>()
				{
					@Override
					public Object render(BeanModel model, String property,
										 com.extjs.gxt.ui.client.widget.grid.ColumnData config,
										 int rowIndex, int colIndex,
										 ListStore<BeanModel> store, Grid<BeanModel> grid) 
					{
						StringBuilder sb = new StringBuilder();
						sb.append("<span>");
						sb.append((String)accessStore.findModel("id", model.get("neededAccess")).get("name"));
						sb.append("</span>");
						return sb.toString();
					}			
				});
		
		// Create an editor for the access column
		ComboBox<BeanModel> accessEditor = new ComboBox<BeanModel>();
		accessEditor.setStore(accessStore);
		accessEditor.setDisplayField("name");
		accessEditor.setTriggerAction(TriggerAction.ALL);
		accessEditor.setEditable(false);		
		accessEditor.setForceSelection(true);
		
		// We'll need some custom handling to transport values
		// between the combo box and the grid/store
		CellEditor accessEditorConfig = new CellEditor(accessEditor) 
			{
				  /**
				   * From testing, it appears that this function
				   * is supposed to return the model that should be
				   * set in the editor when the user enters edit mode.
				   * @param value Seems to be the value from the grid's model
				   * 			  that the editor should be set to represent.
				   */
				  @Override
			      public Object preProcessValue(Object value) 
				  {					 
					 if (value == null) 
					 {  
						 return value;  
					 }  
					 return accessStore.findModel("id", value); 
			      }  
			  
				  /**
				   * From testing, it appears that this function
				   * is supposed to return the value that should be
				   * put in the grid/model when the edit is complete.
				   * @param value Seems to be the ModelData selected in
				   * 			  the editor.
				   */
			      @Override  
			      public Object postProcessValue(Object value) 
			      {			    	  
			    	  if (value == null) 
			    	  {  
			    		  return value;  
			    	  } 
			    	  return ((ModelData)value).get("id");  
			      }				
			};		
		accessConfig.setEditor(accessEditorConfig);	
		
		
		// Add the access column
		columns.add(accessConfig);
		
		// ************ END: Build the access column ************
		
		// Create the grid
		final EditorGrid<BeanModel> notesGrid = new EditorGrid<BeanModel>(charNotesStore, new ColumnModel(columns));

		// This actually disables click to edit behavior for
		// the row editor
		notesRowEditor.setClicksToEdit(ClicksToEdit.TWO);
		
		// Enable the row editor plugin
		notesGrid.addPlugin(notesRowEditor);		
		
		// ************ BEGIN: Build the grid's context menu ************
		
		// Create the menu
		Menu notesCtxMenu = new Menu();
		
		// Build the edit item
		final MenuItem editItem = new MenuItem("Edit");
		editItem.addSelectionListener(new SelectionListener<MenuEvent>()
				{
					@Override
					public void componentSelected(MenuEvent ce)
					{
						// Delete the currently selected note
						executeEditNote();
					}			
				});
		editItem.setEnabled(false);
		notesCtxMenu.add(editItem);
		
		// Build the delete item
		final MenuItem deleteItem = new MenuItem("Delete");
		deleteItem.addSelectionListener(new SelectionListener<MenuEvent>()
				{
					@Override
					public void componentSelected(MenuEvent ce)
					{
						// Delete the currently selected note
						executeDeleteNote();
					}
			
				});
		deleteItem.setEnabled(false);
		notesCtxMenu.add(deleteItem);		
		
		// ************ END: Build the grid's context menu ************
		
		// Set the grid's context menu
		notesGrid.setContextMenu(notesCtxMenu);
		
		// Setup the selection model		
		final GridSelectionModel<BeanModel> selModel = new GridSelectionModel<BeanModel>();
		selModel.bindGrid(notesGrid);
		selModel.bind(charNotesStore);
		selModel.setSelectionMode(SelectionMode.SINGLE);		
		notesGrid.setSelectionModel(selModel);		
		

		// Custom click handling for the grid to change
		// selection behavior and manage the context menu
		notesGrid.addListener(Events.OnMouseDown, new Listener<GridEvent<BeanModel>>() 
				{
					@Override
					public void handleEvent(GridEvent<BeanModel> e) 
					{
						// Get the row (if one exists)						
						BeanModel note = e.getModel();
						
						// Is there a row?
						if (note != null)
						{						
							// Set if the user can update this row					
							editItem.setEnabled((Integer)note.get("addedBy") == userID);				
							
							// Allow the user to delete the row
							deleteItem.setEnabled(true);
							
							// Select this row				
							selModel.select(note, false);
						}
						else
						{
							// No selection, don't allow the user to invoke
							// actions on the grid context menu
							editItem.setEnabled(false);
							deleteItem.setEnabled(false);
						}
					}			
				});
		
		// Suppress click to edit functionality
		notesGrid.addListener(Events.BeforeEdit, new Listener<GridEvent<BeanModel>>()
				{
					@Override
					public void handleEvent(GridEvent<BeanModel> e) 
					{
						// TODO Auto-generated method stub
						e.setCancelled(true);						
					}			
				});
		
		// Set simple properties		
		notesGrid.setAutoExpandColumn("notes");		
		notesGrid.setTabIndex(2);		
		notesGrid.setStripeRows(true);		
		
		// Return the grid
		return notesGrid;
	}
	
	/**
	 * Opens this character in an edit window
	 */
	private void executeCharEdit() 
	{
		WinEditChar editor = new WinEditChar(this, charInfo);
		desk.addWindow(editor);
		editor.show();				
	}	
	
	/**
	 * Brings up the row editor for the selected note.
	 */
	private void executeEditNote() 
	{		
		// Get the currently selected notes
		List<BeanModel> selection = gridNotes.getSelectionModel().getSelectedItems();
		
		// Is there a real selection?
		if (selection.size() == 1)
		{
			// Make sure we're not editing anything
			notesRowEditor.stopEditing(false);	
			
			// Begin editing the note
			notesRowEditor.startEditing(charNotesStore.indexOf(selection.get(0)), true);
		}		
	}

	/**
	 * Deletes the selected note.
	 */
	private void executeDeleteNote() 
	{
		// Get the currently selected notes
		List<BeanModel> selection = gridNotes.getSelectionModel().getSelectedItems();
		
		// Is there a real selection?
		if (selection.size() == 1)
		{		
			// Delete the note
			charNotesStore.remove(selection.get(0));						
		}
	}

	/**
	 * Adds a note
	 */
	private void executeAddNote() 
	{
		// Create a new note
		CharNote note = new CharNote();
		note.setAddedBy(staticLoginData.getUserID());
		note.setNeededAccess(staticLoginData.getAccessGroup());
		note.setCharID(charInfo.getCharID());
		note.setAddedByName(username);
		note.setNotes("Enter the note here...");	
		
		// Make sure we're not editing anything
		notesRowEditor.stopEditing(false);
		
		// Convert the note to a BeanModel and add the note to the store
		BeanModel noteModel = BeanModelLookup.get().getFactory(CharNote.class).createModel(note);
		charNotesStore.insert(noteModel, 0);
  
		// Begin editing the note
		notesRowEditor.startEditing(charNotesStore.indexOf(noteModel), true); 		
	}
}
