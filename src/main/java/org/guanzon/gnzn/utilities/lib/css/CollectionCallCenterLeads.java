package org.guanzon.gnzn.utilities.lib.css;

import com.sun.org.apache.bcel.internal.generic.LOR;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;

public class CollectionCallCenterLeads {
    final int REFILL = 30;
    
    GRider _instance;
    JSONObject _json;
    
    String _period_from;
    String _period_thru;
    
    public CollectionCallCenterLeads(GRider foValue){
        _instance = foValue;
    }
    
    public JSONObject Create() throws SQLException{
        _json = new JSONObject();
                
        //get to leads to fill
        int ln2Fill = get2Fill();
        
        if (ln2Fill > 0){
            int lnFilld = 0;
            
            if (!checkPeriod()){
                _json.put("result", "error");
                _json.put("message", "Unable to initialize period.");
                return _json;
            }

            String lsSQL = getSQ_ForCollection();

            ResultSet loRS = _instance.executeQuery(lsSQL);
            
            while(loRS.next()){
                if (ln2Fill > lnFilld){
                    //check if unpaid
                    if (!isPaid(loRS.getString("sAcctNmbr"))){
                        //check if already added to leads
                        if (!isLRCallEncoded(loRS.getString("sAcctNmbr"))){
                            //add to leads
                            if (!addToLeads(loRS.getString("sAcctNmbr"), loRS.getString("sBranchCd"))){
                                _json.put("result", "error");
                                _json.put("message", "Unable to add account to leads.");
                                return _json;
                            }
                            
                            System.out.println(loRS.getString("sAcctNmbr") + " added to leads.");
                            lnFilld++;
                        }
                    }
                }
            }
        }
        
        _json.put("result", "success");
        return _json;
    }
    
    private boolean checkPeriod() throws SQLException{
        Calendar cal = Calendar.getInstance();

        // Date From: 3rd day of current month
        cal.set(Calendar.DAY_OF_MONTH, 3);
        Date dateFrom = cal.getTime();

        // Date Thru: 2nd day of next month
        cal.add(Calendar.MONTH, 1); // move to next month
        cal.set(Calendar.DAY_OF_MONTH, 2);
        Date dateThru = cal.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        _period_from = sdf.format(dateFrom);
        _period_thru = sdf.format(dateThru);

        String lsSQL = "SELECT" + 
                            "  STR_TO_DATE(sCollFrom, '%Y%m%d') sCollFrom" +
                            ", STR_TO_DATE(sCollThru, '%Y%m%d') sCollThru" +
                        " FROM Collection_Period" +
                        " WHERE cRecdStat = '1'" +
                        " ORDER BY sPeriodID" + 
                        " DESC LIMIT 1";
        
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        if (loRS.next()){
            //collection thru is now less than the current date
            if (SQLUtil.toDate(loRS.getString("sCollThru"), SQLUtil.FORMAT_SHORT_DATE).before(Calendar.getInstance().getTime())){
                //create new period
                lsSQL = "INSERT INTO Collection_Period SET" +
                        "  sPeriodID = " + SQLUtil.toSQL(_period_from.replace("-", "")).substring(0, 6) + //202602
                        ", sCollFrom = " + SQLUtil.toSQL(_period_from.replace("-", "")) +
                        ", sCollThru = " + SQLUtil.toSQL(_period_thru.replace("-", "")) +
                        ", cRecdStat = '1'" +
                        ", sModified = " + SQLUtil.toSQL(_instance.getUserID()) +
                        ", dModified = " + SQLUtil.toSQL(_instance.getServerDate());
                
                System.out.println(lsSQL);
                if (_instance.executeQuery(lsSQL, "Collection_Period", _instance.getBranchCode(), "") <= 0){
                    System.err.println("Unable to create new collection period.");
                    return false;
                }
            } else {
                _period_from = loRS.getString("sCollFrom");
                _period_thru = loRS.getString("sCollThru");
            }
            
            return true;   
        }
        
        return false;
    }
    
