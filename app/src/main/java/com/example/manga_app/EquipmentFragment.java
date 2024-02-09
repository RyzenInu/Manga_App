package com.example.manga_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EquipmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EquipmentFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EquipmentFragment() {
        // Required empty public constructor
    }

    public static EquipmentFragment newInstance(String param1, String param2) {
        EquipmentFragment fragment = new EquipmentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private View fragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_equipment, container, false);
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDevices(view);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (isAdded()) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadDeviceValues(fragmentView);
                            }
                        });
                    }
                }
            }, 1000, 3000);
        }
    }

    private final OkHttpClient client = new OkHttpClient();
    private static final String SERVER_URL = "http://89.115.17.17:3000/";
    private static final String TAG = "EQUIPMENT_FRAGMENT";

    private void loadDevices(View view) {
        SharedPreferences sharedPref = getContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        String userId = String.valueOf(sharedPref.getString("userId", ""));
        Request request = new Request.Builder().url(SERVER_URL + "equipment/user/" + userId).build();
        Log.e(TAG, request.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error getting items from server.");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    String responseString = responseBody.string();
                    Log.d(TAG, responseString);
                    try {
                        JSONArray jsonArray = new JSONArray(responseString);
                        getActivity().runOnUiThread(() -> {
                            try {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    int deviceId = jsonObject.getInt("id");
                                    String deviceTitle = jsonObject.getString("name");
                                    int deviceMotor = jsonObject.getInt("motor");

                                    ScrollView equipmentLayout = view.findViewById(R.id.equipmentLayout);
                                    LinearLayout deviceCard = new LinearLayout(getContext(), null, 0, R.style.equipmentDeviceCard);

                                    float scale = getResources().getDisplayMetrics().density;
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    params.bottomMargin = (int) (16 * scale + 0.5f); // px to dp
                                    deviceCard.setLayoutParams(params);
                                    deviceCard.setTag(deviceId);

                                    TextView title = new TextView(getContext());
                                    title.setText(deviceTitle);
                                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

                                    TextView motorState = new TextView(getContext(), null, 0, R.style.equipmentDeviceCardText);
                                    //motorState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                    if (deviceMotor == 1) {
                                        motorState.setText(R.string.equipment_device_motor_state_on);
                                    } else if (deviceMotor == 0) {
                                        motorState.setText(R.string.equipment_device_motor_state_off);
                                    }

                                    LinearLayout tempRow = new LinearLayout(getContext(), null, 0, R.style.equipmentDeviceCardVolume);
                                    VectorDrawableCompat tempDrawCompat = VectorDrawableCompat.create(getContext().getResources(), R.drawable.device_thermostat_24, null);
                                    ImageView tempIcon = new ImageView(getContext(), null, 0, R.style.iconTint);
                                    tempIcon.setImageDrawable(tempDrawCompat);
                                    LinearLayout.LayoutParams tempIconParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    tempIconParams.rightMargin = (int) (8 * scale + 0.5f);
                                    tempIcon.setLayoutParams(tempIconParams);

                                    TextView temp = new TextView(getContext(), null, 0, R.style.equipmentDeviceCardText);
                                    temp.setText(R.string.equipment_device_temp_default);
                                    tempRow.addView(tempIcon);
                                    tempRow.addView(temp);

                                    LinearProgressIndicator volumeBar = new LinearProgressIndicator(new ContextThemeWrapper(getContext(), R.style.equipmentDeviceCardVolumeBar));
                                    volumeBar.setTrackCornerRadius(500);
                                    volumeBar.setTrackThickness(12);
                                    volumeBar.setTrackColor(ContextCompat.getColor(getContext(), R.color.element_background));
                                    volumeBar.setIndicatorColor(ContextCompat.getColor(getContext(), R.color.redish));
                                    LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    barParams.weight = 1;
                                    barParams.rightMargin = (int) (8 * scale + 0.5f); // px to dp
                                    barParams.leftMargin = (int) (8 * scale + 0.5f); // px to dps
                                    volumeBar.setLayoutParams(barParams);

                                    VectorDrawableCompat volDrawCompat = VectorDrawableCompat.create(getContext().getResources(), R.drawable.water_medium_24, null);
                                    ImageView volIcon = new ImageView(getContext(), null, 0, R.style.iconTint);
                                    volIcon.setImageDrawable(volDrawCompat);

                                    LinearLayout volume = new LinearLayout(getContext(), null, 0, R.style.equipmentDeviceCardVolume);
                                    TextView volumeValue = new TextView(getContext(), null, 0, R.style.equipmentDeviceCardVolumeValue);
                                    volumeValue.setText(R.string.equipment_device_volume_default);

                                    volume.addView(volIcon);
                                    volume.addView(volumeBar);
                                    volume.addView(volumeValue);

                                    deviceCard.addView(title);
                                    deviceCard.addView(motorState);
                                    deviceCard.addView(tempRow);
                                    deviceCard.addView(volume);
                                    equipmentLayout.addView(deviceCard);
                                }
                                loadDeviceValues(view);
                            } catch (JSONException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        });
                    } catch (JSONException e) {
                        throw new IOException("Unexpected format", e);
                    }
                }
            }
        });

    }

    private void loadDeviceValues(View view) {
        ScrollView equipmentLayout = view.findViewById(R.id.equipmentLayout);

        for (int i = 0; i < equipmentLayout.getChildCount(); i++) {

            LinearLayout device = (LinearLayout) equipmentLayout.getChildAt(i);
            int deviceId = (int) device.getTag();
            Request requestDeviceValues = new Request.Builder().url(SERVER_URL + "equipment/" + deviceId + "/sensors").build();
            client.newCall(requestDeviceValues).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Error getting items from server.");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        }
                        assert responseBody != null;
                        String responseString = responseBody.string();
                        //Log.d(TAG, responseString);
                        try {
                            JSONObject resObj = new JSONObject(responseString);
                            JSONObject tempObj = resObj.getJSONObject("temp");
                            JSONObject volObj = resObj.getJSONObject("volume");
                            String tempValue = String.valueOf(tempObj.getDouble("valor"));
                            String volValue = String.valueOf(volObj.getDouble("valor"));

                            getActivity().runOnUiThread(() -> {
                                try {
                                    TextView deviceMotor = (TextView) device.getChildAt(1);


                                    if (resObj.getInt("motor") == 1) {
                                        deviceMotor.setText(R.string.equipment_device_motor_state_on);
                                    } else if (resObj.getInt("motor") == 0) {
                                        deviceMotor.setText(R.string.equipment_device_motor_state_off);
                                    }

                                    LinearLayout deviceTemp = (LinearLayout) device.getChildAt(2);
                                    TextView deviceTempValue = (TextView) deviceTemp.getChildAt(1);
                                    String tempFormat = String.format(getResources().getString(R.string.equipment_device_temp), tempValue);
                                    deviceTempValue.setText(tempFormat);

                                    LinearLayout deviceVolume = (LinearLayout) device.getChildAt(3);
                                    ProgressBar deviceVolumeBar = (ProgressBar) deviceVolume.getChildAt(1);
                                    TextView deviceVolumeValue = (TextView) deviceVolume.getChildAt(2);
                                    deviceVolumeBar.setProgress((int) Math.round((volObj.getDouble("valor") * 100) / resObj.getDouble("totalVolume")));


                                    String volFormat = String.format(getResources().getString(R.string.equipment_device_volume), volValue);
                                    deviceVolumeValue.setText(volFormat);
                                } catch (JSONException e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    } catch (IOException e) {
                        throw new IOException(e);
                    }
                }
            });
        }
    }
}