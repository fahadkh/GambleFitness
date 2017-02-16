package io.github.fahadkh.gamblefitness;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Reveal extends AppCompatActivity {
    int pStatus = 0;
    private Handler handler = new Handler();
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reveal);

        SessionManager session = new SessionManager(getApplicationContext());
        int wager = session.getWager();
        int daily_goal = session.getDailyGoal();

        TextView goalline = (TextView)findViewById(R.id.daily_goal);
        goalline.setText("Your goal for today was " + daily_goal + " min." );

        Intent intent = getIntent();
        String user_selection = intent.getStringExtra(GamePage.USER_SELECT);

        //TODO: Input actual MVPA calculated from day

        final int actualMVPA = 20; // to be changed

        String[] nums = user_selection.split(" - ");
        int num1 = Integer.parseInt(nums[0]);
        int num2 = Integer.parseInt(nums[1]);
        boolean inRange = false;
        int wagerloss = 0;
        TextView coins = (TextView)findViewById(R.id.acti_coins);

        if (actualMVPA >= num1 && actualMVPA<= num2){
            inRange = true;
        }
        else{
            int absdiff = Math.min(Math.abs(actualMVPA-num1), Math.abs(actualMVPA-num2));
            //set a standardclass as 10 minutes. For each standard class away, the player loses a 5% of their wager
            int standardclassesaway = absdiff/10;
            wagerloss = (int) (standardclassesaway * 0.05 * wager);
        }

        if (inRange) {
            TextView uselect = (TextView) findViewById(R.id.user_selection);
            uselect.setText("You guessed in the right range! You win " + wager + " Acticoins!");
            session.addActiCoins(wager);
            int n = session.getActiCoins();
            coins.setText(n + " Acticoins");
        }
        else{
            TextView uselect = (TextView) findViewById(R.id.user_selection);
            uselect.setText("You guessed wrongly! You lose " + wagerloss + " Acticoins!");
            session.minusActiCoins(wagerloss);
            int n = session.getActiCoins();
            coins.setText(n + " Acticoins");
        }



        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.custom_progressbar_drawable);
        final ProgressBar mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgress.setProgress(actualMVPA);   // Main Progress
        mProgress.setSecondaryProgress(daily_goal); // Secondary Progress
        mProgress.setMax(daily_goal); // Maximum Progress
        mProgress.setProgressDrawable(drawable);

      /*  ObjectAnimator animation = ObjectAnimator.ofInt(mProgress, "progress", 0, 100);
        animation.setDuration(50000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();*/

        tv = (TextView) findViewById(R.id.txtProgress);
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (pStatus < actualMVPA) {
                    pStatus += 1;

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mProgress.setProgress(pStatus);
                            tv.setText(pStatus + "min");

                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        // Just to display the progress slowly
                        Thread.sleep(8); //thread will take approx 1.5 seconds to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void gotoSetTmrw(View view) {
        Intent intent = new Intent(this, Gamble.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }
}
