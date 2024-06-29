package org.guanzon.gnzn.utilities.lib;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.mail.MessagingException;
import org.apache.commons.lang3.StringUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.gnzn.utilities.mail.MessageInfo;
import org.guanzon.gnzn.utilities.mail.SendMail;
import org.json.simple.JSONObject;

public class PEF_Notification {
    public GRider _app;
    
    public int _months;
    public String _evaltype;
    public String _emptype;
    
    /**
     * public PEF_Alert(GRider foGRider){
     * 
     * @param foGRider 
     */
    public PEF_Notification(GRider foGRider){
        _app = foGRider;
    }
    
    /**
     * public void setEvaluationType(String fsValue){
     * 
     * @param fsValue\n
     * 0 - For Probationary\n
     * 1 - For Regularization or End of Contract\n
     */
    public void setEvaluationType(String fsValue){
        _evaltype = fsValue;
    }
    
    /**
     * public void setEmployeeType(String fsValue){
     * 
     * @param fsValue\n
     * A - Agency\n
     * C - Casual\n
     * P - Probationary\n
     * R - Regular\n
     */
    public void setEmployeeType(String fsValue){
        _emptype = fsValue;
    }
    
    /**
     * public void setMonthAge(int fnValue){
     * 
     * @param fnValue\n 
     * 3 - 3 months age, currently in 4th mon\n
     * 4 - 4 months age, currently in 5th mon\n
     */
    public void setMonthAge(int fnValue){
        _months = fnValue;
    }
    
    public JSONObject NewTransaction(){
        JSONObject loJSON;
        
        if (_app == null){
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", "Application driver is not set...");
            return loJSON;
        }
        
        if (_evaltype == null){
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", "Evaluation type is not set...");
            return loJSON;
        }
        
        if (_emptype == null){
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", "Employee type is not set...");
            return loJSON;
        }
        
        switch (_months){
            case 3:
            case 4:
                break;
            default:
                loJSON = new JSONObject();
                loJSON.put("result", "error");
                loJSON.put("message", "Aging month is not yet supported...");
                return loJSON;
        }
        
        String lsSQL = getSQ_EvalByMonth();
        
        ResultSet loRS = _app.executeQuery(lsSQL);
        
        try {
            while(loRS.next()){
                String lsEmailAdd;
                
                if (loRS.getString("cMainOffc").equals("1")){
                    lsEmailAdd = loRS.getString("sDeptMail");
                } else {
                    lsEmailAdd = loRS.getString("sBranchMl");
                }
                
                if (!lsEmailAdd.isEmpty()){
                    lsSQL = MiscUtil.getNextCode("Employee_PEF_Alert", "sTransNox", true, _app.getConnection(), _app.getBranchCode());
                
                    lsSQL = "INSERT INTO Employee_PEF_Alert SET" +
                            "  sTransNox = " + SQLUtil.toSQL(lsSQL) +
                            ", dTransact = " + SQLUtil.toSQL(_app.getServerDate()) +
                            ", cEvalType = " + SQLUtil.toSQL(_evaltype) +
                            ", sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                            ", sBranchCd = " + SQLUtil.toSQL(loRS.getString("sBranchCd")) +
                            ", sDeptIDxx = " + SQLUtil.toSQL(loRS.getString("sDeptIDxx")) +
                            ", sEmpLevID = " + SQLUtil.toSQL(loRS.getString("sEmpLevID")) +
                            ", cEmpTypex = " + SQLUtil.toSQL(loRS.getString("cEmpTypex")) +
                            ", nNoMonths = " + (_months + 1) +
                            ", sMangerID = " + SQLUtil.toSQL(getManagerID(loRS.getString("cMainOffc"), loRS.getString("sBranchCd"), loRS.getString("sDeptIDxx"))) +
                            ", sEmailAd1 = " + SQLUtil.toSQL(lsEmailAdd);

                    if (_months <= 3) {
                        lsSQL +=
                                ", sEmailAd2 = NULL" +
                                ", sEmailAd3 = NULL";
                    } else {
                        if (loRS.getString("cMainOffc").equals("1")){
                            lsSQL +=
                                ", sEmailAd2 = 'hr@guanzongroup.com.ph'" +
                                ", sEmailAd3 = NULL";
                        } else {
                            lsSQL +=
                                ", sEmailAd2 = 'hr@guanzongroup.com.ph'" +
                                ", sEmailAd3 = " + SQLUtil.toSQL(getAreaMail(loRS.getString("sBranchCd")));
                        }
                    }

                    lsSQL += 
                            ", cMailSent = '0'" +
                            ", dMailSent = NULL" +
                            ", sMobileNo = NULL" +
                            ", cMobilNtf = '0'" +
                            ", dMobilNtf = NULL" +
                            ", sUserIDxx = NULL" +
                            ", cAppNotif = '0'" +
                            ", dAppNotif = NULL" +
                            ", sModified = " + SQLUtil.toSQL(_app.getUserID()) +
                            ", dModified = " + SQLUtil.toSQL(_app.getServerDate()); 

                    if (_app.executeQuery(lsSQL, "Employee_PEF_Alert", _app.getBranchCode(), "") <= 0){
                        loJSON = new JSONObject();
                        loJSON.put("result", "error");
                        loJSON.put("message", "Unable to create notification...");
                        return loJSON;
                    }
                }
            }
        } catch (SQLException e) {
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", e.getMessage());
            return loJSON;
        }
        
        loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Notifications created...");
        return loJSON;
    }
    
