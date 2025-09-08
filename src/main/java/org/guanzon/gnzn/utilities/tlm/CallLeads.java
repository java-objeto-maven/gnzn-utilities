package org.guanzon.gnzn.utilities.tlm;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

public class CallLeads {
    enum Type{
        NO_SCHED,
        NO_SCHED_BACKDATE,
        WITH_TARGET_ONLY,
        WITH_TARGET_AGENT,
        WITH_TARGET_AGENT_BACKDATE,
        WITH_TARGET_BACKDATE,
        WITH_FOLLOW_UP_ONLY_NO_AGENT
    }
    
    GRider _instance;
    String _network;
    
    int _fu_backdate;
    int _ca_grace_period;
    int _leads_inquiry_first_priority;
    int _leads_inquiry_grace_period;
    
    public CallLeads(GRider foValue){
        _instance = foValue;
        _network = "";
        
        _fu_backdate = Integer.parseInt(System.getProperty("leads.follow.up.backdate"));
        _ca_grace_period = Integer.parseInt(System.getProperty("leads.ca.grace.period"));
        _leads_inquiry_first_priority = Integer.parseInt(System.getProperty("leads.inquiry.first"));
        _leads_inquiry_grace_period = Integer.parseInt(System.getProperty("leads.inquiry.grace.period"));
    }
    
    public void setNetworkProvider(String lsValue){
        _network = lsValue;
    }
    
