package com.example.spyfall.controller;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.NightActionDto;
import com.example.spyfall.common.KillDto;
import com.example.spyfall.common.SoiNguyenDto;
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

import java.util.Comparator;
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
            DataMember member = maSoiService.getOrAssignRole(deviceId, name);
            if (member == null) {
                if (!ObjectUtils.isEmpty(name)) {
                    model.addAttribute("notSetup", true);
                    model.addAttribute("image", maSoiService.getImage());
                    return "masoi/play";
                }
                // Not setup - redirect to lobby
                model.addAttribute("image", maSoiService.getImage());
                return "masoi/lobby";
            } else if (ObjectUtils.isEmpty(member.getRole())) {
                model.addAttribute("fullSlot", true);
                model.addAttribute("image", maSoiService.getImage());
                return "masoi/play";
            }
            model.addAttribute("role", member.getRole());
            model.addAttribute("desc", member.getDescription());
            model.addAttribute("showRoles", maSoiService.getListShowForMember());
            model.addAttribute("image", maSoiService.getImage());
            model.addAttribute("gameNumber", maSoiService.getGameNumber());
            model.addAttribute("idSoi", maSoiService.ID_SOI);
            model.addAttribute("idOutsider", maSoiService.ID_OUTSIDER);
            model.addAttribute("isDead", maSoiService.getDeadPls().contains(member));
            if (maSoiService.getDeadPls().contains(member)) {
                if (maSoiService.isAllowDeadViewGameHistory()) {
                    model.addAttribute("historyGame", maSoiService.getDetailOneGameHistory());
                }
                if (maSoiService.isAllowShowAliveDead()) {
                    model.addAttribute("deadPlayer", maSoiService.getDeadPls());
                    model.addAttribute("alivePlayer", maSoiService.getPls());
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
        List<DataMember> players = maSoiService.getPls();
        players.sort(Comparator.comparing(DataMember::getId));
        model.addAttribute("players", players);
        model.addAttribute("activeRoles", maSoiService.getActiveRolesString());
        model.addAttribute("showSoiNguyen", maSoiService.isShowSoiNguyen());
        model.addAttribute("image", maSoiService.getImage());
        model.addAttribute("idSoi", maSoiService.ID_SOI);
        model.addAttribute("idOutsider", maSoiService.ID_OUTSIDER);
        return "masoi/admin";
    }

    @GetMapping("/admin2")
    String admin2(Model model) throws Exception {
        model.addAttribute("datas", maSoiService.getDatas());
        model.addAttribute("idSoi", maSoiService.ID_SOI);
        model.addAttribute("idOutsider", maSoiService.ID_OUTSIDER);
        return "masoi/admin2";
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

    @PostMapping("/soiNguyen")
    @ResponseBody
    String soiNguyen(@RequestBody SoiNguyenDto soiNguyen) {
        return maSoiService.soiNguyen(soiNguyen);
    }

    @PostMapping("/processNight")
    @ResponseBody
    String processNight(@RequestBody List<NightActionDto> actions) {
        return maSoiService.processNight(actions);
    }

    @PostMapping("/kill")
    @ResponseBody
    String kill(@RequestBody KillDto killDto) {
        return null;
//        return maSoiService.kill(killDto.getDeviceIds(), killDto.getReason());
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
//
//    @PostMapping("/assignPower")
//    @ResponseBody
//    String assignPower(@RequestBody PowerAssignDto dto) {
//        return maSoiService.assignPower(dto.getDeviceId(), dto.getRoleId(), dto.getRoleName(), dto.getSkillType(), dto.isEnabled());
//    }

    @GetMapping("/gameManagement")
    String gameManagement(Model model) {
        model.addAllAttributes(maSoiService.getGameManagementData());
        model.addAttribute("image", maSoiService.getImage());
        return "masoi/gameManagement";
    }
}
