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

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.user.client.Element;

/**
 * Customized row editor for CharNote rows.
 * 
 * Features:
 * 		Sets height of editor to 65px.
 * 		Does not submit when the enter key is pressed.
 * 		Double click does not open the editor, so to disable 
 * 		click to edit functionality, set ClicksToEdit to TWO.
 *
 */
public class NoteRowEditor<M extends ModelData> extends com.extjs.gxt.ui.client.widget.grid.RowEditor<M>
{
	/**
	 * Sets the height of the row editor
	 */
	private static final int EDITOR_HEIGHT = 65;
	
	/**
	 * Default constructor.
	 */
	public NoteRowEditor() 
	{
		super();
	}	

	/**
	 * Sets the size of the row editor to EDITOR_HEIGHT.
	 * Code from http://www.sencha.com/forum/showthread.php?130195-Using-RowEditor-resizing-edited-row-height-in-edit-mode
	 */
	@Override
	protected void verifyLayout(boolean force) 
	{
		// initialize can not be reached... but we use instead "btns !=null"
		if (btns != null && (isVisible() || force)) 
		{
			Element row = (Element) grid.getView().getRow(rowIndex);

			setSize(El.fly(row).getWidth(false), renderButtons ? EDITOR_HEIGHT : 0);

			syncSize();

			ColumnModel cm = grid.getColumnModel();
			for (int i = 0, len = cm.getColumnCount(); i < len; i++)
			{
				if (!cm.isHidden(i)) 
				{
					Field<?> f = (Field<?>) getItem(i);
					f.show();
					f.getElement().setAttribute("gxt-dindex", "" + cm.getDataIndex(i));
					MarginData md = (MarginData) ComponentHelper.getLayoutData(f);
					f.setWidth(cm.getColumnWidth(i) - md.getMargins().left - md.getMargins().right);
				} 
				else 
				{
					getItem(i).hide();
				}
			}
			layout(true);
			positionButtons();
		}
	}	

	@Override
	protected void onEnter(ComponentEvent ce)
	{
		// The enter key shouldn't complete edits,
		// because we have a multiline text editor. 
	}
	
	@Override
	protected void onRowDblClick(GridEvent<M> e) 
	{
	    // We don't want the editor to open on a
		// double click
	}	
}
