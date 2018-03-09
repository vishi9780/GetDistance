package app.com.getdistance;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import app.com.getdistance.draw_route_maps.CalculationByDistance;
import app.com.getdistance.draw_route_maps.DrawMarker;
import app.com.getdistance.draw_route_maps.DrawRouteMaps;
import app.com.getdistance.utils.Constant;
import app.com.getdistance.utils.ConstantKeys;
import app.com.getdistance.utils.MultiTextWatcher;
import app.com.getdistance.utils.PlaceJSONParser;
import app.com.getdistance.utils.TextWatcherWithInstance;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    PlacesTasks placesTasks;
    private ParserTasks parserTasks;
    String seladdress;
    LinearLayout ll_source,ll_destination;
    TextView tv_source,tv_destination,tv_distance;
    Button btn_sendRequest;
    boolean isFromSrc=false;
    ListView lv_addressList;
    AutoCompleteTextView actv_toDestinationlocation;
    Dialog dialog;
    CalculationByDistance mCalculationByDistance;
    LatLng origin = null,destination = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initViews();
    }

    private void initViews() {

        mCalculationByDistance=new CalculationByDistance();
        btn_sendRequest=(Button) findViewById(R.id.btn_sendRequest);
        ll_source=(LinearLayout)findViewById(R.id.ll_source);
        ll_destination=(LinearLayout)findViewById(R.id.ll_destination);
        tv_source=(TextView) findViewById(R.id.tv_source);
        tv_destination=(TextView) findViewById(R.id.tv_destination);
        tv_distance=(TextView) findViewById(R.id.tv_distance);

        //onclick event here
        tv_source.setOnClickListener(this);
        tv_destination.setOnClickListener(this);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_source:
                isFromSrc=true;
                customAlert(MapsActivity.this);
                break;
            case R.id.tv_destination:
                isFromSrc=false;
                customAlert(MapsActivity.this);
                break;
        }
    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    public class PlacesTasks extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            // String key = "key=AIzaSyAdWDAfT_vljW2hBr_drpgLiqTeJR0wT90";
            String key = "key=" + ConstantKeys.API_KEY;
            String location = "location=" + Constant.PICKUP_LATITUDE + "," + Constant.PICKUP_LONGITUDE;
            String input = "";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }


            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "radius=500";

            // Building the parameters to the web service
            String parameters = input + "&" + sensor + "&" + key + "&" + location;


            // Output format
            String output = "json";
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?" + input + "&" + location + "&" + sensor + "&" + key;
            // Building// the url to the web service
            //  String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?"+parameters;

            try {
                // Fetching the data from web service in background
                data = Constant.downloadUrl(url);
            } catch (Exception e) {
                Log.e("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("161","<<<My>>"+result);
            // Creating ParserTask
            parserTasks = new ParserTasks();

            // Starting Parsing the JSON string returned by Web Service
            parserTasks.execute(result);
        }
    }

    public class ParserTasks extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(final List<HashMap<String, String>> result) {

            final String[] from = new String[]{"description"};
            int[] to = new int[]{android.R.id.text1};
            final SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, R.layout.custom_text, from, to);


            actv_toDestinationlocation.setAdapter(adapter);
            actv_toDestinationlocation.setDropDownHeight(0);
            actv_toDestinationlocation.setDropDownWidth(0);
            lv_addressList.setAdapter(adapter);
            lv_addressList.setTextFilterEnabled(true);
            lv_addressList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                    Object item = parent.getItemAtPosition(i);
                    HashMap<String, String> hm = (HashMap<String, String>) item;
                    seladdress = hm.get("description");
                    if (isFromSrc==true){
                        tv_source.setText( hm.get("description"));
                        Constant.SEL_ADDRESS = tv_source.getText().toString();
                    }else {
                        tv_destination.setText( hm.get("description"));
                        Constant.SEL_ADDRESS = tv_destination.getText().toString();
                    }
                    dialog.dismiss();
                    new DataLongOperationAsynchTask().execute("lat", "lng");
                    Constant.hideKeyboardForceFully(MapsActivity.this);

                }
            });
        }
    }
    public class DataLongOperationAsynchTask extends AsyncTask<String, Void, String[]> {
        ProgressDialog dialogs = new ProgressDialog(MapsActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogs.setMessage("Please wait...");
            dialogs.setCanceledOnTouchOutside(false);
            dialogs.show();
        }

        @Override
        protected String[] doInBackground(String... params) {
            String response;
            try {
                String f = Constant.SEL_ADDRESS.replaceAll(" ", "%20");
                response = Constant.getLatLongByURL("https://maps.googleapis.com/maps/api/geocode/json?address=" + f + "&key=" + ConstantKeys.API_KEY);
                Log.d("response", "" + response);
                return new String[]{response};
            } catch (Exception e) {
                return new String[]{"error"};
            }
        }

        @Override
        protected void onPostExecute(String... result) {
            try {
                dialogs.dismiss();
                JSONObject jsonObject = new JSONObject(result[0]);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                JSONObject jsonObject11 = jsonObject1.getJSONObject("geometry");
                JSONObject jsonObject12 = jsonObject11.getJSONObject("location");
                String lats = jsonObject12.getString("lat");
                String lngs = jsonObject12.getString("lng");
                double lng = Double.parseDouble(lngs);

                double lat = Double.parseDouble(lats);
                Constant.PICKUP_LATITUDE = String.valueOf(lat);
                Constant.PICKUP_LONGITUDE= String.valueOf(lng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));

                Log.d("latitude", "" + lat);
                Log.d("longitude", "" + lng);

                if (isFromSrc==true) {
                     origin = new LatLng(lat, lng);
                     Log.e("282","<<<>>>"+origin.latitude+"\nlongitude>>>"+origin.longitude);

                }else {
                     destination = new LatLng(lat, lng);
                    Log.e("285","<<<>>>"+destination.latitude+"\nlongitude>>>"+destination.longitude);
                }
                if (origin!=null&&destination!=null){
                    DrawRouteMaps.getInstance(MapsActivity.this).draw(origin, destination, mMap);

                    DrawMarker.getInstance(MapsActivity.this).draw(mMap, origin, R.drawable.source_location, "Origin Location");
                    DrawMarker.getInstance(MapsActivity.this).draw(mMap, destination, R.drawable.destination_location, "Destination Location");

                    LatLngBounds bounds = new LatLngBounds.Builder()
                            .include(origin)
                            .include(destination).build();
                    Point displaySize = new Point();
                    getWindowManager().getDefaultDisplay().getSize(displaySize);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30));
                    mCalculationByDistance.calculationByDistance(origin,destination);
                    tv_distance.setText("Miles:- "+String.valueOf(mCalculationByDistance.calculationByDistance(origin,destination)));
                }else {
                    if (origin==null){
                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.source), Toast.LENGTH_SHORT).show();
                    }else if (destination==null){
                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.destination), Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MapsActivity.this, "Unidentifed", Toast.LENGTH_SHORT).show();

                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();

            }
            if (dialogs.isShowing()) {
                dialogs.dismiss();

            }
        }
    }
    private void customAlert(Context c) {
        dialog = new Dialog(c, R.style.Dialog_No_Border);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_searchplaces);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        ImageView iv_close=(ImageView)dialog.findViewById(R.id.iv_close);
        actv_toDestinationlocation=(AutoCompleteTextView)dialog.findViewById(R.id.actv_toDestinationlocation);
        actv_toDestinationlocation.setHintTextColor(getResources().getColor(android.R.color.white));
        lv_addressList=(ListView)dialog.findViewById(R.id.lv_addressList);
        if (isFromSrc==true){
            actv_toDestinationlocation.setHint(getString(R.string.source));
        }else {
            actv_toDestinationlocation.setHint(getString(R.string.destination));
        }

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    finish();
                    dialog.dismiss();
                }
                return true;
            }
        });
        dialog.show();
        actv_toDestinationlocation.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
            placesTasks=new PlacesTasks();
            placesTasks.execute(s.toString());
           }

           @Override
           public void afterTextChanged(Editable s) {

           }
       });
    }


}
