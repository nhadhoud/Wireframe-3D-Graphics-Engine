import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ObjFileLoader {
    public static Shape loadShape(String fileName) throws FileNotFoundException {
        ArrayList<float[]> vertices = new ArrayList<>();
        ArrayList<int[]> edges = new ArrayList<>();

        File myObj = new File(fileName);
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
            String line = myReader.nextLine();
            if (line.startsWith("v ")) {
                String[] parts = line.split(" ");
                float[] vertex = new float[4]; //vertices include a w coordinate for projection
                for (int i = 1; i < 4; i++) { //skip "v"
                    vertex[i - 1] = Float.parseFloat(parts[i]);
                }
                vertex[3] = 1f; // w coordinate
                vertices.add(vertex);
            } 
            else if (line.startsWith("f ")) {
                String[] parts = line.split(" ");
                int[] face = new int[parts.length - 1];
                for (int i = 1; i < parts.length; i++) { //skip "f"
                    String[] vertexReferences = parts[i].split("/");
                    face[i - 1] = Integer.parseInt(vertexReferences[0]) - 1; //make 0 indexed
                }
                faceToEdges(face, edges);
            }
        }
        myReader.close();

        float[][] verticesArray = vertices.toArray(new float[0][]);
        int[][] edgesArray = edges.toArray(new int[0][]);
        return new Shape(verticesArray, edgesArray);
    }

    private static void faceToEdges(int[] face, ArrayList<int[]> edges) {
        for (int i = 0; i < face.length; i++) {
            int startVertexIndex = face[i];
            int endVertexIndex = face[(i + 1) % face.length]; //faces store triangles so connect first to last
            edges.add(new int[]{startVertexIndex, endVertexIndex});
        }
    }
}
