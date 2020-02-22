package net.chameleooo.photobooth;

import android.graphics.Bitmap;

public class Picture {

    private String filename;
    private Bitmap bitmap;

    public Picture(String filename, Bitmap bitmap) {
        this.filename = filename;
        this.bitmap = bitmap;
    }

    public String getFilename() {
        return filename;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
