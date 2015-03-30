package com.app.afridge.ui.fragments.wizard;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.app.afridge.R;
import com.app.afridge.interfaces.FragmentLifecycle;
import com.app.afridge.ui.FirstTimeWizardActivity;
import com.app.afridge.utils.AnimationsController;
import com.app.afridge.views.Typewriter;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link WelcomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WelcomeFragment extends Fragment implements FragmentLifecycle {

  @InjectView(R.id.text_title_welcome)
  Typewriter textTitle;
  @InjectView(R.id.text_description_welcome)
  Typewriter textDescription;
  @InjectView(R.id.image_welcome)
  ImageView imageWelcome;

  private int bottomMargin = 0;
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM_MARGIN = "bottomMargin";
  private static final Object itemLock = new Object();
  private boolean alreadyShown = false;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param bottomMargin bottom margin for translucent themes.
   * @return A new instance of fragment WelcomeFragment.
   */
  //  public static WelcomeFragment newInstance(int bottomMargin) {
  //
  //    WelcomeFragment fragment = new WelcomeFragment();
  //    Bundle args = new Bundle();
  //    args.putInt(ARG_PARAM_MARGIN, bottomMargin);
  //    fragment.setArguments(args);
  //    return fragment;
  //  }

  // Singleton
  private static volatile WelcomeFragment instance = null;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment WelcomeFragment.
   */
  public static WelcomeFragment newInstance(int bottomMargin) {

    if (instance == null) {
      synchronized (WelcomeFragment.class) {
        if (instance == null) {
          instance = new WelcomeFragment();
          instance.bottomMargin = bottomMargin;
        }
      }
    }
    return instance;
  }

  public WelcomeFragment() {
    // Required empty public constructor
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
    View containerView = inflater.inflate(R.layout.fragment_welcome, container, false);

    // navigation bar height margin hack
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) containerView.findViewById(R.id.height_hack).getLayoutParams();
    params.height = (int) (bottomMargin * 0.85f);
    containerView.findViewById(R.id.height_hack).setLayoutParams(params);

    // inject and return the view
    ButterKnife.inject(this, containerView);
    return containerView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {

    super.onViewCreated(view, savedInstanceState);
    // TODO welcome - keep track of your fridge items
    if (!alreadyShown) {
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
            textTitle.initSpanText(getString(R.string.welcome_title), getResources().getColor(R.color.primary_dark));
            textDescription.initSpanText(getString(R.string.welcome_description), getResources().getColor(R.color.text_secondary));

            // animate
            textTitle.animateText();
            textDescription.animateText();
            AnimationsController.fadeInAndTranslate(imageWelcome);
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
