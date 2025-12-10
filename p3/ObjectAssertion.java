class ObjectAssertion {
    public Object o;
    
    public ObjectAssertion(Object o) {
        this.o = o;
    }
    
    public ObjectAssertion isNotNull() {
        if (o == null) {
            throw new RuntimeException();
        }
        return this;
    }
    
    public ObjectAssertion isNull() {
        if (o != null) {
            throw new RuntimeException();
        }
        return this;
    }
    
    public ObjectAssertion isEqualTo(Object o2) {
        if (o == null && o2 == null) {
            return this;
        }
        if (o == null || !o.equals(o2)) {
            throw new RuntimeException();
        }
        return this;
    }
    
    public ObjectAssertion isNotEqualTo(Object o2) {
        if (o == null && o2 == null) {
            throw new RuntimeException();
        }
        if (o != null && o.equals(o2)) {
            throw new RuntimeException();
        }
        return this;
    }
    
    public ObjectAssertion isInstanceOf(Class<?> c) {
        if (o == null) {
            throw new RuntimeException();
        }
        if (!c.isInstance(o)) {
            throw new RuntimeException();
        }
        return this;
    }
}