package io.github.fahadkh.gamblefitness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;

public class Gamble extends AppCompatActivity {

    private Spinner newgoal_spin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamble);

        Intent intent = getIntent();

        newgoal_spin = (Spinner) findViewById(R.id.goal_spinner);
        Integer[] items = new Integer[120];
        for (int i = 0; i < 120; i++) {
            items[i] =(5*i);
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, items);
        newgoal_spin.setAdapter(adapter);

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(newgoal_spin);

            // Set popupWindow height to 200px
            popupWindow.setHeight(200);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        newgoal_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }

        });

    }


    public void gotoNightHolding(View view) {
        Intent intent = new Intent(this, NightHold.class);
        startActivity(intent);
    }
    }
