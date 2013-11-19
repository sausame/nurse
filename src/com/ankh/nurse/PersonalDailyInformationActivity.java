package com.ankh.nurse;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.net.Uri;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Menu;

public class PersonalDailyInformationActivity extends Activity implements
		OnItemClickListener {

	private static final String TAG = "PDIActivity";
	public static final String MESSAGE = TAG;
	public static final String ID = "ID";

	private PersonalDailyInformation mPersonalDailyInformation = null;
	private int mID = 0;

	private Spinner mLevelSpinner;
	private LevelAdapter mLevelAdapter;

	private PersonalDailyDetailInformationAdapter mAdapter;
	private SwipeListView mDetailList;

	private static final int SHOW_CHOOSE_DATE_DIALOG = 0;
	private static final int DATE_DIALOG_ID = 1;

	private int mYear;
	private int mMonth;
	private int mDay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FadingActionBarHelper helper = new FadingActionBarHelper()
				.actionBarBackground(R.drawable.ab_background_light)
				.contentLayout(R.layout.activity_personal_daily_information)
				.parallax(false);

		setContentView(helper.createView(this));
		helper.initActionBar(this);

		load();

		mDetailList = (SwipeListView) findViewById(android.R.id.list);
		mDetailList.addHeaderView(getHeaderView(), null, false);

		mAdapter = new PersonalDailyDetailInformationAdapter(this,
				mPersonalDailyInformation);
		mDetailList.setAdapter(mAdapter);
		mDetailList.setOnItemClickListener(this);

		mDetailList.setWindow(getWindow());
		mDetailList.setListViewCallBack(new SwipeListView.ListViewCallBack() {
			@Override
			public void showCannotSwipe() {
			}

			@Override
			public void onChildDismissed(View v, int position) {
				deleteDetail(position);
			}

			@Override
			public boolean canDismissed(View v, int position) {
				return true;
			}

			@Override
			public void undoDismiss() {
				undoDeleteDetail();
			}

			@Override
			public void dismiss(int position) {
			}
		});

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

		case R.id.action_add:
			onActionAdd();
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

	private void onActionAdd() {
		onDetailClicked(mPersonalDailyInformation.getDetailNumber());
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
			Toast.makeText(this, R.string.no_name_message, Toast.LENGTH_SHORT)
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

	private PersonalDailyInformation.DetailInformation mDeletedDetailInfor = new PersonalDailyInformation.DetailInformation();

	private void deleteDetail(int position) {
		position -= mDetailList.getHeaderViewsCount();

		mDeletedDetailInfor.copy(mPersonalDailyInformation.getDetail(position));
		mPersonalDailyInformation.delDetail(position);

		mAdapter.notifyDataSetChanged();
	}

	private void undoDeleteDetail() {
		mPersonalDailyInformation
				.addDetail(new PersonalDailyInformation.DetailInformation(
						mDeletedDetailInfor));

		mAdapter.notifyDataSetChanged();
	}

	private void onDetailChanged(int position,
			PersonalDailyInformation.DetailInformation infor) {
		if (mPersonalDailyInformation.isDetailExist(position)) {
			if (infor != null) {
				mPersonalDailyInformation.setDetail(position, infor);
			} else {
				mPersonalDailyInformation.delDetail(position);
			}
		} else {
			mPersonalDailyInformation.addDetail(infor);
		}

		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "" + requestCode + ", " + resultCode + ", " + data);
		if (resultCode == Activity.RESULT_OK) {
			onDetailChanged(
					requestCode/* position */,
					(PersonalDailyInformation.DetailInformation) data
							.getSerializableExtra(PersonalDailyInformationDetailActivity.MESSAGE));
		}
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
		mDateTextView.setText(getDay(mPersonalDailyInformation.whichDay));
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

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position,
			long id) {
		if (position != 0) {
			onDetailClicked(position - 1);
		}
	}

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
			return mPersonalDailyInformation.getDetailNumber();
		}

		@Override
		public Object getItem(int id) {
			return null;
		}

		@Override
		public long getItemId(int id) {
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup parentView) {
			if (position < 0 || position >= getCount()) {
				return null;
			}

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

			showItem(position, viewGroup);

			return view;
		}

		private void showItem(final int position, ItemViewGroup viewGroup) {
			PersonalDailyInformation.DetailInformation infor = mPersonalDailyInformation
					.getDetail(position);

			viewGroup.mTextView.setText(infor.description);
			final String path = infor.attachmentPath;
			if (path.length() > 0) {
				viewGroup.mImageButton.setVisibility(View.VISIBLE);
				ImageUtils.setImageView(viewGroup.mImageButton, path);

				viewGroup.mImageButton
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								showImage(path);
							}
						});

			} else {
				viewGroup.mImageButton.setVisibility(View.GONE);
			}
		}

		private void showImage(final String path) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
			mContext.startActivity(intent);
		}

		// ====================================================================
		private class ItemViewGroup {
			/* Item */
			private TextView mTextView;
			private ImageButton mImageButton;
		}
	}

	// ====================================================================
	/* Header */
	private EditText mNameEditText;
	private TextView mDateTextView;
	private Button mChooseDateButton;

	private View getHeaderView() {
		LayoutInflater factory = LayoutInflater.from(this);
		View view = factory.inflate(
				R.layout.listitem_person_daily_information_header, null);

		mDateTextView = (TextView) view.findViewById(R.id.date_text);
		mChooseDateButton = (Button) view.findViewById(R.id.choose_date);
		mNameEditText = (EditText) view.findViewById(R.id.name);

		mLevelSpinner = (Spinner) view.findViewById(R.id.spinner);

		showItemHeader();

		return view;
	}

	private String getDay(Date date) {
		return SimpleDateFormat.getDateInstance().format(date);
	}

	private void showItemHeader() {
		mDateTextView.setText(getDay(mPersonalDailyInformation.whichDay));
		mNameEditText.setText(mPersonalDailyInformation.name);
		mNameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable e) {
				mPersonalDailyInformation.name = e.toString();
			}
		});

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

		mLevelAdapter = new LevelAdapter(this);
		mLevelSpinner.setAdapter(mLevelAdapter);
		mLevelSpinner.setSelection(mPersonalDailyInformation.level);
		mLevelSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						mPersonalDailyInformation.level = position;
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
	}

	/*
	 * public void requestFocus() { mNameEditText.requestFocus(); }
	 */

	// ====================================================================
	public class LevelAdapter extends BaseAdapter {
		private Context mContext;

		public LevelAdapter(Context context) {
			mContext = context;
		}

		// ====================================================================
		@Override
		public int getCount() {
			return mResIDGroupOfBackground.length;
		}

		@Override
		public Object getItem(int id) {
			return null;
		}

		@Override
		public long getItemId(int id) {
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup parentView) {
			if (position < 0 || position >= getCount()) {
				return null;
			}

			ItemViewGroup viewGroup = null;

			if (null == view) {
				LayoutInflater factory = LayoutInflater.from(mContext);
				viewGroup = new ItemViewGroup();

				view = factory.inflate(R.layout.listitem_level, null);

				viewGroup.mTextView = (TextView) view.findViewById(R.id.text);
				viewGroup.mImageView = (ImageView) view
						.findViewById(R.id.image);

				view.setTag(viewGroup);
			} else {
				viewGroup = (ItemViewGroup) view.getTag();
			}

			showItem(position, viewGroup);

			return view;
		}

		private void showItem(final int position, ItemViewGroup viewGroup) {
			viewGroup.mTextView.setText(mResIDGroupOfText[position]);
			viewGroup.mImageView
					.setBackgroundResource(mResIDGroupOfBackground[position]);
		}

		// ====================================================================
		private final int mResIDGroupOfBackground[] = {
				R.drawable.list_selector_holo_green,
				R.drawable.list_selector_holo_blue,
				R.drawable.list_selector_holo_yellow,
				R.drawable.list_selector_holo_orange,
				R.drawable.list_selector_holo_red };

		private final int mResIDGroupOfText[] = { R.string.lighter,
				R.string.light, R.string.serious, R.string.more_serious,
				R.string.very_serious };

		// ====================================================================
		private class ItemViewGroup {
			/* Item */
			private TextView mTextView;
			private ImageView mImageView;
		}
	}
}
