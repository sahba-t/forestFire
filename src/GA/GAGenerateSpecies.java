package GA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * The genetic algorithm class, one species runs on one Thread
 * Created by jdudek on 3/28/2017.
 */
public class GAGenerateSpecies implements Runnable{

    private final static double MUT_PROB = 0.1;
    private final String speciesName;
    private final int POP_COUNT;
    private ArrayList<GAForest> speciesPopulation;
    private Random random;

    /**
     * Constructor.
     * @param speciesName
     * @param populationCount
     */
    GAGenerateSpecies(String speciesName, int populationCount){
        this.speciesName = speciesName;
        POP_COUNT = populationCount;

        //generateSpecies(random);

    }

    @Override
    public void run() {

        this.random = new Random();
        speciesPopulation =  new ArrayList<>();
        initializePopulation(speciesPopulation, random);
        System.out.format("Starting evolving species %s\n", speciesName);
        int iteration = 0; //temporary until I find a good stopping condition
        while(iteration < 10){
            getFitness(speciesPopulation);

            selection(speciesPopulation);

            mutate(speciesPopulation);
            iteration++;
            System.out.println(iteration);
        }
        Collections.sort(speciesPopulation, GAForest.Comparators.LONGETIVITYANDBIOMASS);
        Collections.reverse(speciesPopulation);

        System.out.format("Species %s evolved growth rate: %.10f\n", speciesName, speciesPopulation.get(0).getP());
    }

    private void initializePopulation(ArrayList<GAForest>population, Random random){
        for(int i = 0; i < POP_COUNT; i++){
            population.add(new GAForest(random.nextDouble()));
        }
    }



    private void getFitness(ArrayList<GAForest> population){
        for (GAForest f : population) {
            GAFitness gaFitness = new GAFitness(250, f.getP());
            double[] scores = gaFitness.simulate();
            f.setLongetivity(scores[0]);
            f.setBiomass(scores[1]);
        }
    }

    /**
     * Copy the best 1/2 GAForest growth rates over the worst 1/2 Growth rates.
     * We are maximizing on two variables: longetivity and biomass.
     * Longetivity is easy to max out (to 5,000 iterations), so we primarily select
     * for high longetivity and secondarily select for high biomass. What this looks like in pr
     * @param population
     */
    private void selection(ArrayList<GAForest> population){
        int halfway = POP_COUNT/2;
        Collections.sort(population, GAForest.Comparators.LONGETIVITYANDBIOMASS);
        Collections.reverse(population);
        int keeperIndex = 0;
        for (int index = halfway; index < POP_COUNT; index++){
            /*
            System.out.format("keeper longetivity: %.10f  biomass: %.10f\n",
                    population.get(keeperIndex).getLongetivity(),population.get(keeperIndex).getBiomass());
            System.out.format("leaver longetivity: %.10f  biomass: %.10f\n",
                    population.get(index).getLongetivity(),population.get(index).getBiomass());*/
            population.get(index).setP(population.get(keeperIndex).getP());
            keeperIndex++;
        }
    }


    private void mutate(ArrayList<GAForest> population) {
        for (GAForest f : population) {
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
        (new Thread( new GAGenerateSpecies("one", 4))).start();
        (new Thread( new GAGenerateSpecies("two", 4))).start();

    }


}


