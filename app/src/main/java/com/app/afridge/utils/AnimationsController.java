package com.app.afridge.utils;

import com.app.afridge.interfaces.OnAnimationEvent;

import android.animation.Animator;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


/**
 * Animations controller
 * <p/>
 * Created by drakuwa on 15.07.2014.
 */
public class AnimationsController {

    public static final int DURATION_SHORT = 200;

    public static final int DURATION_MEDIUM = 400;

    public static final int DURATION_LONG = 600;

    public static boolean fragmentAnimationsEnabled = true;

    public static boolean homeAsUpEnabled = true;

    //    public static void expandBackground(View backgroundHolder) {
    //
    //        Context context = backgroundHolder.getContext();
    //
    //        backgroundHolder.setTranslationY(0 - Utils.getScreenHeight(context) + Utils.getActionBarHeight(context));
    //        backgroundHolder.animate()
    //                .setDuration(DURATION_MEDIUM)
    //                .setInterpolator(new DecelerateInterpolator())
    //                .translationYBy(Utils.getScreenHeight(context) - Utils.getActionBarHeight(context))
    //                .start();
    //    }
    //
    //    public static void collapseBackground(View backgroundHolder) {
    //
    //        Context context = backgroundHolder.getContext();
    //
    //        RectF mRect = new RectF();
    //        Utils.getOnScreenRect(mRect, backgroundHolder);
    //
    //        backgroundHolder.animate()
    //                .translationY(0 - mRect.height() + Utils.getActionBarHeight(context))
    //                .setDuration(DURATION_MEDIUM)
    //                .setInterpolator(new DecelerateInterpolator())
    //                .start();
    //    }

    public static void expandUp(View view, final View containerView, final int totalHeight,
            final int totalWidth) {

        final int initialHeight = view.getMeasuredHeight();
        final int initialWidth = view.getMeasuredWidth();

        final float totalTranslateX = view.getX();
        final float totalTranslateY = view.getY();

        //        containerView.measure(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        //        totalHeight = containerView.getMeasuredHeight();
        //        totalWidth = containerView.getMeasuredWidth();

        Log.d(Log.TAG, "initialHeight: " + initialHeight + "; totalHeight: " + totalHeight
                + "; initialWidth: " + initialWidth + "; totalWidth: " + totalWidth);

        // containerView.setVisibility(View.VISIBLE);
        Animation animation = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                if (interpolatedTime <= 1) {
                    // containerView.setAlpha(interpolatedTime);
                    containerView.getLayoutParams().height = interpolatedTime == 1
                            ? FrameLayout.LayoutParams.MATCH_PARENT
                            : initialHeight + (int) ((totalHeight - initialHeight)
                                    * interpolatedTime);
                    containerView.getLayoutParams().width = interpolatedTime == 1
                            ? FrameLayout.LayoutParams.MATCH_PARENT
                            : initialWidth + (int) ((totalWidth - initialWidth) * interpolatedTime);
                    containerView.requestLayout();
                    //                    containerView.getLayoutParams().height = initialHeight + (int) (totalHeight * interpolatedTime);
                    //                    containerView.getLayoutParams().width = initialWidth + (int) (totalWidth * interpolatedTime);
                    //                    containerView.requestLayout();

                    containerView
                            .setTranslationX(totalTranslateX - totalTranslateX * interpolatedTime);
                    containerView
                            .setTranslationY(totalTranslateY - totalTranslateY * interpolatedTime);
                }
            }

