package mher.minasyan.lexplain;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        findViewById(R.id.btnBuyTest).setOnClickListener(v -> purchaseVip(60 * 1000L));
        findViewById(R.id.btnBuyMonth).setOnClickListener(v -> purchaseVip(30 * 24 * 60 * 60 * 1000L));
        findViewById(R.id.btnBuyYear).setOnClickListener(v -> purchaseVip(365 * 24 * 60 * 60 * 1000L));
    }

    private void purchaseVip(long durationMs) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Пожалуйста, авторизуйтесь!", Toast.LENGTH_SHORT).show();
            return;
        }
        long vipUntilTimestamp = System.currentTimeMillis() + durationMs;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("vipUntil", vipUntilTimestamp);
        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PayActivity.this, "Покупка успешна! VIP активирован.", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    db.collection("users").document(currentUser.getUid())
                            .set(updates)
                            .addOnSuccessListener(aVoid -> {
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(err -> Log.e("PayActivity", "Ошибка активации", err));
                });
    }
}