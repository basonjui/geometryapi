package com.masterisehomes.geometryapi.tessellation;

import java.lang.Math;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import lombok.Getter;
import lombok.ToString;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.masterisehomes.geometryapi.hexagon.Coordinates;
import com.masterisehomes.geometryapi.hexagon.Hexagon;
import com.masterisehomes.geometryapi.neighbors.NeighborPosition;
import com.masterisehomes.geometryapi.neighbors.Neighbors;
import com.masterisehomes.geometryapi.geodesy.Harversine;
import com.masterisehomes.geometryapi.geojson.GeoJsonManager;

@ToString
public class AxialClockwiseTessellation {
	// Initialization data
	@Getter
	private final Hexagon rootHexagon;
	@Getter
	private final double circumradius;
	@Getter
	private final double inradius;
	@Getter
	private Boundary boundary;

	/*
	 * CORNER HEXAGONS & EDGE HEXAGONS
	 * ---
	 * 
	 * From a Central Hexagon, you can find 6 immediate Neighbor Hexagons that
	 * fit to the central hexagon on its EDGES - given a centroid & inradius.
	 * 
	 * If you keep extending these 6 Neighbors using their centroids and the same
	 * inradius (distance), you will be able to extend the hexagons infinitely in
	 * 6 diagonal directions (or 3 axes).
	 * 
	 * However, the more you extend, the more hexagons you will miss in between the
	 * diagonal directions, in a systematic way.
	 * 
	 * This gap can be perfectly filled with the right number of hexagons (same
	 * size) to form a Hexagonal Grid Map.
	 * -
	 * https://math.stackexchange.com/questions/2389139/determining-neighbors-in-a-
	 * geometric-hexagon-pattern
	 * 
	 * When you look at a complete Hexagon Grid Map, you will see that the grid map
	 * itself form a large Hexagon (in different orientation), that is tiled by
	 * smaller hexagons perfectly without gaps - this concept is Tessellation.
	 * 
	 * What interesting is, these direct Neighbors from the Central Hexagon, when
	 * extended,
	 * - always become the CORNERS of the Hexagon Grid Map (see the website
	 * above).
	 * - and the hexagons that fill the gaps between these Corner Hexagons, always
	 * become the EDGES of the Grid Map.
	 * 
	 * It is much easier to see this when you look at Hexagon Grid Maps as rings
	 * of hexagons around the Central Hexagons.
	 * The more rings wrap around the Central Hexagons, the more EDGE HEXAGONS
	 * exist between the CORNER HEXAGONS in a special 1:1 ratio.
	 * 
	 * From the 2nd ring onward, the geometric property is:
	 * +1 ring = +1 EDGE HEXAGON
	 */

	/*
	 * Corner Hexagons - used to find Edge Hexagons (based on nthRing)
	 * - nthRing should equals any cornerHexagonList.size()
	 */
	private List<Hexagon> c1Hexagons = new ArrayList<Hexagon>(100);
	private List<Hexagon> c2Hexagons = new ArrayList<Hexagon>(100);
	private List<Hexagon> c3Hexagons = new ArrayList<Hexagon>(100);
	private List<Hexagon> c4Hexagons = new ArrayList<Hexagon>(100);
	private List<Hexagon> c5Hexagons = new ArrayList<Hexagon>(100);
	private List<Hexagon> c6Hexagons = new ArrayList<Hexagon>(100);

	private List<Hexagon> c1GisHexagons = new ArrayList<Hexagon>(100);
	private List<Hexagon> c2GisHexagons = new ArrayList<Hexagon>(100);
	private List<Hexagon> c3GisHexagons = new ArrayList<Hexagon>(100);
	private List<Hexagon> c4GisHexagons = new ArrayList<Hexagon>(100);
	private List<Hexagon> c5GisHexagons = new ArrayList<Hexagon>(100);
	private List<Hexagon> c6GisHexagons = new ArrayList<Hexagon>(100);

