package au.gov.ga.geoportal;

public class Mapping {

	private String source;
	private String target;

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return "Mapping [source=" + source + ", target=" + target + "]";
	}

}
