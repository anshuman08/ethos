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
package com.openbravo.pos.sales.restaurant;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.NullIcon;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.mant.TableArrangement;

/**
 *
 * @author JG uniCenta
 */
public class Place implements SerializableRead, java.io.Serializable {

    private static final long serialVersionUID = 8652254694281L;

    private static final Image IMAGE_OCU;

    private static final Icon ICO_OCU;

    static {
        try {
            IMAGE_OCU = ImageIO.read(Place.class.getResource("/com/openbravo/images/edit_group.png"));
            ICO_OCU = new ImageIcon(IMAGE_OCU);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final Icon ICO_FRE = new NullIcon(22, 22);

    private String m_sId;

    private String m_sName;

    private int m_ix;

    private int m_iy;

    private String m_sfloor;

    private String m_customer;

    private String m_waiter;

    private String m_ticketId;

    private Boolean m_tableMoved;

    private boolean m_bPeople;

    private JButton m_btn;

    private String tableDesign;

    private TableArrangement tableArrangement;

    private TablePlaceManager placeManager;
//    private WebSplitButton m_btn;

    private Dimension floorSize;

    /**
     * Creates a new instance of TablePlace
     */
    public Place() {
    }

    /**
     *
     * @param dr
     * @throws BasicException
     */
    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sId = dr.getString(1);
        m_sName = dr.getString(2);
        m_ix = dr.getInt(3);
        m_iy = dr.getInt(4);
        m_sfloor = dr.getString(5);
        m_customer = dr.getString(6);
        m_waiter = dr.getString(7);
        m_ticketId = dr.getString(8);
        m_tableMoved = dr.getBoolean(9);
        tableDesign = dr.getString(10);

        m_bPeople = false;
        m_btn = new PlaceButton();

        //Add drag support.
        placeManager = new TablePlaceManager(m_btn, m_sId);

        /**
         * JG experimental weblaf final WebSplitButton splitButton = new WebSplitButton(); final WebPopupMenu popupMenu
         * = new WebPopupMenu(); m_btn = new WebSplitButton(); m_btn = splitButton; popupMenu.add(new WebMenuItem
         * ("Server")); popupMenu.add(new WebMenuItem ("Customer")); splitButton.setPopupMenu(popupMenu);
         */
        m_btn.setFocusPainted(false);
        m_btn.setFocusable(false);
        m_btn.setRequestFocusEnabled(false);
        m_btn.setHorizontalTextPosition(SwingConstants.CENTER);
        m_btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        m_btn.setText(m_sName);
        m_btn.setMargin(new Insets(2, 5, 2, 5));

        /**
         * Requested feature - set transparency Configuration option
         *
         * m_btn.setMargin(new Insets(0,0,0,0)); m_btn.setOpaque(false); m_btn.setContentAreaFilled(false);
         * m_btn.setBorderPainted(false);
         *
         * //m_btn.setPreferredSize(new Dimension(d.width + 30,d.height + 8));
         */
    }

    public TablePlaceManager getPlaceManager() {
        return placeManager;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return m_sId;
    }
        /**
     *
     * @return
     */
    public void setId(String sId) {
         this.m_sId = sId;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return m_sName;
    }

    /**
     *
     * @return
     */
    public int getX() {
        return m_ix;
    }

    /**
     *
     * @return
     */
    public int getY() {
        return m_iy;
    }

    /**
     *
     * @return
     */
    public String getFloor() {
        return m_sfloor;
    }

    /**
     *
     * @return
     */
    public JButton getButton() {
        return m_btn;
    }

//    public WebSplitButton getButton() {
//        return m_btn;
//    }
    /**
     *
     * @return
     */
    public String getCustomer() {
        return m_customer;
    }

    /**
     *
     * @return
     */
    public String getWaiter() {
        return m_waiter;
    }

    /**
     *
     * @return
     */
    public boolean hasPeople() {
        return m_bPeople;
    }

    /**
     *
     * @param bValue
     */
    public void setPeople(boolean bValue) {
        m_bPeople = bValue;
        renderIcon(floorSize);
    }

    public void setDataLogicSales(final DataLogicSales dataLogicSales) {
        this.placeManager.setDataLogicSales(dataLogicSales);
    }

    public String getTableDesign() {
        return tableDesign;
    }

    public void setTableArrangement(TableArrangement tableArrangement) {
        this.tableArrangement = tableArrangement;
    }

    public void renderIcon(final Dimension floorSize) {
        this.floorSize = floorSize;

        if (floorSize == null || tableArrangement == null || tableArrangement.getImage() == null) {
            return;
        }

        final int width = (int) (tableArrangement.getWidth() * (floorSize.getWidth() / 100.00));
        final int height = (int) (tableArrangement.getLength() * (floorSize.getHeight() / 100.00));
        final Dimension btnSize = new Dimension(width + 20, height + 40);
        m_btn.setSize(btnSize);
        m_btn.validate();
        m_btn.repaint();
    }

    public TableArrangement getTableArrangement() {
        return tableArrangement;
    }

    /**
     *
     */
    public void setButtonBounds() {
        Dimension d = m_btn.getPreferredSize();
        m_btn.setPreferredSize(new Dimension(d.width + 60, d.height + 30));
        d = m_btn.getPreferredSize();
//        m_btn.setBounds(m_ix - d.width / 2, m_iy - d.height / 2, d.width, d.height);
    }

    /**
     *
     * @param btnText
     */
    public void setButtonText(String btnText) {
        m_btn.setText(btnText);
    }

    private class PlaceButton extends JButton {

        private static final long serialVersionUID = -8365264125033617639L;

        {
            setVerticalAlignment(SwingConstants.BOTTOM);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            final int width = (int) (tableArrangement.getWidth() * (floorSize.getWidth() / 100.00));
            final int height = (int) (tableArrangement.getLength() * (floorSize.getHeight() / 100.00));
            final Image tableImage = tableArrangement.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            final BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            final Graphics tableGraphics = bImage.createGraphics();
            tableGraphics.drawImage(tableImage, 0, 0, null);

            //if there are people, draw the people image onto the table
            if (m_bPeople) {
                final int iW = 10 + (IMAGE_OCU.getWidth(null) / 2);
                final int iH = 15 + (IMAGE_OCU.getHeight(null) / 2);
                final int xPos = (int) (10 + width / 2 - iW);
                final int yPos = (int) (10 + height / 2 - iH);
                tableGraphics.drawImage(IMAGE_OCU, xPos, yPos, null);
            }

            g.drawImage(bImage, 10, 0, this);
        }

    }

}
