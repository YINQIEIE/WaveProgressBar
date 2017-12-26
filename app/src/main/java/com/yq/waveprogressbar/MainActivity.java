package com.yq.waveprogressbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WaveProgressBar waveProgressBar = (WaveProgressBar) findViewById(R.id.wp);
        waveProgressBar.startWaveAnimation(3000);
    }
}
