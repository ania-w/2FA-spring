package com.ania.auth.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class QrCodeUtil {

    public static byte[] generateQR(String secret) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(secret, BarcodeFormat.QR_CODE, 200, 200, hints);

        BufferedImage qrCodeImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                qrCodeImage.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrCodeImage, "png", baos);
        return baos.toByteArray();
    }

}
