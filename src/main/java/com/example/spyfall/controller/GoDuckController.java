package com.example.spyfall.controller;

import com.example.spyfall.common.GoDuck;
import com.example.spyfall.service.GoDuckService;
import com.example.spyfall.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/gd")
public class GoDuckController {

    @Autowired
    private GoDuckService goDuckService;

    @GetMapping("/play")
    String lobby(Model model) {
        model.addAttribute("image", goDuckService.getImage());
        return "goduck/lobby";
    }

    @GetMapping("/play/{name}")
    String play(HttpServletRequest request, HttpServletResponse response, @PathVariable String name, Model model) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        try {
            String data = goDuckService.getOrRegisterMember(deviceId, name);
            model.addAttribute("secretInfo", data);
            model.addAttribute("totalPlay", goDuckService.getTotalPlay());
            model.addAttribute("spyTotalPlay", goDuckService.getSpyTotalPlay());
            model.addAttribute("members", goDuckService.getSortedMembers());
            model.addAttribute("image", goDuckService.getImage());
        } catch (Exception e) {
            model.addAttribute("secretInfo", "");
            model.addAttribute("totalPlay", 0);
            model.addAttribute("spyTotalPlay", 0);
            model.addAttribute("members", java.util.List.of());
            model.addAttribute("image", goDuckService.getImage());
        }
        return "goduck/play";
    }

    @GetMapping("/setup/{total}/{spy}")
    String setup(@PathVariable Integer total, @PathVariable Integer spy, Model model) {
        goDuckService.setupGame(total, spy != null ? spy : 1);
        model.addAttribute("totalPlayers", total);
        model.addAttribute("setup", goDuckService.getCountGame());
        model.addAttribute("numSpies", goDuckService.getSpyTotalPlay());
        model.addAttribute("image", goDuckService.getImage());
        return "goduck/setup";
    }

    @GetMapping("/setup")
    String setup(Model model) {
        model.addAttribute("setup", goDuckService.getCountGame());
        model.addAttribute("image", goDuckService.getImage());
        return "goduck/setup";
    }

    @GetMapping("clear")
    @ResponseBody
    String clear(Model model) {
        goDuckService.clear();
        return "Xóa dữ liệu thành công";
    }
}
