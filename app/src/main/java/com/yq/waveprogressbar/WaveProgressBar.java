package com.yq.waveprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import java.text.DecimalFormat;

/**
 * Created by Administrator on 2017/12/26.
 * see https://www.jianshu.com/p/34bbcd80dc7a
 */

public class WaveProgressBar extends View {

    Paint wavePaint;
    Paint secondWavePaint;
    Path wavePath;
    Path secondWavePath;
    private boolean drawSecondPath = false;
    int waveHeight = 60;
    int waveWidth = 80;
    int waveNum = 0;//波浪组数，一上一下为一组
    int circleColor = Color.GRAY;
    int waterColor = Color.GREEN;
    int secondWaterColor = Color.GREEN;

    float percent = 0;
    private WaveAnimation waveAnimation;
    private float waveMovDistance;//画面外x的位置，不断变化产生波浪平移效果
    private Paint circlePaint;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;

    private Paint textpaint;
    DecimalFormat df = new DecimalFormat("0.00");

    private int waveHeightRecord;//记录波高，随着进度波高变小
    private int minWaveHeight = 20;//最小波高
    private float progress = 0.8f;//最大进度

    public WaveProgressBar(Context context) {
        this(context, null);
    }

    public WaveProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init();
        setBackgroundColor(Color.TRANSPARENT);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveProgressBar);
        waveWidth = (int) a.getDimension(R.styleable.WaveProgressBar_waveWid, waveWidth);
        waveHeightRecord = waveHeight = (int) a.getDimension(R.styleable.WaveProgressBar_waveHeight, waveHeight);
        circleColor = a.getColor(R.styleable.WaveProgressBar_circle_color, circleColor);
        waterColor = a.getColor(R.styleable.WaveProgressBar_water_color, waterColor);
        secondWaterColor = a.getColor(R.styleable.WaveProgressBar_second_water_color, secondWaterColor);
        a.recycle();
    }

    private void init() {
        wavePath = new Path();
        secondWavePath = new Path();

        wavePaint = new Paint();
        wavePaint.setColor(waterColor);
        wavePaint.setAntiAlias(true);
        wavePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));//根据绘制顺序的不同选择相应的模式即可

        secondWavePaint = new Paint();
        secondWavePaint.setColor(secondWaterColor);
        secondWavePaint.setAntiAlias(true);
        secondWavePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));//根据绘制顺序的不同选择相应的模式即可

        circlePaint = new Paint();
        circlePaint.setColor(circleColor);
        circlePaint.setAntiAlias(true);//设置抗锯齿

        textpaint = new Paint();
        textpaint.setColor(Color.WHITE);
        textpaint.setTextSize(60);
        textpaint.setTextAlign(Paint.Align.CENTER);
        textpaint.setAntiAlias(true);

        waveAnimation = new WaveAnimation();
        waveAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (percent == progress) {
                    waveAnimation.setDuration(8000);
                    drawSecondPath = true;
                }
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        waveNum = (int) Math.ceil((double) getMeasuredWidth() / waveWidth / 2);
    }

    private Path getWavePath() {
        if (percent < progress)
            waveHeight = (int) (waveHeightRecord * (1 - percent)) > minWaveHeight ? (int) (waveHeightRecord * (1 - percent)) : minWaveHeight;
        wavePath.reset();
        wavePath.moveTo(getWidth(), (1 - percent) * getHeight());
        wavePath.lineTo(getWidth(), getHeight());
        wavePath.lineTo(0, getHeight());
        wavePath.lineTo(-waveMovDistance, (1 - percent) * getHeight());
        for (int i = 0; i < waveNum * 2; i++) {
            wavePath.rQuadTo(waveWidth / 2, waveHeight, waveWidth, 0);
            wavePath.rQuadTo(waveWidth / 2, -waveHeight, waveWidth, 0);
        }
        wavePath.close();
        return wavePath;
    }

    private Path getSecondWavePath() {
        if (percent < progress)
            waveHeight = (int) (waveHeightRecord * (1 - percent)) > minWaveHeight ? (int) (waveHeightRecord * (1 - percent)) : minWaveHeight;
        secondWavePath.reset();
        secondWavePath.moveTo(0, (1 - percent) * getHeight());
        secondWavePath.lineTo(0, getHeight());
        secondWavePath.lineTo(getWidth(), getHeight());
        secondWavePath.lineTo(getWidth() + waveMovDistance, (1 - percent) * getHeight());
        for (int i = 0; i < waveNum * 2; i++) {
            secondWavePath.rQuadTo(-waveWidth / 2, waveHeight, -waveWidth, 0);
            secondWavePath.rQuadTo(-waveWidth / 2, -waveHeight, -waveWidth, 0);
        }
        secondWavePath.close();
        return secondWavePath;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        bitmap = Bitmap.createBitmap(getWidth(), getWidth(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, circlePaint);
        bitmapCanvas.drawPath(getWavePath(), wavePaint);
        if (drawSecondPath) {
            bitmapCanvas.drawPath(getSecondWavePath(), secondWavePaint);
        }

        canvas.drawBitmap(bitmap, 0, 0, null);
        Paint.FontMetrics metrics = textpaint.getFontMetrics();
        canvas.drawText(df.format(Double.parseDouble(percent + "") * 100) + "%", getWidth() / 2, getHeight() / 2 - (metrics.descent + metrics.ascent) / 2, textpaint);
    }


    private class WaveAnimation extends Animation {

        @Override
        // interpolatedTime 0.0~1.0 ，动画重复执行时此方法重复调用
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            if (percent < progress)
                percent = interpolatedTime * progress;
            waveMovDistance = interpolatedTime * waveNum * waveWidth * 2;
            postInvalidate();
        }
    }

    public void startWaveAnimation(int time) {
        waveAnimation.setDuration(time);
        percent = 0;
//        waveAnimation.start();
        waveAnimation.setRepeatCount(Animation.INFINITE);
        waveAnimation.setInterpolator(new LinearInterpolator());
        this.startAnimation(waveAnimation);
    }

    @Override
    protected void onDetachedFromWindow() {
        //在绑定界面 destroy 的时候调用
        super.onDetachedFromWindow();
        if (waveAnimation != null) {
            waveAnimation.cancel();
            waveAnimation = null;
        }
    }
}
