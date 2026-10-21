package com.example.spyfall.controller;

import com.example.spyfall.service.SpyCommService;
import com.example.spyfall.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.util.Map;

@Controller
@RequestMapping("/comm")
public class SpyCommController {

    private final SpyCommService spyCommService;

    public SpyCommController(SpyCommService spyCommService) {
        this.spyCommService = spyCommService;
    }

    @GetMapping("/{game}/state")
    @ResponseBody
    public Map<String, Object> state(@PathVariable String game,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return spyCommService.getPlayerState(game, deviceId);
    }

    @GetMapping("/{game}/admin/state")
    @ResponseBody
    public Map<String, Object> adminState(@PathVariable String game) {
        return spyCommService.getAdminState(game);
    }

    @PostMapping("/{game}/admin/restriction")
    @ResponseBody
    public Map<String, Object> updateRestriction(@PathVariable String game,
                                                 @RequestParam boolean enabled) {
        return spyCommService.setRestrictEliminated(game, enabled);
    }

    @MessageMapping("/comm.register")
    public void register(Map<String, Object> body, SimpMessageHeaderAccessor accessor) {
        String game = asString(body.get("game"));
        String deviceId = asString(body.get("deviceId"));
        String playerName = asString(body.get("playerName"));
        String sessionId = accessor.getSessionId();
        spyCommService.registerSocket(game, deviceId, sessionId, playerName);
    }

    @MessageMapping("/comm.chat")
    public void sendChat(Map<String, Object> body) {
        spyCommService.sendChat(
                asString(body.get("game")),
                asString(body.get("deviceId")),
                asString(body.get("channel")),
                asString(body.get("message"))
        );
    }

    @MessageMapping("/comm.signal")
    public void sendSignal(Map<String, Object> body) {
        spyCommService.relaySignal(
                asString(body.get("game")),
                asString(body.get("deviceId")),
                asString(body.get("targetDeviceId")),
                asString(body.get("signalType")),
                body.get("payload")
        );
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        spyCommService.unregisterSocket(event.getSessionId());
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
