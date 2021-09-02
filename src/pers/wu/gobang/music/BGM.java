package pers.wu.gobang.music;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.net.URI;
import java.net.URL;

public class BGM {
    static public BGM bgm;
    File f;
    URI uri;
    URL url;
    AudioClip audioClip;
    boolean isPlaying;

    BGM() {
        try {
            isPlaying = true;
            f = new File("C:\\Users\\WU\\Desktop\\MyFirstProject\\src\\res\\music\\bgm.wav");
            uri = f.toURI();
            url = uri.toURL();
            audioClip = Applet.newAudioClip(url);
            //audioClip.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BGM getInstance() {
        if (bgm == null)
            bgm = new BGM();
        return bgm;
    }

    public void loop() {
        audioClip.loop();
    }

    public void play() {
        audioClip.play();
    }

    public void stop() {
        audioClip.stop();
    }


}