package com.example.spyfall;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SpringBootApplication
public class SpyfallApplication {


	public static void main(String[] args) {
		SpringApplication.run(SpyfallApplication.class, args);
		String systemEnv = System.getProperty("os.name");

		String serverPort = "8083";
		String filepath = "";
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
					.filter(addr -> addr instanceof Inet4Address && !addr.isLoopbackAddress())
					.filter(ip44 -> ip44.getHostAddress().startsWith("192.168."))
					.map(InetAddress::getHostAddress)
					.findFirst()
					.orElse("127.0.0.1");
			System.out.println("Port: " + ip4 + ":" + serverPort );
			String content1 = "http://" + ip4 + ":" + serverPort + "/ms/play";
			String content2 = "http://" + ip4 + ":" + serverPort + "/sp";
			String content3 = "http://" + ip4 + ":" + serverPort + "/gd/play";
			String content4 = "http://" + ip4 + ":" + serverPort + "/spy2";
			Map<String, String> contents = new HashMap<>();
			contents.put(content1, "src/main/resources/picture/qrcode.png");
			contents.put(content2, "src/main/resources/picture/spy.png");
			contents.put(content3, "src/main/resources/picture/gDuck.png");
			contents.put(content4, "src/main/resources/picture/spy2.png");

			contents.forEach((key, value) -> {
				int width = 300;
				int height = 300;
				Path outputPath = Paths.get(System.getProperty("user.dir")).resolve(value);

				Map<EncodeHintType, Object> hints = new HashMap<>();
				hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
				try {
					BitMatrix matrix = new MultiFormatWriter().encode(
							key, BarcodeFormat.QR_CODE, width, height, hints);

					MatrixToImageWriter.writeToPath(matrix, "PNG", outputPath);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
