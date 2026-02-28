package mher.minasyanlexplain;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dba_tokenizer {

    private OrtEnvironment env;
    private Map<String, Double> vocab;
    private OnnxTensor inputIdsTensor;
    private OnnxTensor attentionMaskTensor;
    private Map<String, OnnxTensor> inputs;
    private final Context context;

    public dba_tokenizer(Context context, String partOfText) throws Exception {
        this.context = context;
        this.env = OrtEnvironment.getEnvironment();

        loadVocabFromJson("resource/tokenizer.json");

        changePartOfText(partOfText);
    }

    private void loadVocabFromJson(String fileName) throws Exception {
        InputStream is = context.getAssets().open(fileName);
        JsonObject jsonObject = new Gson().fromJson(new InputStreamReader(is), JsonObject.class);
        JsonObject vocabJson = jsonObject.getAsJsonObject("model").getAsJsonObject("vocab");

        java.lang.reflect.Type type = new TypeToken<Map<String, Double>>(){}.getType();
        this.vocab = new Gson().fromJson(vocabJson, type);
    }

    public void changePartOfText(String partOfText) {
        closeTensors();

        String[] words = partOfText.toLowerCase().split("\\s+");
        List<Long> ids = new ArrayList<>();

        ids.add(101L);

        for (String word : words) {
            int start = 0;
            while (start < word.length()) {
                int end = word.length();
                String curSubword = null;
                while (start < end) {
                    String subword = (start == 0) ? word.substring(start, end) : "##" + word.substring(start, end);
                    if (vocab.containsKey(subword)) {
                        curSubword = subword;
                        break;
                    }
                    end--;
                }
                if (curSubword == null) {
                    ids.add(100L);
                    break;
                }
                ids.add(vocab.get(curSubword).longValue());
                start = end;
            }
        }
        ids.add(102L);

        long[] inputIds = new long[ids.size()];
        long[] attentionMask = new long[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            inputIds[i] = ids.get(i);
            attentionMask[i] = 1L;
        }

        try {
            inputIdsTensor = OnnxTensor.createTensor(env, new long[][]{inputIds});
            attentionMaskTensor = OnnxTensor.createTensor(env, new long[][]{attentionMask});
            inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }

    private void closeTensors() {
        if (inputIdsTensor != null) inputIdsTensor.close();
        if (attentionMaskTensor != null) attentionMaskTensor.close();
    }

    public Map<String, OnnxTensor> getInputs() {
        return inputs;
    }

    public void close() {
        closeTensors();
        if (env != null) env.close();
    }
}