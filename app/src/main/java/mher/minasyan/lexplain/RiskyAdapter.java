package mher.minasyan.lexplain;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RiskyAdapter extends RecyclerView.Adapter<RiskyAdapter.ViewHolder> {

    private List<String> sentences;
    private List<String> risks;

    public RiskyAdapter(List<String> sentences, List<String> risks) {
        this.sentences = sentences;
        this.risks = risks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_risky_moment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvText.setText(sentences.get(position));

        String risk = risks.get(position);
        String normalizedRisk = risk == null ? "" : risk.toLowerCase();

        if (normalizedRisk.contains("high") || normalizedRisk.contains("высок")) {
            holder.indicator.setBackgroundColor(Color.parseColor("#FF5252"));
        } else if (normalizedRisk.contains("medium") || normalizedRisk.contains("сред")) {
            holder.indicator.setBackgroundColor(Color.parseColor("#FFB74D"));
        } else {
            holder.indicator.setBackgroundColor(Color.parseColor("#81C784"));
        }
    }

    @Override
    public int getItemCount() {
        return sentences != null ? sentences.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        View indicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvMomentText);
            indicator = itemView.findViewById(R.id.riskIndicator);
        }
    }
}