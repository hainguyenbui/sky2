package com.example.spyfall.controller;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.SoiNguyenDto;
import com.example.spyfall.util.HtmlTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/ms")
public class MaSoiController {
    List<DataMember> datas = new ArrayList<>();
    List<DataMember> pls = new ArrayList<>();
    List<String> idSoi = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15");
    List<String> idOutsider = List.of("21", "20", "22");
    List<DataMember> listShowForMember = new ArrayList<>();
    boolean checkDecreaseSoi = false;
    boolean isGameEnd = true;
    boolean isAddSoiNguyen = false;
    boolean isHaveSoiNguyen = false;
    Map<Integer, List<DataMember>> detailEachGame = new TreeMap<>(Comparator.reverseOrder());
    String image = "/qrcode.png";


    @GetMapping("/run")
    String create() {
        prepareData();
        StringBuilder result = new StringBuilder();

        result.append("<div class=\"header-section\">\n")
              .append("    <h1>Ma Sói Setup</h1>\n")
              .append("    <p class=\"desc\">Thiết lập vai trò cho ván đấu mới</p>\n")
              .append("</div>\n");

        result.append("<form id=\"dataForm\">\n");

        // Player total setup card
        result.append("    <div class=\"card\" style=\"margin-bottom: 16px;\">\n")
              .append("        <h3 style=\"margin-bottom: 12px; font-size: 1.1rem;\">Thông tin phòng chơi</h3>\n")
              .append("        <div style=\"display: flex; flex-direction: column; gap: 12px;\">\n")
              .append("            <div style=\"display: flex; justify-content: space-between; align-items: center;\">\n")
              .append("                <span style=\"font-weight: 500;\">Tổng người chơi:</span>\n")
              .append("                <input type=\"number\" min=\"0\" value=\"0\" id=\"total\" name=\"total\" class=\"input-text\" style=\"width: 80px; padding: 8px; text-align: center;\">\n")
              .append("            </div>\n")
              .append("            <div style=\"display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--card-border); padding-top: 12px;\">\n")
              .append("                <span style=\"font-weight: 500;\">Đã chọn:</span>\n")
              .append("                <div style=\"display: flex; align-items: baseline; gap: 4px;\">\n")
              .append("                    <input type=\"text\" id=\"sum\" readonly value=\"0\" style=\"width: 40px; background: transparent; border: none; color: #60a5fa; font-weight: 800; font-size: 1.5rem; text-align: right; outline: none;\">\n")
              .append("                    <span style=\"color: var(--text-muted); font-size: 0.9rem;\">vai trò</span>\n")
              .append("                </div>\n")
              .append("            </div>\n")
              .append("        </div>\n")
              .append("    </div>\n");

        // Roles checklist card
        result.append("    <div class=\"card\" style=\"margin-bottom: 16px;\">\n")
              .append("        <h3 style=\"margin-bottom: 14px; font-size: 1.1rem;\">Danh sách vai trò</h3>\n")
              .append("        <div class=\"item-list\">\n");

        for (int i = 0; i < datas.size(); i++) {
            addRoleCard(result, datas.get(i));
        }

        result.append("        </div>\n")
              .append("    </div>\n");

        // Custom config card (Soi Nguyen, Cupid)
        result.append("    <div class=\"card\" style=\"margin-bottom: 20px;\">\n")
              .append("        <h3 style=\"margin-bottom: 12px; font-size: 1.1rem;\">Cấu hình nâng cao</h3>\n")
              .append("        <div style=\"display: flex; flex-direction: column; gap: 12px;\">\n")
              .append("            <div class=\"role-card\" style=\"border-left: 4px solid var(--warning);\">\n")
              .append("                <div>\n")
              .append("                    <span class=\"role-title\">SÓI NGUYỀN CHÚC</span>\n")
              .append("                    <p style=\"font-size: 0.75rem; color: var(--text-muted);\">Cho phép Sói nguyền hóa 1 người chơi thành Sói</p>\n")
              .append("                </div>\n")
              .append("                <label class=\"switch\">\n")
              .append("                    <input type=\"checkbox\" id=\"checkSoi\" name=\"checkSoi\" value=\"1\">\n")
              .append("                    <span class=\"slider\"></span>\n")
              .append("                </label>\n")
              .append("            </div>\n")
              .append("            <div class=\"role-card\" style=\"border-left: 4px solid var(--warning);\">\n")
              .append("                <div>\n")
              .append("                    <span class=\"role-title\">CUPID (TÌNH YÊU)</span>\n")
              .append("                    <p style=\"font-size: 0.75rem; color: var(--text-muted);\">Ghép đôi ngẫu nhiên 2 người chơi</p>\n")
              .append("                </div>\n")
              .append("                <label class=\"switch\">\n")
              .append("                    <input type=\"checkbox\" id=\"checkCupid\" name=\"checkCupid\" value=\"1\">\n")
              .append("                    <span class=\"slider\"></span>\n")
              .append("                </label>\n")
              .append("            </div>\n")
              .append("        </div>\n")
              .append("    </div>\n");

        result.append("    <button type=\"button\" id=\"sendButton\" class=\"btn btn-primary\" style=\"margin-top: 10px;\">Khởi Chạy Game</button>\n");
        result.append("</form>\n");

        result.append("<script>\n")
              .append("function updateSum() {\n")
              .append("    var sumTotal = 0;\n")
              .append("    var inputs = document.querySelectorAll('input[type=\"number\"]:not(#total)');\n")
              .append("    inputs.forEach(function(input) {\n")
              .append("        sumTotal += parseFloat(input.value) || 0;\n")
              .append("    });\n")
              .append("    var checkboxes = document.querySelectorAll('input[type=\"checkbox\"]:not(#checkSoi):not(#checkCupid):checked');\n")
              .append("    checkboxes.forEach(function(checkbox) {\n")
              .append("        sumTotal += parseInt(checkbox.value) || 0;\n")
              .append("    });\n")
              .append("    document.getElementById('sum').value = sumTotal;\n")
              .append("}\n")
              .append("\n")
              .append("document.getElementById('sendButton').onclick = function() {\n")
              .append("    var formData = new URLSearchParams();\n")
              .append("    var inputs = document.querySelectorAll('input[type=\"number\"]');\n")
              .append("    inputs.forEach(function(input) {\n")
              .append("        formData.append(input.name, input.value);\n")
              .append("    });\n")
              .append("    var checkboxes = document.querySelectorAll('input[type=\"checkbox\"]:checked');\n")
              .append("    checkboxes.forEach(function(checkbox) {\n")
              .append("        if (checkbox.id !== 'checkSoi' && checkbox.id !== 'checkCupid') {\n")
              .append("            formData.append(checkbox.name, 1);\n")
              .append("        }\n")
              .append("    });\n")
              .append("    formData.append('checkSoi', document.getElementById('checkSoi').checked ? 'true' : 'false');\n")
              .append("    formData.append('checkCupid', document.getElementById('checkCupid').checked ? 'true' : 'false');\n")
              .append("\n")
              .append("    fetch('/ms/create?' + formData.toString(), { method: 'GET' })\n")
              .append("        .then(response => response.text())\n")
              .append("        .then(data => {\n")
              .append("            if (data.includes('ERROR')) {\n")
              .append("                alert(data);\n")
              .append("            } else {\n")
              .append("                window.location.href = '/ms/admin';\n")
              .append("            }\n")
              .append("        })\n")
              .append("        .catch(error => console.error('Error:', error));\n")
              .append("};\n")
              .append("</script>\n");

        String styles = 
            "<style>\n" +
            "  .role-card {\n" +
            "      display: flex;\n" +
            "      justify-content: space-between;\n" +
            "      align-items: center;\n" +
            "      padding: 12px 16px;\n" +
            "      background: rgba(30, 41, 59, 0.4);\n" +
            "      border: 1px solid var(--card-border);\n" +
            "      border-radius: 12px;\n" +
            "      transition: all 0.2s;\n" +
            "  }\n" +
            "  .role-title {\n" +
            "      font-weight: 700;\n" +
            "      font-size: 0.9rem;\n" +
            "      letter-spacing: 0.02em;\n" +
            "  }\n" +
            "  .role-card-soi { border-left: 4px solid var(--danger); }\n" +
            "  .role-card-dan { border-left: 4px solid var(--success); }\n" +
            "  .role-card-other { border-left: 4px solid var(--warning); }\n" +
            "  .switch {\n" +
            "      position: relative;\n" +
            "      display: inline-block;\n" +
            "      width: 46px;\n" +
            "      height: 24px;\n" +
            "      flex-shrink: 0;\n" +
            "  }\n" +
            "  .switch input { opacity: 0; width: 0; height: 0; }\n" +
            "  .slider {\n" +
            "      position: absolute;\n" +
            "      cursor: pointer;\n" +
            "      top: 0; left: 0; right: 0; bottom: 0;\n" +
            "      background-color: rgba(15, 23, 42, 0.6);\n" +
            "      transition: .25s;\n" +
            "      border-radius: 24px;\n" +
            "      border: 1px solid var(--card-border);\n" +
            "  }\n" +
            "  .slider:before {\n" +
            "      position: absolute;\n" +
            "      content: \"\";\n" +
            "      height: 16px;\n" +
            "      width: 16px;\n" +
            "      left: 3px;\n" +
            "      bottom: 3px;\n" +
            "      background-color: #94a3b8;\n" +
            "      transition: .25s;\n" +
            "      border-radius: 50%;\n" +
            "  }\n" +
            "  input:checked + .slider { background-color: var(--primary); }\n" +
            "  input:checked + .slider:before {\n" +
            "      transform: translateX(22px);\n" +
            "      background-color: #ffffff;\n" +
            "  }\n" +
            "</style>";

        return HtmlTemplate.wrap("Ma Sói Setup", result.toString(), styles);
    }

