package pers.wu.gobang.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pers.wu.gobang.music.BGM;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

//单机版游戏
@SuppressWarnings("all")
public class SoloGame extends Stage {
    private Date date = null;
    private LocalDateTime localDateTime = LocalDateTime.now();  //获取当前日期时间
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//日期格式
    private Text t = new Text();//定义文本对象
    private int wide = 780; //棋盘宽度
    private int high = 680; //棋盘高度
    private int padding = 40; //棋盘中,线与线之间的距离
    private int lineCount = 15; //棋盘中,水平线和垂直线的个数
    private Pane pane = null;   //定义画板对象,扩大作用域
    private Stage stage = null;//定义舞台对象,扩大作用域
    private int count = 0;  //下棋次数,初始为0
    private ArrayList<Chess> chessmen = new ArrayList<>();  //定义棋子集合
    private ArrayList<Circle> circles = new ArrayList<>();  //定义圆圈对象集合
    private Boolean showing = false;    //是否在打谱
    private Boolean isWin = false;  //判断是否胜利,初始false
    private Button nextButton = null;   //扩大作用域
    private Button backButton = null;
    private Button dropButton = null;
    private String str = null;
    private Label laoZi = null;

    public SoloGame() {
        //获取画板
        pane = getChessBoard();

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

        //展示舞台
        this.show();

        //设置应用图标
        this.getIcons().add(new Image("/res/photo/game.png"));

        //设置名字
        this.setTitle("双人五子棋");


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

    //构建棋盘
    private Pane getChessBoard() {
        //获取画板对象
        pane = new Pane();

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


        //绘制棋盘线条
        int increment = 0;
        for (int i = 0; i < lineCount; i++) {
            Line rowLine = new Line(20, 20 + increment, wide - 200, 20 + increment);
            Line colLine = new Line(20 + increment, 20, 20 + increment, wide - 200);

            //将线条放入画板中
            pane.getChildren().addAll(rowLine, colLine);

            increment += padding;
        }


        //创建重来一局按钮
        Button startButton = getStartButton();

        //创建悔棋按钮对象
        Button retractButton = getRetractButton();

        //创建保存棋谱按钮对象
        Button saveButton = getSaveButton();

        //创建打谱按钮对象
        Button showButton = getShowButton();

        //创建退出按钮
        Button quitButton = getQuitButton();

        //创建音乐按钮
        Button bgmButtoon = getBgmButtion();

        //添加按钮到棋盘上
        pane.getChildren().addAll(startButton, retractButton, saveButton, showButton, quitButton, bgmButtoon);

        getHolders(pane);

        return pane;

    }

    //舞台退出事件
    public void stageEvent(Stage stage) {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                //创建对话框对象
                Alert alert = new Alert(AlertType.CONFIRMATION);
                //设置标题
                alert.setTitle("警告");
                //设置文本内容
                alert.setContentText("你确定要退出吗");
                //设置头文本
                alert.setHeaderText("退出");

                //设置小图标
                ImageView icon = new ImageView("/res/photo/warn1.png");
                icon.setFitHeight(40);
                icon.setFitWidth(40);
                alert.getDialogPane().setGraphic(icon);

                //设置小图标
                final Image APPLICATION_ICON = new Image("/res/photo/game.png");
                Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(APPLICATION_ICON);

                Optional<ButtonType> optional = alert.showAndWait();
                if (ButtonType.OK == optional.get()) {
                    //退出Java虚拟机
                    System.exit(0);
                } else {
                    //不退出
                    event.consume();//点击取消不进行退出
                }
            }
        });
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
                if (isWin) {
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

                //判断是否在打谱
                if (showing) {
                    //设置标题
                    alert.setTitle("警告");
                    //设置文本内容
                    alert.setContentText("正在打打谱,不能進行其他操作");
                    //设置头文本
                    alert.setHeaderText("打谱中");
                    //展示对话框
                    alert.show();

                    return;
                }

                //定义2个变量接收鼠标单击后的坐标
                double x = 0;
                double y = 0;

                //判断是否超出棋盘边线
                if (!(event.getX() >= 20 && event.getX() <= 600 && event.getY() >= 20 && event.getY() <= 600)) {
                    alert.setTitle("警告");
                    alert.setHeaderText("警告!!!!");
                    alert.setContentText("无效区域");
                    alert.show();

                    return;
                }

                //获取实际值坐标
                //判断点击点X坐标,离得最近竖线坐标
                for (int i = 20; i <= wide; i += padding) {
                    if (event.getX() - i < padding) {
                        if (event.getX() - i > 20) {
                            x = i + padding;
                            break;
                        } else {
                            x = i;
                            break;
                        }
                    }
                }

                //判断点击点y坐标,离得最近横线坐标
                for (int i = 20; i <= wide; i += padding) {
                    if (event.getY() - i < padding) {
                        if (event.getY() - i > 20) {
                            y = i + padding;
                            break;
                        } else {
                            y = i;
                            break;
                        }
                    }
                }

                //判断x,y坐标位置是否有棋子
                if (isHad(x, y)) {
                    return;
                }

                Chess chessman = null;
                Circle circle = null;

                //判断落子为黑子还是白子
                if (count % 2 == 0) {             //下棋次数为偶数,落黑子
                    circle = new Circle(x, y, 15, Color.BLACK);
                    chessman = new Chess(x, y, Color.BLACK);
                } else {
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
                pane.getChildren().remove(laoZi);

                //获取执棋方提示
                getHolders(pane);

                //判断是否胜利
                if (Hwin(chessman) || Swin(chessman) || FXwin(chessman) || ZXwin(chessman)) {
                    //设置标题
                    alert.setTitle("Win");
                    //设置文本内容
                    alert.setContentText((chessman.getColor().equals(Color.BLACK) ? "黑方" : "白方") + "胜利");
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
        str = (count % 2 == 0 ? "黑方执棋" : "白方执棋");
        laoZi = new Label(str);
        laoZi.setFont(new Font(25));
        laoZi.setTextFill(Color.DARKBLUE);
        laoZi.setLayoutX(625);
        laoZi.setLayoutY(100);
        pane.getChildren().add(laoZi);
    }

    //创建重开一局按钮对象
    private Button getStartButton() {
        Button startButton = new Button("重开一局");
        startButton.setLayoutX(60);//设置X坐标
        startButton.setLayoutY(600);//设置Y坐标
        startButton.setPrefSize(70, 30);

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

                //设置小图标
                final Image APPLICATION_ICON = new Image("/res/photo/game.png");
                Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(APPLICATION_ICON);

                if (showing) {

                    //设置标题
                    alert.setTitle("警告");
                    //设置文本内容
                    alert.setContentText("不能进行悔棋操作");
                    //设置头文本
                    alert.setHeaderText("打谱中");

                    return;
                }

                //设置标题
                alert.setTitle("警告");
                //设置文本内容
                alert.setContentText("确定重开一局?");
                //设置头文本
                alert.setHeaderText("重新开始");


                Optional<ButtonType> reopen = alert.showAndWait();
                if (ButtonType.OK == reopen.get()) {
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

                pane.getChildren().remove(laoZi);

                //获取执棋方提示
                getHolders(pane);
            }
        });
        return startButton;
    }

    //创建悔棋按钮对象
    public Button getRetractButton() {
        Button retractButton = new Button("悔      棋");
        retractButton.setLayoutX(160);//设置x坐标
        retractButton.setLayoutY(600);//设置y坐标
        retractButton.setPrefSize(70, 30);//设置按钮大小

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
                if (isWin || count == 0 || showing) {

                    //设置标题
                    alert.setTitle("警告");
                    //设置文本内容
                    alert.setContentText("不能进行悔棋操作");
                    //设置头文本
                    alert.setHeaderText("警告!!!");

                    return;
                }
                //退回到上一步,
                pane.getChildren().remove(circles.get(count - 1));
                circles.remove(count - 1);
//                circles[count-1] = null;
//                pane.getChildren().remove(pane.getChildren().size() - 1);
                chessmen.remove(count - 1);
                count--;

                if (!showing) {
                    pane.getChildren().remove(laoZi);

                    //获取执棋方提示
                    getHolders(pane);
                }
            }
        });
        return retractButton;
    }

    //创建退出按钮对象
    private Button getQuitButton() {
        Button quitButton = new Button("退      出");
        quitButton.setLayoutX(460);//设置x坐标
        quitButton.setLayoutY(600);//设置y坐标
        quitButton.setPrefSize(70, 30);//设置按钮大小

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
                alert.setHeaderText("您正在进行退出操作!");


                Optional<ButtonType> optional = alert.showAndWait();
                if (ButtonType.OK == optional.get()) {
                    //退出Java虚拟机
                    System.exit(0);
                }
            }
        });
        return quitButton;
    }

    //创建保存棋盘按钮对象
    public Button getSaveButton() {
        Button saveButton = new Button("保存棋谱");
        saveButton.setLayoutX(260);//设置x坐标
        saveButton.setLayoutY(600);//设置y坐标
        saveButton.setPrefSize(70, 30);//设置按钮大小

        //添加鼠标单击事件
        saveButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (!isWin) {
                    return;
                }
                //展示保存窗口
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showSaveDialog(stage);

                if (file == null)
                    return;
                //创建高效字符输出流
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(file));
                    for (int i = 0; i < count; i++) {
                        String str = chessmen.get(i).toString();
                        bw.write(str);
                        bw.newLine();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bw != null) {
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

    //创建打谱按钮对象
    public Button getShowButton() {
        Button showButton = new Button("打      谱");
        showButton.setLayoutX(360);//设置x坐标
        showButton.setLayoutY(600);//设置y坐标
        showButton.setPrefSize(70, 30);//设置按钮大小

        //添加鼠标单击事件
        showButton.setOnAction(new EventHandler<ActionEvent>() {
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
                alert.setTitle("打谱");
                //设置文本内容
                alert.setContentText("确认打谱?");
                //设置头文本
                alert.setHeaderText("您正在进行打谱操作!!");

                Optional<ButtonType> optional = alert.showAndWait();
                if (ButtonType.OK == optional.get()) {
                    //重新初始化成员属性
                    isWin = false;
                    count = 0;
                    chessmen.clear();
                    circles.clear();
                    pane.getChildren().remove(laoZi);

                    //清空画板上的圆圈
                    pane.getChildren().removeIf(new Predicate() {
                        @Override
                        public boolean test(Object obj) {
                            return obj instanceof Circle;
                        }
                    });

                    //展示打谱窗口
                    FileChooser fileChooser = new FileChooser();
                    File file = fileChooser.showOpenDialog(stage);
                    if (file == null)
                        return;

                    //打谱的時候不能进行別的操作
                    showing = true;

                    //创建集合将读到数据存起来
                    ArrayList<String> al = new ArrayList<>();

                    //创建高效字符输出流将读取到的数据存到集合中
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new FileReader(file));

                        String str = null;
                        while ((str = br.readLine()) != null) {
                            al.add(str);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //创建打谱下一步按钮
                    nextButton = getNextButton(al);

                    //创建打谱上一步按钮
                    backButton = getBackButton();

                    pane.getChildren().add(nextButton);
                    pane.getChildren().add(backButton);

                    //创建打谱退出按钮
                    dropButton = getDropButton();
                    ;
                    pane.getChildren().add(dropButton);
                }
            }
        });
        return showButton;
    }

    //创建打谱下一步按钮
    public Button getNextButton(ArrayList<String> list) {
        Button nextButton = new Button("下一步");
        nextButton.setLayoutX(100);//设置X坐标
        nextButton.setLayoutY(640);//设置Y坐标
        nextButton.setPrefSize(100, 30);

        nextButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String[] str = list.get(count).split(",");
                double x = Double.parseDouble(str[0]);
                double y = Double.parseDouble(str[1]);

                Chess chessman = null;
                Circle circle = null;

                //判断落子为黑子还是白子
                if (count % 2 == 0) {             //下棋次数为偶数,落黑子
                    circle = new Circle(x, y, 15, Color.BLACK);
                    chessman = new Chess(x, y, Color.BLACK);
                } else {
                    circle = new Circle(x, y, 15, Color.WHITE);
                    chessman = new Chess(x, y, Color.WHITE);
                }

                //将棋子对象添加到chessmen集合中
                chessmen.add(chessman);
                //将圆圈对象添加到circles集合中
                circles.add(circle);

                count++;    //落子,下棋次数加一

                //添加圆圈对象到棋盘上
                pane.getChildren().add(circle);

                //判断是否胜利
                if (Hwin(chessman) || Swin(chessman) || FXwin(chessman) || ZXwin(chessman)) {

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
                    alert.setTitle("Win");
                    //设置文本内容
                    alert.setContentText((chessman.getColor().equals(Color.BLACK) ? "黑方" : "白方") + "胜利");
                    //设置头文本
                    alert.setHeaderText("胜利");

                    //展示对话框
                    alert.showAndWait();
                }
            }
        });
        return nextButton;
    }

    //创建打谱上一步按钮
    public Button getBackButton() {
        Button backButton = new Button("上一步");
        backButton.setLayoutX(250);//设置X坐标
        backButton.setLayoutY(640);//设置Y坐标
        backButton.setPrefSize(100, 30);
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                //如果胜利或者没有落子则不能悔棋
                if (isWin || count == 0) {

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
                    alert.setContentText("不能进行悔棋操作");
                    //设置头文本
                    alert.setHeaderText("警告!!!");

                    alert.show();
                    return;
                }
                pane.getChildren().remove(circles.get(count - 1));
                circles.remove(count - 1);
                chessmen.remove(count - 1);
                count--;
            }
        });
        return backButton;
    }

    //创建退出按钮
    public Button getDropButton() {
        Button dropButton = new Button("退出打谱");
        dropButton.setLayoutX(400);//设置X坐标
        dropButton.setLayoutY(640);//设置Y坐标
        dropButton.setPrefSize(100, 30);
        //创建鼠标点击退出事件
        dropButton.setOnAction(new EventHandler<ActionEvent>() {
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
                alert.setContentText("确认退出打谱?");
                //设置头文本
                alert.setHeaderText("您正在进行退出打谱操作!!");
                //设置对话框与舞台在一起

                Optional<ButtonType> optional = alert.showAndWait();
                if (ButtonType.OK == optional.get()) {
                    //重新初始化成员属性
                    isWin = false;
                    count = 0;
                    chessmen.clear();
                    circles.clear();

                    //获取执棋方提示
                    getHolders(pane);

                    //清空画板上的圆圈
                    pane.getChildren().removeIf(new Predicate() {
                        @Override
                        public boolean test(Object obj) {
                            return obj instanceof Circle;
                        }
                    });

                    //退出打谱,showing改为false
                    showing = false;

                    //删除三个按钮
                    pane.getChildren().remove(nextButton);
                    pane.getChildren().remove(backButton);
                    pane.getChildren().remove(dropButton);
                }

            }
        });
        return dropButton;
    }

    //创建背景音乐按钮
    public Button getBgmButtion() {
        Button bgmButton = new Button("关闭音乐");
        bgmButton.setLayoutX(600);//设置X坐标
        bgmButton.setLayoutY(600);//设置Y坐标
        bgmButton.setPrefSize(80, 30);

        bgmButton.setOnAction(new EventHandler<ActionEvent>() {


            @Override
            public void handle(ActionEvent event) {

                BGM bgm = BGM.getInstance();

                bgm.play();

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

    //判断x,y坐标处是否有棋子
    public boolean isHad(double x, double y) {
        for (int i = 0; i < count; i++) {
            if (chessmen.get(i).getX() == x && chessmen.get(i).getY() == y) {
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
        for (int i = 0; i < 4; i++) {
            if (isHave(chess.getX() - reduce, chess.getY(), chess.getColor())) {
                change++;
            } else {
                break;
            }
            reduce += padding;
        }

        int add = padding;
        //向右
        for (int i = 0; i < 4; i++) {
            if (isHave(chess.getX() + add, chess.getY(), chess.getColor())) {
                change++;
            } else {
                break;
            }
            add += padding;
        }
        if (change >= 4) {
            return true;
        } else {
            return false;
        }
    }

    //判读竖向是否能胜利
    public boolean Swin(Chess chess) {
        double reduce = padding;
        int change = 0;
        //向上
        for (int i = 0; i < 4; i++) {
            if (isHave(chess.getX(), chess.getY() - reduce, chess.getColor())) {
                change++;
            } else {
                break;
            }
            reduce += padding;
        }

        int add = padding;
        //向下
        for (int i = 0; i < 4; i++) {
            if (isHave(chess.getX(), chess.getY() + add, chess.getColor())) {
                change++;
            } else {
                break;
            }
            add += padding;
        }
        if (change >= 4) {
            return true;
        } else {
            return false;
        }
    }

    //判断反斜线方向是否能胜利
    public boolean FXwin(Chess chess) {
        double reduce = padding;
        int change = 0;
        //左上
        for (int i = 0; i < 4; i++) {
            if (isHave(chess.getX() - reduce, chess.getY() - reduce, chess.getColor())) {
                change++;
            } else {
                break;
            }
            reduce += padding;
        }

        int add = padding;
        //右下
        for (int i = 0; i < 4; i++) {
            if (isHave(chess.getX() + add, chess.getY() + add, chess.getColor())) {
                change++;
            } else {
                break;
            }
            add += padding;
        }
        if (change >= 4) {
            return true;
        } else {
            return false;
        }
    }

    //判断正斜线方法是否能胜利
    public boolean ZXwin(Chess chess) {
        double reduce = padding;
        int change = 0;
        //左下
        for (int i = 0; i < 4; i++) {
            if (isHave(chess.getX() - reduce, chess.getY() + reduce, chess.getColor())) {
                change++;
            } else {
                break;
            }
            reduce += padding;
        }

        int add = padding;
        //右上
        for (int i = 0; i < 4; i++) {
            if (isHave(chess.getX() + add, chess.getY() - add, chess.getColor())) {
                change++;
            } else {
                break;
            }
            add += padding;
        }
        if (change >= 4) {
            return true;
        } else {
            return false;
        }
    }

    //判断x,y坐标是否有棋子,并且颜色为color
    public boolean isHave(double x, double y, Color color) {
        for (int i = 0; i < count; i++) {
            if (chessmen.get(i).getX() == x && chessmen.get(i).getY() == y && chessmen.get(i).getColor().equals(color)) {
                return true;
            }
        }
        return false;
    }
}