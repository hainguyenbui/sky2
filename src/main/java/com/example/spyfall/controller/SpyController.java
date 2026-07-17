package com.example.spyfall.controller;

import com.example.spyfall.common.Spy2;
import com.example.spyfall.service.SpyService;
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
@RequestMapping("/sp")
public class SpyController {

    @Autowired
    private SpyService spyService;

    @GetMapping
    String lobby(Model model) {
        model.addAttribute("image", spyService.getImage());
        return "spy/lobby";
    }

    @GetMapping("/play/{name}")
    String play(HttpServletRequest request, HttpServletResponse response, @PathVariable String name, Model model) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        try {
            String keyword = spyService.getOrAssignMember(deviceId, name);
            if (keyword == null) {
                model.addAttribute("notSetup", true);
            } else if (keyword.isEmpty()) {
                model.addAttribute("notSetup", false);
                model.addAttribute("fullSlot", true);
                model.addAttribute("image", spyService.getImage());
                model.addAttribute("dataPlay", spyService.getKeywords());
            } else {
                model.addAttribute("notSetup", false);
                model.addAttribute("keyword", keyword);
                model.addAttribute("removed", spyService.getRemoved());
                model.addAttribute("image", spyService.getImage());
                model.addAttribute("dataPlay", spyService.getKeywords());
            }
        } catch (Exception e) {
            model.addAttribute("notSetup", true);
        }
        return "spy/play";
    }

    @GetMapping("/setup/{totalPlayers}/{numSpies}/{numWhite}")
    String setup(@PathVariable int totalPlayers, @PathVariable int numSpies,
                 @PathVariable int numWhite, Model model) {
        spyService.setupGame(totalPlayers, numSpies, numWhite);
        model.addAttribute("totalPlayers", totalPlayers);
        model.addAttribute("numSpies", numSpies);
        model.addAttribute("numWhite", numWhite);
        model.addAttribute("setup", true);
        model.addAttribute("number", spyService.getNumberGamePlay());
        model.addAttribute("image", spyService.getImage());
        model.addAttribute("player", spyService.getDataPlayer());
        return "spy/setup";
    }

    @GetMapping({"/setup"})
    String setupIndex(Model model) {
        if (spyService.getNumberGamePlay() > 0) {
            model.addAttribute("setup", true);
            model.addAttribute("number", spyService.getNumberGamePlay());
            model.addAttribute("player", spyService.getDataPlayer());
        }
        model.addAttribute("image", spyService.getImage());
        return "spy/setup";
    }

    @GetMapping("/remove/{id}")
    @ResponseBody
    String removePlayer(@PathVariable int id) {
        Spy2 removedPlayer = spyService.removePlayer(id);
        return removedPlayer.getRole();
    }

    @GetMapping("/removeList")
    String listRemove(Model model) {
        model.addAttribute("players", spyService.getDataPlayer());
        model.addAttribute("removed", spyService.getRemoved());
        return "spy/list";
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
