package com.example.spyfall.service;

import com.example.spyfall.common.ChatGroup;
import com.example.spyfall.common.ChatMessageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Chat: service quản lý toàn bộ logic chat theo nhóm role.
 * Dùng 1 Map<String, ChatGroup> thay vì 3 Map riêng lẻ.
 */
@Service
public class MaSoiChatService {

    // Chat: prefix topic WebSocket để broadcast message realtime
    private static final String CHAT_TOPIC_PREFIX = "/topic/chat/";

    private final SimpMessagingTemplate messagingTemplate;
    private final MaSoiService maSoiService;

    // Chat: map chính chứa tất cả nhóm chat, key → ChatGroup
    private final Map<String, ChatGroup> chatGroups = new LinkedHashMap<>();

    public MaSoiChatService(SimpMessagingTemplate messagingTemplate, MaSoiService maSoiService) {
        this.messagingTemplate = messagingTemplate;
        this.maSoiService = maSoiService;
    }

    /**
     * Chat: khởi tạo các nhóm chat khi bắt đầu game mới.
     * Gọi từ MaSoiService sau khi loadGame thành công.
     * Định nghĩa cứng các nhóm: Sói, Dân, Toàn bộ.
     */
    public void initChatGroups() {
        chatGroups.clear();

        // Chat: nhóm Sói – chỉ các roleId sói (1-14, trừ 15 là nội gián)
        chatGroups.put("wolf", new ChatGroup(
                "Phe Sói 🐺",
                "#f87171",
                new LinkedHashSet<>(MaSoiService.WOLF_ROLE_IDS.subList(0, MaSoiService.WOLF_ROLE_IDS.size() - 1))
        ));

        // Chat: nhóm toàn bộ – tất cả roleId đều được phép (dùng Set rỗng = không giới hạn, xử lý riêng)
        // Dùng Set chứa tất cả id từ 1-99 để đơn giản
        Set<Integer> allRoles = new LinkedHashSet<>();
        for (int i = 1; i <= 99; i++) allRoles.add(i);
        chatGroups.put("all", new ChatGroup("Tất cả 💬", "#60a5fa", allRoles));
    }

    /**
     * Chat: trả về danh sách các nhóm chat mà roleId được phép xem.
     * Mỗi entry gồm key, title, color, messages, lastMessage.
     */
    public List<Map<String, Object>> getAccessibleChats(int roleId) {
        List<Map<String, Object>> result = new ArrayList<>();
        // Chat: duyệt qua tất cả nhóm, chỉ trả về nhóm mà roleId có quyền
        for (Map.Entry<String, ChatGroup> entry : chatGroups.entrySet()) {
            ChatGroup group = entry.getValue();
            if (!group.isAllowed(roleId)) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", entry.getKey());
            item.put("title", group.getTitle());
            item.put("color", group.getColor());
            item.put("messages", new ArrayList<>(group.getMessages()));
            item.put("lastMessage", group.getLastMessage());
            result.add(item);
        }
        return result;
    }

    /**
     * Chat: xử lý gửi message.
     * Kiểm tra quyền → lưu message → broadcast WebSocket tới topic của key.
     */
    public Map<String, Object> sendMessage(ChatMessageRequest request, int senderRoleId) {
        String key = request.getKey();
        ChatGroup group = chatGroups.get(key);

        // Chat: kiểm tra nhóm chat tồn tại
        if (group == null) {
            return errorResponse("Nhóm chat không tồn tại");
        }

        // Chat: kiểm tra quyền truy cập ở backend, không tin tưởng frontend
        if (!group.isAllowed(senderRoleId)) {
            return errorResponse("Bạn không có quyền gửi vào nhóm chat này");
        }

        // Chat: tạo message theo format "Tên: nội dung"
        String formatted = request.getSenderName() + ": " + request.getMessage();

        // Chat: lưu message vào danh sách của nhóm
        group.addMessage(formatted);

        // Chat: tạo payload broadcast gồm key + message mới để FE cập nhật đúng chat box
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("key", key);
        payload.put("message", formatted);
        payload.put("lastMessage", formatted);

        // Chat: broadcast realtime tới /topic/chat/{key} – chỉ client subscribe đúng key mới nhận
        messagingTemplate.convertAndSend(CHAT_TOPIC_PREFIX + key, payload);

        return payload;
    }

    /**
     * Chat: reset toàn bộ chat khi game mới bắt đầu.
     */
    public void resetChats() {
        chatGroups.clear();
    }

    // Chat: helper trả về error response
    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("ok", false);
        error.put("message", message);
        return error;
    }
}
