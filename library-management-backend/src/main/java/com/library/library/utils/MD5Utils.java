package com.library.library.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Utils {
    
    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    public static String encrypt(String password) {
        return md5(password);
    }

    public static boolean verify(String inputPassword, String storedPassword) {
        return md5(inputPassword).equals(storedPassword);
    }
}