    @GetMapping("/create")
    String loadData(@RequestParam Map<String, String> params) {
        if (isGameEnd) {

            int totalPlay = Integer.parseInt(params.get("total"));
            boolean isSoiNguyen = Boolean.parseBoolean(params.get("checkSoi"));
            boolean isCupid = Boolean.parseBoolean(params.get("checkCupid"));
            int totalRole = 0;
            if (totalPlay == 0) {
                return "ERROR LỖI KHÔNG TỔNG NGƯỜI";
            }
            isGameEnd = false;
            prepareData();
            pls.clear();
            listShowForMember.clear();
            checkDecreaseSoi = false;

            for (DataMember data :datas) {
                int total = 0;
                if (params.get(data.getId()) != null) {
                    total = Integer.parseInt(params.get(data.getId())); // id la khoa chinh
                }
                if (total >= 1) {
                    listShowForMember.add(createDM(data, total));
                }
                for (int i = 0; i < total; i++) {
                    data.setIpData("play");
                    pls.add(DataMember.builder()
                            .id(data.getId())
                            .idPlayGame(String.valueOf(pls.size() + 1))
                            .location(data.getLocation())
                            .description(data.getDescription())
                            .build());
                }
                totalRole += total;
                System.out.println(data.getLocation() + ": " + total);
            }
            // Neu role nhieu hon nguoi choi thi bo so luong tuong ung ra cho can bang
            Random random = new Random();
            for (int i = 0; i < totalRole - totalPlay; i++) {
                int indexMember = random.nextInt(pls.size());
                // kiem tra so bi tru co phai la soi
                if (idSoi.contains(pls.get(indexMember).getId())) {
                    checkDecreaseSoi = true;
                }
                pls.remove(indexMember);
            }

            pls.forEach(item -> {
                if (StringUtils.pathEquals(item.getId(), "3")) { // kiem tra xem co soi nguyen ko de admin add vao data
                    isHaveSoiNguyen = true;
                }
            });

            // neu soi bi giam thi se la soi nguyen
            if (isSoiNguyen && checkDecreaseSoi) {
                for (DataMember item: pls) {
                    String name = "Sói Nguyền";
                    String description = "sói nguyền";
                    if (idSoi.contains(item.getId())) {
                        // them chuc nang nguyen
                        item.setLocation(item.getLocation() + " + " + name);
                        item.setDescription(item.getDescription() + description);
                        isHaveSoiNguyen = true;
                    }

                }
            }

            if(isCupid) {
                Collections.shuffle(pls);
                List <DataMember> cupid = pls.subList(0, 2);
                String cupid1 = cupid.get(0).getLocation();
                String cupid2 = cupid.get(1).getLocation();
                cupid.get(0).setLocation(cupid1 + "<i id=\"cupid\"> cặp đôi với " + cupid2 + "</i>");
                cupid.get(1).setLocation(cupid2 + "<i id=\"cupid\"> cặp đôi với " + cupid1 + "</i>");
                System.out.println(cupid);
            }
            return "OK load";
        } else {
            return "GAME NOT END YES";
        }

    }

