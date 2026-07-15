package com.example.spyfall.controller;

import com.example.spyfall.service.Spy2Service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/spy2")
public class Spy2Controller {

    @Autowired
    private Spy2Service spy2Service;

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip == null || ip.isEmpty()) ? request.getRemoteAddr() : ip;
    }

    @GetMapping
    String lobby(Model model) {
        model.addAttribute("image", spy2Service.getImage());
        return "spy2/lobby";
    }

    @GetMapping("/play/{name}")
    String play(HttpServletRequest request, @PathVariable String name, Model model) {
        String clientIp = getClientIp(request);
        System.out.println(clientIp);
        try {
            String keyword = spy2Service.getOrAssignKeyword(clientIp, name);
            if (keyword == null) {
                model.addAttribute("notSetup", true);
            } else {
                model.addAttribute("notSetup", false);
                model.addAttribute("keyword", keyword);
                model.addAttribute("countnumSpies", spy2Service.getCountnumSpies());
                model.addAttribute("countWhite", spy2Service.getCountWhite());
                model.addAttribute("playerCount", spy2Service.getActivePlayerCount());
                model.addAttribute("removed", spy2Service.getRemoved());
                model.addAttribute("image", spy2Service.getImage());
            }
        } catch (Exception e) {
            model.addAttribute("notSetup", true);
        }
        return "spy2/play";
    }

    @GetMapping("/setup/{totalPlayers}/{numSpies}/{numWhite}")
    String setup(@PathVariable int totalPlayers, @PathVariable int numSpies,
                 @PathVariable int numWhite, Model model) {
        spy2Service.setupGame(totalPlayers, numSpies, numWhite);
        model.addAttribute("totalPlayers", totalPlayers);
        model.addAttribute("numSpies", numSpies);
        model.addAttribute("numWhite", numWhite);
        model.addAttribute("image", spy2Service.getImage());
        return "spy2/setup";
    }

    @GetMapping("/remove/{id}")
    @ResponseBody
    String removePlayer(@PathVariable int id) {
        spy2Service.removePlayer(id);
        return "Success";
    }

    @GetMapping("/removeList")
    String listRemove(Model model) {
        model.addAttribute("players", spy2Service.getDataPlayer());
        model.addAttribute("removed", spy2Service.getRemoved());
        return "spy2/list";
    }
}
