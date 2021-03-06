package com.example.android.baking;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.android.baking.data.Ingredient;
import com.example.android.baking.data.Recipe;
import com.example.android.baking.fragments.SelectStepFragment;
import com.example.android.baking.fragments.ViewStepFragment;
import com.example.android.baking.services.UpdateWidgetService;
import com.google.gson.Gson;

import timber.log.Timber;

public class StepsActivity extends AppCompatActivity implements
        SelectStepFragment.OnStepClickListener {

    private boolean twoPane;

    private FragmentManager mFragmentManager;
    private SelectStepFragment mSelectStepFragment;
    private ViewStepFragment mViewStepFragment;

    private Recipe mRecipe;

    public static final String SELECT_STEP_FRAGMENT_KEY = "step_fragment";
    public static final String VIEW_STEP_FRAGMENT_KEY = "view_step_fragment";
    public static final String INGREDIENTS_KEY = "ingredients";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);

        // Restore the recipe and the fragments
        Fragment restoredFrag = null;
        if (savedInstanceState!=null){
            mRecipe = savedInstanceState.getParcelable(RecipeActivity.RECIPE_KEY);
            mSelectStepFragment = (SelectStepFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, SELECT_STEP_FRAGMENT_KEY);
            mViewStepFragment = (ViewStepFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, VIEW_STEP_FRAGMENT_KEY);
            if (mSelectStepFragment == null)
                restoredFrag = mViewStepFragment;
            else
                restoredFrag = mSelectStepFragment;
        }

        // Set tablet vs phone
        if(getResources().getBoolean(R.bool.is_600_wide)){
            twoPane = true;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Timber.d("Setting two pane to true");
        }
        // Is not tablet
        else {
            twoPane = false;
            Timber.d("Setting two pane to false");
        }

        // Create fragments
        mFragmentManager = getSupportFragmentManager();
        if (mSelectStepFragment == null) mSelectStepFragment = new SelectStepFragment();
        if (mViewStepFragment == null) mViewStepFragment = new ViewStepFragment();

        // Get the recipe from the intent
        if (getIntent().hasExtra(RecipeActivity.RECIPE_KEY)){
            mRecipe = getIntent().getParcelableExtra(RecipeActivity.RECIPE_KEY);
            mSelectStepFragment.setRecipe(mRecipe);
            mViewStepFragment.setSteps(mRecipe.getSteps());
            mViewStepFragment.setStepPosition(0); // initialize step
        }

        // Set the title of the recipe
        setTitle(mRecipe.getName());

        // Handle set up for one pane or two pane layout
        Timber.d("Two pane is: %s", twoPane);
        if (twoPane){
            mFragmentManager.beginTransaction()
                    .replace(R.id.steps_two_pane_left, mSelectStepFragment)
                    .commit();
            mFragmentManager.beginTransaction()
                    .replace(R.id.steps_two_pane_right, mViewStepFragment)
                    .commit();
        }
        else {
            if (restoredFrag==null) {
                mFragmentManager.beginTransaction()
                        .replace(R.id.steps_single_pane_container, mSelectStepFragment)
                        .commit();
            } else {
                mFragmentManager.beginTransaction()
                        .replace(R.id.steps_single_pane_container, restoredFrag)
                        .commit();
            }
        }

        // Set the up button for navigation back to parent activity
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) actionBar.setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public void onStepSelected(int position) {
        // Send the selected position to the fragment
        mViewStepFragment.setStepPosition(position);

        // Swap out the fragment depending upon id
        if (twoPane) {
            mViewStepFragment.updateUI();
        } else {
            mFragmentManager.beginTransaction()
                    .replace(R.id.steps_single_pane_container, mViewStepFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the selected recipe
        outState.putParcelable(RecipeActivity.RECIPE_KEY, mRecipe);
        // Save the state of fragments
        if (twoPane) {
            getSupportFragmentManager()
                    .putFragment(outState, SELECT_STEP_FRAGMENT_KEY, mSelectStepFragment);
            getSupportFragmentManager()
                    .putFragment(outState, VIEW_STEP_FRAGMENT_KEY, mViewStepFragment);
        } else {
            Fragment currentFrag = getSupportFragmentManager().findFragmentById(R.id.steps_single_pane_container);
            if (currentFrag instanceof SelectStepFragment){
                getSupportFragmentManager()
                        .putFragment(outState, SELECT_STEP_FRAGMENT_KEY, currentFrag);
            } else if (currentFrag instanceof  ViewStepFragment){
                getSupportFragmentManager()
                        .putFragment(outState, VIEW_STEP_FRAGMENT_KEY, currentFrag);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.steps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ingredients_save_toggle:
                saveRecipe();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveRecipe(){
        // Save the ingredients in a shared pref
        SharedPreferences pref = this.getBaseContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        // Save the recipe as a JSON object to shared preferences
        Gson gson = new Gson();
        String recipeJson = gson.toJson(mRecipe);
        editor.putString(RecipeActivity.RECIPE_KEY, recipeJson);
        editor.apply();

        // Update the widgets
        UpdateWidgetService.startActionUpdateIngredientWidgets(this);
    }



}
