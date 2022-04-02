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

import java.util.Objects;

import com.openbravo.data.loader.IKeyed;

/**
 *
 * @author adrianromero
 */
public class AttributeSetInfo implements IKeyed {

    private final String id;

    private final String name;

    /**
     *
     * @param id
     * @param name
     */
    public AttributeSetInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getKey() {
        return id;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.id);
        hash = 47 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AttributeSetInfo other = (AttributeSetInfo) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }

        if (!Objects.equals(this.name, other.name)) {
            return false;
        }

        return true;
    }

}
