package com.example.spyfall.controller;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ImageControllerTest {

    @Test
    void werewolfQrCodeUsesTheHostAndPortOfTheCurrentRequest() throws Exception {
        MockMvc mockMvc = standaloneSetup(new ImageController()).build();

        byte[] png = mockMvc.perform(get("http://localhost:8083/qrcode.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(ImageIO.read(new ByteArrayInputStream(png)))));

        assertThat(new MultiFormatReader().decode(bitmap).getText())
                .isEqualTo("http://localhost:8083/ms/play");
    }
}
