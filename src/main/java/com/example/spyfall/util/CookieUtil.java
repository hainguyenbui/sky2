package com.example.spyfall.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public class CookieUtil {
    public static Cookie setCookie(Cookie[] cookies, HttpServletResponse response) {

        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("deviceId".equals(c.getName())) {
                    return c;
                }
            }
        }

        String uuid = UUID.randomUUID().toString();

        Cookie cookie = new Cookie("deviceId", uuid);
        cookie.setPath("/");
        cookie.setMaxAge(365 * 24 * 60 * 60);

        response.addCookie(cookie);

        System.out.println("New Device: " + uuid);
        return cookie;
    }
}
