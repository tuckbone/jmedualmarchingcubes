/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.dualmarchingcubes;

import com.jme3.math.Vector3f;

/**
 *
 * @author Karsten
 */
public interface VolumeSource {
    public float getValue(float x, float y, float z);
    public Vector3f getGradient(float x, float y, float z);
    
    public float getValue(Vector3f pos);
    public Vector3f getGradient(Vector3f pos);
}