    @GetMapping({"/play/" , "/play/{name}"})
    String test(HttpServletRequest request, @PathVariable(required = false) String name) {
        prepareData();
        StringBuilder result = new StringBuilder();
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        Integer ipAssigned = null;
        DataMember yourLocation;
        try {
            if (!pls.isEmpty()) {
                for (DataMember dataMember : pls) {
                    if (clientIp.equals(dataMember.getIpData())) {
                        ipAssigned = pls.indexOf(dataMember);
                        break;
                    }
                }
            }

            result.append("<div class=\"header-section\">\n")
                  .append("    <h1>Ma Sói</h1>\n")
                  .append("    <p class=\"desc\">Ván đấu ").append(detailEachGame.size() + 1).append("</p>\n")
                  .append("</div>\n");

            String roleText = "";
            String descText = "";
            if (ipAssigned != null) {
                roleText = pls.get(ipAssigned).getLocation();
                descText = pls.get(ipAssigned).getDescription();
                if (name != null) {
                    pls.get(ipAssigned).setNameMember(name);
                }
            } else {
                if (!pls.isEmpty()) {
                    Collections.shuffle(pls);
                    pls.sort(Comparator.comparing((DataMember m) -> m.getIpData(), Comparator.nullsFirst(String::compareTo))); // sort nhung id nao chua dc chon cho len dau
                    yourLocation = pls.get(0);

                    yourLocation.setIpData(clientIp);
                    yourLocation.setNameMember(name);
                    roleText = yourLocation.getLocation();
                    descText = yourLocation.getDescription();
                } else {
                    return HtmlTemplate.wrap("Ma Sói", 
                        "<div class=\"card\" style=\"text-align: center;\">\n" +
                        "    <h2 style=\"color: var(--warning);\">Đang chờ khởi tạo...</h2>\n" +
                        "    <p class=\"desc\">Vui lòng đợi Admin setup phòng chơi.</p>\n" +
                        "    <div class=\"qr-container\" style=\"margin-top: 15px;\">\n" +
                        "        <img src=\"/qrcode.png\" alt=\"QR Code\" class=\"qr-image\">\n" +
                        "    </div>\n" +
                        "</div>");
                }
            }

            // Reveal Box for Werewolf Role
            result.append("<div class=\"card\" style=\"text-align: center;\">\n")
                  .append("    <h3 style=\"margin-bottom: 12px; color: var(--text-muted); font-size: 0.95rem;\">CHỨC NĂNG CỦA BẠN:</h3>\n")
                  .append("    <div class=\"reveal-box blurred\" onclick=\"this.classList.toggle('blurred')\">\n")
                  .append("        <div class=\"reveal-placeholder\">Nhấp để ẩn/hiện chức năng</div>\n")
                  .append("        <div class=\"reveal-content\">\n")
                  .append("            <h2 id=\"location\" style=\"font-size: 2rem; color: #f87171;\">").append(roleText).append("</h2>\n")
                  .append("            <p class=\"desc\" style=\"margin-top: 10px; font-size: 0.9rem; color: var(--text-muted);\">").append(descText).append("</p>\n")
                  .append("        </div>\n")
                  .append("    </div>\n")
                  .append("</div>\n");

            // Action Buttons
            result.append("<div style=\"display: flex; gap: 10px;\">\n")
                  .append("    <button type=\"button\" id=\"showHistory\" class=\"btn btn-primary\" style=\"flex: 1;\">Xem Lịch Sử</button>\n")
                  .append("</div>\n");

            // Roles in play description
            result.append("<div class=\"card\">\n")
                  .append("    <h3 style=\"margin-bottom: 12px; font-size: 1.1rem;\">Các chức năng có trong ván</h3>\n")
                  .append("    <div class=\"item-list\">\n");
            
            StringBuilder rolesDesc = new StringBuilder();
            showDescription(listShowForMember, rolesDesc, false);
            result.append(rolesDesc.toString());

            result.append("    </div>\n")
                  .append("</div>\n");

            // QR Code Section
            result.append("<div class=\"card\" style=\"text-align: center;\">\n")
                  .append("    <h3 style=\"margin-bottom: 10px; font-size: 1.1rem;\">Mã QR tham gia</h3>\n")
                  .append("    <div class=\"qr-container\">\n")
                  .append("        <img src=\"").append(image).append("\" alt=\"QR Code\" class=\"qr-image\">\n")
                  .append("    </div>\n")
                  .append("</div>\n");

            // Modal Popup Layout Redesigned
            result.append("<div id=\"popupModal\" style=\"display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; z-index: 1000; background: rgba(9, 13, 22, 0.85); backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px); justify-content: center; align-items: center; padding: 20px;\">\n")
                  .append("    <div class=\"card\" style=\"width: 100%; max-width: 450px; max-height: 85%; display: flex; flex-direction: column; overflow: hidden; border: 1px solid rgba(255,255,255,0.1); background: #131b2e;\">\n")
                  .append("        <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; border-bottom: 1px solid var(--card-border); padding-bottom: 10px;\">\n")
                  .append("            <h2 style=\"font-size: 1.3rem;\">Lịch Sử Ván Đấu</h2>\n")
                  .append("            <button id=\"closePopup\" style=\"background: transparent; border: none; font-size: 1.5rem; color: var(--text-muted); cursor: pointer;\">&times;</button>\n")
                  .append("        </div>\n")
                  .append("        <div id=\"popupContent\" style=\"overflow-y: auto; flex: 1; padding-right: 4px; display: flex; flex-direction: column; gap: 16px;\">\n")
                  .append("            <!-- History content goes here -->\n")
                  .append("        </div>\n")
                  .append("    </div>\n")
                  .append("</div>\n");

            result.append("<script>\n"
                    + "document.getElementById('showHistory').onclick = function() {\n"
                    + "    const popupModal = document.getElementById('popupModal');\n"
                    + "    const popupContent = document.getElementById('popupContent');\n"
                    + "    fetch('/ms/showHistory', { method: 'POST' })\n"
                    + "        .then(response => response.text())\n"
                    + "        .then(responseData => {\n"
                    + "             popupContent.innerHTML = responseData;\n"
                    + "             popupModal.style.display = 'flex';\n"
                    + "        })\n"
                    + "        .catch(error => console.error('Error:', error));\n"
                    + "};\n"
                    + "\n"
                    + "document.getElementById('closePopup').onclick = function() {\n"
                    + "    document.getElementById('popupModal').style.display = 'none';\n"
                    + "};\n"
                    + "\n"
                    + "window.onclick = function(event) {\n"
                    + "    const modal = document.getElementById('popupModal');\n"
                    + "    if (event.target == modal) {\n"
                    + "        modal.style.display = 'none';\n"
                    + "    }\n"
                    + "};\n"
                    + "</script>\n");

            String customCss = 
                "<style>\n" +
                "  #cupid { color: #f59e0b; font-style: normal; font-weight: 500; font-size: 0.8rem; display: block; margin-top: 4px; }\n" +
                "  .history-game-card { border: 1px solid var(--card-border); border-radius: 12px; padding: 12px; background: rgba(30, 41, 59, 0.3); }\n" +
                "</style>";

            return HtmlTemplate.wrap("Ma Sói Play", result.toString(), customCss);
        } catch (Exception e) {
            return HtmlTemplate.wrap("Ma Sói Play", 
                "<div class=\"card\" style=\"text-align: center;\">\n" +
                "    <h2 style=\"margin-bottom: 10px;\">Đang tải phòng...</h2>\n" +
                "    <div class=\"qr-container\" style=\"margin-top: 15px;\">\n" +
                "        <img src=\"/qrcode.png\" alt=\"QR Code\" class=\"qr-image\">\n" +
                "    </div>\n" +
                "</div>");
        }
    }

