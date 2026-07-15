package com.example.spyfall.controller;

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

@RestController
public class ImageController {

    @GetMapping("/qrcode.png")
    public ResponseEntity<Resource> getQRCode() {
        File file = new File(System.getProperty("user.dir") + "/src/main/resources/picture/qrcode.png");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(file.length());
        return new ResponseEntity<>(new FileSystemResource(file), headers, HttpStatus.OK);
    }

    @GetMapping("/spy.png")
    public ResponseEntity<Resource> getQRCode2() throws IOException {
        File file = new File(System.getProperty("user.dir") + "/src/main/resources/picture/spy.png");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(file.length());
        return new ResponseEntity<>(new FileSystemResource(file), headers, HttpStatus.OK);
    }

    @GetMapping("/gDuck.png")
    public ResponseEntity<Resource> getGDuck() throws IOException {
        File file = new File(System.getProperty("user.dir") + "/src/main/resources/picture/gDuck.png");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(file.length());
        return new ResponseEntity<>(new FileSystemResource(file), headers, HttpStatus.OK);
    }

    @GetMapping("/spy2.png")
    public ResponseEntity<Resource> getSpy2() throws IOException {
        File file = new File(System.getProperty("user.dir") + "/src/main/resources/picture/spy2.png");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(file.length());
        return new ResponseEntity<>(new FileSystemResource(file), headers, HttpStatus.OK);
    }
}
