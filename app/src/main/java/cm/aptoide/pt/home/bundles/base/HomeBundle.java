package cm.aptoide.pt.home.bundles.base;

import cm.aptoide.pt.dataprovider.model.v7.Event;
import java.util.List;

/**
 * Created by jdandrade on 13/03/2018.
 */

public interface HomeBundle {

  String getTitle();

  List<?> getContent();

  BundleType getType();

  Event getEvent();

  String getTag();

  enum BundleType {
    EDITORS, APPS, ADS, UNKNOWN, LOADING, INFO_BUNDLE, APPCOINS_ADS, EDITORIAL, SMALL_BANNER, WALLET_ADS_OFFER, TOP, LOAD_MORE_ERROR, FEATURED_BONUS_APPC, NEW_APP;

    public boolean isApp() {
      return this.equals(APPS) || this.equals(EDITORS) || this.equals(ADS) || this.equals(
          APPCOINS_ADS) || this.equals(FEATURED_BONUS_APPC);
    }
  }
}
