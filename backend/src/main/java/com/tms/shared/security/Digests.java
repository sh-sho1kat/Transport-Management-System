package com.tms.shared.security;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HexFormat;

public final class Digests {
  private Digests() {}

  public static String hash(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
