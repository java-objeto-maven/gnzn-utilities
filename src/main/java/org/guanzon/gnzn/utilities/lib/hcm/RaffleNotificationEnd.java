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
public class RaffleNotificationEnd implements ILoveMyJobValidator{
    private final String _code = "fb.raffle.from";
    
    private GRider _instance;
    private boolean _wparent;
    private String _message;
    private final String EMP_RAFFLE_TABLE  = "Employee_Raffle";
    
    @Override
    public void setGRider(GRider foValue) {
        _instance = foValue;
    }
    
    @Override
    public void setWithParent(boolean fbValue) {
        _wparent = fbValue;
    }

    @Override
    public boolean Run(String fsTransNox, String fsRaffleDt, String fsRemarks) {
        if (_instance == null){
            _message = "Application driver is not set.";
            return false;
        }
        
        String lsSQL = getSQL_PanaloRaffleNotification();
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
            
            
            while (loMaster.next()){                
                if (!_wparent) _instance.beginTrans();
                String _body = "Hello everyone,\\\n \\\n \\\n" +
                                "We would like to extend our heartfelt gratitude to all who participated in our raffle draw. Your contributions have helped us achieve our goals, and we could not have done it without your support.\n" +
                                "\\\n \\\n" +
                                "The raffle draw has now ended, and we are pleased to announce the lucky winners. We will be reaching out to the winners individually through the guanzon mobile application.\n" +
                                "\\\n \\\n" +
                                "Thank you once again to everyone who participated, and congratulations to the winners! We look forward to your continued support in our future endeavors.";
               
                if(!WebClient.SendSystemPanaloRaffleStartEndNotification("gRider", loMaster.getString("sUserIDxx"), "I Love My Job", _body, 3)){
                    _message = "HTTP Error detected: " + System.getProperty("store.error.info");
                }
                lsSQL = "UPDATE Employee_Raffle SET" +
                            " cNotified = '1'" +
                        " WHERE sClientID = " + SQLUtil.toSQL(loMaster.getString("sClientID"));
                if (_instance.executeQuery(lsSQL, "Employee_Raffle", _instance.getBranchCode(), "") <= 0){
                    if (!_wparent) _instance.rollbackTrans();
                    _message = _instance.getMessage() + ";" + _instance.getErrMsg();
                    System.out.println(_message);
                    return false;
                }
                
                if (!_wparent) _instance.commitTrans();
            }
            
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
    private String getSQL_PanaloRaffleNotification(){
        return "SELECT" +
                    "  a.sClientID" +
                    ", a.sFullName" +
                    ", d.sUserIDxx" +
                " FROM " + EMP_RAFFLE_TABLE + " a" +
                    " LEFT JOIN Client_Master b ON a.sClientID = b.sClientID" +
                    " LEFT JOIN Employee_Master001 c ON a.sClientID = c.sEmployID" +
                    " LEFT JOIN App_User_Master d ON a.sClientID = d.sEmployNo" +
                " WHERE a.sClientID IS NOT NULL   AND a.sClientID = 'M00120001928' " +
                "   AND d.sProdctID = 'gRider'" +
                  " GROUP BY a.sClientID" + 
                  " ORDER BY sClientID";
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
}
