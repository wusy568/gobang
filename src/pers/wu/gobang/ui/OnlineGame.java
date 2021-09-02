package pers.wu.gobang.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import pers.wu.gobang.MyGlobal;
import pers.wu.gobang.msg.ChessMessage;
import pers.wu.gobang.msg.Message;
import pers.wu.gobang.msg.News;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pers.wu.gobang.music.BGM;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

//联机游戏

public class OnlineGame extends Stage {
    private int wide = 780; //棋盘宽度
    private int high = 680; //棋盘高度
    private int padding = 40; //棋盘中,线与线之间的距离
    private int lineCount = 15; //棋盘中,水平线和垂直线的个数
    private Pane pane = null;   //定义画板对象,扩大作用域
    private Stage stage = null;//定义舞台对象,扩大作用域
    private int count = 0;  //下棋次数,初始为0
    private ArrayList<Chess> chessmen = new ArrayList<>();  //定义棋子集合
    private ArrayList<Circle> circles = new ArrayList<>();  //定义圆圈对象集合
    private Boolean isWin = false;  //判断是否胜利,初始false
    private Button nextButton = null;   //扩大作用域
    private Button backButton = null;
    private Button dropButton = null;
    private Label laoZi = null;
    private Circle circle1 = null;
    private TextField tx = null;
    private Label str = null;
    private Boolean canplay = true;     //是否能落子
    static Date date = null;
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    static Text t = new Text();

