package mher.minasyan.lexplain;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import mher.minayan.lexplain.WiewContract;

public class fragment_home extends Fragment {
    private static final String TAG = "fragment_home";
    private TextView statusText;
    private RecyclerView recyclerView;
    private ContractAdapter adapter;
    private List<Contract> contractList;

    private final Runnable contractRefreshRunnable = this::refreshContractList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_home, container, false);
        statusText = view.findViewById(R.id.Main_Page_Title);
        View btnAdd = view.findViewById(R.id.btnAddContract);
        recyclerView = view.findViewById(R.id.recyclerViewContracts);

        contractList = ContractStorage.load(requireContext());
        adapter = new ContractAdapter(getRecentContracts());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        ActivityResultLauncher<String> filePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) processFile(uri);
                }
        );
        btnAdd.setOnClickListener(v -> filePicker.launch("*/*"));
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
        if (!isAdded()) return;
        contractList = ContractStorage.load(requireContext());
        if (adapter != null) {
            adapter.replaceAll(getRecentContracts());
        }
    }

    private List<Contract> getRecentContracts() {
        if (contractList == null || contractList.isEmpty()) {
            return new ArrayList<>();
        }
        if (contractList.size() > 5) {
            return new ArrayList<>(contractList.subList(0, 5));
        }
        return new ArrayList<>(contractList);
    }

    private void processFile(Uri uri) {
        if (getContext() == null) return;
        String mime = getContext().getContentResolver().getType(uri);
        if (mime != null && !mime.equalsIgnoreCase("application/pdf") && !mime.startsWith("image/")) {
            statusText.setText(R.string.home_select_file);
            return;
        }
        String fileName = "Contract_" + System.currentTimeMillis() % 10000 + ".pdf";
        statusText.setText(R.string.home_analyzing);

        new Thread(() -> {
            try {
                Text_Writer writer = new Text_Writer(getContext(), uri.toString());
                String extractedText = writer.getFullstring();
                new dba_ONNX(getContext(), extractedText, (sentences, risks) -> {
                    String totalRisk = calculateTotalRisk(risks);
                    Contract newContract = new Contract(fileName, totalRisk, sentences, risks);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            contractList.add(0, newContract);
                            ContractStorage.save(requireContext(), contractList);
                            adapter.addContractAtTop(newContract, 5);
                            statusText.setText(R.string.app_name);
                            WiewContract.currentContract = newContract;
                            Intent intent = new Intent(getContext(), WiewContract.class);
                            intent.putExtra("CONTRACT_DATA", newContract);
                            startActivity(intent);
                        });
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Analysis error", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> statusText.setText(R.string.home_analysis_error));
                }
            }
        }).start();
    }

    private String calculateTotalRisk(List<String> risks) {
        if (risks == null || risks.isEmpty()) return "Low Risk";
        boolean hasHigh = false, hasMedium = false;
        for (String r : risks) {
            if (r.contains("High")) hasHigh = true;
            else if (r.contains("Medium")) hasMedium = true;
        }
        return hasHigh ? "High Risk" : (hasMedium ? "Medium Risk" : "Low Risk");
    }
}
