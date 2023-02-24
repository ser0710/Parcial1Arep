package edu.escuelaing.arep.app;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.*;
import java.io.*;
import java.util.Objects;

public class HttpServer {
    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ServerSocket serverSocket = null;
        String clas = null;
        Method methods[] = null;
        Field fields[] = null;
        StringBuilder strFields = new StringBuilder("");;
        StringBuilder strMethods = new StringBuilder("");
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 36000.");
            System.exit(1);
        }
        boolean runnig = true;
        while(runnig){
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine = basehtml();
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Recibí: " + inputLine);
                if(inputLine.startsWith("GET") && !inputLine.contains("/favicon.ico") && inputLine.length() > 15){
                    if(inputLine.contains("Class")){
                        clas = inputLine.split("=")[1].split(" ")[0].replace("Class(", "").replace(")", "");
                        Class<?> c = Class.forName(clas);
                        methods = c.getDeclaredMethods();
                        fields =  c.getFields();
                        for (Field field : fields) {
                            String strField = field.getName();
                            strField += " ";
                            strFields.append(strField);
                        }
                        for (Method method : methods) {
                            String strMethod = method.getName();
                            strMethod += " ";
                            strMethods.append(strMethod);
                        }
                        outputLine = classHtml(String.valueOf(strMethods), String.valueOf(strFields));
                    }else if(inputLine.contains("invoke")){
                        clas = inputLine.split("=")[1].split(" ")[0].replace("invoke(", "").replace(")", "");
                        String className = clas.split(",")[0];
                        String methodName = clas.split(",")[1];
                        Class<?> c = Class.forName(className);
                        Method m = c.getDeclaredMethod(methodName);
                        outputLine = methodsHtml(m.invoke(null));
                    }else if(inputLine.contains("unaryInvoke")){
                        clas = inputLine.split("=")[1].split(" ")[0].replace("unaryInvoke(", "").replace(")", "");
                        String className = clas.split(",")[0];
                        String methodName = clas.split(",")[1];
                        String methodType = clas.split(",")[2];
                        String methodParam = clas.split(",")[3];
                        Class<?> c = Class.forName(className);
                        Method m = c.getMethod(methodName, (Class<?>) tipo(methodType));
                        outputLine = methodsHtml(m.invoke(null, cast(methodType, methodParam)));
                    }
                }
                if (!in.ready()) {
                    break;
                }
            }


            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    public static String basehtml() {
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Form Example</title>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>REALIZAR LA BUSQUEDA SIN DEJAR ESPACIOS</h1>\n" +
                "        <form action=\"/hello\">\n" +
                "            <label for=\"name\">Name:</label><br>\n" +
                "            <input type=\"text\" id=\"name\" name=\"name\" value=\"John\"><br><br>\n" +
                "            <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                "        </form> \n" +
                "        <div id=\"getrespmsg\"></div>\n" +
                "\n" +
                "        <script>\n" +
                "            function loadGetMsg() {\n" +
                "                let nameVar = document.getElementById(\"name\").value;\n" +
                "                const xhttp = new XMLHttpRequest();\n" +
                "                xhttp.onload = function() {\n" +
                "                    document.getElementById(\"getrespmsg\").innerHTML =\n" +
                "                     this.responseText; \n" +
                "                }\n" +
                "                xhttp.open(\"GET\", \"/consulta?name=\"+nameVar);\n" +
                "                xhttp.send();\n" +
                "            }\n" +
                "        </script>\n" +
                "\n" +
                "    </body>\n" +
                "</html>";
    }

    public static String classHtml(String methods, String fields){
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n" +
                "METODOS: " +
                methods +
                "\r\n" +
                "ATRIBUTOS: " +
                fields;
    }

    public static String methodsHtml(Object obj){
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n" +
                obj;
    }

    private static Class tipo(String s){
        if(Objects.equals(s, "int")){
            return int.class;
        }else if(Objects.equals(s, "double")){
            return double.class;
        }
        return Character.TYPE;
    }

    private static Object cast(String s, String param){
        if(Objects.equals(s, "int")){
            return Integer.parseInt(param);
        }else if(Objects.equals(s, "double")){
            return Double.valueOf(param);
        }
        return Character.TYPE;
    }
}



















