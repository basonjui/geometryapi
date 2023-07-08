# Geometry API

Geometry API is a Microservice API that takes in WGS84 coordinates (projected) and a radius parameters, which then will be used as the input centroid and radius for the to-be-generated hexagons inside the program. 

The program may use the provided input to generate a Hexagon, a Hexagon with 6 Neighbors, or a full Tessellation of Hexagons for a specific chosen boundary (not yet implemented).

Finally, this microservice will return data in GeoJSON format - which is implemented following the The GeoJSON Specification (RFC 7946).
https://datatracker.ietf.org/doc/html/rfc7946


## Architecture

### Packages diagram
![Geometry API - Package UML (1)](https://user-images.githubusercontent.com/60636087/181493724-9a59b863-7264-4930-99dd-2d8e0f6a5363.png)


## Dependencies

## About GeometryApi v1 release

GeometryApi is a Hexagonal Grid Geospatial Data System that is capable of generating a single Hexagon, Neighbors (7 adjacent hexagons), and Tessellation (a full grid of regular hexagons) for a specific boundary.

The API returns hexagons/grid data in GeoJSON format (RFC 7946), which can be used for multiple purposes in geospatial computing such as visualization, analytics, and data aggregation.

* Hexagon Grid (Tessellation) - Radius: 1000 meters

## Installation (Maven)

Lorem ipsum ...

Use the package manager [pip](https://pip.pypa.io/en/stable/) to install foobar.

```bash
pip install foobar
```


## Usages

GeometryApi is a local API that can be used to generate Hexagon's coordinates and return data in GeoJSON format. You can test the GeoJSON data output on https://geojson.io/.

### /api/hexagon

#### Request

```json
{
    "latitude": 10.7745419,
    "longitude": 106.7018471,
    "radius": 250
}
```

#### Response

```json
{
    "type": "FeatureCollection",
    "features": [
        {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            106.70072420589484,
                            10.772596990358737
                        ],
                        [
                            106.70296999410515,
                            10.772596990358737
                        ],
                        [
                            106.7040928882103,
                            10.7745419
                        ],
                        [
                            106.70296999410515,
                            10.776486809641261
                        ],
                        [
                            106.70072420589484,
                            10.776486809641261
                        ],
                        [
                            106.69960131178969,
                            10.7745419
                        ],
                        [
                            106.70072420589484,
                            10.772596990358737
                        ]
                    ]
                ]
            },
            "properties": {
                "ccid": {
                    "q": 0,
                    "r": 0,
                    "s": 0
                },
                "centroid": {
                    "longitude": 106.7018471,
                    "latitude": 10.7745419
                },
                "circumradius": 250.0,
                "inradius": 216.50635094610965
            }
        }
    ]
}
```

### /api/neighbors

#### Request

```json
{
    "latitude": 10.7745419,
    "longitude": 106.7018471,
    "radius": 250
}
```

#### Response

```json
{
    "type": "FeatureCollection",
    "features": [
        {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            106.70072420589484,
                            10.772596990358737
                        ],
                        [
                            106.70296999410515,
                            10.772596990358737
                        ],
                        [
                            106.7040928882103,
                            10.7745419
                        ],
                        [
                            106.70296999410515,
                            10.776486809641261
                        ],
                        [
                            106.70072420589484,
                            10.776486809641261
                        ],
                        [
                            106.69960131178969,
                            10.7745419
                        ],
                        [
                            106.70072420589484,
                            10.772596990358737
                        ]
                    ]
                ]
            },
            "properties": {
                "ccid": {
                    "q": 0,
                    "r": 0,
                    "s": 0
                },
                "centroid": {
                    "longitude": 106.7018471,
                    "latitude": 10.7745419
                },
                "circumradius": 250.0,
                "inradius": 216.50635094610965
            }
        },
        {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            106.70072420589484,
                            10.768707171076214
                        ],
                        [
                            106.70296999410515,
                            10.768707171076214
                        ],
                        [
                            106.7040928882103,
                            10.770652080717475
                        ],
                        [
                            106.70296999410515,
                            10.772596990358737
                        ],
                        [
                            106.70072420589484,
                            10.772596990358737
                        ],
                        [
                            106.69960131178969,
                            10.770652080717475
                        ],
                        [
                            106.70072420589484,
                            10.768707171076214
                        ]
                    ]
                ]
            },
            "properties": {
                "ccid": {
                    "q": 0,
                    "r": -1,
                    "s": 1
                },
                "centroid": {
                    "longitude": 106.7018471,
                    "latitude": 10.770652080717475
                },
                "circumradius": 250.0,
                "inradius": 216.50635094610965
            }
        },
        {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            106.7040928882103,
                            10.770652080717475
                        ],
                        [
                            106.7063386764206,
                            10.770652080717475
                        ],
                        [
                            106.70746157052575,
                            10.772596990358737
                        ],
                        [
                            106.7063386764206,
                            10.7745419
                        ],
                        [
                            106.7040928882103,
                            10.7745419
                        ],
                        [
                            106.70296999410515,
                            10.772596990358737
                        ],
                        [
                            106.7040928882103,
                            10.770652080717475
                        ]
                    ]
                ]
            },
            "properties": {
                "ccid": {
                    "q": 1,
                    "r": -1,
                    "s": 0
                },
                "centroid": {
                    "longitude": 106.70521578231545,
                    "latitude": 10.772596990358737
                },
                "circumradius": 250.0,
                "inradius": 216.50635094610965
            }
        },
        {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            106.7040928882103,
                            10.7745419
                        ],
                        [
                            106.7063386764206,
                            10.7745419
                        ],
                        [
                            106.70746157052575,
                            10.776486809641261
                        ],
                        [
                            106.7063386764206,
                            10.778431719282523
                        ],
                        [
                            106.7040928882103,
                            10.778431719282523
                        ],
                        [
                            106.70296999410515,
                            10.776486809641261
                        ],
                        [
                            106.7040928882103,
                            10.7745419
                        ]
                    ]
                ]
            },
            "properties": {
                "ccid": {
                    "q": 1,
                    "r": 0,
                    "s": -1
                },
                "centroid": {
                    "longitude": 106.70521578231545,
                    "latitude": 10.776486809641261
                },
                "circumradius": 250.0,
                "inradius": 216.50635094610965
            }
        },
        {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            106.70072420589484,
                            10.776486809641261
                        ],
                        [
                            106.70296999410515,
                            10.776486809641261
                        ],
                        [
                            106.7040928882103,
                            10.778431719282523
                        ],
                        [
                            106.70296999410515,
                            10.780376628923785
                        ],
                        [
                            106.70072420589484,
                            10.780376628923785
                        ],
                        [
                            106.69960131178969,
                            10.778431719282523
                        ],
                        [
                            106.70072420589484,
                            10.776486809641261
                        ]
                    ]
                ]
            },
            "properties": {
                "ccid": {
                    "q": 0,
                    "r": 1,
                    "s": -1
                },
                "centroid": {
                    "longitude": 106.7018471,
                    "latitude": 10.778431719282523
                },
                "circumradius": 250.0,
                "inradius": 216.50635094610965
            }
        },
        {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            106.69735552357939,
                            10.7745419
                        ],
                        [
                            106.69960131178969,
                            10.7745419
                        ],
                        [
                            106.70072420589484,
                            10.776486809641261
                        ],
                        [
                            106.69960131178969,
                            10.778431719282523
                        ],
                        [
                            106.69735552357939,
                            10.778431719282523
                        ],
                        [
                            106.69623262947424,
                            10.776486809641261
                        ],
                        [
                            106.69735552357939,
                            10.7745419
                        ]
                    ]
                ]
            },
            "properties": {
                "ccid": {
                    "q": -1,
                    "r": 1,
                    "s": 0
                },
                "centroid": {
                    "longitude": 106.69847841768454,
                    "latitude": 10.776486809641261
                },
                "circumradius": 250.0,
                "inradius": 216.50635094610965
            }
        },
        {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            106.69735552357939,
                            10.770652080717475
                        ],
                        [
                            106.69960131178969,
                            10.770652080717475
                        ],
                        [
                            106.70072420589484,
                            10.772596990358737
                        ],
                        [
                            106.69960131178969,
                            10.7745419
                        ],
                        [
                            106.69735552357939,
                            10.7745419
                        ],
                        [
                            106.69623262947424,
                            10.772596990358737
                        ],
                        [
                            106.69735552357939,
                            10.770652080717475
                        ]
                    ]
                ]
            },
            "properties": {
                "ccid": {
                    "q": -1,
                    "r": 0,
                    "s": 1
                },
                "centroid": {
                    "longitude": 106.69847841768454,
                    "latitude": 10.772596990358737
                },
                "circumradius": 250.0,
                "inradius": 216.50635094610965
            }
        }
    ]
}
```

### /api/tessellation

#### Request

```json
{
    "latitude": 10.7755,
    "longitude": 106.7021,
    "radius": 5000,
    "boundary": {
        "minLatitude": 10.8163465,
        "minLongitude": 106.661921,
        "maxLatitude": 10.731605,
        "maxLongitude": 106.725970
    }
}
```

### Response

GeoJSON is compacted due to its large size.

```json
{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.67964211789702,10.736601810155294],[106.72455788210299,10.736601810155294],[106.74701576420598,10.7755],[106.72455788210299,10.814398189844704],[106.67964211789702,10.814398189844704],[106.65718423579402,10.7755],[106.67964211789702,10.736601810155294]]]},"properties":{"ccid":{"q":0,"r":0,"s":0},"centroid":{"longitude":106.7021,"latitude":10.7755},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.67964211789702,10.658805430465884],[106.72455788210299,10.658805430465884],[106.74701576420598,10.69770362031059],[106.72455788210299,10.736601810155294],[106.67964211789702,10.736601810155294],[106.65718423579402,10.69770362031059],[106.67964211789702,10.658805430465884]]]},"properties":{"ccid":{"q":0,"r":-1,"s":1},"centroid":{"longitude":106.7021,"latitude":10.69770362031059},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.74701576420598,10.69770362031059],[106.79193152841195,10.69770362031059],[106.81438941051495,10.736601810155294],[106.79193152841195,10.7755],[106.74701576420598,10.7755],[106.72455788210299,10.736601810155294],[106.74701576420598,10.69770362031059]]]},"properties":{"ccid":{"q":1,"r":-1,"s":0},"centroid":{"longitude":106.76947364630897,"latitude":10.736601810155294},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.74701576420598,10.7755],[106.79193152841195,10.7755],[106.81438941051495,10.814398189844704],[106.79193152841195,10.853296379689409],[106.74701576420598,10.853296379689409],[106.72455788210299,10.814398189844704],[106.74701576420598,10.7755]]]},"properties":{"ccid":{"q":1,"r":0,"s":-1},"centroid":{"longitude":106.76947364630897,"latitude":10.814398189844704},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.67964211789702,10.814398189844704],[106.72455788210299,10.814398189844704],[106.74701576420598,10.853296379689409],[106.72455788210299,10.892194569534114],[106.67964211789702,10.892194569534114],[106.65718423579402,10.853296379689409],[106.67964211789702,10.814398189844704]]]},"properties":{"ccid":{"q":0,"r":1,"s":-1},"centroid":{"longitude":106.7021,"latitude":10.853296379689409},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.61226847158805,10.7755],[106.65718423579402,10.7755],[106.67964211789702,10.814398189844704],[106.65718423579402,10.853296379689409],[106.61226847158805,10.853296379689409],[106.58981058948505,10.814398189844704],[106.61226847158805,10.7755]]]},"properties":{"ccid":{"q":-1,"r":1,"s":0},"centroid":{"longitude":106.63472635369104,"latitude":10.814398189844704},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.61226847158805,10.69770362031059],[106.65718423579402,10.69770362031059],[106.67964211789702,10.736601810155294],[106.65718423579402,10.7755],[106.61226847158805,10.7755],[106.58981058948505,10.736601810155294],[106.61226847158805,10.69770362031059]]]},"properties":{"ccid":{"q":-1,"r":0,"s":1},"centroid":{"longitude":106.63472635369104,"latitude":10.736601810155294},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.67964211789702,10.581009050776474],[106.72455788210299,10.581009050776474],[106.74701576420598,10.61990724062118],[106.72455788210299,10.658805430465884],[106.67964211789702,10.658805430465884],[106.65718423579402,10.61990724062118],[106.67964211789702,10.581009050776474]]]},"properties":{"ccid":{"q":0,"r":-2,"s":2},"centroid":{"longitude":106.7021,"latitude":10.61990724062118},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.74701576420598,10.61990724062118],[106.79193152841195,10.61990724062118],[106.81438941051495,10.658805430465884],[106.79193152841195,10.69770362031059],[106.74701576420598,10.69770362031059],[106.72455788210299,10.658805430465884],[106.74701576420598,10.61990724062118]]]},"properties":{"ccid":{"q":1,"r":-2,"s":1},"centroid":{"longitude":106.76947364630897,"latitude":10.658805430465884},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.81438941051495,10.658805430465884],[106.85930517472092,10.658805430465884],[106.88176305682391,10.69770362031059],[106.85930517472092,10.736601810155294],[106.81438941051495,10.736601810155294],[106.79193152841195,10.69770362031059],[106.81438941051495,10.658805430465884]]]},"properties":{"ccid":{"q":2,"r":-2,"s":0},"centroid":{"longitude":106.83684729261793,"latitude":10.69770362031059},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.81438941051495,10.736601810155294],[106.85930517472092,10.736601810155294],[106.88176305682391,10.7755],[106.85930517472092,10.814398189844704],[106.81438941051495,10.814398189844704],[106.79193152841195,10.7755],[106.81438941051495,10.736601810155294]]]},"properties":{"ccid":{"q":2,"r":-1,"s":-1},"centroid":{"longitude":106.83684729261793,"latitude":10.7755},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.81438941051495,10.814398189844704],[106.85930517472092,10.814398189844704],[106.88176305682391,10.853296379689409],[106.85930517472092,10.892194569534114],[106.81438941051495,10.892194569534114],[106.79193152841195,10.853296379689409],[106.81438941051495,10.814398189844704]]]},"properties":{"ccid":{"q":2,"r":0,"s":-2},"centroid":{"longitude":106.83684729261793,"latitude":10.853296379689409},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.74701576420598,10.853296379689409],[106.79193152841195,10.853296379689409],[106.81438941051495,10.892194569534114],[106.79193152841195,10.931092759378819],[106.74701576420598,10.931092759378819],[106.72455788210299,10.892194569534114],[106.74701576420598,10.853296379689409]]]},"properties":{"ccid":{"q":1,"r":1,"s":-2},"centroid":{"longitude":106.76947364630897,"latitude":10.892194569534114},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.67964211789702,10.892194569534114],[106.72455788210299,10.892194569534114],[106.74701576420598,10.931092759378819],[106.72455788210299,10.969990949223524],[106.67964211789702,10.969990949223524],[106.65718423579402,10.931092759378819],[106.67964211789702,10.892194569534114]]]},"properties":{"ccid":{"q":0,"r":2,"s":-2},"centroid":{"longitude":106.7021,"latitude":10.931092759378819},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.61226847158805,10.853296379689409],[106.65718423579402,10.853296379689409],[106.67964211789702,10.892194569534114],[106.65718423579402,10.931092759378819],[106.61226847158805,10.931092759378819],[106.58981058948505,10.892194569534114],[106.61226847158805,10.853296379689409]]]},"properties":{"ccid":{"q":-1,"r":2,"s":-1},"centroid":{"longitude":106.63472635369104,"latitude":10.892194569534114},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.54489482527909,10.814398189844704],[106.58981058948505,10.814398189844704],[106.61226847158805,10.853296379689409],[106.58981058948505,10.892194569534114],[106.54489482527909,10.892194569534114],[106.52243694317609,10.853296379689409],[106.54489482527909,10.814398189844704]]]},"properties":{"ccid":{"q":-2,"r":2,"s":0},"centroid":{"longitude":106.56735270738207,"latitude":10.853296379689409},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.54489482527909,10.736601810155294],[106.58981058948505,10.736601810155294],[106.61226847158805,10.7755],[106.58981058948505,10.814398189844704],[106.54489482527909,10.814398189844704],[106.52243694317609,10.7755],[106.54489482527909,10.736601810155294]]]},"properties":{"ccid":{"q":-2,"r":1,"s":1},"centroid":{"longitude":106.56735270738207,"latitude":10.7755},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.54489482527909,10.658805430465884],[106.58981058948505,10.658805430465884],[106.61226847158805,10.69770362031059],[106.58981058948505,10.736601810155294],[106.54489482527909,10.736601810155294],[106.52243694317609,10.69770362031059],[106.54489482527909,10.658805430465884]]]},"properties":{"ccid":{"q":-2,"r":0,"s":2},"centroid":{"longitude":106.56735270738207,"latitude":10.69770362031059},"circumradius":5000.0,"inradius":4330.127018922193}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[106.61226847158805,10.61990724062118],[106.65718423579402,10.61990724062118],[106.67964211789702,10.658805430465884],[106.65718423579402,10.69770362031059],[106.61226847158805,10.69770362031059],[106.58981058948505,10.658805430465884],[106.61226847158805,10.61990724062118]]]},"properties":{"ccid":{"q":-1,"r":-1,"s":2},"centroid":{"longitude":106.63472635369104,"latitude":10.658805430465884},"circumradius":5000.0,"inradius":4330.127018922193}}]}
```
