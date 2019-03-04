package cm.aptoide.pt.blacklist;

public class BlacklistUnitMapper {

  public BlacklistManager.BlacklistUnit mapToBlacklistUnit(String blacklistKey) {

    if (blacklistKey.equals("WALLET_ADS_OFFER_51")) {
      return BlacklistManager.BlacklistUnit.WALLET_ADS_OFFER;
    } else {
      return null;
    }
  }
}
