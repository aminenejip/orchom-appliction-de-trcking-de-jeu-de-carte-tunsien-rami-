package com.example.orchom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.orchom.databinding.ContentSetupBinding;
import java.util.HashSet;
import java.util.Set;

public class SetupActivity extends BaseActivity {

    private ContentSetupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ContentSetupBinding.inflate(getLayoutInflater(), baseBinding.container, true);

        if (getIntent().hasExtra("game_config")) {
            prefillFromConfig((GameConfig) getIntent().getSerializableExtra("game_config"));
        } else {
            loadSavedSettings();
        }

        setupListeners();
    }

    @Override
    protected String getActivityTitle() {
        return "Nouvelle Partie";
    }

    private void setupListeners() {
        binding.playerCountGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isFour = checkedId == R.id.fourPlayers;
            binding.player3InputLayout.setVisibility(isFour ? View.VISIBLE : View.GONE);
            binding.player4InputLayout.setVisibility(isFour ? View.VISIBLE : View.GONE);
        });

        binding.startGameButton.setOnClickListener(v -> {
            if (validateInputs()) {
                GameConfig config = createGameConfig();
                saveSettings(config);
                startGameActivity(config);
            }
        });
    }

    private void prefillFromConfig(GameConfig config) {
        if (config == null) return;
        binding.gameNameInput.setText(config.gameName);
        binding.finalScoreInput.setText(String.valueOf(config.targetScore));
        binding.player1NameInput.setText(config.playerNames.length > 0 ? config.playerNames[0] : "");
        binding.player2NameInput.setText(config.playerNames.length > 1 ? config.playerNames[1] : "");
        
        if (config.playerCount == 4) {
            binding.playerCountGroup.check(R.id.fourPlayers);
            binding.player3NameInput.setText(config.playerNames.length > 2 ? config.playerNames[2] : "");
            binding.player4NameInput.setText(config.playerNames.length > 3 ? config.playerNames[3] : "");
        }
        
        binding.ghaltaValueGroup.check(config.ghaltaValue == 100 ? R.id.ghalta100 : R.id.ghalta50);
    }

    private void loadSavedSettings() {
        SharedPreferences prefs = getSharedPreferences("RamiPrefs", MODE_PRIVATE);
        binding.gameNameInput.setText(prefs.getString("gameName", ""));
        binding.finalScoreInput.setText(String.valueOf(prefs.getInt("targetScore", 500)));
        binding.player1NameInput.setText(prefs.getString("p1", ""));
        binding.player2NameInput.setText(prefs.getString("p2", ""));
        binding.player3NameInput.setText(prefs.getString("p3", ""));
        binding.player4NameInput.setText(prefs.getString("p4", ""));

        int count = prefs.getInt("count", 2);
        binding.playerCountGroup.check(count == 4 ? R.id.fourPlayers : R.id.twoPlayers);
        
        int ghaltaVal = prefs.getInt("ghaltaValue", 50);
        binding.ghaltaValueGroup.check(ghaltaVal == 100 ? R.id.ghalta100 : R.id.ghalta50);
    }

    private void saveSettings(GameConfig config) {
        SharedPreferences.Editor editor = getSharedPreferences("RamiPrefs", MODE_PRIVATE).edit();
        editor.putString("gameName", config.gameName);
        editor.putInt("targetScore", config.targetScore);
        editor.putInt("count", config.playerCount);
        editor.putString("p1", config.playerNames[0]);
        editor.putString("p2", config.playerNames[1]);
        if (config.playerCount == 4) {
            editor.putString("p3", config.playerNames[2]);
            editor.putString("p4", config.playerNames[3]);
        }
        editor.putInt("ghaltaValue", config.ghaltaValue);
        editor.apply();
    }

    private boolean validateInputs() {
        String scoreStr = binding.finalScoreInput.getText().toString().trim();
        try {
            int score = Integer.parseInt(scoreStr);
            if (score <= 0) {
                binding.finalScoreInput.setError("Le score doit être supérieur à 0");
                return false;
            }
            if (score > 10000) {
                binding.finalScoreInput.setError("Le score maximum est 10000");
                return false;
            }
        } catch (NumberFormatException e) {
            binding.finalScoreInput.setError("Veuillez entrer un nombre valide");
            return false;
        }

        int count = binding.playerCountGroup.getCheckedRadioButtonId() == R.id.fourPlayers ? 4 : 2;
        Set<String> names = new HashSet<>();
        com.google.android.material.textfield.TextInputEditText[] fields = {
            binding.player1NameInput, binding.player2NameInput, 
            binding.player3NameInput, binding.player4NameInput
        };

        for (int i = 0; i < count; i++) {
            String name = fields[i].getText().toString().trim();
            if (name.isEmpty()) {
                fields[i].setError("Nom requis");
                return false;
            }
            if (!names.add(name.toLowerCase())) {
                fields[i].setError("Doublon !");
                return false;
            }
        }
        return true;
    }

    private GameConfig createGameConfig() {
        int count = binding.playerCountGroup.getCheckedRadioButtonId() == R.id.fourPlayers ? 4 : 2;
        String gameName = binding.gameNameInput.getText().toString().trim();
        if (gameName.isEmpty()) gameName = "Partie sans nom";

        String[] names = new String[count];
        names[0] = binding.player1NameInput.getText().toString().trim();
        names[1] = binding.player2NameInput.getText().toString().trim();
        if (count == 4) {
            names[2] = binding.player3NameInput.getText().toString().trim();
            names[3] = binding.player4NameInput.getText().toString().trim();
        }

        int targetScore = 500;
        try {
            targetScore = Integer.parseInt(binding.finalScoreInput.getText().toString());
        } catch(Exception ignored){}

        int ghaltaVal = binding.ghaltaValueGroup.getCheckedRadioButtonId() == R.id.ghalta100 ? 100 : 50;

        return new GameConfig(gameName, count, targetScore, names, "ROTATION", 0, ghaltaVal, 10, 20, 100);
    }

    private void startGameActivity(GameConfig config) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("game_config", config);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}