import javafx.scene.canvas.GraphicsContext;
import java.util.Arrays;

public class Shape {
    private final float[][] vertices;
    private float[][] transformedVertices;
    private final int[][] edges; // references to connected vertices
    private static float defaultOriginX;
    private static float defaultOriginY;
    private float originX; // position on screen
    private float originY; 
    
    public Shape(float[][] vertices, int[][] edges) {
        this.vertices = vertices;
        this.edges = edges;
        this.originX = defaultOriginX;
        this.originY = defaultOriginY;
        undoTransformations();
    }
    
    //shapes are drawn in a different position to prevent distortion from translation
    public void draw(GraphicsContext gc) {
        for (int i = 0; i < edges.length; i++) {
            float[] startVertex = transformedVertices[edges[i][0]];
            float[] endVertex = transformedVertices[edges[i][1]];
            gc.strokeLine(startVertex[0] + originX, startVertex[1] + originY, endVertex[0] + originX, endVertex[1] + originY);
        }
    }
    
    public void translate(float tx, float ty, float tz) {
        if (tx == 0 && ty == 0 && tz == 0) {
            return;
        }
        
        float[][] transformationMatrix = {
            {1, 0, 0, 0},
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {tx, ty, tz, 1}
        };
        
        transform(transformationMatrix);
    }
    
    public void scale(float sx, float sy, float sz) {
        if (sx == 0 && sy == 0 && sz == 0) {
            return;
        }
        
        float[][] transformationMatrix = {
            {sx, 0, 0, 0},
            {0, sy, 0, 0},
            {0, 0, sz, 0},
            {0, 0, 0, 1}
        };
        
        transform(transformationMatrix);
    }

    public void rotateOnX(float theta) {
        if (theta == 0 || theta % 360 == 0) {
            return;
        }
        
        double thetaRad = Math.toRadians(theta);
        float sinTheta = (float) Math.sin(thetaRad);
        float cosTheta = (float) Math.cos(thetaRad);
        
        float[][] transformationMatrix = {
            {1, 0, 0, 0},
            {0, cosTheta, sinTheta, 0},
            {0, -sinTheta, cosTheta, 0},
            {0, 0, 0, 1}
        };
        
        transform(transformationMatrix);
    }
    
    public void rotateOnY(float theta) {
        if (theta == 0 || theta % 360 == 0) {
            return;
        }
        
        double thetaRad = Math.toRadians(theta);
        float sinTheta = (float) Math.sin(thetaRad);
        float cosTheta = (float) Math.cos(thetaRad);
        
        float[][] transformationMatrix = {
            {cosTheta, 0, -sinTheta, 0},
            {0, 1, 0, 0},
            {sinTheta, 0, cosTheta, 0},
            {0, 0, 0, 1}
        };
        
        transform(transformationMatrix);
    }
    
    public void rotateOnZ(float theta) {
        if (theta == 0 || theta % 360 == 0) {
            return;
        }
        
        double thetaRad = Math.toRadians(theta);
        float sinTheta = (float) Math.sin(thetaRad);
        float cosTheta = (float) Math.cos(thetaRad);
        
        float[][] transformationMatrix = {
            {cosTheta, -sinTheta, 0, 0},
            {sinTheta, cosTheta, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
        };
        
        transform(transformationMatrix);
    }
    
    public void shearOnX(float sy, float sz) {
        if (sy == 0 && sz == 0) {
            return;
        }
        
        float[][] transformationMatrix = {
            {1, sy, sz, 0},
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
        };
        
        transform(transformationMatrix);
    }
    
    public void shearOnY(float sx, float sz) {
        if (sx == 0 && sz == 0) {
            return;
        }
        
        float[][] transformationMatrix = {
            {1, 0, 0, 0},
            {sx, 1, sz, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
        };
        
        transform(transformationMatrix);
    }
    
    public void shearOnZ(float sx, float sy) {
        if (sx == 0 && sy == 0) {
            return;
        }
        
        float[][] transformationMatrix = {
            {1, 0, 0, 0},
            {0, 1, 0, 0},
            {sx, sy, 1, 0},
            {0, 0, 0, 1}
        };
        
        transform(transformationMatrix);
    }
    
    public void perspective(float zNear, float zFar, float fov, float aspectRatio) {
        float perspectiveScale = 1f / (float) Math.tan(Math.toRadians(fov) / 2f);
        float depthScale = zFar / (zFar - zNear);
    
        float[][] transformationMatrix = {
            {perspectiveScale / aspectRatio, 0, 0, 0},
            {0, perspectiveScale, 0, 0},
            {0, 0, depthScale, -depthScale * zNear},
            {0, 0, 1, 0}
        };
        
        transform(transformationMatrix);
        
        //normalise vertices 
        for (int i = 0; i < transformedVertices.length; i++) {
            float w = transformedVertices[i][3];
            if (w != 0) {
                transformedVertices[i][0] /= w;
                transformedVertices[i][1] /= w;
                transformedVertices[i][2] /= w;
            }
        }
    }
    
    public void orthographic(float left, float right, float bottom, float top, float zNear, float zFar) {
        float[][] transformationMatrix = {
            {2 / (right - left), 0, 0, -(right + left) / (right - left)},
            {0, 2 / (top - bottom), 0, -(top + bottom) / (top - bottom)},
            {0, 0, -2 / (zFar - zNear), -(zFar + zNear) / (zFar - zNear)},
            {0, 0, 0, 1}
        };
    
        transform(transformationMatrix);
    }
    
    private void transform(float[][] transformationMatrix) {
        //process matrix multiplication of vertices in parallel
        transformedVertices = Arrays.stream(transformedVertices).parallel().map(vertex -> vectorMatrixMultiplication(vertex, transformationMatrix)).toArray(float[][]::new);
    }
    
    private float[] vectorMatrixMultiplication(float[] vector, float[][] matrix) {
        int vectorCols = vector.length;
        int matrixRows = matrix.length;
        int matrixCols = matrix[0].length;
        float[] result = new float[vectorCols];
        
        for (int i = 0; i < matrixCols; i++) {  // Loop through each column of the matrix
            for (int j = 0; j < vectorCols; j++) {
                result[i] += vector[j] * matrix[j][i];
            }
        }
        
        return result;
    }
    
    public void undoTransformations() {
        transformedVertices = new float[vertices.length][vertices[0].length];
        for (int i = 0; i < vertices.length; i++) {
            transformedVertices[i] = vertices[i].clone();
        }
    }
    
    private void printMatrix(float[][] matrix) {
        System.out.println("");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println("");
        }
    }
    
    public static void setDefaultOriginX(float newDefaultOriginX) {
        defaultOriginX = newDefaultOriginX;
    }
    
    public static void setDefaultOriginY(float newDefaultOriginY) {
        defaultOriginY = newDefaultOriginY;
    }
    
    public void setOriginX(float newOriginX) {
        originX = newOriginX;
    }
    
    public void setOriginY(float newOriginY) {
        originY = newOriginY;
    }
    
    public float getOriginX() {
        return originX;
    }
    
    public float getOriginY() {
        return originY;
    }
}
