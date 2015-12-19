package au.gov.ga.geoportal;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public class ShapefileReader {

	public static void main(String[] args) throws IOException {
		
		Transaction transaction = new DefaultTransaction("create");

		File shapefile = new File("nsw_tenement_12.shp");
		Map<String, Object> shapefileParamsMap = new HashMap<String, Object>();
		shapefileParamsMap.put("url", shapefile.toURI().toURL());

		DataStore shapefileDataStore = DataStoreFinder.getDataStore(shapefileParamsMap);
		String shapefileTypeName = shapefileDataStore.getTypeNames()[0];

		FeatureSource<SimpleFeatureType, SimpleFeature> source = shapefileDataStore.getFeatureSource(shapefileTypeName);
		Filter filter = Filter.INCLUDE;

		Map<String, Object> params = oracleParams();

		DataStore oracleDataStore = DataStoreFinder.getDataStore(params);

		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
		FeatureIterator<SimpleFeature> features = collection.features();

		SimpleFeatureType tenementsSchema = oracleDataStore.getSchema("TENEMENTS");

		// while (features.hasNext()) {
		String[] typeNames = oracleDataStore.getTypeNames();

		SimpleFeature feature = features.next();

		System.out.println("Feature as string: " + feature.toString());

		System.out.print("Type names: " + tenementsSchema.getName().getLocalPart());

		// }

		//
		// oracleDa

	}

	private static Map<String, Object> oracleParams() {
		Map<String, Object> parameters = new HashMap<String, Object>();


		return parameters;
	}
}
