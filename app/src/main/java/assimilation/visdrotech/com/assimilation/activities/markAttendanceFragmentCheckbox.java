package assimilation.visdrotech.com.assimilation.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.zip.Inflater;

import assimilation.visdrotech.com.assimilation.R;
import assimilation.visdrotech.com.assimilation.retrofitModels.AttendanceList;
import assimilation.visdrotech.com.assimilation.retrofitModels.checkboxAttendanceStudentList;
import assimilation.visdrotech.com.assimilation.retrofitModels.singleStudentAttendance;
import assimilation.visdrotech.com.assimilation.utils.baseApplicationClass;
import assimilation.visdrotech.com.assimilation.utils.restClient;
import assimilation.visdrotech.com.assimilation.utils.restInterface;
import cn.pedant.SweetAlert.SweetAlertDialog;
import io.sentry.Sentry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by defcon on 12/07/18.
 */

public class markAttendanceFragmentCheckbox extends Fragment {
    private restInterface restService;
    private LinearLayout rootView;
    private Button submitButton ;
    private String token,eventUID;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return inflater.inflate(R.layout.fragment_markattendance_checkbox, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String prefName =  ((baseApplicationClass) getActivity().getApplication()).PREF_NAME ;
        SharedPreferences prefs = this.getActivity().getSharedPreferences(prefName, MODE_PRIVATE);
        token = prefs.getString("token", "");

        rootView = (LinearLayout) view.findViewById(R.id.root) ;
        final SweetAlertDialog pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        final Activity activity = getActivity();
        eventUID = activity.getIntent().getExtras().getString("eventUID");
        restService = restClient.getClient().create(restInterface.class);
        restService.getAllStudentsAttendanceList(eventUID).enqueue(new Callback<checkboxAttendanceStudentList>() {
            @Override
            public void onResponse(Call<checkboxAttendanceStudentList> call, Response<checkboxAttendanceStudentList> response) {
                if (response.isSuccessful()) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    checkboxAttendanceStudentList responseObj = response.body();
//                    Log.d("BOOLTAG", )
                    Log.d("TAG",responseObj.toString() );
                    for(AttendanceList item : responseObj.getAttendanceList()){
                        View stud = inflater.inflate(R.layout.markattendance_checkbox_row, null,false);
                        TextView name = (TextView) stud.findViewById(R.id.name);
                        name.setText(item.getName());

                        TextView roll = (TextView) stud.findViewById(R.id.roll);
                        roll.setText(item.getRoll());

                        CheckBox attendanceStatusCheckBox = (CheckBox) stud.findViewById(R.id.status);
                        Boolean attendanceStatus = item.getAttendanceStatus();
                        if (attendanceStatus){
                            attendanceStatusCheckBox.setChecked(true);
                        }
                        else {
                            attendanceStatusCheckBox.setChecked(false);
                        }
                        rootView.addView(stud);
                        pDialog.dismissWithAnimation();
                    }

                }

            }

            @Override
            public void onFailure(Call<checkboxAttendanceStudentList> call, Throwable t) {
                pDialog.dismissWithAnimation();
            }
        });


        submitButton = (Button) view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SweetAlertDialog pDialog2 = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
                pDialog2.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                pDialog2.setTitleText("Loading");
                pDialog2.setCancelable(false);
                pDialog2.show();


                ArrayList<LinearLayout> LinearLayoutList = new ArrayList<LinearLayout>();
                TextView studentrollNoSubmit = null;
                CheckBox studentAttendanceStatus = null;
                String dataToSend = "";
                JSONObject data = new JSONObject();
                for( int i = 1; i < rootView.getChildCount(); i++ ){

                    if( rootView.getChildAt( i ) instanceof LinearLayout )
                        studentrollNoSubmit = (TextView) rootView.getChildAt( i ).findViewById(R.id.roll);
                        studentAttendanceStatus = (CheckBox) rootView.getChildAt(i).findViewById(R.id.status);
                    if (studentAttendanceStatus.isChecked())
                    dataToSend += studentrollNoSubmit.getText() + "," + "true\n";
                    else
                        dataToSend += studentrollNoSubmit.getText() + "," + "false\n";
                }
                Log.d("status", dataToSend. toString());
                Log.d("check", String.valueOf(LinearLayoutList.size()));

                restService = restClient.getClient().create(restInterface.class);
                restService.markMultipleStudentAttendance(token,eventUID,dataToSend).enqueue(new Callback<singleStudentAttendance>() {
                    @Override
                    public void onResponse(Call<singleStudentAttendance> call, Response<singleStudentAttendance> response) {
                        pDialog2.dismissWithAnimation();
                        if (response.isSuccessful()){
                            singleStudentAttendance obj = response.body();
                            if (obj.getAttendanceStatus()) {
                                final SweetAlertDialog successAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE);
                                successAlertDialog.setTitleText("Success!");
                                successAlertDialog.setContentText("Attendance marked successfully!");
                                successAlertDialog.show();
                            }
                            else {
                            SweetAlertDialog erroDialog;
                            erroDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                            erroDialog.setTitleText("Error!");
                            erroDialog.setContentText("Unable to mark attendance. Please try again!")  ;
                            erroDialog.show();}
                        }else {
                        SweetAlertDialog erroDialog;
                        erroDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                        erroDialog.setTitleText("Error!");
                        erroDialog.setContentText("Unable to mark attendance. Please try again!")  ;
                        erroDialog.show();}
                    }

                    @Override
                    public void onFailure(Call<singleStudentAttendance> call, Throwable t) {
                        pDialog2.dismissWithAnimation();

                        SweetAlertDialog erroDialog;
                        erroDialog =  new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                        erroDialog.setTitleText("Error!");
                        erroDialog.setContentText("Unable to mark attendance. Please try again!")  ;
                        erroDialog.show();
                    }
                });
            }
        });
    }
}
