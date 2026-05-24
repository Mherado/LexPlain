package mher.minasyan.lexplain;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class ContractStorage {

    public static final String PREFS_NAME = "lexplain_prefs";
    public static final String KEY_CONTRACTS = "contracts";

    private ContractStorage() {
    }

    public static List<Contract> load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CONTRACTS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Contract>>() {}.getType();
        List<Contract> list = new Gson().fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public static void save(Context context, List<Contract> contracts) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        List<Contract> toSave = contracts != null ? contracts : new ArrayList<>();
        prefs.edit()
                .putString(KEY_CONTRACTS, new Gson().toJson(toSave))
                .commit();
    }

    public static void clear(Context context) {
        save(context, new ArrayList<>());
    }
}
