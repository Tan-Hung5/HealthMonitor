package com.example.healthmonitor.ui.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthmonitor.R;
import com.example.healthmonitor.data.model.ChatMessage;
import com.example.healthmonitor.data.model.HealthMetric;
import com.example.healthmonitor.data.remote.ChatbotApiHelper;
import com.example.healthmonitor.data.remote.FirebaseHelper;
import com.example.healthmonitor.utils.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatbotBottomSheetDialog extends BottomSheetDialogFragment {

    private RecyclerView rvChatMessages;
    private EditText etChatInput;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private FirebaseHelper firebaseHelper;
    private PreferenceManager prefManager;
    private ChatbotApiHelper chatbotApiHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_chatbot, container, false);

        rvChatMessages = view.findViewById(R.id.rv_chat_messages);
        etChatInput = view.findViewById(R.id.et_chat_input);
        ImageButton btnSendChat = view.findViewById(R.id.btn_send_chat);
        ImageButton btnCloseChat = view.findViewById(R.id.btn_close_chat);

        Button chipCurrentMetrics = view.findViewById(R.id.chip_current_metrics);
        Button chipSpo2Advice = view.findViewById(R.id.chip_spo2_advice);
        Button chipHeartRateAdvice = view.findViewById(R.id.chip_heart_rate_advice);
        Button chipBpAdvice = view.findViewById(R.id.chip_bp_advice);

        firebaseHelper = FirebaseHelper.getInstance();
        prefManager = new PreferenceManager(requireContext());
        chatbotApiHelper = new ChatbotApiHelper();

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);
        rvChatMessages.setAdapter(chatAdapter);

        // Welcome message from Bot
        addBotMessage("Xin chào! Tôi là Trợ lý Sức khỏe AI của Health Monitor. Bạn có thể hỏi tôi bất kỳ câu hỏi nào về sức khỏe, chỉ số sinh hiệu hay bí quyết sống khỏe!");

        btnSendChat.setOnClickListener(v -> sendMessage());
        btnCloseChat.setOnClickListener(v -> dismiss());

        chipCurrentMetrics.setOnClickListener(v -> handleCurrentMetricsQuery());
        chipSpo2Advice.setOnClickListener(v -> handleUserQuery("Cho tôi lời khuyên về chỉ số SpO2"));
        chipHeartRateAdvice.setOnClickListener(v -> handleUserQuery("Nhịp tim như thế nào là chuẩn?"));
        chipBpAdvice.setOnClickListener(v -> handleUserQuery("Lời khuyên về chỉ số huyết áp"));

        return view;
    }

    private void sendMessage() {
        String text = etChatInput.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            etChatInput.setText("");
            handleUserQuery(text);
        }
    }

    private void handleUserQuery(String query) {
        addUserMessage(query);

        // Add a temporary thinking message
        int loadingPos = addBotMessage("🤖 Đang suy nghĩ...");

        chatbotApiHelper.sendChatMessage(query, messageList, new ChatbotApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String responseText) {
                if (isAdded()) {
                    updateBotMessage(loadingPos, responseText);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    // Fallback to local advisory rule if API connection failed
                    String fallbackResponse = getLocalFallbackResponse(query);
                    updateBotMessage(loadingPos, fallbackResponse);
                }
            }
        });
    }

    private void handleCurrentMetricsQuery() {
        String userDisplay = "Chỉ số hiện tại của tôi thế nào?";
        addUserMessage(userDisplay);

        String deviceId = prefManager.getDeviceId();
        String userId = prefManager.getUserId();

        if (deviceId.isEmpty() || deviceId.equals("DEFAULT_DEVICE")) {
            addBotMessage("Chưa có thiết bị đo nào được kết nối. Bạn hãy vào mục 'More' ➔ 'Change Device' để kết nối thiết bị đo nhé!");
            return;
        }

        int loadingPos = addBotMessage("📊 Đang đọc chỉ số từ thiết bị...");

        firebaseHelper.observeRealtimeData(deviceId, userId, new FirebaseHelper.DataCallback<HealthMetric>() {
            @Override
            public void onDataReceived(HealthMetric metric) {
                if (isAdded() && metric != null) {
                    String enrichedQuery = String.format(Locale.getDefault(),
                            "Chỉ số sức khỏe hiện tại của tôi từ thiết bị %s: Nhịp tim %d BPM, SpO2 %.1f%%, Nhịp thở %d nhịp/phút, Huyết áp %s mmHg. Hãy phân tích và đưa ra tư vấn cho tôi.",
                            deviceId, metric.getHeartRate(), metric.getSpo2(), metric.getRespiratory(), metric.getBloodPressure());

                    chatbotApiHelper.sendChatMessage(enrichedQuery, messageList, new ChatbotApiHelper.ApiCallback() {
                        @Override
                        public void onSuccess(String responseText) {
                            if (isAdded()) {
                                updateBotMessage(loadingPos, responseText);
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            if (isAdded()) {
                                String localReport = String.format(Locale.getDefault(),
                                        "📊 Chỉ số sức khỏe mới nhất (Thiết bị %s):\n" +
                                                "• Nhịp tim: %d BPM\n" +
                                                "• SpO2: %.1f %%\n" +
                                                "• Nhịp thở: %d nhịp/phút\n" +
                                                "• Huyết áp: %s mmHg\n\n" +
                                                "✅ Các chỉ số sức khỏe của bạn đang được theo dõi sát sao.",
                                        deviceId, metric.getHeartRate(), metric.getSpo2(), metric.getRespiratory(), metric.getBloodPressure());
                                updateBotMessage(loadingPos, localReport);
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    updateBotMessage(loadingPos, "Không thể kết nối đến thiết bị đo: " + message);
                }
            }
        });
    }

    private String getLocalFallbackResponse(String query) {
        String lowerQuery = query.toLowerCase(Locale.getDefault());
        if (lowerQuery.contains("spo2") || lowerQuery.contains("oxy")) {
            return "💡 Lời khuyên SpO2:\n- Mức chuẩn: 95% - 100%.\n- SpO2 < 95%: Cần ngồi nghỉ ngơi, hít thở sâu.\n- SpO2 < 90%: Mức nguy hiểm, hãy gọi y tế khẩn cấp.";
        } else if (lowerQuery.contains("nhịp tim") || lowerQuery.contains("tim")) {
            return "❤️ Lời khuyên Nhịp tim:\n- Nhịp tim nghỉ ngơi chuẩn: 60 - 100 BPM.\n- Tập thể dục nhẹ nhàng và uống đủ nước giúp tim luôn khỏe mạnh.";
        } else if (lowerQuery.contains("huyết áp")) {
            return "🩸 Lời khuyên Huyết áp:\n- Huyết áp chuẩn: Dưới 120/80 mmHg.\n- Nên ăn nhạt, giảm muối và vận động hằng ngày.";
        }
        return "Tôi là Trợ lý Sức khỏe AI. Bạn có thể hỏi tôi về các chỉ số nhịp tim, SpO2, huyết áp hoặc bí quyết sống khỏe mỗi ngày!";
    }

    private int addUserMessage(String message) {
        messageList.add(new ChatMessage(message, true));
        int pos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(pos);
        rvChatMessages.smoothScrollToPosition(pos);
        return pos;
    }

    private int addBotMessage(String message) {
        messageList.add(new ChatMessage(message, false));
        int pos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(pos);
        rvChatMessages.smoothScrollToPosition(pos);
        return pos;
    }

    private void updateBotMessage(int position, String newMessage) {
        if (position >= 0 && position < messageList.size()) {
            messageList.get(position).setMessage(newMessage);
            chatAdapter.notifyItemChanged(position);
            rvChatMessages.smoothScrollToPosition(position);
        }
    }
}
