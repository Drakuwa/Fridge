package com.app.afridge.utils.animations;

import com.nineoldandroids.animation.Animator;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;

import java.lang.ref.WeakReference;


/**
 * @hide
 */
public interface RevealAnimator {

    public void setClipOutlines(boolean clip);

    public void setCenter(float cx, float cy);

    public void setTarget(View target);

    public float getRevealRadius();

    public void setRevealRadius(float value);

    public void invalidate(Rect bounds);

    static class RevealFinishedGingerbread extends ViewAnimationUtils.SimpleAnimationListener {

        WeakReference<RevealAnimator> mReference;

        volatile Rect mInvalidateBounds;

        RevealFinishedGingerbread(RevealAnimator target, Rect bounds) {

            mReference = new WeakReference<>(target);
            mInvalidateBounds = bounds;
        }

        @Override
        public void onAnimationEnd(Animator animation) {

            super.onAnimationEnd(animation);

            RevealAnimator target = mReference.get();

            if (target == null) {
                return;
            }

            target.setClipOutlines(false);
            target.setCenter(0, 0);
            target.setTarget(null);
            target.invalidate(mInvalidateBounds);
        }
    }


    static class RevealFinishedIceCreamSandwich extends ViewAnimationUtils.SimpleAnimationListener {

        WeakReference<RevealAnimator> mReference;

        volatile Rect mInvalidateBounds;

        int mLayerType;

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        RevealFinishedIceCreamSandwich(RevealAnimator target, Rect bounds) {

            mReference = new WeakReference<>(target);
            mInvalidateBounds = bounds;

            mLayerType = ((View) target).getLayerType();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void onAnimationStart(Animator animation) {

            super.onAnimationStart(animation);
            ((View) mReference.get()).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void onAnimationEnd(Animator animation) {

            super.onAnimationEnd(animation);
            ((View) mReference.get()).setLayerType(mLayerType, null);

            RevealAnimator target = mReference.get();

            if (target == null) {
                return;
            }

            target.setClipOutlines(false);
            target.setCenter(0, 0);
            target.setTarget(null);
            target.invalidate(mInvalidateBounds);
        }
    }


    static class RevealFinishedJellyBeanMr1 extends ViewAnimationUtils.SimpleAnimationListener {

        WeakReference<RevealAnimator> mReference;

        volatile Rect mInvalidateBounds;

        int mLayerType;

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        RevealFinishedJellyBeanMr1(RevealAnimator target, Rect bounds) {

            mReference = new WeakReference<>(target);
            mInvalidateBounds = bounds;

            mLayerType = ((View) target).getLayerType();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void onAnimationStart(Animator animation) {

            super.onAnimationStart(animation);
            ((View) mReference.get()).setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void onAnimationEnd(Animator animation) {

            super.onAnimationEnd(animation);
            ((View) mReference.get()).setLayerType(mLayerType, null);

            RevealAnimator target = mReference.get();

            if (target == null) {
                return;
            }

            target.setClipOutlines(false);
            target.setCenter(0, 0);
            target.setTarget(null);
            target.invalidate(mInvalidateBounds);
        }
    }
}