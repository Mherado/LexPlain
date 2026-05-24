package mher.minasyan.lexplain;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import mher.minayan.lexplain.WiewContract;

public class FullTextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        setContentView(R.layout.activity_full_text);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        TextView tvFullText = findViewById(R.id.tvFullContractText);

        Contract contract = WiewContract.currentContract;

        if (contract != null) {
            highlightRisks(tvFullText, contract.getcontracttextparts(), contract.getcontracttextrisks());
        }
    }

    private void highlightRisks(TextView textView, List<String> sentences, List<String> risks) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0; i < sentences.size(); i++) {
            String text = sentences.get(i) + " ";
            String risk = risks.get(i);
            SpannableString span = new SpannableString(text);

            int bgColor = Color.TRANSPARENT;
            int textColor = Color.parseColor("#263238");

            if (risk.contains("High")) {
                bgColor = Color.parseColor("#FFCDD2");
                textColor = Color.parseColor("#B71C1C");
            } else if (risk.contains("Medium")) {
                bgColor = Color.parseColor("#FFE0B2");
                textColor = Color.parseColor("#E65100");
            }

            if (bgColor != Color.TRANSPARENT) {
                span.setSpan(new BackgroundColorSpan(bgColor), 0, text.length(), 0);
                span.setSpan(new ForegroundColorSpan(textColor), 0, text.length(), 0);
            }
            builder.append(span);
        }
        textView.setText(builder);
    }
}