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

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.chameleooo.photobooth.Picture;
import net.chameleooo.photobooth.PictureView;
import net.chameleooo.photobooth.R;
import net.chameleooo.photobooth.ptp.Camera;
import net.chameleooo.photobooth.ptp.PtpConstants;
import net.chameleooo.photobooth.ptp.model.LiveViewData;
import net.chameleooo.photobooth.ptp.model.ObjectInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class TabletSessionFragment extends SessionFragment implements Camera.RetrieveImageInfoListener {

    private final Handler handler = new Handler();

    private PictureView liveView;

    private LiveViewData currentLiveViewData;
    private LiveViewData currentLiveViewData2;

    private boolean showsCapturedPicture;

    private boolean justCaptured;

    private boolean fourPicturesMode = false;
    private List<Picture> pictures = new ArrayList<>();

    private final Runnable justCapturedResetRunner = new Runnable() {
        @Override
        public void run() {
            justCaptured = false;
        }
    };

    private View layoutView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.session_frag, container, false);

        liveView = (PictureView) layoutView.findViewById(R.id.liveView);
        FrameLayout fullscreenContent = (FrameLayout) layoutView.findViewById(R.id.fullscreen_content);

        fullscreenContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startLive(v.getRootView());
            }
        });

        ((SessionActivity) getActivity()).setSessionView(this);

        final View onePictButton = layoutView.findViewById(R.id.fab_one_pict);
        final View fourPictButton = layoutView.findViewById(R.id.fab_four_pict);

        if (fourPicturesMode) {
            onePictButton.setVisibility(View.GONE);
            fourPictButton.setVisibility(View.VISIBLE);
        } else {
            onePictButton.setVisibility(View.VISIBLE);
            fourPictButton.setVisibility(View.GONE);
        }

        onePictButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onePictButton.setVisibility(View.GONE);
                fourPictButton.setVisibility(View.VISIBLE);
                fourPicturesMode = true;
            }
        });

        fourPictButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onePictButton.setVisibility(View.VISIBLE);
                fourPictButton.setVisibility(View.GONE);
                fourPicturesMode = false;
            }
        });

        return layoutView;
    }

    public void startLive(View view) {
        startLiveview(view);
    }

    private void startLiveview(final View view) {
        View liveViewLayout = view.findViewById(R.id.liveViewLayout);
        liveViewLayout.setVisibility(FrameLayout.VISIBLE);
        View startLayout = view.findViewById(R.id.startLayout);
        startLayout.setVisibility(FrameLayout.GONE);
        camera().setLiveView(true);
        countdown(view);
    }

    private void countdown(final View view) {
        new CountDownTimer(6000, 500) {
            int count = 12;
            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            TextView progressText = (TextView) view.findViewById(R.id.progress_text);
            TextView smileText = (TextView) view.findViewById(R.id.smile_text);

            public void onTick(final long millisUntilFinished) {
                if (count % 4 == 0) {
                    progressText.setText(String.valueOf(count / 4));
                    progressBar.setVisibility(View.VISIBLE);
                    progressText.setVisibility(View.VISIBLE);
                    ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 100); // see this max value coming back here, we animale towards that value
                    animation.setDuration(2000); //in milliseconds
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.start();
                }
                count--;
            }

            public void onFinish() {
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                smileText.setVisibility(View.VISIBLE);
                takePicture();
                if (fourPicturesMode) {
                    takePicture();
                    takePicture();
                    takePicture();
                }
            }
        }.start();
    }

    private void restart(View view) {
        View liveViewLayout = view.findViewById(R.id.liveViewLayout);
        liveViewLayout.setVisibility(FrameLayout.GONE);
        View startLayout = view.findViewById(R.id.startLayout);
        startLayout.setVisibility(FrameLayout.VISIBLE);
        liveViewLayout.setVisibility(FrameLayout.GONE);
        TextView smileText = (TextView) view.findViewById(R.id.smile_text);
        smileText.setVisibility(View.GONE);
        camera().setLiveView(false);
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
                currentLiveViewData = null;
                currentLiveViewData2 = null;
                camera().getLiveViewPicture(null);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (camera() != null) {
            camera().setLiveView(false);
        }
    }

    @Override
    public void enableUi(boolean enabled) {
    }

    @Override
    public void cameraStarted(Camera camera) {
        if (camera.isLiveViewOpen()) {
            liveViewStarted();
        }
    }

    @Override
    public void cameraStopped(Camera camera) {
    }

    @Override
    public void propertyChanged(int property, int value) {
        if (!inStart || camera() == null) {
            return;
        }
        if (property == Camera.Property.CurrentFocusPoint) {
            liveView.setCurrentFocusPoint(value);
        }
    }

    @Override
    public void propertyDescChanged(int property, int[] values) {
    }

    @Override
    public void setCaptureBtnText(String text) {
    }

    @Override
    public void focusStarted() {
    }

    @Override
    public void focusEnded(boolean hasFocused) {
    }

    @Override
    public void liveViewStarted() {
        if (!inStart || camera() == null) {
            return;
        }
        liveView.setLiveViewData(null);
        showsCapturedPicture = false;
        currentLiveViewData = null;
        currentLiveViewData2 = null;
        camera().getLiveViewPicture(null);
    }

    @Override
    public void liveViewStopped() {
    }

    @Override
    public void liveViewData(LiveViewData data) {
        if (!inStart || camera() == null) {
            return;
        }
        if (justCaptured || showsCapturedPicture) {
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

    @Override
    public void capturedPictureReceived(int objectHandle, final String filename, final Bitmap thumbnail, final Bitmap bitmap) {
        if (!inStart) {
            bitmap.recycle();
            return;
        }

        Picture picture = new Picture(filename, bitmap);

        if (fourPicturesMode && pictures.size() < 4) {
            pictures.add(picture);
            if (pictures.size() == 4) {
                Bitmap result = combineImages(pictures.get(0).getBitmap(),
                        pictures.get(1).getBitmap(),
                        pictures.get(2).getBitmap(),
                        pictures.get(3).getBitmap());
                pictures.clear();
                picture = null;
                Bitmap resized = resize(result, 3696, 2448);
                final File bitmapFile = savePicture(new Picture(filename, resized));
                askForAction(bitmapFile);
                restart(liveView.getRootView());
            }
        } else {
            final File bitmapFile = savePicture(picture);
            askForAction(bitmapFile);
            restart(liveView.getRootView());
        }
    }

    @NonNull
    private File savePicture(Picture picture) {
        final File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        final File bitmapFile = new File(directory, picture.getFilename());
        try {
            picture.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(bitmapFile));
            File thumbnailFile = new File(directory, picture.getFilename().replaceAll("\\.JPG", "\\.thumb\\.JPG"));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap thumbBitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), new BitmapFactory.Options());
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 50, new FileOutputStream(thumbnailFile));
        } catch (FileNotFoundException e) {
            Log.e("CAPUTRING", "Error saving thumbnail file", e);
        }
        return bitmapFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case 1:
                    final Uri mImageUri = data.getData();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    Bitmap thumbBitmap = BitmapFactory.decodeFile(mImageUri.getPath(), new BitmapFactory.Options());
                    try {
                        thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 50, new FileOutputStream(mImageUri.getPath().replaceAll("\\.JPG", "\\.thumb.JPG")));
                    } catch (FileNotFoundException e) {
                        Log.e("", "Cannot save thumbnail", e);
                    }
                    break;
            }
        }
    }

    private void askForAction(final File bitmapFile) {
        PhotoResultDialogFragment.newInstance(bitmapFile.getAbsolutePath()).show(getActivity().getFragmentManager(), null);
    }


    @Override
    public void objectAdded(int handle, int format) {
        if (camera() == null) {
            return;
        }
        if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
            camera().retrievePicture(handle);
        }
    }

    public void takePicture() {
        camera().capture();
        justCaptured = true;
        handler.postDelayed(justCapturedResetRunner, 500);
    }

    @Override
    public void onImageInfoRetrieved(final int objectHandle, final ObjectInfo objectInfo, final Bitmap thumbnail) {
    }

    public Bitmap combineImages(Bitmap first, Bitmap seconde, Bitmap third, Bitmap fourth) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs = null;

        int width = first.getWidth() + 20 + seconde.getWidth();
        int height = first.getHeight() + 20 + third.getHeight();

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);
        comboImage.drawColor(Color.WHITE);
        comboImage.drawBitmap(first, 0f, 0f, null);
        comboImage.drawBitmap(seconde, first.getWidth() + 20, 0f, null);
        comboImage.drawBitmap(third, 0f, first.getHeight() + 20, null);
        comboImage.drawBitmap(fourth, first.getWidth() + 20, first.getHeight() + 20, null);

        return cs;
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
}
