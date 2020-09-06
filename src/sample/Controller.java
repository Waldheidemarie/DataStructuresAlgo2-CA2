package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.io.*;
import java.util.ArrayList;

public class Controller {

    // FXML Fields
    @FXML
    ImageView mapImage;
    @FXML
    ChoiceBox l1;
    @FXML
    ChoiceBox l2;


    static Image img;
    public static int h;
    public static int w;
    public static WritableImage holderImg;
    private GraphNodeAL[] pathNodes;
    private static ArrayList<GraphNodeAL<MapPoint>> landmarkList = new ArrayList<>();

    public void exitApp() {
        Platform.exit();
    }


    public void initialize(){
        openImage();
        setlandmarks();
        //run();
        //readMapPointsFromCSV();
        for(int i = 0; i < landmarkList.size(); i++){
            if(landmarkList.get(i).data.type == "Landmark") {
                l1.getItems().add(landmarkList.get(i).data.getName());
                l2.getItems().add(landmarkList.get(i).data.getName());
            }
        }
        showAllLandmarks();
    }


    // Adds image to image view
    public void openImage() {
        InputStream f = Controller.class.getResourceAsStream("route.png");
        img = new Image(f, mapImage.getFitWidth(), mapImage.getFitHeight(), false, true);
        h = (int) img.getHeight();
        w = (int) img.getWidth();
        pathNodes = new GraphNodeAL[h * w];
        mapImage.setImage(img);
        //makeGrayscale();
    }


    /////////////////////////////////////////
    // SHORTEST ROUTE: BREATH FIRST SEARCH //
    /////////////////////////////////////////

    //Converts image to greyscale
    public void makeGrayscale() {
        PixelReader pr = img.getPixelReader();
        WritableImage gray = new WritableImage((int) w, (int) h);
        PixelWriter pw = gray.getPixelWriter();
        int count = 0;
        for (int i = 0; i < (int) h; i++) {
            for (int j = 0; j < (int) w; j++) {
                count += 1;
                Color color = pr.getColor(j, i);
                //Reading each pixel and converting to Grayscale
                pw.setColor(j, i, new Color((color.getRed() * 0.3 + color.getGreen() * 0.59 + color.getBlue() * 0.11),
                        (color.getRed() * 0.3 + color.getGreen() * 0.59 + color.getBlue() * 0.11),
                        (color.getRed() * 0.3 + color.getGreen() * 0.59 + color.getBlue() * 0.11),
                        1.0));
                holderImg = gray;
            }
        }
    }

