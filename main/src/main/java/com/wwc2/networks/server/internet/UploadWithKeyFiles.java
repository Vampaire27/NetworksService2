package com.wwc2.networks.server.internet;

import android.content.Context;
import android.text.TextUtils;

import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.wwc2.networks.CarServiceClient;
import com.wwc2.networks.server.device.DeviceManagement;
import com.wwc2.networks.server.utils.LogUtils;

import org.json.JSONObject;

import java.io.File;

public class UploadWithKeyFiles {
    private static final String TAG = "UploadWithKeyFiles";
    private UploadManager uploadManager;

    public interface onUploadListener{
        void onComplete(String key);
        void onFailure(String result);
    }

    public void uploadFile(final Context con,
            String filepath, String token, String name, final onUploadListener upListener){
        if (uploadManager == null) {
            Configuration config = new Configuration.Builder()
                    .chunkSize(10240 * 1024)
                    .putThreshhold(10240 * 1024)
                    .zone(Zone.httpAutoZone)
                    .build();
            uploadManager = new UploadManager(config);
        }
        File uploadFile = new File(filepath);
        String sn = DeviceManagement.newInstance(CarServiceClient.getContext()).getSerialNumber();

        String name_key;
        if(TextUtils.isEmpty(name)){
            name_key = sn + uploadFile.getName();
        }else{
            name_key = sn + name;
        }

//        LogUtils.d("postUploadLogFile---name_key=" + name_key + ",,file.size=" + uploadFile.length());
        LogUtils.d("postUploadLogFile---filepath=" + filepath + ",,token=" + token);
        uploadManager.put(uploadFile, name_key, token,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo respInfo,
                                         JSONObject jsonData) {
                        LogUtils.d("postUploadLogFile---respInfo=" +respInfo);
                        if (respInfo.isOK()) {
                            try {
                                String fileKey = jsonData.getString("key");
                                upListener.onComplete(fileKey);
                            } catch (Exception e) {
                                upListener.onFailure(respInfo.toString());
                            }
                        } else {
                            upListener.onFailure(respInfo.toString());
                        }
                    }
                }, new UploadOptions(null, null, false,
                        new UpProgressHandler() {
                            @Override
                            public void progress(String key, double percent) {
                                String pro = ((int)(percent * 100)) + "%";
//                                ToastUtils.showShort(con,"---上传中---进度=" + pro);
                                LogUtils.d("postUploadLogFile---上传中---进度=" + pro);

                                CarServiceClient.updateLogs("图片上传中,进度：" + pro);
                            }
                        }, new UpCancellationSignal() {

                    @Override
                    public boolean isCancelled() {
                        LogUtils.d("postUploadLogFile---isCancelled");
                        return false;
                    }
                })
        );
    }
}
