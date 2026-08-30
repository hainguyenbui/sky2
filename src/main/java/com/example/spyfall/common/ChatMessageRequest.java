package com.example.spyfall.common;

import lombok.Getter;
import lombok.Setter;

/**
 * Chat: DTO nhận request gửi message từ frontend.
 */
@Getter
@Setter
public class ChatMessageRequest {
    // Chat: key xác định nhóm chat (ví dụ "wolf", "village")
    private String key;
    // Chat: tên người gửi lấy từ member.nameMember
    private String senderName;
    // Chat: nội dung message người dùng nhập
    private String message;
}
