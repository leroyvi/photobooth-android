package net.chameleooo.photobooth.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.print.PrintHelper;


import net.chameleooo.photobooth.R;

import java.io.FileNotFoundException;

public class PhotoResultDialogFragment extends DialogFragment {

    public static PhotoResultDialogFragment newInstance(String imagePath) {
        PhotoResultDialogFragment dialog = new PhotoResultDialogFragment();
        Bundle args = new Bundle();
        args.putString("imagePath", imagePath);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String imagePath = getArguments().getString("imagePath");

        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.dialog_print)
                .setPositiveButton(R.string.print, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PrintHelper photoPrinter = new PrintHelper(getActivity());
                        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FILL);
                        try {
                            photoPrinter.printBitmap("Print photo", Uri.parse("file://" + imagePath));

                        } catch (FileNotFoundException e) {
                        }
                    }
                }).setNegativeButton(R.string.restart, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                }).create();

    }
}