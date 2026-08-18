package com.example.spyfall.controller;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.GameSetupRequest;
import com.example.spyfall.common.NightActionDto;
import com.example.spyfall.common.KillDto;
import com.example.spyfall.service.MaSoiService;
import com.example.spyfall.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ms")
public class MaSoiController {

    private final MaSoiService maSoiService;

    public MaSoiController(MaSoiService maSoiService) {
        this.maSoiService = maSoiService;
    }

    @GetMapping("/run")
    String run(Model model) throws Exception {
        model.addAttribute("datas", maSoiService.getDatas());
        addRoleGroupAttributes(model);
        return "masoi/run";
    }

    @GetMapping("/create")
    @ResponseBody
    String create(@RequestParam Map<String, String> params) throws Exception {
        return maSoiService.loadGame(GameSetupRequest.fromQueryParams(params));
    }

    @GetMapping("/play")
    String playLobby(Model model) {
        addCommonAttributes(model);
        return "masoi/lobby";
    }

    @GetMapping({"/play/", "/play/{name}"})
    String play(HttpServletRequest request, HttpServletResponse response, @PathVariable(required = false) String name, Model model) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        try {
            DataMember member = maSoiService.getOrAssignRole(deviceId, name);
            if (member == null) {
                if (!ObjectUtils.isEmpty(name)) {
                    model.addAttribute("notSetup", true);
                    addCommonAttributes(model);
                    return "masoi/play";
                }
                // Not setup - redirect to lobby
                addCommonAttributes(model);
                return "masoi/lobby";
            } else if (ObjectUtils.isEmpty(member.getRole())) {
                model.addAttribute("fullSlot", true);
                addCommonAttributes(model);
                return "masoi/play";
            }
            model.addAttribute("role", member.getRole());
            model.addAttribute("desc", member.getDescription());
            model.addAttribute("dataMore", member.getRoleShow());
            model.addAttribute("showRoles", maSoiService.getListShowForMember());
            addCommonAttributes(model);
            model.addAttribute("gameNumber", maSoiService.getGameNumber());
            addRoleGroupAttributes(model);
            model.addAttribute("isDead", maSoiService.getDeadPls().contains(member));
            if (maSoiService.getDeadPls().contains(member)) {
                if (maSoiService.isAllowDeadViewGameHistory()) {
                    model.addAttribute("historyGame", maSoiService.getCurrentGameHistory());
                }
                if (maSoiService.isAllowShowAliveDead()) {
                    model.addAttribute("deadPlayer", maSoiService.getDeadPls());
                    model.addAttribute("alivePlayer", maSoiService.getPls());
                }
            }

        } catch (Exception e) {
            addCommonAttributes(model);
            return "masoi/lobby";
        }
        return "masoi/play";
    }

    @GetMapping("/admin")
    String admin(Model model) {
        if (maSoiService.isGameEnded()) {
            return "redirect:/ms/run";
        }
        List<DataMember> players = maSoiService.getPls();
        players.sort(Comparator.comparing(DataMember::getId));
        model.addAttribute("playersData", players);
        model.addAttribute("activeRoles", maSoiService.getActiveRolesString());
        model.addAttribute("showSoiNguyen", maSoiService.isShowSoiNguyen());
        addCommonAttributes(model);
        addRoleGroupAttributes(model);
        model.addAttribute("deadViewHistory", maSoiService.isAllowDeadViewGameHistory());
        model.addAttribute("showAliveDead", maSoiService.isAllowShowAliveDead());
        model.addAttribute("invertedSeer", maSoiService.isInvertedSeer());

        model.addAllAttributes(maSoiService.getGameManagementData());
        model.addAttribute("dayKill", maSoiService.isDayKillProcessed());
        model.addAttribute("detailNight", maSoiService.getHistoryAdmin());
        model.addAttribute("detailDay", maSoiService.getDayDetails());
        model.addAttribute("gameDetailNight", maSoiService.getCurrentGameHistory());
        return "masoi/admin";
    }
//
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

    @PostMapping("/reorderPlayers")
    @ResponseBody
    String reorderPlayers(@RequestBody List<String> orderedDeviceIds) {
        maSoiService.updateDeviceOrder(orderedDeviceIds);
        return "OK";
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
            case "invertedSeer":
                maSoiService.setInvertedSeer(value);
                break;
        }
        return "OK";
    }
//
    @GetMapping("/gameManagement")
    String gameManagement(Model model) {
        model.addAllAttributes(maSoiService.getGameManagementData());
        addCommonAttributes(model);
        model.addAttribute("dayKill", maSoiService.isDayKillProcessed());
        return "masoi/gameManagement";
    }

    @GetMapping("/test")
    @ResponseBody
    String forTest(@RequestParam int total) {
        maSoiService.createTest(total);
        return "Success tạo người chơi : " + total;
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("image", maSoiService.getImage());
    }

    private void addRoleGroupAttributes(Model model) {
        model.addAttribute("idSoi", MaSoiService.WOLF_ROLE_IDS);
        model.addAttribute("idOutsider", MaSoiService.OUTSIDER_ROLE_IDS);
    }
}
