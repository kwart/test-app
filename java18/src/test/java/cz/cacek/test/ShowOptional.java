package cz.cacek.test;

import java.util.Optional;

public class ShowOptional {
    void show(Optional<String> v) {
        // @start region="example"
        // @highlight region substring="println"
        // @link region substring="System.out" target="System#out"
        if (v.isPresent()) {
            System.out.println("v: " + v.get());
        }
        // @end
        // @end
        // @end
    }
}