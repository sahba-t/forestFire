package GA;

/**
 * GAForest is used to connect a fitness score with a particular P-value (growth rate)
 * Created by Jessica on 3/28/2017.
 */
public class GAForest {
    private double P;           //growth rate
    private double longetivity; //time in which the forest remained alive
    private double biomass;     //average biomass (#live trees) over CA lifetime

    GAForest(double p){
        P = p;
    }

    public void setP(double p) {
        P = p;
    }

    public double getP() {
        return P;
    }

    public void setLongetivity(double longetivity) {
        this.longetivity = longetivity;
    }

    public double getLongetivity() {
        return longetivity;
    }

    public void setBiomass(double biomass) {
        this.biomass = biomass;
    }

    public double getBiomass() {
        return biomass;
    }
}
