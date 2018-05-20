package com.earware.gummynet.deep.ui;

import java.io.*;

public class JarFileExtractor {

    static public String extractResource(String resourceName) throws Exception {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        File target = null;
        try {
            stream = JarFileExtractor.class.getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if(stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            target = File.createTempFile("JarFileExtractor.temp.", "pmet.rotcartxReliFraJ");
            target.deleteOnExit();
            resStreamOut = new FileOutputStream(target);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            stream.close();
            resStreamOut.close();
        }

        return target.getPath();
    }
 }
