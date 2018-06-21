package eu.andret.kalendarzswiatnietypowych.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class SurveyActivity extends Activity {
    private EditText editText;
    private RadioGroup radioGroup;
    private LinearLayout checkboxGroup;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util util = new Util(this);
        util.applyTheme();
        setContentView(R.layout.activity_survey);
        Bundle b = getIntent().getExtras();

        int type = b.getInt("type");
        String[] answers = b.getStringArray("answers");

        ((TextView) findViewById(R.id.survey_text_title)).setText(b.getString("title"));
        ((TextView) findViewById(R.id.survey_text_content)).setText(b.getString("content"));
        if (type == 0) {
            editText = findViewById(R.id.survey_edit_answer);
            editText.setVisibility(View.VISIBLE);
        } else if (type == 1) {
            radioGroup = findViewById(R.id.survey_radiogroup_answers);
            radioGroup.setVisibility(View.VISIBLE);
            for (String string : answers) {
                RadioButton radio = new RadioButton(this);
                radio.setText(string);
                radioGroup.addView(radio);
            }
        } else if (type == 2) {
            checkboxGroup = findViewById(R.id.survey_checkboxgroup_answers);
            checkboxGroup.setVisibility(View.VISIBLE);
            for (String string : answers) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(string);
                checkboxGroup.addView(checkBox);
            }
        }

        findViewById(R.id.survey_button_submit).setOnClickListener((View v) -> {
            try {
                JSONObject json = new JSONObject();
                if (type == 0) {
                    json.put("answer", editText.getText().toString());
                } else if (type == 1) {
                    for (int i = 0; i < radioGroup.getChildCount(); i++) {
                        if (((RadioButton) radioGroup.getChildAt(i)).isChecked()) {
                            json.put("answer", i);
                        }
                    }
                } else if (type == 2) {
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < checkboxGroup.getChildCount(); i++) {
                        if (((CheckBox) checkboxGroup.getChildAt(i)).isChecked()) {
                            jsonArray.put(i);
                        }
                    }
                    json.put("answer", jsonArray);
                }


                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        return null;
                    }
                }.execute();
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        });
    }
}
