package mher.minasyanlexplain;


import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView tvRisk;
    private TextView tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvRisk = findViewById(R.id.tvRisk);
        tvDescription = findViewById(R.id.tvDescription);
        Button btnBack = findViewById(R.id.btnBack);

        String fullText = getIntent().getStringExtra("EXTRACTED_TEXT");

        analyzeData(fullText);

        btnBack.setOnClickListener(v -> finish());
    }

    private void analyzeData(String text) {
        try {

            dba_ONNX model = new dba_ONNX(this, text);


            tvRisk.setText("ВЫСОКИЙ РИСК");
            tvDescription.setText("Анализ завершен успешно. В тексте обнаружено " + text.length() + " символов.");

        } catch (Exception e) {
            tvRisk.setText("ОШИБКА");
            tvDescription.setText(e.getMessage());
        }
    }
}