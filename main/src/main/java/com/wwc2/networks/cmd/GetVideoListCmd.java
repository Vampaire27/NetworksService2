package com.wwc2.networks.cmd;

import com.wwc2.networks.server.bean.RemotelyBean;
import com.wwc2.networks.server.dvr.DvrManagement;
import com.wwc2.networks.server.utils.Config;
import com.wwc2.networks.server.utils.LogUtils;

public class GetVideoListCmd extends BaseCmd{

    public GetVideoListCmd(RemotelyBean mRemotelyBean) {
        super(Config.VIDEOPAGE, mRemotelyBean);
    }

    @Override
    public void exec() {
        LogUtils.d(" GetVideoListCmd ........");
        DvrManagement.newInstance().getVideoFilelist(mRemotelyBean);
        nextCmd();
    }

    @Override
    public boolean isSingleCmd() {
        return false;
    }
}
