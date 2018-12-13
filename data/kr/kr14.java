//Karl Ramberg

import java.util.Random;

// class for generating elevation values and others based on elevation
public class Generator {

    // elevation, political, climate, biome map values
    private double[][] elevation;
    private double[][] political;
    private double[][] climate;
    private double[][] biome;

    private Random r = new Random();

    private int width, height;

    public Generator(int width, int height) {
        this.width = width;
        this.height = height;
    }

    // elevation is default
    public double[][] generateNewWorld() {
        elevation = new double[width][height];

        // TODO elevation gen
        int col = 0;
        for(int i = 0; i < elevation.length; i++){
            for(int j = 0; j < elevation[i].length; j++){
                elevation[i][j] = r.nextFloat()*(2.0)-1.0; // temp val
            }
        }

        // TODO pol derives from elev

        // TODO clim derives from elev

        // TODO bio derives from elev

        return elevation;
    }

    // very WIP perlin noise
    public double perlin(double x, double y) {
        int x0 = floor(x);
        int x1 = x0 + 1;
        int y0 = floor(y);
        int y1 = y0 + 1;

        double sx = x - (double)x0;
        double sy = y - (double)y0;

        double n0, n1, ix0, ix1;
        n0 = grad(x0, y0, x, y);
        n1 = grad(x1, y0, x, y);
        ix0 = lerp(n0, n1, sx);
        n0 = grad(x0, y1, x, y);
        n1 = grad(x1, y1, x, y);
        ix1 = lerp(n0, n1, sx);
        return lerp(ix0, ix1, sy);
    }

    public int floor(double num) {
        return (int) (num - (num%1.0));
    }

    public double lerp(double a0, double a1, double w) {
        return (1.0 - w)*a0 + w*a1;
    }

    public double grad(int ix, int iy, double x, double y) {
        return 1.0;
    }

    public double[][] switchType(int type) {
        switch(type){
            case 0: return elevation;
            case 1: return political;
            case 2: return climate;
            case 3: return biome;
            default: return elevation;
        }
    }
}