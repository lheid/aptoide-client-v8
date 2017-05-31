package cm.aptoide.pt.v8engine.view.account.store;

import cm.aptoide.pt.v8engine.presenter.View;
import rx.Observable;

public interface ManageStoreView extends View {
  Observable<Void> selectStoreImageClick();

  Observable<ManageStoreViewModel> saveDataClick();

  Observable<Void> cancelClick();

  void showLoadImageDialog();
}
