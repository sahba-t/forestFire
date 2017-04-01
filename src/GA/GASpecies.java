package GA;

import java.util.Comparator;

/**
 * GASpecies is used to connect a fitness score with a particular P-value (growth rate)
 * Created by Jessica on 3/28/2017.
 */
public class GASpecies {
    private double P;           //growth rate
    private double longevity; //time in which the forest remained alive
    private double biomass;     //average biomass (#live trees) over CA lifetime
    private String identifier;

    GASpecies(double p) {
        P = p;
    }

    GASpecies(double p, String identifier) {
        this(p);
        this.identifier = identifier;
    }

    void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    void setP(double p) {
        P = p;
    }

    double getP() {
        return P;
    }

    void setLongevity(double longevity) {
        this.longevity = longevity;
    }

    double getLongevity() {
        return longevity;
    }

    void setBiomass(double biomass) {
        this.biomass = biomass;
    }

    double getBiomass() {
        return biomass;
    }

    static class Comparators {
        static final Comparator<GASpecies> LONGETIVITY =
                (GASpecies o1, GASpecies o2) -> Double.compare(o1.getLongevity(), o2.getLongevity());
        static final Comparator<GASpecies> BIOMASS =
                (GASpecies o1, GASpecies o2) -> Double.compare(o1.getBiomass(), o2.getBiomass());
        static final Comparator<GASpecies> LONGETIVITYANDBIOMASS =
                (GASpecies o1, GASpecies o2) -> LONGETIVITY.thenComparing(BIOMASS).compare(o1, o2);
    }

    @Override
    public String toString() {
        return "GASpecies{" +
                "identifier: " + identifier + ", " +
                "P=" + P +
                ", longevity=" + longevity +
                ", biomass=" + biomass +
                '}';
    }
}
