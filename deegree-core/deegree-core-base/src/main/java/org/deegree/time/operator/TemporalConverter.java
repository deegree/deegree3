package org.deegree.time.operator;

import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDateTime;
import static org.deegree.time.position.IndeterminateValue.NOW;
import static org.deegree.time.position.IndeterminateValue.UNKNOWN;

import java.util.Date;

import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Temporal;
import org.deegree.time.position.IndeterminateValue;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.TimeInstant;

public class TemporalConverter {

	public Temporal convert(final String gmlTimePositionUnion) {
		return parseDateTime(gmlTimePositionUnion);
	}

	public Temporal convert(final TimeInstant t) {
		return convert(t.getPosition());
	}

	public Temporal convert(final TimePosition t) {
		final IndeterminateValue indeterminateness = t.getIndeterminatePosition();
		if (indeterminateness == NOW) {
			return new DateTime(new Date(), null);
		}
		else if (indeterminateness == UNKNOWN) {
			return null;
		}
		else if (indeterminateness != null) {
			throw new UnsupportedOperationException();
		}
		return convert(t.getValue());
	}

}
