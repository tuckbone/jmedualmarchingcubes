/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.dualmarchingcubes.source;

import com.jme3.math.Vector3f;
import mygame.dualmarchingcubes.VolumeSource;

/**
 *
 * @author Karsten
 */
public class FloatGridSource implements VolumeSource{

    /// The raw volume data.
    private float data[][][];
    /// To have a little bit faster data access.
    private int depthTimesHeight;
    /// The maximum absolute density value to be written into the data when combining,
    /// influencing the compression rate on serialization.
    private float maxClampedAbsoluteDensity = 5;
    private boolean trilinearValue = true;
   // private boolean trilinearGradient = false;
    private boolean sobelGradient = true;
    /// The texture width.
    private int width;
    /// The texture height.
    private int height;
    
    private int depth;
    
    /// The scale of the position based on the world width(x)/height(y)/depth(z).
    private Vector3f scale;
    /// The texture depth.
    
    
    public FloatGridSource(Vector3f scale, int width, int height, int depth, float maxClampedAbsoluteDensity)
    {
        this.maxClampedAbsoluteDensity=maxClampedAbsoluteDensity;
        this.scale=scale;
        data = new float[width][height][depth];
        
        this.width=width;
        this.height=height;
        this.depth=depth;
        
        for(int x=0;x<width;x++)
           for(int y=0;y<height;y++)
               for(int z=0;z<depth;z++)
                   data[x][y][z] = -1;
        
    }

    public void setVolumeGridValue(int x, int y, int z, float value) {
        // Clamp if wanted.
        if (maxClampedAbsoluteDensity != 0) {
            if (value > maxClampedAbsoluteDensity) {
                value = maxClampedAbsoluteDensity;
            } else if (value < -maxClampedAbsoluteDensity) {
                value = (float) (-maxClampedAbsoluteDensity);
            }
        }



        data[x][y][z] = value;
    }

    public float getVolumeGridValue(int x, int y, int z) {
        if (x >= getWidth()) {
            x = getWidth() - 1;
        } else if (x < 0) {
            x = 0;
        }

        if (y >= getHeight()) {
            y = getHeight() - 1;
        } else if (y < 0) {
            y = 0;
        }

        if (z >= getDepth()) {
            z = getDepth() - 1;
        } else if (z < 0) {
            z = 0;
        }

        return data[x][y][z];
    }
    
            /** Gets a gradient of a point with optional sobel blurring.
        @param x
            The x coordinate of the point.
        @param y
            The x coordinate of the point.
        @param z
            The x coordinate of the point.
        */
        public Vector3f getGradient(float fx, float fy, float fz)
        {
            int x = (int)(fx* scale.x + 0.5f);
            int y = (int)(fy* scale.y + 0.5f);
            int z = (int)(fz* scale.z + 0.5f);
            
            if (sobelGradient)
            {
                // Calculate gradient like in the original MC paper but mix a bit of Sobel in
                Vector3f rfNormal = new Vector3f(
                (getVolumeGridValue(x + 1, y - 1, z) - getVolumeGridValue(x - 1, y - 1, z))
                        + 2.0f * (getVolumeGridValue(x + 1, y, z) - getVolumeGridValue(x - 1, y, z))
                        + (getVolumeGridValue(x + 1, y + 1, z) - getVolumeGridValue(x - 1, y + 1, z)),
                (getVolumeGridValue(x, y + 1, z - 1) - getVolumeGridValue(x, y - 1, z - 1))
                    + 2.0f * (getVolumeGridValue(x, y + 1, z) - getVolumeGridValue(x, y - 1, z))
                    + (getVolumeGridValue(x, y + 1, z + 1) - getVolumeGridValue(x, y - 1, z + 1)),
                (getVolumeGridValue(x - 1, y, z + 1) - getVolumeGridValue(x - 1, y, z - 1))
                    + 2.0f * (getVolumeGridValue(x, y, z + 1) - getVolumeGridValue(x, y, z - 1))
                    + (getVolumeGridValue(x + 1, y, z + 1) - getVolumeGridValue(x + 1, y, z - 1)));
                rfNormal.multLocal(-1f);
                rfNormal.normalizeLocal();
                return rfNormal;
            }
            // Calculate gradient like in the original MC paper
            Vector3f rfNormal = new Vector3f(
                getVolumeGridValue(x - 1, y, z) - getVolumeGridValue(x + 1, y, z),
                getVolumeGridValue(x, y - 1, z) - getVolumeGridValue(x, y + 1, z),
                getVolumeGridValue(x, y, z - 1) - getVolumeGridValue(x, y, z + 1));
            rfNormal.normalizeLocal();
            return rfNormal;
        }
        
     
    public float getValue(Vector3f position)
    {
        Vector3f scaledPosition = new Vector3f(position.x * scale.x, position.y * scale.y, position.z * scale.z);
        float value;
        if (trilinearValue)
        {
            int x0 = (int)scaledPosition.x;
            int x1 = (int)Math.ceil(scaledPosition.x);
            int y0 = (int)scaledPosition.y;
            int y1 = (int)Math.ceil(scaledPosition.y);
            int z0 = (int)scaledPosition.z;
            int z1 = (int)Math.ceil(scaledPosition.z);

            float dX = scaledPosition.x - x0;
            float dY = scaledPosition.y - y0;
            float dZ = scaledPosition.z - z0;

            float f000 = getVolumeGridValue(x0, y0, z0);
            float f100 = getVolumeGridValue(x1, y0, z0);
            float f010 = getVolumeGridValue(x0, y1, z0);
            float f001 = getVolumeGridValue(x0, y0, z1);
            float f101 = getVolumeGridValue(x1, y0, z1);
            float f011 = getVolumeGridValue(x0, y1, z1);
            float f110 = getVolumeGridValue(x1, y1, z0);
            float f111 = getVolumeGridValue(x1, y1, z1);

            float oneMinX = 1.0f - dX;
            float oneMinY = 1.0f - dY;
            float oneMinZ = 1.0f - dZ;
            float oneMinXoneMinY = oneMinX * oneMinY;
            float dXOneMinY = dX * oneMinY;

            value = oneMinZ * (f000 * oneMinXoneMinY
                + f100 * dXOneMinY
                + f010 * oneMinX * dY)
                + dZ * (f001 * oneMinXoneMinY
                + f101 * dXOneMinY
                + f011 * oneMinX * dY)
                + dX * dY * (f110 * oneMinZ
                + f111 * dZ);
        
        }
        else
        {
            // Nearest neighbour else
            int x = (int)(scaledPosition.x + 0.5);
            int y = (int)(scaledPosition.y + 0.5);
            int z = (int)(scaledPosition.z + 0.5);
            value = getVolumeGridValue(x, y, z);
        }
        return value;
    }

    public float getValue(float x, float y, float z) {
        return getValue(new Vector3f(x,y,z));
    }

    public Vector3f getGradient(Vector3f pos) {
        return getGradient(pos.x,pos.y,pos.z);
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the depth
     */
    public int getDepth() {
        return depth;
    }
    

   
}
