package com.app.afridge.ui.fragments.wizard;

import com.app.afridge.R;
import com.app.afridge.interfaces.FragmentLifecycle;
import com.app.afridge.interfaces.OnAnimationEvent;
import com.app.afridge.ui.FirstTimeWizardActivity;
import com.app.afridge.utils.AnimationsController;
import com.app.afridge.utils.Common;
import com.app.afridge.views.Typewriter;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link SocialLoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SocialLoginFragment extends Fragment implements FragmentLifecycle {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_MARGIN = "bottomMargin";

    private static final Object itemLock = new Object();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bottomMargin bottom margin for translucent themes.
     * @return A new instance of fragment SocialLoginFragment.
     */
    //  public static SocialLoginFragment newInstance(int bottomMargin) {
    //
    //    SocialLoginFragment fragment = new SocialLoginFragment();
    //    Bundle args = new Bundle();
    //    args.putInt(ARG_PARAM_MARGIN, bottomMargin);
    //    fragment.setArguments(args);
    //    return fragment;
    //  }

    // Singleton
    private static volatile SocialLoginFragment instance = null;

    @InjectView(R.id.text_title_social)
    Typewriter textTitle;

    @InjectView(R.id.text_description_social)
    Typewriter textDescription;

    @InjectView(R.id.image_facebook)
    ImageView imageFacebook;

    @InjectView(R.id.image_twitter)
    ImageView imageTwitter;

    @InjectView(R.id.image_gplus)
    ImageView imageGooglePlus;

    private int bottomMargin = 0;

    private boolean alreadyShown = false;

    public SocialLoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SocialLoginFragment.
     */
    public static SocialLoginFragment newInstance(int bottomMargin) {

        if (instance == null) {
            synchronized (SocialLoginFragment.class) {
                if (instance == null) {
                    instance = new SocialLoginFragment();
                    instance.bottomMargin = bottomMargin;
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bottomMargin = getArguments().getInt(ARG_PARAM_MARGIN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View containerView = inflater.inflate(R.layout.fragment_social_login, container, false);

        // navigation bar height margin hack
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) containerView
                .findViewById(R.id.height_hack).getLayoutParams();
        params.height = (int) (bottomMargin * 0.85f);
        containerView.findViewById(R.id.height_hack).setLayoutParams(params);

        // inject and return the view
        ButterKnife.inject(this, containerView);
        return containerView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        // TODO add enter animations here
        if (!alreadyShown) {
            imageFacebook.setAlpha(0f);
            imageTwitter.setAlpha(0f);
            imageGooglePlus.setAlpha(0f);
            textTitle.setVisibility(View.INVISIBLE);
            textDescription.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            animateText();
        }
    }

    private void animateText() {

        synchronized (itemLock) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {

                    if (textTitle != null && textDescription != null && !alreadyShown) {
                        alreadyShown = true;

                        // show items
                        textTitle.setVisibility(View.VISIBLE);
                        textDescription.setVisibility(View.VISIBLE);

                        // set text
                        textTitle.initSpanText(getString(R.string.social_title),
                                getResources().getColor(R.color.primary_dark));
                        textDescription.initSpanText(getString(R.string.social_description),
                                getResources().getColor(R.color.text_secondary));

                        // animate
                        textTitle.animateText();
                        textDescription.animateText();
                        // images animation if OS newer then 12
                        if (Common.versionAtLeast(12)) {
                            AnimationsController
                                    .fadeInAndScale(imageFacebook, new OnAnimationEvent() {

                                        @Override
                                        public void onEnd() {

                                            AnimationsController.fadeInAndScale(imageTwitter,
                                                    new OnAnimationEvent() {

                                                        @Override
                                                        public void onEnd() {

                                                            AnimationsController.fadeInAndScale(
                                                                    imageGooglePlus);
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                    // enable the view pager
                    ((FirstTimeWizardActivity) getActivity()).setPagingEnabled(true);
                }
            }, 100);
        }
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {

    }
}
