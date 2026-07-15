package com.example.spyfall.controller;

import com.example.spyfall.common.GoDuck;
import com.example.spyfall.util.HtmlTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/gd")
public class GoDuckController {
    Map<String, GoDuck> memberPlay = new HashMap<>();
    Map<Integer, GoDuck> memberPlayData = new HashMap<>(); // Data de show ra cho ng dung
    Map<Integer, GoDuck> spyMember = new HashMap<>(); // Danh sach spy
    Map<Integer, GoDuck> spySeeMember = new HashMap<>(); // Danh sach spy nhin thay
    Integer totalPlay = 0; // so luong ng choi
    Integer spyTotalPlay = 0; // so luong spy
    String image ="/gDuck.png";

    private void clearData() {
        spyMember.clear();
        spySeeMember.clear();
        totalPlay = 0;
        spyTotalPlay = 0;
    }

    @GetMapping({"/play"})
    String playGetName() {
        StringBuilder result = new StringBuilder();

        result.append("<div class=\"header-section\">\n")
              .append("    <h1>GoDuck</h1>\n")
              .append("    <p class=\"desc\">Trò chơi suy luận ẩn danh</p>\n")
              .append("</div>\n");

        result.append("<div class=\"card\" style=\"text-align: center;\">\n")
              .append("    <h3 style=\"margin-bottom: 16px;\">Tham gia phòng chơi</h3>\n")
              .append("    <form id=\"dataForm\" onsubmit=\"handleSubmit(event)\">\n")
              .append("        <input type=\"text\" id=\"nameId\" class=\"input-text\" placeholder=\"Nhập tên của bạn\" required style=\"text-align: center; margin-bottom: 20px;\"><br>\n")
              .append("        <button type=\"submit\" class=\"btn btn-primary\">Vào Chơi</button>\n")
              .append("    </form>\n")
              .append("</div>\n");

        result.append("<div class=\"card\" style=\"text-align: center;\">\n")
              .append("    <h3 style=\"margin-bottom: 10px; font-size: 1.1rem;\">Mã QR tham gia</h3>\n")
              .append("    <div class=\"qr-container\">\n")
              .append("        <img src=\"").append(image).append("\" alt=\"QR Code\" class=\"qr-image\">\n")
              .append("    </div>\n")
              .append("</div>\n");

        result.append("<script>\n")
              .append("function handleSubmit(event) {\n")
              .append("    event.preventDefault();\n")
              .append("    var input = document.getElementById(\"nameId\");\n")
              .append("    if (input.value.trim() !== '') {\n")
              .append("        window.location.href = '/gd/play/' + encodeURIComponent(input.value.trim());\n")
              .append("    }\n")
              .append("}\n")
              .append("</script>\n");

        return HtmlTemplate.wrap("GoDuck", result.toString());
    }

