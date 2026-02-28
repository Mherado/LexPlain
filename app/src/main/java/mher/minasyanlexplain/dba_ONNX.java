package mher.minasyanlexplain;

import android.content.Context;
import android.util.Log;

import ai.onnxruntime.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;

public class dba_ONNX {

    private OrtEnvironment env;
    private OrtSession.SessionOptions options;
    private OrtSession legalSession;
    private OrtSession riskSession;
    private dba_tokenizer tokenizer;
    private final Context context;


    public dba_ONNX(Context context, String partOfText) throws Exception {
        this.context = context;
        this.env = OrtEnvironment.getEnvironment();
        this.options = new OrtSession.SessionOptions();
        String riskModelPath = getModelPath("resource/risk_model.onnx");
        String legalModelPath = getModelPath("resource/legal_bert.onnx");
        this.riskSession = env.createSession(riskModelPath, options);
        this.legalSession = env.createSession(legalModelPath, options);

        List<String> sentences = getSmartChunks(partOfText);
        for (String i : sentences) {
            System.out.println(i);
            float[][] vectors = runLegalBert(i);
            runRiskModel(vectors);
        }
    }

    public String runRiskModel(float[][] inputs) throws Exception {
        String inputName = riskSession.getInputNames().iterator().next();
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputs)) {
            Map<String, OnnxTensor> container = Collections.singletonMap(inputName, inputTensor);

            try (OrtSession.Result riskResult = riskSession.run(container)) {
                float[][] probabilities = (float[][]) riskResult.get(1).getValue();
                float riskScore = probabilities[0][1];
                System.out.println("Risk score: " + riskScore);

                String riskLevel;
                if (riskScore < 0.3) riskLevel = "Low";
                else if (riskScore < 0.6) riskLevel = "Medium";
                else riskLevel = "High";

                return ("Risk level: " + riskLevel);
            }
        }
    }

    public float[][] runLegalBert(String text) throws Exception {
        tokenizer = new dba_tokenizer(context, text);

        try {
            Map<String, OnnxTensor> inputs = tokenizer.getInputs();
            try (OrtSession.Result result = legalSession.run(inputs)) {
                float[][][] logits3d = (float[][][]) result.get(0).getValue();
                float[] tokens = logits3d[0][0];

                float[][] a = new float[1][tokens.length];
                System.arraycopy(tokens, 0, a[0], 0, tokens.length);

                return a;
            }
        } finally {
            if (tokenizer != null) {
                tokenizer.close();
            }
        }
    }

    public List<String> getSmartChunks(String text) {
        String[] initialSentences = text.split("(?<=\\.)\\s+(?=[A-Z])");
        List<String> chunks = new ArrayList<>();

        StringBuilder currentChunk = new StringBuilder();
        int minLength = 25;

        for (String sentence : initialSentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;

            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
            }
            currentChunk.append(sentence);

            if (currentChunk.length() >= minLength) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        if (chunks.size() > 1) {
            for (int i = 0; i < chunks.size() - 1; i++) {
                chunks.set(i, chunks.get(i) + " " + chunks.get(i + 1));
            }
            chunks.remove(chunks.size() - 1);
        }

        return chunks;
    }

    public void close() throws Exception {
        if (tokenizer != null) tokenizer.close();
        if (legalSession != null) legalSession.close();
        if (riskSession != null) riskSession.close();
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
}