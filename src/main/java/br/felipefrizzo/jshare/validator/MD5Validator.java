package br.felipefrizzo.jshare.validator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by felipefrizzo on 03/04/17.
 */
public class MD5Validator {
    public static byte[] createChecksum(String filename) {
        try {
            InputStream input = new FileInputStream(filename);

            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;

            do {
                numRead = input.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            input.close();
            return complete.digest();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMD5Checksum(String filename) {
        byte[] bytes;
        try {
            bytes = createChecksum(filename);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String result = "";

        for (int i = 0; i < bytes.length; i++) {
            result += Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
}
