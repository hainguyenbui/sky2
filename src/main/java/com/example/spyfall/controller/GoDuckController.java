package com.example.spyfall.controller;

import com.example.spyfall.service.GoDuckService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gd")
public class GoDuckController {

    @Autowired
    private GoDuckService goDuckService;

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip == null || ip.isEmpty()) ? request.getRemoteAddr() : ip;
    }

    @GetMapping("/play")
    String lobby(Model model) {
        model.addAttribute("image", goDuckService.getImage());
        return "goduck/lobby";
    }

    @GetMapping("/play/{name}")
    String play(HttpServletRequest request, @PathVariable String name, Model model) {
        String clientIp = getClientIp(request);
        try {
            goDuckService.getOrRegisterMember(clientIp, name);
            String secretInfo = goDuckService.buildSecretInfo(clientIp);
            model.addAttribute("secretInfo", secretInfo);
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

    @GetMapping("/run/{total}/{spy}")
    String setup(@PathVariable Integer total, @PathVariable Integer spy, Model model) {
        goDuckService.setupGame(total, spy != null ? spy : 1);
        model.addAttribute("total", total);
        model.addAttribute("spy", goDuckService.getSpyTotalPlay());
        model.addAttribute("image", goDuckService.getImage());
        return "goduck/setup";
    }
}
