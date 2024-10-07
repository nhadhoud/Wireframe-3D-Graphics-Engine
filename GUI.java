import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.image.Image;
import javafx.scene.control.TextField;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.ArrayList;
import javafx.scene.input.MouseEvent;


public class GUI extends Application {
    private static final int SCREEN_WIDTH = 1200;
    private static final int SCREEN_HEIGHT = 600;
    private float zoomAmount = 250;
    private static final float ZOOM_SENSITIVITY = 0.5f;
    private static final float DRAG_SENSITIVITY = 0.1f;
    private float dragMouseX;
    private float dragMouseY;
    private ArrayList<Shape> defaultShapes = new ArrayList<>(); 
    //large defaultShapes are not allowed perspective projection as obj file vectors are not guaranteed to be normalised
    private ArrayList<Shape> loadedShapes = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        Shape.setDefaultOriginX(SCREEN_WIDTH/2);
        Shape.setDefaultOriginY(SCREEN_HEIGHT/2);
        
        BorderPane root = new BorderPane();
        HBox buttonContainer = new HBox(10);
        Canvas canvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        Button cubeButton = new Button("Cube");
        cubeButton.setOnAction(event -> {addCube();});
        
        Button pyramidButton = new Button("Pyramid");
        pyramidButton.setOnAction(event -> {addPyramid();});
        
        Button sphereButton = new Button("Sphere");
        sphereButton.setOnAction(event -> {addSphere();});
        
        Button torusButton = new Button("Torus");
        torusButton.setOnAction(event -> {addTorus();});
        
        Button cylinderButton = new Button("Cylinder");
        cylinderButton.setOnAction(event -> {addCylinder();});
        
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> {clearShapes();});
        
        TextField fileNameInput = new TextField();
        fileNameInput.setPromptText("Enter filename: ");
        
        Button loadButton = new Button("Load");
        loadButton.setOnAction(event -> {
            String fileName = fileNameInput.getText();
            loadedShapes.add(loadShape(fileName));
        });
        
        buttonContainer.getChildren().addAll(fileNameInput, loadButton, cubeButton, pyramidButton, sphereButton, torusButton, cylinderButton, clearButton);
        buttonContainer.setPadding(new Insets(10, 10, 10, 10));
        
        
        canvas.setOnScroll(event -> {
            zoomAmount += (float) event.getDeltaY() * ZOOM_SENSITIVITY;
        });
        canvas.setOnMouseDragged(event -> {
            dragMouseX = (float) (event.getX());
            dragMouseY = (float) (event.getY());
        });

        
        root.setTop(buttonContainer);
        root.setCenter(canvas);
     
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), event -> {
            clearCanvas(gc);
            renderShapes(gc);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); 
        timeline.play();

        
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        scene.setOnKeyPressed(event -> {
            float translationX = 0;
            float translationY = 0;
            switch (event.getCode()) {
                //movement keys
                case W: translationY = -20; break;
                case S: translationY = 20; break;
                case A: translationX = -20; break;
                case D: translationX = 20; break;
    
            }
            
            for (Shape shape : defaultShapes) {
                shape.setOriginX(shape.getOriginX() + translationX);
                shape.setOriginY(shape.getOriginY() + translationY);
            }
            
            for (Shape shape : loadedShapes) {
                shape.setOriginX(shape.getOriginX() + translationX);
                shape.setOriginY(shape.getOriginY() + translationY);
            }
        });
        
        stage.setTitle("3D Graphics Engine");
        stage.getIcons().add(new Image("file:assets/cube.png"));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void addCube() {
        Shape cube = loadShape("file:assets/cube.obj");
    }
    
    private void addPyramid() {
        Shape pyramid = loadShape("file:assets/pyramid.obj");
    }
    
    private void addSphere() {
        Shape sphere = loadShape("file:assets/sphere.obj");
    }
    
    private void addTorus() {
        Shape torus = loadShape("file:assets/torus.obj");
    }
    
    private void addCylinder() {
        Shape cylinder = loadShape("file:assets/cylinder.obj");
    }
    
    private void clearShapes() {
        if (!defaultShapes.isEmpty()) {defaultShapes.clear();}
        if (!loadedShapes.isEmpty()) {loadedShapes.clear();}
        dragMouseX = 0;
        dragMouseY = 0;
        zoomAmount = 200;
    }
    
    private void clearCanvas(GraphicsContext gc) {
        gc.clearRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }
    
    private Shape loadShape(String fileName) {
        try {
            return ObjFileLoader.loadShape("assets/" + fileName);
        } 
        catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("File Not Found");
            alert.setHeaderText(null);
            alert.setContentText("Error: The file '" + fileName + "' could not be found.");
            alert.showAndWait();
            return null;
        }
    }
    
    private void renderShapes(GraphicsContext gc) {
        for (Shape shape : defaultShapes) {
            shape.undoTransformations();
            shape.scale(100,100,100);
            shape.rotateOnX(dragMouseY);
            shape.rotateOnY(dragMouseX);
            shape.translate(SCREEN_WIDTH/2 - shape.getOriginX(), SCREEN_HEIGHT/2 - shape.getOriginY(), zoomAmount);
            shape.perspective(0.1f, 1000f, 90f, (float) SCREEN_WIDTH/SCREEN_HEIGHT);
            //shape.applyTransformations();
            shape.draw(gc);
        }
            
        for (Shape shape : loadedShapes) {
            shape.undoTransformations();
            //orthographic projection means zooming does not enlarge the shape
            shape.scale(zoomAmount, zoomAmount, zoomAmount);
            shape.rotateOnX(dragMouseY);
            shape.rotateOnY(dragMouseX);
            shape.translate(shape.getOriginX(), shape.getOriginY(), zoomAmount * 2 + 50);
            //loaded defaultShapes orthographicly projected
            shape.orthographic(-300, 300, -300, 300, 0.1f, 1000f);
            shape.draw(gc);
        }
    }
}