package com.example.spyfall.common;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Chat: đại diện cho một nhóm chat độc lập.
 * Gộp 3 Map (title, messages, roleIds) thành 1 object duy nhất để dễ quản lý.
 */
@Getter
public class ChatGroup {

    // Chat: tiêu đề hiển thị trên header của chat box
    private final String title;

    // Chat: màu hex của tiêu đề (ví dụ "#ff0000")
    private final String color;

    // Chat: danh sách roleId được phép xem và gửi message trong nhóm này
    private final Set<Integer> allowedRoleIds;

    // Chat: danh sách message đã gửi, mỗi phần tử có dạng "Tên: nội dung"
    private final List<String> messages = new ArrayList<>();

    public ChatGroup(String title, String color, Set<Integer> allowedRoleIds) {
        this.title = title;
        this.color = color;
        this.allowedRoleIds = allowedRoleIds;
    }

    // Chat: kiểm tra roleId có được phép truy cập nhóm chat này không
    public boolean isAllowed(int roleId) {
        return allowedRoleIds.contains(roleId);
    }

    // Chat: thêm message mới vào cuối danh sách
    public void addMessage(String message) {
        messages.add(message);
    }

    // Chat: lấy message cuối cùng để hiển thị khi chat thu nhỏ
    public String getLastMessage() {
        return messages.isEmpty() ? "" : messages.get(messages.size() - 1);
    }
}
