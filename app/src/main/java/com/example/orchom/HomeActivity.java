package com.example.orchom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.orchom.databinding.ContentHomeBinding;

public class HomeActivity extends BaseActivity {

    private ContentHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ContentHomeBinding.inflate(getLayoutInflater(), baseBinding.container, true);

        setupClickListeners();
        updateResumeVisibility();
    }

    @Override
    protected String getActivityTitle() {
        return "Orchom - Rami Tunisien";
    }

    private void setupClickListeners() {
        binding.newGameButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SetupActivity.class));
        });

        binding.historyButton.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });

        binding.resumeButton.setOnClickListener(v -> {
            startActivity(new Intent(this, GameActivity.class));
        });
    }

    private void updateResumeVisibility() {
        GameManager gameManager = GameManager.getInstance();
        gameManager.restoreGame(getApplicationContext());

        if (gameManager.isGameActive()) {
            binding.resumeButton.setVisibility(View.VISIBLE);
            binding.resumeButton.setAlpha(0f);
            binding.resumeButton.animate().alpha(1f).setDuration(300).start();
        } else {
            binding.resumeButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateResumeVisibility();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
