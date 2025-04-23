public class Minterm {
    private final int value;
    private final String binary;
    private final int numVars;

    public Minterm(int value, int numVars) {
        if (value < 0) {
            throw new IllegalArgumentException("Minterm value cannot be negative");
        }
        if (numVars <= 0) {
            throw new IllegalArgumentException("Number of variables must be positive");
        }

        this.value = value;
        this.numVars = numVars;
        this.binary = toBinary(value, numVars);

        // Validate binary length matches numVars
        if (binary.length() != numVars) {
            throw new IllegalStateException(
                    String.format("Binary representation %s should have length %d", binary, numVars)
            );
        }
    }

    public int getValue() {
        return value;
    }

    public String getBinary() {
        return binary;
    }

    public int countOnes() {
        int count = 0;
        for (char c : binary.toCharArray()) {
            if (c == '1') count++;
        }
        return count;
    }

    private String toBinary(int value, int length) {
        String binary = Integer.toBinaryString(value);
        // Ensure binary string has exactly 'length' characters
        if (binary.length() > length) {
            throw new IllegalArgumentException(
                    String.format("Minterm %d requires more than %d variables", value, length)
            );
        }
        // Pad with leading zeros if needed
        while (binary.length() < length) {
            binary = "0" + binary;
        }
        return binary;
    }

    @Override
    public String toString() {
        return String.format("m%d: %s", value, binary);
    }
}