package com.bignerdranch.android.sunset;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SunsetFragment extends Fragment {
    private static final String TAG = "SunsetFragment";

    public static final String PREF_IS_SUNSET = "isSunset";
    private View mSceneView;
    private View mSunView;
    private View mSkyView;
    private int mBlue_sky;
    private int mSunset_sky;
    private int mNight_sky;
    private View mReflectionView;
    private View mSeaView;
    private Float mSunY;
    private Float mReflectionY;
    private AnimatorSet mAnimatorSet;
    private Integer mSunsetBackgroundColor;
    private Integer mSunsetBackgroundColor2;
    private ObjectAnimator mNightSkyAnimator;
    private ObjectAnimator mHeightAnimator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mSceneView = inflater.inflate(R.layout.fragment_sunset, container, false);
        mSunView = mSceneView.findViewById(R.id.sun);
        mSkyView = mSceneView.findViewById(R.id.sky);
        mReflectionView = mSceneView.findViewById(R.id.reflection);
        mSeaView = mSceneView.findViewById(R.id.sea);

        mBlue_sky = getResources().getColor(R.color.blue_sky);
        mSunset_sky = getResources().getColor(R.color.sunset_sky);
        mNight_sky = getResources().getColor(R.color.night_sky);


        mSceneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean isSunset = sharedPreferences
                        .getBoolean(PREF_IS_SUNSET, false);

                startAnimation(!isSunset);

                sharedPreferences.edit()
                        .putBoolean(PREF_IS_SUNSET, !isSunset)
                        .apply();

            }
        });
        return mSceneView;
    }

    private void startAnimation(boolean isSunset) {
        if (isSunset) {
            sunset(false);
        } else {
            sunset(true);
        }
    }

    private void sunset(boolean isSunrise) {
        boolean nightSkyStared=false;
        if (mNightSkyAnimator != null && mNightSkyAnimator.isStarted()) {
            nightSkyStared=true;
        }

        boolean heightAnimatorStarted=false;
        if (mHeightAnimator != null && mHeightAnimator.isStarted()) {
            heightAnimatorStarted=true;
        }

        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }

        float sunYStart;
        float sunYEnd;


        if (!isSunrise) {
            sunYStart = mSunY != null ? mSunY : mSunView.getTop();
            sunYEnd = mSkyView.getHeight();
        } else {
            sunYStart = mSunY != null ? mSunY : mSkyView.getHeight();
            sunYEnd = mSunView.getTop();
        }
        mHeightAnimator = ObjectAnimator.ofFloat(mSunView, "y", sunYStart, sunYEnd)
                .setDuration(3000);
        mHeightAnimator.setInterpolator(new AccelerateInterpolator());
        mHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSunY = (Float) animation.getAnimatedValue("y");
            }
        });

        float reflectionYStart;
        float reflectionYEnd;

        if (!isSunrise) {
            reflectionYStart = mReflectionY != null ? mReflectionY : mReflectionView.getTop();
            reflectionYEnd = -mReflectionView.getHeight();
        } else {
            reflectionYStart = mReflectionY != null ? mReflectionY : -mReflectionView.getHeight();
            reflectionYEnd = mReflectionView.getTop();
        }
        ObjectAnimator reflectionAnimator = ObjectAnimator.ofFloat(mReflectionView, "y", reflectionYStart, reflectionYEnd)
                .setDuration(3000);
        reflectionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mReflectionY = (float) animation.getAnimatedValue("y");
            }
        });


        ObjectAnimator sunsetSkyAnimator = buildSunsetSkyAnimator(isSunrise, mBlue_sky, mSunset_sky
                , () -> mSunsetBackgroundColor, integer -> mSunsetBackgroundColor = integer);


        mNightSkyAnimator = buildSunsetSkyAnimator(isSunrise, mSunset_sky, mNight_sky, () -> mSunsetBackgroundColor2, i -> mSunsetBackgroundColor2 = i);

        mAnimatorSet = new AnimatorSet();
        if (!isSunrise) {//sunset

            if (!nightSkyStared) {
                mAnimatorSet.play(mHeightAnimator)
                        .with(reflectionAnimator)
                        .with(sunsetSkyAnimator)
                        .before(mNightSkyAnimator)
                ;

            }else{
                mAnimatorSet.play(mNightSkyAnimator);
            }
        } else {//sunrise
            if (!heightAnimatorStarted) {
                mAnimatorSet.play(mNightSkyAnimator);
                mAnimatorSet.play(mHeightAnimator)
                        .with(reflectionAnimator)
                        .with(sunsetSkyAnimator);
            }else{
                mAnimatorSet.play(mHeightAnimator)
                        .with(reflectionAnimator)
                        .with(sunsetSkyAnimator);
            }


        }

        mAnimatorSet.start();


    }

    @NonNull
    private ObjectAnimator buildSunsetSkyAnimator(boolean isSunrise, int blue_sky, int sunset_sky
            , Supplier<Integer> supplier, Consumer<Integer> consumer
    ) {
        int sunsetBackgroundColorStart;
        int sunsetBackgroundColorEnd;
        Integer sunsetBackgroundColor = supplier.get();
        if (!isSunrise) {
            sunsetBackgroundColorStart = sunsetBackgroundColor != null ? mSunsetBackgroundColor : blue_sky;
            sunsetBackgroundColorEnd = sunset_sky;
        } else {
            sunsetBackgroundColorStart = sunsetBackgroundColor != null ? sunsetBackgroundColor : sunset_sky;
            sunsetBackgroundColorEnd = blue_sky;
        }

        ObjectAnimator sunsetSkyAnimator = ObjectAnimator.ofInt(mSkyView, "backgroundColor", sunsetBackgroundColorStart, sunsetBackgroundColorEnd)
                .setDuration(3000);
        sunsetSkyAnimator.setEvaluator(new ArgbEvaluator());
        sunsetSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int backgroundColor = (Integer) animation.getAnimatedValue("backgroundColor");
                consumer.accept(backgroundColor);

            }
        });
        return sunsetSkyAnimator;
    }

}
