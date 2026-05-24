package mher.minasyan.lexplain;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import mher.minayan.lexplain.WiewContract;

public class HomeFragment extends Fragment {

    private TextView statusText;
    private RecyclerView recyclerView;
    private ContractAdapter adapter;
    private List<Contract> contractList;

    private ActivityResultLauncher<String> filePicker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_fragment_home, container, false);

        statusText = view.findViewById(R.id.Main_Page_Title);
        View btnAdd = view.findViewById(R.id.btnAddContract);
        recyclerView = view.findViewById(R.id.recyclerViewContracts);

        loadData();

        List<Contract> lastFive = (contractList.size() > 5)
                ? new ArrayList<>(contractList.subList(0, 5))
                : new ArrayList<>(contractList);

        adapter = new ContractAdapter(lastFive);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        filePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) processFile(uri);
                }
        );

        btnAdd.setOnClickListener(v -> filePicker.launch("*/*"));

        return view;
    }

    private void processFile(Uri uri) {

        Context ctx = getContext();
        if (ctx == null) return;

        String mime = ctx.getContentResolver().getType(uri);

        if (mime == null ||
                (!mime.equalsIgnoreCase("application/pdf") && !mime.startsWith("image/"))) {

            setStatus(R.string.home_select_file);
            return;
        }

        String fileName = "Contract_" + System.currentTimeMillis() % 10000 + ".pdf";

        setStatus(R.string.home_analyzing);

        new Thread(() -> {
            try {

                Text_Writer writer = new Text_Writer(ctx, uri.toString());
                String extractedText = writer.getFullstring();

                if (extractedText == null || extractedText.trim().isEmpty()) {
                    runOnUi(() -> setStatus(R.string.home_extract_error));
                    return;
                }

                new dba_ONNX(ctx, extractedText, (sentences, risks) -> {

                    String totalRisk =
                            (risks == null || risks.isEmpty())
                                    ? "Low Risk"
                                    : calculateTotalRisk(risks);

                    Contract newContract = new Contract(
                            fileName,
                            totalRisk,
                            sentences,
                            risks
                    );

                    runOnUi(() -> {

                        if (contractList != null) {
                            contractList.add(0, newContract);
                        }

                        saveData();

                        if (adapter != null) {
                            adapter.addContractAtTop(newContract, 5);
                        }

                        setStatus(R.string.app_name);

                        WiewContract.currentContract = newContract;

                        Intent intent = new Intent(getContext(), WiewContract.class);
                        intent.putExtra("CONTRACT_DATA", newContract);
                        startActivity(intent);
                    });
                });

            } catch (Exception e) {
                runOnUi(() -> setStatus(R.string.home_analysis_error));
            }
        }).start();
    }

    private void setStatus(int resId) {
        if (statusText != null) {
            statusText.setText(resId);
        }
    }

    private void runOnUi(Runnable r) {
        if (!isAdded() || getActivity() == null) return;
        getActivity().runOnUiThread(r);
    }

    private String calculateTotalRisk(List<String> risks) {
        boolean hasHigh = false, hasMedium = false;

        for (String r : risks) {
            if (r == null) continue;

            if (r.contains("High")) hasHigh = true;
            else if (r.contains("Medium")) hasMedium = true;
        }

        return hasHigh ? "High Risk"
                : (hasMedium ? "Medium Risk" : "Low Risk");
    }

    private void saveData() {
        if (getActivity() == null || contractList == null) return;

        SharedPreferences sp =
                getActivity().getSharedPreferences("lexplain_prefs", Context.MODE_PRIVATE);

        sp.edit()
                .putString("contracts", new Gson().toJson(contractList))
                .apply();
    }

    private void loadData() {
        if (getActivity() == null) {
            contractList = new ArrayList<>();
            return;
        }

        SharedPreferences sp =
                getActivity().getSharedPreferences("lexplain_prefs", Context.MODE_PRIVATE);

        String json = sp.getString("contracts", null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<Contract>>() {}.getType();
            contractList = new Gson().fromJson(json, type);
        }

        if (contractList == null) {
            contractList = new ArrayList<>();
        }
    }
}