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
    public String AppName;
    public String scr = "";
    public int read = 0;
    public String[] args;
    public HashMap<String, Object> GLOB = new HashMap<>();
    public HashMap<String, Object> GLOBCONSTR = new HashMap<>();
    public YrC(YrC yrc){
        GLOB = yrc.GLOB;
        AppName = yrc.AppName;
        GLOBCONSTR = yrc.GLOBCONSTR;
    }
    public YrC(String appname){
        AppName = appname;
        GLOB.put("END", "");
        GLOB.put("YrC.ver", "1.3.2/Beta");
        GLOB.put("YrC.codeVer", "005");
        GLOB.put("YrC.AppName", AppName);
        GLOB.put("print", new OnFunction() {
            @Override
            public boolean onFunc(String[] argsg, YrC yrc) {
                String prs = yrc.ParseText(argsg[1]);
                if(prs == null){
                    if(yrc.GLOB.get(argsg[1]) != null){
                        prs = yrc.GLOB.get(argsg[1]).toString();
                    }
                    else{
                        System.err.println("Ошибка! Print Error! '" + argsg[1] + ":" + yrc.read + "' - Значение не установленно!");
                    }
                }
                System.out.println(prs);
                return true;
            }
        });
        addFunc("if", new OnFunction() {
            @Override
            public boolean onFunc(String[] argss, YrC yrc) {
                if(argss.length >= 5){
                    boolean ret = true;
                    if(argss[2].equals("==")){
                        if(yrc.ParseText(argss[1]).equals(yrc.ParseText(argss[3]))) ret = yrc.IFFunc(argss);
                    }else if(argss[2].equals("!=")){
                        if(!yrc.ParseText(argss[1]).equals(yrc.ParseText(argss[3]))) ret = yrc.IFFunc(argss);
                    }else if(argss[2].equals(">")){
                        if(Long.parseLong(yrc.ParseText(argss[1])) > Long.parseLong(yrc.ParseText(argss[3]))) ret = yrc.IFFunc(argss);
                    }else if(argss[2].equals("<")){
                        if(Long.parseLong(yrc.ParseText(argss[1])) < Long.parseLong(yrc.ParseText(argss[3]))) ret = yrc.IFFunc(argss);
                    }else if(argss[2].equals(">=")){
                        if(Long.parseLong(yrc.ParseText(argss[1])) >= Long.parseLong(yrc.ParseText(argss[3]))) ret = yrc.IFFunc(argss);
                    }else if(argss[2].equals("<=")){
                        if(Long.parseLong(yrc.ParseText(argss[1])) <= Long.parseLong(yrc.ParseText(argss[3]))) ret = yrc.IFFunc(argss);
                    }else{
                        yrc.printErr("Не известный оператор!");
                    }
                    return ret;
                }else{
                    yrc.printErr("Не все аргументы указаны!");
                }
                return true;
            }
        });
        addFunc("ifset", new OnFunction() {
            @Override
            public boolean onFunc(String[] argss, YrC yrc) {
                if(argss.length >= 5){
                    boolean isset = yrc.ParseText(argss[1]) != null;
                    boolean ret = true;
                    if(argss[2].equals("==")){
                        if(isset == Boolean.parseBoolean(yrc.ParseText(argss[3]))) ret = yrc.IFFunc(argss);
                    }else if(argss[2].equals("!=")) {
                        if(isset != Boolean.parseBoolean(yrc.ParseText(argss[3]))) ret = yrc.IFFunc(argss);
                    }else{
                        yrc.printErr("Не известный оператор!");
                    }
                    return ret;
                }else{
                    yrc.printErr("Не все аргументы указаны!");
                }
                return true;
            }
        });
        GLOB.put("new", new OnFunction() {
            @Override
            public boolean onFunc(String[] args, YrC yrc) {
                if((OnConstructor) yrc.GLOBCONSTR.get(args[1])!=null){
                    yrc.GLOB.put("END."+args[2], "");
                    OnConstructor f= (OnConstructor) yrc.GLOBCONSTR.get(args[1]);
                    yrc.read+=1;
                    StringBuilder scrp = new StringBuilder();
                    while (!yrc.scrm[read].equals("END."+args[2])) {
                        scrp.append(yrc.scrm[read]).append("\n");
                        yrc.read+=1;
                    }
                    String finalScrp = scrp.toString();
                    f.onConstr(args[2],finalScrp);
                }else{
                    System.err.println("Ошибка! '"+args[0]+ ":"+yrc.read+"' - Такой конструкции не существует!");
                }
                return true;
            }
        });
        GLOB.put("net_get_contents", new OnFunction() {
            @Override
            public boolean onFunc(String[] args, YrC yrc) {
                if(args.length > 2) {
                    yrc.GLOB.put(args[2], yrc.NetGetContents(yrc.ParseText(args[1])));
                }else{
                    yrc.printErr("Ошибка");
                }
                return true;
            }
        });
        addFunc("strCat", new OnFunction() {
            @Override
            public boolean onFunc(String[] str, YrC yrc) {
                if(str.length >= 5){
                    String a = yrc.ParseText(str[1]) + yrc.ParseText(str[3]);
                    yrc.setVar(str[5], a);
                }else yrc.printErr("Не все аргументы указаны!");
                return true;
            }
        });
        addFunc("delete", new OnFunction() {
            @Override
            public boolean onFunc(String[] str, YrC yrc) {
                if(str.length == 2){
                    yrc.GLOB.remove(str[1]);
                }else yrc.printErr("Синтаксическая ошибка");
                return true;
            }
        });
        addFunc("return", new OnFunction() {
            @Override
            public boolean onFunc(String[] str, YrC yrc) {
                try {
                    if (str.length == 2) {
                        return Boolean.parseBoolean(yrc.ParseText(str[1]));
                    } else {
                        return false;
                    }
                }catch(Exception ignore){
                    yrc.printErr("");
                    return false;
                }
            }
        });
        addFunc("exit", new OnFunction() {
            @Override
            public boolean onFunc(String[] str, YrC yrc) {
                try {
                    if (str.length == 2) {
                        System.exit(Integer.parseInt(yrc.ParseText(str[1])));
                    } else {
                        System.exit(0);
                    }
                    return true;
                }
                catch (Exception e){
                    yrc.printErr("");
                    return false;
                }
            }
        });
        addFunc("var", new OnFunction() {
            @Override
            public boolean onFunc(String[] str, YrC yrc) {
                try {
                    if (str.length < 4) {
                        yrc.printErr("Не все аргументы указаны");
                    } else if (str[2].equals("=")) {
                        yrc.GLOB.put(str[1], yrc.ParseText(str[3]));
                    } else if (str[2].equals("+=")) {
                        int a = Integer.parseInt(yrc.ParseText(str[1]));
                        a+= Integer.parseInt(yrc.ParseText(str[3]));
                        yrc.setVar(str[1], String.valueOf(a));
                    } else if (str[2].equals("-=")) {
                        int a = Integer.parseInt(yrc.ParseText(str[1]));
                        a-= Integer.parseInt(yrc.ParseText(str[3]));
                        yrc.setVar(str[1], String.valueOf(a));
                    } else if (str[2].equals("/=")) {
                        int a = Integer.parseInt(yrc.ParseText(str[1]));
                        a/= Integer.parseInt(yrc.ParseText(str[3]));
                        yrc.setVar(str[1], String.valueOf(a));
                    } else if (str[2].equals("*=")) {
                        int a = Integer.parseInt(yrc.ParseText(str[1]));
                        a*= Integer.parseInt(yrc.ParseText(str[3]));
                        yrc.setVar(str[1], String.valueOf(a));
                    }else{
                        yrc.printErr("Неизвестный тип операции.");
                    }
                }catch (Exception e){
                    yrc.printErr("Internal Error");
                }
                return true;
            }
        });
        GLOBCONSTR.put("Function", new OnConstructor() {
            @Override
            public void onConstr(String var, String scr) {
                GLOB.put(var, new OnFunction() {
                    @Override
                    public boolean onFunc(String[] args, YrC yrc) {
                        YrC y = new YrC(appname);
                        y.GLOB = yrc.GLOB;
                        for(int i = 1; i != args.length ; i++){
                            y.setVar(var+".args."+(i-1), y.ParseText(args[i]));
                            y.setVar("this.args."+(i-1), y.ParseText(args[i]));
                        }
                        y.setVar(var+".args.length", String.valueOf(args.length - 1));
                        y.setVar("this.args.length", String.valueOf(args.length - 1));
                        y.parse(scr);
                        yrc.GLOB = y.GLOB;
                        return true;
                    }
                });
            }
        });
    }
