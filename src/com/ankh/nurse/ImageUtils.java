package com.ankh.nurse;

import java.io.IOException;
import java.lang.ref.SoftReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.widget.ImageView;

public class ImageUtils {
	public final static void setImageView(ImageView view, String path) {
		Bitmap bitmap = DecodeBitmap(path, 256 * 256);
		int degree = getExifOrientation(path);

		if (0 == degree) {
			view.setImageBitmap(bitmap);
		} else {
			Matrix matrix = new Matrix();
			matrix.setRotate(90);
			Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
					bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			view.setImageBitmap(newBitmap);
		}
		
		// SoftReference<Bitmap> b = new SoftReference<Bitmap>(newBitmap);
		// view.setImageBitmap(b.get());
	}

	public static int getExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filepath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				// We only recognize a subset of orientation tag values.
				switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
				}

			}
		}
		return degree;
	}

	public final static Bitmap DecodeBitmap(String BitmapPath,
			int maxNumOfPixels) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(BitmapPath, opts);

		opts.inSampleSize = computeSampleSize(opts, -1, maxNumOfPixels);
		opts.inJustDecodeBounds = false;

		Bitmap bmp = null;
		try {
			bmp = BitmapFactory.decodeFile(BitmapPath, opts);
		} catch (OutOfMemoryError err) {
			Log.i("ERROR", "" + err);
			return null;
		}

		return bmp;
	}

	private final static int computeInitialSampleSize(
			BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	private static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

}
