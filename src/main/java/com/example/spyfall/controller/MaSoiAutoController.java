package com.example.spyfall.controller;

import com.example.spyfall.common.DeviceOrderRequest;
import com.example.spyfall.common.SettingDto;
import com.example.spyfall.service.MaSoiAutoService;
import com.example.spyfall.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/msAuto")
public class MaSoiAutoController {
    private final MaSoiAutoService maSoiAutoService;

    public MaSoiAutoController(MaSoiAutoService maSoiAutoService) {
        this.maSoiAutoService = maSoiAutoService;
    }

    @GetMapping
    Map<String, Object> getOrder(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return maSoiAutoService.getViewerOrderConfig(deviceId);
    }

    @PostMapping
    Map<String, Object> saveOrder(@RequestBody(required = false) DeviceOrderRequest request,
                                  HttpServletRequest httpRequest,
                                  HttpServletResponse httpResponse) {
        String deviceId = CookieUtil.setCookie(httpRequest.getCookies(), httpResponse).getValue();
        Map<String, Integer> payload = request == null || request.getDeviceOrder() == null
                ? Map.of()
                : request.getDeviceOrder();
        return maSoiAutoService.updateViewerOrder(deviceId, payload);
    }

    @GetMapping("/settings")
    @ResponseBody
    SettingDto getSettings(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return maSoiAutoService.getSetting(deviceId);
    }

    @PostMapping("/settings")
    @ResponseBody
    SettingDto updateSettings(@RequestBody SettingDto settingDto, HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return maSoiAutoService.updateSetting(deviceId, settingDto);
    }
}
