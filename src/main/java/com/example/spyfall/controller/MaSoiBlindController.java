package com.example.spyfall.controller;

import com.example.spyfall.common.AutoSelectRequest;
import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.GameSetupRequest;
import com.example.spyfall.common.SettingDto;
import com.example.spyfall.service.MaSoiBlindService;
import com.example.spyfall.service.MaSoiChatService;
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

import java.util.Map;

/**
 * Ma Sói Mù (Blind Werewolf) - variant where everyone has the same night actions
 * and cannot see other players' night selections (private/blind)
 */
@Controller
@RequestMapping("/msBlind")
public class MaSoiBlindController {

    private final MaSoiService maSoiService;
    private final MaSoiBlindService maSoiBlindService;
    private final MaSoiChatService chatService;

    public MaSoiBlindController(MaSoiService maSoiService, MaSoiBlindService maSoiBlindService, MaSoiChatService chatService) {
        this.maSoiService = maSoiService;
        this.maSoiBlindService = maSoiBlindService;
        this.chatService = chatService;
    }

    @GetMapping("/play")
    String playLobby(Model model) {
        addCommonAttributes(model);
        return "masoiblind/lobby";
    }

    @GetMapping({"/play/", "/play/{name}"})
    String play(HttpServletRequest request, HttpServletResponse response, @PathVariable(required = false) String name, Model model) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        model.addAttribute("notSetup", false);
        model.addAttribute("fullSlot", false);
        model.addAttribute("isBlindMode", true);
        try {
            DataMember member = maSoiBlindService.getOrAssignRole(deviceId, name);
            if (member == null) {
                model.addAttribute("notSetup", true);
                addCommonAttributes(model);
                return "masoiblind/play";
            } else if (ObjectUtils.isEmpty(member.getRole())) {
                model.addAttribute("fullSlot", true);
                model.addAttribute("member", member);
                addCommonAttributes(model);
                return "masoiblind/play";
            }
            model.addAttribute("member", member);
            model.addAttribute("pls", maSoiBlindService.getMemers());
            model.addAttribute("autoState", maSoiBlindService.getState(deviceId));
            addCommonAttributes(model);
            model.addAttribute("gameNumber", maSoiService.getGameNumber());
            addRoleGroupAttributes(model);
            model.addAttribute("isDead", maSoiService.getDeadPls().contains(member));
            model.addAttribute("showRoles", maSoiService.getListShowForMember());
            if (maSoiService.getDeadPls().contains(member)) {
                model.addAttribute("historyGame", maSoiService.getCurrentGameHistory());
                model.addAttribute("deadPlayer", maSoiService.getDeadPls());
            }
            model.addAttribute("alivePlayer", maSoiService.getPls());

            if (member.getId() == 42 && member.getOldTargetId() != null) {
                maSoiBlindService.findPlayer(member.getOldTargetId()).ifPresent(target -> model.addAttribute("oldTargetName", target.getNameMember()));
            }
        } catch (Exception e) {
            model.addAttribute("notSetup", true);
            addCommonAttributes(model);
            return "masoiblind/play";
        }
        return "masoiblind/play";
    }

    @GetMapping("/state")
    @ResponseBody
    Map<String, Object> state(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return maSoiBlindService.getState(deviceId);
    }

    @PostMapping("/timers")
    @ResponseBody
    Map<String, Object> updateTimers(@RequestParam int daySeconds, @RequestParam int nightSeconds,
                                     @RequestParam int voteSeconds, @RequestParam int selectionSeconds,
                                     @RequestParam int silentSeconds,
                                     HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return maSoiBlindService.updateDurations(daySeconds, nightSeconds, voteSeconds, selectionSeconds, silentSeconds, deviceId);
    }

    @PostMapping("/day/select")
    @ResponseBody
    Map<String, Object> selectDay(@RequestBody AutoSelectRequest request) {
        return maSoiBlindService.selectDayTarget(request);
    }

    @PostMapping("/day/skip")
    @ResponseBody
    Map<String, Object> skipDay(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return maSoiBlindService.skipDaySelection(deviceId);
    }

    @PostMapping("/night/select")
    @ResponseBody
    Map<String, Object> selectNight(@RequestBody AutoSelectRequest request) {
        return maSoiBlindService.selectNightTarget(request);
    }

    @PostMapping("/submitDay")
    @ResponseBody
    Map<String, Object> submitDay() {
        return maSoiBlindService.submitDayNow();
    }

    @PostMapping("/submitNight")
    @ResponseBody
    Map<String, Object> submitNight() {
        return maSoiBlindService.submitNightNow();
    }

    @PostMapping("/board/day")
    @ResponseBody
    Map<String, Object> toggleDayBoard(@RequestParam boolean show) {
        return maSoiBlindService.setShowDayBoard(show);
    }

    @PostMapping("/board/night")
    @ResponseBody
    Map<String, Object> toggleNightBoard(@RequestParam boolean show) {
        return maSoiBlindService.setShowNightBoard(show);
    }

    @PostMapping("/board/vote")
    @ResponseBody
    void toggleVoteBoard(@RequestParam boolean show) {
        maSoiBlindService.setNeedVote(show);
    }

    @PostMapping("/vote/select")
    @ResponseBody
    Map<String, Object> selectVote(@RequestParam String answer, HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return maSoiBlindService.submitVoteSelection(deviceId, answer);
    }

    @GetMapping("/run")
    String run(Model model) throws Exception {
        model.addAttribute("datas", maSoiService.getDatasBlind());
        addRoleGroupAttributes(model);
        return "masoiblind/run";
    }

    @GetMapping("/create")
    @ResponseBody
    String create(@RequestParam Map<String, String> params) throws Exception {
        String result = maSoiService.loadGame(GameSetupRequest.fromQueryParams(params));
        if (result.startsWith("OK")) {
            chatService.initChatGroupsBlindMode();
        }
        return result;
    }

    @GetMapping("/admin")
    String admin(Model model) {
        if (maSoiService.isGameEnded()) {
            return "redirect:/msBlind/run";
        }
        model.addAttribute("dayDurationSeconds", maSoiBlindService.getDayDurationSeconds());
        model.addAttribute("nightDurationSeconds", maSoiBlindService.getNightDurationSeconds());
        model.addAttribute("voteDurationSeconds", maSoiBlindService.getVoteDurationSeconds());
        model.addAttribute("showDayBoard", maSoiBlindService.isShowDayBoard());
        model.addAttribute("showNightBoard", maSoiBlindService.isShowNightBoard());
        model.addAttribute("isNeedVote", maSoiBlindService.isNeedVote());
        addCommonAttributes(model);
        return "masoiblind/admin";
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("image", maSoiBlindService.getImage());
    }

    private void addRoleGroupAttributes(Model model) {
        model.addAttribute("idSoi", MaSoiService.WOLF_ROLE_IDS);
        model.addAttribute("idOutsider", MaSoiService.OUTSIDER_ROLE_IDS);
    }
}
