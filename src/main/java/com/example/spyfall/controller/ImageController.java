package com.example.spyfall.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController
public class ImageController {

    @GetMapping(value = "/qrcode.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQRCode() throws WriterException, IOException {
        return createQRCode("/ms/play");
    }

    @GetMapping(value = "/spy.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getSpyQRCode() throws WriterException, IOException {
        return createQRCode("/sp");
    }

    @GetMapping(value = "/gDuck.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getGoldenDuckQRCode() throws WriterException, IOException {
        return createQRCode("/gd/play");
    }

    @GetMapping(value = "/spy2.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getSpy2QRCode() throws WriterException, IOException {
        return createQRCode("/spy2");
    }

    private ResponseEntity<byte[]> createQRCode(String targetPath) throws WriterException, IOException {
        String targetUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(targetPath)
                .build()
                .toUriString();
        BitMatrix matrix = new MultiFormatWriter().encode(
                targetUrl,
                BarcodeFormat.QR_CODE,
                300,
                300,
                Map.of(EncodeHintType.CHARACTER_SET, "UTF-8"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", output);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.IMAGE_PNG)
                .body(output.toByteArray());
    }
}
