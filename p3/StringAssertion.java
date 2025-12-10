class StringAssertion extends ObjectAssertion {
    public String s;
    
    public StringAssertion(String s) {
        super(s);
        this.s = s;
    }
    
    public StringAssertion isNotNull() {
        super.isNotNull();
        return this;
    }

    public StringAssertion isNull() {
        super.isNull();
        return this;
    }
    
    @Override
    public StringAssertion isEqualTo(Object o) {
        super.isEqualTo(o);
        return this;
    }
    
    @Override
    public StringAssertion isNotEqualTo(Object o) {
        super.isNotEqualTo(o);
        return this;
    }
    
    public StringAssertion startsWith(String s2) {
        if (s == null) {
            throw new RuntimeException();
        }
        if (!s.startsWith(s2)) {
            throw new RuntimeException();
        }
        return this;
    }
    
    public StringAssertion isEmpty() {
        if (s == null) {
            throw new RuntimeException();
        }
        if (!s.isEmpty()) {
            throw new RuntimeException();
        }
        return this;
    }
    
    public StringAssertion contains(String s2) {
        if (s == null) {
            throw new RuntimeException();
        }
        if (!s.contains(s2)) {
            throw new RuntimeException();
        }
        return this;
    }
}
