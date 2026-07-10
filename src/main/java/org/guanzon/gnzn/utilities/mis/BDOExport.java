package org.guanzon.gnzn.utilities.mis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BDOExport {
    public static void main(String[] args) {
        String[][] data = new String[5][28];
        
        data[0][0] = "1";               //Type�of�Remittance		0 = RTGS; 2 = PESONET	Mandatory                                                                        
        data[0][1] = "1.00";            //Amount                            	Mandatory                                                                                    
        data[0][2] = "000000000001";    //Source�Account                    	Mandatory                                                                                    
        data[0][3] = "Juan";            //First�Name                        Mandatory if "Is Corporation = 0"	Mandatory                                                    
        data[0][4] = "Santos";          //Middle�Name                       	Optional                                                                                     
        data[0][5] = "Dela Cruz";       //Beneficiary�Name                  Last Name if "Is Corporation" = 0  ; Corporation Name if "Is Corporation = 1)	Mandatory        
        data[0][6] = "Dagupan City";    //Beneficiary�Address 1             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[0][7] = "na";              //Beneficiary�Address 2             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[0][8] = "na";              //Beneficiary�Address 3             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[0][9] = "012000000001";   //Beneficiary�Account�Number        	Mandatory                                                                                    
        data[0][10] = "BDO";            //Beneficiary�Bank                  Code in Other Bank Maintenance (Local Only)	Mandatory                                          
        data[0][11] = "na";             //Beneficiary�Bank�Address          	Mandatory                                                                                    
        data[0][12] = "na";             //Beneficiary�Information           	Optional                                                                                     
        data[0][13] = "1";              //Charge�Type                       0 = Beneficiary,1 = ON-US	Mandatory                                                            
        data[0][14] = "";               //Bank�to�Bank�Information          	Optional                                                                                     
        data[0][15] = "07/03/2026";     //Date�Established                  	Mandatory                                                                                    
        data[0][16] = "na";             //Place of Incorporation            	Mandatory                                                                                    
        data[0][17] = "na";             //Nationality                       from Country maintenance	Mandatory                                                            
        data[0][18] = "na";             //Nature of Business                	Mandatory                                                                                    
        data[0][19] = "0";              //Is Corporation                    0 = Individual, 1 = Corporation	Mandatory                                                      
        data[0][20] = "PHP";            //Beneficiary Currency Code         	Mandatory                                                                                    
        data[0][21] = "";               //Purpose Code                      Code from Purpose Code Maintenance	Mandatory if Remittance Type = Foreign Transfer            
        data[0][22] = "";               //Nature of Transfer                	Mandatory if Remittance Type = Foreign Transfer                                              
        data[0][23] = "";               //Swift Code                        Valid Swift Address from Other Bank Maintenance	Mandatory if Remittance Type = Foreign Transfer
        data[0][24] = "";               //Country of Destination            Description from Country Maintenance	Mandatory if Remittance Type = Foreign Transfer          
        data[0][25] = "";               //Importers Code                    	Mandatory if Remittance Type = Foreign Transfer                                              
        data[0][26] = "";               //Routing Number                    	Mandatory if Remittance Type = Foreign Transfer                                              
        data[0][27] = "";               //RTGS Purpose Code                 RTGS Purpose Codes - to be provided separately	Mandatory if Remittance Type = RTGS
        
        data[1][0] = "1";               //Type�of�Remittance		0 = RTGS; 2 = PESONET	Mandatory                                                                        
        data[1][1] = "1000.00";            //Amount                            	Mandatory                                                                                    
        data[1][2] = "000000000001";    //Source�Account                    	Mandatory                                                                                    
        data[1][3] = "";            //First�Name                        Mandatory if "Is Corporation = 0"	Mandatory                                                    
        data[1][4] = "";          //Middle�Name                       	Optional                                                                                     
        data[1][5] = "ABC Corporation";       //Beneficiary�Name                  Last Name if "Is Corporation" = 0  ; Corporation Name if "Is Corporation = 1)	Mandatory        
        data[1][6] = "Dagupan City";    //Beneficiary�Address 1             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[1][7] = "na";              //Beneficiary�Address 2             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[1][8] = "na";              //Beneficiary�Address 3             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[1][9] = "012000000002";   //Beneficiary�Account�Number        	Mandatory                                                                                    
        data[1][10] = "BDO";            //Beneficiary�Bank                  Code in Other Bank Maintenance (Local Only)	Mandatory                                          
        data[1][11] = "na";             //Beneficiary�Bank�Address          	Mandatory                                                                                    
        data[1][12] = "na";             //Beneficiary�Information           	Optional                                                                                     
        data[1][13] = "0";              //Charge�Type                       0 = Beneficiary,1 = ON-US	Mandatory                                                            
        data[1][14] = "";               //Bank�to�Bank�Information          	Optional                                                                                     
        data[1][15] = "07/03/2026";     //Date�Established                  	Mandatory                                                                                    
        data[1][16] = "na";             //Place of Incorporation            	Mandatory                                                                                    
        data[1][17] = "na";             //Nationality                       from Country maintenance	Mandatory                                                            
        data[1][18] = "na";             //Nature of Business                	Mandatory                                                                                    
        data[1][19] = "1";              //Is Corporation                    0 = Individual, 1 = Corporation	Mandatory                                                      
        data[1][20] = "PHP";            //Beneficiary Currency Code         	Mandatory                                                                                    
        data[1][21] = "";               //Purpose Code                      Code from Purpose Code Maintenance	Mandatory if Remittance Type = Foreign Transfer            
        data[1][22] = "";               //Nature of Transfer                	Mandatory if Remittance Type = Foreign Transfer                                              
        data[1][23] = "";               //Swift Code                        Valid Swift Address from Other Bank Maintenance	Mandatory if Remittance Type = Foreign Transfer
        data[1][24] = "";               //Country of Destination            Description from Country Maintenance	Mandatory if Remittance Type = Foreign Transfer          
        data[1][25] = "";               //Importers Code                    	Mandatory if Remittance Type = Foreign Transfer                                              
        data[1][26] = "";               //Routing Number                    	Mandatory if Remittance Type = Foreign Transfer                                              
        data[1][27] = "";               //RTGS Purpose Code                 RTGS Purpose Codes - to be provided separately	Mandatory if Remittance Type = RTGS
        
        data[2][0] = "1";               //Type�of�Remittance		0 = RTGS; 2 = PESONET	Mandatory                                                                        
        data[2][1] = "10.00";            //Amount                            	Mandatory                                                                                    
        data[2][2] = "000000000001";    //Source�Account                    	Mandatory                                                                                    
        data[2][3] = "";            //First�Name                        Mandatory if "Is Corporation = 0"	Mandatory                                                    
        data[2][4] = "";          //Middle�Name                       	Optional                                                                                     
        data[2][5] = "ABC Company Inc.";       //Beneficiary�Name                  Last Name if "Is Corporation" = 0  ; Corporation Name if "Is Corporation = 1)	Mandatory        
        data[2][6] = "Dagupan City";    //Beneficiary�Address 1             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[2][7] = "na";              //Beneficiary�Address 2             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[2][8] = "na";              //Beneficiary�Address 3             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[2][9] = "012000000003";   //Beneficiary�Account�Number        	Mandatory                                                                                    
        data[2][10] = "BPI";            //Beneficiary�Bank                  Code in Other Bank Maintenance (Local Only)	Mandatory                                          
        data[2][11] = "na";             //Beneficiary�Bank�Address          	Mandatory                                                                                    
        data[2][12] = "na";             //Beneficiary�Information           	Optional                                                                                     
        data[2][13] = "0";              //Charge�Type                       0 = Beneficiary,1 = ON-US	Mandatory                                                            
        data[2][14] = "";               //Bank�to�Bank�Information          	Optional                                                                                     
        data[2][15] = "07/03/2026";     //Date�Established                  	Mandatory                                                                                    
        data[2][16] = "na";             //Place of Incorporation            	Mandatory                                                                                    
        data[2][17] = "na";             //Nationality                       from Country maintenance	Mandatory                                                            
        data[2][18] = "na";             //Nature of Business                	Mandatory                                                                                    
        data[2][19] = "1";              //Is Corporation                    0 = Individual, 1 = Corporation	Mandatory                                                      
        data[2][20] = "PHP";            //Beneficiary Currency Code         	Mandatory                                                                                    
        data[2][21] = "";               //Purpose Code                      Code from Purpose Code Maintenance	Mandatory if Remittance Type = Foreign Transfer            
        data[2][22] = "";               //Nature of Transfer                	Mandatory if Remittance Type = Foreign Transfer                                              
        data[2][23] = "";               //Swift Code                        Valid Swift Address from Other Bank Maintenance	Mandatory if Remittance Type = Foreign Transfer
        data[2][24] = "";               //Country of Destination            Description from Country Maintenance	Mandatory if Remittance Type = Foreign Transfer          
        data[2][25] = "";               //Importers Code                    	Mandatory if Remittance Type = Foreign Transfer                                              
        data[2][26] = "";               //Routing Number                    	Mandatory if Remittance Type = Foreign Transfer                                              
        data[2][27] = "";               //RTGS Purpose Code                 RTGS Purpose Codes - to be provided separately	Mandatory if Remittance Type = RTGS
        
        data[3][0] = "1";               //Type�of�Remittance		0 = RTGS; 2 = PESONET	Mandatory                                                                        
        data[3][1] = "20.00";            //Amount                            	Mandatory                                                                                    
        data[3][2] = "000000000001";    //Source�Account                    	Mandatory                                                                                    
        data[3][3] = "";            //First�Name                        Mandatory if "Is Corporation = 0"	Mandatory                                                    
        data[3][4] = "";          //Middle�Name                       	Optional                                                                                     
        data[3][5] = "XYZ Trading";       //Beneficiary�Name                  Last Name if "Is Corporation" = 0  ; Corporation Name if "Is Corporation = 1)	Mandatory        
        data[3][6] = "Dagupan City";    //Beneficiary�Address 1             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[3][7] = "na";              //Beneficiary�Address 2             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[3][8] = "na";              //Beneficiary�Address 3             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[3][9] = "012000000103";   //Beneficiary�Account�Number        	Mandatory                                                                                    
        data[3][10] = "BPI";            //Beneficiary�Bank                  Code in Other Bank Maintenance (Local Only)	Mandatory                                          
        data[3][11] = "na";             //Beneficiary�Bank�Address          	Mandatory                                                                                    
        data[3][12] = "na";             //Beneficiary�Information           	Optional                                                                                     
        data[3][13] = "0";              //Charge�Type                       0 = Beneficiary,1 = ON-US	Mandatory                                                            
        data[3][14] = "";               //Bank�to�Bank�Information          	Optional                                                                                     
        data[3][15] = "07/03/2026";     //Date�Established                  	Mandatory                                                                                    
        data[3][16] = "na";             //Place of Incorporation            	Mandatory                                                                                    
        data[3][17] = "na";             //Nationality                       from Country maintenance	Mandatory                                                            
        data[3][18] = "na";             //Nature of Business                	Mandatory                                                                                    
        data[3][19] = "1";              //Is Corporation                    0 = Individual, 1 = Corporation	Mandatory                                                      
        data[3][20] = "PHP";            //Beneficiary Currency Code         	Mandatory                                                                                    
        data[3][21] = "";               //Purpose Code                      Code from Purpose Code Maintenance	Mandatory if Remittance Type = Foreign Transfer            
        data[3][22] = "";               //Nature of Transfer                	Mandatory if Remittance Type = Foreign Transfer                                              
        data[3][23] = "";               //Swift Code                        Valid Swift Address from Other Bank Maintenance	Mandatory if Remittance Type = Foreign Transfer
        data[3][24] = "";               //Country of Destination            Description from Country Maintenance	Mandatory if Remittance Type = Foreign Transfer          
        data[3][25] = "";               //Importers Code                    	Mandatory if Remittance Type = Foreign Transfer                                              
        data[3][26] = "";               //Routing Number                    	Mandatory if Remittance Type = Foreign Transfer                                              
        data[3][27] = "";               //RTGS Purpose Code                 RTGS Purpose Codes - to be provided separately	Mandatory if Remittance Type = RTGS
        
        data[4][0] = "1";               //Type�of�Remittance		0 = RTGS; 2 = PESONET	Mandatory                                                                        
        data[4][1] = "100.00";            //Amount                            	Mandatory                                                                                    
        data[4][2] = "000000000001";    //Source�Account                    	Mandatory                                                                                    
        data[4][3] = "Michael";            //First�Name                        Mandatory if "Is Corporation = 0"	Mandatory                                                    
        data[4][4] = "Torres";          //Middle�Name                       	Optional                                                                                     
        data[4][5] = "Cuison";       //Beneficiary�Name                  Last Name if "Is Corporation" = 0  ; Corporation Name if "Is Corporation = 1)	Mandatory        
        data[4][6] = "Dagupan City";    //Beneficiary�Address 1             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[4][7] = "na";              //Beneficiary�Address 2             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[4][8] = "na";              //Beneficiary�Address 3             Restricted special characters: *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|Ãñ��	Mandatory              
        data[4][9] = "012000000077";   //Beneficiary�Account�Number        	Mandatory                                                                                    
        data[4][10] = "BDO";            //Beneficiary�Bank                  Code in Other Bank Maintenance (Local Only)	Mandatory                                          
        data[4][11] = "na";             //Beneficiary�Bank�Address          	Mandatory                                                                                    
        data[4][12] = "na";             //Beneficiary�Information           	Optional                                                                                     
        data[4][13] = "1";              //Charge�Type                       0 = Beneficiary,1 = ON-US	Mandatory                                                            
        data[4][14] = "";               //Bank�to�Bank�Information          	Optional                                                                                     
        data[4][15] = "07/03/2026";     //Date�Established                  	Mandatory                                                                                    
        data[4][16] = "na";             //Place of Incorporation            	Mandatory                                                                                    
        data[4][17] = "na";             //Nationality                       from Country maintenance	Mandatory                                                            
        data[4][18] = "na";             //Nature of Business                	Mandatory                                                                                    
        data[4][19] = "0";              //Is Corporation                    0 = Individual, 1 = Corporation	Mandatory                                                      
        data[4][20] = "PHP";            //Beneficiary Currency Code         	Mandatory                                                                                    
        data[4][21] = "";               //Purpose Code                      Code from Purpose Code Maintenance	Mandatory if Remittance Type = Foreign Transfer            
        data[4][22] = "";               //Nature of Transfer                	Mandatory if Remittance Type = Foreign Transfer                                              
        data[4][23] = "";               //Swift Code                        Valid Swift Address from Other Bank Maintenance	Mandatory if Remittance Type = Foreign Transfer
        data[4][24] = "";               //Country of Destination            Description from Country Maintenance	Mandatory if Remittance Type = Foreign Transfer          
        data[4][25] = "";               //Importers Code                    	Mandatory if Remittance Type = Foreign Transfer                                              
        data[4][26] = "";               //Routing Number                    	Mandatory if Remittance Type = Foreign Transfer                                              
        data[4][27] = "";               //RTGS Purpose Code                 RTGS Purpose Codes - to be provided separately	Mandatory if Remittance Type = RTGS
        
        for (int lnCtr = 0; lnCtr <= data.length -1; lnCtr++){
            System.out.println();
            for (int y = 0; y <= 27; y++){
                System.out.print(data[lnCtr][y] + "\t");
            }
        }

        String outputFile = "BDO_Outward_Payment.txt";
        try {
            exportToTextFile(data, outputFile);
            System.out.println();
            System.out.println("Export successful: " + Paths.get(outputFile).toAbsolutePath());
        } catch (IOException e) {
            System.out.println();
            System.err.println("Export failed: " + e.getMessage());
        }
    }

    /**
     * Writes the detail records to a tab-delimited TXT file, one record per line,
     * matching the Outward Payment File Format spec (28 fields, TAB delimiter,
     * no header row, no trailing delimiter).
     *
     * @param data       the records, each row containing exactly 28 fields
     * @param filePath   destination file path (created if it doesn't exist, overwritten if it does)
     * @throws IOException if the file cannot be written
     */
    public static void exportToTextFile(String[][] data, String filePath) throws IOException {
        Path path = Paths.get(filePath);

        try (BufferedWriter writer = Files.newBufferedWriter(
                path, StandardCharsets.UTF_8)) {

            for (int lnCtr = 0; lnCtr < data.length; lnCtr++) {
                StringBuilder line = new StringBuilder();
                for (int y = 0; y < data[lnCtr].length; y++) {
                    String value = data[lnCtr][y] == null ? "" : data[lnCtr][y];
                    line.append(value);
                    if (y < data[lnCtr].length - 1) {
                        line.append("\t");
                    }
                }
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }
}