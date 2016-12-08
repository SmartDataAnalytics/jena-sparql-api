package org.aksw.jena_sparql_api.views;

public enum Polarity {
	POSITIVE(true),
	NEGATIVE(false);
	
	private boolean polarity;

	Polarity(boolean polarity) {
		this.polarity = polarity;
	}
	
	public boolean isPositive() {
		return polarity;
	}
}