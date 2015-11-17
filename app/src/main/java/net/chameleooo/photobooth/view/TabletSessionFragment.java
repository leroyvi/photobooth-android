/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.chameleooo.photobooth.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.chameleooo.photobooth.MainActivity;
import net.chameleooo.photobooth.R;
import net.chameleooo.photobooth.ptp.Camera;
import net.chameleooo.photobooth.ptp.PtpConstants;
import net.chameleooo.photobooth.ptp.model.LiveViewData;

public class TabletSessionFragment extends SessionFragment {

    private final Handler handler = new Handler();

    private LayoutInflater inflater;

    private ToggleButton liveViewToggle;

    private Button takePictureBtn;
    private PictureView liveView;


    private LiveViewData currentLiveViewData;
    private LiveViewData currentLiveViewData2;
    private Toast focusToast;

    private Bitmap currentCapturedBitmap;
    private SharedPreferences prefs;

    private boolean showsCapturedPicture;

    private Button btnLiveview;

    private Runnable liveViewRestarterRunner;

    private boolean justCaptured;

    private final Runnable justCapturedResetRunner = new Runnable() {
        @Override
        public void run() {
            justCaptured = false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.live, container, false);

        this.inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//        takePictureBtn = (Button) view.findViewById(R.id.takePictureBtn);
        liveView = (PictureView) view.findViewById(R.id.liveView);
//        liveViewToggle = (ToggleButton) view.findViewById(R.id.liveViewToggle);
//        btnLiveview = (Button) view.findViewById(R.id.btn_liveview);

        btnLiveview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLiveview.setVisibility(View.GONE);
                startLiveViewAgain();
            }
        });

        prefs = getActivity().getSharedPreferences("settings.xml", Context.MODE_PRIVATE);

        takePictureBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onTakePictureClicked(v);
            }
        });

        enableUi(false);

        ((MainActivity) getActivity()).setSessionView(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        liveView.setLiveViewData(null);
        if (camera() != null) {
            cameraStarted(camera());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (camera() != null) {
            if (camera().isLiveViewOpen()) {
                // TODO possible that more than one calls this
                currentLiveViewData = null;
                currentLiveViewData2 = null;
                camera().getLiveViewPicture(null);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void enableUi(boolean enabled) {
        takePictureBtn.setEnabled(enabled);
    }

    @Override
    public void cameraStarted(Camera camera) {
        enableUi(true);

        propertyChanged(Camera.Property.BatteryLevel, camera.getProperty(Camera.Property.BatteryLevel));
        propertyChanged(Camera.Property.FocusMode, camera.getProperty(Camera.Property.FocusMode));
        propertyChanged(Camera.Property.AvailableShots, camera.getProperty(Camera.Property.AvailableShots));
        propertyChanged(Camera.Property.CurrentFocusPoint, camera.getProperty(Camera.Property.CurrentFocusPoint));

        if (camera.isLiveViewSupported()) {
            liveViewToggle.setEnabled(camera.isLiveViewSupported());
        }

        if (camera.isLiveViewOpen()) {
            liveViewStarted();
        }
    }


    @Override
    public void cameraStopped(Camera camera) {
        enableUi(false);
    }

    @Override
    public void propertyChanged(int property, int value) {
        if (!inStart || camera() == null) {
            return;
        }
        Integer icon = camera().propertyToIcon(property, value);
    }

    @Override
    public void propertyDescChanged(int property, int[] values) {
        if (!inStart || camera() == null) {
            return;
        }
    }

    @Override
    public void setCaptureBtnText(String text) {
        takePictureBtn.setText(text);
    }

    @Override
    public void focusStarted() {
        focusToast.cancel();
        takePictureBtn.setEnabled(false);
    }

    @Override
    public void focusEnded(boolean hasFocused) {
        if (hasFocused) {
            focusToast.show();
        }
        takePictureBtn.setEnabled(true);
    }

    @Override
    public void liveViewStarted() {
        if (!inStart || camera() == null) {
            return;
        }
        liveViewToggle.setChecked(true);
        liveView.setLiveViewData(null);
        showsCapturedPicture = false;
        currentLiveViewData = null;
        currentLiveViewData2 = null;
        camera().getLiveViewPicture(null);
    }

    @Override
    public void liveViewStopped() {
        if (!inStart || camera() == null) {
            return;
        }
        liveViewToggle.setChecked(false);
    }

    @Override
    public void liveViewData(LiveViewData data) {
        if (!inStart || camera() == null) {
            return;
        }
        if (justCaptured || showsCapturedPicture || !liveViewToggle.isChecked()) {
            return;
        }
        if (data == null) {
            camera().getLiveViewPicture(null);
            return;
        }

        liveView.setLiveViewData(data);
        currentLiveViewData2 = currentLiveViewData;
        this.currentLiveViewData = data;
        camera().getLiveViewPicture(currentLiveViewData2);
    }

    private void startLiveViewAgain() {
        showsCapturedPicture = false;
        if (currentCapturedBitmap != null) {
            liveView.setPicture(null);
            currentCapturedBitmap.recycle();
            currentCapturedBitmap = null;
        }
        if (camera() != null && camera().isLiveViewOpen()) {
            liveView.setLiveViewData(null);
            currentLiveViewData = null;
            currentLiveViewData2 = null;
            camera().getLiveViewPicture(currentLiveViewData2);
        }
    }

    @Override
    public void capturedPictureReceived(int objectHandle, String filename, Bitmap thumbnail, Bitmap bitmap) {
        if (!inStart) {
            bitmap.recycle();
            return;
        }
        showsCapturedPicture = true;
        if (liveViewToggle.isChecked()) {
            btnLiveview.setVisibility(View.VISIBLE);
        }
        liveView.setPicture(bitmap);
        Toast.makeText(getActivity(), filename, Toast.LENGTH_SHORT).show();
        if (currentCapturedBitmap != null) {
            currentCapturedBitmap.recycle();
        }
        currentCapturedBitmap = bitmap;
        if (bitmap == null) {
            Toast.makeText(getActivity(), "Error decoding picture. Try to reduce picture size in settings!",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void objectAdded(int handle, int format) {
        if (camera() == null) {
            return;
        }
        if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
            if (liveViewToggle.isChecked()) {
                handler.post(liveViewRestarterRunner);
            } else {
                camera().retrievePicture(handle);
            }
        }
    }

    public void onFocusClicked(View view) {
        camera().focus();
    }

    public void onTakePictureClicked(View view) {
        // TODO necessary
        //liveView.setLiveViewData(null);
        camera().capture();
        justCaptured = true;
        handler.postDelayed(justCapturedResetRunner, 500);
    }
}
