/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.sourcegeneration;

/**
 *
 * @author Karsten
 */
public class GoursatSurface implements GeneratorData{
        public float[][] getDim() {

        float[][] res = 
            {{-2, 2, 0.02f},
            {-2, 2, 0.02f},
            {-2, 2, 0.02f}};

        return res;
    }

    public float getValue(float x, float y, float z) {
        return (float)(Math.pow(x,4) + Math.pow(y,4) + Math.pow(z,4) - 1.5 * (x*x  + y*y + z*z) + 1);
    }
}
