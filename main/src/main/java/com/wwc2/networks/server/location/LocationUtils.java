package com.wwc2.networks.server.location;

import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;

public class LocationUtils {

	public static float calculateLineDistance(double sLongitude, double sLatitude,
				double eLongitude, double eLatitude){
		DPoint startLatlng = new DPoint();
		startLatlng.setLongitude(sLongitude);
		startLatlng.setLatitude(sLatitude);
		DPoint endLatlng = new DPoint();
		endLatlng.setLongitude(eLongitude);
		endLatlng.setLatitude(eLatitude);
		float distance = CoordinateConverter.calculateLineDistance(startLatlng, endLatlng);
		return (long)distance;
	}

	public static float calculateMaxSpeed(float oldSpeed, float newSpeed){
		return newSpeed > oldSpeed ? newSpeed : oldSpeed;
	}

	public static int conversionSpeed(float speed){
		return (int)(speed * 3600 / 1000);
	}
}
