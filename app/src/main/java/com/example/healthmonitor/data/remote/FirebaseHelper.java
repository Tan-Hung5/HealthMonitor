package com.example.healthmonitor.data.remote;

import com.example.healthmonitor.data.model.AuthCredentials;
import com.example.healthmonitor.data.model.DateHistoryGroup;
import com.example.healthmonitor.data.model.HealthMetric;
import com.example.healthmonitor.data.model.User;
import com.example.healthmonitor.utils.HashUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FirebaseHelper {
    private final DatabaseReference mDatabase;
    private static FirebaseHelper instance;

    public FirebaseHelper() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public interface DataCallback<T> {
        void onDataReceived(T data);
        void onError(String message);
    }

    // --- AUTH & USER ---
    public void registerUser(User user, String password, ActionCallback callback) {
        String userId = String.format("%06d", new Random().nextInt(1000000));
        user.setUserId(userId);
        String hashedPassword = HashUtils.hashPassword(password);
        AuthCredentials credentials = new AuthCredentials(user.getEmail(), hashedPassword);

        mDatabase.child("auth").child(userId).setValue(credentials)
                .addOnSuccessListener(aVoid -> {
                    mDatabase.child("users").child(userId).setValue(user)
                            .addOnSuccessListener(aVoid1 -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void login(String email, String password, AuthCallback callback) {
        String hashedPassword = HashUtils.hashPassword(password);
        mDatabase.child("auth").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot authSnapshot : snapshot.getChildren()) {
                                AuthCredentials credentials = authSnapshot.getValue(AuthCredentials.class);
                                if (credentials != null && credentials.getPassword().equals(hashedPassword)) {
                                    fetchUserData(authSnapshot.getKey(), callback);
                                    return;
                                }
                            }
                            callback.onFailure("Mật khẩu không chính xác");
                        } else {
                            callback.onFailure("Email không tồn tại");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public void fetchUserData(String userId, AuthCallback callback) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) callback.onSuccess(user);
                else callback.onFailure("Không tìm thấy người dùng");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        });
    }

    public void checkEmailExists(String email, AuthCallback callback) {
        mDatabase.child("auth").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot authSnapshot : snapshot.getChildren()) {
                                // Trả về userId thông qua callback (đối tượng User chỉ cần userId)
                                User user = new User();
                                user.setUserId(authSnapshot.getKey());
                                user.setEmail(email);
                                callback.onSuccess(user);
                                return;
                            }
                        } else {
                            callback.onFailure("Email not found");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public void resetPassword(String userId, String newPassword, ActionCallback callback) {
        String hashedPassword = HashUtils.hashPassword(newPassword);
        mDatabase.child("auth").child(userId).child("password").setValue(hashedPassword)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateUserProfile(User user, ActionCallback callback) {
        mDatabase.child("users").child(user.getUserId()).setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // --- DEVICE ---
    public void checkAndConnectDevice(String newDeviceId, String oldDeviceId, String userId, ActionCallback callback) {
        mDatabase.child("devices").child(newDeviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Boolean used = snapshot.child("used").getValue(Boolean.class);
                    String currentUserId = snapshot.child("user_id").getValue(String.class);
                    if (currentUserId == null) {
                        currentUserId = snapshot.child("userId").getValue(String.class);
                    }

                    if (used == null || !used || (userId != null && userId.equals(currentUserId))) {
                        connectToDevice(newDeviceId, oldDeviceId, userId, callback);
                    } else {
                        callback.onFailure("Thiết bị này đã được sử dụng bởi người dùng khác");
                    }
                } else {
                    // Nếu thiết bị chưa tồn tại, tự động tạo mới (hỗ trợ quá trình test)
                    connectToDevice(newDeviceId, oldDeviceId, userId, callback);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { callback.onFailure(error.getMessage()); }
        });
    }

    private void connectToDevice(String newDeviceId, String oldDeviceId, String userId, ActionCallback callback) {
        // 1. Cập nhật thiết bị mới: used = true và user_id = userId
        mDatabase.child("devices").child(newDeviceId).child("used").setValue(true);
        mDatabase.child("devices").child(newDeviceId).child("user_id").setValue(userId)
                .addOnSuccessListener(aVoid -> {
                    // 2. Nếu có thiết bị cũ và khác thiết bị mới, reset thiết bị cũ
                    if (oldDeviceId != null && !oldDeviceId.isEmpty() && !oldDeviceId.equals("DEFAULT_DEVICE") && !oldDeviceId.equals(newDeviceId)) {
                        mDatabase.child("devices").child(oldDeviceId).child("used").setValue(false);
                        mDatabase.child("devices").child(oldDeviceId).child("user_id").setValue("");
                    }
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void disconnectDevice(String deviceId, ActionCallback callback) {
        if (deviceId == null || deviceId.isEmpty() || deviceId.equals("DEFAULT_DEVICE")) {
            callback.onSuccess();
            return;
        }
        mDatabase.child("devices").child(deviceId).child("used").setValue(false);
        mDatabase.child("devices").child(deviceId).child("user_id").setValue("")
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // --- HEALTH DATA ---
    public void observeRealtimeData(String deviceId, String userId, DataCallback<HealthMetric> callback) {
        String path = deviceId + "_" + userId;
        mDatabase.child("realtime_data").child(path).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HealthMetric metric = snapshot.getValue(HealthMetric.class);
                if (metric != null) {
                    metric.setDeviceId(deviceId);
                    if (metric.getTimestamp() == 0) {
                        metric.setTimestamp(System.currentTimeMillis());
                    }
                    callback.onDataReceived(metric);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { callback.onError(error.getMessage()); }
        });
    }

    public void fetchGroupedHistoryData(String userId, DataCallback<List<DateHistoryGroup>> callback) {
        mDatabase.child("history_data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, List<HealthMetric>> mapByDate = new LinkedHashMap<>();

                if (snapshot.exists()) {
                    for (DataSnapshot deviceUserSnap : snapshot.getChildren()) {
                        String key = deviceUserSnap.getKey();
                        if (key != null && (key.endsWith("_" + userId) || key.equals(userId))) {
                            String devId = key.contains("_") ? key.split("_")[0] : "";

                            for (DataSnapshot dateSnap : deviceUserSnap.getChildren()) {
                                String dateKey = dateSnap.getKey();

                                for (DataSnapshot timeSnap : dateSnap.getChildren()) {
                                    String timeKey = timeSnap.getKey();

                                    HealthMetric metric = timeSnap.getValue(HealthMetric.class);
                                    if (metric != null) {
                                        if (metric.getDeviceId() == null || metric.getDeviceId().isEmpty()) {
                                            metric.setDeviceId(devId);
                                        }
                                        metric.setDate(dateKey);
                                        metric.setTimeKey(timeKey);

                                        if (!mapByDate.containsKey(dateKey)) {
                                            mapByDate.put(dateKey, new ArrayList<>());
                                        }
                                        mapByDate.get(dateKey).add(0, metric);
                                    }
                                }
                            }
                        }
                    }
                }

                List<DateHistoryGroup> groupList = new ArrayList<>();
                for (Map.Entry<String, List<HealthMetric>> entry : mapByDate.entrySet()) {
                    groupList.add(new DateHistoryGroup(entry.getKey(), entry.getValue()));
                }

                callback.onDataReceived(groupList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void fetchHistoryData(String userId, DataCallback<List<HealthMetric>> callback) {
        fetchGroupedHistoryData(userId, new DataCallback<List<DateHistoryGroup>>() {
            @Override
            public void onDataReceived(List<DateHistoryGroup> groupList) {
                List<HealthMetric> flatList = new ArrayList<>();
                for (DateHistoryGroup group : groupList) {
                    if (group.getMetricsList() != null) {
                        flatList.addAll(group.getMetricsList());
                    }
                }
                callback.onDataReceived(flatList);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
