package com.example.spyfall.controller;

import com.example.spyfall.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

import static com.example.spyfall.util.Constant.*;

@RestController
public class ImageController {
    @Autowired
    private ImageService imageService;

    @GetMapping(QR_MS)
    public ResponseEntity<Resource> getQRCode() {
        return imageService.getQRlink(QR_MS, false);
    }

    @GetMapping(QR_MS_AUTO_V1)
    public ResponseEntity<Resource> getMSAutoV1() {
        return imageService.getQRlink(QR_MS_AUTO_V1, false);
    }

    @GetMapping(QR_MS_BLIND)
    public ResponseEntity<Resource> getMSBlind() {
        return imageService.getQRlink(QR_MS_BLIND, false);
    }

    @GetMapping(QR_SPY)
    public ResponseEntity<Resource> getQRCode2() {
        return imageService.getQRlink(QR_SPY, false);
    }

    @GetMapping(QR_SPY2)
    public ResponseEntity<Resource> getQRCode3() {
        return imageService.getQRlink(QR_SPY2, false);
    }

    @GetMapping(QR_GO_DUCK)
    public ResponseEntity<Resource> getGDuck() {
        return imageService.getQRlink(QR_GO_DUCK, false);
    }

    @GetMapping(QR_SETTING)
    public ResponseEntity<Resource> getSettingIcon() {
        return imageService.getQRlink(QR_SETTING, true);
    }

    @GetMapping(QR_FUNCTION)
    public ResponseEntity<Resource> getFunctionIcon() {
        return imageService.getQRlink(QR_FUNCTION, true);
    }

    @GetMapping(QR_CHAT)
    public ResponseEntity<Resource> getChatIcon() {
        return imageService.getQRlink(QR_CHAT, true);
    }
}
