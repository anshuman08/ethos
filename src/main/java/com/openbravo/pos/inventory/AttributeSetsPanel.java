//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2017 uniCenta & previous Openbravo POS works
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.inventory;

import com.openbravo.data.loader.Datas;
import com.openbravo.data.model.*;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.panels.JPanelTable2;

/**
 *
 * @author adrianromero
 */
public class AttributeSetsPanel extends JPanelTable2 {

    private EditorRecord editor;

    /** Creates a new instance of JPanelCategories */
    public AttributeSetsPanel() {
    }

    /**
     *
     */
    @Override
    protected void init() {
        DataLogicSales dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");

        row = new Row(
                new Field("ID", Datas.STRING, Formats.STRING),
                new Field(AppLocal.getIntString("label.name"), Datas.STRING, Formats.STRING, true, true, true),
                new Field("SITEGUID", Datas.STRING, Formats.STRING)
        );

        Table table = new Table(
                "attributeset",
                new PrimaryKey("ID"),
                new Column("NAME"),
                new Column("SITEGUID"));

        lpr = row.getListProvider(app.getSession(), table);
        spr = row.getSaveProvider(app.getSession(), table);

        editor = new AttributeSetsEditor(app,dirty);
    }

    /**
     *
     * @return
     */
    @Override
    public EditorRecord getEditor() {
        return editor;
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.AttributeSets");
    }
}
