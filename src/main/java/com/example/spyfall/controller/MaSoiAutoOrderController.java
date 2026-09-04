package com.example.spyfall.controller;

import com.example.spyfall.common.DeviceOrderRequest;
import com.example.spyfall.service.MaSoiAutoOrderService;
import com.example.spyfall.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/msAuto/order")
public class MaSoiAutoOrderController {
    private final MaSoiAutoOrderService orderService;

    public MaSoiAutoOrderController(MaSoiAutoOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    Map<String, Object> getOrder(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = CookieUtil.setCookie(request.getCookies(), response).getValue();
        return orderService.getViewerOrderConfig(deviceId);
    }

    @PostMapping
    Map<String, Object> saveOrder(@RequestBody(required = false) DeviceOrderRequest request,
                                  HttpServletRequest httpRequest,
                                  HttpServletResponse httpResponse) {
        String deviceId = CookieUtil.setCookie(httpRequest.getCookies(), httpResponse).getValue();
        Map<String, Integer> payload = request == null || request.getDeviceOrder() == null
                ? Map.of()
                : request.getDeviceOrder();
        return orderService.updateViewerOrder(deviceId, payload);
    }
}
