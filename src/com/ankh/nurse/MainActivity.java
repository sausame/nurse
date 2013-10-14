package com.ankh.nurse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";

	private static final int ADD = 0;
	private static final int MODIFY = 1;
	private static final int DELETE = 2;

	private ArrayList<ArrayList<StatusItem>> mData = null;
	private PersonalDailyInformationAdapter mAdapter;
	private ListView mDateStatusList;

	private PersonalDailyInformationManager mManager = new PersonalDailyInformationManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initData();

		mAdapter = new PersonalDailyInformationAdapter(this, mData);
		mDateStatusList = (ListView) findViewById(R.id.list);
		mDateStatusList.setAdapter(mAdapter);

		setData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			onActionAdd();
			return true;
		case R.id.action_settings:
			onActionSettings();
			return true;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "" + requestCode + ", " + resultCode + ", " + data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case ADD:
				onActionAddResult((PersonalDailyInformation) data
						.getSerializableExtra(PersonalDailyInformationActivity.MESSAGE));
				break;
			case MODIFY:
				onActionModifyResult(
						data.getIntExtra(PersonalDailyInformationActivity.ID, 0),
						(PersonalDailyInformation) data
								.getSerializableExtra(PersonalDailyInformationActivity.MESSAGE));
				break;
			case DELETE:
				break;
			default:
				break;
			}
		}
	}

	private void onActionAdd() {
		onActionAdd(-1);
	}

	private void onActionAdd(final int position) {
		Intent intent = new Intent(this, PersonalDailyInformationActivity.class);

		if (position >= 0) {
			Calendar c1 = new GregorianCalendar();
			Calendar c2 = new GregorianCalendar();

			c1.setTime(mData.get(position).get(0).infor.whichDay);

			c2.set(Calendar.YEAR, c1.get(Calendar.YEAR));
			c2.set(Calendar.MONTH, c1.get(Calendar.MONTH));
			c2.set(Calendar.DAY_OF_MONTH, c1.get(Calendar.DAY_OF_MONTH));

			PersonalDailyInformation infor = new PersonalDailyInformation();
			infor.whichDay = c2.getTime();

			Bundle bundle = new Bundle();
			bundle.putSerializable(PersonalDailyInformationActivity.MESSAGE, infor);
			intent.putExtras(bundle);
		}

		startActivityForResult(intent, ADD);
	}

	private void onActionModify(final int id, final int position,
			final int offset) {
		PersonalDailyInformation infor = mData.get(position).get(offset).infor;

		Intent intent = new Intent(this, PersonalDailyInformationActivity.class);

		Bundle bundle = new Bundle();
		bundle.putSerializable(PersonalDailyInformationActivity.MESSAGE, infor);
		bundle.putSerializable(PersonalDailyInformationActivity.ID, id);
		intent.putExtras(bundle);

		startActivityForResult(intent, MODIFY);
	}

	private void onActionAddResult(PersonalDailyInformation infor) {
		mManager.add(infor);
		mManager.save();

		setData();
	}

	private void onActionModifyResult(int id, PersonalDailyInformation infor) {
		if (infor != null) {
			mManager.modify(id, infor);
		} else {
			mManager.del(id);
		}

		mManager.save();

		setData();
	}

	private void onActionSettings() {
	}

	private String getDay(int diff) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, diff);
		return getDay(calendar.getTime());
	}

	private String getDay(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");
		String dateString = formatter.format(date);
		return dateString;
	}

	public void initData() {
		mManager.setPathname("/sdcard/0.json");
		mManager.load();

		mData = new ArrayList<ArrayList<StatusItem>>();
	}

	public void setData() {
		Log.i(TAG, mManager.toString());

		mData.clear();
		mManager.reset();

		Date lastDate = null;
		PersonalDailyInformation infor;

		for (int id = 0; null != (infor = mManager
				.getPersonalDailyInformation()); id++) {
			ArrayList<StatusItem> oneDateStatus;
			if (lastDate != null && infor.isSameDay(lastDate)) {
				oneDateStatus = mData.get(mData.size() - 1);
			} else {
				oneDateStatus = new ArrayList<StatusItem>();
				mData.add(oneDateStatus);
			}

			oneDateStatus.add(new StatusItem(id, infor));
			lastDate = infor.whichDay;
		}

		mAdapter.notifyDataSetChanged();
	}

	public class PersonalDailyInformationAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<ArrayList<StatusItem>> mData = null;

		public PersonalDailyInformationAdapter(Context context,
				ArrayList<ArrayList<StatusItem>> data) {
			mContext = context;
			mData = data;
		}

		// ====================================================================
		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		private final static int MAX_IN_AN_ITEM = 6;

		@Override
		public View getView(int position, View view, ViewGroup arg2) {
			if (position < 0 || position >= getCount()) {
				return null;
			}

			DateStatusViewGroup viewGroup = null;
			if (view == null) {
				LayoutInflater factory = LayoutInflater.from(mContext);
				view = factory.inflate(R.layout.listitem_date_status, null);

				viewGroup = new DateStatusViewGroup();

				viewGroup.mDate = (TextView) view.findViewById(R.id.date);
				viewGroup.mStatusLayout = (LinearLayout) view
						.findViewById(R.id.layout);

				for (int i = 0; i < MAX_IN_AN_ITEM; i++) {
					OneStatusViewGroup object = new OneStatusViewGroup();
					View viewOneStatus = factory.inflate(
							R.layout.layout_one_status,
							viewGroup.mStatusLayout, false);

					object.mImage = (ImageButton) viewOneStatus
							.findViewById(R.id.image);
					object.mButton = (Button) viewOneStatus
							.findViewById(R.id.button);

					viewGroup.mStatusViewGroupList.add(object);

					viewGroup.mStatusLayout.addView(viewOneStatus, i);
				}

				view.setTag(viewGroup);
			} else {
				viewGroup = (DateStatusViewGroup) view.getTag();
			}

			showItemInfos(position, viewGroup);

			return view;
		}

		private void showItemInfos(final int position,
				DateStatusViewGroup viewGroup) {

			ArrayList<StatusItem> currentState = mData.get(position);

			PersonalDailyInformation infor = currentState.get(0).infor;

			viewGroup.mDate.setText(getDay(infor.whichDay));

			int num = mData.get(position).size();
			if (num >= MAX_IN_AN_ITEM) {
				num = MAX_IN_AN_ITEM - 1;
			}

			OneStatusViewGroup object;
			int i;

			for (i = 0; i < num; i++) {
				final int offset = i;
				final int id = currentState.get(offset).id;

				object = viewGroup.mStatusViewGroupList.get(offset);

				infor = currentState.get(offset).infor;

				object.mButton.setVisibility(View.VISIBLE);
				object.mButton.setText(infor.name);
				object.mButton
						.setBackgroundResource(getBackgroundResource(infor.level));

				object.mButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickTextButton(id, position, offset);
					}
				});

				object.mImage.setVisibility(View.GONE);
			}

			object = viewGroup.mStatusViewGroupList.get(i++);
			object.mButton.setVisibility(View.GONE);
			object.mImage.setVisibility(View.VISIBLE);
			object.mImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickImageButton(position);
				}
			});

			for (; i < MAX_IN_AN_ITEM; i++) {
				object = viewGroup.mStatusViewGroupList.get(i);
				object.mButton.setVisibility(View.GONE);
				object.mImage.setVisibility(View.GONE);
			}
		}

		private int getBackgroundResource(int level) {
			return mResIDGroupOfBackground[level % 5];
		}

		private void onClickImageButton(final int position) {
			onActionAdd(position);
		}

		private void onClickTextButton(final int id, final int position,
				final int offset) {
			onActionModify(id, position, offset);
		}

		// ====================================================================
		private class OneStatusViewGroup {
			public ImageButton mImage;
			public Button mButton;
		}

		private class DateStatusViewGroup {
			public TextView mDate;
			public LinearLayout mStatusLayout;
			public ArrayList<OneStatusViewGroup> mStatusViewGroupList = new ArrayList<OneStatusViewGroup>();
		}

		private final int mResIDGroupOfBackground[] = {
				R.drawable.list_selector_holo_green,
				R.drawable.list_selector_holo_blue,
				R.drawable.list_selector_holo_yellow,
				R.drawable.list_selector_holo_orange,
				R.drawable.list_selector_holo_red };
	}

	private class StatusItem {
		public StatusItem(int id, PersonalDailyInformation infor) {
			this.id = id;
			this.infor = infor;
		}

		int id;
		PersonalDailyInformation infor;
	}
}
