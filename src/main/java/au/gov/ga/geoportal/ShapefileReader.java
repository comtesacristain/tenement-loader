package au.gov.ga.geoportal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author michael
 *
 */

public class ShapefileReader {

	private static String partialMappingFilename = "TenementMapping.xml";

	private Map<String, Field> tenementMapping;

	/**
	 * 
	 */
	public static String shapefileDir = "//sun-web-common/public/data/gis_data/geoscience/tenements/";

	/**
	 * 
	 */
	private File shapefile;

	/**
	 * @author michael
	 *
	 */
	public enum States {
		NSW , NT, QLD
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		for (States state : States.values()) {
			File shapefile = findShapefilePath(LocalDate.now(), state);
			if (shapefile.exists()) {

				ShapefileReader reader = new ShapefileReader(shapefile);
				try {
					reader.loadToOracle();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	/**
	 * @param shapefile
	 */
	public ShapefileReader(File shapefile) {
		this.shapefile = shapefile;
		try {
			setTenementMapping();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private DataStore oracleDataStore() throws IOException {
		Properties params = oracleParams();
		DataStore oracleDataStore = DataStoreFinder.getDataStore(params);
		return oracleDataStore;
	}

	public void loadToOracle() throws Exception {
		if (dataExists()) {
			System.out.println("There is already data for " + shapefilePrefix());

		} else {

			SimpleFeatureType tenementsSchema = oracleDataStore().getSchema("TENEMENTS");

			SimpleFeatureSource oracleFeatureSource = oracleDataStore()
					.getFeatureSource(tenementsSchema.getName().getLocalPart());
			Transaction transaction = new DefaultTransaction("create");

			Map<String, Object> shapefileParamsMap = new HashMap<String, Object>();
			shapefileParamsMap.put("url", shapefile.toURI().toURL());

			DataStore shapefileDataStore = DataStoreFinder.getDataStore(shapefileParamsMap);
			String shapefileTypeName = shapefileDataStore.getTypeNames()[0];

			CoordinateReferenceSystem tenementCRS = shapefileDataStore.getSchema(shapefileTypeName)
					.getCoordinateReferenceSystem();
			CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4283");

			MathTransform coordinateSystemTransform = CRS.findMathTransform(tenementCRS, targetCRS, true);

			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(tenementsSchema);

			FeatureSource<SimpleFeatureType, SimpleFeature> shapefileSource = shapefileDataStore
					.getFeatureSource(shapefileTypeName);
			Filter filter = Filter.INCLUDE;

			FeatureCollection<SimpleFeatureType, SimpleFeature> shapefileCollection = shapefileSource
					.getFeatures(filter);

			FeatureIterator<SimpleFeature> shapefileFeatures = shapefileCollection.features();

			SimpleFeatureStore oracleFeatureStore = (SimpleFeatureStore) oracleFeatureSource;

			oracleFeatureStore.setTransaction(transaction);

			while (shapefileFeatures.hasNext()) {

				SimpleFeature source = (SimpleFeature) shapefileFeatures.next();

				for (String attribute : tenementMapping.keySet()) {

					// String attribute = attributeDescriptor.getLocalName();

					Field attributeMapping = tenementMapping.get(attribute);

					String attributeType = attributeMapping.getType();

					Object attributeValue = source.getAttribute(attributeMapping.getSource());

					switch (attributeType) {
					case "string":
						builder.set(attribute.toUpperCase(), attributeValue);
						break;
					case "date":
						String dateString = (String) attributeValue;
						if (!dateString.isEmpty()) {

							String dateFormatString = attributeMapping.getFormat();

							DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormatString);
							LocalDate date = LocalDate.parse(dateString, formatter);
							builder.set(attribute.toUpperCase(), date.format(DateTimeFormatter.ISO_DATE));
						}
						break;

					case "vocabulary":
						System.out.println(attributeValue);
						Map<String, String> mapping = attributeMapping.getMappings();
						if (mapping.containsKey(attributeValue)) {
							String mappedValue = mapping.get(attributeValue);
							System.out.println(mappedValue);
							builder.set(attribute.toUpperCase(), mappedValue);
						} else {
							builder.set(attribute.toUpperCase(), attributeValue);
						}
						break;
					case "geometry":
						Geometry sourceGeometry = (Geometry) source.getDefaultGeometry();
						Geometry targetGeometry = JTS.transform(sourceGeometry, coordinateSystemTransform);
						int srid = CRS.lookupEpsgCode(targetCRS, true);
						targetGeometry.setSRID(srid);
						builder.set(attribute.toUpperCase(), targetGeometry);
						break;
					default:
						break;
					}



				}

				builder.set("STATE", state());
				builder.set("ACTIVITY_CODE", "A");
				builder.set("RECORDDATE", shapefileDate().format(DateTimeFormatter.ISO_DATE));
				oracleFeatureStore.addFeatures(DataUtilities.collection(builder.buildFeature(null)));
			}
			transaction.commit();
			transaction.close();
		}

	}

	public void setTenementMapping() throws SAXException, IOException, ParserConfigurationException {
		String state = state();
		String mappingFilename = state + partialMappingFilename;

		ClassLoader classLoader = ShapefileReader.class.getClassLoader();
		TenementMapping typeMapping = new TenementMapping(classLoader.getResource(mappingFilename).getFile());
		tenementMapping = typeMapping.getMapping();

	}

	/**
	 * @param date
	 * @param state
	 * @return
	 */
	private static File findShapefilePath(LocalDate date, States state) {
		if (date.getYear() < 2000) {
			return new File("null");
		}
		int month = date.getMonthValue();
		String path = shapefileDir + state.toString().toLowerCase() + "_tenement_" + String.format("%02d", month);
		File shapefile = new File(path + ".shp");
		System.out.println("Searching inital path: " + path);
		if (shapefile.exists()) {
			System.out.println("Shapefile found!");

			String newPath = path + "_" + date.minusYears(1).getYear();
			System.out.println("Checking if this is current of last years");
			System.out.println("Does the following path exist? " + newPath);
			if (new File(newPath + ".shp").exists()) {
				return shapefile;
			}
		} else {
			System.out.println("Shapefile not found!");
			path = path + "_" + date.getYear();
			shapefile = new File(path + ".shp");
			if (shapefile.exists()) {
				return shapefile;
			}

		}
		date = date.minusMonths(1);
		return findShapefilePath(date, state);
	}

	/**
	 * @return Boolean on whether data already exists for the given shapefile in
	 *         Oracle
	 * @throws IOException
	 * @throws CQLException
	 */
	private boolean dataExists() throws IOException, CQLException {

		String state = state();
		LocalDate date = shapefileDate();
		SimpleFeatureType tenementsSchema = oracleDataStore().getSchema("TENEMENTS");
		SimpleFeatureSource oracleFeatureSource = oracleDataStore()
				.getFeatureSource(tenementsSchema.getName().getLocalPart());

		// Filter stateFilter = CQL.toFilter("STATE = '" + state + "'");
		// Filter dateFilter = CQL.toFilter("RECORDDATE = '" +
		// date.format(formatter) + "'");

		Filter filter = CQL.toFilter("STATE = '" + state + "' AND RECORDDATE = '" + date + "'");
		// Query query = new Query(tenementsTypeName, filter);
		SimpleFeatureCollection features = oracleFeatureSource.getFeatures(filter);
		if (features.features().hasNext()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return
	 */
	private LocalDate shapefileDate() {
		int year;
		String[] fileComponents = shapefileNameComponents();
		int month = Integer.parseInt(fileComponents[2]);
		if (fileComponents.length > 3) {
			year = Integer.parseInt(fileComponents[3]);
		} else {
			year = returnYearFromMonth(month);
		}
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate date = yearMonth.atDay(1);

		return date;
	}

	/**
	 * @param month
	 * @return
	 */
	private int returnYearFromMonth(int month) {
		LocalDate monthDate = LocalDate.now().withMonth(month);
		if (monthDate.isBefore(LocalDate.now()) || monthDate.isEqual(LocalDate.now())) {
			return monthDate.getYear();
		} else {
			return monthDate.getYear() - 1;
		}

	}

	/**
	 * @return Shapefile prefix from string based file name
	 */
	private String shapefilePrefix() {
		return shapefile.getName().split("\\.")[0];
	}

	/**
	 * @return Breaks the shapefile prefix file name into an array of components
	 *         that describe the state and month/year of the file
	 */
	private String[] shapefileNameComponents() {
		return shapefilePrefix().split("_");
	}

	private String state() {
		return shapefileNameComponents()[0].toUpperCase();
	}

	/**
	 * @return The parameters for connecting to Oracle.
	 * @throws IOException
	 */
	private static Properties oracleParams() throws IOException {
		InputStream inputStream;
		Properties properties = new Properties();
		String propertyFilename = "persistence.properties";
		inputStream = ShapefileReader.class.getClassLoader().getResourceAsStream(propertyFilename);
		properties.load(inputStream);
		return properties;

	}

}
