import java.util.*;
import java.util.stream.Collectors;

public class QMCore {
    private List<Integer> minterms;
    private List<Character> variables;
    private List<String> steps;

    public QMCore(List<Integer> maxterms, List<Character> variables) {
        this.variables = variables;
        this.steps = new ArrayList<>();

        int numVars = variables.size();
        Set<Integer> fullSet = new HashSet<>();
        for (int i = 0; i < (1 << numVars); i++) {
            fullSet.add(i);
        }
        fullSet.removeAll(maxterms);
        this.minterms = new ArrayList<>(fullSet);
    }

    public String minimize() {
        steps.clear();
        steps.add("=== Quine-McCluskey Boolean Function Minimization (POS Form) ===");
        steps.add("Input Maxterms: " + getMaxterms());
        steps.add("Variables: " + variables);

        List<Minterm> mintermObjs = initializeMinterms();
        steps.add("\n=== Step 1: Grouping Minterms by Number of 1s ===");
        steps.add(groupMintermsToString(mintermObjs));

        List<Implicant> primeImplicants = findPrimeImplicants(mintermObjs);
        steps.add("\n=== Step 2: Prime Implicants ===");
        steps.add(primeImplicants.stream().map(Implicant::toString).collect(Collectors.joining("\n")));

        PrimeImplicantTable table = new PrimeImplicantTable(primeImplicants, minterms);
        steps.add("\n=== Step 3: Prime Implicant Table ===");
        steps.add(table.toString());

        List<Implicant> essentialImplicants = table.findEssentialImplicants();
        steps.add("\n=== Step 4: Essential Prime Implicants ===");
        steps.add(essentialImplicants.stream().map(Implicant::toString).collect(Collectors.joining("\n")));

        String minimizedExpr = getMinimizedExpression(essentialImplicants);
        steps.add("\n=== Final Minimized Expression (POS) ===");
        steps.add(minimizedExpr);

        return minimizedExpr;
    }

    public List<String> getSteps() {
        return steps;
    }

    private List<Integer> getMaxterms() {
        int max = 1 << variables.size();
        List<Integer> all = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            all.add(i);
        }
        all.removeAll(minterms);
        return all;
    }

    private List<Minterm> initializeMinterms() {
        List<Minterm> mintermObjs = new ArrayList<>();
        for (int m : minterms) {
            mintermObjs.add(new Minterm(m, variables.size()));
        }
        return mintermObjs;
    }

    private String groupMintermsToString(List<Minterm> minterms) {
        Map<Integer, List<Minterm>> groups = new TreeMap<>();
        for (Minterm m : minterms) {
            groups.computeIfAbsent(m.countOnes(), k -> new ArrayList<>()).add(m);
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, List<Minterm>> entry : groups.entrySet()) {
            sb.append("Group ").append(entry.getKey()).append(":\n");
            for (Minterm m : entry.getValue()) {
                sb.append("  ").append(m).append("\n");
            }
        }
        return sb.toString();
    }

    private List<Implicant> findPrimeImplicants(List<Minterm> minterms) {
        Map<Integer, List<Implicant>> groups = new TreeMap<>();
        for (Minterm m : minterms) {
            Implicant imp = new Implicant(m);
            groups.computeIfAbsent(imp.countOnes(), k -> new ArrayList<>()).add(imp);
        }

        List<Implicant> currentImplicants = new ArrayList<>();
        for (List<Implicant> group : groups.values()) {
            currentImplicants.addAll(group);
        }

        List<Implicant> primeImplicants = new ArrayList<>();
        boolean changed;

        do {
            changed = false;
            List<Implicant> nextImplicants = new ArrayList<>();
            Set<Implicant> marked = new HashSet<>();

            List<List<Implicant>> groupList = new ArrayList<>(groups.values());
            for (int i = 0; i < groupList.size() - 1; i++) {
                List<Implicant> currentGroup = groupList.get(i);
                List<Implicant> nextGroup = groupList.get(i + 1);

                for (Implicant imp1 : currentGroup) {
                    for (Implicant imp2 : nextGroup) {
                        if (imp1.canCombine(imp2)) {
                            Implicant combined = new Implicant(imp1, imp2);
                            nextImplicants.add(combined);
                            marked.add(imp1);
                            marked.add(imp2);
                            changed = true;
                        }
                    }
                }
            }

            for (Implicant imp : currentImplicants) {
                if (!marked.contains(imp) && !primeImplicants.contains(imp)) {
                    primeImplicants.add(imp);
                }
            }

            currentImplicants = nextImplicants;
            groups.clear();
            for (Implicant imp : currentImplicants) {
                groups.computeIfAbsent(imp.countOnes(), k -> new ArrayList<>()).add(imp);
            }

        } while (changed);

        primeImplicants.addAll(currentImplicants);
        return primeImplicants;
    }

    private String getMinimizedExpression(List<Implicant> implicants) {
        if (implicants.isEmpty()) {
            return "1"; // For POS, empty product means function is always true
        }

        List<String> clauses = new ArrayList<>();
        for (Implicant imp : implicants) {
            clauses.add(imp.toPOSExpression(variables));
        }

        return String.join("·", clauses); // AND of ORs
    }
}
