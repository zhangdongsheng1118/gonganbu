package com.zuozhan.app;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.starrtc.demo.utils.AEvent;
import com.starrtc.demo.utils.IEventListener;
import com.yixin.tinode.R;
import com.zuozhan.app.activity.AllBaseActivity;
import com.zuozhan.app.util.LocationUtil;

public class LocationTestActivity extends AllBaseActivity {

    private TextView location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.zh_location);
        location = findViewById(R.id.location);
        location.setText(LocationUtil.getIntance().mLatitude+"-"+LocationUtil.getIntance().mLongitude+"\r\n");
        AEvent.addListener(AEvent.AEVENT_LOCATION, new IEventListener() {
            @Override
            public void dispatchEvent(String aEventID, boolean success, Object eventObj) {
                location.append(eventObj+"\r\n");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AEvent.removeListener(AEvent.AEVENT_LOCATION);
    }
}
