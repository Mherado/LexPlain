package mher.minasyan.lexplain;

import android.content.Context;

import java.io.*;
import java.util.*;

import ai.onnxruntime.*;

public class dba_ONNX {

    private final Context context;
    private OrtEnvironment env;
    private OrtSession session;
    private dba_tokenizer tokenizer;

    public dba_ONNX(Context context, String text, OnAnalysisFinishedListener listener) throws Exception {
        this.context = context;
        this.env = OrtEnvironment.getEnvironment();

        String modelPath = getModelPath("resource/model_int8.onnx");
        this.session = env.createSession(modelPath, new OrtSession.SessionOptions());

        this.tokenizer = new dba_tokenizer(context);

        List<String> sentences = getSmartChunks(text);
        List<String> results = new ArrayList<>();

        for (String sentence : sentences) {
            results.add(runModel(sentence));
        }

        if (listener != null) {
            listener.onFinished(sentences, results);
        }
    }

    public String runModel(String text) throws Exception {

        dba_tokenizer.TokenResult tokens = tokenizer.tokenize(text);

        try (
                OnnxTensor inputIdsTensor = OnnxTensor.createTensor(env, new long[][]{tokens.inputIds});
                OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(env, new long[][]{tokens.attentionMask});
        ) {

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);

            try (OrtSession.Result result = session.run(inputs)) {

                float[][] logits = (float[][]) result.get(0).getValue();
                float[] probs = softmax(logits[0]);

                int predictedClass = argmax(probs);
                float confidence = probs[predictedClass];

                String label;
                switch (predictedClass) {
                    case 0: label = "Low"; break;
                    case 1: label = "Medium"; break;
                    case 2: label = "High"; break;
                    default: label = "UNKNOWN";
                }

                return label + " (" + String.format("%.2f", confidence * 100) + "%)";
            }
        }
    }

    // softmax
    private float[] softmax(float[] logits) {
        float max = Float.NEGATIVE_INFINITY;
        for (float v : logits) max = Math.max(max, v);

        float sum = 0f;
        float[] exps = new float[logits.length];

        for (int i = 0; i < logits.length; i++) {
            exps[i] = (float) Math.exp(logits[i] - max);
            sum += exps[i];
        }

        for (int i = 0; i < exps.length; i++) {
            exps[i] /= sum;
        }

        return exps;
    }

    private int argmax(float[] arr) {
        int idx = 0;
        float max = arr[0];

        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
                idx = i;
            }
        }
        return idx;
    }

    public List<String> getSmartChunks(String text) {
        String[] parts = text.split("(?<=[.!?])\\s+");
        List<String> chunks = new ArrayList<>();

        for (String s : parts) {
            if (s.trim().length() > 10) {
                chunks.add(s.trim());
            }
        }

        return chunks;
    }

    public void close() throws Exception {
        if (session != null) session.close();
        if (env != null) env.close();
    }

    private String getModelPath(String assetName) throws IOException {
        File file = new File(context.getCacheDir(), assetName.replace("/", "_"));

        if (!file.exists()) {
            try (InputStream is = context.getAssets().open(assetName);
                 FileOutputStream fos = new FileOutputStream(file)) {

                byte[] buffer = new byte[8192];
                int read;

                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }
        }

        return file.getAbsolutePath();
    }

    public interface OnAnalysisFinishedListener {
        void onFinished(List<String> sentences, List<String> results);
    }
}