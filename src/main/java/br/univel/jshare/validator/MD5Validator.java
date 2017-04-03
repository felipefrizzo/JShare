package br.univel.jshare.validator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Created by felipefrizzo on 03/04/17.
 */
public class MD5Validator implements GenericValidator {
    @Override
    public String Validation(String validate) {
        Objects.requireNonNull(validate, "Validate cannot be null");

        MessageDigest messageDigest;
        StringBuilder string = new StringBuilder();

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(validate.getBytes());

            byte[] digest = messageDigest.digest();
            for (byte b: digest) {
                string.append(String.format("%02x", b & 0xff));
            }

            return string.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
