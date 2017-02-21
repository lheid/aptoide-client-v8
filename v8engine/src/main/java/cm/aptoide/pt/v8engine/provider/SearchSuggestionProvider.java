/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/06/2016.
 */

package cm.aptoide.pt.v8engine.provider;

import android.app.SearchManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.logger.Logger;
import cm.aptoide.pt.v8engine.websocket.SearchAppsWebSocket;
import cm.aptoide.pt.v8engine.websocket.WebSocketManager;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Refactored by pedroribeiro in 17/01/2017
 */
public class SearchSuggestionProvider extends SearchRecentSuggestionsProviderWrapper {

  @Override public String getSearchProvider() {
    return "cm.aptoide.pt.v8engine.provider.SearchSuggestionProvider";
  }

  @Override public WebSocketManager getWebSocket() {
    return new SearchAppsWebSocket();
  }
}
