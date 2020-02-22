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
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<File> thumbnails;

    public ImageAdapter(Context c) {
        mContext = c;
        this.thumbnails = initThumbnails();
    }

    private List<File> initThumbnails() {
        File[] thumbnails = getDirectory().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith("thumb.JPG");
            }
        });

        List<File> files = Arrays.asList(thumbnails);

        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                int dateComparison = Long.compare(rhs.lastModified(), lhs.lastModified());
                return dateComparison != 0L ? dateComparison : rhs.getName().compareTo(lhs.getName());
            }
        });
        return files;
    }

    public int getCount() {
        return thumbnails.size();
    }

    public List<File> getThumbnails() {
        return thumbnails;
    }

    private File getDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        CheckableImageView  imageView = new CheckableImageView(mContext);
        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(300, 200);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(4, 4, 4, 4);
        imageView.setBackgroundColor(Color.WHITE);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoader.getInstance().displayImage("file://" + thumbnails.get(position).getAbsolutePath(), imageView, options);

        return imageView;
    }
}
