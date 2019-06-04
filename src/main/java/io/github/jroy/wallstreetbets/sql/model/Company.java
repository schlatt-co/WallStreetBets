package io.github.jroy.wallstreetbets.sql.model;

import io.github.jroy.wallstreetbets.sql.SQLManager;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Company {

  private final SQLManager sqlManager;

  private final Integer id;
  private final String callsign;
  private final UUID ownerUuid;
  private int totalShares;
  private List<Shareholder> shareholders;
  private List<Member> members;

  public boolean addMember(UUID uuid) {
    if (sqlManager.addCompanyMember(callsign, uuid)) {
      members.add(new Member(sqlManager, null, uuid, callsign));
      return true;
    }
    return false;
  }

  public boolean addShareholder(UUID uuid) {
    if (sqlManager.addShareholder(callsign, uuid)) {
      shareholders.add(new Shareholder(sqlManager, null, uuid, callsign));
      return true;
    }
    return false;
  }
}
