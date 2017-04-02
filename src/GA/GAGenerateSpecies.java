package GA;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The genetic algorithm class, one species runs on one Thread
 * Created by jdudek on 3/28/2017.
 */
public class GAGenerateSpecies implements Runnable, JungleDataKeeper {

    private final static double MUT_PROB = 0.1;
    private final String speciesName;
    private final int POP_COUNT;
    private Random random;
    //the size of the simulation
    private final static int BOARD_SIZE = 100;
    //how many worker threads should run simultaneously
    private final static int THREAD_POOL_SIZE = 5;
    private final static int MAX_ITERATION = 10;
    //set the comparator for selection from the GA SPECIES class
    private final Comparator<GASpecies> FITNESS_CRITERION;
    private static final boolean TWO_SPECIES = true;

    /**
     * @param speciesName       the name for the species we are optimizing
     * @param populationCount   the number of initial population
     * @param fitnessComparator the comparator for comparing individuals
     */
    GAGenerateSpecies(String speciesName, int populationCount, Comparator<GASpecies> fitnessComparator) {
        this.speciesName = speciesName;
        POP_COUNT = populationCount;
        this.random = new Random();
        this.FITNESS_CRITERION = fitnessComparator;
        //generateSpecies(random);

    }

    @Override
    public void run() {
        GAFitness[] fitnessThreads = new GAFitness[POP_COUNT];
        for (int i = 0; i < fitnessThreads.length; i++) {
            fitnessThreads[i] = new GAFitness(BOARD_SIZE, TWO_SPECIES);
        }
        ArrayList<GASpecies> speciesPopulation = new ArrayList<>();
        initializePopulation(speciesPopulation);

        ArrayList<GASpecies> speciesPopulation2 = null;
        if (TWO_SPECIES) {
            speciesPopulation2 = new ArrayList<>();
            initializePopulation(speciesPopulation2);
        }
        System.out.format("Starting evolving species %s\n", speciesName);
        int iteration = 0; //temporary until I find a good stopping condition
        GAWorker worker1 = new GAWorker();
        GAWorker worker2 = new GAWorker();
        ExecutorService gaExecutor = null;

        while (iteration < MAX_ITERATION) {
            iteration++;
            //creating a thread pool!
            ExecutorService fitnessExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            if (TWO_SPECIES) {
                getFitness(speciesPopulation, speciesPopulation2, fitnessExecutor, iteration, fitnessThreads);
                gaExecutor = Executors.newFixedThreadPool(2);

            } else {
                getFitness(speciesPopulation, fitnessExecutor, iteration, fitnessThreads);
            }
            // I think we should not evolve our last generation
            if (iteration < MAX_ITERATION) {
                if (!TWO_SPECIES) {
                    selection(speciesPopulation);
                    mutate(speciesPopulation);
                } else {

                    worker1.setPopulation(speciesPopulation);
                    worker2.setPopulation(speciesPopulation2);
                    gaExecutor.execute(worker1);
                    gaExecutor.execute(worker2);
                    gaExecutor.shutdown();
                    while (!gaExecutor.isTerminated()) {

                    }
                }
                System.out.println(iteration);
            }
        }

        speciesPopulation.sort(FITNESS_CRITERION);

        System.out.format("Species %s evolved growth rate: %.10f\n", speciesName, speciesPopulation.get(POP_COUNT - 1).getP());
    }

    private void initializePopulation(ArrayList<GASpecies> population) {
        for (int i = 0; i < POP_COUNT; i++) {
            population.add(new GASpecies(random.nextDouble()));

        }
    }


