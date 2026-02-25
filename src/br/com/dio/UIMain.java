package br.com.dio;

import br.com.dio.ui.custom.screen.MainScreen;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class UIMain {

    public static void main(String[] args) {
        final Map<String, String> gameConfig = Arrays.stream(args == null ? new String[0] : args)
                .filter(s -> s != null && s.contains(";"))
                .collect(Collectors.toMap(
                        s -> s.split(";")[0],
                        s -> s.split(";")[1]
                ));

        var mainsScreen = new MainScreen(gameConfig);
        mainsScreen.buildMainScreen();
    }

}
