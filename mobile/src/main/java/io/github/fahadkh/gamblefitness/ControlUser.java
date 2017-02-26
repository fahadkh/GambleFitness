package io.github.fahadkh.gamblefitness;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ControlUser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_user);


        Integer mvpa;

        //TODO: Send MVPA
        //Here
        //TODO: Pull MVPA
        //replace this
        mvpa = 20;


        TextView mvpasofar = (TextView) findViewById(R.id.mvpa_control);
        mvpasofar.setText(mvpa.toString() + " min");
    }

}
