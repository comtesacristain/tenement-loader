package au.gov.ga.geoportal;

import java.util.List;

public class Field {

	private String source;
	private String type;
	private String format =null;
	private String target;
	private List<Mapping> mappings;

	public String getSource() {
		return source;
	}

	public String getType() {
		return type;
	}

	public String getTarget() {
		return target;
	}

	public List<Mapping> getMappings() {
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

	public void setMappings(List<Mapping> mappings) {
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

	

}
