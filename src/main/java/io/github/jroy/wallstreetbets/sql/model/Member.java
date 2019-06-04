package io.github.jroy.wallstreetbets.sql.model;

import io.github.jroy.wallstreetbets.sql.SQLManager;
import lombok.Data;

import java.sql.SQLException;
import java.util.UUID;

@Data
public class Member {

  private final SQLManager sqlManager;

  private final Integer id;
  private final UUID uuid;
  private final String callsign;

  public void remove() throws SQLException {
    sqlManager.removeMember(callsign, uuid);
  }
}
