package org.scoula.common.util;

import java.io.InputStream;
import java.util.Properties;

public class UploadPathName {

    public static String getUploadPath() {
        try (InputStream is =
                     UploadFileName.class.getClassLoader()
                             .getResourceAsStream("secret.properties")) {

            Properties props = new Properties();
            props.load(is);

            return props.getProperty("upload.path");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public  static  String getFeedPath(){
        return getUploadPath() + "/feed/";
    }

    public  static  String getEventPath(){
        return getUploadPath() + "/event/";
    }

    public  static  String getCardPath(){
        return getUploadPath() + "/card/";
    }

    public static String getProfilePath() {
        return getUploadPath() + "/profile/";
    }

    public static String getBankPath() {
        return getUploadPath() + "/bank/";
    }

    public  static  String getCustomCardPath() {return getUploadPath() + "/customCard/";}

}
