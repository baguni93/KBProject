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
}
