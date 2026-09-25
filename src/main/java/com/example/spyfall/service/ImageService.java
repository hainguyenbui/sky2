package com.example.spyfall.service;

import com.example.spyfall.util.AppProperties;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.example.spyfall.util.Constant.*;

@Service
public class ImageService {
    @Autowired
    private AppProperties appProperties;

    @PostConstruct
    public void createImage() {
        String linuxUrl = appProperties.getLinux().getUrl();
        String linuxFolder = appProperties.getLinux().getFolder();
        String winFolder = appProperties.getWin();
        String phoneFolder = appProperties.getPhone();

        String systemEnv = System.getProperty("os.name");

        // lay IP url
        try {
            String ip4 = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .filter(ni -> {
                        try {
                            return ni.isUp() && !ni.isLoopback() && !ni.isVirtual();
                        } catch (SocketException e) {
                            return false;
                        }
                    })
                    .flatMap(ni -> Collections.list(ni.getInetAddresses()).stream())
                    .filter(addr -> addr instanceof Inet4Address)
                    .filter(InetAddress::isSiteLocalAddress)
                    .map(InetAddress::getHostAddress)
                    .findFirst()
                    .orElse("127.0.0.1");
//			String content1 = "http://" + ip4 + ":" + serverPort + "/ms/play";
//			String content2 = "http://" + ip4 + ":" + serverPort + "/sp";
//			String content3 = "http://" + ip4 + ":" + serverPort + "/gd/play";
//			String content4 = "http://" + ip4 + ":" + serverPort + "/spy2";
            String content1 = "http://" + ip4 + MS_LINK;
            String content2 = "http://" + ip4 + SPY_LINK;
            String content3 = "http://" + ip4 + GO_DUCK_LINK;
            String content4 = "http://" + ip4 + SPY2_LINK;
            String content5 = "http://" + ip4 + MS_AUTO_V1_LINK;
            String content6 = "http://" + ip4 + MS_BLIND_LINK;
            Map<String, String> contents = new HashMap<>();
            if (systemEnv.contains("Win")) {
                contents.put(content1, winFolder.substring(1) + QR_MS);
                contents.put(content2, winFolder.substring(1) + QR_SPY);
                contents.put(content3, winFolder.substring(1) + QR_GO_DUCK);
                contents.put(content4, winFolder.substring(1) + QR_SPY2);
                contents.put(content5, winFolder.substring(1) + QR_MS_AUTO_V1);
                contents.put(content6, winFolder.substring(1) + QR_MS_BLIND);
            } else if (systemEnv.contains("Linux")) {
                contents.put(linuxUrl + MS_LINK, linuxFolder + QR_MS);
                contents.put(linuxUrl + SPY_LINK, linuxFolder + QR_SPY);
                contents.put(linuxUrl + GO_DUCK_LINK, linuxFolder + QR_GO_DUCK);
                contents.put(linuxUrl + SPY2_LINK, linuxFolder + QR_SPY2);
                contents.put(linuxUrl + MS_AUTO_V1_LINK, linuxFolder + QR_MS_AUTO_V1);
                contents.put(linuxUrl + MS_BLIND_LINK, linuxFolder + QR_MS_BLIND);
            } else {
                contents.put(content1, phoneFolder + QR_MS);
                contents.put(content2, phoneFolder + QR_SPY);
                contents.put(content3, phoneFolder + QR_GO_DUCK);
                contents.put(content4, phoneFolder + QR_SPY2);
                contents.put(content5, phoneFolder + QR_MS_AUTO_V1);
                contents.put(content6, phoneFolder + QR_MS_BLIND);
            }

            contents.forEach((key, value) -> {
                int width = 300;
                int height = 300;
                Path outputPath = Paths.get(System.getProperty("user.dir")).resolve(value);

                Map<EncodeHintType, Object> hints = new HashMap<>();
                hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                try {
                    BitMatrix matrix = new MultiFormatWriter().encode(
                            key, BarcodeFormat.QR_CODE, width, height, hints);

                    MatrixToImageWriter.writeToPath(matrix, PNG, outputPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResponseEntity<Resource> getQRlink(String qrCode, boolean isSvg) {
        String linuxFolder = appProperties.getLinux().getFolder();
        String winFolder = appProperties.getWin();
        String phoneFolder = appProperties.getPhone();

        File file;
        if (System.getProperty("os.name").contains("Win")) {
            file = new File(System.getProperty("user.dir") +  winFolder + qrCode);

        } else if (System.getProperty("os.name").contains("Linux")) {
            file = new File(linuxFolder + qrCode);
        } else {
            file = new File(phoneFolder + qrCode);
        }

        HttpHeaders headers = new HttpHeaders();
        if (isSvg) {
            headers.setContentType(MediaType.valueOf("image/svg+xml"));
        } else {
            headers.setContentType(MediaType.IMAGE_PNG);
        }
        headers.setContentLength(file.length());
        return new ResponseEntity<>(new FileSystemResource(file), headers, HttpStatus.OK);
    }
}
