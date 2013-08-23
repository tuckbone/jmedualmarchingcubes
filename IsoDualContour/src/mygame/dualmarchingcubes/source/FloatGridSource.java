/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.dualmarchingcubes.source;

import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import mygame.dualmarchingcubes.VolumeSource;

/**
 *
 * @author Karsten
 */
public class FloatGridSource extends VolumeSource{

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
        
     
    public float getValue(float posX, float posY, float posZ)
    {
        float scaledPosX = posX * scale.x;
        float scaledPosY = posY * scale.y; 
        float scaledPosZ = posZ * scale.z;
        
        float value;
        if (trilinearValue)
        {
            int x0 = (int)scaledPosX;
            int x1 = (int)FastMath.ceil(scaledPosX);
            int y0 = (int)scaledPosY;
            int y1 = (int)FastMath.ceil(scaledPosY);
            int z0 = (int)scaledPosZ;
            int z1 = (int)FastMath.ceil(scaledPosZ);

            float dX = scaledPosX - x0;
            float dY = scaledPosY - y0;
            float dZ = scaledPosZ - z0;

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
            int x = (int)(scaledPosX + 0.5);
            int y = (int)(scaledPosY + 0.5);
            int z = (int)(scaledPosZ + 0.5);
            value = getVolumeGridValue(x, y, z);
        }
        return value;
    }

    public float getValue(Vector3f pos) {
        return getValue(pos.x,pos.y,pos.z);
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
    

   public void addSphere(Vector3f center, float radius)
    {
        float worldWidthScale = 1.0f / scale.x;
        float worldHeightScale = 1.0f / scale.y;
        float  worldDepthScale = 1.0f / scale.z;
        
        
        float radiusSqrt = (float)(Math.sqrt(radius)+radius)/2;


        // No need for trilineaer interpolation here as we iterate over the
        // cells anyway.
        boolean oldTrilinearValue = trilinearValue;
        trilinearValue = false;
        float value;
       // int x, y;
        Vector3f scaledCenter = new Vector3f (center.x * scale.x, center.y * scale.y, center.z * scale.z);
        int xStart = clamp((int)(scaledCenter.x - radius * scale.x), 0, width);
        int xEnd = clamp((int)(scaledCenter.x + radius * scale.x), 0, width);
        int yStart = clamp((int)(scaledCenter.y - radius * scale.y), 0, height);
        int yEnd = clamp((int)(scaledCenter.y + radius * scale.y), 0, height);
        int zStart = clamp((int)(scaledCenter.z - radius * scale.z), 0, depth);
        int zEnd = clamp((int)(scaledCenter.z + radius * scale.z), 0, depth);
        Vector3f pos = new Vector3f();
        for (int z = zStart; z < zEnd; ++z)
        {
            for (int y = yStart; y < yEnd; ++y)
            {
                for (int x = xStart; x < xEnd; ++x)
                {
                    pos.x = x * worldWidthScale;
                    pos.y =  y * worldHeightScale;
                    pos.z = z * worldDepthScale;
                    
                    /*float a = (center.x-pos.x);
                    float b = (center.y-pos.y);
                    float c = (center.z-pos.z);*/
                    
                    float otherValue = radiusSqrt - pos.distance(scaledCenter);//;a*a+b*b+c*c;
                    // float otherValue = radius - pos.distance(scaledCenter)-1f;
                    
                    value = Math.max(getValue(pos), otherValue);
                    setVolumeGridValue(x, y, z, value);
                }
            }
        }

        trilinearValue = oldTrilinearValue;
    }
   
   private static int clamp(int  value, int  min, int max) {
    return Math.max(min, Math.min(max, value));
}
}