    @GetMapping({"/play"})
    String playGetName() {
        StringBuilder result = new StringBuilder();

        result.append("<div class=\"header-section\">\n")
              .append("    <h1>Ma Sói</h1>\n")
              .append("    <p class=\"desc\">Đăng ký thông tin người chơi</p>\n")
              .append("</div>\n");

        result.append("<div class=\"card\" style=\"text-align: center;\">\n")
              .append("    <h3 style=\"margin-bottom: 16px;\">Nhập tên của bạn</h3>\n")
              .append("    <form id=\"dataForm\" onsubmit=\"handleSubmit(event)\">\n")
              .append("        <input type=\"text\" id=\"nameId\" class=\"input-text\" placeholder=\"Ví dụ: Huy, Nam...\" required style=\"text-align: center; margin-bottom: 20px;\"><br>\n")
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
              .append("        window.location.href = '/ms/play/' + encodeURIComponent(input.value.trim());\n")
              .append("    }\n")
              .append("}\n")
              .append("</script>\n");

        return HtmlTemplate.wrap("Ma Sói", result.toString());
    }

    @PostMapping("/showHistory")
    String showHistory() {
        StringBuilder result = new StringBuilder();
        detailEachGame.forEach((key, value) -> {
            result.append("<div class=\"history-game-card\">\n")
                  .append("    <h4 style=\"font-size: 1rem; color: #60a5fa; margin-bottom: 10px;\">Ván ").append(key).append(":</h4>\n")
                  .append("    <div class=\"item-list\">\n");

            value.sort(Comparator.comparing((DataMember m) -> Integer.parseInt(m.getId())));
            value.forEach(item -> {
                String badgeClass = "badge-green";
                if (idSoi.contains(item.getId())) {
                    badgeClass = "badge-red";
                } else if (idOutsider.contains(item.getId())) {
                    badgeClass = "badge-orange";
                }

                String displayLocation = item.getLocation();
                // Strip tags for history clean rendering if any
                displayLocation = displayLocation.replaceAll("<[^>]*>", "");

                String name = "Chưa nhận";
                if (item.getNameMember() != null) {
                    name = item.getNameMember();
                } else if (item.getIpData() != null) {
                    name = "Ẩn danh";
                }

                result.append("        <div class=\"item-row\" style=\"padding: 8px 12px; font-size: 0.85rem;\">\n")
                      .append("            <span style=\"font-weight: 500;\">").append(name).append("</span>\n")
                      .append("            <span class=\"badge ").append(badgeClass).append("\">").append(displayLocation).append("</span>\n")
                      .append("        </div>\n");
            });
            result.append("    </div>\n")
                  .append("</div>\n");
        });

        return result.toString();
    }

