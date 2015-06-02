package com.app.afridge.ui.fragments;

import com.app.afridge.R;
import com.app.afridge.views.AdvancedTextView;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Application about dialog
 *
 * Created by drakuwa on 6/2/15.
 */
public class AboutFragment extends DialogFragment {

    // add ButterKnife injects
    @InjectView(R.id.text_app_version)
    AdvancedTextView textAppVersion;

    @InjectView(R.id.text_login_description)
    AdvancedTextView textAbout;

    // Singleton
    private static volatile AboutFragment instance = null;

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

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRetainInstance(true); // try to fix orientation change
        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.AppTheme); // 0 is the default theme for the selected style
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
}
