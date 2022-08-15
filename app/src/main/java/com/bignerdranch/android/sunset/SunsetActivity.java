package com.bignerdranch.android.sunset;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

public class SunsetActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new SunsetFragment();
    }
}