package com.example.healthmonitor.data.remote;

import android.os.Handler;
import android.os.Looper;
import com.example.healthmonitor.data.model.ChatMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatbotApiHelper {

    private static final String API_URL_HTTP = "http://api.healthmonitorchatbot.dpdns.org/chat";
    private static final String API_URL_HTTPS = "https://api.healthmonitorchatbot.dpdns.org/chat";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback {
        void onSuccess(String responseText);
        void onError(String errorMessage);
    }

    public void sendChatMessage(String question, List<ChatMessage> conversationHistory, ApiCallback callback) {
        executorService.execute(() -> {
            String result = callChatApi(API_URL_HTTP, question, conversationHistory);
            if (result == null) {
                result = callChatApi(API_URL_HTTPS, question, conversationHistory);
            }

            final String finalResult = result;
            mainHandler.post(() -> {
                if (finalResult != null) {
                    callback.onSuccess(finalResult);
                } else {
                    callback.onError("Không thể kết nối tới server Chatbot.");
                }
            });
        });
    }

    private String callChatApi(String urlString, String question, List<ChatMessage> conversationHistory) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("question", question);

            JSONArray historyArray = new JSONArray();
            if (conversationHistory != null) {
                int start = Math.max(0, conversationHistory.size() - 10);
                for (int i = start; i < conversationHistory.size(); i++) {
                    ChatMessage msg = conversationHistory.get(i);
                    JSONObject historyObj = new JSONObject();
                    historyObj.put("role", msg.isUser() ? "user" : "assistant");
                    historyObj.put("content", msg.getMessage());
                    historyArray.put(historyObj);
                }
            }
            jsonParam.put("history", historyArray);

            byte[] input = jsonParam.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();

            if (is == null) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            String responseString = sb.toString();
            if (responseCode >= 200 && responseCode < 300) {
                return parseResponseBody(responseString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    private String parseResponseBody(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            if (jsonObject.has("answer")) {
                return jsonObject.getString("answer");
            } else if (jsonObject.has("response")) {
                return jsonObject.getString("response");
            } else if (jsonObject.has("reply")) {
                return jsonObject.getString("reply");
            } else if (jsonObject.has("message")) {
                return jsonObject.getString("message");
            } else if (jsonObject.has("content")) {
                return jsonObject.getString("content");
            } else if (jsonObject.has("text")) {
                return jsonObject.getString("text");
            }
        } catch (Exception e) {
            if (!jsonString.trim().startsWith("{") && !jsonString.trim().isEmpty()) {
                return jsonString.trim();
            }
        }
        return jsonString;
    }
}
