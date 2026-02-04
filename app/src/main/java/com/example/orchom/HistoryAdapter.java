package com.example.orchom;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Date;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<JSONObject> historyList;
    private Context context;
    private boolean isReadOnly = false;

    public HistoryAdapter(Context context, List<JSONObject> historyList, boolean isReadOnly) {
        this.context = context;
        this.historyList = historyList;
        this.isReadOnly = isReadOnly;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject entry = historyList.get(position);
        
        String winner = entry.optString("winner", "Inconnu");
        String gameName = entry.optString("game_name", "Partie sans nom");
        int winnerScore = entry.optInt("winner_score", 0);
        long timestamp = entry.optLong("date", 0);
        String imagePath = entry.optString("image_path", null);
        JSONArray playersArr = entry.optJSONArray("players");
        
        holder.winnerNameText.setText("🏆 " + winner + " (" + winnerScore + " pts)");
        holder.gameNameText.setText(gameName);
        
        if (timestamp > 0) {
            String dateStr = DateFormat.format("dd MMM yyyy, HH:mm", new Date(timestamp)).toString();
            holder.dateText.setText(dateStr);
        } else {
            holder.dateText.setText("");
        }

        // Image loading optimization
        if (imagePath != null && !imagePath.isEmpty()) {
            java.io.File file = new java.io.File(imagePath);
            if (file.exists()) {
                // Optimized loading
                android.graphics.Bitmap bitmap = decodeSampledBitmapFromFile(imagePath, 300, 300);
                if (bitmap != null) {
                    holder.historyGameImage.setImageBitmap(bitmap);
                    holder.historyGameImage.setVisibility(View.VISIBLE);
                } else {
                    holder.historyGameImage.setVisibility(View.GONE);
                }
            } else {
                holder.historyGameImage.setVisibility(View.GONE);
            }
        } else {
            holder.historyGameImage.setVisibility(View.GONE);
        }
 
        if (isReadOnly) {
            holder.replayActionText.setVisibility(View.GONE);
        } else {
            holder.replayActionText.setVisibility(View.GONE); // Always gone as per request
        }
 
        if (playersArr != null) {
            StringBuilder sb = new StringBuilder("Joueurs: ");
            StringBuilder roundDetails = new StringBuilder("Par Round: ");
            Player loser = null;
            int maxScore = -1;
 
            for (int i = 0; i < playersArr.length(); i++) {
                JSONObject pJson = playersArr.optJSONObject(i);
                if (pJson != null) {
                    String name = pJson.optString("name");
                    int score = pJson.optInt("score");
                    sb.append(name).append(" (").append(score).append(")");
                    
                    // Round scores
                    JSONArray rArr = pJson.optJSONArray("roundScores");
                    if (rArr != null) {
                        roundDetails.append(name).append(": ").append(rArr.toString());
                        if (i < playersArr.length() - 1) roundDetails.append(" | ");
                        holder.roundDetailsText.setVisibility(View.VISIBLE);
                    }

                    if (i < playersArr.length() - 1) sb.append(", ");
                    
                    if (score > maxScore) {
                        maxScore = score;
                        loser = Player.fromJSON(pJson);
                    }
                }
            }
            holder.playersSummaryText.setText(sb.toString());
            holder.roundDetailsText.setText(roundDetails.toString());
            
            if (loser != null) {
                holder.loserText.setText("🔻 Perdant: " + loser.getName() + " (" + loser.getScore() + " pts)");
                holder.loserText.setVisibility(View.VISIBLE);
            } else {
                holder.loserText.setVisibility(View.GONE);
            }
        } else {
            holder.playersSummaryText.setText("Détails indisponibles");
            holder.loserText.setVisibility(View.GONE);
            holder.roundDetailsText.setVisibility(View.GONE);
        }
    }
 
 
    private android.graphics.Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        android.graphics.BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return android.graphics.BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(android.graphics.BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
    
    // ... (rest of the file)
 
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView winnerNameText, gameNameText, dateText, playersSummaryText, loserText, replayActionText, roundDetailsText;
        android.widget.ImageView historyGameImage;
 
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            winnerNameText = itemView.findViewById(R.id.winnerNameText);
            gameNameText = itemView.findViewById(R.id.gameNameText);
            dateText = itemView.findViewById(R.id.dateText);
            playersSummaryText = itemView.findViewById(R.id.playersSummaryText);
            loserText = itemView.findViewById(R.id.loserText);
            replayActionText = itemView.findViewById(R.id.replayActionText);
            historyGameImage = itemView.findViewById(R.id.historyGameImage);
            roundDetailsText = itemView.findViewById(R.id.roundDetailsText);
        }
    }
}
