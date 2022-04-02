package com.openbravo.pos.sales.restaurant;

import com.openbravo.pos.forms.AppUser;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 * @since Sep 9, 2017, 4:07:37 PM
 */
public class TablePlaceRoleManager {

    private AppUser appUser;

    private boolean editing;

    public TablePlaceRoleManager(final AppUser appUser) {
        this.appUser = appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public boolean isAllowedToEdit() {
        return appUser.hasPermission(TablePlaceManager.class.getCanonicalName());
    }

}
