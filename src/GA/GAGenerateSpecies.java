package GA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The genetic algorithm class, one species runs on one Thread
 * Created by jdudek on 3/28/2017.
 */
public class GAGenerateSpecies implements Runnable, JungleDataKeeper {

    private final static double MUT_PROB = 0.1;
    private final String species1Name;
    private String species2Name;
    private final int POP_COUNT;
    private final Random random;
    //the size of the simulation
    private final static int BOARD_SIZE = 100;
    //how many worker threads should run simultaneously
    private final static int THREAD_POOL_SIZE = 5;
    private final static int MAX_ITERATION = 15;
    //set the comparator for selection from the GA SPECIES class
    private final Comparator<GASpecies> FITNESS_CRITERION;
    private boolean TWO_SPECIES;
    private final static String datetime = new SimpleDateFormat("MMMdd_HH.mm.ss").format(new Date());
    private BufferedWriter dataWriter;
    private static boolean recordData = true;

    /**
     * The Constructor for one species
     *
     * @param speciesName       the name for the species we are optimizing
     * @param populationCount   the number of initial population
     * @param fitnessComparator the comparator for comparing individuals
     */
    GAGenerateSpecies(String speciesName, int populationCount, Comparator<GASpecies> fitnessComparator) {
        TWO_SPECIES = false;
        this.species1Name = speciesName;
        POP_COUNT = populationCount;
        this.random = new Random();
        this.species2Name = null;
        this.FITNESS_CRITERION = fitnessComparator;
        try {
            dataWriter = initialDataFiles();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //generateSpecies(random);

    }

    /**
     * The Constructor for two species
     *
     * @param species1Name      the name for the first species we are optimizing
     * @param species2Name      the name for the second species we are optimizing
     * @param populationCount   the number of initial population
     * @param fitnessComparator the comparator for comparing individuals
     */
    GAGenerateSpecies(String species1Name, String species2Name, int populationCount, Comparator<GASpecies> fitnessComparator) {
        this(species1Name, populationCount, fitnessComparator);
        this.species2Name = species2Name;
        TWO_SPECIES = true;
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
//                for (GASpecies individual : speciesPopulation) {
//                    keepData(individual.getIdentifier(), iteration, individual.getBiomass());
//                }
//                for (GASpecies individual : speciesPopulation2) {
//                    keepData(individual.getIdentifier(), iteration, individual.getBiomass());
//                }

            } else {
                getFitness(speciesPopulation, fitnessExecutor, iteration, fitnessThreads);
//                for (GASpecies individual : speciesPopulation) {
//                    keepData(individual.getIdentifier(), iteration, individual.getBiomass());
//                }
            }


            // I think we should not evolve our last generation
            if (iteration < MAX_ITERATION) {
                if (!TWO_SPECIES) {
                    try {
                        selection(speciesPopulation);
                        mutate(speciesPopulation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {

                    worker1.setPopulation(speciesPopulation, 1);
                    worker2.setPopulation(speciesPopulation2, 2);
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

        System.out.format("Species %s evolved growth rate: %.10f\n", species1Name, speciesPopulation.get(POP_COUNT - 1).getP());
        if (TWO_SPECIES) {
            System.out.format("Species %s evolved growth rate: %.10f\n", species2Name, speciesPopulation2.get(POP_COUNT - 1).getP());
        }
        if (dataWriter != null) {
            try {
                dataWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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


    public static void main(String[] args) {
        (new Thread(new GAGenerateSpecies("one", "two", 20, GASpecies.Comparators.LONGETIVITYANDBIOMASS))).start();

    }

    private BufferedWriter initialDataFiles() throws IOException {
        Path dir = Paths.get("./data");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path file = dir.resolve("GAdata_" + datetime + ".csv");
        return Files.newBufferedWriter(file);

    }

    /**
     * @param average    whether record the fitness average or just the fittest
     * @param population the sorted population
     * @return true of operation successful
     */
    private synchronized boolean recordFitness(boolean average, ArrayList<GASpecies> population) throws IOException {

        if (average) {
            double longevity = 0;
            double bioMass = 0;
            for (GASpecies individual : population) {
                longevity += individual.getLongevity();
                bioMass += individual.getBiomass();
            }
            bioMass = bioMass / (double) POP_COUNT;
            longevity = longevity / (double) POP_COUNT;
            dataWriter.write(bioMass + "," + longevity + "\n");

        } else {
            GASpecies individual = population.get(population.size() - 1);
            dataWriter.write(+individual.getBiomass() + "," + individual.getLongevity() + "\n");
        }
        return true;
    }

    @Override
    public synchronized void keepData(String identifier, int iteration, double biomass) {
        //System.out.format("identifier:%s reported longevity=%.1f biomass=%.2f", identifier, iteration, biomass);
        new File("data/").mkdirs();
        try {
            File GAdata = new File("data/GAdata_" + datetime + ".csv");
            FileWriter fileWriter;
            StringBuilder sb = new StringBuilder();

            if (!GAdata.isFile()) {
                fileWriter = new FileWriter(GAdata);
                sb.append("identifier");
                sb.append(',');
                sb.append("iteration");
                sb.append(',');
                sb.append("biomass");
                sb.append('\n');
            } else {
                fileWriter = new FileWriter(GAdata, true);
            }

            sb.append(identifier);
            sb.append(',');
            sb.append(iteration);
            sb.append(',');
            sb.append(biomass);
            sb.append('\n');

            fileWriter.write(sb.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class GAWorker extends Thread {
        private ArrayList<GASpecies> population;
        private int populationIdentifier;

        private void setPopulation(ArrayList<GASpecies> population, int populationIdentifier) {
            this.population = population;
            this.populationIdentifier = populationIdentifier;
        }

        @Override
        public void run() {
            try {
                selection(population);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mutate(population);
        }

        private void selection(ArrayList<GASpecies> population) throws IOException {
            int halfway = POP_COUNT / 2;
            population.sort(FITNESS_CRITERION);
            if (recordData) {
                if (populationIdentifier == 1)
                    recordFitness(true, population);
            }
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
     * for high longetivity and secondarily select for high biomass.
     *
     * @param population
     */
    private void selection(ArrayList<GASpecies> population) throws IOException {
        int halfway = POP_COUNT / 2;
        population.sort(FITNESS_CRITERION);
        if (recordData) {
            recordFitness(true, population);
        }
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


