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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.print.PrintHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import net.chameleooo.photobooth.R;
import net.chameleooo.photobooth.util.SystemUIHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static android.app.Activity.RESULT_OK;

public class GalleryFragment extends Fragment {

    private static int selectedPosition = 0;

    private GridView gridview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.gallery_frag, container, false);

        gridview = (GridView) view.findViewById(R.id.gridview);
        final ImageAdapter imageAdapter = new ImageAdapter(getActivity());
        gridview.setAdapter(imageAdapter);
        gridview.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        setSelected(gridview, selectedPosition);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
            }
        });

        FloatingActionButton print = (FloatingActionButton) view.findViewById(R.id.fab_print);

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrintHelper photoPrinter = new PrintHelper(getActivity());
                photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FILL);

                File thumbnail = imageAdapter.getThumbnails().get(gridview.getCheckedItemPosition());

                String imagePath = thumbnail.getPath().replaceAll("\\.thumb", "");

                Uri imageUri = Uri.parse("file://" + imagePath);

                try {
                    photoPrinter.printBitmap("Print photo", imageUri);
                } catch (FileNotFoundException e) {
                    Log.e("TAG", "Cannot print ", e);
                }
            }
        });

        gridview.setOnTouchListener(new View.OnTouchListener() {
            Handler handler = new Handler();

            int numberOfTaps = 0;
            long lastTapTimeMs = 0;
            long touchDownMs = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchDownMs = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacksAndMessages(null);

                        if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
                            //it was not a tap

                            numberOfTaps = 0;
                            lastTapTimeMs = 0;
                            break;
                        }

                        if (numberOfTaps > 0
                                && (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
                            numberOfTaps += 1;
                        } else {
                            numberOfTaps = 1;
                        }

                        lastTapTimeMs = System.currentTimeMillis();

                        if (numberOfTaps == 10) {
                            SystemUIHelper.swicthUI();
                            return true;
                        }
                }

                return false;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        gridview.setAdapter(new ImageAdapter(getActivity()));
        setSelected(gridview, selectedPosition);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case 1:
                    Uri mImageUri = data.getData();

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    Bitmap thumbBitmap = BitmapFactory.decodeFile(mImageUri.getPath(), new BitmapFactory.Options());
                    try {
                        thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(mImageUri.getPath().replaceAll("\\.JPG", "\\.thumb.JPG")));
                    } catch (FileNotFoundException e) {
                        Log.e("", "Cannot save thumbnail", e);
                    }
                    selectedPosition = 0;
                    break;
            }
        }
        setSelected(gridview, selectedPosition);
    }

    private void setSelected(final GridView view, final int position) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setSelection(position);
                view.setItemChecked(position, true);
            }
        });
    }
}
