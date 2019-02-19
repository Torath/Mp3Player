package sample;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import javax.imageio.ImageIO;
import java.applet.AudioClip;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Observable;

public class Controller {
    public ToggleButton playpauseButton;
    public Button backButton;
    public ImageView albumCoverImageView;
    public Label songTitleLabel;
    public Button nextButton;
    public Button addToPlaylistButton;
    public Label progressLabel;
    public Slider volumeSlider;
    public Label volumeLabel;
    public Label secondsLabel;
    public Slider progressSlider;
    public ListView<String> musicListView;

    AudioClip clip;
    MediaPlayer mediaPlayer;
    MediaView mediaView;
    File musicFile=null; //current music file
    Duration totalDuration;
    File imageFilePausePlay;//image for play button
    String localUrl;
    Double totalTimeOfMusic;

    //playlist variables
    ArrayList<File> musicFiles;
    ArrayList<String> filenames;
    ObservableList<String> items;



    public void minimize(MouseEvent mouseEvent) {
        ((Stage)((Button)mouseEvent.getSource()).getScene().getWindow()).setIconified(true);
    }

    public void closeApp(MouseEvent mouseEvent) {
        Platform.exit();
    }

    public void playPause(MouseEvent mouseEvent) throws MalformedURLException, InterruptedException {

        if(!playpauseButton.isSelected()){
            if(musicFile!=null) {

                totalTimeOfMusic = mediaPlayer.getTotalDuration().toSeconds();
                progressLabel.setText(""+totalTimeOfMusic.intValue());

                volumeSlider.setValue(40);
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);

                mediaPlayer.play();
                volumeSlider.valueProperty().addListener((Observable) -> {
                    mediaPlayer.setVolume(volumeSlider.getValue() / 100);
                });

                //progress slider

                mediaPlayer.currentTimeProperty().addListener((Observable) -> {

                    if (progressSlider.isPressed()) {
                        mediaPlayer.seek(javafx.util.Duration.seconds((progressSlider.getValue() * (totalTimeOfMusic) / 100)));
                    }
                    if (progressSlider.isValueChanging()) {
                        mediaPlayer.seek(javafx.util.Duration.seconds((progressSlider.getValue() * (totalTimeOfMusic) / 100)));
                    }

                    progressSlider.setValue((mediaPlayer.getCurrentTime().toSeconds() * 100) / totalTimeOfMusic);
                    secondsLabel.setText(String.valueOf(new DecimalFormat("#0.00").format(mediaPlayer.getCurrentTime().toSeconds())) );

                });

                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            }
        }else{
            if(musicFile!=null){
                //localUrl=imageFilePausePlay.toURI().toURL().toString();

                mediaPlayer.pause();
                secondsLabel.setText(String.valueOf(new DecimalFormat("#0.00").format(mediaPlayer.getCurrentTime().toSeconds())) );

            }
        }
    }

    public void browseFile(MouseEvent mouseEvent) {
        try {
            FileChooser fc = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Audio Files (.mp3)","*.mp3");
            fc.getExtensionFilters().add(filter);
            musicFile = fc.showOpenDialog(null);
            if (musicFile != null) {
                if(mediaPlayer==null)playMedia();
                addToPlaylist(musicFile);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initialize(){
        mediaView=new MediaView(mediaPlayer);
        playpauseButton.setSelected(true);


        filenames=new ArrayList<>();
        musicFiles=new ArrayList<>();
        items= FXCollections.observableArrayList(filenames);
        musicListView.setItems(items);


    }



    private void readAndSetMp3Metadata() throws InvalidDataException, IOException, UnsupportedTagException {
        if(musicFile!=null){
            Mp3File mp3File=new Mp3File(musicFile);
            if(mp3File.hasId3v2Tag()){
                ID3v2 tag=mp3File.getId3v2Tag();
                songTitleLabel.setText(tag.getTitle());
                byte[] image=tag.getAlbumImage();
                if(image!=null) {
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

                    albumCoverImageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
                }else{
                    albumCoverImageView.setImage(new Image("images/album_cover.png"));
                }
            }else{
                songTitleLabel.setText(musicFile.getName());
            }
        }
    }

    private void addToPlaylist(File file) throws InvalidDataException, IOException, UnsupportedTagException {
        System.out.println("adding to playlist: "+file.getName());
        String songTitle="";
        Mp3File mp3File=new Mp3File(musicFile);

        if(mp3File.hasId3v2Tag()){
            ID3v2 tag=mp3File.getId3v2Tag();
            songTitle=tag.getTitle();
        }else{
            songTitle=file.getName();
        }

        musicListView.getItems().add(songTitle);
        musicFiles.add(musicFile);

        if(musicListView.getItems().size()==1){
            musicListView.getSelectionModel().select(0);
            musicListView.getFocusModel().focus(0);
        }

    }

    private void playMedia() throws InvalidDataException, IOException, UnsupportedTagException, InterruptedException {
        if (musicFile != null) {
            String path = musicFile.getAbsolutePath();
            path = path.replace("\\", "/");
            Media media = new javafx.scene.media.Media(new File(path)
                    .toURI().toString());

            mediaPlayer = new MediaPlayer(media);
            secondsLabel.setText("0.00");
            readAndSetMp3Metadata();
            totalTimeOfMusic = mediaPlayer.getTotalDuration().toSeconds();
            progressLabel.setText(""+totalTimeOfMusic.intValue());
        }
    }

    public void switchMusic(MouseEvent mouseEvent) throws InvalidDataException, IOException, UnsupportedTagException, InterruptedException {
        if(!musicListView.getItems().isEmpty()) {
            Object item = musicListView.getSelectionModel().getSelectedItem();
        }
        if(!songTitleLabel.getText().equals(musicListView.getSelectionModel().getSelectedItem()) && mediaPlayer!=null){


            if(!mediaPlayer.isMute())mediaPlayer.pause();
            musicFile=musicFiles.get(
                      musicListView.getSelectionModel().getSelectedIndex());
            playMedia();

            playPause(mouseEvent);
        }


    }


    public void playPrev(MouseEvent mouseEvent) throws InterruptedException, UnsupportedTagException, InvalidDataException, IOException {
        skipSongBy(1);
    }

    public void playNext(MouseEvent mouseEvent) throws InterruptedException, UnsupportedTagException, InvalidDataException, IOException {
        skipSongBy(-1);
    }

    private void skipSongBy(int count) throws InterruptedException, UnsupportedTagException, InvalidDataException, IOException {
        if(mediaPlayer!=null){

        if(!mediaPlayer.isMute())mediaPlayer.pause();

        int currentIndex=musicListView.getSelectionModel().getSelectedIndex();
            System.out.println("current index "+currentIndex);
        currentIndex=currentIndex+count;
        if(currentIndex==-1)currentIndex=0;
        if(currentIndex==musicFiles.size())currentIndex--;
            System.out.println("calculated index "+currentIndex);

        musicListView.getSelectionModel().select(currentIndex);
        musicListView.getFocusModel().focus(currentIndex);
        musicFile=musicFiles.get(currentIndex);
        playMedia();
        playPause(null);

    }}

}
