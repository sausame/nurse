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

	private ArrayList<ArrayList<String>> mData = null;
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
		switch (requestCode) {
		case R.id.action_add:
			onActionAddResult();
			break;
		default:
			break;
		}
	}

	private void onActionAdd() {
        Intent intent = new Intent(this, PersonalDailyInformationActivity.class);
        startActivityForResult(intent, R.id.action_add);
	}

	private void onActionAddResult() {
		
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

		mData = new ArrayList<ArrayList<String>>();

		PersonalDailyInformation infor;
		while (null != (infor = mManager.getPersonalDailyInformation())) {
			ArrayList<String> oneDateStatus = new ArrayList<String>();

			oneDateStatus.add(getDay(infor.whichDay));
			oneDateStatus.add("" + infor.level);

			mData.add(oneDateStatus);
		}
	}

	public class PersonalDailyInformationAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<ArrayList<String>> mData = null;

		public PersonalDailyInformationAdapter(Context context,
				ArrayList<ArrayList<String>> data) {
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

			DateStatusViewGroup myClass = null;
			// Log.i(TAG, "NO." + position + ": " + view + ", " + myClass);
			if (view == null /* || the number changed */) {
				LayoutInflater factory = LayoutInflater.from(mContext);
				view = factory.inflate(R.layout.listitem_date_status, null);

				myClass = new DateStatusViewGroup();

				myClass.mDate = (TextView) view.findViewById(R.id.date);
				myClass.mStatusLayout = (LinearLayout) view
						.findViewById(R.id.layout);

				int num = mData.get(position).size();
				for (int i = 0; i < num; i++) {
					OneStatusViewGroup object = new OneStatusViewGroup();
					View viewOneStatus = factory.inflate(
							R.layout.layout_one_status, myClass.mStatusLayout,
							false);

					object.mImage = (ImageButton) viewOneStatus
							.findViewById(R.id.image);
					object.mButton = (Button) viewOneStatus
							.findViewById(R.id.button);

					myClass.mStatusViewGroupList.add(object);

					// Log.i(TAG, "" + i + ", " + viewOneStatus);
					myClass.mStatusLayout.addView(viewOneStatus, i);
				}

				view.setTag(myClass);
			} else {
				myClass = (DateStatusViewGroup) view.getTag();
			}

			// Log.i(TAG, "NO." + position + ": " + view + ", " + myClass);

			showItemInfos(position, myClass);

			return view;
		}

		private void showItemInfos(final int position,
				DateStatusViewGroup myClass) {
			if (myClass.mStatusViewGroupList.size() != mData.get(position)
					.size()) {
				return;
			}

			ArrayList<String> currentState = mData.get(position);
			myClass.mDate.setText(currentState.get(0));

			int num = mData.get(position).size();
			Log.i(TAG,
					"" + position + ", " + myClass.mStatusViewGroupList.size()
							+ ", " + num);
			for (int i = 1; i < num; i++) {
				final int offset = i;

				OneStatusViewGroup object = myClass.mStatusViewGroupList
						.get(i - 1);
				object.mButton.setText(currentState.get(i));
				object.mButton.setBackgroundResource(getBackgroundResource(
						position, offset));

				object.mButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickTextButton(position, offset);
					}
				});
			}

			OneStatusViewGroup object = myClass.mStatusViewGroupList
					.get(num - 1);

			object.mButton.setVisibility(View.GONE);

			object.mImage.setVisibility(View.VISIBLE);
			object.mImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickImageButton(position);
				}
			});
		}

		private int getBackgroundResource(final int position, final int offset) {
			int color = Integer.parseInt(mData.get(position).get(offset)) % 5;
			return mResIDGroupOfBackground[color];
		}

		private void onClickImageButton(final int position) {
//			Toast.makeText(mContext, "Image " + position, Toast.LENGTH_SHORT)
//					.show();
			mManager.addPersonalDailyInformation(PersonalDailyInformation
					.createRandomPersonalDailyInformation());
		}

		private void onClickTextButton(final int position, final int offset) {
//			Toast.makeText(mContext, "Text " + position + ", " + offset,
//					Toast.LENGTH_SHORT).show();
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
