package me.aventium.projectbeam.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static void delete(File file) {
        if(file.isDirectory()) {
            for(String childFile : file.list()) {
                delete(new File(file, childFile));
            }
        }
        file.delete();
    }

    public static void copy(File original, File destination) {
        if(!original.exists()) System.out.println("Could not copy file '" + original.getName() + "', does not exist.");
        if(!original.canRead()) System.out.println("Could not copy file '" + original.getName() + "', access denied.");

        if(original.isDirectory()) {
            if(!destination.exists()) {
                if(!destination.mkdirs())
                    System.out.println("Could not create destination directory for copy: " + destination.getAbsolutePath() + ".");
            }

            String[] files = original.list();
            for(String f : files) {
                File orig = new File(original, f);
                File dest = new File(destination, f);
                copy(orig, dest);
            }
        } else {
            FileInputStream in = null;
            FileOutputStream out = null;

            byte[] buffer = new byte[4096];
            int bytesRead;
            try {
                in = new FileInputStream(original);
                out = new FileOutputStream(destination);
                while((bytesRead = in.read(buffer)) >= 0) {
                    out.write(buffer,  0, bytesRead);
                }
            } catch(IOException ex) {
                System.out.println("Unable to copy file " + original.getAbsolutePath() + " to " + destination.getAbsolutePath() + ".");
                ex.printStackTrace();
            } finally {
                try {
                    if(in != null) in.close();
                    if(out != null) out.close();
                } catch(IOException ex) {
                    System.out.println("Error closing file streams");
                    ex.printStackTrace();
                }
            }
        }
    }

}
