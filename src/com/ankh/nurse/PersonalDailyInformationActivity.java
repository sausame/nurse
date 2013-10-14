package com.ankh.nurse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.content.Intent;
import android.view.Menu;

public class PersonalDailyInformationActivity extends Activity {

	private static final String TAG = "PDIActivity";
	public static final String MESSAGE = TAG;
	public static final String ID = "ID";

	private PersonalDailyInformation mPersonalDailyInformation = null;
	private int mID = 0;

	private EditText mNameEditText;
	private RatingBar mLevelRatingBar;

	private TextView mDateTextView = null;
	private Button mChooseDateButton = null;

	private static final int SHOW_CHOOSE_DATE_DIALOG = 0;
	private static final int DATE_DIALOG_ID = 1;

	private int mYear;
	private int mMonth;
	private int mDay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_daily_information);

		load();

		initViews();
		setViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.personal_daily_information, menu);
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
		bundle.putInt(ID, mID);

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

	private void setViews() {
		mDateTextView.setText(getDay(mPersonalDailyInformation.whichDay));
		mNameEditText.setText(mPersonalDailyInformation.name);
		mLevelRatingBar.setRating(mPersonalDailyInformation.level);
	}

	private String getDay(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(date);
		return dateString;
	}

	private void load() {
		mPersonalDailyInformation = (PersonalDailyInformation) getIntent()
				.getSerializableExtra(MESSAGE);

		mID = getIntent().getIntExtra(ID, 0);

		if (mPersonalDailyInformation == null) {
			mPersonalDailyInformation = new PersonalDailyInformation();
			mPersonalDailyInformation.whichDay = new Date();
		}

		Calendar c = new GregorianCalendar();

		c.setTime(mPersonalDailyInformation.whichDay);

		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
	}

	private void save() {
		Calendar c = new GregorianCalendar();
		c.setTime(mPersonalDailyInformation.whichDay);
		c.set(Calendar.YEAR, mYear);
		c.set(Calendar.MONTH, mMonth);
		c.set(Calendar.DAY_OF_MONTH, mDay);

		mPersonalDailyInformation.whichDay = c.getTime();
		mPersonalDailyInformation.name = mNameEditText.getText().toString();
		mPersonalDailyInformation.level = (int) mLevelRatingBar.getRating();

		Bundle bundle = new Bundle();
		bundle.putSerializable(MESSAGE, mPersonalDailyInformation);
		bundle.putInt(ID, mID);

		Intent intent = new Intent();
		intent.putExtras(bundle);

		setResult(RESULT_OK, intent);
	}

	private void initViews() {
		mDateTextView = (TextView) findViewById(R.id.date_text);
		mChooseDateButton = (Button) findViewById(R.id.choose_date);

		mChooseDateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Message msg = new Message();
				if (mChooseDateButton.equals((Button) v)) {
					msg.what = PersonalDailyInformationActivity.SHOW_CHOOSE_DATE_DIALOG;
				}
				PersonalDailyInformationActivity.this.dateandtimeHandler
						.sendMessage(msg);
			}
		});

		mNameEditText = (EditText) findViewById(R.id.name);
		mLevelRatingBar = (RatingBar) findViewById(R.id.level);

		((Button) findViewById(R.id.detail))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						onDetailClicked(0);
					}
				});
	}

	private void onDetailClicked(int position) {
		Intent intent = new Intent(this,
				PersonalDailyInformationDetailActivity.class);

		if (mPersonalDailyInformation.isDetailExist(position)) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(
					PersonalDailyInformationDetailActivity.MESSAGE,
					mPersonalDailyInformation.getDetail(position));

			intent.putExtras(bundle);
		}

		startActivityForResult(intent, position);
	}

	private void onDetailChanged(int position,
			PersonalDailyInformation.DetailInformation infor) {
		if (mPersonalDailyInformation.isDetailExist(position)) {
			mPersonalDailyInformation.setDetail(position, infor);
		} else {
			mPersonalDailyInformation.addDetail(infor);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		onDetailChanged(
				requestCode/* position */,
				(PersonalDailyInformation.DetailInformation) data
						.getSerializableExtra(PersonalDailyInformationDetailActivity.MESSAGE));
	}

	/**
	 * 设置日期
	 */
	private void setDateTime() {
		final Calendar c = Calendar.getInstance();

		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		updateDateDisplay();
	}

	/**
	 * 更新日期显示
	 */
	private void updateDateDisplay() {
		mDateTextView.setText(new StringBuilder().append(mYear).append("-")
				.append((mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1))
				.append("-").append((mDay < 10) ? "0" + mDay : mDay));
	}

	/**
	 * 日期控件的事件
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;

			updateDateDisplay();
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);
		}

		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DATE_DIALOG_ID:
			((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
			break;
		}
	}

	/**
	 * 处理日期和时间控件的Handler
	 */
	Handler dateandtimeHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PersonalDailyInformationActivity.SHOW_CHOOSE_DATE_DIALOG:
				showDialog(DATE_DIALOG_ID);
				break;
			}
		}

	};
}
