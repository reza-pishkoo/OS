package os.hw1.master.query;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Query implements Comparable {
    private int priority;
    private int value;
    private Queue<String> programs_row = new LinkedList<>();


    public int getPriority() {
        return priority;
    }

    public Queue<String> getPrograms_row() {
        return programs_row;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Query(int priority, String query_string) {
        this.priority = priority;
        var space = query_string.split(" ");
        String input_number = space[1];
        value = Integer.parseInt(input_number);
        var programs_circle = space[0];
        for (String x:programs_circle.split("\\|")) {
            programs_row.add(x);
        }
    }


    @Override
    public int compareTo(Object o) {
        return priority - ((Query)o).priority;
    }
}