    public boolean GetNextCustomer() throws SQLException{
        //get mc product inquiry (Inquiries)
        String lsSQL;
        ResultSet loRS;
        
        //for follow-up(lastest)
        lsSQL = getSQ_CustomersToFollowUp();
        loRS = _instance.executeQuery(lsSQL);
        
        if(loRS.next()) if (addToLeads(loRS)) return true;
        
        //approved credit applications
        lsSQL = getSQ_CreditApplications();
        loRS = _instance.executeQuery(lsSQL);
        
        if(loRS.next()) if (addToLeads(loRS)) return true;

        //product inquiries with target and agent
        lsSQL = getSQ_ProductInquiries(Type.WITH_TARGET_AGENT);
        loRS = _instance.executeQuery(lsSQL);
        
        if(loRS.next()) if (addToLeads(loRS)) return true;
        
        //product inquiries with target
        lsSQL = getSQ_ProductInquiries(Type.WITH_TARGET_ONLY);
        loRS = _instance.executeQuery(lsSQL);
        
        if(loRS.next()) if (addToLeads(loRS)) return true;
        
        //product inquiries with target and agent backdate
        lsSQL = getSQ_ProductInquiries(Type.WITH_TARGET_AGENT_BACKDATE);
        loRS = _instance.executeQuery(lsSQL);
        
        if(loRS.next()) if (addToLeads(loRS)) return true;
        
        //product inquiries with follow up but no agent assigned(can be the old entries)
        lsSQL = getSQ_ProductInquiries(Type.WITH_FOLLOW_UP_ONLY_NO_AGENT);
        loRS = _instance.executeQuery(lsSQL);
        
        if(loRS.next()) if (addToLeads(loRS)) return true;
        
        //product inquiries with no target and no follow up
        lsSQL = getSQ_ProductInquiries(Type.NO_SCHED);
        loRS = _instance.executeQuery(lsSQL);
        
        if(loRS.next()) if (addToLeads(loRS)) return true;
        
        //for follow-up(backdate)
        lsSQL = getSQ_CustomersToFollowUpBackDate();
        loRS = _instance.executeQuery(lsSQL);
        
        if(loRS.next()) if (addToLeads(loRS)) return true;
        
        //product inquiries with target backdate
        lsSQL = getSQ_ProductInquiries(Type.WITH_TARGET_BACKDATE);
        loRS = _instance.executeQuery(lsSQL);
        
        if(loRS.next()) if (addToLeads(loRS)) return true;
        
        //product inquiries with no target and no follow up backdate
        lsSQL = getSQ_ProductInquiries(Type.NO_SCHED_BACKDATE);
        loRS = _instance.executeQuery(lsSQL);
        
        if(loRS.next()) if (addToLeads(loRS)) return true;
        
        return false;
    }
    
    
    private boolean addToLeads(ResultSet foRS) throws SQLException{
        String lsSQL;
        String lsSourceCd;
        String lsSQLUpdate;
        String lsNetwork;
        
        switch (foRS.getString("sTableNme")){
            case "MC_Product_Inquiry":
                lsSourceCd = "INQR";
                
                lsSQLUpdate = "UPDATE MC_Product_Inquiry SET" + 
                                    "  cTranStat = '1'" + 
                                    ", sCreatedx = " + SQLUtil.toSQL(_instance.getUserID()) +
                                " WHERE sTransNox = " + SQLUtil.toSQL(foRS.getString("sTransNox"));
                break;
            case "MC_Credit_Application":
                lsSourceCd = "MCCA";
                
                lsSQLUpdate = "UPDATE MC_Credit_Application SET" + 
                                    "  cTLMStatx = '1'" + 
                                    ", sAgentIDx = " + SQLUtil.toSQL(_instance.getUserID()) +
                                " WHERE sTransNox = " + SQLUtil.toSQL(foRS.getString("sTransNox"));
                break;
            case "MC_Referral":
                lsSourceCd = "RFRL";
                
                lsSQLUpdate = "UPDATE MC_Referral SET" + 
                                    "  cTranStat = '1'" + 
                                    ", sAgentIDx = " + SQLUtil.toSQL(_instance.getUserID()) +
                                " WHERE sTransNox = " + SQLUtil.toSQL(foRS.getString("sTransNox"));
                break;
            case "TLM_Client":
                lsSourceCd = "TLMC";
                
                lsSQLUpdate = "UPDATE TLM_Client SET" + 
                                    "  cTranStat = '1'" + 
                                    ", sAgentIDx = " + SQLUtil.toSQL(_instance.getUserID()) +
                                " WHERE sTransNox = " + SQLUtil.toSQL(foRS.getString("sTransNox"));
                break;
            default:
                System.err.println("Source table is not registered...");
                return false;
        }
        
        lsNetwork = CommonUtils.classifyNetwork(foRS.getString("sMobileNo"));
        
        if (!lsNetwork.equals(foRS.getString("cSubscrbr"))) {
            //fix the assigned network on Client Mobile for data integrity first
            lsSQL = "UPDATE Client_Mobile SET" +
                        "  cSubscrbr = " + SQLUtil.toSQL(lsNetwork) + 
                    "WHERE sClientID = " + SQLUtil.toSQL(foRS.getString("sClientID")) +
                        " AND sMobileNo = " + SQLUtil.toSQL(foRS.getString("sMobileNo"));
            
            if (_instance.executeQuery(lsSQL, "Client_Mobile", _instance.getBranchCode(), "") <= 0){
                System.err.println("Unable to execute sql.");
                System.exit(2);
            }
            
            return false;
        } else {
            _instance.beginTrans();
            
            lsSQL = "INSERT INTO Call_Outgoing SET" +
                    "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("Call_Outgoing", "sTransNox", true, _instance.getConnection(), _instance.getBranchCode())) +
                    ", dTransact = " + SQLUtil.toSQL(_instance.getServerDate()) +
                    ", sClientID = " + SQLUtil.toSQL(foRS.getString("sClientID")) +
                    ", sMobileNo = " + SQLUtil.toSQL(foRS.getString("sMobileNo")) +
                    ", sRemarksx = ''" + 
                    ", sReferNox = " + SQLUtil.toSQL(foRS.getString("sTransNox")) +
                    ", sSourceCD = " + SQLUtil.toSQL(lsSourceCd) +
                    ", cTranStat = '1'" + 
                    ", sAgentIDx = " + SQLUtil.toSQL(_instance.getUserID()) +
                    ", nNoRetryx = 0" +
                    ", cSubscrbr = " + SQLUtil.toSQL(lsNetwork) +
                    ", cCallStat = '0'" +
                    ", cTLMStatx = '0'" +
                    ", cSMSStatx = '0'" + 
                    ", nSMSSentx = 0" + 
                    ", sModified = " + SQLUtil.toSQL(_instance.getUserID()) +
                    ", dModified = " + SQLUtil.toSQL(_instance.getServerDate());
            
            if (_instance.executeQuery(lsSQL, "Call_Outgoing", _instance.getBranchCode(), "") <= 0){
                System.err.println("Unable to execute sql.");
                _instance.rollbackTrans();
                return false;
            }
            
            if (_instance.executeQuery(lsSQLUpdate, foRS.getString("sTableNme"), _instance.getBranchCode(), "") <= 0){
                System.err.println("Unable to execute sql.");
                _instance.rollbackTrans();
                return false;
            }
            
            _instance.commitTrans();
        }
        
        return true;
    }
    
