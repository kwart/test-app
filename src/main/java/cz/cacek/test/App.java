package cz.cacek.test;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.SubclassClassFilter;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) {
        ClassFinder finder = new ClassFinder();
        if (args != null)
            for (String arg : args)
                finder.add(new File(arg));
        StringTokenizer tok = new StringTokenizer(System.getProperty("sun.boot.class.path"), File.pathSeparator);
        while (tok.hasMoreTokens())
            finder.add(new File(tok.nextToken()));
        finder.addClassPath();

        ClassFilter filter = new SubclassClassFilter(Serializable.class);

        Collection<ClassInfo> foundClasses = new ArrayList<ClassInfo>();
        finder.findClasses(foundClasses, filter);

        SortedSet<String> packageNames = new TreeSet<>();

        for (ClassInfo classInfo : foundClasses) {
            String className = classInfo.getClassName();
            if (className.startsWith("cz.cacek."))
                continue;
            int dotPosition = className.lastIndexOf(".");
            if (dotPosition > 0) {
                packageNames.add(className.substring(0, dotPosition));
            }
        }

        
        System.out.println("<whitelist>");
        for (String pkg : packageNames) {
            System.out.println("  <package>" + pkg + "</package>");
        }
        System.out.println("</whitelist>");
    }
}
