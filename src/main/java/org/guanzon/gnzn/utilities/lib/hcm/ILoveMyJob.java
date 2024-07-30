package org.guanzon.gnzn.utilities.lib.hcm;

public class ILoveMyJob {
    public enum Type{
        LR_PAYMENT,
        LR_PAYMENT_PR,
        OFFICIAL_RECEIPT,
        PROVISIONARY_RECEIPT,
        MC_SALES,
        SP_SALES,
        MP_SALES,
        MP_SALES_ACC,
        RAFFLE_DATE,
        RAFFLE_WINNER,
        RAFFLE_START,
        RAFFLE_END,
        EMPLOYEE
    }
    
    public static ILoveMyJobValidator make(Type foType){
        switch (foType) {
            case EMPLOYEE:
                return new Employees();
            case LR_PAYMENT:
                return new LRPayment();
            case LR_PAYMENT_PR:
                return new LRPaymentPR();
            case OFFICIAL_RECEIPT:
                return new OfficialReceipt();
            case PROVISIONARY_RECEIPT:
                return new ProvisionaryReceipt();
            case MC_SALES:
                return new MCSales();
            case SP_SALES:
                return new SPSales();
            case MP_SALES:
                return new MPSales();
            case MP_SALES_ACC:
                return new MPSalesAcc();
            case RAFFLE_DATE:
                return new PanaloRaffle();
            case RAFFLE_WINNER:
                return new PanaloWinners();
            case RAFFLE_START:
                return new RaffleNotificationStart();
            case RAFFLE_END:
                return new RaffleNotificationEnd();
            default:
                return null;
        }
    }
}
