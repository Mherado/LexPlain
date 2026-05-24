package mher.minasyan.lexplain;

import java.util.List;

public class HuggingFaceResponse {
    public List<Choice> choices;

    public static class Choice {
        public Message message;
    }

    public static class Message {
        public String content;
    }
}