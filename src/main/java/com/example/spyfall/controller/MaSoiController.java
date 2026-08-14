package com.example.spyfall.controller;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.DayActionDto;
import com.example.spyfall.common.GameSessionState;
import com.example.spyfall.common.KillDto;
import com.example.spyfall.common.NightActionDto;
import com.example.spyfall.service.MaSoiService;
import com.example.spyfall.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ms")
public class MaSoiController {

    @Autowired
    private MaSoiService maSoiService;

    @GetMapping("/run")
    String run(Model model) throws Exception {
        model.addAttribute("datas", maSoiService.getDatas());
        model.addAttribute("idSoi", maSoiService.ID_SOI);
        model.addAttribute("idOutsider", maSoiService.ID_OUTSIDER);
        return "masoi/run";
    }

    @GetMapping("/create")
    @ResponseBody
    String create(@RequestParam Map<String, String> params) throws Exception {
        return maSoiService.loadGame(params);
    }

    @GetMapping("/play")
    String playLobby(Model model) {
        model.addAttribute("image", maSoiService.getImage());
        return "masoi/lobby";
    }

    @GetMapping({"/play/", "/play/{name}"})
    String play(HttpServletRequest request, HttpServletResponse response, @PathVariable(required = false) String name, Model model) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        try {
            synchronized (maSoiService) {
                DataMember member = maSoiService.getOrAssignRole(deviceId, name);
                model.addAttribute("gameSessionId", maSoiService.getCurrentGameSessionId());
                if (member == null) {
                    if (!ObjectUtils.isEmpty(name)) {
                        model.addAttribute("notSetup", true);
                        model.addAttribute("sessionPageMode", "notSetup");
                        model.addAttribute("image", maSoiService.getImage());
                        return "masoi/play";
                    }
                    // Not setup - redirect to lobby
                    model.addAttribute("image", maSoiService.getImage());
                    return "masoi/lobby";
                }
                if (ObjectUtils.isEmpty(member.getRole())) {
                    model.addAttribute("fullSlot", true);
                    model.addAttribute("sessionPageMode", "fullSlot");
                    model.addAttribute("image", maSoiService.getImage());
                    return "masoi/play";
                }

                boolean isDead = maSoiService.getDeadPls().contains(member);
                model.addAttribute("sessionPageMode", "assigned");
                model.addAttribute("playerNames", maSoiService.getAllPlayerNames());
                model.addAttribute("role", member.getRole());
                model.addAttribute("desc", member.getDescription());
                model.addAttribute("dataMore", member.getDetailShow());
                model.addAttribute("showRoles", List.copyOf(maSoiService.getListShowForMember()));
                model.addAttribute("image", maSoiService.getImage());
                model.addAttribute("gameNumber", maSoiService.getGameNumber());
                model.addAttribute("idSoi", maSoiService.ID_SOI);
                model.addAttribute("idOutsider", maSoiService.ID_OUTSIDER);
                model.addAttribute("isDead", isDead);
                if (isDead) {
                    if (maSoiService.isAllowDeadViewGameHistory()) {
                        model.addAttribute("historyGame", maSoiService.getPlayerCurrentHistory());
                    }
                    if (maSoiService.isAllowShowAliveDead()) {
                        model.addAttribute("deadPlayer", List.copyOf(maSoiService.getDeadPls()));
                        model.addAttribute("alivePlayer", List.copyOf(maSoiService.getPls()));
                    }
                }
            }
        } catch (Exception e) {
            model.addAttribute("image", maSoiService.getImage());
            return "masoi/lobby";
        }
        return "masoi/play";
    }

    @GetMapping("/admin")
    String admin(Model model) {
        synchronized (maSoiService) {
            model.addAttribute("activeRoles", maSoiService.getActiveRolesString());
            model.addAttribute("image", maSoiService.getImage());
            model.addAttribute("idSoi", maSoiService.ID_SOI);
            model.addAttribute("idOutsider", maSoiService.ID_OUTSIDER);
            model.addAttribute("deadViewHistory", maSoiService.isAllowDeadViewGameHistory());
            model.addAttribute("showAliveDead", maSoiService.isAllowShowAliveDead());
            model.addAllAttributes(maSoiService.getGameManagementData());
            model.addAttribute("dayKill", maSoiService.dayIsReadyKill);
            model.addAttribute("playerNames", maSoiService.getAllPlayerNames());
        }
        return "masoi/admin";
    }

    @PostMapping("/showHistory")
    String showHistory(Model model) {
        model.addAllAttributes(maSoiService.getGameHistoryData());
        return "masoi/history";
    }

    @PostMapping("/end")
    @ResponseBody
    String endGame() {
        return maSoiService.endGame();
    }

    @PostMapping("/discard")
    @ResponseBody
    String discardSetup() {
        return maSoiService.discardSetup();
    }

    @GetMapping("/session-state")
    @ResponseBody
    GameSessionState sessionState(@RequestParam(defaultValue = "0") long sessionId) {
        return maSoiService.getGameSessionState(sessionId);
    }

    @PostMapping("/processNight")
    @ResponseBody
    String processNight(@RequestBody List<NightActionDto> actions) {
        return maSoiService.processNight(actions);
    }

    @PostMapping("/kill")
    @ResponseBody
    String kill(@RequestBody KillDto killDto) {
        return maSoiService.processDay(killDto.getDeviceIds());
    }

    @PostMapping("/processDayRole")
    @ResponseBody
    String processDayRole(@RequestBody DayActionDto action) {
        return maSoiService.processDayRole(action);
    }

    @PostMapping("/toggleAdminOptions")
    @ResponseBody
    String toggleAdminOptions(@RequestParam String option, @RequestParam boolean value) {
        switch(option) {
            case "deadViewGameHistory":
                maSoiService.setAllowDeadViewGameHistory(value);
                break;
            case "showAliveDead":
                maSoiService.setAllowShowAliveDead(value);
                break;
        }
        return "OK";
    }
    @GetMapping("/test")
    @ResponseBody
    String forTest(@RequestParam int total) {
        maSoiService.createTest(total);
        return "Success tạo người chơi : " + total;
    }
}
