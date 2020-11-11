package com.wwc2.networks.cmd;


import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.utils.LogUtils;
import com.wwc2.networks.server.wakeup.AccPowerManager;

import java.util.ArrayList;
import java.util.List;

public class CmdManager implements AccPowerManager.WakeupCallBack {
     // current exe cmd;
     private static CmdManager mCmdManager;

     BaseCmd CurrentCmd = null;

     List<BaseCmd> mCmdList = new ArrayList<>();

     Object mcmdListLock  =new Object();


    public static CmdManager getInstans(){
        if (mCmdManager == null){
            mCmdManager = new CmdManager();
        }
        return mCmdManager;
    }


    public void postCmd(BaseCmd mBaseCmd) {
         if(!CarServiceClient.getDvrEnable()){
             LogUtils.d("getDvrEnable is false!");
             return;
         }

        checkcmdList(mBaseCmd);

        synchronized (mcmdListLock) {
            mCmdList.add(mBaseCmd);
        }

        if(CurrentCmd!= null && CurrentCmd.isSingleCmd()){  //　中断当前命令，不需要执行beginExec，直接执行下一条命令。
                CurrentCmd.InterruptCmd();
        }else{
            beginExec();
        }
    }

    public BaseCmd getCurrentCmd(){
        return CurrentCmd;
    }

    public void beginExec(){
        AccPowerManager.getInstance().aquireWakeLockLocked(this);//需要等待会掉才开始执行
    }

    public void exec(){
        if(mCmdList.size() > 0){
            synchronized (mcmdListLock){
              CurrentCmd = mCmdList.remove(0);
            }
            CurrentCmd.startexec();
        }else{
            finishExec();
        }
    }


    public void finishExec(){
        CurrentCmd = null;
        AccPowerManager.getInstance().releaseWakeLockLocked();
    }


   // implements AccPowerManager.WakeupCallBack
    @Override
    public void wakeUpFail() {
        LogUtils.d("wakeUpFail --- ");
        mCmdList.clear();
        finishExec();

    }

    public void checkcmdList(BaseCmd mCmd){  //remove the single cmd.
        if(mCmd.isSingleCmd() && mCmdList.size() > 0 ){
            synchronized (mcmdListLock) {
                // remove the single cmd.
                for (int i = mCmdList.size() -1; i > 0;i-- ) {
                    if(mCmdList.get(i).isSingleCmd()){
                        mCmdList.remove(i);
                    }
                }
            }
        }
    }

    @Override
    public void wakeUpSuccess() {
        LogUtils.d("wakeUpSuccess ++ beign exec cmd ");
        exec();
    }

}
