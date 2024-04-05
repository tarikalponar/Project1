package RomanRepublicSimulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RomanRepublicSimulation {
    private static final int SIMULATION_YEARS = 200;
    private static final int LIFE_EXPECTANCY_MEAN = 55;
    private static final int LIFE_EXPECTANCY_STD = 10;
    private static final int LIFE_EXPECTANCY_MIN = 25;
    private static final int LIFE_EXPECTANCY_MAX = 80;
    private static final int NEW_CANDIDATES_MEAN = 15;
    private static final int NEW_CANDIDATES_STD = 5;
    private static final int STARTING_PSI = 100;
    private static final Random random = new Random();

    private List<Politician> politicians;
    private int psi;

    public RomanRepublicSimulation() {
        this.politicians = new ArrayList<>();
        this.psi = STARTING_PSI;
        initialize();
    }

    public void runSimulation() {
        for (int year = 1; year <= SIMULATION_YEARS; year++) {
            influxOfNewCandidates();
            Collections.shuffle(politicians);
            fillPositions();
            annualAdjustments();
            ageProgressionAndMortality();
        }
    }

    private void initialize() {
        for (int i = 0; i < 20; i++) {
            politicians.add(new Politician("Quaestor", 30));
        }
        for (int i = 0; i < 10; i++) {
            politicians.add(new Politician("Aedile", 36));
        }
        for (int i = 0; i < 8; i++) {
            politicians.add(new Politician("Praetor", 39));
        }
        for (int i = 0; i < 2; i++) {
            politicians.add(new Politician("Consul", 42));
        }
    }

    private void influxOfNewCandidates() {
        int newCandidates = (int) Math.round(random.nextGaussian() * NEW_CANDIDATES_STD + NEW_CANDIDATES_MEAN);
        for (int i = 0; i < newCandidates; i++) {
            politicians.add(new Politician("Candidate", 30));
        }
    }

    private void fillPositions() {
        fillOffice("Consul", 2, 42, 10);
        fillOffice("Praetor", 8, 39, 2);
        fillOffice("Aedile", 10, 36, 2);
        fillOffice("Quaestor", 20, 30, 0);
    }

    private void fillOffice(String office, int positions, int minAge, int reElectionInterval) {
        List<Politician> eligible = politicians.stream()
                .filter(p -> p.isEligibleForOffice(office, minAge, reElectionInterval))
                .limit(positions)
                .collect(Collectors.toList());
        eligible.forEach(p -> p.electToOffice(office));
        int unfilledPositions = positions - eligible.size();
        psi -= unfilledPositions * 5;
    }

    private void annualAdjustments() {
    List<Politician> consuls = politicians.stream()
            .filter(p -> p.office.equals("Consul") && p.lastElectionYear < 10 && p.lastElectionYear != -10)
            .collect(Collectors.toList());
    psi -= consuls.size() * 10;
    politicians.forEach(p -> {
        if (!p.office.equals("Candidate")) { // Exclude those who have never been elected
            p.lastElectionYear += 1;
        }
    });
}

    private void ageProgressionAndMortality() {
        List<Politician> toRemove = new ArrayList<>();
        for (Politician politician : politicians) {
            politician.age++;
            if (politician.age > politician.lifeExpectancy) {
                toRemove.add(politician);
            }
        }
        politicians.removeAll(toRemove);
    }

    public static void main(String[] args) {
        RomanRepublicSimulation simulation = new RomanRepublicSimulation();
        simulation.runSimulation();
        System.out.println("Simulation finished. Final PSI: " + simulation.psi);
    }

    static class Politician {
        String office;
        int age;
        int lifeExpectancy;
        int lastElectionYear = -10;

        public Politician(String office, int age) {
            this.office = office;
            this.age = age;
            this.lifeExpectancy = truncatedNormal(LIFE_EXPECTANCY_MEAN, LIFE_EXPECTANCY_STD, LIFE_EXPECTANCY_MIN, LIFE_EXPECTANCY_MAX);
        }

        public boolean isEligibleForOffice(String newOffice, int minAge, int reElectionInterval) {
            return this.age >= minAge && (this.lastElectionYear < 0 || this.lastElectionYear + reElectionInterval <= 0);
        }

        public void electToOffice(String office) {
            this.office = office;
            this.lastElectionYear = 0;
        }

        private static int truncatedNormal(double mean, double std, double min, double max) {
            double value;
            do {
                value = random.nextGaussian() * std + mean;
            } while (value < min || value > max);
            return (int) value;
        }
    }
}