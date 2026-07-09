package com.srm.creditengine.domain.receivable;

import com.srm.creditengine.domain.pricing.UnsupportedReceivableTypeException;

public enum ReceivableType {
  MERCANTILE_DUPLICATE,
  POST_DATED_CHECK;

  public static ReceivableType from(String rawValue) {
    try {
      return valueOf(rawValue);
    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new UnsupportedReceivableTypeException(rawValue);
    }
  }
}
