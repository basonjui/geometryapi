package com.masterisehomes.geometryapi.tessellation;

import lombok.Getter;
import lombok.ToString;

import com.masterisehomes.geometryapi.geodesy.Harversine;
import com.masterisehomes.geometryapi.hexagon.Coordinates;

/* Similar to setup() in Processing
 * However, due to abstraction, the setup data is hard-coded and not stored, so we cannot
 * retrieve those data to use as a Coordinate system to setup our Hexagon Grid Map.
 * 
 * This class aims to serve as a formal management system for the boundary aspect:
 * - boundaries of canvas 
 * - boundaries of Processing shapes
 */

@ToString
public class Boundary {
	@Getter
	private final Coordinates minCoordinates, maxCoordinates;

	// Processing attributes
	private int width, height;

	// WGS84 Coordinates attributes
	@Getter
	private final double minLat, minLng;
	@Getter
	private final double maxLat, maxLng;

	// Builder pattern to take in dimension ,
	public Boundary(float x, float y, int width, int height) {
		this.minCoordinates = new Coordinates(x, y);
		this.width = width;
		this.height = height;
		this.maxCoordinates = new Coordinates(x + width, y + height);

		this.minLat = minCoordinates.getLatitude();
		this.minLng = minCoordinates.getLongitude();
		this.maxLat = maxCoordinates.getLatitude();
		this.maxLng = maxCoordinates.getLongitude();
	}

	// WGS84 Coordinates Boundary
	public Boundary(Coordinates minCoordinates, Coordinates maxCoordinates) {
		this.minCoordinates = minCoordinates;
		this.maxCoordinates = maxCoordinates;

		this.minLat = minCoordinates.getLatitude();
		this.minLng = minCoordinates.getLongitude();
		this.maxLat = maxCoordinates.getLatitude();
		this.maxLng = maxCoordinates.getLongitude();
	}

	/* Calculate Great-circle distance */
	public double greatCircleDistance() {
		double greatCircleDistance = Harversine.distance(
			minLat, minLng,
			maxLat, maxLng);

		return greatCircleDistance;
	}

	/* Comparison methods */
	public boolean contains(Coordinates centroid) {
		double centroidLat = centroid.getLatitude();
		double centroidLng = centroid.getLongitude();

		if (this.containsLat(centroidLat) && this.containsLng(centroidLng)) {
			return true;
		} else {
			return false;
		}
	}

	// Internal methods
	private boolean containsLat(double lat) {
		if (lat >= this.minLat && lat <= this.maxLat) {
			return true;
		} else {
			return false;
		}
	}

	private boolean containsLng(double lng) {
		if (lng >= this.minLng && lng <= this.maxLng) {
			return true;
		} else {
			return false;
		}
	}

	/* Getters */
	public String gisBoundary() {
		return String.format("GisBoundary(minLat=%s, minLng=%s, maxLat=%s, maxLng=%s)", minLat, minLng, maxLat, maxLng);
	}
}