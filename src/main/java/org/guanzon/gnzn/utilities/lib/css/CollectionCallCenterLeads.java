package org.guanzon.gnzn.utilities.lib.css;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;

public class CollectionCallCenterLeads {
    final int REFILL = 20;
    
    GRider _instance;
    JSONObject _json;
    
    String _sql;
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

            _sql = getSQ_ForCollection();

            ResultSet loForColl = _instance.executeQuery(_sql);
            
            while(loForColl.next()){
                //check if this customer already paid
                //check if this cusomer was already called for the period
                //add to leads
            }
        }
        
        _json.put("result", "success");
        return _json;
    }
    
    public boolean checkPeriod(){
        _period_from = "";
        _period_thru = "";
        return true;
    }
    
    public boolean isLRCallEncoded(String accountNo){
        return false;
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
                    " h.sEngineNo" + 
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
                        " OR i.dFiredxxx > '2026-02-03 00:00:00'" +
                      " )," +
                    " Client_Master b" + 
                    " LEFT JOIN Barangay X" +
                      " ON b.sBrgyIDxx = x.sBrgyIDxx," +
                    " Route_Area c" + 
                    " LEFT JOIN Branch z" + 
                      " ON c.sBranchCd = z.sBranchCd" + 
                    " LEFT JOIN Branch_Others Y" +
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
                    " AND a.dPurchase < '2026-02-03 00:00:00'" + 
                    " AND (" +
                      " a.cAcctStat = '0'" + 
                      " OR (" +
                        " a.dClosedxx >= '2026-02-03 00:00:00'" + 
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
                      "  OR i.dFiredxxx > '2026-02-01 00:00:00'" +
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
                    " AND a.dTransact BETWEEN '2026-02-01 00:00:00'" + 
                    " AND '2026-02-07 23:59:59'" + 
                    " AND ISNULL(i.sEmployID)" + 
                    " AND b.cMotorNew = '1'" + 
                    " AND e.sManagrID = d.sEmployID" + 
                    " AND a.cOffPaymx IN ('0', '2')" + 
                " ORDER BY xEmployNm," +
                    " a.dTransact," +
                    " a.sORNoxxxx";
    }
}
