package com.example.spyfall.controller;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.util.HtmlTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
public class SpyController {

    List<DataMember> dataPlay = new ArrayList<>();
    Map<String, DataMember> dataPlayMap = new HashMap();
     String data = "Bệnh viện, " +
            "Nhà hát, " +
            "Sân bay, " +
            "Trường học, " +
            "Nhà hàng, " +
            "Trung Tâm giải trí, " +
            "Trạm không gian, " +
            "Rạp xiếc, " +
            "Ngân hàng, " +
            "Trung tâm mua sắm, " +
            "Sở cảnh sát, " +
            "Khách sạn, " +
            "Bãi biển, " +
            "Nhà thờ, " +
            "Sân vận động, " +
            "Nhà tù, " +
            "Trạm xăng, " +
            "Công viên quốc gia, " +
            "Nhà ga xe lửa, " +
            "Sở thú, " +
            "Bảo tàng, " +
            "Chùa";

    String data2 = "Bệnh viện, " +
            "Sân bay, " +
            "Trường học, " +
            "Trung Tâm giải trí, " +
            "Rạp xiếc, " +
            "Ngân hàng, " +
            "Trung tâm mua sắm, " +
            "Khách sạn, " +
            "Bãi biển, " +
            "Sân vận động, " +
            "Nhà tù, " +
            "Quán cà phê, " +
            "Nhà hát, " +
            "Bảo tàng, " +
            "Thư viện, " +
            "Khu cắm trại, " +
            "Phòng gym, " +
            "Ga tàu, " +
            "Nhà thờ, " +
            "Chùa, " +
            "Nhà sách, " +
            "Rạp phim, " +
            "Trung tâm tiệc cưới, " +
            "Công viên nước, " +
            "Thủy cung, " +
            "Chợ đêm, " +
            "Hiệu thuốc, " +
            "Quán bar, " +
            "Karaoke, " +
            "Nhà kho, " +
            "Đồn công an, " +
            "Phố đi bộ, " +
            "Hồ bơi, " +
            "Nhà hàng, " +
            "Công viên, " +
            "Sở thú";

    List<DataMember> memberWillPlay = new ArrayList<>(); // la list nguoi choi

    List<DataMember> memberPlay = new ArrayList<>(); // la chung ta se choi

    List<DataMember> listItemOfSpy = new ArrayList<>();

    DataMember allMemberData = null;

    DataMember spyMemberData = null;
    Path outputPath = Paths.get(System.getProperty("user.dir"));//.resolve("/sdcard/java/spy.png");
    String image ="/spy.png";
    

