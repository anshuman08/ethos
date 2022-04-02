package com.openbravo.pos.util.autocomplete;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import com.openbravo.pos.ticket.ProductInfoExt;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 * @since Sep 16, 2017, 3:18:24 PM
 */
public class AutoCompleteJComboBox extends JComboBox<String> {

    private static final long serialVersionUID = 4787197936485681498L;

    private final List<ProductInfoExt> productInfos;

    private final JTextComponent tc;

    public AutoCompleteJComboBox() {
        super();
        this.productInfos = new ArrayList<>();
        this.setEditable(true);
        this.tc = (JTextComponent) getEditor().getEditorComponent();
        tc.getDocument().addDocumentListener(new AutoCompleteDocumentListener());
        tc.addFocusListener(new AutoCompleteFocusListener());
        tc.setForeground(new Color(120, 115, 115));
        tc.setPreferredSize(new java.awt.Dimension(750, 750));

        tc.setFont(new java.awt.Font("Arial", 1, 10));
    }

    public void setProductInfoExt(final Collection<ProductInfoExt> productInfo) {
        this.productInfos.addAll(productInfo);
    }

    public ProductInfoExt getSelectedProductInfoExt() {
        final String selectedText = tc.getText();
        return productInfos.stream()
                .filter((pi) -> pi.toString().equalsIgnoreCase(selectedText))
                .findFirst()
                .orElse(null);
    }

    public boolean isEditing() {
        return !isEditable();
    }

    private void searchProduct() {
        final String text = tc.getText().toLowerCase();
        final List<String> selectedProducts = productInfos.stream()
                .filter((pi) -> pi.toString().toLowerCase().contains(text))
                .map(ProductInfoExt::toString)
                .sorted()
                .collect(toList());
        setEditable(false);
        removeAllItems();
        if (selectedProducts.stream().noneMatch((str) -> str.equalsIgnoreCase(text))) {
            addItem(text);
        }

        selectedProducts.forEach(this::addItem);
        setEditable(true);
        setPopupVisible(true);
        tc.requestFocus();
        tc.setCaretPosition(tc.getText().length());
    }

    private final class AutoCompleteDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }

        public void update() {

            SwingUtilities.invokeLater(() -> {
                searchProduct();
            });

        }

    }

    private final class AutoCompleteFocusListener extends FocusAdapter {

        @Override
        public void focusGained(FocusEvent e) {
            if (tc.getText().length() > 0) {
                setPopupVisible(true);
            }
        }

    }

}
