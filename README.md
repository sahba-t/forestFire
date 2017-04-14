# <b>CS523- forestFire</b><br>
TEAM MEMBERS: Sahba Tashakkori Jessica Dudek<br>
There will be a bash script provided to facilitate execution<br>
USE java 8 making sure <b>java-fx is available</b><br>

How Run:

1) Clone the Repo
2) CD into the forestFire Directory
3) add execute permission for jars -> chmod +x *.jar

To Run the GUI Simulation
arguments are: p1 (float) p2 (float 0 if you don't want the second specie) firefightercount (int zero if you dont want firefighters);
example java -jar gui.jar 0.03 0 100


To run the GA:
arguments:
"1" or "2" (string): if you wish yo have one/tho species
popcount (int) the size of intial population 
maxItr: how many generation
example java -jar GA.jar 1 20 15