    @GetMapping({"/sp", "/sp/"})
    String test(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        System.out.println(clientIp);
        Integer ipAssigned = null;
        try {
            if (!memberPlay.isEmpty()) {
                for (DataMember dataMember : memberPlay) {
                    if (dataMember.getIpData().equals(clientIp)) {
                        ipAssigned = memberPlay.indexOf(dataMember);
                        break;
                    }
                }
            }
            StringBuilder result = new StringBuilder();
            
            result.append("<div class=\"header-section\">\n")
                  .append("    <h1>Spyfall</h1>\n")
                  .append("    <p class=\"desc\">Tìm ra gián điệp ẩn mình</p>\n")
                  .append("</div>\n");

            String locationText = "";
            if (ipAssigned != null) {
                locationText = memberPlay.get(ipAssigned).getLocation();
            } else {
                if (memberWillPlay.isEmpty()) {
                    return HtmlTemplate.wrap("Spyfall", 
                        "<div class=\"card\" style=\"text-align: center;\">\n" +
                        "    <h2 style=\"color: var(--danger); margin-bottom: 10px;\">Ván đấu chưa được setup</h2>\n" +
                        "    <p class=\"desc\">Vui lòng liên hệ Admin để thiết lập phòng chơi mới.</p>\n" +
                        "    <a href=\"/\" class=\"btn btn-primary\" style=\"margin-top: 20px;\">Về Trang Chủ</a>\n" +
                        "</div>");
                }
                Collections.shuffle(memberWillPlay);
                DataMember yourLocation = memberWillPlay.get(0);

                yourLocation.setIpData(clientIp);
                memberPlay.add(yourLocation);
                memberWillPlay.remove(0);
                System.out.println(yourLocation.getLocation());
                locationText = yourLocation.getLocation();
            }

            // Click-to-reveal Box
            result.append("<div class=\"card\" style=\"text-align: center;\">\n")
                  .append("    <h3 style=\"margin-bottom: 12px; color: var(--text-muted); font-size: 0.95rem;\">VAI TRÒ / ĐỊA ĐIỂM CỦA BẠN:</h3>\n")
                  .append("    <div class=\"reveal-box blurred\" onclick=\"this.classList.toggle('blurred')\">\n")
                  .append("        <div class=\"reveal-placeholder\">Nhấp để ẩn/hiện vai trò</div>\n")
                  .append("        <div class=\"reveal-content\">\n")
                  .append("            <h2 style=\"font-size: 1.8rem; color: #60a5fa;\">").append(locationText).append("</h2>\n")
                  .append("        </div>\n")
                  .append("    </div>\n")
                  .append("</div>\n");

            // List of suspected locations
            result.append("<div class=\"card\">\n")
                  .append("    <h3 style=\"margin-bottom: 14px; text-align: center; font-size: 1.1rem;\">Danh sách địa điểm nghi vấn</h3>\n")
                  .append("    <div style=\"display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 0.85rem;\">\n");

            for (int i = 0; i < dataPlay.size(); i++) {
                result.append("        <div style=\"padding: 8px 10px; background: rgba(15, 23, 42, 0.45); border-radius: 8px; border: 1px solid var(--card-border); text-align: left; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;\">")
                      .append(dataPlay.get(i).getId()).append(". ").append(dataPlay.get(i).getLocation())
                      .append("        </div>\n");
            }

            result.append("    </div>\n")
                  .append("</div>\n");

            // QR Code Section
            result.append("<div class=\"card\" style=\"text-align: center;\">\n")
                  .append("    <h3 style=\"margin-bottom: 10px; font-size: 1.1rem;\">Mã QR tham gia</h3>\n")
                  .append("    <div class=\"qr-container\">\n")
                  .append("        <img src=\"").append(image).append("\" alt=\"QR Code\" class=\"qr-image\">\n")
                  .append("    </div>\n")
                  .append("    <p class=\"desc\" style=\"margin-top: 10px; font-size: 0.85rem;\">Quét mã để thiết bị khác cùng vào chơi</p>\n")
                  .append("</div>\n");

            return HtmlTemplate.wrap("Spyfall", result.toString());
        } catch (Exception e) {
            return HtmlTemplate.wrap("Spyfall", 
                "<div class=\"card\" style=\"text-align: center;\">\n" +
                "    <h2 style=\"margin-bottom: 10px;\">Đợi một chút nha...</h2>\n" +
                "    <p class=\"desc\">Hệ thống đang đồng bộ dữ liệu.</p>\n" +
                "    <div class=\"qr-container\" style=\"margin-top: 15px;\">\n" +
                "        <img src=\"/qrcode.png\" alt=\"QR Code\" class=\"qr-image\">\n" +
                "    </div>\n" +
                "</div>");
        }
    }

