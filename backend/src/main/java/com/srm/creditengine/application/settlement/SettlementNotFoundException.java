package com.srm.creditengine.application.settlement;

import com.srm.creditengine.domain.shared.ResourceNotFoundException;

public class SettlementNotFoundException extends ResourceNotFoundException {

  public SettlementNotFoundException() {
    super("Settlement not found.");
  }
}
