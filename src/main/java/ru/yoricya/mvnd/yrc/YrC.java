package ru.yoricya.mvnd.yrc;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class YrC {
    public String[] scrm;
    public String scr = "";
    public int read = 0;
    public String[] args;
    public HashMap<String, Object> GLOB = new HashMap<>();
    public HashMap<String, Object> GLOBCONSTR = new HashMap<>();
    public YrC(){
        GLOB.put("END", "");
        GLOB.put("YrC.ver", "1.1/Beta");
        GLOB.put("YrC.codeVer", "002");
        GLOB.put("print", new OnFunction() {
            @Override
            public void onFunc(String[] argsg) {
                String prs = ParseText(Arrays.toString(argsg).replace(",", ""));
                argsg[1] = argsg[1].replace(" ", "");
                if(prs == null){
                    if(GLOB.get(argsg[1]) != null){
                        prs = GLOB.get(argsg[1]).toString();
                    }
                    else{
                        System.err.println("Ошибка! Print Error! '" + argsg[1] + ":" + read + "' - Значение не установленно!");
                    }
                }
                System.out.println(prs);
            }
        });
        addFunc("if", new OnFunction() {
            @Override
            public void onFunc(String[] argss) {
                if(argss.length >= 5){
                    if(argss[2].equals("==")){
                        if(ParseText(argss[1]).equals(ParseText(argss[3]))) IFFunc(argss);
                    }else if(argss[2].equals("!=")){
                        if(!ParseText(argss[1]).equals(ParseText(argss[3]))) IFFunc(argss);
                    }else if(argss[2].equals(">")){
                        if(Long.parseLong(ParseText(argss[1])) > Long.parseLong(ParseText(argss[3]))) IFFunc(argss);
                    }else if(argss[2].equals("<")){
                        if(Long.parseLong(ParseText(argss[1])) < Long.parseLong(ParseText(argss[3]))) IFFunc(argss);
                    }else if(argss[2].equals(">=")){
                        if(Long.parseLong(ParseText(argss[1])) >= Long.parseLong(ParseText(argss[3]))) IFFunc(argss);
                    }else if(argss[2].equals("<=")){
                        if(Long.parseLong(ParseText(argss[1])) <= Long.parseLong(ParseText(argss[3]))) IFFunc(argss);
                    }else{
                        printErr("Не известный оператор!");
                    }
                }else{
                    printErr("Не все аргументы указаны!");
                }
            }
        });
        GLOB.put("new", new OnFunction() {
            @Override
            public void onFunc(String[] args) {
                if((OnConstructor) GLOBCONSTR.get(args[1])!=null){
                    GLOB.put("END."+args[2], "");
                    OnConstructor f= (OnConstructor)GLOBCONSTR.get(args[1]);
                    read+=1;
                    StringBuilder scrp = new StringBuilder();
                    while (!scrm[read].equals("END."+args[2])) {
                        scrp.append(scrm[read]).append("\n");
                        read+=1;
                    }
                    String finalScrp = scrp.toString();
                    f.onConstr(args[2],finalScrp);
                }else{
                    System.err.println("Ошибка! '"+args[0]+ ":"+read+"' - Такой конструкции не существует!");
                }
            }
        });
        GLOB.put("net_get_contents", new OnFunction() {
            @Override
            public void onFunc(String[] args) {
                if(args.length > 2) {
                    GLOB.put(args[2],NetGetContents(ParseText(args[1])));
                }else{
                    printErr("Ошибка");
                }
            }
        });
        addFunc("strCat", new OnFunction() {
            @Override
            public void onFunc(String[] str) {
                if(str.length >= 5){
                    String a = ParseText(str[1]) + ParseText(str[3]);
                    setVar(str[5], a);
                }else printErr("Не все аргументы указаны!");
            }
        });
        addFunc("delete", new OnFunction() {
            @Override
            public void onFunc(String[] str) {
                if(str.length == 2){
                    GLOB.remove(str[1]);
                }else printErr("Синтаксическая ошибка");
            }
        });
        GLOBCONSTR.put("Function", new OnConstructor() {
            @Override
            public void onConstr(String var, String scr) {
                GLOB.put(var, new OnFunction() {
                    @Override
                    public void onFunc(String[] args) {
                        YrC y = new YrC();
                        y.GLOBCONSTR = GLOBCONSTR;
                        y.GLOB = GLOB;
                        for(int i = 1; i != args.length ; i++){
                            y.setVar(var+".args."+(i-1), y.ParseText(args[i]));
                        }
                        y.setVar(var+".args.length", String.valueOf(args.length - 1));
                        y.parse(scr);
                        GLOB = y.GLOB;
                        GLOBCONSTR = y.GLOBCONSTR;
                    }
                });
            }
        });
    }
    private void IFFunc(String[] argss){
        System.out.println(Arrays.toString(argss));
        if(argss[4].equals("cast")){
            OnFunction rn = (OnFunction) GLOB.get(argss[5]);
            StringBuilder newArgs = new StringBuilder();
            for(int i = 6; i != argss.length; i+=1){
                newArgs.append(" ").append(argss[i]);
            }
            rn.onFunc(newArgs.toString().split(" "));
        }else printErr("Ожидается тип: cast");
    }

    private YrC getThis(){
        return this;
    }
    public interface OnFunction {
        void onFunc(String[] args);
    }
    public interface OnConstructor {
        void onConstr(String var, String cmds);
    }
    public String NetGetContents(String url) {
        try {
            URL urlObject = new URL(url);
            URLConnection conn = urlObject.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine, output = "";
            while ((inputLine = in.readLine()) != null) {
                output += inputLine;
            }
            in.close();
            return output;
        }catch (Exception e){
            return null;
        }
    }

    public boolean parse(String scro){
        scr = scro;
        try{
            scrm = scr.split("\n");
            while (read != scrm.length) {
                args = scrm[read].split(" ");
                args[0] = args[0].replace("-", "");
                if(args[0].equals("")) {

                }else
                if(args[0].equals("wait")) {
                    TimeUnit.MILLISECONDS.sleep(Integer.parseInt(ParseText(args[1])));
                }
                else
                if(GLOB.get(args[0]) != null){
                    try{
                        OnFunction rn = (OnFunction) GLOB.get(args[0]);
                        rn.onFunc(args);
                    }catch (Exception e){
                        if(args.length == 1) {
                            System.out.println(GLOB.get(args[0]));
                        }
                    }
                }else if(args.length == 1){
                    System.err.println("Ошибка! '"+args[0]+ ":"+read+"' - Переменная/Функция не инициализирована!");
                }
                if(args.length > 1 && args[1].equals("=")){
                    String prs = ParseText(Arrays.toString(args).replace(",", ""));
                    if(prs == null) {
                        System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                        continue;
                    }
                    GLOB.put(args[0], prs);
                } else if(args.length > 2 && args[1].equals("+=")){
                    String prs = ParseText(Arrays.toString(args).replace(",", ""));
                    if(prs == null) {
                        System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                        continue;
                    }
                    int a = Integer.parseInt((String) GLOB.get(args[0]));
                    a += Integer.parseInt(ParseText(args[2]));
                    GLOB.put(args[0], a);
                } else if(args.length > 2 && args[1].equals("-=")) {
                    String prs = ParseText(Arrays.toString(args).replace(",", ""));
                    if (prs == null) {
                        System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                        continue;
                    }
                    int a = Integer.parseInt((String) GLOB.get(args[0]));
                    a -= Integer.parseInt(ParseText(args[2]));
                    GLOB.put(args[0], a);
                } else if(args.length > 2 && args[1].equals("*=")) {
                    String prs = ParseText(Arrays.toString(args).replace(",", ""));
                    if (prs == null) {
                        System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                        continue;
                    }
                    //System.out.println(args[0]);
                    int a = Integer.parseInt((String) GLOB.get(args[0]));
                    a *= Integer.parseInt(ParseText(args[2]));
                    GLOB.put(args[0], a);
                } else if(args.length > 2 && args[1].equals("/=")) {
                    String prs = ParseText(Arrays.toString(args).replace(",", ""));
                    if (prs == null) {
                        System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                        continue;
                    }
                    int a = Integer.parseInt((String) GLOB.get(args[0]));
                    a /= Integer.parseInt(ParseText(args[2]));
                    GLOB.put(args[0], a);
                }
                read +=1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    public boolean parseOnly(String scrm){

        try{
            args = scrm.split(" ");
            args[0] = args[0].replace("-", "");
            if(args[0].equals("")) {

            }else
            if(args[0].equals("wait")) {
                TimeUnit.MILLISECONDS.sleep(Integer.parseInt(ParseText(args[1])));
            }
            else
            if(GLOB.get(args[0]) != null){
                try{
                    OnFunction rn = (OnFunction) GLOB.get(args[0]);
                    rn.onFunc(args);
                }catch (Exception e){
                    if(args.length == 1) {
                        System.out.println(GLOB.get(args[0]));
                    }
                }
            }else if(args.length == 1){
                System.err.println("Ошибка! '"+args[0]+ ":"+read+"' - Переменная/Функция не инициализирована!");
            }
            if(args.length > 1 && args[1].equals("=")){
                String prs = ParseText(Arrays.toString(args).replace(",", ""));
                if(prs == null) {
                    System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                    return false;
                }
                GLOB.put(args[0], prs);
            } else if(args.length > 2 && args[1].equals("+=")){
                String prs = ParseText(Arrays.toString(args).replace(",", ""));
                if(prs == null) {
                    System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                    return false;
                }
                int a = Integer.parseInt((String) GLOB.get(args[0]));
                a += Integer.parseInt(ParseText(args[2]));
                GLOB.put(args[0], a);
            } else if(args.length > 2 && args[1].equals("-=")) {
                String prs = ParseText(Arrays.toString(args).replace(",", ""));
                if (prs == null) {
                    System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                    return false;
                }
                int a = Integer.parseInt((String) GLOB.get(args[0]));
                a -= Integer.parseInt(ParseText(args[2]));
                GLOB.put(args[0], a);
            } else if(args.length > 2 && args[1].equals("*=")) {
                String prs = ParseText(Arrays.toString(args).replace(",", ""));
                if (prs == null) {
                    System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                    return false;
                }
                //System.out.println(args[0]);
                int a = Integer.parseInt((String) GLOB.get(args[0]));
                a *= Integer.parseInt(ParseText(args[2]));
                GLOB.put(args[0], a);
            } else if(args.length > 2 && args[1].equals("/=")) {
                String prs = ParseText(Arrays.toString(args).replace(",", ""));
                if (prs == null) {
                    System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                    return false;
                }
                int a = Integer.parseInt((String) GLOB.get(args[0]));
                a /= Integer.parseInt(ParseText(args[2]));
                GLOB.put(args[0], a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    public void printErr(String err){
        System.err.println("Ошибка! '" + args[1] + ":" + read + "' - "+err);
    }
    public void addFunc(String name , OnFunction func){
        GLOB.put(name, func);
    }
    public void addConstructor(String name, OnConstructor c){
        GLOBCONSTR.put(name, c);
    }
    public void setVar(String name , String run){
        GLOB.put(name, run);
    }
    public void addConstrVar(String var, String newvar, OnFunction f){
        try {
            if ((OnFunction) GLOB.get(var) != null) {
                GLOB.put(var + "." + newvar, f);
                //printErr(var + "." + newvar);
            } else {
                printErr("Неправильно сконфигурирован конструктор!");
            }
        }catch (Exception e){e.printStackTrace();}
    }
    public String ParseText(String str){
        String[] strd = str.split("\"");
        if(strd.length < 2) {
            str = (String) GLOB.get(str);
            if(str == null) return null;
        }else {
            str = strd[1];
        }
        str = str.replace("/SP", " ");
        str = str.replace("/RE", "\n");
        return str;
    }

}
