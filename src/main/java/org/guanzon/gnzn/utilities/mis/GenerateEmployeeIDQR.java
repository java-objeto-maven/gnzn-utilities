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
                                " AND a.sEmployID IN ('M00118000444', 'M00122000905', 'M00120000567', 'M00123001040', 'M00125000298', 'M00124000215', 'M00122000894', 'M00118002731', 'M00120000121', 'M00124001787', 'M00122001276', 'M00119001593', 'M01307000068', 'M00125000038', 'M00124000105', 'M00124000033', 'M00123001106', 'M00115000724', 'M00119002102', 'M00124000994', 'M00123000414', 'M00123000199', 'M00118002065', 'M00124000832', 'M00116001818', 'M00124001587', 'M04110000294', 'M00118001812', 'M00117002703', 'M01608001415', 'M00119000042', 'M00122001522', 'M00123000955', 'M00118003176', 'M00119001501', 'M00124001425', 'M00119001052', 'M00117000419', 'M00125001131', 'M00124000217', 'M00123001044', 'M00124001360', 'M00117003104', 'M00124000954', 'M00115001443', 'M00116001089', 'M00123001271', 'M00125000045', 'M00116000849', 'M00118000057', 'M00124001484', 'M00123000406', 'M00124001511', 'M00120001115', 'M00113001156', 'M00122001028', 'C00109001263', 'M00113000848', 'M00120001544', 'M02111000633', 'M00118001513', 'M00122000947', 'M00123001701', 'M01010000017', 'M01611000965', 'M00114000099', 'M00123000692', 'M00123000458', 'M00121001659', 'M00116000433', 'M00123001085', 'M00118000350', 'C00109000582', 'M00125001128', 'M00120001999', 'M00125000123', 'M00113000426', 'M00121000898', 'M00120001426', 'M00121000248', 'M00119001760', 'M00123000396', 'C00109001217', 'M00123001273', 'M00124000799', 'M00122000387', 'M00120000148', 'M00123001036', 'M00119002200', 'M00125000586', 'M00122001772', 'M00115000083', 'C00109001330', 'M00121000627', 'M00123001011', 'M00122002002', 'M00125000225', 'M00114001143', 'M00117001748', 'M00119000204', 'M00122000759', 'M00124001771', 'M00124000605', 'M00122001577', 'M00113001598', 'M00122001138', 'M00124001260', 'C00109001322', 'M00123000394', 'M00124000612', 'M00123001155', 'M00122000414', 'M00117001503', 'M00124001482', 'M00122001963', 'M00123001108', 'M00122000571', 'M00124001354', 'M00122000678', 'M00125000207', 'M00120001158', 'M00122001048', 'M00124001417', 'M00119002659', 'M00112001023', 'M00121001587', 'M00123001274', 'M00113000604', 'M00123001510', 'M00805000491', 'M00119001822', 'M00118000409', 'M00115000084', 'M00123000193', 'M00124000881', 'M00124000492', 'M00121000887', 'M00113000913', 'M00122001223', 'M00123000948', 'M00124000389', 'M00119001756', 'M00123000649', 'M00125000278', 'M00122000636', 'M00122001540', 'M00124001597', 'M00123000086', 'M00124001574', 'M00111006256', 'M00121000993', 'M00123001516', 'M00113000201', 'M00113001216', 'M00116001410', 'M00122001847', 'M00121000994', 'M00121000715', 'M00124000983', 'M00123001465', 'M00123000870', 'M00124001189', 'M00124000957', 'M00120001284', 'M00119002370', 'M00122001421', 'M00119001782', 'M00123001402', 'M00122001130', 'M00122000890', 'M00124001224', 'M00124000909', 'M00119002116', 'M00122001437', 'M00124000889', 'M00122001660', 'M00125000092', 'M00119002059', 'M00112000325', 'M00116001022', 'M00123000495', 'M00124001593', 'C01511001346', 'M00905000253', 'M00124000058', 'M00123000089', 'M00123000031', 'M00122001705', 'M00124000422', 'M00908001193', 'M00109013317', 'M00124001571', 'M00122000614', 'M00123001575', 'M00508000170', 'M00109015485', 'M00118001080', 'M02611000068', 'M00120000216')" +
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
