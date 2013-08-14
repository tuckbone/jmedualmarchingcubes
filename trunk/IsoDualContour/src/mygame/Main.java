package mygame;

import mygame.source.BoxSource;
import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;
import java.util.LinkedList;
import java.util.List;
import mygame.source.ShortGridSource;


public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        cam.setLocation(new Vector3f(120, 120, 120));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        ShortGridSource source = new ShortGridSource(new Vector3f(0.5f,0.5f,0.5f),256,128,256);
        LoadNoise.loadNoise(0, 0, 0, 256,128,256, 1f, source);

        //Normal Marching Cubes
       // Mesh mesh = createMeshMarchingCubes(locin, source, Vector3f.ZERO, new Vector3f(256,128,256), new Vector3f(2f, 2f, 2f));
        
        //Dual Marching Cubes
         OctreeNode octNode = new OctreeNode(Vector3f.ZERO,new Vector3f(256,256,256));
         octNode.split(new OctreeNodeSplitPolicy(source,2f, 1.5f), source, 1.5f);
        
         IsoSurface isoSurface = new IsoSurface(source);
         MeshBuilder meshBuilder = new MeshBuilder();
         DualGridGenerator dualGridGenerator = new DualGridGenerator();
        
         dualGridGenerator.generateDualGrid(octNode, isoSurface, meshBuilder, 0.5f, Vector3f.ZERO, new Vector3f(256,128,256), true);
        
         Mesh mesh = meshBuilder.generateMesh();

         //Visualisation of the DualGrid, Buggy atm
        /*
         Material matVC = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         matVC.getAdditionalRenderState().setWireframe(true);
         matVC.setColor("Color", ColorRGBA.Blue);
         visualizeDualGrid(dualGridGenerator, matVC);*/
         
         
         /*
          * Stuff not working yet
          * 
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
         chunk.load(rootNode, Vector3f.ZERO, new Vector3f(256,128,256), 5, parameter);
         stateManager.attach(new ChunkAppState(chunk));*/


        Geometry geom = new Geometry("", mesh);
        geom.setMaterial(getMaterial());

        rootNode.attachChild(geom);

        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.45f, -.501f, -.60f).normalizeLocal());
        rootNode.addLight(sun);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        flyCam.setMoveSpeed(50);


        //Visualisation of the Octree
        /*
         Material matVC = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         matVC.getAdditionalRenderState().setWireframe(true);
         matVC.setColor("Color", ColorRGBA.Blue);
         visualizeOctree(octNode,matVC);*/
        
        //for a nicer look ;)
        rootNode.attachChild(SkyFactory.createSky(
            assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public void visualizeDualGrid(DualGridGenerator dualGridGenerator, Material m) {
        for (DualGridGenerator.DualCell d : dualGridGenerator.getDualCells()) {
            visualizeDualCell(d, m);
        }
    }

    public void visualizeDualCell(DualGridGenerator.DualCell d, Material m) {
        LinkedList<Integer> indices = new LinkedList();

        int baseIndex = 0;

        indices.add(baseIndex + 0);
        indices.add(baseIndex + 1);
        indices.add(baseIndex + 1);
        indices.add(baseIndex + 2);
        indices.add(baseIndex + 2);
        indices.add(baseIndex + 3);
        indices.add(baseIndex + 3);
        indices.add(baseIndex + 0);

        indices.add(baseIndex + 4);
        indices.add(baseIndex + 5);
        indices.add(baseIndex + 5);
        indices.add(baseIndex + 6);
        indices.add(baseIndex + 6);
        indices.add(baseIndex + 7);
        indices.add(baseIndex + 7);
        indices.add(baseIndex + 4);

        indices.add(baseIndex + 0);
        indices.add(baseIndex + 4);
        indices.add(baseIndex + 1);
        indices.add(baseIndex + 5);
        indices.add(baseIndex + 2);
        indices.add(baseIndex + 6);
        indices.add(baseIndex + 3);
        indices.add(baseIndex + 7);

        baseIndex += 8;

        Geometry g = new Geometry("Box");

        Mesh mesh = new Mesh();

        System.out.println("DualCell: " + d.c[0]);

        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(d.c));
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(toIntArray(indices)));

        mesh.updateBound();
        g.setMesh(mesh);
        g.setMaterial(m);

        rootNode.attachChild(g);
    }

    int[] toIntArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        int i = 0;
        for (Integer e : list) {
            ret[i++] = e.intValue();
        }
        return ret;
    }

    public void visualizeOctree(OctreeNode treeNode, Material m) {
        Box box1 = new Box(1, 1, 1);
        Geometry g = new Geometry("Box", box1);
        g.setMaterial(m);
        g.setLocalScale((treeNode.getTo().subtract(treeNode.getFrom()).mult(0.5f)));
        g.setLocalTranslation(treeNode.getCenter());

        rootNode.attachChild(g);

        if (treeNode.isSubdivided()) {
            for (int i = 0; i < 8; i++) {
                visualizeOctree(treeNode.getChild(i), m);
            }
        }

    }

    public static Mesh createMeshMarchingCubes(int[] loc, VolumeSource source, Vector3f start, Vector3f end, Vector3f cubeSize) {

        MeshBuilder meshBuilder = new MeshBuilder();
        IsoSurface isoSurface = new IsoSurface(source);

        for (float i = start.x; i < end.x; i += cubeSize.x) {
            for (float j = start.y; j < end.y; j += cubeSize.y) {
                for (float k = 1; k < end.z; k += cubeSize.z) {

                    float[] val = {source.getValue(i, j, k),
                        source.getValue(i + cubeSize.x, j, k),
                        source.getValue(i + cubeSize.x, j, k + cubeSize.z),
                        source.getValue(i, j, k + cubeSize.z),
                        source.getValue(i, j + cubeSize.y, k),
                        source.getValue(i + cubeSize.x, j + cubeSize.y, k),
                        source.getValue(i + cubeSize.x, j + cubeSize.y, k + cubeSize.z),
                        source.getValue(i, j + cubeSize.y, k + cubeSize.z),};

                    Vector3f[] loc1 = {
                        new Vector3f(loc[0] + i + 0, loc[1] + j + 0, loc[2] + k + 0),
                        new Vector3f(loc[0] + i + cubeSize.x, loc[1] + j + 0, loc[2] + k + 0),
                        new Vector3f(loc[0] + i + cubeSize.x, loc[1] + j + 0, loc[2] + k + cubeSize.z),
                        new Vector3f(loc[0] + i + 0, loc[1] + j + 0, loc[2] + k + cubeSize.z),
                        new Vector3f(loc[0] + i + 0, loc[1] + j + cubeSize.y, loc[2] + k + 0),
                        new Vector3f(loc[0] + i + cubeSize.x, loc[1] + j + cubeSize.y, loc[2] + k + 0),
                        new Vector3f(loc[0] + i + cubeSize.x, loc[1] + j + cubeSize.y, loc[2] + k + cubeSize.z),
                        new Vector3f(loc[0] + i + 0, loc[1] + j + cubeSize.y, loc[2] + k + cubeSize.z)
                    };


                    isoSurface.addMarchingCubesTriangles(loc1, val, null, meshBuilder);

                }
            }
        }

        return meshBuilder.generateMesh();
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

        /*
        // BRICK texture
        Texture brick = assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg");
        brick.setWrap(Texture.WrapMode.Repeat);
        matTerrain.setTexture("DiffuseMap_3", brick);
     //   matTerrain.setFloat("DiffuseMap_3_scale", rockScale);

        // RIVER ROCK texture, this texture will use the next alphaMap: AlphaMap_1
        Texture riverRock = assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg");
        riverRock.setWrap(Texture.WrapMode.Repeat);
        matTerrain.setTexture("DiffuseMap_4", riverRock);
      //  matTerrain.setFloat("DiffuseMap_4_scale", rockScale);*/

        matTerrain.setFloat("DiffuseMap_0_scale", 1f / (float) (128f / grassScale));
        matTerrain.setFloat("DiffuseMap_1_scale", 1f / (float) (128f / dirtScale));
        matTerrain.setFloat("DiffuseMap_2_scale", 1f / (float) (128f / rockScale));
      //  matTerrain.setFloat("DiffuseMap_3_scale", 1f / (float) (128f / rockScale));
     //   matTerrain.setFloat("DiffuseMap_4_scale", 1f / (float) (128f / rockScale));
        /*
        matTerrain.setFloat("DiffuseMap_0_scale", 1f / (float) (512f / grassScale));
        matTerrain.setFloat("DiffuseMap_1_scale", 1f / (float) (512f / dirtScale));
        matTerrain.setFloat("DiffuseMap_2_scale", 1f / (float) (512f / rockScale));
        matTerrain.setFloat("DiffuseMap_3_scale", 1f / (float) (512f / rockScale));
        matTerrain.setFloat("DiffuseMap_4_scale", 1f / (float) (512f / rockScale));*/
        
        return matTerrain;
    }
}
