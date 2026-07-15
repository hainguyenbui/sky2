package com.example.spyfall.controller;

import com.example.spyfall.util.HtmlTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @GetMapping()
    String test() {
        String content = 
            "<div class=\"header-section\">\n" +
            "    <h1>Game Hub</h1>\n" +
            "    <p class=\"desc\">Chọn trò chơi để bắt đầu cuộc vui cùng bạn bè</p>\n" +
            "</div>\n" +
            "\n" +
            "<div class=\"card\" style=\"margin-bottom: 16px;\">\n" +
            "    <h2 style=\"margin-bottom: 6px;\">🕵️ Spyfall (Gián điệp v1)</h2>\n" +
            "    <p class=\"desc\" style=\"margin-bottom: 16px;\">Mọi người cùng ở một địa điểm, trừ Spy. Hãy đặt câu hỏi khéo léo để tìm ra gián điệp!</p>\n" +
            "    <div style=\"display: flex; flex-direction: column; gap: 8px;\">\n" +
            "        <a href=\"/sp\" class=\"btn btn-primary\">Vào Phòng Chơi</a>\n" +
            "        <a href=\"/sp/2/4\" class=\"btn btn-success\" style=\"font-size: 0.85rem; padding: 10px;\">Setup Nhanh: 4 Người / 1 Spy ẩn danh</a>\n" +
            "    </div>\n" +
            "</div>\n" +
            "\n" +
            "<div class=\"card\" style=\"margin-bottom: 16px;\">\n" +
            "    <h2 style=\"margin-bottom: 6px;\">🤫 Undercover (Gián điệp v2)</h2>\n" +
            "    <p class=\"desc\" style=\"margin-bottom: 16px;\">Phát hiện kẻ giữ từ khóa khác biệt (Undercover) hoặc không có từ khóa (Mũ trắng).</p>\n" +
            "    <div style=\"display: flex; flex-direction: column; gap: 8px;\">\n" +
            "        <a href=\"/spy2\" class=\"btn btn-primary\">Vào Phòng Chơi</a>\n" +
            "        <a href=\"/spy2/setup/4/1/1\" class=\"btn btn-success\" style=\"font-size: 0.85rem; padding: 10px;\">Setup Nhanh: 4 Người / 1 Spy / 1 Mũ trắng</a>\n" +
            "    </div>\n" +
            "</div>\n" +
            "\n" +
            "<div class=\"card\" style=\"margin-bottom: 16px;\">\n" +
            "    <h2 style=\"margin-bottom: 6px;\">🐺 Ma Sói (Werewolf)</h2>\n" +
            "    <p class=\"desc\" style=\"margin-bottom: 16px;\">Dân làng tìm kiếm bầy Sói hung tợn ẩn mình mỗi đêm. Trò chơi ẩn vai kinh điển.</p>\n" +
            "    <div style=\"display: flex; flex-direction: column; gap: 8px;\">\n" +
            "        <a href=\"/ms/play\" class=\"btn btn-primary\">Vào Phòng Chơi</a>\n" +
            "        <a href=\"/ms/run\" class=\"btn btn-danger\">Thiết Lập Ván Đấu (Admin)</a>\n" +
            "    </div>\n" +
            "</div>\n" +
            "\n" +
            "<div class=\"card\" style=\"margin-bottom: 16px;\">\n" +
            "    <h2 style=\"margin-bottom: 6px;\">🦆 GoDuck</h2>\n" +
            "    <p class=\"desc\" style=\"margin-bottom: 16px;\">Trò chơi suy luận thông tin bất đối xứng nhanh gọn.</p>\n" +
            "    <div style=\"display: flex; flex-direction: column; gap: 8px;\">\n" +
            "        <a href=\"/gd/play\" class=\"btn btn-primary\">Vào Phòng Chơi</a>\n" +
            "        <a href=\"/gd/run/4/1\" class=\"btn btn-success\" style=\"font-size: 0.85rem; padding: 10px;\">Setup Nhanh: 4 Người / 1 Spy</a>\n" +
            "    </div>\n" +
            "</div>";
        
        return HtmlTemplate.wrap("Game Hub", content);
    }
}
