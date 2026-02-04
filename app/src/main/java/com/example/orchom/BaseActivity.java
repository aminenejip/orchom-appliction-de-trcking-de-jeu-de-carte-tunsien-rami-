package com.example.orchom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.orchom.databinding.ActivityBaseBinding;

public abstract class BaseActivity extends AppCompatActivity {

    protected ActivityBaseBinding baseBinding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseBinding = ActivityBaseBinding.inflate(getLayoutInflater());
        setContentView(baseBinding.getRoot());

        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(baseBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getActivityTitle());
        }
    }

    protected abstract String getActivityTitle();

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setActivityContent(@LayoutRes int layoutResID) {
        FrameLayout container = baseBinding.container;
        container.removeAllViews();
        LayoutInflater.from(this).inflate(layoutResID, container, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        baseBinding = null;
    }
}
