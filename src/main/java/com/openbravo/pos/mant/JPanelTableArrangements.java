package com.openbravo.pos.mant;

import javax.swing.ListCellRenderer;

import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.panels.JPanelTable;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 * @since Sep 2, 2017, 1:08:48 PM
 */
public class JPanelTableArrangements extends JPanelTable {

    private static final long serialVersionUID = -6587008548109384518L;

    private static final String DB_TABLE = "table_arrangements";

    private static final String[] DB_TABLE_FIELDS = {"id", "name", "width", "length", "image"};

    private static final Datas[] TYPES = {Datas.STRING, Datas.STRING, Datas.INT, Datas.INT, Datas.IMAGE};

    private static final Formats[] FORMATS = {Formats.STRING, Formats.STRING, Formats.INT, Formats.INT, Formats.NULL};

    private TableArrangementsEditor tableArrangementsEditor;

    private TableDefinition tableDefinition;

    @Override
    protected void init() {
        final DataLogicSales dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        tableDefinition = new TableDefinition(app.getSession(),
                                              DB_TABLE,
                                              DB_TABLE_FIELDS,
                                              DB_TABLE_FIELDS,
                                              TYPES,
                                              FORMATS,
                                              new int[]{0});
        tableArrangementsEditor = new TableArrangementsEditor(dlSales, dirty);
    }

    @Override
    public EditorRecord getEditor() {
        return tableArrangementsEditor;
    }

    @Override
    public ListProvider getListProvider() {
        return new ListProviderCreator(tableDefinition);
    }

    @Override
    public SaveProvider getSaveProvider() {
        return new SaveProvider(tableDefinition);
    }

    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tableDefinition.getRenderStringBasic(new int[]{1}));
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.TableArrangements");
    }

}
