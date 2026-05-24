package mher.minasyan.lexplain;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import mher.minayan.lexplain.WiewContract;

public class ConspectWiew extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conspect_wiew);

        View mainView = findViewById(R.id.scroll_View);

        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        TextView conspect = findViewById(R.id.full_contract);

        if (WiewContract.currentContract != null) {
            String conspect_text = WiewContract.currentContract.getConspect();
            conspect.setText(conspect_text);
        } else {
            conspect.setText("Ошибка: данные контракта потеряны");
        }
    }
}