package mher.minasyan.lexplain;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import mher.minasyan.lexplain.BuildConfig;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class WiewContract extends AppCompatActivity {
    public static Contract currentContract = null;
    private static final String BASE_URL = "https://router.huggingface.co/";
    private static final String HF_TOKEN = "";
    private boolean isVipActive = false;

    public Contract getContractFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            Object data = intent.getSerializableExtra("CONTRACT_DATA");
            if (data instanceof Contract) {
                return (Contract) data;
            }
        }
        return currentContract;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiew_contract);
        TextView tvName = findViewById(R.id.tvDetailFileName);
        TextView tvPercent = findViewById(R.id.tvRiskPercent);
        ProgressBar progressBar = findViewById(R.id.riskProgressBar);
        RecyclerView rvRisks = findViewById(R.id.rvRiskyMoments);
        TextView tvEmpty = findViewById(R.id.tvEmptyMessage);
        ProgressBar loader = findViewById(R.id.loader);

        Contract contract = getContractFromIntent();
        currentContract = contract;
        if (contract == null) {
            finish();
            return;
        }
        saveContractToPreferences();
        checkUserRole();
        tvName.setText(contract.getFileName());
        tvPercent.setText(contract.getRiskLevel());
        String level = contract.getRiskLevel();
        if (level != null) {
            String normalizedLevel = level.toLowerCase();
            if (normalizedLevel.contains("high") || normalizedLevel.contains("высок")) {
                progressBar.setProgress(90);
            } else if (normalizedLevel.contains("medium") || normalizedLevel.contains("сред")) {
                progressBar.setProgress(50);
            } else progressBar.setProgress(15);
        }
        List<String> filteredSentences = new ArrayList<>();
        List<String> filteredRisks = new ArrayList<>();
        List<String> parts = contract.getcontracttextparts();
        List<String> risks = contract.getcontracttextrisks();
        if (parts != null && risks != null) {
            int size = Math.min(parts.size(), risks.size());
            for (int i = 0; i < size; i++) {
                String r = risks.get(i);
                if (r == null) continue;
                String normalizedRisk = r.toLowerCase();
                if (normalizedRisk.contains("high")
                        || normalizedRisk.contains("medium")
                        || normalizedRisk.contains("высок")
                        || normalizedRisk.contains("сред")) {
                    filteredSentences.add(parts.get(i));
                    filteredRisks.add(r);
                }
            }
        }
        if (filteredSentences.isEmpty()) {
            rvRisks.setVisibility(View.GONE);
        } else {
            rvRisks.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);

            RiskyAdapter adapter = new RiskyAdapter(filteredSentences, filteredRisks);
            rvRisks.setLayoutManager(new LinearLayoutManager(this));
            rvRisks.setAdapter(adapter);
        }
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finish();
                    }
                });
        findViewById(R.id.btnShowFull).setOnClickListener(v -> {
            Intent intent = new Intent(this, FullTextActivity.class);
            intent.putExtra("CONTRACT_DATA", currentContract);
            startActivity(intent);
        });
        findViewById(R.id.btnShowConspect).setOnClickListener(v -> {
            if (!isVipActive) {
                showVipUpgradeDialog();
                return;
            }
            if (currentContract.getConspect() != null && !currentContract.getConspect().isEmpty()) {
                Log.d("hf", "Конспект уже есть в памяти, открываю экран...");
                Intent intent = new Intent(WiewContract.this, ConspectWiew.class);
                intent.putExtra("CONTRACT_DATA", currentContract);
                startActivity(intent);
                return;
            }

            loader.setVisibility(View.VISIBLE);
            v.setEnabled(false);
            String fulltext = String.join(" ", currentContract.getcontracttextparts());
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            HuggingFaceApi api = retrofit.create(HuggingFaceApi.class);
            String prompt = "дай конспект этого контракта простым языком без терминологии, чтобы обычный человек понял. " +
                    "Ответ дай только на том языке на чем написан контракт. Текст:\n\n" + fulltext;
            HuggingFaceRequest request = new HuggingFaceRequest(prompt);
            api.summarize(HF_TOKEN, request).enqueue(new Callback<HuggingFaceResponse>() {
                @Override
                public void onResponse(Call<HuggingFaceResponse> call, Response<HuggingFaceResponse> response) {
                    loader.setVisibility(View.GONE);
                    v.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        String result = response.body().choices.get(0).message.content;

                        Log.d("hf", "Результат: " + result);
                        currentContract.setConspect(result);
                        saveContractToPreferences();

                        Intent intent = new Intent(WiewContract.this, ConspectWiew.class);
                        intent.putExtra("CONTRACT_DATA", currentContract);
                        startActivity(intent);
                    } else {
                        try {
                            String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Log.e("hf", "Ошибка API " + response.code() + ": " + errorMsg);
                            Toast.makeText(WiewContract.this, "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onFailure(Call<HuggingFaceResponse> call, Throwable t) {
                    loader.setVisibility(View.GONE);
                    v.setEnabled(true);
                    Log.e("hf", "Сетевая ошибка (возможно таймаут): " + t.getMessage());
                    Toast.makeText(WiewContract.this, "Ошибка сети или таймаут", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void saveContractToPreferences() {
        if (currentContract == null) return;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            currentContract.setUserId(currentUser.getUid());
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            if (currentContract.getDocumentId() == null || currentContract.getDocumentId().isEmpty()) {
                db.collection("contracts")
                        .add(currentContract)
                        .addOnSuccessListener(documentReference -> {
                            String generatedId = documentReference.getId();
                            currentContract.setDocumentId(generatedId);
                            db.collection("contracts").document(generatedId)
                                    .update("documentId", generatedId);
                            writeContractToLocalPrefs();
                            Log.d("Firestore", "Контракт успешно залит в БД с ID: " + generatedId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Ошибка сохранения в облако, пишем только локально", e);
                            writeContractToLocalPrefs();
                        });
            } else {
                db.collection("contracts").document(currentContract.getDocumentId())
                        .set(currentContract)
                        .addOnSuccessListener(aVoid -> {
                            writeContractToLocalPrefs();
                            Log.d("Firestore", "Контракт в БД успешно обновлен");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Ошибка обновления в БД", e);
                            writeContractToLocalPrefs();
                        });
            }
        } else {
            writeContractToLocalPrefs();
        }
    }

    private void writeContractToLocalPrefs() {
        SharedPreferences sp = getSharedPreferences("lexplain_prefs", Context.MODE_PRIVATE);
        String json = sp.getString("contracts", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Contract>>() {}.getType();
        List<Contract> contractList = gson.fromJson(json, type);
        if (contractList == null) {
            contractList = new ArrayList<>();
        }
        boolean isFound = false;
        for (int i = 0; i < contractList.size(); i++) {
            if (contractList.get(i).getFileName().equals(currentContract.getFileName())) {
                contractList.set(i, currentContract);
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            contractList.add(currentContract);
        }
        sp.edit()
                .putString("contracts", gson.toJson(contractList))
                .apply();
        Log.d("LocalPrefs", "Контракт успешно сохранен в память устройства");
    }

    private void checkUserRole() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            isVipActive = false;
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("vipUntil")) {
                        Long vipUntil = documentSnapshot.getLong("vipUntil");
                        if (vipUntil != null) {
                            // Если время окончания подписки больше текущего времени — подписка активна!
                            isVipActive = vipUntil > System.currentTimeMillis();
                            Log.d("VIP_Check", "VIP активен: " + isVipActive + " (До: " + vipUntil + ")");
                            return;
                        }
                    }
                    isVipActive = false;
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Ошибка проверки роли", e);
                    isVipActive = false;
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            checkUserRole();
        }
    }

    private void createNewUserDocument(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        java.util.Map<String, Object> userMap = new java.util.HashMap<>();
        userMap.put("role", "standard");

        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Создан новый профиль пользователя"))
                .addOnFailureListener(e -> Log.e("Firestore", "Не удалось создать профиль", e));
    }

    private void showVipUpgradeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Доступно только в VIP-версии")
                .setMessage("Генерация краткого конспекта контракта с помощью ИИ доступна только VIP-пользователям. Хотите приобрести подписку?")
                .setPositiveButton("Купить VIP", (dialog, which) -> {
                    Intent intent = new Intent(this, PayActivity.class);
                    startActivityForResult(intent, 1001); // 1001 — код запроса
                })
                .setNegativeButton("Назад", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}