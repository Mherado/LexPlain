package mher.minasyan.lexplain;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import mher.minayan.lexplain.WiewContract;

public class ContractAdapter extends RecyclerView.Adapter<ContractAdapter.ViewHolder> {

    private final List<Contract> contractList;
    private final boolean allowDelete;
    private final OnContractDeleteListener onContractDeleteListener;

    public ContractAdapter(List<Contract> contractList) {
        this(contractList, false, null);
    }

    public ContractAdapter(
            List<Contract> contractList,
            boolean allowDelete,
            OnContractDeleteListener onContractDeleteListener
    ) {
        this.contractList = contractList != null ? new ArrayList<>(contractList) : new ArrayList<>();
        this.allowDelete = allowDelete;
        this.onContractDeleteListener = onContractDeleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contract, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contract contract = contractList.get(position);

        holder.tvName.setText(contract.getFileName());
        holder.tvRisk.setText(contract.getRiskLevel());
        updateRiskUi(holder, contract.getRiskLevel());

        holder.tvDelete.setText(R.string.history_delete);
        holder.tvDelete.setVisibility(allowDelete ? View.VISIBLE : View.GONE);
        holder.tvDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            Contract removed = contractList.remove(adapterPosition);
            notifyItemRemoved(adapterPosition);
            if (onContractDeleteListener != null) {
                onContractDeleteListener.onDelete(removed, new ArrayList<>(contractList));
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            WiewContract.currentContract = contract;
            Intent intent = new Intent(context, WiewContract.class);
            intent.putExtra("CONTRACT_DATA", contract);
            context.startActivity(intent);
        });
    }

    private void updateRiskUi(ViewHolder holder, String riskLevel) {
        String level = riskLevel == null ? "" : riskLevel.toLowerCase();

        int color = Color.parseColor("#4CAF50"); // green for Low / no risk
        if (level.contains("high") || level.contains("высок")) {
            color = Color.RED;
        } else if (level.contains("medium") || level.contains("сред")) {
            color = Color.parseColor("#FFA500");
        } else if (level.contains("low") || level.contains("низк") || level.isEmpty()) {
            color = Color.parseColor("#4CAF50");
        } else {
            // fallback: if it's not explicitly high/medium, treat as low
            color = Color.parseColor("#4CAF50");
        }

        holder.tvRisk.setTextColor(color);
        holder.riskIndicator.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return contractList != null ? contractList.size() : 0;
    }

    public void addContractAtTop(Contract contract, int maxItems) {
        if (contract == null) return;
        contractList.add(0, contract);
        notifyItemInserted(0);
        if (maxItems > 0 && contractList.size() > maxItems) {
            int lastIndex = contractList.size() - 1;
            contractList.remove(lastIndex);
            notifyItemRemoved(lastIndex);
        }
    }

    public void replaceAll(List<Contract> contracts) {
        contractList.clear();
        if (contracts != null) {
            contractList.addAll(contracts);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRisk, tvDelete;
        View riskIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFileName);
            tvRisk = itemView.findViewById(R.id.tvRiskLevel);
            riskIndicator = itemView.findViewById(R.id.riskIndicator);
            tvDelete = itemView.findViewById(R.id.tvDeleteContract);
        }
    }

    public interface OnContractDeleteListener {
        void onDelete(Contract removedContract, List<Contract> updatedContracts);
    }
}