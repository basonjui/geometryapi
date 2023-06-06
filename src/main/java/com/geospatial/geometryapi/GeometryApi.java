package com.geospatial.geometryapi;

import static spark.Spark.*;

import com.geospatial.geometryapi.geojson.FeatureCollection;
import com.geospatial.geometryapi.geojson.GeoJsonManager;
import com.geospatial.geometryapi.hexagon.*;
import com.geospatial.geometryapi.neighbors.*;
import com.geospatial.geometryapi.tessellation.CornerEdgeTessellationDto;
import com.geospatial.geometryapi.utils.JsonTransformer;
import com.google.gson.*;

public class GeometryApi {
	public final static Gson gson = new Gson();

	public static void main(String[] args) {
		before((request, response) -> response.type("application/json"));

		post("/api/hexagon", "application/json", (request, response) -> {
			try {
				// Parse request payload to a JSONObject with Gson
				JsonObject payload = gson.fromJson(request.body(), JsonObject.class);

				// Initialize a HexagonDto with payload to get all required data
				HexagonDto dto = new HexagonDto(payload);

				// GeoJsonManager handles all GeoJSON operations
				GeoJsonManager manager = new GeoJsonManager(dto.getHexagon());
				FeatureCollection collection = manager.getFeatureCollection();
				return collection;

			} catch (Exception e) {
				return "Invalid JSON data provided: " + e;
			}

		}, new JsonTransformer());

		post("/api/neighbors", "application/json", (request, response) -> {
			try {
				// Parse request payload to a JSONObject with Gson
				JsonObject payload = gson.fromJson(request.body(), JsonObject.class);

				// Initialize a HexagonDto with payload to store all required data
				NeighborsDto dto = new NeighborsDto(payload);

				GeoJsonManager manager = new GeoJsonManager(dto.getNeighbors());
				FeatureCollection collection = manager.getFeatureCollection();
				return collection;

			} catch (Exception e) {
				return "Invalid JSON data provided: " + e;
			}

		}, new JsonTransformer());

		post("/api/tessellation", "application/json", (request, response) -> {
			try {
				// Parse request payload to a JSONObject with Gson
				JsonObject tessellationPayload = gson.fromJson(request.body(), JsonObject.class);

				CornerEdgeTessellationDto tessellationDto = new CornerEdgeTessellationDto(tessellationPayload);
				GeoJsonManager manager = new GeoJsonManager(tessellationDto.getTessellation());
				
				return manager.getFeatureCollection();

			} catch (Exception e) {
				return "Invalid JSON data provided: " + e;
			}

		}, new JsonTransformer());
	}
}