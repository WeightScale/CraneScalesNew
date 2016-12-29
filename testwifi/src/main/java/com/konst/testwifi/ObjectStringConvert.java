package com.konst.testwifi;

import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;

import java.io.*;

/**
 * @author Kostya on 12.07.2016.
 */
public class ObjectStringConvert {

    public static String objectToString(Serializable obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(new Base64OutputStream(baos, Base64.NO_PADDING | Base64.NO_WRAP));
            oos.writeObject(obj);
            oos.close();
            return baos.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object stringToObject(String str) {
        try {
            return new ObjectInputStream(new Base64InputStream(new ByteArrayInputStream(str.getBytes()), Base64.NO_PADDING | Base64.NO_WRAP)).readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