	/*
	 * Output data
	 *
	 * Notes: ArrayList needs to be assigned initialCapacity for better performance,
	 * it cuts the cycle to expand the Array when it is full
	 * (default initialCapacity = 10)
	 */
	@Getter
	private List<Coordinates> centroids = new ArrayList<Coordinates>(100);
	@Getter
	private List<Coordinates> gisCentroids = new ArrayList<Coordinates>(100);
	@Getter
	private List<Hexagon> hexagons = new ArrayList<Hexagon>(100);
	@Getter
	private List<Hexagon> gisHexagons = new ArrayList<Hexagon>(100);

	/* Updaters */
	@Getter
	private int totalRings = 0; // keep track of hexagon rings generated
	/* The below updaters are used internally only */
	private int minimumRings = 0; // maximum layers of hexagons in a ring required to tessellate
	private int nthRing = 0; // the latest nth rings that tessellate generated

	/* Constructors */
	public AxialClockwiseTessellation(Hexagon rootHexagon) {
		this.rootHexagon = rootHexagon;
		this.circumradius = rootHexagon.getCircumradius();
		this.inradius = rootHexagon.getInradius();
	}

	/* Tessellation */
	public void populateGisHexagons(Boundary boundary) {
		/*
		 * tessellate method is re-runnable
		 * 
		 * Every time this method is run, it does the following actions:
		 * 1. takes in a new Boundary as parameter
		 * 2. clears all the generated centroids & hexagons ArrayList
		 * 3. reset updaters (totalRings, nthRing)
		 * 3. populate new centroids & hexagons with new Boundary
		 */

		// Set boundary to instance
		this.boundary = boundary;

		/*
		 * Clear all tessellation data (in case already generated):
		 * - corner hexagons
		 * - hexagons
		 * - centroids
		 * - tesselllation rings
		 */
		this.clearCornerHexagons();
		this.clearHexagons();
		this.clearCentroids();
		this.resetRings();

		/* Set the maximum amount of tessellation rings */
		this.minimumRings = calculateMinimumRings(boundary);

		/* Loop tessellation logic until nthRing == maxRing */
		while (this.nthRing <= this.minimumRings) {
			switch (this.nthRing) {
				/* Handle special cases: nthRing == 0 -> 1 */
				case 0:
					// Ring 0 is just the rootHexagon
					populateGisRing0(this.rootHexagon);
					break;

				case 1:
					// Ring 1 is basically Neighbors without rootHexagon
					Neighbors neighbors = new Neighbors(this.rootHexagon);
					// TODO: not implemented
					populateGisRing1(neighbors);
					break;

				/* nthRing >= 2 */
				default:
					// Populate GIS Rings
					populateGisRingN(this.nthRing);

					/*
					 * Axial Clock-wise Tessellation algorithm steps
					 * 
					 * 1. Generate next Corner Centroids (nthRing from origin centroid) c1 - c6
					 * 2. Store Corner Centroids
					 * 3. Store Gis/Pixel Centroids
					 * 4. Start with c5, calculate n Edge Cenroids of Corner Centroids clock-wise
					 * (n = requiredEdgeCentroids)
					 * 5. Store Edge Centroids
					 * 6. Store Gis/Pixel Centroids
					 */

					break;
			}

			// Update nthRing each iteration
			this.nthRing++;
		}

	}

	/* Hexagon rings population */
	private void populateRing0(Hexagon rootHexagon) { // Ring 0 has no corners
		this.hexagons.add(rootHexagon);
	}

	private void populateGisRing0(Hexagon rootHexagon) { // Ring 0 has no corners
		this.gisHexagons.add(rootHexagon);
	}

