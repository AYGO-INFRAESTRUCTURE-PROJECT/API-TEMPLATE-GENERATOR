package co.com.escuelaing.cloud;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;

public class AWSStack {

    public software.amazon.awscdk.core.Stack stack;

    public AWSStack(App app, String id, StackProps props) {
        this.stack = new Stack(
            app,
            id,
            props
        );
    }
}
