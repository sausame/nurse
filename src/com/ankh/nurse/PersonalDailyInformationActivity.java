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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;

public class PersonalDailyInformationActivity extends Activity {

	private static final String TAG = "PDIActivity";
	public static final String MESSAGE = TAG;
	public static final String ID = "ID";

	private PersonalDailyInformation mPersonalDailyInformation = null;
	private int mID = 0;

	private PersonalDailyDetailInformationAdapter mAdapter;
	private ListView mDetailList;

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

		mAdapter = new PersonalDailyDetailInformationAdapter(this,
				mPersonalDailyInformation);
		mDetailList = (ListView) findViewById(R.id.list);
		mDetailList.setAdapter(mAdapter);
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
		if (0 == mPersonalDailyInformation.name.length()) {
			Toast.makeText(this, R.string.no_name_message, Toast.LENGTH_LONG)
					.show();
			return;
		}

		Bundle bundle = new Bundle();
		bundle.putSerializable(MESSAGE, mPersonalDailyInformation);
		bundle.putInt(ID, mID);

		Intent intent = new Intent();
		intent.putExtras(bundle);

		setResult(RESULT_OK, intent);
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

	private void setDate(int year, int monthOfYear, int dayOfMonth) {
		mYear = year;
		mMonth = monthOfYear;
		mDay = dayOfMonth;

		Calendar c = new GregorianCalendar();
		c.setTime(mPersonalDailyInformation.whichDay);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, monthOfYear);
		c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

		mPersonalDailyInformation.whichDay = c.getTime();

		mAdapter.notifyDataSetChanged();
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			setDate(year, monthOfYear, dayOfMonth);
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

	/*
	 * public void onItemSelected(AdapterView<?> listView, View view, int
	 * position, long id) { if (position == 1) { //
	 * listView.setItemsCanFocus(true);
	 * 
	 * // Use afterDescendants, because I don't want the ListView to steal //
	 * focus
	 * listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
	 * mAdapter.requestFocus(); // myEditText.requestFocus(); } else { if
	 * (!listView.isFocused()) { // listView.setItemsCanFocus(false);
	 * 
	 * // Use beforeDescendants so that the EditText doesn't re-take // focus
	 * listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
	 * listView.requestFocus(); } } }
	 * 
	 * public void onNothingSelected(AdapterView<?> listView) { // This happens
	 * when you start scrolling, so we need to prevent it from // staying // in
	 * the afterDescendants mode if the EditText was focused
	 * listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS); }
	 */
	public class PersonalDailyDetailInformationAdapter extends BaseAdapter {
		private Context mContext;
		private PersonalDailyInformation mPersonalDailyInformation = null;

		public PersonalDailyDetailInformationAdapter(Context context,
				PersonalDailyInformation infor) {
			mContext = context;
			mPersonalDailyInformation = infor;
		}

		// ====================================================================
		@Override
		public int getCount() {
			return mPersonalDailyInformation.detailList == null ? 1
					: mPersonalDailyInformation.detailList.size() + 1;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup arg2) {
			if (position < 0 || position >= getCount()) {
				return null;
			}

			if (0 == position) {
				return getHeaderView(view, arg2);
			}

			return getItemView(position, view, arg2);
		}

		// ====================================================================
		private View getHeaderView(View view, ViewGroup arg2) {
			ItemHeaderViewGroup viewGroup = null;

			if (null == view) {
				LayoutInflater factory = LayoutInflater.from(mContext);
				view = factory
						.inflate(
								R.layout.listitem_person_daily_information_header,
								null);

				viewGroup = new ItemHeaderViewGroup();
				viewGroup.mDateTextView = (TextView) view
						.findViewById(R.id.date_text);
				viewGroup.mChooseDateButton = (Button) view
						.findViewById(R.id.choose_date);
				viewGroup.mNameEditText = (EditText) view
						.findViewById(R.id.name);
				viewGroup.mLevelRatingBar = (RatingBar) view
						.findViewById(R.id.level);

				mItemHeaderViewGroup = viewGroup;

				view.setTag(viewGroup);
			} else {
				viewGroup = (ItemHeaderViewGroup) view.getTag();
			}

			showItemHeader(viewGroup);

			return view;
		}

		private String getDay(Date date) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = formatter.format(date);
			return dateString;
		}

		private void showItemHeader(final ItemHeaderViewGroup viewGroup) {
			viewGroup.mDateTextView
					.setText(getDay(mPersonalDailyInformation.whichDay));
			viewGroup.mNameEditText.setText(mPersonalDailyInformation.name);
			viewGroup.mNameEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable arg0) {
					mPersonalDailyInformation.name = arg0.toString();
				}
			});

			viewGroup.mLevelRatingBar
					.setRating(mPersonalDailyInformation.level);
			viewGroup.mLevelRatingBar
					.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
						@Override
						public void onRatingChanged(RatingBar ratingBar,
								float rating, boolean fromUser) {
							mPersonalDailyInformation.level = (int) rating;
						}
					});

			viewGroup.mChooseDateButton
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Message msg = new Message();
							if (viewGroup.mChooseDateButton.equals((Button) v)) {
								msg.what = PersonalDailyInformationActivity.SHOW_CHOOSE_DATE_DIALOG;
							}
							PersonalDailyInformationActivity.this.dateandtimeHandler
									.sendMessage(msg);
						}
					});
		}

		public void requestFocus() {
			mItemHeaderViewGroup.mNameEditText.requestFocus();
		}

		private class ItemHeaderViewGroup {
			private EditText mNameEditText;
			private RatingBar mLevelRatingBar;

			private TextView mDateTextView;
			private Button mChooseDateButton;
		}

		private ItemHeaderViewGroup mItemHeaderViewGroup = null;

		// ====================================================================
		public View getItemView(int position, View view, ViewGroup arg2) {

			ItemViewGroup viewGroup = null;

			if (null == view) {
				LayoutInflater factory = LayoutInflater.from(mContext);
				viewGroup = new ItemViewGroup();

				view = factory.inflate(
						R.layout.listitem_person_daily_information, null);

				viewGroup.mTextView = (TextView) view.findViewById(R.id.text);
				viewGroup.mImageButton = (ImageButton) view
						.findViewById(R.id.image);

				view.setTag(viewGroup);
			} else {
				viewGroup = (ItemViewGroup) view.getTag();
			}

			showItem(position - 1, viewGroup);

			return view;
		}

		private void showItem(final int position, ItemViewGroup viewGroup) {
		}

		private class ItemViewGroup {
			public TextView mTextView;
			public ImageButton mImageButton;
		}
	}
}
