/* This can be used to calculate the cost of building a wardrobe. As so many parts can affect the building of a wardobe, carpenters are often hesitant to give 
*  the right price to clients */

// Go to region surrounded by '---' for input
// Wardrobe object with attributes that are relevant measurements or calculations with each part
class WardrobeParts{
    // Core attributions
    double m2Of1Piece;
    double amount;
    double totalM2;
    
    // Material's attributions
    double edging;
    double xfactor;
    double materialTotal;
    
    // Finish Attributes
    
    double finishPrice;
    double finishedSides;
    double finishTotal;
    
    // Labour attributes
    
    double basicCut;
    double leadingEdge;
    double labourTotal;

    // Constructor 
    WardrobeParts(double m2Of1Piece, double amount, double edging, double xfactor, double finishPrice, double finishedSides, double basicCut, double leadingEdge){

        //the formula here are the common formula used in the spreadsheet
        this.m2Of1Piece = m2Of1Piece;
        this.amount = amount;
        this.totalM2 = this.m2Of1Piece * this.amount;
        this.edging = edging;
        this.materialTotal = xfactor * this.totalM2;
        this.finishPrice = finishPrice;
        this.finishedSides = finishedSides;
        this.finishTotal = this.finishPrice * this.finishedSides * this.totalM2;
        this.basicCut = basicCut;
        this.leadingEdge = leadingEdge;
        this.labourTotal = this.amount * (this.basicCut + (5 * this.leadingEdge) + (10 * this.m2Of1Piece));
        
    } 
}



public class Main {
    
    //----------------------------------------------------------------------------------
    
    // Your input values would be placed here - do not change anything outside of the --- region
    public static void main(String[] args) {
        
        // Spell your materials EXACTLY as it is in the spreadsheet and in the order of:
        // structure material, shelves' material, drawers' material, backs' material, fronts 22mm
        String[] material = {"MRMDF18", "MRMDF25", "MRMDF15", "MRMDF12", "mdf/m2"};
        
        // Do the same here with the finish chosen, in the order of:
        //sides, top & bottom, back, shelves, drawers, drawers' fronts
        String[] finish = {"spraying", "spraying", "spraying", "priming", "spraying", "spraying"};
        
        // Replace the number inputs as you wish

	double HEIGHT = 2.4;
	double WIDTH = 3;
	double DEPTH = 0.25;
	double DRAWER_HEIGHT = 0.3;
	double BOARD_AREA = 4;
	double LINEAR = 30;
	int NUMBER_OF_UNITS = 5;
	int NUMBER_OF_SHELVES = 15;
	int NUMBER_OF_DRAWERS = 7;

        
        System.out.printf("Value: %.2f", totalCosts( HEIGHT, WIDTH, DEPTH, DRAWER_HEIGHT, BOARD_AREA, LINEAR, NUMBER_OF_UNITS, NUMBER_OF_SHELVES, NUMBER_OF_DRAWERS, material, finish));
    }

