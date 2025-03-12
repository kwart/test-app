package cz.cacek.mvnquery;

import com.beust.jcommander.DefaultUsageFormatter;
import com.beust.jcommander.JCommander;

public class UsageFormatter extends DefaultUsageFormatter {

    public UsageFormatter(JCommander commander) {
        super(commander);
    }

    @Override
    public void appendMainLine(StringBuilder out, boolean hasOptions, boolean hasCommands, int indentCount, String indent) {
        StringBuilder mainLine = new StringBuilder();
        out.append("MvnQuery retrieves Maven repository index and makes query on it.\n");
        out.append("\n");
        mainLine.append("Usage:\n");
        mainLine.append(indent).append("java -jar mvnquery.jar [options]\n");

        wrapDescription(out, indentCount, mainLine.toString());
        out.append("\n");
    }

}
