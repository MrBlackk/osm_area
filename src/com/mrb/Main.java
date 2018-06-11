package com.mrb;

import com.jillesvangurp.geo.GeoGeometry;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.errors.OsmNotFoundException;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.*;
import de.westnordost.osmapi.map.handler.MapDataHandler;

import java.util.*;

public class Main {

    static HashMap<Long, List<Long>> areaIds = new HashMap<Long, List<Long>>();
    static HashMap<Long, List<Long>> lengthIds = new HashMap<Long, List<Long>>();
    public static final String OSM_API = "https://api.openstreetmap.org/api/0.6/";

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Wrong number of args");
            System.exit(1);
        }

        double latMin = 0.0, lonMin = 0.0, latMax = 0.0, lonMax = 0.0;
        try {
            latMin = Double.parseDouble(args[0]);
            lonMin = Double.parseDouble(args[1]);
            latMax = Double.parseDouble(args[2]);
            lonMax = Double.parseDouble(args[3]);
        } catch (NumberFormatException e){
            System.err.println("Wrong argument: " + e.getMessage());
            System.exit(1);
        }

        BoundingBox bbox = null;
        try {
            bbox = new BoundingBox(latMin, lonMin, latMax, lonMax);
        } catch (IllegalArgumentException e){
            System.err.println("Wrong bbox argument: " + e.getMessage());
            System.exit(1);
        }

        MapDataDao data = null;
        try {
            OsmConnection osm = new OsmConnection(OSM_API,"");
            data = new MapDataDao(osm);
            DataHandler dataHandler = new DataHandler();
            data.getMap(bbox, dataHandler);
        } catch (OsmNotFoundException e) {
            System.err.println("API error: " + e.getMessage());
            System.exit(1);
        }

        ArrayList<Area> areas = new ArrayList<Area>(areaIds.size());
        for (Long key : areaIds.keySet()) {
            areas.add(new Area(key, getName(key, data), calculateArea(areaIds.get(key), data)));
        }
        ArrayList<Distance> distances = new ArrayList<Distance>(lengthIds.size());
        for (Long key : lengthIds.keySet()) {
            distances.add(new Distance(key, getName(key, data), calculateLength(lengthIds.get(key), data)));
        }

        Collections.sort(areas);
        Collections.sort(distances);

        System.out.println("Areas:");
        for (Area area: areas){
            System.out.println(area);
        }
        System.out.println();
        System.out.println("Distances:");
        for (Distance distance: distances){
            System.out.println(distance);
        }
    }

    private static String getName(Long key, MapDataDao data){
        Way way = data.getWay(key);
        if (way != null){
            Map<String, String> tags = way.getTags();
            if (tags.get("name") != null){
                return tags.get("name");
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry: tags.entrySet()){
                stringBuilder.append(entry.getKey());
                stringBuilder.append("=");
                stringBuilder.append(entry.getValue());
            }
            return stringBuilder.toString();
        }
        return "null";
    }

    private static double calculateArea(List<Long> ids, MapDataDao data){
        double[][] polygonPoints = new double[ids.size()][];
        LatLon latLon;
        for (int i = 0; i < ids.size(); i++) {
            latLon = data.getNode(ids.get(i)).getPosition();
            polygonPoints[i] = new double[]{latLon.getLatitude(), latLon.getLongitude()};
        }
        double[][] polygon = GeoGeometry.polygonForPoints(polygonPoints);
        return GeoGeometry.area(polygon);
    }

    private static double calculateLength(List<Long> ids, MapDataDao data){
        double distance = 0.0;
        LatLon latLonFirst;
        LatLon latLonSecond;

        for (int i = 0; i < ids.size() - 1; i++) {
            latLonFirst = data.getNode(ids.get(i)).getPosition();
            latLonSecond = data.getNode(ids.get(i + 1)).getPosition();
            distance += GeoGeometry.distance(latLonFirst.getLatitude(), latLonFirst.getLongitude(),
                    latLonSecond.getLatitude(), latLonSecond.getLongitude());
        }
        return distance;
    }
}

class DataHandler implements MapDataHandler {

    public void handle(BoundingBox boundingBox) {
    }

    public void handle(Node node) {
    }

    public void handle(Way way) {
        if (way != null && way.getTags() != null){
            if (way.getNodeIds().get(0).equals(way.getNodeIds().get(way.getNodeIds().size() - 1))){
                Main.areaIds.put(way.getId(), way.getNodeIds());
            }
            else{
                Main.lengthIds.put(way.getId(), way.getNodeIds());
            }
        }
    }

    public void handle(Relation relation) {
    }
}