class IntAssertion {
    public int i;
    
    public IntAssertion(int i) {
        this.i = i;
    }
    
    public IntAssertion isEqualTo(int i2) {
        if (i != i2) {
            throw new RuntimeException();
        }
        return this;
    }
    
    public IntAssertion isLessThan(int i2) {
        if (i >= i2) {
            throw new RuntimeException();
        }
        return this;
    }
    
    public IntAssertion isGreaterThan(int i2) {
        if (i <= i2) {
            throw new RuntimeException();
        }
        return this;
    }
}
