package com.openbravo.pos.sales.restaurant;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import javax.swing.*;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.NullIcon;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.mant.TableArrangement;
import com.openbravo.pos.sales.*;
import com.openbravo.pos.sales.mobileorders.DatalogicMobileAppSales;
import com.openbravo.pos.sales.mobileorders.JMobileOrderList;
import com.openbravo.pos.sales.mobileorders.MobileAppDetailInfo;
import com.openbravo.pos.sales.mobileorders.MobileAppInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;

import static java.lang.Boolean.parseBoolean;
import static java.util.stream.Collectors.toMap;

/**
 *
 * @author JG uniCenta
 */
public class JTicketsBagRestaurantMap extends JTicketsBag {

    private static final long serialVersionUID = 7505035830549864484L;

    private static class ServerCurrent {

        public ServerCurrent() {
        }

    }

    private static final ImageIcon EDITING_ICON = new ImageIcon(JTicketsBagRestaurantMap.class.getResource("/com/openbravo/images/edit.png"));

    private static final ImageIcon EDIT_ICON = new ImageIcon(JTicketsBagRestaurantMap.class.getResource("/com/openbravo/images/sale_editline.png"));

    private java.util.List<Place> m_aplaces;

    private java.util.List<Floor> m_afloors;

    private java.util.List<Place> m_emptyplaces;

    private Map<String, TableArrangement> tableArrangements;

    protected TicketInfo m_oTicket;

    private JTicketsBagRestaurant m_restaurantmap;

    private final JTicketsBagRestaurantRes m_jreservations;

    private Place m_PlaceCurrent;

    private ServerCurrent m_ServerCurrent;

    private Place m_PlaceClipboard;

    private CustomerInfo customer;

    private DataLogicReceipts dlReceipts = null;

    private DataLogicSales dlSales = null;

    private DatalogicMobileAppSales dlMobileSales = null;

    private MobileAppInfo mobileOrders;

    private MobileAppDetailInfo mobileOrderDetails;

    private Properties attributes;

    private final RestaurantDBUtils restDB;

    private static final Icon ICO_OCU_SM = new ImageIcon(Place.class.getResource("/com/openbravo/images/edit_group_sm.png"));

    private static final Icon ICO_WAITER = new NullIcon(1, 1);

    private String waiterDetails;

    private String customerDetails;

    private String tableName;

    private Boolean transBtns;

    protected JTicketLines m_ticketlines;

    private TablePlaceRoleManager placeRoleManager;

    public String siteguid;

    public TaxesLogic taxeslogic;

    private DirtyManager dirty;

    private ComboBoxValModel mPlaceModel;

    private javax.swing.JComboBox mPlacedropDown;

