package co.makestar.sbvmaker;

import co.makestar.sbvmaker.view.RootView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Runner extends AbstractJavaFxApplicationSupport {
  public static String newLine = System.getProperty("line.separator");

  public static void main(String[] args) {
    launch(Runner.class, RootView.class, args);
  }
}

