package mher.minasyan.lexplain;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences prefs;
    private final CopyOnWriteArrayList<Runnable> contractRefreshCallbacks = new CopyOnWriteArrayList<>();

    private final SharedPreferences.OnSharedPreferenceChangeListener contractsPreferenceListener =
            (sharedPreferences, key) -> {
                if (ContractStorage.KEY_CONTRACTS.equals(key)) {
                    runOnUiThread(this::notifyContractViewsChanged);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySavedLanguage();
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        prefs = getSharedPreferences(ContractStorage.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(contractsPreferenceListener);

        if (viewPager != null && bottomNavigationView != null) {
            setupNavigation();
        }
    }

    @Override
    protected void onDestroy() {
        if (prefs != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(contractsPreferenceListener);
        }
        super.onDestroy();
    }

    public void registerContractRefreshCallback(Runnable callback) {
        if (callback != null) {
            contractRefreshCallbacks.add(callback);
        }
    }

    public void unregisterContractRefreshCallback(Runnable callback) {
        contractRefreshCallbacks.remove(callback);
    }

    public void notifyContractViewsChanged() {
        for (Runnable callback : contractRefreshCallbacks) {
            callback.run();
        }
    }

    private void setupNavigation() {
        MainPagerAdapter adapter = new MainPagerAdapter(this);
        viewPager.setAdapter(adapter);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewPager.getCurrentItem() != 0) {
                    viewPager.setCurrentItem(0, true);
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                viewPager.setCurrentItem(0, true);
            } else if (id == R.id.nav_history) {
                viewPager.setCurrentItem(1, true);
            } else if (id == R.id.nav_settings) {
                viewPager.setCurrentItem(2, true);
            }
            return true;
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (bottomNavigationView.getMenu().size() > position) {
                    bottomNavigationView.getMenu().getItem(position).setChecked(true);
                }
            }
        });
    }

    private void applySavedLanguage() {
        SharedPreferences preferences = getSharedPreferences("lexplain_prefs", Context.MODE_PRIVATE);

        String savedCode = preferences.getString("app_lang", null);

        if (savedCode != null) {
            AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(savedCode)
            );
        }
    }
}