    @PostMapping("/end")
    String endGame() {
        detailEachGame.put(detailEachGame.size() + 1, new ArrayList<>(pls));
        isGameEnd = true;
        isAddSoiNguyen = false;
        pls.clear();
        listShowForMember.clear();
        checkDecreaseSoi = false;
        return "End Game : " + detailEachGame.size();
    }

    private void prepareData() {
        if (datas.isEmpty()) {
            datas.add(createDM("1", "Sói", "Sói đó, sói nè, sói chính hiệu 9999"));
            datas.add(createDM("2", "Sói đầu đàn", "Sói có quyền quyết định cắn ai"));
            datas.add(createDM("3", "Sói nguyền", "Nguyền 1 người chơi, người đó sẽ là sói"));
            datas.add(createDM("4", "Sói đầu đàn(2)", "Sói tiên tri phải soi 2 lần"));
            datas.add(createDM("5", "Sói si đa", "1 lần duy nhất chức năng nào chạm vào bạn thì chức năng đó chết"));
            datas.add(createDM("6", "Kẻ phản bội", "Theo phe sói, biết sói là ai, sói không biết bạn"));

            datas.add(createDM("20", "Sát thủ", "Phe thứ 3, có quyền giết sói nhưng sói ko giết lại được. Nếu bị sói nguyền và là thành viên còn sống duy nhất thì mỗi đêm giết 2 người"));
            datas.add(createDM("21", "Chán đời", "Muốn chết nhưng ko muốn bị sói cắn. Khi bị treo cổ bạn sẽ win"));

            datas.add(createDM("30", "Dân", "Dân giàu nước mạnh"));
            datas.add(createDM("31", "Phù thủy", "Có 1 bình cứu và 1 bình giết. Khi đã sài phép cứu thì sẽ ko biết ai bị giết nữa"));
            datas.add(createDM("32", "Tiên tri", "Soi 1 người có phải Sói hay ko"));
            datas.add(createDM("33", "Bảo vệ", "Mỗi đêm bảo vệ 1 người, ko bảo vệ 1 người 2 đêm liên tiếp"));
            datas.add(createDM("34", "Thợ săn", "Ghim 1 người, chỉ trong đêm nếu bạn chết người đó chết theo"));
            datas.add(createDM("35", "Thanh niên cứng", "Khi bị treo cổ có quyền lật bài và giết 1 người, bạn vẫn sống như bình thường"));

            datas.add(createDM("37", "Tiên tri tập sự", "Khi tiên tri chết thì bạn là Tiên tri"));
            datas.add(createDM("38", "Câm lặng", "Chọn 1 người và cấm phép người đó. Nếu chọn sói đầu đàn thì sói sẽ ko cắn dc người, nếu sói con thì phải là sói duy nhất"));
            datas.add(createDM("39", "Thám tử", "Chọn 1 vùng 3 người, bạn sẽ biết có sói trong đó hay không"));
            datas.add(createDM("40", "Bị nguyền", "Bạn theo phe dân, nếu bị sói cắn sẽ thành sói"));
            datas.add(createDM("41", "Người bệnh", "Nếu sói/sát thủ cắn bạn, thì đêm sau sói/sát thủ không giết được ai"));
            datas.add(createDM("42", "Nhân bản", "Chọn 1 người, nếu người đó chết bạn sẽ nhận chức năng người đó"));
            datas.add(createDM("43", "Độc tài", "Duy nhất: chọn 1 người chơi, nếu không phải dân(- chán đời) người đó chết, nếu dân bạn chết"));
            datas.add(createDM("44", "Thiên thần", "1 lần duy nhất có thể ngăn chặn toàn bộ cái chết trong đêm"));
            datas.add(createDM("45", "Phù thủy già", "mỗi ngày đuổi 1 người ko phải mình ra khỏi làng"));
            datas.add(createDM("46", "Boooooom", "Duy nhất chọn 1 người chơi giao bom, mỗi đêm bạn có quyền kích nổ hoặc ko, sáng ra người cầm bom sẽ chuyển bom đi, nếu chết có quyền chọn chuyển bom hoặc ko"));

        }
    }

