package mher.minasyan.lexplain;

import java.util.ArrayList;
import java.util.List;

public class HuggingFaceRequest {
    public String model = "meta-llama/Llama-3.1-8B-Instruct:novita";
    public List<Message> messages;

    public HuggingFaceRequest(String userText) {
        this.messages = new ArrayList<>();
        this.messages.add(new Message("user", userText));
    }

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}