package com.example.trainadvisor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import models.Cabin;
import models.MRT;
import models.Station;
import algorithm.NearestTrainAlgorithm;

public class TrainSpaceActivity2 extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    /** User determines the direction they are going in here. */
    private Spinner directionSpinner;

    /** Used to store the user's inputted station here. */
    private AutoCompleteTextView selectStationAutoTxtView;

    /** Used to tell the algorithm the direction the user is moving in. */
    private int usrDirection;

    /** Stores the last position of the spinner. */
    private int lastPos;

    /** Stores the user's current station. */
    private Station currentStation;

    /** Stores the user's last station. */
    private Station lastStation;

    /** The auto-refresh thread. */
    private Thread autoRefresh;

    /**
     * Defines the start and end of the range of cabin colour TextViews belonging
     * to the next train.
     */
    private final int[] NEXT_TRAIN_CABINS = { 1, 2 };

    /**
     * Defines the start and end of the range of cabin colour TextViews belonging
     * to the following train.
     */
    private final int[] FOLLOWING_TRAIN_CABINS = { 3, 4 };

    /**
     * Defines the start and end of the range of all cabin colour TextViews.
     */
    private final int[] ALL_TRAIN_CABINS = { 1, 4 };

    /**
     * Initialise UI elements.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_space2);

        selectStationAutoTxtView = findViewById(R.id.selectStationAutoTxtView);
        currentStation = new Station(null);
        lastStation = new Station(null);

        directionSpinner = findViewById(R.id.directionSpinner);
        directionSpinner.setOnItemSelectedListener(TrainSpaceActivity2.this);

        final ImageButton backButton = findViewById(R.id.cabinExitImgBtn);
        backButton.setOnClickListener(view -> startActivity(new Intent(TrainSpaceActivity2.this, MainActivity.class)));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                TrainSpaceActivity2.this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.allStations)
        );
        selectStationAutoTxtView.setThreshold(1);
        selectStationAutoTxtView.setAdapter(adapter);
        selectStationAutoTxtView.addTextChangedListener(watcher);

        AutoRefresh ar = new AutoRefresh();
        autoRefresh = new Thread(ar);
    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            currentStation.setStationName(s.toString().trim());

            if (!currentStation.getStationName().trim().equals(lastStation.getStationName())) {
                directionSpinner.setAdapter(null);
                autoRefresh.interrupt();
                lastStation.setStationName(currentStation.getStationName().trim());

                String[] stationList = getResources().getStringArray(R.array.allStations);
                String[] selectedStation = { currentStation.getStationName() };

                colourCabins(ALL_TRAIN_CABINS, null);

                new TrainSpaceActivity2.PopulateSpinnerTask(TrainSpaceActivity2.this).execute(stationList, selectedStation);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * Used to populate directionSpinner with the 'to X station' and 'to Y station'
     * values for the selected station's line.
     */
    // TODO: Convert AsyncTask to Java concurrency. AsyncTask is deprecated.
    private static class PopulateSpinnerTask extends AsyncTask<String[], Void, String> {

        private final WeakReference<TrainSpaceActivity2> context;

        private PopulateSpinnerTask(TrainSpaceActivity2 context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String[]... strings) {
            for (String station : strings[0]) {
                if (strings[1][0].equals(station)) {
                    String arrayName = "";

                    if (strings[1][0].contains("(DT")) {
                        arrayName = "downtownLineDirection";
                    } else if (strings[1][0].contains("(CC")) {
                        arrayName = "circleLineDirection";
                    } else if (strings[1][0].contains("(NS")) {
                        arrayName = "northSouthLineDirection";
                    }

                    return arrayName;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String arrayName) {
            super.onPostExecute(arrayName);
            TrainSpaceActivity2 tsa2 = context.get();

            if (arrayName != null) {
                final int arrayID = tsa2.getResources().getIdentifier(arrayName, "array", tsa2.getPackageName());

                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                        tsa2,
                        android.R.layout.simple_spinner_item,
                        tsa2.getResources().getStringArray(arrayID)
                );
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                tsa2.directionSpinner.setAdapter(adapter1);

                if (tsa2.directionSpinner.getAdapter() != null) {
                    tsa2.getLineID(tsa2.currentStation);
                }
            }
        }
    }

    /** Request database for the line ID of a station */
    private void getLineID(Station station) {
        String url = "https://c200-project.000webhostapp.com/GetStationLine.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                getLineTrains(jsonObject.getString("line_id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, Throwable::printStackTrace) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("station_name", station.getStationName());

                return params;
            }
        };

        queue.add(request);
    }

    /**
     * Query the database for all the trains on a line. Then, call the
     * GetLineTrainsTask class and pass the response string as a
     * parameter.
     */
    private void getLineTrains(String lineID) {
        String url = "https://c200-project.000webhostapp.com/GetLineTrains.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> new GetLineTrainsTask(TrainSpaceActivity2.this).execute(response),
                Throwable::printStackTrace) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("line_id", lineID);

                return params;
            }
        };

        queue.add(request);
    }

    /**
     * Populates a list of MRT objects, which collectively represent all the
     * trains on a given line. Then, the RunAlgorithmTask class is called.
     */
    // TODO: Convert AsyncTask to Java concurrency. AsyncTask is deprecated.
    private static class GetLineTrainsTask extends AsyncTask<String, Void, List<MRT>> {

        private final WeakReference<TrainSpaceActivity2> context;

        private GetLineTrainsTask(TrainSpaceActivity2 context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected List<MRT> doInBackground(String... strings) {
            List<MRT> mrtList = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(strings[0]);
                JSONArray jsonArray = jsonObject.getJSONArray("trains");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject trains = jsonArray.getJSONObject(i);

                    String trainName = trains.getString("train");
                    Station station = new Station(trains.getString("station"));
                    String direction = trains.getString("direction");

                    mrtList.add(new MRT(trainName, station, Integer.parseInt(direction)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return mrtList;
        }

        @Override
        protected void onPostExecute(List<MRT> mrts) {
            super.onPostExecute(mrts);
            TrainSpaceActivity2 tsa2 = context.get();

            new RunAlgorithmTask(tsa2).execute(mrts);
        }
    }

    /**
     * Populates a List of Station objects with the elements of a string-array
     * that contains the line code of the selected station.
     *
     * Then, the nextAndFollowing static method is called from the
     * NearestTrainAlgorithm class and the List of Station objects is passed into
     * the method as a parameter, along with currentStation, which runs the
     * algorithm to find the next and following train on a given line in a given
     * direction.
     */
    // TODO: Convert AsyncTask to Java concurrency. AsyncTask is deprecated.
    private static class RunAlgorithmTask extends AsyncTask<List<MRT>, Void, MRT[]> {

        private final WeakReference<TrainSpaceActivity2> context;

        private RunAlgorithmTask(TrainSpaceActivity2 context) {
            this.context = new WeakReference<>(context);
        }

        // TODO: Replace this with a non-hard coded solution.
        @Override
        protected MRT[] doInBackground(List<MRT>... lists) {
            TrainSpaceActivity2 tsa2 = context.get();

            String[] arrayName = null;
            List<Station> stationList = new ArrayList<>();

            if (tsa2.selectStationAutoTxtView.getText().toString().contains("(DT")) {
                arrayName = tsa2.getResources().getStringArray(R.array.downtownLine);
            } else if (tsa2.selectStationAutoTxtView.getText().toString().contains("(CC")) {
                arrayName = tsa2.getResources().getStringArray(R.array.circleLine);
            } else if (tsa2.selectStationAutoTxtView.getText().toString().contains("(NS")) {
                arrayName = tsa2.getResources().getStringArray(R.array.northSouthLine);
            }

            if (arrayName != null) {

                for (String station : arrayName) {
                     stationList.add(new Station(station));
                }
            }

            // was tsa2.selectStationAutoTxtView.getText().toString()
            return NearestTrainAlgorithm.nextAndFollowing(tsa2.usrDirection,
                    tsa2.currentStation,
                    stationList,
                    lists[0]);
        }

        @Override
        protected void onPostExecute(MRT[] mrts) {
            super.onPostExecute(mrts);
            TrainSpaceActivity2 tsa2 = context.get();

            tsa2.getCabinStatuses(mrts[0], mrts[1]);
        }
    }

    /**
     * Query the database for the cabin statuses of two trains.
     */
    private void getCabinStatuses(MRT train1, MRT train2) {
        String url = "https://c200-project.000webhostapp.com/GetTrainCabins.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> new CategoriseTrainSpaceTask(TrainSpaceActivity2.this).execute(response, train1, train2),
                Throwable::printStackTrace) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (train1 != null) {
                    params.put("train1", train1.getName());
                } else {
                    params.put("train1", "null");
                }
                if (train2 != null) {
                    params.put("train2", train2.getName());
                } else {
                    params.put("train2", "null");
                }

                return params;
            }
        };

        queue.add(request);
    }

    /**
     * Find all the cabins for two trains, along with the person count in each cabin,
     * and store them in a list of Cabin objects. Then, call the categoriseTrainSpace()
     * method and pass in the list of Cabin objects as a parameter.
     */
    // TODO: Convert AsyncTask to Java concurrency. AsyncTask is deprecated.
    private static class CategoriseTrainSpaceTask extends AsyncTask<Object, Void, List<MRT>> {

        private final WeakReference<TrainSpaceActivity2> context;

        private CategoriseTrainSpaceTask(TrainSpaceActivity2 context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected List<MRT> doInBackground(Object... objects) {
            try {
                if (!objects[0].toString().contains("null")) {
                    List<MRT> mrtList = new ArrayList<>(4);
                    List<Cabin> mrt1Cabins = new ArrayList<>(2);
                    List<Cabin> mrt2Cabins = new ArrayList<>(2);

                    JSONObject jsonObject = new JSONObject(objects[0].toString());
                    JSONArray jsonArray = jsonObject.getJSONArray("cabins");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject trainStatus = jsonArray.getJSONObject(i);

                        String trainName = trainStatus.getString("train_name");
                        String carID = trainStatus.getString("car_id");
                        String personCount = trainStatus.getString("person_count");

                        if (objects[1] != null) {
                            if (((MRT) objects[1]).getName().equals(trainName)) {
                                mrt1Cabins.add(new Cabin(
                                        Integer.parseInt(carID),
                                        Integer.parseInt(personCount)
                                ));
                            }
                        }

                        if (objects[2] != null) {
                            if (((MRT)objects[2]).getName().equals(trainName)) {
                                mrt2Cabins.add(new Cabin(
                                        Integer.parseInt(carID),
                                        Integer.parseInt(personCount)
                                ));
                            }
                        }
                    }

                    if (objects[1] != null) {
                        ((MRT)objects[1]).setCabins(mrt1Cabins);
                        mrtList.add(((MRT)objects[1]));
                    }
                    if (objects[2] != null) {
                        ((MRT)objects[2]).setCabins(mrt2Cabins);
                        mrtList.add(((MRT)objects[2]));
                    }

                    return mrtList;
                }

                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<MRT> mrts) {
            super.onPostExecute(mrts);
            TrainSpaceActivity2 tsa2 = context.get();

            tsa2.categoriseTrainSpace(mrts);

            if (!tsa2.autoRefresh.isAlive()) {
                tsa2.autoRefresh.start();
            }
        }
    }

    /**
     * Tells the colourCabins method what range of cabins to colour and passes
     * in the cabins attribute of the MRT objects in mrtList as a parameter.
     * This happens only twice.
     *
     * @param mrtList List of the two closest MRTs to a given station.
     */
    private void categoriseTrainSpace(List<MRT> mrtList) {

        if (mrtList != null) {
            for (int i = 0; i < mrtList.size(); i++) {

                if (i == 0) {
                    colourCabins(NEXT_TRAIN_CABINS, mrtList.get(i).getCabins());
                } else {
                    colourCabins(FOLLOWING_TRAIN_CABINS, mrtList.get(i).getCabins());
                }
            }
        }
    }

    /**
     * Colours a specified range of cabin_Colour TextViews based on the personCount
     * attribute of the Cabin objects in cabins, where the underscore (_) is an
     * integer from 1 - 4 (inclusive).
     *
     * RED: >5 people
     * YELLOW: <6 people
     * GREEN: <4 people
     *
     * @param range The starting and ending number of the range of cabin colour
     *              TextViews.
     * @param cabins The list of cabins. If the value passed in is null, then
     *               all the cabins in the specified range will be coloured
     *               grey.
     */
    private void colourCabins(int[] range, List<Cabin> cabins) {
        String resID;
        int updatedResID;
        TextView txtViewToChange;

        int index = 0;
        for (int currentRange = range[0]; currentRange < range[1] + 1; currentRange++) {
            resID = "cabin" + (currentRange) + "Colour";
            updatedResID = this.getResources().getIdentifier(resID, "id", this.getPackageName());
            txtViewToChange = findViewById(updatedResID);

            if (cabins == null) {
                txtViewToChange.getBackground().setColorFilter(Color.parseColor("#e8e8e8"), PorterDuff.Mode.SRC_ATOP);
            } else if (cabins.get(index).getPeopleCount() <= 3) {
                txtViewToChange.getBackground().setColorFilter(Color.parseColor("#A5FF7E"), PorterDuff.Mode.SRC_ATOP);
            } else if (cabins.get(index).getPeopleCount() <= 5) {
                txtViewToChange.getBackground().setColorFilter(Color.parseColor("#F7FF00"), PorterDuff.Mode.SRC_ATOP);
            } else {
                txtViewToChange.getBackground().setColorFilter(Color.parseColor("#FF5050"), PorterDuff.Mode.SRC_ATOP);
            }

            index++;
        }
    }

    /**
     * Calls the getLineID method every 10 seconds while the thread is still
     * running.
     */
    private class AutoRefresh implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10000);
                    runOnUiThread(() -> getLineID(currentStation));
                } catch (InterruptedException e) {
                    // stub
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0 && position != lastPos) {
            usrDirection = NearestTrainAlgorithm.MOVING_FORWARDS;
            getLineID(currentStation);
        } else if (position == 1 && position != lastPos) {
            usrDirection = NearestTrainAlgorithm.MOVING_BACKWARDS;
            getLineID(currentStation);
        } else {
            usrDirection = NearestTrainAlgorithm.MOVING_FORWARDS;
        }

        lastPos = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // stub
    }
}