    /**
     * Creates new form JTicketsBagRestaurant
     *
     * @param app
     * @param panelticket
     */
    public JTicketsBagRestaurantMap(AppView app, TicketsEditor panelticket) {

        super(app, panelticket);

        restDB = new RestaurantDBUtils(app);
        transBtns = AppConfig.getInstance().getBoolean("table.transbtn");

        dlReceipts = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlMobileSales = (DatalogicMobileAppSales) m_App.getBean("com.openbravo.pos.sales.mobileorders.DatalogicMobileAppSales");

        final AppUserView userView = app.getAppUserView();
        placeRoleManager = new TablePlaceRoleManager(userView.getUser());
        m_restaurantmap = new JTicketsBagRestaurant(app, this);
        m_PlaceCurrent = null;
        m_PlaceClipboard = null;
        customer = null;
        mobileOrders = null;
        try {
            SentenceList sent = new StaticSentence(
                    app.getSession(),
                    "SELECT ID, NAME, IMAGE FROM floors ORDER BY NAME",
                    null,
                    new SerializerReadClass(Floor.class));
            m_afloors = sent.list();

        } catch (BasicException eD) {
            m_afloors = new ArrayList<>();
        }

        try {
            SentenceList sent = new StaticSentence(app.getSession(), "SELECT ID, NAME, WIDTH, LENGTH, IMAGE FROM table_arrangements ORDER BY ID",
                    null,
                    new SerializerReadClass(TableArrangement.class));
            final java.util.List<TableArrangement> tas = sent.list();
            this.tableArrangements = tas.stream().collect(toMap(TableArrangement::getId, Function.identity()));
        } catch (BasicException eD) {
            tableArrangements = new HashMap<>();
        }

        try {
            SentenceList sent = new StaticSentence(app.getSession(), "SELECT ID, NAME, X, Y, FLOOR, CUSTOMER, WAITER, TICKETID, TABLEMOVED, DESIGN FROM places ORDER BY FLOOR",
                    null,
                    new SerializerReadClass(Place.class));
            m_aplaces = sent.list();
            m_aplaces.forEach((aPlace) -> {
                aPlace.setTableArrangement(tableArrangements.get(aPlace.getTableDesign()));
                aPlace.setDataLogicSales(dlSales);
                aPlace.getPlaceManager().setPlaceRoleManager(placeRoleManager);
            });
        } catch (BasicException eD) {
            m_aplaces = new ArrayList<>();
        }
        try {
            //siteguid
            siteguid = dlSales.getSiteGuid().list(1).toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\t");

        } catch (BasicException ex) {
            Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
        }
//        mPlacedropDown.addActionListener(dirty);
        initComponents();

        if (m_afloors.size() > 1) {
            JTabbedPane jTabFloors = new JTabbedPane();
            jTabFloors.applyComponentOrientation(getComponentOrientation());
            jTabFloors.setBorder(new javax.swing.border.EmptyBorder(new Insets(5, 5, 5, 5)));
            jTabFloors.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            jTabFloors.setFocusable(false);
            jTabFloors.setRequestFocusEnabled(false);
            m_jPanelMap.add(jTabFloors, BorderLayout.CENTER);

            m_afloors.stream().map((f) -> {
                f.getContainer().applyComponentOrientation(getComponentOrientation());
                return f;
            }).forEach((f) -> {
                JScrollPane jScrCont = new JScrollPane();
                jScrCont.applyComponentOrientation(getComponentOrientation());
                JPanel jPanCont = new JPanel();
                jPanCont.applyComponentOrientation(getComponentOrientation());

                jTabFloors.addTab(f.getName(), f.getIcon(), jScrCont);
                jScrCont.setViewportView(jPanCont);
                jPanCont.add(f.getContainer());
            });
        } else if (m_afloors.size() == 1) {
            Floor f = m_afloors.get(0);
            f.getContainer().applyComponentOrientation(getComponentOrientation());

            JPanel jPlaces = new JPanel();
            jPlaces.applyComponentOrientation(getComponentOrientation());
            jPlaces.setLayout(new BorderLayout());
            jPlaces.setBorder(new javax.swing.border.CompoundBorder(
                    new javax.swing.border.EmptyBorder(new Insets(5, 5, 5, 5)),
                    new javax.swing.border.TitledBorder(f.getName())));

            JScrollPane jScrCont = new JScrollPane();
            jScrCont.applyComponentOrientation(getComponentOrientation());
            JPanel jPanCont = new JPanel();
            jPanCont.applyComponentOrientation(getComponentOrientation());

            m_jPanelMap.add(jPlaces, BorderLayout.CENTER);
            jPlaces.add(jScrCont, BorderLayout.CENTER);
            jScrCont.setViewportView(jPanCont);
            jPanCont.add(f.getContainer());
        }

        Floor currfloor = null;

        for (Place pl : m_aplaces) {
            int iFloor = 0;

            if (currfloor == null || !currfloor.getID().equals(pl.getFloor())) {
                do {
                    currfloor = m_afloors.get(iFloor++);
                } while (!currfloor.getID().equals(pl.getFloor()));
            }

            final Container floorCnt = currfloor.getContainer();

            addSizeListener(floorCnt);
            floorCnt.add(pl.getButton());
            pl.setButtonBounds();

            if (transBtns) {
                pl.getButton().setOpaque(false);
                pl.getButton().setContentAreaFilled(false);
                pl.getButton().setBorderPainted(false);
            }

            pl.getButton().addActionListener(new MyActionListener(pl));
        }

        m_jreservations = new JTicketsBagRestaurantRes(app, this);
        add(m_jreservations, "res");

        if (m_App.getProperties().getProperty("till.autoRefreshTableMap").equals("true")) {
            webLblautoRefresh.setText(java.util.ResourceBundle.getBundle("pos_messages")
                    .getString("label.autoRefreshTableMapTimerON"));

            javax.swing.Timer autoRefreshTimer = new javax.swing.Timer(Integer.parseInt(m_App.getProperties()
                    .getProperty("till.autoRefreshTimer")) * 1000, new tableMapRefresh());

            autoRefreshTimer.start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                autoRefreshTimer.stop();
            }
        } else {
            webLblautoRefresh.setText(java.util.ResourceBundle.getBundle("pos_messages")
                    .getString("label.autoRefreshTableMapTimerOFF"));
        }

