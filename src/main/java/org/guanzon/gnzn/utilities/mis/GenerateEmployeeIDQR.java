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
                                " AND a.sEmployID IN ('M00125001396', 'M00125001333', 'M00125001472', 'M00125001471', 'M00122001278', 'M00115000751', 'M00117000507', 'M00121000333', 'M00119000755', 'M00123001511', 'M00123001494', 'M00125000481', 'M00125000167', 'M00124000878', 'M00122001575', 'M00117001601', 'M00124001760', 'M00125000208', 'M00123001555', 'M00122001689', 'M00123000257', 'M00123000833', 'M00123000255', 'M00125000625', 'M01111000992', 'M00122001984', 'M00118001750', 'M00121000118', 'M00123000699', 'M00122000537', 'M00120000464', 'M00124000608', 'M00123000974', 'M00107007132', 'M00120001865', 'M00119000041', 'M00120001707', 'M00124000595', 'M00124001056', 'M00120000555', 'M00122001497', 'M00125002059', 'M00120001551', 'M00120000602', 'M00121000029', 'M00105001636', 'M00108012422', 'M00122001508', 'M00124000134', 'M00114000809', 'M00124000032', 'M00124000036', 'M00123001243', 'M00118002693', 'M00124000892', 'M00119000520', 'M00122001277', 'M00124000060', 'M00907001195', 'M00112000916', 'M00123001268', 'M00114000336', 'M00123001105', 'M00121001443', 'M00125000679', 'M00122000241', 'M00125000435', 'M00119002437', 'M00124000297', 'M00124001650', 'M00118002403', 'M00124001645', 'M00124001576', 'M00123000399', 'M00118003174', 'M00120000608', 'M00125000791', 'M00123000253', 'M00121001661', 'M00125000622', 'M00121001753', 'M00123000714', 'M00122000314', 'M00123000313', 'M00124000293', 'M04609000061', 'M00124001474', 'M00115000004', 'M00122000762', 'M00107001296', 'M00109012535', 'M00116002025', 'M00124001188', 'M00122000129', 'M00125000999', 'M00125000096', 'M00120000146', 'M00109013894', 'M00125000576', 'M00123001093', 'M00117001584', 'M00109021071', 'M00119002098', 'M00122000573', 'M00124000883', 'M00122001814', 'M00119000430', 'M00109009753', 'M00123000589', 'M00110018846', 'M00119002362', 'M00124000309', 'M00119002368', 'M00124000244', 'M00119002391', 'M00125001472', 'M00117002697', 'M00118000454', 'M00125001468', 'M00125000082', 'M00116000796', 'M00122000309', 'M00124000425', 'M00119002349', 'M00126000205', 'M00118002589', 'M00125000173', 'M00119002377', 'M00125001125', 'M00119002354', 'M00123001576', 'M00124000488', 'M00114001603', 'M00126000066', 'M00123001471', 'M00123000923', 'M00113000954', 'M00125001038')" +
                                " AND a.sIDNumber IS NOT NULL" + 
                            " ORDER BY a.sIDNumber";
            
//            String sql = "SELECT" +
//                    "  'N00124000132' sEmployID" +
//                    ", 'DATARIO, MICHAEL CAMILO GONZALES' sCompnyNm" +
//                    ", '01-000-00055' sIDNumber";

//            String sql = "SELECT" +
//                            "  'C10121000221' sEmployID" +
//                            ", 'CLAVERIA, OLIVER MON INANDAN' sCompnyNm" +
//                            ", '01-000-00055' sIDNumber";
            
            ResultSet rs = instance.executeQuery(sql);
            
            while (rs.next()){
                String no = rs.getString("sIDNumber");
                String id = rs.getString("sEmployID");
                String name = rs.getString("sCompnyNm");
                String fname = no + " - " + name + " - " + id; 
                
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
