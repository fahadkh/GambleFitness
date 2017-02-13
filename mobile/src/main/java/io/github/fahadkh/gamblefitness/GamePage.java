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

public class GamePage extends AppCompatActivity {
    public final static String USER_SELECT = "com.example.GambleFitness.USER_SELECT";
    String user_selection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_page);

        Intent intent = getIntent();

        Spinner gamespin = (Spinner) findViewById(R.id.activity_guesser_spinner);
        String[] items = new String[120];
        int j = 0;
        for (int i = 0; i < 120; i++) {
            int temp = j+10;
            String item = Integer.toString(j) + " - " + Integer.toString(temp);
            j+=10;
            items[i] = item;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        gamespin.setAdapter(adapter);

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(gamespin);

            // Set popupWindow height to 200px
            popupWindow.setHeight(200);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        gamespin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                user_selection = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }

        });
    }

    public void gotoReveal(View view) {
        Intent intent = new Intent(this, Reveal.class);
        intent.putExtra(USER_SELECT, user_selection);
        startActivity(intent);
    }
}
