package au.gov.ga.geoportal;

import java.util.Map;

public class Field {

	private String source;
	private String type;
	private String format = null;
	private String URI;
	private String target;

	private Map<String, String> mappings;

	public String getURI() {
		return URI;
	}
	
	
	public String getSource() {
		return source;
	}

	public String getType() {
		return type;
	}

	public String getTarget() {
		return target;
	}

	public Map<String, String> getMappings() {
		return mappings;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setMappings(Map<String, String> mappings) {
		this.mappings = mappings;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public String toString() {
		return "Field [source=" + source + ", type=" + type + ", format=" + format + ", target=" + target
				+ ", mappings=" + mappings + "]";
	}

	public void setURI(String URI) {
		this.URI = URI;

	}

}
