package com.example.spyfall.controller;

import com.example.spyfall.common.Spy2;
import com.example.spyfall.service.DataInputService;
import com.example.spyfall.util.HtmlTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/spy2")
public class Spy2Controller {

    List<List<String>> keywords = new ArrayList<>();
    List<Spy2> dataWillPlay = new ArrayList<>();
    List<Spy2> dataPlayer = new ArrayList<>();
    List<Spy2> dataSpies = new ArrayList<>();
    List<Spy2> removed = new ArrayList<>();
    List<Spy2> dataMainSpy2 = new ArrayList<>();
    Spy2 dataSpy;
    Spy2 dataNotSpy;
    String image ="/spy2.png";

    Integer countnumSpies = 0;
    Integer countWhite = 0;

    @Autowired
    DataInputService dataInputService;

    @GetMapping
    String playSpy2() {
        StringBuilder result = new StringBuilder();

        result.append("<div class=\"header-section\">\n")
              .append("    <h1>Undercover</h1>\n")
              .append("    <p class=\"desc\">Phát hiện kẻ giữ từ khóa khác biệt</p>\n")
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
              .append("        window.location.href = '/spy2/play/' + encodeURIComponent(input.value.trim());\n")
              .append("    }\n")
              .append("}\n")
              .append("</script>\n");

        return HtmlTemplate.wrap("Undercover", result.toString());
    }

