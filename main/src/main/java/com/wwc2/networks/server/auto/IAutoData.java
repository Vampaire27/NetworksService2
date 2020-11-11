package com.wwc2.networks.server.auto;

import com.wwc2.networks.server.bean.AMapBean;

public interface IAutoData {

    void registerCar(String jPushID, AutoManagement.AutoCallback callback);

    AMapBean getLocation();
}