    private String getSQ_ProductInquiries(Type loType){
        switch (loType){
            case WITH_TARGET_ONLY:
                return "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.dTargetxx" +
                    ", a.sTransNox" +
                    ", 'MC_Product_Inquiry' sTableNme" +
                    ", a.dTransact" +
                    ", IFNULL(a.sCreatedx, '') sCreatedx" +
                    ", c.cSubscrbr" + 
                " FROM MC_Product_Inquiry a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND a.cTranStat = '0'" + 
                    " AND DATE_ADD(a.dTargetxx, INTERVAL - 2 DAY) <= CURRENT_DATE()" +
                    " AND dTargetxx >= " + SQLUtil.toSQL(CommonUtils.dateAdd(_instance.getServerDate(), _fu_backdate)) +
                    " AND a.dFollowUp IS NULL" + 
                    " AND a.cTranStat = '0'" +
                    (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                " ORDER BY dTargetxx DESC" +
                " LIMIT 1";
            case WITH_TARGET_AGENT:
                return "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.dTargetxx" +
                    ", a.sTransNox" +
                    ", 'MC_Product_Inquiry' sTableNme" +
                    ", a.dTransact" +
                    ", IFNULL(a.sCreatedx, '') sCreatedx" +
                    ", c.cSubscrbr" + 
                " FROM MC_Product_Inquiry a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND a.cTranStat = '0'" + 
                    " AND DATE_ADD(a.dTargetxx, INTERVAL - 2 DAY) <= CURRENT_DATE()" +
                    " AND dTargetxx >= " + SQLUtil.toSQL(CommonUtils.dateAdd(_instance.getServerDate(), _fu_backdate)) +
                    " AND a.sCreatedx = " + SQLUtil.toSQL(_instance.getUserID()) +
                    " AND a.dFollowUp IS NULL" + 
                    " AND a.cTranStat = '0'" +
                    (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                " ORDER BY dTargetxx DESC" +
                " LIMIT 1";
            case WITH_TARGET_BACKDATE:
                return "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.dTargetxx" +
                    ", a.sTransNox" +
                    ", 'MC_Product_Inquiry' sTableNme" +
                    ", a.dTransact" +
                    ", IFNULL(a.sCreatedx, '') sCreatedx" +
                    ", c.cSubscrbr" + 
                " FROM MC_Product_Inquiry a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND a.cTranStat = '0'" + 
                    " AND a.dTargetxx >= '2024-01-01 00:00:01'" +
                    " AND dTargetxx < " + SQLUtil.toSQL(CommonUtils.dateAdd(_instance.getServerDate(), _fu_backdate)) +
                    " AND a.dFollowUp IS NULL" + 
                    " AND a.cTranStat = '0'" +
                    (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                " ORDER BY dTargetxx DESC" +
                " LIMIT 1";
            case WITH_TARGET_AGENT_BACKDATE:
                return "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.dTargetxx" +
                    ", a.sTransNox" +
                    ", 'MC_Product_Inquiry' sTableNme" +
                    ", a.dTransact" +
                    ", IFNULL(a.sCreatedx, '') sCreatedx" +
                    ", c.cSubscrbr" + 
                " FROM MC_Product_Inquiry a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND a.cTranStat = '0'" + 
                    " AND a.dTargetxx >= '2024-01-01 00:00:01'" +
                    " AND dTargetxx < " + SQLUtil.toSQL(CommonUtils.dateAdd(_instance.getServerDate(), _fu_backdate)) +
                    " AND a.sCreatedx = " + SQLUtil.toSQL(_instance.getUserID()) +
                    " AND a.dFollowUp IS NULL" + 
                    " AND a.cTranStat = '0'" +
                    (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                " ORDER BY dTargetxx DESC" +
                " LIMIT 1";
            case WITH_FOLLOW_UP_ONLY_NO_AGENT:
                return "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.dTargetxx" +
                    ", a.sTransNox" +
                    ", 'MC_Product_Inquiry' sTableNme" +
                    ", a.dTransact" +
                    ", IFNULL(a.sCreatedx, '') sCreatedx" +
                    ", c.cSubscrbr" + 
                " FROM MC_Product_Inquiry a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND a.cTranStat = '0'" + 
                    " AND a.dFollowUp <= CURRENT_TIMESTAMP()" +
                    " AND IFNULL(a.sCreatedx, '') = ''" +
                    (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                " ORDER BY dFollowUp DESC" +
                " LIMIT 1";
            case NO_SCHED:
                return "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.dTargetxx" +
                    ", a.sTransNox" +
                    ", 'MC_Product_Inquiry' sTableNme" +
                    ", a.dTransact" +
                    ", IFNULL(a.sCreatedx, '') sCreatedx" +
                    ", c.cSubscrbr" + 
                " FROM MC_Product_Inquiry a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND a.cTranStat = '0'" + 
                    " AND a.dTargetxx IS NULL" +
                    " AND a.dFollowUp IS NULL" + 
                    " AND a.dTransact BETWEEN " + SQLUtil.toSQL(CommonUtils.dateAdd(_instance.getServerDate(), _leads_inquiry_first_priority)) +
                        " AND " + SQLUtil.toSQL(CommonUtils.dateAdd(_instance.getServerDate(), _leads_inquiry_grace_period)) +
                    (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                " ORDER BY dTransact DESC" +
                " LIMIT 1";
            case NO_SCHED_BACKDATE:
                return "SELECT" + 
                        "  b.sMobileNo" +
                        ", a.sClientID" +
                        ", a.dFollowUp" +
                        ", a.dTargetxx" +
                        ", a.sTransNox" +
                        ", 'MC_Product_Inquiry' sTableNme" +
                        ", a.dTransact" +
                        ", IFNULL(a.sCreatedx, '') sCreatedx" +
                        ", c.cSubscrbr" + 
                    " FROM MC_Product_Inquiry a" +
                        ", Client_Master b" +
                        ", Client_Mobile c" +
                    " WHERE a.sClientID = b.sClientID" +
                        " AND b.sClientID = c.sClientID" +
                        " AND b.sMobileNo = c.sMobileNo" +
                        " AND a.cTranStat = '0'" + 
                        " AND a.dTargetxx IS NULL" +
                        " AND a.dFollowUp IS NULL" + 
                        " AND a.dTransact >= '2024-01-01 00:00:01'" +
                        " AND a.dTransact < " + SQLUtil.toSQL(CommonUtils.dateAdd(_instance.getServerDate(), _leads_inquiry_first_priority)) +
                        (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                    " ORDER BY dTransact DESC" +
                    " LIMIT 1";
            default:
                return "";
        }
    }
    
    private String getSQ_CreditApplications(){
        return "SELECT" +
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.sTransNox" +
                    ", 'MC_Credit_Application' sTableNme" +
                    ", a.dAppliedx dTransact" +
                    ", IFNULL(a.sTLMAgent, '') sCreatedx" +
                    ", c.cSubscrbr" + 
                " FROM MC_Credit_Application a" +
                    " LEFT JOIN Client_Master b" +
                        " ON a.sClientID = b.sClientID" +
                    " LEFT JOIN Client_Mobile c" +
                        " ON b.sClientID = c.sClientID AND b.sMobileNo = c.sMobileNo" +
                " WHERE a.sTransNox LIKE 'M%'" +
                    " AND a.cTranStat = '2'" +
                    " AND a.cTLMStatx = '0'" +
                    " AND LEFT(a.sQMatchNo, 2) = 'CI'" +
                    " AND a.dAppliedx >= DATE_SUB(NOW(), INTERVAL 1 MONTH)" +
                    " AND a.dAppliedx <= " + SQLUtil.toSQL(MiscUtil.dateAdd(_instance.getServerDate(), _ca_grace_period)) + 
                    " AND a.dFollowUp IS NULL" +
                    (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                " ORDER BY a.dAppliedx" +
                " LIMIT 1";
    }
    
    private String getSQ_CustomersToFollowUp(){
        return "SELECT * FROM (" +
                    "SELECT" +
                        "  b.sMobileNo" +
                        ", a.sClientID" +
                        ", a.dFollowUp" +
                        ", a.sTransNox" +
                        ", 'MC_Product_Inquiry' sTableNme" +
                        ", a.dTransact" +
                        ", IFNULL(a.sCreatedx, '') sCreatedx" +
                        ", c.cSubscrbr" + 
                    " FROM MC_Product_Inquiry a" +
                        ", Client_Master b" +
                            " LEFT JOIN Client_Mobile c" +
                                " ON b.sClientID = c.sClientID AND b.sMobileNo = c.sMobileNo" +
                    " WHERE a.sClientID = b.sClientID" +
                        " AND a.dFollowUp IS NOT NULL" +
                        " AND a.cTranStat = '0'" +
                        " AND a.sCreatedx = " + SQLUtil.toSQL(_instance.getUserID()) +
                        (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                    " UNION" +
                    " SELECT" +
                        "  b.sMobileNo" +
                        ", a.sClientID" +
                        ", a.dFollowUp" +
                        ", a.sTransNox" +
                        ", 'MC_Credit_Application' sTableNme" +
                        ", a.dAppliedx dTransact" +
                        ", IFNULL(a.sTLMAgent, '') sCreatedx" +
                        ", c.cSubscrbr" + 
                    " FROM MC_Credit_Application a" +
                        ", Client_Master b" +
                            " LEFT JOIN Client_Mobile c" +
                                " ON b.sClientID = c.sClientID AND b.sMobileNo = c.sMobileNo" +
                    " WHERE a.sClientID = b.sClientID" +
                        " AND a.dFollowUp IS NOT NULL" +
                        " AND a.cTLMStatx = '0'" +
                        " AND a.sTLMAgent = " + SQLUtil.toSQL(_instance.getUserID()) +
                        (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                    " UNION" +
                    " SELECT" +
                        "  b.sMobileNo" +
                        ", a.sClientID" +
                        ", a.dFollowUp" +
                        ", a.sTransNox" +
                        ", 'MC_Referral' sTableNme" +
                        ", a.dTransact" +
                        ", IFNULL(a.sAgentIDx, '') sCreatedx" +
                        ", c.cSubscrbr" + 
                    " FROM MC_Referral a" +
                        ", Client_Master b" +
                            " LEFT JOIN Client_Mobile c" +
                                " ON b.sClientID = c.sClientID AND b.sMobileNo = c.sMobileNo" +
                    " WHERE a.sClientID = b.sClientID" +
                        " AND a.dFollowUp IS NOT NULL" +
                        " AND a.cTranStat = '0'" +
                        " AND a.sAgentIDx = " + SQLUtil.toSQL(_instance.getUserID()) +
                        (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                    " UNION" +
                    " SELECT" +
                        "  b.sMobileNo" +
                        ", a.sClientID" +
                        ", a.dFollowUp" +
                        ", a.sClientID sTransNox" +
                        ", 'TLM_Client' sTableNme" +
                        ", '1900-01-01' dTransact" +
                        ", IFNULL(a.sAgentIDx, '') sCreatedx" +
                        ", c.cSubscrbr" + 
                    " FROM TLM_Client a" +
                        ", Client_Master b" +
                            " LEFT JOIN Client_Mobile c" +
                                " ON b.sClientID = c.sClientID AND b.sMobileNo = c.sMobileNo" +
                    " WHERE a.sClientID = b.sClientID" +
                        " AND a.dFollowUp IS NOT NULL" +
                        " AND a.cTranStat = '0'" +
                        (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                        " AND a.sAgentIDx  = " + SQLUtil.toSQL(_instance.getUserID()) + ") a" +                        
                " WHERE dFollowUp >= " + SQLUtil.toSQL(CommonUtils.dateAdd(_instance.getServerDate(), _fu_backdate)) +
                    " AND dFollowUp <= CURRENT_TIMESTAMP()" + 
                " ORDER BY dFollowUp" +
                " LIMIT 1";
    }
    
    private String getSQ_CustomersToFollowUpBackDate(){
        return "SELECT * FROM (" +
                    "SELECT" +
                        "  b.sMobileNo" +
                        ", a.sClientID" +
                        ", a.dFollowUp" +
                        ", a.sTransNox" +
                        ", 'MC_Product_Inquiry' sTableNme" +
                        ", a.dTransact" +
                        ", IFNULL(a.sCreatedx, '') sCreatedx" +
                        ", c.cSubscrbr" + 
                    " FROM MC_Product_Inquiry a" +
                        ", Client_Master b" +
                            " LEFT JOIN Client_Mobile c" +
                                " ON b.sClientID = c.sClientID AND b.sMobileNo = c.sMobileNo" +
                    " WHERE a.sClientID = b.sClientID" +
                        " AND a.dFollowUp IS NOT NULL" +
                        " AND a.cTranStat = '0'" +
                        " AND a.sCreatedx = " + SQLUtil.toSQL(_instance.getUserID()) +
                        (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                    " UNION" +
                    " SELECT" +
                        "  b.sMobileNo" +
                        ", a.sClientID" +
                        ", a.dFollowUp" +
                        ", a.sTransNox" +
                        ", 'MC_Credit_Application' sTableNme" +
                        ", a.dAppliedx dTransact" +
                        ", IFNULL(a.sTLMAgent, '') sCreatedx" +
                        ", c.cSubscrbr" + 
                    " FROM MC_Credit_Application a" +
                        ", Client_Master b" +
                            " LEFT JOIN Client_Mobile c" +
                                " ON b.sClientID = c.sClientID AND b.sMobileNo = c.sMobileNo" +
                    " WHERE a.sClientID = b.sClientID" +
                        " AND a.dFollowUp IS NOT NULL" +
                        " AND a.cTLMStatx = '0'" +
                        " AND a.sTLMAgent = " + SQLUtil.toSQL(_instance.getUserID()) +
                        (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                    " UNION" +
                    " SELECT" +
                        "  b.sMobileNo" +
                        ", a.sClientID" +
                        ", a.dFollowUp" +
                        ", a.sTransNox" +
                        ", 'MC_Referral' sTableNme" +
                        ", a.dTransact" +
                        ", IFNULL(a.sAgentIDx, '') sCreatedx" +
                        ", c.cSubscrbr" + 
                    " FROM MC_Referral a" +
                        ", Client_Master b" +
                            " LEFT JOIN Client_Mobile c" +
                                " ON b.sClientID = c.sClientID AND b.sMobileNo = c.sMobileNo" +
                    " WHERE a.sClientID = b.sClientID" +
                        " AND a.dFollowUp IS NOT NULL" +
                        " AND a.cTranStat = '0'" +
                        " AND a.sAgentIDx = " + SQLUtil.toSQL(_instance.getUserID()) +
                        (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                    " UNION" +
                    " SELECT" +
                        "  b.sMobileNo" +
                        ", a.sClientID" +
                        ", a.dFollowUp" +
                        ", a.sClientID sTransNox" +
                        ", 'TLM_Client' sTableNme" +
                        ", '1900-01-01' dTransact" +
                        ", IFNULL(a.sAgentIDx, '') sCreatedx" +
                        ", c.cSubscrbr" + 
                    " FROM TLM_Client a" +
                        ", Client_Master b" +
                            " LEFT JOIN Client_Mobile c" +
                                " ON b.sClientID = c.sClientID AND b.sMobileNo = c.sMobileNo" +
                    " WHERE a.sClientID = b.sClientID" +
                        " AND a.dFollowUp IS NOT NULL" +
                        " AND a.cTranStat = '0'" +
                        (_network.equals("3") ? "" : " AND c.cSubscrbr = " + SQLUtil.toSQL(_network)) +
                        " AND a.sAgentIDx  = " + SQLUtil.toSQL(_instance.getUserID()) + ") a" +                        
                " WHERE dFollowUp >= '2024-01-0 00:00:01'" +
                    " AND dFollowUp <= " + SQLUtil.toSQL(CommonUtils.dateAdd(_instance.getServerDate(), _fu_backdate)) +
                " ORDER BY dFollowUp DESC" +
                " LIMIT 1";
    }
}