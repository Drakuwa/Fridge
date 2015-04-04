package com.app.afridge.ui.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.graphics.Palette;
import android.transitions.everywhere.ChangeBounds;
import android.transitions.everywhere.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.dom.RandomStats;
import com.app.afridge.dom.User;
import com.app.afridge.interfaces.OnFragmentInteractionListener;
import com.app.afridge.utils.CircleTransform;
import com.app.afridge.utils.Common;
import com.app.afridge.utils.Constants;
import com.app.afridge.views.AdvancedTextView;
import com.github.gorbin.asne.core.SocialNetwork;
import com.github.gorbin.asne.core.SocialNetworkManager;
import com.github.gorbin.asne.core.listener.OnLoginCompleteListener;
import com.github.gorbin.asne.core.listener.OnRequestSocialPersonCompleteListener;
import com.github.gorbin.asne.core.persons.SocialPerson;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;
import com.github.gorbin.asne.googleplus.GooglePlusSocialNetwork;
import com.github.gorbin.asne.twitter.TwitterSocialNetwork;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * User login/profile fragment
 * <p/>
 * Created by drakuwa on 3/5/15.
 */
public class ProfileFragment extends DialogFragment implements SocialNetworkManager.OnInitializationCompleteListener,
        OnLoginCompleteListener, OnRequestSocialPersonCompleteListener {

  // add ButterKnife injects
  @InjectView(R.id.image_profile)
  ImageView imageProfile;
  @InjectView(R.id.text_username)
  AdvancedTextView textUsername;
  @InjectView(R.id.holder_profile)
  LinearLayout holderProfile;
  @InjectView(R.id.text_login_description)
  AdvancedTextView textLoginDescription;
  @InjectView(R.id.button_facebook)
  AdvancedTextView buttonFacebook;
  @InjectView(R.id.button_twitter)
  AdvancedTextView buttonTwitter;
  @InjectView(R.id.button_gplus)
  AdvancedTextView buttonGplus;
  @InjectView(R.id.button_logout)
  AdvancedTextView buttonLogout;
  @InjectView(R.id.text_item_count)
  AdvancedTextView textItemCount;
  @InjectView(R.id.text_notes_count)
  AdvancedTextView textNotesCount;
  @InjectView(R.id.text_random_stat)
  AdvancedTextView textRandomStat;

  /**
   * SocialNetwork Ids in ASNE:
   * 1 - Twitter
   * 2 - LinkedIn
   * 3 - Google Plus
   * 4 - Facebook
   * 5 - Vkontakte
   * 6 - Odnoklassniki
   * 7 - Instagram
   */
  public static final int TWITTER = 1;
  public static final int GPLUS = 3;
  public static final int FACEBOOK = 4;

  private FridgeApplication application;
  private SocialNetworkManager socialNetworkManager;
  private MaterialDialog progressDialog;

  View containerView;
  OnFragmentInteractionListener mListener;

  // Singleton
  private static volatile ProfileFragment instance = null;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment ProfileFragment.
   */
  public static ProfileFragment getInstance() {

    if (instance == null) {
      synchronized (ProfileFragment.class) {
        if (instance == null) {
          instance = new ProfileFragment();
        }
      }
    }
    return instance;
  }

  public ProfileFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setRetainInstance(true); // try to fix orientation change
    setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme); // 0 is the default theme for the selected style
    RandomStats.with(getActivity()).generateList(true); // generate random stats
  }

  private void initSocialNetworkManager() {
    //Get Keys for initiate SocialNetworks
    String TWITTER_CONSUMER_KEY = getActivity().getString(R.string.twitter_consumer_key);
    String TWITTER_CONSUMER_SECRET = getActivity().getString(R.string.twitter_consumer_secret);

    //Chose permissions
    ArrayList<String> fbScope = new ArrayList<>();
    fbScope.addAll(Collections.singletonList("public_profile, email, user_friends"));

    //Use manager to manage SocialNetworks
    socialNetworkManager = (SocialNetworkManager) getFragmentManager().findFragmentByTag(Constants.SOCIAL_NETWORK_TAG);

    //Check if manager exist
    if (socialNetworkManager == null) {
      socialNetworkManager = new SocialNetworkManager();

      //Init and add to manager FacebookSocialNetwork
      FacebookSocialNetwork fbNetwork = new FacebookSocialNetwork(this, fbScope);
      socialNetworkManager.addSocialNetwork(fbNetwork);

      //Init and add to manager TwitterSocialNetwork
      TwitterSocialNetwork twNetwork = new TwitterSocialNetwork(this, TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET,
              application.getString(R.string.fridge_rocks));
      socialNetworkManager.addSocialNetwork(twNetwork);

      //Init and add to manager GooglePlusSocialNetwork
      GooglePlusSocialNetwork gPlusNetwork = new GooglePlusSocialNetwork(this);
      socialNetworkManager.addSocialNetwork(gPlusNetwork);

      //Initiate every network from mSocialNetworkManager
      getFragmentManager().beginTransaction().add(socialNetworkManager, Constants.SOCIAL_NETWORK_TAG).commit();
      socialNetworkManager.setOnInitializationCompleteListener(this);
    }
    else {
      //if manager exist - get and setup login only for initialized SocialNetworks
      if (!socialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
        List<SocialNetwork> socialNetworks = socialNetworkManager.getInitializedSocialNetworks();
        for (SocialNetwork socialNetwork : socialNetworks) {
          socialNetwork.setOnLoginCompleteListener(this);
        }
      }
    }

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    containerView = inflater.inflate(R.layout.fragment_profile, container, false);

    // inject and return the view
    ButterKnife.inject(this, containerView);
    return containerView;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    super.onViewCreated(view, savedInstanceState);

    // initialize the social network manager
    initSocialNetworkManager();

    progressDialog = new MaterialDialog.Builder(getActivity())
            .title(R.string.signing_in)
            .content(R.string.please_wait)
            .progress(true, 0).build();

    if (application.authState.isAuthenticated()) {
      // set the user name
      textUsername.setText(application.authState.getUser().getFullName());

      // set the profile image
      setProfileImage(application.authState.getUser().getImageUrl(), R.mipmap.ic_launcher);

      // set profile stats
      setProfileStats();
    }
    else {
      textUsername.setText(application.getString(R.string.menu_login).toUpperCase(Locale.ENGLISH));
    }

    // refresh the view state
    refreshViewState();
  }

  @Override
  public void onDestroyView() {

    super.onDestroyView();
    ButterKnife.reset(this);
  }

  @Override
  public void onAttach(Activity activity) {

    super.onAttach(activity);
    application = ((FridgeApplication) activity.getApplication());
    try {
      mListener = (OnFragmentInteractionListener) activity;
    }
    catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
              + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {

    super.onDetach();
    mListener = null;
  }

  @Override
  public void onSocialNetworkManagerInitialized() {

    if (!socialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
      List<SocialNetwork> socialNetworks = socialNetworkManager.getInitializedSocialNetworks();
      for (SocialNetwork socialNetwork : socialNetworks) {
        socialNetwork.setOnLoginCompleteListener(this);
      }
    }
  }

  private void refreshViewState() {
    TransitionManager.beginDelayedTransition((ViewGroup) containerView, new ChangeBounds());
    if (application.authState.isAuthenticated()) {
      textLoginDescription.setVisibility(View.GONE);
      buttonFacebook.setVisibility(View.GONE);
      buttonTwitter.setVisibility(View.GONE);
      buttonGplus.setVisibility(View.GONE);
      buttonLogout.setVisibility(View.VISIBLE);
      textItemCount.setVisibility(View.VISIBLE);
      textNotesCount.setVisibility(View.VISIBLE);
      textRandomStat.setVisibility(View.VISIBLE);
    }
    else {
      // set the app profile image
      setProfileImage(null, R.mipmap.ic_launcher);
      textUsername.setText(application.getString(R.string.menu_login).toUpperCase(Locale.ENGLISH));
      textLoginDescription.setVisibility(View.VISIBLE);
      buttonFacebook.setVisibility(View.VISIBLE);
      buttonTwitter.setVisibility(View.VISIBLE);
      buttonGplus.setVisibility(View.VISIBLE);
      buttonLogout.setVisibility(View.GONE);
      textItemCount.setVisibility(View.GONE);
      textNotesCount.setVisibility(View.GONE);
      textRandomStat.setVisibility(View.GONE);
    }
  }

  private void getProfile(int networkId) {

    SocialNetwork socialNetwork = socialNetworkManager.getSocialNetwork(networkId);
    socialNetwork.setOnRequestCurrentPersonCompleteListener(this);
    socialNetwork.requestCurrentPerson();
    showProgress(true);
  }

  @Override
  public void onLoginSuccess(int networkId) {

    showProgress(false);
    getProfile(networkId);
  }

  @Override
  public void onError(int networkId, String requestID, String errorMessage, Object data) {

    showProgress(false);
    Toast.makeText(getActivity(), "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
  }

  @OnClick(R.id.button_facebook)
  public void loginFacebook(View view) {

    SocialNetwork socialNetwork = socialNetworkManager.getSocialNetwork(FACEBOOK);
    if (!socialNetwork.isConnected()) {
      showProgress(true);
      socialNetwork.requestLogin();
    }
    else {
      getProfile(socialNetwork.getID());
    }
  }

  @OnClick(R.id.button_twitter)
  public void loginTwitter(View view) {

    SocialNetwork socialNetwork = socialNetworkManager.getSocialNetwork(TWITTER);
    if (!socialNetwork.isConnected()) {
      showProgress(true);
      socialNetwork.requestLogin();
    }
    else {
      getProfile(socialNetwork.getID());
    }
  }

  @OnClick(R.id.button_gplus)
  public void loginGPlus(View view) {

    SocialNetwork socialNetwork = socialNetworkManager.getSocialNetwork(GPLUS);
    if (!socialNetwork.isConnected()) {
      showProgress(true);
      socialNetwork.requestLogin();
    }
    else {
      getProfile(socialNetwork.getID());
    }
  }

  @OnClick(R.id.button_logout)
  public void logoutProfile(View view) {

    SocialNetwork facebookSocialNetwork = socialNetworkManager.getSocialNetwork(FACEBOOK);
    facebookSocialNetwork.logout();
    SocialNetwork twitterSocialNetwork = socialNetworkManager.getSocialNetwork(TWITTER);
    twitterSocialNetwork.logout();
    SocialNetwork gPlusSocialNetwork = socialNetworkManager.getSocialNetwork(GPLUS);
    gPlusSocialNetwork.logout();

    // remove the authState user
    application.authState.clearUser();

    // refresh the views
    refreshViewState();
  }

  @Override
  public void onRequestSocialPersonSuccess(int i, SocialPerson socialPerson) {

    showProgress(false);
    if (isAdded()) {
      textUsername.setText(socialPerson.name);
      //        id.setText(socialPerson.id);
      //        String socialPersonString = socialPerson.toString();
      //        String infoString = socialPersonString.substring(socialPersonString.indexOf("{")+1, socialPersonString.lastIndexOf("}"));
      //        info.setText(infoString.replace(", ", "\n"));
      setProfileImage(socialPerson.avatarURL, R.mipmap.ic_launcher);
    }
    // save the user
    User user = new User();
    user.setId(socialPerson.id);
    user.setFullName(socialPerson.name);
    user.setImageUrl(socialPerson.avatarURL);
    if (socialPerson.email == null) {
      // try to get a device email silently
      List<String> emails = Common.getUserEmailAccounts(application.getApplicationContext());
      if (emails.isEmpty()) {
        // TODO maybe set the user id as email address for later sync calls
        user.setEmail(socialPerson.id);
      }
      else {
        // get the first email from the accounts
        user.setEmail(emails.get(0));
      }
    }
    else {
      user.setEmail(socialPerson.email);
    }
    application.authState.setUser(user);

    if (isAdded()) {
      // set profile stats
      setProfileStats();

      // refresh the views
      refreshViewState();
    }
  }

  private void setProfileImage(String imageUrl, int ic_launcher) {

    final Target loadProfileBitmap = new Target() {

      @Override
      public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        // set the profile image
        imageProfile.setImageBitmap(bitmap);

        // generate the profile image Palette
        // Asynchronous methods
        // --------------------------------
        // This is the quick and easy integration path. Internally uses an AsyncTask so
        // this may not be optimal (since you're dipping in and out of threads)

        // Uses the default palette size (16).
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {

          @Override
          public void onGenerated(Palette palette) {
            // Here's your generated palette
            holderProfile.setBackgroundColor(palette.getDarkVibrantColor(R.color.primary_dark));
            textUsername.setTextColor(palette.getLightVibrantColor(R.color.text_icons));
          }
        });
      }

      @Override
      public void onBitmapFailed(Drawable errorDrawable) {

      }

      @Override
      public void onPrepareLoad(Drawable placeHolderDrawable) {

      }
    };
    // set the image tag
    imageProfile.setTag(loadProfileBitmap);
    // show profile image and name
    Picasso loader = Picasso.with(getActivity());
    RequestCreator requestCreator;
    if (imageUrl != null) {
      requestCreator = loader.load(imageUrl);
    }
    else {
      requestCreator = loader.load(ic_launcher);
    }
    requestCreator.resize(application.screenWidth / 2, application.screenWidth / 2)
            .centerInside()
            .transform(new CircleTransform())
            .into(loadProfileBitmap);
  }

  private void setProfileStats() {
    // set item and notes count
    RandomStats.Stat itemCount = RandomStats.with(getActivity()).getItemCount();
    RandomStats.Stat noteCount = RandomStats.with(getActivity()).getNoteCount();
    textItemCount.setText("Items: " + itemCount.getValue());
    textNotesCount.setText("Notes: " + noteCount.getValue());

    // show random stats; always show item count and notes count;
    RandomStats.Stat randomStat = RandomStats.with(getActivity()).getRandomStat();
    textRandomStat.setText(randomStat.getName() + " " + randomStat.getValue());
  }

  private void showProgress(boolean showProgress) {

    if (showProgress) {
      progressDialog.show();
    }
    else {
      progressDialog.dismiss();
    }
  }

  @Override
  public void onDismiss(final DialogInterface dialog) {

    super.onDismiss(dialog);
    final Activity activity = getActivity();
    if (activity != null && activity instanceof DialogInterface.OnDismissListener) {
      ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
    }
  }
}
