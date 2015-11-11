package mlt.test;

import mlt.Config;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.constraints.types.Type;

public class Util {

	public static Valuation toValuation(Object[] test) {
		Valuation valuation = new Valuation();
		int size = test.length;
		for (int i = 0; i < size; i++) {
			Class<?> cls = test[i].getClass();
			Type<?> type = null;
			if (cls == Byte.class) {
				type = BuiltinTypes.SINT8;
			} else if (cls == Short.class) {
				type = BuiltinTypes.SINT16;
			} else if (cls == Integer.class) {
				type = BuiltinTypes.SINT32;
			} else if (cls == Long.class) {
				type = BuiltinTypes.SINT64;
			} else if (cls == Float.class) {
				type = BuiltinTypes.FLOAT;
			} else if (cls == Double.class) {
				type = BuiltinTypes.DOUBLE;
			} else if (cls == Boolean.class) {
				type = BuiltinTypes.BOOL;
			}
			valuation.setParsedValue(Variable.create(type, Config.PARAMETERS[i]), test[i].toString());
		}
		return valuation;
	}
	
	public static Object[] toTest(Valuation val) {
		return null;
	}
	
}
