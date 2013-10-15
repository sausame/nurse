package com.ankh.nurse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

public class PersonalDailyInformationDetailActivity extends Activity {

	private static final String TAG = "PDIDetail";
	public static final String MESSAGE = TAG;

	private PersonalDailyInformation.DetailInformation mInformation = null;

	private EditText mEditText;
	private ImageButton mImageButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_daily_information_detail);

		mEditText = (EditText) findViewById(R.id.detail);
		mImageButton = (ImageButton) findViewById(R.id.image);
		mImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onImageButtonClicked();
			}
		});

		load();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.personal_daily_information_detail,
				menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_cancel:
			onActionCancel();
			return true;
		case R.id.action_delete:
			onActionDelete();
			return true;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void onActionCancel() {
		finish();
	}

	private void onActionDelete() {
		Bundle bundle = new Bundle();
		bundle.putSerializable(MESSAGE, null);

		Intent intent = new Intent();
		intent.putExtras(bundle);

		setResult(RESULT_OK, intent);

		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			save();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void onImageButtonClicked() {
		if (mInformation.attachmentPath.length() > 0) {
			// View
		} else {
			// Select a picture.
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent, 0);
		}
	}

	private boolean savePicture(Bitmap bitmap) {
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
			Log.v(TAG, "SD card is not avaiable/writeable right now.");
			return false;
		}

		FileOutputStream b = null;
		File file = new File("/sdcard/myImage/");
		file.mkdirs();

		String str = null;
		Date date = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		date = new Date();
		str = format.format(date);
		String fileName = "/sdcard/myImage/" + str + ".jpg";
		try {
			b = new FileOutputStream(fileName);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				b.flush();
				b.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		mInformation.attachmentPath = fileName;

		setViews();

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			savePicture((Bitmap) data.getExtras().get("data"));
		}
	}

	private void setViews() {
		if (mInformation.description.length() > 0) {
			mEditText.setText(mInformation.description);
		}

		if (mInformation.attachmentPath.length() > 0) {
			mImageButton.setImageDrawable(Drawable
					.createFromPath(mInformation.attachmentPath));
		} else {
			mImageButton.setBackgroundResource(R.drawable.ic_launcher);
		}
	}

	private void load() {
		mInformation = (PersonalDailyInformation.DetailInformation) getIntent()
				.getSerializableExtra(MESSAGE);

		if (mInformation == null) {
			mInformation = new PersonalDailyInformation.DetailInformation();
		}

		setViews();
	}

	private void save() {
		mInformation.description = mEditText.getText().toString();

		Bundle bundle = new Bundle();
		bundle.putSerializable(MESSAGE, mInformation);

		Intent intent = new Intent();
		intent.putExtras(bundle);

		setResult(RESULT_OK, intent);
	}
}