    private void getFitness(ArrayList<GASpecies> population, ExecutorService executor, int iteration, GAFitness[] threads) {
        GASpecies individual;
        for (int i = 0; i < population.size(); i++) {
            individual = population.get(i);
            //setting a string representation to identify the individual
            individual.setIdentifier(String.format("p=%.6f; itr=%d", individual.getP(), iteration));
            threads[i].resetParameter(individual, null);
            executor.execute(threads[i]);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {

        }
    }

    private void getFitness(ArrayList<GASpecies> population1, ArrayList<GASpecies> population2, ExecutorService executor, int iteration, GAFitness[] threads) {
        GASpecies individual;
        GASpecies individual2;
        for (int i = 0; i < population1.size(); i++) {
            individual = population1.get(i);
            individual2 = population2.get(i);
            //setting a string representation to identify the individual
            individual.setIdentifier(String.format("p=%.6f|itr=%d|ind %d", individual.getP(), iteration, i));
            individual2.setIdentifier(String.format("p=%.6f|itr=%d|ind %d", individual2.getP(), iteration, i));
            threads[i].resetParameter(individual, individual2);
            executor.execute(threads[i]);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {

        }
    }

    private double onePointCross(double d1, double d2) {
        long first = Double.doubleToRawLongBits(d1);
        long second = Double.doubleToRawLongBits(d2);
        long mask = 1L;
        long result = 0;
        for (int i = 0; i < 64; i++, mask <<= 1) {
            if (random.nextBoolean()) {
                result |= mask & first;
            } else {
                result |= mask & second;
            }
        }
        System.out.format("got %.10f and %.10f put out %.10f\n", d1, d2, Double.longBitsToDouble(result));
        return Double.longBitsToDouble(result);
    }

    public static void main(String[] args) {
        (new Thread(new GAGenerateSpecies("one", 6, GASpecies.Comparators.BIOMASS))).start();
        //(new Thread(new GAGenerateSpecies("two", 4))).start();

    }


    @Override
    public synchronized void keepData(String identifier, int iteration, double biomass) {
        System.out.format("identifier:%s reported longevity=%.1f biomass=%.2f", identifier, iteration, biomass);
    }

    private class GAWorker extends Thread {
        private ArrayList<GASpecies> population;

        private void setPopulation(ArrayList<GASpecies> population) {
            this.population = population;
        }

        @Override
        public void run() {
            selection(population);
            mutate(population);
        }

        /**
         * Copy the best 1/2 GASpecies growth rates over the worst 1/2 Growth rates.
         * We are maximizing on two variables: longetivity and biomass.
         * Longetivity is easy to max out (to 5,000 iterations), so we primarily select
         * for high longetivity and secondarily select for high biomass. What this looks like in pr
         *
         * @param population
         */
        private void selection(ArrayList<GASpecies> population) {
            int halfway = POP_COUNT / 2;
            population.sort(FITNESS_CRITERION);
            int keeperIndex;
            for (int index = 0; index < halfway; index++) {
                keeperIndex = POP_COUNT - index - 1;
                System.out.format("keeper longevity: %.10f  biomass: %.10f\n",
                        population.get(keeperIndex).getLongevity(), population.get(keeperIndex).getBiomass());
                System.out.format("leaver longevity: %.10f  biomass: %.10f\n",
                        population.get(index).getLongevity(), population.get(index).getBiomass());
                population.get(index).setP(population.get(keeperIndex).getP());
            }
        }

        private void mutate(ArrayList<GASpecies> population) {
            for (GASpecies f : population) {
                long number = Double.doubleToLongBits(f.getP());
                long mask = 1;
                for (int i = 0; i < 52; i++) {
                    if (random.nextDouble() < MUT_PROB) {
                        number = number ^ mask;
                    }
                    mask <<= 1;
                }
                f.setP(Double.longBitsToDouble(number));
            }
        }
    }

    /**
     * Copy the best 1/2 GASpecies growth rates over the worst 1/2 Growth rates.
     * We are maximizing on two variables: longetivity and biomass.
     * Longetivity is easy to max out (to 5,000 iterations), so we primarily select
     * for high longetivity and secondarily select for high biomass. What this looks like in pr
     *
     * @param population
     */
    private void selection(ArrayList<GASpecies> population) {
        int halfway = POP_COUNT / 2;
        population.sort(FITNESS_CRITERION);
        int keeperIndex;
        for (int index = 0; index < halfway; index++) {
            keeperIndex = POP_COUNT - index - 1;
            System.out.format("keeper longevity: %.10f  biomass: %.10f\n",
                    population.get(keeperIndex).getLongevity(), population.get(keeperIndex).getBiomass());
            System.out.format("leaver longevity: %.10f  biomass: %.10f\n",
                    population.get(index).getLongevity(), population.get(index).getBiomass());
            population.get(index).setP(population.get(keeperIndex).getP());
        }
    }

    private void mutate(ArrayList<GASpecies> population) {
        for (GASpecies f : population) {
            long number = Double.doubleToLongBits(f.getP());
            long mask = 1;
            for (int i = 0; i < 52; i++) {
                if (random.nextDouble() < MUT_PROB) {
                    number = number ^ mask;
                }
                mask <<= 1;
            }
            f.setP(Double.longBitsToDouble(number));
        }
    }
}


