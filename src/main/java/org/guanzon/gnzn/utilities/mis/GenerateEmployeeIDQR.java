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
                                ", a.sIDNumber" +
                            " FROM Employee_Master001 a" +
                                ", Client_Master b" +
                            " WHERE a.sEmployID = b.sClientID" +
                                " AND a.sEmployID IN ('M00106000333')" +
                                " AND a.sIDNumber IS NOT NULL" + 
                            " ORDER BY a.sIDNumber";
            
//            String sql = "SELECT" +
//                    "  'N00124000132' sEmployID" +
//                    ", 'DATARIO, MICHAEL CAMILO GONZALES' sCompnyNm" +
//                    ", '01-000-00055' sIDNumber";
            
            ResultSet rs = instance.executeQuery(sql);
            
            while (rs.next()){
                String no = rs.getString("sIDNumber");
                String id = rs.getString("sEmployID");
                String name = rs.getString("sCompnyNm");
                String fname = no + " - " + name + " - " + id + ".png"; 
                
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
