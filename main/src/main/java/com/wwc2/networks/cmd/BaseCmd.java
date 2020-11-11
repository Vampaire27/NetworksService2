package com.wwc2.networks.cmd;

import android.os.AsyncTask;

import com.wwc2.networks.server.bean.RemotelyBean;
import com.wwc2.networks.server.utils.LogUtils;

public abstract class BaseCmd {

    public int cmdType ;
    public RemotelyBean mRemotelyBean;
    public CmdAsyncTask mCmdAsyncTask;

    public BaseCmd(int cmdType,RemotelyBean mRemotelyBean) {
        this.cmdType = cmdType;
        this.mRemotelyBean = mRemotelyBean;
        mCmdAsyncTask=new CmdAsyncTask();
    }

    public BaseCmd(int cmdType,RemotelyBean mRemotelyBean,boolean Sync) {
        this.cmdType = cmdType;
        this.mRemotelyBean = mRemotelyBean;
        if(Sync) {
            mCmdAsyncTask = null;
        }else{
            mCmdAsyncTask=new CmdAsyncTask();
        }
    }

    public void startexec() {
        if(mCmdAsyncTask != null) {
            mCmdAsyncTask.execute();
        }else{
            exec();
        }
    }


    public void nextCmd(){
        LogUtils.d("nextCmd ---");
        mCmdAsyncTask = null;
        CmdManager.getInstans().exec();
    }

    class CmdAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            exec();
            return true;
        }
    }


 public void InterruptCmd(){
        LogUtils.d("Interrupt this cmd");
 }


    abstract public void exec();
    abstract public boolean isSingleCmd();

}
