package com.example.healthmonitor.ui.base;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private LinearLayout layoutNetworkStatus;
    private TextView tvNetworkStatus;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isCurrentlyConnected = true;

    @Override
    public void setContentView(int layoutResID) {
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        layoutNetworkStatus = new LinearLayout(this);
        layoutNetworkStatus.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        layoutNetworkStatus.setOrientation(LinearLayout.HORIZONTAL);
        layoutNetworkStatus.setGravity(Gravity.CENTER);
        int paddingPx = (int) (8 * getResources().getDisplayMetrics().density);
        layoutNetworkStatus.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        layoutNetworkStatus.setBackgroundColor(Color.parseColor("#D63031"));
        layoutNetworkStatus.setVisibility(View.GONE);

        tvNetworkStatus = new TextView(this);
        tvNetworkStatus.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tvNetworkStatus.setText("No internet connection");
        tvNetworkStatus.setTextColor(Color.WHITE);
        tvNetworkStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvNetworkStatus.setTypeface(null, Typeface.BOLD);

        layoutNetworkStatus.addView(tvNetworkStatus);
        rootLayout.addView(layoutNetworkStatus);

        View contentView = getLayoutInflater().inflate(layoutResID, rootLayout, false);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        rootLayout.addView(contentView, contentParams);

        super.setContentView(rootLayout);

        setupNetworkMonitoring();
    }

    @Override
    public void setContentView(View view) {
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        layoutNetworkStatus = new LinearLayout(this);
        layoutNetworkStatus.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        layoutNetworkStatus.setOrientation(LinearLayout.HORIZONTAL);
        layoutNetworkStatus.setGravity(Gravity.CENTER);
        int paddingPx = (int) (8 * getResources().getDisplayMetrics().density);
        layoutNetworkStatus.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        layoutNetworkStatus.setBackgroundColor(Color.parseColor("#D63031"));
        layoutNetworkStatus.setVisibility(View.GONE);

        tvNetworkStatus = new TextView(this);
        tvNetworkStatus.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tvNetworkStatus.setText("No internet connection");
        tvNetworkStatus.setTextColor(Color.WHITE);
        tvNetworkStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvNetworkStatus.setTypeface(null, Typeface.BOLD);

        layoutNetworkStatus.addView(tvNetworkStatus);
        rootLayout.addView(layoutNetworkStatus);

        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        rootLayout.addView(view, contentParams);

        super.setContentView(rootLayout);

        setupNetworkMonitoring();
    }

    private void setupNetworkMonitoring() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isConnected = isNetworkAvailable();
        isCurrentlyConnected = isConnected;
        if (!isConnected) {
            showOfflineBanner();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    mainHandler.post(() -> {
                        if (!isCurrentlyConnected) {
                            isCurrentlyConnected = true;
                            showOnlineBannerAndHide();
                        }
                    });
                }

                @Override
                public void onLost(@NonNull Network network) {
                    mainHandler.post(() -> {
                        isCurrentlyConnected = false;
                        showOfflineBanner();
                    });
                }
            };
            try {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
            } catch (Exception ignored) {}
        }
    }

    private boolean isNetworkAvailable() {
        if (connectivityManager == null) return true;
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private void showOfflineBanner() {
        if (layoutNetworkStatus != null && tvNetworkStatus != null) {
            layoutNetworkStatus.setBackgroundColor(Color.parseColor("#D63031"));
            tvNetworkStatus.setText("No internet connection");
            layoutNetworkStatus.setVisibility(View.VISIBLE);
        }
    }

    private void showOnlineBannerAndHide() {
        if (layoutNetworkStatus != null && tvNetworkStatus != null) {
            layoutNetworkStatus.setBackgroundColor(Color.parseColor("#27AE60"));
            tvNetworkStatus.setText("Internet connection restored");
            layoutNetworkStatus.setVisibility(View.VISIBLE);

            mainHandler.postDelayed(() -> {
                if (layoutNetworkStatus != null && isCurrentlyConnected) {
                    layoutNetworkStatus.setVisibility(View.GONE);
                }
            }, 2500);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception ignored) {}
        }
    }
}