    public OnlineGame(){
        //获取画板
        pane = getChessBoard();

        //设置画板背景照片
        pane.setBackground(
                new Background(
                        Collections.singletonList(new BackgroundFill(
                                Color.WHITE,
                                new CornerRadii(500),
                                new Insets(10))),
                        Collections.singletonList(new BackgroundImage(
                                new Image("/res/photo/background.png", 780, 680, false, true),
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                BackgroundSize.DEFAULT
                        ))
                )
        );

        //添加鼠标点击事件,落子
        play(pane);

        //创建标签文本对象
        Label label = new Label();
        //设置位置
        label.setLayoutX(600);
        label.setLayoutY(280);

        //创建计时器对象
        Timer timer = new Timer();
        //设置计时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //获取当前日期时间
                LocalDateTime localDateTime = LocalDateTime.now();

                //格式化
                DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String time = localDateTime.format(pattern);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //将时间设置到标签文本中,加特效
                        label.setText(time);
                        label.setTextFill(Color.BLACK);
                        label.setFont(Font.font(null, FontWeight.BOLD, 15));
                        Reflection r = new Reflection();
                        r.setFraction(0.7);
                        label.setEffect(r);

                    }
                });
            }
        }, 0, 1000);

        //将标签文本添加到画板上
        pane.getChildren().add(label);

        //创建场景
        Scene scene = new Scene(pane, wide, high);

        //将场景设置到舞台上(继承Stage,this即当前对象)
        this.setScene(scene);

        //给大舞台绑定一个点击退出事件
        stageEvent(this);

        //设置应用图标
        this.getIcons().add(new Image("/res/photo/game.png"));

        //设置名字
        this.setTitle("网络五子棋");

        //展示舞台
        this.show();
    }

    //构建棋盘
    private Pane getChessBoard() {
        //获取画板对象
        pane = new Pane();

        //设置画板背景颜色
        pane.setBackground(new Background(new BackgroundFill(Color.BURLYWOOD, null, null)));

        //绘制棋盘线条
        int increment = 0;
        for(int i = 0; i < lineCount; i++){
            Line rowLine = new Line(20, 20 + increment, wide - 200, 20 + increment);
            Line colLine = new Line(20 + increment, 20, 20 + increment,  wide - 200);

            //将线条放入画板中
            pane.getChildren().addAll(rowLine, colLine);

            increment += padding;
        }
        //创建重来一局按钮
        Button startButton= getStartButton();

        //创建悔棋按钮对象
        Button retractButton = getRetractButton();

        //创建保存棋谱按钮对象
        Button saveButton = getSaveButton();

        //创建退出按钮
        Button quitButton = getQuitButton();

        //获取发送信息按钮
        Button sendButton = getSendButton();

        //获取背景音乐按钮
        Button bgmButton = getBgmButtion();

        //接收消息框
        Rectangle r = new Rectangle(600, 380, 160, 30);
        r.setFill(Color.WHITE);



        //文本区
        tx = new TextField();
        tx.setPrefSize(160, 30);
        tx.setLayoutX(600);
        tx.setLayoutY(450);
        tx.setPromptText("输入聊天内容");
        tx.setAlignment(Pos.CENTER_LEFT);
        tx.setPrefColumnCount(11);

        //添加按钮到棋盘上
        pane.getChildren().addAll(startButton, retractButton, saveButton, quitButton, sendButton, bgmButton,tx, r);

        laoZi = new Label("黑先白后");
        laoZi.setFont(new Font(25));
        laoZi.setTextFill(Color.DARKBLUE);
        laoZi.setLayoutX(625);
        laoZi.setLayoutY(100);
        circle1 = new Circle(680, 180, 15, Color.BLACK);
        pane.getChildren().addAll(laoZi, circle1);

        //设置应用图标
        this.getIcons().add(new Image("/res/photo/game.png"));
        return pane;
    }

    //舞台退出事件
    public void stageEvent(Stage stage) {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                //创建对话框对象
                Alert alert = new Alert(Alert.AlertType.ERROR);

                //设置小图标
                ImageView icon = new ImageView("/res/photo/warn1.png");
                icon.setFitHeight(48);
                icon.setFitWidth(48);
                alert.getDialogPane().setGraphic(icon);

                final Image APPLICATION_ICON = new Image("/res/photo/game.png");
                Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(APPLICATION_ICON);

                //设置标题
                alert.setTitle("警告");
                //设置文本内容
                alert.setContentText("确定退出?");
                //设置头文本
                alert.setHeaderText("退出");


                Optional<ButtonType> optional = alert.showAndWait();
                if(ButtonType.OK == optional.get()) {
                    //退出Java虚拟机
                    System.exit(0);
                }else {
                    //不退出
                    event.consume();//点击取消不进行退出
                }
            }
        });
    }

    //落子声音
    public static void playVoice() {
        try {
            File f = new File("C:\\Users\\WU\\Desktop\\MyFirstProject\\src\\res\\music\\chessVoice.wav");
            URL url = f.toURI().toURL();
            AudioClip audioClip = Applet.newAudioClip(url);
            audioClip.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下棋,落子
    private void play(Pane pane) {

        //鼠标点击事件,点击落子
        pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                //创建对话框对象
                Alert alert = new Alert(Alert.AlertType.ERROR);

                //设置小图标
                ImageView icon = new ImageView("/res/photo/warn.png");
                icon.setFitHeight(48);
                icon.setFitWidth(48);
                alert.getDialogPane().setGraphic(icon);

                final Image APPLICATION_ICON = new Image("/res/photo/game.png");
                Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(APPLICATION_ICON);

                //判断某一方胜利了
                if(isWin) {
                    //设置标题
                    alert.setTitle("警告");
                    //设置文本内容
                    alert.setContentText("不能再继续");
                    //设置头文本

                    //展示对话框
                    alert.show();

                    return;
                }
                if(!canplay)
                    return;


                //定义2个变量接收鼠标单击后的坐标
                double x = 0;
                double y = 0;

                //判断是否超出棋盘边线
                if(!(event.getX() >= 20 && event.getX() <= 600 && event.getY() >= 20 && event.getY() <= 600)){
                    alert.setTitle("警告");
                    alert.setHeaderText("警告!!!!");
                    alert.setContentText("无效区域");
                    alert.show();

                    return;
                }

                //获取实际值坐标
                //判断点击点X坐标,离得最近竖线坐标
                for(int i = 20; i <= wide; i += padding){
                    if(event.getX() - i < padding){
                        if(event.getX() - i > 20){
                            x = i + padding;
                            break;
                        }else {
                            x = i;
                            break;
                        }
                    }
                }

                //判断点击点y坐标,离得最近横线坐标
                for(int i = 20; i <= wide; i += padding){
                    if(event.getY() - i < padding ) {
                        if(event.getY() - i > 20) {
                            y = i + padding;
                            break;
                        }else {
                            y = i;
                            break;
                        }
                    }
                }

                //判断x,y坐标位置是否有棋子
                if(isHad(x,y)){
                    return;
                }

                Chess chessman = null;
                Circle circle = null;

                //判断落子为黑子还是白子
                if(count % 2 == 0){             //下棋次数为偶数,落黑子
                    circle = new Circle(x, y, 15, Color.BLACK);
                    chessman = new Chess(x, y, Color.BLACK);
                }else{
                    circle = new Circle(x, y, 15, Color.WHITE);
                    chessman = new Chess(x, y, Color.WHITE);
                }

                //将棋子对象添加到chessmen集合中
                chessmen.add(chessman);
                //将圆圈对象添加到circles集合中
                circles.add(circle);
                playVoice();

                count++;    //落子,下棋次数加一

                //添加圆圈对象到棋盘上
                pane.getChildren().add(circle);

                canplay = false;

                pane.getChildren().removeAll(laoZi, circle1);
                //获取执棋方提示
                getHolders(pane);

                //发送端
                try {
                    Socket s = new Socket(MyGlobal.otherIp, MyGlobal.otherProt);
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(new ChessMessage(x, y, false));
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //判断是否胜利
                if(Hwin(chessman) || Swin(chessman) || FXwin(chessman) || ZXwin(chessman)){
                    //设置标题
                    alert.setTitle("Win");
                    //设置文本内容
                    alert.setContentText((chessman.getColor().equals(Color.BLACK)? "黑方" : "白方") + "胜利");
                    //设置头文本
                    alert.setHeaderText("胜利");
                    //展示对话框
                    alert.showAndWait();

                    //胜利,isWin为true
                    isWin = true;
                }
            }
        });
    }

    //执棋方提示
    public void getHolders(Pane pane) {
        laoZi = new Label(canplay ? "我方执棋" : "对手执棋");
        laoZi.setFont(new Font(25));
        laoZi.setTextFill(Color.DARKBLUE);
        laoZi.setLayoutX(625);
        laoZi.setLayoutY(100);
        circle1 = new Circle(680, 160, 15, (count % 2 == 0 ? Color.BLACK : Color.WHITE));
        pane.getChildren().addAll(laoZi, circle1);
    }

    //创建悔棋按钮对象
    public Button getRetractButton() {
        Button retractButton = new Button("悔      棋");
        retractButton.setLayoutX(210);//设置x坐标
        retractButton.setLayoutY(620);//设置y坐标
        retractButton.setPrefSize(70, 40);//设置按钮大小

        //添加鼠标单击事件
        retractButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                //创建对话框对象
                Alert alert = new Alert(Alert.AlertType.ERROR);

                //设置小图标
                ImageView icon = new ImageView("/res/photo/warn.png");
                icon.setFitHeight(48);
                icon.setFitWidth(48);
                alert.getDialogPane().setGraphic(icon);

                final Image APPLICATION_ICON = new Image("/res/photo/game.png");
                Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(APPLICATION_ICON);


                //如果胜利或者没有落子则不能悔棋
                if(isWin || count == 0 || canplay) {

                    //设置标题
                    alert.setTitle("警告");
                    //设置文本内容
                    alert.setContentText("不能进行悔棋操作");
                    //设置头文本
                    alert.setHeaderText("警告!!!");
                    alert.show();
                    return;
                }

                //设置标题
                alert.setTitle("提示");
                //设置文本内容
                alert.setContentText("正在进行悔棋操作?");
                //设置头文本
                alert.setHeaderText("悔棋?");
                //设置对话框与舞台在一起
                alert.initOwner(stage);

                Optional<ButtonType> reopen = alert.showAndWait();
                if(ButtonType.OK == reopen.get()) {
                    //发送端
                    Message m = null;
                    try {
                        Socket s = new Socket(MyGlobal.otherIp, MyGlobal.otherProt);
                        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                        oos.writeObject(new Message("悔棋"));

                        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                        m = (Message) ois.readObject();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //如果为true，则同意重开
                    if("拒绝".equals(m.getMes()))
                        return;

                    //退回到上一步,
                    pane.getChildren().remove(circles.get(count - 1));
                    circles.remove(count - 1);
                    chessmen.remove(count - 1);
                    count--;
                    canplay = !canplay;

                    pane.getChildren().removeAll(laoZi, circle1);

                    //获取执棋方提示
                    getHolders(pane);
                }
            }
        });
        return retractButton;
    }

    //创建重开一局按钮对象
    private Button getStartButton() {
        Button startButton = new Button("重开一局");
        startButton.setLayoutX(80);//设置X坐标
        startButton.setLayoutY(620);//设置Y坐标
        startButton.setPrefSize(70, 40);

        //添加鼠标单击事件
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                //创建对话框对象
                Alert alert = new Alert(Alert.AlertType.ERROR);

                //设置小图标
                ImageView icon = new ImageView("/res/photo/warn.png");
                icon.setFitHeight(48);
                icon.setFitWidth(48);
                alert.getDialogPane().setGraphic(icon);

                final Image APPLICATION_ICON = new Image("/res/photo/game.png");
                Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(APPLICATION_ICON);

                //设置标题
                alert.setTitle("警告");
                //设置文本内容
                alert.setContentText("确定重开一局?");
                //设置头文本
                alert.setHeaderText("重新开始");
                //设置对话框与舞台在一起
                alert.initOwner(stage);

                Optional<ButtonType> reopen = alert.showAndWait();
                if(ButtonType.OK == reopen.get()) {
                    //发送端
                    Message m = null;
                    try {
                        Socket s = new Socket(MyGlobal.otherIp, MyGlobal.otherProt);
                        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                        oos.writeObject(new Message("重开一局"));

                        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                        m = (Message) ois.readObject();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //如果为true，则同意重开
                    if("拒绝".equals(m.getMes()))
                        return;

                    //重新初始化成员属性
                    isWin = false;
                    count = 0;
                    chessmen.clear();
                    circles.clear();

                    //清空画板上的圆圈
                    pane.getChildren().removeIf(new Predicate() {
                        @Override
                        public boolean test(Object obj) {
                            return obj instanceof Circle;
                        }
                    });
                }

                pane.getChildren().removeAll(laoZi, circle1);
                laoZi = new Label("黑先白后");
                laoZi.setFont(new Font(25));
                laoZi.setTextFill(Color.DARKBLUE);
                laoZi.setLayoutX(625);
                laoZi.setLayoutY(100);
                circle1 = new Circle(680, 180, 15, Color.BLACK);
                pane.getChildren().addAll(laoZi, circle1);
            }
        });
        return startButton;
    }

    //创建退出按钮对象
    private Button getQuitButton() {
        Button quitButton = new Button("退      出");
        quitButton.setLayoutX(480);//设置x坐标
        quitButton.setLayoutY(620);//设置y坐标
        quitButton.setPrefSize(70, 40);//设置按钮大小

        //创建鼠标点击退出事件
        quitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                //创建对话框对象
                Alert alert = new Alert(Alert.AlertType.ERROR);

                //设置小图标
                ImageView icon = new ImageView("/res/photo/warn1.png");
                icon.setFitHeight(48);
                icon.setFitWidth(48);
                alert.getDialogPane().setGraphic(icon);

                final Image APPLICATION_ICON = new Image("/res/photo/game.png");
                Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(APPLICATION_ICON);

                //设置标题
                alert.setTitle("警告");
                //设置文本内容
                alert.setContentText("确认退出?");
                //设置头文本
                alert.setHeaderText("您正在进行退出操作!!");
                //设置对话框绑定到舞台
                alert.initOwner(stage);

                Optional<ButtonType> optional = alert.showAndWait();
                if(ButtonType.OK == optional.get()) {
                    //退出Java虚拟机
                    System.exit(0);
                }
            }
        });
        return quitButton;
    }

    //创建背景音乐按钮
    public Button getBgmButtion() {
        Button bgmButton = new Button("关闭音乐");
        bgmButton.setLayoutX(620);//设置X坐标
        bgmButton.setLayoutY(620);//设置Y坐标
        bgmButton.setPrefSize(70, 40);//按钮大小

        bgmButton.setOnAction(new EventHandler<ActionEvent>() {


            @Override
            public void handle(ActionEvent event) {

                BGM bgm = BGM.getInstance();
                //开局默认播放
                bgm.play();
                //按键关闭，开启wa
                if (bgmButton.getText().equals("打开音乐")) {
                    bgmButton.setText("关闭音乐");
                    bgm.loop();
                    return;
                }
                bgm.stop();
                bgmButton.setText("打开音乐");
            }

        });
        return bgmButton;
    }

    //创建保存棋盘按钮对象
    public Button getSaveButton() {
        Button saveButton = new Button("保存棋谱");
        saveButton.setLayoutX(340);//设置x坐标
        saveButton.setLayoutY(620);//设置y坐标
        saveButton.setPrefSize(70, 40);//设置按钮大小

        //添加鼠标单击事件
        saveButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if(!isWin) {
                    return;
                }
                //展示保存窗口
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showSaveDialog(stage);

                if(file == null)
                    return;
                //创建高效字符输出流
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(file));
                    for(int i = 0; i < count; i++) {
                        String str = chessmen.get(i).toString();
                        bw.write(str);
                        bw.newLine();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if(bw != null) {
                        try {
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        return saveButton;
    }

    //创建发送信息按钮对象
    public Button getSendButton(){
        Button sendButton = new Button("发送");
        sendButton.setLayoutX(640);//设置x坐标
        sendButton.setLayoutY(500);//设置y坐标
        sendButton.setPrefSize(70, 30);//设置按钮大小

        //192.168.2.153

        //添加鼠标单击事件
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //发送端
                try {
                    String strnews = "不知名对手说:" + tx.getText();
                    Socket s = new Socket(MyGlobal.otherIp, MyGlobal.otherProt);
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(new News(strnews));

                    pane.getChildren().remove(tx);
                    tx = new TextField();
                    tx.setPrefSize(160, 30);
                    tx.setLayoutX(600);
                    tx.setLayoutY(450);

                    tx.setPromptText("输入聊天内容");
                    tx.setAlignment(Pos.CENTER_LEFT);
                    tx.setPrefColumnCount(11);
                    pane.getChildren().add(tx);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return sendButton;
    }

    //判断x,y坐标处是否有棋子
    public boolean isHad(double x, double y) {
        for(int i = 0; i < count; i++) {
            if(chessmen.get(i).getX() == x && chessmen.get(i).getY() == y) {
                return true;
            }
        }
        return false;
    }

    //判断横向是否能胜利
    public boolean Hwin(Chess chess) {
        double reduce = padding;
        int change = 0;
        //向左
        for(int i = 0; i < 4; i++) {
            if(isHave(chess.getX() - reduce, chess.getY(), chess.getColor())) {
                change++;
            }else {
                break;
            }
            reduce += padding;
        }

        int add = padding;
        //向右
        for(int i = 0; i < 4; i++) {
            if(isHave(chess.getX() + add, chess.getY(), chess.getColor())) {
                change++;
            }else {
                break;
            }
            add += padding;
        }
        if(change >= 4) {
            return true;
        }else {
            return false;
        }
    }

    //判读竖向是否能胜利
    public boolean Swin(Chess chess) {
        double reduce = padding;
        int change = 0;
        //向上
        for(int i = 0; i < 4; i++) {
            if(isHave(chess.getX(), chess.getY() - reduce, chess.getColor())) {
                change++;
            }else {
                break;
            }
            reduce += padding;
        }

        int add = padding;
        //向下
        for(int i = 0; i < 4; i++) {
            if(isHave(chess.getX(), chess.getY() + add, chess.getColor())) {
                change++;
            }else {
                break;
            }
            add += padding;
        }
        if(change >= 4) {
            return true;
        }else {
            return false;
        }
    }

    //判断反斜线方向是否能胜利
    public boolean FXwin(Chess chess) {
        double reduce = padding;
        int change = 0;
        //左上
        for(int i = 0; i < 4; i++) {
            if(isHave(chess.getX() - reduce, chess.getY() - reduce, chess.getColor())) {
                change++;
            }else {
                break;
            }
            reduce += padding;
        }

        int add = padding;
        //右下
        for(int i = 0; i < 4; i++) {
            if(isHave(chess.getX() + add, chess.getY() + add, chess.getColor())) {
                change++;
            }else {
                break;
            }
            add += padding;
        }
        if(change >= 4) {
            return true;
        }else {
            return false;
        }
    }

    //判断正斜线方法是否能胜利
    public boolean ZXwin(Chess chess) {
        double reduce = padding;
        int change = 0;
        //左下
        for(int i = 0; i < 4; i++) {
            if(isHave(chess.getX() - reduce, chess.getY() + reduce, chess.getColor())) {
                change++;
            }else {
                break;
            }
            reduce += padding;
        }

        int add = padding;
        //右上
        for(int i = 0; i < 4; i++) {
            if(isHave(chess.getX() + add, chess.getY() - add, chess.getColor())) {
                change++;
            }else {
                break;
            }
            add += padding;
        }
        if(change >= 4) {
            return true;
        }else {
            return false;
        }
    }

    //判断x,y坐标是否有棋子,并且颜色为color
    public boolean isHave(double x, double y, Color color) {
        for(int i = 0; i < count; i++) {
            if(chessmen.get(i).getX() == x && chessmen.get(i).getY() == y && chessmen.get(i).getColor().equals(color)) {
                return true;
            }
        }
        return false;
    }

    //落子接收端
    public void updateUI(ChessMessage chessmsg) {
        canplay = true;
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                //创建对话框对象
                Alert alert = new Alert(Alert.AlertType.ERROR);

                //设置小图标
                ImageView icon = new ImageView("/res/photo/warn.png");
                icon.setFitHeight(48);
                icon.setFitWidth(48);
                alert.getDialogPane().setGraphic(icon);

                final Image APPLICATION_ICON = new Image("/res/photo/game.png");
                Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(APPLICATION_ICON);

                //判断某一方胜利了
                if(isWin){
                    //设置标题
                    alert.setTitle("警告");
                    //设置文本内容
                    alert.setContentText("不能再继续");
                    //设置头文本
                    alert.setHeaderText("胜负已分");
                    //展示对话框
                    alert.show();

                    return;
                }

                double x = chessmsg.getX();
                double y = chessmsg.getY();

                Chess chessman = null;
                Circle circle = null;
                System.out.println("1123");

                //判断落子为黑子还是白子
                if(count % 2 == 0){             //下棋次数为偶数,落黑子
                    circle = new Circle(x, y, 15, Color.BLACK);
                    chessman = new Chess(x, y, Color.BLACK);
                }else{
                    circle = new Circle(x, y, 15, Color.WHITE);
                    chessman = new Chess(x, y, Color.WHITE);
                }

                //将棋子对象添加到chessmen集合中
                chessmen.add(chessman);
                //将圆圈对象添加到circles集合中
                circles.add(circle);

                //下一次棋，播放一次下棋声
                playVoice();

                //落子,下棋次数加一
                count++;

                //添加圆圈对象到棋盘上
                pane.getChildren().add(circle);
                pane.getChildren().removeAll(laoZi, circle1);

                //获取执棋方提示
                getHolders(pane);

                //判断是否胜利
                if(Hwin(chessman) || Swin(chessman) || FXwin(chessman) || ZXwin(chessman)){
                    //设置标题
                    alert.setTitle("Win");
                    //设置文本内容
                    alert.setContentText((chessman.getColor().equals(Color.BLACK)? "黑方" : "白方") + "胜利");
                    //设置头文本
                    alert.setHeaderText("胜利");
                    //展示对话框
                    alert.showAndWait();

                    //胜利,isWin为true
                    isWin = true;
                }
            }
        });
    }

    //重开一局接收端
    public void updateUI2(Message message, Socket socket) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                //创建对话框对象
                Alert alert = new Alert(Alert.AlertType.ERROR);

                //设置小图标
                ImageView icon = new ImageView("/res/photo/warn.png");
                icon.setFitHeight(48);
                icon.setFitWidth(48);
                alert.getDialogPane().setGraphic(icon);

                final Image APPLICATION_ICON = new Image("/res/photo/game.png");
                Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(APPLICATION_ICON);

                //设置标题
                alert.setTitle("请求");
                //设置文本内容
                alert.setContentText("对方希望重开一局?");
                //设置头文本
                alert.setHeaderText("重开一局");
                //设置对话框与舞台在一起
                alert.initOwner(stage);

                Optional<ButtonType> reopen = alert.showAndWait();
                if (ButtonType.OK == reopen.get()) {
                    //回复
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(new Message("重开一局"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //重新初始化成员属性
                    isWin = false;
                    count = 0;
                    chessmen.clear();
                    circles.clear();
                    canplay = true;

                    //清空画板上的圆圈
                    pane.getChildren().removeIf(new Predicate() {
                        @Override
                        public boolean test(Object obj) {
                            return obj instanceof Circle;
                        }
                    });

                    pane.getChildren().removeAll(laoZi, circle1);
                    laoZi = new Label("黑先白后");
                    laoZi.setFont(new Font(25));
                    laoZi.setTextFill(Color.DARKBLUE);
                    laoZi.setLayoutX(625);
                    laoZi.setLayoutY(100);
                    circle1 = new Circle(680, 180, 15, Color.BLACK);
                    pane.getChildren().addAll(laoZi, circle1);
                } else {
                    //回复
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(new Message("拒绝"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //悔棋接收端
    public void updateUI3(Message message, Socket s) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                //创建对话框对象
                Alert alert = new Alert(Alert.AlertType.ERROR);

                //设置小图标
                ImageView icon = new ImageView("/res/photo/warn.png");
                icon.setFitHeight(48);
                icon.setFitWidth(48);
                alert.getDialogPane().setGraphic(icon);

                final Image APPLICATION_ICON = new Image("/res/photo/game.png");
                Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(APPLICATION_ICON);

                //设置标题
                alert.setTitle("提示");
                //设置文本内容
                alert.setContentText("对方希望悔棋");
                //设置头文本
                alert.setHeaderText("悔棋?");
                //设置对话框与舞台在一起
                alert.initOwner(stage);

                Optional<ButtonType> reopen = alert.showAndWait();
                if(ButtonType.OK == reopen.get()) {
                    //回复
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                        oos.writeObject(new Message("同意悔棋"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //退回到上一步,
                    pane.getChildren().remove(circles.get(count - 1));
                    circles.remove(count - 1);
                    chessmen.remove(count - 1);
                    count--;
                    canplay = !canplay;
                    pane.getChildren().removeAll(laoZi, circle1);

                    //获取执棋方提示
                    getHolders(pane);
                }else {
                    //回复
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                        oos.writeObject(new Message("拒绝"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //接收对方的消息
    public void updateUI1(News news) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                pane.getChildren().remove(str);
                str = new Label(news.getNews());
                str.setLayoutX(605);
                str.setLayoutY(385);
                pane.getChildren().add(str);
            }
        });
    }
}