        initPlaceRoleManager();
    }

    class tableMapRefresh implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            loadTickets();
            printState();
            renderFloorTables();
        }

    }

    /**
     *
     */
    @Override
    public void activate() {

        m_PlaceClipboard = null;
        customer = null;
        mobileOrders = new MobileAppInfo();
        loadTickets();
        printState();
        m_oTicket = null;
        m_panelticket.setActiveTicket(null, null);
        m_restaurantmap.activate();
        mPlaceModel = new ComboBoxValModel(m_emptyplaces);
//        mPlacedropDown.setEnabled(false);
//        mPlacedropDown.setModel(mPlaceModel);

        showView("map");
        renderFloorTables();
        initPlaceRoleManager();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {

        if (viewTables()) {
            m_PlaceClipboard = null;
            customer = null;
            mobileOrders = null;

            if (m_PlaceCurrent != null) {

                try {
                    dlReceipts.updateSharedTicket(m_PlaceCurrent.getId(),
                            m_panelticket.getActiveTicket(),
                            m_panelticket.getActiveTicket().getPickupId());
                    dlReceipts.unlockSharedTicket(m_PlaceCurrent.getId(), null);
                } catch (BasicException e) {
                    new MessageInf(e).show(this);
                }

                m_PlaceCurrent = null;
            }

            printState();
            m_panelticket.setActiveTicket(null, null);

            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return
     */
    @Override
    protected JComponent getBagComponent() {
        return m_restaurantmap;
    }

    /**
     *
     * @return
     */
    @Override
    protected JComponent getNullComponent() {
        return this;
    }

    /**
     *
     * @return
     */
    public TicketInfo getActiveTicket() {
        return m_panelticket.getActiveTicket();
    }

    /**
     *
     */
    public void moveTicket() {

        if (m_PlaceCurrent != null) {

            try {
                dlReceipts.updateRSharedTicket(m_PlaceCurrent.getId(),
                        m_panelticket.getActiveTicket(), m_panelticket.getActiveTicket()
                        .getPickupId());
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }

            m_PlaceClipboard = m_PlaceCurrent;

            customer = null;
            m_PlaceCurrent = null;
        }

        printState();
        m_panelticket.setActiveTicket(null, null);
    }

    /**
     *
     * @param c
     * @return
     */
    public boolean viewTables(CustomerInfo c) {

        if (m_jreservations.deactivate()) {
            showView("map");

            m_PlaceClipboard = null;
            customer = c;
            printState();
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public boolean viewTables() {
        return viewTables(null);
    }

    /**
     *
     */
    public void newTicket() {

        if (m_PlaceCurrent != null) {

            try {
                String m_lockState = null;
                m_lockState = dlReceipts.getLockState(m_PlaceCurrent.getId(), m_lockState);
                dlReceipts.getSharedTicket(m_PlaceCurrent.getId());

                if ("override".equals(m_lockState)
                        || "locked".equals(m_lockState)) {
                    dlReceipts.updateSharedTicket(m_PlaceCurrent.getId(),
                            m_panelticket.getActiveTicket(),
                            m_panelticket.getActiveTicket().getPickupId());
                    dlReceipts.unlockSharedTicket(m_PlaceCurrent.getId(), null);
                    m_PlaceCurrent = null;
                } else {
                    JOptionPane.showMessageDialog(null, AppLocal.getIntString("message.sharedticketlockoverriden"),
                            AppLocal.getIntString("title.editor"), JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (BasicException ex) {
                Logger.getLogger(JTicketsBagRestaurantMap.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        printState();
        m_panelticket.setActiveTicket(null, null);
    }

    /**
     *
     * @return
     */
    public String getTable() {
        String id = null;
        if (m_PlaceCurrent != null) {
            id = m_PlaceCurrent.getId();
        }
        return (id);
    }

    /**
     *
     * @return
     */
    public String getTableName() {
        String stableName = null;
        if (m_PlaceCurrent != null) {
            stableName = m_PlaceCurrent.getName();
        }
        return (stableName);
    }

    /**
     *
     */
    @Override
    public void deleteTicket() {

        if (m_PlaceCurrent != null) {

            String id = m_PlaceCurrent.getId();
            try {
                dlReceipts.deleteSharedTicket(id);
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }

            m_PlaceCurrent.setPeople(false);
            m_PlaceCurrent = null;
        }

        printState();
        m_panelticket.setActiveTicket(null, null);
    }

    /**
     *
     */
    public void changeServer() {

        if (m_ServerCurrent != null) {
        }
    }

    /**
     *
     */
    public void loadTickets() {

        Set<String> atickets = new HashSet<>();

        try {
            java.util.List<SharedTicketInfo> l = dlReceipts.getSharedTicketList();
            l.stream().forEach((ticket) -> {
                atickets.add(ticket.getId());
            });
        } catch (BasicException e) {
            new MessageInf(e).show(this);
        }

        m_aplaces.stream().forEach((place) -> {
            place.setPeople(atickets.contains(place.getId()));
            m_afloors
                    .stream()
                    .filter((fl) -> fl.getID().equals(place.getFloor()))
                    .findFirst()
                    .ifPresent((floor) -> {
                        place.renderIcon(floor.getContainer().getPreferredSize());
                    });
        });
    }

    /*
     * Populate the floor plans and tables
     */
    private void printState() {
        final AppProperties properties = m_App.getProperties();
        final String sDB = properties.getProperty("db.engine");
        if (m_PlaceClipboard == null) {
            if (customer == null) {
                m_jText.setText(null);

                m_aplaces.stream().map((place) -> {
                    place.getButton().setEnabled(true);
                    return place;
                }).map((place) -> {
                    if (properties.getProperty("table.tablecolour") == null) {
                        tableName = "<style=font-size:9px;font-weight:bold;><font color = black>"
                                + place.getName() + "</font></style>";
                    } else {
                        tableName = "<style=font-size:9px;font-weight:bold;><font color ="
                                + properties.getProperty("table.tablecolour") + ">"
                                + place.getName() + "</font></style>";
                    }
                    return place;
                }).map((place) -> {
                    if (Boolean.parseBoolean(properties.getProperty("table.showwaiterdetails"))) {
                        if (properties.getProperty("table.waitercolour") == null) {
                            waiterDetails = (restDB.getWaiterNameInTable(place.getName()) == null) ? ""
                                    : "<style=font-size:9px;font-weight:bold;><font color = red>"
                                    + restDB.getWaiterNameInTableById(place.getId()) + "</font></style><br>";
                        } else {
                            waiterDetails = (restDB.getWaiterNameInTable(place.getName()) == null) ? ""
                                    : "<style=font-size:9px;font-weight:bold;><font color ="
                                    + properties.getProperty("table.waitercolour") + ">"
                                    + restDB.getWaiterNameInTableById(place.getId()) + "</font></style><br>";
                        }
                    } else {
                        waiterDetails = "";
                    }
                    return place;
                }).map((place) -> {
                    if (parseBoolean(properties.getProperty("table.showcustomerdetails"))) {
                        final String customername = restDB.getCustomerNameInTable(place.getName());
                        if (m_App.getProperties().getProperty("table.customercolour") == null) {
                            customerDetails = (customername == null) ? ""
                                    : "<style=font-size:9px;font-weight:bold;><font color = blue>"
                                    + restDB.getCustomerNameInTableById(place.getId()) + "</font></style><br>";
                        } else {
                            customerDetails = (customername == null) ? ""
                                    : "<style=font-size:9px;font-weight:bold;><font color ="
                                    + m_App.getProperties().getProperty("table.customercolour") + ">"
                                    + customername + "</font></style><br>";
                        }
                    } else {
                        customerDetails = "";
                    }
                    return place;
                }).map((place) -> {
                    if ((Boolean.parseBoolean(m_App.getProperties().getProperty("table.showwaiterdetails")))
                            || (Boolean.parseBoolean(m_App.getProperties().getProperty("table.showcustomerdetails")))) {
                        place.getButton().setText("<html><center>"
                                + customerDetails + waiterDetails + tableName + "</html>");
                    } else {
                        if (m_App.getProperties().getProperty("table.tablecolour") == null) {
                            tableName = "<style=font-size:10px;font-weight:bold;><font color = black>"
                                    + place.getName() + "</font></style>";
                        } else {
                            tableName = "<style=font-size:10px;font-weight:bold;><font color ="
                                    + m_App.getProperties().getProperty("table.tablecolour") + ">"
                                    + place.getName() + "</font></style>";
                        }

                        place.getButton().setText("<html><center>" + tableName + "</html>");
                    }
                    return place;
                }).forEach((place) -> {
                    m_afloors
                            .stream()
                            .filter((fl) -> fl.getID().equals(place.getFloor()))
                            .findFirst()
                            .ifPresent((floor) -> {
                                place.renderIcon(floor.getContainer().getPreferredSize());
                            });
                });

                m_afloors.forEach((f) -> f.getContainer().repaint());

                m_jbtnReservations.setEnabled(true);
            } else {
                m_jText.setText(AppLocal.getIntString("label.restaurantcustomer", new Object[]{customer.getName()
                }
                ));

                m_aplaces.stream().forEach((place) -> {
                    place.getButton().setEnabled(!place.hasPeople());
                });
                m_jbtnReservations.setEnabled(false);
            }
        } else {
            m_jText.setText(AppLocal.getIntString("label.restaurantmove", new Object[]{m_PlaceClipboard.getName()
            }
            ));

            m_aplaces.stream().forEach((place) -> {
                place.getButton().setEnabled(true);
            });

            m_jbtnReservations.setEnabled(false);
        }

    }

    private TicketInfo getTicketInfo(Place place) {

        try {
            return dlReceipts.getSharedTicket(place.getId());
        } catch (BasicException e) {
            new MessageInf(e).show(JTicketsBagRestaurantMap.this);
            return null;
        }
    }

    private void setActivePlace(Place place, TicketInfo ticket) {
        m_PlaceCurrent = place;
       
        m_panelticket.setActiveTicket(ticket, m_PlaceCurrent.getName());

        try {
            dlReceipts.lockSharedTicket(m_PlaceCurrent.getId(), "locked");
        } catch (BasicException ex) {
            Logger.getLogger(JTicketsBagRestaurantMap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showView(String view) {
        CardLayout cl = (CardLayout) (getLayout());
        cl.show(this, view);
    }

    public void setCurrentOrder(String id) {
        try {
            mobileOrders = dlMobileSales.getOrder(Integer.parseInt(id));
            if (mobileOrders.orderType.equals("delivery")) {

                setDeliveryOrder(id);

            }

        } catch (BasicException ex) {
            Logger.getLogger(JTicketsBagRestaurantMap.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void setDeliveryOrder(String id) {
        try {
            TicketInfo ticket = new TicketInfo();
            
            ProductInfoExt product = new ProductInfoExt();
            
            mobileOrders = dlMobileSales.getOrder(Integer.parseInt(id));
            
            List<MobileAppDetailInfo> ls = dlMobileSales.getOrderDetails(Integer.parseInt(id));
            
            for (int i = 0; i < ls.size(); i++) {

                product = dlSales.getProductInfo(ls.get(i).getProductId());
                TaxInfo tax = null;
                ticket.insertLine(i, new TicketLineInfo(product, ls.get(i).quantity, ls.get(i).price, tax,
                        (java.util.Properties) (product.getProperties().clone()), siteguid));
            }
            ticket.setTicketType(4);
            
            ticket.setUser(m_App.getAppUserView().getUser().getUserInfo());
            
            ticket.setTicketId(Integer.parseInt(id));
            
            printState();
            
            m_emptyplaces = dlSales.getEmptyTables();
            
            JEmptyTableList listTable = JEmptyTableList.newJDialog(this);
            
            Place table = listTable.showTicketsList(m_emptyplaces, dlSales);
            
            if (table != null) {
                dlReceipts.insertSharedTicket(table.getId(), ticket, ticket.getPickupId());
                
                table.setPeople(true);
                
                table.setButtonText(mobileOrders.getOrderType());
                
                setActivePlace(table, ticket);

                mobileOrders.setOrder_status("rinitiated");

                dlMobileSales.setOrderUpdate(mobileOrders);
            }

        } catch (BasicException ex) {
            Logger.getLogger(JTicketsBagRestaurantMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        //To change body of generated methods, choose Tools | Templates.
    }

    public class MyActionListener implements ActionListener {

        private final Place m_place;

        private boolean handleAction;

        public void setHandleAction(boolean handleAction) {
            this.handleAction = handleAction;
        }

        public MyActionListener(Place place) {
            m_place = place;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (!handleAction) {
                return;
            }
            m_App.getAppUserView().getUser();
            if (m_PlaceClipboard == null) {
                TicketInfo ticket = getTicketInfo(m_place);
                if (ticket == null) {
// it's a clear table and a new ticket
                    ticket = new TicketInfo();
                    ticket.setUser(m_App.getAppUserView().getUser().getUserInfo());

                    try {
                        dlReceipts.insertSharedTicket(m_place.getId(), ticket, ticket.getPickupId());
                    } catch (BasicException e) {
                        new MessageInf(e).show(JTicketsBagRestaurantMap.this);
                    }
                    m_place.setPeople(true);
                    setActivePlace(m_place, ticket);

                } else {
// it's a table in use so existing ticket
                    String m_lockState = null;
                    String m_user = m_App.getAppUserView().getUser().getName();

                    try {
                        // Check if it's in use
                        m_lockState = dlReceipts.getLockState(m_place.getId(), m_lockState);

                        if (m_user.equals(m_place.getWaiter())) {                   // Same ticket as current session
                            m_place.setPeople(true);
                            m_PlaceClipboard = null;
                            setActivePlace(m_place, ticket);
                        } else // It's someone else
                        {
                            if (m_App.getAppUserView().getUser().hasPermission("sales.ViewSharedTicket")) {     // Are they allowed to view my ticket?
                                if ("locked".equals(m_lockState) // Is the ticket locked?
                                        || "override".equals(m_lockState)) {
                                    JOptionPane.showMessageDialog(null,
                                            AppLocal.getIntString("message.sharedticketlock")); // Yes 
                                    // Then check if they can Override
                                    if (m_App.getAppUserView().getUser().hasPermission("sales.Override")) {
                                        int res = JOptionPane.showConfirmDialog(null, AppLocal.getIntString(
                                                "message.sharedticketlockoverride"),
                                                AppLocal.getIntString("title.editor"),
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.WARNING_MESSAGE);

                                        if (res == JOptionPane.YES_OPTION) {        // Sure?
                                            m_place.setPeople(true);
                                            m_PlaceClipboard = null;
                                            setActivePlace(m_place, ticket);
                                            dlReceipts.lockSharedTicket(m_PlaceCurrent.getId(), "override");
                                        }
                                    }
                                } else {                                            // Ticket is not locked
                                    m_place.setPeople(true);
                                    m_PlaceClipboard = null;
                                    setActivePlace(m_place, ticket);
                                }
                            } else {                                                // No they're not
                                JOptionPane.showMessageDialog(null,
                                        AppLocal.getIntString("message.sharedticket"));
                            }
                        }
                    } catch (BasicException ex) {
                        Logger.getLogger(JTicketsBagRestaurantMap.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if (m_PlaceClipboard != null) {
                TicketInfo ticketclip = getTicketInfo(m_PlaceClipboard);
                if (ticketclip != null) {
                    if (m_PlaceClipboard == m_place) {
                        Place placeclip = m_PlaceClipboard;
                        m_PlaceClipboard = null;
                        customer = null;
                        printState();
                        setActivePlace(placeclip, ticketclip);
                    }

                    if (m_place.hasPeople()) {
                        TicketInfo ticket = getTicketInfo(m_place);
                        if (ticket != null) {
                            if (JOptionPane.showConfirmDialog(JTicketsBagRestaurantMap.this,
                                    AppLocal.getIntString("message.mergetablequestion"),
                                    AppLocal.getIntString("message.mergetable"),
                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                                try {
//                                dlReceipts.deleteSharedTicket(m_PlaceClipboard.getId());
                                    m_PlaceClipboard.setPeople(false);
                                    if (ticket.getCustomer() == null) {
                                        ticket.setCustomer(ticketclip.getCustomer());
                                    }
                                    ticketclip.getLines().stream().forEach((line) -> {
                                        ticket.addLine(line);
                                    });
                                    dlReceipts.updateRSharedTicket(m_place.getId(),
                                            ticket, ticket.getPickupId());
                                    dlReceipts.deleteSharedTicket(m_PlaceClipboard.getId());

                                } catch (BasicException e) {
                                    new MessageInf(e).show(JTicketsBagRestaurantMap.this);
                                }

                                m_PlaceClipboard = null;
                                customer = null;

                                restDB.clearCustomerNameInTable(restDB.getTableDetails(ticketclip.getId()));
                                restDB.clearWaiterNameInTable(restDB.getTableDetails(ticketclip.getId()));
                                restDB.clearTableMovedFlag(restDB.getTableDetails(ticketclip.getId()));
                                restDB.clearTicketIdInTable(restDB.getTableDetails(ticketclip.getId()));

                                printState();
                                setActivePlace(m_place, ticket);
                            } else {
                                Place placeclip = m_PlaceClipboard;
                                m_PlaceClipboard = null;
                                customer = null;
                                printState();
                                setActivePlace(placeclip, ticketclip);
                            }
                        } else {
                            new MessageInf(MessageInf.SGN_WARNING,
                                    AppLocal.getIntString("message.tableempty"))
                                    .show(JTicketsBagRestaurantMap.this);
                            m_place.setPeople(false);
                        }
                    } else {

                        TicketInfo ticket = getTicketInfo(m_place);

                        if (ticket == null) {
                            try {
                                dlReceipts.insertRSharedTicket(m_place.getId(),
                                        ticketclip, ticketclip.getPickupId());
                                m_place.setPeople(true);
                                dlReceipts.deleteSharedTicket(m_PlaceClipboard.getId());
                                m_PlaceClipboard.setPeople(false);
                            } catch (BasicException e) {
                                new MessageInf(e).show(JTicketsBagRestaurantMap.this);
                            }

                            m_PlaceClipboard = null;
                            customer = null;
                            printState();
                            setActivePlace(m_place, ticketclip);
                        } else {
                            new MessageInf(MessageInf.SGN_WARNING,
                                    AppLocal.getIntString("message.tablefull"))
                                    .show(JTicketsBagRestaurantMap.this);
                            m_PlaceClipboard.setPeople(true);
                            printState();
                        }
                    }
                } else {
                    new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.tableempty")).show(JTicketsBagRestaurantMap.this);
                    m_PlaceClipboard.setPeople(false);
                    m_PlaceClipboard = null;
                    customer = null;
                    printState();
                }
            }
        }

    }

    /**
     *
     * @param btnText
     */
    public void setButtonTextBags(String btnText) {
        m_PlaceClipboard.setButtonText(btnText);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jPanelMap = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        m_jbtnReservations = new javax.swing.JButton();
        m_jbtnRefresh = new javax.swing.JButton();
        m_jText = new javax.swing.JLabel();
        saveTableArrangementsBtn = new javax.swing.JButton();
        editTableArrangementsBtn = new javax.swing.JButton();
        jMobileOrders = new javax.swing.JButton();
        webLblautoRefresh = new com.alee.laf.label.WebLabel();

        setLayout(new java.awt.CardLayout());

        m_jPanelMap.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPanelMap.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        m_jbtnReservations.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jbtnReservations.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/date.png"))); // NOI18N
        m_jbtnReservations.setText(AppLocal.getIntString("button.reservations")); // NOI18N
        m_jbtnReservations.setToolTipText("Open Reservations screen");
        m_jbtnReservations.setFocusPainted(false);
        m_jbtnReservations.setFocusable(false);
        m_jbtnReservations.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnReservations.setMaximumSize(new java.awt.Dimension(133, 40));
        m_jbtnReservations.setMinimumSize(new java.awt.Dimension(133, 40));
        m_jbtnReservations.setPreferredSize(new java.awt.Dimension(133, 45));
        m_jbtnReservations.setRequestFocusEnabled(false);
        m_jbtnReservations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnReservationsActionPerformed(evt);
            }
        });
        jPanel2.add(m_jbtnReservations);

        m_jbtnRefresh.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jbtnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/reload.png"))); // NOI18N
        m_jbtnRefresh.setText(AppLocal.getIntString("button.reloadticket")); // NOI18N
        m_jbtnRefresh.setToolTipText("Reload table information");
        m_jbtnRefresh.setFocusPainted(false);
        m_jbtnRefresh.setFocusable(false);
        m_jbtnRefresh.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnRefresh.setMaximumSize(new java.awt.Dimension(100, 40));
        m_jbtnRefresh.setMinimumSize(new java.awt.Dimension(100, 40));
        m_jbtnRefresh.setPreferredSize(new java.awt.Dimension(100, 45));
        m_jbtnRefresh.setRequestFocusEnabled(false);
        m_jbtnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnRefreshActionPerformed(evt);
            }
        });
        jPanel2.add(m_jbtnRefresh);

        m_jText.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel2.add(m_jText);

        saveTableArrangementsBtn.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        saveTableArrangementsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/filesave.png"))); // NOI18N
        saveTableArrangementsBtn.setText(AppLocal.getIntString("button.saveArrangements"));
        saveTableArrangementsBtn.setToolTipText("Save table arrangements");
        saveTableArrangementsBtn.setFocusPainted(false);
        saveTableArrangementsBtn.setFocusable(false);
        saveTableArrangementsBtn.setMargin(new java.awt.Insets(8, 14, 8, 14));
        saveTableArrangementsBtn.setMaximumSize(new java.awt.Dimension(100, 40));
        saveTableArrangementsBtn.setMinimumSize(new java.awt.Dimension(100, 40));
        saveTableArrangementsBtn.setPreferredSize(new java.awt.Dimension(100, 45));
        saveTableArrangementsBtn.setRequestFocusEnabled(false);
        saveTableArrangementsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveTableArrangementsBtnActionPerformed(evt);
            }
        });
        jPanel2.add(saveTableArrangementsBtn);

        editTableArrangementsBtn.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        editTableArrangementsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_editline.png"))); // NOI18N
        editTableArrangementsBtn.setText(AppLocal.getIntString("button.editArrangements"));
        editTableArrangementsBtn.setToolTipText("Edit Table Arrangements");
        editTableArrangementsBtn.setFocusPainted(false);
        editTableArrangementsBtn.setFocusable(false);
        editTableArrangementsBtn.setMargin(new java.awt.Insets(8, 14, 8, 14));
        editTableArrangementsBtn.setMaximumSize(new java.awt.Dimension(100, 40));
        editTableArrangementsBtn.setMinimumSize(new java.awt.Dimension(100, 40));
        editTableArrangementsBtn.setPreferredSize(new java.awt.Dimension(100, 45));
        editTableArrangementsBtn.setRequestFocusEnabled(false);
        editTableArrangementsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editTableArrangementsBtnActionPerformed(evt);
            }
        });
        jPanel2.add(editTableArrangementsBtn);

        jMobileOrders.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jMobileOrders.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/mobile.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jMobileOrders.setText(bundle.getString("Button.MobileOrders")); // NOI18N
        jMobileOrders.setToolTipText("Get Mobile Orders");
        jMobileOrders.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jMobileOrders.setMaximumSize(new java.awt.Dimension(100, 40));
        jMobileOrders.setMinimumSize(new java.awt.Dimension(100, 40));
        jMobileOrders.setPreferredSize(new java.awt.Dimension(100, 45));
        jMobileOrders.setRequestFocusEnabled(false);
        jMobileOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMobileOrdersActionPerformed(evt);
            }
        });
        jPanel2.add(jMobileOrders);

        jPanel1.add(jPanel2, java.awt.BorderLayout.LINE_START);

        webLblautoRefresh.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        webLblautoRefresh.setText(bundle.getString("label.autoRefreshTableMapTimerON")); // NOI18N
        webLblautoRefresh.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel1.add(webLblautoRefresh, java.awt.BorderLayout.CENTER);

        m_jPanelMap.add(jPanel1, java.awt.BorderLayout.NORTH);

        add(m_jPanelMap, "map");
    }// </editor-fold>//GEN-END:initComponents

    private void m_jbtnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnRefreshActionPerformed

        m_PlaceClipboard = null;
        customer = null;
        loadTickets();
        printState();

        renderFloorTables();

    }//GEN-LAST:event_m_jbtnRefreshActionPerformed

    private void m_jbtnReservationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnReservationsActionPerformed

        showView("res");
        m_jreservations.activate();

    }//GEN-LAST:event_m_jbtnReservationsActionPerformed

    private void saveTableArrangementsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTableArrangementsBtnActionPerformed
        // TODO add your handling code here:
        m_aplaces.forEach((aPlace) -> {
            final TablePlaceManager placeManager = aPlace.getPlaceManager();
            placeManager.persistPosition();
        });
        setEditing(false);
    }//GEN-LAST:event_saveTableArrangementsBtnActionPerformed

    private void editTableArrangementsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editTableArrangementsBtnActionPerformed
        // TODO add your handling code here:
        setEditing(true);
    }//GEN-LAST:event_editTableArrangementsBtnActionPerformed

    private void jMobileOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMobileOrdersActionPerformed
        SwingUtilities.invokeLater(() -> {
            try {
                String appuser = m_App.getAppUserView().getUser().getId();
                List<MobileAppInfo> l = dlMobileSales.getOrders();
                JMobileOrderList listDialog = JMobileOrderList.newJmapDialog(JTicketsBagRestaurantMap.this);
                String id = listDialog.showTicketsList(l, dlMobileSales);
                if (id != null) {
                    setCurrentOrder(id);
                }

            } catch (BasicException e) {
                new MessageInf(e).show(JTicketsBagRestaurantMap.this);
            }
        });
    }//GEN-LAST:event_jMobileOrdersActionPerformed

    private void addSizeListener(final Container container) {
        final ComponentListener componentListener = Arrays.stream(container.getComponentListeners())
                .filter((l) -> ContainerRenderedSizeListener.class.isInstance(l))
                .findAny()
                .orElse(null);
        if (componentListener != null) {
            return;
        }

        container.addComponentListener(new ContainerRenderedSizeListener());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton editTableArrangementsBtn;
    private javax.swing.JButton jMobileOrders;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel m_jPanelMap;
    private javax.swing.JLabel m_jText;
    private javax.swing.JButton m_jbtnRefresh;
    private javax.swing.JButton m_jbtnReservations;
    private javax.swing.JButton saveTableArrangementsBtn;
    private com.alee.laf.label.WebLabel webLblautoRefresh;
    // End of variables declaration//GEN-END:variables

    private final class ContainerRenderedSizeListener extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            //Resize all the buttons.
            renderFloorTables(e.getComponent());
        }

        @Override
        public void componentShown(ComponentEvent e) {
            componentResized(e);
            e.getComponent().repaint();
        }

    }

    private void renderFloorTables() {
        m_afloors.forEach((floor) -> renderFloorTables(floor.getContainer()));
    }

    private void renderFloorTables(Component cnt) {
        final Random r = new Random();
        m_aplaces.forEach((place) -> {
            place.getPlaceManager().setFloorContainer(cnt);
            final JButton btn = place.getButton();
            int xPos = place.getX();
            int yPos = place.getY();
            if (xPos == 0) {
                xPos = cnt.getX() + r.nextInt(cnt.getWidth() / 2);
            }

            if (yPos == 0) {
                yPos = cnt.getY() + r.nextInt(cnt.getHeight() / 2);
            }
            btn.setLocation(xPos, yPos);
            place.renderIcon(cnt.getSize());
        });
    }

    private void setEditing(boolean editing) {
        placeRoleManager.setEditing(editing);
        saveTableArrangementsBtn.setEnabled(editing);
        saveTableArrangementsBtn.repaint();

        final ImageIcon icon = editing ? EDITING_ICON : EDIT_ICON;
        editTableArrangementsBtn.setIcon(icon);
        editTableArrangementsBtn.repaint();
    }

    private void initPlaceRoleManager() {
        final AppUserView userView = m_App.getAppUserView();
        placeRoleManager.setAppUser(userView.getUser());

        saveTableArrangementsBtn.setEnabled(placeRoleManager.isEditing());
        editTableArrangementsBtn.setEnabled(placeRoleManager.isAllowedToEdit());
    }

}
