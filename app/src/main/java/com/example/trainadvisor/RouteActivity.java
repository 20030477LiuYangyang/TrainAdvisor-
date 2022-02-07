package com.example.trainadvisor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class RouteActivity extends AppCompatActivity {

    private TextView displayRouteTxtView = null;
    private AutoCompleteTextView srcAutoTxtView = null;
    private AutoCompleteTextView destAutoTxtView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        final ImageButton routeExitImgBtn = findViewById(R.id.routeExitImgBtn);
        srcAutoTxtView = findViewById(R.id.srcAutoTxtView);
        destAutoTxtView = findViewById(R.id.destAutoTxtView);
        displayRouteTxtView = findViewById(R.id.displayRouteTxtView);

        String[] listOfStations = { "A", "B", "C", "D", "E" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                RouteActivity.this,
                android.R.layout.select_dialog_item,
                listOfStations
        );
        srcAutoTxtView.setThreshold(1);
        srcAutoTxtView.setAdapter(adapter);
        srcAutoTxtView.addTextChangedListener(watcher);

        destAutoTxtView.setThreshold(1);
        destAutoTxtView.setAdapter(adapter);
        destAutoTxtView.addTextChangedListener(watcher);

        routeExitImgBtn.setOnClickListener(v -> startActivity(new Intent(RouteActivity.this, MainActivity.class)));
    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            if (srcAutoTxtView.getText().toString().length() != 0 && destAutoTxtView.getText().toString().length() != 0) {
                getRoute(srcAutoTxtView.getText().toString(), destAutoTxtView.getText().toString());
            }
        }
    };

    // TODO: Convert AsyncTask to Java concurrency. AsyncTask is deprecated.
    private static class FindRouteTask extends AsyncTask<String, Void, String> {

        private final WeakReference<RouteActivity> context;

        public FindRouteTask(RouteActivity context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject jsonObject = new JSONObject(strings[0]);

                if (jsonObject.getString("route") == null) {
                    return "Invalid start or destination";
                } else {
                    return jsonObject.getString("route");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return e.toString();
            }
        }
        @Override
        protected void onPostExecute(String s) {
            // super.onPostExecute(s);
            RouteActivity ra = context.get();
            ra.displayRouteTxtView.setText(s);
        }
    }

    private void getRoute(String start, String target) {

        String url = "http://192.168.0.1:5000/map/route";
        RequestQueue queue = Volley.newRequestQueue(RouteActivity.this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> new FindRouteTask(RouteActivity.this).execute(response),
                error -> {
                    error.printStackTrace();
                    displayRouteTxtView.setText(error.toString());
                }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("start", start);
                params.put("target", target);

                return params;
            }
        };

        queue.add(request);
    }
}