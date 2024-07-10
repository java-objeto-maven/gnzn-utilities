package org.guanzon.gnzn.utilities.test;


import org.guanzon.appdriver.base.GRider;

public class testLogin {
    public static void main(String [] args){
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Maven_Systems";
        }
        else{
            path = "/srv/GGC_Maven_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRider instance = new GRider("gRider");
        
        if (!instance.logUser("gRider", "M001000001")){
            System.err.println(instance.getErrMsg());
            System.exit(1);
        }
        
        System.out.println("Connected - " + instance.getLogName());
        System.out.println("Connected - " + instance.getDivisionCode());
        System.out.println("Connected - " + instance.getDivisionName());
    }
}