    @GetMapping("/play/{name}")
    public String play(HttpServletRequest request, @PathVariable String name) {
        try {
            StringBuilder result = new StringBuilder();
            String clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = request.getRemoteAddr();
            }
            memberPlay.values().forEach(item -> {
                if (spyMember.containsKey(item.getId())) {
                    spyMember.put(item.getId(), item);
                } else if (spySeeMember.containsKey(item.getId())) {
                    spySeeMember.put(item.getId(), item);
                }
            });
            if (memberPlay.containsKey(clientIp)) {
                GoDuck memberDataCheck = memberPlay.get(clientIp);
                memberDataCheck.setUserName(name);
            } else {
                Integer idMember = memberPlay.size() + 1;
                GoDuck member = GoDuck.builder()
                        .ipConfig(clientIp)
                        .userName(name)
                        .id(idMember)
                        .build();
                memberPlay.put(clientIp, member);
            }
            
            result.append("<div class=\"header-section\">\n")
                  .append("    <h1>GoDuck</h1>\n")
                  .append("    <p class=\"desc\">Vai trò & thông tin phòng chơi</p>\n")
                  .append("</div>\n");

            Integer idMemberPlay = memberPlay.get(clientIp).getId();
            
            // Build secret information content
            StringBuilder secretInfo = new StringBuilder();
            if (spyMember.containsKey(idMemberPlay)) {
                for (Map.Entry<Integer, GoDuck> entry : spySeeMember.entrySet()) {
                    if (entry.getValue() != null) {
                        secretInfo.append(entry.getValue().getUserName()).append(" (ID: ").append(entry.getKey()).append("); ");
                    } else {
                        secretInfo.append("ID: ").append(entry.getKey()).append("; ");
                    }
                }
            } else {
                for (Map.Entry<Integer, GoDuck> entry : spyMember.entrySet()) {
                    if (entry.getValue() != null) {
                        secretInfo.append(entry.getValue().getUserName()).append(" (ID: ").append(entry.getKey()).append("); ");
                    } else {
                        secretInfo.append("ID: ").append(entry.getKey()).append("; ");
                    }
                }
            }

            // Click-to-reveal Box
            result.append("<div class=\"card\" style=\"text-align: center;\">\n")
                  .append("    <h3 style=\"margin-bottom: 12px; color: var(--text-muted); font-size: 0.95rem;\">THÔNG TIN MẬT DÀNH CHO BẠN:</h3>\n")
                  .append("    <div class=\"reveal-box blurred\" onclick=\"this.classList.toggle('blurred')\">\n")
                  .append("        <div class=\"reveal-placeholder\">Nhấp để ẩn/hiện thông tin</div>\n")
                  .append("        <div class=\"reveal-content\">\n")
                  .append("            <h2 style=\"font-size: 1.5rem; color: #f87171;\">")
                  .append(secretInfo.toString().isEmpty() ? "Chưa có thông tin" : secretInfo.toString())
                  .append("</h2>\n")
                  .append("        </div>\n")
                  .append("    </div>\n")
                  .append("</div>\n");

            if (totalPlay == 0) {
                result.append("<div class=\"card\" style=\"text-align: center;\">\n")
                      .append("    <h3 style=\"color: var(--warning);\">Đang chờ Admin khởi chạy ván đấu...</h3>\n")
                      .append("</div>\n");
            } else {
                result.append("<div class=\"card\" style=\"display: flex; justify-content: center; gap: 15px; padding: 12px;\">\n")
                      .append("    <span class=\"badge badge-green\" style=\"font-size: 0.85rem;\">Tổng: ").append(totalPlay).append("</span>\n")
                      .append("    <span class=\"badge badge-red\" style=\"font-size: 0.85rem;\">Số Spy: ").append(spyTotalPlay).append("</span>\n")
                      .append("</div>\n");
            }

            // Member list table redesigned to a card list
            result.append("<div class=\"card\">\n")
                  .append("    <h3 style=\"margin-bottom: 12px; text-align: center; font-size: 1.1rem;\">Danh sách người chơi</h3>\n")
                  .append("    <div style=\"display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 0.85rem;\">\n");

            List<GoDuck> memberData = memberPlay.values().stream().sorted(Comparator.comparing(GoDuck::getId)).toList();
            for (GoDuck player : memberData) {
                result.append("        <div style=\"padding: 8px 10px; background: rgba(15, 23, 42, 0.45); border-radius: 8px; border: 1px solid var(--card-border); text-align: left; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;\">")
                      .append("<strong>").append(player.getId()).append(".</strong> ").append(player.getUserName())
                      .append("        </div>\n");
            }

            result.append("    </div>\n")
                  .append("</div>\n");

            // QR Code at footer
            result.append("<div class=\"card\" style=\"text-align: center;\">\n")
                  .append("    <h3 style=\"margin-bottom: 10px; font-size: 1.1rem;\">Mã QR tham gia</h3>\n")
                  .append("    <div class=\"qr-container\">\n")
                  .append("        <img src=\"").append(image).append("\" alt=\"QR Code\" class=\"qr-image\">\n")
                  .append("    </div>\n")
                  .append("</div>\n");

            return HtmlTemplate.wrap("GoDuck Game", result.toString());
        } catch (Exception e) {
            return HtmlTemplate.wrap("GoDuck Game", 
                "<div class=\"card\" style=\"text-align: center;\">\n" +
                "    <h2 style=\"margin-bottom: 10px;\">Đang đồng bộ...</h2>\n" +
                "    <p class=\"desc\">Vui lòng đợi quản trò cài đặt.</p>\n" +
                "    <div class=\"qr-container\" style=\"margin-top: 15px;\">\n" +
                "        <img src=\"/qrcode.png\" alt=\"QR Code\" class=\"qr-image\">\n" +
                "    </div>\n" +
                "</div>");
        }
    }

    @GetMapping("/run/{total}/{spy}")
    public String play(@PathVariable Integer total, @PathVariable(required = false) Integer spy) {
        clearData();
        totalPlay = total;
        if (spy == null) {
            spyTotalPlay = 1;
        } else {
            spyTotalPlay = spy;
        }
        
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= totalPlay; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        for (int i = 0; i < spyTotalPlay; i++) {
            spyMember.put(numbers.remove(0), null);
            Collections.shuffle(numbers);
        }

        for (int i = 0; i < spyTotalPlay; i++) {
            spySeeMember.put(numbers.remove(0), null);
            Collections.shuffle(numbers);
        }

        String content = 
            "<div class=\"header-section\">\n" +
            "    <h1>GoDuck Setup</h1>\n" +
            "    <p class=\"desc\" style=\"color: var(--success); font-weight: 500;\">Thiết lập phòng chơi thành công!</p>\n" +
            "</div>\n" +
            "\n" +
            "<div class=\"card\" style=\"text-align: center;\">\n" +
            "    <div class=\"qr-container\">\n" +
            "        <img src=\"" + image + "\" alt=\"QR Code\" class=\"qr-image\">\n" +
            "    </div>\n" +
            "    <div style=\"margin-top: 20px; display: flex; flex-direction: column; gap: 8px; font-size: 0.85rem;\">\n" +
            "        <div class=\"item-row\"><span>Tổng người chơi:</span> <strong>" + total + "</strong></div>\n" +
            "        <div class=\"item-row\"><span>Số lượng Spy:</span> <strong>" + spyTotalPlay + "</strong></div>\n" +
            "    </div>\n" +
            "    <a href=\"/gd/play\" class=\"btn btn-primary\" style=\"margin-top: 20px;\">Vào Phòng Chơi</a>\n" +
            "</div>";

        return HtmlTemplate.wrap("GoDuck Setup", content);
    }
}
