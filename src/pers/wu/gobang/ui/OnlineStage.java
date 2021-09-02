package pers.wu.gobang.ui;

import javafx.scene.image.Image;
import pers.wu.gobang.MyGlobal;
import pers.wu.gobang.msg.ChessMessage;
import pers.wu.gobang.msg.Message;
import pers.wu.gobang.msg.News;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("all")
public class OnlineStage extends Stage {
    public OnlineStage(){
        //创建画板
        Pane pane = new Pane();

        //我的Ip地址
        Label myIp = new Label("我 的 IP地 址:");
        myIp.setLayoutX(30);
        myIp.setLayoutY(50);
        //文本框
        TextField myIp_Text = new TextField();
        myIp_Text.setPrefSize(160, 30);
        myIp_Text.setLayoutX(120);
        myIp_Text.setLayoutY(45);

        //我的端口号
        Label myProt = new Label("我 的 端 口 号:");
        myProt.setLayoutX(30);
        myProt.setLayoutY(100);
        //文本框
        TextField myProt_Text = new TextField();
        myProt_Text.setPrefSize(160, 30);
        myProt_Text.setLayoutX(120);
        myProt_Text.setLayoutY(95);

        //我的Ip地址
        Label otherIp = new Label("对方的IP地址 :");
        otherIp.setLayoutX(30);
        otherIp.setLayoutY(150);
        //文本框
        TextField otherIp_Text = new TextField();
        otherIp_Text.setPrefSize(160, 30);
        otherIp_Text.setLayoutX(120);
        otherIp_Text.setLayoutY(145);

        //对方的端口号
        Label otherProt = new Label("对方的端口号 :");
        otherProt.setLayoutX(30);
        otherProt.setLayoutY(200);
        //文本框
        TextField otherProt_Text = new TextField();
        otherProt_Text.setPrefSize(160, 30);
        otherProt_Text.setLayoutX(120);
        otherProt_Text.setLayoutY(195);

        //创建确定按钮对象
        Button confirmButton = new Button("确定");
        confirmButton.setLayoutX(30);//设置X坐标
        confirmButton.setLayoutY(250);//设置Y坐标
        confirmButton.setPrefSize(100, 60);

        //创建鼠标点击事件,进入联机游戏
        confirmButton.setOnAction(new EventHandler<ActionEvent>() {
            class ThreadTest {
                public ThreadTest(Runnable runnable) {
                }
            }

            @Override
            public void handle(ActionEvent event) {
                //获取文本框内容
                MyGlobal.myIp = myIp_Text.getText();
                MyGlobal.myProt = Integer.parseInt(myProt_Text.getText());
                MyGlobal.otherIp = otherIp_Text.getText();
                MyGlobal.otherProt = Integer.parseInt(otherProt_Text.getText());

                //打开联机游戏界面
                OnlineStage.this.close();
                OnlineGame og = new OnlineGame();

                //接收端
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //创建ServerSocket对象
                            ServerSocket ss = new ServerSocket(MyGlobal.myProt);
                            while(true) {
                                //监听连接
                                Socket s = ss.accept();
                                //创建管道流
                                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                                Object obj = ois.readObject();
                                if(obj instanceof ChessMessage) {
                                    ChessMessage chessmsg = (ChessMessage)obj;
                                    og.updateUI(chessmsg);
                                }else if(obj instanceof Message){
                                    Message message = (Message)obj;
                                    if("重开一局".equals(message.getMes())){
                                        og.updateUI2(message,s);
                                    }else{
                                        og.updateUI3(message,s);
                                    }
                                }else if(obj instanceof News){
                                    News news = (News)obj;
                                    og.updateUI1(news);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });


        //创建取消按钮
        Button cancelButton = new Button("取消");
        cancelButton.setLayoutX(170);//设置X坐标
        cancelButton.setLayoutY(250);//设置Y坐标
        cancelButton.setPrefSize(100, 60);
        //创建鼠标点击取消事件
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                OnlineStage.this.close();
            }
        });

        //添加到画板
        pane.getChildren().addAll(myIp, myIp_Text, myProt, myProt_Text,
                otherIp, otherIp_Text, otherProt, otherProt_Text,
                confirmButton, cancelButton);

        //创建场景
        Scene scene = new Scene(pane, 300, 350);

        //将场景设置到初始化舞台上
        this.setScene(scene);

        //设置应用图标
        this.getIcons().add(new Image("/res/photo/game.png"));

        //设置名字
        this.setTitle("登录窗口");

        //展示初始舞台
        this.show();
    }
}