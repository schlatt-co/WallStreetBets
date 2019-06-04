package io.github.jroy.wallstreetbets.sql.model;

import io.github.jroy.wallstreetbets.sql.SQLManager;
import lombok.Data;

import java.util.UUID;

@Data
public class Member {

  private final SQLManager sqlManager;

  private final int id;
  private final UUID uuid;
  private final String callsign;
}