	private void populateRing1(Neighbors neighbors) {
		List<Hexagon> neighborHexagons = neighbors.getHexagons();

		/* Validate neighbors */
		assert neighborHexagons.size() == 7
				: String.format("neighborHexagons size must equals 7, currently: ",
						neighborHexagons.size());

		/*
		 * For each Neighbor, add it to Corners (1 - 6) based on NeighborPosition
		 * 
		 * Since we populated rootHexagon already (from populateRing0 method),
		 * we will skip position 0 of Neighbors' hexagons list.
		 */
		for (Hexagon hexagon : neighborHexagons) {
			NeighborPosition position = hexagon.getPosition();

			switch (position) {
				case ZERO:
					// Ignore position ZERO, already added rootHexagon
					break;
				case ONE:
					this.c1Hexagons.add(hexagon);
					break;
				case TWO:
					this.c2Hexagons.add(hexagon);
					break;
				case THREE:
					this.c4Hexagons.add(hexagon);
					break;
				case FOUR:
					this.c4Hexagons.add(hexagon);
					break;
				case FIVE:
					this.c5Hexagons.add(hexagon);
					break;
				case SIX:
					this.c6Hexagons.add(hexagon);
					break;

				// Handle illegal position
				default:
					throw new IllegalStateException("Only position 1-6 are valid, currently: "
							+ position);
			}
		}

		/* Populate hexagons with Neighbors 1 - 6 */
		this.hexagons.addAll(neighborHexagons.subList(1, 7)); // 7 is exclusive. Why? ask Java :)
	}

	private void populateGisRing1(Neighbors neighbors) {
		List<Hexagon> neighborGisHexagons = neighbors.getGisHexagons();

		/* Validate neighbors */
		assert neighborGisHexagons.size() == 7
				: String.format("neighborHexagons size must equals 7, currently: ",
						neighborGisHexagons.size());

		/*
		 * For each Neighbor, add it to Corners (1 - 6) based on NeighborPosition
		 * 
		 * Since we populated rootHexagon already (from populateRing0 method),
		 * we will skip position 0 of Neighbors' hexagons list.
		 */
		for (Hexagon gisHexagon : neighborGisHexagons) {
			NeighborPosition position = gisHexagon.getPosition();

			switch (position) {
				case ZERO:
					// Ignore position ZERO, already added rootHexagon
					break;
				case ONE:
					this.c1GisHexagons.add(gisHexagon);
					break;
				case TWO:
					this.c2GisHexagons.add(gisHexagon);
					break;
				case THREE:
					this.c3GisHexagons.add(gisHexagon);
					break;
				case FOUR:
					this.c4GisHexagons.add(gisHexagon);
					break;
				case FIVE:
					this.c5GisHexagons.add(gisHexagon);
					break;
				case SIX:
					this.c6GisHexagons.add(gisHexagon);
					break;

				// Handle illegal position
				default:
					throw new IllegalStateException("Only position 1-6 are valid, currently: "
							+ position);
			}
		}

		/* Populate hexagons with Neighbors 1 - 6 */
		this.gisHexagons.addAll(neighborGisHexagons.subList(1, 7)); // 7 is exclusive, why? ask Java doc :)
	}

	private void populateRingN() {

	}