    public JSONObject SendNotifications(){
        JSONObject loJSON;
        
        if (_app == null){
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", "Application driver is not set...");
            return loJSON;
        }
        
        SendMail mail = new SendMail(System.getProperty("sys.default.path.config"), "GMail");
        
        if (mail.connect(true)){
            try {
                String lsSQL = getSQ_2Send();
            
                ResultSet loRS = _app.executeQuery(lsSQL);
                
                while (loRS.next()){
                    if (!loRS.getString("sEmailAd1").isEmpty()){
                        MessageInfo msginfo = new MessageInfo();           
                        msginfo.addTo(loRS.getString("sEmailAd1"));            
                        
                        if (!loRS.getString("sEmailAd2").isEmpty()){
                            msginfo.addCC(loRS.getString("sEmailAd2"));            
                        }
                        
                        if (!loRS.getString("sEmailAd3").isEmpty()){
                            msginfo.addCC(loRS.getString("sEmailAd2"));            
                        }
                        
                        msginfo.setSubject("Employee Evaluation Request");
                        
                        if (loRS.getInt("nNoMonths") <= 4){
                            msginfo.setBody(("KAY gandang araw " + loRS.getString("xMangerNm")).trim() +"!\n<br>" +
                                        "\n<br>" +
                                        "This is a system generated message from the GGC Human Capital Management.\n<br>" +
                                        " \n<br>" +
                                        "You are hereby notified to give due evaluation & recommendations for " + loRS.getString("xEmployNm") + ", on his " + loRS.getInt("nNoMonths") + getSuffix(loRS.getInt("nNoMonths"))+ " month as probationary. \n<br>" +
                                        "\n<br>" +
                                        "Email the latest PEF to HCM at hr@guanzongroup.com.ph after seeking approval from your Area Head/Department Manager etc. \n<br>" +
                                        "\n<br>" +
                                        "Thank you for your compliance.");
                        } else {
                            msginfo.setBody(("KAY gandang araw " + loRS.getString("xMangerNm")).trim() +"!\n<br>" +
                                        "\n<br>" +
                                        "This is a system generated message from the GGC Human Capital Management.\n<br>" +
                                        " \n<br>" +
                                        "You are hereby notified to give due evaluation & recommendations for " + loRS.getString("xEmployNm") + ", on his " + loRS.getInt("nNoMonths") + getSuffix(loRS.getInt("nNoMonths"))+ " month as probationary. \n<br>" +
                                        "\n<br>" +
                                        "Email the latest PEF to HCM at hr@guanzongroup.com.ph after seeking approval from your Area Head/Department Manager etc. \n<br>" +
                                        "\n<br>" +
                                        "Thank you for your compliance.\n<br>" +
                                        "\n<br>" +
                                        "\n<br>" +
                                        "Employee Name:    " + loRS.getString("xEmployNm") + "\n<br>" +
                                        "Reporting Branch: " + loRS.getString("sBranchNm"));
                        }
                        
                        msginfo.setFrom("No Reply <no-reply@guanzongroup.com.ph>");
                        mail.sendMessage(msginfo);
                        
                        lsSQL = "UPDATE Employee_PEF_Alert SET" +
                                    "  cMailSent = '1'" +
                                    ", dMailSent = " + SQLUtil.toSQL(_app.getServerDate())+
                                " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                        
                        if (_app.executeQuery(lsSQL, "Employee_PEF_Alert", _app.getBranchCode(), "") <= 0){
                            if (_app == null){
                                loJSON = new JSONObject();
                                loJSON.put("result", "error");
                                loJSON.put("message", "Unable to update notification...");
                                return loJSON;
                            }
                        }
                    }
                }

            } catch (MessagingException | IOException | SQLException ex) {
                    loJSON = new JSONObject();
                    loJSON.put("result", "error");
                    loJSON.put("message", ex.getMessage());
                    return loJSON;
            }
        }
        
        loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Notifications sent...");
        return loJSON;
    }
    
