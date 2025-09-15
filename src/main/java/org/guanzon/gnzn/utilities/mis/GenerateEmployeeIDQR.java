package org.guanzon.gnzn.utilities.mis;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.guanzon.appdriver.base.GRider;
import static org.guanzon.gnzn.utilities.mis.CustomQR.generateQR;

public class GenerateEmployeeIDQR {
    public static void main (String [] args){
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Maven_Systems";
        }
        else{
            path = "/srv/GGC_Maven_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        try {
            Properties po_props = new Properties();
            po_props.load(new FileInputStream(path + "/config/cas.properties"));
            
            GRider instance = null;
                    
            if (po_props.getProperty("developer.mode").equals("1")){
                instance = new GRider("gRider");
        
                if (!instance.logUser("gRider", "M001000001")){
                    System.exit(1);
                }
            } else {
                System.err.println("Unable to log user.");
                System.exit(1);
            }
            
            String url = "https://www.guanzongroup.com.ph/employee?id=";
            
            String sql = "SELECT" +
                                "  a.sEmployID" +
                                ", b.sCompnyNm" + 
                            " FROM Employee_Master001 a" +
                                ", Client_Master b" +
                            " WHERE a.sEmployID = b.sClientID" +
                                " AND a.sEmployID IN ('M00112001985', 'M05411000386', 'M00120002070', 'M01211000072', 'M00121000395', 'M00115000170', 'M00116001617', 'M00110013877', 'M00118000445', 'M00122001541', 'M01612000282', 'M01603000117', 'M00120001024', 'M03611000385', 'M01608001418', 'M00110013824', 'M00117000347', 'M00110003600', 'M00107007057', 'M01612000320', 'M01310000757', 'M00114000812', 'M00805000157', 'M00109020429', 'M00115001667', 'M00116000533', 'C00109001231', 'M00111005581', 'M00116001560', 'M00115001726', 'M00113000832', 'M00112000874', 'M00122001322', 'M00104000932', 'M00114000019', 'M00115000996', 'M00115000845', 'M00116002521', 'M00110017105', 'M00507001910', 'M05309000007', 'M00113001600', 'M00117003389', 'M05311000055', 'M00109013312', 'M00122001106', 'M00103001003', 'M00117001481', 'M00110004041', 'M00111000071', 'M00108011539', 'M00118002693', 'M03213002045', 'M00120001391', 'C02710000599', 'M00105000801', 'M00115001531', 'M00118002218', 'M00118000084', 'M00112001966', 'M00117000411', 'M00114000391', 'M06311000470', 'M00707000515', 'M00113000670', 'M00117000864', 'M00121000709', 'M01310000757', 'M00112001698', 'M00114001563', 'M00117001333', 'M00117000786', 'M00114000793', 'M00114000759', 'M00117003300', 'M06112000071', 'M00122000514', 'M06111000557', 'M00117003293', 'M00117003302', 'M00116000966', 'M05810000086', 'M00112001024', 'M00116000259', 'M05810000085', 'M00116000394', 'M00122001196', 'M00107002056', 'M03009000876', 'M00118001012', 'M00114000678', 'M00118001439', 'M00118000306', 'M00119001823', 'M00511001084', 'M00117000775', 'M00121001652', 'M00118000408', 'M00121001095', 'M04511000121', 'M04511000123', 'C00109001324', 'M00605000151', 'M00114000764', 'M00121001037', 'M00118001870', 'M00118001272', 'M00116001750', 'M00112000863', 'M00117003249', 'M00118000309', 'M00122001321', 'M00114001278', 'M00122001703', 'M00118000053', 'M00118002486', 'M00119002292', 'M00119001688', 'M00111004476', 'M00116002279', 'M00119000712', 'M00115000805', 'M00117003153', 'M00114001426', 'M00118001579', 'M00115001707', 'M00116002318', 'M07412000388', 'M07412000387', 'M00112000427', 'M00117001424', 'M00117000357', 'M00113001566', 'M00119000173', 'M00120001779', 'M00121001036')" +
                                " AND a.sIDNumber IS NOT NULL";
            
            ResultSet rs = instance.executeQuery(sql);
            
            while (rs.next()){
                String id = rs.getString("sEmployID");
                String name = rs.getString("sCompnyNm");
                String fname = name + " - " + id + ".png"; 
                
                System.out.println(url + encryptBase64(id, "empid"));
                
                generateQR(
                    url + encryptBase64(id, "empid"),
                    System.getProperty("sys.default.path.config") + "/images/logo 2.png",
                    System.getProperty("sys.default.path.config") + "/temp/idqr/" + fname + ".png",
                    600,
                    Color.WHITE,
                    Color.BLACK,
                    0, 40f,
                    40f,
                    0.20f,
                    Color.WHITE,
                    Color.BLACK, 3f,
                    0.10f, 0.25f,
                    null,                   // no label
                    "SansSerif", 30f, false, Color.WHITE,
                    40f, 10f                // ✅ label marginTop, lineSpacing
                );
                
//                generateQR(
//                    url + encryptBase64(id, "empid"),
//                    System.getProperty("sys.default.path.config") + "/images/logo 2.png",
//                    System.getProperty("sys.default.path.config") + "/temp/idqr/" + fname + ".png",
//                    600,
//                    Color.WHITE,
//                    Color.BLACK,
//                    8f, 40f,
//                    40f,
//                    0.20f,
//                    Color.WHITE,
//                    Color.BLACK, 3f,
//                    0.10f, 0.25f,
//                    null,                   // no label
//                    "SansSerif", 30f, false, Color.WHITE,
//                    40f, 10f                // ✅ label marginTop, lineSpacing
//                );
            }
        } catch (IOException | SQLException e){
            e.printStackTrace();
            System.exit(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    private static byte[] sha256Bytes(String key) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(key.getBytes(StandardCharsets.UTF_8)); // 32 bytes
    }

    private static String encryptBase64(String plaintext, String key) throws Exception {
        byte[] aesKey = sha256Bytes(key);
        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static String decryptBase64(String base64Cipher, String key) throws Exception {
        byte[] aesKey = sha256Bytes(key);
        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decoded = Base64.getDecoder().decode(base64Cipher);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
