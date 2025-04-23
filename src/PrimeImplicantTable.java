import java.util.*;

public class PrimeImplicantTable {
    private List<Implicant> implicants;
    private List<Integer> minterms;
    private Map<Implicant, Set<Integer>> coverage;

    public PrimeImplicantTable(List<Implicant> implicants, List<Integer> minterms) {
        this.implicants = new ArrayList<>(implicants);
        this.minterms = new ArrayList<>(minterms);
        this.coverage = new HashMap<>();

        for (Implicant imp : implicants) {
            Set<Integer> covered = new HashSet<>(imp.getMinterms());
            covered.retainAll(minterms);
            coverage.put(imp, covered);
        }
    }

    public List<Implicant> findEssentialImplicants() {
        List<Implicant> essentialImplicants = new ArrayList<>();
        Set<Integer> uncoveredMinterms = new HashSet<>(minterms);

        // Create coverage map
        Map<Integer, List<Implicant>> mintermCoverage = new HashMap<>();
        for (Implicant imp : implicants) {
            for (int m : coverage.get(imp)) {
                mintermCoverage.computeIfAbsent(m, k -> new ArrayList<>()).add(imp);
            }
        }

        // Find essential implicants (minterms covered by only one implicant)
        for (Map.Entry<Integer, List<Implicant>> entry : mintermCoverage.entrySet()) {
            if (entry.getValue().size() == 1) {
                Implicant essential = entry.getValue().get(0);
                if (!essentialImplicants.contains(essential)) {
                    essentialImplicants.add(essential);
                    uncoveredMinterms.removeAll(coverage.get(essential));
                }
            }
        }

        // For remaining uncovered minterms
        List<Implicant> remaining = new ArrayList<>(implicants);
        remaining.removeAll(essentialImplicants);
        remaining.sort((a, b) -> Integer.compare(b.getMinterms().size(), a.getMinterms().size()));

        for (Implicant imp : remaining) {
            if (uncoveredMinterms.isEmpty()) break;
            if (Collections.disjoint(uncoveredMinterms, coverage.get(imp))) continue;

            essentialImplicants.add(imp);
            uncoveredMinterms.removeAll(coverage.get(imp));
        }

        return essentialImplicants;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Calculate column widths
        int piWidth = 18;  // Width for Prime Implicant column
        int mintermWidth = 4;  // Width for each minterm column

        // Header row
        sb.append("| ").append(padCenter("Prime Implicant", piWidth-2)).append(" |");
        for (int m : minterms) {
            sb.append(padCenter(Integer.toString(m), mintermWidth)).append("|");
        }
        sb.append("\n");

        // Divider line
        sb.append("|").append(String.format("%" + (piWidth) + "s", "").replace(' ', '-')).append("|");
        for (int i = 0; i < minterms.size(); i++) {
            sb.append(String.format("%" + mintermWidth + "s", "").replace(' ', '-')).append("|");
        }
        sb.append("\n");

        // Table rows
        for (Implicant imp : implicants) {
            String pattern = getPattern(imp);
            sb.append("| ").append(padCenter("PI " + pattern, piWidth-2)).append(" |");

            for (int m : minterms) {
                String cell = coverage.get(imp).contains(m) ? "X" : "";
                sb.append(padCenter(cell, mintermWidth)).append("|");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String padCenter(String s, int width) {
        if (s.length() >= width) {
            return s;
        }
        int padding = width - s.length();
        int left = padding / 2;
        int right = padding - left;
        String leftPad = " ".repeat(Math.max(0, left));
        String rightPad = " ".repeat(Math.max(0, right));
        return leftPad + s + rightPad;
    }

    private String getPattern(Implicant imp) {
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < imp.getBinaryRep().length(); i++) {
            pattern.append(imp.isSignificant(i) ? imp.getBinaryRep().charAt(i) : '-');
        }
        return pattern.toString();
    }
}