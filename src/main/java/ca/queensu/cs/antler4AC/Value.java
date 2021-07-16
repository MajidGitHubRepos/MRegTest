package ca.queensu.cs.antler4AC;


public class Value {

    public static Value VOID = new Value(new Object(),"");

    final Object value;
    final String type;
    
    public Value(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    public Boolean asBoolean() {
    	//System.out.println("value: "+value.toString());
        if (value.toString().contentEquals("true"))
        	return true;
        else if (value.toString().contentEquals("false"))
        	return false;
        else 
        	System.err.println("value cannot be converted to a boolean because it is: "+value.toString());
        return false;

    }

    public Double asDouble() {
    	return Double.valueOf(value.toString()); 
    }

    public String asString() {
        return String.valueOf(value);
    }
    
    public Integer asInteger() {
    	return Integer.valueOf(value.toString()); 
    }
    
    public boolean isInteger() {
        return value instanceof Integer;
    }

    public boolean isDouble() {
        return value instanceof Double;
    }
    
    public boolean isBoolean() {
        return value instanceof Boolean;
    }
    
    public String getType() {
    	return type;
    }

    @Override
    public int hashCode() {

        if(value == null) {
            return 0;
        }

        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if(value == o) {
            return true;
        }

        if(value == null || o == null || o.getClass() != value.getClass()) {
            return false;
        }

        Value that = (Value)o;

        return this.value.equals(that.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}