    private DataMember createDM(String id, String location, String description) {
        return DataMember.builder()
                .id(id)
                .location(location)
                .description(description)
                .build();
    }

    private DataMember createDM(DataMember dataMember, Integer total) {
        return DataMember.builder()
                .id(dataMember.getId())
                .location(dataMember.getLocation())
                .description(dataMember.getDescription())
                .total(String.valueOf(total))
                .build();
    }

    @GetMapping("/admin")
    String admin() {
        prepareData();
        StringBuilder result = new StringBuilder();
        
        result.append("<div class=\"header-section\">\n")
              .append("    <h1>Quản Trò Ma Sói</h1>\n")
              .append("    <p class=\"desc\">Bảng điều khiển ván đấu hiện tại</p>\n")
              .append("</div>\n");

        // QR Code Section
        result.append("<div class=\"card\" style=\"text-align: center;\">\n")
              .append("    <div class=\"qr-container\">\n")
              .append("        <img src=\"").append(image).append("\" alt=\"QR Code\" class=\"qr-image\" style=\"width: 150px; height: 150px;\">\n")
              .append("    </div>\n")
              .append("    <p class=\"desc\" style=\"font-size: 0.8rem; margin-top: 5px;\">Quét mã QR để người chơi tham gia</p>\n")
              .append("</div>\n");

        // Active roles summary card
        StringBuilder activeRoles = new StringBuilder();
        for (DataMember item: pls) {
            if (!item.getId().equals("1") && !item.getId().equals("30")) {
                activeRoles.append(item.getLocation()).append(", ");
            }
        }
        String rolesStr = activeRoles.toString();
        if (rolesStr.endsWith(", ")) {
            rolesStr = rolesStr.substring(0, rolesStr.length() - 2);
        }
        if (!rolesStr.isEmpty()) {
            result.append("<div class=\"card\" style=\"margin-bottom: 16px;\">\n")
                  .append("    <h3 style=\"margin-bottom: 6px; font-size: 0.95rem; color: var(--text-muted);\">Vai trò đặc biệt trong ván:</h3>\n")
                  .append("    <p style=\"font-weight: 600; font-size: 1rem; color: #a5b4fc;\">").append(rolesStr).append("</p>\n")
                  .append("</div>\n");
        }

        // Players roster card
        result.append("<div class=\"card\">\n")
              .append("    <h3 style=\"margin-bottom: 14px; font-size: 1.1rem;\">Danh sách người chơi</h3>\n")
              .append("    <div class=\"item-list\">\n");

        pls.sort(Comparator.comparing((DataMember m) -> Integer.parseInt(m.getId())));
        pls.forEach(item -> {
            String badgeClass = "badge-green";
            String cardBorder = "border-left: 4px solid var(--success);";
            if (idSoi.contains(item.getId())) {
                badgeClass = "badge-red";
                cardBorder = "border-left: 4px solid var(--danger);";
            } else if (idOutsider.contains(item.getId())) {
                badgeClass = "badge-orange";
                cardBorder = "border-left: 4px solid var(--warning);";
            }

            String displayLocation = item.getLocation().replaceAll("<[^>]*>", "");
            String name = "Chưa nhận chức năng";
            if (item.getNameMember() != null) {
                name = item.getNameMember();
            } else if (item.getIpData() != null) {
                name = "Ẩn danh";
            }

            result.append("        <div class=\"item-row\" style=\"").append(cardBorder).append(" padding: 12px 16px;\">\n")
                  .append("            <span style=\"font-weight: 700; font-size: 1rem;\">")
                  .append(item.getIdPlayGame()).append(". ").append(name)
                  .append("            </span>\n")
                  .append("            <span class=\"badge ").append(badgeClass).append("\">").append(displayLocation).append("</span>\n")
                  .append("        </div>\n");
        });
        result.append("    </div>\n")
              .append("</div>\n");

        // Werewolf Curse Activation (if applicable)
        if (isHaveSoiNguyen && !isAddSoiNguyen) {
            result.append("<div class=\"card\" style=\"margin-top: 16px; text-align: center;\">\n")
                  .append("    <h3 style=\"margin-bottom: 12px; font-size: 1.1rem;\">Nguyền chúc của Sói</h3>\n")
                  .append("    <div style=\"display: flex; gap: 8px; justify-content: center;\">\n")
                  .append("        <select class=\"selectSoiNguyen input-text\" style=\"flex: 1; min-width: 150px; padding: 12px;\">\n");
            
            pls.forEach(item -> {
                String name = (item.getNameMember() == null || item.getNameMember().isEmpty()) ? "Ẩn danh" : item.getNameMember().substring(0, 1).toUpperCase() + item.getNameMember().substring(1);
                String displayLocation = item.getLocation().replaceAll("<[^>]*>", "");
                result.append("            <option value=\"").append(item.getIdPlayGame()).append("\">")
                      .append(item.getIdPlayGame()).append("-").append(displayLocation).append("-").append(name)
                      .append("</option>\n");
            });
            
            result.append("        </select>\n")
                  .append("        <button id=\"addSoiNguyen\" class=\"btn btn-primary\" style=\"width: auto; padding: 12px 20px;\">Nguyền</button>\n")
                  .append("    </div>\n")
                  .append("</div>\n");
        }

        // End Game button
        result.append("<button id=\"endGame\" class=\"btn btn-danger\" style=\"margin-top: 24px;\">Kết Thúc Ván Đấu</button>\n");

        result.append("<script>\n");
        if (isHaveSoiNguyen && !isAddSoiNguyen) {
            result.append("document.getElementById('addSoiNguyen').addEventListener('click', function() {\n")
                  .append("    var soiNguyen = document.querySelector('.selectSoiNguyen');\n")
                  .append("    var data = { soiNguyen: soiNguyen.value };\n")
                  .append("    fetch('/ms/soiNguyen', {\n")
                  .append("        method: 'POST',\n")
                  .append("        headers: { 'Content-Type': 'application/json' },\n")
                  .append("        body: JSON.stringify(data)\n")
                  .append("    })\n")
                  .append("    .then(response => response.text())\n")
                  .append("    .then(responseData => {\n")
                  .append("         alert('Response: ' + responseData);\n")
                  .append("         window.location.reload();\n")
                  .append("    })\n")
                  .append("    .catch(error => console.error('Error:', error));\n")
                  .append("});\n");
        }

        result.append("document.getElementById('endGame').addEventListener('click', function() {\n")
              .append("    const userConfirmed = confirm(\"Bạn có chắc chắn muốn KẾT THÚC GAME?\");\n")
              .append("    if (userConfirmed) { \n")
              .append("         fetch('/ms/end', { method: 'POST' })\n")
              .append("             .then(response => response.text())\n")
              .append("             .then(responseData => {\n")
              .append("                 alert('Response: ' + responseData);\n")
              .append("                 window.location.href = '/ms/run';\n")
              .append("             })\n")
              .append("             .catch(error => console.error('Error:', error));\n")
              .append("    } \n")
              .append("});\n")
              .append("</script>\n");

        return HtmlTemplate.wrap("Quản Trò Ma Sói", result.toString());
    }

