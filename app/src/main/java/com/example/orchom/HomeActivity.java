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
        binding.newGameCard.setOnClickListener(v -> {
            startActivity(new Intent(this, SetupActivity.class));
        });

        binding.historyCard.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });

        binding.resumeCard.setOnClickListener(v -> {
            startActivity(new Intent(this, GameActivity.class));
        });
    }

    private void updateResumeVisibility() {
        GameManager gameManager = GameManager.getInstance();
        gameManager.restoreGame(getApplicationContext());

        if (gameManager.isGameActive()) {
            binding.resumeCard.setVisibility(View.VISIBLE);
            binding.resumeCard.setAlpha(0f);
            binding.resumeCard.animate().alpha(1f).setDuration(300).start();
        } else {
            binding.resumeCard.setVisibility(View.GONE);
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
