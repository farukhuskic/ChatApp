package com.example.chatapp.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RSAUtils {

    private static int bitlength = 1024;

    public static List<BigInteger> generateKeys() {
        List<BigInteger> keys = new ArrayList<>();

        Random r = new Random();
        BigInteger p = BigInteger.probablePrime(bitlength, r);
        BigInteger q = BigInteger.probablePrime(bitlength, r);
        BigInteger N = p.multiply(q);
        keys.add(N);

        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = BigInteger.probablePrime(bitlength / 2, r);
        while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
            e.add(BigInteger.ONE);
        }
        keys.add(e);

        BigInteger d = e.modInverse(phi);  // this-1 mod phi
        keys.add(d);

        return keys;
    }

    private static String bytesToString(byte[] encrypted)
    {
        String stringConvertedFromBytes = "";
        for (byte b : encrypted)
        {
            stringConvertedFromBytes += Byte.toString(b);
        }
        return stringConvertedFromBytes;
    }

    public static byte[] encrypt(byte[] message, BigInteger e, BigInteger N)
    {
        return (new BigInteger(message)).modPow(e, N).toByteArray();
    }

    public static byte[] decrypt(byte[] message, BigInteger d, BigInteger N)
    {
        return (new BigInteger(message)).modPow(d, N).toByteArray();
    }
}
