package io.github.fahadkh.gamblefitness;

/**
 * Created by rovik on 2/15/2017.
 * Code adapted from AndroidHive
 */

        import java.util.HashMap;

        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.content.SharedPreferences.Editor;
        import android.nfc.Tag;
        import android.util.Log;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;
    private static final String TAG = "SessionManager";
    // Sharedpref file name
    private static final String PREF_NAME = "FitGamePref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // DailyGoal
    private static final String WEEKLY_GOAL = "weeklygoal";

    private static final String DAILY_GOAL = "dailygoal";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";

    public static final String ACTICOINS = "coins";
    public static final String WAGER = "wager";
    public static final String GOAL_SET = "goal_set";
    public static final String U_TYPE = "user_type";
    public static final String MVPA ="mvpa";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String name){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        editor.putInt(ACTICOINS, 20);
        editor.putBoolean(GOAL_SET, true);

        editor.putInt(WAGER, 10);

        editor.putBoolean(U_TYPE,true);
        // commit changes
        editor.commit();
    }
    public void createLoginSessionControl(String name){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        editor.putInt(ACTICOINS, 20);
        editor.putBoolean(GOAL_SET, true);

        editor.putInt(WAGER, 10);

        editor.putBoolean(U_TYPE,false);
        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        Log.d(TAG,Boolean.toString(this.isLoggedIn()));
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }



    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        // return user
        return user;
    }

    public boolean getUserType(){
        // return goal
        return pref.getBoolean(U_TYPE,false);
    }
    public int getMVPA(){
        // return goal
        return pref.getInt(MVPA,60);
    }

    public int getDailyGoal(){
        // return goal
        return pref.getInt(DAILY_GOAL,60);
    }

    public int getWeeklyGoal(){
        // return goal
        return pref.getInt(WEEKLY_GOAL,240);
    }

    public int getActiCoins(){
        // return goal
        int n = pref.getInt(ACTICOINS,20);
        return n;
    }

    public int getWager(){
        // return goal
        int n = pref.getInt(WAGER,10);
        return n;
    }

    public boolean getGoalSet(){
        // return goalset boolean
        boolean bool = pref.getBoolean(GOAL_SET,false);
        return bool;
    }

    public void addActiCoins(int number){
        // add to current coins
        int n = pref.getInt(ACTICOINS,20);
        editor.putInt(ACTICOINS,n+number);
        editor.commit();
    }

    public void minusActiCoins(int number){
        // minus from current coins
        int n = pref.getInt(ACTICOINS,20);
        editor.putInt(ACTICOINS,n-number);
        editor.commit();
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    public void setMVPA(int mvpa){
        editor.putInt(MVPA,mvpa);
        editor.commit();
    }

    public void setDailyGoal(int goal){
        editor.putInt(DAILY_GOAL,goal);
        editor.commit();
    }
    public void setWager(int wager){
        editor.putInt(WAGER,wager);
        editor.commit();
    }

    public void setGoalSet(boolean bool){
        editor.putBoolean(GOAL_SET,bool);
        editor.commit();
    }

    public void setWeeklyGoal(int goal){
        editor.putInt(WEEKLY_GOAL,goal);
        editor.commit();
    }
    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }


}

