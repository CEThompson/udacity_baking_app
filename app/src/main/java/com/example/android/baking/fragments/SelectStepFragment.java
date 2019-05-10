package com.example.android.baking.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.baking.R;
import com.example.android.baking.adapters.RecipeAdapter;
import com.example.android.baking.adapters.StepAdapter;
import com.example.android.baking.data.Recipe;
import com.example.android.baking.data.Step;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SelectStepFragment extends Fragment implements StepAdapter.StepOnClickHandler{

    @BindView(R.id.step_recyclerview) RecyclerView mStepRecyclerView;
    StepAdapter mStepAdapter;
    OnStepClickListener mCallback;

    private Recipe mRecipe;

    public interface OnStepClickListener {
        void onStepSelected(Step step);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnStepClickListener) context;
        } catch (Exception e){
            throw new ClassCastException(context.toString() + " must implement OnStepClickListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_step, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create and set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        mStepRecyclerView.setLayoutManager(layoutManager);
        mStepRecyclerView.setHasFixedSize(true);

        // Create the adapter
        mStepAdapter = new StepAdapter(this);

        // Set the adapter on the recycler view
        mStepRecyclerView.setAdapter(mStepAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        mStepAdapter.setmRecipeData(mRecipe.getSteps());
    }

    @Override
    public void onClick(Step clickedStep) {
        mCallback.onStepSelected(clickedStep);
        Timber.d("selecting step");
    }

    public void setRecipe(Recipe recipe){
        mRecipe = recipe;
    }
}