    @PostMapping("/soiNguyen")
    String soiNguyen(@RequestBody SoiNguyenDto soiNguyen) {
        if (isAddSoiNguyen) {
            return "Soi Nguyen Da Add TRUOc DO";
        }
        isAddSoiNguyen = true;
        AtomicReference<String> soiNguyenName = new AtomicReference<>("");
        pls.forEach(item -> {
            if (StringUtils.pathEquals(item.getIdPlayGame(), soiNguyen.getSoiNguyen())) {
                item.setId("1"); //Set la soi
                item.setLocation(item.getLocation() + " - Soi");
                soiNguyenName.set(item.getLocation());
            }
        });
        return "add Soi Nguyen: " + soiNguyenName;
    }

    @GetMapping("/admin2")
    String admin2() {
        prepareData();
        StringBuilder result = new StringBuilder();
        
        result.append("<div class=\"header-section\">\n")
              .append("    <h1>Tất cả chức năng Ma Sói</h1>\n")
              .append("    <p class=\"desc\">Danh sách mô tả chi tiết bài phân vai</p>\n")
              .append("</div>\n");

        result.append("<div class=\"card\">\n")
              .append("    <div class=\"item-list\">\n");

        showDescription(datas, result, true);

        result.append("    </div>\n")
              .append("</div>\n");

        return HtmlTemplate.wrap("Mô tả chức năng", result.toString());
    }

