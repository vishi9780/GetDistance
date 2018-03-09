package app.com.getdistance.draw_route_maps;

import android.util.Log;

/**
 * Created by think360user on 2/9/2018.
 */

public class ConvertKmsToMiles {
    protected double convertKmsToMiles(float kms){
        double miles = (double) (0.621371 * kms);
        Log.e("87","<<<Miles>>>"+miles);
        double d = Double.parseDouble(String.format("%1.2f",miles));
        return d;
    }
    /***
     * Method to get Distance in miles just pass Unit in "M"
     */
   /* private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }*/
}
