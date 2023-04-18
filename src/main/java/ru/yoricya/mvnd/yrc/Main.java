package ru.yoricya.mvnd.yrc;

public class Main {
    public static void main(String[] args) {
        String script = "final a = \"1\"" +
                "\nb = \"ad\"" +
                "\nprint b";
        YrC y = new YrC();
        boolean a = y.parse(script);
    }
}
