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
import android.util.Log;
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

	private ArrayList<ArrayList<PersonalDailyInformation>> mData = null;
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

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case R.id.action_add:
				onActionAddResult((PersonalDailyInformation) data
						.getSerializableExtra(PersonalDailyInformationActivity.MESSAGE));
				break;
			default:
				break;
			}
		}
	}

	private void onActionAdd() {
		Intent intent = new Intent(this, PersonalDailyInformationActivity.class);
		startActivityForResult(intent, R.id.action_add);
	}

	private void onActionAddResult(PersonalDailyInformation infor) {
		mManager.addPersonalDailyInformation(infor);
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

		mData = new ArrayList<ArrayList<PersonalDailyInformation>>();
	}

	public void setData() {
		mData.clear();

		Date lastDate = null;
		PersonalDailyInformation infor;
		while (null != (infor = mManager.getPersonalDailyInformation())) {
			ArrayList<PersonalDailyInformation> oneDateStatus;
			if (lastDate != null && infor.isSameDay(lastDate)) {
				oneDateStatus = mData.get(mData.size() - 1);
			} else {
				oneDateStatus = new ArrayList<PersonalDailyInformation>();
				mData.add(oneDateStatus);
			}

			oneDateStatus.add(infor);
			lastDate = infor.whichDay;
		}

		mAdapter.notifyDataSetChanged();
	}

	public class PersonalDailyInformationAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<ArrayList<PersonalDailyInformation>> mData = null;

		public PersonalDailyInformationAdapter(Context context,
				ArrayList<ArrayList<PersonalDailyInformation>> data) {
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

		@Override
		public View getView(int position, View view, ViewGroup arg2) {
			if (position < 0 || position >= getCount()) {
				return null;
			}

			DateStatusViewGroup viewGroup = null;
			int num = mData.get(position).size();

			if (view != null) {
				viewGroup = (DateStatusViewGroup) view.getTag();
				if (viewGroup.mStatusViewGroupList.size() != num + 1) {
					// The number is changed.
					viewGroup = null;
				}
			}

			if (viewGroup == null) {
				LayoutInflater factory = LayoutInflater.from(mContext);
				view = factory.inflate(R.layout.listitem_date_status, null);

				viewGroup = new DateStatusViewGroup();

				viewGroup.mDate = (TextView) view.findViewById(R.id.date);
				viewGroup.mStatusLayout = (LinearLayout) view
						.findViewById(R.id.layout);

				for (int i = 0; i < num + 1; i++) {
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
			}

			showItemInfos(position, viewGroup);

			return view;
		}

		private void showItemInfos(final int position,
				DateStatusViewGroup viewGroup) {

			ArrayList<PersonalDailyInformation> currentState = mData
					.get(position);

			PersonalDailyInformation infor = currentState.get(0);

			viewGroup.mDate.setText(getDay(infor.whichDay));

			int num = mData.get(position).size();
			for (int i = 0; i < num; i++) {
				final int offset = i;

				OneStatusViewGroup object = viewGroup.mStatusViewGroupList
						.get(offset);

				infor = currentState.get(offset);

				object.mButton.setText(infor.name);
				object.mButton
						.setBackgroundResource(getBackgroundResource(infor.level));

				object.mButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickTextButton(position, offset);
					}
				});
			}

			OneStatusViewGroup object = viewGroup.mStatusViewGroupList.get(num);

			object.mButton.setVisibility(View.GONE);

			object.mImage.setVisibility(View.VISIBLE);
			object.mImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickImageButton(position);
				}
			});
		}

		private int getBackgroundResource(int level) {
			return mResIDGroupOfBackground[level % 5];
		}

		private void onClickImageButton(final int position) {
			// Toast.makeText(mContext, "Image " + position, Toast.LENGTH_SHORT)
			// .show();
			mManager.addPersonalDailyInformation(PersonalDailyInformation
					.createRandomPersonalDailyInformation());
		}

		private void onClickTextButton(final int position, final int offset) {
			// Toast.makeText(mContext, "Text " + position + ", " + offset,
			// Toast.LENGTH_SHORT).show();
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
}
