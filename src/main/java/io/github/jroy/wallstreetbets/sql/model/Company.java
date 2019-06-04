package io.github.jroy.wallstreetbets.sql.model;

import io.github.jroy.wallstreetbets.sql.SQLManager;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Company {

  private final SQLManager sqlManager;

  private final int id;
  private final String callsign;
  private final UUID ownerUuid;
  private int activeShares;
  private int totalShares;
  private final Shareholder[] shareholders;
}
