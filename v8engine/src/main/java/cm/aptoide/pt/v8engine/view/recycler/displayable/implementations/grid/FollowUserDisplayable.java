package cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import cm.aptoide.pt.model.v7.GetFollowers;
import cm.aptoide.pt.model.v7.store.Store;
import cm.aptoide.pt.navigation.NavigationManagerV4;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.V8Engine;
import cm.aptoide.pt.v8engine.fragment.implementations.StoreFragment;
import cm.aptoide.pt.v8engine.fragment.implementations.TimeLineFollowFragment;
import cm.aptoide.pt.v8engine.util.StoreThemeEnum;
import cm.aptoide.pt.v8engine.view.recycler.displayable.DisplayablePojo;

/**
 * Created by trinkes on 16/12/2016.
 */

public class FollowUserDisplayable extends DisplayablePojo<GetFollowers.TimelineUser> {

  private TimeLineFollowFragment.FollowFragmentOpenMode openMode;

  public FollowUserDisplayable() {
  }

  public FollowUserDisplayable(GetFollowers.TimelineUser pojo,
      TimeLineFollowFragment.FollowFragmentOpenMode openMode) {
    super(pojo);
    this.openMode = openMode;
  }

  @Override protected Configs getConfig() {
    return new Configs(1, false);
  }

  @Override public int getViewLayout() {
    return R.layout.timeline_follow_user;
  }

  public String getUserName() {
    return getPojo().getName();
  }

  public String storeName() {
    return getPojo().getStore().getName();
  }

  public String getFollowing() {
    long number;
    if (getPojo().getStats() != null) {
      number = getPojo().getStats().getFollowing();
    } else {
      number = 0;
    }
    return String.valueOf(number);
  }

  public String getFollowers() {
    long number;
    if (getPojo().getStats() != null) {
      number = getPojo().getStats().getFollowers();
    } else {
      number = 0;
    }
    return String.valueOf(number);
  }

  public String getStoreName() {
    return getPojo().getStore().getName();
  }

  public String getStoreAvatar() {
    return getPojo().getStore().getAvatar();
  }

  public String getUserAvatar() {
    return getPojo().getAvatar();
  }

  public boolean hasStoreAndUser() {
    return getPojo().getStore() != null
        && !TextUtils.isEmpty(getPojo().getStore().getName())
        && !TextUtils.isEmpty(getPojo().getName());
  }

  public int getStoreColor() {
    Store store = getPojo().getStore();
    if (store != null && store.getAppearance() != null) {
      return StoreThemeEnum.get(store.getAppearance().getTheme()).getStoreHeaderInt();
    } else {
      return StoreThemeEnum.get(V8Engine.getConfiguration().getDefaultTheme()).getStoreHeaderInt();
    }
  }

  public Drawable getButtonBackgroundStoreThemeColor() {
    Store store = getPojo().getStore();
    StoreThemeEnum storeThemeEnum;
    if (store.getAppearance() != null) {
      storeThemeEnum = StoreThemeEnum.get(store);
    } else {
      storeThemeEnum = StoreThemeEnum.APTOIDE_STORE_THEME_ORANGE;
    }
    return storeThemeEnum.getButtonLayoutDrawable();
  }

  public boolean hasUser() {
    return !TextUtils.isEmpty(getPojo().getName());
  }

  public boolean hasStore() {
    return getPojo().getStore() != null && !TextUtils.isEmpty(getPojo().getStore().getName());
  }

  public void viewClicked(NavigationManagerV4 navigationManager) {
    Store store = getPojo().getStore();
    String theme = getStoreTheme(store);

    if (store != null) {
      navigationManager.navigateTo(V8Engine.getFragmentProvider()
          .newStoreFragment(store.getName(), theme, StoreFragment.OpenType.GetHome));
    } else {
      navigationManager.navigateTo(V8Engine.getFragmentProvider()
          .newStoreFragment(getPojo().getId(), theme, StoreFragment.OpenType.GetHome));
    }
  }

  private String getStoreTheme(Store store) {
    String theme;
    if (store != null && store.getAppearance() != null) {
      theme =
          store.getAppearance().getTheme() == null ? V8Engine.getConfiguration().getDefaultTheme()
              : store.getAppearance().getTheme();
    } else {
      theme = V8Engine.getConfiguration().getDefaultTheme();
    }
    return theme;
  }

  public TimeLineFollowFragment.FollowFragmentOpenMode getOpenMode() {
    return openMode;
  }
}
