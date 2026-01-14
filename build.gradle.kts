plugins {
    java
    application
}

application {
    mainClass.set("com.example.cdk.LambdaApp")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.3")
    implementation("software.amazon.awssdk:dynamodb:2.20.26")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("software.amazon.awscdk:aws-cdk-lib:2.114.1")
    implementation("software.constructs:constructs:10.3.0")
}

tasks.register<Jar>("buildLambda") {
    archiveFileName.set("lambda.jar")
    from(sourceSets.main.get().output) {
        exclude("com/example/cdk/**")
    }
    into("lib") {
        from(configurations.runtimeClasspath.get().filter {
            !it.name.contains("aws-cdk") && !it.name.contains("constructs")
        })
    }
}
