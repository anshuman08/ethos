/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializableRead;
import java.util.Date;
import org.joda.time.DateTime;

/**
 *
 * @author Elegenze
 */
public class SiteguidInfo implements SerializableRead , IKeyed{
    private String siteguid;
    private int sflag;
    private int lflag;
   


    /** Creates new CategoryInfo
     * @param siteguid
     **/
    public SiteguidInfo(String siteguid,Integer sflag, Integer lflag) {
        this.siteguid = siteguid;
        this.sflag = sflag;
        this.lflag = lflag;
        
        
       
    }

    /**
     *
     * @return
     */
    @Override
    public Object getKey() {
        return siteguid;
    }
    
    @Override
    public void readValues(DataRead dr) throws BasicException{
     siteguid = dr.getString(1);
     sflag = dr.getInt(2);
     lflag = dr.getInt(3);
     
    }

    /**
     *
     * @return
     */
    public String getId() {
        return siteguid;
    }
    @Override
    public String toString() {
        return siteguid;
    }
    


       
}