            @Override
            public boolean willChangeBounds() {

                return true;
            }
        };

        containerView.setX(view.getX());
        containerView.setY(view.getY());

        animation.setDuration(DURATION_SHORT);
        animation.setInterpolator(new DecelerateInterpolator());
        containerView.startAnimation(animation);
    }

    public static void fadeIn(final View view) {

        // do a random fade in and translate view animation
        view.animate()
                .alpha(1)
                .setDuration(DURATION_MEDIUM)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {

                        view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    public static void fadeOut(final View view) {

        // do a random fade in and translate view animation
        view.animate()
                .alpha(0)
                .setDuration(DURATION_MEDIUM)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        view.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    public static void fadeInAndTranslate(View view) {

        // do a random fade in and translate view animation
        view.setTranslationY(500);
        view.setTranslationX(-500);
        view.setAlpha(0);
        view.setScaleX(0);
        view.setScaleY(0);
        view.animate()
                .translationY(0)
                .translationX(0)
                .alpha(1)
                .scaleX(1)
                .scaleY(1)
                .setDuration(DURATION_LONG)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    public static void fadeInAndTranslate(View view, View anchor) {

        // use the view anchor as a reference point for the animation
        view.setTranslationY(anchor.getHeight());
        view.animate()
                .translationY(0)
                .alpha(1)
                .setDuration(DURATION_LONG)
                .start();
    }

    public static void fadeInAndScale(View view) {

        // do a random fade in and translate view animation
        // view.setTranslationY(500);
        view.setAlpha(0);
        view.setScaleX(0);
        view.setScaleY(0);
        view.animate()
                .translationY(0)
                .alpha(1)
                .scaleX(1)
                .scaleY(1)
                .setDuration(DURATION_LONG)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    public static void fadeInAndScale(View view, final OnAnimationEvent callback) {

        // do a random fade in and translate view animation
        // view.setTranslationY(500);
        view.setAlpha(0);
        view.setScaleX(0);
        view.setScaleY(0);
        view.animate()
                .translationY(0)
                .alpha(1)
                .scaleX(1)
                .scaleY(1)
                .setDuration(DURATION_LONG)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        callback.onEnd();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    public static void fadeOutAndTranslate(View view) {
        // do a random fade out and translate view animation
        view.animate()
                .translationY(500)
                .alpha(0)
                .setDuration(DURATION_LONG)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public static void fadeOutAndTranslate(View view, View anchor) {

        // use the view anchor as a reference point for the animation
        view.animate()
                .translationY(anchor.getHeight())
                .alpha(0)
                .setDuration(DURATION_MEDIUM)
                .start();
    }

    public static void fadeOutAndScale(View view) {
        // do a random fade out and scale view animation
        view.animate()
                .scaleX(0)
                .scaleY(0)
                .alpha(0)
                .setDuration(DURATION_LONG)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public static void scaleAndTranslateView(RectF actionBarLogoRect, RectF logoRect, View logo,
            final View logoText) { //, final OnAnimationEvent callback) {

        float horizontalOffset
                = 24; // logo.getContext().getResources().getDimension(R.dimen.splash_animation_horizontal_offset);
        float verticalOffset
                = 24; // logo.getContext().getResources().getDimension(R.dimen.splash_animation_vertical_offset);

        logoText.animate()
                .x(actionBarLogoRect.right + actionBarLogoRect.left + 4 * horizontalOffset)
                .y(actionBarLogoRect.top + 4 * verticalOffset)
                .scaleX(0.35f)
                .scaleY(0.35f)
                .setDuration(DURATION_MEDIUM)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // get the scale factors
        float scaleX = 1.0F + 0.97f * (actionBarLogoRect.width() / logoRect.width() - 1.0F);
        float scaleY = 1.0F + 0.97f * (actionBarLogoRect.height() / logoRect.height() - 1.0F);

        logo.animate()
                .x(actionBarLogoRect.right - actionBarLogoRect.left - horizontalOffset)
                .y(actionBarLogoRect.top - verticalOffset)
                .scaleX(scaleX)
                .scaleY(scaleY)
                .setDuration(DURATION_MEDIUM)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animator) {

                        // callback.onStart();
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                        // callback.onEnd();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                })
                .start();
    }

    public static void reverseScaleAndTranslateView(RectF actionBarLogoRect, RectF logoRect,
            RectF logoTextRect, View logo, View logoText) {

        // animate the text
        float horizontalOffset
                = 24; // logo.getContext().getResources().getDimension(R.dimen.splash_animation_horizontal_offset);
        float verticalOffset
                = 24; // logo.getContext().getResources().getDimension(R.dimen.splash_animation_vertical_offset);

        logoText.setScaleX(0.35f);
        logoText.setScaleY(0.35f);
        logoText.setX(actionBarLogoRect.right + actionBarLogoRect.left + 4 * horizontalOffset);
        logoText.setY(actionBarLogoRect.top + 4 * verticalOffset);

        logoText.animate()
                .x(logoTextRect.left)
                .y(logoTextRect.top)
                .scaleX(1)
                .scaleY(1)
                .setDuration(DURATION_MEDIUM)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // animate the logo
        float scaleX = 1.0F + 0.97f * (actionBarLogoRect.width() / logoRect.width() - 1.0F);
        float scaleY = 1.0F + 0.97f * (actionBarLogoRect.height() / logoRect.height() - 1.0F);

        logo.setScaleX(scaleX);
        logo.setScaleY(scaleY);
        logo.setX(actionBarLogoRect.right - actionBarLogoRect.left - 12);
        logo.setY(actionBarLogoRect.top - 4);

        logo.animate()
                .x(logoRect.left)
                .y(logoRect.top)
                .scaleX(1)
                .scaleY(1)
                .setDuration(DURATION_MEDIUM)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public static void translateX(View view, float translationX) {

        view.animate()
                .translationX(translationX)
                .setDuration(AnimationsController.DURATION_SHORT)
                .start();
    }

    public static void expandUp(final View view, final int finalHeight) {

        final int initialHeight = view.getMeasuredHeight();
        final int totalHeight;
        if (finalHeight == 0) {
            view.measure(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            totalHeight = view.getMeasuredHeight() - initialHeight;
        } else {
            totalHeight = finalHeight - initialHeight;
        }

        view.setVisibility(View.VISIBLE);
        Animation animation = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                if (interpolatedTime <= 1) {

                    view.setAlpha(interpolatedTime);
                    view.getLayoutParams().height = initialHeight + (int) (totalHeight
                            * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {

                return true;
            }
        };

        animation.setDuration(DURATION_SHORT);
        animation.setInterpolator(new AccelerateInterpolator());
        view.startAnimation(animation);
    }

    public static void expandRight(final View view) {

        final int initialWidth = view.getMeasuredWidth();

        view.measure(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        final int finalWidth = view.getMeasuredWidth();

        view.setVisibility(View.VISIBLE);
        Animation animation = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                if (interpolatedTime <= 1) {

                    view.setAlpha(interpolatedTime);
                    view.getLayoutParams().width = initialWidth + (int) ((finalWidth - initialWidth)
                            * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {

                return true;
            }
        };

        animation.setDuration(DURATION_SHORT);
        animation.setInterpolator(new AccelerateInterpolator());
        view.startAnimation(animation);
    }

    public static void collapseUp(final View view) {

        final int initialHeight = view.getMeasuredHeight();
        Animation animation = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                if (interpolatedTime <= 1) {

                    view.setAlpha(1 - interpolatedTime);
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight
                            * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {

                return true;
            }
        };

        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation.setDuration(DURATION_MEDIUM);
        animation.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(animation);
    }

    public static void collapseLeft(final View view) {

        final int initialWidth = view.getMeasuredWidth();
        Animation animation = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                if (interpolatedTime <= 1) {

                    view.setAlpha(1 - interpolatedTime);
                    view.getLayoutParams().width = initialWidth - (int) (initialWidth
                            * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {

                return true;
            }
        };

        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation.setDuration(DURATION_SHORT);
        animation.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(animation);
    }
}
