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
package com.openbravo.data.loader;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.openbravo.basic.BasicException;

/**
 *
 * @author adrianromero Created on 26 de febrero de 2007, 21:50
 * @param <T>
 *
 */
public abstract class Transaction<T> {

    private static final Logger LOG = Logger.getLogger(Transaction.class.getName());

    private Session s;

    /**
     * Creates a new instance of Transaction
     *
     * @param s
     */
    public Transaction(Session s) {
        this.s = s;
    }

    /**
     *
     * @return @throws BasicException
     */
    public final T execute() throws BasicException {

        if (s.isTransaction()) {
            return transact();
        } else {
            try {
                try {
                    s.begin();
                    T result = transact();
                    s.commit();
                    return result;
                } catch (BasicException e) {
                    LOG.log(Level.SEVERE, "Error executing query", e);
                    s.rollback();
                    throw e;
                }
            } catch (SQLException eSQL) {
                throw new BasicException("Transaction error", eSQL);
            }
        }
    }

    /**
     *
     * @return @throws BasicException
     */
    protected abstract T transact() throws BasicException;

}