    @GetMapping({"/sp/{style}/{total}/{spy}", "/sp/{style}/{total}", "/sp/{style}/{total}/{spy}/{whiteHat}"})
    String start(@PathVariable Integer total, @PathVariable Integer style, @PathVariable(required = false) Integer spy, @PathVariable(required = false) Optional<Integer> whiteHat) {
        memberPlay.clear();
        memberWillPlay.clear();
        listItemOfSpy.clear();

        String[] datas = data2.split(",");
        for (String dataItem : datas) {
            boolean exist = dataPlay.stream()
                    .anyMatch(item -> item.getLocation().equals(dataItem));

            if (!exist) {
                int maxId = dataPlay.stream()
                        .mapToInt(o -> {
                            try {
                                    return Integer.parseInt(o.getId());
                            } catch (NumberFormatException e) {
                                    return 0;
                            }
                        })
                        .max()
                        .orElse(0);

                int newId = maxId + 1;
                DataMember dataMember = createDM(newId, dataItem);
                dataPlay.add(dataMember);
            }

        }

        Random random = new Random();
        int indexMember = random.nextInt(dataPlay.size());
        allMemberData = dataPlay.get(indexMember);

        listItemOfSpy = new ArrayList<>(dataPlay);
        listItemOfSpy.remove(indexMember);

        int indexSpy = random.nextInt(listItemOfSpy.size());
        spyMemberData = listItemOfSpy.get(indexSpy);

        System.out.println("data" + allMemberData.getLocation() + "spy" + spyMemberData.getLocation());
        if (spy == null) {
            for (int i = 1; i < total; i++) {
                memberWillPlay.add(createDM(total, allMemberData.getLocation()));
            }
            if (style.equals(1)) {
                memberWillPlay.add(createDM(total, spyMemberData.getLocation()));
            } else {
                memberWillPlay.add(createDM(total, "Spy đó nhìn nhìn cái gì"));
            }
        } else {
            int whiteHatNum = whiteHat.orElse(0);
            int totalWhiteHatAndSpy = whiteHatNum + spy;
            for (int i = 0; i < (total - totalWhiteHatAndSpy); i++) {
                memberWillPlay.add(createDM(total, allMemberData.getLocation()));
            }
            for (int i = 0; i < spy; i++) {
                if (style.equals(1)) {
                    memberWillPlay.add(createDM(total, spyMemberData.getLocation()));
                } else {
                    memberWillPlay.add(createDM(total, "Spy đó nhìn nhìn cái gì"));
                }
            }
            for (int i = 0; i < whiteHatNum; i++) {
                memberWillPlay.add(createDM(total, "Spy đó nhìn nhìn cái gì"));
            }
        }
        
        StringBuilder result = new StringBuilder();
        result.append("<div class=\"header-section\">\n")
              .append("    <h1>Spyfall Setup</h1>\n")
              .append("    <p class=\"desc\" style=\"color: var(--success); font-weight: 500;\">Khởi tạo ván chơi thành công!</p>\n")
              .append("</div>\n");

        result.append("<div class=\"card\" style=\"text-align: center;\">\n")
              .append("    <h3 style=\"margin-bottom: 10px; font-size: 1.1rem;\">Mã QR tham gia</h3>\n")
              .append("    <div class=\"qr-container\">\n")
              .append("        <img src=\"").append(image).append("\" alt=\"QR Code\" class=\"qr-image\">\n")
              .append("    </div>\n")
              .append("    <div style=\"margin-top: 15px; display: flex; flex-direction: column; gap: 8px; font-size: 0.85rem;\">\n")
              .append("        <div class=\"item-row\"><span>Tổng người chơi:</span> <strong>").append(total).append("</strong></div>\n")
              .append("        <div class=\"item-row\"><span>Số lượng Spy:</span> <strong>").append(spy != null ? spy : 1).append("</strong></div>\n")
              .append("        <div class=\"item-row\"><span>Chế độ chơi:</span> <strong>").append(style.equals(1) ? "Spy có địa điểm khác biệt" : "Spy ẩn danh (không biết địa điểm)").append("</strong></div>\n")
              .append("    </div>\n")
              .append("</div>\n");

        result.append("<div class=\"card\">\n")
              .append("    <h3 style=\"margin-bottom: 14px; text-align: center; font-size: 1.1rem;\">Danh sách địa điểm nghi vấn</h3>\n")
              .append("    <div style=\"display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 0.85rem;\">\n");

        for (int i = 0; i < dataPlay.size(); i++) {
            result.append("        <div style=\"padding: 8px 10px; background: rgba(15, 23, 42, 0.45); border-radius: 8px; border: 1px solid var(--card-border); text-align: left; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;\">")
                  .append(dataPlay.get(i).getId()).append(". ").append(dataPlay.get(i).getLocation())
                  .append("        </div>\n");
        }

        result.append("    </div>\n")
              .append("</div>\n");

        result.append("<a href=\"/sp\" class=\"btn btn-primary\">Vào Phòng Chơi</a>\n");

        return HtmlTemplate.wrap("Spyfall Setup", result.toString());
    }

    @GetMapping("/sp/delete/{id}")
    String delete(@PathVariable Integer id) {
        for (DataMember item: dataPlay) {
            if (item.getId().trim().equals(id.toString())) {
                System.out.println(item.getLocation());
                dataPlay.remove(item);
                return "<h1 style=\"font-size: 500%;\"> da xoa" + item.getLocation() +"</h1>";
            }
        }
        return "<h1 style=\"font-size: 500%;\"> chua xoa</h1>";

    }

    @GetMapping("/sp/add/{name}")
    String test(@PathVariable String name) {
        int maxId = dataPlay.stream()
                .mapToInt(o -> {
                    try {
                        return Integer.parseInt(o.getId());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        int newId = maxId + 1;
        DataMember dataMember = createDM(newId, name);
        dataPlay.add(dataMember);
        return "<h1 style=\"font-size: 500%;\"> da them</h1>";

    }


    private DataMember createDM(int id, String location) {
        return DataMember.builder()
                .id(String.valueOf(id))
                .location(location)
                .build();
    }

}
