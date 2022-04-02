//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2017 uniCenta
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

package com.openbravo.pos.payment;

public final class MagCardReaderIntelligent implements MagCardReader {
    
    private String m_sHolderName;
    private String m_sCardNumber;
    private String m_sExpirationDate;
    
    private StringBuffer m_sField;
    
    private static final int READING_HOLDER = 0;
    private static final int READING_NUMBER = 1;
    private static final int READING_DATE = 2;
    private static final int READING_FINISHED = 3;
    private int m_iAutomState;
            
    /** Creates a new instance of BasicMagCardReader */
    public MagCardReaderIntelligent() {
        reset();
    }
 
    @Override
    public String getReaderName() {
        return "Basic magnetic card reader";
    }
    
    @Override
    public void reset() {
        m_sHolderName = null;
        m_sCardNumber = null;
        m_sExpirationDate = null;
        m_sField = new StringBuffer();
        m_iAutomState = READING_HOLDER;
    }
    
    @Override
    public void appendChar(char c) {
       
        switch (m_iAutomState) {
            case READING_HOLDER:
            case READING_FINISHED:
        switch (c) {
            case 0x0009:
                m_sHolderName = m_sField.toString();
                m_sField = new StringBuffer();
                m_iAutomState = READING_NUMBER;
                break;
            case 0x000A:
                m_sHolderName = null;
                m_sCardNumber = null;
                m_sExpirationDate = null;
                m_sField = new StringBuffer();
                m_iAutomState = READING_HOLDER;
                break;
            default:
                m_sField.append(c);
                m_iAutomState = READING_HOLDER;
                break;
        }
                break;
            case READING_NUMBER:
        switch (c) {
            case 0x0009:
                m_sCardNumber = m_sField.toString();
                m_sField = new StringBuffer();
                m_iAutomState = READING_DATE;
                break;
            case 0x000A:
                m_sHolderName = null;
                m_sCardNumber = null;
                m_sExpirationDate = null;
                m_sField = new StringBuffer();
                m_iAutomState = READING_HOLDER;
                break;
            default:
                m_sField.append(c);
                break;
        }
                break;                                
            case READING_DATE:
        switch (c) {
            case 0x0009:
                m_sHolderName = m_sCardNumber;
                m_sCardNumber = m_sExpirationDate;
                m_sExpirationDate = null;
                m_sField = new StringBuffer();
                break;
            case 0x000A:
                m_sExpirationDate = m_sField.toString();
                m_sField = new StringBuffer();
                m_iAutomState = READING_FINISHED;
                break;
            default:
                m_sField.append(c);
                break;
        }
                break;    
        }
    }
    
    @Override
    public boolean isComplete() {
        return m_iAutomState == READING_FINISHED;
    }
    
    @Override
    public String getHolderName() {
        return m_sHolderName;
    }
    @Override
    public String getCardNumber() {
        return m_sCardNumber;
    }
    @Override
    public String getExpirationDate() {
        return m_sExpirationDate;
    }
    @Override
    public String getTrack1() {
        return null;
    }
    @Override
    public String getTrack2() {
        return null;
    }    

    /**
     *
     * @return
     */
    @Override
    public String getTrack3() {
        return null;
    }       

    @Override
    public String getEncryptedCardData() {
        return null;
    }

    @Override
    public String getEncryptionKey() {
        return null;
    }
}
