package org.guanzon.gnzn.utilities.lib.hcm;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.StringHelper;

public class OfficialReceipt implements ILoveMyJobValidator{
    private final String _code = "panalo.ilmj.office.payment";
    
    private GRider _instance;
    private boolean _wparent;
    private String _message;
    
    @Override
    public void setGRider(GRider foValue) {
        _instance = foValue;
    }
    
    @Override
    public void setWithParent(boolean fbValue) {
        _wparent = fbValue;
    }

    @Override
    public boolean Run(String fsBranchCd, String fsDateFrom, String fsDateThru) {
        if (_instance == null){
            _message = "Application driver is not set.";
            return false;
        }
        
        String lsSQL = getSQ_Master(fsBranchCd, fsDateFrom, fsDateThru);
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) == 0) return true;      
        
        try {
            RowSetFactory factory = RowSetProvider.newFactory();
            CachedRowSet loMaster = factory.createCachedRowSet();
            loMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            if (getSysConfig(_code) == null) {
                _message = "System config for this object is not set.";
                return false;
            }
            
            int lnEntriesx = Integer.parseInt(getSysConfig(_code));
            int lnRaffleNo = getLastRaffle(lsSQL);
            
            if (!_wparent) _instance.beginTrans();
            
            while (loMaster.next()){                
                //determine number of full amortization
                int lnRfleCtr = (int) Math.round((loMaster.getDouble("nAmountxx") + loMaster.getDouble("nRebatesx")) / loMaster.getDouble("nMonAmort"));
                
                if (lnRfleCtr > 0){
                    for (int lnCtr = 1; lnCtr <= lnRfleCtr * lnEntriesx; lnCtr++){                
                        lnRaffleNo += 1;
                        
                        loMaster.updateString("sRaffleID", MiscUtil.getNextCode("RaffleEntries", "sRaffleID", true, _instance.getConnection(), fsBranchCd));
                        loMaster.updateString("sRaffleNo", StringHelper.prepad(String.valueOf(lnRaffleNo), 6, '0'));
                        loMaster.updateRow();
                        
                        lsSQL = MiscUtil.rowset2SQL(loMaster, "RaffleEntries", "nAmountxx»nRebatesx»nMonAmort");
                        
                        if (lsSQL.isEmpty()){
                            _message = "No statement to execute.";
                            return false;
                        }
                        
                        if (_instance.executeQuery(lsSQL, "RaffleEntries", _instance.getBranchCode(), "") <= 0){
                            _message = _instance.getErrMsg();
                            if (!_wparent) _instance.rollbackTrans();
                            return false;
                        }
                    }
                }
                
            }
            
            if (!_wparent) _instance.commitTrans();
        } catch (SQLException e) {
            _message = e.getMessage();
            return false;
        }
        
        return true;
    }

    @Override
    public String getMessage() {
        return _message;
    }
    
    private String getSysConfig(String fsConfigCd){
        String lsSQL = "SELECT sConfigVl FROM xxxSysConfig WHERE sConfigCd = " + SQLUtil.toSQL(fsConfigCd);
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) != 1){
            MiscUtil.close(loRS);
            return null;
        }
        
        try {
            loRS.first();
            lsSQL = loRS.getString("sConfigVl");
            MiscUtil.close(loRS);
            return lsSQL;
        } catch (SQLException ex) {
            ex.printStackTrace();
            MiscUtil.close(loRS);
            return null;
        }
    }
    
    private int getLastRaffle(String fsBranchCd){
        String lsSQL = "SELECT sRaffleNo" +
                        " FROM RaffleEntries" +
                        " WHERE sRaffleID LIKE " + SQLUtil.toSQL(fsBranchCd + String.valueOf(MiscUtil.getDateYear(_instance.getServerDate())).substring(2) + "%") +
                        " ORDER BY sRaffleID DESC" +
                        " LIMIT 1";
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        int lnRow = (int) MiscUtil.RecordCount(loRS);
        MiscUtil.close(loRS);
        
        return lnRow;
    }
    
    private String getSQ_Master(String fsBranchCd, String fsDateFrom, String fsDateThru){
        return "SELECT" +
                    "  b.sRaffleID" +
                    ", a.dTransact" +
                    ", LEFT(a.sTransNox, 4) sBranchCD" +
                    ", b.sRaffleNo" +
                    ", a.sAcctNmbr" +
                    ", c.sMobileNo" +
                    ", a.sORNoxxxx sReferNox" +
                    ", 'OFCG' sSourceCD" +
                    ", '0' cRaffledx" +
                    ", a.sModified" +
                    ", a.dModified" +
                    ", a.nTranAmtx" +
                    ", a.nDiscount" +
                    ", e.nMonAmort" +
                " FROM Receipt_Master a" +
                    " LEFT JOIN RaffleEntries b" +
                    " ON LEFT(a.sTransNox, 4) = b.sBranchCD" +
                        " AND a.sORNoxxxx = b.sReferNox" +
                        " AND a.dTransact = b.dTransact" +
                    " LEFT JOIN Client_Master c ON a.sClientID = c.sClientID" +
                    " LEFT JOIN Employee_Master001 d ON a.sClientID = d.sEmployID" +
                    " LEFT JOIN MC_AR_Master e ON a.sAcctNmbr = e.sAcctNmbr" +
                " WHERE a.sTransNox LIKE " + SQLUtil.toSQL(fsBranchCd + "%") +
                    " AND a.dTransact BETWEEN " + SQLUtil.toSQL(fsDateFrom) + " AND " + SQLUtil.toSQL(fsDateThru) +
                    " AND a.cTranType = '1'" +
                    " AND NOT (a.cTranStat & '3' = 3)" +
                    " AND d.sEmployID IS NULL" +
                    " AND b.sRaffleID IS NULL" +
                    " AND IFNull(e.nMonAmort, 0) > 0" +
                    " AND LENGTH(c.sMobileNo) BETWEEN 11 AND 13" +
                  " ORDER BY dTransact, sReferNox";
    }
}