    public boolean addToLeads(String accountNo, String branchCode){
        _instance.beginTrans();
        
        //remove for collection unit
        String lsSQL = "DELETE FROM LR_Collection_Unit WHERE sAcctNmbr = " + SQLUtil.toSQL(accountNo);
        _instance.executeUpdate(lsSQL);
        
        //add new for collection unit
        lsSQL = "INSERT INTO LR_Collection_Unit SET" +
                                "  sAcctNmbr = " + SQLUtil.toSQL(accountNo) +
                                ", sBranchCd = " + SQLUtil.toSQL(branchCode) +
                                ", dTransact = " + SQLUtil.toSQL(_instance.getServerDate()) +
                                ", cCollUnit = '0'" +
                                ", sInCharge = " + SQLUtil.toSQL("") +
                                ", cApntUnit = " + SQLUtil.toSQL("") +
                                ", dDueUntil = " + SQLUtil.toSQL(CommonUtils.dateAdd(_instance.getServerDate(), 5)) +
                                ", cCollStat = '0'" +
                                ", nPriority = 1" + 
                                ", nNoSchedx = 0" +
                                ", sModified = " + SQLUtil.toSQL(_instance.getUserID()) +
                                ", dModified = " + SQLUtil.toSQL(_instance.getServerDate());
        
        if (_instance.executeUpdate(lsSQL) <= 0){
            _instance.rollbackTrans();
            return false;
        }
        
        //add customer to leads
        lsSQL = "INSERT INTO LR_Calls_Master SET" +
                "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("LR_Calls_Master", "sTransNox", true, _instance.getConnection(), _instance.getBranchCode())) +
                ", dTransact = " + SQLUtil.toSQL(_instance.getServerDate()) +
                ", sAcctNmbr = " + SQLUtil.toSQL(accountNo) +
                ", sRemarksx = ''" +
                ", cTranStat = '0'" +
                ", sAsgAgent = ''" +
                ", sAgentIDx = ''" +
                ", sModified = " + SQLUtil.toSQL(_instance.getUserID()) +
                ", dModified = " + SQLUtil.toSQL(_instance.getServerDate());

        if (_instance.executeUpdate(lsSQL) <= 0){
            _instance.rollbackTrans();
            return false;
        }

        //update for collection
        lsSQL = "UPDATE LR_Collection_Unit SET" +
                    "  dScheduld = " + SQLUtil.toSQL(_instance.getServerDate()) +
                    ", sInCharge = ''" +
                    ", cCollStat = '1'" + 
                " WHERE sAcctNmbr = " + SQLUtil.toSQL(accountNo) +
                    " AND cCollUnit = '0'";
        _instance.executeUpdate(lsSQL);
        _instance.commitTrans();
        
        return true;
    }
    
    public boolean isLRCallEncoded(String accountNo) throws SQLException{
        String lsSQL = "SELECT sTransNox" +
                        " FROM LR_Calls_Master" +
                        " WHERE sAcctNmbr = " + SQLUtil.toSQL(accountNo) +
                            " AND dTransact BETWEEN " + SQLUtil.toSQL(_period_from) + " AND " + SQLUtil.toSQL(_period_thru);
        
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        return loRS.next();
    }
    
    public boolean isPaid(String accountNo) throws SQLException{
        String lsSQL = MiscUtil.addCondition(getSQ_PeriodCollection(), "a.sAcctNmbr = " + SQLUtil.toSQL(accountNo));
        
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        return loRS.next();
    }
    
    public int get2Fill(){
        String lsSQL = "SELECT sTransNox FROM LR_Calls_Master" +
                        " WHERE dTransact >= '2023-07-22'" +
                            " AND cTranStat = '0'";
        
        System.out.println(lsSQL);
        return REFILL - (int) MiscUtil.RecordCount(_instance.executeQuery(lsSQL));
    }
    
    public String getSQ_ForCollection(){
        return "SELECT" +
                    " CONCAT(j.sFrstName, ' ', j.sLastName) xMasGroup," +
                    " a.sAcctNmbr," +
                    " CONCAT(" +
                    "   b.sLastName," +
                    "   ', '," +
                    "   b.sFrstName," +
                    "   ' '," +
                    "   LEFT(b.sMiddName, 1)" +
                    " ) AS xCustName," +
                    " b.sAddressx," +
                    " x.sBrgyName," +
                    " e.sProvName," +
                    " d.sTownName," +
                    " a.nGrossPrc," +
                    " a.nMonAmort," +
                    " a.nDownPaym," +
                    " a.nCashBalx," +
                    " a.dFirstPay," +
                    " a.dPurchase," +
                    " a.dLastPaym," +
                    " a.nLastPaym," +
                    " a.nDelayAvg," +
                    " a.nPaymTotl," +
                    " a.dDueDatex," +
                    " g.sModelNme," +
                    " a.nAcctTerm," +
                    " a.nDownTotl," +
                    " a.nCashTotl," +
                    " a.nCredTotl," +
                    " a.nDebtTotl," +
                    " a.nRebTotlx," +
                    " a.cRatingxx," +
                    " v.sAreaDesc," +
                    " z.sBranchNm," +
                    " w.sCompnyNm `sCollectx`," +
                    " h.sEngineNo," +
                    " a.sBranchCd" +
                " FROM" +
                    " MC_AR_Master a" + 
                    " LEFT JOIN MC_Serial h" + 
                      " ON a.sSerialID = h.sSerialID" + 
                    " LEFT JOIN MC_Model g" + 
                      " ON h.sModelIDx = g.sModelIDx" + 
                    " LEFT JOIN Employee_Master001 i" + 
                      " ON a.sClientID = i.sEmployID" + 
                      " AND (" +
                        " ISNULL(i.dFiredxxx)" + 
                        " OR i.dFiredxxx > '" + _period_from + " 00:00:00'" +
                      " )," +
                    " Client_Master b" + 
                    " LEFT JOIN Barangay x" +
                      " ON b.sBrgyIDxx = x.sBrgyIDxx," +
                    " Route_Area c" + 
                    " LEFT JOIN Branch z" + 
                      " ON c.sBranchCd = z.sBranchCd" + 
                    " LEFT JOIN Branch_Others y" +
                      " ON z.sBranchCd = y.sBranchCd" + 
                    " LEFT JOIN Branch_Area v" + 
                      " ON y.sAreaCode = v.sAreaCode" + 
                    " LEFT JOIN Client_Master w" + 
                      " ON c.sCollctID = w.sClientID," +
                    " TownCity d," +
                    " Province e," +
                    " Employee_Master001 f" + 
                    " LEFT JOIN Client_Master j" + 
                      " ON f.sEmployID = j.sClientID" + 
                " WHERE a.sClientID = b.sClientID" + 
                    " AND a.sRouteIDx = c.sRouteIDx" + 
                    " AND b.sTownIDxx = d.sTownIDxx" + 
                    " AND d.sProvIDxx = e.sProvIDxx" + 
                    " AND a.nAcctTerm > 0" + 
                    " AND f.sEmployID = c.sManagrID" + 
                    " AND a.dPurchase < '" + _period_from + " 00:00:00'" +
                    " AND (" +
                      " a.cAcctStat = '0'" + 
                      " OR (" +
                        " a.dClosedxx >= '" + _period_from + " 00:00:00'" +
                        " AND a.cAcctStat <> '0'" +
                      " )" +
                    " )" +
                    " AND ISNULL(i.sEmployID)" + 
                    " AND a.cMotorNew = '1'" + 
                    " AND a.cLoanType IN ('0', '1')" + 
                    " AND a.nDelayAvg BETWEEN 1.00 AND 2.00" +
                " ORDER BY nDelayAvg," +
                    " xMasGroup," +
                    " sProvName," +
                    " sTownName," +
                    " sBrgyName," +
                    " xCustName," +
                    " sAddressx";
    }
    
