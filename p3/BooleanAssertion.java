class BooleanAssertion {
    public boolean b;
    
    public BooleanAssertion(boolean b) {
        this.b = b;
    }
    
    public BooleanAssertion isEqualTo(boolean b2) {
        if (b != b2) {
            throw new RuntimeException();
        }
        return this;
    }
    
    public BooleanAssertion isTrue() {
        if (!b) {
            throw new RuntimeException();
        }
        return this;
    }
    
    public BooleanAssertion isFalse() {
        if (b) {
            throw new RuntimeException();
        }
        return this;
    }
}