    //--------------------------------------------------------------------------------
    
    
    // Method to add up all the costs - here is the order of the arguments you should follow for the input above. The names are kept similar to the names in the spreadsheet. I.E. the first number in the brackets above is the same as Heigt
    public static double totalCosts(double HEIGHT, double WIDTH, double DEPTH, double DRAWER_HEIGHT, double BOARD_AREA,double LINEAR, int NUMBER_OF_UNITS, int NUMBER_OF_SHELVES, int NUMBER_OF_DRAWERS, String[] material, String[] finish) {
        
        // Initial calculations
        double sideArea = HEIGHT * WIDTH;
        double volume = HEIGHT * WIDTH * DEPTH;
        double aveUnitWidth = WIDTH / NUMBER_OF_UNITS;
        double unitPerimeterTop = 2 * (DEPTH + WIDTH / NUMBER_OF_UNITS);
        double doorPerimeter = 2 * (HEIGHT + aveUnitWidth);
        double baseArea = DEPTH * WIDTH / NUMBER_OF_UNITS;
        
        // Get prices for materials
        double[] matPrices = priceOfMaterials(material);
        // Get prices for finish
        double[] finPrices = priceOfFinish(finish);
        // This is done using a hashmap near the bottom
        
        
         /* All the important parts of the Wardrobe are outlines below. The arguments are
            the same as the arguments in the WardrobeMeasurements constructor in the class at               the top. This information is taken from the cells (where the formula were unique)               on the spreadsheet and simplified where necessary */
        
        // Calculations for base
        WardrobeParts base = new WardrobeParts(WIDTH, 1, 0, LINEAR, 0, 0, 0, 0);
        base.labourTotal = 0;
        
        // Calculations for sides
        WardrobeParts sides = new WardrobeParts(HEIGHT * DEPTH, NUMBER_OF_UNITS * 2, HEIGHT * NUMBER_OF_UNITS * 2, matPrices[0] / BOARD_AREA, finPrices[0], 1, 18, HEIGHT);
        
        // Calculations for topAndBottom
        WardrobeParts topAndBottom = new WardrobeParts(baseArea, NUMBER_OF_UNITS * 2, WIDTH * 2, matPrices[0] / BOARD_AREA, finPrices[1], 1, 18, aveUnitWidth);
        
        // Calculations for back
        WardrobeParts back = new WardrobeParts(HEIGHT * aveUnitWidth, NUMBER_OF_UNITS, 0, matPrices[3] / BOARD_AREA, finPrices[2], 1, 18, 0);
        
        // Calculations for shelves
        WardrobeParts shelves = new WardrobeParts(baseArea, NUMBER_OF_SHELVES, NUMBER_OF_SHELVES * aveUnitWidth, matPrices[1] / BOARD_AREA, finPrices[3], 2, 18, aveUnitWidth);
        
        // Calculations for drawers
        WardrobeParts drawers = new WardrobeParts((unitPerimeterTop * DRAWER_HEIGHT) + baseArea, NUMBER_OF_DRAWERS, NUMBER_OF_DRAWERS * unitPerimeterTop, matPrices[2] / BOARD_AREA, finPrices[4], 2, 90, unitPerimeterTop);
        
        // Calculations for drawersfront
        WardrobeParts drawersFront = new WardrobeParts(aveUnitWidth * DRAWER_HEIGHT, NUMBER_OF_DRAWERS, 0, matPrices[4], finPrices[5], 2, 0, 0);
        drawersFront.labourTotal = NUMBER_OF_DRAWERS;
        
        // Each of the parts above are stored in this array to be looped through
        WardrobeParts[] wardrobeParts = {base, sides, topAndBottom, back, shelves, drawers, drawersFront};
        
        // This method is used to loop through the different wardrobe parts and add up the      totals
        double totalCosts = costOf(wardrobeParts);
        
        //  This is the ouput
        return totalCosts;     
        
    }
    
    
    // Method to calculate total costs of materials
    public static double costOf(WardrobeParts[] wardrobeParts){
        
        // Each subtotal is added up separately and initialised at 0
        double materialTotalForAll = 0;
        double finishTotalForAll = 0;
        double labourTotalForAll = 0;
        
        // By going through part by part (e.g. side, drawer ets) and adding to each subtotal
        for (WardrobeParts w : wardrobeParts){
            materialTotalForAll += w.materialTotal;
            finishTotalForAll += w.finishTotal;
            labourTotalForAll += w.labourTotal;
        }
        
        // Each of the subtotals are then added together for complete TOTAL
        return labourTotalForAll + materialTotalForAll + finishTotalForAll;
    }
    
    
    