    // Used For BFS, Goes through each pixel and saves the pixels that are white(road or path on the map)
    // Checks the pixel above, left, right, and below and connects them if they are white, creating routes between the pixels.
    public void createNodesOfPixels() {
        makeGrayscale();
        WritableImage pixelPop = copyImage(holderImg);
        PixelReader pr = pixelPop.getPixelReader();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                Color color = pr.getColor(j, i);
                if (color.equals(Color.WHITE)) {
                    pathNodes[(i * w) + j] = new GraphNodeAL(i);
                }
            }
        }

        for (int i = 0; i < pathNodes.length; i++) {
            if (pathNodes[i] != null) {
                if ((i - w) >= 0 && pathNodes[i - w] != null) { // pixel above
                    pathNodes[i].connectToNodeDirected(pathNodes[i - w], 1);
                }
                if ((i + w) < pathNodes.length && pathNodes[i + w] != null) { // pixel below
                    pathNodes[i].connectToNodeDirected(pathNodes[i + w], 1);
                }
                if ((i - 1) >= 0 && pathNodes[i - 1] != null && ((i - 1) % w) != 0) { // pixel left
                    pathNodes[i].connectToNodeDirected(pathNodes[i - 1], 1);
                }
                if ((i + 1) < pathNodes.length && pathNodes[i + 1] != null && ((i + 1) % w) != 0) { // pixel right
                    pathNodes[i].connectToNodeDirected(pathNodes[i + 1], 1);
                }
            }
        }
    }

    public void runBFS(){
        createNodesOfPixels();
        int landmark1 = l1.getSelectionModel().getSelectedIndex();
        int landmark2 = l2.getSelectionModel().getSelectedIndex();
        GraphNodeAL<MapPoint> lm1 = landmarkList.get(landmark1);
        GraphNodeAL<MapPoint> lm2 = landmarkList.get(landmark2);

        BFS(lm1, lm2);
    }

    public void BFS(GraphNodeAL<MapPoint> l1, GraphNodeAL<MapPoint> l2){
        GraphNodeAL.findPathBreadthFirst(l1, l2);
    }



    //////////////////////////////
    // SHORTEST ROUTE: DIJKSTRA //
    //////////////////////////////

    public void shortestRouteDijkstra(){
        int landmark1 = l1.getSelectionModel().getSelectedIndex();
        int landmark2 = l2.getSelectionModel().getSelectedIndex();
        GraphNodeAL<MapPoint> lm1 = landmarkList.get(landmark1);
        GraphNodeAL<MapPoint> lm2 = landmarkList.get(landmark2);

        shortestRouteDij(lm1, lm2);
    }

    public CostedPath shortestRouteDij(GraphNodeAL<MapPoint> l1, GraphNodeAL<MapPoint> l2) {
        CostedPath cpa = GraphNodeAL.findCheapestPathDijkstra(l1, l2.data);
        Utilities.alertBox("Dijkstra's Algorithm", "The cheapest path from " + l1.data.getName() + " to " + l2.data.getName() + "\n" + "Using Dijkstra's algorithm", "Distance: " +cpa.pathCost * 10 + " Meters");
        return cpa;
    }



    ///////////////////////
    // DRAWING LANDMARKS //
    ///////////////////////

    // Draws out the objects onto the pane using other drawing methods
    public void getLandmarksToBeDrawn(){
        ((Pane) mapImage.getParent()).getChildren().removeIf(x->x instanceof Circle || x instanceof Text || x instanceof Line);
        int landmark1 = l1.getSelectionModel().getSelectedIndex();
        int landmark2 = l2.getSelectionModel().getSelectedIndex();
        GraphNodeAL<MapPoint> lm1 = landmarkList.get(landmark1);
        GraphNodeAL<MapPoint> lm2 = landmarkList.get(landmark2);
        CostedPath path=shortestRouteDij(lm1,lm2); 
        GraphNodeAL<MapPoint> prev=null;
        for(GraphNodeAL<?> n : path.pathList) { 
            drawLandmarks((GraphNodeAL<MapPoint>) n);
            if(prev!=null) lineDraw(prev, (GraphNodeAL<MapPoint>) n);
            prev= (GraphNodeAL<MapPoint>) n;
        }
    }

    public void drawLandmarks(GraphNodeAL<MapPoint> l1) {
        if(l1.data.type == "Landmark") {
            double x1 = l1.data.getxCo();
            double y1 = l1.data.getyCo();
            Circle theCircle = new Circle(x1, y1, 8);
            theCircle.setTranslateX(mapImage.getLayoutX());
            theCircle.setTranslateY(mapImage.getLayoutY());
            Text theText = new Text(x1 + 12, y1, l1.data.getName());
            theText.setTranslateX(mapImage.getLayoutX());
            theText.setTranslateY(mapImage.getLayoutY());
            ((Pane) mapImage.getParent()).getChildren().add(theCircle);
            ((Pane) mapImage.getParent()).getChildren().add(theText);
        }
        if (l1.data.type == "Junction"){
            double x1 = l1.data.getxCo();
            double y1 = l1.data.getyCo();
            Circle theCircle = new Circle(x1, y1, 3, Color.BLUEVIOLET);
            theCircle.setTranslateX(mapImage.getLayoutX());
            theCircle.setTranslateY(mapImage.getLayoutY());
            ((Pane) mapImage.getParent()).getChildren().add(theCircle);
        }
    }

    // Used in the getLandmarksToBeDrawn method to draw lines
    public void lineDraw(GraphNodeAL<MapPoint> l1, GraphNodeAL<MapPoint> l2) {
        Line l = new Line((int) l1.data.xCo, (int) l1.data.yCo, (int) l2.data.xCo, (int) l2.data.yCo);
        l.setTranslateX(mapImage.getLayoutX());
        l.setTranslateY(mapImage.getLayoutY());
        ((Pane) mapImage.getParent()).getChildren().add(l);
    }

    // Draws all landmarks out onto the pane
    public void showAllLandmarks(){
        ((Pane) mapImage.getParent()).getChildren().removeIf(x->x instanceof Circle || x instanceof Text || x instanceof Line);
        for(int i = 0; i<landmarkList.size(); i++){
            if(landmarkList.get(i).data.type == "Landmark")
                drawLandmarks(landmarkList.get(i));
        }
    }



    /////////////////
    // COORDINATES //
    /////////////////

    // Gets the distance between two map points
    public int calculateDistance(GraphNodeAL<MapPoint> l1, GraphNodeAL<MapPoint> l2){
        double x1 = l1.data.getxCo();
        double y1 = l1.data.getyCo();
        double x2 = l2.data.getxCo();
        double y2 = l2.data.getyCo();
        double dist = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        return (int) dist;
    }

    // Gets a coordinate on the map by clicking mouse
    public void getCoord(MouseEvent me){
        if(mapImage.getImage()!=null) {
            double xPos = me.getX();
            double yPos = me.getY();
            Main.ps.setTitle(xPos + " " + yPos);
        }
    }



    //////////////////////////
    // SETTING UP LANDMARKS //
    //////////////////////////

    //Attempted to add through CSV file but failed to do so. Attempt can be seen in commented out code.


    /*public void run(){
        ArrayList<GraphNodeAL<MapPoint>> mp = readMapPointsFromCSV("C:\\Users\\seank\\floobits\\share\\conallh1234\\DSA2FINAL\\src\\sample\\landmarks.csv");
        System.out.println(mp);
    }

    public static ArrayList<GraphNodeAL<MapPoint>> readMapPointsFromCSV(String filename){
        Path pathToFile = Paths.get(filename);
        //Path pathToFile = Paths.get("C:\\Users\\Home\\floobits\\share\\conallh1234\\DSA2FINAL\\src\\sample\\landmarks.csv");
        //Path pathToFile = Paths.get("C:\\Users\\seank\\floobits\\share\\conallh1234\\DSA2FINAL\\src\\sample\\landmarks.csv");

        try(BufferedReader br = new BufferedReader(new FileReader("" + pathToFile))){
            String line = br.readLine();

            while(line != null){

                String[] attributes = line.split(",");
                GraphNodeAL<MapPoint> mapPoint = addMapPoint(attributes);
                landmarkList.add(mapPoint);
            }

        } catch (IOException e) {
            System.out.println("File load failed");
        }
        return landmarkList;
    }

    public static GraphNodeAL<MapPoint> addMapPoint(String[] metadata){
        String type = metadata[0];
        String name = metadata[1];
        double xCo = (double) Integer.parseInt(metadata[2]);
        double yCo = (double) Integer.parseInt(metadata[3]);
        GraphNodeAL<MapPoint> mp = new GraphNodeAL<>(new MapPoint(type, name, xCo, yCo));

        return(mp);
    }*/

    public void setlandmarks() {
        // ATTEMPT MADE AT CSV IMPORT CODE

//        for(int i = 0; i < landmarkList.size(); i++){
//            if (landmarkList.get(i).data.name.substring(0, 2) == "La"){
//                String type = landmarkList.get(i).data.type;
//                String name = landmarkList.get(i).data.name;
//                double xCo = landmarkList.get(i).data.xCo;
//                double yCo = landmarkList.get(i).data.yCo;
//                landmarkList.add(new GraphNodeAL<>(new Landmark(type, name, xCo, yCo)));
//            }
//            if(landmarkList.get(i).data.name.substring(0, 2) == "Ju"){
//                String type = landmarkList.get(i).data.type;
//                String name = landmarkList.get(i).data.name;
//                double xCo = landmarkList.get(i).data.xCo;
//                double yCo = landmarkList.get(i).data.yCo;
//                landmarkList.add(new GraphNodeAL<>(new Junction(type, name, xCo, yCo)));
//            }
//        }
        GraphNodeAL<MapPoint> col = new GraphNodeAL<>(new Landmark("Landmark", "Colosseum", 443, 336));
        GraphNodeAL<MapPoint> gio = new GraphNodeAL<>(new Landmark("Landmark","Giovanni", 528, 367));
        GraphNodeAL<MapPoint> piaV = new GraphNodeAL<>(new Landmark("Landmark", "Piazza Venezia", 378, 285));
        GraphNodeAL<MapPoint> mar = new GraphNodeAL<>(new Landmark("Landmark","Maria", 492, 272));
        GraphNodeAL<MapPoint> tre = new GraphNodeAL<>(new Landmark("Landmark","Trevi", 398, 200));
        GraphNodeAL<MapPoint> pietro = new GraphNodeAL<>(new Landmark("Landmark","San Pietro", 192, 240));
        GraphNodeAL<MapPoint> angelo = new GraphNodeAL<>(new Landmark("Landmark","Castel St.Angelo", 260, 216));
        GraphNodeAL<MapPoint> pan = new GraphNodeAL<>(new Landmark("Landmark","Pantheon", 325, 276));

        GraphNodeAL<MapPoint> one = new GraphNodeAL<>(new Junction("Junction","1", 237, 245));
        GraphNodeAL<MapPoint> two = new GraphNodeAL<>(new Junction("Junction","2", 261, 244));
        GraphNodeAL<MapPoint> three = new GraphNodeAL<>(new Junction("Junction","3", 283, 265));
        GraphNodeAL<MapPoint> four = new GraphNodeAL<>(new Junction("Junction","4", 353, 281));
        GraphNodeAL<MapPoint> five = new GraphNodeAL<>(new Junction("Junction","5", 373, 300));
        GraphNodeAL<MapPoint> six = new GraphNodeAL<>(new Junction("Junction","6", 405, 318));
        GraphNodeAL<MapPoint> seven = new GraphNodeAL<>(new Junction("Junction","7", 444, 317));
        GraphNodeAL<MapPoint> eight = new GraphNodeAL<>(new Junction("Junction","8", 439, 298));
        GraphNodeAL<MapPoint> nine = new GraphNodeAL<>(new Junction("Junction","9", 462, 290));
        GraphNodeAL<MapPoint> ten = new GraphNodeAL<>(new Junction("Junction","10", 503, 290));
        GraphNodeAL<MapPoint> eleven = new GraphNodeAL<>(new Junction("Junction","11", 410, 279));
        GraphNodeAL<MapPoint> twelve = new GraphNodeAL<>(new Junction("Junction","12", 474, 267));

        pietro.connectToNodeUndirected(one, calculateDistance(pietro, one));
        one.connectToNodeUndirected(two, calculateDistance(one, two));
        two.connectToNodeUndirected(angelo, calculateDistance(two, angelo));
        two.connectToNodeUndirected(three, calculateDistance(two, three));
        three.connectToNodeUndirected(pan, calculateDistance(three, pan));
        pan.connectToNodeUndirected(four, calculateDistance(pan, four));
        four.connectToNodeUndirected(five, calculateDistance(four, five));
        five.connectToNodeUndirected(piaV, calculateDistance(five, piaV));
        piaV.connectToNodeUndirected(six, calculateDistance(piaV, six));
        piaV.connectToNodeUndirected(eleven, calculateDistance(piaV, eleven));
        six.connectToNodeUndirected(col, calculateDistance(six, col));
        col.connectToNodeUndirected(seven, calculateDistance(col, seven));
        seven.connectToNodeUndirected(eight, calculateDistance(seven, eight));
        eight.connectToNodeUndirected(nine, calculateDistance(eight, nine));
        nine.connectToNodeUndirected(ten, calculateDistance(nine, ten));
        ten.connectToNodeUndirected(gio, calculateDistance(ten, gio));
        ten.connectToNodeUndirected(mar, calculateDistance(ten, mar));
        mar.connectToNodeUndirected(twelve, calculateDistance(mar, twelve));
        twelve.connectToNodeUndirected(eleven, calculateDistance(twelve, eleven));
        twelve.connectToNodeUndirected(tre, calculateDistance(twelve, tre));
        col.connectToNodeUndirected(gio, calculateDistance(col, gio));
        piaV.connectToNodeUndirected(eleven, calculateDistance(piaV, eleven));

        landmarkList.add(col);
        landmarkList.add(gio);
        landmarkList.add(piaV);
        landmarkList.add(mar);
        landmarkList.add(tre);
        landmarkList.add(pietro);
        landmarkList.add(angelo);
        landmarkList.add(pan);
        landmarkList.add(one);
        landmarkList.add(two);
        landmarkList.add(three);
        landmarkList.add(four);
        landmarkList.add(five);
        landmarkList.add(six);
        landmarkList.add(seven);
        landmarkList.add(eight);
        landmarkList.add(nine);
        landmarkList.add(ten);
        landmarkList.add(eleven);
        landmarkList.add(twelve);
    }

    /**
     * copy the given image to a writeable image
     * copied from StackOverFlow
     * @param image
     * @return a writeable image
     */
    public static WritableImage copyImage(Image image) {
        int height=(int)image.getHeight();
        int width=(int)image.getWidth();
        PixelReader pixelReader=image.getPixelReader();
        WritableImage writableImage = new WritableImage(width,height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }
        return writableImage;
    }
}
