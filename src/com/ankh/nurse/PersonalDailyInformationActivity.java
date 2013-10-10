package com.ankh.nurse;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TimePicker;
import android.content.Intent;
import android.view.Menu;

public class PersonalDailyInformationActivity extends Activity {

	private static final String TAG = "PDIActivity";
	public static final String MESSAGE = TAG;

	private PersonalDailyInformation mPersonalDailyInformation = null;

	private EditText mNameEditText;
	private RatingBar mLevelRatingBar;

	private EditText showDate = null;
	private Button pickDate = null;
	private EditText showTime = null;
	private Button pickTime = null;

	private static final int SHOW_DATAPICK = 0;
	private static final int DATE_DIALOG_ID = 1;
	private static final int SHOW_TIMEPICK = 2;
	private static final int TIME_DIALOG_ID = 3;

	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_daily_information);

		initializeViews();

		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);

		setDateTime();
		setTimeOfDay();

		load();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.personal_daily_information, menu);
		return true;
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
	}

	private void load() {
		mPersonalDailyInformation = (PersonalDailyInformation) getIntent()
				.getSerializableExtra(MESSAGE);

		if (mPersonalDailyInformation == null) {
			mPersonalDailyInformation = new PersonalDailyInformation();
		}
	}

	private void save() {
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, mYear);
		c.set(Calendar.MONTH, mMonth);
		c.set(Calendar.DAY_OF_MONTH, mDay);

		c.set(Calendar.HOUR_OF_DAY, mHour);
		c.set(Calendar.MINUTE, mMinute);

		mPersonalDailyInformation.whichDay = c.getTime();
		mPersonalDailyInformation.name = mNameEditText.getText().toString();
		mPersonalDailyInformation.level = (int) mLevelRatingBar.getRating();

		Bundle bundle = new Bundle();
		bundle.putSerializable(MESSAGE, mPersonalDailyInformation);

		Intent intent = new Intent();
		intent.putExtras(bundle);

		setResult(RESULT_OK, intent);
	}

	/**
	 * 初始化控件和UI视图
	 */
	private void initializeViews() {

		mNameEditText = (EditText) findViewById(R.id.name);
		mLevelRatingBar = (RatingBar) findViewById(R.id.level);

		pickDate = (Button) findViewById(R.id.pickdate);
		showTime = (EditText) findViewById(R.id.showtime);
		pickTime = (Button) findViewById(R.id.picktime);

		showDate = (EditText) findViewById(R.id.showdate);
		pickDate = (Button) findViewById(R.id.pickdate);
		showTime = (EditText) findViewById(R.id.showtime);
		pickTime = (Button) findViewById(R.id.picktime);

		pickDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Message msg = new Message();
				if (pickDate.equals((Button) v)) {
					msg.what = PersonalDailyInformationActivity.SHOW_DATAPICK;
				}
				PersonalDailyInformationActivity.this.dateandtimeHandler
						.sendMessage(msg);
			}
		});

		pickTime.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Message msg = new Message();
				if (pickTime.equals((Button) v)) {
					msg.what = PersonalDailyInformationActivity.SHOW_TIMEPICK;
				}
				PersonalDailyInformationActivity.this.dateandtimeHandler
						.sendMessage(msg);
			}
		});

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
		showDate.setText(new StringBuilder().append(mYear).append("-")
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

	/**
	 * 设置时间
	 */
	private void setTimeOfDay() {
		final Calendar c = Calendar.getInstance();
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);
		updateTimeDisplay();
	}

	/**
	 * 更新时间显示
	 */
	private void updateTimeDisplay() {
		showTime.setText(new StringBuilder().append(mHour).append(":")
				.append((mMinute < 10) ? "0" + mMinute : mMinute));
	}

	/**
	 * 时间控件事件
	 */
	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;

			updateTimeDisplay();
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute,
					true);
		}

		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DATE_DIALOG_ID:
			((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
			break;
		case TIME_DIALOG_ID:
			((TimePickerDialog) dialog).updateTime(mHour, mMinute);
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
			case PersonalDailyInformationActivity.SHOW_DATAPICK:
				showDialog(DATE_DIALOG_ID);
				break;
			case PersonalDailyInformationActivity.SHOW_TIMEPICK:
				showDialog(TIME_DIALOG_ID);
				break;
			}
		}

	};
}
