package org.guanzon.gnzn.utilities.mis;

import java.awt.Color;
import static org.guanzon.gnzn.utilities.mis.CustomQR.generateQR;

public class CreateQRWithLogo {
    public static void main(String[] args) throws Exception {
        String attire = "https://apps.guanzongroup.com.ph/apk/attire.jpg";
        String invitation = "https://apps.guanzongroup.com.ph/apk/video.mp4";
        
//        // ✅ Example with styled label
//        generateQR(
//                attire,
//                "d:/logo.png",
//                "d:/qr_with_label.png",
//                600,
//                Color.WHITE,                   // background transparent
//                Color.BLACK,             // outer border color
//                8f, 40f,               // border thickness, border radius
//                40f,                    // QR margin
//                0.20f,                  // logo scale
//                Color.WHITE,            // logo background
//                Color.BLACK, 6f,        // logo border
//                0.15f, 0.25f,           // logo margin, logo border radius
//                new String[]{"Scan Me", "Exclusive Content"}, // label lines
//                "SansSerif", 26f, true, Color.WHITE, // font settings
//                40f, 10f                // ✅ label marginTop, lineSpacing
//        );

//        // ✅ Example without label (same as before)
//        generateQR(
//                attire,
//                "d:/logo.png",
//                "d:/attire.png",
//                600,
//                Color.WHITE,
//                Color.BLACK,
//                8f, 40f,
//                40f,
//                0.20f,
//                Color.WHITE,
//                Color.BLACK, 6f,
//                0.15f, 0.25f,
//                null,                   // no label
//                "SansSerif", 22f, false, Color.BLACK,
//                0f, 0f
//        );
        
        // ✅ Example without label (same as before)
        generateQR(
                invitation,
                "G:\\My Drive\\Guanzon\\Convention 2025\\images\\logo 2.png",
                "d:/invitation.png",
                600,
                Color.WHITE,
                Color.BLACK,
                8f, 40f,
                40f,
                0.20f,
                Color.WHITE,
                Color.BLACK, 3f,
                0.10f, 0.25f,
                null,                   // no label
                "SansSerif", 22f, false, Color.BLACK,
                0f, 0f
        );
        
        // ✅ Example without label (same as before)
        generateQR(
                attire,
                "G:\\My Drive\\Guanzon\\Convention 2025\\images\\logo 2.png",
                "d:/attire.png",
                600,
                Color.WHITE,
                Color.BLACK,
                8f, 40f,
                40f,
                0.20f,
                Color.WHITE,
                Color.BLACK, 3f,
                0.10f, 0.25f,
                null,                   // no label
                "SansSerif", 22f, false, Color.BLACK,
                0f, 0f
        );

        System.out.println("✅ QR codes saved with and without labels!");
    }
}
