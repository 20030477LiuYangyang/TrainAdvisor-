package com.example.trainadvisor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import models.Line;

public class LineStatusActivity extends AppCompatActivity {

    /**
     * A list of Line objects. Each Line object contains a line code and the
     * line status number.
     */
    private final List<Line> lines = new ArrayList<>(3);

    /** Line status number denoting the line is operational. */
    private final int OPERATIONAL = 1;

    /** Line status number denoting the line is under maintenance. */
    private final int MAINTENANCE = 2;

    /** Line status number denoting the line is down */
    private final int DOWN = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_status);

        final ImageButton backButton = findViewById(R.id.lineStatExitImgBtn);
        backButton.setOnClickListener(view -> startActivity(new Intent(LineStatusActivity.this, MainActivity.class)));

        viewLineStatus();
    }

    /**
     * Queries the database for the status number of all lines and colours the _Colour
     * TextViews accordingly.
     *
     * RED: Line is down.
     * YELLOW: Line is under maintenance.
     * GREEN: Line is operational.
     */
    public void viewLineStatus() {
        String url = "https://c200-project.000webhostapp.com/lineStatusViewer.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    ParseLineStatusesQuery plsq = new ParseLineStatusesQuery(response);
                    Thread thread = new Thread(plsq);
                    thread.start();

                    new Thread(
                            () -> runOnUiThread(
                                    () -> {
                                        try {
                                            thread.join();

                                            for (Line line : lines) {
                                                String colourResName = line.getLineCode().toLowerCase() + "Colour";
                                                int colourResID = this.getResources().getIdentifier(colourResName, "id", this.getPackageName());
                                                TextView colourView = findViewById(colourResID);

                                                String statusResName = line.getLineCode().toLowerCase() + "Status";
                                                int statusResID = this.getResources().getIdentifier(statusResName, "id", this.getPackageName());
                                                TextView statusView = findViewById(statusResID);

                                                if (line.getLineStatusNumber() == OPERATIONAL) {
                                                    colourView.getBackground().setColorFilter(Color.parseColor("#A5FF7E"), PorterDuff.Mode.SRC_ATOP);
                                                    statusView.setText("Operational");
                                                } else if (line.getLineStatusNumber() == MAINTENANCE) {
                                                    colourView.getBackground().setColorFilter(Color.parseColor("#F7FF00"), PorterDuff.Mode.SRC_ATOP);
                                                    statusView.setText("Maintenance");
                                                } else if (line.getLineStatusNumber() == DOWN) {
                                                    colourView.getBackground().setColorFilter(Color.parseColor("#FF5050"),  PorterDuff.Mode.SRC_ATOP);
                                                    statusView.setText("Down");
                                                }
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    })
                    ).start();
                },
                Throwable::printStackTrace);

        queue.add(request);
    }

    /**
     * Constructs a Line object for every line found in the Volley response string.
     */
    private class ParseLineStatusesQuery implements Runnable {

        private final JSONObject response;

        public ParseLineStatusesQuery(JSONObject response) {
            this.response = response;
        }

        @Override
        public void run() {
            try {
                JSONArray jsonArray = response.getJSONArray("LineStatusViewer");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject line = jsonArray.getJSONObject(i);

                    String lineCode = line.getString("train_line");
                    String lineStatus = line.getString("line_status");

                    lines.add(new Line(
                            lineCode,
                            Integer.parseInt(lineStatus)
                    ));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