    public String getSQ_PeriodCollection(){
        return "SELECT" + 
                    " CONCAT(g.sFrstName, ' ', g.sLastName) AS xEmployNm," +
                    " a.dTransact," +
                    " a.sAcctNmbr," +
                    " CONCAT(" +
                    "   c.sLastName," +
                    "   ', '," +
                    "   c.sFrstName," +
                    "   ' '," +
                    "   LEFT(c.sMiddName, 1)" +
                    " ) AS xCustName," +
                    " b.nMonAmort," +
                    " a.sORNoxxxx," +
                    " a.nTranAmtx," +
                    " a.nRebatesx," +
                    " a.nABalance," +
                    " a.cTranType," +
                    " a.nEntryNox," +
                    " b.dPurchase," +
                    " 'GL' sSystemCd," +
                    " h.sBranchNm `sAssgndBr`," +
                    " k.sAreaDesc," +
                    " m.sBranchNm `sPymBrnch`" +
                " FROM" +
                    " MC_AR_Ledger a" +
                    " LEFT JOIN LR_Payment_Master f" + 
                      " ON a.sAcctNmbr = f.sAcctNmbr" + 
                      " AND a.sORNoxxxx = f.sReferNox" + 
                      " AND (" +
                      "  f.cPostedxx = '2'" + 
                      "  OR f.cPostedxx = '6'" +
                      ")" + 
                    " LEFT JOIN Branch m" + 
                      " ON a.sBranchCd = m.sBranchCd" + 
                    " LEFT JOIN Branch_Others j" + 
                      " ON m.sBranchCd = j.sBranchCd" + 
                    " LEFT JOIN Branch_Area k" + 
                      " ON j.sAreaCode = k.sAreaCode," +
                    " MC_AR_Master b" + 
                    " LEFT JOIN Employee_Master001 i" + 
                      " ON b.sClientID = i.sEmployID" + 
                      " AND (" +
                      "  ISNULL(i.dFiredxxx)" + 
                      "  OR i.dFiredxxx > '" + getFirstDay() + " 00:00:00'" +
                      " )," +
                    " Client_Master c," +
                    " Employee_Master001 d," +
                    " Route_Area e" + 
                    " LEFT JOIN Branch h" + 
                      " ON e.sBranchCd = h.sBranchCd," +
                    " Client_Master g" + 
                " WHERE a.sAcctNmbr = b.sAcctNmbr" + 
                    " AND b.sClientID = c.sClientID" + 
                    " AND b.sRouteIDx = e.sRouteIDx" + 
                    " AND d.sEmployID = g.sClientID" + 
                    " AND a.dTransact BETWEEN '" + getFirstDay() + " 00:00:00' AND " + SQLUtil.toSQL(_instance.getServerDate()) +
                    " AND ISNULL(i.sEmployID)" + 
                    " AND b.cMotorNew = '1'" + 
                    " AND e.sManagrID = d.sEmployID" + 
                    " AND a.cOffPaymx IN ('0', '2')" + 
                " ORDER BY xEmployNm," +
                    " a.dTransact," +
                    " a.sORNoxxxx";
    }
    
    private String getFirstDay(){
        String lsDate = SQLUtil.dateFormat(_instance.getServerDate(), SQLUtil.FORMAT_SHORT_DATE);
        
        lsDate = lsDate.substring(0, 8); //2026-02-08
        lsDate += "01";
        return lsDate;
    }
}
