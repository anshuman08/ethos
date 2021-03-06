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

package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.data.loader.SerializerRead;
import java.awt.image.*;

/**
 *
 * @author  Adrian
 * @version 
 */
public class CategoryInfo implements IKeyed {

    private static final long serialVersionUID = 8612449444103L;
    private String m_sID;
    private String m_sName;
    private String m_sTextTip;
    private BufferedImage m_Image;
    private String m_imgurl;
    private String m_description;
    private Boolean m_bCatShowName;
    private String m_catorder;

    /** Creates new CategoryInfo
     * @param id
     * @param name
     * @param image
     * @param texttip
     * @param catshowname */
    public CategoryInfo(String id, String name, BufferedImage image,String imgurl, String description ,String texttip, Boolean catshowname, String catorder) {
        m_sID = id;
        m_sName = name;
        m_Image = image;
        m_sTextTip = texttip;
        m_bCatShowName = catshowname;
        m_imgurl = imgurl;
        m_description = description;
        m_catorder = catorder;
        
    }

    /**
     *
     * @return
     */
    @Override
    public Object getKey() {
        return m_sID;
    }

    /**
     *
     * @param sID
     */
    public void setID(String sID) {
        m_sID = sID;
    }

    /**
     *
     * @return
     */
    public String getID() {
        return m_sID;
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
     * @param sName
     */
    public void setName(String sName) {
        m_sName = sName;
    }
    
        /**
     *
     * @return
     */
    public String getCatorder() {
        return m_catorder;
    }

    /**
     *
     * @param catorder
     */
    public void setCatorder(String catorder) {
        m_catorder = catorder;
    }
    
       /**
     *
     * @return
     */
    public String getImageurl() {
        return m_imgurl;
    }

    /**
     *
     * @param imgurl
     */
    public void setImageurl(String imgurl) {
        m_imgurl = imgurl;
    }
       /**
     *
     * @return
     */
    public String getDescription() {
        return m_description;
    }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
        m_description = description;
    }
// ADDED JDL 13.04.13 *************

    /**
     *
     * @return
     */
        public String getTextTip() {
        return m_sTextTip;
    }

    /**
     *
     * @param sName
     */
    public void setTextTip(String sName) {
        m_sTextTip = sName;
    }
 
    /**
     *
     * @return
     */
    public Boolean getCatShowName() {
        return m_bCatShowName;
    }

    /**
     *
     * @param bcatshowname
     */
    public void setCatShowName(Boolean bcatshowname) {
        m_bCatShowName = bcatshowname;
    }
    
    
    
    
    
    // *******************************
    
    /**
     *
     * @return
     */
        
    public BufferedImage getImage() {
        return m_Image;
    }

    /**
     *
     * @param img
     */
    public void setImage(BufferedImage img) {
        m_Image = img;
    }

    @Override
    public String toString() {
        return m_sName;
    }

    /**
     *
     * @return
     */
    public static SerializerRead getSerializerRead() {
        return new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
            return new CategoryInfo(dr.getString(1), 
                dr.getString(2), ImageUtils.readImage(dr.getBytes(3)), dr.getString(4), dr.getString(5),
                dr.getString(6),dr.getBoolean(7),dr.getString(8));
        }};
    }
}
