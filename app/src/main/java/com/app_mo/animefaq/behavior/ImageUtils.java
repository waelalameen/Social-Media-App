package com.app_mo.animefaq.behavior;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

/**
 * Created by hp on 8/13/2017.
 */

public class ImageUtils {
    private static final int ROTATION_DEGREES = 90;

    public static Bitmap rotateImage(Bitmap image, String path) {
        Bitmap rotatedBitmap = null;

        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Matrix matrix = new Matrix();

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = ROTATION_DEGREES;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = ROTATION_DEGREES * 2;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = ROTATION_DEGREES * 3;
                    break;
                default:
                    break;
            }

            matrix.postRotate(orientation);
            rotatedBitmap = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

        } catch (IOException e) {
            e.printStackTrace();
            e.getMessage();

        }

        return rotatedBitmap;
    }

    public static int getOrientation(String path) {
        int orientation = 0;

        try {
            ExifInterface exifInterface = new ExifInterface(path);
            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = ROTATION_DEGREES;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = ROTATION_DEGREES * 2;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = ROTATION_DEGREES * 3;
                    break;
                default:
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
            e.getMessage();

        }

        return orientation;
    }
}
