import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.*;
import de.westnordost.osmapi.map.handler.MapDataHandler;
import com.jillesvangurp.geo.GeoGeometry;
import java.util.*;

public class Main {

//	http://www.openstreetmap.org/api/0.6/map?bbox=153.4000%2C-27.9100%2C153.4020%2C-27.9080

	static HashMap<Long, List<Long>> areas = new HashMap<Long, List<Long>>();
	static HashMap<Long, List<Long>> lines = new HashMap<Long, List<Long>>();


	public static void main(String[] args) {
		OsmConnection osm = new OsmConnection("https://api.openstreetmap.org/api/0.6/","");
		MapDataDao data = new MapDataDao(osm);
		BoundingBox bbox = new BoundingBox(-27.9100, 153.4000, -27.9080, 153.4020);
		DataHandler dataHandler = new DataHandler();
		data.getMap(bbox, dataHandler);

		System.out.println("Areas:");
		for (Long key : areas.keySet()) {
			System.out.print(key + ": ");
			calculateArea(areas.get(key), data);
			System.out.println();
		}
		System.out.println("Lengths:");
		for (Long key : lines.keySet()) {
			System.out.print(key + ": ");
			calculateLength(lines.get(key), data);
			System.out.println();
		}
	}

	private static void calculateArea(List<Long> ids, MapDataDao data){
		double[][] polygonPoints = new double[ids.size()][];
		LatLon latLon;
		for (int i = 0; i < ids.size(); i++) {
			latLon = data.getNode(ids.get(i)).getPosition();
			polygonPoints[i] = new double[]{latLon.getLatitude(), latLon.getLongitude()};
		}
		double[][] polygon = GeoGeometry.polygonForPoints(polygonPoints);
		System.out.print(GeoGeometry.area(polygon));
	}

	private static void calculateLength(List<Long> ids, MapDataDao data){
		float distance = 0.0f;
		LatLon latLonFirst;
		LatLon latLonSecond;
		for (int i = 0; i < ids.size() - 1; i++) {
			latLonFirst = data.getNode(ids.get(i)).getPosition();
			latLonSecond = data.getNode(ids.get(i + 1)).getPosition();
			distance += GeoGeometry.distance(latLonFirst.getLatitude(), latLonFirst.getLongitude(),
					latLonSecond.getLatitude(), latLonSecond.getLongitude());
		}
		System.out.print(distance);
	}
}

class DataHandler implements MapDataHandler{

	public void handle(BoundingBox boundingBox) {
	}

	public void handle(Node node) {
	}

	public void handle(Way way) {
		if (way != null && way.getTags() != null){
			if (way.getNodeIds().get(0).equals(way.getNodeIds().get(way.getNodeIds().size() - 1))){
				Main.areas.put(way.getId(), way.getNodeIds());
			}
			else{
				Main.lines.put(way.getId(), way.getNodeIds());
			}
		}
	}

	public void handle(Relation relation) {
	}
}
