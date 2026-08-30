package com.example.spyfall.controller;

import com.example.spyfall.common.ChatMessageRequest;
import com.example.spyfall.common.DataMember;
import com.example.spyfall.service.MaSoiAutoV1;
import com.example.spyfall.service.MaSoiChatService;
import com.example.spyfall.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Chat: controller xử lý các API liên quan đến chat nhóm role.
 */
@RestController
@RequestMapping("/msAutoV1/chat")
public class MaSoiChatController {

    private final MaSoiChatService chatService;
    private final MaSoiAutoV1 maSoiAutoV1;

    public MaSoiChatController(MaSoiChatService chatService, MaSoiAutoV1 maSoiAutoV1) {
        this.chatService = chatService;
        this.maSoiAutoV1 = maSoiAutoV1;
    }

    /**
     * Chat: trả về danh sách các nhóm chat mà member hiện tại được phép xem.
     * Frontend gọi khi load trang để khởi tạo các chat box.
     */
    @GetMapping("/list")
    public List<Map<String, Object>> getChatList(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        // Chat: lấy roleId của member hiện tại để lọc nhóm chat có quyền
        DataMember member = maSoiAutoV1.findPlayer(deviceId).orElse(null);
        if (member == null) {
            return List.of();
        }
        return chatService.getAccessibleChats(member.getId());
    }

    /**
     * Chat: API gửi message vào một nhóm chat.
     * Backend kiểm tra quyền dựa trên roleId của member, không tin tưởng frontend.
     * POST /msAutoV1/chat/send
     * Body: { key, senderName, message }
     */
    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody ChatMessageRequest request,
                                           HttpServletRequest httpRequest,
                                           HttpServletResponse httpResponse) {
        String deviceId = CookieUtil.setCookie(httpRequest.getCookies(), httpResponse).getValue();
        // Chat: lấy roleId thực từ server, không lấy từ request để tránh giả mạo
        DataMember member = maSoiAutoV1.findPlayer(deviceId).orElse(null);
        if (member == null) {
            return Map.of("ok", false, "message", "Người chơi không hợp lệ");
        }
        return chatService.sendMessage(request, member.getId());
    }
}
