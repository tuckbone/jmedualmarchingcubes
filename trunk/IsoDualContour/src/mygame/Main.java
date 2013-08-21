package mygame;

import mygame.dualmarchingcubes.MeshBuilder;
import mygame.dualmarchingcubes.OctreeNode;
import mygame.dualmarchingcubes.DualGridGenerator;
import mygame.dualmarchingcubes.IsoSurface;
import mygame.dualmarchingcubes.VolumeSource;
import mygame.dualmarchingcubes.OctreeNodeSplitPolicy;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import mygame.dualmarchingcubes.Chunk;
import mygame.dualmarchingcubes.ChunkAppState;
import mygame.dualmarchingcubes.ChunkParameters;
import mygame.dualmarchingcubes.source.FloatGridSource;
import mygame.sourcegeneration.GeneratorData;
import mygame.sourcegeneration.GoursatSurface;
import mygame.sourcegeneration.SourceGenerator;

public class Main extends SimpleApplication {

    private Geometry geom;
    private FloatGridSource source;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    
    @Override
    public void simpleInitApp() {
        //source = new FloatGridSource(new Vector3f(1f, 1f, 1f), 256, 128, 256, 0);
      //  LoadNoise.loadNoise(0, 0, 0, 256, 128, 256, 1f, (FloatGridSource)source);

        source = SourceGenerator.getSource(new GoursatSurface(), 5);
        
        //Mesh mesh = new Mesh();
       // System.out.println(mesh.getTriangleCount());
        
       // return;
        /*
         * Stuff not working yet
         * */
        
         ChunkParameters parameter = new ChunkParameters();
         
         parameter.baseError=1.8f;
         parameter.createGeometryFromLevel = 3;
         parameter.errorMultiplicator=0.9f;
         parameter.maxScreenSpaceError = 30;
         parameter.scale = 1;
         parameter.skirtFactor = 0.7f;
         parameter.updateFrom = Vector3f.ZERO;
         parameter.updateTo = Vector3f.ZERO;
         parameter.source = source;
         parameter.material = getMaterial();
         
         Chunk chunk = new Chunk();
         chunk.load(rootNode, Vector3f.ZERO, new Vector3f(source.getWidth(),source.getHeight(),source.getDepth()), 5, parameter);
         stateManager.attach(new ChunkAppState(chunk));

         /*
        geom = new Geometry("");
        geom.setMaterial(getMaterial());
        
        rootNode.attachChild(geom);

        dualMarchingCubes();
         */

        
         makeLight();
    }


    public void dualMarchingCubes() {
        //Dual Marching Cubes
        geom.setMesh(new Mesh());
        
        FloatGridSource gridSource = (FloatGridSource)source;//SourceGenerator.getSource(new Sphere(),0);
        float v = gridSource.getWidth();
        if(v <  gridSource.getHeight())
           v =  gridSource.getHeight();
        if(v <  gridSource.getDepth())
           v =  gridSource.getDepth();

        
        OctreeNode octNode = new OctreeNode(Vector3f.ZERO, new Vector3f(v, v, v));
        octNode.split(new OctreeNodeSplitPolicy(gridSource, 2f, 1.5f), gridSource, 1.8f);

        IsoSurface isoSurface = new IsoSurface(gridSource);
        MeshBuilder meshBuilder = new MeshBuilder();
        DualGridGenerator dualGridGenerator = new DualGridGenerator();

        dualGridGenerator.generateDualGrid(octNode, isoSurface, meshBuilder, 0.5f, Vector3f.ZERO, new Vector3f(v, v, v), true);

         Mesh mesh = meshBuilder.generateMesh();
         geom.setMesh(mesh);

        
         //Show the Octree
       // Geometry g = DebugVisualisation.visualizeOctree(octNode,assetManager);
      //  rootNode.attachChild(g);
         
        //Show the Dualgrid
       // g = DebugVisualisation.visualizeDualGrid(dualGridGenerator,assetManager);
       // rootNode.attachChild(g);
    }
    

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    public void makeLight()
    {

        cam.setLocation(new Vector3f(120, 120, 120));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.45f, -.501f, -.60f).normalizeLocal());
        rootNode.addLight(sun);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        flyCam.setMoveSpeed(50);
        
        flyCam.setDragToRotate(true);

        //for a nicer look ;)
        rootNode.attachChild(SkyFactory.createSky(
                assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
    }
    
    public Material getMaterial() {

        float grassScale = 64;
        float dirtScale = 16;
        float rockScale = 128;

        Material matTerrain = new Material(assetManager, "Materials/TerrainLighting.j3md");
        matTerrain.setBoolean("useTriPlanarMapping", true);
        matTerrain.setFloat("Shininess", 0.0f);

        //   ALPHA map (for splat textures)
        matTerrain.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alpha1.png"));
        //   matTerrain.setTexture("AlphaMap_1", assetManager.loadTexture("Textures/Terrain/splat/alpha2.png"));
        // this material also supports 'AlphaMap_2', so you can get up to 12 diffuse textures

        // HEIGHTMAP image (for the terrain heightmap)
        //  Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");

        // DIRT texture, Diffuse textures 0 to 3 use the first AlphaMap
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(Texture.WrapMode.Repeat);
        matTerrain.setTexture("DiffuseMap", dirt);
        //  matTerrain.setFloat("DiffuseMap_0_scale", dirtScale);

        // GRASS texture
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        matTerrain.setTexture("DiffuseMap_1", grass);
        //   matTerrain.setFloat("DiffuseMap_1_scale", grassScale);

        // ROCK texture
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(Texture.WrapMode.Repeat);
        matTerrain.setTexture("DiffuseMap_2", rock);
        //  matTerrain.setFloat("DiffuseMap_2_scale", rockScale);


        matTerrain.setFloat("DiffuseMap_0_scale", 1f / (float) (128f / grassScale));
        matTerrain.setFloat("DiffuseMap_1_scale", 1f / (float) (128f / dirtScale));
        matTerrain.setFloat("DiffuseMap_2_scale", 1f / (float) (128f / rockScale));

        return matTerrain;
    }

}
