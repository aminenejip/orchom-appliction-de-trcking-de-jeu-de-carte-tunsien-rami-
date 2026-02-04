package com.example.orchom;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.example.orchom.databinding.ContentGameBinding;
import com.example.orchom.databinding.RoundScoreInputItemBinding;
import com.example.orchom.databinding.PlayerCardLayoutBinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends BaseActivity {

    private ContentGameBinding binding;
    private GameManager gameManager;
    private final List<RoundScoreInputItemBinding> inputBindings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ContentGameBinding.inflate(getLayoutInflater(), baseBinding.container, true);
        gameManager = GameManager.getInstance();

        handleGameState();
        setupListeners();
        refreshUI();
    }

    @Override
    protected String getActivityTitle() {
        return "Partie en cours";
    }

    private void handleGameState() {
        if (getIntent().hasExtra("game_config")) {
            GameConfig config = (GameConfig) getIntent().getSerializableExtra("game_config");
            if (config != null) {
                gameManager.startNewGame(config);
                gameManager.saveGame(this);
            } else {
                Toast.makeText(this, "Configuration invalide", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            if (!gameManager.isGameActive()) {
                gameManager.restoreGame(this);
            }
            if (!gameManager.isGameActive()) {
                Toast.makeText(this, "Aucune partie active trouvée", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setupListeners() {
        if (binding == null) return;
        
        binding.validateRoundButton.setOnClickListener(v -> validateRound());
        binding.endGameButton.setOnClickListener(v -> showEndGameDialog());
        setupRoundInputs();
    }

    private void setupRoundInputs() {
        if (binding == null) return;
        
        List<Player> players = gameManager.getPlayers();
        if (players == null || players.isEmpty()) return;

        binding.roundInputsContainer.removeAllViews();
        inputBindings.clear();

        GameConfig config = gameManager.getCurrentConfig();
        int ghaltaVal = (config != null) ? config.ghaltaValue : 50;

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            RoundScoreInputItemBinding itemBinding = RoundScoreInputItemBinding.inflate(
                getLayoutInflater(), binding.roundInputsContainer, true);
            
            itemBinding.playerNameText.setText(p.getName());
            itemBinding.currentScoreText.setText(p.getScore() + " pts");
            
            itemBinding.btnGhalta.setText("+" + ghaltaVal);
            itemBinding.btnGhalta.setOnClickListener(v -> {
                String current = itemBinding.scoreInput.getText().toString();
                int score = current.isEmpty() ? 0 : Integer.parseInt(current);
                itemBinding.scoreInput.setText(String.valueOf(score + ghaltaVal));
            });

            // Boutons de raccourcis rapides pour le gagnant (clash auto)
            itemBinding.btnWin10.setOnClickListener(v -> itemBinding.scoreInput.setText("-10"));
            itemBinding.btnWin20.setOnClickListener(v -> itemBinding.scoreInput.setText("-20"));
            itemBinding.btnWinRalta.setOnClickListener(v -> itemBinding.scoreInput.setText("-100"));

            // Note: On peut garder ces boutons visibles pour tous, ou les gérer dynamiquement.
            // Pour l'instant on les laisse accessibles comme raccourcis efficaces.
            itemBinding.btnWin10.setVisibility(View.VISIBLE);
            itemBinding.btnWin20.setVisibility(View.VISIBLE);
            itemBinding.btnWinRalta.setVisibility(View.VISIBLE);

            inputBindings.add(itemBinding);
        }
    }

    private void validateRound() {
        List<Player> players = gameManager.getPlayers();
        if (players == null || players.isEmpty()) return;

        int[] scores = new int[players.size()];
        int minScore = Integer.MAX_VALUE;

        // Récupérer tous les scores et trouver le minimum
        for (int i = 0; i < players.size(); i++) {
            String val = inputBindings.get(i).scoreInput.getText().toString().trim();
            if (val.isEmpty()) {
                Toast.makeText(this, "Score manquant pour " + players.get(i).getName(), Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                scores[i] = Integer.parseInt(val);
                if (scores[i] < minScore) {
                    minScore = scores[i];
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Score invalide", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Ajouter les scores au GameManager
        for (int i = 0; i < players.size(); i++) {
            players.get(i).addRoundScore(scores[i]);
        }

        // Passer à la manche suivante
        gameManager.startNextRound();
        gameManager.saveGame(this);
        refreshUI();
        checkForGameOver();

        // Réinitialiser les champs de saisie
        for (RoundScoreInputItemBinding b : inputBindings) {
            b.scoreInput.setText("");
            // b.winnerCheckbox n'est plus utilisé manuellement mais on le reset par sécurité
            b.winnerCheckbox.setChecked(false);
        }
        
        Toast.makeText(this, "Manche validée ✅", Toast.LENGTH_SHORT).show();
    }

    private void refreshUI() {
        if (binding == null) return;
        
        GameConfig config = gameManager.getCurrentConfig();
        if (config == null) return;
        
        binding.roundDisplay.setText("MANCHE " + gameManager.getCurrentRound());
        binding.finalScoreDisplay.setText("Objectif : +" + config.targetScore + " pts");
        displayLeaderboard();
    }

    private void displayLeaderboard() {
        if (binding == null) return;
        
        binding.playersContainer.removeAllViews();
        List<Player> players = new ArrayList<>(gameManager.getPlayers());
        
        // Trier par score croissant (le plus bas en premier = meilleur)
        Collections.sort(players, (p1, p2) -> Integer.compare(p1.getScore(), p2.getScore()));

        GameConfig config = gameManager.getCurrentConfig();
        int targetScore = config != null ? config.targetScore : 500;
        int dangerThreshold = (int)(targetScore * 0.8);

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            PlayerCardLayoutBinding cardBinding = PlayerCardLayoutBinding.inflate(
                getLayoutInflater(), binding.playersContainer, true);
            
            cardBinding.rankPosition.setText(String.valueOf(i + 1));
            cardBinding.playerName.setText(p.getName());
            cardBinding.playerScore.setText(String.valueOf(p.getScore()));
            
            // Logic de couleur du score
            if (p.getScore() >= targetScore) {
                cardBinding.playerScore.setTextColor(Color.parseColor("#EF4444")); // Rouge Danger (Perdu)
                cardBinding.rankPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#0F172A")));
            } else if (p.getScore() > dangerThreshold) {
                cardBinding.playerScore.setTextColor(Color.parseColor("#F59E0B")); // Orange Warning
                cardBinding.rankPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E11D48")));
            } else {
                cardBinding.playerScore.setTextColor(Color.parseColor("#0F172A")); // Normal
                cardBinding.rankPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));
            }

            // Couleur de l'indicateur personnalisée du joueur
            try {
                cardBinding.colorIndicator.setBackgroundColor(Color.parseColor(p.getColor()));
            } catch (Exception e) {
                cardBinding.colorIndicator.setBackgroundColor(Color.parseColor("#E11D48"));
            }
        }
    }

    private void checkForGameOver() {
        GameConfig config = gameManager.getCurrentConfig();
        if (config == null) return;
        
        // Vérifier si un joueur a atteint ou dépassé le score limite (= a perdu)
        for (Player p : gameManager.getPlayers()) {
            if (p.getScore() >= config.targetScore) {
                showGameOverDialog(p);
                break;
            }
        }
    }

    private void showGameOverDialog(Player loser) {
        // Le gagnant est celui avec le score le plus BAS
        Player winner = getWinner();
        
        String winnerName = winner != null ? winner.getName() : "Inconnu";
        String message = loser.getName() + " a perdu avec " + loser.getScore() + " pts.\n" +
                        "Gagnant: " + winnerName + " avec " + 
                        (winner != null ? winner.getScore() : "N/A") + " pts";
        
        new AlertDialog.Builder(this)
                .setTitle("🎉 Partie Terminée")
                .setMessage(message)
                .setPositiveButton("Résultats", (d, w) -> endGame())
                .setCancelable(false)
                .show();
    }

    /**
     * Retourne le joueur avec le score le plus BAS (meilleur score)
     */
    private Player getWinner() {
        List<Player> players = gameManager.getPlayers();
        if (players == null || players.isEmpty()) return null;
        
        Player winner = players.get(0);
        for (Player p : players) {
            if (p.getScore() < winner.getScore()) {
                winner = p;
            }
        }
        return winner;
    }

    private void showEndGameDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Quitter la partie ?")
                .setMessage("Voulez-vous terminer la partie et voir les résultats ?")
                .setPositiveButton("Oui", (d, w) -> endGame())
                .setNegativeButton("Continuer", null)
                .show();
    }

    private void endGame() {
        startActivity(new Intent(this, ResultsActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}