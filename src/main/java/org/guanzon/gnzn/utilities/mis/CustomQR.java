package org.guanzon.gnzn.utilities.mis;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

public class CustomQR {
    public static void generateQR(String text, String logoPath, String outputPath,
                                  int size,
                                  Color bgColor,
                                  Color borderColor, float borderThickness, float borderRadius,
                                  float margin,
                                  float logoScale,
                                  Color logoBgColor,
                                  Color logoBorderColor, float logoBorderThickness,
                                  float logoMarginScale, float logoBorderRadiusScale,
                                  String[] labelLines,
                                  String labelFontName, float labelFontSize, boolean labelBold,
                                  Color labelFontColor,
                                  float labelMarginTop, float labelLineSpacing)   // ✅ NEW
            throws WriterException, IOException {

        // Encode QR
        QRCodeWriter writer = new QRCodeWriter();
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints);
        int matrixSize = matrix.getWidth();

        float availableSize = size - (2 * margin);
        float moduleSize = availableSize / matrixSize;

        // ✅ Extra height if label is present
        int extraHeight = 0;
        if (labelLines != null && labelLines.length > 0) {
            extraHeight = Math.round(labelMarginTop + (labelLines.length * (labelFontSize + labelLineSpacing)));
        }

        // Create transparent image
        BufferedImage img = new BufferedImage(size, size + extraHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background (transparent if null)
        if (bgColor != null) {
            g.setColor(bgColor);
            g.fill(new RoundRectangle2D.Float(0, 0, size, size, borderRadius, borderRadius));
        }

        // Draw QR (pure black, no smoothing)
        Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g.setColor(Color.BLACK);
        for (int x = 0; x < matrixSize; x++) {
            for (int y = 0; y < matrixSize; y++) {
                if (matrix.get(x, y)) {
                    int px = Math.round(margin + x * moduleSize);
                    int py = Math.round(margin + y * moduleSize);
                    g.fillRect(px, py, Math.round(moduleSize), Math.round(moduleSize));
                }
            }
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);

        // Outer border
        g.setStroke(new BasicStroke(borderThickness));
        g.setColor(borderColor);
        float inset = borderThickness / 2f;
        g.draw(new RoundRectangle2D.Float(
                inset, inset,
                size - borderThickness, size - borderThickness,
                borderRadius, borderRadius));

        // Logo
        if (logoPath != null && !logoPath.isEmpty()) {
            BufferedImage logo = ImageIO.read(new File(logoPath));
            int logoW = Math.round(size * logoScale);
            int logoH = logoW * logo.getHeight() / logo.getWidth();
            int cx = (size - logoW) / 2;
            int cy = (size - logoH) / 2;

            float logoMargin = logoW * logoMarginScale;
            float logoBorderRadius = logoW * logoBorderRadiusScale;

            int plateX = Math.round(cx - logoMargin);
            int plateY = Math.round(cy - logoMargin);
            int plateW = Math.round(logoW + (logoMargin * 2));
            int plateH = Math.round(logoH + (logoMargin * 2));

            if (logoBgColor != null) {
                Shape plate = new RoundRectangle2D.Float(plateX, plateY, plateW, plateH,
                        logoBorderRadius, logoBorderRadius);
                g.setColor(logoBgColor);
                g.fill(plate);
            }

            if (logoBorderThickness > 0) {
                Shape border = new RoundRectangle2D.Float(plateX, plateY, plateW, plateH,
                        logoBorderRadius, logoBorderRadius);
                g.setStroke(new BasicStroke(logoBorderThickness));
                g.setColor(logoBorderColor);
                g.draw(border);
            }

            Shape clip = new RoundRectangle2D.Float(cx, cy, logoW, logoH,
                    logoBorderRadius, logoBorderRadius);
            Shape oldClip2 = g.getClip();
            g.setClip(clip);
            g.drawImage(logo, cx, cy, logoW, logoH, null);
            g.setClip(oldClip2);
        }

        // ✅ Label (if provided)
        if (labelLines != null && labelLines.length > 0) {
            int fontStyle = labelBold ? Font.BOLD : Font.PLAIN;
            g.setFont(new Font(labelFontName, fontStyle, Math.round(labelFontSize)));
            g.setColor(labelFontColor);

            FontMetrics fm = g.getFontMetrics();
            int yStart = size + Math.round(labelMarginTop);

            for (int i = 0; i < labelLines.length; i++) {
                String line = labelLines[i];
                int textWidth = fm.stringWidth(line);
                int x = (size - textWidth) / 2;
                int y = yStart + Math.round(i * (labelFontSize + labelLineSpacing));
                g.drawString(line, x, y);
            }
        }

        g.dispose();
        ImageIO.write(img, "png", new File(outputPath));
    }
}