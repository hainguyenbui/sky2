package com.example.spyfall.service;

import com.example.spyfall.common.DataMember;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DataInputService {
    List<DataMember> dataSpyPlay = new ArrayList<>();
    List<List<String>> keywords = new ArrayList<>();
    List<DataMember> dataMaSoi = new ArrayList<>();

    public List<DataMember> createDataSpy() {
        if (dataSpyPlay.isEmpty()) {
            String data = valueDataSpy();
            String[] datas = data.split(",");

            for (String dataItem : datas) {
                boolean exist = dataSpyPlay.stream()
                        .anyMatch(item -> item.getLocation().equals(dataItem));

                if (!exist) {
                    int maxId = dataSpyPlay.stream()
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
                    DataMember dataMember = new DataMember(newId, dataItem);
                    dataSpyPlay.add(dataMember);
                }

            }
        }
        return dataSpyPlay;
    }

    public List<List<String>> createDataSpy2() {
        if (!keywords.isEmpty()) {
            return keywords;
        }
        keywords.add(Arrays.asList("Mây", "Sương mù"));
        keywords.add(Arrays.asList("Gió", "Lốc"));
        keywords.add(Arrays.asList("Mưa", "tuyết"));
        keywords.add(Arrays.asList("Mưa", "bão"));
        keywords.add(Arrays.asList("Gió", "bão"));
        keywords.add(Arrays.asList("sấm ", "sét"));
        keywords.add(Arrays.asList("nóng ", "nắng"));
        keywords.add(Arrays.asList("nhạc ", "giai điệu"));
        keywords.add(Arrays.asList("Tuyết ", "Băng"));
        keywords.add(Arrays.asList("Sấm ", "Chớp"));
        keywords.add(Arrays.asList("Bình minh ", "Hoàng hôn"));
        keywords.add(Arrays.asList("Lửa ", "Khói"));
        keywords.add(Arrays.asList("Sóng ", "Gió biển"));
        keywords.add(Arrays.asList("Lá ", "Cành"));
        keywords.add(Arrays.asList("Sương mù ", "Khói"));
        keywords.add(Arrays.asList("Khói ", "Mây"));
        keywords.add(Arrays.asList("bão ", "Lốc"));
        keywords.add(Arrays.asList("Tro ", "Than"));
        keywords.add(Arrays.asList("Than ", "Củi"));
        keywords.add(Arrays.asList("Tro ", "Củi"));
        keywords.add(Arrays.asList("Yêu thương ", "Hạnh phúc"));
        keywords.add(Arrays.asList("Niềm vui ", "Hân Hoan"));
        keywords.add(Arrays.asList("Buồn bã ", "Nhớ nhung"));
        keywords.add(Arrays.asList("Buồn bã ", "Cô đơn"));
        keywords.add(Arrays.asList("Buồn bã ", "Thất vọng"));
        keywords.add(Arrays.asList("Cô đơn ", "Nhớ nhung"));
        keywords.add(Arrays.asList("Thất vọng ", "Nhớ nhung"));
        keywords.add(Arrays.asList("Cô đơn ", "Thất vọng"));
        keywords.add(Arrays.asList("Nhân hậu", "Bao dung"));
        keywords.add(Arrays.asList("Trung thực", "Kiên nhẫn"));
        keywords.add(Arrays.asList("Trung thực", "Nhân hậu"));
        keywords.add(Arrays.asList("Trung thực", "Bao dung"));
        keywords.add(Arrays.asList("Khéo léo", "Thông minh"));
        keywords.add(Arrays.asList("Sáng tạo", "Thông minh"));
        keywords.add(Arrays.asList("Tỉ mỉ", "Thông minh"));
        keywords.add(Arrays.asList("Tỉ mỉ", "Sáng tạo"));
        keywords.add(Arrays.asList("Khéo léo", "Sáng tạo"));
        keywords.add(Arrays.asList("Khéo léo", "Tỉ mỉ"));
        keywords.add(Arrays.asList("Quyết tâm", "Nỗ lực"));
        keywords.add(Arrays.asList("Quyết tâm", "Kiên trì"));
        keywords.add(Arrays.asList("Quyết tâm", "Dũng cảm"));
        keywords.add(Arrays.asList("Kiên trì", "Dũng cảm"));
        keywords.add(Arrays.asList("Nỗ lực", "Dũng cảm"));
        keywords.add(Arrays.asList("Quyết đoán", "Dũng cảm"));
        keywords.add(Arrays.asList("Tự tin", "Dũng cảm"));
        keywords.add(Arrays.asList("Mạnh mẽ", "Dũng cảm"));
        keywords.add(Arrays.asList("Nỗ lực", "Kiên trì"));
        keywords.add(Arrays.asList("Hân hoan", "Sảng khoái"));
        keywords.add(Arrays.asList("Hân hoan", "Tự hào"));
        keywords.add(Arrays.asList("Lạc quan", "Hy vọng"));
        keywords.add(Arrays.asList("Lạc quan", "Tin tưởng"));
        keywords.add(Arrays.asList("Hy vọng", "Tin tưởng"));
        keywords.add(Arrays.asList("Hài hước", "Vui tươi"));
        keywords.add(Arrays.asList("Hài hước", "Dễ chịu"));
        keywords.add(Arrays.asList("Hài hước", "Thoải mái"));
        keywords.add(Arrays.asList("Lạc quan", "Yêu đời"));
        keywords.add(Arrays.asList("Vui vẻ", "Phấn khởi"));
        keywords.add(Arrays.asList("Vui vẻ", "Hào hứng"));
        keywords.add(Arrays.asList("Phấn khởi", "Hào hứng"));
        keywords.add(Arrays.asList("Lo lắng", "Hồi hộp"));
        keywords.add(Arrays.asList("Lo lắng", "Bồn chồn"));
        keywords.add(Arrays.asList("Lo lắng", "Căng thẳng"));
        keywords.add(Arrays.asList("Bồn chồn", "Căng thẳng"));
        keywords.add(Arrays.asList("Hồi hộp", "Căng thẳng"));
        keywords.add(Arrays.asList("Hồi hộp", "Bồn chồn"));
        keywords.add(Arrays.asList("Giận dữ", "Cáu kỉnh"));
        keywords.add(Arrays.asList("Bực bội", "Cáu kỉnh"));
        keywords.add(Arrays.asList("Sợ hãi", "Hoang mang"));
        keywords.add(Arrays.asList("Sợ hãi", "Lo âu"));
        keywords.add(Arrays.asList("Sợ hãi", "Bất an"));
        keywords.add(Arrays.asList("Lo âu", "Bất an"));
        keywords.add(Arrays.asList("Hoang mang", "Bất an"));
        keywords.add(Arrays.asList("Hoang mang", "Lo âu"));
        keywords.add(Arrays.asList("Mệt mỏi", "Ốm yếu"));
        keywords.add(Arrays.asList("Mệt mỏi", "Suy sụp"));
        keywords.add(Arrays.asList("Mệt mỏi", "Cảm cúm"));
        keywords.add(Arrays.asList("Ốm yếu", "Cảm cúm"));
        keywords.add(Arrays.asList("Suy sụp", "Cảm cúm"));
        keywords.add(Arrays.asList("Suy sụp", "Ốm yếu"));
        keywords.add(Arrays.asList("Hòa nhã", "Lịch sự"));
        keywords.add(Arrays.asList("Tôn trọng", "Lịch sự"));
        keywords.add(Arrays.asList("Hòa nhã", "Tôn trọng"));
        keywords.add(Arrays.asList("Nhiệt tình", "Hăng hái"));
        keywords.add(Arrays.asList("Nhiệt tình", "Hào hứng"));
        keywords.add(Arrays.asList("Nhiệt tình", "Sôi nổi"));
        keywords.add(Arrays.asList("Hào hứng", "Sôi nổi"));
        keywords.add(Arrays.asList("Hăng hái", "Sôi nổi"));
        keywords.add(Arrays.asList("Kiên định", "Tin cậy"));
        keywords.add(Arrays.asList("Cẩn thận", "Chín chắn"));
        keywords.add(Arrays.asList("Cẩn thận", "Thận trọng"));
        keywords.add(Arrays.asList("Cẩn thận", "Sáng suốt"));
        keywords.add(Arrays.asList("Chín chắn", "Sáng suốt"));
        keywords.add(Arrays.asList("Cần cù", "Chịu khó"));
        keywords.add(Arrays.asList("Kiên trì", "Chịu khó"));
        keywords.add(Arrays.asList("Chăm chỉ", "Chịu khó"));
        keywords.add(Arrays.asList("Chăm chỉ", "Kiên trì"));
        keywords.add(Arrays.asList("Nhạy bén", "Linh hoạt"));
        keywords.add(Arrays.asList("Khéo léo", "Linh hoạt"));
        keywords.add(Arrays.asList("Nhạy bén", "Khéo léo"));
        keywords.add(Arrays.asList("Mặt trời", "Mặt trăng"));

        keywords.add(Arrays.asList("Núi", "Đồi"));
        keywords.add(Arrays.asList("Gà", "Vịt"));
        keywords.add(Arrays.asList("Bơ", "Sữa"));
        keywords.add(Arrays.asList("Bánh mì", "Cơm"));
        keywords.add(Arrays.asList("Trà", "Cà phê"));
        keywords.add(Arrays.asList("Nước", "Nước ngọt"));
        keywords.add(Arrays.asList("Xe ô tô", "Xe máy"));
        keywords.add(Arrays.asList("Máy bay", "Tàu hỏa"));
        keywords.add(Arrays.asList("Điện thoại", "Máy tính"));
        keywords.add(Arrays.asList("Sách", "Báo"));
        keywords.add(Arrays.asList("Bút", "Thước"));
        keywords.add(Arrays.asList("Bàn", "Ghế"));
        keywords.add(Arrays.asList("Cửa", "Cửa sổ"));
        keywords.add(Arrays.asList("Áo", "Quần"));
        keywords.add(Arrays.asList("Giày", "Dép"));
        keywords.add(Arrays.asList("Mũ", "Nón"));
        keywords.add(Arrays.asList("Bóng đá", "Bóng rổ"));
        keywords.add(Arrays.asList("Chạy bộ", "Đi bộ"));
        keywords.add(Arrays.asList("Bơi lội", "Lặn"));
        keywords.add(Arrays.asList("Ca hát", "Nhảy múa"));
        keywords.add(Arrays.asList("Vẽ", "Viết"));
        keywords.add(Arrays.asList("Đọc", "Học"));
        keywords.add(Arrays.asList("Nghe", "Nói"));
        keywords.add(Arrays.asList("Ngửi", "Nếm"));
        keywords.add(Arrays.asList("Chạm", "Cầm"));
        keywords.add(Arrays.asList("Đỏ", "Xanh"));
        keywords.add(Arrays.asList("Vàng", "Xám"));
        keywords.add(Arrays.asList("Tím", "Hồng"));
        keywords.add(Arrays.asList("Ngọt", "Đắng"));
        keywords.add(Arrays.asList("Chua", "Mặn"));
        keywords.add(Arrays.asList("Con trai", "Con gái"));
        keywords.add(Arrays.asList("Anh", "Em"));
        keywords.add(Arrays.asList("Chị", "Em"));
        keywords.add(Arrays.asList("Ông", "Bà"));
        keywords.add(Arrays.asList("Thầy", "Cô"));
        keywords.add(Arrays.asList("Bạn", "Người lạ"));
        keywords.add(Arrays.asList("Hàng xóm", "Bạn bè"));
        keywords.add(Arrays.asList("Hắt hơi", "Xổ mũi"));
        keywords.add(Arrays.asList("Hắt hơi", "Ho"));
        keywords.add(Arrays.asList("Hắt hơi", "Thở dốc"));
        keywords.add(Arrays.asList("Ho", "Thở dốc"));
        keywords.add(Arrays.asList("Cay mắt", "Khóc"));
        keywords.add(Arrays.asList("Nhai", "Nuốt"));
        keywords.add(Arrays.asList("Vẫy tay", "Gõ tay"));
        keywords.add(Arrays.asList("Vẫy tay", "Vỗ tay"));
        keywords.add(Arrays.asList("Vẫy tay", "Bắt tay"));
        keywords.add(Arrays.asList("Vẫy tay", "Duỗi tay"));
        keywords.add(Arrays.asList("Vỗ tay", "Bắt tay"));
        keywords.add(Arrays.asList("Vỗ tay", "Nắm tay"));
        keywords.add(Arrays.asList("Duỗi tay", "Nắm tay"));
        keywords.add(Arrays.asList("Vỗ tay", "Duỗi tay"));
        keywords.add(Arrays.asList("Ngửi", "Hít"));
        keywords.add(Arrays.asList("Hít", "Thở sâu"));
        keywords.add(Arrays.asList("Sổ mũi", "Xì mũi"));
        keywords.add(Arrays.asList("Chớp mắt", "Nháy mắt"));
        keywords.add(Arrays.asList("Nhìn trộm", "Nhìn chăm chú"));
        keywords.add(Arrays.asList("Nhìn trộm", "Nheo mắt"));
        keywords.add(Arrays.asList("Nhìn xa", "Nhìn gần"));
        keywords.add(Arrays.asList("Nhìn trộm", "Nhìn gần"));
        keywords.add(Arrays.asList("Nhìn xa", "Nhìn trộm"));
        keywords.add(Arrays.asList("Dưa hấu", "Dưa gang"));
        keywords.add(Arrays.asList("Thanh long", "Mãng cầu"));
        keywords.add(Arrays.asList("quả Cam", "Quýt"));
        keywords.add(Arrays.asList("Bưởi", "Quýt"));
        keywords.add(Arrays.asList("quả Cam", "Chanh"));
        keywords.add(Arrays.asList("quả Cam", "Bưởi"));
        keywords.add(Arrays.asList("Chanh", "Quýt"));
        keywords.add(Arrays.asList("Chanh", "Bưởi"));
        keywords.add(Arrays.asList("Nhãn", "Vải"));
        keywords.add(Arrays.asList("Chôm chôm", "Vải"));
        keywords.add(Arrays.asList("Nhãn", "Chôm chôm"));
        keywords.add(Arrays.asList("Bơ", "Xoài"));
        keywords.add(Arrays.asList("Xoài", "Dứa"));
        keywords.add(Arrays.asList("Bơ", "Dứa"));
        keywords.add(Arrays.asList("Dứa", "Dừa"));
        keywords.add(Arrays.asList("Dừa", "Xoài"));
        keywords.add(Arrays.asList("Dừa", "Bơ"));
        keywords.add(Arrays.asList("Kiwi", "Nho"));
        keywords.add(Arrays.asList("Kiwi", "Lê"));
        keywords.add(Arrays.asList("Kiwi", "Táo"));
        keywords.add(Arrays.asList("Lê", "Nho"));
        keywords.add(Arrays.asList("Táo", "Nho"));
        keywords.add(Arrays.asList("Dâu tây", "Việt quất"));
        keywords.add(Arrays.asList("Mâm xôi", "Nho"));
        keywords.add(Arrays.asList("Nho", "Dâu tây"));
        keywords.add(Arrays.asList("Sư tử", "Hổ"));
        keywords.add(Arrays.asList("Báo", "Cọp"));
        keywords.add(Arrays.asList("Sư tử", "Báo"));;
        keywords.add(Arrays.asList("Sư tử", "Cọp"));
        keywords.add(Arrays.asList("Hổ", "Báo"));;
        keywords.add(Arrays.asList("Hổ", "Cọp"));
        keywords.add(Arrays.asList("Nai", "Hươu"));
        keywords.add(Arrays.asList("Nghé", "Bò tót"));
        keywords.add(Arrays.asList("Nai", "Bò tót"));
        keywords.add(Arrays.asList("Hươu", "Bò tót"));
        keywords.add(Arrays.asList("Ngựa", "Lừa"));
        keywords.add(Arrays.asList("Ngựa", "Bò"));
        keywords.add(Arrays.asList("Lừa", "Bò"));
        keywords.add(Arrays.asList("Sói", "Cáo"));
        keywords.add(Arrays.asList("Gấu xám", "Linh cẩu"));
        keywords.add(Arrays.asList("Sói", "Gấu xám"));
        keywords.add(Arrays.asList("Sói", "Linh cẩu"));
        keywords.add(Arrays.asList("Cáo", "Gấu xám"));
        keywords.add(Arrays.asList("Cáo", "Linh cẩu"));
        keywords.add(Arrays.asList("Gấu Bắc cực", "Gấu trúc"));
        keywords.add(Arrays.asList("Gấu Bắc cực", "Gấu mèo"));
        keywords.add(Arrays.asList("Gấu trúc", "Gấu mèo"));

        // Đời sống
        keywords.add(Arrays.asList("Tủ", "Kệ"));
        keywords.add(Arrays.asList("Chăn", "Gối"));
        keywords.add(Arrays.asList("Chảo", "Nồi"));
        keywords.add(Arrays.asList("Bát", "Đĩa"));
        keywords.add(Arrays.asList("Thảm", "Chiếu"));
        keywords.add(Arrays.asList("Quạt", "Điều hòa"));
        // Hành động
        keywords.add(Arrays.asList("Chạy", "Đi bộ"));
        keywords.add(Arrays.asList("Bơi", "Lặn"));
        keywords.add(Arrays.asList("Nhảy", "Múa"));
        keywords.add(Arrays.asList("Hát", "Ca"));
        keywords.add(Arrays.asList("Vẽ", "Tô màu"));
        keywords.add(Arrays.asList("Đọc", "Viết"));
        keywords.add(Arrays.asList("Chơi", "Nghịch"));
        keywords.add(Arrays.asList("Học", "Ôn tập"));
        keywords.add(Arrays.asList("Làm việc", "Công tác"));
        // Trái cây
        keywords.add(Arrays.asList("Táo", "Lê"));
        keywords.add(Arrays.asList("Chuối", "Đu đủ"));
        keywords.add(Arrays.asList("Nho", "Mận"));
        keywords.add(Arrays.asList("Xoài", "Mít"));
        keywords.add(Arrays.asList("Ổi", "Mận"));
        keywords.add(Arrays.asList("Chôm chôm", "Măng cụt"));
        keywords.add(Arrays.asList("Sầu riêng", "Mít"));
        keywords.add(Arrays.asList("Dứa", "Thơm"));
        // Rau củ
        keywords.add(Arrays.asList("Cà rốt", "Khoai tây"));
        keywords.add(Arrays.asList("Cải xanh", "Cải ngọt"));
        keywords.add(Arrays.asList("Bí đỏ", "Bí xanh"));
        keywords.add(Arrays.asList("Cà chua", "Ớt"));
        keywords.add(Arrays.asList("Hành", "Tỏi"));
        keywords.add(Arrays.asList("Rau muống", "Rau ngót"));
        keywords.add(Arrays.asList("Mướp", "Bầu"));
        keywords.add(Arrays.asList("Su hào", "Củ cải"));
        keywords.add(Arrays.asList("Cải thảo", "Cải bắp"));
        keywords.add(Arrays.asList("Đậu que", "Đậu cô ve"));
        keywords.add(Arrays.asList("Rau cải", "Rau thơm"));
        keywords.add(Arrays.asList("Cà tím", "Cà rốt"));
        keywords.add(Arrays.asList("Khoai lang", "Khoai môn"));
        keywords.add(Arrays.asList("Đậu xanh", "Đậu đỏ"));
        keywords.add(Arrays.asList("Nấm", "Mộc nhĩ"));
        keywords.add(Arrays.asList("Tỏi tây", "Hẹ"));
        keywords.add(Arrays.asList("Măng tây", "Măng cụt"));
        // Động vật
        keywords.add(Arrays.asList("Chó", "Mèo"));
        keywords.add(Arrays.asList("Cá", "Tôm"));
        keywords.add(Arrays.asList("Bò", "Trâu"));
        keywords.add(Arrays.asList("Khỉ", "Vượn"));
        keywords.add(Arrays.asList("Heo", "Lợn"));
        keywords.add(Arrays.asList("Voi", "Tê giác"));
        keywords.add(Arrays.asList("Cáo", "Chó sói"));
        keywords.add(Arrays.asList("Gấu", "Hươu"));
        keywords.add(Arrays.asList("Thỏ", "Chuột"));
        keywords.add(Arrays.asList("Rồng", "Phượng"));
        keywords.add(Arrays.asList("Kỳ nhông", "Thằn lằn"));
        keywords.add(Arrays.asList("Cú", "Vẹt"));
        keywords.add(Arrays.asList("Sò", "Ngao"));
        keywords.add(Arrays.asList("Cá mập", "Cá voi"));
        keywords.add(Arrays.asList("Hải cẩu", "Sư tử biển"));
        keywords.add(Arrays.asList("Ngọc trai", "San hô"));
        // Đồ dùng học tập
        keywords.add(Arrays.asList("Sách", "Vở"));
        keywords.add(Arrays.asList("Bút", "Viết"));
        keywords.add(Arrays.asList("Thước kẻ", "Compa"));
        keywords.add(Arrays.asList("Tẩy", "Gôm"));
        keywords.add(Arrays.asList("Bảng", "Phấn"));
        keywords.add(Arrays.asList("Cặp sách", "Ba lô"));
        keywords.add(Arrays.asList("Bút chì", "Bút mực"));
        keywords.add(Arrays.asList("Kéo", "Dập ghim"));
        keywords.add(Arrays.asList("Bìa sách", "Vỏ bút"));
        keywords.add(Arrays.asList("Sổ tay", "Nhật ký"));
        // Phương tiện giao thông
        keywords.add(Arrays.asList("Xe đạp", "Xe máy"));
        keywords.add(Arrays.asList("Ô tô", "Xe bus"));
        keywords.add(Arrays.asList("Tàu hỏa", "Tàu thủy"));
        keywords.add(Arrays.asList("Máy bay", "Trực thăng"));
        keywords.add(Arrays.asList("Xe tải", "Xe ben"));
        keywords.add(Arrays.asList("Xe buýt", "Xe khách"));
        keywords.add(Arrays.asList("Tàu điện ngầm", "Tàu điện trên cao"));
        keywords.add(Arrays.asList("Xe kéo", "Xe rùa"));
        keywords.add(Arrays.asList("Xe tăng", "Xe bọc thép"));
        // Địa điểm
        keywords.add(Arrays.asList("Nhà", "Căn hộ"));
        keywords.add(Arrays.asList("Trường học", "Đại học"));
        keywords.add(Arrays.asList("Bệnh viện", "Phòng khám"));
        keywords.add(Arrays.asList("Công viên", "Vườn thú"));
        keywords.add(Arrays.asList("Rạp chiếu phim", "Nhà hát"));
        keywords.add(Arrays.asList("Siêu thị", "Cửa hàng tiện lợi"));
        keywords.add(Arrays.asList("Ngân hàng", "ATM"));
        keywords.add(Arrays.asList("Bến xe", "Ga tàu"));
        keywords.add(Arrays.asList("Sân bay", "Cảng biển"));
        keywords.add(Arrays.asList("Thư viện", "Nhà sách"));
        // Nghề nghiệp
        keywords.add(Arrays.asList("Bác sĩ", "Y tá"));
        keywords.add(Arrays.asList("Giáo viên", "Giảng viên"));
        keywords.add(Arrays.asList("Kỹ sư", "Công nhân"));
        keywords.add(Arrays.asList("Nhà văn", "Nhà báo"));
        keywords.add(Arrays.asList("Nghệ sĩ", "Họa sĩ"));
        keywords.add(Arrays.asList("Nhạc sĩ", "Ca sĩ"));
        keywords.add(Arrays.asList("Diễn viên", "MC"));
        keywords.add(Arrays.asList("Lập trình viên", "Kỹ thuật viên"));
        keywords.add(Arrays.asList("Quản lý", "Giám đốc"));
        keywords.add(Arrays.asList("Nhân viên", "Cộng tác viên"));
        // Thực phẩm
        keywords.add(Arrays.asList("Bánh mì", "Bánh ngọt"));
        keywords.add(Arrays.asList("Phở", "Bún"));
        keywords.add(Arrays.asList("Cơm", "Xôi"));
        keywords.add(Arrays.asList("Cháo", "Súp"));
        keywords.add(Arrays.asList("Sữa", "Sữa chua"));
        keywords.add(Arrays.asList("Thịt bò", "Thịt heo"));
        keywords.add(Arrays.asList("Thịt gà", "Thịt vịt"));
        keywords.add(Arrays.asList("Cá hồi", "Cá ngừ"));
        keywords.add(Arrays.asList("Tôm", "Cua"));
        keywords.add(Arrays.asList("Mực", "Bạch tuộc"));
        keywords.add(Arrays.asList("Trứng gà", "Trứng vịt"));
        keywords.add(Arrays.asList("Bánh tráng", "Bánh đa"));
        keywords.add(Arrays.asList("Nước mắm", "Nước tương"));
        keywords.add(Arrays.asList("Dầu ăn", "Mỡ lợn"));
        keywords.add(Arrays.asList("Muối", "Đường"));
        keywords.add(Arrays.asList("Tiêu", "Ớt bột"));
        keywords.add(Arrays.asList("Bột mì", "Bột năng"));
        keywords.add(Arrays.asList("Bột gạo", "Bột sắn"));
        keywords.add(Arrays.asList("Sữa đặc", "Sữa tươi"));
        keywords.add(Arrays.asList("Nước lọc", "Nước khoáng"));
        // Cảm xúc
        keywords.add(Arrays.asList("Vui mừng", "Hạnh phúc"));
        keywords.add(Arrays.asList("Buồn bã", "Chán nản"));
        keywords.add(Arrays.asList("Giận dữ", "Bực bội"));
        keywords.add(Arrays.asList("Ngạc nhiên", "Bất ngờ"));
        keywords.add(Arrays.asList("Tự hào", "Hãnh diện"));
        keywords.add(Arrays.asList("Xúc động", "Cảm động"));
        keywords.add(Arrays.asList("Thư giãn", "Thoải mái"));
        keywords.add(Arrays.asList("Cô đơn", "Lạc lõng"));
        keywords.add(Arrays.asList("Ghen tị", "Đố kỵ"));
        // Thể thao
        keywords.add(Arrays.asList("Bóng đá", "Bóng chuyền"));
        keywords.add(Arrays.asList("Bóng rổ", "Bóng bàn"));
        keywords.add(Arrays.asList("Cầu lông", "Tennis"));
        keywords.add(Arrays.asList("Bơi lội", "Lặn biển"));
        keywords.add(Arrays.asList("Chạy bộ", "Đi bộ nhanh"));
        keywords.add(Arrays.asList("Đạp xe", "Leo núi"));
        keywords.add(Arrays.asList("Cờ vua", "Cờ tướng"));
        keywords.add(Arrays.asList("Bắn cung", "Bắn súng"));
        keywords.add(Arrays.asList("Đua xe", "Đua thuyền"));
        keywords.add(Arrays.asList("Trượt băng", "Trượt patin"));
        // Công nghệ
        keywords.add(Arrays.asList("Máy tính", "Laptop"));
//        keywords.add(Arrays.asList("Điện thoại", "Smartphone"));
        keywords.add(Arrays.asList("Máy in", "Máy photocopy"));
        keywords.add(Arrays.asList("Tivi", "Màn hình"));
        keywords.add(Arrays.asList("Chuột máy tính", "Bàn phím"));
        keywords.add(Arrays.asList("Máy ảnh", "Camera"));
        keywords.add(Arrays.asList("Loa", "Tai nghe"));
        keywords.add(Arrays.asList("USB", "Ổ cứng"));
        keywords.add(Arrays.asList("Pin", "Sạc"));
        // Thiên nhiên
        keywords.add(Arrays.asList("Sông", "Suối"));
        keywords.add(Arrays.asList("Biển", "Hồ"));
        keywords.add(Arrays.asList("Rừng", "Cây"));
        keywords.add(Arrays.asList("Hoa", "Lá"));
        keywords.add(Arrays.asList("Cỏ", "Rêu"));
        keywords.add(Arrays.asList("Đá", "Sỏi"));
        keywords.add(Arrays.asList("Cát", "Bùn"));
        keywords.add(Arrays.asList("Mây", "Sương"));
        keywords.add(Arrays.asList("Gió", "Làn gió"));
        // Gia đình
        keywords.add(Arrays.asList("Cha", "Mẹ"));
        keywords.add(Arrays.asList("Anh trai", "Em trai"));
        keywords.add(Arrays.asList("Chị gái", "Em gái"));
        keywords.add(Arrays.asList("Cô", "Dì"));
        keywords.add(Arrays.asList("Chú", "Bác"));
        keywords.add(Arrays.asList("Cháu", "Em bé"));
        keywords.add(Arrays.asList("Vợ", "Chồng"));
        // Giải trí
        keywords.add(Arrays.asList("Xem phim", "Xem kịch"));
        keywords.add(Arrays.asList("Nghe nhạc", "Hát karaoke"));
        keywords.add(Arrays.asList("Chơi game", "Chơi cờ"));
        keywords.add(Arrays.asList("Đọc sách", "Đọc báo"));
        keywords.add(Arrays.asList("Đi dạo", "Đi chơi"));
        keywords.add(Arrays.asList("Du lịch", "Dã ngoại"));
        keywords.add(Arrays.asList("Chụp ảnh", "Quay phim"));
        keywords.add(Arrays.asList("Nấu ăn", "Làm bánh"));
        keywords.add(Arrays.asList("Vẽ tranh", "Tô màu"));
        keywords.add(Arrays.asList("Làm vườn", "Trồng cây"));
        // Thời tiết
        keywords.add(Arrays.asList("Nắng", "Nắng nhẹ"));
        keywords.add(Arrays.asList("Mưa", "Mưa phùn"));
        keywords.add(Arrays.asList("Gió", "Gió nhẹ"));
        keywords.add(Arrays.asList("Sương mù", "Sương sớm"));
        keywords.add(Arrays.asList("Bão", "Áp thấp"));
        keywords.add(Arrays.asList("Lốc xoáy", "Gió lốc"));
        keywords.add(Arrays.asList("Tuyết", "Băng giá"));
        keywords.add(Arrays.asList("Mây", "Trời mây"));
        keywords.add(Arrays.asList("Trời quang", "Trời trong"));
        // Màu sắc
        keywords.add(Arrays.asList("Đỏ", "Hồng"));
        keywords.add(Arrays.asList("Vàng", "Cam"));
        keywords.add(Arrays.asList("Xanh lá", "Xanh dương"));
        keywords.add(Arrays.asList("Tím", "Tím nhạt"));
        keywords.add(Arrays.asList("Nâu", "Be"));
        keywords.add(Arrays.asList("Trắng", "Kem"));
        keywords.add(Arrays.asList("Đen", "Xám"));
        keywords.add(Arrays.asList("Bạc", "Vàng ánh kim"));
        keywords.add(Arrays.asList("Xanh ngọc", "Xanh lam"));
        keywords.add(Arrays.asList("Hồng phấn", "Hồng đào"));
        // Vật dụng cá nhân
        keywords.add(Arrays.asList("Áo sơ mi", "Áo thun"));
        keywords.add(Arrays.asList("Quần jeans", "Quần kaki"));
        keywords.add(Arrays.asList("Giày thể thao", "Giày lười"));
        keywords.add(Arrays.asList("Túi xách", "Balo"));
        keywords.add(Arrays.asList("Kính mắt", "Kính râm"));
        keywords.add(Arrays.asList("Đồng hồ", "Vòng tay"));
        keywords.add(Arrays.asList("Khăn quàng", "Khăn len"));
        keywords.add(Arrays.asList("Găng tay", "Tất"));
        // Động vật biển
        keywords.add(Arrays.asList("Cá heo", "Cá voi"));
        keywords.add(Arrays.asList("Cua biển", "Ghẹ"));
//        keywords.add(Arrays.asList("Sứa", "Sứa biển"));
//        keywords.add(Arrays.asList("Cá ngựa", "Cá mập búa"));
//        keywords.add(Arrays.asList("Cá trích", "Cá mòi"));
        keywords.add(Arrays.asList("Cá thu", "Cá ngừ đại dương"));
        keywords.add(Arrays.asList("Cá đuối", "Cá chim"));
        keywords.add(Arrays.asList("Cá bơn", "Cá mú"));
        keywords.add(Arrays.asList("Cá trê", "Cá lóc"));
        keywords.add(Arrays.asList("Cá rô", "Cá trắm"));
        // Động vật hoang dã
        keywords.add(Arrays.asList("Báo", "Báo đốm"));
        keywords.add(Arrays.asList("Hươu cao cổ", "Ngựa vằn"));
        keywords.add(Arrays.asList("Tê giác", "Hà mã"));
        keywords.add(Arrays.asList("Linh dương", "Linh miêu"));
        keywords.add(Arrays.asList("Chim cánh cụt", "Chim hải âu"));
        keywords.add(Arrays.asList("Chim đại bàng", "Chim ưng"));
        keywords.add(Arrays.asList("Chim sẻ", "Chim chích"));
        keywords.add(Arrays.asList("Chim sáo", "Chim sơn ca"));
        keywords.add(Arrays.asList("Chim công", "Chim trĩ"));
        keywords.add(Arrays.asList("Chim bồ câu", "Chim cu gáy"));
        // Thực vật
        keywords.add(Arrays.asList("Cây bàng", "Cây phượng"));
        keywords.add(Arrays.asList("Cây me", "Cây xoan"));
        keywords.add(Arrays.asList("Cây mít", "Cây sầu riêng"));
        keywords.add(Arrays.asList("Cây dừa", "Cây cau"));
        keywords.add(Arrays.asList("Cây tre", "Cây trúc"));
        keywords.add(Arrays.asList("Cây mai", "Cây đào"));
        keywords.add(Arrays.asList("Cây sung", "Cây vả"));
        keywords.add(Arrays.asList("Cây bưởi", "Cây cam"));
        keywords.add(Arrays.asList("Cây vú sữa", "Cây mận"));
        keywords.add(Arrays.asList("Cây xoài", "Cây ổi"));
        // Đồ dùng nhà bếp
        keywords.add(Arrays.asList("Dao", "Kéo"));
        keywords.add(Arrays.asList("Muỗng", "Nĩa"));
        keywords.add(Arrays.asList("Chảo chống dính", "Nồi inox"));
        keywords.add(Arrays.asList("Bếp ga", "Bếp điện"));
        keywords.add(Arrays.asList("Lò vi sóng", "Lò nướng"));
        keywords.add(Arrays.asList("Tủ lạnh", "Tủ đông"));
        keywords.add(Arrays.asList("Máy xay", "Máy ép"));
        keywords.add(Arrays.asList("Ấm siêu tốc", "Ấm đun nước"));
        keywords.add(Arrays.asList("Rổ", "Rá"));
        keywords.add(Arrays.asList("Thớt", "Rổ rá"));
        // Đồ dùng cá nhân
        keywords.add(Arrays.asList("Bàn chải", "Kem đánh răng"));
        keywords.add(Arrays.asList("Sữa tắm", "Dầu gội"));
        keywords.add(Arrays.asList("Lược", "Gương"));
        keywords.add(Arrays.asList("Khăn mặt", "Khăn tắm"));
        keywords.add(Arrays.asList("Nước hoa", "Lăn khử mùi"));
        keywords.add(Arrays.asList("Son môi", "Phấn má"));
        keywords.add(Arrays.asList("Kem dưỡng da", "Sữa rửa mặt"));
        keywords.add(Arrays.asList("Tông đơ", "Máy cạo râu"));
        keywords.add(Arrays.asList("Kẹp tóc", "Dây buộc tóc"));
        keywords.add(Arrays.asList("Bông tẩy trang", "Bông ngoáy tai"));
        // Đồ điện tử
        keywords.add(Arrays.asList("Máy tính bảng", "Điện thoại thông minh"));
        keywords.add(Arrays.asList("Máy nghe nhạc", "Máy ghi âm"));
        keywords.add(Arrays.asList("Máy chiếu", "Màn hình LED"));
        keywords.add(Arrays.asList("Ổ cứng di động", "Thẻ nhớ"));
        keywords.add(Arrays.asList("Pin sạc dự phòng", "Cáp sạc"));
        keywords.add(Arrays.asList("Bộ phát wifi", "Bộ khuếch đại sóng"));
        keywords.add(Arrays.asList("Micro", "Loa bluetooth"));
        keywords.add(Arrays.asList("Camera hành trình", "Camera an ninh"));
        keywords.add(Arrays.asList("Đồng hồ thông minh", "Vòng đeo tay thông minh"));
        keywords.add(Arrays.asList("Bàn phím cơ", "Chuột không dây"));
        // Địa danh
        // Nghề truyền thống
        keywords.add(Arrays.asList("Thợ mộc", "Thợ xây"));
        keywords.add(Arrays.asList("Thợ may", "Thợ thêu"));
        keywords.add(Arrays.asList("Thợ rèn", "Thợ hàn"));
        keywords.add(Arrays.asList("Thợ gốm", "Thợ sơn"));
        keywords.add(Arrays.asList("Thợ bạc", "Thợ kim hoàn"));
        keywords.add(Arrays.asList("Thợ làm bánh", "Đầu bếp"));
        keywords.add(Arrays.asList("Thợ điện", "Thợ nước"));
        keywords.add(Arrays.asList("Thợ sửa xe", "Thợ máy"));
        keywords.add(Arrays.asList("Thợ cắt tóc", "Thợ làm móng"));
        keywords.add(Arrays.asList("Thợ ảnh", "Thợ quay phim"));
        // Đồ dùng văn phòng
        keywords.add(Arrays.asList("Bàn làm việc", "Ghế xoay"));
        keywords.add(Arrays.asList("Kẹp giấy", "Bấm kim"));
        keywords.add(Arrays.asList("Giấy note", "Giấy in"));
        keywords.add(Arrays.asList("Bìa hồ sơ", "Bìa trình ký"));
        keywords.add(Arrays.asList("Điện thoại bàn", "Điện thoại di động"));
        keywords.add(Arrays.asList("Tủ tài liệu", "Kệ sách"));
        keywords.add(Arrays.asList("Bảng trắng", "Bảng ghim"));
        keywords.add(Arrays.asList("Bút dạ", "Bút bi"));
        // Đồ chơi trẻ em
        keywords.add(Arrays.asList("Búp bê", "Gấu bông"));
        keywords.add(Arrays.asList("Ô tô đồ chơi", "Xe máy đồ chơi"));
        keywords.add(Arrays.asList("Xếp hình", "Lắp ghép"));
        keywords.add(Arrays.asList("Bóng bay", "Bóng nhựa"));
        keywords.add(Arrays.asList("Cầu trượt", "Xích đu"));
        keywords.add(Arrays.asList("Bập bênh", "Thú nhún"));
        keywords.add(Arrays.asList("Đồ chơi nấu ăn", "Đồ chơi bác sĩ"));
        keywords.add(Arrays.asList("Đồ chơi xếp chữ", "Đồ chơi xếp số"));
        keywords.add(Arrays.asList("Đồ chơi gỗ", "Đồ chơi nhựa"));
        keywords.add(Arrays.asList("Đồ chơi điều khiển", "Đồ chơi lắp ráp"));
        // Đồ dùng học sinh
        keywords.add(Arrays.asList("Bảng con", "Bút lông"));
        keywords.add(Arrays.asList("Giấy kiểm tra", "Giấy nháp"));
        keywords.add(Arrays.asList("Bút màu", "Bút chì màu"));
        keywords.add(Arrays.asList("Bút xóa", "Bút dạ quang"));
        keywords.add(Arrays.asList("Sách giáo khoa", "Sách bài tập"));
        keywords.add(Arrays.asList("Vở bài tập", "Vở ghi"));
        keywords.add(Arrays.asList("Bảng nhóm", "Bảng phụ"));
        keywords.add(Arrays.asList("Thước đo góc", "Thước đo độ"));
        keywords.add(Arrays.asList("Bút mực tím", "Bút mực xanh"));
        keywords.add(Arrays.asList("Bút bi đỏ", "Bút bi xanh"));
        // Thực phẩm bổ sung
        keywords.add(Arrays.asList("Bánh bao", "Bánh giò"));
        keywords.add(Arrays.asList("Bánh cuốn", "Bánh ướt"));
        keywords.add(Arrays.asList("Bánh xèo", "Bánh khọt"));
        keywords.add(Arrays.asList("Bánh bèo", "Bánh bột lọc"));
        keywords.add(Arrays.asList("Bánh tét", "Bánh chưng"));
        keywords.add(Arrays.asList("Bánh da lợn", "Bánh bò"));
        keywords.add(Arrays.asList("Bánh pía", "Bánh in"));
        keywords.add(Arrays.asList("Bánh cốm", "Bánh dày"));
        keywords.add(Arrays.asList("Bánh gai", "Bánh ít"));
        // Động vật bổ sung
        keywords.add(Arrays.asList("Cá chép", "Cá rô phi"));
        keywords.add(Arrays.asList("Cá bống", "Cá linh"));
        keywords.add(Arrays.asList("Cá cơm", "Cá nục"));
        keywords.add(Arrays.asList("Cá thu nhật", "Cá saba"));
        keywords.add(Arrays.asList("Cá trích biển", "Cá mòi biển"));
        keywords.add(Arrays.asList("Cá lăng", "Cá chình"));
        keywords.add(Arrays.asList("Cá bống mú", "Cá mú nghệ"));
        // Thực vật bổ sung
        // Cảm xúc bổ sung
        keywords.add(Arrays.asList("Phấn khích", "Hào hứng"));
        keywords.add(Arrays.asList("Bối rối", "Ngượng ngùng"));
        keywords.add(Arrays.asList("Bình tĩnh", "Điềm đạm"));
        keywords.add(Arrays.asList("Thất vọng", "Chán chường"));
        keywords.add(Arrays.asList("Ngưỡng mộ", "Kính trọng"));
        keywords.add(Arrays.asList("Thương cảm", "Đồng cảm"));
        keywords.add(Arrays.asList("Tò mò", "Hiếu kỳ"));
        keywords.add(Arrays.asList("Sợ hãi", "Lo sợ"));
        keywords.add(Arrays.asList("Tức giận", "Cáu gắt"));
        keywords.add(Arrays.asList("Hài lòng", "Mãn nguyện"));
        // Thể thao bổ sung
        keywords.add(Arrays.asList("Bóng chuyền bãi biển", "Bóng chuyền trong nhà"));
        keywords.add(Arrays.asList("Bóng đá mini", "Bóng đá sân lớn"));
        keywords.add(Arrays.asList("Bóng bàn ", "Tenis"));
        keywords.add(Arrays.asList("Cầu lông", "Tenis"));
        keywords.add(Arrays.asList("Bơi tự do", "Bơi ếch"));
        keywords.add(Arrays.asList("Bơi bướm", "Bơi ngửa"));
        keywords.add(Arrays.asList("Đua xe đạp", "Đua xe máy"));
//        keywords.add(Arrays.asList("Đua thuyền kayak", "Đua thuyền canoe"));
        keywords.add(Arrays.asList("Trượt tuyết", "Trượt ván"));
        keywords.add(Arrays.asList("Leo núi đá", "Leo núi nhân tạo"));
        // Công nghệ bổ sung
        keywords.add(Arrays.asList("Máy tính để bàn", "Máy tính xách tay"));
//        keywords.add(Arrays.asList("Điện thoại thông minh", "Điện thoại cảm ứng"));
        keywords.add(Arrays.asList("Máy in màu", "Máy in đen trắng"));
        keywords.add(Arrays.asList("Máy ảnh số", "Máy ảnh phim"));
        keywords.add(Arrays.asList("Loa bluetooth", "Loa vi tính"));
        keywords.add(Arrays.asList("Tai nghe chụp tai", "Tai nghe nhét tai"));
        keywords.add(Arrays.asList("Ổ cứng SSD", "Ổ cứng HDD"));
        keywords.add(Arrays.asList("Router wifi", "Modem wifi"));
        keywords.add(Arrays.asList("Pin sạc dự phòng", "Quạt cầm tay"));
        keywords.add(Arrays.asList("Bàn phím không dây", "Chuột không dây"));
        // Giải trí bổ sung
        keywords.add(Arrays.asList("Xem hài", "Xem phim hoạt hình"));
        keywords.add(Arrays.asList("Chơi nhạc cụ", "Học nhạc cụ"));
        keywords.add(Arrays.asList("Chơi cờ vua", "Chơi cờ tướng"));
        keywords.add(Arrays.asList("Chơi bóng rổ", "Chơi bóng chuyền"));
        keywords.add(Arrays.asList("Chơi cầu lông", "Chơi tennis"));
        keywords.add(Arrays.asList("Chơi game online", "Chơi game offline"));
        keywords.add(Arrays.asList("Đọc truyện tranh", "Đọc tiểu thuyết"));
        keywords.add(Arrays.asList("Xem ca nhạc", "Xem kịch"));
        keywords.add(Arrays.asList("Đi picnic", "Đi dã ngoại"));
        keywords.add(Arrays.asList("Chụp ảnh phong cảnh", "Chụp ảnh chân dung"));
        // Vật dụng bổ sung
        keywords.add(Arrays.asList("Bàn phím cơ", "Bàn phím giả cơ"));
        keywords.add(Arrays.asList("Chuột có dây", "Chuột không dây"));
        keywords.add(Arrays.asList("Bình giữ nhiệt", "Bình nước"));
        keywords.add(Arrays.asList("Cốc thủy tinh", "Cốc nhựa"));
        keywords.add(Arrays.asList("Tủ lạnh mini", "Tủ lạnh lớn"));
        keywords.add(Arrays.asList("Quạt đứng", "Quạt treo tường"));
        keywords.add(Arrays.asList("Đèn bàn", "Đèn ngủ"));
        keywords.add(Arrays.asList("Ghế gấp", "Ghế xoay"));
        keywords.add(Arrays.asList("Bàn học", "Bàn làm việc"));
        keywords.add(Arrays.asList("Tủ quần áo", "Tủ giày"));
        // Thiên nhiên bổ sung
        keywords.add(Arrays.asList("Thác nước", "Suối nguồn"));
        keywords.add(Arrays.asList("Đồi cỏ", "Đồi thông"));
        keywords.add(Arrays.asList("Cánh đồng", "Ruộng lúa"));
        keywords.add(Arrays.asList("Đầm lầy", "Ao hồ"));
        keywords.add(Arrays.asList("Bãi biển", "Bờ biển"));
        keywords.add(Arrays.asList("Đảo nhỏ", "Đảo lớn"));
        keywords.add(Arrays.asList("Hang động", "Động đá vôi"));
        keywords.add(Arrays.asList("Núi lửa", "Núi đá"));
        keywords.add(Arrays.asList("Rừng thông", "Rừng tràm"));
        keywords.add(Arrays.asList("Rừng ngập mặn", "Rừng nguyên sinh"));
        // Thời tiết bổ sung
        keywords.add(Arrays.asList("Mưa rào", "Mưa phùn"));
        keywords.add(Arrays.asList("Nắng gắt", "Nắng nhẹ"));
        keywords.add(Arrays.asList("Gió mùa", "Gió tây nam"));
        keywords.add(Arrays.asList("Sương giá", "Sương mù"));
        keywords.add(Arrays.asList("Lốc xoáy", "Lốc cát"));
        keywords.add(Arrays.asList("Tuyết rơi", "Băng giá"));
        keywords.add(Arrays.asList("Sấm sét", "Chớp giật"));
        keywords.add(Arrays.asList("Mây mù", "Mây trắng"));
        // Màu sắc bổ sung
        keywords.add(Arrays.asList("Xanh lá cây", "Xanh rêu"));
        keywords.add(Arrays.asList("Xanh dương nhạt", "Xanh dương đậm"));
        keywords.add(Arrays.asList("Đỏ tươi", "Đỏ đậm"));
        keywords.add(Arrays.asList("Vàng chanh", "Vàng nghệ"));
        keywords.add(Arrays.asList("Tím than", "Tím hoa cà"));
        keywords.add(Arrays.asList("Nâu đất", "Nâu socola"));
        keywords.add(Arrays.asList("Trắng sữa", "Trắng ngà"));
        keywords.add(Arrays.asList("Đen tuyền", "Đen nhánh"));
        keywords.add(Arrays.asList("Hồng sen", "Hồng đào"));
        keywords.add(Arrays.asList("Cam đất", "Cam vàng"));
        // Gia đình bổ sung
//        keywords.add(Arrays.asList("Anh chị", "Em út"));
        keywords.add(Arrays.asList("Ông bà nội", "Ông bà ngoại"));
        keywords.add(Arrays.asList("Anh rể", "Em rể"));
        keywords.add(Arrays.asList("Em dâu", "Chị dâu"));
        keywords.add(Arrays.asList("Bố chồng", "Bố vợ"));
        keywords.add(Arrays.asList("Bố vợ", "Mẹ chồng"));
        keywords.add(Arrays.asList("Con nuôi", "Con ruột"));
        // Học tập bổ sung
        keywords.add(Arrays.asList("Bài kiểm tra", "Bài thi"));
        keywords.add(Arrays.asList("Bài tập về nhà", "Bài tập nhóm"));
        keywords.add(Arrays.asList("Giáo trình", "Sách tham khảo"));
        keywords.add(Arrays.asList("Bảng điểm", "Phiếu điểm"));
        keywords.add(Arrays.asList("Lớp học", "Phòng học"));
        keywords.add(Arrays.asList("Giáo viên chủ nhiệm", "Giáo viên bộ môn"));
        keywords.add(Arrays.asList("Sinh viên", "Học sinh"));
        keywords.add(Arrays.asList("Thạc sĩ", "Tiến sĩ"));
        keywords.add(Arrays.asList("Bài luận", "Bài báo cáo"));
        keywords.add(Arrays.asList("Đề cương", "Đề thi"));
        keywords.add(Arrays.asList("Tốt bụng", "Nhân từ"));
        keywords.add(Arrays.asList("Thẳng thắn", "Thành thật"));
        keywords.add(Arrays.asList("Chân thành", "Thành thật"));
        keywords.add(Arrays.asList("Khiêm tốn", "Nhún nhường"));
        keywords.add(Arrays.asList("Hiền lành", "Hiền hậu"));
        keywords.add(Arrays.asList("Hiền lành", "Nhút nhát"));
        keywords.add(Arrays.asList("Dịu dàng", "Nhẹ nhàng"));
        keywords.add(Arrays.asList("Dịu dàng", "Hiền dịu"));
        keywords.add(Arrays.asList("Ân cần", "Chu đáo"));
        keywords.add(Arrays.asList("Cởi mở", "Thân thiện"));
        keywords.add(Arrays.asList("Hào phóng", "Rộng rãi"));
        keywords.add(Arrays.asList("Tiết kiệm", "Tằn tiện"));
        keywords.add(Arrays.asList("Lanh lợi", "Nhanh nhẹn"));
        keywords.add(Arrays.asList("Lanh lợi", "Hoạt bát"));
        keywords.add(Arrays.asList("Nhanh nhẹn", "Hoạt bát"));
        keywords.add(Arrays.asList("Chăm chỉ", "Cần mẫn"));
        keywords.add(Arrays.asList("Cần mẫn", "Cần cù"));
        keywords.add(Arrays.asList("Thận trọng", "Cẩn trọng"));
        keywords.add(Arrays.asList("Nghiêm túc", "Nghiêm chỉnh"));
        keywords.add(Arrays.asList("Điềm tĩnh", "Bình thản"));
        keywords.add(Arrays.asList("Bình thản", "Thản nhiên"));
        keywords.add(Arrays.asList("Ngoan ngoãn", "Vâng lời"));
        keywords.add(Arrays.asList("Lễ phép", "Lịch thiệp"));
        keywords.add(Arrays.asList("Lịch sự", "Lịch thiệp"));
        keywords.add(Arrays.asList("Thông minh", "Lanh lợi"));
        keywords.add(Arrays.asList("Thông minh", "Sắc sảo"));
        keywords.add(Arrays.asList("Nhạy cảm", "Tinh tế"));
        keywords.add(Arrays.asList("Tinh tế", "Tế nhị"));
        keywords.add(Arrays.asList("Tế nhị", "Khéo léo"));

// Đồng nghĩa / gần nghĩa - Cảm xúc
        keywords.add(Arrays.asList("Vui vẻ", "Vui tươi"));
        keywords.add(Arrays.asList("Vui mừng", "Phấn khởi"));
        keywords.add(Arrays.asList("Hạnh phúc", "Sung sướng"));
        keywords.add(Arrays.asList("Sung sướng", "Mãn nguyện"));
        keywords.add(Arrays.asList("Buồn bã", "Sầu muộn"));
        keywords.add(Arrays.asList("Sầu muộn", "Ưu sầu"));
        keywords.add(Arrays.asList("Đau lòng", "Đau khổ"));
        keywords.add(Arrays.asList("Đau khổ", "Khổ sở"));
        keywords.add(Arrays.asList("Khổ sở", "Cực khổ"));
        keywords.add(Arrays.asList("Lo lắng", "Lo âu"));
        keywords.add(Arrays.asList("Hoảng loạn", "Hoảng sợ"));
        keywords.add(Arrays.asList("Hoảng sợ", "Kinh hoàng"));
        keywords.add(Arrays.asList("Kinh hoàng", "Khiếp sợ"));
        keywords.add(Arrays.asList("Giận dữ", "Tức giận"));
        keywords.add(Arrays.asList("Tức giận", "Phẫn nộ"));
        keywords.add(Arrays.asList("Bực bội", "Bực mình"));
        keywords.add(Arrays.asList("Ngạc nhiên", "Ngỡ ngàng"));
        keywords.add(Arrays.asList("Ngỡ ngàng", "Bỡ ngỡ"));
        keywords.add(Arrays.asList("Xấu hổ", "Ngượng ngùng"));
        keywords.add(Arrays.asList("Ngượng ngùng", "Bẽn lẽn"));
        keywords.add(Arrays.asList("Chán nản", "Thất vọng"));
        keywords.add(Arrays.asList("Chán nản", "Nản lòng"));
        keywords.add(Arrays.asList("Nản lòng", "Nản chí"));
        keywords.add(Arrays.asList("Tự hào", "Kiêu hãnh"));
        keywords.add(Arrays.asList("Hối hận", "Ăn năn"));
        keywords.add(Arrays.asList("Ăn năn", "Hối tiếc"));
        keywords.add(Arrays.asList("Nhớ nhung", "Thương nhớ"));
        keywords.add(Arrays.asList("Yêu thương", "Trìu mến"));
        keywords.add(Arrays.asList("Trìu mến", "Âu yếm"));
        keywords.add(Arrays.asList("Căng thẳng", "Áp lực"));
        keywords.add(Arrays.asList("Thoải mái", "Thư thái"));

// Đồng nghĩa / gần nghĩa - Hành động
        keywords.add(Arrays.asList("Nói", "Trò chuyện"));
        keywords.add(Arrays.asList("Trò chuyện", "Tâm sự"));
        keywords.add(Arrays.asList("Tâm sự", "Chia sẻ"));
        keywords.add(Arrays.asList("Nhìn", "Ngắm"));
        keywords.add(Arrays.asList("Ngắm", "Quan sát"));
        keywords.add(Arrays.asList("Quan sát", "Theo dõi"));
        keywords.add(Arrays.asList("Chạy", "Phi nước đại"));
        keywords.add(Arrays.asList("Nhảy", "Bật"));
        keywords.add(Arrays.asList("Leo", "Trèo"));
        keywords.add(Arrays.asList("Ngã", "Vấp"));
        keywords.add(Arrays.asList("Đẩy", "Xô"));
        keywords.add(Arrays.asList("Kéo", "Lôi"));
        keywords.add(Arrays.asList("Ném", "Quăng"));
        keywords.add(Arrays.asList("Bắt", "Đỡ"));
        keywords.add(Arrays.asList("Cắt", "Chặt"));
        keywords.add(Arrays.asList("Xé", "Rách"));
        keywords.add(Arrays.asList("Gõ", "Đập"));
        keywords.add(Arrays.asList("Đánh", "Đập"));
        keywords.add(Arrays.asList("Ôm", "Ghì"));
        keywords.add(Arrays.asList("Nắm", "Giữ"));
        keywords.add(Arrays.asList("Thả", "Buông"));
        keywords.add(Arrays.asList("Đóng", "Khép"));
        keywords.add(Arrays.asList("Nhặt", "Lượm"));
        keywords.add(Arrays.asList("Vứt", "Ném"));
        keywords.add(Arrays.asList("Xây", "Dựng"));
        keywords.add(Arrays.asList("Sửa", "Chữa"));
        keywords.add(Arrays.asList("Sửa chữa", "Khắc phục"));

// Đồng nghĩa / gần nghĩa - Trạng thái / Mô tả
        keywords.add(Arrays.asList("To lớn", "Khổng lồ"));
        keywords.add(Arrays.asList("Nhỏ bé", "Tí hon"));
        keywords.add(Arrays.asList("Cao lớn", "Cao ráo"));
        keywords.add(Arrays.asList("Thấp bé", "Lùn"));
        keywords.add(Arrays.asList("Gầy", "Ốm"));
        keywords.add(Arrays.asList("Đẹp", "Xinh"));
        keywords.add(Arrays.asList("Xinh đẹp", "Xinh xắn"));
        keywords.add(Arrays.asList("Sáng sủa", "Rạng rỡ"));
        keywords.add(Arrays.asList("Rạng rỡ", "Rực rỡ"));
        keywords.add(Arrays.asList("Tối tăm", "Tối mờ"));
        keywords.add(Arrays.asList("Nhanh", "Mau"));
        keywords.add(Arrays.asList("Mạnh", "Khoẻ"));
        keywords.add(Arrays.asList("Sắc bén", "Nhọn"));
        keywords.add(Arrays.asList("Cứng", "Rắn"));
        keywords.add(Arrays.asList("Hẹp", "Chật"));
        keywords.add(Arrays.asList("Sâu", "Thẳm"));
        keywords.add(Arrays.asList("Nông", "Cạn"));
        keywords.add(Arrays.asList("Nóng", "Oi bức"));
        keywords.add(Arrays.asList("Dở", "Nhạt nhẽo"));

// Đồng nghĩa / gần nghĩa - Thiên nhiên / Thời gian
        keywords.add(Arrays.asList("Ban ngày", "Ban mai"));
        keywords.add(Arrays.asList("Sáng sớm", "Bình minh"));
        keywords.add(Arrays.asList("Buổi trưa", "Giữa trưa"));
        keywords.add(Arrays.asList("Buổi chiều", "Xế chiều"));
        keywords.add(Arrays.asList("Ban đêm", "Đêm khuya"));
        keywords.add(Arrays.asList("Nửa đêm", "Đêm khuya"));
        keywords.add(Arrays.asList("Mùa xuân", "Mùa hoa"));
        keywords.add(Arrays.asList("Mùa hè", "Mùa nóng"));
        keywords.add(Arrays.asList("Mùa đông", "Mùa lạnh"));
        keywords.add(Arrays.asList("Năm ngoái", "Năm qua"));
        keywords.add(Arrays.asList("Ngày xưa", "Ngày trước"));
        keywords.add(Arrays.asList("Tương lai", "Mai sau"));

// Đồng nghĩa / gần nghĩa - Không gian
        keywords.add(Arrays.asList("Trong nhà", "Nội thất"));
        keywords.add(Arrays.asList("Ngoài trời", "Ngoại thất"));
        keywords.add(Arrays.asList("Ở giữa", "Trung tâm"));

// Đồng nghĩa / gần nghĩa - Giao tiếp / Ngôn ngữ
        keywords.add(Arrays.asList("Nói chuyện", "Trò chuyện"));
        keywords.add(Arrays.asList("Thảo luận", "Trao đổi"));
        keywords.add(Arrays.asList("Tranh luận", "Cãi nhau"));
        keywords.add(Arrays.asList("Hỏi", "Thắc mắc"));
        keywords.add(Arrays.asList("Trả lời", "Đáp lại"));
        keywords.add(Arrays.asList("Giải thích", "Giảng giải"));
        keywords.add(Arrays.asList("Thuyết trình", "Trình bày"));
        keywords.add(Arrays.asList("Kể chuyện", "Kể lể"));
        keywords.add(Arrays.asList("Phàn nàn", "Càu nhàu"));
        keywords.add(Arrays.asList("Khen ngợi", "Tán dương"));
        keywords.add(Arrays.asList("Chê bai", "Chỉ trích"));
        keywords.add(Arrays.asList("Chỉ trích", "Phê bình"));
        keywords.add(Arrays.asList("Xin lỗi", "Tạ lỗi"));
        keywords.add(Arrays.asList("Cảm ơn", "Biết ơn"));
        keywords.add(Arrays.asList("Chào hỏi", "Vấn an"));
        keywords.add(Arrays.asList("Tạm biệt", "Từ biệt"));
        keywords.add(Arrays.asList("Khuyên bảo", "Khuyên nhủ"));
        keywords.add(Arrays.asList("Ra lệnh", "Sai bảo"));
        keywords.add(Arrays.asList("Năn nỉ", "Cầu xin"));

// Đồng nghĩa / gần nghĩa - Học tập / Tri thức
        keywords.add(Arrays.asList("Học hỏi", "Tiếp thu"));
        keywords.add(Arrays.asList("Hiểu", "Nắm bắt"));
        keywords.add(Arrays.asList("Nghiên cứu", "Tìm hiểu"));
        keywords.add(Arrays.asList("Tìm hiểu", "Khám phá"));
        keywords.add(Arrays.asList("Phân tích", "Mổ xẻ"));
        keywords.add(Arrays.asList("Tổng hợp", "Đúc kết"));
        keywords.add(Arrays.asList("Sáng tạo", "Phát minh"));
        keywords.add(Arrays.asList("Phát minh", "Phát kiến"));
        keywords.add(Arrays.asList("Ứng dụng", "Thực hành"));
        keywords.add(Arrays.asList("Thực hành", "Thực tập"));
        keywords.add(Arrays.asList("Kiểm tra", "Kiểm soát"));
        keywords.add(Arrays.asList("Đánh giá", "Nhận xét"));
        keywords.add(Arrays.asList("Nhận xét", "Phản hồi"));

// Đồng nghĩa / gần nghĩa - Công việc / Xã hội
        keywords.add(Arrays.asList("Làm việc", "Lao động"));
        keywords.add(Arrays.asList("Lao động", "Công tác"));
        keywords.add(Arrays.asList("Hợp tác", "Phối hợp"));
        keywords.add(Arrays.asList("Cạnh tranh", "Thi đua"));
        keywords.add(Arrays.asList("Thành công", "Thắng lợi"));
        keywords.add(Arrays.asList("Thất bại", "Thua cuộc"));
        keywords.add(Arrays.asList("Cố gắng", "Nỗ lực"));
        keywords.add(Arrays.asList("Hy sinh", "Cống hiến"));
        keywords.add(Arrays.asList("Phục vụ", "Cống hiến"));
        keywords.add(Arrays.asList("Lãnh đạo", "Chỉ huy"));
        keywords.add(Arrays.asList("Chỉ huy", "Điều hành"));
        keywords.add(Arrays.asList("Quản lý", "Điều hành"));
        keywords.add(Arrays.asList("Kinh doanh", "Buôn bán"));
        keywords.add(Arrays.asList("Trao đổi", "Giao dịch"));
        keywords.add(Arrays.asList("Tiết kiệm", "Dành dụm"));

// Đồng nghĩa / gần nghĩa - Sức khoẻ / Y tế
        keywords.add(Arrays.asList("Khoẻ mạnh", "Bình an"));
        keywords.add(Arrays.asList("Ốm đau", "Bệnh tật"));
        keywords.add(Arrays.asList("Chữa bệnh", "Điều trị"));
        keywords.add(Arrays.asList("Phục hồi", "Bình phục"));
        keywords.add(Arrays.asList("Đau đớn", "Đau nhức"));
        keywords.add(Arrays.asList("Đau nhức", "Nhức mỏi"));
        keywords.add(Arrays.asList("Chóng mặt", "Hoa mắt"));
        keywords.add(Arrays.asList("Buồn nôn", "Nôn nao"));
        keywords.add(Arrays.asList("Sưng", "Phù nề"));
        keywords.add(Arrays.asList("Rách", "Trầy xước"));
        keywords.add(Arrays.asList("Khỏe", "Mạnh"));
        keywords.add(Arrays.asList("Tập thể dục", "Tập luyện"));
        keywords.add(Arrays.asList("Nghỉ ngơi", "Thư giãn"));
        keywords.add(Arrays.asList("Ngủ", "Nghỉ ngơi"));
        keywords.add(Arrays.asList("Thức dậy", "Thức giấc"));
        return keywords;
    }

    public String valueDataSpy() {
        return  "Bệnh viện, " +
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
    }

    public void prepareDataMaSoi(List<DataMember> datas) {
        if (datas.isEmpty()) {
            datas.add(createDM("1", "Sói", "Sói đó, sói nè, sói chính hiệu 9999"));
            datas.add(createDM("2", "Sói đầu đàn", "Sói có quyền quyết định cắn ai"));
            datas.add(createDM("3", "Sói nguyền", "Nguyền 1 người chơi, người đó sẽ là sói"));
            datas.add(createDM("4", "Sói đầu đàn(2)", "Sói tiên tri phải soi 2 lần"));
            datas.add(createDM("5", "Sói si đa", "1 lần duy nhất chức năng nào chạm vào bạn thì chức năng đó chết"));
            datas.add(createDM("6", "Kẻ phản bội", "Theo phe sói, biết sói là ai, sói không biết bạn"));

            datas.add(createDM("20", "Sát thủ", "Phe thứ 3, có quyền giết sói nhưng sói ko giết lại được."));
            datas.add(createDM("21", "Chán đời", "Muốn chết nhưng ko muốn bị sói cắn. Khi bị treo cổ bạn sẽ win"));

            datas.add(createDM("30", "Dân", "Dân giàu nước mạnh"));
            datas.add(createDM("31", "Phù thủy", "Có 1 bình cứu và 1 bình giết. Khi đã sài phép cứu thì sẽ ko biết ai bị giết nữa"));
            datas.add(createDM("32", "Tiên tri", "Soi 1 người có phải Sói hay ko"));
            datas.add(createDM("33", "Bảo vệ", "Mỗi đêm bảo vệ 1 người, ko bảo vệ 1 người 2 đêm liên tiếp"));
            datas.add(createDM("34", "Thợ săn", "Ghim 1 người, chỉ trong đêm nếu bạn chết người đó chết theo"));
            datas.add(createDM("35", "Thanh niên cứng", "Khi bị treo cổ có quyền lật bài và giết 1 người, bạn vẫn sống như bình thường"));
            datas.add(createDM("37", "Tiên tri tập sự", "Khi tiên tri chết thì bạn là Tiên tri"));
            datas.add(createDM("38", "Câm lặng", "Chọn 1 người và cấm phép người đó."));
            datas.add(createDM("39", "Thám tử", "Chọn 1 vùng 3 người, bạn sẽ biết có sói trong đó hay không"));
            datas.add(createDM("40", "Bị nguyền", "Bạn theo phe dân, nếu bị sói cắn sẽ thành sói"));

            datas.add(createDM("41", "Người bệnh", "Nếu sói/sát thủ cắn bạn, thì đêm sau sói/sát thủ không giết được ai"));
            datas.add(createDM("42", "Nhân bản", "Chọn 1 người, nếu người đó chết bạn sẽ nhận chức năng người đó"));
            datas.add(createDM("43", "Độc tài", "Duy nhất: chọn 1 người chơi, nếu không phải dân người đó chết, nếu dân bạn chết"));
            datas.add(createDM("44", "Thiên thần", "1 lần duy nhất có thể ngăn chặn toàn bộ cái chết trong đêm"));
            datas.add(createDM("45", "Phù thủy già", "mỗi ngày đuổi 1 người ko phải mình ra khỏi làng"));
            datas.add(createDM("46", "Boooooom", "Duy nhất chọn 1 người chơi giao bom, mỗi đêm bạn có quyền kích nổ hoặc ko"));
        }
    }

    private DataMember createDM(String id, String location, String description) {
        return DataMember.builder().id(id).location(location).description(description).build();
    }
}
