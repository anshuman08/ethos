package com.openbravo.pos.mant;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializableRead;

/**
 *
 * @author marembo (marembo2008@gmail.com)
 * @since Sep 4, 2017, 10:04:12 PM
 */
public class TableArrangement implements SerializableRead, IKeyed, Serializable {

    private static final Logger LOG = Logger.getLogger(TableArrangement.class.getName());

    private static final long serialVersionUID = 1599902928932050978L;

    private String id;

    private String name;

    private int width;

    private int length;

    private BufferedImage image;

    @Override
    public void readValues(final DataRead dr) throws BasicException {
        this.id = dr.getString(1);
        this.name = dr.getString(2);
        this.width = dr.getInt(3);
        this.length = dr.getInt(4);

        final byte[] data = dr.getBytes(5);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        try {
            this.image = ImageIO.read(inputStream);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Object getKey() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

}
