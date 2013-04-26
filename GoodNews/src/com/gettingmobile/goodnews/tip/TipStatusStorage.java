package com.gettingmobile.goodnews.tip;

public interface TipStatusStorage {
    boolean wasTipShown(String tipId);
    void setTipShown(String tipId);
}
