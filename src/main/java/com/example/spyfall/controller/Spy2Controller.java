package com.example.spyfall.controller;

import com.example.spyfall.common.Spy2;
import com.example.spyfall.service.Spy2Service;
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
@RequestMapping("/spy2")
public class Spy2Controller {

    @Autowired
    private Spy2Service spy2Service;

    @GetMapping
    String lobby(Model model) {
        model.addAttribute("image", spy2Service.getImage());
        return "spy2/lobby";
    }

    @GetMapping("/play/{name}")
    String play(HttpServletRequest request, HttpServletResponse response, @PathVariable String name, Model model) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        try {
            String keyword = spy2Service.getOrAssignKeyword(deviceId, name);
            if (keyword == null) {
                model.addAttribute("notSetup", true);
            } else if (keyword.isEmpty()) {
                model.addAttribute("notSetup", false);
                model.addAttribute("fullSlot", true);
                model.addAttribute("image", spy2Service.getImage());
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
        model.addAttribute("setup", true);
        model.addAttribute("number", spy2Service.getNumberGamePlay());
        model.addAttribute("image", spy2Service.getImage());
        model.addAttribute("player", spy2Service.getDataPlayer());
        return "spy2/setup";
    }

    @GetMapping({"/setup"})
    String setupIndex(Model model) {
        if (spy2Service.getNumberGamePlay() > 0) {
            model.addAttribute("setup", true);
            model.addAttribute("number", spy2Service.getNumberGamePlay());
            model.addAttribute("player", spy2Service.getDataPlayer());
        }
        model.addAttribute("image", spy2Service.getImage());
        return "spy2/setup";
    }

    @GetMapping("/remove/{id}")
    @ResponseBody
    String removePlayer(@PathVariable int id) {
        Spy2 removedPlayer = spy2Service.removePlayer(id);
        return removedPlayer.getRole();
    }

    @GetMapping("/removeList")
    String listRemove(Model model) {
        model.addAttribute("players", spy2Service.getDataPlayer());
        model.addAttribute("removed", spy2Service.getRemoved());
        return "spy2/list";
    }
}
