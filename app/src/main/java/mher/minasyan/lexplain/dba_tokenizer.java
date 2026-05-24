package mher.minasyan.lexplain;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class dba_tokenizer {

    private Map<String, Integer> vocab;
    private static final int MAX_LEN = 128;

    public dba_tokenizer(Context context) throws Exception {
        loadVocab(context, "resource/tokenizer.json");
    }

    private void loadVocab(Context context, String fileName) throws Exception {
        InputStream is = context.getAssets().open(fileName);
        JsonObject json = new Gson().fromJson(new InputStreamReader(is), JsonObject.class);

        JsonObject vocabJson = json.getAsJsonObject("model").getAsJsonObject("vocab");

        vocab = new HashMap<>();
        for (String key : vocabJson.keySet()) {
            vocab.put(key, vocabJson.get(key).getAsInt());
        }
    }

    public TokenResult tokenize(String text) {

        List<String> tokens = basicTokenize(text);
        List<Integer> ids = new ArrayList<>();

        ids.add(101);

        for (String token : tokens) {
            ids.addAll(wordPiece(token));
        }

        ids.add(102);

        return pad(ids);
    }

    private List<String> basicTokenize(String text) {

        List<String> tokens = new ArrayList<>();

        StringBuilder current = new StringBuilder();

        for (char c : text.toCharArray()) {

            if (Character.isLetterOrDigit(c)) {
                current.append(c);
            } else {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                if (!Character.isWhitespace(c)) {
                    tokens.add(String.valueOf(c));
                }
            }
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens;
    }

    private List<Integer> wordPiece(String word) {

        List<Integer> ids = new ArrayList<>();

        int start = 0;

        while (start < word.length()) {
            int end = word.length();
            String curSub = null;

            while (start < end) {
                String sub = word.substring(start, end);
                if (start > 0) sub = "##" + sub;

                if (vocab.containsKey(sub)) {
                    curSub = sub;
                    break;
                }
                end--;
            }

            if (curSub == null) {
                ids.add(100);
                break;
            }

            ids.add(vocab.get(curSub));
            start = end;
        }

        return ids;
    }

    private TokenResult pad(List<Integer> ids) {

        long[] inputIds = new long[MAX_LEN];
        long[] attentionMask = new long[MAX_LEN];

        int len = Math.min(ids.size(), MAX_LEN);

        for (int i = 0; i < len; i++) {
            inputIds[i] = ids.get(i);
            attentionMask[i] = 1;
        }

        return new TokenResult(inputIds, attentionMask);
    }

    public static class TokenResult {
        public long[] inputIds;
        public long[] attentionMask;

        public TokenResult(long[] inputIds, long[] attentionMask) {
            this.inputIds = inputIds;
            this.attentionMask = attentionMask;
        }
    }
}