	private void populateGisRingN(int nthRing) {
		/* Validate nthRing */
		assert nthRing > 1 : "nthRing must be > 1, current nthRing: " + nthRing;
		assert nthRing < this.minimumRings : String.format("nthRing must be < %s, current nthRing: %s", this.minimumRings, nthRing);

		/* Calculate latestGisHexagonListIndex and requiredEdgeHexagons */
		int latestGisHexagonListIndex = nthRing - 2; // -1 for Ring 1 and -1 due to List index start at 0
		int requiredEdgeHexagons = nthRing - 1;

		/* Begin by populating corners (1 - 6) for current ring */
		Hexagon latestCornerGisHexagon;
		Hexagon nextCornerGisHexagon;

		NeighborPosition edgePosition;
		Hexagon previousEdgeHexagon;

		for (NeighborPosition position : NeighborPosition.values()) {
			List<Hexagon> edgeHexagons = new ArrayList<Hexagon>();

			switch (position) {
				case ZERO:
					/*
					 * Skipping position ZERO, since that is just root...
					 * TODO: We might need to get rid of ZERO :-s
					 */
					break;

				case ONE:
					// Get the latestHexagon in Corner List
					latestCornerGisHexagon = c1GisHexagons.get(latestGisHexagonListIndex); 

					// Generate and add nextCornerGisHexagon
					nextCornerGisHexagon = Neighbors.generateNextGisHexagon(latestCornerGisHexagon, position);
					c1GisHexagons.add(nextCornerGisHexagon);
					gisHexagons.add(nextCornerGisHexagon);

					// Generate EDGE hexagons from nextCornerGisHexagon
					edgePosition = NeighborPosition.THREE;
					for (int i = 1; i <= requiredEdgeHexagons; i++) {
						if (i == 1) {
							edgeHexagons.add(Neighbors.generateNextGisHexagon(nextCornerGisHexagon, edgePosition));
						} else {
							previousEdgeHexagon = edgeHexagons.get(edgeHexagons.size() - 1);
							edgeHexagons.add(Neighbors.generateNextGisHexagon(previousEdgeHexagon, edgePosition));
						}
					}
					gisHexagons.addAll(edgeHexagons);
					break;

				case TWO:
					latestCornerGisHexagon = c2GisHexagons.get(latestGisHexagonListIndex);

					nextCornerGisHexagon = Neighbors.generateNextGisHexagon(latestCornerGisHexagon, position);
					c2GisHexagons.add(nextCornerGisHexagon);
					gisHexagons.add(nextCornerGisHexagon);

					edgePosition = NeighborPosition.FOUR;
					for (int i = 1; i <= requiredEdgeHexagons; i++) {
						if (i == 1) {
							edgeHexagons.add(Neighbors.generateNextGisHexagon(nextCornerGisHexagon, edgePosition));
						} else {
							previousEdgeHexagon = edgeHexagons.get(edgeHexagons.size() - 1);
							edgeHexagons.add(Neighbors.generateNextGisHexagon(previousEdgeHexagon, edgePosition));
						}
					}
					gisHexagons.addAll(edgeHexagons);
					break;

				case THREE:
					latestCornerGisHexagon = c3GisHexagons.get(latestGisHexagonListIndex);

					nextCornerGisHexagon = Neighbors.generateNextGisHexagon(latestCornerGisHexagon, position);
					c3GisHexagons.add(nextCornerGisHexagon);
					gisHexagons.add(nextCornerGisHexagon);

					edgePosition = NeighborPosition.FIVE;
					for (int i = 1; i <= requiredEdgeHexagons; i++) {
						if (i == 1) {
							edgeHexagons.add(Neighbors.generateNextGisHexagon(nextCornerGisHexagon, edgePosition));
						} else {
							previousEdgeHexagon = edgeHexagons.get(edgeHexagons.size() - 1);
							edgeHexagons.add(Neighbors.generateNextGisHexagon(previousEdgeHexagon, edgePosition));
						}
					}
					gisHexagons.addAll(edgeHexagons);
					break;

				case FOUR:
					latestCornerGisHexagon = c4GisHexagons.get(latestGisHexagonListIndex);

					nextCornerGisHexagon = Neighbors.generateNextGisHexagon(latestCornerGisHexagon, position);
					c4GisHexagons.add(nextCornerGisHexagon);
					gisHexagons.add(nextCornerGisHexagon);

					edgePosition = NeighborPosition.SIX;
					for (int i = 1; i <= requiredEdgeHexagons; i++) {
						if (i == 1) {
							edgeHexagons.add(Neighbors.generateNextGisHexagon(nextCornerGisHexagon, edgePosition));
						} else {
							previousEdgeHexagon = edgeHexagons.get(edgeHexagons.size() - 1);
							edgeHexagons.add(Neighbors.generateNextGisHexagon(previousEdgeHexagon, edgePosition));
						}
					}
					gisHexagons.addAll(edgeHexagons);
					break;

				case FIVE:
					latestCornerGisHexagon = c5GisHexagons.get(latestGisHexagonListIndex);

					nextCornerGisHexagon = Neighbors.generateNextGisHexagon(latestCornerGisHexagon, position);
					c5GisHexagons.add(nextCornerGisHexagon);
					gisHexagons.add(nextCornerGisHexagon);

					edgePosition = NeighborPosition.ONE;
					for (int i = 1; i <= requiredEdgeHexagons; i++) {
						if (i == 1) {
							edgeHexagons.add(Neighbors.generateNextGisHexagon(nextCornerGisHexagon, edgePosition));
						} else {
							previousEdgeHexagon = edgeHexagons.get(edgeHexagons.size() - 1);
							edgeHexagons.add(Neighbors.generateNextGisHexagon(previousEdgeHexagon, edgePosition));
						}
					}
					gisHexagons.addAll(edgeHexagons);
					break;

				case SIX:
					latestCornerGisHexagon = c6GisHexagons.get(latestGisHexagonListIndex);

					nextCornerGisHexagon = Neighbors.generateNextGisHexagon(latestCornerGisHexagon, position);
					c6GisHexagons.add(nextCornerGisHexagon);
					gisHexagons.add(nextCornerGisHexagon);

					edgePosition = NeighborPosition.TWO;
					for (int i = 1; i <= requiredEdgeHexagons; i++) {
						if (i == 1) {
							edgeHexagons.add(Neighbors.generateNextGisHexagon(nextCornerGisHexagon, edgePosition));
						} else {
							previousEdgeHexagon = edgeHexagons.get(edgeHexagons.size() - 1);
							edgeHexagons.add(Neighbors.generateNextGisHexagon(previousEdgeHexagon, edgePosition));
						}
					}
					gisHexagons.addAll(edgeHexagons);
					break;

				default:
					throw new IllegalStateException(
							"Should never reach this code, current position: " + position);
			}
		}

	}

