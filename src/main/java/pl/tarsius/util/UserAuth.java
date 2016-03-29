package pl.tarsius.util;

import java.math.BigInteger;

/**
 * Created by Ireneusz Kuliga on 29.03.16.
 */
public class UserAuth {

    public String genSalt() {
        return "Salt";
    }

    public String genHash(String password, String salt) {
        return "Hash";
    }

    public boolean authUser(String password, BigInteger userId) {
        return false;
    }

}