    @GetMapping("/play/{name}")
    String playSpy2(HttpServletRequest request, @PathVariable String name) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        System.out.println(clientIp);
        Integer ipAssigned = null;
        try {
            if (!dataPlayer.isEmpty()) {
                for (Spy2 dataMember : dataPlayer) {
                    if (dataMember.getIpConfig().equals(clientIp)) {
                        ipAssigned = dataPlayer.indexOf(dataMember);
                        if (!Objects.equals(dataMember.getUserName(), name)) {
                            dataMember.setUserName(name);
                        }
                        break;
                    }
                }
            }
            StringBuilder result = new StringBuilder();
            
            result.append("<div class=\"header-section\">\n")
                  .append("    <h1>Undercover</h1>\n")
                  .append("    <p class=\"desc\">Ghi nhớ từ khóa của bạn để giải thích</p>\n")
                  .append("</div>\n");

            String keywordText = "";
            if (ipAssigned != null) {
                keywordText = dataPlayer.get(ipAssigned).getKeyword();
            } else {
                if (dataWillPlay.isEmpty()) {
                    return HtmlTemplate.wrap("Undercover", 
                        "<div class=\"card\" style=\"text-align: center;\">\n" +
                        "    <h2 style=\"color: var(--danger); margin-bottom: 10px;\">Ván đấu chưa được setup</h2>\n" +
                        "    <p class=\"desc\">Vui lòng liên hệ Admin để thiết lập phòng chơi mới.</p>\n" +
                        "    <a href=\"/\" class=\"btn btn-primary\" style=\"margin-top: 20px;\">Về Trang Chủ</a>\n" +
                        "</div>");
                }
                Collections.shuffle(dataWillPlay);
                Spy2 yourLocation = dataWillPlay.get(0);
                yourLocation.setUserName(name);
                yourLocation.setIpConfig(clientIp);
                dataPlayer.add(yourLocation);
                dataWillPlay.remove(0);
                System.out.println(yourLocation.getKeyword());
                keywordText = yourLocation.getKeyword();
            }

            // Click-to-reveal Box
            result.append("<div class=\"card\" style=\"text-align: center;\">\n")
                  .append("    <h3 style=\"margin-bottom: 12px; color: var(--text-muted); font-size: 0.95rem;\">TỪ KHÓA CỦA BẠN:</h3>\n")
                  .append("    <div class=\"reveal-box blurred\" onclick=\"this.classList.toggle('blurred')\">\n")
                  .append("        <div class=\"reveal-placeholder\">Nhấp để ẩn/hiện từ khóa</div>\n")
                  .append("        <div class=\"reveal-content\">\n")
                  .append("            <h2 style=\"font-size: 1.8rem; color: #60a5fa;\">").append(keywordText).append("</h2>\n")
                  .append("        </div>\n")
                  .append("    </div>\n")
                  .append("</div>\n");

            // Game metadata cards
            result.append("<div class=\"card\" style=\"display: flex; flex-direction: column; gap: 8px; font-size: 0.85rem;\">\n")
                  .append("    <div class=\"item-row\"><span>Số lượng Spy:</span> <strong>").append(countnumSpies).append("</strong></div>\n")
                  .append("    <div class=\"item-row\"><span>Số lượng Mũ Trắng:</span> <strong>").append(countWhite).append("</strong></div>\n")
                  .append("    <div class=\"item-row\"><span>Số người còn lại:</span> <strong>").append(dataPlayer.size() - removed.size()).append("</strong></div>\n")
                  .append("</div>\n");

            // Removed players section
            if (!removed.isEmpty()) {
                result.append("<div class=\"card\">\n")
                      .append("    <h3 style=\"margin-bottom: 12px; text-align: center; color: var(--danger); font-size: 1.1rem;\">Danh sách đã bị loại</h3>\n")
                      .append("    <div class=\"item-list\">\n");

                for (Spy2 player : removed) {
                    String badgeClass = "badge-green";
                    if ("Spy".equals(player.getRole())) badgeClass = "badge-red";
                    else if ("Mũ Trắng".equals(player.getRole())) badgeClass = "badge-orange";
                    
                    result.append("        <div class=\"item-row\" style=\"background: rgba(239, 68, 68, 0.08); border-color: rgba(239, 68, 68, 0.15);\">\n")
                          .append("            <span style=\"text-decoration: line-through; color: var(--text-muted); font-weight: 500;\">").append(player.getUserName().toUpperCase()).append("</span>\n")
                          .append("            <span class=\"badge ").append(badgeClass).append("\">").append(player.getRole()).append("</span>\n")
                          .append("        </div>\n");
                }

                result.append("    </div>\n")
                      .append("</div>\n");
            }

            // QR Code Section
            result.append("<div class=\"card\" style=\"text-align: center;\">\n")
                  .append("    <h3 style=\"margin-bottom: 10px; font-size: 1.1rem;\">Mã QR tham gia</h3>\n")
                  .append("    <div class=\"qr-container\">\n")
                  .append("        <img src=\"").append(image).append("\" alt=\"QR Code\" class=\"qr-image\">\n")
                  .append("    </div>\n")
                  .append("</div>\n");

            return HtmlTemplate.wrap("Undercover", result.toString());
        } catch (Exception e) {
            return HtmlTemplate.wrap("Undercover", 
                "<div class=\"card\" style=\"text-align: center;\">\n" +
                "    <h2 style=\"margin-bottom: 10px;\">Đang đồng bộ...</h2>\n" +
                "    <p class=\"desc\">Đợi xí nha đại ca.</p>\n" +
                "    <div class=\"qr-container\" style=\"margin-top: 15px;\">\n" +
                "        <img src=\"/qrcode.png\" alt=\"QR Code\" class=\"qr-image\">\n" +
                "    </div>\n" +
                "</div>");
        }
    }

    @GetMapping("/setup/{totalPlayers}/{numSpies}/{numWhite}")
    String setupSpy2(@PathVariable int totalPlayers, @PathVariable int numSpies, @PathVariable int numWhite) {
        Random random = new Random();
        dataWillPlay.clear(); // clear data
        dataMainSpy2.clear();
        dataPlayer.clear();
        removed.clear();
        if(keywords.isEmpty()) {
            keywords.addAll(dataInputService.createDataSpy2());
        }
        int dataget = random.nextInt(keywords.size());
        checkDuplicatePairs();
        for (String spydata : keywords.get(dataget)) {
            dataMainSpy2.add(Spy2.builder().keyword(spydata).build());
        }
        keywords.remove(dataget);

        countnumSpies = numSpies;
        countWhite = numWhite;

        int indexMember = random.nextInt(dataMainSpy2.size());
        dataNotSpy = dataMainSpy2.get(indexMember);

        dataSpies = new ArrayList<>(dataMainSpy2);
        dataSpies.remove(indexMember);

        int indexSpy = random.nextInt(dataSpies.size());
        dataSpy = dataSpies.get(indexSpy);

        int cnt = 1;
        for (int i = 0; i < totalPlayers - (numSpies + numWhite); i++) {
            dataWillPlay.add(createPlayer(cnt, dataNotSpy.getKeyword(), "Dân Thường"));
            cnt++;
        }
        for (int i = 0; i < numSpies; i++) {
            dataWillPlay.add(createPlayer(cnt, dataSpy.getKeyword(), "Spy"));
            cnt++;
        }

        for (int i = 0; i < numWhite; i++) {
            dataWillPlay.add(createPlayer(cnt, "Bạn là mũ trắng", "Mũ Trắng"));
            cnt++;
        }

        StringBuilder content = new StringBuilder();
        content.append("<div class=\"header-section\">\n")
               .append("    <h1>Undercover Setup</h1>\n")
               .append("    <p class=\"desc\" style=\"color: var(--success); font-weight: 500;\">Khởi tạo thành công!</p>\n")
               .append("</div>\n");

        content.append("<div class=\"card\" style=\"text-align: center;\">\n")
               .append("    <div class=\"qr-container\">\n")
               .append("        <img src=\"").append(image).append("\" alt=\"QR Code\" class=\"qr-image\">\n")
               .append("    </div>\n")
               .append("    <div style=\"margin-top: 15px; display: flex; flex-direction: column; gap: 8px; font-size: 0.85rem;\">\n")
               .append("        <div class=\"item-row\"><span>Tổng người chơi:</span> <strong>").append(totalPlayers).append("</strong></div>\n")
               .append("        <div class=\"item-row\"><span>Số lượng Spy:</span> <strong>").append(numSpies).append("</strong></div>\n")
               .append("        <div class=\"item-row\"><span>Số lượng Mũ Trắng:</span> <strong>").append(numWhite).append("</strong></div>\n")
               .append("    </div>\n")
               .append("    <a href=\"/spy2\" class=\"btn btn-primary\" style=\"margin-top: 20px;\">Vào Phòng Chơi</a>\n")
               .append("</div>\n");

        return HtmlTemplate.wrap("Undercover Setup", content.toString());
    }

    @GetMapping("/remove/{id}")
    String removePlayer(@PathVariable int id) {
        for (Spy2 player : dataPlayer) {
            if (player.getId() == id) {
                player.setRemove(true);
                if (!removed.contains(player)) {
                    removed.add(player);
                }
            }
        }
        return "Success";
    }

    @GetMapping("/removeList")
    String listRemove () {
        StringBuilder result = new StringBuilder();
        
        result.append("<div class=\"header-section\">\n")
              .append("    <h1>Quản Trị Viên</h1>\n")
              .append("    <p class=\"desc\">Quản lý và loại bỏ người chơi</p>\n")
              .append("</div>\n");

        result.append("<div class=\"card\">\n")
              .append("    <h3 style=\"margin-bottom: 12px; font-size: 1.1rem;\">Danh sách phòng chơi</h3>\n")
              .append("    <div class=\"item-list\">\n");

        for (Spy2 player : dataPlayer) {
            if (player.isRemove()) {
                result.append("        <div class=\"item-row\" style=\"opacity: 0.6; background: rgba(239, 68, 68, 0.05);\">\n")
                      .append("            <span style=\"text-decoration: line-through; color: var(--text-muted); font-weight: 500;\">")
                      .append(player.getId()).append(". ").append(player.getUserName())
                      .append("            </span>\n")
                      .append("            <span class=\"badge badge-red\">Đã Loại</span>\n")
                      .append("        </div>\n");
            } else {
                result.append("        <div class=\"item-row\">\n")
                      .append("            <span style=\"font-weight: 600;\">")
                      .append(player.getId()).append(". ").append(player.getUserName())
                      .append("            </span>\n")
                      .append("            <span class=\"badge badge-green\">Đang Sống</span>\n")
                      .append("        </div>\n");
            }
        }
        result.append("    </div>\n")
              .append("</div>\n");

        if (!removed.isEmpty()) {
            result.append("<div class=\"card\">\n")
                  .append("    <h3 style=\"margin-bottom: 12px; color: var(--danger); font-size: 1.1rem;\">Danh tính người bị loại</h3>\n")
                  .append("    <div class=\"item-list\">\n");

            for (Spy2 player : removed) {
                String badgeClass = "badge-green";
                if ("Spy".equals(player.getRole())) badgeClass = "badge-red";
                else if ("Mũ Trắng".equals(player.getRole())) badgeClass = "badge-orange";
                
                result.append("        <div class=\"item-row\" style=\"background: rgba(239, 68, 68, 0.08);\">\n")
                      .append("            <span style=\"font-weight: 500;\">").append(player.getUserName().toUpperCase()).append("</span>\n")
                      .append("            <span class=\"badge ").append(badgeClass).append("\">").append(player.getRole()).append("</span>\n")
                      .append("        </div>\n");
            }
            result.append("    </div>\n")
                  .append("</div>\n");
        }

        // Elimination form
        result.append("<div class=\"card\" style=\"text-align: center;\">\n")
              .append("    <h3 style=\"margin-bottom: 12px; font-size: 1.1rem;\">Bắn bỏ người chơi</h3>\n")
              .append("    <div style=\"display: flex; gap: 8px; justify-content: center;\">\n")
              .append("        <input type=\"number\" id=\"numberInput\" class=\"input-text\" placeholder=\"Nhập ID\" style=\"width: 100px; text-align: center;\">\n")
              .append("        <button id=\"okButton\" class=\"btn btn-danger\" style=\"width: auto; padding: 12px 20px;\">OK</button>\n")
              .append("    </div>\n")
              .append("</div>\n");

        result.append("<script>\n")
              .append("document.getElementById(\"okButton\").addEventListener(\"click\", function() {\n")
              .append("    var number = document.getElementById(\"numberInput\").value;\n")
              .append("    if(number === \"\") { alert(\"Vui lòng nhập ID người chơi!\"); return; }\n")
              .append("    var url = \"/spy2/remove/\" + encodeURIComponent(number);\n")
              .append("    fetch(url)\n")
              .append("        .then(response => {\n")
              .append("            if(response.ok){ \n")
              .append("                alert(\"Bắn bỏ thành công!\"); \n")
              .append("                window.location.reload();\n")
              .append("            } else { \n")
              .append("                alert(\"Lỗi: \" + response.status); \n")
              .append("            } \n")
              .append("        })\n")
              .append("        .catch(error => { alert(\"Gửi yêu cầu thất bại: \" + error); });\n")
              .append("});\n")
              .append("</script>\n");

        return HtmlTemplate.wrap("Elimination Panel", result.toString());
    }

    private Spy2 createPlayer(int id, String keyword, String role) {
        return Spy2.builder()
                .id(id)
                .keyword(keyword)
                .role(role)
                .build();
    }

    public void checkDuplicatePairs() {
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        for (List<String> pair : keywords) {
            String a = pair.get(0).trim();
            String b = pair.get(1).trim();
            String key = a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
            if (seen.contains(key)) {
                duplicates.add(key);
            } else {
                seen.add(key);
            }
        }
        if (duplicates.isEmpty()) {
            System.out.println("Không có cặp nào bị lặp lại.");
        } else {
            System.out.println("Các cặp bị lặp lại (bất kể thứ tự):");
            for (String dup : duplicates) {
                System.out.println(dup.replace("|", " - "));
            }
        }
    }
}