//    public void initGlobs(YrC yrc){
////        Object thi = GLOB.get("this");
////        //Надо сделац нормальный зис
////        GLOB = yrc.GLOB;
////        GLOB.put("this", thi);
////        GLOBCONSTR = yrc.GLOBCONSTR;
//    }

    private boolean IFFunc(String[] argss){
        if(argss[4].equals("cast")){
            OnFunction rn = (OnFunction) GLOB.get(argss[5]);
            StringBuilder newArgs = new StringBuilder();
            for(int i = 6; i != argss.length; i+=1){
                newArgs.append(" ").append(argss[i]);
            }
            return rn.onFunc(newArgs.toString().split(" "), this);
        }else if(argss[4].equals("to")){
            setVar(argss[5], "true");
        }else printErr("Ожидается тип: cast");
        return true;
    }

    private YrC getThis(){
        return this;
    }
    public interface OnFunction {
        boolean onFunc(String[] args, YrC yrc);
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

                }
                else if(args[0].equals("wait")) {
                    TimeUnit.MILLISECONDS.sleep(Integer.parseInt(ParseText(args[1])));
                }
                else
                if(GLOB.get(args[0]) != null){
                    try{
                        OnFunction rn = (OnFunction) GLOB.get(args[0]);
                        boolean as = rn.onFunc(args, this);
                        if(!as){
                            return false;
                        }
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
                    int a = Integer.parseInt(String.valueOf(GLOB.get(args[0])));
                    a += Integer.parseInt(ParseText(args[2]));
                    GLOB.put(args[0], a);
                } else if(args.length > 2 && args[1].equals("-=")) {
                    String prs = ParseText(Arrays.toString(args).replace(",", ""));
                    if (prs == null) {
                        System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                        return false;
                    }
                    int a = Integer.parseInt(String.valueOf(GLOB.get(args[0])));
                    a -= Integer.parseInt(ParseText(args[2]));
                    GLOB.put(args[0], a);
                } else if(args.length > 2 && args[1].equals("*=")) {
                    String prs = ParseText(Arrays.toString(args).replace(",", ""));
                    if (prs == null) {
                        System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                        return false;
                    }
                    //System.out.println(args[0]);
                    int a = Integer.parseInt(String.valueOf(GLOB.get(args[0])));
                    a *= Integer.parseInt(ParseText(args[2]));
                    GLOB.put(args[0], a);
                } else if(args.length > 2 && args[1].equals("/=")) {
                    String prs = ParseText(Arrays.toString(args).replace(",", ""));
                    if (prs == null) {
                        System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                        return false;
                    }
                    int a = Integer.parseInt(String.valueOf(GLOB.get(args[0])));
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
                    rn.onFunc(args, this);
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
                int a = Integer.parseInt(String.valueOf(GLOB.get(args[0])));
                a += Integer.parseInt(ParseText(args[2]));
                GLOB.put(args[0], a);
            } else if(args.length > 2 && args[1].equals("-=")) {
                String prs = ParseText(Arrays.toString(args).replace(",", ""));
                if (prs == null) {
                    System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                    return false;
                }
                int a = Integer.parseInt(String.valueOf( GLOB.get(args[0])));
                a -= Integer.parseInt(ParseText(args[2]));
                GLOB.put(args[0], a);
            } else if(args.length > 2 && args[1].equals("*=")) {
                String prs = ParseText(Arrays.toString(args).replace(",", ""));
                if (prs == null) {
                    System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                    return false;
                }
                //System.out.println(args[0]);
                int a = Integer.parseInt(String.valueOf( GLOB.get(args[0])));
                a *= Integer.parseInt(ParseText(args[2]));
                GLOB.put(args[0], a);
            } else if(args.length > 2 && args[1].equals("/=")) {
                String prs = ParseText(Arrays.toString(args).replace(",", ""));
                if (prs == null) {
                    System.err.println("Ошибка! textParse Error! '" + args[0] + ":" + read + "'");
                    return false;
                }
                int a = Integer.parseInt(String.valueOf( GLOB.get(args[0])));
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
        try {
            String[] strd = str.split("\"");
            if (strd.length < 2) {
                str = GLOB.get(str).toString();
                if (str == null) return null;
            } else {
                str = strd[1];
            }
            str = str.replace("/SP", " ");
            str = str.replace("/RE", "\n");
            return str;
        }catch (Exception e){
            return null;
        }
    }

}
