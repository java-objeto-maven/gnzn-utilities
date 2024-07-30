package org.guanzon.gnzn.utilities.lib.hcm;

import org.guanzon.appdriver.base.GRider;

public interface ILoveMyJobValidator {
    public void setGRider(GRider foValue);
    public void setWithParent(boolean fbValue);
    public boolean Run(String fsBranchCd, String fsDateFrom, String fsDateThru);
    
    public String getMessage();
}