    //Here a dictionary is created for materials to map the prices to. This is using a Hash Map
    public static double[] priceOfMaterials(String[] material){
        
         Map<String, Double> dictionary = new HashMap<String, Double>();

        // List of material used - copied directly from the spreadsheet
        String[] materials = {"MRMDF30",
                            "MRMDF25",
                            "MRMDF22",
                            "MRMDF18",
                            "MRMDF15",
                            "MRMDF12",
                            "MRMDF9",
                            "MRMDF6",

                            "oak 31",
                            "oak 26",
                            "oak 19",
                            "oak 16",
                            "oak 13",
                            "oak 10",
                            "oak 7",

                            "walnut31",
                            "walnut26",
                            "walnut19",
                            "walnut13",
                            "walnut16",
                            "walnut7",

                            "whiteMDF25",
                            "whiteMDF18",
                            "whiteMDF15",
                            "whiteMDF12",
                            "whiteMDF7",
                            "whiteMDF4",

                            "birchPLY30",
                            "birchPLY24",
                            "birchPLY21",
                            "birchPLY18",
                            "birchPLY15",
                            "birchPLY12",
                            "birchPLY9",
                            "birchPLY6",
                            "birchPLY4",
                              
                            "mdf/m2",
                            "Oak/m2",
                            "Walnut/m2"

    };
        
        // Prices of materials listed above - again copied directly from the spreadsheet. 
        double prices[] = {68.6,
                        43.8,
                        46.78,
                        29.5,
                        27.55,
                        23.25,
                        20.5,
                        14.7,

                        91.35,
                        69.5,
                        46.5,
                        49.7,
                        51.83,
                        52.25,
                        40.62,

                        160,
                        125.95,
                        79.95,
                        75.95,
                        98.75,
                        66.5,

                        54.65,
                        37.5,
                        34.75,
                        35.78,
                        25.58,
                        15.3,

                        109.3,
                        68.33,
                        93.66,
                        54.9,
                        56.95,
                        42.45,
                        38.25,
                        33.5,
                        27.35,
                           
                        16.5,
                        80,
                        150


        };
         
        /* Each of the materials and prices in the lists above are in the exact order to be                corresponded with eachother. So the price of the first material listed is the first            price listed and so on.*/
        
          /*  For loop used to fill the dictionary so that each material is now directly                    associated with its price*/
        for (int i = 0; i < materials.length; i++) {
            dictionary.put(materials[i], prices[i]);
     }
         
         // Instatiate list of prices outcome - So the prices of the actual chosen materials
         double[] pricesOutcome= new double[5];
         
         // For every inputed material, find the price of that material
        // And add it to the prices array
         for (int i = 0; i < pricesOutcome.length; i++){
             String m = material[i];
             double a = dictionary.get(m);
             pricesOutcome[i] = a;
         }
         
        // Now the array is filled with the prices needed for this particular wardrobe
         return pricesOutcome;
     }
    
    // The same as above but with the finish required for each part instead of material
    public static double[] priceOfFinish(String[] material){
        
         // Again a hash Map is used to map finish with price
         Map<String, Double> PricesOfFinishes = new HashMap<String, Double>();

        // List of finish used
        String[] finishes = {"none",
                          "priming",
                          "priming and painting",
                          "oiling/varnishing",
                          "spraying"
    };
        
        // Prices of materials listed above - In the exact order as it should be
        double pricesFin[] = {0, 10, 30, 30, 56.25};
         
          //  For loop used to fill the dictionary
        for (int i = 0; i < finishes.length; i++) {
            PricesOfFinishes.put(finishes[i], pricesFin[i]);
     }
         
         // Instatiate list of prices outcome
         double[] pricesOutcomeFin= new double[6];
         
         // For every input material, find the price of that finish
         // And add it to the prices array
         for (int i = 0; i < pricesOutcomeFin.length; i++){
             String m = material[i];
             double a = PricesOfFinishes.get(m);
             pricesOutcomeFin[i] = a;
         }
        
         // Here is the array of the prices for the specific finishes chosen
         return pricesOutcomeFin;
     }
}

// Written for a carpenter to use for wardrobe price/cost calculations by Natalia