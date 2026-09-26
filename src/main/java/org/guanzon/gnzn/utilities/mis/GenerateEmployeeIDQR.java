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
            
//            String sql = "SELECT" +
//                                "  a.sEmployID" +
//                                ", b.sCompnyNm" +
//                                ", a.sIDNumber" +
//                            " FROM Employee_Master001 a" +
//                                ", Client_Master b" +
//                            " WHERE a.sEmployID = b.sClientID" +
//                                " AND a.sEmployID IN ('M00125000586', 'M00125000288', 'M00115001227', 'M00124000424', 'M00118002538', 'M00123000860', 'M00121000554', 'M00119000655', 'M00114000318', 'M00119000203', 'M00121001728', 'M00125000030', 'M00123000295', 'M00123001413', 'M00124000885', 'M00110020099', 'M00123000474', 'M00122001355', 'C00209002662', 'M00122001678', 'M00122001329', 'M05811000285', 'M00124001034', 'M00118001664', 'M00122000780', 'M00125000920', 'M00122000151', 'M00124000095', 'M00120000590', 'M00122000388', 'M00108002604', 'M00125000724', 'M00117001773', 'M00120001598', 'M00118000290', 'M00122001659', 'M00125000443', 'M00122001265', 'M00123001382', 'M00121001482', 'M00117000412', 'M00124001036', 'N00122000023', 'M00116002522', 'M00117000747', 'M00121001136', 'M00123000389', 'M00125001270', 'M00120001106', 'M00124000491', 'M00116001258', 'M00117000863', 'M00125001331', 'M00122000240', 'M00123001676', 'M00124000131', 'M00120001696', 'M00118002532', 'M00124001263', 'M00118000874', 'M00120000285', 'M02911000594', 'M00121000865', 'M00120000597', 'M00112000469', 'M00116001352', 'M00117002964', 'M00124000421', 'M00119002192', 'M00118002156', 'M00124000083', 'M00123001514', 'M00125001338', 'M00123000484', 'M00124000224', 'M00118001814', 'M00125002051', 'C00109001000', 'M00122001154', 'M00117001288', 'M00124000055', 'M00123000627', 'M00121000257', 'M00124001541', 'M00124001761', 'M00121000565', 'M00115000203', 'M00123000190', 'M00121001367', 'M00114001182', 'M00124000947', 'M00118001353', 'C02711001731', 'M00117001064', 'M03206000211', 'M00124001603', 'M00119002099', 'M04211000319', 'M00109013300', 'M00125001346', 'M00121000707', 'M00121000245', 'M00124001413', 'M00122001658', 'M00120001754', 'M00124000978', 'M00124000510', 'M00123001084', 'M02912000484', 'M00124000981', 'M04212000069', 'M00111005412', 'M00122000504', 'M00122001205', 'M00124001350', 'M00112001935', 'M00123001411', 'M00121000873', 'M00117001596', 'M00123000575', 'M00122001576', 'M00118001607', 'M00124001412', 'M00124000311', 'M00109013321', 'M00110017108', 'M00119000571', 'M00118001210', 'M00124000875', 'M00121000783', 'M00121000368', 'M00125001103', 'M00118002861', 'M00124000139')" +
//                                " AND a.sIDNumber IS NOT NULL" + 
//                            " ORDER BY a.sIDNumber";
            
//            String sql = "SELECT" +
//                    "  'N00124000132' sEmployID" +
//                    ", 'DATARIO, MICHAEL CAMILO GONZALES' sCompnyNm" +
//                    ", '01-000-00055' sIDNumber";

//            String sql = "SELECT" +
//                            "  'C10121000221' sEmployID" +
//                            ", 'CLAVERIA, OLIVER MON INANDAN' sCompnyNm" +
//                            ", '01-000-00055' sIDNumber";

            String sql = "SELECT" +
                            "  'M02917001495' sEmployID" +
                            ", 'BARROZO, AMELIA FERRER' sCompnyNm" +
                            ", '01-000-00064' sIDNumber";
            
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