	/* TESSELLATE */
	private void tessellate() {
		/*
		 * GOALS: produce hexagons
		 * 1. generate hexagons 0 (CCI = 0,0,0)
		 * 2. generate hexagon 1 - 6 for each nthRing (with CCI)
		 * 
		 * Given maxRings:
		 * While nthRing <= maxRings:
		 * Case nthRing == 0:
		 * 1. Add rootHexagon to hexagons[]
		 * 
		 * Case nthRing == 1:
		 * 1. Generate neighbors from rootHexagon
		 * 2. Add Neighbors' hexagons 1 - 6 to hexagons[]
		 * 
		 * Case nthRing >= 2:
		 * For each nthRing (until maxRings):
		 * 1. For each direction (1 - 6):
		 * a. Calculate c,r,s displacement (dpm == 1 * nthRing)
		 * b. Generate CORNER centroid
		 * c. Generate CORNER Hexagon (centroid, rootHexagon, direction, displacement)
		 * 
		 * 2. For requiredEdgeHexagons:
		 * a. Generate EDGE centroid
		 * b. Generate EDGE hexagon
		 */
	}

	/* Reset data */
	private void resetRings() {
		this.totalRings = 0;
		this.minimumRings = 0;
		this.nthRing = 0;
	}

	private void clearCornerHexagons() {
		// Corner hexagons
		this.c1Hexagons.clear();
		this.c2Hexagons.clear();
		this.c3Hexagons.clear();
		this.c4Hexagons.clear();
		this.c5Hexagons.clear();
		this.c6Hexagons.clear();

		// Corner GIS hexagons
		this.c1GisHexagons.clear();
		this.c2GisHexagons.clear();
		this.c3GisHexagons.clear();
		this.c4GisHexagons.clear();
		this.c5GisHexagons.clear();
		this.c6GisHexagons.clear();
	}

	private void clearCentroids() {
		this.centroids.clear();
		this.gisCentroids.clear();
	}

