package com.example.spyfall.controller;

import com.example.spyfall.common.AutoSelectRequest;
import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.GameSetupRequest;
import com.example.spyfall.service.MaSoiAutoV1;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/msAutoV1")
public class MaSoiAutoV1Controller {

    private final MaSoiService maSoiService;
    private final MaSoiAutoV1 maSoiAutoV1;
    // Chat: inject chatService để khởi tạo nhóm chat khi game mới bắt đầu
    private final MaSoiChatService chatService;

    public MaSoiAutoV1Controller(MaSoiService maSoiService, MaSoiAutoV1 maSoiAutoV1, MaSoiChatService chatService) {
        this.maSoiService = maSoiService;
        this.maSoiAutoV1 = maSoiAutoV1;
        this.chatService = chatService;
    }

    @GetMapping("/play")
    String playLobby(Model model) {
        addCommonAttributes(model);
        return "masoiAutoV1/lobby";
    }

    @GetMapping({"/play/", "/play/{name}"})
    String play(HttpServletRequest request, HttpServletResponse response, @PathVariable(required = false) String name, Model model) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        model.addAttribute("notSetup", false);
        model.addAttribute("fullSlot", false);
        try {
            DataMember member = maSoiService.getOrAssignRole(deviceId, name);
            if (member == null) {
                model.addAttribute("notSetup", true);
                addCommonAttributes(model);
                return "masoiAutoV1/play";
            } else if (ObjectUtils.isEmpty(member.getRole())) {
                model.addAttribute("fullSlot", true);
                model.addAttribute("member", member);
                addCommonAttributes(model);
                return "masoiAutoV1/play";
            }
            // Truyền thẳng member để giao diện đọc toàn bộ trạng thái người chơi từ 1 nguồn.
            model.addAttribute("member", member);
            model.addAttribute("pls", maSoiAutoV1.getMemers());
            model.addAttribute("autoState", maSoiAutoV1.getState(deviceId));
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

            // voi role 42 thi truyen ten nguoi da chon qua
            if (member.getId() == 42 && member.getOldTargetId() != null) {
                maSoiAutoV1.findPlayer(member.getOldTargetId()).ifPresent(target -> model.addAttribute("oldTargetName", target.getNameMember()));
            }
        } catch (Exception e) {
            model.addAttribute("notSetup", true);
            addCommonAttributes(model);
            return "masoiAutoV1/play";
        }
        return "masoiAutoV1/play";
    }

    @GetMapping("/state")
    @ResponseBody
    Map<String, Object> state(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return maSoiAutoV1.getState(deviceId);
    }

    @PostMapping("/timers")
    @ResponseBody
    Map<String, Object> updateTimers(@RequestParam int daySeconds, @RequestParam int nightSeconds,
                                     @RequestParam int voteSeconds, @RequestParam int selectionSeconds,
                                     @RequestParam int silentSeconds,
                                     HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return maSoiAutoV1.updateDurations(daySeconds, nightSeconds, voteSeconds, selectionSeconds, silentSeconds, deviceId);
    }

    @PostMapping("/day/select")
    @ResponseBody
    Map<String, Object> selectDay(@RequestBody AutoSelectRequest request) {
        return maSoiAutoV1.selectDayTarget(request);
    }

    // Người chơi chọn bỏ qua vote ngày (không chọn ai)
    @PostMapping("/day/skip")
    @ResponseBody
    Map<String, Object> skipDay(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return maSoiAutoV1.skipDaySelection(deviceId);
    }

    @PostMapping("/night/select")
    @ResponseBody
    Map<String, Object> selectNight(@RequestBody AutoSelectRequest request) {
        return maSoiAutoV1.selectNightTarget(request);
    }

    @PostMapping("/submitDay")
    @ResponseBody
    Map<String, Object> submitDay() {
        return maSoiAutoV1.submitDayNow();
    }

    @PostMapping("/submitNight")
    @ResponseBody
    Map<String, Object> submitNight() {
        return maSoiAutoV1.submitNightNow();
    }

    // Admin bật/tắt bảng ngày – broadcast qua WebSocket tới tất cả người chơi
    @PostMapping("/board/day")
    @ResponseBody
    Map<String, Object> toggleDayBoard(@RequestParam boolean show) {
        return maSoiAutoV1.setShowDayBoard(show);
    }

    // Admin bật/tắt bảng đêm – broadcast qua WebSocket tới tất cả người chơi
    @PostMapping("/board/night")
    @ResponseBody
    Map<String, Object> toggleNightBoard(@RequestParam boolean show) {
        return maSoiAutoV1.setShowNightBoard(show);
    }

    // Admin bật/tắt bảng vote Yes/No – broadcast qua WebSocket tới tất cả người chơi
    @PostMapping("/board/vote")
    @ResponseBody
    Map<String, Object> toggleVoteBoard(@RequestParam boolean show) {
        maSoiAutoV1.setNeedVote(show);//TODO clear return
        return null;
//                maSoiAutoV1.setShowVoteBoard(show);
    }

    // Người chơi gửi vote Yes/No
    @PostMapping("/vote/select")
    @ResponseBody
    Map<String, Object> selectVote(@RequestParam String answer, HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return maSoiAutoV1.submitVoteSelection(deviceId, answer);
    }

    @GetMapping("/run")
    String run(Model model) throws Exception {
        model.addAttribute("datas", maSoiService.getDatas());
        addRoleGroupAttributes(model);
        return "masoiAutoV1/run";
    }

    @GetMapping("/create")
    @ResponseBody
    String create(@RequestParam Map<String, String> params) throws Exception {
        String result = maSoiService.loadGame(GameSetupRequest.fromQueryParams(params));
        // Chat: khởi tạo các nhóm chat mới khi game được tạo thành công
        if (result.startsWith("OK")) {
            chatService.initChatGroups();
        }
        return result;
    }

    @GetMapping("/admin")
    String admin(Model model) {
        if (maSoiService.isGameEnded()) {
            return "redirect:/msAutoV1/run";
        }
        model.addAttribute("dayDurationSeconds", maSoiAutoV1.getDayDurationSeconds());
        model.addAttribute("nightDurationSeconds", maSoiAutoV1.getNightDurationSeconds());
        model.addAttribute("voteDurationSeconds", maSoiAutoV1.getVoteDurationSeconds());
        model.addAttribute("showDayBoard", maSoiAutoV1.isShowDayBoard());
        model.addAttribute("showNightBoard", maSoiAutoV1.isShowNightBoard());
        model.addAttribute("showVoteBoard", maSoiAutoV1.isShowVoteBoard());
        model.addAttribute("selectionSeconds", maSoiAutoV1.getSelectionSeconds());
        model.addAttribute("silentSeconds", maSoiAutoV1.getSilentSeconds());
        addCommonAttributes(model);

        return "masoiAutoV1/admin";
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("image", maSoiService.getImage());
    }

    private void addRoleGroupAttributes(Model model) {
        model.addAttribute("idSoi", MaSoiService.WOLF_ROLE_IDS);
        model.addAttribute("idOutsider", MaSoiService.OUTSIDER_ROLE_IDS);
    }
}
