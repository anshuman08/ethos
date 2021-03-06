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
package com.openbravo.pos.mant;

import javax.swing.ListCellRenderer;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.panels.JPanelTable;

import static com.openbravo.pos.forms.AppLocal.getIntString;

/**
 *
 * @author adrianromero
 */
public class JPanelPlaces extends JPanelTable {

    private static final String[] PLACES_COLS = {"ID", "NAME", "X", "Y", "FLOOR", "DESIGN","SITEGUID"};

    private static final String[] PLACES_COL_NAMES = {"ID",
                                                      getIntString("label.name"),
                                                      "X",
                                                      "Y",
                                                      getIntString("label.placefloor"),
                                                      getIntString("label.tableDesign"),"SITEGUID"};

    private static final Datas[] PLACES_COL_TYPES = {Datas.STRING,
                                                     Datas.STRING,
                                                     Datas.INT,
                                                     Datas.INT,
                                                     Datas.STRING,
                                                     Datas.STRING,
                                                     Datas.STRING};

    private static final Formats[] PLACES_COL_FORMATS = {Formats.STRING,
                                                         Formats.STRING,
                                                         Formats.INT,
                                                         Formats.INT,
                                                         Formats.NULL,
                                                         Formats.STRING,
                                                         Formats.STRING};

    private TableDefinition tplaces;

    private PlacesEditor jeditor;

    /**
     * Creates a new instance of JPanelPlaces
     */
    public JPanelPlaces() {
    }

    /**
     *
     */
    @Override
    protected void init() {
        DataLogicSales dlSales = null;
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");

        tplaces = new TableDefinition(app.getSession(),
                                      "places",
                                      PLACES_COLS,
                                      PLACES_COL_NAMES,
                                      PLACES_COL_TYPES,
                                      PLACES_COL_FORMATS,
                                      new int[]{0}
        );
        jeditor = new PlacesEditor(dlSales, dirty);
    }

    /**
     *
     * @return
     */
    @Override
    public ListProvider getListProvider() {
        return new ListProviderCreator(tplaces);
    }

    /**
     *
     * @return
     */
    @Override
    public SaveProvider getSaveProvider() {
        return new SaveProvider(tplaces);
    }

    /**
     *
     * @return
     */
    @Override
    public Vectorer getVectorer() {
        return tplaces.getVectorerBasic(new int[]{1});
    }

    /**
     *
     * @return
     */
    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tplaces.getRenderStringBasic(new int[]{1}));
    }

    /**
     *
     * @return
     */
    @Override
    public EditorRecord getEditor() {
        return jeditor;
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Tables");
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {
        jeditor.activate(); // primero activo el editor
        super.activate();   // segundo activo el padre
    }

}
