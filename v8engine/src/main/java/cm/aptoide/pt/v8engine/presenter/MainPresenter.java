/*
 * Copyright (c) 2017.
 * Modified by Marcelo Benites on 18/01/2017.
 */

package cm.aptoide.pt.v8engine.presenter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import cm.aptoide.pt.dataprovider.ws.v7.store.StoreContext;
import cm.aptoide.pt.preferences.managed.ManagerPreferences;
import cm.aptoide.pt.preferences.secure.SecurePreferences;
import cm.aptoide.pt.v8engine.AutoUpdate;
import cm.aptoide.pt.v8engine.Install;
import cm.aptoide.pt.v8engine.InstallManager;
import cm.aptoide.pt.v8engine.V8Engine;
import cm.aptoide.pt.v8engine.crashreports.CrashReport;
import cm.aptoide.pt.v8engine.install.InstallCompletedNotifier;
import cm.aptoide.pt.v8engine.install.installer.RootInstallationRetryHandler;
import cm.aptoide.pt.v8engine.notification.ContentPuller;
import cm.aptoide.pt.v8engine.notification.NotificationSyncScheduler;
import cm.aptoide.pt.v8engine.util.ApkFy;
import cm.aptoide.pt.v8engine.view.DeepLinkManager;
import cm.aptoide.pt.v8engine.view.navigator.FragmentNavigator;
import cm.aptoide.pt.v8engine.view.store.home.HomeFragment;
import cm.aptoide.pt.v8engine.view.wizard.WizardFragment;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by marcelobenites on 18/01/17.
 */
public class MainPresenter implements Presenter {

  private static final String TAG = MainPresenter.class.getSimpleName();
  private final MainView view;
  private final ContentPuller contentPuller;
  private final InstallManager installManager;
  private final RootInstallationRetryHandler rootInstallationRetryHandler;
  private final CrashReport crashReport;
  private final SharedPreferences sharedPreferences;
  private final SharedPreferences securePreferences;
  private final FragmentNavigator fragmentNavigator;
  private final DeepLinkManager deepLinkManager;
  private NotificationSyncScheduler notificationSyncScheduler;
  private InstallCompletedNotifier installCompletedNotifier;
  private ApkFy apkFy;
  private AutoUpdate autoUpdate;
  private boolean firstCreated;

  public MainPresenter(MainView view, InstallManager installManager,
      RootInstallationRetryHandler rootInstallationRetryHandler, CrashReport crashReport,
      ApkFy apkFy, AutoUpdate autoUpdate, ContentPuller contentPuller,
      NotificationSyncScheduler notificationSyncScheduler,
      InstallCompletedNotifier installCompletedNotifier, SharedPreferences sharedPreferences,
      SharedPreferences securePreferences, FragmentNavigator fragmentNavigator,
      DeepLinkManager deepLinkManager) {
    this.view = view;
    this.installManager = installManager;
    this.rootInstallationRetryHandler = rootInstallationRetryHandler;
    this.crashReport = crashReport;
    this.apkFy = apkFy;
    this.autoUpdate = autoUpdate;
    this.contentPuller = contentPuller;
    this.notificationSyncScheduler = notificationSyncScheduler;
    this.installCompletedNotifier = installCompletedNotifier;
    this.fragmentNavigator = fragmentNavigator;
    this.deepLinkManager = deepLinkManager;
    this.firstCreated = true;
    this.sharedPreferences = sharedPreferences;
    this.securePreferences = securePreferences;
  }

  @Override public void present() {
    view.getLifecycle()
        .filter(event -> View.LifecycleEvent.CREATE.equals(event))
        .doOnNext(created -> apkFy.run())
        .filter(created -> firstCreated)
        .doOnNext(created -> notificationSyncScheduler.forceSync())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .doOnNext(__ -> contentPuller.start())
        .doOnNext(__ -> navigate())
        .subscribe(__ -> {
        }, throwable -> crashReport.log(throwable));
    setupInstallErrorsDisplay();
  }

  @Override public void saveState(Bundle state) {
  }

  @Override public void restoreState(Bundle state) {
    firstCreated = false;
  }

  private void setupInstallErrorsDisplay() {
    view.getLifecycle()
        .filter(event -> View.LifecycleEvent.RESUME.equals(event))
        .flatMap(lifecycleEvent -> rootInstallationRetryHandler.retries()
            .compose(view.bindUntilEvent(View.LifecycleEvent.PAUSE)))
        .distinctUntilChanged(installationProgresses -> installationProgresses.size())
        .filter(installationProgresses -> installationProgresses.size() > 0)
        .observeOn(AndroidSchedulers.mainThread())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(installationProgresses -> {
          watchInstalls(installationProgresses);
          view.showInstallationError(installationProgresses.size());
        }, throwable -> crashReport.log(throwable));

    view.getLifecycle()
        .filter(lifecycleEvent -> View.LifecycleEvent.RESUME.equals(lifecycleEvent))
        .flatMap(lifecycleEvent -> installManager.getTimedOutInstallations())
        .filter(installationProgresses -> installationProgresses.size() == 0)
        .observeOn(AndroidSchedulers.mainThread())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(noInstallErrors -> view.dismissInstallationError(),
            throwable -> crashReport.log(throwable));

    installCompletedNotifier.getWatcher()
        .subscribe(allInstallsCompleted -> view.showInstallationSuccessMessage());

    view.getLifecycle()
        .filter(lifecycleEvent -> View.LifecycleEvent.RESUME.equals(lifecycleEvent))
        .flatMap(lifecycleEvent -> view.getInstallErrorsDismiss())
        .flatMapCompletable(click -> installManager.cleanTimedOutInstalls())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(timeoutErrorsCleaned -> {
        }, throwable -> crashReport.log(throwable));
  }

  // FIXME we are showing home by default when we should decide were to go here and provide
  // proper up/back navigation to home if needed
  private void navigate() {
    showHome();
    if (ManagerPreferences.isCheckAutoUpdateEnable(sharedPreferences)
        && !V8Engine.isAutoUpdateWasCalled()) {
      // only call auto update when the app was not on the background
      autoUpdate.execute();
    }
    if (deepLinkManager.showDeepLink(view.getIntentAfterCreate())) {
      SecurePreferences.setWizardAvailable(false, securePreferences);
    } else {
      if (SecurePreferences.isWizardAvailable(securePreferences)) {
        showWizard();
        SecurePreferences.setWizardAvailable(false, securePreferences);
      }
    }
  }

  private void showWizard() {
    fragmentNavigator.navigateTo(WizardFragment.newInstance());
  }

  private void showHome() {
    Fragment home = HomeFragment.newInstance(V8Engine.getConfiguration()
        .getDefaultStore(), StoreContext.home, V8Engine.getConfiguration()
        .getDefaultTheme());
    fragmentNavigator.navigateToWithoutBackSave(home);
  }

  private void watchInstalls(List<Install> installs) {
    for (Install install : installs) {
      installCompletedNotifier.add(install.getPackageName(), install.getVersionCode(),
          install.getMd5());
    }
  }
}