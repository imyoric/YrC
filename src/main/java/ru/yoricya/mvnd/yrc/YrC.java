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
    public String CriticalMessage;
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
        GLOB.put("YrC.ver", "1.3/Beta");
        GLOB.put("YrC.codeVer", "003");
        GLOB.put("YrC.AppName", AppName);

        GLOB.put("print", new OnFunction() {
            @Override
            public Req onFunc(String[] argsg, Req req) {
                String prs = ParseText(Arrays.toString(argsg).replace(",", ""));
                argsg[1] = argsg[1].replace(" ", "");
                if(prs == null){
                    if(GLOB.get(argsg[1]) != null){
                        prs = GLOB.get(argsg[1]).toString();
                    }
                    else{
                        req.CriticalMessage = "Значение не установлено!";
                    }
                }
                System.out.println(prs);
                return req;
            }
        });
        addFunc("if", new OnFunction() {
            @Override
            public Req onFunc(String[] argss, Req req) {
                if(argss.length >= 5){
                    Req ret = req;
                    if(argss[2].equals("==")){
                        if(ParseText(argss[1]).equals(ParseText(argss[3]))) ret = IFFunc(argss, req);
                    }else if(argss[2].equals("!=")){
                        if(!ParseText(argss[1]).equals(ParseText(argss[3]))) ret = IFFunc(argss, req);
                    }else if(argss[2].equals(">")){
                        if(Long.parseLong(ParseText(argss[1])) > Long.parseLong(ParseText(argss[3]))) ret = IFFunc(argss, req);
                    }else if(argss[2].equals("<")){
                        if(Long.parseLong(ParseText(argss[1])) < Long.parseLong(ParseText(argss[3]))) ret = IFFunc(argss, req);
                    }else if(argss[2].equals(">=")){
                        if(Long.parseLong(ParseText(argss[1])) >= Long.parseLong(ParseText(argss[3]))) ret = IFFunc(argss, req);
                    }else if(argss[2].equals("<=")){
                        if(Long.parseLong(ParseText(argss[1])) <= Long.parseLong(ParseText(argss[3]))) ret = IFFunc(argss, req);
                    }else{
                        req.CriticalMessage = "Не известный оператор!";
                    }
                    req = req;
                    return req;
                }else{
                    req.CriticalMessage = "Не все аргументы указаны!";
                }
                return req;
            }
        });
        addFunc("ifset", new OnFunction() {
            @Override
            public boolean onFunc(String[] argss) {
                if(argss.length >= 5){
                    boolean isset = ParseText(argss[1]) != null;
                    boolean ret = true;
                    if(argss[2].equals("==")){
                        if(isset == Boolean.parseBoolean(ParseText(argss[3]))) ret = IFFunc(argss);
                    }else if(argss[2].equals("!=")) {
                        if(isset != Boolean.parseBoolean(ParseText(argss[3]))) ret = IFFunc(argss);
                    }else{
                        printErr("Не известный оператор!");
                    }
                    return ret;
                }else{
                    printErr("Не все аргументы указаны!");
                }
                return true;
            }
        });
        GLOB.put("new", new OnFunction() {
            @Override
            public Req onFunc(String[] args, Req req) {
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
                    req.onCritical = true;
                    req.CriticalMessage = "Конструкция не существует";
                }
                return req;
            }
        });
        GLOB.put("net_get_contents", new OnFunction() {
            @Override
            public Req onFunc(String[] args, Req req) {
                if(args.length > 2) {
                    GLOB.put(args[2],NetGetContents(ParseText(args[1])));
                }else{
                    req.CriticalMessage = "Произошла ошибка";
                }
                return req;
            }
        });
        addFunc("strCat", new OnFunction() {
            @Override
            public Req onFunc(String[] str, Req req) {
                if(str.length >= 5){
                    String a = ParseText(str[1]) + ParseText(str[3]);
                    setVar(str[5], a);
                }else req.CriticalMessage = "Не все аргументы указаны!";
                return req;
            }
        });
        addFunc("delete", new OnFunction() {
            @Override
            public Req onFunc(String[] str, Req req) {
                if(str.length == 2){
                    GLOB.remove(str[1]);
                }else req.CriticalMessage = "Синтаксическая ошибка";
                return req;
            }
        });
        addFunc("return", new OnFunction() {
            @Override
            public Req onFunc(String[] str, Req req) {
                if(str.length == 2){
                    req.onReturn = Boolean.parseBoolean(ParseText(str[1]));
                    return req;
                }else {
                    req.onReturn = true;
                    return req;
                }
            }
        });
        addFunc("exit", new OnFunction() {
            @Override
            public Req onFunc(String[] str, Req req) {
                if(str.length == 2){
                    System.exit(Integer.parseInt(ParseText(str[1])));
                }else {
                    System.exit(0);
                }
                return req;
            }
        });
        addFunc("final", new OnFunction() {
            @Override
            public Req onFunc(String[] args, Req req) {
                if(args.length < 3){
                    req.CriticalMessage = "Не все аргументы указаны";
                    return req;
                }else if(((Final) GLOB.get(args[1])) != null){
                    req.CriticalMessage = "FINAL - Переменная уже инициализирована";
                    req.onCritical = true;
                    return req;
                }
                Final a = new YrC.Final(ParseText(args[3]));
                GLOB.put(args[1], a);
                return req;
            }
        });
        addFunc("var", new OnFunction() {
            @Override
            public boolean onFunc(String[] str) {
                try {
                    if (str.length < 4) {
                        printErr("Не все аргументы указаны");
                    } else if (str[2].equals("=")) {
                        GLOB.put(str[1], ParseText(str[3]));
                    } else if (str[2].equals("+=")) {
                        int a = Integer.parseInt(ParseText(str[1]));
                        a+= Integer.parseInt(ParseText(str[3]));
                        setVar(str[1], String.valueOf(a));
                    } else if (str[2].equals("-=")) {
                        int a = Integer.parseInt(ParseText(str[1]));
                        a-= Integer.parseInt(ParseText(str[3]));
                        setVar(str[1], String.valueOf(a));
                    } else if (str[2].equals("/=")) {
                        int a = Integer.parseInt(ParseText(str[1]));
                        a/= Integer.parseInt(ParseText(str[3]));
                        setVar(str[1], String.valueOf(a));
                    } else if (str[2].equals("*=")) {
                        int a = Integer.parseInt(ParseText(str[1]));
                        a*= Integer.parseInt(ParseText(str[3]));
                        setVar(str[1], String.valueOf(a));
                    }else{
                        printErr("Неизвестный тип операции.");
                    }
                }catch (Exception e){
                    printErr("Internal Error");
                }
                return true;
            }
        });
        GLOBCONSTR.put("Function", new OnConstructor() {
            @Override
            public void onConstr(String var, String scr) {
                GLOB.put(var, new OnFunction() {
                    @Override
                    public Req onFunc(String[] args, Req req) {
                        YrC y = new YrC();
                        y.GLOBCONSTR = GLOBCONSTR;
                        y.GLOB = GLOB;

                    public boolean onFunc(String[] args) {
                        YrC y = new YrC(getThis());

                        for(int i = 1; i != args.length ; i++){
                            y.setVar(var+".args."+(i-1), y.ParseText(args[i]));
                            y.setVar("this.args."+(i-1), y.ParseText(args[i]));
                        }
                        y.setVar(var+".args.length", String.valueOf(args.length - 1));
                        y.setVar("this.args.length", String.valueOf(args.length - 1));
                        y.parse(scr);

                        GLOB = y.GLOB;
                        GLOBCONSTR = y.GLOBCONSTR;
                        return req;

                        initGlobs(y);
                        return true;

                    }
                });
            }
        });
    }

    private Req IFFunc(String[] argss, Req req){

    public void initGlobs(YrC yrc){
        Object thi = GLOB.get("this");
        //Надо сделац нормальный зис
        GLOB = yrc.GLOB;
        GLOB.put("this", thi);
        GLOBCONSTR = yrc.GLOBCONSTR;
    }

    private boolean IFFunc(String[] argss){

        if(argss[4].equals("cast")){
            OnFunction rn = (OnFunction) GLOB.get(argss[5]);
            StringBuilder newArgs = new StringBuilder();
            for(int i = 6; i != argss.length; i+=1){
                newArgs.append(" ").append(argss[i]);
            }

            return rn.onFunc(newArgs.toString().split(" "), req);
        }else req.CriticalMessage = "Ожидается тип: cast";
        return req;

            return rn.onFunc(newArgs.toString().split(" "));
        }else if(argss[4].equals("to")){
            setVar(argss[5], "true");
        }else printErr("Ожидается тип: cast");
        return true;

    }

    private YrC getThis(){
        return this;
    }
    public interface OnFunction {
        Req onFunc(String[] args, Req req);
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
    public boolean parse(String scr){
        Req req = parseToReq(scr);
        if(req.onCritical){
            printErr(req.CriticalMessage);
        }else{
            return true;
        }
        return false;
    }
    public Req parse(String scr, Req req){
        req = parseToReq(scr);
        if(req.onCritical) {
            printErr(req.CriticalMessage);
        }
        return req;
    }
    public Req parseToReq(String scro){
        Req req = null;
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
                        req = new Req();
                        OnFunction rn = (OnFunction) GLOB.get(args[0]);
                        Req as = rn.onFunc(args, req);
                        if (as.onCritical) {
                            CriticalMessage = as.CriticalMessage;
                            return req;
                        } else if (as.onReturn) return req;
                    }catch (ClassCastException e){
                        e.printStackTrace();
                    }catch (Exception e) {
                        printErr("Внутренняя ошибка");
                        e.printStackTrace();
                    }
                }else if(args.length == 1){
                    System.err.println("Ошибка! '"+args[0]+ ":"+read+"' - Переменная/Функция не инициализирована!");
                }
                //printErr(args.length);
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
        return req;
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
//                try{
//                    OnFunction rn = (OnFunction) GLOB.get(args[0]);
//                    rn.onFunc(args);
//                }catch (Exception e){
//                    if(args.length == 1) {
//                        System.out.println(GLOB.get(args[0]));
//                    }
//                }
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
    public static class Final{
        public Object value = "";
        Final(Object valu){
            this.value = valu;
        }
        @Override
        public String toString() {
            return value.toString();
        }
    }
    public static class Req{
        boolean onCritical = false;
        boolean onReturn = false;
        String CriticalMessage = "";
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
