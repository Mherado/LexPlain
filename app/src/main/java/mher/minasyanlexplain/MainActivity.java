package mher.minasyanlexplain;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        Button btnAdd = findViewById(R.id.btnAddContract);


        ActivityResultLauncher<String> filePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        processFile(uri);
                    }
                }
        );

        btnAdd.setOnClickListener(v -> {

            filePicker.launch("*/*");
        });
    }

    private void processFile(Uri uri) {
        statusText.setText("Обработка файла...");

        new Thread(() -> {
            try {

                Text_Writer writer = new Text_Writer(MainActivity.this, uri.toString());
                String extractedText = writer.getFullstring();

                if (extractedText == null || extractedText.isEmpty()) {
                    throw new Exception("Не удалось извлечь текст из файла");
                }

                runOnUiThread(() -> {
                    statusText.setText("Текст успешно извлечен!");

                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                    intent.putExtra("EXTRACTED_TEXT", extractedText);
                    startActivity(intent);
                });

            } catch (Exception e) {
                Log.e("ProcessFileError", "Ошибка: " + e.getMessage());
                runOnUiThread(() -> {
                    statusText.setText("Ошибка при обработке: " + e.getLocalizedMessage());
                });
            }
        }).start();
    }
}