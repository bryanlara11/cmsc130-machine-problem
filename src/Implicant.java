import java.util.*;
import java.util.stream.Collectors;

public class Implicant {
    private final Set<Integer> minterms;
    private final String binaryRep;
    private final boolean[] mask;

    public Implicant(Minterm minterm) {
        Objects.requireNonNull(minterm, "Minterm cannot be null");
        this.minterms = new HashSet<>();
        this.minterms.add(minterm.getValue());
        this.binaryRep = minterm.getBinary();

        if (binaryRep == null || binaryRep.isEmpty()) {
            throw new IllegalStateException("Binary representation cannot be null or empty");
        }

        this.mask = new boolean[binaryRep.length()];
        Arrays.fill(mask, true);

        validateState();
    }

    public Implicant(Implicant a, Implicant b) {
        Objects.requireNonNull(a, "First implicant cannot be null");
        Objects.requireNonNull(b, "Second implicant cannot be null");

        if (a.binaryRep.length() != b.binaryRep.length()) {
            throw new IllegalArgumentException("Implicants must have equal length binary representations");
        }

        this.minterms = new HashSet<>();
        this.minterms.addAll(a.minterms);
        this.minterms.addAll(b.minterms);

        this.binaryRep = a.binaryRep;
        this.mask = new boolean[a.mask.length];
        System.arraycopy(a.mask, 0, this.mask, 0, a.mask.length);

        int diffPos = -1;
        for (int i = 0; i < a.binaryRep.length(); i++) {
            if (a.mask[i] && b.mask[i] && a.binaryRep.charAt(i) != b.binaryRep.charAt(i)) {
                if (diffPos != -1) {
                    throw new IllegalArgumentException("Implicants differ by more than one bit");
                }
                diffPos = i;
            } else if (a.mask[i] != b.mask[i]) {
                throw new IllegalArgumentException("Implicants have incompatible masks");
            }
        }

        if (diffPos != -1) {
            this.mask[diffPos] = false;
        }

        validateState();
    }

    private void validateState() {
        if (binaryRep.length() != mask.length) {
            throw new IllegalStateException("Binary representation length doesn't match mask length");
        }

        for (char c : binaryRep.toCharArray()) {
            if (c != '0' && c != '1') {
                throw new IllegalStateException("Invalid binary digit in representation: " + c);
            }
        }
    }

    public String getBinaryRep() {
        return binaryRep;
    }

    public boolean canCombine(Implicant other) {
        if (other == null) return false;
        if (this.binaryRep.length() != other.binaryRep.length()) return false;

        int diffCount = 0;
        for (int i = 0; i < binaryRep.length(); i++) {
            if (mask[i] && other.mask[i]) {
                if (binaryRep.charAt(i) != other.binaryRep.charAt(i)) {
                    diffCount++;
                    if (diffCount > 1) return false;
                }
            } else if (mask[i] != other.mask[i]) {
                return false;
            }
        }
        return diffCount == 1;
    }

    public int countOnes() {
        int count = 0;
        for (int i = 0; i < binaryRep.length(); i++) {
            if (mask[i] && binaryRep.charAt(i) == '1') {
                count++;
            }
        }
        return count;
    }

    public Set<Integer> getMinterms() {
        return Collections.unmodifiableSet(minterms);
    }

    public boolean isSignificant(int bitPos) {
        if (bitPos < 0 || bitPos >= mask.length) {
            throw new IllegalArgumentException("Bit position out of range");
        }
        return mask[bitPos];
    }

    public String toPOSExpression(List<Character> variables) {
        Objects.requireNonNull(variables, "Variables list cannot be null");
        if (variables.size() < binaryRep.length()) {
            throw new IllegalArgumentException("Not enough variables provided");
        }

        StringBuilder clause = new StringBuilder("(");
        boolean first = true;
        for (int i = 0; i < binaryRep.length(); i++) {
            if (mask[i]) {
                if (!first) clause.append(" + ");
                char var = variables.get(i);
                if (binaryRep.charAt(i) == '1') {
                    clause.append(var).append("'");
                } else {
                    clause.append(var);
                }
                first = false;
            }
        }
        clause.append(")");
        return clause.toString();
    }

    public String getPattern() {
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < binaryRep.length(); i++) {
            pattern.append(mask[i] ? binaryRep.charAt(i) : '-');
        }
        return pattern.toString();
    }

    @Override
    public String toString() {
        return String.format("PI: %s covers minterms: %s", getPattern(), minterms.stream().sorted().collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Implicant implicant = (Implicant) o;
        return binaryRep.equals(implicant.binaryRep) &&
                Arrays.equals(mask, implicant.mask);
    }

    @Override
    public int hashCode() {
        int result = binaryRep.hashCode();
        result = 31 * result + Arrays.hashCode(mask);
        return result;
    }
}
