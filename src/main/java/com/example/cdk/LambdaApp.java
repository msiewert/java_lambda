package com.example.cdk;

import software.amazon.awscdk.App;

public class LambdaApp {
    public static void main(final String[] args) {
        App app = new App();
        new LambdaStack(app, "JavaLambdaStack", null);
        app.synth();
    }
}
