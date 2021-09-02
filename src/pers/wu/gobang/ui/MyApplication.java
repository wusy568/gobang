package pers.wu.gobang.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import pers.wu.gobang.music.BGM;



public class MyApplication extends Application {
    //主方法
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        //创建画板
        Pane pane = new Pane();

        //设置画板颜色
        pane.setBackground(new Background(new BackgroundFill(Color.BURLYWOOD, null, null)));


        //将游戏名添加到画板
        Label gameName = new Label("五子棋");
        gameName.setLayoutX(110);
        gameName.setLayoutY(20);
        gameName.setFont(new Font(60)); //字体大小

        //获取单击版按钮
        Button soloButton = getSoloButton(stage);

        //获取联机版按钮
        Button onlineButton = getOnlineButton(stage);

        //将按钮添加到画板中
        pane.getChildren().addAll(soloButton, onlineButton, gameName);

        //创建场景
        Scene scene = new Scene(pane, 400, 230);

        //将场景设置到初始化舞台上
        stage.setScene(scene);

        //展示舞台
        stage.show();

        //播放背景音乐
        BGM.getInstance().play();

        //设置名字
        stage.setTitle("五子棋");

        //设置应用图标
        stage.getIcons().add(new Image("/res/photo/game.png"));
    }

    //创建联机版按钮
    private Button getOnlineButton(Stage stage) {
        Button onlineButton = new Button("联机版");
        onlineButton.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        onlineButton.setFont(new Font(22)); //字体大小
        onlineButton.setTextFill(Color.WHITE); //字体颜色
        onlineButton.setLayoutX(240);//设置X坐标
        onlineButton.setLayoutY(110);//设置Y坐标
        onlineButton.setPrefSize(120, 90);

        //创建鼠标点击事件,进入联机版
        onlineButton.setOnAction(event -> {
            OnlineStage os = new OnlineStage();
            stage.close();
        });
        return onlineButton;
    }

    //创建单机版按钮
    private Button getSoloButton(Stage stage) {
        Button soloButton = new Button("单机版");
        soloButton.setFont(new Font(22));//设置字体大小
        soloButton.setLayoutX(50);//设置X坐标
        soloButton.setLayoutY(110);//设置Y坐标
        soloButton.setPrefSize(120, 90);

        //创建鼠标点击事件,进入单机版
        soloButton.setOnAction(event -> {
            SoloGame gmStage = new SoloGame();
            stage.close();
        });
        return soloButton;
    }

}
