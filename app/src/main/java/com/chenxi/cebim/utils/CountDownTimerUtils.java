package com.chenxi.cebim.utils;

import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenxi.cebim.R;

public class CountDownTimerUtils extends CountDownTimer {
    private TextView mTextView;
    private ImageView mPlayAudioImage;
    private int countDownOrspangled;//倒计时为0，闪动为1
    private int spangledNum;

    /**
     * @param textView          The TextView
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receiver
     *                          {@link #onTick(long)} callbacks.
     */
    public CountDownTimerUtils(TextView textView, long millisInFuture, long countDownInterval, int countDown) {
        super(millisInFuture, countDownInterval);
        this.mTextView = textView;
        this.countDownOrspangled = countDown;
    }

    public CountDownTimerUtils(ImageView playAudioImage, long millisInFuture, long countDownInterval, int spangled) {
        super(millisInFuture, countDownInterval);
        this.mPlayAudioImage = playAudioImage;
        this.countDownOrspangled = spangled;
    }

    @Override
    public void onTick(long millisUntilFinished) {

        int rightTime = (int) millisUntilFinished / 1000 + 1;//矫正时间
        if (countDownOrspangled == 0) {
            mTextView.setClickable(false); //设置不可点击
            if (rightTime == 60) {
                mTextView.setText("01:00");
            } else {
                mTextView.setText("00:" + rightTime / 10 + rightTime % 10);
            }
        } else if (countDownOrspangled == 1) {
            spangledNum++;
            if (spangledNum %2 == 0) {
                mPlayAudioImage.setImageResource(R.drawable.play_audio_first);
            } else if(spangledNum %2 == 1){
                mPlayAudioImage.setImageResource(R.drawable.play_audio_second);
            }
        }

    }

    @Override
    public void onFinish() {
        countDownOrspangled=1000;//用于停止上面的onTick
    }
}
