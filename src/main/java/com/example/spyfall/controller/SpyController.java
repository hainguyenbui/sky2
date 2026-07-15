package com.example.spyfall.controller;

import com.example.spyfall.service.SpyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public class SpyController {

    @Autowired
    private SpyService spyService;

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip == null || ip.isEmpty()) ? request.getRemoteAddr() : ip;
    }

    @GetMapping({"/sp", "/sp/"})
    String play(HttpServletRequest request, Model model) {
        String clientIp = getClientIp(request);
        System.out.println(clientIp);
        try {
            String location = spyService.getOrAssignMember(clientIp);
            if (location == null) {
                model.addAttribute("notSetup", true);
            } else {
                model.addAttribute("notSetup", false);
                model.addAttribute("location", location);
                model.addAttribute("dataPlay", spyService.getDataPlay());
                model.addAttribute("image", spyService.getImage());
            }
        } catch (Exception e) {
            model.addAttribute("notSetup", true);
        }
        return "spy/play";
    }

    @GetMapping({"/sp/{style}/{total}/{spy}", "/sp/{style}/{total}", "/sp/{style}/{total}/{spy}/{whiteHat}"})
    String setup(@PathVariable Integer total, @PathVariable Integer style,
                 @PathVariable(required = false) Integer spy,
                 @PathVariable(required = false) Optional<Integer> whiteHat,
                 Model model) {
        spyService.setupGame(total, style, spy, whiteHat.orElse(null));
        model.addAttribute("dataPlay", spyService.getDataPlay());
        model.addAttribute("total", total);
        model.addAttribute("spy", spy != null ? spy : 1);
        model.addAttribute("styleDesc", style == 1 ? "Spy có địa điểm khác biệt" : "Spy ẩn danh (không biết địa điểm)");
        model.addAttribute("image", spyService.getImage());
        return "spy/setup";
    }

    @GetMapping("/sp/delete/{id}")
    @ResponseBody
    String delete(@PathVariable Integer id) {
        return spyService.deleteItem(id)
                ? "<h1 style=\"font-size:500%;\">Đã xóa</h1>"
                : "<h1 style=\"font-size:500%;\">Chưa xóa</h1>";
    }

    @GetMapping("/sp/add/{name}")
    @ResponseBody
    String add(@PathVariable String name) {
        spyService.addItem(name);
        return "<h1 style=\"font-size:500%;\">Đã thêm</h1>";
    }
}