    private String getSuffix(int fnValue){
        String lsValue = String.valueOf(fnValue);
        
        lsValue = StringUtils.right(lsValue, 1);
        
        switch (lsValue){
            case "0":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
                return "th";
            case "1":
                return "st";
            case "2":
                return "nd";
            case "3":
                return "rd";
            default:
                return "";
        }
    }
    
    private String getAreaMail(String fsBranchCd) throws SQLException{
        String lsSQL = "SELECT" +
                            "  IFNULL(c.sAreaMail, '') sAreaMail" +
                        " FROM Branch a" +
                            ", Branch_Others b" +
                                " LEFT JOIN Branch_Area c ON b.sAreaCode = c.sAreaCode" +
                        " WHERE a.sBranchCd = b.sBranchCd" +
                            " AND a.sBranchCd = " + SQLUtil.toSQL(fsBranchCd);
        
        ResultSet loRS = _app.executeQuery(lsSQL);
        
        if (loRS.next())
            return loRS.getString("sAreaMail");
        else
            return ""; 
    }
    
    private String getManagerID(String fsMainOffc, String fsBranchCd, String fsDeptIDxx) throws SQLException{
        String lsSQL = "SELECT" +
                            "  sEmployID" +
                        " FROM Employee_Master001";
        
        if (fsMainOffc.equals("1")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sDeptIDxx = " + SQLUtil.toSQL(fsDeptIDxx) + 
                                                        " AND sEmpLevID = '2'");
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sBranchCd = " + SQLUtil.toSQL(fsBranchCd) +
                                                        " AND sEmpLevID = '3'");
//                                                        " AND (sEmpLevID = '3' OR" +
//                                                            " sPositnID IN ('031', '105', '009', '310'))");
        }
        
        lsSQL = MiscUtil.addCondition(lsSQL, "cRecdStat = '1' AND dFiredxxx IS NULL");
        lsSQL += " ORDER BY sEmpLevID DESC LIMIT 1";
    
        ResultSet loRS = _app.executeQuery(lsSQL);
        
        if (loRS.next())
            return loRS.getString("sEmployID");
        else
            return ""; 
    }
    
    private String getSQ_2Send(){
        return "SELECT" +
                    "  a.sTransNox" +
                    ", b.sCompnyNm xEmployNm" +
                    ", d.sBranchNm" +
                    ", a.nNoMonths" +
                    ", IFNULL(c.sFrstName, '') xMangerNm" +
                    ", IFNULL(a.sEmailAd1, '') sEmailAd1" +
                    ", IFNULL(a.sEmailAd2, '') sEmailAd2" +
                    ", IFNULL(a.sEmailAd3, '') sEmailAd3" +
                " FROM Employee_PEF_Alert a" +
                    " LEFT JOIN Client_Master b ON a.sEmployID = b.sClientID" +
                    " LEFT JOIN Client_Master c ON a.sMangerID = c.sClientID" +
                    " LEFT JOIN Branch d ON a.sBranchCd = d.sBranchCd" +
                " WHERE a.cMailSent = '0'";
    }
    
    private String getSQ_EvalByMonth(){
        return "SELECT" +
                    "  g.sAreaDesc" +
                    ", c.sBranchNm" +
                    ", c.cMainOffc" +
                    ", h.sDeptName" +
                    ", i.sPositnNm" +
                    ", b.sCompnyNm" +
                    ", ROUND(DATEDIFF(NOW(), b.dBirthDte) /365) xAgexxxxx" +
                    ", IF(b.cGenderCd='0','MALE', 'FEMALE') cGenderCd" +
                    ", TRIM(CONCAT(b.sHouseNox, ' ', b.sAddressx, ', ', d.sTownName, ' ', e.sProvName)) xAddressx" +
                    ", d.sTownName" +
                    ", e.sProvName" +
                    ", b.sMobileNo" +
                    ", a.dStartEmp" +
                    ", a.dHiredxxx" +
                    ", a.nSalaryxx" +
                    ", k.sCompnyCd" +
                    ", a.sEmployID" +
                    ", a.sBranchCd" +
                    ", a.sDeptIDxx" +
                    ", a.sEmpLevID" +
                    ", a.cEmpTypex" +
                    ", g.sAreaCode" +
                    ", a.sBnkActNo" +
                    ", l.sBankCode" +
                    ", DATEDIFF(CURDATE(), a.dHiredxxx) xDayAgexx" +
                    ", TIMESTAMPDIFF(MONTH, a.dHiredxxx, CURDATE()) xMonthAge" +
                    ", IFNULL(c.sEmailAdd, '') sBranchMl" +
                    ", IFNULL(h.sEmailAdd, '') sDeptMail" +
                    ", m.sTransNox" +
                " FROM Employee_Master001 a" +
                        " LEFT JOIN Branch c" +
                            " ON a.sBranchCd = c.sBranchCd" +
                        " LEFT JOIN Branch_Others f" +
                            " ON c.sBranchCd = f.sBranchCD" +
                        " LEFT JOIN Branch_Area g" +
                            " ON f.sAreaCode = g.sAreaCode" +
                        " LEFT JOIN Department h" +
                            " ON a.sDeptIDxx = h.sDeptIDxx" +
                        " LEFT JOIN Position i" +
                            " ON a.sPositnID= i.sPositnID" +
                        " LEFT JOIN Branch j" +
                            " ON a.sSgBranch = j.sBranchCd" +
                        " LEFT JOIN Company k" +
                            " ON j.sCompnyID = k.sCompnyID" +
                        " LEFT JOIN Banks l" +
                            " ON a.sBankIDxx = l.sBankIDxx" +
                        " LEFT JOIN Employee_PEF_Alert m" +
                            " ON a.sEmployID = m.sEmployID" +
                                " AND m.cEvalType = " + SQLUtil.toSQL(_evaltype) +
                                " AND m.nNoMonths = " + (_months + 1) +
                    ", Client_Master b" +
                        " LEFT JOIN TownCity d" +
                            " ON b.sTownIDxx = d.sTownIDxx" +
                        " LEFT JOIN Province e" +
                            " ON d.sProvIDxx = e.sProvIDxx" +
                " WHERE a.sEmployID = b.sClientID" +
                    " AND (a.sBranchCd LIKE 'M%' OR a.sBranchCd LIKE 'C%')" +
                    " AND a.cEmpTypex = " + SQLUtil.toSQL(_emptype) +
                    " AND TIMESTAMPDIFF(MONTH, a.dHiredxxx, CURDATE()) = " + _months +
                    " AND a.dFiredxxx IS NULL" +
                    " AND a.cRecdStat = '1'" +
                " HAVING m.sTransNox IS NULL" +
                " ORDER BY DATEDIFF(CURDATE(), a.dHiredxxx), g.sAreaDesc, c.sBranchNm, h.sDeptName, i.sPositnNm, b.sCompnyNm";
    }
}

