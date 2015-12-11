package mlt.test;

import java.util.HashMap;
import java.util.Random;

import mlt.Config;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.constraints.types.Type;

public class Util {

	public static Valuation testToValuation(Object[] test) {
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
	
	public static Object[] valuationToTest(Valuation valuation) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (Variable<?> v : valuation.getVariables()) {
			Object value = null;
			if (v.getResultType() == Byte.class) {
				value = (byte)valuation.getValue(v);
			} else if (v.getResultType() == Short.class) {
				value = (short)valuation.getValue(v);
			} else if (v.getResultType() == Integer.class) {
				value = (int)valuation.getValue(v);
			} else if (v.getResultType() == Long.class) {
				value = (long)valuation.getValue(v);
			} else if (v.getResultType() == Float.class) {
				value = (float)valuation.getValue(v);
			} else if (v.getResultType() == Double.class) {
				value = (double)valuation.getValue(v);
			} else if (v.getResultType() == Boolean.class) {
				value = (boolean)valuation.getValue(v);
			}
			map.put(v.getName(), value);
		}
		int size = Config.PARAMETERS.length;
		Object[] test = new Object[size];
		for (int i = 0; i < size; i++) {
			test[i] = map.get(Config.PARAMETERS[i]);
			if (test[i] == null) {
				if (Config.CLS[i] == byte.class) {
					test[i] = (byte) (new Random().nextInt(Config.MAX_BYTE - Config.MIN_BYTE + 1) + Config.MIN_BYTE);
				} else if (Config.CLS[i] == short.class) {
					test[i] = (short) (new Random().nextInt(Config.MAX_SHORT - Config.MIN_SHORT + 1) + Config.MIN_SHORT);
				} else if (Config.CLS[i] == int.class) {
					test[i] = new Random().nextInt(Config.MAX_INT - Config.MIN_INT + 1) + Config.MIN_INT;
				} else if (Config.CLS[i] == long.class) {
					test[i] = (long) (new Random().nextDouble() * (Config.MAX_LONG - Config.MIN_LONG + 1) + Config.MIN_LONG);
				} else if (Config.CLS[i] == float.class) {
					test[i] = new Random().nextFloat() * (Config.MAX_FLOAT - Config.MIN_FLOAT) + Config.MIN_FLOAT;
				} else if (Config.CLS[i] == double.class) {
					test[i] = new Random().nextDouble() * (Config.MAX_DOUBLE - Config.MIN_DOUBLE) + Config.MIN_DOUBLE;
				} else if (Config.CLS[i] == boolean.class) {
					test[i] = new Random().nextBoolean();
				}
			}
		}
		return test;
	}
	
	public static Object[] randomTest() {
		int size = Config.CLS.length;
		Object[] test = new Object[size];
		for (int i = 0; i < size; i++) {
			if (Config.CLS[i] == byte.class) {
				test[i] = (byte) (new Random().nextInt(Config.MAX_BYTE - Config.MIN_BYTE + 1) + Config.MIN_BYTE);
			} else if (Config.CLS[i] == short.class) {
				test[i] = (short) (new Random().nextInt(Config.MAX_SHORT - Config.MIN_SHORT + 1) + Config.MIN_SHORT);
			} else if (Config.CLS[i] == int.class) {
				test[i] = new Random().nextInt(Config.MAX_INT - Config.MIN_INT + 1) + Config.MIN_INT;
			} else if (Config.CLS[i] == long.class) {
				test[i] = (long) (new Random().nextDouble() * (Config.MAX_LONG - Config.MIN_LONG + 1) + Config.MIN_LONG);
			} else if (Config.CLS[i] == float.class) {
				test[i] = new Random().nextFloat() * (Config.MAX_FLOAT - Config.MIN_FLOAT) + Config.MIN_FLOAT;
			} else if (Config.CLS[i] == double.class) {
				test[i] = new Random().nextDouble() * (Config.MAX_DOUBLE - Config.MIN_DOUBLE) + Config.MIN_DOUBLE;
			} else if (Config.CLS[i] == boolean.class) {
				test[i] = new Random().nextBoolean();
			}
		}
		return test;
	}
	
}