	private void clearHexagons() {
		this.hexagons.clear();
		this.gisHexagons.clear();
	}

	/* Calculations */
	private int calculateMinimumRings(Boundary boundary) {
		// Get boundary coordinates
		double startLat = boundary.getMinLatitude();
		double startLng = boundary.getMinLongitude();
		double endLat = boundary.getMaxLatitude();
		double endLng = boundary.getMaxLongitude();

		/*
		 * Calculate the Great-circle Distance between the START and END boundary
		 * coordinates
		 */
		double maxBoundaryDistance = Harversine.distance(startLat, startLng, endLat, endLng);

		/*
		 * Neighbor's distance - distance between each hexagon's neighbor centroid:
		 * neighborDistance = inradius * 2
		 * 
		 * Given maxDistance,
		 * the maximum number of hexagons stack up in any axial direction is:
		 * = maxDistance / neighborDistance
		 * 
		 * However, we need to use Math.ceil() to round it up to nearest int
		 */
		double neighborDistance = this.inradius * 2;

		/*
		 * In Hexagons grids, we can look at it with 3 primary axes (the 6 neighbor
		 * directions):
		 * - minAxialHexagons is the minimum amount of hexagons that required to stack
		 * up (from edges) in those 3 axes to cover the grid map largest diameter.
		 */
		int minAxialHexagons = (int) Math.ceil(maxBoundaryDistance / neighborDistance); // round up

		/*
		 * Calculate the Minimum Required Rings
		 * 
		 * In a normal case, where Centroid is always in the exact middle of the
		 * Boundary, we would calculate the Minimum Required Rings by:
		 * 	minAxialHexagons / 2 - n (where n = 0 or 1 depends on ODD or EVEN case)
		 * 
		 * However, we need to ensure that this works even in the Worst case scenario,
		 * where the Centroid could be place at the Start or End coordinates of the
		 * Boundary.
		 * 
		 * We can do this by setting the Minimum Required Rings to be EQUAL to
		 * minAxialHexagons - which allow the Centroid to cover the minAxialHexagons
		 * in all 6 neighborly directions:
		 * 	- Cons: Increase the amount of generated hexagons up to ~84% (quick guess)
		 * more than True Required Hexagons.
		 * 	- Pros: To never miss any required coverage
		 */
		int minRings = minAxialHexagons;

		return minRings;
	}

	public static void main(String[] args) {
		Gson gson = new GsonBuilder().create();

		Coordinates origin = new Coordinates(106.7064, 10.7744);

		Hexagon hexagon = new Hexagon(origin, 200);
		Neighbors neighbors = new Neighbors(hexagon);

		AxialClockwiseTessellation tessellation = new AxialClockwiseTessellation(hexagon);

		Boundary boundary = new Boundary(
				Arrays.asList(10.7827, 106.6959,
						10.7744, 106.7063));

		int minRings = tessellation.calculateMinimumRings(boundary);

		// Test harversine
		double greatCircleDistance = Harversine.distance(boundary.getMinLatitude(), boundary.getMinLongitude(),
				boundary.getMaxLatitude(), boundary.getMaxLongitude());

		// Call tessellation population methods here
		// tessellation.populateRing0(hexagon);
		// tessellation.populateRing1(neighbors);

		tessellation.populateGisRing0(hexagon);
		tessellation.populateGisRing1(neighbors);

		tessellation.populateGisHexagons(boundary);

		System.out.println("Great-circle distance: " + greatCircleDistance);

		System.out.println("Total rings: " + tessellation.totalRings);
		System.out.println("Minimum required rings: " + tessellation.minimumRings);
		System.out.println("Current nthRing: " + tessellation.nthRing + "\n");

		List<Hexagon> gisHexagons = tessellation.getGisHexagons();
		GeoJsonManager tessellationManager = new GeoJsonManager(tessellation);
		System.out.println(
				gson.toJson(tessellationManager.getFeatureCollection()));
	}
}