package com.app.afridge.ui.fragments;

import com.app.afridge.BuildConfig;
import com.app.afridge.R;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.Log;
import com.app.afridge.views.AdvancedTextView;
import com.eftimoff.androidplayer.Player;
import com.eftimoff.androidplayer.actions.property.PropertyAction;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Application about dialog
 *
 * Created by drakuwa on 6/2/15.
 */
public class AboutFragment extends DialogFragment {

    // Singleton
    private static volatile AboutFragment instance = null;

    // add ButterKnife injects
    @InjectView(R.id.text_app_version)
    AdvancedTextView textAppVersion;

    @InjectView(R.id.text_about)
    AdvancedTextView textAbout;

    @InjectView(R.id.holder_profile)
    LinearLayout headerLayout;

    @InjectView(R.id.text_random_stat)
    AdvancedTextView textRandom;

    @InjectView(R.id.image_logo)
    ImageView imageLogo;

    public AboutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AboutFragment.
     */
    public static AboutFragment getInstance() {

        if (instance == null) {
            synchronized (AboutFragment.class) {
                if (instance == null) {
                    instance = new AboutFragment();
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRetainInstance(true); // try to fix orientation change
        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.DialogTheme); // 0 is the default theme for the selected style
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View containerView = inflater.inflate(R.layout.fragment_about, container, false);

        // inject and return the view
        ButterKnife.inject(this, containerView);
        return containerView;
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize the link to GitHub
        initGitHubTextSpan();

        // set the version string
        initApplicationVersion();

        // initialize intro animation
        initTransactions();
    }

    private void initTransactions() {
        final PropertyAction fabAction = PropertyAction.newPropertyAction(textRandom).
                scaleX(0).
                scaleY(0).
                duration(350).
                interpolator(new AccelerateDecelerateInterpolator()).
                build();
        final PropertyAction headerAction = PropertyAction.newPropertyAction(headerLayout).
                interpolator(new DecelerateInterpolator()).
                translationY(-200).
                duration(350).
                alpha(0.4f).
                build();
        final PropertyAction logoAction = PropertyAction.newPropertyAction(imageLogo).
                scaleX(0).
                scaleY(0).
                duration(350).
                interpolator(new AccelerateDecelerateInterpolator()).
                build();
        final PropertyAction bottomAction = PropertyAction.newPropertyAction(textAbout).
                translationY(500).
                duration(350).
                alpha(0f).
                build();

        Player.init().
                animate(headerAction).
                then().
                animate(logoAction).
                then().
                animate(fabAction).
                then().
                animate(bottomAction).
                play();
    }

    public void initApplicationVersion() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo != null) {
            textAppVersion.setText(String.format(getActivity().getString(R.string.app_version),
                    packageInfo.versionName));
        } else {
            textAppVersion
                    .setText(String.format(getActivity().getString(R.string.app_version), "2.0"));
        }
    }

    public void initGitHubTextSpan() {
        // set the spannable string
        String text = getActivity().getString(R.string.text_about);
        SpannableString spannable = new SpannableString(text);

        int start = text.indexOf(getActivity().getString(R.string.text_github));
        if (start == -1) {
            return;
        }
        int end = start + getActivity().getString(R.string.text_github).length();

        ClickableSpan span = new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getActivity().getString(R.string.text_github)));
                startActivity(i);
            }
        };
        spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textAbout.setText(spannable);
        textAbout.setLinkTextColor(getResources().getColor(R.color.primary_dark));
        textAbout.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @OnClick(R.id.image_close)
    public void closeDialog() {
        this.dismiss();
    }

    /**
     * Donate with bitcoin by opening a bitcoin: intent if available.
     */
    @OnClick(R.id.donations_bitcoin_button)
    public void donateBitcoinOnClick(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("bitcoin:" + Constants.BITCOIN_WALLET_ADDRESS));

        if (BuildConfig.DEBUG) {
            Log.d(Log.TAG, "Attempting to donate bitcoin using URI: " + i.getDataString());
        }

        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            view.findViewById(R.id.donations_bitcoin_button).performLongClick();
        }
    }

    @OnLongClick(R.id.donations_bitcoin_button)
    public boolean copyBitCoinAddress() {
        // http://stackoverflow.com/a/11012443/832776
        if (Build.VERSION.SDK_INT >= 11) {
            ClipboardManager clipboard =
                    (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(Constants.BITCOIN_WALLET_ADDRESS,
                    Constants.BITCOIN_WALLET_ADDRESS);
            clipboard.setPrimaryClip(clip);
        } else {
            @SuppressWarnings("deprecation")
            android.text.ClipboardManager clipboard =
                    (android.text.ClipboardManager) getActivity()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(Constants.BITCOIN_WALLET_ADDRESS);
        }
        Toast.makeText(getActivity(), R.string.donations_bitcoin_toast_copy, Toast.LENGTH_SHORT)
                .show();
        return true;
    }
}
