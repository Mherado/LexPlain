package mher.minasyan.lexplain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private ContractAdapter adapter;
    private List<Contract> contractList;
    private TextView tvEmpty;

    private final Runnable contractRefreshRunnable = this::refreshContractList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.rvHistory);
        tvEmpty = view.findViewById(R.id.tvEmptyHistory);

        contractList = ContractStorage.load(requireContext());
        bindContractList();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).registerContractRefreshCallback(contractRefreshRunnable);
        }
        refreshContractList();
    }

    @Override
    public void onStop() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).unregisterContractRefreshCallback(contractRefreshRunnable);
        }
        super.onStop();
    }

    public void refreshContractList() {
        if (!isAdded() || tvEmpty == null) return;
        contractList = ContractStorage.load(requireContext());
        bindContractList();
    }

    private void bindContractList() {
        if (contractList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(null);
            adapter = null;
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        if (adapter == null) {
            adapter = new ContractAdapter(contractList, true, this::onContractDeleted);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        } else {
            adapter.replaceAll(contractList);
        }
    }

    private void onContractDeleted(Contract removedContract, List<Contract> updatedContracts) {
        contractList = new ArrayList<>(updatedContracts);
        ContractStorage.save(requireContext(), contractList);
        if (contractList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(null);
            adapter = null;
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
