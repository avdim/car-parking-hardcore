package com.murkitty.parking.lib;

import java.util.*;

public class LibAllGwt {

    public static boolean nullOrEmpty(String s) {
        if(s == null) {
            return true;
        }
        if(s.trim().length()==0) {
            return true;
        }
        return false;
    }

    public static boolean strEquals(String str1, String str2) {
        if(str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

    public static int getRand() {
        return (int) (Math.random() * Integer.MAX_VALUE);
    }

    public static int getRand(int min, int max) {
        return min + getRand()%(max - min + 1);
    }

    public static String putStrArgs(String str, StrArgs args) {//todo optimize me
        String result = str;
        for (String key : args.map.keySet()) {
            result = result.replaceAll("[{]" + key + "[}]", args.map.get(key));
        }
        return result;
    }

    public static class StrArgs {
        private final HashMap<String, String> map;
        public StrArgs() {
            map = new HashMap<String, String>();
        }
        public StrArgs put(String key, String value) {
            map.put(key, value);
            return this;
        }
        public HashMap<String, String> getMap() {
            return map;
        }

        public StrArgs applyForAllValues(IApplyForAllValues handler) {
            for (String k : map.keySet()) {
                map.put(k, handler.handle(map.get(k)));
            }
            return this;
        }

        public interface IApplyForAllValues {
            String handle(String value);
        }
    }

    public static boolean isIp(String str) {
        return str.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
    }

    public static Properties readProperties(String txt) {
        return new Properties(txt);
    }

    public static Properties createProperties() {
        return new Properties();
    }

    public static class Properties {

        List<Element> elements = new ArrayList<Element>();
        Map<String, Property> map = new HashMap<String, Property>();

        private Properties() {

        }

        private Properties(String txt) {
            for (String line : txt.split("\n")) {
                if(line.length() == 0) {
                    continue;
                }
                if(line.charAt(0) == '#') {
                    elements.add(new Comment(line.substring(1)));
                } else {
                    String[] split = line.split("=");
                    Property property = new Property(split[0]);
                    for(int i=1; i < split.length; i++) {
                        property.value += split[i];
                        if(i != split.length - 1) {//Если не последний элемент
                            property.value+="=";
                        }
                    }
                    elements.add(property);
                    map.put(property.name, property);
                }
            }
        }

        public void setProperty(String name, String value) {
            Property prop;
            if(map.containsKey(name)) {
                prop = map.get(name);
            } else {
                prop = new Property(name);
                map.put(name, prop);
                elements.add(prop);
            }
            prop.value = value;
        }

        public String getProperty(String name) {
            Property property = map.get(name);
            if(property != null) {
                return property.value;
            } else {
                return null;
            }
        }

        public String toString() {
            StringBuilder result = new StringBuilder("");
            Iterator<Element> iterator = elements.iterator();
            while (iterator.hasNext()) {
                result.append(iterator.next().toStr());
                if (iterator.hasNext()) {
                    result.append("\n");
                }
            }
            return result.toString();
        }

        interface Element {
            String toStr();
        }

        static class Comment implements Element {
            private final String comment;

            public Comment(String comment) {
                this.comment = comment;
            }

            @Override
            public String toStr() {
                return "#" + comment;
            }
        }

        static class Property implements Element {
            public final String name;
            public String value = "";

            public Property(String name) {
                this.name = name;
            }

            @Override
            public String toStr() {
                return name + "=" + value;
            }
        }
    }

    public static <T> T doNow(IDoNow <T> doNow) {
        return doNow.doNow();
    }

    public interface IDoNow <T> {
        T doNow();
    }

}
