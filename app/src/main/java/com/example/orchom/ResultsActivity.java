package com.example.orchom;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.example.orchom.databinding.ContentResultsBinding;
import com.example.orchom.databinding.ScoreItemLayoutBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultsActivity extends BaseActivity {

    private ContentResultsBinding binding;
    private List<Player> players;
    private String savedImagePath = null;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                savedImagePath = saveImageLocally(uri);
                if (savedImagePath != null) {
                    updateImagePreview();
                    saveResults();
                }
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ContentResultsBinding.inflate(getLayoutInflater(), baseBinding.container, true);

        players = GameManager.getInstance().getPlayers();
        if (players != null) {
            players = new ArrayList<>(players);
            Collections.sort(players, (p1, p2) -> Integer.compare(p1.getScore(), p2.getScore()));
            displayResults();
        }

        setupListeners();
        saveResults();
    }

    @Override
    protected String getActivityTitle() {
        return "Résultats";
    }

    private void setupListeners() {
        binding.newGameButton.setOnClickListener(v -> {
            saveResults();
            GameManager.getInstance().clearCurrentGame(this);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        binding.deleteGameButton.setOnClickListener(v -> showDeleteDialog());

        binding.uploadImageButton.setOnClickListener(v -> galleryLauncher.launch("image/*"));
    }

    private void displayResults() {
        // The 'players' list is already initialized and sorted in onCreate
        // if (players == null || players.isEmpty()) return; // This check is handled by the 'if (players != null)' in onCreate

        Player winner = players.get(0);
        binding.winnerName.setText(winner.getName());
        binding.winnerScore.setText(winner.getScore() + " points");

        binding.finalScoresContainer.removeAllViews();
        String[] rankIcons = {"🥇", "🥈", "🥉", "🏅"};

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            ScoreItemLayoutBinding itemBinding = ScoreItemLayoutBinding.inflate(
                getLayoutInflater(), binding.finalScoresContainer, true);

            String rankText = (i < rankIcons.length) ? rankIcons[i] : String.valueOf(i + 1);
            itemBinding.positionText.setText(rankText);
            itemBinding.positionText.setBackground(null); // Remove circle background for emojis
            itemBinding.playerName.setText(p.getName());
            itemBinding.playerScore.setText(p.getScore() + " pts");

            try {
                itemBinding.scoreIndicator.setBackgroundColor(Color.parseColor(p.getColor()));
            } catch (Exception e) {
                itemBinding.scoreIndicator.setBackgroundColor(Color.parseColor("#E11D48"));
            }
        }
    }

    private void saveResults() {
        GameManager.getInstance().saveToHistory(this, savedImagePath);
    }

    private void updateImagePreview() {
        if (savedImagePath != null) {
            binding.resultImagePreview.setImageURI(Uri.fromFile(new File(savedImagePath)));
            binding.resultImagePreview.setVisibility(View.VISIBLE);
            binding.uploadImageButton.setText("📷 CHANGER LA PHOTO");
        }
    }

    private String saveImageLocally(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File outputDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            File outputFile = new File(outputDir, "orchom_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private void showDeleteDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Supprimer ?")
            .setMessage("Supprimer cette partie de l'historique ?")
            .setPositiveButton("Supprimer", (dialog, which) -> {
                GameManager.getInstance().deleteLastHistoryEntry(this);
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            })
            .setNegativeButton("Annuler", null)
            .show();
    }

    @Override
    public void onBackPressed() {
        saveResults();
        GameManager.getInstance().clearCurrentGame(this);
        // Important: Use the modern way for back press if needed, 
        // but for now calling super is required to avoid the warning and handle lifecycle.
        super.onBackPressed();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}