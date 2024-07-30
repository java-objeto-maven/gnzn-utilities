/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.guanzon.gnzn.utilities.lib.hcm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.WebClient;

/**
 *
 * @author User
 */
public class PanaloWinners implements ILoveMyJobValidator{
     private final String _code = "fb.raffle.from";
    
    private GRider _instance;
    private boolean _wparent;
    private String _message;
    private final String RAFFLE_WINNER_TABLE  = "RaffleWinners";
    
    @Override
    public void setGRider(GRider foValue) {
        _instance = foValue;
    }
    
    @Override
    public void setWithParent(boolean fbValue) {
        _wparent = fbValue;
    }

    @Override
    public boolean Run(String fsEmployID, String fsRaffleNo, String fsRaffleDt) {
        if (_instance == null){
            _message = "Application driver is not set.";
            return false;
        }
        String lsSQL;
        String lsCondition;
        ResultSet loRS;
        lsCondition = " a.dModified Like " + SQLUtil.toSQL(dateSQL(_instance.getServerDate().toString()) + "%") + 
                    " AND b.dRaffleDt Like " + SQLUtil.toSQL(dateSQL(_instance.getServerDate().toString()) + "%");
        lsSQL = getSQL_PanaloRaffleWinnerNotification();
        lsSQL = MiscUtil.addCondition(lsSQL, lsCondition) +" GROUP BY a.sAcctNmbr";
        loRS = _instance.executeQuery(lsSQL);
        System.out.println(lsSQL);
        
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
            
            if (!_wparent) _instance.beginTrans();
            while (loMaster.next()){ 
                String _body = "Dear " + loMaster.getString("xCompnyNm") + ", \\\n \\\n \\\t \\\t Congratulations! You have been selected as a winner of our \"I Love My Job\" Raffle Draw. "+
                        "You have won a"  + loMaster.getString("sPanaloDs") + " of "+ loMaster.getString("nAmountxx") + ", with a total quantity of " +loMaster.getString("nItemQtyx")+"." +
                        " Thank you for your participation and we hope this prize helps make your day a little brighter!" +
                        " \\\n \\\n \\\n Best regards,"+
                        " \\\nGUANZON GROUP OF COMPANIES";
                if(!WebClient.SendSystemPanaloRaffleNotification("gRider", loMaster.getString("sUserIDxx"), "I Love My Job", _body)){
                    _message = "HTTP Error detected: " + System.getProperty("store.error.info");
                }
                lsSQL = "UPDATE RaffleWinners SET" +
                            "  cSendStat = '1'" +
                        " WHERE sAcctNmbr = " + SQLUtil.toSQL(loMaster.getString("sAcctNmbr"));

                if (_instance.executeQuery(lsSQL, "RaffleWinners", _instance.getBranchCode(), "") <= 0){
                    if (!_wparent) _instance.rollbackTrans();
                    _message = _instance.getMessage() + ";" + _instance.getErrMsg();
                }
            }
            if (!_wparent) _instance.commitTrans();
            
        } catch (SQLException e) {
            _message = "message = " + e.getMessage();
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
    private String getSQL_PanaloRaffleWinnerNotification(){
        String lsSQL  = "SELECT " +
                        " c.sTransNox " +
                        " ,a.sAcctNmbr " +
                        " ,a.sAcctNmbr " +
                        " , g.sBranchCd " +
                        " , IFNULL(e.sCompnyNm, CONCAT(e.sLastName, ', ', e.sFrstName, ' ', IFNULL(e.sSuffixNm, ''), ' ', e.sMiddName)) xCompnyNm " +
                        " , g.sBranchNm " +
                        " , h.sPanaloDs  " +
                        " , f.sUserIDxx  " +
                        " , c.nAmountxx  " +
                        " , c.nItemQtyx  " +
                        " FROM  " + RAFFLE_WINNER_TABLE + " a " +
                        " , ILMJ_Master b " +
                        "     LEFT JOIN ILMJ_Detail c " +
                        "       ON b.sTransNox = c.sTransNox " +
                        "     LEFT JOIN Panalo_Info h " +
                        "       ON c.sPanaloCd = h.sPanaloCD " +
                        " ,Employee_Master001 d " +
                        "     LEFT JOIN Client_Master e " +
                        "       ON  d.sEmployID = e.sClientID " +
                        "     LEFT JOIN App_User_Master f " +
                        "       ON  d.sEmployID = f.sEmployNo " +
                        "	   AND f.sProdctID = 'gRider' " +
                        "     LEFT JOIN Branch g " +
                        "       ON  d.sBranchCd = g.sBranchCd " +
                        " WHERE a.sAcctNmbr = e.sClientID " +
                        " AND a.sPrizexxx = c.cWinnerxx  " +
                        " AND a.cTranstat  = '1'  ";
        return lsSQL;
    }
    public static String dateToWord (String dtransact) {
       
        try {
            Date date = new Date();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            date = (Date)formatter.parse(dtransact);  
            SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy");
            String todayStr = fmt.format(date);
            
            return todayStr;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String dateToTime (String dtransact) {
       
        try {
            Date date = new Date();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            date = (Date)formatter.parse(dtransact);  
            SimpleDateFormat fmt = new SimpleDateFormat("hh:mm aa");
            String todayStr = fmt.format(date);
            
            return todayStr;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String dateSQL (String dtransact) {
       
        try {
            Date date = new Date();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            date = (Date)formatter.parse(dtransact);  
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = fmt.format(date);
            
            return todayStr;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
