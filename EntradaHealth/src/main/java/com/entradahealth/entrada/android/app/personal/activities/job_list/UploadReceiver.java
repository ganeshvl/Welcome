package com.entradahealth.entrada.android.app.personal.activities.job_list;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class UploadReceiver extends ResultReceiver {

	private Receiver mReceiver;

	public UploadReceiver(Handler handler) {
        super(handler);
    }
 
    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }
 
    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }
 
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

}