    private void showDescription(List<DataMember> list, StringBuilder result, Boolean isAdmin) {
        list.forEach(item -> {
            String badgeClass = "badge-green";
            String cardBorder = "border-left: 4px solid var(--success);";
            if (idSoi.contains(item.getId())) {
                badgeClass = "badge-red";
                cardBorder = "border-left: 4px solid var(--danger);";
            } else if (idOutsider.contains(item.getId())) {
                badgeClass = "badge-orange";
                cardBorder = "border-left: 4px solid var(--warning);";
            }

            String displayTitle = item.getLocation();
            if (isAdmin) {
                displayTitle = item.getId() + ". " + displayTitle;
            } else {
                String qty = (item.getId().equals("1") || item.getId().equals("30") || !item.getTotal().equals("1")) ? " (x" + item.getTotal() + ")" : "";
                displayTitle = displayTitle + qty;
            }

            result.append("        <div class=\"role-card\" style=\"").append(cardBorder).append(" display: flex; flex-direction: column; align-items: flex-start; gap: 4px; padding: 12px 16px;\">\n")
                  .append("            <span class=\"role-title\" style=\"font-weight:700;\">").append(displayTitle).append("</span>\n")
                  .append("            <p style=\"font-size: 0.8rem; color: var(--text-muted); text-align: left;\">").append(item.getDescription()).append("</p>\n")
                  .append("        </div>\n");
        });
    }

    private void addRoleCard(StringBuilder result, DataMember item) {
        String cardClass = "role-card-dan";
        String colorStyle = "color: #34d399;"; // green-400
        if (item.getId().equals("1") || idSoi.contains(item.getId())) {
            cardClass = "role-card-soi";
            colorStyle = "color: #f87171;"; // red-400
        } else if (idOutsider.contains(item.getId())) {
            cardClass = "role-card-other";
            colorStyle = "color: #fbbf24;"; // amber-400
        }
        
        result.append("            <div class=\"role-card ").append(cardClass).append("\">\n")
              .append("                <div style=\"padding-right: 10px; text-align: left;\">\n")
              .append("                    <span class=\"role-title\" style=\"").append(colorStyle).append("\">").append(item.getLocation().toUpperCase()).append("</span>\n")
              .append("                    <p style=\"font-size: 0.75rem; color: var(--text-muted); margin-top: 2px;\">").append(item.getDescription()).append("</p>\n")
              .append("                </div>\n");
        
        if (item.getId().equals("1") || item.getId().equals("30")) {
            result.append("                <input type=\"number\" min=\"0\" value=\"0\" id=\"").append(item.getId()).append("\" name=\"").append(item.getId()).append("\" ")
                  .append("class=\"input-text\" style=\"width: 60px; padding: 8px; font-size: 0.9rem; text-align: center;\" oninput=\"updateSum()\">\n");
        } else {
            result.append("                <label class=\"switch\">\n")
                  .append("                    <input type=\"checkbox\" id=\"").append(item.getId()).append("\" name=\"").append(item.getId()).append("\" value=\"1\" onclick=\"updateSum()\">\n")
                  .append("                    <span class=\"slider\"></span>\n")
                  .append("                </label>\n");
        }
        result.append("            </div>\n");
    }

    private Map<String, List<String>> createMapSelect() {
        Map<String, List<String>> result = new HashMap<>();
        List<String> soi = List.of("Giết", "Nguyền", "Lây bệnh");
        List<String> soiSida = List.of("Lây covid");

        List<String> satThu = List.of("Giết");

        List<String> phuthuy = List.of("Giết", "Bảo vệ");
        List<String> baove = List.of("Bảo vệ");
        List<String> thoSan = List.of("Ghim");
        List<String> camLang = List.of("Câm lặng");
        List<String> nhanBan = List.of("Chọn");
        List<String> docTai = List.of("Giết");
        List<String> thienThan = List.of("Bảo vệ");
        List<String> bom = List.of("Giết");
        result.put("--Select--", List.of("--Select--"));
        result.put("Sói", soi);
        listShowForMember.forEach(item -> {
            if ("5".equals(item.getId())) {
                result.put(item.getLocation(), soiSida);
            } else if ("20".equals(item.getId())) {
                result.put(item.getLocation(), satThu);
            } else if ("31".equals(item.getId())) {
                result.put(item.getLocation(), phuthuy);
            } else if ("33".equals(item.getId())) {
                result.put(item.getLocation(), baove);
            } else if ("34".equals(item.getId())) {
                result.put(item.getLocation(), thoSan);
            } else if ("38".equals(item.getId())) {
                result.put(item.getLocation(), camLang);
            } else if ("42".equals(item.getId())) {
                result.put(item.getLocation(), nhanBan);
            } else if ("43".equals(item.getId())) {
                result.put(item.getLocation(), docTai);
            } else if ("44".equals(item.getId())) {
                result.put(item.getLocation(), thienThan);
            } else if ("46".equals(item.getId())) {
                result.put(item.getLocation(), bom);
            }
        });

        return result;
    }
}
