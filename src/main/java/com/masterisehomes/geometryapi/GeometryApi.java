package com.masterisehomes.geometryapi;

import static spark.Spark.*;

import com.google.gson.*;
import com.masterisehomes.geometryapi.hexagon.*;

/**
 * Hello world!
 *
 */
public class GeometryApi {
    public final static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    // public final static Gson gson = new Gson();

    public static void main(String[] args) {

        // String jsonString = "{\"latitude\": 150, \"longitude\": 150, \"radius\":
        // 100}";

        // System.out.println(jsonObject.get("latitude").getAsDouble());

        post("/api/hexagon", "application/json", (req, res) -> {
            try {
                // Parse request payload to a JSONObject with Gson
                JsonObject jsonObj = gson.fromJson(req.body(), JsonObject.class);
                // Get GIS data from payload with keys
                Double latitude = jsonObj.get("latitude").getAsDouble();
                Double longitude = jsonObj.get("longitude").getAsDouble();
                Double circumradius = jsonObj.get("radius").getAsDouble();

                // Initialize a hexagon with client's data
                Coordinates centroid = new Coordinates(latitude, longitude);
                Hexagon hexagon = new Hexagon(centroid, circumradius);

                // Get data from DTO
                HexagonDto dto = new HexagonDto(hexagon);

                return dto.build().toGeoJSON();

            } catch (Exception e) {
                return "Invalid JSON data provided: " + e;
            }
        });

        get("/api/neighbors", (req, res) -> {
            return "Neighbor API Endpoint";
        });
    }
}
