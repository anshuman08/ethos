package com.openbravo.pos.sales.restaurant;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.sales.restaurant.JTicketsBagRestaurantMap.MyActionListener;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 * @since Sep 9, 2017, 10:19:32 AM
 */
public class TablePlaceManager {

    private static final Logger LOG = Logger.getLogger(TablePlaceManager.class.getName());

    private int xPos;

    private int yPos;

    private int screenX;

    private int screenY;

    private DataLogicSales dataLogicSales;

    private TablePlaceRoleManager placeRoleManager;

    private final JButton btnPlace;

    private final String placeId;

    private Component floorContainer;

    public TablePlaceManager(final JButton btnPlace, final String placeId) {
        this.btnPlace = btnPlace;
        this.btnPlace.addMouseListener(new StartEvent());
        this.btnPlace.addMouseMotionListener(new DragEvent());
        this.placeId = placeId;
    }

    public void setDataLogicSales(DataLogicSales dataLogicSales) {
        this.dataLogicSales = dataLogicSales;
    }

    public void setPlaceRoleManager(TablePlaceRoleManager placeRoleManager) {
        this.placeRoleManager = placeRoleManager;
    }

    public void setFloorContainer(Component floorContainer) {
        this.floorContainer = floorContainer;
    }

    public void persistPosition() {
        final SentenceExec updatePosExec = dataLogicSales.getPlacesUpdate();
        try {
            final int relativeXPos = btnPlace.getX();
            final int relativeYPos = btnPlace.getY();

            updatePosExec.exec(relativeXPos, relativeYPos, placeId);
        } catch (BasicException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private final class StartEvent extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            //enable handle action by default, and only disable it if we do a drag stuff.
            Arrays.stream(btnPlace.getActionListeners())
                    .filter(JTicketsBagRestaurantMap.MyActionListener.class::isInstance)
                    .forEach((listener) -> {
                        final MyActionListener myActionListener = (MyActionListener) listener;
                        myActionListener.setHandleAction(true);
                    });

            if (!placeRoleManager.isEditing()) {
                return;
            }

            screenX = e.getXOnScreen();
            screenY = e.getYOnScreen();

            xPos = btnPlace.getX();
            yPos = btnPlace.getY();

        }

    }

    private final class DragEvent extends MouseMotionAdapter {

        @Override
        public void mouseDragged(MouseEvent e) {
            //Disable all action events for drag events
            Arrays.stream(btnPlace.getActionListeners())
                    .filter(JTicketsBagRestaurantMap.MyActionListener.class::isInstance)
                    .forEach((listener) -> {
                        final MyActionListener myActionListener = (MyActionListener) listener;
                        myActionListener.setHandleAction(false);
                    });

            if (!placeRoleManager.isEditing()) {
                return;
            }

            final int deltaX = e.getXOnScreen() - screenX;
            final int deltaY = e.getYOnScreen() - screenY;

            final int newXPos = xPos + deltaX;
            final int newYPos = yPos + deltaY;

            final Dimension d = floorContainer.getPreferredSize();
            final int maxXPos = (int) (d.getWidth() - btnPlace.getWidth());
            final int maxYPos = (int) (d.getHeight() - btnPlace.getHeight());
            final int avgXPos = min(max(0, newXPos), maxXPos);
            final int avgYPos = min(max(0, newYPos), maxYPos);
            btnPlace.setLocation(avgXPos, avgYPos);
        }

    }

}
