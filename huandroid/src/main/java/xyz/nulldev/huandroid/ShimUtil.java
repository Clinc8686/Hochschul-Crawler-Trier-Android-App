package xyz.nulldev.huandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ShimUtil {
    // From: https://github.com/qhm123/POI-Android/blob/master/ppt/src/com/poi/poiandroid/Utils.java
    public static synchronized Bitmap decodeSampledBitmapFromFile(String filename,
                                                                  int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    // From: https://github.com/qhm123/POI-Android/blob/master/ppt/src/com/poi/poiandroid/Utils.java
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    // From: https://github.com/qhm123/POI-Android/blob/master/ppt/src/com/poi/poiandroid/Utils.java
    public static String fileBytesShow(float size) {
        long kb = 1024;
        long mb = (kb * 1024);
        long gb = (mb * 1024);
        if (size < kb) {
            return String.format("%d B", (int) size);
        } else if (size < mb) {
            return String.format("%.2f KB", size / kb); // ??????????????????
        } else if (size < gb) {
            return String.format("%.2f MB", size / mb);
        } else {
            return String.format("%.2f GB", size / gb);
        }
    }
}
