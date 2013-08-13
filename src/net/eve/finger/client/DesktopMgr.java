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

import com.extjs.gxt.desktop.client.Desktop;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;


/**
 * Handles all desktop logic (window management) for the application
 * 
 */
public class DesktopMgr 
{
	/**
	 * Desktop instance
	 */
	private Desktop desktop;
	
	/**
	 * Start menu event listener
	 */
	private SelectionListener<MenuEvent> menuListener;
	
	/**
	 * Shortcut event listener
	 */
	private SelectionListener<ComponentEvent> shortcutListener;

	/**
	 * Gets the desktop.
	 * @return The desktop.
	 */
	public Desktop getDesktop() 
	{
		return desktop;
	}
	
	/**
	 * Default constructor
	 */
	public DesktopMgr()
	{		
		// Listener for taskbar menu items
		menuListener = new SelectionListener<MenuEvent>() 
			{
				@Override
				public void componentSelected(MenuEvent me) 
				{
					itemSelected(me);
				}
		    };

		// Listener for shortcuts
		shortcutListener = new SelectionListener<ComponentEvent>()
		    {
				@Override
				public void componentSelected(ComponentEvent ce)
				{
					itemSelected(ce);
				}
		    };
		
	}
	
	/**
	 * Creates an automatically handled start menu entry to
	 * open/bring to front the given window
	 * @param entryName The name used in the start menu.
	 * @param window The window to use.
	 */
	public void addStartMenuEntry(String entryName, Window window)
	{		
		MenuItem menuItem = new MenuItem(entryName);
		menuItem.setData("window", window);
		menuItem.addSelectionListener(menuListener);
		desktop.getStartMenu().add(menuItem);		
	}
	
	/**
	 * Creates the desktop.
	 * Do not call this method more than once for a single instance.
	 */
	public void createDesktop()
	{
		// Create the desktop
		desktop = new Desktop();
		    
		/** Example code
		Window gridWindow = createGridWindow();
		Window accordionWindow = createAccordionWindow();

		Shortcut s1 = new Shortcut();
		s1.setText("Grid Window");
		s1.setId("grid-win-shortcut");
		s1.setData("window", gridWindow);
		s1.addSelectionListener(shortcutListener);
		desktop.addShortcut(s1);

		Shortcut s2 = new Shortcut();
		s2.setText("Accordion Window");
		s2.setId("acc-win-shortcut");
		s2.setData("window", accordionWindow);
		s2.addSelectionListener(shortcutListener);
		desktop.addShortcut(s2);

		TaskBar taskBar = desktop.getTaskBar();

		StartMenu menu = taskBar.getStartMenu();
		menu.setHeading("Darrell Meyer");
		menu.setIconStyle("user");

		MenuItem menuItem = new MenuItem("Grid Window");
		menuItem.setData("window", gridWindow);
		menuItem.setIcon(IconHelper.createStyle("icon-grid"));
		menuItem.addSelectionListener(menuListener);
		menu.add(menuItem);

		menuItem = new MenuItem("Tab Window");
		menuItem.setIcon(IconHelper.createStyle("tabs"));
		menuItem.addSelectionListener(menuListener);
		menuItem.setData("window", createTabWindow());
		menu.add(menuItem);

		menuItem = new MenuItem("Accordion Window");
		menuItem.setIcon(IconHelper.createStyle("accordion"));
		menuItem.addSelectionListener(menuListener);
		menuItem.setData("window", accordionWindow);
		menu.add(menuItem);

		menuItem = new MenuItem("Bogus Submenu");
		menuItem.setIcon(IconHelper.createStyle("bogus"));

		Menu sub = new Menu();

		for (int i = 0; i < 5; i++) {
		  MenuItem item = new MenuItem("Bogus Window " + (i + 1));
		  item.setData("window", createBogusWindow(i));
		  item.addSelectionListener(menuListener);
		  sub.add(item);
		}

		menuItem.setSubMenu(sub);
		menu.add(menuItem);

		// tools
		MenuItem tool = new MenuItem("Settings");
		tool.setIcon(IconHelper.createStyle("settings"));
		tool.addSelectionListener(new SelectionListener<MenuEvent>() {
		  @Override
		  public void componentSelected(MenuEvent ce) {
		    Info.display("Event", "The 'Settings' tool was clicked");
		  }
		});
		menu.addTool(tool);

		menu.addToolSeperator();

		tool = new MenuItem("Logout");
		tool.setIcon(IconHelper.createStyle("logout"));
		tool.addSelectionListener(new SelectionListener<MenuEvent>() {
		  @Override
		  public void componentSelected(MenuEvent ce) {
		    Info.display("Event", "The 'Logout' tool was clicked");
		  }
		});
		menu.addTool(tool);		
	   **/
	}
	
	/**
	 * Handles window selection events
	 * @param ce The event to handle
	 */
	private void itemSelected(ComponentEvent ce) 
	{		
		Window w;		   

		if (ce instanceof MenuEvent) 
		{
			MenuEvent me = (MenuEvent) ce;
		    w = me.getItem().getData("window");
		} 
		else
		{
		 	w = ce.getComponent().getData("window");
		}
		if (!desktop.getWindows().contains(w)) 
		{
		 	desktop.addWindow(w);
		}
		if (w != null && !w.isVisible()) 
		{
		  	w.show();
		} 
		else
		{
		  	w.toFront();
		}
	}

}
