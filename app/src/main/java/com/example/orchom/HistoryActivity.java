package com.example.orchom;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.orchom.databinding.ContentHistoryBinding;
import org.json.JSONObject;
import java.util.List;

public class HistoryActivity extends BaseActivity {

    private ContentHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ContentHistoryBinding.inflate(getLayoutInflater(), baseBinding.container, true);

        loadHistory();
    }

    @Override
    protected String getActivityTitle() {
        return "Historique des parties";
    }

    private void loadHistory() {
        List<JSONObject> history = GameManager.getInstance().getHistory(this);
        
        if (history.isEmpty()) {
            binding.emptyStateText.setVisibility(View.VISIBLE);
            binding.historyRecyclerView.setVisibility(View.GONE);
            binding.tabLayout.setVisibility(View.GONE);
        } else {
            binding.emptyStateText.setVisibility(View.GONE);
            binding.historyRecyclerView.setVisibility(View.VISIBLE);
            binding.tabLayout.setVisibility(View.VISIBLE);
            
            binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            HistoryAdapter adapter = new HistoryAdapter(this, history, false);
            binding.historyRecyclerView.setAdapter(adapter);

            binding.tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    boolean isReadOnly = tab.getPosition() == 1;
                    binding.historyRecyclerView.setAdapter(new HistoryAdapter(HistoryActivity.this, history, isReadOnly));
                }
                @Override
                public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
