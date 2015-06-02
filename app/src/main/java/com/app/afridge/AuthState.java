package com.app.afridge;

import com.google.gson.Gson;

import com.app.afridge.dom.User;
import com.app.afridge.utils.Log;
import com.app.afridge.utils.SharedPrefStore;

import android.support.annotation.Nullable;


/**
 * User authentication state
 * <p/>
 * Created by drakuwa on 3/5/15.
 */
public class AuthState {

  private final Gson gson;
  private final SharedPrefStore prefStore;
  private User authenticatedUser;

  public static AuthState load(Gson gson, SharedPrefStore store) {

    String userJson = store.getString(SharedPrefStore.Pref.USER);
    User user = null;
    if (userJson != null) {
      user = gson.fromJson(userJson, User.class);
    }
    return new AuthState(gson, store, user);
  }

  private AuthState(Gson gson, SharedPrefStore prefStore,
                    @Nullable
                    User authenticatedUser) {

    this.gson = gson;
    this.prefStore = prefStore;
    this.authenticatedUser = authenticatedUser;
  }

  synchronized public void setUser(User user) {
    // Preconditions.checkNotNull(user);
    if (user != null) {
      if (authenticatedUser != user) {
        prefStore.set(SharedPrefStore.Pref.USER, gson.toJson(user));
      }
      authenticatedUser = user;

      Log.d("Login", user.toString());
    }
  }

  synchronized public void clearUser() {

    if (authenticatedUser != null) {
      prefStore.clear(SharedPrefStore.Pref.USER);
    }
    authenticatedUser = null;
  }

  // @Nullable
  public User getUser() {

    if (null != authenticatedUser) {
      return authenticatedUser;
    }
    else {
      return new User();
    }
  }

  public boolean isAuthenticated() {

    return authenticatedUser != null;
  }
}
