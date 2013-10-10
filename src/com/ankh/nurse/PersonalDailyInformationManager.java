package com.ankh.nurse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PersonalDailyInformationManager {

	private static final String TAG = "PersonalDailyInformationManager";

	private JSONArray mJsonArray = null;
	private int mCurrentIndex = 0;
	private String mPathname;

	public void setPathname(String path) {
		mPathname = path;
	}

	public void load() {
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(mPathname)));

			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();

			setPersonalDailyInformationBuffer(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void save() {
		if (null == mJsonArray || 0 == mJsonArray.length()) {
			Log.e(TAG, "Nothing is needed to save.");
			return;
		}

		try {
			String jsonString = mJsonArray.toString();
			OutputStreamWriter osw = new OutputStreamWriter(
					new FileOutputStream(mPathname));

			osw.write(jsonString, 0, jsonString.length());

			osw.flush();
			osw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setPersonalDailyInformationBuffer(String buffer) {
		try {
			mJsonArray = new JSONArray(buffer);
			mCurrentIndex = 0;

			if (0 == mJsonArray.length()) {
				mJsonArray = null;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void reset() {
		mCurrentIndex = 0;
	}

	public void addPersonalDailyInformation(PersonalDailyInformation newInfor) {
		reset();

		PersonalDailyInformation infor = null;
		while (null != (infor = getPersonalDailyInformation())) {
			int diff = infor.compare(newInfor);

			if (diff < 0) {
				continue;
			}

			if (diff > 0) {
				// Append.
				int i = mJsonArray.length() - 1;
				try {
					mJsonArray.put(mJsonArray.getJSONObject(i));
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// Move
				for (; i >= mCurrentIndex - 1; i--) {
					try {
						mJsonArray.put(i + 1, mJsonArray.getJSONObject(i));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				// Insert before.
			} else {
				// Replace.
			}

			// Move the current.
			mCurrentIndex--;
			break;
		}

		try {
			if (mJsonArray == null) {
				mJsonArray = new JSONArray();
			}

			Log.v(TAG, "Add " + mCurrentIndex + ":" + newInfor.toJSONObject());
			mJsonArray.put(mCurrentIndex, newInfor.toJSONObject());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public PersonalDailyInformation getPersonalDailyInformation() {
		if (null == mJsonArray || mCurrentIndex >= mJsonArray.length()) {
			Log.e(TAG, (mJsonArray == null) ? "NO array" : "" + mCurrentIndex
					+ " >= " + mJsonArray.length());
			return null;
		}

		try {
			Log.v(TAG,
					"Try to get " + mCurrentIndex + " in "
							+ mJsonArray.length());
			JSONObject obj = mJsonArray.getJSONObject(mCurrentIndex++);
			return PersonalDailyInformation.parsePersonalDailyInformation(obj);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public PersonalDailyInformation getPersonalDailyInformation(Date whichDay) {
		reset();

		PersonalDailyInformation infor = null;
		while (null != (infor = getPersonalDailyInformation())) {
			int diff = infor.compare(whichDay);

			if (0 == diff) {
				return infor;
			}

			if (diff > 0) {
				break;
			}
		}

		return null;
	}

	public String toString() {
		reset();
		String str = "Pathname: " + mPathname;

		PersonalDailyInformation infor;
		while (null != (infor = getPersonalDailyInformation())) {
			str += "-------------------------------\n";
			str += infor.toString();
		}

		return str;
	}

	public static void test() {
		PersonalDailyInformationManager manager = new PersonalDailyInformationManager();
		manager.setPathname("/sdcard/0.json");

		manager.load();
		Log.i(TAG, manager.toString());

		manager.addPersonalDailyInformation(PersonalDailyInformation
				.createRandomPersonalDailyInformation());

		manager.save();
		Log.i(TAG, manager.toString());
	}
}
